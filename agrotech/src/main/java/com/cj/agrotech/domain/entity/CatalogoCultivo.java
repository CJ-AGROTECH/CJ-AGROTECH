package com.cj.agrotech.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "catalogo_cultivos")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CatalogoCultivo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nombre; // Papa, Flores, Café

    private String variedad;
    private String descripcion;
    private Integer diasCrecimiento;
    private Double tempOptimaMin;
    private Double tempOptimaMax;
    private Double humedadOptimaMin;
    private Double humedadOptimaMax;
}