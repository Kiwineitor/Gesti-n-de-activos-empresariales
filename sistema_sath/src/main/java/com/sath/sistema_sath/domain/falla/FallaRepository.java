package com.sath.sistema_sath.domain.falla;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface FallaRepository extends JpaRepository<Falla, Long> {

    @Query(value = """
            select f.id_falla         as idFalla,
                   f.id_tipo_activo   as idtipoActivo,
                   f.nombre_fall      as nombreFall,
                   f.descripcion_fall as descripcionFall
            from falla f
            where f._estado_aud <> 'ELIMINADO' and f._estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listaParaElFrontendFalla();

    List<Falla> findByTipoActivo_IdTipoActivoAndEstadoNot(Long idTipoActivo, EstadoEnum estado);
}
