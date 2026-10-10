package com.sath.sistema_sath.domain.prioridad.dto;

import com.sath.sistema_sath.domain.prioridad.Enum.PrioridadEnum;

/**
 * DTO for {@link com.sath.sistema_sath.domain.prioridad.Prioridad}
 */
public record PrioridadEstadoDto(
        PrioridadEnum nivel) {
}