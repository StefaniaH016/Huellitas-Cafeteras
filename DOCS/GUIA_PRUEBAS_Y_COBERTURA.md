# Guía de Pruebas Unitarias, Cobertura JaCoCo, Datos Demo y Handler 502

## 1. Requisitos previos

- **Java 21** y **Maven Wrapper** (`huellitas-backend\mvnw.cmd`).
- Para las pruebas automatizadas con **H2 en memoria** (sin necesidad de PostgreSQL ni Docker): establecer `$env:SPRING_PROFILES_ACTIVE="test"` (las pruebas de integración `@ActiveProfiles("test")` también activan `test` automáticamente).
- Para probar el arranque completo con `DemoDataLoader`: tener activo el contenedor `huellitas-db` en PostgreSQL (`localhost:5432`) y los archivos `.env` configurados.

---

## 2. Comandos de pruebas y cobertura (PowerShell)

### 2.1 Suite completa del backend + reporte JaCoCo

```powershell
cd huellitas-backend
$env:SPRING_PROFILES_ACTIVE="test"
.\mvnw.cmd -B clean verify
```

- **Resultado ensayado:** `Tests run: 258, Failures: 0, Errors: 0, Skipped: 0` (`BUILD SUCCESS`, ~58 s).
- **Reporte visual HTML:** abrir `huellitas-backend\target\site\jacoco\index.html` en el navegador.

### 2.2 Ensayo rápido para el video (clases nuevas, mappers, 502 y DemoDataLoader)

```powershell
cd huellitas-backend
$env:SPRING_PROFILES_ACTIVE="test"
.\mvnw.cmd -B test "-Dtest=*CoverageTest,*MapperTest,GlobalExceptionHandlerTest,DemoDataLoaderTest"
```

- **Resultado ensayado:** `Tests run: 46, Failures: 0, Errors: 0, Skipped: 0` (`BUILD SUCCESS`, ~11 s).

### 2.3 Listar clases por debajo del 60 % desde `jacoco.csv`

Ejecutar **dentro de `huellitas-backend`** (después de correr `.\mvnw.cmd -B clean verify`):

```powershell
Import-Csv target\site\jacoco\jacoco.csv | ForEach-Object {
  $ic = [int]$_.INSTRUCTION_COVERED; $im = [int]$_.INSTRUCTION_MISSED; $it = $ic + $im
  $lc = [int]$_.LINE_COVERED; $lm = [int]$_.LINE_MISSED; $lt = $lc + $lm
  $ip = if ($it -gt 0) { [math]::Round(100 * $ic / $it, 2) } else { 100 }
  $lp = if ($lt -gt 0) { [math]::Round(100 * $lc / $lt, 2) } else { 100 }
  if ($ip -lt 60 -or $lp -lt 60) {
    "$($_.PACKAGE).$($_.CLASS) | Instr: $ip% ($ic/$it) | Lineas: $lp% ($lc/$lt)"
  }
}
```

### 2.4 Validación completa del frontend (`huellitas-frontend`)

```powershell
cd huellitas-frontend
npm run lint
npm run prettier:check
npm test -- --no-watch
npm run build
```

- **Resultado ensayado:** `lint` y `prettier:check` sin errores, `314 passed | 4 skipped` en Vitest (`43 passed | 1 skipped`) y `build` de producción exitoso.

---

## 3. Resultados de cobertura JaCoCo (Antes vs. Después)

- **Cobertura global antes (`222` pruebas):** `84.75 %` instrucciones (`10023/11826`), `86.36 %` líneas (`2076/2404`).
- **Cobertura global después (`258` pruebas, post-merge `5944c32`):** **`87.70 %` instrucciones** (`11162/12728`), **`89.24 %` líneas** (`2389/2677`), **`72.33 %` ramas** (`818/1131`).

### Clases objetivo implementadas / reforzadas

| Clase | Antes (Instr / Líneas) | Después (Instr / Líneas / Métodos) |
|---|---|---|
| `GlobalExceptionHandler` | `73.72 %` / `73.33 %` | **`98.80 %`** / **`100.00 %`** / **`100.00 %`** |
| `DemoDataLoader` *(nueva)* | — | **`98.58 %`** / **`99.59 %`** / **`100.00 %`** |
| `AuthServiceImpl` | `87.26 %` / `93.83 %` | **`97.78 %`** / **`97.53 %`** / **`100.00 %`** |
| `VerificacionServiceImpl` | `80.84 %` / `87.25 %` | **`100.00 %`** / **`100.00 %`** / **`100.00 %`** |
| `UsuarioPerfilServiceImpl` | `81.90 %` / `100.00 %` | **`100.00 %`** / **`100.00 %`** / **`100.00 %`** |
| `ModeracionServiceImpl` | `88.98 %` / `95.83 %` | **`100.00 %`** / **`100.00 %`** / **`100.00 %`** |
| `AporteMapper` | `88.11 %` / `83.72 %` | **`100.00 %`** / **`100.00 %`** / **`100.00 %`** |
| `BitacoraMapper` | `79.11 %` / `79.49 %` | **`100.00 %`** / **`100.00 %`** / **`100.00 %`** |
| `ModeracionMapper` | `82.93 %` / `90.91 %` | **`100.00 %`** / **`100.00 %`** / **`100.00 %`** |
| `NotificacionMapper` | `72.22 %` / `90.00 %` | **`100.00 %`** / **`100.00 %`** / **`100.00 %`** |
| `VerificacionMapper` | `96.23 %` / `92.86 %` | **`100.00 %`** / **`100.00 %`** / **`100.00 %`** |

