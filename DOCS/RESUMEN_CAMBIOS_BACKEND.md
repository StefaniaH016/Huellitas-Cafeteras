# Justificación y Resumen de Cambios del Backend

## 1. Alcance y Referencia de Comparación

Este documento detalla **todos los cambios backend** presentes en la rama `Stefania` (`6e3c7dd`) tomando como línea base la rama `origin/Allison` (`e8eaa11`).

En la comparación directa de árboles (`git diff --stat origin/Allison..HEAD -- huellitas-backend`) existen exactamente **65 archivos backend diferentes**:
- **11 archivos nuevos (agregados)**
- **54 archivos modificados**
- **0 archivos eliminados**
- **Métricas:** 1.931 inserciones (+) y 154 eliminaciones (-)

---

## 2. Resumen por Áreas Funcionales

| Área Funcional | Estado en Allison | Diferencia en la rama actual | Justificación del cambio |
|---|---|---|---|
| **Registro y Verificación Institucional** | Existía registro de usuario genérico y resolución administrativa básica sin documentos. | Se agrega registro institucional multipart (`/registro/institucional`), solicitud de revalidación con soporte (`/solicitar-revision`), consulta protegida del documento legal para administradores, persistencia de NIT y municipio de operación. | Permite a fundaciones y veterinarias enviar documentos legales de soporte (PDF/JPG/PNG) y al administrador contrastar la identidad con evidencia real antes de otorgar permisos institucionales. |
| **Soporte Documental** | No existía persistencia de archivos de soporte para organizaciones. | Se crea la entidad `DocumentoSoporte`, el DTO `DocumentoSoporteDTO`, el repositorio `OrganizacionRepository` y la utilidad `DocumentoSoporteUtils`. | Garantiza el almacenamiento persistente de binarios, la validación estricta de tamaño (máx. 5 MB) y tipo MIME mediante números mágicos, asegurando integridad antes de guardar. |
| **Custodia y Manejo de Proxies Lazy** | Existía consulta y asignación de custodia, pero la evaluación de tipo contra `Fundacion` fallaba con proxies de Hibernate. | `CasoServiceImpl` y mappers desenvuelven la organización con `Hibernate.unproxy(...)` antes de validar `instanceof Fundacion`. | Evita que Spring Security o el servicio rechacen con HTTP 403 a una fundación legítima debido a que Hibernate carga la entidad como un proxy CGLIB en lugar del tipo concreto. |
| **Aportes Ciudadanos y Fotografías** | Los aportes solo admitían comentario básico y las fotos se mezclaban con el caso general. | Se añade ubicación textual (máx. 300 caracteres), relación directa de fotografías con el aporte (`Seguimiento`), y autorización para ciudadanos externos en casos activos (`REGISTRADO`, `EN_ATENCION`). | Permite georreferenciar visualizaciones del animal con puntos de referencia y evita que las fotos de avistamientos posteriores contaminen el álbum original del reporte. |
| **Disponibilidad para Rescate y Perfil** | El perfil no permitía a ciudadanos indicar si pueden responder a rescates en su zona. | Se añade el campo `disponibleRescate` a `Usuario`, `UsuarioPerfilDTO`, `ActualizarPerfilDTO`, mappers y servicios. | Conecta la preferencia del voluntario para autorizar el envío de alertas de emergencia de casos reportados en su municipio de interés. |
| **Remitente de Correo Electrónico** | `EmailServiceImpl` dependía estrictamente de `spring.mail.username`. | Se priorizan `spring.mail.from` y la variable de entorno `MAIL_FROM`, manteniendo fallback al usuario SMTP. | Hace posible usar servicios SMTP en producción donde la cuenta autenticada y el remitente visible (*From*) difieren, sin alterar compatibilidad local. |
| **Moderación Administrativa** | La consulta de casos pendientes dependía de `motivoModeracion != null`. | Se incorpora el flag booleano `moderacionPendiente` en `Caso` y se ajusta la consulta `findByModeracionPendienteTrueOrVisibleFalse()`. | Permite una gestión administrativa limpia de casos en revisión sin colisionar con motivos de cierre o descripciones previas. |
| **Bitácora Médica y Mapeo Clínico** | El resumen médico podía omitir el nombre de la veterinaria si la entidad responsable difería del usuario creador. | Se mapea el responsable (`responsableId`) y se extrae el nombre clínico desde la organización veterinaria o el usuario. | Garantiza trazabilidad profesional en la historia clínica del animal en tratamiento. |
| **Seguridad y Excepciones** | Respuestas HTTP 403 devolvían mensajes estandarizados sin detalle de la regla violada. | `GlobalExceptionHandler` propaga el mensaje específico de `AccessDeniedException` y `SecurityConfig` abre el endpoint multipart institucional. | Facilita al frontend informar exactamente al usuario qué condición impidió la acción (p. ej. "Solo fundaciones verificadas"). |
| **Cobertura de Pruebas** | Pruebas existentes en Allison no contemplaban los nuevos flujos institucionales ni contratos extendidos. | Se añaden 7 clases de prueba nuevas y se actualizan 7 existentes más las colecciones Postman. | Asegura prevención de regresiones en validación de documentos, permisos por rol, filtros de fotos y persistencia. |

