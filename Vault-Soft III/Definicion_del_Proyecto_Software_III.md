# Definición del Proyecto - Software III

> **Nota de transcripción (no pertenece al documento original).** Transcripción fiel del PDF "Soft III - Definición del Proyecto" (proyecto *Huellitas Cafeteras*). **Esta versión incorpora una reinterpretación de los diagramas BPMN de las Figuras 1, 2 y 3** (Sección 4), hecha a partir de las imágenes incrustadas en el PDF a alta resolución. El resto del contenido no se modificó.
>
> - **Marcadores de página:** `[PDF pág. N]` es la página real del PDF recibido (25 páginas). `[pág. N]` es la página según el índice del propio documento, que en algunos apartados difiere de la real (ver registro de verificación al final).
> - **Convención de verificación:** ✅ = comprobado en la imagen; ⚠️ = no se puede confirmar y se conserva la advertencia. Donde hay ⚠️ no se completó nada por suposición.
> - **IDs `F1-xx`, `F2-xx`, `F3-xx`:** asignados en esta transcripción para referirse a los elementos de los diagramas; no existen en el documento original.
---

## Portada `[pág. 1]`

**Definición del Proyecto - Software III**

Stefania Herrera Ochoa
C.C. 1090274506

Allison Lopez Caranton
C.C 1088827979

Daniel Valencia Arenas
C.C 1091202998

*(Logo: Universidad del Quindío)*

Cristhian David Henao Hoyos

Universidad del Quindío
Facultad de Ingeniería
Ingeniería de sistemas y computación
Software III

Armenia, Quindío, 2026

---

## Índice `[pág. 2]`

| Sección | Página (según índice) |
|---|---|
| SECCIÓN 1: INTRODUCCIÓN, ALCANCE Y OBJETIVOS | 4 |
| 1.1 Introducción | 4 |
| 1.2 Alcance | 4 |
| 1.3 Objetivos | 6 |
| SECCIÓN 2: INFORMACIÓN DEL CASO DE ESTUDIO | 6 |
| 2.2 Glosario de Términos | 7 |
| SECCIÓN 3: DESCRIPCIÓN DE LA SOLUCIÓN PROPUESTA | 10 |
| 3.1 Descripción de los Actores de Negocio | 10 |
| 3.2 Descripción de los Workers | 11 |
| SECCIÓN 4: PROCESOS DE NEGOCIO | 14 |
| 4.1 Proceso 1: Registro y Clasificación del Caso | 14 |
| 4.2 Proceso 2: Gestión de Atención y seguimiento del caso | 16 |
| 4.3 Proceso 3: Resolución y Cierre del Caso | 19 |
| SECCIÓN 5: CRONOGRAMA DE TRABAJO PROPUESTO | 21 |

---

## HOJA DE CONTROL `[pág. 3]`

| Campo | Valor |
|---|---|
| Proyecto | Huellitas Cafeteras |
| Entregable | Definición del Proyecto |
| Versión/Edición | 0200 |
| Fecha Versión | 21/09/2026 |
| Aprobado por | *(vacío)* |
| Fecha Aprobación | *(vacío)* |
| N° Total de Páginas | 21 |

### REGISTRO DE CAMBIOS

| Versión | Causa del Cambio | Responsable del Cambio | Fecha del Cambio |
|---|---|---|---|
| 01 | Versión Inicial | Daniel Valencia Arenas | 12/09/2026 |
| 02 | Versión Final | Allison Lopez, Stefania Herrera, Daniel Valencia | 21/09/2026 |

### AUTORES

Nombre y apellidos:

- Allison López Caranton - allison.lopezc@uqvirtual.edu.co
- Stefania Herrera Ochoa - stefaniaj.herreraa@uqvirtual.edu.co
- Daniel Valencia Arenas - daniel.valenciaa1@uqvirtual.edu.co

---

## SECCIÓN 1: INTRODUCCIÓN, ALCANCE Y OBJETIVOS `[pág. 4]`

### 1.1 Introducción `[pág. 4]`

En la región del Eje Cafetero, la gestión de reportes sobre animales en situación de vulnerabilidad (heridos, maltratados o abandonados) y la búsqueda de mascotas extraviadas puede ser un proceso complicado y poco eficiente, ya que actualmente los ciudadanos y las organizaciones de protección animal utilizan principalmente medios de comunicación informales, como grupos de mensajería y redes sociales.

Esta falta de organización puede provocar que la información se duplique, se pierda el seguimiento de los casos reportados, no esté clara la prioridad de atención y que los refugios o veterinarias se saturen, lo cual deja claro que la ausencia de un medio centralizado dificulta la coordinación entre los ciudadanos, los rescatistas independientes y los profesionales de la salud animal, generando tiempos de respuesta más largos y haciendo que algunos casos no se resuelvan a tiempo.

### 1.2 Alcance `[pág. 4]`

El presente proyecto comprende el diseño, especificación, desarrollo y despliegue de una plataforma web centralizada para la gestión, seguimiento y resolución de reportes de animales en situación de vulnerabilidad y extraviados en la región del Eje Cafetero. A continuación se definen los límites del mismo.

**Componentes dentro del alcance:**

- Para el desarrollo del **Frontend** se plantea la construcción de una aplicación web utilizando el framework **Angular**, mediante una arquitectura basada en componentes desacoplados, bajo el enfoque de SPA (Single Page Application) y con un diseño responsive que permita su adaptación a dispositivos móviles y de escritorio.
- Para el desarrollo del **Backend** se implementará una **API RESTful** utilizando el lenguaje **Java** junto con **Spring Boot**, la cual se encargará de gestionar la lógica de negocio, la autenticación de los usuarios, los estados de los casos y la exposición de endpoints seguros.
- Para la **gestión y persistencia de la información** se utilizará una base de datos relacional **PostgreSQL**, estructurada bajo el principio de integridad referencial, con el propósito de almacenar los perfiles de usuario, reportes, bitácoras de seguimiento y roles establecidos dentro del sistema.
- Para la **infraestructura** del sistema se contempla el despliegue de sus diferentes servicios en plataformas de nube, para facilitar su disponibilidad y administración durante el desarrollo y la implementación del proyecto. En este sentido se tiene pensado para el despliegue del Frontend desarrollado en Angular, la plataforma **Vercel**, mientras que para los servicios correspondientes al backend y la persistencia de datos, se utilizará la plataforma de nube **Railway**.
- Con propósito de estandarizar el entorno de desarrollo entre el equipo, se implementará **Docker Compose**, permitiendo coordinar los servicios de la base de datos PostgreSQL y el backend Java durante el desarrollo local.
- En cuanto a las **funcionalidades principales**, el sistema contemplará la autenticación de usuarios según sus roles, el registro con fotografía y ubicación por municipio y barrio, un feed o listado de casos con filtros según su nivel de urgencia, una bitácora para el seguimiento asistencial o médico y la validación del reencuentro de mascotas.
- El proyecto abarca la digitalización, modelado e implementación de los tres procesos clave definidos para la plataforma: *Registro y Clasificación del Caso*, *Gestión de Atención y Seguimiento del Caso*, y *Resolución y Cierre del Caso*.
- El proyecto cubrirá las fases clave del ciclo de vida del software: análisis de requisitos, diseño de arquitectura, desarrollo, pruebas (SQA) y despliegue.

**Componentes fuera del alcance:**

- **Visualización de un Mapa Interactivo en Tiempo Real:** La plataforma no contempla la integración visual de mapas dinámicos o interactivos (tales como Google Maps API o Leaflet) para posicionar pines ni mostrar ubicaciones en tiempo real. La geolocalización de los casos y centros se resolverá exclusivamente mediante un esquema de categorización y filtrado regional por municipios y barrios del Eje Cafetero (Armenia, Pereira, Manizales, etc.). Este módulo está excluido del alcance del proyecto pero se considerará como una propuesta de trabajo futuro.
- **Comparación de Imágenes mediante Inteligencia Artificial (IA):** No se contempla dentro del alcance la integración de modelos de visión por computador o algoritmos de procesamiento de imágenes para la comparación automática de fotografías de animales registrados. Al igual que al anterior, este componente se concibe como una funcionalidad optativa o propuesta de desarrollo futuro.

### 1.3 Objetivos `[pág. 6]`

**Objetivo General:**

Desarrollar una solución tecnológica centralizada para el Eje Cafetero que optimice los tiempos de respuesta y la trazabilidad en la atención de animales en situación de vulnerabilidad y la localización de mascotas extraviadas, conectando de forma directa a la ciudadanía, rescatistas y veterinarias.

**Objetivos Específicos:**

- Implementar un módulo de registro y geolocalización (ubicación por zona) de reportes que reduzca la fragmentación y duplicidad de información de animales vulnerables o perdidos.
- Diseñar un sistema de categorización y filtrado por nivel de urgencia geográfica para priorizar los casos que requieran intervención médica o auxilio inmediato.
- Estructurar una bitácora digital de seguimiento asistencial que permita registrar información médica y el estado del animal.
- Desplegar una arquitectura de software desacoplada aplicando estándares de Calidad y asegurando tiempos de respuesta de la aplicación en menos de 5 segundos.
- Implementar un pipeline de integración y despliegue continuo (CI/CD) en Railway/Vercel incorporando pruebas unitarias e integración con un umbral mínimo de cobertura del 60% (SQA).
- Formalizar el flujo de resolución, el registro de resultados y la conservación del historial de cierre, asegurando que los casos finalizados se archiven correctamente y dejen de saturar el listado principal de casos activos.

---

## SECCIÓN 2: INFORMACIÓN DEL CASO DE ESTUDIO `[pág. 6]`

### 2.1 Información del Caso de Estudio `[PDF pág. 6]`

> Nota: el índice del documento no lista el apartado 2.1, pero el cuerpo del documento sí lo incluye con este numeral.

El dominio del proyecto abarca el sector comunitario de protección y bienestar animal en la región del Eje Cafetero. Este ecosistema está compuesto por tres actores principales: los **ciudadanos reportantes** (generadores del dato inicial), las **fundaciones** (gestores de logística y resguardo) y las **clínicas veterinarias** (prestadores de servicios de salud animal).

