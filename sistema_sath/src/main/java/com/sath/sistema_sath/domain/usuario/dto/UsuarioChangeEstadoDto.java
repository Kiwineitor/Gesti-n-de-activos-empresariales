package com.sath.sistema_sath.domain.usuario.dto;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioChangeEstadoDto {
    @NotNull
    private EstadoEnum estado; // solo ACTIVO o INACTIVO
}
