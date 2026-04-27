package com.cj.agrotech.controller;

import com.cj.agrotech.domain.entity.Usuario;
import com.cj.agrotech.dto.UsuarioDTO;
import com.cj.agrotech.dto.UsuarioRequestDTO;
import com.cj.agrotech.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioDTO> listar() {
        return usuarioService.listarTodos().stream()
                .map(u -> new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail(), u.getRol()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UsuarioDTO obtenerPorId(@PathVariable UUID id) {
        Usuario u = usuarioService.obtenerPorId(id);
        return new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail(), u.getRol());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDTO crear(@RequestBody UsuarioRequestDTO request) {
        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setPassword(request.password());
        usuario.setRol(request.rol());
        Usuario creado = usuarioService.crear(usuario);
        return new UsuarioDTO(creado.getId(), creado.getNombre(), creado.getEmail(), creado.getRol());
    }

    @PutMapping("/{id}")
    public UsuarioDTO actualizar(@PathVariable UUID id, @RequestBody UsuarioRequestDTO request) {
        Usuario datos = new Usuario();
        datos.setNombre(request.nombre());
        datos.setEmail(request.email());
        datos.setRol(request.rol());
        Usuario actualizado = usuarioService.actualizar(id, datos);
        return new UsuarioDTO(actualizado.getId(), actualizado.getNombre(), actualizado.getEmail(), actualizado.getRol());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable UUID id) {
        usuarioService.eliminar(id);
    }
}
