package com.qa.automatizacion.paginas;

import com.qa.automatizacion.modelo.ProductoCrud;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Optional;

/**
 * Page Object para las operaciones CRUD de productos del sistema.
 * Implementa la nueva arquitectura optimizada usando PaginaBase y UtileriasComunes.
 *
 * Principios aplicados:
 * - Herencia: Extiende PaginaBase para reutilizar funcionalidades comunes
 * - DRY: No duplica código, reutiliza métodos de PaginaBase/UtileriasComunes
 * - Single Responsibility: Se enfoca únicamente en operaciones CRUD
 * - Encapsulación: Expone métodos de alto nivel, oculta detalles de implementación
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0 - Optimizada con métodos reutilizables
 */
public class PaginaCrud extends PaginaBase {

    // ==================== LOCALIZADORES ====================

    // Botones principales de acción
    private static final By BOTON_NUEVO = By.id("btn-nuevo");
    private static final By BOTON_GUARDAR = By.id("btn-guardar");
    private static final By BOTON_EDITAR = By.id("btn-editar");
    private static final By BOTON_ELIMINAR = By.id("btn-eliminar");
    private static final By BOTON_CANCELAR = By.id("btn-cancelar");
    private static final By BOTON_BUSCAR = By.id("btn-buscar");
    private static final By BOTON_LIMPIAR_BUSQUEDA = By.id("btn-limpiar-busqueda");

    // Campos del formulario de producto
    private static final By CAMPO_NOMBRE = By.id("nombre-producto");
    private static final By CAMPO_DESCRIPCION = By.id("descripcion-producto");
    private static final By CAMPO_PRECIO = By.id("precio-producto");
    private static final By CAMPO_CATEGORIA = By.id("categoria-producto");
    private static final By CAMPO_STOCK = By.id("stock-producto");
    private static final By CAMPO_CODIGO = By.id("codigo-producto");

    // Campos de búsqueda y filtros
    private static final By CAMPO_BUSQUEDA = By.id("campo-busqueda");
    private static final By FILTRO_CATEGORIA = By.id("filtro-categoria");
    private static final By FILTRO_PRECIO_MIN = By.id("precio-min");
    private static final By FILTRO_PRECIO_MAX = By.id("precio-max");

    // Tabla de resultados
    private static final By TABLA_PRODUCTOS = By.id("tabla-productos");
    private static final By FILAS_TABLA = By.cssSelector("#tabla-productos tbody tr");
    private static final By ENCABEZADOS_TABLA = By.cssSelector("#tabla-productos thead th");

    // Elementos de paginación
    private static final By PAGINACION_CONTAINER = By.cssSelector(".paginacion");
    private static final By BOTON_PAGINA_ANTERIOR = By.cssSelector(".paginacion .anterior");
    private static final By BOTON_PAGINA_SIGUIENTE = By.cssSelector(".paginacion .siguiente");
    private static final By INFO_PAGINACION = By.cssSelector(".paginacion .info");

    // Mensajes específicos de CRUD
    private static final By MENSAJE_PRODUCTO_GUARDADO = By.cssSelector("[data-testid='producto-guardado']");
    private static final By MENSAJE_PRODUCTO_ELIMINADO = By.cssSelector("[data-testid='producto-eliminado']");
    private static final By MENSAJE_PRODUCTO_NO_ENCONTRADO = By.cssSelector("[data-testid='producto-no-encontrado']");
    private static final By MENSAJE_ERROR_PRECIO_INVALIDO = By.cssSelector("[data-testid='error-precio-invalido']");

    // Formulario y contenedores
    private static final By FORMULARIO_PRODUCTO = By.id("form-producto");
    private static final By PANEL_BUSQUEDA = By.id("panel-busqueda");
    private static final By MODAL_CONFIRMACION = By.id("modal-confirmacion");

    // Elementos usando @FindBy (opcionales)
    @FindBy(id = "btn-nuevo")
    private WebElement botonNuevoElement;

    @FindBy(id = "tabla-productos")
    private WebElement tablaProductosElement;

    // ==================== CONSTRUCTORES ====================

    /**
     * Constructor que acepta un WebDriver específico.
     *
     * @param driver WebDriver a utilizar
     */
    public PaginaCrud(WebDriver driver) {
        super(driver);
    }

    /**
     * Constructor por defecto que usa el driver global.
     */
    public PaginaCrud() {
        super();
    }

    // ==================== MÉTODOS ABSTRACTOS IMPLEMENTADOS ====================

