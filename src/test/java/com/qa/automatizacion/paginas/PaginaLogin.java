package com.qa.automatizacion.paginas;

import com.qa.automatizacion.modelo.Usuario;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.WebElement;

import java.util.Optional;

/**
 * Page Object para la página de login del sistema.
 * Implementa la nueva arquitectura optimizada usando PaginaBase y UtileriasComunes.
 *
 * Principios aplicados:
 * - Herencia: Extiende PaginaBase para reutilizar funcionalidades comunes
 * - DRY: No duplica código, reutiliza métodos de PaginaBase/UtileriasComunes
 * - Single Responsibility: Se enfoca únicamente en la funcionalidad de login
 * - Encapsulación: Expone métodos de alto nivel, oculta detalles de implementación
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0 - Optimizada con métodos reutilizables
 */
public class PaginaLogin extends PaginaBase {

    // ==================== LOCALIZADORES ====================

    // Campos de entrada
    private static final By CAMPO_EMAIL = By.id("email");
    private static final By CAMPO_PASSWORD = By.id("password");

    // Botones
    private static final By BOTON_LOGIN = By.id("btn-login");
    private static final By BOTON_OLVIDAR_PASSWORD = By.linkText("¿Olvidaste tu contraseña?");
    private static final By BOTON_MOSTRAR_PASSWORD = By.cssSelector("[data-testid='toggle-password']");

    // Checkboxes
    private static final By CHECKBOX_RECORDAR_SESION = By.id("recordar-sesion");

    // Mensajes específicos de login
    private static final By MENSAJE_ERROR_CREDENCIALES = By.cssSelector("[data-testid='error-credenciales']");
    private static final By MENSAJE_CUENTA_BLOQUEADA = By.cssSelector("[data-testid='cuenta-bloqueada']");
    private static final By MENSAJE_LOGIN_EXITOSO = By.cssSelector("[data-testid='login-exitoso']");

    // Formulario y contenedores
    private static final By FORMULARIO_LOGIN = By.id("form-login");
    private static final By CONTENEDOR_LOGIN = By.cssSelector(".login-container");

    // Enlaces
    private static final By ENLACE_REGISTRO = By.linkText("Registrarse");
    private static final By ENLACE_RECUPERAR_CUENTA = By.linkText("¿No puedes acceder a tu cuenta?");

    // Elementos usando @FindBy (opcionales, para demostrar compatibilidad)
    @FindBy(id = "email")
    private WebElement campoEmailElement;

    @FindBy(id = "password")
    private WebElement campoPasswordElement;

    @FindBy(id = "btn-login")
    private WebElement botonLoginElement;

    // ==================== CONSTRUCTORES ====================

    /**
     * Constructor que acepta un WebDriver específico.
     *
     * @param driver WebDriver a utilizar
     */
    public PaginaLogin(WebDriver driver) {
        super(driver);
    }

    /**
     * Constructor por defecto que usa el driver global.
     */
    public PaginaLogin() {
        super();
    }

    // ==================== MÉTODOS ABSTRACTOS IMPLEMENTADOS ====================

    @Override
    public boolean estaPaginaCargada() {
        // Verifica múltiples indicadores de que la página está cargada
        return esElementoVisible(FORMULARIO_LOGIN, 5) &&
                esElementoVisible(CAMPO_EMAIL, 2) &&
                esElementoVisible(CAMPO_PASSWORD, 2) &&
                esElementoVisible(BOTON_LOGIN, 2) &&
                !estaCargando();
    }

    @Override
    public String obtenerUrlBase() {
        return propiedades.obtenerUrlLogin();
    }

    @Override
    protected By[] obtenerLocalizadoresUnicos() {
        return new By[]{
                FORMULARIO_LOGIN,
                BOTON_LOGIN,
                CAMPO_EMAIL,
                CAMPO_PASSWORD
        };
    }

    // ==================== MÉTODOS PRINCIPALES DE LOGIN ====================

