package com.sath.sistema_sath.domain.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CambiarPasswordDto {

    @NotBlank(message = "La contraseña actual es requerida")
    private String passwordActual;

    @NotBlank
    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
    private String passwordNueva;

    @NotBlank(message = "Debes confirmar la nueva contraseña")
    private String confirmarPassword;
}