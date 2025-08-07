package com.qa.automatizacion.paginas;

import com.qa.automatizacion.modelo.ProductoCrud;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Página para operaciones CRUD de productos.
 * Implementa el patrón Page Object Model para interacciones con la interfaz de gestión de productos.
 *
 * Principios aplicados:
 * - Page Object Model: Encapsula elementos y acciones de la página
 * - Single Responsibility: Se enfoca únicamente en operaciones CRUD
 * - Abstracción: Oculta complejidades de interacción con Selenium
 *
 * @author Equipo QA Automatización
 * @version 1.0
 */
public class PaginaCrud extends PaginaBase {

    private static final Logger logger = LoggerFactory.getLogger(PaginaCrud.class);

    // Elementos del formulario de producto
    @FindBy(id = "nombre")
    private WebElement campoNombre;

    @FindBy(id = "descripcion")
    private WebElement campoDescripcion;

    @FindBy(id = "precio")
    private WebElement campoPrecio;

    @FindBy(id = "categoria")
    private WebElement campoCategoria;

    @FindBy(id = "stock")
    private WebElement campoStock;

    // Botones de acción
    @FindBy(id = "btnGuardar")
    private WebElement botonGuardar;

    @FindBy(id = "btnNuevo")
    private WebElement botonNuevo;

    @FindBy(id = "btnBuscar")
    private WebElement botonBuscar;

    @FindBy(id = "btnLimpiar")
    private WebElement botonLimpiar;

    // Campo de búsqueda
    @FindBy(id = "campoBusqueda")
    private WebElement campoBusqueda;

    // Tabla de resultados
    @FindBy(id = "tablaProductos")
    private WebElement tablaProductos;

    @FindBy(css = "#tablaProductos tbody tr")
    private List<WebElement> filasProductos;

    // Mensajes y alertas
    @FindBy(css = ".alert-success")
    private WebElement mensajeExito;

    @FindBy(css = ".alert-danger")
    private WebElement mensajeError;

    @FindBy(css = ".alert-warning")
    private WebElement mensajeAdvertencia;

    @FindBy(css = ".validation-message")
    private WebElement mensajeValidacion;

    // Modales y confirmaciones
    @FindBy(id = "modalConfirmacion")
    private WebElement modalConfirmacion;

    @FindBy(id = "btnConfirmarEliminar")
    private WebElement botonConfirmarEliminar;

    @FindBy(id = "btnCancelarEliminar")
    private WebElement botonCancelarEliminar;

    /**
     * Constructor que inicializa la página CRUD
     *
     * @param driver Instancia del WebDriver
     */
    public PaginaCrud(WebDriver driver) {
        super(driver);
        logger.info("Página CRUD inicializada");
    }

    @Override
    public boolean estaPaginaCargada() {
        try {
            esperarElementoVisible(campoNombre);
            esperarElementoVisible(botonGuardar);
            esperarElementoVisible(tablaProductos);
            return true;
        } catch (Exception e) {
            logger.error("Error verificando carga de página CRUD: {}", e.getMessage());
            return false;
        }
    }

    // Métodos para operaciones CRUD

