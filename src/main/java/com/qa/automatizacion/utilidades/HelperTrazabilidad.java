package com.qa.automatizacion.utilidades;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper para gestionar la trazabilidad entre escenarios y historias de usuario.
 * Permite rastrear qué escenarios cubren qué requerimientos funcionales.
 *
 * Principios aplicados:
 * - Single Responsibility: Se enfoca únicamente en trazabilidad
 * - Observer Pattern: Registra eventos de ejecución de escenarios
 * - Strategy Pattern: Permite diferentes formatos de reporte
 *
 * @author Equipo QA Automatización
 * @version 1.0
 */
public class HelperTrazabilidad {

    private static final Logger logger = LoggerFactory.getLogger(HelperTrazabilidad.class);

    // ==================== VARIABLES FALTANTES ====================

    // Lista para almacenar todos los pasos ejecutados
    private final List<RegistroPaso> pasosEjecutados;

    // Map para almacenar las historias de usuario
    private final Map<String, RegistroHistoriaUsuario> registrosHistorias;

    // Variables para estadísticas
    private LocalDateTime inicioEjecucion;
    private String idEjecucionActual;

    // ==================== CONSTRUCTOR ====================

    /**
     * Constructor que inicializa las estructuras de datos.
     */
    public HelperTrazabilidad() {
        this.pasosEjecutados = new ArrayList<>();
        this.registrosHistorias = new HashMap<>();
        this.inicioEjecucion = LocalDateTime.now();
        this.idEjecucionActual = "EXEC-" + System.currentTimeMillis();

        cargarHistoriasUsuario();
        logger.debug("HelperTrazabilidad inicializado con ID: {}", idEjecucionActual);
    }

    // ==================== CLASES INTERNAS FALTANTES ====================

    /**
     * Clase interna para representar un paso ejecutado.
     */
    private static class RegistroPaso {
        String idHistoriaUsuario;
        String descripcion;
        LocalDateTime timestamp;
        String thread;
        EstadoPaso estado;
        String mensajeError;

        // Constructor por defecto
        public RegistroPaso() {
            this.timestamp = LocalDateTime.now();
            this.thread = Thread.currentThread().getName();
            this.estado = EstadoPaso.EJECUTADO;
        }
    }

    /**
     * Clase interna para representar una historia de usuario.
     */
    private static class RegistroHistoriaUsuario {
        String id;
        String titulo;
        String descripcion;
        List<String> criteriosAceptacion;
        String prioridad;
        String estado;
        int pasosEjecutados;
        int pasosFallidos;
        LocalDateTime ultimaEjecucion;

        // Constructor por defecto
        public RegistroHistoriaUsuario() {
            this.criteriosAceptacion = new ArrayList<>();
            this.pasosEjecutados = 0;
            this.pasosFallidos = 0;
            this.estado = "Pendiente";
            this.prioridad = "Media";
        }
    }

    /**
     * Enum para representar el estado de un paso.
     */
    private enum EstadoPaso {
        EJECUTADO, FALLIDO, OMITIDO, PENDIENTE
    }

    // ==================== MÉTODO PARA CARGAR HISTORIAS ====================

