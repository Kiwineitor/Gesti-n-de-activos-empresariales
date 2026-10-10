package com.sath.sistema_sath.domain.permiso;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    @Query(value = """
            
             select p.id_permiso     as idPermiso,
                   p.nombre_permiso as nombrePermiso,
                               p.descripcion_permiso as descripcionPermiso
            from permiso p
            where p._estado_aud <> 'ELIMINADO'
              and p._estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listaPermiso();

    Optional<Permiso> findByNombrePermiso(String nombrePermiso);

    boolean existsByNombrePermiso(String nombrePermiso);
}