### Clases que permanecen bajo el 60 % (fuera del alcance del bloque actual)

| Clase | Instrucciones | Líneas |
|---|---|---|
| `OcultarAporteDTO` | `0.00 %` (`0/6`) | `0.00 %` (`0/1`) |
| `ImagenWorker` | `0.00 %` (`0/6`) | `0.00 %` (`0/1`) |
| `NotificacionController` | `0.00 %` (`0/38`) | `0.00 %` (`0/6`) |
| `ErrorResponse` | `0.00 %` (`0/15`) | `0.00 %` (`0/1`) |
| `MarcarCasoModeracionDTO` | `0.00 %` (`0/6`) | `0.00 %` (`0/1`) |
| `HuellitasApplication` | `37.50 %` (`3/8`) | `33.33 %` (`1/3`) |
| `ModeracionController` | `42.42 %` (`14/33`) | `40.00 %` (`2/5`) |
| `CasoSpecification` | `51.59 %` (`81/157`) | `71.43 %` (`15/21`) |
| `CloudinaryIntegrationException` | `55.56 %` (`5/9`) | `50.00 %` (`2/4`) |
| `EmailServiceImpl` | `59.16 %` (`239/404`) | `60.27 %` (`44/73`) |
| `EvolucionDetalleDTO` | `71.74 %` (`33/46`) | `33.33 %` (`1/3`) |
| `EvolucionDTO` | `75.00 %` (`18/24`) | `33.33 %` (`1/3`) |

---

## 4. Datos semilla locales (`DemoDataLoader`)

`DemoDataLoader` tiene `@Profile("!prod & !docker & !test")`, por lo que **no se ejecuta en producción** (`prod` en Railway / Compose ni `docker` en Azure) ni interfiere con las pruebas (`test`).

### Credenciales creadas en entorno local

| Rol | Correo | Contraseña |
|---|---|---|
| `ADMINISTRADOR` *(desde `DataLoader`)* | `admin@huellitas.co` | `Admin12345!` |
| `CIUDADANO_VOLUNTARIO` | `ciudadano.demo@huellitas.co` | `Demo12345!` |
| `FUNDACION_REFUGIO` *(verificada, custodia activa)* | `fundacion.demo@huellitas.co` | `Demo12345!` |
| `VETERINARIA` *(verificada, `TP-DEMO-001`)* | `veterinaria.demo@huellitas.co` | `Demo12345!` |

### Casos semilla y bitácora con alta

- **Max** → `REGISTRADO` (`EXTRAVIADO`, `ALTA`).
- **Luna** → `EN_ATENCION` (`HERIDO`, `CRITICA`, con custodia y bitácora médica abierta).
- **Toby** → `RESUELTO` (`VULNERABLE`, `MEDIA`, con custodia, **bitácora médica en estado `ALTA_REGISTRADA`** con 2 evoluciones firmadas y cierre `RECUPERADO`).
- **Milo** → `ARCHIVADO` (`EN_ADOPCION`, `BAJA`, con cierre `ENTREGADO_A_CUIDADOR`).

---

## 5. Guion corto ensayado para el video

1. **Ubicarse en el backend y activar perfil de prueba:**
   ```powershell
   cd huellitas-backend
   $env:SPRING_PROFILES_ACTIVE="test"
   ```
2. **Ejecutar el lote rápido de las nuevas pruebas unitarias (~11 s, 46 pruebas):**
   ```powershell
   .\mvnw.cmd -B test "-Dtest=*CoverageTest,*MapperTest,GlobalExceptionHandlerTest,DemoDataLoaderTest"
   ```
3. **Generar/mostrar cobertura JaCoCo y clases `< 60 %`:**
   - Correr `.\mvnw.cmd -B clean verify` (o abrir `target\site\jacoco\index.html` previamente generado) y enseñar `GlobalExceptionHandler` (`502 Bad Gateway`), mappers al `100 %`, los 4 servicios y `DemoDataLoader`.
   - Ejecutar el comando de la sección `2.3` dentro de `huellitas-backend` para listar las clases bajo el `60 %`.
4. **Demostrar `DemoDataLoader` en vivo (con `huellitas-db` encendido):**
   - Arrancar el backend (`.\mvnw.cmd spring-boot:run`) y hacer login en el frontend (`http://localhost:4200`) con `ciudadano.demo@huellitas.co`, `fundacion.demo@huellitas.co` y `veterinaria.demo@huellitas.co` (`Demo12345!`), mostrando los 4 estados y la bitácora con alta médica de **Toby**.
