package steps;

import hooks.Hooks;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.junit.Assert;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginSteps {

    WebDriver driver = Hooks.getDriver();

    @Given("el usuario está en la página de inicio de sesión")
    public void el_usuario_está_en_la_página_de_inicio_de_sesión() {
        driver.get("https://www.saucedemo.com/");
    }

    @When("ingresa el nombre de usuario {string} y la contraseña {string}")
    public void ingresa_el_nombre_de_usuario_y_la_contraseña(String usuario, String contraseña) {
        driver.findElement(By.id("user-name")).sendKeys(usuario);
        driver.findElement(By.id("password")).sendKeys(contraseña);
    }

    @When("hace clic en el botón de login")
    public void hace_clic_en_el_botón_de_login() {
        driver.findElement(By.id("login-button")).click();
    }

    @Then("debería ver la página principal de productos")
    public void debería_ver_la_página_principal_de_productos() {
        Assert.assertTrue(driver.getCurrentUrl().contains("inventory.html"));
        // NO cierres aquí el driver, déjalo para el @After de Hooks
    }

    @Then("debería ver un mensaje de error")
    public void verMensajeError() {
        Assert.assertTrue(driver.findElement(By.cssSelector("[data-test='error']")).isDisplayed());
    }

    @But("no debería acceder a la página principal")
    public void noAccedePaginaPrincipal() {
        Assert.assertFalse(driver.getCurrentUrl().contains("inventory.html"));
    }

    @Then("debería ver el mensaje {string}")
    public void verMensaje(String mensajeEsperado) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        if (mensajeEsperado.equals("Bienvenido a la tienda")) {
            boolean estaEnPaginaProductos = wait.until(ExpectedConditions.urlContains("inventory.html"));
            Assert.assertTrue("No se redirigió a la página de productos", estaEnPaginaProductos);
        } else {
            var mensajeElemento = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
            String mensajeActual = mensajeElemento.getText();
            Assert.assertEquals(mensajeEsperado, mensajeActual);
        }
    }

}
