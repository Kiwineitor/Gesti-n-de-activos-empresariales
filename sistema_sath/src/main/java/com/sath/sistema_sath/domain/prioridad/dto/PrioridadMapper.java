package com.sath.sistema_sath.domain.prioridad.dto;

import com.sath.sistema_sath.domain.prioridad.Prioridad;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PrioridadMapper {


    Prioridad toEntity(PrioridadCreateDto dto);

    @Mapping(source = "nivel", target = "nombrePri")
    PrioridadresponseDto toResponseDto(Prioridad entity);



    @Mapping(target = "idPrioridad", ignore = true)
    Prioridad updateFromDto(PrioridadUpdateDto dto, @MappingTarget Prioridad entity);

    List<PrioridadresponseDto> toResponseDtoList(List<Prioridad> entities);

}

