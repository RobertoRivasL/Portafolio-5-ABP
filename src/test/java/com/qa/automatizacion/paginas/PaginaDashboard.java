package com.qa.automatizacion.paginas;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Página del Dashboard principal de la aplicación.
 * Implementa el patrón Page Object Model para la página principal del usuario autenticado.
 *
 * Principios aplicados:
 * - Page Object Model: Encapsula elementos y acciones de la página
 * - Single Responsibility: Se enfoca únicamente en el dashboard
 * - Abstracción: Oculta complejidades de interacción con Selenium
 *
 * @author Equipo QA Automatización
 * @version 1.0
 */
public class PaginaDashboard extends PaginaBase {

    private static final Logger logger = LoggerFactory.getLogger(PaginaDashboard.class);

    // Elementos de la barra superior
    @FindBy(css = ".navbar .username, .header .user-name, #username")
    private WebElement nombreUsuario;

    @FindBy(css = ".welcome-message, .greeting, #welcome-msg")
    private WebElement mensajeBienvenida;

    @FindBy(css = ".logout-btn, #logout, .btn-logout")
    private WebElement botonCerrarSesion;

    @FindBy(css = ".user-menu, .profile-menu")
    private WebElement menuUsuario;

    // Elementos de navegación principal
    @FindBy(css = ".main-nav, .sidebar, .navigation")
    private WebElement navegacionPrincipal;

    @FindBy(css = ".nav-item, .menu-item")
    private List<WebElement> itemsNavegacion;

    @FindBy(css = "[data-nav='productos'], .nav-productos, #nav-productos")
    private WebElement linkProductos;

    @FindBy(css = "[data-nav='usuarios'], .nav-usuarios, #nav-usuarios")
    private WebElement linkUsuarios;

    @FindBy(css = "[data-nav='reportes'], .nav-reportes, #nav-reportes")
    private WebElement linkReportes;

    @FindBy(css = "[data-nav='configuracion'], .nav-config, #nav-config")
    private WebElement linkConfiguracion;

    // Elementos del contenido principal
    @FindBy(css = ".dashboard-content, .main-content, #main-content")
    private WebElement contenidoPrincipal;

    @FindBy(css = ".dashboard-widgets, .widgets-container")
    private WebElement contenedorWidgets;

    @FindBy(css = ".widget, .dashboard-card")
    private List<WebElement> widgets;

    // Elementos de estadísticas
    @FindBy(css = ".stats-container, .statistics")
    private WebElement contenedorEstadisticas;

    @FindBy(css = ".stat-item, .metric")
    private List<WebElement> elementosEstadisticas;

    // Elementos de notificaciones
    @FindBy(css = ".notifications, .alerts")
    private WebElement contenedorNotificaciones;

    @FindBy(css = ".notification-badge, .alert-count")
    private WebElement contadorNotificaciones;

    // Elementos de búsqueda rápida
    @FindBy(css = ".search-box, #quick-search")
    private WebElement cajaBusquedaRapida;

    @FindBy(css = ".search-btn, .btn-search")
    private WebElement botonBuscar;

    /**
     * Constructor que inicializa la página del dashboard
     *
     * @param driver Instancia del WebDriver
     */
    public PaginaDashboard(WebDriver driver) {
        super(driver);
        logger.info("Página Dashboard inicializada");
    }

    @Override
    public boolean estaPaginaCargada() {
        try {
            // Verificar elementos clave del dashboard
            esperarElementoVisible(contenidoPrincipal);
            esperarElementoVisible(navegacionPrincipal);

            // Verificar que al menos uno de los elementos de usuario esté presente
            boolean usuarioVisible = estaElementoVisible(nombreUsuario) ||
                    estaElementoVisible(menuUsuario);

            return usuarioVisible;

        } catch (Exception e) {
            logger.error("Error verificando carga de página Dashboard: {}", e.getMessage());
            return false;
        }
    }

    // Métodos para información del usuario

