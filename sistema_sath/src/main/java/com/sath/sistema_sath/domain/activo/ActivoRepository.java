package com.sath.sistema_sath.domain.activo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ActivoRepository extends JpaRepository<Activo, Long> {

    @Query(value = """
               SELECT id_activo       as idActivo,
                      id_tipo_activo  as idTipoActivo,
                       codigo_act      as codicoAct,
                      descripcion_act as descripcionAct
               from activo a
               where a._estado_aud <> 'ELIMINADO'
                 and a._estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listarActivosParaFrontend();

    Optional<Activo> findByCodigoAct(String codigoAct);

    @Query(value = """
            SELECT
                a.codigo_act                                                        AS "codigoActivo",
                a.descripcion_act                                                   AS "descripcionActivo",
                ta_tip.nombre_tipo_act                                              AS "tipoActivo",
                s.id_solicitud                                                      AS "idSolicitud",
                s.nro_sol                                                           AS "nroSolicitud",
                s.fecha_sol                                                         AS "fechaSolicitud",
                s.estado_sol                                                        AS "estadoSolicitud",
                s.descripcion_sol                                                   AS "descripcionSolicitud",
                TRIM(CONCAT(p_sol.nombre_per, ' ', p_sol.ap_paterno_per,
                    CASE WHEN p_sol.ap_materno_per IS NOT NULL AND p_sol.ap_materno_per <> ''
                         THEN CONCAT(' ', p_sol.ap_materno_per) ELSE '' END))       AS "solicitante",
                (SELECT TRIM(CONCAT(p2.nombre_per, ' ', p2.ap_paterno_per))
                 FROM tecnico_asignado ta2
                 JOIN administrativo adm2 ON adm2.id_administrativo = ta2.id_administrativo
                 JOIN persona p2 ON p2.id_persona = adm2.id_persona
                 WHERE ta2.id_solicitud = s.id_solicitud
                   AND ta2._estado_aud <> 'ELIMINADO'
                 LIMIT 1)                                                           AS "tecnico",
                (SELECT fr2.descripcion_fall_rep
                 FROM falla_reportada fr2
                 WHERE fr2.id_detalle_solicitud = ds.id_detalle_solicitud
                   AND fr2._estado_aud <> 'ELIMINADO'
                 LIMIT 1)                                                           AS "fallaReportada",
                (SELECT aten2.fecha_ini_reparacion_aten_sol
                 FROM atencion_solicitud aten2
                 WHERE aten2.id_detalle_solicitud = ds.id_detalle_solicitud
                   AND aten2._estado_aud <> 'ELIMINADO'
                 LIMIT 1)                                                           AS "fechaInicioReparacion",
                (SELECT aten3.fecha_fin_reparacion_aten_sol
                 FROM atencion_solicitud aten3
                 WHERE aten3.id_detalle_solicitud = ds.id_detalle_solicitud
                   AND aten3._estado_aud <> 'ELIMINADO'
                 LIMIT 1)                                                           AS "fechaFinReparacion",
                (SELECT aten4.estado_reparacion_aten_sol
                 FROM atencion_solicitud aten4
                 WHERE aten4.id_detalle_solicitud = ds.id_detalle_solicitud
                   AND aten4._estado_aud <> 'ELIMINADO'
                 LIMIT 1)                                                           AS "estadoReparacion"
            FROM activo a
            LEFT JOIN tipo_activo ta_tip ON ta_tip.id_tipo_activo = a.id_tipo_activo
            JOIN detalle_solicitud ds ON ds.id_activo = a.id_activo
                AND ds._estado_aud <> 'ELIMINADO'
            JOIN solicitud s ON s.id_solicitud = ds.id_solicitud
                AND s._estado_aud <> 'ELIMINADO'
            LEFT JOIN administrativo adm_sol ON adm_sol.id_administrativo = s.id_administrativo
            LEFT JOIN persona p_sol ON p_sol.id_persona = adm_sol.id_persona
            WHERE a.codigo_act = :codigoActivo
              AND a._estado_aud <> 'ELIMINADO'
            ORDER BY s.fecha_sol DESC, s.id_solicitud DESC
            """, nativeQuery = true)
    List<Map<String, Object>> historialPorCodigoActivo(@Param("codigoActivo") String codigoActivo);
}
