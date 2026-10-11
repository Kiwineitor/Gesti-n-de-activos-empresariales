<div align="center">

<img src="sistema_sath/Sath-frontend/public/images/logo-horizontal.svg" alt="ServiceDesk" height="64"/>

### Sistema de Gestión de Solicitudes de Servicio

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3-42b883?logo=vuedotjs&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

</div>

---

## ¿Qué es?

**ServiceDesk** es una aplicación web para gestionar **solicitudes de servicio técnico / mantenimiento** dentro de una organización.

Un empleado reporta un problema con un **recurso** (una computadora, impresora, vehículo, mueble, aire acondicionado…). La solicitud entra a una **cola**. Un **técnico** la atiende: cataloga la falla, le asigna una prioridad, registra los servicios realizados y la finaliza. Al final se generan **fichas técnicas y reportes** en PDF, Word y Excel.

**Funcionalidades principales**

| | |
|---|---|
| 📝 **Solicitudes** | Registro (también público, sin login), cola de pendientes en tiempo real y seguimiento por estado: `PENDIENTE → EN_MANTENIMIENTO → FINALIZADO` |
| 🛠️ **Atenciones** | El técnico cataloga la falla, asigna prioridad (Alta/Media/Baja), registra servicios y sube evidencias fotográficas |
| 👷 **Técnicos** | Asignación de técnicos a solicitudes, modo de auto-asignación y dashboard por técnico |
| 📦 **Recursos** | Catálogo de recursos por tipo, con historial de atenciones por código |
| 📊 **Reportes** | Reporte general, personalizado (filtros por unidad, tipo de servicio y tipo de recurso) y estadístico; ficha técnica con QR |
| 🔐 **Seguridad** | Login con JWT, roles y permisos por módulo (el menú cambia según el rol) |
| 🔔 **Tiempo real** | Notificaciones vía WebSocket cuando entra una solicitud o cambia su estado |

**Tecnologías:** backend Java 21 + Spring Boot 3.5 (Spring Security/JWT, JPA/Hibernate, JasperReports, WebSocket); frontend Vue 3 + Vite; base de datos PostgreSQL 16. Todo se ejecuta con Docker Compose.

---

## 🚀 Levantar el proyecto con Docker

### Requisitos
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y **abierto**.
- Unos **5 GB libres** en disco para las imágenes. La primera construcción tarda varios minutos.

### Levantar

Desde la carpeta raíz del proyecto (donde está `docker-compose.yml`):

```bash
docker compose up -d --build
```

Esto construye la aplicación (frontend + backend) y levanta dos contenedores:

| Contenedor | Qué es | Puerto |
|---|---|---|
| `servicedesk-app` | La aplicación web (frontend + API) | **9006** |
| `servicedesk-db`  | PostgreSQL con su propia base de datos `servicedesk` | 5433 |

La aplicación tarda unos **20–30 segundos** en arrancar. Para ver cuándo está lista:

```bash
docker compose logs -f app
# Espera la línea: "DataSeeder completado — datos base y de demostración cargados."
# (Ctrl + C para salir de los logs; los contenedores siguen corriendo)
```

### 🌐 Ingresar

👉 **http://localhost:9006**

### 👤 Usuarios de prueba

| Usuario | Contraseña | Rol | Qué puede hacer |
|---|---|---|---|
| `admin` | `admin123` | Administrador | Todo: usuarios, catálogos, configuración, reportes y logs |
| `tecnico` | `tecnico123` | Técnico | Atender solicitudes, ver su dashboard y reportes |
| `tecnico2` | `tecnico123` | Técnico | Igual que el anterior (sirve para probar la asignación) |
| `supervisor` | `supervisor123` | Supervisor | Ver solicitudes y reportes |
| `usuario` | `usuario123` | Usuario | Crear y ver solicitudes |

### Detener

```bash
docker compose stop        # detiene los contenedores (los datos se conservan)
docker compose start       # los vuelve a iniciar
docker compose down        # detiene y elimina los contenedores (los datos se conservan)
docker compose down -v     # ⚠️ también BORRA la base de datos → al volver a levantar se recrean los datos de prueba
```

Si cambias el código, reconstruye con `docker compose up -d --build`.

---

## 🗃️ La base de datos de Docker

- Es una base **propia y local**. No se conecta a ningún servidor externo.
- La primera vez que arranca con la base vacía, Hibernate **crea las tablas** y el `DataSeeder` **carga los datos**:
  - **Datos base:** roles, permisos, prioridades, tipos de servicio, unidades, cargos y el usuario `admin`.
  - **Datos de prueba** (`DEMO_DATA=true`): 4 usuarios más, 6 tipos de recurso con su catálogo de fallas, 10 recursos (`REC-0001`…`REC-0010`), 7 servicios, personal solicitante y **9 solicitudes**: 3 pendientes, 2 en mantenimiento y 4 finalizadas en los últimos meses, para que los dashboards y reportes muestren datos.
