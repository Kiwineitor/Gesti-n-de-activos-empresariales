<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { useApi } from '@/composables/useApi'
import ApexCharts from 'apexcharts'

const auth = useAuthStore()
const appStore = useAppStore()
const { get } = useApi()

const cargando = ref(true)
const tecnicoActual = ref('')
const tecnicoSeleccionado = ref('')
const listaTecnicos = ref([])
const kpis = ref({
  totalAsignadas: 0,
  finalizadas: 0,
  enMantenimiento: 0,
  pendientes: 0,
  promedioDiasResolucion: null,
})
const porMes = ref([])
const topFallas = ref([])

let barChart = null
let donutChart = null

const isAdminOrDev = computed(() => auth.isAdmin || auth.isDeveloper)

const colores = computed(() => {
  const dark = appStore.isDark
  return {
    text: dark ? '#888ea8' : '#515365',
    grid: dark ? '#1b2e4b' : '#e0e6ed',
    bar: dark ? ['#22d3ee'] : ['#4361ee'],
    donut: dark ? ['#4ade80', '#fb923c', '#60a5fa'] : ['#22c55e', '#f97316', '#3b82f6'],
    fill: dark
      ? [
          { offset: 0, color: '#22d3ee', opacity: 1 },
          { offset: 100, color: '#0e7490', opacity: 0.6 },
        ]
      : [
          { offset: 0, color: '#4361ee', opacity: 1 },
          { offset: 100, color: '#a5b4fc', opacity: 0.5 },
        ],
  }
})

onMounted(async () => {
  tecnicoActual.value = auth.nombre
  await cargarListaTecnicos()
  await cargarStats()
})

onUnmounted(() => {
  barChart?.destroy()
  barChart = null
  donutChart?.destroy()
  donutChart = null
})

watch(() => appStore.isDark, actualizarTema)

async function cargarListaTecnicos() {
  if (!isAdminOrDev.value) return
  try {
    const res = await get('/api/dashboard/lista-tecnicos')
    if (!res?.ok) return
    listaTecnicos.value = await res.json()
  } catch {}
}

async function cargarStats() {
  cargando.value = true
  try {
    const url =
      isAdminOrDev.value && tecnicoSeleccionado.value
        ? `/api/dashboard/tecnico/${tecnicoSeleccionado.value}/stats`
        : '/api/dashboard/mis-stats'

    const res = await get(url)
    if (!res?.ok) throw new Error('Error al cargar stats')

    const data = await res.json()
    kpis.value = data.kpis ?? {
      totalAsignadas: 0,
      finalizadas: 0,
      enMantenimiento: 0,
      pendientes: 0,
      promedioDiasResolucion: null,
    }
    porMes.value = data.porMes ?? []
    topFallas.value = data.topFallas ?? []

    if (isAdminOrDev.value && tecnicoSeleccionado.value) {
      const t = listaTecnicos.value.find((t) => t.username === tecnicoSeleccionado.value)
      tecnicoActual.value = t ? t.nombreCompleto : tecnicoSeleccionado.value
    } else {
      tecnicoActual.value = auth.nombre
    }

    cargando.value = false
    await nextTick()
    renderCharts()
  } catch (e) {
    console.error('Dashboard técnico:', e.message)
    cargando.value = false
  }
}

function renderCharts() {
  renderBarChart()
  renderDonutChart()
}

