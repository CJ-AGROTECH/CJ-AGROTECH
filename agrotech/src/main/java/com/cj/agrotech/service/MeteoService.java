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
import org.springframework.transaction.annotation.Transactional;
import com.cj.agrotech.repository.TelemetriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeteoService {

    private final FincaRepository fincaRepository;
    private final LoteRepository loteRepository;
    private final TelemetriaRepository telemetriaRepository;
    private final RestTemplate restTemplate;

    @Transactional(readOnly = true)
    public void sincronizarClimaPorFinca(UUID fincaId) {
        Finca finca = fincaRepository.findById(fincaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca no encontrada"));

        Double latitud = finca.getLatitud();
        Double longitud = finca.getLongitud();

        List<Lote> lotes = finca.getLotes();
        // Siempre ingestar datos generales de clima de la zona para la finca
        ingestClimaForCoordinates(null, latitud, longitud);

        if (lotes == null || lotes.isEmpty()) {
            return;
        }

        for (Lote lote : lotes) {
            if (shouldIngestForLote(lote)) {
                ingestClimaForCoordinates(lote.getId(), latitud, longitud);
            }
        }
    }

    @Transactional(readOnly = true)
    public void sincronizarClimaPorLote(UUID loteId) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));

        Finca finca = lote.getFinca();
        if (finca == null) {
            throw new ResourceNotFoundException("Finca asociada al lote no encontrada");
        }

        Double latitud = finca.getLatitud();
        Double longitud = finca.getLongitud();

        if (shouldIngestForLote(lote)) {
            ingestClimaForLote(lote, latitud, longitud);
        }
    }

    private boolean shouldIngestForLote(Lote lote) {
        List<Dispositivo> dispositivos = lote.getDispositivos();
        return dispositivos == null || dispositivos.isEmpty() || dispositivos.stream().noneMatch(d -> d.getEstado() == EstadoDispositivo.ACTIVO);
    }

    private void ingestClimaForLote(Lote lote, Double latitud, Double longitud) {
        ingestClimaForCoordinates(lote != null ? lote.getId() : null, latitud, longitud);
    }

    private void ingestClimaForCoordinates(UUID loteId, Double latitud, Double longitud) {
        if (latitud == null || longitud == null) {
            return;
        }

        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitud +
                "&longitude=" + longitud +
                "&current_weather=true&hourly=time,relativehumidity_2m,precipitation,wind_speed_10m,temperature_2m&timezone=auto";

        try {
            log.info("Consultando Open-Meteo para coordinates (loteId={}): {}", loteId, url);
            OpenMeteoCurrentResponse response = restTemplate.getForObject(url, OpenMeteoCurrentResponse.class);

            if (response == null) {
                log.warn("Respuesta nula de Open-Meteo para coordinates (loteId={})", loteId);
                return;
            }

            if (response.current_weather() != null && response.hourly() != null && response.hourly().time() != null && !response.hourly().time().isEmpty()) {
                int lastIndex = response.hourly().time().size() - 1;
                Float humedad = safeGet(response.hourly().relativehumidity_2m(), lastIndex);
                Float precipitacion = safeGet(response.hourly().precipitation(), lastIndex);

                Telemetria telemetria = Telemetria.builder()
                        .loteId(loteId)
                        .dispositivoId(null)
                        .timestamp(Instant.now())
                        .lecturas(new Telemetria.Lecturas(
                                new Telemetria.Ambiente(
                                        response.current_weather().temperature(),
                                        humedad,
                                        0.0f,
                                        0.0f
                                ),
                                null,
                                new Telemetria.Clima(
                                        precipitacion,
                                        response.current_weather().windspeed()
                                )
                        ))
                        .build();

                telemetriaRepository.save(telemetria);
                log.info("Telemetría guardada para coordinates (loteId={})", loteId);
            }
        } catch (HttpClientErrorException httpEx) {
            log.warn("Open-Meteo returned HTTP error for coordinates (loteId={}): {}. Trying fallback without hourly fields.", loteId, httpEx.getMessage());
            // Fallback: request only current weather (no hourly) and build Telemetria from current_weather
            try {
                String fallbackUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + latitud +
                        "&longitude=" + longitud +
                        "&current_weather=true&timezone=auto";
                log.info("Consultando Open-Meteo fallback para coordinates (loteId={}): {}", loteId, fallbackUrl);
                OpenMeteoCurrentResponse fallbackResponse = restTemplate.getForObject(fallbackUrl, OpenMeteoCurrentResponse.class);

                if (fallbackResponse != null && fallbackResponse.current_weather() != null) {
                    Telemetria telemetria = Telemetria.builder()
                            .loteId(loteId)
                            .dispositivoId(null)
                            .timestamp(Instant.now())
                            .lecturas(new Telemetria.Lecturas(
                                    new Telemetria.Ambiente(
                                            fallbackResponse.current_weather().temperature(),
                                            null,
                                            0.0f,
                                            0.0f
                                    ),
                                    null,
                                    new Telemetria.Clima(
                                            null,
                                            fallbackResponse.current_weather().windspeed()
                                    )
                            ))
                            .build();

                    telemetriaRepository.save(telemetria);
                    log.info("Telemetría guardada (fallback) para coordinates (loteId={})", loteId);
                }
            } catch (Exception ex) {
                log.error("Fallback failed for coordinates (loteId={}): {}", loteId, ex.getMessage(), ex);
            }
        } catch (Exception e) {
            log.error("Error al consultar Open-Meteo o guardar telemetría para coordinates (loteId={}): {}", loteId, e.getMessage(), e);
        }
    }

    private Float safeGet(List<Float> values, int index) {
        if (values == null || values.isEmpty() || index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }
}
