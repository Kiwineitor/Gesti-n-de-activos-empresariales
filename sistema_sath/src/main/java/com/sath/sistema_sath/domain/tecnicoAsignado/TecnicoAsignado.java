package com.sath.sistema_sath.domain.tecnicoAsignado;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.administrativo.Administrativo;
import com.sath.sistema_sath.domain.solicitud.Solicitud;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "tecnico_asignado")
public class TecnicoAsignado extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTecnicoAsignado;

    @Column(name = "fecha_asignacion_tec_asig")
    private LocalDate fechaAsignacionTecAsig;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrativo")
    private Administrativo administrativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitud")
    private Solicitud solicitud;

}
