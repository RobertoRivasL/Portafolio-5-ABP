package com.qa.automatizacion.utilidades;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Utilidad para validación de formatos de datos.
 *
 * Aplica los principios SOLID:
 * - Single Responsibility: Solo se encarga de validaciones de formato
 * - Open/Closed: Extensible para nuevos tipos de validación sin modificar existentes
 * - Liskov Substitution: Métodos consistentes que pueden sustituirse
 * - Interface Segregation: Métodos específicos para cada tipo de validación
 * - Dependency Inversion: No depende de implementaciones concretas
 *
 * Patrones aplicados:
 * - Utility Pattern: Clase con métodos estáticos
 * - Validation Pattern: Centraliza todas las validaciones de formato
 */
public final class ValidadorFormatos {

    private static final Logger logger = LoggerFactory.getLogger(ValidadorFormatos.class);

    // ==================== PATRONES DE VALIDACIÓN ====================

    /**
     * Patrón para validación de email según RFC 5322 simplificado.
     */
    private static final Pattern PATRON_EMAIL = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    /**
     * Patrón para validación de RUT chileno.
     */
    private static final Pattern PATRON_RUT = Pattern.compile(
            "^[0-9]{1,2}\\.[0-9]{3}\\.[0-9]{3}-[0-9kK]$"
    );

    /**
     * Patrón para validación de teléfono chileno.
     */
    private static final Pattern PATRON_TELEFONO_CL = Pattern.compile(
            "^(\\+56)?[9][0-9]{8}$"
    );

    /**
     * Patrón para validación de códigos postales chilenos.
     */
    private static final Pattern PATRON_CODIGO_POSTAL_CL = Pattern.compile(
            "^[0-9]{7}$"
    );

    /**
     * Patrón para validación de contraseñas seguras.
     * Mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número y un carácter especial.
     */
    private static final Pattern PATRON_PASSWORD_SEGURA = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    /**
     * Patrón para validación de nombres de usuario.
     * Solo letras, números, guiones y guiones bajos. Entre 3 y 20 caracteres.
     */
    private static final Pattern PATRON_NOMBRE_USUARIO = Pattern.compile(
            "^[a-zA-Z0-9._-]{3,20}$"
    );

    /**
     * Patrón para validación de SKU de productos.
     * Formato: AAA-BBB-999-9999 (letras, números, guiones)
     */
    private static final Pattern PATRON_SKU = Pattern.compile(
            "^[A-Z]{2,4}-[A-Z]{2,4}-[A-Z0-9]{2,4}-[0-9]{4}$"
    );

    // Constructor privado para evitar instanciación
    private ValidadorFormatos() {
        throw new UnsupportedOperationException("Clase de utilidad no instanciable");
    }

    // ==================== VALIDACIONES DE EMAIL ====================

    /**
     * Valida el formato de un email.
     *
     * @param email Email a validar
     * @return true si el formato es válido
     */
    public static boolean esEmailValido(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.debug("Email es nulo o vacío");
            return false;
        }

        String emailLimpio = email.trim().toLowerCase();
        boolean esValido = PATRON_EMAIL.matcher(emailLimpio).matches();