La digitalización de este sector responde a necesidades críticas en la dinámica social de la región, ya que se visibilizó desde eventos como el terremoto del 10 de agosto y también frente a la evidente falta de control y organización en los medios actuales. Hoy en día, la publicación masiva e informal de animales perdidos o en situación de vulnerabilidad a través de redes sociales y aplicaciones de mensajería carece de filtros, trazabilidad y moderación, lo que genera desinformación, duplicidad de reportes y la saturación de los rescatistas.

Para superar esta problemática, el proyecto propone cambiar la gestión empírica y tradicional por un modelo asistencial estructurado bajo el enfoque de **Calidad por Diseño (Quality by Design)**. Al implementar un sistema de información relacional, se automatiza el flujo de trabajo, se establece un control riguroso sobre las publicaciones, se formaliza el seguimiento técnico de cada animal y se optimiza el uso de los recursos, garantizando que la plataforma opere a través de procesos claros, estructurados y verificables.

### 2.2 Glosario de Términos `[pág. 7]`

| Término | Definición |
|---|---|
| Angular | Framework de desarrollo de código abierto mantenido por Google para la construcción de aplicaciones web de una sola página (Single Page Application) basadas en TypeScript. |
| API (Application Programming Interface) | Conjunto de definiciones y protocolos que se utilizan para diseñar e integrar el software de las aplicaciones, permitiendo la comunicación entre el frontend y el backend. |
| Bitácora de Seguimiento | Registro cronológico y auditable donde se documentan los eventos, evoluciones de salud y cambios de estado operacional asociados a un caso registrado en el sistema. |
| Caso Activo | Estado del registro de un animal dentro de la plataforma que requiere auxilio, atención médica o que se encuentra en proceso de búsqueda por extravío. |
| CI/CD (Continuous Integration / Continuous Deployment) | Práctica de ingeniería de software que automatiza la integración y el despliegue de código en entornos de producción o staging de manera frecuente y confiable. |
| Docker Compose | Herramienta para definir y ejecutar aplicaciones Docker de múltiples contenedores mediante un archivo de configuración en formato YAML, utilizada para estandarizar el entorno de desarrollo local. |
| Endpoint | URL específica expuesta por una API REST a la cual los clientes (frontend) envían peticiones HTTP para consumir o manipular recursos de la base de datos. |
| Geolocalización por Zonas | Método de ubicación que clasifica e indexa la información geográfica mediante campos estructurados correspondientes a divisiones político-administrativas (departamentos, municipios y barrios). |
| Java | Lenguaje de programación orientado a objetos, tipado y multiplataforma, utilizado para el desarrollo de la lógica de negocio en el backend. |
| JSON (JavaScript Object Notation) | Formato ligero de intercambio de datos, legible por humanos y máquinas, utilizado para estructurar las respuestas y peticiones en las interfaces API RESTful. |
| JWT (JSON Web Token) | Estándar abierto (RFC 7519) para la creación de tokens de acceso compactos y autocontenidos que garantizan la autenticación y autorización segura de usuarios en la API. |
| Ley 1581 de 2012 | Disposición legislativa expedida por el Congreso de la República de Colombia para la protección de datos personales y la regulación del tratamiento de información sensible de los ciudadanos. |
| PMV (Producto mínimo viable) | Versión inicial de un producto de software que incluye únicamente las funcionalidades indispensables para resolver el problema central y permitir su validación en un entorno real. |
| PostgreSQL | Sistema de gestión de bases de datos relacional orientado a objetos, de código abierto, que garantiza la integridad referencial y el cumplimiento de propiedades ACID en las transacciones. |
| Railway | Plataforma como servicio (PaaS) en la nube utilizada para desplegar, gestionar y escalar aplicaciones backend y motores de bases de datos relacionales. |
| REST (Representational State Transfer) | Estilo de arquitectura de software para sistemas hipermedia distribuidos que utiliza peticiones HTTP estándar (GET, POST, PUT, DELETE) para la gestión de recursos. |
| Rol de Usuario | Nivel de autorización asignado a un perfil dentro del sistema (Ciudadano, Fundación, Veterinaria, y Administrador) que determina sus permisos de lectura, escritura y modificación de registros. |
| SPA (Single Page Application) | Aplicación web que interactúa con el usuario reescribiendo dinámicamente la página web actual en lugar de cargar páginas enteras desde un servidor. |
| Spring Boot | Framework basado en Java que simplifica la creación y configuración de microservicios y aplicaciones web autónomas listas para producción con arquitectura API RESTful. |
| SQA (Software Quality Assurance) | Disciplina que abarca el conjunto de actividades, pruebas y estándares aplicados durante el ciclo de vida del software para garantizar que el producto cumpla con los requisitos de calidad definidos. |
| Vercel | Plataforma en la nube optimizada para el alojamiento y despliegue automatizado de aplicaciones web frontend y arquitecturas Jamstack. |

---

## SECCIÓN 3: DESCRIPCIÓN DE LA SOLUCIÓN PROPUESTA `[PDF pág. 9; índice: 10]`

La solución **Huellitas Cafeteras** es una plataforma web centralizada ideada para resolver la fragmentación y falta de seguimiento que existe actualmente en el reporte de animales vulnerables o perdidos en el Eje Cafetero. Para dar solución a las dolencias del sector, el sistema coordina los esfuerzos comunitarios e institucionales mediante un flujo de trabajo digital estructurado en cuatro en cuatro aspectos esenciales: *(sic: el original repite "en cuatro")*

- **Estandarización del Reporte:** Reemplaza las publicaciones informales en redes sociales capturando de forma estructurada los datos del animal (fotografía, urgencia y ubicación por municipio/barrio) dentro de una base de datos única, evitando registros duplicados.
- **Priorización Geográfica:** Elimina los retrasos en casos críticos mediante filtros de urgencia y localización que permiten a fundaciones y veterinarias identificar e intervenir inmediatamente en las emergencias de su zona.
- **Trazabilidad mediante Bitácora Digital:** Resuelve la falta de información sobre el estado de los animales al centralizar un registro clínico y asistencial auditable, accesible para las entidades involucradas durante todo el proceso de atención.
- **Gestión y Descongestión del Sistema:** Evita que los reportes ya resueltos permanezcan publicados indefinidamente administrando estados claros (**Registrado, En Atención, Resuelto, Archivado**) para que la comunidad visualice únicamente las emergencias activas.

### 3.1 Descripción de los Actores de Negocio `[pág. 10]`

**Tabla 1. Actores de Negocio**

| Actor de Negocio | Tipo | Descripción y Responsabilidad |
|---|---|---|
| A-01 Administrador del Sistema | Rol Técnico Interno | Encargado de la plataforma, cuya responsabilidad principal es validar la autenticidad de las credenciales institucionales de las Veterinarias y Fundaciones antes de su activación, además de moderar contenidos impropios o duplicados. |
| A-02 Ciudadano | Persona / Usuario Final | Usuario de la comunidad que registra casos iniciales de animales en vulnerabilidad o mascotas extraviadas, aporta avistamientos y/o comentarios en reportes de terceros, puede actuar como rescatista independiente y confirmar el reencuentro o resolución final de sus casos. |
| A-03 Fundación / Refugio de Protección Animal | Organización / Entidad Verificada | Entidad sin ánimo de lucro verificada en la plataforma, la cual es responsable de asumir formalmente la custodia y resguardo temporal de los animales rescatados, coordinar la logística asistencial a mayor escala y solicitar la atención médica necesaria. |
| A-04 Veterinaria / Profesional de Salud Animal | Entidad / Especialista Verificado | Profesional o centro médico verificado en el sistema, el cual posee la responsabilidad exclusiva de aperturar, actualizar y firmar digitalmente la Bitácora Médica, registrar diagnósticos, tratamientos, procedimientos y emitir el alta médica del animal. |

### 3.2 Descripción de los Workers `[PDF pág. 11; Tabla 2 en pág. 12]`

**Tabla 2. Workers**

| Worker | ¿Qué hace? | ¿Cómo funciona? | Implementación Técnica |
|---|---|---|---|
| **W-01** Worker de Notificaciones y Alertas | Envía avisos automáticos y en tiempo real sobre eventos clave del sistema. | Monitorea eventos en tiempo real (casos graves, estados, citas o pérdidas) y los clasifica para notificar automáticamente a cada rol: emergencias a rescatistas de la zona, citas a veterinarias y seguimiento a usuarios. | Se implementará en Spring Boot mediante un servicio que genere las notificaciones, las almacene en PostgreSQL y las entregue mediante la API REST para mostrarlas dentro de la aplicación. |
| **W-02** Worker de Organización y Filtros | Organiza y filtra los casos por urgencia, tipo y zona para encontrar rápido los que requieren atención. | Usa los datos del reporte (urgencia, tipo y zona normalizada por W-05) para ordenar y filtrar el listado de casos activos cuando el usuario lo consulta. | Servicio en Spring Boot con consultas e índices en PostgreSQL, expuesto por la API REST. |
| **W-03** Worker de Reportes Médicos en PDF | Genera el archivo PDF descargable con la historia clínica del animal según los permisos del rol remitente. | Cuando una veterinaria o rescatista solicita el informe, toma los diagnósticos y tratamientos de la bitácora y los convierte automáticamente en un documento listo para descargar. | Se implementará en Spring Boot mediante una librería de generación de PDF, como iText o PDFBox, tomando la información registrada en el historial médico. |
| **W-04** Worker de Limpieza y Cierre de Casos | Mantiene el sistema actualizado y sin información obsoleta. | Se ejecuta todas las noches para revisar reportes antiguos sin actividad. Envía recordatorios a los usuarios o archiva los casos resueltos para no saturar el listado principal. | Se implementará como una tarea programada en Spring Boot mediante `@Scheduled`, encargada de revisar periódicamente los casos antiguos o resueltos. |
| **W-05** Worker de Mapeo de Zonas | Lee la dirección o texto ingresado (ej: "Barrio Las Mercedes, Armenia") y le asigna sus identificadores estandarizados de Departamento, Municipio y Barrio. | Permite que los filtros por zona (W-02) funcionen con precisión sin que la aplicación se bloquee mientras traduce las ubicaciones. | Se implementará mediante un proceso en Spring Boot que normalice la ubicación ingresada y la relacione con las tablas de departamento, municipio y barrio en PostgreSQL. |
| **W-06** Worker de Procesamiento y Optimización de Imágenes | Gestiona la subida y vinculación de las fotografías del animal al momento de registrar un reporte, delegando el procesamiento, compresión y optimización de archivos al servicio externo de Cloudinary. | Recibe el archivo enviado por el usuario, lo transmite de forma segura a Cloudinary para su optimización automática en la nube y recupera la URL pública generada para almacenarla en la base de datos PostgreSQL. | Se implementará en Spring Boot mediante un servicio integrador que consume la SDK/API REST de Cloudinary, garantizando que el servidor backend no consuma recursos de procesamiento de imágenes ni almacene archivos locales. |

