package com.qa.automatizacion.paginas;

import com.qa.automatizacion.utilidades.Utileria;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page Object para la página de registro del sistema.
 * Encapsula todos los elementos y acciones relacionadas con el registro de usuarios.
 *
 * Principios aplicados:
 * - Page Object Pattern: Separa la lógica de UI de los tests
 * - Encapsulación: Oculta los detalles de implementación de Selenium
 * - Single Responsibility: Se enfoca únicamente en la página de registro
 * - Integration with Utileria: Todas las operaciones pasan por la facade central
 */
public class PaginaRegistro extends PaginaBase {

    private static final Logger logger = LoggerFactory.getLogger(PaginaRegistro.class);
    private final Utileria utileria;

    // ==================== LOCALIZADORES ====================

    // Campos de entrada principales
    private static final By CAMPO_NOMBRE = By.id("nombre");
    private static final By CAMPO_EMAIL = By.id("email");
    private static final By CAMPO_PASSWORD = By.id("password");
    private static final By CAMPO_CONFIRMAR_PASSWORD = By.id("confirmar-password");

    // Botones principales
    private static final By BOTON_REGISTRARSE = By.id("btn-registrarse");
    private static final By BOTON_CANCELAR = By.id("btn-cancelar");

    // Checkboxes y opciones
    private static final By CHECKBOX_TERMINOS = By.id("acepto-terminos");
    private static final By CHECKBOX_NEWSLETTER = By.id("suscribir-newsletter");

    // Mensajes y alertas
    private static final By MENSAJE_ERROR = By.cssSelector(".alert-error, .error-message, [data-testid='error-message']");
    private static final By MENSAJE_EXITO = By.cssSelector(".alert-success, .success-message, [data-testid='success-message']");
    private static final By MENSAJE_VALIDACION = By.cssSelector(".field-error, .validation-message");

    // Indicadores de validación por campo
    private static final By ERROR_NOMBRE = By.cssSelector("#nombre + .error-text, [data-field='nombre'] .error");
    private static final By ERROR_EMAIL = By.cssSelector("#email + .error-text, [data-field='email'] .error");
    private static final By ERROR_PASSWORD = By.cssSelector("#password + .error-text, [data-field='password'] .error");
    private static final By ERROR_CONFIRMAR_PASSWORD = By.cssSelector("#confirmar-password + .error-text, [data-field='confirmar-password'] .error");

    // Indicador de fortaleza de contraseña
    private static final By INDICADOR_FORTALEZA = By.cssSelector(".password-strength, [data-testid='password-strength']");
    private static final By ETIQUETA_FORTALEZA = By.cssSelector(".strength-label, [data-testid='strength-label']");
    private static final By CONSEJOS_PASSWORD = By.cssSelector(".password-tips, [data-testid='password-tips']");

    // Formulario y contenedores
    private static final By FORMULARIO_REGISTRO = By.id("form-registro");
    private static final By CONTENEDOR_REGISTRO = By.cssSelector(".registro-container, .signup-container");

    // Títulos y etiquetas
    private static final By TITULO_PAGINA = By.cssSelector("h1, .registro-title, [data-testid='registro-title']");
    private static final By SUBTITULO = By.cssSelector(".subtitle, .registro-subtitle");

    // Enlaces
    private static final By ENLACE_LOGIN = By.linkText("¿Ya tienes cuenta? Inicia sesión");
    private static final By ENLACE_TERMINOS = By.linkText("Términos y Condiciones");

    // Indicadores de carga
    private static final By SPINNER_CARGA = By.cssSelector(".spinner, .loading, [data-testid='loading']");
    private static final By BOTON_CARGANDO = By.cssSelector(".btn-loading, [data-testid='btn-loading']");

    // ==================== CONSTRUCTOR ====================

    public PaginaRegistro() {
        super();
        this.utileria = Utileria.obtenerInstancia();
        logger.debug("PaginaRegistro inicializada");
    }

    // ==================== MÉTODOS ABSTRACTOS IMPLEMENTADOS ====================

    /**
     * Implementa el método abstracto de PaginaBase.
     * Retorna la URL esperada para la página de registro.
     *
     * @return URL esperada de la página de registro
     */
    @Override
    protected String obtenerUrlEsperada() {
        return "/registro";
    }

    // ==================== MÉTODOS PRINCIPALES ====================

