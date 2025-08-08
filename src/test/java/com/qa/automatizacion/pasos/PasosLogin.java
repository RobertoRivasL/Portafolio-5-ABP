package com.qa.automatizacion.pasos;

import com.qa.automatizacion.paginas.PaginaLogin;
import com.qa.automatizacion.paginas.PaginaDashboard;
import com.qa.automatizacion.utilidades.Utileria;
import com.qa.automatizacion.modelo.Usuario;

import io.cucumber.java.es.*;
import io.cucumber.datatable.DataTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions para los escenarios de login.
 * Implementa los pasos en español usando la sintaxis Gherkin.
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0
 */
public class PasosLogin {

    private static final Logger logger = LoggerFactory.getLogger(PasosLogin.class);

    private final Utileria utileria;
    private final PaginaLogin paginaLogin;
    private final PaginaDashboard paginaDashboard;

    private Usuario usuarioActual;
    private String mensajeErrorEsperado;

    public PasosLogin() {
        this.utileria = Utileria.obtenerInstancia();
        this.paginaLogin = new PaginaLogin();
        this.paginaDashboard = new PaginaDashboard();

        logger.debug("PasosLogin inicializados");
    }

    // ==================== PASOS DADO (GIVEN) ====================

    @Dado("que el usuario está en la página de login")
    public void elUsuarioEstaEnLaPaginaDeLogin() {
        logger.info("=== PASO: Usuario está en la página de login ===");
        utileria.registrarPaso("HU-001", "Navegación a página de login");

        try {
            paginaLogin.navegarAPagina();
            assertTrue(paginaLogin.esPaginaCargada(),
                    "La página de login no se cargó correctamente");
            logger.info("Usuario navegó exitosamente a la página de login");

        } catch (Exception e) {
            logger.error("Error navegando a página de login: {}", e.getMessage());
            utileria.capturarScreenshotError("navegacion_login_error");
            throw new RuntimeException("Error navegando a login: " + e.getMessage(), e);
        }
    }

    @Dado("que el sistema está funcionando correctamente")
    public void elSistemaEstaFuncionandoCorrectamente() {
        logger.info("=== PASO: Verificando que el sistema funciona correctamente ===");
        utileria.registrarPaso("HU-001", "Verificación de sistema");

        try {
            paginaLogin.validarPaginaListaParaLogin();
            assertFalse(paginaLogin.hayErroresPrevios(),
                    "El sistema tiene errores previos");
            logger.info("Sistema verificado como funcional");

        } catch (Exception e) {
            logger.error("Error verificando sistema: {}", e.getMessage());
            utileria.capturarScreenshotError("verificacion_sistema_error");
            throw new RuntimeException("Sistema no está funcionando: " + e.getMessage(), e);
        }
    }

    @Dado("que el usuario tiene credenciales válidas")
    public void elUsuarioTieneCredencialesValidas(DataTable tablaCredenciales) {
        logger.info("=== PASO: Usuario tiene credenciales válidas ===");
        utileria.registrarPaso("HU-001", "Configuración de credenciales válidas");

        try {
            List<Map<String, String>> credenciales = tablaCredenciales.asMaps();

            if (credenciales.isEmpty()) {
                throw new IllegalArgumentException("No se proporcionaron credenciales");
            }

            Map<String, String> primerCredencial = credenciales.get(0);
            String email = primerCredencial.get("email");
            String password = primerCredencial.get("password");

            assertTrue(paginaLogin.validarCredenciales(email, password),
                    "Las credenciales proporcionadas no tienen formato válido");

            this.usuarioActual = new Usuario();
            this.usuarioActual.setEmail(email);
            this.usuarioActual.setPassword(password);

            logger.info("Credenciales válidas configuradas para email: {}", email);

        } catch (Exception e) {
            logger.error("Error configurando credenciales: {}", e.getMessage());
            throw new RuntimeException("Error en credenciales: " + e.getMessage(), e);
        }
    }

    @Dado("que el usuario tiene credenciales inválidas")
    public void elUsuarioTieneCredencialesInvalidas(DataTable tablaCredenciales) {
        logger.info("=== PASO: Usuario tiene credenciales inválidas ===");
        utileria.registrarPaso("HU-002", "Configuración de credenciales inválidas");

        try {
            List<Map<String, String>> credenciales = tablaCredenciales.asMaps();
            Map<String, String> primerCredencial = credenciales.get(0);

            String email = primerCredencial.get("email");
            String password = primerCredencial.get("password");

            this.usuarioActual = new Usuario();
            this.usuarioActual.setEmail(email);
            this.usuarioActual.setPassword(password);

            logger.info("Credenciales inválidas configuradas");

        } catch (Exception e) {
            logger.error("Error configurando credenciales inválidas: {}", e.getMessage());
            throw new RuntimeException("Error en credenciales inválidas: " + e.getMessage(), e);
        }
    }