**Nota de Arquitectura:**
Los Workers descritos no constituyen microservicios independientes ni procesos externos, sino componentes de servicio internos en la capa de negocio de Spring Boot. W-01 se ejecuta de forma asíncrona (`@Async`), W-04 mediante tareas programadas (`@Scheduled`), y W-02, W-03, W-05 y W-06 como servicios síncronos invocados durante la petición.

---

## SECCIÓN 4: PROCESOS DE NEGOCIO `[pág. 14]`

En esta sección se presentan los tres procesos procesos *(sic)* de negocio definidos para Huellitas Cafeteras, los cuales describen el flujo que sigue un caso desde su registro inicial hasta su resolución y cierre. Los procesos se encuentran relacionados de manera secuencial y transaccional, de modo que la salida de un proceso contribuye la entrada necesaria para el siguiente proceso. Para cada proceso se especifican su nombre, objetivo, alcance, entradas, salidas, actividades principales, actores involucrados, herramientas utilizadas, frecuencia de ejecución, problemas o dolencias actuales, indicadores de desempeño, normativas, dependencias y su respectiva representación mediante diagramas BPMN 2.0.

### 4.1 Proceso 1: Registro y Clasificación del Caso `[pág. 14]`

**Tabla 3. Primer proceso de negocio**

| Ítem | Descripción |
|---|---|
| Nombre del Proceso | Registro y Clasificación del Caso |
| Objetivo del proceso | Registrar de forma centralizada los casos relacionados con animales en situación de vulnerabilidad o mascotas extraviadas, organizando la información necesaria para facilitar su consulta, priorización y atención. |
| Alcance | El proceso inicia cuando un Ciudadano/Voluntario identifica una situación que requiere ser reportada y registra la información correspondiente en la plataforma. Finaliza cuando el reporte queda almacenado como un caso registrado y clasificado de acuerdo con su tipo, ubicación y nivel de urgencia. |
| Entradas | Información suministrada por el Ciudadano/Voluntario: reporte de caso, información del animal, fotografías, tipo de caso, ubicación mediante departamento, municipio y barrio, y nivel de urgencia correspondiente. |
| Salidas | Caso registrado y clasificado, disponible en la plataforma para su consulta y posterior gestión. Esta salida constituye la entrada necesaria del Proceso de Negocio 2. |
| Actividades/Tareas Clave | 1. El Ciudadano/Voluntario inicia el registro del reporte.<br>2. Establece el nivel de urgencia.<br>3. Ingresa la información descriptiva del animal.<br>4. Adjunta las fotografías correspondientes.<br>5. Selecciona el tipo de caso.<br>6. Registra la ubicación mediante departamento, municipio y barrio.<br>7. El sistema valida la información requerida.<br>8. El reporte es almacenado.<br>9. El caso se clasifica.<br>10. Se confirma el caso registrado.<br>11. Se notifica al reportante el estado del caso.<br>12. El caso queda registrado y clasificado para estar y queda disponible para consulta y seguimiento. *(sic: redacción literal del original)* |
| Roles o actores involucrados | Ciudadano/Voluntario |
| Herramientas o sistemas usados | Plataforma web Huellitas Cafeteras, interfaz, API REST, base de datos y Worker de Organización. |
| Frecuencia de ejecución | A demanda. Se ejecuta cada vez que un usuario registra un nuevo caso de animal en situación de vulnerabilidad o mascota extraviada. |
| Indicadores clave (KPIs) | Porcentaje de reportes registrados con información completa ≥ 90 %. Porcentaje de casos correctamente clasificados por ubicación y nivel de urgencia ≥ 95 %. Tiempo promedio de registro y validación inicial del caso ≤ 5 segundos. Porcentaje de reportes que requieren corrección por información incompleta ≤ 10 %. |
| Problemas o dolencias actuales | La información relacionada con animales vulnerables o mascotas extraviadas se encuentra dispersa en diferentes medios y redes sociales, lo que dificulta su organización, consulta y seguimiento. También puede existir duplicidad de información y dificultad para identificar los casos que requieren atención prioritaria. |
| Normativas o políticas relacionadas | Ley 1581 de 2012 – Protección de Datos Personales. Ley 1774 de 2016 – Protección y Bienestar Animal. Ley 84 de 1989 – Estatuto Nacional de Protección de los Animales. |
| Dependencias con otros procesos | Este es el proceso inicial del flujo. No requiere la ejecución previa de otro proceso de negocio. Su salida, el caso registrado y clasificado, constituye la entrada indispensable para el Proceso 2: Gestión de Atención y Seguimiento del Caso. |
| Mapa o diagrama del proceso | *Figura 1. Diagrama del Proceso 1: Registro y Clasificación del Caso* (ver descripción abajo) |

**Figura 1. Diagrama del Proceso 1: Registro y Clasificación del Caso** `[PDF pág. 16]`

> **Cómo leer esta descripción (no pertenece al documento original).** Se interpretó la imagen incrustada en el PDF (2500 px de ancho), ampliando las zonas con conectores largos.
> - ✅ = origen, destino, punta de flecha y condición visibles y comprobados en la imagen.
> - ⚠️ = no se puede confirmar con la imagen; se describe lo que se ve y **no se completa por suposición**.
> - Los IDs `F1-xx` fueron asignados en esta transcripción solo para poder referirse a los elementos; **no existen en el original**.
> - Las etiquetas se copian tal como aparecen en el diagrama (sin tildes y con erratas del original).

#### Estructura general

- **Pool:** rótulo vertical "PROCESO 1: Registro y Clasificaion del caso" *(sic)*.
- **Carriles (de arriba abajo):** `Ciudadano/Voluntario` · `Sistema (Huellitas cafeteras)` · `Backend`.
- **Evento de inicio:** círculo verde con la etiqueta "inicio", en el carril Ciudadano/Voluntario.
- **Evento de fin:** círculo verde con la etiqueta "Caso registrado y clasificado", en el carril Sistema.
- **Compuertas:** una sola (`F1-G1`), un rombo sin marcador interno visible (el tipo BPMN exclusivo/paralelo no se distingue).
- **Estilo de trazo:** la mayoría de los conectores son continuos con punta de flecha. Un conector es **punteado** (flujo 9).

#### Elementos

| ID | Carril | Tipo | Etiqueta (literal) | Ítem de la Tabla 3 (correspondencia aparente) |
|---|---|---|---|---|
| F1-S | Ciudadano/Voluntario | Evento de inicio | inicio | — |
| F1-01 | Ciudadano/Voluntario | Tarea | Ingresar reporte del caso | 1 |
| F1-02 | Ciudadano/Voluntario | Tarea | Establecer nivel de urgencia | 2 |
| F1-03 | Ciudadano/Voluntario | Tarea | Digelenciar informacion del animal | 3 |
| F1-04 | Ciudadano/Voluntario | Tarea | Adjuntar fotografias | 4 |
| F1-05 | Ciudadano/Voluntario | Tarea | Seleccionar tipo de caso | 5 |
| F1-06 | Ciudadano/Voluntario | Tarea | Registrar ubicacion (departamento, municipio y barrio) | 6 |
| F1-07 | Ciudadano/Voluntario | Tarea | Enviar reporte | sin ítem numerado equivalente |
| F1-08 | Sistema | Tarea | Validar informacion registrada | 7 |
| F1-G1 | Sistema | Compuerta (decisión) | Informacion completa? | implícita en 7 |
| F1-09 | Sistema | Tarea | Solicitar correccion de la informacion | sin ítem equivalente |
| F1-10 | Backend | Tarea | Registrar caso en la base de datos | 8 |
| F1-11 | Backend | Tarea | Clasificar/organizar caso por ubicacion y urgencia | 9 |
| F1-12 | Sistema | Tarea | Confirmar registro del caso | 10 |
| F1-13 | Sistema | Tarea | Notificar al reportante(estado del caso) | 11 |
| F1-E | Sistema | Evento de fin | Caso registrado y clasificado | 12 |

#### Flujos (conectores)

| # | De | A | Condición | Trazo y recorrido | Estado |
|---|---|---|---|---|---|
| 1 | F1-S | F1-01 | — | Continuo, horizontal. | ✅ |
| 2 | F1-01 | F1-02 | — | Continuo, horizontal. | ✅ |
| 3 | F1-02 | F1-03 | — | Continuo, horizontal. | ✅ |
| 4 | F1-03 | F1-04 | — | Continuo, horizontal. | ✅ |
| 5 | F1-04 | F1-05 | — | Continuo, horizontal. | ✅ |
| 6 | F1-05 | F1-06 | — | Continuo, horizontal. | ✅ |
| 7 | F1-06 | F1-07 | — | Continuo, horizontal. | ✅ |
| 8 | F1-07 | F1-11 | — | Continuo. Sale por debajo de F1-07, gira a la izquierda, baja cruzando el carril Sistema sin tocar ninguna tarea y llega con punta de flecha al borde superior de F1-11 (carril Backend). | ✅ |
| 9 | F1-08 | F1-03 | — | **Punteado.** Sale por el borde izquierdo de F1-08, sube y **la punta de flecha está en el borde inferior de F1-03** (no en F1-08). | ⚠️ El trazado es visible; su significado y su dirección lógica no se pueden confirmar (ver "Pendiente de verificación"). |
| 10 | F1-08 | F1-G1 | — | Continuo, horizontal. | ✅ |
| 11 | F1-G1 | F1-09 | **NO** | Continuo, horizontal; etiqueta "NO" junto al rombo. | ✅ |
| 12 | F1-G1 | F1-10 | **SI** | Continuo. Sale por el vértice inferior del rombo (etiqueta "SI"), baja al carril Backend y gira a la derecha con punta de flecha en el borde izquierdo de F1-10. | ✅ |
| 13 | F1-09 | conector entre F1-S y F1-01 | — | Continuo. Sale por la derecha de F1-09, sube al carril Ciudadano/Voluntario, retrocede hacia la izquierda por debajo de las tareas y sube; la punta de flecha **aterriza sobre el conector F1-S → F1-01**, inmediatamente antes de F1-01. | ✅ trazado; ⚠️ el destino es un conector, no un nodo |
| 14 | F1-10 | F1-11 | — | Continuo, horizontal. | ✅ |
| 15 | F1-11 | F1-12 | — | Continuo. Sale por la derecha de F1-11 y sube con punta de flecha al borde inferior de F1-12. | ✅ |
| 16 | F1-12 | F1-13 | — | Continuo, horizontal. | ✅ |
| 17 | F1-13 | F1-E | — | Continuo, horizontal. | ✅ |

