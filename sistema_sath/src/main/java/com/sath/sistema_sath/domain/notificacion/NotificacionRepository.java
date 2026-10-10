package com.sath.sistema_sath.domain.notificacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findTop50ByOrderByFechaHoraDesc();

    long countByLeidaFalse();

    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.leida = false")
    void marcarTodasLeidas();
}