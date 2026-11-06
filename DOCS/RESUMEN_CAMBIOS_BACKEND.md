# Justificación de cambios del backend

## Alcance

Este documento describe los cambios backend acumulados que están en el árbol de trabajo al momento de prepararlo: **50 archivos modificados y 8 archivos nuevos**. Se agrupan por comportamiento relacionado, y se indica para cada archivo qué aporta y qué se perdería si se omitiera. Los cambios de frontend, configuración general del repositorio y Docker no forman parte del inventario backend.

## 1. Registro y verificación de organizaciones

Se amplió el registro institucional para que fundaciones y veterinarias creen una organización asociada a su usuario y aporten información verificable. La solicitud de revisión permite enviar un documento de soporte, el administrador puede consultarlo y resolver la verificación; se guardan los cambios de estado y se notifica al usuario.

| Archivo | Cambio y propósito | Si se omitiera |
|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/controller/AuthController.java` | Añade el endpoint multipart para registrar cuentas institucionales con sus datos y documento opcional. | El cliente no podría enviar en una sola operación los datos del registro y el archivo de soporte. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/security/SecurityConfig.java` | Permite el endpoint institucional como ruta pública de registro, igual que el registro ordinario. | Spring Security bloquearía el registro institucional antes de que el servicio lo procese. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/auth/RegistroUsuarioDTO.java` | Incluye nombre de entidad, NIT, documento legal y municipio de operación; conserva el constructor anterior para compatibilidad con los usos existentes. | No podrían transportarse los datos necesarios para crear la organización y los clientes/test antiguos podrían dejar de compilar. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/AuthService.java` | Declara la variante de registro que recibe el documento multipart. | El controlador no tendría un contrato de servicio que permita delegar el nuevo registro. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/AuthServiceImpl.java` | Crea y asocia entidades `Fundacion` o `Veterinaria`, valida los datos requeridos y la unicidad del NIT, asigna el municipio y adjunta el documento cuando se envía. | Las cuentas institucionales quedarían sin entidad asociada o no se podrían registrar con los datos que la verificación necesita. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/repository/OrganizacionRepository.java` *(nuevo)* | Añade persistencia y consulta de existencia por NIT. | No se podría guardar la organización ni prevenir NIT duplicados mediante el repositorio. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/repository/MunicipioRepository.java` | Añade búsqueda de municipio por nombre sin distinguir mayúsculas/minúsculas. | No se podría resolver el municipio de operación informado por nombre en el registro o la revalidación. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/entity/Organizacion.java` | Relaciona la organización con el documento almacenado y conserva nombre y tipo MIME para presentarlo en la bandeja administrativa. | La organización no tendría dónde vincular el soporte ni metadatos para mostrarlo y descargarlo. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/entity/DocumentoSoporte.java` *(nuevo)* | Define la entidad persistente con nombre, tipo de contenido y bytes del documento. | No habría una representación persistente para conservar el soporte recibido. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/util/DocumentoSoporteUtils.java` *(nuevo)* | Valida presencia y tamaño máximo de 5 MB, reconoce PDF/JPG/PNG por contenido y normaliza el nombre antes de asociar el archivo. | Se aceptarían archivos vacíos, excesivos o de tipos no admitidos sin la validación centralizada. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/controller/VerificacionController.java` | Añade la solicitud multipart de revisión para cuentas institucionales y la consulta administrativa del documento con MIME, `no-store` y `nosniff`. | No existirían las rutas para solicitar revisión ni para que el administrador examine el soporte de manera restringida. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/VerificacionService.java` | Expone contratos para solicitar revisión y recuperar el documento de soporte. | El controlador carecería de operaciones de servicio para esas acciones. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/VerificacionServiceImpl.java` | Gestiona solicitudes y revalidaciones, valida rol y asociación entre rol y tipo de organización, persiste el soporte y actualiza estados; al resolver verifica la organización y notifica el resultado. | Se podrían aprobar cuentas sin una organización consistente, no se podrían revalidar con nuevo soporte ni guardar una decisión coherente en usuario y organización. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/verificacion/VerificacionPendienteDTO.java` | Expone NIT, municipio, nombre del soporte y disponibilidad, manteniendo el constructor previo. | La bandeja administrativa no podría mostrar esos datos ni identificar si hay un documento para revisar. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/verificacion/DocumentoSoporteDTO.java` *(nuevo)* | Transporta nombre, MIME y contenido del documento entre servicio y controlador. | No habría un contrato tipado para servir el soporte al administrador. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/mapper/VerificacionMapper.java` | Mapea detalles de organización y del soporte a la cola administrativa y devuelve el estado real de verificación. | La API retornaría datos incompletos o estados que no coinciden con la organización persistida. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/usuario/UsuarioPerfilDTO.java` | Añade organización, estado de verificación y datos de custodia/capacidad al perfil. | Los consumidores del perfil no podrían conocer la condición institucional y capacidad asociada. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/mapper/UsuarioMapper.java` | Mapea la organización y usa su municipio cuando el usuario no tiene municipio de interés individual. | La respuesta del perfil omitiría información institucional o ubicación de operación. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/entity/enums/TipoNotificacion.java` | Añade el tipo de notificación `VERIFICACION`. | Las notificaciones sobre decisiones de verificación no tendrían un tipo semántico propio. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/worker/NotificacionWorker.java` | Genera notificaciones de aprobación/rechazo institucional y agrega transacciones a notificaciones asíncronas que acceden a datos persistidos; ajusta el registro cuando no hay caso asociado. | El usuario no recibiría el resultado de la verificación y las operaciones asíncronas podrían no tener el contexto transaccional requerido. |

