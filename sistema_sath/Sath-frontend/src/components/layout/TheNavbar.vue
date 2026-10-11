<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { useApi } from '@/composables/useApi'
import { useWebSocket } from '@/composables/useWebSocket'
import { useSession } from '@/composables/useSession'
import { useRouter, useRoute } from 'vue-router'

const auth = useAuthStore()
const appStore = useAppStore()
const router = useRouter()
const route = useRoute()
const { get, patch } = useApi()
const { on } = useWebSocket()
const session = useSession()

const menuOpen = ref(false)
const notiOpen = ref(false)
const windowWidth = ref(window.innerWidth)
function onResize() { windowWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))

watch(() => route.path, () => {
  menuOpen.value = false
  notiOpen.value = false
})
const cargandoNoti = ref(false)
const notificaciones = ref([])
const noLeidas = computed(() => notificaciones.value.filter((n) => !n.leida).length)

async function cargarNotificaciones() {
  if (cargandoNoti.value) return
  cargandoNoti.value = true
  try {
    const res = await get('/api/notificaciones')
    if (res?.ok) notificaciones.value = await res.json()
  } catch {} finally { cargandoNoti.value = false }
}

// Carga notificaciones al abrir el panel por primera vez
watch(notiOpen, (v) => { if (v && notificaciones.value.length === 0) cargarNotificaciones() })

on('sath:nueva-solicitud', (e) => {
  const d = e.detail || {}
  notificaciones.value.unshift({
    id: Date.now() + Math.random(),
    tipo: 'nueva',
    titulo: 'Nueva solicitud',
    descripcion: `Solicitud #${d.nroSol ?? d.idSolicitud ?? '?'} — ${d.descripcionSol ?? ''}`.trim().replace(/—\s*$/, ''),
    tiempo: new Date().toLocaleTimeString('es-BO', { hour: '2-digit', minute: '2-digit' }),
    leida: false,
  })
})

on('sath:estado-solicitud', (e) => {
  const d = e.detail || {}
  notificaciones.value.unshift({
    id: Date.now() + Math.random(),
    tipo: 'estado',
    titulo: 'Cambio de estado',
    descripcion: `Solicitud #${d.nroSol ?? d.idSolicitud ?? '?'} → ${d.estadoSol ?? ''}`,
    tiempo: new Date().toLocaleTimeString('es-BO', { hour: '2-digit', minute: '2-digit' }),
    leida: false,
  })
})

async function marcarLeida(n) {
  if (n.leida) return
  n.leida = true
  if (n.id && typeof n.id === 'number' && n.id > 1e10) return // id generado localmente (WS)
  try { await patch(`/api/notificaciones/${n.id}/leer`) } catch {}
}

async function marcarTodas() {
  notificaciones.value.forEach((n) => (n.leida = true))
  try { await patch('/api/notificaciones/leer-todas') } catch {}
}

function limpiar() {
  notificaciones.value = []
  notiOpen.value = false
}

function irPerfil() {
  menuOpen.value = false
  router.push('/perfil')
}

function cerrarSesion() {
  auth.logout()
}
</script>

