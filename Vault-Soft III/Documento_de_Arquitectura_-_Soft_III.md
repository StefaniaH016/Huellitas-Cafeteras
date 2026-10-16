# Documento de Arquitectura - Software III

> **Fuente:** `Documento_de_Arquitectura_-_Soft_III.pdf` (título interno: "Documento de Arquitectura - Soft III"), 12 páginas.
> **Mapa de páginas:** p. 1 = portada · p. 2–8 = ADR-001 a ADR-010 · p. 9 = Modelo C4 (Contexto y Contenedores) · p. 10 = Contenedores (versión sin herramientas de CI/CD) y Componentes · p. 11 = Estructura del repositorio · p. 12 = Diagrama de clases/entidades.
> Las marcas `[PDF p. N]` indican la página donde inicia cada bloque. Los diagramas del PDF son imágenes; su descripción abajo es una transcripción de lo que se lee en ellos. Los puntos dudosos están marcados con ⚠️ y listados en la sección final.
> **Revisión 2:** las relaciones del diagrama de clases (p. 12) se contrastaron con la imagen de mayor resolución `huellitas_diagrama_dominio_final.png`, aportada como respaldo visual. Ver la subsección "Relaciones" y las notas finales.

---

## Portada `[PDF p. 1]`

- Documento de Arquitectura - Software III
- Stefania Herrera Ochoa — C.C. 1090274506
- Allison Lopez Caranton — C.C 1088827979
- Daniel Valencia Arenas — C.C 1091202998
- *(Logo de la Universidad del Quindío)*
- Cristhian David Henao Hoyos *(aparece solo, bajo el logo, sin rol indicado en el documento)*
- Universidad del Quindío
- Facultad de Ingeniería
- Ingeniería de sistemas y computación
- Software III
- Armenia, Quindío, 2026

---

## Decisiones de Arquitectura (ADRs) `[PDF p. 2]`

### ADR-001: Lenguaje de Programación para el Backend `[p. 2]`

| Campo | Contenido |
|---|---|
| Estado | Aceptado |
| Contexto | Para desarrollar la API de Huellitas Cafeteras se necesita un lenguaje seguro, organizado y que todo el equipo domine bien. |
| Decisión | Se utilizará Java |
| Alternativas | 1. Python<br>2. C# |
| Justificación | Es un lenguaje con el que ya se tiene experiencia, ofrece mucho control con el manejo de datos, gracias al fuerte tipado, y se integra perfecto con las herramientas de Spring Boot que se usaran. |
| Consecuencias | **Positivas:** Código tipado, mantenible y con amplio soporte técnico.<br>**Negativas:** Requiere escribir mayor cantidad de código estructurado en comparación con lenguajes de tipado dinámico como Python o JavaScript |

### ADR-002: Definición del Estilo de Arquitectura del Sistema `[p. 2–3]`

| Campo | Contenido |
|---|---|
| Estado | Aceptado |
| Contexto | El proyecto necesita una estructura clara para separar la presentación, la lógica de negocio y el acceso a datos sin generar complejidad en el desarrollo del producto. |
| Decisión | Se adoptará un estilo de Arquitectura en Capas (Monolito Modular). |
| Alternativas | 1. Arquitectura de Microservicios<br>2. Arquitectura Hexagonal |
| Justificación | La arquitectura en capas permite una separación de responsabilidades simples de mantener para el tamaño actual del equipo y del alcance del proyecto, sin añadir la complejidad de infraestructura, orquestación y latencia de red que conllevan, por ejemplo, los microservicios. |
| Consecuencias | **Positivas:** Desarrollo, pruebas y despliegue centralizados y sencillos.<br>**Negativas:** Si el sistema crece de manera masiva, el escalamiento debe hacerse de forma global y no por componentes independientes. *(la cláusula de "Negativas" continúa en p. 3)* |

### ADR-003: Selección del Motor de Base de Datos `[p. 3]`

| Campo | Contenido |
|---|---|
| Estado | Aceptado |
| Contexto | Se requiere almacenar información estructurada de usuarios, reportes y bitácoras médicas garantizando consistencia y relaciones estrictas. |
| Decisión | Se utilizará la base de datos relacional PostgreSQL. |
| Alternativas | 1. Oracle<br>2. MariaDB |
| Justificación | Garantiza la consistencia e integridad de los datos mediante el cumplimiento de propiedades ACID, permitiendo consultar la información de forma eficiente. |
| Consecuencias | **Positivas:** Datos estructurados seguros y sin duplicidad.<br>**Negativas:** Menor flexibilidad si la estructura de los datos cambia drásticamente. |

