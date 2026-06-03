package com.cj.agrotech.service;

import com.cj.agrotech.config.UserDetailsImpl;
import com.cj.agrotech.domain.document.Telemetria;
import com.cj.agrotech.dto.ConfiguracionAlertaRequest;
import com.cj.agrotech.domain.entity.ConfiguracionAlerta;
import com.cj.agrotech.domain.entity.Dispositivo;
import com.cj.agrotech.domain.entity.HistorialAlerta;
import com.cj.agrotech.domain.entity.Lote;
import com.cj.agrotech.domain.enums.CondicionAlerta;
import com.cj.agrotech.domain.enums.PrioridadAlerta;
import com.cj.agrotech.domain.enums.VariableSensor;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.ConfiguracionAlertaRepository;
import com.cj.agrotech.repository.DispositivoRepository;
import com.cj.agrotech.repository.HistorialAlertaRepository;
import com.cj.agrotech.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MotorAlertasService {

    private static final int COOLDOWN_MINUTOS = 15;

    private final ConfiguracionAlertaRepository configuracionAlertaRepository;
    private final HistorialAlertaRepository historialAlertaRepository;
    private final DispositivoRepository dispositivoRepository;
    private final LoteRepository loteRepository;
    private final AlertStreamingService alertStreamingService;

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
        regla.setMensaje(request.mensaje());
        regla.setPrioridad(PrioridadAlerta.valueOf(
                request.prioridad() != null ? request.prioridad() : PrioridadAlerta.MEDIA.name()));

        boolean modoComparacion = request.condicion() != null && !request.condicion().isBlank()
                && request.umbral() != null;

        if (modoComparacion) {
            regla.setCondicion(CondicionAlerta.valueOf(request.condicion()));
            regla.setUmbral(request.umbral());
            regla.setUmbralMin(null);
            regla.setUmbralMax(null);
        } else {
            regla.setCondicion(null);
            regla.setUmbral(null);
            regla.setUmbralMin(request.umbralMin());
            regla.setUmbralMax(request.umbralMax());
        }

        if (request.dispositivoId() != null) {
            Dispositivo dispositivo = dispositivoRepository.findById(request.dispositivoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado."));
            validarPropietarioDispositivo(dispositivo);
            regla.setDispositivo(dispositivo);
            regla.setLote(null);
        }
        if (request.loteId() != null) {
            Lote lote = loteRepository.findById(request.loteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado."));
            validarPropietarioLote(lote);
            regla.setLote(lote);
            regla.setDispositivo(null);
        }

        return regla;
    }

    /**
     * Evalúa reglas tras cada lectura guardada (sensor físico u Open-Meteo por lote/dispositivo).
     */
    @Transactional
    public void evaluarLectura(Telemetria telemetria, Dispositivo dispositivo, UUID loteId) {
        if (telemetria == null || telemetria.getLecturas() == null) {
            return;
        }

        if (dispositivo == null && telemetria.getDispositivoId() != null) {
            dispositivo = dispositivoRepository.findById(telemetria.getDispositivoId()).orElse(null);
        }

        UUID effectiveLoteId = loteId != null ? loteId : telemetria.getLoteId();
        if (effectiveLoteId == null && dispositivo != null && dispositivo.getLote() != null) {
            effectiveLoteId = dispositivo.getLote().getId();
        }

        List<ConfiguracionAlerta> reglas = cargarReglas(dispositivo, effectiveLoteId);
        log.debug("Evaluando {} reglas (dispositivo={}, lote={})",
                reglas.size(),
                dispositivo != null ? dispositivo.getId() : telemetria.getDispositivoId(),
                effectiveLoteId);

        for (ConfiguracionAlerta regla : reglas) {
            if (!aplicaReglaALectura(regla, dispositivo, telemetria, effectiveLoteId)) {
                continue;
            }
            if (regla.getVariable() == null) {
                continue;
            }

            Float valor = obtenerValorVariable(telemetria, regla.getVariable().name());
            if (valor == null) {
                log.debug("Sin dato para {} en regla {}", regla.getVariable(), regla.getId());
                continue;
            }

            if (!reglaDispara(regla, valor)) {
                continue;
            }

            String mensaje = buildMensajeAlerta(regla, valor);
            registrarSiNoExisteReciente(regla, mensaje);
        }
    }

    /**
     * Reglas de lote: aplican a lecturas climáticas del lote (con o sin dispositivo en el documento).
     * Reglas de dispositivo: solo si la lectura pertenece a ese dispositivo.
     */
    private boolean aplicaReglaALectura(
            ConfiguracionAlerta regla,
            Dispositivo dispositivo,
            Telemetria telemetria,
            UUID loteId) {

        if (regla.getDispositivo() != null) {
            UUID lecturaDispositivoId = dispositivo != null
                    ? dispositivo.getId()
                    : telemetria.getDispositivoId();
            return lecturaDispositivoId != null
                    && regla.getDispositivo().getId().equals(lecturaDispositivoId);
        }

        if (regla.getLote() != null) {
            return loteId != null && regla.getLote().getId().equals(loteId);
        }

        return false;
    }

    private List<ConfiguracionAlerta> cargarReglas(Dispositivo dispositivo, UUID loteId) {
        Set<ConfiguracionAlerta> reglas = new LinkedHashSet<>();
        if (dispositivo != null) {
            reglas.addAll(configuracionAlertaRepository.findByDispositivoId(dispositivo.getId()));
        }
        if (loteId != null) {
            reglas.addAll(configuracionAlertaRepository.findByLoteId(loteId));
        }
        return new ArrayList<>(reglas);
    }

    private boolean reglaDispara(ConfiguracionAlerta regla, float valor) {
        if (esModoComparacion(regla)) {
            return cumpleComparacion(regla, valor);
        }
        if (esModoRango(regla)) {
            return fueraDeRangoPermitido(regla, valor);
        }
        return false;
    }

    private boolean esModoComparacion(ConfiguracionAlerta regla) {
        return regla.getCondicion() != null && regla.getUmbral() != null;
    }

    private boolean esModoRango(ConfiguracionAlerta regla) {
        return regla.getUmbralMin() != null || regla.getUmbralMax() != null;
    }

    private boolean fueraDeRangoPermitido(ConfiguracionAlerta regla, float valor) {
        if (regla.getUmbralMin() != null && valor < regla.getUmbralMin()) {
            return true;
        }
        if (regla.getUmbralMax() != null && valor > regla.getUmbralMax()) {
            return true;
        }
        return false;
    }

    private boolean cumpleComparacion(ConfiguracionAlerta regla, float valor) {
        double umbral = regla.getUmbral();
        return switch (regla.getCondicion()) {
            case MAYOR_QUE -> valor > umbral;
            case MENOR_QUE -> valor < umbral;
            case IGUAL_A -> Math.abs(valor - umbral) < 0.01;
        };
    }

    private void registrarSiNoExisteReciente(ConfiguracionAlerta regla, String mensaje) {
        LocalDateTime desde = LocalDateTime.now().minusMinutes(COOLDOWN_MINUTOS);
        UUID dispositivoId = regla.getDispositivo() != null ? regla.getDispositivo().getId() : null;
        UUID loteId = regla.getLote() != null ? regla.getLote().getId() : null;

        if (historialAlertaRepository.existsAlertaReciente(mensaje, desde, dispositivoId, loteId)) {
            log.debug("Alerta omitida por cooldown: {}", mensaje);
            return;
        }

        HistorialAlerta alerta = HistorialAlerta.builder()
                .mensaje(mensaje)
                .fecha(LocalDateTime.now())
                .prioridad(regla.getPrioridad())
                .leida(false)
                .dispositivo(regla.getDispositivo())
                .lote(regla.getLote())
                .build();

        HistorialAlerta guardada = historialAlertaRepository.save(alerta);
        UUID propietarioId = resolverPropietarioId(dispositivoId, loteId);
        if (propietarioId != null) {
            alertStreamingService.publish(propietarioId, guardada);
        }
        log.warn("Alerta registrada: {} | prioridad {}", mensaje, regla.getPrioridad());
    }

    private UUID resolverPropietarioId(UUID dispositivoId, UUID loteId) {
        if (dispositivoId != null) {
            return dispositivoRepository.findPropietarioUsuarioId(dispositivoId).orElse(null);
        }
        if (loteId != null) {
            return loteRepository.findPropietarioUsuarioId(loteId).orElse(null);
        }
        return null;
    }

    private String buildMensajeAlerta(ConfiguracionAlerta regla, float valor) {
        String ubicacion = buildUbicacionMensaje(regla);
        String unidad = getUnidad(regla.getVariable());

        if (regla.getMensaje() != null && !regla.getMensaje().isBlank()) {
            return regla.getMensaje().trim() + (ubicacion.isBlank() ? "" : " " + ubicacion);
        }

        String descripcion = regla.getVariable().getDescripcion();
        if (esModoRango(regla)) {
            if (regla.getUmbralMin() != null && valor < regla.getUmbralMin()) {
                return String.format("⚠️ %s %.2f%s por debajo del mínimo permitido (%.2f%s) %s",
                        descripcion, valor, unidad, regla.getUmbralMin(), unidad, ubicacion);
            }
            if (regla.getUmbralMax() != null && valor > regla.getUmbralMax()) {
                return String.format("⚠️ %s %.2f%s por encima del máximo permitido (%.2f%s) %s",
                        descripcion, valor, unidad, regla.getUmbralMax(), unidad, ubicacion);
            }
        }
        if (esModoComparacion(regla)) {
            String operador = switch (regla.getCondicion()) {
                case MAYOR_QUE -> "supera";
                case MENOR_QUE -> "está por debajo de";
                case IGUAL_A -> "coincide con";
            };
            return String.format("⚠️ %s %.2f%s %s el umbral de %.2f%s %s",
                    descripcion, valor, unidad, operador, regla.getUmbral(), unidad, ubicacion);
        }
        return String.format("⚠️ %s fuera de condición (valor actual: %.2f%s) %s", descripcion, valor, unidad, ubicacion);
    }

    private String buildUbicacionMensaje(ConfiguracionAlerta regla) {
        if (regla.getDispositivo() != null) {
            String dispositivo = regla.getDispositivo().getNombre();
            String lote = regla.getDispositivo().getLote() != null ? regla.getDispositivo().getLote().getNombre() : null;
            if (lote != null) {
                return String.format("en dispositivo %s (Lote: %s)", dispositivo, lote);
            }
            return String.format("en dispositivo %s", dispositivo);
        }
        if (regla.getLote() != null) {
            return String.format("en lote %s (clima zona)", regla.getLote().getNombre());
        }
        return "";
    }

    private String getUnidad(VariableSensor variable) {
        return switch (variable) {
            case TEMP_AIRE, TEMP_SUELO -> "°C";
            case HUM_AIRE, HUM_SUELO -> "%";
            case LUX -> " lux";
            case PRESION -> " hPa";
            case PRECIPITACION -> " mm";
            case VIENTO -> " m/s";
            default -> "";
        };
    }

    private Float obtenerValorVariable(Telemetria t, String variable) {
        if (t.getLecturas() == null) {
            return null;
        }
        return switch (variable) {
            case "TEMP_AIRE" -> t.getLecturas().getAmbiente() != null ? t.getLecturas().getAmbiente().getTempAire() : null;
            case "HUM_AIRE" -> t.getLecturas().getAmbiente() != null ? t.getLecturas().getAmbiente().getHumAire() : null;
            case "PRESION" -> t.getLecturas().getAmbiente() != null ? t.getLecturas().getAmbiente().getPresion() : null;
            case "LUX" -> t.getLecturas().getAmbiente() != null ? t.getLecturas().getAmbiente().getLux() : null;
            case "HUM_SUELO" -> t.getLecturas().getSuelo() != null ? t.getLecturas().getSuelo().getHumSuelo() : null;
            case "TEMP_SUELO" -> t.getLecturas().getSuelo() != null ? t.getLecturas().getSuelo().getTempSuelo() : null;
            case "PRECIPITACION" -> t.getLecturas().getClima() != null ? t.getLecturas().getClima().getPrecipitacion() : null;
            case "VIENTO" -> t.getLecturas().getClima() != null ? t.getLecturas().getClima().getViento() : null;
            default -> null;
        };
    }

    private VariableSensor mapTipoAVariable(String tipo) {
        return switch (tipo) {
            case "TEMPERATURA" -> VariableSensor.TEMP_AIRE;
            case "HUMEDAD" -> VariableSensor.HUM_AIRE;
            case "LUMINOSIDAD" -> VariableSensor.LUX;
            case "HUM_SUELO", "HUMEDAD_SUELO" -> VariableSensor.HUM_SUELO;
            case "TEMP_SUELO" -> VariableSensor.TEMP_SUELO;
            default -> VariableSensor.TEMP_AIRE;
        };
    }

    private void validarPropietarioDispositivo(Dispositivo dispositivo) {
        UUID actual = obtenerUsuarioAutenticadoId();
        UUID propietario = dispositivoRepository.findPropietarioUsuarioId(dispositivo.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado."));
        if (!actual.equals(propietario)) {
            throw new ResourceNotFoundException("Dispositivo no encontrado.");
        }
    }

    private void validarPropietarioLote(Lote lote) {
        UUID actual = obtenerUsuarioAutenticadoId();
        UUID propietario = loteRepository.findPropietarioUsuarioId(lote.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado."));
        if (!actual.equals(propietario)) {
            throw new ResourceNotFoundException("Lote no encontrado.");
        }
    }

    private UUID obtenerUsuarioAutenticadoId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Usuario no autenticado.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }
        throw new ResourceNotFoundException("No se pudo identificar el usuario autenticado.");
    }
}
