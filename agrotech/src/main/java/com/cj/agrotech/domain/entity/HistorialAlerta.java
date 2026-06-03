package com.cj.agrotech.domain.entity;

import com.cj.agrotech.domain.enums.PrioridadAlerta;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historial_alertas")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class HistorialAlerta {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadAlerta prioridad;

    @Column(nullable = false)
    private Boolean leida;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dispositivo_id")
    private Dispositivo dispositivo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lote_id")
    private Lote lote;
}