    @Dado("que el usuario quiere recordar su sesión")
    public void elUsuarioQuiereRecordarSuSesion() {
        logger.info("=== PASO: Usuario quiere recordar sesión ===");
        utileria.registrarPaso("HU-003", "Configuración de recordar sesión");

        if (usuarioActual != null) {
            usuarioActual.setRecordarSesion(true);
        }

        logger.info("Configurado para recordar sesión");
    }

    // ==================== PASOS CUANDO (WHEN) ====================

    @Cuando("el usuario ingresa sus credenciales")
    public void elUsuarioIngresaSusCredenciales() {
        logger.info("=== PASO: Usuario ingresa credenciales ===");
        utileria.registrarPaso("HU-001", "Ingreso de credenciales");

        try {
            if (usuarioActual == null) {
                throw new IllegalStateException("No hay credenciales configuradas");
            }

            paginaLogin.ingresarCredenciales(
                    usuarioActual.getEmail(),
                    usuarioActual.getPassword()
            );

            logger.info("Credenciales ingresadas para: {}", usuarioActual.getEmail());

        } catch (Exception e) {
            logger.error("Error ingresando credenciales: {}", e.getMessage());
            utileria.capturarScreenshotError("ingreso_credenciales_error");
            throw new RuntimeException("Error ingresando credenciales: " + e.getMessage(), e);
        }
    }

    @Cuando("ingresa el email {string}")
    public void ingresaElEmail(String email) {
        logger.info("=== PASO: Ingresando email específico: {} ===", email);
        utileria.registrarPaso("HU-002", "Ingreso de email específico");

        try {
            paginaLogin.ingresarEmail(email);

            if (usuarioActual == null) {
                usuarioActual = new Usuario();
            }
            usuarioActual.setEmail(email);

            logger.info("Email específico ingresado: {}", email);

        } catch (Exception e) {
            logger.error("Error ingresando email específico: {}", e.getMessage());
            utileria.capturarScreenshotError("ingreso_email_especifico_error");
            throw new RuntimeException("Error ingresando email: " + e.getMessage(), e);
        }
    }

    @Cuando("ingresa la contraseña {string}")
    public void ingresaLaContrasena(String password) {
        logger.info("=== PASO: Ingresando contraseña específica ===");
        utileria.registrarPaso("HU-002", "Ingreso de contraseña específica");

        try {
            paginaLogin.ingresarPassword(password);

            if (usuarioActual == null) {
                usuarioActual = new Usuario();
            }
            usuarioActual.setPassword(password);

            logger.info("Contraseña específica ingresada");

        } catch (Exception e) {
            logger.error("Error ingresando contraseña específica: {}", e.getMessage());
            utileria.capturarScreenshotError("ingreso_password_especifico_error");
            throw new RuntimeException("Error ingresando contraseña: " + e.getMessage(), e);
        }
    }

    @Cuando("hace clic en el botón {string}")
    public void haceClicEnElBoton(String nombreBoton) {
        logger.info("=== PASO: Haciendo clic en botón: {} ===", nombreBoton);
        utileria.registrarPaso("HU-001", "Clic en botón " + nombreBoton);

        try {
            switch (nombreBoton.toLowerCase()) {
                case "iniciar sesión":
                case "login":
                case "entrar":
                    paginaLogin.hacerClickLogin();
                    break;
                case "registrarse":
                case "registro":
                    paginaLogin.navegarARegistro();
                    break;
                case "olvidaste tu contraseña":
                case "recuperar contraseña":
                    paginaLogin.navegarARecuperarPassword();
                    break;
                default:
                    throw new IllegalArgumentException("Botón no reconocido: " + nombreBoton);
            }

            logger.info("Clic ejecutado en botón: {}", nombreBoton);

        } catch (Exception e) {
            logger.error("Error haciendo clic en botón '{}': {}", nombreBoton, e.getMessage());
            utileria.capturarScreenshotError("click_boton_error");
            throw new RuntimeException("Error en clic de botón: " + e.getMessage(), e);
        }
    }

