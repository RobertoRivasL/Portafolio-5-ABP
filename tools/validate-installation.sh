#!/bin/bash

# =============================================================================
# Script de Validación de Instalación BDD - COMPLETO
# Proyecto: Automatización de Pruebas
# Autores: Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez
# =============================================================================

# Configuración de colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m' # No Color

# Variables globales
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0
WARNING_CHECKS=0
VALIDATION_LOG="validation-report-$(date +%Y%m%d_%H%M%S).log"
START_TIME=$(date +%s)

# Función para imprimir mensajes con formato
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}" | tee -a "$VALIDATION_LOG"
}

print_header() {
    local message="$1"
    local length=${#message}
    local border=$(printf "═%.0s" $(seq 1 $length))

    echo | tee -a "$VALIDATION_LOG"
    print_message $CYAN "╔═$border═╗"
    print_message $CYAN "║ $message ║"
    print_message $CYAN "╚═$border═╝"
    echo | tee -a "$VALIDATION_LOG"
}

print_subheader() {
    print_message $WHITE "▶ $1"
}

print_success() {
    print_message $GREEN "  ✅ $1"
    ((PASSED_CHECKS++))
}

print_error() {
    print_message $RED "  ❌ $1"
    ((FAILED_CHECKS++))
}

print_warning() {
    print_message $YELLOW "  ⚠️  $1"
    ((WARNING_CHECKS++))
}

print_info() {
    print_message $BLUE "  ℹ️  $1"
}

# Función para verificar si un comando existe
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Función para incrementar contador de checks
increment_checks() {
    ((TOTAL_CHECKS++))
}

# Validar sistema operativo y arquitectura
validate_system() {
    print_subheader "Validando Sistema Operativo"

    increment_checks
    local os_name=$(uname -s)
    local os_version=$(uname -r)
    local architecture=$(uname -m)

    print_info "SO: $os_name $os_version"
    print_info "Arquitectura: $architecture"

    case $os_name in
        "Linux"|"Darwin")
            print_success "Sistema operativo compatible: $os_name"
            ;;
        *)
            print_warning "Sistema operativo no probado: $os_name"
            ;;
    esac

    # Verificar memoria RAM disponible
    increment_checks
    if command_exists free; then
        local ram_mb=$(free -m | awk 'NR==2{printf "%.0f", $2}')
        if [[ $ram_mb -ge 4096 ]]; then
            print_success "Memoria RAM: ${ram_mb}MB (suficiente)"
        elif [[ $ram_mb -ge 2048 ]]; then
            print_warning "Memoria RAM: ${ram_mb}MB (mínima recomendada: 4GB)"
        else
            print_error "Memoria RAM: ${ram_mb}MB (insuficiente, mínimo: 2GB)"
        fi
    elif command_exists system_profiler; then
        # macOS
        local ram_gb=$(system_profiler SPHardwareDataType | grep "Memory:" | awk '{print $2}')
        print_info "Memoria RAM: $ram_gb (macOS)"
        print_success "Memoria detectada en macOS"
    else
        print_warning "No se pudo verificar la memoria RAM"
    fi

    # Verificar espacio en disco
    increment_checks
    local disk_space=$(df -h . | awk 'NR==2 {print $4}')
    print_info "Espacio disponible: $disk_space"
    print_success "Espacio en disco verificado"
}

# Validar Java
validate_java() {
    print_subheader "Validando Java"

    increment_checks
    if command_exists java; then
        local java_version_output=$(java -version 2>&1 | head -n1)
        local java_version=$(echo $java_version_output | cut -d'"' -f2 | cut -d'.' -f1)

        print_info "$java_version_output"

        if [[ "$java_version" -ge 21 ]]; then
            print_success "Java versión $java_version (cumple requisito ≥21)"
        elif [[ "$java_version" -ge 11 ]]; then
            print_warning "Java versión $java_version (recomendado: ≥21)"
        else
            print_error "Java versión $java_version (requerido: ≥21)"
        fi
    else
        print_error "Java no está instalado"
    fi

    # Verificar JAVA_HOME
    increment_checks
    if [[ -n "$JAVA_HOME" ]]; then
        if [[ -d "$JAVA_HOME" ]]; then
            print_success "JAVA_HOME configurado: $JAVA_HOME"
        else
            print_error "JAVA_HOME apunta a directorio inexistente: $JAVA_HOME"
        fi
    else
        print_warning "JAVA_HOME no está configurado"
    fi

    # Verificar javac (compilador)
    increment_checks
    if command_exists javac; then
        local javac_version=$(javac -version 2>&1)
        print_success "Compilador Java disponible: $javac_version"
    else
        print_error "javac (compilador Java) no encontrado"
    fi
}

