package com.cj.agrotech.service;

import com.cj.agrotech.domain.entity.ConfiguracionAlerta;
import com.cj.agrotech.exception.ResourceNotFoundException;
import com.cj.agrotech.repository.ConfiguracionAlertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfiguracionAlertaService {

    private final ConfiguracionAlertaRepository configuracionAlertaRepository;

    @Transactional(readOnly = true)
    public List<ConfiguracionAlerta> listarTodos() {
        return configuracionAlertaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ConfiguracionAlerta obtenerPorId(UUID id) {
        return configuracionAlertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de alerta no encontrada."));
    }

    @Transactional
    public ConfiguracionAlerta crear(ConfiguracionAlerta configuracion) {
        return configuracionAlertaRepository.save(configuracion);
    }

    @Transactional
    public ConfiguracionAlerta actualizar(UUID id, ConfiguracionAlerta datos) {
        ConfiguracionAlerta existente = obtenerPorId(id);
        existente.setVariable(datos.getVariable());
        existente.setCondicion(datos.getCondicion());
        existente.setUmbral(datos.getUmbral());
        existente.setPrioridad(datos.getPrioridad());
        return configuracionAlertaRepository.save(existente);
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!configuracionAlertaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Configuración de alerta no encontrada.");
        }
        configuracionAlertaRepository.deleteById(id);
    }
}
