package com.qa.automatizacion.paginas;

import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import com.qa.automatizacion.configuracion.PropiedadesPrueba;
import com.qa.automatizacion.modelo.ProductoCrud;
import com.qa.automatizacion.utilidades.AccionesComunes;
import com.qa.automatizacion.utilidades.HelperTrazabilidad;
import com.qa.automatizacion.utilidades.ManejadorEsperas;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Page Object para la página de gestión de productos.
 *
 * Principios aplicados:
 * - Single Responsibility: Maneja únicamente las interacciones con la página de productos
 * - Open/Closed: Extensible para nuevas funcionalidades sin modificar código existente
 * - Liskov Substitution: Implementa correctamente PaginaBase
 * - Interface Segregation: Métodos específicos para cada funcionalidad
 * - Dependency Inversion: Depende de abstracciones (interfaces) no de implementaciones
 *
 * Patrones implementados:
 * - Page Object Pattern: Encapsula elementos y acciones de la página
 * - Builder Pattern: Para construcción de criterios de búsqueda
 * - Strategy Pattern: Diferentes estrategias de filtrado y ordenamiento
 */
public class PaginaProductos extends PaginaBase {

    private static final Logger logger = LoggerFactory.getLogger(PaginaProductos.class);

    // ==================== LOCALIZADORES DE ELEMENTOS ====================

    // Elementos principales de la página
    @FindBy(id = "productos-titulo")
    private WebElement titulopagina;

    @FindBy(id = "btn-nuevo-producto")
    private WebElement botonNuevoProducto;

    @FindBy(id = "tabla-productos")
    private WebElement tablaProductos;

    @FindBy(css = "#tabla-productos tbody tr")
    private List<WebElement> filasProductos;

    // Elementos de búsqueda y filtros
    @FindBy(id = "campo-busqueda")
    private WebElement campoBusqueda;

    @FindBy(id = "btn-buscar")
    private WebElement botonBuscar;

    @FindBy(id = "btn-limpiar-busqueda")
    private WebElement botonLimpiarBusqueda;

    @FindBy(id = "filtro-categoria")
    private WebElement filtroCategoria;

    @FindBy(id = "filtro-estado")
    private WebElement filtroEstado;

    @FindBy(id = "filtro-precio-min")
    private WebElement filtroPrecioMinimo;

    @FindBy(id = "filtro-precio-max")
    private WebElement filtroPrecioMaximo;

    @FindBy(id = "btn-aplicar-filtros")
    private WebElement botonAplicarFiltros;

    @FindBy(id = "btn-limpiar-filtros")
    private WebElement botonLimpiarFiltros;

    // Elementos de ordenamiento
    @FindBy(id = "orden-columna")
    private WebElement selectorOrdenColumna;

    @FindBy(id = "orden-direccion")
    private WebElement selectorOrdenDireccion;

    // Elementos de paginación
    @FindBy(css = ".paginacion")
    private WebElement contenedorPaginacion;

    @FindBy(css = ".paginacion .pagina-anterior")
    private WebElement botonPaginaAnterior;

    @FindBy(css = ".paginacion .pagina-siguiente")
    private WebElement botonPaginaSiguiente;

    @FindBy(css = ".paginacion .numero-pagina")
    private List<WebElement> numerosPagina;

    @FindBy(css = ".info-paginacion")
    private WebElement infoPaginacion;

    // Elementos de acciones masivas
    @FindBy(id = "checkbox-seleccionar-todos")
    private WebElement checkboxSeleccionarTodos;

    @FindBy(id = "btn-eliminar-seleccionados")
    private WebElement botonEliminarSeleccionados;

    @FindBy(id = "btn-exportar-seleccionados")
    private WebElement botonExportarSeleccionados;

    // Elementos de modal/formulario
    @FindBy(id = "modal-producto")
    private WebElement modalProducto;

    @FindBy(id = "form-producto")
    private WebElement formularioProducto;

    @FindBy(id = "campo-nombre")
    private WebElement campoNombre;

    @FindBy(id = "campo-descripcion")
    private WebElement campoDescripcion;

    @FindBy(id = "campo-precio")
    private WebElement campoPrecio;

    @FindBy(id = "campo-categoria")
    private WebElement campoCategoria;

    @FindBy(id = "campo-sku")
    private WebElement campoSku;

    @FindBy(id = "campo-stock")
    private WebElement campoStock;

    @FindBy(id = "btn-guardar-producto")
    private WebElement botonGuardarProducto;

