package com.sath.sistema_sath.domain.rol.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;

@Data
public class RolCreateDto {
    @NotBlank
    private String nombreRol;
    private String descripcionRol;
}
