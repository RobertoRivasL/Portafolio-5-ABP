# language: es
@Registro @Autenticacion
Característica: Registro de nuevos usuarios en el sistema
  Como visitante del sitio web
  Quiero poder registrarme en el sistema
  Para obtener acceso a las funcionalidades de la aplicación

  # Referencia: HU-002 - Registro de Nuevo Usuario

  Antecedentes:
    Dado que el visitante está en la página de registro
    Y el sistema está disponible para nuevos registros

  @SmokeTest @Positivo
  Escenario: Registro exitoso con datos válidos
    # HU-002: Criterios de aceptación - Formulario con campos obligatorios
    Dado que el visitante completa el formulario de registro con datos válidos:
      | nombre     | Juan Carlos                    |
      | apellido   | Pérez González                 |
      | email      | juan.perez@ejemplo.com         |
      | password   | MiPassword123!                 |
      | confirmar  | MiPassword123!                 |
      | telefono   | +56912345678                   |
    Cuando hace clic en el botón "Registrarse"
    Entonces debe ver el mensaje de éxito "Registro completado exitosamente"
    Y debe recibir un email de confirmación
    Y debe ser redirigido a la página de login
    Y debe ver el mensaje "Por favor, confirme su email para activar su cuenta"

  @Validacion @Negativo
  Esquema del escenario: Validación de campos obligatorios
    # HU-002: Validación de campos requeridos
    Dado que el visitante completa el formulario excepto el campo "<campo_faltante>"
    Cuando hace clic en el botón "Registrarse"
    Entonces debe ver el mensaje de error "<mensaje_error>"
    Y debe permanecer en la página de registro

    Ejemplos:
      | campo_faltante | mensaje_error                           |
      | nombre         | El nombre es obligatorio                |
      | apellido       | El apellido es obligatorio              |
      | email          | El email es obligatorio                 |
      | password       | La contraseña es obligatoria           |
      | confirmar      | La confirmación de contraseña es obligatoria |

  @Validacion @Negativo
  Esquema del escenario: Validación de formato de email
    # HU-002: Criterio de aceptación - Validación de formato de email
    Dado que el visitante ingresa el email "<email_invalido>"
    Y completa el resto de campos correctamente
    Cuando hace clic en el botón "Registrarse"
    Entonces debe ver el mensaje de error "Formato de email inválido"

    Ejemplos:
      | email_invalido    |
      | email-invalido    |
      | @dominio.com      |
      | usuario@          |
      | usuario.dominio   |
      | usuario@@test.com |

  @Seguridad @Validacion
  Esquema del escenario: Validación de fortaleza de contraseña
    # HU-002: Requisito de seguridad - Contraseñas seguras
    Dado que el visitante ingresa la contraseña "<password_debil>"
    Y completa el resto de campos correctamente
    Cuando hace clic en el botón "Registrarse"
    Entonces debe ver el mensaje de error "<mensaje_error>"

    Ejemplos:
      | password_debil | mensaje_error                                          |
      | 123            | La contraseña debe tener al menos 8 caracteres        |
      | password       | La contraseña debe contener al menos un número        |
      | PASSWORD123    | La contraseña debe contener al menos una minúscula    |
      | password123    | La contraseña debe contener al menos una mayúscula    |
      | Password123    | La contraseña debe contener al menos un carácter especial |

  @Validacion @Negativo
  Escenario: Confirmación de contraseña no coincide
    # HU-002: Criterio de aceptación - Confirmación de contraseña
    Dado que el visitante ingresa la contraseña "MiPassword123!"
    Y ingresa la confirmación "OtraPassword456!"
    Y completa el resto de campos correctamente
    Cuando hace clic en el botón "Registrarse"
    Entonces debe ver el mensaje de error "Las contraseñas no coinciden"
    Y los campos de contraseña deben quedar vacíos

  @BusinessRule @Negativo
  Escenario: Prevención de usuarios duplicados
    # HU-002: Criterio de aceptación - Prevención de usuarios duplicados
    Dado que ya existe un usuario registrado con el email "usuario.existente@test.com"
    Cuando un visitante intenta registrarse con el mismo email
    Entonces debe ver el mensaje de error "Ya existe una cuenta con este email"
    Y debe ver un enlace "¿Olvidaste tu contraseña?"
    Y debe permanecer en la página de registro

  @UX @Positivo
  Escenario: Indicador de fortaleza de contraseña en tiempo real
    # HU-002: Mejora de experiencia de usuario
    Dado que el visitante está completando el campo de contraseña
    Cuando ingresa "<password_parcial>"
    Entonces debe ver el indicador de fortaleza "<nivel_fortaleza>"
    Y debe ver las reglas de contraseña restantes "<reglas_pendientes>"

    Ejemplos:
      | password_parcial | nivel_fortaleza | reglas_pendientes                    |
      | pass             | Débil           | Mínimo 8 caracteres, números, mayúsculas, especiales |
      | password         | Débil           | Números, mayúsculas, caracteres especiales |
      | Password         | Media           | Números, caracteres especiales       |
      | Password1        | Media           | Caracteres especiales                |
      | Password1!       | Fuerte          | ¡Contraseña segura!                 |

  @Integracion @Positivo
  Escenario: Verificación por email después del registro
    # HU-002: Flujo completo de registro y verificación
    Dado que el usuario se registró exitosamente con "nuevo.usuario@test.com"
    Cuando recibe el email de verificación
    Y hace clic en el enlace de verificación
    Entonces debe ser redirigido a la página de confirmación
    Y debe ver el mensaje "Email verificado exitosamente"
    Y debe poder hacer login con sus credenciales
    Y su cuenta debe estar activa en el sistema

  @Accesibilidad @UX
  Escenario: Navegación con teclado en formulario de registro
    # HU-002: Requisito de accesibilidad
    Dado que el visitante usa navegación por teclado
    Cuando presiona Tab secuencialmente
    Entonces debe poder navegar en el siguiente orden:
      | campo                    | orden |
      | Nombre                   | 1     |
      | Apellido                 | 2     |
      | Email                    | 3     |
      | Teléfono                 | 4     |
      | Contraseña              | 5     |
      | Confirmar contraseña    | 6     |
      | Acepto términos         | 7     |
      | Botón registrarse       | 8     |
    Y cada campo debe mostrar claramente el foco visual

  @Performance @SmokeTest
  Escenario: Tiempo de respuesta del registro
    # HU-002: Requisito no funcional - Performance
    Dado que el visitante completa correctamente el formulario de registro
    Cuando hace clic en "Registrarse"
    Entonces el sistema debe procesar la solicitud en menos de 3 segundos
    Y debe mostrar la confirmación en menos de 5 segundos totales

  @Regression @DataIntegrity
  Escenario: Integridad de datos después del registro
    # HU-002: Verificación de integridad de datos
    Dado que un usuario se registra con los siguientes datos:
      | nombre     | María José                     |
      | apellido   | Silva Rodríguez                |
      | email      | maria.silva@empresa.cl         |
      | telefono   | +56987654321                   |
    Cuando el registro se completa exitosamente
    Entonces los datos deben estar correctamente almacenados en la base de datos
    Y la contraseña debe estar encriptada
    Y la fecha de registro debe ser la fecha actual
    Y el estado de la cuenta debe ser "Pendiente de verificación"

  @Seguridad @Edge
  Escenario: Protección contra ataques de fuerza bruta en registro
    # HU-002: Medida de seguridad adicional
    Dado que se han realizado más de 5 intentos de registro desde la misma IP en 10 minutos
    Cuando se intenta realizar otro registro
    Entonces debe aparecer un captcha de verificación
    Y debe mostrar el mensaje "Verificación adicional requerida"
    Y debe requerir completar el captcha antes de proceder