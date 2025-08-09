package com.qa.automatizacion.paginas;

import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase base abstracta para todos los Page Objects del sistema.
 * Proporciona funcionalidades comunes y establece contratos para las páginas hijas.
 *
 * Principios aplicados:
 * - Template Method Pattern: Define el esqueleto de operaciones comunes
 * - Abstract Factory Pattern: Establece contratos para creación de páginas
 * - DRY: Evita duplicación de código entre páginas
 * - Single Responsibility: Se enfoca en funcionalidades base de páginas
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
public abstract class PaginaBase {

    protected static final Logger logger = LoggerFactory.getLogger(PaginaBase.class);
    protected final PropiedadesAplicacion propiedades;

    /**
     * Constructor protegido para uso de clases hijas.
     */
    protected PaginaBase() {
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();
        logger.debug("PaginaBase inicializada para: {}", this.getClass().getSimpleName());
    }

    // ==================== MÉTODOS ABSTRACTOS ====================

    /**
     * Verifica si la página actual está completamente cargada.
     * Cada página hija debe implementar su propia lógica de verificación.
     *
     * @return true si la página está cargada, false en caso contrario
     */
    public abstract boolean esPaginaCargada();

    /**
     * Obtiene la URL esperada para esta página específica.
     * Cada página hija debe retornar su URL característica.
     *
     * @return fragmento de URL esperado para esta página (ej: "/login", "/dashboard")
     */
    protected abstract String obtenerUrlEsperada();

    // ==================== MÉTODOS COMUNES ====================

    /**
     * Obtiene el WebDriver actual de forma segura.
     *
     * @return WebDriver actual
     * @throws RuntimeException si no hay WebDriver activo
     */
    protected WebDriver obtenerNavegador() {
        WebDriver navegador = ConfiguradorNavegador.obtenerNavegador();
        if (navegador == null) {
            String mensaje = "WebDriver no está inicializado para " + this.getClass().getSimpleName();
            logger.error(mensaje);
            throw new RuntimeException(mensaje);
        }
        return navegador;
    }

