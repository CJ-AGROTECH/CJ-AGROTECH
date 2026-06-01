package com.cj.agrotech.dto;

import java.util.UUID;

public record ConfiguracionAlertaRequest(
        UUID dispositivoId,
        UUID loteId,
        String tipo,
        Double umbralMin,
        Double umbralMax,
        String mensaje,
        String prioridad,
        String condicion,
        Double umbral
) {
}
