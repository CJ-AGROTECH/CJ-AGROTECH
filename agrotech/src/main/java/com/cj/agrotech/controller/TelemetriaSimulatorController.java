package com.cj.agrotech.controller;

import com.cj.agrotech.dto.TelemetriaCapturaDTO;
import com.cj.agrotech.service.TelemetriaService;
import com.cj.agrotech.repository.DispositivoRepository;
import com.cj.agrotech.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/telemetria")
@RequiredArgsConstructor
@Slf4j
public class TelemetriaSimulatorController {

    private final TelemetriaService telemetriaService;
    private final DispositivoRepository dispositivoRepository;
    private final LoteRepository loteRepository;

    @PostMapping("/simular")
    @ResponseStatus(HttpStatus.CREATED)
    public void simularUnaLectura() {
        // busca un dispositivo demo
        var dOpt = dispositivoRepository.findByMacAddress("DE:MO:00:00:00:01");
        if (dOpt.isEmpty()) {
            log.warn("No demo dispositivo found to simulate telemetry");
            return;
        }

        var dispositivo = dOpt.get();
        var lote = dispositivo.getLote();

        TelemetriaCapturaDTO.LecturasDTO lecturas = new TelemetriaCapturaDTO.LecturasDTO(
                new TelemetriaCapturaDTO.AmbienteDTO(25.0f, 60.0f, 1013.0f, 300.0f),
                new TelemetriaCapturaDTO.SueloDTO(30.0f, 20.0f),
                new TelemetriaCapturaDTO.ClimaDTO(0.0f, 3.5f)
        );

        TelemetriaCapturaDTO.DiagnosticoDTO diag = new TelemetriaCapturaDTO.DiagnosticoDTO(95, -60);

        TelemetriaCapturaDTO dto = new TelemetriaCapturaDTO(dispositivo.getId(), lote.getId(), lecturas, diag);

        telemetriaService.registrarCaptura(dto);
        log.info("Simulated telemetry created for dispositivo {} lote {}", dispositivo.getId(), lote.getId());
    }
}
