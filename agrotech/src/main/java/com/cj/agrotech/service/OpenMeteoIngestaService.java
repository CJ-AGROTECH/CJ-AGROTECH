package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.Finca;
import com.cj.agrotech.domain.entity.Lote;
import com.cj.agrotech.dto.TelemetriaCapturaDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cj.agrotech.repository.FincaRepository;
import com.cj.agrotech.repository.LoteRepository;
import com.cj.agrotech.service.MeteoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenMeteoIngestaService {

    private final RestTemplate restTemplate;
    private final TelemetriaService telemetriaService;
    private final ObjectMapper objectMapper;
    private final FincaRepository fincaRepository;
    private final LoteRepository loteRepository;
    private final MeteoService meteoService;

    // Cronjob cada 15 minutos para ingesta automática de clima de fincas y lotes
    @Scheduled(fixedRate = 900000) // 15 minutos en milisegundos
    public void ingestarDatosClimaticosAutomaticamente() {
        log.info("Iniciando ingesta automática de datos climáticos desde Open-Meteo (Fincas y Lotes)");
        
        try {
            // Primero actualizar clima de todas las fincas (esto también actualizará sus lotes)
            List<Finca> fincas = fincaRepository.findAll();
            log.info("Actualizando clima de {} fincas...", fincas.size());
            
            for (Finca finca : fincas) {
                try {
                    meteoService.sincronizarClimaPorFinca(finca.getId());
                    log.debug("Clima actualizado exitosamente para finca: {} ({})", finca.getNombre(), finca.getId());
                } catch (Exception ex) {
                    log.error("Error al ingestar datos climáticos para finca {}: {}", finca.getId(), ex.getMessage(), ex);
                }
            }
            
            // Luego actualizar clima de lotes sin finca o que necesiten actualización específica
            List<Lote> lotes = loteRepository.findAll();
            log.info("Finalizó ingesta automática de clima para {} fincas y {} lotes", fincas.size(), lotes.size());
            
        } catch (Exception ex) {
            log.error("Error crítico en ingesta automática de datos climáticos: {}", ex.getMessage(), ex);
        }
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
Float soilMoistureRaw = hourly.has("soil_moisture_0_to_7cm") ? hourly.get("soil_moisture_0_to_7cm").get(lastIndex).floatValue() : null;
                Float soilMoisture = soilMoistureRaw == null ? null : soilMoistureRaw * 100;
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