### ADR-004: Selección del Protocolo de Comunicación `[p. 3]`

| Campo | Contenido |
|---|---|
| Estado | Aceptado |
| Contexto | El cliente web (Angular) y el servidor (Spring Boot) necesitan comunicarse de manera ligera, estandarizada y desacoplada. |
| Decisión | Se utilizará API RESTful mediante protocolo HTTP/HTTPS con formato JSON. |
| Alternativas | 1. SOAP con formato XML |
| Justificación | REST sobre HTTP es el estándar más compatible, fácil de probar e integrar entre cliente y servidor. |
| Consecuencias | **Positivas:** Fácil consumo desde Angular y herramientas de pruebas de API.<br>**Negativa:** Posible sobrecarga de peticiones en vistas que puedan ser complejas. |

### ADR-005: Estrategia de Autenticación de Usuarios `[p. 4]`

| Campo | Contenido |
|---|---|
| Estado | Aceptado |
| Contexto | Se requiere autenticar y controlar accesos de forma segura para los 4 roles del sistema, que serán: Ciudadano, Fundación, Veterinaria, Admin. Sin mantener sesión en servidor. |
| Decisión | Se implementará autenticación stateless con tokens JWT (JSON Web Token). |
| Alternativas | 1. API Keys<br>2. Autenticación Básica HTTP<br>3. Autenticación con proveedores externos |
| Justificación | Permite validar roles e identidades en cada petición sin recargar la base de datos ni depender de memoria de sesión en el servidor. |
| Consecuencias | **Positiva:** Arquitectura desacoplada, ligera y escalable.<br>**Negativa:** Requiere lógica adicional para invalidar tokens antes de su expiración. |

### ADR-006: Integración con Servicios Externos de Despliegue en Nube `[p. 4–5]`

| Campo | Contenido |
|---|---|
| Estado | Aceptado |
| Contexto | Se requiere publicar la plataforma en internet de forma automatizada para pruebas y demostración. |
| Decisión | Se integrará con Vercel para el frontend y Railway para el backend y base de datos. |
| Alternativas | 1. AWS / Azure<br>2. Render / Netlify<br>3. Despliegue en un servidor local |
| Justificación | Facilita el despliegue continuo (CI/CD) directo desde el repositorio con costos nulos o mínimos y configuración rápida. |
| Consecuencias | **Positivas:** Publicación inmediata y mantenimiento de infraestructura automatizado.<br>**Negativas:** Dependencia de los límites de consumo de los planes gratuitos. *(las "Negativas" están en p. 5)* |

### ADR-007: Entorno de Desarrollo Local con Docker Compose `[p. 5]`

| Campo | Contenido |
|---|---|
| Estado | Aceptado |
| Contexto | Para evitar fallos por inconsistencias de versiones y dependencias entre las máquinas del equipo de desarrollo, se requiere estandarizar el entorno local para el backend y la base de datos. |
| Decisión | Se utilizará Docker Compose para orquestar los contenedores locales de PostgreSQL y la API en Java/Spring Boot. |
| Alternativas | 1. Uso de máquinas virtuales (VMs). |
| Justificación | Docker Compose permite levantar los servicios de base de datos y backend de manera idéntica en cualquier equipo mediante un único archivo de configuración (docker-compose.yml), garantizando un entorno aislado y rápido de configurar. |
| Consecuencias | **Positivas:** Entorno de desarrollo unificado, rápida incorporación de desarrolladores y despliegue local replicable<br>**Negativas:** Requiere consumo adicional de recursos de RAM/CPU en las máquinas locales para ejecutar el daemon de Docker. |

### ADR-008: Estrategia para Almacenamiento de Fotos y Archivos `[p. 5–6]`

