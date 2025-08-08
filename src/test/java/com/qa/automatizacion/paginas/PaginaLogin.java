package com.qa.automatizacion.paginas;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Page Object para la página de login del sistema.
 * Encapsula todos los elementos y acciones relacionadas con la autenticación.
 *
 * Esta implementación utiliza:
 * - Herencia de PaginaBase para funcionalidades comunes
 * - Patrón Page Object para encapsular la UI
 * - Utileria centralizada para todas las operaciones
 * - Logging y trazabilidad unificados
 *
 * Responsabilidades:
 * - Gestionar la interacción con elementos de login
 * - Validar el estado de la página de login
 * - Proporcionar métodos de alto nivel para autenticación
 * - Manejar errores específicos de autenticación
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0
 */
public class PaginaLogin extends PaginaBase {

    // ==================== LOCALIZADORES ====================

    // Campos de entrada
    private static final By CAMPO_EMAIL = By.id("email");
    private static final By CAMPO_PASSWORD = By.id("password");

    // Botones
    private static final By BOTON_LOGIN = By.id("btn-login");
    private static final By BOTON_OLVIDAR_PASSWORD = By.linkText("¿Olvidaste tu contraseña?");
    private static final By ENLACE_REGISTRO = By.linkText("Registrarse");

    // Checkboxes y opciones
    private static final By CHECKBOX_RECORDAR_SESION = By.id("recordar-sesion");

    // Contenedores y estructura
    private static final By FORMULARIO_LOGIN = By.id("form-login");
    private static final By CONTENEDOR_LOGIN = By.cssSelector(".login-container, .auth-container");
    private static final By TITULO_PAGINA = By.cssSelector("h1, .login-title, [data-testid='login-title']");

    // Etiquetas y texto
    private static final By ETIQUETA_EMAIL = By.cssSelector("label[for='email']");
    private static final By ETIQUETA_PASSWORD = By.cssSelector("label[for='password']");

    // Indicadores de estado
    private static final By SPINNER_CARGA = By.cssSelector(".spinner, .loading, [data-testid='loading']");

    // Elementos usando @FindBy para demostrar ambos enfoques
    @FindBy(id = "email")
    private WebElement campoEmailElement;

    @FindBy(id = "password")
    private WebElement campoPasswordElement;

    @FindBy(id = "btn-login")
    private WebElement botonLoginElement;

    // ==================== CONSTRUCTOR ====================

    /**
     * Constructor que inicializa la página de login.
     */
    public PaginaLogin() {
        super();
        logger.info("PaginaLogin inicializada");
    }

    // ==================== MÉTODOS ABSTRACTOS IMPLEMENTADOS ====================