---

## 3. Inventario de Archivos Nuevos (11 archivos agregados)

Todos estos archivos fueron incorporados en esta rama y no existen en `origin/Allison`:

| Documento / Ruta | Tipo | Qué se agregó y propósito | Justificación técnica |
|---|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/verificacion/DocumentoSoporteDTO.java` | DTO | Record inmutable para transportar `nombreArchivo`, `tipoContenido` y `byte[] contenido` con implementaciones seguras de `equals`, `hashCode` y `toString`. | Permite servir el binario del documento al administrador desacoplándolo de la entidad JPA y evitando exponer la entidad de persistencia directamente en el controlador. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/entity/DocumentoSoporte.java` | Entidad JPA | Entidad `@Table(name = "documentos_soporte")` con campos `id`, `nombreArchivo`, `tipoContenido` y columna `bytea` (`contenido`). | Persiste de manera segura los archivos acreditativos de fundaciones y veterinarias directamente en base de datos vinculados a la organización. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/repository/OrganizacionRepository.java` | Repositorio | Repositorio Spring Data JPA con consulta `existsByNit(String nit)`. | Permite verificar la unicidad del NIT durante el registro institucional o revalidación para evitar organizaciones duplicadas. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/util/DocumentoSoporteUtils.java` | Utilidad | Validador de archivos: límite de 5 MB, detección de tipos MIME reales por magic numbers (PDF, JPEG, PNG) y asignación a la entidad `Organizacion`. | Evita subida de archivos vacíos, maliciosos, de tipos no soportados o excesivamente pesados, centralizando la regla de negocio. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/dto/verificacion/DocumentoSoporteDTOTest.java` | Prueba Unitaria | Pruebas sobre el record `DocumentoSoporteDTO` (inmutabilidad, igualdad de contenido y representación segura de hashes). | Previene fallos en el manejo de arrays de bytes en capas superiores de servicio y controladores. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/mapper/UsuarioMapperTest.java` | Prueba Unitaria | Pruebas de mapeo de usuario a `UsuarioPerfilDTO`, validando organización, verificación, custodia y `disponibleRescate`. | Garantiza que los atributos institucionales y de voluntariado se proyecten fielmente al perfil de usuario en la API. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/service/AuthServiceImplTest.java` | Prueba Unitaria | Pruebas unitarias de registro institucional y ordinario con validación de NIT, roles y documentos de soporte. | Asegura que las reglas de negocio de creación de cuentas institucionales operen correctamente en aislamiento. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/service/EmailServiceImplTest.java` | Prueba Unitaria | Pruebas unitarias sobre la resolución de remitentes en `EmailServiceImpl` (`spring.mail.from`, `MAIL_FROM` y fallbacks). | Verifica que el correo saliente tome el remitente adecuado en diferentes escenarios de configuración. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/service/ModeracionServiceTest.java` | Prueba Unitaria | Pruebas de moderación administrativa: listado de pendientes, decisiones de visibilidad y ocultamiento de aportes. | Asegura la correcta transición de estados de moderación y la protección de casos reportados. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/service/UsuarioPerfilServiceTest.java` | Prueba Unitaria | Pruebas de consulta y actualización del perfil de usuario, incluyendo el nuevo campo `disponibleRescate`. | Valida la persistencia y recuperación del estado de disponibilidad de rescate y datos de contacto. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/util/DocumentoSoporteUtilsTest.java` | Prueba Unitaria | Pruebas exhaustivas de validación de archivos (formatos admitidos, magic numbers, archivos vacíos y límites de tamaño). | Garantiza que las restricciones de seguridad documental no permitan archivos corruptos o extensiones falsificadas. |

---

## 4. Inventario de Archivos Modificados (54 archivos)

### 4.1. Controladores (5 archivos)

| Documento / Ruta | Qué se modificó | Justificación / Si se omitiera |
|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/controller/AuthController.java` | Añade endpoint `POST /registro/institucional` con `consumes = MediaType.MULTIPART_FORM_DATA_VALUE` recibiendo `datos` (`RegistroUsuarioDTO`) y `documentoSoporte` (`MultipartFile`). | El frontend no podría registrar una fundación o veterinaria junto con su evidencia documental en una sola transacción HTTP. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/controller/VerificacionController.java` | Añade endpoints `POST /solicitar-revision` (multipart) y `GET /{usuarioId}/documento-soporte` (descarga segura con `no-store`, `nosniff` y `Content-Disposition: inline`). | Las organizaciones no tendrían vía para revalidarse tras un rechazo, ni el administrador podría inspeccionar visualmente el soporte desde la web. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/controller/AporteController.java` | Normalización de salto de línea final (EOF). | Garantiza estilo y formato homogéneo según estándares del repositorio. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/controller/BitacoraController.java` | Normalización de salto de línea final (EOF). | Garantiza consistencia de estilo y evita advertencias en linters y git. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/controller/ModeracionController.java` | Normalización de salto de línea final (EOF). | Mantiene el archivo normalizado sin diferencias espurias. |

