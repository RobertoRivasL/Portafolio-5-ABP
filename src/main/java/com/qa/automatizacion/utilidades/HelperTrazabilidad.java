package com.qa.automatizacion.utilidades;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper para gestionar la trazabilidad de pruebas con historias de usuario.
 * Registra la ejecución de escenarios y su relación con requerimientos.
 *
 * Principios aplicados:
 * - Single Responsibility: Se enfoca únicamente en la trazabilidad
 * - Encapsulación: Oculta la complejidad de persistencia de datos
 * - Thread Safety: Utiliza estructuras concurrentes para ejecución paralela
 * - Abstracción: Proporciona una interfaz simple para el registro
 */
public class HelperTrazabilidad {

    private static final Logger logger = LoggerFactory.getLogger(HelperTrazabilidad.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DIRECTORIO_TRAZABILIDAD = "reportes/trazabilidad";

    // Estructuras thread-safe para almacenar información de trazabilidad
    private final Map<String, RegistroEscenario> registrosEscenarios = new ConcurrentHashMap<>();
    private final Map<String, List<String>> mapaHistoriasEscenarios = new ConcurrentHashMap<>();
    private final Map<String, RegistroHistoriaUsuario> registrosHistorias = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public HelperTrazabilidad() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        inicializarDirectorios();
        cargarHistoriasUsuario();
    }

    // ==================== CLASES DE DATOS ====================

    /**
     * Registro de un escenario ejecutado.
     */
    public static class RegistroEscenario {
        public String nombre;
        public Collection<String> tags;
        public String estado;
        public LocalDateTime inicioEjecucion;
        public LocalDateTime finEjecucion;
        public long duracionSegundos;
        public List<String> pasosEjecutados;
        public String historiaUsuarioReferenciada;
        public String mensajeError;
        public String informacionDiagnostico;

        public RegistroEscenario() {
            this.pasosEjecutados = new ArrayList<>();
        }
    }

    /**
     * Registro de una historia de usuario.
     */
    public static class RegistroHistoriaUsuario {
        public String id;
        public String titulo;
        public String descripcion;
        public List<String> criteriosAceptacion;
        public String prioridad;
        public String estado;
        public List<String> escenariosAsociados;
        public int escenariosPasados;
        public int escenariosFallidos;
        public double porcentajeCobertura;

        public RegistroHistoriaUsuario() {
            this.criteriosAceptacion = new ArrayList<>();
            this.escenariosAsociados = new ArrayList<>();
        }
    }

    /**
     * Resumen de trazabilidad.
     */
    public static class ResumenTrazabilidad {
        public LocalDateTime fechaGeneracion;
        public int totalEscenarios;
        public int escenariosPasados;
        public int escenariosFallidos;
        public int totalHistoriasUsuario;
        public int historiasCompletamenteCubiertas;
        public int historiasParcialmenteCubiertas;
        public int historiasSinCobertura;
        public double coberturaGlobal;
        public Map<String, Integer> distribucionPorTag;

        public ResumenTrazabilidad() {
            this.distribucionPorTag = new HashMap<>();
        }
    }

    // ==================== MÉTODOS PRINCIPALES ====================

    /**
     * Inicia el registro de un escenario.
     *
     * @param nombreEscenario nombre del escenario
     * @param tags tags del escenario
     */
    public void iniciarEscenario(String nombreEscenario, Collection<String> tags) {
        logger.debug("Iniciando registro de escenario: {}", nombreEscenario);

        RegistroEscenario registro = new RegistroEscenario();
        registro.nombre = nombreEscenario;
        registro.tags = new ArrayList<>(tags);
        registro.inicioEjecucion = LocalDateTime.now();
        registro.estado = "EJECUTANDO";

        // Extraer referencia a historia de usuario desde tags o nombre
        registro.historiaUsuarioReferenciada = extraerReferenciaHistoriaUsuario(nombreEscenario, tags);

        registrosEscenarios.put(nombreEscenario, registro);

        // Asociar con historia de usuario
        if (registro.historiaUsuarioReferenciada != null) {
            asociarEscenarioConHistoria(nombreEscenario, registro.historiaUsuarioReferenciada);
        }

        logger.debug("Escenario registrado: {} -> HU: {}", nombreEscenario, registro.historiaUsuarioReferenciada);
    }

