package com.qa.automatizacion.utilidades;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Clase centralizada con métodos reutilizables para todo el proyecto.
 * Implementa todas las funcionalidades comunes que pueden ser utilizadas
 * por cualquier Page Object o Step Definition.
 *
 * Principios aplicados:
 * - DRY: Evita duplicación de código en todo el proyecto
 * - Single Responsibility: Cada método tiene una responsabilidad específica
 * - Open/Closed: Abierto para extensión, cerrado para modificación
 * - Interface Segregation: Métodos específicos y bien definidos
 * - Dependency Inversion: Depende de abstracciones (WebDriver)
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
public final class UtileriasComunes {

    private static final Logger logger = LoggerFactory.getLogger(UtileriasComunes.class);

    // Timeouts por defecto
    private static final int TIMEOUT_DEFECTO = 10;
    private static final int TIMEOUT_EXPLICITO = 15;
    private static final int TIMEOUT_CORTO = 5;
    private static final int POLLING_INTERVAL = 500; // milisegundos

    // Constructor privado para evitar instanciación
    private UtileriasComunes() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no puede ser instanciada");
    }

    // ==================== MÉTODOS DE NAVEGACIÓN ====================

    /**
     * Navega a la URL especificada de forma segura.
     *
     * @param driver WebDriver activo
     * @param url URL de destino
     * @throws IllegalArgumentException si la URL es inválida
     */
    public static void navegarAUrl(WebDriver driver, String url) {
        validarParametros(driver, "Driver no puede ser null");
        validarTextoNoVacio(url, "URL no puede ser vacía o null");

        try {
            logger.info("Navegando a: {}", url);
            driver.get(url);
            esperarCargaPagina(driver);
            logger.debug("Navegación exitosa a: {}", url);
        } catch (Exception e) {
            logger.error("Error navegando a URL {}: {}", url, e.getMessage());
            throw new RuntimeException("Error en navegación: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza la página actual.
     *
     * @param driver WebDriver activo
     */
    public static void actualizarPagina(WebDriver driver) {
        validarParametros(driver, "Driver no puede ser null");

        try {
            logger.info("Actualizando página");
            driver.navigate().refresh();
            esperarCargaPagina(driver);
        } catch (Exception e) {
            logger.error("Error actualizando página: {}", e.getMessage());
            throw new RuntimeException("Error actualizando página: " + e.getMessage(), e);
        }
    }

    /**
     * Navega hacia atrás en el historial del navegador.
     *
     * @param driver WebDriver activo
     */
    public static void navegarAtras(WebDriver driver) {
        validarParametros(driver, "Driver no puede ser null");

        try {
            logger.info("Navegando hacia atrás");
            driver.navigate().back();
            esperarCargaPagina(driver);
        } catch (Exception e) {
            logger.error("Error navegando hacia atrás: {}", e.getMessage());
            throw new RuntimeException("Error navegando hacia atrás: " + e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS DE BÚSQUEDA DE ELEMENTOS ====================

    /**
     * Busca un elemento con espera explícita y manejo robusto de errores.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del elemento
     * @param timeoutSegundos timeout en segundos (opcional, usa default si es 0)
     * @return Optional con el elemento si se encuentra
     */
    public static Optional<WebElement> buscarElemento(WebDriver driver, By localizador, int... timeoutSegundos) {
        validarParametros(driver, "Driver no puede ser null");
        validarParametros(localizador, "Localizador no puede ser null");

        int timeout = timeoutSegundos.length > 0 ? timeoutSegundos[0] : TIMEOUT_DEFECTO;

        try {
            logger.debug("Buscando elemento: {} con timeout: {}s", localizador, timeout);

            WebDriverWait espera = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            WebElement elemento = espera.until(ExpectedConditions.presenceOfElementLocated(localizador));

            logger.debug("Elemento encontrado: {}", localizador);
            return Optional.of(elemento);

        } catch (TimeoutException e) {
            logger.debug("Elemento no encontrado en {}s: {}", timeout, localizador);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error inesperado buscando elemento {}: {}", localizador, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Busca múltiples elementos con espera.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador de los elementos
     * @param timeoutSegundos timeout en segundos
     * @return Lista de elementos encontrados (puede estar vacía)
     */
    public static List<WebElement> buscarElementos(WebDriver driver, By localizador, int... timeoutSegundos) {
        validarParametros(driver, "Driver no puede ser null");
        validarParametros(localizador, "Localizador no puede ser null");

        int timeout = timeoutSegundos.length > 0 ? timeoutSegundos[0] : TIMEOUT_DEFECTO;

        try {
            logger.debug("Buscando elementos: {} con timeout: {}s", localizador, timeout);

            WebDriverWait espera = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            List<WebElement> elementos = espera.until(ExpectedConditions.presenceOfAllElementsLocatedBy(localizador));

            logger.debug("Encontrados {} elementos: {}", elementos.size(), localizador);
            return elementos;

        } catch (TimeoutException e) {
            logger.debug("No se encontraron elementos en {}s: {}", timeout, localizador);
            return List.of(); // Lista vacía
        } catch (Exception e) {
            logger.error("Error buscando elementos {}: {}", localizador, e.getMessage());
            return List.of();
        }
    }

    /**
     * Busca un elemento clickeable con espera fluida.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del elemento
     * @param timeoutSegundos timeout en segundos
     * @return Optional con el elemento clickeable
     */
    public static Optional<WebElement> buscarElementoClickeable(WebDriver driver, By localizador, int... timeoutSegundos) {
        validarParametros(driver, "Driver no puede ser null");
        validarParametros(localizador, "Localizador no puede ser null");

        int timeout = timeoutSegundos.length > 0 ? timeoutSegundos[0] : TIMEOUT_DEFECTO;

        try {
            logger.debug("Buscando elemento clickeable: {}", localizador);

            FluentWait<WebDriver> espera = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeout))
                    .pollingEvery(Duration.ofMillis(POLLING_INTERVAL))  // Cambiar ofMilliseconds por ofMillis
                    .ignoring(NoSuchElementException.class)
                    .ignoring(ElementClickInterceptedException.class);

            WebElement elemento = espera.until(ExpectedConditions.elementToBeClickable(localizador));

            logger.debug("Elemento clickeable encontrado: {}", localizador);
            return Optional.of(elemento);

        } catch (TimeoutException e) {
            logger.debug("Elemento no clickeable en {}s: {}", timeout, localizador);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error buscando elemento clickeable {}: {}", localizador, e.getMessage());
            return Optional.empty();
        }
    }

    // ==================== MÉTODOS DE VERIFICACIÓN ====================

    /**
     * Verifica si un elemento está visible en la página.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del elemento
     * @param timeoutSegundos timeout en segundos
     * @return true si el elemento está visible
     */
    public static boolean esElementoVisible(WebDriver driver, By localizador, int... timeoutSegundos) {
        Optional<WebElement> elemento = buscarElemento(driver, localizador, timeoutSegundos);

        if (elemento.isPresent()) {
            try {
                boolean visible = elemento.get().isDisplayed();
                logger.debug("Elemento {} está visible: {}", localizador, visible);
                return visible;
            } catch (Exception e) {
                logger.debug("Error verificando visibilidad de {}: {}", localizador, e.getMessage());
                return false;
            }
        }

        logger.debug("Elemento no encontrado para verificar visibilidad: {}", localizador);
        return false;
    }

    /**
     * Verifica si un elemento está habilitado.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del elemento
     * @return true si el elemento está habilitado
     */
    public static boolean esElementoHabilitado(WebDriver driver, By localizador) {
        Optional<WebElement> elemento = buscarElemento(driver, localizador);

        if (elemento.isPresent()) {
            try {
                boolean habilitado = elemento.get().isEnabled();
                logger.debug("Elemento {} está habilitado: {}", localizador, habilitado);
                return habilitado;
            } catch (Exception e) {
                logger.debug("Error verificando si está habilitado {}: {}", localizador, e.getMessage());
                return false;
            }
        }

        return false;
    }

    /**
     * Verifica si un elemento está seleccionado (checkbox, radio button).
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del elemento
     * @return true si el elemento está seleccionado
     */
    public static boolean esElementoSeleccionado(WebDriver driver, By localizador) {
        Optional<WebElement> elemento = buscarElemento(driver, localizador);

        if (elemento.isPresent()) {
            try {
                boolean seleccionado = elemento.get().isSelected();
                logger.debug("Elemento {} está seleccionado: {}", localizador, seleccionado);
                return seleccionado;
            } catch (Exception e) {
                logger.debug("Error verificando selección de {}: {}", localizador, e.getMessage());
                return false;
            }
        }

        return false;
    }

    // ==================== MÉTODOS DE INTERACCIÓN CON ELEMENTOS ====================

    /**
     * Hace clic en un elemento de forma segura con reintentos.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del elemento
     * @param reintentos número de reintentos en caso de fallo
     * @return true si el clic fue exitoso
     */
    public static boolean hacerClicSeguro(WebDriver driver, By localizador, int... reintentos) {
        int maxReintentos = reintentos.length > 0 ? reintentos[0] : 3;

        for (int intento = 1; intento <= maxReintentos; intento++) {
            try {
                Optional<WebElement> elemento = buscarElementoClickeable(driver, localizador);

                if (elemento.isPresent()) {
                    // Scroll al elemento si es necesario
                    scrollHastaElemento(driver, elemento.get());

                    elemento.get().click();
                    logger.debug("Clic exitoso en elemento: {} (intento {})", localizador, intento);
                    return true;
                }

            } catch (ElementClickInterceptedException e) {
                logger.debug("Elemento interceptado en intento {}: {}", intento, localizador);
                if (intento < maxReintentos) {
                    esperarSegundos(1);
                }
            } catch (Exception e) {
                logger.debug("Error en clic intento {}: {} - {}", intento, localizador, e.getMessage());
                if (intento < maxReintentos) {
                    esperarSegundos(1);
                }
            }
        }

        logger.error("No se pudo hacer clic en elemento después de {} intentos: {}", maxReintentos, localizador);
        return false;
    }

    /**
     * Ingresa texto en un campo de forma segura.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del campo
     * @param texto texto a ingresar
     * @param limpiarAntes true para limpiar el campo antes de escribir
     * @return true si el texto se ingresó correctamente
     */
    public static boolean ingresarTextoSeguro(WebDriver driver, By localizador, String texto, boolean... limpiarAntes) {
        validarTextoNoVacio(texto, "Texto no puede ser vacío o null");

        try {
            Optional<WebElement> elemento = buscarElemento(driver, localizador);

            if (elemento.isPresent()) {
                WebElement campo = elemento.get();

                // Scroll al elemento
                scrollHastaElemento(driver, campo);

                // Limpiar campo si se solicita
                boolean limpiar = limpiarAntes.length > 0 ? limpiarAntes[0] : true;
                if (limpiar) {
                    campo.clear();
                    // Verificar que se limpió
                    if (!campo.getAttribute("value").isEmpty()) {
                        campo.sendKeys(Keys.CONTROL + "a");
                        campo.sendKeys(Keys.DELETE);
                    }
                }

                // Ingresar texto
                campo.sendKeys(texto);

                // Verificar que el texto se ingresó correctamente
                String valorActual = campo.getAttribute("value");
                boolean exitoso = texto.equals(valorActual);

                logger.debug("Texto ingresado en {}: '{}' (exitoso: {})", localizador, texto, exitoso);
                return exitoso;

            } else {
                logger.error("No se encontró el campo para ingresar texto: {}", localizador);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error ingresando texto en {}: {}", localizador, e.getMessage());
            return false;
        }
    }

    /**
     * Selecciona una opción de un dropdown por texto visible.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del select
     * @param textoOpcion texto de la opción a seleccionar
     * @return true si la selección fue exitosa
     */
    public static boolean seleccionarOpcionPorTexto(WebDriver driver, By localizador, String textoOpcion) {
        validarTextoNoVacio(textoOpcion, "Texto de opción no puede ser vacío");

        try {
            Optional<WebElement> elemento = buscarElemento(driver, localizador);

            if (elemento.isPresent()) {
                Select select = new Select(elemento.get());
                select.selectByVisibleText(textoOpcion);

                // Verificar selección
                String textoSeleccionado = select.getFirstSelectedOption().getText();
                boolean exitoso = textoOpcion.equals(textoSeleccionado);

                logger.debug("Opción seleccionada en {}: '{}' (exitoso: {})", localizador, textoOpcion, exitoso);
                return exitoso;

            } else {
                logger.error("No se encontró el select: {}", localizador);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error seleccionando opción '{}' en {}: {}", textoOpcion, localizador, e.getMessage());
            return false;
        }
    }

    /**
     * Selecciona una opción de un dropdown por valor.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del select
     * @param valor valor de la opción a seleccionar
     * @return true si la selección fue exitosa
     */
    public static boolean seleccionarOpcionPorValor(WebDriver driver, By localizador, String valor) {
        validarTextoNoVacio(valor, "Valor no puede ser vacío");

        try {
            Optional<WebElement> elemento = buscarElemento(driver, localizador);

            if (elemento.isPresent()) {
                Select select = new Select(elemento.get());
                select.selectByValue(valor);

                // Verificar selección
                String valorSeleccionado = select.getFirstSelectedOption().getAttribute("value");
                boolean exitoso = valor.equals(valorSeleccionado);

                logger.debug("Opción por valor seleccionada en {}: '{}' (exitoso: {})", localizador, valor, exitoso);
                return exitoso;

            } else {
                logger.error("No se encontró el select: {}", localizador);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error seleccionando por valor '{}' en {}: {}", valor, localizador, e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE OBTENCIÓN DE DATOS ====================

    /**
     * Obtiene el texto de un elemento.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del elemento
     * @return Optional con el texto del elemento
     */
    public static Optional<String> obtenerTextoElemento(WebDriver driver, By localizador) {
        try {
            Optional<WebElement> elemento = buscarElemento(driver, localizador);

            if (elemento.isPresent()) {
                String texto = elemento.get().getText().trim();
                logger.debug("Texto obtenido de {}: '{}'", localizador, texto);
                return Optional.of(texto);
            } else {
                logger.debug("No se encontró elemento para obtener texto: {}", localizador);
                return Optional.empty();
            }

        } catch (Exception e) {
            logger.error("Error obteniendo texto de {}: {}", localizador, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Obtiene el valor de un atributo de un elemento.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del elemento
     * @param nombreAtributo nombre del atributo
     * @return Optional con el valor del atributo
     */
    public static Optional<String> obtenerAtributoElemento(WebDriver driver, By localizador, String nombreAtributo) {
        validarTextoNoVacio(nombreAtributo, "Nombre de atributo no puede ser vacío");

        try {
            Optional<WebElement> elemento = buscarElemento(driver, localizador);

            if (elemento.isPresent()) {
                String valor = elemento.get().getAttribute(nombreAtributo);
                logger.debug("Atributo '{}' de {}: '{}'", nombreAtributo, localizador, valor);
                return Optional.ofNullable(valor);
            } else {
                logger.debug("No se encontró elemento para obtener atributo: {}", localizador);
                return Optional.empty();
            }

        } catch (Exception e) {
            logger.error("Error obteniendo atributo '{}' de {}: {}", nombreAtributo, localizador, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Obtiene el valor de un campo de entrada.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del campo
     * @return Optional con el valor del campo
     */
    public static Optional<String> obtenerValorCampo(WebDriver driver, By localizador) {
        return obtenerAtributoElemento(driver, localizador, "value");
    }

    // ==================== MÉTODOS DE ESPERA ====================

    /**
     * Espera que un elemento sea visible.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del elemento
     * @param timeoutSegundos timeout en segundos
     * @return true si el elemento se volvió visible
     */
    public static boolean esperarElementoVisible(WebDriver driver, By localizador, int timeoutSegundos) {
        try {
            WebDriverWait espera = new WebDriverWait(driver, Duration.ofSeconds(timeoutSegundos));
            espera.until(ExpectedConditions.visibilityOfElementLocated(localizador));
            logger.debug("Elemento visible después de espera: {}", localizador);
            return true;
        } catch (TimeoutException e) {
            logger.debug("Elemento no se volvió visible en {}s: {}", timeoutSegundos, localizador);
            return false;
        }
    }

    /**
     * Espera que un elemento no sea visible.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del elemento
     * @param timeoutSegundos timeout en segundos
     * @return true si el elemento dejó de ser visible
     */
    public static boolean esperarElementoNoVisible(WebDriver driver, By localizador, int timeoutSegundos) {
        try {
            WebDriverWait espera = new WebDriverWait(driver, Duration.ofSeconds(timeoutSegundos));
            espera.until(ExpectedConditions.invisibilityOfElementLocated(localizador));
            logger.debug("Elemento no visible después de espera: {}", localizador);
            return true;
        } catch (TimeoutException e) {
            logger.debug("Elemento siguió visible después de {}s: {}", timeoutSegundos, localizador);
            return false;
        }
    }

    /**
     * Espera que aparezca texto específico en un elemento.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del elemento
     * @param texto texto esperado
     * @param timeoutSegundos timeout en segundos
     * @return true si el texto apareció
     */
    public static boolean esperarTextoEnElemento(WebDriver driver, By localizador, String texto, int timeoutSegundos) {
        validarTextoNoVacio(texto, "Texto esperado no puede ser vacío");

        try {
            WebDriverWait espera = new WebDriverWait(driver, Duration.ofSeconds(timeoutSegundos));
            espera.until(ExpectedConditions.textToBePresentInElementLocated(localizador, texto));
            logger.debug("Texto '{}' encontrado en elemento {}", texto, localizador);
            return true;
        } catch (TimeoutException e) {
            logger.debug("Texto '{}' no encontrado en {}s en elemento: {}", texto, timeoutSegundos, localizador);
            return false;
        }
    }

    /**
     * Espera una cantidad específica de segundos.
     *
     * @param segundos segundos a esperar
     */
    public static void esperarSegundos(int segundos) {
        try {
            logger.debug("Esperando {} segundos", segundos);
            Thread.sleep(segundos * 1000L);
        } catch (InterruptedException e) {
            logger.warn("Espera interrumpida: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Espera que la página termine de cargar completamente.
     *
     * @param driver WebDriver activo
     */
    public static void esperarCargaPagina(WebDriver driver) {
        try {
            WebDriverWait espera = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_EXPLICITO));
            espera.until(webDriver ->
                    ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
            logger.debug("Página cargada completamente");
        } catch (TimeoutException e) {
            logger.warn("Timeout esperando carga completa de página");
        }
    }

    // ==================== MÉTODOS DE ACCIONES AVANZADAS ====================

    /**
     * Realiza scroll hasta un elemento específico.
     *
     * @param driver WebDriver activo
     * @param elemento WebElement al que hacer scroll
     */
    public static void scrollHastaElemento(WebDriver driver, WebElement elemento) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", elemento);
            logger.debug("Scroll realizado hasta el elemento");
            // Pequeña pausa para que el scroll se complete
            Thread.sleep(300);
        } catch (Exception e) {
            logger.debug("Error realizando scroll: {}", e.getMessage());
        }
    }

    /**
     * Realiza doble clic en un elemento.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del elemento
     * @return true si el doble clic fue exitoso
     */
    public static boolean hacerDobleClicSeguro(WebDriver driver, By localizador) {
        try {
            Optional<WebElement> elemento = buscarElementoClickeable(driver, localizador);

            if (elemento.isPresent()) {
                Actions actions = new Actions(driver);
                actions.doubleClick(elemento.get()).perform();
                logger.debug("Doble clic exitoso en: {}", localizador);
                return true;
            } else {
                logger.error("No se encontró elemento para doble clic: {}", localizador);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error en doble clic en {}: {}", localizador, e.getMessage());
            return false;
        }
    }

    /**
     * Realiza clic derecho en un elemento.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del elemento
     * @return true si el clic derecho fue exitoso
     */
    public static boolean hacerClicDerechoSeguro(WebDriver driver, By localizador) {
        try {
            Optional<WebElement> elemento = buscarElementoClickeable(driver, localizador);

            if (elemento.isPresent()) {
                Actions actions = new Actions(driver);
                actions.contextClick(elemento.get()).perform();
                logger.debug("Clic derecho exitoso en: {}", localizador);
                return true;
            } else {
                logger.error("No se encontró elemento para clic derecho: {}", localizador);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error en clic derecho en {}: {}", localizador, e.getMessage());
            return false;
        }
    }

    /**
     * Arrastra un elemento hacia otro (drag and drop).
     *
     * @param driver WebDriver activo
     * @param localizadorOrigen By localizador del elemento origen
     * @param localizadorDestino By localizador del elemento destino
     * @return true si el drag and drop fue exitoso
     */
    public static boolean arrastrarYSoltar(WebDriver driver, By localizadorOrigen, By localizadorDestino) {
        try {
            Optional<WebElement> elementoOrigen = buscarElementoClickeable(driver, localizadorOrigen);
            Optional<WebElement> elementoDestino = buscarElementoClickeable(driver, localizadorDestino);

            if (elementoOrigen.isPresent() && elementoDestino.isPresent()) {
                Actions actions = new Actions(driver);
                actions.dragAndDrop(elementoOrigen.get(), elementoDestino.get()).perform();
                logger.debug("Drag and drop exitoso desde {} hacia {}", localizadorOrigen, localizadorDestino);
                return true;
            } else {
                logger.error("No se encontraron elementos para drag and drop: {} -> {}",
                        localizadorOrigen, localizadorDestino);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error en drag and drop {} -> {}: {}",
                    localizadorOrigen, localizadorDestino, e.getMessage());
            return false;
        }
    }

    /**
     * Simula presionar una tecla específica.
     *
     * @param driver WebDriver activo
     * @param tecla Keys tecla a presionar
     */
    public static void presionarTecla(WebDriver driver, Keys tecla) {
        try {
            Actions actions = new Actions(driver);
            actions.sendKeys(tecla).perform();
            logger.debug("Tecla presionada: {}", tecla);
        } catch (Exception e) {
            logger.error("Error presionando tecla {}: {}", tecla, e.getMessage());
        }
    }

    /**
     * Simula combinación de teclas (ej: Ctrl+A).
     *
     * @param driver WebDriver activo
     * @param teclas combinación de teclas
     */
    public static void presionarCombinacionTeclas(WebDriver driver, Keys... teclas) {
        try {
            Actions actions = new Actions(driver);
            actions.sendKeys(teclas).perform();
            logger.debug("Combinación de teclas presionada: {}", java.util.Arrays.toString(teclas));
        } catch (Exception e) {
            logger.error("Error presionando combinación de teclas: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE MANEJO DE VENTANAS ====================

    /**
     * Cambia a una ventana/pestaña específica por su título.
     *
     * @param driver WebDriver activo
     * @param tituloVentana título parcial o completo de la ventana
     * @return true si se cambió exitosamente
     */
    public static boolean cambiarAVentanaPorTitulo(WebDriver driver, String tituloVentana) {
        validarTextoNoVacio(tituloVentana, "Título de ventana no puede ser vacío");

        try {
            String ventanaOriginal = driver.getWindowHandle();

            for (String handle : driver.getWindowHandles()) {
                driver.switchTo().window(handle);
                if (driver.getTitle().contains(tituloVentana)) {
                    logger.debug("Cambiado a ventana: {}", tituloVentana);
                    return true;
                }
            }

            // Si no se encuentra, regresar a la ventana original
            driver.switchTo().window(ventanaOriginal);
            logger.error("No se encontró ventana con título: {}", tituloVentana);
            return false;

        } catch (Exception e) {
            logger.error("Error cambiando a ventana {}: {}", tituloVentana, e.getMessage());
            return false;
        }
    }

    /**
     * Cierra todas las ventanas excepto la principal.
     *
     * @param driver WebDriver activo
     */
    public static void cerrarVentanasSecundarias(WebDriver driver) {
        try {
            String ventanaPrincipal = driver.getWindowHandles().iterator().next();

            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(ventanaPrincipal)) {
                    driver.switchTo().window(handle);
                    driver.close();
                }
            }

            driver.switchTo().window(ventanaPrincipal);
            logger.debug("Ventanas secundarias cerradas");

        } catch (Exception e) {
            logger.error("Error cerrando ventanas secundarias: {}", e.getMessage());
        }
    }

    /**
     * Obtiene el número total de ventanas/pestañas abiertas.
     *
     * @param driver WebDriver activo
     * @return número de ventanas abiertas
     */
    public static int obtenerNumeroVentanas(WebDriver driver) {
        try {
            int numeroVentanas = driver.getWindowHandles().size();
            logger.debug("Número de ventanas abiertas: {}", numeroVentanas);
            return numeroVentanas;
        } catch (Exception e) {
            logger.error("Error obteniendo número de ventanas: {}", e.getMessage());
            return 0;
        }
    }

    // ==================== MÉTODOS DE MANEJO DE ALERTAS ====================

    /**
     * Acepta una alerta JavaScript si está presente.
     *
     * @param driver WebDriver activo
     * @param timeoutSegundos timeout para esperar la alerta
     * @return true si se aceptó la alerta
     */
    public static boolean aceptarAlerta(WebDriver driver, int timeoutSegundos) {
        try {
            WebDriverWait espera = new WebDriverWait(driver, Duration.ofSeconds(timeoutSegundos));
            Alert alerta = espera.until(ExpectedConditions.alertIsPresent());

            String textoAlerta = alerta.getText();
            alerta.accept();

            logger.debug("Alerta aceptada: '{}'", textoAlerta);
            return true;

        } catch (TimeoutException e) {
            logger.debug("No se encontró alerta en {}s", timeoutSegundos);
            return false;
        } catch (Exception e) {
            logger.error("Error aceptando alerta: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Rechaza una alerta JavaScript si está presente.
     *
     * @param driver WebDriver activo
     * @param timeoutSegundos timeout para esperar la alerta
     * @return true si se rechazó la alerta
     */
    public static boolean rechazarAlerta(WebDriver driver, int timeoutSegundos) {
        try {
            WebDriverWait espera = new WebDriverWait(driver, Duration.ofSeconds(timeoutSegundos));
            Alert alerta = espera.until(ExpectedConditions.alertIsPresent());

            String textoAlerta = alerta.getText();
            alerta.dismiss();

            logger.debug("Alerta rechazada: '{}'", textoAlerta);
            return true;

        } catch (TimeoutException e) {
            logger.debug("No se encontró alerta en {}s", timeoutSegundos);
            return false;
        } catch (Exception e) {
            logger.error("Error rechazando alerta: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el texto de una alerta si está presente.
     *
     * @param driver WebDriver activo
     * @param timeoutSegundos timeout para esperar la alerta
     * @return Optional con el texto de la alerta
     */
    public static Optional<String> obtenerTextoAlerta(WebDriver driver, int timeoutSegundos) {
        try {
            WebDriverWait espera = new WebDriverWait(driver, Duration.ofSeconds(timeoutSegundos));
            Alert alerta = espera.until(ExpectedConditions.alertIsPresent());

            String texto = alerta.getText();
            logger.debug("Texto de alerta obtenido: '{}'", texto);
            return Optional.of(texto);

        } catch (TimeoutException e) {
            logger.debug("No se encontró alerta en {}s", timeoutSegundos);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error obteniendo texto de alerta: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // ==================== MÉTODOS DE MANEJO DE FRAMES ====================

    /**
     * Cambia al frame por su índice.
     *
     * @param driver WebDriver activo
     * @param indice índice del frame (0-based)
     * @return true si se cambió exitosamente
     */
    public static boolean cambiarAFramePorIndice(WebDriver driver, int indice) {
        try {
            driver.switchTo().frame(indice);
            logger.debug("Cambiado al frame índice: {}", indice);
            return true;
        } catch (Exception e) {
            logger.error("Error cambiando al frame índice {}: {}", indice, e.getMessage());
            return false;
        }
    }

    /**
     * Cambia al frame por su nombre o ID.
     *
     * @param driver WebDriver activo
     * @param nombreOId nombre o ID del frame
     * @return true si se cambió exitosamente
     */
    public static boolean cambiarAFramePorNombre(WebDriver driver, String nombreOId) {
        validarTextoNoVacio(nombreOId, "Nombre o ID del frame no puede ser vacío");

        try {
            driver.switchTo().frame(nombreOId);
            logger.debug("Cambiado al frame: {}", nombreOId);
            return true;
        } catch (Exception e) {
            logger.error("Error cambiando al frame {}: {}", nombreOId, e.getMessage());
            return false;
        }
    }

    /**
     * Cambia al frame por elemento WebElement.
     *
     * @param driver WebDriver activo
     * @param localizadorFrame By localizador del frame
     * @return true si se cambió exitosamente
     */
    public static boolean cambiarAFramePorElemento(WebDriver driver, By localizadorFrame) {
        try {
            Optional<WebElement> frame = buscarElemento(driver, localizadorFrame);

            if (frame.isPresent()) {
                driver.switchTo().frame(frame.get());
                logger.debug("Cambiado al frame: {}", localizadorFrame);
                return true;
            } else {
                logger.error("No se encontró frame: {}", localizadorFrame);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error cambiando al frame {}: {}", localizadorFrame, e.getMessage());
            return false;
        }
    }

    /**
     * Regresa al contenido principal (sale de todos los frames).
     *
     * @param driver WebDriver activo
     */
    public static void regresarAContenidoPrincipal(WebDriver driver) {
        try {
            driver.switchTo().defaultContent();
            logger.debug("Regresado al contenido principal");
        } catch (Exception e) {
            logger.error("Error regresando al contenido principal: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE CAPTURA DE PANTALLA ====================

    /**
     * Toma una captura de pantalla completa.
     *
     * @param driver WebDriver activo
     * @return byte array con la imagen o null si hubo error
     */
    public static byte[] tomarCapturaPantalla(WebDriver driver) {
        try {
            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                logger.debug("Captura de pantalla tomada exitosamente");
                return screenshot;
            } else {
                logger.warn("El driver no soporta capturas de pantalla");
                return null;
            }
        } catch (Exception e) {
            logger.error("Error tomando captura de pantalla: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Toma captura de pantalla de un elemento específico.
     *
     * @param driver WebDriver activo
     * @param localizador By localizador del elemento
     * @return byte array con la imagen del elemento o null si hubo error
     */
    public static byte[] tomarCapturaElemento(WebDriver driver, By localizador) {
        try {
            Optional<WebElement> elemento = buscarElemento(driver, localizador);

            if (elemento.isPresent() && elemento.get() instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) elemento.get()).getScreenshotAs(OutputType.BYTES);
                logger.debug("Captura de elemento tomada: {}", localizador);
                return screenshot;
            } else {
                logger.debug("No se pudo tomar captura del elemento: {}", localizador);
                return null;
            }

        } catch (Exception e) {
            logger.error("Error tomando captura del elemento {}: {}", localizador, e.getMessage());
            return null;
        }
    }

    // ==================== MÉTODOS DE VALIDACIÓN Y UTILIDADES ====================

    /**
     * Valida que un parámetro no sea null.
     *
     * @param objeto objeto a validar
     * @param mensaje mensaje de error si es null
     * @throws IllegalArgumentException si el objeto es null
     */
    private static void validarParametros(Object objeto, String mensaje) {
        if (objeto == null) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    /**
     * Valida que un texto no sea null o vacío.
     *
     * @param texto texto a validar
     * @param mensaje mensaje de error
     * @throws IllegalArgumentException si el texto es inválido
     */
    private static void validarTextoNoVacio(String texto, String mensaje) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    /**
     * Obtiene información de diagnóstico del navegador para debugging.
     *
     * @param driver WebDriver activo
     * @return String con información de diagnóstico
     */
    public static String obtenerInformacionDiagnostico(WebDriver driver) {
        StringBuilder diagnostico = new StringBuilder();

        try {
            diagnostico.append("=== INFORMACIÓN DE DIAGNÓSTICO ===\n");
            diagnostico.append("Timestamp: ").append(LocalDateTime.now()).append("\n");
            diagnostico.append("URL actual: ").append(driver.getCurrentUrl()).append("\n");
            diagnostico.append("Título página: ").append(driver.getTitle()).append("\n");
            diagnostico.append("Tamaño ventana: ").append(driver.manage().window().getSize()).append("\n");
            diagnostico.append("User Agent: ").append(
                    ((JavascriptExecutor) driver).executeScript("return navigator.userAgent;")).append("\n");
            diagnostico.append("Número de ventanas: ").append(driver.getWindowHandles().size()).append("\n");
            diagnostico.append("===============================\n");

        } catch (Exception e) {
            diagnostico.append("Error obteniendo diagnóstico: ").append(e.getMessage()).append("\n");
        }

        return diagnostico.toString();
    }

    /**
     * Resalta un elemento visualmente para debugging (añade borde rojo).
     *
     * @param driver WebDriver activo
     * @param elemento WebElement a resaltar
     */
    public static void resaltarElemento(WebDriver driver, WebElement elemento) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].style.border='3px solid red'", elemento);
            logger.debug("Elemento resaltado para debugging");

            // Esperar un momento para que sea visible
            Thread.sleep(500);

            // Quitar el resaltado
            js.executeScript("arguments[0].style.border=''", elemento);

        } catch (Exception e) {
            logger.debug("Error resaltando elemento: {}", e.getMessage());
        }
    }

    /**
     * Ejecuta JavaScript personalizado de forma segura.
     *
     * @param driver WebDriver activo
     * @param script script de JavaScript a ejecutar
     * @param argumentos argumentos opcionales para el script
     * @return resultado de la ejecución o null si hubo error
     */
    public static Object ejecutarJavaScript(WebDriver driver, String script, Object... argumentos) {
        validarTextoNoVacio(script, "Script JavaScript no puede ser vacío");

        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object resultado = js.executeScript(script, argumentos);
            logger.debug("JavaScript ejecutado exitosamente");
            return resultado;
        } catch (Exception e) {
            logger.error("Error ejecutando JavaScript: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si la página actual contiene texto específico.
     *
     * @param driver WebDriver activo
     * @param texto texto a buscar en la página
     * @return true si el texto está presente
     */
    public static boolean paginaContienTexto(WebDriver driver, String texto) {
        validarTextoNoVacio(texto, "Texto a buscar no puede ser vacío");

        try {
            String contenidoPagina = driver.getPageSource().toLowerCase();
            boolean contiene = contenidoPagina.contains(texto.toLowerCase());
            logger.debug("Página contiene texto '{}': {}", texto, contiene);
            return contiene;
        } catch (Exception e) {
            logger.error("Error verificando texto en página: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el título actual de la página.
     *
     * @param driver WebDriver activo
     * @return título de la página o cadena vacía si hay error
     */
    public static String obtenerTituloPagina(WebDriver driver) {
        try {
            String titulo = driver.getTitle();
            logger.debug("Título de página: '{}'", titulo);
            return titulo != null ? titulo : "";
        } catch (Exception e) {
            logger.error("Error obteniendo título de página: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Obtiene la URL actual de la página.
     *
     * @param driver WebDriver activo
     * @return URL actual o cadena vacía si hay error
     */
    public static String obtenerUrlActual(WebDriver driver) {
        try {
            String url = driver.getCurrentUrl();
            logger.debug("URL actual: '{}'", url);
            return url != null ? url : "";
        } catch (Exception e) {
            logger.error("Error obteniendo URL actual: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Registra una acción en el log para trazabilidad.
     *
     * @param accion descripción de la acción realizada
     * @param detalles detalles adicionales de la acción
     */
    public static void registrarAccionTrazabilidad(String accion, String... detalles) {
        String detalle = detalles.length > 0 ? String.join(" | ", detalles) : "";
        logger.info("ACCION: {} {}", accion, detalle);
    }

    // ==================== CONSTANTES Y MÉTODOS DE CONFIGURACIÓN ====================

    /**
     * Obtiene el timeout por defecto configurado.
     *
     * @return timeout en segundos
     */
    public static int obtenerTimeoutDefecto() {
        return TIMEOUT_DEFECTO;
    }

    /**
     * Obtiene el timeout explícito configurado.
     *
     * @return timeout explícito en segundos
     */
    public static int obtenerTimeoutExplicito() {
        return TIMEOUT_EXPLICITO;
    }

    /**
     * Obtiene el timeout corto configurado.
     *
     * @return timeout corto en segundos
     */
    public static int obtenerTimeoutCorto() {
        return TIMEOUT_CORTO;
    }
}