package com.qa.automatizacion.hooks;

import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import com.qa.automatizacion.utilidades.GestorBaseDatos;
import io.cucumber.java.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Hooks especializados para la gestión de base de datos durante las pruebas BDD.
 * Maneja la preparación, limpieza y validación de datos entre escenarios.
 *
 * Principios aplicados:
 * - Single Responsibility: Se enfoca únicamente en la gestión de datos
 * - Dependency Inversion: Depende de la abstracción GestorBaseDatos
 * - Open/Closed: Extensible para nuevos tipos de datos sin modificar código existente
 * - Separation of Concerns: Separa lógica de BD de lógica de negocio de tests
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
public class HooksBaseDatos {

    private static final Logger logger = LoggerFactory.getLogger(HooksBaseDatos.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final GestorBaseDatos gestorBD;
    private final PropiedadesAplicacion propiedades;

    // Control de estado de la base de datos
    private static boolean baseDatosInicializada = false;
    private static int contadorTransacciones = 0;

    // Variables para tracking
    private String nombreEscenarioActual;
    private Set<String> tagsEscenarioActual;
    private LocalDateTime inicioEscenario;

    public HooksBaseDatos() {
        this.gestorBD = new GestorBaseDatos();
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();
    }

    // ==================== HOOKS GLOBALES ====================

    /**
     * Inicialización global de la base de datos antes de todas las pruebas.
     */
    @BeforeAll
    public static void inicializarBaseDatosGlobal() {
        Logger staticLogger = LoggerFactory.getLogger(HooksBaseDatos.class);

        try {
            staticLogger.info("=== INICIALIZACIÓN GLOBAL DE BASE DE DATOS ===");

            GestorBaseDatos gestorGlobal = new GestorBaseDatos();

            // Verificar conectividad
            if (!gestorGlobal.verificarConectividad()) {
                throw new RuntimeException("No se pudo establecer conexión con la base de datos");
            }

            // Crear esquema de pruebas si no existe
            gestorGlobal.crearEsquemaPruebas();

            // Cargar datos maestros base
            gestorGlobal.cargarDatosMaestros();

            baseDatosInicializada = true;
            staticLogger.info("Base de datos inicializada exitosamente");

        } catch (Exception e) {
            staticLogger.error("Error inicializando base de datos global: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo crítico en inicialización de BD", e);
        }
    }

    /**
     * Limpieza global de la base de datos después de todas las pruebas.
     */
    @AfterAll
    public static void limpiarBaseDatosGlobal() {
        Logger staticLogger = LoggerFactory.getLogger(HooksBaseDatos.class);

        try {
            staticLogger.info("=== LIMPIEZA GLOBAL DE BASE DE DATOS ===");

            GestorBaseDatos gestorGlobal = new GestorBaseDatos();

            // Limpiar datos de prueba
            gestorGlobal.limpiarDatosPrueba();

            // Generar reporte de uso de BD si está habilitado
            if (PropiedadesAplicacion.obtenerInstancia().debeGenerarReporteBD()) {
                gestorGlobal.generarReporteUsoBaseDatos();
            }

            // Cerrar conexiones
            gestorGlobal.cerrarConexiones();

            staticLogger.info("Limpieza global de BD completada. Transacciones procesadas: {}", contadorTransacciones);

        } catch (Exception e) {
            staticLogger.error("Error en limpieza global de BD: {}", e.getMessage(), e);
        }
    }

    // ==================== HOOKS POR ESCENARIO ====================

    /**
     * Preparación de datos antes de cada escenario según sus tags.
     */
    @Before(order = 10)
    public void prepararDatosEscenario(Scenario escenario) {
        this.nombreEscenarioActual = escenario.getName();
        this.tagsEscenarioActual = escenario.getSourceTagNames();
        this.inicioEscenario = LocalDateTime.now();

        logger.info("Preparando datos para escenario: {}", nombreEscenarioActual);

        try {
            // Verificar que la BD esté lista
            if (!baseDatosInicializada) {
                throw new IllegalStateException("Base de datos no inicializada");
            }

            // Iniciar transacción para el escenario si es necesario
            if (requiereTransaccion()) {
                gestorBD.iniciarTransaccion();
                contadorTransacciones++;
            }

            // Preparar datos específicos según tags
            prepararDatosSegunTags(tagsEscenarioActual);

            // Validar estado inicial de datos
            validarEstadoInicialDatos();

            logger.debug("Datos preparados exitosamente para: {}", nombreEscenarioActual);

        } catch (Exception e) {
            logger.error("Error preparando datos para escenario {}: {}", nombreEscenarioActual, e.getMessage(), e);
            escenario.attach(generarInformeDiagnostico(e), "text/plain", "Error de preparación de BD");
            throw new RuntimeException("Fallo en preparación de datos", e);
        }
    }

    /**
     * Limpieza de datos después de cada escenario.
     */
    @After(order = 10)
    public void limpiarDatosEscenario(Scenario escenario) {
        String nombreEscenario = escenario.getName();

        try {
            logger.info("Limpiando datos para escenario: {}", nombreEscenario);

            // Si el escenario falló, capturar estado de la BD para diagnóstico
            if (escenario.isFailed()) {
                capturarEstadoBDParaDiagnostico(escenario);
            }

            // Limpiar datos específicos según tags
            limpiarDatosSegunTags(tagsEscenarioActual);

            // Finalizar transacción si está activa
            if (gestorBD.tieneTransaccionActiva()) {
                if (escenario.isFailed() && propiedades.debeRevertirTransaccionEnFallo()) {
                    gestorBD.revertirTransaccion();
                    logger.debug("Transacción revertida debido a fallo en escenario");
                } else {
                    gestorBD.confirmarTransaccion();
                    logger.debug("Transacción confirmada para escenario exitoso");
                }
            }

            // Validar limpieza
            validarLimpiezaDatos();

            long duracion = java.time.Duration.between(inicioEscenario, LocalDateTime.now()).getSeconds();
            logger.info("Limpieza completada para escenario: {} (duración: {}s)", nombreEscenario, duracion);

        } catch (Exception e) {
            logger.error("Error limpiando datos para escenario {}: {}", nombreEscenario, e.getMessage(), e);

            // Forzar rollback en caso de error
            try {
                gestorBD.forzarRollback();
            } catch (SQLException sqlEx) {
                logger.error("Error forzando rollback: {}", sqlEx.getMessage());
            }
        } finally {
            // Limpiar variables de instancia
            this.nombreEscenarioActual = null;
            this.tagsEscenarioActual = null;
            this.inicioEscenario = null;
        }
    }

    // ==================== HOOKS ESPECÍFICOS POR TAGS ====================

    /**
     * Preparación específica para escenarios de Login/Autenticación.
     */
    @Before("@Login or @Autenticacion")
    public void prepararDatosAutenticacion(Scenario escenario) {
        logger.debug("Preparando datos específicos de autenticación");

        try {
            // Crear usuarios de prueba para login
            gestorBD.prepararUsuariosPrueba();

            // Limpiar sesiones previas
            gestorBD.limpiarSesionesPrueba();

            // Configurar políticas de autenticación para pruebas
            gestorBD.configurarPoliticasAutenticacionPrueba();

            logger.debug("Datos de autenticación preparados exitosamente");

        } catch (Exception e) {
            logger.error("Error preparando datos de autenticación: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo en preparación de datos de autenticación", e);
        }
    }

    /**
     * Preparación específica para escenarios de CRUD/Productos.
     */
    @Before("@CRUD or @GestionProductos")
    public void prepararDatosProductos(Scenario escenario) {
        logger.debug("Preparando datos específicos de productos");

        try {
            // Crear categorías base
            gestorBD.prepararCategoriasPrueba();

            // Crear productos de prueba
            gestorBD.prepararProductosPrueba();

            // Configurar permisos para operaciones CRUD
            gestorBD.configurarPermisosCrudPrueba();

            logger.debug("Datos de productos preparados exitosamente");

        } catch (Exception e) {
            logger.error("Error preparando datos de productos: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo en preparación de datos de productos", e);
        }
    }

    /**
     * Preparación específica para escenarios de Registro.
     */
    @Before("@Registro")
    public void prepararDatosRegistro(Scenario escenario) {
        logger.debug("Preparando datos específicos de registro");

        try {
            // Limpiar usuarios de prueba preexistentes
            gestorBD.limpiarUsuariosPrueba();

            // Configurar validaciones de registro
            gestorBD.configurarValidacionesRegistro();

            // Preparar datos de referencia (países, ciudades, etc.)
            gestorBD.prepararDatosReferenciaRegistro();

            logger.debug("Datos de registro preparados exitosamente");

        } catch (Exception e) {
            logger.error("Error preparando datos de registro: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo en preparación de datos de registro", e);
        }
    }

    /**
     * Limpieza específica para escenarios de Performance.
     */
    @After("@Performance")
    public void limpiarDatosPerformance(Scenario escenario) {
        logger.debug("Limpieza específica para escenarios de performance");

        try {
            // Limpiar datos de gran volumen que puedan afectar performance
            gestorBD.limpiarDatosVoluminosos();

            // Resetear contadores y estadísticas
            gestorBD.resetearEstadisticas();

            // Optimizar tablas si es necesario
            if (propiedades.debeOptimizarTablasPostPerformance()) {
                gestorBD.optimizarTablasPerformance();
            }

            logger.debug("Limpieza de performance completada");

        } catch (Exception e) {
            logger.error("Error en limpieza de performance: {}", e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Prepara datos según los tags del escenario.
     */
    private void prepararDatosSegunTags(Set<String> tags) throws Exception {
        for (String tag : tags) {
            switch (tag) {
                case "@SmokeTest" -> gestorBD.prepararDatosMinimos();
                case "@Regression" -> gestorBD.prepararConjuntoCompletoDatos();
                case "@Integration" -> gestorBD.prepararDatosIntegracion();
                case "@Security" -> gestorBD.prepararDatosSeguridad();
                case "@Edge" -> gestorBD.prepararDatosCasosLimite();
            }
        }
    }

    /**
     * Limpia datos según los tags del escenario.
     */
    private void limpiarDatosSegunTags(Set<String> tags) throws Exception {
        for (String tag : tags) {
            switch (tag) {
                case "@Login", "@Autenticacion" -> gestorBD.limpiarDatosAutenticacion();
                case "@CRUD", "@GestionProductos" -> gestorBD.limpiarDatosProductos();
                case "@Registro" -> gestorBD.limpiarDatosRegistro();
                case "@Integration" -> gestorBD.limpiarDatosIntegracion();
            }
        }
    }

    /**
     * Determina si el escenario requiere transacción basado en sus tags.
     */
    private boolean requiereTransaccion() {
        if (tagsEscenarioActual == null) return false;

        return tagsEscenarioActual.stream().anyMatch(tag ->
                tag.equals("@CRUD") ||
                        tag.equals("@Registro") ||
                        tag.equals("@GestionProductos") ||
                        tag.equals("@Transaction")
        );
    }

    /**
     * Valida el estado inicial de los datos.
     */
    private void validarEstadoInicialDatos() throws Exception {
        if (!gestorBD.validarIntegridadReferencial()) {
            throw new IllegalStateException("Fallo en validación de integridad referencial");
        }

        if (!gestorBD.validarDatosConsistentes()) {
            throw new IllegalStateException("Datos inconsistentes detectados");
        }
    }

    /**
     * Valida que la limpieza de datos fue exitosa.
     */
    private void validarLimpiezaDatos() throws Exception {
        if (propiedades.debeValidarLimpiezaEstricta()) {
            if (gestorBD.tieneRegistrosPrueba()) {
                logger.warn("Advertencia: Aún existen registros de prueba después de la limpieza");
            }
        }
    }

    /**
     * Captura el estado de la BD para diagnóstico cuando un escenario falla.
     */
    private void capturarEstadoBDParaDiagnostico(Scenario escenario) {
        try {
            String estadoBD = gestorBD.generarEstadoActualBD();
            escenario.attach(estadoBD, "text/plain", "Estado BD al fallar");

            String consultasRecientes = gestorBD.obtenerConsultasRecientes();
            escenario.attach(consultasRecientes, "text/plain", "Consultas recientes BD");

            logger.debug("Estado de BD capturado para diagnóstico del fallo");

        } catch (Exception e) {
            logger.warn("No se pudo capturar estado de BD para diagnóstico: {}", e.getMessage());
        }
    }

    /**
     * Genera un informe de diagnóstico detallado.
     */
    private String generarInformeDiagnostico(Exception error) {
        StringBuilder informe = new StringBuilder();

        informe.append("=== DIAGNÓSTICO DE ERROR BD ===\n");
        informe.append("Timestamp: ").append(LocalDateTime.now().format(FORMATO_FECHA)).append("\n");
        informe.append("Escenario: ").append(nombreEscenarioActual).append("\n");
        informe.append("Tags: ").append(tagsEscenarioActual).append("\n");
        informe.append("Error: ").append(error.getMessage()).append("\n");

        try {
            informe.append("Estado conexión BD: ").append(gestorBD.obtenerEstadoConexion()).append("\n");
            informe.append("Transacciones activas: ").append(gestorBD.contarTransaccionesActivas()).append("\n");
        } catch (Exception e) {
            informe.append("Error obteniendo info BD: ").append(e.getMessage()).append("\n");
        }

        informe.append("=== FIN DIAGNÓSTICO ===");

        return informe.toString();
    }
}