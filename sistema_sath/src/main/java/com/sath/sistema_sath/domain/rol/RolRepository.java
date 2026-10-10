package com.sath.sistema_sath.domain.rol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {

    @Query(value = """
            
             select r.id_rol     as idRol,
                   r.nombre_rol as nombreRol,
                         r.descripcion_rol as descripcionRol
            from rol r
            where r._estado_aud <> 'ELIMINADO'
              and r._estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listaRol();

   Optional<Rol> findByNombreRol(String nombreRol);



   @Query(value = """

           select
                case
                    when rol.nombre_rol = 'ADMINISTRADOR' then true
                    else false
                end as tiene_rol
           from rol
           where rol.nombre_rol = :tieneRol
            """, nativeQuery = true)
    boolean tieneRol(@Param("tieneRol") String tieneRol);

}
