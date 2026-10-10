package com.sath.sistema_sath.domain.InformeEquipoGeneral.dto;

import com.sath.sistema_sath.domain.InformeEquipoGeneral.InformeEquipoGeneral;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InformeEquipoGeneralMapper {

    @Mapping(target = "solicitud", ignore = true)
    @Mapping(target = "idInformeEquipoGeneral", ignore = true)
    @Mapping(target = "nroInforme", ignore = true)
    @Mapping(target = "fechaCierreInformeGnr", ignore = true)
    InformeEquipoGeneral toEntity(InformeEquipoGeneralCreateDto dto);

    @Mapping(source = "solicitud.idSolicitud", target = "idSolicitud")
    @Mapping(source = "solicitud.nroSol", target = "nroSolicitud")
    InformeEquipoGeneralResponseDto toResponseDto(InformeEquipoGeneral entity);

    List<InformeEquipoGeneralResponseDto> toResponseDtoList(List<InformeEquipoGeneral> entities);
}
