package com.sath.sistema_sath.domain.solicitud;

import com.sath.sistema_sath.domain.solicitud.Enum.EstadoSolicitudEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByEstadoSolOrderByIdSolicitudAsc(EstadoSolicitudEnum estadoSol);


    @Query(value = """
            
             select s.id_solicitud     as idSolicitud,
                   s.estado_sol       as estadoSol,
                   s.nro_sol          as nroSol,
                   s.gestion_sol      as gestionSol,
                   s.fecha_sol        as fechaSol,
                   s.descripcion_sol  as descripcionSol
            from solicitud s
            where s._estado_aud <> 'ELIMINADO'
              and s._estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listaSolicitud();

    @Query("SELECT COUNT(s) > 0 FROM Solicitud s WHERE s.nroSolExterna = :nroSolExterna")
    boolean existsByNroSolExterna(@Param("nroSolExterna") String nroSolExterna);


    @Query("SELECT COUNT(s) FROM Solicitud s WHERE s.gestionSol = :anio")
    long countByGestionSol(@Param("anio") int anio);

    @Query(value = """
            select s.id_solicitud     as "idSolicitud",
                   s.estado_sol       as "estadoSol",
                   s.nro_sol          as "nroSol",
                               s.id_externo as "nroSolExterna",
                   s.gestion_sol      as "gestionSol",
                   s.fecha_sol        as "fechaSol",
                   s.descripcion_sol  as "descripcionSol"
            from solicitud s
            where s._estado_aud <> 'ELIMINADO'
              and s.estado_sol = 'FINALIZADO'
              and s._estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listaSolicitudFinalizado();

    @Query(value = """
            SELECT DISTINCT
                   s.id_solicitud    AS "idSolicitud",
                   s.estado_sol      AS "estadoSol",
                   s.nro_sol         AS "nroSol",
                   s.id_externo  AS "idExterno",
                   s.gestion_sol     AS "gestionSol",
                   s.fecha_sol       AS "fechaSol",
                   s.descripcion_sol AS "descripcionSol"
            FROM solicitud s
            JOIN tecnico_asignado ta ON ta.id_solicitud = s.id_solicitud
                                    AND ta._estado_aud <> 'ELIMINADO'
            JOIN administrativo adm ON adm.id_administrativo = ta.id_administrativo
            JOIN persona per        ON per.id_persona = adm.id_persona
            JOIN usuario u          ON u.id_persona = per.id_persona
            WHERE s._estado_aud <> 'ELIMINADO'
              AND s._estado_aud IS NOT NULL
              AND u.username = :username
            ORDER BY s.id_solicitud DESC
            """, nativeQuery = true)
    List<Map<String, Object>> listaSolicitudPorTecnico(@Param("username") String username);

    @Query(value = """
        SELECT COUNT(*)
        FROM solicitud
        WHERE estado_sol = 'PENDIENTE'
        """, nativeQuery = true)
    Long colaMax();

    @Query(value = """
            SELECT DISTINCT
                   s.id_solicitud    AS "idSolicitud",
                   s.estado_sol      AS "estadoSol",
                   s.nro_sol         AS "nroSol",
                   s.nro_sol_externa AS "nroSolExterna",
                   s.gestion_sol     AS "gestionSol",
                   s.fecha_sol       AS "fechaSol",
                   s.descripcion_sol AS "descripcionSol"
            FROM public.solicitud s
            JOIN public.detalle_solicitud ds    ON ds.id_solicitud = s.id_solicitud
                                               AND ds._estado_aud <> 'ELIMINADO'
            JOIN public.atencion_solicitud aten ON aten.id_detalle_solicitud = ds.id_detalle_solicitud
                                               AND aten._estado_aud <> 'ELIMINADO'
            JOIN public.atencion_servicio asrv  ON asrv.id_atencion_solicitud = aten.id_atencion_solicitud
                                               AND asrv._estado_aud <> 'ELIMINADO'
            JOIN public.servicio srv            ON srv.id_servicio = asrv.id_servicio
            JOIN public.tipo_servicio ts        ON ts.id_tipo_servicio = srv.id_tipo_servicio
            WHERE s._estado_aud <> 'ELIMINADO'
              AND s._estado_aud IS NOT NULL
              AND ts.nombre_tipo = 'SERVICIO TÉCNICO'
            ORDER BY s.id_solicitud DESC
            """, nativeQuery = true)
    List<Map<String, Object>> listaSolicitudServicioTecnico();
}

