package com.qa.automatizacion.utilidades;

import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Clase central de utilidades que unifica todas las funcionalidades del proyecto.
 * Actúa como fachada (Facade Pattern) para simplificar el acceso a las operaciones comunes.
 *
 * Esta clase centraliza:
 * - Operaciones con WebDriver
 * - Manejo de esperas
 * - Captura de screenshots
 * - Logging unificado
 * - Gestión de trazabilidad
 * - Utilidades de archivos
 * - Validaciones comunes
 *
 * Principios aplicados:
 * - Facade Pattern: Simplifica el acceso a subsistemas complejos
 * - Singleton Pattern: Una sola instancia para uso global
 * - Single Responsibility: Cada método tiene una responsabilidad específica
 * - DRY: Evita duplicación de código entre clases
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0
 */
public class Utileria {

    private static final Logger logger = LoggerFactory.getLogger(Utileria.class);
    private static Utileria instancia;

    // Componentes centrales
    private final PropiedadesAplicacion propiedades;
    private final HelperTrazabilidad trazabilidad;
    private final GestorBaseDatos gestorBD;

    // Configuraciones de timeouts
    private static final int TIMEOUT_DEFECTO = 10;
    private static final int TIMEOUT_CORTO = 5;
    private static final int TIMEOUT_LARGO = 30;

    // Formateadores
    private static final DateTimeFormatter FORMATO_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Constructor privado para implementar Singleton Pattern.
     */
    private Utileria() {
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();
        this.trazabilidad = HelperTrazabilidad.obtenerInstancia();
        this.gestorBD = GestorBaseDatos.obtenerInstancia();

        logger.info("Utileria inicializada correctamente");
    }

    /**
     * Obtiene la instancia única de Utileria (Singleton Pattern).
     *
     * @return instancia única de Utileria
     */
    public static synchronized Utileria obtenerInstancia() {
        if (instancia == null) {
            instancia = new Utileria();
        }
        return instancia;
    }

    // ==================== MÉTODOS DE NAVEGADOR ====================

    /**
     * Obtiene el WebDriver actual de forma segura.
     *
     * @return WebDriver actual
     * @throws RuntimeException si no hay WebDriver activo
     */
    public WebDriver obtenerNavegador() {
        WebDriver navegador = ConfiguradorNavegador.obtenerNavegador();
        if (navegador == null) {
            logger.error("WebDriver no está inicializado");
            throw new RuntimeException("WebDriver no está disponible");
        }
        return navegador;
    }

    /**
     * Obtiene WebDriverWait con timeout por defecto.
     *
     * @return WebDriverWait configurado
     */
    public WebDriverWait obtenerEsperaDefecto() {
        return new WebDriverWait(obtenerNavegador(), Duration.ofSeconds(TIMEOUT_DEFECTO));
    }

    /**
     * Obtiene WebDriverWait con timeout personalizado.
     *
     * @param segundos timeout en segundos
     * @return WebDriverWait configurado
     */
    public WebDriverWait obtenerEspera(int segundos) {
        return new WebDriverWait(obtenerNavegador(), Duration.ofSeconds(segundos));
    }

    /**
     * Navega a una URL de forma segura con validación.
     *
     * @param url URL destino
     */
    public void navegarA(String url) {
        validarParametroNoNulo(url, "URL");

        try {
            logger.info("Navegando a: {}", url);
            obtenerNavegador().get(url);
            esperarCargaPagina();

            trazabilidad.registrarNavegacion(url);
            logger.debug("Navegación exitosa a: {}", url);

        } catch (Exception e) {
            logger.error("Error navegando a {}: {}", url, e.getMessage());
            capturarScreenshotError("navegacion_error");
            throw new RuntimeException("Error en navegación: " + e.getMessage(), e);
        }
    }

