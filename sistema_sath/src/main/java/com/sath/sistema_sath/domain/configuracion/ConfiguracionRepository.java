package com.sath.sistema_sath.domain.configuracion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, Long> {

    Optional<Configuracion> findByUnidad_IdUnidadAndClave(Long idUnidad, String clave);

    List<Configuracion> findByUnidad_IdUnidad(Long idUnidad);
}
