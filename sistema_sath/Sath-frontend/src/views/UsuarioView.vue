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
  { key: 'username', label: 'Username' },
  { key: 'nombrePersona', label: 'Persona vinculada', thClass: 'hidden sm:table-cell', tdClass: 'hidden sm:table-cell' },
  { key: 'cargo', label: 'Cargo', thClass: 'hidden lg:table-cell', tdClass: 'hidden lg:table-cell' },
  { key: 'estado', label: 'Estado', thClass: 'text-center', tdClass: 'text-center' },
  { key: 'acciones', label: 'Acciones', sortable: false, thClass: 'text-center', tdClass: 'text-center' },
]

const auth = useAuthStore()
const { get, post, put, patch, del } = useApi()
const { toast, confirm } = useToast()

const esAdmin = auth.isAdmin

const lista = ref([])
const personas = ref([])
const roles = ref([])
const cargos = ref([])
const unidades = ref([])
const textoBusqueda = ref('')
const cargando = ref(false)
const guardando = ref(false)
const tipoVista = ref(typeof window !== 'undefined' && window.innerWidth < 768 ? 'grid' : 'list')
const cargandoCatalogos = ref(false)

// Drawer crear/editar
const drawerAbierto = ref(false)
const modoEdicion = ref(false)
const form = ref({ idUsuario: null, username: '', password: '', passwordConfirm: '', idPersona: '' })
const fc = ref({
  ciPer: '', nombrePer: '', apPaternoPer: '', apMaternoPer: '',
  nroCellPer: '', correoPer: '', username: '', password: '',
  idRol: '', codigoAdmi: '', idCargo: '', idUnidad: '',
})

const mostrarPass = ref(false)
const mostrarNuevaPass = ref(false)
const mostrarConfirmPass = ref(false)
const mostrarNuevoCargo = ref(false)
const nuevoCargo = ref('')
const guardandoCargo = ref(false)
const mostrarNuevaUnidad = ref(false)
const nuevaUnidad = ref('')
const guardandoUnidad = ref(false)

// Drawer roles
const drawerRoles = ref(false)
const usuSeleccionado = ref(null)
const usuarioRoles = ref([])
const todosRoles = ref([])
const rolIdAAsignar = ref('')
const cargandoRoles = ref(false)

const listaFiltrada = computed(() => {
  const texto = textoBusqueda.value.toLowerCase().trim()
  return lista.value.filter((u) =>
    !texto ||
    (u.username ?? '').toLowerCase().includes(texto) ||
    (u.nombrePersona ?? '').toLowerCase().includes(texto) ||
    (u.cargo ?? '').toLowerCase().includes(texto),
  )
})

const rolesDisponibles = computed(() => {
  const asignados = new Set(usuarioRoles.value.map(ur => ur.rolId))
  return todosRoles.value.filter(r => !asignados.has(r.idRol))
})

const personasOpciones = computed(() =>
  personas.value.map(p => ({
    idPersona: p.idPersona,
    label: p.nombreCompleto + (p.ciPer ? ` (${p.ciPer})` : ''),
  }))
)

onMounted(async () => {
  await Promise.all([cargar(), cargarCatalogos()])
})

async function cargar() {
  cargando.value = true
  try {
    const res = await get('/api/usuario/listar')
    if (!res?.ok) throw new Error('Error al cargar usuarios')
    lista.value = await res.json()
  } catch (e) { toast(e.message, 'error') } finally { cargando.value = false }
}

async function cargarCatalogos() {
  cargandoCatalogos.value = true
  const [rRes, caRes, unRes, pRes] = await Promise.all([
    get('/api/rol/listar'),
    get('/api/cargo/listar'),
    get('/api/unidad/listar'),
    get('/api/persona/listar'),
  ])
  if (rRes?.ok) roles.value = await rRes.json()
  if (caRes?.ok) cargos.value = await caRes.json()
  if (unRes?.ok) unidades.value = await unRes.json()
  if (pRes?.ok) personas.value = await pRes.json()
  cargandoCatalogos.value = false
}

function abrirCrear() {
  fc.value = { ciPer: '', nombrePer: '', apPaternoPer: '', apMaternoPer: '', nroCellPer: '', correoPer: '', username: '', password: '', idRol: '', codigoAdmi: '', idCargo: '', idUnidad: '' }
  mostrarNuevoCargo.value = false
  mostrarNuevaUnidad.value = false
  modoEdicion.value = false
  drawerAbierto.value = true
}

function abrirEditar(u) {
  form.value = { idUsuario: u.idUsuario, username: u.username, password: '', passwordConfirm: '', idPersona: u.idPersona || '' }
  modoEdicion.value = true
  drawerAbierto.value = true
}

function cerrarModal() { drawerAbierto.value = false }

