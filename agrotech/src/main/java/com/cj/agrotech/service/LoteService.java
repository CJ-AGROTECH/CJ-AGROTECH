package com.cj.agrotech.service;

import com.cj.agrotech.config.UserDetailsImpl;
import com.cj.agrotech.domain.entity.Finca;
import com.cj.agrotech.domain.entity.Lote;
import com.cj.agrotech.domain.entity.CatalogoCultivo;
import com.cj.agrotech.exception.BadRequestException;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.CatalogoCultivoRepository;
import com.cj.agrotech.repository.FincaRepository;
import com.cj.agrotech.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoteService {

    private final LoteRepository loteRepository;
    private final FincaRepository fincaRepository;
    private final CatalogoCultivoRepository catalogoCultivoRepository;

    @Transactional(readOnly = true)
    public List<Lote> listarTodos() {
        UUID usuarioId = obtenerUsuarioAutenticadoId();
        return loteRepository.findByFincaUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Lote> listarPorFinca(UUID fincaId) {
        Finca finca = fincaRepository.findById(fincaId)
                .orElseThrow(() -> new BadRequestException("Finca no encontrada."));
        if (!finca.getUsuario().getId().equals(obtenerUsuarioAutenticadoId())) {
            throw new ResourceNotFoundException("Lote no encontrado.");
        }
        return loteRepository.findByFincaId(fincaId);
    }

    @Transactional(readOnly = true)
    public Lote obtenerPorId(UUID id) {
        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado."));
        if (lote.getFinca() == null || lote.getFinca().getUsuario() == null || !lote.getFinca().getUsuario().getId().equals(obtenerUsuarioAutenticadoId())) {
            throw new ResourceNotFoundException("Lote no encontrado.");
        }
        return lote;
    }

    @Transactional
    public Lote crear(Lote lote) {
        Finca finca = fincaRepository.findById(lote.getFinca().getId())
                .orElseThrow(() -> new BadRequestException("Finca no encontrada."));
        CatalogoCultivo cultivo = catalogoCultivoRepository.findById(lote.getCultivo().getId())
                .orElseThrow(() -> new BadRequestException("Cultivo no encontrado."));
        lote.setFinca(finca);
        lote.setCultivo(cultivo);
        return loteRepository.save(lote);
    }

    @Transactional
    public Lote actualizar(UUID id, Lote datos) {
        Lote existente = obtenerPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setAreaHectareas(datos.getAreaHectareas());
        if (datos.getFinca() != null && datos.getFinca().getId() != null) {
            Finca finca = fincaRepository.findById(datos.getFinca().getId())
                    .orElseThrow(() -> new BadRequestException("Finca no encontrada."));
            existente.setFinca(finca);
        }
        if (datos.getCultivo() != null && datos.getCultivo().getId() != null) {
            CatalogoCultivo cultivo = catalogoCultivoRepository.findById(datos.getCultivo().getId())
                    .orElseThrow(() -> new BadRequestException("Cultivo no encontrado."));
            existente.setCultivo(cultivo);
        }
        return loteRepository.save(existente);
    }

    @Transactional
    public void eliminar(UUID id) {
        Lote existente = obtenerPorId(id);
        loteRepository.delete(existente);
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
