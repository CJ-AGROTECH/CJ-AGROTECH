package com.cj.agrotech.repository;

import com.cj.agrotech.domain.entity.CatalogoCultivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogoCultivoRepository extends JpaRepository<CatalogoCultivo, UUID> {
    List<CatalogoCultivo> findByUsuarioId(UUID usuarioId);
    Optional<CatalogoCultivo> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}
