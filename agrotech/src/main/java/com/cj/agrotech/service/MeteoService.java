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
import com.cj.agrotech.repository.TelemetriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeteoService {

    private final FincaRepository fincaRepository;
    private final LoteRepository loteRepository;
    private final TelemetriaRepository telemetriaRepository;
    private final RestTemplate restTemplate;

    public void sincronizarClimaPorFinca(UUID fincaId) {
        Finca finca = fincaRepository.findById(fincaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca no encontrada"));

        Double latitud = finca.getLatitud();
        Double longitud = finca.getLongitud();

        for (Lote lote : finca.getLotes()) {
            if (shouldIngestForLote(lote)) {
                ingestClimaForLote(lote, latitud, longitud);
            }
        }
    }

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
        if (latitud == null || longitud == null) {
            return;
        }

        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitud +
                "&longitude=" + longitud +
                "&current_weather=true&hourly=time,relativehumidity_2m,precipitation,surface_pressure&timezone=auto";

        OpenMeteoCurrentResponse response = restTemplate.getForObject(url, OpenMeteoCurrentResponse.class);

        if (response != null && response.current_weather() != null && response.hourly() != null && response.hourly().time() != null && !response.hourly().time().isEmpty()) {
            int lastIndex = response.hourly().time().size() - 1;
            Float humedad = safeGet(response.hourly().relativehumidity_2m(), lastIndex);
            Float precipitacion = safeGet(response.hourly().precipitation(), lastIndex);
            Float presion = safeGet(response.hourly().surface_pressure(), lastIndex);

            Telemetria telemetria = Telemetria.builder()
                    .loteId(lote.getId())
                    .dispositivoId(null)
                    .timestamp(Instant.now())
                    .lecturas(new Telemetria.Lecturas(
                            new Telemetria.Ambiente(
                                    response.current_weather().temperature(),
                                    humedad,
                                    presion,
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
        }
    }

    private Float safeGet(List<Float> values, int index) {
        if (values == null || values.isEmpty() || index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }
}
