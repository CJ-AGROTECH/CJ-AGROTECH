package com.cj.agrotech.domain.entity;

import com.cj.agrotech.domain.enums.CondicionAlerta;
import com.cj.agrotech.domain.enums.PrioridadAlerta;
import com.cj.agrotech.domain.enums.VariableSensor;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Table(name = "configuracion_alertas")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConfiguracionAlerta {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VariableSensor variable;

    @Enumerated(EnumType.STRING)
    @Column
    private CondicionAlerta condicion;

    @Column
    private Double umbral;

    @Column(name = "umbral_min")
    private Double umbralMin;

    @Column(name = "umbral_max")
    private Double umbralMax;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadAlerta prioridad;

    @Column(length = 500)
    private String mensaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_id")
    private Dispositivo dispositivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Lote lote;
}
