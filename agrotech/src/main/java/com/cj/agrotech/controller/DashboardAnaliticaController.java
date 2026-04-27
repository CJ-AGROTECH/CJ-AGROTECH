package com.cj.agrotech.controller;

import com.cj.agrotech.domain.document.Telemetria;
import com.cj.agrotech.service.AnaliticaDashboardService;
import com.cj.agrotech.service.ExportacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardAnaliticaController {
    private final AnaliticaDashboardService analiticaService;
    private final ExportacionService exportacionService;

    @GetMapping("/historico/{dispositivoId}")
    public List<Telemetria> obtenerDatosGraficos(@PathVariable UUID dispositivoId) {
        return analiticaService.obtenerHistoricoTelemetria(dispositivoId);
    }

    @GetMapping("/eficiencia-hidrica/{dispositivoId}")
    public Map<String, Object> obtenerEficienciaHidrica(@PathVariable UUID dispositivoId) {
        return analiticaService.calcularEficienciaHidrica(dispositivoId);
    }

    @GetMapping("/exportar/{dispositivoId}")
    public ResponseEntity<byte[]> exportarDatosCSV(@PathVariable UUID dispositivoId) {
        String csvData = analiticaService.exportarCSV(dispositivoId);
        byte[] output = csvData.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "datos_clima_nodo_" + dispositivoId + ".csv");

        return ResponseEntity.ok().headers(headers).body(output);
    }
}
