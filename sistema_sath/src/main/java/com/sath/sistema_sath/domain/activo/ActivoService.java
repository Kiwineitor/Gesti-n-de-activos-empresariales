package com.sath.sistema_sath.domain.activo;

import com.sath.sistema_sath.domain.activo.dto.ActivoCreateDto;
import com.sath.sistema_sath.domain.activo.dto.ActivoResponseDto;
import com.sath.sistema_sath.domain.activo.dto.ActivoUpdateDto;

import java.util.List;
import java.util.Map;

public interface ActivoService {

    ActivoResponseDto create(ActivoCreateDto dto);

    List<ActivoResponseDto> findAll();

    ActivoResponseDto findById(Long id);

    ActivoResponseDto update(Long id, ActivoUpdateDto dto);

    void delete(Long id);

    List<Map<String, Object>> listarActivosParaFrontend();

    List<Map<String, Object>> historialPorCodigoActivo(String codigoActivo);
}
