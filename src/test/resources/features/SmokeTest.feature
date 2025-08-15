@SmokeTest
Feature: Verificación de carga de la página principal

  Scenario: Página principal se carga correctamente
    Given el usuario accede a la URL de la aplicación
    Then debería ver el título "Swag Labs" en la página
