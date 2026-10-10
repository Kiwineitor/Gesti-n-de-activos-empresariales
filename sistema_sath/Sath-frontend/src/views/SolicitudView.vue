<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { useWebSocket } from '@/composables/useWebSocket'
import { useRouter } from 'vue-router'
import SearchSelect from '@/components/ui/SearchSelect.vue'
import DataTable from '@/components/ui/DataTable.vue'
import Skeleton from '@/components/ui/Skeleton.vue'
import SathLoader from '@/components/ui/SathLoader.vue'

const columns = [
  { key: 'nroSol', label: 'Solicitud', sortable: true },
  { key: 'descripcionSol', label: 'Descripción', sortable: true },
  { key: 'estadoSol', label: 'Estado', sortable: true, thClass: 'text-center', tdClass: 'text-center' },
  { key: 'acciones', label: 'Acciones', sortable: false, thClass: 'text-center', tdClass: 'text-center' },
]

const auth = useAuthStore()
const { get, patch, post } = useApi()
const { toast } = useToast()
const { on } = useWebSocket()
const router = useRouter()

const esAdmin = auth.isAdmin
const esTecnico = auth.isTecnico

const solicitudList = ref([])
const textoBusqueda = ref('')
const filtroEstado = ref('')
const cargando = ref(false)
const guardando = ref(false)
const modoAutoAsignar = ref(false)
const tomando = ref(false)

const tipoVista = ref(localStorage.getItem('sath_sol_vista') || (typeof window !== 'undefined' && window.innerWidth < 1024 ? 'grid' : 'list'))

const modalEstado = ref(false)
const modalAyuda = ref(false)
const solicitudSeleccionada = ref(null)
const formEstado = ref({ estado: '', observacion: '' })

const solicitudListFiltrada = computed(() => {
  const texto = textoBusqueda.value.toLowerCase().trim()
  return solicitudList.value.filter((s) => {
    const coincideTexto =
      !texto ||
      String(s.nroSol ?? '').includes(texto) ||
      (s.descripcionSol ?? '').toLowerCase().includes(texto) ||
      (s.nroSolExterna ?? '').toLowerCase().includes(texto)
    const coincideEstado = !filtroEstado.value || s.estadoSol === filtroEstado.value
    return coincideTexto && coincideEstado
  })
})

onMounted(async () => {
  await cargarModo()
  await cargar()
})
on('sath:nueva-solicitud', cargar)
on('sath:modo-auto-asignar', (e) => {
  modoAutoAsignar.value = e.detail.activo
  cargar()
})

async function cargarModo() {
  const res = await get('/api/solicitud/modo-auto-asignar')
  if (res && res.ok) {
    const data = await res.json()
    modoAutoAsignar.value = data.activo
  }
}

async function cargar() {
  cargando.value = true
  try {
    let url
    if (esTecnico && !esAdmin) {
      url = modoAutoAsignar.value
        ? '/api/solicitud/listar'
        : '/api/solicitud/mis-solicitudes'
    } else {
      url = filtroEstado.value
        ? `/api/solicitud/listar?estado=${filtroEstado.value}`
        : '/api/solicitud/listar'
    }
    const res = await get(url)
    if (!res) return
    if (!res.ok) throw new Error('Error al cargar solicitudes')
    solicitudList.value = await res.json()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    cargando.value = false
  }
}

async function toggleAutoAsignar() {
  const res = await post('/api/solicitud/modo-auto-asignar/toggle')
  if (!res || !res.ok) { toast('Error al cambiar el modo', 'error'); return }
  const data = await res.json()
  toast(data.activo ? 'Auto-asignación activada' : 'Auto-asignación desactivada',
        data.activo ? 'success' : 'info')
}

async function tomarSolicitud(s) {
  tomando.value = true
  try {
    const res = await post(`/api/tecnicoAsignado/tomar/${s.idSolicitud}`)
    if (!res) return
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      throw new Error(err.message || `Error ${res.status}`)
    }
    toast('Solicitud tomada exitosamente', 'success')
    await cargar()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    tomando.value = false
  }
}

