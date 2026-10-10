package com.sath.sistema_sath.domain.tecnicoAsignado.dto;

import java.time.LocalDate;

public record TecnicoAsignadoResponseDto(
        long idTecnicoAsignado,
        LocalDate fechaAsignacionTecAsig,
        Long idAdministrativo,
        Long idSolicitud,
        String nombreCompleto
) {
}