    /**
     * Verifica si la página de registro está completamente cargada.
     * Utiliza Utileria para operaciones consistentes y trazabilidad automática.
     *
     * @return true si la página está cargada, false en caso contrario
     */
    @Override
    public boolean esPaginaCargada() {
        try {
            utileria.registrarTrazabilidad("HU-002", "Verificación de carga de página de registro");

            boolean formularioVisible = utileria.esElementoVisible(FORMULARIO_REGISTRO);
            boolean tituloPresente = utileria.esElementoVisible(TITULO_PAGINA);
            boolean camposPresentes = verificarCamposPrincipalesPresentes();

            boolean paginaCargada = formularioVisible && tituloPresente && camposPresentes;

            if (paginaCargada) {
                logger.info("Página de registro cargada correctamente");
                utileria.tomarScreenshot("pagina-registro-cargada");
            } else {
                logger.warn("La página de registro no está completamente cargada");
                utileria.tomarScreenshot("pagina-registro-error-carga");
            }

            return paginaCargada;

        } catch (Exception e) {
            logger.error("Error al verificar carga de página de registro: {}", e.getMessage());
            utileria.manejarError("Error verificando página de registro", e);
            return false;
        }
    }

    /**
     * Ingresa el nombre del usuario en el campo correspondiente.
     *
     * @param nombre nombre a ingresar
     */
    public void ingresarNombre(String nombre) {
        try {
            logger.info("Ingresando nombre: {}", nombre);
            utileria.registrarTrazabilidad("HU-002", "Ingreso de nombre: " + nombre);

            utileria.esperarElementoVisible(CAMPO_NOMBRE);
            utileria.limpiarCampo(CAMPO_NOMBRE);
            utileria.escribirTexto(CAMPO_NOMBRE, nombre);

            // Verificar que el texto fue ingresado correctamente
            if (!utileria.obtenerTexto(CAMPO_NOMBRE).equals(nombre)) {
                throw new RuntimeException("Error al ingresar nombre: texto no coincide");
            }

        } catch (Exception e) {
            logger.error("Error al ingresar nombre: {}", e.getMessage());
            utileria.manejarError("Error ingresando nombre", e);
            throw e;
        }
    }

    /**
     * Ingresa el email del usuario en el campo correspondiente.
     *
     * @param email email a ingresar
     */
    public void ingresarEmail(String email) {
        try {
            logger.info("Ingresando email: {}", email);
            utileria.registrarTrazabilidad("HU-002", "Ingreso de email: " + email);

            utileria.esperarElementoVisible(CAMPO_EMAIL);
            utileria.limpiarCampo(CAMPO_EMAIL);
            utileria.escribirTexto(CAMPO_EMAIL, email);

            // Esperar un momento para que se ejecute la validación de formato
            utileria.esperarTiempo(500);

        } catch (Exception e) {
            logger.error("Error al ingresar email: {}", e.getMessage());
            utileria.manejarError("Error ingresando email", e);
            throw e;
        }
    }

    /**
     * Ingresa la contraseña del usuario y monitorea el indicador de fortaleza.
     *
     * @param password contraseña a ingresar
     */
    public void ingresarPassword(String password) {
        try {
            logger.info("Ingresando contraseña");
            utileria.registrarTrazabilidad("HU-002", "Ingreso de contraseña");

            utileria.esperarElementoVisible(CAMPO_PASSWORD);
            utileria.limpiarCampo(CAMPO_PASSWORD);
            utileria.escribirTexto(CAMPO_PASSWORD, password);

            // Esperar a que se actualice el indicador de fortaleza
            utileria.esperarTiempo(1000);

            String nivelFortaleza = obtenerNivelFortalezaPassword();
            logger.info("Nivel de fortaleza de contraseña: {}", nivelFortaleza);

        } catch (Exception e) {
            logger.error("Error al ingresar contraseña: {}", e.getMessage());
            utileria.manejarError("Error ingresando contraseña", e);
            throw e;
        }
    }

    /**
     * Ingresa la confirmación de contraseña.
     *
     * @param confirmarPassword confirmación de contraseña a ingresar
     */
    public void ingresarConfirmarPassword(String confirmarPassword) {
        try {
            logger.info("Ingresando confirmación de contraseña");
            utileria.registrarTrazabilidad("HU-002", "Ingreso de confirmación de contraseña");

            utileria.esperarElementoVisible(CAMPO_CONFIRMAR_PASSWORD);
            utileria.limpiarCampo(CAMPO_CONFIRMAR_PASSWORD);
            utileria.escribirTexto(CAMPO_CONFIRMAR_PASSWORD, confirmarPassword);

            // Esperar validación de coincidencia
            utileria.esperarTiempo(500);

        } catch (Exception e) {
            logger.error("Error al ingresar confirmación de contraseña: {}", e.getMessage());
            utileria.manejarError("Error ingresando confirmación de contraseña", e);
            throw e;
        }
    }

