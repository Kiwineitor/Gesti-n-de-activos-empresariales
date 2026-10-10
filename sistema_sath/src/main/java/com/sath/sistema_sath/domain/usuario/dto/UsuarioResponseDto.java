package com.sath.sistema_sath.domain.usuario.dto;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;

import java.util.List;

public record UsuarioResponseDto(
        Long idUsuario,
        String username,
        Long idPersona,
        String nombrePersona,
        EstadoEnum estado,
        List<String> roles,
        String cargo,
        Long idAdministrativo
) {}