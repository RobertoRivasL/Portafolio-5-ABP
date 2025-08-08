package com.qa.automatizacion.paginas;

import com.qa.automatizacion.utilidades.Utileria;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Clase base para todos los Page Objects del proyecto.
 * Proporciona funcionalidades comunes y estandariza el comportamiento.
 *
 * Esta clase implementa:
 * - Patrón Template Method: Define la estructura común de las páginas
 * - Principio DRY: Evita duplicación de código entre páginas
 * - Encapsulación: Oculta la complejidad de Selenium
 * - Single Responsibility: Se enfoca en operaciones comunes de páginas
 *
 * Todas las clases de página deben heredar de esta clase base para:
 * - Mantener consistencia en el comportamiento
 * - Reutilizar funcionalidades comunes
 * - Facilitar el mantenimiento
 * - Asegurar el logging unificado
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0
 */
public abstract class PaginaBase {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final Utileria utileria;

    // Localizadores comunes a todas las páginas
    protected static final By LOADER = By.cssSelector(".loader, .spinner, [data-testid='loading']");
    protected static final By MENSAJE_ERROR = By.cssSelector(".alert-danger, .error, [data-testid='error']");
    protected static final By MENSAJE_EXITO = By.cssSelector(".alert-success, .success, [data-testid='success']");
    protected static final By MENSAJE_ADVERTENCIA = By.cssSelector(".alert-warning, .warning, [data-testid='warning']");
    protected static final By MENSAJE_INFO = By.cssSelector(".alert-info, .info, [data-testid='info']");

    // Timeouts específicos para páginas
    protected static final int TIMEOUT_CARGA_PAGINA = 30;
    protected static final int TIMEOUT_ELEMENTO = 10;
    protected static final int TIMEOUT_CORTO = 5;

    /**
     * Constructor base que inicializa los componentes comunes.
     * Todas las páginas heredadas deben llamar a super() en su constructor.
     */
    protected PaginaBase() {
        this.utileria = Utileria.obtenerInstancia();

        // Inicializar PageFactory para @FindBy annotations
        WebDriver navegador = utileria.obtenerNavegador();
        PageFactory.initElements(navegador, this);

        logger.debug("Página {} inicializada", this.getClass().getSimpleName());
    }

    // ==================== MÉTODOS ABSTRACTOS ====================

    /**
     * Verifica si la página está completamente cargada.
     * Cada página debe implementar su propia lógica de verificación.
     *
     * @return true si la página está cargada, false en caso contrario
     */
    public abstract boolean esPaginaCargada();

    /**
     * Obtiene el título esperado de la página.
     * Cada página debe definir su título específico.
     *
     * @return título esperado de la página
     */
    public abstract String obtenerTituloEsperado();

    /**
     * Obtiene la URL esperada o fragmento de URL de la página.
     * Cada página debe definir su URL específica.
     *
     * @return URL o fragmento de URL esperado
     */
    public abstract String obtenerUrlEsperada();

    // ==================== MÉTODOS DE NAVEGACIÓN ====================

    /**
     * Navega a la URL específica de esta página.
     */
    public void navegarAPagina() {
        String urlPagina = obtenerUrlEsperada();
        logger.info("Navegando a página: {}", this.getClass().getSimpleName());

        utileria.navegarA(urlPagina);
        esperarCargaCompletaPagina();

        if (!esPaginaCargada()) {
            throw new RuntimeException("La página " + this.getClass().getSimpleName() + " no se cargó correctamente");
        }
    }

    /**
     * Espera a que la página termine de cargar completamente.
     * Incluye espera de elementos comunes y verificaciones específicas.
     */
    public void esperarCargaCompletaPagina() {
        logger.debug("Esperando carga completa de página: {}", this.getClass().getSimpleName());

        // Esperar carga básica
        utileria.esperarCargaPagina();

        // Esperar que desaparezcan los loaders
        esperarDesaparicionLoader();

        // Dar tiempo adicional para elementos dinámicos
        utileria.pausa(500);

        logger.debug("Carga completa de página finalizada");
    }

