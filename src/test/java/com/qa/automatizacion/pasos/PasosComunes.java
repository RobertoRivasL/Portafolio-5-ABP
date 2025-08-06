package com.automatizacion.pruebas.pasos;

import com.automatizacion.pruebas.contexto.ContextoPrueba;
import com.automatizacion.pruebas.modelos.Usuario;
import com.automatizacion.pruebas.paginas.PaginaLogin;
import com.automatizacion.pruebas.paginas.PaginaPrincipal;
import com.automatizacion.pruebas.utilidades.GeneradorDatos;
import com.automatizacion.pruebas.utilidades.CapturaPantalla;
import io.cucumber.java.es.*;
import io.cucumber.datatable.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions comunes reutilizables entre diferentes features
 * Implementa pasos genéricos que pueden ser utilizados en múltiples escenarios
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
public class PasosComunes {

    private static final Logger logger = LoggerFactory.getLogger(PasosComunes.class);

    private final ContextoPrueba contexto;
    private PaginaLogin paginaLogin;
    private PaginaPrincipal paginaPrincipal;
    private final GeneradorDatos generadorDatos;
    private final CapturaPantalla capturaPantalla;

    public PasosComunes(ContextoPrueba contexto) {
        this.contexto = contexto;
        this.generadorDatos = new GeneradorDatos();
        this.capturaPantalla = new CapturaPantalla(contexto.getNavegador());
        logger.info("PasosComunes inicializado");
    }

    // Given - Condiciones iniciales comunes

    @Dado("que estoy en la página principal de la aplicación")
    public void queEstoyEnLaPaginaPrincipalDeLaAplicacion() {
        String urlBase = contexto.getConfiguracion().getUrlBase();
        contexto.getNavegador().get(urlBase);

        paginaPrincipal = new PaginaPrincipal(contexto.getNavegador());
        assertTrue(paginaPrincipal.estaPaginaCargada(), "La página principal no se cargó correctamente");

        logger.info("Usuario navegó a la página principal: {}", urlBase);
    }

    @Dado("que estoy en la página de login")
    public void queEstoyEnLaPaginaDeLogin() {
        String urlLogin = contexto.getConfiguracion().getUrlBase() + "/login";
        contexto.getNavegador().get(urlLogin);

        paginaLogin = new PaginaLogin(contexto.getNavegador());
        assertTrue(paginaLogin.estaPaginaCargada(), "La página de login no se cargó correctamente");

        logger.info("Usuario navegó a la página de login: {}", urlLogin);
    }

    @Dado("que soy un usuario autenticado")
    public void queSoyUnUsuarioAutenticado() {
        // Navegar al login si no estamos allí
        if (paginaLogin == null) {
            queEstoyEnLaPaginaDeLogin();
        }

        // Usar credenciales de usuario por defecto
        Usuario usuarioDefecto = generadorDatos.generarUsuarioValido();
        boolean loginExitoso = paginaLogin.iniciarSesion(
                usuarioDefecto.getNombreUsuario(),
                usuarioDefecto.getContrasena()
        );

        assertTrue(loginExitoso, "No se pudo autenticar al usuario");
        contexto.almacenarDato("usuarioAutenticado", usuarioDefecto);

        logger.info("Usuario autenticado exitosamente: {}", usuarioDefecto.getNombreUsuario());
    }

    @Dado("que soy un usuario autenticado con los siguientes datos:")
    public void queSoyUnUsuarioAutenticadoConLosSiguientesDatos(DataTable datosUsuario) {
        Map<String, String> datos = datosUsuario.asMap();

        if (paginaLogin == null) {
            queEstoyEnLaPaginaDeLogin();
        }

        boolean loginExitoso = paginaLogin.iniciarSesion(
                datos.get("usuario"),
                datos.get("contrasena")
        );

        assertTrue(loginExitoso, "No se pudo autenticar con las credenciales proporcionadas");

        Usuario usuario = Usuario.builder()
                .nombreUsuario(datos.get("usuario"))
                .email(datos.get("email"))
                .nombre(datos.get("nombre"))
                .apellido(datos.get("apellido"))
                .build();

        contexto.almacenarDato("usuarioAutenticado", usuario);
        logger.info("Usuario autenticado con datos personalizados: {}", datos.get("usuario"));
    }

