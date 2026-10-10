package com.sath.sistema_sath.domain.tecnicoAsignado.dto;

import lombok.Data;

import java.util.List;

@Data
public class TecnicoAsignadoCreateMasivoDto {

    private Long idUsuario;
    private List<Long> idsSolicitud;

}
