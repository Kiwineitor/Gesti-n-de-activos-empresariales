package com.sath.sistema_sath.domain.falla.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.sath.sistema_sath.domain.falla.Falla}
 */
@Value
public class FallaUpdateDto  {
    Long idFalla;
    String nombreFall;
    Long idTipoActivo;
}