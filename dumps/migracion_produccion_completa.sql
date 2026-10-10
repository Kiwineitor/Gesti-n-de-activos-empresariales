-- ============================================================
--  MIGRACIÓN COMPLETA: gestion_hardware_uap → bd_hardware_v2
--  Versión: PRODUCCIÓN
--  Fecha   : 2026-06-16
-- ============================================================
--  Incluye:
--    1. Migración base (cargo, unidad, persona, activo, solicitud…)
--    2. Catálogo servicio + técnicos asignados
--    3. atencion_solicitud + atencion_servicio con tipo de servicio
--
--  Prerrequisito:
--    - Spring Boot corrió al menos una vez (solo para crear el schema JPA)
--    - La BD origen 172.16.21.12 debe ser accesible desde este servidor
--    - El DataSeeder NO necesita haber corrido — este script siembra roles,
--      permisos y prioridades directamente.
--
--  Ejecutar:
--    PGPASSWORD="12391664Lp*" psql -h localhost -U arcangel \
--      -d bd_hardware_v2 -f migracion_produccion_completa.sql
-- ============================================================

BEGIN;

-- ──────────────────────────────────────────────────────────────
-- 0. EXTENSIONES
-- ──────────────────────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS dblink;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ──────────────────────────────────────────────────────────────
-- 1. CONECTAR AL ORIGEN
-- ──────────────────────────────────────────────────────────────
DO $$ BEGIN
  BEGIN PERFORM dblink_disconnect('origen'); EXCEPTION WHEN OTHERS THEN NULL; END;
  PERFORM dblink_connect('origen',
    'host=172.16.21.12 user=postgres password=usic2024 dbname=gestion_hardware_uap');
  RAISE NOTICE '[OK] Conexión al origen establecida';
END $$;

-- ──────────────────────────────────────────────────────────────
-- 2. LIMPIAR TODAS LAS TABLAS (incluyendo datos base)
--    Este script es autosuficiente: siembra roles/permisos/prioridades
--    desde cero, sin depender del DataSeeder.
-- ──────────────────────────────────────────────────────────────
TRUNCATE TABLE
  falla_reportada,
  atencion_servicio,
  atencion_solicitud,
  informe_equipo_especifico,
  informe_equipo_general,
  detalle_solicitud,
  evidencia_foto,
  historial_estado,
  solicitud,
  servicio,
  activo,
  notificacion,
  usr_permiso,
  usr_rol,
  tecnico_asignado,
  usuario,
  administrativo,
  persona,
  unidad,
  cargo,
  falla,
  tipo_activo,
  tipo_servicio,
  rol_permiso,
  permiso,
  rol,
  prioridad
CASCADE;
RAISE NOTICE '[OK] Tablas limpiadas';

-- ──────────────────────────────────────────────────────────────
-- 3. DATOS BASE DEL SISTEMA (antes dependían del DataSeeder)
-- ──────────────────────────────────────────────────────────────

-- Prioridades
INSERT INTO prioridad (nivel, orden_pri, _estado_aud, _fecha_registro) VALUES
  ('ALTO',  1, 'ACTIVO', NOW()),
  ('MEDIO', 2, 'ACTIVO', NOW()),
  ('BAJO',  3, 'ACTIVO', NOW());

-- Roles
INSERT INTO rol (nombre_rol, descripcion_rol, _estado_aud, _fecha_registro) VALUES
  ('ADMINISTRADOR', 'Acceso total al sistema',                         'ACTIVO', NOW()),
  ('TECNICO',       'Gestión de atenciones y mantenimientos',          'ACTIVO', NOW()),
  ('SUPERVISOR',    'Supervisión de solicitudes y reportes',           'ACTIVO', NOW()),
  ('APOYO',         'Acceso limitado al sistema',                      'ACTIVO', NOW()),
  ('DEVELOPER',     'Desarrollador — acceso total sin restricciones',  'ACTIVO', NOW());

