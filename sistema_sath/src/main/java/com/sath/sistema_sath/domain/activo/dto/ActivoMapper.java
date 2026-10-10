package com.sath.sistema_sath.domain.activo.dto;

import com.sath.sistema_sath.domain.activo.Activo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActivoMapper {

    @Mapping(target = "tipoActivo.idTipoActivo", ignore = true)
    @Mapping(target = "idActivo", ignore = true)
    Activo toEntity(ActivoCreateDto dto);

    @Mapping(target = "idActivo", ignore = true)
    @Mapping(target = "tipoActivo", ignore = true)
    Activo updateFromDto(ActivoUpdateDto Dto, @MappingTarget Activo entity);

    @Mapping(source = "tipoActivo.idTipoActivo", target = "idTipoActivo")
    ActivoResponseDto toResponseDto(Activo entity);

    List<ActivoResponseDto> toResponseDtoList(List<Activo> entites);


}
