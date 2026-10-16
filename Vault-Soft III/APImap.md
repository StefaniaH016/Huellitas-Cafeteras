# API Map – Huellitas Cafeteras (HC)

Mapeo de la API REST consumida por el frontend **Angular** y expuesta por el backend **Spring Boot**, con persistencia en **PostgreSQL**. Define el contrato de comunicación: qué datos se envían al servidor (**Request**) y qué devuelve (**Response**).

Los módulos se derivan de los **RF-01 a RF-18**, los **RNF-01 a RNF-14**, la definición del proyecto, la arquitectura y las historias HU 8, HU 10, HU 11 y HU 12. Se mantiene la estructura del API Map de referencia, pero **ningún recurso de PuntoVenta Ágil forma parte de este documento**.

> **Fuentes del proyecto:**
> - `Soft III - Formulacion proyecto.pdf` — problema y propósito.
> - `Soft III - Definición del Proyecto.pdf` — alcance, actores, procesos y Workers W-01 a W-06.
> - `RF y RNF.docx.pdf` — RF-01 a RF-18 y RNF-01 a RNF-14.
> - `Documento de Arquitectura - Soft III.pdf` — ADR y arquitectura de componentes.
> - Jira Huellitas Cafeteras — distribución de HU 8, 10, 11 y 12.
> - Repositorio `StefaniaH016/Huellitas-Cafeteras` — estructura y contratos en desarrollo.

> **Estado del contrato:** especificación funcional propuesta, **no inventario de endpoints ya operativos**. Las rutas y campos que aún no están definidos de forma definitiva por el backend deben acordarse con el equipo antes de implementarse. Los ejemplos JSON muestran tipos y reglas, como el archivo de referencia.

---

## Notación Base de la API

```text
Protocolo : HTTP / HTTPS (HTTPS en despliegue)
Prefijo   : /api/v1/
Formato   : JSON (Content-Type: application/json)
Auth      : Bearer Token (JWT) en Authorization para endpoints protegidos
            Authorization: Bearer <token>
Imágenes  : multipart/form-data para archivos; URLs almacenadas mediante Cloudinary
```

**Atención de integración:** el `application.yml` revisado define `server.servlet.context-path: /api`; si un controlador también declara `/api/v1`, la ruta pública puede terminar en `/api/api/v1`. El contrato de este documento usa **una sola vez** `/api/v1` y exige ajustar o verificar el context path.

### Convenciones de Tipos JSON

| Tipo PostgreSQL / Java | Tipo JSON | Observación |
| :--- | :--- | :--- |
| `BIGINT` / `Long` | `number (long)` | Identificadores generados por el servidor |
| `VARCHAR` / `TEXT` / `String` | `string` | Longitudes según DTO y entidad |
| `INTEGER` / `Integer` | `number (int)` | Conteos y paginación |
| `BOOLEAN` / `Boolean` | `boolean` | `true` o `false` |
| `TIMESTAMP` / `OffsetDateTime` | `string (ISO 8601)` | Ej.: `2026-10-14T14:30:00-05:00` |
| `ENUM` Java | `string` | Valores exactos del enum del backend |
| Relación JPA | `number (long)` | En Request se envía ID; el servidor valida existencia |
| Imagen Cloudinary | `string (URL)` | No se guarda binario en el JSON de casos |

### Estructura de Respuesta de Error

**Formato objetivo propuesto**, sujeto a la implementación del manejador global de excepciones:

```json
{
  "error": "string — descripción del error",
  "codigo": "string — identificador interno, ej. CASO-NO-ENCONTRADO",
  "detalle": "string | null — información adicional"
}
```

**Códigos HTTP comunes:** `200` consulta/actualización, `201` creación, `204` operación sin body, `400` datos inválidos, `401` sin autenticación, `403` sin permisos, `404` recurso inexistente, `409` conflicto y `500` error interno.

**Roles funcionales:** `ADMINISTRADOR`, `CIUDADANO_VOLUNTARIO`, `FUNDACION_REFUGIO` y `VETERINARIA`. Estos nombres son **etiquetas del mapa**; sus literales JWT definitivos se toman del enum real del backend. Las entidades institucionales requieren verificación administrativa para acciones exclusivas.

---

## Módulo 1 — Autenticación y sesión (`/api/v1/auth`)

