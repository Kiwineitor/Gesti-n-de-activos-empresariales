package com.sath.sistema_sath.domain.falla.dto;

import java.io.Serializable;

/**
 * DTO for {@link com.sath.sistema_sath.domain.falla.Falla}
 */
public record FallaResponseDto(Long idFalla,
                               String nombreFall,
                               String nombreTipoAct,
                               Long idTipoActivo
)  {
}