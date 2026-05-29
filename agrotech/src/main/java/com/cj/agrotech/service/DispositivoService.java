package com.cj.agrotech.service;

import com.cj.agrotech.config.UserDetailsImpl;
import com.cj.agrotech.domain.entity.Dispositivo;
import com.cj.agrotech.domain.entity.Lote;
import com.cj.agrotech.domain.entity.Mantenimiento;
import com.cj.agrotech.domain.enums.EstadoDispositivo;
import com.cj.agrotech.exception.BadRequestException;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.DispositivoRepository;
import com.cj.agrotech.repository.LoteRepository;
import com.cj.agrotech.repository.MantenimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DispositivoService {

    private final DispositivoRepository dispositivoRepository;
    private final LoteRepository loteRepository;
    private final MantenimientoRepository mantenimientoRepository;

    @Transactional(readOnly = true)
    public List<Dispositivo> listarTodos() {
        UUID usuarioId = obtenerUsuarioAutenticadoId();
        return dispositivoRepository.findByLoteFincaUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Dispositivo> listarPorLote(UUID loteId) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new BadRequestException("Lote no encontrado."));
        if (lote.getFinca() == null || lote.getFinca().getUsuario() == null || !lote.getFinca().getUsuario().getId().equals(obtenerUsuarioAutenticadoId())) {
            throw new ResourceNotFoundException("Dispositivo no encontrado.");
        }
        return dispositivoRepository.findByLoteId(loteId);
    }

    @Transactional(readOnly = true)
    public List<Dispositivo> listarDispositivosPorLote(UUID loteId) {
        return listarPorLote(loteId);
    }

    @Transactional(readOnly = true)
    public Dispositivo obtenerPorId(UUID id) {
        Dispositivo dispositivo = dispositivoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado."));
        if (dispositivo.getLote() == null || dispositivo.getLote().getFinca() == null || dispositivo.getLote().getFinca().getUsuario() == null ||
                !dispositivo.getLote().getFinca().getUsuario().getId().equals(obtenerUsuarioAutenticadoId())) {
            throw new ResourceNotFoundException("Dispositivo no encontrado.");
        }
        return dispositivo;
    }

    @Transactional(readOnly = true)
    public Optional<Dispositivo> obtenerPorMacAddress(String macAddress) {
        return dispositivoRepository.findByMacAddress(macAddress);
    }

    @Transactional
    public Dispositivo crear(Dispositivo dispositivo) {
        // Validar unicidad de MAC
        if (dispositivoRepository.findByMacAddress(dispositivo.getMacAddress()).isPresent()) {
            throw new BadRequestException("MAC Address ya registrada.");
        }
        // Validar lote y propiedad
        Lote lote = loteRepository.findById(dispositivo.getLote().getId())
                .orElseThrow(() -> new BadRequestException("Lote no encontrado."));
        if (lote.getFinca() == null || lote.getFinca().getUsuario() == null || !lote.getFinca().getUsuario().getId().equals(obtenerUsuarioAutenticadoId())) {
            throw new BadRequestException("No tienes permisos para crear un dispositivo en este lote.");
        }
        dispositivo.setLote(lote);
        dispositivo.setUltimaSincronizacion(LocalDateTime.now());
        return dispositivoRepository.save(dispositivo);
    }

    @Transactional
    public Dispositivo registrarDispositivo(Dispositivo dispositivo) {
        return crear(dispositivo);
    }

    @Transactional
    public Dispositivo actualizar(UUID id, Dispositivo datos) {
        Dispositivo existente = obtenerPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setEstado(datos.getEstado());
        return dispositivoRepository.save(existente);
    }

    @Transactional
    public Dispositivo actualizarEstado(UUID id, EstadoDispositivo estado) {
        Dispositivo dispositivo = obtenerPorId(id);
        dispositivo.setEstado(estado);
        return dispositivoRepository.save(dispositivo);
    }

    @Transactional
    public Dispositivo actualizarEstadoDispositivo(UUID id, EstadoDispositivo nuevoEstado) {
        Dispositivo dispositivo = obtenerPorId(id);
        dispositivo.setEstado(nuevoEstado);
        dispositivo.setUltimaSincronizacion(LocalDateTime.now());
        return dispositivoRepository.save(dispositivo);
    }

    @Transactional
    public void eliminar(UUID id) {
        Dispositivo existente = obtenerPorId(id);
        dispositivoRepository.delete(existente);
    }

    @Transactional
    public Mantenimiento registrarMantenimiento(Mantenimiento mantenimiento) {
        // Validar dispositivo
        Dispositivo dispositivo = obtenerPorId(mantenimiento.getDispositivo().getId());
        mantenimiento.setDispositivo(dispositivo);
        return mantenimientoRepository.save(mantenimiento);
    }

    private UUID obtenerUsuarioAutenticadoId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new BadRequestException("Usuario no autenticado.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }

        throw new BadRequestException("No se pudo identificar el usuario autenticado.");
    }
}