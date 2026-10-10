package com.sath.sistema_sath.domain.atencionSolicitud;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.atencionSolicitud.Enum.EstadoAtencion;
import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitud;
import com.sath.sistema_sath.domain.prioridad.Prioridad;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "atencion_solicitud")
public class AtencionSolicitud extends Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAtencionSolicitud;

    @Column(name = "fecha_ini_reparacion_aten_sol")
    private LocalDate fechIniReparacionAtenSol;

    @Column(name = "fecha_fin_reparacion_aten_sol")
    private LocalDate fechFinReparacionAtenSol;

    @Enumerated(EnumType.STRING)
    private EstadoAtencion estadoReparacionAtenSol;

    @OneToMany(mappedBy = "atencionSolicitud", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AtencionServicio> atencionServicios = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prioridad")
    private Prioridad prioridad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_detalle_solicitud")
    private DetalleSolicitud detalleSolicitud;
}
