package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.Finca;
import com.cj.agrotech.repository.FincaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenMeteoIngestaService {

    private final FincaRepository fincaRepository;
    private final MeteoService meteoService;

    /**
     * Cada 15 minutos: actualiza clima Open-Meteo por finca/lote y evalúa alertas.
     * Cubre usuarios sin sensores (solo lote) y usuarios con dispositivos (clima por nodo).
     */
    @Scheduled(fixedRate = 900000)
    public void ingestarDatosClimaticosAutomaticamente() {
        log.info("Ingesta automática Open-Meteo (fincas y lotes)");
        List<Finca> fincas = fincaRepository.findAll();
        for (Finca finca : fincas) {
            try {
                meteoService.sincronizarClimaPorFinca(finca.getId());
            } catch (Exception ex) {
                log.error("Error ingesta clima finca {}: {}", finca.getId(), ex.getMessage());
            }
        }
        log.info("Ingesta automática finalizada para {} fincas", fincas.size());
    }
}
