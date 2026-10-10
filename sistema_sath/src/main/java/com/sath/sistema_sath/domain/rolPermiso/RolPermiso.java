package com.sath.sistema_sath.domain.rolPermiso;

import com.sath.sistema_sath.Config.Auditoria.Auditoria;
import com.sath.sistema_sath.domain.permiso.Permiso;
import com.sath.sistema_sath.domain.rol.Rol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "rol_permiso",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_rol", "id_permiso"}))
public class RolPermiso extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRolPermiso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_permiso", nullable = false)
    private Permiso permiso;
}