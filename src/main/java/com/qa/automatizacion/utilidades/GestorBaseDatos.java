package com.qa.automatizacion.utilidades;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Gestor para operaciones de base de datos durante las pruebas.
 * Maneja conexiones, limpieza de datos y restauración de estados.
 *
 * Principios aplicados:
 * - Single Responsibility: Se enfoca únicamente en operaciones de BD
 * - Resource Management: Manejo apropiado de conexiones
 * - Exception Handling: Manejo robusto de errores de BD
 *
 * @author Equipo QA Automatización
 * @version 1.0
 */
public class GestorBaseDatos {

    private static final Logger logger = LoggerFactory.getLogger(GestorBaseDatos.class);

    // Configuraciones de conexión
    private static final String URL_BD_DEFECTO = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String USUARIO_DEFECTO = "sa";
    private static final String PASSWORD_DEFECTO = "";

    private Connection conexion;
    private Properties configuracion;

    /**
     * Constructor que inicializa la configuración de base de datos
     */
    public GestorBaseDatos() {
        this.configuracion = cargarConfiguracion();
        logger.debug("GestorBaseDatos inicializado");
    }

    /**
     * Establece conexión con la base de datos de pruebas
     *
     * @return Connection activa
     * @throws RuntimeException si no se puede conectar
     */
    public Connection obtenerConexion() {
        if (conexion == null || !estaConexionActiva()) {
            try {
                String url = configuracion.getProperty("bd.url", URL_BD_DEFECTO);
                String usuario = configuracion.getProperty("bd.usuario", USUARIO_DEFECTO);
                String password = configuracion.getProperty("bd.password", PASSWORD_DEFECTO);

                conexion = DriverManager.getConnection(url, usuario, password);

                logger.info("Conexión a base de datos establecida: {}", url);

            } catch (Exception e) {
                logger.error("Error estableciendo conexión a BD: {}", e.getMessage());
                throw new RuntimeException("No se pudo conectar a la base de datos", e);
            }
        }

        return conexion;
    }

    /**
     * Limpia todos los datos de prueba de las tablas principales
     */
    public void limpiarDatosPrueba() {
        logger.info("Iniciando limpieza de datos de prueba");

        try (Connection conn = obtenerConexion()) {
            // Deshabilitar checks de claves foráneas temporalmente
            ejecutarSQL(conn, "SET FOREIGN_KEY_CHECKS = 0");

            // Limpiar tablas en orden apropiado
            String[] tablasLimpiar = {
                    "productos_test",
                    "usuarios_test",
                    "sesiones_test",
                    "audit_log_test"
            };

            for (String tabla : tablasLimpiar) {
                limpiarTabla(conn, tabla);
            }

            // Re-habilitar checks de claves foráneas
            ejecutarSQL(conn, "SET FOREIGN_KEY_CHECKS = 1");

            logger.info("Limpieza de datos de prueba completada");

        } catch (Exception e) {
            logger.error("Error durante limpieza de datos: {}", e.getMessage());
            throw new RuntimeException("Fallo en limpieza de datos de prueba", e);
        }
    }

    /**
     * Restaura el estado inicial de la base de datos con datos base
     */
    public void restaurarEstadoInicial() {
        logger.info("Iniciando restauración de estado inicial");

        try {
            // Primero limpiar datos existentes
            limpiarDatosPrueba();

            // Insertar datos base para pruebas
            insertarDatosBase();

            logger.info("Estado inicial de BD restaurado exitosamente");

        } catch (Exception e) {
            logger.error("Error restaurando estado inicial: {}", e.getMessage());
            throw new RuntimeException("Fallo en restauración de estado inicial", e);
        }
    }

