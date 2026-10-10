package com.sath.sistema_sath.domain.prioridad;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface PrioridadRepository extends JpaRepository<Prioridad, Long> {

    @Query(value = """
            select p.id_prioridad as idPrioridad,
                   p.nombre_pri   as nivel,          -- Cambiado 'nombrePrio' por 'nivel'
                   p.orden_pri    as ordenPri        -- Cambiado para coincidir con la entidad
            from prioridad p
            where p._estado_aud <> 'ELIMINADO'
              and p._estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listaPrioridad();

}
