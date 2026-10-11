<script setup>
import {ref, computed, onMounted} from 'vue'
import {useAuthStore} from '@/stores/auth'
import {useApi} from '@/composables/useApi'
import {useToast} from '@/composables/useToast'
import DrawerAtenderEquipo from '@/components/DrawerAtenderEquipo.vue'
import DataTable from '@/components/ui/DataTable.vue'
import Skeleton from '@/components/ui/Skeleton.vue'
import SathLoader from '@/components/ui/SathLoader.vue'

const columnsTabla = [
  { key: 'equipo', label: 'Recurso' },
  { key: 'prioridad', label: 'Prioridad', thClass: 'text-center', tdClass: 'text-center' },
  { key: 'estadoDetalle', label: 'Estado', thClass: 'text-center', tdClass: 'text-center' },
  { key: 'acciones', label: 'Acciones', sortable: false, thClass: 'text-center', tdClass: 'text-center' },
]

const auth = useAuthStore()
const {get} = useApi()
const {toast} = useToast()

const esAdmin = auth.isAdmin
const esTecnico = auth.isTecnico

const lista = ref([])
const textoBusqueda = ref('')
const filtroEstado = ref('')
const filtroPrioridad = ref('')
const ordenAsc = ref(true)
const cargando = ref(false)
const vistaGrilla = ref(typeof window !== 'undefined' && window.innerWidth < 768)

const modalDetalle = ref(false)
const itemDetalle = ref(null)

const drawerAtender = ref(false)
const itemSeleccionado = ref(null)

// Paleta de colores por solicitud
const solicitudColores = ref({})
const palette = ['#4361ee', '#00ab55', '#e2a03f', '#2196f3', '#e7515a', '#805dca', '#3b3f5c', '#1abc9c']

const listaFiltrada = computed(() => {
  const texto = textoBusqueda.value.toLowerCase().trim()
  const estado = filtroEstado.value
  const prioridad = filtroPrioridad.value

  return lista.value.filter(item => {
    const coincideTexto = !texto
        || (item.nroSolicitud ?? '').toLowerCase().includes(texto)
        || (item.descripcionActivo ?? '').toLowerCase().includes(texto)
        || (item.codigoActivo ?? '').toLowerCase().includes(texto)

    const coincideEstado = !estado
        || (item.estadoDetalle ?? 'PENDIENTE') === estado

    const coincidePrioridad = !prioridad
        || (prioridad === 'SIN' ? !item.prioridad : item.prioridad === prioridad)

    return coincideTexto && coincideEstado && coincidePrioridad
  })
})

const listaAgrupada = computed(() => {
  const grupos = new Map()
  for (const item of listaFiltrada.value) {
    const key = item.idSolicitud ?? 0
    if (!grupos.has(key)) {
      grupos.set(key, {idSolicitud: key, nroSolicitud: item.nroSolicitud, items: []})
    }
    grupos.get(key).items.push(item)
  }
  return [...grupos.values()]
})

onMounted(cargar)

async function cargar() {
  cargando.value = true
  try {
    const url = esTecnico && !esAdmin
        ? '/api/atencion-solicitud/mis-atenciones'
        : '/api/atencion-solicitud/listar-atenciones'
    const res = await get(url)
    if (!res) return
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      throw new Error(err.error || `Error ${res.status} al cargar atenciones`)
    }
    const data = await res.json()
    lista.value = Array.isArray(data) ? data : []
    construirColores()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    cargando.value = false
  }
}

function construirColores() {
  const mapa = {}
  let idx = 0
  for (const item of lista.value) {
    const id = item.idSolicitud
    if (id != null && !(id in mapa)) {
      mapa[id] = palette[idx % palette.length]
      idx++
    }
  }
  solicitudColores.value = mapa
}

function estiloFila(idSolicitud) {
  const c = solicitudColores.value[idSolicitud]
  return c ? `border-left: 4px solid ${c}` : ''
}

function ordenarPorPrioridad() {
  ordenAsc.value = !ordenAsc.value
  const orden = {ALTO: 1, MEDIO: 2, BAJO: 3}
  lista.value = [...lista.value].sort((a, b) => {
    const pa = orden[a.prioridad] ?? 99
    const pb = orden[b.prioridad] ?? 99
    return ordenAsc.value ? pa - pb : pb - pa
  })
}

function abrirDetalle(item) {
  itemDetalle.value = item
  modalDetalle.value = true
}

function abrirAtender(item) {
  itemSeleccionado.value = item
  drawerAtender.value = true
}

