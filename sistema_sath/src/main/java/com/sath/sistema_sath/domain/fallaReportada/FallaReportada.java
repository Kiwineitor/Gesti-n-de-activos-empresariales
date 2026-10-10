package com.sath.sistema_sath.domain.fallaReportada;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitud;
import com.sath.sistema_sath.domain.falla.Falla;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "falla_reportada")
public class FallaReportada extends Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFallaReportada;

    @Column(name = "descripcion_fall_rep", columnDefinition = "TEXT")
    private String descripcionFallRep;

    @ManyToOne
    @JoinColumn(name = "id_falla")
    private Falla falla;

    @ManyToOne
    @JoinColumn(name = "id_detalle_solicitud")
    private DetalleSolicitud detalleSolicitud;

}
