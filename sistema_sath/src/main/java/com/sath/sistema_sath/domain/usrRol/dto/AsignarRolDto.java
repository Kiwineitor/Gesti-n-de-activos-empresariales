package com.sath.sistema_sath.domain.usrRol.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignarRolDto {
    @NotNull
    private Long idUsuario;
    @NotNull
    private Long idRol;
}