#### Lectura del flujo (solo lo dibujado)

1. **Inicio:** F1-S → F1-01 (carril Ciudadano/Voluntario).
2. **Secuencia del ciudadano:** F1-01 → F1-02 → F1-03 → F1-04 → F1-05 → F1-06 → F1-07.
3. **Tras enviar:** el único conector de salida de F1-07 llega a F1-11 (flujo 8).
4. **Bloque de validación (carril Sistema):** F1-08 → F1-G1.
   - **Información completa = SI** → F1-10 (registrar en base de datos) → F1-11.
   - **Información completa = NO** → F1-09 (solicitar corrección) → regresa al conector que entra a F1-01 (flujo 13).
5. **Cierre:** F1-11 → F1-12 → F1-13 → F1-E.
6. **Fin:** el proceso termina en F1-E ("Caso registrado y clasificado"). Es el único evento de fin.

#### Diferencias entre el diagrama y la Tabla 3 (no resueltas; se conservan ambas versiones)

- **Orden de validación y clasificación.** La Tabla 3 lista: 7 validar → 8 almacenar → 9 clasificar. En el diagrama, F1-11 (clasificar) recibe **dos** conectores continuos: uno directo desde F1-07 (Enviar reporte) y otro desde F1-10 (Registrar caso en la base de datos). F1-08 (Validar) **no recibe ningún conector continuo**.
- **Rama de corrección.** El diagrama dibuja la decisión "Informacion completa?" y el ciclo de corrección (F1-09). La Tabla 3 no enumera una actividad equivalente; solo menciona un KPI de "reportes que requieren corrección por información incompleta ≤ 10 %".
- **Actores.** La Tabla 3 lista solo "Ciudadano/Voluntario"; el diagrama añade los carriles Sistema y Backend. La Tabla 3 menciona el "Worker de Organización" como herramienta; **no aparece en el diagrama**.
- **Tarea sin ítem.** "Enviar reporte" (F1-07) no tiene ítem numerado propio en la Tabla 3.

#### Pendiente de verificación manual (⚠️)

1. **Flujo 9 (punteado, F1-08 ↔ F1-03).** La punta de flecha está en F1-03, pero el conector nace en F1-08. No se confirma si representa "la validación devuelve información al ciudadano", una asociación u otra relación, ni si F1-08 debería recibir su entrada desde F1-07.
2. **Entrada de F1-08.** No existe un conector continuo de F1-07 a F1-08. Su relación con F1-07 solo podría inferirse por la Tabla 3, y esa inferencia **no se hace aquí**.
3. **Flujo 13.** Aterriza sobre un conector (F1-S → F1-01), no directamente sobre F1-01. Lo más cercano es "vuelve al inicio del ingreso del reporte", pero el diagrama no lo expresa con un nodo de destino.

### 4.2 Proceso 2: Gestión de Atención y seguimiento del caso `[pág. 16]`

**Tabla 4. Segundo proceso de negocio**

| Ítem | Descripción |
|---|---|
| Nombre del Proceso | Gestión de Atención y Seguimiento del Caso |
| Objetivo del proceso | Gestionar la atención y el seguimiento de un caso previamente registrado, permitiendo la participación de los actores correspondientes y manteniendo actualizada la información sobre las acciones realizadas y la evolución del animal. |
| Alcance | El proceso inicia cuando existe un caso registrado y clasificado proveniente del Proceso 1. Comprende la consulta del caso, los aportes de información, la intervención de Ciudadanos/Voluntarios o Fundaciones/Refugios y cuando se requiere atención médica, la participación de una Veterinaria/Profesional de Salud. Finaliza cuando el caso cuenta con información suficiente sobre la atención realizada y su evolución para continuar hacia su resolución. |
| Entradas | Caso registrado y clasificado proveniente del Proceso 1, incluyendo su descripción, fotografías, ubicación, tipo de animal, nivel de urgencia y demás información disponible. |
| Salidas | Caso atendido y con seguimiento registrado, incluyendo las actualizaciones realizadas durante su atención y, cuando corresponda, la información médica registrada. Esta salida permite iniciar el Proceso de Negocio 3. |
| Actividades/Tareas Clave | 1. Consultar estado del caso.<br>2. Consultar casos mediante filtros.<br>3. Actualizar seguimiento del caso.<br>4. Guardar información de seguimiento.<br>5. Evaluar si la fundación asume la custodia.<br>6. Si la fundación asume la custodia, revisar el caso asignado.<br>7. Gestionar atención o custodia.<br>8. Si la fundación no asume la custodia, el caso continúa con el ciudadano (sin custodia).<br>9. Evaluar si requiere atención médica.<br>10. Si requiere atención médica, registrar atención veterinaria.<br>11. Abrir bitácora médica.<br>12. Registrar diagnóstico, procedimiento y tratamiento.<br>13. Registrar evolución o alta médica.<br>14. Si no requiere atención médica, continuar con la actualización del seguimiento del caso.<br>15. Almacenar registros de seguimiento.<br>16. Generar notificaciones a los usuarios.<br>17. Notificar actualizaciones.<br>18. Consultar actualizaciones del caso. |
| Roles o actores involucrados | Ciudadano/Voluntario, Fundación/Refugio, Veterinaria/Profesional de Salud. |
| Herramientas o sistemas usados | Plataforma Huellitas Cafeteras, Angular, API REST con Spring Boot, PostgreSQL, Worker de Notificaciones y Alertas (W-01), Worker de Organización y Filtros (W-02) y, cuando existe atención médica, Worker de Reportes Médicos en PDF (W-03). |
| Frecuencia de ejecución | A demanda y durante la vigencia de cada caso. Puede ejecutarse varias veces debido a las diferentes actualizaciones, aportes, intervenciones o registros médicos realizados durante el seguimiento. |
| Indicadores clave (KPIs) | Porcentaje de casos activos que reciben al menos una actualización de seguimiento ≥ 80 %. Porcentaje de casos que requieren atención médica y cuentan con Bitácora Médica ≥ 90 %. Porcentaje de casos con seguimiento cuya información se mantiene actualizada ≥ 85 %. Porcentaje de casos con atención veterinaria que cuentan con registro de diagnóstico, tratamiento o evolución ≥ 90 %. |
| Problemas o dolencias actuales | La atención realizada por ciudadanos, rescatistas, fundaciones y veterinarias puede quedar registrada en medios diferentes, dificultando conocer quién intervino, qué acciones fueron realizadas y cuál es el estado actual del animal. La ausencia de un seguimiento centralizado reduce la trazabilidad de los casos. |
| Normativas o políticas relacionadas | Ley 1774 de 2016 – Protección y Bienestar Animal. Ley 84 de 1989 – Estatuto Nacional de Protección de los Animales. Ley 576 de 2000 – Código de Ética para el ejercicio profesional de la Medicina Veterinaria. Ley 1581 de 2012 – Protección de Datos Personales. |
| Dependencias con otros procesos | Depende directamente del Proceso 1, debido a que requiere como entrada un caso previamente registrado y clasificado. Su salida, el caso atendido y con seguimiento registrado, permite iniciar el Proceso 3: Resolución y Cierre del Caso. |
| Mapa o diagrama del proceso | *Figura 2. Diagrama del Proceso 2: Gestión de Atención y seguimiento del caso* (ver descripción abajo) |

**Figura 2. Diagrama del Proceso 2: Gestión de Atención y seguimiento del caso** `[PDF pág. 18]`

> **Cómo leer esta descripción (no pertenece al documento original).** Se interpretó la imagen incrustada en el PDF (2500 px de ancho), ampliando las zonas con conectores largos.
> - ✅ = origen, destino, punta de flecha y condición visibles y comprobados en la imagen.
> - ⚠️ = no se puede confirmar con la imagen; se describe lo que se ve y **no se completa por suposición**.
> - Los IDs `F2-xx` fueron asignados en esta transcripción solo para poder referirse a los elementos; **no existen en el original**.
> - Las etiquetas se copian tal como aparecen en el diagrama (sin tildes en su mayoría).

#### Estructura general

- **Pool:** rótulo vertical "PROCESO 2: Gestion de atencion y Seguimiento del Caso".
- **Carriles (de arriba abajo):** `Ciudadano/Voluntario` · `Fundacion/Refugio` · `Veterinaria` · `Sistema (Huellitas Cafeteras)` · `Backend`.
- **Evento de inicio:** círculo verde "Caso registrado y clasificado", en el carril Ciudadano/Voluntario.
- **Evento de fin:** círculo verde "Caso atendido y con seguimiento registrado", en el carril Ciudadano/Voluntario.
- **Compuertas:** dos (`F2-G1`, `F2-G2`), rombos sin marcador interno visible.
- **Estilo de trazo:** conectores continuos y **tres tramos punteados** (flujos 2, 18 y 19).

#### Elementos

