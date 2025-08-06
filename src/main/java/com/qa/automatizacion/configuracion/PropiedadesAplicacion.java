package com.qa.automatizacion.configuracion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Gestiona las propiedades de configuración de la aplicación.
 * Implementa el patrón Singleton para garantizar una única instancia.
 *
 * Principios aplicados:
 * - Singleton: Una sola instancia para toda la aplicación
 * - Encapsulación: Oculta la complejidad de carga de propiedades
 * - Separación de Intereses: Se enfoca únicamente en la gestión de configuración
 */
public class PropiedadesAplicacion {

    private static final Logger logger = LoggerFactory.getLogger(PropiedadesAplicacion.class);
    private static final String ARCHIVO_PROPIEDADES = "configuracion/application.properties";
    private static PropiedadesAplicacion instancia;
    private final Properties propiedades;

    /**
     * Constructor privado para implementar el patrón Singleton.
     * Carga las propiedades desde el archivo de configuración.
     */
    private PropiedadesAplicacion() {
        this.propiedades = new Properties();
        cargarPropiedades();
    }

    /**
     * Obtiene la instancia única de PropiedadesAplicacion.
     * Thread-safe usando synchronized.
     *
     * @return instancia única de PropiedadesAplicacion
     */
    public static synchronized PropiedadesAplicacion obtenerInstancia() {
        if (instancia == null) {
            instancia = new PropiedadesAplicacion();
        }
        return instancia;
    }

    /**
     * Carga las propiedades desde el archivo de configuración.
     * Implementa manejo de errores robusto.
     */
    private void cargarPropiedades() {
        try (InputStream flujoEntrada = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(ARCHIVO_PROPIEDADES)) {

            if (flujoEntrada == null) {
                logger.warn("No se encontró el archivo de propiedades: {}. Usando valores por defecto.",
                        ARCHIVO_PROPIEDADES);
                cargarPropiedadesPorDefecto();
                return;
            }

            propiedades.load(flujoEntrada);
            logger.info("Propiedades cargadas exitosamente desde: {}", ARCHIVO_PROPIEDADES);

            // Log de propiedades cargadas (sin mostrar información sensible)
            if (logger.isDebugEnabled()) {
                propiedades.forEach((clave, valor) -> {
                    String valorMostrar = esPropiedadSensible(clave.toString()) ? "***" : valor.toString();
                    logger.debug("Propiedad cargada: {} = {}", clave, valorMostrar);
                });
            }

        } catch (IOException e) {
            logger.error("Error al cargar el archivo de propiedades: {}", e.getMessage());
            cargarPropiedadesPorDefecto();
        } catch (Exception e) {
            logger.error("Error inesperado al cargar propiedades: {}", e.getMessage());
            cargarPropiedadesPorDefecto();
        }
    }

    /**
     * Carga propiedades por defecto en caso de error o archivo faltante.
     */
    private void cargarPropiedadesPorDefecto() {
        logger.info("Cargando propiedades por defecto...");

        // Configuración del navegador
        propiedades.setProperty("navegador.tipo", "chrome");
        propiedades.setProperty("navegador.headless", "false");
        propiedades.setProperty("navegador.timeout.implicito", "10");
        propiedades.setProperty("navegador.timeout.explicito", "15");

        // URLs de la aplicación
        propiedades.setProperty("aplicacion.url.base", "http://localhost:8080");
        propiedades.setProperty("aplicacion.url.login", "http://localhost:8080/login");
        propiedades.setProperty("aplicacion.url.registro", "http://localhost:8080/registro");
        propiedades.setProperty("aplicacion.url.dashboard", "http://localhost:8080/dashboard");

        // Base de datos
        propiedades.setProperty("bd.url", "jdbc:h2:mem:testdb");
        propiedades.setProperty("bd.usuario", "sa");
        propiedades.setProperty("bd.password", "");
        propiedades.setProperty("bd.driver", "org.h2.Driver");

        // Reportes
        propiedades.setProperty("reportes.directorio", "reportes");
        propiedades.setProperty("reportes.formato", "html,json");
        propiedades.setProperty("reportes.incluir.screenshots", "true");

        logger.info("Propiedades por defecto cargadas exitosamente");
    }

    /**
     * Determina si una propiedad contiene información sensible.
     *
     * @param nombrePropiedad nombre de la propiedad a evaluar
     * @return true si es sensible, false en caso contrario
     */
    private boolean esPropiedadSensible(String nombrePropiedad) {
        return nombrePropiedad.toLowerCase().contains("password") ||
                nombrePropiedad.toLowerCase().contains("clave") ||
                nombrePropiedad.toLowerCase().contains("secret") ||
                nombrePropiedad.toLowerCase().contains("token");
    }

