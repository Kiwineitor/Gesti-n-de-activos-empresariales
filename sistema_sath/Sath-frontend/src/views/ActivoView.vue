<script setup>
import { ref, computed, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import DataTable from '@/components/ui/DataTable.vue'
import Skeleton from '@/components/ui/Skeleton.vue'
import SathLoader from '@/components/ui/SathLoader.vue'

const { get } = useApi()
const { toast } = useToast()

// ── Pestaña activa ─────────────────────────────────────────────
const pestanaActiva = ref('sciaf')

// ── Tab SCIAF ──────────────────────────────────────────────────
const codigoBusqueda  = ref('')
const buscando        = ref(false)
const fichaResultado  = ref(null)
const errorBusqueda   = ref('')

async function buscarFicha() {
  const codigo = codigoBusqueda.value.trim()
  if (!codigo) return
  buscando.value     = true
  fichaResultado.value = null
  errorBusqueda.value  = ''
  try {
    const res = await get(`/api/sciaf/ficha/${encodeURIComponent(codigo)}`)
    if (res.status === 404) {
      errorBusqueda.value = `No se encontró el activo con código "${codigo}".`
      return
    }
    if (!res.ok) throw new Error('Error al consultar SCIAF')
    fichaResultado.value = await res.json()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    buscando.value = false
  }
}

function limpiarBusqueda() {
  codigoBusqueda.value  = ''
  fichaResultado.value  = null
  errorBusqueda.value   = ''
}

// ── Tab BD Local ───────────────────────────────────────────────
const listaActivos  = ref([])
const listaTipos    = ref([])
const textoBusqueda = ref('')
const cargando      = ref(false)
const tipoVista     = ref(localStorage.getItem('activo_vista') || 'list')

const columns = [
  { key: 'codigoAct',      label: 'Código',         sortable: true },
  { key: 'descripcionAct', label: 'Descripción',     sortable: true },
  { key: 'tipoActivo',     label: 'Tipo de Activo',  sortable: true },
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
    if (!resActivos?.ok) throw new Error('Error al cargar activos')
    if (!resTipos?.ok)   throw new Error('Error al cargar tipos de activo')
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

function activarPestana(p) {
  pestanaActiva.value = p
  if (p === 'local' && listaActivos.value.length === 0) cargar()
}
</script>

<template>
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Activos</li>
  </ul>

  <div class="panel">
    <!-- Pestañas -->
    <div class="flex border-b border-[#e0e6ed] dark:border-[#1b2e4b] mb-6">
      <button
        type="button"
        class="px-5 py-3 text-sm font-medium border-b-2 transition-colors focus:outline-none"
        :class="pestanaActiva === 'sciaf'
          ? 'border-primary text-primary'
          : 'border-transparent text-white-dark hover:text-primary hover:border-primary'"
        @click="activarPestana('sciaf')"
      >
        <span class="flex items-center gap-2">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35" stroke-linecap="round"/>
          </svg>
          Consulta
        </span>
      </button>
      <button
        type="button"
        class="px-5 py-3 text-sm font-medium border-b-2 transition-colors focus:outline-none"
        :class="pestanaActiva === 'local'
          ? 'border-primary text-primary'
          : 'border-transparent text-white-dark hover:text-primary hover:border-primary'"
        @click="activarPestana('local')"
      >
        <span class="flex items-center gap-2">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="2" y="3" width="20" height="14" rx="2"/>
            <line x1="8" y1="21" x2="16" y2="21" stroke-linecap="round"/>
            <line x1="12" y1="17" x2="12" y2="21" stroke-linecap="round"/>
          </svg>
          BD Local
        </span>
      </button>
    </div>

    <!-- ══════════════════════════════════════ -->
    <!-- PESTAÑA: Consulta SCIAF               -->
    <!-- ══════════════════════════════════════ -->
    <div v-if="pestanaActiva === 'sciaf'">
      <div class="mb-6">
        <h5 class="text-xl font-bold dark:text-white-light/90 mb-1">
          Consulta de Activos
        </h5>
        <p class="text-sm text-white-dark">
          Ingresa el código de activo para consultar su ficha en el sistema de la UAP.
        </p>
      </div>

      <!-- Buscador -->
      <form class="flex gap-3 mb-6" @submit.prevent="buscarFicha">
        <div class="relative flex-1 max-w-md">
          <input
            v-model="codigoBusqueda"
            type="text"
            placeholder="Ej: 01-01-08-00583"
            class="form-input py-2.5 ltr:pr-11 rtl:pl-11 w-full font-mono"
            :disabled="buscando"
          />
          <div class="absolute top-1/2 -translate-y-1/2 text-white-dark ltr:right-3 rtl:left-3">
<!--            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">-->
<!--              <rect x="2" y="3" width="20" height="14" rx="2"/>-->
<!--              <line x1="8" y1="21" x2="16" y2="21" stroke-linecap="round"/>-->
<!--              <line x1="12" y1="17" x2="12" y2="21" stroke-linecap="round"/>-->
<!--            </svg>-->
          </div>
        </div>
        <button
          type="submit"
          class="btn btn-primary px-6"
          :disabled="buscando || !codigoBusqueda.trim()"
        >
          <span v-if="buscando" class="flex items-center gap-2">
            <svg class="animate-spin" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 12a9 9 0 1 1-6.219-8.56" stroke-linecap="round"/>
            </svg>
            Buscando…
          </span>
          <span v-else class="flex items-center gap-2">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5"/>
              <path d="M18.5 18.5L22 22" stroke-linecap="round"/>
            </svg>
            Buscar
          </span>
        </button>
        <button
          v-if="fichaResultado || errorBusqueda"
          type="button"
          class="btn btn-outline-danger px-4"
          @click="limpiarBusqueda"
        >
          Limpiar
        </button>
      </form>

      <!-- Error -->
      <div v-if="errorBusqueda" class="flex items-start gap-3 p-4 rounded-lg bg-danger/10 border border-danger/30 text-danger">
        <svg class="shrink-0 mt-0.5" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <p class="text-sm">{{ errorBusqueda }}</p>
      </div>

      <!-- Resultado -->
      <div v-if="fichaResultado" class="rounded-xl border border-[#e0e6ed] dark:border-[#1b2e4b] overflow-hidden">
        <!-- Header de la ficha -->
        <div class="bg-primary/10 dark:bg-primary/20 px-6 py-4 flex items-center gap-3">
          <div class="grid h-10 w-10 shrink-0 place-content-center rounded-lg bg-primary/20">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="text-primary">
              <rect x="2" y="3" width="20" height="14" rx="2"/>
              <line x1="8" y1="21" x2="16" y2="21" stroke-linecap="round"/>
              <line x1="12" y1="17" x2="12" y2="21" stroke-linecap="round"/>
            </svg>
          </div>
          <div>
            <p class="text-xs text-white-dark uppercase tracking-wider font-medium">Código de Activo</p>
            <p class="font-mono font-bold text-primary text-lg leading-tight">{{ fichaResultado.codigo }}</p>
          </div>
        </div>

        <!-- Campos -->
        <div class="divide-y divide-[#e0e6ed] dark:divide-[#1b2e4b]">
          <div class="grid grid-cols-1 sm:grid-cols-3 px-6 py-4 gap-1">
            <span class="text-xs font-semibold text-white-dark uppercase tracking-wide sm:col-span-1">Responsable</span>
            <span class="dark:text-white font-medium sm:col-span-2">{{ fichaResultado.responsable ?? '—' }}</span>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-3 px-6 py-4 gap-1">
            <span class="text-xs font-semibold text-white-dark uppercase tracking-wide sm:col-span-1">Oficina</span>
            <span class="dark:text-white sm:col-span-2">{{ fichaResultado.oficina ?? '—' }}</span>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-3 px-6 py-4 gap-1">
            <span class="text-xs font-semibold text-white-dark uppercase tracking-wide sm:col-span-1">Unidad</span>
            <span class="dark:text-white sm:col-span-2">
              <span class="badge bg-primary/10 text-primary py-0.5 px-2 rounded-full text-xs">
                {{ fichaResultado.unidad ?? '—' }}
              </span>
            </span>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-3 px-6 py-4 gap-1">
            <span class="text-xs font-semibold text-white-dark uppercase tracking-wide sm:col-span-1">Descripción</span>
            <span class="dark:text-white text-sm leading-relaxed sm:col-span-2">{{ fichaResultado.descripcion ?? '—' }}</span>
          </div>
        </div>
      </div>

      <!-- Estado vacío inicial -->
      <div v-else-if="!buscando && !errorBusqueda" class="flex flex-col items-center justify-center py-16 text-white-dark">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" class="mb-4 opacity-40">
          <circle cx="11.5" cy="11.5" r="9.5"/><path d="M18.5 18.5L22 22" stroke-linecap="round"/>
          <line x1="11.5" y1="8" x2="11.5" y2="15"/><line x1="11.5" y1="17" x2="11.51" y2="17"/>
        </svg>
        <p class="text-sm">Ingresa un código de activo para consultar su ficha.</p>
      </div>
    </div>

    <!-- ══════════════════════════════════════ -->
    <!-- PESTAÑA: BD Local                     -->
    <!-- ══════════════════════════════════════ -->
    <div v-else-if="pestanaActiva === 'local'">
      <!-- Header -->
      <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
        <div class="flex items-center gap-3">
          <h5 class="text-xl font-bold dark:text-white-light/90">
            Activos
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
            placeholder="Buscar activo..."
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
            empty-text="No hay activos registrados"
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
              No hay activos registrados
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