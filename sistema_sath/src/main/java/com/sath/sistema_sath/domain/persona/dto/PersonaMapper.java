package com.sath.sistema_sath.domain.persona.dto;

import com.sath.sistema_sath.domain.persona.Persona;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonaMapper {

    @Mapping(target = "idPersona", ignore = true)
    Persona toEntity(PersonaCreateDto dto);

    @Mapping(target = "idPersona", ignore = true)
    void updateEntityFromDto( PersonaUpdateDto Dto, @MappingTarget Persona entity);

    @Mapping(target = "nombreCompleto", expression = "java(entity.getNombrePer() + ' ' + entity.getApPaternoPer() + ' ' + entity.getApMaternoPer())")
    PersonaResponseDto toResponseDto(Persona entity);


    List<PersonaResponseDto> toResponseDtoList(List<Persona> entities);
}
