# language: es
@CRUD @GestionProductos
Característica: Gestión de productos (CRUD) en el sistema
  Como usuario autenticado del sistema
  Quiero gestionar productos en el catálogo
  Para mantener actualizada la información de productos disponibles

  # Referencia: HU-003 - Gestión de Productos (CRUD)

  Antecedentes:
    Dado que el usuario está autenticado en el sistema
    Y está en la sección de gestión de productos
    Y tiene permisos de administración de productos

  @Create @SmokeTest @Positivo
  Escenario: Crear un nuevo producto exitosamente
    # HU-003: Criterio de aceptación - Crear nuevos productos
    Dado que el usuario hace clic en "Nuevo Producto"
    Cuando completa el formulario con los siguientes datos:
      | nombre        | Laptop Dell Inspiron 15                    |
      | descripcion   | Laptop para uso profesional y estudiantil  |
      | precio        | 799999                                      |
      | categoria     | Electrónicos                               |
      | stock         | 25                                         |
      | codigo_sku    | DELL-INS-15-2024                           |
    Y hace clic en "Guardar Producto"
    Entonces debe ver el mensaje de éxito "Producto creado exitosamente"
    Y el producto debe aparecer en la lista de productos
    Y debe tener el estado "Activo"
    Y debe mostrar la fecha de creación actual

  @Read @SmokeTest @Positivo
  Escenario: Visualizar lista completa de productos
    # HU-003: Criterio de aceptación - Visualizar lista de productos
    Dado que existen productos en el sistema
    Cuando el usuario accede a la lista de productos
    Entonces debe ver una tabla con las siguientes columnas:
      | columna     | visible |
      | Código SKU  | Sí      |
      | Nombre      | Sí      |
      | Categoría   | Sí      |
      | Precio      | Sí      |
      | Stock       | Sí      |
      | Estado      | Sí      |
      | Acciones    | Sí      |
    Y debe ver paginación si hay más de 10 productos
    Y debe poder ordenar por cualquier columna

  @Read @Funcionalidad
  Escenario: Buscar productos por diferentes criterios
    # HU-003: Funcionalidad de búsqueda avanzada
    Dado que existen múltiples productos en el sistema
    Cuando el usuario busca productos usando "<criterio_busqueda>" con valor "<valor_busqueda>"
    Entonces debe ver solo los productos que coincidan con el criterio
    Y debe mostrar el número de resultados encontrados

    Ejemplos:
      | criterio_busqueda | valor_busqueda |
      | nombre           | Laptop          |
      | categoria        | Electrónicos    |
      | codigo_sku       | DELL           |
      | rango_precio     | 500000-1000000 |

  @Update @Positivo
  Escenario: Editar información de un producto existente
    # HU-003: Criterio de aceptación - Editar productos existentes
    Dado que existe un producto con código SKU "DELL-INS-15-2024"
    Cuando el usuario hace clic en "Editar" para ese producto
    Y modifica los siguientes campos:
      | campo       | nuevo_valor                                |
      | precio      | 849999                                     |
      | stock       | 30                                         |
      | descripcion | Laptop para uso profesional - Actualizado |
    Y hace clic en "Actualizar Producto"
    Entonces debe ver el mensaje de éxito "Producto actualizado exitosamente"
    Y los cambios deben reflejarse en la lista de productos
    Y debe registrarse la fecha de última modificación

  @Delete @Positivo
  Escenario: Eliminar un producto del sistema
    # HU-003: Criterio de aceptación - Eliminar productos
    Dado que existe un producto con código SKU "PROD-TEST-001"
    Cuando el usuario hace clic en "Eliminar" para ese producto
    Entonces debe aparecer un diálogo de confirmación con el mensaje "¿Está seguro de eliminar este producto?"
    Cuando confirma la eliminación
    Entonces debe ver el mensaje de éxito "Producto eliminado exitosamente"
    Y el producto no debe aparecer más en la lista
    Y debe registrarse en el historial de eliminaciones

  @Validacion @Negativo
  Esquema del escenario: Validación de campos obligatorios al crear producto
    # HU-003: Validaciones de formulario
    Dado que el usuario está creando un nuevo producto
    Cuando deja vacío el campo "<campo_vacio>"
    Y intenta guardar el producto
    Entonces debe ver el mensaje de error "<mensaje_error>"
    Y el producto no debe ser creado

    Ejemplos:
      | campo_vacio  | mensaje_error                      |
      | nombre       | El nombre del producto es obligatorio |
      | precio       | El precio es obligatorio           |
      | categoria    | La categoría es obligatoria        |
      | codigo_sku   | El código SKU es obligatorio       |

  @BusinessRule @Negativo
  Escenario: Prevenir duplicación de códigos SKU
    # HU-003: Regla de negocio - SKU único
    Dado que existe un producto con código SKU "UNIQUE-SKU-001"
    Cuando el usuario intenta crear otro producto con el mismo código SKU
    Entonces debe ver el mensaje de error "El código SKU ya existe en el sistema"
    Y debe sugerir un código SKU alternativo
    Y el producto no debe ser creado

  @Validacion @Negativo
  Esquema del escenario: Validación de tipos de datos
    # HU-003: Validación de formatos de datos
    Dado que el usuario está creando un producto
    Cuando ingresa "<valor_invalido>" en el campo "<campo>"
    Y intenta guardar el producto
    Entonces debe ver el mensaje de error "<mensaje_error>"

    Ejemplos:
      | campo  | valor_invalido | mensaje_error                           |
      | precio | -100           | El precio debe ser mayor a cero         |
      | precio | abc            | El precio debe ser un número válido     |
      | stock  | -5             | El stock no puede ser negativo          |
      | stock  | 1.5            | El stock debe ser un número entero      |

  @Performance @SmokeTest
  Escenario: Tiempo de respuesta en operaciones CRUD
    # HU-003: Requisito no funcional - Performance
    Dado que el usuario realiza operaciones CRUD sobre productos
    Cuando ejecuta cualquier operación (crear, leer, actualizar, eliminar)
    Entonces la operación debe completarse en menos de 2 segundos
    Y la interfaz debe permanecer responsiva durante la operación

  @Integracion @Regression
  Escenario: Sincronización con inventario después de operaciones CRUD
    # HU-003: Integración con sistema de inventario
    Dado que se crea un producto con stock inicial de 100 unidades
    Cuando se actualiza el stock a 75 unidades
    Entonces el sistema de inventario debe reflejar la cantidad actualizada
    Y debe generar una alerta si el stock es menor a 10 unidades
    Y debe actualizar automáticamente los reportes de inventario

  @Audit @Funcionalidad
  Escenario: Trazabilidad de cambios en productos
    # HU-003: Requisito de auditoría
    Dado que un producto ha sido modificado múltiples veces
    Cuando el usuario accede al historial del producto
    Entonces debe ver un registro completo de todos los cambios realizados
    Y cada cambio debe mostrar:
      | información        | visible |
      | Fecha y hora       | Sí      |
      | Usuario que modificó | Sí    |
      | Campos modificados | Sí      |
      | Valores anteriores | Sí      |
      | Valores nuevos     | Sí      |

  @Edge @Funcionalidad
  Escenario: Manejo de productos con stock agotado
    # HU-003: Caso extremo - Stock cero
    Dado que un producto tiene stock de 1 unidad
    Cuando se actualiza el stock a 0
    Entonces el producto debe cambiar automáticamente a estado "Agotado"
    Y debe aparecer marcado visualmente en la lista
    Y debe generar una notificación al administrador
    Y no debe ser visible para clientes en el catálogo público

  @Bulk @Funcionalidad
  Escenario: Operaciones masivas sobre múltiples productos
    # HU-003: Funcionalidad avanzada - Operaciones en lote
    Dado que existen múltiples productos seleccionables
    Cuando el usuario selecciona varios productos usando checkboxes
    Y elige la acción "Actualizar categoría masivamente"
    Y selecciona la nueva categoría "Ofertas Especiales"
    Entonces todos los productos seleccionados deben actualizarse
    Y debe mostrar un resumen de "X productos actualizados exitosamente"
    Y debe registrar la operación masiva en el historial

  @Security @Funcionalidad
  Escenario: Control de permisos en operaciones de productos
    # HU-003: Seguridad y control de acceso
    Dado que el usuario tiene rol "Visualizador" (sin permisos de edición)
    Cuando intenta acceder a las opciones de crear, editar o eliminar productos
    Entonces no debe ver los botones de "Nuevo", "Editar" ni "Eliminar"
    Y solo debe poder visualizar la lista y detalles de productos
    Y debe ver el mensaje "No tiene permisos para modificar productos" si intenta acceder por URL directa