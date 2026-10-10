package com.sath.sistema_sath.domain.detalleSolicitud;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.InformeEquipoEspecifico.InformeEquipoEspecifico;
import com.sath.sistema_sath.domain.InformeEquipoGeneral.InformeEquipoGeneral;
import com.sath.sistema_sath.domain.activo.Activo;
import com.sath.sistema_sath.domain.atencionSolicitud.AtencionSolicitud;
import com.sath.sistema_sath.domain.detalleSolicitud.Enum.TipoInformeEnum;
import com.sath.sistema_sath.domain.fallaReportada.FallaReportada;
import com.sath.sistema_sath.domain.solicitud.Solicitud;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "detalle_solicitud")
public class DetalleSolicitud extends Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalleSolicitud;

    @Column(name = "estado_det_sol", length = 100)
    private String estadoDetSol;

    @Column(name = "fecha_actualizacion_det_sol")
    private LocalDate fechaActualizacionDetSol;

    @Column(name = "tipo_informe", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoInformeEnum tipoInforme;


    @OneToMany(mappedBy = "detalleSolicitud", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<FallaReportada> fallasReportadas;

    @OneToMany(mappedBy = "detalleSolicitud", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<AtencionSolicitud> atencionSolicitudes;

    @OneToMany(mappedBy = "detalleSolicitud", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<InformeEquipoEspecifico> informesEquiposEspecificos;



    @ManyToOne
    @JoinColumn(name = "id_activo")
    private Activo activo;

    @ManyToOne
    @JoinColumn(name = "id_solicitud")
    private Solicitud solicitud;
}
