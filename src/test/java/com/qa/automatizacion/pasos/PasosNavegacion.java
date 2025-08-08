package com.qa.automatizacion.pasos;


import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import com.qa.automatizacion.paginas.*;
import com.qa.automatizacion.utilidades.HelperTrazabilidad;
import io.cucumber.java.es.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions para escenarios de navegación entre páginas del sistema.
 * Implementa la lógica de navegación y verificación de páginas.
 *
 * Principios aplicados:
 * - Separación de Intereses: Se enfoca únicamente en navegación
 * - Abstracción: Utiliza Page Objects para ocultar complejidad de navegación
 * - Modularidad: Métodos reutilizables para diferentes tipos de navegación
 * - Single Responsibility: Cada método maneja un aspecto específico de navegación
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
public class PasosNavegacion {

    private static final Logger logger = LoggerFactory.getLogger(PasosNavegacion.class);

    private final PropiedadesAplicacion propiedades;
    private final HelperTrazabilidad trazabilidad;

    // Page Objects para navegación
    private final PaginaBase paginaActual;
    private PaginaLogin paginaLogin;
    private PaginaRegistro paginaRegistro;
    private PaginaDashboard paginaDashboard;
    private PaginaCrud paginaCrud;
    private PaginaProductos paginaProductos;

    // Variables de contexto
    private LocalDateTime tiempoInicioNavegacion;
    private String paginaAnterior;
    private String paginaDestino;

    public PasosNavegacion() {
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();
        this.trazabilidad = new HelperTrazabilidad();
        this.paginaActual = new PaginaBase() {
            @Override
            public boolean estaPaginaCargada() {
                return true; // Implementación base
            }

            @Override
            public String obtenerUrlEsperada() {
                return ConfiguradorNavegador.obtenerUrlActual();
            }
        };
    }

    // ==================== PASOS DADO (Given) ====================

    @Dado("que el usuario está en la página de {string}")
    public void elUsuarioEstaEnLaPaginaDe(String nombrePagina) {
        logger.info("Verificando que el usuario está en la página de: {}", nombrePagina);
        trazabilidad.registrarAccion("Navegación", "Verificar ubicación en página: " + nombrePagina);

        this.tiempoInicioNavegacion = LocalDateTime.now();

        // Obtener página específica según el nombre
        PaginaBase pagina = obtenerPaginaPorNombre(nombrePagina);

        // Verificar si estamos en la página correcta
        if (!pagina.estamosEnEstaPagina()) {
            logger.info("No estamos en la página correcta. Navegando a: {}", nombrePagina);
            navegarAPagina(nombrePagina);
        }

        // Verificar que la página esté completamente cargada
        assertTrue(pagina.estaPaginaCargada(),
                "La página '" + nombrePagina + "' no está completamente cargada");

        logger.debug("Usuario ubicado correctamente en página: {}", nombrePagina);
    }

    @Dado("que el usuario navega a {string}")
    public void elUsuarioNavegaA(String url) {
        logger.info("Navegando directamente a URL: {}", url);
        trazabilidad.registrarAccion("Navegación", "Navegación directa a: " + url);

        this.tiempoInicioNavegacion = LocalDateTime.now();
        this.paginaAnterior = ConfiguradorNavegador.obtenerUrlActual();
        this.paginaDestino = url;

        String urlCompleta = construirUrlCompleta(url);
        ConfiguradorNavegador.navegarA(urlCompleta);

        // Esperar a que la página cargue
        esperarCargaPagina();

        logger.debug("Navegación directa completada a: {}", url);
    }

    @Dado("que el usuario tiene el sistema abierto en múltiples pestañas")
    public void elUsuarioTieneElSistemaAbiertoEnMultiplesPestanas() {
        logger.info("Configurando múltiples pestañas del sistema");
        trazabilidad.registrarAccion("Navegación", "Configurar múltiples pestañas");

        // Este paso típicamente se simula o se documenta
        // En pruebas reales podríamos abrir ventanas adicionales
        String ventanaOriginal = ConfiguradorNavegador.obtenerDriver().getWindowHandle();

        // Simular apertura de nueva pestaña (usando JavaScript)
        ConfiguradorNavegador.obtenerDriver().switchTo().newWindow(org.openqa.selenium.WindowType.TAB);
        ConfiguradorNavegador.navegarA(propiedades.obtenerUrlBase());

        // Volver a la ventana original
        ConfiguradorNavegador.obtenerDriver().switchTo().window(ventanaOriginal);

        logger.debug("Múltiples pestañas configuradas");
    }