| ID | Carril | Tipo | Etiqueta (literal) | Ítem de la Tabla 4 (correspondencia aparente) |
|---|---|---|---|---|
| F2-S | Ciudadano/Voluntario | Evento de inicio | Caso registrado y clasificado | entrada (viene del Proceso 1) |
| F2-01 | Ciudadano/Voluntario | Tarea | Consultar estado del caso | 1 |
| F2-02 | Sistema | Tarea | Consultar casos mediante filtros | 2 |
| F2-03 | Sistema | Tarea | Actualizar seguimiento del caso | 3 |
| F2-04 | Sistema | Tarea | Guardar informacion de seguimiento | 4 |
| F2-G1 | Fundacion/Refugio | Compuerta (decisión) | ¿Fundación asume custodia? | 5 |
| F2-05 | Fundacion/Refugio | Tarea | Revisar caso asignado | 6 |
| F2-06 | Fundacion/Refugio | Tarea | Gestionar atencion o custodia | 7 |
| F2-07 | Sistema | Tarea | Caso continua con Ciudadano (o sin custodia) | 8 |
| F2-G2 | Veterinaria | Compuerta (decisión) | Requiere atencion medica | 9 |
| F2-08 | Veterinaria | Tarea | Registrar atencion veterinaria | 10 |
| F2-09 | Veterinaria | Tarea | Abrir bitacora medica | 11 |
| F2-10 | Veterinaria | Tarea | Registrar diagnostico, procedimiento y tratamiento | 12 |
| F2-11 | Veterinaria | Tarea | Registrar evolucion o alta medica | 13 |
| F2-12 | Backend | Tarea | Almacenar registros de seguimiento | 15 |
| F2-13 | Backend | Tarea | Generar notificaciones a los usuarios | 16 |
| F2-14 | Sistema | Tarea | Notificar actualizaciones | 17 |
| F2-15 | Ciudadano/Voluntario | Tarea | Consultar actualizaciones del caso | 18 |
| F2-E | Ciudadano/Voluntario | Evento de fin | Caso atendido y con seguimiento registrado | salida |

El ítem 14 de la Tabla 4 ("Si no requiere atención médica, continuar con la actualización del seguimiento del caso") corresponde al flujo 17 (F2-G2 NO → F2-03), no a una tarea.

#### Flujos (conectores)

| # | De | A | Condición | Trazo y recorrido | Estado |
|---|---|---|---|---|---|
| 1 | F2-S | F2-01 | — | Continuo, horizontal. | ✅ |
| 2 | F2-01 | F2-02 | — | **Punteado**, vertical hacia abajo desde F2-01 (carril Ciudadano) hasta F2-02 (carril Sistema), punta de flecha en F2-02. | ✅ |
| 3 | F2-02 | F2-03 | — | Continuo, horizontal. | ✅ |
| 4 | F2-03 | F2-04 | — | Continuo, horizontal. | ✅ |
| 5 | F2-04 | F2-G1 | — | Continuo. Una línea vertical va desde el borde **superior** de F2-04 hacia arriba (cruzando los carriles Veterinaria y Fundación) y gira a la derecha con punta de flecha en el vértice izquierdo de F2-G1. | ✅ destino y recorrido; ⚠️ el origen es inferido por geometría: la línea coincide con el eje vertical de F2-04 y queda oculta tras el cuadro |
| 6 | F2-04 | F2-14 | — | Continuo. Una línea vertical desde el borde **inferior** de F2-04 baja hasta la parte baja del carril Backend, corre hacia la derecha **sin tocar F2-12 ni F2-13**, sube al carril Sistema y llega con punta de flecha al borde izquierdo de F2-14. | ✅ destino y recorrido; ⚠️ origen inferido por geometría (igual que el flujo 5) |
| 7 | F2-G1 | F2-05 | **Sí** | Continuo, horizontal; etiqueta "Sí" junto al rombo. | ✅ |
| 8 | F2-05 | F2-06 | — | Continuo, horizontal. | ✅ |
| 9 | F2-06 | F2-G2 | — | Continuo, vertical hacia abajo, punta de flecha en el vértice superior de F2-G2. | ✅ |
| 10 | F2-G1 | F2-07 | **NO** | Continuo. Sale por el vértice inferior de F2-G1 (etiqueta "NO"), baja, gira a la derecha y baja otra vez con punta de flecha en el borde superior de F2-07 (carril Sistema). | ✅ |
| 11 | F2-07 | F2-G2 | — | Continuo. Sale por la derecha de F2-07, sube y gira a la derecha con punta de flecha en el vértice izquierdo de F2-G2. | ✅ |
| 12 | F2-G2 | F2-08 | **SI** | Continuo, horizontal; etiqueta "SI" junto al rombo. | ✅ |
| 13 | F2-08 | F2-09 | — | Continuo, horizontal. | ✅ |
| 14 | F2-09 | F2-10 | — | Continuo, horizontal. | ✅ |
| 15 | F2-10 | F2-11 | — | Continuo, horizontal. | ✅ |
| 16 | F2-11 | F2-03 | — | Continuo. Sale por el borde inferior de F2-11, baja al carril Sistema, corre hacia la izquierda por la franja **superior** del carril y desciende con punta de flecha al borde **superior** de F2-03. | ✅ |
| 17 | F2-G2 | F2-03 | **NO** | Continuo. Sale por el vértice inferior de F2-G2 (etiqueta "NO"), baja al carril Sistema, corre hacia la izquierda por la franja **inferior** del carril y sube con punta de flecha al borde **inferior** de F2-03. | ✅ |
| 18 | F2-12 | F2-13 | — | **Punteado**, horizontal, punta de flecha en F2-13. | ✅ |
| 19 | F2-13 | F2-14 | — | **Punteado**, vertical hacia arriba, punta de flecha en el borde inferior de F2-14. | ✅ |
| 20 | F2-14 | F2-15 | — | Continuo. Sale por la derecha de F2-14, sube hasta el carril Ciudadano y llega con punta de flecha al borde izquierdo de F2-15. | ✅ |
| 21 | F2-15 | F2-E | — | Continuo, horizontal. | ✅ |

#### Lectura del flujo (solo lo dibujado)

1. **Inicio:** F2-S → F2-01 ⇢ F2-02 (el tramo F2-01 → F2-02 es punteado).
2. **Núcleo de seguimiento:** F2-02 → F2-03 → F2-04.
3. **Desde F2-04 salen dos conectores** (ambos con origen inferido por geometría, ver ⚠️):
   - Hacia arriba a **F2-G1** (¿Fundación asume custodia?).
   - Hacia abajo, vía carril Backend, a **F2-14** (Notificar actualizaciones).
4. **Decisión F2-G1:**
   - **Sí** → F2-05 → F2-06 → F2-G2.
   - **NO** → F2-07 → F2-G2.
5. **Decisión F2-G2 (Requiere atencion medica):**
   - **SI** → F2-08 → F2-09 → F2-10 → F2-11 → regresa a **F2-03** (por su borde superior).
   - **NO** → regresa a **F2-03** (por su borde inferior).
6. **Ciclo:** F2-03 → F2-04 → F2-G1 → … → F2-G2 → F2-03. El diagrama **no rotula ninguna condición de salida de ese ciclo**.
7. **Salida hacia el fin:** F2-14 → F2-15 → F2-E. F2-14 recibe el conector largo desde F2-04 (flujo 6) y el punteado desde F2-13 (flujo 19).
8. **Backend:** F2-12 ⇢ F2-13 ⇢ F2-14. **F2-12 no tiene ningún conector de entrada.**
9. **Fin:** F2-E ("Caso atendido y con seguimiento registrado"). Es el único evento de fin.

#### Diferencias entre el diagrama y la Tabla 4 (no resueltas; se conservan ambas versiones)

- **Actores.** La Tabla 4 lista tres roles (Ciudadano/Voluntario, Fundación/Refugio, Veterinaria/Profesional de Salud). El diagrama añade los carriles **Sistema** y **Backend**.
- **Workers.** La Tabla 4 cita W-01, W-02 y W-03 como herramientas; **ninguno aparece dibujado** en el diagrama.
- **Orden de los ítems 15–17.** La Tabla 4 los lista después de la rama médica (14 → 15 → 16 → 17 → 18). En el diagrama, F2-14 se alimenta directamente desde F2-04 (flujo 6) y F2-12/F2-13 no están conectadas hacia atrás con nada.
- **Nombres de las compuertas.** "¿Fundación asume custodia?" y "Requiere atencion medica" (esta última sin signos de interrogación en el diagrama).

#### Pendiente de verificación manual (⚠️)

1. **Origen de los flujos 5 y 6.** Ambas líneas verticales coinciden con el eje de F2-04 y parecen salir de su borde superior y de su borde inferior, pero la imagen no muestra el punto de unión (la línea queda tapada por el cuadro). Podrían ser dos conectores de F2-04 o, en teoría, un único conector dibujado por detrás. **No se confirma con la imagen.**
2. **F2-12 sin entrada.** No hay ningún conector (ni continuo ni punteado) que llegue a "Almacenar registros de seguimiento". No se infiere de qué tarea procede.
3. **Significado del trazo punteado** (flujos 2, 18 y 19) frente al continuo: el diagrama no incluye leyenda.
4. **Condición de salida del ciclo** F2-03 → F2-04 → F2-G1 … → F2-03: no se rotula.

### 4.3 Proceso 3: Resolución y Cierre del Caso `[PDF pág. 18; índice: 19]`

**Tabla 5. Tercer proceso de negocio**