    @Cuando("selecciona recordar sesión")
    public void seleccionaRecordarSesion() {
        logger.info("=== PASO: Seleccionando recordar sesión ===");
        utileria.registrarPaso("HU-003", "Selección de recordar sesión");

        try {
            paginaLogin.marcarRecordarSesion();

            if (usuarioActual != null) {
                usuarioActual.setRecordarSesion(true);
            }

            logger.info("Recordar sesión seleccionado");

        } catch (Exception e) {
            logger.error("Error seleccionando recordar sesión: {}", e.getMessage());
            utileria.capturarScreenshotError("recordar_sesion_error");
            throw new RuntimeException("Error seleccionando recordar sesión: " + e.getMessage(), e);
        }
    }

    @Cuando("intenta realizar login")
    public void intentaRealizarLogin() {
        logger.info("=== PASO: Intentando realizar login ===");
        utileria.registrarPaso("HU-002", "Intento de login");

        try {
            if (usuarioActual == null) {
                throw new IllegalStateException("No hay usuario configurado para login");
            }

            boolean recordarSesion = usuarioActual.isRecordarSesion();

            paginaLogin.realizarLogin(
                    usuarioActual.getEmail(),
                    usuarioActual.getPassword(),
                    recordarSesion
            );

            logger.info("Intento de login ejecutado");

        } catch (Exception e) {
            logger.error("Error en intento de login: {}", e.getMessage());
            utileria.capturarScreenshotError("intento_login_error");
        }
    }

    @Cuando("espera {int} segundos")
    public void espera(int segundos) {
        logger.info("=== PASO: Esperando {} segundos ===", segundos);
        utileria.registrarPaso("ACTUAL", "Espera de " + segundos + " segundos");

        utileria.pausa(segundos * 1000L);

        logger.info("Espera de {} segundos completada", segundos);
    }

    // ==================== PASOS ENTONCES (THEN) ====================

    @Entonces("el usuario debe ser redirigido al dashboard")
    public void elUsuarioDebeSerRedirigidoAlDashboard() {
        logger.info("=== PASO: Verificando redirección al dashboard ===");
        utileria.registrarPaso("HU-001", "Verificación de redirección al dashboard");

        try {
            paginaLogin.esperarProcesoLogin(15);

            assertFalse(utileria.verificarUrl("login"),
                    "Usuario sigue en página de login después del login exitoso");

            assertTrue(utileria.verificarUrl("dashboard"),
                    "Usuario no fue redirigido al dashboard");

            paginaDashboard.esperarCargaCompletaPagina();
            assertTrue(paginaDashboard.esPaginaCargada(),
                    "Dashboard no se cargó correctamente");

            logger.info("Redirección al dashboard verificada exitosamente");

        } catch (Exception e) {
            logger.error("Error verificando redirección al dashboard: {}", e.getMessage());
            utileria.capturarScreenshotError("verificacion_dashboard_error");
            throw new RuntimeException("Error en redirección al dashboard: " + e.getMessage(), e);
        }
    }

    @Entonces("debe ver el mensaje de bienvenida {string}")
    public void debeVerElMensajeDeBienvenida(String mensajeEsperado) {
        logger.info("=== PASO: Verificando mensaje de bienvenida: {} ===", mensajeEsperado);
        utileria.registrarPaso("HU-001", "Verificación de mensaje de bienvenida");

        try {
            assertTrue(paginaDashboard.hayMensajeBienvenida(),
                    "No se encontró mensaje de bienvenida en el dashboard");

            String mensajeActual = paginaDashboard.obtenerMensajeBienvenida();

            assertTrue(mensajeActual.contains(mensajeEsperado),
                    String.format("Mensaje de bienvenida no contiene el texto esperado. " +
                            "Esperado: '%s', Actual: '%s'", mensajeEsperado, mensajeActual));

            logger.info("Mensaje de bienvenida verificado: {}", mensajeActual);

        } catch (Exception e) {
            logger.error("Error verificando mensaje de bienvenida: {}", e.getMessage());
            utileria.capturarScreenshotError("verificacion_bienvenida_error");
            throw new RuntimeException("Error verificando mensaje de bienvenida: " + e.getMessage(), e);
        }
    }