    @Override
    public boolean estaPaginaCargada() {
        return esElementoVisible(TABLA_PRODUCTOS, 5) &&
                esElementoVisible(BOTON_NUEVO, 2) &&
                esElementoVisible(PANEL_BUSQUEDA, 2) &&
                !estaCargando();
    }

    @Override
    public String obtenerUrlBase() {
        return propiedades.obtenerUrlCrud();
    }

    @Override
    protected By[] obtenerLocalizadoresUnicos() {
        return new By[]{
                TABLA_PRODUCTOS,
                BOTON_NUEVO,
                PANEL_BUSQUEDA,
                CAMPO_BUSQUEDA
        };
    }

    // ==================== OPERACIONES CRUD PRINCIPALES ====================

    /**
     * Crea un nuevo producto con todos los datos.
     *
     * @param producto objeto ProductoCrud con los datos completos
     * @return true si la creación fue exitosa
     */
    public boolean crearProducto(ProductoCrud producto) {
        registrarAccion("Creando producto", "Nombre: " + producto.getNombre());

        try {
            // Hacer clic en el botón nuevo
            if (!hacerClicEnNuevo()) {
                logger.error("Error haciendo clic en botón nuevo");
                return false;
            }

            // Llenar el formulario con los datos del producto
            if (!llenarFormularioProducto(producto)) {
                logger.error("Error llenando formulario del producto");
                return false;
            }

            // Guardar el producto
            if (!hacerClicEnGuardar()) {
                logger.error("Error haciendo clic en guardar");
                return false;
            }

            // Verificar que se guardó correctamente
            return verificarProductoGuardado();

        } catch (Exception e) {
            logger.error("Error durante creación de producto: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Lee/busca productos por diferentes criterios.
     *
     * @param criterioBusqueda texto de búsqueda
     * @return true si la búsqueda se ejecutó correctamente
     */
    public boolean buscarProductos(String criterioBusqueda) {
        registrarAccion("Buscando productos", "Criterio: " + criterioBusqueda);

        try {
            // Limpiar búsqueda anterior
            limpiarBusqueda();

            // Ingresar criterio de búsqueda
            if (!ingresarTextoSeguro(CAMPO_BUSQUEDA, criterioBusqueda, true)) {
                logger.error("Error ingresando criterio de búsqueda");
                return false;
            }

            // Hacer clic en buscar
            if (!hacerClicEnBuscar()) {
                logger.error("Error haciendo clic en buscar");
                return false;
            }

            // Esperar que se carguen los resultados
            esperarSegundos(2);
            return true;

        } catch (Exception e) {
            logger.error("Error durante búsqueda: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza un producto existente.
     *
     * @param nombreProductoActual nombre del producto a editar
     * @param productoNuevo datos actualizados del producto
     * @return true si la actualización fue exitosa
     */
    public boolean actualizarProducto(String nombreProductoActual, ProductoCrud productoNuevo) {
        registrarAccion("Actualizando producto", "Producto: " + nombreProductoActual);

        try {
            // Buscar el producto a editar
            if (!buscarProductos(nombreProductoActual)) {
                logger.error("Error buscando producto a editar");
                return false;
            }

            // Seleccionar el producto en la tabla
            if (!seleccionarProductoEnTabla(nombreProductoActual)) {
                logger.error("Error seleccionando producto en tabla");
                return false;
            }

            // Hacer clic en editar
            if (!hacerClicEnEditar()) {
                logger.error("Error haciendo clic en editar");
                return false;
            }

            // Actualizar datos en el formulario
            if (!llenarFormularioProducto(productoNuevo)) {
                logger.error("Error actualizando datos del producto");
                return false;
            }

            // Guardar cambios
            if (!hacerClicEnGuardar()) {
                logger.error("Error guardando cambios");
                return false;
            }

            return verificarProductoGuardado();

        } catch (Exception e) {
            logger.error("Error durante actualización: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un producto por su nombre.
     *
     * @param nombreProducto nombre del producto a eliminar
     * @return true si la eliminación fue exitosa
     */
    public boolean eliminarProducto(String nombreProducto) {
        registrarAccion("Eliminando producto", "Producto: " + nombreProducto);

        try {
            // Buscar el producto a eliminar
            if (!buscarProductos(nombreProducto)) {
                logger.error("Error buscando producto a eliminar");
                return false;
            }

            // Seleccionar el producto en la tabla
            if (!seleccionarProductoEnTabla(nombreProducto)) {
                logger.error("Error seleccionando producto para eliminar");
                return false;
            }

            // Hacer clic en eliminar
            if (!hacerClicEnEliminar()) {
                logger.error("Error haciendo clic en eliminar");
                return false;
            }

            // Confirmar eliminación si aparece modal
            if (esElementoVisible(MODAL_CONFIRMACION, 3)) {
                confirmarAccion();
            }

            return verificarProductoEliminado();

        } catch (Exception e) {
            logger.error("Error durante eliminación: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE INTERACCIÓN CON BOTONES ====================

    /**
     * Hace clic en el botón nuevo.
     *
     * @return true si el clic fue exitoso
     */
    public boolean hacerClicEnNuevo() {
        registrarAccion("Haciendo clic en botón nuevo");
        return hacerClicSeguro(BOTON_NUEVO, 3);
    }

    /**
     * Hace clic en el botón guardar.
     *
     * @return true si el clic fue exitoso
     */
    public boolean hacerClicEnGuardar() {
        registrarAccion("Haciendo clic en botón guardar");
        return hacerClicSeguro(BOTON_GUARDAR, 3);
    }

    /**
     * Hace clic en el botón editar.
     *
     * @return true si el clic fue exitoso
     */
    public boolean hacerClicEnEditar() {
        registrarAccion("Haciendo clic en botón editar");
        return hacerClicSeguro(BOTON_EDITAR, 3);
    }

    /**
     * Hace clic en el botón eliminar.
     *
     * @return true si el clic fue exitoso
     */
    public boolean hacerClicEnEliminar() {
        registrarAccion("Haciendo clic en botón eliminar");
        return hacerClicSeguro(BOTON_ELIMINAR, 3);
    }

    /**
     * Hace clic en el botón buscar.
     *
     * @return true si el clic fue exitoso
     */
    public boolean hacerClicEnBuscar() {
        registrarAccion("Haciendo clic en botón buscar");
        return hacerClicSeguro(BOTON_BUSCAR, 3);
    }

    /**
     * Hace clic en el botón cancelar.
     *
     * @return true si el clic fue exitoso
     */
    public boolean hacerClicEnCancelar() {
        registrarAccion("Haciendo clic en botón cancelar");
        return hacerClicSeguro(BOTON_CANCELAR);
    }

    // ==================== MÉTODOS DE FORMULARIO ====================

    /**
     * Llena el formulario de producto con todos los datos.
     *
     * @param producto objeto ProductoCrud con los datos
     * @return true si se llenó correctamente
     */
    private boolean llenarFormularioProducto(ProductoCrud producto) {
        registrarAccion("Llenando formulario de producto");

        try {
            // Campos obligatorios
            if (!ingresarTextoSeguro(CAMPO_NOMBRE, producto.getNombre(), true)) return false;
            if (!ingresarTextoSeguro(CAMPO_DESCRIPCION, producto.getDescripcion(), true)) return false;
            if (!ingresarTextoSeguro(CAMPO_PRECIO, producto.getPrecio().toString(), true)) return false;

            // Campos opcionales
            if (producto.getCategoria() != null) {
                seleccionarOpcionPorTexto(CAMPO_CATEGORIA, producto.getCategoria());
            }

            if (producto.getStock() != null) {
                ingresarTextoSeguro(CAMPO_STOCK, producto.getStock().toString(), true);
            }

            if (producto.getCodigo() != null && !producto.getCodigo().isEmpty()) {
                ingresarTextoSeguro(CAMPO_CODIGO, producto.getCodigo(), true);
            }

            logger.info("Formulario de producto llenado exitosamente");
            return true;

        } catch (Exception e) {
            logger.error("Error llenando formulario: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Limpia todos los campos del formulario.
     */
    public void limpiarFormulario() {
        registrarAccion("Limpiando formulario de producto");

        try {
            ingresarTextoSeguro(CAMPO_NOMBRE, "", true);
            ingresarTextoSeguro(CAMPO_DESCRIPCION, "", true);
            ingresarTextoSeguro(CAMPO_PRECIO, "", true);
            ingresarTextoSeguro(CAMPO_STOCK, "", true);
            ingresarTextoSeguro(CAMPO_CODIGO, "", true);

            logger.info("Formulario limpiado");
        } catch (Exception e) {
            logger.error("Error limpiando formulario: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE TABLA Y RESULTADOS ====================

    /**
     * Selecciona un producto específico en la tabla por su nombre.
     *
     * @param nombreProducto nombre del producto a seleccionar
     * @return true si se seleccionó correctamente
     */
    public boolean seleccionarProductoEnTabla(String nombreProducto) {
        registrarAccion("Seleccionando producto en tabla", nombreProducto);

        try {
            List<WebElement> filas = buscarElementos(FILAS_TABLA);

            for (WebElement fila : filas) {
                if (fila.getText().contains(nombreProducto)) {
                    fila.click();
                    logger.debug("Producto seleccionado: {}", nombreProducto);
                    return true;
                }
            }

            logger.error("Producto no encontrado en tabla: {}", nombreProducto);
            return false;

        } catch (Exception e) {
            logger.error("Error seleccionando producto: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el número de productos mostrados en la tabla.
     *
     * @return número de productos en la tabla
     */
    public int obtenerNumeroProductosEnTabla() {
        try {
            List<WebElement> filas = buscarElementos(FILAS_TABLA);
            int numero = filas.size();
            logger.debug("Número de productos en tabla: {}", numero);
            return numero;
        } catch (Exception e) {
            logger.error("Error obteniendo número de productos: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Verifica si un producto específico existe en la tabla.
     *
     * @param nombreProducto nombre del producto a buscar
     * @return true si el producto existe en la tabla
     */
    public boolean existeProductoEnTabla(String nombreProducto) {
        try {
            List<WebElement> filas = buscarElementos(FILAS_TABLA);

            for (WebElement fila : filas) {
                if (fila.getText().contains(nombreProducto)) {
                    logger.debug("Producto encontrado en tabla: {}", nombreProducto);
                    return true;
                }
            }

            logger.debug("Producto no encontrado en tabla: {}", nombreProducto);
            return false;

        } catch (Exception e) {
            logger.error("Error verificando existencia de producto: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE BÚSQUEDA Y FILTROS ====================

    /**
     * Aplica filtros de búsqueda avanzada.
     *
     * @param categoria categoría a filtrar (puede ser null)
     * @param precioMin precio mínimo (puede ser null)
     * @param precioMax precio máximo (puede ser null)
     * @return true si se aplicaron los filtros correctamente
     */
    public boolean aplicarFiltrosAvanzados(String categoria, Double precioMin, Double precioMax) {
        registrarAccion("Aplicando filtros avanzados",
                "Categoría: " + categoria + ", Precio: " + precioMin + "-" + precioMax);

        try {
            if (categoria != null && !categoria.isEmpty()) {
                seleccionarOpcionPorTexto(FILTRO_CATEGORIA, categoria);
            }

            if (precioMin != null) {
                ingresarTextoSeguro(FILTRO_PRECIO_MIN, precioMin.toString(), true);
            }

            if (precioMax != null) {
                ingresarTextoSeguro(FILTRO_PRECIO_MAX, precioMax.toString(), true);
            }

            // Aplicar filtros
            hacerClicEnBuscar();
            esperarSegundos(2);

            return true;

        } catch (Exception e) {
            logger.error("Error aplicando filtros: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Limpia todos los filtros de búsqueda.
     */
    public void limpiarBusqueda() {
        registrarAccion("Limpiando búsqueda y filtros");

        try {
            if (esElementoVisible(BOTON_LIMPIAR_BUSQUEDA, 2)) {
                hacerClicSeguro(BOTON_LIMPIAR_BUSQUEDA);
            } else {
                // Limpiar manualmente si no hay botón específico
                ingresarTextoSeguro(CAMPO_BUSQUEDA, "", true);
                ingresarTextoSeguro(FILTRO_PRECIO_MIN, "", true);
                ingresarTextoSeguro(FILTRO_PRECIO_MAX, "", true);
            }

            esperarSegundos(1);
            logger.info("Búsqueda limpiada");

        } catch (Exception e) {
            logger.error("Error limpiando búsqueda: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS DE PAGINACIÓN ====================

    /**
     * Navega a la página siguiente de resultados.
     *
     * @return true si se navegó exitosamente
     */
    public boolean irAPaginaSiguiente() {
        registrarAccion("Navegando a página siguiente");

        if (esElementoHabilitado(BOTON_PAGINA_SIGUIENTE)) {
            return hacerClicSeguro(BOTON_PAGINA_SIGUIENTE);
        } else {
            logger.debug("No hay página siguiente disponible");
            return false;
        }
    }

    /**
     * Navega a la página anterior de resultados.
     *
     * @return true si se navegó exitosamente
     */
    public boolean irAPaginaAnterior() {
        registrarAccion("Navegando a página anterior");

        if (esElementoHabilitado(BOTON_PAGINA_ANTERIOR)) {
            return hacerClicSeguro(BOTON_PAGINA_ANTERIOR);
        } else {
            logger.debug("No hay página anterior disponible");
            return false;
        }
    }

    /**
     * Obtiene información de paginación actual.
     *
     * @return texto con información de paginación
     */
    public Optional<String> obtenerInfoPaginacion() {
        return obtenerTextoElemento(INFO_PAGINACION);
    }

    // ==================== MÉTODOS DE VERIFICACIÓN ====================

    /**
     * Verifica que un producto se guardó correctamente.
     *
     * @return true si se confirmó el guardado
     */
    private boolean verificarProductoGuardado() {
        // Esperar mensaje de confirmación
        if (esElementoVisible(MENSAJE_PRODUCTO_GUARDADO, 5)) {
            logger.info("Producto guardado - mensaje de confirmación visible");
            return true;
        }

        // Verificar si regresó a la lista (indicativo de guardado exitoso)
        if (esElementoVisible(TABLA_PRODUCTOS, 3)) {
            logger.info("Producto guardado - regresó a la tabla");
            return true;
        }

        logger.error("No se pudo verificar que el producto se guardó");
        return false;
    }

    /**
     * Verifica que un producto se eliminó correctamente.
     *
     * @return true si se confirmó la eliminación
     */
    private boolean verificarProductoEliminado() {
        // Esperar mensaje de confirmación
        if (esElementoVisible(MENSAJE_PRODUCTO_ELIMINADO, 5)) {
            logger.info("Producto eliminado - mensaje de confirmación visible");
            return true;
        }

        // Si no hay mensaje específico, asumir éxito si no hay errores
        if (!hayMensajesError()) {
            logger.info("Producto eliminado - no hay errores");
            return true;
        }

        logger.error("No se pudo verificar que el producto se eliminó");
        return false;
    }

    /**
     * Verifica si hay errores específicos de CRUD.
     *
     * @return true si hay algún error específico
     */
    public boolean hayErroresCrud() {
        return esElementoVisible(MENSAJE_ERROR_PRECIO_INVALIDO, 2) ||
                esElementoVisible(MENSAJE_PRODUCTO_NO_ENCONTRADO, 2) ||
                hayMensajesError();
    }

    // ==================== MÉTODOS DE CONFIRMACIÓN ====================

    /**
     * Confirma una acción en el modal de confirmación.
     *
     * @return true si se confirmó la acción
     */
    public boolean confirmarAccion() {
        registrarAccion("Confirmando acción en modal");

        By botonConfirmar = By.cssSelector("#modal-confirmacion .btn-confirmar, .modal .btn-si, .btn-aceptar");
        return hacerClicSeguro(botonConfirmar);
    }

    /**
     * Cancela una acción en el modal de confirmación.
     *
     * @return true si se canceló la acción
     */
    public boolean cancelarAccion() {
        registrarAccion("Cancelando acción en modal");

        By botonCancelar = By.cssSelector("#modal-confirmacion .btn-cancelar, .modal .btn-no, .btn-cancelar");
        return hacerClicSeguro(botonCancelar);
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Valida que los datos de un producto sean correctos antes de guardar.
     *
     * @param producto producto a validar
     * @return true si los datos son válidos
     */
    public boolean validarDatosProducto(ProductoCrud producto) {
        registrarAccion("Validando datos de producto", producto.getNombre());

        // Validaciones básicas
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            logger.error("El nombre del producto es obligatorio");
            return false;
        }

        if (producto.getPrecio() == null || producto.getPrecio().doubleValue() <= 0) {
            logger.error("El precio debe ser mayor a 0");
            return false;
        }

        if (producto.getStock() != null && producto.getStock() < 0) {
            logger.error("El stock no puede ser negativo");
            return false;
        }

        logger.info("Datos del producto válidos");
        return true;
    }

    // ==================== MÉTODOS DE VERIFICACIÓN DE SALUD ====================

    /**
     * Verifica el estado de salud específico de la página CRUD.
     *
     * @return true si la página está en buen estado para operaciones CRUD
     */
    @Override
    public boolean verificarSaludPagina() {
        boolean saludBase = super.verificarSaludPagina();

        if (!saludBase) {
            return false;
        }

        // Verificaciones específicas de CRUD
        boolean saludCrud = esElementoVisible(TABLA_PRODUCTOS, 3) &&
                esElementoHabilitado(BOTON_NUEVO) &&
                esElementoVisible(PANEL_BUSQUEDA, 3) &&
                !hayErroresCrud();

        logger.debug("Salud específica de CRUD: {}", saludCrud);
        return saludCrud;
    }

    /**
     * Limpia recursos específicos de la página CRUD.
     */
    @Override
    public void limpiarRecursos() {
        super.limpiarRecursos();

        // Limpiar formularios y búsquedas si están visibles
        if (esElementoVisible(FORMULARIO_PRODUCTO, 2)) {
            limpiarFormulario();
        }

        limpiarBusqueda();

        logger.debug("Recursos específicos de PaginaCrud limpiados");
    }
}