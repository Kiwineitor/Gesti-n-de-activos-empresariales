package com.sath.sistema_sath.domain.permiso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// PermisoCreateDto.java  (ya lo tienes, solo agrega @Size si quieres)
@Data
public class PermisoCreateDto {
    @NotBlank
    private String nombrePermiso;
    private String descripcionPermiso;
}