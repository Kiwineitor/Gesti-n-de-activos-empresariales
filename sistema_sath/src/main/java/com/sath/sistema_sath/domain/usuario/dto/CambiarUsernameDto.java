package com.sath.sistema_sath.domain.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CambiarUsernameDto {

    @NotBlank(message = "El nombre de usuario es requerido")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String usernameNuevo;
}