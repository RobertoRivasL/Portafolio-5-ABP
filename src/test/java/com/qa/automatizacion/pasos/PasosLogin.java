package com.qa.automatizacion.pasos;

import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import com.qa.automatizacion.modelo.Usuario;
import com.qa.automatizacion.paginas.PaginaLogin;
import com.qa.automatizacion.utilidades.HelperTrazabilidad;
import com.qa.automatizacion.utilidades.UtileriasComunes;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.*;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions optimizados para los escenarios de login.
 * Utiliza la nueva arquitectura con métodos centralizados y reutilizables.
 *
 * Principios aplicados:
 * - DRY: Reutiliza métodos de UtileriasComunes y PaginaLogin optimizada
 * - Single Responsibility: Se enfoca únicamente en los pasos de login
 * - Composition over Inheritance: Usa composición en lugar de herencia múltiple
 * - Dependency Injection: Recibe dependencias en constructor
 * - Open/Closed: Abierto para extensión, cerrado para modificación
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0 - Optimizado con métodos reutilizables centralizados
 */
public class PasosLogin {

    private static final Logger logger = LoggerFactory.getLogger(PasosLogin.class);

    // Dependencias inyectadas o inicializadas
    private final PropiedadesAplicacion propiedades;
    private final HelperTrazabilidad trazabilidad;
    private final PaginaLogin paginaLogin;

    // Variables de contexto para el escenario actual
    private String emailUsuario;
    private String passwordUsuario;
    private boolean recordarSesion;
    private Usuario usuarioContexto;
    private long tiempoInicioLogin;
    private boolean loginExitoso;

    // ==================== CONSTRUCTOR ====================

    /**
     * Constructor que inicializa todas las dependencias necesarias.
     * Sigue el patrón de inyección de dependencias para mejor testabilidad.
     */
    public PasosLogin() {
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();
        this.trazabilidad = new HelperTrazabilidad();
        this.paginaLogin = new PaginaLogin(); // Usa el constructor optimizado

        // Inicializar variables de contexto
        this.loginExitoso = false;
        this.recordarSesion = false;

        logger.debug("PasosLogin inicializado con arquitectura optimizada");
    }

    // ==================== PASOS DADO (GIVEN) ====================

    @Dado("que el usuario está en la página de login")
    public void elUsuarioEstaEnLaPaginaDeLogin() {
        registrarInicioAccion("Navegando a página de login");

        try {
            // Navegar a la página usando el método optimizado
            paginaLogin.navegarAPagina();

            // Verificar que la página está completamente cargada
            assertTrue(paginaLogin.estaPaginaCargada(),
                    "La página de login no se cargó correctamente");

            // Verificar salud de la página
            assertTrue(paginaLogin.verificarSaludPagina(),
                    "La página de login no está en un estado saludable");

            registrarFinAccion("Usuario en página de login", "Éxito");

        } catch (Exception e) {
            registrarFinAccion("Error navegando a login", e.getMessage());
            fail("Error navegando a la página de login: " + e.getMessage());
        }
    }

    @Dado("que el usuario tiene credenciales válidas")
    public void elUsuarioTieneCredencialesValidas() {
        registrarInicioAccion("Configurando credenciales válidas por defecto");

        // Usar credenciales por defecto desde propiedades
        this.emailUsuario = propiedades.obtenerUsuarioDefecto();
        this.passwordUsuario = propiedades.obtenerPasswordDefecto();

        // Validar formato de credenciales
        assertTrue(paginaLogin.esFormatoEmailValido(emailUsuario),
                "El formato del email no es válido: " + emailUsuario);

        // Crear objeto usuario para contexto
        this.usuarioContexto = Usuario.builder()
                .email(emailUsuario)
                .contrasena(passwordUsuario)
                .build();

        registrarFinAccion("Credenciales configuradas",
                "Email: " + emailUsuario + ", Usuario: " + usuarioContexto.getId());
    }

