package com.cj.agrotech.repository;

import com.cj.agrotech.domain.entity.HistorialAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HistorialAlertaRepository extends JpaRepository<HistorialAlerta, UUID> {}
