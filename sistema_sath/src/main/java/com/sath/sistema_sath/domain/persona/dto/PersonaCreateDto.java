package com.sath.sistema_sath.domain.persona.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PersonaCreateDto {

    private Long idAup;
    @NotBlank(message = "El campo ci es obligatorio")
    private String ciPer;
    @NotBlank(message = "El campo nombre es obligatorio")
    private String nombrePer;
    @NotBlank(message = "El campo apellido paterno es obligatorio")
    private String apPaternoPer;
    @NotBlank(message = "El campo apellido materno es obligatorio")
    private String apMaternoPer;
    @NotBlank(message = "El campo celular es obligatorio")
    private String nroCellPer;
    @NotBlank(message = "El campo correo es obligatorio")
    private String correoPer;


}
