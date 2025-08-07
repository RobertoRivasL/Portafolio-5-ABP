package com.qa.automatizacion.hooks;

import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import com.qa.automatizacion.utilidades.HelperTrazabilidad;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Hook para manejo de capturas de pantalla durante la ejecución de pruebas.
 * Implementa capturas automáticas en casos de fallo y capturas manuales.
 *
 * Principios aplicados:
 * - Single Responsibility: Se enfoca únicamente en la gestión de capturas
 * - Observer Pattern: Reacciona a eventos de Cucumber
 * - Strategy Pattern: Diferentes estrategias de captura según el contexto
 */
public class HooksScreenshots {

    private static final Logger logger = LoggerFactory.getLogger(HooksScreenshots.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String DIRECTORIO_SCREENSHOTS = "reportes/screenshots";

    private final PropiedadesAplicacion propiedades;
    private final HelperTrazabilidad trazabilidad;
    private final Map<String, String> directoriosEscenarios;

    // Variables para tracking de escenarios
    private String nombreEscenarioActual;
    private LocalDateTime inicioEscenario;
    private int contadorPasos;

    public HooksScreenshots() {
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();
        this.trazabilidad = new HelperTrazabilidad();
        this.directoriosEscenarios = new HashMap<>();
        this.contadorPasos = 0;

        inicializarDirectorios();
        logger.debug("HooksScreenshots inicializado");
    }

    // ==================== HOOKS DE ESCENARIO ====================

    @Before
    public void configurarScreenshotCompleto(Scenario escenario) {
        try {
            this.nombreEscenarioActual = ConfiguradorNavegador.limpiarNombreArchivo(escenario.getName());
            this.inicioEscenario = LocalDateTime.now();
            this.contadorPasos = 0;

            // Crear directorio específico para este escenario
            String directorioEscenario = crearDirectorioEscenario(nombreEscenarioActual);
            directoriosEscenarios.put(nombreEscenarioActual, directorioEscenario);

            // Captura inicial si está configurado
            if (debeCapturarEnInicio()) {
                capturarPantalla(escenario, "inicio_escenario");
            }

            trazabilidad.registrarEvento("SCENARIO_START",
                    "Inicio de escenario: " + escenario.getName(),
                    obtenerInfoEscenario(escenario));

            logger.info("Screenshot setup completado para escenario: {}", escenario.getName());

        } catch (Exception e) {
            logger.error("Error configurando screenshots para escenario: {}", e.getMessage());
            // No fallar la prueba por problemas de screenshots
        }
    }

    @After
    public void capturarPostEscenario(Scenario escenario) {
        try {
            // Capturar al final del escenario según su estado
            if (escenario.isFailed()) {
                capturarPantallaError(escenario);
                trazabilidad.registrarEvento("SCENARIO_FAILED",
                        "Escenario fallido: " + escenario.getName(),
                        "Tags: " + escenario.getSourceTagNames());
            } else {
                if (debeCapturarEnExito()) {
                    capturarPantalla(escenario, "fin_escenario_exitoso");
                }
                trazabilidad.registrarEvento("SCENARIO_PASSED",
                        "Escenario exitoso: " + escenario.getName());
            }

            // Limpiar datos del escenario
            directoriosEscenarios.remove(nombreEscenarioActual);

            logger.info("Post-procesamiento de screenshots completado para: {}", escenario.getName());

        } catch (Exception e) {
            logger.error("Error en post-procesamiento de screenshots: {}", e.getMessage());
        }
    }

    @AfterStep
    public void capturaPostPaso(Scenario escenario) {
        try {
            contadorPasos++;

            // Capturar después de cada paso si está configurado
            if (debeCapturarCadaPaso()) {
                String nombreCaptura = String.format("paso_%03d_%s",
                        contadorPasos,
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH-mm-ss")));
                capturarPantalla(escenario, nombreCaptura);
            }

        } catch (Exception e) {
            logger.debug("Error capturando paso {}: {}", contadorPasos, e.getMessage());
            // No fallar por problemas de captura de pasos
        }
    }

    // ==================== MÉTODOS DE CAPTURA ====================

    /**
     * Captura pantalla para escenarios fallidos con información detallada.
     */
    public void capturarPantallaError(Scenario escenario) {
        try {
            String timestamp = LocalDateTime.now().format(FORMATO_FECHA);
            String nombreArchivo = String.format("ERROR_%s_%s", nombreEscenarioActual, timestamp);

            byte[] screenshot = obtenerScreenshot();
            if (screenshot != null) {
                // Adjuntar al reporte de Cucumber
                escenario.attach(screenshot, "image/png", nombreArchivo);

                // Guardar en archivo también
                guardarScreenshotEnArchivo(screenshot, nombreArchivo);

                // Registrar información adicional del error
                registrarInformacionError(escenario, nombreArchivo);

                logger.info("Screenshot de error capturado: {}", nombreArchivo);
            }

        } catch (Exception e) {
            logger.error("Error capturando screenshot de fallo: {}", e.getMessage());
        }
    }

    /**
     * Captura pantalla con nombre personalizado.
     */
    public void capturarPantalla(Scenario escenario, String sufijo) {
        try {
            String timestamp = LocalDateTime.now().format(FORMATO_FECHA);
            String nombreArchivo = String.format("%s_%s_%s",
                    nombreEscenarioActual, sufijo, timestamp);

            byte[] screenshot = obtenerScreenshot();
            if (screenshot != null) {
                // Adjuntar al reporte si es necesario
                if (debeAdjuntarAReporte(sufijo)) {
                    escenario.attach(screenshot, "image/png", nombreArchivo);
                }

                // Guardar en archivo
                guardarScreenshotEnArchivo(screenshot, nombreArchivo);

                logger.debug("Screenshot capturado: {}", nombreArchivo);
            }

        } catch (Exception e) {
            logger.debug("Error capturando screenshot '{}': {}", sufijo, e.getMessage());
        }
    }

    /**
     * Captura manual de pantalla (método público para usar en steps).
     */
    public void capturarScreenshotManual(String nombre) {
        try {
            if (!tieneDriverDisponible()) {
                logger.warn("No hay driver disponible para captura manual");
                return;
            }

            String nombreLimpio = ConfiguradorNavegador.limpiarNombreArchivo(nombre);
            String timestamp = LocalDateTime.now().format(FORMATO_FECHA);
            String nombreArchivo = String.format("MANUAL_%s_%s", nombreLimpio, timestamp);

            byte[] screenshot = obtenerScreenshot();
            if (screenshot != null) {
                guardarScreenshotEnArchivo(screenshot, nombreArchivo);

                trazabilidad.registrarEvento("MANUAL_SCREENSHOT",
                        "Captura manual: " + nombre, nombreArchivo);

                logger.info("Screenshot manual capturado: {}", nombreArchivo);
            }

        } catch (Exception e) {
            logger.error("Error en captura manual '{}': {}", nombre, e.getMessage());
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Obtiene screenshot del navegador actual.
     */
    private byte[] obtenerScreenshot() {
        try {
            if (!tieneDriverDisponible()) {
                logger.debug("Driver no disponible para screenshot");
                return null;
            }

            return ConfiguradorNavegador.tomarCapturaPantalla();

        } catch (Exception e) {
            logger.debug("Error obteniendo screenshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si hay un driver disponible para capturas.
     */
    private boolean tieneDriverDisponible() {
        try {
            WebDriver driver = ConfiguradorNavegador.obtenerDriver();
            return driver != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Guarda screenshot en archivo del sistema.
     */
    private void guardarScreenshotEnArchivo(byte[] screenshot, String nombreArchivo) {
        try {
            String directorio = directoriosEscenarios.getOrDefault(
                    nombreEscenarioActual, DIRECTORIO_SCREENSHOTS);

            Path rutaArchivo = Paths.get(directorio, nombreArchivo + ".png");
            Files.write(rutaArchivo, screenshot);

            logger.debug("Screenshot guardado en: {}", rutaArchivo);

        } catch (IOException e) {
            logger.error("Error guardando screenshot en archivo: {}", e.getMessage());
        }
    }

    /**
     * Crea directorio específico para un escenario.
     */
    private String crearDirectorioEscenario(String nombreEscenario) {
        try {
            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Path directorioEscenario = Paths.get(DIRECTORIO_SCREENSHOTS, fecha, nombreEscenario);

            Files.createDirectories(directorioEscenario);
            return directorioEscenario.toString();

        } catch (IOException e) {
            logger.warn("Error creando directorio para escenario: {}", e.getMessage());
            return DIRECTORIO_SCREENSHOTS;
        }
    }

    /**
     * Inicializa los directorios base de screenshots.
     */
    private void inicializarDirectorios() {
        try {
            Path directorio = Paths.get(DIRECTORIO_SCREENSHOTS);
            Files.createDirectories(directorio);
            logger.debug("Directorio de screenshots inicializado: {}", directorio);
        } catch (IOException e) {
            logger.error("Error creando directorio base de screenshots: {}", e.getMessage());
        }
    }

    /**
     * Registra información adicional sobre errores.
     */
    private void registrarInformacionError(Scenario escenario, String nombreArchivo) {
        try {
            Map<String, Object> infoError = ConfiguradorNavegador.obtenerInformacionDiagnostico();
            infoError.put("timestamp", LocalDateTime.now());
            infoError.put("nombreEscenario", escenario.getName());
            infoError.put("tags", escenario.getSourceTagNames());
            infoError.put("archivoScreenshot", nombreArchivo);

            trazabilidad.registrarEvento("ERROR_DIAGNOSTIC",
                    "Información de diagnóstico de error", infoError.toString());

        } catch (Exception e) {
            logger.debug("Error registrando información de diagnóstico: {}", e.getMessage());
        }
    }

    /**
     * Obtiene información del escenario para logging.
     */
    private String obtenerInfoEscenario(Scenario escenario) {
        return String.format("Escenario: %s, Tags: %s, Línea: %d",
                escenario.getName(),
                escenario.getSourceTagNames(),
                escenario.getLine());
    }

    // ==================== MÉTODOS DE CONFIGURACIÓN ====================

    private boolean debeCapturarEnInicio() {
        return propiedades.obtenerPropiedad("capturas.en.inicio", "false").equals("true");
    }

    private boolean debeCapturarEnExito() {
        return propiedades.obtenerPropiedad("capturas.en.exito", "false").equals("true");
    }

    private boolean debeCapturarCadaPaso() {
        return propiedades.obtenerPropiedad("capturas.cada.paso", "false").equals("true");
    }

    private boolean debeAdjuntarAReporte(String sufijo) {
        // Solo adjuntar al reporte capturas importantes para no saturarlo
        return sufijo.contains("error") || sufijo.contains("inicio") || sufijo.contains("fin");
    }
}