    @Dado("que el usuario tiene las siguientes credenciales:")
    public void elUsuarioTieneLasSiguientesCredenciales(DataTable credenciales) {
        registrarInicioAccion("Configurando credenciales desde DataTable");

        Map<String, String> datos = credenciales.asMap();

        // Extraer y validar credenciales
        this.emailUsuario = datos.get("email");
        this.passwordUsuario = datos.get("password");

        assertNotNull(emailUsuario, "Email no puede ser null");
        assertNotNull(passwordUsuario, "Password no puede ser null");
        assertFalse(emailUsuario.trim().isEmpty(), "Email no puede estar vacío");
        assertFalse(passwordUsuario.trim().isEmpty(), "Password no puede estar vacío");

        // Validar formato de email
        assertTrue(paginaLogin.esFormatoEmailValido(emailUsuario),
                "Formato de email inválido: " + emailUsuario);

        // Configurar opción de recordar si está presente
        if (datos.containsKey("recordar")) {
            this.recordarSesion = Boolean.parseBoolean(datos.get("recordar"));
        }

        // Crear objeto usuario completo
        this.usuarioContexto = Usuario.builder()
                .email(emailUsuario)
                .contrasena(passwordUsuario)
                .nombre(datos.getOrDefault("nombre", "Usuario Test"))
                .apellido(datos.getOrDefault("apellido", "Prueba"))
                .build();

        registrarFinAccion("Credenciales desde DataTable configuradas",
                "Email: " + emailUsuario + ", Recordar: " + recordarSesion);
    }

    @Dado("que el formulario de login está visible y habilitado")
    public void elFormularioDeLoginEstaVisibleYHabilitado() {
        registrarInicioAccion("Verificando estado del formulario de login");

        // Verificar visibilidad del formulario
        assertTrue(paginaLogin.esFormularioVisible(),
                "El formulario de login no está visible");

        // Verificar que los campos estén habilitados
        assertTrue(paginaLogin.esCampoEmailHabilitado(),
                "El campo de email no está habilitado");
        assertTrue(paginaLogin.esCampoPasswordHabilitado(),
                "El campo de contraseña no está habilitado");
        assertTrue(paginaLogin.esBotonLoginHabilitado(),
                "El botón de login no está habilitado");

        // Verificar que no haya mensajes de error previos
        assertFalse(paginaLogin.hayErrorCredenciales(),
                "Hay errores de credenciales previos");
        assertFalse(paginaLogin.hayCuentaBloqueada(),
                "La cuenta aparece como bloqueada");

        registrarFinAccion("Formulario verificado", "Todos los elementos están disponibles");
    }

    @Dado("que el sistema está funcionando correctamente")
    public void elSistemaEstaFuncionandoCorrectamente() {
        registrarInicioAccion("Verificando funcionamiento del sistema");

        try {
            // Verificar conectividad básica
            String urlActual = paginaLogin.obtenerUrlActual();
            assertNotNull(urlActual, "No se puede obtener la URL actual");
            assertTrue(urlActual.contains(propiedades.obtenerUrlBase()),
                    "La URL no corresponde al sistema esperado");

            // Verificar que la página responde
            String titulo = paginaLogin.obtenerTituloPagina();
            assertFalse(titulo.isEmpty(), "El título de la página está vacío");

            // Verificar que no hay overlays de carga
            assertFalse(paginaLogin.estaCargando(),
                    "El sistema aún está en estado de carga");

            // Verificar salud general de la página
            assertTrue(paginaLogin.verificarSaludPagina(),
                    "La página no está en un estado saludable");

            registrarFinAccion("Sistema funcionando", "Todas las verificaciones pasaron");

        } catch (Exception e) {
            registrarFinAccion("Error verificando sistema", e.getMessage());
            fail("Error verificando el funcionamiento del sistema: " + e.getMessage());
        }
    }

    // ==================== PASOS CUANDO (WHEN) ====================

