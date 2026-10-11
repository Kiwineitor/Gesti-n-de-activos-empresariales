# ServiceDesk · Frontend

Interfaz web de ServiceDesk (Sistema de Gestión de Solicitudes de Servicio), construida con Vue 3 + Vite.

- `npm install` — instala las dependencias.
- `npm run dev` — servidor de desarrollo en http://localhost:5173; las rutas `/api` y `/ws` se redirigen al backend en http://localhost:9006 (configurable con `BACKEND_URL`).
- `npm run build` — genera la versión de producción en `../src/main/resources/static`, desde donde la sirve el backend.

Para levantar el sistema completo con Docker, consulta el README de la raíz del repositorio.
