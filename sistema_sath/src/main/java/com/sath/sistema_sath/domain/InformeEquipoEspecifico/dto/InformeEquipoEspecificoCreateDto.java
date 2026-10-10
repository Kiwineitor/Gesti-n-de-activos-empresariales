package com.sath.sistema_sath.domain.InformeEquipoEspecifico.dto;

import lombok.Value;

/**
 * DTO for {@link com.sath.sistema_sath.domain.InformeEquipoEspecifico.InformeEquipoEspecifico}
 */
@Value
public class InformeEquipoEspecificoCreateDto {
    int nroInforme;
    String observaciones;
    String trabajoRealizado;
    String detalle;
    Long idDetalleSolicitud;
}