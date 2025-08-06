package com.qa.automatizacion.paginas;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * Page Object para la página de login del sistema.
 * Encapsula todos los elementos y acciones relacionadas con la autenticación.
 *
 * Principios aplicados:
 * - Page Object Pattern: Separa la lógica de UI de los tests
 * - Encapsulación: Oculta los detalles de implementación de Selenium
 * - Single Responsibility: Se enfoca únicamente en la página de login
 */
public class PaginaLogin extends PaginaBase {

    // ==================== LOCALIZADORES ====================

    // Campos de entrada
    private static final By CAMPO_EMAIL = By.id("email");
    private static final By CAMPO_PASSWORD = By.id("password");

    // Botones
    private static final By BOTON_LOGIN = By.id("btn-login");
    private static final By BOTON_OLVIDAR_PASSWORD = By.linkText("¿Olvidaste tu contraseña?");

    // Checkboxes
    private static final By CHECKBOX_RECORDAR_SESION = By.id("recordar-sesion");

    // Mensajes
    private static final By MENSAJE_ERROR = By.cssSelector(".alert-error, .error-message, [data-testid='error-message']");
    private static final By MENSAJE_EXITO = By.cssSelector(".alert-success, .success-message, [data-testid='success-message']");

    // Formulario y contenedores
    private static final By FORMULARIO_LOGIN = By.id("form-login");
    private static final By CONTENEDOR_LOGIN = By.cssSelector(".login-container, .auth-container");

    // Títulos y etiquetas
    private static final By TITULO_PAGINA = By.cssSelector("h1, .login-title, [data-testid='login-title']");
    private static final By ETIQUETA_EMAIL = By.cssSelector("label[for='email']");
    private static final By ETIQUETA_PASSWORD = By.cssSelector("label[for='password']");

    // Enlaces
    private static final By ENLACE_REGISTRO = By.linkText("Registrarse");

    // Indicadores de carga
    private static final By SPINNER_CARGA = By.cssSelector(".spinner, .loading, [data-testid='loading']");

    // ==================== MÉTODOS PRINCIPALES ====================

