# Guía de Contribución

## 📋 Información General

Esta guía establece las mejores prácticas y estándares para contribuir al proyecto de Automatización de Pruebas BDD. Seguir estas directrices asegura la consistencia, calidad y mantenibilidad del código.

## 🎯 Principios Fundamentales

### Principios de Desarrollo
- **SOLID**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **DRY**: Don't Repeat Yourself - Evitar duplicación de código
- **KISS**: Keep It Simple, Stupid - Mantener simplicidad en el diseño
- **YAGNI**: You Aren't Gonna Need It - No implementar funcionalidades innecesarias

### Principios BDD
- **Lenguaje Ubicuo**: Usar terminología del dominio de negocio
- **Colaboración**: Los escenarios deben ser entendibles por todos los stakeholders
- **Ejemplos Concretos**: Usar datos específicos y realistas
- **Enfoque en Comportamiento**: Describir qué hace el sistema, no cómo lo hace

## 🏗️ Estructura del Proyecto

### Organización de Paquetes

```
com.qa.automatizacion/
├── configuracion/     # Configuración del sistema
├── hooks/            # Hooks de Cucumber
├── pasos/            # Step Definitions
├── paginas/          # Page Objects
├── modelo/           # Modelos de datos
├── utilidades/       # Utilidades y helpers
└── ejecutor/         # Configuración de ejecución
```

### Convenciones de Nomenclatura

#### Clases Java
- **PascalCase** para nombres de clases
- **Nombres descriptivos** en español cuando sea posible
- **Prefijos específicos** por tipo:
    - `Pagina` para Page Objects: `PaginaLogin`, `PaginaRegistro`
    - `Pasos` para Step Definitions: `PasosLogin`, `PasosRegistro`
    - `Helper` para utilidades: `HelperTrazabilidad`
    - `Gestor` para managers: `GestorBaseDatos`

#### Métodos
- **camelCase** para nombres de métodos
- **Verbos descriptivos** que indiquen la acción
- **Nombres en español** para métodos de negocio
```java
// ✅ Correcto
public void ingresarCredenciales(String email, String password)
public boolean esPaginaCargada()
public void navegarAProductos()

// ❌ Incorrecto
public void doLogin(String email, String password)
public boolean isLoaded()
public void goToProducts()
```

#### Variables
- **camelCase** para variables
- **Nombres descriptivos** que indiquen el propósito
- **Evitar abreviaciones** no estándar
```java
// ✅ Correcto
private String emailUsuario;
private LocalDateTime tiempoInicioEjecucion;
private List<String> mensajesError;

// ❌ Incorrecto
private String usrEmail;
private LocalDateTime startTime;
private List<String> errMsg;
```

#### Archivos Feature
- **kebab-case** para nombres de archivos
- **Nombres descriptivos** del área funcional
```
✅ Correcto:
- login.feature
- registro-usuario.feature
- crud-productos.feature

❌ Incorrecto:
- Login.feature
- user_registration.feature
- productCRUD.feature
```

### Estructura de Archivos Feature

```gherkin
# language: es
@TagPrincipal @TagSecundario
Característica: Descripción clara de la funcionalidad
  Como [rol]
  Quiero [objetivo]
  Para [beneficio]

  # Referencia: HU-XXX - Nombre de la Historia de Usuario
  
  Antecedentes:
    Dado que el contexto común está establecido
    
  @SmokeTest @Positivo
  Escenario: Descripción del escenario en lenguaje natural
    # HU-XXX: Criterio de aceptación específico
    Dado que existe una precondición
    Cuando se ejecuta una acción
    Entonces se verifica el resultado esperado
    
  @Regression @Negativo
  Esquema del escenario: Plantilla para múltiples casos
    Dado que se configura el contexto con "<parametro>"
    Cuando se ejecuta la acción
    Entonces se obtiene el resultado "<resultado>"
    
    Ejemplos:
      | parametro | resultado |
      | valor1    | resultado1 |
      | valor2    | resultado2 |
```

## 📝 Estándares de Codificación

### Comentarios y Documentación

#### Comentarios de Clase
```java
/**
 * Descripción clara y concisa de la responsabilidad de la clase.
 * Explica el propósito y contexto de uso.
 * 
 * Principios aplicados:
 * - Principio específico 1: Explicación
 * - Principio específico 2: Explicación
 * - Patrón utilizado: Explicación del patrón
 */
public class PaginaLogin extends PaginaBase {
```

