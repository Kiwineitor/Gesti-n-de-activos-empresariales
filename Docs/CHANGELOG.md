# Bitácora de cambios

> Registro acumulativo e indefinido de cambios, implementaciones y arreglos del proyecto.
> Nueva entrada arriba: `## FECHA` seguida de una o más `### TIPO · Título`.

| Fecha | Tipo | Descripción |
|---|---|---|
| 2026-09-14 | ✨ | Ficha técnica con variante para administradores (PDF y Word) |
| 2026-09-14 | ✨ | Endpoint de verificación de rol (GET /api/rol/varificar) |
| 2026-09-10 | 🔧 | Ocultar la barra de scroll del sidebar |
| 2026-09-11 | ✨ | Observación de ficha técnica se carga desde BD al abrir el modal |
| 2026-09-11 | ✨ | Fix: recargar datos de solicitud tras generar ficha técnica |
| 2026-09-11 | 🔧 | Rutas de almacenamiento de archivos configuradas dentro del sistema |
| 2026-09-10 | ✨ | Celular y correo opcionales en la solicitud manual |
| 2026-09-10 | ♻️ | Cambio de tipo de dato en FallaReportada (descripcionFallRep → TEXT) |
| 2026-09-09 | 🐛 | Corrección del bug en solicitud manual con sincronizarAdministrativo y campo "Ci" |
| 2026-09-09 | ♻️ | Cambio de tipo de dato en Solicitud (descripcionSol → TEXT) |

## Convenciones

- **Fecha:** `YYYY-MM-DD` (no usar texto suelto).
- **Verificación:** cada entrada debe indicar cómo se validó el cambio.
- **Fuente de verdad:** este archivo se mantiene al día al finalizar cada tarea.

---

## 2026-09-14

### ✨ Nuevo · Ficha técnica con variante para administradores (PDF y Word)

- **Módulo / área:** backend (`sistema_sath`)
- **Descripción:** si el usuario autenticado tiene el rol `ADMINISTRADOR`, las fichas técnicas (normal y de servicio técnico, en PDF y en Word) se generan con plantillas "Admin" que omiten la sección del técnico colaborador y el QR de verificación, dejando únicamente "Recibí conforme" y "Autorización" con cajas más anchas. Para el resto de roles se mantienen las plantillas originales. El rol se detecta por las authorities del `SecurityContext` (`ROLE_ADMINISTRADOR`), sin consultas a BD.
- **Cambios clave:**
  - `ReporteService.java` → los métodos `exportarFichaTecnicaConQR`, `exportarFichaTecnicaConQRServicioTecnico`, `exportarFichaTecnicaComoWord` y `exportarFichaTecnicaServicioComoWord` reciben `boolean varAdmin` y conmutan la plantilla normal / Admin.
  - `Reportes.java` → nuevo helper `esAdmin()` que se pasa en los 4 endpoints (`ficha-tecnica`, `ficha-tecnica-word`, `ficha-tecnica-servicio`, `ficha-tecnica-servicio-word`).
  - `FichaTecnicav3Admin.jrxml` → ajustada a 2 secciones inferiores (Recibí conforme y Autorización ensanchadas); guardada con Jaspersoft Studio 7.0.8.
  - `FichaTecnicav3_ServicioTecnicoAdmin.jrxml` → nueva plantilla Admin para la ficha de servicio técnico (2 secciones, conserva `QRServicioTecnico` y `QRAutorizacion`).
- **Archivos afectados:** `ReporteService.java`, `Reportes.java`, `FichaTecnicav3Admin.jrxml`, `FichaTecnicav3_ServicioTecnicoAdmin.jrxml` (nuevo)
- **Verificación:** `./mvnw compile` compiló sin errores (con `JAVA_HOME` apuntando a un JDK 21 completo, ya que el del sistema es solo JRE).

### ✨ Nuevo · Endpoint de verificación de rol

- **Módulo / área:** backend (`sistema_sath`)
- **Descripción:** nuevo endpoint `GET /api/rol/varificar?nombreRol=...` que responde `{ "existe": true|false }`. Indica si existe un rol con ese nombre; por diseño, la consulta devuelve `true` únicamente para `ADMINISTRADOR`.
- **Cambios clave:**
  - `RolController.java` → `varificarRol()` (ruta "varificar" sin el segundo "i" es intencional).
  - `RolRepository.java` → query nativa `tieneRol()`.
  - `RolService.java` / `RolServiceImpl.java` → nuevo método `tieneRol()` con validación de `null`/blank.