    /**
     * Carga las historias de usuario predefinidas.
     */
    private void cargarHistoriasUsuario() {
        // HU-001: Autenticación de Usuario
        RegistroHistoriaUsuario hu001 = new RegistroHistoriaUsuario();
        hu001.id = "HU-001";
        hu001.titulo = "Autenticación de Usuario";
        hu001.descripcion = "Como usuario del sistema quiero poder iniciar sesión con mis credenciales para acceder a las funcionalidades";
        hu001.criteriosAceptacion = List.of(
                "El usuario puede ingresar email y contraseña",
                "El sistema valida las credenciales",
                "Se muestra mensaje de error para credenciales inválidas",
                "Se redirige al dashboard para credenciales válidas"
        );
        hu001.prioridad = "Alta";
        hu001.estado = "En Desarrollo";
        registrosHistorias.put(hu001.id, hu001);

        // HU-002: Registro de Nuevo Usuario
        RegistroHistoriaUsuario hu002 = new RegistroHistoriaUsuario();
        hu002.id = "HU-002";
        hu002.titulo = "Registro de Nuevo Usuario";
        hu002.descripcion = "Como visitante del sitio web quiero poder registrarme en el sistema para obtener acceso a las funcionalidades";
        hu002.criteriosAceptacion = List.of(
                "Formulario con campos obligatorios",
                "Validación de formato de email",
                "Confirmación de contraseña",
                "Prevención de usuarios duplicados"
        );
        hu002.prioridad = "Alta";
        hu002.estado = "En Desarrollo";
        registrosHistorias.put(hu002.id, hu002);

        // HU-003: Gestión de Productos (CRUD)
        RegistroHistoriaUsuario hu003 = new RegistroHistoriaUsuario();
        hu003.id = "HU-003";
        hu003.titulo = "Gestión de Productos (CRUD)";
        hu003.descripcion = "Como usuario autenticado quiero gestionar productos en el sistema para mantener actualizado el catálogo";
        hu003.criteriosAceptacion = List.of(
                "Crear nuevos productos",
                "Visualizar lista de productos",
                "Editar productos existentes",
                "Eliminar productos"
        );
        hu003.prioridad = "Media";
        hu003.estado = "En Desarrollo";
        registrosHistorias.put(hu003.id, hu003);

        logger.debug("Historias de usuario cargadas: {}", registrosHistorias.size());
    }

    // ==================== MÉTODOS PRINCIPALES ====================

    /**
     * Registra un paso de ejecución asociado a una historia de usuario.
     * Este método es llamado desde los Step Definitions para trazabilidad.
     *
     * @param idHistoriaUsuario ID de la historia de usuario (ej: "HU-001")
     * @param descripcionPaso descripción del paso ejecutado
     */
    public void registrarPaso(String idHistoriaUsuario, String descripcionPaso) {
        if (idHistoriaUsuario == null || descripcionPaso == null) {
            logger.warn("ID de historia o descripción del paso es nulo - omitiendo registro");
            return;
        }

        try {
            // Crear registro del paso
            RegistroPaso paso = new RegistroPaso();
            paso.idHistoriaUsuario = idHistoriaUsuario;
            paso.descripcion = descripcionPaso;
            paso.estado = EstadoPaso.EJECUTADO;

            // Agregar a la lista de pasos
            pasosEjecutados.add(paso);

            // Actualizar estadísticas de la historia
            actualizarEstadisticasHistoria(idHistoriaUsuario);

            logger.debug("Paso registrado - HU: {}, Descripción: {}", idHistoriaUsuario, descripcionPaso);

        } catch (Exception e) {
            logger.error("Error registrando paso para HU {}: {}", idHistoriaUsuario, e.getMessage());
        }
    }

    /**
     * Registra un paso fallido con información del error.
     *
     * @param idHistoriaUsuario ID de la historia de usuario
     * @param descripcionPaso descripción del paso que falló
     * @param error excepción o error ocurrido
     */
    public void registrarPasoFallido(String idHistoriaUsuario, String descripcionPaso, Throwable error) {
        try {
            RegistroPaso paso = new RegistroPaso();
            paso.idHistoriaUsuario = idHistoriaUsuario;
            paso.descripcion = descripcionPaso;
            paso.estado = EstadoPaso.FALLIDO;
            paso.mensajeError = error != null ? error.getMessage() : "Error desconocido";

            pasosEjecutados.add(paso);
            actualizarEstadisticasHistoria(idHistoriaUsuario);

            logger.error("Paso fallido registrado - HU: {}, Error: {}", idHistoriaUsuario, paso.mensajeError);

        } catch (Exception e) {
            logger.error("Error registrando paso fallido: {}", e.getMessage());
        }
    }

