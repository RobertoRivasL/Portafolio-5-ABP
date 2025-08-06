# language: es
@Login @Autenticacion
Característica: Autenticación de usuarios en el sistema
  Como usuario registrado del sistema
  Quiero poder iniciar sesión con mis credenciales válidas
  Para acceder a las funcionalidades del sistema

  # Referencia: HU-001 - Autenticación de Usuario

  Antecedentes:
    Dado que el usuario está en la página de login
    Y el sistema está funcionando correctamente

  @SmokeTest @Positivo
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

  @Negativo @ErrorHandling
  Escenario: Login fallido con credenciales inválidas
    # HU-001: Criterio de aceptación - Se muestra mensaje de error para credenciales inválidas
    Dado que el usuario tiene credenciales inválidas
      | email           | password    |
      | invalid@test.com| wrongpass   |
    Cuando el usuario ingresa sus credenciales
    Y hace clic en el botón "Iniciar Sesión"
    Entonces debe ver el mensaje de error "Credenciales inválidas"
    Y debe permanecer en la página de login
    Y los campos de entrada deben estar vacíos

  @Negativo @Validacion
  Escenario: Intento de login con campos vacíos
    # HU-001: Validación de campos obligatorios
    Cuando el usuario hace clic en el botón "Iniciar Sesión" sin llenar los campos
    Entonces debe ver el mensaje de error "Por favor, complete todos los campos"
    Y el botón "Iniciar Sesión" debe permanecer deshabilitado

  @Negativo @Validacion
  Esquema del escenario: Validación de formato de email
    # HU-001: Validación de formato de email
    Dado que el usuario ingresa un email con formato "<email>"
    Y ingresa una contraseña "password123"
    Cuando hace clic en el botón "Iniciar Sesión"
    Entonces debe ver el mensaje de error "<mensaje_error>"

    Ejemplos:
      | email           | mensaje_error                    |
      | invalidemail    | Formato de email inválido        |
      | @test.com       | Formato de email inválido        |
      | test@           | Formato de email inválido        |
      | test.com        | Formato de email inválido        |

  @Seguridad @Negativo
  Escenario: Bloqueo temporal después de múltiples intentos fallidos
    # HU-001: Medida de seguridad - Bloqueo por intentos fallidos
    Dado que el usuario ha fallado el login 3 veces consecutivas
    Cuando intenta hacer login nuevamente
    Entonces debe ver el mensaje "Cuenta temporalmente bloqueada. Intente en 15 minutos"
    Y no debe poder ingresar credenciales por 15 minutos

  @Funcionalidad @Positivo
  Escenario: Funcionalidad de "Recordar sesión"
    # HU-001: Funcionalidad adicional - Recordar sesión
    Dado que el usuario tiene credenciales válidas
    Y marca la opción "Recordar sesión"
    Cuando hace login exitosamente
    Y cierra el navegador
    Y vuelve a abrir la aplicación
    Entonces debe estar automáticamente logueado
    Y debe ver el dashboard sin necesidad de ingresar credenciales

  @Accesibilidad @UX
  Escenario: Navegación con teclado en el formulario de login
    # HU-001: Requisito de accesibilidad
    Dado que el usuario está en la página de login
    Cuando usa la tecla Tab para navegar entre los campos
    Entonces debe poder moverse secuencialmente entre:
      | campo                | orden |
      | Campo email          | 1     |
      | Campo contraseña     | 2     |
      | Checkbox recordar    | 3     |
      | Botón iniciar sesión | 4     |
    Y cada campo debe tener el foco visual claramente visible

  @Performance @SmokeTest
  Escenario: Tiempo de respuesta del login
    # HU-001: Requisito no funcional - Performance
    Dado que el usuario tiene credenciales válidas
    Cuando hace login
    Entonces el sistema debe responder en menos de 2 segundos
    Y debe ser redirigido al dashboard en menos de 3 segundos totales

  @Regression @Integracion
  Escenario: Login después de cambio de contraseña
    # HU-001: Escenario de integración con gestión de usuarios
    Dado que el usuario cambió su contraseña recientemente
    Y la nueva contraseña es "nuevaPassword456"
    Cuando intenta hacer login con la contraseña anterior "password123"
    Entonces debe ver el mensaje de error "Credenciales inválidas"
    Cuando hace login con la nueva contraseña "nuevaPassword456"
    Entonces debe acceder exitosamente al sistema