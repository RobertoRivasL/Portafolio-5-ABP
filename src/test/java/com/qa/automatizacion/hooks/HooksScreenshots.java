package com.qa.automatizacion.hooks;

import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import com.qa.automatizacion.configuracion.PropiedadesPrueba;
import com.qa.automatizacion.utilidades.HelperTrazabilidad;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Hook especializado para gestión de capturas de pantalla en pruebas BDD.
 *
 * Responsabilidades:
 * - Captura automática de screenshots en fallos
 * - Captura opcional en pasos exitosos
 * - Organización de capturas por escenario y fecha
 * - Adjuntar capturas a reportes de Cucumber
 * - Gestión de almacenamiento y limpieza de archivos
 *
 * Principios aplicados:
 * - Single Responsibility: Solo maneja capturas de pantalla
 * - Open/Closed: Extensible para nuevos tipos de captura
 * - Interface Segregation: Métodos específicos para cada tipo de captura
 * - Dependency Inversion: No depende de implementaciones específicas de WebDriver
 *
 * @author Antonio B. Arriagada LL.
 * @author Dante Escalona Bustos
 * @author Roberto Rivas Lopez
 */
public class HooksScreenshots {

    private static final Logger logger = LoggerFactory.getLogger(HooksScreenshots.class);

    private static PropiedadesPrueba propiedades;
    private static HelperTrazabilidad trazabilidad;

    // Configuración de capturas
    private static final String DIRECTORIO_BASE_SCREENSHOTS = "reportes/screenshots";
    private static final DateTimeFormatter FORMATO_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    // Cache de información de escenarios
    private static final ThreadLocal<String> escenarioActual = new ThreadLocal<>();
    private static final ThreadLocal<Integer> contadorPasos = new ThreadLocal<>();
    private static final Map<String, String> directoriosEscenarios = new HashMap<>();

    /**
     * Inicialización estática del hook.
     */
    static {
        try {
            propiedades = PropiedadesPrueba.getInstance();
            trazabilidad = HelperTrazabilidad.getInstance();

            // Crear directorio base para screenshots
            crearDirectorioBase();

            logger.info("HooksScreenshots inicializado correctamente");

        } catch (Exception e) {
            logger.error("Error inicializando HooksScreenshots: {}", e.getMessage());
            throw new RuntimeException("Fallo inicializando hooks de screenshots", e);
        }
    }

    /**
     * Se ejecuta antes de cada escenario para preparar el contexto de capturas.
     */
    @Before
    public void prepararContextoScreenshots(Scenario escenario) {
        try {
            String nombreEscenario = limpiarNombreArchivo(escenario.getName());
            escenarioActual.set(nombreEscenario);
            contadorPasos.set(0);

            // Crear directorio específico para el escenario
            String directorioEscenario = crearDirectorioEscenario(nombreEscenario);
            directoriosEscenarios.put(nombreEscenario, directorioEscenario);

            logger.debug("Contexto de screenshots preparado para: {}", nombreEscenario);

        } catch (Exception e) {
            logger.error("Error preparando contexto screenshots: {}", e.getMessage());
        }
    }

    /**
     * Se ejecuta después de cada paso - captura condicional.
     */
    @AfterStep
    public void capturaPostPaso(Scenario escenario) {
        try {
            // Incrementar contador de pasos
            int numeroPaso = contadorPasos.get() + 1;
            contadorPasos.set(numeroPaso);

            // Capturar según configuración
            String modoCaptura = propiedades.obtenerModoCapturaPantalla();

            boolean debeCapturar = switch (modoCaptura.toLowerCase()) {
                case "always", "siempre" -> true;
                case "never", "nunca" -> false;
                case "on_failure", "solo_fallos" -> escenario.isFailed();
                case "on_step_failure" -> escenario.isFailed();
                case "debug" -> esModeDEBUG();
                default -> escenario.isFailed(); // Por defecto solo en fallos
            };

            if (debeCapturar && ConfiguradorNavegador.tieneDriverActivo()) {
                String tipoCaptura = escenario.isFailed() ? "FALLO" : "PASO";
                capturarPantallaPaso(escenario, numeroPaso, tipoCaptura);
            }

        } catch (Exception e) {
            logger.warn("Error en captura post-paso: {}", e.getMessage());
        }
    }

