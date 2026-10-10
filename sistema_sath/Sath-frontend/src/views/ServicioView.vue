<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import SearchSelect from '@/components/ui/SearchSelect.vue'
import DataTable from '@/components/ui/DataTable.vue'
import Skeleton from '@/components/ui/Skeleton.vue'
import SathLoader from '@/components/ui/SathLoader.vue'

const columns = [
  { key: 'nombreServ', label: 'Nombre' },
  { key: 'tipo', label: 'Tipo', thClass: 'text-center', tdClass: 'text-center' },
  { key: 'estado', label: 'Estado', thClass: 'text-center', tdClass: 'text-center' },
  { key: 'acciones', label: 'Acciones', sortable: false, thClass: 'text-center', tdClass: 'text-center' },
]

const auth = useAuthStore()
const { get, post, put, del } = useApi()
const { toast, confirm } = useToast()

const esAdmin = auth.isAdmin

const servicioList = ref([])
const tipos = ref([])
const textoBusqueda = ref('')
const filtroTipo = ref('')
const cargando = ref(false)
const guardando = ref(false)
const tipoVista = ref('list')

const drawerAbierto = ref(false)
const modoEdicion = ref(false)
const formulario = ref({ idServicio: null, nombreServ: '', idTipoServicio: null })

const servicioListFiltrada = computed(() => {
  const texto = textoBusqueda.value.toLowerCase().trim()
  return servicioList.value.filter((s) => {
    const coincideTexto = (s.nombreServ ?? '').toLowerCase().includes(texto)
    const coincideTipo = !filtroTipo.value || String(s.idTipoServicio) === String(filtroTipo.value)
    return coincideTexto && coincideTipo
  })
})

onMounted(() => Promise.all([cargarServicios(), cargarTipos()]))

async function cargarTipos() {
  try {
    const res = await get('/api/tipo-servicio/lista')
    if (res?.ok) tipos.value = await res.json()
  } catch {}
}

async function cargarServicios() {
  cargando.value = true
  try {
    const res = await get('/api/servicio/lista')
    if (!res || !res.ok) throw new Error('Error al cargar servicios')
    servicioList.value = await res.json()
  } catch (e) { toast(e.message, 'error') } finally { cargando.value = false }
}

function claseBadgeTipo(nombreTipo) {
  if (!nombreTipo) return 'bg-gray-100 text-gray-500 dark:bg-gray-700 dark:text-gray-300'
  const n = nombreTipo.toUpperCase()
  if (n.includes('PREVENTIVO')) return 'badge bg-info/10 text-info'
  if (n.includes('CORRECTIVO')) return 'badge bg-warning/10 text-warning'
  if (n.includes('TÉCNICO') || n.includes('TECNICO')) return 'badge bg-primary/10 text-primary'
  return 'badge bg-secondary/10 text-secondary'
}

function abrirCrear() {
  formulario.value = { idServicio: null, nombreServ: '', idTipoServicio: null }
  modoEdicion.value = false
  drawerAbierto.value = true
}

async function abrirEditar(id) {
  try {
    const res = await get(`/api/servicio/${id}`)
    if (!res || !res.ok) throw new Error('Servicio no encontrado')
    const datos = await res.json()
    formulario.value = { idServicio: datos.idServicio, nombreServ: datos.nombreServ, idTipoServicio: datos.idTipoServicio ?? null }
    modoEdicion.value = true
    drawerAbierto.value = true
  } catch (e) { toast(e.message, 'error') }
}

function cerrarDrawer() {
  drawerAbierto.value = false
  formulario.value = { idServicio: null, nombreServ: '', idTipoServicio: null }
  modoEdicion.value = false
}

async function guardarServicio() {
  if (!formulario.value.nombreServ.trim()) {
    toast('El nombre es requerido', 'error')
    return
  }
  guardando.value = true
  try {
    let res
    if (modoEdicion.value) {
      // Endpoint correcto del backend
      res = await put(`/api/servicio/editar/${formulario.value.idServicio}`, {
        nombreServ: formulario.value.nombreServ.trim(),
        idTipoServicio: formulario.value.idTipoServicio || null,
      })
      if (!res) return
      if (!res.ok) {
        const err = await res.json().catch(() => ({}))
        throw new Error(err.error || err.message || `Error ${res.status}`)
      }
      const actualizado = await res.json()
      const idx = servicioList.value.findIndex(s => s.idServicio === actualizado.idServicio)
      if (idx !== -1) {
        servicioList.value[idx].nombreServ = actualizado.nombreServ
        servicioList.value[idx].idTipoServicio = actualizado.idTipoServicio ?? null
        servicioList.value[idx].nombreTipoServicio = actualizado.nombreTipoServicio ?? null
      }
      toast('Servicio actualizado correctamente', 'success')
    } else {
      res = await post('/api/servicio/crear', { nombreServ: formulario.value.nombreServ.trim(), idTipoServicio: formulario.value.idTipoServicio || null })
      if (!res) return
      if (res.status === 201 || res.ok) {
        const nuevo = await res.json()
        servicioList.value.unshift({ idServicio: nuevo.idServicio, nombreServ: nuevo.nombreServ, estado: 'ACTIVO', idTipoServicio: nuevo.idTipoServicio ?? null, nombreTipoServicio: nuevo.nombreTipoServicio ?? null })
        toast('Servicio creado correctamente', 'success')
      } else {
        const err = await res.json().catch(() => ({}))
        throw new Error(err.error || err.message || `Error ${res.status}`)
      }
    }
    cerrarDrawer()
  } catch (e) { toast(e.message, 'error') } finally { guardando.value = false }
}

