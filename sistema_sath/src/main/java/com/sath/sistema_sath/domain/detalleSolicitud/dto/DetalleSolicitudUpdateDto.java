package com.sath.sistema_sath.domain.detalleSolicitud.dto;

import lombok.Value;

import java.time.LocalDate;

/**
 * DTO for {@link com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitud}
 */
@Value
public class DetalleSolicitudUpdateDto {
    Long idDetalleSolicitud;
    String estadoDetSol;
    LocalDate fechaActualizacionDetSol;
    Long idActivo;
    Long idSolicitud;
}