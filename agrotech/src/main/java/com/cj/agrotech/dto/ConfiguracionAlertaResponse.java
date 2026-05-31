package com.cj.agrotech.dto;

import java.util.UUID;

public record ConfiguracionAlertaResponse(
        UUID id,
        String tipo,
        Double umbralMin,
        Double umbralMax,
        String mensaje,
        String prioridad,
        UUID dispositivoId,
        String dispositivoNombre,
        UUID loteId,
        String loteNombre
) {
}