*Cubre: RF-01, RF-02; W-03, RNF-04 y RNF-05.*

---

### POST `/api/v1/auth/registro`

Registra un usuario ciudadano; el alta de entidades institucionales queda sujeta a verificación.

**Acceso:** Público.

**Request Body:**
```json
{
  "nombre": "string — requerido",
  "correo": "string — requerido, único",
  "contrasena": "string — requerido",
  "telefono": "string | null",
  "barrioId": "number (long) | null",
  "rol": "CIUDADANO_VOLUNTARIO"
}
```

**Response 201:**
```json
{
  "id": "number (long)",
  "nombre": "string",
  "correo": "string",
  "rol": "CIUDADANO_VOLUNTARIO",
  "verificado": false
}
```

**Respuestas de error relevantes:** 400 validación; 409 correo duplicado.

---

### POST `/api/v1/auth/login`

Autentica con credenciales y devuelve JWT.

**Acceso:** Público.

**Request Body:**
```json
{
  "correo": "string — requerido",
  "contrasena": "string — requerido"
}
```

**Response 200:**
```json
{
  "token": "string — JWT",
  "tipo": "Bearer",
  "usuario": {"id": "number (long)", "nombre": "string", "rol": "string", "verificado": "boolean"}
}
```

**Respuestas de error relevantes:** 401 credenciales inválidas.

---

### GET `/api/v1/auth/me`

Consulta identidad y permisos efectivos del usuario autenticado. **Ruta propuesta**, verificar contra controlador de autenticación.

**Acceso:** Autenticado.

**Request:** Sin body.

**Response 200:**
```json
{
  "id": "number (long)",
  "nombre": "string",
  "correo": "string",
  "rol": "string",
  "verificado": "boolean"
}
```

**Respuestas de error relevantes:** 401 token inválido.

---

## Módulo 2 — Usuarios y perfiles (`/api/v1/usuarios`)

*Cubre: RF-01 y RF-04.*

---

### GET `/api/v1/usuarios/me`

Obtiene el perfil propio; no expone contraseña ni credenciales.

**Acceso:** Autenticado.

**Request:** Sin body.

**Response 200:**
```json
{
  "id": "number (long)",
  "nombre": "string",
  "correo": "string",
  "telefono": "string | null",
  "rol": "string",
  "ubicacion": {"departamentoId": "number (long) | null", "municipioId": "number (long) | null", "barrioId": "number (long) | null"}
}
```

---

### PUT `/api/v1/usuarios/me`

Actualiza información permitida del propio perfil.

**Acceso:** Autenticado.

**Request Body:**
```json
{
  "nombre": "string",
  "telefono": "string | null",
  "barrioId": "number (long) | null"
}
```

**Response 200:**
```json
{
  "id": "number (long)",
  "nombre": "string",
  "telefono": "string | null",
  "barrioId": "number (long) | null"
}
```

**Respuestas de error relevantes:** 400 datos inválidos; 404 barrio inexistente.

---

## Módulo 3 — Verificación de fundaciones y veterinarias (`/api/v1/verificaciones`)

*Cubre: RF-03, W-03.*

---

### GET `/api/v1/verificaciones/pendientes`

Lista solicitudes institucionales pendientes de validación.

**Acceso:** Administrador.

**Request:** Sin body.

**Response 200:**
```json
[
  {"usuarioId": "number (long)", "nombreEntidad": "string", "rolSolicitado": "FUNDACION_REFUGIO | VETERINARIA", "estado": "PENDIENTE"}
]
```

---

### PATCH `/api/v1/verificaciones/{usuarioId}`

Aprueba o rechaza la habilitación de una entidad, dejando trazabilidad.

**Acceso:** Administrador.

**Request Body:**
```json
{
  "decision": "APROBAR | RECHAZAR",
  "observacion": "string | null"
}
```

**Response 200:**
```json
{
  "usuarioId": "number (long)",
  "estado": "APROBADA | RECHAZADA",
  "verificado": "boolean"
}
```

**Respuestas de error relevantes:** 404 solicitud no encontrada; 409 solicitud ya resuelta.

---

## Módulo 4 — Catálogos de ubicación (`/api/v1/ubicaciones`)

*Cubre: RF-06, HU 11; W-05.*

---

