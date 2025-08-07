package com.qa.automatizacion.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modelo de datos para representar un producto en operaciones CRUD.
 * Implementa el patrón Builder para una construcción flexible de objetos.
 *
 * Principios aplicados:
 * - Encapsulación: Atributos privados con getters y setters
 * - Inmutabilidad: Posibilidad de crear objetos inmutables
 * - Builder Pattern: Construcción flexible de objetos complejos
 * - Single Responsibility: Se enfoca únicamente en representar un producto
 *
 * @author Equipo QA Automatización
 * @version 1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoCrud {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("precio")
    private BigDecimal precio;

    @JsonProperty("categoria")
    private String categoria;

    @JsonProperty("stock")
    private Integer stock;

    @JsonProperty("activo")
    private Boolean activo;

    @JsonProperty("fechaCreacion")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCreacion;

    @JsonProperty("fechaModificacion")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaModificacion;

    @JsonProperty("categoriaProducto")
    private CategoriaProducto categoriaProducto;

    // Constructores

    /**
     * Constructor por defecto requerido para Jackson
     */
    public ProductoCrud() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
        this.stock = 0;
    }

    /**
     * Constructor completo para crear un producto con todos los datos
     */
    public ProductoCrud(Long id, String nombre, String descripcion, BigDecimal precio,
                        String categoria, Integer stock, Boolean activo,
                        LocalDateTime fechaCreacion, LocalDateTime fechaModificacion,
                        CategoriaProducto categoriaProducto) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.stock = stock != null ? stock : 0;
        this.activo = activo != null ? activo : true;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
        this.fechaModificacion = fechaModificacion;
        this.categoriaProducto = categoriaProducto;
    }

    /**
     * Constructor simplificado para productos básicos
     */
    public ProductoCrud(String nombre, String descripcion, BigDecimal precio, String categoria) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.categoriaProducto = CategoriaProducto.porDescripcion(categoria);
    }

    /**
     * Constructor privado para el Builder
     */
    private ProductoCrud(Builder builder) {
        this.id = builder.id;
        this.nombre = builder.nombre;
        this.descripcion = builder.descripcion;
        this.precio = builder.precio;
        this.categoria = builder.categoria;
        this.stock = builder.stock;
        this.activo = builder.activo;
        this.fechaCreacion = builder.fechaCreacion;
        this.fechaModificacion = builder.fechaModificacion;
        this.categoriaProducto = builder.categoriaProducto;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
        this.categoriaProducto = CategoriaProducto.porDescripcion(categoria);
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
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

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public CategoriaProducto getCategoriaProducto() {
        return categoriaProducto;
    }

    public void setCategoriaProducto(CategoriaProducto categoriaProducto) {
        this.categoriaProducto = categoriaProducto;
    }

    // Métodos de utilidad

    /**
     * Verifica si el producto tiene datos básicos válidos
     *
     * @return true si el producto es válido, false en caso contrario
     */
    public boolean esValido() {
        return nombre != null && !nombre.trim().isEmpty() &&
                descripcion != null && !descripcion.trim().isEmpty() &&
                precio != null && precio.compareTo(BigDecimal.ZERO) >= 0 &&
                categoria != null && !categoria.trim().isEmpty();
    }

    /**
     * Actualiza la fecha de modificación al momento actual
     */
    public void actualizarFechaModificacion() {
        this.fechaModificacion = LocalDateTime.now();
    }

    /**
     * Crea una copia del producto actual
     *
     * @return Nueva instancia con los mismos datos
     */
    public ProductoCrud copia() {
        return new Builder()
                .id(this.id)
                .nombre(this.nombre)
                .descripcion(this.descripcion)
                .precio(this.precio)
                .categoria(this.categoria)
                .stock(this.stock)
                .activo(this.activo)
                .fechaCreacion(this.fechaCreacion)
                .fechaModificacion(this.fechaModificacion)
                .categoriaProducto(this.categoriaProducto)
                .build();
    }

    // Métodos de Object

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductoCrud that = (ProductoCrud) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(nombre, that.nombre) &&
                Objects.equals(descripcion, that.descripcion) &&
                Objects.equals(precio, that.precio) &&
                Objects.equals(categoria, that.categoria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, descripcion, precio, categoria);
    }

    @Override
    public String toString() {
        return String.format("ProductoCrud{id=%d, nombre='%s', descripcion='%s', precio=%s, categoria='%s', stock=%d, activo=%s}",
                id, nombre, descripcion, precio, categoria, stock, activo);
    }

    // Builder Pattern

    /**
     * Crea un nuevo Builder para construir ProductoCrud
     *
     * @return Nueva instancia del Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Clase Builder para construcción flexible de ProductoCrud
     */
    public static class Builder {
        private Long id;
        private String nombre;
        private String descripcion;
        private BigDecimal precio;
        private String categoria;
        private Integer stock = 0;
        private Boolean activo = true;
        private LocalDateTime fechaCreacion = LocalDateTime.now();
        private LocalDateTime fechaModificacion;
        private CategoriaProducto categoriaProducto;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public Builder precio(BigDecimal precio) {
            this.precio = precio;
            return this;
        }

        public Builder precio(double precio) {
            this.precio = precio > 0 ? BigDecimal.valueOf(precio) : null;
            return this;
        }

        public Builder categoria(String categoria) {
            this.categoria = categoria;
            if (categoria != null) {
                this.categoriaProducto = CategoriaProducto.porDescripcion(categoria);
            }
            return this;
        }

        public Builder stock(Integer stock) {
            this.stock = stock;
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

        public Builder fechaModificacion(LocalDateTime fechaModificacion) {
            this.fechaModificacion = fechaModificacion;
            return this;
        }

        public Builder categoriaProducto(CategoriaProducto categoriaProducto) {
            this.categoriaProducto = categoriaProducto;
            return this;
        }

        /**
         * Construye el objeto ProductoCrud final
         *
         * @return Nueva instancia de ProductoCrud
         */
        public ProductoCrud build() {
            // Validaciones antes de construir
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del producto es obligatorio");
            }

            if (precio != null && precio.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El precio no puede ser negativo");
            }

            if (stock != null && stock < 0) {
                throw new IllegalArgumentException("El stock no puede ser negativo");
            }

            return new ProductoCrud(this);
        }
    }

    // Enum para categorías de producto
    public enum CategoriaProducto {
        @JsonProperty("ELECTRODOMESTICOS")
        ELECTRODOMESTICOS("Electrodomésticos"),

        @JsonProperty("ELECTRONICOS")
        ELECTRONICOS("Electrónicos"),

        @JsonProperty("ROPA")
        ROPA("Ropa y Accesorios"),

        @JsonProperty("HOGAR")
        HOGAR("Hogar y Jardín"),

        @JsonProperty("DEPORTES")
        DEPORTES("Deportes y Recreación"),

        @JsonProperty("LIBROS")
        LIBROS("Libros y Medios"),

        @JsonProperty("SALUD")
        SALUD("Salud y Belleza"),

        @JsonProperty("AUTOMOVILES")
        AUTOMOVILES("Automóviles y Repuestos"),

        @JsonProperty("JUGUETES")
        JUGUETES("Juguetes y Juegos"),

        @JsonProperty("ALIMENTOS")
        ALIMENTOS("Alimentos y Bebidas"),

        @JsonProperty("MASCOTAS")
        MASCOTAS("Mascotas y Animales"),

        @JsonProperty("OFICINA")
        OFICINA("Oficina y Papelería"),

        @JsonProperty("HERRAMIENTAS")
        HERRAMIENTAS("Herramientas y Mejoras del Hogar"),

        @JsonProperty("MUSICA")
        MUSICA("Música e Instrumentos"),

        @JsonProperty("ARTE")
        ARTE("Arte y Manualidades"),

        @JsonProperty("GENERAL")
        GENERAL("General");

        private final String descripcion;

        /**
         * Constructor del enum
         *
         * @param descripcion Descripción legible de la categoría
         */
        CategoriaProducto(String descripcion) {
            this.descripcion = descripcion;
        }

        /**
         * Obtiene la descripción de la categoría
         *
         * @return Descripción de la categoría
         */
        public String getDescripcion() {
            return descripcion;
        }

        /**
         * Representación en string de la categoría
         *
         * @return Descripción de la categoría
         */
        @Override
        public String toString() {
            return descripcion;
        }

        /**
         * Obtiene la categoría por descripción o nombre
         *
         * @param descripcion Descripción o nombre de la categoría
         * @return CategoriaProducto correspondiente o GENERAL si no se encuentra
         */
        public static CategoriaProducto porDescripcion(String descripcion) {
            if (descripcion == null || descripcion.trim().isEmpty()) {
                return GENERAL;
            }

            String descripcionLimpia = descripcion.trim();

            // Buscar por descripción exacta (ignorando mayúsculas/minúsculas)
            for (CategoriaProducto categoria : values()) {
                if (categoria.getDescripcion().equalsIgnoreCase(descripcionLimpia)) {
                    return categoria;
                }
            }

            // Buscar por nombre del enum
            for (CategoriaProducto categoria : values()) {
                if (categoria.name().equalsIgnoreCase(descripcionLimpia)) {
                    return categoria;
                }
            }

            // Buscar por palabras clave
            String descripcionMinusculas = descripcionLimpia.toLowerCase();

            if (descripcionMinusculas.contains("electro") && descripcionMinusculas.contains("domé")) {
                return ELECTRODOMESTICOS;
            } else if (descripcionMinusculas.contains("electró") || descripcionMinusculas.contains("electronic")) {
                return ELECTRONICOS;
            } else if (descripcionMinusculas.contains("ropa") || descripcionMinusculas.contains("acces")) {
                return ROPA;
            } else if (descripcionMinusculas.contains("hogar") || descripcionMinusculas.contains("jardín")) {
                return HOGAR;
            } else if (descripcionMinusculas.contains("deporte") || descripcionMinusculas.contains("recreac")) {
                return DEPORTES;
            } else if (descripcionMinusculas.contains("libro") || descripcionMinusculas.contains("medi")) {
                return LIBROS;
            } else if (descripcionMinusculas.contains("salud") || descripcionMinusculas.contains("belleza")) {
                return SALUD;
            } else if (descripcionMinusculas.contains("auto") || descripcionMinusculas.contains("repuesto")) {
                return AUTOMOVILES;
            } else if (descripcionMinusculas.contains("juguete") || descripcionMinusculas.contains("juego")) {
                return JUGUETES;
            } else if (descripcionMinusculas.contains("alimento") || descripcionMinusculas.contains("bebida")) {
                return ALIMENTOS;
            } else if (descripcionMinusculas.contains("mascota") || descripcionMinusculas.contains("animal")) {
                return MASCOTAS;
            } else if (descripcionMinusculas.contains("oficina") || descripcionMinusculas.contains("papel")) {
                return OFICINA;
            } else if (descripcionMinusculas.contains("herramienta") || descripcionMinusculas.contains("mejora")) {
                return HERRAMIENTAS;
            } else if (descripcionMinusculas.contains("música") || descripcionMinusculas.contains("instrumento")) {
                return MUSICA;
            } else if (descripcionMinusculas.contains("arte") || descripcionMinusculas.contains("manual")) {
                return ARTE;
            }

            return GENERAL;
        }

        /**
         * Obtiene todas las categorías disponibles como array de strings
         *
         * @return Array con las descripciones de todas las categorías
         */
        public static String[] obtenerTodasLasDescripciones() {
            CategoriaProducto[] categorias = values();
            String[] descripciones = new String[categorias.length];

            for (int i = 0; i < categorias.length; i++) {
                descripciones[i] = categorias[i].getDescripcion();
            }

            return descripciones;
        }

        /**
         * Verifica si una descripción corresponde a una categoría válida
         *
         * @param descripcion Descripción a verificar
         * @return true si es una categoría válida
         */
        public static boolean esCategoriaValida(String descripcion) {
            return porDescripcion(descripcion) != GENERAL ||
                    (descripcion != null && descripcion.equalsIgnoreCase("general"));
        }
    }
}