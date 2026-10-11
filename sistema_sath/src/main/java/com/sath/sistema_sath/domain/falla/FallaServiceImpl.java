package com.sath.sistema_sath.domain.falla;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.falla.dto.FallaCreateDto;
import com.sath.sistema_sath.domain.falla.dto.FallaMapper;
import com.sath.sistema_sath.domain.falla.dto.FallaResponseDto;
import com.sath.sistema_sath.domain.falla.dto.FallaUpdateDto;
import com.sath.sistema_sath.domain.tipoActivo.TipoActivo;
import com.sath.sistema_sath.domain.tipoActivo.TipoActivoRepository;
import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FallaServiceImpl implements FallaService {

    private final TipoActivoRepository tipoActivoRepository;
    private final FallaMapper mapper;
    private final FallaRepository fallaRepository;

    @Override
    @Transactional
    public FallaResponseDto create(FallaCreateDto dto) {
        Falla entity = mapper.toEntity(dto);

        if (dto.getIdTipoActivo() != null) {
            entity.setTipoActivo(tipoActivoRepository.findById(dto.getIdTipoActivo())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de recurso no encontrado con ID: " + dto.getIdTipoActivo())));
        }

        return mapper.toResponseDto(fallaRepository.save(entity));
    }

    @Override
    public List<FallaResponseDto> findAll() {
        return mapper.toResponseDtoList(fallaRepository.findAll());
    }

    @Override
    public FallaResponseDto findById(Long id) {
        Falla entity = fallaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Falla no encontrado con ID: " + id));
        return mapper.toResponseDto(entity);
    }

    @Override
    @Transactional
    public FallaResponseDto update(Long id, FallaUpdateDto dto) {
        // 1. Buscar la falla existente usando el id del path (REST convention)
        Falla entity = fallaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Falla no encontrada con ID: " + id));
        log.info("Actualizando falla id={}", id);

        // 2. Actualizar campos básicos (nombre, descripción)
        mapper.updateEntityFromDto(dto, entity);

        // 3. Manejar el cambio de TipoActivo manualmente
        if (dto.getIdTipoActivo() != null) {
            TipoActivo nuevoTipo = tipoActivoRepository.findById(dto.getIdTipoActivo())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de recurso no encontrado: " + dto.getIdTipoActivo()));
            entity.setTipoActivo(nuevoTipo);
        }

        // 4. Guardar y retornar
        return mapper.toResponseDto(fallaRepository.save(entity));
    }


    @Override
    public void delete(Long id) {
        Falla entity = fallaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Falla no encontrado con ID: " + id));
        entity.setEstado(EstadoEnum.ELIMINADO);
        fallaRepository.save(entity);

    }

    @Override
    public List<Map<String, Object>> listaParaElFrontendFalla() {
        return fallaRepository.listaParaElFrontendFalla();
    }

    @Override
    public List<FallaResponseDto> findByTipoActivo(Long idTipoActivo) {
        return mapper.toResponseDtoList(
                fallaRepository.findByTipoActivo_IdTipoActivoAndEstadoNot(idTipoActivo, EstadoEnum.ELIMINADO));
    }
}