### GET `/api/v1/ubicaciones/departamentos`

Lista departamentos disponibles en la cobertura del Eje Cafetero.

**Acceso:** Lectura pública según configuración de seguridad.

**Request:** Sin body.

**Response 200:**
```json
[
  {"id": "number (long)", "nombre": "string"}
]
```

---

### GET `/api/v1/ubicaciones/municipios`

Lista municipios del departamento indicado.

**Acceso:** Lectura pública.

**Parámetros:**
- `departamentoId` — number (long), requerido

**Request:** Sin body.

**Response 200:**
```json
[
  {"id": "number (long)", "nombre": "string", "departamentoId": "number (long)"}
]
```

**Respuestas de error relevantes:** 404 departamento inexistente.

---

### GET `/api/v1/ubicaciones/barrios`

Lista barrios del municipio indicado.

**Acceso:** Lectura pública.

**Parámetros:**
- `municipioId` — number (long), requerido

**Request:** Sin body.

**Response 200:**
```json
[
  {"id": "number (long)", "nombre": "string", "municipioId": "number (long)"}
]
```

**Respuestas de error relevantes:** 404 municipio inexistente.

---

## Módulo 5 — Captura y registro de casos (`/api/v1/casos`)

*Cubre: RF-05, RF-06, RF-07; HU 10, HU 11; W-05 y W-06.*

---

### POST `/api/v1/casos`

Registra un reporte de mascota extraviada o animal vulnerable. El servidor obtiene el autor del JWT, valida ubicación y urgencia y persiste el caso. **La implementación de ReporteService sigue pendiente en la revisión del repositorio**.

**Acceso:** Ciudadano/Voluntario autenticado.

**Request Body:**
```json
{
  "tipoCaso": "string — enum del backend, requerido",
  "urgencia": "string — enum del backend, requerido",
  "descripcion": "string — requerida",
  "barrioId": "number (long) — requerido",
  "animal": {
    "nombre": "string | null",
    "especie": "string — enum Especie, requerido",
    "raza": "string | null",
    "color": "string | null",
    "sexo": "string | null — enum Sexo",
    "edadAproximada": "string | null",
    "senasParticulares": "string | null"
  },
  "fotografias": ["string — URL Cloudinary; integración por definir"]
}
```

**Response 201:**
```json
{
  "id": "number (long)",
  "tipoCaso": "string",
  "urgencia": "string",
  "estado": "string — estado inicial del caso",
  "descripcion": "string",
  "autorId": "number (long)",
  "barrioId": "number (long)",
  "fechaCreacion": "string (ISO 8601)"
}
```

**Respuestas de error relevantes:** 400 validaciones; 401 sin JWT; 403 rol no permitido; 404 barrio inexistente.

---

### POST `/api/v1/imagenes`

Sube una fotografía de un animal a Cloudinary mediante W-06. **Contrato propuesto:** el campo `archivo` viaja como multipart y no como JSON.

**Acceso:** Autenticado; propietario o rol autorizado si se asocia a un caso.

**Request Body:**
```json
{
  "archivo": "binary — campo multipart/form-data (no JSON real)",
  "casoId": "number (long) | null — asociación opcional"
}
```

**Response 201:**
```json
{
  "url": "string — URL segura de Cloudinary",
  "publicId": "string — identificador del recurso"
}
```

**Respuestas de error relevantes:** 400 archivo inválido; 413 archivo demasiado grande; 502 error proveedor de imágenes.

---

## Módulo 6 — Feed, filtros y detalle de casos (`/api/v1/casos`)

*Cubre: RF-10, RF-11, RF-12; HU 12; W-02.*

---

### GET `/api/v1/casos`

Devuelve casos activos ordenados y paginados. Los filtros son combinables y el Worker W-02 utiliza consultas JPA optimizadas.

**Acceso:** Autenticado según seguridad actual; revisar si feed será público.

**Parámetros:**
- `page` — number (int), opcional, sugerido 0
- `size` — number (int), opcional, sugerido 20
- `sort` — string, opcional; orden por fecha o urgencia
- `departamentoId` — number (long), opcional
- `municipioId` — number (long), opcional
- `barrioId` — number (long), opcional
- `estado` — string, opcional
- `especie` — string, opcional
- `urgencia` — string, opcional
- `tipoCaso` — string, opcional

