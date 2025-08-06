package com.qa.automatizacion.ejecutor;

import io.cucumber.junit.platform.engine.Cucumber;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Ejecutor especializado para pruebas de regresión completas.
 * Configura la ejecución de todas las pruebas marcadas con @Regression.
 *
 * Este ejecutor está diseñado para:
 * - Ejecutar un conjunto completo de pruebas de regresión
 * - Generar reportes detallados con trazabilidad
 * - Configurar timeouts apropiados para ejecuciones largas
 * - Integrar con pipelines de CI/CD
 *
 * Principios aplicados:
 * - Single Responsibility: Se enfoca únicamente en pruebas de regresión
 * - Configuración centralizada: Todas las opciones en un lugar
 * - Separación de Intereses: Diferentes ejecutores para diferentes propósitos
 *
 * @author Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
 * @version 1.0.0
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@Cucumber
public class EjecutorRegressionTests {

    // ==================== CONFIGURACIÓN DE FILTROS ====================

    /**
     * Filtro principal: Solo ejecutar escenarios de regresión
     * Incluye todos los escenarios marcados con @Regression
     * Excluye escenarios en desarrollo (@WIP) y bloqueados (@Blocked)
     */
    @ConfigurationParameter(
            key = FILTER_TAGS_PROPERTY_NAME,
            value = "@Regression and not @WIP and not @Blocked"
    )
    static void filtroTags() {}

    // ==================== CONFIGURACIÓN DE FEATURES ====================

    /**
     * Glue packages: Ubicación de los Step Definitions
     */
    @ConfigurationParameter(
            key = GLUE_PROPERTY_NAME,
            value = "com.qa.automatizacion.pasos,com.qa.automatizacion.hooks"
    )
    static void configurarGlue() {}

    /**
     * Directorio de features: Ubicación de los archivos .feature
     */
    @ConfigurationParameter(
            key = FEATURES_PROPERTY_NAME,
            value = "src/test/resources/features"
    )
    static void configurarFeatures() {}

    // ==================== CONFIGURACIÓN DE PLUGINS Y REPORTES ====================

    /**
     * Plugin para reporte HTML interactivo
     */
    @ConfigurationParameter(
            key = PLUGIN_PROPERTY_NAME,
            value = "html:reportes/html/regression-report.html"
    )
    static void configurarReporteHtml() {}

    /**
     * Plugin para reporte JSON (integración con herramientas externas)
     */
    @ConfigurationParameter(
            key = PLUGIN_PROPERTY_NAME,
            value = "json:reportes/json/regression-report.json"
    )
    static void configurarReporteJson() {}

    /**
     * Plugin para reporte JUnit (compatibilidad con CI/CD)
     */
    @ConfigurationParameter(
            key = PLUGIN_PROPERTY_NAME,
            value = "junit:reportes/junit/regression-report.xml"
    )
    static void configurarReporteJunit() {}

    /**
     * Plugin para salida detallada en consola
     */
    @ConfigurationParameter(
            key = PLUGIN_PROPERTY_NAME,
            value = "pretty"
    )
    static void configurarSalidaConsola() {}

    /**
     * Plugin para reporte de uso (análisis de pasos no utilizados)
     */
    @ConfigurationParameter(
            key = PLUGIN_PROPERTY_NAME,
            value = "usage:reportes/usage/regression-usage.json"
    )
    static void configurarReporteUsage() {}

    /**
     * Plugin para timeline de ejecución
     */
    @ConfigurationParameter(
            key = PLUGIN_PROPERTY_NAME,
            value = "timeline:reportes/timeline/regression-timeline.html"
    )
    static void configurarTimeline() {}

    // ==================== CONFIGURACIÓN DE COMPORTAMIENTO ====================

    /**
     * Configurar para que NO falle rápido en el primer error
     * Permitir que todas las pruebas se ejecuten para obtener un panorama completo
     */
    @ConfigurationParameter(
            key = EXECUTION_DRY_RUN_PROPERTY_NAME,
            value = "false"
    )
    static void configurarEjecucionCompleta() {}

    /**
     * Configurar para mostrar snippets de pasos faltantes
     */
    @ConfigurationParameter(
            key = PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME,
            value = "false"
    )
    static void deshabilitarPublicacion() {}

