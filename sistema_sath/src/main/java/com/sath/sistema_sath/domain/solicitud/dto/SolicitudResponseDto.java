package com.sath.sistema_sath.domain.solicitud.dto;

import com.sath.sistema_sath.domain.solicitud.Solicitud;

import java.time.LocalDate;

/**
 * DTO for {@link Solicitud}
 */
public record SolicitudResponseDto(Long idSolicitud,
        String estadoSol,
        Long nroSol,
        int gestionSol,
        LocalDate fechaSol,
        String descripcionSol,
        String observacionFicha,
        Integer idExterno,
        String nroSolExterna,
        int posicionCola) {
}
