package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.Dispositivo;
import com.cj.agrotech.domain.entity.Finca;
import com.cj.agrotech.domain.entity.Lote;
import com.cj.agrotech.domain.entity.document.Telemetria;
import com.cj.agrotech.dto.OpenMeteoCurrentResponse;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.FincaRepository;
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
    private final TelemetriaRepository telemetriaRepository;

    public void sincronizarClimaPorFinca(UUID fincaId) {
        Finca finca = fincaRepository.findById(fincaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca no encontrada"));

        Double latitud = finca.getLatitud();
        Double longitud = finca.getLongitud();

        for (Lote lote : finca.getLotes()) {
            List<Dispositivo> dispositivos = lote.getDispositivos();
            if (dispositivos.isEmpty() || dispositivos.stream().noneMatch(d -> d.getEstado() == EstadoDispositivo.ACTIVO)) {
                String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitud + "&longitude=" + longitud + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,precipitation";
                RestTemplate restTemplate = new RestTemplate();
                OpenMeteoCurrentResponse response = restTemplate.getForObject(url, OpenMeteoCurrentResponse.class);

                if (response != null && response.getCurrent() != null) {
                    Telemetria telemetria = Telemetria.builder()
                            .loteId(lote.getId())
                            .dispositivoId(null)
                            .timestamp(Instant.now())
                            .lecturas(Telemetria.Lecturas.builder()
                                    .ambiente(Telemetria.Ambiente.builder()
                                            .tempAire(response.getCurrent().getTemperature_2m())
                                            .humAire(response.getCurrent().getRelative_humidity_2m())
                                            .build())
                                    .clima(Telemetria.Clima.builder()
                                            .precipitacion(response.getCurrent().getPrecipitation())
                                            .viento(response.getCurrent().getWind_speed_10m())
                                            .build())
                                    .build())
                            .build();

                    telemetriaRepository.save(telemetria);
                }
            }
        }
    }
}
