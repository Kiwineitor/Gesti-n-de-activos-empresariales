package com.sath.sistema_sath.domain.prioridad.dto;

import com.sath.sistema_sath.domain.prioridad.Prioridad;

/**
 * DTO for {@link Prioridad}
 */
public record PrioridadresponseDto(Long idPrioridad, String nombrePri, Integer ordenPri) {
}