    @Cuando("el usuario ingresa sus credenciales")
    public void elUsuarioIngresaSusCredenciales() {
        registrarInicioAccion("Ingresando credenciales", "Email: " + emailUsuario);

        assertNotNull(emailUsuario, "Email no está configurado en el contexto");
        assertNotNull(passwordUsuario, "Password no está configurado en el contexto");

        this.tiempoInicioLogin = System.currentTimeMillis();

        try {
            // Limpiar formulario antes de ingresar credenciales
            paginaLogin.limpiarFormulario();

            // Ingresar credenciales usando métodos optimizados
            boolean credencialesIngresadas = paginaLogin.ingresarCredenciales(emailUsuario, passwordUsuario);

            assertTrue(credencialesIngresadas,
                    "No se pudieron ingresar las credenciales correctamente");

            // Verificar que las credenciales se ingresaron correctamente
            Optional<String> emailIngresado = paginaLogin.obtenerValorEmail();
            assertTrue(emailIngresado.isPresent(), "Email no se ingresó correctamente");
            assertEquals(emailUsuario, emailIngresado.get(),
                    "El email ingresado no coincide con el esperado");

            registrarFinAccion("Credenciales ingresadas", "Verificación exitosa");

        } catch (Exception e) {
            registrarFinAccion("Error ingresando credenciales", e.getMessage());
            fail("Error ingresando credenciales: " + e.getMessage());
        }
    }

    @Cuando("el usuario marca la opción {string}")
    public void elUsuarioMarcaLaOpcion(String opcion) {
        registrarInicioAccion("Marcando opción", opcion);

        try {
            switch (opcion.toLowerCase()) {
                case "recordar sesión":
                case "recordar mi sesión":
                case "mantener sesión activa":
                    boolean marcado = paginaLogin.marcarRecordarSesion();
                    assertTrue(marcado, "No se pudo marcar la opción de recordar sesión");
                    this.recordarSesion = true;
                    break;

                default:
                    fail("Opción no reconocida: " + opcion);
            }

            registrarFinAccion("Opción marcada", opcion + " - Éxito");

        } catch (Exception e) {
            registrarFinAccion("Error marcando opción", e.getMessage());
            fail("Error marcando la opción '" + opcion + "': " + e.getMessage());
        }
    }

    @Cuando("hace clic en el botón {string}")
    public void haceClicEnElBoton(String nombreBoton) {
        registrarInicioAccion("Haciendo clic en botón", nombreBoton);

        try {
            boolean clicExitoso = false;

            switch (nombreBoton.toLowerCase()) {
                case "iniciar sesión":
                case "login":
                case "entrar":
                case "acceder":
                    clicExitoso = paginaLogin.hacerClicEnLogin();
                    break;

                case "olvidé mi contraseña":
                case "recuperar contraseña":
                case "¿olvidaste tu contraseña?":
                    clicExitoso = paginaLogin.hacerClicEnOlvidoPassword();
                    break;

                case "registrarse":
                case "crear cuenta":
                case "registro":
                    clicExitoso = paginaLogin.hacerClicEnRegistro();
                    break;

                default:
                    fail("Botón no reconocido: " + nombreBoton);
            }

            assertTrue(clicExitoso, "No se pudo hacer clic en el botón: " + nombreBoton);

            // Si es el botón de login, esperar respuesta del sistema
            if (nombreBoton.toLowerCase().contains("iniciar") ||
                    nombreBoton.toLowerCase().contains("login")) {

                // Esperar un momento para que el sistema procese
                UtileriasComunes.esperarSegundos(2);
            }

            registrarFinAccion("Clic en botón exitoso", nombreBoton);

        } catch (Exception e) {
            registrarFinAccion("Error haciendo clic", e.getMessage());
            fail("Error haciendo clic en el botón '" + nombreBoton + "': " + e.getMessage());
        }
    }

    @Cuando("el usuario intenta hacer login con credenciales incorrectas")
    public void elUsuarioIntentaHacerLoginConCredencialesIncorrectas() {
        registrarInicioAccion("Intentando login con credenciales incorrectas");

        // Configurar credenciales incorrectas
        this.emailUsuario = "usuario.inexistente@test.com";
        this.passwordUsuario = "passwordIncorrecto123";

        this.tiempoInicioLogin = System.currentTimeMillis();

        try {
            // Intentar login sabiendo que fallará
            this.loginExitoso = paginaLogin.iniciarSesion(emailUsuario, passwordUsuario);

            // Esperar respuesta del sistema
            UtileriasComunes.esperarSegundos(3);

            registrarFinAccion("Intento de login incorrecto completado",
                    "Resultado: " + (loginExitoso ? "Exitoso" : "Fallido"));

        } catch (Exception e) {
            registrarFinAccion("Error en intento de login", e.getMessage());
            // No fallar aquí porque esperamos que el login falle
        }
    }

