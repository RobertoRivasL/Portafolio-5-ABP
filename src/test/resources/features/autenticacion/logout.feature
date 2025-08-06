# language: es

@Logout @Autenticacion
Característica: Cerrar sesión del sistema
  Como usuario autenticado del sistema
  Quiero poder cerrar mi sesión de forma segura
  Para proteger mi información personal y evitar accesos no autorizados

  # Referencia: HU-003 - Cierre seguro de sesión

  Antecedentes:
    Dado que el sistema está funcionando correctamente
    Y que existe un usuario registrado con email "usuario@test.com" y password "password123"
    Y que el usuario ha iniciado sesión exitosamente

  @SmokeTest @Positivo
  Escenario: Logout exitoso desde el dashboard
    # HU-003: El usuario puede cerrar sesión desde cualquier página del sistema
    Dado que el usuario está en el dashboard principal
    Y que puede ver su nombre de usuario en la barra superior
    Cuando el usuario hace clic en el menú de usuario
    Y selecciona la opción "Cerrar Sesión"
    Entonces el usuario debe ser redirigido a la página de login
    Y debe ver el mensaje "Sesión cerrada exitosamente"
    Y no debe poder acceder a páginas protegidas sin autenticarse nuevamente
    Y la sesión debe estar completamente terminada

  @Regression @Positivo
  Escenario: Logout desde diferentes páginas del sistema
    # HU-003: El logout debe funcionar desde cualquier página
    Dado que el usuario está en la página de "productos"
    Cuando el usuario cierra su sesión
    Entonces debe ser redirigido a la página de login
    Y la sesión debe estar terminada

  @Regression @Positivo
  Escenario: Verificación de limpieza de sesión después del logout
    # HU-003: La sesión debe limpiarse completamente
    Dado que el usuario está en el dashboard
    Cuando el usuario cierra su sesión
    Y intenta navegar directamente a "/dashboard"
    Entonces debe ser redirigido automáticamente al login
    Y debe ver el mensaje "Debe iniciar sesión para acceder a esta página"

  @Security @Positivo
  Escenario: Logout con múltiples pestañas abiertas
    # HU-003: El logout debe cerrar la sesión en todas las pestañas
    Dado que el usuario tiene el sistema abierto en múltiples pestañas
    Y está autenticado en todas ellas
    Cuando cierra sesión en una de las pestañas
    Entonces las otras pestañas deben detectar el cierre de sesión
    Y mostrar el mensaje de sesión expirada al intentar realizar alguna acción

  @UX @Positivo
  Escenario: Confirmación antes del logout
    # HU-003: El sistema puede pedir confirmación antes de cerrar sesión
    Dado que el usuario tiene trabajo sin guardar
    Cuando intenta cerrar sesión
    Entonces el sistema debe mostrar una confirmación
    Y preguntar "¿Está seguro de que desea cerrar sesión?"
    Y debe ofrecer las opciones "Sí, cerrar sesión" y "Cancelar"

  @UX @Positivo
  Escenario: Logout automático por inactividad
    # HU-003: El sistema debe cerrar sesión automáticamente por seguridad
    Dado que el usuario está autenticado
    Y ha estado inactivo por más del tiempo límite configurado
    Cuando intenta realizar cualquier acción
    Entonces el sistema debe mostrar el mensaje "Su sesión ha expirado por inactividad"
    Y debe redirigirlo automáticamente al login
    Y debe requerir nueva autenticación

  @Accessibility @Positivo
  Escenario: Logout usando atajos de teclado
    # HU-003: El logout debe ser accesible mediante teclado
    Dado que el usuario está navegando con el teclado
    Cuando presiona "Alt+L" o la combinación configurada para logout
    Entonces debe activarse el proceso de cierre de sesión
    Y debe funcionar igual que hacer clic en el botón

  @Performance @NoFuncional
  Escenario: Tiempo de respuesta del logout
    # HU-003: El logout debe ser rápido y eficiente
    Dado que el usuario está autenticado
    Cuando inicia el proceso de logout
    Entonces el cierre de sesión debe completarse en menos de 2 segundos
    Y la redirección debe ocurrir inmediatamente

  @Negativo @ErrorHandling
  Escenario: Intento de logout cuando ya se cerró la sesión
    # HU-003: Manejar intentos de logout con sesión ya cerrada
    Dado que el usuario cerró su sesión
    Cuando intenta cerrar sesión nuevamente
    Entonces el sistema debe manejar graciosamente la situación
    Y debe redirigir al login sin mostrar errores

  @Negativo @ErrorHandling
  Escenario: Logout con problemas de conectividad
    # HU-003: Manejar logout cuando hay problemas de red
    Dado que el usuario está autenticado
    Y se simula pérdida de conectividad temporal
    Cuando intenta cerrar sesión
    Entonces el sistema debe mostrar un mensaje apropiado
    Y debe intentar cerrar la sesión localmente
    Y limpiar los datos de sesión del navegador

  @API @Tecnico
  Escenario: Invalidación del token de sesión en el logout
    # HU-003: El token debe invalidarse en el servidor
    Dado que el usuario tiene un token de sesión válido
    Cuando cierra su sesión
    Entonces el token debe invalidarse en el servidor
    Y cualquier intento posterior de usar el token debe fallar
    Y debe retornar un error 401 "Token inválido"

  # Esquema de escenario para diferentes tipos de usuario
  @Regression @Outline
  Esquema del escenario: Logout para diferentes roles de usuario
    # HU-003: El logout debe funcionar igual para todos los roles
    Dado que existe un usuario con rol "<rol>"
    Y que ha iniciado sesión correctamente
    Cuando el usuario de rol "<rol>" cierra su sesión
    Entonces debe ser redirigido a la página de login
    Y la sesión debe estar completamente terminada
    Y el mensaje debe ser "<mensaje_esperado>"

    Ejemplos:
      | rol           | mensaje_esperado                    |
      | admin         | Sesión de administrador cerrada     |
      | usuario       | Sesión cerrada exitosamente        |
      | invitado      | Sesión de invitado terminada        |
      | moderador     | Sesión de moderador finalizada      |

  @Integration @Sistemas
  Escenario: Logout con integración de sistemas externos
    # HU-003: El logout debe notificar a sistemas integrados
    Dado que el usuario está autenticado
    Y el sistema tiene integraciones con servicios externos
    Cuando cierra su sesión
    Entonces debe notificar el logout a los sistemas integrados
    Y debe esperar confirmación antes de completar el proceso
    Y en caso de fallo, debe mostrar advertencia apropiada

  @Audit @Seguridad
  Escenario: Registro de auditoría del logout
    # HU-003: El logout debe quedar registrado para auditoría
    Dado que el usuario "juan.perez@test.com" está autenticado
    Cuando cierra su sesión
    Entonces debe registrarse en el log de auditoría
    Y debe incluir la fecha, hora, IP y usuario
    Y debe marcarse como "LOGOUT_EXITOSO"
    Y debe ser consultable en los reportes de seguridad