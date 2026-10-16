# Requisitos Funcionales y No Funcionales – Huellitas Cafeteras

> **Fuente:** `RF_y_RNF_docx.pdf` (título interno del PDF: "RF y RNF.docx"), 7 páginas.
> **Mapa de páginas:** p. 1 = intro RF + RF01–RF03 · p. 2 = RF04–RF07 · p. 3 = RF08–RF12 · p. 4 = RF13–RF16 · p. 5 = RF17–RF18 · p. 6 = intro RNF + RNF01–RNF07 · p. 7 = RNF08–RNF14.
> Las marcas `[PDF p. N]` indican la página donde **inicia** cada bloque/requisito.
> El PDF no contiene imágenes, diagramas ni figuras (verificado). No contiene ADR, endpoints ni nombres de clases.

---

## Requisitos Funcionales (RF) `[PDF p. 1]`

Los requisitos funcionales definen las funcionalidades y comportamientos que deberá proporcionar el sistema Huellitas Cafeteras para permitir la interacción de los diferentes actores y apoyar la gestión, seguimiento y solución de casos relacionados con animales en situación de vulnerabilidad o mascotas perdidas en la región del Eje Cafetero.

| ID Requisito Funcional | Descripción | Actor(es) |
|---|---|---|
| **RF01** — Registro de Usuarios por Rol `[p. 1]` | El sistema debe permitir el registro de usuarios, clasificándolos según los roles definidos en la plataforma: Administrador deCiudadano/Voluntario, Fundación/Refugio y Veterinaria/Profesional de Salud, capturando los datos básicos requeridos para la creación de la cuenta. ⚠️ *Ver nota de verificación 1.* | Administrador del sistema, Ciudadano/Voluntario, Fundación/Refugio, Veterinaria/Profesional de Salud |
| **RF02** — Autenticación Segura (JWT) `[p. 1]` | El sistema debe permitir el inicio de sesión mediante credenciales de acceso y generar un token seguro basado en JWT que permita autenticar al usuario y controlar el acceso a las funcionalidades correspondientes a su rol. | Administrador, Ciudadano/Voluntario, Fundación/Refugio, Veterinaria/Profesional de Salud |
| **RF03** — Verificación de Entidades (Veterinarias y Fundaciones) `[p. 1]` | El sistema debe permitir al Administrador validar las cuentas registradas como Veterinaria/Profesional de Salud y Fundación/Refugio antes de habilitar las funcionalidades y permisos exclusivos correspondientes a estos roles. | Administrador |
| **RF04** — Gestión de Perfil de Usuario `[p. 2]` | El sistema debe permitir a los usuarios autenticados consultar y actualizar la información disponible en su perfil, incluyendo sus datos de contacto y la información de ubicación correspondiente a su municipio y barrio de residencia habitual. | Ciudadano/Voluntario, Fundación/Refugio, Veterinaria/Profesional de Salud |
| **RF05** — Creación de Reporte de Animal en Vulnerabilidad o Pérdida `[p. 2]` | El sistema debe permitir al Ciudadano/Voluntario registrar un nuevo caso correspondiente a un animal en situación de vulnerabilidad o una mascota extraviada, adjuntando la información necesaria para su identificación y seguimiento, incluyendo fotografías, descripción del estado físico, tipo de animal, ubicación y nivel inicial de urgencia. | Ciudadano/Voluntario |
| **RF06** — Categorización y Geolocalización por Zonas `[p. 2]` | El sistema debe permitir clasificar geográficamente cada reporte mediante campos estructurados de Departamento, Municipio y Barrio dentro de la región del Eje Cafetero, con el propósito de facilitar la organización y localización de los casos sin requerir mapas interactivos en tiempo real. | Ciudadano/Voluntario |
| **RF07** — Asignación de Nivel de Urgencia `[p. 2]` | El sistema debe permitir clasificar los casos de animales en situación de vulnerabilidad según niveles de prioridad definidos, con el propósito de facilitar la identificación y atención de aquellos que requieran una intervención prioritaria. | Ciudadano/Voluntario |
| **RF08** — Edición y Actualización del Estado del Caso `[p. 3]` | El sistema debe permitir a los actores autorizados actualizar el estado de un caso durante su ciclo de atención y seguimiento, de acuerdo con las acciones realizadas y los permisos correspondientes a cada rol. | Ciudadano/Voluntario, Fundación/Refugio, Veterinaria/Profesional de Salud |
| **RF09** — Aportes y Comentarios de Avistamiento `[p. 3]` | El sistema debe permitir al Ciudadano/Voluntario registrar comentarios, información descriptiva, fotografías o datos de ubicación relacionados con avistamientos de animales reportados, con el propósito de aportar información que facilite su búsqueda, seguimiento o solución. | Ciudadano/Voluntario |
| **RF10** — Feed Principal de Casos Activos `[p. 3]` | El sistema debe mostrar un listado actualizado de los casos activos registrados en la plataforma, presentando información relevante como fotografía del animal, estado actual, nivel de urgencia y ubicación general. | Ciudadano/Voluntario, Fundación/Refugio, Veterinaria/Profesional de Salud |
| **RF11** — Filtrado Multi-criterio `[p. 3]` | El sistema debe permitir filtrar los casos registrados mediante diferentes criterios, incluyendo municipio, barrio, estado del caso, tipo de animal y nivel de urgencia, con el propósito de facilitar la localización de información relevante para el usuario. | Ciudadano/Voluntario, Fundación/Refugio, Veterinaria/Profesional de Salud |
| **RF12** — Consulta Detallada del Caso `[p. 3]` | El sistema debe permitir consultar la información detallada de un caso individual, incluyendo las fotografías registradas, información descriptiva del animal, ubicación general, estado actual, historial de actualizaciones, aportes de la comunidad y resumen asistencial disponible. | Ciudadano/Voluntario, Fundación/Refugio, Veterinaria/Profesional de Salud |
| **RF13** — Apertura de Bitácora Asistencial/Médica `[p. 4]` | El sistema debe permitir a la Veterinaria/Profesional de Salud autorizada abrir una bitácora de seguimiento asociada a un caso que requiera atención médica, con el propósito de registrar cronológicamente la evolución asistencial y médica del animal. | Veterinaria/Profesional de Salud |
| **RF14** — Registro de Evolución Médica y Tratamientos `[p. 4]` | El sistema debe permitir a la Veterinaria/Profesional de Salud registrar en la bitácora médica las evoluciones correspondientes al animal, incluyendo fecha, diagnóstico, procedimientos realizados, tratamientos, medicamentos formulados y observaciones médicas. | Veterinaria/Profesional de Salud |
| **RF15** — Emisión de Alta Médica `[p. 4]` | El sistema debe permitir a la Veterinaria/Profesional de Salud registrar el alta médica del animal cuando finalice la atención requerida, dejando constancia de su condición médica y habilitando la continuación del proceso correspondiente(adopción o regreso a hogar) para la resolución del caso. | Veterinaria/Profesional de Salud |
| **RF16** — Generación de Resumen Clínico en PDF `[p. 4]` | El sistema debe permitir generar, mediante el Worker de Reportes Médicos, un documento en formato PDF que contenga el resumen de la información registrada en la bitácora médica del animal, incluyendo su historial de atención, diagnósticos, tratamientos y evolución. | Veterinaria/Profesional de Salud |
| **RF17** — Validación de Reencuentro / Cierre por Propietario `[p. 5]` | El sistema debe permitir al Ciudadano/Voluntario responsable de un reporte de mascota extraviada confirmar su recuperación o reencuentro, de manera que el caso pueda actualizarse a un estado de resolución o cierre y quede registrada su finalización. | Ciudadano/Voluntario responsable del caso |
| **RF18** — Archivo de Casos Inactivos `[p. 5]` | El sistema debe permitir identificar los casos que permanezcan durante un periodo prolongado sin actualización o interacción y, mediante el Worker de Limpieza y Cierre de Casos, gestionar recordatorios y el archivado de aquellos casos que cumplan las condiciones establecidas, evitando la saturación del listado principal y conservando su trazabilidad. | Sistema / Worker de Limpieza y Cierre de Casos |

