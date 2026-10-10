<script setup>
import { ref, computed, watch } from 'vue'
import Skeleton from './Skeleton.vue'

const props = defineProps({
  data:           { type: Array,   default: () => [] },
  // [{ key, label, sortable?, thClass?, tdClass? }]
  columns:        { type: Array,   default: () => [] },
  showIndex:      { type: Boolean, default: true },
  defaultSort:    { type: String,  default: '' },
  defaultSortAsc: { type: Boolean, default: true },
  emptyText:      { type: String,  default: 'No hay registros' },
  striped:        { type: Boolean, default: false },
  loading:        { type: Boolean, default: false },
})

const paginaActual = ref(1)
const porPagina    = ref(10)
const sortCol      = ref(props.defaultSort)
const sortAsc      = ref(props.defaultSortAsc)

const dataOrdenada = computed(() => {
  const lista = [...props.data]
  if (!sortCol.value) return lista
  return lista.sort((a, b) => {
    const va = a[sortCol.value] ?? ''
    const vb = b[sortCol.value] ?? ''
    const cmp = String(va).localeCompare(String(vb), undefined, { numeric: true })
    return sortAsc.value ? cmp : -cmp
  })
})

const totalRegistros = computed(() => dataOrdenada.value.length)
const totalPaginas   = computed(() => Math.max(1, Math.ceil(totalRegistros.value / porPagina.value)))

const dataPaginada = computed(() => {
  const ini = (paginaActual.value - 1) * porPagina.value
  return dataOrdenada.value.slice(ini, ini + porPagina.value)
})

const paginasVisibles = computed(() => {
  const pages = []
  for (let i = Math.max(1, paginaActual.value - 2); i <= Math.min(totalPaginas.value, paginaActual.value + 2); i++) {
    pages.push(i)
  }
  return pages
})

function sortBy(col) {
  if (sortCol.value === col) sortAsc.value = !sortAsc.value
  else { sortCol.value = col; sortAsc.value = true }
}

function iconoSort(col) {
  if (sortCol.value !== col) return '↕'
  return sortAsc.value ? '↑' : '↓'
}

watch(() => props.data, () => { paginaActual.value = 1 })
watch(porPagina,        () => { paginaActual.value = 1 })
</script>

<template>
  <div>
    <!-- Selector registros por página -->
    <div class="flex items-center gap-2 mb-3 text-sm text-white-dark">
      <span>Mostrar</span>
      <select v-model="porPagina" class="form-select py-1 w-20 text-sm">
        <option :value="10">10</option>
        <option :value="25">25</option>
        <option :value="50">50</option>
        <option :value="100">100</option>
      </select>
      <span>registros</span>
    </div>

    <!-- Tabla -->
    <div class="table-responsive">
      <table :class="['table-hover', striped && 'table-striped']">
        <thead>
          <tr>
            <th v-if="showIndex" class="w-8">#</th>
            <th
              v-for="col in columns"
              :key="col.key"
              :class="[col.thClass, col.sortable !== false ? 'cursor-pointer select-none' : '']"
              @click="col.sortable !== false ? sortBy(col.key) : undefined"
            >
              <span
                class="flex items-center gap-1"
                :class="col.thClass?.includes('text-center') ? 'justify-center' : ''"
              >
                {{ col.label }}
                <span v-if="col.sortable !== false" class="text-xs opacity-50">{{ iconoSort(col.key) }}</span>
              </span>
            </th>
          </tr>
        </thead>
        <tbody>
          <!-- Estado de carga (Skeletons) -->
          <template v-if="loading">
            <tr v-for="i in porPagina" :key="'sk'+i">
              <td v-if="showIndex"><Skeleton width="20px" /></td>
              <td v-for="col in columns" :key="col.key">
                <Skeleton :width="Math.floor(Math.random() * (90 - 40 + 1) + 40) + '%'" />
              </td>
            </tr>
          </template>

          <template v-else>
            <tr v-if="dataPaginada.length === 0">
              <td :colspan="columns.length + (showIndex ? 1 : 0)" class="text-center text-white-dark py-8">
                <slot name="empty">{{ emptyText }}</slot>
              </td>
            </tr>
            <tr v-for="(row, i) in dataPaginada" :key="i">
              <td v-if="showIndex" class="text-white-dark text-xs">
                {{ (paginaActual - 1) * porPagina + i + 1 }}
              </td>
              <td v-for="col in columns" :key="col.key" :class="col.tdClass">
                <slot :name="`cell-${col.key}`" :row="row" :index="i" :global-index="(paginaActual - 1) * porPagina + i">
                  {{ row[col.key] ?? '—' }}
                </slot>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>

    <!-- Paginación -->
    <div class="flex flex-wrap items-center justify-between gap-4 mt-4">
      <p class="text-sm text-white-dark">
        Mostrando
        <span class="font-semibold text-black dark:text-white">
          {{ totalRegistros === 0 ? 0 : (paginaActual - 1) * porPagina + 1 }}
        </span>
        a
        <span class="font-semibold text-black dark:text-white">
          {{ Math.min(paginaActual * porPagina, totalRegistros) }}
        </span>
        de
        <span class="font-semibold text-black dark:text-white">{{ totalRegistros }}</span>
        registros
      </p>

      <div class="flex items-center gap-1">
        <button class="btn btn-xs btn-outline-primary px-2" :disabled="paginaActual === 1"            @click="paginaActual = 1">«</button>
        <button class="btn btn-xs btn-outline-primary px-2" :disabled="paginaActual === 1"            @click="paginaActual--">‹</button>
        <button
          v-for="p in paginasVisibles" :key="p"
          class="btn btn-xs px-3"
          :class="p === paginaActual ? 'btn-primary' : 'btn-outline-primary'"
          @click="paginaActual = p"
        >{{ p }}</button>
        <button class="btn btn-xs btn-outline-primary px-2" :disabled="paginaActual === totalPaginas" @click="paginaActual++">›</button>
        <button class="btn btn-xs btn-outline-primary px-2" :disabled="paginaActual === totalPaginas" @click="paginaActual = totalPaginas">»</button>
      </div>
    </div>
  </div>
</template>