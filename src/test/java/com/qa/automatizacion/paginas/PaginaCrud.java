package com.qa.automatizacion.paginas;

import com.qa.automatizacion.utilidades.Utileria;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Page Object para la gestión CRUD de productos del sistema.
 * Encapsula todas las operaciones relacionadas con la gestión de productos.
 *
 * Principios aplicados:
 * - Page Object Pattern: Separa la lógica de UI de los tests
 * - Encapsulación: Oculta los detalles de implementación de Selenium
 * - Single Responsibility: Se enfoca únicamente en la gestión de productos
 * - Integration with Utileria: Todas las operaciones pasan por la facade central
 * - CRUD Operations: Create, Read, Update, Delete completamente implementadas
 */
public class PaginaCrud extends PaginaBase {

    private static final Logger logger = LoggerFactory.getLogger(PaginaCrud.class);
    private final Utileria utileria;

    // ==================== LOCALIZADORES ====================

    // Elementos principales de la página
    private static final By TITULO_PAGINA = By.cssSelector("h1, .productos-title, [data-testid='productos-title']");
    private static final By CONTENEDOR_PRODUCTOS = By.cssSelector(".productos-container, .crud-container");

    // Botones principales de acción
    private static final By BOTON_NUEVO_PRODUCTO = By.cssSelector("#btn-nuevo-producto, .btn-nuevo, [data-testid='btn-nuevo-producto']");
    private static final By BOTON_EXPORTAR = By.cssSelector("#btn-exportar, .btn-export, [data-testid='btn-exportar']");
    private static final By BOTON_IMPORTAR = By.cssSelector("#btn-importar, .btn-import, [data-testid='btn-importar']");

    // Formulario de producto (crear/editar)
    private static final By MODAL_PRODUCTO = By.cssSelector(".modal-producto, .product-modal, [data-testid='modal-producto']");
    private static final By CAMPO_NOMBRE = By.id("nombre-producto");
    private static final By CAMPO_CODIGO = By.id("codigo-producto");
    private static final By CAMPO_CATEGORIA = By.id("categoria-producto");
    private static final By CAMPO_PRECIO = By.id("precio-producto");
    private static final By CAMPO_STOCK = By.id("stock-producto");
    private static final By CAMPO_DESCRIPCION = By.id("descripcion-producto");

    // Botones del formulario
    private static final By BOTON_GUARDAR = By.cssSelector("#btn-guardar-producto, .btn-save, [data-testid='btn-guardar']");
    private static final By BOTON_CANCELAR = By.cssSelector("#btn-cancelar-producto, .btn-cancel, [data-testid='btn-cancelar']");

    // Búsqueda y filtros
    private static final By CAMPO_BUSQUEDA = By.cssSelector("#buscar-productos, .search-input, [data-testid='buscar-productos']");
    private static final By BOTON_BUSCAR = By.cssSelector("#btn-buscar, .btn-search, [data-testid='btn-buscar']");
    private static final By FILTRO_CATEGORIA = By.cssSelector("#filtro-categoria, .category-filter");
    private static final By FILTRO_PRECIO_MIN = By.id("precio-min");
    private static final By FILTRO_PRECIO_MAX = By.id("precio-max");
    private static final By BOTON_LIMPIAR_FILTROS = By.cssSelector(".btn-clear-filters, [data-testid='limpiar-filtros']");

    // Tabla de productos
    private static final By TABLA_PRODUCTOS = By.cssSelector("#tabla-productos, .products-table, [data-testid='tabla-productos']");
    private static final By FILAS_PRODUCTOS = By.cssSelector(".producto-fila, .product-row, tbody tr");
    private static final By HEADERS_TABLA = By.cssSelector("thead th, .table-header");

    // Acciones por fila de producto
    private static final By BOTON_VER_DETALLE = By.cssSelector(".btn-ver-detalle, [data-action='view']");
    private static final By BOTON_EDITAR = By.cssSelector(".btn-editar, [data-action='edit']");
    private static final By BOTON_ELIMINAR = By.cssSelector(".btn-eliminar, [data-action='delete']");