function onGuardado(resultado) {
  const idx = lista.value.findIndex(i => i.idDetalleSolicitud === resultado.idDetalleSolicitud)
  if (idx !== -1) {
    lista.value[idx] = {
      ...lista.value[idx],
      prioridad: resultado.prioridad,
      ordenPrioridad: resultado.ordenPrioridad,
      servicio: resultado.servicio,
      estadoDetalle: 'EN_ATENCION',
      idAtencion: true,
      idPrioridad: resultado.idPrioridad,
      idsServicios: resultado.idsServicios,
    }
  }
}

function badgePrioridad(valor) {
  if (!valor) return 'sath-badge sath-badge-secondary'
  return {
    ALTO: 'sath-badge sath-badge-danger',
    MEDIO: 'sath-badge sath-badge-warning',
    BAJO: 'sath-badge sath-badge-success'
  }[valor] ?? 'sath-badge sath-badge-secondary'
}

function etiquetaPrioridad(valor) {
  return {ALTO: 'Alta', MEDIO: 'Media', BAJO: 'Baja'}[valor] ?? (valor || 'Sin prioridad')
}

function badgeEstado(estado) {
  return {
    PENDIENTE: 'sath-badge sath-badge-warning',
    EN_ATENCION: 'sath-badge sath-badge-info',
    RESUELTO: 'sath-badge sath-badge-success'
  }[estado] ?? 'sath-badge sath-badge-warning'
}

function etiquetaEstado(estado) {
  return {PENDIENTE: 'Pendiente', EN_ATENCION: 'En Atención', RESUELTO: 'Resuelto'}[estado] ?? (estado ?? 'Pendiente')
}
</script>

