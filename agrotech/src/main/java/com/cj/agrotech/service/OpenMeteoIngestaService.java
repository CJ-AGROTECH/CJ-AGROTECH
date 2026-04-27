package com.cj.agrotech.service;

import com.cj.agrotech.dto.TelemetriaCapturaDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenMeteoIngestaService {

    private final WebClient webClient;
    private final TelemetriaService telemetriaService;

    // Cronjob cada 15 minutos para ingesta automática
    @Scheduled(fixedRate = 900000) // 15 minutos en milisegundos
    public void ingestarDatosClimaticosAutomaticamente() {
        // Para cada dispositivo activo, ingestar datos
        // Aquí se asume que se obtiene la lista de dispositivos activos
        // Por simplicidad, se puede implementar en el futuro con un método que obtenga dispositivos
        log.info("Iniciando ingesta automática de datos climáticos desde Open-Meteo");
        // Ejemplo: ingestarDatosClimaticos(dispositivoId, loteId, lat, lon);
    }

    public void ingestarDatosClimaticos(UUID dispositivoId, UUID loteId, Double lat, Double lon) {
        JsonNode response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("latitude", lat)
                        .queryParam("longitude", lon)
                        .queryParam("current", "temperature_2m,relative_humidity_2m,surface_pressure,precipitation,wind_speed_10m,soil_temperature_0_to_7cm,soil_moisture_0_to_7cm")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response != null && response.has("current")) {
            JsonNode current = response.get("current");

            TelemetriaCapturaDTO dto = new TelemetriaCapturaDTO(
                    dispositivoId,
                    loteId,
                    new TelemetriaCapturaDTO.LecturasDTO(
                            new TelemetriaCapturaDTO.AmbienteDTO(
                                    current.get("temperature_2m").floatValue(),
                                    current.get("relative_humidity_2m").floatValue(),
                                    current.get("surface_pressure").floatValue(),
                                    1000.0f // Dato estático de relleno para LUX
                            ),
                            new TelemetriaCapturaDTO.SueloDTO(
                                    current.get("soil_moisture_0_to_7cm").floatValue(),
                                    current.get("soil_temperature_0_to_7cm").floatValue()
                            ),
                            new TelemetriaCapturaDTO.ClimaDTO(
                                    current.get("precipitation").floatValue(),
                                    current.get("wind_speed_10m").floatValue()
                            )
                    ),
                    new TelemetriaCapturaDTO.DiagnosticoDTO(100, -50)
            );

            telemetriaService.registrarCaptura(dto);
            log.info("Datos climáticos ingestados para dispositivo {}", dispositivoId);
        }
    }
}
