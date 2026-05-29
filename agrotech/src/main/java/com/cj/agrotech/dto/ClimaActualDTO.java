package com.cj.agrotech.dto;

import java.time.Instant;
import java.util.UUID;

public record ClimaActualDTO(
        UUID dispositivoId,
        Instant timestamp,
        Float tempAire,
        Float humAire,
        Float presion,
        Float precipitacion,
        Float viento,
        Float humSuelo,
        Float tempSuelo
) {}
