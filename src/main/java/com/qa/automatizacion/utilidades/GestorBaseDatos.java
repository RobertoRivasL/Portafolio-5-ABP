package com.qa.automatizacion.utilidades;

import com.qa.automatizacion.modelo.Usuario;
import com.qa.automatizacion.modelo.ProductoCrud;
import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor para el manejo de datos de prueba y simulación de base de datos.
 * Proporciona datos consistentes y limpios para las pruebas BDD.
 *
 * Principios aplicados:
 * - Singleton: Una sola instancia para mantener consistencia
 * - Encapsulación: Oculta la complejidad del manejo de datos
 * - Separación de Intereses: Se enfoca únicamente en gestión de datos
 * - Integración: Funciona perfectamente con Utileria.java
 *
 * @author Roberto Rivas Lopez
 * @version 1.0
 */
public class GestorBaseDatos {

    private static final Logger logger = LoggerFactory.getLogger(GestorBaseDatos.class);

    // Singleton instance
    private static volatile GestorBaseDatos instancia;

    // Storage simulado thread-safe
    private final Map<String, Usuario> usuariosRegistrados;
    private final Map<String, ProductoCrud> productosDisponibles;
    private final Map<String, Object> cacheDatos;

    // Contadores para generación de IDs únicos
    private int contadorUsuarios = 1000;
    private int contadorProductos = 2000;

    // Configuración
    private final PropiedadesAplicacion propiedades;
    private boolean datosInicializados = false;

    /**
     * Constructor privado para Singleton.
     */
    private GestorBaseDatos() {
        this.usuariosRegistrados = new ConcurrentHashMap<>();
        this.productosDisponibles = new ConcurrentHashMap<>();
        this.cacheDatos = new ConcurrentHashMap<>();
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();

        logger.debug("GestorBaseDatos inicializado");
    }

    /**
     * Obtiene la instancia única del gestor (Singleton).
     *
     * @return instancia del GestorBaseDatos
     */
    public static GestorBaseDatos obtenerInstancia() {
        if (instancia == null) {
            synchronized (GestorBaseDatos.class) {
                if (instancia == null) {
                    instancia = new GestorBaseDatos();
                }
            }
        }
        return instancia;
    }

    // ==================== MÉTODOS PRINCIPALES ====================

    /**
     * Inicializa el gestor y carga datos de prueba básicos.
     */
    public void inicializar() {
        try {
            logger.info("Inicializando GestorBaseDatos...");

            limpiarDatos();
            cargarDatosPrueba();
            cargarUsuariosPredefinidos();
            cargarProductosPredefinidos();

            datosInicializados = true;
            logger.info("GestorBaseDatos inicializado exitosamente");

        } catch (Exception e) {
            logger.error("Error inicializando GestorBaseDatos: {}", e.getMessage());
            throw new RuntimeException("Fallo en inicialización de datos de prueba", e);
        }
    }

    /**
     * Limpia todos los datos almacenados.
     */
    public void limpiarDatos() {
        logger.debug("Limpiando datos de prueba...");

        usuariosRegistrados.clear();
        productosDisponibles.clear();
        cacheDatos.clear();

        // Reset contadores
        contadorUsuarios = 1000;
        contadorProductos = 2000;

        datosInicializados = false;
        logger.debug("Datos de prueba limpiados");
    }

    /**
     * Carga el conjunto completo de datos para pruebas de regresión.
     */
    public void cargarConjuntoCompletoDatos() {
        try {
            logger.info("Cargando conjunto completo de datos...");

            // Cargar usuarios adicionales para regresión
            cargarUsuariosRegresion();

            // Cargar productos adicionales para regresión
            cargarProductosRegresion();

            // Cargar datos de configuración
            cargarDatosConfiguracion();

            logger.info("Conjunto completo de datos cargado");

        } catch (Exception e) {
            logger.error("Error cargando conjunto completo: {}", e.getMessage());
            throw new RuntimeException("Fallo cargando datos de regresión", e);
        }
    }

    // ==================== GESTIÓN DE USUARIOS ====================