-- Permisos
INSERT INTO permiso (nombre_permiso, descripcion_permiso, _estado_aud, _fecha_registro) VALUES
  ('GESTIONAR_USUARIOS',    'Crear, editar y eliminar usuarios',           'ACTIVO', NOW()),
  ('GESTIONAR_ROLES',       'Asignar y administrar roles',                 'ACTIVO', NOW()),
  ('VER_SOLICITUDES',       'Visualizar solicitudes',                      'ACTIVO', NOW()),
  ('GESTIONAR_SOLICITUDES', 'Crear y modificar solicitudes',               'ACTIVO', NOW()),
  ('ATENDER_SOLICITUDES',   'Registrar atenciones y técnicos',             'ACTIVO', NOW()),
  ('VER_REPORTES',          'Acceder al módulo de reportes',               'ACTIVO', NOW()),
  ('GESTIONAR_ACTIVOS',     'ABM de activos y tipos de activo',            'ACTIVO', NOW()),
  ('GESTIONAR_FALLAS',      'ABM del catálogo de fallas',                  'ACTIVO', NOW()),
  ('MODULO_SOLICITUDES',    'Acceso al módulo Solicitudes',                'ACTIVO', NOW()),
  ('MODULO_ATENCIONES',     'Acceso al módulo Atenciones',                 'ACTIVO', NOW()),
  ('MODULO_HISTORIAL',      'Acceso al módulo Historial de Activo',        'ACTIVO', NOW()),
  ('MODULO_TECNICOS',       'Acceso al módulo Técnicos Asignados',         'ACTIVO', NOW()),
  ('MODULO_ADMINISTRATIVOS','Acceso al módulo Administrativos',            'ACTIVO', NOW()),
  ('MODULO_USUARIOS',       'Acceso al módulo Usuarios',                   'ACTIVO', NOW()),
  ('MODULO_TIPOS_SERVICIO', 'Acceso al módulo Tipos de Servicio',          'ACTIVO', NOW()),
  ('MODULO_SERVICIOS',      'Acceso al módulo Servicios',                  'ACTIVO', NOW()),
  ('MODULO_TIPOS_ACTIVO',   'Acceso al módulo Tipos de Activo',            'ACTIVO', NOW()),
  ('MODULO_FALLAS',         'Acceso al módulo Fallas',                     'ACTIVO', NOW()),
  ('MODULO_REPORTES',       'Acceso al módulo Reportes',                   'ACTIVO', NOW());

-- Rol → Permiso
INSERT INTO rol_permiso (id_rol, id_permiso, _estado_aud, _fecha_registro)
SELECT r.id_rol, p.id_permiso, 'ACTIVO', NOW()
FROM rol r, permiso p
WHERE r.nombre_rol IN ('ADMINISTRADOR', 'DEVELOPER');  -- acceso total

INSERT INTO rol_permiso (id_rol, id_permiso, _estado_aud, _fecha_registro)
SELECT r.id_rol, p.id_permiso, 'ACTIVO', NOW()
FROM rol r
JOIN permiso p ON p.nombre_permiso IN (
  'VER_SOLICITUDES','GESTIONAR_SOLICITUDES','ATENDER_SOLICITUDES',
  'VER_REPORTES','GESTIONAR_FALLAS',
  'MODULO_SOLICITUDES','MODULO_ATENCIONES','MODULO_HISTORIAL',
  'MODULO_TECNICOS','MODULO_REPORTES','MODULO_FALLAS'
)
WHERE r.nombre_rol = 'TECNICO';

INSERT INTO rol_permiso (id_rol, id_permiso, _estado_aud, _fecha_registro)
SELECT r.id_rol, p.id_permiso, 'ACTIVO', NOW()
FROM rol r
JOIN permiso p ON p.nombre_permiso IN (
  'VER_SOLICITUDES','VER_REPORTES','GESTIONAR_SOLICITUDES',
  'MODULO_SOLICITUDES','MODULO_REPORTES','MODULO_HISTORIAL'
)
WHERE r.nombre_rol = 'SUPERVISOR';

RAISE NOTICE '[OK] Datos base sembrados: roles=%, permisos=%, prioridades=%',
  (SELECT COUNT(*) FROM rol),
  (SELECT COUNT(*) FROM permiso),
  (SELECT COUNT(*) FROM prioridad);

-- ──────────────────────────────────────────────────────────────
-- 4. CARGO  (cargo → cargo)
-- ──────────────────────────────────────────────────────────────
INSERT INTO cargo (id_cargo, nombre_car, _estado_aud, _fecha_registro)
SELECT DISTINCT ON (SUBSTRING(cargo,1,100))
  id_cargo,
  SUBSTRING(cargo, 1, 100),
  'ACTIVO', NOW()
FROM dblink('origen', 'SELECT id_cargo, cargo FROM cargo')
AS t(id_cargo bigint, cargo varchar)
ORDER BY SUBSTRING(cargo,1,100), id_cargo;
RAISE NOTICE '[OK] cargo: % filas', (SELECT COUNT(*) FROM cargo);

-- ──────────────────────────────────────────────────────────────
-- 5. UNIDAD  (unidad → unidad)
-- ──────────────────────────────────────────────────────────────
INSERT INTO unidad (id_unidad, nombre_uni, _estado_aud, _fecha_registro)
SELECT DISTINCT ON (unidad)
  id_unidad, unidad, 'ACTIVO', NOW()
FROM dblink('origen', 'SELECT id_unidad, unidad FROM unidad')
AS t(id_unidad bigint, unidad varchar)
ORDER BY unidad, id_unidad;
RAISE NOTICE '[OK] unidad: % filas', (SELECT COUNT(*) FROM unidad);

