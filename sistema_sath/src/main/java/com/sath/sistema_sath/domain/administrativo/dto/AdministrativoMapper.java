package com.sath.sistema_sath.domain.administrativo.dto;

import com.sath.sistema_sath.domain.administrativo.Administrativo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdministrativoMapper {

    @Mapping(target = "persona", ignore = true)
    @Mapping(target = "unidad", ignore = true)
    @Mapping(target = "cargo", ignore = true)
    @Mapping(target = "idAdministrativo", ignore = true)
    Administrativo toEntity(AdministrativoCreateDto dto);


    @Mapping(target = "idAdministrativo", ignore = true)
    @Mapping(target = "persona", ignore = true)
    @Mapping(target = "unidad", ignore = true)
    @Mapping(target = "cargo", ignore = true)
    void updateEntityFromDto(AdministrativoUpdateDto dto, @MappingTarget Administrativo entity);

    @Mapping(source = "persona.idPersona", target = "idPersona")
    @Mapping(source = "persona.nombreCompleto", target = "nombreCompleto")
    @Mapping(source = "persona.nroCellPer", target = "nroCellPer")
    @Mapping(source = "persona.correoPer", target = "correoPer")
    @Mapping(source = "unidad.idUnidad", target = "idUnidad")
    @Mapping(source = "unidad.nombreUni", target = "nombreUni")
    @Mapping(source = "cargo.idCargo", target = "idCargo")
    @Mapping(source = "cargo.nombreCar", target = "nombreCargo")
    AdministrativoResponseDto toResponseDto(Administrativo entity);

    List<AdministrativoResponseDto> toResponseDtoList(List<Administrativo> entity);

}
