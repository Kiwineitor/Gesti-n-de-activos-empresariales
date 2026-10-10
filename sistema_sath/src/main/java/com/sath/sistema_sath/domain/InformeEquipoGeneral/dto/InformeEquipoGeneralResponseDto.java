package com.sath.sistema_sath.domain.InformeEquipoGeneral.dto;

import com.sath.sistema_sath.domain.InformeEquipoGeneral.InformeEquipoGeneral;

import java.time.LocalDate;

/**
 * DTO for {@link InformeEquipoGeneral}
 */
public record InformeEquipoGeneralResponseDto(
        Long idInformeEquipoGeneral,
        int nroInforme,
        String observaciones,
        String ResumenTecnico,
        LocalDate fechaCierreInforme,
        Long idSolicitud,
        Integer nroSolicitud
) {
}