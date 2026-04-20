package com.cj.agrotech.repository;

import com.cj.agrotech.domain.entity.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DispositivoRepository extends JpaRepository<Dispositivo, UUID> {
    Optional<Dispositivo> findByMacAddress(String macAddress);

    List<Dispositivo> findByLoteId(UUID loteId);
}