    /**
     * Registra un paso ejecutado en el escenario actual.
     *
     * @param historiaUsuario referencia a la historia de usuario
     * @param descripcionPaso descripción del paso ejecutado
     */
    public void registrarPaso(String historiaUsuario, String descripcionPaso) {
        String nombreEscenario = obtenerEscenarioActual();

        if (nombreEscenario != null) {
            RegistroEscenario registro = registrosEscenarios.get(nombreEscenario);
            if (registro != null) {
                String pasoCompleto = String.format("[%s] %s - %s",
                        LocalDateTime.now().format(FORMATO_FECHA), historiaUsuario, descripcionPaso);
                registro.pasosEjecutados.add(pasoCompleto);

                logger.debug("Paso registrado para {}: {}", nombreEscenario, descripcionPaso);
            }
        }
    }

    /**
     * Finaliza el registro de un escenario.
     *
     * @param nombreEscenario nombre del escenario
     * @param estado estado final (PASSED, FAILED, SKIPPED)
     * @param duracion duración en segundos
     */
    public void finalizarEscenario(String nombreEscenario, String estado, long duracion) {
        logger.debug("Finalizando registro de escenario: {} - {}", nombreEscenario, estado);

        RegistroEscenario registro = registrosEscenarios.get(nombreEscenario);
        if (registro != null) {
            registro.finEjecucion = LocalDateTime.now();
            registro.estado = estado;
            registro.duracionSegundos = duracion;

            // Actualizar estadísticas de historia de usuario
            if (registro.historiaUsuarioReferenciada != null) {
                actualizarEstadisticasHistoria(registro.historiaUsuarioReferenciada, estado);
            }

            logger.info("Escenario finalizado: {} - {} ({}s)", nombreEscenario, estado, duracion);
        }
    }

    /**
     * Registra un fallo en el escenario.
     *
     * @param nombreEscenario nombre del escenario
     * @param informacionDiagnostico información de diagnóstico del fallo
     */
    public void registrarFallo(String nombreEscenario, String informacionDiagnostico) {
        RegistroEscenario registro = registrosEscenarios.get(nombreEscenario);
        if (registro != null) {
            registro.mensajeError = "FALLO DETECTADO";
            registro.informacionDiagnostico = informacionDiagnostico;

            logger.warn("Fallo registrado para escenario: {}", nombreEscenario);
        }
    }

    /**
     * Registra un éxito en el escenario.
     *
     * @param nombreEscenario nombre del escenario
     */
    public void registrarExito(String nombreEscenario) {
        RegistroEscenario registro = registrosEscenarios.get(nombreEscenario);
        if (registro != null) {
            registro.mensajeError = null;
            registro.informacionDiagnostico = "Escenario ejecutado exitosamente";

            logger.debug("Éxito registrado para escenario: {}", nombreEscenario);
        }
    }

    // ==================== MÉTODOS DE GENERACIÓN DE REPORTES ====================

