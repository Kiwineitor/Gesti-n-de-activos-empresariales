package com.sath.sistema_sath.domain.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioUpdateDto {
    @NotBlank
    private String username;

    private Long idPersona;

    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
}
