package com.qa.automatizacion.paginas;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Clase base para todas las páginas del patrón Page Object Model.
 * Proporciona funcionalidades comunes para interactuar con elementos web.
 *
 * Principios aplicados:
 * - Template Method: Define el esqueleto de operaciones comunes
 * - DRY: Evita repetición de código en las páginas
 * - Abstracción: Encapsula complejidades de Selenium
 * - Single Responsibility: Se enfoca en operaciones básicas de página
 *
 * @author Equipo QA Automatización
 * @version 1.0
 */
public abstract class PaginaBase {

    protected static final Logger logger = LoggerFactory.getLogger(PaginaBase.class);

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Actions actions;
    protected JavascriptExecutor jsExecutor;

    // Configuración de timeouts
    protected static final Duration TIMEOUT_DEFECTO = Duration.ofSeconds(10);
    protected static final Duration TIMEOUT_LARGO = Duration.ofSeconds(30);
    protected static final Duration TIMEOUT_CORTO = Duration.ofSeconds(5);

    /**
     * Constructor base que inicializa los componentes comunes
     *
     * @param driver Instancia del WebDriver
     */
    protected PaginaBase(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TIMEOUT_DEFECTO);
        this.actions = new Actions(driver);
        this.jsExecutor = (JavascriptExecutor) driver;

        // Inicializar elementos de la página con PageFactory
        PageFactory.initElements(driver, this);