    /**
     * Verifica si el nombre de usuario es visible en la barra superior
     *
     * @return true si el nombre de usuario está visible
     */
    public boolean esNombreUsuarioVisible() {
        return estaElementoVisible(nombreUsuario);
    }

    /**
     * Obtiene el nombre de usuario mostrado en la barra superior
     *
     * @return Nombre de usuario o cadena vacía si no está disponible
     */
    public String obtenerNombreUsuario() {
        if (esNombreUsuarioVisible()) {
            return obtenerTextoSeguro(nombreUsuario);
        }

        // Intentar obtener de atributos alternativos
        try {
            String textoAtributo = nombreUsuario.getAttribute("data-username");
            if (textoAtributo != null && !textoAtributo.trim().isEmpty()) {
                return textoAtributo;
            }
        } catch (Exception e) {
            logger.debug("No se pudo obtener nombre de usuario de atributos: {}", e.getMessage());
        }

        return "";
    }

    /**
     * Obtiene el mensaje de bienvenida
     *
     * @return Mensaje de bienvenida
     */
    public String obtenerMensajeBienvenida() {
        if (estaElementoVisible(mensajeBienvenida)) {
            return obtenerTextoSeguro(mensajeBienvenida);
        }

        // Buscar mensaje de bienvenida en otros elementos posibles
        try {
            WebElement mensajeAlternativo = driver.findElement(
                    By.cssSelector(".welcome, .greeting, [class*='welcome'], [id*='welcome']"));
            return obtenerTextoSeguro(mensajeAlternativo);
        } catch (Exception e) {
            logger.debug("No se encontró mensaje de bienvenida alternativo: {}", e.getMessage());
        }

        return "";
    }

    // Métodos de navegación

