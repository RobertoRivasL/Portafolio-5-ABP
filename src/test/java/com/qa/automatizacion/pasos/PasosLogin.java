package com.qa.automatizacion.pasos;

import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import com.qa.automatizacion.paginas.PaginaLogin;
import com.qa.automatizacion.paginas.PaginaDashboard;
import com.qa.automatizacion.utilidades.HelperTrazabilidad;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions para los escenarios de login.
 * Implementa la lógica de los pasos definidos en login.feature
 *
 * Principios aplicados:
 * - Separación de Intereses: Se enfoca únicamente en los pasos de login
 * - Abstracción: Utiliza Page Objects para ocultar la complejidad de la UI
 * - Modularidad: Métodos pequeños y específicos para cada paso
 */
public class PasosLogin {

    private static final Logger logger = LoggerFactory.getLogger(PasosLogin.class);

    private final PropiedadesAplicacion propiedades;
    private final PaginaLogin paginaLogin;
    private final PaginaDashboard paginaDashboard;
    private final HelperTrazabilidad trazabilidad;

    // Variables de contexto para el escenario
    private String emailUsuario;
    private String passwordUsuario;
    private LocalDateTime tiempoInicioLogin;
    private int intentosFallidos;

    public PasosLogin() {
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();
        this.paginaLogin = new PaginaLogin();
        this.paginaDashboard = new PaginaDashboard();
        this.trazabilidad = new HelperTrazabilidad();
        this.intentosFallidos = 0;
    }

    // ==================== PASOS DADO (Given) ====================

    @Dado("que el usuario está en la página de login")
    public void elUsuarioEstaEnLaPaginaDeLogin() {
        logger.info("Navegando a la página de login");
        trazabilidad.registrarPaso("HU-001", "Navegación a página de login");

        String urlLogin = propiedades.obtenerUrlLogin();
        ConfiguradorNavegador.navegarA(urlLogin);

        // Verificar que la página se cargó correctamente
        assertTrue(paginaLogin.estaPaginaCargada(),
                "La página de login no se cargó correctamente");
        logger.info("Página de login cargada exitosamente: {}", urlLogin);
    }

    @Dado("el sistema está funcionando correctamente")
    public void elSistemaEstaFuncionandoCorrectamente() {
        logger.info("Verificando que el sistema esté funcionando");
        trazabilidad.registrarPaso("HU-001", "Verificación de estado del sistema");

        // Verificar elementos básicos de la página
        assertTrue(paginaLogin.esFormularioVisible(),
                "El formulario de login no está visible");
        assertTrue(paginaLogin.esBotonLoginVisible(),
                "El botón de login no está visible");

        logger.info("Sistema funcionando correctamente");
    }

    @Dado("que el usuario tiene credenciales válidas")
    public void elUsuarioTieneCredencialesValidas(DataTable credenciales) {
        logger.info("Configurando credenciales válidas para el usuario");
        trazabilidad.registrarPaso("HU-001", "Configuración de credenciales válidas");

        List<Map<String, String>> datos = credenciales.asMaps(String.class, String.class);
        Map<String, String> primerRegistro = datos.get(0);

        this.emailUsuario = primerRegistro.get("email");
        this.passwordUsuario = primerRegistro.get("password");

        assertNotNull(emailUsuario, "Email no puede ser nulo");
        assertNotNull(passwordUsuario, "Password no puede ser nulo");

        logger.info("Credenciales configuradas para email: {}", emailUsuario);
    }

    @Dado("que el usuario tiene credenciales inválidas")
    public void elUsuarioTieneCredencialesInvalidas(DataTable credenciales) {
        logger.info("Configurando credenciales inválidas para el usuario");
        trazabilidad.registrarPaso("HU-001", "Configuración de credenciales inválidas");

        List<Map<String, String>> datos = credenciales.asMaps(String.class, String.class);
        Map<String, String> primerRegistro = datos.get(0);

        this.emailUsuario = primerRegistro.get("email");
        this.passwordUsuario = primerRegistro.get("password");

        logger.info("Credenciales inválidas configuradas para email: {}", emailUsuario);
    }

    @Dado("que el usuario ingresa un email con formato {string}")
    public void elUsuarioIngresaUnEmailConFormato(String email) {
        logger.info("Ingresando email con formato específico: {}", email);
        trazabilidad.registrarPaso("HU-001", "Ingreso de email con formato específico");

        this.emailUsuario = email;
        paginaLogin.ingresarEmail(email);
    }