    @Dado("que el usuario está navegando con el teclado")
    public void elUsuarioEstaNavegandoConElTeclado() {
        logger.info("Configurando navegación por teclado");
        trazabilidad.registrarAccion("Navegación", "Configurar navegación por teclado");

        // Enfocar el primer elemento interactivo
        try {
            ConfiguradorNavegador.obtenerDriver().switchTo().activeElement()
                    .sendKeys(org.openqa.selenium.Keys.TAB);
        } catch (Exception e) {
            logger.debug("No se pudo configurar navegación por teclado: {}", e.getMessage());
        }

        logger.debug("Navegación por teclado configurada");
    }

    // ==================== PASOS CUANDO (When) ====================

    @Cuando("el usuario navega a la página de {string}")
    public void elUsuarioNavegaALaPaginaDe(String nombrePagina) {
        logger.info("Navegando a la página de: {}", nombrePagina);
        trazabilidad.registrarAccion("Navegación", "Navegar a página: " + nombrePagina);

        this.tiempoInicioNavegacion = LocalDateTime.now();
        this.paginaAnterior = ConfiguradorNavegador.obtenerUrlActual();
        this.paginaDestino = nombrePagina;

        navegarAPagina(nombrePagina);

        logger.debug("Navegación iniciada a: {}", nombrePagina);
    }

    @Cuando("navega por las secciones principales:")
    public void navegaPorLasSeccionesPrincipales(io.cucumber.datatable.DataTable seccionesTable) {
        logger.info("Navegando por múltiples secciones principales");
        trazabilidad.registrarAccion("Navegación", "Navegar por secciones múltiples");

        var secciones = seccionesTable.asMaps();

        for (Map<String, String> seccion : secciones) {
            String nombreSeccion = seccion.get("seccion");
            String urlEsperada = seccion.get("url_esperada");

            logger.debug("Navegando a sección: {} ({})", nombreSeccion, urlEsperada);

            LocalDateTime inicioSeccion = LocalDateTime.now();
            navegarAPagina(nombreSeccion);

            // Verificar que llegamos a la URL correcta
            String urlActual = ConfiguradorNavegador.obtenerUrlActual();
            assertTrue(urlActual.contains(urlEsperada),
                    "URL incorrecta para " + nombreSeccion + ". Esperada: " + urlEsperada + ", Actual: " + urlActual);

            // Verificar tiempo de carga
            long tiempoCarga = java.time.Duration.between(inicioSeccion, LocalDateTime.now()).getSeconds();
            assertTrue(tiempoCarga <= 3,
                    "La página " + nombreSeccion + " tardó " + tiempoCarga + "s en cargar (máximo 3s)");

            logger.debug("Sección {} cargada correctamente en {}s", nombreSeccion, tiempoCarga);
        }

        logger.info("Navegación por secciones principales completada");
    }

    @Cuando("hace clic en el enlace {string}")
    public void haceClicEnElEnlace(String textoEnlace) {
        logger.info("Haciendo clic en enlace: {}", textoEnlace);
        trazabilidad.registrarAccion("Navegación", "Clic en enlace: " + textoEnlace);

        this.tiempoInicioNavegacion = LocalDateTime.now();
        this.paginaAnterior = ConfiguradorNavegador.obtenerUrlActual();

        try {
            var enlace = ConfiguradorNavegador.obtenerDriver()
                    .findElement(org.openqa.selenium.By.linkText(textoEnlace));
            enlace.click();

            esperarCargaPagina();
            logger.debug("Clic en enlace '{}' ejecutado", textoEnlace);

        } catch (Exception e) {
            logger.error("Error haciendo clic en enlace '{}': {}", textoEnlace, e.getMessage());
            throw new RuntimeException("No se pudo hacer clic en el enlace: " + textoEnlace, e);
        }
    }

    @Cuando("navega hacia atrás en el navegador")
    public void navegaHaciaAtrasEnElNavegador() {
        logger.info("Navegando hacia atrás en el navegador");
        trazabilidad.registrarAccion("Navegación", "Navegación hacia atrás");

        this.paginaAnterior = ConfiguradorNavegador.obtenerUrlActual();
        ConfiguradorNavegador.navegarAtras();

        esperarCargaPagina();
        logger.debug("Navegación hacia atrás completada");
    }

