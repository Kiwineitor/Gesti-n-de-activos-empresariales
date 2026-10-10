package com.sath.sistema_sath.domain.activo;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.detalleSolicitud.DetalleSolicitud;
import com.sath.sistema_sath.domain.tipoActivo.TipoActivo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "activo")
public class Activo extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idActivo;

    @Column(name = "codigo_act", length = 100)
    private String codigoAct;

    @Column(name = "descripcion_act", length = 250)
    private String descripcionAct;

    @OneToMany(mappedBy = "activo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<DetalleSolicitud> detallesSolicitudes;

    @ManyToOne
    @JoinColumn(name = "id_tipo_activo")
    private TipoActivo tipoActivo;
}
