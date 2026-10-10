<script setup>
import { ref, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { useDownload } from '@/composables/useDownload'

const { get } = useApi()
const { toast } = useToast()
const { descargarArchivo } = useDownload()

const tabActivo        = ref('general')
const cargando         = ref(false)
const cargandoCatalogos = ref(false)
const error            = ref(null)
const errorCatalogos   = ref(null)
const verificando      = ref(false)
const conteo           = ref(null)
const rangoDisponible  = ref(null)

const fechaInicial   = ref('')
const fechaFinal     = ref('')
const idUsuario      = ref(null)
const idTipoServicio = ref(null)
const idTipoEquipo   = ref(null)
const idUnidad       = ref(null)

const usuarios      = ref([])
const tiposServicio = ref([])
const tiposEquipo   = ref([])
const unidades      = ref([])

onMounted(async () => {
  await Promise.all([cargarCatalogos(), cargarRangoDisponible()])
})

async function cargarCatalogos() {
  cargandoCatalogos.value = true
  errorCatalogos.value = null
  try {
    const [uRes, tsRes, teRes, unRes] = await Promise.all([
      get('/api/reportes-legacy/catalogo/usuarios'),
      get('/api/reportes-legacy/catalogo/tipos-servicio'),
      get('/api/reportes-legacy/catalogo/tipos-equipo'),
      get('/api/reportes-legacy/catalogo/unidades'),
    ])
    if (uRes?.ok)  usuarios.value      = await uRes.json()
    if (tsRes?.ok) tiposServicio.value = await tsRes.json()
    if (teRes?.ok) tiposEquipo.value   = await teRes.json()
    if (unRes?.ok) unidades.value      = await unRes.json()

    if (usuarios.value.length      && idUsuario.value      === null) idUsuario.value      = usuarios.value[0].id
    if (tiposServicio.value.length && idTipoServicio.value === null) idTipoServicio.value = tiposServicio.value[0].id
    if (tiposEquipo.value.length   && idTipoEquipo.value   === null) idTipoEquipo.value   = tiposEquipo.value[0].id
    if (unidades.value.length      && idUnidad.value       === null) idUnidad.value       = unidades.value[0].id
  } catch (e) {
    errorCatalogos.value = 'No se pudieron cargar los catálogos del sistema antiguo: ' + e.message
  } finally {
    cargandoCatalogos.value = false
  }
}

async function cargarRangoDisponible() {
  try {
    const res = await get('/api/reportes-legacy/rango-disponible')
    if (!res?.ok) return
    rangoDisponible.value = await res.json()
    if (rangoDisponible.value?.primera_fecha) {
      fechaInicial.value = rangoDisponible.value.primera_fecha
      fechaFinal.value   = rangoDisponible.value.ultima_fecha
    }
  } catch {}
}

async function verificarConteo() {
  if (!fechaInicial.value || !fechaFinal.value) {
    error.value = 'Seleccione fecha inicial y fecha final para verificar.'
    return
  }
  verificando.value = true
  conteo.value = null
  error.value = null
  try {
    const params = new URLSearchParams({ fechaInicial: fechaInicial.value, fechaFinal: fechaFinal.value })
    if (idTipoServicio.value) params.set('idTipoServicio', idTipoServicio.value)
    if (idTipoEquipo.value)   params.set('idTipoEquipo', idTipoEquipo.value)
    if (idUnidad.value)       params.set('idUnidad', idUnidad.value)
    const res = await get(`/api/reportes-legacy/preview-count?${params}`)
    if (res?.ok) conteo.value = await res.json()
  } catch {} finally { verificando.value = false }
}

function resetConteo() { conteo.value = null }

function validar() {
  if (!fechaInicial.value || !fechaFinal.value) {
    error.value = 'Debe seleccionar fecha inicial y fecha final.'
    return false
  }
  if (!idUsuario.value) {
    error.value = 'Debe seleccionar un usuario.'
    return false
  }
  error.value = null
  return true
}

function buildUrl(endpoint, extras = {}) {
  const base = { fechaInicial: fechaInicial.value, fechaFinal: fechaFinal.value, idUsuario: idUsuario.value }
  const merged = { ...base, ...extras }
  const params = new URLSearchParams(
    Object.fromEntries(Object.entries(merged).filter(([, v]) => v !== null && v !== undefined && v !== ''))
  )
  return `/api/reportes-legacy/${endpoint}?${params}`
}

async function descargar(endpoint, extras = {}, nombreArchivo = null) {
  if (!validar()) return
  cargando.value = true
  try {
    const res = await get(buildUrl(endpoint, extras))
    if (!res?.ok) {
      const err = await res?.json().catch(() => ({}))
      throw new Error(err?.message || err?.error || `Error ${res?.status}`)
    }
    const blob = await res.blob()
    const resultado = await descargarArchivo(blob, nombreArchivo ?? (endpoint + '.pdf'))
    if (resultado?.mensaje) toast(resultado.mensaje, 'success')
  } catch (e) {
    error.value = 'No se pudo generar el reporte: ' + e.message
    toast(e.message, 'error')
  } finally { cargando.value = false }
}

const descargarGeneral = () =>
  descargar('reporte-general', {}, 'Reporte_General_Legacy.pdf')

const descargarPersonalizado = () =>
  descargar('reporte-personalizado', { idTipoServicio: idTipoServicio.value, idTipoEquipo: idTipoEquipo.value, idUnidad: idUnidad.value }, 'Reporte_Personalizado_V1_Legacy.pdf')

const descargarPersonalizado2 = () =>
  descargar('reporte-personalizado-2', { idTipoServicio: idTipoServicio.value, idTipoEquipo: idTipoEquipo.value }, 'Reporte_Personalizado_V2_Legacy.pdf')

const descargarPersonalizado3 = () =>
  descargar('reporte-personalizado-3', { idTipoServicio: idTipoServicio.value }, 'Reporte_Personalizado_V3_Legacy.pdf')

const descargarPersonalizado4 = () =>
  descargar('reporte-personalizado-4', { idTipoEquipo: idTipoEquipo.value }, 'Reporte_Personalizado_V4_Legacy.pdf')

const descargarServiciosUsuario = () =>
  descargar('reporte-servicios-usuario', {}, 'Reporte_Servicios_Usuario_Legacy.pdf')
</script>

<template>
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Reportes Descontinuados</li>
  </ul>

  <!-- Advertencia -->
  <div class="mb-5 flex items-start gap-3 rounded-lg border border-info/40 bg-info/10 p-4">
    <svg class="mt-0.5 shrink-0 text-info" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
      <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
      <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
    </svg>
    <div>
      <p class="font-semibold text-info">Reportes del sistema anterior (SATH Legacy)</p>
      <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">
        Estos reportes consultan la base de datos del sistema antiguo (<strong>gestion_hardware_uap</strong>).
        Son de solo lectura y se mantienen como referencia histórica. No reflejan datos del sistema actual.
      </p>
    </div>
  </div>

  <!-- Cargando catálogos -->
  <div v-if="cargandoCatalogos" class="mb-5 flex items-center gap-3 rounded-lg border border-info/30 bg-info/5 p-3 text-sm text-info">
    <span class="animate-spin border-2 border-info border-l-transparent rounded-full w-4 h-4 inline-block"/>
    Cargando catálogos del sistema antiguo…
  </div>

  <!-- Error catálogos -->
  <div v-if="errorCatalogos" class="mb-5 rounded-lg border border-danger/40 bg-danger/10 p-3 text-sm text-danger">
    {{ errorCatalogos }}
    <button class="ml-3 underline font-medium" @click="cargarCatalogos">Reintentar</button>
  </div>

  <div class="panel">
    <div class="flex items-center justify-between mb-5">
      <h5 class="text-xl font-bold dark:text-white-light/90">Centro de Reportes — Sistema Descontinuado</h5>
    </div>

    <!-- Campos comunes -->
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-5 border border-[#e0e6ed] dark:border-[#1b2e4b] rounded-lg p-4 bg-gray-50 dark:bg-[#1b2e4b]/30">
      <div>
        <label class="block text-sm font-medium mb-1">Fecha inicial</label>
        <input v-model="fechaInicial" type="date" class="form-input w-full" @change="resetConteo" />
      </div>
      <div>
        <label class="block text-sm font-medium mb-1">Fecha final</label>
        <input v-model="fechaFinal" type="date" class="form-input w-full" @change="resetConteo" />
      </div>
      <div>
        <label class="block text-sm font-medium mb-1">Usuario</label>
        <div v-if="cargandoCatalogos" class="form-input w-full h-9 animate-pulse rounded bg-gray-200 dark:bg-gray-700" />
        <select v-else v-model="idUsuario" class="form-select w-full">
          <option v-for="u in usuarios" :key="u.id" :value="u.id">{{ u.nombre }}</option>
        </select>
      </div>
    </div>

    <!-- Rango disponible -->
    <div v-if="rangoDisponible" class="mb-4 flex flex-wrap items-center gap-3 rounded-lg border border-primary/30 bg-primary/5 p-3 text-sm">
      <svg class="shrink-0 text-primary" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
        <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
      </svg>
      <span class="text-gray-600 dark:text-gray-300">
        BD antigua contiene <strong class="text-primary">{{ rangoDisponible.total }}</strong> servicios terminados
      </span>
    </div>

    <!-- Verificar conteo -->
    <div class="mb-5 flex flex-wrap items-center gap-3">
      <button type="button" class="btn btn-outline-primary btn-sm gap-2"
        :disabled="verificando || cargandoCatalogos" @click="verificarConteo">
        <span v-if="verificando" class="animate-spin border-2 border-primary border-l-transparent rounded-full w-3.5 h-3.5 inline-block"/>
        <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        {{ verificando ? 'Verificando...' : 'Verificar disponibilidad' }}
      </button>
      <span v-if="conteo !== null && Number(conteo.total) > 0" class="flex items-center gap-1.5 text-sm font-medium text-success">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="20 6 9 17 4 12"/>
        </svg>
        {{ conteo.total }} registros encontrados — puede generar el reporte
      </span>
      <span v-if="conteo !== null && Number(conteo.total) === 0" class="flex items-center gap-1.5 text-sm font-medium text-danger">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
        </svg>
        Sin datos para estos filtros — ajuste las fechas o los filtros
      </span>
    </div>

    <!-- Error operación -->
    <div v-if="error" class="mb-4 rounded-lg border border-danger/40 bg-danger/10 p-3 text-sm text-danger">{{ error }}</div>

    <!-- Tabs -->
    <div class="mb-5 flex flex-wrap gap-2 border-b border-[#e0e6ed] dark:border-[#1b2e4b] pb-3">
      <button type="button" class="btn btn-sm" :class="tabActivo === 'general' ? 'btn-info' : 'btn-outline-info'"
        @click="tabActivo = 'general'; error = null">
        <svg class="inline-block mr-1" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="8" y1="13" x2="16" y2="13"/><line x1="8" y1="17" x2="16" y2="17"/>
        </svg>
        Reporte General
      </button>
      <button type="button" class="btn btn-sm" :class="tabActivo === 'personalizado' ? 'btn-info' : 'btn-outline-info'"
        @click="tabActivo = 'personalizado'; error = null">
        <svg class="inline-block mr-1" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="3"/>
          <path d="M19.07 4.93a10 10 0 010 14.14M4.93 4.93a10 10 0 000 14.14"/>
        </svg>
        Reportes Personalizados
      </button>
      <button type="button" class="btn btn-sm" :class="tabActivo === 'servicios' ? 'btn-info' : 'btn-outline-info'"
        @click="tabActivo = 'servicios'; error = null">
        <svg class="inline-block mr-1" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <rect x="2" y="7" width="20" height="14" rx="2"/>
          <path d="M16 7V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v2"/>
        </svg>
        Servicios por Usuario
      </button>
    </div>

    <!-- ─── Tab: Reporte General ─── -->
    <div v-if="tabActivo === 'general'">
      <p class="text-sm text-gray-500 dark:text-gray-400 mb-4">
        Reporte global de atenciones del sistema anterior, filtrado por usuario y rango de fechas.
      </p>
      <button type="button" class="btn btn-info gap-2" :disabled="cargando || cargandoCatalogos" @click="descargarGeneral">
        <span v-if="cargando" class="animate-spin border-2 border-white border-l-transparent rounded-full w-4 h-4 inline-block"/>
        <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/>
          <polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
        </svg>
        {{ cargando ? 'Generando...' : 'Descargar Reporte General' }}
      </button>
    </div>

    <!-- ─── Tab: Reportes Personalizados ─── -->
    <div v-if="tabActivo === 'personalizado'">
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-5 border border-[#e0e6ed] dark:border-[#1b2e4b] rounded-lg p-4">
        <div>
          <label class="block text-sm font-medium mb-1">
            Tipo Servicio <span class="text-gray-400 font-normal">(opcional)</span>
          </label>
          <div v-if="cargandoCatalogos" class="form-input w-full h-9 animate-pulse rounded bg-gray-200 dark:bg-gray-700"/>
          <select v-else v-model="idTipoServicio" class="form-select w-full" @change="resetConteo">
            <option v-for="t in tiposServicio" :key="t.id" :value="t.id">{{ t.nombre }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">
            Tipo Equipo <span class="text-gray-400 font-normal">(opcional)</span>
          </label>
          <div v-if="cargandoCatalogos" class="form-input w-full h-9 animate-pulse rounded bg-gray-200 dark:bg-gray-700"/>
          <select v-else v-model="idTipoEquipo" class="form-select w-full" @change="resetConteo">
            <option v-for="te in tiposEquipo" :key="te.id" :value="te.id">{{ te.nombre }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1">
            Unidad <span class="text-gray-400 font-normal">(opcional)</span>
          </label>
          <div v-if="cargandoCatalogos" class="form-input w-full h-9 animate-pulse rounded bg-gray-200 dark:bg-gray-700"/>
          <select v-else v-model="idUnidad" class="form-select w-full" @change="resetConteo">
            <option v-for="un in unidades" :key="un.id" :value="un.id">{{ un.nombre }}</option>
          </select>
        </div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="panel border border-info/20 bg-info/5">
          <div class="mb-3">
            <span class="badge bg-info/20 text-info text-xs">Variante 1</span>
            <p class="text-xs text-gray-500 dark:text-gray-400 mt-2">Filtros: tipo servicio + tipo equipo + unidad</p>
          </div>
          <button type="button" class="btn btn-outline-info btn-sm w-full" :disabled="cargando || cargandoCatalogos" @click="descargarPersonalizado">
            <svg class="mr-1 inline" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            Descargar V1
          </button>
        </div>
        <div class="panel border border-info/20 bg-info/5">
          <div class="mb-3">
            <span class="badge bg-info/20 text-info text-xs">Variante 2</span>
            <p class="text-xs text-gray-500 dark:text-gray-400 mt-2">Filtros: tipo servicio + tipo equipo</p>
          </div>
          <button type="button" class="btn btn-outline-info btn-sm w-full" :disabled="cargando || cargandoCatalogos" @click="descargarPersonalizado2">
            <svg class="mr-1 inline" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            Descargar V2
          </button>
        </div>
        <div class="panel border border-info/20 bg-info/5">
          <div class="mb-3">
            <span class="badge bg-info/20 text-info text-xs">Variante 3</span>
            <p class="text-xs text-gray-500 dark:text-gray-400 mt-2">Filtro: solo tipo servicio</p>
          </div>
          <button type="button" class="btn btn-outline-info btn-sm w-full" :disabled="cargando || cargandoCatalogos" @click="descargarPersonalizado3">
            <svg class="mr-1 inline" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            Descargar V3
          </button>
        </div>
        <div class="panel border border-info/20 bg-info/5">
          <div class="mb-3">
            <span class="badge bg-info/20 text-info text-xs">Variante 4</span>
            <p class="text-xs text-gray-500 dark:text-gray-400 mt-2">Filtro: solo tipo equipo</p>
          </div>
          <button type="button" class="btn btn-outline-info btn-sm w-full" :disabled="cargando || cargandoCatalogos" @click="descargarPersonalizado4">
            <svg class="mr-1 inline" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            Descargar V4
          </button>
        </div>
      </div>
    </div>

    <!-- ─── Tab: Servicios por Usuario ─── -->
    <div v-if="tabActivo === 'servicios'">
      <p class="text-sm text-gray-500 dark:text-gray-400 mb-4">
        Listado de servicios atendidos por el usuario seleccionado en el rango de fechas indicado.
      </p>
      <button type="button" class="btn btn-info gap-2" :disabled="cargando || cargandoCatalogos" @click="descargarServiciosUsuario">
        <span v-if="cargando" class="animate-spin border-2 border-white border-l-transparent rounded-full w-4 h-4 inline-block"/>
        <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/>
          <polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
        </svg>
        {{ cargando ? 'Generando...' : 'Descargar Reporte Servicios' }}
      </button>
    </div>
  </div>
</template>