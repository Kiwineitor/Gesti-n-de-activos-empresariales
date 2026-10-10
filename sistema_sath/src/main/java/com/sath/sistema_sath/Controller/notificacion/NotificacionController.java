package com.sath.sistema_sath.Controller.notificacion;

import com.sath.sistema_sath.domain.notificacion.NotificacionService;
import com.sath.sistema_sath.domain.notificacion.dto.NotificacionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService service;

    @GetMapping
    public ResponseEntity<List<NotificacionDto>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id) {
        service.marcarLeida(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/leer-todas")
    public ResponseEntity<Void> marcarTodasLeidas() {
        service.marcarTodasLeidas();
        return ResponseEntity.noContent().build();
    }
}