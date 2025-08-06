package com.automatizacion.pruebas.pasos;

import com.automatizacion.pruebas.contexto.ContextoPrueba;
import com.automatizacion.pruebas.modelos.ProductoCrud;
import com.automatizacion.pruebas.paginas.PaginaCrud;
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

    public PasosCrud(ContextoPrueba contexto) {
        this.contexto = contexto;
        logger.info("PasosCrud inicializado");
    }

    // Given - Condiciones iniciales

    @Dado("que estoy en la página de gestión CRUD")
    public void queEstoyEnLaPaginaDeGestionCrud() {
        paginaCrud = new PaginaCrud(contexto.getNavegador());
        paginaCrud.navegarAPaginaCrud(contexto.getConfiguracion().getUrlBase());
        logger.info("Usuario navegó a la página de gestión CRUD");
    }

    @Dado("que existe un producto con los siguientes datos:")
    public void queExisteUnProductoConLosSiguientesDatos(DataTable datosProducto) {
        Map<String, String> datos = datosProducto.asMap();
        ProductoCrud producto = crearProductoDesdeMap(datos);

        boolean creado = paginaCrud.crearProducto(producto);
        assertTrue(creado, "No se pudo crear el producto prerequisito");

        contexto.almacenarDato("productoExistente", producto);
        logger.info("Producto prerequisito creado: {}", producto.getNombre());
    }

    @Dado("que no existe ningún producto en el sistema")
    public void queNoExisteNingunProductoEnElSistema() {
        // En un escenario real, aquí se limpiaría la base de datos
        // Por ahora, asumimos que la página está limpia
        logger.info("Sistema limpio - no hay productos existentes");
    }

    // When - Acciones del usuario

    @Cuando("creo un nuevo producto con los siguientes datos:")
    public void creoUnNuevoProductoConLosSiguientesDatos(DataTable datosProducto) {
        Map<String, String> datos = datosProducto.asMap();
        productoActual = crearProductoDesdeMap(datos);

        boolean resultado = paginaCrud.crearProducto(productoActual);
        contexto.almacenarDato("resultadoOperacion", resultado);
        contexto.almacenarDato("ultimoProducto", productoActual);

        logger.info("Intento de creación de producto: {} - Resultado: {}",
                productoActual.getNombre(), resultado);
    }

    @Cuando("busco el producto {string}")
    public void buscoElProducto(String nombreProducto) {
        boolean encontrado = paginaCrud.buscarProducto(nombreProducto);
        contexto.almacenarDato("productoBuscado", nombreProducto);
        contexto.almacenarDato("productoEncontrado", encontrado);

        logger.info("Búsqueda de producto: {} - Encontrado: {}", nombreProducto, encontrado);
    }

    @Cuando("edito el producto {string} con los siguientes datos:")
    public void editoElProductoConLosSiguientesDatos(String nombreOriginal, DataTable datosNuevos) {
        Map<String, String> datos = datosNuevos.asMap();
        ProductoCrud productoActualizado = crearProductoDesdeMap(datos);

        boolean resultado = paginaCrud.editarProducto(nombreOriginal, productoActualizado);
        contexto.almacenarDato("resultadoOperacion", resultado);
        contexto.almacenarDato("productoEditado", productoActualizado);

        logger.info("Edición de producto: {} -> {} - Resultado: {}",
                nombreOriginal, productoActualizado.getNombre(), resultado);
    }

    @Cuando("elimino el producto {string}")
    public void eliminoElProducto(String nombreProducto) {
        boolean resultado = paginaCrud.eliminarProducto(nombreProducto);
        contexto.almacenarDato("resultadoOperacion", resultado);
        contexto.almacenarDato("productoEliminado", nombreProducto);

        logger.info("Eliminación de producto: {} - Resultado: {}", nombreProducto, resultado);
    }

    @Cuando("intento crear un producto sin proporcionar el campo obligatorio {string}")
    public void intentoCrearUnProductoSinProporcionarElCampoObligatorio(String campoObligatorio) {
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