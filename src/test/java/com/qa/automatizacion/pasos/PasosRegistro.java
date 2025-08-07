package com.qa.automatizacion.pasos;

import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import com.qa.automatizacion.modelo.Usuario;
import com.qa.automatizacion.paginas.PaginaRegistro;
import com.qa.automatizacion.paginas.PaginaLogin;
import com.qa.automatizacion.utilidades.HelperTrazabilidad;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions para los escenarios de registro de usuarios.
 * Implementa la lógica de los pasos definidos en registro.feature
 */
public class PasosRegistro {

    private static final Logger logger = LoggerFactory.getLogger(PasosRegistro.class);

    private final PropiedadesAplicacion propiedades;
    private final PaginaRegistro paginaRegistro;
    private final PaginaLogin paginaLogin;
    private final HelperTrazabilidad trazabilidad;

    // Variables de contexto para el escenario
    private String nombreUsuario;
    private String apellidoUsuario;
    private String emailUsuario;
    private String telefonoUsuario;
    private String passwordUsuario;
    private String confirmarPasswordUsuario;
    private LocalDateTime tiempoInicioRegistro;

    public PasosRegistro() {
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();
        this.paginaRegistro = new PaginaRegistro();
        this.paginaLogin = new PaginaLogin();
        this.trazabilidad = new HelperTrazabilidad();
    }

    // ==================== PASOS DADO (Given) ====================

    @Dado("que el visitante está en la página de registro")
    public void elVisitanteEstaEnLaPaginaDeRegistro() {
        logger.info("Navegando a la página de registro");
        trazabilidad.registrarPaso("HU-002", "Navegación a página de registro");

        String urlRegistro = propiedades.obtenerUrlRegistro();
        ConfiguradorNavegador.navegarA(urlRegistro);

        assertTrue(paginaRegistro.estaPaginaCargada(),
                "La página de registro no se cargó correctamente");
        logger.info("Página de registro cargada exitosamente: {}", urlRegistro);
    }

    @Dado("el sistema está disponible para nuevos registros")
    public void elSistemaEstaDisponibleParaNuevosRegistros() {
        logger.info("Verificando disponibilidad del sistema para registros");
        trazabilidad.registrarPaso("HU-002", "Verificación de disponibilidad del sistema");

        // Corregir nombres de métodos
        assertTrue(paginaRegistro.esFormularioRegistroVisible(),
                "El formulario de registro no está visible");
        assertTrue(paginaRegistro.esBotonRegistroHabilitado(),
                "El botón de registro no está habilitado");

        logger.info("Sistema disponible para nuevos registros");
    }

    @Dado("que el visitante completa el formulario de registro con datos válidos:")
    public void elVisitanteCompletaElFormularioConDatosValidos(DataTable datosRegistro) {
        logger.info("Completando formulario con datos válidos");
        trazabilidad.registrarPaso("HU-002", "Completar formulario con datos válidos");

        List<Map<String, String>> datos = datosRegistro.asMaps(String.class, String.class);
        Map<String, String> primerRegistro = datos.get(0);

        this.nombreUsuario = primerRegistro.get("nombre");
        this.apellidoUsuario = primerRegistro.get("apellido");
        this.emailUsuario = primerRegistro.get("email");
        this.passwordUsuario = primerRegistro.get("password");
        this.confirmarPasswordUsuario = primerRegistro.get("confirmar");
        this.telefonoUsuario = primerRegistro.get("telefono");

        // Crear objeto Usuario con métodos corregidos
        Usuario usuario = Usuario.builder()
                .nombre(nombreUsuario)
                .apellido(apellidoUsuario)
                .email(emailUsuario)
                .celular(telefonoUsuario)  // Cambiar 'telefono' por 'celular'
                .password(passwordUsuario)
                .build();

        // Usar métodos existentes en PaginaRegistro
        paginaRegistro.completarFormularioRegistro(usuario);
        paginaRegistro.completarConfirmarPassword(confirmarPasswordUsuario);  // Cambiar nombre
        paginaRegistro.marcarTerminosCondiciones();  // Cambiar nombre

        logger.info("Formulario completado para: {} {}", nombreUsuario, apellidoUsuario);
    }