    /**
     * Verifica si la página de login está completamente cargada.
     *
     * @return true si la página está cargada, false en caso contrario
     */
    @Override
    public boolean esPaginaCargada() {
        try {
            boolean formularioPresente = esElementoPresente(FORMULARIO_LOGIN);
            boolean campoEmailPresente = esElementoPresente(CAMPO_EMAIL);
            boolean campoPasswordPresente = esElementoPresente(CAMPO_PASSWORD);
            boolean botonLoginPresente = esElementoPresente(BOTON_LOGIN);
            boolean tituloPresente = esElementoPresente(TITULO_PAGINA);

            boolean cargada = formularioPresente && campoEmailPresente &&
                    campoPasswordPresente && botonLoginPresente && tituloPresente;

            logger.debug("Verificación de carga - Formulario: {}, Email: {}, Password: {}, Botón: {}, Título: {}, Resultado: {}",
                    formularioPresente, campoEmailPresente, campoPasswordPresente, botonLoginPresente, tituloPresente, cargada);

            return cargada;

        } catch (Exception e) {
            logger.error("Error verificando carga de página login: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el título esperado de la página de login.
     *
     * @return título esperado
     */
    @Override
    public String obtenerTituloEsperado() {
        return "Login";
    }

    /**
     * Obtiene la URL esperada de la página de login.
     *
     * @return URL esperada
     */
    @Override
    public String obtenerUrlEsperada() {
        return utileria.obtenerUrlLogin();
    }

    // ==================== MÉTODOS DE ACCIONES PRINCIPALES ====================

    /**
     * Realiza el proceso completo de login.
     *
     * @param email email del usuario
     * @param password contraseña del usuario
     * @param recordarSesion si se debe recordar la sesión
     */
    public void realizarLogin(String email, String password, boolean recordarSesion) {
        logger.info("Iniciando proceso de login para usuario: {}", email);
        registrarAccion("Iniciando login completo");

        try {
            validarPaginaListaParaLogin();

            ingresarCredenciales(email, password);

            if (recordarSesion) {
                marcarRecordarSesion();
            }

            hacerClickLogin();

            logger.info("Proceso de login completado para usuario: {}", email);

        } catch (Exception e) {
            logger.error("Error en proceso de login: {}", e.getMessage());
            utileria.capturarScreenshotError("login_proceso_error");
            throw new RuntimeException("Error en login: " + e.getMessage(), e);
        }
    }

    /**
     * Realiza login básico con email y contraseña.
     *
     * @param email email del usuario
     * @param password contraseña del usuario
     */
    public void realizarLogin(String email, String password) {
        realizarLogin(email, password, false);
    }

    /**
     * Ingresa las credenciales de usuario.
     *
     * @param email email del usuario
     * @param password contraseña del usuario
     */
    public void ingresarCredenciales(String email, String password) {
        logger.info("Ingresando credenciales para usuario: {}", email);
        registrarAccion("Ingresando credenciales");

        ingresarEmail(email);
        ingresarPassword(password);

        logger.debug("Credenciales ingresadas exitosamente");
    }

    /**
     * Ingresa el email en el campo correspondiente.
     *
     * @param email email a ingresar
     */
    public void ingresarEmail(String email) {
        utileria.validarCadenaNoVacia(email, "Email");

        logger.debug("Ingresando email: {}", email);
        registrarAccion("Ingresando email");

        try {
            // Verificar formato de email
            if (!validarFormatoEmail(email)) {
                logger.warn("Formato de email inválido: {}", email);
            }

            ingresarTextoSeguro(CAMPO_EMAIL, email);

            // Verificar que se ingresó correctamente
            String emailIngresado = obtenerAtributo(CAMPO_EMAIL, "value");
            if (!email.equals(emailIngresado)) {
                throw new RuntimeException("Email no se ingresó correctamente");
            }

            logger.debug("Email ingresado correctamente: {}", email);

        } catch (Exception e) {
            logger.error("Error ingresando email: {}", e.getMessage());
            utileria.capturarScreenshotError("ingreso_email_error");
            throw new RuntimeException("Error ingresando email: " + e.getMessage(), e);
        }
    }

    /**
     * Ingresa la contraseña en el campo correspondiente.
     *
     * @param password contraseña a ingresar
     */
    public void ingresarPassword(String password) {
        utileria.validarCadenaNoVacia(password, "Password");

        logger.debug("Ingresando contraseña");
        registrarAccion("Ingresando contraseña");

        try {
            ingresarTextoSeguro(CAMPO_PASSWORD, password);

            // Verificar que el campo no esté vacío (sin mostrar la contraseña real)
            String passwordIngresado = obtenerAtributo(CAMPO_PASSWORD, "value");
            if (passwordIngresado == null || passwordIngresado.isEmpty()) {
                throw new RuntimeException("Contraseña no se ingresó correctamente");
            }

            logger.debug("Contraseña ingresada correctamente");

        } catch (Exception e) {
            logger.error("Error ingresando contraseña: {}", e.getMessage());
            utileria.capturarScreenshotError("ingreso_password_error");
            throw new RuntimeException("Error ingresando contraseña: " + e.getMessage(), e);
        }
    }

    /**
     * Hace clic en el botón de login.
     */
    public void hacerClickLogin() {
        logger.debug("Haciendo clic en botón de login");
        registrarAccion("Haciendo clic en botón login");

        try {
            // Verificar que el botón esté habilitado
            WebElement boton = buscarElemento(BOTON_LOGIN);
            if (!boton.isEnabled()) {
                throw new RuntimeException("Botón de login no está habilitado");
            }

            hacerClick(BOTON_LOGIN);

            // Esperar un momento para que inicie el proceso
            utileria.pausa(1000);

            logger.debug("Clic en botón login ejecutado");

        } catch (Exception e) {
            logger.error("Error haciendo clic en botón login: {}", e.getMessage());
            utileria.capturarScreenshotError("click_login_error");
            throw new RuntimeException("Error en clic login: " + e.getMessage(), e);
        }
    }

    /**
     * Marca el checkbox de recordar sesión.
     */
    public void marcarRecordarSesion() {
        logger.debug("Marcando checkbox recordar sesión");
        registrarAccion("Marcando recordar sesión");

        try {
            if (esElementoPresente(CHECKBOX_RECORDAR_SESION)) {
                WebElement checkbox = buscarElemento(CHECKBOX_RECORDAR_SESION);

                if (!checkbox.isSelected()) {
                    hacerClick(CHECKBOX_RECORDAR_SESION);
                    logger.debug("Checkbox recordar sesión marcado");
                } else {
                    logger.debug("Checkbox recordar sesión ya estaba marcado");
                }
            } else {
                logger.warn("Checkbox recordar sesión no encontrado en la página");
            }

        } catch (Exception e) {
            logger.error("Error marcando recordar sesión: {}", e.getMessage());
            throw new RuntimeException("Error marcando recordar sesión: " + e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS DE NAVEGACIÓN ====================

    /**
     * Navega a la página de registro.
     */
    public void navegarARegistro() {
        logger.info("Navegando a página de registro");
        registrarAccion("Navegando a registro");

        try {
            if (esElementoPresente(ENLACE_REGISTRO)) {
                hacerClick(ENLACE_REGISTRO);
                logger.debug("Navegación a registro ejecutada");
            } else {
                logger.warn("Enlace de registro no encontrado");
                throw new RuntimeException("Enlace de registro no disponible");
            }

        } catch (Exception e) {
            logger.error("Error navegando a registro: {}", e.getMessage());
            throw new RuntimeException("Error navegando a registro: " + e.getMessage(), e);
        }
    }

    /**
     * Navega a la página de recuperación de contraseña.
     */
    public void navegarARecuperarPassword() {
        logger.info("Navegando a recuperación de contraseña");
        registrarAccion("Navegando a recuperar contraseña");

        try {
            if (esElementoPresente(BOTON_OLVIDAR_PASSWORD)) {
                hacerClick(BOTON_OLVIDAR_PASSWORD);
                logger.debug("Navegación a recuperar contraseña ejecutada");
            } else {
                logger.warn("Enlace de recuperar contraseña no encontrado");
                throw new RuntimeException("Enlace de recuperar contraseña no disponible");
            }

        } catch (Exception e) {
            logger.error("Error navegando a recuperar contraseña: {}", e.getMessage());
            throw new RuntimeException("Error navegando a recuperar contraseña: " + e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Valida que la página esté lista para realizar login.
     *
     * @throws RuntimeException si la página no está lista
     */
    public void validarPaginaListaParaLogin() {
        logger.debug("Validando que la página esté lista para login");

        if (!esPaginaCargada()) {
            throw new RuntimeException("La página de login no está completamente cargada");
        }

        if (!esFormularioHabilitado()) {
            throw new RuntimeException("El formulario de login no está habilitado");
        }

        if (hayErroresPrevios()) {
            logger.warn("Hay errores previos en la página de login");
        }

        logger.debug("Página lista para realizar login");
    }

    /**
     * Verifica si el formulario de login está habilitado.
     *
     * @return true si está habilitado, false en caso contrario
     */
    public boolean esFormularioHabilitado() {
        try {
            WebElement campoEmail = buscarElemento(CAMPO_EMAIL);
            WebElement campoPassword = buscarElemento(CAMPO_PASSWORD);
            WebElement botonLogin = buscarElemento(BOTON_LOGIN);

            boolean habilitado = campoEmail.isEnabled() &&
                    campoPassword.isEnabled() &&
                    botonLogin.isEnabled();

            logger.debug("Estado del formulario - Email: {}, Password: {}, Botón: {}, Resultado: {}",
                    campoEmail.isEnabled(), campoPassword.isEnabled(), botonLogin.isEnabled(), habilitado);

            return habilitado;

        } catch (Exception e) {
            logger.error("Error verificando estado del formulario: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si hay errores previos en la página.
     *
     * @return true si hay errores, false en caso contrario
     */
    public boolean hayErroresPrevios() {
        return hayMensajeError();
    }

    /**
     * Valida las credenciales antes de intentar login.
     *
     * @param email email a validar
     * @param password contraseña a validar
     * @return true si las credenciales son válidas para el formato, false en caso contrario
     */
    public boolean validarCredenciales(String email, String password) {
        logger.debug("Validando formato de credenciales");

        try {
            // Validar email
            if (email == null || email.trim().isEmpty()) {
                logger.warn("Email está vacío");
                return false;
            }

            if (!validarFormatoEmail(email)) {
                logger.warn("Formato de email inválido: {}", email);
                return false;
            }

            // Validar contraseña
            if (password == null || password.trim().isEmpty()) {
                logger.warn("Contraseña está vacía");
                return false;
            }

            if (password.length() < 6) {
                logger.warn("Contraseña muy corta (mínimo 6 caracteres)");
                return false;
            }

            logger.debug("Credenciales válidas en formato");
            return true;

        } catch (Exception e) {
            logger.error("Error validando credenciales: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE ESTADO ====================

    /**
     * Verifica si el usuario ya está logueado (redirección automática).
     *
     * @return true si ya está logueado, false en caso contrario
     */
    public boolean esUsuarioYaLogueado() {
        try {
            // Si no estamos en la página de login, probablemente ya estamos logueados
            if (!utileria.verificarUrl("login")) {
                logger.debug("Usuario parece estar ya logueado (no en página login)");
                return true;
            }

            // Verificar si hay redirección automática
            utileria.pausa(2000);
            if (!utileria.verificarUrl("login")) {
                logger.debug("Usuario logueado automáticamente (redirección detectada)");
                return true;
            }

            return false;

        } catch (Exception e) {
            logger.error("Error verificando si usuario ya está logueado: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si hay un spinner de carga visible.
     *
     * @return true si hay loading, false en caso contrario
     */
    public boolean haySpinnerCarga() {
        return esElementoVisible(SPINNER_CARGA);
    }

    /**
     * Espera a que termine el proceso de login.
     *
     * @param timeoutSegundos timeout máximo en segundos
     */
    public void esperarProcesoLogin(int timeoutSegundos) {
        logger.debug("Esperando proceso de login (timeout: {} segundos)", timeoutSegundos);

        try {
            // Esperar que aparezca el spinner (si existe)
            if (esElementoPresente(SPINNER_CARGA)) {
                utileria.obtenerEspera(5).until(driver -> esElementoVisible(SPINNER_CARGA));
                logger.debug("Spinner de carga apareció");
            }

            // Esperar que desaparezca el spinner o que cambie la URL
            utileria.obtenerEspera(timeoutSegundos).until(driver ->
                    !esElementoVisible(SPINNER_CARGA) || !utileria.verificarUrl("login")
            );

            logger.debug("Proceso de login completado");

        } catch (Exception e) {
            logger.warn("Timeout esperando proceso de login: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE OBTENCIÓN DE DATOS ====================

    /**
     * Obtiene el valor actual del campo email.
     *
     * @return valor del campo email
     */
    public String obtenerEmailIngresado() {
        try {
            return obtenerAtributo(CAMPO_EMAIL, "value");
        } catch (Exception e) {
            logger.error("Error obteniendo email ingresado: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Obtiene el texto del título de la página.
     *
     * @return texto del título
     */
    public String obtenerTituloPagina() {
        try {
            if (esElementoPresente(TITULO_PAGINA)) {
                return obtenerTexto(TITULO_PAGINA);
            }
            return "";
        } catch (Exception e) {
            logger.error("Error obteniendo título de página: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Verifica si el checkbox de recordar sesión está marcado.
     *
     * @return true si está marcado, false en caso contrario
     */
    public boolean esRecordarSesionMarcado() {
        try {
            if (esElementoPresente(CHECKBOX_RECORDAR_SESION)) {
                WebElement checkbox = buscarElemento(CHECKBOX_RECORDAR_SESION);
                return checkbox.isSelected();
            }
            return false;
        } catch (Exception e) {
            logger.error("Error verificando estado de recordar sesión: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE LIMPIEZA ====================

    /**
     * Limpia los campos del formulario de login.
     */
    public void limpiarFormulario() {
        logger.debug("Limpiando formulario de login");
        registrarAccion("Limpiando formulario");

        try {
            if (esElementoPresente(CAMPO_EMAIL)) {
                WebElement campoEmail = buscarElemento(CAMPO_EMAIL);
                campoEmail.clear();
            }

            if (esElementoPresente(CAMPO_PASSWORD)) {
                WebElement campoPassword = buscarElemento(CAMPO_PASSWORD);
                campoPassword.clear();
            }

            // Desmarcar checkbox si está marcado
            if (esRecordarSesionMarcado()) {
                hacerClick(CHECKBOX_RECORDAR_SESION);
            }

            logger.debug("Formulario de login limpiado");

        } catch (Exception e) {
            logger.error("Error limpiando formulario: {}", e.getMessage());
        }
    }

    /**
     * Sobrescribe el método de limpieza base para incluir limpieza específica de login.
     */
    @Override
    public void limpiar() {
        super.limpiar();
        limpiarFormulario();
    }

    // ==================== MÉTODOS DE ESPERA ESPECÍFICOS ====================

    /**
     * Espera a que el formulario esté completamente cargado y listo.
     */
    public void esperarFormularioListo() {
        logger.debug("Esperando que el formulario esté listo");

        try {
            utileria.obtenerEspera(TIMEOUT_ELEMENTO).until(driver ->
                    esElementoPresente(CAMPO_EMAIL) &&
                            esElementoPresente(CAMPO_PASSWORD) &&
                            esElementoPresente(BOTON_LOGIN) &&
                            esFormularioHabilitado()
            );

            logger.debug("Formulario listo para uso");

        } catch (Exception e) {
            logger.error("Timeout esperando formulario listo: {}", e.getMessage());
            throw new RuntimeException("Formulario no está listo: " + e.getMessage(), e);
        }
    }

    /**
     * Espera a que aparezca un mensaje de error específico.
     *
     * @param timeoutSegundos timeout en segundos
     * @return true si aparece el mensaje, false si hay timeout
     */
    public boolean esperarMensajeError(int timeoutSegundos) {
        try {
            utileria.obtenerEspera(timeoutSegundos).until(driver -> hayMensajeError());
            logger.debug("Mensaje de error apareció");
            return true;

        } catch (Exception e) {
            logger.debug("No apareció mensaje de error en {} segundos", timeoutSegundos);
            return false;
        }
    }

    // ==================== MÉTODOS DE UTILIDAD ESPECÍFICOS ====================

    /**
     * Realiza login con manejo completo de errores y reintentos.
     *
     * @param email email del usuario
     * @param password contraseña del usuario
     * @param reintentos número máximo de reintentos
     * @return true si el login fue exitoso, false en caso contrario
     */
    public boolean intentarLoginConReintentos(String email, String password, int reintentos) {
        logger.info("Intentando login con reintentos (máximo: {}) para usuario: {}", reintentos, email);

        for (int intento = 1; intento <= reintentos; intento++) {
            try {
                logger.debug("Intento de login #{}", intento);

                limpiarFormulario();
                realizarLogin(email, password);
                esperarProcesoLogin(10);

                // Verificar si el login fue exitoso
                if (!utileria.verificarUrl("login")) {
                    logger.info("Login exitoso en intento #{}", intento);
                    return true;
                }

                // Si hay mensaje de error, registrarlo
                if (hayMensajeError()) {
                    String mensajeError = obtenerMensajeError();
                    logger.warn("Error en intento #{}: {}", intento, mensajeError);
                }

                if (intento < reintentos) {
                    logger.debug("Preparando reintento #{}", intento + 1);
                    utileria.pausa(1000);
                }

            } catch (Exception e) {
                logger.error("Error en intento de login #{}: {}", intento, e.getMessage());

                if (intento < reintentos) {
                    utileria.pausa(2000);
                    refrescarPagina();
                }
            }
        }

        logger.error("Login falló después de {} intentos", reintentos);
        return false;
    }

    /**
     * Obtiene información completa del estado actual de la página.
     *
     * @return información detallada del estado
     */
    public String obtenerEstadoCompleto() {
        try {
            return String.format(
                    "Estado Login - Cargada: %s | Formulario habilitado: %s | " +
                            "Email presente: %s | Password presente: %s | Botón presente: %s | " +
                            "Hay errores: %s | Recordar sesión: %s | %s",
                    esPaginaCargada(),
                    esFormularioHabilitado(),
                    esElementoPresente(CAMPO_EMAIL),
                    esElementoPresente(CAMPO_PASSWORD),
                    esElementoPresente(BOTON_LOGIN),
                    hayMensajeError(),
                    esRecordarSesionMarcado(),
                    obtenerInformacionDebug()
            );

        } catch (Exception e) {
            return "Error obteniendo estado completo: " + e.getMessage();
        }
    }
}