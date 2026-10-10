package com.sath.sistema_sath.domain.permiso.dto;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;

public record PermisoResponseDto(
        Long idPermiso,
        String nombrePermiso,
        String descripcionPermiso,
        EstadoEnum estado
) {}