function renderBarChart() {
  const el = document.getElementById('chart-mes')
  if (!el) return
  barChart?.destroy()
  barChart = null
  el.innerHTML = ''

  const c = colores.value
  const dark = appStore.isDark

  barChart = new ApexCharts(el, {
    chart: {
      type: 'bar',
      height: 260,
      toolbar: { show: false },
      background: 'transparent',
      fontFamily: 'Nunito, sans-serif',
      foreColor: c.text,
    },
    theme: { mode: dark ? 'dark' : 'light' },
    colors: c.bar,
    plotOptions: { bar: { borderRadius: 6, columnWidth: '45%' } },
    dataLabels: { enabled: false },
    series: [{ name: 'Solicitudes', data: porMes.value.map((m) => Number(m.cantidad)) }],
    xaxis: {
      categories: porMes.value.map((m) => m.mes),
      axisBorder: { show: false },
      axisTicks: { show: false },
      labels: { style: { colors: c.text, fontSize: '12px' } },
    },
    yaxis: {
      min: 0,
      labels: { formatter: (v) => Math.floor(v), style: { colors: c.text } },
    },
    grid: { borderColor: c.grid, strokeDashArray: 4, xaxis: { lines: { show: false } } },
    fill: {
      type: 'gradient',
      gradient: {
        type: 'vertical',
        shadeIntensity: 0.4,
        opacityFrom: 1,
        opacityTo: 0.5,
        stops: [0, 100],
        colorStops: c.fill,
      },
    },
    tooltip: { theme: dark ? 'dark' : 'light', y: { formatter: (v) => `${v} solicitudes` } },
    noData: { text: 'Sin datos para el año actual', style: { color: c.text } },
  })
  barChart.render()
}

function renderDonutChart() {
  const el = document.getElementById('chart-estado')
  if (!el) return
  donutChart?.destroy()
  donutChart = null
  el.innerHTML = ''

  const c = colores.value
  const dark = appStore.isDark
  const fin = Number(kpis.value.finalizadas ?? 0)
  const mant = Number(kpis.value.enMantenimiento ?? 0)
  const pend = Number(kpis.value.pendientes ?? 0)

  donutChart = new ApexCharts(el, {
    chart: {
      type: 'donut',
      height: 220,
      background: 'transparent',
      fontFamily: 'Nunito, sans-serif',
    },
    theme: { mode: dark ? 'dark' : 'light' },
    colors: c.donut,
    labels: ['Finalizadas', 'En Mantenimiento', 'Pendientes'],
    series: [fin, mant, pend],
    legend: { position: 'bottom', fontSize: '12px', labels: { colors: c.text } },
    dataLabels: { enabled: false },
    plotOptions: {
      pie: {
        donut: {
          size: '68%',
          labels: {
            show: true,
            total: {
              show: true,
              label: 'Total',
              fontSize: '13px',
              fontWeight: '700',
              color: c.text,
              formatter: () => String(Number(kpis.value.totalAsignadas ?? 0)),
            },
          },
        },
      },
    },
    stroke: { width: 0 },
    tooltip: { theme: dark ? 'dark' : 'light' },
  })
  donutChart.render()
}

function actualizarTema() {
  const c = colores.value
  const dark = appStore.isDark
  const opts = {
    chart: { foreColor: c.text },
    theme: { mode: dark ? 'dark' : 'light' },
    colors: c.bar,
    fill: {
      type: 'gradient',
      gradient: {
        type: 'vertical',
        shadeIntensity: 0.4,
        opacityFrom: 1,
        opacityTo: 0.5,
        stops: [0, 100],
        colorStops: c.fill,
      },
    },
    xaxis: { labels: { style: { colors: c.text } } },
    yaxis: { labels: { style: { colors: c.text } } },
    grid: { borderColor: c.grid },
    tooltip: { theme: dark ? 'dark' : 'light' },
  }
  barChart?.updateOptions(opts, false, false)
  donutChart?.updateOptions(
    {
      theme: { mode: dark ? 'dark' : 'light' },
      colors: c.donut,
      legend: { labels: { colors: c.text } },
      tooltip: { theme: dark ? 'dark' : 'light' },
      plotOptions: { pie: { donut: { labels: { show: true, total: { color: c.text } } } } },
    },
    false,
    false,
  )
}

function porcentaje(valor) {
  const total = Number(kpis.value.totalAsignadas ?? 0)
  if (!total) return ''
  return Math.round((Number(valor) / total) * 100) + '% del total'
}