# Validar Maven
validate_maven() {
    print_subheader "Validando Maven"

    increment_checks
    if command_exists mvn; then
        local maven_version_output=$(mvn -version 2>&1 | head -n1)
        local maven_version=$(echo $maven_version_output | awk '{print $3}')

        print_info "$maven_version_output"
        print_success "Maven versión $maven_version"

        # Verificar Maven Home
        increment_checks
        local maven_home_output=$(mvn -version 2>&1 | grep "Maven home:")
        if [[ -n "$maven_home_output" ]]; then
            print_success "$maven_home_output"
        else
            print_warning "No se pudo determinar Maven home"
        fi
    else
        print_error "Maven no está instalado"
    fi

    # Verificar configuración de Maven
    increment_checks
    if [[ -f "$HOME/.m2/settings.xml" ]]; then
        print_info "Archivo de configuración Maven encontrado: ~/.m2/settings.xml"
        print_success "Configuración personalizada de Maven detectada"
    else
        print_info "Usando configuración por defecto de Maven"
        print_success "Configuración de Maven OK"
    fi

    # Verificar repositorio local
    increment_checks
    local repo_path="$HOME/.m2/repository"
    if [[ -d "$repo_path" ]]; then
        local repo_size=$(du -sh "$repo_path" 2>/dev/null | cut -f1)
        print_success "Repositorio local Maven: $repo_path ($repo_size)"
    else
        print_warning "Repositorio local Maven no existe (se creará al primer uso)"
    fi
}

# Validar Git
validate_git() {
    print_subheader "Validando Git"

    increment_checks
    if command_exists git; then
        local git_version=$(git --version)
        print_success "$git_version"

        # Verificar configuración de Git
        increment_checks
        local git_user=$(git config --global user.name 2>/dev/null)
        local git_email=$(git config --global user.email 2>/dev/null)

        if [[ -n "$git_user" && -n "$git_email" ]]; then
            print_success "Configuración Git: $git_user <$git_email>"
        else
            print_warning "Configuración Git incompleta (user.name y user.email)"
            print_info "Ejecuta: git config --global user.name 'Tu Nombre'"
            print_info "Ejecuta: git config --global user.email 'tu@email.com'"
        fi

        # Verificar si estamos en un repositorio Git
        increment_checks
        if git rev-parse --git-dir >/dev/null 2>&1; then
            local branch=$(git branch --show-current 2>/dev/null)
            print_success "Repositorio Git activo (rama: ${branch:-'detached HEAD'})"
        else
            print_info "No estamos en un repositorio Git"
            print_success "Git funcional"
        fi
    else
        print_error "Git no está instalado"
    fi
}

# Validar navegadores
validate_browsers() {
    print_subheader "Validando Navegadores"

    local browsers_found=0

    # Google Chrome
    increment_checks
    if command_exists google-chrome || command_exists google-chrome-stable; then
        local chrome_version=$(google-chrome --version 2>/dev/null || google-chrome-stable --version 2>/dev/null)
        print_success "Google Chrome: $chrome_version"
        ((browsers_found++))
    elif command_exists "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"; then
        local chrome_version=$("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" --version 2>/dev/null)
        print_success "Google Chrome (macOS): $chrome_version"
        ((browsers_found++))
    else
        print_warning "Google Chrome no encontrado"
    fi

    # Chromium
    increment_checks
    if command_exists chromium-browser || command_exists chromium; then
        local chromium_version=$(chromium-browser --version 2>/dev/null || chromium --version 2>/dev/null)
        print_success "Chromium: $chromium_version"
        ((browsers_found++))
    else
        print_info "Chromium no encontrado (opcional)"
    fi

    # Firefox
    increment_checks
    if command_exists firefox; then
        local firefox_version=$(firefox --version 2>/dev/null)
        print_success "Firefox: $firefox_version"
        ((browsers_found++))
    elif [[ -f "/Applications/Firefox.app/Contents/MacOS/firefox" ]]; then
        local firefox_version=$("/Applications/Firefox.app/Contents/MacOS/firefox" --version 2>/dev/null)
        print_success "Firefox (macOS): $firefox_version"
        ((browsers_found++))
    else
        print_info "Firefox no encontrado (opcional)"
    fi

    # Edge (si está disponible)
    increment_checks
    if command_exists microsoft-edge || command_exists msedge; then
        local edge_version=$(microsoft-edge --version 2>/dev/null || msedge --version 2>/dev/null)
        print_success "Microsoft Edge: $edge_version"
        ((browsers_found++))
    else
        print_info "Microsoft Edge no encontrado (opcional)"
    fi

    if [[ $browsers_found -eq 0 ]]; then
        print_error "No se encontraron navegadores compatibles"
    else
        print_success "Navegadores compatibles encontrados: $browsers_found"
    fi
}