    /**
     * Espera a que la página termine de cargar completamente.
     */
    public void esperarCargaPagina() {
        try {
            obtenerEspera(TIMEOUT_LARGO).until(
                    driver -> ((JavascriptExecutor) driver)
                            .executeScript("return document.readyState").equals("complete")
            );
            logger.debug("Página cargada completamente");

        } catch (Exception e) {
            logger.warn("Timeout esperando carga de página: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE ELEMENTOS ====================

    /**
     * Busca un elemento de forma segura con espera implícita.
     *
     * @param localizador localizador del elemento
     * @return WebElement encontrado
     * @throws RuntimeException si el elemento no se encuentra
     */
    public WebElement buscarElemento(By localizador) {
        validarParametroNoNulo(localizador, "Localizador");

        try {
            logger.debug("Buscando elemento: {}", localizador);
            WebElement elemento = obtenerEsperaDefecto()
                    .until(ExpectedConditions.presenceOfElementLocated(localizador));

            logger.debug("Elemento encontrado: {}", localizador);
            return elemento;

        } catch (Exception e) {
            logger.error("Elemento no encontrado: {}", localizador);
            capturarScreenshotError("elemento_no_encontrado");
            throw new RuntimeException("Elemento no encontrado: " + localizador, e);
        }
    }

    /**
     * Busca un elemento visible y clickeable.
     *
     * @param localizador localizador del elemento
     * @return WebElement visible y clickeable
     */
    public WebElement buscarElementoClickeable(By localizador) {
        validarParametroNoNulo(localizador, "Localizador");

        try {
            logger.debug("Buscando elemento clickeable: {}", localizador);
            WebElement elemento = obtenerEsperaDefecto()
                    .until(ExpectedConditions.elementToBeClickable(localizador));

            logger.debug("Elemento clickeable encontrado: {}", localizador);
            return elemento;

        } catch (Exception e) {
            logger.error("Elemento no clickeable: {}", localizador);
            capturarScreenshotError("elemento_no_clickeable");
            throw new RuntimeException("Elemento no clickeable: " + localizador, e);
        }
    }

    /**
     * Busca múltiples elementos.
     *
     * @param localizador localizador de los elementos
     * @return lista de WebElement encontrados
     */
    public List<WebElement> buscarElementos(By localizador) {
        validarParametroNoNulo(localizador, "Localizador");

        try {
            logger.debug("Buscando elementos: {}", localizador);
            obtenerEsperaDefecto()
                    .until(ExpectedConditions.presenceOfElementLocated(localizador));

            List<WebElement> elementos = obtenerNavegador().findElements(localizador);
            logger.debug("Encontrados {} elementos: {}", elementos.size(), localizador);

            return elementos;

        } catch (Exception e) {
            logger.warn("No se encontraron elementos: {}", localizador);
            return List.of(); // Lista vacía en lugar de null
        }
    }

    /**
     * Verifica si un elemento está presente en la página.
     *
     * @param localizador localizador del elemento
     * @return true si el elemento está presente, false en caso contrario
     */
    public boolean esElementoPresente(By localizador) {
        validarParametroNoNulo(localizador, "Localizador");

        try {
            obtenerNavegador().findElement(localizador);
            logger.debug("Elemento presente: {}", localizador);
            return true;

        } catch (Exception e) {
            logger.debug("Elemento no presente: {}", localizador);
            return false;
        }
    }

    /**
     * Verifica si un elemento está visible.
     *
     * @param localizador localizador del elemento
     * @return true si el elemento está visible, false en caso contrario
     */
    public boolean esElementoVisible(By localizador) {
        try {
            WebElement elemento = obtenerNavegador().findElement(localizador);
            boolean visible = elemento.isDisplayed();
            logger.debug("Elemento {} visible: {}", localizador, visible);
            return visible;

        } catch (Exception e) {
            logger.debug("Elemento no visible: {}", localizador);
            return false;
        }
    }

    // ==================== MÉTODOS DE ACCIONES ====================

    /**
     * Hace clic en un elemento de forma segura.
     *
     * @param localizador localizador del elemento
     */
    public void hacerClick(By localizador) {
        validarParametroNoNulo(localizador, "Localizador");

        try {
            logger.debug("Haciendo clic en: {}", localizador);
            WebElement elemento = buscarElementoClickeable(localizador);

            // Scroll al elemento si es necesario
            scrollHaciaElemento(elemento);

            elemento.click();
            logger.debug("Clic exitoso en: {}", localizador);

            trazabilidad.registrarAccion("Click", localizador.toString());

        } catch (Exception e) {
            logger.error("Error haciendo clic en {}: {}", localizador, e.getMessage());
            capturarScreenshotError("click_error");

            // Intento alternativo con JavaScript
            try {
                logger.info("Intentando clic con JavaScript: {}", localizador);
                hacerClickJavaScript(localizador);

            } catch (Exception jsError) {
                throw new RuntimeException("Error en clic (normal y JS): " + e.getMessage(), e);
            }
        }
    }

    /**
     * Hace clic usando JavaScript como alternativa.
     *
     * @param localizador localizador del elemento
     */
    public void hacerClickJavaScript(By localizador) {
        validarParametroNoNulo(localizador, "Localizador");

        try {
            WebElement elemento = buscarElemento(localizador);
            JavascriptExecutor js = (JavascriptExecutor) obtenerNavegador();

            js.executeScript("arguments[0].click();", elemento);
            logger.debug("Clic JavaScript exitoso en: {}", localizador);

        } catch (Exception e) {
            logger.error("Error en clic JavaScript: {}", e.getMessage());
            throw new RuntimeException("Error en clic JavaScript: " + e.getMessage(), e);
        }
    }

    /**
     * Ingresa texto en un campo de forma segura.
     *
     * @param localizador localizador del campo
     * @param texto texto a ingresar
     */
    public void ingresarTexto(By localizador, String texto) {
        validarParametroNoNulo(localizador, "Localizador");
        validarParametroNoNulo(texto, "Texto");

        try {
            logger.debug("Ingresando texto en: {}", localizador);
            WebElement elemento = buscarElemento(localizador);

            // Limpiar campo antes de escribir
            elemento.clear();
            Thread.sleep(100); // Pequeña pausa para estabilidad

            elemento.sendKeys(texto);
            logger.debug("Texto ingresado exitosamente en: {}", localizador);

            trazabilidad.registrarAccion("Ingreso de texto", localizador.toString());

        } catch (Exception e) {
            logger.error("Error ingresando texto en {}: {}", localizador, e.getMessage());
            capturarScreenshotError("ingreso_texto_error");
            throw new RuntimeException("Error ingresando texto: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el texto de un elemento.
     *
     * @param localizador localizador del elemento
     * @return texto del elemento
     */
    public String obtenerTexto(By localizador) {
        validarParametroNoNulo(localizador, "Localizador");

        try {
            WebElement elemento = buscarElemento(localizador);
            String texto = elemento.getText().trim();

            logger.debug("Texto obtenido de {}: {}", localizador, texto);
            return texto;

        } catch (Exception e) {
            logger.error("Error obteniendo texto de {}: {}", localizador, e.getMessage());
            throw new RuntimeException("Error obteniendo texto: " + e.getMessage(), e);
        }
    }

    /**
     * Realiza scroll hacia un elemento.
     *
     * @param elemento WebElement destino
     */
    public void scrollHaciaElemento(WebElement elemento) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) obtenerNavegador();
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", elemento);

            Thread.sleep(300); // Pausa para que complete el scroll
            logger.debug("Scroll realizado hacia elemento");

        } catch (Exception e) {
            logger.warn("Error en scroll: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE SCREENSHOTS ====================

    /**
     * Captura screenshot con nombre personalizado.
     *
     * @param nombreArchivo nombre del archivo (sin extensión)
     * @return ruta del archivo generado
     */
    public String capturarScreenshot(String nombreArchivo) {
        try {
            TakesScreenshot screenshot = (TakesScreenshot) obtenerNavegador();
            byte[] archivoBytes = screenshot.getScreenshotAs(OutputType.BYTES);

            String timestamp = LocalDateTime.now().format(FORMATO_TIMESTAMP);
            String nombreCompleto = nombreArchivo + "_" + timestamp + ".png";

            Path directorioScreenshots = crearDirectorioScreenshots();
            Path rutaArchivo = directorioScreenshots.resolve(nombreCompleto);

            Files.write(rutaArchivo, archivoBytes);

            logger.info("Screenshot capturado: {}", rutaArchivo);
            return rutaArchivo.toString();

        } catch (Exception e) {
            logger.error("Error capturando screenshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Captura screenshot automático en caso de error.
     *
     * @param contexto contexto del error para el nombre del archivo
     * @return ruta del archivo generado
     */
    public String capturarScreenshotError(String contexto) {
        String nombreArchivo = "ERROR_" + contexto;
        return capturarScreenshot(nombreArchivo);
    }

    /**
     * Crea el directorio de screenshots si no existe.
     *
     * @return Path del directorio de screenshots
     */
    private Path crearDirectorioScreenshots() throws IOException {
        String directorioReportes = propiedades.obtenerDirectorioReportes();
        Path directorioScreenshots = Paths.get(directorioReportes, "screenshots");

        if (!Files.exists(directorioScreenshots)) {
            Files.createDirectories(directorioScreenshots);
            logger.debug("Directorio de screenshots creado: {}", directorioScreenshots);
        }

        return directorioScreenshots;
    }

    // ==================== MÉTODOS DE TRAZABILIDAD ====================

    /**
     * Registra el inicio de un escenario.
     *
     * @param historiaUsuario ID de la historia de usuario
     * @param nombreEscenario nombre del escenario
     */
    public void iniciarEscenario(String historiaUsuario, String nombreEscenario) {
        logger.info("=== INICIANDO ESCENARIO: {} - {} ===", historiaUsuario, nombreEscenario);
        trazabilidad.iniciarEscenario(historiaUsuario, nombreEscenario);
    }

    /**
     * Registra el fin de un escenario.
     *
     * @param historiaUsuario ID de la historia de usuario
     * @param resultado resultado del escenario (PASSED, FAILED, SKIPPED)
     */
    public void finalizarEscenario(String historiaUsuario, String resultado) {
        logger.info("=== FINALIZANDO ESCENARIO: {} - {} ===", historiaUsuario, resultado);
        trazabilidad.finalizarEscenario(historiaUsuario, resultado);
    }

    /**
     * Registra un paso ejecutado.
     *
     * @param historiaUsuario ID de la historia de usuario
     * @param descripcionPaso descripción del paso
     */
    public void registrarPaso(String historiaUsuario, String descripcionPaso) {
        logger.debug("Paso ejecutado [{}]: {}", historiaUsuario, descripcionPaso);
        trazabilidad.registrarPaso(historiaUsuario, descripcionPaso);
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Valida que un parámetro no sea nulo.
     *
     * @param parametro parámetro a validar
     * @param nombre nombre del parámetro para el mensaje de error
     * @throws IllegalArgumentException si el parámetro es nulo
     */
    public void validarParametroNoNulo(Object parametro, String nombre) {
        if (parametro == null) {
            String mensaje = nombre + " no puede ser nulo";
            logger.error(mensaje);
            throw new IllegalArgumentException(mensaje);
        }
    }

    /**
     * Valida que una cadena no sea nula ni vacía.
     *
     * @param cadena cadena a validar
     * @param nombre nombre de la cadena para el mensaje de error
     * @throws IllegalArgumentException si la cadena es nula o vacía
     */
    public void validarCadenaNoVacia(String cadena, String nombre) {
        validarParametroNoNulo(cadena, nombre);

        if (cadena.trim().isEmpty()) {
            String mensaje = nombre + " no puede estar vacío";
            logger.error(mensaje);
            throw new IllegalArgumentException(mensaje);
        }
    }

    /**
     * Verifica que la página actual contiene el título esperado.
     *
     * @param tituloEsperado título esperado de la página
     * @return true si el título es correcto, false en caso contrario
     */
    public boolean verificarTituloPagina(String tituloEsperado) {
        validarCadenaNoVacia(tituloEsperado, "Título esperado");

        try {
            String tituloActual = obtenerNavegador().getTitle();
            boolean tituloEsCorrecto = tituloActual.contains(tituloEsperado);

            logger.debug("Verificación de título - Esperado: {}, Actual: {}, Correcto: {}",
                    tituloEsperado, tituloActual, tituloEsCorrecto);

            return tituloEsCorrecto;

        } catch (Exception e) {
            logger.error("Error verificando título: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica que la URL actual contiene el fragmento esperado.
     *
     * @param fragmentoUrl fragmento esperado en la URL
     * @return true si la URL contiene el fragmento, false en caso contrario
     */
    public boolean verificarUrl(String fragmentoUrl) {
        validarCadenaNoVacia(fragmentoUrl, "Fragmento URL");

        try {
            String urlActual = obtenerNavegador().getCurrentUrl();
            boolean urlEsCorrecta = urlActual.contains(fragmentoUrl);

            logger.debug("Verificación de URL - Fragmento: {}, URL actual: {}, Correcto: {}",
                    fragmentoUrl, urlActual, urlEsCorrecta);

            return urlEsCorrecta;

        } catch (Exception e) {
            logger.error("Error verificando URL: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE CONFIGURACIÓN ====================

    /**
     * Obtiene una propiedad de configuración.
     *
     * @param clave clave de la propiedad
     * @param valorDefecto valor por defecto si no existe la propiedad
     * @return valor de la propiedad
     */
    public String obtenerPropiedad(String clave, String valorDefecto) {
        return propiedades.obtenerPropiedad(clave, valorDefecto);
    }

    /**
     * Obtiene la URL base de la aplicación.
     *
     * @return URL base
     */
    public String obtenerUrlBase() {
        return propiedades.obtenerUrlBase();
    }

    /**
     * Obtiene la URL de login.
     *
     * @return URL de login
     */
    public String obtenerUrlLogin() {
        return propiedades.obtenerUrlLogin();
    }

    /**
     * Obtiene la URL de registro.
     *
     * @return URL de registro
     */
    public String obtenerUrlRegistro() {
        return propiedades.obtenerUrlRegistro();
    }

    /**
     * Obtiene la URL del dashboard.
     *
     * @return URL del dashboard
     */
    public String obtenerUrlDashboard() {
        return propiedades.obtenerUrlDashboard();
    }

    // ==================== MÉTODOS DE LIMPIEZA ====================

    /**
     * Limpia recursos y reinicia estado para nuevas pruebas.
     */
    public void limpiarRecursos() {
        try {
            logger.info("Limpiando recursos del sistema");

            // Limpiar cookies y storage del navegador
            if (obtenerNavegador() != null) {
                obtenerNavegador().manage().deleteAllCookies();

                // Limpiar localStorage y sessionStorage
                JavascriptExecutor js = (JavascriptExecutor) obtenerNavegador();
                js.executeScript("localStorage.clear();");
                js.executeScript("sessionStorage.clear();");
            }

            // Limpiar datos de trazabilidad temporal
            trazabilidad.limpiarDatosTempo();

            logger.debug("Recursos limpiados exitosamente");

        } catch (Exception e) {
            logger.warn("Error limpiando recursos: {}", e.getMessage());
        }
    }

    /**
     * Realiza una pausa controlada.
     *
     * @param milisegundos tiempo en milisegundos
     */
    public void pausa(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
            logger.debug("Pausa de {} ms ejecutada", milisegundos);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Pausa interrumpida: {}", e.getMessage());
        }
    }

    /**
     * Obtiene información del entorno para debugging.
     *
     * @return información del entorno actual
     */
    public String obtenerInformacionEntorno() {
        try {
            WebDriver navegador = obtenerNavegador();
            return String.format(
                    "URL: %s | Título: %s | Navegador: %s",
                    navegador.getCurrentUrl(),
                    navegador.getTitle(),
                    navegador.getClass().getSimpleName()
            );

        } catch (Exception e) {
            return "Error obteniendo información del entorno: " + e.getMessage();
        }
    }

    /**
     * Espera a que un elemento sea visible en la página.
     *
     * @param localizador el localizador del elemento a esperar
     * @param timeoutSegundos el tiempo máximo de espera en segundos
     */
    public void esperarElementoVisible(By localizador, int timeoutSegundos) {
        validarParametroNoNulo(localizador, "Localizador");

        try {
            logger.debug("Esperando visibilidad del elemento: {}", localizador);
            WebDriverWait espera = obtenerEspera(timeoutSegundos);
            espera.until(ExpectedConditions.visibilityOfElementLocated(localizador));
            logger.debug("Elemento visible: {}", localizador);

        } catch (Exception e) {
            logger.error("Error esperando visibilidad del elemento {}: {}", localizador, e.getMessage());
            capturarScreenshotError("esperar_elemento_visible");
            throw new RuntimeException("Error esperando visibilidad del elemento: " + localizador, e);
        }
    }
}