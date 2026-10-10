package com.sath.sistema_sath.domain.usuario.dto;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import com.sath.sistema_sath.domain.usuario.Usuario;
import com.sath.sistema_sath.domain.usrRol.UsrRol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsuarioMapper {

    @Mapping(target = "persona", ignore = true)
    Usuario toEntity(UsuarioCreateDto dto);

    @Mapping(source = "persona.idPersona", target = "idPersona")
    @Mapping(source = "persona.nombreCompleto", target = "nombrePersona")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "cargo", ignore = true)
    UsuarioResponseDto toResponseDto(Usuario entity);

    // toResponseDtoList is NOT used directly — service calls enrichWithRoles per element
    List<UsuarioResponseDto> toResponseDtoList(List<Usuario> entities);

    default List<String> extractRoles(List<UsrRol> usrRoles) {
        if (usrRoles == null) return List.of();
        return usrRoles.stream()
                .filter(ur -> ur.getEstado() != EstadoEnum.ELIMINADO)
                .map(ur -> ur.getRol().getNombreRol())
                .collect(Collectors.toList());
    }
}
