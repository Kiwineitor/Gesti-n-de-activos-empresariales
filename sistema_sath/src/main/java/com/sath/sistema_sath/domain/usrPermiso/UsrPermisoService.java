package com.sath.sistema_sath.domain.usrPermiso;

import com.sath.sistema_sath.domain.usrPermiso.dto.AsignarPermisoUsuarioDto;
import com.sath.sistema_sath.domain.usrPermiso.dto.UsrPermisoResponseDto;

import java.util.List;

public interface UsrPermisoService {

    UsrPermisoResponseDto asignar(AsignarPermisoUsuarioDto dto);

    void revocar(Long idUsrPermiso);

    List<UsrPermisoResponseDto> findByUsuario(Long idUsuario);
}
