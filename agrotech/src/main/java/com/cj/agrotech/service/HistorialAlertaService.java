package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.HistorialAlerta;
import com.cj.agrotech.exception.ResourceNotFoundException;
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
    public List<HistorialAlerta> listarTodos() {
        return historialAlertaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public HistorialAlerta obtenerPorId(UUID id) {
        return historialAlertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Historial de alerta no encontrado."));
    }

    @Transactional(readOnly = true)
    public List<HistorialAlerta> obtenerAlertasNoLeidas() {
        return historialAlertaRepository.findAll().stream()
                .filter(a -> !a.getLeida())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistorialAlerta> obtenerAlertasNoVistas() {
        return historialAlertaRepository.findAll().stream()
                .filter(a -> !a.getLeida())
                .toList();
    }

    @Transactional
    public HistorialAlerta crear(HistorialAlerta historial) {
        return historialAlertaRepository.save(historial);
    }

    @Transactional
    public void marcarComoLeida(UUID id) {
        HistorialAlerta alerta = obtenerPorId(id);
        alerta.setLeida(true);
        historialAlertaRepository.save(alerta);
    }

    @Transactional
    public void marcarAlertaComoVista(UUID id) {
        marcarComoLeida(id);
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!historialAlertaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Historial de alerta no encontrado.");
        }
        historialAlertaRepository.deleteById(id);
    }
}