    // Paginación
    private static final By CONTENEDOR_PAGINACION = By.cssSelector(".paginacion, .pagination");
    private static final By BOTON_PAGINA_ANTERIOR = By.cssSelector(".btn-prev, .pagination-prev");
    private static final By BOTON_PAGINA_SIGUIENTE = By.cssSelector(".btn-next, .pagination-next");
    private static final By NUMERO_PAGINA_ACTUAL = By.cssSelector(".pagina-actual, .current-page");
    private static final By TOTAL_PRODUCTOS = By.cssSelector(".total-productos, .total-count");

    // Mensajes y alertas
    private static final By MENSAJE_EXITO = By.cssSelector(".alert-success, .success-message, [data-testid='success-message']");
    private static final By MENSAJE_ERROR = By.cssSelector(".alert-error, .error-message, [data-testid='error-message']");
    private static final By MENSAJE_CONFIRMACION = By.cssSelector(".confirm-dialog, .confirmation-modal");

    // Indicadores de estado
    private static final By INDICADOR_CARGA = By.cssSelector(".loading, .spinner, [data-testid='loading']");
    private static final By PRODUCTO_AGOTADO = By.cssSelector(".agotado, .out-of-stock");
    private static final By INDICADOR_STOCK_BAJO = By.cssSelector(".stock-bajo, .low-stock");

    // Validaciones y errores por campo
    private static final By ERROR_NOMBRE = By.cssSelector("[data-field='nombre'] .error, #nombre-producto + .error");
    private static final By ERROR_CODIGO = By.cssSelector("[data-field='codigo'] .error, #codigo-producto + .error");
    private static final By ERROR_PRECIO = By.cssSelector("[data-field='precio'] .error, #precio-producto + .error");
    private static final By ERROR_STOCK = By.cssSelector("[data-field='stock'] .error, #stock-producto + .error");

    // ==================== CONSTRUCTOR ====================

    public PaginaCrud() {
        super();
        this.utileria = Utileria.obtenerInstancia();
        logger.debug("PaginaCrud inicializada");
    }

    // ==================== MÉTODOS ABSTRACTOS IMPLEMENTADOS ====================

    /**
     * Implementa el método abstracto de PaginaBase.
     * Retorna la URL esperada para la página de gestión de productos.
     *
     * @return URL esperada de la página de productos
     */
    @Override
    protected String obtenerUrlEsperada() {
        return "/productos";
    }

    // ==================== MÉTODOS PRINCIPALES ====================

    /**
     * Verifica si la página de gestión de productos está completamente cargada.
     *
     * @return true si la página está cargada, false en caso contrario
     */
    @Override
    public boolean esPaginaCargada() {
        try {
            utileria.registrarTrazabilidad("HU-003", "Verificación de carga de página CRUD productos");

            boolean tituloVisible = utileria.esElementoVisible(TITULO_PAGINA);
            boolean tablaVisible = utileria.esElementoVisible(TABLA_PRODUCTOS);
            boolean botonesPresentes = verificarBotonesPrincipalesPresentes();

            boolean paginaCargada = tituloVisible && tablaVisible && botonesPresentes;

            if (paginaCargada) {
                logger.info("Página de gestión de productos cargada correctamente");
                utileria.tomarScreenshot("pagina-crud-productos-cargada");
            } else {
                logger.warn("La página de gestión de productos no está completamente cargada");
                utileria.tomarScreenshot("pagina-crud-productos-error-carga");
            }

            return paginaCargada;

        } catch (Exception e) {
            logger.error("Error al verificar carga de página CRUD: {}", e.getMessage());
            utileria.manejarError("Error verificando página CRUD", e);
            return false;
        }
    }

    // ==================== OPERACIONES CREATE ====================

    /**
     * Abre el modal para crear un nuevo producto.
     */
    public void abrirFormularioNuevoProducto() {
        try {
            logger.info("Abriendo formulario para nuevo producto");
            utileria.registrarTrazabilidad("HU-003", "Apertura de formulario nuevo producto");

            utileria.esperarElementoClickeable(BOTON_NUEVO_PRODUCTO);
            utileria.hacerClick(BOTON_NUEVO_PRODUCTO);

            // Esperar a que aparezca el modal
            utileria.esperarElementoVisible(MODAL_PRODUCTO);
            utileria.tomarScreenshot("modal-nuevo-producto-abierto");

        } catch (Exception e) {
            logger.error("Error al abrir formulario nuevo producto: {}", e.getMessage());
            utileria.manejarError("Error abriendo formulario", e);
            throw e;
        }
    }