        logger.debug("Página base inicializada: {}", this.getClass().getSimpleName());
    }

    /**
     * Método abstracto para verificar que la página esté cargada
     * Debe ser implementado por cada página específica
     *
     * @return true si la página está cargada correctamente
     */
    public abstract boolean estaPaginaCargada();

    // Métodos para interactuar con elementos

    /**
     * Espera a que un elemento sea visible y clickeable
     *
     * @param elemento WebElement a esperar
     * @return El mismo WebElement para encadenamiento
     */
    protected WebElement esperarElementoClickeable(WebElement elemento) {
        return wait.until(ExpectedConditions.elementToBeClickable(elemento));
    }

    /**
     * Espera a que un elemento sea visible
     *
     * @param elemento WebElement a esperar
     * @return El mismo WebElement para encadenamiento
     */
    protected WebElement esperarElementoVisible(WebElement elemento) {
        return wait.until(ExpectedConditions.visibilityOf(elemento));
    }

    /**
     * Espera a que un elemento sea visible usando localizador
     *
     * @param localizador By localizador del elemento
     * @return WebElement encontrado
     */
    protected WebElement esperarElementoVisible(By localizador) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(localizador));
    }

    /**
     * Hace clic en un elemento de forma segura
     *
     * @param elemento WebElement a hacer clic
     */
    protected void hacerClicSeguro(WebElement elemento) {
        try {
            esperarElementoClickeable(elemento).click();
            logger.debug("Clic realizado en elemento: {}", elemento.getTagName());
        } catch (Exception e) {
            logger.warn("Clic normal falló, intentando con JavaScript: {}", e.getMessage());
            hacerClicConJavascript(elemento);
        }
    }

    /**
     * Hace clic usando JavaScript
     *
     * @param elemento WebElement a hacer clic
     */
    protected void hacerClicConJavascript(WebElement elemento) {
        jsExecutor.executeScript("arguments[0].click();", elemento);
        logger.debug("Clic con JavaScript realizado");
    }

    /**
     * Escribe texto en un elemento de forma segura
     *
     * @param elemento WebElement donde escribir
     * @param texto Texto a escribir
     */
    protected void escribirTextoSeguro(WebElement elemento, String texto) {
        try {
            esperarElementoVisible(elemento);
            elemento.clear();
            elemento.sendKeys(texto);
            logger.debug("Texto '{}' escrito en elemento", texto);
        } catch (Exception e) {
            logger.warn("Error al escribir texto: {}", e.getMessage());
            escribirTextoConJavascript(elemento, texto);
        }
    }

    /**
     * Escribe texto usando JavaScript
     *
     * @param elemento WebElement donde escribir
     * @param texto Texto a escribir
     */
    protected void escribirTextoConJavascript(WebElement elemento, String texto) {
        jsExecutor.executeScript("arguments[0].value = arguments[1];", elemento, texto);
        logger.debug("Texto '{}' escrito con JavaScript", texto);
    }

    /**
     * Obtiene el texto de un elemento de forma segura
     *
     * @param elemento WebElement del cual obtener texto
     * @return Texto del elemento o cadena vacía si hay error
     */
    protected String obtenerTextoSeguro(WebElement elemento) {
        try {
            esperarElementoVisible(elemento);
            String texto = elemento.getText();
            logger.debug("Texto obtenido: '{}'", texto);
            return texto;
        } catch (Exception e) {
            logger.warn("Error al obtener texto: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Verifica si un elemento está presente en la página
     *
     * @param localizador By localizador del elemento
     * @return true si el elemento está presente
     */
    protected boolean estaElementoPresente(By localizador) {
        try {
            driver.findElement(localizador);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si un elemento está visible
     *
     * @param elemento WebElement a verificar
     * @return true si el elemento está visible
     */
    protected boolean estaElementoVisible(WebElement elemento) {
        try {
            return elemento.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Espera a que un elemento desaparezca
     *
     * @param localizador By localizador del elemento
     * @return true si el elemento desapareció
     */
    protected boolean esperarElementoDesaparezca(By localizador) {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(localizador));
        } catch (Exception e) {
            logger.warn("Elemento no desapareció en el tiempo esperado: {}", localizador);
            return false;
        }
    }

    /**
     * Desplaza la página hasta un elemento
     *
     * @param elemento WebElement al cual desplazarse
     */
    protected void desplazarseAElemento(WebElement elemento) {
        jsExecutor.executeScript("arguments[0].scrollIntoView(true);", elemento);
        logger.debug("Desplazamiento realizado hacia elemento");
    }

    /**
     * Realiza hover sobre un elemento
     *
     * @param elemento WebElement sobre el cual hacer hover
     */
    protected void hacerHover(WebElement elemento) {
        actions.moveToElement(elemento).perform();
        logger.debug("Hover realizado sobre elemento");
    }

    /**
     * Espera un tiempo específico (usar con moderación)
     *
     * @param milisegundos Tiempo a esperar en milisegundos
     */
    protected void esperarTiempo(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Espera interrumpida: {}", e.getMessage());
        }
    }

    /**
     * Ejecuta JavaScript personalizado
     *
     * @param script Script de JavaScript a ejecutar
     * @param argumentos Argumentos para el script
     * @return Resultado de la ejecución
     */
    protected Object ejecutarJavascript(String script, Object... argumentos) {
        return jsExecutor.executeScript(script, argumentos);
    }

    /**
     * Obtiene el título de la página actual
     *
     * @return Título de la página
     */
    protected String obtenerTituloPagina() {
        return driver.getTitle();
    }

    /**
     * Obtiene la URL actual
     *
     * @return URL actual
     */
    protected String obtenerUrlActual() {
        return driver.getCurrentUrl();
    }

    /**
     * Navega hacia atrás en el historial del navegador
     */
    protected void navegarAtras() {
        driver.navigate().back();
        logger.debug("Navegación hacia atrás realizada");
    }

    /**
     * Refresca la página actual
     */
    protected void refrescarPagina() {
        driver.navigate().refresh();
        logger.debug("Página refrescada");
    }

    /**
     * Espera a que la página termine de cargar completamente
     */
    protected void esperarCargaCompleta() {
        wait.until(webDriver ->
                jsExecutor.executeScript("return document.readyState").equals("complete"));
        logger.debug("Carga completa de página confirmada");
    }

    /**
     * Toma una captura de pantalla de la página actual
     *
     * @return Array de bytes con la imagen
     */
    protected byte[] tomarCapturaPantalla() {
        try {
            if (driver instanceof org.openqa.selenium.TakesScreenshot) {
                org.openqa.selenium.TakesScreenshot takesScreenshot =
                        (org.openqa.selenium.TakesScreenshot) driver;
                return takesScreenshot.getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
            }
        } catch (Exception e) {
            logger.error("Error al tomar captura de pantalla: {}", e.getMessage());
        }
        return new byte[0];
    }

    /**
     * Verifica si hay mensajes de error visibles en la página
     *
     * @return true si hay mensajes de error
     */
    protected boolean hayMensajesError() {
        // Selectores comunes para mensajes de error
        String[] selectoresError = {
                ".error", ".alert-danger", ".text-danger",
                "[class*='error']", "[class*='danger']"
        };

        for (String selector : selectoresError) {
            if (estaElementoPresente(By.cssSelector(selector))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene mensajes de error visibles en la página
     *
     * @return Texto de los mensajes de error concatenados
     */
    protected String obtenerMensajesError() {
        StringBuilder mensajes = new StringBuilder();
        String[] selectoresError = {
                ".error", ".alert-danger", ".text-danger"
        };

        for (String selector : selectoresError) {
            try {
                var elementos = driver.findElements(By.cssSelector(selector));
                for (var elemento : elementos) {
                    if (elemento.isDisplayed()) {
                        mensajes.append(elemento.getText()).append(" ");
                    }
                }
            } catch (Exception e) {
                // Ignorar errores al buscar elementos
            }
        }

        return mensajes.toString().trim();
    }

    /**
     * Verifica si la página contiene texto específico
     *
     * @param texto Texto a buscar
     * @return true si el texto está presente
     */
    protected boolean contienTexto(String texto) {
        return driver.getPageSource().contains(texto);
    }

    /**
     * Espera a que aparezca texto específico en la página
     *
     * @param texto Texto a esperar
     * @param timeout Timeout personalizado
     * @return true si el texto apareció
     */
    protected boolean esperarTexto(String texto, Duration timeout) {
        try {
            WebDriverWait waitPersonalizado = new WebDriverWait(driver, timeout);
            return waitPersonalizado.until(webDriver -> contienTexto(texto));
        } catch (Exception e) {
            logger.warn("Texto '{}' no apareció en el tiempo esperado", texto);
            return false;
        }
    }
}