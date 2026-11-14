# Despliegue del backend en Azure

Guía para desplegar el backend Spring Boot en Azure App Service como contenedor, con PostgreSQL alojado en Railway. El frontend se publica por separado y su URL de API de producción está definida en `huellitas-frontend/src/environments/environment.production.ts`.

## Arquitectura actual

- **Backend:** Azure App Service para contenedores Linux, imagen almacenada en Azure Container Registry (ACR).
- **Base de datos:** PostgreSQL en Railway. Azure debe conectarse usando el host y puerto públicos de Railway; el hostname privado de Railway no es accesible desde Azure.
- **Frontend:** aplicación web publicada por separado. Su API apunta a `https://huellitas-cafeteras-prod.azurewebsites.net/v1`.
- **CI/CD:** `azure-pipelines.yml` construye la imagen desde la raíz del repositorio, la publica en ACR y la despliega en App Service al hacer push a `main`.

## Requisitos previos

1. Un Azure Container Registry y un App Service Linux configurado para ejecutar una imagen personalizada.
2. En Azure DevOps, una conexión de servicio de Azure con acceso al App Service y una conexión de registro con permiso para publicar en ACR.
3. La base de datos PostgreSQL activa en Railway y sus datos de conexión pública.
4. Los secretos de JWT, Cloudinary y SMTP disponibles para configurarlos en App Service.

El pipeline actual define estos valores, que deben corresponder a recursos y conexiones existentes en Azure DevOps:

| Configuración del pipeline | Valor actual |
|---|---|
| Conexión de servicio de Azure | `azure-connection` |
| Conexión de servicio de ACR | `docker-registry-connection` |
| Registro | `acrhuellitascafeteras.azurecr.io` |
| Repositorio de imagen | `huellitas-cafeteras` |
| App Service | `huellitas-cafeteras-prod` |

Si los nombres reales difieren, actualiza las variables en `azure-pipelines.yml`.

## Configurar App Service

En App Service, abre **Settings → Environment variables** (o **Configuration → Application settings**, según la vista del portal) y agrega las siguientes variables. Los valores secretos se guardan en Azure; no los escribas en archivos versionados.

### PostgreSQL de Railway

| App Setting | Valor |
|---|---|
| `PGHOST` | Host público de PostgreSQL mostrado por Railway |
| `PGPORT` | Puerto público de PostgreSQL mostrado por Railway |
| `PGDATABASE` | Nombre de la base de datos |
| `PGUSER` | Usuario de la base de datos |
| `PGPASSWORD` | Contraseña de la base de datos |

Usa el host y puerto públicos de Railway, no las variables de red privada internas. La configuración actual de Spring construye la URL JDBC con `PGHOST`, `PGPORT` y `PGDATABASE`; `DATABASE_URL` no reemplaza esos valores en `application.yml`.

### JWT, Cloudinary y correo

| App Setting | Uso |
|---|---|
| `JWT_SECRET` | Secreto para firmar tokens JWT |
| `JWT_EXPIRATION` | Duración del token en milisegundos |
| `CLOUDINARY_CLOUD_NAME` | Nombre de la cuenta Cloudinary |
| `CLOUDINARY_API_KEY` | Clave API Cloudinary |
| `CLOUDINARY_API_SECRET` | Secreto API Cloudinary |
| `CLOUDINARY_TIMEOUT` | Timeout del SDK en segundos (opcional; predeterminado: `30`) |
| `MAIL_HOST` | Host SMTP (opcional; predeterminado: `smtp.gmail.com`) |
| `MAIL_PORT` | Puerto SMTP (opcional; predeterminado: `587`) |
| `MAIL_USERNAME` | Usuario SMTP |
| `MAIL_PASSWORD` | Contraseña SMTP |
| `MAIL_FROM` | Remitente de correo (opcional; por defecto usa `MAIL_USERNAME`) |

Configura además:

| App Setting | Valor |
|---|---|
| `WEBSITES_PORT` | `8080`, puerto expuesto por el contenedor |
| `SERVER_PORT` | `8080` (opcional; es el valor predeterminado) |

El `Dockerfile` del backend activa el perfil Spring `docker` al iniciar. No es necesario establecer `SPRING_PROFILES_ACTIVE` en App Service para el despliegue actual.

Después de agregar o modificar App Settings, guarda los cambios y espera a que App Service reinicie el contenedor.

## Despliegue con Azure Pipelines

El archivo `azure-pipelines.yml` ya contiene las tareas para construir, publicar y desplegar:

1. Verifica que las conexiones de servicio de Azure DevOps y los recursos de ACR/App Service coincidan con la tabla anterior.
2. Asegúrate de que App Service tenga acceso de extracción a la imagen de ACR.
3. Habilita el pipeline en Azure DevOps y autoriza el acceso al repositorio y las conexiones de servicio cuando se solicite.
4. Un push a la rama `main` inicia la etapa `BuildAndPush` y, si esta pasa, la etapa `Deploy`.
5. En Azure DevOps, abre la ejecución del pipeline y confirma que ambas etapas terminen correctamente.
6. En App Service, consulta **Deployment Center** y los logs del contenedor si el despliegue no inicia.

El contexto de construcción debe seguir siendo la raíz del repositorio, pues el Dockerfile copia `huellitas-backend/`.

## Verificación posterior al despliegue

Ejecuta el smoke test de solo lectura desde la raíz del repositorio. Comprueba que cargue el frontend, que el backend reporte `UP` y que las consultas públicas respondan en menos de cinco segundos:

```powershell
.\scripts\smoke-test-production.ps1
```

Para validar otro entorno, pasa las URLs como parámetros. El script termina con código distinto de cero si alguna comprobación falla:

```powershell
.\scripts\smoke-test-production.ps1 -FrontendUrl 'https://<frontend>' -BackendUrl 'https://<backend>'
```

Desde PowerShell, comprueba salud del servicio y una ruta pública de API:

```powershell
$api = 'https://huellitas-cafeteras-prod.azurewebsites.net'
curl.exe -sS -o NUL -w "HTTP %{http_code} | %{time_total}s`n" "$api/actuator/health"
curl.exe -sS -o NUL -w "HTTP %{http_code} | %{time_total}s`n" "$api/v1/ubicaciones/departamentos"
```

La respuesta de health debe indicar estado `UP`; la ruta de ubicaciones debe responder con un código 2xx. Para verificar una ruta que requiere autenticación, envía un JWT válido en el encabezado `Authorization`.

## Diagnóstico básico

- **El contenedor no inicia:** revisa los logs de App Service y confirma que `WEBSITES_PORT=8080`.
- **Error de conexión a PostgreSQL:** confirma que `PGHOST` y `PGPORT` sean los públicos de Railway y que el servicio de base de datos esté activo.
- **Error de autenticación con la base de datos:** revisa `PGDATABASE`, `PGUSER` y `PGPASSWORD` en App Settings.
- **Errores de token:** verifica `JWT_SECRET` y que `JWT_EXPIRATION` esté expresado en milisegundos.
- **Fallos de correo o imágenes:** revisa las variables `MAIL_*` o `CLOUDINARY_*` y los logs de la aplicación.
- **La API funciona, pero el frontend no conecta:** confirma que `environment.production.ts` apunte al dominio del App Service y que CORS permita el origen real del frontend.

## Seguridad

- No guardes contraseñas, tokens ni claves en el repositorio, el pipeline YAML ni este documento.
- Usa App Settings para credenciales y restringe su acceso en Azure.
- Rota cualquier secreto que haya sido expuesto accidentalmente.
