package com.sath.sistema_sath.tools.jasper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sath.sistema_sath.domain.configuracion.ConfiguracionService;
import com.sath.sistema_sath.domain.configuracion.ConfiguracionServiceImpl;
import com.sath.sistema_sath.domain.evidenciaFoto.EvidenciaFotoService;
import com.sath.sistema_sath.domain.solicitud.SolicitudRepository;
import com.sath.sistema_sath.tools.reportes.ReporteGenericoRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.base.JRBasePrintImage;
import net.sf.jasperreports.engine.base.JRBasePrintPage;
import net.sf.jasperreports.engine.base.JRBasePrintText;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ScaleImageEnum;
import net.sf.jasperreports.renderers.SimpleDataRenderer;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleDocxReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.pdf.JRPdfExporter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.sql.DataSource;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final DataSource dataSource;
    private final ReporteGenericoRepository reporteGenericoRepository;
    private final EvidenciaFotoService evidenciaFotoService;
    private final SolicitudRepository solicitudRepository;
    private final ConfiguracionService configuracionService;

    // Caché de reportes compilados — la compilación JRXML solo ocurre una vez por reporte
    private final ConcurrentHashMap<String, JasperReport> reporteCache = new ConcurrentHashMap<>();
    // Caché de imágenes — se leen una sola vez del classpath
    private final ConcurrentHashMap<String, byte[]> imagenCache = new ConcurrentHashMap<>();

    private JasperReport getReporte(String resourcePath) throws JRException {
        return reporteCache.computeIfAbsent(resourcePath, path -> {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is == null) throw new RuntimeException("No se encontró el reporte: " + path);
                return JasperCompileManager.compileReport(is);
            } catch (Exception e) {
                throw new RuntimeException("Error compilando reporte: " + path, e);
            }
        });
    }

    private InputStream getImagen(String resourcePath) {
        byte[] bytes = imagenCache.computeIfAbsent(resourcePath, path -> {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is == null) return new byte[0];
                return is.readAllBytes();
            } catch (Exception e) {
                return new byte[0];
            }
        });
        return bytes.length == 0 ? null : new ByteArrayInputStream(bytes);
    }

    private String obtenerAutorizadoPor() {
        return configuracionService.obtenerValor(
                ConfiguracionServiceImpl.CLAVE_NOMBRE_AUTORIZADO,
                ConfiguracionServiceImpl.NOMBRE_AUTORIZADO_POR_DEFECTO);
    }

    private String obtenerCargoAutorizado() {
        return configuracionService.obtenerValor(
                ConfiguracionServiceImpl.CLAVE_CARGO_AUTORIZADO,
                ConfiguracionServiceImpl.CARGO_AUTORIZADO_POR_DEFECTO);
    }

    private String obtenerContenidoQRAutorizacion() {
        return "Autorizado por: " + obtenerAutorizadoPor() + "\nCargo: " + obtenerCargoAutorizado();
    }

    private List<JRPrintPage> filtrarPaginasConContenido(List<JRPrintPage> paginas) {
        return paginas.stream()
                .filter(page -> page.getElements() != null && !page.getElements().isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }


    public byte[] exportarInformeEspecifico(Long idInformeEspecifico) throws Exception {
        JasperReport jasperReport = getReporte("/reports/InformeEspecifico.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("IdInformeEspecifico", idInformeEspecifico);
        params.put("logoUap", getImagen("/imagesForReports/Logo_UAP.png"));
        params.put("Dtic", getImagen("/imagesForReports/Dtic.png"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Connection con = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(jasperReport, params, con);
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
            exporter.exportReport();
        }
        return out.toByteArray();
    }

    public byte[] exportarInformeGeneral(Long idInformeGeneral) throws Exception {
        JasperReport jasperReport = getReporte("/reports/InformeGeneral.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("IdInformeGeneral", idInformeGeneral);
        params.put("logoUap", getImagen("/imagesForReports/Logo_UAP.png"));
        params.put("Dtic", getImagen("/imagesForReports/Dtic.png"));
        params.put("Membrete_Informes", getImagen("/imagesForReports/Membrete_Informes.jpeg"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Connection con = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(jasperReport, params, con);
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
            exporter.exportReport();
        }
        return out.toByteArray();
    }

    public byte[] exportarReporteGeneral(java.util.Date fechaInicial, java.util.Date fechaFinal, Long idAdministrativo) throws Exception {
        JasperReport jasperReport = getReporte("/reports/Reporte_General_SATHv2.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("fecha_inicial", fechaInicial);
        params.put("fecha_final", fechaFinal);
        params.put("id_administrativo", idAdministrativo);
        params.put("logoUap", getImagen("/imagesForReports/Logo_UAP.png"));
        params.put("Dtic", getImagen("/imagesForReports/Dtic.png"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Connection con = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(jasperReport, params, con);
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
            exporter.exportReport();
        }
        return out.toByteArray();
    }

    public byte[] exportarReporteGeneralExcel(java.util.Date fechaInicial, java.util.Date fechaFinal, Long idAdministrativo) throws Exception {
        final String SQL = """
                SELECT DISTINCT
                    COALESCE(p_adm.nombre_per || ' ' || p_adm.ap_paterno_per || ' ' || COALESCE(p_adm.ap_materno_per, ''), 'Sistema SATH') AS nom_admin,
                    so.nro_sol                                                                       AS nro_sol,
                    p_sol.nombre_per || ' ' || p_sol.ap_paterno_per || ' ' || COALESCE(p_sol.ap_materno_per, '') AS solicitante,
                    COALESCE(u_sol.nombre_uni, '—')                                                  AS unidad,
                    COALESCE(c_sol.nombre_car, '—')                                                  AS cargo,
                    COALESCE(a.codigo_act, '—')                                                      AS cod_equipo,
                    COALESCE(tip.nombre_tipo_act, '—')                                               AS equipo,
                    COALESCE(a.descripcion_act, '—')                                                 AS descripcion,
                    COALESCE(srv.nombre_serv, 'MANTENIMIENTO CORRECTIVO')                            AS tipo_servicio,
                    so.fecha_sol                                                                     AS fecha_recepcion,
                    aten.fecha_fin_reparacion_aten_sol                                               AS fecha_entrega
                FROM solicitud so
                LEFT JOIN administrativo adm_sol  ON so.id_administrativo = adm_sol.id_administrativo
                LEFT JOIN persona p_sol           ON adm_sol.id_persona = p_sol.id_persona
                LEFT JOIN unidad u_sol            ON adm_sol.id_unidad = u_sol.id_unidad
                LEFT JOIN cargo c_sol             ON adm_sol.id_cargo = c_sol.id_cargo
                LEFT JOIN detalle_solicitud ds    ON so.id_solicitud = ds.id_solicitud AND ds._estado_aud <> 'ELIMINADO'
                LEFT JOIN activo a                ON ds.id_activo = a.id_activo
                LEFT JOIN tipo_activo tip         ON a.id_tipo_activo = tip.id_tipo_activo
                LEFT JOIN atencion_solicitud aten ON ds.id_detalle_solicitud = aten.id_detalle_solicitud AND aten._estado_aud <> 'ELIMINADO'
                LEFT JOIN atencion_servicio aserv ON aten.id_atencion_solicitud = aserv.id_atencion_solicitud AND aserv._estado_aud <> 'ELIMINADO'
                LEFT JOIN servicio srv            ON aserv.id_servicio = srv.id_servicio
                LEFT JOIN administrativo adm_rep  ON adm_rep.id_administrativo = ?
                LEFT JOIN persona p_adm           ON adm_rep.id_persona = p_adm.id_persona
                WHERE so.estado_sol = 'FINALIZADO'
                  AND so._estado_aud <> 'ELIMINADO'
                  AND so.fecha_sol BETWEEN ? AND ?
                ORDER BY so.fecha_sol
                """;

        record Fila(String admin, long nroSol, String solicitante, String unidad, String cargo,
                    String codEquipo, String equipo, String descripcion, String tipoServicio,
                    java.sql.Date fechaRecepcion, java.sql.Date fechaEntrega) {}

        List<Fila> filas = new ArrayList<>();
        java.sql.Date desde = new java.sql.Date(fechaInicial.getTime());
        java.sql.Date hasta = new java.sql.Date(fechaFinal.getTime());
        long idAdm = idAdministrativo != null ? idAdministrativo : 0L;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL)) {
            ps.setLong(1, idAdm); ps.setDate(2, desde); ps.setDate(3, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) filas.add(new Fila(
                        rs.getString("nom_admin"), rs.getLong("nro_sol"),
                        rs.getString("solicitante"), rs.getString("unidad"), rs.getString("cargo"),
                        rs.getString("cod_equipo"), rs.getString("equipo"), rs.getString("descripcion"),
                        rs.getString("tipo_servicio"), rs.getDate("fecha_recepcion"), rs.getDate("fecha_entrega")));
            }
        }

        String adminNombre = filas.isEmpty() ? "Sistema SATH" : filas.getFirst().admin();
        String periodo = new java.text.SimpleDateFormat("dd-MM-yyyy").format(fechaInicial)
                       + " al " + new java.text.SimpleDateFormat("dd-MM-yyyy").format(fechaFinal);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            Font fBold   = wb.createFont(); fBold.setBold(true);
            Font fBoldLg = wb.createFont(); fBoldLg.setBold(true); fBoldLg.setFontHeightInPoints((short) 12);
            Font fSmall  = wb.createFont(); fSmall.setFontHeightInPoints((short) 9);
            short dateFmt = wb.getCreationHelper().createDataFormat().getFormat("dd-mm-yyyy");

            CellStyle csTitle = wb.createCellStyle();
            csTitle.setFont(fBoldLg); csTitle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle csMeta = wb.createCellStyle(); csMeta.setFont(fBold);

            CellStyle csHdr = wb.createCellStyle();
            csHdr.setFont(fBold);
            csHdr.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            csHdr.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            csHdr.setAlignment(HorizontalAlignment.CENTER);
            csHdr.setWrapText(true);
            csHdr.setBorderBottom(BorderStyle.THIN); csHdr.setBorderTop(BorderStyle.THIN);
            csHdr.setBorderLeft(BorderStyle.THIN);  csHdr.setBorderRight(BorderStyle.THIN);

            CellStyle csData = wb.createCellStyle();
            csData.setFont(fSmall);
            csData.setBorderBottom(BorderStyle.THIN); csData.setBorderTop(BorderStyle.THIN);
            csData.setBorderLeft(BorderStyle.THIN);  csData.setBorderRight(BorderStyle.THIN);

            CellStyle csDate = wb.createCellStyle();
            csDate.setFont(fSmall); csDate.setDataFormat(dateFmt);
            csDate.setAlignment(HorizontalAlignment.CENTER);
            csDate.setBorderBottom(BorderStyle.THIN); csDate.setBorderTop(BorderStyle.THIN);
            csDate.setBorderLeft(BorderStyle.THIN);  csDate.setBorderRight(BorderStyle.THIN);

            CellStyle csTotal = wb.createCellStyle();
            csTotal.setFont(fBold);
            csTotal.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            csTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            csTotal.setBorderBottom(BorderStyle.MEDIUM); csTotal.setBorderTop(BorderStyle.MEDIUM);
            csTotal.setBorderLeft(BorderStyle.MEDIUM);  csTotal.setBorderRight(BorderStyle.MEDIUM);

            XSSFSheet sh = wb.createSheet("Reporte General");
            sh.setColumnWidth(0,  4 * 256);   // N°
            sh.setColumnWidth(1, 28 * 256);   // Solicitante
            sh.setColumnWidth(2, 28 * 256);   // Unidad
            sh.setColumnWidth(3, 22 * 256);   // Cargo
            sh.setColumnWidth(4, 14 * 256);   // Cód. Equipo
            sh.setColumnWidth(5, 20 * 256);   // Tipo Equipo
            sh.setColumnWidth(6, 30 * 256);   // Descripción
            sh.setColumnWidth(7, 22 * 256);   // Tipo Servicio
            sh.setColumnWidth(8, 14 * 256);   // F. Recepción
            sh.setColumnWidth(9, 14 * 256);   // F. Entrega

            int r = 0;
            // Título
            Cell ct = sh.createRow(r++).createCell(0);
            ct.setCellValue("REPORTE GENERAL — SISTEMA DE ATENCIÓN DE HARDWARE (SATH)");
            ct.setCellStyle(csTitle);
            sh.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            // Metadatos
            Row rm1 = sh.createRow(r++);
            Cell cm1 = rm1.createCell(0); cm1.setCellValue("Periodo:"); cm1.setCellStyle(csMeta);
            rm1.createCell(1).setCellValue(periodo);
            Row rm2 = sh.createRow(r++);
            Cell cm2 = rm2.createCell(0); cm2.setCellValue("Responsable:"); cm2.setCellStyle(csMeta);
            rm2.createCell(1).setCellValue(adminNombre);
            r++;

            // Encabezados de columna
            sh.createRow(r).setHeightInPoints(28);
            Row rh = sh.getRow(r++);
            String[] hdrs = {"N°", "NOMBRE SOLICITANTE", "UNIDAD", "CARGO",
                             "CÓD. EQUIPO", "TIPO EQUIPO", "DESCRIPCIÓN", "TIPO SERVICIO",
                             "F. RECEPCIÓN", "F. ENTREGA"};
            for (int c = 0; c < hdrs.length; c++) {
                Cell hc = rh.createCell(c); hc.setCellValue(hdrs[c]); hc.setCellStyle(csHdr);
            }

            // Filas de datos
            int fila = 1;
            for (Fila f : filas) {
                Row rd = sh.createRow(r++);
                Cell c0 = rd.createCell(0); c0.setCellValue(fila++);             c0.setCellStyle(csData);
                Cell c1 = rd.createCell(1); c1.setCellValue(f.solicitante());    c1.setCellStyle(csData);
                Cell c2 = rd.createCell(2); c2.setCellValue(f.unidad());         c2.setCellStyle(csData);
                Cell c3 = rd.createCell(3); c3.setCellValue(f.cargo());          c3.setCellStyle(csData);
                Cell c4 = rd.createCell(4); c4.setCellValue(f.codEquipo());      c4.setCellStyle(csData);
                Cell c5 = rd.createCell(5); c5.setCellValue(f.equipo());         c5.setCellStyle(csData);
                Cell c6 = rd.createCell(6); c6.setCellValue(f.descripcion());    c6.setCellStyle(csData);
                Cell c7 = rd.createCell(7); c7.setCellValue(f.tipoServicio());   c7.setCellStyle(csData);
                Cell c8 = rd.createCell(8);
                if (f.fechaRecepcion() != null) { c8.setCellValue(f.fechaRecepcion()); c8.setCellStyle(csDate); }
                else c8.setCellStyle(csData);
                Cell c9 = rd.createCell(9);
                if (f.fechaEntrega() != null) { c9.setCellValue(f.fechaEntrega()); c9.setCellStyle(csDate); }
                else c9.setCellStyle(csData);
            }

            // Total
            r++;
            Row rTot = sh.createRow(r++);
            Cell ctL = rTot.createCell(0); ctL.setCellValue("TOTAL DE SERVICIOS ATENDIDOS:"); ctL.setCellStyle(csTotal);
            sh.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 8));
            Cell ctV = rTot.createCell(9); ctV.setCellValue(filas.size()); ctV.setCellStyle(csTotal);

            wb.write(out);
        }
        return out.toByteArray();
    }

    public byte[] exportarReportePersonalizado(java.util.Date fechaInicial, java.util.Date fechaFinal,
                                               Long idUnidad, Long idTipoServicio, Long idTipoActivo,
                                               Long idAdministrativo) throws Exception {
        JasperReport jasperReport = getReporte("/reports/Reporte_Personalizado_SATHv7.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("fecha_inicial", fechaInicial);
        params.put("fecha_final", fechaFinal);
        params.put("id_unidad", idUnidad);
        params.put("id_tipo_servicio", idTipoServicio);
        params.put("id_tipo_activo", idTipoActivo);
        params.put("id_administrativo", idAdministrativo);
        params.put("logoUap", getImagen("/imagesForReports/Logo_UAP.png"));
        params.put("Dtic", getImagen("/imagesForReports/Dtic.png"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Connection con = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(jasperReport, params, con);
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
            exporter.exportReport();
        }
        return out.toByteArray();
    }

    public byte[] exportarReportePersonalizadoExcel(java.util.Date fechaInicial, java.util.Date fechaFinal,
                                                    Long idUnidad, Long idTipoServicio, Long idTipoActivo,
                                                    Long idAdministrativo) throws Exception {
        JasperReport jasperReport = getReporte("/reports/Reporte_Personalizado_SATHv7.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("fecha_inicial", fechaInicial);
        params.put("fecha_final", fechaFinal);
        params.put("id_unidad", idUnidad);
        params.put("id_tipo_servicio", idTipoServicio);
        params.put("id_tipo_activo", idTipoActivo);
        params.put("id_administrativo", idAdministrativo);
        params.put("logoUap", getImagen("/imagesForReports/Logo_UAP.png"));
        params.put("Dtic", getImagen("/imagesForReports/Dtic.png"));
        params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Connection con = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(jasperReport, params, con);

            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));

            SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
            config.setOnePagePerSheet(false);
            config.setRemoveEmptySpaceBetweenRows(true);
            config.setRemoveEmptySpaceBetweenColumns(true);
            config.setDetectCellType(true);
            config.setWhitePageBackground(false);
            config.setIgnorePageMargins(true);
            exporter.setConfiguration(config);

            exporter.exportReport();
        }
        return out.toByteArray();
    }

    public byte[] generarQRComoBytes(String contenido, int ancho, int alto) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(contenido, BarcodeFormat.QR_CODE, ancho, alto);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);

        return out.toByteArray();
    }

    public byte[] exportarFichaTecnicaConQR(Long nroSolicitud, String observacion,
                                            Long idUsuario, String nombreTecnico, boolean varAdmin) throws Exception {

        String ruta = varAdmin ? "/reports/FichaTecnicav3Admin.jrxml" : "/reports/FichaTecnicav3.jrxml";
        JasperReport jasperReport = getReporte(ruta);

        // ── Generar QR de Autorización ──────────────────────────────────────
        String contenidoQRAutorizacion = obtenerContenidoQRAutorizacion();

        byte[] bytesQRAutorizacion = generarQRComoBytes(contenidoQRAutorizacion, 150, 150);
//        InputStream qrAutorizacionStream = new ByteArrayInputStream(bytesQRAutorizacion);

        // ── Generar QR de Verificación ──────────────────────────────────────
        String contenidoQRVerificacion =
                "Verificado por el Ing. " + nombreTecnico + "\n" +
                        "Técnico Colaborador";

        byte[] bytesQRVerificacion = generarQRComoBytes(contenidoQRVerificacion, 150, 150);
//        InputStream qrVerificacionStream = new ByteArrayInputStream(bytesQRVerificacion);

        // ── Armar parámetros ────────────────────────────────────────────────
        List<String> codigos = obtenerCodigosActivo(nroSolicitud);
        if (codigos.isEmpty()) throw new RuntimeException("No hay activos para la solicitud " + nroSolicitud);

        List<JasperPrint> prints = new ArrayList<>();

        try (Connection con = dataSource.getConnection()) {
            for (String codigo : codigos) {
                Map<String, Object> params = new HashMap<>();
                params.put("NumSol", nroSolicitud);
                params.put("CodigoActivo", codigo);
                params.put("logoUap", getImagen("/imagesForReports/Logo_UAP.png"));
                params.put("Dtic", getImagen("/imagesForReports/Dtic.png"));
                params.put("Membrete_Informes", getImagen("/imagesForReports/Membrete_Informes.jpeg"));
                params.put("QRAutorizacion", bytesQRAutorizacion);
                params.put("QRVerificacion", bytesQRVerificacion);
                params.put("AutorizadoPor", obtenerAutorizadoPor());
                params.put("CargoAutorizado", obtenerCargoAutorizado());

                if (observacion != null && !observacion.isBlank()) {
                    params.put("Observacion", observacion.trim());
                }

                List<String> servicios = reporteGenericoRepository.obtenerServiciosPorActivo(nroSolicitud, codigo);
                int mitad = (int) Math.ceil(servicios.size() / 2.0);

                String serviciosIzquierda = servicios.subList(0, mitad)
                        .stream().map(s -> "• " + s).collect(Collectors.joining("\n"));
                String serviciosDerecha = servicios.subList(mitad, servicios.size())
                        .stream().map(s -> "• " + s).collect(Collectors.joining("\n"));

                params.put("ServiciosIzquierda", serviciosIzquierda);
                params.put("ServiciosDerecha", serviciosDerecha);
                params.put("tipoServicios", reporteGenericoRepository.obtenerServiciosPorTipoPorActivo(nroSolicitud, codigo));

                JasperPrint print = JasperFillManager.fillReport(jasperReport, params, con);
                prints.add(print);
            }
        }

        // ── Fusionar y exportar (igual que tu código actual) ────────────────
        JasperPrint base = prints.get(0);
        base.getPages().removeIf(page -> page.getElements() == null || page.getElements().isEmpty());

        for (int i = 1; i < prints.size(); i++) {
            for (JRPrintPage page : prints.get(i).getPages()) {
                if (page.getElements() != null && !page.getElements().isEmpty()) {
                    base.addPage(page);
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(base));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        exporter.exportReport();

        Long idSolicitud = obtenerIdSolicitudDesdeNro(nroSolicitud);
        if (idSolicitud == null) return out.toByteArray();
        guardarObservacionSolicitud(nroSolicitud, observacion);
        return appendarFotosAlPdf(out.toByteArray(), idSolicitud);
    }

    // Agrega páginas de evidencias fotográficas al JasperPrint — funciona tanto para PDF como para Word
    private void appendarFotosAlJasperPrint(JasperPrint base, Long idSolicitud) throws Exception {
        List<byte[]> fotos = evidenciaFotoService.obtenerBytesParaSolicitud(idSolicitud);
        if (fotos.isEmpty()) return;

        int pageW  = base.getPageWidth();   // 630
        int pageH  = base.getPageHeight();  // 842
        int cols   = 2, rows = 2, perPage = cols * rows;
        int margin = 40, headerH = 50, gap = 12;
        int fotoW  = (pageW - margin * 2 - gap) / cols;
        int fotoH  = (pageH - margin * 2 - headerH - gap) / rows;

        for (int inicio = 0; inicio < fotos.size(); inicio += perPage) {
            JRBasePrintPage page = new JRBasePrintPage();
            List<byte[]> grupo = fotos.subList(inicio, Math.min(inicio + perPage, fotos.size()));

            // Título de la página
            JRBasePrintText titulo = new JRBasePrintText(null);
            titulo.setText("Evidencias Fotográficas");
            titulo.setX(margin);
            titulo.setY(margin);
            titulo.setWidth(pageW - margin * 2);
            titulo.setHeight(30);
            titulo.setFontSize(12f);
            titulo.setBold(true);
            titulo.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
            page.addElement(titulo);

            for (int i = 0; i < grupo.size(); i++) {
                int col = i % cols;
                int row = i / cols;
                int x   = margin + col * (fotoW + gap);
                int y   = margin + headerH + row * (fotoH + gap);

                byte[] bytes = corregirOrientacionExif(grupo.get(i));

                JRBasePrintImage img = new JRBasePrintImage(null);
                img.setX(x);
                img.setY(y);
                img.setWidth(fotoW);
                img.setHeight(fotoH);
                img.setScaleImage(ScaleImageEnum.RETAIN_SHAPE);
                img.setRenderer(SimpleDataRenderer.getInstance(bytes));
                page.addElement(img);
            }
            base.addPage(page);
        }
    }

    public byte[] appendarFotosAlPdf(byte[] pdfPrincipal, Long idSolicitud) throws Exception {
        List<byte[]> fotos = evidenciaFotoService.obtenerBytesParaSolicitud(idSolicitud);
        if (fotos.isEmpty()) return pdfPrincipal;

        // Grilla 2×2: 4 fotos por página, todas con el mismo tamaño máximo
        final int    COLS       = 2;
        final int    ROWS       = 2;
        final int    POR_PAGINA = COLS * ROWS;
        final float  MARGIN     = 40f;
        final float  HEADER_H   = 28f;
        final float  GAP        = 14f;
        final float  FOTO_MAX_W = 240f;
        final float  FOTO_MAX_H = 300f;

        try (PDDocument doc = Loader.loadPDF(pdfPrincipal);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            for (int inicio = 0; inicio < fotos.size(); inicio += POR_PAGINA) {
                PDPage pagina = new PDPage(new PDRectangle(630f, 842f));
                doc.addPage(pagina);

                float pageW = pagina.getMediaBox().getWidth();
                float pageH = pagina.getMediaBox().getHeight();

                List<byte[]> grupo = fotos.subList(inicio, Math.min(inicio + POR_PAGINA, fotos.size()));

                try (PDPageContentStream cs = new PDPageContentStream(doc, pagina)) {

                    // Título de sección
                    cs.beginText();
                    cs.setFont(font, 11);
                    cs.newLineAtOffset(MARGIN, pageH - MARGIN + 2);
                    cs.showText("Evidencias Fotográficas");
                    cs.endText();

                    // Línea separadora bajo el título
                    cs.moveTo(MARGIN, pageH - MARGIN - 8);
                    cs.lineTo(pageW - MARGIN, pageH - MARGIN - 8);
                    cs.stroke();

                    for (int i = 0; i < grupo.size(); i++) {
                        int col = i % COLS;
                        int row = i / COLS;

                        // Esquina superior-izquierda de la celda en coordenadas PDF (Y hacia arriba)
                        float cellTopY = pageH - MARGIN - HEADER_H - row * (FOTO_MAX_H + GAP);
                        float cellLeftX = MARGIN + col * (FOTO_MAX_W + GAP);

                        byte[] bytes = corregirOrientacionExif(grupo.get(i));
                        PDImageXObject img = PDImageXObject.createFromByteArray(doc, bytes, "ef" + i);

                        float iw = img.getWidth();
                        float ih = img.getHeight();
                        float scale = Math.min(FOTO_MAX_W / iw, FOTO_MAX_H / ih);
                        float dw = iw * scale;
                        float dh = ih * scale;

                        // Centrar dentro de la celda
                        float x = cellLeftX + (FOTO_MAX_W - dw) / 2f;
                        float y = (cellTopY - FOTO_MAX_H) + (FOTO_MAX_H - dh) / 2f;

                        cs.drawImage(img, x, y, dw, dh);
                    }
                }
            }

            doc.save(out);
            return out.toByteArray();
        }
    }

    // ── Corrección de orientación EXIF ──────────────────────────────────────

    private byte[] corregirOrientacionExif(byte[] bytes) {
        try {
            int orientacion = leerOrientacionExifJpeg(bytes);
            if (orientacion <= 1) return bytes;

            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img == null) return bytes;

            BufferedImage corregida = rotarPorOrientacion(img, orientacion);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(corregida, "JPEG", out);
            return out.toByteArray();
        } catch (Exception e) {
            return bytes;
        }
    }

    private BufferedImage rotarPorOrientacion(BufferedImage img, int orientacion) {
        int w = img.getWidth();
        int h = img.getHeight();
        AffineTransform t = new AffineTransform();

        switch (orientacion) {
            case 3 -> { t.translate(w, h);  t.rotate(Math.PI); }          // 180°
            case 6 -> { t.translate(h, 0);  t.rotate(Math.PI / 2); }      // 90° CW
            case 8 -> { t.translate(0, w);  t.rotate(-Math.PI / 2); }     // 90° CCW
            default -> { return img; }
        }

        int nw = (orientacion == 6 || orientacion == 8) ? h : w;
        int nh = (orientacion == 6 || orientacion == 8) ? w : h;
        int type = img.getType() == 0 ? BufferedImage.TYPE_INT_RGB : img.getType();
        BufferedImage resultado = new BufferedImage(nw, nh, type);
        new AffineTransformOp(t, AffineTransformOp.TYPE_BILINEAR).filter(img, resultado);
        return resultado;
    }

    private int leerOrientacionExifJpeg(byte[] b) {
        // Verifica firma JPEG
        if (b.length < 4 || (b[0] & 0xFF) != 0xFF || (b[1] & 0xFF) != 0xD8) return 1;
        try {
            int i = 2;
            while (i + 3 < b.length) {
                if ((b[i] & 0xFF) != 0xFF) return 1;
                int marker = b[i + 1] & 0xFF;
                int segLen  = ((b[i + 2] & 0xFF) << 8) | (b[i + 3] & 0xFF);

                if (marker == 0xE1 && i + 9 < b.length) {    // APP1
                    // Comprueba cabecera "Exif\0\0"
                    if (b[i+4]=='E' && b[i+5]=='x' && b[i+6]=='i' && b[i+7]=='f') {
                        int tiff = i + 10;
                        boolean be = (b[tiff] & 0xFF) == 'M';
                        int ifdOffset = readInt4(b, tiff + 4, be);
                        int ifd = tiff + ifdOffset;
                        int entries = readInt2(b, ifd, be);
                        for (int e = 0; e < entries && ifd + 2 + e * 12 + 12 <= b.length; e++) {
                            int base = ifd + 2 + e * 12;
                            if (readInt2(b, base, be) == 0x0112) {   // tag Orientation
                                return readInt2(b, base + 8, be);
                            }
                        }
                    }
                }
                if (marker == 0xDA) break;   // SOS — fin de cabeceras
                i += 2 + segLen;
            }
        } catch (Exception ignored) {}
        return 1;
    }

    private int readInt2(byte[] b, int off, boolean be) {
        int a = b[off] & 0xFF, c = b[off + 1] & 0xFF;
        return be ? (a << 8) | c : (c << 8) | a;
    }

    private int readInt4(byte[] b, int off, boolean be) {
        int a=b[off]&0xFF, c=b[off+1]&0xFF, d=b[off+2]&0xFF, e=b[off+3]&0xFF;
        return be ? (a<<24)|(c<<16)|(d<<8)|e : (e<<24)|(d<<16)|(c<<8)|a;
    }

    private Long obtenerIdSolicitudDesdeNro(Long nroSolicitud) throws Exception {
        String sql = "SELECT id_solicitud FROM public.solicitud WHERE nro_sol = ? LIMIT 1";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, nroSolicitud);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id_solicitud");
        }
        return null;
    }

    private Map<String, String> obtenerInfoParaQRServicio(Long nroSolicitud) throws Exception {
        String sql = """
                SELECT p.nombre_per || ' ' || p.ap_paterno_per || ' ' || p.ap_materno_per AS nombreSolicitante,
                       c.nombre_car AS cargo
                FROM public.solicitud so
                JOIN public.administrativo adm ON so.id_administrativo = adm.id_administrativo
                JOIN public.persona p           ON adm.id_persona       = p.id_persona
                LEFT JOIN public.cargo c         ON adm.id_cargo         = c.id_cargo
                WHERE so.nro_sol = ?
                LIMIT 1
                """;
        Map<String, String> info = new HashMap<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, nroSolicitud);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                info.put("nombreSolicitante", rs.getString("nombreSolicitante") != null ? rs.getString("nombreSolicitante") : "");
                info.put("cargo", rs.getString("cargo") != null ? rs.getString("cargo") : "");
            }
        }
        return info;
    }

    private List<String> obtenerCodigosActivo(Long nroSolicitud) throws Exception {
        String sql = """
                SELECT a.codigo_act
                FROM public.solicitud so
                JOIN public.detalle_solicitud ds ON so.id_solicitud = ds.id_solicitud
                JOIN public.activo a             ON ds.id_activo   = a.id_activo
                WHERE so.nro_sol = ?
                ORDER BY a.codigo_act
                """;
        List<String> codigos = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, nroSolicitud);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) codigos.add(rs.getString("codigo_act"));
        }
        return codigos;
    }

    public byte[] exportarFichaTecnicaConQRServicioTecnico(Long nroSolicitud, String observacion,
                                            Long idUsuario, String nombreTecnico, boolean varAdmin) throws Exception {

        String ruta = varAdmin ? "/reports/FichaTecnicav3_ServicioTecnicoAdmin.jrxml" : "/reports/FichaTecnicav3_ServicioTecnico.jrxml";
        JasperReport jasperReport = getReporte(ruta);

        // ── Generar QR de Autorización ──────────────────────────────────────
        String contenidoQRAutorizacion = obtenerContenidoQRAutorizacion();

        byte[] bytesQRAutorizacion = generarQRComoBytes(contenidoQRAutorizacion, 150, 150);

        // ── Generar QR de Verificación ──────────────────────────────────────
        String contenidoQRVerificacion =
                "Verificado por el Ing. " + nombreTecnico + "\n" +
                        "Técnico Colaborador";

        byte[] bytesQRVerificacion = generarQRComoBytes(contenidoQRVerificacion, 150, 150);

        // ── Generar QR de Servicio Técnico (datos del solicitante y equipo) ─
        Map<String, String> infoSolicitud = obtenerInfoParaQRServicio(nroSolicitud);
        String contenidoQRServicioTecnico =
                "Solicitud N°: " + nroSolicitud + "\n" +
                "Solicitante: " + infoSolicitud.getOrDefault("nombreSolicitante", "") + "\n" +
                "Cargo: " + infoSolicitud.getOrDefault("cargo", "") + "\n" +
                "Técnico: " + nombreTecnico + "\n" +
                "Área de Hardware - DTIC - UAP";

        byte[] bytesQRServicioTecnico = generarQRComoBytes(contenidoQRServicioTecnico, 150, 150);

        // ── Armar parámetros ────────────────────────────────────────────────
        List<String> codigos = obtenerCodigosActivo(nroSolicitud);
        if (codigos.isEmpty()) throw new RuntimeException("No hay activos para la solicitud " + nroSolicitud);

        List<JasperPrint> prints = new ArrayList<>();

        try (Connection con = dataSource.getConnection()) {
            for (String codigo : codigos) {
                Map<String, Object> params = new HashMap<>();
                params.put("NumSol", nroSolicitud);
                params.put("CodigoActivo", codigo);
                params.put("logoUap", getImagen("/imagesForReports/Logo_UAP.png"));
                params.put("Dtic", getImagen("/imagesForReports/Dtic.png"));
                params.put("QRAutorizacion", bytesQRAutorizacion);
                params.put("QRVerificacion", bytesQRVerificacion);
                params.put("QRServicioTecnico", bytesQRServicioTecnico);
                params.put("AutorizadoPor", obtenerAutorizadoPor());
                params.put("CargoAutorizado", obtenerCargoAutorizado());

                if (observacion != null && !observacion.isBlank()) {
                    params.put("Observacion", observacion.trim());
                }

                List<String> servicios = reporteGenericoRepository.obtenerServiciosPorActivo(nroSolicitud, codigo);
                int mitad = (int) Math.ceil(servicios.size() / 2.0);

                String serviciosIzquierda = servicios.subList(0, mitad)
                        .stream().map(s -> "• " + s).collect(Collectors.joining("\n"));
                String serviciosDerecha = servicios.subList(mitad, servicios.size())
                        .stream().map(s -> "• " + s).collect(Collectors.joining("\n"));

                params.put("ServiciosIzquierda", serviciosIzquierda);
                params.put("ServiciosDerecha", serviciosDerecha);
                params.put("tipoServicios", reporteGenericoRepository.obtenerServiciosPorTipoPorActivo(nroSolicitud, codigo));

                JasperPrint print = JasperFillManager.fillReport(jasperReport, params, con);
                prints.add(print);
            }
        }

        // ── Fusionar y exportar (igual que tu código actual) ────────────────
        JasperPrint base = prints.get(0);
        base.getPages().removeIf(page -> page.getElements() == null || page.getElements().isEmpty());

        for (int i = 1; i < prints.size(); i++) {
            for (JRPrintPage page : prints.get(i).getPages()) {
                if (page.getElements() != null && !page.getElements().isEmpty()) {
                    base.addPage(page);
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(base));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        exporter.exportReport();

        Long idSolicitud = obtenerIdSolicitudDesdeNro(nroSolicitud);
        if (idSolicitud == null) return out.toByteArray();
        return appendarFotosAlPdf(out.toByteArray(), idSolicitud);
    }

    public byte[] exportarFichaTecnicaComoWord(Long nroSolicitud, String observacion,
                                               Long idUsuario, String nombreTecnico, boolean varAdmin) throws Exception {
        String ruta = varAdmin ? "/reports/FichaTecnicav3Admin.jrxml" : "/reports/FichaTecnicav3.jrxml";
        JasperReport jasperReport = getReporte(ruta);

        String contenidoQRAutorizacion = obtenerContenidoQRAutorizacion();
        byte[] bytesQRAutorizacion = generarQRComoBytes(contenidoQRAutorizacion, 150, 150);

        String contenidoQRVerificacion =
                "Verificado por el Ing. " + nombreTecnico + "\n" + "Técnico Colaborador";
        byte[] bytesQRVerificacion = generarQRComoBytes(contenidoQRVerificacion, 150, 150);

        List<String> codigos = obtenerCodigosActivo(nroSolicitud);
        if (codigos.isEmpty()) throw new RuntimeException("No hay activos para la solicitud " + nroSolicitud);

        List<JasperPrint> prints = new ArrayList<>();
        try (Connection con = dataSource.getConnection()) {
            for (String codigo : codigos) {
                Map<String, Object> params = new HashMap<>();
                params.put("NumSol", nroSolicitud);
                params.put("CodigoActivo", codigo);
                params.put("logoUap", getImagen("/imagesForReports/Logo_UAP.png"));
                params.put("Dtic", getImagen("/imagesForReports/Dtic.png"));
                params.put("Membrete_Informes", getImagen("/imagesForReports/Membrete_Informes.jpeg"));
                params.put("QRAutorizacion", bytesQRAutorizacion);
                params.put("QRVerificacion", bytesQRVerificacion);
                params.put("AutorizadoPor", obtenerAutorizadoPor());
                params.put("CargoAutorizado", obtenerCargoAutorizado());
                if (observacion != null && !observacion.isBlank()) params.put("Observacion", observacion.trim());

                List<String> servicios = reporteGenericoRepository.obtenerServiciosPorActivo(nroSolicitud, codigo);
                int mitad = (int) Math.ceil(servicios.size() / 2.0);
                params.put("ServiciosIzquierda", servicios.subList(0, mitad).stream().map(s -> "• " + s).collect(Collectors.joining("\n")));
                params.put("ServiciosDerecha", servicios.subList(mitad, servicios.size()).stream().map(s -> "• " + s).collect(Collectors.joining("\n")));

                prints.add(JasperFillManager.fillReport(jasperReport, params, con));
            }
        }

        JasperPrint base = prints.get(0);
        base.getPages().removeIf(p -> p.getElements() == null || p.getElements().isEmpty());
        for (int i = 1; i < prints.size(); i++) {
            for (JRPrintPage page : prints.get(i).getPages()) {
                if (page.getElements() != null && !page.getElements().isEmpty()) base.addPage(page);
            }
        }

        Long idSolicitud = obtenerIdSolicitudDesdeNro(nroSolicitud);
        if (idSolicitud != null) appendarFotosAlJasperPrint(base, idSolicitud);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JRDocxExporter exporter = new JRDocxExporter();
        exporter.setExporterInput(new SimpleExporterInput(base));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        SimpleDocxReportConfiguration config = new SimpleDocxReportConfiguration();
        config.setFlexibleRowHeight(true);
        exporter.setConfiguration(config);
        exporter.exportReport();
        return out.toByteArray();
    }

    public byte[] exportarReporteEstadisticoServicios(java.util.Date fechaInicial, java.util.Date fechaFinal,
                                                       Long idAdministrativo) throws Exception {
        JasperReport jasperReport = getReporte("/reports/Reporte_Estadistico_Servicios.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("fecha_inicial", fechaInicial);
        params.put("fecha_final", fechaFinal);
        params.put("id_administrativo", idAdministrativo);
        params.put("logoUap", getImagen("/imagesForReports/Logo_UAP.png"));
        params.put("Dtic", getImagen("/imagesForReports/Dtic.png"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Connection con = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(jasperReport, params, con);
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
            exporter.exportReport();
        }
        return out.toByteArray();
    }

    public byte[] exportarReporteEstadisticoServiciosExcel(java.util.Date fechaInicial, java.util.Date fechaFinal,
                                                            Long idAdministrativo) throws Exception {
        final String SQL_RESUMEN = """
                SELECT
                    COALESCE(ts.nombre_tipo, srv.nombre_serv, 'SIN TIPO') AS tipo,
                    COUNT(DISTINCT ds.id_detalle_solicitud)                AS cantidad
                FROM solicitud so
                LEFT JOIN detalle_solicitud ds    ON so.id_solicitud = ds.id_solicitud AND ds._estado_aud <> 'ELIMINADO'
                LEFT JOIN atencion_solicitud aten ON ds.id_detalle_solicitud = aten.id_detalle_solicitud AND aten._estado_aud <> 'ELIMINADO'
                LEFT JOIN atencion_servicio aserv ON aten.id_atencion_solicitud = aserv.id_atencion_solicitud AND aserv._estado_aud <> 'ELIMINADO'
                LEFT JOIN servicio srv            ON aserv.id_servicio = srv.id_servicio
                LEFT JOIN tipo_servicio ts        ON srv.id_tipo_servicio = ts.id_tipo_servicio
                WHERE so.estado_sol = 'FINALIZADO'
                  AND so._estado_aud <> 'ELIMINADO'
                  AND so.fecha_sol BETWEEN ? AND ?
                GROUP BY COALESCE(ts.nombre_tipo, srv.nombre_serv, 'SIN TIPO')
                ORDER BY cantidad DESC
                """;
        final String SQL_DETALLE = """
                SELECT * FROM (
                    SELECT DISTINCT ON (so.nro_sol, ds.id_detalle_solicitud)
                        COALESCE(p_adm.nombre_per || ' ' || p_adm.ap_paterno_per || ' ' || COALESCE(p_adm.ap_materno_per, ''), 'Sistema SATH') AS nom_admin,
                        so.nro_sol                                                                                                               AS nro_sol,
                        p_sol.nombre_per || ' ' || p_sol.ap_paterno_per || ' ' || COALESCE(p_sol.ap_materno_per, '')                            AS solicitante,
                        COALESCE(u_sol.nombre_uni, '—')                                                                                          AS unidad,
                        COALESCE(a.codigo_act, '—')                                                                                              AS cod_equipo,
                        COALESCE(tip.nombre_tipo_act, '—')                                                                                       AS equipo,
                        COALESCE(ts.nombre_tipo, srv.nombre_serv, 'SIN TIPO')                                                                   AS tipo_servicio,
                        so.fecha_sol                                                                                                             AS fecha_recepcion,
                        aten.fecha_fin_reparacion_aten_sol                                                                                       AS fecha_entrega
                    FROM solicitud so
                    LEFT JOIN administrativo adm_sol  ON so.id_administrativo = adm_sol.id_administrativo
                    LEFT JOIN persona p_sol           ON adm_sol.id_persona = p_sol.id_persona
                    LEFT JOIN unidad u_sol            ON adm_sol.id_unidad = u_sol.id_unidad
                    LEFT JOIN detalle_solicitud ds    ON so.id_solicitud = ds.id_solicitud AND ds._estado_aud <> 'ELIMINADO'
                    LEFT JOIN activo a                ON ds.id_activo = a.id_activo
                    LEFT JOIN tipo_activo tip         ON a.id_tipo_activo = tip.id_tipo_activo
                    LEFT JOIN atencion_solicitud aten ON ds.id_detalle_solicitud = aten.id_detalle_solicitud AND aten._estado_aud <> 'ELIMINADO'
                    LEFT JOIN atencion_servicio aserv ON aten.id_atencion_solicitud = aserv.id_atencion_solicitud AND aserv._estado_aud <> 'ELIMINADO'
                    LEFT JOIN servicio srv            ON aserv.id_servicio = srv.id_servicio
                    LEFT JOIN tipo_servicio ts        ON srv.id_tipo_servicio = ts.id_tipo_servicio
                    LEFT JOIN administrativo adm_rep  ON adm_rep.id_administrativo = ?
                    LEFT JOIN persona p_adm           ON adm_rep.id_persona = p_adm.id_persona
                    WHERE so.estado_sol = 'FINALIZADO'
                      AND so._estado_aud <> 'ELIMINADO'
                      AND so.fecha_sol BETWEEN ? AND ?
                    ORDER BY so.nro_sol, ds.id_detalle_solicitud
                ) sub
                ORDER BY tipo_servicio, nro_sol
                """;

        record Resumen(String tipo, long cantidad) {}
        record Detalle(String admin, long nroSol, String solicitante, String unidad,
                       String codEquipo, String equipo, String tipoServicio,
                       java.sql.Date fechaRecepcion, java.sql.Date fechaEntrega) {}

        List<Resumen> resumen  = new ArrayList<>();
        List<Detalle> detalle  = new ArrayList<>();
        java.sql.Date desde    = new java.sql.Date(fechaInicial.getTime());
        java.sql.Date hasta    = new java.sql.Date(fechaFinal.getTime());
        long idAdm             = idAdministrativo != null ? idAdministrativo : 0L;

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(SQL_RESUMEN)) {
                ps.setDate(1, desde); ps.setDate(2, hasta);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) resumen.add(new Resumen(rs.getString(1), rs.getLong(2)));
                }
            }
            try (PreparedStatement ps = con.prepareStatement(SQL_DETALLE)) {
                ps.setLong(1, idAdm); ps.setDate(2, desde); ps.setDate(3, hasta);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) detalle.add(new Detalle(
                            rs.getString("nom_admin"), rs.getLong("nro_sol"),
                            rs.getString("solicitante"), rs.getString("unidad"),
                            rs.getString("cod_equipo"),  rs.getString("equipo"),
                            rs.getString("tipo_servicio"),
                            rs.getDate("fecha_recepcion"), rs.getDate("fecha_entrega")));
                }
            }
        }

        String adminNombre = detalle.isEmpty() ? "Sistema SATH" : detalle.get(0).admin();
        String periodo     = new java.text.SimpleDateFormat("dd-MM-yyyy").format(fechaInicial)
                           + " al " + new java.text.SimpleDateFormat("dd-MM-yyyy").format(fechaFinal);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            // ── Estilos ──────────────────────────────────────────────────────
            Font fBold   = wb.createFont(); fBold.setBold(true);
            Font fBoldLg = wb.createFont(); fBoldLg.setBold(true); fBoldLg.setFontHeightInPoints((short) 12);
            Font fItalic = wb.createFont(); fItalic.setItalic(true); fItalic.setFontHeightInPoints((short) 9);
            Font fSmall  = wb.createFont(); fSmall.setFontHeightInPoints((short) 9);

            short dateFmt = wb.getCreationHelper().createDataFormat().getFormat("dd-mm-yyyy");

            CellStyle csTitle = wb.createCellStyle();
            csTitle.setFont(fBoldLg);
            csTitle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle csMeta = wb.createCellStyle();
            csMeta.setFont(fBold);

            CellStyle csHdr = wb.createCellStyle();
            csHdr.setFont(fBold);
            csHdr.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            csHdr.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            csHdr.setAlignment(HorizontalAlignment.CENTER);
            csHdr.setBorderBottom(BorderStyle.THIN); csHdr.setBorderTop(BorderStyle.THIN);
            csHdr.setBorderLeft(BorderStyle.THIN);  csHdr.setBorderRight(BorderStyle.THIN);

            CellStyle csGroup = wb.createCellStyle();
            csGroup.setFont(fBold);
            csGroup.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            csGroup.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle csSub = wb.createCellStyle();
            csSub.setFont(fItalic);
            csSub.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            csSub.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle csData = wb.createCellStyle();
            csData.setFont(fSmall);
            csData.setBorderBottom(BorderStyle.THIN); csData.setBorderTop(BorderStyle.THIN);
            csData.setBorderLeft(BorderStyle.THIN);  csData.setBorderRight(BorderStyle.THIN);

            CellStyle csDate = wb.createCellStyle();
            csDate.setFont(fSmall);
            csDate.setDataFormat(dateFmt);
            csDate.setAlignment(HorizontalAlignment.CENTER);
            csDate.setBorderBottom(BorderStyle.THIN); csDate.setBorderTop(BorderStyle.THIN);
            csDate.setBorderLeft(BorderStyle.THIN);  csDate.setBorderRight(BorderStyle.THIN);

            CellStyle csTotal = wb.createCellStyle();
            csTotal.setFont(fBold);
            csTotal.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            csTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            csTotal.setBorderBottom(BorderStyle.MEDIUM); csTotal.setBorderTop(BorderStyle.MEDIUM);
            csTotal.setBorderLeft(BorderStyle.MEDIUM);  csTotal.setBorderRight(BorderStyle.MEDIUM);

            // ── Hoja 1: Detalle ───────────────────────────────────────────────
            XSSFSheet shDet = wb.createSheet("Detalle");
            shDet.setColumnWidth(0,  4 * 256);
            shDet.setColumnWidth(1, 32 * 256);
            shDet.setColumnWidth(2, 30 * 256);
            shDet.setColumnWidth(3, 16 * 256);
            shDet.setColumnWidth(4, 22 * 256);
            shDet.setColumnWidth(5, 14 * 256);
            shDet.setColumnWidth(6, 14 * 256);

            int r = 0;
            Cell ct = shDet.createRow(r++).createCell(0);
            ct.setCellValue("REPORTE ESTADÍSTICO DE SERVICIOS POR TIPO");
            ct.setCellStyle(csTitle);
            shDet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            Row rm1 = shDet.createRow(r++);
            Cell cm1 = rm1.createCell(0); cm1.setCellValue("Periodo:"); cm1.setCellStyle(csMeta);
            rm1.createCell(1).setCellValue(periodo);

            Row rm2 = shDet.createRow(r++);
            Cell cm2 = rm2.createCell(0); cm2.setCellValue("Responsable:"); cm2.setCellStyle(csMeta);
            rm2.createCell(1).setCellValue(adminNombre);
            r++;

            Row rh = shDet.createRow(r++);
            String[] hdrs = {"N°", "SOLICITANTE", "UNIDAD", "CÓD. EQUIPO", "TIPO EQUIPO", "F. RECEPCIÓN", "F. ENTREGA"};
            for (int c = 0; c < hdrs.length; c++) {
                Cell hc = rh.createCell(c); hc.setCellValue(hdrs[c]); hc.setCellStyle(csHdr);
            }

            String currentTipo = null;
            int fila = 1, grupoCount = 0;
            for (Detalle d : detalle) {
                if (!d.tipoServicio().equals(currentTipo)) {
                    if (currentTipo != null) {
                        Cell cSub = shDet.createRow(r++).createCell(1);
                        cSub.setCellValue("  Subtotal: " + grupoCount + " servicio(s)");
                        cSub.setCellStyle(csSub);
                        shDet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 1, 6));
                        grupoCount = 0;
                    }
                    currentTipo = d.tipoServicio();
                    Cell cg = shDet.createRow(r++).createCell(0);
                    cg.setCellValue(currentTipo); cg.setCellStyle(csGroup);
                    shDet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 6));
                }
                Row rd = shDet.createRow(r++); grupoCount++;
                Cell c0 = rd.createCell(0); c0.setCellValue(fila++); c0.setCellStyle(csData);
                Cell c1 = rd.createCell(1); c1.setCellValue(d.solicitante()); c1.setCellStyle(csData);
                Cell c2 = rd.createCell(2); c2.setCellValue(d.unidad()); c2.setCellStyle(csData);
                Cell c3 = rd.createCell(3); c3.setCellValue(d.codEquipo()); c3.setCellStyle(csData);
                Cell c4 = rd.createCell(4); c4.setCellValue(d.equipo()); c4.setCellStyle(csData);
                Cell c5 = rd.createCell(5);
                if (d.fechaRecepcion() != null) { c5.setCellValue(d.fechaRecepcion()); c5.setCellStyle(csDate); }
                else c5.setCellStyle(csData);
                Cell c6 = rd.createCell(6);
                if (d.fechaEntrega() != null) { c6.setCellValue(d.fechaEntrega()); c6.setCellStyle(csDate); }
                else c6.setCellStyle(csData);
            }
            if (currentTipo != null) {
                Cell cSub = shDet.createRow(r++).createCell(1);
                cSub.setCellValue("  Subtotal: " + grupoCount + " servicio(s)");
                cSub.setCellStyle(csSub);
                shDet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 1, 6));
            }
            r++;
            Row rTot = shDet.createRow(r++);
            Cell ctL = rTot.createCell(0); ctL.setCellValue("TOTAL DE SERVICIOS ATENDIDOS:"); ctL.setCellStyle(csTotal);
            shDet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 5));
            Cell ctV = rTot.createCell(6); ctV.setCellValue(detalle.size()); ctV.setCellStyle(csTotal);

            // ── Hoja 2: Estadísticas + gráficos nativos ───────────────────────
            XSSFSheet shStat = wb.createSheet("Estadísticas");
            shStat.setColumnWidth(0, 35 * 256);
            shStat.setColumnWidth(1, 14 * 256);

            int s = 0;
            Cell ct2 = shStat.createRow(s++).createCell(0);
            ct2.setCellValue("ESTADÍSTICAS POR TIPO DE SERVICIO"); ct2.setCellStyle(csTitle);
            shStat.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

            Row rm3 = shStat.createRow(s++);
            Cell cm3 = rm3.createCell(0); cm3.setCellValue("Periodo:"); cm3.setCellStyle(csMeta);
            rm3.createCell(1).setCellValue(periodo);
            s++;

            Row rhStat = shStat.createRow(s++);
            Cell sh1 = rhStat.createCell(0); sh1.setCellValue("TIPO DE SERVICIO"); sh1.setCellStyle(csHdr);
            Cell sh2 = rhStat.createCell(1); sh2.setCellValue("CANTIDAD"); sh2.setCellStyle(csHdr);

            int chartFirst = s;
            for (Resumen res : resumen) {
                Row row = shStat.createRow(s++);
                Cell cr0 = row.createCell(0); cr0.setCellValue(res.tipo()); cr0.setCellStyle(csData);
                Cell cr1 = row.createCell(1); cr1.setCellValue(res.cantidad()); cr1.setCellStyle(csData);
            }
            int chartLast = s - 1;

            s++;
            Row rTot2 = shStat.createRow(s++);
            Cell ctL2 = rTot2.createCell(0); ctL2.setCellValue("TOTAL"); ctL2.setCellStyle(csTotal);
            Cell ctV2 = rTot2.createCell(1);
            ctV2.setCellValue(resumen.stream().mapToLong(Resumen::cantidad).sum());
            ctV2.setCellStyle(csTotal);

            if (!resumen.isEmpty()) {
                XSSFDrawing drawing = shStat.createDrawingPatriarch();

                // ── Torta ─────────────────────────────────────────────────────
                XSSFClientAnchor aPie = drawing.createAnchor(0, 0, 0, 0, 3, 0, 13, 18);
                XSSFChart pie = drawing.createChart(aPie);
                pie.setTitleText("Distribución por Tipo de Servicio");
                pie.setTitleOverlay(false);
                pie.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

                XDDFDataSource<String> pieCats = XDDFDataSourcesFactory.fromStringCellRange(shStat,
                        new CellRangeAddress(chartFirst, chartLast, 0, 0));
                XDDFNumericalDataSource<Double> pieVals = XDDFDataSourcesFactory.fromNumericCellRange(shStat,
                        new CellRangeAddress(chartFirst, chartLast, 1, 1));
                XDDFPieChartData pieData = (XDDFPieChartData) pie.createData(ChartTypes.PIE, null, null);
                pieData.setVaryColors(true);
                pieData.addSeries(pieCats, pieVals).setTitle("Servicios", null);
                pie.plot(pieData);

                // ── Barras ────────────────────────────────────────────────────
                XSSFClientAnchor aBar = drawing.createAnchor(0, 0, 0, 0, 3, 19, 13, 37);
                XSSFChart bar = drawing.createChart(aBar);
                bar.setTitleText("Cantidad por Tipo de Servicio");
                bar.setTitleOverlay(false);
                bar.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

                XDDFCategoryAxis catAxis = bar.createCategoryAxis(AxisPosition.BOTTOM);
                XDDFValueAxis valAxis    = bar.createValueAxis(AxisPosition.LEFT);
                valAxis.setCrosses(AxisCrosses.AUTO_ZERO);

                XDDFDataSource<String> barCats = XDDFDataSourcesFactory.fromStringCellRange(shStat,
                        new CellRangeAddress(chartFirst, chartLast, 0, 0));
                XDDFNumericalDataSource<Double> barVals = XDDFDataSourcesFactory.fromNumericCellRange(shStat,
                        new CellRangeAddress(chartFirst, chartLast, 1, 1));
                XDDFBarChartData barData = (XDDFBarChartData) bar.createData(ChartTypes.BAR, catAxis, valAxis);
                barData.setBarDirection(BarDirection.COL);
                barData.addSeries(barCats, barVals).setTitle("Servicios", null);
                bar.plot(barData);
            }

            wb.write(out);
        }
        return out.toByteArray();
    }

    public byte[] exportarFichaTecnicaServicioComoWord(Long nroSolicitud, String observacion,
                                                        Long idUsuario, String nombreTecnico, boolean varAdmin) throws Exception {
        String ruta = varAdmin ? "/reports/FichaTecnicav3_ServicioTecnicoAdmin.jrxml" : "/reports/FichaTecnicav3_ServicioTecnico.jrxml";
        JasperReport jasperReport = getReporte(ruta);

        byte[] bytesQRAutorizacion = generarQRComoBytes(obtenerContenidoQRAutorizacion(), 150, 150);
        byte[] bytesQRVerificacion = generarQRComoBytes(
                "Verificado por el Ing. " + nombreTecnico + "\nTécnico Colaborador", 150, 150);

        Map<String, String> infoSolicitud = obtenerInfoParaQRServicio(nroSolicitud);
        byte[] bytesQRServicioTecnico = generarQRComoBytes(
                "Solicitud N°: " + nroSolicitud + "\nSolicitante: " + infoSolicitud.getOrDefault("nombreSolicitante", "") +
                "\nCargo: " + infoSolicitud.getOrDefault("cargo", "") +
                "\nTécnico: " + nombreTecnico + "\nÁrea de Hardware - DTIC - UAP", 150, 150);

        List<String> codigos = obtenerCodigosActivo(nroSolicitud);
        if (codigos.isEmpty()) throw new RuntimeException("No hay activos para la solicitud " + nroSolicitud);

        List<JasperPrint> prints = new ArrayList<>();
        try (Connection con = dataSource.getConnection()) {
            for (String codigo : codigos) {
                Map<String, Object> params = new HashMap<>();
                params.put("NumSol", nroSolicitud);
                params.put("CodigoActivo", codigo);
                params.put("logoUap", getImagen("/imagesForReports/Logo_UAP.png"));
                params.put("Dtic", getImagen("/imagesForReports/Dtic.png"));
                params.put("QRAutorizacion", bytesQRAutorizacion);
                params.put("QRVerificacion", bytesQRVerificacion);
                params.put("QRServicioTecnico", bytesQRServicioTecnico);
                params.put("AutorizadoPor", obtenerAutorizadoPor());
                params.put("CargoAutorizado", obtenerCargoAutorizado());
                if (observacion != null && !observacion.isBlank()) params.put("Observacion", observacion.trim());

                List<String> servicios = reporteGenericoRepository.obtenerServiciosPorActivo(nroSolicitud, codigo);
                int mitad = (int) Math.ceil(servicios.size() / 2.0);
                params.put("ServiciosIzquierda", servicios.subList(0, mitad).stream().map(s -> "• " + s).collect(Collectors.joining("\n")));
                params.put("ServiciosDerecha", servicios.subList(mitad, servicios.size()).stream().map(s -> "• " + s).collect(Collectors.joining("\n")));

                prints.add(JasperFillManager.fillReport(jasperReport, params, con));
            }
        }

        JasperPrint base = prints.get(0);
        base.getPages().removeIf(p -> p.getElements() == null || p.getElements().isEmpty());
        for (int i = 1; i < prints.size(); i++) {
            for (JRPrintPage page : prints.get(i).getPages()) {
                if (page.getElements() != null && !page.getElements().isEmpty()) base.addPage(page);
            }
        }

        Long idSolicitud = obtenerIdSolicitudDesdeNro(nroSolicitud);
        if (idSolicitud != null) appendarFotosAlJasperPrint(base, idSolicitud);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JRDocxExporter exporter = new JRDocxExporter();
        exporter.setExporterInput(new SimpleExporterInput(base));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        SimpleDocxReportConfiguration config = new SimpleDocxReportConfiguration();
        config.setFlexibleRowHeight(true);
        exporter.setConfiguration(config);
        exporter.exportReport();
        return out.toByteArray();
    }

    private void guardarObservacionSolicitud(Long nroSolicitud, String observacion) throws Exception {
        if (observacion == null || observacion.isBlank()) return;
        Long idSolicitud = obtenerIdSolicitudDesdeNro(nroSolicitud);
        if (idSolicitud == null) return;
        solicitudRepository.findById(idSolicitud).ifPresent(s -> {
            s.setObservacionFicha(observacion.trim());
            solicitudRepository.save(s);
        });
    }
}