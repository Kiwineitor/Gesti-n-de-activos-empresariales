package com.sath.sistema_sath.domain.servicio;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.atencionSolicitud.AtencionServicio;
import com.sath.sistema_sath.domain.tipoServicio.TipoServicio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "servicio")
public class Servicio extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idServicio;

    @Column(name = "nombre_serv", length = 100)
    private String nombreServ;

    @Column(name = "observaciones_serv", length = 500)
    private String observacionesServ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_servicio")
    private TipoServicio tipoServicio;

    @OneToMany(mappedBy = "servicio", fetch = FetchType.LAZY)
    List<AtencionServicio> atencionServicios;
}