    @FindBy(id = "btn-cancelar-producto")
    private WebElement botonCancelarProducto;

    // Elementos de mensajes
    @FindBy(css = ".mensaje-exito")
    private WebElement mensajeExito;

    @FindBy(css = ".mensaje-error")
    private WebElement mensajeError;

    @FindBy(css = ".mensaje-advertencia")
    private WebElement mensajeAdvertencia;

    @FindBy(css = ".mensaje-info")
    private WebElement mensajeInfo;

    // Elementos de estado de carga
    @FindBy(css = ".spinner-carga")
    private WebElement spinnerCarga;

    @FindBy(css = ".overlay-carga")
    private WebElement overlayCarga;

    // ==================== CONSTRUCTOR ====================

    /**
     * Constructor que inicializa la página de productos.
     *
     * @param driver WebDriver para interactuar con la página
     * @param propiedades Propiedades de configuración
     * @param trazabilidad Helper para trazabilidad
     */
    public PaginaProductos(WebDriver driver, PropiedadesPrueba propiedades, HelperTrazabilidad trazabilidad) {
        super(driver, propiedades, trazabilidad);
        PageFactory.initElements(driver, this);
        this.accionesComunes = new AccionesComunes(driver);
        this.manejadorEsperas = new ManejadorEsperas(driver);

        logger.info("Página de productos inicializada");
    }

    // ==================== MÉTODOS PRINCIPALES ====================

