package com.qa.automatizacion.paginas;

import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import com.qa.automatizacion.utilidades.HelperTrazabilidad;
import com.qa.automatizacion.utilidades.UtileriasComunes;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Clase base abstracta para todos los Page Objects del proyecto.
 * Proporciona funcionalidades comunes y reutilizables usando UtileriasComunes.
 *
 * Principios aplicados:
 * - DRY: Centraliza funcionalidades comunes en UtileriasComunes
 * - Template Method: Define la estructura común para todas las páginas
 * - Delegation: Delega operaciones complejas a UtileriasComunes
 * - Single Responsibility: Se enfoca en ser la base para Page Objects
 * - Open/Closed: Abierta para extensión por las páginas hijas
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0 - Optimizada con métodos reutilizables centralizados
 */
public abstract class PaginaBase {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final WebDriver driver;
    protected final PropiedadesAplicacion propiedades;
    protected final HelperTrazabilidad trazabilidad;

    // Localizadores comunes para todas las páginas
    protected static final By SPINNER_CARGA = By.cssSelector(".spinner, .loading, [data-testid='loading']");
    protected static final By OVERLAY_CARGA = By.cssSelector(".overlay, .modal-backdrop, [data-testid='overlay']");
    protected static final By MENSAJE_ERROR_GLOBAL = By.cssSelector(".alert-error, .error-global, [data-testid='global-error']");
    protected static final By MENSAJE_EXITO_GLOBAL = By.cssSelector(".alert-success, .success-global, [data-testid='global-success']");

    /**
     * Constructor que inicializa la página base con todas las dependencias.
     *
     * @param driver WebDriver activo - puede ser null para usar el driver global
     */
    protected PaginaBase(WebDriver driver) {
        this.driver = driver != null ? driver : ConfiguradorNavegador.obtenerDriver();
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();
        this.trazabilidad = new HelperTrazabilidad();

        // Inicializar elementos usando PageFactory si el driver está disponible
        if (this.driver != null) {
            PageFactory.initElements(this.driver, this);
        }

        logger.debug("Página base inicializada: {}", this.getClass().getSimpleName());
    }

    /**
     * Constructor por defecto que usa el driver global.
     */
    protected PaginaBase() {
        this(null);
    }

    // ==================== MÉTODOS ABSTRACTOS ====================

    /**
     * Verifica si la página está completamente cargada.
     * Debe ser implementado por cada página específica.
     *
     * @return true si la página está cargada
     */
    public abstract boolean estaPaginaCargada();

    /**
     * Obtiene la URL base de la página.
     * Debe ser implementado por cada página específica.
     *
     * @return URL base de la página
     */
    public abstract String obtenerUrlBase();

    /**
     * Obtiene los localizadores únicos que identifican esta página.
     * Debe ser implementado por cada página específica.
     *
     * @return array de localizadores únicos de la página
     */
    protected abstract By[] obtenerLocalizadoresUnicos();

    // ==================== MÉTODOS DE NAVEGACIÓN ====================

    /**
     * Navega a esta página usando su URL base.
     */
    public void navegarAPagina() {
        String url = obtenerUrlBase();
        registrarAccion("Navegando a página", this.getClass().getSimpleName(), url);
        UtileriasComunes.navegarAUrl(driver, url);
        esperarCargaPagina();
    }

    /**
     * Actualiza la página actual.
     */
    public void actualizarPagina() {
        registrarAccion("Actualizando página", this.getClass().getSimpleName());
        UtileriasComunes.actualizarPagina(driver);
        esperarCargaPagina();
    }

    /**
     * Navega hacia atrás en el historial.
     */
    public void navegarAtras() {
        registrarAccion("Navegando hacia atrás");
        UtileriasComunes.navegarAtras(driver);
        esperarCargaPagina();
    }

    // ==================== MÉTODOS DE BÚSQUEDA DE ELEMENTOS ====================

