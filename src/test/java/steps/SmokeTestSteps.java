package steps;

import hooks.Hooks;
import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.junit.Assert;

public class SmokeTestSteps {

    WebDriver driver = Hooks.getDriver();

    @Given("el usuario accede a la URL de la aplicación")
    public void el_usuario_accede_a_la_url_de_la_aplicación() {
        driver.get("https://www.saucedemo.com/");
    }

    @Then("debería ver el título {string} en la página")
    public void debería_ver_el_título_en_la_página(String tituloEsperado) {
        String tituloActual = driver.getTitle();
        Assert.assertEquals(tituloEsperado, tituloActual);
        // No cerrar el driver aquí
    }
}