- **Archivos afectados:** `RolController.java`, `RolRepository.java`, `RolService.java`, `RolServiceImpl.java`
- **Verificación:** compilación del backend.

---

## 2026-09-11

### ✨ Nuevo · Observación de ficha técnica se carga desde BD al abrir el modal

- **Módulo / área:** backend (`sistema_sath`) + frontend (`Sath-frontend`)
- **Descripción:** cuando se abre el modal de ficha técnica, ahora se carga automáticamente la última observación guardada en la base de datos (`Solicitud.observacionFicha`). Si se modifica el texto y se genera la ficha, el nuevo texto reemplaza el anterior en la BD.
- **Cambios clave:**
  - `SolicitudDetalleResponseDto` → se agregó campo `observacionFicha`
  - `SolicitudResponseDto` → se agregó campo `observacionFicha`
  - `SolicitudServiceImpl.getDetalle()` → pasa `observacionFicha` al DTO
  - `SolicitudServiceImpl.buildWithQueuePositions()` → pasa `observacionFicha` al DTO
  - `SolicitudManualService` → pasa `observacionFicha` al DTO
  - `SolicitudDetalleView.vue:81` → `observacionTexto = s.observacionFicha || ''`
  - `ReportesView.vue:195` → `observacionTexto = s.observacionFicha || ''`
- **Archivos afectados:** `SolicitudDetalleResponseDto.java`, `SolicitudResponseDto.java`, `SolicitudServiceImpl.java`, `SolicitudManualService.java`, `SolicitudDetalleView.vue`, `ReportesView.vue`
- **Verificación:** `./mvnw compile -q` compiló sin errores

#### Fix: recargar datos tras generar ficha técnica

- **Problema:** al generar la ficha técnica por 1ra vez, la observación se guardaba en BD pero al reabrir el modal para generar por 2da vez, el texto no aparecía porque el objeto `solicitud` en memoria de Vue seguía con el valor anterior
- **Solución:** después de generar la ficha, se recarga la solicitud desde la API (`await cargar()` / `await cargarFichas()`) para obtener la `observacionFicha` actualizada de la BD
- **Archivos afectados:** `SolicitudDetalleView.vue:92`, `ReportesView.vue:206`, `ReportesView.vue:265`

### 🔧 Ajuste · Rutas de almacenamiento de archivos configuradas dentro del sistema

- **Módulo / área:** backend (`sistema_sath`)
- **Descripción:** se cambió la ruta de almacenamiento de archivos (fotografías/evidencias) de una ruta absoluta local (`/home/usic03/Escritorio/ArchivosSath`) a una ruta relativa dentro del sistema (`uploads/`), para que funcione independientemente de la máquina sin necesidad de una ruta exacta.
- **Cambios clave:**
  - `application.properties:73` → `file.storage.archivos=uploads` (antes `/home/usic03/Escritorio/ArchivosSath`)
  - `application-cris.properties:73` → `file.storage.archivos=uploads` (antes `home/usic03/Escritorio/ArchivosSath`, faltaba `/`)
  - `.gitignore` → NO se agrega `uploads/` para permitir versionar archivos subidos en el repositorio
  - Se creó `uploads/evidencias/` en la raíz del backend como directorio destino
- **Archivos afectados:** `sistema_sath/src/main/resources/application.properties`, `sistema_sath/src/main/resources/application-cris.properties`, `sistema_sath/.gitignore`, `sistema_sath/uploads/`
- **Verificación:** `./mvnw compile -q` compiló sin errores; resolución de ruta confirmada en `/home/usic03/IdeaProjects/sistema-hardware/sistema_sath/uploads/evidencias`

---

## 2026-09-10

### 🔧 Ajuste · Ocultar la barra de scroll del sidebar

- **Módulo / área:** frontend (`sistema_sath/Sath-frontend`)
- **Descripción:** se ocultó la barra de scroll visible del menú lateral (sidebar).
- **Cambios clave:**
  - El `nav.sidebar` ahora usa `overflow-hidden` (se quitó `min-h-screen`).
  - Se reforzó el CSS de scrollbar oculto (`scrollbar-width: none`, `scrollbar-color`,
    `::-webkit-scrollbar { display: none }`) aplicado tanto al `nav` como al `ul.perfect-scrollbar`.
- **Archivos afectados:** `sistema_sath/Sath-frontend/src/components/layout/TheSidebar.vue`
- **Verificación:** `npm run build` en `Sath-frontend` compila sin errores.

