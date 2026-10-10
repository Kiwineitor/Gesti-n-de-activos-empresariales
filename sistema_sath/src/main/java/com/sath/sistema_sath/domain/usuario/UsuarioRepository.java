package com.sath.sistema_sath.domain.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

   // Agregar estos dos métodos al repository existente
   Optional<Usuario> findByUsername(String username);
   boolean existsByUsername(String username);
   // En UsuarioRepository — agrega este método:
   @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.rol WHERE u.username = :username")
   Optional<Usuario> findByUsernameWithRoles(@Param("username") String username);

   @Query(value = """
           SELECT u.id_usuario       AS "idUsuario",
                  u.username         AS "username",
                  u._estado_aud          AS "estado",
                  CONCAT(p.nombre_per, ' ', p.ap_paterno_per) AS "nombrePersona",
                  c.nombre_car       AS "cargo"
           FROM usuario u
           INNER JOIN persona p        ON u.id_persona     = p.id_persona
           INNER JOIN administrativo a ON p.id_persona     = a.id_persona
           LEFT  JOIN cargo c          ON a.id_cargo       = c.id_cargo
           WHERE u._estado_aud <> 'ELIMINADO'
             AND u._estado_aud IS NOT NULL
             AND u._estado_aud = 'ACTIVO'
             AND a._estado_aud <> 'ELIMINADO'
           ORDER BY p.nombre_per
           """, nativeQuery = true)
   List<Map<String, Object>> usuariosConAdministrativo();
}
