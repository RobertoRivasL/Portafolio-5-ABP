package com.qa.automatizacion.hooks;

import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import com.qa.automatizacion.configuracion.PropiedadesAplicacion;
import com.qa.automatizacion.utilidades.Utileria;
import com.qa.automatizacion.utilidades.HelperTrazabilidad;

import io.cucumber.java.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Hooks de Cucumber para gestionar el ciclo de vida de las pruebas.
 * Los hooks se ejecutan en momentos específicos del ciclo de vida de Cucumber.
 *
 * Esta clase implementa:
 * - Configuración global del entorno de pruebas
 * - Inicialización y limpieza de recursos por escenario
 * - Captura de screenshots en caso de fallo
 * - Registro de trazabilidad unificado
 * - Manejo de errores y recuperación de estado
 *
 * Hooks disponibles:
 * - @BeforeAll: Se ejecuta una vez antes de todos los escenarios
 * - @Before: Se ejecuta antes de cada escenario
 * - @After: Se ejecuta después de cada escenario
 * - @AfterAll: Se ejecuta una vez después de todos los escenarios
 *
 * Principios aplicados:
 * - Single Responsibility: Cada hook tiene una responsabilidad específica
 * - DRY: Centraliza la lógica de configuración y limpieza
 * - Fail-Safe: Maneja errores para no interrumpir la ejecución
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 2.0.0
 */
public class HooksPruebas {

    private static final Logger logger = LoggerFactory.getLogger(HooksPruebas.class);

    // Componentes centrales
    private Utileria utileria;
    private HelperTrazabilidad trazabilidad;
    private PropiedadesAplicacion propiedades;

    // Control de estado
    private static boolean configuracionGlobalCompletada = false;
    private static int contadorEscenarios = 0;
    private static int escenariosPasados = 0;
    private static int escenariosEjecutados = 0;

    // Información del escenario actual
    private String nombreEscenarioActual;
    private String historiaUsuarioActual;
    private LocalDateTime inicioEscenario;

    // ==================== HOOKS GLOBALES ====================

    /**
     * Se ejecuta una vez antes de todos los escenarios.
     * Configura el entorno global de pruebas.
     */
    @BeforeAll
    public static void configuracionGlobal() {
        Logger loggerEstatico = LoggerFactory.getLogger(HooksPruebas.class);

        try {
            loggerEstatico.info("=".repeat(80));
            loggerEstatico.info("🚀 INICIANDO EJECUCIÓN DE PRUEBAS BDD");
            loggerEstatico.info("=".repeat(80));

            // Configurar propiedades del sistema
            PropiedadesAplicacion propiedades = PropiedadesAplicacion.obtenerInstancia();
            loggerEstatico.info("📋 Configuración cargada desde: {}",
                    propiedades.obtenerRutaArchivoConfiguracion());

            // Inicializar trazabilidad global
            HelperTrazabilidad trazabilidad = HelperTrazabilidad.obtenerInstancia();
            trazabilidad.inicializarSesionEjecucion();

            // Configurar navegador si es necesario
            String navegadorTipo = propiedades.obtenerTipoNavegador();
            loggerEstatico.info("🌐 Navegador configurado: {}", navegadorTipo);

            // Verificar URLs de configuración
            loggerEstatico.info("🔗 URL Base: {}", propiedades.obtenerUrlBase());
            loggerEstatico.info("🔗 URL Login: {}", propiedades.obtenerUrlLogin());

            configuracionGlobalCompletada = true;
            loggerEstatico.info("✅ Configuración global completada exitosamente");

        } catch (Exception e) {
            loggerEstatico.error("❌ Error en configuración global: {}", e.getMessage(), e);
            throw new RuntimeException("Error en configuración global: " + e.getMessage(), e);
        }
    }