-- ──────────────────────────────────────────────────────────────
-- 5. TIPO_ACTIVO  (tipoequipo → tipo_activo)
-- ──────────────────────────────────────────────────────────────
INSERT INTO tipo_activo (id_tipo_activo, nombre_tipo_act, _estado_aud, _fecha_registro)
SELECT id_tipoequipo, equipo, 'ACTIVO', NOW()
FROM dblink('origen', 'SELECT id_tipoequipo, equipo FROM tipoequipo')
AS t(id_tipoequipo bigint, equipo varchar);
RAISE NOTICE '[OK] tipo_activo: % filas', (SELECT COUNT(*) FROM tipo_activo);

-- ──────────────────────────────────────────────────────────────
-- 6. FALLA  (falla → falla)
-- ──────────────────────────────────────────────────────────────
WITH fallas AS (
  SELECT id_falla, falla
  FROM dblink('origen', 'SELECT id_falla, falla FROM falla')
  AS t(id_falla bigint, falla varchar)
),
fallaequipos AS (
  SELECT DISTINCT ON (id_falla) id_falla, id_tipoequipo
  FROM dblink('origen',
    'SELECT id_falla, id_tipoequipo FROM fallaequipo WHERE estado = ''A''')
  AS t(id_falla bigint, id_tipoequipo bigint)
  ORDER BY id_falla, id_tipoequipo
)
INSERT INTO falla (id_falla, nombre_fall, id_tipo_activo, _estado_aud, _fecha_registro)
SELECT f.id_falla, f.falla, fe.id_tipoequipo, 'ACTIVO', NOW()
FROM fallas f
LEFT JOIN fallaequipos fe USING (id_falla);
RAISE NOTICE '[OK] falla: % filas', (SELECT COUNT(*) FROM falla);

-- ──────────────────────────────────────────────────────────────
-- 7. TIPO_SERVICIO  (tiposervicio → tipo_servicio)
-- ──────────────────────────────────────────────────────────────
INSERT INTO tipo_servicio (id_tipo_servicio, nombre_tipo, descripcion_tipo,
                           _estado_aud, _fecha_registro)
SELECT id_tipo_servicio, nom_tipo_servicio, nom_tipo_servicio, 'ACTIVO', NOW()
FROM dblink('origen', 'SELECT id_tipo_servicio, nom_tipo_servicio FROM tiposervicio')
AS t(id_tipo_servicio bigint, nom_tipo_servicio varchar);
RAISE NOTICE '[OK] tipo_servicio: % filas', (SELECT COUNT(*) FROM tipo_servicio);

-- ──────────────────────────────────────────────────────────────
-- 8. PERSONA  (persona → persona)
-- ──────────────────────────────────────────────────────────────
INSERT INTO persona (id_persona, nombre_per, ap_paterno_per, ap_materno_per,
                     ci_per, celular_per, _estado_aud, _fecha_registro)
SELECT DISTINCT ON (ci)
  id_persona, nombre, ap_paterno, ap_materno, ci,
  NULLIF(celular::varchar, '0'),
  CASE WHEN estado = 'X' THEN 'INACTIVO' ELSE 'ACTIVO' END,
  NOW()
