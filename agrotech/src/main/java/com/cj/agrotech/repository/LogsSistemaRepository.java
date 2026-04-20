package com.cj.agrotech.repository;

import com.cj.agrotech.domain.entity.LogsSistema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogsSistemaRepository extends JpaRepository<LogsSistema, UUID> {}