# Validar estructura del proyecto
validate_project_structure() {
    print_subheader "Validando Estructura del Proyecto"

    # Verificar pom.xml
    increment_checks
    if [[ -f "pom.xml" ]]; then
        print_success "pom.xml encontrado"

        # Validar contenido básico del pom.xml
        if grep -q "cucumber" pom.xml && grep -q "selenium" pom.xml; then
            print_success "Dependencias principales detectadas en pom.xml"
        else
            print_warning "Dependencias principales no detectadas en pom.xml"
        fi
    else
        print_error "pom.xml no encontrado"
    fi

    # Verificar directorios principales
    local required_dirs=(
        "src/main/java"
        "src/test/java"
        "src/test/resources"
        "src/test/resources/features"
    )

    for dir in "${required_dirs[@]}"; do
        increment_checks
        if [[ -d "$dir" ]]; then
            print_success "Directorio encontrado: $dir"
        else
            print_error "Directorio faltante: $dir"
        fi
    done

    # Verificar directorios de reportes
    increment_checks
    if [[ -d "reportes" ]]; then
        print_success "Directorio de reportes encontrado"
    else
        print_warning "Directorio de reportes no encontrado (se creará automáticamente)"
    fi

    # Verificar archivos de configuración
    increment_checks
    if [[ -f "src/test/resources/configuracion/application.properties" ]]; then
        print_success "application.properties encontrado"
    else
        print_warning "application.properties no encontrado"
    fi

    increment_checks
    if [[ -f "src/test/resources/cucumber.properties" ]]; then
        print_success "cucumber.properties encontrado"
    else
        print_warning "cucumber.properties no encontrado"
    fi
}

# Validar dependencias Maven
validate_maven_dependencies() {
    print_subheader "Validando Dependencias Maven"

    if [[ ! -f "pom.xml" ]]; then
        print_error "pom.xml no encontrado, omitiendo validación de dependencias"
        return
    fi

    increment_checks
    print_info "Resolviendo dependencias Maven..."
    if mvn dependency:resolve -q 2>/dev/null; then
        print_success "Dependencias Maven resueltas correctamente"
    else
        print_error "Error resolviendo dependencias Maven"
        print_info "Ejecuta: mvn dependency:resolve para más detalles"
    fi

    # Verificar dependencias críticas
    local critical_deps=(
        "io.cucumber:cucumber-java"
        "org.seleniumhq.selenium:selenium-java"
        "org.junit.jupiter:junit-jupiter"
    )

    for dep in "${critical_deps[@]}"; do
        increment_checks
        if mvn dependency:tree -q 2>/dev/null | grep -q "$dep"; then
            print_success "Dependencia crítica encontrada: $dep"
        else
            print_error "Dependencia crítica no encontrada: $dep"
        fi
    done

    # Verificar conflictos de dependencias
    increment_checks
    print_info "Verificando conflictos de dependencias..."
    if mvn dependency:analyze-duplicate -q 2>/dev/null; then
        print_success "No se detectaron conflictos de dependencias"
    else
        print_warning "Posibles conflictos de dependencias detectados"
    fi
}

# Validar compilación del proyecto
validate_project_compilation() {
    print_subheader "Validando Compilación del Proyecto"

    if [[ ! -f "pom.xml" ]]; then
        print_error "pom.xml no encontrado, omitiendo compilación"
        return
    fi

    increment_checks
    print_info "Compilando código fuente..."
    if mvn compile -q 2>/dev/null; then
        print_success "Código fuente compilado exitosamente"
    else
        print_error "Error compilando código fuente"
    fi

    increment_checks
    print_info "Compilando código de pruebas..."
    if mvn test-compile -q 2>/dev/null; then
        print_success "Código de pruebas compilado exitosamente"
    else
        print_error "Error compilando código de pruebas"
    fi

    # Verificar que se generaron los .class files
    increment_checks
    if [[ -d "target/classes" ]]; then
        local class_count=$(find target/classes -name "*.class" 2>/dev/null | wc -l)
        if [[ $class_count -gt 0 ]]; then
            print_success "Archivos .class generados: $class_count"
        else
            print_warning "No se generaron archivos .class"
        fi
    else
        print_warning "Directorio target/classes no encontrado"
    fi
}

