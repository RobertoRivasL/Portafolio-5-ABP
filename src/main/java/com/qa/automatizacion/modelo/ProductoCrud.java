package com.qa.automatizacion.modelo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de datos para representar un producto en el sistema CRUD.
 * Encapsula toda la información relacionada con un producto y sus operaciones.
 *
 * Principios aplicados:
 * - Encapsulación: Datos privados con acceso controlado vía getters/setters
 * - Inmutabilidad parcial: ID generado automáticamente y no modificable
 * - Validación: Métodos de validación integrados
 * - Serialización: Compatible con JSON para APIs y almacenamiento
 * - Clean Code: Nombres descriptivos y métodos con responsabilidad única
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoCrud {

    // ==================== CAMPOS PRINCIPALES ====================

    @JsonProperty("id")
    private final String id;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("categoria")
    private String categoria;

    @JsonProperty("precio")
    private BigDecimal precio;

    @JsonProperty("stock")
    private Integer stock;

    @JsonProperty("descripcion")
    private String descripcion;

    // ==================== CAMPOS ADICIONALES ====================

    @JsonProperty("marca")
    private String marca;

    @JsonProperty("peso")
    private String peso;

    @JsonProperty("garantia")
    private String garantia;

    @JsonProperty("activo")
    private boolean activo;

    @JsonProperty("destacado")
    private boolean destacado;

    // ==================== CAMPOS DE AUDITORÍA ====================

    @JsonProperty("fechaCreacion")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    @JsonProperty("fechaModificacion")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaModificacion;

    @JsonProperty("usuarioCreacion")
    private String usuarioCreacion;

    @JsonProperty("usuarioModificacion")
    private String usuarioModificacion;

    // ==================== CAMPOS DE CONTROL ====================

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("stockMinimo")
    private Integer stockMinimo;

    @JsonProperty("stockMaximo")
    private Integer stockMaximo;

    // ==================== CONSTRUCTORES ====================

    /**
     * Constructor por defecto requerido para deserialización JSON.
     */
    public ProductoCrud() {
        this.id = UUID.randomUUID().toString();
        this.fechaCreacion = LocalDateTime.now();
        this.activo = true;
        this.version = 1;
        this.stockMinimo = 0;
        this.stockMaximo = 1000;
    }

    /**
     * Constructor con campos obligatorios.
     *
     * @param nombre nombre del producto
     * @param codigo código único del producto
     * @param categoria categoría del producto
     * @param precio precio del producto
     * @param stock cantidad en stock
     */
    public ProductoCrud(String nombre, String codigo, String categoria, BigDecimal precio, Integer stock) {
        this();
        validarCamposObligatorios(nombre, codigo, categoria, precio, stock);

        this.nombre = nombre.trim();
        this.codigo = codigo.trim().toUpperCase();
        this.categoria = categoria.trim();
        this.precio = precio;
        this.stock = stock;
    }

    /**
     * Constructor completo para pruebas y casos específicos.
     */
    public ProductoCrud(String nombre, String codigo, String categoria, BigDecimal precio,
                        Integer stock, String descripcion, String marca) {
        this(nombre, codigo, categoria, precio, stock);
        this.descripcion = descripcion != null ? descripcion.trim() : "";
        this.marca = marca != null ? marca.trim() : "";
    }

    // ==================== GETTERS Y SETTERS ====================

    /**
     * Obtiene el ID único del producto (inmutable).
     *
     * @return ID del producto
     */
    public String getId() {
        return id;
    }

    /**
     * Obtiene el nombre del producto.
     *
     * @return nombre del producto
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del producto con validación.
     *
     * @param nombre nuevo nombre del producto
     * @throws IllegalArgumentException si el nombre es inválido
     */
    public void setNombre(String nombre) {
        validarNombre(nombre);
        this.nombre = nombre.trim();
        actualizarFechaModificacion();
    }

    /**
     * Obtiene el código del producto.
     *
     * @return código del producto
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Establece el código del producto con validación.
     *
     * @param codigo nuevo código del producto
     * @throws IllegalArgumentException si el código es inválido
     */
    public void setCodigo(String codigo) {
        validarCodigo(codigo);
        this.codigo = codigo.trim().toUpperCase();
        actualizarFechaModificacion();
    }

    /**
     * Obtiene la categoría del producto.
     *
     * @return categoría del producto
     */
    public String getCategoria() {
        return categoria;
    }

    /**
     * Establece la categoría del producto con validación.
     *
     * @param categoria nueva categoría del producto
     * @throws IllegalArgumentException si la categoría es inválida
     */
    public void setCategoria(String categoria) {
        validarCategoria(categoria);
        this.categoria = categoria.trim();
        actualizarFechaModificacion();
    }

    /**
     * Obtiene el precio del producto.
     *
     * @return precio del producto
     */
    public BigDecimal getPrecio() {
        return precio;
    }

    /**
     * Establece el precio del producto con validación.
     *
     * @param precio nuevo precio del producto
     * @throws IllegalArgumentException si el precio es inválido
     */
    public void setPrecio(BigDecimal precio) {
        validarPrecio(precio);
        this.precio = precio;
        actualizarFechaModificacion();
    }

    /**
     * Establece el precio del producto desde string.
     *
     * @param precio precio como string
     * @throws IllegalArgumentException si el precio es inválido
     */
    public void setPrecio(String precio) {
        try {
            setPrecio(new BigDecimal(precio));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato de precio inválido: " + precio);
        }
    }

    /**
     * Obtiene el stock del producto.
     *
     * @return stock del producto
     */
    public Integer getStock() {
        return stock;
    }

    /**
     * Establece el stock del producto con validación.
     *
     * @param stock nuevo stock del producto
     * @throws IllegalArgumentException si el stock es inválido
     */
    public void setStock(Integer stock) {
        validarStock(stock);
        this.stock = stock;
        actualizarFechaModificacion();
    }

    /**
     * Obtiene la descripción del producto.
     *
     * @return descripción del producto
     */
    public String getDescripcion() {
        return descripcion != null ? descripcion : "";
    }

    /**
     * Establece la descripción del producto.
     *
     * @param descripcion nueva descripción del producto
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion != null ? descripcion.trim() : "";
        actualizarFechaModificacion();
    }

    /**
     * Obtiene la marca del producto.
     *
     * @return marca del producto
     */
    public String getMarca() {
        return marca != null ? marca : "";
    }

    /**
     * Establece la marca del producto.
     *
     * @param marca nueva marca del producto
     */
    public void setMarca(String marca) {
        this.marca = marca != null ? marca.trim() : "";
        actualizarFechaModificacion();
    }

    /**
     * Obtiene el peso del producto.
     *
     * @return peso del producto
     */
    public String getPeso() {
        return peso != null ? peso : "";
    }

    /**
     * Establece el peso del producto.
     *
     * @param peso nuevo peso del producto
     */
    public void setPeso(String peso) {
        this.peso = peso != null ? peso.trim() : "";
        actualizarFechaModificacion();
    }

    /**
     * Obtiene la garantía del producto.
     *
     * @return garantía del producto
     */
    public String getGarantia() {
        return garantia != null ? garantia : "";
    }

    /**
     * Establece la garantía del producto.
     *
     * @param garantia nueva garantía del producto
     */
    public void setGarantia(String garantia) {
        this.garantia = garantia != null ? garantia.trim() : "";
        actualizarFechaModificacion();
    }

    /**
     * Verifica si el producto está activo.
     *
     * @return true si está activo, false en caso contrario
     */
    public boolean isActivo() {
        return activo;
    }

    /**
     * Establece el estado activo del producto.
     *
     * @param activo nuevo estado activo
     */
    public void setActivo(boolean activo) {
        this.activo = activo;
        actualizarFechaModificacion();
    }

    /**
     * Verifica si el producto está destacado.
     *
     * @return true si está destacado, false en caso contrario
     */
    public boolean isDestacado() {
        return destacado;
    }

    /**
     * Establece si el producto está destacado.
     *
     * @param destacado nuevo estado destacado
     */
    public void setDestacado(boolean destacado) {
        this.destacado = destacado;
        actualizarFechaModificacion();
    }

    /**
     * Obtiene la fecha de creación.
     *
     * @return fecha de creación
     */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Obtiene la fecha de modificación.
     *
     * @return fecha de modificación
     */
    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    /**
     * Obtiene el usuario creador.
     *
     * @return usuario creador
     */
    public String getUsuarioCreacion() {
        return usuarioCreacion != null ? usuarioCreacion : "";
    }

    /**
     * Establece el usuario creador.
     *
     * @param usuarioCreacion usuario creador
     */
    public void setUsuarioCreacion(String usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    /**
     * Obtiene el usuario que modificó por última vez.
     *
     * @return usuario modificador
     */
    public String getUsuarioModificacion() {
        return usuarioModificacion != null ? usuarioModificacion : "";
    }

    /**
     * Establece el usuario modificador.
     *
     * @param usuarioModificacion usuario modificador
     */
    public void setUsuarioModificacion(String usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
        actualizarFechaModificacion();
    }

    /**
     * Obtiene la versión del producto.
     *
     * @return versión del producto
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Obtiene el stock mínimo.
     *
     * @return stock mínimo
     */
    public Integer getStockMinimo() {
        return stockMinimo;
    }

    /**
     * Establece el stock mínimo.
     *
     * @param stockMinimo nuevo stock mínimo
     */
    public void setStockMinimo(Integer stockMinimo) {
        if (stockMinimo != null && stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }
        this.stockMinimo = stockMinimo != null ? stockMinimo : 0;
        actualizarFechaModificacion();
    }

    /**
     * Obtiene el stock máximo.
     *
     * @return stock máximo
     */
    public Integer getStockMaximo() {
        return stockMaximo;
    }

    /**
     * Establece el stock máximo.
     *
     * @param stockMaximo nuevo stock máximo
     */
    public void setStockMaximo(Integer stockMaximo) {
        if (stockMaximo != null && stockMaximo < 0) {
            throw new IllegalArgumentException("El stock máximo no puede ser negativo");
        }
        this.stockMaximo = stockMaximo != null ? stockMaximo : 1000;
        actualizarFechaModificacion();
    }

    // ==================== MÉTODOS DE VALIDACIÓN ====================

    /**
     * Valida los campos obligatorios del producto.
     */
    private void validarCamposObligatorios(String nombre, String codigo, String categoria,
                                           BigDecimal precio, Integer stock) {
        validarNombre(nombre);
        validarCodigo(codigo);
        validarCategoria(categoria);
        validarPrecio(precio);
        validarStock(stock);
    }

    /**
     * Valida el nombre del producto.
     */
    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (nombre.trim().length() < 3) {
            throw new IllegalArgumentException("El nombre debe tener al menos 3 caracteres");
        }
        if (nombre.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede exceder 100 caracteres");
        }
    }

    /**
     * Valida el código del producto.
     */
    private void validarCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del producto es obligatorio");
        }
        if (!codigo.trim().matches("^[A-Z0-9]+$")) {
            throw new IllegalArgumentException("El código solo puede contener letras y números");
        }
        if (codigo.trim().length() < 3 || codigo.trim().length() > 20) {
            throw new IllegalArgumentException("El código debe tener entre 3 y 20 caracteres");
        }
    }

    /**
     * Valida la categoría del producto.
     */
    private void validarCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar una categoría");
        }
    }

    /**
     * Valida el precio del producto.
     */
    private void validarPrecio(BigDecimal precio) {
        if (precio == null) {
            throw new IllegalArgumentException("El precio es obligatorio");
        }
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        if (precio.scale() > 2) {
            throw new IllegalArgumentException("El precio debe tener máximo 2 decimales");
        }
    }

    /**
     * Valida el stock del producto.
     */
    private void validarStock(Integer stock) {
        if (stock == null) {
            throw new IllegalArgumentException("El stock es obligatorio");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Actualiza la fecha de modificación al momento actual.
     */
    private void actualizarFechaModificacion() {
        this.fechaModificacion = LocalDateTime.now();
        this.version++;
    }

    /**
     * Verifica si el producto está agotado.
     *
     * @return true si está agotado, false en caso contrario
     */
    public boolean estaAgotado() {
        return stock == null || stock == 0;
    }

    /**
     * Verifica si el producto tiene stock bajo.
     *
     * @return true si tiene stock bajo, false en caso contrario
     */
    public boolean tieneStockBajo() {
        return stock != null && stockMinimo != null && stock <= stockMinimo;
    }

    /**
     * Incrementa el stock en la cantidad especificada.
     *
     * @param cantidad cantidad a incrementar
     * @throws IllegalArgumentException si la cantidad es inválida
     */
    public void incrementarStock(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad a incrementar debe ser positiva");
        }
        setStock(this.stock + cantidad);
    }

    /**
     * Decrementa el stock en la cantidad especificada.
     *
     * @param cantidad cantidad a decrementar
     * @throws IllegalArgumentException si la cantidad es inválida o insuficiente
     */
    public void decrementarStock(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad a decrementar debe ser positiva");
        }
        if (this.stock < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + this.stock + ", Solicitado: " + cantidad);
        }
        setStock(this.stock - cantidad);
    }

    /**
     * Aplica un descuento al precio.
     *
     * @param porcentajeDescuento porcentaje de descuento (0-100)
     * @throws IllegalArgumentException si el porcentaje es inválido
     */
    public void aplicarDescuento(double porcentajeDescuento) {
        if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");
        }

        BigDecimal factorDescuento = BigDecimal.valueOf((100 - porcentajeDescuento) / 100);
        BigDecimal nuevoPrecio = precio.multiply(factorDescuento).setScale(2, BigDecimal.ROUND_HALF_UP);

        setPrecio(nuevoPrecio);
    }

    /**
     * Crea una copia del producto con nuevo ID.
     *
     * @return nueva instancia del producto
     */
    public ProductoCrud clonar() {
        ProductoCrud clon = new ProductoCrud();
        clon.nombre = this.nombre;
        clon.codigo = this.codigo + "_COPY";
        clon.categoria = this.categoria;
        clon.precio = this.precio;
        clon.stock = this.stock;
        clon.descripcion = this.descripcion;
        clon.marca = this.marca;
        clon.peso = this.peso;
        clon.garantia = this.garantia;
        clon.activo = this.activo;
        clon.destacado = false; // Los clones no son destacados por defecto
        clon.stockMinimo = this.stockMinimo;
        clon.stockMaximo = this.stockMaximo;

        return clon;
    }

    // ==================== MÉTODOS ESTÁNDAR ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ProductoCrud producto = (ProductoCrud) obj;
        return Objects.equals(id, producto.id) ||
                Objects.equals(codigo, producto.codigo); // Productos son iguales si tienen el mismo código
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo); // Hash basado en código único
    }

    @Override
    public String toString() {
        return String.format("ProductoCrud{id='%s', nombre='%s', codigo='%s', categoria='%s', precio=%s, stock=%d, activo=%s}",
                id, nombre, codigo, categoria, precio, stock, activo);
    }

    /**
     * Representación detallada del producto para debugging.
     *
     * @return representación detallada
     */
    public String toStringDetallado() {
        return String.format(
                "ProductoCrud{\n" +
                        "  id='%s',\n" +
                        "  nombre='%s',\n" +
                        "  codigo='%s',\n" +
                        "  categoria='%s',\n" +
                        "  precio=%s,\n" +
                        "  stock=%d,\n" +
                        "  descripcion='%s',\n" +
                        "  marca='%s',\n" +
                        "  activo=%s,\n" +
                        "  destacado=%s,\n" +
                        "  fechaCreacion=%s,\n" +
                        "  fechaModificacion=%s,\n" +
                        "  version=%d\n" +
                        "}",
                id, nombre, codigo, categoria, precio, stock,
                descripcion, marca, activo, destacado,
                fechaCreacion, fechaModificacion, version
        );
    }

    /**
     * Valida si el producto está en un estado válido para persistencia.
     *
     * @return true si es válido, false en caso contrario
     */
    public boolean esValido() {
        try {
            validarCamposObligatorios(nombre, codigo, categoria, precio, stock);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Obtiene un resumen del producto para mostrar en listas.
     *
     * @return resumen del producto
     */
    public String obtenerResumen() {
        String estadoStock = estaAgotado() ? " (AGOTADO)" :
                tieneStockBajo() ? " (STOCK BAJO)" : "";

        return String.format("%s - %s - $%s - Stock: %d%s",
                codigo, nombre, precio, stock, estadoStock);
    }
}