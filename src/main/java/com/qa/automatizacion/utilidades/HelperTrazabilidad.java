package com.qa.automatizacion.utilidades;

import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper para gestionar la trazabilidad entre escenarios y historias de usuario.
 * Permite rastrear qué escenarios cubren qué requerimientos funcionales.
 *
 * Principios aplicados:
 * - Single Responsibility: Se enfoca únicamente en trazabilidad
 * - Observer Pattern: Registra eventos de ejecución de escenarios
 * - Strategy Pattern: Permite diferentes formatos de reporte
 *
 * @author Equipo QA Automatización
 * @version 1.0
 */
public class HelperTrazabilidad {

    private static final Logger logger = LoggerFactory.getLogger(HelperTrazabilidad.class);

    // Mapa thread-safe para almacenar información de trazabilidad
    private static final Map<String, InformacionEscenario> registroEscenarios = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> mapeoHistoriasUsuario = new ConcurrentHashMap<>();

    // Estadísticas globales
    private static final Map<String, Integer> estadisticasEjecucion = new ConcurrentHashMap<>();

    // Formatters para fechas
    private static final DateTimeFormatter FORMATTER_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMATTER_ARCHIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Constructor que inicializa las estadísticas
     */
    public HelperTrazabilidad() {
        inicializarEstadisticas();
    }

    /**
     * Registra el inicio de un escenario
     *
     * @param escenario Escenario de Cucumber
     */
    public void registrarInicioEscenario(Scenario escenario) {
        String nombreEscenario = escenario.getName();
        Collection<String> tags = escenario.getSourceTagNames();

        InformacionEscenario info = new InformacionEscenario();
        info.nombre = nombreEscenario;
        info.tags = new ArrayList<>(tags);
        info.inicioEjecucion = LocalDateTime.now();
        info.historiasUsuario = extraerHistoriasUsuario(tags);
        info.prioridad = extraerPrioridad(tags);
        info.categoria = extraerCategoria(tags);

        registroEscenarios.put(nombreEscenario, info);

        // Actualizar mapeo de historias de usuario
        for (String hu : info.historiasUsuario) {
            mapeoHistoriasUsuario.computeIfAbsent(hu, k -> new ArrayList<>()).add(nombreEscenario);
        }

        logger.info("Escenario registrado - Nombre: {}, HU: {}, Tags: {}",
                nombreEscenario, info.historiasUsuario, tags);
    }

    /**
     * Registra el resultado de un escenario
     *
     * @param escenario Escenario de Cucumber completado
     */
    public void registrarResultadoEscenario(Scenario escenario) {
        String nombreEscenario = escenario.getName();
        InformacionEscenario info = registroEscenarios.get(nombreEscenario);

        if (info != null) {
            info.finEjecucion = LocalDateTime.now();
            info.estado = escenario.getStatus().toString();
            info.duracionMs = calcularDuracion(info.inicioEjecucion, info.finEjecucion);

            // Actualizar estadísticas
            actualizarEstadisticas(info);

            logger.info("Resultado registrado - Escenario: {}, Estado: {}, Duración: {} ms",
                    nombreEscenario, info.estado, info.duracionMs);
        } else {
            logger.warn("No se encontró información previa para el escenario: {}", nombreEscenario);
        }
    }

    /**
     * Genera un reporte final de trazabilidad
     */
    public void generarReporteFinal() {
        try {
            generarReporteHTML();
            generarReporteJSON();
            generarMatrizTrazabilidad();

            logger.info("Reportes de trazabilidad generados exitosamente");

        } catch (Exception e) {
            logger.error("Error generando reportes de trazabilidad: {}", e.getMessage());
        }
    }

    /**
     * Genera un reporte en formato HTML
     */
    private void generarReporteHTML() throws IOException {
        String timestamp = LocalDateTime.now().format(FORMATTER_ARCHIVO);
        String nombreArchivo = String.format("reportes/trazabilidad/trazabilidad_%s.html", timestamp);

        // Crear directorio si no existe
        Files.createDirectories(Paths.get("reportes/trazabilidad"));

        try (FileWriter writer = new FileWriter(nombreArchivo)) {
            writer.write(construirHTMLReporte());
        }

        logger.info("Reporte HTML generado: {}", nombreArchivo);
    }

