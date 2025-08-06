# language: es

@Busqueda @Productos @CRUD
Característica: Búsqueda y filtrado de productos
  Como usuario del sistema
  Quiero buscar y filtrar productos de manera eficiente
  Para encontrar rápidamente los productos que necesito

  # Referencia: HU-005 - Sistema de búsqueda de productos

  Antecedentes:
    Dado que el sistema está funcionando correctamente
    Y que el usuario está autenticado en el sistema
    Y que existen productos en el catálogo:
      | nombre                | categoria     | precio  | stock | estado  | destacado |
      | Laptop Gaming Pro     | Electrónicos  | 1299.99 | 15    | activo  | true      |
      | Mouse Inalámbrico     | Electrónicos  | 29.99   | 50    | activo  | false     |
      | Teclado Mecánico      | Electrónicos  | 89.99   | 25    | activo  | true      |
      | Monitor 4K            | Electrónicos  | 399.99  | 8     | activo  | false     |
      | Camiseta Deportiva    | Ropa          | 19.99   | 100   | activo  | false     |
      | Zapatillas Running    | Ropa          | 79.99   | 30    | activo  | true      |
      | Libro JavaScript      | Libros        | 45.00   | 20    | activo  | false     |
      | Producto Agotado      | Test          | 99.99   | 0     | activo  | false     |

  @SmokeTest @Positivo
  Escenario: Búsqueda básica por nombre de producto
    # HU-005: El usuario puede buscar productos por nombre
    Dado que el usuario está en la página de productos
    Cuando ingresa "Laptop" en el campo de búsqueda
    Y hace clic en el botón "Buscar"
    Entonces debe ver los resultados de búsqueda
    Y debe mostrar al menos 1 producto
    Y debe resaltar "Laptop Gaming Pro" en los resultados
    Y debe mostrar el mensaje "1 producto encontrado"

  @Regression @Positivo
  Escenario: Búsqueda con múltiples resultados
    # HU-005: La búsqueda debe mostrar todos los productos coincidentes
    Dado que el usuario está en la página de productos
    Cuando busca productos que contengan "electrónicos" en la categoría
    Entonces debe ver 4 productos en los resultados
    Y debe mostrar "Laptop Gaming Pro"
    Y debe mostrar "Mouse Inalámbrico"
    Y debe mostrar "Teclado Mecánico"
    Y debe mostrar "Monitor 4K"
    Y debe mostrar el contador "4 productos encontrados"

  @Positivo
  Escenario: Búsqueda sin resultados
    # HU-005: El sistema debe manejar búsquedas sin resultados
    Dado que el usuario está en la página de productos
    Cuando busca "ProductoInexistente123"
    Entonces debe ver el mensaje "No se encontraron productos"
    Y debe mostrar "0 productos encontrados"
    Y debe sugerir "Intenta con otros términos de búsqueda"
    Y debe mantener el término de búsqueda en el campo

  @Filtros @Positivo
  Escenario: Filtrado por categoría
    # HU-005: El usuario puede filtrar productos por categoría
    Dado que el usuario está en la página de productos
    Cuando selecciona la categoría "Ropa" en el filtro
    Entonces debe ver solo productos de la categoría "Ropa"
    Y debe mostrar 2 productos
    Y debe mostrar "Camiseta Deportiva"
    Y debe mostrar "Zapatillas Running"

  @Filtros @Positivo
  Escenario: Filtrado por rango de precios
    # HU-005: El usuario puede filtrar por rango de precios
    Dado que el usuario está en la página de productos
    Cuando establece el precio mínimo en "50"
    Y establece el precio máximo en "100"
    Y aplica el filtro de precios
    Entonces debe ver solo productos con precios entre $50 y $100
    Y debe mostrar "Teclado Mecánico" ($89.99)
    Y debe mostrar "Zapatillas Running" ($79.99)
    Y no debe mostrar productos fuera del rango

  @Filtros @Positivo
  Escenario: Filtrado por disponibilidad de stock
    # HU-005: El usuario puede filtrar productos en stock
    Dado que el usuario está en la página de productos
    Cuando activa el filtro "Solo productos en stock"
    Entonces debe ver solo productos con stock mayor a 0
    Y no debe mostrar "Producto Agotado"
    Y debe mostrar todos los demás productos

  @Filtros @Positivo
  Escenario: Filtrado por productos destacados
    # HU-005: El usuario puede ver solo productos destacados
    Dado que el usuario está en la página de productos
    Cuando activa el filtro "Solo productos destacados"
    Entonces debe ver solo productos marcados como destacados
    Y debe mostrar "Laptop Gaming Pro"
    Y debe mostrar "Teclado Mecánico"
    Y debe mostrar "Zapatillas Running"
    Y no debe mostrar productos no destacados

  @Combinado @Positivo
  Escenario: Búsqueda combinada con múltiples filtros
    # HU-005: Los filtros deben poder combinarse
    Dado que el usuario está en la página de productos
    Cuando busca "Gaming" en el campo de búsqueda
    Y selecciona la categoría "Electrónicos"
    Y establece precio máximo en "1500"
    Y aplica todos los filtros
    Entonces debe ver "Laptop Gaming Pro"
    Y debe mostrar "1 producto encontrado"
    Y debe indicar los filtros aplicados

  @Ordenamiento @Positivo
  Escenario: Ordenar resultados por precio ascendente
    # HU-005: Los resultados deben poder ordenarse
    Dado que el usuario ha realizado una búsqueda con resultados
    Cuando selecciona "Precio: Menor a Mayor" en el ordenamiento
    Entonces los productos deben mostrarse ordenados por precio ascendente
    Y el producto más barato debe aparecer primero
    Y el producto más caro debe aparecer último

  @Ordenamiento @Positivo
  Escenario: Ordenar resultados por precio descendente
    # HU-005: Los resultados deben poder ordenarse de manera descendente
    Dado que el usuario está viendo resultados de búsqueda
    Cuando selecciona "Precio: Mayor a Menor" en el ordenamiento
    Entonces los productos deben mostrarse ordenados por precio descendente
    Y el producto más caro debe aparecer primero

  @Ordenamiento @Positivo
  Escenario: Ordenar por nombre alfabéticamente
    # HU-005: Los productos deben poder ordenarse alfabéticamente
    Dado que el usuario está viendo resultados de búsqueda
    Cuando selecciona "Nombre A-Z" en el ordenamiento
    Entonces los productos deben mostrarse en orden alfabético
    Y "Camiseta Deportiva" debe aparecer antes que "Laptop Gaming Pro"

  @Performance @NoFuncional
  Escenario: Rendimiento de búsqueda con muchos productos
    # HU-005: La búsqueda debe ser eficiente
    Dado que existen más de 1000 productos en el sistema
    Cuando el usuario realiza una búsqueda
    Entonces los resultados deben cargarse en menos de 3 segundos
    Y debe mostrar máximo 20 productos por página
    Y debe incluir paginación si hay más resultados

  @AutoComplete @UX
  Escenario: Sugerencias automáticas durante la búsqueda
    # HU-005: El sistema debe ayudar con sugerencias
    Dado que el usuario está en el campo de búsqueda
    Cuando escribe "Lap"
    Entonces debe ver sugerencias automáticas
    Y debe mostrar "Laptop Gaming Pro" como sugerencia
    Y debe poder seleccionar la sugerencia con un clic
    Y debe poder navegar las sugerencias con las flechas del teclado

  @Historial @UX
  Escenario: Historial de búsquedas recientes
    # HU-005: El usuario debe ver sus búsquedas recientes
    Dado que el usuario ha realizado búsquedas anteriormente
    Cuando hace clic en el campo de búsqueda
    Entonces debe ver sus búsquedas recientes
    Y debe poder seleccionar una búsqueda anterior
    Y debe poder limpiar el historial de búsquedas

  @Negativo @ErrorHandling
  Escenario: Búsqueda con términos muy largos
    # HU-005: Manejar términos de búsqueda excesivamente largos
    Dado que el usuario está en la página de productos
    Cuando ingresa un término de búsqueda de más de 100 caracteres
    Entonces el sistema debe limitar el término a 100 caracteres
    Y debe mostrar el mensaje "Término de búsqueda demasiado largo"
    Y debe realizar la búsqueda con el término recortado

  @Negativo @Security
  Escenario: Búsqueda con caracteres especiales maliciosos
    # HU-005: Prevenir inyecciones mediante búsqueda
    Dado que el usuario está en la página de productos
    Cuando ingresa "<script>alert('XSS')</script>" en el campo de búsqueda
    Entonces el sistema debe sanitizar la entrada
    Y no debe ejecutar código malicioso
    Y debe mostrar "No se encontraron productos" de manera segura

    Cuando navega al campo de búsqueda
    Entonces debe escuchar "Campo de búsqueda de productos"
    Y debe poder realizar búsquedas usando solo el teclado
    Y los resultados deben ser anunciados por el lector de pantalla

  @Mobile @Responsive
  Escenario: Búsqueda en dispositivos móviles
    # HU-005: La búsqueda debe funcionar en móviles
    Dado que el usuario accede desde un dispositivo móvil
    Y está en la página de productos
    Cuando realiza una búsqueda
    Entonces la interfaz debe adaptarse al tamaño de pantalla
    Y los filtros deben ser accesibles en un menú desplegable
    Y los resultados deben mostrarse de manera optimizada

  # Esquema de escenario para diferentes tipos de búsqueda
  @Regression @Outline
  Esquema del escenario: Búsqueda por diferentes atributos
    # HU-005: La búsqueda debe funcionar con diferentes criterios
    Dado que el usuario está en la página de productos
    Cuando busca por "<atributo>" con valor "<valor>"
    Entonces debe encontrar "<cantidad>" productos
    Y todos los resultados deben coincidir con "<criterio>"

    Ejemplos:
      | atributo    | valor        | cantidad | criterio                           |
      | nombre      | Mouse        | 1        | nombre contiene "Mouse"            |
      | categoria   | Electrónicos | 4        | categoría es "Electrónicos"        |
      | precio_max  | 50           | 3        | precio menor o igual a 50          |
      | stock_min   | 25           | 5        | stock mayor o igual a 25           |
      | destacado   | true         | 3        | producto marcado como destacado    |

  @API @Tecnico
  Escenario: Búsqueda via API REST
    # HU-005: La búsqueda debe estar disponible via API
    Dado que se accede al endpoint "/api/productos/buscar"
    Cuando se envía una petición GET con parámetros:
      | parametro | valor        |
      | q         | Laptop       |
      | categoria | Electrónicos |
      | limite    | 10           |
    Entonces debe retornar status 200
    Y debe retornar productos en formato JSON
    Y debe incluir metadata de paginación

  @Cache @Performance
  Escenario: Cache de resultados de búsqueda frecuentes
    # HU-005: Optimizar búsquedas frecuentes con cache
    Dado que un término se busca frecuentemente
    Cuando el usuario busca ese término por primera vez
    Entonces los resultados deben guardarse en cache
    Y búsquedas posteriores del mismo término deben ser más rápidas
    Y el cache debe actualizarse cuando cambie el catálogo

  @Analytics @Reporting
  Escenario: Registro de estadísticas de búsqueda
    # HU-005: Recopilar datos para analítica
    Dado que el usuario realiza una búsqueda
    Cuando se ejecuta la búsqueda
    Entonces debe registrarse en las estadísticas:
      | campo                | ejemplo                   |
      | termino_busqueda     | "Laptop Gaming"           |
      | cantidad_resultados  | 1                         |
      | tiempo_respuesta     | "0.25s"                   |
      | filtros_aplicados    | "categoria:Electronics"   |
      | usuario_id           | "12345"                   |

  @Integration @Elasticsearch
  Escenario: Búsqueda avanzada con motor de búsqueda
    # HU-005: Integración con motor de búsqueda especializado
    Dado que el sistema usa Elasticsearch para búsquedas
    Cuando el usuario busca "Gaming Laptop rápido"
    Entonces debe realizar búsqueda por relevancia
    Y debe considerar sinónimos y palabras relacionadas
    Y debe rankear resultados por popularidad y relevancia
    Y debe soportar búsqueda difusa para errores tipográficos

  @Export @Utilidades
  Escenario: Exportar resultados de búsqueda
    # HU-005: El usuario puede exportar los resultados
    Dado que el usuario ha realizado una búsqueda con resultados
    Cuando selecciona "Exportar resultados"
    Y elige el formato "CSV"
    Entonces debe descargar un archivo CSV
    Y debe contener todos los productos encontrados
    Y debe incluir nombre, precio, categoría y stock

  @Favoritos @Personalizacion
  Escenario: Guardar búsquedas favoritas
    # HU-005: El usuario puede guardar búsquedas frecuentes
    Dado que el usuario ha configurado una búsqueda específica
    Cuando hace clic en "Guardar búsqueda"
    Y le asigna el nombre "Electrónicos Baratos"
    Entonces la búsqueda debe guardarse en sus favoritos
    Y debe poder accederla desde el menú de búsquedas guardadas
    Y debe poder ejecutarla con un solo clic

  @SEO @URL
  Escenario: URLs amigables para SEO en búsquedas
    # HU-005: Las búsquedas deben generar URLs indexables
    Dado que el usuario realiza una búsqueda con filtros específicos
    Cuando la búsqueda se completa
    Entonces la URL debe reflejar los parámetros de búsqueda
    Y debe ser compartible y marcable como favorito
    Y debe cargar correctamente cuando se accede directamente

  @Multilingual @I18n
  Escenario: Búsqueda multiidioma
    # HU-005: Soporte para búsquedas en diferentes idiomas
    Dado que el sistema soporta múltiples idiomas
    Cuando el usuario busca un término en español
    Entonces debe encontrar productos con nombres en español
    Y debe soportar caracteres especiales (ñ, acentos)
    Y debe funcionar con términos tanto en español como inglés

  @Batch @BulkOperations
  Escenario: Operaciones masivas sobre resultados de búsqueda
    # HU-005: Gestión masiva de productos encontrados
    Dado que el usuario es administrador
    Y ha realizado una búsqueda con múltiples resultados
    Cuando selecciona "Operaciones masivas"
    Entonces debe poder:
      | operacion           | descripcion                    |
      | cambiar_categoria   | Cambiar categoría de varios    |
      | aplicar_descuento   | Aplicar descuento porcentual   |
      | actualizar_stock    | Modificar stock masivamente    |
      | exportar_datos      | Exportar información completa  |

  @Voice @Accessibility
  Escenario: Búsqueda por voz
    # HU-005: Búsqueda accesible mediante comandos de voz
    Dado que el usuario tiene capacidades de reconocimiento de voz habilitadas
    Cuando dice "Buscar laptop gaming"
    Entonces el sistema debe procesar el comando de voz
    Y convertirlo a texto en el campo de búsqueda
    Y ejecutar la búsqueda automáticamente
    Y anunciar los resultados encontrados
    Cuando navega al campo de búsqueda
    Entonces debe escuchar "Campo de búsqueda de productos"
    Y debe poder realizar búsquedas usando solo el teclado
    Y los resultados deben ser anunciados por el lector de pantalla

  @Mobile @Responsive
  Escenario: Búsqueda en dispositivos móviles
    # HU-005: La búsqueda debe funcionar en móviles
    Dado que el usuario accede desde un dispositivo móvil
    Y está en la página de productos
    Cuando realiza una búsqueda
    Entonces la interfaz debe adaptarse al tamaño de pantalla
    Y los filtros deben ser accesibles en un menú desplegable
    Y los resultados deben mostrarse de manera optimizada

  # Esquema de escenario para diferentes tipos de búsqueda
  @Regression @Outline
  Esquema del escenario: Búsqueda por diferentes atributos
    # HU-005: La búsqueda debe funcionar con diferentes criterios
    Dado que el usuario está en la página de productos
    Cuando busca por "<atributo>" con valor "<valor>"
    Entonces debe encontrar "<cantidad>" productos
    Y todos los resultados deben coincidir con "<criterio>"

    Ejemplos:
      | atributo    | valor        | cantidad | criterio                           |
      | nombre      | Mouse        | 1        | nombre contiene "Mouse"            |
      | categoria   | Electrónicos | 4        | categoría es "Electrónicos"        |
      | precio_max  | 50           | 3        | precio menor o igual a 50          |
      | stock_min   | 25           | 5        | stock mayor o igual a 25           |
      | destacado   | true         | 3        | producto marcado como destacado    |

  @API @Tecnico
  Escenario: Búsqueda via API REST
    # HU-005: La búsqueda debe estar disponible via API
    Dado que se accede al endpoint "/api/productos/buscar"
    Cuando se envía una petición GET con parámetros:
      | parametro | valor     |
      | q         | Laptop    |
      | categoria | Electrónicos |
      | limite    | 10        |
    Entonces debe retornar status 200
    Y debe retornar productos en formato JSON
    Y debe incluir metadata de paginación

  @Cache @Performance
  Escenario: Cache de resultados de búsqueda frecuentes
    # HU-005: Optimizar búsquedas frecuentes con cache
    Dado que un término se busca frecuentemente
    Cuando el usuario busca ese término por primera vez
    Entonces los resultados deben guardarse en cache
    Y búsquedas posteriores del mismo término deben ser más rápidas
    Y el cache debe actualizarse cuando cambie el catálogo

  @Analytics @Reporting
  Escenario: Registro de estadísticas de búsqueda
    # HU-005: Recopilar datos para analítica
    Dado que el usuario realiza una búsqueda
    Cuando se ejecuta la búsqueda
    Entonces debe registrarse en las estadísticas:
      | campo              | ejemplo           |
      | termino_busqueda   | "Laptop Gaming"   |
      | cantidad_resultados| 1                 |
      | tiempo_respuesta   | "0.25s"           |
      | filtros_aplicados  | "categoria:Electronics" |
      | usuario_id         | "12345"           |

  @Integration @Elasticsearch
  Escenario: Búsqueda avanzada con motor de búsqueda
    # HU-005: Integración con motor de búsqueda especializado
    Dado que el sistema usa Elasticsearch para búsquedas
    Cuando el usuario busca "Gaming Laptop rápido"
    Entonces debe realizar búsqueda por relevancia
    Y debe considerar sinónimos y palabras relacionadas
    Y debe rankear resultados por popularidad y relevancia
    Y debe soportar búsqueda difusa para errores tipográficos

  @Export @Utilidades
  Escenario: Exportar resultados de búsqueda
    # HU-005: El usuario puede exportar los resultados
    Dado que el usuario ha realizado una búsqueda con resultados
    Cuando selecciona "Exportar resultados"
    Y elige el formato "CSV"
    Entonces debe descargar un archivo CSV
    Y debe contener todos los productos encontrados
    Y debe incluir nombre, precio, categoría y stock

  @Favoritos @Personalizacion
  Escenario: Guardar búsquedas favoritas
    # HU-005: El usuario puede guardar búsquedas frecuentes
    Dado que el usuario ha configurado una búsqueda específica
    Cuando hace clic en "Guardar búsqueda"
    Y le asigna el nombre "Electrónicos Baratos"
    Entonces la búsqueda debe guardarse en sus favoritos
    Y debe poder accederla desde el menú de búsquedas guardadas
    Y debe poder ejecutarla con un solo clic