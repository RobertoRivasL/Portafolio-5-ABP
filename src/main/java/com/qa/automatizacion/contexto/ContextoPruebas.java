package com.qa.automatizacion.contexto;

import com.qa.automatizacion.modelo.Usuario;
import com.qa.automatizacion.modelo.ProductoCrud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contexto compartido para almacenar datos entre los pasos de las pruebas.
 * Implementa el patrón Singleton para garantizar una única instancia por ejecución.
 *
 * Principios aplicados:
 * - Singleton: Una sola instancia compartida
 * - Encapsulación: Datos privados con acceso controlado
 * - Responsabilidad Única: Se enfoca solo en gestionar el contexto
 *
 * @author Equipo QA Automatización
 * @version 1.0
 */
public class ContextoPruebas {

    private static final Logger logger = LoggerFactory.getLogger(ContextoPruebas.class);
    private static ContextoPruebas instancia;

    // Almacén de datos del contexto
    private final Map<String, Object> almacenDatos;

    // Usuario actual de la sesión
    private Usuario usuarioActual;

    // Producto actual en operaciones CRUD
    private ProductoCrud productoActual;

    /**
     * Constructor privado para implementar Singleton
     */
    private ContextoPruebas() {
        this.almacenDatos = new HashMap<>();
        logger.debug("Contexto de pruebas inicializado");
    }

    /**
     * Obtiene la instancia única del contexto (Singleton)
     *
     * @return Instancia del contexto
     */
    public static synchronized ContextoPruebas obtenerInstancia() {
        if (instancia == null) {
            instancia = new ContextoPruebas();
        }
        return instancia;
    }

    /**
     * Almacena un dato en el contexto
     *
     * @param clave Clave identificadora del dato
     * @param valor Valor a almacenar
     */
    public void almacenarDato(String clave, Object valor) {
        if (clave == null || clave.trim().isEmpty()) {
            throw new IllegalArgumentException("La clave no puede ser nula o vacía");
        }

        almacenDatos.put(clave, valor);
        logger.debug("Dato almacenado en contexto: {} = {}", clave, valor);
    }

    /**
     * Obtiene un dato del contexto
     *
     * @param clave Clave del dato a obtener
     * @return Valor almacenado o null si no existe
     */
    public Object obtenerDato(String clave) {
        if (clave == null || clave.trim().isEmpty()) {
            logger.warn("Intento de obtener dato con clave nula o vacía");
            return null;
        }

        Object valor = almacenDatos.get(clave);
        logger.debug("Dato obtenido del contexto: {} = {}", clave, valor);
        return valor;
    }

    /**
     * Obtiene un dato del contexto con tipo específico
     *
     * @param <T> Tipo esperado del dato
     * @param clave Clave del dato
     * @param tipoClase Clase del tipo esperado
     * @return Optional con el valor tipado o vacío si no existe o no es del tipo correcto
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> obtenerDatoTipado(String clave, Class<T> tipoClase) {
        Object valor = obtenerDato(clave);

        if (valor == null) {
            return Optional.empty();
        }

        if (tipoClase.isInstance(valor)) {
            return Optional.of((T) valor);
        } else {
            logger.warn("El dato '{}' no es del tipo esperado: {}. Tipo actual: {}",
                    clave, tipoClase.getSimpleName(), valor.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    /**
     * Verifica si existe un dato con la clave especificada
     *
     * @param clave Clave a verificar
     * @return true si existe, false en caso contrario
     */
    public boolean existeDato(String clave) {
        return almacenDatos.containsKey(clave);
    }

    /**
     * Elimina un dato del contexto
     *
     * @param clave Clave del dato a eliminar
     * @return Valor eliminado o null si no existía
     */
    public Object eliminarDato(String clave) {
        Object valorEliminado = almacenDatos.remove(clave);
        logger.debug("Dato eliminado del contexto: {} = {}", clave, valorEliminado);
        return valorEliminado;
    }

    /**
     * Establece el usuario actual de la sesión
     *
     * @param usuario Usuario a establecer como actual
     */
    public void establecerUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        almacenarDato("usuarioActual", usuario);
        logger.info("Usuario actual establecido: {}",
                usuario != null ? usuario.getNombreUsuario() : "null");
    }

    /**
     * Obtiene el usuario actual de la sesión
     *
     * @return Usuario actual o null si no hay sesión activa
     */
    public Usuario obtenerUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Establece el producto actual para operaciones CRUD
     *
     * @param producto Producto a establecer como actual
     */
    public void establecerProductoActual(ProductoCrud producto) {
        this.productoActual = producto;
        almacenarDato("productoActual", producto);
        logger.info("Producto actual establecido: {}",
                producto != null ? producto.getNombre() : "null");
    }

    /**
     * Obtiene el producto actual
     *
     * @return Producto actual o null si no hay uno establecido
     */
    public ProductoCrud obtenerProductoActual() {
        return productoActual;
    }

    /**
     * Verifica si hay un usuario autenticado
     *
     * @return true si hay un usuario actual, false en caso contrario
     */
    public boolean hayUsuarioAutenticado() {
        return usuarioActual != null;
    }

    /**
     * Limpia todo el contexto de datos
     */
    public void limpiarContexto() {
        almacenDatos.clear();
        usuarioActual = null;
        productoActual = null;
        logger.debug("Contexto de pruebas limpiado completamente");
    }

    /**
     * Limpia solo los datos temporales, manteniendo usuario y producto actuales
     */
    public void limpiarDatosTemporales() {
        Map<String, Object> datosEsenciales = new HashMap<>();
        datosEsenciales.put("usuarioActual", usuarioActual);
        datosEsenciales.put("productoActual", productoActual);

        almacenDatos.clear();
        almacenDatos.putAll(datosEsenciales);

        logger.debug("Datos temporales del contexto limpiados");
    }

    /**
     * Obtiene el número de elementos almacenados en el contexto
     *
     * @return Cantidad de elementos en el contexto
     */
    public int obtenerTamanoContexto() {
        return almacenDatos.size();
    }

    /**
     * Obtiene todas las claves almacenadas en el contexto
     *
     * @return Set con todas las claves
     */
    public java.util.Set<String> obtenerClaves() {
        return almacenDatos.keySet();
    }

    /**
     * Genera un resumen del contenido actual del contexto
     *
     * @return String con el resumen del contexto
     */
    public String generarResumenContexto() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("=== Resumen del Contexto de Pruebas ===\n");
        resumen.append("Total de elementos: ").append(almacenDatos.size()).append("\n");
        resumen.append("Usuario actual: ").append(usuarioActual != null ? usuarioActual.getNombreUsuario() : "Ninguno").append("\n");
        resumen.append("Producto actual: ").append(productoActual != null ? productoActual.getNombre() : "Ninguno").append("\n");
        resumen.append("Datos almacenados:\n");

        almacenDatos.forEach((clave, valor) -> {
            resumen.append("  - ").append(clave).append(": ")
                    .append(valor != null ? valor.getClass().getSimpleName() : "null")
                    .append("\n");
        });

        return resumen.toString();
    }
}