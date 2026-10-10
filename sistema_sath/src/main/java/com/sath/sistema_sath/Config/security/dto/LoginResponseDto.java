package com.sath.sistema_sath.Config.security.dto;

import java.util.List;

public record LoginResponseDto(
        String token,
        String username,
        String nombreCompleto,
        List<String> roles,
        String cargo,
        List<String> permisos
) {}
