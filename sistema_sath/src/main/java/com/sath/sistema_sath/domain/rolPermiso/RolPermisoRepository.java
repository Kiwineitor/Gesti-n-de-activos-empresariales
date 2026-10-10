package com.sath.sistema_sath.domain.rolPermiso;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, Long> {

    @Query(value = """
            
             select rp.id_rol_permiso as idRolPermiso,
                   rp.id_rol        as idRol,
                   rp.id_permiso    as idPermiso
            from rol_permiso rp
            where rp._estado_aud <> 'ELIMINADO'
              and rp._estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listaRolPermiso();

    @Query("SELECT rp FROM RolPermiso rp WHERE rp.rol.idRol = :idRol AND rp.estado != :estado")
    List<RolPermiso> findActiveByRolId(@Param("idRol") Long idRol, @Param("estado") EstadoEnum estado);

    Optional<RolPermiso> findByRol_IdRolAndPermiso_IdPermiso(Long idRol, Long idPermiso);

    boolean existsByRol_IdRolAndPermiso_IdPermisoAndEstadoNot(Long idRol, Long idPermiso, EstadoEnum estado);
}
