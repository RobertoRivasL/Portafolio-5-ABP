@CRUD
Feature: Interacción con el formulario web de Selenium

  Background:
    Given el usuario navega al formulario web de Selenium

  @SmokeTest
  Scenario: Completar y enviar el formulario sin subir archivos
    When el usuario completa el campo de texto con "Dante"
    And el usuario ingresa la contraseña "123456"
    And el usuario escribe "Este es un comentario" en el textarea
    And el usuario marca el checkbox
    And el usuario elige el color "#ff0000"
    And el usuario selecciona la fecha "2025-08-08"
    And el usuario mueve el slider al valor "7"
    Then el formulario se envía correctamente