---

## Requisitos No Funcionales del Sistema – Huellitas Cafeteras `[PDF p. 6]`

Los Requisitos No Funcionales (RNF) establecen los criterios de calidad, rendimiento, seguridad, disponibilidad, usabilidad, arquitectura y mantenibilidad que cumplirá la plataforma Huellitas Cafeteras. Estos requisitos definen las condiciones bajo las cuales vamos a operar las funcionalidades del sistema y permiten establecer criterios verificables para evaluar su calidad.

| ID Requisito No Funcional | Descripción |
|---|---|
| **RNF01** — Tiempo de Respuesta `[p. 6]` | El sistema debe responder de manera rápida a las consultas realizadas por los usuarios, procurando que las acciones principales no presenten demoras que dificulten su uso. |
| **RNF02** — Procesos en Segundo Plano `[p. 6]` | El sistema debe realizar procesos como el envío de notificaciones y la generación de reportes sin interrumpir o bloquear las demás acciones que esté realizando el usuario. |
| **RNF03** — Carga de Imágenes `[p. 6]` | El sistema debe procurar que las imágenes utilizadas en los reportes tengan un tamaño adecuado para facilitar su carga y visualización sin afectar significativamente el funcionamiento de la plataforma. |
| **RNF04** — Control de Acceso por Roles `[p. 6]` | El sistema debe controlar el acceso a las diferentes funcionalidades según el rol de cada usuario, evitando que una persona pueda realizar acciones para las cuales no tiene autorización. |
| **RNF05** — Protección de Datos de los Usuarios `[p. 6]` | El sistema debe proteger la información personal y de contacto registrada por los usuarios, evitando que pueda ser consultada o modificada por personas que no tengan los permisos correspondientes. |
| **RNF06** — Conexión Segura `[p. 6]` | El sistema debe mantener una comunicación segura entre sus diferentes componentes para proteger la información intercambiada durante el uso de la plataforma. |
| **RNF07** — Validación de Datos de Entrada `[p. 6]` | El sistema debe validar la información ingresada por los usuarios antes de guardarla, evitando datos inválidos, incompletos o que puedan generar errores en el funcionamiento de la plataforma. |
| **RNF08** — Disponibilidad del Sistema `[p. 7]` | El sistema debe mantenerse disponible durante la mayor parte del tiempo de uso previsto, procurando que los usuarios puedan acceder a sus funcionalidades cuando las necesiten. |
| **RNF09** — Organización de los Componentes `[p. 7]` | El sistema debe mantener separados sus principales componentes, como la interfaz de usuario, la lógica del sistema y el almacenamiento de información, facilitando su desarrollo y mantenimiento. |
| **RNF10** — Diseño Adaptable `[p. 7]` | La interfaz del sistema debe adaptarse correctamente a diferentes tamaños de pantalla, permitiendo su utilización desde computadores, tabletas y dispositivos móviles. |
| **RNF11** — Facilidad de Uso `[p. 7]` | El sistema debe ofrecer una interfaz clara y sencilla que permita a los usuarios realizar las acciones principales, especialmente la creación y consulta de reportes, sin requerir conocimientos técnicos. |
| **RNF12** — Entorno de Desarrollo Consistente `[p. 7]` | El sistema debe contar con una configuración que permita a los integrantes del equipo trabajar en un entorno de desarrollo similar, reduciendo problemas ocasionados por diferencias en la configuración de sus equipos. |
| **RNF13** — Integridad de la Información `[p. 7]` | El sistema debe mantener la información almacenada de forma organizada y relacionada correctamente, evitando la existencia de datos duplicados, incompletos o asociados de manera incorrecta. |
| **RNF14** — Pruebas del Sistema `[p. 7]` | El sistema debe ser probado durante su desarrollo para verificar que las funcionalidades principales trabajen correctamente y detectar posibles errores antes de realizar una nueva versión o despliegue. |