    /**
     * Navega a la página de productos
     *
     * @return true si la navegación fue exitosa
     */
    public boolean navegarAProductos() {
        try {
            if (estaElementoVisible(linkProductos)) {
                hacerClicSeguro(linkProductos);
                logger.info("Navegación a productos iniciada");
                return true;
            } else {
                // Intentar navegación alternativa
                return navegarPorTexto("Productos");
            }
        } catch (Exception e) {
            logger.error("Error navegando a productos: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Navega a la página de usuarios
     *
     * @return true si la navegación fue exitosa
     */
    public boolean navegarAUsuarios() {
        try {
            if (estaElementoVisible(linkUsuarios)) {
                hacerClicSeguro(linkUsuarios);
                logger.info("Navegación a usuarios iniciada");
                return true;
            } else {
                return navegarPorTexto("Usuarios");
            }
        } catch (Exception e) {
            logger.error("Error navegando a usuarios: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Navega a la página de reportes
     *
     * @return true si la navegación fue exitosa
     */
    public boolean navegarAReportes() {
        try {
            if (estaElementoVisible(linkReportes)) {
                hacerClicSeguro(linkReportes);
                logger.info("Navegación a reportes iniciada");
                return true;
            } else {
                return navegarPorTexto("Reportes");
            }
        } catch (Exception e) {
            logger.error("Error navegando a reportes: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Navega a la página de configuración
     *
     * @return true si la navegación fue exitosa
     */
    public boolean navegarAConfiguracion() {
        try {
            if (estaElementoVisible(linkConfiguracion)) {
                hacerClicSeguro(linkConfiguracion);
                logger.info("Navegación a configuración iniciada");
                return true;
            } else {
                return navegarPorTexto("Configuración");
            }
        } catch (Exception e) {
            logger.error("Error navegando a configuración: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si un elemento es navegable
     *
     * @param nombreElemento Nombre del elemento a verificar
     * @return true si el elemento es navegable
     */
    public boolean esElementoNavegable(String nombreElemento) {
        try {
            String nombreLower = nombreElemento.toLowerCase().trim();

            switch (nombreLower) {
                case "productos":
                    return estaElementoVisible(linkProductos) || existeEnlacePorTexto("Productos");

                case "usuarios":
                    return estaElementoVisible(linkUsuarios) || existeEnlacePorTexto("Usuarios");

                case "reportes":
                    return estaElementoVisible(linkReportes) || existeEnlacePorTexto("Reportes");

                case "configuracion", "configuración":
                    return estaElementoVisible(linkConfiguracion) || existeEnlacePorTexto("Configuración");

                default:
                    return existeEnlacePorTexto(nombreElemento);
            }

        } catch (Exception e) {
            logger.debug("Error verificando navegabilidad de '{}': {}", nombreElemento, e.getMessage());
            return false;
        }
    }

    // Métodos para widgets y estadísticas

    /**
     * Obtiene la cantidad de widgets visibles en el dashboard
     *
     * @return Número de widgets visibles
     */
    public int obtenerCantidadWidgets() {
        try {
            return (int) widgets.stream().filter(this::estaElementoVisible).count();
        } catch (Exception e) {
            logger.debug("Error contando widgets: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Verifica si las estadísticas están cargadas
     *
     * @return true si hay estadísticas visibles
     */
    public boolean estanEstadisticasCargadas() {
        try {
            return estaElementoVisible(contenedorEstadisticas) &&
                    !elementosEstadisticas.isEmpty() &&
                    elementosEstadisticas.stream().anyMatch(this::estaElementoVisible);
        } catch (Exception e) {
            logger.debug("Error verificando estadísticas: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el valor de una estadística específica
     *
     * @param nombreEstadistica Nombre de la estadística
     * @return Valor de la estadística o cadena vacía si no se encuentra
     */
    public String obtenerValorEstadistica(String nombreEstadistica) {
        try {
            // Buscar por atributo data-metric
            WebElement estadistica = driver.findElement(
                    By.cssSelector(String.format("[data-metric='%s']", nombreEstadistica.toLowerCase())));
            return obtenerTextoSeguro(estadistica);

        } catch (Exception e) {
            try {
                // Buscar por texto del label
                WebElement estadistica = driver.findElement(
                        By.xpath(String.format("//div[contains(@class,'stat')]//span[contains(text(),'%s')]/following-sibling::span", nombreEstadistica)));
                return obtenerTextoSeguro(estadistica);

            } catch (Exception e2) {
                logger.debug("No se pudo obtener estadística '{}': {}", nombreEstadistica, e2.getMessage());
                return "";
            }
        }
    }

    // Métodos para notificaciones

    /**
     * Verifica si hay notificaciones pendientes
     *
     * @return true si hay notificaciones
     */
    public boolean hayNotificacionesPendientes() {
        try {
            if (estaElementoVisible(contadorNotificaciones)) {
                String texto = obtenerTextoSeguro(contadorNotificaciones);
                return !texto.isEmpty() && !texto.equals("0");
            }
            return false;
        } catch (Exception e) {
            logger.debug("Error verificando notificaciones: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el número de notificaciones pendientes
     *
     * @return Número de notificaciones
     */
    public int obtenerNumeroNotificaciones() {
        try {
            if (hayNotificacionesPendientes()) {
                String texto = obtenerTextoSeguro(contadorNotificaciones);
                return Integer.parseInt(texto.replaceAll("[^0-9]", ""));
            }
            return 0;
        } catch (Exception e) {
            logger.debug("Error obteniendo número de notificaciones: {}", e.getMessage());
            return 0;
        }
    }

    // Métodos para búsqueda rápida

    /**
     * Realiza una búsqueda rápida
     *
     * @param termino Término a buscar
     * @return true si la búsqueda se ejecutó exitosamente
     */
    public boolean realizarBusquedaRapida(String termino) {
        try {
            if (estaElementoVisible(cajaBusquedaRapida)) {
                escribirTextoSeguro(cajaBusquedaRapida, termino);

                if (estaElementoVisible(botonBuscar)) {
                    hacerClicSeguro(botonBuscar);
                } else {
                    // Presionar Enter en la caja de búsqueda
                    cajaBusquedaRapida.sendKeys(org.openqa.selenium.Keys.ENTER);
                }

                logger.info("Búsqueda rápida realizada: {}", termino);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error realizando búsqueda rápida: {}", e.getMessage());
            return false;
        }
    }

    // Métodos para gestión de sesión

    /**
     * Cierra la sesión del usuario
     *
     * @return true si el cierre de sesión fue exitoso
     */
    public boolean cerrarSesion() {
        try {
            if (estaElementoVisible(botonCerrarSesion)) {
                hacerClicSeguro(botonCerrarSesion);
                logger.info("Cierre de sesión iniciado");
                return true;
            } else {
                // Buscar botón de logout alternativo
                try {
                    WebElement logoutAlt = driver.findElement(
                            By.cssSelector("a[href*='logout'], button[onclick*='logout'], .logout"));
                    hacerClicSeguro(logoutAlt);
                    logger.info("Cierre de sesión iniciado (método alternativo)");
                    return true;
                } catch (Exception e2) {
                    logger.warn("No se encontró botón de cierre de sesión");
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("Error cerrando sesión: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Espera a que la página del dashboard termine de cargar completamente
     *
     * @return true si la página cargó exitosamente
     */
    public boolean esperarCargaPagina() {
        try {
            // Esperar elementos críticos
            esperarElementoVisible(contenidoPrincipal);
            esperarElementoVisible(navegacionPrincipal);

            // Esperar que termine la carga de JavaScript
            esperarCargaCompleta();

            // Verificar que al menos algunos widgets estén cargados
            esperarTiempo(1000); // Dar tiempo para que carguen los widgets

            return estaPaginaCargada();

        } catch (Exception e) {
            logger.error("Error esperando carga de dashboard: {}", e.getMessage());
            return false;
        }
    }

    // Métodos auxiliares privados

    /**
     * Navega a una sección por texto del enlace
     *
     * @param textoEnlace Texto del enlace a buscar
     * @return true si se encontró y se hizo clic en el enlace
     */
    private boolean navegarPorTexto(String textoEnlace) {
        try {
            WebElement enlace = driver.findElement(
                    By.xpath(String.format("//a[contains(text(),'%s')] | //button[contains(text(),'%s')]",
                            textoEnlace, textoEnlace)));
            hacerClicSeguro(enlace);
            logger.info("Navegación por texto exitosa: {}", textoEnlace);
            return true;
        } catch (Exception e) {
            logger.debug("No se pudo navegar por texto '{}': {}", textoEnlace, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si existe un enlace con el texto especificado
     *
     * @param texto Texto a buscar en los enlaces
     * @return true si existe el enlace
     */
    private boolean existeEnlacePorTexto(String texto) {
        try {
            WebElement enlace = driver.findElement(
                    By.xpath(String.format("//a[contains(text(),'%s')] | //button[contains(text(),'%s')]",
                            texto, texto)));
            return enlace.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene información del estado actual del dashboard
     *
     * @return String con información del estado
     */
    public String obtenerEstadoDashboard() {
        StringBuilder estado = new StringBuilder();
        estado.append("=== Estado del Dashboard ===\n");

        estado.append("Página cargada: ").append(estaPaginaCargada()).append("\n");
        estado.append("Usuario visible: ").append(esNombreUsuarioVisible()).append("\n");
        estado.append("Nombre usuario: ").append(obtenerNombreUsuario()).append("\n");
        estado.append("Widgets visibles: ").append(obtenerCantidadWidgets()).append("\n");
        estado.append("Estadísticas cargadas: ").append(estanEstadisticasCargadas()).append("\n");
        estado.append("Notificaciones pendientes: ").append(hayNotificacionesPendientes()).append("\n");

        if (hayNotificacionesPendientes()) {
            estado.append("Número de notificaciones: ").append(obtenerNumeroNotificaciones()).append("\n");
        }

        return estado.toString();
    }
}