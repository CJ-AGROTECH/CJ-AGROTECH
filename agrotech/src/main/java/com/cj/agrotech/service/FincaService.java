package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.Finca;
import com.cj.agrotech.domain.entity.Usuario;
import com.cj.agrotech.exception.BadRequestException;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.FincaRepository;
import com.cj.agrotech.repository.UsuarioRepository;
import com.cj.agrotech.config.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FincaService {

    private final FincaRepository fincaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Finca> listarTodas() {
        return fincaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Finca> listarPorUsuario(UUID usuarioId) {
        UUID usuarioActualId = obtenerUsuarioAutenticadoId();
        if (!usuarioActualId.equals(usuarioId)) {
            throw new ResourceNotFoundException("Finca no encontrada.");
        }
        return fincaRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Finca> listarPorUsuarioActual() {
        UUID usuarioId = obtenerUsuarioAutenticadoId();
        return fincaRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Finca obtenerPorId(UUID id) {
        Finca finca = fincaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finca no encontrada."));
        validarPropietario(finca);
        return finca;
    }

    @Transactional
    public Finca crear(Finca finca) {
        UUID usuarioId = obtenerUsuarioAutenticadoId();
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BadRequestException("Usuario propietario no encontrado."));
        finca.setUsuario(usuario);
        return fincaRepository.save(finca);
    }

    @Transactional
    public Finca actualizar(UUID id, Finca datos) {
        Finca existente = obtenerPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setMunicipio(datos.getMunicipio());
        existente.setLatitud(datos.getLatitud());
        existente.setLongitud(datos.getLongitud());
        return fincaRepository.save(existente);
    }

    @Transactional
    public void eliminar(UUID id) {
        Finca existente = obtenerPorId(id);
        fincaRepository.delete(existente);
    }

    private void validarPropietario(Finca finca) {
        UUID usuarioId = obtenerUsuarioAutenticadoId();
        if (finca.getUsuario() == null || !usuarioId.equals(finca.getUsuario().getId())) {
            throw new ResourceNotFoundException("Finca no encontrada.");
        }
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
