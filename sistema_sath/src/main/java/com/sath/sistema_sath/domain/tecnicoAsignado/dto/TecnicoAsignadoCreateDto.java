package com.sath.sistema_sath.domain.tecnicoAsignado.dto;

import lombok.Data;

@Data
public class TecnicoAsignadoCreateDto {

    private Long idAdministrativo;
    private Long idUsuario;
    private Long idSolicitud;

}