    @Dado("que tengo datos de prueba válidos")
    public void queTengoDatosDePruebaValidos() {
        // Generar y almacenar datos de prueba válidos
        Usuario usuarioValido = generadorDatos.generarUsuarioValido();
        contexto.almacenarDato("usuarioValido", usuarioValido);

        logger.info("Datos de prueba válidos generados para usuario: {}", usuarioValido.getNombreUsuario());
    }

    @Dado("que la aplicación está en estado inicial")
    public void queLaAplicacionEstaEnEstadoInicial() {
        // Limpiar cualquier estado previo
        contexto.limpiarDatos();

        // Navegar a la página principal
        queEstoyEnLaPaginaPrincipalDeLaAplicacion();

        logger.info("Aplicación reiniciada a estado inicial");
    }

    // When - Acciones comunes

    @Cuando("navego a la sección {string}")
    public void navegoALaSeccion(String seccion) {
        if (paginaPrincipal == null) {
            paginaPrincipal = new PaginaPrincipal(contexto.getNavegador());
        }

        boolean navegacionExitosa = paginaPrincipal.navegarASeccion(seccion);
        assertTrue(navegacionExitosa, "No se pudo navegar a la sección: " + seccion);

        contexto.almacenarDato("seccionActual", seccion);
        logger.info("Navegación exitosa a la sección: {}", seccion);
    }

