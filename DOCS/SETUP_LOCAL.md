# 🚀 SETUP LOCAL - Guía de Configuración Inicial

Sigue esta guía para clonar el repositorio y tener todo configurado correctamente en tu máquina.

---

## REQUISITOS PREVIOS

Antes de empezar, **asegúrate de tener instalados:**

### Windows / Mac / Linux

#### 1. **Java Development Kit (JDK) 21**
   - **Descarga:** https://www.oracle.com/java/technologies/downloads/#java21
   - **Instalación:**
     ```bash
     # Windows: Ejecuta el instalador
     # Verifica la instalación:
     java -version
     javac -version
     ```
   - **Versión mínima:** Java 21.0.12.1 LTS
   - **JAVA_HOME:** Debe estar configurado automáticamente

#### 2. **Maven 3.9+**
   - **Descarga:** https://maven.apache.org/download.cgi
   - **Instalación:**
     ```bash
     # Extrae el archivo .zip
     # Agrega a PATH: /path/to/maven/bin
     # Verifica:
     mvn -version
     ```
   - **Versión mínima:** 3.9.0

#### 3. **Node.js 22+ (para frontend)**
   - **Descarga:** https://nodejs.org (LTS version 22 o superior)
   - **Instalación:**
     ```bash
     # Ejecuta el instalador
     # Verifica:
     node --version
     npm --version
     ```
   - **Versión mínima Node:** 22.0.0
   - **Versión mínima npm:** 10.0.0

#### 4. **Git**
   - **Descarga:** https://git-scm.com/downloads
   - **Verifica:**
     ```bash
     git --version
     ```

#### 5. **IDE (Recomendado)**
   - **Visual Studio Code** https://code.visualstudio.com
   - **IntelliJ IDEA Community** https://www.jetbrains.com/idea/

#### 6. **Jira Integration (Para tracking de tareas)**
   - **Jira Account:** Solicita acceso a tu líder de equipo
   - **Jira URL:** https://huellitas-cafeteras.atlassian.net (o la que uses)
   - **Jira CLI opcional:** https://github.com/ankitpokhrel/jira-cli (instalable vía Homebrew o npm)
   - **Kiro o Antigravity
---

## PASO 1: Clonar el Repositorio

```bash
git clone https://github.com/StefaniaH016/Huellitas-Cafeteras.git
cd Huellitas-Cafeteras
```

---

## PASO 2: Configurar Backend

```bash
cd huellitas-backend

# Descargar dependencias Maven
mvn clean install -DskipTests

# Verificar compilación
mvn compile
```

**Resultado esperado:**
```
[INFO] BUILD SUCCESS
```

---

## PASO 3: Configurar Frontend

```bash
cd ../huellitas-frontend

# Instalar dependencias Node.js
npm install

# Verificar instalación
npm list
```

**Resultado esperado:**
```
No vulnerabilities found
```

---

## PASO 4: Configurar Variables de Entorno (Opcional - Para Tests con Railway)

Crea un archivo `.env` en la raíz del proyecto (o en el backend):

```bash
# En huellitas-backend/
DATABASE_URL=postgresql://user:password@localhost:5432/huellitas
DATABASE_USER=postgres
DATABASE_PASSWORD=tu_contraseña
DATABASE_DRIVER=org.postgresql.Driver
```

**Nota:** Si no tienes PostgreSQL local, los tests se saltarán automáticamente. Esto es normal mientras trabajas en desarrollo.

---

## PASO 5: Verificar la Configuración Completa

Ejecuta el script de validación (ver archivo `PRE_PUSH_CHECKS.md`):

```bash
# En la raíz del proyecto
./pre-push-checks.sh  # Mac/Linux
# O en Windows PowerShell:
# Ejecuta los comandos manualmente del archivo
```

---

## ESTRUCTURA DEL PROYECTO

