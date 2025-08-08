package com.qa.automatizacion.paginas;

import com.qa.automatizacion.paginas.PaginaBase;
import org.openqa.selenium.By;

public class PaginaRegistro extends PaginaBase {

    private static final By CAMPO_NOMBRE = By.id("nombre");
    private static final By CAMPO_EMAIL = By.id("email");
    private static final By CAMPO_PASSWORD = By.id("password");
    private static final By BOTON_REGISTRAR = By.id("registrar");

    // Constructor
    public PaginaRegistro() {
        super(); // Hereda toda la funcionalidad de PaginaBase
    }

    // Implementar métodos abstractos
    @Override
    public boolean esPaginaCargada() {
        return esElementoPresente(CAMPO_NOMBRE) &&
                esElementoPresente(BOTON_REGISTRAR);
    }

    @Override
    public String obtenerTituloEsperado() {
        return "Registro";
    }

    @Override
    public String obtenerUrlEsperada() {
        return utileria.obtenerUrlRegistro(); // Acceso directo a Utileria
    }

    // Métodos de negocio
    public void realizarRegistro(String nombre, String email, String password) {
        registrarAccion("Iniciando registro"); // Logging automático

        ingresarTextoSeguro(CAMPO_NOMBRE, nombre);    // Método de PaginaBase
        ingresarTextoSeguro(CAMPO_EMAIL, email);      // Que usa Utileria
        ingresarTextoSeguro(CAMPO_PASSWORD, password);

        hacerClick(BOTON_REGISTRAR); // Manejo de errores automático
    }
}