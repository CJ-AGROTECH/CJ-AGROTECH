package com.cj.agrotech.dto;

import java.util.UUID;

public record LoteDTO(
        UUID id,
        String nombre,
        Double areaHectareas,
        UUID fincaId,
        UUID cultivoId
) {}
