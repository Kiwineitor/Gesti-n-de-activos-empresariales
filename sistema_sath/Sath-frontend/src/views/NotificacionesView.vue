<script setup>
import { ref, computed, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import SathLoader from '@/components/ui/SathLoader.vue'
import Skeleton from '@/components/ui/Skeleton.vue'

const { get, patch } = useApi()
const { toast, confirm } = useToast()

const notificaciones = ref([])
const cargando = ref(false)
const filtro = ref('todas')

const filtradas = computed(() => {
  if (filtro.value === 'no-leidas') return notificaciones.value.filter(n => !n.leida)
  if (filtro.value === 'leidas')    return notificaciones.value.filter(n =>  n.leida)
  return notificaciones.value
})

const noLeidas = computed(() => notificaciones.value.filter(n => !n.leida).length)

onMounted(cargar)

async function cargar() {
  cargando.value = true
  try {
    const res = await get('/api/notificaciones')
    if (!res?.ok) throw new Error('Error al cargar notificaciones')
    notificaciones.value = await res.json()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    cargando.value = false
  }
}

async function marcarLeida(n) {
  if (n.leida) return
  n.leida = true
  try { await patch(`/api/notificaciones/${n.id}/leer`) } catch {}
}

async function marcarTodas() {
  notificaciones.value.forEach(n => (n.leida = true))
  try { await patch('/api/notificaciones/leer-todas') } catch {}
  toast('Todas marcadas como leídas', 'success')
}

async function limpiar() {
  const r = await confirm('¿Eliminar todas las notificaciones?')
  if (!r.isConfirmed) return
  notificaciones.value = []
  toast('Notificaciones eliminadas', 'success')
}

function iconoTipo(tipo) {
  return tipo === 'nueva' ? 'nueva' : 'estado'
}
</script>

<template>
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Notificaciones</li>
  </ul>

  <!-- Cabecera con stats y acciones -->
  <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
    <div class="flex items-center gap-3">
      <h5 class="text-xl font-bold dark:text-white-light/90">Historial de Notificaciones</h5>
      <span v-if="noLeidas > 0" class="sath-badge sath-badge-danger">{{ noLeidas }} sin leer</span>
    </div>
    <div class="flex flex-wrap gap-2">
      <button
        v-if="noLeidas > 0"
        type="button"
        class="btn btn-outline-primary btn-sm"
        @click="marcarTodas"
      >
        <svg class="inline w-4 h-4 mr-1 align-middle" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="20 6 9 17 4 12"/>
        </svg>
        Marcar todas leídas
      </button>
      <button
        type="button"
        class="btn btn-outline-secondary btn-sm"
        @click="cargar"
        :disabled="cargando"
      >
        <svg class="inline w-4 h-4 mr-1 align-middle" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M4 4v5h5M20 20v-5h-5"/>
          <path d="M20.49 9A9 9 0 0 0 5.64 5.64M3.51 15a9 9 0 0 0 14.85 3.36"/>
        </svg>
        Recargar
      </button>
      <button
        v-if="notificaciones.length > 0"
        type="button"
        class="btn btn-outline-danger btn-sm"
        @click="limpiar"
      >
        <svg class="inline w-4 h-4 mr-1 align-middle" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="3 6 5 6 21 6"/>
          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
        </svg>
        Limpiar todo
      </button>
    </div>
  </div>

  <!-- Filtros -->
  <div class="flex gap-2 mb-5">
    <button
      type="button"
      class="btn btn-sm"
      :class="filtro === 'todas' ? 'btn-primary' : 'btn-outline-secondary'"
      @click="filtro = 'todas'"
    >
      Todas ({{ notificaciones.length }})
    </button>
    <button
      type="button"
      class="btn btn-sm"
      :class="filtro === 'no-leidas' ? 'btn-primary' : 'btn-outline-secondary'"
      @click="filtro = 'no-leidas'"
    >
      Sin leer ({{ noLeidas }})
    </button>
    <button
      type="button"
      class="btn btn-sm"
      :class="filtro === 'leidas' ? 'btn-primary' : 'btn-outline-secondary'"
      @click="filtro = 'leidas'"
    >
      Leídas ({{ notificaciones.length - noLeidas }})
    </button>
  </div>

  <!-- Loading -->
  <div v-if="cargando" class="space-y-3">
    <SathLoader size="md" label="Cargando notificaciones..." class="mb-8" />
    <div v-for="i in 5" :key="i" class="panel !p-4 flex gap-4">
      <Skeleton width="40px" height="40px" circle />
      <div class="flex-1 space-y-2">
        <div class="flex justify-between">
          <Skeleton width="40%" height="1rem" />
          <Skeleton width="60px" height="0.8rem" />
        </div>
        <Skeleton width="90%" height="1.5rem" />
      </div>
    </div>
  </div>

  <!-- Sin resultados -->
  <div
    v-else-if="filtradas.length === 0"
    class="panel flex flex-col items-center justify-center py-16 gap-3 text-white-dark"
  >
    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" opacity="0.4">
      <path d="M19.0001 9.7041V9C19.0001 5.13401 15.8661 2 12.0001 2C8.13407 2 5.00006 5.13401 5.00006 9V9.7041C5.00006 10.5491 4.74995 11.3752 4.28123 12.0783L3.13263 13.8012C2.08349 15.3749 2.88442 17.5139 4.70913 18.0116C9.48258 19.3134 14.5175 19.3134 19.291 18.0116C21.1157 17.5139 21.9166 15.3749 20.8675 13.8012L19.7189 12.0783C19.2502 11.3752 19.0001 10.5491 19.0001 9.7041Z"/>
      <path d="M7.5 19C8.15503 20.7478 9.92246 22 12 22C14.0775 22 15.845 20.7478 16.5 19"/>
    </svg>
    <p class="font-semibold">Sin notificaciones</p>
    <p class="text-xs">
      {{ filtro === 'no-leidas' ? 'No tienes notificaciones sin leer.' : filtro === 'leidas' ? 'No hay notificaciones leídas.' : 'No hay notificaciones registradas.' }}
    </p>
  </div>

  <!-- Lista -->
  <div v-else class="space-y-3">
    <div
      v-for="n in filtradas"
      :key="n.id"
      class="panel !p-0 overflow-hidden cursor-pointer transition-all hover:shadow-md"
      :class="!n.leida ? 'border-l-4 border-success' : ''"
      @click="marcarLeida(n)"
    >
      <div class="flex items-start gap-4 px-4 py-4">
        <!-- Icono tipo -->
        <div
          class="mt-0.5 flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full"
          :class="iconoTipo(n.tipo) === 'nueva' ? 'bg-success/15 text-success' : 'bg-info/15 text-info'"
        >
          <svg v-if="iconoTipo(n.tipo) === 'nueva'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <path d="M12 5v14M5 12h14"/>
          </svg>
          <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 4v5h5M20 20v-5h-5"/>
            <path d="M20.49 9A9 9 0 0 0 5.64 5.64M3.51 15a9 9 0 0 0 14.85 3.36"/>
          </svg>
        </div>

        <!-- Contenido -->
        <div class="min-w-0 flex-1">
          <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-1">
            <p class="text-sm font-semibold dark:text-white leading-tight">{{ n.titulo }}</p>
            <span class="text-[11px] text-white-dark whitespace-nowrap">{{ n.tiempo }}</span>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400 leading-relaxed mt-1 break-words">
            {{ n.descripcion }}
          </p>
        </div>

        <!-- Indicador no leída -->
        <div class="flex-shrink-0 flex items-center pt-1">
          <span v-if="!n.leida" class="h-2.5 w-2.5 rounded-full bg-success" title="Sin leer" />
          <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-white-dark/40">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
      </div>
    </div>
  </div>
</template>