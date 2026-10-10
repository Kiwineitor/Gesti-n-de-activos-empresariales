package com.sath.sistema_sath.domain.prioridad;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.prioridad.dto.*;
import com.sath.sistema_sath.domain.prioridad.Enum.PrioridadEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PrioridadServiceImpl implements PrioridadService {


    private final PrioridadRepository repo;
    private final PrioridadMapper mapper;


    @Override
    @CacheEvict(value = "prioridad", allEntries = true)
    public PrioridadresponseDto create(PrioridadCreateDto dto) {
        Prioridad entity = mapper.toEntity(dto);
        entity.setNivel(PrioridadEnum.BAJO);
        return mapper.toResponseDto(repo.save(entity));
    }

    @Override
    @Cacheable("prioridad")
    public List<PrioridadresponseDto> findAll() {
        return mapper.toResponseDtoList(repo.findAll());
    }

    @Override
    public PrioridadresponseDto findById(Long id) {
        Prioridad entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Prioridad no encontrado con ID: " + id));

        return mapper.toResponseDto(entity);
    }

    @Override
    @CacheEvict(value = "prioridad", allEntries = true)
    public PrioridadresponseDto update(Long id, PrioridadUpdateDto dto) {
        Prioridad entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Prioridad no encontrado con ID: " + id));
        mapper.updateFromDto(dto, entity);
        return mapper.toResponseDto(repo.save(entity));
    }

    @Override
    @CacheEvict(value = "prioridad", allEntries = true)
    public void delete(Long id) {
        Prioridad entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Prioridad no encontrado con ID: " + id));
        entity.setEstado(EstadoEnum.ELIMINADO);
        repo.save(entity);
    }

    @Override
    @Cacheable("prioridad")
    public List<Map<String, Object>> listaPrioridad() {
        return repo.listaPrioridad();
    }

    @Override
    @CacheEvict(value = "prioridad", allEntries = true)
    public void cambiarEstado(long id, PrioridadEstadoDto dto) {
        Prioridad entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Prioridad no encontrado"));
        entity.setNivel(dto.nivel());
        repo.save(entity);
    }
}

