package com.qa.automatizacion.paginas;

import com.qa.automatizacion.modelo.Usuario;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.WebElement;

import java.util.Optional;

/**
 * Page Object para la página de registro de usuarios del sistema.
 * Implementa la nueva arquitectura optimizada usando PaginaBase y UtileriasComunes.
 *
 * Principios aplicados:
 * - Herencia: Extiende PaginaBase para reutilizar funcionalidades comunes
 * - DRY: No duplica código, reutiliza métodos de PaginaBase/UtileriasComunes
 * - Single Responsibility: Se enfoca únicamente en la funcionalidad de registro
 * - Encapsulación: Expone métodos de alto nivel, oculta detalles de implementación
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0 - Optimizada con métodos reutilizables
 */
public class PaginaRegistro extends PaginaBase {

    // ==================== LOCALIZADORES ====================

    // Campos de entrada principales
    private static final By CAMPO_NOMBRE = By.id("nombre");
    private static final By CAMPO_APELLIDO = By.id("apellido");
    private static final By CAMPO_EMAIL = By.id("email");
    private static final By CAMPO_PASSWORD = By.id("password");
    private static final By CAMPO_CONFIRMAR_PASSWORD = By.id("confirmar-password");
    private static final By CAMPO_TELEFONO = By.id("telefono");

    // Botones
    private static final By BOTON_REGISTRAR = By.id("btn-registrar");
    private static final By BOTON_LIMPIAR = By.id("btn-limpiar");
    private static final By BOTON_CANCELAR = By.id("btn-cancelar");
    private static final By BOTON_MOSTRAR_PASSWORD = By.cssSelector("[data-testid='toggle-password']");

    // Checkboxes y opciones
    private static final By CHECKBOX_ACEPTA_TERMINOS = By.id("acepta-terminos");
    private static final By CHECKBOX_SUSCRIBIR_NEWSLETTER = By.id("suscribir-newsletter");

    // Mensajes específicos de registro
    private static final By MENSAJE_REGISTRO_EXITOSO = By.cssSelector("[data-testid='registro-exitoso']");
    private static final By MENSAJE_ERROR_EMAIL_DUPLICADO = By.cssSelector("[data-testid='error-email-duplicado']");
    private static final By MENSAJE_ERROR_PASSWORD_DEBIL = By.cssSelector("[data-testid='error-password-debil']");
    private static final By MENSAJE_ERROR_PASSWORDS_NO_COINCIDEN = By.cssSelector("[data-testid='error-passwords-no-coinciden']");

    // Formulario y contenedores
    private static final By FORMULARIO_REGISTRO = By.id("form-registro");
    private static final By CONTENEDOR_REGISTRO = By.cssSelector(".registro-container");

    // Enlaces
    private static final By ENLACE_LOGIN = By.linkText("¿Ya tienes cuenta? Inicia sesión");
    private static final By ENLACE_TERMINOS = By.linkText("Términos y Condiciones");
    private static final By ENLACE_POLITICA_PRIVACIDAD = By.linkText("Política de Privacidad");

    // Elementos usando @FindBy (opcionales, para compatibilidad)
    @FindBy(id = "nombre")
    private WebElement campoNombreElement;

    @FindBy(id = "email")
    private WebElement campoEmailElement;

    @FindBy(id = "btn-registrar")
    private WebElement botonRegistrarElement;

    // ==================== CONSTRUCTORES ====================

    /**
     * Constructor que acepta un WebDriver específico.
     *
     * @param driver WebDriver a utilizar
     */
    public PaginaRegistro(WebDriver driver) {
        super(driver);
    }

    /**
     * Constructor por defecto que usa el driver global.
     */
    public PaginaRegistro() {
        super();
    }

    // ==================== MÉTODOS ABSTRACTOS IMPLEMENTADOS ====================

    @Override
    public boolean estaPaginaCargada() {
        return esElementoVisible(FORMULARIO_REGISTRO, 5) &&
                esElementoVisible(CAMPO_NOMBRE, 2) &&
                esElementoVisible(CAMPO_EMAIL, 2) &&
                esElementoVisible(BOTON_REGISTRAR, 2) &&
                !estaCargando();
    }

    @Override
    public String obtenerUrlBase() {
        return propiedades.obtenerUrlRegistro();
    }

