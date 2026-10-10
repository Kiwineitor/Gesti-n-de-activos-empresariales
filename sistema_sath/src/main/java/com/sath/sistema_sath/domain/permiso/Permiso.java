package com.sath.sistema_sath.domain.permiso;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.rolPermiso.RolPermiso;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "permiso")
public class Permiso extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPermiso;

    @NonNull
    @Column(name = "nombre_permiso", length = 100, unique = true, nullable = false)
    private String nombrePermiso; // Ej: "USUARIO_CREAR", "ROL_VER"

    @Column(name = "descripcion_permiso", length = 250)
    private String descripcionPermiso;

    @OneToMany(mappedBy = "permiso")
    private List<RolPermiso> rolPermisos;
}