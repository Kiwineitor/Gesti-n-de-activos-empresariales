package com.sath.sistema_sath.domain.InformeEquipoEspecifico;

import com.sath.sistema_sath.domain.InformeEquipoEspecifico.dto.InformeEquipoEspecificoCreateDto;
import com.sath.sistema_sath.domain.InformeEquipoEspecifico.dto.InformeEquipoEspecificoResponseDto;

import java.util.List;
import java.util.Map;

public interface InformeEquipoEspecificoService {

    InformeEquipoEspecificoResponseDto create(InformeEquipoEspecificoCreateDto dto);

    InformeEquipoEspecificoResponseDto findById(Long id);

    List<InformeEquipoEspecificoResponseDto> findAll();

    List<Map<String, Object>> listaInformeEquipoEspecifico();

}
