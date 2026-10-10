<script setup>
import { ref, computed } from 'vue'
import { useVueLogsStore } from '@/stores/vueLogs'
import { useToast } from '@/composables/useToast'
import { useDownload } from '@/composables/useDownload'
import SearchSelect from '@/components/ui/SearchSelect.vue'

const store   = useVueLogsStore()
const { toast, confirm } = useToast()
const { descargarArchivo } = useDownload()

const filNivel    = ref('')
const filBusqueda = ref('')
const drawerOpen  = ref(false)
const drawerLog   = ref(null)
const exportMenu  = ref(false)

// ── Filtrado ──────────────────────────────────────────────────────
const logsFiltrados = computed(() => {
  const q = filBusqueda.value.toLowerCase().trim()
  return store.logs.filter(l => {
    const nivelOk = !filNivel.value || l.nivel === filNivel.value
    const textoOk = !q ||
      l.mensaje.toLowerCase().includes(q) ||
      l.componente.toLowerCase().includes(q) ||
      l.url.toLowerCase().includes(q)
    return nivelOk && textoOk
  })
})

// ── Drawer ────────────────────────────────────────────────────────
function abrirDrawer(log) { drawerLog.value = log; drawerOpen.value = true }
function cerrarDrawer()   { drawerOpen.value = false; drawerLog.value = null }

// ── Limpiar ───────────────────────────────────────────────────────
async function limpiarLogs() {
  const r = await confirm('¿Eliminar todos los logs de Vue del almacenamiento local?')
  if (!r.isConfirmed) return
  store.limpiar()
  toast('Logs eliminados', 'success')
}

// ── Exportar ──────────────────────────────────────────────────────
document.addEventListener('click', (e) => {
  if (!e.target.closest('[data-export-menu-vue]')) exportMenu.value = false
})

function _blob(contenido, nombre, tipo) {
  const blob = new Blob([contenido], { type: `${tipo};charset=utf-8;` })
  descargarArchivo(blob, nombre)
}

function exportarTxt() {
  exportMenu.value = false
  const ts  = new Date().toISOString().slice(0, 10)
  const txt = logsFiltrados.value.map(l =>
    `${l.fecha} [${l.nivel}] ${l.componente} — ${l.url}\n${l.mensaje}` +
    (l.info      ? `\nHook: ${l.info}` : '') +
    (l.stackTrace ? `\n${l.stackTrace}` : '')
  ).join('\n\n─────────────────────────────────────\n\n')
  _blob(txt, `vue_logs_${ts}.txt`, 'text/plain')
  toast('Exportado como TXT', 'success')
}

function exportarJson() {
  exportMenu.value = false
  const ts = new Date().toISOString().slice(0, 10)
  _blob(JSON.stringify(logsFiltrados.value, null, 2), `vue_logs_${ts}.json`, 'application/json')
  toast('Exportado como JSON', 'success')
}

function exportarCsv() {
  exportMenu.value = false
  const ts  = new Date().toISOString().slice(0, 10)
  const esc = (v) => `"${String(v ?? '').replace(/"/g, '""')}"`
  const cab = ['id','fecha','nivel','componente','url','mensaje','info','stackTrace'].join(',')
  const fil = logsFiltrados.value.map(l =>
    [l.id, l.fecha, l.nivel, l.componente, l.url, l.mensaje, l.info, l.stackTrace].map(esc).join(',')
  )
  _blob([cab, ...fil].join('\n'), `vue_logs_${ts}.csv`, 'text/csv')
  toast('Exportado como CSV', 'success')
}

// ── Distribución por nivel ────────────────────────────────────────
const distribucion = computed(() => {
  const total = store.logs.length
  if (!total) return []
  const c = { ERROR: 0, WARN: 0, INFO: 0 }
  for (const l of store.logs) {
    const n = l.nivel?.toUpperCase() ?? ''
    if (n === 'ERROR') c.ERROR++
    else if (n === 'WARN') c.WARN++
    else c.INFO++
  }
  return [
    { nivel: 'ERROR', count: c.ERROR, pct: Math.round((c.ERROR / total) * 100), color: 'bg-danger',  text: 'text-danger'  },
    { nivel: 'WARN',  count: c.WARN,  pct: Math.round((c.WARN  / total) * 100), color: 'bg-warning', text: 'text-warning' },
    { nivel: 'INFO',  count: c.INFO,  pct: Math.round((c.INFO  / total) * 100), color: 'bg-info',    text: 'text-info'    },
  ]
})