### 4.2. DTOs de Entrada y Salida (7 archivos)

| Documento / Ruta | Qué se modificó | Justificación / Si se omitiera |
|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/auth/RegistroUsuarioDTO.java` | Incorpora campos institucionales (`nombreEntidad`, `nit`, `documentoLegal`, `municipioOperacion`) y mantiene constructor sobrecargado compatible con registros previos. | Permite recolectar los datos obligatorios de la organización sin romper pruebas ni clientes que usan el constructor ordinario. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/verificacion/VerificacionPendienteDTO.java` | Añade `nit`, `municipio`, `documentoSoporteNombre` y booleano `documentoSoporteDisponible`, con constructor de retrocompatibilidad. | La bandeja administrativa carecería de los datos indispensables para evaluar una organización sin tener que consultar la BD manualmente. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/aporte/CrearAporteDTO.java` | Agrega `@Size(max = 300)` sobre `ubicacionTexto`. | Textos excesivos provocarían errores de base de datos no controlados en lugar de validaciones HTTP 400 descriptivas. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/reporte/AporteDetalleDTO.java` | Añade los campos `zona`, `ubicacionTexto` y `fotografias` (`List<String>`). | El detalle del caso no mostraría la referencia geográfica ni las fotos asociadas a los aportes ciudadanos. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/reporte/CasoDetalleDTO.java` | Añade `fundacionCustodioId` y `fundacionCustodioNombre`. | El frontend no podría mostrar qué fundación tiene en custodia al animal ni activar acciones exclusivas de custodia. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/reporte/ResumenMedicoDTO.java` | Añade `responsableId`. | El cliente no podría asociar la bitácora médica al usuario veterinario autenticado para controlar permisos de edición. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/usuario/ActualizarPerfilDTO.java` | Añade `Boolean disponibleRescate`. | El usuario voluntario no podría habilitar o pausar su disponibilidad para acudir a rescates de emergencia. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/usuario/UsuarioPerfilDTO.java` | Añade campos institucionales (`organizacionId`, `organizacionNombre`, `estadoVerificacionOrganizacion`, `aceptaCustodia`, `capacidadAlbergue`) y `disponibleRescate`. | Las pantallas del perfil y paneles institucionales quedarían sin datos para renderizar el estado del usuario. |

### 4.3. Entidades y Modelos de Persistencia (4 archivos)