    /**
     * Se ejecuta una vez después de todos los escenarios.
     * Realiza limpieza global y genera reportes finales.
     */
    @AfterAll
    public static void limpiezaGlobal() {
        Logger loggerEstatico = LoggerFactory.getLogger(HooksPruebas.class);

        try {
            loggerEstatico.info("=".repeat(80));
            loggerEstatico.info("🏁 FINALIZANDO EJECUCIÓN DE PRUEBAS BDD");
            loggerEstatico.info("=".repeat(80));

            // Estadísticas de ejecución
            loggerEstatico.info("📊 Estadísticas de Ejecución:");
            loggerEstatico.info("   • Total de escenarios ejecutados: {}", escenariosEjecutados);
            loggerEstatico.info("   • Escenarios exitosos: {}", escenariosPasados);
            loggerEstatico.info("   • Escenarios fallidos: {}", escenariosEjecutados - escenariosPasados);

            if (escenariosEjecutados > 0) {
                double porcentajeExito = (double) escenariosPasados / escenariosEjecutados * 100;
                loggerEstatico.info("   • Porcentaje de éxito: {:.2f}%", porcentajeExito);
            }

            // Finalizar trazabilidad
            try {
                HelperTrazabilidad trazabilidad = HelperTrazabilidad.obtenerInstancia();
                trazabilidad.finalizarSesionEjecucion();
                loggerEstatico.info("📋 Reporte de trazabilidad generado");
            } catch (Exception e) {
                loggerEstatico.warn("⚠️  Error generando reporte de trazabilidad: {}", e.getMessage());
            }

            // Cerrar navegador si está abierto
            try {
                ConfiguradorNavegador.cerrarNavegador();
                loggerEstatico.info("🌐 Navegador cerrado");
            } catch (Exception e) {
                loggerEstatico.warn("⚠️  Error cerrando navegador: {}", e.getMessage());
            }

            loggerEstatico.info("✅ Limpieza global completada");
            loggerEstatico.info("=".repeat(80));

        } catch (Exception e) {
            loggerEstatico.error("❌ Error en limpieza global: {}", e.getMessage(), e);
        }
    }

    // ==================== HOOKS POR ESCENARIO ====================

    /**
     * Se ejecuta antes de cada escenario.
     * Prepara el entorno específico para el escenario.
     */
    @Before
    public void antesDelEscenario(Scenario escenario) {
        try {
            // Incrementar contador
            contadorEscenarios++;
            escenariosEjecutados++;
            inicioEscenario = LocalDateTime.now();

            // Extraer información del escenario
            nombreEscenarioActual = escenario.getName();
            historiaUsuarioActual = extraerHistoriaUsuario(escenario);

            logger.info("🎬 INICIANDO ESCENARIO #{}: {}", contadorEscenarios, nombreEscenarioActual);
            logger.info("📝 Historia de Usuario: {}", historiaUsuarioActual);
            logger.info("🏷️  Tags: {}", escenario.getSourceTagNames());

            // Inicializar componentes
            inicializarComponentes();

            // Configurar navegador para el escenario
            configurarNavegadorParaEscenario(escenario);

            // Registrar inicio en trazabilidad
            if (trazabilidad != null) {
                trazabilidad.iniciarEscenario(historiaUsuarioActual, nombreEscenarioActual);
            }

            // Limpiar estado previo
            limpiarEstadoPrevio();

            logger.debug("✅ Configuración previa del escenario completada");

        } catch (Exception e) {
            logger.error("❌ Error en configuración previa del escenario: {}", e.getMessage(), e);

            // Capturar screenshot del error
            if (utileria != null) {
                utileria.capturarScreenshotError("configuracion_escenario_error");
            }

            throw new RuntimeException("Error configurando escenario: " + e.getMessage(), e);
        }
    }

    /**
     * Se ejecuta después de cada escenario.
     * Realiza limpieza y captura evidencias.
     */
    @After
    public void despuesDelEscenario(Scenario escenario) {
        try {
            LocalDateTime finEscenario = LocalDateTime.now();
            long duracionSegundos = java.time.Duration.between(inicioEscenario, finEscenario).getSeconds();

            String estado = escenario.getStatus().toString();
            String estadoEmoji = obtenerEmojiEstado(estado);

            logger.info("🎬 FINALIZANDO ESCENARIO #{}: {}", contadorEscenarios, nombreEscenarioActual);
            logger.info("{}  Estado: {} | Duración: {}s", estadoEmoji, estado, duracionSegundos);

            // Actualizar contadores
            if ("PASSED".equals(estado)) {
                escenariosPasados++;
            }

            // Capturar screenshot según el resultado
            if (utileria != null) {
                capturarEvidenciaEscenario(escenario, estado);
            }

            // Registrar fin en trazabilidad
            if (trazabilidad != null) {
                trazabilidad.finalizarEscenario(historiaUsuarioActual, estado);
            }

            // Adjuntar información adicional al reporte
            adjuntarInformacionDebug(escenario);

            // Limpiar recursos del escenario
            limpiarRecursosEscenario();

            logger.info("✅ Limpieza posterior del escenario completada");
            logger.info("-".repeat(80));

        } catch (Exception e) {
            logger.error("❌ Error en limpieza posterior del escenario: {}", e.getMessage(), e);
        }
    }

