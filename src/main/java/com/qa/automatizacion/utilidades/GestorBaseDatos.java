package com.qa.automatizacion.utilidades;

import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestor para operaciones de base de datos en las pruebas.
 * Maneja la configuración, inicialización y limpieza de datos de prueba.
 *
 * Principios aplicados:
 * - Single Responsibility: Se enfoca únicamente en operaciones de BD
 * - Encapsulación: Oculta la complejidad de las operaciones SQL
 * - Abstracción: Proporciona métodos de alto nivel para los tests
 * - Resource Management: Manejo seguro de conexiones y recursos
 */
public class GestorBaseDatos {

    private static final Logger logger = LoggerFactory.getLogger(GestorBaseDatos.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PropiedadesAplicacion propiedades;
    private Connection conexion;

    // Scripts SQL para inicialización
    private static final String CREAR_TABLA_USUARIOS = """
        CREATE TABLE IF NOT EXISTS usuarios (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre VARCHAR(100) NOT NULL,
            apellido VARCHAR(100) NOT NULL,
            email VARCHAR(255) UNIQUE NOT NULL,
            password VARCHAR(255) NOT NULL,
            telefono VARCHAR(20),
            fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            estado VARCHAR(20) DEFAULT 'ACTIVO',
            intentos_fallidos INTEGER DEFAULT 0,
            ultimo_acceso TIMESTAMP
        )
        """;

    private static final String CREAR_TABLA_PRODUCTOS = """
        CREATE TABLE IF NOT EXISTS productos (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            codigo_sku VARCHAR(50) UNIQUE NOT NULL,
            nombre VARCHAR(200) NOT NULL,
            descripcion TEXT,
            precio DECIMAL(10,2) NOT NULL,
            categoria VARCHAR(100) NOT NULL,
            stock INTEGER NOT NULL DEFAULT 0,
            estado VARCHAR(20) DEFAULT 'ACTIVO',
            fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            usuario_creacion VARCHAR(100),
            usuario_modificacion VARCHAR(100)
        )
        """;

    private static final String CREAR_TABLA_SESIONES = """
        CREATE TABLE IF NOT EXISTS sesiones_usuario (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            usuario_id INTEGER,
            token_sesion VARCHAR(255),
            fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            fecha_expiracion TIMESTAMP,
            ip_address VARCHAR(45),
            user_agent TEXT,
            activa BOOLEAN DEFAULT TRUE,
            FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        )
        """;

    private static final String CREAR_TABLA_AUDITORIA = """
        CREATE TABLE IF NOT EXISTS auditoria_cambios (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            tabla VARCHAR(50) NOT NULL,
            operacion VARCHAR(10) NOT NULL,
            registro_id INTEGER NOT NULL,
            datos_anteriores TEXT,
            datos_nuevos TEXT,
            usuario VARCHAR(100),
            fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            ip_address VARCHAR(45)
        )
        """;

    public GestorBaseDatos() {
        this.propiedades = PropiedadesAplicacion.obtenerInstancia();
    }

    // ==================== MÉTODOS DE CONEXIÓN ====================

    /**
     * Inicializa la base de datos y crea las tablas necesarias.
     */
    public void inicializar() {
        logger.info("Inicializando base de datos de pruebas...");

        try {
            establecerConexion();
            crearTablas();
            logger.info("Base de datos inicializada exitosamente");
        } catch (SQLException e) {
            logger.error("Error inicializando base de datos: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo inicializar la base de datos", e);
        }
    }

    /**
     * Establece la conexión con la base de datos.
     */
    private void establecerConexion() throws SQLException {
        if (conexion != null && !conexion.isClosed()) {
            return;
        }

        String urlBD = propiedades.obtenerPropiedad("bd.url", "jdbc:h2:mem:testdb");
        String usuario = propiedades.obtenerPropiedad("bd.usuario", "sa");
        String password = propiedades.obtenerPropiedad("bd.password", "");
        String driver = propiedades.obtenerPropiedad("bd.driver", "org.h2.Driver");

        try {
            Class.forName(driver);
            conexion = DriverManager.getConnection(urlBD, usuario, password);
            conexion.setAutoCommit(true);

            logger.debug("Conexión establecida con base de datos: {}", urlBD);
        } catch (ClassNotFoundException e) {
            logger.error("Driver de base de datos no encontrado: {}", driver);
            throw new SQLException("Driver no encontrado", e);
        }
    }

    /**
     * Crea las tablas necesarias para las pruebas.
     */
    private void crearTablas() throws SQLException {
        ejecutarScript(CREAR_TABLA_USUARIOS);
        ejecutarScript(CREAR_TABLA_PRODUCTOS);
        ejecutarScript(CREAR_TABLA_SESIONES);
        ejecutarScript(CREAR_TABLA_AUDITORIA);

        logger.debug("Tablas de base de datos creadas exitosamente");
    }

    /**
     * Ejecuta un script SQL.
     *
     * @param script script SQL a ejecutar
     */
    private void ejecutarScript(String script) throws SQLException {
        try (Statement stmt = conexion.createStatement()) {
            stmt.execute(script);
        }
    }

    // ==================== MÉTODOS DE DATOS DE PRUEBA ====================

    /**
     * Carga datos de prueba básicos para todos los escenarios.
     */
    public void cargarDatosPrueba() {
        logger.info("Cargando datos de prueba...");

        try {
            prepararDatosUsuarios();
            prepararDatosProductos();
            logger.info("Datos de prueba cargados exitosamente");
        } catch (Exception e) {
            logger.error("Error cargando datos de prueba: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudieron cargar los datos de prueba", e);
        }
    }

    /**
     * Prepara datos de usuarios para las pruebas de autenticación.
     */
    public void prepararDatosUsuarios() {
        logger.debug("Preparando datos de usuarios...");

        try {
            // Limpiar usuarios de prueba existentes
            limpiarUsuariosPrueba();

            // Usuarios de prueba para login
            insertarUsuario("Juan", "Pérez", "test@test.com", "password123", "+56912345678", "ACTIVO");
            insertarUsuario("María", "González", "maria@test.com", "password456", "+56987654321", "ACTIVO");
            insertarUsuario("Pedro", "Silva", "pedro@test.com", "password789", "+56955555555", "ACTIVO");

            // Usuario para pruebas de bloqueo
            insertarUsuario("Usuario", "Bloqueado", "bloqueado@test.com", "password123", "+56911111111", "BLOQUEADO");

            // Usuario con intentos fallidos
            insertarUsuarioConIntentosFallidos("Intentos", "Fallidos", "intentos@test.com", "password123", "+56922222222", 3);

            logger.debug("Datos de usuarios preparados: 5 usuarios creados");

        } catch (SQLException e) {
            logger.error("Error preparando datos de usuarios: {}", e.getMessage(), e);
            throw new RuntimeException("Error en preparación de datos de usuarios", e);
        }
    }

    /**
     * Prepara datos de productos para las pruebas CRUD.
     */
    public void prepararDatosProductos() {
        logger.debug("Preparando datos de productos...");

        try {
            // Limpiar productos de prueba existentes
            limpiarProductosPrueba();

            // Productos de prueba
            insertarProducto("LAPTOP-DELL-001", "Laptop Dell Inspiron 15",
                    "Laptop para uso profesional y estudiantil", 799999.00, "Electrónicos", 25, "test@test.com");

            insertarProducto("MOUSE-LOGIC-001", "Mouse Logitech MX Master 3",
                    "Mouse inalámbrico de alta precisión", 89990.00, "Electrónicos", 50, "test@test.com");

            insertarProducto("LIBRO-JAVA-001", "Effective Java 3rd Edition",
                    "Libro de mejores prácticas en Java", 45990.00, "Libros", 15, "test@test.com");

            insertarProducto("SILLA-ERGO-001", "Silla Ergonómica Premium",
                    "Silla de oficina ergonómica con soporte lumbar", 299990.00, "Muebles", 8, "test@test.com");

            insertarProducto("CAFE-ORIGIN-001", "Café de Origen Colombia",
                    "Café premium de origen único", 12990.00, "Alimentos", 100, "test@test.com");

            // Producto con stock agotado
            insertarProducto("AGOTADO-001", "Producto Agotado",
                    "Producto para probar manejo de stock cero", 19990.00, "Test", 0, "test@test.com");

            logger.debug("Datos de productos preparados: 6 productos creados");

        } catch (SQLException e) {
            logger.error("Error preparando datos de productos: {}", e.getMessage(), e);
            throw new RuntimeException("Error en preparación de datos de productos", e);
        }
    }

    /**
     * Carga conjunto completo de datos para pruebas de regresión.
     */
    public void cargarConjuntoCompletoDatos() {
        logger.info("Cargando conjunto completo de datos para regresión...");

        try {
            // Cargar datos básicos
            cargarDatosPrueba();

            // Datos adicionales para regresión
            cargarDatosRegresionUsuarios();
            cargarDatosRegresionProductos();
            crearSesionesUsuario();

            logger.info("Conjunto completo de datos cargado exitosamente");

        } catch (Exception e) {
            logger.error("Error cargando conjunto completo de datos: {}", e.getMessage(), e);
            throw new RuntimeException("Error cargando datos de regresión", e);
        }
    }

    // ==================== MÉTODOS DE INSERCIÓN ====================

    /**
     * Inserta un usuario en la base de datos.
     */
    private void insertarUsuario(String nombre, String apellido, String email,
                                 String password, String telefono, String estado) throws SQLException {
        String sql = """
            INSERT INTO usuarios (nombre, apellido, email, password, telefono, estado, fecha_registro)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, apellido);
            pstmt.setString(3, email);
            pstmt.setString(4, encriptarPassword(password));
            pstmt.setString(5, telefono);
            pstmt.setString(6, estado);
            pstmt.setString(7, LocalDateTime.now().format(FORMATO_FECHA));

            pstmt.executeUpdate();
        }
    }

    /**
     * Inserta un usuario con intentos fallidos específicos.
     */
    private void insertarUsuarioConIntentosFallidos(String nombre, String apellido, String email,
                                                    String password, String telefono, int intentosFallidos) throws SQLException {
        insertarUsuario(nombre, apellido, email, password, telefono, "ACTIVO");

        String sqlUpdate = "UPDATE usuarios SET intentos_fallidos = ? WHERE email = ?";
        try (PreparedStatement pstmt = conexion.prepareStatement(sqlUpdate)) {
            pstmt.setInt(1, intentosFallidos);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
        }
    }

    /**
     * Inserta un producto en la base de datos.
     */
    private void insertarProducto(String codigoSku, String nombre, String descripcion,
                                  double precio, String categoria, int stock, String usuarioCreacion) throws SQLException {
        String sql = """
            INSERT INTO productos (codigo_sku, nombre, descripcion, precio, categoria, stock, 
                                 usuario_creacion, fecha_creacion, fecha_modificacion)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, codigoSku);
            pstmt.setString(2, nombre);
            pstmt.setString(3, descripcion);
            pstmt.setDouble(4, precio);
            pstmt.setString(5, categoria);
            pstmt.setInt(6, stock);
            pstmt.setString(7, usuarioCreacion);

            String fechaActual = LocalDateTime.now().format(FORMATO_FECHA);
            pstmt.setString(8, fechaActual);
            pstmt.setString(9, fechaActual);

            pstmt.executeUpdate();
        }
    }

    // ==================== MÉTODOS DE LIMPIEZA ====================

    /**
     * Limpia todos los datos de prueba.
     */
    public void limpiarDatosPrueba() {
        logger.debug("Limpiando todos los datos de prueba...");

        try {
            limpiarSesiones();
            limpiarProductosPrueba();
            limpiarUsuariosPrueba();
            limpiarAuditoria();

            logger.debug("Datos de prueba limpiados exitosamente");

        } catch (SQLException e) {
            logger.error("Error limpiando datos de prueba: {}", e.getMessage(), e);
        }
    }

    /**
     * Limpia usuarios de prueba de la base de datos.
     */
    public void limpiarUsuariosPrueba() throws SQLException {
        String sql = "DELETE FROM usuarios WHERE email LIKE '%@test.com' OR email LIKE '%@ejemplo.com'";
        try (Statement stmt = conexion.createStatement()) {
            int filasAfectadas = stmt.executeUpdate(sql);
            logger.debug("Usuarios de prueba eliminados: {}", filasAfectadas);
        }
    }

    /**
     * Limpia productos de prueba de la base de datos.
     */
    public void limpiarProductosPrueba() throws SQLException {
        String sql = "DELETE FROM productos WHERE codigo_sku LIKE '%-TEST-%' OR codigo_sku LIKE '%TEST%' OR categoria = 'Test'";
        try (Statement stmt = conexion.createStatement()) {
            int filasAfectadas = stmt.executeUpdate(sql);
            logger.debug("Productos de prueba eliminados: {}", filasAfectadas);
        }
    }

    /**
     * Limpia sesiones de usuario.
     */
    private void limpiarSesiones() throws SQLException {
        String sql = "DELETE FROM sesiones_usuario";
        try (Statement stmt = conexion.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Limpia registros de auditoría.
     */
    private void limpiarAuditoria() throws SQLException {
        String sql = "DELETE FROM auditoria_cambios";
        try (Statement stmt = conexion.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Restaura el estado inicial de la base de datos.
     */
    public void restaurarEstadoInicial() {
        logger.debug("Restaurando estado inicial de la base de datos...");

        try {
            limpiarDatosPrueba();
            cargarDatosPrueba();

            logger.debug("Estado inicial restaurado exitosamente");

        } catch (Exception e) {
            logger.error("Error restaurando estado inicial: {}", e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS DE CONSULTA ====================

    /**
     * Verifica si un usuario existe en la base de datos.
     *
     * @param email email del usuario
     * @return true si el usuario existe
     */
    public boolean existeUsuario(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error verificando existencia de usuario: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Verifica si un producto existe en la base de datos.
     *
     * @param codigoSku código SKU del producto
     * @return true si el producto existe
     */
    public boolean existeProducto(String codigoSku) {
        String sql = "SELECT COUNT(*) FROM productos WHERE codigo_sku = ?";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, codigoSku);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error verificando existencia de producto: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Obtiene información de un usuario por email.
     *
     * @param email email del usuario
     * @return mapa con información del usuario o null si no existe
     */
    public Map<String, Object> obtenerUsuario(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> usuario = new HashMap<>();
                    usuario.put("id", rs.getInt("id"));
                    usuario.put("nombre", rs.getString("nombre"));
                    usuario.put("apellido", rs.getString("apellido"));
                    usuario.put("email", rs.getString("email"));
                    usuario.put("telefono", rs.getString("telefono"));
                    usuario.put("estado", rs.getString("estado"));
                    usuario.put("intentos_fallidos", rs.getInt("intentos_fallidos"));
                    usuario.put("fecha_registro", rs.getString("fecha_registro"));

                    return usuario;
                }
            }
        } catch (SQLException e) {
            logger.error("Error obteniendo información de usuario: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Obtiene la lista de productos por categoría.
     *
     * @param categoria categoría de productos
     * @return lista de productos
     */
    public List<Map<String, Object>> obtenerProductosPorCategoria(String categoria) {
        List<Map<String, Object>> productos = new ArrayList<>();
        String sql = "SELECT * FROM productos WHERE categoria = ? ORDER BY nombre";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, categoria);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> producto = new HashMap<>();
                    producto.put("id", rs.getInt("id"));
                    producto.put("codigo_sku", rs.getString("codigo_sku"));
                    producto.put("nombre", rs.getString("nombre"));
                    producto.put("descripcion", rs.getString("descripcion"));
                    producto.put("precio", rs.getDouble("precio"));
                    producto.put("categoria", rs.getString("categoria"));
                    producto.put("stock", rs.getInt("stock"));
                    producto.put("estado", rs.getString("estado"));

                    productos.add(producto);
                }
            }
        } catch (SQLException e) {
            logger.error("Error obteniendo productos por categoría: {}", e.getMessage());
        }

        return productos;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Carga datos adicionales para pruebas de regresión de usuarios.
     */
    private void cargarDatosRegresionUsuarios() throws SQLException {
        // Usuarios adicionales para pruebas extensivas
        insertarUsuario("Ana", "Martínez", "ana@test.com", "password123", "+56933333333", "ACTIVO");
        insertarUsuario("Carlos", "López", "carlos@test.com", "password123", "+56944444444", "ACTIVO");
        insertarUsuario("Laura", "Rodríguez", "laura@test.com", "password123", "+56955555555", "INACTIVO");
        insertarUsuario("Miguel", "Fernández", "miguel@test.com", "password123", "+56966666666", "PENDIENTE");
    }

    /**
     * Carga datos adicionales para pruebas de regresión de productos.
     */
    private void cargarDatosRegresionProductos() throws SQLException {
        // Productos adicionales para pruebas extensivas
        insertarProducto("MONITOR-SAMSUNG-001", "Monitor Samsung 27''",
                "Monitor LED Full HD", 299990.00, "Electrónicos", 12, "test@test.com");

        insertarProducto("TECLADO-MECHANICAL-001", "Teclado Mecánico RGB",
                "Teclado gaming con switches Cherry MX", 159990.00, "Electrónicos", 20, "test@test.com");

        insertarProducto("ESCRITORIO-STANDING-001", "Escritorio Standing Desk",
                "Escritorio ajustable en altura", 699990.00, "Muebles", 5, "test@test.com");
    }

    /**
     * Crea sesiones de usuario para pruebas.
     */
    private void crearSesionesUsuario() throws SQLException {
        String sql = """
            INSERT INTO sesiones_usuario (usuario_id, token_sesion, fecha_expiracion, ip_address, user_agent, activa)
            VALUES ((SELECT id FROM usuarios WHERE email = ?), ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            // Sesión activa para test@test.com
            pstmt.setString(1, "test@test.com");
            pstmt.setString(2, "token_123456789");
            pstmt.setString(3, LocalDateTime.now().plusHours(2).format(FORMATO_FECHA));
            pstmt.setString(4, "127.0.0.1");
            pstmt.setString(5, "Mozilla/5.0 Test Browser");
            pstmt.setBoolean(6, true);
            pstmt.executeUpdate();
        }
    }

    /**
     * Encripta una contraseña de forma simple para pruebas.
     * En un entorno real se usaría BCrypt o similar.
     *
     * @param password contraseña en texto plano
     * @return contraseña encriptada
     */
    private String encriptarPassword(String password) {
        // Simulación simple de encriptación para pruebas
        return "encrypted_" + password + "_hash";
    }

    /**
     * Cierra la conexión con la base de datos.
     */
    public void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
                logger.debug("Conexión con base de datos cerrada");
            } catch (SQLException e) {
                logger.error("Error cerrando conexión: {}", e.getMessage());
            }
        }
    }

    /**
     * Verifica si la conexión está activa.
     *
     * @return true si la conexión está activa
     */
    public boolean esConexionActiva() {
        try {
            return conexion != null && !conexion.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Obtiene estadísticas de la base de datos para diagnóstico.
     *
     * @return mapa con estadísticas
     */
    public Map<String, Integer> obtenerEstadisticas() {
        Map<String, Integer> estadisticas = new HashMap<>();

        try {
            estadisticas.put("usuarios", contarRegistros("usuarios"));
            estadisticas.put("productos", contarRegistros("productos"));
            estadisticas.put("sesiones", contarRegistros("sesiones_usuario"));
            estadisticas.put("auditoria", contarRegistros("auditoria_cambios"));
        } catch (SQLException e) {
            logger.error("Error obteniendo estadísticas: {}", e.getMessage());
        }

        return estadisticas;
    }

    /**
     * Cuenta registros en una tabla.
     *
     * @param tabla nombre de la tabla
     * @return número de registros
     */
    private int contarRegistros(String tabla) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tabla;

        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }
}