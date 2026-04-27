package com.cj.agrotech.service;

import com.cj.agrotech.domain.document.Telemetria;
import com.cj.agrotech.domain.entity.ConfiguracionAlerta;
import com.cj.agrotech.domain.entity.Dispositivo;
import com.cj.agrotech.domain.entity.HistorialAlerta;
import com.cj.agrotech.domain.enums.CondicionAlerta;
import com.cj.agrotech.domain.enums.PrioridadAlerta;
import com.cj.agrotech.repository.ConfiguracionAlertaRepository;
import com.cj.agrotech.repository.HistorialAlertaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MotorAlertasService {

    private final ConfiguracionAlertaRepository configuracionAlertaRepository;
    private final HistorialAlertaRepository historialAlertaRepository;

    // Gestión de Reglas
    public ConfiguracionAlerta guardarRegla(ConfiguracionAlerta regla) {
        return configuracionAlertaRepository.save(regla);
    }

    public List<ConfiguracionAlerta> obtenerReglasPorDispositivo(UUID dispositivoId) {
        return configuracionAlertaRepository.findByDispositivoId(dispositivoId);
    }


    // Evaluación en tiempo real con Anti-Spam (1 hora cooldown)
    @Transactional
    public void evaluarLectura(Telemetria telemetria, Dispositivo dispositivo, UUID loteId) {
        List<ConfiguracionAlerta> reglas = configuracionAlertaRepository.findByDispositivoId(dispositivo.getId());

        for (ConfiguracionAlerta regla : reglas) {
            Float valor = obtenerValorVariable(telemetria, regla.getVariable().name());
            if (valor == null) continue;

            boolean cumpleCondicion = evaluarCondicion(regla.getCondicion(), valor, regla.getUmbral());
            if (cumpleCondicion) {
                // Verificar Anti-Spam: no alertar si ya hay una alerta similar en la última hora
                LocalDateTime unaHoraAtras = LocalDateTime.now().minusHours(1);
                boolean yaAlertado = historialAlertaRepository.findAll().stream()
                        .anyMatch(a -> a.getDispositivo().getId().equals(dispositivo.getId()) &&
                                a.getMensaje().contains(regla.getVariable().name()) &&
                                a.getFecha().isAfter(unaHoraAtras));

                if (!yaAlertado) {
                    String mensaje = String.format("Alerta: %s = %.2f %s %.2f",
                            regla.getVariable().getDescripcion(), valor,
                            regla.getCondicion() == CondicionAlerta.MAYOR_QUE ? ">" :
                            regla.getCondicion() == CondicionAlerta.MENOR_QUE ? "<" : "=",
                            regla.getUmbral());

                    HistorialAlerta alerta = HistorialAlerta.builder()
                            .mensaje(mensaje)
                            .fecha(LocalDateTime.now())
                            .prioridad(regla.getPrioridad())
                            .leida(false)
                            .dispositivo(dispositivo)
                            .build();
                    historialAlertaRepository.save(alerta);
                    log.warn("Alerta registrada: {} | Prioridad {}", mensaje, regla.getPrioridad());
                }
            }
        }
    }

    private boolean evaluarCondicion(CondicionAlerta condicion, Float valor, Double umbral) {
        return switch (condicion) {
            case MAYOR_QUE -> valor > umbral;
            case MENOR_QUE -> valor < umbral;
            case IGUAL_A -> Math.abs(valor - umbral) < 0.01; // Tolerancia para float
        };
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
