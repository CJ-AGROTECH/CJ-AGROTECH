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
public class CatalogoCultivoService {

    private final CatalogoCultivoRepository catalogoCultivoRepository;

    @Transactional(readOnly = true)
    public List<CatalogoCultivo> listarTodos() {
        return catalogoCultivoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CatalogoCultivo obtenerPorId(UUID id) {
        return catalogoCultivoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado."));
    }

    @Transactional
    public CatalogoCultivo crear(CatalogoCultivo cultivo) {
        return catalogoCultivoRepository.save(cultivo);
    }

    @Transactional
    public CatalogoCultivo actualizar(UUID id, CatalogoCultivo datos) {
        CatalogoCultivo existente = obtenerPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setTempOptimaMin(datos.getTempOptimaMin());
        existente.setTempOptimaMax(datos.getTempOptimaMax());
        existente.setHumedadOptimaMin(datos.getHumedadOptimaMin());
        existente.setHumedadOptimaMax(datos.getHumedadOptimaMax());
        return catalogoCultivoRepository.save(existente);
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!catalogoCultivoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cultivo no encontrado.");
        }
        catalogoCultivoRepository.deleteById(id);
    }
}
