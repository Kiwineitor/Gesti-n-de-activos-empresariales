package com.sath.sistema_sath.domain.usrPermiso.dto;

public record UsrPermisoResponseDto(
        Long idUsrPermiso,
        Long idUsuario,
        String username,
        Long idPermiso,
        String nombrePermiso,
        String descripcionPermiso,
        String estado
) {}
