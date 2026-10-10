package com.sath.sistema_sath.Config.security.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank String username,
        @NotBlank String password
) {}