package com.cj.agrotech.controller;

import com.cj.agrotech.domain.entity.ConfiguracionAlerta;
import com.cj.agrotech.domain.entity.HistorialAlerta;
import com.cj.agrotech.service.HistorialAlertaService;
import com.cj.agrotech.service.MotorAlertasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alertas")
@RequiredArgsConstructor
public class AlertaController {
    private final MotorAlertasService motorAlertasService;
    private final HistorialAlertaService historialAlertaService;

    // Configuración de reglas
    @GetMapping("/configuracion/lote/{loteId}")
    public List<ConfiguracionAlerta> listarReglas(@PathVariable UUID loteId) { return motorAlertasService.obtenerReglasPorLote(loteId); }

    @PostMapping("/configuracion")
    @ResponseStatus(HttpStatus.CREATED)
    public ConfiguracionAlerta guardarRegla(@RequestBody ConfiguracionAlerta regla) { return motorAlertasService.guardarRegla(regla); }

    // Historial de alertas (Dashboard)
    @GetMapping("/historial/activas")
    public List<HistorialAlerta> listarAlertasActivas() { return historialAlertaService.obtenerAlertasNoVistas(); }

    @PatchMapping("/historial/{id}/vista")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarComoVista(@PathVariable UUID id) { historialAlertaService.marcarAlertaComoVista(id); }
}
