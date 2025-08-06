package com.qa.automatizacion.utilidades;

import com.qa.automatizacion.modelo.Usuario;
import com.qa.automatizacion.modelo.ProductoCrud;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Generador de datos de prueba para los diferentes escenarios BDD.
 * Proporciona datos realistas y consistentes para las pruebas automatizadas.
 *
 * Principios aplicados:
 * - Factory Pattern: Crea objetos de diferentes tipos de manera consistente
 * - Single Responsibility: Se enfoca únicamente en generar datos de prueba
 * - Configurabilidad: Permite personalizar los datos generados
 * - Reutilización: Datos que pueden ser usados en múltiples escenarios
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
public class GeneradorDatos {

    private static final Random random = new Random();

    // ==================== DATOS BASE ====================

    private static final List<String> NOMBRES = Arrays.asList(
            "Juan", "María", "Carlos", "Ana", "Pedro", "Laura", "Diego", "Carmen",
            "José", "Patricia", "Manuel", "Rosa", "Francisco", "Teresa", "Antonio",
            "Isabel", "Alejandro", "Mercedes", "Fernando", "Pilar", "Ricardo", "Elena"
    );

    private static final List<String> APELLIDOS = Arrays.asList(
            "García", "Rodríguez", "González", "Fernández", "López", "Martínez", "Sánchez",
            "Pérez", "Gómez", "Martín", "Jiménez", "Ruiz", "Hernández", "Díaz", "Moreno",
            "Álvarez", "Muñoz", "Romero", "Alonso", "Gutiérrez", "Navarro", "Torres"
    );

    private static final List<String> DOMINIOS_EMAIL = Arrays.asList(
            "gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "ejemplo.com",
            "test.com", "prueba.cl", "testing.org"
    );

    private static final List<String> CATEGORIAS_PRODUCTO = Arrays.asList(
            "Electrónicos", "Ropa", "Hogar", "Deportes", "Libros", "Juguetes",
            "Salud y Belleza", "Automóviles", "Música", "Cine", "Jardinería", "Mascotas"
    );

    private static final List<String> PREFIJOS_PRODUCTO = Arrays.asList(
            "Super", "Mega", "Ultra", "Pro", "Premium", "Deluxe", "Smart", "Eco",
            "Advanced", "Professional", "Classic", "Modern", "Digital", "Wireless"
    );

    private static final List<String> NOMBRES_PRODUCTO = Arrays.asList(
            "Widget", "Gadget", "Dispositivo", "Herramienta", "Accesorio", "Componente",
            "Sistema", "Solución", "Kit", "Paquete", "Conjunto", "Colección"
    );

    // ==================== GENERACIÓN DE USUARIOS ====================

    /**
     * Genera un usuario aleatorio con datos realistas.
     *
     * @return usuario con datos generados aleatoriamente
     */
    public static Usuario generarUsuarioAleatorio() {
        String nombre = obtenerElementoAleatorio(NOMBRES);
        String apellido = obtenerElementoAleatorio(APELLIDOS);
        String nombreCompleto = nombre + " " + apellido;

        String nombreUsuario = generarNombreUsuario(nombre, apellido);
        String email = generarEmail(nombre, apellido);
        String password = generarPassword();

        Usuario usuario = new Usuario(nombreCompleto, email, nombreUsuario, password);
        usuario.setTelefono(generarTelefono());
        usuario.setRol(obtenerElementoAleatorio(Arrays.asList(Usuario.TipoRol.values())));

        return usuario;
    }

    /**
     * Genera un usuario con datos específicos para pruebas.
     *
     * @param prefijo prefijo para hacer único el usuario
     * @return usuario de prueba
     */
    public static Usuario generarUsuarioPrueba(String prefijo) {
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        String sufijo = prefijo + timestamp;

        return new Usuario(
                "Usuario Prueba " + sufijo,
                "test" + sufijo + "@ejemplo.com",
                "user" + sufijo,
                "password123"
        );
    }

    /**
     * Genera un usuario administrador para pruebas.
     *
     * @return usuario con rol de administrador
     */
    public static Usuario generarAdministradorPrueba() {
        Usuario admin = generarUsuarioPrueba("admin");
        admin.setNombreCompleto("Administrador Sistema");
        admin.setRol(Usuario.TipoRol.ADMINISTRADOR);
        return admin;
    }

    /**
     * Genera una lista de usuarios para pruebas masivas.
     *
     * @param cantidad cantidad de usuarios a generar
     * @return lista de usuarios generados
     */
    public static List<Usuario> generarListaUsuarios(int cantidad) {
        List<Usuario> usuarios = new ArrayList<>();

        for (int i = 0; i < cantidad; i++) {
            usuarios.add(generarUsuarioPrueba("bulk" + i));
        }

        return usuarios;
    }

