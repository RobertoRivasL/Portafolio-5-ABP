package com.qa.automatizacion.pruebas.modelo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modelo de datos para representar un Usuario del sistema
 * Implementa el patrón Builder y encapsulación de datos
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Usuario {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("nombreUsuario")
    private String nombreUsuario;

    @JsonProperty("email")
    private String email;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("apellido")
    private String apellido;

    @JsonProperty("contrasena")
    private String contrasena;

    @JsonProperty("activo")
    private Boolean activo;

    @JsonProperty("fechaCreacion")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    @JsonProperty("ultimoAcceso")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ultimoAcceso;

    @JsonProperty("rol")
    private TipoRol rol;

    @JsonProperty("telefono")
    private String telefono;

    @JsonProperty("direccion")
    private String direccion;

    @JsonProperty("fechaNacimiento")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime fechaNacimiento;

    // Constructor por defecto
    public Usuario() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
        this.rol = TipoRol.USUARIO;
    }

    // Constructor completo
    private Usuario(Builder builder) {
        this.id = builder.id;
        this.nombreUsuario = builder.nombreUsuario;
        this.email = builder.email;
        this.nombre = builder.nombre;
        this.apellido = builder.apellido;
        this.contrasena = builder.contrasena;
        this.activo = builder.activo != null ? builder.activo : true;
        this.fechaCreacion = builder.fechaCreacion != null ? builder.fechaCreacion : LocalDateTime.now();
        this.ultimoAcceso = builder.ultimoAcceso;
        this.rol = builder.rol != null ? builder.rol : TipoRol.USUARIO;
        this.telefono = builder.telefono;
        this.direccion = builder.direccion;
        this.fechaNacimiento = builder.fechaNacimiento;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getEmail() {
        return email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getContrasena() {
        return contrasena;
    }

    public Boolean getActivo() {
        return activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public TipoRol getRol() {
        return rol;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public LocalDateTime getFechaNacimiento() {
        return fechaNacimiento;
    }

    // Setters para casos específicos (preferir usar Builder)
    public void setId(Long id) {
        this.id = id;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    /**
     * Obtiene el nombre completo del usuario
     *
     * @return Nombre y apellido concatenados
     */
    public String getNombreCompleto() {
        if (nombre == null && apellido == null) {
            return nombreUsuario;
        }

        StringBuilder nombreCompleto = new StringBuilder();
        if (nombre != null && !nombre.trim().isEmpty()) {
            nombreCompleto.append(nombre);
        }
        if (apellido != null && !apellido.trim().isEmpty()) {
            if (nombreCompleto.length() > 0) {
                nombreCompleto.append(" ");
            }
            nombreCompleto.append(apellido);
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : nombreUsuario;
    }

    /**
     * Verifica si el usuario está activo
     *
     * @return true si el usuario está activo
     */
    public boolean estaActivo() {
        return activo != null && activo;
    }

    /**
     * Verifica si el usuario es administrador
     *
     * @return true si el usuario tiene rol de administrador
     */
    public boolean esAdministrador() {
        return rol == TipoRol.ADMINISTRADOR;
    }

    /**
     * Verifica si el usuario es moderador
     *
     * @return true si el usuario tiene rol de moderador
     */
    public boolean esModerador() {
        return rol == TipoRol.MODERADOR;
    }

    /**
     * Verifica si el usuario tiene privilegios elevados (admin o moderador)
     *
     * @return true si el usuario tiene privilegios elevados
     */
    public boolean tienePrivilegiosElevados() {
        return esAdministrador() || esModerador();
    }

    /**
     * Crea una copia del usuario sin datos sensibles
     *
     * @return Usuario sin contraseña
     */
    public Usuario copiarSinDatosSensibles() {
        return Usuario.builder()
                .id(this.id)
                .nombreUsuario(this.nombreUsuario)
                .email(this.email)
                .nombre(this.nombre)
                .apellido(this.apellido)
                .activo(this.activo)
                .fechaCreacion(this.fechaCreacion)
                .ultimoAcceso(this.ultimoAcceso)
                .rol(this.rol)
                .telefono(this.telefono)
                .direccion(this.direccion)
                .fechaNacimiento(this.fechaNacimiento)
                .build();
    }

    /**
     * Valida los datos básicos del usuario
     *
     * @return true si los datos son válidos
     */
    public boolean esValido() {
        return nombreUsuario != null && !nombreUsuario.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                email.contains("@") && email.contains(".") &&
                contrasena != null && contrasena.length() >= 6;
    }

    /**
     * Valida el formato del email
     *
     * @return true si el email tiene formato válido
     */
    public boolean tieneEmailValido() {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Validación básica de email
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Valida la fortaleza de la contraseña
     *
     * @return true si la contraseña cumple criterios mínimos
     */
    public boolean tieneContrasenaSegura() {
        if (contrasena == null || contrasena.length() < 8) {
            return false;
        }

        // Verificar que tenga al menos una letra minúscula, mayúscula y número
        boolean tieneMinuscula = contrasena.chars().anyMatch(Character::isLowerCase);
        boolean tieneMayuscula = contrasena.chars().anyMatch(Character::isUpperCase);
        boolean tieneNumero = contrasena.chars().anyMatch(Character::isDigit);

        return tieneMinuscula && tieneMayuscula && tieneNumero;
    }

    /**
     * Calcula la edad del usuario basada en fecha de nacimiento
     *
     * @return Edad en años, -1 si no hay fecha de nacimiento
     */
    public int calcularEdad() {
        if (fechaNacimiento == null) {
            return -1;
        }

        LocalDateTime ahora = LocalDateTime.now();
        int edad = ahora.getYear() - fechaNacimiento.getYear();

        // Ajustar si no ha cumplido años este año
        if (ahora.getDayOfYear() < fechaNacimiento.getDayOfYear()) {
            edad--;
        }

        return edad;
    }

    /**
     * Verifica si el usuario es mayor de edad
     *
     * @return true si es mayor de 18 años
     */
    public boolean esMayorDeEdad() {
        return calcularEdad() >= 18;
    }

    /**
     * Obtiene información de contacto principal
     *
     * @return Email o teléfono como contacto principal
     */
    public String getContactoPrincipal() {
        if (email != null && !email.trim().isEmpty()) {
            return email;
        }
        if (telefono != null && !telefono.trim().isEmpty()) {
            return telefono;
        }
        return "Sin contacto";
    }

    /**
     * Marca el último acceso con la fecha/hora actual
     */
    public void actualizarUltimoAcceso() {
        this.ultimoAcceso = LocalDateTime.now();
    }

    /**
     * Verifica si el usuario ha accedido recientemente (últimas 24 horas)
     *
     * @return true si ha accedido en las últimas 24 horas
     */
    public boolean haAccedidoRecientemente() {
        if (ultimoAcceso == null) {
            return false;
        }

        LocalDateTime hace24Horas = LocalDateTime.now().minusHours(24);
        return ultimoAcceso.isAfter(hace24Horas);
    }

    // Método estático para crear Builder
    public static Builder builder() {
        return new Builder();
    }

    // Clase Builder interna
    public static class Builder {
        private Long id;
        private String nombreUsuario;
        private String email;
        private String nombre;
        private String apellido;
        private String contrasena;
        private Boolean activo;
        private LocalDateTime fechaCreacion;
        private LocalDateTime ultimoAcceso;
        private TipoRol rol;
        private String telefono;
        private String direccion;
        private LocalDateTime fechaNacimiento;

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

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder apellido(String apellido) {
            this.apellido = apellido;
            return this;
        }

        public Builder contrasena(String contrasena) {
            this.contrasena = contrasena;
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

        public Builder ultimoAcceso(LocalDateTime ultimoAcceso) {
            this.ultimoAcceso = ultimoAcceso;
            return this;
        }

        public Builder rol(TipoRol rol) {
            this.rol = rol;
            return this;
        }

        public Builder telefono(String telefono) {
            this.telefono = telefono;
            return this;
        }

        public Builder direccion(String direccion) {
            this.direccion = direccion;
            return this;
        }

        public Builder fechaNacimiento(LocalDateTime fechaNacimiento) {
            this.fechaNacimiento = fechaNacimiento;
            return this;
        }

        public Usuario build() {
            return new Usuario(this);
        }
    }

    // Enum para tipos de rol
    public enum TipoRol {
        @JsonProperty("USUARIO")
        USUARIO("Usuario", 1),

        @JsonProperty("MODERADOR")
        MODERADOR("Moderador", 2),

        @JsonProperty("ADMINISTRADOR")
        ADMINISTRADOR("Administrador", 3);

        private final String descripcion;
        private final int nivelAutorizacion;

        TipoRol(String descripcion, int nivelAutorizacion) {
            this.descripcion = descripcion;
            this.nivelAutorizacion = nivelAutorizacion;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public int getNivelAutorizacion() {
            return nivelAutorizacion;
        }

        /**
         * Verifica si este rol tiene al menos el nivel de autorización del rol dado
         *
         * @param rolRequerido Rol mínimo requerido
         * @return true si tiene autorización suficiente
         */
        public boolean tieneAutorizacion(TipoRol rolRequerido) {
            return this.nivelAutorizacion >= rolRequerido.nivelAutorizacion;
        }

        /**
         * Obtiene el rol por descripción
         *
         * @param descripcion Descripción del rol
         * @return TipoRol correspondiente o USUARIO si no se encuentra
         */
        public static TipoRol porDescripcion(String descripcion) {
            if (descripcion == null) {
                return USUARIO;
            }

            for (TipoRol rol : values()) {
                if (rol.descripcion.equalsIgnoreCase(descripcion)) {
                    return rol;
                }
            }
            return USUARIO;
        }

        @Override
        public String toString() {
            return descripcion;
        }
    }

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
        return "Usuario{" +
                "id=" + id +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", email='" + email + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                ", ultimoAcceso=" + ultimoAcceso +
                ", rol=" + rol +
                ", telefono='" + telefono + '\'' +
                ", direccion='" + direccion + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                '}';
    }

    /**
     * Representación resumida del usuario para logging
     *
     * @return String con información básica del usuario
     */
    public String toStringResumido() {
        return String.format("Usuario{id=%d, username='%s', email='%s', rol=%s, activo=%s}",
                id, nombreUsuario, email, rol, activo);
    }
}