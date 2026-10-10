package com.sath.sistema_sath.domain.usrPermiso.dto;

import jakarta.validation.constraints.NotNull;

public record AsignarPermisoUsuarioDto(
        @NotNull Long idUsuario,
        @NotNull Long idPermiso
) {}