| Ítem | Descripción |
|---|---|
| Nombre del Proceso | Resolución y Cierre del Caso |
| Objetivo del proceso | Formalizar la resolución de los casos atendidos, registrar su resultado final y evitar que los casos resueltos permanezcan innecesariamente dentro del listado principal de casos activos, conservando su información para garantizar la trazabilidad. |
| Alcance | El proceso inicia cuando un caso proveniente del Proceso 2 cuenta con seguimiento suficiente para determinar su resolución. Comprende la confirmación del resultado correspondiente, la actualización final del estado y el posterior archivado cuando corresponda. Finaliza cuando el caso queda cerrado o archivado, conservando su historial dentro del sistema. |
| Entradas | Caso atendido y con seguimiento registrado proveniente del Proceso 2, junto con las actualizaciones y evidencias generadas durante su atención. |
| Salidas | Caso resuelto y cerrado, con su historial conservado para consulta y trazabilidad y retirado del listado principal de casos activos cuando corresponda. |
| Actividades/Tareas Clave | 1. Consultar el estado y seguimiento acumulado del caso.<br>2. Verificar la información de cierre (evidencias, condiciones, etc.).<br>3. Actualizar el caso como resuelto/cerrado.<br>4. Evaluar si tuvo atención médica.<br>5. Si tuvo atención médica, verificar el alta médica.<br>6. Confirmar el alta médica.<br>7. Evaluar si fue atendido por una fundación.<br>8. Si fue atendido por una fundación, confirmar la recuperación.<br>9. Entregar al cuidador.<br>10. Registrar el resultado de la atención.<br>11. Conservar el historial y la trazabilidad del caso.<br>12. Evaluar si corresponde archivar el caso.<br>13. Si corresponde, archivar el caso.<br>14. Retirar el caso del listado de casos activos.<br>15. Si no corresponde archivar, mantener el registro histórico.<br>16. Generar la notificación de cierre del caso.<br>17. Recibir la notificación de cierre del caso. |
| Roles o actores involucrados | Ciudadano/Voluntario, Fundación/Refugio, Veterinaria/Profesional de Salud, Worker W-04: apoya la revisión y archivado de casos resueltos o sin actividad. |
| Herramientas o sistemas usados | Plataforma Huellitas Cafeteras, Angular, API REST con Spring Boot, PostgreSQL y Worker de Limpieza y Cierre de Casos (W-04). Cuando el caso tenga historial médico, el Worker W-03 puede generar el resumen clínico en PDF. |
| Frecuencia de ejecución | El cierre se realiza a demanda cuando un caso alcanza una condición de resolución. Adicionalmente, el Worker W-04 ejecuta revisiones periódicas de casos antiguos, sin actividad o ya resueltos. |
| Indicadores clave (KPIs) | Porcentaje de casos atendidos que alcanzan el estado resuelto/cerrado ≥ 70 %. Porcentaje de casos cerrados que conservan completo su historial de seguimiento ≥ 95 %. Porcentaje de casos de mascotas extraviadas cerrados con confirmación de recuperación o reencuentro ≥ 90 %. Porcentaje de casos retirados correctamente del listado de activos cuando corresponde su archivo ≥ 95 %. |
| Problemas o dolencias actuales | En los medios informales utilizados actualmente, los reportes pueden permanecer publicados aun después de haber sido resueltos, dificultando diferenciar los casos activos de los finalizados. Además, la falta de un historial centralizado dificulta conocer el resultado final de cada caso. |
| Normativas o políticas relacionadas | Ley 1581 de 2012 – Protección de Datos Personales. Ley 1774 de 2016 – Protección y Bienestar Animal. Ley 84 de 1989 – Estatuto Nacional de Protección de los Animales. Ley 576 de 2000 – Código de Ética para el ejercicio profesional de la Medicina Veterinaria. |
| Dependencias con otros procesos | Depende directamente del Proceso 2, ya que requiere como entrada un caso que haya recibido atención y seguimiento. Es el último proceso de la cadena transaccional y genera como resultado final un caso resuelto y cerrado. |
| Mapa o diagrama del proceso | *Figura 3. Diagrama del Proceso 3: Resolución y Cierre del Caso* (ver descripción abajo) |

**Figura 3. Diagrama del Proceso 3: Resolución y Cierre del Caso** `[PDF pág. 20]`

> **Cómo leer esta descripción (no pertenece al documento original).** Se interpretó la imagen incrustada en el PDF (2500 px de ancho), ampliando las zonas con conectores largos.
> - ✅ = origen, destino, punta de flecha y condición visibles y comprobados en la imagen.
> - ⚠️ = no se puede confirmar con la imagen; se describe lo que se ve y **no se completa por suposición**.
> - Los IDs `F3-xx` fueron asignados en esta transcripción solo para poder referirse a los elementos; **no existen en el original**.
> - Las etiquetas se copian tal como aparecen en el diagrama.

#### Estructura general

- **Pool:** rótulo vertical "PROCESO 3: Cierre del caso" (el título del proceso en el texto del documento es "Resolución y Cierre del Caso").
- **Carriles (de arriba abajo):** `Ciudadano/Voluntario` · `Fundacion/Refugio` · `Veterinaria` · `Sistema (Huellitas Cafeteras)`. No hay carril Backend.
- **Evento de inicio:** círculo verde "Caso atendido y con seguimiento", en el carril Ciudadano/Voluntario.
- **Evento de fin:** círculo verde "Fin", en el carril Ciudadano/Voluntario.
- **Compuertas:** tres (`F3-G1`, `F3-G2`, `F3-G3`), rombos sin marcador interno visible.
- **Estilo de trazo:** conectores continuos y **dos tramos punteados** (flujos 2 y 3).

#### Elementos

| ID | Carril | Tipo | Etiqueta (literal) | Ítem de la Tabla 5 (correspondencia aparente) |
|---|---|---|---|---|
| F3-S | Ciudadano/Voluntario | Evento de inicio | Caso atendido y con seguimiento | entrada (viene del Proceso 2) |
| F3-01 | Ciudadano/Voluntario | Tarea | Consulta el estado y seguimiento acumulado del caso | 1 |
| F3-02 | Sistema | Tarea | Verifica la informacion de cierre(evidencias, condiciones,etc) | 2 |
| F3-03 | Sistema | Tarea | Actualizar caso como resuelto/cerrado | 3 |
| F3-G1 | Sistema | Compuerta (decisión) | ¿Tuvo atención medica? | 4 |
| F3-04 | Veterinaria | Tarea | Verificar alta medica | 5 |
| F3-05 | Veterinaria | Tarea | Confirma alta medica | 6 |
| F3-G2 | Sistema | Compuerta (decisión) | ¿Fue atendido por fundación? | 7 |
| F3-06 | Fundacion/Refugio | Tarea | Confirma recuperacion | 8 |
| F3-07 | Fundacion/Refugio | Tarea | Entrega al cuidador | 9 |
| F3-08 | Fundacion/Refugio | Tarea | Registra resultado de la atencion | 10 |
| F3-09 | Sistema | Tarea | Conservar historial y trazabilidad del caso | 11 |
| F3-G3 | Sistema | Compuerta (decisión) | Corresponde archivar? | 12 |
| F3-10 | Sistema | Tarea | Archivar el caso | 13 |
| F3-11 | Sistema | Tarea | Retirar del listado de casos activos | 14 |
| F3-12 | Sistema | Tarea | Mantener registro historico | 15 |
| F3-13 | Sistema | Tarea | Genera notificacion del cierre del caso | 16 |
| F3-14 | Ciudadano/Voluntario | Tarea | Recibe notificacion del cierre del caso | 17 |
| F3-E | Ciudadano/Voluntario | Evento de fin | Fin | salida |

#### Flujos (conectores)

`J` designa el **punto de confluencia** descrito en los flujos 8 y 14: una línea vertical que baja y aterriza con punta de flecha sobre el conector F3-09 → F3-G3, inmediatamente a la izquierda de F3-G3. `J` **no es un nodo** del diagrama; se nombra aquí solo por comodidad.

| # | De | A | Condición | Trazo y recorrido | Estado |
|---|---|---|---|---|---|
| 1 | F3-S | F3-01 | — | Continuo, horizontal. | ✅ |
| 2 | F3-01 | F3-02 | — | **Punteado**, vertical hacia abajo hasta el carril Sistema, punta de flecha en el borde superior de F3-02. | ✅ |
| 3 | F3-02 | F3-03 | — | **Punteado**, horizontal. | ✅ |
| 4 | F3-03 | F3-G1 | — | Continuo, horizontal. | ✅ |
| 5 | F3-G1 | F3-04 | **Sí** | Continuo. Sale por el vértice derecho de F3-G1 (etiqueta "Sí"), sube al carril Veterinaria y llega con punta de flecha al borde inferior de F3-04. | ✅ |
| 6 | F3-G1 | F3-G2 | **No** | Continuo. Sale por el vértice inferior de F3-G1 (etiqueta "No"), corre a la derecha, sube y llega con punta de flecha al vértice izquierdo de F3-G2. | ✅ |
| 7 | F3-04 | F3-05 | — | Continuo, horizontal. | ✅ |
| 8 | F3-05 | J (conector F3-09 → F3-G3) | — | Continuo. Sale por la derecha de F3-05, corre hacia la derecha por el carril Veterinaria, desciende al carril Sistema y aterriza con punta de flecha sobre el conector F3-09 → F3-G3. | ✅ trazado; ⚠️ el destino es un conector, no un nodo |
| 9 | F3-G2 | F3-06 | **Sí** | Continuo. Sale por el vértice derecho de F3-G2 (etiqueta "Sí"), sube al carril Fundación y llega con punta de flecha al borde izquierdo de F3-06. | ✅ |
| 10 | F3-06 | F3-07 | — | Continuo, horizontal. | ✅ |
| 11 | F3-07 | F3-08 | — | Continuo, horizontal. | ✅ |
| 12 | F3-08 | J (conector F3-09 → F3-G3) | — | Continuo. Sale por el borde inferior de F3-08, baja al carril Veterinaria, corre hacia la izquierda y se une al mismo punto de confluencia `J` del flujo 8. | ✅ trazado; ⚠️ el destino es un conector, no un nodo |
| 13 | F3-G2 | F3-09 | **No** | Continuo. Sale por el vértice inferior de F3-G2 (etiqueta "No"), corre a la derecha, sube y llega con punta de flecha al borde izquierdo de F3-09. | ✅ |
| 14 | F3-09 | F3-G3 | — | Continuo, horizontal. Sobre esta línea aterrizan los flujos 8 y 12 en `J`. | ✅ |
| 15 | F3-G3 | F3-10 | **SI** | Continuo; etiqueta "SI" junto al rombo. | ✅ |
| 16 | F3-10 | F3-11 | — | Continuo, horizontal. | ✅ |
| 17 | F3-11 | F3-13 | — | Continuo. Corre a la derecha, baja y llega con punta de flecha al borde izquierdo de F3-13. | ✅ |
| 18 | F3-G3 | F3-12 | **NO** | Continuo; sale por el vértice inferior de F3-G3 (etiqueta "NO"). | ✅ |
| 19 | F3-12 | F3-13 | — | Continuo. Corre a la derecha y converge con el flujo 17 en el borde izquierdo de F3-13. | ✅ |
| 20 | F3-13 | F3-14 | — | Continuo. Sale por la derecha de F3-13, sube por el lado derecho del diagrama y llega con punta de flecha al borde inferior de F3-14. | ✅ |
| 21 | F3-14 | F3-E | — | Continuo, horizontal. | ✅ |

#### Lectura del flujo (solo lo dibujado)