    /**
     * Completa el formulario de producto con los datos proporcionados.
     *
     * @param datosProducto mapa con los datos del producto
     */
    public void completarFormularioProducto(Map<String, String> datosProducto) {
        try {
            logger.info("Completando formulario de producto");
            utileria.registrarTrazabilidad("HU-003", "Completando formulario producto");
            utileria.tomarScreenshot("antes-completar-formulario");

            // Completar campos uno por uno
            if (datosProducto.containsKey("nombre")) {
                ingresarNombreProducto(datosProducto.get("nombre"));
            }
            if (datosProducto.containsKey("codigo")) {
                ingresarCodigoProducto(datosProducto.get("codigo"));
            }
            if (datosProducto.containsKey("categoria")) {
                seleccionarCategoriaProducto(datosProducto.get("categoria"));
            }
            if (datosProducto.containsKey("precio")) {
                ingresarPrecioProducto(datosProducto.get("precio"));
            }
            if (datosProducto.containsKey("stock")) {
                ingresarStockProducto(datosProducto.get("stock"));
            }
            if (datosProducto.containsKey("descripcion")) {
                ingresarDescripcionProducto(datosProducto.get("descripcion"));
            }

            utileria.tomarScreenshot("formulario-producto-completado");

        } catch (Exception e) {
            logger.error("Error completando formulario producto: {}", e.getMessage());
            utileria.manejarError("Error completando formulario", e);
            throw e;
        }
    }

    /**
     * Ingresa el nombre del producto.
     */
    public void ingresarNombreProducto(String nombre) {
        try {
            logger.info("Ingresando nombre de producto: {}", nombre);
            utileria.registrarTrazabilidad("HU-003", "Ingreso nombre producto: " + nombre);

            utileria.esperarElementoVisible(CAMPO_NOMBRE);
            utileria.limpiarCampo(CAMPO_NOMBRE);
            utileria.escribirTexto(CAMPO_NOMBRE, nombre);

        } catch (Exception e) {
            logger.error("Error ingresando nombre producto: {}", e.getMessage());
            utileria.manejarError("Error ingresando nombre", e);
            throw e;
        }
    }

    /**
     * Ingresa el código del producto.
     */
    public void ingresarCodigoProducto(String codigo) {
        try {
            logger.info("Ingresando código de producto: {}", codigo);
            utileria.registrarTrazabilidad("HU-003", "Ingreso código producto: " + codigo);

            utileria.esperarElementoVisible(CAMPO_CODIGO);
            utileria.limpiarCampo(CAMPO_CODIGO);
            utileria.escribirTexto(CAMPO_CODIGO, codigo);

        } catch (Exception e) {
            logger.error("Error ingresando código producto: {}", e.getMessage());
            utileria.manejarError("Error ingresando código", e);
            throw e;
        }
    }

    /**
     * Selecciona la categoría del producto.
     */
    public void seleccionarCategoriaProducto(String categoria) {
        try {
            logger.info("Seleccionando categoría: {}", categoria);
            utileria.registrarTrazabilidad("HU-003", "Selección categoría: " + categoria);

            utileria.esperarElementoVisible(CAMPO_CATEGORIA);
            utileria.seleccionarOpcion(CAMPO_CATEGORIA, categoria);

        } catch (Exception e) {
            logger.error("Error seleccionando categoría: {}", e.getMessage());
            utileria.manejarError("Error seleccionando categoría", e);
            throw e;
        }
    }

    /**
     * Ingresa el precio del producto.
     */
    public void ingresarPrecioProducto(String precio) {
        try {
            logger.info("Ingresando precio de producto: {}", precio);
            utileria.registrarTrazabilidad("HU-003", "Ingreso precio producto: " + precio);

            utileria.esperarElementoVisible(CAMPO_PRECIO);
            utileria.limpiarCampo(CAMPO_PRECIO);
            utileria.escribirTexto(CAMPO_PRECIO, precio);

        } catch (Exception e) {
            logger.error("Error ingresando precio: {}", e.getMessage());
            utileria.manejarError("Error ingresando precio", e);
            throw e;
        }
    }

