package com.sath.sistema_sath.domain.solicitud.manual;

public record FuncionarioManualDto(
    String ci,
    String nombre,
    String paterno,
    String materno,
    String celular,
    String correo,
    Integer codigoAdministrativo,
    String nombreUnidad,
    String cargoSolicitante
) {}