    /**
     * Espera a que desaparezcan los indicadores de carga.
     */
    protected void esperarDesaparicionLoader() {
        try {
            if (utileria.esElementoPresente(LOADER)) {
                logger.debug("Esperando desaparición de loader");
                utileria.obtenerEspera(TIMEOUT_CARGA_PAGINA)
                        .until(driver -> !utileria.esElementoVisible(LOADER));
                logger.debug("Loader desaparecido");
            }
        } catch (Exception e) {
            logger.debug("No se encontró loader o timeout esperando desaparición");
        }
    }

    // ==================== MÉTODOS DE ELEMENTOS ====================

    /**
     * Busca un elemento de forma segura con el contexto de la página actual.
     *
     * @param localizador localizador del elemento
     * @return WebElement encontrado
     */
    protected WebElement buscarElemento(By localizador) {
        registrarAccion("Buscando elemento: " + localizador);
        return utileria.buscarElemento(localizador);
    }

    /**
     * Busca un elemento clickeable.
     *
     * @param localizador localizador del elemento
     * @return WebElement clickeable
     */
    protected WebElement buscarElementoClickeable(By localizador) {
        registrarAccion("Buscando elemento clickeable: " + localizador);
        return utileria.buscarElementoClickeable(localizador);
    }

    /**
     * Busca múltiples elementos.
     *
     * @param localizador localizador de los elementos
     * @return lista de WebElement
     */
    protected List<WebElement> buscarElementos(By localizador) {
        registrarAccion("Buscando elementos: " + localizador);
        return utileria.buscarElementos(localizador);
    }

    /**
     * Verifica si un elemento está presente.
     *
     * @param localizador localizador del elemento
     * @return true si está presente, false en caso contrario
     */
    protected boolean esElementoPresente(By localizador) {
        return utileria.esElementoPresente(localizador);
    }

    /**
     * Verifica si un elemento está visible.
     *
     * @param localizador localizador del elemento
     * @return true si está visible, false en caso contrario
     */
    protected boolean esElementoVisible(By localizador) {
        return utileria.esElementoVisible(localizador);
    }

    // ==================== MÉTODOS DE ACCIONES ====================

    /**
     * Hace clic en un elemento de forma segura.
     *
     * @param localizador localizador del elemento
     */
    protected void hacerClick(By localizador) {
        registrarAccion("Haciendo clic en: " + localizador);
        utileria.hacerClick(localizador);
    }

    /**
     * Hace clic usando JavaScript.
     *
     * @param localizador localizador del elemento
     */
    protected void hacerClickJavaScript(By localizador) {
        registrarAccion("Haciendo clic con JavaScript en: " + localizador);
        utileria.hacerClickJavaScript(localizador);
    }

    /**
     * Ingresa texto en un campo.
     *
     * @param localizador localizador del campo
     * @param texto texto a ingresar
     */
    protected void ingresarTexto(By localizador, String texto) {
        registrarAccion("Ingresando texto en: " + localizador);
        utileria.ingresarTexto(localizador, texto);
    }

