package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.Dispositivo;
import com.cj.agrotech.domain.entity.Mantenimiento;
import com.cj.agrotech.domain.enums.EstadoDispositivo;
import com.cj.agrotech.repository.DispositivoRepository;
import com.cj.agrotech.repository.MantenimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DispositivoService {

    private final DispositivoRepository dispositivoRepository;
    private final MantenimientoRepository mantenimientoRepository;

    @Transactional(readOnly = true)
    public List<Dispositivo> listarDispositivosPorLote(UUID loteId) {
        return dispositivoRepository.findByLoteId(loteId);
    }

    @Transactional
    public Dispositivo registrarDispositivo(Dispositivo dispositivo) {
        dispositivo.setUltimaSincronizacion(LocalDateTime.now());
        return dispositivoRepository.save(dispositivo);
    }

    @Transactional
    public Dispositivo actualizarEstadoDispositivo(UUID id, EstadoDispositivo estado) {
        Dispositivo d = dispositivoRepository.findById(id).orElseThrow(() -> new RuntimeException("Dispositivo no encontrado"));
        d.setEstado(estado);
        return dispositivoRepository.save(d);
    }

    @Transactional
    public Mantenimiento registrarMantenimiento(Mantenimiento mantenimiento) {
        return mantenimientoRepository.save(mantenimiento);
    }
}