// ── Helpers visuales ──────────────────────────────────────────────
function nivelBadge(nivel) {
  return ({
    ERROR: 'sath-badge sath-badge-danger',
    WARN:  'sath-badge sath-badge-warning',
    INFO:  'sath-badge sath-badge-info',
  }[nivel] ?? 'sath-badge sath-badge-secondary')
}
</script>

<template>
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Logs del Frontend</li>
  </ul>

  <!-- ── Stats ── -->
  <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6">
    <div class="panel">
      <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-primary/10">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-primary">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" fill="currentColor" opacity="0.2"/>
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
          </svg>
        </div>
        <div>
          <p class="text-white-dark text-xs">Total</p>
          <h5 class="text-xl font-bold dark:text-white-light/90">{{ store.stats.total }}</h5>
        </div>
      </div>
    </div>
    <div class="panel">
      <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-danger/10">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-danger">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
        </div>
        <div>
          <p class="text-white-dark text-xs">Errores</p>
          <h5 class="text-xl font-bold text-danger">{{ store.stats.errores }}</h5>
        </div>
      </div>
    </div>
    <div class="panel">
      <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-warning/10">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-warning">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
        </div>
        <div>
          <p class="text-white-dark text-xs">Warnings</p>
          <h5 class="text-xl font-bold text-warning">{{ store.stats.warnings }}</h5>
        </div>
      </div>
    </div>
    <div class="panel">
      <div class="flex items-center gap-3">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-success/10">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-success">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
        </div>
        <div>
          <p class="text-white-dark text-xs">Hoy</p>
          <h5 class="text-xl font-bold text-success">{{ store.stats.hoy }}</h5>
        </div>
      </div>
    </div>
  </div>

  <!-- ── Distribución por nivel ── -->
  <div v-if="distribucion.length" class="panel mb-6">
    <div class="flex items-center justify-between mb-4">
      <h5 class="text-base font-bold dark:text-white-light/90">Distribución por nivel</h5>
      <span class="text-xs text-white-dark">{{ store.logs.length }} registros capturados</span>
    </div>
    <!-- Barra apilada -->
    <div class="flex h-3 w-full rounded-full overflow-hidden mb-5">
      <template v-for="d in distribucion" :key="d.nivel">
        <div v-if="d.pct > 0" :class="d.color" :style="{ width: d.pct + '%' }"
          class="transition-all duration-700 first:rounded-l-full last:rounded-r-full"
          :title="`${d.nivel}: ${d.count} (${d.pct}%)`" />
      </template>
    </div>
    <!-- Filas de detalle -->
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
      <div v-for="d in distribucion" :key="d.nivel" class="flex flex-col gap-1.5">
        <div class="flex items-center justify-between text-xs">
          <div class="flex items-center gap-2">
            <span :class="[d.color, 'inline-block w-2.5 h-2.5 rounded-sm flex-shrink-0']" />
            <span :class="d.text" class="font-semibold">{{ d.nivel }}</span>
          </div>
          <span class="text-white-dark font-mono">
            {{ d.count }} <span class="text-[10px] opacity-60">({{ d.pct }}%)</span>
          </span>
        </div>
        <div class="h-2 w-full rounded-full bg-[#ebedf2] dark:bg-[#1b2e4b] overflow-hidden">
          <div :class="d.color" class="h-full rounded-full transition-all duration-700" :style="{ width: d.pct + '%' }" />
        </div>
      </div>
    </div>
  </div>

  <!-- ── Info banner ── -->
  <div class="flex items-start gap-3 rounded-lg bg-primary/5 dark:bg-primary/10 px-4 py-3 text-sm text-white-dark mb-5">
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="mt-0.5 flex-shrink-0 text-primary">
      <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
    </svg>
    Los logs se capturan en tiempo real y persisten en <code class="text-primary font-mono text-xs">localStorage</code>. Se reinician al limpiar el almacenamiento del navegador.
  </div>

  <!-- ── Tabla ── -->
  <div class="panel">
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
      <h5 class="text-xl font-bold dark:text-white-light/90">
        Logs del Frontend
        <span class="ml-2 badge bg-primary/10 text-primary py-1 px-3 rounded-full text-sm">
          {{ logsFiltrados.length }} registros
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
          ]"
          empty-label="Todos los niveles"
          empty-value=""
          placeholder="Todos los niveles"
          class="w-full sm:w-40"
        />

        <!-- Buscador -->
        <div class="relative">
          <input v-model="filBusqueda" type="text" placeholder="Buscar mensaje o componente..."
            class="peer form-input py-2 ltr:pr-11 rtl:pl-11 w-full sm:w-56" />
          <div class="absolute top-1/2 -translate-y-1/2 peer-focus:text-primary ltr:right-[11px] rtl:left-[11px]">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5" /><path d="M18.5 18.5L22 22" stroke-linecap="round" />
            </svg>
          </div>
        </div>

        <div class="hidden sm:block h-7 w-px bg-[#e0e6ed] dark:bg-[#253b5e]" />

        <!-- Limpiar -->
        <button type="button" class="btn btn-outline-danger whitespace-nowrap" @click="limpiarLogs" :disabled="store.logs.length === 0">
          <svg class="inline w-4 h-4 mr-1.5 align-middle" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
          </svg>
          Limpiar
        </button>

        <!-- Exportar dropdown -->
        <div class="relative" data-export-menu-vue>
          <button type="button" class="btn btn-outline-success whitespace-nowrap"
            :disabled="logsFiltrados.length === 0"
            @click.stop="exportMenu = !exportMenu">
            <svg class="inline w-4 h-4 mr-1.5 align-middle" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            Exportar
            <svg class="inline w-3 h-3 ml-1 align-middle transition-transform" :class="exportMenu ? 'rotate-180' : ''" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </button>

          <Transition name="dropdown">
            <div v-if="exportMenu"
              class="absolute right-0 top-full mt-1 z-50 w-44 rounded-lg shadow-lg border border-[#e0e6ed] dark:border-[#1b2e4b] bg-white dark:bg-[#1b2e4b] overflow-hidden">
              <button type="button" class="w-full flex items-center gap-3 px-4 py-2.5 text-sm hover:bg-gray-50 dark:hover:bg-[#253b5e] transition-colors" @click="exportarTxt">
                <svg class="w-4 h-4 text-white-dark flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
                <span class="dark:text-white">Descargar TXT</span>
              </button>
              <div class="border-t border-[#e0e6ed] dark:border-[#253b5e]" />
              <button type="button" class="w-full flex items-center gap-3 px-4 py-2.5 text-sm hover:bg-gray-50 dark:hover:bg-[#253b5e] transition-colors" @click="exportarJson">
                <svg class="w-4 h-4 text-warning flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/>
                </svg>
                <span class="dark:text-white">Descargar JSON</span>
              </button>
              <div class="border-t border-[#e0e6ed] dark:border-[#253b5e]" />
              <button type="button" class="w-full flex items-center gap-3 px-4 py-2.5 text-sm hover:bg-gray-50 dark:hover:bg-[#253b5e] transition-colors" @click="exportarCsv">
                <svg class="w-4 h-4 text-success flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="3" width="18" height="18" rx="2"/>
                  <line x1="3" y1="9" x2="21" y2="9"/><line x1="3" y1="15" x2="21" y2="15"/>
                  <line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/>
                </svg>
                <span class="dark:text-white">Descargar CSV</span>
              </button>
            </div>
          </Transition>
        </div>
      </div>
    </div>

    <!-- Tabla vacía -->
    <div v-if="store.logs.length === 0" class="flex flex-col items-center justify-center py-16 text-white-dark gap-3">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" opacity="0.4">
        <circle cx="12" cy="12" r="10"/>
        <polyline points="20 6 9 17 4 12"/>
      </svg>
      <p class="font-semibold">Sin errores registrados</p>
      <p class="text-xs">Los errores y warnings de Vue aparecerán aquí automáticamente.</p>
    </div>

    <div v-else-if="logsFiltrados.length === 0" class="text-center text-white-dark py-6">
      Sin resultados para los filtros actuales
    </div>

    <div v-else class="table-responsive">
      <table class="table-striped table-hover text-xs">
        <thead>
          <tr>
            <th class="whitespace-nowrap hidden sm:table-cell">Fecha / Hora</th>
            <th class="text-center">Nivel</th>
            <th class="hidden md:table-cell">Componente</th>
            <th>Mensaje</th>
            <th class="hidden lg:table-cell">Ruta</th>
            <th class="text-center">Ver</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in logsFiltrados" :key="log.id" class="cursor-pointer" @click="abrirDrawer(log)">
            <td class="text-white-dark whitespace-nowrap font-mono hidden sm:table-cell">{{ log.fecha }}</td>
            <td class="text-center">
              <span :class="nivelBadge(log.nivel)">{{ log.nivel }}</span>
            </td>
            <td class="text-white-dark font-mono hidden md:table-cell">
              <span :title="log.componente">{{ log.componente || '—' }}</span>
            </td>
            <td>
              <div class="flex flex-col sm:block">
                <span class="text-[10px] text-white-dark font-mono sm:hidden">{{ log.fecha }}</span>
                <span class="block truncate max-w-[160px] sm:max-w-xs md:max-w-sm dark:text-white" :title="log.mensaje">
                  {{ log.mensaje || '—' }}
                </span>
              </div>
            </td>
            <td class="text-white-dark font-mono text-xs hidden lg:table-cell">{{ log.url || '—' }}</td>
            <td class="text-center" @click.stop="abrirDrawer(log)">
              <button type="button" class="btn btn-xs btn-outline-primary px-2">
                <svg class="inline w-3 h-3 align-middle" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" /><circle cx="12" cy="12" r="3" />
                </svg>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- ══════════════════════════════════════════════════════════════ -->
  <!-- Drawer: Detalle del Log Vue                                   -->
  <!-- ══════════════════════════════════════════════════════════════ -->
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="drawerOpen" class="fixed inset-0 z-[998] bg-black/50" @click="cerrarDrawer" />
    </Transition>

    <Transition name="slide">
      <div v-if="drawerOpen"
        class="fixed inset-y-0 ltr:right-0 rtl:left-0 z-[999] flex flex-col bg-white dark:bg-[#0e1726] shadow-2xl"
        style="width: 100%; max-width: 560px">

        <!-- Cabecera -->
        <div class="flex-shrink-0 flex items-center justify-between bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4 border-b border-[#e0e6ed] dark:border-[#1b2e4b]">
          <div class="flex items-center gap-3">
            <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full"
              :class="{
                'bg-danger/10':  drawerLog?.nivel === 'ERROR',
                'bg-warning/10': drawerLog?.nivel === 'WARN',
                'bg-info/10':    drawerLog?.nivel === 'INFO',
                'bg-primary/10': !['ERROR','WARN','INFO'].includes(drawerLog?.nivel ?? ''),
              }">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                :class="{
                  'text-danger':  drawerLog?.nivel === 'ERROR',
                  'text-warning': drawerLog?.nivel === 'WARN',
                  'text-info':    drawerLog?.nivel === 'INFO',
                  'text-primary': !['ERROR','WARN','INFO'].includes(drawerLog?.nivel ?? ''),
                }">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
            </div>
            <div>
              <h3 class="text-base font-semibold">Detalle del Error Vue</h3>
              <p class="text-xs text-white-dark font-mono">{{ drawerLog?.fecha }}</p>
            </div>
          </div>
          <button type="button" class="text-white-dark hover:text-dark transition-colors" @click="cerrarDrawer">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <!-- Cuerpo -->
        <div v-if="drawerLog" class="flex-1 overflow-y-auto">

          <!-- Campos básicos -->
          <div class="px-5 pt-5 pb-4 grid grid-cols-2 gap-x-6 gap-y-4">
            <div>
              <p class="mb-1 text-xs font-semibold uppercase text-white-dark">Nivel</p>
              <span :class="nivelBadge(drawerLog.nivel)">{{ drawerLog.nivel }}</span>
            </div>
            <div>
              <p class="mb-1 text-xs font-semibold uppercase text-white-dark">Componente</p>
              <p class="font-mono text-sm dark:text-white font-semibold">{{ drawerLog.componente || '—' }}</p>
            </div>
            <div>
              <p class="mb-1 text-xs font-semibold uppercase text-white-dark">Ruta</p>
              <p class="font-mono text-xs text-white-dark">{{ drawerLog.url || '—' }}</p>
            </div>
            <div v-if="drawerLog.info">
              <p class="mb-1 text-xs font-semibold uppercase text-white-dark">Hook / Contexto</p>
              <p class="text-xs text-white-dark leading-relaxed">{{ drawerLog.info }}</p>
            </div>
          </div>

          <div class="border-t border-[#e0e6ed] dark:border-[#1b2e4b]" />

          <!-- Mensaje completo -->
          <div class="px-5 py-4">
            <p class="mb-2 text-xs font-semibold uppercase text-white-dark">Mensaje</p>
            <pre class="p-3 rounded-lg text-xs font-mono leading-relaxed overflow-x-auto whitespace-pre-wrap"
              :class="{
                'bg-red-50   dark:bg-[#2a1520] text-danger':    drawerLog.nivel === 'ERROR',
                'bg-amber-50 dark:bg-[#2a1e0a] text-warning':   drawerLog.nivel === 'WARN',
                'bg-gray-100 dark:bg-[#1b2e4b] dark:text-white':!['ERROR','WARN'].includes(drawerLog.nivel),
              }">{{ drawerLog.mensaje || '—' }}</pre>
          </div>

          <!-- Stack Trace -->
          <template v-if="drawerLog.stackTrace">
            <div class="border-t border-[#e0e6ed] dark:border-[#1b2e4b]" />
            <div class="px-5 py-4">
              <div class="flex items-center gap-2 mb-2">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-danger flex-shrink-0">
                  <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                <p class="text-xs font-semibold uppercase text-danger">Stack Trace</p>
              </div>
              <pre class="p-3 bg-red-50 dark:bg-[#2a1520] rounded-lg text-xs overflow-x-auto whitespace-pre-wrap text-danger leading-relaxed">{{ drawerLog.stackTrace }}</pre>
            </div>
          </template>

        </div>

        <!-- Footer -->
        <div class="flex-shrink-0 flex items-center justify-between gap-3 px-5 py-4 border-t border-[#e0e6ed] dark:border-[#1b2e4b] bg-[#fbfbfb] dark:bg-[#121c2c]">
          <p class="text-xs text-white-dark font-mono">{{ drawerLog?.fecha }}</p>
          <div class="flex gap-2">
            <button type="button" class="btn btn-xs btn-outline-secondary px-3"
              @click="navigator.clipboard.writeText(drawerLog.mensaje + (drawerLog.stackTrace ? '\n\n' + drawerLog.stackTrace : '')).then(() => toast('Copiado', 'success'))">
              <svg class="inline w-3 h-3 mr-1 align-middle" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
              </svg>
              Copiar
            </button>
            <button class="btn btn-outline-danger btn-xs px-3" @click="cerrarDrawer">Cerrar</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active,    .fade-leave-active    { transition: opacity 0.25s ease; }
.fade-enter-from,      .fade-leave-to        { opacity: 0; }

.slide-enter-active,   .slide-leave-active   { transition: transform 0.3s cubic-bezier(.4,0,.2,1); }
.slide-enter-from,     .slide-leave-to       { transform: translateX(100%); }

.dropdown-enter-active,.dropdown-leave-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.dropdown-enter-from,  .dropdown-leave-to    { opacity: 0; transform: translateY(-6px); }
</style>