    /**
     * Busca un elemento en la página usando UtileriasComunes.
     *
     * @param localizador By localizador del elemento
     * @param timeoutSegundos timeout opcional
     * @return Optional con el elemento si se encuentra
     */
    protected Optional<WebElement> buscarElemento(By localizador, int... timeoutSegundos) {
        return UtileriasComunes.buscarElemento(driver, localizador, timeoutSegundos);
    }

    /**
     * Busca múltiples elementos en la página.
     *
     * @param localizador By localizador de los elementos
     * @param timeoutSegundos timeout opcional
     * @return Lista de elementos encontrados
     */
    protected List<WebElement> buscarElementos(By localizador, int... timeoutSegundos) {
        return UtileriasComunes.buscarElementos(driver, localizador, timeoutSegundos);
    }

    /**
     * Busca un elemento clickeable con espera fluida.
     *
     * @param localizador By localizador del elemento
     * @param timeoutSegundos timeout opcional
     * @return Optional con el elemento clickeable
     */
    protected Optional<WebElement> buscarElementoClickeable(By localizador, int... timeoutSegundos) {
        return UtileriasComunes.buscarElementoClickeable(driver, localizador, timeoutSegundos);
    }

    // ==================== MÉTODOS DE VERIFICACIÓN ====================

    /**
     * Verifica si un elemento está visible.
     *
     * @param localizador By localizador del elemento
     * @param timeoutSegundos timeout opcional
     * @return true si el elemento está visible
     */
    protected boolean esElementoVisible(By localizador, int... timeoutSegundos) {
        return UtileriasComunes.esElementoVisible(driver, localizador, timeoutSegundos);
    }

    /**
     * Verifica si un elemento está habilitado.
     *
     * @param localizador By localizador del elemento
     * @return true si el elemento está habilitado
     */
    protected boolean esElementoHabilitado(By localizador) {
        return UtileriasComunes.esElementoHabilitado(driver, localizador);
    }

    /**
     * Verifica si un elemento está seleccionado.
     *
     * @param localizador By localizador del elemento
     * @return true si el elemento está seleccionado
     */
    protected boolean esElementoSeleccionado(By localizador) {
        return UtileriasComunes.esElementoSeleccionado(driver, localizador);
    }

    /**
     * Verifica si la página contiene un texto específico.
     *
     * @param texto texto a buscar
     * @return true si el texto está presente
     */
    public boolean paginaContieneTexto(String texto) {
        registrarAccion("Verificando texto en página", texto);
        return UtileriasComunes.paginaContienTexto(driver, texto);
    }

    // ==================== MÉTODOS DE INTERACCIÓN ====================

    /**
     * Hace clic en un elemento de forma segura.
     *
     * @param localizador By localizador del elemento
     * @param reintentos número de reintentos opcionales
     * @return true si el clic fue exitoso
     */
    protected boolean hacerClicSeguro(By localizador, int... reintentos) {
        registrarAccion("Haciendo clic en elemento", localizador.toString());
        return UtileriasComunes.hacerClicSeguro(driver, localizador, reintentos);
    }

    /**
     * Hace doble clic en un elemento.
     *
     * @param localizador By localizador del elemento
     * @return true si el doble clic fue exitoso
     */
    protected boolean hacerDobleClicSeguro(By localizador) {
        registrarAccion("Haciendo doble clic en elemento", localizador.toString());
        return UtileriasComunes.hacerDobleClicSeguro(driver, localizador);
    }

    /**
     * Hace clic derecho en un elemento.
     *
     * @param localizador By localizador del elemento
     * @return true si el clic derecho fue exitoso
     */
    protected boolean hacerClicDerechoSeguro(By localizador) {
        registrarAccion("Haciendo clic derecho en elemento", localizador.toString());
        return UtileriasComunes.hacerClicDerechoSeguro(driver, localizador);
    }

