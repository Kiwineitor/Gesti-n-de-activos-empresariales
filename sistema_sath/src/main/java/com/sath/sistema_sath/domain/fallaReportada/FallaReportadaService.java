package com.sath.sistema_sath.domain.fallaReportada;


import com.sath.sistema_sath.domain.fallaReportada.dto.FallaReportadaCreateDto;
import com.sath.sistema_sath.domain.fallaReportada.dto.FallaReportadaResponseDto;
import com.sath.sistema_sath.domain.fallaReportada.dto.FallaReportadaUpdateDto;

import java.util.List;
import java.util.Map;

public interface FallaReportadaService {

    FallaReportadaResponseDto create(FallaReportadaCreateDto dto);

    List<FallaReportadaResponseDto> findAll();

    FallaReportadaResponseDto findById(Long idFallaReportada);

    FallaReportadaResponseDto update(Long idFallaReportada, FallaReportadaUpdateDto dto);

    void delete(Long idFallaReportada);

    List<Map<String, Object>> listaFallaReportada();

    FallaReportadaResponseDto catalogarFalla(Long idFallaReportada, Long idFalla);
}