#### Comentarios de Método
```java
/**
 * Descripción de qué hace el método y por qué.
 * 
 * @param parametro descripción del parámetro
 * @return descripción de lo que retorna
 * @throws ExceptionType cuándo se lanza la excepción
 */
public boolean verificarElementoVisible(By localizador) {
```

#### Comentarios en Línea
```java
// Explicación del por qué, no del qué
configuracion.setTimeoutImplicito(10); // Timeout aumentado para elementos dinámicos

// TODO: Implementar validación de formato de email más robusta
// FIXME: Manejar timeout específico para elementos de carga lenta
```

### Manejo de Errores

#### Logging Estructurado
```java
// Niveles de logging apropiados
logger.debug("Iniciando búsqueda de elemento: {}", localizador);
logger.info("Usuario autenticado exitosamente: {}", emailUsuario);
logger.warn("Elemento no encontrado, reintentando: {}", localizador);
logger.error("Error crítico en autenticación: {}", e.getMessage(), e);
```

#### Excepciones Específicas
```java
// ✅ Correcto: Excepciones específicas y descriptivas
if (!elemento.isDisplayed()) {
    throw new ElementoNoVisibleException(
        "El elemento no está visible: " + localizador
    );
}

// ❌ Incorrecto: Excepciones genéricas
if (!elemento.isDisplayed()) {
    throw new RuntimeException("Error");
}
```

### Patrones de Diseño

#### Page Object Pattern
```java
public class PaginaLogin extends PaginaBase {
    // Localizadores como constantes
    private static final By CAMPO_EMAIL = By.id("email");
    
    // Métodos de interacción
    public void ingresarEmail(String email) {
        registrarAccion("Ingresando email: " + email);
        ingresarTextoSeguro(CAMPO_EMAIL, email);
    }
    
    // Métodos de verificación
    public boolean esElementoVisible() {
        return esElementoVisible(CAMPO_EMAIL);
    }
}
```

#### Step Definitions
```java
@Dado("que el usuario está en la página de login")
public void elUsuarioEstaEnLaPaginaDeLogin() {
    // 1. Logging de la acción
    logger.info("Navegando a la página de login");
    
    // 2. Registro de trazabilidad
    trazabilidad.registrarPaso("HU-001", "Navegación a página de login");
    
    // 3. Acción principal
    ConfiguradorNavegador.navegarA(propiedades.obtenerUrlLogin());
    
    // 4. Verificación
    assertTrue(paginaLogin.estaPaginaCargada(), 
              "La página de login no se cargó correctamente");
}
```

## 🧪 Estándares de Testing

### Nomenclatura de Escenarios

#### Estructura Recomendada
```
[Tipo] [Condición] [Resultado Esperado]

Ejemplos:
✅ "Login exitoso con credenciales válidas"
✅ "Validación de formato de email inválido"
✅ "Creación de producto con datos completos"

❌ "Test de login"
❌ "Validar email"
❌ "CRUD producto"
```

### Tags de Organización

#### Tags Obligatorios
- `@[Funcionalidad]`: `@Login`, `@Registro`, `@CRUD`
- `@[Tipo]`: `@SmokeTest`, `@Regression`, `@Integration`
- `@[Historia]`: `@HU-001`, `@HU-002`, `@HU-003`

#### Tags Opcionales
- `@[Clasificación]`: `@Positivo`, `@Negativo`, `@Edge`
- `@[Estado]`: `@WIP`, `@Ignore`, `@Blocked`
- `@[Performance]`: `@Slow`, `@Fast`, `@Performance`

### Datos de Prueba

#### Archivos JSON Estructurados
```json
{
  "usuarios": [
    {
      "id": "usuario_login_valido",
      "descripcion": "Usuario principal para pruebas de login exitoso",
      "casos_uso": ["login_exitoso", "navegacion_dashboard"],
      "datos": {
        "email": "test@test.com",
        "password": "password123"
      }
    }
  ]
}
```

#### Datos en Escenarios
```gherkin
# ✅ Usar datos específicos y realistas
Dado que el usuario tiene credenciales válidas:
  | email           | password    |
  | juan@test.com   | password123 |

# ❌ Evitar datos genéricos
Dado que el usuario tiene credenciales válidas:
  | email | password |
  | email | pass     |
```

## 🔄 Proceso de Desarrollo

### Workflow de Git

#### Estructura de Ramas
```
main
├── develop
├── feature/nombre-funcionalidad
├── bugfix/descripcion-bug
└── hotfix/arreglo-critico
```

