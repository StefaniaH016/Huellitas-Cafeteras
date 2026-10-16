# 🚀 Despliegue en Railway - Backend + PostgreSQL

Guía completa para desplegar Huellitas Cafeteras en Railway.

---

## PARTE 1: Configurar PostgreSQL en Railway

### PASO 1: Crear Proyecto en Railway

1. Ve a **https://railway.app**
2. Inicia sesión (con GitHub es más fácil)
3. Click en **"New Project"**
4. Selecciona **"Provision PostgreSQL"**
5. Espera a que se despliegue (≈1-2 minutos)

### PASO 2: Configurar PostgreSQL

Una vez que PostgreSQL esté corriendo:

1. En el dashboard, haz click en el contenedor **postgres**
2. Ve a la pestaña **"Variables"**
3. Deberías ver:
   - `PGHOST` - Host del servidor
   - `PGPORT` - Puerto (5432)
   - `PGUSER` - Usuario (postgres)
   - `PGPASSWORD` - Contraseña generada
   - `PGDATABASE` - Nombre base de datos (railway)

### PASO 3: Obtener Connection URL

1. En Railway, ve a **"Connect"**
2. Copia la **Connection URL** completa
   - Formato: `postgresql://postgres:password@host:5432/railway`
3. Guarda esta URL, la necesitarás después

### PASO 4: Crear Base de Datos (Opcional)

Si quieres crear una BD con un nombre específico:

```bash
# Desde terminal local (con psql instalado)
psql postgresql://postgres:password@host:5432/railway

# Dentro de psql:
CREATE DATABASE huellitas;
\l  # Ver bases de datos

# Salir
\q
```

---

## PARTE 2: Agregar Secrets a GitHub

Estos secrets permiten que GitHub Actions se conecte a Railway.

### PASO 1: Obtener Credenciales de Railway

De la URL de conexión `postgresql://postgres:mypass@containers-us-west-1.railway.app:5432/railway`:

| Variable | Valor |
|----------|-------|
| `RAILWAY_DATABASE_URL` | `postgresql://postgres:mypass@containers-us-west-1.railway.app:5432/railway` |
| `RAILWAY_DATABASE_USER` | `postgres` |
| `RAILWAY_DATABASE_PASSWORD` | `mypass` |

### PASO 2: Agregar Secrets a GitHub

1. Ve a tu repositorio GitHub
2. **Settings** → **Secrets and variables** → **Actions**
3. Click en **"New repository secret"**
4. Crea 3 secrets:

```
Secret Name                  | Valor
-----------------------------|------------------------------------------
RAILWAY_DATABASE_URL         | postgresql://postgres:xxx@host:5432/railway
RAILWAY_DATABASE_USER        | postgres
RAILWAY_DATABASE_PASSWORD    | xxxxx
```

✅ Listo. GitHub Actions ahora puede conectarse a Railway.

---

## PARTE 3: Desplegar Backend en Railway

### PASO 1: Conectar GitHub a Railway

1. En Railway, click en **"New"** → **"GitHub Repo"**
2. Busca tu repositorio `Huellitas-Cafeteras`
3. Selecciona el repositorio
4. Railway detectará automáticamente:
   - **Java 21** (del pom.xml)
   - **Spring Boot** (del pom.xml)

### PASO 2: Configurar Variables de Entorno en Railway

1. En Railway, ve al proyecto del backend
2. Click en la pestaña **"Variables"**
3. Agrega las variables necesarias:

```
Variable Name              | Valor
---------------------------|----------------------------------------
DATABASE_URL               | postgresql://postgres:xxx@host:5432/huellitas
DATABASE_USER              | postgres
DATABASE_PASSWORD          | xxxxx
SPRING_JPA_HIBERNATE_DDL_AUTO | update
SPRING_PROFILES_ACTIVE     | prod
```

### PASO 3: Configurar Dockerfile (si es necesario)

El archivo `huellitas-backend/Dockerfile` ya está configurado. Verifica que contenga:

```dockerfile
FROM openjdk:21-slim
COPY target/huellitas-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### PASO 4: Deploy

1. Railway detecta cambios en el repositorio automáticamente
2. En el dashboard, verás el build en progreso
3. Cuando termine, obtendrás una URL pública:
   - Ej: `https://huellitas-backend-prod.railway.app`

### PASO 5: Verificar Deployment

```bash
# Desde terminal
curl https://huellitas-backend-prod.railway.app/health

# Debería responder:
{"status":"UP"}
```

