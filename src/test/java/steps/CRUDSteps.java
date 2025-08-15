package steps;

import hooks.Hooks;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;

public class CRUDSteps {

    WebDriver driver = Hooks.getDriver();


    @Given("el usuario navega al formulario web de Selenium")
    public void navegarAlFormulario() {
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
        driver.get("https://www.selenium.dev/selenium/web/web-form.html");
    }

    @When("el usuario completa el campo de texto con {string}")
    public void completarCampoTexto(String texto) {
        driver.findElement(By.name("my-text")).sendKeys(texto);
    }

    @When("el usuario ingresa la contraseña {string}")
    public void ingresarPassword(String password) {
        driver.findElement(By.name("my-password")).sendKeys(password);
    }

    @When("el usuario escribe {string} en el textarea")
    public void escribirTextarea(String comentario) {
        driver.findElement(By.name("my-textarea")).sendKeys(comentario);
    }


    @When("el usuario marca el checkbox")
    public void marcarCheckbox() {
        WebElement checkbox = driver.findElement(By.name("my-check"));
        if (!checkbox.isSelected()) checkbox.click();
    }


    @When("el usuario elige el color {string}")
    public void elegirColor(String color) {
        driver.findElement(By.name("my-colors")).sendKeys(color);
    }

    @When("el usuario selecciona la fecha {string}")
    public void seleccionarFecha(String fecha) {
        driver.findElement(By.name("my-date")).sendKeys(fecha);
    }

    @When("el usuario mueve el slider al valor {string}")
    public void moverSlider(String valor) {
        WebElement slider = driver.findElement(By.name("my-range"));
        slider.sendKeys(valor);
    }

    @Then("el formulario se envía correctamente")
    public void enviarFormulario() {
        driver.findElement(By.cssSelector("button")).click();
        driver.quit();
    }
}