    /**
     * Ingresa el stock del producto.
     */
    public void ingresarStockProducto(String stock) {
        try {
            logger.info("Ingresando stock de producto: {}", stock);
            utileria.registrarTrazabilidad("HU-003", "Ingreso stock producto: " + stock);

            utileria.esperarElementoVisible(CAMPO_STOCK);
            utileria.limpiarCampo(CAMPO_STOCK);
            utileria.escribirTexto(CAMPO_STOCK, stock);

        } catch (Exception e) {
            logger.error("Error ingresando stock: {}", e.getMessage());
            utileria.manejarError("Error ingresando stock", e);
            throw e;
        }
    }

    /**
     * Ingresa la descripción del producto.
     */
    public void ingresarDescripcionProducto(String descripcion) {
        try {
            logger.info("Ingresando descripción de producto");
            utileria.registrarTrazabilidad("HU-003", "Ingreso descripción producto");

            utileria.esperarElementoVisible(CAMPO_DESCRIPCION);
            utileria.limpiarCampo(CAMPO_DESCRIPCION);
            utileria.escribirTexto(CAMPO_DESCRIPCION, descripcion);

        } catch (Exception e) {
            logger.error("Error ingresando descripción: {}", e.getMessage());
            utileria.manejarError("Error ingresando descripción", e);
            throw e;
        }
    }

    /**
     * Guarda el producto haciendo clic en el botón guardar.
     */
    public void guardarProducto() {
        try {
            logger.info("Guardando producto");
            utileria.registrarTrazabilidad("HU-003", "Guardado de producto");
            utileria.tomarScreenshot("antes-guardar-producto");

            utileria.esperarElementoClickeable(BOTON_GUARDAR);
            utileria.hacerClick(BOTON_GUARDAR);

            // Esperar a que se procese la operación
            utileria.esperarTiempo(2000);
            utileria.tomarScreenshot("despues-guardar-producto");

        } catch (Exception e) {
            logger.error("Error guardando producto: {}", e.getMessage());
            utileria.manejarError("Error guardando producto", e);
            throw e;
        }
    }

    // ==================== OPERACIONES READ ====================

    /**
     * Busca productos por criterio específico.
     *
     * @param criterio criterio de búsqueda (nombre, código, categoría)
     * @param valor valor a buscar
     */
    public void buscarProductos(String criterio, String valor) {
        try {
            logger.info("Buscando productos por {}: {}", criterio, valor);
            utileria.registrarTrazabilidad("HU-003", "Búsqueda por " + criterio + ": " + valor);

            // Limpiar búsqueda anterior
            utileria.limpiarCampo(CAMPO_BUSQUEDA);

            // Ingresar término de búsqueda
            utileria.escribirTexto(CAMPO_BUSQUEDA, valor);
            utileria.hacerClick(BOTON_BUSCAR);

            // Esperar resultados
            utileria.esperarTiempo(1500);
            utileria.tomarScreenshot("resultados-busqueda-" + criterio);

        } catch (Exception e) {
            logger.error("Error buscando productos: {}", e.getMessage());
            utileria.manejarError("Error en búsqueda", e);
            throw e;
        }
    }

