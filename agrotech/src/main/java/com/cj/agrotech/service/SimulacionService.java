package com.cj.agrotech.service;

import com.cj.agrotech.domain.document.Telemetria;
import com.cj.agrotech.domain.entity.ConfiguracionAlerta;
import com.cj.agrotech.domain.entity.Dispositivo;
import com.cj.agrotech.domain.enums.CondicionAlerta;
import com.cj.agrotech.repository.ConfiguracionAlertaRepository;
import com.cj.agrotech.repository.DispositivoRepository;
import com.cj.agrotech.repository.HistorialAlertaRepository;
import com.cj.agrotech.repository.TelemetriaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulacionService {

    private final RestTemplate restTemplate;
    private final TelemetriaRepository telemetriaRepository;
    private final DispositivoRepository dispositivoRepository;
    private final ConfiguracionAlertaRepository configuracionAlertaRepository;
    private final HistorialAlertaRepository historialAlertaRepository;
    private final MotorAlertasService motorAlertasService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Telemetria ejecutarSimulacionCompleta(UUID dispositivoId, UUID loteId) {
        log.info("Iniciando simulación para el dispositivo: {}", dispositivoId);

        // 1. Validar que el dispositivo exista en Postgres
        Dispositivo dispositivo = dispositivoRepository.findById(dispositivoId)
                .orElseThrow(() -> new RuntimeException("Dispositivo no encontrado en Postgres"));

        // 2. Consumir API de Open-Meteo (Variables completas)
        String url = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=6.15&longitude=-75.37&" +
                "current=temperature_2m,relative_humidity_2m,surface_pressure,precipitation,wind_speed_10m,soil_temperature_0_to_7cm,soil_moisture_0_to_7cm";
        
        JsonNode response;
        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            response = objectMapper.readTree(jsonResponse);
        } catch (Exception e) {
            log.error("Error al obtener datos de Open-Meteo", e);
            throw new RuntimeException("Error al obtener datos de Open-Meteo: " + e.getMessage());
        }

        if (response == null || !response.has("current")) {
            throw new RuntimeException("Error al obtener datos de Open-Meteo");
        }

        JsonNode current = response.get("current");

        // 3. Mapear al Documento MongoDB
        Telemetria telemetria = Telemetria.builder()
                .dispositivoId(dispositivoId)
                .loteId(loteId)
                .timestamp(Instant.now())
                .lecturas(new Telemetria.Lecturas(
                        new Telemetria.Ambiente(
                                current.get("temperature_2m").floatValue(),
                                current.get("relative_humidity_2m").floatValue(),
                                current.get("surface_pressure").floatValue(),
                                (float) (Math.random() * 1000) // Lux simulado
                        ),
                        new Telemetria.Suelo(
                                current.get("soil_moisture_0_to_7cm").floatValue(),
                                current.get("soil_temperature_0_to_7cm").floatValue()
                        ),
                        new Telemetria.Clima(
                                current.get("precipitation").floatValue(),
                                current.get("wind_speed_10m").floatValue()
                        )
                ))
                .diagnostico(new Telemetria.Diagnostico(100, -50)) // Batería al 100%, Señal excelente
                .build();

        // 4. Persistir en MongoDB (Telemetría cruda)
        telemetria = telemetriaRepository.save(telemetria);
        log.info("Datos de telemetría guardados en MongoDB. ID: {}", telemetria.getId());

        // 5. Evaluar Motor de Alertas usando motorAlertasService
        motorAlertasService.evaluarLectura(telemetria, dispositivo, loteId);

        return telemetria;
    }
}
