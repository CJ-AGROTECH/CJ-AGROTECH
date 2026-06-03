package com.cj.agrotech.repository;

import com.cj.agrotech.domain.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoteRepository extends JpaRepository<Lote, UUID> {

    List<Lote> findByFincaId(UUID fincaId);
    List<Lote> findByFincaUsuarioId(UUID usuarioId);
    List<Lote> findByCultivoId(UUID cultivoId);
    java.util.Optional<Lote> findByNombre(String nombre);

    @Query("SELECT l.finca.usuario.id FROM Lote l WHERE l.id = :loteId")
    Optional<UUID> findPropietarioUsuarioId(@Param("loteId") UUID loteId);
}
