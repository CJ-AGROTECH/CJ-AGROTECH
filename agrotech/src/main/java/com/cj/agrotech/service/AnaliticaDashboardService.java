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

    // Exportación CSV
    public String exportarCSV(UUID dispositivoId) {
        List<Telemetria> lecturas = obtenerHistoricoTelemetria(dispositivoId);
        StringBuilder csv = new StringBuilder("timestamp,temp_aire,hum_aire,presion,lux,hum_suelo,temp_suelo,precipitacion,viento,bateria,rssi\n");
        for (Telemetria t : lecturas) {
            csv.append(t.getTimestamp()).append(",")
                    .append(t.getLecturas().getAmbiente().getTempAire()).append(",")
                    .append(t.getLecturas().getAmbiente().getHumAire()).append(",")
                    .append(t.getLecturas().getAmbiente().getPresion()).append(",")
                    .append(t.getLecturas().getAmbiente().getLux()).append(",")
                    .append(t.getLecturas().getSuelo().getHumSuelo()).append(",")
                    .append(t.getLecturas().getSuelo().getTempSuelo()).append(",")
                    .append(t.getLecturas().getClima().getPrecipitacion()).append(",")
                    .append(t.getLecturas().getClima().getViento()).append(",")
                    .append(t.getDiagnostico().getBateria()).append(",")
                    .append(t.getDiagnostico().getRssiWifi()).append("\n");
        }
        return csv.toString();
    }

    public ClimaActualDTO obtenerUltimoClimaPorLote(UUID loteId) {
        Telemetria ultimo = telemetriaRepository.findTopByLoteIdOrderByTimestampDesc(loteId);
        if (ultimo == null || ultimo.getLecturas() == null) {
            throw new ResourceNotFoundException("No hay datos climáticos para el lote solicitado.");
        }

        return new ClimaActualDTO(
                ultimo.getDispositivoId(),
                ultimo.getTimestamp(),
                ultimo.getLecturas().getAmbiente() != null ? ultimo.getLecturas().getAmbiente().getTempAire() : null,
                ultimo.getLecturas().getAmbiente() != null ? ultimo.getLecturas().getAmbiente().getHumAire() : null,
                ultimo.getLecturas().getAmbiente() != null ? ultimo.getLecturas().getAmbiente().getPresion() : null,
                ultimo.getLecturas().getClima() != null ? ultimo.getLecturas().getClima().getPrecipitacion() : null,
                ultimo.getLecturas().getClima() != null ? ultimo.getLecturas().getClima().getViento() : null,
                ultimo.getLecturas().getSuelo() != null ? ultimo.getLecturas().getSuelo().getHumSuelo() : null,
                ultimo.getLecturas().getSuelo() != null ? ultimo.getLecturas().getSuelo().getTempSuelo() : null
        );
    }
}
