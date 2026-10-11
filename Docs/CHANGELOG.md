# Bitácora de cambios

> Registro acumulativo de cambios del proyecto. Nueva entrada arriba: `## FECHA` seguida de una o más `### TIPO · Título`.

## 2026-10-11

### ♻️ Refactor · Proyecto genérico "ServiceDesk" + entorno Docker

- **Descripción:** el sistema se volvió genérico (sin referencias a una institución en particular) y se preparó para levantarse con Docker con una base de datos propia y datos de prueba.
- **Cambios clave:**
  - Marca **ServiceDesk**, logos genéricos (web y reportes PDF). En la interfaz y los reportes, "Activo/Equipo" pasa a llamarse **Recurso** y el área responsable es **Soporte Técnico**.
  - Se eliminaron las integraciones con sistemas externos (API de fichas de activos, sincronización de solicitudes externas), la BD secundaria y los reportes legacy.
  - Se eliminaron los dumps con datos reales. `application.properties` ya no contiene IPs ni credenciales: todo se lee de variables de entorno.
  - `DataSeeder` carga datos base y, con `app.demo-data=true`, usuarios por rol, catálogos y solicitudes en todos los estados.
  - `Dockerfile` multi-etapa (Vue → Maven → JDK con ffmpeg y fuentes) y `docker-compose.yml` (app + PostgreSQL 16).
  - Endpoint de configuración renombrado: `/api/configuracion/dtic` → `/api/configuracion/soporte`. Campo `mantIdLondra` → `idExterno`.