**Request:** Sin body.

**Response 200:**
```json
{
  "content": [
    {"id": "number (long)", "tipoCaso": "string", "estado": "string", "urgencia": "string", "descripcion": "string", "especie": "string", "fotoPrincipal": "string (URL) | null", "municipio": "string", "barrio": "string", "fechaCreacion": "string (ISO 8601)"}
  ],
  "page": "number (int)",
  "size": "number (int)",
  "totalElements": "number (long)",
  "totalPages": "number (int)",
  "last": "boolean"
}
```

**Respuestas de error relevantes:** 400 filtro o paginación inválidos.

---

### GET `/api/v1/casos/{id}`

Consulta detalle completo de un caso, fotografías, ubicación, historial, aportes y resumen médico disponible.

**Acceso:** Autenticado; datos personales sujetos a permisos.

**Parámetros:**
- `id` — number (long), requerido

**Request:** Sin body.

**Response 200:**
```json
{
  "id": "number (long)",
  "tipoCaso": "string",
  "estado": "string",
  "urgencia": "string",
  "descripcion": "string",
  "animal": {"nombre": "string | null", "especie": "string", "raza": "string | null", "color": "string | null"},
  "ubicacion": {"departamento": "string", "municipio": "string", "barrio": "string"},
  "fotografias": ["string — URL Cloudinary"],
  "historial": [{"fecha": "string (ISO 8601)", "estado": "string", "observacion": "string | null"}],
  "aportes": [{"id": "number (long)", "descripcion": "string"}],
  "resumenMedico": "object | null"
}
```

**Respuestas de error relevantes:** 404 caso no encontrado.

---

## Módulo 7 — Gestión y seguimiento del caso (`/api/v1/casos`)

*Cubre: RF-08, proceso de negocio 2.*

---

### PATCH `/api/v1/casos/{id}/estado`

Actualiza el estado del caso según transiciones permitidas y permisos del rol. Cada cambio debe quedar en historial.

**Acceso:** Responsable o entidad autorizada.

**Request Body:**
```json
{
  "estado": "string — valor válido según ciclo de vida",
  "observacion": "string — motivo o descripción"
}
```

**Response 200:**
```json
{
  "id": "number (long)",
  "estado": "string",
  "fechaActualizacion": "string (ISO 8601)"
}
```

**Respuestas de error relevantes:** 403 sin autorización; 404 caso no encontrado; 409 transición no permitida.

---

### GET `/api/v1/casos/{id}/historial`

Consulta eventos cronológicos de seguimiento del caso.

**Acceso:** Autenticado.

**Request:** Sin body.

**Response 200:**
```json
[
  {"id": "number (long)", "estadoAnterior": "string | null", "estadoNuevo": "string", "observacion": "string | null", "fecha": "string (ISO 8601)"}
]
```

---

## Módulo 8 — Aportes, comentarios y avistamientos (`/api/v1/casos/{id}/aportes`)

*Cubre: RF-09, RF-12.*

---

### GET `/api/v1/casos/{id}/aportes`

Lista aportes de la comunidad asociados al caso.

**Acceso:** Autenticado.

**Request:** Sin body.

**Response 200:**
```json
[
  {"id": "number (long)", "casoId": "number (long)", "descripcion": "string", "ubicacionTexto": "string | null", "fotografias": ["string (URL)"], "fecha": "string (ISO 8601)"}
]
```

---

### POST `/api/v1/casos/{id}/aportes`

Registra comentario o avistamiento, sin modificar automáticamente el estado del caso.

**Acceso:** Ciudadano/Voluntario autenticado.

**Request Body:**
```json
{
  "descripcion": "string — requerido",
  "ubicacionTexto": "string | null",
  "barrioId": "number (long) | null",
  "fotografias": ["string (URL)"]
}
```

**Response 201:**
```json
{
  "id": "number (long)",
  "casoId": "number (long)",
  "autorId": "number (long)",
  "fecha": "string (ISO 8601)"
}
```

**Respuestas de error relevantes:** 400 datos inválidos; 404 caso inexistente.

---

## Módulo 9 — Bitácora médica y atención veterinaria (`/api/v1/casos/{id}/bitacora`)

*Cubre: RF-13, RF-14, RF-15; proceso de negocio 2.*

---

