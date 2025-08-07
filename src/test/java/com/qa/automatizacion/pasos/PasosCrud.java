package com.qa.automatizacion.pasos;

import com.qa.automatizacion.utilidades.ContextoPrueba;
import com.qa.automatizacion.modelo.ProductoCrud;
import com.qa.automatizacion.paginas.PaginaCrud;
import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import io.cucumber.java.es.*;
import io.cucumber.datatable.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions para las operaciones CRUD
 * Implementa los pasos de Gherkin para las pruebas de funcionalidad CRUD
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
public class PasosCrud {

    private static final Logger logger = LoggerFactory.getLogger(PasosCrud.class);

    private final ContextoPrueba contexto;
    private PaginaCrud paginaCrud;
    private ProductoCrud productoActual;
    private final PropiedadesAplicacion propiedades;

    public PasosCrud() {
        this.contexto = ContextoPrueba.obtenerInstancia();
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();
        logger.info("PasosCrud inicializado");
    }

    // ✅ 3. CORREGIR los métodos para usar las clases correctas:

    @Dado("que estoy en la página de gestión CRUD")
    public void queEstoyEnLaPaginaDeGestionCrud() {
        paginaCrud = new PaginaCrud();

        // Navegar a la página CRUD usando ConfiguradorNavegador
        String urlCrud = propiedades.obtenerUrlBase() + "/productos";  // Asumir URL de productos
        ConfiguradorNavegador.navegarA(urlCrud);

        assertTrue(paginaCrud.estaPaginaCargada(), "La página CRUD no se cargó correctamente");
        logger.info("Usuario navegó a la página de gestión CRUD");
    }

    @Dado("que existe un producto con los siguientes datos:")
    public void queExisteUnProductoConLosSiguientesDatos(DataTable datosProducto) {
        Map<String, String> datos = datosProducto.asMap();
        ProductoCrud producto = crearProductoDesdeMap(datos);

        // Asegurar que estamos en la página CRUD
        if (paginaCrud == null) {
            queEstoyEnLaPaginaDeGestionCrud();
        }

        boolean creado = paginaCrud.crearProducto(producto);
        assertTrue(creado, "No se pudo crear el producto prerequisito");

        contexto.almacenarDato("productoExistente", producto);
        logger.info("Producto prerequisito creado: {}", producto.getNombre());
    }

    // When - Acciones

    @Cuando("creo un nuevo producto con los siguientes datos:")
    public void creoUnNuevoProductoConLosSiguientesDatos(DataTable datosProducto) {
        Map<String, String> datos = datosProducto.asMap();
        ProductoCrud producto = crearProductoDesdeMap(datos);

        if (paginaCrud == null) {
            queEstoyEnLaPaginaDeGestionCrud();
        }

        boolean resultado = paginaCrud.crearProducto(producto);

        contexto.almacenarDato("ultimoProducto", producto);
        contexto.almacenarDato("resultadoOperacion", resultado);

        logger.info("Intento de creación de producto: {} - Resultado: {}",
                producto.getNombre(), resultado);
    }

    @Cuando("busco un producto por nombre {string}")
    public void buscoUnProductoPorNombre(String nombreProducto) {
        if (paginaCrud == null) {
            queEstoyEnLaPaginaDeGestionCrud();
        }

        boolean encontrado = paginaCrud.buscarProducto(nombreProducto);

        contexto.almacenarDato("productoBuscado", nombreProducto);
        contexto.almacenarDato("productoEncontrado", encontrado);

        logger.info("Búsqueda de producto: {} - Encontrado: {}", nombreProducto, encontrado);
    }

    @Cuando("edito el producto {string} con los siguientes datos:")
    public void editoElProductoConLosSiguientesDatos(String nombreProducto, DataTable nuevosdatos) {
        Map<String, String> datos = nuevosdatos.asMap();
        ProductoCrud productoEditado = crearProductoDesdeMap(datos);

        if (paginaCrud == null) {
            queEstoyEnLaPaginaDeGestionCrud();
        }

        // Primero buscar el producto
        boolean encontrado = paginaCrud.buscarProducto(nombreProducto);
        assertTrue(encontrado, "No se encontró el producto a editar: " + nombreProducto);

        // Luego editarlo
        boolean resultado = paginaCrud.editarProducto(nombreProducto, productoEditado);

        contexto.almacenarDato("productoEditado", productoEditado);
        contexto.almacenarDato("resultadoOperacion", resultado);

        logger.info("Edición de producto: {} - Resultado: {}", nombreProducto, resultado);
    }

