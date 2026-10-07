# ✅ PRE-PUSH CHECKS - Verificar Todo Antes de Hacer Push

Ejecuta estos comandos **ANTES** de hacer `git push` para asegurar que todo está correcto.

---

## RESUMEN RÁPIDO

```bash
# OPCIÓN 1: Ejecutar todo de una vez (RECOMENDADO)
# En PowerShell (Windows):
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.12.1'; `
mvn -q clean compile; `
npm --prefix huellitas-frontend run lint; `
npm --prefix huellitas-frontend run prettier:check; `
npm --prefix huellitas-frontend run build; `
Write-Host "✅ Todas las validaciones pasaron!"

# O en Bash (Mac/Linux):
export JAVA_HOME=/path/to/java21
mvn -q clean compile && \
npm --prefix huellitas-frontend run lint && \
npm --prefix huellitas-frontend run prettier:check && \
npm --prefix huellitas-frontend run build && \
echo "✅ Todas las validaciones pasaron!"
```

---

## VALIDACIONES DETALLADAS

### 1️⃣ VERIFICAR JAVA 21

```bash
# Verificar que Java 21 está instalado
java -version
# Debe mostrar: java version "21.x.x"

# Verificar javac
javac -version
# Debe mostrar: javac 21.x.x

# Si no funciona, configurar JAVA_HOME:
# Windows PowerShell:
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12.1"

# Mac/Linux:
export JAVA_HOME=/usr/libexec/java_home -v 21
```

---

### 2️⃣ VERIFICAR MAVEN

```bash
# Verificar Maven
mvn -version
# Debe mostrar: Apache Maven 3.9.x

# Si falta, descargarlo desde: https://maven.apache.org/download.cgi
```

---

### 3️⃣ VERIFICAR NODE.JS

```bash
# Verificar Node.js (mínimo 22)
node --version
# Debe mostrar: v22.x.x

# Verificar npm (mínimo 10)
npm --version
# Debe mostrar: 10.x.x

# Si no está actualizado:
npm install -g npm@latest
```

---

### 4️⃣ VALIDACIÓN DEL BACKEND

#### 4.1 Compilar Backend

```bash
cd huellitas-backend

# Compilar (sin tests, rápido)
mvn clean compile

# Esperado: BUILD SUCCESS

# Alternativamente, compilar + tests (más lento pero más completo):
mvn clean verify
```

#### 4.2 Validar Checkstyle

```bash
cd huellitas-backend

# Validar código style
mvn checkstyle:check

# Esperado: No violations
```

#### 4.3 Generar JaCoCo Coverage

```bash
cd huellitas-backend

# Generar coverage report
mvn jacoco:report

# Archivo generado: target/site/jacoco/index.html
# Abre en navegador para ver cobertura
```

---

### 5️⃣ VALIDACIÓN DEL FRONTEND

#### 5.1 Instalar Dependencias

```bash
cd huellitas-frontend

# Instalar dependencias si no están
npm install

# Verificar no hay vulnerabilidades críticas
npm audit

# Si hay vulnerabilidades:
npm audit fix
```

#### 5.2 ESLint - Linting

```bash
cd huellitas-frontend

# Ejecutar ESLint
npm run lint

# Esperado: "All files pass linting."

# Si hay errores, auto-fix:
npm run lint:fix
```

#### 5.3 Prettier - Formateo

```bash
cd huellitas-frontend

# Verificar formateo (no modifica)
npm run prettier:check

# Esperado: "All matched files use Prettier code style!"

# Si hay errores, auto-fix:
npm run prettier:fix
```

#### 5.4 Build Frontend

```bash
cd huellitas-frontend

# Compilar para producción
npm run build

# Esperado: "Application bundle generation complete"

# Archivo generado: dist/huellitas-frontend/
```

#### 5.5 TypeScript - Verificación de Tipos

```bash
cd huellitas-frontend

# Verificar tipos TypeScript
npx tsc --noEmit

# Esperado: Sin errores
```

---

### 6️⃣ VALIDACIÓN DE GIT

#### 6.1 Estado de Git

```bash
# Ver cambios
git status

# Debe mostrar los archivos que planeabas cambiar
# NO debe haber archivos no deseados
```

#### 6.2 Revisar Cambios

```bash
# Ver diferencias
git diff

# Ver diferencias de un archivo específico
git diff huellitas-frontend/eslint.config.js

# Verifica que los cambios sean los esperados
```

#### 6.3 Verificar Rama Correcta

```bash
# Ver rama actual
git branch
# Debe ser: * main (NO otra rama)

# Ver últimos commits
git log --oneline -5
# Todos deben tener formato: HC-123: descripción
```

---

## CHECKLIST COMPLETO PRE-PUSH

Copia y pega este checklist:

```
BACKEND:
[ ] Java 21 instalado (java -version)
[ ] Maven instalado (mvn -version)
[ ] mvn clean compile ✅
[ ] mvn checkstyle:check ✅
[ ] mvn jacoco:report ✅

FRONTEND:
[ ] Node.js 22+ instalado (node --version)
[ ] npm 10+ instalado (npm --version)
[ ] npm install ✅
[ ] npm run lint ✅ (All files pass linting)
[ ] npm run prettier:check ✅ (All matched files use Prettier)
[ ] npm run build ✅ (Application bundle generation complete)
[ ] npx tsc --noEmit ✅ (Sin errores)

GIT:
[ ] git branch muestra: * main
[ ] git pull origin main ✅ (traí últimos cambios)
[ ] git status ✅ (cambios esperados)
[ ] git diff ✅ (cambios correctos)
[ ] git log --oneline -5 ✅ (commits con código Jira)