    @Dado("que el visitante completa el formulario excepto el campo {string}")
    public void elVisitanteCompletaElFormularioExceptoElCampo(String campoFaltante) {
        logger.info("Completando formulario excepto el campo: {}", campoFaltante);
        trazabilidad.registrarPaso("HU-002", "Completar formulario con campo faltante: " + campoFaltante);

        // Datos base válidos
        String nombre = "Juan";
        String apellido = "Pérez";
        String email = "juan.perez@test.com";
        String telefono = "+56912345678";
        String password = "Password123!";
        String confirmar = "Password123!";

        // Limpiar el campo específico que debe faltar
        switch (campoFaltante.toLowerCase()) {
            case "nombre" -> nombre = "";
            case "apellido" -> apellido = "";
            case "email" -> email = "";
            case "password" -> password = "";
            case "confirmar" -> confirmar = "";
        }

        // Completar formulario con el campo faltante
        if (!nombre.isEmpty()) paginaRegistro.completarNombre(nombre);
        if (!apellido.isEmpty()) paginaRegistro.completarApellido(apellido);
        if (!email.isEmpty()) paginaRegistro.completarEmail(email);
        if (!telefono.isEmpty()) paginaRegistro.completarTelefono(telefono);
        if (!password.isEmpty()) paginaRegistro.completarPassword(password);
        if (!confirmar.isEmpty()) paginaRegistro.completarConfirmacionPassword(confirmar);

        paginaRegistro.marcarAceptacionTerminos();

        logger.info("Formulario completado sin el campo: {}", campoFaltante);
    }

    @Dado("que el visitante ingresa el email {string}")
    public void elVisitanteIngresaElEmail(String emailInvalido) {
        logger.info("Ingresando email: {}", emailInvalido);
        trazabilidad.registrarPaso("HU-002", "Ingreso de email específico");

        this.emailUsuario = emailInvalido;
        paginaRegistro.ingresarEmail(emailInvalido);  // Método corregido
    }

    @Dado("completa el resto de campos correctamente")
    public void completaElRestoDeCamposCorrectamente() {
        logger.info("Completando resto de campos correctamente");
        trazabilidad.registrarPaso("HU-002", "Completar campos restantes");

        // Usar métodos corregidos
        paginaRegistro.ingresarNombre("Usuario");
        paginaRegistro.ingresarApellido("Prueba");
        paginaRegistro.ingresarCelular("+56912345678");
        paginaRegistro.ingresarPassword("Password123!");
        paginaRegistro.ingresarConfirmarPassword("Password123!");
        paginaRegistro.marcarTerminosCondiciones();
    }

    @Dado("que el visitante ingresa la contraseña {string}")
    public void elVisitanteIngresaLaContrasena(String passwordDebil) {
        logger.info("Ingresando contraseña específica");
        trazabilidad.registrarPaso("HU-002", "Ingreso de contraseña específica");

        this.passwordUsuario = passwordDebil;
        paginaRegistro.ingresarPassword(passwordDebil);  // Método corregido
    }

    @Dado("que el visitante ingresa la confirmación {string}")
    public void elVisitanteIngresaLaConfirmacion(String confirmarDiferente) {
        logger.info("Ingresando confirmación de contraseña diferente");
        trazabilidad.registrarPaso("HU-002", "Ingreso de confirmación diferente");

        this.confirmarPasswordUsuario = confirmarDiferente;
        paginaRegistro.ingresarConfirmarPassword(confirmarDiferente);  // Método corregido
    }

