no <script setup>
import { ref, watch, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { useRoute } from 'vue-router'

const auth     = useAuthStore()
const appStore = useAppStore()
const route    = useRoute()

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}

watch(() => route.path, () => {
  if (window.innerWidth < 1024) appStore.sidebar = false
  autoExpandSection()
})

// ── Secciones colapsables ──────────────────────────────────────
const secciones = ref({
  gestion:       true,
  operaciones:   false,
  administracion: false,
  reportes:      false,
  developer:     false,
})

const rutasGestion       = ['/solicitud', '/solicitud/manual', '/atenciones', '/activo', '/historial-activo']
const rutasOperaciones   = ['/tecnico-asignado', '/administrativo']
const rutasAdministracion = ['/usuario', '/tipo-servicio', '/servicio', '/tipo-activo', '/falla', '/configuracion']
const rutasReportes      = ['/dashboard-tecnico', '/reportes', '/reportes-legacy']
const rutasDeveloper     = ['/logs', '/logs-vue']

function perteneceA(rutas) {
  return rutas.some(r => route.path === r || route.path.startsWith(r + '/'))
}

function autoExpandSection() {
  if (perteneceA(rutasGestion))        secciones.value.gestion        = true
  if (perteneceA(rutasOperaciones))    secciones.value.operaciones     = true
  if (perteneceA(rutasAdministracion)) secciones.value.administracion  = true
  if (perteneceA(rutasReportes))       secciones.value.reportes        = true
  if (perteneceA(rutasDeveloper))      secciones.value.developer       = true
}

autoExpandSection()

function toggle(key) {
  secciones.value[key] = !secciones.value[key]
}

const showOperaciones    = computed(() => auth.hasModuloAccess('TECNICOS') || auth.hasModuloAccess('ADMINISTRATIVOS'))
const showAdministracion = computed(() => auth.hasModuloAccess('USUARIOS') || auth.hasModuloAccess('TIPOS_SERVICIO') || auth.hasModuloAccess('SERVICIOS') || auth.hasModuloAccess('TIPOS_ACTIVO') || auth.hasModuloAccess('FALLAS'))
const showReportes       = computed(() => auth.hasModuloAccess('REPORTES') || auth.isTecnico)
</script>

