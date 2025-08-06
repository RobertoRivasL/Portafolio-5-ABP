package com.qa.automatizacion.pruebas.modelo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modelo de datos para representar un Producto en operaciones CRUD
 * Implementa el patrón Builder y encapsulación de datos
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    @JsonProperty("fechaModificacion")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaModificacion;

    @JsonProperty("categoria_tipo")
    private CategoriaProducto categoriaProducto;

    // Constructor por defecto
    public ProductoCrud() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
        this.stock = 0;
        this.categoriaProducto = CategoriaProducto.GENERAL;
    }

    // Constructor completo usando Builder
    private ProductoCrud(Builder builder) {
        this.id = builder.id;
        this.nombre = builder.nombre;
        this.descripcion = builder.descripcion;
        this.precio = builder.precio;
        this.categoria = builder.categoria;
        this.stock = builder.stock != null ? builder.stock : 0;
        this.activo = builder.activo != null ? builder.activo : true;
        this.fechaCreacion = builder.fechaCreacion != null ? builder.fechaCreacion : LocalDateTime.now();
        this.fechaModificacion = builder.fechaModificacion;
        this.categoriaProducto = builder.categoriaProducto != null ? builder.categoriaProducto : CategoriaProducto.GENERAL;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public Integer getStock() {
        return stock;
    }

    public Boolean getActivo() {
        return activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public CategoriaProducto getCategoriaProducto() {
        return categoriaProducto;
    }

    // Setters para casos específicos (preferir usar Builder)
    public void setId(Long id) {
        this.id = id;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    /**
     * Verifica si el producto está activo
     *
     * @return true si el producto está activo
     */
    public boolean estaActivo() {
        return activo != null && activo;
    }

    /**
     * Verifica si el producto tiene stock disponible
     *
     * @return true si hay stock disponible
     */
    public boolean tieneStock() {
        return stock != null && stock > 0;
    }

    /**
     * Obtiene el precio formateado como string
     *
     * @return Precio formateado con símbolo de moneda
     */
    public String getPrecioFormateado() {
        if (precio == null) {
            return "$0.00";
        }
        return String.format("$%.2f", precio);
    }

    /**
     * Verifica si el producto está en una categoría específica
     *
     * @param categoria Categoría a verificar
     * @return true si el producto pertenece a la categoría
     */
    public boolean esDeCategoria(String categoria) {
        return this.categoria != null && this.categoria.equalsIgnoreCase(categoria);
    }

    /**
     * Verifica si el producto está en una categoría específica (enum)
     *
     * @param categoriaProducto Categoría a verificar
     * @return true si el producto pertenece a la categoría
     */
    public boolean esDeCategoria(CategoriaProducto categoriaProducto) {
        return this.categoriaProducto == categoriaProducto;
    }

    /**
     * Calcula el valor total del stock
     *
     * @return Valor total basado en precio y stock
     */
    public BigDecimal calcularValorTotalStock() {
        if (precio == null || stock == null) {
            return BigDecimal.ZERO;
        }
        return precio.multiply(BigDecimal.valueOf(stock));
    }

    /**
     * Valida los datos del producto
     *
     * @return true si los datos son válidos
     */
    public boolean esValido() {
        return nombre != null && !nombre.trim().isEmpty() &&
                descripcion != null && !descripcion.trim().isEmpty() &&
                precio != null && precio.compareTo(BigDecimal.ZERO) > 0 &&
                categoria != null && !categoria.trim().isEmpty();
    }

    /**
     * Crea una copia del producto
     *
     * @return Nueva instancia con los mismos datos
     */
    public ProductoCrud copiar() {
        return ProductoCrud.builder()
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

    /**
     * Actualiza la fecha de modificación
     */
    public void marcarComoModificado() {
        this.fechaModificacion = LocalDateTime.now();
    }

    // Método estático para crear Builder
    public static Builder builder() {
        return new Builder();
    }

    // Clase Builder interna
    public static class Builder {
        private Long id;
        private String nombre;
        private String descripcion;
        private BigDecimal precio;
        private String categoria;
        private Integer stock;
        private Boolean activo;
        private LocalDateTime fechaCreacion;
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

        public Builder precio(Double precio) {
            this.precio = precio != null ? BigDecimal.valueOf(precio) : null;
            return this;
        }

        public Builder categoria(String categoria) {
            this.categoria = categoria;
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

        public ProductoCrud build() {
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

        @JsonProperty("GENERAL")
        GENERAL("General");

        private final String descripcion;

        CategoriaProducto(String descripcion) {
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
         * Obtiene la categoría por descripción
         *
         * @param descripcion Descripción de la categoría
         * @return CategoriaProducto correspondiente o GENERAL si no se encuentra
         */
        public static CategoriaProducto porDescripcion(String descripcion) {
            if (descripcion == null) {
                return GENERAL;
            }

            for (CategoriaProducto categoria : values()) {
                if (categoria.descripcion.equalsIgnoreCase(descripcion)) {
                    return categoria;
                }
            }
            return GENERAL;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductoCrud that = (ProductoCrud) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(nombre, that.nombre) &&
                Objects.equals(categoria, that.categoria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, categoria);
    }

    @Override
    public String toString() {
        return "ProductoCrud{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", categoria='" + categoria + '\'' +
                ", stock=" + stock +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaModificacion=" + fechaModificacion +
                ", categoriaProducto=" + categoriaProducto +
                '}';
    }
}