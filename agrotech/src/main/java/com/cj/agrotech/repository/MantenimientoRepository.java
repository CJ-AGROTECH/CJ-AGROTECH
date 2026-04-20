package com.cj.agrotech.repository;

import com.cj.agrotech.domain.entity.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MantenimientoRepository extends JpaRepository<Mantenimiento, UUID> {}
