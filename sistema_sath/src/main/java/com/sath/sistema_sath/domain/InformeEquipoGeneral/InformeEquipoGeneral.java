package com.sath.sistema_sath.domain.InformeEquipoGeneral;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitud;
import com.sath.sistema_sath.domain.solicitud.Solicitud;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "informe_equipo_general")
public class InformeEquipoGeneral extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInformeEquipoGeneral;

    @Column(name = "nro_informe")
    private int nroInforme;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "trabajo_realizado", length = 500)
    private String ResumenTecnico;

    @Column(name= "fecha_cierre_informe")
    private LocalDate fechaCierreInformeGnr;


    @OneToOne
    @JoinColumn(name = "id_solicitud", unique = true) // unique garantiza el 1 a 1 en BD
    private Solicitud solicitud;
}
