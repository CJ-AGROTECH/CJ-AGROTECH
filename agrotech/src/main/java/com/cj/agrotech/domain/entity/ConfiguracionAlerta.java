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
    @Column(nullable = false)
    private CondicionAlerta condicion;

    @Column(nullable = false)
    private Double umbral;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadAlerta prioridad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;
}