| Campo | Contenido |
|---|---|
| Estado | Aceptado |
| Contexto | El registro de casos exige adjuntar fotografías del animal para su identificación, por tanto, guardar directamente estos archivos en la base de datos relacional puede afectar el rendimiento. |
| Decisión | Se utilizará un servicio de almacenamiento en la nube (Cloudinary), guardando únicamente la URL pública de referencia en PostgreSQL. |
| Alternativas | 1. Amazon S3<br>2. Guardar archivos localmente en el servidor<br>3. Guardar en Base de Datos (BLOB) |
| Justificación `[p. 6]` | Almacenar sólo la URL optimiza el rendimiento de PostgreSQL, reduce el tamaño de las copias de seguridad y permite servir las imágenes de forma rápida a la interfaz en Angular. |
| Consecuencias `[p. 6]` | **Positivas:** Carga rápida de la plataforma y base de datos ligera.<br>**Negativas:** Dependencia de un servicio externo para consultar las imágenes. |

### ADR-009: Selección de Herramientas para el Aseguramiento de Calidad de Código (SQA) `[p. 7]`

| Campo | Contenido |
|---|---|
| Estado | Aceptado |
| Contexto | Se requiere garantizar que el código fuente de Huellitas Cafeteras cumpla con los estándares de calidad, legibilidad, seguridad y mantenibilidad tanto en el backend como en el frontend, sin consumir la memoria ni los recursos de los servidores de producción/despliegue en Railway o Vercel |
| Decisión | Se utilizará Checkstyle para el backend (Java / Spring Boot), ESLint para el frontend (Angular / TypeScript) y SonarCloud (versión en la nube de SonarQube) integrado con GitHub Actions para el análisis estático continuo y la gestión automatizada de la deuda técnica. |
| Alternativas | 1. PMD y FindBugs<br>2. SpotBugs y StyleCop<br>3. SonarLint como extensión independiente en IDE sin servidor centralizado |
| Justificación | Checkstyle y ESLint permiten verificar el cumplimiento de reglas y estándares de estilo durante el desarrollo local en Java y Angular. SonarCloud ofrece el mismo motor de análisis estático que SonarQube para medir seguridad, duplicación y cobertura de pruebas, pero con la ventaja de ser 100% gratuito para repositorios públicos de GitHub, automatizarse mediante GitHub Actions y no consumir recursos de infraestructura local o remota. |
| Consecuencias | **Positivas:** Cero costo de infraestructura para el servidor de calidad, análisis automático en cada Pull Request antes de hacer merge, estandarización de estilo en todo el equipo y prevención de vulnerabilidades de seguridad.<br>**Negativas:** Dependencia de la disponibilidad del servicio externo de SonarCloud y necesidad de configurar secretos (SONAR_TOKEN) y workflows en GitHub Actions. |

### ADR-010: Estrategia de Pruebas y Cobertura de Código `[p. 7–8]`

> El título aparece al final de la p. 7; el cuerpo de la tabla inicia en la p. 8.

| Campo | Contenido |
|---|---|
| Estado | Aceptado |
| Contexto | Se requiere garantizar que Huellitas Cafeteras cumpla un umbral mínimo de cobertura de pruebas del 60 %, tal como lo establece el objetivo de CI/CD del proyecto. Las herramientas definidas en el ADR-009 (Checkstyle, ESLint y SonarCloud) analizan la calidad del código, pero no ejecutan pruebas ni generan reportes de cobertura. |
| Decisión | Se utilizará JUnit 5 y Mockito para las pruebas unitarias y Spring Boot Test para las pruebas de integración en el backend, con JaCoCo para medir la cobertura. En el frontend se utilizará Vitest, el ejecutor de pruebas incluido por defecto en Angular, con su reporte de cobertura. Las pruebas se ejecutarán en cada Pull Request mediante GitHub Actions, y SonarCloud validará el umbral del 60 % mediante un Quality Gate. |
| Alternativas | 1. TestNG y Cobertura 2. Jasmine y Karma en el frontend 3. Pruebas manuales sin medición automatizada *(en el original aparecen en una sola línea, sin saltos)* |
| Justificación | JUnit 5, Mockito y Spring Boot Test son el estándar del ecosistema Spring Boot y se integran directamente con Maven. JaCoCo genera el reporte de cobertura que SonarCloud lee para calcular el porcentaje. Vitest viene configurado por defecto en las versiones recientes de Angular |
| Consecuencias | **Positivas:** Verificación automática del 60 % de cobertura en cada Pull Request, bloqueo del merge si no se cumple el umbral y detección temprana de errores.<br>**Negativas:** Mayor tiempo de desarrollo para escribir las pruebas y mayor duración del pipeline en cada ejecución. |

---

## Modelo C4 `[PDF p. 9]`

