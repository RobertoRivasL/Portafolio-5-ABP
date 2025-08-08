package com.qa.automatizacion.paginas;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

/**
 * Page Object para la página de dashboard principal del sistema.
 * Representa la página principal después del login exitoso.
 *
 * Principios aplicados:
 * - Page Object Pattern: Separa la lógica de UI de los tests
 * - Encapsulación: Oculta los detalles de implementación de Selenium
 * - Herencia: Extiende PaginaBase para reutilizar funcionalidades comunes
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0
 */
public class PaginaDashboard extends PaginaBase {

    // ==================== LOCALIZADORES ====================

    // Barra de navegación superior
    private static final By BARRA_NAVEGACION = By.cssSelector(".navbar, .top-nav, [data-testid='navbar']");
    private static final By NOMBRE_USUARIO = By.cssSelector(".user-name, .username, [data-testid='username']");
    private static final By AVATAR_USUARIO = By.cssSelector(".user-avatar, .avatar, [data-testid='user-avatar']");
    private static final By MENU_USUARIO = By.cssSelector(".user-menu, .dropdown-user, [data-testid='user-menu']");

    // Mensajes de bienvenida
    private static final By MENSAJE_BIENVENIDA = By.cssSelector(".welcome-message, .greeting, [data-testid='welcome-message']");
    private static final By TITULO_DASHBOARD = By.cssSelector("h1, .dashboard-title, [data-testid='dashboard-title']");

    // Menú lateral
    private static final By MENU_LATERAL = By.cssSelector(".sidebar, .side-nav, [data-testid='sidebar']");
    private static final By ITEM_MENU_PRODUCTOS = By.cssSelector("a[href*='productos'], [data-testid='menu-productos']");
    private static final By ITEM_MENU_USUARIOS = By.cssSelector("a[href*='usuarios'], [data-testid='menu-usuarios']");
    private static final By ITEM_MENU_REPORTES = By.cssSelector("a[href*='reportes'], [data-testid='menu-reportes']");
    private static final By ITEM_MENU_CONFIGURACION = By.cssSelector("a[href*='configuracion'], [data-testid='menu-configuracion']");

    // Contenido principal
    private static final By CONTENIDO_PRINCIPAL = By.cssSelector(".dashboard-content, .main-content, [data-testid='main-content']");
    private static final By CONTENEDOR_WIDGETS = By.cssSelector(".dashboard-widgets, .widgets-container, [data-testid='widgets']");
    private static final By WIDGETS = By.cssSelector(".widget, .dashboard-card, [data-testid='widget']");

    // Estadísticas
    private static final By CONTENEDOR_ESTADISTICAS = By.cssSelector(".stats-container, .statistics, [data-testid='stats']");
    private static final By ELEMENTOS_ESTADISTICAS = By.cssSelector(".stat-item, .metric, [data-testid='stat']");

    // Notificaciones
    private static final By CONTENEDOR_NOTIFICACIONES = By.cssSelector(".notifications, .alerts, [data-testid='notifications']");
    private static final By CONTADOR_NOTIFICACIONES = By.cssSelector(".notification-badge, .alert-count, [data-testid='notification-count']");

    // Búsqueda rápida
    private static final By CAJA_BUSQUEDA_RAPIDA = By.cssSelector(".search-box, #quick-search, [data-testid='search']");
    private static final By BOTON_BUSCAR = By.cssSelector(".search-btn, .btn-search, [data-testid='search-button']");

    // Botones de acción
    private static final By BOTON_CERRAR_SESION = By.cssSelector(".logout-btn, #logout, .btn-logout, [data-testid='logout']");
    private static final By BOTON_NUEVO_PRODUCTO = By.cssSelector(".btn-new-product, [data-testid='new-product']");
    private static final By BOTON_GESTIONAR_USUARIOS = By.cssSelector(".btn-manage-users, [data-testid='manage-users']");

    // Elementos usando @FindBy para demostrar ambos enfoques
    @FindBy(css = ".welcome-message, .greeting, [data-testid='welcome-message']")
    private WebElement mensajeBienvenidaElement;

    @FindBy(css = ".user-name, .username, [data-testid='username']")
    private WebElement nombreUsuarioElement;

