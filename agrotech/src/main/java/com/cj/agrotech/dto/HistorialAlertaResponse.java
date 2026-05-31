package com.cj.agrotech.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HistorialAlertaResponse(
        UUID id,
        String mensaje,
        LocalDateTime fecha,
        String prioridad,
        Boolean leida,
        UUID dispositivoId,
        String dispositivoNombre,
        UUID loteId,
        String loteNombre
) {
}
