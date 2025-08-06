package com.automatizacion.pruebas.paginas;

import com.automatizacion.pruebas.modelos.Usuario;
import com.automatizacion.pruebas.modelos.ProductoCrud;
import com.automatizacion.pruebas.utilidades.ManejadorEsperas;
import com.automatizacion.pruebas.utilidades.AccionesComunes;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Page Object para las operaciones CRUD de la aplicación
 * Implementa el patrón Page Object Model siguiendo principios SOLID
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
public class PaginaCrud {

    private static final Logger logger = LoggerFactory.getLogger(PaginaCrud.class);

    private final WebDriver navegador;
    private final ManejadorEsperas manejadorEsperas;
    private final AccionesComunes accionesComunes;

    // Selectores para formularios CRUD
    @FindBy(id = "btnNuevo")
    private WebElement botonNuevo;

    @FindBy(id = "btnGuardar")
    private WebElement botonGuardar;

    @FindBy(id = "btnEditar")
    private WebElement botonEditar;

    @FindBy(id = "btnEliminar")
    private WebElement botonEliminar;

    @FindBy(id = "btnBuscar")
    private WebElement botonBuscar;

    // Campos del formulario
    @FindBy(id = "inputNombre")
    private WebElement campoNombre;

    @FindBy(id = "inputDescripcion")
    private WebElement campoDescripcion;

    @FindBy(id = "inputPrecio")
    private WebElement campoPrecio;

    @FindBy(id = "inputCategoria")
    private WebElement campoCategoria;

    @FindBy(id = "campoBusqueda")
    private WebElement campoBusqueda;

    // Tabla de resultados
    @FindBy(id = "tablaResultados")
    private WebElement tablaResultados;

    @FindBy(css = "tbody tr")
    private List<WebElement> filasTabla;

    // Mensajes de retroalimentación
    @FindBy(id = "mensajeExito")
    private WebElement mensajeExito;

    @FindBy(id = "mensajeError")
    private WebElement mensajeError;

    @FindBy(id = "mensajeValidacion")
    private WebElement mensajeValidacion;

    /**
     * Constructor que inicializa la página CRUD
     *
     * @param navegador Driver de Selenium
     */
    public PaginaCrud(WebDriver navegador) {
        this.navegador = navegador;
        this.manejadorEsperas = new ManejadorEsperas(navegador);
        this.accionesComunes = new AccionesComunes(navegador);
        PageFactory.initElements(navegador, this);
        logger.info("PaginaCrud inicializada correctamente");
    }

    /**
     * Navega a la página CRUD
     *
     * @param urlBase URL base de la aplicación
     */
    public void navegarAPaginaCrud(String urlBase) {
        String urlCrud = urlBase + "/crud";
        navegador.get(urlCrud);
        manejadorEsperas.esperarElementoVisible(botonNuevo);
        logger.info("Navegando a página CRUD: {}", urlCrud);
    }

