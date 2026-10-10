package com.sath.sistema_sath.domain.unidad;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.unidad.dto.UnidadCreateDto;
import com.sath.sistema_sath.domain.unidad.dto.UnidadMapper;
import com.sath.sistema_sath.domain.unidad.dto.UnidadResponseDto;
import com.sath.sistema_sath.domain.unidad.dto.UnidadUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnidadServiceImpl implements UnidadService {

    private final UnidadRepository repo;
    private final UnidadMapper mapper;


    @Override
    @CacheEvict(value = "unidad", allEntries = true)
    public UnidadResponseDto create(UnidadCreateDto dto) {
        Unidad entity = mapper.toEntity(dto);
        return mapper.toResponseDto(repo.save(entity));
    }

    @Override
    public List<UnidadResponseDto> findAll() {
        return mapper.toResponseDtoList(repo.findAll());
    }

    @Override
    public UnidadResponseDto findById(Long id) {
        Unidad entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrado con ID: " + id));
        return mapper.toResponseDto(entity);
    }

    @Override
    @Cacheable("unidad")
    public List<Map<String, Object>> listaUnidad() {
        return repo.listaUnidad();
    }

    @Override
    @CacheEvict(value = "unidad", allEntries = true)
    public UnidadResponseDto update(Long id, UnidadUpdateDto dto) {
        Unidad entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada con ID: " + id));
        log.info("Actualizando unidad id={} '{}' -> '{}'", id, entity.getNombreUni(), dto.getNombreUni());
        entity.setNombreUni(dto.getNombreUni());
        return mapper.toResponseDto(repo.save(entity));
    }

    @Override
    @CacheEvict(value = "unidad", allEntries = true)
    public void delete(Long id) {
        Unidad entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrado con ID: " + id));
        entity.setEstado(EstadoEnum.ELIMINADO);
        repo.save(entity);
    }
}
