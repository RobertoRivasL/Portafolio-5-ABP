package com.qa.automatizacion.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Modelo que representa una Historia de Usuario en el contexto BDD.
 * Encapsula información sobre requerimientos funcionales y su trazabilidad con escenarios de prueba.
 *
 * Principios aplicados:
 * - Encapsulación: Datos y comportamiento relacionado agrupados
 * - Immutabilidad: Uso del patrón Builder para creación segura
 * - Single Responsibility: Se enfoca únicamente en representar una HU
 * - Open/Closed: Extensible mediante enums y campos adicionales
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoriaUsuario {

    @JsonProperty("id")
    private final String id;

    @JsonProperty("titulo")
    private final String titulo;

    @JsonProperty("descripcion")
    private final String descripcion;

    @JsonProperty("como")
    private final String como;

    @JsonProperty("quiero")
    private final String quiero;

    @JsonProperty("para")
    private final String para;

    @JsonProperty("criterios_aceptacion")
    private final List<CriterioAceptacion> criteriosAceptacion;

    @JsonProperty("prioridad")
    private final Prioridad prioridad;

    @JsonProperty("estado")
    private final Estado estado;

    @JsonProperty("puntos_historia")
    private final Integer puntosHistoria;

    @JsonProperty("epic")
    private final String epic;

    @JsonProperty("sprint")
    private final String sprint;

    @JsonProperty("fecha_creacion")
    private final LocalDateTime fechaCreacion;

    @JsonProperty("fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @JsonProperty("escenarios_asociados")
    private final Set<String> escenariosAsociados;

    @JsonProperty("tags")
    private final Set<String> tags;

    @JsonProperty("notas")
    private final String notas;

    // Constructor privado para uso del Builder
    private HistoriaUsuario(Builder builder) {
        this.id = builder.id;
        this.titulo = builder.titulo;
        this.descripcion = builder.descripcion;
        this.como = builder.como;
        this.quiero = builder.quiero;
        this.para = builder.para;
        this.criteriosAceptacion = Collections.unmodifiableList(new ArrayList<>(builder.criteriosAceptacion));
        this.prioridad = builder.prioridad;
        this.estado = builder.estado;
        this.puntosHistoria = builder.puntosHistoria;
        this.epic = builder.epic;
        this.sprint = builder.sprint;
        this.fechaCreacion = builder.fechaCreacion != null ? builder.fechaCreacion : LocalDateTime.now();
        this.fechaModificacion = builder.fechaModificacion;
        this.escenariosAsociados = new HashSet<>(builder.escenariosAsociados);
        this.tags = Collections.unmodifiableSet(new HashSet<>(builder.tags));
        this.notas = builder.notas;
    }

    // ==================== GETTERS ====================

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getComo() { return como; }
    public String getQuiero() { return quiero; }
    public String getPara() { return para; }
    public List<CriterioAceptacion> getCriteriosAceptacion() { return criteriosAceptacion; }
    public Prioridad getPrioridad() { return prioridad; }
    public Estado getEstado() { return estado; }
    public Integer getPuntosHistoria() { return puntosHistoria; }
    public String getEpic() { return epic; }
    public String getSprint() { return sprint; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public Set<String> getEscenariosAsociados() { return new HashSet<>(escenariosAsociados); }
    public Set<String> getTags() { return tags; }
    public String getNotas() { return notas; }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Asocia un escenario con esta historia de usuario.
     */
    public void asociarEscenario(String nombreEscenario) {
        if (nombreEscenario != null && !nombreEscenario.trim().isEmpty()) {
            escenariosAsociados.add(nombreEscenario.trim());
            actualizarFechaModificacion();
        }
    }

    /**
     * Desasocia un escenario de esta historia de usuario.
     */
    public void desasociarEscenario(String nombreEscenario) {
        if (escenariosAsociados.remove(nombreEscenario)) {
            actualizarFechaModificacion();
        }
    }

    /**
     * Verifica si un escenario está asociado con esta historia.
     */
    public boolean tieneEscenarioAsociado(String nombreEscenario) {
        return escenariosAsociados.contains(nombreEscenario);
    }

    /**
     * Obtiene la cantidad de escenarios asociados.
     */
    public int getCantidadEscenariosAsociados() {
        return escenariosAsociados.size();
    }

    /**
     * Verifica si la historia tiene criterios de aceptación definidos.
     */
    public boolean tieneCriteriosAceptacion() {
        return criteriosAceptacion != null && !criteriosAceptacion.isEmpty();
    }

    /**
     * Obtiene la cantidad de criterios de aceptación.
     */
    public int getCantidadCriteriosAceptacion() {
        return criteriosAceptacion != null ? criteriosAceptacion.size() : 0;
    }

    /**
     * Calcula el porcentaje de cobertura de criterios basado en escenarios.
     */
    public double calcularPorcentajeCobertura() {
        if (!tieneCriteriosAceptacion()) {
            return escenariosAsociados.isEmpty() ? 0.0 : 100.0;
        }

        long criteriosCubiertos = criteriosAceptacion.stream()
                .mapToLong(criterio -> criterio.getEscenariosCubriendo().size())
                .sum();

        if (criteriosCubiertos == 0) {
            return 0.0;
        }

        return Math.min(100.0, (criteriosCubiertos * 100.0) / criteriosAceptacion.size());
    }

    /**
     * Verifica si la historia está completamente implementada.
     */
    public boolean estaCompletamenteImplementada() {
        return estado == Estado.TERMINADO &&
                !escenariosAsociados.isEmpty() &&
                calcularPorcentajeCobertura() >= 100.0;
    }

    /**
     * Genera el formato estándar de Historia de Usuario.
     */
    public String generarFormatoEstandar() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("# %s: %s\n\n", id, titulo));
        sb.append(String.format("**Como** %s\n", como));
        sb.append(String.format("**Quiero** %s\n", quiero));
        sb.append(String.format("**Para** %s\n\n", para));

        if (tieneCriteriosAceptacion()) {
            sb.append("## Criterios de Aceptación:\n");
            criteriosAceptacion.forEach(criterio ->
                    sb.append(String.format("- %s\n", criterio.getDescripcion())));
        }

        return sb.toString();
    }

    /**
     * Actualiza la fecha de modificación.
     */
    private void actualizarFechaModificacion() {
        this.fechaModificacion = LocalDateTime.now();
    }

    // ==================== ENUMS ====================

    public enum Prioridad {
        @JsonProperty("CRITICA")
        CRITICA("Crítica", 1),

        @JsonProperty("ALTA")
        ALTA("Alta", 2),

        @JsonProperty("MEDIA")
        MEDIA("Media", 3),

        @JsonProperty("BAJA")
        BAJA("Baja", 4),

        @JsonProperty("OPCIONAL")
        OPCIONAL("Opcional", 5);

        private final String descripcion;
        private final int orden;

        Prioridad(String descripcion, int orden) {
            this.descripcion = descripcion;
            this.orden = orden;
        }

        public String getDescripcion() { return descripcion; }
        public int getOrden() { return orden; }

        @Override
        public String toString() { return descripcion; }
    }

    public enum Estado {
        @JsonProperty("BACKLOG")
        BACKLOG("Backlog"),

        @JsonProperty("EN_REFINAMIENTO")
        EN_REFINAMIENTO("En Refinamiento"),

        @JsonProperty("LISTO_DESARROLLO")
        LISTO_DESARROLLO("Listo para Desarrollo"),

        @JsonProperty("EN_DESARROLLO")
        EN_DESARROLLO("En Desarrollo"),

        @JsonProperty("EN_PRUEBAS")
        EN_PRUEBAS("En Pruebas"),

        @JsonProperty("EN_REVISION")
        EN_REVISION("En Revisión"),

        @JsonProperty("TERMINADO")
        TERMINADO("Terminado"),

        @JsonProperty("CANCELADO")
        CANCELADO("Cancelado");

        private final String descripcion;

        Estado(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() { return descripcion; }

        @Override
        public String toString() { return descripcion; }
    }

    // ==================== CLASE INTERNA: CRITERIO DE ACEPTACIÓN ====================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CriterioAceptacion {
        @JsonProperty("id")
        private final String id;

        @JsonProperty("descripcion")
        private final String descripcion;

        @JsonProperty("escenarios_cubriendo")
        private final Set<String> escenariosCubriendo;

        @JsonProperty("prioridad")
        private final PrioridadCriterio prioridad;

        @JsonProperty("tipo")
        private final TipoCriterio tipo;

        public CriterioAceptacion(
                @JsonProperty("id") String id,
                @JsonProperty("descripcion") String descripcion,
                @JsonProperty("escenarios_cubriendo") Set<String> escenariosCubriendo,
                @JsonProperty("prioridad") PrioridadCriterio prioridad,
                @JsonProperty("tipo") TipoCriterio tipo) {
            this.id = id;
            this.descripcion = descripcion;
            this.escenariosCubriendo = escenariosCubriendo != null ? new HashSet<>(escenariosCubriendo) : new HashSet<>();
            this.prioridad = prioridad != null ? prioridad : PrioridadCriterio.OBLIGATORIO;
            this.tipo = tipo != null ? tipo : TipoCriterio.FUNCIONAL;
        }

        // Getters
        public String getId() { return id; }
        public String getDescripcion() { return descripcion; }
        public Set<String> getEscenariosCubriendo() { return new HashSet<>(escenariosCubriendo); }
        public PrioridadCriterio getPrioridad() { return prioridad; }
        public TipoCriterio getTipo() { return tipo; }

        // Métodos de utilidad
        public void agregarEscenarioCubriendo(String escenario) {
            if (escenario != null && !escenario.trim().isEmpty()) {
                escenariosCubriendo.add(escenario.trim());
            }
        }

        public boolean estaCubiertoPorEscenario(String escenario) {
            return escenariosCubriendo.contains(escenario);
        }

        public boolean estaCubierto() {
            return !escenariosCubriendo.isEmpty();
        }

        public enum PrioridadCriterio {
            @JsonProperty("OBLIGATORIO")
            OBLIGATORIO("Obligatorio"),

            @JsonProperty("OPCIONAL")
            OPCIONAL("Opcional"),

            @JsonProperty("DESEABLE")
            DESEABLE("Deseable");

            private final String descripcion;

            PrioridadCriterio(String descripcion) {
                this.descripcion = descripcion;
            }

            public String getDescripcion() { return descripcion; }
        }

        public enum TipoCriterio {
            @JsonProperty("FUNCIONAL")
            FUNCIONAL("Funcional"),

            @JsonProperty("NO_FUNCIONAL")
            NO_FUNCIONAL("No Funcional"),

            @JsonProperty("NEGOCIO")
            NEGOCIO("Negocio"),

            @JsonProperty("TECNICO")
            TECNICO("Técnico"),

            @JsonProperty("USABILIDAD")
            USABILIDAD("Usabilidad"),

            @JsonProperty("PERFORMANCE")
            PERFORMANCE("Performance"),

            @JsonProperty("SEGURIDAD")
            SEGURIDAD("Seguridad");

            private final String descripcion;

            TipoCriterio(String descripcion) {
                this.descripcion = descripcion;
            }

            public String getDescripcion() { return descripcion; }
        }
    }

    // ==================== BUILDER PATTERN ====================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String titulo;
        private String descripcion;
        private String como;
        private String quiero;
        private String para;
        private List<CriterioAceptacion> criteriosAceptacion = new ArrayList<>();
        private Prioridad prioridad = Prioridad.MEDIA;
        private Estado estado = Estado.BACKLOG;
        private Integer puntosHistoria;
        private String epic;
        private String sprint;
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaModificacion;
        private Set<String> escenariosAsociados = new HashSet<>();
        private Set<String> tags = new HashSet<>();
        private String notas;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder titulo(String titulo) {
            this.titulo = titulo;
            return this;
        }

        public Builder descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public Builder como(String como) {
            this.como = como;
            return this;
        }

        public Builder quiero(String quiero) {
            this.quiero = quiero;
            return this;
        }

        public Builder para(String para) {
            this.para = para;
            return this;
        }

        public Builder criteriosAceptacion(List<CriterioAceptacion> criterios) {
            this.criteriosAceptacion = criterios != null ? new ArrayList<>(criterios) : new ArrayList<>();
            return this;
        }

        public Builder agregarCriterioAceptacion(CriterioAceptacion criterio) {
            if (criterio != null) {
                this.criteriosAceptacion.add(criterio);
            }
            return this;
        }

        public Builder prioridad(Prioridad prioridad) {
            this.prioridad = prioridad;
            return this;
        }

        public Builder estado(Estado estado) {
            this.estado = estado;
            return this;
        }

        public Builder puntosHistoria(Integer puntos) {
            this.puntosHistoria = puntos;
            return this;
        }

        public Builder epic(String epic) {
            this.epic = epic;
            return this;
        }

        public Builder sprint(String sprint) {
            this.sprint = sprint;
            return this;
        }

        public Builder fechaCreacion(LocalDateTime fecha) {
            this.fechaCreacion = fecha;
            return this;
        }

        public Builder fechaModificacion(LocalDateTime fecha) {
            this.fechaModificacion = fecha;
            return this;
        }

        public Builder escenariosAsociados(Set<String> escenarios) {
            this.escenariosAsociados = escenarios != null ? new HashSet<>(escenarios) : new HashSet<>();
            return this;
        }

        public Builder agregarEscenarioAsociado(String escenario) {
            if (escenario != null && !escenario.trim().isEmpty()) {
                this.escenariosAsociados.add(escenario.trim());
            }
            return this;
        }

        public Builder tags(Set<String> tags) {
            this.tags = tags != null ? new HashSet<>(tags) : new HashSet<>();
            return this;
        }

        public Builder agregarTag(String tag) {
            if (tag != null && !tag.trim().isEmpty()) {
                this.tags.add(tag.trim());
            }
            return this;
        }

        public Builder notas(String notas) {
            this.notas = notas;
            return this;
        }

        public HistoriaUsuario build() {
            // Validaciones básicas
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("El ID de la historia de usuario es obligatorio");
            }
            if (titulo == null || titulo.trim().isEmpty()) {
                throw new IllegalArgumentException("El título de la historia de usuario es obligatorio");
            }

            return new HistoriaUsuario(this);
        }
    }

    // ==================== EQUALS, HASHCODE, TOSTRING ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoriaUsuario that = (HistoriaUsuario) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("HistoriaUsuario{id='%s', titulo='%s', estado=%s, prioridad=%s, escenarios=%d}",
                id, titulo, estado, prioridad, escenariosAsociados.size());
    }
}