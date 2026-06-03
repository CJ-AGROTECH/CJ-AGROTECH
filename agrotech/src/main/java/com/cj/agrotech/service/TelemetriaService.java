package com.cj.agrotech.service;

import com.cj.agrotech.domain.document.Telemetria;
import com.cj.agrotech.domain.entity.Dispositivo;
import com.cj.agrotech.dto.TelemetriaCapturaDTO;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.DispositivoRepository;
import com.cj.agrotech.repository.TelemetriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TelemetriaService {

    private final TelemetriaRepository telemetriaRepository;
    private final DispositivoRepository dispositivoRepository;
    private final MotorAlertasService motorAlertasService;

    @Transactional(readOnly = true)
    public List<Telemetria> listarPorDispositivo(UUID dispositivoId) {
        return telemetriaRepository.findByDispositivoIdOrderByTimestampDesc(dispositivoId);
    }

    /**
     * Captura desde sensores físicos (ESP32).
     */
    @Transactional
    public void registrarCaptura(TelemetriaCapturaDTO dto) {
        Dispositivo dispositivo = dispositivoRepository.findById(dto.dispositivoId())
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));

        dispositivo.setUltimaSincronizacion(LocalDateTime.now());
        dispositivoRepository.save(dispositivo);

        Telemetria telemetria = Telemetria.builder()
                .dispositivoId(dispositivo.getId())
                .loteId(dto.loteId())
                .timestamp(Instant.now())
                .lecturas(new Telemetria.Lecturas(
                        new Telemetria.Ambiente(
                                dto.lecturas().ambiente().tempAire(),
                                dto.lecturas().ambiente().humAire(),
                                dto.lecturas().ambiente().presion(),
                                dto.lecturas().ambiente().lux()),
                        new Telemetria.Suelo(
                                dto.lecturas().suelo().humSuelo(),
                                dto.lecturas().suelo().tempSuelo()),
                        new Telemetria.Clima(
                                dto.lecturas().clima().precipitacion(),
                                dto.lecturas().clima().viento())))
                .diagnostico(new Telemetria.Diagnostico(
                        dto.diagnostico().bateria(),
                        dto.diagnostico().rssiWifi()))
                .build();

        guardarYEvaluarAlertas(telemetria, dispositivo);
    }

    /**
     * Lectura climática (Open-Meteo): por lote sin sensores, por finca o asociada a dispositivo.
     */
    @Transactional
    public Telemetria guardarLecturaClimatica(Telemetria telemetria) {
        Dispositivo dispositivo = null;
        if (telemetria.getDispositivoId() != null) {
            dispositivo = dispositivoRepository.findById(telemetria.getDispositivoId()).orElse(null);
            if (dispositivo != null) {
                dispositivo.setUltimaSincronizacion(LocalDateTime.now());
                dispositivoRepository.save(dispositivo);
            }
        }
        return guardarYEvaluarAlertas(telemetria, dispositivo);
    }

    private Telemetria guardarYEvaluarAlertas(Telemetria telemetria, Dispositivo dispositivo) {
        if (telemetria.getTimestamp() == null) {
            telemetria.setTimestamp(Instant.now());
        }
        telemetriaRepository.save(telemetria);
        UUID loteId = telemetria.getLoteId();
        if (loteId == null && dispositivo != null && dispositivo.getLote() != null) {
            loteId = dispositivo.getLote().getId();
        }
        motorAlertasService.evaluarLectura(telemetria, dispositivo, loteId);
        return telemetria;
    }
}
