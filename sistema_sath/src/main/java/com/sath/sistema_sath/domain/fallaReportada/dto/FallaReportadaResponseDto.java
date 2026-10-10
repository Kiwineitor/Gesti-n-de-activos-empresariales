package com.sath.sistema_sath.domain.fallaReportada.dto;

/**
 * DTO for {@link com.sath.sistema_sath.domain.fallaReportada.FallaReportada}
 */
public record FallaReportadaResponseDto(
        Long idFallaReportada,
        String descripcionFallRep,
        String nombreFalla,
        Long idDetalleSolicitud
) {
}