<template>
  <div :class="{ 'dark text-white-dark': appStore.semidark }">
    <nav class="sidebar fixed bottom-0 top-0 z-50 h-full min-h-screen w-[260px] shadow-[5px_0_25px_0_rgba(94,92,154,0.1)] transition-all duration-300">
      <div class="h-full bg-white dark:bg-[#0e1726] flex flex-col">

        <!-- Logo -->
        <div class="flex items-center justify-between px-4 py-3 flex-shrink-0">
          <RouterLink to="/" class="main-logo flex shrink-0 items-center">
            <div class="ml-[5px] w-8 flex-none rounded bg-white p-1 dark:bg-white">
              <img class="w-full" src="/images/Logo_Head.png" alt="SATH" />
            </div>
            <span class="align-middle text-2xl font-semibold ltr:ml-1.5 rtl:mr-1.5 dark:text-white-light lg:inline">SATH</span>
          </RouterLink>
          <button
            class="collapse-icon flex h-10 w-10 items-center justify-center rounded-full transition duration-300 hover:bg-gray-500/10 dark:text-white-light dark:hover:bg-dark-light/10"
            @click="appStore.toggleSidebar()"
          >
            <svg class="m-auto h-5 w-5" width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M13 19L7 12L13 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              <path opacity="0.5" d="M16.9998 19L10.9998 12L16.9998 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>

        <!-- Menú scrollable -->
        <ul
          class="perfect-scrollbar relative flex-1 space-y-0.5 overflow-y-auto overflow-x-hidden p-4 py-0 font-semibold"
          style="padding-bottom: max(16px, env(safe-area-inset-bottom))"
        >

          <!-- Dashboard (siempre visible) -->
          <li class="menu nav-item">
            <RouterLink to="/" class="nav-link group min-h-[48px]" :class="{ active: route.path === '/' }">
              <div class="flex items-center">
                <svg class="shrink-0 group-hover:!text-primary" width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path opacity="0.5" d="M2 12.2039C2 9.91549 2 8.77128 2.5192 7.82274C3.0384 6.87421 3.98695 6.28551 5.88403 5.10813L7.88403 3.86687C9.88939 2.62229 10.8921 2 12 2C13.1079 2 14.1106 2.62229 16.116 3.86687L18.116 5.10812C20.0131 6.28551 20.9616 6.87421 21.4808 7.82274C22 8.77128 22 9.91549 22 12.2039V13.725C22 17.6258 22 19.5763 20.8284 20.7881C19.6569 22 17.7712 22 14 22H10C6.22876 22 4.34315 22 3.17157 20.7881C2 19.5763 2 17.6258 2 13.725V12.2039Z" fill="currentColor"/>
                  <path d="M9 17.25C8.58579 17.25 8.25 17.5858 8.25 18C8.25 18.4142 8.58579 18.75 9 18.75H15C15.4142 18.75 15.75 18.4142 15.75 18C15.75 17.5858 15.4142 17.25 15 17.25H9Z" fill="currentColor"/>
                </svg>
                <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Dashboard</span>
              </div>
            </RouterLink>
          </li>

          <!-- ══ GESTIÓN ══ -->
          <li class="menu nav-item">
            <button
              type="button"
              class="nav-link group min-h-[44px] w-full"
              :class="{ active: perteneceA(rutasGestion) }"
              @click="toggle('gestion')"
            >
              <span class="flex items-center flex-1">
                <svg class="shrink-0 group-hover:!text-primary" width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path opacity="0.5" d="M2 12h20M2 7h20M2 17h20" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  <path d="M5 7V5a1 1 0 011-1h12a1 1 0 011 1v2" stroke="currentColor" stroke-width="1.5"/>
                </svg>
                <span class="ltr:pl-3 rtl:pr-3 font-extrabold text-xs uppercase tracking-wider text-warning dark:text-warning">Gestión</span>
                <svg class="ml-auto shrink-0 transition-transform duration-200" :class="secciones.gestion ? 'rotate-180' : ''" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M6 9l6 6 6-6"/>
                </svg>
              </span>
            </button>
            <Transition name="accordion">
              <ul v-show="secciones.gestion" class="sub-menu ml-4 space-y-0.5 border-l border-[#e0e6ed] dark:border-[#1b2e4b] pl-3 mt-0.5 mb-1">

                <li class="menu nav-item">
                  <RouterLink to="/solicitud" class="nav-link group min-h-[40px]" :class="{ active: isActive('/solicitud') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path fill-rule="evenodd" clip-rule="evenodd" d="M18 14C18 18.4183 14.4183 22 10 22C8.76449 22 7.5944 21.7199 6.54976 21.2198C6.19071 21.0479 5.78393 20.9876 5.39939 21.0904L4.17335 21.4185C3.20701 21.677 2.32295 20.793 2.58151 19.8267L2.90955 18.6006C3.01245 18.2161 2.95209 17.8093 2.7802 17.4502C2.28008 16.4056 2 15.2355 2 14C2 9.58172 5.58172 6 10 6C14.4183 6 18 9.58172 18 14ZM6.5 15C7.05228 15 7.5 14.5523 7.5 14C7.5 13.4477 7.05228 13 6.5 13C5.94772 13 5.5 13.4477 5.5 14C5.5 14.5523 5.94772 15 6.5 15ZM10 15C10.5523 15 11 14.5523 11 14C11 13.4477 10.5523 13 10 13C9.44772 13 9 13.4477 9 14C9 14.5523 9.44772 15 10 15ZM13.5 15C14.0523 15 14.5 14.5523 14.5 14C14.5 13.4477 14.0523 13 13.5 13C12.9477 13 12.5 13.4477 12.5 14C12.5 14.5523 12.9477 15 13.5 15Z" fill="currentColor"/>
                        <path opacity="0.6" d="M17.9842 14.5084C18.0921 14.4638 18.1986 14.4163 18.3035 14.3661C18.5952 14.2264 18.9257 14.1774 19.2381 14.261L20.2343 14.5275C21.0194 14.7376 21.7377 14.0193 21.5277 13.2342L21.2611 12.238C21.1775 11.9256 21.2266 11.595 21.3662 11.3033C21.7726 10.4545 22.0001 9.50385 22.0001 8.5C22.0001 4.91015 19.09 2 15.5001 2C12.7901 2 10.4674 3.6585 9.4917 6.0159C9.65982 6.00535 9.82936 6 10.0001 6C14.4184 6 18.0001 9.58172 18.0001 14C18.0001 14.1708 17.9948 14.3403 17.9842 14.5084Z" fill="currentColor"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Solicitudes</span>
                    </div>
                  </RouterLink>
                </li>

                <li class="menu nav-item">
                  <RouterLink to="/solicitud/manual" class="nav-link group min-h-[40px]" :class="{ active: isActive('/solicitud/manual') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path opacity="0.5" d="M12 20h9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                        <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Solicitud Manual</span>
                    </div>
                  </RouterLink>
                </li>

                <li v-if="auth.hasModuloAccess('ATENCIONES') || auth.isTecnico" class="menu nav-item">
                  <RouterLink to="/atenciones" class="nav-link group min-h-[40px]" :class="{ active: isActive('/atenciones') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path opacity="0.5" d="M3 6C3 4.34315 4.34315 3 6 3H18C19.6569 3 21 4.34315 21 6V18C21 19.6569 19.6569 21 18 21H6C4.34315 21 3 19.6569 3 18V6Z" fill="currentColor"/>
                        <path d="M7 9C7 8.44772 7.44772 8 8 8H16C16.5523 8 17 8.44772 17 9C17 9.55228 16.5523 10 16 10H8C7.44772 10 7 9.55228 7 9ZM7 12C7 11.4477 7.44772 11 8 11H13C13.5523 11 14 11.4477 14 12C14 12.5523 13.5523 13 13 13H8C7.44772 13 7 12.5523 7 12ZM7 15C7 14.4477 7.44772 14 8 14H11C11.5523 14 12 14.4477 12 15C12 15.5523 11.5523 16 11 16H8C7.44772 16 7 15.5523 7 15Z" fill="currentColor"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Atenciones</span>
                    </div>
                  </RouterLink>
                </li>

                <li v-if="auth.hasModuloAccess('ACTIVOS') || auth.isAdmin" class="menu nav-item">
                  <RouterLink to="/activo" class="nav-link group min-h-[40px]" :class="{ active: isActive('/activo') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <rect x="2" y="3" width="20" height="14" rx="2" stroke="currentColor" stroke-width="1.5" fill="none" opacity="0.5"/>
                        <line x1="8" y1="21" x2="16" y2="21" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                        <line x1="12" y1="17" x2="12" y2="21" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                        <path d="M7 8h10M7 11h7M7 14h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Activos</span>
                    </div>
                  </RouterLink>
                </li>

                <li v-if="auth.hasModuloAccess('HISTORIAL') || auth.isTecnico" class="menu nav-item">
                  <RouterLink to="/historial-activo" class="nav-link group min-h-[40px]" :class="{ active: isActive('/historial-activo') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path opacity="0.5" d="M3 6C3 4.34315 4.34315 3 6 3H18C19.6569 3 21 4.34315 21 6V18C21 19.6569 19.6569 21 18 21H6C4.34315 21 3 19.6569 3 18V6Z" fill="currentColor"/>
                        <path d="M7 9h10M7 12h7M7 15h5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Historial de Activo</span>
                    </div>
                  </RouterLink>
                </li>

              </ul>
            </Transition>
          </li>

          <!-- ══ OPERACIONES ══ -->
          <li v-if="showOperaciones" class="menu nav-item">
            <button
              type="button"
              class="nav-link group min-h-[44px] w-full"
              :class="{ active: perteneceA(rutasOperaciones) }"
              @click="toggle('operaciones')"
            >
              <span class="flex items-center flex-1">
                <svg class="shrink-0 group-hover:!text-primary" width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <circle opacity="0.5" cx="15" cy="6" r="3" fill="currentColor"/>
                  <ellipse opacity="0.5" cx="16" cy="17" rx="5" ry="3" fill="currentColor"/>
                  <circle cx="9.001" cy="6" r="4" fill="currentColor"/>
                  <ellipse cx="9.001" cy="17.001" rx="7" ry="4" fill="currentColor"/>
                </svg>
                <span class="ltr:pl-3 rtl:pr-3 font-extrabold text-xs uppercase tracking-wider text-warning dark:text-warning">Operaciones</span>
                <svg class="ml-auto shrink-0 transition-transform duration-200" :class="secciones.operaciones ? 'rotate-180' : ''" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M6 9l6 6 6-6"/>
                </svg>
              </span>
            </button>
            <Transition name="accordion">
              <ul v-show="secciones.operaciones" class="sub-menu ml-4 space-y-0.5 border-l border-[#e0e6ed] dark:border-[#1b2e4b] pl-3 mt-0.5 mb-1">

                <li v-if="auth.hasModuloAccess('TECNICOS')" class="menu nav-item">
                  <RouterLink to="/tecnico-asignado" class="nav-link group min-h-[40px]" :class="{ active: isActive('/tecnico-asignado') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path opacity="0.5" d="M12 2C9.38 2 7.25 4.13 7.25 6.75C7.25 9.32 9.26 11.4 11.88 11.49C11.96 11.48 12.04 11.48 12.1 11.49C14.73 11.4 16.74 9.32 16.75 6.75C16.75 4.13 14.62 2 12 2Z" fill="currentColor"/>
                        <path d="M17.08 14.15C14.29 12.29 9.74 12.29 6.93 14.15C5.66 15 4.96 16.15 4.96 17.38C4.96 18.61 5.66 19.75 6.92 20.59C8.32 21.53 10.16 22 12 22C13.84 22 15.68 21.53 17.08 20.59C18.34 19.74 19.04 18.6 19.04 17.36C19.03 16.13 18.34 14.99 17.08 14.15Z" fill="currentColor"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Técnicos Asignados</span>
                    </div>
                  </RouterLink>
                </li>

                <li v-if="auth.hasModuloAccess('ADMINISTRATIVOS')" class="menu nav-item">
                  <RouterLink to="/administrativo" class="nav-link group min-h-[40px]" :class="{ active: isActive('/administrativo') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <circle opacity="0.5" cx="15" cy="6" r="3" fill="currentColor"/>
                        <ellipse opacity="0.5" cx="16" cy="17" rx="5" ry="3" fill="currentColor"/>
                        <circle cx="9.001" cy="6" r="4" fill="currentColor"/>
                        <ellipse cx="9.001" cy="17.001" rx="7" ry="4" fill="currentColor"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Administrativos</span>
                    </div>
                  </RouterLink>
                </li>

              </ul>
            </Transition>
          </li>

          <!-- ══ ADMINISTRACIÓN ══ -->
          <li v-if="showAdministracion" class="menu nav-item">
            <button
              type="button"
              class="nav-link group min-h-[44px] w-full"
              :class="{ active: perteneceA(rutasAdministracion) }"
              @click="toggle('administracion')"
            >
              <span class="flex items-center flex-1">
                <svg class="shrink-0 group-hover:!text-primary" width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path opacity="0.5" d="M2 16C2 13.1716 2 11.7574 2.87868 10.8787C3.75736 10 5.17157 10 8 10H16C18.8284 10 20.2426 10 21.1213 10.8787C22 11.7574 22 13.1716 22 16C22 18.8284 22 20.2426 21.1213 21.1213C20.2426 22 18.8284 22 16 22H8C5.17157 22 3.75736 22 2.87868 21.1213C2 20.2426 2 18.8284 2 16Z" fill="currentColor"/>
                  <path d="M8 17c.55 0 1-.45 1-1s-.45-1-1-1-1 .45-1 1 .45 1 1 1zm4 0c.55 0 1-.45 1-1s-.45-1-1-1-1 .45-1 1 .45 1 1 1zm4 0c.55 0 1-.45 1-1s-.45-1-1-1-1 .45-1 1 .45 1 1 1z" fill="currentColor"/>
                  <path d="M6.75 8C6.75 5.1 9.1 2.75 12 2.75C14.9 2.75 17.25 5.1 17.25 8V10.05C17.82 10.01 18.31 10.02 18.75 10.05V8C18.75 4.27 15.73 1.25 12 1.25C8.27 1.25 5.25 4.27 5.25 8V10.05C5.69 10.02 6.18 10.01 6.75 10.05V8Z" fill="currentColor"/>
                </svg>
                <span class="ltr:pl-3 rtl:pr-3 font-extrabold text-xs uppercase tracking-wider text-warning dark:text-warning">Administración</span>
                <svg class="ml-auto shrink-0 transition-transform duration-200" :class="secciones.administracion ? 'rotate-180' : ''" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M6 9l6 6 6-6"/>
                </svg>
              </span>
            </button>
            <Transition name="accordion">
              <ul v-show="secciones.administracion" class="sub-menu ml-4 space-y-0.5 border-l border-[#e0e6ed] dark:border-[#1b2e4b] pl-3 mt-0.5 mb-1">

                <li v-if="auth.hasModuloAccess('USUARIOS')" class="menu nav-item">
                  <RouterLink to="/usuario" class="nav-link group min-h-[40px]" :class="{ active: isActive('/usuario') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path opacity="0.5" d="M12 2C9.38 2 7.25 4.13 7.25 6.75C7.25 9.32 9.26 11.4 11.88 11.49C11.96 11.48 12.04 11.48 12.1 11.49C14.73 11.4 16.74 9.32 16.75 6.75C16.75 4.13 14.62 2 12 2Z" fill="currentColor"/>
                        <path d="M12 14.5C8.66 14.5 5.89 16.36 5.89 18.5C5.89 18.56 5.89 18.61 5.9 18.67C6.72 19.53 7.77 20.2 8.93 20.59C9.9 20.92 10.93 21.1 12 21.1C13.07 21.1 14.1 20.92 15.07 20.59C16.23 20.2 17.28 19.53 18.1 18.67C18.11 18.61 18.11 18.56 18.11 18.5C18.11 16.36 15.34 14.5 12 14.5Z" fill="currentColor"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Usuarios</span>
                    </div>
                  </RouterLink>
                </li>

                <li v-if="auth.hasModuloAccess('USUARIOS')" class="menu nav-item">
                  <RouterLink to="/configuracion" class="nav-link group min-h-[40px]" :class="{ active: isActive('/configuracion') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path opacity="0.5" d="M12 15.5a4.5 4.5 0 1 0 0-9 4.5 4.5 0 0 0 0 9z" fill="currentColor"/>
                        <path d="M21.003 12c0-1.08-.2-2.12-.56-3.06l2.08-1.62-2-3.46-2.44 1.04a8.85 8.85 0 0 0-2.64-1.53L15.22 1h-4l-.25 2.34a8.85 8.85 0 0 0-2.64 1.53L5.9 3.86l-2 3.46 2.08 1.62a9 9 0 0 0 0 6.12l-2.08 1.62 2 3.46 2.44-1.04a8.85 8.85 0 0 0 2.64 1.53l.25 2.37h4l.25-2.37a8.85 8.85 0 0 0 2.64-1.53l2.44 1.04 2-3.46-2.08-1.62c.36-.94.56-1.98.56-3.06z" fill="currentColor"/>
                      </svg>
                      <span title="Configuración de Reportes" class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Configuración</span>
                    </div>
                  </RouterLink>
                </li>

                <li v-if="auth.hasModuloAccess('TIPOS_SERVICIO')" class="menu nav-item">
                  <RouterLink to="/tipo-servicio" class="nav-link group min-h-[40px]" :class="{ active: isActive('/tipo-servicio') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path d="M9 3H4a1 1 0 0 0-1 1v4a1 1 0 0 0 1 1h5a1 1 0 0 0 1-1V4a1 1 0 0 0-1-1z" stroke="currentColor" stroke-width="1.5"/>
                        <path opacity="0.5" d="M20 3h-5a1 1 0 0 0-1 1v4a1 1 0 0 0 1 1h5a1 1 0 0 0 1-1V4a1 1 0 0 0-1-1zM9 14H4a1 1 0 0 0-1 1v4a1 1 0 0 0 1 1h5a1 1 0 0 0 1-1v-4a1 1 0 0 0-1-1z" stroke="currentColor" stroke-width="1.5"/>
                        <path d="M20 14h-5a1 1 0 0 0-1 1v4a1 1 0 0 0 1 1h5a1 1 0 0 0 1-1v-4a1 1 0 0 0-1-1z" stroke="currentColor" stroke-width="1.5"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Tipos de Servicio</span>
                    </div>
                  </RouterLink>
                </li>

                <li v-if="auth.hasModuloAccess('SERVICIOS')" class="menu nav-item">
                  <RouterLink to="/servicio" class="nav-link group min-h-[40px]" :class="{ active: isActive('/servicio') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.5"/>
                        <path opacity="0.5" d="M3.66 10.64C4.13 10.94 4.44 11.44 4.44 12C4.44 12.56 4.13 13.06 3.66 13.36C3.34 13.56 3.13 13.72 2.99 13.92C2.66 14.34 2.52 14.87 2.59 15.39C2.64 15.79 2.87 16.19 3.34 17C3.81 17.81 4.04 18.21 4.35 18.45C4.78 18.78 5.31 18.92 5.83 18.85C6.07 18.82 6.32 18.72 6.65 18.54C7.15 18.28 7.74 18.27 8.22 18.55C8.7 18.83 8.99 19.34 9.01 19.9C9.02 20.28 9.06 20.54 9.15 20.77C9.36 21.26 9.74 21.64 10.23 21.85C10.6 22 11.07 22 12 22C12.93 22 13.4 22 13.77 21.85C14.26 21.64 14.64 21.26 14.85 20.77C14.94 20.54 14.98 20.28 14.99 19.9C15.01 19.34 15.3 18.83 15.78 18.55C16.26 18.27 16.85 18.28 17.35 18.54C17.68 18.72 17.93 18.82 18.17 18.85C18.69 18.92 19.22 18.78 19.65 18.45C19.96 18.21 20.19 17.81 20.66 17C21.13 16.19 21.36 15.79 21.41 15.39C21.48 14.87 21.34 14.34 21.01 13.92C20.87 13.72 20.66 13.56 20.34 13.36C19.87 13.06 19.56 12.56 19.56 12C19.56 11.44 19.87 10.94 20.34 10.64C20.66 10.44 20.87 10.28 21.01 10.08C21.34 9.66 21.48 9.13 21.41 8.61C21.36 8.21 21.13 7.81 20.66 7C20.19 6.19 19.96 5.79 19.65 5.55C19.22 5.22 18.69 5.08 18.17 5.15C17.93 5.18 17.68 5.28 17.35 5.46C16.85 5.72 16.26 5.73 15.78 5.45C15.3 5.17 15.01 4.66 14.99 4.1C14.98 3.72 14.94 3.46 14.85 3.23C14.64 2.74 14.26 2.36 13.77 2.15C13.4 2 12.93 2 12 2C11.07 2 10.6 2 10.23 2.15C9.74 2.36 9.36 2.74 9.15 3.23C9.06 3.46 9.02 3.72 9.01 4.1C8.99 4.66 8.7 5.17 8.22 5.45C7.74 5.73 7.15 5.72 6.65 5.46C6.32 5.28 6.07 5.18 5.83 5.15C5.31 5.08 4.78 5.22 4.35 5.55C4.04 5.79 3.81 6.19 3.34 7C2.87 7.81 2.64 8.21 2.59 8.61C2.52 9.13 2.66 9.66 2.99 10.08C3.13 10.28 3.34 10.44 3.66 10.64Z" stroke="currentColor" stroke-width="1.5"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Servicios</span>
                    </div>
                  </RouterLink>
                </li>

                <li v-if="auth.hasModuloAccess('TIPOS_ACTIVO')" class="menu nav-item">
                  <RouterLink to="/tipo-activo" class="nav-link group min-h-[40px]" :class="{ active: isActive('/tipo-activo') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path opacity="0.5" d="M3 6C3 4.34 4.34 3 6 3H18C19.66 3 21 4.34 21 6V18C21 19.66 19.66 21 18 21H6C4.34 21 3 19.66 3 18V6Z" fill="currentColor"/>
                        <path d="M8 9h8M8 12h5M8 15h3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Tipos de Activo</span>
                    </div>
                  </RouterLink>
                </li>

                <li v-if="auth.hasModuloAccess('FALLAS')" class="menu nav-item">
                  <RouterLink to="/falla" class="nav-link group min-h-[40px]" :class="{ active: isActive('/falla') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path opacity="0.5" d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2Z" fill="currentColor"/>
                        <path d="M12 7v6M12 17h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Fallas</span>
                    </div>
                  </RouterLink>
                </li>

              </ul>
            </Transition>
          </li>

          <!-- ══ REPORTES ══ -->
          <li v-if="showReportes" class="menu nav-item">
            <button
              type="button"
              class="nav-link group min-h-[44px] w-full"
              :class="{ active: perteneceA(rutasReportes) }"
              @click="toggle('reportes')"
            >
              <span class="flex items-center flex-1">
                <svg class="shrink-0 group-hover:!text-primary" width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path d="M4 4C4 2.9 4.9 2 6 2H14L20 8V20C20 21.1 19.1 22 18 22H6C4.9 22 4 21.1 4 20V4Z" fill="currentColor" fill-opacity="0.3"/>
                  <path d="M14 2L20 8H14V2Z" fill="currentColor" fill-opacity="0.5"/>
                  <path d="M8 16H16M8 12H12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <span class="ltr:pl-3 rtl:pr-3 font-extrabold text-xs uppercase tracking-wider text-warning dark:text-warning">Reportes</span>
                <svg class="ml-auto shrink-0 transition-transform duration-200" :class="secciones.reportes ? 'rotate-180' : ''" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M6 9l6 6 6-6"/>
                </svg>
              </span>
            </button>
            <Transition name="accordion">
              <ul v-show="secciones.reportes" class="sub-menu ml-4 space-y-0.5 border-l border-[#e0e6ed] dark:border-[#1b2e4b] pl-3 mt-0.5 mb-1">

                <li class="menu nav-item">
                  <RouterLink to="/dashboard-tecnico" class="nav-link group min-h-[40px]" :class="{ active: isActive('/dashboard-tecnico') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path opacity="0.5" d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z" fill="currentColor"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Dashboard Técnico</span>
                    </div>
                  </RouterLink>
                </li>

                <li class="menu nav-item">
                  <RouterLink to="/reportes" class="nav-link group min-h-[40px]" :class="{ active: isActive('/reportes') && !isActive('/reportes-legacy') }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path d="M4 4C4 2.9 4.9 2 6 2H14L20 8V20C20 21.1 19.1 22 18 22H6C4.9 22 4 21.1 4 20V4Z" fill="currentColor" fill-opacity="0.3"/>
                        <path d="M14 2L20 8H14V2Z" fill="currentColor" fill-opacity="0.5"/>
                        <path d="M8 16H16M8 12H12M8 20H12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Centro de Reportes</span>
                    </div>
                  </RouterLink>
                </li>

