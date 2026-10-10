package com.sath.sistema_sath.domain.atencionSolicitud.dto;

import com.sath.sistema_sath.domain.atencionSolicitud.Enum.EstadoAtencion;

import java.time.LocalDate;
import java.util.List;

public record AtencionSolicitudResponseDto(
        long idAtencionSolicitud,
        LocalDate fechIniReparacionAtenSol,
        LocalDate fechFinReparacionAtenSol,
        EstadoAtencion estadoReparacionAtenSol,
        List<Long> idsServicios,
        Long idPrioridad,
        Long idDetalleSolicitud
) {
}