## 2. Bitácora clínica y privacidad

Se limitó la lectura de las entradas clínicas y del PDF a veterinarias verificadas. Una veterinaria puede iniciar la atención desde un caso registrado o continuar uno en atención; las modificaciones posteriores quedan restringidas a la veterinaria responsable. El inicio se refleja en el estado, el historial y el feed de seguimiento.

| Archivo | Cambio y propósito | Si se omitiera |
|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/controller/BitacoraController.java` | Protege consulta y descarga PDF con el rol de veterinaria y pasa la identidad del usuario al servicio al generar el PDF. | Las rutas no aplicarían la restricción de rol ni podrían validar al usuario que solicita la historia clínica. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/BitacoraService.java` | Actualiza el contrato de generación del PDF para recibir la identidad del solicitante. | La capa de servicio no podría autorizar la descarga de forma consistente con la consulta clínica. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/BitacoraServiceImpl.java` | Permite tomar casos activos, cambia el caso a `EN_ATENCION`, registra historial y actualización, restringe lectura a veterinarias verificadas y edición/alta a la veterinaria responsable. También valida acceso al PDF. | No habría trazabilidad del inicio de atención ni controles de confidencialidad y responsabilidad clínica en las operaciones de servicio. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/reporte/ResumenMedicoDTO.java` | Incluye el identificador de la persona responsable en el resumen médico. | El consumidor no podría identificar inequívocamente quién está a cargo de la bitácora. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/mapper/CasoMapper.java` | Construye el nombre del responsable desde la organización veterinaria o, como alternativa, desde el usuario, y mapea su identificador. | El resumen podría omitir el profesional cuando no exista una organización asociada. |

## 3. Aportes ciudadanos, ubicación y fotografías

Se habilitaron aportes de ciudadanos externos al caso, conservando controles para el reportante y la fundación custodio verificada. Se restringen los aportes a casos activos, se persisten puntos de referencia y se asocian las fotografías al aporte para que no se mezclen con las imágenes originales del caso.

| Archivo | Cambio y propósito | Si se omitiera |
|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/aporte/CrearAporteDTO.java` | Limita a 300 caracteres el punto de referencia textual. | La ubicación podría exceder el tamaño permitido por el modelo y fallar al persistir. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/AporteServiceImpl.java` | Autoriza aportes conforme al rol y relación con el caso, solo acepta estados activos, guarda ubicación y asocia/reutiliza fotografías con el seguimiento. | Se rechazarían aportes externos válidos, se permitiría aportar a casos cerrados o se perdería la relación correcta entre fotos y aporte. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/entity/Seguimiento.java` | Añade ubicación textual y colección ordenada de fotografías del seguimiento. | El aporte no podría conservar ni exponer sus referencias de ubicación y fotos. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/entity/Foto.java` | Añade la relación opcional con seguimiento y exclusiones de serialización/igualdad para evitar ciclos y comparaciones recursivas. | Las imágenes solo podrían vincularse al caso, mezclando las fotos de aportes con la galería del reporte. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/repository/FotoRepository.java` | Permite localizar una foto del caso aún no asociada a un seguimiento por URL. | El servicio no podría reutilizar la foto recibida para enlazarla al aporte y podría duplicar registros. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/reporte/AporteDetalleDTO.java` | Incluye zona, ubicación textual y URLs fotográficas en el detalle del aporte. | Las respuestas no incluirían datos necesarios para presentar el aporte completo. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/mapper/AporteMapper.java` | Mapea ubicación, zona y fotografías tanto en el detalle como en la respuesta del aporte. | Los datos persistidos no llegarían a los consumidores de la API. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/mapper/CasoMapper.java` | Excluye del álbum del caso las fotos pertenecientes a seguimientos y selecciona la foto principal solo entre las fotos originales. | Las fotos de aportes podrían aparecer erróneamente como fotos principales o generales del caso. |

## 4. Reportes ciudadanos y moderación

Se añadió una operación para que un ciudadano autenticado denuncie casos ajenos que sigan visibles. El motivo se valida, se guarda una marca explícita de moderación pendiente y la operación evita duplicar una marca ya pendiente. La cola administrativa consulta casos pendientes o ya ocultos; al decidir, se limpia la marca y se conserva la visibilidad correspondiente.

| Archivo | Cambio y propósito | Si se omitiera |
|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/controller/ModeracionController.java` | Expone el endpoint de denuncia ciudadana con autorización por rol. | El cliente no tendría una ruta backend para enviar una denuncia de caso. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/moderacion/ReportarCasoDTO.java` *(nuevo)* | Valida que el motivo no esté vacío y tenga entre 10 y 500 caracteres. | El reporte podría llegar sin explicación o con un motivo demasiado corto/largo. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/ModeracionService.java` | Declara la operación de reporte de caso. | El controlador no podría invocar la denuncia desde la capa de servicio. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/ModeracionServiceImpl.java` | Rechaza reportes propios, de roles no ciudadanos o sobre casos no visibles; registra el motivo, marca pendiente y trata de forma idempotente un reporte duplicado. Al resolver, elimina la marca pendiente. | No se aplicarían las reglas de autorización ni quedaría un registro consultable por administración. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/entity/Caso.java` | Persiste el indicador `moderacionPendiente`, separado de la visibilidad pública. | No se podría distinguir un caso señalado para revisión de uno simplemente visible o ya oculto. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/repository/CasoRepository.java` | Busca casos pendientes de moderación o no visibles para alimentar la cola administrativa. | La cola no podría recuperar los casos por el nuevo estado de moderación. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/mapper/ModeracionMapper.java` | Expone el estado pendiente basado en la marca persistida. | La API podría presentar un estado de revisión incorrecto al administrador. |

