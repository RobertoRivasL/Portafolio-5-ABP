#!/bin/bash
# =============================================================================
# SCRIPT DE CONFIGURACIÓN DEL PROYECTO BDD AUTOMATIZACIÓN
# Proyecto: Escenarios de Comportamiento - Curso de Automatización de Pruebas  
# Autores: Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
# =============================================================================

echo "🚀 Configurando Proyecto BDD de Automatización de Pruebas..."
echo "============================================================="

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para mostrar mensajes con colores
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Verificar que estamos en el directorio correcto
if [ ! -f "pom.xml" ]; then
    print_error "No se encontró pom.xml. Asegúrate de ejecutar este script en el directorio raíz del proyecto."
    exit 1
fi

print_info "Verificando estructura del proyecto..."

# Crear directorios necesarios
print_info "Creando estructura de directorios..."

DIRECTORIES=(
    "src/test/resources/configuracion"
    "src/test/resources/templates" 
    "src/test/resources/features/autenticacion"
    "src/test/resources/features/operaciones"
    "src/test/resources/features/regresion"
    "reportes/html"
    "reportes/json"
    "reportes/junit"
    "reportes/timeline"
    "reportes/screenshots"
    "reportes/trazabilidad"
    "reportes/logs"
    "documentacion/historias-usuario"
    "documentacion/casos-prueba"
    "documentacion/evidencias"
)

for dir in "${DIRECTORIES[@]}"; do
    if mkdir -p "$dir"; then
        print_success "Directorio creado: $dir"
    else
        print_error "Error creando directorio: $dir"
    fi
done

# Crear archivo logback-test.xml
print_info "Creando archivo de configuración de logs..."
cat > src/test/resources/logback-test.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>reportes/logs/automatizacion.log</file>
        <encoder>
            <pattern>%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="com.qa.automatizacion" level="DEBUG"/>
    <logger name="org.seleniumhq.selenium" level="INFO"/>
    <logger name="io.cucumber" level="INFO"/>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
EOF

if [ $? -eq 0 ]; then
    print_success "Archivo logback-test.xml creado"
else
    print_error "Error creando logback-test.xml"
fi

# Crear .gitignore si no existe
if [ ! -f ".gitignore" ]; then
    print_info "Creando archivo .gitignore..."
    cat > .gitignore << 'EOF'
# Directorios de reportes generados
reportes/
target/

# Archivos de configuración sensibles
src/test/resources/configuracion/application-prod.properties
src/test/resources/configuracion/secrets.properties

# Archivos de IDE
.idea/
*.iml
.vscode/
.settings/
.project
.classpath

# Logs
*.log

# Screenshots y videos
screenshots/
videos/

# Archivos temporales
*.tmp
*.temp

# Dependencias de Node.js
node_modules/

# Archivos de sistema
.DS_Store
Thumbs.db
EOF
    print_success "Archivo .gitignore creado"
else
    print_warning "Archivo .gitignore ya existe"
fi

# Crear ejemplo de archivo .feature si no existe
if [ ! -f "src/test/resources/features/autenticacion/login.feature" ]; then
    print_info "Creando ejemplo de archivo .feature..."
    cat > src/test/resources/features/autenticacion/login.feature << 'EOF'
# language: es
@Login @Autenticacion
Característica: Autenticación de usuario
  Como usuario del sistema
  Quiero poder iniciar sesión con mis credenciales
  Para acceder a las funcionalidades del sistema

  # Referencia: HU-001 - Autenticación de Usuario

  Antecedentes:
    Dado que el usuario está en la página de login
    Y el sistema está funcionando correctamente

  @SmokeTest @Positivo
  Escenario: Login exitoso con credenciales válidas
    # HU-001: Criterio de aceptación - El usuario puede ingresar email y contraseña válidos
    Dado que el usuario tiene credenciales válidas
      | email         | password    |
      | test@test.com | password123 |
    Cuando el usuario ingresa sus credenciales
    Y hace clic en el botón "Iniciar Sesión"
    Entonces el usuario debe ser redirigido al dashboard
    Y debe ver el mensaje de bienvenida "Bienvenido al sistema"
    Y debe ver su nombre de usuario en la barra superior

  @Regression @Negativo
  Escenario: Login fallido con credenciales inválidas
    # HU-001: Criterio de aceptación - Se muestra mensaje de error para credenciales inválidas
    Dado que el usuario tiene credenciales inválidas
      | email              | password           |
      | noexiste@test.com  | passwordIncorrecto |
    Cuando el usuario ingresa sus credenciales
    Y hace clic en el botón "Iniciar Sesión"
    Entonces debe ver el mensaje de error "Credenciales inválidas"
    Y debe permanecer en la página de login
    Y el campo de contraseña debe estar vacío
