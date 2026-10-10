package com.sath.sistema_sath.domain.tipoActivo.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.sath.sistema_sath.domain.tipoActivo.TipoActivo}
 */
@Value
public class TipoActivoUpdateDto  {
    Long idTipoActivo;
    String nombreTipoAct;
}