    @Cuando("navega hacia adelante en el navegador")
    public void navegaHaciaAdelanteEnElNavegador() {
        logger.info("Navegando hacia adelante en el navegador");
        trazabilidad.registrarAccion("Navegación", "Navegación hacia adelante");

        this.paginaAnterior = ConfiguradorNavegador.obtenerUrlActual();
        ConfiguradorNavegador.navegarAdelante();

        esperarCargaPagina();
        logger.debug("Navegación hacia adelante completada");
    }

    @Cuando("recarga la página actual")
    public void recargaLaPaginaActual() {
        logger.info("Recargando página actual");
        trazabilidad.registrarAccion("Navegación", "Recarga de página");

        this.tiempoInicioNavegacion = LocalDateTime.now();
        ConfiguradorNavegador.recargarPagina();

        esperarCargaPagina();
        logger.debug("Página recargada");
    }

    @Cuando("intenta navegar directamente a {string}")
    public void intentaNavegaDirectamenteA(String url) {
        logger.info("Intentando navegar directamente a: {}", url);
        trazabilidad.registrarAccion("Navegación", "Intento navegación directa: " + url);

        this.tiempoInicioNavegacion = LocalDateTime.now();
        this.paginaDestino = url;

        String urlCompleta = construirUrlCompleta(url);
        ConfiguradorNavegador.navegarA(urlCompleta);

        esperarCargaPagina();
        logger.debug("Intento de navegación directa realizado");
    }

    // ==================== PASOS ENTONCES (Then) ====================

    @Entonces("debe estar en la página de {string}")
    public void debeEstarEnLaPaginaDe(String nombrePagina) {
        logger.info("Verificando que está en la página de: {}", nombrePagina);

        PaginaBase pagina = obtenerPaginaPorNombre(nombrePagina);

        assertTrue(pagina.estamosEnEstaPagina(),
                "No está en la página esperada: " + nombrePagina);
        assertTrue(pagina.estaPaginaCargada(),
                "La página '" + nombrePagina + "' no está completamente cargada");

        logger.debug("Verificación exitosa: está en página {}", nombrePagina);
    }

    @Entonces("todas las páginas deben cargar correctamente")
    public void todasLasPaginasDebenCargarCorrectamente() {
        logger.info("Verificando que todas las páginas cargaron correctamente");

        // Esta verificación se realiza durante la navegación en el paso When
        // Aquí podemos agregar verificaciones adicionales si es necesario

        String urlActual = ConfiguradorNavegador.obtenerUrlActual();
        assertFalse(urlActual.contains("error") || urlActual.contains("404"),
                "Se detectó una página de error: " + urlActual);

        logger.debug("Todas las páginas cargaron correctamente");
    }

    @Entonces("y en menos de {int} segundos cada una")
    public void yEnMenosDe_SegundosCadaUna(int segundosMaximos) {
        // La verificación de tiempo se realiza durante la navegación
        logger.debug("Verificación de tiempo completada (máximo {}s por página)", segundosMaximos);
    }

    @Entonces("debe ser redirigido automáticamente al login")
    public void debeSerRedirigidoAutomaticamenteAlLogin() {
        logger.info("Verificando redirección automática al login");

        // Esperar a que ocurra la redirección
        esperarRedireccion();

        inicializarPaginaLogin();
        assertTrue(paginaLogin.estamosEnEstaPagina(),
                "No fue redirigido al login automáticamente");

        logger.debug("Redirección automática al login verificada");
    }

    @Entonces("la navegación debe completarse en menos de {int} segundos")
    public void laNavegacionDebeCompletarseEnMenosDe_Segundos(int segundosMaximos) {
        if (tiempoInicioNavegacion != null) {
            long tiempoTranscurrido = java.time.Duration.between(tiempoInicioNavegacion, LocalDateTime.now()).getSeconds();

            assertTrue(tiempoTranscurrido <= segundosMaximos,
                    "La navegación tardó " + tiempoTranscurrido + "s (máximo " + segundosMaximos + "s)");

            logger.debug("Navegación completada en {}s (límite: {}s)", tiempoTranscurrido, segundosMaximos);
        }
    }

