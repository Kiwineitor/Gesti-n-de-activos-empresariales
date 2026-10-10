package com.sath.sistema_sath.domain.tipoActivo.dto;

import java.io.Serializable;

/**
 * DTO for {@link com.sath.sistema_sath.domain.tipoActivo.TipoActivo}
 */
public record TipoActivoResponseDto(Long idTipoActivo,
                                    String nombreTipoAct
)  {
}