package com.sath.sistema_sath.domain.InformeEquipoEspecifico.dto;

/**
 * DTO for {@link com.sath.sistema_sath.domain.InformeEquipoEspecifico.InformeEquipoEspecifico}
 */
public record InformeEquipoEspecificoResponseDto(
        Long idInformeEquipoEspecifico,
        int nroInforme,
        String observaciones,
        String trabajoRealizado,
        String detalle,
        Long idDetalleSolicitud
) {
}