package com.cj.agrotech.dto;

import com.cj.agrotech.domain.enums.Rol;

import java.util.UUID;

public record UsuarioDTO(
        UUID id,
        String nombre,
        String email,
        Rol rol
) {}
