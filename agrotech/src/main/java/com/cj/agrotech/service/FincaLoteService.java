package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.Finca;
import com.cj.agrotech.domain.entity.Lote;
import com.cj.agrotech.repository.FincaRepository;
import com.cj.agrotech.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FincaLoteService {

    private final FincaRepository fincaRepository;
    private final LoteRepository loteRepository;

    // --- FINCAS ---
    @Transactional(readOnly = true)
    public List<Finca> obtenerFincasPorUsuario(UUID usuarioId) {
        return fincaRepository.findByUsuarioId(usuarioId);
    }

    @Transactional
    public Finca registrarFinca(Finca finca) {
        return fincaRepository.save(finca);
    }

    @Transactional
    public void eliminarFinca(UUID fincaId) {
        fincaRepository.deleteById(fincaId);
    }

    @Transactional(readOnly = true)
    public List<Lote> obtenerLotesPorFinca(UUID fincaId) {
        return loteRepository.findByFincaId(fincaId);
    }

    @Transactional
    public Lote registrarLote(Lote lote) {
        return loteRepository.save(lote);
    }
}