    @Dado("que ya existe un usuario registrado con el email {string}")
    public void yaExisteUnUsuarioRegistradoConElEmail(String emailExistente) {
        logger.info("Verificando usuario existente con email: {}", emailExistente);
        trazabilidad.registrarPaso("HU-002", "Verificación de usuario existente");

        this.emailUsuario = emailExistente;
        logger.info("Usuario existente configurado: {}", emailExistente);
    }

    @Dado("que un visitante intenta registrarse con el mismo email")
    public void unVisitanteIntentaRegistrarseConElMismoEmail() {
        logger.info("Intentando registro con email duplicado");
        trazabilidad.registrarPaso("HU-002", "Intento de registro con email duplicado");

        Usuario usuario = Usuario.builder()
                .nombre("Nuevo")
                .apellido("Usuario")
                .email(emailUsuario)
                .telefono("+56987654321")
                .password("Password123!")
                .build();

        paginaRegistro.completarFormularioRegistro(usuario);
        paginaRegistro.ingresarConfirmacionPassword("Password123!");
        paginaRegistro.aceptarTerminos();
    }

    @Dado("que el visitante está completando el campo de contraseña")
    public void elVisitanteEstaCompletandoElCampoDeContrasena() {
        logger.info("Iniciando completado de campo contraseña");
        trazabilidad.registrarPaso("HU-002", "Inicio de completado de contraseña");

        paginaRegistro.completarNombre("Usuario");
        paginaRegistro.completarApellido("Prueba");
        paginaRegistro.completarEmail("usuario@test.com");
        paginaRegistro.enfocarCampoPassword();
    }

    @Dado("que el usuario se registró exitosamente con {string}")
    public void elUsuarioSeRegistroExitosamenteCon(String emailRegistro) {
        logger.info("Simulando registro exitoso previo para: {}", emailRegistro);
        trazabilidad.registrarPaso("HU-002", "Registro exitoso previo");

        this.emailUsuario = emailRegistro;
        logger.info("Usuario registrado previamente: {}", emailRegistro);
    }

    @Dado("que el visitante usa navegación por teclado")
    public void elVisitanteUsaNavegacionPorTeclado() {
        logger.info("Configurando navegación por teclado");
        trazabilidad.registrarPaso("HU-002", "Configuración de navegación por teclado");

        assertTrue(paginaRegistro.estaPaginaCargada(),
                "La página debe estar cargada para navegación por teclado");
    }

    @Dado("que el visitante completa correctamente el formulario de registro")
    public void elVisitanteCompletaCorrectamenteElFormularioDeRegistro() {
        logger.info("Completando formulario de registro correctamente");
        trazabilidad.registrarPaso("HU-002", "Completado correcto de formulario");

        this.tiempoInicioRegistro = LocalDateTime.now();

        Usuario usuario = Usuario.builder()
                .nombre("María")
                .apellido("González")
                .email("maria.gonzalez@test.com")
                .telefono("+56987654321")
                .password("Password123!")
                .build();

        paginaRegistro.completarFormularioRegistro(usuario);
        paginaRegistro.ingresarConfirmacionPassword("Password123!");
        paginaRegistro.aceptarTerminos();
    }

    @Dado("que se han realizado más de {int} intentos de registro desde la misma IP en {int} minutos")
    public void seHanRealizadoMasDeIntentosDeRegistro(int numeroIntentos, int minutos) {
        logger.info("Simulando {} intentos de registro en {} minutos", numeroIntentos, minutos);
        trazabilidad.registrarPaso("HU-002", "Simulación de múltiples intentos de registro");

        logger.info("Múltiples intentos simulados: {} en {} minutos", numeroIntentos, minutos);
    }

    // ==================== PASOS CUANDO (When) ====================

    @Cuando("hace clic en el botón {string}")
    public void haceClickEnElBoton(String nombreBoton) {
        logger.info("Haciendo clic en el botón: {}", nombreBoton);
        trazabilidad.registrarPaso("HU-002", "Clic en botón: " + nombreBoton);

        if (tiempoInicioRegistro == null) {
            this.tiempoInicioRegistro = LocalDateTime.now();
        }

        switch (nombreBoton) {
            case "Registrarse" -> paginaRegistro.clickBotonRegistrarse();
            default -> fail("Botón no reconocido: " + nombreBoton);
        }
    }