    /**
     * Se ejecuta después de cada escenario - captura final.
     */
    @After
    public void capturaPostEscenario(Scenario escenario) {
        try {
            if (ConfiguradorNavegador.tieneDriverActivo()) {
                String tipoCaptura = escenario.isFailed() ? "FALLO-FINAL" : "EXITO-FINAL";

                // Siempre capturar al final si hay un WebDriver activo
                byte[] screenshot = capturarPantallaEscenario(escenario, tipoCaptura);

                if (screenshot != null) {
                    // Adjuntar al reporte de Cucumber
                    escenario.attach(screenshot, "image/png", "Screenshot " + tipoCaptura);

                    trazabilidad.registrarEvento("Screenshot",
                            String.format("Captura %s para escenario: %s", tipoCaptura, escenario.getName()));
                }
            }

        } catch (Exception e) {
            logger.warn("Error en captura post-escenario: {}", e.getMessage());
        } finally {
            // Limpiar contexto del hilo
            escenarioActual.remove();
            contadorPasos.remove();
        }
    }

    /**
     * Hook específico para capturas en escenarios críticos.
     */
    @After("@CapturaObligatoria")
    public void capturaObligatoriaEscenario(Scenario escenario) {
        logger.info("Ejecutando captura obligatoria para: {}", escenario.getName());

        try {
            if (ConfiguradorNavegador.tieneDriverActivo()) {
                byte[] screenshot = capturarPantallaEscenario(escenario, "OBLIGATORIA");

                if (screenshot != null) {
                    escenario.attach(screenshot, "image/png", "Screenshot Obligatoria");
                    logger.info("Captura obligatoria completada para: {}", escenario.getName());
                }
            }

        } catch (Exception e) {
            logger.error("Error en captura obligatoria: {}", e.getMessage());
        }
    }

