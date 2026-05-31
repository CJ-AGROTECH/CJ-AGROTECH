package com.cj.agrotech.service;

import com.cj.agrotech.config.UserDetailsImpl;
import com.cj.agrotech.domain.entity.CatalogoCultivo;
import com.cj.agrotech.exception.BadRequestException;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.CatalogoCultivoRepository;
import com.cj.agrotech.repository.LoteRepository;
import com.cj.agrotech.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CultivoService {

    private final CatalogoCultivoRepository cultivoRepository;
    private final LoteRepository loteRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<CatalogoCultivo> listarCultivos() {
        UUID usuarioId = obtenerUsuarioAutenticadoId();
        return cultivoRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public CatalogoCultivo obtenerPorId(UUID id) {
        UUID usuarioId = obtenerUsuarioAutenticadoId();
        return cultivoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado."));
    }

    @Transactional
    public CatalogoCultivo registrarCultivo(CatalogoCultivo cultivo) {
        UUID usuarioId = obtenerUsuarioAutenticadoId();
        cultivo.setUsuario(usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BadRequestException("Usuario autenticado no encontrado.")));
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
        UUID usuarioId = obtenerUsuarioAutenticadoId();
        CatalogoCultivo cultivo = cultivoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cultivo no encontrado."));

        if (!loteRepository.findByCultivoId(id).isEmpty()) {
            throw new BadRequestException("No se puede eliminar el cultivo porque tiene lotes asociados.");
        }
        cultivoRepository.delete(cultivo);
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