<!--                <li class="menu nav-item">-->
<!--                  <RouterLink to="/reportes-legacy" class="nav-link group min-h-[40px]" :class="{ active: isActive('/reportes-legacy') }">-->
<!--                    <div class="flex items-center">-->
<!--                      <svg class="shrink-0 group-hover:!text-warning" width="18" height="18" viewBox="0 0 24 24" fill="none">-->
<!--                        <path d="M4 4C4 2.9 4.9 2 6 2H14L20 8V20C20 21.1 19.1 22 18 22H6C4.9 22 4 21.1 4 20V4Z" fill="currentColor" fill-opacity="0.2"/>-->
<!--                        <path d="M14 2L20 8H14V2Z" fill="currentColor" fill-opacity="0.4"/>-->
<!--                        <path d="M12 9v4M12 17h.01" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>-->
<!--                      </svg>-->
<!--                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Reportes Descontinuados</span>-->
<!--                    </div>-->
<!--                  </RouterLink>-->
<!--                </li>-->

              </ul>
            </Transition>
          </li>

          <!-- ══ DEVELOPER ══ -->
          <li v-if="auth.isDeveloper" class="menu nav-item">
            <button
              type="button"
              class="nav-link group min-h-[44px] w-full"
              :class="{ active: perteneceA(rutasDeveloper) }"
              @click="toggle('developer')"
            >
              <span class="flex items-center flex-1">
                <svg class="shrink-0 group-hover:!text-primary" width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path opacity="0.5" d="M3 7C3 5.34 4.34 4 6 4H18C19.66 4 21 5.34 21 7V17C21 18.66 19.66 20 18 20H6C4.34 20 3 18.66 3 17V7Z" fill="currentColor"/>
                  <path d="M7 8L10 11L7 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M11 14H17" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <span class="ltr:pl-3 rtl:pr-3 font-extrabold text-xs uppercase tracking-wider text-danger dark:text-danger">Developer</span>
                <svg class="ml-auto shrink-0 transition-transform duration-200" :class="secciones.developer ? 'rotate-180' : ''" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M6 9l6 6 6-6"/>
                </svg>
              </span>
            </button>
            <Transition name="accordion">
              <ul v-show="secciones.developer" class="sub-menu ml-4 space-y-0.5 border-l border-[#e0e6ed] dark:border-[#1b2e4b] pl-3 mt-0.5 mb-1">

                <li class="menu nav-item">
                  <RouterLink to="/logs" class="nav-link group min-h-[40px]" :class="{ active: route.path === '/logs' }">
                    <div class="flex items-center">
                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path opacity="0.5" d="M3 7C3 5.34 4.34 4 6 4H18C19.66 4 21 5.34 21 7V17C21 18.66 19.66 20 18 20H6C4.34 20 3 18.66 3 17V7Z" fill="currentColor"/>
                        <path d="M7 8L10 11L7 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M11 14H17" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                      </svg>
                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Logs del Sistema</span>
                    </div>
                  </RouterLink>
                </li>

                <li class="menu nav-item">
