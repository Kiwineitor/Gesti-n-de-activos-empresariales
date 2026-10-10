package com.sath.sistema_sath.domain.notificacion;

import com.sath.sistema_sath.domain.notificacion.dto.NotificacionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository repository;

    private static final DateTimeFormatter FMT_HOY    = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FMT_HIST   = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    @Override
    @Transactional
    public void guardar(String tipo, String titulo, String descripcion) {
        Notificacion n = new Notificacion();
        n.setTipo(tipo);
        n.setTitulo(titulo);
        n.setDescripcion(descripcion);
        repository.save(n);
    }

    @Override
    public List<NotificacionDto> listar() {
        LocalDate hoy = LocalDate.now();
        return repository.findTop50ByOrderByFechaHoraDesc()
                .stream()
                .map(n -> new NotificacionDto(
                        n.getIdNotificacion(),
                        n.getTipo(),
                        n.getTitulo(),
                        n.getDescripcion(),
                        n.isLeida(),
                        n.getFechaHora().toLocalDate().equals(hoy)
                                ? n.getFechaHora().format(FMT_HOY)
                                : n.getFechaHora().format(FMT_HIST)
                ))
                .toList();
    }

    @Override
    @Transactional
    public void marcarLeida(Long id) {
        repository.findById(id).ifPresent(n -> {
            n.setLeida(true);
            repository.save(n);
        });
    }

    @Override
    @Transactional
    public void marcarTodasLeidas() {
        repository.marcarTodasLeidas();
    }
}