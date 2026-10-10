package com.sath.sistema_sath.domain.activo.dto;

import lombok.Value;

/**
 * DTO for {@link com.sath.sistema_sath.domain.activo.Activo}
 */
@Value
public class ActivoUpdateDto {
    Long idActivo;
    String codigoAct;
    String descripcionAct;
    Long idTipoActivo;
}