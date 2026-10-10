package com.sath.sistema_sath.domain.atencionSolicitud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface AtencionSolicitudRepository extends JpaRepository<AtencionSolicitud, Long> {
@Query(value = """
        select ats.id_atencion_solicitud         as idAtencionSolicitud,
               ats.id_detalle_solicitud          as idDetalleSolicitud,
               ats.id_prioridad                  as idPrioridad,
               (select asv.id_servicio
                from atencion_servicio asv
                where asv.id_atencion_solicitud = ats.id_atencion_solicitud
                  and asv._estado_aud <> 'ELIMINADO'
                order by asv.id_atencion_servicio
                limit 1)                         as idServicio,
               ats.fecha_ini_reparacion_aten_sol as fechaIniReparacionAtenSol,
               ats.fecha_fin_reparacion_aten_sol as fechaFinReparacionAtenSol,
               ats.estado_reparacion_aten_sol    as estadoReparacionAtenSol
        from atencion_solicitud ats
        where _estado_aud <> 'ELIMINADO'
          and _estado_aud is not null
        """, nativeQuery = true)
    List<Map<String,Object>> listaParaElFrontend();

    @Query(value = """
            SELECT DISTINCT ON (s.id_solicitud)
                   aten.id_atencion_solicitud AS "idAtencionSolicitud",
                   s.nro_sol                 AS "nroSolicitud",
                   s.estado_sol              AS "estadoSol",
                   s.mant_id_londra          AS "mantIdLondra"
            FROM atencion_solicitud aten
            INNER JOIN detalle_solicitud ds ON aten.id_detalle_solicitud = ds.id_detalle_solicitud
                                           AND ds._estado_aud <> 'ELIMINADO'
            INNER JOIN solicitud s          ON ds.id_solicitud = s.id_solicitud
                                           AND s._estado_aud  <> 'ELIMINADO'
            WHERE aten._estado_aud <> 'ELIMINADO'
              AND aten._estado_aud IS NOT NULL
            ORDER BY s.id_solicitud, aten.id_atencion_solicitud
            """, nativeQuery = true)
    List<Map<String, Object>> listaParaAsignar();

    @Modifying
    @Query(value = """
            UPDATE atencion_solicitud ats
            SET fecha_fin_reparacion_aten_sol = CURRENT_DATE
            FROM detalle_solicitud ds
            WHERE ats.id_detalle_solicitud = ds.id_detalle_solicitud
              AND ds.id_solicitud = :idSolicitud
              AND ats._estado_aud <> 'ELIMINADO'
              AND ats.fecha_fin_reparacion_aten_sol IS NULL
            """, nativeQuery = true)
    void cerrarFechaFinPorSolicitud(@Param("idSolicitud") Long idSolicitud);

    @Query(value = """
            SELECT p.nombre_pri FROM prioridad p
            JOIN atencion_solicitud ats ON ats.id_prioridad = p.id_prioridad
            JOIN detalle_solicitud ds ON ds.id_detalle_solicitud = ats.id_detalle_solicitud
            WHERE ds.id_solicitud = :idSolicitud
              AND ats._estado_aud <> 'ELIMINADO'
            LIMIT 1
            """, nativeQuery = true)
    String findPrioridadNivelBySolicitudId(@Param("idSolicitud") Long idSolicitud);
}
