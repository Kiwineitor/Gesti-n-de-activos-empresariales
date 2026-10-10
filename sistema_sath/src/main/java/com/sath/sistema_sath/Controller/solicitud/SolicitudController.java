package com.sath.sistema_sath.Controller.solicitud;

import com.sath.sistema_sath.domain.solicitud.Enum.EstadoSolicitudEnum;
import com.sath.sistema_sath.domain.solicitud.ModoAutoAsignarService;
import com.sath.sistema_sath.domain.solicitud.SolicitudService;
import org.springframework.security.access.prepost.PreAuthorize;
import com.sath.sistema_sath.domain.solicitud.dto.CambiarEstadoSolicitudDto;

import com.sath.sistema_sath.domain.solicitud.dto.SolicitudCreateDto;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudDetalleResponseDto;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudResponseDto;
import com.sath.sistema_sath.domain.solicitud.dto.SolicitudUpdateDto;
import com.sath.sistema_sath.domain.solicitud.manual.SolicitudManualService;
import com.sath.sistema_sath.domain.solicitud.manual.SolicitudManualCreateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/solicitud")
public class SolicitudController {

    private final SolicitudService service;
    private final SolicitudManualService manualService;
    private final ModoAutoAsignarService modoAutoAsignarService;


    @GetMapping("/detalle/{id}")
    @ResponseBody
    public ResponseEntity<SolicitudDetalleResponseDto> getDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDetalle(id));
    }

    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<SolicitudResponseDto> create(@RequestBody SolicitudCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/listar")
    @ResponseBody
    public ResponseEntity<List<SolicitudResponseDto>> findAll(
            @RequestParam(required = false) String estado) {
        if (estado != null && !estado.isBlank()) {
            EstadoSolicitudEnum estadoEnum = EstadoSolicitudEnum.valueOf(estado);
            return ResponseEntity.ok(service.findAllByEstado(estadoEnum));
        }
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<SolicitudResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/mis-solicitudes")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> misSolicitudes(Principal principal) {
        return ResponseEntity.ok(service.listaSolicitudPorTecnico(principal.getName()));
    }

    @GetMapping("/lista-finalizados")
    @ResponseBody
    public List<Map<String, Object>> listaSolicitudFinalizados() {
//        log.info("Lista de solicitudes finalizadas" + service.listaSolicitudFinalizado());
        return service.listaSolicitudFinalizado();
    }

    @GetMapping("/lista-servicio-tecnico")
    @ResponseBody
    public List<Map<String, Object>> listaSolicitudServicioTecnico() {
        return service.listaSolicitudServicioTecnico();
    }

    @PutMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<SolicitudResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudUpdateDto dto
    ) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estados")
    @ResponseBody
    public ResponseEntity<EstadoSolicitudEnum[]> listarEstados() {
        return ResponseEntity.ok(EstadoSolicitudEnum.values());
    }

    @PatchMapping("/{id}/estado")
    @ResponseBody
    public ResponseEntity<String> cambiarEstado(
            @PathVariable Long id,
            @RequestBody CambiarEstadoSolicitudDto dto
    ) {
        service.cambiarEstado(id, dto);
        return ResponseEntity.ok("Estado actualizado correctamente");
    }

    @PostMapping("/manual")
    @ResponseBody
    public ResponseEntity<SolicitudResponseDto> registrarManual(
            @RequestBody SolicitudManualCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(manualService.registrar(dto));
    }

    @GetMapping("/modo-auto-asignar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getModo() {
        return ResponseEntity.ok(Map.of("activo", modoAutoAsignarService.isActivo()));
    }

    @PostMapping("/modo-auto-asignar/toggle")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DEVELOPER')")
    public ResponseEntity<Map<String, Object>> toggleModo() {
        return ResponseEntity.ok(Map.of("activo", modoAutoAsignarService.toggle()));
    }

}
