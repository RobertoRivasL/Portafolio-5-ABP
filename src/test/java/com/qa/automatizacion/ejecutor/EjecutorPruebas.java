package com.qa.automatizacion.ejecutor;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Ejecutor principal para las pruebas BDD con Cucumber.
 * Configura la ejecución de los archivos .feature y los reportes.
 *
 * Principios aplicados:
 * - Separación de Intereses: Se enfoca únicamente en la configuración de ejecución
 * - Configuración Centralizada: Todas las opciones de ejecución en un solo lugar
 * - Flexibilidad: Permite diferentes tipos de ejecución mediante configuración
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
        key = Constants.GLUE_PROPERTY_NAME,
        value = "com.qa.automatizacion.pasos,com.qa.automatizacion.hooks"
)
@ConfigurationParameter(
        key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty," +
                "html:reportes/html/cucumber-report.html," +
                "json:reportes/json/cucumber-report.json," +
                "junit:reportes/junit/cucumber-report.xml," +
                "timeline:reportes/timeline"
)
@ConfigurationParameter(
        key = Constants.FEATURES_PROPERTY_NAME,
        value = "src/test/resources/features"
)
@ConfigurationParameter(
        key = Constants.FILTER_TAGS_PROPERTY_NAME,
        value = "not @WIP and not @Ignore"
)
@ConfigurationParameter(
        key = Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME,
        value = "false"
)
@ConfigurationParameter(
        key = Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME,
        value = "true"
)
@ConfigurationParameter(
        key = Constants.EXECUTION_DRY_RUN_PROPERTY_NAME,
        value = "false"
)
@ConfigurationParameter(
        key = Constants.EXECUTION_STRICT_PROPERTY_NAME,
        value = "true"
)
@ConfigurationParameter(
        key = Constants.JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME,
        value = "long"
)
public class EjecutorPruebas {

    /*
     * Esta clase no necesita código adicional.
     * La configuración se realiza completamente mediante anotaciones.
     *
     * Configuraciones incluidas:
     *
     * 1. GLUE: Paquetes donde están los Step Definitions y Hooks
     *    - com.qa.automatizacion.pasos: Contiene todas las Step Definitions
     *    - com.qa.automatizacion.hooks: Contiene los Hooks de configuración
     *
     * 2. PLUGINS: Formatos de reporte generados
     *    - pretty: Salida colorida en consola
     *    - html: Reporte HTML interactivo
     *    - json: Reporte JSON para integraciones
     *    - junit: Reporte XML compatible con JUnit
     *    - timeline: Reporte de línea de tiempo
     *
     * 3. FEATURES: Ubicación de los archivos .feature
     *    - src/test/resources/features: Directorio estándar de Maven
     *
     * 4. FILTER_TAGS: Tags que determinan qué escenarios ejecutar
     *    - not @WIP: Excluye escenarios en desarrollo
     *    - not @Ignore: Excluye escenarios marcados para ignorar
     *
     * 5. CONFIGURACIONES ADICIONALES:
     *    - publish: Deshabilitado para evitar publicación automática
     *    - dry-run: Deshabilitado para ejecución real
     *    - strict: Habilitado para fallar en pasos no definidos
     *    - naming-strategy: Nombres largos para mejor identificación
     *
     * EJECUCIÓN:
     *
     * Para ejecutar todas las pruebas:
     * mvn clean test
     *
     * Para ejecutar solo pruebas de smoke:
     * mvn clean test -Dcucumber.filter.tags="@SmokeTest"
     *
     * Para ejecutar pruebas específicas:
     * mvn clean test -Dcucumber.filter.tags="@Login"
     *
     * Para ejecutar excluyendo ciertas pruebas:
     * mvn clean test -Dcucumber.filter.tags="not @Slow"
     *
     * Para combinar tags:
     * mvn clean test -Dcucumber.filter.tags="@SmokeTest and @Login"
     * mvn clean test -Dcucumber.filter.tags="@Regression or @CRUD"
     *
     * REPORTES:
     *
     * Los reportes se generan en el directorio 'reportes/':
     * - reportes/html/cucumber-report.html: Reporte HTML principal
     * - reportes/json/cucumber-report.json: Datos en formato JSON
     * - reportes/junit/cucumber-report.xml: Compatible con CI/CD
     * - reportes/timeline/: Visualización temporal de la ejecución
     *
     * TRAZABILIDAD:
     *
     * La trazabilidad con historias de usuario se maneja a través de:
     * - Tags en los archivos .feature que referencian HU-XXX
     * - HelperTrazabilidad que registra la ejecución
     * - Reportes que muestran la cobertura de historias de usuario
     *
     * CONFIGURACIÓN AVANZADA:
     *
     * Para modificar la configuración en tiempo de ejecución:
     *
     * System.setProperty("cucumber.filter.tags", "@SmokeTest");
     * System.setProperty("cucumber.plugin", "pretty,html:custom-report.html");
     *
     * INTEGRACIÓN CON CI/CD:
     *
     * El formato JUnit XML permite integración con:
     * - Jenkins
     * - GitLab CI
     * - GitHub Actions
     * - Azure DevOps
     * - Otros sistemas de CI/CD
     *
     * PARALELIZACIÓN:
     *
     * Para ejecución en paralelo, configurar en pom.xml:
     * <configuration>
     *   <parallel>methods</parallel>
     *   <threadCount>4</threadCount>
     * </configuration>
     */
}