    @Cuando("el usuario deja los campos vacíos")
    public void elUsuarioDejaLosCamposVacios() {
        registrarInicioAccion("Dejando campos vacíos");

        try {
            // Limpiar formulario completamente
            paginaLogin.limpiarFormulario();

            // Verificar que los campos están efectivamente vacíos
            Optional<String> valorEmail = paginaLogin.obtenerValorEmail();
            Optional<String> valorPassword = paginaLogin.obtenerValorPassword();

            assertTrue(valorEmail.isEmpty() || valorEmail.get().trim().isEmpty(),
                    "El campo email no está vacío");
            assertTrue(valorPassword.isEmpty() || valorPassword.get().trim().isEmpty(),
                    "El campo password no está vacío");

            // Configurar contexto para campos vacíos
            this.emailUsuario = "";
            this.passwordUsuario = "";

            registrarFinAccion("Campos vacíos configurados", "Éxito");

        } catch (Exception e) {
            registrarFinAccion("Error dejando campos vacíos", e.getMessage());
            fail("Error configurando campos vacíos: " + e.getMessage());
        }
    }

    @Cuando("el usuario espera {int} segundos")
    public void elUsuarioEsperaSegundos(int segundos) {
        registrarInicioAccion("Esperando tiempo específico", segundos + " segundos");

        UtileriasComunes.esperarSegundos(segundos);

        registrarFinAccion("Espera completada", segundos + " segundos");
    }

    // ==================== PASOS ENTONCES (THEN) ====================

    @Entonces("el usuario debe ser redirigido al dashboard")
    public void elUsuarioDebeSerRedirigidoAlDashboard() {
        registrarInicioAccion("Verificando redirección al dashboard");

        try {
            // Esperar redirección con timeout
            boolean redireccionExitosa = paginaLogin.esperarRedireccionLogin(10);
            assertTrue(redireccionExitosa, "No hubo redirección después del login");

            // Verificar que la URL cambió al dashboard
            String urlActual = paginaLogin.obtenerUrlActual();
            String urlDashboard = propiedades.obtenerUrlDashboard();

            assertTrue(urlActual.contains("dashboard") || urlActual.equals(urlDashboard),
                    "La URL no corresponde al dashboard. URL actual: " + urlActual);

            // Marcar login como exitoso
            this.loginExitoso = true;

            registrarFinAccion("Redirección al dashboard verificada", "URL: " + urlActual);

        } catch (Exception e) {
            registrarFinAccion("Error verificando redirección", e.getMessage());
            fail("Error verificando redirección al dashboard: " + e.getMessage());
        }
    }

    @Entonces("debe ver el mensaje de bienvenida {string}")
    public void debeVerElMensajeDeBienvenida(String mensajeEsperado) {
        registrarInicioAccion("Verificando mensaje de bienvenida", mensajeEsperado);

        try {
            // Esperar que aparezca el mensaje de éxito
            boolean mensajeVisible = paginaLogin.esperarMensajeExito(5);
            assertTrue(mensajeVisible, "No apareció mensaje de éxito/bienvenida");

            // Verificar contenido del mensaje
            Optional<String> mensajeExito = paginaLogin.obtenerMensajeExito();
            assertTrue(mensajeExito.isPresent(), "No se pudo obtener el mensaje de éxito");

            String mensajeActual = mensajeExito.get();
            assertTrue(mensajeActual.contains(mensajeEsperado) ||
                            mensajeEsperado.contains(mensajeActual),
                    "El mensaje no contiene el texto esperado. " +
                            "Esperado: '" + mensajeEsperado + "', " +
                            "Actual: '" + mensajeActual + "'");

            registrarFinAccion("Mensaje de bienvenida verificado",
                    "Esperado: " + mensajeEsperado + ", Actual: " + mensajeActual);

        } catch (Exception e) {
            registrarFinAccion("Error verificando mensaje", e.getMessage());
            fail("Error verificando mensaje de bienvenida: " + e.getMessage());
        }
    }