EOF
    print_success "Archivo de ejemplo login.feature creado"
fi

# Verificar Java y Maven
print_info "Verificando prerrequisitos..."

# Verificar Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
    print_success "Java encontrado: $JAVA_VERSION"
else
    print_error "Java no está instalado o no está en PATH"
fi

# Verificar Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | awk '{print $3}')
    print_success "Maven encontrado: $MVN_VERSION"
else
    print_error "Maven no está instalado o no está en PATH"
fi

# Ejecutar mvn clean compile para verificar que el proyecto compila
print_info "Compilando el proyecto..."
if mvn clean compile -q; then
    print_success "Proyecto compilado exitosamente"
else
    print_error "Error al compilar el proyecto"
fi

# Verificar archivos críticos
print_info "Verificando archivos críticos del proyecto..."

CRITICAL_FILES=(
    "pom.xml"
    "src/main/java/com/qa/automatizacion/utilidades/HelperTrazabilidad.java"
    "src/test/java/com/qa/automatizacion/ejecutor/EjecutorPruebas.java"
    "src/test/resources/datos/usuarios-prueba.json"
    "src/test/resources/datos/productos-prueba.json"
)

for file in "${CRITICAL_FILES[@]}"; do
    if [ -f "$file" ]; then
        print_success "Archivo encontrado: $file"
    else
        print_warning "Archivo faltante: $file"
    fi
done

# Crear archivos de configuración si no existen
print_info "Verificando archivos de configuración..."

# Verificar si application.properties existe
if [ ! -f "src/test/resources/configuracion/application.properties" ]; then
    print_warning "Archivo application.properties no encontrado"
    print_info "Por favor, copia el contenido del artefacto 'application.properties' a:"
    print_info "src/test/resources/configuracion/application.properties"
fi

# Verificar si cucumber.properties existe  
if [ ! -f "src/test/resources/configuracion/cucumber.properties" ]; then
    print_warning "Archivo cucumber.properties no encontrado"
    print_info "Por favor, copia el contenido del artefacto 'cucumber.properties' a:"
    print_info "src/test/resources/configuracion/cucumber.properties"
fi

# Verificar si el template HTML existe
if [ ! -f "src/test/resources/templates/reporte-html.template" ]; then
    print_warning "Template HTML no encontrado"
    print_info "Por favor, copia el contenido del artefacto 'reporte-html.template' a:"
    print_info "src/test/resources/templates/reporte-html.template"
fi

# Crear script de ejecución rápida
print_info "Creando scripts de ejecución rápida..."

# Script para ejecución de smoke tests
cat > ejecutar-smoke.sh << 'EOF'
#!/bin/bash
echo "🔥 Ejecutando Smoke Tests..."
mvn clean test -Dcucumber.filter.tags="@SmokeTest" -q
echo "✅ Smoke Tests completados. Revisa los reportes en: reportes/"
EOF

chmod +x ejecutar-smoke.sh
print_success "Script ejecutar-smoke.sh creado"

# Script para ejecución completa
cat > ejecutar-completo.sh << 'EOF'
#!/bin/bash
echo "🚀 Ejecutando todas las pruebas..."
mvn clean test -q
echo "✅ Pruebas completadas. Revisa los reportes en: reportes/"
if [ -f "reportes/html/cucumber-report.html" ]; then
    echo "🌐 Abriendo reporte HTML..."
    if command -v xdg-open &> /dev/null; then
        xdg-open reportes/html/cucumber-report.html
    elif command -v open &> /dev/null; then
        open reportes/html/cucumber-report.html
    else
        echo "Abre manualmente: reportes/html/cucumber-report.html"
    fi
fi
EOF

