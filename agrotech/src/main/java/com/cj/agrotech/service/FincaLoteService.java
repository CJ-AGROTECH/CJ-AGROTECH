package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.Finca;
import com.cj.agrotech.domain.entity.Lote;
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
@Transactional
public class FincaLoteService {

    private final FincaRepository fincaRepository;
    private final LoteRepository loteRepository;

    // Finca Operations
    public List<Finca> obtenerFincasPorUsuario(UUID usuarioId) {
        return fincaRepository.findByUsuarioId(usuarioId);
    }

    public Finca registrarFinca(Finca finca) {
        return fincaRepository.save(finca);
    }

    public void eliminarFinca(UUID id) {
        Finca finca = fincaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finca no encontrada con ID: " + id));
        fincaRepository.delete(finca);
    }

    // Lote Operations
    public List<Lote> obtenerLotesPorFinca(UUID fincaId) {
        Finca finca = fincaRepository.findById(fincaId)
                .orElseThrow(() -> new ResourceNotFoundException("Finca no encontrada con ID: " + fincaId));
        return loteRepository.findByFincaId(fincaId);
    }

    public Lote registrarLote(Lote lote) {
        // Validar que la Finca existe
        Finca finca = fincaRepository.findById(lote.getFinca().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Finca no encontrada"));
        return loteRepository.save(lote);
    }
}

