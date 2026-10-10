package com.sath.sistema_sath.domain.permiso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// PermisoUpdateDto.java
@Data
public class PermisoUpdateDto {
    @NotBlank
    private String nombrePermiso;
    private String descripcionPermiso;
}