    // ==================== HOOKS ESPECÍFICOS POR TAG ====================

    /**
     * Hook específico para escenarios de login.
     */
    @Before("@Login")
    public void antesDelEscenarioLogin(Scenario escenario) {
        logger.info("🔐 Configuración específica para escenarios de Login");

        try {
            // Configuraciones específicas para login
            if (utileria != null) {
                // Navegar a página de login si no estamos allí
                String urlLogin = utileria.obtenerUrlLogin();
                logger.debug("Verificando navegación a: {}", urlLogin);
            }

        } catch (Exception e) {
            logger.warn("⚠️  Error en configuración específica de Login: {}", e.getMessage());
        }
    }

    /**
     * Hook específico para escenarios de registro.
     */
    @Before("@Registro")
    public void antesDelEscenarioRegistro(Scenario escenario) {
        logger.info("📝 Configuración específica para escenarios de Registro");

        try {
            // Configuraciones específicas para registro
            if (utileria != null) {
                // Limpiar datos de usuario previos si es necesario
                utileria.limpiarRecursos();
            }

        } catch (Exception e) {
            logger.warn("⚠️  Error en configuración específica de Registro: {}", e.getMessage());
        }
    }

    /**
     * Hook específico para escenarios CRUD.
     */
    @Before("@CRUD")
    public void antesDelEscenarioCrud(Scenario escenario) {
        logger.info("🔄 Configuración específica para escenarios CRUD");

        try {
            // Configuraciones específicas para CRUD
            // Por ejemplo: limpiar base de datos, configurar datos de prueba

        } catch (Exception e) {
            logger.warn("⚠️  Error en configuración específica de CRUD: {}", e.getMessage());
        }
    }

    /**
     * Hook para escenarios de smoke test.
     */
    @Before("@SmokeTest")
    public void antesDelSmokeTest(Scenario escenario) {
        logger.info("💨 Configuración para Smoke Test - verificación rápida");

        try {
            // Configuración mínima para smoke tests
            // Timeout más cortos, verificaciones básicas

        } catch (Exception e) {
            logger.warn("⚠️  Error en configuración de Smoke Test: {}", e.getMessage());
        }
    }

    // ==================== MÉTODOS PRIVADOS DE UTILIDAD ====================

    /**
     * Inicializa los componentes necesarios para el escenario.
     */
    private void inicializarComponentes() {
        try {
            // Verificar que la configuración global esté completa
            if (!configuracionGlobalCompletada) {
                throw new IllegalStateException("Configuración global no completada");
            }

            // Inicializar componentes
            this.propiedades = PropiedadesAplicacion.obtenerInstancia();
            this.trazabilidad = HelperTrazabilidad.obtenerInstancia();
            this.utileria = Utileria.obtenerInstancia();

            logger.debug("Componentes inicializados correctamente");

        } catch (Exception e) {
            logger.error("Error inicializando componentes: {}", e.getMessage());
            throw new RuntimeException("Error en inicialización: " + e.getMessage(), e);
        }
    }

    /**
     * Configura el navegador específicamente para el escenario.
     */
    private void configurarNavegadorParaEscenario(Scenario escenario) {
        try {
            // Obtener tags del escenario
            var tags = escenario.getSourceTagNames();

            // Configurar navegador si no está inicializado
            if (!ConfiguradorNavegador.esNavegadorInicializado()) {
                logger.debug("Inicializando navegador para escenario");
                ConfiguradorNavegador.inicializarNavegador();
            }

            // Configuraciones específicas basadas en tags
            if (tags.contains("@Headless")) {
                logger.debug("Modo headless requerido por tag");
                // El navegador ya debería estar configurado en modo headless
            }

            if (tags.contains("@Mobile")) {
                logger.debug("Emulación móvil requerida por tag");
                ConfiguradorNavegador.configurarEmulacionMovil();
            }

            if (tags.contains("@Slow")) {
                logger.debug("Timeouts extendidos por tag @Slow");
                ConfiguradorNavegador.configurarTimeoutsExtendidos();
            }

            // Verificar que el navegador esté funcionando
            utileria.navegarA(propiedades.obtenerUrlBase());

            logger.debug("Navegador configurado para escenario");

        } catch (Exception e) {
            logger.error("Error configurando navegador: {}", e.getMessage());
            throw new RuntimeException("Error configurando navegador: " + e.getMessage(), e);
        }
    }