    /**
     * Ingresa texto en un campo de forma segura.
     *
     * @param localizador By localizador del campo
     * @param texto texto a ingresar
     * @param limpiarAntes true para limpiar el campo antes de escribir
     * @return true si el texto se ingresó correctamente
     */
    protected boolean ingresarTextoSeguro(By localizador, String texto, boolean... limpiarAntes) {
        registrarAccion("Ingresando texto en campo", localizador.toString(), "Texto: " + texto);
        return UtileriasComunes.ingresarTextoSeguro(driver, localizador, texto, limpiarAntes);
    }

    /**
     * Selecciona una opción de dropdown por texto visible.
     *
     * @param localizador By localizador del select
     * @param textoOpcion texto de la opción a seleccionar
     * @return true si la selección fue exitosa
     */
    protected boolean seleccionarOpcionPorTexto(By localizador, String textoOpcion) {
        registrarAccion("Seleccionando opción por texto", localizador.toString(), "Opción: " + textoOpcion);
        return UtileriasComunes.seleccionarOpcionPorTexto(driver, localizador, textoOpcion);
    }

    /**
     * Selecciona una opción de dropdown por valor.
     *
     * @param localizador By localizador del select
     * @param valor valor de la opción a seleccionar
     * @return true si la selección fue exitosa
     */
    protected boolean seleccionarOpcionPorValor(By localizador, String valor) {
        registrarAccion("Seleccionando opción por valor", localizador.toString(), "Valor: " + valor);
        return UtileriasComunes.seleccionarOpcionPorValor(driver, localizador, valor);
    }

    // ==================== MÉTODOS DE OBTENCIÓN DE DATOS ====================

    /**
     * Obtiene el texto de un elemento.
     *
     * @param localizador By localizador del elemento
     * @return Optional con el texto del elemento
     */
    protected Optional<String> obtenerTextoElemento(By localizador) {
        return UtileriasComunes.obtenerTextoElemento(driver, localizador);
    }

    /**
     * Obtiene el valor de un atributo de un elemento.
     *
     * @param localizador By localizador del elemento
     * @param nombreAtributo nombre del atributo
     * @return Optional con el valor del atributo
     */
    protected Optional<String> obtenerAtributoElemento(By localizador, String nombreAtributo) {
        return UtileriasComunes.obtenerAtributoElemento(driver, localizador, nombreAtributo);
    }

    /**
     * Obtiene el valor de un campo de entrada.
     *
     * @param localizador By localizador del campo
     * @return Optional con el valor del campo
     */
    protected Optional<String> obtenerValorCampo(By localizador) {
        return UtileriasComunes.obtenerValorCampo(driver, localizador);
    }

    /**
     * Obtiene el título actual de la página.
     *
     * @return título de la página
     */
    public String obtenerTituloPagina() {
        return UtileriasComunes.obtenerTituloPagina(driver);
    }

    /**
     * Obtiene la URL actual de la página.
     *
     * @return URL actual
     */
    public String obtenerUrlActual() {
        return UtileriasComunes.obtenerUrlActual(driver);
    }

    // ==================== MÉTODOS DE ESPERA ====================

    /**
     * Espera que un elemento sea visible.
     *
     * @param localizador By localizador del elemento
     * @param timeoutSegundos timeout en segundos
     * @return true si el elemento se volvió visible
     */
    protected boolean esperarElementoVisible(By localizador, int timeoutSegundos) {
        return UtileriasComunes.esperarElementoVisible(driver, localizador, timeoutSegundos);
    }

    /**
     * Espera que un elemento no sea visible.
     *
     * @param localizador By localizador del elemento
     * @param timeoutSegundos timeout en segundos
     * @return true si el elemento dejó de ser visible
     */
    protected boolean esperarElementoNoVisible(By localizador, int timeoutSegundos) {
        return UtileriasComunes.esperarElementoNoVisible(driver, localizador, timeoutSegundos);
    }

