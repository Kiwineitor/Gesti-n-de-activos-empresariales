<script setup>
import { ref, computed, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import DataTable from '@/components/ui/DataTable.vue'
import Skeleton from '@/components/ui/Skeleton.vue'
import SathLoader from '@/components/ui/SathLoader.vue'

const columns = [
  { key: 'nombreCompleto', label: 'Nombre Completo' },
  { key: 'nombreCargo', label: 'Cargo' },
  { key: 'nombreUni', label: 'Unidad' },
  { key: 'codigoAdmi', label: 'Código' },
  { key: 'acciones', label: 'Acciones', sortable: false, thClass: 'text-center', tdClass: 'text-center' },
]

const { get } = useApi()
const { toast } = useToast()

const lista          = ref([])
const seleccionado   = ref(null)
const textoBusqueda  = ref('')
const cargando       = ref(false)
const modalDetalle   = ref(false)
const tipoVista      = ref(localStorage.getItem('admin_vista') || 'list')

const listaFiltrada = computed(() => {
  const texto = textoBusqueda.value.toLowerCase().trim()
  return lista.value.filter(
    (a) =>
      !texto ||
      (a.nombreCompleto ?? '').toLowerCase().includes(texto) ||
      (a.nombreUni ?? '').toLowerCase().includes(texto) ||
      (a.nombreCargo ?? '').toLowerCase().includes(texto),
  )
})

onMounted(cargar)

async function cargar() {
  cargando.value = true
  try {
    const res = await get('/api/administrativo/listar')
    if (!res?.ok) throw new Error('Error al cargar administrativos')
    lista.value = await res.json()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    cargando.value = false
  }
}

function cambiarVista(v) {
  tipoVista.value = v
  localStorage.setItem('admin_vista', v)
}

function abrirDetalle(a) {
  seleccionado.value = a
  modalDetalle.value = true
}

function iniciales(nombre) {
  if (!nombre) return '?'
  return nombre.trim().split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase()
}
</script>

<template>
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Administrativos</li>
  </ul>

  <div class="panel">
    <!-- Header -->
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
      <div class="flex items-center gap-3">
        <h5 class="text-xl font-bold dark:text-white-light/90">
          Administrativos
          <span class="ml-2 badge bg-primary/10 text-primary py-1 px-3 rounded-full text-sm">
            {{ listaFiltrada.length }} registros
          </span>
        </h5>
        <!-- Toggle lista/grilla -->
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

      <div class="relative">
        <input v-model="textoBusqueda" type="text" placeholder="Buscar nombre, cargo o unidad..."
          class="peer form-input py-2 ltr:pr-11 rtl:pl-11 w-full sm:w-64" />
        <div class="absolute top-1/2 -translate-y-1/2 peer-focus:text-primary ltr:right-[11px] rtl:left-[11px]">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5"/><path d="M18.5 18.5L22 22" stroke-linecap="round"/>
          </svg>
        </div>
      </div>
    </div>

    <!-- Loader Llamativo -->
    <SathLoader v-if="cargando && tipoVista === 'list'" size="md" />

    <template v-if="tipoVista === 'grid' || !cargando">
      <!-- ── VISTA LISTA ── -->
      <div v-if="tipoVista === 'list'">
        <DataTable
          :data="listaFiltrada"
          :columns="columns"
          :loading="cargando"
          default-sort="nombreCompleto"
          empty-text="Sin administrativos registrados"
          striped
        >
          <template #cell-nombreCompleto="{ row }">
            <span class="font-semibold dark:text-white">{{ row.nombreCompleto || '—' }}</span>
          </template>
          <template #cell-nombreCargo="{ row }">
            <span class="text-white-dark">{{ row.nombreCargo || '—' }}</span>
          </template>
          <template #cell-nombreUni="{ row }">
            <span class="text-white-dark">{{ row.nombreUni || '—' }}</span>
          </template>
          <template #cell-codigoAdmi="{ row }">
            <span class="text-white-dark text-xs font-mono">{{ row.codigoAdmi || '—' }}</span>
          </template>
          <template #cell-acciones="{ row }">
            <button class="btn btn-xs btn-outline-primary px-3" @click="abrirDetalle(row)">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="inline-block mr-1">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
              </svg>
              Ver
            </button>
          </template>
        </DataTable>
      </div>

      <!-- ── VISTA GRILLA ── -->
      <div v-else>
        <div v-if="cargando" class="grid grid-cols-2 gap-3 sm:grid-cols-2 xl:grid-cols-3">
          <div v-for="i in 6" :key="i" class="rounded-xl border border-white-light dark:border-[#1b2e4b] bg-white dark:bg-[#0e1726] overflow-hidden">
            <div class="h-1.5 bg-white-light/50 dark:bg-[#1b2e4b]" />
            <div class="p-4 flex flex-col items-center text-center gap-2">
              <Skeleton width="56px" height="56px" circle />
              <Skeleton width="70%" height="0.9rem" />
              <Skeleton width="50%" height="0.8rem" />
              <Skeleton width="60%" height="0.8rem" />
            </div>
          </div>
        </div>
        <template v-else>
          <p v-if="listaFiltrada.length === 0" class="py-10 text-center text-white-dark">Sin administrativos registrados</p>
          <div class="grid grid-cols-2 gap-3 sm:grid-cols-2 xl:grid-cols-3">
            <div
              v-for="a in listaFiltrada"
              :key="a.idAdministrativo"
              class="rounded-xl border border-white-light dark:border-[#1b2e4b] bg-white dark:bg-[#0e1726] overflow-hidden hover:shadow-md transition-shadow duration-200 cursor-pointer"
              @click="abrirDetalle(a)"
            >
              <!-- Barra de acento superior -->
              <div class="h-1.5 bg-gradient-to-r from-primary to-primary/40" />
              <!-- Contenido centrado -->
              <div class="p-4 flex flex-col items-center text-center gap-1.5">
                <div class="grid h-14 w-14 place-content-center rounded-full bg-primary/10 text-primary font-bold text-lg select-none">
                  {{ iniciales(a.nombreCompleto) }}
                </div>
                <p class="font-bold text-sm dark:text-white leading-snug w-full line-clamp-2">{{ a.nombreCompleto || '—' }}</p>
                <span class="inline-block max-w-full truncate rounded-full bg-primary/10 px-2.5 py-0.5 text-xs text-primary">
                  {{ a.nombreCargo || 'Sin cargo' }}
                </span>
                <p class="w-full truncate text-xs text-white-dark">{{ a.nombreUni || 'Sin unidad' }}</p>
                <p v-if="a.codigoAdmi" class="font-mono text-[11px] text-white-dark/60 truncate w-full">{{ a.codigoAdmi }}</p>
              </div>
            </div>
          </div>
        </template>
      </div>
    </template>
  </div>

  <!-- Modal Detalle -->
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="modalDetalle" class="fixed inset-0 z-[999] overflow-y-auto bg-black/60" @click.self="modalDetalle = false">
        <div class="flex min-h-screen items-center justify-center px-4">
          <div class="panel w-full max-w-md rounded-lg border-0 p-0 overflow-hidden">
            <div class="flex items-center justify-between bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4">
              <div class="flex items-center gap-3">
                <div class="grid h-10 w-10 shrink-0 place-content-center rounded-full bg-primary/10 text-primary font-bold text-sm">
                  {{ iniciales(seleccionado?.nombreCompleto) }}
                </div>
                <div>
                  <h3 class="text-base font-semibold">{{ seleccionado?.nombreCompleto }}</h3>
                  <p class="text-xs text-white-dark">{{ seleccionado?.nombreCargo || 'Sin cargo' }}</p>
                </div>
              </div>
              <button type="button" class="text-white-dark hover:text-dark" @click="modalDetalle = false">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
            <div v-if="seleccionado" class="p-5 space-y-3 text-sm">
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <p class="text-xs text-white-dark uppercase font-semibold mb-0.5">Unidad</p>
                  <p class="font-semibold dark:text-white">{{ seleccionado.nombreUni || '—' }}</p>
                </div>
                <div>
                  <p class="text-xs text-white-dark uppercase font-semibold mb-0.5">Código</p>
                  <p class="font-semibold dark:text-white font-mono">{{ seleccionado.codigoAdmi || '—' }}</p>
                </div>
                <div>
                  <p class="text-xs text-white-dark uppercase font-semibold mb-0.5">Teléfono</p>
                  <p class="font-semibold dark:text-white">{{ seleccionado.nroCellPer || '—' }}</p>
                </div>
                <div>
                  <p class="text-xs text-white-dark uppercase font-semibold mb-0.5">Correo</p>
                  <p class="font-semibold dark:text-white break-all">{{ seleccionado.correoPer || '—' }}</p>
                </div>
              </div>
            </div>
            <div class="flex justify-end px-5 pb-5">
              <button class="btn btn-outline-danger" @click="modalDetalle = false">Cerrar</button>
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