## 5. Publicación de casos con imágenes y notificaciones

Se añadió una ruta multipart para publicar un reporte con imágenes. El servicio carga las imágenes primero y solo después crea el caso y registra las fotos; ante error intenta limpiar lo cargado. Las notificaciones de publicación se ejecutan tras confirmar la transacción para evitar avisar de reportes que no quedaron guardados.

| Archivo | Cambio y propósito | Si se omitiera |
|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/controller/ReporteController.java` | Añade el endpoint autenticado multipart para crear un caso junto con sus imágenes. | No habría una operación de API para enviar reporte y fotos en una sola petición. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/CasoService.java` | Declara la creación de casos con archivos multipart. | El controlador no tendría un contrato para delegar la publicación con imágenes. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/CasoServiceImpl.java` | Separa la creación de la entidad, sube imágenes antes de persistir, crea fotos principales/secundarias, intenta limpiar archivos de Cloudinary si falla y agenda notificaciones tras el commit. También inicializa relaciones necesarias antes de notificaciones asíncronas y resuelve proxies de organizaciones. | Un fallo de carga podría dejar el reporte guardado sin sus imágenes; una notificación podría anunciar una transacción fallida o no disponer de relaciones cargadas. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/service/impl/CloudinaryServiceImpl.java` | Evita incluir el texto libre del error externo en el log y registra el tipo de excepción. | Los logs podrían exponer mensajes del proveedor que no se necesitan para diagnosticar el tipo de fallo. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/worker/NotificacionWorker.java` | Hace transaccionales las rutinas asíncronas de notificación y evita duplicar el aviso de emergencia al reportante. | Podrían perderse avisos al reportante o producirse duplicados y errores por operaciones asíncronas sin transacción. |

## 6. Datos de respuesta, excepciones y compatibilidad

| Archivo | Cambio y propósito | Si se omitiera |
|---|---|---|
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/dto/reporte/CasoDetalleDTO.java` | Expone identificador y nombre de la fundación custodio en el detalle del caso. | El consumidor no podría identificar la fundación que tiene la custodia. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/mapper/CasoMapper.java` | Completa el mapeo de custodio y responsable clínico, además de separar fotos originales de fotos de aporte. | Los datos nuevos quedarían en entidades pero no en las respuestas REST. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/exception/GlobalExceptionHandler.java` | Devuelve el mensaje de denegación específico en las respuestas HTTP 403. | El cliente recibiría únicamente un mensaje genérico, sin poder explicar la regla de acceso que rechazó la operación. |
| `huellitas-backend/src/main/java/co/edu/uniquindio/huellitas/security/JwtAuthenticationFilter.java` | Registra una advertencia cuando no logra extraer el usuario de un JWT. | Los fallos de extracción del usuario quedarían sin una señal de diagnóstico en los logs. |
| `huellitas-backend/pom.xml` | Actualiza `springdoc-openapi-starter-webmvc-ui` de 2.6.0 a 3.1.0. | La interfaz y las dependencias de documentación OpenAPI seguirían en la versión anterior. |