### ✨ Nuevo · Celular y correo opcionales en la solicitud manual

- **Módulo / área:** frontend (`sistema_sath/Sath-frontend`)
- **Descripción:** en el formulario de Registro de Solicitud de Mantenimiento (solicitud manual),
  los campos "Celular" y "Correo electrónico" dejaron de ser obligatorios. El backend ya los
  toleraba opcionales (servicio, DTO y entidad permiten nulos); solo el front bloqueaba el envío.
- **Cambios clave:**
  - Se eliminaron las validaciones que impedían enviar si celular/correo estaban vacíos.
  - Se quitaron los asteriscos `*` (obligatorio) de las etiquetas Celular y Correo electrónico.
  - Al enviar, si el campo está vacío se envía `null` en lugar de cadena vacía, para no persistir
    valores en blanco.
- **Archivos afectados:** `sistema_sath/Sath-frontend/src/views/SolicitudManualView.vue`
- **Verificación:** revisión del flujo de `enviar()` y del template; el backend ya aceptaba los
  campos opcionales (sin `@Valid`/anotaciones en el DTO, service ignora blanks y entidad nullable).

### ♻️ Refactor · Cambio de tipo de dato en FallaReportada

- **Módulo / área:** backend (`sistema_sath/src/main/java/.../domain/fallaReportada/`)
- **Descripción:** se cambió el tipo de columna de `descripcionFallRep` de `length = 250` a
  `columnDefinition = "TEXT"` para soportar descripciones de falla más largas.
- **Cambios clave:**
  - Anotación `@Column` de `descripcionFallRep` actualizada a `columnDefinition = "TEXT"`.
- **Archivos afectados:** `sistema_sath/src/main/java/com/sath/sistema_sath/domain/fallaReportada/FallaReportada.java`
- **Verificación:** compilación del backend.

## 2026-09-09

### 🐛 Arreglo · Corrección del bug en la solicitud manual con `sincronizarAdministrativo` y campo "Ci"

- **Módulo / área:** backend (`sistema_sath/src/main/java/.../domain/solicitud/manual/`) | frontend (`sistema_sath/Sath-frontend`)
- **Descripción:** se corrigió el bug presentado al momento de realizar la solicitud manual:
  el método `sincronizarAdministrativo` fue refinado con nuevas validaciones y se añadió el
  campo "Ci" (Carnet de Identidad) al formulario de registro manual para el correcto registro.
- **Cambios clave:**
  - **Frontend** (`SolicitudManualView.vue`): nuevo campo "C.I. (Carnet de Identidad)" en el
    formulario; `funcionarioManual` ahora incluye `ci` y se envía en el body del request.
  - **Backend** (`SolicitudManualService.java`):
    - Nuevos helpers `buscarPersonaPorCi(ci)` y `buscarAdministrativoPorCodigo(codigoAdmi)`
      usando `Optional`.
    - Resolución de Persona: primero por CI, luego por la persona del administrativo hallado
      por código, si no se crea una nueva.
    - Asignación null-safe: solo se setean valores no blank/null (con `trim`) para persona,
      unidad y cargo.
    - Resolución de Administrativo: por código administrativo; si no hay código, `findFirstByPersona`.
  - **Config:** cambio de la ruta de `file.storage.archivos` en `application.properties`.
- **Archivos afectados:**
  - `sistema_sath/Sath-frontend/src/views/SolicitudManualView.vue`,
  - `sistema_sath/src/main/java/com/sath/sistema_sath/domain/solicitud/manual/SolicitudManualService.java`,
  - `sistema_sath/src/main/resources/application.properties`
- **Verificación:** compilación del backend y revisión del flujo `enviar()` del formulario.

### ♻️ Refactor · Cambio de tipo de dato en Solicitud para soportar más contexto

- **Módulo / área:** backend (`sistema_sath/src/main/java/.../domain/solicitud/`)
- **Descripción:** se cambió el tipo de columna de `descripcionSol` de `length = 250` a
  `columnDefinition = "TEXT"` para soportar descripciones de solicitud más largas.
- **Cambios clave:**
  - Anotación `@Column` de `descripcionSol` actualizada a `columnDefinition = "TEXT"`.
- **Archivos afectados:** `sistema_sath/src/main/java/com/sath/sistema_sath/domain/solicitud/Solicitud.java`
- **Verificación:** compilación del backend.