async function eliminarServicio(s) {
  const result = await confirm(`¿Eliminar "${s.nombreServ}"?`)
  if (!result.isConfirmed) return
  const idx = servicioList.value.findIndex(x => x.idServicio === s.idServicio)
  const backup = idx !== -1 ? { ...servicioList.value[idx] } : null
  if (idx !== -1) servicioList.value.splice(idx, 1)
  try {
    const res = await del(`/api/servicio/eliminar/${s.idServicio}`)
    if (!res) return
    if (res.status === 204 || res.ok) {
      toast('Servicio eliminado correctamente', 'success')
    } else {
      if (backup && idx !== -1) servicioList.value.splice(idx, 0, backup)
      const err = await res.json().catch(() => ({}))
      throw new Error(err.error || 'Error al eliminar')
    }
  } catch (e) {
    if (backup && idx !== -1) servicioList.value.splice(idx, 0, backup)
    toast(e.message, 'error')
  }
}
</script>

<template>
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Servicios</li>
  </ul>

  <div class="panel h-full w-full">
    <!-- Header -->
    <div class="flex items-center justify-between mb-5">
      <h5 class="text-xl font-bold dark:text-white-light/90">
        Servicios
        <span class="ml-2 badge bg-primary/10 text-primary py-1 px-3 rounded-full text-sm">
          {{ servicioListFiltrada.length }} registros
        </span>
      </h5>
    </div>

    <!-- Controles -->
    <div class="flex flex-wrap items-center gap-3 mb-5">
      <!-- Botón nuevo -->
      <button v-if="esAdmin" type="button" class="btn btn-success" @click="abrirCrear">
        <svg class="inline w-4 h-4 mr-1.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>
        </svg>
        Nuevo Servicio
      </button>

      <!-- Toggle lista/grid -->
      <button type="button" class="btn btn-outline-success p-2"
        :class="{ 'bg-success text-white': tipoVista === 'list' }"
        @click="tipoVista = 'list'" title="Vista lista">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" class="h-5 w-5">
          <path d="M2 5.5L3.21429 7L7.5 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path opacity="0.5" d="M2 12.5L3.21429 14L7.5 10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M2 19.5L3.21429 21L7.5 17" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M22 19L12 19" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <path opacity="0.5" d="M22 12L12 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <path d="M22 5L12 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
      </button>
      <button type="button" class="btn btn-outline-success p-2"
        :class="{ 'bg-success text-white': tipoVista === 'grid' }"
        @click="tipoVista = 'grid'" title="Vista grilla">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" class="h-5 w-5">
          <path opacity="0.5" d="M2.5 6.5C2.5 4.61438 2.5 3.67157 3.08579 3.08579C3.67157 2.5 4.61438 2.5 6.5 2.5C8.38562 2.5 9.32843 2.5 9.91421 3.08579C10.5 3.67157 10.5 4.61438 10.5 6.5C10.5 8.38562 10.5 9.32843 9.91421 9.91421C9.32843 10.5 8.38562 10.5 6.5 10.5C4.61438 10.5 3.67157 10.5 3.08579 9.91421C2.5 9.32843 2.5 8.38562 2.5 6.5Z" stroke="currentColor" stroke-width="1.5"/>
          <path opacity="0.5" d="M13.5 17.5C13.5 15.6144 13.5 14.6716 14.0858 14.0858C14.6716 13.5 15.6144 13.5 17.5 13.5C19.3856 13.5 20.3284 13.5 20.9142 14.0858C21.5 14.6716 21.5 15.6144 21.5 17.5C21.5 19.3856 21.5 20.3284 20.9142 20.9142C20.3284 21.5 19.3856 21.5 17.5 21.5C15.6144 21.5 14.6716 21.5 14.0858 20.9142C13.5 20.3284 13.5 19.3856 13.5 17.5Z" stroke="currentColor" stroke-width="1.5"/>
          <path d="M2.5 17.5C2.5 15.6144 2.5 14.6716 3.08579 14.0858C3.67157 13.5 4.61438 13.5 6.5 13.5C8.38562 13.5 9.32843 13.5 9.91421 14.0858C10.5 14.6716 10.5 15.6144 10.5 17.5C10.5 19.3856 10.5 20.3284 9.91421 20.9142C9.32843 21.5 8.38562 21.5 6.5 21.5C4.61438 21.5 3.67157 21.5 3.08579 20.9142C2.5 20.3284 2.5 19.3856 2.5 17.5Z" stroke="currentColor" stroke-width="1.5"/>
          <path d="M13.5 6.5C13.5 4.61438 13.5 3.67157 14.0858 3.08579C14.6716 2.5 15.6144 2.5 17.5 2.5C19.3856 2.5 20.3284 2.5 20.9142 3.08579C21.5 3.67157 21.5 4.61438 21.5 6.5C21.5 8.38562 21.5 9.32843 20.9142 9.91421C20.3284 10.5 19.3856 10.5 17.5 10.5C15.6144 10.5 14.6716 10.5 14.0858 9.91421C13.5 9.32843 13.5 8.38562 13.5 6.5Z" stroke="currentColor" stroke-width="1.5"/>
        </svg>
      </button>

      <!-- Filtro por tipo -->
      <div class="w-full sm:w-48">
        <SearchSelect
          v-model="filtroTipo"
          :options="tipos"
          label-key="nombreTipo"
          value-key="idTipoServicio"
          placeholder="Todos los tipos"
          empty-label="Todos los tipos"
          empty-value=""
        />
      </div>

      <!-- Buscador -->
      <div class="relative ml-auto">
        <input v-model="textoBusqueda" type="text" placeholder="Buscar servicio..."
          class="peer form-input py-2 ltr:pr-11 rtl:pl-11 w-full sm:w-56" />
        <div class="absolute top-1/2 -translate-y-1/2 peer-focus:text-primary ltr:right-[11px] rtl:left-[11px]">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5"/><path d="M18.5 18.5L22 22" stroke-linecap="round"/>
          </svg>
        </div>
      </div>
    </div>

    <!-- Loader Llamativo -->
    <SathLoader v-if="cargando && tipoVista === 'list'" size="md" />

    <template v-else-if="!cargando || tipoVista === 'grid'">
      <!-- ── VISTA LISTA ── -->
      <div v-if="tipoVista === 'list'">
        <DataTable
        :data="servicioListFiltrada"
        :columns="columns"
        :loading="cargando"
        default-sort="nombreServ"
        empty-text="No hay servicios registrados"
        striped
      >
        <template #cell-nombreServ="{ row }">
          <span class="font-semibold dark:text-white">{{ row.nombreServ }}</span>
        </template>
        <template #cell-tipo="{ row }">
          <span :class="claseBadgeTipo(row.nombreTipoServicio)">{{ row.nombreTipoServicio || '—' }}</span>
        </template>
        <template #cell-estado="{ row }">
          <span v-if="row.estado === 'ACTIVO'" class="badge bg-success/10 text-success">Activo</span>
          <span v-else class="badge bg-danger/10 text-danger">Eliminado</span>
        </template>
        <template #cell-acciones="{ row }">
          <div class="flex items-center justify-center gap-1">
            <button v-if="esAdmin" type="button" class="btn btn-xs btn-outline-warning px-2" title="Editar" @click="abrirEditar(row.idServicio)">
              <svg class="inline w-3 h-3 mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              Editar
            </button>
            <button v-if="esAdmin" type="button" class="btn btn-xs btn-outline-danger px-2" title="Eliminar" @click="eliminarServicio(row)">
              <svg class="inline w-3 h-3 mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
              </svg>
              Eliminar
            </button>
          </div>
        </template>
      </DataTable>
    </div>

    <!-- ── VISTA GRID ── -->
    <div v-if="tipoVista === 'grid'">
      <div v-if="cargando" class="my-5 grid w-full grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
        <div v-for="i in 8" :key="i" class="panel text-center flex flex-col items-center gap-3">
          <Skeleton width="56px" height="56px" circle />
          <Skeleton width="70%" height="1.4rem" />
          <Skeleton width="40%" height="1rem" />
          <div class="flex gap-2 w-full mt-4">
            <Skeleton width="50%" height="2.2rem" />
            <Skeleton width="50%" height="2.2rem" />
          </div>
        </div>
      </div>
      <div v-else class="my-5 grid w-full grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
        <div v-if="servicioListFiltrada.length === 0" class="text-center text-white-dark py-6 col-span-full">
          No hay servicios registrados
        </div>
        <div v-for="s in servicioListFiltrada" :key="s.idServicio" class="panel text-center">
          <div class="flex justify-center mb-4">
            <div class="grid h-14 w-14 place-content-center rounded-full bg-success/10 text-success text-xl font-bold">
              {{ s.nombreServ.charAt(0).toUpperCase() }}
            </div>
          </div>
          <div class="text-lg font-semibold dark:text-white">{{ s.nombreServ }}</div>
          <div class="mt-1.5">
            <span :class="claseBadgeTipo(s.nombreTipoServicio)">{{ s.nombreTipoServicio || '—' }}</span>
          </div>
          <div class="mt-1.5">
            <span v-if="s.estado === 'ACTIVO'" class="badge bg-success/10 text-success">Activo</span>
            <span v-else class="badge bg-danger/10 text-danger">Eliminado</span>
          </div>
          <div class="flex gap-3 mt-5 justify-center">
            <button v-if="esAdmin" type="button" class="btn btn-outline-warning flex-1" @click="abrirEditar(s.idServicio)">
              <svg class="inline w-3.5 h-3.5 mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              Editar
            </button>
            <button v-if="esAdmin" type="button" class="btn btn-outline-danger flex-1" @click="eliminarServicio(s)">
              <svg class="inline w-3.5 h-3.5 mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
              </svg>
              Eliminar
            </button>
          </div>
        </div>
      </div>
    </div>
  </template>
