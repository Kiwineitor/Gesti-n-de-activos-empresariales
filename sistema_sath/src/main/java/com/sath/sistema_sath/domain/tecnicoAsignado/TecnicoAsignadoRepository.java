package com.sath.sistema_sath.domain.tecnicoAsignado;

import com.sath.sistema_sath.domain.administrativo.Administrativo;
import com.sath.sistema_sath.domain.solicitud.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface TecnicoAsignadoRepository extends JpaRepository<TecnicoAsignado, Long> {

    boolean existsByAdministrativoAndSolicitud(Administrativo administrativo, Solicitud solicitud);

    @Query(value = """
            SELECT t.id_tecnico_asignado                               AS "idTecnicoAsignado",
                   t.id_administrativo                                 AS "idAdministrativo",
                   t.id_solicitud                                      AS "idSolicitud",
                   t.fecha_asignacion_tec_asig                         AS "fechaAsignacionTecAsig",
                   CONCAT(p.nombre_per, ' ', p.ap_paterno_per)         AS "nombreCompleto",
                   s.nro_sol                                           AS "nroSolicitud"
            FROM tecnico_asignado t
            LEFT JOIN administrativo a ON t.id_administrativo = a.id_administrativo
            LEFT JOIN persona p        ON a.id_persona        = p.id_persona
            LEFT JOIN solicitud s      ON t.id_solicitud      = s.id_solicitud
            WHERE t._estado_aud <> 'ELIMINADO'
              AND t._estado_aud IS NOT NULL
            ORDER BY t.id_tecnico_asignado DESC
            """, nativeQuery = true)
    List<Map<String, Object>> listaTecnicoAsignado();

}