    @Entonces("debe ver su nombre de usuario en la barra superior")
    public void debeVerSuNombreDeUsuarioEnLaBarraSuperior() {
        logger.info("=== PASO: Verificando nombre de usuario en barra superior ===");
        utileria.registrarPaso("HU-001", "Verificación de nombre de usuario");

        try {
            assertTrue(paginaDashboard.hayInformacionUsuario(),
                    "No se encontró información de usuario en la barra superior");

            String nombreUsuario = paginaDashboard.obtenerNombreUsuario();

            assertFalse(nombreUsuario.isEmpty(),
                    "Nombre de usuario está vacío en la barra superior");

            if (usuarioActual != null && usuarioActual.getEmail() != null) {
                String emailEsperado = usuarioActual.getEmail();
                assertTrue(nombreUsuario.contains(emailEsperado) ||
                                emailEsperado.contains(nombreUsuario.toLowerCase()),
                        String.format("Nombre de usuario '%s' no es consistente con email '%s'",
                                nombreUsuario, emailEsperado));
            }

            logger.info("Nombre de usuario verificado en barra superior: {}", nombreUsuario);

        } catch (Exception e) {
            logger.error("Error verificando nombre de usuario: {}", e.getMessage());
            utileria.capturarScreenshotError("verificacion_nombre_usuario_error");
            throw new RuntimeException("Error verificando nombre de usuario: " + e.getMessage(), e);
        }
    }

    @Entonces("debe ver un mensaje de error")
    public void debeVerUnMensajeDeError() {
        logger.info("=== PASO: Verificando presencia de mensaje de error ===");
        utileria.registrarPaso("HU-002", "Verificación de mensaje de error");

        try {
            boolean hayError = paginaLogin.esperarMensajeError(10);

            assertTrue(hayError, "No se mostró mensaje de error cuando se esperaba");
            assertTrue(paginaLogin.hayMensajeError(), "Mensaje de error no está visible");

            String mensajeError = paginaLogin.obtenerMensajeError();
            logger.info("Mensaje de error verificado: {}", mensajeError);

        } catch (Exception e) {
            logger.error("Error verificando mensaje de error: {}", e.getMessage());
            utileria.capturarScreenshotError("verificacion_error_esperado");
            throw new RuntimeException("Error verificando mensaje de error: " + e.getMessage(), e);
        }
    }

    @Entonces("debe ver el mensaje {string}")
    public void debeVerElMensaje(String mensajeEsperado) {
        logger.info("=== PASO: Verificando mensaje específico: {} ===", mensajeEsperado);
        utileria.registrarPaso("HU-002", "Verificación de mensaje específico");

        try {
            if (mensajeEsperado.toLowerCase().contains("error") ||
                    mensajeEsperado.toLowerCase().contains("incorrecto") ||
                    mensajeEsperado.toLowerCase().contains("inválido")) {

                assertTrue(paginaLogin.hayMensajeError(),
                        "No se encontró mensaje de error cuando se esperaba");

                String mensajeActual = paginaLogin.obtenerMensajeError();
                assertTrue(mensajeActual.contains(mensajeEsperado) ||
                                mensajeEsperado.contains(mensajeActual),
                        String.format("Mensaje de error no coincide. Esperado: '%s', Actual: '%s'",
                                mensajeEsperado, mensajeActual));

            } else if (mensajeEsperado.toLowerCase().contains("éxito") ||
                    mensajeEsperado.toLowerCase().contains("bienvenido")) {

                assertTrue(paginaDashboard.hayMensajeBienvenida() || paginaLogin.hayMensajeExito(),
                        "No se encontró mensaje de éxito cuando se esperaba");

            } else {
                boolean encontrado = paginaLogin.hayMensajeError() ||
                        paginaLogin.hayMensajeExito() ||
                        paginaLogin.hayMensajeAdvertencia();

                assertTrue(encontrado, "No se encontró ningún mensaje cuando se esperaba: " + mensajeEsperado);
            }

            logger.info("Mensaje específico verificado: {}", mensajeEsperado);

        } catch (Exception e) {
            logger.error("Error verificando mensaje específico: {}", e.getMessage());
            utileria.capturarScreenshotError("verificacion_mensaje_especifico_error");
            throw new RuntimeException("Error verificando mensaje: " + e.getMessage(), e);
        }
    }

    @Entonces("debe permanecer en la página de login")
    public void debePermanecer_en_la_página_de_login() {
        logger.info("=== PASO: Verificando permanencia en página de login ===");
        utileria.registrarPaso("HU-002", "Verificación de permanencia en login");

        try {
            utileria.pausa(3000);

            assertTrue(utileria.verificarUrl("login"),
                    "Usuario no permaneció en página de login como se esperaba");

            assertTrue(paginaLogin.esPaginaCargada(),
                    "Página de login no está cargada correctamente");

            logger.info("Permanencia en página de login verificada");

        } catch (Exception e) {
            logger.error("Error verificando permanencia en login: {}", e.getMessage());
            utileria.capturarScreenshotError("verificacion_permanencia_login_error");
            throw new RuntimeException("Error verificando permanencia en login: " + e.getMessage(), e);
        }
    }

