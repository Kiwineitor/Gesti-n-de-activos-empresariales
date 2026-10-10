package com.sath.sistema_sath.domain.usrRol.dto;

import com.sath.sistema_sath.domain.usrRol.UsrRol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsrRolMapper {
    @Mapping(source = "usuario.idUsuario", target = "usuarioId")
    @Mapping(source = "rol.idRol", target = "rolId")
    UsrRolDto toDto(UsrRol entity);

    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "rol", ignore = true)
    UsrRol toEntity(UsrRolDto dto);

    List<UsrRolDto> toDtoList(List<UsrRol> entities);
}
