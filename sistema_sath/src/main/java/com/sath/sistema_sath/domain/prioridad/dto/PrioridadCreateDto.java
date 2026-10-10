package com.sath.sistema_sath.domain.prioridad.dto;

import com.sath.sistema_sath.domain.prioridad.Prioridad;
import lombok.Value;

/**
 * DTO for {@link Prioridad}
 */
@Value
public class PrioridadCreateDto {
    String nombrePri;
    Integer ordenPri;
}