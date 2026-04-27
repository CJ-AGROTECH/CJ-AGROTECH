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

    public record LecturasDTO(
            AmbienteDTO ambiente,
            SueloDTO suelo,
            ClimaDTO clima
    ) {}

    public record AmbienteDTO(
            Float tempAire,
            Float humAire,
            Float presion,
            Float lux
    ) {}

    public record SueloDTO(
            Float humSuelo,
            Float tempSuelo
    ) {}

    public record ClimaDTO(
            Float precipitacion,
            Float viento
    ) {}

    public record DiagnosticoDTO(
            Integer bateria,
            Integer rssiWifi
    ) {}
}
