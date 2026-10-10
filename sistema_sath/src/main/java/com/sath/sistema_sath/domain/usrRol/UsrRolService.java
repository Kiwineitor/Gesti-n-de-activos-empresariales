package com.sath.sistema_sath.domain.usrRol;


import com.sath.sistema_sath.domain.rolPermiso.dto.RolPermisoResponseDto;
import com.sath.sistema_sath.domain.usrRol.dto.AsignarRolDto;
import com.sath.sistema_sath.domain.usrRol.dto.UsrRolDto;

import java.util.List;
import java.util.Map;

public interface UsrRolService {
    UsrRolDto asignarRol(AsignarRolDto dto);

    void revocarRol(Long idUsrRol);

    List<Map<String, Object>> listaUsrRol();

    List<UsrRolDto> findRolesByUsuario(Long idUsuario);

    List<RolPermisoResponseDto> findPermisosByUsuario(Long idUsuario);
}