1. **Inicio:** F3-S → F3-01 ⇢ F3-02 ⇢ F3-03 → F3-G1.
2. **Decisión F3-G1 (¿Tuvo atención medica?):**
   - **Sí** → F3-04 → F3-05 → `J` → F3-G3. Por este camino **no se pasa por F3-G2, por F3-06/07/08 ni por F3-09**.
   - **No** → F3-G2.
3. **Decisión F3-G2 (¿Fue atendido por fundación?):**
   - **Sí** → F3-06 → F3-07 → F3-08 → `J` → F3-G3. Por este camino **no se pasa por F3-09**.
   - **No** → F3-09 → F3-G3.
4. **Decisión F3-G3 (Corresponde archivar?):**
   - **SI** → F3-10 → F3-11 → F3-13.
   - **NO** → F3-12 → F3-13.
5. **Cierre:** F3-13 → F3-14 → F3-E ("Fin"). Es el único evento de fin.

#### Diferencias entre el diagrama y la Tabla 5 (no resueltas; se conservan ambas versiones)

- **Rutas condicionales.** La Tabla 5 enumera los ítems 4 a 11 en una secuencia lineal (evaluar atención médica → alta médica → evaluar fundación → recuperación → entregar al cuidador → resultado → conservar historial). En el diagrama, la rama "Sí" de F3-G1 **no evalúa F3-G2** y llega a F3-G3 sin pasar por F3-09; la rama "Sí" de F3-G2 tampoco pasa por F3-09.
- **Conservar historial.** F3-09 ("Conservar historial y trazabilidad del caso") solo queda en el recorrido de la ruta No/No (F3-G1 = No y F3-G2 = No).
- **Título.** El pool se titula "PROCESO 3: Cierre del caso"; la Tabla 5 titula el proceso "Resolución y Cierre del Caso".
- **Actores y herramientas.** La Tabla 5 incluye el Worker W-04 entre los roles y el Worker W-03 entre las herramientas; **ninguno aparece dibujado**. El diagrama no tiene carril Backend.
- **Etiqueta de inicio.** El diagrama dice "Caso atendido y con seguimiento"; la Tabla 5 dice "Caso atendido y con seguimiento registrado" en sus entradas.

#### Pendiente de verificación manual (⚠️)

1. **Punto de confluencia `J` (flujos 8 y 12).** Ambas flechas aterrizan sobre el conector F3-09 → F3-G3, a la izquierda de F3-G3, y no sobre un nodo. La imagen no permite saber si la intención es entrar a F3-G3 o a otra actividad.
2. **Significado del trazo punteado** (flujos 2 y 3) frente al continuo: el diagrama no incluye leyenda.
3. **Cobertura de rutas.** El diagrama no dibuja ningún camino en que un caso con atención médica **y** custodia de fundación pase por F3-06 → F3-08; se deja constancia sin corregirlo.

---

## SECCIÓN 5: CRONOGRAMA DE TRABAJO PROPUESTO `[PDF pág. 20; índice: 21]`

Para garantizar una planificación estructurada, una gestión ágil y un seguimiento riguroso del ciclo de vida del proyecto Huellitas Cafeteras, el cronograma de trabajo ha sido organizado a través de la plataforma Jira Software. La ejecución del proyecto se estructurará en iteraciones de trabajo (Sprints), orientadas a la construcción progresiva de la arquitectura técnica (Spring Boot, Angular y PostgreSQL) y a la digitalización e integración de los tres procesos de negocio definidos.

- **Herramienta de Gestión:** Jira Software
- **Enlace al Espacio de Trabajo:** Proyecto Jira - Huellitas Cafeteras *(en el PDF aparece como texto de hipervínculo; la URL no es visible en el texto extraído → ⚠️ VERIFICAR)*

Las actividades y paquetes de trabajo están agrupados formalmente en cinco Épicas, las cuales cubren las fases de fundamentación técnica y los módulos transaccionales del sistema:

- **Épica 1: Definición del Proyecto:** Comprende el levantamiento de requisitos, delimitación del alcance, alineación de objetivos y formalización del caso de estudio.
- **Épica 2: Definición de la Arquitectura:** Abarca el diseño del modelo entidad-relación, la estructuración de la API RESTful en Spring Boot, la configuración del frontend en Angular y la contenerización del entorno de desarrollo mediante Docker Compose.
- **Épica 3: Módulo 1 - Gestión de Registro y Clasificación:** Enfocada en la implementación de las funcionalidades de captura de reportes, geolocalización por zonas (municipio/barrio) y priorización por niveles de urgencia.
- **Épica 4: Módulo 2 - Gestión de Atención y Seguimiento:** Corresponde al desarrollo de las herramientas de consulta, asignación de custodia por fundaciones, publicación de actualizaciones y la construcción de la Bitácora Médica digital para veterinarias.
- **Épica 5: Módulo 3 - Resolución y Cierre de Casos:** Incluye la lógica de cambio de estados, validación de reencuentros o altas médicas, archivado automático de casos resueltos y emisión de reportes clínicos.

A continuación se adjunta la visualización del Backlog, el desglose de tareas con sus respectivos responsables y las evidencias del cronograma configurado en el entorno de desarrollo.

### Figura 4. Backlog inicial — ⚠️ VERIFICAR (captura de pantalla de Jira, texto pequeño) `[PDF pág. 22]`

Espacio "Huellitas Cafeteras", pestaña Backlog. Contenido legible:

**SCRUM Sprint 1 — 9 sep – 23 sep (5 actividades).** Objetivo del sprint: "Definir claramente el Proyecto que se trabajará de aquí en adelante".

| Clave | Actividad | Épica | Estado | Fecha | Puntos | Asignado (iniciales) |
|---|---|---|---|---|---|---|
| SCRUM-6 | HU1 Secciones 1, 2 y 3.1 del documento de definición del ... *(texto truncado)* | Definición del Proy... | Hecho | 18 sept | 3 | DA |
| SCRUM-8 | HU3 Definición de RF y sección 4 | Definición del Proy... | Hecho | 19 sept | 3 | AC |
| SCRUM-9 | HU4 Validación y Consolidación Avances | Definición del Proy... | Hecho | 19 sept | 2 | DA |
| SCRUM-7 | HU2 Sección 3.2, identidad visual y mockups | Definición del Proy... | Hecho | 20 sept | 5 | SO |
| SCRUM-10 | HU5 Sección 5 | Definición del Proy... | Pendientes | 21 sept (marcada con alerta de vencimiento) | 1 | DA |

**SCRUM Sprint 2:** "Añadir fechas", 0 actividades. Mensaje: "Planifica un sprint arrastrando actividades hasta él o arrastrando el pie de página sobre ella."

*(Las claves de ticket y los puntos se leyeron de una imagen de baja resolución; verificar contra el original.)*

### Figura 5. Tabla de tareas/actividades — ⚠️ VERIFICAR `[PDF pág. 22]`

Vista "Todas las actividades" de Jira, espacio Huellitas Cafeteras, filtro de categoría de estado "Listo", cambiado en la última semana. Muestra "17 de 17" actividades. Columnas: Actividad, Persona asignada, Informador, Prioridad, Estado, Resolución, Creada, Actualizada. Actividades legibles (los ID de ticket y las columnas de persona están truncados o poco legibles):

| Actividad (según se lee) | Estado | Resolución | Prioridad |
|---|---|---|---|
| Estructura de Mockups | Hecho | Listo | Medium |
| Realizar correcciones necesarias | Hecho | Listo | Medium |
| Validar progreso del equipo | Hecho | Listo | Medium |
| Integrar RF, RNF y Procesos de Negocio en el ... *(truncado)* | Hecho | Listo | Medium |
| Definir el Proceso de Negocio 3 | Hecho | Listo | Medium |
| Definir el proceso de Negocio 2 | Hecho | Listo | Medium |
| Definir el Proceso de Negocio 1 | Hecho | Listo | Medium |
| Definir y documentar los Requisitos No Funcio... *(truncado)* | Hecho | Listo | Medium |
| Definir y documentar los requisitos funcionale... *(truncado)* | Hecho | Listo | Medium |
| Desarrollo sección 3.1 | Hecho | Listo | Medium |
| Desarrollo sección 2 | Hecho | Listo | Medium |
| Desarrollo sección 1 | Hecho | Listo | Medium |
| Sección 3.2 - workers | Hecho | Listo | Medium |
| HU4 Validación y Consolidación Avances | Hecho | Listo | Lowest |
| HU3 Definición de RF y RFN y Sección 4 | Hecho | Listo | Medium |
| HU2 Sección 3.2, identidad visual y mockups | Hecho | Listo | Medium |

Fechas de creación/actualización visibles en el rango 16–20 sept 2026. Personas asignadas/informadoras visibles (truncadas): Stefania Herrera, Daniel Valencia, Allison López Caranton. **No se pudo asociar con certeza cada fila a cada persona ni al ID exacto del ticket; se requiere verificación manual.** Se listan 16 filas legibles de las "17 de 17" que indica el contador; la fila restante no es visible en la captura.

### Figura 6. Tablero Kanban — ⚠️ VERIFICAR `[PDF pág. 23]`

Tablero del espacio Huellitas Cafeteras con cuatro columnas:

| Columna | Cantidad | Contenido visible |
|---|---|---|
| Pendientes | 1 | HU5 Sección 5 — fecha de vencimiento 21 sept 2026 — épica "Definición del Proyecto" — SCRUM-10 — subtareas 0/2 |
| En Progreso | 0 | *(vacía)* |
| En Revisión | 0 | *(vacía)* |
| Hecho | 4 | HU1 Secciones 1, 2 y 3.1 del documento de definición del proyecto (vence 18 sept 2026, SCRUM-6); HU3 Definición de RF y RFN y Sección 4 (vence 19 sept 2026); HU4 Validación y Consolidación Avances (vence 19 sept 2026); además HU2 *(la cuarta tarjeta no es completamente visible)* |

### Figura 7. Cronograma y Épicas (Jira) — ⚠️ VERIFICAR `[PDF pág. 23]`

Vista "Cronograma" (línea de tiempo por meses: septiembre, octubre, noviembre, diciembre). Épicas listadas, con "5 de 5":