    // ==================== GENERACIÓN DE PRODUCTOS ====================

    /**
     * Genera un producto aleatorio con datos realistas.
     *
     * @return producto con datos generados aleatoriamente
     */
    public static ProductoCrud generarProductoAleatorio() {
        String prefijo = obtenerElementoAleatorio(PREFIJOS_PRODUCTO);
        String nombre = obtenerElementoAleatorio(NOMBRES_PRODUCTO);
        String nombreProducto = prefijo + " " + nombre;

        String categoria = obtenerElementoAleatorio(CATEGORIAS_PRODUCTO);
        String descripcion = generarDescripcionProducto(nombreProducto);
        BigDecimal precio = generarPrecio();

        ProductoCrud producto = new ProductoCrud(nombreProducto, descripcion, precio, categoria);
        producto.setStockDisponible(generarStock());
        producto.setDestacado(random.nextBoolean());

        return producto;
    }

    /**
     * Genera un producto con datos específicos para pruebas.
     *
     * @param sufijo sufijo para hacer único el producto
     * @return producto de prueba
     */
    public static ProductoCrud generarProductoPrueba(String sufijo) {
        return new ProductoCrud(
                "Producto Prueba " + sufijo,
                "Descripción detallada del producto de prueba " + sufijo,
                new BigDecimal("99.99"),
                "Categoría Prueba"
        );
    }

    /**
     * Genera una lista de productos para pruebas.
     *
     * @param cantidad cantidad de productos a generar
     * @return lista de productos generados
     */
    public static List<ProductoCrud> generarListaProductos(int cantidad) {
        List<ProductoCrud> productos = new ArrayList<>();

        for (int i = 0; i < cantidad; i++) {
            productos.add(generarProductoPrueba("item" + i));
        }

        return productos;
    }

    // ==================== GENERADORES AUXILIARES ====================

    /**
     * Genera un nombre de usuario basado en nombre y apellido.
     *
     * @param nombre nombre del usuario
     * @param apellido apellido del usuario
     * @return nombre de usuario generado
     */
    private static String generarNombreUsuario(String nombre, String apellido) {
        String base = (nombre.charAt(0) + apellido).toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        int numero = random.nextInt(999) + 1;
        return base + numero;
    }

    /**
     * Genera un email basado en nombre y apellido.
     *
     * @param nombre nombre del usuario
     * @param apellido apellido del usuario
     * @return email generado
     */
    private static String generarEmail(String nombre, String apellido) {
        String localPart = (nombre + "." + apellido).toLowerCase()
                .replaceAll("[^a-z.]", "");
        String dominio = obtenerElementoAleatorio(DOMINIOS_EMAIL);

        int numero = random.nextInt(99) + 1;
        return localPart + numero + "@" + dominio;
    }

    /**
     * Genera una contraseña segura.
     *
     * @return contraseña generada
     */
    private static String generarPassword() {
        String[] passwords = {
                "password123", "admin123", "user123", "test123", "demo123",
                "segura123", "prueba123", "sistema123", "acceso123", "clave123"
        };

        return obtenerElementoAleatorio(Arrays.asList(passwords));
    }

    /**
     * Genera un número de teléfono chileno.
     *
     * @return número de teléfono generado
     */
    private static String generarTelefono() {
        // Formato: +56 9 XXXX XXXX (móvil chileno)
        StringBuilder telefono = new StringBuilder("+56 9 ");

        for (int i = 0; i < 4; i++) {
            telefono.append(random.nextInt(10));
        }
        telefono.append(" ");

        for (int i = 0; i < 4; i++) {
            telefono.append(random.nextInt(10));
        }

        return telefono.toString();
    }

    /**
     * Genera una descripción para un producto.
     *
     * @param nombreProducto nombre del producto
     * @return descripción generada
     */
    private static String generarDescripcionProducto(String nombreProducto) {
        String[] plantillas = {
                "El %s es perfecto para uso diario y ofrece excelente calidad.",
                "Descubre las características innovadoras del %s.",
                "El %s combina funcionalidad y diseño en un solo producto.",
                "Experimenta la diferencia con nuestro %s de alta calidad.",
                "El %s está diseñado para satisfacer todas tus necesidades."
        };

        String plantilla = obtenerElementoAleatorio(Arrays.asList(plantillas));
        return String.format(plantilla, nombreProducto);
    }

