package com.cj.agrotech.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "catalogo_cultivos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoCultivo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nombreComun;

    private String nombreCientifico;
    private Float tempMinOptima;
    private Float tempMaxOptima;
    private Float humedadSueloMin;
}
