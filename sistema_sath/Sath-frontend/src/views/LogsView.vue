<script setup>
import {ref, computed, onMounted, onUnmounted} from 'vue'
import {useApi} from '@/composables/useApi'
import {useToast} from '@/composables/useToast'
import {useDownload} from '@/composables/useDownload'
import SearchSelect from '@/components/ui/SearchSelect.vue'

const {get} = useApi()
const {toast} = useToast()
const {descargarArchivo} = useDownload()

const logs = ref([])
const stats = ref({total: 0, errores: 0, warnings: 0, hoy: 0})
const cargando = ref(false)
const filNivel = ref('')
const filUsuario = ref('')
const autoRefresh = ref(false)
const drawerOpen = ref(false)
const drawerLog = ref(null)
const exportMenu = ref(false)
const fechasDisponible = ref([])
const fechaDescarga = ref('')

let timer = null

onMounted(async () => {
  await Promise.all([cargar(), cargarStats(), cargarFechas()])
  document.addEventListener('click', cerrarExportMenu)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  document.removeEventListener('click', cerrarExportMenu)
})

async function cargar() {
  cargando.value = true
  try {
    const params = new URLSearchParams({limite: 500})
    if (filNivel.value) params.set('nivel', filNivel.value)
    if (filUsuario.value) params.set('usuario', filUsuario.value)
    const res = await get(`/api/logs/listar?${params}`)
    if (!res) return
    if (res.status === 403) {
      toast('Sin permiso para ver los logs', 'warning')
      logs.value = []
      return
    }
    if (!res.ok) throw new Error('Error al cargar logs')
    logs.value = await res.json()
  } catch (e) {
    logs.value = []
    toast(e.message, 'error')
  } finally {
    cargando.value = false
  }
}

async function cargarStats() {
  try {
    const res = await get('/api/logs/stats')
    if (!res || !res.ok) return
    stats.value = await res.json()
  } catch {
  }
}

async function cargarFechas() {
  try {
    const res = await get('/api/logs/fechas')
    if (!res || !res.ok) return
    fechasDisponible.value = await res.json()
    if (!fechaDescarga.value) fechaDescarga.value = fechasDisponible.value[0] ?? ''
  } catch {
  }
}

async function descargarLogDia() {
  const params = new URLSearchParams()
  if (fechaDescarga.value) params.append('fecha', fechaDescarga.value)
  const res = await get(`/api/logs/descargar?${params}`)
  if (!res) return
  if (!res.ok) {
    toast("No se encontró el log de esa fecha", 'error');
    return
  }
  const cd = res.headers.get('Content-Disposition') || ''
  const nombre = cd.match(/filename="?([^"]+)!?/)?.[1] || "sath.log"
  const blob = await res.blob()
  const r = await descargarArchivo(blob, nombre)
  if (r?.mensaje) toast(r.mensaje, 'success')

}


async function recargar() {
  await Promise.all([cargar(), cargarStats()])
}


function toggleAutoRefresh() {
  autoRefresh.value = !autoRefresh.value
  if (autoRefresh.value) {
    timer = setInterval(recargar, 10_000)
  } else {
    clearInterval(timer)
    timer = null
  }
}

function abrirDrawer(log) {
  drawerLog.value = log
  drawerOpen.value = true
}

function cerrarDrawer() {
  drawerOpen.value = false
  drawerLog.value = null
}

// ── Exportar ──────────────────────────────────────────────────────

function cerrarExportMenu(e) {
  if (!e.target.closest('[data-export-menu]')) exportMenu.value = false
}

function _blob(contenido, nombre, tipo) {
  const blob = new Blob([contenido], {type: `${tipo};charset=utf-8;`})
  descargarArchivo(blob, nombre).then(r => {
    if (r?.mensaje) toast(r.mensaje, 'success')
  })
}

function exportarTxt() {
  exportMenu.value = false
  const ts = new Date().toISOString().slice(0, 10)
  const txt = logs.value
      .map(
          (l) =>
              `${l.fecha || '?'} [${l.nivel || '?'}] ${l.logger || '?'} [${l.usuario || 'sistema'}] - ${l.mensaje || ''}` +
              (l.stackTrace ? '\n' + l.stackTrace : ''),
      )
      .join('\n\n')
  _blob(txt, `logs_sath_${ts}.txt`, 'text/plain')
  toast('Exportado como TXT', 'success')
}