| Documento / Ruta | Qué se modificó | Justificación / Si se omitiera |
|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/entity/Caso.java` | Añade columna booleana `moderacionPendiente` (por defecto `false`). | Se mezclarían casos que requieren revisión con casos resueltos u ocultos, dificultando el filtrado en la cola de moderación. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/entity/Organizacion.java` | Añade relación `@OneToOne` con `DocumentoSoporte`, y columnas `documentoSoporteNombre` y `documentoSoporteContentType`. | La organización no conservaría el enlace a su soporte documental ni los metadatos necesarios para descargarlo. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/entity/Foto.java` | Añade relación opcional `@ManyToOne` con `Seguimiento`, con `@JsonBackReference` y exclusiones en Lombok `ToString`/`EqualsAndHashCode`. | Las fotos de aportes se mezclarían con la galería general del reporte y se producirían desbordamientos de pila (ciclos infinitos). |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/entity/Seguimiento.java` | Añade `ubicacionTexto` (300 caracteres) y relación `@OneToMany` con `Foto` ordenada por `id ASC`. | Los aportes no podrían asociar fotografías ni guardar descripciones de ubicación física del avistamiento. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/entity/enums/TipoNotificacion.java` | Añade el valor `VERIFICACION` al enum. | No se podrían generar notificaciones tipadas cuando una organización es verificada o rechazada. |

### 4.4. Repositorios Spring Data (3 archivos)

| Documento / Ruta | Qué se modificó | Justificación / Si se omitiera |
|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/repository/CasoRepository.java` | Sustituye la consulta por `findByModeracionPendienteTrueOrVisibleFalse()`. | No se obtendrían de forma certera los casos que un administrador debe evaluar en su bandeja de moderación. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/repository/FotoRepository.java` | Añade `findFirstByCasoIdAndUrlAndSeguimientoIsNull(Long casoId, String url)`. | El servicio de aportes no podría vincular una foto recién subida al seguimiento específico sin crear registros huérfanos o duplicados. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/repository/MunicipioRepository.java` | Añade `findFirstByNombreIgnoreCase(String nombre)`. | El registro institucional fallaría si el usuario ingresa el municipio con diferencias en mayúsculas/minúsculas. |

### 4.5. Mappers (5 archivos)

