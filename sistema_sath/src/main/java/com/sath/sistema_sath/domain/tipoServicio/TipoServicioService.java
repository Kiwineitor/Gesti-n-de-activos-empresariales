package com.sath.sistema_sath.domain.tipoServicio;

import com.sath.sistema_sath.domain.tipoServicio.dto.TipoServicioCreateDto;
import com.sath.sistema_sath.domain.tipoServicio.dto.TipoServicioResponseDto;

import java.util.List;
import java.util.Map;

public interface TipoServicioService {

    TipoServicioResponseDto create(TipoServicioCreateDto dto);

    TipoServicioResponseDto findById(Long id);

    List<Map<String, Object>> listaTipoServicio();

    TipoServicioResponseDto update(Long id, TipoServicioCreateDto dto);

    void delete(Long id);
}
