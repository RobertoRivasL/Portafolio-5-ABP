package com.qa.automatizacion.paginas;

import com.qa.automatizacion.modelo.Usuario;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

/**
 * Page Object para la página de registro del sistema.
 * Encapsula todos los elementos y acciones relacionadas con el registro de usuarios.
 *
 * Principios aplicados:
 * - Page Object Pattern: Separa la lógica de UI de los tests
 * - Encapsulación: Oculta los detalles de implementación de Selenium
 * - Single Responsibility: Se enfoca únicamente en la página de registro
 * - DRY: Reutiliza funcionalidades de la clase base
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
public class PaginaRegistro extends PaginaBase {

    // ==================== LOCALIZADORES CON FINDBY ====================

    // Campos del formulario principal
    @FindBy(id = "nombre-completo")
    private WebElement campoNombreCompleto;

    @FindBy(id = "email")
    private WebElement campoEmail;

    @FindBy(id = "nombre-usuario")
    private WebElement campoNombreUsuario;

    @FindBy(id = "password")
    private WebElement campoPassword;

    @FindBy(id = "confirmar-password")
    private WebElement campoConfirmarPassword;

    @FindBy(id = "telefono")
    private WebElement campoTelefono;

    // Botones principales
    @FindBy(id = "btn-registrar")
    private WebElement botonRegistrar;

    @FindBy(id = "btn-cancelar")
    private WebElement botonCancelar;

    @FindBy(linkText = "¿Ya tienes cuenta? Inicia sesión")
    private WebElement enlaceIniciarSesion;

    // Checkboxes y opciones
    @FindBy(id = "acepto-terminos")
    private WebElement checkboxTerminos;

    @FindBy(id = "acepto-privacidad")
    private WebElement checkboxPrivacidad;

    @FindBy(id = "recibir-newsletter")
    private WebElement checkboxNewsletter;

    // Mensajes y alertas
    @FindBy(css = ".alert-success")
    private WebElement mensajeExito;

    @FindBy(css = ".alert-error")
    private WebElement mensajeError;

    // Contenedores
    @FindBy(id = "form-registro")
    private WebElement formularioRegistro;

    @FindBy(css = ".registro-container")
    private WebElement contenedorRegistro;

    // ==================== LOCALIZADORES ESTÁTICOS ====================

    // Campos de entrada adicionales
    private static final By SELECTOR_ROL = By.id("selector-rol");
    private static final By CAMPO_FECHA_NACIMIENTO = By.id("fecha-nacimiento");
    private static final By CAMPO_DIRECCION = By.id("direccion");
    private static final By CAMPO_CIUDAD = By.id("ciudad");

    // Validaciones y mensajes de error específicos
    private static final By ERROR_EMAIL_EXISTENTE = By.cssSelector("[data-error='email-existente']");
    private static final By ERROR_USUARIO_EXISTENTE = By.cssSelector("[data-error='usuario-existente']");
    private static final By ERROR_PASSWORD_DEBIL = By.cssSelector("[data-error='password-debil']");
    private static final By ERROR_PASSWORDS_NO_COINCIDEN = By.cssSelector("[data-error='passwords-no-coinciden']");

    // Indicadores de validación
    private static final By CAMPO_EMAIL_VALIDO = By.cssSelector("#email.valid");
    private static final By CAMPO_EMAIL_INVALIDO = By.cssSelector("#email.invalid");
    private static final By CAMPO_PASSWORD_VALIDO = By.cssSelector("#password.valid");
    private static final By CAMPO_PASSWORD_INVALIDO = By.cssSelector("#password.invalid");

    // Elementos de ayuda
    private static final By TOOLTIP_PASSWORD = By.cssSelector(".password-help");
    private static final By FORTALEZA_PASSWORD = By.cssSelector(".password-strength");
    private static final By CONTADOR_CARACTERES = By.cssSelector(".character-counter");

    // Loading y estados
    private static final By SPINNER_REGISTRO = By.cssSelector(".spinner-registro");
    private static final By BOTON_REGISTRAR_DESHABILITADO = By.cssSelector("#btn-registrar:disabled");

    // Enlaces adicionales
    private static final By ENLACE_TERMINOS = By.linkText("Términos y Condiciones");
    private static final By ENLACE_PRIVACIDAD = By.linkText("Política de Privacidad");

    // ==================== MÉTODOS PRINCIPALES ====================

    /**
     * Verifica si la página de registro está completamente cargada.
     *
     * @return true si la página está cargada
     */
    @Override
    public boolean estaPaginaCargada() {
        registrarAccion("Verificando carga de página de registro");

        try {
            boolean formularioVisible = formularioRegistro.isDisplayed();
            boolean camposPresentes = campoNombreCompleto.isDisplayed() &&
                    campoEmail.isDisplayed() &&
                    campoPassword.isDisplayed();
            boolean botonPresente = botonRegistrar.isDisplayed();

            boolean paginaCargada = formularioVisible && camposPresentes && botonPresente;

            logger.debug("Página registro cargada: {} (Formulario: {}, Campos: {}, Botón: {})",
                    paginaCargada, formularioVisible, camposPresentes, botonPresente);

            return paginaCargada;

        } catch (Exception e) {
            logger.error("Error verificando carga de página registro: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la URL esperada para la página de registro.
     *
     * @return URL esperada
     */
    @Override
    public String obtenerUrlEsperada() {
        return propiedades.obtenerUrlRegistro();
    }

    // ==================== MÉTODOS DE INTERACCIÓN ====================

    /**
     * Completa el formulario de registro con datos de un usuario.
     *
     * @param usuario usuario con los datos para el registro
     */
    public void completarFormularioRegistro(Usuario usuario) {
        registrarAccion("Completando formulario de registro para: " + usuario.getNombreUsuario());

        ingresarNombreCompleto(usuario.getNombreCompleto());
        ingresarEmail(usuario.getEmail());
        ingresarNombreUsuario(usuario.getNombreUsuario());
        ingresarPassword(usuario.getPassword());
        confirmarPassword(usuario.getPassword());

        if (usuario.getTelefono() != null && !usuario.getTelefono().isEmpty()) {
            ingresarTelefono(usuario.getTelefono());
        }

        logger.info("Formulario de registro completado para usuario: {}", usuario.getNombreUsuario());
    }

    /**
     * Ingresa el nombre completo del usuario.
     *
     * @param nombreCompleto nombre completo a ingresar
     */
    public void ingresarNombreCompleto(String nombreCompleto) {
        registrarAccion("Ingresando nombre completo: " + nombreCompleto);

        try {
            campoNombreCompleto.clear();
            campoNombreCompleto.sendKeys(nombreCompleto);
            logger.debug("Nombre completo ingresado: {}", nombreCompleto);
        } catch (Exception e) {
            logger.error("Error ingresando nombre completo: {}", e.getMessage());
            throw new RuntimeException("No se pudo ingresar el nombre completo", e);
        }
    }

    /**
     * Ingresa el email del usuario.
     *
     * @param email email a ingresar
     */
    public void ingresarEmail(String email) {
        registrarAccion("Ingresando email: " + email);

        try {
            campoEmail.clear();
            campoEmail.sendKeys(email);

            // Esperar a que se valide el email
            Thread.sleep(500);

            logger.debug("Email ingresado: {}", email);
        } catch (Exception e) {
            logger.error("Error ingresando email: {}", e.getMessage());
            throw new RuntimeException("No se pudo ingresar el email", e);
        }
    }

    /**
     * Ingresa el nombre de usuario.
     *
     * @param nombreUsuario nombre de usuario a ingresar
     */
    public void ingresarNombreUsuario(String nombreUsuario) {
        registrarAccion("Ingresando nombre de usuario: " + nombreUsuario);

        try {
            campoNombreUsuario.clear();
            campoNombreUsuario.sendKeys(nombreUsuario);

            // Esperar validación
            Thread.sleep(500);

            logger.debug("Nombre de usuario ingresado: {}", nombreUsuario);
        } catch (Exception e) {
            logger.error("Error ingresando nombre de usuario: {}", e.getMessage());
            throw new RuntimeException("No se pudo ingresar el nombre de usuario", e);
        }
    }

    /**
     * Ingresa la contraseña del usuario.
     *
     * @param password contraseña a ingresar
     */
    public void ingresarPassword(String password) {
        registrarAccion("Ingresando contraseña");

        try {
            campoPassword.clear();
            campoPassword.sendKeys(password);

            // Esperar a que se evalúe la fortaleza
            Thread.sleep(500);

            logger.debug("Contraseña ingresada");
        } catch (Exception e) {
            logger.error("Error ingresando contraseña: {}", e.getMessage());
            throw new RuntimeException("No se pudo ingresar la contraseña", e);
        }
    }

    /**
     * Confirma la contraseña del usuario.
     *
     * @param password contraseña de confirmación
     */
    public void confirmarPassword(String password) {
        registrarAccion("Confirmando contraseña");

        try {
            campoConfirmarPassword.clear();
            campoConfirmarPassword.sendKeys(password);

            // Esperar validación de coincidencia
            Thread.sleep(500);

            logger.debug("Contraseña confirmada");
        } catch (Exception e) {
            logger.error("Error confirmando contraseña: {}", e.getMessage());
            throw new RuntimeException("No se pudo confirmar la contraseña", e);
        }
    }

    /**
     * Ingresa el teléfono del usuario.
     *
     * @param telefono teléfono a ingresar
     */
    public void ingresarTelefono(String telefono) {
        registrarAccion("Ingresando teléfono: " + telefono);

        try {
            campoTelefono.clear();
            campoTelefono.sendKeys(telefono);
            logger.debug("Teléfono ingresado: {}", telefono);
        } catch (Exception e) {
            logger.error("Error ingresando teléfono: {}", e.getMessage());
            throw new RuntimeException("No se pudo ingresar el teléfono", e);
        }
    }

    /**
     * Acepta los términos y condiciones.
     */
    public void aceptarTerminosYCondiciones() {
        registrarAccion("Aceptando términos y condiciones");

        try {
            if (!checkboxTerminos.isSelected()) {
                checkboxTerminos.click();
                logger.debug("Términos y condiciones aceptados");
            } else {
                logger.debug("Términos y condiciones ya estaban aceptados");
            }
        } catch (Exception e) {
            logger.error("Error aceptando términos: {}", e.getMessage());
            throw new RuntimeException("No se pudo aceptar los términos", e);
        }
    }

    /**
     * Acepta la política de privacidad.
     */
    public void aceptarPoliticaPrivacidad() {
        registrarAccion("Aceptando política de privacidad");

        try {
            if (!checkboxPrivacidad.isSelected()) {
                checkboxPrivacidad.click();
                logger.debug("Política de privacidad aceptada");
            }
        } catch (Exception e) {
            logger.error("Error aceptando política de privacidad: {}", e.getMessage());
            throw new RuntimeException("No se pudo aceptar la política de privacidad", e);
        }
    }

    /**
     * Opta por recibir newsletter (opcional).
     *
     * @param recibirNewsletter true para recibir newsletter
     */
    public void configurarNewsletter(boolean recibirNewsletter) {
        registrarAccion("Configurando newsletter: " + recibirNewsletter);

        try {
            if (recibirNewsletter != checkboxNewsletter.isSelected()) {
                checkboxNewsletter.click();
            }
            logger.debug("Newsletter configurado: {}", recibirNewsletter);
        } catch (Exception e) {
            logger.error("Error configurando newsletter: {}", e.getMessage());
            // No es crítico, continuar
        }
    }

    /**
     * Hace clic en el botón de registrar.
     */
    public void hacerClicRegistrar() {
        registrarAccion("Haciendo clic en botón Registrar");

        try {
            // Verificar que el botón esté habilitado
            if (!botonRegistrar.isEnabled()) {
                throw new RuntimeException("El botón registrar está deshabilitado");
            }

            botonRegistrar.click();
            logger.debug("Clic en botón registrar ejecutado");

            // Esperar a que se procese el registro
            esperarProcesamiento();

        } catch (Exception e) {
            logger.error("Error haciendo clic en registrar: {}", e.getMessage());
            throw new RuntimeException("No se pudo hacer clic en registrar", e);
        }
    }

    /**
     * Cancela el proceso de registro.
     */
    public void cancelarRegistro() {
        registrarAccion("Cancelando registro");

        try {
            botonCancelar.click();
            logger.debug("Registro cancelado");
        } catch (Exception e) {
            logger.error("Error cancelando registro: {}", e.getMessage());
            throw new RuntimeException("No se pudo cancelar el registro", e);
        }
    }

    /**
     * Navega al login desde la página de registro.
     */
    public void navegarALogin() {
        registrarAccion("Navegando a login desde registro");

        try {
            enlaceIniciarSesion.click();
            logger.debug("Navegación a login iniciada");
        } catch (Exception e) {
            logger.error("Error navegando a login: {}", e.getMessage());
            throw new RuntimeException("No se pudo navegar al login", e);
        }
    }

    // ==================== MÉTODOS DE VERIFICACIÓN ====================

    /**
     * Verifica si el email ya existe en el sistema.
     *
     * @return true si el email ya existe
     */
    public boolean emailYaExiste() {
        return esElementoVisible(ERROR_EMAIL_EXISTENTE);
    }

    /**
     * Verifica si el nombre de usuario ya existe.
     *
     * @return true si el usuario ya existe
     */
    public boolean usuarioYaExiste() {
        return esElementoVisible(ERROR_USUARIO_EXISTENTE);
    }

    /**
     * Verifica si la contraseña es considerada débil.
     *
     * @return true si la contraseña es débil
     */
    public boolean passwordEsDebil() {
        return esElementoVisible(ERROR_PASSWORD_DEBIL) || esElementoVisible(CAMPO_PASSWORD_INVALIDO);
    }

    /**
     * Verifica si las contraseñas no coinciden.
     *
     * @return true si las contraseñas no coinciden
     */
    public boolean passwordsNoCoinciden() {
        return esElementoVisible(ERROR_PASSWORDS_NO_COINCIDEN);
    }

    /**
     * Verifica si el registro fue exitoso.
     *
     * @return true si el registro fue exitoso
     */
    public boolean registroFueExitoso() {
        return esElementoVisible(By.cssSelector(".alert-success")) &&
                obtenerMensajeExito().contains("Registro exitoso");
    }

    /**
     * Verifica si el formulario está válido y listo para enviar.
     *
     * @return true si el formulario es válido
     */
    public boolean formularioEsValido() {
        try {
            boolean camposRequeridos = !campoNombreCompleto.getAttribute("value").trim().isEmpty() &&
                    !campoEmail.getAttribute("value").trim().isEmpty() &&
                    !campoPassword.getAttribute("value").trim().isEmpty() &&
                    !campoConfirmarPassword.getAttribute("value").trim().isEmpty();

            boolean terminosAceptados = checkboxTerminos.isSelected() && checkboxPrivacidad.isSelected();

            boolean botonHabilitado = botonRegistrar.isEnabled();

            return camposRequeridos && terminosAceptados && botonHabilitado;
        } catch (Exception e) {
            logger.debug("Error verificando validez del formulario: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE OBTENCIÓN DE DATOS ====================

    /**
     * Obtiene el mensaje de éxito del registro.
     *
     * @return mensaje de éxito
     */
    public String obtenerMensajeExito() {
        try {
            return mensajeExito.isDisplayed() ? mensajeExito.getText().trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Obtiene el mensaje de error del registro.
     *
     * @return mensaje de error
     */
    public String obtenerMensajeError() {
        try {
            return mensajeError.isDisplayed() ? mensajeError.getText().trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Obtiene todos los mensajes de validación visibles.
     *
     * @return lista de mensajes de validación
     */
    public List<String> obtenerMensajesValidacion() {
        List<String> mensajes = new java.util.ArrayList<>();

        try {
            List<WebElement> elementosError = navegador.findElements(By.cssSelector(".error-message, .invalid-feedback"));

            for (WebElement elemento : elementosError) {
                if (elemento.isDisplayed() && !elemento.getText().trim().isEmpty()) {
                    mensajes.add(elemento.getText().trim());
                }
            }
        } catch (Exception e) {
            logger.debug("Error obteniendo mensajes de validación: {}", e.getMessage());
        }

        return mensajes;
    }

    /**
     * Obtiene la fortaleza actual de la contraseña.
     *
     * @return fortaleza de la contraseña (débil, media, fuerte)
     */
    public String obtenerFortalezaPassword() {
        try {
            WebElement indicador = navegador.findElement(FORTALEZA_PASSWORD);
            if (indicador.isDisplayed()) {
                return indicador.getText().trim();
            }
        } catch (Exception e) {
            logger.debug("Error obteniendo fortaleza de password: {}", e.getMessage());
        }

        return "desconocida";
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Espera a que se complete el procesamiento del registro.
     */
    private void esperarProcesamiento() {
        try {
            // Esperar a que aparezca el spinner si existe
            if (esElementoPresente(SPINNER_REGISTRO)) {
                esperarElementoDesaparezca(SPINNER_REGISTRO);
            } else {
                // Espera fija si no hay indicador visual
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            logger.debug("Error esperando procesamiento: {}", e.getMessage());
        }
    }

    /**
     * Limpia todos los campos del formulario.
     */
    public void limpiarFormulario() {
        registrarAccion("Limpiando formulario de registro");

        try {
            campoNombreCompleto.clear();
            campoEmail.clear();
            campoNombreUsuario.clear();
            campoPassword.clear();
            campoConfirmarPassword.clear();
            campoTelefono.clear();

            // Desmarcar checkboxes
            if (checkboxTerminos.isSelected()) {
                checkboxTerminos.click();
            }
            if (checkboxPrivacidad.isSelected()) {
                checkboxPrivacidad.click();
            }
            if (checkboxNewsletter.isSelected()) {
                checkboxNewsletter.click();
            }

            logger.debug("Formulario de registro limpiado");
        } catch (Exception e) {
            logger.error("Error limpiando formulario: {}", e.getMessage());
        }
    }
}