    @Entonces("los campos deben estar vacíos")
    public void losCamposDebenEstarVacios() {
        logger.info("=== PASO: Verificando que los campos están vacíos ===");
        utileria.registrarPaso("HU-002", "Verificación de campos vacíos");

        try {
            String emailActual = paginaLogin.obtenerEmailIngresado();
            assertTrue(emailActual == null || emailActual.trim().isEmpty(),
                    "Campo email no está vacío: " + emailActual);

            logger.info("Campos vacíos verificados");

        } catch (Exception e) {
            logger.error("Error verificando campos vacíos: {}", e.getMessage());
            utileria.capturarScreenshotError("verificacion_campos_vacios_error");
            throw new RuntimeException("Error verificando campos vacíos: " + e.getMessage(), e);
        }
    }

    @Entonces("el checkbox de recordar sesión debe estar marcado")
    public void elCheckboxDeRecordarSesionDebeEstarMarcado() {
        logger.info("=== PASO: Verificando checkbox recordar sesión marcado ===");
        utileria.registrarPaso("HU-003", "Verificación de checkbox marcado");

        try {
            assertTrue(paginaLogin.esRecordarSesionMarcado(),
                    "Checkbox de recordar sesión no está marcado");

            logger.info("Checkbox recordar sesión verificado como marcado");

        } catch (Exception e) {
            logger.error("Error verificando checkbox: {}", e.getMessage());
            utileria.capturarScreenshotError("verificacion_checkbox_error");
            throw new RuntimeException("Error verificando checkbox: " + e.getMessage(), e);
        }
    }

    // ==================== PASOS Y (AND) ====================

    @Y("el formulario está habilitado")
    public void elFormularioEstaHabilitado() {
        logger.info("=== PASO: Verificando formulario habilitado ===");
        utileria.registrarPaso("ACTUAL", "Verificación de formulario habilitado");

        try {
            assertTrue(paginaLogin.esFormularioHabilitado(),
                    "El formulario de login no está habilitado");

            logger.info("Formulario habilitado verificado");

        } catch (Exception e) {
            logger.error("Error verificando formulario habilitado: {}", e.getMessage());
            throw new RuntimeException("Formulario no habilitado: " + e.getMessage(), e);
        }
    }

    @Y("no hay errores previos en la página")
    public void noHayErroresPreviosEnLaPagina() {
        logger.info("=== PASO: Verificando ausencia de errores previos ===");
        utileria.registrarPaso("ACTUAL", "Verificación de ausencia de errores");

        try {
            assertFalse(paginaLogin.hayErroresPrevios(),
                    "Hay errores previos en la página de login");

            logger.info("Ausencia de errores previos verificada");

        } catch (Exception e) {
            logger.error("Error verificando ausencia de errores: {}", e.getMessage());
            throw new RuntimeException("Hay errores previos: " + e.getMessage(), e);
        }
    }

    @Y("el título de la página es correcto")
    public void elTituloDeLaPaginaEsCorrecto() {
        logger.info("=== PASO: Verificando título de página ===");
        utileria.registrarPaso("ACTUAL", "Verificación de título");

        try {
            String tituloEsperado = paginaLogin.obtenerTituloEsperado();
            assertTrue(utileria.verificarTituloPagina(tituloEsperado),
                    "El título de la página no es correcto");

            logger.info("Título de página verificado");

        } catch (Exception e) {
            logger.error("Error verificando título: {}", e.getMessage());
            throw new RuntimeException("Título incorrecto: " + e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    private void configurarUsuario(String email, String password) {
        this.usuarioActual = new Usuario();
        this.usuarioActual.setEmail(email);
        this.usuarioActual.setPassword(password);
        this.usuarioActual.setRecordarSesion(false);
    }

    public void limpiarEstado() {
        logger.debug("Limpiando estado de pasos login");

        this.usuarioActual = null;
        this.mensajeErrorEsperado = null;

        try {
            paginaLogin.limpiar();
        } catch (Exception e) {
            logger.warn("Error limpiando página login: {}", e.getMessage());
        }
    }

    public String obtenerEstadoDebug() {
        try {
            return String.format(
                    "Estado PasosLogin - Usuario: %s | Página Login: %s | Página Dashboard: %s",
                    usuarioActual != null ? usuarioActual.getEmail() : "null",
                    paginaLogin.obtenerEstadoCompleto(),
                    paginaDashboard != null ? paginaDashboard.obtenerInformacionDebug() : "N/A"
            );

        } catch (Exception e) {
            return "Error obteniendo estado debug: " + e.getMessage();
        }
    }
}