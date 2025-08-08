package com.qa.automatizacion.configuracion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Gestor de propiedades de la aplicación para pruebas automatizadas.
 * Centraliza la configuración y permite sobrescribir valores via system properties.
 *
 * Principios aplicados:
 * - Single Responsibility: Se enfoca únicamente en gestión de propiedades
 * - Strategy Pattern: Permite diferentes fuentes de configuración
 * - Singleton Pattern: Una sola instancia de configuración
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0
 */
public class PropiedadesAplicacion {

    private static final Logger logger = LoggerFactory.getLogger(PropiedadesAplicacion.class);

    private static PropiedadesAplicacion instancia;
    private final Properties propiedades;

    // Nombres de archivos de propiedades
    private static final String ARCHIVO_PROPIEDADES_PRINCIPAL = "application.properties";
    private static final String ARCHIVO_PROPIEDADES_TEST = "application-test.properties";

    // URLs por defecto
    private static final String URL_BASE_DEFECTO = "http://localhost:8080";
    private static final String URL_LOGIN_DEFECTO = URL_BASE_DEFECTO + "/login";
    private static final String URL_REGISTRO_DEFECTO = URL_BASE_DEFECTO + "/registro";
    private static final String URL_DASHBOARD_DEFECTO = URL_BASE_DEFECTO + "/dashboard";
    private static final String URL_PRODUCTOS_DEFECTO = URL_BASE_DEFECTO + "/productos";

    // Timeouts por defecto (en segundos)
    private static final int TIMEOUT_IMPLICITO_DEFECTO = 10;
    private static final int TIMEOUT_EXPLICITO_DEFECTO = 15;
    private static final int TIMEOUT_CARGA_PAGINA_DEFECTO = 30;

    /**
     * Constructor privado para implementar Singleton
     */
    private PropiedadesAplicacion() {
        this.propiedades = new Properties();
        cargarPropiedades();
    }

    /**
     * Obtiene la instancia única de PropiedadesAplicacion
     *
     * @return Instancia única
     */
    public static synchronized PropiedadesAplicacion obtenerInstancia() {
        if (instancia == null) {
            instancia = new PropiedadesAplicacion();
        }
        return instancia;
    }

    // ==================== MÉTODOS DE URLS ====================

    /**
     * Obtiene la URL base de la aplicación
     */
    public String obtenerUrlBase() {
        return obtenerPropiedad("aplicacion.url.base", URL_BASE_DEFECTO);
    }

    /**
     * Obtiene la URL de la página de login
     */
    public String obtenerUrlLogin() {
        return obtenerPropiedad("aplicacion.url.login", obtenerUrlBase() + "/login");
    }

    /**
     * Obtiene la URL de la página de registro
     */
    public String obtenerUrlRegistro() {
        return obtenerPropiedad("aplicacion.url.registro", obtenerUrlBase() + "/registro");
    }

    /**
     * Obtiene la URL del dashboard
     */
    public String obtenerUrlDashboard() {
        return obtenerPropiedad("aplicacion.url.dashboard", obtenerUrlBase() + "/dashboard");
    }

    /**
     * Obtiene la URL de la página de productos
     */
    public String obtenerUrlProductos() {
        return obtenerPropiedad("aplicacion.url.productos", obtenerUrlBase() + "/productos");
    }

    /**
     * Obtiene la URL del módulo CRUD
     */
    public String obtenerUrlCrud() {
        return obtenerUrlProductos();
    }

    // ==================== MÉTODOS DE NAVEGADOR ====================

    /**
     * Obtiene el tipo de navegador configurado
     */
    public String obtenerTipoNavegador() {
        return obtenerPropiedad("navegador.tipo", "chrome");
    }

    /**
     * Verifica si el navegador debe ejecutarse en modo headless
     */
    public boolean obtenerNavegadorHeadless() {
        return obtenerPropiedadBooleano("navegador.headless", false);
    }

    /**
     * Verifica si el navegador debe maximizarse
     */
    public boolean obtenerNavegadorMaximizar() {
        return obtenerPropiedadBooleano("navegador.maximizar", true);
    }

    /**
     * Verifica si el navegador debe ejecutarse en modo incógnito
     */
    public boolean obtenerNavegadorIncognito() {
        return obtenerPropiedadBooleano("navegador.incognito", false);
    }

    /**
     * Obtiene el ancho de resolución del navegador
     */
    public int obtenerNavegadorAncho() {
        return obtenerPropiedadEntero("navegador.resolucion.ancho", 1920);
    }

    /**
     * Obtiene el alto de resolución del navegador
     */
    public int obtenerNavegadorAlto() {
        return obtenerPropiedadEntero("navegador.resolucion.alto", 1080);
    }

    /**
     * Obtiene el dispositivo móvil para emulación
     */
    public String obtenerDispositivoMovil() {
        return obtenerPropiedad("navegador.movil.dispositivo", "iPhone 12");
    }

    // ==================== MÉTODOS DE TIMEOUTS ====================