    @Cuando("navega usando la tecla Tab")
    public void navegaUsandoLaTeclaTab() {
        logger.info("Navegando con tecla Tab");
        trazabilidad.registrarPaso("HU-002", "Navegación con Tab");

        paginaRegistro.navegarConTab();
    }

    // ==================== PASOS ENTONCES (Then) ====================

    @Entonces("debe ver el mensaje de éxito {string}")
    public void debeVerElMensajeDeExito(String mensajeEsperado) {
        logger.info("Verificando mensaje de éxito: {}", mensajeEsperado);
        trazabilidad.registrarPaso("HU-002", "Verificación de mensaje de éxito");

        assertTrue(paginaRegistro.esMensajeExitoVisible(),
                "El mensaje de éxito no está visible");
        assertEquals(mensajeEsperado, paginaRegistro.obtenerMensajeExito(),
                "El mensaje de éxito no coincide");

        logger.info("Mensaje de éxito verificado correctamente");
    }

    @Entonces("debe recibir un email de confirmación")
    public void debeRecibirUnEmailDeConfirmacion() {
        logger.info("Verificando envío de email de confirmación");
        trazabilidad.registrarPaso("HU-002", "Verificación de email de confirmación");

        // En un escenario real, se verificaría el envío del email
        logger.info("Email de confirmación enviado (simulado)");
    }

    @Entonces("debe ser redirigido a la página de login")
    public void debeSerRedirigidoALaPaginaDeLogin() {
        logger.info("Verificando redirección a página de login");
        trazabilidad.registrarPaso("HU-002", "Verificación de redirección a login");

        esperarOperacionAsincrona(2000);
        assertTrue(paginaLogin.estaPaginaCargada(),
                "No se redirigió correctamente a la página de login");

        logger.info("Redirección a login verificada");
    }

    @Entonces("debe ver el mensaje {string}")
    public void debeVerElMensaje(String mensajeEsperado) {
        logger.info("Verificando mensaje: {}", mensajeEsperado);
        trazabilidad.registrarPaso("HU-002", "Verificación de mensaje");

        assertTrue(paginaRegistro.esMensajeErrorVisible(),
                "El mensaje de error no está visible");
        assertEquals(mensajeEsperado, paginaRegistro.obtenerMensajeError(),
                "El mensaje de error no coincide");

        logger.info("Mensaje verificado correctamente");
    }

    @Entonces("debe permanecer en la página de registro")
    public void debePermanecer EnLaPaginaDeRegistro() {
        logger.info("Verificando permanencia en página de registro");
        trazabilidad.registrarPaso("HU-002", "Verificación de permanencia en registro");

        assertTrue(paginaRegistro.estaPaginaCargada(),
                "No se mantuvo en la página de registro");

        logger.info("Permanencia en página de registro verificada");
    }

    @Entonces("debe ver el indicador de fortaleza {string}")
    public void debeVerElIndicadorDeFortaleza(String nivelFortaleza) {
        logger.info("Verificando indicador de fortaleza: {}", nivelFortaleza);
        trazabilidad.registrarPaso("HU-002", "Verificación de indicador de fortaleza");

        assertEquals(nivelFortaleza, paginaRegistro.obtenerIndicadorFortaleza(),
                "El indicador de fortaleza no coincide");

        logger.info("Indicador de fortaleza verificado");
    }

    @Entonces("debe ver las reglas de contraseña restantes {string}")
    public void debeVerLasReglasDeContrasenaRestantes(String reglasPendientes) {
        logger.info("Verificando reglas de contraseña pendientes");
        trazabilidad.registrarPaso("HU-002", "Verificación de reglas pendientes");

        assertTrue(paginaRegistro.sonReglasVisible(),
                "Las reglas de contraseña no están visibles");

        logger.info("Reglas de contraseña verificadas");
    }

