package com.sath.sistema_sath.domain.tipoActivo;


import com.sath.sistema_sath.domain.tipoActivo.dto.TipoActivoCreateDto;
import com.sath.sistema_sath.domain.tipoActivo.dto.TipoActivoResponseDto;

import java.util.List;
import java.util.Map;

public interface TipoActivoService {

    TipoActivoResponseDto create(TipoActivoCreateDto dto);

    List<TipoActivoResponseDto> findAll();

    TipoActivoResponseDto findById(Long id);

    List<Map<String, Object>> listaTipoActivo();

    void delete(Long id);

    TipoActivoResponseDto update(Long id, TipoActivoCreateDto dto);

}
