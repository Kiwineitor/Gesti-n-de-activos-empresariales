package com.sath.sistema_sath.domain.solicitud.dto;

import com.sath.sistema_sath.domain.solicitud.Solicitud;
import lombok.Value;

/**
 * DTO for {@link Solicitud}
 */
@Value
public class SolicitudUpdateDto {
    Long idSolicitud;
    String estadoSol;
}