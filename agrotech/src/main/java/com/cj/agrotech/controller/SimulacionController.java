package com.cj.agrotech.controller;

import com.cj.agrotech.domain.document.Telemetria;
import com.cj.agrotech.dto.simulacion.SimulacionRequestDTO;
import com.cj.agrotech.service.SimulacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulacion")
@RequiredArgsConstructor
public class SimulacionController {

    private final SimulacionService simulacionService;

    @PostMapping("/ejecutar")
    public ResponseEntity<Telemetria> ejecutarSimulacion(@Valid @RequestBody SimulacionRequestDTO request) {
        Telemetria resultado = simulacionService.ejecutarSimulacionCompleta(
                request.dispositivoId(),
                request.loteId()
        );
        return ResponseEntity.ok(resultado);
    }
}
