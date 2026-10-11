<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import DataTable from '@/components/ui/DataTable.vue'
import Skeleton from '@/components/ui/Skeleton.vue'
import SathLoader from '@/components/ui/SathLoader.vue'

const columns = [
  { key: 'tecnico', label: 'Técnico' },
  { key: 'nroSolicitud', label: 'Nro. Solicitud', thClass: 'hidden sm:table-cell', tdClass: 'hidden sm:table-cell' },
  { key: 'fechaAsignacion', label: 'Fecha Asignación', thClass: 'hidden md:table-cell', tdClass: 'hidden md:table-cell' },
  { key: 'acciones', label: 'Acciones', sortable: false, thClass: 'text-center', tdClass: 'text-center' },
]

const auth = useAuthStore()
const { get, post, put, del } = useApi()
const { toast, confirm } = useToast()

const esAdmin = auth.isAdmin
const esTecnico = auth.isTecnico

const lista = ref([])
const usuarios = ref([])
const solicitudes = ref([])
const textoBusqueda = ref('')
const cargando = ref(false)
const guardando = ref(false)
const tipoVista = ref(typeof window !== 'undefined' && window.innerWidth < 768 ? 'grid' : 'list')

const modalAbierto = ref(false)
const modoEdicion = ref(false)
const form = ref({ idTecnicoAsignado: null, idUsuario: '', idsSolicitud: [] })

const tecBusqueda = ref('')
const tecLabel = ref('')
const tecAbierto = ref(false)
const solBusqueda = ref('')
const solAbierto = ref(false)
const cargandoCat = ref(false)

const listaFiltrada = computed(() => {
  const texto = textoBusqueda.value.toLowerCase().trim()
  return lista.value.filter((t) =>
    !texto ||
    (t.nombreCompleto ?? t.nombreTecnico ?? '').toLowerCase().includes(texto) ||
    (t.nroSolicitud ?? t.nroSol ?? '').toLowerCase().includes(texto),
  )
})

const tecFiltrados = computed(() => {
  const q = tecBusqueda.value.toLowerCase()
  const activos = usuarios.value.filter((u) => u.estado === 'ACTIVO')
  return !q ? activos : activos.filter((u) =>
    (u.nombrePersona ?? '').toLowerCase().includes(q) ||
    (u.username ?? '').toLowerCase().includes(q),
  )
})

const solFiltradas = computed(() => {
  const q = solBusqueda.value.toLowerCase().trim()
  if (!q) return solicitudes.value
  return solicitudes.value.filter((s) =>
    String(s.nroSol ?? '').includes(q) ||
    (s.descripcionSol ?? '').toLowerCase().includes(q) ||
    (s.idExterno ?? '').toLowerCase().includes(q),
  )
})

onMounted(async () => {
  cargando.value = true
  cargandoCat.value = true
  await Promise.all([cargar(), cargarUsuarios(), cargarSolicitudes()])
  cargando.value = false
  cargandoCat.value = false
})

async function cargar() {
  try {
    const res = await get('/api/tecnicoAsignado/listar')
    if (!res || !res.ok) return
    lista.value = await res.json()
  } catch {}
}

async function cargarUsuarios() {
  try {
    const res = await get('/api/usuario/listar')
    if (!res || !res.ok) return
    usuarios.value = await res.json()
  } catch {}
}

async function cargarSolicitudes() {
  try {
    const res = await get('/api/solicitud/listar')
    if (!res || !res.ok) return
    solicitudes.value = await res.json()
  } catch {}
}

function labelSolicitud(id) {
  const s = solicitudes.value.find(x => x.idSolicitud === id)
  return s ? ('Nro. ' + s.nroSol + (s.idExterno ? ' (ID ext.: ' + s.idExterno + ')' : '')) : 'ID ' + id
}

function seleccionarTecnico(u) {
  form.value.idUsuario = u.idUsuario
  tecLabel.value = (u.nombrePersona || u.username) + (u.cargo ? ` — ${u.cargo}` : '')
  tecBusqueda.value = tecLabel.value
  tecAbierto.value = false
}

