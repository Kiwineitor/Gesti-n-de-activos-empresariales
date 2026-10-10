package com.sath.sistema_sath.domain.persona.dto;

public record PersonaResponseDto(
        Long idPersona,
        String ciPer,
        String nombrePer,
        String apPaternoPer,
        String apMaternoPer,
        String nombreCompleto,
        String nroCellPer,
        String correoPer
) {}