    /**
     * Verifica si la página de login está completamente cargada.
     *
     * @return true si la página está cargada
     */
    @Override
    public boolean estaPaginaCargada() {
        registrarAccion("Verificando carga de página de login");

        try {
            // Verificar elementos esenciales
            boolean formularioVisible = esElementoVisible(FORMULARIO_LOGIN) ||
                    esElementoVisible(CONTENEDOR_LOGIN);
            boolean camposPresentes = esElementoPresente(CAMPO_EMAIL) &&
                    esElementoPresente(CAMPO_PASSWORD);
            boolean botonPresente = esElementoPresente(BOTON_LOGIN);

            boolean paginaCargada = formularioVisible && camposPresentes && botonPresente;

            logger.debug("Página login cargada: {} (Formulario: {}, Campos: {}, Botón: {})",
                    paginaCargada, formularioVisible, camposPresentes, botonPresente);

            return paginaCargada;

        } catch (Exception e) {
            logger.error("Error verificando carga de página login: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la URL esperada para la página de login.
     *
     * @return URL esperada
     */
    @Override
    public String obtenerUrlEsperada() {
        return propiedades.obtenerUrlLogin();
    }

    // ==================== MÉTODOS DE INTERACCIÓN ====================

    /**
     * Ingresa el email en el campo correspondiente.
     *
     * @param email email a ingresar
     */
    public void ingresarEmail(String email) {
        registrarAccion("Ingresando email: " + email);

        try {
            ingresarTextoSeguro(CAMPO_EMAIL, email);
            logger.debug("Email ingresado exitosamente: {}", email);
        } catch (Exception e) {
            logger.error("Error ingresando email '{}': {}", email, e.getMessage());
            throw new RuntimeException("No se pudo ingresar el email", e);
        }
    }

    /**
     * Ingresa la contraseña en el campo correspondiente.
     *
     * @param password contraseña a ingresar
     */
    public void ingresarPassword(String password) {
        registrarAccion("Ingresando contraseña");

        try {
            ingresarTextoSeguro(CAMPO_PASSWORD, password);
            logger.debug("Contraseña ingresada exitosamente");
        } catch (Exception e) {
            logger.error("Error ingresando contraseña: {}", e.getMessage());
            throw new RuntimeException("No se pudo ingresar la contraseña", e);
        }
    }

    /**
     * Hace clic en el botón de login.
     */
    public void hacerClickBotonLogin() {
        registrarAccion("Haciendo clic en botón de login");

        try {
            hacerClickSeguro(BOTON_LOGIN);

            // Esperar a que desaparezca el spinner de carga si existe
            if (esElementoVisible(SPINNER_CARGA)) {
                esperarElementoDesaparezca(SPINNER_CARGA);
            }

            logger.debug("Clic en botón login ejecutado exitosamente");
        } catch (Exception e) {
            logger.error("Error haciendo clic en botón login: {}", e.getMessage());
            throw new RuntimeException("No se pudo hacer clic en el botón de login", e);
        }
    }

    /**
     * Marca o desmarca el checkbox de "Recordar sesión".
     *
     * @param marcar true para marcar, false para desmarcar
     */
    public void marcarRecordarSesion(boolean marcar) {
        registrarAccion("Configurando 'Recordar sesión': " + marcar);

        try {
            WebElement checkbox = esperarElementoClickeable(CHECKBOX_RECORDAR_SESION);
            boolean estaSeleccionado = checkbox.isSelected();

            if (estaSeleccionado != marcar) {
                checkbox.click();
                logger.debug("Checkbox 'Recordar sesión' cambiado a: {}", marcar);
            } else {
                logger.debug("Checkbox 'Recordar sesión' ya estaba en estado: {}", marcar);
            }
        } catch (Exception e) {
            logger.error("Error configurando 'Recordar sesión': {}", e.getMessage());
            throw new RuntimeException("No se pudo configurar 'Recordar sesión'", e);
        }
    }

    /**
     * Marca el checkbox de "Recordar sesión".
     */
    public void marcarRecordarSesion() {
        marcarRecordarSesion(true);
    }

    /**
     * Hace clic en el enlace "¿Olvidaste tu contraseña?".
     */
    public void hacerClickOlvidarPassword() {
        registrarAccion("Haciendo clic en '¿Olvidaste tu contraseña?'");

        try {
            hacerClickSeguro(BOTON_OLVIDAR_PASSWORD);
            logger.debug("Clic en 'Olvidar contraseña' ejecutado");
        } catch (Exception e) {
            logger.error("Error haciendo clic en 'Olvidar contraseña': {}", e.getMessage());
            throw new RuntimeException("No se pudo hacer clic en 'Olvidar contraseña'", e);
        }
    }

    /**
     * Hace clic en el enlace de registro.
     */
    public void hacerClickRegistro() {
        registrarAccion("Haciendo clic en enlace de registro");

        try {
            hacerClickSeguro(ENLACE_REGISTRO);
            logger.debug("Clic en enlace de registro ejecutado");
        } catch (Exception e) {
            logger.error("Error haciendo clic en enlace de registro: {}", e.getMessage());
            throw new RuntimeException("No se pudo hacer clic en enlace de registro", e);
        }
    }

    /**
     * Limpia todos los campos del formulario.
     */
    public void limpiarCampos() {
        registrarAccion("Limpiando campos del formulario");

        try {
            limpiarCampo(CAMPO_EMAIL);
            limpiarCampo(CAMPO_PASSWORD);
            logger.debug("Campos del formulario limpiados");
        } catch (Exception e) {
            logger.error("Error limpiando campos: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE VERIFICACIÓN ====================

    /**
     * Verifica si el formulario de login es visible.
     *
     * @return true si el formulario es visible
     */
    public boolean esFormularioVisible() {
        return esElementoVisible(FORMULARIO_LOGIN) || esElementoVisible(CONTENEDOR_LOGIN);
    }

    /**
     * Verifica si el botón de login es visible.
     *
     * @return true si el botón es visible
     */
    public boolean esBotonLoginVisible() {
        return esElementoVisible(BOTON_LOGIN);
    }

    /**
     * Verifica si el botón de login está habilitado.
     *
     * @return true si el botón está habilitado
     */
    public boolean esBotonLoginHabilitado() {
        return esElementoHabilitado(BOTON_LOGIN);
    }

    /**
     * Verifica si el campo de email está vacío.
     *
     * @return true si el campo está vacío
     */
    public boolean esCampoEmailVacio() {
        return esCampoVacio(CAMPO_EMAIL);
    }

    /**
     * Verifica si el campo de contraseña está vacío.
     *
     * @return true si el campo está vacío
     */
    public boolean esCampoPasswordVacio() {
        return esCampoVacio(CAMPO_PASSWORD);
    }

    /**
     * Verifica si el checkbox "Recordar sesión" está marcado.
     *
     * @return true si está marcado
     */
    public boolean estaRecordarSesionMarcado() {
        return esElementoSeleccionado(CHECKBOX_RECORDAR_SESION);
    }

    /**
     * Verifica si un mensaje de error es visible.
     *
     * @return true si hay un mensaje de error visible
     */
    public boolean esMensajeErrorVisible() {
        return esElementoVisible(MENSAJE_ERROR);
    }

    /**
     * Verifica si un mensaje de éxito es visible.
     *
     * @return true si hay un mensaje de éxito visible
     */
    public boolean esMensajeExitoVisible() {
        return esElementoVisible(MENSAJE_EXITO);
    }

    // ==================== MÉTODOS DE OBTENCIÓN DE DATOS ====================

    /**
     * Obtiene el mensaje de error actual.
     *
     * @return texto del mensaje de error
     */
    public String obtenerMensajeError() {
        try {
            return obtenerTexto(MENSAJE_ERROR);
        } catch (Exception e) {
            logger.debug("No se pudo obtener mensaje de error: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Obtiene el mensaje de éxito actual.
     *
     * @return texto del mensaje de éxito
     */
    public String obtenerMensajeExito() {
        try {
            return obtenerTexto(MENSAJE_EXITO);
        } catch (Exception e) {
            logger.debug("No se pudo obtener mensaje de éxito: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Obtiene el título de la página de login.
     *
     * @return título de la página
     */
    public String obtenerTituloLogin() {
        try {
            return obtenerTexto(TITULO_PAGINA);
        } catch (Exception e) {
            logger.debug("No se pudo obtener título de login: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Obtiene el valor actual del campo email.
     *
     * @return valor del campo email
     */
    public String obtenerValorEmail() {
        return obtenerAtributo(CAMPO_EMAIL, "value");
    }

    /**
     * Obtiene el valor actual del campo contraseña.
     *
     * @return valor del campo contraseña
     */
    public String obtenerValorPassword() {
        return obtenerAtributo(CAMPO_PASSWORD, "value");
    }

    // ==================== MÉTODOS DE NAVEGACIÓN POR TECLADO ====================

    /**
     * Navega entre los campos usando la tecla Tab.
     */
    public void navegarConTab() {
        registrarAccion("Navegando con tecla Tab");

        try {
            // Enfocar primer campo
            WebElement campoEmail = esperarElementoClickeable(CAMPO_EMAIL);
            campoEmail.click();

            // Navegar con Tab
            campoEmail.sendKeys(Keys.TAB);

            logger.debug("Navegación con Tab ejecutada");
        } catch (Exception e) {
            logger.error("Error navegando con Tab: {}", e.getMessage());
        }
    }

    /**
     * Verifica si un campo específico tiene el foco en el orden esperado.
     *
     * @param nombreCampo nombre del campo a verificar
     * @param ordenEsperado orden esperado del campo
     * @return true si el campo tiene el foco en el orden correcto
     */
    public boolean verificarEnfoqueCampo(String nombreCampo, int ordenEsperado) {
        try {
            By localizadorCampo = switch (nombreCampo) {
                case "Campo email" -> CAMPO_EMAIL;
                case "Campo contraseña" -> CAMPO_PASSWORD;
                case "Checkbox recordar" -> CHECKBOX_RECORDAR_SESION;
                case "Botón iniciar sesión" -> BOTON_LOGIN;
                default -> throw new IllegalArgumentException("Campo no reconocido: " + nombreCampo);
            };

            WebElement elemento = driver.findElement(localizadorCampo);
            WebElement elementoEnfocado = driver.switchTo().activeElement();

            boolean tieneEnfoque = elemento.equals(elementoEnfocado);
            logger.debug("Campo '{}' tiene enfoque en orden {}: {}", nombreCampo, ordenEsperado, tieneEnfoque);

            return tieneEnfoque;

        } catch (Exception e) {
            logger.error("Error verificando enfoque de campo '{}': {}", nombreCampo, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si el foco visual es claramente visible.
     *
     * @return true si el foco visual es visible
     */
    public boolean esFocoVisualVisible() {
        try {
            WebElement elementoEnfocado = driver.switchTo().activeElement();

            // Verificar si el elemento tiene estilos de foco
            String outline = elementoEnfocado.getCssValue("outline");
            String boxShadow = elementoEnfocado.getCssValue("box-shadow");
            String border = elementoEnfocado.getCssValue("border");

            boolean focoVisible = !outline.equals("none") ||
                    !boxShadow.equals("none") ||
                    border.contains("focus");

            logger.debug("Foco visual visible: {} (outline: {}, box-shadow: {}, border: {})",
                    focoVisible, outline, boxShadow, border);

            return focoVisible;

        } catch (Exception e) {
            logger.error("Error verificando foco visual: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE UTILIDAD ESPECÍFICOS ====================

    /**
     * Realiza un login completo con credenciales específicas.
     *
     * @param email email del usuario
     * @param password contraseña del usuario
     * @param recordarSesion si debe marcar "recordar sesión"
     */
    public void realizarLoginCompleto(String email, String password, boolean recordarSesion) {
        registrarAccion(String.format("Realizando login completo para: %s (recordar: %s)",
                email, recordarSesion));

        try {
            // Limpiar campos previos
            limpiarCampos();

            // Ingresar credenciales
            ingresarEmail(email);
            ingresarPassword(password);

            // Configurar recordar sesión si es necesario
            if (recordarSesion) {
                marcarRecordarSesion(true);
            }

            // Hacer clic en login
            hacerClickBotonLogin();

            logger.info("Login completo ejecutado para: {}", email);

        } catch (Exception e) {
            logger.error("Error en login completo para '{}': {}", email, e.getMessage());
            throw new RuntimeException("Login completo falló", e);
        }
    }

    /**
     * Realiza un login rápido con credenciales predeterminadas.
     *
     * @param email email del usuario
     * @param password contraseña del usuario
     */
    public void realizarLoginRapido(String email, String password) {
        realizarLoginCompleto(email, password, false);
    }

    /**
     * Verifica si la página muestra indicadores de carga.
     *
     * @return true si hay indicadores de carga visibles
     */
    public boolean estaCargando() {
        return esElementoVisible(SPINNER_CARGA);
    }

    /**
     * Espera a que termine la carga de la página.
     *
     * @return true si la carga terminó exitosamente
     */
    public boolean esperarFinDeCarga() {
        try {
            if (esElementoVisible(SPINNER_CARGA)) {
                return esperarElementoDesaparezca(SPINNER_CARGA);
            }
            return true;
        } catch (Exception e) {
            logger.error("Error esperando fin de carga: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica todos los elementos de accesibilidad de la página.
     *
     * @return true si todos los elementos de accesibilidad están presentes
     */
    public boolean verificarAccesibilidad() {
        registrarAccion("Verificando elementos de accesibilidad");

        try {
            boolean etiquetasPresentes = esElementoPresente(ETIQUETA_EMAIL) &&
                    esElementoPresente(ETIQUETA_PASSWORD);

            boolean camposTienenId = !obtenerAtributo(CAMPO_EMAIL, "id").isEmpty() &&
                    !obtenerAtributo(CAMPO_PASSWORD, "id").isEmpty();

            boolean accesible = etiquetasPresentes && camposTienenId;

            logger.debug("Verificación de accesibilidad: {} (etiquetas: {}, IDs: {})",
                    accesible, etiquetasPresentes, camposTienenId);

            return accesible;

        } catch (Exception e) {
            logger.error("Error verificando accesibilidad: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene información de diagnóstico de la página.
     *
     * @return string con información de diagnóstico
     */
    public String obtenerInformacionDiagnostico() {
        StringBuilder diagnostico = new StringBuilder();

        try {
            diagnostico.append("=== DIAGNÓSTICO PÁGINA LOGIN ===\n");
            diagnostico.append("URL actual: ").append(driver.getCurrentUrl()).append("\n");
            diagnostico.append("Título: ").append(obtenerTituloPagina()).append("\n");
            diagnostico.append("Formulario visible: ").append(esFormularioVisible()).append("\n");
            diagnostico.append("Botón login visible: ").append(esBotonLoginVisible()).append("\n");
            diagnostico.append("Botón login habilitado: ").append(esBotonLoginHabilitado()).append("\n");
            diagnostico.append("Campo email vacío: ").append(esCampoEmailVacio()).append("\n");
            diagnostico.append("Campo password vacío: ").append(esCampoPasswordVacio()).append("\n");
            diagnostico.append("Mensaje error visible: ").append(esMensajeErrorVisible()).append("\n");
            diagnostico.append("Mensaje éxito visible: ").append(esMensajeExitoVisible()).append("\n");
            diagnostico.append("Recordar sesión marcado: ").append(estaRecordarSesionMarcado()).append("\n");
            diagnostico.append("Está cargando: ").append(estaCargando()).append("\n");

            if (esMensajeErrorVisible()) {
                diagnostico.append("Mensaje error: ").append(obtenerMensajeError()).append("\n");
            }

            if (esMensajeExitoVisible()) {
                diagnostico.append("Mensaje éxito: ").append(obtenerMensajeExito()).append("\n");
            }

            diagnostico.append("=== FIN DIAGNÓSTICO ===");

        } catch (Exception e) {
            diagnostico.append("Error generando diagnóstico: ").append(e.getMessage());
        }

        return diagnostico.toString();
    }

    // ==================== MÉTODOS DE VALIDACIÓN ESPECÍFICOS ====================

    /**
     * Valida el formato del email ingresado.
     *
     * @return true si el formato es válido
     */
    public boolean esFormatoEmailValido() {
        String email = obtenerValorEmail();
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Regex básico para validación de email
        String patronEmail = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        boolean esValido = email.matches(patronEmail);

        logger.debug("Formato email '{}' válido: {}", email, esValido);
        return esValido;
    }

    /**
     * Valida que los campos obligatorios estén completos.
     *
     * @return true si todos los campos obligatorios están completos
     */
    public boolean sonCamposObligatoriosCompletos() {
        boolean emailCompleto = !esCampoEmailVacio();
        boolean passwordCompleto = !esCampoPasswordVacio();
        boolean completos = emailCompleto && passwordCompleto;

        logger.debug("Campos obligatorios completos: {} (email: {}, password: {})",
                completos, emailCompleto, passwordCompleto);

        return completos;
    }

    /**
     * Simula presionar Enter en el campo de contraseña para enviar el formulario.
     */
    public void enviarFormularioConEnter() {
        registrarAccion("Enviando formulario con tecla Enter");

        try {
            WebElement campoPassword = esperarElementoVisible(CAMPO_PASSWORD);
            campoPassword.sendKeys(Keys.ENTER);

            // Esperar respuesta del servidor
            esperarFinDeCarga();

            logger.debug("Formulario enviado con Enter");
        } catch (Exception e) {
            logger.error("Error enviando formulario con Enter: {}", e.getMessage());
            throw new RuntimeException("No se pudo enviar formulario con Enter", e);
        }
    }

    /**
     * Método para testing - simula diferentes estados de la página.
     *
     * @param estado estado a simular (loading, error, success)
     */
    public void simularEstadoPagina(String estado) {
        registrarAccion("Simulando estado de página: " + estado);

        try {
            String script = switch (estado.toLowerCase()) {
                case "loading" ->
                        "document.body.insertAdjacentHTML('beforeend', " +
                                "'<div class=\"spinner loading\" style=\"display:block;\">Cargando...</div>');";
                case "error" ->
                        "document.body.insertAdjacentHTML('beforeend', " +
                                "'<div class=\"alert-error error-message\" style=\"display:block;\">Error de prueba</div>');";
                case "success" ->
                        "document.body.insertAdjacentHTML('beforeend', " +
                                "'<div class=\"alert-success success-message\" style=\"display:block;\">Éxito de prueba</div>');";
                default -> "";
            };

            if (!script.isEmpty()) {
                ejecutarJavaScript(script);
                logger.debug("Estado '{}' simulado exitosamente", estado);
            }

        } catch (Exception e) {
            logger.error("Error simulando estado '{}': {}", estado, e.getMessage());
        }
    }
}