package com.sath.sistema_sath.domain.prioridad;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.atencionSolicitud.AtencionSolicitud;
import com.sath.sistema_sath.domain.prioridad.Enum.PrioridadEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "prioridad")
public class Prioridad extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPrioridad;

    @Enumerated(EnumType.STRING)
    @Column(name = "nombre_pri", length = 50)
    private PrioridadEnum nivel;
 
    @Column(name = "orden_pri")
    private Integer ordenPri;


    @OneToMany(mappedBy = "prioridad", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<AtencionSolicitud> atencionSolicitudes;

}