    /**
     * Obtiene el valor de una propiedad.
     *
     * @param clave nombre de la propiedad
     * @return valor de la propiedad o null si no existe
     */
    public String obtenerPropiedad(String clave) {
        String valor = propiedades.getProperty(clave);

        if (valor == null) {
            logger.warn("Propiedad no encontrada: {}", clave);
        }

        return valor;
    }

    /**
     * Obtiene el valor de una propiedad con un valor por defecto.
     *
     * @param clave nombre de la propiedad
     * @param valorPorDefecto valor a retornar si la propiedad no existe
     * @return valor de la propiedad o valorPorDefecto si no existe
     */
    public String obtenerPropiedad(String clave, String valorPorDefecto) {
        String valor = propiedades.getProperty(clave, valorPorDefecto);

        if (valor.equals(valorPorDefecto)) {
            logger.debug("Usando valor por defecto para {}: {}", clave, valorPorDefecto);
        }

        return valor;
    }

    /**
     * Obtiene una propiedad como entero.
     *
     * @param clave nombre de la propiedad
     * @param valorPorDefecto valor por defecto si la propiedad no existe o no es un entero válido
     * @return valor entero de la propiedad
     */
    public int obtenerPropiedadEntero(String clave, int valorPorDefecto) {
        String valor = obtenerPropiedad(clave);

        if (valor == null) {
            return valorPorDefecto;
        }

        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            logger.warn("El valor '{}' para la propiedad '{}' no es un entero válido. Usando valor por defecto: {}",
                    valor, clave, valorPorDefecto);
            return valorPorDefecto;
        }
    }

    /**
     * Obtiene una propiedad como booleano.
     *
     * @param clave nombre de la propiedad
     * @param valorPorDefecto valor por defecto si la propiedad no existe
     * @return valor booleano de la propiedad
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
     * Establece el valor de una propiedad en tiempo de ejecución.
     *
     * @param clave nombre de la propiedad
     * @param valor valor a establecer
     */
    public void establecerPropiedad(String clave, String valor) {
        propiedades.setProperty(clave, valor);
        logger.debug("Propiedad establecida: {} = {}", clave,
                esPropiedadSensible(clave) ? "***" : valor);
    }

    /**
     * Verifica si una propiedad existe.
     *
     * @param clave nombre de la propiedad
     * @return true si la propiedad existe, false en caso contrario
     */
    public boolean existePropiedad(String clave) {
        return propiedades.containsKey(clave);
    }

    /**
     * Obtiene todas las propiedades como un objeto Properties.
     * Retorna una copia para evitar modificaciones externas.
     *
     * @return copia de todas las propiedades
     */
    public Properties obtenerTodasLasPropiedades() {
        Properties copia = new Properties();
        copia.putAll(propiedades);
        return copia;
    }

    /**
     * Obtiene la URL base de la aplicación.
     *
     * @return URL base configurada
     */
    public String obtenerUrlBase() {
        return obtenerPropiedad("aplicacion.url.base", "http://localhost:8080");
    }

    /**
     * Obtiene la URL de login.
     *
     * @return URL de login
     */
    public String obtenerUrlLogin() {
        return obtenerPropiedad("aplicacion.url.login", obtenerUrlBase() + "/login");
    }

    /**
     * Obtiene la URL de registro.
     *
     * @return URL de registro
     */
    public String obtenerUrlRegistro() {
        return obtenerPropiedad("aplicacion.url.registro", obtenerUrlBase() + "/registro");
    }

    /**
     * Obtiene la URL del dashboard.
     *
     * @return URL del dashboard
     */
    public String obtenerUrlDashboard() {
        return obtenerPropiedad("aplicacion.url.dashboard", obtenerUrlBase() + "/dashboard");
    }

    /**
     * Obtiene el directorio de reportes.
     *
     * @return directorio configurado para reportes
     */
    public String obtenerDirectorioReportes() {
        return obtenerPropiedad("reportes.directorio", "reportes");
    }

    /**
     * Verifica si se deben incluir screenshots en los reportes.
     *
     * @return true si se deben incluir screenshots
     */
    public boolean debeIncluirScreenshots() {
        return obtenerPropiedadBooleano("reportes.incluir.screenshots", true);
    }
}