package com.sath.sistema_sath.domain.persona;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    @Query(value = """
            
             select p.id_persona as idPersona,
                   p.nombre_per    as nombres,
                   p.ap_paterno_per as paterno,
                   p.ap_materno_per    as materno,
                   p.ci_per         as ci,
                   p.celular_per    as celular,
                   p.correo_per     as correo
            from persona p
            where p._estado_aud <> 'ELIMINADO'
              and p._estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listaPersona();

    Optional<Persona> findByCiPer(String ci);
}