    /**
     * Espera que aparezca texto específico en un elemento.
     *
     * @param localizador By localizador del elemento
     * @param texto texto esperado
     * @param timeoutSegundos timeout en segundos
     * @return true si el texto apareció
     */
    protected boolean esperarTextoEnElemento(By localizador, String texto, int timeoutSegundos) {
        return UtileriasComunes.esperarTextoEnElemento(driver, localizador, texto, timeoutSegundos);
    }

    /**
     * Espera una cantidad específica de segundos.
     *
     * @param segundos segundos a esperar
     */
    protected void esperarSegundos(int segundos) {
        UtileriasComunes.esperarSegundos(segundos);
    }

    /**
     * Espera que la página termine de cargar completamente.
     * Incluye verificaciones específicas para esta página.
     */
    public void esperarCargaPagina() {
        registrarAccion("Esperando carga completa de página", this.getClass().getSimpleName());

        // Espera base para carga de DOM
        UtileriasComunes.esperarCargaPagina(driver);

        // Esperar que desaparezcan los spinners de carga comunes
        esperarElementoNoVisible(SPINNER_CARGA, 5);
        esperarElementoNoVisible(OVERLAY_CARGA, 5);

        // Verificar que los localizadores únicos estén presentes
        for (By localizador : obtenerLocalizadoresUnicos()) {
            if (esperarElementoVisible(localizador, 3)) {
                break; // Si al menos uno está visible, la página está cargada
            }
        }

        logger.debug("Carga de página completada: {}", this.getClass().getSimpleName());
    }

    // ==================== MÉTODOS DE MANEJO DE ALERTAS Y FRAMES ====================

    /**
     * Acepta una alerta JavaScript si está presente.
     *
     * @param timeoutSegundos timeout para esperar la alerta
     * @return true si se aceptó la alerta
     */
    protected boolean aceptarAlerta(int timeoutSegundos) {
        registrarAccion("Aceptando alerta JavaScript");
        return UtileriasComunes.aceptarAlerta(driver, timeoutSegundos);
    }

    /**
     * Rechaza una alerta JavaScript si está presente.
     *
     * @param timeoutSegundos timeout para esperar la alerta
     * @return true si se rechazó la alerta
     */
    protected boolean rechazarAlerta(int timeoutSegundos) {
        registrarAccion("Rechazando alerta JavaScript");
        return UtileriasComunes.rechazarAlerta(driver, timeoutSegundos);
    }

    /**
     * Obtiene el texto de una alerta si está presente.
     *
     * @param timeoutSegundos timeout para esperar la alerta
     * @return Optional con el texto de la alerta
     */
    protected Optional<String> obtenerTextoAlerta(int timeoutSegundos) {
        return UtileriasComunes.obtenerTextoAlerta(driver, timeoutSegundos);
    }

    /**
     * Cambia al frame por su nombre o ID.
     *
     * @param nombreOId nombre o ID del frame
     * @return true si se cambió exitosamente
     */
    protected boolean cambiarAFrame(String nombreOId) {
        registrarAccion("Cambiando a frame", nombreOId);
        return UtileriasComunes.cambiarAFramePorNombre(driver, nombreOId);
    }

    /**
     * Cambia al frame por elemento.
     *
     * @param localizadorFrame By localizador del frame
     * @return true si se cambió exitosamente
     */
    protected boolean cambiarAFrame(By localizadorFrame) {
        registrarAccion("Cambiando a frame", localizadorFrame.toString());
        return UtileriasComunes.cambiarAFramePorElemento(driver, localizadorFrame);
    }

    /**
     * Regresa al contenido principal.
     */
    protected void regresarAContenidoPrincipal() {
        registrarAccion("Regresando al contenido principal");
        UtileriasComunes.regresarAContenidoPrincipal(driver);
    }

    // ==================== MÉTODOS DE CAPTURA Y DIAGNÓSTICO ====================

