package com.sath.sistema_sath.domain.tecnicoAsignado;

import com.sath.sistema_sath.domain.tecnicoAsignado.dto.TecnicoAsignadoCreateDto;
import com.sath.sistema_sath.domain.tecnicoAsignado.dto.TecnicoAsignadoCreateMasivoDto;
import com.sath.sistema_sath.domain.tecnicoAsignado.dto.TecnicoAsignadoResponseDto;
import com.sath.sistema_sath.domain.tecnicoAsignado.dto.TecnicoAsignadoUpdateDto;

import java.util.List;
import java.util.Map;

public interface TecnicoAsignadoService {

    TecnicoAsignadoResponseDto create(TecnicoAsignadoCreateDto dto);

    List<TecnicoAsignadoResponseDto> createMasivo(TecnicoAsignadoCreateMasivoDto dto);

    List<TecnicoAsignadoResponseDto> findAll();

    TecnicoAsignadoResponseDto findById(Long id);

    List<Map<String, Object>> listaTecnicoAsignado();

    TecnicoAsignadoResponseDto update(Long id, TecnicoAsignadoUpdateDto dto);

    void delete(Long id);

    TecnicoAsignadoResponseDto tomar(String username, Long idSolicitud);

}
