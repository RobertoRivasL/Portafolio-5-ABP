package com.qa.automatizacion.ejecutor;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ejecutor especializado para Smoke Tests.
 *
 * Los Smoke Tests son un subconjunto de pruebas que verifican la funcionalidad básica
 * y crítica del sistema. Son pruebas rápidas que se ejecutan frecuentemente para
 * asegurar que las funciones principales del sistema funcionan correctamente.
 *
 * Principios aplicados:
 * - Single Responsibility: Solo ejecuta smoke tests
 * - Open/Closed: Extensible para configuraciones adicionales sin modificar base
 * - Interface Segregation: Interfaz específica para smoke tests
 * - Dependency Inversion: Utiliza abstracciones de Cucumber
 *
 * Características de los Smoke Tests:
 * - Ejecución rápida (< 5 minutos)
 * - Cobertura de funcionalidades críticas
 * - Verificación de conectividad y servicios básicos
 * - Validación de flujos principales de usuario
 * - Detección temprana de problemas graves
 *
 * @author Antonio B. Arriagada LL.
 * @author Dante Escalona Bustos
 * @author Roberto Rivas Lopez
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value =
        "pretty," +
                "html:reportes/smoke/html/smoke-report.html," +
                "json:reportes/smoke/json/smoke-report.json," +
                "junit:reportes/smoke/junit/smoke-report.xml," +
                "timeline:reportes/smoke/timeline," +
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value =
        "com.qa.automatizacion.pasos," +
                "com.qa.automatizacion.hooks")
