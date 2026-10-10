package com.sath.sistema_sath.domain.rol;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.rolPermiso.RolPermiso;
import com.sath.sistema_sath.domain.usrRol.UsrRol;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "rol")
public class Rol extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRol;

    @NonNull
    @Column(name = "nombre_rol", length = 100)
    private String nombreRol;

    @Column(name = "descripcion_rol", length = 250)
    private String descripcionRol;


    @OneToMany(mappedBy = "rol")
    private List<UsrRol> usuarioRoles;



    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RolPermiso> rolPermisos;

}
