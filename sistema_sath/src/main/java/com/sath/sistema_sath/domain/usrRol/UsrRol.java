package com.sath.sistema_sath.domain.usrRol;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.rol.Rol;
import com.sath.sistema_sath.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "UsrRol")
public class UsrRol extends Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsrRol;


    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_rol")
    private Rol rol;


}