    /**
     * Extrae la historia de usuario del nombre del escenario o tags.
     */
    private String extraerHistoriaUsuario(Scenario escenario) {
        try {
            // Buscar en tags primero
            for (String tag : escenario.getSourceTagNames()) {
                if (tag.matches("@HU-\\d+")) {
                    return tag.substring(1); // Remover el @
                }
            }

            // Buscar en el nombre del escenario
            String nombre = escenario.getName();
            if (nombre.contains("HU-")) {
                int inicio = nombre.indexOf("HU-");
                int fin = nombre.indexOf(" ", inicio);
                if (fin == -1) fin = nombre.indexOf(":", inicio);
                if (fin == -1) fin = inicio + 6; // HU-XXX

                return nombre.substring(inicio, Math.min(fin, nombre.length()));
            }

            // Valor por defecto
            return "HU-GENERAL";

        } catch (Exception e) {
            logger.warn("Error extrayendo historia de usuario: {}", e.getMessage());
            return "HU-UNKNOWN";
        }
    }

    /**
     * Limpia el estado previo de componentes.
     */
    private void limpiarEstadoPrevio() {
        try {
            logger.debug("Limpiando estado previo");

            // Limpiar utileria
            if (utileria != null) {
                utileria.limpiarRecursos();
            }

            // Limpiar cookies y storage del navegador
            if (ConfiguradorNavegador.esNavegadorInicializado()) {
                var navegador = ConfiguradorNavegador.obtenerNavegador();
                navegador.manage().deleteAllCookies();
            }

        } catch (Exception e) {
            logger.warn("Error limpiando estado previo: {}", e.getMessage());
        }
    }

    /**
     * Captura evidencia según el resultado del escenario.
     */
    private void capturarEvidenciaEscenario(Scenario escenario, String estado) {
        try {
            String nombreArchivo = String.format("escenario_%d_%s_%s",
                    contadorEscenarios,
                    estado.toLowerCase(),
                    nombreEscenarioActual.replaceAll("[^a-zA-Z0-9]", "_"));

            String rutaScreenshot = utileria.capturarScreenshot(nombreArchivo);

            if (rutaScreenshot != null) {
                logger.debug("Screenshot capturado: {}", rutaScreenshot);

                // Adjuntar al reporte de Cucumber si es posible
                try {
                    byte[] screenshot = java.nio.file.Files.readAllBytes(
                            java.nio.file.Paths.get(rutaScreenshot));
                    escenario.attach(screenshot, "image/png", "Screenshot del escenario");
                } catch (Exception e) {
                    logger.warn("Error adjuntando screenshot al reporte: {}", e.getMessage());
                }
            }

            // Captura adicional para escenarios fallidos
            if ("FAILED".equals(estado)) {
                logger.info("📸 Capturando evidencia adicional para escenario fallido");
                utileria.capturarScreenshotError("escenario_fallido_" + contadorEscenarios);
            }

        } catch (Exception e) {
            logger.warn("Error capturando evidencia: {}", e.getMessage());
        }
    }