### POST `/api/v1/casos/{id}/bitacora`

Abre una bitácora médica vinculada al caso; operación exclusiva de veterinaria verificada.

**Acceso:** Veterinaria verificada.

**Request Body:**
```json
{
  "observacionInicial": "string — requerida"
}
```

**Response 201:**
```json
{
  "id": "number (long)",
  "casoId": "number (long)",
  "veterinariaId": "number (long)",
  "fechaApertura": "string (ISO 8601)",
  "estado": "ABIERTA"
}
```

**Respuestas de error relevantes:** 403 entidad no verificada; 409 bitácora existente.

---

### GET `/api/v1/casos/{id}/bitacora`

Consulta bitácora e historial de evoluciones, sujeto a permisos y privacidad.

**Acceso:** Veterinaria autorizada; consulta resumida según permisos.

**Request:** Sin body.

**Response 200:**
```json
{
  "id": "number (long)",
  "casoId": "number (long)",
  "estado": "string",
  "evoluciones": [{"id": "number (long)", "fecha": "string (ISO 8601)", "diagnostico": "string", "tratamiento": "string", "observaciones": "string | null"}],
  "alta": "object | null"
}
```

---

### POST `/api/v1/casos/{id}/bitacora/evoluciones`

Agrega un registro clínico cronológico, conservando el historial.

**Acceso:** Veterinaria verificada responsable.

**Request Body:**
```json
{
  "diagnostico": "string — requerido",
  "procedimientos": "string | null",
  "tratamiento": "string | null",
  "medicamentos": "string | null",
  "observaciones": "string | null"
}
```

**Response 201:**
```json
{
  "id": "number (long)",
  "bitacoraId": "number (long)",
  "fecha": "string (ISO 8601)"
}
```

---

### PATCH `/api/v1/casos/{id}/bitacora/alta`

Registra alta médica y deja constancia de la condición final.

**Acceso:** Veterinaria verificada responsable.

**Request Body:**
```json
{
  "condicionAlta": "string — requerida",
  "indicaciones": "string | null"
}
```

**Response 200:**
```json
{
  "casoId": "number (long)",
  "fechaAlta": "string (ISO 8601)",
  "estadoBitacora": "ALTA_REGISTRADA"
}
```

**Respuestas de error relevantes:** 409 alta ya registrada.

---

## Módulo 10 — Resumen médico PDF (`/api/v1/casos/{id}/bitacora/pdf`)

*Cubre: RF-16; Worker de reportes médicos.*

---

### GET `/api/v1/casos/{id}/bitacora/pdf`

Genera o descarga el resumen clínico PDF de la bitácora. **Respuesta binaria**, no JSON.

**Acceso:** Veterinaria autorizada; otros permisos por definir.

**Request:** Sin body.

**Response 200:** `Content-Type: application/pdf`; body: archivo PDF.

**Respuestas de error relevantes:** 404 bitácora inexistente; 403 acceso denegado.

---

## Módulo 11 — Resolución, cierre y archivo (`/api/v1/casos`)

*Cubre: RF-17, RF-18; W-04; proceso de negocio 3.*

---

### PATCH `/api/v1/casos/{id}/cerrar`

Permite al responsable confirmar reencuentro o resolución, conservando trazabilidad.

**Acceso:** Responsable del caso o rol autorizado.

**Request Body:**
```json
{
  "motivoCierre": "REENCUENTRO | RESOLUCION — categorías conceptuales por validar",
  "observacion": "string | null"
}
```

**Response 200:**
```json
{
  "id": "number (long)",
  "estado": "string — estado final del enum",
  "fechaCierre": "string (ISO 8601)"
}
```

**Respuestas de error relevantes:** 403 sin permisos; 409 cierre inválido.

---

### GET `/api/v1/casos/mis-reportes`

Consulta casos registrados por el usuario autenticado, incluyendo cerrados y archivados.

**Acceso:** Ciudadano/Voluntario autenticado.

**Parámetros:**
- `page` — number (int), opcional
- `size` — number (int), opcional
- `estado` — string, opcional

**Request:** Sin body.

**Response 200:**
```json
{
  "content": [{"id": "number (long)", "estado": "string", "tipoCaso": "string", "fechaCreacion": "string (ISO 8601)"}],
  "totalElements": "number (long)"
}
```

---

