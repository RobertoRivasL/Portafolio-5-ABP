package com.qa.automatizacion.paginas;

import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import com.qa.automatizacion.utilidades.HelperTrazabilidad;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Clase base para todas las páginas del sistema.
 * Implementa funcionalidades comunes y el patrón Page Object.
 *
 * Principios aplicados:
 * - Template Method Pattern: Define métodos base que las subclases implementan
 * - DRY: Evita repetición de código común en todas las páginas
 * - Encapsulación: Oculta la complejidad de Selenium de las pruebas
 * - Single Responsibility: Se enfoca en funcionalidades básicas de página
 * - Open/Closed: Abierto para extensión, cerrado para modificación
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
public abstract class PaginaBase {

    protected static final Logger logger = LoggerFactory.getLogger(PaginaBase.class);

    // Dependencias
    protected final WebDriver navegador;
    protected final WebDriverWait espera;
    protected final WebDriverWait esperaLarga;
    protected final PropiedadesAplicacion propiedades;
    protected final HelperTrazabilidad trazabilidad;
    protected final Actions acciones;
    protected final JavascriptExecutor ejecutorJS;

    // Configuraciones de tiempo
    private static final int TIMEOUT_ESTANDAR = 10;
    private static final int TIMEOUT_LARGO = 30;
    private static final int TIMEOUT_CORTO = 5;

    // ==================== CONSTRUCTOR ====================

    /**
     * Constructor base que inicializa todas las dependencias comunes.
     */
    protected PaginaBase() {
        this.navegador = ConfiguradorNavegador.obtenerDriver();
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();
        this.trazabilidad = new HelperTrazabilidad();

        // Configurar timeouts desde propiedades
        int timeoutEstandar = propiedades.obtenerPropiedadEntero("selenium.timeout.estandar", TIMEOUT_ESTANDAR);
        int timeoutLargo = propiedades.obtenerPropiedadEntero("selenium.timeout.largo", TIMEOUT_LARGO);

        this.espera = new WebDriverWait(navegador, Duration.ofSeconds(timeoutEstandar));
        this.esperaLarga = new WebDriverWait(navegador, Duration.ofSeconds(timeoutLargo));
        this.acciones = new Actions(navegador);
        this.ejecutorJS = (JavascriptExecutor) navegador;

        // Inicializar elementos de la página usando PageFactory
        PageFactory.initElements(navegador, this);

        logger.debug("PaginaBase inicializada para: {}", this.getClass().getSimpleName());
    }

    // ==================== MÉTODOS ABSTRACTOS ====================

    /**
     * Verifica si la página está completamente cargada.
     * Cada página debe implementar su propia lógica de verificación.
     *
     * @return true si la página está cargada, false en caso contrario
     */
    public abstract boolean estaPaginaCargada();

    /**
     * Obtiene la URL esperada para esta página.
     * Usado para validación y navegación.
     *
     * @return URL esperada de la página
     */
    public abstract String obtenerUrlEsperada();

    // ==================== MÉTODOS DE NAVEGACIÓN ====================

    /**
     * Navega a esta página y espera a que esté completamente cargada.
     */
    public void navegarAPagina() {
        String urlDestino = obtenerUrlEsperada();
        logger.info("Navegando a página: {} ({})", this.getClass().getSimpleName(), urlDestino);

        navegador.get(urlDestino);
        esperarCargaCompletaPagina();

        if (!estaPaginaCargada()) {
            throw new RuntimeException("La página " + this.getClass().getSimpleName() + " no se cargó correctamente");
        }

        logger.debug("Navegación exitosa a: {}", this.getClass().getSimpleName());
    }

    /**
     * Verifica si estamos actualmente en esta página.
     *
     * @return true si estamos en la página correcta
     */
    public boolean estamosEnEstaPagina() {
        try {
            String urlActual = navegador.getCurrentUrl();
            String urlEsperada = obtenerUrlEsperada();

            // Comparación flexible que permite parámetros de consulta
            boolean enPaginaCorrecta = urlActual.contains(extraerRutaPrincipal(urlEsperada));

            if (enPaginaCorrecta) {
                enPaginaCorrecta = estaPaginaCargada();
            }

            logger.debug("¿Estamos en {}? {} (URL actual: {}, esperada: {})",
                    this.getClass().getSimpleName(), enPaginaCorrecta, urlActual, urlEsperada);

            return enPaginaCorrecta;

        } catch (Exception e) {
            logger.error("Error verificando ubicación de página: {}", e.getMessage());
            return false;
        }
    }

