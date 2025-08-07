package com.qa.automatizacion.utilidades;

import com.qa.automatizacion.modelo.Usuario;
import com.qa.automatizacion.modelo.ProductoCrud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generador de datos de prueba para usuarios y productos.
 * Proporciona métodos para crear datos válidos e inválidos según los escenarios de prueba.
 *
 * Principios aplicados:
 * - Single Responsibility: Se enfoca únicamente en generar datos de prueba
 * - DRY: Evita repetición de lógica de generación
 * - Factory Pattern: Crea objetos de diferentes tipos según necesidades
 *
 * @author Equipo QA Automatización
 * @version 1.0
 */
public class GeneradorDatos {

    private static final Logger logger = LoggerFactory.getLogger(GeneradorDatos.class);

    // Arrays de datos base para generación aleatoria
    private static final String[] NOMBRES = {
            "Ana", "Carlos", "María", "José", "Laura", "Pedro", "Carmen", "Luis",
            "Isabel", "Antonio", "Rosa", "Manuel", "Patricia", "Francisco", "Elena"
    };

    private static final String[] APELLIDOS = {
            "García", "Rodríguez", "González", "Fernández", "López", "Martínez",
            "Sánchez", "Pérez", "Gómez", "Martín", "Jiménez", "Ruiz", "Hernández"
    };

    private static final String[] NOMBRES_PRODUCTOS = {
            "Laptop", "Smartphone", "Tablet", "Monitor", "Teclado", "Mouse",
            "Auriculares", "Cámara", "Impresora", "Disco Duro", "Memoria USB",
            "Router", "Parlantes", "Micrófono", "Webcam"
    };

    private static final String[] MARCAS = {
            "TechPro", "Digital", "Ultra", "Master", "Premium", "Advanced",
            "Professional", "Elite", "Supreme", "Excellence"
    };

    private static final String[] CATEGORIAS = {
            "Electrónicos", "Electrodomésticos", "Ropa y Accesorios", "Hogar y Jardín",
            "Deportes y Recreación", "Libros y Medios", "Salud y Belleza", "General"
    };

    private static final String[] DOMINIOS_EMAIL = {
            "gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "test.com"
    };

    /**
     * Constructor privado para clase utilitaria
     */
    private GeneradorDatos() {
        throw new UnsupportedOperationException("Clase utilitaria no debe ser instanciada");
    }

    // Métodos para generar usuarios

    /**
     * Genera un usuario válido con datos aleatorios
     *
     * @return Usuario con datos válidos
     */
    public static Usuario generarUsuarioValido() {
        String nombre = obtenerElementoAleatorio(NOMBRES);
        String apellido = obtenerElementoAleatorio(APELLIDOS);
        String nombreUsuario = generarNombreUsuario(nombre, apellido);
        String email = generarEmail(nombre, apellido);
        String contrasena = generarContrasenaValida();

        Usuario usuario = Usuario.builder()
                .nombreUsuario(nombreUsuario)
                .email(email)
                .contrasena(contrasena)
                .nombre(nombre)
                .apellido(apellido)
                .activo(true)
                .rol(Usuario.RolUsuario.USUARIO)
                .build();

        logger.debug("Usuario válido generado: {}", usuario.getNombreUsuario());
        return usuario;
    }

    /**
     * Genera un usuario con datos específicos desde un Map
     *
     * @param datosUsuario Map con los datos del usuario
     * @return Usuario generado
     */
    public static Usuario generarUsuarioDesdeMap(Map<String, String> datosUsuario) {
        return Usuario.builder()
                .nombreUsuario(datosUsuario.get("nombreUsuario"))
                .email(datosUsuario.get("email"))
                .contrasena(datosUsuario.get("contrasena"))
                .nombre(datosUsuario.get("nombre"))
                .apellido(datosUsuario.get("apellido"))
                .activo(Boolean.parseBoolean(datosUsuario.getOrDefault("activo", "true")))
                .rol(Usuario.RolUsuario.porDescripcion(datosUsuario.getOrDefault("rol", "USUARIO")))
                .build();
    }

    /**
     * Genera un usuario con campo faltante para pruebas negativas
     *
     * @param campoFaltante Campo que debe estar vacío o nulo
     * @return Usuario con campo faltante
     */
    public static Usuario generarUsuarioConCampoFaltante(String campoFaltante) {
        Usuario.Builder builder = Usuario.builder()
                .nombreUsuario("usuario_test")
                .email("test@example.com")
                .contrasena("Password123!")
                .nombre("Test")
                .apellido("Usuario");

        switch (campoFaltante.toLowerCase()) {
            case "nombreusuario":
                builder.nombreUsuario("");
                break;
            case "email":
                builder.email("");
                break;
            case "contrasena":
                builder.contrasena("");
                break;
            case "nombre":
                builder.nombre("");
                break;
            case "apellido":
                builder.apellido("");
                break;
        }

        Usuario usuario = builder.build();
        logger.debug("Usuario con campo faltante '{}' generado", campoFaltante);
        return usuario;
    }

