package com.cj.agrotech.domain.entity;

import com.cj.agrotech.domain.enums.PrioridadAlerta;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "configuracion_alertas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionAlerta {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    private String variable; // Ej: "temp_aire", "hum_suelo"
    private Float min;
    private Float max;

    @Enumerated(EnumType.STRING)
    private PrioridadAlerta prioridad;
}
