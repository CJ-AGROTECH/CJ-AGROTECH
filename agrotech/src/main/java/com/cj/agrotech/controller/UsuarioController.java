package com.cj.agrotech.controller;

import com.cj.agrotech.domain.entity.Usuario;
import com.cj.agrotech.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> listarUsuarios() { return usuarioService.listarUsuarios(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crearUsuario(@RequestBody Usuario usuario) { return usuarioService.crearUsuario(usuario); }
}