    @Cuando("elimino el producto {string}")
    public void eliminoElProducto(String nombreProducto) {
        if (paginaCrud == null) {
            queEstoyEnLaPaginaDeGestionCrud();
        }

        // Primero buscar el producto
        boolean encontrado = paginaCrud.buscarProducto(nombreProducto);
        assertTrue(encontrado, "No se encontró el producto a eliminar: " + nombreProducto);

        // Luego eliminarlo
        boolean resultado = paginaCrud.eliminarProducto(nombreProducto);

        contexto.almacenarDato("productoEliminado", nombreProducto);
        contexto.almacenarDato("resultadoOperacion", resultado);

        logger.info("Eliminación de producto: {} - Resultado: {}", nombreProducto, resultado);
    }

    @Cuando("intento crear un producto sin el campo obligatorio {string}")
    public void intentoCrearUnProductoSinElCampoObligatorio(String campoObligatorio) {
        if (paginaCrud == null) {
            queEstoyEnLaPaginaDeGestionCrud();
        }

        // Crear producto con campo faltante
        ProductoCrud productoIncompleto = ProductoCrud.builder()
                .nombre(campoObligatorio.equals("nombre") ? "" : "Producto Test")
                .descripcion(campoObligatorio.equals("descripcion") ? "" : "Descripción test")
                .precio(campoObligatorio.equals("precio") ? null : BigDecimal.valueOf(100.0))
                .categoria(campoObligatorio.equals("categoria") ? "" : "Electrónicos")
                .build();

        boolean resultado = paginaCrud.crearProducto(productoIncompleto);

        contexto.almacenarDato("resultadoOperacion", resultado);
        contexto.almacenarDato("campoFaltante", campoObligatorio);

        logger.info("Intento de creación con campo faltante: {} - Resultado: {}",
                campoObligatorio, resultado);
    }

    @Cuando("busco productos que contengan {string} en el nombre")
    public void buscoProductosQueContenganEnElNombre(String textoBusqueda) {
        if (paginaCrud == null) {
            queEstoyEnLaPaginaDeGestionCrud();
        }

        boolean encontrados = paginaCrud.buscarProducto(textoBusqueda);
        List<String> productosEncontrados = paginaCrud.obtenerProductosMostrados();

        contexto.almacenarDato("textoBusqueda", textoBusqueda);
        contexto.almacenarDato("productosEncontrados", productosEncontrados);

        logger.info("Búsqueda por texto: {} - Productos encontrados: {}",
                textoBusqueda, productosEncontrados.size());
    }

    // Then - Verificaciones

    @Entonces("el producto debería crearse exitosamente")
    public void elProductoDeberiaCrearseExitosamente() {
        Boolean resultado = (Boolean) contexto.obtenerDato("resultadoOperacion");
        assertTrue(resultado, "El producto no se creó exitosamente");

        // Verificar que el producto aparece en la lista
        ProductoCrud producto = (ProductoCrud) contexto.obtenerDato("ultimoProducto");
        boolean encontrado = paginaCrud.buscarProducto(producto.getNombre());
        assertTrue(encontrado, "El producto creado no aparece en la lista");

        logger.info("Verificación exitosa: producto creado correctamente");
    }

    @Entonces("debería ver el producto en los resultados de búsqueda")
    public void deberiaVerElProductoEnLosResultadosDeBusqueda() {
        Boolean encontrado = (Boolean) contexto.obtenerDato("productoEncontrado");
        assertTrue(encontrado, "El producto no fue encontrado en los resultados");

        String nombreBuscado = (String) contexto.obtenerDato("productoBuscado");
        List<String> productosEncontrados = paginaCrud.obtenerProductosMostrados();
        assertTrue(productosEncontrados.contains(nombreBuscado),
                "El producto buscado no está en la lista de resultados");

        logger.info("Verificación exitosa: producto encontrado en búsqueda");
    }