FROM dblink('origen',
  'SELECT id_persona, nombre, ap_paterno, ap_materno, ci, celular, estado
   FROM persona WHERE ci IS NOT NULL')
AS t(id_persona bigint, nombre varchar, ap_paterno varchar, ap_materno varchar,
     ci varchar, celular int, estado varchar)
ORDER BY ci, id_persona;

INSERT INTO persona (id_persona, nombre_per, ap_paterno_per, ap_materno_per,
                     ci_per, celular_per, _estado_aud, _fecha_registro)
SELECT id_persona, nombre, ap_paterno, ap_materno, NULL,
       NULLIF(celular::varchar, '0'),
       CASE WHEN estado = 'X' THEN 'INACTIVO' ELSE 'ACTIVO' END,
       NOW()
FROM dblink('origen',
  'SELECT id_persona, nombre, ap_paterno, ap_materno, ci, celular, estado
   FROM persona WHERE ci IS NULL')
AS t(id_persona bigint, nombre varchar, ap_paterno varchar, ap_materno varchar,
     ci varchar, celular int, estado varchar)
ON CONFLICT (id_persona) DO NOTHING;
RAISE NOTICE '[OK] persona: % filas', (SELECT COUNT(*) FROM persona);

-- ──────────────────────────────────────────────────────────────
-- 9. ADMINISTRATIVO  (persona → administrativo)
-- ──────────────────────────────────────────────────────────────
INSERT INTO administrativo (id_administrativo, id_persona, id_unidad, id_cargo,
                            codigo_admi, _estado_aud, _fecha_registro)
SELECT
  p.id_persona, p.id_persona, p.id_unidad,
  CASE WHEN p.id_cargo IN (SELECT id_cargo FROM cargo) THEN p.id_cargo ELSE NULL END,
  0,
  CASE WHEN p.estado = 'X' THEN 'INACTIVO' ELSE 'ACTIVO' END,
  NOW()
FROM dblink('origen',
  'SELECT id_persona, id_unidad, id_cargo, estado FROM persona WHERE id_unidad IS NOT NULL')
AS p(id_persona bigint, id_unidad bigint, id_cargo bigint, estado varchar)
WHERE p.id_persona IN (SELECT id_persona  FROM persona)
  AND p.id_unidad  IN (SELECT id_unidad   FROM unidad);
RAISE NOTICE '[OK] administrativo: % filas', (SELECT COUNT(*) FROM administrativo);

-- ──────────────────────────────────────────────────────────────
-- 10. USUARIO  (usuario → usuario)
-- ──────────────────────────────────────────────────────────────
INSERT INTO usuario (id_usuario, username, password, id_persona,
                     _estado_aud, _fecha_registro)
SELECT
  id_usuario, usuario,
  crypt(contrasena, gen_salt('bf', 10)),
  id_persona,
  CASE WHEN estado = 'A' THEN 'ACTIVO' ELSE 'INACTIVO' END,
  NOW()
FROM dblink('origen',
  'SELECT id_usuario, usuario, contrasena, id_persona, estado FROM usuario')
AS t(id_usuario bigint, usuario varchar, contrasena varchar,
     id_persona bigint, estado char)
WHERE id_persona IN (SELECT id_persona FROM persona);
RAISE NOTICE '[OK] usuario: % filas', (SELECT COUNT(*) FROM usuario);

-- ──────────────────────────────────────────────────────────────
-- 11. USR_ROL  — asignar rol TECNICO a todos los usuarios migrados
-- ──────────────────────────────────────────────────────────────
INSERT INTO usr_rol (id_usr_rol, id_usuario, id_rol, _estado_aud, _fecha_registro)
SELECT
  ROW_NUMBER() OVER (ORDER BY u.id_usuario),
  u.id_usuario, r.id_rol,
  'ACTIVO', NOW()
FROM usuario u
CROSS JOIN (SELECT id_rol FROM rol WHERE nombre_rol = 'TECNICO' LIMIT 1) r;
RAISE NOTICE '[OK] usr_rol: % filas', (SELECT COUNT(*) FROM usr_rol);

-- ──────────────────────────────────────────────────────────────
-- 12. USUARIO ADMINISTRADOR (appu)
-- ──────────────────────────────────────────────────────────────
DO $$
DECLARE
  nxt_per BIGINT; nxt_adm BIGINT; nxt_usr BIGINT; nxt_rol BIGINT;
  rid_adm BIGINT; rid_dev BIGINT;
  cid BIGINT; uid BIGINT;
BEGIN
  SELECT COALESCE(MAX(id_persona),        0) + 1 INTO nxt_per FROM persona;
  SELECT COALESCE(MAX(id_administrativo), 0) + 1 INTO nxt_adm FROM administrativo;
  SELECT COALESCE(MAX(id_usuario),        0) + 1 INTO nxt_usr FROM usuario;
  SELECT COALESCE(MAX(id_usr_rol),        0) + 1 INTO nxt_rol FROM usr_rol;
  SELECT id_rol INTO rid_adm FROM rol WHERE nombre_rol = 'ADMINISTRADOR' LIMIT 1;
  SELECT id_rol INTO rid_dev FROM rol WHERE nombre_rol = 'DEVELOPER'     LIMIT 1;
  -- Buscar cargo y unidad por nombre exacto; si no existen, tomar el de menor ID
  SELECT COALESCE(
    (SELECT id_cargo FROM cargo WHERE UPPER(nombre_car) = 'DESARROLLADOR DE SISTEMAS' LIMIT 1),
    (SELECT id_cargo FROM cargo ORDER BY id_cargo LIMIT 1)
  ) INTO cid;
  SELECT COALESCE(
    (SELECT id_unidad FROM unidad WHERE UPPER(nombre_uni) = 'USIC' LIMIT 1),
    (SELECT id_unidad FROM unidad ORDER BY id_unidad LIMIT 1)
  ) INTO uid;

  INSERT INTO persona (id_persona, nombre_per, ap_paterno_per, ap_materno_per,
                       ci_per, correo_per, _estado_aud, _fecha_registro)
  VALUES (nxt_per, 'CRISTOPHER', 'COAQUIRA', 'GUAYHUA',
          '12391664', 'cristopherangel018@gmail.com', 'ACTIVO', NOW());

  INSERT INTO administrativo (id_administrativo, id_persona, id_unidad, id_cargo,
                               codigo_admi, _estado_aud, _fecha_registro)
  VALUES (nxt_adm, nxt_per, uid, cid, 2449, 'ACTIVO', NOW());

  INSERT INTO usuario (id_usuario, username, password, id_persona,
                       _estado_aud, _fecha_registro)
  VALUES (nxt_usr, 'appu',
          crypt('12391664', gen_salt('bf', 10)),
          nxt_per, 'ACTIVO', NOW());

  INSERT INTO usr_rol (id_usr_rol, id_usuario, id_rol, _estado_aud, _fecha_registro)
  VALUES (nxt_rol,     nxt_usr, rid_adm, 'ACTIVO', NOW()),
         (nxt_rol + 1, nxt_usr, rid_dev, 'ACTIVO', NOW());

  RAISE NOTICE '[OK] usuario appu creado: persona=%, admin=%, usuario=%',
               nxt_per, nxt_adm, nxt_usr;
END $$;

-- ──────────────────────────────────────────────────────────────
-- 13. ACTIVO  (almacen → activo)
-- ──────────────────────────────────────────────────────────────
INSERT INTO activo (id_activo, codigo_act, descripcion_act, id_tipo_activo,
                    _estado_aud, _fecha_registro)
SELECT
  id_almacen,
  COALESCE(NULLIF(TRIM(cod_equipo), ''), 'SIN-CODIGO'),
  TRIM(CONCAT_WS(' ', descripcion, marca, modelo, serie)),
  id_tipoequipo,
  CASE WHEN estado = 'X' THEN 'INACTIVO' ELSE 'ACTIVO' END,
  NOW()
FROM dblink('origen',
  'SELECT id_almacen, cod_equipo, descripcion, marca, modelo, serie,
          id_tipoequipo, estado FROM almacen')
AS t(id_almacen bigint, cod_equipo varchar, descripcion varchar, marca varchar,
     modelo varchar, serie varchar, id_tipoequipo bigint, estado char)
WHERE id_tipoequipo IN (SELECT id_tipo_activo FROM tipo_activo);
RAISE NOTICE '[OK] activo: % filas', (SELECT COUNT(*) FROM activo);

-- ──────────────────────────────────────────────────────────────
-- 14. SOLICITUD  (servicio → solicitud)
--     nro_sol = id_servicio del legacy (mapeo 1:1)
-- ──────────────────────────────────────────────────────────────
INSERT INTO solicitud (
  id_solicitud, nro_sol, gestion_sol, fecha_sol,
  descripcion_sol, estado_sol, id_administrativo,
  _estado_aud, _fecha_registro
)
SELECT
  id_servicio, id_servicio,
  EXTRACT(YEAR FROM COALESCE(fecha_recepcion, NOW()))::int,
  fecha_recepcion::date,
  SUBSTRING(COALESCE(descripcion, observacion, 'Sin descripcion'), 1, 250),
  CASE
    WHEN TRIM(COALESCE(estado_servicio, '')) IN ('T','TT','A') THEN 'FINALIZADO'
    WHEN TRIM(COALESCE(estado_servicio, '')) = 'AP'            THEN 'EN_MANTENIMIENTO'
    ELSE 'PENDIENTE'
  END,
  id_persona,
  'ACTIVO',
  COALESCE(fecha_recepcion, NOW())
FROM dblink('origen',
  'SELECT id_servicio, fecha_recepcion, descripcion, observacion,
          estado, estado_servicio, id_persona
   FROM servicio')
AS t(id_servicio bigint, fecha_recepcion timestamp, descripcion varchar,
     observacion text, estado varchar(5), estado_servicio varchar, id_persona bigint)
WHERE id_persona IN (SELECT id_administrativo FROM administrativo);
RAISE NOTICE '[OK] solicitud: % filas', (SELECT COUNT(*) FROM solicitud);

-- ──────────────────────────────────────────────────────────────
-- 15. DETALLE_SOLICITUD  (servicio.id_almacen → detalle_solicitud)
--     id_detalle_solicitud = id_servicio (mapeo 1:1)
-- ──────────────────────────────────────────────────────────────
INSERT INTO detalle_solicitud (
  id_detalle_solicitud, id_solicitud, id_activo,
  tipo_informe, _estado_aud, _fecha_registro
)
SELECT
  id_servicio, id_servicio, id_almacen,
  'GENERAL', 'ACTIVO', NOW()
FROM dblink('origen',
  'SELECT id_servicio, id_almacen FROM servicio WHERE id_almacen IS NOT NULL')
AS t(id_servicio bigint, id_almacen bigint)
WHERE id_servicio IN (SELECT id_solicitud FROM solicitud)
  AND id_almacen  IN (SELECT id_activo    FROM activo);
RAISE NOTICE '[OK] detalle_solicitud: % filas', (SELECT COUNT(*) FROM detalle_solicitud);

-- ──────────────────────────────────────────────────────────────
-- 16. FALLA_REPORTADA  (detallefalla → falla_reportada)
-- ──────────────────────────────────────────────────────────────
INSERT INTO falla_reportada (
  id_falla_reportada, id_falla, id_detalle_solicitud,
  _estado_aud, _fecha_registro
)
SELECT id_detallefalla, id_falla, id_servicio, 'ACTIVO', NOW()
FROM dblink('origen',
  'SELECT id_detallefalla, id_falla, id_servicio FROM detallefalla')
AS t(id_detallefalla bigint, id_falla bigint, id_servicio bigint)
WHERE id_falla    IN (SELECT id_falla            FROM falla)
  AND id_servicio IN (SELECT id_detalle_solicitud FROM detalle_solicitud);
RAISE NOTICE '[OK] falla_reportada: % filas', (SELECT COUNT(*) FROM falla_reportada);

-- ──────────────────────────────────────────────────────────────
-- 17. SERVICIO  (solucion → servicio)
--     Catálogo de servicios técnicos.
--     id_tipo_servicio se asigna en el paso 19.
-- ──────────────────────────────────────────────────────────────
INSERT INTO servicio (id_servicio, nombre_serv, observaciones_serv,
                      id_tipo_servicio, _estado_aud, _fecha_registro)
SELECT
  id_solucion,
  SUBSTRING(solucion, 1, 100),
  NULL, NULL, 'ACTIVO', NOW()
FROM dblink('origen', 'SELECT id_solucion, solucion FROM solucion')
AS t(id_solucion bigint, solucion varchar)
ON CONFLICT (id_servicio) DO NOTHING;
RAISE NOTICE '[OK] servicio (catálogo): % filas', (SELECT COUNT(*) FROM servicio);

-- ──────────────────────────────────────────────────────────────
-- 18. TECNICO_ASIGNADO  (colaborador → tecnico_asignado)
-- ──────────────────────────────────────────────────────────────
INSERT INTO tecnico_asignado (
  id_tecnico_asignado, id_administrativo, id_solicitud,
  fecha_asignacion_tec_asig, _estado_aud, _fecha_registro
)
SELECT
  c.id_colaborador, c.id_persona, c.id_servicio,
  COALESCE(c.fecha_recepcion::date, CURRENT_DATE),
  'ACTIVO',
  COALESCE(c.fecha_recepcion, NOW())
FROM dblink('origen',
  'SELECT id_colaborador, id_persona, id_servicio, fecha_recepcion
   FROM colaborador WHERE estado = ''A'''
) AS c(id_colaborador bigint, id_persona bigint, id_servicio bigint, fecha_recepcion timestamp)
WHERE c.id_persona  IN (SELECT id_administrativo FROM administrativo)
  AND c.id_servicio IN (SELECT id_solicitud       FROM solicitud)
ON CONFLICT (id_tecnico_asignado) DO NOTHING;
RAISE NOTICE '[OK] tecnico_asignado: % filas', (SELECT COUNT(*) FROM tecnico_asignado);

-- ──────────────────────────────────────────────────────────────
-- 19. ASIGNAR TIPO_SERVICIO AL CATÁLOGO DE SERVICIOS
--     Usa el tipo más frecuente por solucion del historial legacy.
-- ──────────────────────────────────────────────────────────────
WITH tipo_mas_comun AS (
  SELECT DISTINCT ON (id_solucion) id_solucion, id_tipo_servicio
  FROM dblink('origen',
    'SELECT ds.id_solucion, s.id_tipo_servicio, COUNT(*) AS cnt
     FROM detalle_solucion ds
     JOIN servicio s ON ds.id_servicio = s.id_servicio
     WHERE s.id_tipo_servicio IS NOT NULL
     GROUP BY ds.id_solucion, s.id_tipo_servicio
     ORDER BY ds.id_solucion, cnt DESC'
  ) AS t(id_solucion bigint, id_tipo_servicio bigint, cnt bigint)
)
UPDATE servicio s
SET    id_tipo_servicio = t.id_tipo_servicio
FROM   tipo_mas_comun t
WHERE  s.id_servicio = t.id_solucion
  AND  s.id_tipo_servicio IS NULL;

DO $$
DECLARE v INTEGER;
BEGIN
  SELECT COUNT(*) INTO v FROM servicio WHERE id_tipo_servicio IS NULL;
  RAISE NOTICE '[OK] tipo_servicio asignado — servicios sin tipo restantes: %', v;
END $$;

-- ──────────────────────────────────────────────────────────────
-- 20. ATENCION_SOLICITUD
--     Un registro por detalle_solicitud, vinculado al servicio legacy
--     a través de nro_sol = id_servicio.
-- ──────────────────────────────────────────────────────────────
INSERT INTO atencion_solicitud (
  _estado_aud, _fecha_registro,
  estado_reparacion_aten_sol,
  fecha_ini_reparacion_aten_sol,
  fecha_fin_reparacion_aten_sol,
  id_detalle_solicitud
)
SELECT
  'ACTIVO', NOW(),
  CASE
    WHEN sol.estado_sol = 'FINALIZADO'       THEN 'FINALIZADO'
    WHEN sol.estado_sol = 'EN_MANTENIMIENTO' THEN 'TALLER'
    ELSE                                           'RECIBIDO'
  END,
  srv.fecha_recepcion::date,
  srv.fecha_entrega::date,
  ds.id_detalle_solicitud
FROM detalle_solicitud ds
JOIN solicitud sol ON ds.id_solicitud = sol.id_solicitud
JOIN dblink('origen',
  'SELECT id_servicio, fecha_recepcion, fecha_entrega FROM servicio'
) AS srv(id_servicio bigint, fecha_recepcion timestamp, fecha_entrega timestamp)
  ON sol.nro_sol = srv.id_servicio
WHERE NOT EXISTS (
  SELECT 1 FROM atencion_solicitud a WHERE a.id_detalle_solicitud = ds.id_detalle_solicitud
);
RAISE NOTICE '[OK] atencion_solicitud: % filas', (SELECT COUNT(*) FROM atencion_solicitud);

-- ──────────────────────────────────────────────────────────────
-- 21. ATENCION_SERVICIO — desde detalle_solucion del legacy
--     detalle_solucion.id_solucion = servicio.id_servicio (catálogo)
-- ──────────────────────────────────────────────────────────────
INSERT INTO atencion_servicio (
  _estado_aud, _fecha_registro,
  fecha_asignacion,
  id_atencion_solicitud,
  id_servicio
)
SELECT DISTINCT
  'ACTIVO', NOW(),
  srv.fecha_recepcion::date,
  aten.id_atencion_solicitud,
  ds_ant.id_solucion
FROM atencion_solicitud aten
JOIN detalle_solicitud ds  ON aten.id_detalle_solicitud = ds.id_detalle_solicitud
JOIN solicitud         sol ON ds.id_solicitud           = sol.id_solicitud
JOIN dblink('origen',
  'SELECT ds.id_servicio, ds.id_solucion FROM detalle_solucion ds'
) AS ds_ant(id_servicio bigint, id_solucion bigint)
  ON sol.nro_sol = ds_ant.id_servicio
JOIN dblink('origen',
  'SELECT id_servicio, fecha_recepcion FROM servicio'
) AS srv(id_servicio bigint, fecha_recepcion timestamp)
  ON sol.nro_sol = srv.id_servicio
WHERE ds_ant.id_solucion IN (SELECT id_servicio FROM servicio)
  AND NOT EXISTS (
    SELECT 1 FROM atencion_servicio av
    WHERE av.id_atencion_solicitud = aten.id_atencion_solicitud
      AND av.id_servicio           = ds_ant.id_solucion
  );

-- ──────────────────────────────────────────────────────────────
-- 22. ATENCION_SERVICIO — fallback para servicios sin tareas específicas
--     Servicios legacy con tipo asignado pero sin detalle_solucion.
--     Se usa un servicio genérico representativo por tipo:
--       tipo 1 (SOPORTE TÉCNICO)          → id 92  (Apoyo Tecnico)
--       tipo 2 (MANTENIMIENTO PREVENTIVO) → id 146 (REALIZAR MANTENIMIENTO PREVENTIVO)
--       tipo 3 (MANTENIMIENTO CORRECTIVO) → id 145 (REALIZAR MANTENIMIENTO CORRECTIVO)
-- ──────────────────────────────────────────────────────────────
INSERT INTO atencion_servicio (
  _estado_aud, _fecha_registro,
  fecha_asignacion,
  id_atencion_solicitud,
  id_servicio
)
SELECT DISTINCT
  'ACTIVO', NOW(),
  aten.fecha_ini_reparacion_aten_sol,
  aten.id_atencion_solicitud,
  CASE srv_ant.id_tipo_servicio
    WHEN 1 THEN 92
    WHEN 2 THEN 146
    WHEN 3 THEN 145
  END
FROM atencion_solicitud aten
JOIN detalle_solicitud ds ON aten.id_detalle_solicitud = ds.id_detalle_solicitud
JOIN solicitud         sol ON ds.id_solicitud          = sol.id_solicitud
JOIN dblink('origen',
  'SELECT id_servicio, id_tipo_servicio FROM servicio WHERE id_tipo_servicio IS NOT NULL'
) AS srv_ant(id_servicio bigint, id_tipo_servicio bigint)
  ON sol.nro_sol = srv_ant.id_servicio
WHERE NOT EXISTS (
  SELECT 1 FROM atencion_servicio av WHERE av.id_atencion_solicitud = aten.id_atencion_solicitud
)
  AND sol.estado_sol = 'FINALIZADO';

RAISE NOTICE '[OK] atencion_servicio: % filas', (SELECT COUNT(*) FROM atencion_servicio);

-- ──────────────────────────────────────────────────────────────
-- 23. RESET DE SECUENCIAS
-- ──────────────────────────────────────────────────────────────
DO $$
DECLARE
  r        RECORD;
  max_val  BIGINT;
  seq_name TEXT;
BEGIN
  FOR r IN
    SELECT table_name AS tbl, column_name AS col
    FROM   information_schema.columns
    WHERE  table_schema        = 'public'
      AND  identity_generation = 'BY DEFAULT'
  LOOP
    EXECUTE format('SELECT COALESCE(MAX(%I), 0) FROM %I', r.col, r.tbl) INTO max_val;
    seq_name := pg_get_serial_sequence(r.tbl, r.col);
    IF seq_name IS NOT NULL AND max_val > 0 THEN
      PERFORM setval(seq_name, max_val);
    END IF;
  END LOOP;
  RAISE NOTICE '[OK] Secuencias reseteadas';
END $$;

-- ──────────────────────────────────────────────────────────────
-- 24. CERRAR CONEXIÓN AL ORIGEN
-- ──────────────────────────────────────────────────────────────
SELECT dblink_disconnect('origen');

COMMIT;

-- ──────────────────────────────────────────────────────────────
-- 25. RESUMEN FINAL
-- ──────────────────────────────────────────────────────────────
SELECT tabla, filas FROM (
  SELECT 'cargo'                AS tabla, COUNT(*) AS filas FROM cargo
  UNION ALL SELECT 'unidad',              COUNT(*) FROM unidad
  UNION ALL SELECT 'tipo_activo',         COUNT(*) FROM tipo_activo
  UNION ALL SELECT 'falla',               COUNT(*) FROM falla
  UNION ALL SELECT 'tipo_servicio',       COUNT(*) FROM tipo_servicio
  UNION ALL SELECT 'persona',             COUNT(*) FROM persona
  UNION ALL SELECT 'administrativo',      COUNT(*) FROM administrativo
  UNION ALL SELECT 'usuario',             COUNT(*) FROM usuario
  UNION ALL SELECT 'usr_rol',             COUNT(*) FROM usr_rol
  UNION ALL SELECT 'activo',              COUNT(*) FROM activo
  UNION ALL SELECT 'solicitud',           COUNT(*) FROM solicitud
  UNION ALL SELECT 'detalle_solicitud',   COUNT(*) FROM detalle_solicitud
  UNION ALL SELECT 'falla_reportada',     COUNT(*) FROM falla_reportada
  UNION ALL SELECT 'servicio',            COUNT(*) FROM servicio
  UNION ALL SELECT 'tecnico_asignado',    COUNT(*) FROM tecnico_asignado
  UNION ALL SELECT 'atencion_solicitud',  COUNT(*) FROM atencion_solicitud
  UNION ALL SELECT 'atencion_servicio',   COUNT(*) FROM atencion_servicio
) s ORDER BY tabla;

-- ──────────────────────────────────────────────────────────────
-- VERIFICACIÓN DEL REPORTE ESTADÍSTICO
-- ──────────────────────────────────────────────────────────────
SELECT
  COALESCE(ts.nombre_tipo, srv.nombre_serv, 'SIN TIPO') AS tipo_servicio,
  COUNT(DISTINCT ds.id_detalle_solicitud)                AS cantidad
FROM solicitud so
LEFT JOIN detalle_solicitud ds    ON so.id_solicitud = ds.id_solicitud AND ds._estado_aud <> 'ELIMINADO'
LEFT JOIN atencion_solicitud aten ON ds.id_detalle_solicitud = aten.id_detalle_solicitud AND aten._estado_aud <> 'ELIMINADO'
LEFT JOIN atencion_servicio aserv ON aten.id_atencion_solicitud = aserv.id_atencion_solicitud AND aserv._estado_aud <> 'ELIMINADO'
LEFT JOIN servicio srv            ON aserv.id_servicio = srv.id_servicio
LEFT JOIN tipo_servicio ts        ON srv.id_tipo_servicio = ts.id_tipo_servicio
WHERE so.estado_sol = 'FINALIZADO'
  AND so._estado_aud <> 'ELIMINADO'
GROUP BY COALESCE(ts.nombre_tipo, srv.nombre_serv, 'SIN TIPO')
ORDER BY cantidad DESC;