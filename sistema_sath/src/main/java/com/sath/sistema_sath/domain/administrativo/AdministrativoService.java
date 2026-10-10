package com.sath.sistema_sath.domain.administrativo;

import com.sath.sistema_sath.domain.administrativo.dto.AdministrativoCreateDto;
import com.sath.sistema_sath.domain.administrativo.dto.AdministrativoResponseDto;
import com.sath.sistema_sath.domain.administrativo.dto.AdministrativoUpdateDto;

import java.util.List;
import java.util.Map;


public interface AdministrativoService {

    AdministrativoResponseDto create (AdministrativoCreateDto dto);

    List<AdministrativoResponseDto> findAll();

    AdministrativoResponseDto findById(Long id);

    AdministrativoResponseDto update(Long id, AdministrativoUpdateDto dto);

    void delete(Long id);

    List<Map<String, Object>> listaParaElFrontend();

}
