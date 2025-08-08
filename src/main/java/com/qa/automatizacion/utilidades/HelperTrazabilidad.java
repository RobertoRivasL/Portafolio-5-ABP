package com.qa.automatizacion.utilidades;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper centralizado para gestión de trazabilidad con historias de usuario.
 * Rastrea la ejecución de escenarios y su relación con historias de usuario.
 *
 * Funcionalidades:
 * - Mapeo de escenarios con historias de usuario
 * - Registro de pasos ejecutados por historia
 * - Generación de reportes de trazabilidad
 * - Métricas de cobertura de historias
 * - Seguimiento de resultados por historia
 *
 * Principios aplicados:
 * - Singleton Pattern: Una sola instancia para toda la ejecución
 * - Observer Pattern: Registra eventos de ejecución
 * - Strategy Pattern: Diferentes formatos de reporte
 * - Thread-Safe: Uso de ConcurrentHashMap para concurrencia
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0
 */
public class HelperTrazabilidad {

    private static final Logger logger = LoggerFactory.getLogger(HelperTrazabilidad.class);

    // Instancia única (Singleton)
    private static HelperTrazabilidad instancia;

    // Datos de trazabilidad thread-safe
    private final Map<String, HistoriaUsuario> historiasUsuario = new ConcurrentHashMap<>();
    private final Map<String, EscenarioEjecucion> escenariosEjecutados = new ConcurrentHashMap<>();
    private final List<EventoTrazabilidad> eventosEjecucion = Collections.synchronizedList(new ArrayList<>());

    // Configuración
    private final ObjectMapper objectMapper;
    private LocalDateTime inicioSesion;
    private LocalDateTime finSesion;
    private String archivoReporte = "trazabilidad-reporte.json";

    // Métricas de sesión
    private int totalEscenariosEjecutados = 0;
    private int totalEscenariosPasados = 0;
    private int totalEscenariosFallidos = 0;

