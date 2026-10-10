<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import DataTable from '@/components/ui/DataTable.vue'
import Skeleton from '@/components/ui/Skeleton.vue'
import SathLoader from '@/components/ui/SathLoader.vue'

const auth = useAuthStore()
const { get, post, put, del } = useApi()
const { toast } = useToast()

const columns = computed(() => [
  { key: 'nombreTipo', label: 'Nombre' },
  { key: 'descripcionTipo', label: 'Descripción', thClass: 'hidden sm:table-cell', tdClass: 'hidden sm:table-cell' },
  ...(auth.isAdmin ? [{ key: 'acciones', label: 'Acciones', sortable: false, thClass: 'text-center w-32', tdClass: 'text-center' }] : []),
])

const lista          = ref([])
const textoBusqueda  = ref('')
const cargando       = ref(false)
const guardando      = ref(false)
const tipoVista      = ref(localStorage.getItem('tiposervicio_vista') || 'list')
const modalAbierto   = ref(false)
const modoEdicion    = ref(false)
const formulario     = ref({ idTipoServicio: null, nombreTipo: '', descripcionTipo: '' })
const confirmEliminar = ref(null)

const listaFiltrada = computed(() => {
  const texto = textoBusqueda.value.toLowerCase().trim()
  return !texto
    ? lista.value
    : lista.value.filter(t =>
        (t.nombreTipo ?? '').toLowerCase().includes(texto) ||
        (t.descripcionTipo ?? '').toLowerCase().includes(texto)
      )
})

onMounted(cargar)

async function cargar() {
  cargando.value = true
  try {
    const res = await get('/api/tipo-servicio/lista')
    if (!res?.ok) throw new Error('Error al cargar tipos de servicio')
    lista.value = await res.json()
  } catch (e) { toast(e.message, 'error') } finally { cargando.value = false }
}

function cambiarVista(v) {
  tipoVista.value = v
  localStorage.setItem('tiposervicio_vista', v)
}

function abrirModalCrear() {
  formulario.value   = { idTipoServicio: null, nombreTipo: '', descripcionTipo: '' }
  modoEdicion.value  = false
  modalAbierto.value = true
}

async function abrirModalEditar(id) {
  try {
    const res = await get(`/api/tipo-servicio/${id}`)
    if (!res?.ok) throw new Error('Tipo de servicio no encontrado')
    const datos = await res.json()
    formulario.value  = { idTipoServicio: datos.idTipoServicio, nombreTipo: datos.nombreTipo, descripcionTipo: datos.descripcionTipo ?? '' }
    modoEdicion.value = true
    modalAbierto.value = true
  } catch (e) { toast(e.message, 'error') }
}

function cerrarModal() {
  modalAbierto.value = false
  formulario.value   = { idTipoServicio: null, nombreTipo: '', descripcionTipo: '' }
  modoEdicion.value  = false
}

async function guardar() {
  if (!formulario.value.nombreTipo.trim()) {
    toast('El nombre es requerido', 'error'); return
  }
  guardando.value = true
  try {
    const payload = {
      nombreTipo:      formulario.value.nombreTipo.trim(),
      descripcionTipo: formulario.value.descripcionTipo.trim() || null,
    }
    if (modoEdicion.value) {
      const res = await put(`/api/tipo-servicio/editar/${formulario.value.idTipoServicio}`, payload)
      if (!res?.ok) { const e = await res.json().catch(() => ({})); throw new Error(e.error || 'Error al actualizar') }
      const actualizado = await res.json()
      const idx = lista.value.findIndex(t => t.idTipoServicio === actualizado.idTipoServicio)
      if (idx !== -1) {
        lista.value[idx].nombreTipo     = actualizado.nombreTipo
        lista.value[idx].descripcionTipo = actualizado.descripcionTipo
      }
      toast('Tipo de servicio actualizado correctamente')
    } else {
      const res = await post('/api/tipo-servicio/crear', payload)
      if (res?.status !== 201) { const e = await res.json().catch(() => ({})); throw new Error(e.error || 'Error al crear') }
      const nuevo = await res.json()
      lista.value.unshift({ idTipoServicio: nuevo.idTipoServicio, nombreTipo: nuevo.nombreTipo, descripcionTipo: nuevo.descripcionTipo })
      toast('Tipo de servicio creado correctamente')
    }
    cerrarModal()
  } catch (e) { toast(e.message, 'error') } finally { guardando.value = false }
}

