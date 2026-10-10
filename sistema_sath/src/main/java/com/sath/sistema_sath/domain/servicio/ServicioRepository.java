package com.sath.sistema_sath.domain.servicio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    @Query(value = """
            SELECT s.id_servicio          AS "idServicio",
                   s.nombre_serv          AS "nombreServ",
                   s._estado_aud          AS "estado",
                   ts.id_tipo_servicio    AS "idTipoServicio",
                   ts.nombre_tipo         AS "nombreTipoServicio"
            FROM servicio s
            LEFT JOIN tipo_servicio ts ON s.id_tipo_servicio = ts.id_tipo_servicio
            WHERE s._estado_aud <> 'ELIMINADO'
              AND s._estado_aud IS NOT NULL
            ORDER BY ts.nombre_tipo ASC, s.nombre_serv ASC
            """, nativeQuery = true)
    List<Map<String, Object>> listaServicio();

}
