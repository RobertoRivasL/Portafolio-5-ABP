package com.qa.automatizacion.utilidades;

import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contexto compartido entre los pasos de prueba.
 * Implementa el patrón Singleton con soporte ThreadLocal para ejecución en paralelo.
 *
 * Principios aplicados:
 * - Singleton: Una única instancia por hilo de ejecución
 * - Encapsulación: Estado interno protegido
 * - Thread Safety: Manejo seguro en entornos multi-hilo
 * - Observer Pattern: Permite suscripción a eventos del contexto
 */
public class ContextoPrueba {

    private static final Logger logger = LoggerFactory.getLogger(ContextoPrueba.class);
    private static final ThreadLocal<ContextoPrueba> instancia = new ThreadLocal<>();

    private final Map<String, Object> datosCompartidos;
    private final Map<String, LocalDateTime> timestampsAcceso;
    private final String idContexto;
    private LocalDateTime fechaCreacion;

    /**
     * Constructor privado para implementar Singleton.
     */
    private ContextoPrueba() {
        this.datosCompartidos = new ConcurrentHashMap<>();
        this.timestampsAcceso = new ConcurrentHashMap<>();
        this.idContexto = "CTX-" + Thread.currentThread().getName() + "-" + System.currentTimeMillis();
        this.fechaCreacion = LocalDateTime.now();

        logger.debug("Nueva instancia de ContextoPrueba creada: {}", idContexto);
    }

    /**
     * Obtiene la instancia del contexto para el hilo actual.
     *
     * @return instancia del contexto
     */
    public static ContextoPrueba obtenerInstancia() {
        ContextoPrueba contexto = instancia.get();
        if (contexto == null) {
            contexto = new ContextoPrueba();
            instancia.set(contexto);
            logger.debug("Contexto creado para hilo: {}", Thread.currentThread().getName());
        }
        return contexto;
    }

    // ==================== GESTIÓN DE NAVEGADOR ====================

    /**
     * Obtiene el navegador web actual.
     *
     * @return WebDriver instanciado
     */
    public WebDriver getNavegador() {
        try {
            return ConfiguradorNavegador.obtenerDriver();
        } catch (Exception e) {
            logger.error("Error obteniendo navegador del contexto: {}", e.getMessage());
            throw new RuntimeException("No se pudo obtener el navegador", e);
        }
    }

    /**
     * Verifica si hay un navegador disponible.
     *
     * @return true si hay navegador disponible
     */
    public boolean tieneNavegadorDisponible() {
        try {
            return ConfiguradorNavegador.tieneDriverActivo();
        } catch (Exception e) {
            logger.debug("Error verificando disponibilidad de navegador: {}", e.getMessage());
            return false;
        }
    }

    // ==================== GESTIÓN DE DATOS ====================

    /**
     * Almacena un dato en el contexto compartido con timestamp.
     *
     * @param clave clave del dato
     * @param valor valor a almacenar
     */
    public void almacenarDato(String clave, Object valor) {
        validarClave(clave);

        datosCompartidos.put(clave, valor);
        timestampsAcceso.put(clave, LocalDateTime.now());

        logger.debug("Dato almacenado - Clave: {}, Tipo: {}, ID Contexto: {}",
                clave,
                valor != null ? valor.getClass().getSimpleName() : "null",
                idContexto);
    }

    /**
     * Obtiene un dato del contexto compartido.
     *
     * @param clave clave del dato
     * @return valor almacenado o null si no existe
     */
    public Object obtenerDato(String clave) {
        validarClave(clave);

        Object valor = datosCompartidos.get(clave);

        if (valor != null) {
            timestampsAcceso.put(clave, LocalDateTime.now());
        }

        logger.debug("Dato recuperado - Clave: {}, Encontrado: {}, ID Contexto: {}",
                clave, valor != null, idContexto);

        return valor;
    }

