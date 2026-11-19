# PostgreSQL en Railway

Guía para crear y administrar la base de datos PostgreSQL de Huellitas Cafeteras en Railway. El despliegue del backend en Azure está documentado aparte en [AZURE_DEPLOYMENT.md](./AZURE_DEPLOYMENT.md).

## Crear PostgreSQL

1. Inicia sesión en [Railway](https://railway.app).
2. Crea un proyecto o abre el proyecto existente.
3. Selecciona **New → Database → PostgreSQL** y espera a que el servicio quede disponible.
4. Abre el servicio PostgreSQL y revisa sus variables de conexión.

## Datos de conexión

Railway muestra los datos de conexión en las variables del servicio y en **Connect**. Para conectar una aplicación desplegada fuera de Railway, como el backend en Azure, usa el host y puerto públicos (TCP Proxy) de Railway.

| Variable | Descripción |
|---|---|
| `PGHOST` | Host público para conexiones desde fuera de Railway |
| `PGPORT` | Puerto público asignado por Railway |
| `PGDATABASE` | Nombre de la base de datos |
| `PGUSER` | Usuario de la base de datos |
| `PGPASSWORD` | Contraseña de la base de datos |

No uses el hostname privado de Railway desde Azure: solo está disponible dentro de la red privada de Railway. Copia los valores en las variables de entorno del servicio consumidor; para el backend en Azure, sigue [la guía de despliegue en Azure](./AZURE_DEPLOYMENT.md).

## Probar la conexión con `psql`

Con `psql` instalado, usa el host, puerto, usuario y nombre de base de datos de Railway. El cliente solicitará la contraseña:

```powershell
psql -h <PGHOST> -p <PGPORT> -U <PGUSER> -d <PGDATABASE>
```

Dentro de `psql`, puedes comprobar la conexión:

```sql
SELECT current_database(), current_user, now();
\q
```

## Copias de seguridad

Para exportar la base de datos:

```powershell
pg_dump -h <PGHOST> -p <PGPORT> -U <PGUSER> -d <PGDATABASE> -F c -f huellitas-backup.dump
```

Para restaurarla en una base de datos vacía:

```powershell
pg_restore -h <PGHOST> -p <PGPORT> -U <PGUSER> -d <PGDATABASE> --clean huellitas-backup.dump
```

`pg_dump` y `pg_restore` solicitarán la contraseña. Verifica el archivo de respaldo y el destino antes de restaurar, ya que `--clean` elimina objetos existentes antes de recrearlos.

## Seguridad

- No publiques la contraseña ni agregues credenciales al repositorio.
- Mantén privados los archivos de respaldo, pues contienen los datos de la aplicación.
- Rota la contraseña desde Railway si se expone accidentalmente y actualiza las variables del servicio que consume la base de datos.
