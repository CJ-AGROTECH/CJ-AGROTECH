package com.cj.agrotech.service;

import com.cj.agrotech.dto.TelemetriaCapturaDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenMeteoIngestaService {

    private final RestTemplate restTemplate;
    private final TelemetriaService telemetriaService;
    private final ObjectMapper objectMapper;

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
        try {
                String url = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=" + lat + "&longitude=" + lon + "&" +
                    "current_weather=true&hourly=temperature_2m,relativehumidity_2m,precipitation,wind_speed_10m,soil_temperature_0_to_7cm,soil_moisture_0_to_7cm&timezone=auto";

            String jsonResponse = restTemplate.getForObject(url, String.class);
            JsonNode response = objectMapper.readTree(jsonResponse);

            if (response != null && response.has("hourly") && response.has("current_weather")) {
                JsonNode hourly = response.get("hourly");
                JsonNode currentWeather = response.get("current_weather");
                JsonNode times = hourly.get("time");
                int lastIndex = (times != null && times.isArray()) ? times.size() - 1 : -1;

                if (lastIndex >= 0) {
                    Float temperature = currentWeather.has("temperature") ? currentWeather.get("temperature").floatValue() : null;
                    Float humidity = hourly.has("relativehumidity_2m") ? hourly.get("relativehumidity_2m").get(lastIndex).floatValue() : null;
                    Float pressure = hourly.has("surface_pressure") ? hourly.get("surface_pressure").get(lastIndex).floatValue() : null;
                    Float precipitation = hourly.has("precipitation") ? hourly.get("precipitation").get(lastIndex).floatValue() : null;
                    Float windSpeed = hourly.has("wind_speed_10m") ? hourly.get("wind_speed_10m").get(lastIndex).floatValue() : null;
                    Float soilMoisture = hourly.has("soil_moisture_0_to_7cm") ? hourly.get("soil_moisture_0_to_7cm").get(lastIndex).floatValue() : null;
                    Float soilTemperature = hourly.has("soil_temperature_0_to_7cm") ? hourly.get("soil_temperature_0_to_7cm").get(lastIndex).floatValue() : null;

                    TelemetriaCapturaDTO dto = new TelemetriaCapturaDTO(
                            dispositivoId,
                            loteId,
                            new TelemetriaCapturaDTO.LecturasDTO(
                                    new TelemetriaCapturaDTO.AmbienteDTO(
                                            temperature,
                                            humidity,
                                            pressure,
                                            1000.0f
                                    ),
                                    new TelemetriaCapturaDTO.SueloDTO(
                                            soilMoisture,
                                            soilTemperature
                                    ),
                                    new TelemetriaCapturaDTO.ClimaDTO(
                                            precipitation,
                                            windSpeed
                                    )
                            ),
                            new TelemetriaCapturaDTO.DiagnosticoDTO(100, -50)
                    );

                    telemetriaService.registrarCaptura(dto);
                    log.info("Datos climáticos ingestados para dispositivo {}", dispositivoId);
                }
            }
        } catch (Exception e) {
            log.error("Error al ingestar datos climáticos: {}", e.getMessage(), e);
        }
    }
}