### PATCH `/api/v1/casos/{id}/archivar`

Archiva un caso según reglas de inactividad o resolución. Normalmente lo ejecuta W-04 internamente; la ruta REST es **opcional/propuesta**.

**Acceso:** Administrador o proceso interno autorizado.

**Request Body:**
```json
{
  "motivo": "string — requerido"
}
```

**Response 200:**
```json
{
  "id": "number (long)",
  "archivado": true,
  "fechaArchivo": "string (ISO 8601)"
}
```

**Respuestas de error relevantes:** 409 condiciones de archivo no cumplidas.

---

## Módulo 12 — Notificaciones y alertas (`/api/v1/notificaciones`)

*Cubre: W-01, seguimiento y priorización.*

---

### GET `/api/v1/notificaciones`

Obtiene avisos del usuario según su rol, zona y casos relacionados. **Propuesta de contrato para W-01**.

**Acceso:** Autenticado.

**Parámetros:**
- `leidas` — boolean, opcional
- `page` — number (int), opcional

**Request:** Sin body.

**Response 200:**
```json
{
  "content": [{"id": "number (long)", "tipo": "string", "mensaje": "string", "leida": "boolean", "fecha": "string (ISO 8601)", "casoId": "number (long) | null"}]
}
```

---

### PATCH `/api/v1/notificaciones/{id}/leer`

Marca una notificación propia como leída.

**Acceso:** Destinatario autenticado.

**Request:** Sin body.

**Response 200:**
```json
{
  "id": "number (long)",
  "leida": true
}
```

**Respuestas de error relevantes:** 403 notificación ajena; 404 no encontrada.

---

## Módulo 13 — Moderación de contenido (`/api/v1/moderacion`)

*Cubre: Responsabilidad del Administrador; actores del proyecto.*

---

### GET `/api/v1/moderacion/casos`

Consulta reportes señalados como impropios o duplicados. **Contrato propuesto**.

**Acceso:** Administrador.

**Request:** Sin body.

**Response 200:**
```json
[
  {"casoId": "number (long)", "motivo": "string", "estadoRevision": "PENDIENTE"}
]
```

---

### PATCH `/api/v1/moderacion/casos/{id}`

Registra decisión administrativa sobre un caso señalado, sin borrado físico automático.

**Acceso:** Administrador.

**Request Body:**
```json
{
  "decision": "MANTENER | OCULTAR",
  "motivo": "string — requerido"
}
```

**Response 200:**
```json
{
  "casoId": "number (long)",
  "decision": "string",
  "fechaRevision": "string (ISO 8601)"
}
```

---

## Resumen de Endpoints

