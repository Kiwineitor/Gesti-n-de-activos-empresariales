package com.sath.sistema_sath.domain.InformeEquipoGeneral;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface InformeEquipoGeneralRepository extends JpaRepository<InformeEquipoGeneral, Long> {

    @Query(value = """
            
             select i.id_informe_equipo_general as idInformeEquipoGeneral,
                    i.id_solicitud              as idSolicitud,
                    i.nro_informe               as nroInforme,
                    i.trabajo_realizado         as trabajoRealizado,
                    i.observaciones             as observacionesInforme,
                    i.fecha_cierre_informe      as fechaCierreinforme
             from informe_equipo_general i
             where i._estado_aud <> 'ELIMINADO'
               and i._estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listaInformeEquipoGeneral();

    @Query("SELECT COALESCE(MAX(i.nroInforme), 0) FROM InformeEquipoGeneral i")
    int findMaxNroInforme();


    boolean existsBySolicitudIdSolicitud(Long idSolicitud);

    java.util.Optional<InformeEquipoGeneral> findBySolicitudIdSolicitud(Long idSolicitud);
}
