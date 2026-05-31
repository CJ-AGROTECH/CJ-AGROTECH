package com.cj.agrotech.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String nombre; 

    private String variedad;
    private String descripcion;
    private Integer diasCrecimiento;
    private Double tempOptimaMin;
    private Double tempOptimaMax;
    private Double humedadOptimaMin;
    private Double humedadOptimaMax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;
}