    /**
     * Acepta los términos y condiciones marcando el checkbox correspondiente.
     */
    public void aceptarTerminos() {
        try {
            logger.info("Aceptando términos y condiciones");
            utileria.registrarTrazabilidad("HU-002", "Aceptación de términos y condiciones");

            if (!utileria.esElementoSeleccionado(CHECKBOX_TERMINOS)) {
                utileria.hacerClick(CHECKBOX_TERMINOS);
                logger.info("Términos y condiciones aceptados");
            } else {
                logger.info("Términos y condiciones ya estaban aceptados");
            }

        } catch (Exception e) {
            logger.error("Error al aceptar términos: {}", e.getMessage());
            utileria.manejarError("Error aceptando términos", e);
            throw e;
        }
    }

    /**
     * Hace clic en el botón de registrarse.
     */
    public void hacerClickBotonRegistrarse() {
        try {
            logger.info("Haciendo clic en botón Registrarse");
            utileria.registrarTrazabilidad("HU-002", "Clic en botón Registrarse");
            utileria.tomarScreenshot("antes-click-registrarse");

            utileria.esperarElementoClickeable(BOTON_REGISTRARSE);
            utileria.hacerClick(BOTON_REGISTRARSE);

            // Esperar a que se procese el registro
            utileria.esperarTiempo(2000);
            utileria.tomarScreenshot("despues-click-registrarse");

        } catch (Exception e) {
            logger.error("Error al hacer clic en registrarse: {}", e.getMessage());
            utileria.manejarError("Error en clic registrarse", e);
            throw e;
        }
    }