    @Entonces("debe ver su nombre de usuario en la barra superior")
    public void debeVerSuNombreDeUsuarioEnLaBarraSuperior() {
        registrarInicioAccion("Verificando nombre de usuario en barra superior");

        try {
            // Localizador para la barra superior con nombre de usuario
            By barraSuperior = By.cssSelector(".navbar .user-info, .header .username, [data-testid='user-display']");

            // Esperar que aparezca el nombre de usuario
            boolean nombreVisible = UtileriasComunes.esperarElementoVisible(
                    paginaLogin.obtenerDriver(), barraSuperior, 8);
            assertTrue(nombreVisible, "No se encontró el nombre de usuario en la barra superior");

            // Obtener y verificar el texto del nombre
            Optional<String> textoUsuario = UtileriasComunes.obtenerTextoElemento(
                    paginaLogin.obtenerDriver(), barraSuperior);
            assertTrue(textoUsuario.isPresent(), "No se pudo obtener el texto del usuario");

            String nombreMostrado = textoUsuario.get();
            assertFalse(nombreMostrado.trim().isEmpty(), "El nombre de usuario está vacío");

            // Si tenemos contexto del usuario, verificar que coincida
            if (usuarioContexto != null && usuarioContexto.getNombre() != null) {
                assertTrue(nombreMostrado.contains(usuarioContexto.getNombre()) ||
                                nombreMostrado.contains(emailUsuario),
                        "El nombre mostrado no coincide con el usuario logueado. " +
                                "Mostrado: '" + nombreMostrado + "', Esperado: '" +
                                (usuarioContexto.getNombre() != null ? usuarioContexto.getNombre() : emailUsuario) + "'");
            }

            registrarFinAccion("Nombre de usuario verificado", "Nombre: " + nombreMostrado);

        } catch (Exception e) {
            registrarFinAccion("Error verificando nombre de usuario", e.getMessage());
            fail("Error verificando nombre de usuario en barra superior: " + e.getMessage());
        }
    }

    @Entonces("debe ver un mensaje de error indicando {string}")
    public void debeVerUnMensajeDeErrorIndicando(String tipoError) {
        registrarInicioAccion("Verificando mensaje de error", tipoError);

        try {
            // Esperar que aparezca algún mensaje de error
            boolean errorVisible = paginaLogin.esperarMensajeError(5);
            assertTrue(errorVisible, "No apareció ningún mensaje de error");

            Optional<String> mensajeError = Optional.empty();

            switch (tipoError.toLowerCase()) {
                case "credenciales incorrectas":
                case "email o contraseña incorrectos":
                case "datos incorrectos":
                    assertTrue(paginaLogin.hayErrorCredenciales(),
                            "No hay mensaje de error de credenciales");
                    mensajeError = paginaLogin.obtenerMensajeErrorCredenciales();
                    break;

                case "cuenta bloqueada":
                case "usuario bloqueado":
                case "acceso bloqueado":
                    assertTrue(paginaLogin.hayCuentaBloqueada(),
                            "No hay mensaje de cuenta bloqueada");
                    mensajeError = paginaLogin.obtenerMensajeCuentaBloqueada();
                    break;

                case "campos obligatorios":
                case "datos requeridos":
                case "campos vacíos":
                    assertTrue(paginaLogin.hayMensajesError(),
                            "No hay mensajes de error generales");
                    mensajeError = paginaLogin.obtenerMensajeError();
                    break;

                default:
                    // Verificar mensaje de error general
                    assertTrue(paginaLogin.hayMensajesError(),
                            "No hay mensajes de error para el tipo: " + tipoError);
                    mensajeError = paginaLogin.obtenerMensajeError();
            }

            assertTrue(mensajeError.isPresent(),
                    "No se pudo obtener el mensaje de error específico");

            String mensajeActual = mensajeError.get().toLowerCase();
            String tipoEsperado = tipoError.toLowerCase();

            assertTrue(mensajeActual.contains(tipoEsperado) ||
                            contieneTerminosRelacionados(mensajeActual, tipoEsperado),
                    "El mensaje de error no contiene los términos esperados. " +
                            "Tipo esperado: '" + tipoError + "', " +
                            "Mensaje actual: '" + mensajeError.get() + "'");

            registrarFinAccion("Mensaje de error verificado",
                    "Tipo: " + tipoError + ", Mensaje: " + mensajeError.get());

        } catch (Exception e) {
            registrarFinAccion("Error verificando mensaje de error", e.getMessage());
            fail("Error verificando mensaje de error '" + tipoError + "': " + e.getMessage());
        }
    }

