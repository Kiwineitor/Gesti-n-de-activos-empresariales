package com.sath.sistema_sath.domain.servicio.dto;

import lombok.Data;

@Data
public class ServicioCreateDto {

    String nombreServ;
    String observacionesServ;
    Long   idTipoServicio;
}
