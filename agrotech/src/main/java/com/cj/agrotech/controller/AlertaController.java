package com.cj.agrotech.controller;

import com.cj.agrotech.dto.ConfiguracionAlertaRequest;
import com.cj.agrotech.dto.ConfiguracionAlertaResponse;
import com.cj.agrotech.dto.HistorialAlertaResponse;
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
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {
    private final MotorAlertasService motorAlertasService;
    private final HistorialAlertaService historialAlertaService;

    // Configuración de reglas
    @GetMapping("/configuracion/dispositivo/{dispositivoId}")
    public List<ConfiguracionAlertaResponse> listarReglasPorDispositivo(@PathVariable UUID dispositivoId) {
        return motorAlertasService.obtenerReglasPorDispositivo(dispositivoId).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/configuracion/lote/{loteId}")
    public List<ConfiguracionAlertaResponse> listarReglasPorLote(@PathVariable UUID loteId) {
        return motorAlertasService.obtenerReglasPorLote(loteId).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/configuracion")
    @ResponseStatus(HttpStatus.CREATED)
    public ConfiguracionAlertaResponse guardarRegla(@RequestBody ConfiguracionAlertaRequest regla) {
        return toResponse(motorAlertasService.guardarRegla(motorAlertasService.buildFromRequest(regla)));
    }

    @PutMapping("/configuracion/{id}")
    public ConfiguracionAlertaResponse actualizarRegla(@PathVariable UUID id, @RequestBody ConfiguracionAlertaRequest regla) {
        ConfiguracionAlerta configuracion = motorAlertasService.buildFromRequest(regla);
        return toResponse(motorAlertasService.actualizarRegla(id, configuracion));
    }

    @DeleteMapping("/configuracion/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarRegla(@PathVariable UUID id) {
        motorAlertasService.eliminarRegla(id);
    }

    // Historial de alertas (Dashboard)
    @GetMapping("/historial/activas")
    public List<HistorialAlertaResponse> listarAlertasActivas() {
        return historialAlertaService.obtenerAlertasNoVistas().stream()
                .map(this::toResponse)
                .toList();
    }

    @PatchMapping("/historial/{id}/vista")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarComoVista(@PathVariable UUID id) {
        historialAlertaService.marcarAlertaComoVista(id);
    }

    private ConfiguracionAlertaResponse toResponse(ConfiguracionAlerta configuracion) {
        return new ConfiguracionAlertaResponse(
                configuracion.getId(),
                configuracion.getVariable() != null ? configuracion.getVariable().name() : null,
                configuracion.getUmbralMin(),
                configuracion.getUmbralMax(),
                configuracion.getMensaje(),
                configuracion.getPrioridad() != null ? configuracion.getPrioridad().name() : null,
                configuracion.getDispositivo() != null ? configuracion.getDispositivo().getId() : null,
                configuracion.getDispositivo() != null ? configuracion.getDispositivo().getNombre() : null,
                configuracion.getLote() != null ? configuracion.getLote().getId() : null,
                configuracion.getLote() != null ? configuracion.getLote().getNombre() : null
        );
    }

    private HistorialAlertaResponse toResponse(HistorialAlerta alerta) {
        return new HistorialAlertaResponse(
                alerta.getId(),
                alerta.getMensaje(),
                alerta.getFecha(),
                alerta.getPrioridad() != null ? alerta.getPrioridad().name() : null,
                alerta.getLeida(),
                alerta.getDispositivo() != null ? alerta.getDispositivo().getId() : null,
                alerta.getDispositivo() != null ? alerta.getDispositivo().getNombre() : null,
                alerta.getLote() != null ? alerta.getLote().getId() : null,
                alerta.getLote() != null ? alerta.getLote().getNombre() : null
        );
    }
}
