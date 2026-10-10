<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { useApi } from '@/composables/useApi'
import { useWebSocket } from '@/composables/useWebSocket'
import ApexCharts from 'apexcharts'
import SathLoader from '@/components/ui/SathLoader.vue'

const auth = useAuthStore()
const appStore = useAppStore()
const { get } = useApi()
const { on } = useWebSocket()

const stats = ref({ total: 0, pendiente: 0, enMantenimiento: 0, finalizado: 0 })
const solicitudesRecientes = ref([])
const porMes = ref([])
const cargando = ref(false)

let barChart = null
let donutChart = null
let timer = null

onMounted(async () => {
  await cargarDatos()
  initCharts()
  timer = setInterval(cargarDatos, 60000)
})

onUnmounted(() => {
  clearInterval(timer)
  barChart?.destroy()
  donutChart?.destroy()
})

// Los colores de los charts se calculan al crearlos — si el tema cambia
// mientras la página ya está montada, hay que recrearlos o el texto queda
// con el color del tema anterior (invisible contra el nuevo fondo).
watch(
  () => appStore.isDark,
  () => {
    barChart?.destroy()
    donutChart?.destroy()
    initCharts()
  },
)

on('sath:nueva-solicitud', cargarDatos)
on('sath:estado-solicitud', cargarDatos)

async function cargarDatos() {
  cargando.value = true
  try {
    const res = await get('/api/solicitud/listar')
    if (!res) return
    if (!res.ok) throw new Error('Error al cargar solicitudes')
    const lista = await res.json()

    stats.value.total = lista.length
    stats.value.pendiente = lista.filter((s) => s.estadoSol === 'PENDIENTE').length
    stats.value.enMantenimiento = lista.filter((s) => s.estadoSol === 'EN_MANTENIMIENTO').length
    stats.value.finalizado = lista.filter((s) => s.estadoSol === 'FINALIZADO').length
    porMes.value = calcularPorMes(lista)
    solicitudesRecientes.value = [...lista].reverse().slice(0, 8)

    actualizarGraficas()
  } catch (e) {
    console.error(e.message)
  } finally {
    cargando.value = false
  }
}

function calcularPorMes(lista) {
  const now = new Date()
  return Array.from({ length: 6 }, (_, i) => {
    const d = new Date(now.getFullYear(), now.getMonth() - (5 - i), 1)
    const label = d.toLocaleDateString('es', { month: 'short', year: '2-digit' })
    const count = lista.filter((s) => {
      if (!s.fechaSol) return false
      const f = new Date(s.fechaSol)
      return f.getMonth() === d.getMonth() && f.getFullYear() === d.getFullYear()
    }).length
    return { label, count }
  })
}

function initCharts() {
  const barEl = document.getElementById('sath-bar-chart')
  const donutEl = document.getElementById('sath-donut-chart')
  if (!barEl || !donutEl) return

  const isDark = appStore.isDark
  const textColor = isDark ? '#cbd5e1' : '#475569'
  const gridColor = isDark ? '#1e293b' : '#e2e8f0'

  barChart = new ApexCharts(barEl, {
    chart: {
      type: 'bar',
      height: 240,
      toolbar: { show: false },
      background: 'transparent',
      fontFamily: 'Nunito, sans-serif',
      foreColor: textColor,
    },
    theme: { mode: isDark ? 'dark' : 'light' },
    colors: isDark ? ['#22d3ee'] : ['#4361ee'],
    plotOptions: { bar: { borderRadius: 6, columnWidth: '45%' } },
    dataLabels: { enabled: false },
    series: [{ name: 'Solicitudes', data: porMes.value.map((m) => m.count) }],
    xaxis: {
      categories: porMes.value.map((m) => m.label),
      axisBorder: { show: false },
      axisTicks: { show: false },
      labels: { style: { colors: textColor, fontSize: '12px' } },
    },
    yaxis: { labels: { formatter: (v) => Math.floor(v), style: { colors: textColor } }, min: 0 },
    grid: { borderColor: gridColor, strokeDashArray: 4, xaxis: { lines: { show: false } } },
    tooltip: { theme: isDark ? 'dark' : 'light', y: { formatter: (v) => `${v} solicitudes` } },
    fill: {
      type: 'gradient',
      gradient: {
        type: 'vertical',
        shadeIntensity: 0.4,
        opacityFrom: 1,
        opacityTo: 0.5,
        stops: [0, 100],
      },
    },
  })
  barChart.render()

  donutChart = new ApexCharts(donutEl, {
    chart: {
      type: 'donut',
      height: 240,
      background: 'transparent',
      fontFamily: 'Nunito, sans-serif',
    },
    theme: { mode: isDark ? 'dark' : 'light' },
    colors: isDark ? ['#fbbf24', '#22d3ee', '#4ade80'] : ['#f59e0b', '#4361ee', '#10b981'],
    labels: ['Pendiente', 'En Mantenimiento', 'Finalizado'],
    series: [stats.value.pendiente, stats.value.enMantenimiento, stats.value.finalizado],
    legend: {
      position: 'bottom',
      fontSize: '12px',
      fontFamily: 'Nunito, sans-serif',
      labels: { colors: textColor },
    },
    dataLabels: { enabled: true, style: { fontSize: '12px', fontFamily: 'Nunito, sans-serif' } },
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
              color: isDark ? '#f1f5f9' : '#1e293b',
              formatter: () => String(stats.value.total),
            },
          },
        },
      },
    },
    stroke: { width: 0 },
    tooltip: { theme: isDark ? 'dark' : 'light' },
  })
  donutChart.render()
}

