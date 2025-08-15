package com.evm5.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CrearProductoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/crear-producto.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String nombre = req.getParameter("nombre");
        String precio = req.getParameter("precio");

        // Aquí podrías guardar el producto o mostrar una confirmación
        resp.setContentType("text/html");
        resp.getWriter().println("<h1>Producto creado: " + nombre + " - $" + precio + "</h1>");
    }
}