## 7. Pruebas añadidas o actualizadas

Estos archivos prueban los nuevos contratos, permisos, respuestas y flujos; son cobertura de regresión, no lógica de ejecución de la aplicación.

| Archivo de prueba | Cobertura y valor | Si se omitiera |
|---|---|---|
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/Fase2IntegrationTests.java` | Ajusta el flujo integrado de verificación institucional a organizaciones y prueba operaciones relacionadas con la fundación verificada. | Cambios entre registro, autenticación, verificación y operaciones posteriores podrían romperse sin detección integrada. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/controller/ReporteControllerTest.java` | Cubre el contrato HTTP de la publicación de reportes con imágenes. | El mapeo de multipart, autorización y respuesta del controlador podría regresionar. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/mapper/CasoMapperTest.java` | Comprueba filtrado de fotos de aportes y mapeo de reportante, custodio y responsable clínico. | Los errores de forma y mezcla de datos en el DTO de detalle podrían pasar inadvertidos. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/service/AporteServiceTest.java` | Verifica límites de estado y permisos de ciudadanos externos, incluidos casos con custodio. | Cambios de autorización o aportes a casos cerrados podrían permitir regresiones no detectadas. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/service/BitacoraServiceTest.java` | Comprueba inicio de atención, trazabilidad, acceso clínico y que solo la veterinaria responsable modifique la bitácora. | Se perdería cobertura de las reglas de privacidad y del ciclo de atención. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/service/CasoServiceTest.java` | Comprueba orden de carga/publicación y que un fallo inicial de carga no cree ni notifique un caso. | Se podrían introducir registros incompletos o notificaciones prematuras sin fallar una prueba. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/service/VerificacionServiceImplTest.java` | Cubre decisiones de verificación y flujo de documento/organización. | Podrían regresionar las transiciones institucionales o la gestión del soporte sin señal de prueba. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/worker/NotificacionWorkerTest.java` | Verifica los tipos y destinatarios de notificaciones relacionados con verificación y casos. | La ausencia o duplicación de notificaciones podría pasar inadvertida. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/mapper/UsuarioMapperTest.java` *(nuevo)* | Prueba el mapeo de los datos institucionales al perfil de usuario. | Los nuevos campos del perfil podrían salir incompletos o inconsistentes sin detectarse. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/service/AuthServiceImplTest.java` *(nuevo)* | Prueba registro de cuentas institucionales y creación de la organización asociada. | La validación y asociación del registro institucional no tendría cobertura unitaria. |
| `huellitas-backend/src/test/java/co/edu/uniquindio/huellitas/service/ModeracionServiceTest.java` *(nuevo)* | Prueba reglas del reporte ciudadano y su efecto en la cola de moderación. | Podrían aceptarse reportes propios/duplicados o dejar de marcarse casos para revisión sin detección. |

## Consideraciones

- Los cambios de dominio y persistencia están pensados para que las nuevas propiedades y relaciones queden almacenadas por la configuración JPA existente. Debe confirmarse en el entorno de despliegue que la estrategia de actualización de esquema admite esas columnas y tablas.
- El soporte institucional se limita a PDF, JPG y PNG por contenido y a 5 MB; esta validación no sustituye controles operativos de almacenamiento o cuotas.
- La interfaz administrativa para **marcar** un caso pendiente desde el detalle no forma parte de este conjunto: el backend aquí implementa la denuncia ciudadana y la decisión administrativa existente.

## Validación ejecutada

Se ejecutó `.\huellitas-backend\mvnw.cmd -f huellitas-backend\pom.xml test`: **170 pruebas, 0 fallos, 0 errores**. Durante la primera ejecución, la prueba de integración del PDF recibió 403 porque no enviaba autenticación; se corrigió para usar el token de la veterinaria verificada, y la suite completa pasó en la segunda ejecución.