| Módulo | Método | Endpoint | Rol mínimo | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| Autenticación y sesión | POST | `/api/v1/auth/registro` | Según permisos del módulo | Consultar sección correspondiente |
| Autenticación y sesión | POST | `/api/v1/auth/login` | Según permisos del módulo | Consultar sección correspondiente |
| Autenticación y sesión | GET | `/api/v1/auth/me` | Según permisos del módulo | Consultar sección correspondiente |
| Usuarios y perfiles | GET | `/api/v1/usuarios/me` | Según permisos del módulo | Consultar sección correspondiente |
| Usuarios y perfiles | PUT | `/api/v1/usuarios/me` | Según permisos del módulo | Consultar sección correspondiente |
| Verificación de fundaciones y veterinarias | GET | `/api/v1/verificaciones/pendientes` | Según permisos del módulo | Consultar sección correspondiente |
| Verificación de fundaciones y veterinarias | PATCH | `/api/v1/verificaciones/{usuarioId}` | Según permisos del módulo | Consultar sección correspondiente |
| Catálogos de ubicación | GET | `/api/v1/ubicaciones/departamentos` | Según permisos del módulo | Consultar sección correspondiente |
| Catálogos de ubicación | GET | `/api/v1/ubicaciones/municipios` | Según permisos del módulo | Consultar sección correspondiente |
| Catálogos de ubicación | GET | `/api/v1/ubicaciones/barrios` | Según permisos del módulo | Consultar sección correspondiente |
| Captura y registro de casos | POST | `/api/v1/casos` | Según permisos del módulo | Consultar sección correspondiente |
| Captura y registro de casos | POST | `/api/v1/imagenes` | Según permisos del módulo | Consultar sección correspondiente |
| Feed, filtros y detalle de casos | GET | `/api/v1/casos` | Según permisos del módulo | Consultar sección correspondiente |
| Feed, filtros y detalle de casos | GET | `/api/v1/casos/{id}` | Según permisos del módulo | Consultar sección correspondiente |
| Gestión y seguimiento del caso | PATCH | `/api/v1/casos/{id}/estado` | Según permisos del módulo | Consultar sección correspondiente |
| Gestión y seguimiento del caso | GET | `/api/v1/casos/{id}/historial` | Según permisos del módulo | Consultar sección correspondiente |
| Aportes, comentarios y avistamientos | GET | `/api/v1/casos/{id}/aportes` | Según permisos del módulo | Consultar sección correspondiente |
| Aportes, comentarios y avistamientos | POST | `/api/v1/casos/{id}/aportes` | Según permisos del módulo | Consultar sección correspondiente |
| Bitácora médica y atención veterinaria | POST | `/api/v1/casos/{id}/bitacora` | Según permisos del módulo | Consultar sección correspondiente |
| Bitácora médica y atención veterinaria | GET | `/api/v1/casos/{id}/bitacora` | Según permisos del módulo | Consultar sección correspondiente |
| Bitácora médica y atención veterinaria | POST | `/api/v1/casos/{id}/bitacora/evoluciones` | Según permisos del módulo | Consultar sección correspondiente |
| Bitácora médica y atención veterinaria | PATCH | `/api/v1/casos/{id}/bitacora/alta` | Según permisos del módulo | Consultar sección correspondiente |
| Resumen médico PDF | GET | `/api/v1/casos/{id}/bitacora/pdf` | Según permisos del módulo | Consultar sección correspondiente |
| Resolución, cierre y archivo | PATCH | `/api/v1/casos/{id}/cerrar` | Según permisos del módulo | Consultar sección correspondiente |
| Resolución, cierre y archivo | GET | `/api/v1/casos/mis-reportes` | Según permisos del módulo | Consultar sección correspondiente |
| Resolución, cierre y archivo | PATCH | `/api/v1/casos/{id}/archivar` | Según permisos del módulo | Consultar sección correspondiente |
| Notificaciones y alertas | GET | `/api/v1/notificaciones` | Según permisos del módulo | Consultar sección correspondiente |
| Notificaciones y alertas | PATCH | `/api/v1/notificaciones/{id}/leer` | Según permisos del módulo | Consultar sección correspondiente |
| Moderación de contenido | GET | `/api/v1/moderacion/casos` | Según permisos del módulo | Consultar sección correspondiente |
| Moderación de contenido | PATCH | `/api/v1/moderacion/casos/{id}` | Según permisos del módulo | Consultar sección correspondiente |

---

## Observaciones de integración y pendientes de validación

1. **Rutas efectivas:** verificar el `context-path` y las anotaciones `@RequestMapping` para evitar el doble prefijo `/api`.
2. **Nombres de campos:** los DTO Java existentes usan nombres como `tipoCaso`, `urgencia`, `barrioId` y `animal`; el frontend Angular debe consumir exactamente los nombres serializados definitivos. Los demás esquemas son propuestas a revisar.
3. **Enums:** consultar los valores exactos de `TipoCaso`, `Urgencia`, `EstadoCaso`, `Especie`, `Sexo` y roles en el código; las etiquetas conceptuales de este documento no los sustituyen.
4. **Imágenes:** acordar si el archivo se sube primero a Cloudinary y luego se registra su URL, o si el backend recibe un multipart asociado al caso. No mezclar archivo binario y JSON sin contrato explícito.
5. **Seguridad:** comprobar qué endpoints serán públicos, quién puede editar, cerrar y consultar datos clínicos y cómo se verifican fundaciones/veterinarias.
6. **Workers:** W-01 a W-06 son **servicios internos** del monolito Spring Boot; no se les asignan APIs externas propias por defecto.
7. **Alcance:** no se incluyen mapas interactivos en tiempo real, reconocimiento de animales por IA, pagos, ventas ni inventario; no pertenecen al alcance acordado.
8. **Estado de implementación:** este mapa describe la API objetivo. No afirma que todos los controladores, servicios, validaciones o pruebas estén completados.