    /**
     * Genera un reporte en formato JSON
     */
    private void generarReporteJSON() throws IOException {
        String timestamp = LocalDateTime.now().format(FORMATTER_ARCHIVO);
        String nombreArchivo = String.format("reportes/trazabilidad/trazabilidad_%s.json", timestamp);

        try (FileWriter writer = new FileWriter(nombreArchivo)) {
            writer.write(construirJSONReporte());
        }

        logger.info("Reporte JSON generado: {}", nombreArchivo);
    }

    /**
     * Genera una matriz de trazabilidad
     */
    private void generarMatrizTrazabilidad() throws IOException {
        String timestamp = LocalDateTime.now().format(FORMATTER_ARCHIVO);
        String nombreArchivo = String.format("reportes/trazabilidad/matriz_trazabilidad_%s.csv", timestamp);

        try (FileWriter writer = new FileWriter(nombreArchivo)) {
            writer.write(construirMatrizCSV());
        }

        logger.info("Matriz de trazabilidad generada: {}", nombreArchivo);
    }

    // Métodos auxiliares privados

    /**
     * Extrae referencias a historias de usuario de los tags
     */
    private List<String> extraerHistoriasUsuario(Collection<String> tags) {
        List<String> historias = new ArrayList<>();

        for (String tag : tags) {
            if (tag.toUpperCase().startsWith("@HU-") || tag.toUpperCase().startsWith("@HISTORIA-")) {
                historias.add(tag.substring(1)); // Remover el @
            }
        }

        // Si no hay HU específica, asignar una genérica basada en otros tags
        if (historias.isEmpty()) {
            if (tags.contains("@Login")) {
                historias.add("HU-001-Autenticacion");
            } else if (tags.contains("@CRUD")) {
                historias.add("HU-002-GestionProductos");
            } else if (tags.contains("@Registro")) {
                historias.add("HU-003-RegistroUsuarios");
            } else {
                historias.add("HU-000-General");
            }
        }

        return historias;
    }

    /**
     * Extrae la prioridad del escenario de los tags
     */
    private String extraerPrioridad(Collection<String> tags) {
        if (tags.contains("@Alta") || tags.contains("@SmokeTest")) {
            return "Alta";
        } else if (tags.contains("@Media") || tags.contains("@Regression")) {
            return "Media";
        } else if (tags.contains("@Baja")) {
            return "Baja";
        }
        return "Media"; // Prioridad por defecto
    }

    /**
     * Extrae la categoría del escenario de los tags
     */
    private String extraerCategoria(Collection<String> tags) {
        if (tags.contains("@Login") || tags.contains("@Autenticacion")) {
            return "Autenticación";
        } else if (tags.contains("@CRUD") || tags.contains("@Productos")) {
            return "Gestión de Productos";
        } else if (tags.contains("@Registro")) {
            return "Registro de Usuarios";
        } else if (tags.contains("@SmokeTest")) {
            return "Pruebas de Humo";
        } else if (tags.contains("@Regression")) {
            return "Pruebas de Regresión";
        }
        return "General";
    }

    /**
     * Calcula la duración entre dos momentos
     */
    private long calcularDuracion(LocalDateTime inicio, LocalDateTime fin) {
        return java.time.Duration.between(inicio, fin).toMillis();
    }

    /**
     * Actualiza las estadísticas globales
     */
    private void actualizarEstadisticas(InformacionEscenario info) {
        estadisticasEjecucion.merge("total", 1, Integer::sum);
        estadisticasEjecucion.merge(info.estado.toLowerCase(), 1, Integer::sum);
        estadisticasEjecucion.merge("categoria_" + info.categoria.toLowerCase().replace(" ", "_"), 1, Integer::sum);

        // Actualizar duración promedio
        Integer totalDuracion = estadisticasEjecucion.getOrDefault("duracion_total_ms", 0);
        estadisticasEjecucion.put("duracion_total_ms", totalDuracion + (int) info.duracionMs);
    }

