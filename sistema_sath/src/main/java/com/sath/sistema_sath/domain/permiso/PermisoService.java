package com.sath.sistema_sath.domain.permiso;

import com.sath.sistema_sath.domain.permiso.dto.PermisoCreateDto;
import com.sath.sistema_sath.domain.permiso.dto.PermisoResponseDto;
import com.sath.sistema_sath.domain.permiso.dto.PermisoUpdateDto;

import java.util.List;
import java.util.Map;

public interface PermisoService {
    PermisoResponseDto create(PermisoCreateDto dto);

    PermisoResponseDto update(Long id, PermisoUpdateDto dto);

    List<PermisoResponseDto> findAll();

    PermisoResponseDto findById(Long id);

    List<Map<String, Object>> listaPermiso();

    void delete(Long id);
}
