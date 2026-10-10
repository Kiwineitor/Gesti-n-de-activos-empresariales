package com.sath.sistema_sath.domain.atencionSolicitud.dto;

import java.util.List;

public record AtenderDetalleDto(
        Long idDetalleSolicitud,
        Long idFallaReportada,
        List<Long> idsFallas,
        Long idPrioridad,
        List<Long> idsServicios
) {}
