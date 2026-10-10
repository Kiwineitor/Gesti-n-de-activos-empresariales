package com.sath.sistema_sath.domain.usrPermiso;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.permiso.Permiso;
import com.sath.sistema_sath.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "usr_permiso",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_permiso"}))
public class UsrPermiso extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsrPermiso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_permiso", nullable = false)
    private Permiso permiso;
}
