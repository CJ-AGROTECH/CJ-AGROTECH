package com.cj.agrotech.service;

import com.cj.agrotech.domain.document.Telemetria;
import com.cj.agrotech.domain.entity.ConfiguracionAlerta;
import com.cj.agrotech.domain.entity.Dispositivo;
import com.cj.agrotech.domain.entity.HistorialAlerta;
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

    public List<ConfiguracionAlerta> obtenerReglasPorLote(UUID loteId) {
        return configuracionAlertaRepository.findByLoteId(loteId);
    }

    // Evaluación en tiempo real (RF2)
    @Transactional
    public void evaluarLectura(Telemetria telemetria, Dispositivo dispositivo, UUID loteId) {
        List<ConfiguracionAlerta> reglas = configuracionAlertaRepository.findByLoteId(loteId);

        for (ConfiguracionAlerta regla : reglas) {
            Float valor = obtenerValorVariable(telemetria, regla.getVariable().name());
            if (valor == null) continue;

            if ((regla.getMin() != null && valor < regla.getMin()) ||
                    (regla.getMax() != null && valor > regla.getMax())) {

                HistorialAlerta alerta = HistorialAlerta.builder()
                        .dispositivo(dispositivo)
                        .variable(regla.getVariable().name())
                        .valorLeido(valor)
                        .fechaHora(LocalDateTime.now())
                        .vistoPorUsuario(false)
                        .build();
                historialAlertaRepository.save(alerta);
                log.warn("Alerta registrada: Lote {} | Variable {} | Valor {}", loteId, regla.getVariable(), valor);
            }
        }
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
