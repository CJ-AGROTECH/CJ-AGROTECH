package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.CatalogoCultivo;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.CatalogoCultivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CultivoService {

    private final CatalogoCultivoRepository cultivoRepository;

    @Transactional(readOnly = true)
    public List<CatalogoCultivo> listarCultivos() {
        return cultivoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CatalogoCultivo obtenerPorId(UUID id) {
        return cultivoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado."));
    }

    @Transactional
    public CatalogoCultivo registrarCultivo(CatalogoCultivo cultivo) {
        return cultivoRepository.save(cultivo);
    }

    @Transactional
    public CatalogoCultivo actualizar(UUID id, CatalogoCultivo datos) {
        CatalogoCultivo existente = obtenerPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setVariedad(datos.getVariedad());
        existente.setDescripcion(datos.getDescripcion());
        existente.setDiasCrecimiento(datos.getDiasCrecimiento());
        return cultivoRepository.save(existente);
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!cultivoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cultivo no encontrado.");
        }
        cultivoRepository.deleteById(id);
    }
}
