package com.qa.automatizacion.pasos;


import com.qa.automatizacion.utilidades.ContextoPrueba;
import com.qa.automatizacion.modelo.Usuario;
import com.qa.automatizacion.paginas.PaginaLogin;
import com.qa.automatizacion.paginas.PaginaDashboard;
import com.qa.automatizacion.utilidades.GeneradorDatos;
import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import io.cucumber.java.es.*;
import io.cucumber.datatable.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

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
    private PaginaDashboard paginaDashboard;  // Cambiar nombre
    private final GeneradorDatos generadorDatos;
    private final PropiedadesAplicacion propiedades;

    public PasosComunes() {
        this.contexto = ContextoPrueba.obtenerInstancia();
        this.generadorDatos = new GeneradorDatos();
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();
        logger.info("PasosComunes inicializado");
    }

    // ✅ 3. CORREGIR los métodos para usar las clases correctas:

    @Dado("que estoy en la página principal de la aplicación")
    public void queEstoyEnLaPaginaPrincipalDeLaAplicacion() {
        String urlBase = propiedades.obtenerUrlBase();
        ConfiguradorNavegador.navegarA(urlBase);

        paginaDashboard = new PaginaDashboard();
        assertTrue(paginaDashboard.estaPaginaCargada(), "La página principal no se cargó correctamente");

        logger.info("Usuario navegó a la página principal: {}", urlBase);
    }

    @Dado("que estoy en la página de login")
    public void queEstoyEnLaPaginaDeLogin() {
        String urlLogin = propiedades.obtenerUrlLogin();
        ConfiguradorNavegador.navegarA(urlLogin);

        paginaLogin = new PaginaLogin();
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
                usuarioDefecto.getEmail(),  // Usar email en lugar de nombreUsuario
                usuarioDefecto.getContrasena()
        );

        assertTrue(loginExitoso, "No se pudo autenticar al usuario");
        contexto.almacenarDato("usuarioAutenticado", usuarioDefecto);

        logger.info("Usuario autenticado exitosamente: {}", usuarioDefecto.getEmail());
    }

    @Dado("que soy un usuario autenticado con los siguientes datos:")
    public void queSoyUnUsuarioAutenticadoConLosSiguientesDatos(DataTable datosUsuario) {
        Map<String, String> datos = datosUsuario.asMap();

        if (paginaLogin == null) {
            queEstoyEnLaPaginaDeLogin();
        }

        boolean loginExitoso = paginaLogin.iniciarSesion(
                datos.get("email"),  // Cambiar de "usuario" a "email"
                datos.get("contrasena")
        );

        assertTrue(loginExitoso, "No se pudo autenticar con las credenciales proporcionadas");

        Usuario usuario = Usuario.builder()
                .email(datos.get("email"))
                .nombre(datos.get("nombre"))
                .apellido(datos.get("apellido"))
                .build();

        contexto.almacenarDato("usuarioAutenticado", usuario);
        logger.info("Usuario autenticado con datos personalizados: {}", datos.get("email"));
    }

    @Dado("que tengo datos de prueba válidos")
    public void queTengoDatosDePruebaValidos() {
        // Generar y almacenar datos de prueba válidos
        Usuario usuarioValido = generadorDatos.generarUsuarioValido();
        contexto.almacenarDato("usuarioValido", usuarioValido);

        logger.info("Datos de prueba válidos generados para usuario: {}", usuarioValido.getEmail());
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
        if (paginaDashboard == null) {
            paginaDashboard = new PaginaDashboard();
        }

        // Navegar usando métodos del dashboard
        switch (seccion.toLowerCase()) {
            case "productos":
                paginaDashboard.navegarAProductos();
                break;
            case "usuarios":
                paginaDashboard.navegarAUsuarios();
                break;
            case "reportes":
                paginaDashboard.navegarAReportes();
                break;
            default:
                logger.warn("Sección desconocida: {}", seccion);
                return;
        }

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
    public void tomoUnaCapturaeDePantallaConNombre(String nombreArchivo) {
        try {
            byte[] screenshot = ConfiguradorNavegador.tomarCapturaPantalla();
            if (screenshot != null) {
                contexto.almacenarDato("ultimaCaptura", screenshot);
                contexto.almacenarDato("nombreCaptura", nombreArchivo);
                logger.info("Captura de pantalla tomada: {}", nombreArchivo);
            } else {
                logger.warn("No se pudo tomar la captura de pantalla");
            }
        } catch (Exception e) {
            logger.error("Error tomando captura de pantalla: {}", e.getMessage());
        }
    }

    // Then - Verificaciones comunes

    @Entonces("la página debería cargar correctamente")
    public void laPaginaDeberiaCargarCorrectamente() {
        // Verificar que el título no esté vacío
        String titulo = ConfiguradorNavegador.obtenerTituloPagina();
        assertNotNull(titulo, "El título de la página no debería ser nulo");
        assertFalse(titulo.trim().isEmpty(), "El título de la página no debería estar vacío");

        // Verificar que la URL actual sea válida
        String urlActual = ConfiguradorNavegador.obtenerUrlActual();
        assertTrue(validarUrlAccesible(urlActual), "La URL actual no es accesible");

        logger.info("Página cargada correctamente - Título: {}, URL: {}", titulo, urlActual);
    }

    @Entonces("debería estar en la sección {string}")
    public void deberiaEstarEnLaSeccion(String seccionEsperada) {
        String seccionActual = (String) contexto.obtenerDato("seccionActual");
        assertEquals(seccionEsperada, seccionActual,
                "No se encuentra en la sección esperada");

        // Verificar también en la URL o título si es posible
        String urlActual = ConfiguradorNavegador.obtenerUrlActual().toLowerCase();
        assertTrue(urlActual.contains(seccionEsperada.toLowerCase()) ||
                        seccionActual.equalsIgnoreCase(seccionEsperada),
                "La sección actual no coincide con la esperada");

        logger.info("Verificación exitosa: usuario en sección {}", seccionEsperada);
    }

    @Entonces("no debería ver el elemento {string}")
    public void noDeberiaVerElElemento(String selector) {
        boolean presente = validarElementoAusente(selector, "Elemento que debería estar ausente");
        assertTrue(presente, "El elemento está presente cuando no debería estarlo: " + selector);

        logger.info("Verificación exitosa: elemento ausente {}", selector);
    }

    @Entonces("el elemento {string} debería estar visible")
    public void elElementoDeberiaEstarVisible(String selector) {
        boolean visible = validarElementoPresente(selector, "Elemento requerido");
        assertTrue(visible, "El elemento no está visible: " + selector);

        logger.info("Verificación exitosa: elemento visible {}", selector);
    }

    // Métodos auxiliares

    /**
     * Valida que un elemento esté presente en la página
     */
    public boolean validarElementoPresente(String selector, String descripcion) {
        try {
            WebElement elemento = contexto.getNavegador().findElement(By.cssSelector(selector));
            boolean visible = elemento.isDisplayed();
            logger.debug("Elemento {} visible: {}", descripcion, visible);
            return visible;
        } catch (NoSuchElementException e) {
            logger.debug("Elemento {} no encontrado: {}", descripcion, selector);
            return false;
        }
    }

    /**
     * Valida que un elemento NO esté presente en la página
     */
    public boolean validarElementoAusente(String selector, String descripcion) {
        try {
            contexto.getNavegador().findElement(By.cssSelector(selector));
            logger.warn("Elemento presente cuando no debería: {}", descripcion);
            return false;
        } catch (NoSuchElementException e) {
            logger.info("Elemento correctamente ausente: {}", descripcion);
            return true;
        }
    }

    /**
     * Verifica si una URL es válida y accesible
     */
    public boolean validarUrlAccesible(String url) {
        try {
            // Verificar que la URL no contenga errores comunes
            String pageSource = contexto.getNavegador().getPageSource().toLowerCase();
            boolean hayError = pageSource.contains("404") ||
                    pageSource.contains("500") ||
                    pageSource.contains("error") ||
                    pageSource.contains("not found");

            if (hayError) {
                logger.warn("URL contiene indicadores de error: {}", url);
                return false;
            }

            logger.debug("URL accesible: {}", url);
            return true;

        } catch (Exception e) {
            logger.error("Error al validar URL: {} - {}", url, e.getMessage());
            return false;
        }
    }
}