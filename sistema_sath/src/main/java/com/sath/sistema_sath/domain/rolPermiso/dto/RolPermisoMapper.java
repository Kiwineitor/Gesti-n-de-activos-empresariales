package  com.sath.sistema_sath.domain.rolPermiso.dto;

import com.sath.sistema_sath.domain.rolPermiso.RolPermiso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RolPermisoMapper {

    @Mapping(source = "rol.idRol", target = "idRol")
    @Mapping(source = "rol.nombreRol", target = "nombreRol")
    @Mapping(source = "permiso.idPermiso", target = "idPermiso")
    @Mapping(source = "permiso.nombrePermiso", target = "nombrePermiso")
    RolPermisoResponseDto toResponseDto(RolPermiso entity);

    List<RolPermisoResponseDto> toResponseDtoList(List<RolPermiso> entities);
}