    /**
     * Realiza el proceso completo de login con credenciales básicas.
     *
     * @param email email del usuario
     * @param password contraseña del usuario
     * @return true si el login fue exitoso
     */
    public boolean iniciarSesion(String email, String password) {
        registrarAccion("Iniciando sesión", "Email: " + email);

        try {
            // Verificar que la página esté cargada
            if (!estaPaginaCargada()) {
                logger.error("La página de login no está completamente cargada");
                return false;
            }

            // Limpiar campos existentes e ingresar credenciales
            if (!ingresarCredenciales(email, password)) {
                logger.error("Error ingresando credenciales");
                return false;
            }

            // Hacer clic en el botón de login
            if (!hacerClicEnLogin()) {
                logger.error("Error haciendo clic en botón de login");
                return false;
            }

            // Verificar resultado del login
            return verificarResultadoLogin();

        } catch (Exception e) {
            logger.error("Error durante el proceso de login: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Realiza login con un objeto Usuario.
     *
     * @param usuario objeto Usuario con las credenciales
     * @return true si el login fue exitoso
     */
    public boolean iniciarSesion(Usuario usuario) {
        if (usuario == null) {
            logger.error("El objeto usuario no puede ser null");
            return false;
        }

        return iniciarSesion(usuario.getEmail(), usuario.getContrasena());
    }

    /**
     * Realiza login con opción de recordar sesión.
     *
     * @param email email del usuario
     * @param password contraseña del usuario
     * @param recordarSesion true para marcar "Recordar sesión"
     * @return true si el login fue exitoso
     */
    public boolean iniciarSesionConRecordar(String email, String password, boolean recordarSesion) {
        registrarAccion("Iniciando sesión con opción recordar",
                "Email: " + email, "Recordar: " + recordarSesion);

        // Primero ingresar credenciales
        if (!ingresarCredenciales(email, password)) {
            return false;
        }

        // Manejar checkbox de recordar sesión
        if (recordarSesion) {
            marcarRecordarSesion();
        } else {
            desmarcarRecordarSesion();
        }

        // Proceder con el login
        if (!hacerClicEnLogin()) {
            return false;
        }

        return verificarResultadoLogin();
    }

    // ==================== MÉTODOS DE INGRESO DE DATOS ====================

    /**
     * Ingresa las credenciales en los campos correspondientes.
     *
     * @param email email a ingresar
     * @param password contraseña a ingresar
     * @return true si las credenciales se ingresaron correctamente
     */
    public boolean ingresarCredenciales(String email, String password) {
        registrarAccion("Ingresando credenciales", "Email: " + email);

        // Validar parámetros
        if (email == null || email.trim().isEmpty()) {
            logger.error("Email no puede ser vacío o null");
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            logger.error("Password no puede ser vacío o null");
            return false;
        }

        // Ingresar email
        if (!ingresarEmail(email)) {
            logger.error("Error ingresando email");
            return false;
        }

        // Ingresar contraseña
        if (!ingresarPassword(password)) {
            logger.error("Error ingresando contraseña");
            return false;
        }

        logger.info("Credenciales ingresadas exitosamente");
        return true;
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

    // ==================== MÉTODOS DE INTERACCIÓN CON ELEMENTOS ====================

    /**
     * Hace clic en el botón de login.
     *
     * @return true si el clic fue exitoso
     */
    public boolean hacerClicEnLogin() {
        registrarAccion("Haciendo clic en botón de login");
        return hacerClicSeguro(BOTON_LOGIN, 3);
    }

    /**
     * Hace clic en el enlace de "Olvidé mi contraseña".
     *
     * @return true si el clic fue exitoso
     */
    public boolean hacerClicEnOlvidoPassword() {
        registrarAccion("Haciendo clic en 'Olvidé mi contraseña'");
        return hacerClicSeguro(BOTON_OLVIDAR_PASSWORD);
    }

    /**
     * Hace clic en el enlace de registro.
     *
     * @return true si el clic fue exitoso
     */
    public boolean hacerClicEnRegistro() {
        registrarAccion("Haciendo clic en enlace de registro");
        return hacerClicSeguro(ENLACE_REGISTRO);
    }

    /**
     * Marca el checkbox de "Recordar sesión".
     *
     * @return true si se marcó correctamente
     */
    public boolean marcarRecordarSesion() {
        registrarAccion("Marcando 'Recordar sesión'");

        if (!esElementoSeleccionado(CHECKBOX_RECORDAR_SESION)) {
            return hacerClicSeguro(CHECKBOX_RECORDAR_SESION);
        }

        logger.debug("Checkbox 'Recordar sesión' ya estaba marcado");
        return true;
    }

    /**
     * Desmarca el checkbox de "Recordar sesión".
     *
     * @return true si se desmarcó correctamente
     */
    public boolean desmarcarRecordarSesion() {
        registrarAccion("Desmarcando 'Recordar sesión'");

        if (esElementoSeleccionado(CHECKBOX_RECORDAR_SESION)) {
            return hacerClicSeguro(CHECKBOX_RECORDAR_SESION);
        }

        logger.debug("Checkbox 'Recordar sesión' ya estaba desmarcado");
        return true;
    }

    /**
     * Togglea la visibilidad de la contraseña.
     *
     * @return true si se cambió la visibilidad
     */
    public boolean toggleVisibilidadPassword() {
        registrarAccion("Cambiando visibilidad de contraseña");
        return hacerClicSeguro(BOTON_MOSTRAR_PASSWORD);
    }

    // ==================== MÉTODOS DE VERIFICACIÓN ====================

    /**
     * Verifica el resultado del intento de login.
     *
     * @return true si el login fue exitoso
     */
    private boolean verificarResultadoLogin() {
        registrarAccion("Verificando resultado de login");

        // Esperar un momento para que aparezcan los mensajes
        esperarSegundos(2);

        // Verificar si hay mensaje de éxito
        if (esElementoVisible(MENSAJE_LOGIN_EXITOSO, 3)) {
            logger.info("Login exitoso - mensaje de confirmación visible");
            return true;
        }

        // Verificar si cambió la URL (redirección exitosa)
        String urlActual = obtenerUrlActual();
        if (!urlActual.contains("/login")) {
            logger.info("Login exitoso - redirección detectada a: {}", urlActual);
            return true;
        }

        // Verificar si hay mensajes de error específicos
        if (hayErrorCredenciales()) {
            logger.error("Login fallido - credenciales incorrectas");
            return false;
        }

        if (hayCuentaBloqueada()) {
            logger.error("Login fallido - cuenta bloqueada");
            return false;
        }

        if (hayMensajesError()) {
            Optional<String> mensajeError = obtenerMensajeError();
            logger.error("Login fallido - error general: {}", mensajeError.orElse("Error desconocido"));
            return false;
        }

        // Si no hay mensajes claros, verificar si seguimos en login
        if (estaPaginaCargada()) {
            logger.error("Login fallido - aún en página de login sin mensajes claros");
            return false;
        }

        logger.info("Login aparentemente exitoso");
        return true;
    }

    /**
     * Verifica si hay errores de credenciales.
     *
     * @return true si hay errores de credenciales
     */
    public boolean hayErrorCredenciales() {
        return esElementoVisible(MENSAJE_ERROR_CREDENCIALES, 2);
    }

    /**
     * Verifica si la cuenta está bloqueada.
     *
     * @return true si la cuenta está bloqueada
     */
    public boolean hayCuentaBloqueada() {
        return esElementoVisible(MENSAJE_CUENTA_BLOQUEADA, 2);
    }

    /**
     * Verifica si el formulario de login está visible.
     *
     * @return true si el formulario está visible
     */
    public boolean esFormularioVisible() {
        return esElementoVisible(FORMULARIO_LOGIN, 2);
    }

    /**
     * Verifica si el campo de email está habilitado.
     *
     * @return true si está habilitado
     */
    public boolean esCampoEmailHabilitado() {
        return esElementoHabilitado(CAMPO_EMAIL);
    }

    /**
     * Verifica si el campo de contraseña está habilitado.
     *
     * @return true si está habilitado
     */
    public boolean esCampoPasswordHabilitado() {
        return esElementoHabilitado(CAMPO_PASSWORD);
    }

    /**
     * Verifica si el botón de login está habilitado.
     *
     * @return true si está habilitado
     */
    public boolean esBotonLoginHabilitado() {
        return esElementoHabilitado(BOTON_LOGIN);
    }

    /**
     * Verifica si el checkbox de recordar sesión está marcado.
     *
     * @return true si está marcado
     */
    public boolean estaRecordarSesionMarcado() {
        return esElementoSeleccionado(CHECKBOX_RECORDAR_SESION);
    }

    // ==================== MÉTODOS DE OBTENCIÓN DE DATOS ====================

    /**
     * Obtiene el valor actual del campo de email.
     *
     * @return Optional con el valor del email
     */
    public Optional<String> obtenerValorEmail() {
        return obtenerValorCampo(CAMPO_EMAIL);
    }

    /**
     * Obtiene el valor actual del campo de contraseña.
     *
     * @return Optional con el valor de la contraseña
     */
    public Optional<String> obtenerValorPassword() {
        return obtenerValorCampo(CAMPO_PASSWORD);
    }

    /**
     * Obtiene el mensaje de error de credenciales si está presente.
     *
     * @return Optional con el mensaje de error
     */
    public Optional<String> obtenerMensajeErrorCredenciales() {
        if (hayErrorCredenciales()) {
            return obtenerTextoElemento(MENSAJE_ERROR_CREDENCIALES);
        }
        return Optional.empty();
    }

    /**
     * Obtiene el mensaje de cuenta bloqueada si está presente.
     *
     * @return Optional con el mensaje de cuenta bloqueada
     */
    public Optional<String> obtenerMensajeCuentaBloqueada() {
        if (hayCuentaBloqueada()) {
            return obtenerTextoElemento(MENSAJE_CUENTA_BLOQUEADA);
        }
        return Optional.empty();
    }

    /**
     * Obtiene el texto del botón de login.
     *
     * @return Optional con el texto del botón
     */
    public Optional<String> obtenerTextoBotonLogin() {
        return obtenerTextoElemento(BOTON_LOGIN);
    }

    // ==================== MÉTODOS DE VALIDACIÓN DE CAMPOS ====================

    /**
     * Valida el formato del email ingresado.
     *
     * @param email email a validar
     * @return true si el formato es válido
     */
    public boolean esFormatoEmailValido(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String patronEmail = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        boolean valido = email.matches(patronEmail);

        registrarAccion("Validando formato de email", "Email: " + email, "Válido: " + valido);
        return valido;
    }

    /**
     * Valida la fortaleza de la contraseña.
     *
     * @param password contraseña a validar
     * @return true si cumple los criterios mínimos
     */
    public boolean esPasswordSeguro(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        // Validaciones básicas de seguridad
        boolean tieneMayuscula = password.matches(".*[A-Z].*");
        boolean tieneMinuscula = password.matches(".*[a-z].*");
        boolean tieneNumero = password.matches(".*\\d.*");
        boolean tieneCaracterEspecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        boolean seguro = tieneMayuscula && tieneMinuscula && tieneNumero && tieneCaracterEspecial;

        registrarAccion("Validando fortaleza de contraseña", "Seguro: " + seguro);
        return seguro;
    }

    // ==================== MÉTODOS DE ESPERA ESPECÍFICOS ====================

    /**
     * Espera que aparezca un mensaje de error específico.
     *
     * @param timeoutSegundos timeout en segundos
     * @return true si apareció el mensaje de error
     */
    public boolean esperarMensajeError(int timeoutSegundos) {
        return esperarElementoVisible(MENSAJE_ERROR_CREDENCIALES, timeoutSegundos) ||
                esperarElementoVisible(MENSAJE_CUENTA_BLOQUEADA, timeoutSegundos) ||
                esperarElementoVisible(MENSAJE_ERROR_GLOBAL, timeoutSegundos);
    }

    /**
     * Espera que aparezca el mensaje de login exitoso.
     *
     * @param timeoutSegundos timeout en segundos
     * @return true si apareció el mensaje de éxito
     */
    public boolean esperarMensajeExito(int timeoutSegundos) {
        return esperarElementoVisible(MENSAJE_LOGIN_EXITOSO, timeoutSegundos);
    }

    /**
     * Espera que se complete la redirección después del login.
     *
     * @param timeoutSegundos timeout en segundos
     * @return true si hubo redirección
     */
    public boolean esperarRedireccionLogin(int timeoutSegundos) {
        String urlInicial = obtenerUrlActual();

        for (int i = 0; i < timeoutSegundos; i++) {
            esperarSegundos(1);
            String urlActual = obtenerUrlActual();

            if (!urlActual.equals(urlInicial) && !urlActual.contains("/login")) {
                logger.info("Redirección detectada a: {}", urlActual);
                return true;
            }
        }

        logger.debug("No se detectó redirección en {} segundos", timeoutSegundos);
        return false;
    }

    // ==================== MÉTODOS DE LIMPIEZA Y RESET ====================

    /**
     * Limpia todos los campos del formulario de login.
     */
    public void limpiarFormulario() {
        registrarAccion("Limpiando formulario de login");

        // Limpiar campo de email
        Optional<String> valorEmail = obtenerValorEmail();
        if (valorEmail.isPresent() && !valorEmail.get().isEmpty()) {
            ingresarTextoSeguro(CAMPO_EMAIL, "", true);
        }

        // Limpiar campo de contraseña
        Optional<String> valorPassword = obtenerValorPassword();
        if (valorPassword.isPresent() && !valorPassword.get().isEmpty()) {
            ingresarTextoSeguro(CAMPO_PASSWORD, "", true);
        }

        // Desmarcar checkbox si está marcado
        if (estaRecordarSesionMarcado()) {
            desmarcarRecordarSesion();
        }

        logger.info("Formulario de login limpiado");
    }

    /**
     * Resetea la página de login a su estado inicial.
     */
    public void resetearPagina() {
        registrarAccion("Reseteando página de login");

        limpiarFormulario();
        actualizarPagina();
        esperarCargaPagina();

        logger.info("Página de login reseteada");
    }

    // ==================== MÉTODOS DE ACCIONES COMPLEJAS ====================

    /**
     * Intenta múltiples logins con diferentes credenciales.
     * Útil para pruebas de seguridad y robustez.
     *
     * @param credenciales array de arrays [email, password]
     * @param limpiarEntreIntentos true para limpiar formulario entre intentos
     * @return índice del intento exitoso, -1 si ninguno fue exitoso
     */
    public int intentarMultiplesLogins(String[][] credenciales, boolean limpiarEntreIntentos) {
        registrarAccion("Intentando múltiples logins", "Cantidad: " + credenciales.length);

        for (int i = 0; i < credenciales.length; i++) {
            String email = credenciales[i][0];
            String password = credenciales[i][1];

            logger.info("Intento de login {} de {}: {}", i + 1, credenciales.length, email);

            if (limpiarEntreIntentos && i > 0) {
                limpiarFormulario();
                esperarSegundos(1);
            }

            if (iniciarSesion(email, password)) {
                logger.info("Login exitoso en intento {} con credenciales: {}", i + 1, email);
                return i;
            }

            logger.debug("Login fallido en intento {} con credenciales: {}", i + 1, email);
            esperarSegundos(2); // Evitar intentos demasiado rápidos
        }

        logger.info("Todos los intentos de login fallaron");
        return -1;
    }

    /**
     * Realiza login con manejo automático de errores.
     * Reintenta automáticamente en caso de errores transitorios.
     *
     * @param email email del usuario
     * @param password contraseña del usuario
     * @param maxReintentos número máximo de reintentos
     * @return true si el login fue exitoso
     */
    public boolean loginConReintentos(String email, String password, int maxReintentos) {
        registrarAccion("Login con reintentos", "Email: " + email, "Max reintentos: " + maxReintentos);

        for (int intento = 1; intento <= maxReintentos; intento++) {
            logger.info("Intento de login {} de {}", intento, maxReintentos);

            try {
                if (iniciarSesion(email, password)) {
                    logger.info("Login exitoso en intento {}", intento);
                    return true;
                }

                // Verificar si es un error recuperable
                if (hayCuentaBloqueada()) {
                    logger.error("Cuenta bloqueada - no se reintentará");
                    return false;
                }

                if (intento < maxReintentos) {
                    logger.info("Login fallido, reintentando en 3 segundos...");
                    esperarSegundos(3);
                    limpiarFormulario();
                }

            } catch (Exception e) {
                logger.error("Error en intento {} de login: {}", intento, e.getMessage());
                if (intento < maxReintentos) {
                    esperarSegundos(2);
                    actualizarPagina();
                    esperarCargaPagina();
                }
            }
        }

        logger.error("Login fallido después de {} intentos", maxReintentos);
        return false;
    }

    // ==================== MÉTODOS DE DEBUGGING Y DIAGNÓSTICO ====================

    /**
     * Obtiene información de diagnóstico específica de la página de login.
     *
     * @return String con información detallada
     */
    @Override
    public String obtenerInformacionDiagnostico() {
        StringBuilder diagnostico = new StringBuilder(super.obtenerInformacionDiagnostico());

        diagnostico.append("\n=== DIAGNÓSTICO PÁGINA LOGIN ===\n");
        diagnostico.append("Formulario visible: ").append(esFormularioVisible()).append("\n");
        diagnostico.append("Campo email habilitado: ").append(esCampoEmailHabilitado()).append("\n");
        diagnostico.append("Campo password habilitado: ").append(esCampoPasswordHabilitado()).append("\n");
        diagnostico.append("Botón login habilitado: ").append(esBotonLoginHabilitado()).append("\n");
        diagnostico.append("Recordar sesión marcado: ").append(estaRecordarSesionMarcado()).append("\n");
        diagnostico.append("Hay errores credenciales: ").append(hayErrorCredenciales()).append("\n");
        diagnostico.append("Hay cuenta bloqueada: ").append(hayCuentaBloqueada()).append("\n");

        obtenerValorEmail().ifPresent(email ->
                diagnostico.append("Valor email actual: ").append(email).append("\n"));

        obtenerMensajeErrorCredenciales().ifPresent(mensaje ->
                diagnostico.append("Mensaje error credenciales: ").append(mensaje).append("\n"));

        obtenerMensajeCuentaBloqueada().ifPresent(mensaje ->
                diagnostico.append("Mensaje cuenta bloqueada: ").append(mensaje).append("\n"));

        diagnostico.append("================================\n");

        return diagnostico.toString();
    }

    /**
     * Toma screenshot específico del formulario de login.
     *
     * @return byte array con la imagen del formulario
     */
    public byte[] tomarCapturaFormulario() {
        registrarAccion("Tomando captura del formulario de login");
        return tomarCapturaElemento(FORMULARIO_LOGIN);
    }

    // ==================== MÉTODOS DE LIMPIEZA DE RECURSOS ====================

    /**
     * Limpia recursos específicos de la página de login.
     */
    @Override
    public void limpiarRecursos() {
        super.limpiarRecursos();

        // Limpiar formulario si aún está visible
        if (esFormularioVisible()) {
            limpiarFormulario();
        }

        logger.debug("Recursos específicos de PaginaLogin limpiados");
    }

    // ==================== MÉTODOS DE VERIFICACIÓN DE SALUD ESPECÍFICOS ====================

    /**
     * Verifica el estado de salud específico de la página de login.
     *
     * @return true si la página está en buen estado para login
     */
    @Override
    public boolean verificarSaludPagina() {
        boolean saludBase = super.verificarSaludPagina();

        if (!saludBase) {
            return false;
        }

        // Verificaciones específicas de login
        boolean saludLogin = esFormularioVisible() &&
                esCampoEmailHabilitado() &&
                esCampoPasswordHabilitado() &&
                esBotonLoginHabilitado() &&
                !hayCuentaBloqueada();

        logger.debug("Salud específica de login: {}", saludLogin);
        return saludLogin;
    }
}