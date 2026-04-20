package com.cj.agrotech.domain.entity;

import com.cj.agrotech.domain.enums.EstadoDispositivo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dispositivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispositivo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @Column(unique = true, nullable = false)
    private String macAddress;

    @Enumerated(EnumType.STRING)
    private EstadoDispositivo estado;

    private LocalDateTime ultimaSincronizacion;
}
