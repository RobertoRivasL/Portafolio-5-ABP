@Login
Feature: Inicio de sesión en la aplicación

  Scenario: Usuario inicia sesión con credenciales válidas
    Given el usuario está en la página de inicio de sesión
    When ingresa el nombre de usuario "standard_user" y la contraseña "secret_sauce"
    And hace clic en el botón de login
    Then debería ver la página principal de productos

  Scenario: Usuario ingresa contraseña incorrecta
    Given el usuario está en la página de inicio de sesión
    When ingresa el nombre de usuario "standard_user" y la contraseña "wrong_password"
    And hace clic en el botón de login
    Then debería ver un mensaje de error
    But no debería acceder a la página principal

  Scenario Outline: Usuario intenta iniciar sesión con diferentes credenciales
    Given el usuario está en la página de inicio de sesión
    When ingresa el nombre de usuario "<usuario>" y la contraseña "<contraseña>"
    And hace clic en el botón de login
    Then debería ver el mensaje "<mensaje>"

    Examples:
      | usuario        | contraseña     | mensaje                                                       |
      | standard_user  | secret_sauce   | Bienvenido a la tienda                                        |
      | locked_user    | secret_sauce   | Epic sadface: Username and password do not match any user in this service |
      | standard_user  | wrong_password | Epic sadface: Username and password do not match any user in this service |
