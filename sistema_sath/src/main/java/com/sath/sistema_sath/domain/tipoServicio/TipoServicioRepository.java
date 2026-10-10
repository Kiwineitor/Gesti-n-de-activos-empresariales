package com.sath.sistema_sath.domain.tipoServicio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface TipoServicioRepository extends JpaRepository<TipoServicio, Long> {

    @Query(value = """
            SELECT ts.id_tipo_servicio AS "idTipoServicio",
                   ts.nombre_tipo      AS "nombreTipo",
                   ts.descripcion_tipo AS "descripcionTipo",
                   ts._estado_aud      AS "estado"
            FROM tipo_servicio ts
            WHERE ts._estado_aud <> 'ELIMINADO'
              AND ts._estado_aud IS NOT NULL
            ORDER BY ts.nombre_tipo ASC
            """, nativeQuery = true)
    List<Map<String, Object>> listaTipoServicio();
}
