package com.sath.sistema_sath.domain.falla.dto;

import lombok.Value;

/**
 * DTO for {@link com.sath.sistema_sath.domain.falla.Falla}
 */
@Value
public class FallaCreateDto  {

    String nombreFall;



    Long idTipoActivo;

}