        logger.debug("Validación email '{}': {}", email, esValido);
        return esValido;
    }

    /**
     * Valida que el email no sea de dominios temporales conocidos.
     *
     * @param email Email a validar
     * @return true si no es un email temporal
     */
    public static boolean noEsEmailTemporal(String email) {
        if (!esEmailValido(email)) {
            return false;
        }

        String[] dominiosTemporales = {
                "10minutemail.com", "tempmail.org", "guerrillamail.com",
                "mailinator.com", "throwaway.email", "temp-mail.org"
        };

        String dominio = email.substring(email.indexOf('@') + 1).toLowerCase();

        for (String dominioTemporal : dominiosTemporales) {
            if (dominio.equals(dominioTemporal)) {
                logger.debug("Email temporal detectado: {}", email);
                return false;
            }
        }

        return true;
    }

    // ==================== VALIDACIONES DE CONTRASEÑA ====================

    /**
     * Valida que una contraseña cumple con los criterios de seguridad.
     *
     * @param password Contraseña a validar
     * @return true si la contraseña es segura
     */
    public static boolean esPasswordSegura(String password) {
        if (password == null) {
            logger.debug("Password es nulo");
            return false;
        }

        boolean esSegura = PATRON_PASSWORD_SEGURA.matcher(password).matches();
        logger.debug("Validación password segura: {}", esSegura);
        return esSegura;
    }

    /**
     * Calcula el nivel de fortaleza de una contraseña.
     *
     * @param password Contraseña a evaluar
     * @return Nivel de fortaleza (Muy Débil, Débil, Media, Fuerte, Muy Fuerte)
     */
    public static String calcularFortalezaPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Muy Débil";
        }

        int puntuacion = 0;

        // Longitud
        if (password.length() >= 8) puntuacion++;
        if (password.length() >= 12) puntuacion++;

        // Complejidad de caracteres
        if (password.chars().anyMatch(Character::isLowerCase)) puntuacion++;
        if (password.chars().anyMatch(Character::isUpperCase)) puntuacion++;
        if (password.chars().anyMatch(Character::isDigit)) puntuacion++;
        if (password.chars().anyMatch(ch -> "@$!%*?&".indexOf(ch) >= 0)) puntuacion++;

        // Sin patrones comunes
        if (!contienePatronesComunes(password)) puntuacion++;

        return switch (puntuacion) {
            case 0, 1 -> "Muy Débil";
            case 2, 3 -> "Débil";
            case 4, 5 -> "Media";
            case 6 -> "Fuerte";
            default -> "Muy Fuerte";
        };
    }

    /**
     * Verifica si la contraseña contiene patrones comunes débiles.
     *
     * @param password Contraseña a verificar
     * @return true si contiene patrones comunes
     */
    private static boolean contienePatronesComunes(String password) {
        String[] patronesComunes = {
                "password", "123456", "qwerty", "admin", "user", "test",
                "12345", "abc123", "password123", "123456789", "welcome"
        };

        String passwordLower = password.toLowerCase();

        for (String patron : patronesComunes) {
            if (passwordLower.contains(patron)) {
                return true;
            }
        }

        // Verificar secuencias numéricas (123, 234, etc.)
        return contieneSecuenciaNumerica(password) || contieneSecuenciaAlfabetica(password);
    }

    /**
     * Verifica si contiene secuencias numéricas de 3 o más dígitos consecutivos.
     */
    private static boolean contieneSecuenciaNumerica(String password) {
        for (int i = 0; i <= password.length() - 3; i++) {
            if (Character.isDigit(password.charAt(i))) {
                boolean esSecuencia = true;
                for (int j = 1; j < 3; j++) {
                    if (i + j >= password.length() ||
                            !Character.isDigit(password.charAt(i + j)) ||
                            password.charAt(i + j) != password.charAt(i) + j) {
                        esSecuencia = false;
                        break;
                    }
                }
                if (esSecuencia) return true;
            }
        }
        return false;
    }

    /**
     * Verifica si contiene secuencias alfabéticas de 3 o más letras consecutivas.
     */
    private static boolean contieneSecuenciaAlfabetica(String password) {
        String passwordLower = password.toLowerCase();
        for (int i = 0; i <= passwordLower.length() - 3; i++) {
            if (Character.isLetter(passwordLower.charAt(i))) {
                boolean esSecuencia = true;
                for (int j = 1; j < 3; j++) {
                    if (i + j >= passwordLower.length() ||
                            !Character.isLetter(passwordLower.charAt(i + j)) ||
                            passwordLower.charAt(i + j) != passwordLower.charAt(i) + j) {
                        esSecuencia = false;
                        break;
                    }
                }
                if (esSecuencia) return true;
            }
        }
        return false;
    }

    // ==================== VALIDACIONES DE DATOS CHILENOS ====================

    /**
     * Valida el formato de un RUT chileno.
     *
     * @param rut RUT en formato XX.XXX.XXX-X
     * @return true si el formato y dígito verificador son válidos
     */
    public static boolean esRutValido(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }

        String rutLimpio = rut.trim().toUpperCase();

        if (!PATRON_RUT.matcher(rutLimpio).matches()) {
            logger.debug("RUT no cumple formato: {}", rut);
            return false;
        }

        return validarDigitoVerificadorRut(rutLimpio);
    }

    /**
     * Valida el dígito verificador del RUT usando algoritmo chileno estándar.
     */
    private static boolean validarDigitoVerificadorRut(String rut) {
        String rutSinGuiones = rut.replaceAll("\\.|\\-", "");
        String numero = rutSinGuiones.substring(0, rutSinGuiones.length() - 1);
        char digitoVerificador = rutSinGuiones.charAt(rutSinGuiones.length() - 1);

        int suma = 0;
        int multiplicador = 2;

        for (int i = numero.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(numero.charAt(i)) * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }

        int resto = suma % 11;
        char digitoCalculado = (resto < 2) ? (char) ('0' + resto) :
                (resto == 10) ? 'K' : (char) ('0' + (11 - resto));

        boolean esValido = digitoCalculado == digitoVerificador;
        logger.debug("Validación dígito verificador RUT {}: {}", rut, esValido);

        return esValido;
    }

    /**
     * Valida el formato de un teléfono chileno.
     *
     * @param telefono Teléfono a validar (formato: +569XXXXXXXX o 9XXXXXXXX)
     * @return true si el formato es válido
     */
    public static boolean esTelefonoChilenoValido(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return false;
        }

        String telefonoLimpio = telefono.trim().replaceAll("\\s+", "");
        boolean esValido = PATRON_TELEFONO_CL.matcher(telefonoLimpio).matches();

        logger.debug("Validación teléfono chileno '{}': {}", telefono, esValido);
        return esValido;
    }

    // ==================== VALIDACIONES DE FECHAS ====================

    /**
     * Valida que una fecha esté en formato ISO (YYYY-MM-DD).
     *
     * @param fecha Fecha en formato string
     * @return true si el formato es válido y la fecha existe
     */
    public static boolean esFechaValidaISO(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) {
            return false;
        }

        try {
            LocalDate.parse(fecha.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            logger.debug("Fecha ISO válida: {}", fecha);
            return true;
        } catch (DateTimeParseException e) {
            logger.debug("Fecha ISO inválida '{}': {}", fecha, e.getMessage());
            return false;
        }
    }

    /**
     * Valida que una fecha/hora esté en formato ISO (YYYY-MM-DDTHH:mm:ss).
     *
     * @param fechaHora Fecha/hora en formato string
     * @return true si el formato es válido y la fecha existe
     */
    public static boolean esFechaHoraValidaISO(String fechaHora) {
        if (fechaHora == null || fechaHora.trim().isEmpty()) {
            return false;
        }

        try {
            LocalDateTime.parse(fechaHora.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            logger.debug("Fecha/hora ISO válida: {}", fechaHora);
            return true;
        } catch (DateTimeParseException e) {
            logger.debug("Fecha/hora ISO inválida '{}': {}", fechaHora, e.getMessage());
            return false;
        }
    }

    /**
     * Valida que una fecha no sea futura.
     *
     * @param fecha Fecha a validar
     * @return true si la fecha no es posterior a hoy
     */
    public static boolean noEsFechaFutura(String fecha) {
        if (!esFechaValidaISO(fecha)) {
            return false;
        }

        LocalDate fechaParsed = LocalDate.parse(fecha.trim());
        LocalDate hoy = LocalDate.now();

        boolean noEsFutura = !fechaParsed.isAfter(hoy);
        logger.debug("Validación fecha no futura '{}': {}", fecha, noEsFutura);

        return noEsFutura;
    }

    // ==================== VALIDACIONES DE PRODUCTOS ====================

    /**
     * Valida el formato de un SKU de producto.
     *
     * @param sku SKU a validar
     * @return true si el formato es válido
     */
    public static boolean esSkuValido(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            return false;
        }

        String skuLimpio = sku.trim().toUpperCase();
        boolean esValido = PATRON_SKU.matcher(skuLimpio).matches();

        logger.debug("Validación SKU '{}': {}", sku, esValido);
        return esValido;
    }

    /**
     * Valida que un precio sea válido (positivo y con máximo 2 decimales).
     *
     * @param precio Precio a validar
     * @return true si el precio es válido
     */
    public static boolean esPrecioValido(String precio) {
        if (precio == null || precio.trim().isEmpty()) {
            return false;
        }

        try {
            double precioDouble = Double.parseDouble(precio.trim());

            // Debe ser positivo
            if (precioDouble <= 0) {
                logger.debug("Precio no positivo: {}", precio);
                return false;
            }

            // Validar máximo 2 decimales
            String[] partes = precio.trim().split("\\.");
            if (partes.length > 1 && partes[1].length() > 2) {
                logger.debug("Precio con más de 2 decimales: {}", precio);
                return false;
            }

            logger.debug("Precio válido: {}", precio);
            return true;

        } catch (NumberFormatException e) {
            logger.debug("Precio con formato inválido '{}': {}", precio, e.getMessage());
            return false;
        }
    }

    // ==================== VALIDACIONES GENERALES ====================

    /**
     * Valida que un nombre de usuario cumpla con las reglas establecidas.
     *
     * @param nombreUsuario Nombre de usuario a validar
     * @return true si es válido
     */
    public static boolean esNombreUsuarioValido(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            return false;
        }

        String nombreLimpio = nombreUsuario.trim();
        boolean esValido = PATRON_NOMBRE_USUARIO.matcher(nombreLimpio).matches();

        logger.debug("Validación nombre usuario '{}': {}", nombreUsuario, esValido);
        return esValido;
    }

    /**
     * Valida que un texto no contenga caracteres especiales peligrosos.
     *
     * @param texto Texto a validar
     * @return true si el texto es seguro
     */
    public static boolean esTextoSeguro(String texto) {
        if (texto == null) {
            return true; // null es considerado seguro
        }

        String[] caracteresProhibidos = {
                "<script", "</script", "javascript:", "vbscript:",
                "onload=", "onclick=", "onerror=", "alert(", "eval(",
                "DROP TABLE", "DELETE FROM", "INSERT INTO", "UPDATE SET",
                "--", "/*", "*/"
        };

        String textoLower = texto.toLowerCase();

        for (String prohibido : caracteresProhibidos) {
            if (textoLower.contains(prohibido)) {
                logger.warn("Texto contiene caracteres peligrosos: {}", texto);
                return false;
            }
        }

        return true;
    }

    /**
     * Valida código postal chileno.
     *
     * @param codigoPostal Código postal a validar
     * @return true si es válido
     */
    public static boolean esCodigoPostalChilenoValido(String codigoPostal) {
        if (codigoPostal == null || codigoPostal.trim().isEmpty()) {
            return false;
        }

        String codigoLimpio = codigoPostal.trim();
        boolean esValido = PATRON_CODIGO_POSTAL_CL.matcher(codigoLimpio).matches();

        logger.debug("Validación código postal '{}': {}", codigoPostal, esValido);
        return esValido;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Limpia y normaliza un string eliminando espacios extras y caracteres de control.
     *
     * @param texto Texto a limpiar
     * @return Texto limpio o null si el input era null
     */
    public static String limpiarTexto(String texto) {
        if (texto == null) {
            return null;
        }

        return texto.trim()
                .replaceAll("\\s+", " ")  // Múltiples espacios -> un espacio
                .replaceAll("[\\p{Cntrl}]", ""); // Eliminar caracteres de control
    }

    /**
     * Formatea un RUT agregando puntos y guión si no los tiene.
     *
     * @param rut RUT sin formato
     * @return RUT formateado o el original si no se puede formatear
     */
    public static String formatearRut(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return rut;
        }

        String rutLimpio = rut.replaceAll("[^0-9kK]", "").toUpperCase();

        if (rutLimpio.length() < 2) {
            return rut; // Muy corto para ser un RUT válido
        }

        String numero = rutLimpio.substring(0, rutLimpio.length() - 1);
        String digitoVerificador = rutLimpio.substring(rutLimpio.length() - 1);

        StringBuilder rutFormateado = new StringBuilder();

        // Agregar puntos cada 3 dígitos desde la derecha
        for (int i = numero.length() - 1, contador = 0; i >= 0; i--, contador++) {
            if (contador > 0 && contador % 3 == 0) {
                rutFormateado.insert(0, ".");
            }
            rutFormateado.insert(0, numero.charAt(i));
        }

        rutFormateado.append("-").append(digitoVerificador);

        String resultado = rutFormateado.toString();
        logger.debug("RUT formateado de '{}' a '{}'", rut, resultado);

        return resultado;
    }
}