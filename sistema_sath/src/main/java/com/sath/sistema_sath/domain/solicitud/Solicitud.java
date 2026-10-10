package com.sath.sistema_sath.domain.solicitud;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.InformeEquipoGeneral.InformeEquipoGeneral;
import com.sath.sistema_sath.domain.administrativo.Administrativo;
import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitud;
import com.sath.sistema_sath.domain.evidenciaFoto.EvidenciaFoto;

import com.sath.sistema_sath.domain.solicitud.Enum.EstadoSolicitudEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "solicitud")
public class Solicitud extends Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_sol", length = 50, nullable = false)
    private EstadoSolicitudEnum estadoSol = EstadoSolicitudEnum.PENDIENTE;

    @Column(name = "nro_sol", length = 100, unique = true, nullable = false)
    private Long nroSol;

    @Column(name = "gestion_sol", length = 100)
    private int gestionSol;

    @Column(name = "fecha_sol")
    private LocalDate fechaSol;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "descripcion_sol", columnDefinition = "TEXT")
    private String descripcionSol;

    @Column(name = "nro_sol_externa", length = 50)
    private String nroSolExterna;

    @Column(name = "mant_id_londra")
    private Integer mantIdLondra;

    @Column(name = "observacion_ficha", columnDefinition = "TEXT")
    private String observacionFicha;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<DetalleSolicitud> detalleSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrativo")
    private Administrativo administrativo;
    // AGREGA esto
    @OneToOne(mappedBy = "solicitud", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InformeEquipoGeneral informeEquipoGeneral;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<EvidenciaFoto> evidenciaFotos;
}
