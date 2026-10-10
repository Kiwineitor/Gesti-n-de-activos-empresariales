package com.sath.sistema_sath.domain.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioCompletoCreateDto {

    // ── Persona ──────────────────────────────────────────────────
    @NotBlank(message = "El CI es obligatorio")
    private String ciPer;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombrePer;

    @NotBlank(message = "El apellido paterno es obligatorio")
    private String apPaternoPer;

    @NotBlank(message = "El apellido materno es obligatorio")
    private String apMaternoPer;

    private String nroCellPer;
    private String correoPer;

    // ── Cuenta ───────────────────────────────────────────────────
    @NotBlank(message = "El username es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    // ── Rol ──────────────────────────────────────────────────────
    @NotNull(message = "Debe seleccionar un rol")
    private Long idRol;

    // ── Puesto (opcional) ─────────────────────────────────────────
    private Integer codigoAdmi;
    private Long idCargo;
    private Long idUnidad;
}
