package com.sath.sistema_sath.domain.usuario;


import com.sath.sistema_sath.domain.usuario.dto.*;

import java.util.List;
import java.util.Map;

public interface UsuarioService {

    UsuarioResponseDto create(UsuarioCreateDto dto);

    UsuarioResponseDto crearCompleto(UsuarioCompletoCreateDto dto);

    UsuarioResponseDto update(Long id, UsuarioUpdateDto dto);

    UsuarioResponseDto changeEstado(Long id, UsuarioChangeEstadoDto dto);

    UsuarioResponseDto toggleEstado(Long id);

    List<UsuarioResponseDto> findAll();

    UsuarioResponseDto findById(Long id);

    void delete(Long id);

    List<Map<String, Object>> usuariosConAdministrativo();

    void resetPassword(Long id, ResetPasswordAdminDto dto);
}
