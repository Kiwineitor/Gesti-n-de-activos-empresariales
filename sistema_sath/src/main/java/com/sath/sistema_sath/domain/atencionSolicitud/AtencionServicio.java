package com.sath.sistema_sath.domain.atencionSolicitud;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.servicio.Servicio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "atencion_servicio")
public class AtencionServicio extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAtencionServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_atencion_solicitud", nullable = false)
    private AtencionSolicitud atencionSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDate fechaAsignacion;
}
