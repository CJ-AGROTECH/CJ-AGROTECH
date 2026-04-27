package com.cj.agrotech.dto;

import com.cj.agrotech.domain.enums.Rol;

public record SignupRequestDTO(String nombre, String email, String password, Rol rol) {}
