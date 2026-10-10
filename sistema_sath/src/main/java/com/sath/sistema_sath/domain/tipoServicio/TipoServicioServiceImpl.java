package com.sath.sistema_sath.domain.tipoServicio;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.tipoServicio.dto.TipoServicioCreateDto;
import com.sath.sistema_sath.domain.tipoServicio.dto.TipoServicioMapper;
import com.sath.sistema_sath.domain.tipoServicio.dto.TipoServicioResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TipoServicioServiceImpl implements TipoServicioService {

    private final TipoServicioRepository repo;
    private final TipoServicioMapper mapper;

    @Override
    @CacheEvict(value = "tipoServicio", allEntries = true)
    public TipoServicioResponseDto create(TipoServicioCreateDto dto) {
        return mapper.toResponseDto(repo.save(mapper.toEntity(dto)));
    }

    @Override
    public TipoServicioResponseDto findById(Long id) {
        return mapper.toResponseDto(repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de servicio no encontrado con ID: " + id)));
    }

    @Override
    @Cacheable("tipoServicio")
    public List<Map<String, Object>> listaTipoServicio() {
        return repo.listaTipoServicio();
    }

    @Override
    @CacheEvict(value = "tipoServicio", allEntries = true)
    public TipoServicioResponseDto update(Long id, TipoServicioCreateDto dto) {
        TipoServicio entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de servicio no encontrado con ID: " + id));
        entity.setNombreTipo(dto.getNombreTipo());
        entity.setDescripcionTipo(dto.getDescripcionTipo());
        return mapper.toResponseDto(repo.save(entity));
    }

    @Override
    @CacheEvict(value = "tipoServicio", allEntries = true)
    public void delete(Long id) {
        TipoServicio entity = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de servicio no encontrado con ID: " + id));
        entity.setEstado(EstadoEnum.ELIMINADO);
        repo.save(entity);
    }
}
