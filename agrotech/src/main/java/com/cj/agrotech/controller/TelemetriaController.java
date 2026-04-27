package com.cj.agrotech.controller;

import com.cj.agrotech.domain.document.Telemetria;
import com.cj.agrotech.dto.TelemetriaCapturaDTO;
import com.cj.agrotech.service.TelemetriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/telemetria")
@RequiredArgsConstructor
public class TelemetriaController {
    private final TelemetriaService telemetriaService;

    // Este es el endpoint que consumirá el ESP32 físico cuando lo conectes
    @PostMapping("/captura")
    @ResponseStatus(HttpStatus.CREATED)
    public void recibirDatosSensor(@RequestBody TelemetriaCapturaDTO dto) {
        telemetriaService.registrarCaptura(dto);
    }

    @GetMapping("/dispositivo/{dispositivoId}")
    public List<Telemetria> listarPorDispositivo(@PathVariable UUID dispositivoId) {
        return telemetriaService.listarPorDispositivo(dispositivoId);
    }
}
