package com.qa.automatizacion.pasos;

import com.qa.automatizacion.contexto.ContextoPruebas;
import com.qa.automatizacion.configuracion.ConfiguradorNavegador;
import io.cucumber.java.es.*;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step Definitions comunes para todas las funcionalidades.
 * Contiene pasos que se reutilizan en múltiples escenarios.
 *
 * Principios aplicados:
 * - DRY: Evita repetición de pasos comunes
 * - Single Responsibility: Se enfoca en pasos de navegación y verificación general
 * - Dependency Injection: Utiliza el contexto compartido
 *
 * @author Equipo QA Automatización
 * @version 1.0
 */
public class PasosComunes {

    private static final Logger logger = LoggerFactory.getLogger(PasosComunes.class);

    private final ContextoPruebas contexto;
    private WebDriver driver;

    /**
     * Constructor que inicializa el contexto compartido
     */
    public PasosComunes() {
        this.contexto = ContextoPruebas.obtenerInstancia();
        logger.debug("PasosComunes inicializados");
    }

    // Given - Condiciones iniciales

    @Dado("que estoy en la página principal de la aplicación")
    public void queEstoyEnLaPaginaPrincipalDeLaAplicacion() {
        driver = ConfiguradorNavegador.obtenerDriver();
        String urlPrincipal = "http://localhost:8080"; // URL configurable

        try {
            driver.get(urlPrincipal);
            contexto.almacenarDato("urlActual", urlPrincipal);
            logger.info("Navegado a página principal: {}", urlPrincipal);
        } catch (Exception e) {
            logger.error("Error navegando a página principal: {}", e.getMessage());
            fail("No se pudo cargar la página principal");
        }
    }
}