    /**
     * Genera una lista de usuarios para pruebas masivas
     *
     * @param cantidad Número de usuarios a generar
     * @return Lista de usuarios
     */
    public static List<Usuario> generarListaUsuarios(int cantidad) {
        List<Usuario> usuarios = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            usuarios.add(generarUsuarioValido());
        }
        logger.debug("Lista de {} usuarios generada", cantidad);
        return usuarios;
    }

    // Métodos para generar productos

    /**
     * Genera un producto válido con datos aleatorios
     *
     * @return ProductoCrud con datos válidos
     */
    public static ProductoCrud generarProductoValido() {
        String nombreProducto = obtenerElementoAleatorio(MARCAS) + " " + obtenerElementoAleatorio(NOMBRES_PRODUCTOS);
        String descripcion = "Descripción detallada del " + nombreProducto.toLowerCase();
        BigDecimal precio = generarPrecioAleatorio();
        String categoria = obtenerElementoAleatorio(CATEGORIAS);
        Integer stock = ThreadLocalRandom.current().nextInt(1, 100);

        ProductoCrud producto = ProductoCrud.builder()
                .nombre(nombreProducto)
                .descripcion(descripcion)
                .precio(precio)
                .categoria(categoria)
                .stock(stock)
                .activo(true)
                .build();

        logger.debug("Producto válido generado: {}", producto.getNombre());
        return producto;
    }

    /**
     * Genera un producto desde un Map de datos
     *
     * @param datosProducto Map con los datos del producto
     * @return ProductoCrud generado
     */
    public static ProductoCrud generarProductoDesdeMap(Map<String, String> datosProducto) {
        ProductoCrud.Builder builder = ProductoCrud.builder()
                .nombre(datosProducto.get("nombre"))
                .descripcion(datosProducto.get("descripcion"))
                .categoria(datosProducto.get("categoria"));

        // Manejo seguro del precio
        String precioStr = datosProducto.get("precio");
        if (precioStr != null && !precioStr.trim().isEmpty()) {
            try {
                builder.precio(new BigDecimal(precioStr));
            } catch (NumberFormatException e) {
                logger.warn("Precio inválido: {}, usando precio por defecto", precioStr);
                builder.precio(BigDecimal.valueOf(100.0));
            }
        }

        // Manejo seguro del stock
        String stockStr = datosProducto.get("stock");
        if (stockStr != null && !stockStr.trim().isEmpty()) {
            try {
                builder.stock(Integer.parseInt(stockStr));
            } catch (NumberFormatException e) {
                logger.warn("Stock inválido: {}, usando stock por defecto", stockStr);
                builder.stock(10);
            }
        }

        return builder.build();
    }

    /**
     * Genera un producto con campo faltante para pruebas negativas
     *
     * @param campoFaltante Campo que debe estar vacío o nulo
     * @return ProductoCrud con campo faltante
     */
    public static ProductoCrud generarProductoConCampoFaltante(String campoFaltante) {
        ProductoCrud.Builder builder = ProductoCrud.builder()
                .nombre("Producto Test")
                .descripcion("Descripción test")
                .precio(BigDecimal.valueOf(100.0))
                .categoria("Electrónicos")
                .stock(10);

        switch (campoFaltante.toLowerCase()) {
            case "nombre":
                builder.nombre("");
                break;
            case "descripcion":
                builder.descripcion("");
                break;
            case "precio":
                builder.precio(null);
                break;
            case "categoria":
                builder.categoria("");
                break;
        }

        ProductoCrud producto = builder.build();
        logger.debug("Producto con campo faltante '{}' generado", campoFaltante);
        return producto;
    }

    /**
     * Genera una lista de productos para pruebas masivas
     *
     * @param cantidad Número de productos a generar
     * @return Lista de productos
     */
    public static List<ProductoCrud> generarListaProductos(int cantidad) {
        List<ProductoCrud> productos = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            productos.add(generarProductoValido());
        }
        logger.debug("Lista de {} productos generada", cantidad);
        return productos;
    }

    // Métodos de datos específicos para pruebas

    /**
     * Genera datos de login válidos
     *
     * @return Usuario con credenciales válidas
     */
    public static Usuario generarDatosLoginValidos() {
        return new Usuario("usuario_valido", "password123");
    }

    /**
     * Genera datos de login inválidos
     *
     * @return Usuario con credenciales inválidas
     */
    public static Usuario generarDatosLoginInvalidos() {
        return new Usuario("usuario_invalido", "password_incorrecto");
    }

    // Métodos auxiliares privados

    /**
     * Obtiene un elemento aleatorio de un array
     */
    private static String obtenerElementoAleatorio(String[] array) {
        int indice = ThreadLocalRandom.current().nextInt(array.length);
        return array[indice];
    }

    /**
     * Genera un nombre de usuario único
     */
    private static String generarNombreUsuario(String nombre, String apellido) {
        int numero = ThreadLocalRandom.current().nextInt(100, 999);
        return (nombre.substring(0, 1) + apellido + numero).toLowerCase();
    }

    /**
     * Genera un email válido
     */
    private static String generarEmail(String nombre, String apellido) {
        String dominio = obtenerElementoAleatorio(DOMINIOS_EMAIL);
        int numero = ThreadLocalRandom.current().nextInt(10, 99);
        return (nombre + "." + apellido + numero + "@" + dominio).toLowerCase();
    }

    /**
     * Genera una contraseña válida
     */
    private static String generarContrasenaValida() {
        return "Password" + ThreadLocalRandom.current().nextInt(100, 999) + "!";
    }

    /**
     * Genera un precio aleatorio entre 10 y 1000
     */
    private static BigDecimal generarPrecioAleatorio() {
        double precio = ThreadLocalRandom.current().nextDouble(10.0, 1000.0);
        return BigDecimal.valueOf(Math.round(precio * 100.0) / 100.0);
    }
}