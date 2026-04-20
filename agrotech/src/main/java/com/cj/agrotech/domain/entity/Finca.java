package com.cj.agrotech.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "fincas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Finca {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String nombre;

    private String municipio;
    private String vereda;

    private Double latitud;
    private Double longitud;

    @OneToMany(mappedBy = "finca", cascade = CascadeType.ALL)
    private List<Lote> lotes;
}
