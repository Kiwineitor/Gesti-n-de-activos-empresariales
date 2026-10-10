package com.sath.sistema_sath.domain.falla.dto;

import com.sath.sistema_sath.domain.falla.Falla;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FallaMapper {

    @Mapping(target = "idFalla", ignore = true)
    @Mapping(target = "tipoActivo", ignore = true)
    Falla toEntity(FallaCreateDto dto);

    @Mapping(source = "tipoActivo.idTipoActivo", target = "idTipoActivo")
    //Se omite el id para que lo gestione la entidad
    @Mapping(source = "tipoActivo.nombreTipoAct", target = "nombreTipoAct")
        // se llama el nombre de tipoactivo para pasarlo al response
    FallaResponseDto toResponseDto(Falla entity);


    @Mapping(target = "idFalla", ignore = true)
    @Mapping(target = "tipoActivo", ignore = true)
    void updateEntityFromDto(FallaUpdateDto dto, @MappingTarget Falla entity);

    List<FallaResponseDto> toResponseDtoList(List<Falla> entities);
}
