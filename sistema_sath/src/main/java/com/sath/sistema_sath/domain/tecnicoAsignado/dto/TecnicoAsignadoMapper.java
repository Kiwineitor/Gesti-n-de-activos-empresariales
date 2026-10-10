package com.sath.sistema_sath.domain.tecnicoAsignado.dto;

import com.sath.sistema_sath.domain.persona.Persona;
import com.sath.sistema_sath.domain.tecnicoAsignado.TecnicoAsignado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TecnicoAsignadoMapper {

    @Mapping(target = "administrativo", ignore = true)
    @Mapping(target = "solicitud", ignore = true)
    @Mapping(target = "idTecnicoAsignado", ignore = true)
    TecnicoAsignado toEntity(TecnicoAsignadoCreateDto dto);

    @Mapping(target = "idTecnicoAsignado", ignore = true)
    @Mapping(target = "administrativo", ignore = true)
    @Mapping(target = "solicitud", ignore = true)
    TecnicoAsignado updateEntityFromDto(TecnicoAsignadoUpdateDto dto, @MappingTarget TecnicoAsignado entity);

    @Mapping(source = "administrativo.idAdministrativo", target = "idAdministrativo")
    @Mapping(source = "administrativo.persona", target = "nombreCompleto")
    @Mapping(source = "solicitud.idSolicitud", target = "idSolicitud")
    TecnicoAsignadoResponseDto toResponseDto(TecnicoAsignado entity);

    List<TecnicoAsignadoResponseDto> toResponseDtoList(List<TecnicoAsignado> entities);

    default String personaToNombreCompleto(Persona entity) {
        return entity != null ? entity.getNombreCompleto() : null;
    }

}
