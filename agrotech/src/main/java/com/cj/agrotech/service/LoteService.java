package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.Finca;
import com.cj.agrotech.domain.entity.Lote;
import com.cj.agrotech.exception.BadRequestException;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.FincaRepository;
import com.cj.agrotech.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoteService {

    private final LoteRepository loteRepository;
    private final FincaRepository fincaRepository;

    @Transactional(readOnly = true)
    public List<Lote> listarTodos() {
        return loteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Lote> listarPorFinca(UUID fincaId) {
        return loteRepository.findByFincaId(fincaId);
    }

    @Transactional(readOnly = true)
    public Lote obtenerPorId(UUID id) {
        return loteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado."));
    }

    @Transactional
    public Lote crear(Lote lote) {
        // Validar que la finca existe
        Finca finca = fincaRepository.findById(lote.getFinca().getId())
                .orElseThrow(() -> new BadRequestException("Finca no encontrada."));
        lote.setFinca(finca);
        return loteRepository.save(lote);
    }

    @Transactional
    public Lote actualizar(UUID id, Lote datos) {
        Lote existente = obtenerPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setAreaHectareas(datos.getAreaHectareas());
        return loteRepository.save(existente);
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!loteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lote no encontrado.");
        }
        loteRepository.deleteById(id);
    }
}
