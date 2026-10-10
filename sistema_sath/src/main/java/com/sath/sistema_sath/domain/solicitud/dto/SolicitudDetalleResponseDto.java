package com.sath.sistema_sath.domain.solicitud.dto;

import java.util.List;

public record SolicitudDetalleResponseDto(
        Long idSolicitud,
        String nroSol,
        String nroSolExterna,
        String estadoSol,
        String fechaSol,
        String descripcionSol,
        String observacionFicha,
        String solicitante,
        String celular,
        String correo,
        String unidad,
        String cargo,
        boolean tieneInformeGen,
        Long idInformeGen,
        List<DetalleActivoDto> activos
) {
    public record DetalleActivoDto(
            Long idDetalleSolicitud,
            Long idFallaReportada,
            Long idTipoActivo,
            String codigoActivo,
            String descripcionActivo,
            String tipoActivo,
            String problemaEspecifico,
            String estadoDetalle,
            String prioridad,
            boolean tieneAtencion,
            boolean tieneInformeEsp,
            Long idInformeEsp
    ) {}
}
