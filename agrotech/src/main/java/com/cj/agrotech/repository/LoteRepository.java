package com.cj.agrotech.repository;

import com.cj.agrotech.domain.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoteRepository extends JpaRepository<Lote, UUID> {

    List<Lote> findByFincaId(UUID fincaId);
}
