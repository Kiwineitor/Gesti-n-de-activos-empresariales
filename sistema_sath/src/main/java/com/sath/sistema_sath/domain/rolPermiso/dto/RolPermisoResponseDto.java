package com.sath.sistema_sath.domain.rolPermiso.dto;


public record RolPermisoResponseDto(
        Long idRolPermiso,
        Long idRol,
        String nombreRol,
        Long idPermiso,
        String nombrePermiso
) {}
