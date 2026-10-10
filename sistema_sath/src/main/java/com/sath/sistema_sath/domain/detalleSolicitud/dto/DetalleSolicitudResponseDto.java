package com.sath.sistema_sath.domain.detalleSolicitud.dto;

import java.time.LocalDate;

/**
 * DTO for {@link com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitud}
 */
public record DetalleSolicitudResponseDto(
        Long idDetalleSolicitud,
        String estadoDetSol,
        LocalDate fechaActualizacionDetSol,
        Long idActivo,
        Long idSolicitud,
        String codActivo,
        String nroSolicitud
) {}