function cambiarVista(tipo) {
  tipoVista.value = tipo
  localStorage.setItem('sath_sol_vista', tipo)
}

function verDetalle(s) {
  router.push(`/solicitud/${s.idSolicitud}`)
}

function abrirCambiarEstado(s) {
  solicitudSeleccionada.value = s
  formEstado.value = { estado: s.estadoSol, observacion: '' }
  modalEstado.value = true
}

async function guardarEstado() {
  if (!formEstado.value.estado) {
    toast('Selecciona un estado', 'error')
    return
  }
  guardando.value = true
  try {
    const res = await patch(
      `/api/solicitud/${solicitudSeleccionada.value.idSolicitud}/estado`,
      formEstado.value,
    )
    if (!res) return
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      throw new Error(err.message || `Error ${res.status}`)
    }
    toast('Estado actualizado', 'success')
    modalEstado.value = false
    await cargar()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    guardando.value = false
  }
}

function badgeClase(estado) {
  return (
    {
      PENDIENTE: 'sath-badge sath-badge-warning',
      EN_MANTENIMIENTO: 'sath-badge sath-badge-info',
      FINALIZADO: 'sath-badge sath-badge-success',
    }[estado] ?? 'sath-badge sath-badge-secondary'
  )
}

function etiquetaEstado(estado) {
  return (
    {
      PENDIENTE: 'Pendiente',
      EN_MANTENIMIENTO: 'En Mantenimiento',
      FINALIZADO: 'Finalizado',
    }[estado] ?? estado
  )
}
</script>

