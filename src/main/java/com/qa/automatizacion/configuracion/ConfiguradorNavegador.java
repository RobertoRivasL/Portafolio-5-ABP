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
 * Configurador del navegador para las pruebas automatizadas.
 * Implementa el patrón Singleton para gestionar instancias de WebDriver.
 *
 * Principios aplicados:
 * - Singleton Pattern: Una sola instancia por hilo de ejecución
 * - Encapsulación: Oculta la lógica de configuración del navegador
 * - Separación de Intereses: Se enfoca únicamente en gestión del navegador
 * - Open/Closed: Abierto para extensión con nuevos navegadores
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
public class ConfiguradorNavegador {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguradorNavegador.class);

    // ThreadLocal para soporte de ejecución en paralelo
    private static final ThreadLocal<WebDriver> driverLocal = new ThreadLocal<>();

    private static final PropiedadesAplicacion propiedades = PropiedadesAplicacion.obtenerInstancia();

    // Configuraciones por defecto
    private static final int TIMEOUT_IMPLICITO_SEGUNDOS = 10;
    private static final int TIMEOUT_PAGINA_SEGUNDOS = 30;

    /**
     * Constructor privado para prevenir instanciación externa.
     */
    private ConfiguradorNavegador() {
        // Constructor privado para patrón Singleton
    }

    // ==================== GESTIÓN DEL DRIVER ====================

    /**
     * Obtiene la instancia del WebDriver para el hilo actual.
     * Si no existe, crea una nueva instancia.
     *
     * @return instancia de WebDriver
     */
    public static WebDriver obtenerDriver() {
        if (driverLocal.get() == null) {
            inicializarDriver();
        }
        return driverLocal.get();
    }

    /**
     * Inicializa el WebDriver según la configuración.
     * Utiliza las propiedades de la aplicación para determinar el navegador.
     */
    public static void inicializarDriver() {
        try {
            String navegador = propiedades.obtenerPropiedad("navegador.tipo", "chrome");
            boolean modoHeadless = propiedades.obtenerPropiedadBooleano("navegador.headless", false);
            boolean maximizar = propiedades.obtenerPropiedadBooleano("navegador.maximizar", true);

            logger.info("Inicializando navegador: {} (headless: {})", navegador, modoHeadless);

            WebDriver driver = crearDriverSegunTipo(navegador, modoHeadless);
            configurarDriver(driver, maximizar);
            driverLocal.set(driver);

            logger.info("Navegador {} inicializado correctamente", navegador);

        } catch (Exception e) {
            logger.error("Error inicializando el navegador: {}", e.getMessage());
            throw new RuntimeException("No se pudo inicializar el navegador", e);
        }
    }

    /**
     * Crea el driver según el tipo especificado.
     *
     * @param tipoNavegador tipo de navegador (chrome, firefox, edge)
     * @param modoHeadless si debe ejecutarse en modo headless
     * @return instancia de WebDriver configurada
     */
    private static WebDriver crearDriverSegunTipo(String tipoNavegador, boolean modoHeadless) {
        return switch (tipoNavegador.toLowerCase()) {
            case "chrome" -> crearDriverChrome(modoHeadless);
            case "firefox" -> crearDriverFirefox(modoHeadless);
            case "edge" -> crearDriverEdge(modoHeadless);
            default -> {
                logger.warn("Navegador '{}' no reconocido. Usando Chrome por defecto.", tipoNavegador);
                yield crearDriverChrome(modoHeadless);
            }
        };
    }

    /**
     * Crea y configura un driver de Chrome.
     *
     * @param modoHeadless si debe ejecutarse sin interfaz gráfica
     * @return driver de Chrome configurado
     */
    private static WebDriver crearDriverChrome(boolean modoHeadless) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions opciones = new ChromeOptions();

        if (modoHeadless) {
            opciones.addArguments("--headless");
        }

        // Opciones de seguridad y rendimiento
        opciones.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--remote-allow-origins=*",
                "--disable-web-security",
                "--disable-features=VizDisplayCompositor"
        );

        // Configuraciones adicionales desde propiedades
        String argumentosAdicionales = propiedades.obtenerPropiedad("navegador.chrome.argumentos", "");
        if (!argumentosAdicionales.isEmpty()) {
            opciones.addArguments(argumentosAdicionales.split(","));
        }

        logger.debug("Creando driver Chrome con opciones: {}", opciones);
        return new ChromeDriver(opciones);
    }

    /**
     * Crea y configura un driver de Firefox.
     *
     * @param modoHeadless si debe ejecutarse sin interfaz gráfica
     * @return driver de Firefox configurado
     */
    private static WebDriver crearDriverFirefox(boolean modoHeadless) {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions opciones = new FirefoxOptions();

        if (modoHeadless) {
            opciones.addArguments("--headless");
        }

        logger.debug("Creando driver Firefox con modo headless: {}", modoHeadless);
        return new FirefoxDriver(opciones);
    }

    /**
     * Crea y configura un driver de Edge.
     *
     * @param modoHeadless si debe ejecutarse sin interfaz gráfica
     * @return driver de Edge configurado
     */
    private static WebDriver crearDriverEdge(boolean modoHeadless) {
        WebDriverManager.edgedriver().setup();

        EdgeOptions opciones = new EdgeOptions();

        if (modoHeadless) {
            opciones.addArguments("--headless");
        }

        opciones.addArguments("--remote-allow-origins=*");

        logger.debug("Creando driver Edge con modo headless: {}", modoHeadless);
        return new EdgeDriver(opciones);
    }

    /**
     * Aplica configuraciones generales al driver.
     *
     * @param driver driver a configurar
     * @param maximizar si debe maximizar la ventana
     */
    private static void configurarDriver(WebDriver driver, boolean maximizar) {
        // Configurar timeouts
        int timeoutImplicito = propiedades.obtenerPropiedadEntero("navegador.timeout.implicito", TIMEOUT_IMPLICITO_SEGUNDOS);
        int timeoutPagina = propiedades.obtenerPropiedadEntero("navegador.timeout.pagina", TIMEOUT_PAGINA_SEGUNDOS);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeoutImplicito));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeoutPagina));

        // Configurar ventana
        if (maximizar) {
            driver.manage().window().maximize();
            logger.debug("Ventana maximizada");
        } else {
            // Usar tamaño personalizado si se especifica
            String resolucion = propiedades.obtenerPropiedad("navegador.resolucion", "1920x1080");
            String[] dimensiones = resolucion.split("x");
            if (dimensiones.length == 2) {
                try {
                    int ancho = Integer.parseInt(dimensiones[0]);
                    int alto = Integer.parseInt(dimensiones[1]);
                    driver.manage().window().setSize(new org.openqa.selenium.Dimension(ancho, alto));
                    logger.debug("Ventana configurada a {}x{}", ancho, alto);
                } catch (NumberFormatException e) {
                    logger.warn("Formato de resolución inválido: {}. Usando tamaño por defecto.", resolucion);
                    driver.manage().window().maximize();
                }
            }
        }

        logger.debug("Driver configurado con timeout implícito: {}s, timeout página: {}s",
                timeoutImplicito, timeoutPagina);
    }

    // ==================== NAVEGACIÓN ====================

    /**
     * Navega a la URL especificada.
     *
     * @param url URL de destino
     */
    public static void navegarA(String url) {
        logger.info("Navegando a: {}", url);
        obtenerDriver().get(url);
    }

    /**
     * Navega hacia atrás en el historial del navegador.
     */
    public static void navegarAtras() {
        logger.debug("Navegando hacia atrás");
        obtenerDriver().navigate().back();
    }

    /**
     * Navega hacia adelante en el historial del navegador.
     */
    public static void navegarAdelante() {
        logger.debug("Navegando hacia adelante");
        obtenerDriver().navigate().forward();
    }

    /**
     * Recarga la página actual.
     */
    public static void recargarPagina() {
        logger.debug("Recargando página actual");
        obtenerDriver().navigate().refresh();
    }

    // ==================== GESTIÓN DEL CICLO DE VIDA ====================

    /**
     * Verifica si existe un driver activo para el hilo actual.
     *
     * @return true si hay un driver activo, false en caso contrario
     */
    public static boolean tieneDriverActivo() {
        return driverLocal.get() != null;
    }

    /**
     * Cierra la ventana actual del navegador.
     */
    public static void cerrarVentana() {
        WebDriver driver = driverLocal.get();
        if (driver != null) {
            logger.debug("Cerrando ventana actual del navegador");
            driver.close();
        }
    }

    /**
     * Cierra el navegador y limpia el driver del hilo actual.
     */
    public static void cerrarDriver() {
        WebDriver driver = driverLocal.get();
        if (driver != null) {
            try {
                logger.info("Cerrando navegador");
                driver.quit();
            } catch (Exception e) {
                logger.warn("Error cerrando el navegador: {}", e.getMessage());
            } finally {
                driverLocal.remove();
                logger.debug("Driver removido del ThreadLocal");
            }
        }
    }

    /**
     * Cierra todos los drivers activos.
     * Útil para limpieza global al finalizar todas las pruebas.
     */
    public static void cerrarTodosLosDrivers() {
        logger.info("Iniciando cierre de todos los drivers activos");

        // Cerrar driver del hilo actual
        cerrarDriver();

        // Limpiar referencias del ThreadLocal
        driverLocal.remove();

        logger.info("Todos los drivers han sido cerrados");
    }

    // ==================== UTILIDADES ====================

    /**
     * Obtiene la URL actual del navegador.
     *
     * @return URL actual o cadena vacía si no hay driver activo
     */
    public static String obtenerUrlActual() {
        WebDriver driver = driverLocal.get();
        return driver != null ? driver.getCurrentUrl() : "";
    }

    /**
     * Obtiene el título de la página actual.
     *
     * @return título de la página o cadena vacía si no hay driver activo
     */
    public static String obtenerTituloPagina() {
        WebDriver driver = driverLocal.get();
        return driver != null ? driver.getTitle() : "";
    }

    /**
     * Toma una captura de pantalla de la página actual.
     *
     * @return bytes de la imagen o null si no es posible
     */
    public static byte[] tomarCapturaPantalla() {
        WebDriver driver = driverLocal.get();
        if (driver instanceof org.openqa.selenium.TakesScreenshot) {
            try {
                return ((org.openqa.selenium.TakesScreenshot) driver)
                        .getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
            } catch (Exception e) {
                logger.error("Error tomando captura de pantalla: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * Obtiene información del navegador para diagnóstico.
     *
     * @return información del estado actual del navegador
     */
    public static String obtenerInformacionDiagnostico() {
        WebDriver driver = driverLocal.get();
        if (driver == null) {
            return "No hay driver activo";
        }

        StringBuilder info = new StringBuilder();
        info.append("URL actual: ").append(driver.getCurrentUrl()).append("\n");
        info.append("Título: ").append(driver.getTitle()).append("\n");
        info.append("Ventanas abiertas: ").append(driver.getWindowHandles().size()).append("\n");

        return info.toString();
    }
}