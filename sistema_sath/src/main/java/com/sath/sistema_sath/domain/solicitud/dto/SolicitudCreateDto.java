package com.sath.sistema_sath.domain.solicitud.dto;

import lombok.Value;

import java.time.LocalDate;

/**
 * DTO for {@link com.sath.sistema_sath.domain.solicitud.Solicitud}
 */
@Value
public class SolicitudCreateDto {
    Integer nroSol;
    int gestionSol;
    LocalDate fechaSol;
    String descripcionSol;
    String nroSolExterna;
}