    @FindBy(css = ".dashboard-content, .main-content, [data-testid='main-content']")
    private WebElement contenidoPrincipalElement;

    // ==================== CONSTRUCTOR ====================

    /**
     * Constructor que inicializa la página del dashboard.
     */
    public PaginaDashboard() {
        super();
        logger.info("PaginaDashboard inicializada");
    }

    // ==================== MÉTODOS ABSTRACTOS IMPLEMENTADOS ====================

    /**
     * Verifica si la página de dashboard está completamente cargada.
     *
     * @return true si la página está cargada, false en caso contrario
     */
    @Override
    public boolean esPaginaCargada() {
        try {
            boolean contenidoPresente = esElementoPresente(CONTENIDO_PRINCIPAL);
            boolean barraNavegacionPresente = esElementoPresente(BARRA_NAVEGACION);
            boolean usuarioVisible = esElementoPresente(NOMBRE_USUARIO) || esElementoPresente(MENU_USUARIO);
            boolean mensajeBienvenidaPresente = esElementoPresente(MENSAJE_BIENVENIDA);

            boolean cargada = contenidoPresente && barraNavegacionPresente && usuarioVisible;

            logger.debug("Verificación de carga - Contenido: {}, Barra: {}, Usuario: {}, Mensaje: {}, Resultado: {}",
                    contenidoPresente, barraNavegacionPresente, usuarioVisible, mensajeBienvenidaPresente, cargada);

            return cargada;

        } catch (Exception e) {
            logger.error("Error verificando carga de página dashboard: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el título esperado de la página de dashboard.
     *
     * @return título esperado
     */
    @Override
    public String obtenerTituloEsperado() {
        return "Dashboard";
    }

    /**
     * Obtiene la URL esperada de la página de dashboard.
     *
     * @return URL esperada
     */
    @Override
    public String obtenerUrlEsperada() {
        return utileria.obtenerUrlDashboard();
    }

    // ==================== MÉTODOS DE INFORMACIÓN DEL USUARIO ====================

    /**
     * Verifica si hay información de usuario visible.
     *
     * @return true si hay información de usuario, false en caso contrario
     */
    public boolean hayInformacionUsuario() {
        return esElementoPresente(NOMBRE_USUARIO) || esElementoPresente(MENU_USUARIO);
    }

    /**
     * Obtiene el nombre de usuario mostrado en la barra superior.
     *
     * @return nombre de usuario o cadena vacía si no está disponible
     */
    public String obtenerNombreUsuario() {
        registrarAccion("Obteniendo nombre de usuario");

        try {
            if (esElementoPresente(NOMBRE_USUARIO)) {
                String nombre = obtenerTexto(NOMBRE_USUARIO);
                if (!nombre.isEmpty()) {
                    logger.debug("Nombre de usuario obtenido: {}", nombre);
                    return nombre;
                }
            }

            // Intentar obtener de atributos alternativos
            try {
                WebElement elemento = buscarElemento(NOMBRE_USUARIO);
                String textoAtributo = elemento.getAttribute("data-username");
                if (textoAtributo != null && !textoAtributo.trim().isEmpty()) {
                    logger.debug("Nombre de usuario obtenido de atributo: {}", textoAtributo);
                    return textoAtributo;
                }
            } catch (Exception e) {
                logger.debug("No se pudo obtener nombre de usuario de atributos: {}", e.getMessage());
            }

            // Buscar en otros elementos posibles
            List<WebElement> elementos = buscarElementos(By.cssSelector(".user, .username, [data-user], [class*='user']"));
            for (WebElement elemento : elementos) {
                if (esElementoVisible(By.xpath(".//*"))) {
                    String texto = elemento.getText().trim();
                    if (!texto.isEmpty()) {
                        logger.debug("Nombre de usuario encontrado en elemento alternativo: {}", texto);
                        return texto;
                    }
                }
            }

            logger.warn("No se pudo obtener nombre de usuario");
            return "";

        } catch (Exception e) {
            logger.error("Error obteniendo nombre de usuario: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Verifica si hay mensaje de bienvenida visible.
     *
     * @return true si hay mensaje de bienvenida, false en caso contrario
     */
    public boolean hayMensajeBienvenida() {
        return esElementoVisible(MENSAJE_BIENVENIDA);
    }

    /**
     * Obtiene el mensaje de bienvenida.
     *
     * @return mensaje de bienvenida o cadena vacía si no está disponible
     */
    public String obtenerMensajeBienvenida() {
        registrarAccion("Obteniendo mensaje de bienvenida");

        try {
            if (hayMensajeBienvenida()) {
                String mensaje = obtenerTexto(MENSAJE_BIENVENIDA);
                logger.debug("Mensaje de bienvenida obtenido: {}", mensaje);
                return mensaje;
            }

            // Buscar mensaje de bienvenida en otros elementos posibles
            List<WebElement> elementos = buscarElementos(By.cssSelector(".welcome, .greeting, [class*='welcome'], [id*='welcome']"));
            for (WebElement elemento : elementos) {
                if (elemento.isDisplayed()) {
                    String texto = elemento.getText().trim();
                    if (!texto.isEmpty()) {
                        logger.debug("Mensaje de bienvenida encontrado en elemento alternativo: {}", texto);
                        return texto;
                    }
                }
            }

            logger.debug("No se encontró mensaje de bienvenida");
            return "";

        } catch (Exception e) {
            logger.error("Error obteniendo mensaje de bienvenida: {}", e.getMessage());
            return "";
        }
    }

    // ==================== MÉTODOS DE NAVEGACIÓN ====================

    /**
     * Navega a la sección de productos.
     */
    public void navegarAProductos() {
        registrarAccion("Navegando a sección de productos");

        try {
            hacerClick(ITEM_MENU_PRODUCTOS);
            esperarCargaPagina();
            logger.debug("Navegación a productos ejecutada");
        } catch (Exception e) {
            logger.error("Error navegando a productos: {}", e.getMessage());
            throw new RuntimeException("No se pudo navegar a productos", e);
        }
    }

    /**
     * Navega a la sección de usuarios.
     */
    public void navegarAUsuarios() {
        registrarAccion("Navegando a sección de usuarios");

        try {
            hacerClick(ITEM_MENU_USUARIOS);
            esperarCargaPagina();
            logger.debug("Navegación a usuarios ejecutada");
        } catch (Exception e) {
            logger.error("Error navegando a usuarios: {}", e.getMessage());
            throw new RuntimeException("No se pudo navegar a usuarios", e);
        }
    }

    /**
     * Navega a la sección de reportes.
     */
    public void navegarAReportes() {
        registrarAccion("Navegando a sección de reportes");

        try {
            hacerClick(ITEM_MENU_REPORTES);
            esperarCargaPagina();
            logger.debug("Navegación a reportes ejecutada");
        } catch (Exception e) {
            logger.error("Error navegando a reportes: {}", e.getMessage());
            throw new RuntimeException("No se pudo navegar a reportes", e);
        }
    }

    /**
     * Navega a la configuración del sistema.
     */
    public void navegarAConfiguracion() {
        registrarAccion("Navegando a configuración");

        try {
            hacerClick(ITEM_MENU_CONFIGURACION);
            esperarCargaPagina();
            logger.debug("Navegación a configuración ejecutada");
        } catch (Exception e) {
            logger.error("Error navegando a configuración: {}", e.getMessage());
            throw new RuntimeException("No se pudo navegar a configuración", e);
        }
    }

    // ==================== MÉTODOS DE ACCIONES ====================

    /**
     * Hace clic en el botón para crear un nuevo producto.
     */
    public void hacerClickNuevoProducto() {
        registrarAccion("Haciendo clic en 'Nuevo Producto'");

        try {
            hacerClick(BOTON_NUEVO_PRODUCTO);
            logger.debug("Clic en 'Nuevo Producto' ejecutado");
        } catch (Exception e) {
            logger.error("Error haciendo clic en 'Nuevo Producto': {}", e.getMessage());
            throw new RuntimeException("No se pudo hacer clic en 'Nuevo Producto'", e);
        }
    }

    /**
     * Hace clic en el botón para gestionar usuarios.
     */
    public void hacerClickGestionarUsuarios() {
        registrarAccion("Haciendo clic en 'Gestionar Usuarios'");

        try {
            hacerClick(BOTON_GESTIONAR_USUARIOS);
            logger.debug("Clic en 'Gestionar Usuarios' ejecutado");
        } catch (Exception e) {
            logger.error("Error haciendo clic en 'Gestionar Usuarios': {}", e.getMessage());
            throw new RuntimeException("No se pudo hacer clic en 'Gestionar Usuarios'", e);
        }
    }

    /**
     * Cierra la sesión del usuario.
     */
    public void cerrarSesion() {
        registrarAccion("Cerrando sesión");

        try {
            hacerClick(BOTON_CERRAR_SESION);
            logger.info("Cierre de sesión iniciado");
        } catch (Exception e) {
            logger.error("Error cerrando sesión: {}", e.getMessage());

            // Intentar método alternativo
            try {
                List<WebElement> botonesLogout = buscarElementos(
                        By.cssSelector("a[href*='logout'], button[onclick*='logout'], .logout"));

                for (WebElement boton : botonesLogout) {
                    if (boton.isDisplayed()) {
                        boton.click();
                        logger.info("Cierre de sesión ejecutado con método alternativo");
                        return;
                    }
                }

                throw new RuntimeException("No se encontró botón de cierre de sesión", e);

            } catch (Exception e2) {
                logger.error("Error en método alternativo de cierre de sesión: {}", e2.getMessage());
                throw new RuntimeException("No se pudo cerrar sesión", e2);
            }
        }
    }

    // ==================== MÉTODOS DE WIDGETS Y ESTADÍSTICAS ====================

    /**
     * Obtiene la cantidad de widgets visibles en el dashboard.
     *
     * @return número de widgets visibles
     */
    public int obtenerCantidadWidgets() {
        registrarAccion("Contando widgets visibles");

        try {
            List<WebElement> widgets = buscarElementos(WIDGETS);
            int widgetsVisibles = 0;

            for (WebElement widget : widgets) {
                if (widget.isDisplayed()) {
                    widgetsVisibles++;
                }
            }

            logger.debug("Widgets visibles encontrados: {}", widgetsVisibles);
            return widgetsVisibles;

        } catch (Exception e) {
            logger.error("Error contando widgets: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Verifica si las estadísticas están cargadas.
     *
     * @return true si hay estadísticas visibles, false en caso contrario
     */
    public boolean estanEstadisticasCargadas() {
        try {
            boolean contenedorPresente = esElementoPresente(CONTENEDOR_ESTADISTICAS);
            if (!contenedorPresente) {
                return false;
            }

            List<WebElement> elementos = buscarElementos(ELEMENTOS_ESTADISTICAS);
            boolean hayElementosVisibles = elementos.stream().anyMatch(WebElement::isDisplayed);

            logger.debug("Estadísticas cargadas - Contenedor: {}, Elementos visibles: {}",
                    contenedorPresente, hayElementosVisibles);

            return hayElementosVisibles;

        } catch (Exception e) {
            logger.error("Error verificando estadísticas: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE NOTIFICACIONES ====================

    /**
     * Verifica si hay notificaciones pendientes.
     *
     * @return true si hay notificaciones, false en caso contrario
     */
    public boolean hayNotificacionesPendientes() {
        try {
            if (esElementoVisible(CONTADOR_NOTIFICACIONES)) {
                String texto = obtenerTexto(CONTADOR_NOTIFICACIONES);
                boolean hayNotificaciones = !texto.isEmpty() && !texto.equals("0");
                logger.debug("Notificaciones pendientes: {} (texto: '{}')", hayNotificaciones, texto);
                return hayNotificaciones;
            }
            return false;
        } catch (Exception e) {
            logger.debug("Error verificando notificaciones: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el número de notificaciones pendientes.
     *
     * @return número de notificaciones
     */
    public int obtenerNumeroNotificaciones() {
        try {
            if (hayNotificacionesPendientes()) {
                String texto = obtenerTexto(CONTADOR_NOTIFICACIONES);
                String numeroTexto = texto.replaceAll("[^0-9]", "");
                if (!numeroTexto.isEmpty()) {
                    int numero = Integer.parseInt(numeroTexto);
                    logger.debug("Número de notificaciones: {}", numero);
                    return numero;
                }
            }
            return 0;
        } catch (Exception e) {
            logger.error("Error obteniendo número de notificaciones: {}", e.getMessage());
            return 0;
        }
    }

    // ==================== MÉTODOS DE BÚSQUEDA ====================

    /**
     * Realiza una búsqueda rápida.
     *
     * @param termino término a buscar
     */
    public void realizarBusquedaRapida(String termino) {
        registrarAccion("Realizando búsqueda rápida: " + termino);

        try {
            utileria.validarCadenaNoVacia(termino, "Término de búsqueda");

            if (esElementoPresente(CAJA_BUSQUEDA_RAPIDA)) {
                ingresarTexto(CAJA_BUSQUEDA_RAPIDA, termino);

                if (esElementoPresente(BOTON_BUSCAR)) {
                    hacerClick(BOTON_BUSCAR);
                } else {
                    // Presionar Enter en la caja de búsqueda
                    WebElement caja = buscarElemento(CAJA_BUSQUEDA_RAPIDA);
                    caja.sendKeys(org.openqa.selenium.Keys.ENTER);
                }

                logger.info("Búsqueda rápida realizada: {}", termino);
            } else {
                throw new RuntimeException("Caja de búsqueda no disponible");
            }

        } catch (Exception e) {
            logger.error("Error realizando búsqueda rápida: {}", e.getMessage());
            throw new RuntimeException("Error en búsqueda rápida: " + e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Verifica si un elemento de navegación es accesible.
     *
     * @param nombreElemento nombre del elemento a verificar
     * @return true si el elemento es navegable, false en caso contrario
     */
    public boolean esElementoNavegable(String nombreElemento) {
        try {
            String nombreLower = nombreElemento.toLowerCase().trim();

            return switch (nombreLower) {
                case "productos" -> esElementoPresente(ITEM_MENU_PRODUCTOS);
                case "usuarios" -> esElementoPresente(ITEM_MENU_USUARIOS);
                case "reportes" -> esElementoPresente(ITEM_MENU_REPORTES);
                case "configuracion", "configuración" -> esElementoPresente(ITEM_MENU_CONFIGURACION);
                default -> {
                    // Buscar por texto genérico
                    try {
                        WebElement elemento = utileria.obtenerNavegador().findElement(
                                By.xpath(String.format("//a[contains(text(),'%s')] | //button[contains(text(),'%s')]",
                                        nombreElemento, nombreElemento)));
                        yield elemento.isDisplayed();
                    } catch (Exception e) {
                        yield false;
                    }
                }
            };

        } catch (Exception e) {
            logger.debug("Error verificando navegabilidad de '{}': {}", nombreElemento, e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE INFORMACIÓN ====================

    /**
     * Obtiene información completa del estado actual del dashboard.
     *
     * @return información detallada del estado
     */
    public String obtenerEstadoCompleto() {
        try {
            return String.format(
                    "Estado Dashboard - Cargada: %s | Usuario visible: %s | Nombre: '%s' | " +
                            "Mensaje bienvenida: %s | Widgets: %d | Estadísticas cargadas: %s | " +
                            "Notificaciones: %s | %s",
                    esPaginaCargada(),
                    hayInformacionUsuario(),
                    obtenerNombreUsuario(),
                    hayMensajeBienvenida(),
                    obtenerCantidadWidgets(),
                    estanEstadisticasCargadas(),
                    hayNotificacionesPendientes(),
                    obtenerInformacionDebug()
            );

        } catch (Exception e) {
            return "Error obteniendo estado completo: " + e.getMessage();
        }
    }

    // ==================== MÉTODOS DE LIMPIEZA ====================

    /**
     * Sobrescribe el método de limpieza base para incluir limpieza específica del dashboard.
     */
    @Override
    public void limpiar() {
        super.limpiar();

        try {
            // Limpieza específica del dashboard
            // Por ejemplo: cerrar modales abiertos, limpiar filtros, etc.
            logger.debug("Limpieza específica del dashboard ejecutada");
        } catch (Exception e) {
            logger.warn("Error en limpieza específica del dashboard: {}", e.getMessage());
        }
    }
}