package com.cj.agrotech.service;

import com.cj.agrotech.domain.document.Telemetria;
import com.cj.agrotech.repository.TelemetriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExportacionService {

    private final TelemetriaRepository telemetriaRepository;

    public String generarCsvHistorico(UUID dispositivoId) {
        List<Telemetria> datos = telemetriaRepository.findByDispositivoIdOrderByTimestampDesc(dispositivoId);
        StringBuilder csv = new StringBuilder("Timestamp,TempAire,HumAire,HumSuelo,TempSuelo,Precipitacion\n");

        for (Telemetria t : datos) {
            csv.append(t.getTimestamp()).append(",")
                    .append(t.getLecturas().getAmbiente().getTempAire()).append(",")
                    .append(t.getLecturas().getAmbiente().getHumAire()).append(",")
                    .append(t.getLecturas().getSuelo().getHumSuelo()).append(",")
                    .append(t.getLecturas().getSuelo().getTempSuelo()).append(",")
                    .append(t.getLecturas().getClima().getPrecipitacion()).append("\n");
        }
        return csv.toString();
    }
}
