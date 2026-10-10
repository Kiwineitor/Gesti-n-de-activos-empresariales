package com.sath.sistema_sath.domain.usrPermiso;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsrPermisoRepository extends JpaRepository<UsrPermiso, Long> {

    @Query("SELECT up FROM UsrPermiso up WHERE up.usuario.idUsuario = :idUsuario AND up.estado != :eliminado")
    List<UsrPermiso> findActivosByUsuario(@Param("idUsuario") Long idUsuario,
                                          @Param("eliminado") EstadoEnum eliminado);

    @Query("SELECT up FROM UsrPermiso up WHERE up.usuario.username = :username AND up.estado != :eliminado")
    List<UsrPermiso> findActivosByUsername(@Param("username") String username,
                                           @Param("eliminado") EstadoEnum eliminado);

    boolean existsByUsuario_IdUsuarioAndPermiso_IdPermisoAndEstadoNot(
            Long idUsuario, Long idPermiso, EstadoEnum estado);
}