    /**
     * Obtiene el timeout implícito en segundos
     */
    public int obtenerTimeoutImplicito() {
        return obtenerPropiedadEntero("navegador.timeout.implicito", TIMEOUT_IMPLICITO_DEFECTO);
    }

    /**
     * Obtiene el timeout explícito en segundos
     */
    public int obtenerTimeoutExplicito() {
        return obtenerPropiedadEntero("navegador.timeout.explicito", TIMEOUT_EXPLICITO_DEFECTO);
    }

    /**
     * Obtiene el timeout de carga de página en segundos
     */
    public int obtenerTimeoutCargaPagina() {
        return obtenerPropiedadEntero("navegador.timeout.carga.pagina", TIMEOUT_CARGA_PAGINA_DEFECTO);
    }

    /**
     * Obtiene el timeout de script en segundos
     */
    public int obtenerTimeoutScript() {
        return obtenerPropiedadEntero("navegador.timeout.script", 30);
    }

    // ==================== MÉTODOS DE CI/CD ====================

    /**
     * Verifica si está forzado el modo headless en CI
     */
    public boolean obtenerCiHeadlessForzado() {
        return obtenerPropiedadBooleano("ci.headless.forzado", true);
    }

    /**
     * Obtiene la ruta del archivo de configuración actual
     */
    public String obtenerRutaArchivoConfiguracion() {
        return "src/test/resources/application.properties";
    }

    // ==================== MÉTODOS DE REPORTES ====================

    /**
     * Obtiene el directorio base para reportes
     */
    public String obtenerDirectorioReportes() {
        return obtenerPropiedad("reportes.directorio", "reportes");
    }

    /**
     * Indica si se deben tomar capturas de pantalla en fallos
     */
    public boolean tomarCapturasEnFallos() {
        return obtenerPropiedadBooleano("reportes.capturas.fallos", true);
    }

    /**
     * Indica si se deben tomar capturas de pantalla en todos los pasos
     */
    public boolean tomarCapturasEnTodosLosPasos() {
        return obtenerPropiedadBooleano("reportes.capturas.todos", false);
    }

    // ==================== MÉTODOS DE BASE DE DATOS ====================

    /**
     * Obtiene la URL de la base de datos de pruebas
     */
    public String obtenerUrlBaseDatos() {
        return obtenerPropiedad("bd.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
    }

    /**
     * Obtiene el usuario de la base de datos
     */
    public String obtenerUsuarioBaseDatos() {
        return obtenerPropiedad("bd.usuario", "sa");
    }

    /**
     * Obtiene la contraseña de la base de datos
     */
    public String obtenerPasswordBaseDatos() {
        return obtenerPropiedad("bd.password", "");
    }

    // ==================== MÉTODOS DE DATOS DE PRUEBA ====================

    /**
     * Obtiene el email del usuario de prueba principal
     */
    public String obtenerEmailUsuarioPrueba() {
        return obtenerPropiedad("datos.usuario.valido.email", "test@test.com");
    }

    /**
     * Obtiene la contraseña del usuario de prueba principal
     */
    public String obtenerPasswordUsuarioPrueba() {
        return obtenerPropiedad("datos.usuario.valido.password", "password123");
    }

    /**
     * Obtiene el nombre del usuario de prueba principal
     */
    public String obtenerNombreUsuarioPrueba() {
        return obtenerPropiedad("datos.usuario.valido.nombre", "Usuario Test");
    }

    /**
     * Obtiene el email del usuario por defecto para pruebas
     */
    public String obtenerUsuarioDefecto() {
        return obtenerEmailUsuarioPrueba();
    }

    /**
     * Obtiene la contraseña del usuario por defecto para pruebas
     */
    public String obtenerPasswordDefecto() {
        return obtenerPasswordUsuarioPrueba();
    }

    // ==================== MÉTODOS DE LOGGING ====================

    /**
     * Obtiene el nivel de logging
     */
    public String obtenerNivelLogging() {
        return obtenerPropiedad("logs.nivel", "INFO");
    }

    /**
     * Indica si se debe loggear información detallada de las pruebas
     */
    public boolean esLoggingDetallado() {
        return obtenerPropiedadBooleano("logs.detallado", true);
    }

    // ==================== MÉTODOS DE ENTORNO ====================

    /**
     * Obtiene el entorno de ejecución actual
     */
    public String obtenerEntorno() {
        return obtenerPropiedad("entorno.nombre", "desarrollo");
    }

    /**
     * Indica si se está ejecutando en modo de desarrollo
     */
    public boolean esModoDesarrollo() {
        return "desarrollo".equalsIgnoreCase(obtenerEntorno());
    }

    /**
     * Indica si se está ejecutando en modo de producción
     */
    public boolean esModoProduccion() {
        return "produccion".equalsIgnoreCase(obtenerEntorno());
    }

    // ==================== MÉTODOS AUXILIARES PÚBLICOS ====================

    /**
     * Obtiene una propiedad con valor por defecto
     */
    public String obtenerPropiedad(String clave, String valorDefecto) {
        // Prioridad: System Properties > Archivo Properties > Valor por defecto
        String valor = System.getProperty(clave);

        if (valor == null) {
            valor = propiedades.getProperty(clave, valorDefecto);
        }

        logger.debug("Propiedad '{}' = '{}'", clave,
                esPropiedadSensible(clave) ? "***" : valor);
        return valor;
    }

    /**
     * Obtiene una propiedad sin valor por defecto
     */
    public String obtenerPropiedad(String clave) {
        return obtenerPropiedad(clave, null);
    }

    /**
     * Obtiene una propiedad como booleano
     */
    public boolean obtenerPropiedadBooleano(String clave, boolean valorPorDefecto) {
        String valor = obtenerPropiedad(clave);

        if (valor == null) {
            return valorPorDefecto;
        }

        // Valores considerados como true: true, yes, 1, on, enabled
        return valor.equalsIgnoreCase("true") ||
                valor.equalsIgnoreCase("yes") ||
                valor.equals("1") ||
                valor.equalsIgnoreCase("on") ||
                valor.equalsIgnoreCase("enabled");
    }

    /**
     * Obtiene una propiedad como entero con valor por defecto
     */
    public int obtenerPropiedadEntero(String clave, int valorPorDefecto) {
        String valor = obtenerPropiedad(clave);

        if (valor == null) {
            return valorPorDefecto;
        }

        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            logger.warn("Error parseando propiedad '{}' con valor '{}'. " +
                            "Usando valor por defecto: {}",
                    clave, valor, valorPorDefecto);
            return valorPorDefecto;
        }
    }

