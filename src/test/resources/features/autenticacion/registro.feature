# language: es
@Registro @Autenticacion
Característica: Registro de nuevos usuarios en el sistema
  Como visitante del sitio web
  Quiero poder registrarme en el sistema
  Para obtener acceso a las funcionalidades del sistema

  # Referencia: HU-002 - Registro de Usuario

  Antecedentes:
    Dado que el usuario está en la página de registro
    Y el sistema está funcionando correctamente

  @SmokeTest @Positivo
  Escenario: Registro exitoso con datos válidos
    # HU-002: Criterio de aceptación - El usuario puede registrarse con datos válidos
    Dado que el usuario tiene datos de registro válidos
      | nombre    | email           | password    | confirmarPassword |
      | Juan Test | juan@test.com   | password123 | password123       |
    Cuando el usuario completa el formulario de registro
    Y hace clic en el botón "Registrarse"
    Entonces debe ver el mensaje de éxito "Usuario registrado exitosamente"
    Y debe ser redirigido a la página de confirmación
    Y debe recibir un email de verificación

  @Negativo @Validacion
  Escenario: Intento de registro con campos vacíos
    # HU-002: Validación de campos obligatorios
    Cuando el usuario hace clic en el botón "Registrarse" sin llenar los campos
    Entonces debe ver el mensaje de error "Por favor, complete todos los campos obligatorios"
    Y debe ver indicadores de error en los campos vacíos
    Y el botón "Registrarse" debe permanecer deshabilitado

  @Negativo @Validacion
  Esquema del escenario: Validación de formato de email en registro
    # HU-002: Validación de formato de email
    Dado que el usuario ingresa los siguientes datos de registro:
      | nombre    | email     | password    | confirmarPassword |
      | Juan Test | <email>   | password123 | password123       |
    Cuando hace clic en el botón "Registrarse"
    Entonces debe ver el mensaje de error "<mensaje_error>"
    Y debe permanecer en la página de registro

    Ejemplos:
      | email           | mensaje_error                    |
      | invalidemail    | Formato de email inválido        |
      | @test.com       | Formato de email inválido        |
      | test@           | Formato de email inválido        |
      | test.com        | Formato de email inválido        |
      | test@.com       | Formato de email inválido        |

  @Negativo @Validacion
  Esquema del escenario: Validación de fortaleza de contraseña
    # HU-002: Criterio de aceptación - Validación de contraseña segura
    Dado que el usuario ingresa los siguientes datos de registro:
      | nombre    | email         | password   | confirmarPassword |
      | Juan Test | juan@test.com | <password> | <password>        |
    Cuando hace clic en el botón "Registrarse"
    Entonces debe ver el mensaje de error "<mensaje_error>"
    Y debe ver el indicador de fortaleza "<nivel_fortaleza>"

    Ejemplos:
      | password | mensaje_error                               | nivel_fortaleza |
      | 123      | La contraseña debe tener al menos 8 caracteres | Muy débil       |
      | password | La contraseña debe incluir números y mayúsculas | Débil           |
      | Password | La contraseña debe incluir números             | Moderada        |
      | 12345678 | La contraseña debe incluir letras              | Débil           |

  @Negativo @Validacion
  Escenario: Confirmación de contraseña no coincide
    # HU-002: Validación de confirmación de contraseña
    Dado que el usuario ingresa los siguientes datos de registro:
      | nombre    | email         | password    | confirmarPassword |
      | Juan Test | juan@test.com | password123 | password456       |
    Cuando hace clic en el botón "Registrarse"
    Entonces debe ver el mensaje de error "Las contraseñas no coinciden"
    Y el campo "Confirmar Contraseña" debe estar resaltado en rojo
    Y debe permanecer en la página de registro

  @Negativo @BusinessRule
  Escenario: Prevención de usuarios duplicados
    # HU-002: Criterio de aceptación - No permitir emails duplicados
    Dado que ya existe un usuario registrado con email "juan@test.com"
    Y el usuario intenta registrarse con los datos:
      | nombre     | email         | password    | confirmarPassword |
      | Juan Nuevo | juan@test.com | password123 | password123       |
    Cuando hace clic en el botón "Registrarse"
    Entonces debe ver el mensaje de error "Este email ya está registrado"
    Y debe ver el enlace "¿Ya tienes cuenta? Inicia sesión"
    Y debe permanecer en la página de registro

  @Positivo @UX
  Escenario: Indicador de fortaleza de contraseña en tiempo real
    # HU-002: Funcionalidad adicional - Feedback visual de contraseña
    Dado que el usuario está en la página de registro
    Cuando ingresa progresivamente una contraseña
      | contraseña  | fortaleza_esperada |
      | a           | Muy débil          |
      | abc123      | Débil              |
      | Abc123      | Moderada           |
      | Abc123!     | Fuerte             |
      | Abc123!@#   | Muy fuerte         |
    Entonces debe ver el indicador de fortaleza actualizarse en tiempo real
    Y debe ver consejos para mejorar la contraseña cuando sea necesario

  @Positivo @Integration
  Escenario: Verificación por email después del registro
    # HU-002: Criterio de aceptación - Proceso de verificación
    Dado que el usuario se ha registrado exitosamente con email "juan@test.com"
    Cuando revisa su bandeja de entrada
    Entonces debe recibir un email con asunto "Verifica tu cuenta"
    Y el email debe contener un enlace de verificación válido
    Y al hacer clic en el enlace debe activar su cuenta
    Y debe poder hacer login con sus credenciales

  @Positivo @Accessibility
  Escenario: Navegación con teclado en formulario de registro
    # HU-002: Accesibilidad - Navegación con Tab
    Dado que el usuario está en la página de registro
    Cuando usa la tecla Tab para navegar entre los campos
    Entonces debe poder acceder a todos los campos en orden lógico:
      | orden | campo               |
      | 1     | Nombre              |
      | 2     | Email               |
      | 3     | Contraseña          |
      | 4     | Confirmar Contraseña|
      | 5     | Términos y Condiciones |
      | 6     | Botón Registrarse   |
    Y cada campo debe tener indicadores visuales claros de foco

  @Performance @SmokeTest
  Escenario: Tiempo de respuesta del registro
    # HU-002: Requerimiento no funcional - Performance
    Dado que el usuario tiene datos de registro válidos
    Cuando completa el proceso de registro
    Entonces el sistema debe responder en menos de 3 segundos
    Y debe mostrar indicadores de carga mientras procesa
    Y debe confirmar el registro exitoso

  @Integration @DataIntegrity
  Escenario: Integridad de datos después del registro
    # HU-002: Validación de integridad de datos
    Dado que el usuario se registra con datos válidos:
      | nombre    | email         | password    |
      | Juan Test | juan@test.com | password123 |
    Cuando el registro se completa exitosamente
    Entonces los datos del usuario deben estar correctamente almacenados en la base de datos
    Y la contraseña debe estar encriptada de forma segura
    Y debe existir un registro de auditoría de la creación del usuario
    Y el usuario debe aparecer en la lista de usuarios pendientes de verificación

  @Security @Negativo
  Escenario: Protección contra ataques de fuerza bruta en registro
    # HU-002: Medida de seguridad - Rate limiting
    Dado que se han realizado 5 intentos de registro fallidos desde la misma IP
    Cuando se intenta un sexto registro
    Entonces debe mostrar el mensaje "Demasiados intentos de registro. Intente más tarde"
    Y debe bloquear los intentos de registro por 15 minutos
    Y debe registrar el evento de seguridad en los logs

  @Regression @E2E
  Escenario: Flujo completo de registro e inicio de sesión
    # HU-002: Escenario de regresión - Flujo end-to-end
    Dado que el usuario es un visitante nuevo del sistema
    Cuando completa el proceso de registro con datos válidos
    Y verifica su email
    Y hace login con sus nuevas credenciales
    Entonces debe acceder exitosamente al dashboard
    Y debe ver un mensaje de bienvenida personalizado
    Y debe tener todos los permisos de usuario estándar