function actualizarGraficas() {
  if (barChart) {
    barChart.updateSeries([{ name: 'Solicitudes', data: porMes.value.map((m) => m.count) }])
    barChart.updateOptions({ xaxis: { categories: porMes.value.map((m) => m.label) } })
  }
  if (donutChart) {
    donutChart.updateSeries([
      stats.value.pendiente,
      stats.value.enMantenimiento,
      stats.value.finalizado,
    ])
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
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Dashboard</li>
  </ul>

  <!-- Loading -->
  <SathLoader v-if="cargando && stats.total === 0" size="lg" label="Cargando Dashboard..." class="my-16" />

  <div v-else>
    <!-- Stat cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6 mb-6">
      <div class="panel h-full">
        <div class="flex items-center">
          <div class="flex h-14 w-14 shrink-0 items-center justify-center rounded-xl bg-primary/10">
            <svg class="text-primary" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
              <line x1="16" y1="13" x2="8" y2="13" />
              <line x1="16" y1="17" x2="8" y2="17" />
            </svg>
          </div>
          <div class="ltr:ml-4 rtl:mr-4 flex-1">
            <h5 class="text-2xl font-bold dark:text-white-light/90">{{ stats.total }}</h5>
            <p class="font-semibold text-white-dark">Total Solicitudes</p>
            <p class="text-xs text-white-dark/60 mt-0.5">Registros en el sistema</p>
          </div>
        </div>
      </div>

      <div class="panel h-full">
        <div class="flex items-center">
          <div class="flex h-14 w-14 shrink-0 items-center justify-center rounded-xl bg-warning/10">
            <svg class="text-warning" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
          </div>
          <div class="ltr:ml-4 rtl:mr-4 flex-1">
            <h5 class="text-2xl font-bold dark:text-white-light/90">{{ stats.pendiente }}</h5>
            <p class="font-semibold text-white-dark">Pendientes</p>
            <p class="text-xs text-white-dark/60 mt-0.5">En espera de atención</p>
          </div>
        </div>
      </div>

      <div class="panel h-full">
        <div class="flex items-center">
          <div class="flex h-14 w-14 shrink-0 items-center justify-center rounded-xl bg-info/10">
            <svg class="text-info" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z" />
            </svg>
          </div>
          <div class="ltr:ml-4 rtl:mr-4 flex-1">
            <h5 class="text-2xl font-bold dark:text-white-light/90">{{ stats.enMantenimiento }}</h5>
            <p class="font-semibold text-white-dark">En Mantenimiento</p>
            <p class="text-xs text-white-dark/60 mt-0.5">Siendo atendidas ahora</p>
          </div>
        </div>
      </div>

      <div class="panel h-full">
        <div class="flex items-center">
          <div class="flex h-14 w-14 shrink-0 items-center justify-center rounded-xl bg-success/10">
            <svg class="text-success" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
          </div>
          <div class="ltr:ml-4 rtl:mr-4 flex-1">
            <h5 class="text-2xl font-bold dark:text-white-light/90">{{ stats.finalizado }}</h5>
            <p class="font-semibold text-white-dark">Finalizadas</p>
            <p class="text-xs text-white-dark/60 mt-0.5">Completadas exitosamente</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Gráficas -->
    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-6">
      <div class="panel">
        <div class="flex items-center justify-between mb-5">
          <h5 class="text-lg font-semibold dark:text-white-light/90">Solicitudes por Mes</h5>
          <span class="badge bg-primary/10 text-primary py-1 px-3 rounded-full">Últimos 6 meses</span>
        </div>
        <div id="sath-bar-chart" />
      </div>
      <div class="panel">
        <div class="flex items-center justify-between mb-5">
          <h5 class="text-lg font-semibold dark:text-white-light/90">Distribución por Estado</h5>
        </div>
        <div id="sath-donut-chart" />
      </div>
    </div>

    <!-- Solicitudes recientes -->
    <div class="panel">
      <div class="flex items-center justify-between mb-5">
        <h5 class="text-lg font-semibold dark:text-white-light/90">Solicitudes Recientes</h5>
        <RouterLink
          to="/solicitud"
          class="btn btn-outline-primary btn-sm inline-flex items-center gap-2"
        >
          Ver todas
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="5" y1="12" x2="19" y2="12" /><polyline points="12 5 19 12 12 19" />
          </svg>
        </RouterLink>
      </div>
      <div v-if="solicitudesRecientes.length === 0" class="flex flex-col items-center justify-center py-12 text-white-dark gap-2">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" opacity="0.4">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
        </svg>
        <p class="text-sm">No hay solicitudes registradas</p>
      </div>
      <div v-else class="table-responsive">
        <table class="table-striped table-hover">
          <thead>
            <tr>
              <th>Nro. Solicitud</th>
              <th>Descripción</th>
              <th class="hidden sm:table-cell">Fecha</th>
              <th class="text-center">Estado</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in solicitudesRecientes" :key="s.idSolicitud">
              <td class="font-bold text-primary whitespace-nowrap">{{ s.nroSol || 'SOL-' + s.idSolicitud }}</td>
              <td>
                <div class="flex flex-col sm:block">
                  <span class="text-[10px] text-white-dark font-mono sm:hidden">{{ s.fechaSol || '—' }}</span>
                  <span class="block truncate max-w-[140px] sm:max-w-xs text-white-dark" :title="s.descripcionSol">
                    {{ s.descripcionSol || '—' }}
                  </span>
                </div>
              </td>
              <td class="text-white-dark text-xs hidden sm:table-cell">{{ s.fechaSol || '—' }}</td>
              <td class="text-center">
                <span :class="badgeClase(s.estadoSol)">{{ etiquetaEstado(s.estadoSol) }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