    private String extraerRutaPrincipal(String url) {
        try {
            // Extraer la ruta principal sin parámetros
            int indexParametros = url.indexOf('?');
            return indexParametros > 0 ? url.substring(0, indexParametros) : url;
        } catch (Exception e) {
            return url;
        }
    }

    // ==================== MÉTODOS DE ESPERA ====================

    /**
     * Espera a que un elemento esté visible.
     *
     * @param localizador localizador del elemento
     * @return el elemento una vez que está visible
     */
    protected WebElement esperarElementoVisible(By localizador) {
        try {
            logger.debug("Esperando elemento visible: {}", localizador);
            return espera.until(ExpectedConditions.visibilityOfElementLocated(localizador));
        } catch (TimeoutException e) {
            logger.error("Timeout esperando elemento visible: {}", localizador);
            throw new RuntimeException("Elemento no se hizo visible: " + localizador, e);
        }
    }

    /**
     * Espera a que un elemento esté presente en el DOM.
     *
     * @param localizador localizador del elemento
     * @return el elemento una vez que está presente
     */
    protected WebElement esperarElementoPresente(By localizador) {
        try {
            logger.debug("Esperando elemento presente: {}", localizador);
            return espera.until(ExpectedConditions.presenceOfElementLocated(localizador));
        } catch (TimeoutException e) {
            logger.error("Timeout esperando elemento presente: {}", localizador);
            throw new RuntimeException("Elemento no está presente: " + localizador, e);
        }
    }

    /**
     * Espera a que un elemento sea clickeable.
     *
     * @param localizador localizador del elemento
     * @return el elemento una vez que es clickeable
     */
    protected WebElement esperarElementoClickeable(By localizador) {
        try {
            logger.debug("Esperando elemento clickeable: {}", localizador);
            return espera.until(ExpectedConditions.elementToBeClickable(localizador));
        } catch (TimeoutException e) {
            logger.error("Timeout esperando elemento clickeable: {}", localizador);
            throw new RuntimeException("Elemento no es clickeable: " + localizador, e);
        }
    }

    /**
     * Espera a que un elemento desaparezca.
     *
     * @param localizador localizador del elemento
     */
    protected void esperarElementoDesaparezca(By localizador) {
        try {
            logger.debug("Esperando que elemento desaparezca: {}", localizador);
            espera.until(ExpectedConditions.invisibilityOfElementLocated(localizador));
        } catch (TimeoutException e) {
            logger.warn("Elemento no desapareció en el tiempo esperado: {}", localizador);
        }
    }

