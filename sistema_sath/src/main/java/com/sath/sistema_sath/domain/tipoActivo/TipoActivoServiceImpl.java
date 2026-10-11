package com.sath.sistema_sath.domain.tipoActivo;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.tipoActivo.dto.TipoActivoCreateDto;
import com.sath.sistema_sath.domain.tipoActivo.dto.TipoActivoMapper;
import com.sath.sistema_sath.domain.tipoActivo.dto.TipoActivoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TipoActivoServiceImpl implements TipoActivoService {

    private final TipoActivoRepository tipoActivoRepository;
    private final TipoActivoMapper mapper;


    @Override
    @CacheEvict(value = "tipoActivo", allEntries = true)
    public TipoActivoResponseDto create(TipoActivoCreateDto dto) {
        TipoActivo entity = mapper.toEntity(dto);
        return mapper.toResponseDto(tipoActivoRepository.save(entity));
    }

    @Override
    public List<TipoActivoResponseDto> findAll() {
        return mapper.toResponseDtoList(tipoActivoRepository.findAll());
    }

    @Override
    public TipoActivoResponseDto findById(Long id) {
        TipoActivo entity = tipoActivoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Tipo de recurso no encontrado con ID: " + id));

        return mapper.toResponseDto(entity);
    }

    @Override
    @Cacheable("tipoActivo")
    public List<Map<String, Object>> listaTipoActivo() {
        return tipoActivoRepository.listaTipoActivo();
    }

    @Override
    @CacheEvict(value = "tipoActivo", allEntries = true)
    public void delete(Long id) {
        TipoActivo entity = tipoActivoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Tipo de recurso no encontrado con ID: " + id));
        entity.setEstado(EstadoEnum.ELIMINADO);
        tipoActivoRepository.save(entity);

    }

    @Override
    @CacheEvict(value = "tipoActivo", allEntries = true)
    public TipoActivoResponseDto update(Long id, TipoActivoCreateDto dto) {
        TipoActivo entity = tipoActivoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Tipo de recurso no encontrado con ID: " + id));

        entity.setNombreTipoAct(dto.getNombreTipoAct());

        return mapper.toResponseDto(tipoActivoRepository.save(entity));
    }
}
