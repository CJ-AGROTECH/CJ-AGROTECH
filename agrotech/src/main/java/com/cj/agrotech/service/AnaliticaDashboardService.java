package com.cj.agrotech.service;

import com.cj.agrotech.domain.document.Telemetria;
import com.cj.agrotech.repository.TelemetriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnaliticaDashboardService {

    private final TelemetriaRepository telemetriaRepository;

    // RF5 y RF7 - Gráficos Históricos
    public List<Telemetria> obtenerHistoricoTelemetria(UUID dispositivoId) {
        return telemetriaRepository.findByDispositivoIdOrderByTimestampDesc(dispositivoId);
    }

    // RF6 - Indicadores de Eficiencia Hídrica
    public Map<String, Object> calcularEficienciaHidrica(UUID dispositivoId) {
        List<Telemetria> lecturas = obtenerHistoricoTelemetria(dispositivoId);
        if (lecturas.isEmpty()) return Map.of("error", "No hay datos");

        double precipTotal = lecturas.stream().mapToDouble(t -> t.getLecturas().getClima().getPrecipitacion()).sum();
        double humSueloPromedio = lecturas.stream().mapToDouble(t -> t.getLecturas().getSuelo().getHumSuelo()).average().orElse(0.0);

        String estado = "ÓPTIMO";
        if (precipTotal > 15 && humSueloPromedio < 30) estado = "ALERTA: Baja Retención de Suelo";
        if (precipTotal < 5 && humSueloPromedio > 70) estado = "ALERTA: Posible Encharcamiento";

        Map<String, Object> calculo = new HashMap<>();
        calculo.put("precipitacionAcumulada", precipTotal);
        calculo.put("humedadSueloPromedio", humSueloPromedio);
        calculo.put("estado", estado);
        return calculo;
    }
}
