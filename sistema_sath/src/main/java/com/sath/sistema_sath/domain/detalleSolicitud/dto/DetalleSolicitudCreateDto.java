package com.sath.sistema_sath.domain.detalleSolicitud.dto;

import com.sath.sistema_sath.domain.detalleSolicitud.Enum.TipoInformeEnum;
import lombok.Value;

import java.time.LocalDate;

/**
 * DTO for {@link com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitud}
 */
@Value
public class DetalleSolicitudCreateDto {
    String estadoDetSol;
    LocalDate fechaActualizacionDetSol;
    Long idActivo;
    Long idSolicitud;

    TipoInformeEnum tipoInforme;
}