    @Cuando("espero {int} segundos")
    public void esperoSegundos(int segundos) {
        try {
            Thread.sleep(segundos * 1000L);
            logger.info("Esperando {} segundos", segundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Espera interrumpida: {}", e.getMessage());
        }
    }

    @Cuando("tomo una captura de pantalla con nombre {string}")
    public void tomoUnaCapturaeDePantallaConNombre(String nombreCaptura) {
        String rutaCaptura = capturaPantalla.capturarPantalla(nombreCaptura);
        contexto.almacenarDato("ultimaCaptura", rutaCaptura);

        logger.info("Captura de pantalla tomada: {}", nombreCaptura);
    }

    @Cuando("refresco la página")
    public void refrescoLaPagina() {
        contexto.getNavegador().navigate().refresh();

        // Esperar a que la página se recargue
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.info("Página refrescada");
    }

    @Cuando("cierro sesión")
    public void cierroSesion() {
        if (paginaPrincipal == null) {
            paginaPrincipal = new PaginaPrincipal(contexto.getNavegador());
        }

        boolean cerrarSesionExitoso = paginaPrincipal.cerrarSesion();
        assertTrue(cerrarSesionExitoso, "No se pudo cerrar la sesión");

        // Limpiar datos de usuario autenticado
        contexto.removerDato("usuarioAutenticado");

        logger.info("Sesión cerrada exitosamente");
    }

    // Then - Verificaciones comunes

    @Entonces("debería estar en la página {string}")
    public void deberiaEstarEnLaPagina(String paginaEsperada) {
        String urlActual = contexto.getNavegador().getCurrentUrl();
        String urlBase = contexto.getConfiguracion().getUrlBase();

        assertTrue(urlActual.contains(paginaEsperada.toLowerCase()) ||
                        urlActual.equals(urlBase + "/" + paginaEsperada.toLowerCase()),
                "No estoy en la página esperada. URL actual: " + urlActual);

        logger.info("Verificación exitosa: usuario en página {}", paginaEsperada);
    }

    @Entonces("debería ver el mensaje {string}")
    public void deberiaVerElMensaje(String mensajeEsperado) {
        if (paginaPrincipal == null) {
            paginaPrincipal = new PaginaPrincipal(contexto.getNavegador());
        }

        assertTrue(paginaPrincipal.contieneMensaje(mensajeEsperado),
                "No se encontró el mensaje esperado: " + mensajeEsperado);

        logger.info("Mensaje verificado exitosamente: {}", mensajeEsperado);
    }

    @Entonces("no debería ver el mensaje {string}")
    public void noDeberiaVerElMensaje(String mensajeNoEsperado) {
        if (paginaPrincipal == null) {
            paginaPrincipal = new PaginaPrincipal(contexto.getNavegador());
        }

        assertFalse(paginaPrincipal.contieneMensaje(mensajeNoEsperado),
                "Se encontró un mensaje que no debería estar presente: " + mensajeNoEsperado);

        logger.info("Verificación exitosa: mensaje no presente: {}", mensajeNoEsperado);
    }

    @Entonces("la página debería cargar correctamente")
    public void laPaginaDeberiaCargarCorrectamente() {
        // Verificar que la página no esté en blanco
        String paginaSource = contexto.getNavegador().getPageSource();
        assertFalse(paginaSource.isEmpty(), "La página está vacía");

        // Verificar que no hay errores obvios
        String titulo = contexto.getNavegador().getTitle();
        assertFalse(titulo.toLowerCase().contains("error"),
                "El título de la página indica un error: " + titulo);

        logger.info("Página cargada correctamente. Título: {}", titulo);
    }

    @Entonces("debería estar autenticado en el sistema")
    public void deberiaEstarAutenticadoEnElSistema() {
        if (paginaPrincipal == null) {
            paginaPrincipal = new PaginaPrincipal(contexto.getNavegador());
        }

        assertTrue(paginaPrincipal.usuarioEstaAutenticado(),
                "El usuario no está autenticado en el sistema");

        logger.info("Verificación exitosa: usuario autenticado en el sistema");
    }

    @Entonces("no debería estar autenticado en el sistema")
    public void noDeberiaEstarAutenticadoEnElSistema() {
        if (paginaPrincipal == null) {
            paginaPrincipal = new PaginaPrincipal(contexto.getNavegador());
        }

        assertFalse(paginaPrincipal.usuarioEstaAutenticado(),
                "El usuario está autenticado cuando no debería estarlo");

        logger.info("Verificación exitosa: usuario no autenticado");
    }

    @Entonces("la URL actual debería contener {string}")
    public void laUrlActualDeberiaContener(String fragmentoUrl) {
        String urlActual = contexto.getNavegador().getCurrentUrl();
        assertTrue(urlActual.contains(fragmentoUrl),
                "La URL actual no contiene el fragmento esperado. " +
                        "URL actual: " + urlActual + ", Fragmento esperado: " + fragmentoUrl);

        logger.info("Verificación exitosa: URL contiene fragmento {}", fragmentoUrl);
    }

    @Entonces("el título de la página debería ser {string}")
    public void elTituloDeLaPaginaDeberiaSer(String tituloEsperado) {
        String tituloActual = contexto.getNavegador().getTitle();
        assertEquals(tituloEsperado, tituloActual,
                "El título de la página no coincide con el esperado");

        logger.info("Verificación exitosa: título de página correcto: {}", tituloEsperado);
    }

    @Entonces("debería poder ver la sección {string}")
    public void deberiaPoderveLaSeccion(String seccion) {
        if (paginaPrincipal == null) {
            paginaPrincipal = new PaginaPrincipal(contexto.getNavegador());
        }

        assertTrue(paginaPrincipal.seccionEsVisible(seccion),
                "La sección no es visible: " + seccion);

        logger.info("Verificación exitosa: sección visible: {}", seccion);
    }

    @Entonces("no debería poder ver la sección {string}")
    public void noDeberiaPoderveLaSeccion(String seccion) {
        if (paginaPrincipal == null) {
            paginaPrincipal = new PaginaPrincipal(contexto.getNavegador());
        }

        assertFalse(paginaPrincipal.seccionEsVisible(seccion),
                "La sección es visible cuando no debería: " + seccion);

        logger.info("Verificación exitosa: sección no visible: {}", seccion);
    }

    // Métodos auxiliares para validaciones comunes

    /**
     * Valida que un elemento esté presente en la página
     *
     * @param selector Selector CSS del elemento
     * @param descripcion Descripción del elemento para logging
     * @return true si el elemento está presente
     */
    public boolean validarElementoPresente(String selector, String descripcion) {
        try {
            contexto.getNavegador().findElement(org.openqa.selenium.By.cssSelector(selector));
            logger.info("Elemento presente: {}", descripcion);
            return true;
        } catch (org.openqa.selenium.NoSuchElementException e) {
            logger.warn("Elemento no encontrado: {}", descripcion);
            return false;
        }
    }

    /**
     * Valida que un elemento no esté presente en la página
     *
     * @param selector Selector CSS del elemento
     * @param descripcion Descripción del elemento para logging
     * @return true si el elemento no está presente
     */
    public boolean validarElementoNoPresente(String selector, String descripcion) {
        try {
            contexto.getNavegador().findElement(org.openqa.selenium.By.cssSelector(selector));
            logger.warn("Elemento presente cuando no debería: {}", descripcion);
            return false;
        } catch (org.openqa.selenium.NoSuchElementException e) {
            logger.info("Elemento correctamente ausente: {}", descripcion);
            return true;
        }
    }

    /**
     * Obtiene el texto de un elemento
     *
     * @param selector Selector CSS del elemento
     * @return Texto del elemento o string vacío si no se encuentra
     */
    public String obtenerTextoElemento(String selector) {
        try {
            return contexto.getNavegador()
                    .findElement(org.openqa.selenium.By.cssSelector(selector))
                    .getText();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            logger.warn("No se pudo obtener texto del elemento: {}", selector);
            return "";
        }
    }

    /**
     * Verifica si una URL es válida y accesible
     *
     * @param url URL a verificar
     * @return true si la URL es accesible
     */
    public boolean validarUrlAccesible(String url) {
        try {
            String urlOriginal = contexto.getNavegador().getCurrentUrl();
            contexto.getNavegador().get(url);

            // Verificar que no hay errores 404, 500, etc.
            String pageSource = contexto.getNavegador().getPageSource().toLowerCase();
            boolean hayError = pageSource.contains("404") ||
                    pageSource.contains("500") ||
                    pageSource.contains("error") ||
                    pageSource.contains("not found");

            // Volver a la URL original
            contexto.getNavegador().get(urlOriginal);

            if (hayError) {
                logger.warn("URL no accesible o con errores: {}", url);
                return false;
            }

            logger.info("URL accesible: {}", url);
            return true;

        } catch (Exception e) {
            logger.error("Error al validar URL: {} - {}", url, e.getMessage());
            return false;
        }
    }

    /**
     * Guarda el estado actual del contexto para debugging
     */
    public void guardarEstadoContexto() {
        logger.info("=== Estado actual del contexto ===");
        logger.info("URL actual: {}", contexto.getNavegador().getCurrentUrl());
        logger.info("Título página: {}", contexto.getNavegador().getTitle());

        // Mostrar datos almacenados en el contexto
        if (contexto.obtenerDato("usuarioAutenticado") != null) {
            Usuario usuario = (Usuario) contexto.obtenerDato("usuarioAutenticado");
            logger.info("Usuario autenticado: {}", usuario.getNombreUsuario());
        }

        if (contexto.obtenerDato("seccionActual") != null) {
            logger.info("Sección actual: {}", contexto.obtenerDato("seccionActual"));
        }

        logger.info("================================");
    }

    /**
     * Pasos de debugging para desarrollo y troubleshooting
     */
    @Cuando("imprimo el estado actual del sistema")
    public void imprimoElEstadoActualDelSistema() {
        guardarEstadoContexto();
    }

    @Cuando("capturo pantalla para debugging")
    public void capturoPantallaParaDebugging() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        tomoUnaCapturaeDePantallaConNombre("debug_" + timestamp);
    }

    @Entonces("el sistema debería estar funcionando correctamente")
    public void elSistemaDeberiaEstarFuncionandoCorrectamente() {
        // Verificaciones básicas del sistema
        laPaginaDeberiaCargarCorrectamente();

        // Verificar que los elementos básicos están presentes
        assertTrue(validarElementoPresente("body", "Cuerpo de la página"));

        // Verificar que no hay errores de JavaScript en la consola
        // (esto requeriría una implementación más avanzada con JavaScriptExecutor)

        logger.info("Verificación exitosa: sistema funcionando correctamente");
    }
}