package com.sath.sistema_sath.domain.InformeEquipoGeneral;

import com.sath.sistema_sath.domain.InformeEquipoGeneral.dto.InformeEquipoGeneralCreateDto;
import com.sath.sistema_sath.domain.InformeEquipoGeneral.dto.InformeEquipoGeneralResponseDto;

import java.util.List;
import java.util.Map;

public interface InformeEquipoGeneralService {

    InformeEquipoGeneralResponseDto create(InformeEquipoGeneralCreateDto dto);

    InformeEquipoGeneralResponseDto findById(Long id);

    List<InformeEquipoGeneralResponseDto> findAll();

    List<Map<String, Object>> listaInformeEquipoGeneral();

}
