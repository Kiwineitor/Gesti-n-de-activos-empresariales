package com.sath.sistema_sath.domain.solicitud;

import com.sath.sistema_sath.domain.solicitud.Enum.EstadoSolicitudEnum;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudResponseDto;

public record EstadoCambiadoEvent(SolicitudResponseDto dto, EstadoSolicitudEnum nuevoEstado) {}