    /**
     * Obtiene la lista de productos visibles en la tabla.
     *
     * @return lista de productos como mapas de datos
     */
    public List<Map<String, String>> obtenerListaProductos() {
        try {
            logger.info("Obteniendo lista de productos visible");
            utileria.registrarTrazabilidad("HU-003", "Obtención de lista de productos");

            List<WebElement> filas = utileria.buscarElementos(FILAS_PRODUCTOS);
            List<Map<String, String>> productos = new java.util.ArrayList<>();

            for (WebElement fila : filas) {
                Map<String, String> producto = new HashMap<>();

                // Extraer datos de cada columna
                List<WebElement> columnas = fila.findElements(By.tagName("td"));

                if (columnas.size() >= 6) {
                    producto.put("id", columnas.get(0).getText());
                    producto.put("nombre", columnas.get(1).getText());
                    producto.put("codigo", columnas.get(2).getText());
                    producto.put("categoria", columnas.get(3).getText());
                    producto.put("precio", columnas.get(4).getText());
                    producto.put("stock", columnas.get(5).getText());
                }

                productos.add(producto);
            }

            logger.info("Se obtuvieron {} productos de la lista", productos.size());
            return productos;

        } catch (Exception e) {
            logger.error("Error obteniendo lista de productos: {}", e.getMessage());
            utileria.manejarError("Error obteniendo lista", e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Verifica si un producto específico existe en la lista.
     *
     * @param nombreProducto nombre del producto a buscar
     * @return true si el producto existe, false en caso contrario
     */
    public boolean existeProductoEnLista(String nombreProducto) {
        try {
            logger.info("Verificando existencia de producto: {}", nombreProducto);
            utileria.registrarTrazabilidad("HU-003", "Verificación existencia: " + nombreProducto);

            List<Map<String, String>> productos = obtenerListaProductos();

            boolean existe = productos.stream()
                    .anyMatch(producto -> producto.get("nombre").equals(nombreProducto));

            logger.info("Producto '{}' {}", nombreProducto, existe ? "encontrado" : "no encontrado");
            return existe;

        } catch (Exception e) {
            logger.error("Error verificando existencia de producto: {}", e.getMessage());
            utileria.manejarError("Error verificando existencia", e);
            return false;
        }
    }

    /**
     * Obtiene el total de productos mostrado en la página.
     *
     * @return número total de productos
     */
    public int obtenerTotalProductos() {
        try {
            if (utileria.esElementoVisible(TOTAL_PRODUCTOS)) {
                String textoTotal = utileria.obtenerTexto(TOTAL_PRODUCTOS);
                // Extraer número del texto (ej: "Total: 25 productos" -> "25")
                String numero = textoTotal.replaceAll("[^0-9]", "");
                return Integer.parseInt(numero);
            }
            return 0;
        } catch (Exception e) {
            logger.warn("Error obteniendo total de productos: {}", e.getMessage());
            return 0;
        }
    }

    // ==================== OPERACIONES UPDATE ====================

    /**
     * Selecciona un producto para editar por su nombre.
     *
     * @param nombreProducto nombre del producto a editar
     */
    public void seleccionarProductoParaEditar(String nombreProducto) {
        try {
            logger.info("Seleccionando producto para editar: {}", nombreProducto);
            utileria.registrarTrazabilidad("HU-003", "Selección para editar: " + nombreProducto);

            // Buscar el producto en la tabla
            By filaProducto = By.xpath("//tr[td[contains(text(), '" + nombreProducto + "')]]");
            By botonEditarProducto = By.xpath("//tr[td[contains(text(), '" + nombreProducto + "')]]//button[@data-action='edit']");

            utileria.esperarElementoVisible(filaProducto);
            utileria.hacerClick(botonEditarProducto);

            // Esperar a que aparezca el modal de edición
            utileria.esperarElementoVisible(MODAL_PRODUCTO);
            utileria.tomarScreenshot("modal-editar-producto");

        } catch (Exception e) {
            logger.error("Error seleccionando producto para editar: {}", e.getMessage());
            utileria.manejarError("Error seleccionando para editar", e);
            throw e;
        }
    }

    /**
     * Modifica campos específicos de un producto.
     *
     * @param camposAModificar mapa con los campos y nuevos valores
     */
    public void modificarCamposProducto(Map<String, String> camposAModificar) {
        try {
            logger.info("Modificando campos de producto");
            utileria.registrarTrazabilidad("HU-003", "Modificación de campos");
            utileria.tomarScreenshot("antes-modificar-campos");

            for (Map.Entry<String, String> campo : camposAModificar.entrySet()) {
                String nombreCampo = campo.getKey();
                String nuevoValor = campo.getValue();

                logger.info("Modificando campo '{}' a valor '{}'", nombreCampo, nuevoValor);

                switch (nombreCampo.toLowerCase()) {
                    case "nombre" -> ingresarNombreProducto(nuevoValor);
                    case "codigo" -> ingresarCodigoProducto(nuevoValor);
                    case "categoria" -> seleccionarCategoriaProducto(nuevoValor);
                    case "precio" -> ingresarPrecioProducto(nuevoValor);
                    case "stock" -> ingresarStockProducto(nuevoValor);
                    case "descripcion" -> ingresarDescripcionProducto(nuevoValor);
                    default -> logger.warn("Campo no reconocido para modificación: {}", nombreCampo);
                }
            }

            utileria.tomarScreenshot("campos-modificados");

        } catch (Exception e) {
            logger.error("Error modificando campos: {}", e.getMessage());
            utileria.manejarError("Error modificando campos", e);
            throw e;
        }
    }

    // ==================== OPERACIONES DELETE ====================

    /**
     * Selecciona un producto para eliminar por su nombre.
     *
     * @param nombreProducto nombre del producto a eliminar
     */
    public void seleccionarProductoParaEliminar(String nombreProducto) {
        try {
            logger.info("Seleccionando producto para eliminar: {}", nombreProducto);
            utileria.registrarTrazabilidad("HU-003", "Selección para eliminar: " + nombreProducto);

            // Buscar el botón eliminar del producto específico
            By botonEliminarProducto = By.xpath("//tr[td[contains(text(), '" + nombreProducto + "')]]//button[@data-action='delete']");

            utileria.esperarElementoClickeable(botonEliminarProducto);
            utileria.hacerClick(botonEliminarProducto);

            // Esperar a que aparezca el diálogo de confirmación
            utileria.esperarElementoVisible(MENSAJE_CONFIRMACION);
            utileria.tomarScreenshot("dialogo-confirmacion-eliminar");

        } catch (Exception e) {
            logger.error("Error seleccionando producto para eliminar: {}", e.getMessage());
            utileria.manejarError("Error seleccionando para eliminar", e);
            throw e;
        }
    }

    /**
     * Confirma la eliminación del producto en el diálogo.
     */
    public void confirmarEliminacion() {
        try {
            logger.info("Confirmando eliminación de producto");
            utileria.registrarTrazabilidad("HU-003", "Confirmación de eliminación");

            By botonConfirmarEliminacion = By.cssSelector(".btn-confirm-delete, [data-action='confirm-delete']");
            utileria.esperarElementoClickeable(botonConfirmarEliminacion);
            utileria.hacerClick(botonConfirmarEliminacion);

            // Esperar a que se procese la eliminación
            utileria.esperarTiempo(2000);
            utileria.tomarScreenshot("producto-eliminado");

        } catch (Exception e) {
            logger.error("Error confirmando eliminación: {}", e.getMessage());
            utileria.manejarError("Error confirmando eliminación", e);
            throw e;
        }
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Obtiene el mensaje de éxito mostrado en la página.
     *
     * @return texto del mensaje de éxito
     */
    public String obtenerMensajeExito() {
        try {
            if (utileria.esElementoVisible(MENSAJE_EXITO)) {
                String mensaje = utileria.obtenerTexto(MENSAJE_EXITO);
                logger.info("Mensaje de éxito obtenido: {}", mensaje);
                return mensaje;
            }
            return "";
        } catch (Exception e) {
            logger.warn("Error obteniendo mensaje de éxito: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Obtiene el mensaje de error mostrado en la página.
     *
     * @return texto del mensaje de error
     */
    public String obtenerMensajeError() {
        try {
            if (utileria.esElementoVisible(MENSAJE_ERROR)) {
                String mensaje = utileria.obtenerTexto(MENSAJE_ERROR);
                logger.info("Mensaje de error obtenido: {}", mensaje);
                return mensaje;
            }
            return "";
        } catch (Exception e) {
            logger.warn("Error obteniendo mensaje de error: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Verifica si hay errores de validación en campos específicos.
     *
     * @param campo nombre del campo a verificar
     * @return true si hay error en el campo, false en caso contrario
     */
    public boolean tieneErrorValidacion(String campo) {
        try {
            By localizadorError = switch (campo.toLowerCase()) {
                case "nombre" -> ERROR_NOMBRE;
                case "codigo" -> ERROR_CODIGO;
                case "precio" -> ERROR_PRECIO;
                case "stock" -> ERROR_STOCK;
                default -> throw new IllegalArgumentException("Campo no válido: " + campo);
            };

            boolean tieneError = utileria.esElementoVisible(localizadorError);
            if (tieneError) {
                String mensajeError = utileria.obtenerTexto(localizadorError);
                logger.info("Error en campo {}: {}", campo, mensajeError);
            }
            return tieneError;

        } catch (Exception e) {
            logger.warn("Error verificando validación del campo {}: {}", campo, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si el botón guardar está habilitado.
     *
     * @return true si está habilitado, false en caso contrario
     */
    public boolean esBotonGuardarHabilitado() {
        try {
            return utileria.esElementoHabilitado(BOTON_GUARDAR);
        } catch (Exception e) {
            logger.warn("Error verificando estado del botón guardar: {}", e.getMessage());
            return false;
        }
    }

    // ==================== OPERACIONES DE FILTRADO Y PAGINACIÓN ====================

    /**
     * Aplica filtros de búsqueda avanzada.
     *
     * @param filtros mapa con los filtros a aplicar
     */
    public void aplicarFiltros(Map<String, String> filtros) {
        try {
            logger.info("Aplicando filtros de búsqueda");
            utileria.registrarTrazabilidad("HU-003", "Aplicación de filtros");

            if (filtros.containsKey("categoria")) {
                utileria.seleccionarOpcion(FILTRO_CATEGORIA, filtros.get("categoria"));
            }
            if (filtros.containsKey("precio_min")) {
                utileria.escribirTexto(FILTRO_PRECIO_MIN, filtros.get("precio_min"));
            }
            if (filtros.containsKey("precio_max")) {
                utileria.escribirTexto(FILTRO_PRECIO_MAX, filtros.get("precio_max"));
            }

            // Aplicar filtros
            utileria.hacerClick(BOTON_BUSCAR);
            utileria.esperarTiempo(1500);
            utileria.tomarScreenshot("filtros-aplicados");

        } catch (Exception e) {
            logger.error("Error aplicando filtros: {}", e.getMessage());
            utileria.manejarError("Error aplicando filtros", e);
            throw e;
        }
    }

    /**
     * Limpia todos los filtros aplicados.
     */
    public void limpiarFiltros() {
        try {
            logger.info("Limpiando filtros de búsqueda");
            utileria.registrarTrazabilidad("HU-003", "Limpieza de filtros");

            if (utileria.esElementoVisible(BOTON_LIMPIAR_FILTROS)) {
                utileria.hacerClick(BOTON_LIMPIAR_FILTROS);
                utileria.esperarTiempo(1000);
                utileria.tomarScreenshot("filtros-limpiados");
            }

        } catch (Exception e) {
            logger.error("Error limpiando filtros: {}", e.getMessage());
            utileria.manejarError("Error limpiando filtros", e);
            throw e;
        }
    }

    /**
     * Navega a la siguiente página de resultados.
     */
    public void irAPaginaSiguiente() {
        try {
            logger.info("Navegando a página siguiente");
            utileria.registrarTrazabilidad("HU-003", "Navegación página siguiente");

            if (utileria.esElementoClickeable(BOTON_PAGINA_SIGUIENTE)) {
                utileria.hacerClick(BOTON_PAGINA_SIGUIENTE);
                utileria.esperarTiempo(1500);
                utileria.tomarScreenshot("pagina-siguiente");
            }

        } catch (Exception e) {
            logger.error("Error navegando a página siguiente: {}", e.getMessage());
            utileria.manejarError("Error navegación página", e);
            throw e;
        }
    }

    /**
     * Navega a la página anterior de resultados.
     */
    public void irAPaginaAnterior() {
        try {
            logger.info("Navegando a página anterior");
            utileria.registrarTrazabilidad("HU-003", "Navegación página anterior");

            if (utileria.esElementoClickeable(BOTON_PAGINA_ANTERIOR)) {
                utileria.hacerClick(BOTON_PAGINA_ANTERIOR);
                utileria.esperarTiempo(1500);
                utileria.tomarScreenshot("pagina-anterior");
            }

        } catch (Exception e) {
            logger.error("Error navegando a página anterior: {}", e.getMessage());
            utileria.manejarError("Error navegación página", e);
            throw e;
        }
    }

    // ==================== OPERACIONES MASIVAS ====================

    /**
     * Selecciona múltiples productos por sus nombres.
     *
     * @param nombresProductos lista de nombres de productos a seleccionar
     */
    public void seleccionarMultiplesProductos(List<String> nombresProductos) {
        try {
            logger.info("Seleccionando múltiples productos: {}", nombresProductos);
            utileria.registrarTrazabilidad("HU-003", "Selección múltiple productos");

            for (String nombre : nombresProductos) {
                By checkboxProducto = By.xpath("//tr[td[contains(text(), '" + nombre + "')]]//input[@type='checkbox']");
                if (utileria.esElementoVisible(checkboxProducto)) {
                    utileria.hacerClick(checkboxProducto);
                    utileria.esperarTiempo(200);
                }
            }

            utileria.tomarScreenshot("productos-seleccionados");

        } catch (Exception e) {
            logger.error("Error seleccionando múltiples productos: {}", e.getMessage());
            utileria.manejarError("Error selección múltiple", e);
            throw e;
        }
    }

    /**
     * Aplica una operación masiva a los productos seleccionados.
     *
     * @param operacion tipo de operación (eliminar, cambiar_categoria, etc.)
     * @param parametros parámetros adicionales para la operación
     */
    public void aplicarOperacionMasiva(String operacion, Map<String, String> parametros) {
        try {
            logger.info("Aplicando operación masiva: {}", operacion);
            utileria.registrarTrazabilidad("HU-003", "Operación masiva: " + operacion);

            // Buscar y hacer clic en el botón de operación masiva
            By botonOperacionMasiva = By.cssSelector("[data-action='mass-" + operacion + "']");
            utileria.hacerClick(botonOperacionMasiva);

            // Configurar parámetros si es necesario
            if (parametros != null && !parametros.isEmpty()) {
                configurarParametrosOperacionMasiva(parametros);
            }

            // Confirmar operación
            By botonConfirmar = By.cssSelector(".btn-confirm-mass-operation");
            utileria.hacerClick(botonConfirmar);

            utileria.esperarTiempo(3000);
            utileria.tomarScreenshot("operacion-masiva-completada");

        } catch (Exception e) {
            logger.error("Error en operación masiva: {}", e.getMessage());
            utileria.manejarError("Error operación masiva", e);
            throw e;
        }
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    /**
     * Verifica que los botones principales estén presentes.
     */
    private boolean verificarBotonesPrincipalesPresentes() {
        try {
            return utileria.esElementoVisible(BOTON_NUEVO_PRODUCTO) &&
                    utileria.esElementoVisible(CAMPO_BUSQUEDA) &&
                    utileria.esElementoVisible(BOTON_BUSCAR);
        } catch (Exception e) {
            logger.warn("Error verificando botones principales: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Configura parámetros adicionales para operaciones masivas.
     */
    private void configurarParametrosOperacionMasiva(Map<String, String> parametros) {
        try {
            for (Map.Entry<String, String> parametro : parametros.entrySet()) {
                By campo = By.id("mass-param-" + parametro.getKey());
                if (utileria.esElementoVisible(campo)) {
                    utileria.escribirTexto(campo, parametro.getValue());
                }
            }
        } catch (Exception e) {
            logger.warn("Error configurando parámetros operación masiva: {}", e.getMessage());
        }
    }

    /**
     * Cancela la operación actual cerrando el modal.
     */
    public void cancelarOperacion() {
        try {
            logger.info("Cancelando operación actual");
            utileria.registrarTrazabilidad("HU-003", "Cancelación de operación");

            if (utileria.esElementoVisible(BOTON_CANCELAR)) {
                utileria.hacerClick(BOTON_CANCELAR);
                utileria.esperarTiempo(1000);
            }

        } catch (Exception e) {
            logger.error("Error cancelando operación: {}", e.getMessage());
            utileria.manejarError("Error cancelando", e);
        }
    }

    /**
     * Exporta la lista actual de productos.
     */
    public void exportarProductos() {
        try {
            logger.info("Exportando lista de productos");
            utileria.registrarTrazabilidad("HU-003", "Exportación de productos");

            utileria.hacerClick(BOTON_EXPORTAR);
            utileria.esperarTiempo(3000);
            utileria.tomarScreenshot("exportacion-completada");

        } catch (Exception e) {
            logger.error("Error exportando productos: {}", e.getMessage());
            utileria.manejarError("Error exportando", e);
            throw e;
        }
    }
}