#### Convención de Commits
```
tipo(ámbito): descripción breve

Tipos válidos:
- feat: nueva funcionalidad
- fix: corrección de bug
- docs: documentación
- style: formato, espacios, etc.
- refactor: refactorización de código
- test: agregar o modificar tests
- chore: tareas de mantenimiento

Ejemplos:
feat(login): agregar validación de formato de email
fix(registro): corregir validación de contraseña
docs(readme): actualizar instrucciones de instalación
test(crud): agregar escenarios de validación de productos
```

### Code Review

#### Checklist de Revisión

##### Funcionalidad
- [ ] ¿El código cumple con los requerimientos?
- [ ] ¿Los escenarios cubren casos positivos y negativos?
- [ ] ¿Las validaciones son apropiadas?
- [ ] ¿El manejo de errores es adecuado?

##### Calidad de Código
- [ ] ¿Se siguen las convenciones de nomenclatura?
- [ ] ¿Los métodos tienen responsabilidad única?
- [ ] ¿Hay duplicación de código que se pueda evitar?
- [ ] ¿Los comentarios son claros y útiles?

##### Testing
- [ ] ¿Los tests pasan exitosamente?
- [ ] ¿Los escenarios son comprensibles para stakeholders?
- [ ] ¿Se incluyen tags apropiados?
- [ ] ¿La trazabilidad con HU está clara?

##### Performance
- [ ] ¿Los timeouts son apropiados?
- [ ] ¿Se evitan esperas innecesarias?
- [ ] ¿Los localizadores son eficientes?

## 📊 Métricas y Reportes

### Métricas de Calidad
- **Cobertura de Historias de Usuario**: Mínimo 90%
- **Tasa de Éxito de Pruebas**: Objetivo 95%
- **Tiempo de Ejecución**: Smoke Tests < 5 min, Regression < 30 min
- **Duplicación de Código**: Máximo 5%

### Reportes Requeridos
- **Reporte de Cucumber HTML**: Generado automáticamente
- **Matriz de Trazabilidad**: Relación escenarios-HU
- **Reporte de Performance**: Tiempos de ejecución
- **Cobertura de Código**: Con JaCoCo (opcional)

## 🔧 Herramientas y Configuración

### IDEs Recomendados
#### IntelliJ IDEA
```xml
<!-- Plugins recomendados -->
- Cucumber for Java
- Gherkin
- SonarLint
- CheckStyle-IDEA
```

#### Visual Studio Code
```json
{
  "recommendations": [
    "alexkrechik.cucumberautocomplete",
    "ms-vscode.vscode-java-pack",
    "sonarsource.sonarlint-vscode"
  ]
}
```

### Configuración de Maven
```xml
<!-- Perfiles útiles para desarrollo -->
<profiles>
  <profile>
    <id>smoke</id>
    <properties>
      <cucumber.filter.tags>@SmokeTest</cucumber.filter.tags>
    </properties>
  </profile>
  
  <profile>
    <id>regression</id>
    <properties>
      <cucumber.filter.tags>@Regression</cucumber.filter.tags>
    </properties>
  </profile>
</profiles>
```

### Configuración de Git Hooks
```bash
# Pre-commit hook para validar código
#!/bin/sh
echo "Ejecutando validaciones pre-commit..."

# Ejecutar smoke tests
mvn clean test -Dcucumber.filter.tags="@SmokeTest" -q

if [ $? -ne 0 ]; then
  echo "❌ Smoke tests fallaron. Commit rechazado."
  exit 1
fi

echo "✅ Validaciones pasaron. Procediendo con commit."
```

## 📋 Templates y Plantillas

### Template de Historia de Usuario
```markdown
# HU-XXX: [Título de la Historia]

## Descripción
**Como** [rol]  
**Quiero** [objetivo]  
**Para** [beneficio]

## Criterios de Aceptación
- [ ] Criterio 1: Descripción específica
- [ ] Criterio 2: Descripción específica
- [ ] Criterio 3: Descripción específica

## Escenarios Relacionados
- Escenario 1: Descripción
- Escenario 2: Descripción

## Definición de Terminado
- [ ] Escenarios implementados
- [ ] Pruebas pasando
- [ ] Code review completado
- [ ] Documentación actualizada
```

### Template de Escenario
```gherkin
@[Tag-Principal] @[Tag-Secundario]
Escenario: [Descripción clara del comportamiento]
  # HU-XXX: [Criterio de aceptación específico]
  Dado que [precondición específica]
  Cuando [acción del usuario]
  Entonces [resultado verificable]
  Y [verificación adicional si es necesaria]
```

