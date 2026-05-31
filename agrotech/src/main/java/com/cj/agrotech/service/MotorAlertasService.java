package com.cj.agrotech.service;

import com.cj.agrotech.domain.document.Telemetria;
import com.cj.agrotech.dto.ConfiguracionAlertaRequest;
import com.cj.agrotech.domain.entity.ConfiguracionAlerta;
import com.cj.agrotech.domain.entity.Dispositivo;
import com.cj.agrotech.domain.entity.HistorialAlerta;
import com.cj.agrotech.domain.enums.CondicionAlerta;
import com.cj.agrotech.domain.enums.PrioridadAlerta;
import com.cj.agrotech.domain.enums.VariableSensor;
import com.cj.agrotech.repository.ConfiguracionAlertaRepository;
import com.cj.agrotech.repository.DispositivoRepository;
import com.cj.agrotech.repository.HistorialAlertaRepository;
import com.cj.agrotech.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MotorAlertasService {

    private final ConfiguracionAlertaRepository configuracionAlertaRepository;
    private final HistorialAlertaRepository historialAlertaRepository;
    private final DispositivoRepository dispositivoRepository;
    private final LoteRepository loteRepository;

    // Gestión de Reglas
    public ConfiguracionAlerta guardarRegla(ConfiguracionAlerta regla) {
        return configuracionAlertaRepository.save(regla);
    }

    public ConfiguracionAlerta actualizarRegla(UUID id, ConfiguracionAlerta reglaActualizada) {
        ConfiguracionAlerta existente = configuracionAlertaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La configuración de alerta no existe."));

        existente.setVariable(reglaActualizada.getVariable());
        existente.setCondicion(reglaActualizada.getCondicion());
        existente.setUmbral(reglaActualizada.getUmbral());
        existente.setUmbralMin(reglaActualizada.getUmbralMin());
        existente.setUmbralMax(reglaActualizada.getUmbralMax());
        existente.setPrioridad(reglaActualizada.getPrioridad());
        existente.setMensaje(reglaActualizada.getMensaje());
        existente.setDispositivo(reglaActualizada.getDispositivo());
        existente.setLote(reglaActualizada.getLote());

        return configuracionAlertaRepository.save(existente);
    }

    public void eliminarRegla(UUID id) {
        configuracionAlertaRepository.deleteById(id);
    }

    public List<ConfiguracionAlerta> obtenerReglasPorDispositivo(UUID dispositivoId) {
        return configuracionAlertaRepository.findByDispositivoId(dispositivoId);
    }

    public List<ConfiguracionAlerta> obtenerReglasPorLote(UUID loteId) {
        return configuracionAlertaRepository.findByLoteId(loteId);
    }

    public ConfiguracionAlerta buildFromRequest(ConfiguracionAlertaRequest request) {
        ConfiguracionAlerta regla = new ConfiguracionAlerta();
        if (request.tipo() != null) {
            regla.setVariable(mapTipoAVariable(request.tipo()));
        }
        regla.setUmbralMin(request.umbralMin());
        regla.setUmbralMax(request.umbralMax());
        regla.setMensaje(request.mensaje());
        regla.setPrioridad(PrioridadAlerta.valueOf(
                request.prioridad() != null ? request.prioridad() : PrioridadAlerta.MEDIA.name()));

        if (request.dispositivoId() != null) {
            dispositivoRepository.findById(request.dispositivoId()).ifPresent(regla::setDispositivo);
        }
        if (request.loteId() != null) {
            loteRepository.findById(request.loteId()).ifPresent(regla::setLote);
        }

        return regla;
    }

    private VariableSensor mapTipoAVariable(String tipo) {
        return switch (tipo) {
            case "TEMPERATURA" -> VariableSensor.TEMP_AIRE;
            case "HUMEDAD" -> VariableSensor.HUM_AIRE;
            case "LUMINOSIDAD" -> VariableSensor.LUX;
            default -> VariableSensor.TEMP_AIRE;
        };
    }

    // Evaluación en tiempo real con Anti-Spam (1 hora cooldown)
    @Transactional
    public void evaluarLectura(Telemetria telemetria, Dispositivo dispositivo, UUID loteId) {
        List<ConfiguracionAlerta> reglas = new ArrayList<>();

        if (dispositivo != null) {
            reglas.addAll(configuracionAlertaRepository.findByDispositivoId(dispositivo.getId()));
        }
        if (loteId != null) {
            configuracionAlertaRepository.findByLoteId(loteId).forEach(regla -> {
                if (regla.getId() == null || reglas.stream().noneMatch(r -> r.getId().equals(regla.getId()))) {
                    reglas.add(regla);
                }
            });
        }

        for (ConfiguracionAlerta regla : reglas) {
            Float valor = obtenerValorVariable(telemetria, regla.getVariable().name());
            if (valor == null) continue;

            boolean cumpleCondicion = evaluarCondicion(regla, valor);
            if (!cumpleCondicion) {
                continue;
            }

            LocalDateTime unaHoraAtras = LocalDateTime.now().minusHours(1);
            boolean yaAlertado = historialAlertaRepository.findAll().stream()
                    .anyMatch(a ->
                            targetMatches(a, dispositivo, loteId) &&
                                    a.getMensaje().contains(regla.getVariable().name()) &&
                                    a.getFecha().isAfter(unaHoraAtras)
                    );

            if (!yaAlertado) {
                String mensaje = buildMensajeAlerta(regla, valor);
                HistorialAlerta alerta = HistorialAlerta.builder()
                        .mensaje(mensaje)
                        .fecha(LocalDateTime.now())
                        .prioridad(regla.getPrioridad())
                        .leida(false)
                        .dispositivo(dispositivo)
                        .lote(dispositivo != null ? dispositivo.getLote() : (loteId != null ? loteRepository.findById(loteId).orElse(null) : null))
                        .build();
                historialAlertaRepository.save(alerta);
                log.warn("Alerta registrada: {} | Prioridad {}", mensaje, regla.getPrioridad());
            }
        }
    }

    private boolean targetMatches(HistorialAlerta alerta, Dispositivo dispositivo, UUID loteId) {
        if (dispositivo != null && alerta.getDispositivo() != null) {
            return alerta.getDispositivo().getId().equals(dispositivo.getId());
        }
        if (loteId != null && alerta.getLote() != null) {
            return alerta.getLote().getId().equals(loteId);
        }
        return false;
    }

    private boolean evaluarCondicion(ConfiguracionAlerta regla, Float valor) {
        if (regla.getUmbralMin() != null && valor < regla.getUmbralMin()) {
            return true;
        }
        if (regla.getUmbralMax() != null && valor > regla.getUmbralMax()) {
            return true;
        }
        if (regla.getCondicion() != null && regla.getUmbral() != null) {
            return switch (regla.getCondicion()) {
                case MAYOR_QUE -> valor > regla.getUmbral();
                case MENOR_QUE -> valor < regla.getUmbral();
                case IGUAL_A -> Math.abs(valor - regla.getUmbral()) < 0.01;
            };
        }
        return false;
    }

    private String buildMensajeAlerta(ConfiguracionAlerta regla, Float valor) {
        if (regla.getMensaje() != null && !regla.getMensaje().isBlank()) {
            return regla.getMensaje();
        }

        String descripcion = regla.getVariable().getDescripcion();
        if (regla.getUmbralMin() != null && valor < regla.getUmbralMin()) {
            return String.format("Alerta: %s está por debajo de %.2f", descripcion, regla.getUmbralMin());
        }
        if (regla.getUmbralMax() != null && valor > regla.getUmbralMax()) {
            return String.format("Alerta: %s está por encima de %.2f", descripcion, regla.getUmbralMax());
        }
        if (regla.getCondicion() != null && regla.getUmbral() != null) {
            return String.format("Alerta: %s = %.2f %s %.2f",
                    descripcion, valor,
                    regla.getCondicion() == CondicionAlerta.MAYOR_QUE ? ">" :
                            regla.getCondicion() == CondicionAlerta.MENOR_QUE ? "<" : "=",
                    regla.getUmbral());
        }
        return String.format("Alerta: %s fuera de rango (valor actual: %.2f)", descripcion, valor);
    }

    private Float obtenerValorVariable(Telemetria t, String variable) {
        return switch (variable) {
            case "TEMP_AIRE" -> t.getLecturas().getAmbiente().getTempAire();
            case "HUM_AIRE" -> t.getLecturas().getAmbiente().getHumAire();
            case "PRESION" -> t.getLecturas().getAmbiente().getPresion();
            case "LUX" -> t.getLecturas().getAmbiente().getLux();
            case "HUM_SUELO" -> t.getLecturas().getSuelo().getHumSuelo();
            case "TEMP_SUELO" -> t.getLecturas().getSuelo().getTempSuelo();
            case "PRECIPITACION" -> t.getLecturas().getClima().getPrecipitacion();
            case "VIENTO" -> t.getLecturas().getClima().getViento();
            default -> null;
        };
    }
}
