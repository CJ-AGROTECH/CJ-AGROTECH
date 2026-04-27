package com.cj.agrotech.service;

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
        return dispositivoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Dispositivo> listarPorLote(UUID loteId) {
        return dispositivoRepository.findByLoteId(loteId);
    }

    @Transactional(readOnly = true)
    public List<Dispositivo> listarDispositivosPorLote(UUID loteId) {
        return listarPorLote(loteId);
    }

    @Transactional(readOnly = true)
    public Dispositivo obtenerPorId(UUID id) {
        return dispositivoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado."));
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
        // Validar lote
        Lote lote = loteRepository.findById(dispositivo.getLote().getId())
                .orElseThrow(() -> new BadRequestException("Lote no encontrado."));
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
        if (!dispositivoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dispositivo no encontrado.");
        }
        dispositivoRepository.deleteById(id);
    }

    @Transactional
    public Mantenimiento registrarMantenimiento(Mantenimiento mantenimiento) {
        // Validar dispositivo
        Dispositivo dispositivo = dispositivoRepository.findById(mantenimiento.getDispositivo().getId())
                .orElseThrow(() -> new BadRequestException("Dispositivo no encontrado."));
        mantenimiento.setDispositivo(dispositivo);
        return mantenimientoRepository.save(mantenimiento);
    }
}