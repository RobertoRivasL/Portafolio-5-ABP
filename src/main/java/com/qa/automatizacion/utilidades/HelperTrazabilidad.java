package com.qa.automatizacion.utilidades;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper para gestión de trazabilidad y auditoría de escenarios.
 * Registra la ejecución de pruebas y su vinculación con historias de usuario.
 *
 * Principios aplicados:
 * - Singleton Pattern: Una sola instancia para todo el sistema
 * - Thread Safety: Operaciones seguras en entornos concurrentes
 * - Single Responsibility: Se enfoca únicamente en trazabilidad
 */
public class HelperTrazabilidad {

    private static final Logger logger = LoggerFactory.getLogger(HelperTrazabilidad.class);
    private static HelperTrazabilidad instancia;

    // Almacenamiento thread-safe de datos de trazabilidad
    private final Map<String, EscenarioTrazabilidad> escenarios;
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor privado para Singleton.
     */
    private HelperTrazabilidad() {
        this.escenarios = new ConcurrentHashMap<>();
        logger.info("HelperTrazabilidad inicializado");
    }

    /**
     * Obtiene la instancia única del helper.
     *
     * @return instancia única
     */
    public static synchronized HelperTrazabilidad obtenerInstancia() {
        if (instancia == null) {
            instancia = new HelperTrazabilidad();
        }
        return instancia;
    }

    /**
     * Registra el inicio de un escenario.
     *
     * @param historiaUsuario ID de la historia de usuario
     * @param nombreEscenario nombre del escenario
     */
    public void iniciarEscenario(String historiaUsuario, String nombreEscenario) {
        EscenarioTrazabilidad escenario = new EscenarioTrazabilidad();
        escenario.historiaUsuario = historiaUsuario;
        escenario.nombreEscenario = nombreEscenario;
        escenario.inicioEjecucion = LocalDateTime.now();

        escenarios.put(historiaUsuario + "_" + nombreEscenario, escenario);
        logger.info("Escenario iniciado: {} - {}", historiaUsuario, nombreEscenario);
    }

    /**
     * Registra el final de un escenario.
     *
     * @param historiaUsuario ID de la historia de usuario
     * @param resultado resultado del escenario
     */
    public void finalizarEscenario(String historiaUsuario, String resultado) {
        String clave = encontrarClaveEscenario(historiaUsuario);
        if (clave != null) {
            EscenarioTrazabilidad escenario = escenarios.get(clave);
            escenario.finEjecucion = LocalDateTime.now();
            escenario.resultado = resultado;

            logger.info("Escenario finalizado: {} - {}", historiaUsuario, resultado);
        }
    }

    /**
     * Registra un paso ejecutado en un escenario.
     *
     * @param historiaUsuario ID de la historia de usuario
     * @param descripcionPaso descripción del paso
     */
    public void registrarPaso(String historiaUsuario, String descripcionPaso) {
        String clave = encontrarClaveEscenario(historiaUsuario);
        if (clave != null) {
            EscenarioTrazabilidad escenario = escenarios.get(clave);
            escenario.pasos.add(formatoFecha.format(LocalDateTime.now()) + " - " + descripcionPaso);
        }

        logger.debug("Paso registrado [{}]: {}", historiaUsuario, descripcionPaso);
    }

    /**
     * Registra una navegación realizada.
     *
     * @param url URL navegada
     */
    public void registrarNavegacion(String url) {
        logger.debug("Navegación registrada: {}", url);
        // Aquí se podría extender para registrar navegaciones específicas
    }

    /**
     * Registra una acción realizada.
     *
     * @param accion tipo de acción
     * @param detalle detalle de la acción
     */
    public void registrarAccion(String accion, String detalle) {
        logger.debug("Acción registrada: {} - {}", accion, detalle);
        // Aquí se podría extender para registrar acciones específicas
    }

    /**
     * Registra un error ocurrido durante la ejecución.
     *
     * @param contexto contexto del error
     * @param mensaje mensaje del error
     * @param rutaScreenshot ruta del screenshot capturado
     */
    public void registrarError(String contexto, String mensaje, String rutaScreenshot) {
        logger.error("Error registrado - Contexto: {}, Mensaje: {}, Screenshot: {}",
                contexto, mensaje, rutaScreenshot);

        // Registrar en el escenario actual si existe
        for (EscenarioTrazabilidad escenario : escenarios.values()) {
            if (escenario.finEjecucion == null) { // Escenario en ejecución
                escenario.errores.add(String.format("%s - %s: %s (Screenshot: %s)",
                        formatoFecha.format(LocalDateTime.now()), contexto, mensaje, rutaScreenshot));
                break;
            }
        }
    }

    /**
     * Limpia datos temporales de trazabilidad.
     */
    public void limpiarDatosTempo() {
        // Mantener solo escenarios finalizados de la sesión actual
        escenarios.entrySet().removeIf(entry ->
                entry.getValue().finEjecucion == null ||
                        entry.getValue().inicioEjecucion.isBefore(LocalDateTime.now().minusHours(1))
        );

        logger.debug("Datos temporales de trazabilidad limpiados");
    }

    /**
     * Genera reporte de trazabilidad.
     *
     * @return reporte en formato texto
     */
    public String generarReporte() {
        StringBuilder reporte = new StringBuilder();
        reporte.append("=== REPORTE DE TRAZABILIDAD ===\n");
        reporte.append("Fecha: ").append(formatoFecha.format(LocalDateTime.now())).append("\n\n");

        for (EscenarioTrazabilidad escenario : escenarios.values()) {
            reporte.append("Historia de Usuario: ").append(escenario.historiaUsuario).append("\n");
            reporte.append("Escenario: ").append(escenario.nombreEscenario).append("\n");
            reporte.append("Inicio: ").append(formatoFecha.format(escenario.inicioEjecucion)).append("\n");

            if (escenario.finEjecucion != null) {
                reporte.append("Fin: ").append(formatoFecha.format(escenario.finEjecucion)).append("\n");
                reporte.append("Resultado: ").append(escenario.resultado).append("\n");
            } else {
                reporte.append("Estado: EN EJECUCIÓN\n");
            }

            reporte.append("Pasos ejecutados: ").append(escenario.pasos.size()).append("\n");
            if (!escenario.errores.isEmpty()) {
                reporte.append("Errores: ").append(escenario.errores.size()).append("\n");
            }
            reporte.append("---\n");
        }

        return reporte.toString();
    }

    /**
     * Encuentra la clave de un escenario por historia de usuario.
     *
     * @param historiaUsuario ID de la historia de usuario
     * @return clave del escenario o null si no se encuentra
     */
    private String encontrarClaveEscenario(String historiaUsuario) {
        return escenarios.keySet().stream()
                .filter(clave -> clave.startsWith(historiaUsuario + "_"))
                .findFirst()
                .orElse(null);
    }

    /**
     * Clase interna para representar un escenario de trazabilidad.
     */
    private static class EscenarioTrazabilidad {
        String historiaUsuario;
        String nombreEscenario;
        LocalDateTime inicioEjecucion;
        LocalDateTime finEjecucion;
        String resultado;
        java.util.List<String> pasos = new java.util.ArrayList<>();
        java.util.List<String> errores = new java.util.ArrayList<>();
    }
}