package com.sath.sistema_sath.domain.tipoServicio;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(10)
@RequiredArgsConstructor
public class TipoServicioSeeder implements CommandLineRunner {

    private final TipoServicioRepository repo;

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;

        List<String[]> tipos = List.of(
                new String[]{"PREVENTIVO",       "Mantenimiento programado del recurso"},
                new String[]{"CORRECTIVO",        "Reparación de fallas o daños en el recurso"},
                new String[]{"SERVICIO TÉCNICO",  "Servicios menores de soporte técnico"}
        );

        for (String[] t : tipos) {
            TipoServicio ts = new TipoServicio();
            ts.setNombreTipo(t[0]);
            ts.setDescripcionTipo(t[1]);
            repo.save(ts);
        }
    }
}