async function confirmarEliminar() {
  const item = confirmEliminar.value
  if (!item) return
  const idx    = lista.value.findIndex(t => t.idTipoServicio === item.idTipoServicio)
  const backup = idx !== -1 ? { ...lista.value[idx] } : null
  if (idx !== -1) lista.value.splice(idx, 1)
  confirmEliminar.value = null
  try {
    const res = await del(`/api/tipo-servicio/eliminar/${item.idTipoServicio}`)
    if (res?.status === 204) { toast('Tipo de servicio eliminado correctamente') }
    else {
      if (backup && idx !== -1) lista.value.splice(idx, 0, backup)
      const e = await res.json().catch(() => ({}))
      toast(e.error || 'Error al eliminar', 'error')
    }
  } catch { if (backup && idx !== -1) lista.value.splice(idx, 0, backup); toast('Error de conexión', 'error') }
}
</script>

<template>
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Tipos de Servicio</li>
  </ul>

  <div class="panel">
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
      <div class="flex items-center gap-3">
        <h5 class="text-xl font-bold dark:text-white-light/90">
          Tipos de Servicio
          <span class="ml-2 badge bg-primary/10 text-primary py-1 px-3 rounded-full text-sm">
            {{ listaFiltrada.length }} registros
          </span>
        </h5>
        <div class="flex items-center gap-1 border border-[#e0e6ed] dark:border-[#1b2e4b] rounded-lg p-1">
          <button type="button" title="Lista" class="p-1.5 rounded transition-colors"
            :class="tipoVista === 'list' ? 'bg-primary text-white' : 'text-white-dark hover:bg-[#f1f5ff] dark:hover:bg-[#1b2e4b]'"
            @click="cambiarVista('list')">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/>
              <line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/>
              <line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/>
            </svg>
          </button>
          <button type="button" title="Grilla" class="p-1.5 rounded transition-colors"
            :class="tipoVista === 'grid' ? 'bg-primary text-white' : 'text-white-dark hover:bg-[#f1f5ff] dark:hover:bg-[#1b2e4b]'"
            @click="cambiarVista('grid')">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
              <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="flex w-full flex-col gap-3 sm:w-auto sm:flex-row sm:items-center">
        <div class="relative">
          <input v-model="textoBusqueda" type="text" placeholder="Buscar tipo..."
            class="peer form-input py-2 ltr:pr-11 rtl:pl-11 w-full sm:w-52" />
          <div class="absolute top-1/2 -translate-y-1/2 peer-focus:text-primary ltr:right-[11px] rtl:left-[11px]">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5"/><path d="M18.5 18.5L22 22" stroke-linecap="round"/>
            </svg>
          </div>
        </div>
        <button v-if="auth.isAdmin" type="button" class="btn btn-primary gap-2" @click="abrirModalCrear">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Nuevo Tipo
        </button>
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
          default-sort="nombreTipo"
          empty-text="No hay tipos de servicio registrados"
          striped
        >
          <template #cell-nombreTipo="{ row }">
            <div class="font-semibold dark:text-white">{{ row.nombreTipo }}</div>
            <div v-if="row.descripcionTipo" class="sm:hidden text-[10px] text-white-dark mt-0.5">{{ row.descripcionTipo }}</div>
          </template>
          <template #cell-descripcionTipo="{ row }">
            <span class="text-white-dark text-sm">{{ row.descripcionTipo || '—' }}</span>
          </template>
          <template #cell-acciones="{ row }">
            <div class="flex items-center justify-center gap-1">
              <button class="btn btn-xs btn-outline-primary px-2" title="Editar" @click="abrirModalEditar(row.idTipoServicio)">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
              </button>
              <button class="btn btn-xs btn-outline-danger px-2" title="Eliminar" @click="confirmEliminar = row">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/>
                  <path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/>
                </svg>
              </button>
            </div>
          </template>
        </DataTable>
      </div>

      <!-- Vista grilla -->
      <div v-else>
        <div v-if="cargando" class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <div v-for="i in 6" :key="i" class="panel flex flex-col gap-3">
            <Skeleton width="60%" height="1.4rem" />
            <Skeleton width="100%" height="2rem" />
            <div class="flex justify-end gap-1">
              <Skeleton width="30px" height="24px" />
              <Skeleton width="30px" height="24px" />
            </div>
          </div>
        </div>
        <template v-else>
          <p v-if="listaFiltrada.length === 0" class="py-10 text-center text-white-dark">No hay tipos de servicio registrados</p>
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <div v-for="item in listaFiltrada" :key="item.idTipoServicio"
            class="panel flex flex-col gap-2 hover:shadow-md transition-shadow">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="grid h-9 w-9 place-content-center rounded-lg bg-success/10">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-success">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/>
                  </svg>
                </div>
                <span class="font-bold dark:text-white">{{ item.nombreTipo }}</span>
              </div>
              <div v-if="auth.isAdmin" class="flex gap-1">
                <button class="btn btn-xs btn-outline-primary px-2" @click="abrirModalEditar(item.idTipoServicio)">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                </button>
                <button class="btn btn-xs btn-outline-danger px-2" @click="confirmEliminar = item">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/>
                    <path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/>
                  </svg>
                </button>
              </div>
            </div>
            <p v-if="item.descripcionTipo" class="text-sm text-white-dark leading-relaxed">{{ item.descripcionTipo }}</p>
            <p v-else class="text-xs text-white-dark italic">Sin descripción</p>
          </div>
        </div>
      </template>
      </div>
    </template>
  </div>

  <Teleport to="body">
    <!-- Modal crear/editar -->
    <Transition name="fade">
      <div v-if="modalAbierto" class="fixed inset-0 z-[999] overflow-y-auto bg-black/60" @click.self="cerrarModal">
        <div class="flex min-h-screen items-center justify-center px-4">
          <div class="panel w-full max-w-md rounded-lg border-0 p-0 overflow-hidden">
            <div class="flex items-center justify-between bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4">
              <div class="flex items-center gap-3">
                <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full"
                  :class="modoEdicion ? 'bg-warning/10' : 'bg-primary/10'">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                    :class="modoEdicion ? 'text-warning' : 'text-primary'">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                  </svg>
                </div>
                <h3 class="text-base font-semibold">{{ modoEdicion ? 'Editar' : 'Nuevo' }} Tipo de Servicio</h3>
              </div>
              <button type="button" class="text-white-dark hover:text-dark" @click="cerrarModal">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
            <div class="p-5 space-y-4">
              <div>
                <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Nombre <span class="text-danger">*</span></label>
                <input v-model="formulario.nombreTipo" type="text" class="form-input"
                  placeholder="Ej: Mantenimiento, Reparación..." autocomplete="off"
                  @keydown.enter.prevent="guardar" />
              </div>
              <div>
                <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Descripción</label>
                <textarea v-model="formulario.descripcionTipo" class="form-textarea" rows="3"
                  placeholder="Descripción del tipo de servicio (opcional)" />
              </div>
              <div class="flex justify-end gap-2">
                <button class="btn btn-outline-danger" @click="cerrarModal">Cancelar</button>
                <button class="btn" :class="modoEdicion ? 'btn-warning' : 'btn-primary'"
                  :disabled="guardando" @click="guardar">
                  {{ guardando ? 'Guardando...' : (modoEdicion ? 'Actualizar' : 'Crear') }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Modal confirmar eliminar -->
    <Transition name="fade">
      <div v-if="confirmEliminar" class="fixed inset-0 z-[999] overflow-y-auto bg-black/60" @click.self="confirmEliminar = null">
        <div class="flex min-h-screen items-center justify-center px-4">
          <div class="panel w-full max-w-sm rounded-lg border-0 p-0 overflow-hidden">
            <div class="flex items-center gap-3 bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4">
              <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-danger/10">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-danger">
                  <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/>
                  <path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/>
                </svg>
              </div>
              <h3 class="text-base font-semibold">¿Eliminar tipo?</h3>
            </div>
            <div class="p-5">
              <p class="text-sm text-white-dark mb-4">
                ¿Eliminar <strong class="text-dark dark:text-white">{{ confirmEliminar?.nombreTipo }}</strong>?
                El tipo de servicio quedará inactivo.
              </p>
              <div class="flex justify-end gap-2">
                <button class="btn btn-outline-secondary" @click="confirmEliminar = null">Cancelar</button>
                <button class="btn btn-danger" @click="confirmarEliminar">Sí, eliminar</button>
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