### Template de Page Object
```java
package com.qa.automatizacion.paginas;

/**
 * Page Object para la página de [nombre].
 * [Descripción del propósito de la página]
 * 
 * Principios aplicados:
 * - Page Object Pattern: [explicación]
 * - Encapsulación: [explicación]
 * - [Otros principios]
 */
public class Pagina[Nombre] extends PaginaBase {
    
    // ==================== LOCALIZADORES ====================
    
    private static final By ELEMENTO_PRINCIPAL = By.id("id-elemento");
    
    // ==================== MÉTODOS PRINCIPALES ====================
    
    @Override
    public boolean estaPaginaCargada() {
        // Implementación específica
        return esElementoVisible(ELEMENTO_PRINCIPAL);
    }
    
    @Override
    public String obtenerUrlEsperada() {
        return propiedades.obtenerUrl[Nombre]();
    }
    
    // ==================== MÉTODOS DE INTERACCIÓN ====================
    
    public void realizarAccion() {
        registrarAccion("Descripción de la acción");
        // Implementación
    }
    
    // ==================== MÉTODOS DE VERIFICACIÓN ====================
    
    public boolean esCondicionCumplida() {
        // Implementación
        return true;
    }
}
```

### Template de Step Definition
```java
package com.qa.automatizacion.pasos;

/**
 * Step Definitions para los escenarios de [funcionalidad].
 * [Descripción del alcance]
 * 
 * Principios aplicados:
 * - Separación de Intereses
 * - Abstracción
 * - Modularidad
 */
public class Pasos[Funcionalidad] {
    
    private static final Logger logger = LoggerFactory.getLogger(Pasos[Funcionalidad].class);
    
    // Dependencias
    private final Pagina[Nombre] pagina;
    private final HelperTrazabilidad trazabilidad;
    
    // Variables de contexto
    private String variableContexto;
    
    public Pasos[Funcionalidad]() {
        this.pagina = new Pagina[Nombre]();
        this.trazabilidad = new HelperTrazabilidad();
    }
    
    @Dado("que [condición inicial]")
    public void queCondicionInicial() {
        logger.info("Estableciendo condición inicial");
        trazabilidad.registrarPaso("HU-XXX", "Descripción del paso");
        
        // Implementación
    }
    
    @Cuando("[acción del usuario]")
    public void accionDelUsuario() {
        logger.info("Ejecutando acción del usuario");
        trazabilidad.registrarPaso("HU-XXX", "Descripción del paso");
        
        // Implementación
    }
    
    @Entonces("[resultado esperado]")
    public void resultadoEsperado() {
        logger.info("Verificando resultado esperado");
        trazabilidad.registrarPaso("HU-XXX", "Descripción del paso");
        
        // Verificaciones con assertions
        assertTrue(condicion, "Mensaje de error descriptivo");
    }
}
```

## 🚨 Troubleshooting Común

### Problemas Frecuentes y Soluciones

#### 1. Elementos no encontrados
```java
// ❌ Problema: Localizador muy específico
By.xpath("//div[@class='container']/div[1]/span[2]/a")

// ✅ Solución: Localizador más robusto
By.cssSelector("[data-testid='login-button']")
By.id("login-btn")
```

#### 2. Timeouts y esperas
```java
// ❌ Problema: Esperas fijas
Thread.sleep(5000);

// ✅ Solución: Esperas explícitas
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.visibilityOfElementLocated(localizador));
```

#### 3. Datos de prueba duplicados
```java
// ❌ Problema: Datos hardcodeados
paginaLogin.ingresarEmail("test@test.com");

// ✅ Solución: Datos dinámicos
String email = GeneradorDatos.generarEmailUnico();
paginaLogin.ingresarEmail(email);
```

#### 4. Fallos intermitentes
```java
// ✅ Solución: Retry mechanism
@Retryable(value = {StaleElementReferenceException.class}, maxAttempts = 3)
public void hacerClickElemento(By localizador) {
    esperarElementoClickeable(localizador).click();
}
```

### Debugging Tips

#### Logs Útiles
```java
// Información de contexto
logger.debug("Estado antes de la acción: URL={}, Título={}", 
             driver.getCurrentUrl(), driver.getTitle());

// Screenshots en fallos
if (scenario.isFailed()) {
    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    scenario.attach(screenshot, "image/png", "Screenshot del fallo");
}

// Información del DOM
logger.debug("HTML del elemento: {}", elemento.getAttribute("outerHTML"));
```