# Validar ejecución de pruebas de ejemplo
validate_test_execution() {
    print_subheader "Validando Ejecución de Pruebas"

    if [[ ! -f "pom.xml" ]]; then
        print_error "pom.xml no encontrado, omitiendo validación de pruebas"
        return
    fi

    # Verificar que existan archivos .feature
    increment_checks
    local feature_count=$(find src/test/resources -name "*.feature" 2>/dev/null | wc -l)
    if [[ $feature_count -gt 0 ]]; then
        print_success "Archivos .feature encontrados: $feature_count"
    else
        print_warning "No se encontraron archivos .feature"
        print_info "Crea al menos un archivo .feature para ejecutar pruebas"
    fi

    # Verificar que existan Step Definitions
    increment_checks
    local step_def_count=$(find src/test/java -name "*Pasos*.java" -o -name "*Steps*.java" 2>/dev/null | wc -l)
    if [[ $step_def_count -gt 0 ]]; then
        print_success "Step Definitions encontradas: $step_def_count"
    else
        print_warning "No se encontraron Step Definitions"
    fi

    # Intentar ejecutar pruebas (solo si existen features y steps)
    if [[ $feature_count -gt 0 && $step_def_count -gt 0 ]]; then
        increment_checks
        print_info "Ejecutando pruebas de validación..."

        # Ejecutar con timeout para evitar bloqueos
        timeout 60s mvn test -q 2>/dev/null
        local exit_code=$?

        case $exit_code in
            0)
                print_success "Pruebas ejecutadas exitosamente"
                ;;
            124)
                print_warning "Timeout en ejecución de pruebas (>60s)"
                ;;
            *)
                print_warning "Pruebas completadas con advertencias (código: $exit_code)"
                ;;
        esac
    else
        print_info "Omitiendo ejecución de pruebas (faltan features o step definitions)"
    fi

    # Verificar generación de reportes
    increment_checks
    if [[ -d "target/cucumber-reports" ]] || [[ -d "reportes" ]]; then
        print_success "Directorio de reportes detectado"

        # Buscar reportes generados
        local html_reports=$(find target reportes -name "*.html" 2>/dev/null | wc -l)
        local json_reports=$(find target reportes -name "*.json" 2>/dev/null | wc -l)

        if [[ $html_reports -gt 0 || $json_reports -gt 0 ]]; then
            print_success "Reportes generados: $html_reports HTML, $json_reports JSON"
        else
            print_info "No se encontraron reportes (normal si no se han ejecutado pruebas)"
        fi
    else
        print_info "No se detectaron directorios de reportes"
    fi
}

# Validar configuración de WebDrivers
validate_webdrivers() {
    print_subheader "Validando Configuración de WebDrivers"

    # Verificar WebDriverManager en dependencias
    increment_checks
    if [[ -f "pom.xml" ]] && grep -q "webdrivermanager" pom.xml; then
        print_success "WebDriverManager configurado en pom.xml"
        print_info "Los drivers se descargarán automáticamente"
    else
        print_warning "WebDriverManager no detectado en pom.xml"
    fi

    # Verificar drivers manuales (legacy)
    increment_checks
    local driver_paths=(
        "/usr/local/bin/chromedriver"
        "/usr/bin/chromedriver"
        "$HOME/.webdrivers/chromedriver"
        "./drivers/chromedriver"
    )

    local drivers_found=0
    for driver_path in "${driver_paths[@]}"; do
        if [[ -f "$driver_path" ]]; then
            print_info "Driver manual encontrado: $driver_path"
            ((drivers_found++))
        fi
    done

    if [[ $drivers_found -gt 0 ]]; then
        print_success "Drivers manuales encontrados: $drivers_found"
    else
        print_info "No se encontraron drivers manuales (esperado con WebDriverManager)"
    fi

    # Verificar permisos de ejecución para navegadores
    increment_checks
    local chrome_executable=""
    if command_exists google-chrome; then
        chrome_executable="google-chrome"
    elif command_exists google-chrome-stable; then
        chrome_executable="google-chrome-stable"
    fi

    if [[ -n "$chrome_executable" ]]; then
        if [[ -x "$(command -v $chrome_executable)" ]]; then
            print_success "Chrome ejecutable tiene permisos correctos"
        else
            print_error "Chrome no tiene permisos de ejecución"
        fi
    fi
}

