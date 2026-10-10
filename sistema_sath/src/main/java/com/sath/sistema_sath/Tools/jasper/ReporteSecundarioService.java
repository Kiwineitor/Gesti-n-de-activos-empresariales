package com.sath.sistema_sath.tools.jasper;

import com.sath.sistema_sath.Config.Exception.ReglaDeNegocioException;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ReporteSecundarioService {

    private final DataSource dataSource;

    public ReporteSecundarioService(@Qualifier("secondaryDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ── Utilidad ──────────────────────────────────────────────────────────────

    private byte[] compilarYExportar(String jrxmlPath, Map<String, Object> params) throws Exception {
        InputStream stream = getClass().getResourceAsStream(jrxmlPath);
        if (stream == null) throw new RuntimeException("No se encontró el reporte: " + jrxmlPath);

        JasperReport jasperReport = JasperCompileManager.compileReport(stream);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Connection con = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(jasperReport, params, con);
            if (print.getPages().isEmpty()) {
                throw new ReglaDeNegocioException(
                        "No se encontraron datos para los filtros seleccionados. Intente con otro rango de fechas o diferentes filtros.");
            }
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
            exporter.exportReport();
        }
        return out.toByteArray();
    }

    private InputStream logoUap() {
        return getClass().getResourceAsStream("/imagesForReports/Logo_UAP.png");
    }

    private InputStream logoUsic() {
        return getClass().getResourceAsStream("/imagesForReports/usic2.png");
    }

    // ── Reportes ─────────────────────────────────────────────────────────────

    public byte[] exportarReporteGeneral(Date fechaInicial, Date fechaFinal, Long idUsuario) throws Exception {
        log.info(">>> [Legacy] Reporte_General_SATH idUsuario={}", idUsuario);
        Map<String, Object> params = new HashMap<>();
        params.put("fecha_inicial", fechaInicial);
        params.put("fecha_final",   fechaFinal);
        params.put("id_usuario",    idUsuario);
        params.put("logoUAP",       logoUap());
        params.put("logoUSIC",      logoUsic());
        return compilarYExportar("/JaspertReport/Reporte_General_SATH.jrxml", params);
    }

    public byte[] exportarReportePersonalizado(Date fechaInicial, Date fechaFinal,
                                               Long idUsuario, Long idTipoServicio,
                                               Long idTipoEquipo, Long idUnidad) throws Exception {
        log.info(">>> [Legacy] Reporte_Personalizado_SATH idUsuario={}", idUsuario);
        Map<String, Object> params = new HashMap<>();
        params.put("fecha_inicial",    fechaInicial);
        params.put("fecha_final",      fechaFinal);
        params.put("id_usuario",       idUsuario);
        params.put("id_tipo_servicio", idTipoServicio);
        params.put("id_tipoequipo",    idTipoEquipo);
        params.put("id_unidad",        idUnidad);
        params.put("logoUAP",          logoUap());
        params.put("logoUSIC",         logoUsic());
        return compilarYExportar("/JaspertReport/Reporte_Personalizado_SATH.jrxml", params);
    }

    public byte[] exportarReportePersonalizado2(Date fechaInicial, Date fechaFinal,
                                                Long idUsuario, Long idTipoServicio,
                                                Long idTipoEquipo) throws Exception {
        log.info(">>> [Legacy] Reporte_Personalizado_SATH_2 idUsuario={}", idUsuario);
        Map<String, Object> params = new HashMap<>();
        params.put("fecha_inicial",    fechaInicial);
        params.put("fecha_final",      fechaFinal);
        params.put("id_usuario",       idUsuario);
        params.put("id_tipo_servicio", idTipoServicio);
        params.put("id_tipoequipo",    idTipoEquipo);
        params.put("logoUAP",          logoUap());
        params.put("logoUSIC",         logoUsic());
        return compilarYExportar("/JaspertReport/Reporte_Personalizado_SATH_2.jrxml", params);
    }

    public byte[] exportarReportePersonalizado3(Date fechaInicial, Date fechaFinal,
                                                Long idUsuario, Long idTipoServicio) throws Exception {
        log.info(">>> [Legacy] Reporte_Personalizado_SATH_3 idUsuario={}", idUsuario);
        Map<String, Object> params = new HashMap<>();
        params.put("fecha_inicial",    fechaInicial);
        params.put("fecha_final",      fechaFinal);
        params.put("id_usuario",       idUsuario);
        params.put("id_tipo_servicio", idTipoServicio);
        params.put("logoUAP",          logoUap());
        params.put("logoUSIC",         logoUsic());
        return compilarYExportar("/JaspertReport/Reporte_Personalizado_SATH_3.jrxml", params);
    }

    public byte[] exportarReportePersonalizado4(Date fechaInicial, Date fechaFinal,
                                                Long idUsuario, Long idTipoEquipo) throws Exception {
        log.info(">>> [Legacy] Reporte_Personalizado_SATH_4 idUsuario={}", idUsuario);
        Map<String, Object> params = new HashMap<>();
        params.put("fecha_inicial",   fechaInicial);
        params.put("fecha_final",     fechaFinal);
        params.put("id_usuario",      idUsuario);
        params.put("id_tipoequipo",   idTipoEquipo);
        params.put("logoUAP",         logoUap());
        params.put("logoUSIC",        logoUsic());
        return compilarYExportar("/JaspertReport/Reporte_Personalizado_SATH_4.jrxml", params);
    }

    public byte[] exportarReporteServiciosUsuario(Long idUsuario,
                                                   Date fechaInicial, Date fechaFinal) throws Exception {
        log.info(">>> [Legacy] Reporte_Servicios_Usuario idUsuario={}", idUsuario);
        Map<String, Object> params = new HashMap<>();
        params.put("id_usuario",   idUsuario);
        params.put("fecha_inicial", fechaInicial);
        params.put("fecha_final",   fechaFinal);
        params.put("logoUAP",       logoUap());
        params.put("logoUSIC",      logoUsic());
        return compilarYExportar("/JaspertReport/Reporte_Servicios_Usuario.jrxml", params);
    }
}
