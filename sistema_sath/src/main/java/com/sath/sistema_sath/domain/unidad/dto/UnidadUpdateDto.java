package com.sath.sistema_sath.domain.unidad.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.sath.sistema_sath.domain.unidad.Unidad}
 */
@Value
public class UnidadUpdateDto  {
    Long idUnidad;
    String nombreUni;

}