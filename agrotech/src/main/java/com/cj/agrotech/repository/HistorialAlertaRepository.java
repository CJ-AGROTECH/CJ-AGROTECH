package com.cj.agrotech.repository;

import com.cj.agrotech.domain.entity.HistorialAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HistorialAlertaRepository extends JpaRepository<HistorialAlerta, UUID> {
    List<HistorialAlerta> findByLeidaFalseAndDispositivoLoteFincaUsuarioId(UUID usuarioId);
    Optional<HistorialAlerta> findByIdAndDispositivoLoteFincaUsuarioId(UUID id, UUID usuarioId);
    boolean existsByIdAndDispositivoLoteFincaUsuarioId(UUID id, UUID usuarioId);
}
