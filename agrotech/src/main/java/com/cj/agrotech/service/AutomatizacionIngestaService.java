package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.Dispositivo;
import com.cj.agrotech.domain.enums.EstadoDispositivo;
import com.cj.agrotech.repository.DispositivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutomatizacionIngestaService {

    private final DispositivoRepository dispositivoRepository;
    private final OpenMeteoIngestaService openMeteoIngestaService;

    // Se ejecuta automáticamente cada 15 minutos (900000 ms)
    @Scheduled(fixedRate = 900000)
    @Transactional(readOnly = true)
    public void recolectarDatosClimaticosAutomaticamente() {
        log.info("Iniciando recolección automática de datos de sensores (Open-Meteo)...");

        List<Dispositivo> dispositivosActivos = dispositivoRepository.findAll().stream()
                .filter(d -> d.getEstado() == EstadoDispositivo.ACTIVO)
                .toList();

        for (Dispositivo d : dispositivosActivos) {
            try {
                // Toma las coordenadas de la finca a la que pertenece el lote del dispositivo
                Double lat = d.getLote().getFinca().getLatitud();
                Double lon = d.getLote().getFinca().getLongitud();

                openMeteoIngestaService.ingestarDatosClimaticos(d.getId(), d.getLote().getId(), lat, lon);
                log.info("Datos recolectados exitosamente para el nodo: {}", d.getMacAddress());
            } catch (Exception e) {
                log.error("Fallo al recolectar datos para el nodo {}: {}", d.getMacAddress(), e.getMessage());
            }
        }
    }
}