    /**
     * Genera el reporte completo de trazabilidad.
     *
     * @return true si el reporte se generó exitosamente
     */
    public boolean generarReporteTrazabilidad() {
        logger.info("Generando reporte de trazabilidad...");

        try {
            // Calcular estadísticas
            calcularEstadisticasFinales();

            // Generar resumen
            ResumenTrazabilidad resumen = generarResumen();

            // Guardar archivos
            guardarReporteJson(resumen);
            guardarReporteHtml(resumen);
            guardarMatrizTrazabilidad();

            logger.info("Reporte de trazabilidad generado exitosamente en: {}", DIRECTORIO_TRAZABILIDAD);
            return true;

        } catch (Exception e) {
            logger.error("Error generando reporte de trazabilidad: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Genera un resumen de la trazabilidad.
     *
     * @return resumen de trazabilidad
     */
    private ResumenTrazabilidad generarResumen() {
        ResumenTrazabilidad resumen = new ResumenTrazabilidad();
        resumen.fechaGeneracion = LocalDateTime.now();

        // Estadísticas de escenarios
        resumen.totalEscenarios = registrosEscenarios.size();
        resumen.escenariosPasados = (int) registrosEscenarios.values().stream()
                .filter(r -> "PASSED".equals(r.estado))
                .count();
        resumen.escenariosFallidos = (int) registrosEscenarios.values().stream()
                .filter(r -> "FAILED".equals(r.estado))
                .count();

        // Estadísticas de historias de usuario
        resumen.totalHistoriasUsuario = registrosHistorias.size();
        resumen.historiasCompletamenteCubiertas = (int) registrosHistorias.values().stream()
                .filter(h -> h.porcentajeCobertura == 100.0)
                .count();
        resumen.historiasParcialmenteCubiertas = (int) registrosHistorias.values().stream()
                .filter(h -> h.porcentajeCobertura > 0 && h.porcentajeCobertura < 100.0)
                .count();
        resumen.historiasSinCobertura = (int) registrosHistorias.values().stream()
                .filter(h -> h.porcentajeCobertura == 0.0)
                .count();

        // Cobertura global
        if (resumen.totalHistoriasUsuario > 0) {
            resumen.coberturaGlobal = ((double) resumen.historiasCompletamenteCubiertas /
                    resumen.totalHistoriasUsuario) * 100.0;
        }

        // Distribución por tags
        registrosEscenarios.values().forEach(registro -> {
            registro.tags.forEach(tag -> {
                resumen.distribucionPorTag.merge(tag, 1, Integer::sum);
            });
        });

        return resumen;
    }

    /**
     * Guarda el reporte en formato JSON.
     *
     * @param resumen resumen de trazabilidad
     */
    private void guardarReporteJson(ResumenTrazabilidad resumen) throws IOException {
        Map<String, Object> reporteCompleto = new HashMap<>();
        reporteCompleto.put("resumen", resumen);
        reporteCompleto.put("escenarios", registrosEscenarios);
        reporteCompleto.put("historiasUsuario", registrosHistorias);
        reporteCompleto.put("mapaEscenarios", mapaHistoriasEscenarios);

        File archivo = new File(DIRECTORIO_TRAZABILIDAD, "reporte-trazabilidad.json");
        objectMapper.writeValue(archivo, reporteCompleto);

        logger.debug("Reporte JSON guardado: {}", archivo.getAbsolutePath());
    }

    /**
     * Guarda el reporte en formato HTML.
     *
     * @param resumen resumen de trazabilidad
     */
    private void guardarReporteHtml(ResumenTrazabilidad resumen) throws IOException {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n")
                .append("<html lang='es'>\n")
                .append("<head>\n")
                .append("    <meta charset='UTF-8'>\n")
                .append("    <title>Reporte de Trazabilidad BDD</title>\n")
                .append("    <style>\n")
                .append(obtenerEstilosCSS())
                .append("    </style>\n")
                .append("</head>\n")
                .append("<body>\n");

        // Encabezado
        html.append("    <div class='header'>\n")
                .append("        <h1>Reporte de Trazabilidad BDD</h1>\n")
                .append("        <p>Generado el: ").append(resumen.fechaGeneracion.format(FORMATO_FECHA)).append("</p>\n")
                .append("    </div>\n");

        // Resumen ejecutivo
        html.append(generarSeccionResumen(resumen));

        // Tabla de historias de usuario
        html.append(generarTablaHistorias());

        // Tabla de escenarios
        html.append(generarTablaEscenarios());

        // Matriz de trazabilidad
        html.append(generarMatrizTrazabilidadHtml());

        html.append("</body>\n</html>");

        File archivo = new File(DIRECTORIO_TRAZABILIDAD, "reporte-trazabilidad.html");
        try (FileWriter writer = new FileWriter(archivo)) {
            writer.write(html.toString());
        }

        logger.debug("Reporte HTML guardado: {}", archivo.getAbsolutePath());
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Inicializa los directorios necesarios.
     */
    private void inicializarDirectorios() {
        try {
            Path directorio = Paths.get(DIRECTORIO_TRAZABILIDAD);
            Files.createDirectories(directorio);
            logger.debug("Directorio de trazabilidad creado: {}", directorio);
        } catch (IOException e) {
            logger.error("Error creando directorio de trazabilidad: {}", e.getMessage());
        }
    }

    /**
     * Carga las historias de usuario predefinidas.
     */
    private void cargarHistoriasUsuario() {
        // HU-001: Autenticación de Usuario
        RegistroHistoriaUsuario hu001 = new RegistroHistoriaUsuario();
        hu001.id = "HU-001";
        hu001.titulo = "Autenticación de Usuario";
        hu001.descripcion = "Como usuario del sistema quiero poder iniciar sesión con mis credenciales para acceder a las funcionalidades";
        hu001.criteriosAceptacion = List.of(
                "El usuario puede ingresar email y contraseña",
                "El sistema valida las credenciales",
                "Se muestra mensaje de error para credenciales inválidas",
                "Se redirige al dashboard para credenciales válidas"
        );
        hu001.prioridad = "Alta";
        hu001.estado = "En Desarrollo";
        registrosHistorias.put(hu001.id, hu001);

        // HU-002: Registro de Nuevo Usuario
        RegistroHistoriaUsuario hu002 = new RegistroHistoriaUsuario();
        hu002.id = "HU-002";
        hu002.titulo = "Registro de Nuevo Usuario";
        hu002.descripcion = "Como visitante del sitio web quiero poder registrarme en el sistema para obtener acceso a las funcionalidades";
        hu002.criteriosAceptacion = List.of(
                "Formulario con campos obligatorios",
                "Validación de formato de email",
                "Confirmación de contraseña",
                "Prevención de usuarios duplicados"
        );
        hu002.prioridad = "Alta";
        hu002.estado = "En Desarrollo";
        registrosHistorias.put(hu002.id, hu002);

        // HU-003: Gestión de Productos (CRUD)
        RegistroHistoriaUsuario hu003 = new RegistroHistoriaUsuario();
        hu003.id = "HU-003";
        hu003.titulo = "Gestión de Productos (CRUD)";
        hu003.descripcion = "Como usuario autenticado quiero gestionar productos en el sistema para mantener actualizado el catálogo";
        hu003.criteriosAceptacion = List.of(
                "Crear nuevos productos",
                "Visualizar lista de productos",
                "Editar productos existentes",
                "Eliminar productos"
        );
        hu003.prioridad = "Media";
        hu003.estado = "En Desarrollo";
        registrosHistorias.put(hu003.id, hu003);

        logger.debug("Historias de usuario cargadas: {}", registrosHistorias.size());
    }

    /**
     * Extrae la referencia a historia de usuario desde tags o nombre del escenario.
     *
     * @param nombreEscenario nombre del escenario
     * @param tags tags del escenario
     * @return ID de la historia de usuario o null
     */
    private String extraerReferenciaHistoriaUsuario(String nombreEscenario, Collection<String> tags) {
        // Buscar en tags primero
        for (String tag : tags) {
            if (tag.startsWith("@HU-") || tag.matches("@HU\\d+")) {
                return tag.substring(1); // Remover el @
            }
        }

        // Buscar en el nombre del escenario
        if (nombreEscenario.contains("HU-001") || nombreEscenario.toLowerCase().contains("login") ||
                nombreEscenario.toLowerCase().contains("autenticacion")) {
            return "HU-001";
        }

        if (nombreEscenario.contains("HU-002") || nombreEscenario.toLowerCase().contains("registro")) {
            return "HU-002";
        }

        if (nombreEscenario.contains("HU-003") || nombreEscenario.toLowerCase().contains("crud") ||
                nombreEscenario.toLowerCase().contains("producto")) {
            return "HU-003";
        }

        // Buscar por tags de funcionalidad
        for (String tag : tags) {
            switch (tag) {
                case "@Login", "@Autenticacion" -> {
                    return "HU-001";
                }
                case "@Registro" -> {
                    return "HU-002";
                }
                case "@CRUD", "@GestionProductos" -> {
                    return "HU-003";
                }
            }
        }

        return null;
    }

    /**
     * Asocia un escenario con una historia de usuario.
     *
     * @param nombreEscenario nombre del escenario
     * @param idHistoria ID de la historia de usuario
     */
    private void asociarEscenarioConHistoria(String nombreEscenario, String idHistoria) {
        mapaHistoriasEscenarios.computeIfAbsent(idHistoria, k -> new ArrayList<>()).add(nombreEscenario);

        RegistroHistoriaUsuario historia = registrosHistorias.get(idHistoria);
        if (historia != null && !historia.escenariosAsociados.contains(nombreEscenario)) {
            historia.escenariosAsociados.add(nombreEscenario);
        }
    }

    /**
     * Actualiza las estadísticas de una historia de usuario.
     *
     * @param idHistoria ID de la historia de usuario
     * @param estadoEscenario estado del escenario (PASSED, FAILED, etc.)
     */
    private void actualizarEstadisticasHistoria(String idHistoria, String estadoEscenario) {
        RegistroHistoriaUsuario historia = registrosHistorias.get(idHistoria);
        if (historia != null) {
            if ("PASSED".equals(estadoEscenario)) {
                historia.escenariosPasados++;
            } else if ("FAILED".equals(estadoEscenario)) {
                historia.escenariosFallidos++;
            }
        }
    }

    /**
     * Calcula las estadísticas finales de todas las historias.
     */
    private void calcularEstadisticasFinales() {
        registrosHistorias.values().forEach(historia -> {
            int totalEscenarios = historia.escenariosAsociados.size();
            if (totalEscenarios > 0) {
                historia.porcentajeCobertura = ((double) historia.escenariosPasados / totalEscenarios) * 100.0;
            } else {
                historia.porcentajeCobertura = 0.0;
            }

            // Actualizar estado de la historia basado en cobertura
            if (historia.porcentajeCobertura == 100.0) {
                historia.estado = "Completada";
            } else if (historia.porcentajeCobertura > 0) {
                historia.estado = "En Progreso";
            } else {
                historia.estado = "Sin Cobertura";
            }
        });
    }

    /**
     * Obtiene el nombre del escenario actual basado en el hilo.
     *
     * @return nombre del escenario actual o null
     */
    private String obtenerEscenarioActual() {
        // En un entorno real, esto podría usar ThreadLocal o el contexto de Cucumber
        // Para esta implementación, buscaremos el escenario más reciente
        return registrosEscenarios.entrySet().stream()
                .filter(entry -> "EJECUTANDO".equals(entry.getValue().estado))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Genera la sección de resumen del reporte HTML.
     *
     * @param resumen resumen de trazabilidad
     * @return HTML de la sección de resumen
     */
    private String generarSeccionResumen(ResumenTrazabilidad resumen) {
        StringBuilder html = new StringBuilder();

        html.append("    <div class='section'>\n")
                .append("        <h2>Resumen Ejecutivo</h2>\n")
                .append("        <div class='stats-grid'>\n")
                .append("            <div class='stat-card'>\n")
                .append("                <h3>").append(resumen.totalEscenarios).append("</h3>\n")
                .append("                <p>Total Escenarios</p>\n")
                .append("            </div>\n")
                .append("            <div class='stat-card success'>\n")
                .append("                <h3>").append(resumen.escenariosPasados).append("</h3>\n")
                .append("                <p>Escenarios Exitosos</p>\n")
                .append("            </div>\n")
                .append("            <div class='stat-card error'>\n")
                .append("                <h3>").append(resumen.escenariosFallidos).append("</h3>\n")
                .append("                <p>Escenarios Fallidos</p>\n")
                .append("            </div>\n")
                .append("            <div class='stat-card'>\n")
                .append("                <h3>").append(String.format("%.1f%%", resumen.coberturaGlobal)).append("</h3>\n")
                .append("                <p>Cobertura Global</p>\n")
                .append("            </div>\n")
                .append("        </div>\n")
                .append("    </div>\n");

        return html.toString();
    }

    /**
     * Genera la tabla de historias de usuario del reporte HTML.
     *
     * @return HTML de la tabla de historias
     */
    private String generarTablaHistorias() {
        StringBuilder html = new StringBuilder();

        html.append("    <div class='section'>\n")
                .append("        <h2>Historias de Usuario</h2>\n")
                .append("        <table class='data-table'>\n")
                .append("            <thead>\n")
                .append("                <tr>\n")
                .append("                    <th>ID</th>\n")
                .append("                    <th>Título</th>\n")
                .append("                    <th>Estado</th>\n")
                .append("                    <th>Escenarios</th>\n")
                .append("                    <th>Pasados</th>\n")
                .append("                    <th>Fallidos</th>\n")
                .append("                    <th>Cobertura</th>\n")
                .append("                </tr>\n")
                .append("            </thead>\n")
                .append("            <tbody>\n");

        registrosHistorias.values().forEach(historia -> {
            String claseEstado = historia.porcentajeCobertura == 100.0 ? "success" :
                    historia.porcentajeCobertura > 0 ? "warning" : "error";

            html.append("                <tr>\n")
                    .append("                    <td>").append(historia.id).append("</td>\n")
                    .append("                    <td>").append(historia.titulo).append("</td>\n")
                    .append("                    <td><span class='status ").append(claseEstado).append("'>")
                    .append(historia.estado).append("</span></td>\n")
                    .append("                    <td>").append(historia.escenariosAsociados.size()).append("</td>\n")
                    .append("                    <td>").append(historia.escenariosPasados).append("</td>\n")
                    .append("                    <td>").append(historia.escenariosFallidos).append("</td>\n")
                    .append("                    <td>").append(String.format("%.1f%%", historia.porcentajeCobertura))
                    .append("</td>\n")
                    .append("                </tr>\n");
        });

        html.append("            </tbody>\n")
                .append("        </table>\n")
                .append("    </div>\n");

        return html.toString();
    }

    /**
     * Genera la tabla de escenarios del reporte HTML.
     *
     * @return HTML de la tabla de escenarios
     */
    private String generarTablaEscenarios() {
        StringBuilder html = new StringBuilder();

        html.append("    <div class='section'>\n")
                .append("        <h2>Escenarios Ejecutados</h2>\n")
                .append("        <table class='data-table'>\n")
                .append("            <thead>\n")
                .append("                <tr>\n")
                .append("                    <th>Escenario</th>\n")
                .append("                    <th>Historia Usuario</th>\n")
                .append("                    <th>Estado</th>\n")
                .append("                    <th>Duración (s)</th>\n")
                .append("                    <th>Tags</th>\n")
                .append("                </tr>\n")
                .append("            </thead>\n")
                .append("            <tbody>\n");

        registrosEscenarios.values().forEach(escenario -> {
            String claseEstado = "PASSED".equals(escenario.estado) ? "success" :
                    "FAILED".equals(escenario.estado) ? "error" : "warning";

            html.append("                <tr>\n")
                    .append("                    <td>").append(escenario.nombre).append("</td>\n")
                    .append("                    <td>").append(escenario.historiaUsuarioReferenciada != null ?
                            escenario.historiaUsuarioReferenciada : "N/A").append("</td>\n")
                    .append("                    <td><span class='status ").append(claseEstado).append("'>")
                    .append(escenario.estado).append("</span></td>\n")
                    .append("                    <td>").append(escenario.duracionSegundos).append("</td>\n")
                    .append("                    <td>").append(String.join(", ", escenario.tags)).append("</td>\n")
                    .append("                </tr>\n");
        });

        html.append("            </tbody>\n")
                .append("        </table>\n")
                .append("    </div>\n");

        return html.toString();
    }

    /**
     * Genera la matriz de trazabilidad en HTML.
     *
     * @return HTML de la matriz de trazabilidad
     */
    private String generarMatrizTrazabilidadHtml() {
        StringBuilder html = new StringBuilder();

        html.append("    <div class='section'>\n")
                .append("        <h2>Matriz de Trazabilidad</h2>\n")
                .append("        <table class='traceability-matrix'>\n")
                .append("            <thead>\n")
                .append("                <tr>\n")
                .append("                    <th>Historia de Usuario</th>\n");

        // Obtener todos los escenarios únicos
        Set<String> todosEscenarios = new HashSet<>();
        mapaHistoriasEscenarios.values().forEach(todosEscenarios::addAll);

        todosEscenarios.forEach(escenario -> {
            html.append("                    <th class='scenario-header'>").append(escenario).append("</th>\n");
        });

        html.append("                </tr>\n")
                .append("            </thead>\n")
                .append("            <tbody>\n");

        registrosHistorias.values().forEach(historia -> {
            html.append("                <tr>\n")
                    .append("                    <td class='story-cell'>").append(historia.id).append("</td>\n");

            todosEscenarios.forEach(escenario -> {
                boolean cubierto = historia.escenariosAsociados.contains(escenario);
                RegistroEscenario registroEscenario = registrosEscenarios.get(escenario);
                String estado = registroEscenario != null ? registroEscenario.estado : "N/A";

                String claseCSS = cubierto ?
                        ("PASSED".equals(estado) ? "covered-passed" :
                                "FAILED".equals(estado) ? "covered-failed" : "covered-other") :
                        "not-covered";

                html.append("                    <td class='").append(claseCSS).append("'>")
                        .append(cubierto ? "✓" : "")
                        .append("</td>\n");
            });

            html.append("                </tr>\n");
        });

        html.append("            </tbody>\n")
                .append("        </table>\n")
                .append("    </div>\n");

        return html.toString();
    }

    /**
     * Guarda la matriz de trazabilidad en formato CSV.
     */
    private void guardarMatrizTrazabilidad() throws IOException {
        File archivo = new File(DIRECTORIO_TRAZABILIDAD, "matriz-trazabilidad.csv");

        try (FileWriter writer = new FileWriter(archivo)) {
            // Encabezados
            writer.write("Historia de Usuario,Escenarios Asociados,Estado,Cobertura\n");

            // Datos
            for (RegistroHistoriaUsuario historia : registrosHistorias.values()) {
                writer.write(String.format("%s,\"%s\",%s,%.1f%%\n",
                        historia.id,
                        String.join("; ", historia.escenariosAsociados),
                        historia.estado,
                        historia.porcentajeCobertura));
            }
        }

        logger.debug("Matriz de trazabilidad CSV guardada: {}", archivo.getAbsolutePath());
    }

    /**
     * Obtiene los estilos CSS para el reporte HTML.
     *
     * @return estilos CSS
     */
    private String obtenerEstilosCSS() {
        return """
            body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
            .header { text-align: center; padding: 20px; background-color: #2c3e50; color: white; border-radius: 8px; margin-bottom: 20px; }
            .section { background-color: white; padding: 20px; margin-bottom: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
            .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 20px; }
            .stat-card { padding: 20px; text-align: center; border-radius: 8px; background-color: #ecf0f1; }
            .stat-card.success { background-color: #d5f4e6; }
            .stat-card.error { background-color: #fadbd8; }
            .stat-card h3 { margin: 0; font-size: 2em; color: #2c3e50; }
            .data-table { width: 100%; border-collapse: collapse; margin-top: 20px; }
            .data-table th, .data-table td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
            .data-table th { background-color: #34495e; color: white; }
            .data-table tr:hover { background-color: #f5f5f5; }
            .status { padding: 4px 8px; border-radius: 4px; font-weight: bold; }
            .status.success { background-color: #2ecc71; color: white; }
            .status.error { background-color: #e74c3c; color: white; }
            .status.warning { background-color: #f39c12; color: white; }
            .traceability-matrix { font-size: 12px; }
            .traceability-matrix .scenario-header { writing-mode: vertical-lr; text-orientation: mixed; min-width: 30px; }
            .traceability-matrix .story-cell { font-weight: bold; background-color: #ecf0f1; }
            .covered-passed { background-color: #2ecc71; color: white; text-align: center; }
            .covered-failed { background-color: #e74c3c; color: white; text-align: center; }
            .covered-other { background-color: #f39c12; color: white; text-align: center; }
            .not-covered { background-color: #bdc3c7; text-align: center; }
            """;
    }

    // ==================== MÉTODOS PÚBLICOS DE CONSULTA ====================

    /**
     * Obtiene el registro de un escenario específico.
     *
     * @param nombreEscenario nombre del escenario
     * @return registro del escenario o null si no existe
     */
    public RegistroEscenario obtenerRegistroEscenario(String nombreEscenario) {
        return registrosEscenarios.get(nombreEscenario);
    }

    /**
     * Obtiene el registro de una historia de usuario específica.
     *
     * @param idHistoria ID de la historia de usuario
     * @return registro de la historia o null si no existe
     */
    public RegistroHistoriaUsuario obtenerRegistroHistoria(String idHistoria) {
        return registrosHistorias.get(idHistoria);
    }

    /**
     * Obtiene todos los escenarios asociados a una historia de usuario.
     *
     * @param idHistoria ID de la historia de usuario
     * @return lista de nombres de escenarios
     */
    public List<String> obtenerEscenariosDeHistoria(String idHistoria) {
        return mapaHistoriasEscenarios.getOrDefault(idHistoria, new ArrayList<>());
    }

    /**
     * Obtiene estadísticas generales de la ejecución.
     *
     * @return mapa con estadísticas
     */
    public Map<String, Object> obtenerEstadisticasGenerales() {
        Map<String, Object> estadisticas = new HashMap<>();

        long totalEscenarios = registrosEscenarios.size();
        long escenariosPasados = registrosEscenarios.values().stream()
                .filter(r -> "PASSED".equals(r.estado))
                .count();
        long escenariosFallidos = registrosEscenarios.values().stream()
                .filter(r -> "FAILED".equals(r.estado))
                .count();

        estadisticas.put("totalEscenarios", totalEscenarios);
        estadisticas.put("escenariosPasados", escenariosPasados);
        estadisticas.put("escenariosFallidos", escenariosFallidos);
        estadisticas.put("porcentajeExito", totalEscenarios > 0 ?
                (double) escenariosPasados / totalEscenarios * 100 : 0);

        estadisticas.put("totalHistorias", registrosHistorias.size());
        estadisticas.put("historiasCompletadas", registrosHistorias.values().stream()
                .filter(h -> h.porcentajeCobertura == 100.0)
                .count());

        return estadisticas;
    }
}