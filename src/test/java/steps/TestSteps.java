package steps;

import hooks.Hooks;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.Assert;

public class TestSteps {

    @Given("el usuario abre la página de registro de Selenium para test")
    public void el_usuario_abre_la_página_de_registro_de_selenium_para_test() {
        Hooks.getDriver().get("https://www.selenium.dev/selenium/web/web-form.html");
    }


    @Then("fuerza un fallo para probar captura")
    public void fuerzaFallo() {
        Assert.fail("Fallo intencional para prueba de captura");
    }
}