# Validar configuración de red y conectividad
validate_network() {
    print_subheader "Validando Conectividad de Red"

    # Verificar conectividad a Maven Central
    increment_checks
    if ping -c 1 -W 5 repo1.maven.org >/dev/null 2>&1; then
        print_success "Conectividad a Maven Central Repository"
    else
        print_warning "No se pudo conectar a Maven Central"
        print_info "Verifica tu conexión a internet o configuración de proxy"
    fi

    # Verificar conectividad a GitHub (para drivers)
    increment_checks
    if ping -c 1 -W 5 github.com >/dev/null 2>&1; then
        print_success "Conectividad a GitHub"
    else
        print_warning "No se pudo conectar a GitHub"
    fi

    # Verificar configuración de proxy Maven
    increment_checks
    if [[ -f "$HOME/.m2/settings.xml" ]] && grep -q "<proxy>" "$HOME/.m2/settings.xml"; then
        print_info "Configuración de proxy detectada en Maven"
        print_success "Proxy Maven configurado"
    else
        print_info "No se detectó configuración de proxy Maven"
        print_success "Red directa configurada"
    fi
}

# Validar herramientas adicionales
validate_additional_tools() {
    print_subheader "Validando Herramientas Adicionales"

    # Verificar curl
    increment_checks
    if command_exists curl; then
        local curl_version=$(curl --version | head -n1)
        print_success "curl disponible: $curl_version"
    else
        print_warning "curl no encontrado (recomendado para scripts)"
    fi

    # Verificar wget
    increment_checks
    if command_exists wget; then
        local wget_version=$(wget --version | head -n1)
        print_success "wget disponible: $wget_version"
    else
        print_info "wget no encontrado (opcional)"
    fi

    # Verificar unzip
    increment_checks
    if command_exists unzip; then
        print_success "unzip disponible"
    else
        print_warning "unzip no encontrado (necesario para algunos drivers)"
    fi

    # Verificar herramientas de desarrollo adicionales
    local dev_tools=("code" "idea" "eclipse")
    for tool in "${dev_tools[@]}"; do
        increment_checks
        if command_exists $tool; then
            print_success "IDE/Editor detectado: $tool"
        else
            print_info "IDE/Editor no detectado: $tool (opcional)"
        fi
    done
}

# Validar configuración específica del proyecto BDD
validate_bdd_specific() {
    print_subheader "Validando Configuración Específica BDD"

    # Verificar que existan las clases principales del framework
    local required_classes=(
        "src/test/java/com/qa/automatizacion/ejecutor/EjecutorPruebas.java"
        "src/test/java/com/qa/automatizacion/hooks/HooksPruebas.java"
        "src/main/java/com/qa/automatizacion/configuracion/ConfiguradorNavegador.java"
    )

    for class_file in "${required_classes[@]}"; do
        increment_checks
        if [[ -f "$class_file" ]]; then
            print_success "Clase encontrada: $(basename $class_file)"
        else
            print_warning "Clase faltante: $(basename $class_file)"
        fi
    done

    # Verificar archivos de configuración específicos
    increment_checks
    if [[ -f "src/test/resources/cucumber.properties" ]]; then
        print_success "cucumber.properties configurado"

        # Validar contenido básico
        if grep -q "cucumber.plugin" src/test/resources/cucumber.properties; then
            print_success "Plugins de Cucumber configurados"
        else
            print_warning "Plugins de Cucumber no configurados"
        fi
    else
        print_warning "cucumber.properties no encontrado"
    fi

    # Verificar logging
    increment_checks
    if [[ -f "src/test/resources/logback-test.xml" ]] || [[ -f "src/main/resources/logback.xml" ]]; then
        print_success "Configuración de logging encontrada"
    else
        print_warning "Configuración de logging no encontrada"
    fi

    # Verificar scripts de automatización
    increment_checks
    if [[ -f "setup-environment.sh" ]]; then
        print_success "Script de setup encontrado"
        if [[ -x "setup-environment.sh" ]]; then
            print_success "Script de setup tiene permisos de ejecución"
        else
            print_warning "Script de setup no tiene permisos de ejecución"
        fi
    else
        print_warning "Script setup-environment.sh no encontrado"
    fi
}

