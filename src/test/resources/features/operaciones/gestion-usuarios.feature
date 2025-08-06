# language: es

@GestionUsuarios @Admin @CRUD
Característica: Gestión administrativa de usuarios
  Como administrador del sistema
  Quiero gestionar los usuarios de la plataforma
  Para mantener control sobre accesos, roles y permisos del sistema

  # Referencia: HU-007 - Panel de Administración de Usuarios

  Antecedentes:
    Dado que el sistema está funcionando correctamente
    Y que existe un usuario administrador autenticado
    Y que existen los siguientes usuarios en el sistema:
      | nombre_completo    | email              | rol           | estado  | fecha_registro |
      | Juan Pérez         | juan@test.com      | usuario       | activo  | 2024-01-01     |
      | María González     | maria@test.com     | moderador     | activo  | 2024-01-05     |
      | Carlos López       | carlos@test.com    | usuario       | inactivo| 2024-01-10     |
      | Ana Martínez       | ana@test.com       | usuario       | bloqueado| 2024-01-15    |
      | Luis Rodríguez     | luis@test.com      | invitado      | activo  | 2024-01-20     |

  @SmokeTest @Positivo @Admin
  Escenario: Acceso al panel de administración de usuarios
    # HU-007: Solo administradores pueden acceder al panel
    Dado que el administrador está autenticado
    Cuando navega al panel de gestión de usuarios
    Entonces debe ver la lista de todos los usuarios del sistema
    Y debe ver las opciones de gestión disponibles
    Y debe mostrar el contador total de usuarios: 5

  @Security @Positivo
  Escenario: Restricción de acceso para usuarios no administradores
    # HU-007: Control de acceso basado en roles
    Dado que existe un usuario con rol "usuario" autenticado
    Cuando intenta acceder al panel de gestión de usuarios
    Entonces debe ser redirigido a una página de "Acceso Denegado"
    Y debe ver el mensaje "No tiene permisos para acceder a esta sección"
    Y debe registrarse el intento de acceso no autorizado

  @Busqueda @Positivo
  Escenario: Búsqueda de usuarios por diferentes criterios
    # HU-007: Facilitar la localización de usuarios específicos
    Dado que el administrador está en el panel de gestión
    Cuando busca usuarios con los siguientes criterios:
      | criterio      | valor           | usuarios_esperados |
      | nombre        | Juan            | 1                  |
      | email         | @test.com       | 5                  |
      | rol           | usuario         | 3                  |
      | estado        | activo          | 3                  |
    Entonces debe mostrar solo los usuarios que coincidan con cada criterio
    Y debe mantener visible el filtro aplicado

  @Filtros @Positivo
  Escenario: Filtrado por estado de usuario
    # HU-007: Gestión según estado del usuario
    Dado que el administrador está en el panel de gestión
    Cuando aplica el filtro de estado "inactivo"
    Entonces debe mostrar solo usuarios inactivos
    Y debe mostrar "Carlos López" en los resultados
    Y debe mostrar "1 usuario encontrado"

  @Filtros @Positivo
  Escenario: Filtrado por rol de usuario
    # HU-007: Gestión según permisos y roles
    Dado que el administrador está en el panel de gestión
    Cuando aplica el filtro de rol "moderador"
    Entonces debe mostrar solo usuarios con rol moderador
    Y debe mostrar "María González" en los resultados
    Y debe incluir información de permisos del rol

  @CreacionUsuario @Positivo
  Escenario: Crear nuevo usuario desde panel administrativo
    # HU-007: El administrador puede crear usuarios directamente
    Dado que el administrador está en el panel de gestión
    Cuando hace clic en "Crear Nuevo Usuario"
    Y completa el formulario con:
      | campo           | valor                    |
      | nombre_completo | Pedro Sánchez            |
      | email           | pedro@nuevousuario.com   |
      | rol             | usuario                  |
      | password_temp   | temporal123              |
    Y hace clic en "Crear Usuario"
    Entonces debe ver el mensaje "Usuario creado exitosamente"
    Y el usuario debe aparecer en la lista
    Y debe enviarse email de bienvenida con credenciales temporales

  @EdicionUsuario @Positivo
  Escenario: Editar información básica de usuario
    # HU-007: Modificación de datos de usuarios existentes
    Dado que el administrador está viendo los detalles de "Juan Pérez"
    Cuando hace clic en "Editar Usuario"
    Y modifica los siguientes campos:
      | campo           | nuevo_valor           |
      | nombre_completo | Juan Carlos Pérez     |
      | telefono        | +56 9 8765 4321       |
    Y guarda los cambios
    Entonces debe ver "Información actualizada exitosamente"
    Y los cambios deben reflejarse en la lista de usuarios
    Y debe registrarse la modificación en el log de auditoría

  @CambioRol @Positivo @Critical
  Escenario: Cambiar rol de usuario
    # HU-007: Gestión de permisos mediante cambio de roles
    Dado que el administrador está editando el usuario "Luis Rodríguez"
    Cuando cambia el rol de "invitado" a "usuario"
    Y confirma el cambio
    Entonces debe ver "Rol actualizado exitosamente"
    Y el usuario debe tener los nuevos permisos inmediatamente
    Y debe notificarse al usuario sobre el cambio de permisos
    Y debe registrarse el cambio en el log de auditoría con timestamp

  @EstadoUsuario @Positivo
  Escenario: Activar usuario inactivo
    # HU-007: Reactivación de usuarios suspendidos
    Dado que el administrador está viendo el usuario "Carlos López" (inactivo)
    Cuando hace clic en "Activar Usuario"
    Y confirma la acción
    Entonces debe ver "Usuario activado exitosamente"
    Y el estado del usuario debe cambiar a "activo"
    Y el usuario debe poder iniciar sesión nuevamente
    Y debe enviarse notificación de reactivación

  @EstadoUsuario @Positivo
  Escenario: Desactivar usuario problemático
    # HU-007: Suspensión temporal de usuarios
    Dado que el administrador está gestionando el usuario "Juan Pérez"
    Cuando hace clic en "Desactivar Usuario"
    Y selecciona el motivo "Actividad sospechosa"
    Y confirma la desactivación
    Entonces debe ver "Usuario desactivado exitosamente"
    Y el usuario no debe poder iniciar sesión
    Y debe cerrarse cualquier sesión activa del usuario
    Y debe notificarse al usuario sobre la suspensión

  @BloqueoUsuario @Security @Critical
  Escenario: Bloquear usuario por violación de términos
    # HU-007: Bloqueo permanente por infracciones graves
    Dado que el administrador está revisando el usuario "Ana Martínez"
    Cuando selecciona "Bloquear Usuario Permanentemente"
    Y especifica la razón "Violación grave de términos de servicio"
    Y confirma el bloqueo definitivo
    Entonces debe ver "Usuario bloqueado permanentemente"
    Y el usuario debe aparecer marcado como "bloqueado"
    Y no debe poder crear nuevas cuentas con el mismo email
    Y debe registrarse en el log de seguridad

  @DesbloqueoUsuario @Positivo
  Escenario: Desbloquear usuario tras revisión
    # HU-007: Proceso de rehabilitación de usuarios
    Dado que el administrador está revisando el usuario bloqueado "Ana Martínez"
    Cuando hace clic en "Revisar Bloqueo"
    Y evalúa que el caso amerita desbloqueo
    Y hace clic en "Desbloquear Usuario"
    Y documenta la justificación "Situación aclarada satisfactoriamente"
    Entonces debe ver "Usuario desbloqueado exitosamente"
    Y el usuario debe poder solicitar reactivación de cuenta
    Y debe notificarse sobre las condiciones de reintegración

  @ResetPassword @Seguridad
  Escenario: Restablecer contraseña de usuario
    # HU-007: Asistencia con problemas de acceso
    Dado que el administrador recibe solicitud de reset de password
    Cuando busca al usuario "María González"
    Y hace clic en "Restablecer Contraseña"
    Y confirma la acción
    Entonces debe generarse una contraseña temporal segura
    Y debe enviarse por email cifrado al usuario
    Y la contraseña actual debe invalidarse inmediatamente
    Y debe forzarse cambio de contraseña en próximo login

  @HistorialUsuario @Auditoria
  Escenario: Ver historial completo de usuario
    # HU-007: Auditoría y seguimiento de actividad
    Dado que el administrador está revisando "Juan Pérez"
    Cuando hace clic en "Ver Historial Completo"
    Entonces debe mostrar cronológicamente:
      | evento              | fecha       | detalles           |
      | Registro inicial    | 2024-01-01  | Email verificado   |
      | Primer login        | 2024-01-01  | IP: 192.168.1.100  |
      | Último login        | 2024-01-25  | IP: 192.168.1.105  |
      | Cambios de perfil   | 2024-01-15  | Teléfono agregado  |
    Y debe permitir filtrar por tipo de evento
    Y debe incluir detalles técnicos (IP, navegador, etc.)

  @ExportarDatos @GDPR @Compliance
  Escenario: Exportar datos personales de usuario
    # HU-007: Cumplimiento con regulaciones de privacidad
    Dado que se recibe solicitud GDPR del usuario "Luis Rodríguez"
    Cuando el administrador hace clic en "Exportar Datos Personales"
    Y confirma que tiene autorización legal
    Entonces debe generarse archivo JSON con todos los datos
    Y debe incluir historial de actividad completo
    Y debe cifrarse el archivo con password seguro
    Y debe enviarse link de descarga temporal (24h)
    Y debe registrarse la exportación en log de compliance

  @EliminarUsuario @GDPR @Destructivo
  Escenario: Eliminación definitiva de cuenta de usuario
    # HU-007: Derecho al olvido - eliminación permanente
    Dado que se recibe solicitud legal de eliminación de "Carlos López"
    Cuando el administrador accede a "Opciones Avanzadas"
    Y selecciona "Eliminación Definitiva de Cuenta"
    Y confirma múltiples veces la acción irreversible
    Y documenta la justificación legal
    Entonces debe eliminarse toda la información personal
    Y debe anonimizarse los datos transaccionales
    Y debe mantenerse solo ID hash para prevenir re-registro
    Y debe confirmarse la eliminación a la autoridad solicitante

  @BulkOperations @Eficiencia
  Escenario: Operaciones masivas sobre múltiples usuarios
    # HU-007: Gestión eficiente de múltiples usuarios
    Dado que el administrador necesita realizar cambios masivos
    Cuando selecciona múltiples usuarios usando checkboxes
    Y elige la operación "Cambiar Estado Masivo"
    Y selecciona nuevo estado "inactivo"
    Y confirma la operación masiva
    Entonces debe aplicarse el cambio a todos los usuarios seleccionados
    Y debe mostrarse progreso de la operación
    Y debe generarse reporte de la operación masiva
    Y debe notificarse a usuarios afectados según configuración

  @Reportes @Analytics
  Escenario: Generar reportes de gestión de usuarios
    # HU-007: Informes para toma de decisiones
    Dado que el administrador necesita reportes estadísticos
    Cuando accede a "Reportes y Estadísticas"
    Y selecciona período "último mes"
    Entonces debe mostrar métricas como:
      | metrica                  | valor_ejemplo |
      | Nuevos registros         | 12            |
      | Usuarios activos         | 45            |
      | Usuarios inactivos       | 8             |
      | Bloqueos realizados      | 2             |
      | Recuperaciones de cuenta | 5             |
    Y debe permitir exportar el reporte en PDF/Excel
    Y debe incluir gráficos de tendencias

  @Notificaciones @Comunicacion
  Escenario: Sistema de notificaciones administrativas
    # HU-007: Comunicación efectiva con usuarios
    Dado que el administrador necesita comunicarse con usuarios
    Cuando selecciona usuarios por criterio específico
    Y hace clic en "Enviar Notificación Masiva"
    Y redacta mensaje: "Mantenimiento programado este domingo"
    Y programa envío para fecha específica
    Entonces debe programarse el envío automático
    Y debe permitir seguimiento de entrega
    Y debe registrar respuestas y confirmaciones de lectura

  @Integration @API
  Escenario: Integración con sistemas externos de gestión
    # HU-007: Sincronización con otros sistemas
    Dado que el sistema se integra con LDAP/Active Directory
    Cuando se ejecuta sincronización automática
    Entonces debe importar nuevos usuarios del directorio externo
    Y debe actualizar información existente
    Y debe desactivar usuarios eliminados del sistema externo
    Y debe mantener mapeo de roles consistente
    Y debe generar log detallado de la sincronización

  # Esquema de escenario para diferentes acciones administrativas
  @Regression @Outline
  Esquema del escenario: Validación de permisos por rol
    # HU-007: Cada rol debe tener permisos específicos
    Dado que un usuario con rol "<rol>" está autenticado
    Cuando intenta realizar la acción "<accion>"
    Entonces el resultado debe ser "<resultado>"
    Y debe "<accion_log>"

    Ejemplos:
      | rol         | accion              | resultado | accion_log           |
      | admin       | crear_usuario       | exitoso   | registrar_accion     |
      | admin       | eliminar_usuario    | exitoso   | registrar_accion     |
      | moderador   | editar_usuario      | exitoso   | registrar_accion     |
      | moderador   | eliminar_usuario    | denegado  | registrar_intento    |
      | usuario     | ver_panel_admin     | denegado  | registrar_violacion  |
      | invitado    | crear_usuario       | denegado  | registrar_violacion  |

  @Performance @NonFunctional
  Escenario: Rendimiento del panel con gran cantidad de usuarios
    # HU-007: El panel debe ser eficiente con muchos usuarios
    Dado que existen más de 10,000 usuarios en el sistema
    Cuando el administrador accede al panel de gestión
    Entonces debe cargar la primera página en menos de 3 segundos
    Y debe implementar paginación eficiente
    Y debe permitir búsqueda incremental sin afectar rendimiento
    Y debe mantener respuesta ágil en todas las operaciones

  @Backup @Disaster Recovery
  Escenario: Recuperación de usuarios eliminados accidentalmente
    # HU-007: Protección contra eliminaciones accidentales
    Dado que se eliminó accidentalmente el usuario "María González"
    Cuando el administrador accede a "Papelera de Usuarios"
    Y busca usuarios eliminados en últimos 30 días
    Y selecciona "María González"
    Y hace clic en "Restaurar Usuario"
    Entonces debe restaurarse toda la información del usuario
    Y debe recuperarse el historial completo
    Y debe notificarse al usuario sobre la restauración
    Y debe registrarse la recuperación en logs de auditoría

  @Mobile @Responsive
  Escenario: Gestión de usuarios desde dispositivos móviles
    # HU-007: Administración móvil para emergencias
    Dado que el administrador accede desde un dispositivo móvil
    Cuando navega al panel de gestión de usuarios
    Entonces la interfaz debe adaptarse al tamaño de pantalla
    Y debe mantener funcionalidades críticas accesibles
    Y debe permitir operaciones de emergencia como bloqueos
    Y debe funcionar eficientemente con conexiones lentas

  @Accessibility @A11y
  Escenario: Accesibilidad del panel administrativo
    # HU-007: Panel accesible para administradores con discapacidades
    Dado que un administrador usa tecnologías asistivas
    Cuando navega por el panel de gestión
    Entonces todos los elementos deben tener etiquetas apropiadas
    Y debe ser navegable completamente con teclado
    Y debe funcionar correctamente con lectores de pantalla
    Y debe mantener contraste apropiado en todos los elementos