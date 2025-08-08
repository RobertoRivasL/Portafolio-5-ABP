package com.qa.automatizacion.ejecutor;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

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
        key = Constants.EXECUTION_DRY_RUN_PROPERTY_NAME,
        value = "false"
)

public class EjecutorPruebas {
    // No requiere código adicional, solo anotaciones.
}