    @Entonces("el usuario permanece en la página de login")
    public void elUsuarioPermaneceEnLaPaginaDeLogin() {
        registrarInicioAccion("Verificando que permanece en página de login");

        try {
            // Esperar un momento para posibles redirecciones
            UtileriasComunes.esperarSegundos(3);

            // Verificar que seguimos en la página de login
            String urlActual = paginaLogin.obtenerUrlActual();
            assertTrue(urlActual.contains("/login"),
                    "La URL no indica que estamos en la página de login. URL actual: " + urlActual);

            // Verificar que el formulario sigue visible
            assertTrue(paginaLogin.estaPaginaCargada(),
                    "La página de login no está completamente cargada");
            assertTrue(paginaLogin.esFormularioVisible(),
                    "El formulario de login no está visible");

            // Marcar login como fallido
            this.loginExitoso = false;

            registrarFinAccion("Permanencia en login verificada", "URL: " + urlActual);

        } catch (Exception e) {
            registrarFinAccion("Error verificando permanencia", e.getMessage());
            fail("Error verificando que permanece en página de login: " + e.getMessage());
        }
    }

    @Entonces("los campos deben estar habilitados para nuevo intento")
    public void losCamposDebenEstarHabilitadosParaNuevoIntento() {
        registrarInicioAccion("Verificando campos habilitados para reintento");

        try {
            // Verificar que los campos estén habilitados
            assertTrue(paginaLogin.esCampoEmailHabilitado(),
                    "El campo email no está habilitado para reintento");
            assertTrue(paginaLogin.esCampoPasswordHabilitado(),
                    "El campo password no está habilitado para reintento");
            assertTrue(paginaLogin.esBotonLoginHabilitado(),
                    "El botón login no está habilitado para reintento");

            // Verificar que no hay bloqueos temporales
            assertFalse(paginaLogin.hayCuentaBloqueada(),
                    "La cuenta aparece bloqueada, impidiendo reintentos");

            registrarFinAccion("Campos habilitados verificados", "Listos para reintento");

        } catch (Exception e) {
            registrarFinAccion("Error verificando campos", e.getMessage());
            fail("Error verificando que los campos están habilitados: " + e.getMessage());
        }
    }

    @Entonces("el tiempo de login debe ser menor a {int} segundos")
    public void elTiempoDeLoginDebeSerMenorASegundos(int segundosMaximos) {
        registrarInicioAccion("Verificando tiempo de login", "Máximo: " + segundosMaximos + "s");

        try {
            assertTrue(tiempoInicioLogin > 0, "No se registró el inicio del login");

            long tiempoTranscurrido = (System.currentTimeMillis() - tiempoInicioLogin) / 1000;

            assertTrue(tiempoTranscurrido <= segundosMaximos,
                    "El login tardó más de lo esperado. " +
                            "Tiempo transcurrido: " + tiempoTranscurrido + "s, " +
                            "Máximo permitido: " + segundosMaximos + "s");

            registrarFinAccion("Tiempo de login verificado",
                    "Transcurrido: " + tiempoTranscurrido + "s, Límite: " + segundosMaximos + "s");

        } catch (Exception e) {
            registrarFinAccion("Error verificando tiempo", e.getMessage());
            fail("Error verificando tiempo de login: " + e.getMessage());
        }
    }

    // ==================== MÉTODOS Y (AND) / PERO (BUT) ====================

    @Y("el sistema está funcionando correctamente")
    public void yElSistemaEstaFuncionandoCorrectamente() {
        elSistemaEstaFuncionandoCorrectamente();
    }

    @Y("que el usuario tiene credenciales válidas")
    public void yQueElUsuarioTieneCredencialesValidas() {
        elUsuarioTieneCredencialesValidas();
    }

    @Y("hace clic en el botón {string}")
    public void yHaceClicEnElBoton(String nombreBoton) {
        haceClicEnElBoton(nombreBoton);
    }