    @Override
    public boolean estaPaginaCargada() {
        try {
            return manejadorEsperas.esperarElementoVisible(titulopagina, 10) &&
                    manejadorEsperas.esperarElementoVisible(tablaProductos, 5) &&
                    obtenerTituloPagina().toLowerCase().contains("productos");
        } catch (Exception e) {
            logger.error("Error verificando si página está cargada: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String obtenerUrlEsperada() {
        return propiedades.obtenerUrlBase() + "/productos";
    }

    @Override
    public void esperarCargaCompleta() {
        logger.info("Esperando carga completa de página de productos");

        // Esperar que desaparezca el spinner de carga
        if (accionesComunes.esElementoVisible(spinnerCarga)) {
            manejadorEsperas.esperarElementoInvisible(spinnerCarga, 30);
        }

        // Esperar elementos principales
        manejadorEsperas.esperarElementoVisible(tablaProductos, 15);
        manejadorEsperas.esperarElementoClicable(botonNuevoProducto, 10);

        // Esperar que se carguen los datos de la tabla
        esperarCargaTablaProductos();

        logger.info("Carga completa de página de productos terminada");
    }

    // ==================== MÉTODOS DE NAVEGACIÓN ====================

    /**
     * Navega a la página de productos.
     */
    public void navegarAPaginaProductos() {
        registrarAccion("Navegando a página de productos");

        String url = obtenerUrlEsperada();
        ConfiguradorNavegador.navegarA(url);

        esperarCargaCompleta();

        if (!estaPaginaCargada()) {
            throw new RuntimeException("No se pudo cargar la página de productos");
        }

        logger.info("Navegación a página de productos completada");
    }

    // ==================== MÉTODOS DE BÚSQUEDA Y FILTROS ====================

    /**
     * Realiza una búsqueda de productos por texto.
     *
     * @param textoBusqueda Texto a buscar
     * @return Lista de productos encontrados
     */
    public List<ProductoCrud> buscarProductos(String textoBusqueda) {
        registrarAccion("Buscando productos con texto: " + textoBusqueda);

        try {
            accionesComunes.limpiarYEscribir(campoBusqueda, textoBusqueda);
            accionesComunes.hacerClickSeguro(botonBuscar);

            esperarCargaTablaProductos();

            List<ProductoCrud> resultados = obtenerProductosTabla();
            logger.info("Búsqueda completada. {} productos encontrados", resultados.size());

            return resultados;

        } catch (Exception e) {
            logger.error("Error en búsqueda de productos: {}", e.getMessage());
            throw new RuntimeException("Error realizando búsqueda de productos", e);
        }
    }

    /**
     * Busca productos usando Enter en el campo de búsqueda.
     *
     * @param textoBusqueda Texto a buscar
     * @return Lista de productos encontrados
     */
    public List<ProductoCrud> buscarProductosConEnter(String textoBusqueda) {
        registrarAccion("Buscando productos con Enter: " + textoBusqueda);

        try {
            accionesComunes.limpiarYEscribir(campoBusqueda, textoBusqueda);
            campoBusqueda.sendKeys(Keys.ENTER);

            esperarCargaTablaProductos();

            List<ProductoCrud> resultados = obtenerProductosTabla();
            logger.info("Búsqueda con Enter completada. {} productos encontrados", resultados.size());

            return resultados;

        } catch (Exception e) {
            logger.error("Error en búsqueda con Enter: {}", e.getMessage());
            throw new RuntimeException("Error realizando búsqueda con Enter", e);
        }
    }

    /**
     * Aplica filtros de búsqueda avanzada.
     *
     * @param criterios Criterios de búsqueda
     * @return Lista de productos filtrados
     */
    public List<ProductoCrud> aplicarFiltros(CriteriosBusqueda criterios) {
        registrarAccion("Aplicando filtros de búsqueda");

        try {
            if (criterios.getCategoria() != null) {
                accionesComunes.seleccionarOpcion(filtroCategoria, criterios.getCategoria());
            }

            if (criterios.getEstado() != null) {
                accionesComunes.seleccionarOpcion(filtroEstado, criterios.getEstado());
            }

            if (criterios.getPrecioMinimo() != null) {
                accionesComunes.limpiarYEscribir(filtroPrecioMinimo, criterios.getPrecioMinimo().toString());
            }

            if (criterios.getPrecioMaximo() != null) {
                accionesComunes.limpiarYEscribir(filtroPrecioMaximo, criterios.getPrecioMaximo().toString());
            }

            accionesComunes.hacerClickSeguro(botonAplicarFiltros);
            esperarCargaTablaProductos();

            List<ProductoCrud> resultados = obtenerProductosTabla();
            logger.info("Filtros aplicados. {} productos encontrados", resultados.size());

            return resultados;

        } catch (Exception e) {
            logger.error("Error aplicando filtros: {}", e.getMessage());
            throw new RuntimeException("Error aplicando filtros de búsqueda", e);
        }
    }

    /**
     * Limpia todos los filtros aplicados.
     */
    public void limpiarFiltros() {
        registrarAccion("Limpiando filtros de búsqueda");

        try {
            if (accionesComunes.esElementoVisible(botonLimpiarFiltros)) {
                accionesComunes.hacerClickSeguro(botonLimpiarFiltros);
                esperarCargaTablaProductos();
                logger.info("Filtros limpiados correctamente");
            }

        } catch (Exception e) {
            logger.error("Error limpiando filtros: {}", e.getMessage());
            throw new RuntimeException("Error limpiando filtros", e);
        }
    }

    /**
     * Limpia el campo de búsqueda.
     */
    public void limpiarBusqueda() {
        registrarAccion("Limpiando búsqueda");

        try {
            if (accionesComunes.esElementoVisible(botonLimpiarBusqueda)) {
                accionesComunes.hacerClickSeguro(botonLimpiarBusqueda);
                esperarCargaTablaProductos();
                logger.info("Búsqueda limpiada correctamente");
            }

        } catch (Exception e) {
            logger.error("Error limpiando búsqueda: {}", e.getMessage());
            throw new RuntimeException("Error limpiando búsqueda", e);
        }
    }

    // ==================== MÉTODOS DE GESTIÓN DE PRODUCTOS ====================

    /**
     * Abre el modal para crear un nuevo producto.
     */
    public void abrirModalNuevoProducto() {
        registrarAccion("Abriendo modal para nuevo producto");

        try {
            accionesComunes.hacerClickSeguro(botonNuevoProducto);
            manejadorEsperas.esperarElementoVisible(modalProducto, 10);

            if (!esModalProductoVisible()) {
                throw new RuntimeException("No se pudo abrir el modal de producto");
            }

            logger.info("Modal de nuevo producto abierto correctamente");

        } catch (Exception e) {
            logger.error("Error abriendo modal de nuevo producto: {}", e.getMessage());
            throw new RuntimeException("Error abriendo modal de nuevo producto", e);
        }
    }

    /**
     * Crea un nuevo producto.
     *
     * @param producto Datos del producto a crear
     * @return true si la creación fue exitosa
     */
    public boolean crearProducto(ProductoCrud producto) {
        registrarAccion("Creando producto: " + producto.getNombre());

        try {
            abrirModalNuevoProducto();
            llenarFormularioProducto(producto);

            accionesComunes.hacerClickSeguro(botonGuardarProducto);

            // Esperar confirmación
            boolean exito = esperarMensajeExito("Producto creado exitosamente");

            if (exito) {
                esperarCargaTablaProductos();
                logger.info("Producto creado exitosamente: {}", producto.getNombre());
            }

            return exito;

        } catch (Exception e) {
            logger.error("Error creando producto '{}': {}", producto.getNombre(), e.getMessage());
            return false;
        }
    }

    /**
     * Edita un producto existente.
     *
     * @param nombreProducto Nombre del producto a editar
     * @param productocualizado Datos actualizados
     * @return true si la edición fue exitosa
     */
    public boolean editarProducto(String nombreProducto, ProductoCrud productoActualizado) {
        registrarAccion("Editando producto: " + nombreProducto);

        try {
            if (!seleccionarProductoPorNombre(nombreProducto)) {
                logger.error("No se pudo encontrar el producto: {}", nombreProducto);
                return false;
            }

            WebElement botonEditar = obtenerBotonAccion(nombreProducto, "editar");
            accionesComunes.hacerClickSeguro(botonEditar);

            manejadorEsperas.esperarElementoVisible(modalProducto, 10);
            llenarFormularioProducto(productoActualizado);

            accionesComunes.hacerClickSeguro(botonGuardarProducto);

            boolean exito = esperarMensajeExito("Producto actualizado exitosamente");

            if (exito) {
                esperarCargaTablaProductos();
                logger.info("Producto editado exitosamente: {} -> {}",
                        nombreProducto, productoActualizado.getNombre());
            }

            return exito;

        } catch (Exception e) {
            logger.error("Error editando producto '{}': {}", nombreProducto, e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un producto.
     *
     * @param nombreProducto Nombre del producto a eliminar
     * @return true si la eliminación fue exitosa
     */
    public boolean eliminarProducto(String nombreProducto) {
        registrarAccion("Eliminando producto: " + nombreProducto);

        try {
            if (!seleccionarProductoPorNombre(nombreProducto)) {
                logger.error("No se pudo encontrar el producto: {}", nombreProducto);
                return false;
            }

            WebElement botonEliminar = obtenerBotonAccion(nombreProducto, "eliminar");
            accionesComunes.hacerClickSeguro(botonEliminar);

            // Confirmar eliminación en modal de confirmación
            confirmarEliminacion();

            boolean exito = esperarMensajeExito("Producto eliminado exitosamente");

            if (exito) {
                esperarCargaTablaProductos();
                logger.info("Producto eliminado exitosamente: {}", nombreProducto);
            }

            return exito;

        } catch (Exception e) {
            logger.error("Error eliminando producto '{}': {}", nombreProducto, e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE FORMULARIO ====================

    /**
     * Llena el formulario de producto con los datos proporcionados.
     *
     * @param producto Datos del producto
     */
    private void llenarFormularioProducto(ProductoCrud producto) {
        logger.debug("Llenando formulario de producto");

        if (producto.getNombre() != null) {
            accionesComunes.limpiarYEscribir(campoNombre, producto.getNombre());
        }

        if (producto.getDescripcion() != null) {
            accionesComunes.limpiarYEscribir(campoDescripcion, producto.getDescripcion());
        }

        if (producto.getPrecio() != null) {
            accionesComunes.limpiarYEscribir(campoPrecio, producto.getPrecio().toString());
        }

        if (producto.getCategoria() != null) {
            accionesComunes.seleccionarOpcion(campoCategoria, producto.getCategoria());
        }

        if (producto.getCodigoSku() != null) {
            accionesComunes.limpiarYEscribir(campoSku, producto.getCodigoSku());
        }

        if (producto.getStock() != null) {
            accionesComunes.limpiarYEscribir(campoStock, producto.getStock().toString());
        }

        logger.debug("Formulario de producto llenado");
    }

    /**
     * Cancela la edición/creación del producto.
     */
    public void cancelarEdicionProducto() {
        registrarAccion("Cancelando edición de producto");

        try {
            if (esModalProductoVisible()) {
                accionesComunes.hacerClickSeguro(botonCancelarProducto);
                manejadorEsperas.esperarElementoInvisible(modalProducto, 5);
                logger.info("Edición de producto cancelada");
            }

        } catch (Exception e) {
            logger.error("Error cancelando edición: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE TABLA ====================

    /**
     * Obtiene todos los productos mostrados en la tabla actual.
     *
     * @return Lista de productos de la tabla
     */
    public List<ProductoCrud> obtenerProductosTabla() {
        List<ProductoCrud> productos = new ArrayList<>();

        try {
            manejadorEsperas.esperarElementoVisible(tablaProductos, 10);

            for (WebElement fila : filasProductos) {
                ProductoCrud producto = extraerProductoDeFila(fila);
                if (producto != null) {
                    productos.add(producto);
                }
            }

            logger.debug("{} productos extraídos de la tabla", productos.size());

        } catch (Exception e) {
            logger.error("Error obteniendo productos de la tabla: {}", e.getMessage());
        }

        return productos;
    }

    /**
     * Extrae los datos de un producto de una fila de la tabla.
     *
     * @param fila Elemento WebElement de la fila
     * @return ProductoCrud con los datos extraídos
     */
    private ProductoCrud extraerProductoDeFila(WebElement fila) {
        try {
            List<WebElement> celdas = fila.findElements(By.tagName("td"));

            if (celdas.size() < 6) {
                logger.warn("Fila con menos columnas de las esperadas");
                return null;
            }

            return ProductoCrud.builder()
                    .nombre(celdas.get(1).getText().trim())
                    .descripcion(celdas.get(2).getText().trim())
                    .precio(new BigDecimal(celdas.get(3).getText().replaceAll("[^0-9.]", "")))
                    .categoria(celdas.get(4).getText().trim())
                    .stock(Integer.parseInt(celdas.get(5).getText().trim()))
                    .build();

        } catch (Exception e) {
            logger.error("Error extrayendo producto de fila: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Cuenta el número total de productos mostrados.
     *
     * @return Número de productos en la tabla
     */
    public int contarProductos() {
        try {
            manejadorEsperas.esperarElementoVisible(tablaProductos, 10);
            int cantidad = filasProductos.size();
            logger.debug("Contados {} productos en la tabla", cantidad);
            return cantidad;

        } catch (Exception e) {
            logger.error("Error contando productos: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Verifica si un producto específico está en la tabla.
     *
     * @param nombreProducto Nombre del producto a buscar
     * @return true si el producto está presente
     */
    public boolean estaProductoEnTabla(String nombreProducto) {
        try {
            for (WebElement fila : filasProductos) {
                List<WebElement> celdas = fila.findElements(By.tagName("td"));
                if (celdas.size() > 1 && celdas.get(1).getText().trim().equals(nombreProducto)) {
                    logger.debug("Producto '{}' encontrado en tabla", nombreProducto);
                    return true;
                }
            }

            logger.debug("Producto '{}' no encontrado en tabla", nombreProducto);
            return false;

        } catch (Exception e) {
            logger.error("Error verificando producto en tabla: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE SELECCIÓN ====================

    /**
     * Selecciona un producto por su nombre.
     *
     * @param nombreProducto Nombre del producto a seleccionar
     * @return true si se pudo seleccionar
     */
    private boolean seleccionarProductoPorNombre(String nombreProducto) {
        try {
            for (WebElement fila : filasProductos) {
                List<WebElement> celdas = fila.findElements(By.tagName("td"));
                if (celdas.size() > 1 && celdas.get(1).getText().trim().equals(nombreProducto)) {
                    WebElement checkbox = celdas.get(0).findElement(By.cssSelector("input[type='checkbox']"));
                    if (!checkbox.isSelected()) {
                        accionesComunes.hacerClickSeguro(checkbox);
                    }
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            logger.error("Error seleccionando producto '{}': {}", nombreProducto, e.getMessage());
            return false;
        }
    }

    /**
     * Selecciona todos los productos de la página actual.
     */
    public void seleccionarTodosLosProductos() {
        registrarAccion("Seleccionando todos los productos");

        try {
            if (!checkboxSeleccionarTodos.isSelected()) {
                accionesComunes.hacerClickSeguro(checkboxSeleccionarTodos);
            }
            logger.info("Todos los productos seleccionados");

        } catch (Exception e) {
            logger.error("Error seleccionando todos los productos: {}", e.getMessage());
        }
    }

    /**
     * Deselecciona todos los productos.
     */
    public void deseleccionarTodosLosProductos() {
        registrarAccion("Deseleccionando todos los productos");

        try {
            if (checkboxSeleccionarTodos.isSelected()) {
                accionesComunes.hacerClickSeguro(checkboxSeleccionarTodos);
            }
            logger.info("Todos los productos deseleccionados");

        } catch (Exception e) {
            logger.error("Error deseleccionando productos: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE PAGINACIÓN ====================

    /**
     * Navega a la página siguiente.
     *
     * @return true si se pudo navegar
     */
    public boolean irAPaginaSiguiente() {
        registrarAccion("Navegando a página siguiente");

        try {
            if (accionesComunes.esElementoClicable(botonPaginaSiguiente)) {
                accionesComunes.hacerClickSeguro(botonPaginaSiguiente);
                esperarCargaTablaProductos();
                logger.info("Navegación a página siguiente completada");
                return true;
            }

            logger.info("No hay página siguiente disponible");
            return false;

        } catch (Exception e) {
            logger.error("Error navegando a página siguiente: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Navega a la página anterior.
     *
     * @return true si se pudo navegar
     */
    public boolean irAPaginaAnterior() {
        registrarAccion("Navegando a página anterior");

        try {
            if (accionesComunes.esElementoClicable(botonPaginaAnterior)) {
                accionesComunes.hacerClickSeguro(botonPaginaAnterior);
                esperarCargaTablaProductos();
                logger.info("Navegación a página anterior completada");
                return true;
            }

            logger.info("No hay página anterior disponible");
            return false;

        } catch (Exception e) {
            logger.error("Error navegando a página anterior: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Navega a una página específica.
     *
     * @param numeroPagina Número de página (1-based)
     * @return true si se pudo navegar
     */
    public boolean irAPagina(int numeroPagina) {
        registrarAccion("Navegando a página: " + numeroPagina);

        try {
            for (WebElement numeroPag : numerosPagina) {
                if (numeroPag.getText().equals(String.valueOf(numeroPagina))) {
                    accionesComunes.hacerClickSeguro(numeroPag);
                    esperarCargaTablaProductos();
                    logger.info("Navegación a página {} completada", numeroPagina);
                    return true;
                }
            }

            logger.warn("Página {} no encontrada", numeroPagina);
            return false;

        } catch (Exception e) {
            logger.error("Error navegando a página {}: {}", numeroPagina, e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE ORDENAMIENTO ====================

    /**
     * Ordena la tabla por una columna específica.
     *
     * @param columna Nombre de la columna
     * @param direccion Dirección del orden (ASC/DESC)
     */
    public void ordenarPor(String columna, String direccion) {
        registrarAccion("Ordenando por columna: " + columna + " " + direccion);

        try {
            accionesComunes.seleccionarOpcion(selectorOrdenColumna, columna);
            accionesComunes.seleccionarOpcion(selectorOrdenDireccion, direccion);

            esperarCargaTablaProductos();
            logger.info("Tabla ordenada por {} {}", columna, direccion);

        } catch (Exception e) {
            logger.error("Error ordenando tabla: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE VALIDACIÓN Y ESTADO ====================

    /**
     * Verifica si el modal de producto está visible.
     *
     * @return true si el modal está visible
     */
    public boolean esModalProductoVisible() {
        return accionesComunes.esElementoVisible(modalProducto);
    }

    /**
     * Verifica si hay productos en la tabla.
     *
     * @return true si hay productos
     */
    public boolean hayProductos() {
        try {
            return !filasProductos.isEmpty();
        } catch (Exception e) {
            logger.error("Error verificando si hay productos: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si la tabla está vacía.
     *
     * @return true si no hay productos
     */
    public boolean estaTablaVacia() {
        return !hayProductos();
    }

    /**
     * Obtiene el mensaje de información de paginación.
     *
     * @return Texto de información de paginación
     */
    public String obtenerInfoPaginacion() {
        try {
            if (accionesComunes.esElementoVisible(infoPaginacion)) {
                return infoPaginacion.getText().trim();
            }
            return "";

        } catch (Exception e) {
            logger.error("Error obteniendo info de paginación: {}", e.getMessage());
            return "";
        }
    }

    // ==================== MÉTODOS DE MENSAJES ====================

    /**
     * Verifica si hay un mensaje de éxito visible.
     *
     * @return true si hay mensaje de éxito
     */
    public boolean hayMensajeExito() {
        return accionesComunes.esElementoVisible(mensajeExito);
    }

    /**
     * Obtiene el texto del mensaje de éxito.
     *
     * @return Texto del mensaje de éxito
     */
    public String obtenerMensajeExito() {
        if (hayMensajeExito()) {
            return mensajeExito.getText().trim();
        }
        return "";
    }

    /**
     * Verifica si hay un mensaje de error visible.
     *
     * @return true si hay mensaje de error
     */
    public boolean hayMensajeError() {
        return accionesComunes.esElementoVisible(mensajeError);
    }

    /**
     * Obtiene el texto del mensaje de error.
     *
     * @return Texto del mensaje de error
     */
    public String obtenerMensajeError() {
        if (hayMensajeError()) {
            return mensajeError.getText().trim();
        }
        return "";
    }

    /**
     * Espera que aparezca un mensaje de éxito específico.
     *
     * @param mensajeEsperado Mensaje que se espera
     * @return true si aparece el mensaje esperado
     */
    private boolean esperarMensajeExito(String mensajeEsperado) {
        try {
            manejadorEsperas.esperarElementoVisible(mensajeExito, 10);

            if (hayMensajeExito()) {
                String mensaje = obtenerMensajeExito();
                return mensaje.contains(mensajeEsperado);
            }

            return false;

        } catch (Exception e) {
            logger.error("Error esperando mensaje de éxito: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    /**
     * Espera que se complete la carga de la tabla de productos.
     */
    private void esperarCargaTablaProductos() {
        try {
            // Esperar que desaparezca el overlay de carga si está presente
            if (accionesComunes.esElementoVisible(overlayCarga)) {
                manejadorEsperas.esperarElementoInvisible(overlayCarga, 20);
            }

            // Esperar que la tabla esté presente y visible
            manejadorEsperas.esperarElementoVisible(tablaProductos, 15);

            // Pequeña espera adicional para asegurar que los datos se carguen
            Thread.sleep(500);

        } catch (Exception e) {
            logger.error("Error esperando carga de tabla: {}", e.getMessage());
        }
    }

    /**
     * Obtiene el botón de acción para un producto específico.
     *
     * @param nombreProducto Nombre del producto
     * @param tipoAccion Tipo de acción (editar, eliminar, ver)
     * @return WebElement del botón de acción
     */
    private WebElement obtenerBotonAccion(String nombreProducto, String tipoAccion) {
        for (WebElement fila : filasProductos) {
            List<WebElement> celdas = fila.findElements(By.tagName("td"));
            if (celdas.size() > 1 && celdas.get(1).getText().trim().equals(nombreProducto)) {
                // La última celda contiene los botones de acción
                WebElement celdaAcciones = celdas.get(celdas.size() - 1);
                return celdaAcciones.findElement(By.cssSelector("button[data-action='" + tipoAccion + "']"));
            }
        }

        throw new RuntimeException("No se encontró el botón " + tipoAccion + " para el producto: " + nombreProducto);
    }

    /**
     * Confirma la eliminación en el modal de confirmación.
     */
    private void confirmarEliminacion() {
        try {
            WebElement modalConfirmacion = driver.findElement(By.id("modal-confirmacion"));
            manejadorEsperas.esperarElementoVisible(modalConfirmacion, 5);

            WebElement botonConfirmar = modalConfirmacion.findElement(By.cssSelector("button[data-action='confirmar']"));
            accionesComunes.hacerClickSeguro(botonConfirmar);

            logger.debug("Eliminación confirmada");

        } catch (Exception e) {
            logger.error("Error confirmando eliminación: {}", e.getMessage());
            throw new RuntimeException("Error confirmando eliminación", e);
        }
    }

    // ==================== MÉTODOS DE ACCIONES MASIVAS ====================

    /**
     * Elimina todos los productos seleccionados.
     *
     * @return true si la eliminación fue exitosa
     */
    public boolean eliminarProductosSeleccionados() {
        registrarAccion("Eliminando productos seleccionados");

        try {
            if (!accionesComunes.esElementoClicable(botonEliminarSeleccionados)) {
                logger.warn("No hay productos seleccionados para eliminar");
                return false;
            }

            accionesComunes.hacerClickSeguro(botonEliminarSeleccionados);
            confirmarEliminacion();

            boolean exito = esperarMensajeExito("Productos eliminados exitosamente");

            if (exito) {
                esperarCargaTablaProductos();
                logger.info("Productos seleccionados eliminados exitosamente");
            }

            return exito;

        } catch (Exception e) {
            logger.error("Error eliminando productos seleccionados: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Exporta los productos seleccionados.
     *
     * @return true si la exportación se inició correctamente
     */
    public boolean exportarProductosSeleccionados() {
        registrarAccion("Exportando productos seleccionados");

        try {
            if (!accionesComunes.esElementoClicable(botonExportarSeleccionados)) {
                logger.warn("No hay productos seleccionados para exportar");
                return false;
            }

            accionesComunes.hacerClickSeguro(botonExportarSeleccionados);

            // Esperar mensaje de confirmación de exportación
            Thread.sleep(2000);

            logger.info("Exportación de productos iniciada");
            return true;

        } catch (Exception e) {
            logger.error("Error exportando productos seleccionados: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE INFORMACIÓN Y DIAGNÓSTICO ====================

    /**
     * Obtiene información detallada del estado actual de la página.
     *
     * @return String con información de diagnóstico
     */
    public String obtenerInformacionDiagnostico() {
        StringBuilder diagnostico = new StringBuilder();

        try {
            diagnostico.append("=== DIAGNÓSTICO PÁGINA PRODUCTOS ===\n");
            diagnostico.append("URL actual: ").append(driver.getCurrentUrl()).append("\n");
            diagnostico.append("Título: ").append(obtenerTituloPagina()).append("\n");
            diagnostico.append("Página cargada: ").append(estaPaginaCargada()).append("\n");
            diagnostico.append("Tabla visible: ").append(accionesComunes.esElementoVisible(tablaProductos)).append("\n");
            diagnostico.append("Número de productos: ").append(contarProductos()).append("\n");
            diagnostico.append("Modal abierto: ").append(esModalProductoVisible()).append("\n");
            diagnostico.append("Hay mensaje éxito: ").append(hayMensajeExito()).append("\n");
            diagnostico.append("Hay mensaje error: ").append(hayMensajeError()).append("\n");
            diagnostico.append("Botón nuevo producto visible: ").append(accionesComunes.esElementoVisible(botonNuevoProducto)).append("\n");
            diagnostico.append("Campo búsqueda visible: ").append(accionesComunes.esElementoVisible(campoBusqueda)).append("\n");
            diagnostico.append("Info paginación: ").append(obtenerInfoPaginacion()).append("\n");

            if (hayMensajeExito()) {
                diagnostico.append("Mensaje éxito: ").append(obtenerMensajeExito()).append("\n");
            }

            if (hayMensajeError()) {
                diagnostico.append("Mensaje error: ").append(obtenerMensajeError()).append("\n");
            }

            diagnostico.append("=== FIN DIAGNÓSTICO ===");

        } catch (Exception e) {
            diagnostico.append("Error generando diagnóstico: ").append(e.getMessage());
        }

        return diagnostico.toString();
    }

    // ==================== CLASE INTERNA PARA CRITERIOS DE BÚSQUEDA ====================

    /**
     * Clase Builder para criterios de búsqueda avanzada.
     * Implementa el patrón Builder para construcción flexible de criterios.
     */
    public static class CriteriosBusqueda {
        private String categoria;
        private String estado;
        private BigDecimal precioMinimo;
        private BigDecimal precioMaximo;
        private String textoBusqueda;
        private String ordenarPor;
        private String direccionOrden;

        private CriteriosBusqueda() {}

        public static CriteriosBusqueda builder() {
            return new CriteriosBusqueda();
        }

        public CriteriosBusqueda categoria(String categoria) {
            this.categoria = categoria;
            return this;
        }

        public CriteriosBusqueda estado(String estado) {
            this.estado = estado;
            return this;
        }

        public CriteriosBusqueda precioMinimo(BigDecimal precioMinimo) {
            this.precioMinimo = precioMinimo;
            return this;
        }

        public CriteriosBusqueda precioMinimo(double precioMinimo) {
            this.precioMinimo = BigDecimal.valueOf(precioMinimo);
            return this;
        }

        public CriteriosBusqueda precioMaximo(BigDecimal precioMaximo) {
            this.precioMaximo = precioMaximo;
            return this;
        }

        public CriteriosBusqueda precioMaximo(double precioMaximo) {
            this.precioMaximo = BigDecimal.valueOf(precioMaximo);
            return this;
        }

        public CriteriosBusqueda textoBusqueda(String textoBusqueda) {
            this.textoBusqueda = textoBusqueda;
            return this;
        }

        public CriteriosBusqueda ordenarPor(String ordenarPor) {
            this.ordenarPor = ordenarPor;
            return this;
        }

        public CriteriosBusqueda direccionOrden(String direccionOrden) {
            this.direccionOrden = direccionOrden;
            return this;
        }

        // Getters
        public String getCategoria() { return categoria; }
        public String getEstado() { return estado; }
        public BigDecimal getPrecioMinimo() { return precioMinimo; }
        public BigDecimal getPrecioMaximo() { return precioMaximo; }
        public String getTextoBusqueda() { return textoBusqueda; }
        public String getOrdenarPor() { return ordenarPor; }
        public String getDireccionOrden() { return direccionOrden; }

        @Override
        public String toString() {
            return String.format("CriteriosBusqueda{categoria='%s', estado='%s', precioMin=%s, precioMax=%s, texto='%s', orden='%s %s'}",
                    categoria, estado, precioMinimo, precioMaximo, textoBusqueda, ordenarPor, direccionOrden);
        }
    }
}