package com.sath.sistema_sath.tools.reportes;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReporteGenericoService {

    private final ReporteGenericoRepository reporteRepository;

    public List<Map<String, Object>> obtenerDetalleSolicitud(String nroSol) {
        // Aquí podrías agregar validaciones antes de llamar al repo
        return reporteRepository.generarReporte(nroSol);
    }
}
