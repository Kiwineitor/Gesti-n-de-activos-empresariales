package com.sath.sistema_sath.domain.solicitud.manual;

public record ActivoManualDto(
    String codigoActivo,
    String descripcionActivo,
    String tipoActivo,
    String problemaEspecifico
) {}
