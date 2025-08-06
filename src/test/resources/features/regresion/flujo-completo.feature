# language: es
# Autor: Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
# Fecha: 2025
# Historia de Usuario: HU-005 - Flujo completo de gestión de productos
# Descripción: Validación del flujo completo desde login hasta gestión CRUD de productos

@FlujoCompleto @Regression @E2E
Característica: Flujo completo de gestión de productos
  Como usuario del sistema de gestión
  Quiero poder realizar un flujo completo desde login hasta gestión de productos
  Para validar que todo el sistema funciona correctamente de extremo a extremo

  Antecedentes:
    Dado que la aplicación está en estado inicial
    Y que tengo datos de prueba válidos

  @SmokeTest @Login @CRUD
  Escenario: Flujo completo exitoso - Login, navegación y gestión CRUD
    # Fase 1: Autenticación
    Dado que estoy en la página de login
    Cuando inicio sesión con credenciales válidas
      | usuario    | admin_test      |
      | contrasena | password123     |
      | email      | admin@test.com  |
      | nombre     | Admin           |
      | apellido   | Tester          |
    Entonces debería estar autenticado en el sistema
    Y debería estar en la página "dashboard"
    Y debería ver el mensaje "Bienvenido al sistema"

    # Fase 2: Navegación a CRUD
    Cuando navego a la sección "Gestión de Productos"
    Entonces debería estar en la página "crud"
    Y la página debería cargar correctamente

    # Fase 3: Crear producto
    Cuando creo un nuevo producto con los siguientes datos:
      | nombre      | Laptop Dell XPS 13        |
      | descripcion | Laptop ultrabook premium  |
      | precio      | 1299.99                   |
      | categoria   | Electrónicos              |
    Entonces el producto debería crearse exitosamente
    Y debería ver el mensaje "Producto creado exitosamente"

    # Fase 4: Buscar producto
    Cuando busco el producto "Laptop Dell XPS 13"
    Entonces debería ver el producto en los resultados de búsqueda

    # Fase 5: Editar producto
    Cuando edito el producto "Laptop Dell XPS 13" con los siguientes datos:
      | nombre      | Laptop Dell XPS 13 (Actualizada) |
      | descripcion | Laptop ultrabook premium 2025     |
      | precio      | 1199.99                           |
      | categoria   | Electrónicos                      |
    Entonces los datos del producto deberían actualizarse correctamente
    Y debería ver el mensaje "Producto actualizado exitosamente"

    # Fase 6: Verificar actualización
    Cuando busco el producto "Laptop Dell XPS 13 (Actualizada)"
    Entonces debería ver el producto en los resultados de búsqueda

    # Fase 7: Eliminar producto
    Cuando elimino el producto "Laptop Dell XPS 13 (Actualizada)"
    Entonces el producto debería ser eliminado del sistema
    Y debería ver el mensaje "Producto eliminado exitosamente"

    # Fase 8: Verificar eliminación
    Cuando busco el producto "Laptop Dell XPS 13 (Actualizada)"
    Entonces no debería ver el producto en los resultados de búsqueda

    # Fase 9: Cerrar sesión
    Cuando cierro sesión
    Entonces no debería estar autenticado en el sistema
    Y debería estar en la página "login"

  @ErrorHandling @NegativeTesting
  Escenario: Flujo con manejo de errores durante operaciones CRUD
    # Setup inicial
    Dado que soy un usuario autenticado
    Y que estoy en la página de gestión CRUD

    # Intentar crear producto con datos inválidos
    Cuando creo un nuevo producto con los siguientes datos:
      | nombre      | Producto Test    |
      | descripcion |                  |
      | precio      | -100             |
      | categoria   | Electrónicos     |
    Entonces no debería crearse el producto
    Y debería ver un mensaje de error de validación

    # Buscar producto inexistente
    Cuando busco el producto "Producto Inexistente XYZ123"
    Entonces no debería ver el producto en los resultados de búsqueda
    Y debería ver el mensaje "No se encontraron productos"

    # Intentar editar producto inexistente
    Cuando intento editar el producto "Producto Fantasma" con los siguientes datos:
      | nombre      | Producto Actualizado |
      | descripcion | Nueva descripción    |
      | precio      | 500.00               |
      | categoria   | General              |
    Entonces debería ver un mensaje de error de validación
    Y debería ver el mensaje "Producto no encontrado"

  @DataDriven @MultipleProducts
  Esquema del escenario: Gestión de múltiples productos con diferentes categorías
    Dado que soy un usuario autenticado
    Y que estoy en la página de gestión CRUD

    Cuando creo un nuevo producto con los siguientes datos:
      | nombre      | <nombre>      |
      | descripcion | <descripcion> |
      | precio      | <precio>      |
      | categoria   | <categoria>   |
    Entonces el producto debería crearse exitosamente

    Cuando busco productos que contengan "<termino_busqueda>" en el nombre
    Entonces debería ver 1 producto(s) en los resultados
    Y todos los productos mostrados deberían contener "<termino_busqueda>" en el nombre

    Ejemplos:
      | nombre              | descripcion                  | precio  | categoria        | termino_busqueda |
      | Smartphone Galaxy   | Teléfono inteligente premium | 899.99  | Electrónicos     | Smartphone       |
      | Silla Ergonómica    | Silla de oficina cómoda      | 299.99  | Hogar            | Silla            |
      | Libro Python        | Guía completa de Python      | 49.99   | Libros           | Libro            |
      | Zapatillas Running  | Calzado deportivo liviano    | 129.99  | Deportes         | Zapatillas       |

  @PerformanceTest @StressTest
  Escenario: Prueba de rendimiento - Creación masiva de productos
    Dado que soy un usuario autenticado
    Y que estoy en la página de gestión CRUD

    Cuando creo múltiples productos en secuencia:
      | Producto Test 001 | Descripción 001 | 100.00 | General      |
      | Producto Test 002 | Descripción 002 | 200.00 | Electrónicos |
      | Producto Test 003 | Descripción 003 | 300.00 | Hogar        |
      | Producto Test 004 | Descripción 004 | 400.00 | Deportes     |
      | Producto Test 005 | Descripción 005 | 500.00 | Libros       |

    Entonces todos los productos deberían crearse exitosamente
    Y el tiempo de respuesta promedio debería ser menor a 3 segundos
    Y debería ver 5 producto(s) en los resultados al buscar "Producto Test"

  @SecurityTest @Authorization
  Escenario: Validación de autorización en operaciones CRUD
    Dado que estoy en la página de login

    # Intentar acceder a CRUD sin autenticación
    Cuando navego directamente a la URL "/crud"
    Entonces debería ser redirigido a la página de login
    Y debería ver el mensaje "Debe iniciar sesión para acceder"

    # Autenticarse con usuario con permisos limitados
    Cuando inicio sesión con credenciales de usuario limitado
      | usuario    | user_limited    |
      | contrasena | password123     |
    Y navego a la sección "Gestión de Productos"
    Entonces debería poder ver la sección "Consultar Productos"
    Pero no debería poder ver la sección "Crear Producto"
    Y no debería poder ver la sección "Eliminar Producto"

  @Integration @DatabaseValidation
  Escenario: Validación de persistencia de datos
    Dado que soy un usuario autenticado
    Y que estoy en la página de gestión CRUD
    Y que no existe ningún producto en el sistema

    # Crear producto y validar persistencia
    Cuando creo un nuevo producto con los siguientes datos:
      | nombre      | Producto Persistencia |
      | descripcion | Test de persistencia  |
      | precio      | 999.99                |
      | categoria   | General               |
    Entonces el producto debería crearse exitosamente

    # Refrescar página y verificar que los datos persisten
    Cuando refresco la página
    Y busco el producto "Producto Persistencia"
    Entonces debería ver el producto en los resultados de búsqueda

    # Cerrar sesión, volver a autenticarse y verificar persistencia
    Cuando cierro sesión
    Y inicio sesión nuevamente
    Y navego a la sección "Gestión de Productos"
    Y busco el producto "Producto Persistencia"
    Entonces debería ver el producto en los resultados de búsqueda

  @UserExperience @ResponsiveDesign
  Escenario: Validación de experiencia de usuario y diseño responsivo
    Dado que soy un usuario autenticado
    Y que estoy en la página de gestión CRUD

    # Verificar elementos de interfaz principales
    Entonces debería poder ver la sección "Formulario de Producto"
    Y debería poder ver la sección "Lista de Productos"
    Y debería poder ver la sección "Opciones de Búsqueda"

    # Probar interacciones de usuario
    Cuando tomo una captura de pantalla con nombre "interfaz_crud_completa"
    Y creo un nuevo producto con datos válidos
    Y tomo una captura de pantalla con nombre "producto_creado"
    Entonces la página debería cargar correctamente
    Y el sistema debería estar funcionando correctamente

  @Cleanup @DataTeardown
  Escenario: Limpieza de datos de prueba
    Dado que soy un usuario autenticado
    Y que estoy en la página de gestión CRUD

    # Limpiar cualquier dato de prueba que pueda haber quedado
    Cuando busco productos que contengan "Test" en el nombre
    Y elimino todos los productos encontrados
    Entonces no debería ver productos de prueba en el sistema
    Y el sistema debería estar limpio para futuras pruebas