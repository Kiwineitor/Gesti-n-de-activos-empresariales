package com.sath.sistema_sath.domain.rol;


import com.sath.sistema_sath.domain.rol.dto.RolCreateDto;
import com.sath.sistema_sath.domain.rol.dto.RolResponseDto;
import com.sath.sistema_sath.domain.rol.dto.RolUpdateDto;

import java.util.List;
import java.util.Map;

public interface RolService {

    RolResponseDto create(RolCreateDto dto);

    RolResponseDto update(Long id, RolUpdateDto dto);

    List<RolResponseDto> findAll();

    RolResponseDto findById(Long id);

    List<Map<String, Object>> listaRol();

    void delete(Long id);

    boolean tieneRol(String tieneRol);
}
