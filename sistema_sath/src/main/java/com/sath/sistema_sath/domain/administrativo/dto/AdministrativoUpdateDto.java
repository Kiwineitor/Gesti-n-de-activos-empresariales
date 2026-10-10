package com.sath.sistema_sath.domain.administrativo.dto;

import lombok.Data;

@Data
public class AdministrativoUpdateDto {

    private Long idAdministrativo;

    private int codigoAdmi;

    private Long idPersona;
    private Long idUnidad;
    private Long idCargo;
    
}