    @Dado("ingresa una contraseña {string}")
    public void ingresaUnaContrasena(String password) {
        logger.info("Ingresando contraseña");
        trazabilidad.registrarPaso("HU-001", "Ingreso de contraseña");

        this.passwordUsuario = password;
        paginaLogin.ingresarPassword(password);
    }

    @Dado("que el usuario ha fallado el login {int} veces consecutivas")
    public void elUsuarioHaFalladoElLoginVecesConsecutivas(int numeroIntentos) {
        logger.info("Simulando {} intentos fallidos de login", numeroIntentos);
        trazabilidad.registrarPaso("HU-001", "Simulación de intentos fallidos: " + numeroIntentos);

        this.intentosFallidos = numeroIntentos;

        // Simular intentos fallidos
        for (int i = 0; i < numeroIntentos; i++) {
            paginaLogin.ingresarEmail("usuario.fallido@test.com");
            paginaLogin.ingresarPassword("passwordIncorrecto" + i);
            paginaLogin.hacerClickBotonLogin();

            // Verificar mensaje de error en cada intento
            assertTrue(paginaLogin.esMensajeErrorVisible(),
                    "No se mostró mensaje de error en intento " + (i + 1));
        }

        logger.info("Completados {} intentos fallidos", numeroIntentos);
    }

    @Dado("marca la opción {string}")
    public void marcaLaOpcion(String opcion) {
        logger.info("Marcando la opción: {}", opcion);
        trazabilidad.registrarPaso("HU-001", "Marcado de opción: " + opcion);

        if ("Recordar sesión".equals(opcion)) {
            paginaLogin.marcarRecordarSesion();
            assertTrue(paginaLogin.estaRecordarSesionMarcado(),
                    "La opción 'Recordar sesión' no se marcó correctamente");
        }
    }

    @Dado("que el usuario cambió su contraseña recientemente")
    public void elUsuarioCambioSuContrasenaRecientemente() {
        logger.info("Simulando cambio reciente de contraseña");
        trazabilidad.registrarPaso("HU-001", "Simulación de cambio de contraseña");

        // En un escenario real, esto podría implicar actualizar la base de datos
        // Para efectos de la prueba, solo registramos el evento
        logger.info("Contraseña cambiada recientemente - simulado");
    }

    @Dado("la nueva contraseña es {string}")
    public void laNuevaContrasenaEs(String nuevaPassword) {
        logger.info("Configurando nueva contraseña");
        trazabilidad.registrarPaso("HU-001", "Configuración de nueva contraseña");

        this.passwordUsuario = nuevaPassword;
    }

    // ==================== PASOS CUANDO (When) ====================

    @Cuando("el usuario ingresa sus credenciales")
    public void elUsuarioIngresaSusCredenciales() {
        logger.info("Usuario ingresando credenciales: {}", emailUsuario);
        trazabilidad.registrarPaso("HU-001", "Ingreso de credenciales");

        assertNotNull(emailUsuario, "Email debe estar configurado");
        assertNotNull(passwordUsuario, "Password debe estar configurado");

        paginaLogin.ingresarEmail(emailUsuario);
        paginaLogin.ingresarPassword(passwordUsuario);

        logger.info("Credenciales ingresadas correctamente");
    }

    @Cuando("hace clic en el botón {string}")
    public void haceClickEnElBoton(String nombreBoton) {
        logger.info("Haciendo clic en el botón: {}", nombreBoton);
        trazabilidad.registrarPaso("HU-001", "Clic en botón: " + nombreBoton);

        this.tiempoInicioLogin = LocalDateTime.now();

        switch (nombreBoton) {
            case "Iniciar Sesión" -> {
                assertTrue(paginaLogin.esBotonLoginHabilitado(),
                        "El botón 'Iniciar Sesión' no está habilitado");
                paginaLogin.hacerClickBotonLogin();
            }
            default -> fail("Botón no reconocido: " + nombreBoton);
        }

        logger.info("Clic realizado en botón: {}", nombreBoton);
    }

    @Cuando("el usuario hace clic en el botón {string} sin llenar los campos")
    public void elUsuarioHaceClickEnElBotonSinLlenarLosCampos(String nombreBoton) {
        logger.info("Intentando hacer clic en '{}' sin llenar campos", nombreBoton);
        trazabilidad.registrarPaso("HU-001", "Intento de login sin credenciales");

        // Verificar que los campos estén vacíos
        assertTrue(paginaLogin.esCampoEmailVacio(), "El campo email debería estar vacío");
        assertTrue(paginaLogin.esCampoPasswordVacio(), "El campo password debería estar vacío");

        // Intentar hacer clic en el botón
        if ("Iniciar Sesión".equals(nombreBoton)) {
            paginaLogin.hacerClickBotonLogin();
        }
    }