    /**
     * Crea un nuevo producto
     *
     * @param producto Datos del producto a crear
     * @return true si la creación fue exitosa
     */
    public boolean crearProducto(ProductoCrud producto) {
        try {
            logger.info("Iniciando creación de producto: {}", producto.getNombre());

            accionesComunes.hacerClickSeguro(botonNuevo);
            manejadorEsperas.esperarElementoVisible(campoNombre);

            llenarFormularioProducto(producto);
            accionesComunes.hacerClickSeguro(botonGuardar);

            return esperarOperacionExitosa("Producto creado exitosamente");

        } catch (Exception e) {
            logger.error("Error al crear producto: {}", e.getMessage());
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

            accionesComunes.escribirTextoSeguro(campoBusqueda, nombreProducto);
            accionesComunes.hacerClickSeguro(botonBuscar);

            manejadorEsperas.esperarElementoVisible(tablaResultados);

            return verificarProductoEnTabla(nombreProducto);

        } catch (Exception e) {
            logger.error("Error al buscar producto: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Edita un producto existente
     *
     * @param nombreOriginal Nombre original del producto
     * @param productoActualizado Datos actualizados del producto
     * @return true si la edición fue exitosa
     */
    public boolean editarProducto(String nombreOriginal, ProductoCrud productoActualizado) {
        try {
            logger.info("Editando producto: {} -> {}", nombreOriginal, productoActualizado.getNombre());

            if (!buscarYSeleccionarProducto(nombreOriginal)) {
                return false;
            }

            accionesComunes.hacerClickSeguro(botonEditar);
            manejadorEsperas.esperarElementoVisible(campoNombre);

            llenarFormularioProducto(productoActualizado);
            accionesComunes.hacerClickSeguro(botonGuardar);

            return esperarOperacionExitosa("Producto actualizado exitosamente");

        } catch (Exception e) {
            logger.error("Error al editar producto: {}", e.getMessage());
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

            if (!buscarYSeleccionarProducto(nombreProducto)) {
                return false;
            }

            accionesComunes.hacerClickSeguro(botonEliminar);

            // Confirmar eliminación si aparece modal de confirmación
            confirmarEliminacion();

            return esperarOperacionExitosa("Producto eliminado exitosamente");

        } catch (Exception e) {
            logger.error("Error al eliminar producto: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la lista de productos mostrados en la tabla
     *
     * @return Lista de nombres de productos
     */
    public List<String> obtenerProductosMostrados() {
        manejadorEsperas.esperarElementoVisible(tablaResultados);

        return filasTabla.stream()
                .map(fila -> fila.findElement(By.cssSelector("td:first-child")).getText())
                .toList();
    }

    /**
     * Verifica si existe un mensaje de error
     *
     * @return true si hay un mensaje de error visible
     */
    public boolean hayMensajeError() {
        return accionesComunes.esElementoVisible(mensajeError);
    }

    /**
     * Obtiene el texto del mensaje de error
     *
     * @return Texto del mensaje de error
     */
    public String obtenerMensajeError() {
        if (hayMensajeError()) {
            return mensajeError.getText();
        }
        return "";
    }

    /**
     * Verifica si hay un mensaje de validación
     *
     * @return true si hay un mensaje de validación visible
     */
    public boolean hayMensajeValidacion() {
        return accionesComunes.esElementoVisible(mensajeValidacion);
    }

    /**
     * Obtiene el texto del mensaje de validación
     *
     * @return Texto del mensaje de validación
     */
    public String obtenerMensajeValidacion() {
        if (hayMensajeValidacion()) {
            return mensajeValidacion.getText();
        }
        return "";
    }

    // Métodos privados de soporte

    /**
     * Llena el formulario con los datos del producto
     *
     * @param producto Datos del producto
     */
    private void llenarFormularioProducto(ProductoCrud producto) {
        accionesComunes.limpiarYEscribir(campoNombre, producto.getNombre());
        accionesComunes.limpiarYEscribir(campoDescripcion, producto.getDescripcion());
        accionesComunes.limpiarYEscribir(campoPrecio, producto.getPrecio().toString());
        accionesComunes.seleccionarOpcion(campoCategoria, producto.getCategoria());
    }

    /**
     * Busca y selecciona un producto en la tabla
     *
     * @param nombreProducto Nombre del producto a seleccionar
     * @return true si se pudo seleccionar el producto
     */
    private boolean buscarYSeleccionarProducto(String nombreProducto) {
        if (!buscarProducto(nombreProducto)) {
            return false;
        }

        Optional<WebElement> filaProducto = filasTabla.stream()
                .filter(fila -> fila.findElement(By.cssSelector("td:first-child"))
                        .getText().equals(nombreProducto))
                .findFirst();

        if (filaProducto.isPresent()) {
            accionesComunes.hacerClickSeguro(filaProducto.get());
            return true;
        }

        return false;
    }

    /**
     * Verifica si un producto está presente en la tabla
     *
     * @param nombreProducto Nombre del producto a verificar
     * @return true si el producto está en la tabla
     */
    private boolean verificarProductoEnTabla(String nombreProducto) {
        return filasTabla.stream()
                .anyMatch(fila -> fila.findElement(By.cssSelector("td:first-child"))
                        .getText().equals(nombreProducto));
    }

    /**
     * Confirma la eliminación de un producto
     */
    private void confirmarEliminacion() {
        try {
            WebElement modalConfirmacion = navegador.findElement(By.id("modalConfirmacion"));
            if (modalConfirmacion.isDisplayed()) {
                WebElement botonConfirmar = modalConfirmacion.findElement(By.id("btnConfirmarEliminacion"));
                accionesComunes.hacerClickSeguro(botonConfirmar);
            }
        } catch (Exception e) {
            logger.debug("No se encontró modal de confirmación, continuando...");
        }
    }

    /**
     * Espera a que aparezca un mensaje de operación exitosa
     *
     * @param mensajeEsperado Mensaje que se espera ver
     * @return true si aparece el mensaje de éxito
     */
    private boolean esperarOperacionExitosa(String mensajeEsperado) {
        try {
            manejadorEsperas.esperarElementoVisible(mensajeExito);
            return mensajeExito.getText().contains(mensajeEsperado);
        } catch (Exception e) {
            logger.error("No se pudo confirmar operación exitosa: {}", e.getMessage());
            return false;
        }
    }
}