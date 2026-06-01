package com.cj.agrotech.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequestDTO(
        @NotBlank String currentPassword,
        @NotBlank String newPassword
) {
}