---

## PARTE 4: Configurar GitHub Actions para CI/CD

El archivo `.github/workflows/sqa-pipeline.yml` ya está configurado. Verifica que incluya:

```yaml
backend-quality:
  env:
    DATABASE_URL: ${{ secrets.RAILWAY_DATABASE_URL }}
    DATABASE_USER: ${{ secrets.RAILWAY_DATABASE_USER }}
    DATABASE_PASSWORD: ${{ secrets.RAILWAY_DATABASE_PASSWORD }}
  run: mvn -B clean verify
```

---

## SOLUCIÓN DE PROBLEMAS

### Problema: "Build failed: Java not found"
**Solución:** Railway detecta automáticamente Java 21 del `pom.xml`. Si falla:
1. Verifica que `pom.xml` contenga: `<java.version>21</java.version>`
2. Re-run el build en Railway

### Problema: "Connection refused to database"
**Solución:** 
1. Verifica que `RAILWAY_DATABASE_URL` sea correcto
2. Asegúrate que PostgreSQL está corriendo en Railway
3. Check que las credenciales sean exactas

### Problema: "Port already in use"
**Solución:**
1. Por defecto Spring Boot usa puerto 8080
2. En Railway, configura variable: `SERVER_PORT=8080`
3. Railway asignará automáticamente el puerto público

### Problema: "Application exits immediately"
**Solución:**
1. Revisa los logs en Railway: Click en **"Logs"**
2. Busca error de conexión a BD
3. Verifica variables de entorno

---

## MONITOREO Y LOGS

### Ver Logs en Railway

1. Dashboard de Railway
2. Click en tu aplicación
3. Pestaña **"Logs"**
4. Busca errores o warnings

### Comandos útiles

```bash
# Ver status del contenedor
curl https://your-app.railway.app/actuator/health

# Ver info detallada
curl https://your-app.railway.app/actuator/info
```

---

## ESCALABILIDAD

### Aumentar Recursos

1. En Railway, click en **"Scaling"**
2. Aumenta:
   - **CPU:** de 0.5 a 1-2 vCPU
   - **Memory:** de 256MB a 512MB-1GB
3. Los cambios se aplican automáticamente

### Aumentar DB

1. En el contenedor PostgreSQL, click en **"Scaling"**
2. Aumenta memoria/CPU según necesidad
3. Los cambios NO requieren downtime

---

## BACKUP DE BD

Railway hace backup automático. Para descargar:

1. En PostgreSQL en Railway, pestaña **"Data"**
2. Click en **"Download"**
3. Obtendrás un archivo SQL

Para restaurar:

```bash
psql postgresql://postgres:password@host:5432/railway < backup.sql
```

---

## DOMINIO PERSONALIZADO (Opcional)

1. En Railway, click en **"Domains"**
2. Click en **"Add Custom Domain"**
3. Ingresa tu dominio: `api.huellitas.com`
4. Sigue instrucciones para configurar DNS

---

## DESENLAZAR GITHUB (Para Despliegues Manuales)

Si prefieres desplegar manualmente en lugar de automáticamente:

1. En Railway, click en **"Deployments"**
2. Disable **"Auto Deploy"**
3. Deploy manualmente: `railway up` (via CLI)

---

## CHECKLIST PRE-DEPLOYMENT

Antes de hacer deploy:

- [ ] PostgreSQL corriendo en Railway
- [ ] Variables de entorno configuradas
- [ ] GitHub Secrets agregados (3 variables)
- [ ] `pom.xml` contiene Java 21
- [ ] `Dockerfile` está correcto
- [ ] `application.properties` tiene configuración correcta
- [ ] Tests pasan localmente (`mvn clean verify`)
- [ ] GitHub Actions workflow es exitoso

---

## COMANDOS RÁPIDOS

```bash
# Ver estado de Railway
railway status

# Deploy manualmente
railway up

# Ver variables
railway variables

# Ver logs
railway logs

# Conectar a BD desde línea de comandos
psql postgresql://postgres:password@host:5432/railway

# Ejecutar query
psql postgresql://user:pass@host:5432/db -c "SELECT * FROM users;"
```

---

## CONTACTO Y SOPORTE

- **Docs de Railway:** https://docs.railway.app
- **Docs de Spring Boot:** https://spring.io/guides
- **PostgreSQL Docs:** https://www.postgresql.org/docs

¡Listo! Tu backend está en producción en Railway. 🚀
