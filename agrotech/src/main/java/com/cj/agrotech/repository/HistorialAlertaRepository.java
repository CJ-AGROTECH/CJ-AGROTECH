package com.cj.agrotech.repository;

import com.cj.agrotech.domain.entity.HistorialAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HistorialAlertaRepository extends JpaRepository<HistorialAlerta, UUID> {
    @Query("SELECT h FROM HistorialAlerta h WHERE h.leida = false AND ((h.dispositivo IS NOT NULL AND h.dispositivo.lote.finca.usuario.id = :usuarioId) OR (h.lote IS NOT NULL AND h.lote.finca.usuario.id = :usuarioId))")
    List<HistorialAlerta> findByLeidaFalseAndUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT h FROM HistorialAlerta h WHERE h.id = :id AND ((h.dispositivo IS NOT NULL AND h.dispositivo.lote.finca.usuario.id = :usuarioId) OR (h.lote IS NOT NULL AND h.lote.finca.usuario.id = :usuarioId))")
    Optional<HistorialAlerta> findByIdAndUsuarioId(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM HistorialAlerta h WHERE h.id = :id AND ((h.dispositivo IS NOT NULL AND h.dispositivo.lote.finca.usuario.id = :usuarioId) OR (h.lote IS NOT NULL AND h.lote.finca.usuario.id = :usuarioId))")
    boolean existsByIdAndUsuarioId(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    @Query("""
            SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM HistorialAlerta h
            WHERE h.mensaje = :mensaje AND h.fecha > :desde
            AND (
                (:dispositivoId IS NOT NULL AND h.dispositivo IS NOT NULL AND h.dispositivo.id = :dispositivoId)
                OR (:loteId IS NOT NULL AND h.lote IS NOT NULL AND h.lote.id = :loteId)
            )
            """)
    boolean existsAlertaReciente(
            @Param("mensaje") String mensaje,
            @Param("desde") LocalDateTime desde,
            @Param("dispositivoId") UUID dispositivoId,
            @Param("loteId") UUID loteId);
}
