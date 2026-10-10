package com.sath.sistema_sath.domain.fallaReportada;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface FallaReportadaRepository extends JpaRepository<FallaReportada, Long> {

    @Query(value = """
            
             select fr.id_falla_reportada as idFallaReportada,
                   fr.id_falla           as idFalla
            from falla_reportada fr
            where fr._estado_aud <> 'ELIMINADO'
              and fr._estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listaFallaReportada();

}