    /**
     * Toma una captura de pantalla de la página completa.
     *
     * @return byte array con la imagen
     */
    public byte[] tomarCapturaPantalla() {
        registrarAccion("Tomando captura de pantalla", this.getClass().getSimpleName());
        return UtileriasComunes.tomarCapturaPantalla(driver);
    }

    /**
     * Toma captura de pantalla de un elemento específico.
     *
     * @param localizador By localizador del elemento
     * @return byte array con la imagen del elemento
     */
    protected byte[] tomarCapturaElemento(By localizador) {
        registrarAccion("Tomando captura de elemento", localizador.toString());
        return UtileriasComunes.tomarCapturaElemento(driver, localizador);
    }

    /**
     * Obtiene información de diagnóstico de la página.
     *
     * @return String con información de diagnóstico
     */
    public String obtenerInformacionDiagnostico() {
        return UtileriasComunes.obtenerInformacionDiagnostico(driver);
    }

    /**
     * Ejecuta JavaScript personalizado en la página.
     *
     * @param script script de JavaScript
     * @param argumentos argumentos opcionales
     * @return resultado de la ejecución
     */
    protected Object ejecutarJavaScript(String script, Object... argumentos) {
        registrarAccion("Ejecutando JavaScript personalizado");
        return UtileriasComunes.ejecutarJavaScript(driver, script, argumentos);
    }

    // ==================== MÉTODOS DE VALIDACIÓN COMUNES ====================

    /**
     * Verifica si hay mensajes de error globales en la página.
     *
     * @return true si hay mensajes de error
     */
    public boolean hayMensajesError() {
        return esElementoVisible(MENSAJE_ERROR_GLOBAL, 2);
    }

    /**
     * Verifica si hay mensajes de éxito globales en la página.
     *
     * @return true si hay mensajes de éxito
     */
    public boolean hayMensajesExito() {
        return esElementoVisible(MENSAJE_EXITO_GLOBAL, 2);
    }

    /**
     * Obtiene el texto del mensaje de error global si existe.
     *
     * @return Optional con el texto del mensaje de error
     */
    public Optional<String> obtenerMensajeError() {
        if (hayMensajesError()) {
            return obtenerTextoElemento(MENSAJE_ERROR_GLOBAL);
        }
        return Optional.empty();
    }

    /**
     * Obtiene el texto del mensaje de éxito global si existe.
     *
     * @return Optional con el texto del mensaje de éxito
     */
    public Optional<String> obtenerMensajeExito() {
        if (hayMensajesExito()) {
            return obtenerTextoElemento(MENSAJE_EXITO_GLOBAL);
        }
        return Optional.empty();
    }

    /**
     * Verifica si la página está en estado de carga.
     *
     * @return true si la página está cargando
     */
    public boolean estaCargando() {
        return esElementoVisible(SPINNER_CARGA, 1) || esElementoVisible(OVERLAY_CARGA, 1);
    }

    // ==================== MÉTODOS DE ACCIONES ESPECÍFICAS DE PÁGINA ====================

    /**
     * Presiona la tecla Enter en la página.
     */
    protected void presionarEnter() {
        registrarAccion("Presionando tecla Enter");
        UtileriasComunes.presionarTecla(driver, Keys.ENTER);
    }

    /**
     * Presiona la tecla Escape en la página.
     */
    protected void presionarEscape() {
        registrarAccion("Presionando tecla Escape");
        UtileriasComunes.presionarTecla(driver, Keys.ESCAPE);
    }

    /**
     * Presiona la tecla Tab en la página.
     */
    protected void presionarTab() {
        registrarAccion("Presionando tecla Tab");
        UtileriasComunes.presionarTecla(driver, Keys.TAB);
    }

    /**
     * Presiona Ctrl+A para seleccionar todo.
     */
    protected void seleccionarTodo() {
        registrarAccion("Seleccionando todo (Ctrl+A)");
        UtileriasComunes.presionarCombinacionTeclas(driver, Keys.CONTROL, Keys.chord("a"));
    }

