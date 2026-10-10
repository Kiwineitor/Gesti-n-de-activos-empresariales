package com.sath.sistema_sath.domain.evidenciaFoto;

import com.sath.sistema_sath.Config.Enum.EstadoEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenciaFotoRepository extends JpaRepository<EvidenciaFoto, Long> {

    List<EvidenciaFoto> findBySolicitudIdSolicitudAndEstadoNot(Long idSolicitud, EstadoEnum estado);

    long countBySolicitudIdSolicitudAndEstadoNot(Long idSolicitud, EstadoEnum estado);
}