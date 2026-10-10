package com.sath.sistema_sath.domain.usrRol.dto;

import com.sath.sistema_sath.domain.usrRol.UsrRol;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

/**
 * DTO for {@link UsrRol}
 */
@Value
@Getter
@Setter
public class UsrRolDto  {
    private Long idUsrRol;
    private Long usuarioId;
    private Long rolId;
}