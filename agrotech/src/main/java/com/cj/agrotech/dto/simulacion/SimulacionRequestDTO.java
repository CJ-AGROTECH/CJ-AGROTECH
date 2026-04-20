package com.cj.agrotech.dto.simulacion;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SimulacionRequestDTO(
        @NotNull(message = "El ID del dispositivo es obligatorio") UUID dispositivoId,
        @NotNull(message = "El ID del lote es obligatorio") UUID loteId
) {}