### Diagrama de Contexto del Sistema `[p. 9]`

*(Imagen. Transcripción de su contenido.)*

**Personas** (4, cada una con flecha hacia el sistema):

| Persona | Etiqueta de la relación hacia el sistema |
|---|---|
| Ciudadano `[Persona]` | Reporta casos y confirma reencuentros |
| Fundación / Refugio `[Persona]` | Asume custodia y coordina atención |
| Veterinaria `[Persona]` | Registra bitácora médica y alta |
| Administrador `[Persona]` | Verifica entidades y modera contenido |

**Sistema central:** **Huellitas Cafeteras** `[Sistema de software]` — "Gestión de casos de animales vulnerables o extraviados".

**Sistemas externos** (flechas salientes desde Huellitas Cafeteras):

| Sistema externo | Etiqueta de la relación |
|---|---|
| Servicio de correo `[Sistema externo]` | Envía notificaciones |
| Cloudinary `[Sistema externo]` | Guarda y sirve fotos de los casos |

### Diagrama de Contenedores `[p. 9]`

*(Imagen. Versión con herramientas de desarrollo y entrega.)*

**Elementos:**

- **Usuarios** — "4 roles del sistema" (Persona).
- Dentro del límite **"Huellitas Cafeteras"**:
  - **Aplicación web** — Angular (SPA), Vercel.
  - **API REST** — Java + Spring Boot, Railway.
  - **Base de datos** — PostgreSQL, Railway.
- Externos: **Servicio de correo** (Envío de correos) y **Cloudinary** (Fotos y archivos).
- Recuadro **"Desarrollo y entrega (herramientas fuera del runtime)"**:
  - **Docker Compose · local** — PostgreSQL + API Spring Boot.
  - **GitHub Actions (CI/CD) → Vercel / Railway** — JUnit 5 · Vitest · JaCoCo · Checkstyle · ESLint · SonarCloud.
- Leyenda: Persona · Contenedor · Sistema externo · Herramienta de desarrollo / CI-CD.

**Relaciones:**

| Origen | Destino | Etiqueta |
|---|---|---|
| Usuarios | Aplicación web | HTTPS |
| Aplicación web | API REST | REST/JSON + JWT |
| API REST | Base de datos | JDBC |
| API REST | Servicio de correo | SMTP / API |
| API REST | Cloudinary | HTTPS (SDK) |

### Diagrama de Contenedores (versión simplificada) `[p. 10]`

Al inicio de la p. 10 aparece, sin título propio, una segunda imagen del diagrama de contenedores. Contiene los mismos elementos y relaciones anteriores **excepto** el recuadro "Desarrollo y entrega" y su entrada de leyenda; en esta versión la relación Aplicación web → API REST se etiqueta solo "REST/JSON" (sin "+ JWT" visible). Leyenda: Persona · Contenedor · Sistema externo.

### Diagrama de Componentes `[p. 10]`

*(Imagen. Transcripción de su contenido.)*

**Flujo general:** Aplicación web (Angular) → `HTTPS · REST/JSON · JWT` → bloque **API REST · Spring Boot**.

Dentro de **API REST · Spring Boot**:

| Capa / bloque | Contenido textual |
|---|---|
| **Transversal** | Security: JwtFilter, SecurityConfig · DTOs y Mappers · GlobalExceptionHandler |
| **Controllers** | AuthController · UsuarioController · CasoController · ZonaController · SeguimientoController · NotificacionController · ReporteController |
| **Services** | AuthService · UsuarioService · CasoService · SeguimientoService · BitacoraMedicaService · ZonaService |
| **Repositories (Spring Data JPA)** | Usuario · Caso · Seguimiento · Bitácora · Notificación · Zona |
| **Workers (@Async / @Scheduled)** | W-01 Notificaciones (@Async)<br>W-02 Organización y filtros<br>W-03 Reportes PDF<br>W-04 Cierre de casos (@Scheduled)<br>W-05 Mapeo de zonas<br>W-06 Imágenes (Cloudinary) |

**Relaciones (flechas):**

| Origen | Destino | Etiqueta |
|---|---|---|
| Aplicación web (Angular) | Transversal / API REST | HTTPS · REST/JSON · JWT |
| Transversal | Controllers | — |
| Controllers | Services | — |
| Services | Repositories | — |
| Services | Workers | — |
| Repositories | PostgreSQL | JDBC |
| Workers | Cloudinary | HTTPS · SDK |
| Workers | Correo externo | SMTP / API |

