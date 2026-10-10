package com.sath.sistema_sath.domain.persona;

import com.sath.sistema_sath.domain.persona.dto.PersonaCreateDto;
import com.sath.sistema_sath.domain.persona.dto.PersonaResponseDto;
import com.sath.sistema_sath.domain.persona.dto.PersonaUpdateDto;

import java.util.List;
import java.util.Map;

public interface PersonaService {

    PersonaResponseDto create(PersonaCreateDto dto);

    List<PersonaResponseDto> findAll();

    PersonaResponseDto findById(Long id);

    PersonaResponseDto update(Long id, PersonaUpdateDto dto);

    List<Map<String, Object>> listaPersona();

    void delete(Long id);
}