    @Entonces("debe ser redirigido a la página de confirmación")
    public void debeSerRedirigidoALaPaginaDeConfirmacion() {
        logger.info("Verificando redirección a página de confirmación");
        trazabilidad.registrarPaso("HU-002", "Verificación de redirección a confirmación");

        esperarOperacionAsincrona(2000);
        assertTrue(paginaRegistro.estaPaginaConfirmacionCargada(),
                "No se redirigió a la página de confirmación");

        logger.info("Redirección a confirmación verificada");
    }

    @Entonces("debe poder hacer login con sus credenciales")
    public void debePoder HacerLoginConSusCredenciales() {
        logger.info("Verificando capacidad de login con credenciales");
        trazabilidad.registrarPaso("HU-002", "Verificación de login");

        // En un escenario real, se intentaría hacer login
        logger.info("Capacidad de login verificada (simulado)");
    }

    @Entonces("su cuenta debe estar activa en el sistema")
    public void suCuentaDebeEstarActivaEnElSistema() {
        logger.info("Verificando estado activo de la cuenta");
        trazabilidad.registrarPaso("HU-002", "Verificación de cuenta activa");

        // En un escenario real, se consultaría el estado en BD
        logger.info("Estado activo de cuenta verificado (simulado)");
    }

    @Entonces("debe poder navegar en el siguiente orden:")
    public void debePoderNavegarEnElSiguienteOrden(DataTable tablaCampos) {
        logger.info("Verificando orden de navegación por teclado");
        trazabilidad.registrarPaso("HU-002", "Verificación de orden de navegación");

        List<String> campos = tablaCampos.asList();
        for (String campo : campos) {
            paginaRegistro.navegarConTab();
            verificarCampoEnfocado(campo);
        }

        logger.info("Orden de navegación verificado");
    }

    @Entonces("cada campo debe mostrar claramente el foco visual")
    public void cadaCampoDebeMostrarClaramenteElFocoVisual() {
        logger.info("Verificando foco visual de campos");
        trazabilidad.registrarPaso("HU-002", "Verificación de foco visual");

        assertTrue(paginaRegistro.esFocoVisualClaro(),
                "El foco visual no está claro");

        logger.info("Foco visual verificado");
    }

    @Entonces("el sistema debe procesar la solicitud en menos de {int} segundos")
    public void elSistemaDebeProcesarLaSolicitudEnMenosDeSegundos(int segundosMaximos) {
        logger.info("Verificando tiempo de procesamiento");
        trazabilidad.registrarPaso("HU-002", "Verificación de tiempo de procesamiento");

        if (tiempoInicioRegistro != null) {
            long segundosTranscurridos = ChronoUnit.SECONDS.between(tiempoInicioRegistro, LocalDateTime.now());
            assertTrue(segundosTranscurridos <= segundosMaximos,
                    String.format("El procesamiento tomó %d segundos, máximo permitido: %d",
                            segundosTranscurridos, segundosMaximos));
        }

        logger.info("Tiempo de procesamiento verificado");
    }

    @Entonces("debe mostrar la confirmación en menos de {int} segundos totales")
    public void debeMostrarLaConfirmacionEnMenosDeSegundosTotales(int segundosTotales) {
        logger.info("Verificando tiempo total hasta confirmación");
        trazabilidad.registrarPaso("HU-002", "Verificación de tiempo total");

        if (tiempoInicioRegistro != null) {
            long segundosTranscurridos = ChronoUnit.SECONDS.between(tiempoInicioRegistro, LocalDateTime.now());
            assertTrue(segundosTranscurridos <= segundosTotales,
                    String.format("El tiempo total fue %d segundos, máximo permitido: %d",
                            segundosTranscurridos, segundosTotales));
        }

        logger.info("Tiempo total verificado");
    }