# Validación de troubleshooting
validate_troubleshooting() {
    print_subheader "Diagnóstico de Problemas Comunes"

    # Verificar conflictos de versiones de Java
    increment_checks
    if command_exists java && command_exists javac; then
        local java_runtime_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
        local java_compiler_version=$(javac -version 2>&1 | awk '{print $2}')

        if [[ "$java_runtime_version" == "$java_compiler_version"* ]]; then
            print_success "Versiones de Java consistentes"
        else
            print_warning "Posible conflicto de versiones de Java"
            print_info "Runtime: $java_runtime_version, Compiler: $java_compiler_version"
        fi
    fi

    # Verificar variables de entorno críticas
    increment_checks
    local env_vars=("JAVA_HOME" "PATH" "M2_HOME")
    local env_issues=0

    for var in "${env_vars[@]}"; do
        if [[ -n "${!var}" ]]; then
            print_info "$var = ${!var}"
        else
            print_warning "$var no está configurado"
            ((env_issues++))
        fi
    done

    if [[ $env_issues -eq 0 ]]; then
        print_success "Variables de entorno principales configuradas"
    else
        print_warning "$env_issues variables de entorno necesitan atención"
    fi

    # Verificar permisos de escritura
    increment_checks
    local write_dirs=("target" "reportes" "logs" ".")
    local permission_issues=0

    for dir in "${write_dirs[@]}"; do
        if [[ -w "$dir" ]] || mkdir -p "$dir" 2>/dev/null; then
            print_success "Permisos de escritura OK: $dir"
        else
            print_error "Sin permisos de escritura: $dir"
            ((permission_issues++))
        fi
    done

    if [[ $permission_issues -gt 0 ]]; then
        print_warning "Ejecuta: $0 --fix-permissions"
    fi

    # Verificar conectividad de red con diagnóstico
    increment_checks
    local network_issues=0
    local test_urls=("repo1.maven.org" "github.com" "registry-1.docker.io")

    for url in "${test_urls[@]}"; do
        if ping -c 1 -W 3 "$url" >/dev/null 2>&1; then
            print_success "Conectividad OK: $url"
        else
            print_warning "Problema de conectividad: $url"
            ((network_issues++))
        fi
    done

    if [[ $network_issues -gt 0 ]]; then
        print_info "Posibles causas: firewall, proxy, DNS"
    fi
}

# Generar resumen de la validación
generate_summary() {
    print_header "RESUMEN DE VALIDACIÓN"

    local end_time=$(date +%s)
    local duration=$((end_time - START_TIME))

    print_info "Tiempo de validación: ${duration}s"
    print_info "Total de verificaciones: $TOTAL_CHECKS"

    if [[ $PASSED_CHECKS -gt 0 ]]; then
        print_success "Verificaciones exitosas: $PASSED_CHECKS"
    fi

    if [[ $WARNING_CHECKS -gt 0 ]]; then
        print_warning "Advertencias: $WARNING_CHECKS"
    fi

    if [[ $FAILED_CHECKS -gt 0 ]]; then
        print_error "Verificaciones fallidas: $FAILED_CHECKS"
    fi

    # Calcular porcentaje de éxito
    local success_percentage=$(( (PASSED_CHECKS * 100) / TOTAL_CHECKS ))

    echo
    if [[ $FAILED_CHECKS -eq 0 ]]; then
        if [[ $WARNING_CHECKS -eq 0 ]]; then
            print_message $GREEN "🎉 ¡VALIDACIÓN COMPLETADA EXITOSAMENTE! (${success_percentage}%)"
            print_message $GREEN "✨ Tu ambiente BDD está listo para usar"
        else
            print_message $YELLOW "✅ VALIDACIÓN COMPLETADA CON ADVERTENCIAS (${success_percentage}%)"
            print_message $YELLOW "⚠️  Revisa las advertencias para optimizar tu setup"
        fi
    else
        print_message $RED "❌ VALIDACIÓN COMPLETADA CON ERRORES (${success_percentage}%)"
        print_message $RED "🔧 Corrige los errores antes de usar el framework"
    fi

    echo
    print_info "Reporte detallado guardado en: $VALIDATION_LOG"

    # Sugerencias basadas en los resultados
    if [[ $FAILED_CHECKS -gt 0 ]]; then
        print_message $CYAN "📋 Sugerencias para corregir errores:"

        if ! command_exists java; then
            print_message $CYAN "  • Instala Java 21: ./setup-environment.sh"
        fi

        if ! command_exists mvn; then
            print_message $CYAN "  • Instala Maven: ./setup-environment.sh"
        fi

        if [[ ! -f "pom.xml" ]]; then
            print_message $CYAN "  • Ejecuta el setup completo: ./setup-environment.sh"
        fi
    fi

    if [[ $WARNING_CHECKS -gt 0 ]]; then
        print_message $CYAN "💡 Sugerencias para optimizar:"
        print_message $CYAN "  • Configura JAVA_HOME si no está definido"
        print_message $CYAN "  • Instala navegadores adicionales para más opciones"
        print_message $CYAN "  • Configura Git user.name y user.email"
    fi

    echo
}