    /**
     * Completa todo el formulario de registro con los datos proporcionados.
     *
     * @param nombre nombre del usuario
     * @param email email del usuario
     * @param password contraseña del usuario
     * @param confirmarPassword confirmación de contraseña
     * @param aceptarTerminos si acepta términos y condiciones
     */
    public void completarFormularioRegistro(String nombre, String email, String password,
                                            String confirmarPassword, boolean aceptarTerminos) {
        try {
            logger.info("Completando formulario de registro completo");
            utileria.registrarTrazabilidad("HU-002", "Inicio de formulario de registro completo");
            utileria.tomarScreenshot("inicio-formulario-registro");

            ingresarNombre(nombre);
            ingresarEmail(email);
            ingresarPassword(password);
            ingresarConfirmarPassword(confirmarPassword);

            if (aceptarTerminos) {
                aceptarTerminos();
            }

            utileria.tomarScreenshot("formulario-registro-completado");
            logger.info("Formulario de registro completado exitosamente");

        } catch (Exception e) {
            logger.error("Error al completar formulario de registro: {}", e.getMessage());
            utileria.manejarError("Error completando formulario registro", e);
            throw e;
        }
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Obtiene el mensaje de error general mostrado en la página.
     *
     * @return texto del mensaje de error, o cadena vacía si no hay error
     */
    public String obtenerMensajeError() {
        try {
            if (utileria.esElementoVisible(MENSAJE_ERROR)) {
                String mensaje = utileria.obtenerTexto(MENSAJE_ERROR);
                logger.info("Mensaje de error obtenido: {}", mensaje);
                return mensaje;
            }
            return "";
        } catch (Exception e) {
            logger.warn("Error al obtener mensaje de error: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Obtiene el mensaje de éxito mostrado en la página.
     *
     * @return texto del mensaje de éxito, o cadena vacía si no hay mensaje
     */
    public String obtenerMensajeExito() {
        try {
            if (utileria.esElementoVisible(MENSAJE_EXITO)) {
                String mensaje = utileria.obtenerTexto(MENSAJE_EXITO);
                logger.info("Mensaje de éxito obtenido: {}", mensaje);
                return mensaje;
            }
            return "";
        } catch (Exception e) {
            logger.warn("Error al obtener mensaje de éxito: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Obtiene el nivel de fortaleza de la contraseña actualmente mostrado.
     *
     * @return nivel de fortaleza (Muy débil, Débil, Moderada, Fuerte, Muy fuerte)
     */
    public String obtenerNivelFortalezaPassword() {
        try {
            if (utileria.esElementoVisible(ETIQUETA_FORTALEZA)) {
                String nivel = utileria.obtenerTexto(ETIQUETA_FORTALEZA);
                logger.debug("Nivel de fortaleza detectado: {}", nivel);
                return nivel;
            }
            return "No detectado";
        } catch (Exception e) {
            logger.warn("Error al obtener nivel de fortaleza: {}", e.getMessage());
            return "Error";
        }
    }

    /**
     * Verifica si hay errores de validación en campos específicos.
     *
     * @param campo nombre del campo a verificar (nombre, email, password, confirmar-password)
     * @return true si hay error en el campo, false en caso contrario
     */
    public boolean tieneErrorValidacion(String campo) {
        try {
            By localizadorError = switch (campo.toLowerCase()) {
                case "nombre" -> ERROR_NOMBRE;
                case "email" -> ERROR_EMAIL;
                case "password" -> ERROR_PASSWORD;
                case "confirmar-password" -> ERROR_CONFIRMAR_PASSWORD;
                default -> throw new IllegalArgumentException("Campo no válido: " + campo);
            };

            boolean tieneError = utileria.esElementoVisible(localizadorError);
            if (tieneError) {
                String mensajeError = utileria.obtenerTexto(localizadorError);
                logger.info("Error en campo {}: {}", campo, mensajeError);
            }
            return tieneError;

        } catch (Exception e) {
            logger.warn("Error al verificar validación del campo {}: {}", campo, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si el botón de registrarse está habilitado.
     *
     * @return true si está habilitado, false en caso contrario
     */
    public boolean esBotonRegistrarseHabilitado() {
        try {
            return utileria.esElementoHabilitado(BOTON_REGISTRARSE);
        } catch (Exception e) {
            logger.warn("Error al verificar estado del botón registrarse: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE NAVEGACIÓN ====================

    /**
     * Navega entre los campos del formulario usando la tecla Tab.
     */
    public void navegarConTab() {
        try {
            logger.info("Navegando formulario con tecla Tab");
            utileria.registrarTrazabilidad("HU-002", "Navegación con Tab en registro");

            utileria.enviarTecla(CAMPO_NOMBRE, Keys.TAB);
            utileria.esperarTiempo(200);
            utileria.enviarTecla(CAMPO_EMAIL, Keys.TAB);
            utileria.esperarTiempo(200);
            utileria.enviarTecla(CAMPO_PASSWORD, Keys.TAB);
            utileria.esperarTiempo(200);
            utileria.enviarTecla(CAMPO_CONFIRMAR_PASSWORD, Keys.TAB);

        } catch (Exception e) {
            logger.error("Error en navegación con Tab: {}", e.getMessage());
            utileria.manejarError("Error navegación Tab", e);
        }
    }

    /**
     * Limpia todos los campos del formulario.
     */
    public void limpiarFormulario() {
        try {
            logger.info("Limpiando formulario de registro");
            utileria.registrarTrazabilidad("HU-002", "Limpieza de formulario");

            utileria.limpiarCampo(CAMPO_NOMBRE);
            utileria.limpiarCampo(CAMPO_EMAIL);
            utileria.limpiarCampo(CAMPO_PASSWORD);
            utileria.limpiarCampo(CAMPO_CONFIRMAR_PASSWORD);

            // Desmarcar checkbox si está marcado
            if (utileria.esElementoSeleccionado(CHECKBOX_TERMINOS)) {
                utileria.hacerClick(CHECKBOX_TERMINOS);
            }

        } catch (Exception e) {
            logger.error("Error al limpiar formulario: {}", e.getMessage());
            utileria.manejarError("Error limpiando formulario", e);
        }
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    /**
     * Verifica que todos los campos principales del formulario estén presentes.
     *
     * @return true si todos los campos están presentes, false en caso contrario
     */
    private boolean verificarCamposPrincipalesPresentes() {
        try {
            return utileria.esElementoVisible(CAMPO_NOMBRE) &&
                    utileria.esElementoVisible(CAMPO_EMAIL) &&
                    utileria.esElementoVisible(CAMPO_PASSWORD) &&
                    utileria.esElementoVisible(CAMPO_CONFIRMAR_PASSWORD) &&
                    utileria.esElementoVisible(BOTON_REGISTRARSE);
        } catch (Exception e) {
            logger.warn("Error verificando campos principales: {}", e.getMessage());
            return false;
        }
    }
}