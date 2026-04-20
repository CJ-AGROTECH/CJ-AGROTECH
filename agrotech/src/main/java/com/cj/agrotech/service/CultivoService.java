package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.CatalogoCultivo;
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

    @Transactional
    public CatalogoCultivo registrarCultivo(CatalogoCultivo cultivo) {
        return cultivoRepository.save(cultivo);
    }
}