    /**
     * Verifica si la URL actual corresponde a esta página.
     *
     * @return true si estamos en la URL correcta, false en caso contrario
     */
    public boolean estaEnUrlCorrecta() {
        try {
            String urlActual = obtenerNavegador().getCurrentUrl();
            String urlEsperada = obtenerUrlEsperada();

            boolean urlCorrecta = urlActual.contains(urlEsperada);
            logger.debug("Verificación URL - Actual: {}, Esperada: {}, Correcta: {}",
                    urlActual, urlEsperada, urlCorrecta);

            return urlCorrecta;

        } catch (Exception e) {
            logger.warn("Error verificando URL para {}: {}", this.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el título actual de la página.
     *
     * @return título de la página
     */
    public String obtenerTituloPagina() {
        try {
            String titulo = obtenerNavegador().getTitle();
            logger.debug("Título de página obtenido: {}", titulo);
            return titulo != null ? titulo : "";

        } catch (Exception e) {
            logger.warn("Error obteniendo título de página: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Verifica si el título de la página contiene el texto esperado.
     *
     * @param textoEsperado texto que debe contener el título
     * @return true si el título contiene el texto, false en caso contrario
     */
    public boolean tituloContiene(String textoEsperado) {
        if (textoEsperado == null || textoEsperado.trim().isEmpty()) {
            return false;
        }

        try {
            String tituloActual = obtenerTituloPagina().toLowerCase();
            String textoBuscar = textoEsperado.toLowerCase().trim();

            boolean contiene = tituloActual.contains(textoBuscar);
            logger.debug("Verificación título - Actual: '{}', Buscar: '{}', Contiene: {}",
                    tituloActual, textoBuscar, contiene);

            return contiene;

        } catch (Exception e) {
            logger.warn("Error verificando título: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la URL actual del navegador.
     *
     * @return URL actual
     */
    public String obtenerUrlActual() {
        try {
            String url = obtenerNavegador().getCurrentUrl();
            logger.debug("URL actual obtenida: {}", url);
            return url != null ? url : "";

        } catch (Exception e) {
            logger.warn("Error obteniendo URL actual: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Espera a que la página termine de cargar usando JavaScript.
     *
     * @return true si la página cargó completamente, false si hubo timeout
     */
    public boolean esperarCargaCompleta() {
        return esperarCargaCompleta(30); // 30 segundos por defecto
    }

    /**
     * Espera a que la página termine de cargar con timeout personalizado.
     *
     * @param timeoutSegundos timeout en segundos
     * @return true si la página cargó completamente, false si hubo timeout
     */
    public boolean esperarCargaCompleta(int timeoutSegundos) {
        try {
            org.openqa.selenium.support.ui.WebDriverWait wait =
                    new org.openqa.selenium.support.ui.WebDriverWait(
                            obtenerNavegador(),
                            java.time.Duration.ofSeconds(timeoutSegundos)
                    );

            boolean cargaCompleta = wait.until(driver -> {
                org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
                String readyState = js.executeScript("return document.readyState").toString();
                return "complete".equals(readyState);
            });

            logger.debug("Carga completa de página: {}", cargaCompleta);
            return cargaCompleta;

        } catch (Exception e) {
            logger.warn("Timeout esperando carga completa de página: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si hay elementos de carga visible en la página.
     *
     * @return true si hay indicadores de carga, false en caso contrario
     */
    public boolean hayCargaVisible() {
        try {
            // Selectores comunes para indicadores de carga
            String[] selectoresCarga = {
                    ".loading", ".spinner", ".loader",
                    "[data-testid='loading']", ".progress",
                    ".loading-overlay", ".ajax-loader"
            };

            for (String selector : selectoresCarga) {
                try {
                    org.openqa.selenium.WebElement elemento =
                            obtenerNavegador().findElement(org.openqa.selenium.By.cssSelector(selector));

                    if (elemento.isDisplayed()) {
                        logger.debug("Indicador de carga visible: {}", selector);
                        return true;
                    }
                } catch (org.openqa.selenium.NoSuchElementException e) {
                    // Elemento no encontrado, continuar con el siguiente
                }
            }

            return false;

        } catch (Exception e) {
            logger.debug("Error verificando indicadores de carga: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene información de debugging de la página actual.
     *
     * @return información de debugging
     */
    public String obtenerInformacionDebug() {
        try {
            return String.format(
                    "Página: %s | URL: %s | Título: %s | URL Correcta: %s | Cargada: %s",
                    this.getClass().getSimpleName(),
                    obtenerUrlActual(),
                    obtenerTituloPagina(),
                    estaEnUrlCorrecta(),
                    esPaginaCargada()
            );

        } catch (Exception e) {
            return String.format("Error obteniendo info debug para %s: %s",
                    this.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * Template method que implementa la verificación estándar de carga de página.
     * Las clases hijas pueden sobrescribir este método para lógica personalizada.
     *
     * @return true si la página está cargada según criterios estándar
     */
    public boolean verificarCargaEstandar() {
        try {
            // Verificaciones estándar
            boolean urlCorrecta = estaEnUrlCorrecta();
            boolean cargaCompleta = esperarCargaCompleta(10);
            boolean sinCargaVisible = !hayCargaVisible();

            logger.debug("Verificación estándar - URL: {}, Carga: {}, Sin Loading: {}",
                    urlCorrecta, cargaCompleta, sinCargaVisible);

            return urlCorrecta && cargaCompleta && sinCargaVisible;

        } catch (Exception e) {
            logger.warn("Error en verificación estándar de carga: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el nombre de la clase de la página para logging.
     *
     * @return nombre simple de la clase
     */
    protected String obtenerNombrePagina() {
        return this.getClass().getSimpleName();
    }

    /**
     * Log estándar para inicio de operación en la página.
     *
     * @param operacion descripción de la operación
     */
    protected void logInicioOperacion(String operacion) {
        logger.info("[{}] Iniciando: {}", obtenerNombrePagina(), operacion);
    }

    /**
     * Log estándar para fin exitoso de operación en la página.
     *
     * @param operacion descripción de la operación
     */
    protected void logExitoOperacion(String operacion) {
        logger.info("[{}] Completado exitosamente: {}", obtenerNombrePagina(), operacion);
    }

    /**
     * Log estándar para error en operación de la página.
     *
     * @param operacion descripción de la operación
     * @param error error ocurrido
     */
    protected void logErrorOperacion(String operacion, Exception error) {
        logger.error("[{}] Error en {}: {}", obtenerNombrePagina(), operacion, error.getMessage());
    }
}