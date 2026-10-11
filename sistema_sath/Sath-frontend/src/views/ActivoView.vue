<script setup>
import { ref, computed, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import DataTable from '@/components/ui/DataTable.vue'
import Skeleton from '@/components/ui/Skeleton.vue'
import SathLoader from '@/components/ui/SathLoader.vue'

const { get } = useApi()
const { toast } = useToast()

// ── Recursos (BD local) ───────────────────────────────────────────────
const listaActivos  = ref([])
const listaTipos    = ref([])
const textoBusqueda = ref('')
const cargando      = ref(false)
const tipoVista     = ref(localStorage.getItem('activo_vista') || 'list')

const columns = [
  { key: 'codigoAct',      label: 'Código',         sortable: true },
  { key: 'descripcionAct', label: 'Descripción',     sortable: true },
  { key: 'tipoActivo',     label: 'Tipo de Recurso', sortable: true },
]

const listaConTipo = computed(() => {
  const mapTipo = Object.fromEntries(listaTipos.value.map(t => [t.idTipoActivo, t.nombreTipoAct]))
  return listaActivos.value.map(a => ({
    ...a,
    tipoActivo: mapTipo[a.idTipoActivo] ?? '—',
  }))
})

const listaFiltrada = computed(() => {
  const q = textoBusqueda.value.toLowerCase().trim()
  if (!q) return listaConTipo.value
  return listaConTipo.value.filter(a =>
    (a.codigoAct      ?? '').toLowerCase().includes(q) ||
    (a.descripcionAct ?? '').toLowerCase().includes(q) ||
    (a.tipoActivo     ?? '').toLowerCase().includes(q)
  )
})

async function cargar() {
  cargando.value = true
  try {
    const [resActivos, resTipos] = await Promise.all([
      get('/api/activos/listar'),
      get('/api/tipo-activo/listar'),
    ])
    if (!resActivos?.ok) throw new Error('Error al cargar recursos')
    if (!resTipos?.ok)   throw new Error('Error al cargar tipos de recurso')
    listaActivos.value = await resActivos.json()
    listaTipos.value   = await resTipos.json()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    cargando.value = false
  }
}

function cambiarVista(v) {
  tipoVista.value = v
  localStorage.setItem('activo_vista', v)
}

onMounted(cargar)
</script>

<template>
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Recursos</li>
  </ul>

  <div class="panel">
    <div>
      <!-- Header -->
      <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
        <div class="flex items-center gap-3">
          <h5 class="text-xl font-bold dark:text-white-light/90">
            Recursos
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

        <!-- Buscador -->
        <div class="relative w-52">
          <input
            v-model="textoBusqueda"
            type="text"
            placeholder="Buscar recurso..."
            class="peer form-input py-2 ltr:pr-11 rtl:pl-11 w-full"
          />
          <div class="absolute top-1/2 -translate-y-1/2 peer-focus:text-primary ltr:right-[11px] rtl:left-[11px]">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5"/>
              <path d="M18.5 18.5L22 22" stroke-linecap="round"/>
            </svg>
          </div>
        </div>
      </div>

      <!-- Loader -->
      <SathLoader v-if="cargando && tipoVista === 'list'" size="md" />

      <template v-else-if="!cargando || tipoVista === 'grid'">
        <div v-if="tipoVista === 'list'">
          <DataTable
            :data="listaFiltrada"
            :columns="columns"
            :loading="cargando"
            default-sort="codigoAct"
            empty-text="No hay recursos registrados"
            striped
          >
            <template #cell-codigoAct="{ row }">
              <span class="font-mono font-semibold text-primary">{{ row.codigoAct ?? '—' }}</span>
            </template>
            <template #cell-descripcionAct="{ row }">
              <span class="dark:text-white">{{ row.descripcionAct ?? '—' }}</span>
            </template>
            <template #cell-tipoActivo="{ row }">
              <span class="badge bg-primary/10 text-primary py-0.5 px-2 rounded-full text-xs">
                {{ row.tipoActivo }}
              </span>
            </template>
          </DataTable>
        </div>

        <!-- Vista grilla -->
        <div v-else>
          <div v-if="cargando" class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
            <div v-for="i in 8" :key="i" class="panel flex flex-col gap-3">
              <div class="flex items-center gap-2">
                <Skeleton width="36px" height="36px" />
                <Skeleton width="60%" height="1.2rem" />
              </div>
              <Skeleton width="40%" height="1rem" />
              <Skeleton width="100%" height="3rem" />
            </div>
          </div>
          <template v-else>
            <p v-if="listaFiltrada.length === 0" class="py-10 text-center text-white-dark">
              No hay recursos registrados
            </p>
            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
              <div
                v-for="item in listaFiltrada"
                :key="item.idActivo"
                class="panel flex flex-col gap-2 hover:shadow-md transition-shadow"
              >
                <div class="flex items-center gap-2 min-w-0">
                  <div class="grid h-9 w-9 shrink-0 place-content-center rounded-lg bg-primary/10">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="text-primary">
                      <rect x="2" y="3" width="20" height="14" rx="2"/>
                      <line x1="8" y1="21" x2="16" y2="21" stroke-linecap="round"/>
                      <line x1="12" y1="17" x2="12" y2="21" stroke-linecap="round"/>
                    </svg>
                  </div>
                  <span class="font-mono font-bold text-primary truncate">{{ item.codigoAct ?? '—' }}</span>
                </div>
                <span class="badge bg-primary/10 text-primary py-0.5 px-2 rounded-full text-xs w-fit max-w-full truncate">
                  {{ item.tipoActivo }}
                </span>
                <p class="text-sm text-white-dark leading-snug line-clamp-2">
                  {{ item.descripcionAct ?? 'Sin descripción' }}
                </p>
              </div>
            </div>
          </template>
        </div>
      </template>
    </div>
  </div>
</template>