    /**
     * Registra un nuevo usuario en el sistema de prueba.
     *
     * @param usuario usuario a registrar
     * @return true si se registró exitosamente
     */
    public boolean registrarUsuario(Usuario usuario) {
        try {
            if (usuario == null || usuario.getEmail() == null) {
                logger.warn("Intento de registrar usuario nulo o sin email");
                return false;
            }

            if (usuariosRegistrados.containsKey(usuario.getEmail())) {
                logger.warn("Usuario ya existe: {}", usuario.getEmail());
                return false;
            }

            // Asignar ID único si no tiene
            if (usuario.getId() == null) {
                usuario.setId(String.valueOf(++contadorUsuarios));
            }

            usuariosRegistrados.put(usuario.getEmail(), usuario);
            logger.debug("Usuario registrado: {}", usuario.getEmail());

            return true;

        } catch (Exception e) {
            logger.error("Error registrando usuario: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene un usuario por su email.
     *
     * @param email email del usuario
     * @return usuario encontrado o null
     */
    public Usuario obtenerUsuario(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        return usuariosRegistrados.get(email.toLowerCase().trim());
    }

    /**
     * Valida las credenciales de un usuario.
     *
     * @param email email del usuario
     * @param password password del usuario
     * @return true si las credenciales son válidas
     */
    public boolean validarCredenciales(String email, String password) {
        try {
            Usuario usuario = obtenerUsuario(email);

            if (usuario == null) {
                logger.debug("Usuario no encontrado: {}", email);
                return false;
            }

            boolean credencialesValidas = usuario.getPassword().equals(password);
            logger.debug("Validación credenciales para {}: {}", email, credencialesValidas);

            return credencialesValidas;

        } catch (Exception e) {
            logger.error("Error validando credenciales: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la lista completa de usuarios de prueba.
     *
     * @return lista de usuarios
     */
    public List<Usuario> obtenerUsuariosPrueba() {
        return new ArrayList<>(usuariosRegistrados.values());
    }

    // ==================== GESTIÓN DE PRODUCTOS ====================

    /**
     * Registra un nuevo producto en el catálogo de prueba.
     *
     * @param producto producto a registrar
     * @return true si se registró exitosamente
     */
    public boolean registrarProducto(ProductoCrud producto) {
        try {
            if (producto == null || producto.getNombre() == null) {
                logger.warn("Intento de registrar producto nulo o sin nombre");
                return false;
            }

            // Asignar ID único si no tiene
            if (producto.getId() == null) {
                producto.setId(String.valueOf(++contadorProductos));
            }

            // Generar clave única
            String clave = generarClaveProducto(producto);
            productosDisponibles.put(clave, producto);

            logger.debug("Producto registrado: {} [{}]", producto.getNombre(), clave);
            return true;

        } catch (Exception e) {
            logger.error("Error registrando producto: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene un producto por su ID.
     *
     * @param id ID del producto
     * @return producto encontrado o null
     */
    public ProductoCrud obtenerProducto(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }

        return productosDisponibles.values().stream()
                .filter(p -> id.equals(p.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Busca productos por nombre (búsqueda parcial).
     *
     * @param nombre nombre o parte del nombre
     * @return lista de productos que coinciden
     */
    public List<ProductoCrud> buscarProductosPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String nombreBusqueda = nombre.toLowerCase().trim();

        return productosDisponibles.values().stream()
                .filter(p -> p.getNombre().toLowerCase().contains(nombreBusqueda))
                .sorted(Comparator.comparing(ProductoCrud::getNombre))
                .toList();
    }

    /**
     * Obtiene la lista completa de productos de prueba.
     *
     * @return lista de productos
     */
    public List<ProductoCrud> obtenerProductosPrueba() {
        return new ArrayList<>(productosDisponibles.values());
    }

    /**
     * Elimina un producto del catálogo.
     *
     * @param id ID del producto a eliminar
     * @return true si se eliminó exitosamente
     */
    public boolean eliminarProducto(String id) {
        try {
            ProductoCrud producto = obtenerProducto(id);
            if (producto == null) {
                logger.warn("Producto no encontrado para eliminar: {}", id);
                return false;
            }

            String clave = generarClaveProducto(producto);
            productosDisponibles.remove(clave);

            logger.debug("Producto eliminado: {} [{}]", producto.getNombre(), id);
            return true;

        } catch (Exception e) {
            logger.error("Error eliminando producto: {}", e.getMessage());
            return false;
        }
    }

    // ==================== MÉTODOS DE CARGA DE DATOS ====================

    /**
     * Carga datos básicos de prueba.
     */
    private void cargarDatosPrueba() {
        logger.debug("Cargando datos básicos de prueba...");

        // Este método es llamado por inicializar()
        // Los datos específicos se cargan en métodos separados

        cacheDatos.put("fechaInicializacion", LocalDateTime.now());
        cacheDatos.put("entornoPrueba", propiedades.obtenerEntorno());

        logger.debug("Datos básicos cargados");
    }

    /**
     * Carga usuarios predefinidos para pruebas.
     */
    private void cargarUsuariosPredefinidos() {
        logger.debug("Cargando usuarios predefinidos...");

        // Usuario administrador
        Usuario admin = new Usuario();
        admin.setId("1001");
        admin.setNombre("Administrador");
        admin.setApellido("Sistema");
        admin.setEmail("admin@test.com");
        admin.setPassword("admin123");
        admin.setRol("ADMIN");
        admin.setActivo(true);
        registrarUsuario(admin);

        // Usuario estándar
        Usuario usuario = new Usuario();
        usuario.setId("1002");
        usuario.setNombre("Usuario");
        usuario.setApellido("Prueba");
        usuario.setEmail("test@test.com");
        usuario.setPassword("password123");
        usuario.setRol("USER");
        usuario.setActivo(true);
        registrarUsuario(usuario);

        // Usuario para testing
        Usuario tester = new Usuario();
        tester.setId("1003");
        tester.setNombre("QA");
        tester.setApellido("Tester");
        tester.setEmail("qa@test.com");
        tester.setPassword("qa123");
        tester.setRol("QA");
        tester.setActivo(true);
        registrarUsuario(tester);

        logger.debug("Usuarios predefinidos cargados: {}", usuariosRegistrados.size());
    }

    /**
     * Carga productos predefinidos para pruebas.
     */
    private void cargarProductosPredefinidos() {
        logger.debug("Cargando productos predefinidos...");

        // Producto 1
        ProductoCrud producto1 = new ProductoCrud();
        producto1.setId("2001");
        producto1.setNombre("Laptop Gaming");
        producto1.setDescripcion("Laptop para gaming de alta gama");
        producto1.setPrecio(new BigDecimal("1999.99"));
        producto1.setStock(25);
        producto1.setCategoria("ELECTRONICA");
        producto1.setActivo(true);
        registrarProducto(producto1);

        // Producto 2
        ProductoCrud producto2 = new ProductoCrud();
        producto2.setId("2002");
        producto2.setNombre("Mouse Inalámbrico");
        producto2.setDescripcion("Mouse ergonómico inalámbrico");
        producto2.setPrecio(new BigDecimal("49.99"));
        producto2.setStock(100);
        producto2.setCategoria("ACCESORIOS");
        producto2.setActivo(true);
        registrarProducto(producto2);

        // Producto 3
        ProductoCrud producto3 = new ProductoCrud();
        producto3.setId("2003");
        producto3.setNombre("Teclado Mecánico");
        producto3.setDescripcion("Teclado mecánico RGB");
        producto3.setPrecio(new BigDecimal("129.99"));
        producto3.setStock(50);
        producto3.setCategoria("ACCESORIOS");
        producto3.setActivo(true);
        registrarProducto(producto3);

        logger.debug("Productos predefinidos cargados: {}", productosDisponibles.size());
    }

    /**
     * Carga usuarios adicionales para pruebas de regresión.
     */
    private void cargarUsuariosRegresion() {
        logger.debug("Cargando usuarios para regresión...");

        // Generar usuarios adicionales
        for (int i = 1; i <= 10; i++) {
            Usuario usuario = new Usuario();
            usuario.setId(String.valueOf(1100 + i));
            usuario.setNombre("Usuario" + i);
            usuario.setApellido("Regresion" + i);
            usuario.setEmail("regresion" + i + "@test.com");
            usuario.setPassword("regresion" + i);
            usuario.setRol(i % 2 == 0 ? "USER" : "EDITOR");
            usuario.setActivo(true);
            registrarUsuario(usuario);
        }

        logger.debug("Usuarios de regresión cargados");
    }

    /**
     * Carga productos adicionales para pruebas de regresión.
     */
    private void cargarProductosRegresion() {
        logger.debug("Cargando productos para regresión...");

        String[] categorias = {"ELECTRONICA", "ACCESORIOS", "SOFTWARE", "LIBROS", "JUEGOS"};

        // Generar productos adicionales
        for (int i = 1; i <= 20; i++) {
            ProductoCrud producto = new ProductoCrud();
            producto.setId(String.valueOf(2100 + i));
            producto.setNombre("Producto Regresión " + i);
            producto.setDescripcion("Descripción del producto " + i + " para pruebas de regresión");
            producto.setPrecio(new BigDecimal(String.valueOf(10.0 + (i * 5.5))));
            producto.setStock(10 + (i * 3));
            producto.setCategoria(categorias[i % categorias.length]);
            producto.setActivo(i % 7 != 0); // Algunos productos inactivos
            registrarProducto(producto);
        }

        logger.debug("Productos de regresión cargados");
    }

    /**
     * Carga datos de configuración adicionales.
     */
    private void cargarDatosConfiguracion() {
        logger.debug("Cargando datos de configuración...");

        // Configuraciones para diferentes escenarios
        cacheDatos.put("categorias", Arrays.asList("ELECTRONICA", "ACCESORIOS", "SOFTWARE", "LIBROS", "JUEGOS"));
        cacheDatos.put("roles", Arrays.asList("ADMIN", "USER", "EDITOR", "QA"));
        cacheDatos.put("estadosProducto", Arrays.asList("ACTIVO", "INACTIVO", "DESCONTINUADO"));

        // Datos para validaciones
        cacheDatos.put("precioMinimo", new BigDecimal("0.01"));
        cacheDatos.put("precioMaximo", new BigDecimal("99999.99"));
        cacheDatos.put("stockMinimo", 0);
        cacheDatos.put("stockMaximo", 1000);

        logger.debug("Datos de configuración cargados");
    }

    // ==================== MÉTODOS UTILITARIOS ====================

    /**
     * Genera una clave única para un producto.
     *
     * @param producto producto para generar clave
     * @return clave única
     */
    private String generarClaveProducto(ProductoCrud producto) {
        return producto.getId() + "_" + producto.getNombre().replaceAll("\\s+", "_").toLowerCase();
    }

    /**
     * Verifica si los datos están inicializados.
     *
     * @return true si están inicializados
     */
    public boolean estanDatosInicializados() {
        return datosInicializados;
    }

    /**
     * Obtiene estadísticas de los datos cargados.
     *
     * @return mapa con estadísticas
     */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("totalUsuarios", usuariosRegistrados.size());
        estadisticas.put("totalProductos", productosDisponibles.size());
        estadisticas.put("datosInicializados", datosInicializados);
        estadisticas.put("fechaInicializacion", cacheDatos.get("fechaInicializacion"));

        // Estadísticas de usuarios por rol
        Map<String, Long> usuariosPorRol = usuariosRegistrados.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Usuario::getRol,
                        java.util.stream.Collectors.counting()
                ));
        estadisticas.put("usuariosPorRol", usuariosPorRol);

        // Estadísticas de productos por categoría
        Map<String, Long> productosPorCategoria = productosDisponibles.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ProductoCrud::getCategoria,
                        java.util.stream.Collectors.counting()
                ));
        estadisticas.put("productosPorCategoria", productosPorCategoria);

        return estadisticas;
    }

    /**
     * Obtiene datos del caché por clave.
     *
     * @param clave clave del dato
     * @return valor almacenado o null
     */
    public Object obtenerDatoCache(String clave) {
        return cacheDatos.get(clave);
    }
}