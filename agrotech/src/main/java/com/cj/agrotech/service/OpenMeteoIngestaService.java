package com.cj.agrotech.service;

import com.cj.agrotech.dto.TelemetriaCapturaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OpenMeteoIngestaService {

    private final WebClient webClient;
    private final TelemetriaService telemetriaService;

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
        }
    }
}
