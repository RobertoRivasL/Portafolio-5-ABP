package com.qa.automatizacion.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modelo de datos para representar un usuario del sistema.
 *
 * Principios aplicados:
 * - Encapsulación: Atributos privados con acceso controlado
 * - Single Responsibility: Se enfoca únicamente en representar un usuario
 * - Builder Pattern: Construcción flexible de objetos
 *
 * @author Equipo QA Automatización
 * @version 1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Usuario {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("nombreUsuario")
    private String nombreUsuario;

    @JsonProperty("email")
    private String email;

    @JsonProperty("contrasena")
    private String contrasena;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("apellido")
    private String apellido;

    @JsonProperty("activo")
    private Boolean activo;

    @JsonProperty("fechaCreacion")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCreacion;

    @JsonProperty("fechaUltimoAcceso")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaUltimoAcceso;

    @JsonProperty("rol")
    private RolUsuario rol;

    // Constructores

    /**
     * Constructor por defecto
     */
    public Usuario() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
        this.rol = RolUsuario.USUARIO;
    }

    /**
     * Constructor completo
     */
    public Usuario(Long id, String nombreUsuario, String email, String contrasena,
                   String nombre, String apellido, Boolean activo, LocalDateTime fechaCreacion,
                   LocalDateTime fechaUltimoAcceso, RolUsuario rol) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.email = email;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.apellido = apellido;
        this.activo = activo != null ? activo : true;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
        this.fechaUltimoAcceso = fechaUltimoAcceso;
        this.rol = rol != null ? rol : RolUsuario.USUARIO;
    }

    /**
     * Constructor simplificado para registro
     */
    public Usuario(String nombreUsuario, String email, String contrasena, String nombre, String apellido) {
        this();
        this.nombreUsuario = nombreUsuario;
        this.email = email;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    /**
     * Constructor para login básico
     */
    public Usuario(String nombreUsuario, String contrasena) {
        this();
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
    }

    /**
     * Constructor privado para Builder
     */
    private Usuario(Builder builder) {
        this.id = builder.id;
        this.nombreUsuario = builder.nombreUsuario;
        this.email = builder.email;
        this.contrasena = builder.contrasena;
        this.nombre = builder.nombre;
        this.apellido = builder.apellido;
        this.activo = builder.activo;
        this.fechaCreacion = builder.fechaCreacion;
        this.fechaUltimoAcceso = builder.fechaUltimoAcceso;
        this.rol = builder.rol;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaUltimoAcceso() {
        return fechaUltimoAcceso;
    }

    public void setFechaUltimoAcceso(LocalDateTime fechaUltimoAcceso) {
        this.fechaUltimoAcceso = fechaUltimoAcceso;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    // Métodos de utilidad

    /**
     * Obtiene el nombre completo del usuario
     *
     * @return Nombre y apellido concatenados
     */
    public String getNombreCompleto() {
        return String.format("%s %s", nombre != null ? nombre : "", apellido != null ? apellido : "").trim();
    }

    /**
     * Verifica si el usuario tiene datos básicos válidos
     *
     * @return true si es válido, false en caso contrario
     */
    public boolean esValido() {
        return nombreUsuario != null && !nombreUsuario.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                contrasena != null && !contrasena.trim().isEmpty();
    }

    /**
     * Actualiza la fecha de último acceso
     */
    public void actualizarUltimoAcceso() {
        this.fechaUltimoAcceso = LocalDateTime.now();
    }

    /**
     * Verifica si el usuario está activo
     *
     * @return true si está activo, false en caso contrario
     */
    public boolean estaActivo() {
        return activo != null && activo;
    }

    // Métodos de Object

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) &&
                Objects.equals(nombreUsuario, usuario.nombreUsuario) &&
                Objects.equals(email, usuario.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombreUsuario, email);
    }

    @Override
    public String toString() {
        return String.format("Usuario{id=%d, nombreUsuario='%s', email='%s', nombre='%s', apellido='%s', activo=%s, rol=%s}",
                id, nombreUsuario, email, nombre, apellido, activo, rol);
    }

    // Builder Pattern

    /**
     * Crea un nuevo Builder
     *
     * @return Nueva instancia del Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Clase Builder para construcción flexible de Usuario
     */
    public static class Builder {
        private Long id;
        private String nombreUsuario;
        private String email;
        private String contrasena;
        private String nombre;
        private String apellido;
        private Boolean activo = true;
        private LocalDateTime fechaCreacion = LocalDateTime.now();
        private LocalDateTime fechaUltimoAcceso;
        private RolUsuario rol = RolUsuario.USUARIO;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder nombreUsuario(String nombreUsuario) {
            this.nombreUsuario = nombreUsuario;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder contrasena(String contrasena) {
            this.contrasena = contrasena;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder apellido(String apellido) {
            this.apellido = apellido;
            return this;
        }

        public Builder activo(Boolean activo) {
            this.activo = activo;
            return this;
        }

        public Builder fechaCreacion(LocalDateTime fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
            return this;
        }

        public Builder fechaUltimoAcceso(LocalDateTime fechaUltimoAcceso) {
            this.fechaUltimoAcceso = fechaUltimoAcceso;
            return this;
        }

        public Builder rol(RolUsuario rol) {
            this.rol = rol;
            return this;
        }

        public Usuario build() {
            return new Usuario(this);
        }
    }

    // Enum para roles de usuario
    public enum RolUsuario {
        @JsonProperty("ADMINISTRADOR")
        ADMINISTRADOR("Administrador"),

        @JsonProperty("USUARIO")
        USUARIO("Usuario"),

        @JsonProperty("MODERADOR")
        MODERADOR("Moderador"),

        @JsonProperty("INVITADO")
        INVITADO("Invitado");

        private final String descripcion;

        RolUsuario(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }

        @Override
        public String toString() {
            return descripcion;
        }

        /**
         * Obtiene el rol por descripción
         *
         * @param descripcion Descripción del rol
         * @return RolUsuario correspondiente o USUARIO si no se encuentra
         */
        public static RolUsuario porDescripcion(String descripcion) {
            if (descripcion == null) {
                return USUARIO;
            }

            for (RolUsuario rol : values()) {
                if (rol.getDescripcion().equalsIgnoreCase(descripcion) ||
                        rol.name().equalsIgnoreCase(descripcion)) {
                    return rol;
                }
            }
            return USUARIO;
        }
    }
}