Leyenda: Capas · Workers (servicios internos) · Transversal o externo.

### Diagrama de Código / Entidad-Relación `[p. 11–12]`

El PDF titula el apartado "Diagrama de Código / Entidad-Relación" (p. 11). Esa página contiene la estructura de carpetas del repositorio; el diagrama de clases/entidades está en la p. 12, **sin título propio**.

#### Estructura del repositorio `[p. 11]`

```text
huellitas-cafeteras/
├── docker-compose.yml            PostgreSQL + backend (desarrollo local · Docker Compose)
├── .github/workflows/            CI/CD: pruebas, JaCoCo, Checkstyle, ESLint, SonarCloud
├── backend/                      Spring Boot (Java) · Maven
│   ├── pom.xml                   Spring Boot, JPA, JWT, JaCoCo, Checkstyle
│   ├── checkstyle.xml            Reglas de estilo Java
│   ├── Dockerfile
│   └── src/
│       ├── main/java/com/huellitas/cafeteras/
│       │   ├── controller/       Auth, Usuario, Caso, Seguimiento, ...
│       │   ├── service/          Reglas de negocio
│       │   ├── worker/           @Async y @Scheduled (W-01 a W-06)
│       │   ├── repository/       Spring Data JPA
│       │   ├── entity/           Usuario, Caso, Bitácora, Zona, ...
│       │   ├── dto/
│       │   ├── mapper/
│       │   ├── security/         JwtFilter, SecurityConfig (JWT)
│       │   └── exception/        GlobalExceptionHandler
│       ├── main/resources/       application.yml, scripts SQL
│       └── test/java/            JUnit 5 · Mockito · Spring Boot Test (cobertura JaCoCo)
└── frontend/                     Angular (SPA)
    ├── package.json              Dependencias, scripts de ESLint y Vitest
    ├── eslint.config.js          Reglas de estilo TypeScript
    └── src/app/
        ├── core/                 Servicios HTTP, interceptor JWT, guards
        ├── shared/               Componentes reutilizables
        └── features/
            ├── auth/
            ├── casos/            Registro, feed y filtros
            ├── seguimiento/      Bitácora y custodia
            └── admin/            Verificación y moderación
```

*(Reconstruido a partir de la imagen. En el original, `worker/` aparece resaltado en azul. Los árboles de `frontend/` se muestran con indentación ligeramente distinta; la jerarquía aquí es la que se infiere visualmente.)*

#### Diagrama de clases / entidades `[p. 12]`

*(Imagen del PDF de baja resolución; atributos leídos tras ampliarla y vueltos a contrastar con `huellitas_diagrama_dominio_final.png`: los atributos, nombres de clase y tipos coinciden en las 16 clases, sin diferencias. Todos los atributos aparecen con prefijo `-`, es decir, privados. Los compartimentos de métodos están vacíos.)*

**Organizacion** `«abstract»`
- `-Long id`
- `-String nombre`
- `-String nit`
- `-String direccion`
- `-EstadoVerificacion estadoVerificacion`
- `-String documentoCredencialUrl`
- `-Instant fechaVerificacion`

**Fundacion** (hereda de Organizacion)
- `-int capacidadAlbergue`
- `-boolean aceptaCustodia`

**Veterinaria** (hereda de Organizacion)
- `-String registroProfesional`
- `-String especialidad`
- `-String horarioAtencion`

**Usuario**
- `-Long id`
- `-String nombre`
- `-String email`
- `-String passwordHash`
- `-String telefono`
- `-Rol rol`
- `-boolean activo`
- `-boolean disponibleRescate`
- `-boolean aceptoTratamientoDatos`
- `-Instant fechaAceptacionDatos`

**Animal**
- `-Long id`
- `-String nombre`
- `-Especie especie`
- `-String raza`
- `-String color`
- `-Sexo sexo`
- `-String edadAproximada`
- `-String senasParticulares`

**Caso**
- `-Long id`
- `-TipoCaso tipoCaso`
- `-String descripcion`
- `-Urgencia urgencia`
- `-EstadoCaso estado`
- `-String direccionReferencia`
- `-Instant fechaRegistro`
- `-Instant fechaUltimaActividad`
- `-Instant fechaCustodia`
- `-Instant fechaArchivado`
- `-boolean visible`
- `-String motivoModeracion`
- `-Long version`