#### Verificaciones Adicionales
```java
// Verificar estado del elemento
logger.debug("Elemento visible: {}, habilitado: {}, seleccionado: {}", 
             elemento.isDisplayed(), elemento.isEnabled(), elemento.isSelected());

// Verificar propiedades CSS
logger.debug("Color: {}, Tamaño: {}", 
             elemento.getCssValue("color"), elemento.getSize());
```

## 📚 Recursos Adicionales

### Documentación Oficial
- [Cucumber Documentation](https://cucumber.io/docs)
- [Selenium WebDriver](https://selenium-python.readthedocs.io/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Maven Documentation](https://maven.apache.org/guides/)

### Herramientas Recomendadas
- **SonarQube**: Análisis de calidad de código
- **Allure**: Reportes avanzados de pruebas
- **TestRail**: Gestión de casos de prueba
- **Jira**: Tracking de bugs e historias

### Libros Recomendados
- "Specification by Example" - Gojko Adzic
- "BDD in Action" - John Ferguson Smart
- "Selenium WebDriver Practical Guide" - Satya Avasarala
- "Clean Code" - Robert C. Martin

### Cursos y Certificaciones
- **ISTQB Foundation Level**
- **Cucumber School**
- **Selenium Certification**
- **Java SE Certification**

## 🤝 Comunicación y Colaboración

### Canales de Comunicación
- **Slack/Teams**: Comunicación diaria
- **Email**: Comunicación formal
- **Meetings**: Stand-ups y retrospectivas
- **Wiki**: Documentación técnica

### Ceremonias Ágiles
- **Daily Stand-up**: Estado de pruebas
- **Sprint Planning**: Estimación de escenarios
- **Sprint Review**: Demo de funcionalidades
- **Retrospective**: Mejora continua

### Definición de Terminado (DoD)
Para que una historia de usuario se considere terminada:

- [ ] Todos los escenarios implementados
- [ ] Pruebas automatizadas pasando
- [ ] Code review aprobado
- [ ] Documentación actualizada
- [ ] Sin bugs críticos o altos
- [ ] Trazabilidad verificada
- [ ] Performance aceptable

## 📝 Checklist de Pull Request

Antes de crear un PR, verificar:

### Código
- [ ] Sigue las convenciones de nomenclatura
- [ ] Métodos con responsabilidad única
- [ ] Comentarios claros y útiles
- [ ] Sin código duplicado
- [ ] Manejo apropiado de excepciones

### Testing
- [ ] Todos los tests pasan localmente
- [ ] Escenarios cubren casos positivos y negativos
- [ ] Tags apropiados asignados
- [ ] Datos de prueba actualizados
- [ ] Trazabilidad con HU clara

### Documentación
- [ ] README actualizado si es necesario
- [ ] Javadoc para métodos públicos
- [ ] Changelog actualizado
- [ ] Historias de usuario documentadas

### Performance
- [ ] No hay esperas innecesarias
- [ ] Localizadores eficientes
- [ ] Timeouts apropiados
- [ ] Recursos liberados correctamente

## 🆘 Contacto y Soporte

### Equipo de Desarrollo
- **Antonio B. Arriagada LL.**: anarriag@gmail.com
- **Dante Escalona Bustos**: Jacobo.bustos.22@gmail.com
- **Roberto Rivas Lopez**: umancl@gmail.com

### Escalación de Issues
1. **Nivel 1**: Compañeros de equipo
2. **Nivel 2**: Tech Lead / Arquitecto
3. **Nivel 3**: Product Owner / Stakeholders

### Reportar Bugs
Usar el template de GitHub Issues:
```markdown
**Descripción del Bug**
Descripción clara y concisa del bug.

**Pasos para Reproducir**
1. Ir a '...'
2. Hacer clic en '....'
3. Desplazarse hacia abajo hasta '....'
4. Ver error

**Comportamiento Esperado**
Descripción clara de lo que esperabas que sucediera.

**Screenshots**
Si aplica, agregar screenshots para explicar el problema.

**Información del Entorno**
- OS: [e.g. macOS, Windows, Linux]
- Navegador: [e.g. Chrome, Firefox]
- Versión de Java: [e.g. 21]
- Versión de Maven: [e.g. 3.9.10]
```

---

## 🎉 ¡Gracias por Contribuir!

Tu contribución hace que este proyecto sea mejor para todos. Recuerda que estamos aprendiendo juntos y que cada error es una oportunidad de mejora.

**¡Happy Testing!** 🧪✨