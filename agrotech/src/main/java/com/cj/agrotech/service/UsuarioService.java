package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.Usuario;
import com.cj.agrotech.exception.BadRequestException;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Transactional
    public Usuario crear(Usuario usuario) {
        // Validación de correo único
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new BadRequestException("El correo ya está registrado.");
        }
        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(UUID id, Usuario datosNuevos) {
        Usuario existente = obtenerPorId(id);
        existente.setNombre(datosNuevos.getNombre());
        existente.setRol(datosNuevos.getRol());
        // Para correo, verificar unicidad si cambia
        if (!existente.getEmail().equals(datosNuevos.getEmail())) {
            if (usuarioRepository.findByEmail(datosNuevos.getEmail()).isPresent()) {
                throw new BadRequestException("El correo ya está registrado.");
            }
            existente.setEmail(datosNuevos.getEmail());
        }
        return usuarioRepository.save(existente);
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado.");
        }
        usuarioRepository.deleteById(id);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con email " + email + " no encontrado."));
    }
}
