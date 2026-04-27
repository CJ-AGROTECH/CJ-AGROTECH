package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.Mantenimiento;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.MantenimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MantenimientoService {

    private final MantenimientoRepository mantenimientoRepository;

    @Transactional(readOnly = true)
    public List<Mantenimiento> listarTodos() {
        return mantenimientoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Mantenimiento obtenerPorId(UUID id) {
        return mantenimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mantenimiento no encontrado."));
    }

    @Transactional
    public Mantenimiento crear(Mantenimiento mantenimiento) {
        return mantenimientoRepository.save(mantenimiento);
    }

    @Transactional
    public Mantenimiento actualizar(UUID id, Mantenimiento datos) {
        Mantenimiento existente = obtenerPorId(id);
        existente.setFecha(datos.getFecha());
        existente.setDescripcion(datos.getDescripcion());
        return mantenimientoRepository.save(existente);
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!mantenimientoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Mantenimiento no encontrado.");
        }
        mantenimientoRepository.deleteById(id);
    }
}
