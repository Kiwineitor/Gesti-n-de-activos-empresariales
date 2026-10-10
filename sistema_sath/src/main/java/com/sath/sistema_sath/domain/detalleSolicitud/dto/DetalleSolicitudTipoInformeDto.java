package com.sath.sistema_sath.domain.detalleSolicitud.dto;

import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitud;
import com.sath.sistema_sath.domain.detalleSolicitud.Enum.TipoInformeEnum;

/**
 * DTO for {@link DetalleSolicitud}
 */
public record DetalleSolicitudTipoInformeDto(TipoInformeEnum tipoInforme) {
}