function exportarJson() {
  exportMenu.value = false
  const ts = new Date().toISOString().slice(0, 10)
  _blob(JSON.stringify(logs.value, null, 2), `logs_sath_${ts}.json`, 'application/json')
  toast('Exportado como JSON', 'success')
}

function exportarCsv() {
  exportMenu.value = false
  const ts = new Date().toISOString().slice(0, 10)
  const esc = (v) => `"${String(v ?? '').replace(/"/g, '""')}"`
  const cab = ['id', 'fecha', 'nivel', 'usuario', 'logger', 'mensaje', 'stackTrace'].join(',')
  const fil = logs.value.map((l) =>
      [l.id, l.fecha, l.nivel, l.usuario, l.logger, l.mensaje, l.stackTrace].map(esc).join(','),
  )
  _blob([cab, ...fil].join('\n'), `logs_sath_${ts}.csv`, 'text/csv')
  toast('Exportado como CSV', 'success')
}

// ── Distribución por nivel ────────────────────────────────────────
const distribucion = computed(() => {
  const total = logs.value.length
  if (!total) return []
  const c = {ERROR: 0, WARN: 0, INFO: 0, DEBUG: 0}
  for (const l of logs.value) {
    const n = l.nivel?.toUpperCase() ?? ''
    if (n === 'ERROR') c.ERROR++
    else if (n === 'WARN' || n === 'WARNING') c.WARN++
    else if (n === 'INFO') c.INFO++
    else c.DEBUG++
  }
  return [
    {nivel: 'ERROR', count: c.ERROR, pct: Math.round((c.ERROR / total) * 100), color: 'bg-danger', text: 'text-danger'},
    {nivel: 'WARN', count: c.WARN, pct: Math.round((c.WARN / total) * 100), color: 'bg-warning', text: 'text-warning'},
    {nivel: 'INFO', count: c.INFO, pct: Math.round((c.INFO / total) * 100), color: 'bg-info', text: 'text-info'},
    {
      nivel: 'DEBUG',
      count: c.DEBUG,
      pct: Math.round((c.DEBUG / total) * 100),
      color: 'bg-secondary',
      text: 'text-white-dark'
    },
  ]
})

// ── Helpers visuales ──────────────────────────────────────────────

function nivelBadge(nivel) {
  return (
      {
        ERROR: 'sath-badge sath-badge-danger',
        WARN: 'sath-badge sath-badge-warning',
        WARNING: 'sath-badge sath-badge-warning',
        INFO: 'sath-badge sath-badge-info',
        DEBUG: 'sath-badge sath-badge-secondary',
      }[nivel?.toUpperCase()] ?? 'sath-badge sath-badge-secondary'
  )
}

function loggerCorto(logger) {
  if (!logger) return '—'
  const p = logger.split('.')
  return p[p.length - 1] || logger
}

function lineaRaw(log) {
  return `${log.fecha || '?'} [${log.nivel || '?'}] ${log.logger || '?'} [${log.usuario || 'sistema'}] - ${log.mensaje || ''}`
}
</script>