    /**
     * Inicializa las estadísticas con valores por defecto
     */
    private void inicializarEstadisticas() {
        estadisticasEjecucion.put("total", 0);
        estadisticasEjecucion.put("passed", 0);
        estadisticasEjecucion.put("failed", 0);
        estadisticasEjecucion.put("skipped", 0);
        estadisticasEjecucion.put("duracion_total_ms", 0);
    }

    /**
     * Construye el contenido HTML del reporte
     */
    private String construirHTMLReporte() {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='es'>\n");
        html.append("<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <title>Reporte de Trazabilidad - Pruebas BDD</title>\n");
        html.append("    <style>\n");
        html.append(obtenerEstilosCSS());
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        // Encabezado
        html.append("    <header>\n");
        html.append("        <h1>Reporte de Trazabilidad</h1>\n");
        html.append("        <p>Generado el: ").append(LocalDateTime.now().format(FORMATTER_TIMESTAMP)).append("</p>\n");
        html.append("    </header>\n");

        // Estadísticas
        html.append("    <section id='estadisticas'>\n");
        html.append("        <h2>Estadísticas de Ejecución</h2>\n");
        html.append(construirTablaEstadisticas());
        html.append("    </section>\n");

        // Mapeo de Historias de Usuario
        html.append("    <section id='historias'>\n");
        html.append("        <h2>Mapeo de Historias de Usuario</h2>\n");
        html.append(construirTablaHistorias());
        html.append("    </section>\n");

        // Detalle de Escenarios
        html.append("    <section id='escenarios'>\n");
        html.append("        <h2>Detalle de Escenarios</h2>\n");
        html.append(construirTablaEscenarios());
        html.append("    </section>\n");

        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    /**
     * Construye el contenido JSON del reporte
     */
    private String construirJSONReporte() {
        StringBuilder json = new StringBuilder();

        json.append("{\n");
        json.append("  \"metadata\": {\n");
        json.append("    \"generado\": \"").append(LocalDateTime.now().format(FORMATTER_TIMESTAMP)).append("\",\n");
        json.append("    \"total_escenarios\": ").append(registroEscenarios.size()).append("\n");
        json.append("  },\n");

        json.append("  \"estadisticas\": {\n");
        estadisticasEjecucion.forEach((k, v) ->
                json.append("    \"").append(k).append("\": ").append(v).append(",\n"));
        json.setLength(json.length() - 2); // Remover última coma
        json.append("\n  },\n");

        json.append("  \"historias_usuario\": {\n");
        mapeoHistoriasUsuario.forEach((hu, escenarios) -> {
            json.append("    \"").append(hu).append("\": [");
            escenarios.forEach(e -> json.append("\"").append(e).append("\","));
            json.setLength(json.length() - 1); // Remover última coma
            json.append("],\n");
        });
        json.setLength(json.length() - 2); // Remover última coma
        json.append("\n  },\n");

        json.append("  \"escenarios\": [\n");
        registroEscenarios.forEach((nombre, info) -> {
            json.append("    {\n");
            json.append("      \"nombre\": \"").append(nombre).append("\",\n");
            json.append("      \"estado\": \"").append(info.estado).append("\",\n");
            json.append("      \"duracion_ms\": ").append(info.duracionMs).append(",\n");
            json.append("      \"categoria\": \"").append(info.categoria).append("\",\n");
            json.append("      \"historias_usuario\": [");
            info.historiasUsuario.forEach(hu -> json.append("\"").append(hu).append("\","));
            json.setLength(json.length() - 1); // Remover última coma
            json.append("]\n");
            json.append("    },\n");
        });
        json.setLength(json.length() - 2); // Remover última coma
        json.append("\n  ]\n");
        json.append("}");

        return json.toString();
    }

    /**
     * Construye la matriz de trazabilidad en formato CSV
     */
    private String construirMatrizCSV() {
        StringBuilder csv = new StringBuilder();

        // Encabezados
        csv.append("Historia_Usuario,Escenario,Estado,Categoria,Prioridad,Duracion_ms,Tags\n");

        // Datos
        registroEscenarios.forEach((nombre, info) -> {
            for (String hu : info.historiasUsuario) {
                csv.append(hu).append(",");
                csv.append("\"").append(nombre).append("\",");
                csv.append(info.estado).append(",");
                csv.append("\"").append(info.categoria).append("\",");
                csv.append(info.prioridad).append(",");
                csv.append(info.duracionMs).append(",");
                csv.append("\"").append(String.join(";", info.tags)).append("\"\n");
            }
        });

        return csv.toString();
    }

    // Métodos auxiliares para HTML

    private String obtenerEstilosCSS() {
        return """
            body { font-family: Arial, sans-serif; margin: 20px; }
            header { background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
            h1 { color: #333; margin: 0; }
            h2 { color: #555; border-bottom: 2px solid #007bff; padding-bottom: 5px; }
            table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
            th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
            th { background-color: #f8f9fa; font-weight: bold; }
            .passed { background-color: #d4edda; }
            .failed { background-color: #f8d7da; }
            .skipped { background-color: #fff3cd; }
            section { margin-bottom: 30px; }
            """;
    }

    private String construirTablaEstadisticas() {
        StringBuilder tabla = new StringBuilder();
        tabla.append("<table>\n");
        tabla.append("  <tr><th>Métrica</th><th>Valor</th></tr>\n");

        estadisticasEjecucion.forEach((metrica, valor) -> {
            String nombreMetrica = metrica.replace("_", " ").toUpperCase();
            tabla.append("  <tr><td>").append(nombreMetrica).append("</td><td>").append(valor).append("</td></tr>\n");
        });

        tabla.append("</table>\n");
        return tabla.toString();
    }

    private String construirTablaHistorias() {
        StringBuilder tabla = new StringBuilder();
        tabla.append("<table>\n");
        tabla.append("  <tr><th>Historia de Usuario</th><th>Escenarios</th><th>Total</th></tr>\n");

        mapeoHistoriasUsuario.forEach((hu, escenarios) -> {
            tabla.append("  <tr><td>").append(hu).append("</td>");
            tabla.append("<td>").append(String.join(", ", escenarios)).append("</td>");
            tabla.append("<td>").append(escenarios.size()).append("</td></tr>\n");
        });

        tabla.append("</table>\n");
        return tabla.toString();
    }

    private String construirTablaEscenarios() {
        StringBuilder tabla = new StringBuilder();
        tabla.append("<table>\n");
        tabla.append("  <tr><th>Escenario</th><th>Estado</th><th>Categoría</th><th>Duración (ms)</th><th>HU</th></tr>\n");

        registroEscenarios.forEach((nombre, info) -> {
            String claseEstado = info.estado.toLowerCase();
            tabla.append("  <tr class='").append(claseEstado).append("'>");
            tabla.append("<td>").append(nombre).append("</td>");
            tabla.append("<td>").append(info.estado).append("</td>");
            tabla.append("<td>").append(info.categoria).append("</td>");
            tabla.append("<td>").append(info.duracionMs).append("</td>");
            tabla.append("<td>").append(String.join(", ", info.historiasUsuario)).append("</td>");
            tabla.append("</tr>\n");
        });

        tabla.append("</table>\n");
        return tabla.toString();
    }

    // Clase interna para almacenar información de escenarios
    private static class InformacionEscenario {
        String nombre;
        String estado;
        String categoria;
        String prioridad;
        List<String> tags;
        List<String> historiasUsuario;
        LocalDateTime inicioEjecucion;
        LocalDateTime finEjecucion;
        long duracionMs;

        InformacionEscenario() {
            this.tags = new ArrayList<>();
            this.historiasUsuario = new ArrayList<>();
        }
    }
}