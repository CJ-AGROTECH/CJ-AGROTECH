package com.cj.agrotech.repository;

import com.cj.agrotech.domain.entity.Finca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FincaRepository extends JpaRepository<Finca, UUID> {
    List<Finca> findByUsuarioId(UUID id);
    java.util.Optional<Finca> findByNombre(String nombre);
}
