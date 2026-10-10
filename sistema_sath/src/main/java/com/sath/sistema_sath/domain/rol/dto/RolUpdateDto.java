package com.sath.sistema_sath.domain.rol.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// RolUpdateDto.java
@Data
public class RolUpdateDto {
    @NotBlank
    private String nombreRol;
    private String descripcionRol;
}
