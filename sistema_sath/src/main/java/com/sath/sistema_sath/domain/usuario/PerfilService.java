package com.sath.sistema_sath.domain.usuario;

import com.sath.sistema_sath.domain.persona.dto.PersonaResponseDto;
import com.sath.sistema_sath.domain.usuario.dto.CambiarPasswordDto;
import com.sath.sistema_sath.domain.usuario.dto.CambiarUsernameDto;
import com.sath.sistema_sath.domain.usuario.dto.UsuarioResponseDto;

public interface PerfilService {
    UsuarioResponseDto getMiPerfil(String username);
    UsuarioResponseDto cambiarUsername(String usernameActual, CambiarUsernameDto dto);
    void cambiarPassword(String username, CambiarPasswordDto dto);
    PersonaResponseDto getMiPersona(String username);
}