    /**
     * Hook específico para capturas detalladas en debugging.
     */
    @AfterStep("@Debug")
    public void capturaDebugPaso(Scenario escenario) {
        try {
            if (ConfiguradorNavegador.tieneDriverActivo()) {
                int numeroPaso = contadorPasos.get();
                capturarPantallaPaso(escenario, numeroPaso, "DEBUG");

                // En modo debug, también capturar información adicional
                capturarInformacionDebug(escenario, numeroPaso);
            }

        } catch (Exception e) {
            logger.warn("Error en captura debug: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE CAPTURA ====================

    /**
     * Captura screenshot para un paso específico.
     */
    private void capturarPantallaPaso(Scenario escenario, int numeroPaso, String tipoCaptura) {
        try {
            WebDriver driver = ConfiguradorNavegador.obtenerDriverActual();
            if (driver == null) return;

            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            if (screenshot != null && screenshot.length > 0) {
                // Generar nombre del archivo
                String nombreArchivo = String.format("paso-%02d-%s-%s.png",
                        numeroPaso, tipoCaptura.toLowerCase(),
                        LocalDateTime.now().format(FORMATO_TIMESTAMP));

                // Guardar archivo
                String rutaArchivo = guardarScreenshot(escenarioActual.get(), nombreArchivo, screenshot);

                // Adjuntar al reporte si es un fallo o modo debug
                if ("FALLO".equals(tipoCaptura) || "DEBUG".equals(tipoCaptura)) {
                    escenario.attach(screenshot, "image/png", "Screenshot Paso " + numeroPaso + " - " + tipoCaptura);
                }

                logger.debug("Screenshot paso capturado: {}", rutaArchivo);
            }

        } catch (Exception e) {
            logger.error("Error capturando screenshot paso {}: {}", numeroPaso, e.getMessage());
        }
    }

    /**
     * Captura screenshot final del escenario.
     */
    private byte[] capturarPantallaEscenario(Scenario escenario, String tipoCaptura) {
        try {
            WebDriver driver = ConfiguradorNavegador.obtenerDriverActual();
            if (driver == null) return null;

            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            if (screenshot != null && screenshot.length > 0) {
                // Generar nombre del archivo
                String nombreArchivo = String.format("final-%s-%s.png",
                        tipoCaptura.toLowerCase(),
                        LocalDateTime.now().format(FORMATO_TIMESTAMP));

                // Guardar archivo
                String rutaArchivo = guardarScreenshot(escenarioActual.get(), nombreArchivo, screenshot);

                logger.debug("Screenshot escenario capturado: {}", rutaArchivo);
                return screenshot;
            }

        } catch (Exception e) {
            logger.error("Error capturando screenshot escenario: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Captura información adicional para debugging.
     */
    private void capturarInformacionDebug(Scenario escenario, int numeroPaso) {
        try {
            WebDriver driver = ConfiguradorNavegador.obtenerDriverActual();
            if (driver == null) return;

            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("=== INFORMACIÓN DEBUG PASO ").append(numeroPaso).append(" ===\n");
            debugInfo.append("Timestamp: ").append(LocalDateTime.now()).append("\n");
            debugInfo.append("URL actual: ").append(driver.getCurrentUrl()).append("\n");
            debugInfo.append("Título página: ").append(driver.getTitle()).append("\n");
            debugInfo.append("Tamaño ventana: ").append(driver.manage().window().getSize()).append("\n");

            // Información de cookies (opcional)
            try {
                debugInfo.append("Cookies: ").append(driver.manage().getCookies().size()).append(" cookies\n");
            } catch (Exception e) {
                debugInfo.append("Cookies: Error obteniendo cookies\n");
            }

            debugInfo.append("=== FIN INFORMACIÓN DEBUG ===");

            // Adjuntar información debug al reporte
            escenario.attach(debugInfo.toString(), "text/plain", "Debug Info Paso " + numeroPaso);

        } catch (Exception e) {
            logger.warn("Error capturando información debug: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE GESTIÓN DE ARCHIVOS ====================

    /**
     * Guarda el screenshot en el sistema de archivos.
     */
    private String guardarScreenshot(String nombreEscenario, String nombreArchivo, byte[] screenshot) {
        try {
            String directorioEscenario = directoriosEscenarios.get(nombreEscenario);
            if (directorioEscenario == null) {
                directorioEscenario = crearDirectorioEscenario(nombreEscenario);
                directoriosEscenarios.put(nombreEscenario, directorioEscenario);
            }

            Path rutaArchivo = Paths.get(directorioEscenario, nombreArchivo);
            Files.write(rutaArchivo, screenshot);

            return rutaArchivo.toString();

        } catch (IOException e) {
            logger.error("Error guardando screenshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Crea el directorio base para screenshots.
     */
    private static void crearDirectorioBase() {
        try {
            Path directorioBase = Paths.get(DIRECTORIO_BASE_SCREENSHOTS);
            if (!Files.exists(directorioBase)) {
                Files.createDirectories(directorioBase);
                logger.debug("Directorio base screenshots creado: {}", directorioBase);
            }

        } catch (IOException e) {
            logger.error("Error creando directorio base screenshots: {}", e.getMessage());
        }
    }

    /**
     * Crea directorio específico para el escenario.
     */
    private String crearDirectorioEscenario(String nombreEscenario) {
        try {
            // Crear estructura de directorios por fecha y escenario
            LocalDateTime ahora = LocalDateTime.now();
            String fechaEjecucion = ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String horaEjecucion = ahora.format(DateTimeFormatter.ofPattern("HH-mm-ss"));

            String rutaDirectorio = String.format("%s/%s/%s-%s",
                    DIRECTORIO_BASE_SCREENSHOTS, fechaEjecucion, horaEjecucion, nombreEscenario);

            Path directorio = Paths.get(rutaDirectorio);
            if (!Files.exists(directorio)) {
                Files.createDirectories(directorio);
                logger.debug("Directorio escenario creado: {}", directorio);
            }

            return rutaDirectorio;

        } catch (IOException e) {
            logger.error("Error creando directorio escenario: {}", e.getMessage());
            // Fallback al directorio base
            return DIRECTORIO_BASE_SCREENSHOTS;
        }
    }

    /**
     * Limpia nombre de archivo removiendo caracteres especiales.
     */
    private String limpiarNombreArchivo(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "escenario-sin-nombre";
        }

        return nombre
                .replaceAll("[^a-zA-Z0-9áéíóúüñÁÉÍÓÚÜÑ\\s-]", "") // Remover caracteres especiales
                .replaceAll("\\s+", "-") // Espacios a guiones
                .replaceAll("-+", "-") // Múltiples guiones a uno
                .toLowerCase()
                .substring(0, Math.min(nombre.length(), 50)); // Máximo 50 caracteres
    }

    // ==================== MÉTODOS DE CONFIGURACIÓN Y UTILIDAD ====================

    /**
     * Verifica si está en modo DEBUG.
     */
    private boolean esModeDEBUG() {
        return "DEBUG".equalsIgnoreCase(System.getProperty("logging.level.root")) ||
                "true".equalsIgnoreCase(System.getProperty("debug.mode")) ||
                "true".equalsIgnoreCase(System.getProperty("cucumber.debug"));
    }

    /**
     * Captura screenshot manual - método público para usar en steps.
     */
    public static byte[] capturarScreenshotManual(String descripcion) {
        try {
            if (!ConfiguradorNavegador.tieneDriverActivo()) {
                logger.warn("No hay WebDriver activo para captura manual");
                return null;
            }

            WebDriver driver = ConfiguradorNavegador.obtenerDriverActual();
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            if (screenshot != null) {
                // Guardar con nombre descriptivo
                String nombreEscenario = escenarioActual.get();
                if (nombreEscenario != null) {
                    String nombreArchivo = String.format("manual-%s-%s.png",
                            limpiarNombreArchivo(descripcion),
                            LocalDateTime.now().format(FORMATO_TIMESTAMP));

                    // Obtener instancia para acceder al método no estático
                    HooksScreenshots instance = new HooksScreenshots();
                    instance.guardarScreenshot(nombreEscenario, nombreArchivo, screenshot);
                }

                logger.debug("Screenshot manual capturado: {}", descripcion);
            }

            return screenshot;

        } catch (Exception e) {
            logger.error("Error en captura manual '{}': {}", descripcion, e.getMessage());
            return null;
        }
    }

    /**
     * Limpia screenshots antiguos (más de X días).
     */
    public static void limpiarScreenshotsAntiguos(int diasRetencion) {
        try {
            Path directorioBase = Paths.get(DIRECTORIO_BASE_SCREENSHOTS);
            if (!Files.exists(directorioBase)) return;

            long timestampLimite = System.currentTimeMillis() - (diasRetencion * 24L * 60L * 60L * 1000L);

            Files.walk(directorioBase)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < timestampLimite;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            logger.debug("Screenshot antiguo eliminado: {}", path);
                        } catch (IOException e) {
                            logger.warn("No se pudo eliminar screenshot: {}", path);
                        }
                    });

            logger.info("Limpieza de screenshots completada (retención: {} días)", diasRetencion);

        } catch (Exception e) {
            logger.error("Error limpiando screenshots antiguos: {}", e.getMessage());
        }
    }

    /**
     * Obtiene estadísticas de screenshots.
     */
    public static Map<String, Object> obtenerEstadisticasScreenshots() {
        Map<String, Object> estadisticas = new HashMap<>();

        try {
            Path directorioBase = Paths.get(DIRECTORIO_BASE_SCREENSHOTS);
            if (!Files.exists(directorioBase)) {
                estadisticas.put("total_archivos", 0);
                estadisticas.put("tamaño_total_mb", 0.0);
                return estadisticas;
            }

            final long[] totalArchivos = {0};
            final long[] tamaßoTotalBytes = {0};

            Files.walk(directorioBase)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            totalArchivos[0]++;
                            tamaßoTotalBytes[0] += Files.size(path);
                        } catch (IOException e) {
                            logger.debug("Error obteniendo tamaño de {}", path);
                        }
                    });

            estadisticas.put("directorio_base", directorioBase.toString());
            estadisticas.put("total_archivos", totalArchivos[0]);
            estadisticas.put("tamaño_total_mb", tamaßoTotalBytes[0] / 1024.0 / 1024.0);
            estadisticas.put("timestamp_consulta", LocalDateTime.now().toString());

        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas screenshots: {}", e.getMessage());
            estadisticas.put("error", e.getMessage());
        }

        return estadisticas;
    }

    /**
     * Configuración avanzada de capturas por escenario.
     */
    @Before("@ScreenshotCompleto")
    public void configurarScreenshotCompleto(Scenario escenario) {
        logger.info("Configurando captura completa para escenario: {}", escenario.getName());

        try {
            // Configurar captura en cada paso para este escenario
            System.setProperty("screenshot.mode.override", "always");

            // Configurar información adicional
            if (ConfiguradorNavegador.tieneDriverActivo()) {
                WebDriver driver = ConfiguradorNavegador.obtenerDriverActual();

                // Maximizar ventana para capturas completas
                driver.manage().window().maximize();

                // Esperar que la página se estabilice
                Thread.sleep(500);
            }

        } catch (Exception e) {
            logger.warn("Error configurando screenshot completo: {}", e.getMessage());
        }
    }

    /**
     * Limpieza de configuración después de escenarios con captura completa.
     */
    @After("@ScreenshotCompleto")
    public void limpiarConfiguracionScreenshotCompleto(Scenario escenario) {
        try {
            // Restaurar configuración por defecto
            System.clearProperty("screenshot.mode.override");

            logger.debug("Configuración screenshot completo limpiada");

        } catch (Exception e) {
            logger.warn("Error limpiando configuración screenshot: {}", e.getMessage());
        }
    }

    // Método de limpieza general para shutdown
    public static void limpiezaGeneral() {
        try {
            // Limpiar ThreadLocal
            escenarioActual.remove();
            contadorPasos.remove();

            // Limpiar cache de directorios
            directoriosEscenarios.clear();

            // Limpiar screenshots antiguos (más de 7 días)
            limpiarScreenshotsAntiguos(7);

            logger.info("Limpieza general de HooksScreenshots completada");

        } catch (Exception e) {
            logger.error("Error en limpieza general screenshots: {}", e.getMessage());
        }
    }
}