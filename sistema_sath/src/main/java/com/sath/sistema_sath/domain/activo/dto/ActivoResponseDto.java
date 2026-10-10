package com.sath.sistema_sath.domain.activo.dto;

/**
 * DTO for {@link com.sath.sistema_sath.domain.activo.Activo}
 */
public record ActivoResponseDto(
        Long idActivo,
        String codigoAct,
        String descripcionAct,
        Long idTipoActivo
) {
}