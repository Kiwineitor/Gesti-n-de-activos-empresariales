package com.sath.sistema_sath.domain.detalleSolicitud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface DetalleSolicitudRepository extends JpaRepository<DetalleSolicitud, Long> {

    @Modifying
    @Query(value = """
            UPDATE detalle_solicitud
               SET estado_det_sol              = :estado,
                   fecha_actualizacion_det_sol = CURRENT_DATE
             WHERE id_solicitud  = :idSolicitud
               AND _estado_aud  <> 'ELIMINADO'
            """, nativeQuery = true)
    void actualizarEstadoPorSolicitud(@Param("idSolicitud") Long idSolicitud,
                                      @Param("estado") String estado);

    @Query(value = """
                SELECT ds.id_detalle_solicitud        AS "idDetalleSolicitud",
                       ds.id_activo                   AS "idActivo",
                       ds.id_solicitud                AS "idSolicitud",
                       a.codigo_act                   AS "codigoActivo",
                       a.descripcion_act              AS "descripcionActivo",
                       s.nro_sol                      AS "nroSol",
                       ds.tipo_informe                AS "tipoInforme",
                       ds.estado_det_sol              AS "estadoDetalleSol",
                       ds.fecha_actualizacion_det_sol AS "fechaActualizacionDetalleAct"
                FROM detalle_solicitud ds
                JOIN activo    a ON a.id_activo    = ds.id_activo
                JOIN solicitud s ON s.id_solicitud = ds.id_solicitud
                WHERE ds._estado_aud <> 'ELIMINADO'
                  AND ds._estado_aud IS NOT NULL
            """, nativeQuery = true)
    List<Map<String, Object>> listaParaELFrontend();


}
