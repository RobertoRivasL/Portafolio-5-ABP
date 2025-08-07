package com.qa.automatizacion.configuracion;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Configurador del navegador web para las pruebas automatizadas.
 * Gestiona la creación, configuración y ciclo de vida del WebDriver.
 *
 * Principios aplicados:
 * - Singleton: Una sola instancia del driver por hilo
 * - Factory Pattern: Crea diferentes tipos de navegadores
 * - Strategy Pattern: Permite cambiar estrategias de configuración
 * - Single Responsibility: Se enfoca únicamente en gestión del navegador
 *
 * @author Equipo QA Automatización
 * @version 1.0
 */
public class ConfiguradorNavegador {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguradorNavegador.class);

    // ThreadLocal para manejo de múltiples hilos en ejecución paralela
    private static final ThreadLocal<WebDriver> driverLocal = new ThreadLocal<>();

    // Configuraciones por defecto
    private static final String NAVEGADOR_DEFECTO = "chrome";
    private static final boolean HEADLESS_DEFECTO = false;
    private static final Duration TIMEOUT_DEFECTO = Duration.ofSeconds(10);

    /**
     * Constructor privado para evitar instanciación
     */
    private ConfiguradorNavegador() {
        throw new UnsupportedOperationException("Clase utilitaria no debe ser instanciada");
    }

    /**
     * Obtiene la instancia del WebDriver para el hilo actual
     *
     * @return WebDriver configurado y listo para usar
     */
    public static WebDriver obtenerDriver() {
        WebDriver driver = driverLocal.get();

        if (driver == null) {
            driver = crearDriver();
            driverLocal.set(driver);
            logger.info("Nuevo driver creado para hilo: {}", Thread.currentThread().getName());
        }

        return driver;
    }

    /**
     * Crea una nueva instancia del WebDriver según la configuración
     *
     * @return WebDriver configurado
     */
    private static WebDriver crearDriver() {
        String navegador = System.getProperty("navegador.tipo", NAVEGADOR_DEFECTO);
        boolean headless = Boolean.parseBoolean(System.getProperty("navegador.headless", "false"));

        WebDriver driver = switch (navegador.toLowerCase()) {
            case "firefox" -> crearFirefoxDriver(headless);
            case "edge" -> crearEdgeDriver(headless);
            case "chrome" -> crearChromeDriver(headless);
            default -> {
                logger.warn("Navegador '{}' no reconocido, usando Chrome por defecto", navegador);
                yield crearChromeDriver(headless);
            }
        };

        configurarDriver(driver);
        return driver;
    }

    /**
     * Crea y configura un driver de Chrome
     *
     * @param headless Si debe ejecutarse en modo headless
     * @return ChromeDriver configurado
     */
    private static WebDriver crearChromeDriver(boolean headless) {
        try {
            WebDriverManager.chromedriver().setup();

            ChromeOptions opciones = new ChromeOptions();

            // Configuraciones básicas
            opciones.addArguments("--disable-web-security");
            opciones.addArguments("--allow-running-insecure-content");
            opciones.addArguments("--disable-extensions");
            opciones.addArguments("--disable-plugins");
            opciones.addArguments("--disable-images");
            opciones.addArguments("--disable-javascript");
            opciones.addArguments("--disable-dev-shm-usage");
            opciones.addArguments("--no-sandbox");

            // Configuración de ventana
            opciones.addArguments("--window-size=1920,1080");
            opciones.addArguments("--start-maximized");

            if (headless) {
                opciones.addArguments("--headless");
                opciones.addArguments("--disable-gpu");
                logger.info("Chrome configurado en modo headless");
            }

            // Configuraciones adicionales para estabilidad
            opciones.addArguments("--remote-allow-origins=*");
            opciones.setExperimentalOption("useAutomationExtension", false);
            opciones.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

            ChromeDriver driver = new ChromeDriver(opciones);
            logger.info("ChromeDriver creado exitosamente");
            return driver;

        } catch (Exception e) {
            logger.error("Error creando ChromeDriver: {}", e.getMessage());
            throw new RuntimeException("No se pudo crear ChromeDriver", e);
        }
    }

    /**
     * Crea y configura un driver de Firefox
     *
     * @param headless Si debe ejecutarse en modo headless
     * @return FirefoxDriver configurado
     */
    private static WebDriver crearFirefoxDriver(boolean headless) {
        try {
            WebDriverManager.firefoxdriver().setup();

            FirefoxOptions opciones = new FirefoxOptions();

            if (headless) {
                opciones.addArguments("--headless");
                logger.info("Firefox configurado en modo headless");
            }

            // Configuraciones adicionales para Firefox
            opciones.addArguments("--width=1920");
            opciones.addArguments("--height=1080");

            FirefoxDriver driver = new FirefoxDriver(opciones);
            logger.info("FirefoxDriver creado exitosamente");
            return driver;

        } catch (Exception e) {
            logger.error("Error creando FirefoxDriver: {}", e.getMessage());
            throw new RuntimeException("No se pudo crear FirefoxDriver", e);
        }
    }

    /**
     * Crea y configura un driver de Edge
     *
     * @param headless Si debe ejecutarse en modo headless
     * @return EdgeDriver configurado
     */
    private static WebDriver crearEdgeDriver(boolean headless) {
        try {
            WebDriverManager.edgedriver().setup();

            EdgeOptions opciones = new EdgeOptions();

            // Configuraciones básicas
            opciones.addArguments("--disable-web-security");
            opciones.addArguments("--allow-running-insecure-content");
            opciones.addArguments("--disable-extensions");
            opciones.addArguments("--window-size=1920,1080");

            if (headless) {
                opciones.addArguments("--headless");
                opciones.addArguments("--disable-gpu");
                logger.info("Edge configurado en modo headless");
            }

            EdgeDriver driver = new EdgeDriver(opciones);
            logger.info("EdgeDriver creado exitosamente");
            return driver;

        } catch (Exception e) {
            logger.error("Error creando EdgeDriver: {}", e.getMessage());
            throw new RuntimeException("No se pudo crear EdgeDriver", e);
        }
    }

    /**
     * Configura el driver con timeouts y otras configuraciones comunes
     *
     * @param driver WebDriver a configurar
     */
    private static void configurarDriver(WebDriver driver) {
        try {
            // Configurar timeouts
            driver.manage().timeouts().implicitlyWait(TIMEOUT_DEFECTO);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));

            // Maximizar ventana si no es headless
            boolean headless = Boolean.parseBoolean(System.getProperty("navegador.headless", "false"));
            if (!headless) {
                driver.manage().window().maximize();
            }

            logger.debug("Driver configurado con timeouts y ventana maximizada");

        } catch (Exception e) {
            logger.warn("Error configurando driver: {}", e.getMessage());
        }
    }

    /**
     * Cierra el driver del hilo actual
     */
    public static void cerrarDriver() {
        WebDriver driver = driverLocal.get();

        if (driver != null) {
            try {
                driver.quit();
                logger.info("Driver cerrado para hilo: {}", Thread.currentThread().getName());
            } catch (Exception e) {
                logger.warn("Error cerrando driver: {}", e.getMessage());
            } finally {
                driverLocal.remove();
            }
        }
    }

    /**
     * Cierra todos los drivers activos (para limpieza global)
     */
    public static void cerrarTodosLosDrivers() {
        try {
            cerrarDriver();

            // Adicional: limpiar procesos orphanos si es necesario
            Runtime.getRuntime().exec("taskkill /f /im chromedriver.exe").waitFor();
            Runtime.getRuntime().exec("taskkill /f /im geckodriver.exe").waitFor();
            Runtime.getRuntime().exec("taskkill /f /im msedgedriver.exe").waitFor();

        } catch (Exception e) {
            logger.debug("Error en limpieza adicional de drivers: {}", e.getMessage());
        }
    }

    /**
     * Verifica si hay un driver activo en el hilo actual
     *
     * @return true si hay un driver activo
     */
    public static boolean hayDriverActivo() {
        WebDriver driver = driverLocal.get();

        if (driver == null) {
            return false;
        }

        try {
            // Verificar que el driver responde
            driver.getTitle();
            return true;
        } catch (Exception e) {
            logger.debug("Driver no responde, se considera inactivo: {}", e.getMessage());
            driverLocal.remove();
            return false;
        }
    }

    /**
     * Reinicia el driver del hilo actual
     *
     * @return Nuevo WebDriver configurado
     */
    public static WebDriver reiniciarDriver() {
        cerrarDriver();
        return obtenerDriver();
    }

    /**
     * Obtiene información sobre la configuración actual del navegador
     *
     * @return String con información de configuración
     */
    public static String obtenerConfiguracionActual() {
        String navegador = System.getProperty("navegador.tipo", NAVEGADOR_DEFECTO);
        boolean headless = Boolean.parseBoolean(System.getProperty("navegador.headless", "false"));

        return String.format("Navegador: %s, Headless: %s, Hilo: %s",
                navegador, headless, Thread.currentThread().getName());
    }
}