| Documento / Ruta | Qué se modificó | Justificación / Si se omitiera |
|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/mapper/AporteMapper.java` | Extrae fotografías y compone la zona (`barrio · municipio`) y `ubicacionTexto` para los DTOs de aporte. | Los clientes de la API recibirían listas vacías de imágenes y sin el contexto geográfico del aporte. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/mapper/CasoMapper.java` | Filtra fotos con `seguimiento == null` para que fotos de aporte no salgan en la galería principal del caso; mapea custodio y nombre de veterinaria responsable. | Las fotos de aportes de ciudadanos se mostrarían erróneamente como fotos oficiales del caso; el custodio y veterinario saldrían nulos. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/mapper/ModeracionMapper.java` | Formatea la fecha de revisión con `FechaUtils.formatear(...)`. | Garantiza el estándar ISO / cadena consistente en la respuesta de moderación. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/mapper/UsuarioMapper.java` | Desenvuelve el proxy Hibernate de la organización y proyecta NIT, municipio operativo, albergue, custodia y `disponibleRescate`. | Evita errores de tipo en tiempo de ejecución por proxies Hibernate y proyecta todos los datos institucionales al perfil. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/mapper/VerificacionMapper.java` | Desenvuelve `Organizacion` con `Hibernate.unproxy(...)`, mapea NIT, municipio y soporte a `VerificacionPendienteDTO`; ajusta estado de respuesta. | La bandeja administrativa no vería los datos institucionales del solicitante ni el estado verídico de la organización. |

### 4.6. Servicios e Implementaciones (11 archivos)

| Documento / Ruta | Qué se modificó | Justificación / Si se omitiera |
|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/AuthService.java` | Declara la sobrecarga `registro(RegistroUsuarioDTO dto, MultipartFile documentoSoporte)`. | El controlador de autenticación carecería del contrato tipado para procesar registros con archivos. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/VerificacionService.java` | Declara `solicitarRevision(...)` y `obtenerDocumentoSoporte(Long usuarioId)`. | El controlador de verificación no podría delegar la revalidación ni la obtención de bytes del documento. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/AuthServiceImpl.java` | Crea y asocia entidades `Fundacion` o `Veterinaria`, valida NIT único, asigna municipio de operación y valida/adjunta documento de soporte. | Las cuentas institucionales se crearían como ciudadanos comunes sin organización ni evidencia persistida. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/VerificacionServiceImpl.java` | Implementa solicitud de revisión y consulta de documento; valida concordancia de rol con organización; desenvuelve proxies; agenda notificación tras commit de transacción. | Se podrían aprobar organizaciones sin soporte, mezclar roles y tipos de entidad, o enviar notificaciones antes de persistir la decisión. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/AporteServiceImpl.java` | Restringe aportes a casos en `REGISTRADO` o `EN_ATENCION`; autoriza a ciudadanos externos, autor y custodio; persiste ubicación y asocia fotografías al seguimiento. | Se permitirían aportes a casos cerrados, se bloquearían aportes útiles de terceros o las fotos no se asociarían al aporte. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/BitacoraServiceImpl.java` | Ajuste en la verificación de permisos y consistencia de excepciones al validar el profesional a cargo. | Asegura que la bitácora médica mantenga invariantes de privacidad y responsabilidad médica. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/CasoServiceImpl.java` | Desenvuelve la organización con `Hibernate.unproxy(...)` antes de evaluar que sea una `Fundacion` en la asignación de custodia; valida nulos explícitamente. | Resuelve el fallo crítico donde Spring denegaba la custodia a fundaciones legítimas porque la relación lazy se envolvía en un proxy de Hibernate. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/CloudinaryServiceImpl.java` | Sanitiza el registro de errores para no imprimir texto libre o secretos de la API externa en los logs del sistema. | Cumple buenas prácticas de observabilidad y previene filtración de datos sensibles en trazas de error. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/EmailServiceImpl.java` | Resuelve el remitente visible priorizando `spring.mail.from` y la variable `MAIL_FROM` antes de recurrir al usuario SMTP. | Permite separar la identidad autenticada SMTP del remitente público visible en los correos transaccionales. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/ModeracionServiceImpl.java` | Al resolver una moderación, limpia `moderacionPendiente = false` y aplica la visibilidad según la decisión administrativa. | Los casos resueltos continuarían apareciendo en la cola de moderación pendiente. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/UsuarioPerfilServiceImpl.java` | Persiste el campo `disponibleRescate` cuando el usuario actualiza sus preferencias de perfil. | El usuario no podría actualizar ni guardar su estado de disponibilidad de rescate en la base de datos. |

### 4.7. Seguridad, Excepciones y Configuración (5 archivos)

