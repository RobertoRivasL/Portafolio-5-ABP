package com.qa.automatizacion.paginas;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object para la página de dashboard principal del sistema.
 * Representa la página principal después del login exitoso.
 *
 * Principios aplicados:
 * - Page Object Pattern: Separa la lógica de UI de los tests
 * - Encapsulación: Oculta los detalles de implementación de Selenium
 * - Herencia: Extiende PaginaBase para reutilizar funcionalidades comunes
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

    // Área principal de contenido
    private static final By CONTENIDO_PRINCIPAL = By.cssSelector(".main-content, .dashboard-content, [data-testid='main-content']");
    private static final By WIDGETS_DASHBOARD = By.cssSelector(".widget, .dashboard-widget, [data-testid='widget']");

    // Estadísticas y métricas
    private static final By WIDGET_PRODUCTOS = By.cssSelector(".widget-productos, [data-testid='widget-productos']");
    private static final By WIDGET_USUARIOS = By.cssSelector(".widget-usuarios, [data-testid='widget-usuarios']");
    private static final By WIDGET_VENTAS = By.cssSelector(".widget-ventas, [data-testid='widget-ventas']");
    private static final By WIDGET_ACTIVIDAD = By.cssSelector(".widget-actividad, [data-testid='widget-actividad']");

    // Botones de acción
    private static final By BOTON_NUEVO_PRODUCTO = By.cssSelector(".btn-nuevo-producto, [data-testid='btn-nuevo-producto']");
    private static final By BOTON_GESTIONAR_USUARIOS = By.cssSelector(".btn-gestionar-usuarios, [data-testid='btn-gestionar-usuarios']");
    private static final By BOTON_VER_REPORTES = By.cssSelector(".btn-ver-reportes, [data-testid='btn-ver-reportes']");

    // Opciones de usuario
    private static final By BOTON_PERFIL = By.cssSelector(".btn-perfil, [data-testid='btn-perfil']");
    private static final By BOTON_CONFIGURACION = By.cssSelector(".btn-configuracion, [data-testid='btn-configuracion']");
    private static final By BOTON_CERRAR_SESION = By.cssSelector(".btn-logout, .cerrar-sesion, [data-testid='btn-logout']");

    // Notificaciones
    private static final By ICONO_NOTIFICACIONES = By.cssSelector(".notifications-icon, [data-testid='notifications']");
    private static final By CONTADOR_NOTIFICACIONES = By.cssSelector(".notification-count, [data-testid='notification-count']");
    private static final By PANEL_NOTIFICACIONES = By.cssSelector(".notifications-panel, [data-testid='notifications-panel']");

    // Búsqueda rápida
    private static final By CAMPO_BUSQUEDA_RAPIDA = By.cssSelector(".quick-search, [data-testid='quick-search']");
    private static final By RESULTADOS_BUSQUEDA = By.cssSelector(".search-results, [data-testid='search-results']");

    // Footer
    private static final By PIE_PAGINA = By.cssSelector(".footer, [data-testid='footer']");
    private static final By VERSION_SISTEMA = By.cssSelector(".version, [data-testid='version']");

    // ==================== MÉTODOS PRINCIPALES ====================

    /**
     * Verifica si la página de dashboard está completamente cargada.
     *
     * @return true si la página está cargada
     */
    @Override
    public boolean estaPaginaCargada() {
        registrarAccion("Verificando carga de página dashboard");

        try {
            // Verificar elementos esenciales del dashboard
            boolean barraNavegacionVisible = esElementoVisible(BARRA_NAVEGACION);
            boolean contenidoPrincipalVisible = esElementoVisible(CONTENIDO_PRINCIPAL);
            boolean mensajeBienvenidaPresente = esElementoPresente(MENSAJE_BIENVENIDA) ||
                    esElementoPresente(TITULO_DASHBOARD);

            boolean paginaCargada = barraNavegacionVisible && contenidoPrincipalVisible && mensajeBienvenidaPresente;

            logger.debug("Dashboard cargado: {} (Barra: {}, Contenido: {}, Mensaje: {})",
                    paginaCargada, barraNavegacionVisible, contenidoPrincipalVisible, mensajeBienvenidaPresente);

            return paginaCargada;

        } catch (Exception e) {
            logger.error("Error verificando carga de dashboard: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la URL esperada para el dashboard.
     *
     * @return URL esperada
     */
    @Override
    public String obtenerUrlEsperada() {
        return propiedades.obtenerUrlDashboard();
    }

    // ==================== MÉTODOS DE VERIFICACIÓN ====================

    /**
     * Verifica si el mensaje de bienvenida es visible.
     *
     * @return true si el mensaje de bienvenida es visible
     */
    public boolean esMensajeBienvenidaVisible() {
        return esElementoVisible(MENSAJE_BIENVENIDA) || esElementoVisible(TITULO_DASHBOARD);
    }

    /**
     * Verifica si el nombre de usuario es visible en la barra superior.
     *
     * @return true si el nombre de usuario es visible
     */
    public boolean esNombreUsuarioVisible() {
        return esElementoVisible(NOMBRE_USUARIO);
    }

    /**
     * Verifica si el menú lateral es visible.
     *
     * @return true si el menú lateral es visible
     */
    public boolean esMenuLateralVisible() {
        return esElementoVisible(MENU_LATERAL);
    }

    /**
     * Verifica si los widgets del dashboard están visibles.
     *
     * @return true si al menos un widget es visible
     */
    public boolean sonWidgetsVisibles() {
        List<WebElement> widgets = obtenerElementos(WIDGETS_DASHBOARD);
        return !widgets.isEmpty() && widgets.stream().anyMatch(WebElement::isDisplayed);
    }

    /**
     * Verifica si las notificaciones están disponibles.
     *
     * @return true si el icono de notificaciones es visible
     */
    public boolean sonNotificacionesVisibles() {
        return esElementoVisible(ICONO_NOTIFICACIONES);
    }

    // ==================== MÉTODOS DE OBTENCIÓN DE DATOS ====================

    /**
     * Obtiene el mensaje de bienvenida actual.
     *
     * @return texto del mensaje de bienvenida
     */
    public String obtenerMensajeBienvenida() {
        try {
            if (esElementoVisible(MENSAJE_BIENVENIDA)) {
                return obtenerTexto(MENSAJE_BIENVENIDA);
            } else if (esElementoVisible(TITULO_DASHBOARD)) {
                return obtenerTexto(TITULO_DASHBOARD);
            }
        } catch (Exception e) {
            logger.debug("No se pudo obtener mensaje de bienvenida: {}", e.getMessage());
        }
        return "";
    }

    /**
     * Obtiene el nombre de usuario mostrado.
     *
     * @return nombre de usuario
     */
    public String obtenerNombreUsuario() {
        try {
            return obtenerTexto(NOMBRE_USUARIO);
        } catch (Exception e) {
            logger.debug("No se pudo obtener nombre de usuario: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Obtiene el número de notificaciones pendientes.
     *
     * @return número de notificaciones o 0 si no hay
     */
    public int obtenerNumeroNotificaciones() {
        try {
            if (esElementoVisible(CONTADOR_NOTIFICACIONES)) {
                String contador = obtenerTexto(CONTADOR_NOTIFICACIONES);
                return Integer.parseInt(contador.replaceAll("[^0-9]", ""));
            }
        } catch (Exception e) {
            logger.debug("No se pudo obtener número de notificaciones: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * Obtiene la versión del sistema si está visible.
     *
     * @return versión del sistema
     */
    public String obtenerVersionSistema() {
        try {
            if (esElementoVisible(VERSION_SISTEMA)) {
                return obtenerTexto(VERSION_SISTEMA);
            }
        } catch (Exception e) {
            logger.debug("No se pudo obtener versión del sistema: {}", e.getMessage());
        }
        return "";
    }

    // ==================== MÉTODOS DE NAVEGACIÓN ====================

    /**
     * Navega a la sección de productos.
     */
    public void navegarAProductos() {
        registrarAccion("Navegando a sección de productos");

        try {
            hacerClickSeguro(ITEM_MENU_PRODUCTOS);
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
            hacerClickSeguro(ITEM_MENU_USUARIOS);
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
            hacerClickSeguro(ITEM_MENU_REPORTES);
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
            hacerClickSeguro(ITEM_MENU_CONFIGURACION);
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
            hacerClickSeguro(BOTON_NUEVO_PRODUCTO);
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
            hacerClickSeguro(BOTON_GESTIONAR_USUARIOS);
            logger.debug("Clic en 'Gestionar Usuarios' ejecutado");
        } catch (Exception e) {
            logger.error("Error haciendo clic en 'Gestionar Usuarios': {}", e.getMessage());
            throw new RuntimeException("No se pudo hacer clic en 'Gestionar Usuarios'", e);
        }
    }

    /**
     * Hace clic en el botón para ver reportes.
     */
    public void hacerClickVerReportes() {
        registrarAccion("Haciendo clic en 'Ver Reportes'");

        try {
            hacerClickSeguro(BOTON_VER_REPORTES);
            logger.debug("Clic en 'Ver Reportes' ejecutado");
        } catch (Exception e) {
            logger.error("Error haciendo clic en 'Ver Reportes': {}", e.getMessage());
            throw new RuntimeException("No se pudo hacer clic en 'Ver Reportes'", e);
        }
    }

    /**
     * Abre el menú de usuario.
     */
    public void abrirMenuUsuario() {
        registrarAccion("Abriendo menú de usuario");

        try {
            // Intentar hacer clic en el nombre de usuario o avatar
            if (esElementoVisible(NOMBRE_USUARIO)) {
                hacerClickSeguro(NOMBRE_USUARIO);
            } else if (esElementoVisible(AVATAR_USUARIO)) {
                hacerClickSeguro(AVATAR_USUARIO);
            } else {
                throw new RuntimeException("No se encontró elemento para abrir menú de usuario");
            }

            // Esperar a que aparezca el menú
            esperarElementoVisible(MENU_USUARIO);

            logger.debug("Menú de usuario abierto");
        } catch (Exception e) {
            logger.error("Error abriendo menú de usuario: {}", e.getMessage());
            throw new RuntimeException("No se pudo abrir el menú de usuario", e);
        }
    }

    /**
     * Accede al perfil de usuario.
     */
    public void accederPerfil() {
        registrarAccion("Accediendo al perfil de usuario");

        try {
            abrirMenuUsuario();
            hacerClickSeguro(BOTON_PERFIL);
            esperarCargaPagina();
            logger.debug("Acceso al perfil ejecutado");
        } catch (Exception e) {
            logger.error("Error accediendo al perfil: {}", e.getMessage());
            throw new RuntimeException("No se pudo acceder al perfil", e);
        }
    }

    /**
     * Cierra la sesión del usuario.
     */
    public void cerrarSesion() {
        registrarAccion("Cerrando sesión de usuario");

        try {
            abrirMenuUsuario();
            hacerClickSeguro(BOTON_CERRAR_SESION);
            esperarCargaPagina();
            logger.info("Sesión cerrada exitosamente");
        } catch (Exception e) {
            logger.error("Error cerrando sesión: {}", e.getMessage());
            throw new RuntimeException("No se pudo cerrar la sesión", e);
        }
    }

    // ==================== MÉTODOS DE BÚSQUEDA ====================

    /**
     * Realiza una búsqueda rápida en el dashboard.
     *
     * @param termino término de búsqueda
     */
    public void realizarBusquedaRapida(String termino) {
        registrarAccion("Realizando búsqueda rápida: " + termino);

        try {
            if (esElementoVisible(CAMPO_BUSQUEDA_RAPIDA)) {
                ingresarTextoSeguro(CAMPO_BUSQUEDA_RAPIDA, termino);

                // Presionar Enter o esperar resultados automáticos
                WebElement campo = esperarElementoVisible(CAMPO_BUSQUEDA_RAPIDA);
                campo.sendKeys(org.openqa.selenium.Keys.ENTER);

                // Esperar resultados
                Thread.sleep(1000);

                logger.debug("Búsqueda rápida ejecutada: {}", termino);
            }
        } catch (Exception e) {
            logger.error("Error en búsqueda rápida '{}': {}", termino, e.getMessage());
            throw new RuntimeException("No se pudo realizar la búsqueda rápida", e);
        }
    }

    /**
     * Verifica si hay resultados de búsqueda visibles.
     *
     * @return true si hay resultados visibles
     */
    public boolean hayResultadosBusqueda() {
        return esElementoVisible(RESULTADOS_BUSQUEDA);
    }

    /**
     * Obtiene la cantidad de resultados de búsqueda.
     *
     * @return número de resultados
     */
    public int obtenerCantidadResultados() {
        try {
            if (hayResultadosBusqueda()) {
                List<WebElement> resultados = obtenerElementos(By.cssSelector(".search-result-item, [data-testid='search-result']"));
                return resultados.size();
            }
        } catch (Exception e) {
            logger.debug("Error obteniendo cantidad de resultados: {}", e.getMessage());
        }
        return 0;
    }

    // ==================== MÉTODOS DE NOTIFICACIONES ====================

    /**
     * Abre el panel de notificaciones.
     */
    public void abrirNotificaciones() {
        registrarAccion("Abriendo panel de notificaciones");

        try {
            if (sonNotificacionesVisibles()) {
                hacerClickSeguro(ICONO_NOTIFICACIONES);
                esperarElementoVisible(PANEL_NOTIFICACIONES);
                logger.debug("Panel de notificaciones abierto");
            }
        } catch (Exception e) {
            logger.error("Error abriendo notificaciones: {}", e.getMessage());
            throw new RuntimeException("No se pudo abrir las notificaciones", e);
        }
    }

    /**
     * Verifica si el panel de notificaciones está abierto.
     *
     * @return true si el panel está abierto
     */
    public boolean esPanelNotificacionesAbierto() {
        return esElementoVisible(PANEL_NOTIFICACIONES);
    }

    // ==================== MÉTODOS DE WIDGETS ====================

    /**
     * Obtiene el valor mostrado en un widget específico.
     *
     * @param tipoWidget tipo de widget (productos, usuarios, ventas, actividad)
     * @return valor del widget
     */
    public String obtenerValorWidget(String tipoWidget) {
        By localizadorWidget = switch (tipoWidget.toLowerCase()) {
            case "productos" -> WIDGET_PRODUCTOS;
            case "usuarios" -> WIDGET_USUARIOS;
            case "ventas" -> WIDGET_VENTAS;
            case "actividad" -> WIDGET_ACTIVIDAD;
            default -> throw new IllegalArgumentException("Tipo de widget no reconocido: " + tipoWidget);
        };

        try {
            if (esElementoVisible(localizadorWidget)) {
                return obtenerTexto(localizadorWidget);
            }
        } catch (Exception e) {
            logger.debug("No se pudo obtener valor del widget '{}': {}", tipoWidget, e.getMessage());
        }

        return "";
    }

    /**
     * Verifica si un widget específico está visible.
     *
     * @param tipoWidget tipo de widget a verificar
     * @return true si el widget está visible
     */
    public boolean esWidgetVisible(String tipoWidget) {
        By localizadorWidget = switch (tipoWidget.toLowerCase()) {
            case "productos" -> WIDGET_PRODUCTOS;
            case "usuarios" -> WIDGET_USUARIOS;
            case "ventas" -> WIDGET_VENTAS;
            case "actividad" -> WIDGET_ACTIVIDAD;
            default -> throw new IllegalArgumentException("Tipo de widget no reconocido: " + tipoWidget);
        };

        return esElementoVisible(localizadorWidget);
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Verifica si el usuario tiene permisos de administrador.
     *
     * @return true si se muestran opciones de administrador
     */
    public boolean tienePermisosAdministrador() {
        try {
            // Verificar si están visibles opciones típicas de administrador
            boolean gestionUsuarios = esElementoVisible(BOTON_GESTIONAR_USUARIOS) || esElementoVisible(ITEM_MENU_USUARIOS);
            boolean configuracion = esElementoVisible(ITEM_MENU_CONFIGURACION);
            boolean reportes = esElementoVisible(BOTON_VER_REPORTES) || esElementoVisible(ITEM_MENU_REPORTES);

            return gestionUsuarios || configuracion || reportes;

        } catch (Exception e) {
            logger.debug("Error verificando permisos de administrador: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza el dashboard refrescando la página.
     */
    public void actualizarDashboard() {
        registrarAccion("Actualizando dashboard");

        try {
            actualizarPagina();
            esperarCargaPagina();
            logger.debug("Dashboard actualizado");
        } catch (Exception e) {
            logger.error("Error actualizando dashboard: {}", e.getMessage());
        }
    }

    /**
     * Obtiene información de diagnóstico del dashboard.
     *
     * @return string con información de diagnóstico
     */
    public String obtenerInformacionDiagnostico() {
        StringBuilder diagnostico = new StringBuilder();

        try {
            diagnostico.append("=== DIAGNÓSTICO PÁGINA DASHBOARD ===\n");
            diagnostico.append("URL actual: ").append(driver.getCurrentUrl()).append("\n");
            diagnostico.append("Título: ").append(obtenerTituloPagina()).append("\n");
            diagnostico.append("Barra navegación visible: ").append(esElementoVisible(BARRA_NAVEGACION)).append("\n");
            diagnostico.append("Contenido principal visible: ").append(esElementoVisible(CONTENIDO_PRINCIPAL)).append("\n");
            diagnostico.append("Mensaje bienvenida visible: ").append(esMensajeBienvenidaVisible()).append("\n");
            diagnostico.append("Nombre usuario visible: ").append(esNombreUsuarioVisible()).append("\n");
            diagnostico.append("Menú lateral visible: ").append(esMenuLateralVisible()).append("\n");
            diagnostico.append("Widgets visibles: ").append(sonWidgetsVisibles()).append("\n");
            diagnostico.append("Notificaciones visibles: ").append(sonNotificacionesVisibles()).append("\n");
            diagnostico.append("Permisos administrador: ").append(tienePermisosAdministrador()).append("\n");

            if (esNombreUsuarioVisible()) {
                diagnostico.append("Nombre usuario: ").append(obtenerNombreUsuario()).append("\n");
            }

            if (esMensajeBienvenidaVisible()) {
                diagnostico.append("Mensaje bienvenida: ").append(obtenerMensajeBienvenida()).append("\n");
            }

            if (sonNotificacionesVisibles()) {
                diagnostico.append("Número notificaciones: ").append(obtenerNumeroNotificaciones()).append("\n");
            }

            diagnostico.append("=== FIN DIAGNÓSTICO ===");

        } catch (Exception e) {
            diagnostico.append("Error generando diagnóstico: ").append(e.getMessage());
        }

        return diagnostico.toString();
    }

    /**
     * Espera a que el dashboard se cargue completamente con un timeout específico.
     *
     * @param timeoutSegundos timeout en segundos
     * @return true si se cargó correctamente
     */
    public boolean esperarCargaCompleta(int timeoutSegundos) {
        try {
            // Esperar elementos críticos del dashboard
            esperarCondicion(driver -> estaPaginaCargada(), timeoutSegundos);

            // Esperar que los widgets se carguen
            esperarCondicion(driver -> sonWidgetsVisibles() || esElementoVisible(CONTENIDO_PRINCIPAL), timeoutSegundos);

            logger.debug("Dashboard cargado completamente en {} segundos", timeoutSegundos);
            return true;

        } catch (Exception e) {
            logger.error("Timeout esperando carga completa del dashboard: {}", e.getMessage());
            return false;
        }
    }
}