```
Huellitas-Cafeteras/
├── huellitas-backend/          # Spring Boot + Java 21
│   ├── src/main/java/          # Código principal
│   ├── src/test/java/          # Tests JUnit5
│   ├── pom.xml                 # Dependencias Maven
│   └── Dockerfile              # Para despliegue
│
├── huellitas-frontend/         # Angular 21
│   ├── src/                    # Código TypeScript/HTML
│   ├── package.json            # Dependencias npm
│   ├── angular.json            # Config Angular
│   ├── eslint.config.js        # Linting rules
│   └── tsconfig.*.json         # TypeScript config
│
├── .github/workflows/          # GitHub Actions CI/CD
│   └── sqa-pipeline.yml        # Tests + SonarCloud
│
├── compose.yaml                # Docker Compose (desarrollo)
├── sonar-project.properties    # SonarCloud config
└── README.md
```

---

## IDE SETUP (Visual Studio Code - Recomendado)

### Extensiones Recomendadas
1. **Java Extension Pack** (Microsoft)
2. **Angular Language Service** (Angular)
3. **ESLint** (Microsoft)
4. **Prettier - Code formatter** (Prettier)
5. **GitLens** (Eric Amodio)
6. **Jira and Bitbucket (Official)** (Atlassian) ⭐ **NUEVA**

### Instalar extensiones:
```bash
code --install-extension ms-vscode.java-pack
code --install-extension Angular.ng-template
code --install-extension dbaeumer.vscode-eslint
code --install-extension esbenp.prettier-vscode
code --install-extension eamodio.gitlens
code --install-extension atlassian.atlascode  # Jira integration
```

### Configurar Jira en VS Code:
1. Click en extensión Jira (lado izquierdo)
2. Click en "Authorize" 
3. Ingresa credenciales de Jira
4. ¡Listo! Ahora puedes ver y crear issues desde VS Code

---

## IDE SETUP (IntelliJ IDEA Community)

### Plugins Recomendados
1. **Kotlin** (built-in)
2. **Spring Boot** (built-in)
3. **Angular and TypeScript** (built-in)
4. **ESLint** (Settings → Plugins → Browse → ESLint)
5. **Prettier** (Settings → Plugins → Browse → Prettier)
6. **Jira Integration** (Settings → Plugins → Browse → Jira) ⭐ **NUEVA**

### Configurar:
- Settings → Build, Execution, Deployment → Build Tools → Maven
  - Maven home path: `/path/to/maven`
  - Java version: 21
- Settings → Tools → Jira
  - URL: Ingresa tu Jira URL
  - Autoriza con tu cuenta

---

## COMANDOS DE DESARROLLO

### Backend
```bash
cd huellitas-backend

# Compilar
mvn clean compile

# Ejecutar tests
mvn test

# Ejecutar con coverage
mvn clean verify

# Generar JAR
mvn package

# Ejecutar aplicación (local)
mvn spring-boot:run
```

### Frontend
```bash
cd huellitas-frontend

# Instalar dependencias
npm install

# Ejecutar en desarrollo
npm start

# Compilar para producción
npm run build

# Ejecutar linter
npm run lint

# Formatear con Prettier
npm run prettier:fix

# Ejecutar tests
npm test

# Ejecutar tests con coverage
npm run test:coverage
```

---

## TROUBLESHOOTING

### Problema: "Java 21 no encontrado"
```bash
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12.1"

# Mac/Linux
export JAVA_HOME=/path/to/jdk-21

# Verifica
java -version
```

### Problema: "npm command not found"
```bash
# Reinstala Node.js desde https://nodejs.org
# Luego verifica:
node --version
npm --version
```

### Problema: "Maven no compila"
```bash
# Limpia caché
mvn clean

# Verifica Maven
mvn -version

# Si aún falla, descarga Maven nuevamente
```

### Problema: "Port 4200 already in use" (Frontend)
```bash
# Frontend usa puerto 4200 por defecto
# Cambiar puerto:
npm start -- --port 4300
```

---

## PRÓXIMOS PASOS

1. ✅ Clona el repo
2. ✅ Sigue este SETUP_LOCAL.md
3. ✅ Verifica todo con PRE_PUSH_CHECKS.md
4. ✅ Cuando estés listo para colaborar:
   - Crea tu rama: `git checkout -b feature/tu-feature`
   - Haz cambios
   - Ejecuta validaciones pre-push
   - Haz push

---

## 🔗 WORKFLOW CON JIRA (IMPORTANTE)

Esta sección es **CRÍTICA** para tu equipo. Todos deben seguirlo.

### PASO 1: Obtener Código de Tarea en Jira

