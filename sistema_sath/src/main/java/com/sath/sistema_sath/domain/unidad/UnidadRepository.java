package com.sath.sistema_sath.domain.unidad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UnidadRepository extends JpaRepository<Unidad, Long> {

    @Query(value = """
            
             select u.id_unidad      as "idUnidad",
                   u.nombre_uni     as "nombreUni"
            from unidad u
            where u._estado_aud <> 'ELIMINADO'
              and u._estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listaUnidad();

    Optional<Unidad> findByNombreUni(String nombre);

    Optional<Unidad> findFirstByNombreUniContainsIgnoreCaseAndNombreUniContainsIgnoreCase(String parteA, String parteB);

    Optional<Unidad> findFirstByNombreUniContainsIgnoreCase(String parte);
}