    /**
     * Crea un nuevo producto
     *
     * @param producto ProductoCrud con los datos a crear
     * @return true si la creación fue exitosa
     */
    public boolean crearProducto(ProductoCrud producto) {
        try {
            logger.info("Iniciando creación de producto: {}", producto.getNombre());

            // Hacer clic en nuevo para limpiar formulario
            hacerClicSeguro(botonNuevo);

            // Llenar formulario
            llenarFormularioProducto(producto);

            // Guardar producto
            hacerClicSeguro(botonGuardar);

            // Verificar mensaje de éxito
            boolean exitoso = esperarMensajeExito();

            if (exitoso) {
                logger.info("Producto creado exitosamente: {}", producto.getNombre());
            } else {
                logger.warn("Error al crear producto: {}", producto.getNombre());
            }

            return exitoso;

        } catch (Exception e) {
            logger.error("Error durante creación de producto: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Busca un producto por nombre
     *
     * @param nombreProducto Nombre del producto a buscar
     * @return true si se encontró el producto
     */
    public boolean buscarProducto(String nombreProducto) {
        try {
            logger.info("Buscando producto: {}", nombreProducto);

            // Escribir en campo de búsqueda
            escribirTextoSeguro(campoBusqueda, nombreProducto);

            // Hacer clic en buscar
            hacerClicSeguro(botonBuscar);

            // Esperar a que se actualice la tabla
            esperarTiempo(1000);

            // Verificar si el producto aparece en los resultados
            List<String> productosEncontrados = obtenerProductosMostrados();
            boolean encontrado = productosEncontrados.stream()
                    .anyMatch(nombre -> nombre.toLowerCase().contains(nombreProducto.toLowerCase()));

            logger.info("Producto {} {}", nombreProducto, encontrado ? "encontrado" : "no encontrado");
            return encontrado;

        } catch (Exception e) {
            logger.error("Error durante búsqueda de producto: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Edita un producto existente
     *
     * @param nombreProductoOriginal Nombre del producto a editar
     * @param productosNuevosDatos Nuevos datos del producto
     * @return true si la edición fue exitosa
     */
    public boolean editarProducto(String nombreProductoOriginal, ProductoCrud productosNuevosDatos) {
        try {
            logger.info("Editando producto: {} -> {}", nombreProductoOriginal, productosNuevosDatos.getNombre());

            // Buscar y seleccionar el producto
            if (!buscarYSeleccionarProducto(nombreProductoOriginal)) {
                logger.warn("No se pudo encontrar el producto para editar: {}", nombreProductoOriginal);
                return false;
            }

            // Llenar formulario con nuevos datos
            llenarFormularioProducto(productosNuevosDatos);

            // Guardar cambios
            hacerClicSeguro(botonGuardar);

            // Verificar mensaje de éxito
            boolean exitoso = esperarMensajeExito();

            if (exitoso) {
                logger.info("Producto editado exitosamente");
            } else {
                logger.warn("Error al editar producto");
            }

            return exitoso;

        } catch (Exception e) {
            logger.error("Error durante edición de producto: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un producto
     *
     * @param nombreProducto Nombre del producto a eliminar
     * @return true si la eliminación fue exitosa
     */
    public boolean eliminarProducto(String nombreProducto) {
        try {
            logger.info("Eliminando producto: {}", nombreProducto);

            // Buscar y hacer clic en botón eliminar del producto
            WebElement botonEliminar = encontrarBotonEliminarProducto(nombreProducto);
            if (botonEliminar == null) {
                logger.warn("No se encontró botón eliminar para producto: {}", nombreProducto);
                return false;
            }

            // Hacer clic en eliminar
            hacerClicSeguro(botonEliminar);

            // Confirmar eliminación en modal
            if (estaElementoVisible(modalConfirmacion)) {
                hacerClicSeguro(botonConfirmarEliminar);
            }

            // Verificar mensaje de éxito
            boolean exitoso = esperarMensajeExito();

            if (exitoso) {
                logger.info("Producto eliminado exitosamente: {}", nombreProducto);
            } else {
                logger.warn("Error al eliminar producto: {}", nombreProducto);
            }

            return exitoso;

        } catch (Exception e) {
            logger.error("Error durante eliminación de producto: {}", e.getMessage());
            return false;
        }
    }

    // Métodos de utilidad

    /**
     * Llena el formulario de producto con los datos proporcionados
     *
     * @param producto ProductoCrud con los datos
     */
    private void llenarFormularioProducto(ProductoCrud producto) {
        if (producto.getNombre() != null) {
            escribirTextoSeguro(campoNombre, producto.getNombre());
        }

        if (producto.getDescripcion() != null) {
            escribirTextoSeguro(campoDescripcion, producto.getDescripcion());
        }

        if (producto.getPrecio() != null) {
            escribirTextoSeguro(campoPrecio, producto.getPrecio().toString());
        }

        if (producto.getCategoria() != null) {
            escribirTextoSeguro(campoCategoria, producto.getCategoria());
        }

        if (producto.getStock() != null) {
            escribirTextoSeguro(campoStock, producto.getStock().toString());
        }

        logger.debug("Formulario llenado con datos del producto");
    }

    /**
     * Busca y selecciona un producto en la tabla
     *
     * @param nombreProducto Nombre del producto a seleccionar
     * @return true si se seleccionó correctamente
     */
    private boolean buscarYSeleccionarProducto(String nombreProducto) {
        try {
            // Primero buscar el producto
            if (!buscarProducto(nombreProducto)) {
                return false;
            }

            // Encontrar y hacer clic en la fila del producto
            for (WebElement fila : filasProductos) {
                if (fila.getText().toLowerCase().contains(nombreProducto.toLowerCase())) {
                    hacerClicSeguro(fila);
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            logger.error("Error buscando y seleccionando producto: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Encuentra el botón eliminar de un producto específico
     *
     * @param nombreProducto Nombre del producto
     * @return WebElement del botón eliminar o null si no se encuentra
     */
    private WebElement encontrarBotonEliminarProducto(String nombreProducto) {
        try {
            // Buscar el producto primero
            if (!buscarProducto(nombreProducto)) {
                return null;
            }

            // Buscar botón eliminar en la fila correspondiente
            for (WebElement fila : filasProductos) {
                if (fila.getText().toLowerCase().contains(nombreProducto.toLowerCase())) {
                    return fila.findElement(By.cssSelector(".btn-eliminar, .btn-delete, [title='Eliminar']"));
                }
            }

        } catch (Exception e) {
            logger.error("Error encontrando botón eliminar: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Obtiene la lista de productos mostrados en la tabla
     *
     * @return Lista con los nombres de los productos
     */
    public List<String> obtenerProductosMostrados() {
        List<String> productos = new ArrayList<>();

        try {
            for (WebElement fila : filasProductos) {
                if (fila.isDisplayed()) {
                    // Obtener el nombre del producto (generalmente primera columna)
                    WebElement columnaNombre = fila.findElement(By.cssSelector("td:first-child"));
                    String nombre = obtenerTextoSeguro(columnaNombre);
                    if (!nombre.isEmpty()) {
                        productos.add(nombre);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error obteniendo productos mostrados: {}", e.getMessage());
        }

        logger.debug("Productos mostrados: {}", productos.size());
        return productos;
    }

    /**
     * Limpia el formulario de producto
     */
    public void limpiarFormulario() {
        try {
            hacerClicSeguro(botonLimpiar);
            logger.debug("Formulario limpiado");
        } catch (Exception e) {
            logger.error("Error limpiando formulario: {}", e.getMessage());
        }
    }

    // Métodos para verificar mensajes

    /**
     * Verifica si hay un mensaje de éxito visible
     *
     * @return true si hay mensaje de éxito
     */
    public boolean hayMensajeExito() {
        return estaElementoVisible(mensajeExito);
    }

    /**
     * Verifica si hay un mensaje de error visible
     *
     * @return true si hay mensaje de error
     */
    public boolean hayMensajeError() {
        return estaElementoVisible(mensajeError);
    }

    /**
     * Verifica si hay un mensaje de validación visible
     *
     * @return true si hay mensaje de validación
     */
    public boolean hayMensajeValidacion() {
        return estaElementoVisible(mensajeValidacion);
    }

    /**
     * Obtiene el texto del mensaje de éxito
     *
     * @return Texto del mensaje de éxito
     */
    public String obtenerMensajeExito() {
        return obtenerTextoSeguro(mensajeExito);
    }

    /**
     * Obtiene el texto del mensaje de error
     *
     * @return Texto del mensaje de error
     */
    public String obtenerMensajeError() {
        return obtenerTextoSeguro(mensajeError);
    }

    /**
     * Obtiene el texto del mensaje de validación
     *
     * @return Texto del mensaje de validación
     */
    public String obtenerMensajeValidacion() {
        return obtenerTextoSeguro(mensajeValidacion);
    }

    /**
     * Espera a que aparezca un mensaje de éxito
     *
     * @return true si apareció el mensaje
     */
    private boolean esperarMensajeExito() {
        try {
            esperarElementoVisible(mensajeExito);
            return true;
        } catch (Exception e) {
            logger.debug("No apareció mensaje de éxito en el tiempo esperado");
            return false;
        }
    }

    /**
     * Verifica si la tabla de productos está vacía
     *
     * @return true si no hay productos mostrados
     */
    public boolean estaTablaVacia() {
        return filasProductos.isEmpty() ||
                filasProductos.stream().noneMatch(WebElement::isDisplayed);
    }

    /**
     * Obtiene el número de productos mostrados
     *
     * @return Cantidad de productos en la tabla
     */
    public int obtenerCantidadProductos() {
        return obtenerProductosMostrados().size();
    }
}