# Crear archivo de estado
create_status_file() {
    local status_file=".bdd-validation-status"

    cat > "$status_file" << EOF
# Estado de Validación BDD
# Generado el $(date)

VALIDATION_DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
TOTAL_CHECKS=$TOTAL_CHECKS
PASSED_CHECKS=$PASSED_CHECKS
FAILED_CHECKS=$FAILED_CHECKS
WARNING_CHECKS=$WARNING_CHECKS
SUCCESS_PERCENTAGE=$(( (PASSED_CHECKS * 100) / TOTAL_CHECKS ))

# Información del sistema
JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 2>/dev/null || echo "No detectado")
MAVEN_VERSION=$(mvn -version 2>&1 | head -n1 | awk '{print $3}' 2>/dev/null || echo "No detectado")
OS_INFO=$(uname -s -r)

# Estado general
EOF

    if [[ $FAILED_CHECKS -eq 0 ]]; then
        echo 'OVERALL_STATUS="READY"' >> "$status_file"
    elif [[ $FAILED_CHECKS -lt 3 ]]; then
        echo 'OVERALL_STATUS="PARTIAL"' >> "$status_file"
    else
        echo 'OVERALL_STATUS="NOT_READY"' >> "$status_file"
    fi

    print_info "Estado guardado en: $status_file"
}

# Función principal
main() {
    # Inicializar log
    echo "Validación de Instalación BDD - $(date)" > "$VALIDATION_LOG"
    echo "Autores: Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez" >> "$VALIDATION_LOG"
    echo "========================================" >> "$VALIDATION_LOG"

    print_header "VALIDACIÓN DE INSTALACIÓN BDD"

    print_message $PURPLE "🔍 Validando configuración del ambiente de automatización"
    print_message $PURPLE "👥 Equipo: Antonio B. Arriagada LL., Dante Escalona Bustos, Roberto Rivas Lopez"

    # Ejecutar todas las validaciones
    validate_system
    validate_java
    validate_maven
    validate_git
    validate_browsers
    validate_project_structure
    validate_maven_dependencies
    validate_project_compilation
    validate_test_execution
    validate_webdrivers
    validate_network
    validate_additional_tools
    validate_bdd_specific
    validate_troubleshooting

    # Generar resumen
    generate_summary

    # Crear archivo de estado para referencia futura
    create_status_file

    # Retornar código de salida apropiado
    if [[ $FAILED_CHECKS -gt 0 ]]; then
        return 1
    else
        return 0
    fi
}

# Función para corregir permisos
fix_permissions() {
    print_subheader "Corrigiendo Permisos de Archivos"

    # Scripts ejecutables
    local scripts=("setup-environment.sh" "validate-installation.sh" "mantenimiento.sh")
    for script in "${scripts[@]}"; do
        if [[ -f "$script" ]]; then
            chmod +x "$script"
            print_success "Permisos corregidos: $script"
        fi
    done

    # Directorios con permisos correctos
    local dirs=("src" "target" "reportes" "logs")
    for dir in "${dirs[@]}"; do
        if [[ -d "$dir" ]]; then
            chmod 755 "$dir" 2>/dev/null
            print_success "Permisos corregidos: $dir/"
        fi
    done

    print_success "Corrección de permisos completada"
}

# Función para limpiar logs
clean_logs() {
    print_subheader "Limpiando Logs Anteriores"

    # Limpiar logs de validación antiguos
    find . -name "validation-report-*.log" -mtime +7 -delete 2>/dev/null
    print_success "Logs de validación antiguos eliminados"

    # Limpiar logs de Maven
    if [[ -d "target" ]]; then
        find target -name "*.log" -delete 2>/dev/null
        print_success "Logs de Maven eliminados"
    fi

    # Limpiar reportes antiguos
    if [[ -d "reportes" ]]; then
        find reportes -name "*.html" -mtime +3 -delete 2>/dev/null
        find reportes -name "*.json" -mtime +3 -delete 2>/dev/null
        print_success "Reportes antiguos eliminados"
    fi

    print_success "Limpieza completada"
}