---

## Notas de verificación de la conversión

1. **RF01 – texto ambiguo (p. 1):** el PDF dice literalmente "…roles definidos en la plataforma: Administrador deCiudadano/Voluntario, Fundación/Refugio y Veterinaria/Profesional de Salud". La palabra "deCiudadano" aparece unida en el original y la frase queda incompleta u ambigua (no es posible saber si falta una coma o texto). Se transcribió tal cual, sin corregir. La columna de actores de RF01 sí lista claramente los cuatro roles: Administrador del sistema, Ciudadano/Voluntario, Fundación/Refugio, Veterinaria/Profesional de Salud.
2. **RF15 (p. 4):** el original tiene "correspondiente(adopción o regreso a hogar)" sin espacio antes del paréntesis; se conservó tal cual.
3. **Redacción original conservada:** RNF06 y otros usan términos no cuantificados ("rápida", "mayor parte del tiempo", "adecuado"); el documento no define métricas ni umbrales numéricos, y no se añadió ninguno.
4. **Contenido no presente en el PDF:** no hay ADR, endpoints, nombres de clases, criterios de aceptación explícitos, ni definición de los "niveles de urgencia" ni de los "estados del caso"; se mencionan sin enumerarse (RF07, RF08, RF11).
5. **Identificadores técnicos mencionados** (conservados): JWT (RF02), Worker de Reportes Médicos (RF16), Worker de Limpieza y Cierre de Casos (RF18).
