package com.sath.sistema_sath.Controller.atencionSolicitud;

import com.sath.sistema_sath.domain.atencionSolicitud.AtencionSolicitudService;
import com.sath.sistema_sath.domain.atencionSolicitud.dto.AtencionSolicitudCreateDto;
import com.sath.sistema_sath.domain.atencionSolicitud.dto.AtencionSolicitudResponseDto;
import com.sath.sistema_sath.domain.atencionSolicitud.dto.AtencionSolicitudUpdateDto;
import com.sath.sistema_sath.domain.atencionSolicitud.dto.AtenderDetalleDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/atencion-solicitud")
@RequiredArgsConstructor
public class AtencionSolicitudController {

    private final AtencionSolicitudService service;



    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<AtencionSolicitudResponseDto> create(@Valid @RequestBody AtencionSolicitudCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<AtencionSolicitudResponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<AtencionSolicitudResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/actualizar/{id}")
    @ResponseBody
    public ResponseEntity<AtencionSolicitudResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody AtencionSolicitudUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/atender")
    @ResponseBody
    public ResponseEntity<AtencionSolicitudResponseDto> atender(@RequestBody AtenderDetalleDto dto,
                                                                Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.atender(dto, principal.getName()));
    }

    @GetMapping("/listar-atenciones")
    @ResponseBody
    public ResponseEntity<?> listarAtenciones() {
        try {
            List<Map<String, Object>> lista = service.listaAtenciones();
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/mis-atenciones")
    @ResponseBody
    public ResponseEntity<?> misAtenciones(Principal principal) {
        try {
            List<Map<String, Object>> lista = service.listaAtencionesPorTecnico(principal.getName());
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/para-asignar")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> listaParaAsignar() {
        return ResponseEntity.ok(service.listaParaAsignar());
    }

    @GetMapping("/listaParaElFrontend")
    @ResponseBody
    public ResponseEntity<?> listaParaElFrontend() {
        try {
            List<Map<String,Object>> lista = service.listaParaElFrontend();
            if (lista.isEmpty()){
                return ResponseEntity.noContent().build();
            }
            return  ResponseEntity.ok(lista);
        }catch (Exception e){
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al recuperar la lista del servdor");
            error.put("mensaje", e.getMessage());
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

}
