package com.cj.agrotech.repository;

import com.cj.agrotech.domain.entity.ConfiguracionAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConfiguracionAlertaRepository extends JpaRepository<ConfiguracionAlerta, UUID> {
    List<ConfiguracionAlerta> findByLoteId(UUID loteId);
}
