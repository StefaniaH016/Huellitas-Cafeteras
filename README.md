# 🐾 Huellitas Cafeteras

> **Plataforma de Gestión y Seguimiento de Animales Vulnerables o Extraviados en el Eje Cafetero**

Una solución web integral para centralizar, priorizar y gestionar el bienestar animal en las regiones del Eje Cafetero (Quindío, Risaralda y Caldas). La plataforma conecta ciudadanos, fundaciones de rescate animal y veterinarias en un ecosistema coordinado de reportes, seguimiento médico y cierre de casos.

---

## 📋 Contenido

- [🎯 Propósito y Contexto](#-propósito-y-contexto)
- [🏗️ Arquitectura](#-arquitectura)
- [🛠️ Tecnologías](#-tecnologías)
- [📦 Requisitos Previos](#-requisitos-previos)
- [🚀 Instalación y Configuración](#-instalación-y-configuración)
- [📂 Estructura del Proyecto](#-estructura-del-proyecto)
- [👥 Equipo de Desarrollo](#-equipo-de-desarrollo)
- [📋 Política de Gestión de Configuración](#-política-de-gestión-de-configuración)
- [✅ Control de Calidad (SQA)](#-control-de-calidad-sqa)
- [🌐 Despliegue](#-despliegue)
- [📚 Documentación Adicional](#-documentación-adicional)
- [📞 Soporte y Contacto](#-soporte-y-contacto)

---

## 🎯 Propósito y Contexto

### Misión

Digitalizar y centralizar el registro, la priorización geográfica (municipio/barrio) y el seguimiento médico de animales en vulnerabilidad o extraviados, permitiendo una respuesta coordinada y eficiente entre actores de bienestar animal en el Eje Cafetero.

### Actores del Ecosistema

1. **Ciudadanos**: Reportan avistamientos de animales vulnerables o extraviados
2. **Fundaciones de Rescate**: Reciben y priorizan reportes, coordina rescates y atención inicial
3. **Veterinarias**: Registran bitácora médica, tratamientos y evolución del animal
4. **Administradores**: Supervisan la plataforma y generan reportes de impacto

### Objetivos Clave

- ✅ Centralizar todos los reportes de animales en vulnerabilidad en una única plataforma
- ✅ Priorizar casos por zona geográfica (municipio/barrio)
- ✅ Mantener una bitácora médica integral del animal desde rescate hasta cierre del caso
- ✅ Facilitar la coordinación entre fundaciones y veterinarias
- ✅ Generar reportes de impacto y seguimiento de casos

---

## 🏗️ Arquitectura

### Diagrama General

```
┌─────────────────────────────────────────────────────────────────────┐
│                         HUELLITAS CAFETERAS                         │
└─────────────────────────────────────────────────────────────────────┘

┌──────────────────┐         ┌────────────────────┐      ┌──────────────┐
│   FRONTEND SPA   │         │   BACKEND API      │      │  POSTGRESQL  │
│   Angular 21     │────────▶│   Spring Boot 4.1  │◀────▶│   Database   │
│   TypeScript     │         │   Java 21          │      │   Railway    │
│   Vite + Signals │         │   REST + JWT       │      │   (Cloud)    │
└──────────────────┘         └────────────────────┘      └──────────────┘
        │                              │                        │
        │                              ▼
        │                     ┌────────────────────┐
        │                     │   Cloudinary SDK   │
        │                     │  (Image Procesing) │
        │                     └────────────────────┘
        │
        ▼
   ┌─────────────┐
   │   Vercel    │
   │ Deployment  │
   └─────────────┘
```

### Capas de la Aplicación

#### **Frontend (Angular SPA)**
- **Interfaz de Usuario**: Componentes reutilizables, formularios reactivos
- **Roles**: Ciudadano, Fundación, Veterinaria, Administrador (UI adaptada por rol)
- **Gestión de Estado**: Angular Signals para reactividad
- **Comunicación**: HttpClient + JWT para consumir API REST
- **Validación**: ESLint, Prettier, TypeScript strict mode

#### **Backend (Spring Boot REST API)**
- **Servicios de Lógica de Negocio**: Gestión de casos, usuarios, notificaciones
- **Persistencia**: Spring Data JPA + Hibernate ORM
- **Seguridad**: Spring Security + JWT (JJWT 0.12.6)
- **Manejo de Errores**: GlobalExceptionHandler con respuestas JSON estandarizadas
- **Integración Externa**: Cloudinary para almacenamiento de imágenes optimizadas

#### **Base de Datos (PostgreSQL)**
- **Modelo Relacional**: Tablas con llaves primarias/foráneas e integridad referencial (ACID)
- **Índices**: Optimizados para consultas geoespaciales (municipio/barrio)
- **Persistencia en la Nube**: Railway para reliability y escalabilidad

---

## 🛠️ Tecnologías

### Backend
| Tecnología | Versión | Propósito |
|-----------|---------|----------|
| **Java** | 21 LTS | Lenguaje de programación |
| **Spring Boot** | 4.1.1 | Framework web y REST API |
| **Spring Data JPA** | latest | ORM e interacción con BD |
| **Hibernate** | latest | Mapeo objeto-relacional |
| **Spring Security** | latest | Autenticación y autorización |
| **JJWT** | 0.12.6 | Tokens JWT |
| **PostgreSQL Driver** | latest | Conexión a PostgreSQL |
| **Cloudinary Java SDK** | 1.39.0 | Procesamiento y almacenamiento de imágenes |
| **Lombok** | 1.18.34 | Anotaciones para reducir boilerplate |
| **JUnit 5** | latest | Testing unitario |
| **Mockito** | latest | Mocking en tests |
| **JaCoCo** | latest | Cobertura de código |

### Frontend
| Tecnología | Versión | Propósito |
|-----------|---------|----------|
| **Angular** | 21.2 | Framework SPA |
| **TypeScript** | 5.x | Lenguaje tipado para JS |
| **Vite** | latest | Build tool y bundler |
| **Angular Signals** | latest | Reactividad avanzada |
| **RxJS** | latest | Programación reactiva |
| **ESLint** | latest | Linting y análisis estático |
| **Prettier** | latest | Formateador de código |
| **Vitest** | latest | Testing framework |

### Base de Datos
| Tecnología | Versión | Propósito |
|-----------|---------|----------|
| **PostgreSQL** | 16 | Base de datos relacional |
| **Railway** | cloud | Hosting en la nube |

### DevOps e Infraestructura
| Tecnología | Versión | Propósito |
|-----------|---------|----------|
| **Docker** | latest | Contenedorización |
| **Docker Compose** | latest | Orquestación local |
| **GitHub Actions** | - | CI/CD pipeline |
| **Maven** | 3.9+ | Build tool (Backend) |
| **npm** | 10.0+ | Package manager (Frontend) |
| **Vercel** | cloud | Hosting Frontend |
| **Azure** | cloud | Hosting Backend |

### Análisis de Calidad
| Tecnología | Propósito |
|-----------|----------|
| **SonarCloud** | Análisis estático continuo |
| **Checkstyle** | Estándares de código Java |
| **JaCoCo** | Cobertura de tests (umbral 60%) |

---

## 📦 Requisitos Previos

### Sistema Operativo
- **Windows 10/11** (PowerShell 7+)
- **macOS** 12+
- **Linux** (Ubuntu 22.04 LTS recomendado)

### Software Requerido

#### 1. Java Development Kit (JDK) 21
```bash
# Descarga: https://www.oracle.com/java/technologies/downloads/#java21
# Verifica:
java -version       # Debe mostrar Java 21.x.x
javac -version
# Configura JAVA_HOME automáticamente (o manualmente si es necesario)
```

#### 2. Maven 3.9+
```bash
# Descarga: https://maven.apache.org/download.cgi
# Extrae y agrega /bin a tu PATH
# Verifica:
mvn -version        # Debe mostrar 3.9+
```

#### 3. Node.js 22+ (para Frontend)
```bash
# Descarga: https://nodejs.org
# Verifica:
node --version      # Debe mostrar 22.x.x
npm --version       # Debe mostrar 10.x.x
```

#### 4. Git
```bash
# Descarga: https://git-scm.com/downloads
# Verifica:
git --version
```

#### 5. Docker (para desarrollo local)
```bash
# Descarga: https://www.docker.com/products/docker-desktop
# Verifica:
docker --version
docker-compose --version
```

#### 6. IDE (Recomendado)
- **Visual Studio Code** (con extensiones: Java Pack, Angular, ESLint, Prettier, GitLens)
- **IntelliJ IDEA Community** (con plugins: Spring Boot, Angular, ESLint)

### Cuentas en la Nube (para deploy)
- **GitHub** (repositorio y CI/CD)
- **Vercel** (frontend deployment)
- **Railway** (base de datos PostgreSQL)
- **Azure** (backend deployment)
- **Cloudinary** (almacenamiento de imágenes)
- **Jira Software** (gestión de tareas)

---

## 🚀 Instalación y Configuración

### Paso 1: Clonar el Repositorio

```bash
git clone https://github.com/StefaniaH016/Huellitas-Cafeteras.git
cd Huellitas-Cafeteras
```

### Paso 2: Configurar Backend (Spring Boot)

```bash
cd huellitas-backend

# Descargar dependencias Maven
mvn clean install -DskipTests

# Compilar
mvn compile

# Resultado esperado: BUILD SUCCESS
```

**Nota:** Si necesitas ejecutar tests con BD real:

```bash
# Crea archivo .env en la raíz del proyecto o en huellitas-backend/
# (Ver sección de Variables de Entorno)
mvn test
```

### Paso 3: Configurar Frontend (Angular)

```bash
cd ../huellitas-frontend

# Instalar dependencias
npm install

# Verificar no hay vulnerabilidades críticas
npm audit

# Resultado esperado: No vulnerabilities found
```

### Paso 4: Configurar Variables de Entorno

Crea un archivo `.env` **en la raíz del proyecto** (`Huellitas-Cafeteras/.env`):

```env
# ===== PostgreSQL =====
POSTGRES_DB=huellitas_dev
PGUSER=postgres
PGPASSWORD=tu_contraseña_segura
POSTGRES_PASSWORD=tu_contraseña_segura

# ===== JWT (Backend) =====
JWT_SECRET=tu_clave_jwt_super_segura_de_minimo_256_bits
JWT_EXPIRATION=86400000  # 24 horas en milisegundos

# ===== Cloudinary (Imágenes) =====
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_API_KEY=tu_api_key
CLOUDINARY_API_SECRET=tu_api_secret

# ===== Base de Datos (para Railway en producción) =====
# DATABASE_URL=postgresql://user:pass@host:port/db
```

**Importante:** Nunca commitees el archivo `.env` en Git. Está en `.gitignore`.

### Paso 5: Ejecutar Docker Compose (Desarrollo Local)

```bash
# Desde la raíz del proyecto
docker-compose up -d

# Verifica que los 3 contenedores están corriendo:
docker ps

# Resultado esperado:
# CONTAINER                STATUS              PORTS
# huellitas-db            Up (healthy)        5432:5432
# huellitas-api           Up                  8080:8080, 8081:8080
# huellitas-web           Up                  4200:80
```

**URLs de desarrollo:**
- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080
- **Swagger API Docs**: http://localhost:8081/swagger-ui/index.html
- **PostgreSQL**: localhost:5432

### Paso 6: Verificar Instalación Completa

```bash
# Detén los contenedores si los ejecutaste
docker-compose down

# Ejecuta build de prueba
cd huellitas-backend
mvn clean compile
# Resultado: BUILD SUCCESS

cd ../huellitas-frontend
npm run build
# Resultado: ✓ compiled successfully
```

---

## 📂 Estructura del Proyecto

```
Huellitas-Cafeteras/
│
├── huellitas-backend/                  # Spring Boot REST API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/co/edu/uniquindio/huellitas/
│   │   │   │   ├── controller/         # Endpoints REST
│   │   │   │   ├── service/            # Lógica de negocio
│   │   │   │   ├── repository/         # Acceso a datos (JPA)
│   │   │   │   ├── model/              # Entidades JPA
│   │   │   │   ├── security/           # JWT y autenticación
│   │   │   │   ├── exception/          # Excepciones personalizadas
│   │   │   │   ├── util/               # Utilidades
│   │   │   │   └── config/             # Configuración Spring
│   │   │   └── resources/
│   │   │       └── application.yml     # Configuración de perfil
│   │   └── test/
│   │       ├── java/                   # Tests JUnit 5 + Mockito
│   │       └── resources/
│   │           └── application-test.properties
│   ├── pom.xml                         # Dependencias Maven
│   ├── Dockerfile                      # Imagen Docker del backend
│   ├── mvnw / mvnw.cmd                 # Maven wrapper
│   └── railway.toml                    # Config Railway
│
├── huellitas-frontend/                 # Angular SPA
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/                   # Servicios centrales (HTTP, Auth)
│   │   │   ├── shared/                 # Componentes reutilizables
│   │   │   ├── features/               # Módulos por rol/feature
│   │   │   │   ├── caso/               # Gestión de casos
│   │   │   │   ├── notificaciones/     # Notificaciones
│   │   │   │   ├── cuenta/             # Perfil de usuario
│   │   │   │   ├── panel-admin/        # Panel administrativo
│   │   │   │   ├── panel-fundacion/    # Panel de fundaciones
│   │   │   │   └── panel-veterinaria/  # Panel de veterinarias
│   │   │   ├── app.component.ts        # Componente raíz
│   │   │   └── app.routes.ts           # Rutas
│   │   ├── assets/                     # Imágenes, iconos, etc.
│   │   ├── styles.css                  # Estilos globales
│   │   └── main.ts                     # Entry point
│   ├── package.json                    # Dependencias npm
│   ├── angular.json                    # Configuración Angular
│   ├── tsconfig.json                   # Configuración TypeScript
│   ├── eslint.config.js                # Reglas ESLint
│   ├── prettier.config.js              # Configuración Prettier
│   ├── Dockerfile                      # Imagen Docker del frontend
│   └── .angular/                       # Caché de build
│
├── .github/
│   └── workflows/
│       ├── sqa-pipeline.yml            # CI/CD: Tests + SonarCloud
│       └── docker-push.yml             # Docker image build & push
│
├── DOCS/
│   ├── SETUP_LOCAL.md                  # Esta guía (detallada)
│   ├── PRE_PUSH_CHECKS.md              # Validaciones pre-commit
│   ├── JIRA_GIT_WORKFLOW.md            # Integración Jira + Git
│   └── RAILWAY_DEPLOYMENT.md           # Despliegue en Railway
│
├── Soft 3/                             # Documentación del proyecto (local)
│   ├── Definición del Proyecto.docx
│   ├── Documento de Arquitectura.docx
│   └── Informe Admin Configuración.docx
│
├── compose.yaml                        # Docker Compose (desarrollo local)
├── .env                                # Variables de entorno (NO COMMITEAR)
├── .gitignore                          # Archivos ignorados por Git
├── .dockerignore                       # Archivos ignorados por Docker
├── sonar-project.properties            # SonarCloud config
├── azure-pipelines.yml                 # Azure Pipelines (opcional)
├── pom.xml                             # POM root (si es necesario)
└── README.md                           # Este archivo
```

---

## 👥 Equipo de Desarrollo

| Rol | Integrante | Responsabilidad |
|-----|-----------|-----------------|
| **Diseñadora Frontend / Líder SQA / DevOps** | Stefania Herrera Ochoa | Diseño UI/UX, despliegue, control de calidad SQA |
| **Desarrollador Backend / Product Owner** | Daniel Valencia Arenas | Servicios, persistencia, API REST, requerimientos |
| **Desarrolladora Backend / Tester** | Allison López Carantón | Backend, testing, validación, reportes de bugs |

### Universidad
**Universidad del Quindío** — Programa de Ingeniería de Sistemas y Computación  
Asignatura: Ingeniería de Software III (2026-1)  
Docente: Cristhian David Henao Hoyos

---

## 📋 Política de Gestión de Configuración

### Estrategia de Ramificación (Git Branching Model)

El proyecto utiliza un modelo de 4 ramas principales:

```
┌─────────────────────────────────────────────────────┐
│                   MAIN (Producción)                 │
│            Vercel Deployment Automático             │
│        Require: SonarCloud + JaCoCo 60% + PR Review │
└─────────────────────────────────────────────────────┘
                        ▲
                        │ (Pull Request)
                        │
    ┌───────────────┬───┴────────────┬──────────────┐
    │               │                │              │
┌─────────┐   ┌─────────┐   ┌──────────┐   ┌────────────┐
│ Stefania│   │ Daniel  │   │ Allison  │   │ Otros Dev  │
│ (Branch)│   │(Branch) │   │ (Branch) │   │ (Branch)   │
└─────────┘   └─────────┘   └──────────┘   └────────────┘
```

### Políticas de Commits

**Formato Obligatorio del Mensaje:**
```
{CODIGO-JIRA}: {Descripción breve}

{Descripción detallada (opcional)}

- Cambio 1
- Cambio 2
```

**Ejemplos:**
```
feat(HC-45): Agregar UI de perfil de usuario
- Crear componente ProfileComponent
- Agregar formulario de edición

fix(HC-67): Corregir validación de email
- Email con punto no se validaba correctamente
- Actualizar regex con tests

(SQA) Implementación de excepciones personalizadas
- Crear GlobalExceptionHandler
- Refactorizar manejadores existentes
```

### Flujo de Pull Request (PR) y Revisión de Código

1. **Peer Review**: Otro integrante revisa cambios en la rama personal
2. **Validación Automatizada**: GitHub Actions ejecuta:
   - ✅ ESLint (Frontend) + Checkstyle (Backend)
   - ✅ Build: `mvn clean compile` + `npm run build`
   - ✅ Tests: `mvn test` + `npm test`
   - ✅ SonarCloud: Análisis estático
   - ✅ JaCoCo: Cobertura ≥ 60%
3. **Merge a main**: Solo si **todas las validaciones pasan**

### Gestión de Cambios y Issues

**Para reportar errores o solicitar features:**

1. Crear **Issue en GitHub** con descripción y evidencias
2. Crear **Tarea en Jira** con código (ej: HC-123)
3. Crear rama personal o trabajar en rama existente
4. Commit referenciando Jira: `git commit -m "HC-123: descripción"`
5. Pull Request con descripción del cambio
6. Esperar revisión y validaciones automáticas
7. Merge a `main` una vez aprobado

---

## ✅ Control de Calidad (SQA)

### Herramientas de Análisis Estático

#### Backend (Java)
| Herramienta | Propósito | Umbral |
|-----------|----------|--------|
| **Checkstyle** | Estándares de código Java | Configuración Google Code Style |
| **SonarCloud** | Análisis estático continuo | Sonarqube cloud |
| **JaCoCo** | Cobertura de tests | **Mínimo 60%** (bloqueante) |
| **JUnit 5** | Testing unitario | Ejecución automática en CI/CD |

#### Frontend (Angular/TypeScript)
| Herramienta | Propósito | Umbral |
|-----------|----------|--------|
| **ESLint** | Linting y análisis de código | Configuración angular-eslint |
| **Prettier** | Formateador de código | Consistencia automática |
| **SonarCloud** | Análisis estático continuo | Integración con GitHub |
| **Vitest** | Testing unitario | Ejecución automática en CI/CD |

### Pipeline CI/CD (GitHub Actions)

**Archivo:** `.github/workflows/sqa-pipeline.yml`

El pipeline ejecuta automáticamente en cada **push** y **pull request**:

```yaml
1. Build Backend
   └─ mvn clean compile
   └─ Resultado: JAR compilado

2. Build Frontend
   └─ npm run build
   └─ Resultado: Dist compilado

3. Tests Backend
   └─ mvn test (JUnit 5 + Mockito)
   └─ JaCoCo Report (Coverage ≥ 60%)

4. Tests Frontend
   └─ npm test (Vitest)
   └─ Coverage report

5. Linting
   └─ Backend: Checkstyle
   └─ Frontend: ESLint + Prettier

6. SonarCloud Analysis
   └─ Análisis estático integrado
   └─ Reporte de código duplicado, complejidad, vulnerabilidades

7. Deploy (solo en main)
   └─ Frontend → Vercel
   └─ Backend → Azure
   └─ BD → Railway
```

### Métricas de Calidad

| Métrica | Objetivo | Verificación |
|---------|----------|--------------|
| **Cobertura JaCoCo** | ≥ 60% | Pull request bloqueada si < 60% |
| **Complejidad Ciclomática** | Bajo | SonarCloud reporta métodos complejos |
| **Código Duplicado** | < 3% | SonarCloud identifica duplicación |
| **Vulnerabilidades de Seguridad** | 0 | SonarCloud + OWASP checks |
| **Deuda Técnica** | Mínima | SonarCloud rating A–E |
| **Tiempo de Respuesta API** | < 5s | Optimización de queries + índices |

### Checklist de Autoauditoría (SQA)

- ✅ Backend compila en Spring Boot 4.1.1 con Java 21
- ✅ Frontend se construye con Angular 21 sin errores
- ✅ Todo el código está versionado en GitHub
- ✅ Modelo de BD PostgreSQL documentado (Diagrama C4 Nivel 4)
- ✅ Tablas con llaves primarias/foráneas e integridad referencial ACID
- ✅ Excepciones personalizadas en GlobalExceptionHandler
- ✅ Herramientas de análisis estático integradas y auditadas en CI/CD
- ✅ Cobertura de código ≥ 60% (JaCoCo + JUnit 5)
- ✅ Autenticación con JWT Spring Security
- ✅ Validación de entradas (Backend + Frontend)

---

## 🌐 Despliegue

### Desarrollo Local

```bash
# Desde la raíz del proyecto
docker-compose up -d

# Verificar:
docker ps  # Debe mostrar 3 contenedores UP
```

**URLs:**
- Frontend: http://localhost:4200
- Backend: http://localhost:8080
- Swagger Docs: http://localhost:8081/swagger-ui/index.html
- PostgreSQL: localhost:5432 (pguser / pgpassword)

### Despliegue en Producción

| Componente | Plataforma | URL | Rama |
|-----------|-----------|-----|------|
| **Frontend** | Vercel | https://huellitas-cafeteras.vercel.app | main |
| **Backend** | Azure App Service | API RESTful | main |
| **BD PostgreSQL** | Railway | Cloud instance | main |

**Flujo Automático:**
1. Commit/PR a `main`
2. GitHub Actions ejecuta validaciones
3. Si todo pasa → Merge a `main`
4. GitHub Actions detecta merge a `main`
5. Despliegue automático a:
   - ✅ Vercel (Frontend)
   - ✅ Azure (Backend)
   - ✅ Railway (BD ya persistente)

### Variables de Entorno en Producción

Cada plataforma requiere variables de entorno. Configurar en:

- **Vercel**: Project Settings → Environment Variables
- **Azure**: App Service → Configuration → Application settings
- **Railway**: Service → Variables → Add Variable

**Variables requeridas:**
```
JWT_SECRET
JWT_EXPIRATION
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
POSTGRES_DB
POSTGRES_USER
POSTGRES_PASSWORD
DATABASE_URL
```

---

## 📚 Documentación Adicional

| Documento | Descripción |
|-----------|------------|
| [SETUP_LOCAL.md](DOCS/SETUP_LOCAL.md) | Guía detallada de instalación local |
| [PRE_PUSH_CHECKS.md](DOCS/PRE_PUSH_CHECKS.md) | Validaciones antes de push a main |
| [JIRA_GIT_WORKFLOW.md](DOCS/JIRA_GIT_WORKFLOW.md) | Integración Jira + Git workflow |
| [RAILWAY_DEPLOYMENT.md](DOCS/RAILWAY_DEPLOYMENT.md) | Despliegue de BD en Railway |

### Documentos de Proyecto (Local)
- `Soft 3/Definición del Proyecto.docx` — Especificación de requerimientos
- `Soft 3/Documento de Arquitectura.docx` — Diseño técnico (C4, ER, componentes)
- `Soft 3/Informe Admin Configuración.docx` — SCMP + SQA + Gestión de Cambios

---

## 📞 Soporte y Contacto

### Canales de Comunicación

| Medio | Propósito | Contacto |
|------|----------|---------|
| **GitHub Issues** | Reportar bugs y solicitar features | [Issues](https://github.com/StefaniaH016/Huellitas-Cafeteras/issues) |
| **Jira Software** | Gestión de tareas y sprints | [Jira Board](https://huellitas-cafeteras.atlassian.net) |
| **Pull Requests** | Revisión de código | [PRs](https://github.com/StefaniaH016/Huellitas-Cafeteras/pulls) |
| **Slack/Teams** | Comunicación del equipo | [Equipo] |

### Troubleshooting Común

#### ❌ Error: "Port 4200 already in use"
```bash
# Solución 1: Matar el proceso anterior
# Windows PowerShell:
Get-Process | Where-Object {$_.Port -eq 4200} | Stop-Process -Force

# Solución 2: Cambiar puerto
npm start -- --port 4300
```

#### ❌ Error: "Java 21 no encontrado"
```bash
# Configurar JAVA_HOME
# Windows PowerShell:
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12.1"
java -version

# Mac/Linux:
export JAVA_HOME=/path/to/jdk-21
java -version
```

#### ❌ Error: "Maven no compila"
```bash
# Limpiar caché
mvn clean

# Reinstalar dependencias
mvn clean install -DskipTests

# Si persiste, verificar Maven version
mvn -version  # Debe ser 3.9+
```

#### ❌ Error: "npm ERR! code ERESOLVE"
```bash
# Resolver dependencias con legacy flag
npm install --legacy-peer-deps

# O usar npm 7+
npm --version  # Debe ser 10.0+
```

#### ❌ Error: "Docker containers not starting"
```bash
# Verificar Docker daemon
docker ps

# Limpiar contenedores previos
docker-compose down -v

# Verificar logs
docker logs huellitas-api
docker logs huellitas-db

# Reintentar
docker-compose up -d
```

---

## 📄 Licencia

Proyecto académico — Universidad del Quindío (2026-1)

---

## 🙏 Créditos

**Equipo:**
- Stefania Herrera Ochoa (Diseñadora Frontend / Líder SQA / DevOps)
- Daniel Valencia Arenas (Desarrollador Backend / Product Owner)
- Allison López Carantón (Desarrolladora Backend / Tester)

**Docente:**
- Cristhian David Henao Hoyos — Ingeniería de Software III

**Tecnologías y Herramientas:**
- Spring Boot, Angular, PostgreSQL, Docker, GitHub Actions, Vercel, Railway, Cloudinary, SonarCloud, JUnit 5, Vitest, ESLint, Prettier

---

## 📝 Historial de Cambios

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | 2026-11-05 | README inicial con arquitectura, setup, y políticas de SQA |

---

**Última actualización:** 5 de noviembre de 2026

Para contribuciones, reportar issues o preguntas, consulta los [issues](https://github.com/StefaniaH016/Huellitas-Cafeteras/issues) o contacta al equipo de desarrollo.

---

*Plataforma desarrollada con ❤️ para mejorar el bienestar animal en el Eje Cafetero.*