<template>
  <!-- Breadcrumb -->
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Solicitudes</li>
  </ul>

  <div class="panel">
    <!-- Título + controles -->
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">

      <!-- Izquierda: título + ayuda + toggle lista/grilla -->
      <div class="flex items-center gap-3 flex-wrap">
        <div class="flex items-center gap-2">
          <h5 class="text-xl font-bold dark:text-white-light/90">
            Solicitudes
            <span class="ml-2 badge bg-primary/10 text-primary py-1 px-3 rounded-full text-sm">
              {{ solicitudListFiltrada.length }} registros
            </span>
          </h5>
          <!-- Botón ayuda -->
          <button
            type="button"
            title="Ayuda"
            class="grid h-6 w-6 place-content-center rounded-full border border-[#e0e6ed] dark:border-[#1b2e4b] text-white-dark hover:border-primary hover:text-primary transition-colors text-xs font-bold shrink-0"
            @click="modalAyuda = true"
          >?</button>
        </div>

        <!-- Toggle vista lista / grilla -->
        <div class="flex items-center gap-1 border border-[#e0e6ed] dark:border-[#1b2e4b] rounded-lg p-1">
          <button
            type="button"
            title="Vista lista"
            class="p-1.5 rounded transition-colors"
            :class="tipoVista === 'list' ? 'bg-primary text-white' : 'text-white-dark hover:bg-[#f1f5ff] dark:hover:bg-[#1b2e4b]'"
            @click="cambiarVista('list')"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/>
              <line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/>
              <line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/>
            </svg>
          </button>
          <button
            type="button"
            title="Vista grilla"
            class="p-1.5 rounded transition-colors"
            :class="tipoVista === 'grid' ? 'bg-primary text-white' : 'text-white-dark hover:bg-[#f1f5ff] dark:hover:bg-[#1b2e4b]'"
            @click="cambiarVista('grid')"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
              <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Botón auto-asignación (solo admin) -->
      <button
        v-if="esAdmin"
        type="button"
        class="btn btn-sm flex items-center gap-1.5"
        :class="modoAutoAsignar ? 'btn-success' : 'btn-outline-secondary'"
        :title="modoAutoAsignar ? 'Desactivar auto-asignación' : 'Activar auto-asignación para técnicos'"
        @click="toggleAutoAsignar"
      >
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
        </svg>
        {{ modoAutoAsignar ? 'Auto-Asignar activo' : 'Auto-Asignar inactivo' }}
      </button>

      <!-- Derecha: filtros -->
      <div class="flex w-full flex-col gap-3 sm:w-auto sm:flex-row sm:items-center">
        <SearchSelect
          v-model="filtroEstado"
          :options="[
            { value: 'PENDIENTE',        label: 'Pendiente' },
            { value: 'EN_MANTENIMIENTO', label: 'En Mantenimiento' },
            { value: 'FINALIZADO',       label: 'Finalizado' },
          ]"
          empty-label="Todos los estados"
          empty-value=""
          placeholder="Todos los estados"
          class="w-full sm:w-48"
          @change="cargar"
        />
        <div class="relative">
          <input
            v-model="textoBusqueda"
            type="text"
            placeholder="Buscar..."
            class="peer form-input py-2 ltr:pr-11 rtl:pl-11 w-full sm:w-52"
          />
          <div class="absolute top-1/2 -translate-y-1/2 peer-focus:text-primary ltr:right-[11px] rtl:left-[11px]">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5"/>
              <path d="M18.5 18.5L22 22" stroke-linecap="round"/>
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- Banner modo auto-asignación (técnico) -->
    <div
      v-if="modoAutoAsignar && esTecnico && !esAdmin"
      class="mb-4 flex items-center gap-2 rounded-lg bg-success/10 px-4 py-2.5 text-sm text-success"
    >
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
      </svg>
      Modo auto-asignación activo — puedes tomar cualquier solicitud pendiente.
    </div>

    <!-- Loader Llamativo -->
    <SathLoader v-if="cargando && tipoVista === 'list'" size="md" />

    <template v-else-if="!cargando || tipoVista === 'grid'">
      <!-- ── VISTA LISTA (Data Table) ── -->
      <div v-if="tipoVista === 'list'">
        <DataTable
          :data="solicitudListFiltrada"
          :columns="columns"
          :loading="cargando"
          default-sort="nroSol"
          :default-sort-asc="false"
          empty-text="No hay solicitudes que coincidan"
        >
          <template #cell-nroSol="{ row }">
            <p class="font-semibold leading-tight dark:text-white">{{ row.nroSol || 'SOL-' + row.idSolicitud }}</p>
            <div class="flex items-center gap-1.5 mt-0.5 flex-wrap">
              <span v-if="row.posicionCola > 0" class="badge bg-primary/10 text-primary text-[10px] font-bold">#{{ row.posicionCola }}</span>
              <span class="text-xs text-white-dark font-mono">{{ row.fechaSol || '—' }}</span>
            </div>
          </template>
          <template #cell-descripcionSol="{ row }">
            <p class="leading-snug">{{ row.descripcionSol || '—' }}</p>
            <p v-if="row.nroSolExterna" class="text-xs text-white-dark font-mono mt-0.5">{{ row.nroSolExterna }}</p>
          </template>
          <template #cell-estadoSol="{ row }">
            <span :class="badgeClase(row.estadoSol)">{{ etiquetaEstado(row.estadoSol) }}</span>
          </template>
          <template #cell-acciones="{ row }">
            <div class="flex items-center justify-center gap-1">
              <button class="btn btn-xs btn-outline-primary px-2" title="Ver detalle" @click="verDetalle(row)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                </svg>
              </button>
              <button
                v-if="row.estadoSol !== 'FINALIZADO' && (esAdmin || esTecnico)"
                class="btn btn-xs btn-outline-warning px-2"
                title="Cambiar estado"
                @click="abrirCambiarEstado(row)"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
                </svg>
              </button>
              <button
                v-if="modoAutoAsignar && esTecnico && !esAdmin && row.estadoSol === 'PENDIENTE'"
                class="btn btn-xs btn-success px-2 flex items-center gap-1"
                title="Tomar esta solicitud"
                :disabled="tomando"
                @click="tomarSolicitud(row)"
              >
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M9 12l2 2 4-4"/><circle cx="12" cy="12" r="10"/>
                </svg>
                Tomar
              </button>
            </div>
          </template>
        </DataTable>
      </div>

      <!-- ── VISTA GRILLA ── -->
      <div v-else>
        <div v-if="cargando" class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <div v-for="i in 6" :key="i" class="panel flex flex-col gap-4">
            <div class="flex justify-between items-center">
              <Skeleton width="40%" height="1.4rem" />
              <Skeleton width="25%" height="1.2rem" />
            </div>
            <Skeleton width="100%" height="2.5rem" />
            <div class="flex gap-4 border-t border-[#e0e6ed] dark:border-[#1b2e4b] pt-3">
              <Skeleton width="30%" height="0.8rem" />
              <Skeleton width="30%" height="0.8rem" />
            </div>
            <div class="flex gap-2">
              <Skeleton width="50%" height="2rem" />
              <Skeleton width="50%" height="2rem" />
            </div>
          </div>
        </div>
        <template v-else>
          <p v-if="solicitudListFiltrada.length === 0" class="py-10 text-center text-white-dark">
            No hay solicitudes que coincidan
          </p>
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
            <div
              v-for="s in solicitudListFiltrada"
              :key="s.idSolicitud"
              class="panel flex flex-col gap-3 hover:shadow-md transition-shadow duration-200"
            >
            <!-- Header: nro + cola + estado -->
            <div class="flex items-center justify-between">
              <span class="text-base font-bold dark:text-white">{{ s.nroSol || 'SOL-' + s.idSolicitud }}</span>
              <div class="flex items-center gap-1.5">
                <span v-if="s.posicionCola > 0" class="badge bg-primary/10 text-primary text-xs font-bold">#{{ s.posicionCola }}</span>
                <span :class="badgeClase(s.estadoSol)">{{ etiquetaEstado(s.estadoSol) }}</span>
              </div>
            </div>

            <!-- Descripción -->
            <p class="text-sm text-white-dark line-clamp-2">{{ s.descripcionSol || '—' }}</p>

            <!-- Meta -->
            <div class="flex flex-wrap gap-x-4 gap-y-1 text-xs text-white-dark border-t border-[#e0e6ed] dark:border-[#1b2e4b] pt-2">
              <span class="flex items-center gap-1">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
                {{ s.fechaSol || '—' }}
              </span>
              <span v-if="s.nroSolExterna" class="flex items-center gap-1">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                  <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                </svg>
                {{ s.nroSolExterna }}
              </span>
            </div>

            <!-- Acciones -->
            <div class="flex gap-2 pt-1">
              <button
                class="btn btn-xs btn-outline-primary flex-1 flex items-center justify-center gap-1"
                @click="verDetalle(s)"
              >
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                </svg>
                Ver detalle
              </button>
              <button
                v-if="s.estadoSol !== 'FINALIZADO' && (esAdmin || esTecnico)"
                class="btn btn-xs btn-outline-warning flex-1 flex items-center justify-center gap-1"
                @click="abrirCambiarEstado(s)"
              >
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
                </svg>
                Estado
              </button>
              <button
                v-if="modoAutoAsignar && esTecnico && !esAdmin && s.estadoSol === 'PENDIENTE'"
                class="btn btn-xs btn-success flex-1 flex items-center justify-center gap-1"
                :disabled="tomando"
                @click="tomarSolicitud(s)"
              >
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M9 12l2 2 4-4"/><circle cx="12" cy="12" r="10"/>
                </svg>
                Tomar
              </button>
            </div>
          </div>
        </div>
      </template>
    </div>
  </template>