function toggleSolicitud(s) {
  const id = s.idSolicitud
  const idx = form.value.idsSolicitud.indexOf(id)
  if (idx === -1) form.value.idsSolicitud.push(id)
  else form.value.idsSolicitud.splice(idx, 1)
}

function estaSeleccionada(idSolicitud) { return form.value.idsSolicitud.includes(idSolicitud) }
function quitarSolicitud(id) {
  const idx = form.value.idsSolicitud.indexOf(id)
  if (idx !== -1) form.value.idsSolicitud.splice(idx, 1)
}

function abrirCrear() {
  form.value = { idTecnicoAsignado: null, idUsuario: '', idsSolicitud: [] }
  tecBusqueda.value = ''
  tecLabel.value = ''
  solBusqueda.value = ''
  tecAbierto.value = false
  solAbierto.value = false
  modoEdicion.value = false
  modalAbierto.value = true
}

function abrirEditar(t) {
  form.value = {
    idTecnicoAsignado: t.idTecnicoAsignado,
    idUsuario: t.idUsuario,
    idsSolicitud: t.idsSolicitud || [],
  }
  const u = usuarios.value.find((x) => x.idUsuario === t.idUsuario)
  tecLabel.value = u ? (u.nombrePersona || u.username) : (t.nombreCompleto || t.nombreTecnico || '')
  tecBusqueda.value = tecLabel.value
  solBusqueda.value = ''
  tecAbierto.value = false
  solAbierto.value = false
  modoEdicion.value = true
  modalAbierto.value = true
}

function cerrarModal() { modalAbierto.value = false }

async function guardar() {
  if (!form.value.idUsuario || form.value.idsSolicitud.length === 0) {
    toast('Selecciona técnico y al menos una solicitud', 'error')
    return
  }
  guardando.value = true
  try {
    const body = { idUsuario: form.value.idUsuario, idsSolicitud: form.value.idsSolicitud }
    let res
    if (modoEdicion.value) {
      // @PutMapping("/editar/{id}")
      res = await put(`/api/tecnicoAsignado/editar/${form.value.idTecnicoAsignado}`, body)
    } else {
      // @PostMapping("/crear-masivo") acepta { idUsuario, idsSolicitud: [] }
      res = await post('/api/tecnicoAsignado/crear-masivo', body)
    }
    if (!res) return
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      throw new Error(err.message || `Error ${res.status}`)
    }
    toast(modoEdicion.value ? 'Actualizado' : 'Asignado', 'success')
    cerrarModal()
    await cargar()
  } catch (e) { toast(e.message, 'error') } finally { guardando.value = false }
}

async function eliminar(t) {
  const result = await confirm('¿Eliminar asignación?')
  if (!result.isConfirmed) return
  try {
    const res = await del(`/api/tecnicoAsignado/eliminar/${t.idTecnicoAsignado}`)
    if (!res || !res.ok) throw new Error('Error al eliminar')
    toast('Eliminado', 'success')
    await cargar()
  } catch (e) { toast(e.message, 'error') }
}

function inicial(nombre) { return (nombre || 'T').charAt(0).toUpperCase() }
</script>