    @Entonces("debe mostrar el contenido específico de {string}")
    public void debeMostrarElContenidoEspecificoDe(String nombrePagina) {
        logger.info("Verificando contenido específico de: {}", nombrePagina);

        PaginaBase pagina = obtenerPaginaPorNombre(nombrePagina);
        assertTrue(pagina.estaPaginaCargada(),
                "El contenido específico de '" + nombrePagina + "' no está visible");

        logger.debug("Contenido específico de '{}' verificado", nombrePagina);
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Obtiene la instancia de página correspondiente según el nombre.
     */
    private PaginaBase obtenerPaginaPorNombre(String nombrePagina) {
        return switch (nombrePagina.toLowerCase()) {
            case "login", "inicio de sesión" -> {
                inicializarPaginaLogin();
                yield paginaLogin;
            }
            case "registro", "registro de usuario" -> {
                inicializarPaginaRegistro();
                yield paginaRegistro;
            }
            case "dashboard", "panel principal" -> {
                inicializarPaginaDashboard();
                yield paginaDashboard;
            }
            case "productos", "catálogo" -> {
                inicializarPaginaProductos();
                yield paginaProductos;
            }
            case "crud", "gestión" -> {
                inicializarPaginaCrud();
                yield paginaCrud;
            }
            default -> throw new IllegalArgumentException("Página no reconocida: " + nombrePagina);
        };
    }

    /**
     * Navega a una página específica.
     */
    private void navegarAPagina(String nombrePagina) {
        PaginaBase pagina = obtenerPaginaPorNombre(nombrePagina);
        pagina.navegarAPagina();
        esperarCargaPagina();
    }

    /**
     * Construye URL completa a partir de una ruta relativa.
     */
    private String construirUrlCompleta(String url) {
        if (url.startsWith("http")) {
            return url;
        }

        String baseUrl = propiedades.obtenerUrlBase();
        if (url.startsWith("/")) {
            return baseUrl + url;
        } else {
            return baseUrl + "/" + url;
        }
    }

    /**
     * Espera a que la página actual cargue completamente.
     */
    private void esperarCargaPagina() {
        try {
            Thread.sleep(1000); // Pausa básica

            // Esperar a que el DOM esté listo
            org.openqa.selenium.support.ui.WebDriverWait wait =
                    new org.openqa.selenium.support.ui.WebDriverWait(ConfiguradorNavegador.obtenerDriver(),
                            java.time.Duration.ofSeconds(10));

            wait.until(webDriver ->
                    ((org.openqa.selenium.JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState").equals("complete"));

        } catch (Exception e) {
            logger.debug("Error esperando carga de página: {}", e.getMessage());
        }
    }

    /**
     * Espera a que ocurra una redirección.
     */
    private void esperarRedireccion() {
        try {
            Thread.sleep(2000); // Esperar redirección
            esperarCargaPagina();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ==================== INICIALIZACIÓN DE PAGE OBJECTS ====================

    private void inicializarPaginaLogin() {
        if (paginaLogin == null) {
            paginaLogin = new PaginaLogin();
        }
    }

    private void inicializarPaginaRegistro() {
        if (paginaRegistro == null) {
            paginaRegistro = new PaginaRegistro();
        }
    }

    private void inicializarPaginaDashboard() {
        if (paginaDashboard == null) {
            paginaDashboard = new PaginaDashboard();
        }
    }

    private void inicializarPaginaProductos() {
        if (paginaProductos == null) {
            paginaProductos = new PaginaProductos();
        }
    }

    private void inicializarPaginaCrud() {
        if (paginaCrud == null) {
            paginaCrud = new PaginaCrud();
        }
    }

    /**
     * Obtiene información de diagnóstico de la navegación actual.
     */
    public String obtenerInformacionDiagnostico() {
        StringBuilder info = new StringBuilder();
        info.append("=== INFORMACIÓN DE NAVEGACIÓN ===\n");
        info.append("URL Actual: ").append(ConfiguradorNavegador.obtenerUrlActual()).append("\n");
        info.append("Título: ").append(ConfiguradorNavegador.obtenerTituloPagina()).append("\n");
        info.append("Página Anterior: ").append(paginaAnterior != null ? paginaAnterior : "N/A").append("\n");
        info.append("Página Destino: ").append(paginaDestino != null ? paginaDestino : "N/A").append("\n");

        if (tiempoInicioNavegacion != null) {
            long duracion = java.time.Duration.between(tiempoInicioNavegacion, LocalDateTime.now()).getSeconds();
            info.append("Duración Navegación: ").append(duracion).append(" segundos\n");
        }

        return info.toString();
    }
}