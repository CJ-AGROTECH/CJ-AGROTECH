package com.cj.agrotech.repository;

import com.cj.agrotech.domain.entity.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DispositivoRepository extends JpaRepository<Dispositivo, UUID> {
    Optional<Dispositivo> findByMacAddress(String macAddress);

    List<Dispositivo> findByLoteId(UUID loteId);
    List<Dispositivo> findByLoteFincaUsuarioId(UUID usuarioId);

    @Query("SELECT d.lote.finca.usuario.id FROM Dispositivo d WHERE d.id = :dispositivoId")
    Optional<UUID> findPropietarioUsuarioId(@Param("dispositivoId") UUID dispositivoId);
}
