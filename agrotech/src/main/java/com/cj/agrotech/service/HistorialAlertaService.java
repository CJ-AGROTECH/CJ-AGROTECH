package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.HistorialAlerta;
import com.cj.agrotech.repository.HistorialAlertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistorialAlertaService {

    private final HistorialAlertaRepository historialAlertaRepository;

    @Transactional(readOnly = true)
    public List<HistorialAlerta> obtenerAlertasNoVistas() {
        return historialAlertaRepository.findAll().stream()
                .filter(a -> !a.getVistoPorUsuario())
                .toList();
    }

    @Transactional
    public void marcarAlertaComoVista(UUID alertaId) {
        HistorialAlerta alerta = historialAlertaRepository.findById(alertaId)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));
        alerta.setVistoPorUsuario(true);
        historialAlertaRepository.save(alerta);
    }
}
