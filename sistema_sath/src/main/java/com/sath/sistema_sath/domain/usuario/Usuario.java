package com.sath.sistema_sath.domain.usuario;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.persona.Persona;
import com.sath.sistema_sath.domain.rol.Rol;
import com.sath.sistema_sath.domain.usrRol.UsrRol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(name = "username", length = 50, nullable = false)
    private String username;

    @Column(name = "password", length = 100,  nullable = false)
    private String password;



    @ManyToOne
    @JoinColumn(name = "id_persona")
    private Persona persona;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UsrRol> roles;


}