<template>
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li>
      <RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink>
    </li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Atenciones</li>
  </ul>

  <div class="panel">
    <!-- Título + filtros -->
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
       <div class="flex items-center gap-3 flex-wrap">
        <h5 class="text-xl font-bold dark:text-white-light/90">Atenciones por Recurso</h5>
        <span class="badge bg-primary/10 text-primary py-1 px-3 rounded-full text-sm">
          {{ listaFiltrada.length }} recursos · {{ listaAgrupada.length }} solicitudes
        </span>
        <!-- Toggle tabla / tarjetas -->
        <div class="flex rounded-lg overflow-hidden border border-white-light dark:border-[#253b5e]">
          <button type="button"
            :class="!vistaGrilla ? 'bg-primary text-white' : 'bg-white dark:bg-[#1b2e4b] text-white-dark hover:text-primary'"
            class="px-2.5 py-1.5 transition-colors" title="Vista tabla" @click="vistaGrilla = false">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/>
              <line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/>
              <line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/>
            </svg>
          </button>
          <button type="button"
            :class="vistaGrilla ? 'bg-primary text-white' : 'bg-white dark:bg-[#1b2e4b] text-white-dark hover:text-primary'"
            class="px-2.5 py-1.5 transition-colors border-l border-white-light dark:border-[#253b5e]" title="Vista tarjetas" @click="vistaGrilla = true">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
              <rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>
            </svg>
          </button>
        </div>
      </div>
      <div class="flex w-full flex-col gap-3 sm:w-auto sm:flex-row sm:items-center">
        <select v-model="filtroPrioridad" class="form-select py-2 w-full sm:w-44">
          <option value="">Todas las prioridades</option>
          <option value="ALTO">Alta</option>
          <option value="MEDIO">Media</option>
          <option value="BAJO">Baja</option>
          <option value="SIN">Sin prioridad</option>
        </select>
        <select v-model="filtroEstado" class="form-select py-2 w-full sm:w-44">
          <option value="">Todos los estados</option>
          <option value="PENDIENTE">Pendiente</option>
          <option value="EN_ATENCION">En Atención</option>
          <option value="RESUELTO">Resuelto</option>
        </select>
        <div class="relative">
          <input v-model="textoBusqueda" type="text" placeholder="Buscar solicitud o recurso..."
                 class="peer form-input py-2 ltr:pr-11 rtl:pl-11 w-full sm:w-56"/>
          <div class="absolute top-1/2 -translate-y-1/2 peer-focus:text-primary ltr:right-[11px] rtl:left-[11px]">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5"/>
              <path d="M18.5 18.5L22 22" stroke-linecap="round"/>
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- Loader Llamativo -->
    <SathLoader v-if="cargando && !vistaGrilla" size="md" />

    <!-- Vista Tarjetas -->
    <div v-else-if="vistaGrilla" class="space-y-5">
      <div v-if="cargando" class="space-y-6">
        <div v-for="i in 3" :key="i" class="space-y-3">
          <div class="flex items-center gap-3 px-1">
            <Skeleton width="12px" height="12px" circle />
            <Skeleton width="120px" height="1.2rem" />
            <Skeleton width="60px" height="1rem" />
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            <div v-for="j in 3" :key="j" class="panel flex flex-col gap-3">
              <Skeleton width="70%" height="1.2rem" />
              <div class="flex gap-2">
                <Skeleton width="60px" height="1.5rem" />
                <Skeleton width="80px" height="1.5rem" />
              </div>
              <div class="flex gap-2 mt-2">
                <Skeleton width="50%" height="2rem" />
                <Skeleton width="50%" height="2rem" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <template v-else>
        <div v-if="listaFiltrada.length === 0" class="text-center text-white-dark py-10">
          No hay recursos que coincidan con los filtros.
        </div>
        <template v-for="grupo in listaAgrupada" :key="grupo.idSolicitud">
          <div class="flex items-center gap-2.5 px-1 mb-2">
            <span class="inline-block w-3 h-3 rounded-full flex-shrink-0"
                  :style="`background-color: ${solicitudColores[grupo.idSolicitud] || '#888'}`"/>
            <span class="font-bold text-primary text-sm">{{ grupo.nroSolicitud || 'SOL-' + grupo.idSolicitud }}</span>
            <span class="badge bg-white-dark/10 text-white-dark text-xs">
              {{ grupo.items.length }} {{ grupo.items.length === 1 ? 'recurso' : 'recursos' }}
            </span>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 mb-4">
            <div v-for="item in grupo.items" :key="item.idDetalleSolicitud"
                 class="rounded-xl border border-white-light dark:border-[#1b2e4b] bg-white dark:bg-[#0e1726] p-4 flex flex-col gap-3"
                 :style="`border-left: 3px solid ${solicitudColores[item.idSolicitud] || '#ccc'}`">
              <div>
                <p class="font-semibold leading-tight dark:text-white text-sm">{{ item.descripcionActivo || '—' }}</p>
                <p v-if="item.codigoActivo" class="text-xs text-white-dark font-mono mt-0.5">{{ item.codigoActivo }}</p>
              </div>
              <div class="flex flex-wrap gap-1.5">
                <span :class="badgePrioridad(item.prioridad)">{{ etiquetaPrioridad(item.prioridad) }}</span>
                <span :class="badgeEstado(item.estadoDetalle)">{{ etiquetaEstado(item.estadoDetalle) }}</span>
              </div>
              <div class="flex gap-2 mt-auto pt-1">
                <button type="button" class="btn btn-outline-info btn-sm flex-1" @click="abrirDetalle(item)">
                  Ver
                </button>
                <button v-if="esAdmin || esTecnico" type="button"
                        :class="item.idAtencion ? 'btn btn-outline-success btn-sm flex-1' : 'btn btn-primary btn-sm flex-1'"
                        @click="abrirAtender(item)">
                  {{ item.idAtencion ? 'Atendido' : 'Atender' }}
                </button>
              </div>
            </div>
          </div>
        </template>
      </template>
    </div>

    <!-- Tabla -->
    <div v-else>
      <DataTable
        :data="listaFiltrada"
        :columns="columnsTabla"
        :loading="cargando"
        empty-text="No hay recursos que coincidan con los filtros."
      >
        <template #cell-equipo="{ row }">
          <div :style="estiloFila(row.idSolicitud)" class="pl-2">
            <p class="font-semibold leading-tight dark:text-white">{{ row.descripcionActivo || '—' }}</p>
            <p v-if="row.codigoActivo" class="text-xs text-white-dark font-mono mt-0.5">{{ row.codigoActivo }}</p>
            <span class="text-xs font-bold mt-0.5 inline-block"
              :style="`color: ${solicitudColores[row.idSolicitud] || '#888'}`">
              {{ row.nroSolicitud || 'SOL-' + row.idSolicitud }}
            </span>
          </div>
        </template>
        <template #cell-prioridad="{ row }">
          <span :class="badgePrioridad(row.prioridad)">{{ etiquetaPrioridad(row.prioridad) }}</span>
        </template>
        <template #cell-estadoDetalle="{ row }">
          <span :class="badgeEstado(row.estadoDetalle)">{{ etiquetaEstado(row.estadoDetalle) }}</span>
        </template>
        <template #cell-acciones="{ row }">
          <div class="flex items-center justify-center gap-2">
            <button type="button" class="btn btn-xs btn-outline-info px-2" title="Ver detalles"
                    @click="abrirDetalle(row)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </button>
            <button v-if="esAdmin || esTecnico" type="button"
                    :class="row.idAtencion ? 'btn btn-xs btn-outline-success' : 'btn btn-xs btn-primary'"
                    @click="abrirAtender(row)">
              {{ row.idAtencion ? 'Atendido' : 'Atender' }}
            </button>
          </div>
        </template>
      </DataTable>
    </div>
  </div>

  <!-- ── MODAL VER DETALLES ── -->
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="modalDetalle" class="fixed inset-0 z-[999] overflow-y-auto bg-black/60"
           @click.self="modalDetalle = false">
        <div class="flex min-h-screen items-center justify-center px-4">
          <div class="panel rounded-lg border-0 p-0 overflow-hidden" style="width: 100%; max-width: 576px">
            <div class="flex items-center justify-between bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4">
              <div class="flex items-center gap-3">
                <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-info/10">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                       class="text-info">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                    <circle cx="12" cy="12" r="3"/>
                  </svg>
                </div>
                <h3 class="text-base font-semibold">Detalle del recurso</h3>
              </div>
              <button type="button" class="text-white-dark hover:text-dark" @click="modalDetalle = false">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"/>
                  <line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
            <div v-if="itemDetalle" class="p-5 space-y-4">
              <div class="rounded-lg bg-white-light/50 dark:bg-[#1b2e4b] p-4">
                <div class="flex items-start justify-between gap-3 mb-3">
                  <div>
                    <p class="font-bold text-base leading-tight">{{ itemDetalle.descripcionActivo || '—' }}</p>
                    <p class="text-xs font-mono text-white-dark mt-0.5">{{ itemDetalle.codigoActivo }}</p>
                  </div>
                  <div class="flex flex-col items-end gap-1 shrink-0">
                    <span class="badge bg-secondary/10 text-secondary text-xs">{{
                        itemDetalle.tipoActivo || '—'
                      }}</span>
                    <span :class="badgeEstado(itemDetalle.estadoDetalle)">{{
                        etiquetaEstado(itemDetalle.estadoDetalle)
                      }}</span>
                  </div>
                </div>
                <div class="flex items-center gap-2">
                  <span class="text-xs uppercase font-semibold text-white-dark">Solicitud:</span>
                  <span class="text-sm font-bold text-primary">{{ itemDetalle.nroSolicitud }}</span>
                </div>
              </div>
              <div v-if="itemDetalle.problemaReportado">
                <p class="text-xs uppercase font-semibold text-white-dark mb-1">Problema reportado</p>
                <p class="text-sm bg-warning/5 border border-warning/20 rounded-lg px-3 py-2 leading-relaxed">
                  {{ itemDetalle.problemaReportado }}
                </p>
              </div>
              <div v-if="itemDetalle.idAtencion" class="space-y-3">
                <p class="text-xs uppercase font-semibold text-white-dark">Diagnóstico</p>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div class="rounded-lg bg-white-light/50 dark:bg-[#1b2e4b] p-3">
                    <p class="text-xs text-white-dark mb-1">Prioridad</p>
                    <span :class="badgePrioridad(itemDetalle.prioridad)">{{
                        etiquetaPrioridad(itemDetalle.prioridad)
                      }}</span>
                  </div>
                  <div class="rounded-lg bg-white-light/50 dark:bg-[#1b2e4b] p-3">
                    <p class="text-xs text-white-dark mb-1">Servicio</p>
                    <p class="text-sm font-semibold">{{ itemDetalle.servicio || '—' }}</p>
                  </div>
                </div>
              </div>
              <div class="flex justify-end gap-2 pt-1">
                <button type="button" class="btn btn-outline-danger" @click="modalDetalle = false">Cerrar</button>
                <button v-if="!itemDetalle.idAtencion && (esAdmin || esTecnico)" type="button" class="btn btn-primary"
                        @click="modalDetalle = false; abrirAtender(itemDetalle)">
                  Atender
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>

  </Teleport>

  <!-- ── DRAWER ATENDER RECURSO ── -->
  <DrawerAtenderEquipo
    v-model="drawerAtender"
    :item="itemSeleccionado"
    @guardado="onGuardado"
  />

</template>