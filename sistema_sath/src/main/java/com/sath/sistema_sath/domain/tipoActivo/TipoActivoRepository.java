package com.sath.sistema_sath.domain.tipoActivo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TipoActivoRepository extends JpaRepository<TipoActivo, Long> {

    @Query(value = """
            
             select t.id_tipo_activo   as "idTipoActivo",
                   t.nombre_tipo_act  as "nombreTipoAct"
            from tipo_activo t
            where t._estado_aud IS NULL OR t._estado_aud <> 'ELIMINADO'
            """, nativeQuery = true)
    List<Map<String, Object>> listaTipoActivo();

    Optional<TipoActivo> findByNombreTipoAct(String nombreTipoAct);
}