    @Cuando("intenta hacer login nuevamente")
    public void intentaHacerLoginNuevamente() {
        logger.info("Intentando hacer login después de bloqueo");
        trazabilidad.registrarPaso("HU-001", "Intento de login después de bloqueo");

        paginaLogin.ingresarEmail("usuario.bloqueado@test.com");
        paginaLogin.ingresarPassword("cualquierPassword");
        paginaLogin.hacerClickBotonLogin();
    }

    @Cuando("hace login exitosamente")
    public void haceLoginExitosamente() {
        logger.info("Realizando login exitoso");
        trazabilidad.registrarPaso("HU-001", "Login exitoso");

        // Usar credenciales válidas predefinidas
        this.emailUsuario = "test@test.com";
        this.passwordUsuario = "password123";

        paginaLogin.ingresarEmail(emailUsuario);
        paginaLogin.ingresarPassword(passwordUsuario);
        paginaLogin.hacerClickBotonLogin();

        // Verificar que el login fue exitoso
        assertTrue(paginaDashboard.estaPaginaCargada(),
                "No se pudo completar el login exitosamente");
    }

    @Cuando("cierra el navegador")
    public void cierraElNavegador() {
        logger.info("Cerrando navegador para simular cierre de sesión");
        trazabilidad.registrarPaso("HU-001", "Cierre de navegador");

        ConfiguradorNavegador.cerrarDriver();
    }

    @Cuando("vuelve a abrir la aplicación")
    public void vuelveAAbrirLaAplicacion() {
        logger.info("Reabriendo aplicación");
        trazabilidad.registrarPaso("HU-001", "Reapertura de aplicación");

        String urlBase = propiedades.obtenerUrlBase();
        ConfiguradorNavegador.navegarA(urlBase);
    }

    @Cuando("usa la tecla Tab para navegar entre los campos")
    public void usaLaTeclaTabParaNavegar() {
        logger.info("Navegando con tecla Tab");
        trazabilidad.registrarPaso("HU-001", "Navegación con Tab");

        paginaLogin.navegarConTab();
    }

    @Cuando("hace login")
    public void haceLogin() {
        logger.info("Ejecutando proceso de login");
        trazabilidad.registrarPaso("HU-001", "Proceso de login");

        this.tiempoInicioLogin = LocalDateTime.now();

        // Usar credenciales válidas por defecto
        if (emailUsuario == null || passwordUsuario == null) {
            this.emailUsuario = "test@test.com";
            this.passwordUsuario = "password123";
        }

        paginaLogin.ingresarEmail(emailUsuario);
        paginaLogin.ingresarPassword(passwordUsuario);
        paginaLogin.hacerClickBotonLogin();
    }

    @Cuando("intenta hacer login con la contraseña anterior {string}")
    public void intentaHacerLoginConLaContrasenaAnterior(String passwordAnterior) {
        logger.info("Intentando login con contraseña anterior");
        trazabilidad.registrarPaso("HU-001", "Intento con contraseña anterior");

        paginaLogin.limpiarCampos();
        paginaLogin.ingresarEmail(emailUsuario != null ? emailUsuario : "test@test.com");
        paginaLogin.ingresarPassword(passwordAnterior);
        paginaLogin.hacerClickBotonLogin();
    }

    @Cuando("hace login con la nueva contraseña {string}")
    public void haceLoginConLaNuevaContrasena(String nuevaPassword) {
        logger.info("Haciendo login con nueva contraseña");
        trazabilidad.registrarPaso("HU-001", "Login con nueva contraseña");

        paginaLogin.limpiarCampos();
        paginaLogin.ingresarEmail(emailUsuario != null ? emailUsuario : "test@test.com");
        paginaLogin.ingresarPassword(nuevaPassword);
        paginaLogin.hacerClickBotonLogin();
    }

    // ==================== PASOS ENTONCES (Then) ====================

    @Entonces("el usuario debe ser redirigido al dashboard")
    public void elUsuarioDebeSerRedirigidoAlDashboard() {
        logger.info("Verificando redirección al dashboard");
        trazabilidad.registrarPaso("HU-001", "Verificación de redirección al dashboard");

        // Esperar a que la página se cargue
        assertTrue(paginaDashboard.esperarCargaPagina(),
                "El dashboard no se cargó en el tiempo esperado");

        // Verificar que estamos en el dashboard
        assertTrue(paginaDashboard.estaPaginaCargada(),
                "No se redirigió correctamente al dashboard");

        String urlActual = ConfiguradorNavegador.obtenerUrlActual();
        String urlEsperada = propiedades.obtenerUrlDashboard();

        assertTrue(urlActual.contains("dashboard") || urlActual.equals(urlEsperada),
                "URL actual no corresponde al dashboard. Actual: " + urlActual);

        logger.info("Redirección al dashboard verificada exitosamente");
    }

