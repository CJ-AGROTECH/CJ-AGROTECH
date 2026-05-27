package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.Finca;
import com.cj.agrotech.domain.entity.Usuario;
import com.cj.agrotech.exception.BadRequestException;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.FincaRepository;
import com.cj.agrotech.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
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
        return fincaRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Finca obtenerPorId(UUID id) {
        return fincaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finca no encontrada."));
    }

    @Transactional
    public Finca crear(Finca finca) {
        Usuario usuario = usuarioRepository.findById(finca.getUsuario().getId())
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
        if (!fincaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Finca no encontrada.");
        }
        fincaRepository.deleteById(id);
    }
}