    @Y("debe ver su nombre de usuario en la barra superior")
    public void yDebeVerSuNombreDeUsuarioEnLaBarraSuperior() {
        debeVerSuNombreDeUsuarioEnLaBarraSuperior();
    }

    @Pero("debe ver un mensaje de error indicando {string}")
    public void peroDebeVerUnMensajeDeErrorIndicando(String tipoError) {
        debeVerUnMensajeDeErrorIndicando(tipoError);
    }

    @Pero("el usuario permanece en la página de login")
    public void peroElUsuarioPermaneceEnLaPaginaDeLogin() {
        elUsuarioPermaneceEnLaPaginaDeLogin();
    }

    // ==================== MÉTODOS DE UTILIDADES PRIVADAS ====================

    /**
     * Registra el inicio de una acción para trazabilidad.
     *
     * @param accion descripción de la acción
     * @param detalles detalles adicionales
     */
    private void registrarInicioAccion(String accion, String... detalles) {
        String contexto = "Step: " + accion;
        String detalleCompleto = detalles.length > 0 ? String.join(" | ", detalles) : "";

        logger.info("INICIANDO: {} {}", accion, detalleCompleto);
        UtileriasComunes.registrarAccionTrazabilidad("INICIO - " + accion, contexto, detalleCompleto);

        try {
            trazabilidad.registrarPaso(accion, contexto + " | " + detalleCompleto);
        } catch (Exception e) {
            logger.debug("Error registrando trazabilidad de inicio: {}", e.getMessage());
        }
    }

    /**
     * Registra el fin de una acción para trazabilidad.
     *
     * @param accion descripción de la acción
     * @param resultado resultado de la acción
     */
    private void registrarFinAccion(String accion, String resultado) {
        logger.info("COMPLETADO: {} - {}", accion, resultado);
        UtileriasComunes.registrarAccionTrazabilidad("FIN - " + accion, resultado);

        try {
            trazabilidad.registrarResultado(accion, resultado);
        } catch (Exception e) {
            logger.debug("Error registrando resultado en trazabilidad: {}", e.getMessage());
        }
    }

    /**
     * Verifica si un mensaje contiene términos relacionados al tipo de error.
     *
     * @param mensaje mensaje actual
     * @param tipoEsperado tipo de error esperado
     * @return true si contiene términos relacionados
     */
    private boolean contieneTerminosRelacionados(String mensaje, String tipoEsperado) {
        String[][] terminosRelacionados = {
                {"credenciales", "email", "contraseña", "password", "usuario", "login", "acceso"},
                {"bloqueada", "bloqueado", "suspendida", "deshabilitada", "inactiva"},
                {"obligatorios", "requeridos", "vacíos", "necesarios", "faltantes", "completo"}
        };

        for (String[] grupo : terminosRelacionados) {
            for (String termino : grupo) {
                if (tipoEsperado.contains(termino) && mensaje.contains(termino)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Obtiene el contexto del usuario actual.
     *
     * @return Usuario en contexto o null si no hay
     */
    public Usuario obtenerUsuarioContexto() {
        return usuarioContexto;
    }

    /**
     * Verifica si el último login fue exitoso.
     *
     * @return true si el login fue exitoso
     */
    public boolean fueLoginExitoso() {
        return loginExitoso;
    }

    /**
     * Obtiene el tiempo transcurrido desde el inicio del login.
     *
     * @return tiempo en milisegundos o 0 si no se ha iniciado
     */
    public long obtenerTiempoLogin() {
        return tiempoInicioLogin > 0 ? System.currentTimeMillis() - tiempoInicioLogin : 0;
    }

    /**
     * Limpia el contexto del escenario actual.
     * Útil para limpiar entre escenarios o al finalizar.
     */
    public void limpiarContexto() {
        logger.debug("Limpiando contexto de PasosLogin");

        this.emailUsuario = null;
        this.passwordUsuario = null;
        this.usuarioContexto = null;
        this.loginExitoso = false;
        this.recordarSesion = false;
        this.tiempoInicioLogin = 0;

        // Limpiar página si está disponible
        if (paginaLogin != null) {
            try {
                paginaLogin.limpiarRecursos();
            } catch (Exception e) {
                logger.debug("Error limpiando recursos de página: {}", e.getMessage());
            }
        }
    }
}