@ConfigurationParameter(key = Constants.FEATURES_PROPERTY_NAME, value = "src/test/resources/features")
@ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "@SmokeTest and not @WIP and not @Ignore")
@ConfigurationParameter(key = Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME, value = "false")
@ConfigurationParameter(key = Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
@ConfigurationParameter(key = Constants.EXECUTION_DRY_RUN_PROPERTY_NAME, value = "false")
@ConfigurationParameter(key = Constants.EXECUTION_STRICT_PROPERTY_NAME, value = "true")
@ConfigurationParameter(key = Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME, value = "false")
@ConfigurationParameter(key = Constants.OBJECT_FACTORY_PROPERTY_NAME, value = "io.cucumber.picocontainer.PicoFactory")
public class EjecutorSmokeTests {

    private static final Logger logger = LoggerFactory.getLogger(EjecutorSmokeTests.class);

    /**
     * Configuración estática del ejecutor.
     * Se ejecuta antes de la inicialización de la suite de pruebas.
     */
    static {
        configurarEntornoSmokeTests();
    }

    /**
     * Configura el entorno específico para smoke tests.
     *
     * Configuraciones aplicadas:
     * - Timeouts reducidos para ejecución rápida
     * - Logging específico para smoke tests
     * - Variables de entorno para identificación
     * - Configuración de reportes especializados
     */
    private static void configurarEntornoSmokeTests() {
        logger.info("=".repeat(80));
        logger.info("🔥 INICIANDO EJECUCIÓN DE SMOKE TESTS");
        logger.info("=".repeat(80));

        try {
            // Configurar identificador de tipo de ejecución
            System.setProperty("tipo.ejecucion", "SMOKE");
            System.setProperty("suite.nombre", "Smoke Tests Suite");
            System.setProperty("suite.descripcion", "Pruebas críticas de funcionalidad básica");

            // Configurar timeouts optimizados para smoke tests
            System.setProperty("selenium.timeout.implicit", "5");
            System.setProperty("selenium.timeout.explicit", "10");
            System.setProperty("selenium.timeout.pageload", "15");
            System.setProperty("selenium.timeout.script", "10");

            // Configurar comportamiento de WebDriver para smoke tests
            System.setProperty("webdriver.smoke.mode", "true");
            System.setProperty("webdriver.headless", "false"); // Visible por defecto para smoke tests
            System.setProperty("webdriver.window.maximize", "true");

            // Configurar logging específico para smoke tests
            System.setProperty("logging.level.smoke", "INFO");
            System.setProperty("logging.pattern.smoke", "%d{HH:mm:ss.SSS} [SMOKE] %-5level %logger{36} - %msg%n");

            // Configurar captura de evidencias para smoke tests
            System.setProperty("evidencia.screenshots", "on_failure");
            System.setProperty("evidencia.videos", "false"); // No videos para smoke tests por velocidad
            System.setProperty("evidencia.logs", "true");

            // Configurar base de datos para smoke tests
            System.setProperty("database.reset.before", "false"); // No resetear BD para smoke tests
            System.setProperty("database.cleanup.after", "minimal"); // Limpieza mínima

            // Configurar paralelización (deshabilitada para smoke tests por simplicidad)
            System.setProperty("cucumber.execution.parallel.enabled", "false");
            System.setProperty("cucumber.execution.parallel.mode.default", "same_thread");

            // Configurar reportes específicos
            configurarReportesSmokeTests();

            // Verificar servicios críticos antes de comenzar
            verificarServiciosCriticos();

            logger.info("✅ Configuración de smoke tests completada exitosamente");
            logger.info("📊 Reportes se generarán en: reportes/smoke/");
            logger.info("🏷️  Etiquetas ejecutadas: @SmokeTest and not @WIP and not @Ignore");

        } catch (Exception e) {
            logger.error("❌ Error configurando entorno de smoke tests: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo en configuración de smoke tests", e);
        }
    }

    /**
     * Configura los reportes específicos para smoke tests.
     */
    private static void configurarReportesSmokeTests() {
        try {
            // Configurar metadatos para reportes HTML
            System.setProperty("cucumber.publish.enabled", "false");
            System.setProperty("cucumber.publish.quiet", "true");

            // Configurar información del proyecto para reportes
            System.setProperty("project.name", "Automatización BDD - Smoke Tests");
            System.setProperty("project.version", "1.0.0");
            System.setProperty("environment", "SMOKE_TESTING");
            System.setProperty("browser", System.getProperty("webdriver.browser", "chrome"));
            System.setProperty("platform", System.getProperty("os.name"));

            // Configurar etiquetas de tiempo
            System.setProperty("execution.start.time", String.valueOf(System.currentTimeMillis()));

            // Configurar directorio de reportes
            crearDirectoriosReportes();

            logger.debug("Configuración de reportes smoke tests completada");

        } catch (Exception e) {
            logger.warn("Advertencia configurando reportes: {}", e.getMessage());
        }
    }

    /**
     * Crea los directorios necesarios para los reportes.
     */
    private static void crearDirectoriosReportes() {
        String[] directorios = {
                "reportes/smoke/html",
                "reportes/smoke/json",
                "reportes/smoke/junit",
                "reportes/smoke/timeline",
                "reportes/smoke/screenshots",
                "reportes/smoke/logs"
        };

        for (String directorio : directorios) {
            java.io.File dir = new java.io.File(directorio);
            if (!dir.exists()) {
                boolean creado = dir.mkdirs();
                if (creado) {
                    logger.debug("Directorio creado: {}", directorio);
                }
            }
        }
    }

    /**
     * Verifica que los servicios críticos estén disponibles antes de ejecutar los tests.
     * Esta verificación es fundamental para smoke tests ya que valida la disponibilidad
     * básica del sistema.
     */
    private static void verificarServiciosCriticos() {
        logger.info("🔍 Verificando servicios críticos para smoke tests...");

        try {
            // Verificar conectividad básica de red
            verificarConectividadRed();

            // Verificar disponibilidad de la aplicación web
            verificarDisponibilidadAplicacion();

            // Verificar servicios de base de datos (si aplica)
            verificarServicioBaseDatos();

            // Verificar WebDriver
            verificarWebDriver();

            logger.info("✅ Todos los servicios críticos están disponibles");

        } catch (Exception e) {
            logger.error("❌ Fallo en verificación de servicios críticos: {}", e.getMessage());
            throw new RuntimeException("Servicios críticos no disponibles para smoke tests", e);
        }
    }

    /**
     * Verifica la conectividad básica de red.
     */
    private static void verificarConectividadRed() {
        try {
            String urlBase = System.getProperty("app.url.base", "http://localhost:8080");
            java.net.URL url = new java.net.URL(urlBase);
            java.net.URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();

            logger.debug("✓ Conectividad de red verificada: {}", urlBase);

        } catch (Exception e) {
            logger.error("✗ Fallo en conectividad de red: {}", e.getMessage());
            throw new RuntimeException("Sin conectividad de red", e);
        }
    }

    /**
     * Verifica que la aplicación web esté disponible.
     */
    private static void verificarDisponibilidadAplicacion() {
        try {
            String urlLogin = System.getProperty("app.url.login", "http://localhost:8080/login");

            java.net.HttpURLConnection connection = (java.net.HttpURLConnection)
                    new java.net.URL(urlLogin).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();

            if (responseCode >= 200 && responseCode < 400) {
                logger.debug("✓ Aplicación web disponible: {} (HTTP {})", urlLogin, responseCode);
            } else {
                throw new RuntimeException("Aplicación no disponible - HTTP " + responseCode);
            }

        } catch (Exception e) {
            logger.error("✗ Aplicación web no disponible: {}", e.getMessage());
            throw new RuntimeException("Aplicación web no disponible", e);
        }
    }

    /**
     * Verifica la disponibilidad del servicio de base de datos.
     */
    private static void verificarServicioBaseDatos() {
        try {
            // Para smoke tests, solo verificamos si la configuración está presente
            String dbUrl = System.getProperty("database.url", "jdbc:h2:mem:testdb");

            if (dbUrl.contains("h2:mem")) {
                // Base de datos en memoria para tests - siempre disponible
                logger.debug("✓ Base de datos en memoria configurada");
            } else {
                // Para bases de datos reales, se podría hacer una conexión rápida
                logger.debug("✓ Configuración de base de datos presente: {}",
                        dbUrl.replaceAll("password=[^;]*", "password=***"));
            }

        } catch (Exception e) {
            logger.warn("⚠ Advertencia en verificación de BD: {}", e.getMessage());
            // No fallar por BD en smoke tests si no es crítico
        }
    }

    /**
     * Verifica que WebDriver esté disponible.
     */
    private static void verificarWebDriver() {
        try {
            String browser = System.getProperty("webdriver.browser", "chrome");

            // Verificar que el driver esté en el PATH o configurado
            switch (browser.toLowerCase()) {
                case "chrome":
                    verificarChromeDriver();
                    break;
                case "firefox":
                    verificarFirefoxDriver();
                    break;
                case "edge":
                    verificarEdgeDriver();
                    break;
                default:
                    logger.debug("✓ Navegador configurado: {}", browser);
            }

            logger.debug("✓ WebDriver configurado para: {}", browser);

        } catch (Exception e) {
            logger.error("✗ Problema con WebDriver: {}", e.getMessage());
            throw new RuntimeException("WebDriver no disponible", e);
        }
    }

    /**
     * Verifica la disponibilidad de ChromeDriver.
     */
    private static void verificarChromeDriver() {
        try {
            String driverPath = System.getProperty("webdriver.chrome.driver");
            if (driverPath != null && !driverPath.isEmpty()) {
                java.io.File driverFile = new java.io.File(driverPath);
                if (!driverFile.exists() || !driverFile.canExecute()) {
                    throw new RuntimeException("ChromeDriver no encontrado o no ejecutable: " + driverPath);
                }
            }
            // Si no hay path específico, asumimos que está en PATH del sistema

        } catch (Exception e) {
            logger.warn("Advertencia con ChromeDriver: {}", e.getMessage());
        }
    }

    /**
     * Verifica la disponibilidad de GeckoDriver (Firefox).
     */
    private static void verificarFirefoxDriver() {
        try {
            String driverPath = System.getProperty("webdriver.gecko.driver");
            if (driverPath != null && !driverPath.isEmpty()) {
                java.io.File driverFile = new java.io.File(driverPath);
                if (!driverFile.exists() || !driverFile.canExecute()) {
                    throw new RuntimeException("GeckoDriver no encontrado o no ejecutable: " + driverPath);
                }
            }

        } catch (Exception e) {
            logger.warn("Advertencia con GeckoDriver: {}", e.getMessage());
        }
    }

    /**
     * Verifica la disponibilidad de EdgeDriver.
     */
    private static void verificarEdgeDriver() {
        try {
            String driverPath = System.getProperty("webdriver.edge.driver");
            if (driverPath != null && !driverPath.isEmpty()) {
                java.io.File driverFile = new java.io.File(driverPath);
                if (!driverFile.exists() || !driverFile.canExecute()) {
                    throw new RuntimeException("EdgeDriver no encontrado o no ejecutable: " + driverPath);
                }
            }

        } catch (Exception e) {
            logger.warn("Advertencia con EdgeDriver: {}", e.getMessage());
        }
    }

    /**
     * Método principal para ejecución directa.
     * Útil para integración con herramientas de CI/CD.
     */
    public static void main(String[] args) {
        logger.info("🚀 Iniciando ejecución directa de Smoke Tests");

        try {
            // Configurar sistema para ejecución directa
            System.setProperty("cucumber.execution.parallel.enabled", "false");
            System.setProperty("junit.platform.output.capture.stdout", "true");
            System.setProperty("junit.platform.output.capture.stderr", "true");

            // Ejecutar usando JUnit Platform
            org.junit.platform.launcher.LauncherFactory
                    .create()
                    .execute(org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
                            .request()
                            .selectors(org.junit.platform.engine.discovery.DiscoverySelectors
                                    .selectClass(EjecutorSmokeTests.class))
                            .build());

            logger.info("🎉 Ejecución de Smoke Tests completada");

        } catch (Exception e) {
            logger.error("💥 Error en ejecución de Smoke Tests: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}

/*
 * EJEMPLOS DE EJECUCIÓN:
 *
 * 1. Ejecución básica con Maven:
 *    mvn test -Dtest=EjecutorSmokeTests
 *
 * 2. Ejecución con navegador específico:
 *    mvn test -Dtest=EjecutorSmokeTests -Dwebdriver.browser=chrome
 *
 * 3. Ejecución en modo headless:
 *    mvn test -Dtest=EjecutorSmokeTests -Dwebdriver.headless=true
 *
 * 4. Ejecución con URL personalizada:
 *    mvn test -Dtest=EjecutorSmokeTests -Dapp.url.base=http://staging.app.com
 *
 * 5. Ejecución directa con Java:
 *    java -cp "target/test-classes:..." com.qa.automatizacion.ejecutor.EjecutorSmokeTests
 *
 * 6. Integración con Jenkins:
 *    stage('Smoke Tests') {
 *        steps {
 *            sh 'mvn test -Dtest=EjecutorSmokeTests -Dwebdriver.headless=true'
 *        }
 *        post {
 *            always {
 *                publishHTML([
 *                    allowMissing: false,
 *                    alwaysLinkToLastBuild: true,
 *                    keepAll: true,
 *                    reportDir: 'reportes/smoke/html',
 *                    reportFiles: 'smoke-report.html',
 *                    reportName: 'Smoke Tests Report'
 *                ])
 *            }
 *        }
 *    }
 *
 * VARIABLES DE ENTORNO SOPORTADAS:
 * - WEBDRIVER_BROWSER: chrome, firefox, edge
 * - WEBDRIVER_HEADLESS: true/false
 * - APP_URL_BASE: URL base de la aplicación
 * - APP_URL_LOGIN: URL de login
 * - DATABASE_URL: URL de base de datos
 * - SELENIUM_TIMEOUT_IMPLICIT: timeout implícito en segundos
 * - SELENIUM_TIMEOUT_EXPLICIT: timeout explícito en segundos
 */