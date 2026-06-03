package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.Dispositivo;
import com.cj.agrotech.domain.entity.Finca;
import com.cj.agrotech.domain.entity.Lote;
import com.cj.agrotech.domain.document.Telemetria;
import com.cj.agrotech.domain.enums.EstadoDispositivo;
import com.cj.agrotech.dto.OpenMeteoCurrentResponse;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.FincaRepository;
import com.cj.agrotech.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeteoService {

    private final FincaRepository fincaRepository;
    private final LoteRepository loteRepository;
    private final TelemetriaService telemetriaService;
    private final RestTemplate restTemplate;

    @Transactional
    public void sincronizarClimaPorFinca(UUID fincaId) {
        Finca finca = fincaRepository.findById(fincaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca no encontrada"));

        Double latitud = finca.getLatitud();
        Double longitud = finca.getLongitud();

        ingestClimaForCoordinates(fincaId, null, null, latitud, longitud);

        List<Lote> lotes = finca.getLotes();
        if (lotes == null || lotes.isEmpty()) {
            return;
        }

        for (Lote lote : lotes) {
            ingestClimaForLote(fincaId, lote, latitud, longitud);
        }
    }

    @Transactional
    public void sincronizarClimaPorLote(UUID loteId) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));

        Finca finca = lote.getFinca();
        if (finca == null) {
            throw new ResourceNotFoundException("Finca asociada al lote no encontrada");
        }

        ingestClimaForLote(finca.getId(), lote, finca.getLatitud(), finca.getLongitud());
    }

    private void ingestClimaForLote(UUID fincaId, Lote lote, Double latitud, Double longitud) {
        if (lote == null) {
            return;
        }

        List<Dispositivo> dispositivos = lote.getDispositivos() == null
                ? Collections.emptyList()
                : lote.getDispositivos();
        List<Dispositivo> activos = dispositivos.stream()
                .filter(d -> d.getEstado() == EstadoDispositivo.ACTIVO)
                .collect(Collectors.toList());

        if (activos.isEmpty()) {
            // Usuario sin sensores: telemetría solo por lote (dispara alertas de lote)
            ingestClimaForCoordinates(fincaId, lote.getId(), null, latitud, longitud);
        } else {
            // Con sensores activos: también se ingiere clima por dispositivo (alertas de dispositivo + lote)
            activos.forEach(dispositivo ->
                    ingestClimaForCoordinates(fincaId, lote.getId(), dispositivo.getId(), latitud, longitud));
        }
    }

    private void ingestClimaForCoordinates(UUID fincaId, UUID loteId, UUID dispositivoId, Double latitud, Double longitud) {
        if (latitud == null || longitud == null) {
            log.warn("Finca/lote sin coordenadas; no se puede consultar Open-Meteo (loteId={})", loteId);
            return;
        }

        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitud +
                "&longitude=" + longitud +
                "&current_weather=true&hourly=relativehumidity_2m,precipitation,wind_speed_10m,surface_pressure,soil_temperature_0_to_7cm,soil_moisture_0_to_7cm&timezone=auto";

        try {
            log.debug("Open-Meteo loteId={} dispositivoId={}", loteId, dispositivoId);
            OpenMeteoCurrentResponse response = restTemplate.getForObject(url, OpenMeteoCurrentResponse.class);

            if (response == null) {
                log.warn("Respuesta nula de Open-Meteo (loteId={})", loteId);
                return;
            }

            if (response.current_weather() != null
                    && response.hourly() != null
                    && response.hourly().time() != null
                    && !response.hourly().time().isEmpty()) {
                persistirDesdeOpenMeteo(fincaId, loteId, dispositivoId, response, true);
            }
        } catch (HttpClientErrorException httpEx) {
            log.warn("Open-Meteo error (loteId={}): {}. Usando fallback.", loteId, httpEx.getMessage());
            ingestFallback(fincaId, loteId, dispositivoId, latitud, longitud);
        } catch (Exception e) {
            log.error("Error Open-Meteo (loteId={}): {}", loteId, e.getMessage(), e);
        }
    }

    private void ingestFallback(UUID fincaId, UUID loteId, UUID dispositivoId, Double latitud, Double longitud) {
        try {
            String fallbackUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + latitud +
                    "&longitude=" + longitud +
                    "&current_weather=true&timezone=auto";
            OpenMeteoCurrentResponse fallbackResponse = restTemplate.getForObject(fallbackUrl, OpenMeteoCurrentResponse.class);
            if (fallbackResponse != null && fallbackResponse.current_weather() != null) {
                persistirDesdeOpenMeteo(fincaId, loteId, dispositivoId, fallbackResponse, false);
            }
        } catch (Exception ex) {
            log.error("Fallback Open-Meteo falló (loteId={}): {}", loteId, ex.getMessage(), ex);
        }
    }

    private void persistirDesdeOpenMeteo(
            UUID fincaId,
            UUID loteId,
            UUID dispositivoId,
            OpenMeteoCurrentResponse response,
            boolean conHourly) {

        Float humedad = null;
        Float precipitacion = null;
        Float presion = null;
        Float tempSuelo = null;
        Float humSuelo = null;

        if (conHourly && response.hourly() != null && response.hourly().time() != null && !response.hourly().time().isEmpty()) {
            int lastIndex = response.hourly().time().size() - 1;
            humedad = safeGet(response.hourly().relativehumidity_2m(), lastIndex);
            precipitacion = safeGet(response.hourly().precipitation(), lastIndex);
            presion = safeGet(response.hourly().surface_pressure(), lastIndex);
            tempSuelo = safeGet(response.hourly().soil_temperature_0_to_7cm(), lastIndex);
            Float humSueloRaw = safeGet(response.hourly().soil_moisture_0_to_7cm(), lastIndex);
            humSuelo = humSueloRaw == null ? null : humSueloRaw * 100;
        }

        Telemetria telemetria = Telemetria.builder()
                .fincaId(fincaId)
                .loteId(loteId)
                .dispositivoId(dispositivoId)
                .timestamp(Instant.now())
                .lecturas(new Telemetria.Lecturas(
                        new Telemetria.Ambiente(
                                response.current_weather().temperature(),
                                humedad,
                                presion,
                                null),
                        new Telemetria.Suelo(humSuelo, tempSuelo),
                        new Telemetria.Clima(
                                precipitacion,
                                response.current_weather().windspeed())))
                .diagnostico(new Telemetria.Diagnostico(100, -50))
                .build();

        telemetriaService.guardarLecturaClimatica(telemetria);
        log.info("Clima Open-Meteo guardado y alertas evaluadas (loteId={}, dispositivoId={})", loteId, dispositivoId);
    }

    private Float safeGet(List<Float> values, int index) {
        if (values == null || values.isEmpty() || index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }
}
