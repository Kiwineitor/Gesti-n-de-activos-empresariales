package com.sath.sistema_sath.domain.rolPermiso;

import com.sath.sistema_sath.domain.rolPermiso.dto.AsignarPermisoDto;
import com.sath.sistema_sath.domain.rolPermiso.dto.RolPermisoResponseDto;

import java.util.List;
import java.util.Map;

public interface RolPermisoService {

    RolPermisoResponseDto asignarPermiso(AsignarPermisoDto dto);

    void revocarPermiso(Long idRolPermiso);

    List<Map<String, Object>> listaRolPermiso();

    List<RolPermisoResponseDto> findPermisosByRol(Long idRol);
}
