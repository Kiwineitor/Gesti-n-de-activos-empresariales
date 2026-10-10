package com.sath.sistema_sath.domain.usrRol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface UsrRolRepository extends JpaRepository<UsrRol, Long> {

    @Query(value = """
            
             select ur.id_usr_rol as idUsrRol,
                   ur.id_usuario as idUsuario,
                   ur.id_rol     as idRol
            from usr_rol ur
            where ur._estado_aud <> 'ELIMINADO'
              and ur._estado_aud is not null
            """, nativeQuery = true)
    List<Map<String, Object>> listaUsrRol();
}