chmod +x ejecutar-completo.sh
print_success "Script ejecutar-completo.sh creado"

# Script de limpieza
cat > limpiar-reportes.sh << 'EOF'
#!/bin/bash
echo "🧹 Limpiando reportes anteriores..."
rm -rf reportes/html/*
rm -rf reportes/json/*
rm -rf reportes/junit/*
rm -rf reportes/timeline/*
rm -rf reportes/screenshots/*
rm -rf reportes/trazabilidad/*
rm -rf reportes/logs/*
echo "✅ Reportes limpiados"
EOF

chmod +x limpiar-reportes.sh
print_success "Script limpiar-reportes.sh creado"

# Crear README para el desarrollo
print_info "Creando README de desarrollo..."
cat > DESARROLLO.md << 'EOF'
# Guía de Desarrollo - Proyecto BDD Automatización

## 🚀 Comandos Rápidos

### Ejecución de Pruebas
```bash
# Smoke Tests
./ejecutar-smoke.sh
# o
mvn clean test -Dcucumber.filter.tags="@SmokeTest"

# Todas las pruebas
./ejecutar-completo.sh
# o  
mvn clean test

# Pruebas por funcionalidad
mvn clean test -Dcucumber.filter.tags="@Login"
mvn clean test -Dcucumber.filter.tags="@CRUD"
mvn clean test -Dcucumber.filter.tags="@Regression"
```

### Limpieza
```bash
# Limpiar reportes
./limpiar-reportes.sh

# Limpiar compilación
mvn clean
```

### Desarrollo
```bash
# Solo compilar
mvn compile

# Validar sintaxis sin ejecutar
mvn test -Dcucumber.execution.dry-run=true
```

## 📁 Estructura de Archivos Importantes

- `src/test/resources/features/` - Archivos .feature con escenarios
- `src/test/java/com/qa/automatizacion/pasos/` - Step Definitions
- `src/test/resources/configuracion/` - Archivos de configuración
- `src/test/resources/datos/` - Datos de prueba (JSON)
- `reportes/` - Reportes generados automáticamente

## 🔧 Configuración

1. **application.properties** - Configuración general del proyecto
2. **cucumber.properties** - Configuración específica de Cucumber
3. **logback-test.xml** - Configuración de logging

## 📊 Reportes

Los reportes se generan automáticamente en:
- **HTML**: `reportes/html/cucumber-report.html` (principal)
- **JSON**: `reportes/json/cucumber-report.json` (para CI/CD)
- **JUnit**: `reportes/junit/cucumber-report.xml` (para CI/CD)
- **Trazabilidad**: `reportes/trazabilidad/reporte-trazabilidad.html`

## 🏷️ Tags Importantes

- `@SmokeTest` - Pruebas críticas básicas
- `@Regression` - Pruebas de regresión completas  
- `@Login` - Funcionalidad de autenticación
- `@CRUD` - Operaciones CRUD
- `@WIP` - Work In Progress (se excluyen por defecto)
- `@Ignore` - Temporalmente deshabilitadas

## 🐛 Debugging

1. **Ver logs detallados**:
   ```bash
   mvn test -Dlog.level=DEBUG
   ```

2. **Ejecutar con navegador visible**:
   Editar `application.properties`: `navegador.headless=false`

3. **Capturar screenshots en fallos**:
   Configurado automáticamente en `reportes/screenshots/`

## 👥 Equipo

- Antonio B. Arriagada LL. - anarriag@gmail.com
- Dante Escalona Bustos - Jacobo.bustos.22@gmail.com  
- Roberto Rivas Lopez - umancl@gmail.com
EOF

print_success "Archivo DESARROLLO.md creado"

# Crear versión de Windows del script
print_info "Creando versión para Windows..."
cat > setup-proyecto.bat << 'EOF'
@echo off
echo 🚀 Configurando Proyecto BDD de Automatización de Pruebas...
echo =============================================================

REM Crear directorios necesarios
echo Creando estructura de directorios...

mkdir "src\test\resources\configuracion" 2>nul
mkdir "src\test\resources\templates" 2>nul
mkdir "src\test\resources\features\autenticacion" 2>nul
mkdir "src\test\resources\features\operaciones" 2>nul
mkdir "src\test\resources\features\regresion" 2>nul
mkdir "reportes\html" 2>nul
mkdir "reportes\json" 2>nul
mkdir "reportes\junit" 2>nul
mkdir "reportes\timeline" 2>nul
mkdir "reportes\screenshots" 2>nul
mkdir "reportes\trazabilidad" 2>nul
mkdir "reportes\logs" 2>nul
mkdir "documentacion\historias-usuario" 2>nul
mkdir "documentacion\casos-prueba" 2>nul
mkdir "documentacion\evidencias" 2>nul

echo ✅ Directorios creados

REM Crear scripts de ejecución para Windows
echo @echo off > ejecutar-smoke.bat
echo echo 🔥 Ejecutando Smoke Tests... >> ejecutar-smoke.bat
echo mvn clean test -Dcucumber.filter.tags="@SmokeTest" -q >> ejecutar-smoke.bat
echo echo ✅ Smoke Tests completados. Revisa los reportes en: reportes/ >> ejecutar-smoke.bat

echo @echo off > ejecutar-completo.bat
echo echo 🚀 Ejecutando todas las pruebas... >> ejecutar-completo.bat  
echo mvn clean test -q >> ejecutar-completo.bat
echo echo ✅ Pruebas completadas. Revisa los reportes en: reportes/ >> ejecutar-completo.bat
echo if exist "reportes\html\cucumber-report.html" start reportes\html\cucumber-report.html >> ejecutar-completo.bat

echo @echo off > limpiar-reportes.bat
echo echo 🧹 Limpiando reportes anteriores... >> limpiar-reportes.bat
echo del /q reportes\html\* 2^>nul >> limpiar-reportes.bat
echo del /q reportes\json\* 2^>nul >> limpiar-reportes.bat  
echo del /q reportes\junit\* 2^>nul >> limpiar-reportes.bat
echo del /q reportes\timeline\* 2^>nul >> limpiar-reportes.bat
echo del /q reportes\screenshots\* 2^>nul >> limpiar-reportes.bat
echo del /q reportes\trazabilidad\* 2^>nul >> limpiar-reportes.bat
echo del /q reportes\logs\* 2^>nul >> limpiar-reportes.bat
echo echo ✅ Reportes limpiados >> limpiar-reportes.bat

echo ✅ Scripts de Windows creados

echo.
echo 📋 SIGUIENTE PASOS:
echo 1. Copiar los archivos de configuración a sus ubicaciones
echo 2. Copiar el template HTML a src\test\resources\templates\
echo 3. Ejecutar: mvn clean test
echo.
echo 🎯 SCRIPTS DISPONIBLES:
echo - ejecutar-smoke.bat (pruebas rápidas)
echo - ejecutar-completo.bat (todas las pruebas)  
echo - limpiar-reportes.bat (limpieza)
echo.
pause
EOF

print_success "Script setup-proyecto.bat para Windows creado"

# Mostrar resumen final
echo ""
echo "============================================================="
print_success "🎉 CONFIGURACIÓN COMPLETADA"
echo "============================================================="
echo ""
print_info "📋 ARCHIVOS CREADOS:"
echo "  ✅ Estructura de directorios"
echo "  ✅ logback-test.xml"
echo "  ✅ .gitignore"
echo "  ✅ login.feature (ejemplo)"
echo "  ✅ Scripts de ejecución (Linux/Mac)"
echo "  ✅ setup-proyecto.bat (Windows)"
echo "  ✅ DESARROLLO.md (guía)"
echo ""
print_warning "📝 TAREAS PENDIENTES:"
echo "  🔸 Copiar application.properties a src/test/resources/configuracion/"
echo "  🔸 Copiar cucumber.properties a src/test/resources/configuracion/"
echo "  🔸 Copiar reporte-html.template a src/test/resources/templates/"
echo "  🔸 Integrar código del generador HTML en HelperTrazabilidad"
echo ""
print_info "🚀 COMANDOS SIGUIENTES:"
echo "  ./ejecutar-smoke.sh    # Pruebas rápidas"
echo "  ./ejecutar-completo.sh # Todas las pruebas"
echo "  mvn clean test         # Ejecución estándar"
echo ""
print_success "¡Proyecto listo para desarrollo!"
EOF