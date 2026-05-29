package com.cj.agrotech.dto;

public record FincaRequestDTO(
        String nombre,
        String municipio,
        Double latitud,
        Double longitud
) {}