    /**
     * Configurar modo estricto para validar que todos los pasos estén definidos
     */
    @ConfigurationParameter(
            key = EXECUTION_STRICT_PROPERTY_NAME,
            value = "true"
    )
    static void configurarModoEstricto() {}

    // ==================== CONFIGURACIÓN DE PARALELIZACIÓN ====================

    /**
     * Habilitar ejecución paralela por features para acelerar las pruebas de regresión
     * NOTA: Solo habilitar si la aplicación bajo prueba soporta múltiples sesiones concurrentes
     */
    @ConfigurationParameter(
            key = PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME,
            value = "true"
    )
    static void habilitarParalelizacion() {}

    /**
     * Configurar estrategia de paralelización
     */
    @ConfigurationParameter(
            key = "cucumber.execution.parallel.config.strategy",
            value = "dynamic"
    )
    static void configurarEstrategiaParalela() {}

    /**
     * Número de threads paralelos
     * Ajustar según la capacidad del servidor de CI/CD
     */
    @ConfigurationParameter(
            key = "cucumber.execution.parallel.config.fixed.parallelism",
            value = "4"
    )
    static void configurarNumeroThreads() {}

    // ==================== CONFIGURACIÓN DE ORDENAMIENTO ====================

    /**
     * Configurar orden de ejecución de escenarios
     * Para regresión: alfabético para consistencia en reportes
     */
    @ConfigurationParameter(
            key = "cucumber.execution.order",
            value = "lexical"
    )
    static void configurarOrdenEjecucion() {}

    // ==================== CONFIGURACIÓN DE SNIPPETS ====================

    /**
     * Configurar formato de snippets para nuevos pasos
     */
    @ConfigurationParameter(
            key = SNIPPET_TYPE_PROPERTY_NAME,
            value = "camelcase"
    )
    static void configurarSnippets() {}

    // ==================== CONFIGURACIÓN DE TRANSFORMACIÓN ====================

    /**
     * Configurar transformaciones de objetos para DataTables
     */
    @ConfigurationParameter(
            key = OBJECT_FACTORY_PROPERTY_NAME,
            value = "io.cucumber.core.objectfactory.DefaultObjectFactory"
    )
    static void configurarObjectFactory() {}

    // ==================== MÉTODOS DE UTILIDAD ESTÁTICOS ====================

    /**
     * Configuración adicional que puede ser llamada desde scripts externos
     */
    public static void configurarPropiedadesExternas() {
        // Configuraciones que pueden ser sobrescritas por variables de entorno o propiedades del sistema

        // Timeout para escenarios de regresión (más largo que smoke tests)
        System.setProperty("cucumber.execution.timeout", "300"); // 5 minutos por escenario

        // Configurar nivel de logging para regresión
        System.setProperty("cucumber.logging.level", "INFO");

        // Configurar captura de screenshots para regresión
        System.setProperty("cucumber.screenshots.regression", "on-failure");

        // Configurar retry de escenarios fallidos (útil para regresión)
        System.setProperty("cucumber.execution.retry.failed", "1");

        // Configurar limpieza de datos entre escenarios
        System.setProperty("cucumber.data.cleanup.between.scenarios", "true");

        // Configurar reporting extendido para regresión
        System.setProperty("cucumber.reporting.extended", "true");

        System.out.println("=== CONFIGURACIÓN DE REGRESIÓN ===");
        System.out.println("Filtro de tags: @Regression and not @WIP and not @Blocked");
        System.out.println("Paralelización: Habilitada (4 threads)");
        System.out.println("Timeout por escenario: 5 minutos");
        System.out.println("Reportes: HTML, JSON, JUnit, Timeline");
        System.out.println("Modo estricto: Habilitado");
        System.out.println("====================================");
    }

    /**
     * Validaciones pre-ejecución para pruebas de regresión
     */
    public static void validarConfiguracionRegresion() {
        // Validar que las configuraciones necesarias estén presentes
        String[] propiedadesRequeridas = {
                "selenium.grid.url",
                "app.base.url",
                "database.connection.url"
        };

        for (String propiedad : propiedadesRequeridas) {
            if (System.getProperty(propiedad) == null) {
                System.err.println("ADVERTENCIA: Propiedad no configurada: " + propiedad);
            }
        }

        // Validar directorios de reportes