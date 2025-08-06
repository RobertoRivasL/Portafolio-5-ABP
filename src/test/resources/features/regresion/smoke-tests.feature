# language: es

@SmokeTest @Critical
Característica: Pruebas de humo para funcionalidades críticas
  Como equipo de QA
  Quiero ejecutar pruebas de humo rápidas y críticas
  Para verificar que las funcionalidades principales del sistema funcionan correctamente

  # Referencia: Casos críticos de todas las HU principales

  @SmokeTest @Login @Critical
  Escenario: Smoke Test - Login básico funciona
    # HU-001: Verificación rápida de login
    Dado que el sistema está disponible
    Y que existe un usuario de prueba registrado
    Cuando el usuario intenta hacer login con credenciales válidas
    Entonces debe poder acceder al dashboard principal
    Y debe completarse en menos de 5 segundos

  @SmokeTest @Registro @Critical
  Escenario: Smoke Test - Registro básico funciona
    # HU-002: Verificación rápida de registro
    Dado que el sistema está disponible
    Cuando un nuevo usuario se registra con datos válidos únicos
    Entonces el registro debe completarse exitosamente
    Y debe recibir confirmación inmediata
    Y debe completarse en menos de 10 segundos

  @SmokeTest @CRUD @Critical
  Escenario: Smoke Test - Operaciones CRUD básicas funcionan
    # HU-004: Verificación rápida de CRUD
    Dado que un usuario autenticado está en el sistema
    Cuando crea un nuevo producto con datos mínimos requeridos
    Entonces el producto debe guardarse correctamente
    Y debe aparecer en la lista de productos
    Y debe poder editarse inmediatamente
    Y debe completarse en menos de 8 segundos

  @SmokeTest @Navegacion @Critical
  Escenario: Smoke Test - Navegación principal funciona
    # Verificación de rutas críticas
    Dado que un usuario está autenticado
    Cuando navega por las secciones principales:
      | seccion     | url_esperada |
      | Dashboard   | /dashboard   |
      | Productos   | /productos   |
      | Usuarios    | /usuarios    |
      | Perfil      | /perfil      |
    Entonces todas las páginas deben cargar correctamente
    Y en menos de 3 segundos cada una

  @SmokeTest @Database @Critical
  Escenario: Smoke Test - Conexión a base de datos funciona
    # Verificación de conectividad crítica
    Dado que el sistema requiere base de datos
    Cuando se realiza una consulta básica de verificación
    Entonces la base de datos debe responder
    Y debe retornar datos válidos
    Y debe completarse en menos de 2 segundos

  @SmokeTest @API @Critical
  Escenario: Smoke Test - APIs críticas funcionan
    # Verificación de endpoints principales
  @SmokeTest @API @Critical
  Escenario: Smoke Test - APIs críticas funcionan
    # Verificación de endpoints principales
    Dado que el sistema expone APIs REST
    Cuando se realizan llamadas a los endpoints críticos:
      | endpoint           | metodo | status_esperado |
      | /api/health        | GET    | 200             |
      | /api/auth/login    | POST   | 200             |
      | /api/productos     | GET    | 200             |
      | /api/usuarios/me   | GET    | 200             |
    Entonces todos los endpoints deben responder correctamente
    Y en menos de 1 segundo cada uno

  @SmokeTest @Security @Critical
  Escenario: Smoke Test - Autenticación y autorización básica
    # Verificación de seguridad básica
    Dado que el sistema tiene controles de acceso
    Cuando un usuario no autenticado intenta acceder a páginas protegidas
    Entonces debe ser redirigido al login
    Y no debe poder acceder a datos sensibles
    Y debe completarse la redirección en menos de 2 segundos

  @SmokeTest @Performance @Critical
  Escenario: Smoke Test - Tiempo de carga de página principal
    # Verificación de performance básica
    Dado que un usuario accede a la aplicación
    Cuando carga la página principal
    Entonces debe cargar completamente en menos de 3 segundos
    Y todos los elementos críticos deben estar presentes
    Y no debe mostrar errores de carga

  @SmokeTest @UI @Critical
  Escenario: Smoke Test - Elementos UI críticos están presentes
    # Verificación de elementos esenciales de UI
    Dado que un usuario autenticado está en el dashboard
    Entonces debe ver los elementos críticos:
      | elemento              | selector_css        |
      | Logo del sistema      | .logo               |
      | Menú principal        | .main-navigation    |
      | Nombre de usuario     | .user-name          |
      | Botón de logout       | .logout-btn         |
      | Contenido principal   | .main-content       |
    Y todos deben estar visibles y ser interactivos

  @SmokeTest @Data @Critical
  Escenario: Smoke Test - Datos de configuración están disponibles
    # Verificación de datos críticos del sistema
    Dado que el sistema requiere configuración básica
    Cuando se verifica la disponibilidad de configuraciones:
      | configuracion     | valor_esperado |
      | app.name          | no vacío       |
      | app.version       | formato x.y.z  |
      | db.connection     | activa         |
      | api.endpoints     | configurados   |
    Entonces todas las configuraciones deben estar presentes
    Y tener valores válidos

  @SmokeTest @Integration @Critical
  Escenario: Smoke Test - Servicios externos críticos funcionan
    # Verificación de integraciones esenciales
    Dado que el sistema depende de servicios externos
    Cuando se verifica la conectividad con servicios críticos:
      | servicio          | endpoint_salud    | timeout |
      | Servicio Auth     | /auth/health      | 5s      |
      | Base de datos     | conexión directa  | 2s      |
      | Servicio Email    | /email/status     | 3s      |
    Entonces todos los servicios críticos deben estar disponibles
    Y responder dentro del timeout establecido

  @SmokeTest @Mobile @Critical
  Escenario: Smoke Test - Responsividad básica funciona
    # Verificación de adaptabilidad móvil
    Dado que el usuario accede desde dispositivos móviles
    Cuando cambia el tamaño de pantalla a móvil (320px)
    Entonces la interfaz debe adaptarse correctamente
    Y los elementos principales deben seguir siendo accesibles
    Y la navegación debe funcionar en touch

  @SmokeTest @Logout @Critical
  Escenario: Smoke Test - Logout básico funciona
    # HU-003: Verificación rápida de logout
    Dado que un usuario está autenticado
    Cuando cierra su sesión
    Entonces debe ser redirigido al login
    Y no debe poder acceder a páginas protegidas
    Y debe completarse en menos de 3 segundos

  @SmokeTest @Search @Critical
  Escenario: Smoke Test - Búsqueda básica funciona
    # HU-005: Verificación rápida de búsqueda
    Dado que un usuario está en la página de productos
    Y existen productos en el sistema
    Cuando realiza una búsqueda simple
    Entonces debe obtener resultados relevantes
    Y debe completarse en menos de 2 segundos

  @SmokeTest @Forms @Critical
  Escenario: Smoke Test - Formularios críticos funcionan
    # Verificación de formularios principales
    Dado que un usuario autenticado accede a formularios críticos
    Cuando interactúa con los formularios principales:
      | formulario        | accion_critica      |
      | Crear producto    | guardar_datos       |
      | Editar perfil     | actualizar_info     |
      | Contacto          | enviar_mensaje      |
    Entonces todos los formularios deben procesar datos correctamente
    Y mostrar retroalimentación apropiada

  @SmokeTest @Validation @Critical
  Escenario: Smoke Test - Validaciones básicas funcionan
    # Verificación de validaciones esenciales
    Dado que el sistema tiene campos con validación
    Cuando se ingresan datos inválidos en campos críticos:
      | campo             | valor_invalido    |
      | Email             | email-invalido    |
      | Password          | 123               |
      | Precio            | -100              |
      | Teléfono          | abc123            |
    Entonces debe mostrar mensajes de error apropiados
    Y no debe permitir el envío del formulario

  @SmokeTest @Accessibility @Critical
  Escenario: Smoke Test - Accesibilidad básica funciona
    # Verificación de accesibilidad esencial
    Dado que el sistema debe ser accesible
    Cuando se navega usando solo el teclado
    Entonces los elementos interactivos deben ser alcanzables con Tab
    Y debe existir indicación visual del foco
    Y los elementos críticos deben tener etiquetas apropiadas

  @SmokeTest @ErrorHandling @Critical
  Escenario: Smoke Test - Manejo básico de errores funciona
    # Verificación de manejo de errores
    Dado que pueden ocurrir errores en el sistema
    Cuando se simula un error controlado:
      | tipo_error        | escenario           |
      | 404 Not Found     | página inexistente  |
      | 500 Server Error  | error simulado      |
      | Timeout           | respuesta lenta     |
    Entonces debe mostrar páginas de error apropiadas
    Y ofrecer opciones de recuperación
    Y no debe exponer información sensible

  @SmokeTest @Browser @Critical
  Escenario: Smoke Test - Compatibilidad básica de navegadores
    # Verificación en navegadores principales
    Dado que el sistema debe funcionar en múltiples navegadores
    Cuando se accede desde diferentes navegadores compatibles
    Entonces las funcionalidades críticas deben funcionar:
      | funcionalidad     | debe_funcionar |
      | Login             | sí             |
      | Navegación        | sí             |
      | Formularios       | sí             |
      | JavaScript básico | sí             |
    Y no debe mostrar errores específicos del navegador

  @SmokeTest @Session @Critical
  Escenario: Smoke Test - Gestión básica de sesiones funciona
    # Verificación de sesiones
    Dado que un usuario se autentica en el sistema
    Cuando mantiene su sesión activa por un período normal
    Entonces la sesión debe mantenerse estable
    Y debe poder realizar operaciones sin re-autenticarse
    Y debe manejar correctamente la expiración de sesión

  @SmokeTest @Monitoring @Critical
  Escenario: Smoke Test - Monitoreo básico funciona
    # Verificación de herramientas de monitoreo
    Dado que el sistema tiene capacidades de monitoreo
    Cuando se verifican los indicadores básicos de salud
    Entonces debe reportar métricas fundamentales:
      | metrica           | valor_esperado  |
      | CPU usage         | < 80%           |
      | Memory usage      | < 80%           |
      | Response time     | < 2s promedio   |
      | Error rate        | < 5%            |
    Y debe generar logs de actividad

  # Esquema para verificación rápida de múltiples funciones
  @SmokeTest @QuickCheck @Outline
  Esquema del escenario: Smoke Test rápido de funciones críticas
    # Verificación sistemática de funciones clave
    Dado que el sistema está operativo
    Cuando se ejecuta la función "<funcion>"
    Entonces debe completarse exitosamente
    Y debe tomar menos de "<tiempo_max>" segundos
    Y debe retornar "<resultado_esperado>"

    Ejemplos:
      | funcion           | tiempo_max | resultado_esperado |
      | login             | 5          | acceso_dashboard   |
      | crear_producto    | 8          | producto_creado    |
      | buscar_producto   | 2          | resultados_mostrados |
      | logout            | 3          | redireccion_login  |
      | cargar_dashboard  | 3          | pagina_completa    |

  @SmokeTest @HealthCheck @Final
  Escenario: Smoke Test - Verificación final de salud del sistema
    # Verificación integral final
    Dado que se han ejecutado todas las pruebas de humo críticas
    Cuando se revisa el estado general del sistema
    Entonces todas las funcionalidades críticas deben estar operativas
    Y no debe haber errores críticos en los logs
    Y el sistema debe estar listo para pruebas más exhaustivas
    Y debe generar reporte de estado del smoke test