**CierreCaso**
- `-Long id`
- `-ResultadoCierre resultado`
- `-String entregadoA`
- `-String observaciones`
- `-Instant fecha`

**Notificacion**
- `-Long id`
- `-TipoNotificacion tipo`
- `-String mensaje`
- `-boolean leida`
- `-Instant fecha`

**Foto**
- `-Long id`
- `-String url`
- `-String publicId`
- `-boolean principal`

**Seguimiento**
- `-Long id`
- `-TipoAporte tipo`
- `-String comentario`
- `-Instant fecha`
- `-boolean visible`

**BitacoraMedica**
- `-Long id`
- `-Instant fechaApertura`
- `-boolean altaMedica`
- `-Instant fechaAlta`

**RegistroMedico**
- `-Long id`
- `-String diagnostico`
- `-String procedimiento`
- `-String tratamiento`
- `-String evolucion`
- `-Instant fecha`
- `-Instant fechaFirma`

**Barrio**
- `-Long id`
- `-String nombre`

**Municipio**
- `-Long id`
- `-String nombre`

**Departamento**
- `-Long id`
- `-String nombre`

**Tipos enumerados referenciados pero no definidos en el documento** (solo aparece su nombre como tipo de atributo): `EstadoVerificacion`, `Rol`, `Especie`, `Sexo`, `TipoCaso`, `Urgencia`, `EstadoCaso`, `ResultadoCierre`, `TipoNotificacion`, `TipoAporte`. Sus valores **no** están en el PDF.

**Relaciones** (etiqueta tal como aparece en el diagrama)

Verificadas visualmente contra `huellitas_diagrama_dominio_final.png`, siguiendo cada línea desde su origen hasta su punta de flecha o rombo y leyendo la multiplicidad junto a cada extremo. Convenciones gráficas observadas en la imagen: punta de flecha abierta (→) en el extremo destino de las asociaciones; rombo relleno (◆) en el extremo de **Caso** o **BitacoraMedica** en las composiciones; triángulo hueco en el extremo de **Organizacion** en la herencia.

| Etiqueta en el diagrama | Origen (mult.) → Destino (mult.) | Marcador en el extremo | Estado |
|---|---|---|---|
| *(sin etiqueta)* | Fundacion → Organizacion | Triángulo hueco en Organizacion (generalización); sin multiplicidades | ✅ Verificada |
| *(sin etiqueta)* | Veterinaria → Organizacion | Triángulo hueco en Organizacion (generalización); sin multiplicidades | ✅ Verificada |
| pertenece a | Usuario (`*`) → Organizacion (`0..1`) | Flecha en Organizacion | ✅ Verificada |
| verificada por admin | Organizacion (`*`) → Usuario (`0..1`) | Flecha en Usuario | ✅ Verificada |
| propietario | Animal (`*`) → Usuario (`0..1`) | Flecha en Usuario | ✅ Verificada |
| confirmado por | CierreCaso (`*`) → Usuario (`1`) | Flecha en Usuario | ✅ Verificada |
| custodia | Fundacion (`0..1`) → Caso (`*`) | Flecha en Caso | ✅ Verificada |
| protagoniza | Animal (`1`) → Caso (`*`) | Flecha en Caso | ✅ Verificada |
| reporta | Usuario (`1`) → Caso (`*`) | Flecha en Caso | ✅ Verificada |
| recibe | Usuario (`1`) → Notificacion (`*`) | Flecha en Notificacion | ✅ Verificada |
| sobre | Notificacion (`*`) → Caso (`0..1`) | Flecha en Caso | ✅ Verificada |
| autor | Usuario (`1`) → Seguimiento (`*`) | Flecha en Seguimiento | ✅ Verificada |
| firmado por | RegistroMedico (`*`) → Usuario (`1`) | Flecha en Usuario | ✅ Verificada |
| zona de interes | Usuario (`*`) → Municipio (`0..1`) | Flecha en Municipio | ✅ Verificada |
| duplicado de | Caso (`*`) → Caso (`0..1`) (auto-relación) | Flecha en Caso | ✅ Verificada |
| abre | Veterinaria (`1`) → BitacoraMedica (`*`) | Flecha en BitacoraMedica | ✅ Verificada |
| sede | Organizacion (`*`) → Municipio (`1`) | Flecha en Municipio | ✅ Verificada |
| ubicado en | Caso (`*`) → Barrio (`1`) | Flecha en Barrio | ✅ Verificada |
| *(sin etiqueta)* | Caso (`1`) ◆— CierreCaso (`0..1`) | Rombo relleno en Caso (composición); sin flecha | ✅ Verificada |
| *(sin etiqueta)* | Caso (`1`) ◆— Foto (`*`) | Rombo relleno en Caso (composición); sin flecha | ✅ Verificada |
| *(sin etiqueta)* | Caso (`1`) ◆— Seguimiento (`*`) | Rombo relleno en Caso (composición); sin flecha | ✅ Verificada |
| *(sin etiqueta)* | Caso (`1`) ◆— BitacoraMedica (`0..1`) | Rombo relleno en Caso (composición); sin flecha | ✅ Verificada |
| *(sin etiqueta)* | BitacoraMedica (`1`) ◆— RegistroMedico (`*`) | Rombo relleno en BitacoraMedica (composición); sin flecha | ✅ Verificada |
| *(sin etiqueta)* | Barrio (`*`) → Municipio (`1`) | Flecha en Municipio | ✅ Verificada |
| *(sin etiqueta)* | Municipio (`*`) → Departamento (`1`) | Flecha en Departamento | ✅ Verificada |