<template>
  <header class="z-40" :class="{ dark: appStore.semidark && appStore.menu === 'horizontal' }">
    <div class="shadow-sm">
      <div class="relative flex w-full items-center bg-white px-5 py-2.5 dark:bg-[#0e1726]">
        <!-- Logo móvil -->
        <div class="horizontal-logo flex items-center justify-between ltr:mr-2 rtl:ml-2 lg:hidden">
          <RouterLink to="/" class="main-logo flex shrink-0 items-center">
            <img class="ltr:-ml-1 rtl:-mr-1 h-8 w-auto flex-none" src="/images/logo-horizontal.svg" alt="ServiceDesk" />
          </RouterLink>
          <button
            class="collapse-icon flex flex-none rounded-full bg-white-light/40 p-2 hover:bg-white-light/90 hover:text-primary ltr:ml-2 rtl:mr-2 dark:bg-dark/40 dark:text-[#d0d2d6] dark:hover:bg-dark/60 dark:hover:text-primary lg:hidden"
            @click="appStore.toggleSidebar()"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M20 7L4 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
              <path
                opacity="0.5"
                d="M20 12L4 12"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
              />
              <path
                d="M20 17L4 17"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
              />
            </svg>
          </button>
        </div>

        <div
          class="flex items-center space-x-1.5 ltr:ml-auto rtl:mr-auto rtl:space-x-reverse dark:text-[#d0d2d6] lg:space-x-2"
        >
          <!-- Toggle tema: muestra el icono del modo al que vas a cambiar -->
          <button
            :title="appStore.theme === 'light' ? 'Cambiar a modo oscuro' : 'Cambiar a modo claro'"
            class="flex items-center rounded-full bg-white-light/40 p-2 hover:bg-white-light/90 hover:text-primary dark:bg-dark/40 dark:hover:bg-dark/60"
            @click="appStore.toggleTheme(appStore.theme === 'light' ? 'dark' : 'light')"
          >
            <!-- Luna: se muestra en modo claro → click para ir a oscuro -->
            <svg v-if="appStore.theme === 'light'" width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path
                d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"
                stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"
              />
            </svg>
            <!-- Sol: se muestra en modo oscuro → click para ir a claro -->
            <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="5" stroke="currentColor" stroke-width="1.5" />
              <path d="M12 2V4M12 20V22M4 12H2M22 12H20M5.64 5.64L7.05 7.05M16.95 16.95L18.36 18.36M5.64 18.36L7.05 16.95M16.95 7.05L18.36 5.64"
                stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
            </svg>
          </button>

          <!-- Chip countdown sesión -->
          <Transition name="fade">
            <button
              v-if="session.isWarning.value"
              class="flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-bold font-mono transition-colors duration-500"
              :class="session.secondsLeft.value <= 120
                ? 'bg-danger/15 text-danger hover:bg-danger/25'
                : 'bg-warning/15 text-warning hover:bg-warning/25'"
              :title="'Sesión por vencer — click para renovar'"
              @click="session.showModal.value = true"
            >
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                <circle cx="12" cy="12" r="10" />
                <path d="M12 6v6l4 2" />
              </svg>
              {{ session.minutesLeft.value }}
            </button>
          </Transition>

          <!-- Notificaciones -->
          <div class="relative">
            <button
              class="relative flex items-center rounded-full bg-white-light/40 p-2 hover:bg-white-light/90 hover:text-primary dark:bg-dark/40 dark:hover:bg-dark/60"
              @click="notiOpen = !notiOpen"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path
                  d="M19.0001 9.7041V9C19.0001 5.13401 15.8661 2 12.0001 2C8.13407 2 5.00006 5.13401 5.00006 9V9.7041C5.00006 10.5491 4.74995 11.3752 4.28123 12.0783L3.13263 13.8012C2.08349 15.3749 2.88442 17.5139 4.70913 18.0116C9.48258 19.3134 14.5175 19.3134 19.291 18.0116C21.1157 17.5139 21.9166 15.3749 20.8675 13.8012L19.7189 12.0783C19.2502 11.3752 19.0001 10.5491 19.0001 9.7041Z"
                  stroke="currentColor"
                  stroke-width="1.5"
                />
                <path
                  d="M7.5 19C8.15503 20.7478 9.92246 22 12 22C14.0775 22 15.845 20.7478 16.5 19"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                />
              </svg>
              <span
                v-if="noLeidas > 0"
                class="absolute -top-1 -right-1 flex h-[18px] min-w-[18px] items-center justify-center rounded-full bg-danger px-1 text-[10px] font-bold text-white leading-none"
              >{{ noLeidas > 9 ? '9+' : noLeidas }}</span>
              <span v-else class="absolute top-0 flex h-3 w-3 ltr:right-0 rtl:left-0">
                <span class="absolute -top-[3px] inline-flex h-full w-full animate-ping rounded-full bg-success/50 opacity-75 ltr:-left-[3px] rtl:-right-[3px]" />
                <span class="relative inline-flex h-[6px] w-[6px] rounded-full bg-success" />
              </span>
            </button>

            <!-- Panel notificaciones -->
            <Transition name="fade">
              <div
                v-if="notiOpen"
                v-click-outside="() => (notiOpen = false)"
                class="fixed ltr:right-2 rtl:left-2 top-16 w-[calc(100vw-1rem)] z-50 sm:absolute sm:top-12 sm:ltr:right-0 sm:rtl:left-0 rounded-xl bg-white dark:bg-[#1b2e4b] border border-white-light/60 dark:border-[#253b5e] overflow-hidden"
                :style="windowWidth >= 640 ? 'width: 420px' : ''"
              >
                <div
                  class="flex items-center justify-between px-4 py-3 border-b border-white-light dark:border-[#253b5e]"
                >
                  <span class="text-sm font-semibold dark:text-white">Notificaciones</span>
                  <button
                    v-if="noLeidas > 0"
                    class="text-xs text-primary hover:underline"
                    @click="marcarTodas"
                  >
                    Marcar todas
                  </button>
                </div>
                <ul class="max-h-72 overflow-y-auto py-1.5 space-y-0.5 px-2">
                  <li
                    v-if="notificaciones.length === 0"
                    class="px-4 py-8 text-center text-sm text-gray-400"
                  >
                    Sin notificaciones nuevas
                  </li>
                  <li
                    v-for="n in notificaciones"
                    :key="n.id"
                    class="flex items-start gap-2.5 px-3 py-2.5 rounded-lg cursor-pointer transition-colors"
                    :class="
                      !n.leida
                        ? 'bg-success/5 dark:bg-success/10 border-l-2 border-success'
                        : 'hover:bg-gray-50 dark:hover:bg-dark/30'
                    "
                    @click="marcarLeida(n)"
                  >
                    <div
                      class="mt-0.5 flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full"
                      :class="
                        n.tipo === 'nueva'
                          ? 'bg-success/15 text-success'
                          : 'bg-info/15 text-info'
                      "
                    >
                      <svg
                        v-if="n.tipo === 'nueva'"
                        width="13"
                        height="13"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2.5"
                        stroke-linecap="round"
                      >
                        <path d="M12 5v14M5 12h14" />
                      </svg>
                      <svg
                        v-else
                        width="13"
                        height="13"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      >
                        <path d="M4 4v5h5M20 20v-5h-5" />
                        <path d="M20.49 9A9 9 0 0 0 5.64 5.64M3.51 15a9 9 0 0 0 14.85 3.36" />
                      </svg>
                    </div>
                    <div class="min-w-0 flex-1">
                      <p class="text-[13px] font-medium dark:text-white leading-tight mb-0.5">
                        {{ n.titulo }}
                      </p>
                      <p class="text-xs text-gray-500 dark:text-gray-400 break-words leading-relaxed">
                        {{ n.descripcion }}
                      </p>
                      <span class="text-[11px] text-gray-400">{{ n.tiempo }}</span>
                    </div>
                    <span
                      v-if="!n.leida"
                      class="mt-1.5 h-2 w-2 flex-shrink-0 rounded-full bg-success"
                    />
                  </li>
                </ul>
                <div class="border-t border-white-light dark:border-[#253b5e] px-4 py-2 flex items-center justify-between gap-2">
                  <RouterLink
                    to="/notificaciones"
                    class="text-xs text-primary hover:underline"
                    @click="notiOpen = false"
                  >
                    Ver historial
                  </RouterLink>
                  <button
                    v-if="notificaciones.length > 0"
                    class="text-xs text-danger hover:underline"
                    @click="limpiar"
                  >
                    Limpiar todo
                  </button>
                </div>
              </div>
            </Transition>
          </div>

          <!-- Menú usuario -->
          <div class="relative">
            <button
              class="group relative flex h-9 w-9 items-center justify-center rounded-full bg-primary text-white font-bold text-sm"
              @click="menuOpen = !menuOpen"
            >
              {{ auth.inicial }}
            </button>

            <Transition name="fade">
              <ul
                v-if="menuOpen"
                v-click-outside="() => (menuOpen = false)"
                class="absolute top-11 z-50 w-[230px] !py-0 font-semibold text-dark ltr:right-0 rtl:left-0 dark:text-white-dark dark:text-white-light/90 rounded-xl bg-white dark:bg-[#1b2e4b] border border-white-light/60 dark:border-[#253b5e] overflow-hidden shadow-[0_5px_25px_0_rgba(94,92,154,0.1)]"
              >
                <li>
                  <div class="flex items-center px-4 py-4">
                    <div
                      class="flex h-10 w-10 flex-none items-center justify-center rounded-md bg-primary text-white font-bold"
                    >
                      {{ auth.inicial }}
                    </div>
                    <div class="truncate ltr:pl-4 rtl:pr-4">
                      <h4 class="text-base">{{ auth.nombre }}</h4>
                      <span class="block text-xs text-black/50 dark:text-dark-light/50"
                        >@{{ auth.username }}</span
                      >
                      <span
                        v-if="auth.cargo"
                        class="block text-xs text-primary/80 font-medium truncate"
                        >{{ auth.cargo }}</span
                      >
                    </div>
                  </div>
                </li>
                <li>
                  <button
                    class="w-full text-left flex items-center px-4 py-2 dark:hover:text-white"
                    @click="irPerfil"
                  >
                    <svg
                      class="h-4.5 w-4.5 shrink-0 ltr:mr-2 rtl:ml-2"
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                    >
                      <circle cx="12" cy="6" r="4" stroke="currentColor" stroke-width="1.5" />
                      <path
                        opacity="0.5"
                        d="M20 17.5C20 19.9853 20 22 12 22C4 22 4 19.9853 4 17.5C4 15.0147 7.58172 13 12 13C16.4183 13 20 15.0147 20 17.5Z"
                        stroke="currentColor"
                        stroke-width="1.5"
                      />
                    </svg>
                    Mi Perfil
                  </button>
                </li>
                <li class="border-t border-white-light dark:border-white-light/10">
                  <button
                    class="w-full text-left flex items-center px-4 py-3 text-danger"
                    @click="cerrarSesion"
                  >
                    <svg
                      class="h-4.5 w-4.5 shrink-0 rotate-90 ltr:mr-2 rtl:ml-2"
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                    >
                      <path
                        opacity="0.5"
                        d="M17 9.00195C19.175 9.01406 20.3529 9.11051 21.1213 9.8789C22 10.7576 22 12.1718 22 15.0002V16.0002C22 18.8286 22 20.2429 21.1213 21.1215C20.2426 22.0002 18.8284 22.0002 16 22.0002H8C5.17157 22.0002 3.75736 22.0002 2.87868 21.1215C2 20.2429 2 18.8286 2 16.0002L2 15.0002C2 12.1718 2 10.7576 2.87868 9.87889C3.64706 9.11051 4.82497 9.01406 7 9.00195"
                        stroke="currentColor"
                        stroke-width="1.5"
                        stroke-linecap="round"
                      />
                      <path
                        d="M12 15L12 2M12 2L15 5.5M12 2L9 5.5"
                        stroke="currentColor"
                        stroke-width="1.5"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                    </svg>
                    Cerrar Sesión
                  </button>
                </li>
              </ul>
            </Transition>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s, transform 0.15s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
