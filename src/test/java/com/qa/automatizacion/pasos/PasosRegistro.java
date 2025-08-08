package com.qa.automatizacion.pasos;

import com.qa.automatizacion.modelo.Usuario;
import com.qa.automatizacion.paginas.PaginaRegistro;
import com.qa.automatizacion.utilidades.Utileria;
import io.cucumber.java.es.Dado;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasosRegistro {

    private final Utileria utileria;           // Una sola instancia
    private final PaginaRegistro paginaRegistro;
    private Usuario usuarioActual;

    public PasosRegistro() {
        this.utileria = Utileria.obtenerInstancia(); // Singleton
        this.paginaRegistro = new PaginaRegistro();
    }

    @Dado("que el usuario está en la página de registro")
    public void elUsuarioEstaEnLaPaginaDeRegistro() {
        utileria.registrarPaso("HU-004", "Navegación a página de registro");

        paginaRegistro.navegarAPagina(); // Método heredado de PaginaBase

        assertTrue(paginaRegistro.esPaginaCargada(),
                "Página de registro no cargó");
    }
}