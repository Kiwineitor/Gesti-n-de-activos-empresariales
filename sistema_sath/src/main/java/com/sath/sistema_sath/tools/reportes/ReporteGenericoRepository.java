package com.sath.sistema_sath.tools.reportes;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ReporteGenericoRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public List<String> obtenerServiciosPorActivo(Long nroSolicitud, String codigoActivo) {
        String sql = """
                SELECT DISTINCT s.nombre_serv
                FROM public.solicitud so
                JOIN public.detalle_solicitud ds 
                     ON so.id_solicitud = ds.id_solicitud
                JOIN public.atencion_solicitud aten 
                     ON ds.id_detalle_solicitud = aten.id_detalle_solicitud
                     AND aten._estado_aud <> 'ELIMINADO'
                JOIN public.atencion_servicio asv
                     ON aten.id_atencion_solicitud = asv.id_atencion_solicitud
                     AND asv._estado_aud <> 'ELIMINADO'
                JOIN public.servicio s 
                     ON asv.id_servicio = s.id_servicio
                JOIN public.activo a 
                     ON ds.id_activo = a.id_activo
                WHERE so.nro_sol = :nroSolicitud
                  AND a.codigo_act = :codigoActivo
                  AND ds._estado_aud <> 'ELIMINADO'
                """;

        Map<String, Object> params = Map.of(
                "nroSolicitud", nroSolicitud,
                "codigoActivo", codigoActivo
        );

        return namedParameterJdbcTemplate.queryForList(sql, params, String.class);
    }

    public List<Map<String, Object>> listaAtenciones() {

        String sql = """
                SELECT DISTINCT ON (dt.id_detalle_solicitud)
                
                    dt.id_detalle_solicitud AS "idDetalleSolicitud",
                    s.id_solicitud AS "idSolicitud",
                    s.nro_sol AS "nroSolicitud",
                
                    a.codigo_act AS "codigoActivo",
                    a.descripcion_act AS "descripcionActivo",
                
                    t.nombre_tipo_act AS "tipoActivo",
                    t.id_tipo_activo AS "idTipoActivo",
                
                    (
                        SELECT fr2.descripcion_fall_rep
                        FROM falla_reportada fr2
                        WHERE fr2.id_detalle_solicitud = dt.id_detalle_solicitud
                          AND fr2._estado_aud <> 'ELIMINADO'
                        ORDER BY fr2.id_falla_reportada
                        LIMIT 1
                    ) AS "problemaReportado",
                
                    (
                        SELECT fr2.id_falla_reportada
                        FROM falla_reportada fr2
                        WHERE fr2.id_detalle_solicitud = dt.id_detalle_solicitud
                          AND fr2._estado_aud <> 'ELIMINADO'
                        ORDER BY fr2.id_falla_reportada
                        LIMIT 1
                    ) AS "idFallaReportada",
                
                    COALESCE(dt.estado_det_sol::text, 'PENDIENTE') AS "estadoDetalle",
                
                    p.nombre_pri AS "prioridad",
                    p.orden_pri AS "ordenPrioridad",
                
                    (
                        SELECT STRING_AGG(f2.nombre_fall, ', ')
                        FROM falla_reportada fr2
                        LEFT JOIN falla f2
                            ON fr2.id_falla = f2.id_falla
                        WHERE fr2.id_detalle_solicitud = dt.id_detalle_solicitud
                          AND fr2._estado_aud <> 'ELIMINADO'
                          AND f2.id_falla IS NOT NULL
                    ) AS "fallaCatalogada",
                
                    (
                        SELECT STRING_AGG(srv2.nombre_serv, ', ')
                        FROM atencion_servicio asv2
                        JOIN servicio srv2 ON asv2.id_servicio = srv2.id_servicio
                        WHERE asv2.id_atencion_solicitud = aten.id_atencion_solicitud
                          AND asv2._estado_aud <> 'ELIMINADO'
                    ) AS "servicio",
                
                    aten.id_atencion_solicitud AS "idAtencion",
                    aten.id_prioridad AS "idPrioridad",
                    (
                        SELECT STRING_AGG(asv2.id_servicio::text, ',')
                        FROM atencion_servicio asv2
                        WHERE asv2.id_atencion_solicitud = aten.id_atencion_solicitud
                          AND asv2._estado_aud <> 'ELIMINADO'
                    ) AS "idsServicios"
                
                FROM detalle_solicitud dt
                
                INNER JOIN solicitud s
                    ON dt.id_solicitud = s.id_solicitud
                
                INNER JOIN activo a
                    ON dt.id_activo = a.id_activo
                
                LEFT JOIN tipo_activo t
                    ON a.id_tipo_activo = t.id_tipo_activo
                
                LEFT JOIN atencion_solicitud aten
                    ON dt.id_detalle_solicitud = aten.id_detalle_solicitud
                   AND aten._estado_aud <> 'ELIMINADO'
                
                LEFT JOIN prioridad p
                    ON aten.id_prioridad = p.id_prioridad
                
                WHERE dt._estado_aud <> 'ELIMINADO'
                  AND s._estado_aud <> 'ELIMINADO'
                
                ORDER BY dt.id_detalle_solicitud,
                         COALESCE(p.orden_pri, 999)
                """;

        return namedParameterJdbcTemplate.queryForList(sql, Map.of());
    }

    public List<Map<String, Object>> listaAtencionesPorTecnico(String username) {

        String sql = """
                SELECT DISTINCT ON (dt.id_detalle_solicitud)
                
                    dt.id_detalle_solicitud AS "idDetalleSolicitud",
                    s.id_solicitud AS "idSolicitud",
                    s.nro_sol AS "nroSolicitud",
                
                    a.codigo_act AS "codigoActivo",
                    a.descripcion_act AS "descripcionActivo",
                
                    t.nombre_tipo_act AS "tipoActivo",
                    t.id_tipo_activo AS "idTipoActivo",
                
                    (
                        SELECT fr2.descripcion_fall_rep
                        FROM falla_reportada fr2
                        WHERE fr2.id_detalle_solicitud = dt.id_detalle_solicitud
                          AND fr2._estado_aud <> 'ELIMINADO'
                        ORDER BY fr2.id_falla_reportada
                        LIMIT 1
                    ) AS "problemaReportado",
                
                    (
                        SELECT fr2.id_falla_reportada
                        FROM falla_reportada fr2
                        WHERE fr2.id_detalle_solicitud = dt.id_detalle_solicitud
                          AND fr2._estado_aud <> 'ELIMINADO'
                        ORDER BY fr2.id_falla_reportada
                        LIMIT 1
                    ) AS "idFallaReportada",
                
                    COALESCE(dt.estado_det_sol::text, 'PENDIENTE') AS "estadoDetalle",
                
                    p.nombre_pri AS "prioridad",
                    p.orden_pri AS "ordenPrioridad",
                
                    (
                        SELECT STRING_AGG(f2.nombre_fall, ', ')
                        FROM falla_reportada fr2
                        LEFT JOIN falla f2 ON fr2.id_falla = f2.id_falla
                        WHERE fr2.id_detalle_solicitud = dt.id_detalle_solicitud
                          AND fr2._estado_aud <> 'ELIMINADO'
                          AND f2.id_falla IS NOT NULL
                    ) AS "fallaCatalogada",
                
                    (
                        SELECT STRING_AGG(srv2.nombre_serv, ', ')
                        FROM atencion_servicio asv2
                        JOIN servicio srv2 ON asv2.id_servicio = srv2.id_servicio
                        WHERE asv2.id_atencion_solicitud = aten.id_atencion_solicitud
                          AND asv2._estado_aud <> 'ELIMINADO'
                    ) AS "servicio",
                
                    aten.id_atencion_solicitud AS "idAtencion",
                    aten.id_prioridad AS "idPrioridad",
                    (
                        SELECT STRING_AGG(asv2.id_servicio::text, ',')
                        FROM atencion_servicio asv2
                        WHERE asv2.id_atencion_solicitud = aten.id_atencion_solicitud
                          AND asv2._estado_aud <> 'ELIMINADO'
                    ) AS "idsServicios"
                
                FROM detalle_solicitud dt
                
                INNER JOIN solicitud s      ON dt.id_solicitud = s.id_solicitud
                INNER JOIN activo a         ON dt.id_activo = a.id_activo
                INNER JOIN tecnico_asignado ta ON ta.id_solicitud = s.id_solicitud
                                             AND ta._estado_aud <> 'ELIMINADO'
                INNER JOIN administrativo adm ON adm.id_administrativo = ta.id_administrativo
                INNER JOIN persona per      ON per.id_persona = adm.id_persona
                INNER JOIN usuario u        ON u.id_persona = per.id_persona
                
                LEFT JOIN tipo_activo t     ON a.id_tipo_activo = t.id_tipo_activo
                LEFT JOIN atencion_solicitud aten
                    ON dt.id_detalle_solicitud = aten.id_detalle_solicitud
                   AND aten._estado_aud <> 'ELIMINADO'
                LEFT JOIN prioridad p       ON aten.id_prioridad = p.id_prioridad
                
                WHERE dt._estado_aud <> 'ELIMINADO'
                  AND s._estado_aud  <> 'ELIMINADO'
                  AND u.username = :username
                
                ORDER BY dt.id_detalle_solicitud,
                         COALESCE(p.orden_pri, 999)
                """;

        return namedParameterJdbcTemplate.queryForList(sql, Map.of("username", username));
    }

    public List<Map<String, Object>> generarReporte(String nroSol) {
        String nroLimpio = nroSol.replaceAll("[^0-9]", "");

        String sql = """
                
                    SELECT
                    p_sol.nombre_per || ' ' || p_sol.ap_paterno_per || ' ' || p_sol.ap_materno_per AS nombresolicitante,
                    p_sol.celular_per AS telefonosolicitante,
                    c_sol.nombre_car AS cargosolicitante,
                    u_sol.nombre_uni AS unidadsolicitante,
                    a.codigo_act AS codigoactivo,
                    a.descripcion_act AS descripcionactivo,
                    u_sol.nombre_uni AS unidadactivo,
                    t.nombre_tipo_act AS tipoactivo,
                    STRING_AGG(DISTINCT f.nombre_fall, ', ') AS fallas,
                    STRING_AGG(DISTINCT s.observaciones_serv, ', ') AS observaciones,
                    so.fecha_sol AS fecharecepcion,
                    p_tec.nombre_per || ' ' || p_tec.ap_paterno_per || ' ' || p_tec.ap_materno_per AS nombrecompletotecnico
                FROM public.solicitud so
                         INNER JOIN public.administrativo adm_sol ON so.id_administrativo = adm_sol.id_administrativo
                         INNER JOIN public.persona p_sol ON adm_sol.id_persona = p_sol.id_persona
                         INNER JOIN public.cargo c_sol ON adm_sol.id_cargo = c_sol.id_cargo
                         INNER JOIN public.unidad u_sol ON adm_sol.id_unidad = u_sol.id_unidad
                         INNER JOIN public.detalle_solicitud ds ON so.id_solicitud = ds.id_solicitud
                         INNER JOIN public.activo a ON ds.id_activo = a.id_activo
                         INNER JOIN public.tipo_activo t ON a.id_tipo_activo = t.id_tipo_activo
                         INNER JOIN public.falla_reportada fr ON ds.id_detalle_solicitud = fr.id_detalle_solicitud
                         LEFT JOIN public.falla f ON fr.id_falla = f.id_falla
                         INNER JOIN public.atencion_solicitud aten ON ds.id_detalle_solicitud = aten.id_detalle_solicitud
                         INNER JOIN public.atencion_servicio asv ON aten.id_atencion_solicitud = asv.id_atencion_solicitud
                                                                AND asv._estado_aud <> 'ELIMINADO'
                         INNER JOIN public.servicio s ON asv.id_servicio = s.id_servicio
                         LEFT JOIN (
                             SELECT DISTINCT ON (id_solicitud) id_solicitud, id_administrativo
                             FROM public.tecnico_asignado
                             WHERE _estado_aud <> 'ELIMINADO'
                             ORDER BY id_solicitud, id_tecnico_asignado
                         ) ta ON ta.id_solicitud = so.id_solicitud
                         LEFT JOIN public.administrativo adm_tec ON ta.id_administrativo = adm_tec.id_administrativo
                         LEFT JOIN public.persona p_tec ON adm_tec.id_persona = p_tec.id_persona
                WHERE so.nro_sol = :nroLimpio
                GROUP BY
                    p_sol.nombre_per, p_sol.ap_paterno_per, p_sol.ap_materno_per, p_sol.celular_per,
                    c_sol.nombre_car, u_sol.nombre_uni,
                    a.codigo_act, a.descripcion_act, t.nombre_tipo_act,
                    so.fecha_sol,
                    p_tec.nombre_per, p_tec.ap_paterno_per, p_tec.ap_materno_per
                """;

        Map<String, Object> params = Map.of("nroLimpio", Long.parseLong(nroLimpio));

        return namedParameterJdbcTemplate.queryForList(sql, params);
    }

    // ── Métodos de vista previa (datos JSON) ──────────────────────────────

    public String obtenerServiciosPorTipoPorActivo(Long nroSolicitud, String codigoActivo) {
        String sql = """
                SELECT COALESCE(ts.nombre_tipo, 'SIN CATEGORÍA') AS nombreTipo,
                       s.nombre_serv                              AS nombreServ
                FROM public.solicitud so
                JOIN public.detalle_solicitud ds    ON so.id_solicitud = ds.id_solicitud
                                                   AND ds._estado_aud <> 'ELIMINADO'
                JOIN public.atencion_solicitud aten ON ds.id_detalle_solicitud = aten.id_detalle_solicitud
                                                   AND aten._estado_aud <> 'ELIMINADO'
                JOIN public.atencion_servicio asv   ON aten.id_atencion_solicitud = asv.id_atencion_solicitud
                                                   AND asv._estado_aud <> 'ELIMINADO'
                JOIN public.servicio s              ON asv.id_servicio = s.id_servicio
                LEFT JOIN public.tipo_servicio ts   ON s.id_tipo_servicio = ts.id_tipo_servicio
                JOIN public.activo a                ON ds.id_activo = a.id_activo
                WHERE so.nro_sol = :nroSolicitud
                  AND a.codigo_act = :codigoActivo
                ORDER BY ts.nombre_tipo NULLS LAST, s.nombre_serv
                """;

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql,
                Map.of("nroSolicitud", nroSolicitud, "codigoActivo", codigoActivo));

        if (rows.isEmpty()) return "";

        java.util.LinkedHashMap<String, java.util.List<String>> porTipo = new java.util.LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String tipo = (String) row.get("nombreTipo");
            String serv = (String) row.get("nombreServ");
            porTipo.computeIfAbsent(tipo, k -> new java.util.ArrayList<>()).add(serv);
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, java.util.List<String>> entry : porTipo.entrySet()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(entry.getKey()).append(":\n");
            for (String s : entry.getValue()) sb.append("  • ").append(s).append("\n");
        }
        return sb.toString().trim();
    }

    public List<Map<String, Object>> previewFichaTecnica(int nroSol) {
        String sql = """
                SELECT p_sol.nombre_per || ' ' || p_sol.ap_paterno_per || ' ' || p_sol.ap_materno_per AS nombreSolicitante,
                       p_sol.celular_per                                                               AS telefonoSolicitante,
                       c_sol.nombre_car                                                                AS cargoSolicitante,
                       u_sol.nombre_uni                                                                AS unidadSolicitante,
                       a.codigo_act                                                                    AS codigoActivo,
                       a.descripcion_act                                                               AS descripcionActivo,
                       t.nombre_tipo_act                                                               AS tipoActivo,
                       STRING_AGG(DISTINCT f.nombre_fall, ', ')                                        AS fallas,
                       STRING_AGG(DISTINCT s.observaciones_serv, ', ')                                 AS observaciones,
                       so.fecha_sol                                                                    AS fechaRecepcion,
                       p_tec.nombre_per || ' ' || p_tec.ap_paterno_per || ' ' || p_tec.ap_materno_per AS nombreCompletoTecnico,
                       STRING_AGG(DISTINCT s.nombre_serv, ', ')                                        AS tipoServicio,
                       so.descripcion_sol                                                              AS recomendacion,
                       so.nro_sol_externa                                                              AS idExterno,
                       COALESCE(MAX(aten.fecha_fin_reparacion_aten_sol),
                                MAX(ds.fecha_actualizacion_det_sol))                                   AS fechaEntrega
                FROM public.solicitud so
                         LEFT JOIN public.administrativo adm_sol ON so.id_administrativo = adm_sol.id_administrativo
                         LEFT JOIN public.persona p_sol          ON adm_sol.id_persona = p_sol.id_persona
                         LEFT JOIN public.cargo c_sol            ON adm_sol.id_cargo = c_sol.id_cargo
                         LEFT JOIN public.unidad u_sol           ON adm_sol.id_unidad = u_sol.id_unidad
                         LEFT JOIN public.detalle_solicitud ds   ON so.id_solicitud = ds.id_solicitud
                         LEFT JOIN public.activo a               ON ds.id_activo = a.id_activo
                         LEFT JOIN public.tipo_activo t          ON a.id_tipo_activo = t.id_tipo_activo
                         LEFT JOIN public.falla_reportada fr     ON ds.id_detalle_solicitud = fr.id_detalle_solicitud
                         LEFT JOIN public.falla f                ON fr.id_falla = f.id_falla
                         LEFT JOIN public.atencion_solicitud aten
                                ON ds.id_detalle_solicitud = aten.id_detalle_solicitud
                               AND aten._estado_aud <> 'ELIMINADO'
                         LEFT JOIN public.atencion_servicio asv
                                ON aten.id_atencion_solicitud = asv.id_atencion_solicitud
                               AND asv._estado_aud <> 'ELIMINADO'
                         LEFT JOIN public.servicio s             ON asv.id_servicio = s.id_servicio
                         LEFT JOIN (
                             SELECT DISTINCT ON (id_solicitud) id_solicitud, id_administrativo
                             FROM public.tecnico_asignado
                             WHERE _estado_aud <> 'ELIMINADO'
                             ORDER BY id_solicitud, id_tecnico_asignado
                         ) ta ON ta.id_solicitud = so.id_solicitud
                         LEFT JOIN public.administrativo adm_tec ON ta.id_administrativo = adm_tec.id_administrativo
                         LEFT JOIN public.persona p_tec          ON adm_tec.id_persona = p_tec.id_persona
                WHERE so.nro_sol = :nroSol
                GROUP BY p_sol.nombre_per, p_sol.ap_paterno_per, p_sol.ap_materno_per, p_sol.celular_per,
                         c_sol.nombre_car, u_sol.nombre_uni,
                         a.codigo_act, a.descripcion_act, t.nombre_tipo_act,
                         so.fecha_sol, so.descripcion_sol, so.nro_sol_externa,
                         p_tec.nombre_per, p_tec.ap_paterno_per, p_tec.ap_materno_per
                """;
        return namedParameterJdbcTemplate.queryForList(sql, Map.of("nroSol", nroSol));
    }

    public List<Map<String, Object>> previewInformeGeneral(int idInforme) {
        String sql = """
                SELECT
                    ig.nro_informe                                                                                   AS nroinformetext,
                    ig.observaciones                                                                                 AS observaciones,
                    ig.trabajo_realizado                                                                             AS resumentecnico,
                    ig.fecha_cierre_informe                                                                          AS fechacierre,
                    so.nro_sol                                                                                       AS nrosolicitud,
                    so.fecha_sol                                                                                     AS fechasolicitud,
                    so.descripcion_sol                                                                               AS descripcion,
                    p_sol.nombre_per || ' ' || p_sol.ap_paterno_per || ' ' || p_sol.ap_materno_per                  AS solicitante,
                    c_sol.nombre_car                                                                                 AS cargo,
                    u_sol.nombre_uni                                                                                 AS unidad,
                    COALESCE(STRING_AGG(DISTINCT p_tec.nombre_per || ' ' || p_tec.ap_paterno_per, ', '), '—')       AS tecnicos,
                    COALESCE(STRING_AGG(DISTINCT a.codigo_act || ' · ' || a.descripcion_act, ', '), '—')            AS activos,
                    COALESCE(STRING_AGG(DISTINCT srv.nombre_serv, ', '), '—')                                       AS servicios,
                    COALESCE(STRING_AGG(DISTINCT f.nombre_fall, ', '), 'Sin catalogar')                             AS fallas
                FROM informe_equipo_general ig
                         JOIN solicitud so              ON ig.id_solicitud = so.id_solicitud
                         LEFT JOIN administrativo adm_sol ON so.id_administrativo = adm_sol.id_administrativo
                         LEFT JOIN persona p_sol        ON adm_sol.id_persona = p_sol.id_persona
                         LEFT JOIN cargo c_sol          ON adm_sol.id_cargo = c_sol.id_cargo
                         LEFT JOIN unidad u_sol         ON adm_sol.id_unidad = u_sol.id_unidad
                         LEFT JOIN detalle_solicitud ds ON so.id_solicitud = ds.id_solicitud
                                                       AND ds._estado_aud <> 'ELIMINADO'
                         LEFT JOIN activo a             ON ds.id_activo = a.id_activo
                         LEFT JOIN falla_reportada fr   ON ds.id_detalle_solicitud = fr.id_detalle_solicitud
                         LEFT JOIN falla f              ON fr.id_falla = f.id_falla
                         LEFT JOIN atencion_solicitud aten ON ds.id_detalle_solicitud = aten.id_detalle_solicitud
                                                          AND aten._estado_aud <> 'ELIMINADO'
                         LEFT JOIN atencion_servicio asv ON aten.id_atencion_solicitud = asv.id_atencion_solicitud
                                                         AND asv._estado_aud <> 'ELIMINADO'
                         LEFT JOIN servicio srv         ON asv.id_servicio = srv.id_servicio
                         LEFT JOIN (
                             SELECT DISTINCT ON (id_solicitud) id_solicitud, id_administrativo
                             FROM tecnico_asignado
                             WHERE _estado_aud <> 'ELIMINADO'
                             ORDER BY id_solicitud, id_tecnico_asignado
                         ) ta ON ta.id_solicitud = so.id_solicitud
                         LEFT JOIN administrativo adm_tec ON ta.id_administrativo = adm_tec.id_administrativo
                         LEFT JOIN persona p_tec         ON adm_tec.id_persona = p_tec.id_persona
                WHERE ig.id_informe_equipo_general = :idInforme
                GROUP BY ig.nro_informe, ig.observaciones, ig.trabajo_realizado, ig.fecha_cierre_informe,
                         so.nro_sol, so.fecha_sol, so.descripcion_sol,
                         p_sol.nombre_per, p_sol.ap_paterno_per, p_sol.ap_materno_per,
                         c_sol.nombre_car, u_sol.nombre_uni
                """;
        return namedParameterJdbcTemplate.queryForList(sql, Map.of("idInforme", idInforme));
    }

    public List<Map<String, Object>> previewInformeEspecifico(int idInforme) {
        String sql = """
                SELECT
                    ie.nro_informe                                                                              AS nroinformetext,
                    ie.observaciones                                                                            AS observaciones,
                    ie.trabajo_realizado                                                                        AS trabajorealizado,
                    ie.detalle                                                                                  AS detalle,
                    ie.fecha_cierre_informe                                                                     AS fechacierre,
                    so.nro_sol                                                                                  AS nrosolicitud,
                    so.fecha_sol                                                                                AS fechasolicitud,
                    a.codigo_act                                                                                AS codigoactivo,
                    a.descripcion_act                                                                           AS descripcionactivo,
                    tip.nombre_tipo_act                                                                         AS tipoactivo,
                    COALESCE(STRING_AGG(DISTINCT f.nombre_fall, ', '), 'Sin catalogar')                        AS fallas,
                    COALESCE(STRING_AGG(DISTINCT srv.nombre_serv, ', '), '—')                                  AS servicio,
                    COALESCE(pri.nombre_pri, '—')                                                              AS prioridad,
                    aten.fecha_ini_reparacion_aten_sol                                                         AS fechainireparacion,
                    aten.fecha_fin_reparacion_aten_sol                                                         AS fechafinreparacion,
                    COALESCE(STRING_AGG(DISTINCT p_tec.nombre_per || ' ' || p_tec.ap_paterno_per, ', '), '—') AS tecnicos,
                    p_sol.nombre_per || ' ' || p_sol.ap_paterno_per || ' ' || p_sol.ap_materno_per             AS solicitante,
                    u_sol.nombre_uni                                                                            AS unidad,
                    c_sol.nombre_car                                                                            AS cargo
                FROM informe_equipo_especifico ie
                         JOIN detalle_solicitud ds      ON ie.id_detalle_solicitud = ds.id_detalle_solicitud
                         JOIN solicitud so              ON ds.id_solicitud = so.id_solicitud
                         JOIN activo a                  ON ds.id_activo = a.id_activo
                         LEFT JOIN tipo_activo tip      ON a.id_tipo_activo = tip.id_tipo_activo
                         LEFT JOIN falla_reportada fr   ON ds.id_detalle_solicitud = fr.id_detalle_solicitud
                         LEFT JOIN falla f              ON fr.id_falla = f.id_falla
                         LEFT JOIN atencion_solicitud aten ON ds.id_detalle_solicitud = aten.id_detalle_solicitud
                                                          AND aten._estado_aud <> 'ELIMINADO'
                         LEFT JOIN atencion_servicio asv ON aten.id_atencion_solicitud = asv.id_atencion_solicitud
                                                         AND asv._estado_aud <> 'ELIMINADO'
                         LEFT JOIN servicio srv         ON asv.id_servicio = srv.id_servicio
                         LEFT JOIN prioridad pri        ON aten.id_prioridad = pri.id_prioridad
                         LEFT JOIN (
                             SELECT DISTINCT ON (id_solicitud) id_solicitud, id_administrativo
                             FROM tecnico_asignado
                             WHERE _estado_aud <> 'ELIMINADO'
                             ORDER BY id_solicitud, id_tecnico_asignado
                         ) ta ON ta.id_solicitud = so.id_solicitud
                         LEFT JOIN administrativo adm_tec ON ta.id_administrativo = adm_tec.id_administrativo
                         LEFT JOIN persona p_tec         ON adm_tec.id_persona = p_tec.id_persona
                         LEFT JOIN administrativo adm_sol ON so.id_administrativo = adm_sol.id_administrativo
                         LEFT JOIN persona p_sol         ON adm_sol.id_persona = p_sol.id_persona
                         LEFT JOIN unidad u_sol          ON adm_sol.id_unidad = u_sol.id_unidad
                         LEFT JOIN cargo c_sol           ON adm_sol.id_cargo = c_sol.id_cargo
                WHERE ie.id_informe_equipo_especifico = :idInforme
                GROUP BY ie.nro_informe, ie.observaciones, ie.trabajo_realizado, ie.detalle, ie.fecha_cierre_informe,
                         so.nro_sol, so.fecha_sol, a.codigo_act, a.descripcion_act, tip.nombre_tipo_act,
                         pri.nombre_pri, aten.fecha_ini_reparacion_aten_sol, aten.fecha_fin_reparacion_aten_sol,
                         p_sol.nombre_per, p_sol.ap_paterno_per, p_sol.ap_materno_per, u_sol.nombre_uni, c_sol.nombre_car
                """;
        return namedParameterJdbcTemplate.queryForList(sql, Map.of("idInforme", idInforme));
    }

    public Map<String, Object> statsTecnico(String username) {
        String sql = """
                SELECT
                    COUNT(DISTINCT ta.id_solicitud)                                      AS "totalAsignadas",
                    COUNT(DISTINCT CASE WHEN s.estado_sol = 'FINALIZADO'
                                        THEN ta.id_solicitud END)                        AS "finalizadas",
                    COUNT(DISTINCT CASE WHEN s.estado_sol = 'EN_MANTENIMIENTO'
                                        THEN ta.id_solicitud END)                        AS "enMantenimiento",
                    COUNT(DISTINCT CASE WHEN s.estado_sol = 'PENDIENTE'
                                        THEN ta.id_solicitud END)                        AS "pendientes",
                    ROUND(AVG(
                        CASE WHEN aten.fecha_fin_reparacion_aten_sol IS NOT NULL
                                  AND aten.fecha_ini_reparacion_aten_sol IS NOT NULL
                                  AND aten.fecha_fin_reparacion_aten_sol >= aten.fecha_ini_reparacion_aten_sol
                             THEN (aten.fecha_fin_reparacion_aten_sol - aten.fecha_ini_reparacion_aten_sol)
                        END
                    )::numeric, 1)                                                        AS "promedioDiasResolucion"
                FROM tecnico_asignado ta
                INNER JOIN administrativo adm ON adm.id_administrativo = ta.id_administrativo
                INNER JOIN persona per         ON per.id_persona        = adm.id_persona
                INNER JOIN usuario u           ON u.id_persona          = per.id_persona
                INNER JOIN solicitud s         ON s.id_solicitud        = ta.id_solicitud
                LEFT JOIN detalle_solicitud ds  ON ds.id_solicitud       = s.id_solicitud
                                               AND ds._estado_aud       <> 'ELIMINADO'
                LEFT JOIN atencion_solicitud aten ON aten.id_detalle_solicitud = ds.id_detalle_solicitud
                                                 AND aten._estado_aud          <> 'ELIMINADO'
                WHERE ta._estado_aud  <> 'ELIMINADO'
                  AND s._estado_aud   <> 'ELIMINADO'
                  AND u.username       = :username
                """;
        var rows = namedParameterJdbcTemplate.queryForList(sql, Map.of("username", username));
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    public List<Map<String, Object>> solicitudesPorMesTecnico(String username) {
        String sql = """
                SELECT
                    TO_CHAR(s.fecha_sol, 'Mon') AS "mes",
                    EXTRACT(MONTH FROM s.fecha_sol)::int AS "nroMes",
                    COUNT(DISTINCT s.id_solicitud)       AS "cantidad"
                FROM tecnico_asignado ta
                INNER JOIN administrativo adm ON adm.id_administrativo = ta.id_administrativo
                INNER JOIN persona per         ON per.id_persona        = adm.id_persona
                INNER JOIN usuario u           ON u.id_persona          = per.id_persona
                INNER JOIN solicitud s         ON s.id_solicitud        = ta.id_solicitud
                WHERE ta._estado_aud <> 'ELIMINADO'
                  AND s._estado_aud  <> 'ELIMINADO'
                  AND EXTRACT(YEAR FROM s.fecha_sol) = EXTRACT(YEAR FROM CURRENT_DATE)
                  AND u.username = :username
                GROUP BY EXTRACT(MONTH FROM s.fecha_sol), TO_CHAR(s.fecha_sol, 'Mon')
                ORDER BY 2
                """;
        return namedParameterJdbcTemplate.queryForList(sql, Map.of("username", username));
    }

    public List<Map<String, Object>> topFallasTecnico(String username) {
        String sql = """
                SELECT
                    f.nombre_fall AS "nombreFalla",
                    COUNT(*)      AS "cantidad"
                FROM tecnico_asignado ta
                INNER JOIN administrativo adm ON adm.id_administrativo = ta.id_administrativo
                INNER JOIN persona per         ON per.id_persona        = adm.id_persona
                INNER JOIN usuario u           ON u.id_persona          = per.id_persona
                INNER JOIN solicitud s         ON s.id_solicitud        = ta.id_solicitud
                INNER JOIN detalle_solicitud ds ON ds.id_solicitud      = s.id_solicitud
                                              AND ds._estado_aud        <> 'ELIMINADO'
                INNER JOIN falla_reportada fr  ON fr.id_detalle_solicitud = ds.id_detalle_solicitud
                                              AND fr._estado_aud          <> 'ELIMINADO'
                INNER JOIN falla f             ON f.id_falla             = fr.id_falla
                WHERE ta._estado_aud <> 'ELIMINADO'
                  AND s._estado_aud  <> 'ELIMINADO'
                  AND u.username = :username
                GROUP BY f.nombre_fall
                ORDER BY COUNT(*) DESC
                LIMIT 5
                """;
        return namedParameterJdbcTemplate.queryForList(sql, Map.of("username", username));
    }

    public List<Map<String, Object>> listaTecnicosConStats() {
        String sql = """
                SELECT
                    u.username                                                AS "username",
                    CONCAT(per.nombre_per, ' ', per.ap_paterno_per)           AS "nombreCompleto",
                    COUNT(DISTINCT ta.id_solicitud)                           AS "totalAsignadas",
                    COUNT(DISTINCT CASE WHEN s.estado_sol = 'FINALIZADO'
                                        THEN ta.id_solicitud END)             AS "finalizadas"
                FROM usuario u
                INNER JOIN persona per          ON per.id_persona        = u.id_persona
                INNER JOIN administrativo adm   ON adm.id_persona        = per.id_persona
                INNER JOIN usr_rol ur           ON ur.id_usuario         = u.id_usuario
                                               AND ur._estado_aud        <> 'ELIMINADO'
                INNER JOIN rol r               ON r.id_rol               = ur.id_rol
                LEFT JOIN tecnico_asignado ta   ON ta.id_administrativo  = adm.id_administrativo
                                               AND ta._estado_aud        <> 'ELIMINADO'
                LEFT JOIN solicitud s           ON s.id_solicitud        = ta.id_solicitud
                                               AND s._estado_aud         <> 'ELIMINADO'
                WHERE u._estado_aud   <> 'ELIMINADO'
                  AND adm._estado_aud <> 'ELIMINADO'
                  AND r.nombre_rol     = 'TECNICO'
                GROUP BY u.username, per.nombre_per, per.ap_paterno_per
                ORDER BY per.nombre_per
                """;
        return namedParameterJdbcTemplate.queryForList(sql, Map.of());
    }

}