    /**
     * Establece una propiedad programáticamente
     */
    public void establecerPropiedad(String clave, String valor) {
        propiedades.setProperty(clave, valor);
        logger.debug("Propiedad establecida: '{}' = '{}'", clave,
                esPropiedadSensible(clave) ? "***" : valor);
    }

    /**
     * Verifica si una propiedad existe
     */
    public boolean existePropiedad(String clave) {
        return propiedades.containsKey(clave) || System.getProperty(clave) != null;
    }

    /**
     * Obtiene todas las propiedades como Properties
     */
    public Properties obtenerTodasLasPropiedades() {
        Properties copia = new Properties();
        copia.putAll(propiedades);
        // Agregar system properties
        copia.putAll(System.getProperties());
        return copia;
    }

    /**
     * Genera un resumen de la configuración actual
     */
    public String generarResumenConfiguracion() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("=== Configuración Actual ===\n");
        resumen.append("Entorno: ").append(obtenerEntorno()).append("\n");
        resumen.append("URL Base: ").append(obtenerUrlBase()).append("\n");
        resumen.append("Navegador: ").append(obtenerTipoNavegador());

        if (obtenerNavegadorHeadless()) {
            resumen.append(" (headless)");
        }
        resumen.append("\n");

        resumen.append("Timeout Implícito: ").append(obtenerTimeoutImplicito()).append("s\n");
        resumen.append("Timeout Explícito: ").append(obtenerTimeoutExplicito()).append("s\n");
        resumen.append("Directorio Reportes: ").append(obtenerDirectorioReportes()).append("\n");
        resumen.append("Nivel Logging: ").append(obtenerNivelLogging()).append("\n");

        return resumen.toString();
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    /**
     * Carga las propiedades desde archivos y system properties
     */
    private void cargarPropiedades() {
        try {
            // Cargar propiedades principales
            cargarArchivoPropiedad(ARCHIVO_PROPIEDADES_PRINCIPAL);

            // Cargar propiedades específicas de test si existen
            cargarArchivoPropiedad(ARCHIVO_PROPIEDADES_TEST);

            // Las system properties ya se manejan en obtenerPropiedad()

            logger.info("Propiedades cargadas exitosamente. Entorno: {}", obtenerEntorno());

        } catch (Exception e) {
            logger.warn("Error cargando propiedades: {}. Usando valores por defecto.", e.getMessage());
        }
    }

    /**
     * Carga un archivo de propiedades específico
     */
    private void cargarArchivoPropiedad(String nombreArchivo) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(nombreArchivo)) {

            if (inputStream != null) {
                Properties propiedadesArchivo = new Properties();
                propiedadesArchivo.load(inputStream);

                // Agregar propiedades del archivo a las propiedades principales
                propiedades.putAll(propiedadesArchivo);

                logger.debug("Archivo de propiedades cargado: {}", nombreArchivo);
            } else {
                logger.debug("Archivo de propiedades no encontrado: {}", nombreArchivo);
            }

        } catch (IOException e) {
            logger.warn("Error leyendo archivo de propiedades {}: {}", nombreArchivo, e.getMessage());
        }
    }

    /**
     * Verifica si una propiedad contiene información sensible
     */
    private boolean esPropiedadSensible(String clave) {
        return clave.toLowerCase().contains("password") ||
                clave.toLowerCase().contains("token") ||
                clave.toLowerCase().contains("secret") ||
                clave.toLowerCase().contains("key");
    }
}