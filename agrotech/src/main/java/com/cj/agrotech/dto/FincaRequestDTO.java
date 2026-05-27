package com.cj.agrotech.dto;

import java.util.UUID;

public record FincaRequestDTO(
        String nombre,
        String municipio,
        Double latitud,
        Double longitud,
        UUID usuarioId
) {}
