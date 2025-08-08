# language: es

@Login @Autenticacion
Característica: Autenticación de usuarios en el sistema
  Como usuario del sistema
  Quiero poder autenticarme con mis credenciales
  Para acceder a las funcionalidades del sistema

  # Referencia: HU-001 - Autenticación exitosa de usuarios
  # Criterios de aceptación:
  # - El usuario puede ingresar email y contraseña válidos
  # - El sistema valida las credenciales correctamente
  # - El usuario es redirigido al dashboard tras login exitoso
  # - Se muestra mensaje de bienvenida personalizado

  Antecedentes:
    Dado que el usuario está en la página de login
    Y el sistema está funcionando correctamente
    Y el formulario está habilitado
    Y no hay errores previos en la página
    Y el título de la página es correcto

  @SmokeTest @Positivo @HU-001
  Escenario: Login exitoso con credenciales válidas
    # HU-001: Criterio de aceptación - El usuario puede ingresar email y contraseña válidos
    Dado que el usuario tiene credenciales válidas
      | email           | password    |
      | test@test.com   | password123 |
    Cuando el usuario ingresa sus credenciales
    Y hace clic en el botón "Iniciar Sesión"
    Entonces el usuario debe ser redirigido al dashboard
    Y debe ver el mensaje de bienvenida "Bienvenido al sistema"
    Y debe ver su nombre de usuario en la barra superior

  @Regression @Positivo @HU-003
  Escenario: Login exitoso con opción recordar sesión
    # HU-003: Criterio de aceptación - El usuario puede optar por recordar su sesión
    Dado que el usuario tiene credenciales válidas
      | email           | password    |
      | test@test.com   | password123 |
    Y que el usuario quiere recordar su sesión
    Cuando el usuario ingresa sus credenciales
    Y selecciona recordar sesión
    Y hace clic en el botón "Iniciar Sesión"
    Entonces el usuario debe ser redirigido al dashboard
    Y debe ver el mensaje de bienvenida "Bienvenido al sistema"
    Y el checkbox de recordar sesión debe estar marcado

  @Regression @Negativo @HU-002
  Escenario: Login fallido con credenciales inválidas
    # HU-002: Criterio de aceptación - El sistema rechaza credenciales inválidas
    Dado que el usuario tiene credenciales inválidas
      | email               | password        |
      | invalid@invalid.com | wrongpassword   |
    Cuando intenta realizar login
    Entonces debe ver un mensaje de error
    Y debe permanecer en la página de login
    Y los campos deben estar vacíos

  @Regression @Negativo @HU-002
  Esquema del escenario: Validación de diferentes tipos de credenciales inválidas
    # HU-002: Criterio de aceptación - Validación de múltiples casos de error
    Dado que el usuario está en la página de login
    Cuando ingresa el email "<email>"
    Y ingresa la contraseña "<password>"
    Y hace clic en el botón "Iniciar Sesión"
    Entonces debe ver el mensaje "<mensaje_error>"
    Y debe permanecer en la página de login

    Ejemplos:
      | email                 | password    | mensaje_error                    |
      |                       | password123 | El email es obligatorio          |
      | test@test.com         |             | La contraseña es obligatoria     |
      | email_invalido        | password123 | Formato de email inválido        |
      | test@test.com         | 123         | Contraseña muy corta             |
      | usuario@noexiste.com  | password123 | Credenciales incorrectas         |

  @Edge @Negativo @HU-002
  Escenario: Intento de login con campos vacíos
    # HU-002: Criterio de aceptación - Validación de campos obligatorios
    Dado que el usuario está en la página de login
    Cuando hace clic en el botón "Iniciar Sesión"
    Entonces debe ver un mensaje de error
    Y debe ver el mensaje "Todos los campos son obligatorios"
    Y debe permanecer en la página de login

  @Security @Negativo @HU-002
  Escenario: Intento de login con inyección SQL
    # HU-002: Criterio de aceptación - Protección contra ataques de inyección
    Dado que el usuario está en la página de login
    Cuando ingresa el email "admin@test.com' OR '1'='1"
    Y ingresa la contraseña "' OR '1'='1"
    Y hace clic en el botón "Iniciar Sesión"
    Entonces debe ver un mensaje de error
    Y debe ver el mensaje "Credenciales incorrectas"
    Y debe permanecer en la página de login

  @Performance @SmokeTest @HU-001
  Escenario: Login exitoso en tiempo razonable
    # HU-001: Criterio de aceptación - El proceso de login debe ser eficiente
    Dado que el usuario tiene credenciales válidas
      | email           | password    |
      | test@test.com   | password123 |
    Cuando el usuario ingresa sus credenciales
    Y hace clic en el botón "Iniciar Sesión"
    Y espera 5 segundos
    Entonces el usuario debe ser redirigido al dashboard
    Y debe ver el mensaje de bienvenida "Bienvenido al sistema"

  @UI @Regression @HU-001
  Escenario: Navegación a página de registro desde login
    # HU-001: Criterio de aceptación - Enlaces de navegación funcionan correctamente
    Dado que el usuario está en la página de login
    Cuando hace clic en el botón "Registrarse"
    Entonces debe ser redirigido a la página de registro
    Y debe ver el formulario de registro

  @UI @Regression @HU-001
  Escenario: Navegación a recuperación de contraseña
    # HU-001: Criterio de aceptación - Funcionalidad de recuperar contraseña disponible
    Dado que el usuario está en la página de login
    Cuando hace clic en el botón "¿Olvidaste tu contraseña?"
    Entonces debe ser redirigido a la página de recuperación
    Y debe ver el formulario de recuperación de contraseña