    @Entonces("debe ver el mensaje de bienvenida {string}")
    public void debeVerElMensajeDeBienvenida(String mensajeEsperado) {
        logger.info("Verificando mensaje de bienvenida");
        trazabilidad.registrarPaso("HU-001", "Verificación de mensaje de bienvenida");

        assertTrue(paginaDashboard.esMensajeBienvenidaVisible(),
                "El mensaje de bienvenida no es visible");

        String mensajeActual = paginaDashboard.obtenerMensajeBienvenida();
        assertTrue(mensajeActual.contains(mensajeEsperado),
                "Mensaje de bienvenida incorrecto. Esperado: '" + mensajeEsperado +
                        "', Actual: '" + mensajeActual + "'");

        logger.info("Mensaje de bienvenida verificado: {}", mensajeActual);
    }

    @Entonces("debe ver su nombre de usuario en la barra superior")
    public void debeVerSuNombreDeUsuarioEnLaBarraSuperior() {
        logger.info("Verificando nombre de usuario en barra superior");
        trazabilidad.registrarPaso("HU-001", "Verificación de nombre en barra superior");

        assertTrue(paginaDashboard.esNombreUsuarioVisible(),
                "El nombre de usuario no es visible en la barra superior");

        String nombreVisible = paginaDashboard.obtenerNombreUsuario();
        assertNotNull(nombreVisible, "El nombre de usuario no debe ser nulo");
        assertFalse(nombreVisible.trim().isEmpty(), "El nombre de usuario no debe estar vacío");

        logger.info("Nombre de usuario verificado en barra superior: {}", nombreVisible);
    }

    @Entonces("debe ver el mensaje de error {string}")
    public void debeVerElMensajeDeError(String mensajeErrorEsperado) {
        logger.info("Verificando mensaje de error: {}", mensajeErrorEsperado);
        trazabilidad.registrarPaso("HU-001", "Verificación de mensaje de error");

        assertTrue(paginaLogin.esMensajeErrorVisible(),
                "El mensaje de error no es visible");

        String mensajeErrorActual = paginaLogin.obtenerMensajeError();
        assertTrue(mensajeErrorActual.contains(mensajeErrorEsperado),
                "Mensaje de error incorrecto. Esperado: '" + mensajeErrorEsperado +
                        "', Actual: '" + mensajeErrorActual + "'");

        logger.info("Mensaje de error verificado: {}", mensajeErrorActual);
    }

    @Entonces("debe permanecer en la página de login")
    public void debePermanecerenLaPaginaDeLogin() {
        logger.info("Verificando permanencia en página de login");
        trazabilidad.registrarPaso("HU-001", "Verificación de permanencia en login");

        assertTrue(paginaLogin.estaPaginaCargada(),
                "No permaneció en la página de login");

        String urlActual = ConfiguradorNavegador.obtenerUrlActual();
        String urlLogin = propiedades.obtenerUrlLogin();

        assertTrue(urlActual.contains("login") || urlActual.equals(urlLogin),
                "No permaneció en la página de login. URL actual: " + urlActual);

        logger.info("Verificación exitosa: permaneció en página de login");
    }

    @Entonces("los campos de entrada deben estar vacíos")
    public void losCamposDeEntradaDebenEstarVacios() {
        logger.info("Verificando que los campos estén vacíos");
        trazabilidad.registrarPaso("HU-001", "Verificación de campos vacíos");

        assertTrue(paginaLogin.esCampoEmailVacio(),
                "El campo email no está vacío");
        assertTrue(paginaLogin.esCampoPasswordVacio(),
                "El campo password no está vacío");

        logger.info("Campos de entrada verificados como vacíos");
    }

    @Entonces("el botón {string} debe permanecer deshabilitado")
    public void elBotonDebePermanecer Deshabilitado(String nombreBoton) {
        logger.info("Verificando que el botón '{}' esté deshabilitado", nombreBoton);
        trazabilidad.registrarPaso("HU-001", "Verificación de botón deshabilitado");

        if ("Iniciar Sesión".equals(nombreBoton)) {
            assertFalse(paginaLogin.esBotonLoginHabilitado(),
                    "El botón 'Iniciar Sesión' no debería estar habilitado");
        }

        logger.info("Botón '{}' verificado como deshabilitado", nombreBoton);
    }

