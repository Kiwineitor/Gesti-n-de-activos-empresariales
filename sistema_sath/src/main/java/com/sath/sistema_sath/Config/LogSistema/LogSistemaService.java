package com.sath.sistema_sath.Config.LogSistema;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LogSistemaService {

    @Value("${sath.log.dir:src/main/resources/Logs}")
    private String logDir;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Pattern LINEA = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}) \\[(\\w+)\\s*\\] (\\S+)(?:\\s+\\[([^\\]]*)\\])? - (.*)$"
    );

    public List<LogSistema> listar(String nivel, String usuario, int limite) {
        List<String> lineas = leerUltimasLineas(limite * 6);
        List<LogSistema> todos = parsear(lineas);

        return todos.stream()
                .filter(l -> nivel == null || nivel.isBlank() || nivel.equalsIgnoreCase(l.getNivel()))
                .filter(l -> usuario == null || usuario.isBlank() ||
                        (l.getUsuario() != null && l.getUsuario().toLowerCase().contains(usuario.toLowerCase())))
                .limit(limite)
                .collect(Collectors.toList());
    }

    public Map<String, Object> stats() {
        List<LogSistema> todos = parsear(leerUltimasLineas(20_000));
        LocalDateTime hoy = LocalDate.now().atStartOfDay();

        Map<String, Object> s = new LinkedHashMap<>();
        s.put("total", todos.size());
        s.put("errores", todos.stream().filter(l -> "ERROR".equals(l.getNivel()) && despuesDeHoy(l, hoy)).count());
        s.put("warnings", todos.stream().filter(l -> "WARN".equals(l.getNivel()) && despuesDeHoy(l, hoy)).count());
        s.put("hoy", todos.stream().filter(l -> despuesDeHoy(l, hoy)).count());
        return s;
    }

    public List<String> listarfechasDisponibles() {
        Path dir = resolverDirectorio();
        List<String> fechas = new ArrayList<>();
        fechas.add(LocalDate.now().toString());
        if (!Files.isDirectory(dir)) return fechas;

        Pattern p = Pattern.compile("sath-(\\d{4}-\\d{2}-\\d{2})\\.log\\.gz");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "sath-*.log.gz")) {
            for (Path path : stream) {
                Matcher m = p.matcher(path.getFileName().toString());
                if (m.matches()) fechas.add(m.group(1));
            }
        } catch (IOException ignored) {
        }
        fechas.sort(Comparator.reverseOrder());
        return fechas;
    }

    public record ArchivoLog(Path ruta, String nombre, String contentType) {
    }

    public Optional<ArchivoLog> resolverArchivoDescarga(String fecha) {
        Path dir = resolverDirectorio();
        String hoy = LocalDate.now().toString();
        if (fecha == null || fecha.isBlank() || fecha.equals(hoy)) {
            Path activo = dir.resolve("sath.log");
            if (!Files.exists(activo)) return Optional.empty();
            return Optional.of(new ArchivoLog(activo, "sath.log", "text/plain"));
        }
        Path historico = dir.resolve("sath-" + fecha+ ".log.gz");
        if (!Files.exists(historico)) return Optional.empty();
        return Optional.of(new ArchivoLog(historico, historico.getFileName().toString(), "application/gzip"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private boolean despuesDeHoy(LogSistema l, LocalDateTime hoy) {
        return l.getFecha() != null && l.getFecha().isAfter(hoy);
    }

    private Path resolverDirectorio() {
        Path p = Paths.get(logDir);
        return p.isAbsolute() ? p : Paths.get(System.getProperty("user.dir")).resolve(p);
    }

    private List<String> leerUltimasLineas(int n) {
        Path path = resolverRuta();
        if (!Files.exists(path)) return Collections.emptyList();
        try {
            List<String> todas = Files.readAllLines(path, StandardCharsets.UTF_8);
            int desde = Math.max(0, todas.size() - n);
            return new ArrayList<>(todas.subList(desde, todas.size()));
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private Path resolverRuta() {
        Path p = Paths.get(logDir, "sath.log");
        // Si es relativa, la ancla al directorio de trabajo del JVM
        return resolverDirectorio().resolve("sath.log");
    }

    private List<LogSistema> parsear(List<String> lineas) {
        List<LogSistema> result = new ArrayList<>();
        long id = 1;
        LogSistema actual = null;

        for (String linea : lineas) {
            Matcher m = LINEA.matcher(linea);
            if (m.matches()) {
                actual = new LogSistema();
                actual.setId(id++);
                try {
                    actual.setFecha(LocalDateTime.parse(m.group(1), FMT));
                } catch (Exception ignored) {
                }
                actual.setNivel(m.group(2).trim());
                actual.setLogger(m.group(3).trim());
                String usr = m.group(4) != null ? m.group(4).trim() : "";
                actual.setUsuario(usr.isEmpty() ? "sistema" : usr);
                actual.setMensaje(m.group(5));
                result.add(actual);
            } else if (actual != null && !linea.isBlank()) {
                String prev = actual.getStackTrace();
                actual.setStackTrace(prev == null ? linea : prev + "\n" + linea);
            }
        }

        Collections.reverse(result);
        return result;
    }
}