package com.cj.agrotech.dto;

import java.util.UUID;

public record LoteRequestDTO(
        String nombre,
        Double areaHectareas,
        UUID fincaId,
        UUID cultivoId
) {}
