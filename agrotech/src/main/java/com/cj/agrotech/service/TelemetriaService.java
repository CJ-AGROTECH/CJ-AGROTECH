package com.cj.agrotech.service;

import com.cj.agrotech.domain.document.Telemetria;
import com.cj.agrotech.domain.entity.Dispositivo;
import com.cj.agrotech.dto.TelemetriaCapturaDTO;
import com.cj.agrotech.repository.DispositivoRepository;
import com.cj.agrotech.repository.TelemetriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TelemetriaService {

    private final TelemetriaRepository telemetriaRepository;
    private final DispositivoRepository dispositivoRepository;
    private final MotorAlertasService motorAlertasService;

    @Transactional
    public void registrarCaptura(TelemetriaCapturaDTO dto) {
        Dispositivo dispositivo = dispositivoRepository.findById(dto.dispositivoId())
                .orElseThrow(() -> new RuntimeException("Dispositivo no válido"));

        dispositivo.setUltimaSincronizacion(LocalDateTime.now());
        dispositivoRepository.save(dispositivo);

        Telemetria telemetria = Telemetria.builder()
                .dispositivoId(dispositivo.getId())
                .loteId(dto.loteId())
                .timestamp(Instant.now())
                .lecturas(new Telemetria.Lecturas(
                        new Telemetria.Ambiente(dto.lecturas().ambiente().tempAire(), dto.lecturas().ambiente().humAire(), dto.lecturas().ambiente().presion(), dto.lecturas().ambiente().lux()),
                        new Telemetria.Suelo(dto.lecturas().suelo().humSuelo(), dto.lecturas().suelo().tempSuelo()),
                        new Telemetria.Clima(dto.lecturas().clima().precipitacion(), dto.lecturas().clima().viento())
                ))
                .diagnostico(new Telemetria.Diagnostico(dto.diagnostico().bateria(), dto.diagnostico().rssiWifi()))
                .build();

        telemetriaRepository.save(telemetria);

        // Dispara la evaluación de reglas inmediatamente (RF2)
        motorAlertasService.evaluarLectura(telemetria, dispositivo, dto.loteId());
    }
}