    @Override
    protected By[] obtenerLocalizadoresUnicos() {
        return new By[]{
                FORMULARIO_REGISTRO,
                CAMPO_NOMBRE,
                CAMPO_APELLIDO,
                BOTON_REGISTRAR
        };
    }

    // ==================== MÉTODOS PRINCIPALES DE REGISTRO ====================

    /**
     * Realiza el proceso completo de registro con un objeto Usuario.
     *
     * @param usuario objeto Usuario con los datos completos
     * @return true si el registro fue exitoso
     */
    public boolean registrarUsuario(Usuario usuario) {
        registrarAccion("Registrando usuario", "Email: " + usuario.getEmail());

        try {
            // Verificar que la página esté cargada
            if (!estaPaginaCargada()) {
                logger.error("La página de registro no está completamente cargada");
                return false;
            }

            // Limpiar formulario antes de llenar datos
            limpiarFormulario();

            // Llenar todos los campos del formulario
            if (!llenarFormularioCompleto(usuario)) {
                logger.error("Error llenando el formulario de registro");
                return false;
            }

            // Aceptar términos y condiciones si es requerido
            if (!aceptarTerminosYCondiciones()) {
                logger.error("Error aceptando términos y condiciones");
                return false;
            }

            // Hacer clic en el botón de registrar
            if (!hacerClicEnRegistrar()) {
                logger.error("Error haciendo clic en botón de registrar");
                return false;
            }

            // Verificar resultado del registro
            return verificarResultadoRegistro();

        } catch (Exception e) {
            logger.error("Error durante el proceso de registro: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Realiza registro básico con datos mínimos.
     *
     * @param nombre nombre del usuario
     * @param apellido apellido del usuario
     * @param email email del usuario
     * @param password contraseña del usuario
     * @return true si el registro fue exitoso
     */
    public boolean registrarUsuarioBasico(String nombre, String apellido, String email, String password) {
        Usuario usuario = Usuario.builder()
                .nombre(nombre)
                .apellido(apellido)
                .email(email)
                .contrasena(password)
                .build();

        return registrarUsuario(usuario);
    }

    // ==================== MÉTODOS DE INGRESO DE DATOS ====================

    /**
     * Llena el formulario completo con los datos del usuario.
     *
     * @param usuario objeto Usuario con los datos
     * @return true si se llenó correctamente
     */
    private boolean llenarFormularioCompleto(Usuario usuario) {
        registrarAccion("Llenando formulario de registro");

        try {
            // Campos obligatorios
            if (!ingresarNombre(usuario.getNombre())) return false;
            if (!ingresarApellido(usuario.getApellido())) return false;
            if (!ingresarEmail(usuario.getEmail())) return false;
            if (!ingresarPassword(usuario.getContrasena())) return false;
            if (!confirmarPassword(usuario.getContrasena())) return false;


            logger.info("Formulario llenado exitosamente");
            return true;

        } catch (Exception e) {
            logger.error("Error llenando formulario: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Ingresa el nombre en el campo correspondiente.
     *
     * @param nombre nombre a ingresar
     * @return true si se ingresó correctamente
     */
    public boolean ingresarNombre(String nombre) {
        registrarAccion("Ingresando nombre", nombre);
        return ingresarTextoSeguro(CAMPO_NOMBRE, nombre, true);
    }

    /**
     * Ingresa el apellido en el campo correspondiente.
     *
     * @param apellido apellido a ingresar
     * @return true si se ingresó correctamente
     */
    public boolean ingresarApellido(String apellido) {
        registrarAccion("Ingresando apellido", apellido);
        return ingresarTextoSeguro(CAMPO_APELLIDO, apellido, true);
    }

    /**
     * Ingresa el email en el campo correspondiente.
     *
     * @param email email a ingresar
     * @return true si se ingresó correctamente
     */
    public boolean ingresarEmail(String email) {
        registrarAccion("Ingresando email", email);
        return ingresarTextoSeguro(CAMPO_EMAIL, email, true);
    }

    /**
     * Ingresa la contraseña en el campo correspondiente.
     *
     * @param password contraseña a ingresar
     * @return true si se ingresó correctamente
     */
    public boolean ingresarPassword(String password) {
        registrarAccion("Ingresando contraseña", "***");
        return ingresarTextoSeguro(CAMPO_PASSWORD, password, true);
    }

    /**
     * Ingresa la confirmación de contraseña.
     *
     * @param password contraseña a confirmar
     * @return true si se ingresó correctamente
     */
    public boolean confirmarPassword(String password) {
        registrarAccion("Confirmando contraseña", "***");
        return ingresarTextoSeguro(CAMPO_CONFIRMAR_PASSWORD, password, true);
    }

    /**
     * Ingresa el número de teléfono.
     *
     * @param telefono teléfono a ingresar
     * @return true si se ingresó correctamente
     */
    public boolean ingresarTelefono(String telefono) {
        registrarAccion("Ingresando teléfono", telefono);
        return ingresarTextoSeguro(CAMPO_TELEFONO, telefono, true);
    }

    // ==================== MÉTODOS DE INTERACCIÓN CON ELEMENTOS ====================

    /**
     * Hace clic en el botón de registrar.
     *
     * @return true si el clic fue exitoso
     */
    public boolean hacerClicEnRegistrar() {
        registrarAccion("Haciendo clic en botón registrar");
        return hacerClicSeguro(BOTON_REGISTRAR, 3);
    }

    /**
     * Hace clic en el botón de limpiar formulario.
     *
     * @return true si el clic fue exitoso
     */
    public boolean hacerClicEnLimpiar() {
        registrarAccion("Haciendo clic en limpiar formulario");
        return hacerClicSeguro(BOTON_LIMPIAR);
    }

    /**
     * Hace clic en el botón de cancelar.
     *
     * @return true si el clic fue exitoso
     */
    public boolean hacerClicEnCancelar() {
        registrarAccion("Haciendo clic en cancelar");
        return hacerClicSeguro(BOTON_CANCELAR);
    }

    /**
     * Hace clic en el enlace de login.
     *
     * @return true si el clic fue exitoso
     */
    public boolean hacerClicEnLogin() {
        registrarAccion("Haciendo clic en enlace de login");
        return hacerClicSeguro(ENLACE_LOGIN);
    }

    /**
     * Marca el checkbox de aceptar términos y condiciones.
     *
     * @return true si se marcó correctamente
     */
    public boolean aceptarTerminosYCondiciones() {
        registrarAccion("Aceptando términos y condiciones");

        if (!esElementoSeleccionado(CHECKBOX_ACEPTA_TERMINOS)) {
            return hacerClicSeguro(CHECKBOX_ACEPTA_TERMINOS);
        }

        logger.debug("Términos y condiciones ya estaban aceptados");
        return true;
    }

    /**
     * Marca o desmarca el checkbox de suscribirse al newsletter.
     *
     * @param suscribir true para suscribirse, false para no suscribirse
     * @return true si la acción fue exitosa
     */
    public boolean configurarSuscripcionNewsletter(boolean suscribir) {
        registrarAccion("Configurando suscripción newsletter", "Suscribir: " + suscribir);

        boolean yaSeleccionado = esElementoSeleccionado(CHECKBOX_SUSCRIBIR_NEWSLETTER);

        if (suscribir && !yaSeleccionado) {
            return hacerClicSeguro(CHECKBOX_SUSCRIBIR_NEWSLETTER);
        } else if (!suscribir && yaSeleccionado) {
            return hacerClicSeguro(CHECKBOX_SUSCRIBIR_NEWSLETTER);
        }

        logger.debug("Configuración de newsletter ya estaba en el estado deseado");
        return true;
    }

    // ==================== MÉTODOS DE VERIFICACIÓN ====================

    /**
     * Verifica el resultado del intento de registro.
     *
     * @return true si el registro fue exitoso
     */
    private boolean verificarResultadoRegistro() {
        registrarAccion("Verificando resultado de registro");

        // Esperar un momento para que aparezcan los mensajes
        esperarSegundos(2);

        // Verificar si hay mensaje de éxito
        if (esElementoVisible(MENSAJE_REGISTRO_EXITOSO, 5)) {
            logger.info("Registro exitoso - mensaje de confirmación visible");
            return true;
        }

        // Verificar si cambió la URL (redirección exitosa)
        String urlActual = obtenerUrlActual();
        if (!urlActual.contains("/registro")) {
            logger.info("Registro exitoso - redirección detectada a: {}", urlActual);
            return true;
        }

        // Verificar si hay mensajes de error específicos
        if (hayErrorEmailDuplicado()) {
            logger.error("Registro fallido - email ya está registrado");
            return false;
        }

        if (hayErrorPasswordDebil()) {
            logger.error("Registro fallido - contraseña demasiado débil");
            return false;
        }

        if (hayErrorPasswordsNoCoinciden()) {
            logger.error("Registro fallido - las contraseñas no coinciden");
            return false;
        }

        if (hayMensajesError()) {
            Optional<String> mensajeError = obtenerMensajeError();
            logger.error("Registro fallido - error general: {}", mensajeError.orElse("Error desconocido"));
            return false;
        }

        // Si seguimos en la página de registro sin mensajes claros
        if (estaPaginaCargada()) {
            logger.error("Registro fallido - aún en página de registro sin mensajes claros");
            return false;
        }

        logger.info("Registro aparentemente exitoso");
        return true;
    }

    /**
     * Verifica si hay error de email duplicado.
     *
     * @return true si hay error de email duplicado
     */
    public boolean hayErrorEmailDuplicado() {
        return esElementoVisible(MENSAJE_ERROR_EMAIL_DUPLICADO, 2);
    }

    /**
     * Verifica si hay error de contraseña débil.
     *
     * @return true si hay error de contraseña débil
     */
    public boolean hayErrorPasswordDebil() {
        return esElementoVisible(MENSAJE_ERROR_PASSWORD_DEBIL, 2);
    }

    /**
     * Verifica si hay error de contraseñas que no coinciden.
     *
     * @return true si hay error de contraseñas no coincidentes
     */
    public boolean hayErrorPasswordsNoCoinciden() {
        return esElementoVisible(MENSAJE_ERROR_PASSWORDS_NO_COINCIDEN, 2);
    }

    /**
     * Verifica si el formulario de registro está visible.
     *
     * @return true si el formulario está visible
     */
    public boolean esFormularioVisible() {
        return esElementoVisible(FORMULARIO_REGISTRO, 2);
    }

    /**
     * Verifica si todos los campos obligatorios están habilitados.
     *
     * @return true si todos los campos están habilitados
     */
    public boolean sonCamposObligatoriosHabilitados() {
        return esElementoHabilitado(CAMPO_NOMBRE) &&
                esElementoHabilitado(CAMPO_APELLIDO) &&
                esElementoHabilitado(CAMPO_EMAIL) &&
                esElementoHabilitado(CAMPO_PASSWORD) &&
                esElementoHabilitado(CAMPO_CONFIRMAR_PASSWORD) &&
                esElementoHabilitado(BOTON_REGISTRAR);
    }

    /**
     * Verifica si el checkbox de términos está marcado.
     *
     * @return true si está marcado
     */
    public boolean estanTerminosAceptados() {
        return esElementoSeleccionado(CHECKBOX_ACEPTA_TERMINOS);
    }

    // ==================== MÉTODOS DE OBTENCIÓN DE DATOS ====================

    /**
     * Obtiene el valor actual del campo nombre.
     *
     * @return Optional con el valor del nombre
     */
    public Optional<String> obtenerValorNombre() {
        return obtenerValorCampo(CAMPO_NOMBRE);
    }

    /**
     * Obtiene el valor actual del campo apellido.
     *
     * @return Optional con el valor del apellido
     */
    public Optional<String> obtenerValorApellido() {
        return obtenerValorCampo(CAMPO_APELLIDO);
    }

    /**
     * Obtiene el valor actual del campo email.
     *
     * @return Optional con el valor del email
     */
    public Optional<String> obtenerValorEmail() {
        return obtenerValorCampo(CAMPO_EMAIL);
    }

    /**
     * Obtiene el valor actual del campo teléfono.
     *
     * @return Optional con el valor del teléfono
     */
    public Optional<String> obtenerValorTelefono() {
        return obtenerValorCampo(CAMPO_TELEFONO);
    }

    /**
     * Obtiene el mensaje de error de email duplicado si está presente.
     *
     * @return Optional con el mensaje de error
     */
    public Optional<String> obtenerMensajeErrorEmailDuplicado() {
        if (hayErrorEmailDuplicado()) {
            return obtenerTextoElemento(MENSAJE_ERROR_EMAIL_DUPLICADO);
        }
        return Optional.empty();
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Valida que las contraseñas coincidan.
     *
     * @param password contraseña principal
     * @param confirmarPassword confirmación de contraseña
     * @return true si coinciden
     */
    public boolean validarCoincidenciaPasswords(String password, String confirmarPassword) {
        boolean coinciden = password != null && password.equals(confirmarPassword);
        registrarAccion("Validando coincidencia de contraseñas", "Coinciden: " + coinciden);
        return coinciden;
    }

    /**
     * Valida que todos los campos obligatorios estén llenos.
     *
     * @return true si todos los campos obligatorios tienen contenido
     */
    public boolean validarCamposObligatoriosLlenos() {
        Optional<String> nombre = obtenerValorNombre();
        Optional<String> apellido = obtenerValorApellido();
        Optional<String> email = obtenerValorEmail();

        boolean todosLlenos = nombre.isPresent() && !nombre.get().trim().isEmpty() &&
                apellido.isPresent() && !apellido.get().trim().isEmpty() &&
                email.isPresent() && !email.get().trim().isEmpty();

        registrarAccion("Validando campos obligatorios", "Todos llenos: " + todosLlenos);
        return todosLlenos;
    }

    // ==================== MÉTODOS DE LIMPIEZA ====================

    /**
     * Limpia todos los campos del formulario de registro.
     */
    public void limpiarFormulario() {
        registrarAccion("Limpiando formulario de registro");

        try {
            // Limpiar todos los campos de texto
            ingresarTextoSeguro(CAMPO_NOMBRE, "", true);
            ingresarTextoSeguro(CAMPO_APELLIDO, "", true);
            ingresarTextoSeguro(CAMPO_EMAIL, "", true);
            ingresarTextoSeguro(CAMPO_PASSWORD, "", true);
            ingresarTextoSeguro(CAMPO_CONFIRMAR_PASSWORD, "", true);
            ingresarTextoSeguro(CAMPO_TELEFONO, "", true);

            // Desmarcar checkboxes si están marcados
            if (esElementoSeleccionado(CHECKBOX_ACEPTA_TERMINOS)) {
                hacerClicSeguro(CHECKBOX_ACEPTA_TERMINOS);
            }
            if (esElementoSeleccionado(CHECKBOX_SUSCRIBIR_NEWSLETTER)) {
                hacerClicSeguro(CHECKBOX_SUSCRIBIR_NEWSLETTER);
            }

            logger.info("Formulario de registro limpiado");

        } catch (Exception e) {
            logger.error("Error limpiando formulario: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE ESPERA ESPECÍFICOS ====================

    /**
     * Espera que aparezca un mensaje de éxito de registro.
     *
     * @param timeoutSegundos timeout en segundos
     * @return true si apareció el mensaje
     */
    public boolean esperarMensajeExito(int timeoutSegundos) {
        return esperarElementoVisible(MENSAJE_REGISTRO_EXITOSO, timeoutSegundos);
    }

    /**
     * Espera que aparezca cualquier mensaje de error.
     *
     * @param timeoutSegundos timeout en segundos
     * @return true si apareció algún mensaje de error
     */
    public boolean esperarMensajeError(int timeoutSegundos) {
        return esperarElementoVisible(MENSAJE_ERROR_EMAIL_DUPLICADO, timeoutSegundos) ||
                esperarElementoVisible(MENSAJE_ERROR_PASSWORD_DEBIL, timeoutSegundos) ||
                esperarElementoVisible(MENSAJE_ERROR_PASSWORDS_NO_COINCIDEN, timeoutSegundos) ||
                esperarElementoVisible(MENSAJE_ERROR_GLOBAL, timeoutSegundos);
    }

    // ==================== MÉTODOS DE VERIFICACIÓN DE SALUD ====================

    /**
     * Verifica el estado de salud específico de la página de registro.
     *
     * @return true si la página está en buen estado para registro
     */
    @Override
    public boolean verificarSaludPagina() {
        boolean saludBase = super.verificarSaludPagina();

        if (!saludBase) {
            return false;
        }

        // Verificaciones específicas de registro
        boolean saludRegistro = esFormularioVisible() &&
                sonCamposObligatoriosHabilitados() &&
                !hayErrorEmailDuplicado() &&
                !hayErrorPasswordDebil();

        logger.debug("Salud específica de registro: {}", saludRegistro);
        return saludRegistro;
    }

    /**
     * Limpia recursos específicos de la página de registro.
     */
    @Override
    public void limpiarRecursos() {
        super.limpiarRecursos();

        // Limpiar formulario si aún está visible
        if (esFormularioVisible()) {
            limpiarFormulario();
        }

        logger.debug("Recursos específicos de PaginaRegistro limpiados");
    }
}