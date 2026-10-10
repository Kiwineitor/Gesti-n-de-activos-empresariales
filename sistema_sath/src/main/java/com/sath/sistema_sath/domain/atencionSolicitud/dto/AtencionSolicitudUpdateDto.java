package com.sath.sistema_sath.domain.atencionSolicitud.dto;

import com.sath.sistema_sath.domain.atencionSolicitud.Enum.EstadoAtencion;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AtencionSolicitudUpdateDto {

    private Long idAtencionSolicitud;

    private LocalDate fechIniReparacionAtenSol;

    private LocalDate fechFinReparacionAtenSol;

    private EstadoAtencion estadoReparacionAtenSol;

    private List<Long> idsServicios;

    private Long idPrioridad;

    private Long idDetalleSolicitud;

}
