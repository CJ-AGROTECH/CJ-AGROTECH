package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.LogsSistema;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.LogsSistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LogsSistemaService {

    private final LogsSistemaRepository logsSistemaRepository;

    @Transactional(readOnly = true)
    public List<LogsSistema> listarTodos() {
        return logsSistemaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public LogsSistema obtenerPorId(UUID id) {
        return logsSistemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Log del sistema no encontrado."));
    }

    @Transactional
    public LogsSistema crear(LogsSistema log) {
        return logsSistemaRepository.save(log);
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!logsSistemaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Log del sistema no encontrado.");
        }
        logsSistemaRepository.deleteById(id);
    }
}
