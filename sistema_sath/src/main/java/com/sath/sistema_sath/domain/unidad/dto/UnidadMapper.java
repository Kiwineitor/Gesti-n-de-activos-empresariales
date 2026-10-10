package com.sath.sistema_sath.domain.unidad.dto;

import com.sath.sistema_sath.domain.unidad.Unidad;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UnidadMapper {


    Unidad toEntity(UnidadCreateDto dto);

    List<UnidadResponseDto> toDtoList(List<Unidad> entities);

    UnidadResponseDto toResponseDto(Unidad entity);

    List<UnidadResponseDto> toResponseDtoList(List<Unidad> entities);
}
