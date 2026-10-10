

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security_6-JWT_Stateless-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-336791?style=for-the-badge&logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Alpine.js](https://img.shields.io/badge/Alpine.js-3.x-77C1D2?style=for-the-badge&logo=alpine.js&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP%2FSockJS-010101?style=for-the-badge&logo=socketdotio&logoColor=white)

<br/>

> Plataforma web para la gestión integral de solicitudes de mantenimiento y soporte técnico de equipos informáticos.

</div>

---

## 📦 Stack Tecnológico

<table>
  <thead>
    <tr>
      <th>Capa</th>
      <th>Tecnología</th>
      <th>Versión</th>
      <th>Descripción</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>☕ Lenguaje</td>
      <td><b>Java</b></td>
      <td><code>JDK 21 LTS</code></td>
      <td>Versión LTS con Records, Pattern Matching y Virtual Threads</td>
    </tr>
    <tr>
      <td>🍃 Framework</td>
      <td><b>Spring Boot</b></td>
      <td><code>3.5</code></td>
      <td>Autoconfiguración, contenedor embebido Tomcat, Spring DevTools</td>
    </tr>
    <tr>
      <td>🔐 Seguridad</td>
      <td><b>Spring Security + JWT</b></td>
      <td><code>6.x</code></td>
      <td>Autenticación JWT stateless, control de acceso por roles (RBAC), sin cookies de sesión</td>
    </tr>
    <tr>
      <td>🗄️ Base de datos</td>
      <td><b>PostgreSQL</b></td>
      <td><code>16+</code></td>
      <td>Motor relacional, transacciones ACID, queries nativas con aliases en comillas dobles</td>
    </tr>
    <tr>
      <td>🔗 ORM</td>
      <td><b>Hibernate + Spring Data JPA</b></td>
      <td><code>6.x</code></td>
      <td>Mapeo objeto-relacional, repositorios declarativos, auditoría automática</td>
    </tr>
    <tr>
      <td>🖼️ Vistas</td>
      <td><b>Thymeleaf</b></td>
      <td><code>3.1</code></td>
      <td>Motor de plantillas server-side; solo sirve el HTML, la lógica es REST + Alpine</td>
    </tr>
    <tr>
      <td>⚡ Frontend reactivo</td>
      <td><b>Alpine.js</b></td>
      <td><code>3.x</code></td>
      <td>Componentes reactivos sin build step; plantilla VRISTO Admin Dashboard</td>
    </tr>
    <tr>
      <td>🔄 Mapeo de DTOs</td>
      <td><b>MapStruct</b></td>
      <td><code>1.5.5</code></td>
      <td>Generación de mappers en tiempo de compilación (va después de Lombok en pom.xml)</td>
    </tr>
    <tr>
      <td>📡 Tiempo real</td>
      <td><b>WebSocket</b> (STOMP / SockJS)</td>
      <td><code>Spring 6</code></td>
      <td>Notificaciones instantáneas de nuevas solicitudes y cambios de estado</td>
    </tr>
    <tr>
      <td>📊 Reportes</td>
      <td><b>JasperReports</b></td>
      <td><code>7.0.4</code></td>
      <td>Generación de fichas técnicas e informes PDF; secciones single-band sin &lt;band&gt; hijo</td>
    </tr>
    <tr>
      <td>✍️ Boilerplate</td>
      <td><b>Lombok</b></td>
      <td><code>1.18.x</code></td>
      <td>Generación de getters, setters, builders con anotaciones</td>
    </tr>
    <tr>
      <td>🏗️ Build</td>
      <td><b>Maven</b></td>
      <td><code>3.9+</code></td>
      <td>Gestión de dependencias y ciclo de vida del proyecto</td>
    </tr>
  </tbody>
</table>

---

## ✨ Funcionalidades Principales

```
📥  Sincronización automática de solicitudes desde Londra (polling cada 60 s + webhook fallback)
🎯  Asignación de prioridad por atención: ALTO / MEDIO / BAJO
📊  Cola FIFO de llegada + cola priorizada en memoria
🔄  Cambio de estado en cascada: solicitud → detalles → activos
🔌  Actualizaciones en tiempo real vía WebSocket (STOMP / SockJS)
🐛  Catalogación múltiple de fallas por equipo atendido
📋  Auditoría automática con soft-delete en todas las entidades
📄  Generación y descarga automática de fichas técnicas, informes y reportes en PDF y Excel
📂  Reportes históricos del sistema anterior (BD legada, solo lectura) con verificación de disponibilidad
👥  Control de acceso por roles: ADMINISTRADOR / TECNICO / APOYO
🔒  Permisos granulares en el frontend por rol (esAdmin / esTecnico)
🔔  Notificaciones automáticas a Londra al cambiar estado o catalogar falla
👤  Gestión completa de usuarios: crear, editar, restablecer contraseña, editar persona vinculada
```

---

## 🧩 Módulos del Sistema

| Módulo | Roles con acceso | Descripción |
|---|---|---|
| **Dashboard** | Todos | Métricas, gráficas (ApexCharts) y feed WebSocket en tiempo real |
| **Solicitudes** | Todos (escritura: ADMIN + TECNICO) | Lista de solicitudes; cambio de estado con observación |
| **Atenciones** | ADMIN + TECNICO | Atender equipos, catalogar fallas, asignar prioridad y servicio |
| **Técnicos Asignados** | ADMIN + TECNICO | CRUD de asignaciones técnico ↔ solicitud; vistas lista y grid |
| **Administrativos** | ADMIN | Vista detalle de administrativos registrados |
| **Usuarios** | ADMIN | CRUD completo: cuenta, persona vinculada, roles, contraseña |
| **Servicios** | ADMIN (write) / TECNICO (read) | Catálogo de servicios de mantenimiento; vistas lista y grid |
| **Centro de Reportes** | ADMIN + TECNICO | Fichas técnicas, informes específicos y generales en PDF (descarga automática) |
| **Reportes Legacy** | ADMIN + TECNICO | Reportes históricos del sistema anterior (BD `gestion_hardware_uap`); solo lectura |
| **Perfil** | Todos | Ver datos personales completos, cambiar username y contraseña |

---

## 🔐 Seguridad — JWT Stateless

La autenticación es **100% stateless**: sin cookies de sesión, sin estado en servidor.

```
POST /api/auth/login  →  { token, username, roles }
                              ↓
                   Bearer <token> en cada petición
```

### Reglas de acceso por ruta

| Ruta | Roles permitidos |
|---|---|
| `/api/auth/**`, `/ws/**`, páginas HTML | Público |
| `/api/perfil/**` | Cualquier autenticado |
| `/api/solicitud/**` | Cualquier autenticado |
| `/api/atencion-solicitud/**`, `/api/tecnicoAsignado/**`, `/api/reportes/**` | ADMIN + TECNICO |
| `GET /api/servicio/**` | ADMIN + TECNICO |
| `/api/usuario/**`, `/api/persona/**`, `/api/administrativo/**`, `/api/servicio/**` (write) | ADMINISTRADOR |

### Permisos granulares en el frontend

Cada app Alpine expone `esAdmin` / `esTecnico` (`SathAuth.isAdmin()` / `SathAuth.isTecnico()`).
Los botones de escritura usan `x-show` para ocultarse según el rol, sin necesidad de redirigir:

```javascript
// Ejemplo — solo aparece para administradores
x-show="esAdmin"

// Ejemplo — aparece para admin y técnicos
x-show="esAdmin || esTecnico"
```

---


## 🚀 Levantar el Proyecto

### Prerrequisitos

- ✅ **JDK 21** instalado y `JAVA_HOME` configurado
- ✅ **PostgreSQL 14+** corriendo localmente
- ✅ **Maven 3.9+** (o usar el wrapper incluido `./mvnw`)

### Pasos

```bash
# 1. Clonar el repositorio
git clone <url-del-repositorio>
cd sistema_hardware/sistema_sath

# 2. Crear el archivo de configuración (ver sección anterior)
# src/main/resources/application.properties

# 3. Compilar e iniciar
./mvnw spring-boot:run
```

El servidor inicia en → **`http://localhost:9090`**

### 🌱 DataSeeder — Datos de prueba

Al iniciar por primera vez con la base de datos vacía, el **DataSeeder** carga automáticamente:
roles, usuarios, activos de hardware, catálogo de fallas y solicitudes de ejemplo.
En ejecuciones posteriores **no modifica nada** (guard: `if (rolRepository.count() > 0) return`).

**Credenciales de prueba:**

---

## 🗂️ Estructura de Paquetes

```
com.sath.sistema_sath
│
├── 🔧 Config/
│   ├── Auditoria/              ← AuditorAwareImpl (soft-delete + timestamps automáticos)
│   ├── Datasources/            ← PrimaryDataSourceConfig, SecondaryDataSourceConfig (BD legacy)
│   ├── Exception/              ← GlobalExceptionHandler (detecta /api/** → JSON automático)
│   ├── Enum/                   ← EstadoEnum (ACTIVO / INACTIVO / ELIMINADO)
│   ├── LogSistema/             ← LogSistema entity, service y MdcFilter
│   ├── security/               ← SecurityConfiguration, JwtService, CustomUserDetailsService
│   ├── DataSeeder.java         ← Carga inicial de datos (ejecuta una sola vez)
│   └── RestTemplateConfig.java
│
├── 🌐 Controller/
│   ├── auth/                   ← AuthController (login → JWT)
│   ├── usuario/                ← UsuarioController, PerfilController
│   ├── persona/
│   ├── administrativo/
│   ├── solicitud/
│   ├── atencionSolicitud/
│   ├── detalleSolicitud/
│   ├── falla/ + fallaReportada/
│   ├── servicio/
│   ├── tecnicoAsignado/
│   ├── tipoActivo/
│   ├── logs/                   ← LogsController
│   └── reportes/
│       ├── Reportes.java           ← Fichas técnicas, informes y reporte general (JR 7.x)
│       └── ReportesDescontinuados  ← Reportes legacy sobre BD gestion_hardware_uap
│
├── 📡 SolicitudLondra/
│   ├── MantenimientoService    ← Procesamiento de solicitudes recibidas
│   ├── LondraPollingService    ← Polling activo cada 60 s (@Scheduled)
│   └── LondraNotificacionService ← Callbacks salientes a Londra
│
├── 🗃️ colas/
│   ├── ColaSolicitudes         ← Cola FIFO por orden de llegada
│   └── ColaPriorizadaSolicitudes ← Cola por nivel ALTO / MEDIO / BAJO
│
└── 📦 domain/                  ← Entidades, DTOs (MapStruct), repositorios, servicios
    ├── usuario/ + usrRol/ + rol/ + permiso/
    ├── persona/ + administrativo/
    ├── solicitud/ + detalleSolicitud/
    ├── atencionSolicitud/ + tecnicoAsignado/
    ├── falla/ + fallaReportada/
    ├── activo/ + tipoActivo/
    ├── servicio/ + prioridad/ + unidad/ + cargo/
    └── informes/               ← InformeEquipoGeneral + InformeEquipoEspecifico
```
---

## 🗃️ Modelo de Datos

```
Persona ──▶ Usuario ──▶ UsrRol ──▶ Rol ──▶ RolPermiso ──▶ Permiso
         └─▶ Administrativo ──▶ Solicitud ──▶ DetalleSolicitud
                                                    ├──▶ FallaReportada ──▶ Falla
                                                    ├──▶ AtencionSolicitud
                                                    │         ├──▶ TecnicoAsignado
                                                    │         ├──▶ Servicio
                                                    │         └──▶ Prioridad
                                                    ├──▶ InformeEquipoEspecifico
                                                    └──▶ (Activo ──▶ TipoActivo)
                                          Solicitud ──▶ InformeEquipoGeneral
                                          Falla ──▶ TipoActivo
```

**Notas de diseño:**
- `Auditoria` (clase base): soft-delete con `estado = ELIMINADO`, `createdAt`, `updatedAt`, `createdBy`
- `FallaReportada.falla` es **nullable** — se cataloga al momento de atender
- Aliases SQL siempre con comillas dobles: `AS "idSolicitud"` (PostgreSQL convierte sin comillas a minúsculas)
- `DetalleSolicitud` hereda estado de `Solicitud` en cascada: `PENDIENTE → PENDIENTE`, `EN_MANTENIMIENTO → EN_ATENCION`, `FINALIZADO → RESUELTO`

---

## ☕ ¿Por qué Java 21 LTS?

Java 21 es la versión **Long-Term Support (LTS)** más moderna disponible al momento del desarrollo. Incluye mejoras clave que este proyecto aprovecha:

| Característica | Beneficio en SATH |
|---|---|
| **Virtual Threads** (Project Loom) | Manejo eficiente de múltiples conexiones WebSocket |
| **Pattern Matching** | Switch expressions más legibles en lógica de estados |
| **Records** | DTOs inmutables (`UsuarioResponseDto`, `PersonaResponseDto`, etc.) con menos boilerplate |
| **Garbage Collector ZGC/G1** | Menor latencia en operaciones de base de datos |

> Requiere **JDK 21** instalado. Se recomienda [OpenJdk 21](https://openjdk.org//).

---

## 🧰 Comandos útiles

```bash
# Compilar e iniciar en modo desarrollo
./mvnw spring-boot:run

# Solo compilar (verifica errores de MapStruct / Lombok)
./mvnw clean compile

# Ejecutar tests
./mvnw test

# Generar JAR ejecutable
./mvnw clean package -DskipTests
```

---

<div align="center">
