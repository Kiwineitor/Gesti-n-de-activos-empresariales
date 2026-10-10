package com.sath.sistema_sath.domain.tecnicoAsignado.dto;

import lombok.Data;

@Data
public class TecnicoAsignadoUpdateDto {

    private long idTecnicoAsignado;
    private Long idAdministrativo;
    private Long idUsuario;
    private Long idSolicitud;

}
