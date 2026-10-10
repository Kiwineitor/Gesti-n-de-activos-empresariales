package com.sath.sistema_sath.domain.rol.dto;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;

public record RolResponseDto(
        Long idRol,
        String nombreRol,
        String descripcionRol,
        EstadoEnum estado
) {}