    /**
     * Presiona Ctrl+C para copiar.
     */
    protected void copiar() {
        registrarAccion("Copiando (Ctrl+C)");
        UtileriasComunes.presionarCombinacionTeclas(driver, Keys.CONTROL, Keys.chord("c"));
    }

    /**
     * Presiona Ctrl+V para pegar.
     */
    protected void pegar() {
        registrarAccion("Pegando (Ctrl+V)");
        UtileriasComunes.presionarCombinacionTeclas(driver, Keys.CONTROL, Keys.chord("v"));
    }

    // ==================== MÉTODOS DE UTILIDADES Y LOGGING ====================

    /**
     * Registra una acción para trazabilidad.
     *
     * @param accion descripción de la acción
     * @param detalles detalles adicionales opcionales
     */
    protected void registrarAccion(String accion, String... detalles) {
        String contexto = "Página: " + this.getClass().getSimpleName();
        String[] detallesCompletos = new String[detalles.length + 1];
        detallesCompletos[0] = contexto;
        System.arraycopy(detalles, 0, detallesCompletos, 1, detalles.length);

        UtileriasComunes.registrarAccionTrazabilidad(accion, detallesCompletos);

        // También registrar en el helper de trazabilidad
        try {
            trazabilidad.registrarPaso(accion, String.join(" | ", detallesCompletos));
        } catch (Exception e) {
            logger.debug("Error registrando trazabilidad: {}", e.getMessage());
        }
    }

    /**
     * Verifica si el driver está disponible y activo.
     *
     * @return true si el driver está disponible
     */
    protected boolean esDriverDisponible() {
        try {
            return driver != null && driver.getWindowHandle() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida que el driver esté disponible antes de realizar operaciones.
     *
     * @throws IllegalStateException si el driver no está disponible
     */
    protected void validarDriverDisponible() {
        if (!esDriverDisponible()) {
            throw new IllegalStateException("WebDriver no está disponible o fue cerrado");
        }
    }

    // ==================== MÉTODOS DE CONFIGURACIÓN Y ESTADO ====================

    /**
     * Obtiene el WebDriver actual.
     *
     * @return WebDriver activo
     */
    public WebDriver obtenerDriver() {
        return driver;
    }

    /**
     * Obtiene las propiedades de la aplicación.
     *
     * @return PropiedadesAplicacion
     */
    protected PropiedadesAplicacion obtenerPropiedades() {
        return propiedades;
    }

    /**
     * Obtiene el helper de trazabilidad.
     *
     * @return HelperTrazabilidad
     */
    protected HelperTrazabilidad obtenerTrazabilidad() {
        return trazabilidad;
    }

    /**
     * Método de limpieza que puede ser sobrescrito por las páginas hijas.
     * Se ejecuta para limpiar recursos específicos de la página.
     */
    public void limpiarRecursos() {
        logger.debug("Limpiando recursos de página: {}", this.getClass().getSimpleName());
        // Las páginas hijas pueden sobrescribir este método para limpieza específica
    }

    /**
     * Método toString para debugging y logging.
     *
     * @return representación en String de la página
     */
    @Override
    public String toString() {
        return String.format("%s{url=%s, titulo=%s, cargada=%s}",
                this.getClass().getSimpleName(),
                obtenerUrlActual(),
                obtenerTituloPagina(),
                estaPaginaCargada());
    }

    /**
     * Método para verificar el estado de salud de la página.
     * Útil para diagnósticos y debugging.
     *
     * @return true si la página está en buen estado
     */
    public boolean verificarSaludPagina() {
        try {
            validarDriverDisponible();

            boolean saludable = estaPaginaCargada() &&
                    !estaCargando() &&
                    !hayMensajesError();

            logger.debug("Salud de página {}: {}", this.getClass().getSimpleName(), saludable);
            return saludable;

        } catch (Exception e) {
            logger.error("Error verificando salud de página: {}", e.getMessage());
            return false;
        }
    }
}