En Jira, cada tarea tiene un código (ej: `SCRUM-123`, `HC-45`):

1. Ve a tu Jira board
2. Abre la tarea en la que trabajarás
3. Copia el código de la tarea (ej: `HC-123`)

### PASO 2: Hacer Commits Directamente en Main

No necesitas crear rama. Los commits van directamente a `main`:

```bash
# Git ya está en main
git pull origin main  # Traer últimos cambios

# Hacer cambios...

# Commit CON código Jira
git commit -m "HC-123: Descripción del cambio

- Cambio 1
- Cambio 2"

# Push directo a main
git push origin main
```

**Patrón de mensaje:**
```
{CODIGO-JIRA}: {Descripción breve}

{Descripción detallada si es necesario}
```

**Siempre comienza con:** `HC-123:` o el código Jira que uses

---

## 📋 EJEMPLOS COMPLETOS

### Ejemplo 1: Agregar Nueva Feature

```bash
# 1. Obtener código Jira: HC-45

# 2. Traer últimos cambios de main
git pull origin main

# 3. Hacer cambios locales...

# 4. Commits directamente en main con referencia Jira
git commit -m "HC-45: Agregar UI de perfil de usuario

- Crear componente ProfileComponent
- Agregar formulario de edición
- Integrar con API"

# 5. Push directo a main
git push origin main

# 6. GitHub Actions ejecuta automáticamente
# → ESLint pasa ✅
# → Prettier pasa ✅
# → Build pasa ✅

# 7. Jira ve el commit automáticamente
# → HC-45 se actualiza
```

### Ejemplo 2: Bugfix

```bash
# 1. Obtener código Jira: HC-67

# 2. Traer cambios
git pull origin main

# 3. Hacer cambios...

# 4. Commits
git commit -m "HC-67: Corregir validación de email

- Email con punto no se validaba correctamente
- Actualizar regex
- Tests actualizados"

# 5. Push directo
git push origin main

# 6. CI/CD ejecuta automáticamente
# 7. Jira marcado como resuelto
```

### Ejemplo 3: Múltiples Commits en una Tarea

```bash
# Todos los commits hacen referencia a la misma tarea (HC-89)

git commit -m "HC-89: Crear modelo User

- Agregar entidad User
- Configurar repositorio"

git commit -m "HC-89: Implementar endpoints de usuario

- GET /users
- POST /users
- GET /users/{id}"

git commit -m "HC-89: Agregar tests para endpoints

- Tests unitarios
- Tests de integración"

# Un push que incluye todos
git push origin main

# Todos se verán en Jira bajo la tarea HC-89
```

---

## ⚙️ CONFIGURACIÓN GIT GLOBAL (Opcional pero Recomendado)

Para no olvidar el código Jira en cada commit, configura un template:

```bash
# Crear archivo de template
cat > ~/.gitmessage << EOF
# {CODIGO-JIRA}: {Descripción breve}
#
# Descripción detallada aquí
#
# - Cambio 1
# - Cambio 2
EOF

# Configurar Git para usar este template
git config --global commit.template ~/.gitmessage
```

Ahora cada `git commit` mostrará este template como sugerencia.

---

## ✅ CHECKLIST JIRA + GIT PARA CADA TAREA

Antes de hacer push a `main`, verifica:

```
[ ] Obtuve el código Jira (ej: HC-123)
[ ] Estoy en rama main: git branch
[ ] Traí últimos cambios: git pull origin main
[ ] Todos mis commits tienen el formato: "HC-123: descripción"
[ ] Ejecuté PRE_PUSH_CHECKS.md
[ ] Listo para hacer push: git push origin main
```

---

## 🎯 BENEFICIOS DE HACER ESTO

✅ **Trazabilidad completa:** Cada línea de código vinculada a una tarea Jira
✅ **Historial limpio:** Git + Jira sincronizados automáticamente
✅ **Visibilidad del equipo:** Todos ven qué se está haciendo
✅ **Reportes automáticos:** Jira genera reportes de progreso
✅ **Auditoría:** Saber quién hizo qué y cuándo

---

## SOPORTE

- **Issues con Jira:** Contacta a tu Scrum Master
- **Issues con Git:** Ver sección Troubleshooting
- **Issues con conexión Jira-GitHub:** Verificar configuración en GitHub
