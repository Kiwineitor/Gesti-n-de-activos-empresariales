package com.sath.sistema_sath.domain.InformeEquipoEspecifico;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitud;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "informe_equipo_especifico")
public class InformeEquipoEspecifico extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInformeEquipoEspecifico;

    @Column(name = "nro_informe")
    private int nroInforme;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "trabajoRealizado", length = 500)
    private String trabajoRealizado;

    @Column(name = "detalle")
    private String detalle;

    @Column(name= "fechaCierreInforme")
    private LocalDate fechaCierreInformeEsp;


    @ManyToOne
    @JoinColumn(name = "id_detalle_solicitud")
    private DetalleSolicitud detalleSolicitud;


}
