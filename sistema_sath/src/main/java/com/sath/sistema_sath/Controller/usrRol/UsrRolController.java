package com.sath.sistema_sath.Controller.usrRol;

import com.sath.sistema_sath.domain.rolPermiso.dto.RolPermisoResponseDto;
import com.sath.sistema_sath.domain.usrRol.UsrRolService;
import com.sath.sistema_sath.domain.usrRol.dto.AsignarRolDto;
import com.sath.sistema_sath.domain.usrRol.dto.UsrRolDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/usr-rol")
public class UsrRolController {

    private final UsrRolService service;

    @PostMapping("/asignar")
    @ResponseBody
    public ResponseEntity<UsrRolDto> asignar(@Valid @RequestBody AsignarRolDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.asignarRol(dto));
    }

    @DeleteMapping("/revocar/{id}")
    @ResponseBody
    public ResponseEntity<Void> revocar(@PathVariable Long id) {
        service.revocarRol(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles/{idUsuario}")
    @ResponseBody
    public ResponseEntity<List<UsrRolDto>> findRoles(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(service.findRolesByUsuario(idUsuario));
    }

    @GetMapping("/permisos/{idUsuario}")
    @ResponseBody
    public ResponseEntity<List<RolPermisoResponseDto>> findPermisos(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(service.findPermisosByUsuario(idUsuario));
    }

    @GetMapping("/lista")
    @ResponseBody
    public List<Map<String, Object>> listaUsrRol() {
        return service.listaUsrRol();
    }
}