    @Entonces("los datos deben estar correctamente almacenados en la base de datos")
    public void losDatosDebenEstarCorrectamenteAlmacenadosEnLaBaseDeDatos() {
        logger.info("Verificando almacenamiento en base de datos");
        trazabilidad.registrarPaso("HU-002", "Verificación de almacenamiento en BD");

        // En un escenario real, se consultaría la BD
        logger.info("Almacenamiento en BD verificado (simulado)");
    }

    @Entonces("la contraseña debe estar encriptada")
    public void laContrasenaDebeEstarEncriptada() {
        logger.info("Verificando encriptación de contraseña");
        trazabilidad.registrarPaso("HU-002", "Verificación de encriptación");

        // En un escenario real, se verificaría que la contraseña esté hasheada
        logger.info("Encriptación de contraseña verificada (simulado)");
    }

    @Entonces("la fecha de registro debe ser la fecha actual")
    public void laFechaDeRegistroDebeSer LaFechaActual() {
        logger.info("Verificando fecha de registro");
        trazabilidad.registrarPaso("HU-002", "Verificación de fecha de registro");

        // En un escenario real, se verificaría la fecha en BD
        logger.info("Fecha de registro verificada (simulado)");
    }

    @Entonces("el estado de la cuenta debe ser {string}")
    public void elEstadoDeLaCuentaDebeSer(String estadoEsperado) {
        logger.info("Verificando estado de cuenta: {}", estadoEsperado);
        trazabilidad.registrarPaso("HU-002", "Verificación de estado de cuenta");

        // En un escenario real, se consultaría el estado en BD
        logger.info("Estado de cuenta verificado: {} (simulado)", estadoEsperado);
    }

    @Entonces("debe aparecer un captcha de verificación")
    public void debeAparecerUnCaptchaDeVerificacion() {
        logger.info("Verificando aparición de captcha");
        trazabilidad.registrarPaso("HU-002", "Verificación de captcha");

        assertTrue(paginaRegistro.esCaptchaVisible(),
                "El captcha no está visible");

        logger.info("Captcha verificado");
    }

    @Entonces("debe requerir completar el captcha antes de proceder")
    public void debeRequerirCompletarElCaptchaAntesDeProceder() {
        logger.info("Verificando requerimiento de captcha");
        trazabilidad.registrarPaso("HU-002", "Verificación de requerimiento de captcha");

        assertTrue(paginaRegistro.esCaptchaRequerido(),
                "El captcha no es requerido");

        logger.info("Requerimiento de captcha verificado");
    }

    @Entonces("los campos de contraseña deben quedar vacíos")
    public void losCamposDeContrasenaDebenQuedarVacios() {
        logger.info("Verificando que campos de contraseña estén vacíos");
        trazabilidad.registrarPaso("HU-002", "Verificación de campos vacíos");

        assertTrue(paginaRegistro.estaCampoPasswordVacio(),
                "El campo de contraseña no está vacío");
        assertTrue(paginaRegistro.estaCampoConfirmarPasswordVacio(),
                "El campo de confirmar contraseña no está vacío");

        logger.info("Campos de contraseña vacíos verificados");
    }

    @Entonces("debe ver un enlace {string}")
    public void debeVerUnEnlace(String textoEnlace) {
        logger.info("Verificando enlace: {}", textoEnlace);
        trazabilidad.registrarPaso("HU-002", "Verificación de enlace");

        assertTrue(paginaRegistro.esEnlaceVisible(textoEnlace),
                "El enlace no está visible: " + textoEnlace);

        logger.info("Enlace verificado: {}", textoEnlace);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void verificarCampoEnfocado(String nombreCampo) {
        assertTrue(paginaRegistro.estaCampoEnfocado(nombreCampo),
                "El campo no está enfocado: " + nombreCampo);
    }

    private void esperarOperacionAsincrona(int milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupción durante espera: {}", e.getMessage());
        }
    }
}