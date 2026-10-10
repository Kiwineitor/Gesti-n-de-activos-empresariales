package com.sath.sistema_sath.domain.servicio.dto;

public record ServicioResponseDto(
        Long   idServicio,
        String nombreServ,
        String observacionesServ,
        Long   idTipoServicio,
        String nombreTipoServicio
) {}