async function guardar() {
  if (!modoEdicion.value && (!fc.value.password || fc.value.password.length < 6)) {
    toast('La contraseña debe tener al menos 6 caracteres', 'error')
    return
  }
  guardando.value = true
  try {
    if (modoEdicion.value) {
      if (form.value.password && form.value.password !== form.value.passwordConfirm) {
        toast('Las contraseñas no coinciden', 'error')
        return
      }
      const body = { username: form.value.username }
      if (form.value.password) body.password = form.value.password
      if (form.value.idPersona) body.idPersona = form.value.idPersona
      const res = await put(`/api/usuario/actualizar/${form.value.idUsuario}`, body)
      if (!res) return
      if (!res.ok) {
        const err = await res.json().catch(() => ({}))
        throw new Error(err.message || `Error ${res.status}`)
      }
      toast('Usuario actualizado', 'success')
    } else {
      const res = await post('/api/usuario/crear-completo', fc.value)
      if (!res) return
      if (!res.ok) {
        const err = await res.json().catch(() => ({}))
        throw new Error(err.message || `Error ${res.status}`)
      }
      toast('Usuario creado', 'success')
    }
    cerrarModal()
    await cargar()
  } catch (e) { toast(e.message, 'error') } finally { guardando.value = false }
}

async function toggleEstado(u) {
  try {
    const res = await patch(`/api/usuario/toggle-estado/${u.idUsuario}`, {})
    if (!res?.ok) throw new Error('Error al cambiar estado')
    const data = await res.json()
    u.estado = data.estado ?? (u.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO')
    toast(u.estado === 'ACTIVO' ? 'Usuario activado' : 'Usuario desactivado', 'success')
  } catch (e) { toast(e.message, 'error') }
}

async function crearCargo() {
  if (!nuevoCargo.value.trim()) return
  guardandoCargo.value = true
  try {
    const res = await post('/api/cargo/crear', { nombreCar: nuevoCargo.value.trim() })
    if (!res?.ok) throw new Error('Error al crear cargo')
    const nuevo = await res.json()
    cargos.value.push(nuevo)
    fc.value.idCargo = nuevo.idCargo
    nuevoCargo.value = ''
    mostrarNuevoCargo.value = false
    toast('Cargo creado', 'success')
  } catch (e) { toast(e.message, 'error') } finally { guardandoCargo.value = false }
}

async function crearUnidad() {
  if (!nuevaUnidad.value.trim()) return
  guardandoUnidad.value = true
  try {
    const res = await post('/api/unidad/crear', { nombreUni: nuevaUnidad.value.trim() })
    if (!res?.ok) throw new Error('Error al crear unidad')
    const nueva = await res.json()
    unidades.value.push(nueva)
    fc.value.idUnidad = nueva.idUnidad
    nuevaUnidad.value = ''
    mostrarNuevaUnidad.value = false
    toast('Unidad creada', 'success')
  } catch (e) { toast(e.message, 'error') } finally { guardandoUnidad.value = false }
}

async function abrirGestionRoles(u) {
  usuSeleccionado.value = u
  drawerRoles.value = true
  cargandoRoles.value = true
  rolIdAAsignar.value = ''
  try {
    const [rRes, urRes] = await Promise.all([
      get('/api/rol/listar'),
      get(`/api/usr-rol/roles/${u.idUsuario}`),
    ])
    if (rRes?.ok) todosRoles.value = await rRes.json()
    if (urRes?.ok) usuarioRoles.value = await urRes.json()
  } catch {} finally { cargandoRoles.value = false }
}

function cerrarRoles() { drawerRoles.value = false }

function rolNombre(rolId) {
  return todosRoles.value.find(r => r.idRol === rolId)?.nombreRol ?? rolId
}

async function asignarRol() {
  if (!rolIdAAsignar.value) return
  try {
    const res = await post('/api/usr-rol/asignar', {
      idUsuario: usuSeleccionado.value.idUsuario,
      idRol: rolIdAAsignar.value,
    })
    if (!res?.ok) {
      const err = await res?.json().catch(() => ({}))
      throw new Error(err?.message || 'Error al asignar rol')
    }
    rolIdAAsignar.value = ''
    await abrirGestionRoles(usuSeleccionado.value)
    toast('Rol asignado', 'success')
  } catch (e) { toast(e.message, 'error') }
}

async function revocarRol(idUsrRol, nombreRol) {
  const result = await confirm(`¿Revocar el rol "${nombreRol}"?`)
  if (!result.isConfirmed) return
  try {
    const res = await del(`/api/usr-rol/revocar/${idUsrRol}`)
    if (!res?.ok) throw new Error('Error al revocar rol')
    usuarioRoles.value = usuarioRoles.value.filter(ur => ur.idUsrRol !== idUsrRol)
    toast('Rol revocado', 'success')
  } catch (e) { toast(e.message, 'error') }
}

function inicial(nombre) { return (nombre || '?').charAt(0).toUpperCase() }
</script>

<template>
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Usuarios</li>
  </ul>

  <div class="panel">
    <!-- Header -->
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
      <h5 class="text-xl font-bold dark:text-white-light/90">
        Usuarios del Sistema
        <span class="ml-2 badge bg-primary/10 text-primary py-1 px-3 rounded-full text-sm">
          {{ listaFiltrada.length }} registros
        </span>
      </h5>
      <div class="flex w-full flex-col gap-3 sm:w-auto sm:flex-row sm:items-center">
        <button type="button" class="btn btn-primary" @click="abrirCrear">
          <svg class="inline w-4 h-4 mr-1.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" />
            <line x1="19" y1="8" x2="19" y2="14" /><line x1="22" y1="11" x2="16" y2="11" />
          </svg>
          Nuevo Usuario
        </button>

        <!-- Toggle lista/grid -->
        <div class="flex gap-1">
          <button type="button" class="btn btn-outline-primary p-2"
            :class="{ 'bg-primary text-white': tipoVista === 'list' }"
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
          <button type="button" class="btn btn-outline-primary p-2"
            :class="{ 'bg-primary text-white': tipoVista === 'grid' }"
            @click="tipoVista = 'grid'" title="Vista tarjetas">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" class="h-5 w-5">
              <path opacity="0.5" d="M2.5 6.5C2.5 4.61438 2.5 3.67157 3.08579 3.08579C3.67157 2.5 4.61438 2.5 6.5 2.5C8.38562 2.5 9.32843 2.5 9.91421 3.08579C10.5 3.67157 10.5 4.61438 10.5 6.5C10.5 8.38562 10.5 9.32843 9.91421 9.91421C9.32843 10.5 8.38562 10.5 6.5 10.5C4.61438 10.5 3.67157 10.5 3.08579 9.91421C2.5 9.32843 2.5 8.38562 2.5 6.5Z" stroke="currentColor" stroke-width="1.5"/>
              <path d="M2.5 17.5C2.5 15.6144 2.5 14.6716 3.08579 14.0858C3.67157 13.5 4.61438 13.5 6.5 13.5C8.38562 13.5 9.32843 13.5 9.91421 14.0858C10.5 14.6716 10.5 15.6144 10.5 17.5C10.5 19.3856 10.5 20.3284 9.91421 20.9142C9.32843 21.5 8.38562 21.5 6.5 21.5C4.61438 21.5 3.67157 21.5 3.08579 20.9142C2.5 20.3284 2.5 19.3856 2.5 17.5Z" stroke="currentColor" stroke-width="1.5"/>
              <path d="M13.5 6.5C13.5 4.61438 13.5 3.67157 14.0858 3.08579C14.6716 2.5 15.6144 2.5 17.5 2.5C19.3856 2.5 20.3284 2.5 20.9142 3.08579C21.5 3.67157 21.5 4.61438 21.5 6.5C21.5 8.38562 21.5 9.32843 20.9142 9.91421C20.3284 10.5 19.3856 10.5 17.5 10.5C15.6144 10.5 14.6716 10.5 14.0858 9.91421C13.5 9.32843 13.5 8.38562 13.5 6.5Z" stroke="currentColor" stroke-width="1.5"/>
              <path opacity="0.5" d="M13.5 17.5C13.5 15.6144 13.5 14.6716 14.0858 14.0858C14.6716 13.5 15.6144 13.5 17.5 13.5C19.3856 13.5 20.3284 13.5 20.9142 14.0858C21.5 14.6716 21.5 15.6144 21.5 17.5C21.5 19.3856 21.5 20.3284 20.9142 20.9142C20.3284 21.5 19.3856 21.5 17.5 21.5C15.6144 21.5 14.6716 21.5 14.0858 20.9142C13.5 20.3284 13.5 19.3856 13.5 17.5Z" stroke="currentColor" stroke-width="1.5"/>
            </svg>
          </button>
        </div>

        <div class="relative">
          <input v-model="textoBusqueda" type="text" placeholder="Buscar..."
            class="peer form-input py-2 ltr:pr-11 rtl:pl-11 w-full sm:w-48" />
          <div class="absolute top-1/2 -translate-y-1/2 peer-focus:text-primary ltr:right-[11px] rtl:left-[11px]">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <circle cx="11.5" cy="11.5" r="9.5" stroke="currentColor" stroke-width="1.5" opacity="0.5"/>
              <path d="M18.5 18.5L22 22" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- Loader Llamativo -->
    <SathLoader v-if="cargando && tipoVista === 'list'" size="md" />

    <template v-else-if="!cargando || tipoVista === 'grid'">
      <!-- ── VISTA LISTA ── -->
      <div v-if="tipoVista === 'list'">
        <DataTable
        :data="listaFiltrada"
        :columns="columns"
        :loading="cargando"
        default-sort="username"
        empty-text="No hay usuarios registrados"
        striped
      >
        <template #cell-username="{ row }">
          <div class="flex items-center gap-2">
            <div class="grid h-8 w-8 shrink-0 place-content-center rounded-full bg-primary/10 text-primary text-xs font-bold">
              {{ inicial(row.username) }}
            </div>
            <div class="min-w-0">
              <div class="truncate font-semibold">{{ row.username }}</div>
              <div class="sm:hidden text-xs text-white-dark truncate">{{ row.nombrePersona || '—' }}</div>
            </div>
          </div>
        </template>
        <template #cell-nombrePersona="{ row }">
          <span class="text-white-dark">{{ row.nombrePersona || '—' }}</span>
        </template>
        <template #cell-cargo="{ row }">
          <span class="text-white-dark text-xs">{{ row.cargo || '—' }}</span>
        </template>
        <template #cell-estado="{ row }">
          <span v-if="row.estado === 'ACTIVO'" class="badge bg-success/10 text-success">Activo</span>
          <span v-else class="badge bg-danger/10 text-danger">Inactivo</span>
        </template>
        <template #cell-acciones="{ row }">
          <div class="flex items-center justify-center gap-1">
            <button type="button" class="btn btn-xs btn-outline-primary px-2" title="Gestionar roles" @click="abrirGestionRoles(row)">
              <svg class="inline w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              </svg>
              <span class="hidden sm:inline ml-1">Roles</span>
            </button>
            <button type="button" class="btn btn-xs btn-outline-warning px-2" title="Editar usuario" @click="abrirEditar(row)">
              <svg class="inline w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              <span class="hidden sm:inline ml-1">Editar</span>
            </button>
            <button type="button" class="btn btn-xs px-2"
              :class="row.estado === 'ACTIVO' ? 'btn-outline-secondary' : 'btn-outline-success'"
              :title="row.estado === 'ACTIVO' ? 'Desactivar' : 'Activar'"
              @click="toggleEstado(row)">
              <svg v-if="row.estado === 'ACTIVO'" class="inline w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
              </svg>
              <svg v-else class="inline w-3 h-3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              <span class="hidden sm:inline ml-1">{{ row.estado === 'ACTIVO' ? 'Desactivar' : 'Activar' }}</span>
            </button>
          </div>
        </template>
      </DataTable>
    </div>

    <!-- ── VISTA GRID ── -->
    <div v-if="tipoVista === 'grid'">
      <div v-if="cargando" class="mt-2 grid w-full grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
        <div v-for="i in 8" :key="i" class="panel flex flex-col items-center text-center gap-3">
          <Skeleton width="64px" height="64px" circle />
          <Skeleton width="60%" height="1.2rem" />
          <Skeleton width="40%" height="0.8rem" />
          <div class="flex gap-2 w-full mt-4">
            <Skeleton width="50%" height="2rem" />
            <Skeleton width="50%" height="2rem" />
          </div>
        </div>
      </div>
      <div v-else class="mt-2 grid w-full grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
        <div v-if="listaFiltrada.length === 0" class="col-span-full text-center text-white-dark py-10">
          No hay usuarios registrados
        </div>
        <div v-for="u in listaFiltrada" :key="u.idUsuario" class="panel flex flex-col items-center text-center">
          <div class="relative mb-4">
            <div class="grid h-16 w-16 place-content-center rounded-full bg-primary/10 text-primary text-2xl font-bold">
              {{ inicial(u.username) }}
            </div>
            <span class="absolute bottom-0.5 right-0.5 h-3.5 w-3.5 rounded-full border-2 border-white dark:border-[#0e1726]"
              :class="u.estado === 'ACTIVO' ? 'bg-success' : 'bg-danger'" />
          </div>
          <div class="font-bold text-base dark:text-white">{{ u.username }}</div>
          <div class="mt-0.5 text-xs text-white-dark truncate w-full px-2">{{ u.nombrePersona || 'Sin persona vinculada' }}</div>
          <div class="mt-1.5 flex items-center justify-center gap-1.5">
            <span v-if="u.cargo" class="badge bg-primary/10 text-primary text-xs">{{ u.cargo }}</span>
            <span v-if="u.estado === 'ACTIVO'" class="badge bg-success/10 text-success text-xs">Activo</span>
            <span v-else class="badge bg-danger/10 text-danger text-xs">Inactivo</span>
          </div>
          <div class="mt-5 w-full grid grid-cols-2 gap-2">
            <button type="button" class="btn btn-sm btn-outline-primary" @click="abrirGestionRoles(u)">
              <svg class="inline w-3.5 h-3.5 mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              </svg>
              Roles
            </button>
            <button type="button" class="btn btn-sm btn-outline-warning" @click="abrirEditar(u)">
              <svg class="inline w-3.5 h-3.5 mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              Editar
            </button>
            <button type="button" class="btn btn-sm col-span-2"
              :class="u.estado === 'ACTIVO' ? 'btn-outline-secondary' : 'btn-outline-success'"
              @click="toggleEstado(u)">
              <svg v-if="u.estado === 'ACTIVO'" class="inline w-3.5 h-3.5 mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
              </svg>
              <svg v-else class="inline w-3.5 h-3.5 mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              {{ u.estado === 'ACTIVO' ? 'Desactivar usuario' : 'Activar usuario' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    </template>
  </div>

  <!-- ── DRAWER GESTIÓN DE ROLES ── -->
  <Teleport to="body">
  <Transition name="overlay">
    <div v-if="drawerRoles" class="fixed inset-0 z-[998] bg-black/60" @click="cerrarRoles" />
  </Transition>
  <Transition name="drawer">
    <div v-if="drawerRoles"
      class="fixed inset-y-0 ltr:right-0 rtl:left-0 z-[999] flex flex-col bg-white dark:bg-[#0e1726] shadow-2xl"
      style="width: 100%; max-width: 400px">

      <div class="h-1 w-full flex-shrink-0 bg-info" />

      <div class="flex items-center justify-between px-6 py-5 border-b border-[#e0e6ed] dark:border-[#1b2e4b] flex-shrink-0 bg-[#fbfbfb] dark:bg-[#121c2c]">
        <div class="flex items-center gap-3">
          <div class="grid h-11 w-11 place-content-center rounded-xl bg-info/10 text-info">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
          </div>
          <div>
            <h5 class="font-bold text-base dark:text-white-light">Gestión de Roles</h5>
            <p class="text-xs text-info mt-0.5">{{ usuSeleccionado ? '@' + usuSeleccionado.username : '' }}</p>
          </div>
        </div>
        <button type="button"
          class="grid h-8 w-8 place-content-center rounded-lg text-white-dark hover:bg-[#f1f5ff] dark:hover:bg-[#1b2e4b] transition-colors"
          @click="cerrarRoles">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>

      <div class="flex-1 overflow-y-auto px-6 py-6 space-y-5">
        <div v-if="cargandoRoles" class="flex justify-center py-10">
          <span class="animate-spin border-4 border-info border-l-transparent rounded-full w-8 h-8 inline-block" />
        </div>

        <template v-if="!cargandoRoles">
          <!-- Roles asignados -->
          <div>
            <p class="text-xs font-semibold uppercase tracking-widest text-white-dark mb-3">Roles asignados</p>
            <div v-if="usuarioRoles.length === 0" class="text-sm text-white-dark italic">Sin roles asignados</div>
            <div class="space-y-2">
              <div v-for="ur in usuarioRoles" :key="ur.idUsrRol"
                class="flex items-center justify-between rounded-lg bg-primary/5 px-3 py-2.5">
                <span class="flex items-center gap-2">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-primary">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                  </svg>
                  <span class="badge bg-primary text-white text-xs">{{ rolNombre(ur.rolId) }}</span>
                </span>
                <button type="button" class="btn btn-xs btn-outline-danger" @click="revocarRol(ur.idUsrRol, rolNombre(ur.rolId))">
                  <svg class="inline w-3 h-3 mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                  </svg>
                  Revocar
                </button>
              </div>
            </div>
          </div>

          <!-- Asignar nuevo rol -->
          <div class="border-t border-dashed border-[#e0e6ed] dark:border-[#1b2e4b] pt-4">
            <p class="text-xs font-semibold uppercase tracking-widest text-white-dark mb-3">Asignar rol</p>
            <div v-if="rolesDisponibles.length === 0" class="text-sm text-white-dark italic">Todos los roles ya están asignados</div>
            <div v-else class="flex gap-2">
              <SearchSelect
                v-model="rolIdAAsignar"
                :options="rolesDisponibles"
                label-key="nombreRol"
                value-key="idRol"
                placeholder="— Seleccionar rol —"
                empty-label="— Seleccionar rol —"
                empty-value=""
                class="flex-1"
              />
              <button type="button" class="btn btn-primary" :disabled="!rolIdAAsignar" @click="asignarRol">
                <svg class="inline w-3.5 h-3.5 mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 5v14M5 12h14"/>
                </svg>
                Asignar
              </button>
            </div>
          </div>
        </template>
      </div>

      <div class="flex-shrink-0 border-t border-[#e0e6ed] dark:border-[#1b2e4b] px-6 py-4 bg-[#fbfbfb] dark:bg-[#121c2c]">
        <div class="flex justify-end">
          <button type="button" class="btn btn-outline-secondary" @click="cerrarRoles">Cerrar</button>
        </div>
      </div>
    </div>
  </Transition>
  </Teleport>

  <!-- ── DRAWER CREAR / EDITAR USUARIO ── -->
  <Teleport to="body">
  <Transition name="overlay">
    <div v-if="drawerAbierto" class="fixed inset-0 z-[998] bg-black/60" @click="cerrarModal" />
  </Transition>
  <Transition name="drawer">
    <div v-if="drawerAbierto"
      class="fixed inset-y-0 ltr:right-0 rtl:left-0 z-[999] flex flex-col bg-white dark:bg-[#0e1726] shadow-2xl"
      style="width: 100%; max-width: 680px">

      <div class="h-1 w-full flex-shrink-0" :class="modoEdicion ? 'bg-warning' : 'bg-primary'" />

      <div class="flex items-center justify-between px-6 py-5 border-b border-[#e0e6ed] dark:border-[#1b2e4b] flex-shrink-0 bg-[#fbfbfb] dark:bg-[#121c2c]">
        <div class="flex items-center gap-3">
          <div class="grid h-11 w-11 place-content-center rounded-xl"
            :class="modoEdicion ? 'bg-warning/10 text-warning' : 'bg-primary/10 text-primary'">
            <svg v-if="!modoEdicion" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
              <line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/>
            </svg>
            <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
            </svg>
          </div>
          <div>
            <h5 class="font-bold text-base dark:text-white-light">{{ modoEdicion ? 'Editar Usuario' : 'Nuevo Usuario' }}</h5>
            <p class="text-xs text-white-dark mt-0.5">{{ modoEdicion ? 'Modifica los datos del usuario' : 'Completa todos los campos requeridos' }}</p>
          </div>
        </div>
        <button type="button"
          class="grid h-8 w-8 place-content-center rounded-lg text-white-dark hover:bg-[#f1f5ff] dark:hover:bg-[#1b2e4b] transition-colors"
          @click="cerrarModal">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>

      <div class="flex-1 overflow-y-auto px-6 py-6">

        <!-- ── MODO CREAR ── -->
        <template v-if="!modoEdicion">
          <form id="drawerFormUsuario" @submit.prevent="guardar">
            <!-- Datos personales -->
            <div class="mb-6">
              <h6 class="mb-4 border-b border-primary/20 pb-2 text-xs font-semibold uppercase tracking-widest text-primary">
                Datos Personales
              </h6>
              <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label class="form-label font-medium">CI <span class="text-danger">*</span></label>
                  <input v-model="fc.ciPer" type="text" class="form-input" placeholder="Ej: 12345678" required />
                </div>
                <div>
                  <label class="form-label font-medium">Nombres <span class="text-danger">*</span></label>
                  <input v-model="fc.nombrePer" type="text" class="form-input" placeholder="Ej: Juan Carlos" required />
                </div>
                <div>
                  <label class="form-label font-medium">Ap. Paterno <span class="text-danger">*</span></label>
                  <input v-model="fc.apPaternoPer" type="text" class="form-input" placeholder="Ej: Pérez" required />
                </div>
                <div>
                  <label class="form-label font-medium">Ap. Materno <span class="text-danger">*</span></label>
                  <input v-model="fc.apMaternoPer" type="text" class="form-input" placeholder="Ej: López" required />
                </div>
                <div>
                  <label class="form-label font-medium">Celular</label>
                  <input v-model="fc.nroCellPer" type="text" class="form-input" placeholder="Ej: 70000000" />
                </div>
                <div>
                  <label class="form-label font-medium">Correo</label>
                  <input v-model="fc.correoPer" type="email" class="form-input" placeholder="correo@ejemplo.com" />
                </div>
              </div>
            </div>

            <!-- Cuenta de acceso -->
            <div class="mb-6">
              <h6 class="mb-4 border-b border-primary/20 pb-2 text-xs font-semibold uppercase tracking-widest text-primary">
                Cuenta de Acceso
              </h6>
              <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label class="form-label font-medium">Username <span class="text-danger">*</span></label>
                  <input v-model="fc.username" type="text" class="form-input" placeholder="Ej: jperez" autocomplete="off" required />
                </div>
                <div>
                  <label class="form-label font-medium">Contraseña <span class="text-danger">*</span></label>
                  <div class="relative">
                    <input v-model="fc.password" :type="mostrarPass ? 'text' : 'password'" class="form-input pr-10" placeholder="Mínimo 6 caracteres"
                           autocomplete="new-password" required />
                    <button type="button" class="absolute inset-y-0 right-0 flex items-center pr-3 text-white-dark hover:text-primary transition-colors" @click="mostrarPass = !mostrarPass">
                      <svg v-if="!mostrarPass" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                      </svg>
                      <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                        <line x1="1" y1="1" x2="23" y2="23"/>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Puesto (opcional) -->
            <div class="mb-6">
              <h6 class="mb-4 border-b border-primary/20 pb-2 text-xs font-semibold uppercase tracking-widest text-primary">
                Puesto en el Sistema
              </h6>
              <div class="space-y-4">
                <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                  <div>
                    <label class="form-label font-medium">Código Adm.<span class="text-danger">*</span></label>
                    <input v-model="fc.codigoAdmi" type="number" class="form-input no-spinner" placeholder="Ej: 1001" />
                  </div>
                  <div>
                    <label class="form-label font-medium">Rol <span class="text-danger">*</span></label>
                    <div v-if="cargandoCatalogos" class="py-1 text-sm text-white-dark">Cargando roles...</div>
                    <SearchSelect
                      v-else
                      v-model="fc.idRol"
                      :options="roles"
                      label-key="nombreRol"
                      value-key="idRol"
                      placeholder="— Seleccionar rol —"
                      empty-label="— Seleccionar rol —"
                      empty-value=""
                    />
                  </div>
                </div>
                <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                  <div>
                    <div class="mb-1 flex items-center justify-between">
                      <label class="form-label mb-0 font-medium">Cargo</label>
                      <button type="button"
                        class="flex h-5 w-5 items-center justify-center rounded-full bg-primary/10 text-primary hover:bg-primary hover:text-white transition-colors"
                        @click="mostrarNuevoCargo = !mostrarNuevoCargo; nuevoCargo = ''">
                        <svg v-if="!mostrarNuevoCargo" width="10" height="10" viewBox="0 0 24 24" fill="none">
                          <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                        </svg>
                        <svg v-else width="10" height="10" viewBox="0 0 24 24" fill="none">
                          <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                        </svg>
                      </button>
                    </div>
                    <SearchSelect
                      v-if="!mostrarNuevoCargo"
                      v-model="fc.idCargo"
                      :options="cargos"
                      label-key="nombreCar"
                      value-key="idCargo"
                      placeholder="— Sin cargo —"
                      empty-label="— Sin cargo —"
                      empty-value=""
                    />
                    <div v-else class="flex gap-1.5">
                      <input v-model="nuevoCargo" type="text" class="form-input flex-1 py-1.5 text-sm" placeholder="Nombre del cargo"
                        @keydown.enter.prevent="crearCargo" @keydown.escape="mostrarNuevoCargo = false; nuevoCargo = ''" />
                      <button type="button" class="btn btn-sm btn-primary px-2.5"
                        :disabled="guardandoCargo || !nuevoCargo.trim()" @click="crearCargo">
                        <span v-if="guardandoCargo" class="inline-block h-3 w-3 animate-spin rounded-full border-2 border-white border-l-transparent" />
                        <span v-else>Crear</span>
                      </button>
                    </div>
                  </div>
                  <div>
                    <div class="mb-1 flex items-center justify-between">
                      <label class="form-label mb-0 font-medium">Unidad</label>
                      <button type="button"
                        class="flex h-5 w-5 items-center justify-center rounded-full bg-primary/10 text-primary hover:bg-primary hover:text-white transition-colors"
                        @click="mostrarNuevaUnidad = !mostrarNuevaUnidad; nuevaUnidad = ''">
                        <svg v-if="!mostrarNuevaUnidad" width="10" height="10" viewBox="0 0 24 24" fill="none">
                          <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                        </svg>
                        <svg v-else width="10" height="10" viewBox="0 0 24 24" fill="none">
                          <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                        </svg>
                      </button>
                    </div>
                    <SearchSelect
                      v-if="!mostrarNuevaUnidad"
                      v-model="fc.idUnidad"
                      :options="unidades"
                      label-key="nombreUni"
                      value-key="idUnidad"
                      placeholder="— Sin unidad —"
                      empty-label="— Sin unidad —"
                      empty-value=""
                    />
                    <div v-else class="flex gap-1.5">
                      <input v-model="nuevaUnidad" type="text" class="form-input flex-1 py-1.5 text-sm" placeholder="Nombre de la unidad"
                        @keydown.enter.prevent="crearUnidad" @keydown.escape="mostrarNuevaUnidad = false; nuevaUnidad = ''" />
                      <button type="button" class="btn btn-sm btn-primary px-2.5"
                        :disabled="guardandoUnidad || !nuevaUnidad.trim()" @click="crearUnidad">
                        <span v-if="guardandoUnidad" class="inline-block h-3 w-3 animate-spin rounded-full border-2 border-white border-l-transparent" />
                        <span v-else>Crear</span>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </form>
        </template>

        <!-- ── MODO EDITAR ── -->
        <template v-else>
          <form id="drawerFormUsuario" @submit.prevent="guardar" class="space-y-4">
            <div>
              <label class="block text-xs font-semibold uppercase tracking-widest text-white-dark mb-2">
                Username <span class="text-danger">*</span>
              </label>
              <input v-model="form.username" type="text" class="form-input w-full" placeholder="Ej: jperez" autocomplete="off" required />
            </div>
            <div>
              <label class="block text-xs font-semibold uppercase tracking-widest text-white-dark mb-2">Persona vinculada</label>
              <div v-if="cargandoCatalogos" class="py-1 text-sm text-white-dark">Cargando...</div>
              <SearchSelect
                v-else
                v-model="form.idPersona"
                :options="personasOpciones"
                label-key="label"
                value-key="idPersona"
                placeholder="— Sin persona vinculada —"
                empty-label="— Sin persona vinculada —"
                empty-value=""
                class="w-full"
              />
            </div>
            <div class="border-t border-dashed border-[#e0e6ed] dark:border-[#1b2e4b] pt-4 space-y-3">
              <p class="text-xs font-semibold uppercase tracking-widest text-warning">
                Cambiar contraseña
                <span class="ml-1 text-xs font-normal normal-case text-white-dark">(dejar vacío para no cambiar)</span>
              </p>
              <div>
                <label class="block text-xs font-semibold mb-1">Nueva contraseña</label>
                <div class="relative">
                  <input v-model="form.password" :type="mostrarNuevaPass ? 'text' : 'password'" class="form-input w-full pr-10" placeholder="Mínimo 6 caracteres" autocomplete="new-password" />
                  <button type="button" class="absolute inset-y-0 right-0 flex items-center pr-3 text-white-dark hover:text-primary transition-colors" @click="mostrarNuevaPass = !mostrarNuevaPass">
                    <svg v-if="!mostrarNuevaPass" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                    </svg>
                    <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                  </button>
                </div>
              </div>
              <div>
                <label class="block text-xs font-semibold mb-1">Confirmar contraseña</label>
                <div class="relative">
                  <input v-model="form.passwordConfirm" :type="mostrarConfirmPass ? 'text' : 'password'" class="form-input w-full pr-10"
                    :class="form.password && form.passwordConfirm && form.password !== form.passwordConfirm ? 'border-danger' : ''"
                    placeholder="Repetir nueva contraseña" autocomplete="new-password" />
                  <button type="button" class="absolute inset-y-0 right-0 flex items-center pr-3 text-white-dark hover:text-primary transition-colors" @click="mostrarConfirmPass = !mostrarConfirmPass">
                    <svg v-if="!mostrarConfirmPass" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                    </svg>
                    <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                  </button>
                </div>
                <p v-if="form.password && form.passwordConfirm && form.password !== form.passwordConfirm"
                  class="text-xs text-danger mt-1">Las contraseñas no coinciden</p>
              </div>
            </div>
          </form>
        </template>
      </div>

      <!-- Footer fijo -->
      <div class="flex-shrink-0 border-t border-[#e0e6ed] dark:border-[#1b2e4b] px-6 py-4 bg-[#fbfbfb] dark:bg-[#121c2c]">
        <div class="flex items-center justify-end gap-3">
          <button type="button" class="btn btn-outline-secondary" @click="cerrarModal">Cancelar</button>
          <button type="submit" form="drawerFormUsuario" class="btn px-6"
            :class="modoEdicion ? 'btn-warning' : 'btn-primary'"
            :disabled="guardando">
            <span v-if="guardando" class="animate-spin border-2 border-white border-l-transparent rounded-full w-3.5 h-3.5 inline-block mr-1.5 align-middle" />
            <svg v-else class="inline w-3.5 h-3.5 mr-1.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            {{ modoEdicion ? 'Actualizar' : 'Crear Usuario' }}
          </button>
        </div>
      </div>
    </div>
  </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.overlay-enter-active { transition: opacity 0.3s ease-out; }
.overlay-leave-active { transition: opacity 0.2s ease-in; }
.overlay-enter-from, .overlay-leave-to { opacity: 0; }

.drawer-enter-active { transition: transform 0.3s ease-out; }
.drawer-leave-active { transition: transform 0.2s ease-in; }
.drawer-enter-from, .drawer-leave-to { transform: translateX(100%); }

.no-spinner::-webkit-inner-spin-button,
.no-spinner::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.no-spinner {
  -moz-appearance: textfield;
}
</style>
