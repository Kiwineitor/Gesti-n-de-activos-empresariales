package com.sath.sistema_sath.domain.persona;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.persona.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements PersonaService {

    private final PersonaMapper mapper;
    private final PersonaRepository repo;


    @Override
    public PersonaResponseDto create(PersonaCreateDto dto) {
        Persona entity = mapper.toEntity(dto);
        return mapper.toResponseDto(repo.save(entity));
    }

    @Override
    public List<PersonaResponseDto> findAll() {
        return mapper.toResponseDtoList(repo.findAll());
    }

    @Override
    public PersonaResponseDto findById(Long id) {
        Persona entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con ID: " + id));

        return mapper.toResponseDto(entity);
    }

    @Override
    public PersonaResponseDto update(Long id, PersonaUpdateDto dto) {
        Persona entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con ID: " + id));
        mapper.updateEntityFromDto(dto, entity);
        return mapper.toResponseDto(repo.save(entity));
    }

    @Override
    public List<Map<String, Object>> listaPersona() {
        return repo.listaPersona();
    }

    @Override
    public void delete(Long id) {
        Persona entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con ID: " + id));
        entity.setEstado(EstadoEnum.ELIMINADO);
        repo.save(entity);

    }
}
