package com.qa.automatizacion.configuracion;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Configurador centralizado para la gestión del WebDriver.
 * Implementa Singleton Pattern para asegurar una única instancia del navegador.
 *
 * Responsabilidades:
 * - Inicialización y configuración del WebDriver
 * - Gestión del ciclo de vida del navegador
 * - Configuración de timeouts y opciones
 * - Soporte para múltiples navegadores
 * - Configuración para diferentes entornos (local, CI/CD)
 *
 * Principios aplicados:
 * - Singleton Pattern: Una sola instancia de WebDriver
 * - Factory Pattern: Creación de diferentes tipos de navegador
 * - Abstraction: Oculta complejidad de configuración de Selenium
 * - Configuration Management: Centraliza toda la configuración
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0
 */
public class ConfiguradorNavegador {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguradorNavegador.class);

    // Instancia única del WebDriver (Singleton)
    private static WebDriver navegador;
    private static WebDriverWait espera;

    // Configuración
    private static PropiedadesAplicacion propiedades;
    private static boolean navegadorInicializado = false;

    // Constantes
    private static final int TIMEOUT_DEFECTO = 10;
    private static final String USER_AGENT_DEFECTO = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    /**
     * Constructor privado para implementar Singleton Pattern.
     */
    private ConfiguradorNavegador() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Inicializa el WebDriver según la configuración.
     *
     * @return WebDriver configurado
     */
    public static synchronized WebDriver inicializarNavegador() {
        if (navegador != null) {
            logger.debug("WebDriver ya está inicializado");
            return navegador;
        }

        try {
            logger.info("🚀 Inicializando WebDriver");

            // Cargar configuración
            propiedades = PropiedadesAplicacion.obtenerInstancia();

            // Obtener tipo de navegador
            String tipoNavegador = propiedades.obtenerTipoNavegador();
            logger.info("Configurando navegador: {}", tipoNavegador);

            // Crear navegador según tipo
            navegador = crearNavegador(tipoNavegador);

            // Configurar navegador
            configurarNavegador();

            // Crear WebDriverWait
            int timeoutExplicito = propiedades.obtenerTimeoutExplicito();
            espera = new WebDriverWait(navegador, Duration.ofSeconds(timeoutExplicito));

            navegadorInicializado = true;
            logger.info("✅ WebDriver inicializado exitosamente: {}",
                    navegador.getClass().getSimpleName());

            return navegador;

        } catch (Exception e) {
            logger.error("❌ Error inicializando WebDriver: {}", e.getMessage(), e);
            throw new RuntimeException("Error inicializando navegador: " + e.getMessage(), e);
        }
    }

    /**
     * Crea el navegador según el tipo especificado.
     *
     * @param tipoNavegador tipo de navegador (chrome, firefox, edge)
     * @return WebDriver configurado
     */
    private static WebDriver crearNavegador(String tipoNavegador) {
        return switch (tipoNavegador.toLowerCase()) {
            case "chrome" -> crearChrome();
            case "firefox" -> crearFirefox();
            case "edge" -> crearEdge();
            default -> {
                logger.warn("Tipo de navegador no reconocido: {}. Usando Chrome por defecto", tipoNavegador);
                yield crearChrome();
            }
        };
    }

    /**
     * Crea y configura Chrome WebDriver.
     *
     * @return ChromeDriver configurado
     */
    private static WebDriver crearChrome() {
        logger.debug("Configurando Chrome WebDriver");

        try {
            // Configurar WebDriverManager
            WebDriverManager.chromedriver().setup();

            // Crear opciones de Chrome
            ChromeOptions opciones = new ChromeOptions();

            // Configuraciones básicas
            configurarOpcionesChromeBasicas(opciones);

            // Configuraciones específicas del entorno
            configurarOpcionesChromeEntorno(opciones);

            // Crear driver
            ChromeDriver driver = new ChromeDriver(opciones);

            logger.debug("Chrome WebDriver creado exitosamente");
            return driver;

        } catch (Exception e) {
            logger.error("Error creando Chrome WebDriver: {}", e.getMessage());
            throw new RuntimeException("Error configurando Chrome: " + e.getMessage(), e);
        }
    }

    /**
     * Configura las opciones básicas de Chrome.
     */
    private static void configurarOpcionesChromeBasicas(ChromeOptions opciones) {
        // Configuraciones de estabilidad
        opciones.addArguments("--no-sandbox");
        opciones.addArguments("--disable-dev-shm-usage");
        opciones.addArguments("--disable-gpu");
        opciones.addArguments("--disable-extensions");
        opciones.addArguments("--disable-plugins");
        opciones.addArguments("--disable-images");

        // Configuraciones de rendimiento
        opciones.addArguments("--memory-pressure-off");
        opciones.addArguments("--max_old_space_size=4096");

        // Configuraciones de red
        opciones.addArguments("--aggressive-cache-discard");
        opciones.addArguments("--disable-background-timer-throttling");
        opciones.addArguments("--disable-renderer-backgrounding");

        // User Agent
        opciones.addArguments("--user-agent=" + USER_AGENT_DEFECTO);

        // Configuraciones de seguridad
        opciones.addArguments("--disable-web-security");
        opciones.addArguments("--allow-running-insecure-content");
        opciones.addArguments("--ignore-certificate-errors");
        opciones.addArguments("--ignore-ssl-errors");
        opciones.addArguments("--ignore-certificate-errors-spki-list");

        // Preferencias adicionales
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("profile.managed_default_content_settings.images", 2);
        opciones.setExperimentalOption("prefs", prefs);
    }

    /**
     * Configura opciones específicas del entorno para Chrome.
     */
    private static void configurarOpcionesChromeEntorno(ChromeOptions opciones) {
        // Modo headless
        boolean headless = propiedades.obtenerNavegadorHeadless();
        if (headless) {
            opciones.addArguments("--headless=new");
            logger.debug("Chrome configurado en modo headless");
        }

        // Modo incógnito
        boolean incognito = propiedades.obtenerNavegadorIncognito();
        if (incognito) {
            opciones.addArguments("--incognito");
            logger.debug("Chrome configurado en modo incógnito");
        }

        // Tamaño de ventana
        if (headless || !propiedades.obtenerNavegadorMaximizar()) {
            int ancho = propiedades.obtenerNavegadorAncho();
            int alto = propiedades.obtenerNavegadorAlto();
            opciones.addArguments(String.format("--window-size=%d,%d", ancho, alto));
            logger.debug("Chrome configurado con resolución: {}x{}", ancho, alto);
        }

        // Configuración para CI/CD
        if (esModoCI()) {
            opciones.addArguments("--headless=new");
            opciones.addArguments("--disable-logging");
            opciones.addArguments("--silent");
            opciones.addArguments("--disable-background-timer-throttling");
            logger.debug("Chrome configurado para modo CI/CD");
        }
    }

    /**
     * Crea y configura Firefox WebDriver.
     *
     * @return FirefoxDriver configurado
     */
    private static WebDriver crearFirefox() {
        logger.debug("Configurando Firefox WebDriver");

        try {
            // Configurar WebDriverManager
            WebDriverManager.firefoxdriver().setup();

            // Crear opciones de Firefox
            FirefoxOptions opciones = new FirefoxOptions();

            // Configuraciones básicas
            if (propiedades.obtenerNavegadorHeadless()) {
                opciones.addArguments("--headless");
            }

            // Configuraciones de rendimiento
            opciones.addPreference("browser.cache.disk.enable", false);
            opciones.addPreference("browser.cache.memory.enable", false);
            opciones.addPreference("network.http.use-cache", false);

            // Configuraciones de seguridad
            opciones.addPreference("security.tls.insecure_fallback_hosts", "localhost");
            opciones.addPreference("security.mixed_content.block_active_content", false);

            // Crear driver
            FirefoxDriver driver = new FirefoxDriver(opciones);

            logger.debug("Firefox WebDriver creado exitosamente");
            return driver;

        } catch (Exception e) {
            logger.error("Error creando Firefox WebDriver: {}", e.getMessage());
            throw new RuntimeException("Error configurando Firefox: " + e.getMessage(), e);
        }
    }

    /**
     * Crea y configura Edge WebDriver.
     *
     * @return EdgeDriver configurado
     */
    private static WebDriver crearEdge() {
        logger.debug("Configurando Edge WebDriver");

        try {
            // Configurar WebDriverManager
            WebDriverManager.edgedriver().setup();

            // Crear opciones de Edge
            EdgeOptions opciones = new EdgeOptions();

            // Configuraciones básicas
            if (propiedades.obtenerNavegadorHeadless()) {
                opciones.addArguments("--headless");
            }

            opciones.addArguments("--no-sandbox");
            opciones.addArguments("--disable-dev-shm-usage");
            opciones.addArguments("--disable-gpu");

            // Crear driver
            EdgeDriver driver = new EdgeDriver(opciones);

            logger.debug("Edge WebDriver creado exitosamente");
            return driver;

        } catch (Exception e) {
            logger.error("Error creando Edge WebDriver: {}", e.getMessage());
            throw new RuntimeException("Error configurando Edge: " + e.getMessage(), e);
        }
    }

    /**
     * Configura el navegador después de la inicialización.
     */
    private static void configurarNavegador() {
        try {
            // Configurar timeouts
            configurarTimeouts();

            // Configurar ventana
            configurarVentana();

            logger.debug("Navegador configurado exitosamente");

        } catch (Exception e) {
            logger.error("Error configurando navegador: {}", e.getMessage());
            throw new RuntimeException("Error en configuración post-inicialización: " + e.getMessage(), e);
        }
    }

    /**
     * Configura los timeouts del navegador.
     */
    private static void configurarTimeouts() {
        int timeoutImplicito = propiedades.obtenerTimeoutImplicito();
        int timeoutCargaPagina = propiedades.obtenerTimeoutCargaPagina();
        int timeoutScript = propiedades.obtenerTimeoutScript();

        navegador.manage().timeouts()
                .implicitlyWait(Duration.ofSeconds(timeoutImplicito))
                .pageLoadTimeout(Duration.ofSeconds(timeoutCargaPagina))
                .scriptTimeout(Duration.ofSeconds(timeoutScript));

        logger.debug("Timeouts configurados - Implícito: {}s, Carga página: {}s, Script: {}s",
                timeoutImplicito, timeoutCargaPagina, timeoutScript);
    }

    /**
     * Configura la ventana del navegador.
     */
    private static void configurarVentana() {
        if (propiedades.obtenerNavegadorMaximizar() && !propiedades.obtenerNavegadorHeadless()) {
            navegador.manage().window().maximize();
            logger.debug("Ventana maximizada");
        } else {
            int ancho = propiedades.obtenerNavegadorAncho();
            int alto = propiedades.obtenerNavegadorAlto();
            navegador.manage().window().setSize(new org.openqa.selenium.Dimension(ancho, alto));
            logger.debug("Ventana configurada a {}x{}", ancho, alto);
        }
    }

    /**
     * Obtiene la instancia actual del WebDriver.
     *
     * @return WebDriver actual o null si no está inicializado
     */
    public static WebDriver obtenerNavegador() {
        if (navegador == null) {
            logger.warn("WebDriver no está inicializado. Inicializando automáticamente...");
            return inicializarNavegador();
        }
        return navegador;
    }

    /**
     * Obtiene la instancia de WebDriverWait.
     *
     * @return WebDriverWait configurado
     */
    public static WebDriverWait obtenerEspera() {
        if (espera == null && navegador != null) {
            int timeout = propiedades != null ? propiedades.obtenerTimeoutExplicito() : TIMEOUT_DEFECTO;
            espera = new WebDriverWait(navegador, Duration.ofSeconds(timeout));
        }
        return espera;
    }

    /**
     * Navega a una URL específica.
     *
     * @param url URL destino
     */
    public static void navegarA(String url) {
        if (navegador == null) {
            throw new IllegalStateException("WebDriver no está inicializado");
        }

        logger.debug("Navegando a: {}", url);
        navegador.get(url);
    }

    /**
     * Cierra el navegador y limpia recursos.
     */
    public static synchronized void cerrarNavegador() {
        if (navegador != null) {
            try {
                logger.info("🔒 Cerrando WebDriver");
                navegador.quit();

            } catch (Exception e) {
                logger.warn("Error cerrando WebDriver: {}", e.getMessage());

            } finally {
                navegador = null;
                espera = null;
                navegadorInicializado = false;
                logger.debug("WebDriver limpiado exitosamente");
            }
        }
    }

    /**
     * Verifica si el navegador está inicializado.
     *
     * @return true si está inicializado, false en caso contrario
     */
    public static boolean esNavegadorInicializado() {
        return navegadorInicializado && navegador != null;
    }

    /**
     * Reinicia el navegador.
     */
    public static void reiniciarNavegador() {
        logger.info("🔄 Reiniciando WebDriver");
        cerrarNavegador();
        inicializarNavegador();
    }

    /**
     * Configura emulación móvil para Chrome.
     */
    public static void configurarEmulacionMovil() {
        if (navegador instanceof ChromeDriver && propiedades != null) {
            logger.info("📱 Configurando emulación móvil");

            // Esta configuración requiere reiniciar el navegador con nuevas opciones
            String dispositivo = propiedades.obtenerDispositivoMovil();
            logger.debug("Dispositivo móvil configurado: {}", dispositivo);

            // Reiniciar con configuración móvil sería necesario aquí
            // Por simplicidad, se registra la acción
        }
    }

    /**
     * Configura timeouts extendidos para pruebas lentas.
     */
    public static void configurarTimeoutsExtendidos() {
        if (navegador != null) {
            logger.info("⏱️  Configurando timeouts extendidos");

            navegador.manage().timeouts()
                    .implicitlyWait(Duration.ofSeconds(30))
                    .pageLoadTimeout(Duration.ofSeconds(60))
                    .scriptTimeout(Duration.ofSeconds(60));

            // Actualizar WebDriverWait también
            espera = new WebDriverWait(navegador, Duration.ofSeconds(30));

            logger.debug("Timeouts extendidos configurados");
        }
    }

    /**
     * Verifica si está ejecutándose en modo CI/CD.
     *
     * @return true si es modo CI, false en caso contrario
     */
    private static boolean esModoCI() {
        return System.getenv("CI") != null ||
                System.getenv("JENKINS_URL") != null ||
                System.getenv("GITHUB_ACTIONS") != null ||
                System.getenv("GITLAB_CI") != null ||
                (propiedades != null && propiedades.obtenerCiHeadlessForzado());
    }

    /**
     * Obtiene información del navegador actual.
     *
     * @return información del navegador
     */
    public static String obtenerInformacionNavegador() {
        if (navegador != null) {
            try {
                return String.format("Navegador: %s | URL: %s | Título: %s",
                        navegador.getClass().getSimpleName(),
                        navegador.getCurrentUrl(),
                        navegador.getTitle());
            } catch (Exception e) {
                return "Error obteniendo información: " + e.getMessage();
            }
        }
        return "Navegador no inicializado";
    }
}