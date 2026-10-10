package com.sath.sistema_sath.domain.tipoServicio.dto;

public record TipoServicioResponseDto(
        Long   idTipoServicio,
        String nombreTipo,
        String descripcionTipo
) {}
