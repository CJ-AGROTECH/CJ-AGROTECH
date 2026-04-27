package com.cj.agrotech.dto;

import java.util.UUID;

public record FincaDTO(
        UUID id,
        String nombre,
        String municipio,
        UUID usuarioId
) {}