    @Entonces("el sistema debe responder en menos de {int} segundos")
    public void elSistemaDebeResponderEnMenosDe Segundos(int segundosMaximos) {
        logger.info("Verificando tiempo de respuesta del sistema");
        trazabilidad.registrarPaso("HU-001", "Verificación de tiempo de respuesta");

        assertNotNull(tiempoInicioLogin, "No se registró el tiempo de inicio del login");

        LocalDateTime tiempoActual = LocalDateTime.now();
        long segundosTranscurridos = java.time.Duration.between(tiempoInicioLogin, tiempoActual).getSeconds();

        assertTrue(segundosTranscurridos < segundosMaximos,
                "El sistema tardó " + segundosTranscurridos +
                        " segundos, que excede el límite de " + segundosMaximos + " segundos");

        logger.info("Tiempo de respuesta verificado: {} segundos (límite: {} segundos)",
                segundosTranscurridos, segundosMaximos);
    }

    @Entonces("debe ser redirigido al dashboard en menos de {int} segundos totales")
    public void debeSerRedirigidoAlDashboardEnMenosDeSegundosTotales(int segundosTotales) {
        logger.info("Verificando tiempo total de redirección");
        trazabilidad.registrarPaso("HU-001", "Verificación de tiempo total de redirección");

        assertNotNull(tiempoInicioLogin, "No se registró el tiempo de inicio del login");

        // Esperar a que la redirección se complete
        assertTrue(paginaDashboard.esperarCargaPagina(),
                "El dashboard no se cargó en el tiempo esperado");

        LocalDateTime tiempoActual = LocalDateTime.now();
        long segundosTranscurridos = java.time.Duration.between(tiempoInicioLogin, tiempoActual).getSeconds();

        assertTrue(segundosTranscurridos < segundosTotales,
                "La redirección tardó " + segundosTranscurridos +
                        " segundos, que excede el límite de " + segundosTotales + " segundos");

        logger.info("Tiempo total de redirección verificado: {} segundos (límite: {} segundos)",
                segundosTranscurridos, segundosTotales);
    }

    @Entonces("debe estar automáticamente logueado")
    public void debeEstarAutomaticamenteLogueado() {
        logger.info("Verificando login automático");
        trazabilidad.registrarPaso("HU-001", "Verificación de login automático");

        // Verificar que está en el dashboard sin haber hecho login manual
        assertTrue(paginaDashboard.estaPaginaCargada() ||
                        ConfiguradorNavegador.obtenerUrlActual().contains("dashboard"),
                "No se realizó el login automático");

        logger.info("Login automático verificado exitosamente");
    }

    @Entonces("debe poder moverse secuencialmente entre {string}")
    public void debePodermoverseSecuencialmenteEntre(String elementos) {
        logger.info("Verificando navegación secuencial entre: {}", elementos);
        trazabilidad.registrarPaso("HU-001", "Verificación de navegación secuencial");

        String[] elementosArray = elementos.split(",");
        for (String elemento : elementosArray) {
            elemento = elemento.trim();
            assertTrue(paginaDashboard.esElementoNavegable(elemento),
                    "No se puede navegar al elemento: " + elemento);
            logger.debug("Navegación exitosa a: {}", elemento);
        }

        logger.info("Verificación exitosa: navegación secuencial completada");
    }

    @Entonces("cada campo debe tener el foco visual claramente visible")
    public void cadaCampoDebeTenerElFocoVisualClaramenteVisible() {
        logger.info("Verificando visibilidad del foco visual");
        trazabilidad.registrarPaso("HU-001", "Verificación de foco visual");

        assertTrue(paginaLogin.esFocoVisualVisible(),
                "El foco visual no es claramente visible");

        logger.info("Foco visual verificado como visible");
    }

    @Entonces("debe acceder exitosamente al sistema")
    public void debeAccederExitosamenteAlSistema() {
        logger.info("Verificando acceso exitoso al sistema");
        trazabilidad.registrarPaso("HU-001", "Verificación de acceso exitoso");

        assertTrue(paginaDashboard.estaPaginaCargada(),
                "No se pudo acceder exitosamente al sistema");

        // Verificar elementos que confirman el acceso exitoso
        assertTrue(paginaDashboard.esMensajeBienvenidaVisible() ||
                        paginaDashboard.esNombreUsuarioVisible(),
                "No se encontraron elementos que confirmen el acceso exitoso");

        logger.info("Acceso exitoso al sistema verificado");
    }
}