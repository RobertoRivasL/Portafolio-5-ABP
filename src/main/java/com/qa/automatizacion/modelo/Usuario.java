package com.qa.automatizacion.modelo;

/**
 * Modelo de datos para representar un usuario del sistema.
 * Encapsula la información básica necesaria para las pruebas de autenticación.
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0
 */
public class Usuario {

    private String email;
    private String password;
    private String nombre;
    private boolean recordarSesion = false;

    // ==================== CONSTRUCTORES ====================

    /**
     * Constructor por defecto.
     */
    public Usuario() {
    }

    /**
     * Constructor con email y contraseña.
     */
    public Usuario(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Constructor completo.
     */
    public Usuario(String email, String password, String nombre) {
        this.email = email;
        this.password = password;
        this.nombre = nombre;
    }

    // ==================== GETTERS Y SETTERS ====================

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isRecordarSesion() {
        return recordarSesion;
    }

    public void setRecordarSesion(boolean recordarSesion) {
        this.recordarSesion = recordarSesion;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Verifica si el usuario tiene credenciales válidas.
     */
    public boolean tieneCredencialesValidas() {
        return email != null && !email.trim().isEmpty() &&
                password != null && !password.trim().isEmpty();
    }

    /**
     * Obtiene el nombre de usuario a partir del email.
     */
    public String obtenerNombreUsuario() {
        if (nombre != null && !nombre.trim().isEmpty()) {
            return nombre;
        }

        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }

        return "Usuario";
    }

    @Override
    public String toString() {
        return String.format("Usuario{email='%s', nombre='%s', recordarSesion=%s}",
                email, nombre, recordarSesion);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Usuario usuario = (Usuario) obj;
        return email != null ? email.equals(usuario.email) : usuario.email == null;
    }

    @Override
    public int hashCode() {
        return email != null ? email.hashCode() : 0;
    }
}