    /**
     * Actualiza las estadísticas de una historia de usuario.
     *
     * @param idHistoriaUsuario ID de la historia
     */
    private void actualizarEstadisticasHistoria(String idHistoriaUsuario) {
        RegistroHistoriaUsuario historia = registrosHistorias.get(idHistoriaUsuario);
        if (historia != null) {
            // Contar pasos por estado para esta historia
            long pasosEjecutados = this.pasosEjecutados.stream()
                    .filter(p -> idHistoriaUsuario.equals(p.idHistoriaUsuario))
                    .filter(p -> p.estado == EstadoPaso.EJECUTADO)
                    .count();

            long pasosFallidos = this.pasosEjecutados.stream()
                    .filter(p -> idHistoriaUsuario.equals(p.idHistoriaUsuario))
                    .filter(p -> p.estado == EstadoPaso.FALLIDO)
                    .count();

            historia.pasosEjecutados = (int) pasosEjecutados;
            historia.pasosFallidos = (int) pasosFallidos;
            historia.ultimaEjecucion = LocalDateTime.now();

            // Actualizar estado general
            if (pasosFallidos > 0) {
                historia.estado = "Fallido";
            } else if (pasosEjecutados > 0) {
                historia.estado = "En Ejecución";
            }
        }
    }

    /**
     * Obtiene estadísticas de ejecución para reporting.
     *
     * @return mapa con estadísticas
     */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("totalPasos", pasosEjecutados.size());
        estadisticas.put("pasosExitosos",
                pasosEjecutados.stream().filter(p -> p.estado == EstadoPaso.EJECUTADO).count());
        estadisticas.put("pasosFallidos",
                pasosEjecutados.stream().filter(p -> p.estado == EstadoPaso.FALLIDO).count());
        estadisticas.put("totalHistorias", registrosHistorias.size());
        estadisticas.put("inicioEjecucion", inicioEjecucion);
        estadisticas.put("idEjecucion", idEjecucionActual);

        return estadisticas;
    }

    /**
     * Registra el resultado de una acción específica.
     * Versión simplificada compatible con la estructura actual.
     *
     * @param accion descripción de la acción ejecutada
     * @param resultado resultado obtenido de la acción
     */
    public void registrarResultado(String accion, String resultado) {
        try {
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String entrada = String.format("[%s] RESULTADO - %s: %s", timestamp, accion, resultado);

            logger.info("Registrando resultado: {} -> {}", accion, resultado);
            logger.debug("Entrada de trazabilidad: {}", entrada);

            // Si tienes algún sistema de almacenamiento de trazabilidad, agrégalo aquí
            // Por ahora solo loggeamos el resultado

        } catch (Exception e) {
            logger.error("Error registrando resultado: {}", e.getMessage(), e);
        }
    }

// Si necesitas variables adicionales, agrega estas al inicio de tu clase HelperTrazabilidad:

    // Variables de clase (agregar al inicio de la clase si no existen)
    private final java.util.Map<String, Object> registrosEscenarios = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.List<String> historialAcciones = new java.util.ArrayList<>();

    /**
     * Registra una acción en el historial de trazabilidad.
     *
     * @param accion descripción de la acción
     * @param detalles detalles adicionales de la acción
     */
    public void registrarAccion(String accion, String detalles) {
        try {
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String entrada = String.format("[%s] ACCION - %s: %s", timestamp, accion, detalles);

            synchronized (historialAcciones) {
                historialAcciones.add(entrada);
            }

            logger.info("Acción registrada: {} - {}", accion, detalles);

        } catch (Exception e) {
            logger.error("Error registrando acción: {}", e.getMessage(), e);
        }
    }

    /**
     * Obtiene el historial completo de acciones registradas.
     *
     * @return lista con todas las acciones registradas
     */
    public java.util.List<String> obtenerHistorialAcciones() {
        synchronized (historialAcciones) {
            return new java.util.ArrayList<>(historialAcciones);
        }
    }

    /**
     * Limpia el historial de acciones.
     */
    public void limpiarHistorial() {
        synchronized (historialAcciones) {
            historialAcciones.clear();
        }
        registrosEscenarios.clear();
        logger.info("Historial de trazabilidad limpiado");
    }

}