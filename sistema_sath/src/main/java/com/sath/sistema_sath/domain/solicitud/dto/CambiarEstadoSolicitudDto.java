package com.sath.sistema_sath.domain.solicitud.dto;

import com.sath.sistema_sath.domain.solicitud.Enum.EstadoSolicitudEnum;

public record CambiarEstadoSolicitudDto(
        EstadoSolicitudEnum estado,
        String observacion
) {
}
