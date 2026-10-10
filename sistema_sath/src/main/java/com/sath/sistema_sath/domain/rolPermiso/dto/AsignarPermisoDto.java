package com.sath.sistema_sath.domain.rolPermiso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignarPermisoDto {
    @NotNull
    private Long idRol;
    @NotNull
    private Long idPermiso;
}