function barraRelativa(cantidad) {
  if (!topFallas.value.length) return '0%'
  const max = Number(topFallas.value[0].cantidad)
  return max ? Math.round((Number(cantidad) / max) * 100) + '%' : '0%'
}
</script>

<template>
  <!-- Breadcrumb -->
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">
      Dashboard Técnico
    </li>
  </ul>

  <!-- Header -->
  <div class="flex flex-wrap items-center justify-between gap-4 mb-6">
    <div>
      <h5 class="text-2xl font-bold dark:text-white-light/90">Dashboard Técnico</h5>
      <p class="text-white-dark mt-1 text-sm">
        Stats de:
        <span class="font-semibold text-primary">{{ tecnicoActual }}</span>
      </p>
    </div>

    <!-- Selector de técnico — solo admin/dev -->
    <div v-if="isAdminOrDev" class="flex items-center gap-3">
      <label class="text-sm font-semibold dark:text-white-light shrink-0">Ver técnico:</label>
      <select
        v-model="tecnicoSeleccionado"
        class="form-select w-64"
        @change="cargarStats()"
      >
        <option value="">Mis estadísticas</option>
        <option
          v-for="t in listaTecnicos"
          :key="t.username"
          :value="t.username"
        >
          {{ t.nombreCompleto }} ({{ t.totalAsignadas }} asig.)
        </option>
      </select>
    </div>
  </div>

  <!-- Spinner -->
  <div v-if="cargando" class="flex justify-center items-center py-24">
    <span
      class="border-4 border-primary/30 border-t-primary rounded-full w-12 h-12 inline-block animate-spin"
    />
  </div>

  <div v-else>
    <!-- KPI Cards -->
    <div class="mb-6 grid grid-cols-2 gap-4 lg:grid-cols-4">

      <div class="panel border-l-4 border-blue-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wider text-white-dark">
              Total Asignadas
            </p>
            <h3 class="mt-1 text-3xl font-bold dark:text-white-light">
              {{ kpis.totalAsignadas ?? 0 }}
            </h3>
          </div>
          <div class="rounded-full bg-blue-100 p-3 dark:bg-blue-500/20">
            <svg class="text-blue-500" width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
              <path opacity="0.5" d="M12 2C9.38 2 7.25 4.13 7.25 6.75C7.25 9.32 9.26 11.4 11.88 11.49C12.04 11.48 12.13 11.49 12.17 11.49C14.73 11.4 16.74 9.32 16.75 6.75C16.75 4.13 14.62 2 12 2Z"/>
              <path d="M17.08 14.15C14.29 12.29 9.74 12.29 6.93 14.15C5.66 15 4.96 16.15 4.96 17.38C4.96 18.61 5.66 19.75 6.92 20.59C8.32 21.53 10.16 22 12 22C13.84 22 15.68 21.53 17.08 20.59C18.34 19.74 19.04 18.6 19.04 17.36C19.03 16.13 18.34 14.99 17.08 14.15Z"/>
            </svg>
          </div>
        </div>
      </div>

      <div class="panel border-l-4 border-green-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wider text-white-dark">Finalizadas</p>
            <h3 class="mt-1 text-3xl font-bold dark:text-white-light">
              {{ kpis.finalizadas ?? 0 }}
            </h3>
            <p class="mt-0.5 text-xs text-white-dark">{{ porcentaje(kpis.finalizadas) }}</p>
          </div>
          <div class="rounded-full bg-green-100 p-3 dark:bg-green-500/20">
            <svg class="text-green-500" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 12L11 14L15 10M21 12C21 16.97 16.97 21 12 21C7.03 21 3 16.97 3 12C3 7.03 7.03 3 12 3C16.97 3 21 7.03 21 12Z"/>
            </svg>
          </div>
        </div>
      </div>

      <div class="panel border-l-4 border-orange-400">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wider text-white-dark">
              En Mantenimiento
            </p>
            <h3 class="mt-1 text-3xl font-bold dark:text-white-light">
              {{ kpis.enMantenimiento ?? 0 }}
            </h3>
          </div>
          <div class="rounded-full bg-orange-100 p-3 dark:bg-orange-500/20">
            <svg class="text-orange-400" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/>
            </svg>
          </div>
        </div>
      </div>

      <div class="panel border-l-4 border-purple-500">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wider text-white-dark">
              Promedio Resolución
            </p>
            <h3 class="mt-1 text-3xl font-bold dark:text-white-light">
              <span>{{ kpis.promedioDiasResolucion ?? '—' }}</span>
              <span v-if="kpis.promedioDiasResolucion" class="text-base font-normal"> días</span>
            </h3>
          </div>
          <div class="rounded-full bg-purple-100 p-3 dark:bg-purple-500/20">
            <svg class="text-purple-500" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- Gráficos -->
    <div class="mb-6 grid grid-cols-1 gap-6 lg:grid-cols-3">

      <!-- Barras: solicitudes por mes -->
      <div class="panel lg:col-span-2">
        <div class="mb-5 flex items-center justify-between">
          <h5 class="text-lg font-semibold dark:text-white-light">
            Solicitudes por mes
            <span class="ml-1 text-xs font-normal text-white-dark">(año actual)</span>
          </h5>
        </div>
        <div id="chart-mes" />
      </div>

      <!-- Dona: distribución por estado -->
      <div class="panel">
        <div class="mb-5 flex items-center justify-between">
          <h5 class="text-lg font-semibold dark:text-white-light">Estado</h5>
        </div>
        <div id="chart-estado" />
        <div class="mt-4 space-y-2">
          <div class="flex items-center gap-2">
            <span class="inline-block h-3 w-3 rounded-full bg-green-500"></span>
            <span class="text-xs dark:text-white-dark">
              Finalizadas: <strong>{{ kpis.finalizadas ?? 0 }}</strong>
            </span>
          </div>
          <div class="flex items-center gap-2">
            <span class="inline-block h-3 w-3 rounded-full bg-orange-400"></span>
            <span class="text-xs dark:text-white-dark">
              En Mantenimiento: <strong>{{ kpis.enMantenimiento ?? 0 }}</strong>
            </span>
          </div>
          <div class="flex items-center gap-2">
            <span class="inline-block h-3 w-3 rounded-full bg-blue-400"></span>
            <span class="text-xs dark:text-white-dark">
              Pendientes: <strong>{{ kpis.pendientes ?? 0 }}</strong>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Top 5 Fallas -->
    <div class="panel">
      <div class="mb-5 flex items-center justify-between">
        <h5 class="text-lg font-semibold dark:text-white-light">Top 5 Fallas más frecuentes</h5>
      </div>

      <div
        v-if="!topFallas.length"
        class="flex flex-col items-center justify-center py-10 text-white-dark"
      >
        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" class="mb-2 opacity-30">
          <path opacity="0.5" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z" fill="currentColor"/>
          <path d="M12 7v6M12 17h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span class="text-sm">Sin fallas catalogadas aún</span>
      </div>

      <div v-else class="space-y-4">
        <div v-for="(falla, i) in topFallas" :key="i" class="flex items-center gap-3">
          <span
            class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-primary/10 text-xs font-bold text-primary"
          >{{ i + 1 }}</span>
          <div class="min-w-0 flex-1">
            <div class="mb-1 flex items-center justify-between">
              <span class="truncate text-sm font-medium dark:text-white-light">
                {{ falla.nombreFalla }}
              </span>
              <span class="ml-2 shrink-0 text-xs text-white-dark">{{ falla.cantidad }} veces</span>
            </div>
            <div class="h-2 overflow-hidden rounded-full bg-[#ebedf2] dark:bg-dark/40">
              <div
                class="h-full rounded-full bg-gradient-to-r from-primary to-secondary transition-all duration-700"
                :style="'width:' + barraRelativa(falla.cantidad)"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>