<template>
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li>
      <RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink>
    </li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">
      Logs del Backend
    </li>
  </ul>

  <!-- ── Stats ── -->
  <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6">
    <div class="panel">
      <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-primary/10">
          <svg
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              class="text-primary"
          >
            <line x1="8" y1="6" x2="21" y2="6"/>
            <line x1="8" y1="12" x2="21" y2="12"/>
            <line x1="8" y1="18" x2="21" y2="18"/>
            <line x1="3" y1="6" x2="3.01" y2="6"/>
            <line x1="3" y1="12" x2="3.01" y2="12"/>
            <line x1="3" y1="18" x2="3.01" y2="18"/>
          </svg>
        </div>
        <div>
          <p class="text-white-dark text-xs">Total</p>
          <h5 class="text-xl font-bold dark:text-white-light/90">{{ stats.total }}</h5>
        </div>
      </div>
    </div>
    <div class="panel">
      <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-danger/10">
          <svg
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              class="text-danger"
          >
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
        </div>
        <div>
          <p class="text-white-dark text-xs">Errores hoy</p>
          <h5 class="text-xl font-bold text-danger">{{ stats.errores }}</h5>
        </div>
      </div>
    </div>
    <div class="panel">
      <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-warning/10">
          <svg
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              class="text-warning"
          >
            <path
                d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"
            />
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
        </div>
        <div>
          <p class="text-white-dark text-xs">Warnings hoy</p>
          <h5 class="text-xl font-bold text-warning">{{ stats.warnings }}</h5>
        </div>
      </div>
    </div>
    <div class="panel">
      <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-success/10">
          <svg
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              class="text-success"
          >
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
        </div>
        <div>
          <p class="text-white-dark text-xs">Registros hoy</p>
          <h5 class="text-xl font-bold text-success">{{ stats.hoy }}</h5>
        </div>
      </div>
    </div>
  </div>

  <!-- ── Distribución por nivel ── -->
  <div v-if="distribucion.length" class="panel mb-6">
    <div class="flex items-center justify-between mb-4">
      <h5 class="text-base font-bold dark:text-white-light/90">Distribución por nivel</h5>
      <span class="text-xs text-white-dark">{{ logs.length }} registros cargados</span>
    </div>
    <!-- Barra apilada -->
    <div class="flex h-3 w-full rounded-full overflow-hidden mb-5">
      <template v-for="d in distribucion" :key="d.nivel">
        <div v-if="d.pct > 0" :class="d.color" :style="{ width: d.pct + '%' }"
             class="transition-all duration-700 first:rounded-l-full last:rounded-r-full"
             :title="`${d.nivel}: ${d.count} (${d.pct}%)`"/>
      </template>
    </div>
    <!-- Filas de detalle -->
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
      <div v-for="d in distribucion" :key="d.nivel" class="flex flex-col gap-1.5">
        <div class="flex items-center justify-between text-xs">
          <div class="flex items-center gap-2">
            <span :class="[d.color, 'inline-block w-2.5 h-2.5 rounded-sm flex-shrink-0']"/>
            <span :class="d.text" class="font-semibold">{{ d.nivel }}</span>
          </div>
          <span class="text-white-dark font-mono">
            {{ d.count }} <span class="text-[10px] opacity-60">({{ d.pct }}%)</span>
          </span>
        </div>
        <div class="h-2 w-full rounded-full bg-[#ebedf2] dark:bg-[#1b2e4b] overflow-hidden">
          <div :class="d.color" class="h-full rounded-full transition-all duration-700"
               :style="{ width: d.pct + '%' }"/>
        </div>
      </div>
    </div>
  </div>

  <!-- ── Tabla de logs ── -->
  <div class="panel">
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
      <h5 class="text-xl font-bold dark:text-white-light/90">
        Logs del Backend
        <span class="ml-2 badge bg-primary/10 text-primary py-1 px-3 rounded-full text-sm">
          {{ logs.length }} registros
        </span>
      </h5>

      <div class="flex w-full flex-col gap-3 sm:w-auto sm:flex-row sm:items-center">
        <!-- Filtro nivel -->
        <SearchSelect
            v-model="filNivel"
            :options="[
            { value: 'ERROR', label: 'Error' },
            { value: 'WARN',  label: 'Warning' },
            { value: 'INFO',  label: 'Info' },
            { value: 'DEBUG', label: 'Debug' },
          ]"
            empty-label="Todos los niveles"
            empty-value=""
            placeholder="Todos los niveles"
            class="w-full sm:w-40"
            @change="cargar"
        />

        <!-- Filtro usuario -->
        <div class="relative">
          <input
              v-model="filUsuario"
              type="text"
              placeholder="Usuario..."
              class="peer form-input py-2 ltr:pr-11 rtl:pl-11 w-full sm:w-40"
              @keyup.enter="cargar"
          />
          <div
              class="absolute top-1/2 -translate-y-1/2 peer-focus:text-primary ltr:right-[11px] rtl:left-[11px]"
          >
            <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
            >
              <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5"/>
              <path d="M18.5 18.5L22 22" stroke-linecap="round"/>
            </svg>
          </div>
        </div>

        <SearchSelect
            v-model="fechaDescarga"
            :options=" fechasDisponible.map((f,i) => ({value: f, label: i === 0 ? `Hoy (${f})`: f}))"
            placeholder="Fecha"
            class="w-full sm:w-44"
        />
          <button
              type="button"
              class="btn btn-outline-info whitespace-nowrap"
              :disabled="!fechaDescarga"
              @click="descargarLogDia()"
          >
            Descargar dia
          </button>


        <!-- Separador visual -->
        <div class="hidden sm:block h-7 w-px bg-[#e0e6ed] dark:bg-[#253b5e]"/>

        <!-- Recargar -->
        <button
            type="button"
            class="btn btn-outline-primary"
            @click="recargar"
            :disabled="cargando"
        >
          Recargar
        </button>

        <!-- Auto refresh toggle -->
        <button
            type="button"
            class="btn whitespace-nowrap"
            :class="autoRefresh ? 'btn-success' : 'btn-outline-secondary'"
            @click="toggleAutoRefresh"
        >
          <svg
              class="inline w-4 h-4 mr-1.5 align-middle"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
          >
            <circle cx="12" cy="12" r="10" opacity="0.4"/>
            <circle
                cx="12"
                cy="12"
                r="4"
                :fill="autoRefresh ? 'currentColor' : 'none'"
                stroke="currentColor"
            />
          </svg>
          Auto {{ autoRefresh ? 'ON' : 'OFF' }}
        </button>

        <!-- Exportar dropdown -->
        <div class="relative" data-export-menu>
          <button
              type="button"
              class="btn btn-outline-success whitespace-nowrap"
              :disabled="logs.length === 0"
              @click.stop="exportMenu = !exportMenu"
          >
            <svg
                class="inline w-4 h-4 mr-1.5 align-middle"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
            >
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            Exportar
            <svg
                class="inline w-3 h-3 ml-1 align-middle transition-transform"
                :class="exportMenu ? 'rotate-180' : ''"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2.5"
            >
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </button>

          <Transition name="dropdown">
            <div
                v-if="exportMenu"
                class="absolute right-0 top-full mt-1 z-50 w-44 rounded-lg shadow-lg border border-[#e0e6ed] dark:border-[#1b2e4b] bg-white dark:bg-[#1b2e4b] overflow-hidden"
            >
              <button
                  type="button"
                  class="w-full flex items-center gap-3 px-4 py-2.5 text-sm hover:bg-gray-50 dark:hover:bg-[#253b5e] transition-colors"
                  @click="exportarTxt"
              >
                <svg
                    class="w-4 h-4 text-white-dark flex-shrink-0"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                >
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                  <polyline points="10 9 9 9 8 9"/>
                </svg>
                <span class="dark:text-white">Descargar TXT</span>
              </button>
              <div class="border-t border-[#e0e6ed] dark:border-[#253b5e]"/>
              <button
                  type="button"
                  class="w-full flex items-center gap-3 px-4 py-2.5 text-sm hover:bg-gray-50 dark:hover:bg-[#253b5e] transition-colors"
                  @click="exportarJson"
              >
                <svg
                    class="w-4 h-4 text-warning flex-shrink-0"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                >
                  <polyline points="16 18 22 12 16 6"/>
                  <polyline points="8 6 2 12 8 18"/>
                </svg>
                <span class="dark:text-white">Descargar JSON</span>
              </button>
              <div class="border-t border-[#e0e6ed] dark:border-[#253b5e]"/>
              <button
                  type="button"
                  class="w-full flex items-center gap-3 px-4 py-2.5 text-sm hover:bg-gray-50 dark:hover:bg-[#253b5e] transition-colors"
                  @click="exportarCsv"
              >
                <svg
                    class="w-4 h-4 text-success flex-shrink-0"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                >
                  <rect x="3" y="3" width="18" height="18" rx="2"/>
                  <line x1="3" y1="9" x2="21" y2="9"/>
                  <line x1="3" y1="15" x2="21" y2="15"/>
                  <line x1="9" y1="3" x2="9" y2="21"/>
                  <line x1="15" y1="3" x2="15" y2="21"/>
                </svg>
                <span class="dark:text-white">Descargar CSV</span>
              </button>
            </div>
          </Transition>
        </div>
      </div>
    </div>

    <div v-if="cargando" class="flex justify-center py-10">
      <span
          class="animate-spin border-4 border-primary border-l-transparent rounded-full w-10 h-10 inline-block"
      />
    </div>

    <div v-else class="table-responsive">
      <table class="table-striped table-hover text-xs">
        <thead>
        <tr>
          <th class="whitespace-nowrap hidden sm:table-cell">Fecha / Hora</th>
          <th class="text-center">Nivel</th>
          <th class="hidden sm:table-cell">Usuario</th>
          <th>Mensaje</th>
          <th class="hidden md:table-cell">Clase</th>
          <th class="text-center">Ver</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="logs.length === 0">
          <td colspan="6" class="text-center text-white-dark py-6">Sin logs que coincidan</td>
        </tr>
        <tr v-for="log in logs" :key="log.id" class="cursor-pointer" @click="abrirDrawer(log)">
          <td class="text-white-dark whitespace-nowrap font-mono hidden sm:table-cell">{{ log.fecha || '—' }}</td>
          <td class="text-center">
            <span :class="nivelBadge(log.nivel)">{{ log.nivel || '—' }}</span>
          </td>
          <td class="text-white-dark hidden sm:table-cell">{{ log.usuario || 'sistema' }}</td>
          <td>
            <div class="flex flex-col sm:block">
              <span class="text-[10px] text-white-dark font-mono sm:hidden">{{ log.fecha || '—' }}</span>
              <span class="block truncate max-w-[160px] sm:max-w-xs md:max-w-sm dark:text-white" :title="log.mensaje">
                  {{ log.mensaje || '—' }}
                </span>
            </div>
          </td>
          <td class="text-white-dark font-mono hidden md:table-cell">
            <span :title="log.logger">{{ loggerCorto(log.logger) }}</span>
          </td>
          <td class="text-center" @click.stop="abrirDrawer(log)">
            <button type="button" class="btn btn-xs btn-outline-primary px-2">
              <svg
                  class="inline w-3 h-3 align-middle"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
              >
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- ══════════════════════════════════════════════════════════════ -->
  <!-- Drawer: Detalle del Log                                       -->
  <!-- ══════════════════════════════════════════════════════════════ -->
  <Teleport to="body">
    <!-- Overlay -->
    <Transition name="fade">
      <div v-if="drawerOpen" class="fixed inset-0 z-[998] bg-black/50" @click="cerrarDrawer"/>
    </Transition>

    <!-- Panel lateral -->
    <Transition name="slide">
      <div
          v-if="drawerOpen"
          class="fixed inset-y-0 ltr:right-0 rtl:left-0 z-[999] flex flex-col bg-white dark:bg-[#0e1726] shadow-2xl"
          style="width: 100%; max-width: 560px"
      >
        <!-- Cabecera -->
        <div
            class="flex-shrink-0 flex items-center justify-between bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4 border-b border-[#e0e6ed] dark:border-[#1b2e4b]"
        >
          <div class="flex items-center gap-3">
            <div
                class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full"
                :class="{
                'bg-danger/10': drawerLog?.nivel === 'ERROR',
                'bg-warning/10': ['WARN', 'WARNING'].includes(drawerLog?.nivel),
                'bg-info/10': drawerLog?.nivel === 'INFO',
                'bg-primary/10': !['ERROR', 'WARN', 'WARNING', 'INFO'].includes(
                  drawerLog?.nivel ?? '',
                ),
              }"
            >
              <svg
                  width="15"
                  height="15"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  :class="{
                  'text-danger': drawerLog?.nivel === 'ERROR',
                  'text-warning': ['WARN', 'WARNING'].includes(drawerLog?.nivel),
                  'text-info': drawerLog?.nivel === 'INFO',
                  'text-primary': !['ERROR', 'WARN', 'WARNING', 'INFO'].includes(
                    drawerLog?.nivel ?? '',
                  ),
                }"
              >
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
            </div>
            <div>
              <h3 class="text-base font-semibold">Detalle del Log</h3>
              <p class="text-xs text-white-dark font-mono">{{ drawerLog?.fecha || '' }}</p>
            </div>
          </div>
          <button
              type="button"
              class="text-white-dark hover:text-dark transition-colors"
              @click="cerrarDrawer"
          >
            <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
            >
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>

        <!-- Cuerpo con scroll -->
        <div v-if="drawerLog" class="flex-1 overflow-y-auto">
          <!-- Bloque de campos básicos -->
          <div class="px-5 pt-5 pb-4 grid grid-cols-2 gap-x-6 gap-y-4">
            <div>
              <p class="mb-1 text-xs font-semibold uppercase text-white-dark">Nivel</p>
              <span :class="nivelBadge(drawerLog.nivel)">{{ drawerLog.nivel || '—' }}</span>
            </div>
            <div>
              <p class="mb-1 text-xs font-semibold uppercase text-white-dark">Usuario</p>
              <p class="font-semibold dark:text-white">{{ drawerLog.usuario || 'sistema' }}</p>
            </div>
            <div class="col-span-2">
              <p class="mb-1 text-xs font-semibold uppercase text-white-dark">Clase / Logger</p>
              <p class="font-mono text-xs text-white-dark break-all leading-relaxed">
                {{ drawerLog.logger || '—' }}
              </p>
            </div>
          </div>

          <div class="border-t border-[#e0e6ed] dark:border-[#1b2e4b]"/>

          <!-- Línea raw del archivo de log -->
          <div class="px-5 py-4">
            <p class="mb-2 text-xs font-semibold uppercase text-white-dark">Línea del Log</p>
            <pre
                class="p-3 rounded-lg text-xs font-mono leading-relaxed overflow-x-auto whitespace-pre-wrap"
                :class="{
                'bg-red-50   dark:bg-[#2a1520] text-danger': drawerLog.nivel === 'ERROR',
                'bg-amber-50 dark:bg-[#2a1e0a] text-warning': ['WARN', 'WARNING'].includes(
                  drawerLog.nivel,
                ),
                'bg-blue-50  dark:bg-[#0a1a2e] text-info': drawerLog.nivel === 'INFO',
                'bg-gray-100 dark:bg-[#1b2e4b] text-white-dark': ![
                  'ERROR',
                  'WARN',
                  'WARNING',
                  'INFO',
                ].includes(drawerLog.nivel ?? ''),
              }"
            >{{ lineaRaw(drawerLog) }}</pre
            >
          </div>

          <div class="border-t border-[#e0e6ed] dark:border-[#1b2e4b]"/>

          <!-- Mensaje completo -->
          <div class="px-5 py-4">
            <p class="mb-2 text-xs font-semibold uppercase text-white-dark">Mensaje completo</p>
            <pre
                class="p-3 bg-gray-100 dark:bg-[#1b2e4b] rounded-lg text-xs overflow-x-auto whitespace-pre-wrap dark:text-white leading-relaxed"
            >{{ drawerLog.mensaje || '—' }}</pre
            >
          </div>

          <!-- Stack Trace (solo si existe) -->
          <template v-if="drawerLog.stackTrace">
            <div class="border-t border-[#e0e6ed] dark:border-[#1b2e4b]"/>
            <div class="px-5 py-4">
              <div class="flex items-center gap-2 mb-2">
                <svg
                    width="13"
                    height="13"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    class="text-danger flex-shrink-0"
                >
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="8" x2="12" y2="12"/>
                  <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                <p class="text-xs font-semibold uppercase text-danger">Stack Trace</p>
              </div>
              <pre
                  class="p-3 bg-red-50 dark:bg-[#2a1520] rounded-lg text-xs overflow-x-auto whitespace-pre-wrap text-danger leading-relaxed"
              >{{ drawerLog.stackTrace }}</pre
              >
            </div>
          </template>
        </div>

        <!-- Footer con acciones -->
        <div
            class="flex-shrink-0 flex items-center justify-between gap-3 px-5 py-4 border-t border-[#e0e6ed] dark:border-[#1b2e4b] bg-[#fbfbfb] dark:bg-[#121c2c]"
        >
          <p class="text-xs text-white-dark">
            ID: <span class="font-mono">{{ drawerLog?.id }}</span>
          </p>
          <div class="flex gap-2">
            <button
                type="button"
                class="btn btn-xs btn-outline-secondary px-3"
                :title="'Copiar línea del log al portapapeles'"
                @click="
                navigator.clipboard
                  .writeText(lineaRaw(drawerLog))
                  .then(() => toast('Copiado', 'success'))
              "
            >
              <svg
                  class="inline w-3 h-3 mr-1 align-middle"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
              >
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
              </svg>
              Copiar
            </button>
            <button type="button" class="btn btn-outline-danger btn-xs px-3" @click="cerrarDrawer">
              Cerrar
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease,
  transform 0.15s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