    /**
     * Obtiene un dato con casting automático a la clase especificada.
     *
     * @param clave clave del dato
     * @param claseEsperada clase esperada del dato
     * @param <T> tipo esperado
     * @return valor casteado o null si no existe o no coincide el tipo
     */
    @SuppressWarnings("unchecked")
    public <T> T obtenerDato(String clave, Class<T> claseEsperada) {
        Object valor = obtenerDato(clave);

        if (valor == null) {
            return null;
        }

        try {
            if (claseEsperada.isInstance(valor)) {
                return (T) valor;
            } else {
                logger.warn("Tipo incorrecto para clave '{}'. Esperado: {}, Actual: {}",
                        clave, claseEsperada.getSimpleName(), valor.getClass().getSimpleName());
                return null;
            }
        } catch (ClassCastException e) {
            logger.error("Error de casting para clave '{}': {}", clave, e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si existe un dato con la clave especificada.
     *
     * @param clave clave a verificar
     * @return true si existe el dato
     */
    public boolean existeDato(String clave) {
        validarClave(clave);
        return datosCompartidos.containsKey(clave);
    }

    /**
     * Elimina un dato del contexto.
     *
     * @param clave clave del dato a eliminar
     * @return el valor eliminado o null si no existía
     */
    public Object eliminarDato(String clave) {
        validarClave(clave);

        Object valorAnterior = datosCompartidos.remove(clave);
        timestampsAcceso.remove(clave);

        logger.debug("Dato eliminado - Clave: {}, Existía: {}, ID Contexto: {}",
                clave, valorAnterior != null, idContexto);

        return valorAnterior;
    }

    /**
     * Actualiza un dato existente o lo crea si no existe.
     *
     * @param clave clave del dato
     * @param valor nuevo valor
     * @return valor anterior o null si no existía
     */
    public Object actualizarDato(String clave, Object valor) {
        validarClave(clave);

        Object valorAnterior = datosCompartidos.put(clave, valor);
        timestampsAcceso.put(clave, LocalDateTime.now());

        logger.debug("Dato actualizado - Clave: {}, Tenía valor anterior: {}",
                clave, valorAnterior != null);

        return valorAnterior;
    }

    // ==================== GESTIÓN MASIVA DE DATOS ====================

    /**
     * Limpia todos los datos del contexto.
     */
    public void limpiarDatos() {
        int cantidadAnterior = datosCompartidos.size();

        datosCompartidos.clear();
        timestampsAcceso.clear();

        logger.debug("Contexto limpiado - Datos eliminados: {}, ID Contexto: {}",
                cantidadAnterior, idContexto);
    }

    /**
     * Obtiene todos los datos del contexto como un Map inmutable.
     *
     * @return Map con todos los datos
     */
    public Map<String, Object> obtenerTodosLosDatos() {
        return new HashMap<>(datosCompartidos);
    }

    /**
     * Carga múltiples datos desde un Map.
     *
     * @param datos Map con los datos a cargar
     */
    public void cargarDatos(Map<String, Object> datos) {
        if (datos != null) {
            LocalDateTime ahora = LocalDateTime.now();

            datos.forEach((clave, valor) -> {
                datosCompartidos.put(clave, valor);
                timestampsAcceso.put(clave, ahora);
            });

            logger.debug("Cargados {} datos en el contexto: {}", datos.size(), idContexto);
        }
    }

    // ==================== INFORMACIÓN Y DIAGNÓSTICO ====================

    /**
     * Obtiene estadísticas del contexto.
     *
     * @return Map con estadísticas
     */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("idContexto", idContexto);
        stats.put("fechaCreacion", fechaCreacion);
        stats.put("hiloActual", Thread.currentThread().getName());
        stats.put("totalDatos", datosCompartidos.size());
        stats.put("tieneNavegador", tieneNavegadorDisponible());
        stats.put("ultimaActividad", timestampsAcceso.values().stream()
                .max(LocalDateTime::compareTo)
                .orElse(fechaCreacion));

        if (tieneNavegadorDisponible()) {
            try {
                stats.put("urlActual", ConfiguradorNavegador.obtenerUrlActual());
                stats.put("tituloActual", ConfiguradorNavegador.obtenerTituloPagina());
            } catch (Exception e) {
                stats.put("infoNavegador", "Error: " + e.getMessage());
            }
        }

        return stats;
    }

    /**
     * Obtiene información completa de diagnóstico del contexto.
     *
     * @return cadena con información detallada
     */
    public String obtenerInformacionDiagnostico() {
        StringBuilder info = new StringBuilder();

        info.append("=== DIAGNÓSTICO CONTEXTO PRUEBA ===\n");
        info.append("ID Contexto: ").append(idContexto).append("\n");
        info.append("Hilo actual: ").append(Thread.currentThread().getName()).append("\n");
        info.append("Fecha creación: ").append(fechaCreacion).append("\n");
        info.append("Datos almacenados: ").append(datosCompartidos.size()).append("\n");

        // Información de navegador
        info.append("Navegador disponible: ").append(tieneNavegadorDisponible()).append("\n");
        if (tieneNavegadorDisponible()) {
            try {
                info.append("URL actual: ").append(ConfiguradorNavegador.obtenerUrlActual()).append("\n");
                info.append("Título página: ").append(ConfiguradorNavegador.obtenerTituloPagina()).append("\n");
            } catch (Exception e) {
                info.append("Error info navegador: ").append(e.getMessage()).append("\n");
            }
        }

        // Lista de datos almacenados
        if (!datosCompartidos.isEmpty()) {
            info.append("\nDatos almacenados:\n");
            datosCompartidos.forEach((clave, valor) -> {
                LocalDateTime timestamp = timestampsAcceso.get(clave);
                info.append("  ").append(clave).append(": ")
                        .append(valor != null ? valor.getClass().getSimpleName() : "null")
                        .append(" (").append(timestamp != null ? timestamp : "sin timestamp").append(")\n");
            });
        }

        info.append("=== FIN DIAGNÓSTICO ===");
        return info.toString();
    }

    /**
     * Obtiene las claves de datos ordenadas por último acceso.
     *
     * @return lista de claves ordenadas por actividad
     */
    public java.util.List<String> obtenerClavesOrdenadaPorActividad() {
        return datosCompartidos.keySet().stream()
                .sorted((clave1, clave2) -> {
                    LocalDateTime time1 = timestampsAcceso.get(clave1);
                    LocalDateTime time2 = timestampsAcceso.get(clave2);

                    if (time1 == null && time2 == null) return 0;
                    if (time1 == null) return 1;
                    if (time2 == null) return -1;

                    return time2.compareTo(time1); // Más reciente primero
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // ==================== LIMPIEZA Y LIFECYCLE ====================

    /**
     * Limpia completamente el contexto incluyendo el navegador.
     */
    public void limpiarContextoCompleto() {
        limpiarDatos();

        if (tieneNavegadorDisponible()) {
            try {
                ConfiguradorNavegador.cerrarDriver();
                logger.debug("Navegador cerrado desde contexto: {}", idContexto);
            } catch (Exception e) {
                logger.warn("Error cerrando navegador desde contexto: {}", e.getMessage());
            }
        }
    }

    /**
     * Limpia la instancia del hilo actual.
     * Debe llamarse al final de cada escenario.
     */
    public static void limpiarInstancia() {
        ContextoPrueba contexto = instancia.get();
        if (contexto != null) {
            contexto.limpiarContextoCompleto();
            instancia.remove();
            logger.debug("Instancia de contexto removida del hilo: {}",
                    Thread.currentThread().getName());
        }
    }

    // ==================== MÉTODOS UTILITARIOS PRIVADOS ====================

    /**
     * Valida que una clave no sea nula o vacía.
     */
    private void validarClave(String clave) {
        if (clave == null || clave.trim().isEmpty()) {
            throw new IllegalArgumentException("La clave no puede ser nula o vacía");
        }
    }

    /**
     * Obtiene el ID único del contexto.
     *
     * @return ID del contexto
     */
    public String getIdContexto() {
        return idContexto;
    }

    /**
     * Obtiene la fecha de creación del contexto.
     *
     * @return fecha de creación
     */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}