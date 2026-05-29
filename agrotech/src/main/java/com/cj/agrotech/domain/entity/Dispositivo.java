package com.cj.agrotech.domain.entity;

import com.cj.agrotech.domain.enums.EstadoDispositivo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dispositivos")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Dispositivo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String macAddress;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDispositivo estado;

    @Column(name = "ultima_sincronizacion")
    private LocalDateTime ultimaSincronizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @OneToMany(mappedBy = "dispositivo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ConfiguracionAlerta> configuracionesAlerta = new ArrayList<>();

    @OneToMany(mappedBy = "dispositivo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<HistorialAlerta> historialAlertas = new ArrayList<>();
}