    /**
     * Genera un precio aleatorio realista.
     *
     * @return precio generado
     */
    private static BigDecimal generarPrecio() {
        double precio = 10.0 + (random.nextDouble() * 990.0); // Entre $10 y $1000
        return BigDecimal.valueOf(Math.round(precio * 100.0) / 100.0); // Redondear a 2 decimales
    }

    /**
     * Genera una cantidad de stock aleatoria.
     *
     * @return stock generado
     */
    private static Integer generarStock() {
        return random.nextInt(100) + 1; // Entre 1 y 100 unidades
    }

    // ==================== GENERADORES DE DATOS ESPECÍFICOS ====================

    /**
     * Genera datos de prueba para escenarios de login.
     *
     * @return conjunto de credenciales de prueba
     */
    public static class DatosLogin {
        public static final String EMAIL_VALIDO = "usuario@test.com";
        public static final String PASSWORD_VALIDO = "password123";
        public static final String EMAIL_INVALIDO = "email-invalido";
        public static final String PASSWORD_INVALIDO = "123";
        public static final String EMAIL_NO_REGISTRADO = "noexiste@test.com";
        public static final String PASSWORD_INCORRECTO = "passwordincorrecto";
    }

    /**
     * Genera datos de prueba para escenarios de registro.
     */
    public static class DatosRegistro {
        public static Usuario datosCompletos() {
            return generarUsuarioPrueba("registro");
        }

        public static Usuario datosConEmailExistente() {
            Usuario usuario = generarUsuarioPrueba("duplicado");
            usuario.setEmail(DatosLogin.EMAIL_VALIDO); // Email que ya existe
            return usuario;
        }

        public static Usuario datosConPasswordDebil() {
            Usuario usuario = generarUsuarioPrueba("debil");
            usuario.setPassword("123"); // Password muy corto
            return usuario;
        }
    }

    /**
     * Genera datos de prueba para escenarios CRUD.
     */
    public static class DatosCrud {
        public static ProductoCrud productoCompleto() {
            return generarProductoPrueba("completo");
        }

        public static ProductoCrud productoSinStock() {
            ProductoCrud producto = generarProductoPrueba("sinstock");
            producto.setStockDisponible(0);
            return producto;
        }

        public static ProductoCrud productoConPrecioAlto() {
            ProductoCrud producto = generarProductoPrueba("caro");
            producto.setPrecio(new BigDecimal("9999.99"));
            return producto;
        }
    }

    // ==================== GENERADORES DE FECHAS ====================

    /**
     * Genera una fecha aleatoria en el pasado.
     *
     * @param diasAtras máximo número de días hacia atrás
     * @return fecha generada
     */
    public static LocalDate generarFechaPasada(int diasAtras) {
        int diasAleatorios = random.nextInt(diasAtras) + 1;
        return LocalDate.now().minusDays(diasAleatorios);
    }

    /**
     * Genera una fecha aleatoria en el futuro.
     *
     * @param diasAdelante máximo número de días hacia adelante
     * @return fecha generada
     */
    public static LocalDate generarFechaFutura(int diasAdelante) {
        int diasAleatorios = random.nextInt(diasAdelante) + 1;
        return LocalDate.now().plusDays(diasAleatorios);
    }

    /**
     * Formatea una fecha para uso en campos de entrada.
     *
     * @param fecha fecha a formatear
     * @return fecha formateada como string
     */
    public static String formatearFecha(LocalDate fecha) {
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // ==================== GENERADORES DE TEXTO ====================

    /**
     * Genera un texto aleatorio de longitud específica.
     *
     * @param longitud longitud del texto a generar
     * @return texto generado
     */
    public static String generarTextoAleatorio(int longitud) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ";
        StringBuilder texto = new StringBuilder();

        for (int i = 0; i < longitud; i++) {
            int indice = random.nextInt(caracteres.length());
            texto.append(caracteres.charAt(indice));
        }

        return texto.toString().trim();
    }

    /**
     * Genera Lorem Ipsum de longitud específica.
     *
     * @param palabras número de palabras a generar
     * @return texto Lorem Ipsum generado
     */
    public static String generarLoremIpsum(int palabras) {
        String[] loremWords = {
                "lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit",
                "sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore", "et", "dolore",
                "magna", "aliqua", "enim", "ad", "minim", "veniam", "quis", "nostrud",
                "exercitation", "ullamco", "laboris", "nisi", "aliquip", "ex", "ea", "commodo"
        };

        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < palabras; i++) {
            if (i > 0) texto.append(" ");
            texto.append(obtenerElementoAleatorio(Arrays.asList(loremWords)));
        }

