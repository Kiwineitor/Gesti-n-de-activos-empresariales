package com.sath.sistema_sath.domain.InformeEquipoGeneral.dto;

import lombok.Value;

import java.time.LocalDate;

/**
 * DTO for {@link com.sath.sistema_sath.domain.InformeEquipoGeneral.InformeEquipoGeneral}
 */
@Value
public class InformeEquipoGeneralCreateDto {
    int nroInforme;
    String observaciones;
    String ResumenTecnico;
    LocalDate fechaCierreInforme;
    Long idSolicitud;
}