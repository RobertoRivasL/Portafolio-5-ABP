package com.qa.automatizacion.configuracion;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configurador del navegador web para las pruebas automatizadas.
 * Implementa el patrón Singleton para gestionar las instancias de WebDriver.
 *
 * Principios aplicados:
 * - Separación de Intereses: Se encarga únicamente de la configuración del navegador
 * - Encapsulación: Oculta la complejidad de la configuración del WebDriver
 * - Modularidad: Permite cambiar fácilmente entre diferentes tipos de navegadores
 */
public class ConfiguradorNavegador {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguradorNavegador.class);
    private static final ConcurrentHashMap<String, WebDriver> instanciasDriver = new ConcurrentHashMap<>();
    private static final PropiedadesAplicacion propiedades = PropiedadesAplicacion.obtenerInstancia();

    private ConfiguradorNavegador() {
        // Constructor privado para implementar Singleton
    }

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
     * Crea una nueva instancia de WebDriver basada en la configuración.
     *
     * @return WebDriver configurado
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
     *
     * @param modoHeadless indica si ejecutar en modo headless
     * @return WebDriver de Chrome configurado
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

        // Configuraciones adicionales para estabilidad
        opciones.setExperimentalOption("useAutomationExtension", false);
        opciones.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        return new ChromeDriver(opciones);
    }

    /**
     * Crea y configura un WebDriver para Firefox.
     *
     * @param modoHeadless indica si ejecutar en modo headless
     * @return WebDriver de Firefox configurado
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
     * Configura los timeouts del WebDriver según las propiedades de la aplicación.
     *
     * @param driver WebDriver a configurar
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
     * Cierra el WebDriver del hilo actual y lo remueve del cache.
     */
    public static void cerrarDriver() {
        String nombreHilo = Thread.currentThread().getName();
        WebDriver driver = instanciasDriver.get(nombreHilo);

        if (driver != null) {
            try {
                driver.quit();
                instanciasDriver.remove(nombreHilo);
                logger.info("WebDriver cerrado para el hilo: {}", nombreHilo);
            } catch (Exception e) {
                logger.error("Error al cerrar WebDriver para el hilo {}: {}", nombreHilo, e.getMessage());
            }
        }
    }

    /**
     * Cierra todas las instancias de WebDriver.
     * Útil para limpieza al final de la ejecución de todas las pruebas.
     */
    public static void cerrarTodasLasInstancias() {
        logger.info("Cerrando todas las instancias de WebDriver...");

        instanciasDriver.forEach((hilo, driver) -> {
            try {
                driver.quit();
                logger.info("WebDriver cerrado para el hilo: {}", hilo);
            } catch (Exception e) {
                logger.error("Error al cerrar WebDriver para el hilo {}: {}", hilo, e.getMessage());
            }
        });

        instanciasDriver.clear();
        logger.info("Todas las instancias de WebDriver han sido cerradas");
    }

    /**
     * Verifica si existe un driver activo para el hilo actual.
     *
     * @return true si existe un driver activo, false en caso contrario
     */
    public static boolean tieneDriverActivo() {
        String nombreHilo = Thread.currentThread().getName();
        return instanciasDriver.containsKey(nombreHilo);
    }

    /**
     * Navega a la URL especificada usando el driver actual.
     *
     * @param url URL de destino
     */
    public static void navegarA(String url) {
        WebDriver driver = obtenerDriver();
        driver.get(url);
        logger.info("Navegando a: {}", url);
    }

    /**
     * Obtiene la URL actual del navegador.
     *
     * @return URL actual
     */
    public static String obtenerUrlActual() {
        return obtenerDriver().getCurrentUrl();
    }

    /**
     * Obtiene el título de la página actual.
     *
     * @return título de la página
     */
    public static String obtenerTituloPagina() {
        return obtenerDriver().getTitle();
    }
}