    /**
     * Constructor privado para Singleton Pattern.
     */
    private HelperTrazabilidad() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        logger.debug("HelperTrazabilidad inicializado");
    }

    /**
     * Obtiene la instancia única del helper (Singleton Pattern).
     *
     * @return instancia única de HelperTrazabilidad
     */
    public static synchronized HelperTrazabilidad obtenerInstancia() {
        if (instancia == null) {
            instancia = new HelperTrazabilidad();
        }
        return instancia;
    }

    // ==================== GESTIÓN DE SESIÓN ====================

    /**
     * Inicializa una nueva sesión de ejecución.
     */
    public void inicializarSesionEjecucion() {
        this.inicioSesion = LocalDateTime.now();

        logger.info("🎯 Sesión de trazabilidad iniciada: {}",
                inicioSesion.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Registrar evento
        registrarEvento("SESION_INICIADA", "Inicio de sesión de ejecución", null);
    }

    /**
     * Finaliza la sesión de ejecución y genera reportes.
     */
    public void finalizarSesionEjecucion() {
        this.finSesion = LocalDateTime.now();

        logger.info("🏁 Sesión de trazabilidad finalizada: {}",
                finSesion.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Registrar evento
        registrarEvento("SESION_FINALIZADA", "Fin de sesión de ejecución", null);

        // Generar reportes
        try {
            generarReporteTrazabilidad();
            generarResumenEjecucion();

        } catch (Exception e) {
            logger.error("Error generando reportes de trazabilidad: {}", e.getMessage(), e);
        }
    }

    // ==================== GESTIÓN DE ESCENARIOS ====================

    /**
     * Registra el inicio de un escenario.
     *
     * @param historiaUsuario ID de la historia de usuario
     * @param nombreEscenario nombre del escenario
     */
    public void iniciarEscenario(String historiaUsuario, String nombreEscenario) {
        logger.debug("📋 Iniciando escenario: {} - {}", historiaUsuario, nombreEscenario);

        // Crear o actualizar historia de usuario
        HistoriaUsuario hu = historiasUsuario.computeIfAbsent(historiaUsuario,
                k -> new HistoriaUsuario(k));

        // Crear ejecución de escenario
        EscenarioEjecucion escenario = new EscenarioEjecucion(
                nombreEscenario, historiaUsuario, LocalDateTime.now());

        String claveEscenario = generarClaveEscenario(historiaUsuario, nombreEscenario);
        escenariosEjecutados.put(claveEscenario, escenario);

        // Agregar a historia de usuario
        hu.agregarEscenario(escenario);

        // Registrar evento
        registrarEvento("ESCENARIO_INICIADO", nombreEscenario, historiaUsuario);

        totalEscenariosEjecutados++;
    }

    /**
     * Registra la finalización de un escenario.
     *
     * @param historiaUsuario ID de la historia de usuario
     * @param resultado resultado del escenario (PASSED, FAILED, SKIPPED)
     */
    public void finalizarEscenario(String historiaUsuario, String resultado) {
        logger.debug("📋 Finalizando escenario: {} - {}", historiaUsuario, resultado);

        // Buscar escenario actual de esta historia
        EscenarioEjecucion escenarioActual = encontrarEscenarioActual(historiaUsuario);

        if (escenarioActual != null) {
            escenarioActual.finalizarEjecucion(resultado);

            // Actualizar métricas
            if ("PASSED".equalsIgnoreCase(resultado)) {
                totalEscenariosPasados++;
            } else {
                totalEscenariosFallidos++;
            }

            // Registrar evento
            registrarEvento("ESCENARIO_FINALIZADO",
                    escenarioActual.getNombre() + " - " + resultado, historiaUsuario);

            logger.debug("Escenario finalizado: {} con resultado: {}",
                    escenarioActual.getNombre(), resultado);
        } else {
            logger.warn("No se encontró escenario activo para historia: {}", historiaUsuario);
        }
    }

    /**
     * Registra un paso ejecutado.
     *
     * @param historiaUsuario ID de la historia de usuario
     * @param descripcionPaso descripción del paso
     */
    public void registrarPaso(String historiaUsuario, String descripcionPaso) {
        logger.debug("📝 Registrando paso [{}]: {}", historiaUsuario, descripcionPaso);

        // Buscar escenario actual
        EscenarioEjecucion escenarioActual = encontrarEscenarioActual(historiaUsuario);

        if (escenarioActual != null) {
            PasoEjecucion paso = new PasoEjecucion(descripcionPaso, LocalDateTime.now());
            escenarioActual.agregarPaso(paso);

            // Registrar evento
            registrarEvento("PASO_EJECUTADO", descripcionPaso, historiaUsuario);
        }
    }

    /**
     * Registra una acción específica.
     *
     * @param accion tipo de acción
     * @param detalle detalle de la acción
     */
    public void registrarAccion(String accion, String detalle) {
        registrarEvento(accion, detalle, "ACTUAL");
    }

    /**
     * Registra navegación a una URL.
     *
     * @param url URL de destino
     */
    public void registrarNavegacion(String url) {
        registrarEvento("NAVEGACION", "Navegando a: " + url, "ACTUAL");
    }

    // ==================== GESTIÓN DE DATOS ====================

    /**
     * Encuentra el escenario actual de una historia de usuario.
     */
    private EscenarioEjecucion encontrarEscenarioActual(String historiaUsuario) {
        return escenariosEjecutados.values().stream()
                .filter(escenario -> historiaUsuario.equals(escenario.getHistoriaUsuario()))
                .filter(escenario -> escenario.getFin() == null) // Escenario aún en ejecución
                .findFirst()
                .orElse(null);
    }

    /**
     * Genera clave única para un escenario.
     */
    private String generarClaveEscenario(String historiaUsuario, String nombreEscenario) {
        return historiaUsuario + "_" + nombreEscenario.replaceAll("[^a-zA-Z0-9]", "_") +
                "_" + System.currentTimeMillis();
    }

    /**
     * Registra un evento de trazabilidad.
     */
    private void registrarEvento(String tipo, String descripcion, String historiaUsuario) {
        EventoTrazabilidad evento = new EventoTrazabilidad(
                tipo, descripcion, historiaUsuario, LocalDateTime.now());
        eventosEjecucion.add(evento);
    }

    // ==================== GENERACIÓN DE REPORTES ====================

    /**
     * Genera el reporte completo de trazabilidad.
     */
    public void generarReporteTrazabilidad() {
        try {
            logger.info("📊 Generando reporte de trazabilidad");

            // Crear estructura del reporte
            Map<String, Object> reporte = new HashMap<>();
            reporte.put("metadata", crearMetadataReporte());
            reporte.put("metricas", crearMetricasReporte());
            reporte.put("historias_usuario", crearResumenHistorias());
            reporte.put("escenarios", crearResumenEscenarios());
            reporte.put("eventos", eventosEjecucion);

            // Escribir archivo JSON
            Path rutaReporte = Paths.get("reportes", archivoReporte);
            Files.createDirectories(rutaReporte.getParent());

            objectMapper.writeValue(rutaReporte.toFile(), reporte);

            logger.info("✅ Reporte de trazabilidad generado: {}", rutaReporte.toAbsolutePath());

        } catch (IOException e) {
            logger.error("Error generando reporte de trazabilidad: {}", e.getMessage(), e);
        }
    }

    /**
     * Crea metadata del reporte.
     */
    private Map<String, Object> crearMetadataReporte() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fecha_generacion", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        metadata.put("inicio_sesion", inicioSesion != null ?
                inicioSesion.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        metadata.put("fin_sesion", finSesion != null ?
                finSesion.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        metadata.put("duracion_sesion_minutos", calcularDuracionSesion());
        metadata.put("version_reporte", "2.0.0");
        metadata.put("generado_por", "HelperTrazabilidad");

        return metadata;
    }

    /**
     * Crea métricas del reporte.
     */
    private Map<String, Object> crearMetricasReporte() {
        Map<String, Object> metricas = new HashMap<>();

        // Métricas básicas
        metricas.put("total_historias_usuario", historiasUsuario.size());
        metricas.put("total_escenarios_ejecutados", totalEscenariosEjecutados);
        metricas.put("total_escenarios_pasados", totalEscenariosPasados);
        metricas.put("total_escenarios_fallidos", totalEscenariosFallidos);

        // Porcentajes
        if (totalEscenariosEjecutados > 0) {
            double porcentajeExito = (double) totalEscenariosPasados / totalEscenariosEjecutados * 100;
            metricas.put("porcentaje_exito", Math.round(porcentajeExito * 100.0) / 100.0);
        }

        // Métricas por historia de usuario
        metricas.put("historias_completamente_cubiertas", contarHistoriasCompletamenteCubiertas());
        metricas.put("historias_parcialmente_cubiertas", contarHistoriasParcialmenteCubiertas());
        metricas.put("historias_sin_cobertura", contarHistoriasSinCobertura());

        // Estadísticas de pasos
        metricas.put("total_pasos_ejecutados", contarTotalPasos());
        metricas.put("promedio_pasos_por_escenario", calcularPromedioPasosPorEscenario());

        return metricas;
    }

    /**
     * Crea resumen de historias de usuario.
     */
    private List<Map<String, Object>> crearResumenHistorias() {
        List<Map<String, Object>> resumen = new ArrayList<>();

        for (HistoriaUsuario hu : historiasUsuario.values()) {
            Map<String, Object> historiaMap = new HashMap<>();
            historiaMap.put("id", hu.getId());
            historiaMap.put("total_escenarios", hu.getEscenarios().size());
            historiaMap.put("escenarios_pasados", hu.contarEscenariosPorEstado("PASSED"));
            historiaMap.put("escenarios_fallidos", hu.contarEscenariosPorEstado("FAILED"));
            historiaMap.put("estado_cobertura", determinarEstadoCobertura(hu));
            historiaMap.put("primer_ejecucion", hu.obtenerPrimeraEjecucion());
            historiaMap.put("ultima_ejecucion", hu.obtenerUltimaEjecucion());

            resumen.add(historiaMap);
        }

        return resumen;
    }

    /**
     * Crea resumen de escenarios.
     */
    private List<Map<String, Object>> crearResumenEscenarios() {
        List<Map<String, Object>> resumen = new ArrayList<>();

        for (EscenarioEjecucion escenario : escenariosEjecutados.values()) {
            if (escenario.getFin() != null) { // Solo escenarios completados
                Map<String, Object> escenarioMap = new HashMap<>();
                escenarioMap.put("nombre", escenario.getNombre());
                escenarioMap.put("historia_usuario", escenario.getHistoriaUsuario());
                escenarioMap.put("resultado", escenario.getResultado());
                escenarioMap.put("inicio", escenario.getInicio().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                escenarioMap.put("fin", escenario.getFin().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                escenarioMap.put("duracion_segundos", escenario.obtenerDuracionSegundos());
                escenarioMap.put("total_pasos", escenario.getPasos().size());

                resumen.add(escenarioMap);
            }
        }

        return resumen;
    }

    /**
     * Genera resumen ejecutivo de la ejecución.
     */
    public void generarResumenEjecucion() {
        try {
            logger.info("📈 Generando resumen ejecutivo");

            StringBuilder resumen = new StringBuilder();
            resumen.append("=".repeat(80)).append("\n");
            resumen.append("RESUMEN EJECUTIVO DE TRAZABILIDAD BDD").append("\n");
            resumen.append("=".repeat(80)).append("\n");

            // Información general
            resumen.append(String.format("Fecha: %s%n",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
            resumen.append(String.format("Duración de sesión: %d minutos%n", calcularDuracionSesion()));
            resumen.append("\n");

            // Métricas principales
            resumen.append("MÉTRICAS PRINCIPALES:\n");
            resumen.append(String.format("• Total de historias de usuario: %d%n", historiasUsuario.size()));
            resumen.append(String.format("• Total de escenarios ejecutados: %d%n", totalEscenariosEjecutados));
            resumen.append(String.format("• Escenarios exitosos: %d%n", totalEscenariosPasados));
            resumen.append(String.format("• Escenarios fallidos: %d%n", totalEscenariosFallidos));

            if (totalEscenariosEjecutados > 0) {
                double porcentaje = (double) totalEscenariosPasados / totalEscenariosEjecutados * 100;
                resumen.append(String.format("• Porcentaje de éxito: %.2f%%%n", porcentaje));
            }
            resumen.append("\n");

            // Cobertura de historias
            resumen.append("COBERTURA DE HISTORIAS DE USUARIO:\n");
            resumen.append(String.format("• Completamente cubiertas: %d%n", contarHistoriasCompletamenteCubiertas()));
            resumen.append(String.format("• Parcialmente cubiertas: %d%n", contarHistoriasParcialmenteCubiertas()));
            resumen.append(String.format("• Sin cobertura: %d%n", contarHistoriasSinCobertura()));
            resumen.append("\n");

            // Top historias por escenarios
            resumen.append("TOP 5 HISTORIAS CON MÁS ESCENARIOS:\n");
            historiasUsuario.values().stream()
                    .sorted((h1, h2) -> Integer.compare(h2.getEscenarios().size(), h1.getEscenarios().size()))
                    .limit(5)
                    .forEach(hu -> resumen.append(String.format("• %s: %d escenarios%n",
                            hu.getId(), hu.getEscenarios().size())));

            resumen.append("\n");
            resumen.append("=".repeat(80));

            // Escribir archivo
            Path rutaResumen = Paths.get("reportes", "resumen-ejecutivo.txt");
            Files.createDirectories(rutaResumen.getParent());
            Files.write(rutaResumen, resumen.toString().getBytes());

            logger.info("✅ Resumen ejecutivo generado: {}", rutaResumen.toAbsolutePath());

            // También loggear el resumen
            logger.info("\n{}", resumen.toString());

        } catch (IOException e) {
            logger.error("Error generando resumen ejecutivo: {}", e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS DE CÁLCULO ====================

    private long calcularDuracionSesion() {
        if (inicioSesion == null) return 0;
        LocalDateTime fin = finSesion != null ? finSesion : LocalDateTime.now();
        return java.time.Duration.between(inicioSesion, fin).toMinutes();
    }

    private int contarHistoriasCompletamenteCubiertas() {
        return (int) historiasUsuario.values().stream()
                .filter(hu -> hu.getEscenarios().size() > 0 &&
                        hu.getEscenarios().stream().allMatch(e -> "PASSED".equals(e.getResultado())))
                .count();
    }

    private int contarHistoriasParcialmenteCubiertas() {
        return (int) historiasUsuario.values().stream()
                .filter(hu -> hu.getEscenarios().size() > 0 &&
                        hu.getEscenarios().stream().anyMatch(e -> "PASSED".equals(e.getResultado())) &&
                        hu.getEscenarios().stream().anyMatch(e -> !"PASSED".equals(e.getResultado())))
                .count();
    }

    private int contarHistoriasSinCobertura() {
        return (int) historiasUsuario.values().stream()
                .filter(hu -> hu.getEscenarios().isEmpty() ||
                        hu.getEscenarios().stream().noneMatch(e -> "PASSED".equals(e.getResultado())))
                .count();
    }

    private int contarTotalPasos() {
        return escenariosEjecutados.values().stream()
                .mapToInt(escenario -> escenario.getPasos().size())
                .sum();
    }

    private double calcularPromedioPasosPorEscenario() {
        if (totalEscenariosEjecutados == 0) return 0.0;
        return (double) contarTotalPasos() / totalEscenariosEjecutados;
    }

    private String determinarEstadoCobertura(HistoriaUsuario hu) {
        if (hu.getEscenarios().isEmpty()) {
            return "SIN_COBERTURA";
        }

        long pasados = hu.getEscenarios().stream()
                .filter(e -> "PASSED".equals(e.getResultado()))
                .count();

        if (pasados == hu.getEscenarios().size()) {
            return "COMPLETAMENTE_CUBIERTA";
        } else if (pasados > 0) {
            return "PARCIALMENTE_CUBIERTA";
        } else {
            return "COBERTURA_FALLIDA";
        }
    }

    // ==================== MÉTODOS DE CONSULTA ====================

    /**
     * Obtiene todas las historias de usuario registradas.
     */
    public Collection<HistoriaUsuario> obtenerHistoriasUsuario() {
        return new ArrayList<>(historiasUsuario.values());
    }

    /**
     * Obtiene una historia de usuario específica.
     */
    public HistoriaUsuario obtenerHistoriaUsuario(String id) {
        return historiasUsuario.get(id);
    }

    /**
     * Obtiene todos los escenarios ejecutados.
     */
    public Collection<EscenarioEjecucion> obtenerEscenariosEjecutados() {
        return new ArrayList<>(escenariosEjecutados.values());
    }

    /**
     * Obtiene métricas actuales de ejecución.
     */
    public Map<String, Object> obtenerMetricasActuales() {
        return crearMetricasReporte();
    }

    // ==================== MÉTODOS DE LIMPIEZA ====================

    /**
     * Limpia datos temporales pero mantiene la configuración.
     */
    public void limpiarDatosTempo() {
        eventosEjecucion.clear();
        logger.debug("Datos temporales de trazabilidad limpiados");
    }

    /**
     * Reinicia completamente el helper.
     */
    public void reiniciar() {
        historiasUsuario.clear();
        escenariosEjecutados.clear();
        eventosEjecucion.clear();

        totalEscenariosEjecutados = 0;
        totalEscenariosPasados = 0;
        totalEscenariosFallidos = 0;

        inicioSesion = null;
        finSesion = null;

        logger.info("HelperTrazabilidad reiniciado completamente");
    }

    // ==================== CLASES INTERNAS ====================

    /**
     * Representa una historia de usuario con sus escenarios asociados.
     */
    public static class HistoriaUsuario {
        private final String id;
        private final List<EscenarioEjecucion> escenarios = new ArrayList<>();
        private final LocalDateTime fechaCreacion = LocalDateTime.now();

        public HistoriaUsuario(String id) {
            this.id = id;
        }

        public void agregarEscenario(EscenarioEjecucion escenario) {
            escenarios.add(escenario);
        }

        public int contarEscenariosPorEstado(String estado) {
            return (int) escenarios.stream()
                    .filter(e -> estado.equals(e.getResultado()))
                    .count();
        }

        public LocalDateTime obtenerPrimeraEjecucion() {
            return escenarios.stream()
                    .map(EscenarioEjecucion::getInicio)
                    .min(LocalDateTime::compareTo)
                    .orElse(fechaCreacion);
        }

        public LocalDateTime obtenerUltimaEjecucion() {
            return escenarios.stream()
                    .map(EscenarioEjecucion::getInicio)
                    .max(LocalDateTime::compareTo)
                    .orElse(fechaCreacion);
        }

        // Getters
        public String getId() { return id; }
        public List<EscenarioEjecucion> getEscenarios() { return new ArrayList<>(escenarios); }
        public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    }

    /**
     * Representa la ejecución de un escenario específico.
     */
    public static class EscenarioEjecucion {
        private final String nombre;
        private final String historiaUsuario;
        private final LocalDateTime inicio;
        private LocalDateTime fin;
        private String resultado;
        private final List<PasoEjecucion> pasos = new ArrayList<>();

        public EscenarioEjecucion(String nombre, String historiaUsuario, LocalDateTime inicio) {
            this.nombre = nombre;
            this.historiaUsuario = historiaUsuario;
            this.inicio = inicio;
        }

        public void finalizarEjecucion(String resultado) {
            this.fin = LocalDateTime.now();
            this.resultado = resultado;
        }

        public void agregarPaso(PasoEjecucion paso) {
            pasos.add(paso);
        }

        public long obtenerDuracionSegundos() {
            if (fin == null) return 0;
            return java.time.Duration.between(inicio, fin).getSeconds();
        }

        // Getters
        public String getNombre() { return nombre; }
        public String getHistoriaUsuario() { return historiaUsuario; }
        public LocalDateTime getInicio() { return inicio; }
        public LocalDateTime getFin() { return fin; }
        public String getResultado() { return resultado; }
        public List<PasoEjecucion> getPasos() { return new ArrayList<>(pasos); }
    }

    /**
     * Representa la ejecución de un paso individual.
     */
    public static class PasoEjecucion {
        private final String descripcion;
        private final LocalDateTime timestamp;

        public PasoEjecucion(String descripcion, LocalDateTime timestamp) {
            this.descripcion = descripcion;
            this.timestamp = timestamp;
        }

        // Getters
        public String getDescripcion() { return descripcion; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * Representa un evento de trazabilidad.
     */
    public static class EventoTrazabilidad {
        private final String tipo;
        private final String descripcion;
        private final String historiaUsuario;
        private final LocalDateTime timestamp;

        public EventoTrazabilidad(String tipo, String descripcion, String historiaUsuario, LocalDateTime timestamp) {
            this.tipo = tipo;
            this.descripcion = descripcion;
            this.historiaUsuario = historiaUsuario;
            this.timestamp = timestamp;
        }

        // Getters
        public String getTipo() { return tipo; }
        public String getDescripcion() { return descripcion; }
        public String getHistoriaUsuario() { return historiaUsuario; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}