🔗 JIRA:
[ ] Mis commits tienen formato: "HC-XXX: descripción" ✅
[ ] Estoy en main (NO en otra rama) ✅
[ ] Push será directo a origin/main ✅

LISTO PARA PUSH:
[ ] Todos los checks pasaron
```

---

## SCRIPT AUTOMATIZADO (RECOMENDADO)

### Para Windows PowerShell:

Crea archivo `pre-push-check.ps1`:

```powershell
# pre-push-check.ps1

Write-Host "🔍 Iniciando validaciones pre-push..." -ForegroundColor Cyan

# Configurar Java 21
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12.1"

# Backend
Write-Host "`n📦 Validando Backend..." -ForegroundColor Yellow
cd huellitas-backend

Write-Host "  → Compilando..." -NoNewline
mvn clean compile -q
if ($LASTEXITCODE -eq 0) { Write-Host " ✅" } else { Write-Host " ❌"; exit 1 }

Write-Host "  → Checkstyle..." -NoNewline
mvn checkstyle:check -q
if ($LASTEXITCODE -eq 0) { Write-Host " ✅" } else { Write-Host " ❌"; exit 1 }

# Frontend
Write-Host "`n🎨 Validando Frontend..." -ForegroundColor Yellow
cd ../huellitas-frontend

Write-Host "  → Instalando dependencias..." -NoNewline
npm install -q
if ($LASTEXITCODE -eq 0) { Write-Host " ✅" } else { Write-Host " ❌"; exit 1 }

Write-Host "  → ESLint..." -NoNewline
npm run lint -q
if ($LASTEXITCODE -eq 0) { Write-Host " ✅" } else { Write-Host " ❌"; exit 1 }

Write-Host "  → Prettier..." -NoNewline
npm run prettier:check -q
if ($LASTEXITCODE -eq 0) { Write-Host " ✅" } else { Write-Host " ❌"; exit 1 }

Write-Host "  → Build..." -NoNewline
npm run build -q
if ($LASTEXITCODE -eq 0) { Write-Host " ✅" } else { Write-Host " ❌"; exit 1 }

Write-Host "`n✅ Todas las validaciones pasaron! Listo para push." -ForegroundColor Green
cd ..
```

Ejecutar:
```powershell
powershell -ExecutionPolicy Bypass -File pre-push-check.ps1
```

### Para Mac/Linux:

Crea archivo `pre-push-check.sh`:

```bash
#!/bin/bash

echo "🔍 Iniciando validaciones pre-push..."

# Configurar Java 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Backend
echo ""
echo "📦 Validando Backend..."
cd huellitas-backend

echo -n "  → Compilando... "
mvn clean compile -q && echo "✅" || { echo "❌"; exit 1; }

echo -n "  → Checkstyle... "
mvn checkstyle:check -q && echo "✅" || { echo "❌"; exit 1; }

# Frontend
echo ""
echo "🎨 Validando Frontend..."
cd ../huellitas-frontend

echo -n "  → Instalando dependencias... "
npm install -q && echo "✅" || { echo "❌"; exit 1; }

echo -n "  → ESLint... "
npm run lint -q && echo "✅" || { echo "❌"; exit 1; }

echo -n "  → Prettier... "
npm run prettier:check -q && echo "✅" || { echo "❌"; exit 1; }

echo -n "  → Build... "
npm run build -q && echo "✅" || { echo "❌"; exit 1; }

echo ""
echo "✅ Todas las validaciones pasaron! Listo para push."
cd ..
```

Ejecutar:
```bash
chmod +x pre-push-check.sh
./pre-push-check.sh
```

---

## SI ALGO FALLA

### Backend falla en compilación
```bash
cd huellitas-backend

# Limpiar caché
mvn clean

# Reintentar
mvn compile

# Si sigue fallando, revisar:
# - ¿Java 21 instalado?
# - ¿JAVA_HOME configurado?
# - ¿Maven versión 3.9+?
```

### Frontend falla en ESLint
```bash
cd huellitas-frontend

# Auto-fix
npm run lint:fix

# Revisar cambios
git diff
```

### Frontend falla en Prettier
```bash
cd huellitas-frontend

# Auto-fix
npm run prettier:fix

# Revisar cambios
git diff
```

### Frontend falla en build
```bash
cd huellitas-frontend

# Limpiar node_modules
rm -rf node_modules
npm install

# Reintentar build
npm run build
```

---

## COMANDOS FINALES ANTES DE PUSH

```bash
# 1. Asegurar que estés en main
git branch
# Debe mostrar: * main

# 2. Traer últimos cambios
git pull origin main

# 3. Ver cambios
git status
git diff

# 4. Ver que mis commits tienen código Jira
git log --oneline -5
# Formato: HC-123: descripción

# 5. Agregar cambios
git add .

# 6. Crear commit CON CÓDIGO JIRA
git commit -m "HC-123: tu descripción aquí

- Cambio 1
- Cambio 2"

# 7. Hacer push directo a main
git push origin main

# 8. Verificar en GitHub que se actualizó
# → GitHub Actions ejecuta automáticamente
# → Jira se actualiza automáticamente
```

---

## CHECKLIST FINAL

✅ Todas las validaciones pasaron
✅ Cambios son los esperados
✅ Rama es correcta
✅ Commit message es descriptivo
✅ LISTO PARA PUSH 🚀

---

## SOPORTE

Si algo no funciona:
1. Revisa este archivo
2. Ejecuta validaciones una por una
3. Pide ayuda al equipo con el error específico
4. No hagas push si las validaciones fallan

¡Gracias por mantener la calidad del código! 🙌