Ninguna relación del diagrama de clases permanece ambigua tras esta revisión.

---

## Notas de verificación de la conversión

**Páginas procesadas:** las 12 (1–12).

**Requieren verificación manual:**

1. **Relaciones del diagrama de clases (p. 12) — resuelto en la revisión 2:** las relaciones antes marcadas con ⚠️ (`sede`, `verificada por admin`, `propietario`, `confirmado por`, `firmado por`, `zona de interes` y Caso–CierreCaso, además de las de certeza media) se contrastaron con `huellitas_diagrama_dominio_final.png`, de mayor resolución. Los extremos y multiplicidades coinciden con la lectura previa; no fue necesario corregir ninguna relación. Cambios en el Markdown: se eliminaron los ⚠️ de esa tabla, se añadieron las columnas de marcador por extremo y se retiró la columna de "certeza". La imagen aportada es un archivo separado del PDF; el PDF original no se modificó y su diagrama (p. 12) sigue siendo la fuente documental.
2. **Título del diagrama de la p. 11–12:** el PDF titula "Diagrama de Código / Entidad-Relación" sobre la estructura de carpetas (p. 11); el diagrama de clases (p. 12) no tiene título. No es un diagrama entidad-relación estricto, sino de clases (atributos con visibilidad `-`).
3. **Valores de los enums** (`Rol`, `EstadoCaso`, `Urgencia`, etc.) y **tipos de cardinalidad completos:** no están definidos en el documento.
4. **Árbol de carpetas (p. 11):** transcrito de una imagen; la indentación original del bloque `frontend/` es irregular y la jerarquía mostrada es la inferida visualmente. Los comentarios de cada línea sí son literales.
5. **Segundo diagrama de contenedores (p. 10):** sin título propio en el PDF; se identificó por su contenido.
6. **Cristhian David Henao Hoyos (p. 1):** aparece sin cédula ni rol; no se asumió ninguno.
7. **Texto original conservado sin corregir:** "Negativa/Negativas" (singular y plural mezclados), "que se usaran" (ADR-001), "Alternativas" de ADR-010 en una sola línea, y puntuación faltante al final de algunas celdas (p. ej. "despliegue local replicable", "versiones recientes de Angular").
8. **Inconsistencia pendiente de aclaración (sin resolver, no se unificaron nombres):** ADR-005 nombra los roles como "Ciudadano, Fundación, Veterinaria, Admin", mientras el documento de requisitos (RF) usa "Ciudadano/Voluntario, Fundación/Refugio, Veterinaria/Profesional de Salud, Administrador". Los diagramas C4 del documento de arquitectura usan "Ciudadano", "Fundación / Refugio", "Veterinaria" y "Administrador". En el diagrama de clases, el atributo `Rol rol` de `Usuario` no tiene sus valores definidos. No hay evidencia en los documentos para decidir cuál denominación es la correcta; requiere aclaración del equipo.
9. **Contenido no presente en el PDF:** no hay endpoints, criterios de aceptación ni definiciones de los workers W-01…W-06 más allá de los nombres que aparecen en los diagramas.
