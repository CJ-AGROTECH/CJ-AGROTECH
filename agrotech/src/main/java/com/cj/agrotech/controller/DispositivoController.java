package com.cj.agrotech.controller;

import com.cj.agrotech.domain.entity.Dispositivo;
import com.cj.agrotech.domain.entity.Mantenimiento;
import com.cj.agrotech.domain.enums.EstadoDispositivo;
import com.cj.agrotech.service.DispositivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dispositivos")
@RequiredArgsConstructor
public class DispositivoController {
    private final DispositivoService dispositivoService;

    @GetMapping("/lote/{loteId}")
    public List<Dispositivo> listarPorLote(@PathVariable UUID loteId) { return dispositivoService.listarDispositivosPorLote(loteId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Dispositivo registrarDispositivo(@RequestBody Dispositivo dispositivo) { return dispositivoService.registrarDispositivo(dispositivo); }

    @PatchMapping("/{id}/estado")
    public Dispositivo actualizarEstado(@PathVariable UUID id, @RequestParam EstadoDispositivo estado) { return dispositivoService.actualizarEstadoDispositivo(id, estado); }

    @PostMapping("/mantenimiento")
    @ResponseStatus(HttpStatus.CREATED)
    public Mantenimiento registrarMantenimiento(@RequestBody Mantenimiento mantenimiento) { return dispositivoService.registrarMantenimiento(mantenimiento); }
}