<!--                  <RouterLink to="/logs-vue" class="nav-link group min-h-[40px]" :class="{ active: route.path === '/logs-vue' }">-->
<!--                    <div class="flex items-center">-->
<!--                      <svg class="shrink-0 group-hover:!text-primary" width="18" height="18" viewBox="0 0 24 24" fill="none">-->
<!--                        <path opacity="0.5" d="M12 2L2 7l10 5 10-5-10-5z" fill="currentColor"/>-->
<!--                        <path d="M2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>-->
<!--                      </svg>-->
<!--                      <span class="text-black ltr:pl-3 rtl:pr-3 dark:text-[#506690] dark:group-hover:text-white-dark">Logs del Frontend</span>-->
<!--                    </div>-->
<!--                  </RouterLink>-->
                </li>

              </ul>
            </Transition>
          </li>

        </ul>
      </div>
    </nav>
  </div>
</template>

<style>
.sidebar .perfect-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.sidebar .perfect-scrollbar::-webkit-scrollbar {
  display: none;
}
.accordion-enter-active,
.accordion-leave-active {
  transition: opacity 0.2s ease, max-height 0.25s ease;
  max-height: 400px;
  overflow: hidden;
}
.accordion-enter-from,
.accordion-leave-to {
  opacity: 0;
  max-height: 0;
}
</style>