- Los datos se guardan en un volumen de Docker y **se conservan** entre reinicios. Para volver a empezar desde cero: `docker compose down -v`.
- Para conectarte con DBeaver o pgAdmin: host `localhost`, puerto `5433`, base `servicedesk`, usuario `servicedesk`, contraseña `servicedesk`.

Para cambiar puertos, contraseñas o desactivar los datos de prueba, copia `.env.example` a `.env` y edita los valores.

---

## 🧭 Recorrido sugerido para ver el sistema

1. **Dashboard** (`admin`). Resumen de solicitudes por estado y gráficos.
2. **Gestión → Solicitudes.** La cola de pendientes y las solicitudes en curso. Abre una para ver su detalle: solicitante, recurso, falla reportada, técnico y servicios.
3. **Gestión → Solicitud Manual.** Registra una solicitud nueva. Prueba con el código `REC-0001`: los datos del recurso se completan solos. Esta pantalla también funciona **sin iniciar sesión** en `http://localhost:9006/solicitud/manual`.
4. **Tiempo real.** Abre la app en dos pestañas y crea una solicitud en una: la otra recibe la notificación al instante.
5. **Atender como técnico.** Cierra sesión y entra como `tecnico`. En **Atenciones**, toma una solicitud pendiente: cataloga la falla, elige la prioridad y los servicios. Luego cambia su estado hasta **FINALIZADO**.
6. **Reportes → Centro de Reportes.** Genera el reporte general, el personalizado o el estadístico en PDF o Excel. Desde una solicitud finalizada genera la **ficha técnica** con QR, en PDF o Word.
7. **Administración** (`admin`). Usuarios y roles, tipos de servicio, servicios, tipos de recurso, fallas. En **Configuración** se define el nombre y cargo de quien firma los reportes.
8. **Compara roles.** Entra como `supervisor` o `usuario`: el menú lateral muestra solo los módulos permitidos.
9. **Developer → Logs** (`admin`). Visor de logs del backend y del frontend.

---

## 🧑‍💻 Desarrollo local (sin Docker para la app)

Solo hace falta la base de datos en Docker:

```bash
docker compose up -d db
```

Backend (JDK 21). Corre en http://localhost:9006 y usa por defecto `localhost:5432`, así que indícale el puerto 5433:

```bash
cd sistema_sath
DB_URL=jdbc:postgresql://localhost:5433/servicedesk ./mvnw spring-boot:run
```

Frontend con recarga en caliente. Corre en http://localhost:5173 y redirige `/api` al backend:

```bash
cd sistema_sath/Sath-frontend
npm install
npm run dev
```

---

## 📁 Estructura

```
.
├── docker-compose.yml          ← levanta app + base de datos
├── .env.example                ← variables configurables
└── sistema_sath/
    ├── Dockerfile              ← build: frontend (Node) → backend (Maven) → runtime (JDK)
    ├── pom.xml
    ├── src/main/java/...       ← backend Spring Boot
    │   ├── Config/             ← seguridad JWT, CORS, WebSocket, DataSeeder (datos de prueba)
    │   ├── Controller/         ← endpoints REST (/api/...)
    │   ├── domain/             ← entidades, repositorios y servicios por módulo
    │   └── tools/              ← reportes Jasper, colas de solicitudes, archivos
    ├── src/main/resources/
    │   ├── application.properties   ← configuración (lee variables de entorno)
    │   ├── reports/                 ← plantillas de reportes .jrxml
    │   └── imagesForReports/        ← logos usados en los PDF
    └── Sath-frontend/          ← frontend Vue 3 + Vite
```

> En el código, el modelo de datos conserva los nombres técnicos originales: `Activo` es lo que la interfaz llama **Recurso**, y `Administrativo` es el **personal**.

---

## ❓ Problemas comunes

| Problema | Solución |
|---|---|
| `port is already allocated` | El puerto 9006 o 5433 está en uso. Cambia `APP_PORT` o `DB_PORT` en `.env` |
| La página no carga justo después de levantar | La app sigue arrancando. Revisa `docker compose logs -f app` |
| `read-only file system` / `no space left on device` | El disco está lleno. Libera espacio y ejecuta `docker system prune` |
| Quiero los datos de prueba desde cero | `docker compose down -v` y luego `docker compose up -d` |
