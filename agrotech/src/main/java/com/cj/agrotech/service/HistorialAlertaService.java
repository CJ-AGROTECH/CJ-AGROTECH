package com.cj.agrotech.service;

import com.cj.agrotech.config.UserDetailsImpl;
import com.cj.agrotech.domain.entity.HistorialAlerta;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.HistorialAlertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        return historialAlertaRepository.findByLeidaFalseAndDispositivoLoteFincaUsuarioId(obtenerUsuarioAutenticadoId());
    }

    @Transactional(readOnly = true)
    public HistorialAlerta obtenerPorId(UUID id) {
        return historialAlertaRepository.findByIdAndDispositivoLoteFincaUsuarioId(id, obtenerUsuarioAutenticadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Historial de alerta no encontrado o no pertenece al usuario."));
    }

    @Transactional(readOnly = true)
    public List<HistorialAlerta> obtenerAlertasNoLeidas() {
        return historialAlertaRepository.findByLeidaFalseAndDispositivoLoteFincaUsuarioId(obtenerUsuarioAutenticadoId());
    }

    @Transactional(readOnly = true)
    public List<HistorialAlerta> obtenerAlertasNoVistas() {
        return historialAlertaRepository.findByLeidaFalseAndDispositivoLoteFincaUsuarioId(obtenerUsuarioAutenticadoId());
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
        UUID usuarioId = obtenerUsuarioAutenticadoId();
        if (!historialAlertaRepository.existsByIdAndDispositivoLoteFincaUsuarioId(id, usuarioId)) {
            throw new ResourceNotFoundException("Historial de alerta no encontrado o no pertenece al usuario.");
        }
        historialAlertaRepository.deleteById(id);
    }

    private UUID obtenerUsuarioAutenticadoId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new ResourceNotFoundException("Usuario no autenticado.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }

        throw new ResourceNotFoundException("No se pudo identificar el usuario autenticado.");
    }
}