    /**
     * Inserta un usuario de prueba en la base de datos
     *
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param nombre Nombre del usuario
     * @return ID del usuario creado
     */
    public Long insertarUsuarioPrueba(String email, String password, String nombre) {
        logger.debug("Insertando usuario de prueba: {}", email);

        String sql = """
            INSERT INTO usuarios_test (email, password, nombre, activo, fecha_creacion) 
            VALUES (?, ?, ?, true, CURRENT_TIMESTAMP)
            """;

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, nombre);

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long id = rs.getLong(1);
                        logger.debug("Usuario de prueba creado con ID: {}", id);
                        return id;
                    }
                }
            }

            throw new RuntimeException("No se pudo crear el usuario de prueba");

        } catch (Exception e) {
            logger.error("Error insertando usuario de prueba: {}", e.getMessage());
            throw new RuntimeException("Fallo insertando usuario de prueba", e);
        }
    }

    /**
     * Inserta un producto de prueba en la base de datos
     *
     * @param nombre Nombre del producto
     * @param precio Precio del producto
     * @param categoria Categoría del producto
     * @return ID del producto creado
     */
    public Long insertarProductoPrueba(String nombre, Double precio, String categoria) {
        logger.debug("Insertando producto de prueba: {}", nombre);

        String sql = """
            INSERT INTO productos_test (nombre, precio, categoria, activo, fecha_creacion) 
            VALUES (?, ?, ?, true, CURRENT_TIMESTAMP)
            """;

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nombre);
            stmt.setDouble(2, precio);
            stmt.setString(3, categoria);

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long id = rs.getLong(1);
                        logger.debug("Producto de prueba creado con ID: {}", id);
                        return id;
                    }
                }
            }

            throw new RuntimeException("No se pudo crear el producto de prueba");

        } catch (Exception e) {
            logger.error("Error insertando producto de prueba: {}", e.getMessage());
            throw new RuntimeException("Fallo insertando producto de prueba", e);
        }
    }

    /**
     * Verifica si un usuario existe en la base de datos
     *
     * @param email Email del usuario a verificar
     * @return true si el usuario existe
     */
    public boolean existeUsuario(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios_test WHERE email = ?";

        try (Connection conn = obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (Exception e) {
            logger.error("Error verificando existencia de usuario: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Cierra la conexión a la base de datos
     */
    public void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
                conexion = null;
                logger.debug("Conexión a BD cerrada");
            } catch (Exception e) {
                logger.warn("Error cerrando conexión a BD: {}", e.getMessage());
            }
        }
    }

    // Métodos auxiliares privados

    /**
     * Carga la configuración de base de datos
     */
    private Properties cargarConfiguracion() {
        Properties props = new Properties();

        // Configuraciones por defecto
        props.setProperty("bd.url", URL_BD_DEFECTO);
        props.setProperty("bd.usuario", USUARIO_DEFECTO);
        props.setProperty("bd.password", PASSWORD_DEFECTO);

        // Sobrescribir con system properties si existen
        String urlSistema = System.getProperty("test.db.url");
        if (urlSistema != null) {
            props.setProperty("bd.url", urlSistema);
        }

        String usuarioSistema = System.getProperty("test.db.usuario");
        if (usuarioSistema != null) {
            props.setProperty("bd.usuario", usuarioSistema);
        }

        String passwordSistema = System.getProperty("test.db.password");
        if (passwordSistema != null) {
            props.setProperty("bd.password", passwordSistema);
        }

        return props;
    }

    /**
     * Verifica si la conexión está activa
     */
    private boolean estaConexionActiva() {
        try {
            return conexion != null && !conexion.isClosed() && conexion.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ejecuta una sentencia SQL simple
     */
    private void ejecutarSQL(Connection conn, String sql) {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            logger.debug("SQL ejecutado: {}", sql);
        } catch (Exception e) {
            logger.warn("Error ejecutando SQL '{}': {}", sql, e.getMessage());
        }
    }

    /**
     * Limpia una tabla específica
     */
    private void limpiarTabla(Connection conn, String nombreTabla) {
        try {
            // Verificar si la tabla existe antes de limpiarla
            if (existeTabla(conn, nombreTabla)) {
                String sql = "DELETE FROM " + nombreTabla;
                ejecutarSQL(conn, sql);
                logger.debug("Tabla limpiada: {}", nombreTabla);
            } else {
                logger.debug("Tabla no existe, omitiendo limpieza: {}", nombreTabla);
            }
        } catch (Exception e) {
            logger.warn("Error limpiando tabla '{}': {}", nombreTabla, e.getMessage());
        }
    }

    /**
     * Verifica si una tabla existe en la base de datos
     */
    private boolean existeTabla(Connection conn, String nombreTabla) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nombreTabla.toUpperCase());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (Exception e) {
            // Si falla la consulta de metadatos, intentar acceso directo
            try {
                String sql = "SELECT 1 FROM " + nombreTabla + " LIMIT 1";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeQuery();
                    return true;
                }
            } catch (Exception e2) {
                return false;
            }
        }
        return false;
    }

    /**
     * Inserta datos base necesarios para las pruebas
     */
    private void insertarDatosBase() {
        logger.debug("Insertando datos base para pruebas");

        try {
            // Crear tablas si no existen
            crearTablasBaseSiNoExisten();

            // Insertar usuarios base para pruebas
            insertarUsuarioPrueba("test@test.com", "password123", "Usuario Test");
            insertarUsuarioPrueba("admin@test.com", "admin123", "Admin Test");

            // Insertar productos base para pruebas
            insertarProductoPrueba("Laptop Test", 1500.00, "Electrónicos");
            insertarProductoPrueba("Mouse Test", 25.00, "Electrónicos");
            insertarProductoPrueba("Libro Test", 15.00, "Libros");

            logger.debug("Datos base insertados correctamente");

        } catch (Exception e) {
            logger.error("Error insertando datos base: {}", e.getMessage());
            throw new RuntimeException("Fallo insertando datos base", e);
        }
    }

    /**
     * Crea las tablas base si no existen
     */
    private void crearTablasBaseSiNoExisten() {
        logger.debug("Creando tablas base si no existen");

        try (Connection conn = obtenerConexion()) {

            // Tabla de usuarios de prueba
            String sqlUsuarios = """
                CREATE TABLE IF NOT EXISTS usuarios_test (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    email VARCHAR(255) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    nombre VARCHAR(255) NOT NULL,
                    activo BOOLEAN DEFAULT true,
                    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;
            ejecutarSQL(conn, sqlUsuarios);

            // Tabla de productos de prueba
            String sqlProductos = """
                CREATE TABLE IF NOT EXISTS productos_test (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    nombre VARCHAR(255) NOT NULL,
                    descripcion TEXT,
                    precio DECIMAL(10,2) NOT NULL,
                    categoria VARCHAR(100) NOT NULL,
                    stock INT DEFAULT 0,
                    activo BOOLEAN DEFAULT true,
                    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;
            ejecutarSQL(conn, sqlProductos);

            // Tabla de sesiones de prueba
            String sqlSesiones = """
                CREATE TABLE IF NOT EXISTS sesiones_test (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    usuario_id BIGINT NOT NULL,
                    token VARCHAR(255) NOT NULL,
                    fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    fecha_expiracion TIMESTAMP,
                    activa BOOLEAN DEFAULT true,
                    FOREIGN KEY (usuario_id) REFERENCES usuarios_test(id)
                )
                """;
            ejecutarSQL(conn, sqlSesiones);

            // Tabla de auditoría de prueba
            String sqlAudit = """
                CREATE TABLE IF NOT EXISTS audit_log_test (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    tabla_afectada VARCHAR(100) NOT NULL,
                    accion VARCHAR(50) NOT NULL,
                    usuario_id BIGINT,
                    datos_anteriores TEXT,
                    datos_nuevos TEXT,
                    fecha_accion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            ejecutarSQL(conn, sqlAudit);

            logger.debug("Tablas base creadas exitosamente");

        } catch (Exception e) {
            logger.error("Error creando tablas base: {}", e.getMessage());
            throw new RuntimeException("Fallo creando tablas base", e);
        }
    }

    /**
     * Obtiene estadísticas de la base de datos para debugging
     *
     * @return String con estadísticas de tablas
     */
    public String obtenerEstadisticasTablas() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== Estadísticas de Base de Datos ===\n");

        String[] tablas = {"usuarios_test", "productos_test", "sesiones_test", "audit_log_test"};

        try (Connection conn = obtenerConexion()) {
            for (String tabla : tablas) {
                if (existeTabla(conn, tabla)) {
                    int count = contarRegistros(conn, tabla);
                    stats.append(String.format("%s: %d registros\n", tabla, count));
                } else {
                    stats.append(String.format("%s: no existe\n", tabla));
                }
            }
        } catch (Exception e) {
            stats.append("Error obteniendo estadísticas: ").append(e.getMessage());
        }

        return stats.toString();
    }

    /**
     * Cuenta los registros en una tabla
     */
    private int contarRegistros(Connection conn, String tabla) {
        String sql = "SELECT COUNT(*) FROM " + tabla;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            logger.warn("Error contando registros en tabla '{}': {}", tabla, e.getMessage());
        }
        return 0;
    }

    /**
     * Ejecuta una transacción completa con rollback automático en caso de error
     *
     * @param operaciones Array de operaciones SQL a ejecutar
     * @return true si todas las operaciones fueron exitosas
     */
    public boolean ejecutarTransaccion(String... operaciones) {
        try (Connection conn = obtenerConexion()) {
            conn.setAutoCommit(false);

            try {
                for (String sql : operaciones) {
                    ejecutarSQL(conn, sql);
                }

                conn.commit();
                logger.debug("Transacción completada exitosamente");
                return true;

            } catch (Exception e) {
                conn.rollback();
                logger.error("Error en transacción, rollback ejecutado: {}", e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            logger.error("Error estableciendo transacción: {}", e.getMessage());
            return false;
        }
    }
}