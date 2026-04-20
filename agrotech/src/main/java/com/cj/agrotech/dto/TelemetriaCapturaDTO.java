package com.cj.agrotech.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TelemetriaCapturaDTO(
        @NotNull(message = "El ID del dispositivo es obligatorio")
        UUID dispositivoId,

        @NotNull(message = "El ID del lote es obligatorio")
        UUID loteId,

        @NotNull(message = "Las lecturas son obligatorias")
        LecturasDTO lecturas,

        @NotNull
        DiagnosticoDTO diagnostico
) {
}