| Clave | Épica | Estado |
|---|---|---|
| SCRUM-11 | Definición del Proyecto | Barra de progreso parcialmente completa (verde/azul); asociada a "SCRUM Sprint 1" en septiembre |
| SCRUM-27 | Definición Arquitectura | Pendientes |
| SCRUM-28 | Módulo 1: Gestión de Registros y Cla... *(truncado)* | Pendientes |
| SCRUM-29 | Módulo 2: Gestión de Atención y Seg... *(truncado)* | Pendientes |
| SCRUM-30 *(lectura aproximada)* | Módulo 3: Resolución y Cierre de Ca... *(truncado)* | Pendientes |

Barras en la línea de tiempo (lectura aproximada): la épica 1 en septiembre; las épicas "Definición Arquitectura" y "Módulo 1" en octubre; las épicas "Módulo 2" y "Módulo 3" hacia noviembre. Columna "Persona..." con el valor "Sin..." (sin asignar) en todas.

### Figura 8. Diagrama de GANTT inicial `[PDF pág. 24]`

**Encabezado:** PLANTILLA OFICIAL: CRONOGRAMA DE GANTT (*Huellitas Cafeteras*)
Espacio Académico: GESTIÓN DE PROYECTOS TI (IX Semestre) | Ingeniería de Software
Estudiante/Equipo: Allison López Caranton, Stefania Herrera Ochoa, Daniel Valencia Arenas | Fecha: 2026-II

| TOTAL TAREA | PROGRESO GENERAL | HITOS CUMPLIDOS |
|---|---|---|
| 18 | 27,8% | 5 |

Texto de instrucciones bajo el resumen *(parcialmente legible)*: "Instrucciones: Edite únicamente las celdas amarillas (Inicio, Fin, Responsables, Progreso). Las semanas S1 a S14 se sombrearán automáticamente."

| ID | Fase / Tarea de la EDT | Roles Responsables | Inicio (S) | Fin (S) | Progreso % |
|---|---|---|---|---|---|
| **1.0** | **Definición del Proyecto y Validación** | Todos | | | |
| 1.1 | Secciones 1, 2 y 3.1 del documento de definición del proyec... *(truncado)* | Scrum Master / PO | 1 | 2 | 100% |
| 1.2 | Sección 3.2, identidad visual y mockups | Diseñador | 1 | 2 | 100% |
| 1.3 | Sección 4 y definición de RF y RNF | Developer | 1 | 2 | 100% |
| 1.4 | Validación y Consolidación Avances | Scrum Master / PO | 1 | 2 | 100% |
| 1.5 | Sección 5 y Cronograma | Scrum Master / PO | 1 | 2 | 100% |
| **2.0** | **Arquitectura y Construcción** | Todos | | | |
| 2.1 | Diseño de arquitectura del sistema y modelo de datos | Developer | 2 | 4 | 0% |
| 2.2 | Preparación del entorno de desarrollo, repositorio y estructu... *(truncado)* | Developer | 5 | 5 | 0% |
| 2.3 | Desarrollo del frontend web en Angular | Diseñador | 5 | 5 | 0% |
| 2.4 | Consolidación y análisis de requerimientos | Scrum Master / PO | 6 | 6 | 0% |
| 2.5 | Desarrollo del backend, API REST y PostgreSQL | Developer | 7 | 8 | 0% |
| 2.6 | Desarrollo del prototipo navegable | Diseñador/Developer | 9 | 9 | 0% |
| 2.7 | Despliegue y verificación del entorno | Diseñador/Developer | 9 | 10 | 0% |
| **3.0** | **Culminación del Proceso de Construcción y Modelado de P... *(truncado)*** | Todos | | | |
| 3.1 | Desarrollo del Módulo de Gestión de Atención, Bitácora Mé... *(truncado)* | Diseñador/Developer | 8 | 8 | 0% |
| 3.2 | Desarrollo del Módulo Resolución, Cierre de Casos y Archiva... *(truncado)* | Diseñador/Developer | 9 | 9 | 0% |
| 3.3 | Planificación, Diseño y Ejecución del Modelado de Pruebas ( *(truncado)* | Todos | 10 | 10 | 0% |
| 3.4 | Pruebas de Atributos de Calidad y Rendimiento | Todos | 11 | 11 | 0% |
| 3.5 | Ejecución de Pruebas de Software (Unidad, Integración y AP... *(truncado)* | Todos | 12 | 13 | 0% |
| 3.6 | Despliegue Final y verificación del entorno | Diseñador/Developer | 13 | 14 | 0% |

Columnas de semanas: S1 a S14, con barras sombreadas según Inicio/Fin (no transcritas celda por celda; se derivan de las columnas Inicio y Fin).

### Figura 9. Diagrama del trabajo en Jira — ⚠️ VERIFICAR `[PDF pág. 24]`

Gráfico de líneas titulado **"Work item cycle time"**. Eje Y: "Average issue cycle time (horas)", escala 0–60+. Eje X: "Fecha de finalización de la actividad" (2026-09-17 a 2026-09-22). Valores aproximados leídos de los puntos del gráfico (no hay etiquetas numéricas en el original):

| Fecha | Cycle time promedio (horas, aprox.) |
|---|---|
| 2026-09-17 | ~12 |
| 2026-09-18 | ~47 |
| 2026-09-19 | ~43 |
| 2026-09-20 | ~63 |
| 2026-09-21 | ~2 |
| 2026-09-22 | ~0 |

---

## Aclaración `[PDF pág. 25]`

El docente cuenta actualmente con invitación al espacio de Jira Huellitas Cafeteras con rol de Lector, a continuación la respectiva evidencia.

**Evidencia (captura de pantalla):** CRISTHIAN DAVID HENAO HOYOS — cdhenao@uniquindio.edu.co — estado "Invitado" — "Fecha de invitación: hace 4 días".

---

## Registro de verificación de la transcripción (no pertenece al documento original)

1. **Identificadores técnicos:** El documento **no contiene** requisitos RF/RNF numerados, ADR, endpoints concretos ni nombres de clases. Solo los menciona de forma genérica (p. ej. "definición de RF y RNF" en tareas de Jira). Los únicos identificadores del documento son: actores **A-01 a A-04**, workers **W-01 a W-06**, procesos 1–3, tablas 1–5, figuras 1–9 y las claves Jira `SCRUM-n`.
2. **Estados de caso definidos:** Registrado, En Atención, Resuelto, Archivado (Sección 3).
3. **Roles de usuario del glosario:** Ciudadano, Fundación, Veterinaria y Administrador.
4. **Inconsistencias presentes en el original (no corregidas):** (a) "cuatro en cuatro aspectos" en la Sección 3; (b) "tres procesos procesos" en la Sección 4; (c) Hoja de control indica Versión/Edición "0200" mientras el registro de cambios lista versiones "01" y "02"; (d) la Hoja de control indica **21 páginas** y el PDF recibido tiene **25**; el índice da páginas que difieren de las reales en algunos apartados (Sección 3: índice 10 / real 9; 4.3: índice 19 / real 18; Sección 5: índice 21 / real 20); (e) el apartado "2.1" no aparece en el índice; (f) las actividades del Proceso 1 incluyen el paso 12 con redacción incompleta ("...para estar y queda disponible..."); (g) los actores se llaman "Ciudadano" (Tabla 1) y "Ciudadano/Voluntario" (Tablas de procesos), "Fundación / Refugio de Protección Animal" y "Fundación/Refugio", etc.
5. **Mapa de páginas reales del PDF recibido (25 págs.):** portada 1 · índice 2 · hoja de control 3 · Sección 1 en 4 (1.3 en 6) · Sección 2 en 6 (2.2 en 7) · Sección 3 en 9 (3.1 en 10, 3.2 en 11, Tabla 2 en 12) · Sección 4 en 14 (Tabla 3 en 14; Figura 1 en 16; 4.2 y Tabla 4 en 16; Figura 2 en 18; 4.3 y Tabla 5 en 18; Figura 3 en 20) · Sección 5 en 20 · Figuras 4–5 en 22 · Figuras 6–7 en 23 · Figuras 8–9 en 24 · Aclaración en 25.
6. **Diagramas BPMN (Figuras 1–3), estado de la verificación.** Se leyeron las imágenes incrustadas (2500 px de ancho) y se ampliaron las zonas con conectores largos.
   - **Verificados (✅):** el evento de inicio y el de fin de cada diagrama; todas las tareas y sus carriles; todas las decisiones con sus etiquetas de rama (SI/NO, Sí/No); y todos los conectores listados como ✅ en las tablas de flujos de cada figura (Figura 1: flujos 1–8, 10–12 y 14–17; Figura 2: flujos 1–4 y 7–21; Figura 3: flujos 1–7, 9–11 y 13–21). Los flujos 13 (Fig. 1), 5 y 6 (Fig. 2) y 8 y 12 (Fig. 3) tienen trazado visible pero con la salvedad indicada en ⚠️ más abajo.
   - **Siguen necesitando revisión manual (⚠️):**
     - **Figura 1:** (a) el conector punteado F1-08 ↔ F1-03 (la punta de flecha está en F1-03; no se confirma su significado ni dirección lógica); (b) F1-08 no recibe ningún conector continuo, por lo que no se confirma qué lo alimenta; (c) el flujo 13 aterriza sobre un conector (F1-S → F1-01), no sobre un nodo.
     - **Figura 2:** (a) el origen de los flujos 5 y 6 (líneas verticales que pasan por el eje de F2-04; se infiere que salen de su borde superior e inferior); (b) F2-12 no tiene ningún conector de entrada; (c) significado de los trazos punteados; (d) no se rotula condición de salida del ciclo F2-03 → F2-04 → F2-G1 … → F2-03.
     - **Figura 3:** (a) los flujos 8 y 12 aterrizan sobre un conector (F3-09 → F3-G3), no sobre un nodo; (b) significado de los trazos punteados.
   - **Diferencias entre diagramas y tablas de texto** (documentadas en cada figura, no resueltas): orden validación/clasificación en la Figura 1; rutas que omiten F3-G2 y F3-09 en la Figura 3; carriles Sistema/Backend y Workers que aparecen en las tablas pero no en los diagramas (o viceversa); títulos de pool distintos ("PROCESO 3: Cierre del caso").
7. **Otros elementos que requieren verificación manual:** Figuras 4–7 y 9 (capturas de Jira de baja resolución; claves, asignados y valores aproximados; **no se reinterpretaron en esta versión**) y el hipervínculo del espacio de Jira.