    /**
     * Adjunta información de debugging al reporte.
     */
    private void adjuntarInformacionDebug(Scenario escenario) {
        try {
            StringBuilder infoDebug = new StringBuilder();
            infoDebug.append("=== INFORMACIÓN DE DEBUG ===\n");
            infoDebug.append("Escenario: ").append(nombreEscenarioActual).append("\n");
            infoDebug.append("Historia de Usuario: ").append(historiaUsuarioActual).append("\n");
            infoDebug.append("Estado: ").append(escenario.getStatus()).append("\n");
            infoDebug.append("Inicio: ").append(inicioEscenario.format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            infoDebug.append("Duración: ").append(
                            java.time.Duration.between(inicioEscenario, LocalDateTime.now()).getSeconds())
                    .append(" segundos\n");

            // Información del entorno
            if (utileria != null) {
                infoDebug.append("Entorno: ").append(utileria.obtenerInformacionEntorno()).append("\n");
            }

            // Información de configuración
            if (propiedades != null) {
                infoDebug.append("URL Base: ").append(propiedades.obtenerUrlBase()).append("\n");
                infoDebug.append("Navegador: ").append(propiedades.obtenerTipoNavegador()).append("\n");
            }

            infoDebug.append("===============================");

            // Adjuntar como texto al reporte
            escenario.attach(infoDebug.toString(), "text/plain", "Información de Debug");

        } catch (Exception e) {
            logger.warn("Error adjuntando información de debug: {}", e.getMessage());
        }
    }

    /**
     * Limpia recursos específicos del escenario.
     */
    private void limpiarRecursosEscenario() {
        try {
            logger.debug("Limpiando recursos del escenario");

            // Limpiar variables de instancia
            nombreEscenarioActual = null;
            historiaUsuarioActual = null;
            inicioEscenario = null;

            // Limpiar componentes si es necesario
            if (utileria != null) {
                try {
                    utileria.limpiarRecursos();
                } catch (Exception e) {
                    logger.warn("Error limpiando utileria: {}", e.getMessage());
                }
            }

            // No cerrar navegador aquí ya que puede ser reutilizado
            // Solo limpiar cookies y estado
            if (ConfiguradorNavegador.esNavegadorInicializado()) {
                try {
                    var navegador = ConfiguradorNavegador.obtenerNavegador();
                    navegador.manage().deleteAllCookies();
                } catch (Exception e) {
                    logger.warn("Error limpiando cookies: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.warn("Error en limpieza de recursos: {}", e.getMessage());
        }
    }

    /**
     * Obtiene emoji según el estado del escenario.
     */
    private String obtenerEmojiEstado(String estado) {
        return switch (estado.toUpperCase()) {
            case "PASSED" -> "✅";
            case "FAILED" -> "❌";
            case "SKIPPED" -> "⏭️";
            case "PENDING" -> "⏳";
            case "UNDEFINED" -> "❓";
            default -> "⚪";
        };
    }

    // ==================== HOOKS PARA MANEJO DE ERRORES ====================

    /**
     * Hook que se ejecuta cuando hay una excepción no manejada.
     */
    @After
    public void manejarErroresNoManeJados(Scenario escenario) {
        if (escenario.isFailed()) {
            logger.error("💥 Escenario falló: {}", nombreEscenarioActual);

            try {
                // Capturar información adicional del error
                if (utileria != null) {
                    String infoEntorno = utileria.obtenerInformacionEntorno();
                    escenario.attach(infoEntorno, "text/plain", "Estado del entorno al fallar");

                    // Screenshot de emergencia
                    utileria.capturarScreenshotError("fallo_emergencia_" + contadorEscenarios);
                }

                // Log detallado para debugging
                logger.error("Información del fallo:");
                logger.error("  - Escenario: {}", nombreEscenarioActual);
                logger.error("  - Historia: {}", historiaUsuarioActual);
                logger.error("  - Tiempo transcurrido: {} segundos",
                        inicioEscenario != null ?
                                java.time.Duration.between(inicioEscenario, LocalDateTime.now()).getSeconds() : "N/A");

            } catch (Exception e) {
                logger.error("Error adicional capturando información de fallo: {}", e.getMessage());
            }
        }
    }

    // ==================== MÉTODOS PÚBLICOS DE UTILIDAD ====================

    /**
     * Obtiene estadísticas de la ejecución actual.
     */
    public static String obtenerEstadisticas() {
        return String.format(
                "Escenarios ejecutados: %d | Exitosos: %d | Fallidos: %d | Porcentaje éxito: %.2f%%",
                escenariosEjecutados,
                escenariosPasados,
                escenariosEjecutados - escenariosPasados,
                escenariosEjecutados > 0 ? (double) escenariosPasados / escenariosEjecutados * 100 : 0.0
        );
    }

    /**
     * Obtiene el nombre del escenario actual.
     */
    public String obtenerNombreEscenarioActual() {
        return nombreEscenarioActual;
    }

    /**
     * Obtiene la historia de usuario actual.
     */
    public String obtenerHistoriaUsuarioActual() {
        return historiaUsuarioActual;
    }

    /**
     * Verifica si la configuración global está completa.
     */
    public static boolean esConfiguracionGlobalCompleta() {
        return configuracionGlobalCompletada;
    }
}