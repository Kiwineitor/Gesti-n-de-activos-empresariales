<script setup>
import { ref } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import SathLoader from '@/components/ui/SathLoader.vue'

const { get } = useApi()
const { toast } = useToast()

const codigoBusqueda = ref('')
const historial = ref([])
const cargando = ref(false)
const buscado = ref(false)

async function buscar() {
  const codigo = codigoBusqueda.value.trim()
  if (!codigo) return
  cargando.value = true
  buscado.value = false
  historial.value = []
  try {
    const res = await get(`/api/activos/historial?codigo=${encodeURIComponent(codigo)}`)
    if (!res) return
    if (!res.ok) throw new Error('Error al consultar historial')
    historial.value = await res.json()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    cargando.value = false
    buscado.value = true
  }
}

function limpiar() {
  codigoBusqueda.value = ''
  historial.value = []
  buscado.value = false
}

function fmt(fecha) {
  if (!fecha) return null
  try {
    const [y, m, d] = String(fecha).split('T')[0].split('-')
    return `${d}/${m}/${y}`
  } catch {
    return fecha
  }
}

function labelEstado(e) {
  return (
    { PENDIENTE: 'Pendiente', EN_MANTENIMIENTO: 'En Mantenimiento', FINALIZADO: 'Finalizado' }[e] ??
    e ??
    '—'
  )
}

function badgeClaseEstado(e) {
  return (
    {
      PENDIENTE: 'sath-badge sath-badge-warning',
      EN_MANTENIMIENTO: 'sath-badge sath-badge-info',
      FINALIZADO: 'sath-badge sath-badge-success',
    }[e] ?? 'sath-badge sath-badge-secondary'
  )
}
</script>

<template>
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Historial de Activo</li>
  </ul>

  <!-- Búsqueda -->
  <div class="panel mb-6">
    <h5 class="text-lg font-semibold dark:text-white-light/90 mb-4">Historial de Activo</h5>
    <div class="flex gap-3">
      <input
        v-model="codigoBusqueda"
        type="text"
        class="form-input flex-1"
        placeholder="Ingresa el código del activo..."
        @keyup.enter="buscar"
      />
      <button class="btn btn-primary" :disabled="cargando" @click="buscar">
        Buscar
      </button>
      <button class="btn btn-outline-danger" @click="limpiar">Limpiar</button>
    </div>
  </div>

  <!-- Resultados -->
  <SathLoader v-if="cargando" size="md" label="Buscando historial..." class="my-10" />

  <div v-else-if="buscado">
    <div v-if="historial.length === 0" class="panel text-center text-white-dark py-10">
      No se encontró historial para el código ingresado.
    </div>

    <div v-else>
      <!-- Activo info -->
      <div v-if="historial[0]" class="panel mb-6">
        <div class="flex items-center gap-4">
          <div class="flex h-14 w-14 items-center justify-center rounded-xl bg-primary/10">
            <svg class="text-primary" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="2" y="3" width="20" height="14" rx="2" />
              <path d="M8 21h8M12 17v4" />
            </svg>
          </div>
          <div>
            <h5 class="text-lg font-bold dark:text-white-light/90">
              {{ historial[0].nombreActivo || codigoBusqueda }}
            </h5>
            <p class="text-white-dark text-sm">{{ historial[0].tipoActivo || '—' }}</p>
            <p class="text-white-dark text-xs">Código: {{ codigoBusqueda }}</p>
          </div>
        </div>
      </div>

      <!-- Timeline historial -->
      <div class="panel">
        <h6 class="font-semibold dark:text-white mb-4">{{ historial.length }} registro(s) encontrado(s)</h6>
        <div class="table-responsive">
          <table class="table-striped table-hover">
            <thead>
              <tr>
                <th>Solicitud</th>
                <th class="hidden sm:table-cell">Fecha</th>
                <th class="hidden md:table-cell">Descripción</th>
                <th class="hidden lg:table-cell">Fallas</th>
                <th class="hidden sm:table-cell">Técnico</th>
                <th class="text-center">Estado</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="h in historial" :key="h.idAtencion || h.idSolicitud">
                <td>
                  <div class="font-bold text-primary">{{ h.nroSol || '—' }}</div>
                  <div class="sm:hidden text-[10px] text-white-dark mt-0.5">
                    <span v-if="h.fechaSol">{{ fmt(h.fechaSol) }}</span>
                    <span v-if="h.fechaSol && h.nombreTecnico"> · </span>
                    <span v-if="h.nombreTecnico">{{ h.nombreTecnico }}</span>
                  </div>
                  <div v-if="h.descripcionSol" class="md:hidden text-[10px] text-white-dark/80 mt-0.5 truncate max-w-[180px]" :title="h.descripcionSol">
                    {{ h.descripcionSol }}
                  </div>
                </td>
                <td class="text-white-dark text-xs hidden sm:table-cell">{{ fmt(h.fechaSol) || '—' }}</td>
                <td class="hidden md:table-cell">
                  <span class="block truncate max-w-xs text-white-dark" :title="h.descripcionSol">
                    {{ h.descripcionSol || '—' }}
                  </span>
                </td>
                <td class="text-white-dark text-xs hidden lg:table-cell">
                  {{ (h.fallas || []).map((f) => f.nombreFall).join(', ') || '—' }}
                </td>
                <td class="text-white-dark hidden sm:table-cell">{{ h.nombreTecnico || '—' }}</td>
                <td class="text-center">
                  <span :class="badgeClaseEstado(h.estadoSol)">{{ labelEstado(h.estadoSol) }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>