    /**
     * Espera a que la página esté completamente cargada.
     */
    protected void esperarCargaCompletaPagina() {
        logger.debug("Esperando carga completa de página");

        try {
            // Esperar a que el DOM esté listo
            esperaLarga.until(webDriver ->
                    ejecutorJS.executeScript("return document.readyState").equals("complete"));

            // Esperar a que no haya requests AJAX pendientes (si se usa jQuery)
            try {
                esperaLarga.until(webDriver ->
                        ejecutorJS.executeScript("return window.jQuery != undefined ? jQuery.active == 0 : true"));
            } catch (Exception e) {
                // jQuery puede no estar disponible, continuar
                logger.debug("jQuery no disponible, continuando con carga de página");
            }

            // Pequeña pausa adicional para elementos dinámicos
            Thread.sleep(500);

        } catch (Exception e) {
            logger.warn("Error esperando carga completa de página: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE VERIFICACIÓN ====================

    /**
     * Verifica si un elemento está visible.
     *
     * @param localizador localizador del elemento
     * @return true si el elemento está visible
     */
    protected boolean esElementoVisible(By localizador) {
        try {
            WebElement elemento = navegador.findElement(localizador);
            return elemento.isDisplayed();
        } catch (NoSuchElementException e) {
            logger.debug("Elemento no encontrado: {}", localizador);
            return false;
        } catch (Exception e) {
            logger.debug("Error verificando visibilidad de elemento {}: {}", localizador, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si un elemento está presente en el DOM.
     *
     * @param localizador localizador del elemento
     * @return true si el elemento está presente
     */
    protected boolean esElementoPresente(By localizador) {
        try {
            navegador.findElement(localizador);
            return true;
        } catch (NoSuchElementException e) {
            logger.debug("Elemento no presente: {}", localizador);
            return false;
        } catch (Exception e) {
            logger.debug("Error verificando presencia de elemento {}: {}", localizador, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si un elemento está habilitado.
     *
     * @param localizador localizador del elemento
     * @return true si el elemento está habilitado
     */
    protected boolean esElementoHabilitado(By localizador) {
        try {
            WebElement elemento = navegador.findElement(localizador);
            return elemento.isEnabled();
        } catch (NoSuchElementException e) {
            logger.debug("Elemento no encontrado para verificar habilitación: {}", localizador);
            return false;
        } catch (Exception e) {
            logger.debug("Error verificando habilitación de elemento {}: {}", localizador, e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE INTERACCIÓN ====================

    /**
     * Hace clic en un elemento de forma segura.
     *
     * @param localizador localizador del elemento
     */
    protected void hacerClicSeguro(By localizador) {
        logger.debug("Haciendo clic en elemento: {}", localizador);

        try {
            WebElement elemento = esperarElementoClickeable(localizador);

            // Intentar scroll al elemento si no está visible
            scrollHastaElemento(elemento);

            elemento.click();
            logger.debug("Clic exitoso en: {}", localizador);

        } catch (Exception e) {
            logger.error("Error haciendo clic en {}: {}", localizador, e.getMessage());

            // Intentar clic con JavaScript como fallback
            try {
                WebElement elemento = navegador.findElement(localizador);
                ejecutorJS.executeScript("arguments[0].click();", elemento);
                logger.debug("Clic con JavaScript exitoso en: {}", localizador);
            } catch (Exception jsE) {
                logger.error("Error con clic JavaScript en {}: {}", localizador, jsE.getMessage());
                throw new RuntimeException("No se pudo hacer clic en el elemento: " + localizador, e);
            }
        }
    }

    /**
     * Ingresa texto en un campo de forma segura.
     *
     * @param localizador localizador del campo
     * @param texto texto a ingresar
     */
    protected void ingresarTextoSeguro(By localizador, String texto) {
        logger.debug("Ingresando texto en campo: {}", localizador);

        try {
            WebElement campo = esperarElementoVisible(localizador);

            // Limpiar campo primero
            campo.clear();

            // Ingresar nuevo texto
            campo.sendKeys(texto);

            // Verificar que el texto se ingresó correctamente
            String valorActual = campo.getAttribute("value");
            if (!texto.equals(valorActual)) {
                logger.warn("Texto ingresado no coincide. Esperado: '{}', Actual: '{}'", texto, valorActual);
            }

            logger.debug("Texto ingresado exitosamente en: {}", localizador);

        } catch (Exception e) {
            logger.error("Error ingresando texto en {}: {}", localizador, e.getMessage());
            throw new RuntimeException("No se pudo ingresar texto en el campo: " + localizador, e);
        }
    }

    /**
     * Selecciona una opción de un dropdown por texto visible.
     *
     * @param localizador localizador del dropdown
     * @param textoOpcion texto de la opción a seleccionar
     */
    protected void seleccionarOpcionPorTexto(By localizador, String textoOpcion) {
        logger.debug("Seleccionando opción '{}' en dropdown: {}", textoOpcion, localizador);

        try {
            WebElement dropdown = esperarElementoVisible(localizador);
            Select select = new Select(dropdown);
            select.selectByVisibleText(textoOpcion);

            logger.debug("Opción seleccionada exitosamente: {}", textoOpcion);

        } catch (Exception e) {
            logger.error("Error seleccionando opción '{}' en {}: {}", textoOpcion, localizador, e.getMessage());
            throw new RuntimeException("No se pudo seleccionar la opción: " + textoOpcion, e);
        }
    }

    /**
     * Realiza scroll hasta un elemento.
     *
     * @param elemento elemento al que hacer scroll
     */
    protected void scrollHastaElemento(WebElement elemento) {
        try {
            ejecutorJS.executeScript("arguments[0].scrollIntoView({block: 'center'});", elemento);
            Thread.sleep(300); // Pequeña pausa para que termine el scroll
        } catch (Exception e) {
            logger.debug("Error haciendo scroll al elemento: {}", e.getMessage());
        }
    }

    /**
     * Obtiene el texto de un elemento.
     *
     * @param localizador localizador del elemento
     * @return texto del elemento o cadena vacía si no se encuentra
     */
    protected String obtenerTextoElemento(By localizador) {
        try {
            WebElement elemento = esperarElementoVisible(localizador);
            String texto = elemento.getText().trim();
            logger.debug("Texto obtenido de {}: '{}'", localizador, texto);
            return texto;
        } catch (Exception e) {
            logger.debug("Error obteniendo texto de {}: {}", localizador, e.getMessage());
            return "";
        }
    }

    /**
     * Obtiene el valor del atributo de un elemento.
     *
     * @param localizador localizador del elemento
     * @param nombreAtributo nombre del atributo
     * @return valor del atributo o cadena vacía si no se encuentra
     */
    protected String obtenerAtributoElemento(By localizador, String nombreAtributo) {
        try {
            WebElement elemento = esperarElementoPresente(localizador);
            String valor = elemento.getAttribute(nombreAtributo);
            logger.debug("Atributo '{}' de {}: '{}'", nombreAtributo, localizador, valor);
            return valor != null ? valor : "";
        } catch (Exception e) {
            logger.debug("Error obteniendo atributo '{}' de {}: {}", nombreAtributo, localizador, e.getMessage());
            return "";
        }
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Registra una acción realizada en la página para trazabilidad.
     *
     * @param descripcionAccion descripción de la acción realizada
     */
    protected void registrarAccion(String descripcionAccion) {
        try {
            String paginaActual = this.getClass().getSimpleName();
            trazabilidad.registrarAccion(paginaActual, descripcionAccion);
            logger.debug("Acción registrada en {}: {}", paginaActual, descripcionAccion);
        } catch (Exception e) {
            logger.warn("Error registrando acción: {}", e.getMessage());
        }
    }

    /**
     * Toma una captura de pantalla de la página actual.
     *
     * @return bytes de la imagen o null si no es posible
     */
    protected byte[] tomarCapturaPantalla() {
        return ConfiguradorNavegador.tomarCapturaPantalla();
    }

    /**
     * Ejecuta JavaScript en la página.
     *
     * @param script script a ejecutar
     * @param argumentos argumentos del script
     * @return resultado de la ejecución
     */
    protected Object ejecutarJavaScript(String script, Object... argumentos) {
        try {
            return ejecutorJS.executeScript(script, argumentos);
        } catch (Exception e) {
            logger.error("Error ejecutando JavaScript: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene todos los elementos que coinciden con un localizador.
     *
     * @param localizador localizador de los elementos
     * @return lista de elementos encontrados
     */
    protected List<WebElement> obtenerElementos(By localizador) {
        try {
            return navegador.findElements(localizador);
        } catch (Exception e) {
            logger.debug("Error obteniendo elementos {}: {}", localizador, e.getMessage());
            return List.of();
        }
    }

    /**
     * Cuenta los elementos que coinciden con un localizador.
     *
     * @param localizador localizador de los elementos
     * @return número de elementos encontrados
     */
    protected int contarElementos(By localizador) {
        return obtenerElementos(localizador).size();
    }

    // ==================== MÉTODOS DE INFORMACIÓN ====================

    /**
     * Obtiene el título actual de la página.
     *
     * @return título de la página
     */
    public String obtenerTituloPagina() {
        return navegador.getTitle();
    }

    /**
     * Obtiene la URL actual.
     *
     * @return URL actual
     */
    public String obtenerUrlActual() {
        return navegador.getCurrentUrl();
    }

    /**
     * Obtiene información de diagnóstico de la página.
     *
     * @return información útil para debugging
     */
    public String obtenerInformacionDiagnostico() {
        StringBuilder info = new StringBuilder();
        info.append("Página: ").append(this.getClass().getSimpleName()).append("\n");
        info.append("URL actual: ").append(obtenerUrlActual()).append("\n");
        info.append("URL esperada: ").append(obtenerUrlEsperada()).append("\n");
        info.append("Título: ").append(obtenerTituloPagina()).append("\n");
        info.append("Página cargada: ").append(estaPaginaCargada()).append("\n");

        return info.toString();
    }
}