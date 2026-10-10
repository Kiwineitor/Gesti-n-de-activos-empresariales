package com.sath.sistema_sath.domain.administrativo;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.cargo.Cargo;
import com.sath.sistema_sath.domain.persona.Persona;
import com.sath.sistema_sath.domain.solicitud.Solicitud;
import com.sath.sistema_sath.domain.tecnicoAsignado.TecnicoAsignado;
import com.sath.sistema_sath.domain.unidad.Unidad;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "administrativo")
public class Administrativo extends Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAdministrativo;

    @Column(name = "codigo_admi")
    private int codigoAdmi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona")
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad")
    private Unidad unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cargo")
    private Cargo cargo;

    @OneToMany(mappedBy = "administrativo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Solicitud> solicitudes;

    @OneToMany(mappedBy = "administrativo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TecnicoAsignado> tecnicosAsignados;

}
