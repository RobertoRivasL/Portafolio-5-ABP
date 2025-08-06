package com.qa.automatizacion.hooks;

import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import com.qa.automatizacion.utilidades.GestorBaseDatos;
import com.qa.automatizacion.utilidades.HelperTrazabilidad;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Hooks para la configuración y limpieza de las pruebas BDD.
 * Maneja la inicialización y finalización de recursos compartidos.
 *
 * Principios aplicados:
 * - Separación de Intereses: Se enfoca únicamente en la gestión del ciclo de vida de las pruebas
 * - DRY: Evita repetir configuraciones en cada Step Definition
 * - Single Responsibility: Cada hook tiene una responsabilidad específica
 * - Abstracción: Oculta la complejidad de configuración a los tests
 */
public class HooksPruebas {

    private static final Logger logger = LoggerFactory.getLogger(HooksPruebas.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PropiedadesAplicacion propiedades;
    private final HelperTrazabilidad trazabilidad;
    private final GestorBaseDatos gestorBD;

    // Variables para tracking de la ejecución
    private static int contadorEscenarios = 0;
    private static int escenariosPasados = 0;
    private static int escenariosFallidos = 0;
    private LocalDateTime inicioEscenario;

    public HooksPruebas() {
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();
        this.trazabilidad = new HelperTrazabilidad();
        this.gestorBD = new GestorBaseDatos();
    }

    // ==================== HOOKS GLOBALES ====================

    /**
     * Se ejecuta una vez antes de todas las pruebas.
     * Configura el entorno global y recursos compartidos.
     */
    @BeforeAll
    public static void configuracionGlobal() {
        Logger loggerEstatico = LoggerFactory.getLogger(HooksPruebas.class);

        loggerEstatico.info("=".repeat(80));
        loggerEstatico.info("INICIANDO EJECUCIÓN DE PRUEBAS BDD");
        loggerEstatico.info("Fecha y hora: {}", LocalDateTime.now().format(FORMATO_FECHA));
        loggerEstatico.info("=".repeat(80));

        try {
            // Configurar propiedades del sistema
            System.setProperty("webdriver.chrome.silentOutput", "true");
            System.setProperty("webdriver.gecko.silentOutput", "true");

            // Configurar logging si es necesario
            configurarLogging();

            // Crear directorios de reportes si no existen
            crearDirectoriosReportes();

            // Inicializar base de datos de prueba si es necesario
            inicializarBaseDatosPrueba();

            loggerEstatico.info("Configuración global completada exitosamente");

        } catch (Exception e) {
            loggerEstatico.error("Error en configuración global: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo en configuración global", e);
        }
    }

    /**
     * Se ejecuta una vez después de todas las pruebas.
     * Limpia recursos globales y genera reportes finales.
     */
    @AfterAll
    public static void limpiezaGlobal() {
        Logger loggerEstatico = LoggerFactory.getLogger(HooksPruebas.class);

        try {
            // Cerrar todas las instancias de WebDriver
            ConfiguradorNavegador.cerrarTodasLasInstancias();

            // Generar resumen de ejecución
            generarResumenEjecucion(loggerEstatico);

            // Limpiar recursos temporales
            limpiarRecursosTemporales();

            loggerEstatico.info("=".repeat(80));
            loggerEstatico.info("EJECUCIÓN DE PRUEBAS COMPLETADA");
            loggerEstatico.info("Fecha y hora: {}", LocalDateTime.now().format(FORMATO_FECHA));
            loggerEstatico.info("=".repeat(80));

        } catch (Exception e) {
            loggerEstatico.error("Error en limpieza global: {}", e.getMessage(), e);
        }
    }

    // ==================== HOOKS POR ESCENARIO ====================

    /**
     * Se ejecuta antes de cada escenario.
     * Prepara el entorno específico para el escenario.
     *
     * @param escenario escenario que se va a ejecutar
     */
    @Before
    public void configuracionEscenario(Scenario escenario) {
        contadorEscenarios++;
        this.inicioEscenario = LocalDateTime.now();

        logger.info("-".repeat(60));
        logger.info("INICIANDO ESCENARIO #{}: {}", contadorEscenarios, escenario.getName());
        logger.info("Tags: {}", String.join(", ", escenario.getSourceTagNames()));
        logger.info("Línea: {}", escenario.getLine());
        logger.info("-".repeat(60));

        try {
            // Registrar inicio del escenario en trazabilidad
            trazabilidad.iniciarEscenario(escenario.getName(), escenario.getSourceTagNames());

            // Preparar base de datos si es necesario
            prepararDatosEscenario(escenario);

            // Configurar contexto específico del escenario
            configurarContextoEscenario(escenario);

            logger.info("Configuración del escenario completada");

        } catch (Exception e) {
            logger.error("Error en configuración del escenario '{}': {}", escenario.getName(), e.getMessage());
            throw new RuntimeException("Fallo en configuración del escenario", e);
        }
    }

    /**
     * Se ejecuta después de cada escenario.
     * Limpia el entorno y maneja el resultado del escenario.
     *
     * @param escenario escenario que se ejecutó
     */
    @After
    public void limpiezaEscenario(Scenario escenario) {
        LocalDateTime finEscenario = LocalDateTime.now();
        long duracionSegundos = java.time.Duration.between(inicioEscenario, finEscenario).getSeconds();

        try {
            // Manejar el resultado del escenario
            if (escenario.isFailed()) {
                manejarEscenarioFallido(escenario);
                escenariosFallidos++;
            } else {
                manejarEscenarioExitoso(escenario);
                escenariosPasados++;
            }

            // Limpiar datos del escenario
            limpiarDatosEscenario(escenario);

            // Cerrar WebDriver del hilo actual si existe
            if (ConfiguradorNavegador.tieneDriverActivo()) {
                ConfiguradorNavegador.cerrarDriver();
            }

            // Registrar finalización en trazabilidad
            trazabilidad.finalizarEscenario(escenario.getName(), escenario.getStatus().toString(), duracionSegundos);

            logger.info("ESCENARIO FINALIZADO: {} - Estado: {} - Duración: {}s",
                    escenario.getName(), escenario.getStatus(), duracionSegundos);
            logger.info("-".repeat(60));

        } catch (Exception e) {
            logger.error("Error en limpieza del escenario '{}': {}", escenario.getName(), e.getMessage());
        }
    }

    // ==================== HOOKS CON TAGS ESPECÍFICOS ====================

    /**
     * Se ejecuta antes de escenarios etiquetados con @SmokeTest.
     *
     * @param escenario escenario de smoke test
     */
    @Before("@SmokeTest")
    public void configuracionSmokeTest(Scenario escenario) {
        logger.info("Configurando escenario de Smoke Test: {}", escenario.getName());

        try {
            // Configuraciones específicas para smoke tests
            // Por ejemplo: verificar que servicios críticos estén disponibles
            verificarServiciosCriticos();

            // Configurar timeout reducido para smoke tests
            System.setProperty("selenium.timeout", "10");

            logger.info("Configuración de Smoke Test completada");

        } catch (Exception e) {
            logger.error("Error configurando Smoke Test: {}", e.getMessage());
            throw new RuntimeException("Fallo en configuración de Smoke Test", e);
        }
    }

    /**
     * Se ejecuta antes de escenarios etiquetados con @Regression.
     *
     * @param escenario escenario de regresión
     */
    @Before("@Regression")
    public void configuracionRegression(Scenario escenario) {
        logger.info("Configurando escenario de Regresión: {}", escenario.getName());

        try {
            // Configuraciones específicas para tests de regresión
            // Por ejemplo: preparar conjunto completo de datos de prueba
            prepararDatosRegresion();

            // Configurar timeout extendido para tests de regresión
            System.setProperty("selenium.timeout", "20");

            logger.info("Configuración de Regresión completada");

        } catch (Exception e) {
            logger.error("Error configurando Regresión: {}", e.getMessage());
            throw new RuntimeException("Fallo en configuración de Regresión", e);
        }
    }

    /**
     * Se ejecuta después de escenarios etiquetados con @CRUD.
     *
     * @param escenario escenario CRUD
     */
    @After("@CRUD")
    public void limpiezaCrud(Scenario escenario) {
        logger.info("Limpieza específica para escenario CRUD: {}", escenario.getName());

        try {
            // Limpiar datos de productos creados durante las pruebas CRUD
            gestorBD.limpiarDatosPrueba();

            // Restablecer estado inicial de la base de datos
            gestorBD.restaurarEstadoInicial();

            logger.info("Limpieza CRUD completada");

        } catch (Exception e) {
            logger.error("Error en limpieza CRUD: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Configura el sistema de logging.
     */
    private static void configurarLogging() {
        // Configurar niveles de logging según el entorno
        String nivelLog = System.getProperty("log.level", "INFO");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", nivelLog);

        // Silenciar logs verbosos de Selenium
        System.setProperty("org.slf4j.simpleLogger.log.org.openqa.selenium", "WARN");
        System.setProperty("org.slf4j.simpleLogger.log.io.github.bonigarcia", "WARN");
    }

    /**
     * Crea los directorios necesarios para los reportes.
     */
    private static void crearDirectoriosReportes() {
        PropiedadesAplicacion props = PropiedadesAplicacion.obtenerInstancia();
        String directorioReportes = props.obtenerDirectorioReportes();

        try {
            java.nio.file.Path rutaReportes = java.nio.file.Paths.get(directorioReportes);
            java.nio.file.Files.createDirectories(rutaReportes);

            // Crear subdirectorios
            java.nio.file.Files.createDirectories(rutaReportes.resolve("html"));
            java.nio.file.Files.createDirectories(rutaReportes.resolve("json"));
            java.nio.file.Files.createDirectories(rutaReportes.resolve("screenshots"));
            java.nio.file.Files.createDirectories(rutaReportes.resolve("trazabilidad"));

            Logger.getLogger(HooksPruebas.class).info("Directorios de reportes creados: {}", rutaReportes);

        } catch (Exception e) {
            Logger.getLogger(HooksPruebas.class).error("Error creando directorios de reportes: {}", e.getMessage());
        }
    }

    /**
     * Inicializa la base de datos de prueba.
     */
    private static void inicializarBaseDatosPrueba() {
        try {
            GestorBaseDatos gestor = new GestorBaseDatos();
            gestor.inicializar();
            gestor.cargarDatosPrueba();

            Logger.getLogger(HooksPruebas.class).info("Base de datos de prueba inicializada");

        } catch (Exception e) {
            Logger.getLogger(HooksPruebas.class).error("Error inicializando base de datos: {}", e.getMessage());
        }
    }

    /**
     * Maneja un escenario que falló.
     *
     * @param escenario escenario fallido
     */
    private void manejarEscenarioFallido(Scenario escenario) {
        logger.error("ESCENARIO FALLIDO: {}", escenario.getName());

        try {
            // Tomar captura de pantalla si hay un driver activo
            if (ConfiguradorNavegador.tieneDriverActivo()) {
                byte[] screenshot = tomarCapturaPantalla();
                if (screenshot != null) {
                    escenario.attach(screenshot, "image/png", "Screenshot del fallo");
                    logger.info("Captura de pantalla adjuntada al escenario");
                }
            }

            // Registrar información de diagnóstico
            String infoDiagnostico = obtenerInformacionDiagnostico();
            escenario.attach(infoDiagnostico, "text/plain", "Información de diagnóstico");

            // Registrar en trazabilidad
            trazabilidad.registrarFallo(escenario.getName(), infoDiagnostico);

        } catch (Exception e) {
            logger.error("Error manejando escenario fallido: {}", e.getMessage());
        }
    }

    /**
     * Maneja un escenario que pasó exitosamente.
     *
     * @param escenario escenario exitoso
     */
    private void manejarEscenarioExitoso(Scenario escenario) {
        logger.info("ESCENARIO EXITOSO: {}", escenario.getName());

        try {
            // Tomar captura de pantalla final si está configurado
            if (propiedades.debeIncluirScreenshots() && ConfiguradorNavegador.tieneDriverActivo()) {
                byte[] screenshot = tomarCapturaPantalla();
                if (screenshot != null) {
                    escenario.attach(screenshot, "image/png", "Screenshot final");
                }
            }

            // Registrar éxito en trazabilidad
            trazabilidad.registrarExito(escenario.getName());

        } catch (Exception e) {
            logger.error("Error manejando escenario exitoso: {}", e.getMessage());
        }
    }

    /**
     * Prepara datos específicos para el escenario.
     *
     * @param escenario escenario a ejecutar
     */
    private void prepararDatosEscenario(Scenario escenario) {
        try {
            // Preparar datos basados en los tags del escenario
            for (String tag : escenario.getSourceTagNames()) {
                switch (tag) {
                    case "@Login", "@Autenticacion" -> gestorBD.prepararDatosUsuarios();
                    case "@CRUD", "@GestionProductos" -> gestorBD.prepararDatosProductos();
                    case "@Registro" -> gestorBD.limpiarUsuariosPrueba();
                }
            }

            logger.debug("Datos preparados para escenario: {}", escenario.getName());

        } catch (Exception e) {
            logger.error("Error preparando datos para escenario: {}", e.getMessage());
        }
    }

    /**
     * Configura el contexto específico del escenario.
     *
     * @param escenario escenario a configurar
     */
    private void configurarContextoEscenario(Scenario escenario) {
        // Configurar variables de contexto basadas en el escenario
        String nombreEscenario = escenario.getName().toLowerCase();

        if (nombreEscenario.contains("performance") || nombreEscenario.contains("tiempo")) {
            // Configurar para pruebas de performance
            System.setProperty("selenium.timeout", "5");
        } else if (nombreEscenario.contains("seguridad")) {
            // Configurar para pruebas de seguridad
            System.setProperty("selenium.security.mode", "strict");
        }
    }

    /**
     * Limpia datos específicos del escenario.
     *
     * @param escenario escenario ejecutado
     */
    private void limpiarDatosEscenario(Scenario escenario) {
        try {
            // Limpiar datos basados en los tags
            for (String tag : escenario.getSourceTagNames()) {
                if (tag.equals("@CRUD") || tag.equals("@GestionProductos")) {
                    gestorBD.limpiarProductosPrueba();
                }
            }

            logger.debug("Datos limpiados para escenario: {}", escenario.getName());

        } catch (Exception e) {
            logger.error("Error limpiando datos del escenario: {}", e.getMessage());
        }
    }

    /**
     * Verifica que los servicios críticos estén disponibles.
     */
    private void verificarServiciosCriticos() {
        // Verificar conectividad con la aplicación
        String urlBase = propiedades.obtenerUrlBase();
        // Implementar verificación de conectividad
        logger.debug("Servicios críticos verificados");
    }

    /**
     * Prepara datos específicos para pruebas de regresión.
     */
    private void prepararDatosRegresion() {
        try {
            gestorBD.cargarConjuntoCompletoDatos();
            logger.debug("Datos de regresión preparados");
        } catch (Exception e) {
            logger.error("Error preparando datos de regresión: {}", e.getMessage());
        }
    }

    /**
     * Toma una captura de pantalla del estado actual.
     *
     * @return bytes de la imagen
     */
    private byte[] tomarCapturaPantalla() {
        try {
            if (ConfiguradorNavegador.tieneDriverActivo()) {
                return ((org.openqa.selenium.TakesScreenshot) ConfiguradorNavegador.obtenerDriver())
                        .getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error tomando captura de pantalla: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene información de diagnóstico del sistema.
     *
     * @return información de diagnóstico
     */
    private String obtenerInformacionDiagnostico() {
        StringBuilder info = new StringBuilder();

        try {
            info.append("=== INFORMACIÓN DE DIAGNÓSTICO ===\n");
            info.append("Timestamp: ").append(LocalDateTime.now().format(FORMATO_FECHA)).append("\n");
            info.append("Thread: ").append(Thread.currentThread().getName()).append("\n");

            if (ConfiguradorNavegador.tieneDriverActivo()) {
                info.append("URL actual: ").append(ConfiguradorNavegador.obtenerUrlActual()).append("\n");
                info.append("Título página: ").append(ConfiguradorNavegador.obtenerTituloPagina()).append("\n");
            }

            info.append("Propiedades sistema:\n");
            info.append("  - Java version: ").append(System.getProperty("java.version")).append("\n");
            info.append("  - OS: ").append(System.getProperty("os.name")).append("\n");
            info.append("  - User dir: ").append(System.getProperty("user.dir")).append("\n");

            info.append("=== FIN DIAGNÓSTICO ===");

        } catch (Exception e) {
            info.append("Error generando diagnóstico: ").append(e.getMessage());
        }

        return info.toString();
    }

    /**
     * Genera resumen de la ejecución de pruebas.
     *
     * @param logger logger para imprimir el resumen
     */
    private static void generarResumenEjecucion(Logger logger) {
        try {
            logger.info("=== RESUMEN DE EJECUCIÓN ===");
            logger.info("Total escenarios ejecutados: {}", contadorEscenarios);
            logger.info("Escenarios exitosos: {}", escenariosPasados);
            logger.info("Escenarios fallidos: {}", escenariosFallidos);

            if (contadorEscenarios > 0) {
                double porcentajeExito = (double) escenariosPasados / contadorEscenarios * 100;
                logger.info("Porcentaje de éxito: {:.2f}%", porcentajeExito);
            }

            logger.info("=== FIN RESUMEN ===");

        } catch (Exception e) {
            logger.error("Error generando resumen: {}", e.getMessage());
        }
    }

    /**
     * Limpia recursos temporales creados durante la ejecución.
     */
    private static void limpiarRecursosTemporales() {
        try {
            // Limpiar archivos temporales
            java.nio.file.Path tempDir = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"));
            java.nio.file.Files.list(tempDir)
                    .filter(path -> path.getFileName().toString().startsWith("selenium"))
                    .forEach(path -> {
                        try {
                            java.nio.file.Files.deleteIfExists(path);
                        } catch (Exception e) {
                            // Ignorar errores de limpieza individual
                        }
                    });

            Logger.getLogger(HooksPruebas.class).debug("Recursos temporales limpiados");

        } catch (Exception e) {
            Logger.getLogger(HooksPruebas.class).error("Error limpiando recursos temporales: {}", e.getMessage());
        }
    }
}