</div>

  <!-- ── MODAL CREAR / EDITAR ── -->
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="drawerAbierto" class="fixed inset-0 z-[999] overflow-y-auto bg-black/60" @click.self="cerrarDrawer">
        <div class="flex min-h-screen items-center justify-center px-4 py-8">
          <div class="panel rounded-lg border-0 p-0 overflow-hidden" style="width: 100%; max-width: 440px">

            <!-- Header -->
            <div class="flex items-center justify-between bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4 border-b border-[#e0e6ed] dark:border-[#1b2e4b]">
              <div class="flex items-center gap-3">
                <div class="grid h-9 w-9 shrink-0 place-content-center rounded-full"
                  :class="modoEdicion ? 'bg-warning/10 text-warning' : 'bg-success/10 text-success'">
                  <svg v-if="!modoEdicion" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="3"/>
                    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>
                  </svg>
                  <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                </div>
                <div>
                  <h3 class="text-base font-semibold">{{ modoEdicion ? 'Editar Servicio' : 'Nuevo Servicio' }}</h3>
                  <p class="text-xs text-white-dark">Complete los campos del formulario</p>
                </div>
              </div>
              <button type="button" class="text-white-dark hover:text-dark" @click="cerrarDrawer">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>

            <!-- Body -->
            <div class="p-5 space-y-4">
              <div>
                <label class="block text-xs font-semibold uppercase tracking-widest text-white-dark mb-2">
                  Tipo de Servicio
                </label>
                <SearchSelect
                  v-model="formulario.idTipoServicio"
                  :options="tipos"
                  label-key="nombreTipo"
                  value-key="idTipoServicio"
                  placeholder="-- Seleccione un tipo --"
                  empty-label="-- Sin tipo --"
                  :empty-value="null"
                />
              </div>
              <div>
                <label class="block text-xs font-semibold uppercase tracking-widest text-white-dark mb-2">
                  Nombre del Servicio <span class="text-danger">*</span>
                </label>
                <input v-model="formulario.nombreServ" type="text" class="form-input w-full"
                  placeholder="Ej: Mantenimiento preventivo" autocomplete="off"
                  @keydown.enter.prevent="guardarServicio" />
              </div>
              <div class="flex items-start gap-2 rounded-lg bg-success/5 dark:bg-success/10 px-4 py-3 text-xs text-white-dark">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="mt-0.5 flex-shrink-0 text-success">
                  <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                Los servicios quedan activos por defecto al crearse
              </div>
            </div>

            <!-- Footer -->
            <div class="flex justify-end gap-2 px-5 pb-5">
              <button type="button" class="btn btn-outline-danger" @click="cerrarDrawer">Cancelar</button>
              <button type="button" class="btn px-6"
                :class="modoEdicion ? 'btn-warning' : 'btn-success'"
                :disabled="guardando"
                @click="guardarServicio">
                <span v-if="guardando" class="animate-spin border-2 border-white border-l-transparent rounded-full w-3.5 h-3.5 inline-block mr-1.5 align-middle" />
                <svg v-else class="inline w-3.5 h-3.5 mr-1.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                {{ modoEdicion ? 'Actualizar' : 'Guardar' }}
              </button>
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
