package com.sath.sistema_sath.domain.notificacion.dto;

public record NotificacionDto(
        Long id,
        String tipo,
        String titulo,
        String descripcion,
        boolean leida,
        String tiempo
) {}