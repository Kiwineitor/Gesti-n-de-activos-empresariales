package com.sath.sistema_sath.domain.persona;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.administrativo.Administrativo;
import com.sath.sistema_sath.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "persona")
public class Persona extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPersona;

    @Column(name = "ci_per", unique = true, length = 50)
    private String ciPer;

    @Column(name = "nombre_per", length = 100)
    private String nombrePer;

    @Column(name = "ap_paterno_per", length = 100)
    private String apPaternoPer;

    @Column(name = "ap_materno_per", length = 100)
    private String apMaternoPer;

    @Column(name = "celular_per", length = 20)
    private String nroCellPer;

    @Column(name = "correo_per", length = 100, unique = true)
    private String correoPer;



    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List <Usuario> usuarios;

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List <Administrativo> administrativos;

    // En la clase Persona.java
    public String getNombreCompleto() {
        if (nombrePer == null) return "";
        return String.format("%s %s %s", nombrePer, apPaternoPer, apMaternoPer).trim();
    }

}