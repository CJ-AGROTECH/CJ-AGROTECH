package com.cj.agrotech.controller;

import com.cj.agrotech.config.JwtUtils;
import com.cj.agrotech.config.UserDetailsImpl;
import com.cj.agrotech.domain.entity.ConfiguracionAlerta;
import com.cj.agrotech.domain.entity.HistorialAlerta;
import com.cj.agrotech.domain.entity.Usuario;
import com.cj.agrotech.dto.ConfiguracionAlertaRequest;
import com.cj.agrotech.dto.ConfiguracionAlertaResponse;
import com.cj.agrotech.dto.HistorialAlertaResponse;
import com.cj.agrotech.exception.BadRequestException;
import com.cj.agrotech.repository.UsuarioRepository;
import com.cj.agrotech.service.AlertStreamingService;
import com.cj.agrotech.service.HistorialAlertaService;
import com.cj.agrotech.service.MeteoService;
import com.cj.agrotech.service.MotorAlertasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Slf4j
public class AlertaController {
    private final MotorAlertasService motorAlertasService;
    private final HistorialAlertaService historialAlertaService;
    private final AlertStreamingService alertStreamingService;
    private final MeteoService meteoService;
    private final JwtUtils jwtUtils;
    private final UsuarioRepository usuarioRepository;

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
        ConfiguracionAlerta guardada = motorAlertasService.guardarRegla(motorAlertasService.buildFromRequest(regla));
        sincronizarClimaYEvaluar(guardada);
        return toResponse(guardada);
    }

    @PutMapping("/configuracion/{id}")
    public ConfiguracionAlertaResponse actualizarRegla(@PathVariable UUID id, @RequestBody ConfiguracionAlertaRequest regla) {
        ConfiguracionAlerta configuracion = motorAlertasService.buildFromRequest(regla);
        ConfiguracionAlerta actualizada = motorAlertasService.actualizarRegla(id, configuracion);
        sincronizarClimaYEvaluar(actualizada);
        return toResponse(actualizada);
    }

    @DeleteMapping("/configuracion/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarRegla(@PathVariable UUID id) {
        motorAlertasService.eliminarRegla(id);
    }

    @GetMapping("/historial/activas")
    public List<HistorialAlertaResponse> listarAlertasActivas() {
        return historialAlertaService.obtenerAlertasNoVistas().stream()
                .map(this::toHistorialResponse)
                .toList();
    }

    @PatchMapping("/historial/{id}/vista")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarComoVista(@PathVariable UUID id) {
        historialAlertaService.marcarAlertaComoVista(id);
    }

    @GetMapping("/stream")
    public SseEmitter streamAlertas(
            @RequestParam(name = "token", required = false) String token,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UUID usuarioId = resolveUsuarioId(token, userDetails);
        if (usuarioId == null) {
            throw new BadRequestException("Token inválido o sesión no autenticada para el stream de alertas.");
        }
        return alertStreamingService.register(usuarioId);
    }

    /**
     * Tras guardar una regla, trae clima real de Open-Meteo y evalúa con esos datos.
     */
    private void sincronizarClimaYEvaluar(ConfiguracionAlerta regla) {
        try {
            if (regla.getLote() != null) {
                meteoService.sincronizarClimaPorLote(regla.getLote().getId());
            } else if (regla.getDispositivo() != null && regla.getDispositivo().getLote() != null) {
                meteoService.sincronizarClimaPorLote(regla.getDispositivo().getLote().getId());
            }
        } catch (Exception ex) {
            log.warn("No se pudo sincronizar clima tras guardar regla {}: {}", regla.getId(), ex.getMessage());
        }
    }

    private UUID resolveUsuarioId(String token, UserDetailsImpl userDetails) {
        if (userDetails != null) {
            return userDetails.getId();
        }
        if (token != null && jwtUtils.validateJwtToken(token)) {
            String email = jwtUtils.getUserNameFromJwtToken(token);
            return usuarioRepository.findByEmail(email).map(Usuario::getId).orElse(null);
        }
        return null;
    }

    private ConfiguracionAlertaResponse toResponse(ConfiguracionAlerta configuracion) {
        return new ConfiguracionAlertaResponse(
                configuracion.getId(),
                configuracion.getVariable() != null ? variableToTipo(configuracion.getVariable()) : null,
                configuracion.getUmbralMin(),
                configuracion.getUmbralMax(),
                configuracion.getCondicion() != null ? configuracion.getCondicion().name() : null,
                configuracion.getUmbral(),
                configuracion.getMensaje(),
                configuracion.getPrioridad() != null ? configuracion.getPrioridad().name() : null,
                configuracion.getDispositivo() != null ? configuracion.getDispositivo().getId() : null,
                configuracion.getDispositivo() != null ? configuracion.getDispositivo().getNombre() : null,
                configuracion.getLote() != null ? configuracion.getLote().getId() : null,
                configuracion.getLote() != null ? configuracion.getLote().getNombre() : null
        );
    }

    private String variableToTipo(com.cj.agrotech.domain.enums.VariableSensor variable) {
        return switch (variable) {
            case TEMP_AIRE -> "TEMPERATURA";
            case HUM_AIRE -> "HUMEDAD";
            case LUX -> "LUMINOSIDAD";
            case HUM_SUELO -> "HUMEDAD_SUELO";
            case TEMP_SUELO -> "TEMP_SUELO";
            default -> variable.name();
        };
    }

    private HistorialAlertaResponse toHistorialResponse(HistorialAlerta alerta) {
        return alertStreamingService.mapToDto(alerta);
    }
}
