package com.qa.automatizacion.hooks;

import com.qa.automatizacion.contexto.ContextoPruebas;
import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import com.qa.automatizacion.utilidades.HelperTrazabilidad;
import io.cucumber.java.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Hooks de Cucumber para configuración y limpieza de pruebas.
 * Gestiona el ciclo de vida de los recursos de prueba.
 *
 * Principios aplicados:
 * - Template Method: Define el esqueleto de configuración/limpieza
 * - Single Responsibility: Se enfoca únicamente en gestión de hooks
 * - Dependency Injection: Utiliza servicios externos de forma controlada
 *
 * @author Equipo QA Automatización
 * @version 1.0
 */
public class HooksPruebas {

    private static final Logger logger = LoggerFactory.getLogger(HooksPruebas.class);

    private ContextoPruebas contexto;
    private HelperTrazabilidad trazabilidad;

    /**
     * Constructor que inicializa las dependencias
     */
    public HooksPruebas() {
        this.contexto = ContextoPruebas.obtenerInstancia();
        this.trazabilidad = new HelperTrazabilidad();
    }

    /**
     * Hook que se ejecuta antes de toda la suite de pruebas
     */
    @BeforeAll
    public static void configuracionGlobal() {
        logger.info("=== INICIANDO SUITE DE PRUEBAS BDD ===");
        logger.info("Tiempo de inicio: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Configuraciones globales
        System.setProperty("webdriver.http.factory", "jdk-http-client");

        // Crear directorios de reportes si no existen
        crearDirectoriosReportes();

        logger.info("Configuración global completada");
    }

    /**
     * Hook que se ejecuta antes de cada escenario
     *
     * @param escenario Información del escenario actual
     */
    @Before
    public void configuracionEscenario(Scenario escenario) {
        String nombreEscenario = escenario.getName();
        logger.info("=== INICIANDO ESCENARIO: {} ===", nombreEscenario);

        // Limpiar contexto para nuevo escenario
        contexto.limpiarContexto();

        // Almacenar información del escenario
        contexto.almacenarDato("nombreEscenario", nombreEscenario);
        contexto.almacenarDato("tagsEscenario", escenario.getSourceTagNames());
        contexto.almacenarDato("estadoEscenario", "EJECUTANDO");
        contexto.almacenarDato("inicioEscenario", LocalDateTime.now());

        // Registrar trazabilidad
        trazabilidad.registrarInicioEscenario(escenario);

        logger.info("Escenario '{}' configurado correctamente", nombreEscenario);
    }

    /**
     * Hook específico para escenarios con tag @RequiereNavegador
     *
     * @param escenario Información del escenario actual
     */
    @Before("@RequiereNavegador or @SmokeTest or @Login or @CRUD or @Regression")
    public void configuracionNavegador(Scenario escenario) {
        logger.info("Inicializando navegador para escenario: {}", escenario.getName());

        try {
            // Asegurar que hay un driver disponible
            WebDriver driver = ConfiguradorNavegador.obtenerDriver();
            contexto.almacenarDato("driverDisponible", true);

            logger.info("Navegador inicializado: {}",
                    ConfiguradorNavegador.obtenerConfiguracionActual());

        } catch (Exception e) {
            logger.error("Error inicializando navegador: {}", e.getMessage());
            contexto.almacenarDato("driverDisponible", false);
            throw new RuntimeException("No se pudo inicializar el navegador", e);
        }
    }

    /**
     * Hook específico para escenarios de base de datos
     *
     * @param escenario Información del escenario actual
     */
    @Before("@RequiereBaseDatos")
    public void configuracionBaseDatos(Scenario escenario) {
        logger.info("Configurando base de datos para escenario: {}", escenario.getName());

        try {
            // Aquí se podría configurar conexión a BD de pruebas
            contexto.almacenarDato("baseDatosDisponible", true);
            logger.info("Base de datos configurada correctamente");

        } catch (Exception e) {
            logger.error("Error configurando base de datos: {}", e.getMessage());
            contexto.almacenarDato("baseDatosDisponible", false);
        }
    }

    /**
     * Hook que se ejecuta después de cada escenario
     *
     * @param escenario Información del escenario actual
     */
    @After
    public void limpiezaEscenario(Scenario escenario) {
        String nombreEscenario = escenario.getName();
        String estado = escenario.getStatus().toString();

        logger.info("=== FINALIZANDO ESCENARIO: {} - ESTADO: {} ===", nombreEscenario, estado);

        /**
         * Hook que se ejecuta después de cada escenario
         *
         * @param escenario Información del escenario actual
         */
        @After
        public void limpiezaEscenario(Scenario escenario) {
            String nombreEscenario = escenario.getName();
            String estado = escenario.getStatus().toString();

            logger.info("=== FINALIZANDO ESCENARIO: {} - ESTADO: {} ===", nombreEscenario, estado);

            // Actualizar información del escenario
            contexto.almacenarDato("estadoEscenario", estado);
            contexto.almacenarDato("finEscenario", LocalDateTime.now());

            // Tomar captura de pantalla si el escenario falló
            if (escenario.isFailed()) {
                tomarCapturaEscenarioFallido(escenario);
            }

            // Registrar resultado en trazabilidad
            trazabilidad.registrarResultadoEscenario(escenario);

            // Generar reporte de contexto si es necesario
            if (logger.isDebugEnabled()) {
                logger.debug("Estado final del contexto:\n{}", contexto.generarResumenContexto());
            }

            logger.info("Escenario '{}' finalizado con estado: {}", nombreEscenario, estado);
        }

        /**
         * Hook específico para limpiar navegador después de escenarios que lo usan
         *
         * @param escenario Información del escenario actual
         */
        @After("@RequiereNavegador or @SmokeTest or @Login or @CRUD or @Regression")
        public void limpiezaNavegador(Scenario escenario) {
            logger.info("Limpiando navegador después del escenario: {}", escenario.getName());

            try {
                // No cerrar el driver después de cada escenario para mejorar performance
                // Solo limpiar cookies y datos de sesión
                if (ConfiguradorNavegador.hayDriverActivo()) {
                    WebDriver driver = ConfiguradorNavegador.obtenerDriver();
                    driver.manage().deleteAllCookies();

                    // Navegar a página en blanco para limpiar estado
                    driver.get("about:blank");
                }

                logger.debug("Limpieza de navegador completada");

            } catch (Exception e) {
                logger.warn("Error durante limpieza de navegador: {}", e.getMessage());
                // Si hay problemas, reiniciar el driver
                ConfiguradorNavegador.reiniciarDriver();
            }
        }

        /**
         * Hook específico para limpiar base de datos después de escenarios
         *
         * @param escenario Información del escenario actual
         */
        @After("@RequiereBaseDatos")
        public void limpiezaBaseDatos(Scenario escenario) {
            logger.info("Limpiando datos de prueba de base de datos: {}", escenario.getName());

            try {
                // Aquí se podría limpiar datos de prueba específicos
                // Por ahora solo log de la acción
                logger.debug("Limpieza de base de datos completada");

            } catch (Exception e) {
                logger.warn("Error durante limpieza de base de datos: {}", e.getMessage());
            }
        }

        /**
         * Hook que se ejecuta después de toda la suite de pruebas
         */
        @AfterAll
        public static void limpiezaGlobal() {
            logger.info("=== FINALIZANDO SUITE DE PRUEBAS BDD ===");
            logger.info("Tiempo de finalización: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            try {
                // Cerrar todos los drivers
                ConfiguradorNavegador.cerrarTodosLosDrivers();

                // Generar reporte final de trazabilidad
                HelperTrazabilidad trazabilidadFinal = new HelperTrazabilidad();
                trazabilidadFinal.generarReporteFinal();

                logger.info("Limpieza global completada exitosamente");

            } catch (Exception e) {
                logger.error("Error durante limpieza global: {}", e.getMessage());
            }

            logger.info("=== SUITE DE PRUEBAS FINALIZADA ===");
        }

        // Métodos auxiliares privados

        /**
         * Toma una captura de pantalla cuando un escenario falla
         *
         * @param escenario Escenario que falló
         */
        private void tomarCapturaEscenarioFallido(Scenario escenario) {
            try {
                if (ConfiguradorNavegador.hayDriverActivo()) {
                    WebDriver driver = ConfiguradorNavegador.obtenerDriver();

                    if (driver instanceof TakesScreenshot) {
                        byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

                        // Adjuntar captura al reporte de Cucumber
                        String nombreCaptura = "Error_" + escenario.getName().replaceAll("[^a-zA-Z0-9]", "_");
                        escenario.attach(screenshot, "image/png", nombreCaptura);

                        // Almacenar en contexto para uso posterior
                        contexto.almacenarDato("capturaError", screenshot);

                        logger.info("Captura de error tomada para escenario: {}", escenario.getName());
                    }
                }
            } catch (Exception e) {
                logger.warn("Error tomando captura de escenario fallido: {}", e.getMessage());
            }
        }

        /**
         * Crea los directorios necesarios para los reportes
         */
        private static void crearDirectoriosReportes() {
            try {
                java.nio.file.Path directorioReportes = java.nio.file.Paths.get("reportes");
                java.nio.file.Files.createDirectories(directorioReportes);

                // Crear subdirectorios
                java.nio.file.Files.createDirectories(directorioReportes.resolve("html"));
                java.nio.file.Files.createDirectories(directorioReportes.resolve("json"));
                java.nio.file.Files.createDirectories(directorioReportes.resolve("junit"));
                java.nio.file.Files.createDirectories(directorioReportes.resolve("screenshots"));
                java.nio.file.Files.createDirectories(directorioReportes.resolve("trazabilidad"));
                java.nio.file.Files.createDirectories(directorioReportes.resolve("timeline"));

                logger.debug("Directorios de reportes creados exitosamente");

            } catch (Exception e) {
                logger.warn("Error creando directorios de reportes: {}", e.getMessage());
            }
        }

        /**
         * Verifica el estado de los recursos del sistema
         *
         * @return true si todos los recursos están disponibles
         */
        private boolean verificarEstadoRecursos() {
            boolean navegadorOk = Boolean.TRUE.equals(contexto.obtenerDato("driverDisponible"));
            boolean baseDatosOk = Boolean.TRUE.equals(contexto.obtenerDato("baseDatosDisponible"));

            logger.debug("Estado de recursos - Navegador: {}, Base de datos: {}", navegadorOk, baseDatosOk);

            return navegadorOk; // Base de datos es opcional en algunos escenarios
        }

        /**
         * Registra métricas de rendimiento del escenario
         *
         * @param escenario Escenario a analizar
         */
        private void registrarMetricasRendimiento(Scenario escenario) {
            try {
                LocalDateTime inicio = (LocalDateTime) contexto.obtenerDato("inicioEscenario");
                LocalDateTime fin = (LocalDateTime) contexto.obtenerDato("finEscenario");

                if (inicio != null && fin != null) {
                    long duracionMs = java.time.Duration.between(inicio, fin).toMillis();
                    contexto.almacenarDato("duracionEscenarioMs", duracionMs);

                    logger.info("Duración del escenario '{}': {} ms", escenario.getName(), duracionMs);

                    // Alertar si el escenario tardó demasiado
                    if (duracionMs > 60000) { // Más de 1 minuto
                        logger.warn("Escenario '{}' tardó más de 1 minuto en ejecutarse: {} ms",
                                escenario.getName(), duracionMs);
                    }
                }
            } catch (Exception e) {
                logger.debug("Error registrando métricas de rendimiento: {}", e.getMessage());
            }
        }

        /**
         * Limpia archivos temporales generados durante las pruebas
         */
        private static void limpiarArchivosTemporales() {
            try {
                // Limpiar archivos temporales del sistema
                String tempDir = System.getProperty("java.io.tmpdir");
                java.nio.file.Path tempPath = java.nio.file.Paths.get(tempDir);

                // Buscar y eliminar archivos relacionados con las pruebas
                java.nio.file.Files.walk(tempPath)
                        .filter(path -> path.getFileName().toString().startsWith("webdriver"))
                        .forEach(path -> {
                            try {
                                java.nio.file.Files.deleteIfExists(path);
                            } catch (Exception e) {
                                // Ignorar errores de limpieza
                            }
                        });

                logger.debug("Archivos temporales limpiados");

            } catch (Exception e) {
                logger.debug("Error limpiando archivos temporales: {}", e.getMessage());
            }
        }
    }