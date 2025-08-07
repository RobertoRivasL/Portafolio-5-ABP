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
 * @author Equipo QA Automatización
 * @version 1.0
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

    // Métodos para URLs de la aplicación

    /**
     * Obtiene la URL base de la aplicación
     *
     * @return URL base
     */
    public String obtenerUrlBase() {
        return obtenerPropiedad("app.url.base", URL_BASE_DEFECTO);
    }

    /**
     * Obtiene la URL de la página de login
     *
     * @return URL de login
     */
    public String obtenerUrlLogin() {
        return obtenerPropiedad("app.url.login", URL_LOGIN_DEFECTO);
    }

    /**
     * Obtiene la URL de la página de registro
     *
     * @return URL de registro
     */
    public String obtenerUrlRegistro() {
        return obtenerPropiedad("app.url.registro", URL_REGISTRO_DEFECTO);
    }

    /**
     * Obtiene la URL del dashboard
     *
     * @return URL del dashboard
     */
    public String obtenerUrlDashboard() {
        return obtenerPropiedad("app.url.dashboard", URL_DASHBOARD_DEFECTO);
    }

    /**
     * Obtiene la URL de la página de productos
     *
     * @return URL de productos
     */
    public String obtenerUrlProductos() {
        return obtenerPropiedad("app.url.productos", URL_PRODUCTOS_DEFECTO);
    }

    // Métodos para configuración de WebDriver

    /**
     * Obtiene el tipo de navegador a usar
     *
     * @return Tipo de navegador (chrome, firefox, edge)
     */
    public String obtenerTipoNavegador() {
        return obtenerPropiedad("webdriver.browser", "chrome");
    }

    /**
     * Indica si el navegador debe ejecutarse en modo headless
     *
     * @return true si debe ser headless
     */
    public boolean esNavegadorHeadless() {
        return Boolean.parseBoolean(obtenerPropiedad("webdriver.headless", "false"));
    }

    /**
     * Obtiene el timeout implícito para WebDriver
     *
     * @return Timeout en segundos
     */
    public int obtenerTimeoutImplicito() {
        return Integer.parseInt(obtenerPropiedad("webdriver.timeout.implicit",
                String.valueOf(TIMEOUT_IMPLICITO_DEFECTO)));
    }

    /**
     * Obtiene el timeout explícito para WebDriverWait
     *
     * @return Timeout en segundos
     */
    public int obtenerTimeoutExplicito() {
        return Integer.parseInt(obtenerPropiedad("webdriver.timeout.explicit",
                String.valueOf(TIMEOUT_EXPLICITO_DEFECTO)));
    }

    /**
     * Obtiene el timeout de carga de página
     *
     * @return Timeout en segundos
     */
    public int obtenerTimeoutCargaPagina() {
        return Integer.parseInt(obtenerPropiedad("webdriver.timeout.pageload",
                String.valueOf(TIMEOUT_CARGA_PAGINA_DEFECTO)));
    }

    // Métodos para configuración de reportes

    /**
     * Obtiene el directorio base para reportes
     *
     * @return Directorio de reportes
     */
    public String obtenerDirectorioReportes() {
        return obtenerPropiedad("reportes.directorio", "reportes");
    }

    /**
     * Indica si se deben tomar capturas de pantalla en fallos
     *
     * @return true si se deben tomar capturas
     */
    public boolean tomarCapturasEnFallos() {
        return Boolean.parseBoolean(obtenerPropiedad("reportes.capturas.fallos", "true"));
    }

    /**
     * Indica si se deben tomar capturas de pantalla en todos los pasos
     *
     * @return true si se deben tomar capturas en todos los pasos
     */
    public boolean tomarCapturasEnTodosLosPasos() {
        return Boolean.parseBoolean(obtenerPropiedad("reportes.capturas.todos", "false"));
    }

    // Métodos para configuración de base de datos

    /**
     * Obtiene la URL de la base de datos de pruebas
     *
     * @return URL de BD
     */
    public String obtenerUrlBaseDatos() {
        return obtenerPropiedad("bd.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
    }

    /**
     * Obtiene el usuario de la base de datos
     *
     * @return Usuario de BD
     */
    public String obtenerUsuarioBaseDatos() {
        return obtenerPropiedad("bd.usuario", "sa");
    }

    /**
     * Obtiene la contraseña de la base de datos
     *
     * @return Contraseña de BD
     */
    public String obtenerPasswordBaseDatos() {
        return obtenerPropiedad("bd.password", "");
    }

    // Métodos para datos de prueba

    /**
     * Obtiene el email del usuario de prueba principal
     *
     * @return Email de usuario de prueba
     */
    public String obtenerEmailUsuarioPrueba() {
        return obtenerPropiedad("test.usuario.email", "test@test.com");
    }

    /**
     * Obtiene la contraseña del usuario de prueba principal
     *
     * @return Contraseña de usuario de prueba
     */
    public String obtenerPasswordUsuarioPrueba() {
        return obtenerPropiedad("test.usuario.password", "password123");
    }

    /**
     * Obtiene el nombre del usuario de prueba principal
     *
     * @return Nombre de usuario de prueba
     */
    public String obtenerNombreUsuarioPrueba() {
        return obtenerPropiedad("test.usuario.nombre", "Usuario Test");
    }

    // Métodos para configuración de logging

    /**
     * Obtiene el nivel de logging
     *
     * @return Nivel de logging (DEBUG, INFO, WARN, ERROR)
     */
    public String obtenerNivelLogging() {
        return obtenerPropiedad("logging.level", "INFO");
    }

    /**
     * Indica si se debe loggear información detallada de las pruebas
     *
     * @return true si se debe loggear información detallada
     */
    public boolean esLoggingDetallado() {
        return Boolean.parseBoolean(obtenerPropiedad("logging.detallado", "true"));
    }

    // Métodos para configuración de entorno

    /**
     * Obtiene el entorno de ejecución actual
     *
     * @return Entorno (dev, test, staging, prod)
     */
    public String obtenerEntorno() {
        return obtenerPropiedad("app.entorno", "test");
    }

    /**
     * Indica si se está ejecutando en modo de desarrollo
     *
     * @return true si es entorno de desarrollo
     */
    public boolean esModoDesarrollo() {
        return "dev".equalsIgnoreCase(obtenerEntorno());
    }

    /**
     * Indica si se está ejecutando en modo de producción
     *
     * @return true si es entorno de producción
     */
    public boolean esModoProduccion() {
        return "prod".equalsIgnoreCase(obtenerEntorno());
    }

    // Métodos auxiliares privados

    /**
     * Carga las propiedades desde archivos y system properties
     */
    private void cargarPropiedades() {
        try {
            // Cargar propiedades principales
            cargarArchivoPropiedad(ARCHIVO_PROPIEDADES_PRINCIPAL);

            // Cargar propiedades específicas de test si existen
            cargarArchivoPropiedad(ARCHIVO_PROPIEDADES_TEST);

            // Sobrescribir con system properties
            propiedades.putAll(System.getProperties());

            logger.info("Propiedades cargadas exitosamente. Entorno: {}", obtenerEntorno());

        } catch (Exception e) {
            logger.warn("Error cargando propiedades: {}. Usando valores por defecto.", e.getMessage());
        }
    }

    /**
     * Carga un archivo de propiedades específico
     *
     * @param nombreArchivo Nombre del archivo a cargar
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
     * Obtiene una propiedad con valor por defecto
     *
     * @param clave Clave de la propiedad
     * @param valorDefecto Valor por defecto si no existe la propiedad
     * @return Valor de la propiedad
     */
    public String obtenerPropiedad(String clave, String valorDefecto) {
        // Prioridad: System Properties > Archivo Properties > Valor por defecto
        String valor = System.getProperty(clave);

        if (valor == null) {
            valor = propiedades.getProperty(clave, valorDefecto);
        }

        logger.debug("Propiedad '{}' = '{}'", clave, valor);
        return valor;
    }

    /**
     * Establece una propiedad programáticamente
     *
     * @param clave Clave de la propiedad
     * @param valor Valor de la propiedad
     */
    public void establecerPropiedad(String clave, String valor) {
        propiedades.setProperty(clave, valor);
        logger.debug("Propiedad establecida: '{}' = '{}'", clave, valor);
    }

    /**
     * Obtiene todas las propiedades como Properties
     *
     * @return Copia de todas las propiedades
     */
    public Properties obtenerTodasLasPropiedades() {
        Properties copia = new Properties();
        copia.putAll(propiedades);
        return copia;
    }

    /**
     * Genera un resumen de la configuración actual
     *
     * @return String con resumen de configuración
     */
    public String generarResumenConfiguracion() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("=== Configuración Actual ===\n");
        resumen.append("Entorno: ").append(obtenerEntorno()).append("\n");
        resumen.append("URL Base: ").append(obtenerUrlBase()).append("\n");
        resumen.append("Navegador: ").append(obtenerTipoNavegador());

        if (esNavegadorHeadless()) {
            resumen.append(" (headless)");
        }
        resumen.append("\n");

        resumen.append("Timeout Implícito: ").append(obtenerTimeoutImplicito()).append("s\n");
        resumen.append("Timeout Explícito: ").append(obtenerTimeoutExplicito()).append("s\n");
        resumen.append("Directorio Reportes: ").append(obtenerDirectorioReportes()).append("\n");
        resumen.append("Nivel Logging: ").append(obtenerNivelLogging()).append("\n");

        return resumen.toString();
    }

    /**
     * Obtiene el email del usuario por defecto para pruebas.
     *
     * @return email del usuario por defecto
     */
    public String obtenerUsuarioDefecto() {
        return propiedades.getProperty("usuario.defecto.email", "test@test.com");
    }

    /**
     * Obtiene la contraseña del usuario por defecto para pruebas.
     *
     * @return contraseña del usuario por defecto
     */
    public String obtenerPasswordDefecto() {
        return propiedades.getProperty("usuario.defecto.password", "password123");
    }


    /**
     * Obtiene la URL del módulo CRUD de la aplicación.
     *
     * @return URL del CRUD
     */
    public String obtenerUrlCrud() {
        return propiedades.getProperty("aplicacion.url.crud",
                obtenerUrlBase() + "/productos");
    }

}