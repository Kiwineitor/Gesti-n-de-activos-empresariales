package com.sath.sistema_sath.domain.falla;

import com.sath.sistema_sath.domain.falla.dto.FallaCreateDto;
import com.sath.sistema_sath.domain.falla.dto.FallaResponseDto;
import com.sath.sistema_sath.domain.falla.dto.FallaUpdateDto;

import java.util.List;
import java.util.Map;

public interface FallaService {

    FallaResponseDto create(FallaCreateDto dto);

    List<FallaResponseDto> findAll();

    FallaResponseDto findById(Long id);

    FallaResponseDto update(Long id, FallaUpdateDto dto);

    void delete(Long id);

    List<Map<String, Object>> listaParaElFrontendFalla();

    List<FallaResponseDto> findByTipoActivo(Long idTipoActivo);
}
