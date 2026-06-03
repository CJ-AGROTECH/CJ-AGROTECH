package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.HistorialAlerta;
import com.cj.agrotech.dto.HistorialAlertaResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertStreamingService {

    private final ObjectMapper objectMapper;
    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    public SseEmitter register(UUID usuarioId) {
        SseEmitter emitter = new SseEmitter(0L);
        emittersByUser.computeIfAbsent(usuarioId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable cleanup = () -> removeEmitter(usuarioId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException ex) {
            log.debug("No se pudo enviar evento connected SSE: {}", ex.getMessage());
        }
        return emitter;
    }

    public void publish(UUID usuarioId, HistorialAlerta alerta) {
        if (usuarioId == null || alerta == null) {
            return;
        }
        publish(usuarioId, mapToDto(alerta));
    }

    public void publish(UUID usuarioId, HistorialAlertaResponse dto) {
        if (usuarioId == null || dto == null) {
            return;
        }
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByUser.get(usuarioId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException ex) {
            log.error("Error serializando alerta para SSE", ex);
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("alerta").data(json));
            } catch (IOException ex) {
                removeEmitter(usuarioId, emitter);
            }
        }
    }

    private void removeEmitter(UUID usuarioId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByUser.get(usuarioId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                emittersByUser.remove(usuarioId);
            }
        }
    }

    public HistorialAlertaResponse mapToDto(HistorialAlerta a) {
        return new HistorialAlertaResponse(
                a.getId(),
                a.getMensaje(),
                a.getFecha(),
                a.getPrioridad() != null ? a.getPrioridad().name() : null,
                a.getLeida(),
                a.getDispositivo() != null ? a.getDispositivo().getId() : null,
                a.getDispositivo() != null ? a.getDispositivo().getNombre() : null,
                a.getLote() != null ? a.getLote().getId() : null,
                a.getLote() != null ? a.getLote().getNombre() : null
        );
    }
}
