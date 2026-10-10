package com.sath.sistema_sath.domain.administrativo.dto;


public record AdministrativoResponseDto(
        Long idAdministrativo,
        int codigoAdmi,

        Long idPersona,
        String nombreCompleto,
        String nroCellPer,
        String correoPer,
        Long idUnidad,
        String nombreUni,
        Long idCargo,
        String nombreCargo
) {
}