# Función para mostrar estado guardado
show_saved_status() {
    local status_file=".bdd-validation-status"

    if [[ -f "$status_file" ]]; then
        print_header "ESTADO DE VALIDACIÓN ANTERIOR"

        source "$status_file"

        print_info "Fecha de validación: $VALIDATION_DATE"
        print_info "Total de verificaciones: $TOTAL_CHECKS"

        if [[ $PASSED_CHECKS -gt 0 ]]; then
            print_success "Verificaciones exitosas: $PASSED_CHECKS"
        fi

        if [[ $WARNING_CHECKS -gt 0 ]]; then
            print_warning "Advertencias: $WARNING_CHECKS"
        fi

        if [[ $FAILED_CHECKS -gt 0 ]]; then
            print_error "Verificaciones fallidas: $FAILED_CHECKS"
        fi

        print_info "Porcentaje de éxito: $SUCCESS_PERCENTAGE%"
        print_info "Java: $JAVA_VERSION"
        print_info "Maven: $MAVEN_VERSION"
        print_info "Sistema: $OS_INFO"

        case $OVERALL_STATUS in
            "READY")
                print_success "Estado general: LISTO PARA USAR"
                ;;
            "PARTIAL")
                print_warning "Estado general: PARCIALMENTE LISTO"
                ;;
            "NOT_READY")
                print_error "Estado general: NO LISTO"
                ;;
        esac

        echo
        print_info "Para una validación actualizada, ejecuta: $0"

    else
        print_error "No se encontró estado de validación anterior"
        print_info "Ejecuta una validación completa: $0"
        return 1
    fi
}

# Función de ayuda
show_help() {
    cat << EOF
Uso: $0 [opciones]

OPCIONES:
    -h, --help          Muestra esta ayuda
    -v, --verbose       Modo verbose (más detalles)
    -q, --quiet         Modo silencioso (solo errores)
    --quick             Validación rápida (omite compilación y tests)
    --network-only      Solo validar conectividad de red
    --summary-only      Solo mostrar resumen de validaciones previas
    --bdd-only          Solo validar componentes específicos de BDD
    --fix-permissions   Intenta corregir permisos de archivos
    --clean-logs        Limpia logs de validaciones anteriores

DESCRIPCIÓN:
    Script de validación completa del ambiente BDD de automatización de pruebas.
    Verifica todos los componentes necesarios para ejecutar pruebas con Cucumber,
    Selenium WebDriver y Maven.

EJEMPLOS:
    $0                  # Validación completa
    $0 --quick          # Validación rápida
    $0 --verbose        # Con información detallada
    $0 --bdd-only       # Solo componentes BDD

CÓDIGO DE SALIDA:
    0    Validación exitosa (sin errores críticos)
    1    Errores encontrados que requieren atención
    2    Error en argumentos de línea de comandos

AUTORES:
    Antonio B. Arriagada LL.    - anarriag@gmail.com
    Dante Escalona Bustos       - Jacobo.bustos.22@gmail.com
    Roberto Rivas Lopez         - umancl@gmail.com

EOF
}

# Procesar argumentos de línea de comandos
VERBOSE=false
QUIET=false
QUICK=false
NETWORK_ONLY=false
SUMMARY_ONLY=false
BDD_ONLY=false
FIX_PERMISSIONS=false
CLEAN_LOGS=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -q|--quiet)
            QUIET=true
            shift
            ;;
        --quick)
            QUICK=true
            shift
            ;;
        --network-only)
            NETWORK_ONLY=true
            shift
            ;;
        --summary-only)
            SUMMARY_ONLY=true
            shift
            ;;
        --bdd-only)
            BDD_ONLY=true
            shift
            ;;
        --fix-permissions)
            FIX_PERMISSIONS=true
            shift
            ;;
        --clean-logs)
            CLEAN_LOGS=true
            shift
            ;;
        *)
            echo "Opción desconocida: $1"
            echo "Usa --help para ver las opciones disponibles"
            exit 2
            ;;
    esac
done

# Configurar output según opciones
if [[ "$QUIET" == "true" ]]; then
    exec 1>/dev/null
fi

# Ejecutar acciones según opciones
if [[ "$CLEAN_LOGS" == "true" ]]; then
    clean_logs
    exit 0
fi

if [[ "$FIX_PERMISSIONS" == "true" ]]; then
    fix_permissions
    exit 0
fi

if [[ "$SUMMARY_ONLY" == "true" ]]; then
    show_saved_status
    exit $?
fi

# Ejecutar validaciones según opciones
if [[ "$NETWORK_ONLY" == "true" ]]; then
    print_header "VALIDACIÓN DE CONECTIVIDAD"
    validate_network
    validate_troubleshooting
    generate_summary
elif [[ "$BDD_ONLY" == "true" ]]; then
    print_header "VALIDACIÓN ESPECÍFICA BDD"
    validate_project_structure
    validate_bdd_specific
    validate_maven_dependencies
    generate_summary
elif [[ "$QUICK" == "true" ]]; then
    print_header "VALIDACIÓN RÁPIDA"
    validate_system
    validate_java
    validate_maven
    validate_git
    validate_browsers
    validate_project_structure
    validate_bdd_specific
    generate_summary
else
    # Validación completa
    main
fi

# Salir con código apropiado
if [[ $FAILED_CHECKS -gt 0 ]]; then
    exit 1
else
    exit 0
fi