    /**
     * Ingresa texto de forma segura con validación previa.
     *
     * @param localizador localizador del campo
     * @param texto texto a ingresar
     */
    protected void ingresarTextoSeguro(By localizador, String texto) {
        utileria.validarCadenaNoVacia(texto, "Texto a ingresar");

        try {
            // Verificar que el campo esté habilitado
            WebElement campo = buscarElemento(localizador);
            if (!campo.isEnabled()) {
                throw new RuntimeException("El campo no está habilitado: " + localizador);
            }

            ingresarTexto(localizador, texto);

            // Verificar que el texto se ingresó correctamente
            String textoIngresado = campo.getAttribute("value");
            if (!texto.equals(textoIngresado)) {
                logger.warn("Texto ingresado no coincide. Esperado: {}, Actual: {}", texto, textoIngresado);
            }

        } catch (Exception e) {
            logger.error("Error ingresando texto seguro: {}", e.getMessage());
            utileria.capturarScreenshotError("ingreso_texto_seguro_error");
            throw new RuntimeException("Error en ingreso de texto seguro: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el texto de un elemento.
     *
     * @param localizador localizador del elemento
     * @return texto del elemento
     */
    protected String obtenerTexto(By localizador) {
        registrarAccion("Obteniendo texto de: " + localizador);
        return utileria.obtenerTexto(localizador);
    }

    /**
     * Obtiene el valor de un atributo de un elemento.
     *
     * @param localizador localizador del elemento
     * @param atributo nombre del atributo
     * @return valor del atributo
     */
    protected String obtenerAtributo(By localizador, String atributo) {
        registrarAccion("Obteniendo atributo '" + atributo + "' de: " + localizador);

        try {
            WebElement elemento = buscarElemento(localizador);
            String valor = elemento.getAttribute(atributo);

            logger.debug("Atributo '{}' obtenido: {}", atributo, valor);
            return valor;

        } catch (Exception e) {
            logger.error("Error obteniendo atributo '{}' de {}: {}", atributo, localizador, e.getMessage());
            throw new RuntimeException("Error obteniendo atributo: " + e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS DE MENSAJES ====================

    /**
     * Verifica si hay un mensaje de error visible.
     *
     * @return true si hay mensaje de error, false en caso contrario
     */
    public boolean hayMensajeError() {
        return esElementoVisible(MENSAJE_ERROR);
    }

    /**
     * Obtiene el texto del mensaje de error si está presente.
     *
     * @return texto del mensaje de error o cadena vacía si no hay mensaje
     */
    public String obtenerMensajeError() {
        if (hayMensajeError()) {
            String mensaje = obtenerTexto(MENSAJE_ERROR);
            logger.info("Mensaje de error encontrado: {}", mensaje);
            return mensaje;
        }
        return "";
    }

    /**
     * Verifica si hay un mensaje de éxito visible.
     *
     * @return true si hay mensaje de éxito, false en caso contrario
     */
    public boolean hayMensajeExito() {
        return esElementoVisible(MENSAJE_EXITO);
    }

    /**
     * Obtiene el texto del mensaje de éxito si está presente.
     *
     * @return texto del mensaje de éxito o cadena vacía si no hay mensaje
     */
    public String obtenerMensajeExito() {
        if (hayMensajeExito()) {
            String mensaje = obtenerTexto(MENSAJE_EXITO);
            logger.info("Mensaje de éxito encontrado: {}", mensaje);
            return mensaje;
        }
        return "";
    }

    /**
     * Verifica si hay un mensaje de advertencia visible.
     *
     * @return true si hay mensaje de advertencia, false en caso contrario
     */
    public boolean hayMensajeAdvertencia() {
        return esElementoVisible(MENSAJE_ADVERTENCIA);
    }

    /**
     * Obtiene el texto del mensaje de advertencia si está presente.
     *
     * @return texto del mensaje de advertencia o cadena vacía si no hay mensaje
     */
    public String obtenerMensajeAdvertencia() {
        if (hayMensajeAdvertencia()) {
            String mensaje = obtenerTexto(MENSAJE_ADVERTENCIA);
            logger.info("Mensaje de advertencia encontrado: {}", mensaje);
            return mensaje;
        }
        return "";
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Verifica que la página actual sea la esperada.
     *
     * @return true si es la página correcta, false en caso contrario
     */
    public boolean esPaginaCorrecta() {
        try {
            boolean tituloCorrect = utileria.verificarTituloPagina(obtenerTituloEsperado());
            boolean urlCorrecta = utileria.verificarUrl(obtenerUrlEsperada());
            boolean paginaCargada = esPaginaCargada();

            boolean esCorrecta = tituloCorrect && urlCorrecta && paginaCargada;

            logger.debug("Verificación de página: Título={}, URL={}, Cargada={}, Resultado={}",
                    tituloCorrect, urlCorrecta, paginaCargada, esCorrecta);

            return esCorrecta;

        } catch (Exception e) {
            logger.error("Error verificando página correcta: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valida que un campo obligatorio tenga contenido.
     *
     * @param localizador localizador del campo
     * @param nombreCampo nombre del campo para el mensaje de error
     * @return true si el campo tiene contenido, false en caso contrario
     */
    protected boolean validarCampoObligatorio(By localizador, String nombreCampo) {
        try {
            String valor = obtenerAtributo(localizador, "value");

            if (valor == null || valor.trim().isEmpty()) {
                logger.warn("Campo obligatorio vacío: {}", nombreCampo);
                return false;
            }

            logger.debug("Campo obligatorio válido: {}", nombreCampo);
            return true;

        } catch (Exception e) {
            logger.error("Error validando campo obligatorio {}: {}", nombreCampo, e.getMessage());
            return false;
        }
    }

    /**
     * Valida el formato de un email.
     *
     * @param email email a validar
     * @return true si el formato es válido, false en caso contrario
     */
    protected boolean validarFormatoEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String patronEmail = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        boolean esValido = email.matches(patronEmail);

        logger.debug("Validación de email '{}': {}", email, esValido);
        return esValido;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Registra una acción ejecutada en la página.
     *
     * @param descripcionAccion descripción de la acción
     */
    protected void registrarAccion(String descripcionAccion) {
        String accionCompleta = String.format("[%s] %s", this.getClass().getSimpleName(), descripcionAccion);
        logger.debug(accionCompleta);

        // Registrar en trazabilidad si hay contexto de escenario activo
        try {
            utileria.registrarPaso("ACTUAL", accionCompleta);
        } catch (Exception e) {
            // Ignorar errores de trazabilidad para no interrumpir el flujo
            logger.trace("No se pudo registrar en trazabilidad: {}", e.getMessage());
        }
    }

    /**
     * Captura un screenshot específico de la página.
     *
     * @param contexto contexto adicional para el nombre del archivo
     * @return ruta del archivo de screenshot
     */
    public String capturarScreenshotPagina(String contexto) {
        String nombreArchivo = this.getClass().getSimpleName() + "_" + contexto;
        return utileria.capturarScreenshot(nombreArchivo);
    }

    /**
     * Refresh de la página actual.
     */
    public void refrescarPagina() {
        logger.info("Refrescando página: {}", this.getClass().getSimpleName());

        try {
            utileria.obtenerNavegador().navigate().refresh();
            esperarCargaCompletaPagina();

            logger.debug("Página refrescada exitosamente");

        } catch (Exception e) {
            logger.error("Error refrescando página: {}", e.getMessage());
            throw new RuntimeException("Error refrescando página: " + e.getMessage(), e);
        }
    }

    /**
     * Vuelve a la página anterior en el historial.
     */
    public void volverPaginaAnterior() {
        logger.info("Navegando a página anterior desde: {}", this.getClass().getSimpleName());

        try {
            utileria.obtenerNavegador().navigate().back();
            utileria.esperarCargaPagina();

            logger.debug("Navegación a página anterior exitosa");

        } catch (Exception e) {
            logger.error("Error navegando a página anterior: {}", e.getMessage());
            throw new RuntimeException("Error navegando atrás: " + e.getMessage(), e);
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
                    "Página: %s | %s | Cargada: %s | Mensajes: Error=%s, Éxito=%s",
                    this.getClass().getSimpleName(),
                    utileria.obtenerInformacionEntorno(),
                    esPaginaCargada(),
                    hayMensajeError(),
                    hayMensajeExito()
            );

        } catch (Exception e) {
            return "Error obteniendo información de debug: " + e.getMessage();
        }
    }

    /**
     * Método de limpieza que se puede sobreescribir en páginas específicas.
     * Se ejecuta al finalizar operaciones en la página.
     */
    public void limpiar() {
        logger.debug("Ejecutando limpieza de página: {}", this.getClass().getSimpleName());

        try {
            // Limpieza básica - las páginas pueden sobreescribir para agregar limpieza específica
            // Por ejemplo: cerrar modales, limpiar filtros, etc.

        } catch (Exception e) {
            logger.warn("Error en limpieza de página: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS PROTEGIDOS FINALES ====================

    /**
     * Obtiene la instancia de Utileria para uso en clases heredadas.
     *
     * @return instancia de Utileria
     */
    protected final Utileria obtenerUtileria() {
        return utileria;
    }

    /**
     * Obtiene el logger para uso en clases heredadas.
     *
     * @return logger configurado
     */
    protected final Logger obtenerLogger() {
        return logger;
    }

    protected void esperarElementoVisible(By localizador) {
        utileria.esperarElementoVisible(localizador, TIMEOUT_CARGA_PAGINA);
    }
}