| Documento / Ruta | Qué se modificó | Justificación / Si se omitiera |
|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/security/SecurityConfig.java` | Añade los endpoints `/v1/auth/registro/institucional` y `/api/v1/auth/registro/institucional` como rutas públicas sin token. | Los usuarios nuevos no podrían registrarse institucionalmente porque Spring Security devolvería HTTP 401/403. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/security/JwtAuthenticationFilter.java` | Registra una advertencia en log cuando no logra extraer el correo/usuario del token JWT. | Dificultaría diagnosticar tokens expirados o corruptos enviados por clientes. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/exception/GlobalExceptionHandler.java` | Retorna el mensaje puntual de `AccessDeniedException` en el cuerpo de la respuesta HTTP 403. | El usuario vería únicamente "Acceso denegado" sin conocer la regla exacta que incumplió. |
| `huellitas-backend/pom.xml` | Actualiza `springdoc-openapi-starter-webmvc-ui` a la versión `3.1.0`. | Mantiene la documentación OpenAPI/Swagger actualizada y compatible con el starter de Spring Boot. |
| `huellitas-backend/src/main/resources/application.yml` | Añade `spring.mail.from: ${MAIL_FROM:${MAIL_USERNAME:noreply@huellitas.org}}` y retira bloque redundante de configuración de correo. | Elimina configuraciones duplicadas y permite configurar el remitente por variable de entorno. |

### 4.8. Workers Asíncronos (2 archivos)

| Documento / Ruta | Qué se modificó | Justificación / Si se omitiera |
|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/worker/NotificacionWorker.java` | Agrega `notificarVerificacionOrganizacion` con tipo `VERIFICACION`; añade transaccionalidad `@Transactional` a métodos asíncronos y sincronización `afterCommit`. | Evita que notificaciones asíncronas fallen por falta de sesión JPA o anuncien transacciones que terminaron en rollback. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/worker/PdfWorker.java` | Ajusta transaccionalidad y manejo de sesión para generación de documentos clínicos. | Asegura que la lectura de la historia clínica disponga de datos consistentes durante el renderizado del PDF. |

### 4.9. Pruebas y Colecciones de Postman (11 archivos)

| Documento / Ruta | Qué se modificó | Justificación / Si se omitiera |
|---|---|---|
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/Fase1IntegrationTests.java` | Actualiza assertions y DTOs para incluir soporte al nuevo campo `disponibleRescate`. | Mantiene la suite de integración de Fase 1 sincronizada con el contrato actual de perfil. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/Fase2IntegrationTests.java` | Expande el flujo integrado: registro institucional con soporte, aprobación por admin, asignación de custodia a fundación verificada. | Detecta fallos de integración entre controladores, servicios, repositorios y seguridad para los nuevos flujos. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/mapper/CasoMapperTest.java` | Comprueba que las fotos de seguimiento se filtren del álbum general y verifica mapeo de custodio y responsable. | Previene regresiones donde fotos de aportes aparezcan en la galería oficial del caso. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/service/AporteServiceTest.java` | Prueba aportes de ciudadanos externos en casos activos, rechazo en casos cerrados y persistencia de ubicación y fotos. | Garantiza que las reglas de aportes comunitarios no se degraden con cambios futuros. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/service/BitacoraServiceTest.java` | Valida inicio de atención, permisos de veterinaria responsable y restricciones de edición y alta. | Asegura la confidencialidad y consistencia en el seguimiento médico de animales. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/service/CasoServiceTest.java` | Valida que la asignación de custodia funcione correctamente incluso cuando la organización viene envuelta en un proxy Hibernate. | Evita regresiones en la resolución de proxies Hibernate en la gestión de custodia. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/service/VerificacionServiceImplTest.java` | Prueba flujos de solicitud de revisión, validación de NIT duplicado y descarga del documento de soporte. | Protege la integridad de la cola de verificación administrativa y del manejo de archivos. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/worker/NotificacionWorkerTest.java` | Prueba la emisión de notificaciones de verificación aprobada y rechazada a organizaciones. | Garantiza que el usuario reciba la notificación adecuada según el veredicto del administrador. |
| `huellitas-backend/src/test/resources/postman_collection.json` | Añade peticiones multipart para registro institucional, solicitud de revisión y descarga de documento de soporte. | Permite validar manualmente o con Newman todos los nuevos endpoints desde Postman. |
| `huellitas-backend/src/test/resources/postman_flat_collection.json` | Versión aplanada sincronizada de la colección Postman para pipelines de CI/CD. | Mantiene los pipelines de pruebas automatizadas alineados con los nuevos contratos del backend. |

---

## 5. Resumen Consolidado y Validación

```
Total de archivos comparados frente a origin/Allison: 65 archivos
├── Archivos nuevos (agregados): 11
│   ├── DTOs y Utilidades:       2 (DocumentoSoporteDTO, DocumentoSoporteUtils)
│   ├── Entidades y Repositorio: 2 (DocumentoSoporte, OrganizacionRepository)
│   └── Pruebas Unitarias:       7 (DTOs, Mappers, Servicios y Utils)
└── Archivos modificados:        54
    ├── Controladores:           5
    ├── DTOs:                    7
    ├── Entidades y Enums:       4
    ├── Repositorios:            3
    ├── Mappers:                 5
    ├── Servicios:              11
    ├── Seguridad y Config:      5
    ├── Workers Asíncronos:      2
    └── Pruebas y Postman:      12 (incluyendo colecciones y Fase 1/2)
```

### Validación de Compilación y Pruebas
Todos los archivos del backend compilan limpiamente contra Java 17 y Spring Boot 3.3.4. Las pruebas unitarias y de integración cubren los nuevos contratos, garantizando que el sistema soporte el registro y verificación institucional documental, la gestión de custodia sin conflictos de proxies lazy, el voluntariado de rescate y la separación de fotografías de aportes.
