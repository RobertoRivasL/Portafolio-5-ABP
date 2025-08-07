package com.qa.automatizacion.configuracion;

import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configurador del navegador web para las pruebas automatizadas.
 * Implementa el patrón Singleton para gestionar las instancias de WebDriver.
 *
 * Principios aplicados:
 * - Separación de Intereses: Se encarga únicamente de la configuración del navegador
 * - Encapsulación: Oculta la complejidad de la configuración del WebDriver
 * - Modularidad: Permite cambiar fácilmente entre diferentes tipos de navegadores
 * - Thread Safety: Soporte para ejecución en paralelo
 */
public class ConfiguradorNavegador {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguradorNavegador.class);
    private static final ConcurrentHashMap<String, WebDriver> instanciasDriver = new ConcurrentHashMap<>();
    private static final PropiedadesAplicacion propiedades = PropiedadesAplicacion.obtenerInstancia();

    // Constructor privado para implementar Singleton
    private ConfiguradorNavegador() {
    }

    // ==================== MÉTODOS PÚBLICOS PRINCIPALES ====================

    /**
     * Obtiene una instancia de WebDriver para el hilo actual.
     * Implementa ThreadLocal para soporte de ejecución en paralelo.
     *
     * @return WebDriver configurado según las propiedades
     */
    public static WebDriver obtenerDriver() {
        String nombreHilo = Thread.currentThread().getName();

        if (!instanciasDriver.containsKey(nombreHilo)) {
            WebDriver driver = crearNuevoDriver();
            configurarTimeouts(driver);
            instanciasDriver.put(nombreHilo, driver);
            logger.info("Nuevo WebDriver creado para el hilo: {}", nombreHilo);
        }

        return instanciasDriver.get(nombreHilo);
    }

    /**
     * Navega a una URL específica.
     *
     * @param url URL de destino
     */
    public static void navegarA(String url) {
        validarUrl(url);

        try {
            WebDriver driver = obtenerDriver();
            logger.info("Navegando a: {}", url);

            driver.get(url);
            esperarCargaCompletaPagina(driver);

            logger.debug("Navegación exitosa a: {}", url);

        } catch (Exception e) {
            logger.error("Error navegando a {}: {}", url, e.getMessage());
            throw new RuntimeException("No se pudo navegar a la URL: " + url, e);
        }
    }

    /**
     * Obtiene la URL actual del navegador.
     *
     * @return URL actual
     */
    public static String obtenerUrlActual() {
        try {
            WebDriver driver = obtenerDriver();
            String urlActual = driver.getCurrentUrl();

            logger.debug("URL actual obtenida: {}", urlActual);
            return urlActual;

        } catch (Exception e) {
            logger.error("Error obteniendo URL actual: {}", e.getMessage());
            throw new RuntimeException("No se pudo obtener la URL actual", e);
        }
    }

    /**
     * Obtiene el título de la página actual.
     *
     * @return título de la página
     */
    public static String obtenerTituloPagina() {
        try {
            WebDriver driver = obtenerDriver();
            String titulo = driver.getTitle();

            logger.debug("Título de página obtenido: {}", titulo);
            return titulo;

        } catch (Exception e) {
            logger.error("Error obteniendo título de página: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Verifica si hay un driver activo para el hilo actual.
     *
     * @return true si hay un driver activo y funcional
     */
    public static boolean tieneDriverActivo() {
        try {
            String nombreHilo = Thread.currentThread().getName();
            WebDriver driver = instanciasDriver.get(nombreHilo);

            if (driver == null) {
                return false;
            }

            // Verificación simple de que el driver funciona
            driver.getCurrentUrl();
            return true;

        } catch (Exception e) {
            logger.debug("Driver no disponible: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Toma una captura de pantalla del estado actual.
     *
     * @return bytes de la imagen o null si hay error
     */
    public static byte[] tomarCapturaPantalla() {
        try {
            WebDriver driver = obtenerDriver();

            if (driver instanceof TakesScreenshot) {
                TakesScreenshot screenshot = (TakesScreenshot) driver;
                byte[] captura = screenshot.getScreenshotAs(OutputType.BYTES);

                logger.debug("Captura de pantalla tomada exitosamente");
                return captura;
            } else {
                logger.warn("El driver no soporta capturas de pantalla");
                return null;
            }

        } catch (Exception e) {
            logger.error("Error tomando captura de pantalla: {}", e.getMessage());
            return null;
        }
    }

    // ==================== MÉTODOS DE NAVEGACIÓN ====================

    /**
     * Actualiza la página actual.
     */
    public static void actualizarPagina() {
        try {
            WebDriver driver = obtenerDriver();
            logger.debug("Actualizando página actual");

            driver.navigate().refresh();
            esperarCargaCompletaPagina(driver);

        } catch (Exception e) {
            logger.error("Error actualizando página: {}", e.getMessage());
            throw new RuntimeException("No se pudo actualizar la página", e);
        }
    }

    /**
     * Navega hacia atrás en el historial del navegador.
     */
    public static void navegarAtras() {
        try {
            WebDriver driver = obtenerDriver();
            logger.debug("Navegando hacia atrás");

            driver.navigate().back();
            esperarCargaCompletaPagina(driver);

        } catch (Exception e) {
            logger.error("Error navegando hacia atrás: {}", e.getMessage());
            throw new RuntimeException("No se pudo navegar hacia atrás", e);
        }
    }

    /**
     * Navega hacia adelante en el historial del navegador.
     */
    public static void navegarAdelante() {
        try {
            WebDriver driver = obtenerDriver();
            logger.debug("Navegando hacia adelante");

            driver.navigate().forward();
            esperarCargaCompletaPagina(driver);

        } catch (Exception e) {
            logger.error("Error navegando hacia adelante: {}", e.getMessage());
            throw new RuntimeException("No se pudo navegar hacia adelante", e);
        }
    }

    // ==================== MÉTODOS DE GESTIÓN DE DRIVER ====================

    /**
     * Cierra el WebDriver del hilo actual.
     */
    public static void cerrarDriver() {
        String nombreHilo = Thread.currentThread().getName();
        WebDriver driver = instanciasDriver.remove(nombreHilo);

        if (driver != null) {
            try {
                driver.quit();
                logger.info("WebDriver cerrado para el hilo: {}", nombreHilo);
            } catch (Exception e) {
                logger.warn("Error cerrando WebDriver: {}", e.getMessage());
            }
        }
    }

    /**
     * Cierra todos los WebDrivers activos.
     */
    public static void cerrarTodosLosDrivers() {
        instanciasDriver.forEach((hilo, driver) -> {
            try {
                driver.quit();
                logger.info("WebDriver cerrado para el hilo: {}", hilo);
            } catch (Exception e) {
                logger.warn("Error cerrando WebDriver del hilo {}: {}", hilo, e.getMessage());
            }
        });

        instanciasDriver.clear();
        logger.info("Todos los WebDrivers han sido cerrados");
    }

    // ==================== MÉTODOS UTILITARIOS ====================

    /**
     * Limpia y valida un nombre de archivo para screenshots.
     *
     * @param nombre nombre propuesto para el archivo
     * @return nombre de archivo válido
     */
    public static String limpiarNombreArchivo(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "screenshot_" + System.currentTimeMillis();
        }

        return nombre.replaceAll("[^a-zA-Z0-9_-]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_+|_+$", "")
                .toLowerCase();
    }

    /**
     * Obtiene información de diagnóstico del navegador.
     *
     * @return mapa con información del navegador
     */
    public static Map<String, Object> obtenerInformacionDiagnostico() {
        Map<String, Object> info = new HashMap<>();

        try {
            String nombreHilo = Thread.currentThread().getName();
            boolean tieneDriver = instanciasDriver.containsKey(nombreHilo);

            info.put("hiloActual", nombreHilo);
            info.put("tieneDriver", tieneDriver);
            info.put("totalDrivers", instanciasDriver.size());

            if (tieneDriver) {
                WebDriver driver = instanciasDriver.get(nombreHilo);
                info.put("tipoDriver", driver.getClass().getSimpleName());

                try {
                    info.put("urlActual", driver.getCurrentUrl());
                    info.put("titulo", driver.getTitle());
                } catch (Exception e) {
                    info.put("estadoDriver", "No disponible: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            info.put("error", "Error obteniendo diagnóstico: " + e.getMessage());
        }

        return info;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Crea una nueva instancia de WebDriver basada en la configuración.
     */
    private static WebDriver crearNuevoDriver() {
        String tipoNavegador = propiedades.obtenerPropiedad("navegador.tipo", "chrome");
        boolean modoHeadless = Boolean.parseBoolean(
                propiedades.obtenerPropiedad("navegador.headless", "false")
        );

        return switch (tipoNavegador.toLowerCase()) {
            case "chrome" -> crearDriverChrome(modoHeadless);
            case "firefox" -> crearDriverFirefox(modoHeadless);
            default -> {
                logger.warn("Tipo de navegador no soportado: {}. Usando Chrome por defecto.", tipoNavegador);
                yield crearDriverChrome(modoHeadless);
            }
        };
    }

    /**
     * Crea y configura un WebDriver para Chrome.
     */
    private static WebDriver crearDriverChrome(boolean modoHeadless) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions opciones = new ChromeOptions();
        opciones.addArguments("--disable-blink-features=AutomationControlled");
        opciones.addArguments("--disable-extensions");
        opciones.addArguments("--no-sandbox");
        opciones.addArguments("--disable-dev-shm-usage");
        opciones.addArguments("--remote-allow-origins=*");

        if (modoHeadless) {
            opciones.addArguments("--headless");
            logger.info("Chrome configurado en modo headless");
        }

        opciones.setExperimentalOption("useAutomationExtension", false);
        opciones.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        return new ChromeDriver(opciones);
    }

    /**
     * Crea y configura un WebDriver para Firefox.
     */
    private static WebDriver crearDriverFirefox(boolean modoHeadless) {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions opciones = new FirefoxOptions();

        if (modoHeadless) {
            opciones.addArguments("--headless");
            logger.info("Firefox configurado en modo headless");
        }

        return new FirefoxDriver(opciones);
    }

    /**
     * Configura los timeouts del WebDriver según las propiedades.
     */
    private static void configurarTimeouts(WebDriver driver) {
        int timeoutImplicito = Integer.parseInt(
                propiedades.obtenerPropiedad("navegador.timeout.implicito", "10")
        );
        int timeoutExplicito = Integer.parseInt(
                propiedades.obtenerPropiedad("navegador.timeout.explicito", "15")
        );

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeoutImplicito));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeoutExplicito));
        driver.manage().window().maximize();

        logger.info("Timeouts configurados - Implícito: {}s, Explícito: {}s",
                timeoutImplicito, timeoutExplicito);
    }

    /**
     * Espera a que la página se cargue completamente.
     */
    private static void esperarCargaCompletaPagina(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState").equals("complete"));

            logger.debug("Página cargada completamente");

        } catch (Exception e) {
            logger.warn("Timeout esperando carga de página: {}", e.getMessage());
        }
    }

    /**
     * Valida que una URL no sea nula o vacía.
     */
    private static void validarUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL no puede ser nula o vacía");
        }
    }
}