    @Entonces("los datos del producto deberían actualizarse correctamente")
    public void losDatosDelProductoDeberianActualizarseCorrectamente() {
        Boolean resultado = (Boolean) contexto.obtenerDato("resultadoOperacion");
        assertTrue(resultado, "La actualización del producto falló");

        // Verificar que los nuevos datos están presentes
        ProductoCrud productoEditado = (ProductoCrud) contexto.obtenerDato("productoEditado");
        boolean encontrado = paginaCrud.buscarProducto(productoEditado.getNombre());
        assertTrue(encontrado, "El producto con los nuevos datos no fue encontrado");

        logger.info("Verificación exitosa: producto actualizado correctamente");
    }

    @Entonces("el producto debería ser eliminado del sistema")
    public void elProductoDeberiaSerEliminadoDelSistema() {
        Boolean resultado = (Boolean) contexto.obtenerDato("resultadoOperacion");
        assertTrue(resultado, "La eliminación del producto falló");

        // Verificar que el producto ya no existe
        String nombreEliminado = (String) contexto.obtenerDato("productoEliminado");
        boolean encontrado = paginaCrud.buscarProducto(nombreEliminado);
        assertFalse(encontrado, "El producto eliminado aún aparece en el sistema");

        logger.info("Verificación exitosa: producto eliminado correctamente");
    }

    @Entonces("debería ver un mensaje de error de validación")
    public void deberiaVerUnMensajeDeErrorDeValidacion() {
        assertTrue(paginaCrud.hayMensajeValidacion() || paginaCrud.hayMensajeError(),
                "No se mostró mensaje de validación");

        String campoFaltante = (String) contexto.obtenerDato("campoFaltante");
        String mensajeError = paginaCrud.hayMensajeValidacion() ?
                paginaCrud.obtenerMensajeValidacion() :
                paginaCrud.obtenerMensajeError();

        assertTrue(mensajeError.toLowerCase().contains(campoFaltante.toLowerCase()) ||
                        mensajeError.contains("obligatorio") ||
                        mensajeError.contains("requerido"),
                "El mensaje de error no indica el campo faltante");

        logger.info("Verificación exitosa: mensaje de validación mostrado para campo: {}", campoFaltante);
    }

    @Entonces("no debería crearse el producto")
    public void noDeberiaCrearseElProducto() {
        Boolean resultado = (Boolean) contexto.obtenerDato("resultadoOperacion");
        assertFalse(resultado, "El producto se creó cuando no debería haberse creado");

        logger.info("Verificación exitosa: producto no fue creado como se esperaba");
    }

    @Entonces("debería ver {int} producto(s) en los resultados")
    public void deberiaVerProductosEnLosResultados(int cantidadEsperada) {
        @SuppressWarnings("unchecked")
        List<String> productosEncontrados = (List<String>) contexto.obtenerDato("productosEncontrados");

        assertEquals(cantidadEsperada, productosEncontrados.size(),
                String.format("Se esperaban %d productos pero se encontraron %d",
                        cantidadEsperada, productosEncontrados.size()));

        logger.info("Verificación exitosa: cantidad correcta de productos encontrados: {}", cantidadEsperada);
    }

    @Entonces("todos los productos mostrados deberían contener {string} en el nombre")
    public void todosLosProductosMostradosDeberianContenerEnElNombre(String textoEsperado) {
        @SuppressWarnings("unchecked")
        List<String> productosEncontrados = (List<String>) contexto.obtenerDato("productosEncontrados");

        assertTrue(productosEncontrados.stream()
                        .allMatch(nombre -> nombre.toLowerCase().contains(textoEsperado.toLowerCase())),
                "No todos los productos contienen el texto buscado en el nombre");

        logger.info("Verificación exitosa: todos los productos contienen '{}' en el nombre", textoEsperado);
    }

    // Métodos auxiliares privados

    /**
     * Crea un objeto ProductoCrud a partir de un Map de datos
     *
     * @param datos Map con los datos del producto
     * @return ProductoCrud creado
     */
    private ProductoCrud crearProductoDesdeMap(Map<String, String> datos) {
        return ProductoCrud.builder()
                .nombre(datos.get("nombre"))
                .descripcion(datos.get("descripcion"))
                .precio(new BigDecimal(datos.get("precio")))
                .categoria(datos.get("categoria"))
                .build();
    }
}