package com.sath.sistema_sath.domain.InformeEquipoEspecifico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface InformeEquipoEspecificoRepository extends JpaRepository<InformeEquipoEspecifico, Long> {

    @Query(value = """
            
             select i.id_informe_equipo_especifico as idInformeEquipoEspecifico,
                                i.id_detalle_solicitud as idDetalleSolicitud,
                   i.nro_informe                  as nroInforme,
                   i.observaciones                as observaciones,
                   i.trabajo_realizado             as trabajoRealizado,
                   i.detalle                      as detalle,
                   i.fecha_cierre_informe           as fechaCierreInformeEsp
            from informe_equipo_especifico i
            where i._estado_aud <> 'ELIMINADO'
              and i._estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listaInformeEquipoEspecifico();

    @Query("SELECT COALESCE(MAX(i.nroInforme), 0) FROM InformeEquipoEspecifico i")
    int findMaxNroInforme();

    boolean existsByDetalleSolicitudIdDetalleSolicitud(Long idDetalleSolicitud);

    java.util.Optional<InformeEquipoEspecifico> findByDetalleSolicitudIdDetalleSolicitud(Long idDetalleSolicitud);
}
