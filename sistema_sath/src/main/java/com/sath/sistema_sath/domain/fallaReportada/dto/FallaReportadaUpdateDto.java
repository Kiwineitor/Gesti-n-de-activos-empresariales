package com.sath.sistema_sath.domain.fallaReportada.dto;

import lombok.Value;

/**
 * DTO for {@link com.sath.sistema_sath.domain.fallaReportada.FallaReportada}
 */
@Value
public class FallaReportadaUpdateDto {
    Long idFallaReportada;
    String descripcionFallRep;
    Long idDetalleSolicitud;
    Long idFalla;
}