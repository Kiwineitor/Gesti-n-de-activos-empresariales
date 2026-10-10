package com.sath.sistema_sath.domain.administrativo;

import com.sath.sistema_sath.domain.persona.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AdministrativoRepository extends JpaRepository<Administrativo, Long> {


    @Query(value = """
            
                select
                id_administrativo as idAdministrativo,
                   codigo_admi       as codigoAdmi,
                   id_cargo          as idCargo,
                   id_persona        as idPersona,
                   id_unidad         as idUnidad
            
            from administrativo
            where _estado_aud <> 'ELIMINADO'
              and _estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listaParaElFrontend();

    Administrativo findByPersona(Persona persona);

    Optional<Administrativo> findFirstByPersona(Persona persona);

    Optional<Administrativo> findByCodigoAdmi(Integer codigoAdmi);

    @Query(value = """
        SELECT a.id_administrativo                   AS "idAdministrativo",
               a.codigo_admi                         AS "codigoAdministrativo",
               p.ci_per                              AS "ci",
               p.nombre_per                          AS "nombrePer",
               p.ap_paterno_per                      AS "apPaternoPer",
               COALESCE(p.ap_materno_per,'')         AS "apMaternoPer",
               CONCAT(p.nombre_per,' ',p.ap_paterno_per,' ',
                      COALESCE(p.ap_materno_per,'')) AS "nombreCompleto",
               COALESCE(p.celular_per,'')            AS "celular",
               COALESCE(p.correo_per,'')             AS "correo",
               COALESCE(u.nombre_uni,'')             AS "nombreUnidad",
               COALESCE(c.nombre_car,'')             AS "nombreCargo"
        FROM administrativo a
        JOIN persona p ON p.id_persona = a.id_persona
        LEFT JOIN unidad u ON u.id_unidad = a.id_unidad
        LEFT JOIN cargo c ON c.id_cargo = a.id_cargo
        WHERE p.ci_per = :ci
          AND a._estado_aud <> 'ELIMINADO'
        LIMIT 1
        """, nativeQuery = true)
    List<Map<String, Object>> findByCiPersona(@Param("ci") String ci);

    @Query(value = """
        SELECT a.id_administrativo                   AS "idAdministrativo",
               a.codigo_admi                         AS "codigoAdministrativo",
               p.ci_per                              AS "ci",
               p.nombre_per                          AS "nombrePer",
               p.ap_paterno_per                      AS "apPaternoPer",
               COALESCE(p.ap_materno_per,'')         AS "apMaternoPer",
               CONCAT(p.nombre_per,' ',p.ap_paterno_per,' ',
                      COALESCE(p.ap_materno_per,'')) AS "nombreCompleto",
               COALESCE(p.celular_per,'')            AS "celular",
               COALESCE(p.correo_per,'')             AS "correo",
               COALESCE(u.nombre_uni,'')             AS "nombreUnidad",
               COALESCE(c.nombre_car,'')             AS "nombreCargo"
        FROM administrativo a
        JOIN persona p ON p.id_persona = a.id_persona
        LEFT JOIN unidad u ON u.id_unidad = a.id_unidad
        LEFT JOIN cargo c ON c.id_cargo = a.id_cargo
        WHERE a.codigo_admi = :codigo
          AND a._estado_aud <> 'ELIMINADO'
        LIMIT 1
        """, nativeQuery = true)
    List<Map<String, Object>> findByCodigoAdmiMap(@Param("codigo") Long codigo);

}