        return texto.toString();
    }

    // ==================== GENERADORES NUMÉRICOS ====================

    /**
     * Genera un número entero aleatorio en un rango.
     *
     * @param minimo valor mínimo (inclusivo)
     * @param maximo valor máximo (inclusivo)
     * @return número generado
     */
    public static int generarEntero(int minimo, int maximo) {
        return random.nextInt((maximo - minimo) + 1) + minimo;
    }

    /**
     * Genera un número decimal aleatorio en un rango.
     *
     * @param minimo valor mínimo (inclusivo)
     * @param maximo valor máximo (exclusivo)
     * @return número generado
     */
    public static double generarDecimal(double minimo, double maximo) {
        return minimo + (random.nextDouble() * (maximo - minimo));
    }

    // ==================== GENERADORES DE IDENTIFICADORES ====================

    /**
     * Genera un UUID simplificado para pruebas.
     *
     * @return UUID simplificado
     */
    public static String generarId() {
        return "ID-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
    }

    /**
     * Genera un código SKU aleatorio.
     *
     * @return código SKU generado
     */
    public static String generarCodigoSku() {
        String letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sku = new StringBuilder("SKU-");

        // Agregar 3 letras aleatorias
        for (int i = 0; i < 3; i++) {
            sku.append(letras.charAt(random.nextInt(letras.length())));
        }

        // Agregar 4 números aleatorios
        sku.append("-");
        for (int i = 0; i < 4; i++) {
            sku.append(random.nextInt(10));
        }

        return sku.toString();
    }

    // ==================== UTILIDADES AUXILIARES ====================

    /**
     * Obtiene un elemento aleatorio de una lista.
     *
     * @param lista lista de elementos
     * @param <T> tipo de elementos
     * @return elemento aleatorio
     */
    private static <T> T obtenerElementoAleatorio(List<T> lista) {
        return lista.get(random.nextInt(lista.size()));
    }

    /**
     * Genera un booleano aleatorio con probabilidad específica.
     *
     * @param probabilidadTrue probabilidad de que sea true (0.0 a 1.0)
     * @return booleano generado
     */
    public static boolean generarBooleano(double probabilidadTrue) {
        return random.nextDouble() < probabilidadTrue;
    }

    /**
     * Mezcla aleatoriamente una lista.
     *
     * @param lista lista a mezclar
     * @param <T> tipo de elementos
     * @return lista mezclada
     */
    public static <T> List<T> mezclarLista(List<T> lista) {
        List<T> listaMezclada = new ArrayList<>(lista);
        for (int i = listaMezclada.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            T temp = listaMezclada.get(i);
            listaMezclada.set(i, listaMezclada.get(j));
            listaMezclada.set(j, temp);
        }
        return listaMezclada;
    }

    // ==================== DATOS PARA CASOS EDGE ====================

    /**
     * Proporciona datos para casos límite y edge cases.
     */
    public static class DatosEdgeCase {
        // Strings problemáticos
        public static final String STRING_VACIO = "";
        public static final String STRING_ESPACIOS = "   ";
        public static final String STRING_MUY_LARGO = generarTextoAleatorio(1000);
        public static final String STRING_CARACTERES_ESPECIALES = "!@#$%^&*()[]{}|;':\",./<>?";
        public static final String STRING_UNICODE = "áéíóúñç测试тест";
        public static final String STRING_SQL_INJECTION = "'; DROP TABLE usuarios; --";
        public static final String STRING_XSS = "<script>alert('XSS')</script>";

        // Números problemáticos
        public static final BigDecimal PRECIO_NEGATIVO = new BigDecimal("-100.00");
        public static final BigDecimal PRECIO_CERO = BigDecimal.ZERO;
        public static final BigDecimal PRECIO_MUY_ALTO = new BigDecimal("999999.99");

        // Emails problemáticos
        public static final String EMAIL_SIN_ARROBA = "emailsinarroba.com";
        public static final String EMAIL_SIN_DOMINIO = "email@";
        public static final String EMAIL_MUY_LARGO = generarTextoAleatorio(255) + "@test.com";

        public static List<String> obtenerStringsProblemáticos() {
            return Arrays.asList(
                    STRING_VACIO, STRING_ESPACIOS, STRING_MUY_LARGO,
                    STRING_CARACTERES_ESPECIALES, STRING_UNICODE,
                    STRING_SQL_INJECTION, STRING_XSS
            );
        }
    }

    // ==================== CONFIGURACIÓN Y RESET ====================

    /**
     * Configura la semilla del generador aleatorio para reproducibilidad.
     *
     * @param semilla semilla para el generador
     */
    public static void configurarSemilla(long semilla) {
        random.setSeed(semilla);
    }

    /**
     * Resetea el generador aleatorio.
     */
    public static void resetearGenerador() {
        random.setSeed(System.currentTimeMillis());
    }
}