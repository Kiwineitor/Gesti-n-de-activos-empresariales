package com.sath.sistema_sath.domain.unidad;

import com.sath.sistema_sath.domain.unidad.dto.UnidadCreateDto;
import com.sath.sistema_sath.domain.unidad.dto.UnidadResponseDto;
import com.sath.sistema_sath.domain.unidad.dto.UnidadUpdateDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface UnidadService {
    UnidadResponseDto create (UnidadCreateDto dto);

    List<UnidadResponseDto> findAll();

    UnidadResponseDto findById(Long id);

    List<Map<String, Object>> listaUnidad();

    UnidadResponseDto update(Long id, UnidadUpdateDto dto);

    void delete(Long id);
}
