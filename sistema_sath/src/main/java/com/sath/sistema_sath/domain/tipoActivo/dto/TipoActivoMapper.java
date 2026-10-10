package com.sath.sistema_sath.domain.tipoActivo.dto;

import com.sath.sistema_sath.domain.tipoActivo.TipoActivo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipoActivoMapper {

    @Mapping(target = "idTipoActivo", ignore = true)
    TipoActivo toEntity(TipoActivoCreateDto dto);

    TipoActivo updateEntityFronDto(TipoActivoUpdateDto Dto,@MappingTarget TipoActivo entity);

    TipoActivoResponseDto toResponseDto(TipoActivo entity);

    List<TipoActivoResponseDto> toResponseDtoList(List<TipoActivo> entities);
}
