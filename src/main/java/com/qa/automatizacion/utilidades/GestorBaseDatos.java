package com.qa.automatizacion.utilidades;

public class GestorBaseDatos {
    private static GestorBaseDatos instancia;

    private GestorBaseDatos() {}

    public static synchronized GestorBaseDatos obtenerInstancia() {
        if (instancia == null) {
            instancia = new GestorBaseDatos();
        }
        return instancia;
    }

    // Métodos para limpiar datos de prueba, insertar datos, etc.
    public void limpiarDatosPrueba() {
        // Implementar limpieza de BD si es necesario
    }
}