<template>
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Técnicos Asignados</li>
  </ul>

  <div class="panel">
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
      <h5 class="text-xl font-bold dark:text-white-light/90">
        Técnicos Asignados
        <span class="ml-2 badge bg-primary/10 text-primary py-1 px-3 rounded-full text-sm">
          {{ listaFiltrada.length }} registros
        </span>
      </h5>
      <div class="flex w-full flex-col gap-3 sm:w-auto sm:flex-row sm:items-center">
        <button v-if="esAdmin || esTecnico" type="button" class="btn btn-primary" @click="abrirCrear">
          <svg class="inline w-4 h-4 mr-1.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" />
            <line x1="19" y1="8" x2="19" y2="14" /><line x1="22" y1="11" x2="16" y2="11" />
          </svg>
          Asignar Técnico
        </button>

        <!-- Toggle lista/grid -->
        <div class="flex items-center gap-1 border border-[#e0e6ed] dark:border-[#1b2e4b] rounded-lg p-1">
          <button type="button" @click="tipoVista = 'list'"
            class="p-1.5 rounded transition-colors"
            :class="tipoVista === 'list' ? 'bg-primary text-white' : 'text-white-dark hover:bg-[#f1f5ff] dark:hover:bg-[#1b2e4b]'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="8" y1="6" x2="21" y2="6" /><line x1="8" y1="12" x2="21" y2="12" />
              <line x1="8" y1="18" x2="21" y2="18" /><line x1="3" y1="6" x2="3.01" y2="6" />
              <line x1="3" y1="12" x2="3.01" y2="12" /><line x1="3" y1="18" x2="3.01" y2="18" />
            </svg>
          </button>
          <button type="button" @click="tipoVista = 'grid'"
            class="p-1.5 rounded transition-colors"
            :class="tipoVista === 'grid' ? 'bg-primary text-white' : 'text-white-dark hover:bg-[#f1f5ff] dark:hover:bg-[#1b2e4b]'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7" /><rect x="14" y="3" width="7" height="7" />
              <rect x="14" y="14" width="7" height="7" /><rect x="3" y="14" width="7" height="7" />
            </svg>
          </button>
        </div>

        <div class="relative">
          <input v-model="textoBusqueda" type="text" placeholder="Buscar..."
            class="peer form-input py-2 ltr:pr-11 rtl:pl-11 w-full sm:w-52" />
          <div class="absolute top-1/2 -translate-y-1/2 peer-focus:text-primary ltr:right-[11px] rtl:left-[11px]">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <circle cx="11.5" cy="11.5" r="9.5" stroke="currentColor" stroke-width="1.5" opacity="0.5" />
              <path d="M18.5 18.5L22 22" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- Loader Llamativo -->
    <SathLoader v-if="cargando && tipoVista === 'list'" size="md" />

    <template v-if="tipoVista === 'grid' || !cargando">
      <!-- Vista lista -->
      <div v-if="tipoVista === 'list'">
        <DataTable
          :data="listaFiltrada"
          :columns="columns"
          :loading="cargando"
          empty-text="No hay técnicos asignados"
          striped
        >
          <template #cell-tecnico="{ row }">
            <div class="flex items-center gap-2">
              <div class="grid h-8 w-8 shrink-0 place-content-center rounded-full bg-success/10 text-success text-xs font-bold">
                {{ inicial(row.nombreCompleto || row.nombreTecnico) }}
              </div>
              <div class="min-w-0">
                <div class="font-semibold truncate dark:text-white">{{ row.nombreCompleto || row.nombreTecnico || '—' }}</div>
                <div class="sm:hidden flex items-center gap-1 mt-0.5">
                  <span v-if="row.nroSolicitud || row.nroSol" class="badge bg-primary/10 text-primary text-[10px] font-semibold">
                    Nro. {{ row.nroSolicitud || row.nroSol }}
                  </span>
                  <span v-else class="text-[10px] text-white-dark">Sin solicitud</span>
                </div>
              </div>
            </div>
          </template>
          <template #cell-nroSolicitud="{ row }">
            <span v-if="row.nroSolicitud || row.nroSol" class="badge bg-primary/10 text-primary font-semibold">
              Nro. {{ row.nroSolicitud || row.nroSol }}
            </span>
            <span v-else class="text-white-dark">—</span>
          </template>
          <template #cell-fechaAsignacion="{ row }">
            <span class="text-white-dark text-sm">{{ row.fechaAsignacionTecAsig || row.fechaAsignacion || '—' }}</span>
          </template>
          <template #cell-acciones="{ row }">
            <div class="flex items-center justify-center gap-1">
              <button v-if="esAdmin || esTecnico" type="button" class="btn btn-xs btn-outline-warning px-2"
                title="Editar" @click="abrirEditar(row)">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                </svg>
              </button>
              <button v-if="esAdmin || esTecnico" type="button" class="btn btn-xs btn-outline-danger px-2"
                title="Eliminar" @click="eliminar(row)">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6" />
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
                </svg>
              </button>
            </div>
          </template>
        </DataTable>
      </div>

      <!-- Vista grid -->
      <div v-else>
        <div v-if="cargando" class="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-3">
          <div v-for="i in 6" :key="i" class="panel flex flex-col gap-3">
            <div class="flex flex-col items-center gap-3 pb-4 border-b border-[#e0e6ed] dark:border-[#1b2e4b]">
              <Skeleton width="64px" height="64px" circle />
              <Skeleton width="60%" height="1.2rem" />
              <Skeleton width="40%" height="1rem" />
            </div>
            <div class="flex gap-2 mt-1">
              <Skeleton width="50%" height="2rem" />
              <Skeleton width="50%" height="2rem" />
            </div>
          </div>
        </div>
        <template v-else>
          <div v-if="listaFiltrada.length === 0" class="py-10 text-center text-white-dark">No hay técnicos asignados</div>
          <div class="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-3">
            <div v-for="t in listaFiltrada" :key="t.idTecnicoAsignado" class="panel flex flex-col">
              <div class="flex flex-col items-center gap-3 pb-4 text-center border-b border-[#e0e6ed] dark:border-[#1b2e4b]">
                <div class="grid h-16 w-16 place-content-center rounded-full bg-success/10 text-success text-2xl font-bold mt-1">
                  {{ inicial(t.nombreCompleto || t.nombreTecnico) }}
                </div>
                <div>
                  <p class="font-bold text-base leading-tight">{{ t.nombreCompleto || t.nombreTecnico || '—' }}</p>
                  <span v-if="t.nroSolicitud || t.nroSol"
                    class="mt-1.5 inline-block badge bg-primary/10 text-primary">
                    Nro. {{ t.nroSolicitud || t.nroSol }}
                  </span>
                  <span v-else class="mt-1.5 inline-block text-xs text-white-dark">Sin solicitud</span>
                </div>
              </div>
              <div class="flex items-center gap-2 py-3 text-xs text-white-dark">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
                </svg>
                <span>{{ t.fechaAsignacionTecAsig || t.fechaAsignacion || 'Sin fecha' }}</span>
              </div>
              <div class="mt-auto flex gap-2 pt-1">
                <button v-if="esAdmin || esTecnico" type="button" class="btn btn-xs btn-outline-warning flex-1" @click="abrirEditar(t)">
                  <svg width="12" height="12" class="inline mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                  </svg>
                  Editar
                </button>
                <button v-if="esAdmin || esTecnico" type="button" class="btn btn-xs btn-outline-danger flex-1" @click="eliminar(t)">
                  <svg width="12" height="12" class="inline mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6" />
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
                  </svg>
                  Eliminar
                </button>
              </div>
            </div>
          </div>
        </template>
      </div>
    </template>
  </div>

  <!-- ── MODAL ASIGNAR / EDITAR TÉCNICO ── -->
  <Teleport to="body">
  <Transition name="fade">
    <div v-if="modalAbierto" class="fixed inset-0 z-[999] overflow-y-auto bg-black/60" @click.self="cerrarModal">
      <div class="flex min-h-screen items-center justify-center px-4 py-8">
        <div class="panel rounded-lg border-0 p-0" style="width: 100%; max-width: 680px">
          <!-- Header -->
          <div class="flex items-center justify-between bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4 rounded-t-lg border-b border-[#e0e6ed] dark:border-[#1b2e4b]">
            <div class="flex items-center gap-3">
              <div class="grid h-10 w-10 place-content-center rounded-xl"
                :class="modoEdicion ? 'bg-warning/10 text-warning' : 'bg-primary/10 text-primary'">
                <svg v-if="!modoEdicion" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" />
                  <line x1="19" y1="8" x2="19" y2="14" /><line x1="22" y1="11" x2="16" y2="11" />
                </svg>
                <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                </svg>
              </div>
              <div>
                <h3 class="text-base font-semibold">{{ modoEdicion ? 'Editar Asignación' : 'Asignar Técnico' }}</h3>
                <p class="text-xs text-white-dark">La fecha se registra automáticamente</p>
              </div>
            </div>
            <button type="button" @click="cerrarModal" class="text-white-dark hover:text-dark">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>

          <!-- Body -->
          <div class="p-5 space-y-5">
            <SathLoader v-if="cargandoCat" size="sm" />

            <template v-if="!cargandoCat">
              <!-- Técnico con autocomplete -->
              <div :style="{ position: 'relative', marginBottom: tecAbierto && tecFiltrados.length ? '220px' : '0' }">
                <label class="mb-2 block text-xs font-semibold uppercase tracking-widest text-white-dark">
                  Técnico <span class="text-danger">*</span>
                </label>
                <div class="relative">
                  <input type="text"
                    class="form-input w-full transition-all"
                    :class="form.idUsuario ? 'border-success/60 bg-success/5' : ''"
                    placeholder="Buscar por nombre"
                    v-model="tecBusqueda"
                    @focus="tecAbierto = true; solAbierto = false"
                    @input="tecAbierto = true; form.idUsuario = ''"
                    autocomplete="off" />
                  <span v-if="form.idUsuario"
                    class="absolute inset-y-0 right-3 flex items-center text-success pointer-events-none">
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                      <polyline points="20 6 9 17 4 12" />
                    </svg>
                  </span>
                  <div v-if="tecAbierto && tecFiltrados.length > 0" v-click-outside="() => (tecAbierto = false)"
                    class="absolute left-0 right-0 mt-1 max-h-52 overflow-y-auto rounded-lg border border-[#e0e6ed] dark:border-[#1b2e4b] bg-white dark:bg-[#1b2e4b] shadow-xl"
                    style="z-index: 9999">
                    <div v-for="u in tecFiltrados" :key="u.idUsuario"
                      class="flex items-center gap-3 px-4 py-2.5 cursor-pointer transition-colors border-b border-[#f1f5ff] dark:border-[#1b2e4b] last:border-0"
                      :class="form.idUsuario === u.idUsuario ? 'bg-primary/10 dark:bg-primary/20' : 'hover:bg-[#f8faff] dark:hover:bg-primary/10'"
                      @click="seleccionarTecnico(u)">
                      <div class="grid h-8 w-8 flex-shrink-0 place-content-center rounded-full bg-success/10 text-success text-xs font-bold">
                        {{ (u.nombrePersona || u.username || '?').charAt(0).toUpperCase() }}
                      </div>
                      <div class="min-w-0 flex-1">
                        <div class="text-sm font-semibold truncate dark:text-white">{{ u.nombrePersona || u.username }}</div>
                        <div class="text-xs text-white-dark truncate">{{ (u.cargo ? u.cargo + ' · ' : '') + '@' + u.username }}</div>
                      </div>
                      <svg v-if="form.idUsuario === u.idUsuario" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" class="text-primary flex-shrink-0">
                        <polyline points="20 6 9 17 4 12" />
                      </svg>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Solicitudes multi-select con chips -->
              <div :style="{ position: 'relative', marginBottom: solAbierto && solFiltradas.length ? '250px' : '0' }">
                <label class="mb-2 block text-xs font-semibold uppercase tracking-widest text-white-dark">
                  Solicitud(es) <span class="text-danger">*</span>
                  <span v-if="form.idsSolicitud.length > 0" class="normal-case tracking-normal font-normal ml-1 text-primary">
                    ({{ form.idsSolicitud.length }} seleccionadas)
                  </span>
                </label>

                <!-- Chips de seleccionadas -->
                <div v-if="form.idsSolicitud.length > 0" class="flex flex-wrap gap-1.5 mb-2">
                  <span v-for="id in form.idsSolicitud" :key="id"
                    class="inline-flex items-center gap-1 rounded-full bg-primary/10 text-primary text-xs font-semibold px-2.5 py-1">
                    <span>{{ labelSolicitud(id) }}</span>
                    <button type="button" class="hover:text-danger leading-none ml-0.5" @click="quitarSolicitud(id)">
                      <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                        <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
                      </svg>
                    </button>
                  </span>
                </div>

                <!-- Búsqueda -->
                <div class="relative">
                  <input type="text"
                    class="form-input w-full transition-all"
                    :class="form.idsSolicitud.length > 0 ? 'border-success/60' : ''"
                    placeholder="Buscar por Nro. o ID externo..."
                    v-model="solBusqueda"
                    @focus="solAbierto = true; tecAbierto = false"
                    @input="solAbierto = true"
                    autocomplete="off" />
                  <div v-if="solAbierto && solFiltradas.length > 0" v-click-outside="() => (solAbierto = false)"
                    class="absolute left-0 right-0 mt-1 max-h-56 overflow-y-auto rounded-lg border border-[#e0e6ed] dark:border-[#1b2e4b] bg-white dark:bg-[#1b2e4b] shadow-xl"
                    style="z-index: 9999">
                    <div v-for="s in solFiltradas" :key="s.idSolicitud"
                      class="flex items-center justify-between gap-3 px-4 py-2.5 cursor-pointer transition-colors border-b border-[#f1f5ff] dark:border-[#1b2e4b] last:border-0"
                      :class="estaSeleccionada(s.idSolicitud) ? 'bg-primary/10 dark:bg-primary/20' : 'hover:bg-[#f8faff] dark:hover:bg-primary/10'"
                      @click="toggleSolicitud(s)">
                      <div class="flex items-center gap-2">
                        <div class="grid h-5 w-5 flex-shrink-0 place-content-center rounded border-2 transition-colors"
                          :class="estaSeleccionada(s.idSolicitud) ? 'border-primary bg-primary text-white' : 'border-[#d0d5dd] dark:border-[#3b4357]'">
                          <svg v-if="estaSeleccionada(s.idSolicitud)" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3.5">
                            <polyline points="20 6 9 17 4 12" />
                          </svg>
                        </div>
                        <div>
                          <span class="font-semibold text-sm dark:text-white">Nro. {{ s.nroSol }}</span>
                          <span v-if="s.idExterno" class="ml-2 text-xs text-white-dark">· ID ext.: {{ s.idExterno }}</span>
                        </div>
                      </div>
                      <span class="badge text-xs font-medium flex-shrink-0"
                        :class="{
                          'bg-warning/10 text-warning': s.estadoSol === 'PENDIENTE',
                          'bg-info/10 text-info':       s.estadoSol === 'EN_MANTENIMIENTO',
                          'bg-success/10 text-success': s.estadoSol === 'FINALIZADO'
                        }">
                        {{ s.estadoSol === 'EN_MANTENIMIENTO' ? 'EN MANT.' : s.estadoSol }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Nota -->
              <div class="flex items-start gap-2 rounded-lg bg-primary/5 dark:bg-primary/10 px-4 py-3 text-xs text-white-dark">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="mt-0.5 flex-shrink-0 text-primary">
                  <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                Solo aparecen solicitudes con atenciones registradas
              </div>
            </template>
          </div>

          <!-- Footer — visualmente separado del cuerpo -->
          <div v-if="!cargandoCat" class="border-t border-[#e0e6ed] dark:border-[#1b2e4b] px-5 py-4 bg-[#fbfbfb] dark:bg-[#121c2c] rounded-b-lg">
            <div class="flex justify-end gap-2">
              <button type="button" class="btn btn-outline-danger" @click="cerrarModal">Cancelar</button>
              <button type="button"
                :class="modoEdicion ? 'btn btn-warning' : 'btn btn-primary'"
                :disabled="guardando"
                @click="guardar">
                <span v-if="guardando" class="animate-spin border-2 border-white border-l-transparent rounded-full w-3.5 h-3.5 inline-block mr-1.5 align-middle" />
                <svg v-else class="inline w-3.5 h-3.5 mr-1.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
                <span>{{ modoEdicion ? 'Actualizar' : (form.idsSolicitud.length > 1 ? 'Asignar ' + form.idsSolicitud.length + ' solicitudes' : 'Asignar') }}</span>
              </button>
            </div>
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
