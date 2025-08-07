package com.qa.automatizacion.hooks;

import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import com.qa.automatizacion.utilidades.HelperTrazabilidad;
import com.qa.automatizacion.utilidades.GestorBaseDatos;
import io.cucumber.java.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Hooks de Cucumber para configuración y limpieza de pruebas.
 */
public class HooksPruebas {

    private static final Logger logger = LoggerFactory.getLogger(HooksPruebas.class);

    // Variables de estado
    private static int contadorEscenarios = 0;
    private static int escenariosPasados = 0;
    private static int escenariosFallidos = 0;

    private LocalDateTime inicioEscenario;
    private HelperTrazabilidad trazabilidad;
    private GestorBaseDatos gestorBD;

    public HooksPruebas() {
        this.trazabilidad = new HelperTrazabilidad();
        this.gestorBD = new GestorBaseDatos();
    }

    @BeforeAll
    public static void configuracionGlobal() {
        logger.info("=== INICIANDO SUITE DE PRUEBAS BDD ===");
        logger.info("Tiempo de inicio: {}",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Crear directorios de reportes
        crearDirectoriosReportes();

        logger.info("Configuración global completada");
    }

    @Before
    public void configuracionEscenario(Scenario escenario) {
        contadorEscenarios++;
        this.inicioEscenario = LocalDateTime.now();

        logger.info("-".repeat(60));
        logger.info("INICIANDO ESCENARIO #{}: {}", contadorEscenarios, escenario.getName());
        logger.info("Tags: {}", String.join(", ", escenario.getSourceTagNames()));
        logger.info("-".repeat(60));

        try {
            // Registrar inicio del escenario en trazabilidad
            trazabilidad.registrarInicioEscenario(escenario);

            // Preparar datos del escenario
            prepararDatosEscenario(escenario);

            logger.info("Configuración del escenario completada");

        } catch (Exception e) {
            logger.error("Error en configuración del escenario '{}': {}",
                    escenario.getName(), e.getMessage());
            throw new RuntimeException("Fallo en configuración del escenario", e);
        }
    }

    @After
    public void limpiezaEscenario(Scenario escenario) {
        LocalDateTime finEscenario = LocalDateTime.now();
        long duracionSegundos = java.time.Duration.between(inicioEscenario, finEscenario).getSeconds();

        try {
            if (escenario.isFailed()) {
                manejarEscenarioFallido(escenario);
                escenariosFallidos++;
            } else {
                manejarEscenarioExitoso(escenario);
                escenariosPasados++;
            }

            // Limpiar datos del escenario
            limpiarDatosEscenario(escenario);

            // Cerrar WebDriver si existe
            ConfiguradorNavegador.cerrarDriver();

            // Registrar finalización en trazabilidad
            trazabilidad.registrarResultadoEscenario(escenario);

            logger.info("ESCENARIO FINALIZADO: {} - Estado: {} - Duración: {}s",
                    escenario.getName(), escenario.getStatus(), duracionSegundos);
            logger.info("-".repeat(60));

        } catch (Exception e) {
            logger.error("Error en limpieza del escenario '{}': {}",
                    escenario.getName(), e.getMessage());
        }
    }

    @Before("@SmokeTest")
    public void configuracionSmokeTest(Scenario escenario) {
        logger.info("Configurando escenario de Smoke Test: {}", escenario.getName());

        try {
            verificarServiciosCriticos();
            System.setProperty("selenium.timeout", "10");
            logger.info("Configuración de Smoke Test completada");
        } catch (Exception e) {
            logger.error("Error configurando Smoke Test: {}", e.getMessage());
            throw new RuntimeException("Fallo en configuración de Smoke Test", e);
        }
    }

    @Before("@Regression")
    public void configuracionRegression(Scenario escenario) {
        logger.info("Configurando escenario de Regresión: {}", escenario.getName());

        try {
            prepararDatosRegresion();
            System.setProperty("selenium.timeout", "20");
            logger.info("Configuración de Regresión completada");
        } catch (Exception e) {
            logger.error("Error configurando Regresión: {}", e.getMessage());
            throw new RuntimeException("Fallo en configuración de Regresión", e);
        }
    }

    @After("@CRUD")
    public void limpiezaCrud(Scenario escenario) {
        logger.info("Limpieza específica para escenario CRUD: {}", escenario.getName());

        try {
            gestorBD.limpiarDatosPrueba();
            gestorBD.restaurarEstadoInicial();
            logger.info("Limpieza CRUD completada");
        } catch (Exception e) {
            logger.error("Error en limpieza CRUD: {}", e.getMessage());
        }
    }

    @AfterAll
    public static void limpiezaGlobal() {
        logger.info("=== FINALIZANDO SUITE DE PRUEBAS BDD ===");

        try {
            ConfiguradorNavegador.cerrarTodosLosDrivers();
            generarResumenEjecucion();
            limpiarRecursosTemporales();

            logger.info("Limpieza global completada exitosamente");
        } catch (Exception e) {
            logger.error("Error durante limpieza global: {}", e.getMessage());
        }

        logger.info("=== SUITE DE PRUEBAS FINALIZADA ===");
    }

    // Métodos auxiliares privados

    private void prepararDatosEscenario(Scenario escenario) {
        // Implementar preparación de datos específicos
        logger.debug("Preparando datos para escenario: {}", escenario.getName());
    }

    private void limpiarDatosEscenario(Scenario escenario) {
        // Implementar limpieza de datos específicos
        logger.debug("Limpiando datos de escenario: {}", escenario.getName());
    }

    private void manejarEscenarioFallido(Scenario escenario) {
        logger.error("Escenario fallido: {}", escenario.getName());

        // Tomar captura de pantalla si hay driver activo
        if (ConfiguradorNavegador.hayDriverActivo()) {
            try {
                byte[] screenshot = ((org.openqa.selenium.TakesScreenshot)
                        ConfiguradorNavegador.obtenerDriver()).getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
                escenario.attach(screenshot, "image/png", "Error_Screenshot");
            } catch (Exception e) {
                logger.warn("No se pudo tomar captura de pantalla: {}", e.getMessage());
            }
        }
    }

    private void manejarEscenarioExitoso(Scenario escenario) {
        logger.info("Escenario exitoso: {}", escenario.getName());
    }

    private void verificarServiciosCriticos() {
        // Implementar verificación de servicios críticos
        logger.debug("Verificando servicios críticos");
    }

    private void prepararDatosRegresion() {
        // Implementar preparación de datos para regresión
        logger.debug("Preparando datos de regresión");
    }

    private static void crearDirectoriosReportes() {
        try {
            java.nio.file.Path directorioReportes = java.nio.file.Paths.get("reportes");
            java.nio.file.Files.createDirectories(directorioReportes);

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

    private static void generarResumenEjecucion() {
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

    private static void limpiarRecursosTemporales() {
        try {
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

            logger.debug("Recursos temporales limpiados");
        } catch (Exception e) {
            logger.error("Error limpiando recursos temporales: {}", e.getMessage());
        }
    }
}