</div>

  <!-- ── MODAL CAMBIAR ESTADO ── -->
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="modalEstado" class="fixed inset-0 z-[999] overflow-y-auto bg-black/60" @click.self="modalEstado = false">
        <div class="flex min-h-screen items-center justify-center px-4">
          <div class="panel w-full max-w-sm rounded-lg border-0 p-0 overflow-hidden">
            <div class="flex items-center justify-between bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4">
              <div class="flex items-center gap-3">
                <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-warning/10">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-warning">
                    <polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
                  </svg>
                </div>
                <div>
                  <h3 class="text-base font-semibold">Actualizar Estado</h3>
                  <p class="text-xs text-white-dark">
                    {{ solicitudSeleccionada ? (solicitudSeleccionada.nroSol || 'SOL-' + solicitudSeleccionada.idSolicitud) : '' }}
                  </p>
                </div>
              </div>
              <button type="button" class="text-white-dark hover:text-dark" @click="modalEstado = false">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
            <div class="p-5 space-y-4">
              <div>
                <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Estado</label>
                <SearchSelect
                  v-model="formEstado.estado"
                  :options="[
                    { value: 'PENDIENTE',        label: 'Pendiente' },
                    { value: 'EN_MANTENIMIENTO', label: 'En Mantenimiento' },
                    { value: 'FINALIZADO',       label: 'Finalizado' },
                  ]"
                  placeholder="Seleccionar estado..."
                />
              </div>
              <div>
                <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Observaciones</label>
                <textarea v-model="formEstado.observacion" class="form-textarea" rows="3" placeholder="Observaciones..." />
              </div>
              <div class="flex justify-end gap-2">
                <button class="btn btn-outline-danger" @click="modalEstado = false">Cancelar</button>
                <button class="btn btn-warning" :disabled="guardando" @click="guardarEstado">
                  {{ guardando ? 'Guardando...' : 'Guardar' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ── MODAL AYUDA ── -->
    <Transition name="fade">
      <div v-if="modalAyuda" class="fixed inset-0 z-[999] overflow-y-auto bg-black/60" @click.self="modalAyuda = false">
        <div class="flex min-h-screen items-center justify-center px-4">
          <div class="panel w-full max-w-lg rounded-lg border-0 p-0 overflow-hidden">
            <!-- Header -->
            <div class="flex items-center justify-between bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4">
              <div class="flex items-center gap-3">
                <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary/10">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-primary">
                    <circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
                    <line x1="12" y1="17" x2="12.01" y2="17"/>
                  </svg>
                </div>
                <h3 class="text-base font-semibold">Ayuda — Solicitudes</h3>
              </div>
              <button type="button" class="text-white-dark hover:text-dark" @click="modalAyuda = false">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
            <!-- Body -->
            <div class="p-5 space-y-4 text-sm leading-relaxed">
              <div>
                <h4 class="font-semibold text-black dark:text-white mb-1">Acerca de este módulo</h4>
                <p class="text-white-dark">
                  Dentro de este módulo se podrá ver un listado de todas las solicitudes que llegan al sistema,
                  cada una con información detallada correspondiente. Podés alternar entre <strong class="text-black dark:text-white">vista lista</strong>
                  y <strong class="text-black dark:text-white">vista grilla</strong> con los botones del encabezado.
                </p>
              </div>
              <div>
                <h4 class="font-semibold text-black dark:text-white mb-2">Atender una solicitud</h4>
                <p class="text-white-dark">
                  Para atender una solicitud se debe cambiar su estado:
                </p>
                <div class="mt-2 flex flex-wrap gap-2 items-center">
                  <span class="sath-badge sath-badge-warning">Pendiente</span>
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-white-dark shrink-0"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
                  <span class="sath-badge sath-badge-info">En Mantenimiento</span>
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-white-dark shrink-0"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
                  <span class="sath-badge sath-badge-success">Finalizado</span>
                </div>
                <p class="text-white-dark mt-2">
                  Cada cambio de estado puede incluir <span class="font-semibold text-primary">observaciones</span>
                  que serán notificadas a quien realizó la solicitud.
                </p>
              </div>
            </div>
            <div class="flex justify-end px-5 pb-5">
              <button class="btn btn-primary px-6" @click="modalAyuda = false">Entendido</button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>