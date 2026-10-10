<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import SathLoader from '@/components/ui/SathLoader.vue'

const auth = useAuthStore()
const { get, patch } = useApi()
const { toast } = useToast()

const usuario = ref(null)
const persona = ref(null)
const cargando = ref(false)
const guardandoUsername = ref(false)
const guardandoPassword = ref(false)

const mostrarActual   = ref(false)
const mostrarNueva    = ref(false)
const mostrarConfirma = ref(false)

const formUsername = ref({ usernameNuevo: '' })
const formPassword = ref({ passwordActual: '', passwordNueva: '', confirmarPassword: '' })

onMounted(async () => {
  await Promise.all([cargarPerfil(), cargarPersona()])
})

async function cargarPerfil() {
  cargando.value = true
  try {
    const res = await get('/api/perfil/me')
    if (!res || !res.ok) throw new Error('Error al cargar perfil')
    usuario.value = await res.json()
    formUsername.value.usernameNuevo = usuario.value.username
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    cargando.value = false
  }
}

async function cargarPersona() {
  try {
    const res = await get('/api/perfil/persona')
    if (!res || !res.ok) return
    persona.value = await res.json()
  } catch {}
}

async function cambiarUsername() {
  if (!formUsername.value.usernameNuevo.trim()) {
    toast('El username no puede estar vacío', 'error')
    return
  }
  guardandoUsername.value = true
  try {
    const res = await patch('/api/perfil/username', {
      usernameNuevo: formUsername.value.usernameNuevo.trim(),
    })
    if (!res) return
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      throw new Error(err.message || `Error ${res.status}`)
    }
    const actualizado = await res.json()
    usuario.value = actualizado
    auth.setSession(auth.token, { ...auth.user, username: actualizado.username })
    toast('Username actualizado. Vuelve a iniciar sesión.', 'success')
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    guardandoUsername.value = false
  }
}

async function cambiarPassword() {
  const { passwordNueva, confirmarPassword, passwordActual } = formPassword.value
  if (!passwordActual) { toast('Ingresa tu contraseña actual', 'error'); return }
  if (!passwordNueva)  { toast('Ingresa la nueva contraseña', 'error'); return }
  if (passwordNueva !== confirmarPassword) { toast('Las contraseñas no coinciden', 'error'); return }
  if (passwordNueva.length < 6) { toast('Mínimo 6 caracteres', 'error'); return }

  guardandoPassword.value = true
  try {
    const res = await patch('/api/perfil/password', formPassword.value)
    if (!res) return
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      throw new Error(err.message || `Error ${res.status}`)
    }
    toast('Contraseña cambiada exitosamente', 'success')
    formPassword.value = { passwordActual: '', passwordNueva: '', confirmarPassword: '' }
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    guardandoPassword.value = false
  }
}

function rolColor(rol) {
  return ({
    ADMINISTRADOR: 'bg-danger/10 text-danger',
    DEVELOPER:     'bg-warning/10 text-warning',
    TECNICO:       'bg-info/10 text-info',
  }[rol] ?? 'bg-primary/10 text-primary')
}
</script>

<template>
  <!-- Breadcrumb -->
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Mi Perfil</li>
  </ul>

  <!-- Loading -->
  <SathLoader v-if="cargando" size="lg" label="Cargando perfil..." class="my-20" />

  <template v-else>
    <!-- ── Hero Banner ── -->
    <div class="panel !p-0 mb-6 overflow-hidden">
      <!-- Gradiente superior -->
      <div class="relative h-32 bg-gradient-to-r from-primary via-blue-500 to-indigo-600">
        <div class="absolute inset-0 opacity-20"
          style="background-image: radial-gradient(circle at 20% 50%, white 1px, transparent 1px), radial-gradient(circle at 80% 20%, white 1px, transparent 1px); background-size: 40px 40px" />
      </div>

      <!-- Avatar + info (solapado sobre el gradiente) -->
      <div class="px-6 pb-6">
        <div class="flex flex-col sm:flex-row sm:items-center gap-4 -mt-10">
          <!-- Avatar -->
          <div class="flex h-20 w-20 items-center justify-center rounded-2xl bg-primary ring-4 ring-white dark:ring-[#0e1726] text-white font-bold text-3xl shadow-lg flex-shrink-0">
            {{ auth.inicial }}
          </div>

          <!-- Nombre, cargo y roles en un solo bloque -->
          <div class="flex-1 min-w-0 pt-2 sm:pt-3">
            <h4 class="text-xl font-bold dark:text-white truncate">{{ auth.nombre }}</h4>
            <div class="flex flex-wrap items-center gap-2 mt-1">
              <span class="text-sm text-white-dark">@{{ auth.username }}</span>
              <span v-if="auth.cargo" class="text-xs bg-primary/10 text-primary px-2 py-0.5 rounded-full font-medium">
                {{ auth.cargo }}
              </span>
            </div>
            <div class="flex flex-wrap gap-1.5 mt-2">
              <span
                v-for="r in (usuario?.roles || [])"
                :key="r"
                class="text-xs font-semibold px-2.5 py-1 rounded-full"
                :class="rolColor(r)"
              >{{ r }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Cuerpo ── -->
    <div class="grid grid-cols-1 xl:grid-cols-5 gap-6">

      <!-- Columna izquierda: datos personales -->
      <div class="xl:col-span-2 space-y-6">

        <!-- Datos personales -->
        <div class="panel">
          <div class="flex items-center gap-2 mb-5">
            <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-primary">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
            <h6 class="font-semibold dark:text-white">Datos Personales</h6>
          </div>

          <div v-if="persona" class="space-y-4">
            <!-- CI -->
            <div class="flex items-start gap-3">
              <div class="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg bg-[#f1f2f3] dark:bg-[#1b2e4b]">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-white-dark">
                  <rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/>
                </svg>
              </div>
              <div class="min-w-0">
                <p class="text-xs text-white-dark mb-0.5">Carnet de Identidad</p>
                <p class="font-semibold dark:text-white text-sm">{{ persona.ciPer || '—' }}</p>
              </div>
            </div>

            <!-- Nombre completo -->
            <div class="flex items-start gap-3">
              <div class="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg bg-[#f1f2f3] dark:bg-[#1b2e4b]">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-white-dark">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                </svg>
              </div>
              <div class="min-w-0">
                <p class="text-xs text-white-dark mb-0.5">Nombre Completo</p>
                <p class="font-semibold dark:text-white text-sm break-words">{{ persona.nombreCompleto || '—' }}</p>
              </div>
            </div>

            <!-- Teléfono -->
            <div class="flex items-start gap-3">
              <div class="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg bg-[#f1f2f3] dark:bg-[#1b2e4b]">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-white-dark">
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.61 3.38a2 2 0 0 1 1.99-2.18h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.96a16 16 0 0 0 6.13 6.13l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
                </svg>
              </div>
              <div class="min-w-0">
                <p class="text-xs text-white-dark mb-0.5">Teléfono</p>
                <p class="font-semibold dark:text-white text-sm">{{ persona.nroCellPer || '—' }}</p>
              </div>
            </div>

            <!-- Correo -->
            <div class="flex items-start gap-3">
              <div class="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg bg-[#f1f2f3] dark:bg-[#1b2e4b]">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-white-dark">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                  <polyline points="22,6 12,13 2,6"/>
                </svg>
              </div>
              <div class="min-w-0">
                <p class="text-xs text-white-dark mb-0.5">Correo Electrónico</p>
                <p class="font-semibold dark:text-white text-sm break-all">{{ persona.correoPer || '—' }}</p>
              </div>
            </div>
          </div>

          <div v-else class="text-center py-6 text-white-dark text-sm">
            Sin datos personales vinculados
          </div>
        </div>

        <!-- Acceso rápido -->
        <div class="panel">
          <div class="flex items-center gap-2 mb-4">
            <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-success/10">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-success">
                <polyline points="9 11 12 14 22 4"/>
                <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
              </svg>
            </div>
            <h6 class="font-semibold dark:text-white">Acceso Rápido</h6>
          </div>
          <div class="space-y-2">
            <RouterLink to="/notificaciones"
              class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm hover:bg-[#f1f2f3] dark:hover:bg-[#1b2e4b] transition-colors">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-primary flex-shrink-0">
                <path d="M19 9.7V9a7 7 0 1 0-14 0v.7A4 4 0 0 1 2 13.6v.3A3.1 3.1 0 0 0 5.1 17h13.8A3.1 3.1 0 0 0 22 13.9v-.3a4 4 0 0 1-3-3.9z"/>
                <path d="M9 17v1a3 3 0 0 0 6 0v-1"/>
              </svg>
              <span class="dark:text-white-dark">Historial de notificaciones</span>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-white-dark ml-auto">
                <polyline points="9 18 15 12 9 6"/>
              </svg>
            </RouterLink>
          </div>
        </div>
      </div>

      <!-- Columna derecha: seguridad -->
      <div class="xl:col-span-3 space-y-6">

        <!-- Cambiar username -->
        <div class="panel">
          <div class="flex items-center gap-2 mb-5">
            <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-warning/10">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-warning">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
            <h6 class="font-semibold dark:text-white">Cambiar Username</h6>
          </div>

          <div class="mb-5">
            <label class="flex items-center gap-1.5 text-xs font-semibold uppercase text-white-dark mb-2">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/>
              </svg>
              Nuevo username
            </label>
            <input
              v-model="formUsername.usernameNuevo"
              type="text"
              class="form-input w-full"
              placeholder="nuevo_username"
            />
            <p class="text-xs text-white-dark mt-1.5">El cambio de username cerrará tu sesión actual.</p>
          </div>

          <button
            class="btn btn-warning w-full sm:w-auto"
            :disabled="guardandoUsername"
            @click="cambiarUsername"
          >
            <svg v-if="guardandoUsername" class="inline w-4 h-4 mr-2 animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
            </svg>
            {{ guardandoUsername ? 'Guardando...' : 'Actualizar Username' }}
          </button>
        </div>

        <!-- Cambiar contraseña -->
        <div class="panel">
          <div class="flex items-center gap-2 mb-5">
            <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-danger/10">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-danger">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
            </div>
            <h6 class="font-semibold dark:text-white">Cambiar Contraseña</h6>
          </div>

          <div class="space-y-4 mb-5">
            <!-- Contraseña actual -->
            <div>
              <label class="flex items-center gap-1.5 text-xs font-semibold uppercase text-white-dark mb-2">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
                Contraseña actual
              </label>
              <div class="relative">
                <input
                  v-model="formPassword.passwordActual"
                  :type="mostrarActual ? 'text' : 'password'"
                  class="form-input pr-10 w-full"
                  placeholder="••••••••"
                />
                <button type="button" class="absolute inset-y-0 right-0 flex items-center pr-3 text-white-dark hover:text-primary transition-colors" @click="mostrarActual = !mostrarActual">
                  <svg v-if="!mostrarActual" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                  </svg>
                  <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                    <line x1="1" y1="1" x2="23" y2="23"/>
                  </svg>
                </button>
              </div>
            </div>

            <!-- Nueva contraseña -->
            <div>
              <label class="flex items-center gap-1.5 text-xs font-semibold uppercase text-white-dark mb-2">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
                Nueva contraseña
              </label>
              <div class="relative">
                <input
                  v-model="formPassword.passwordNueva"
                  :type="mostrarNueva ? 'text' : 'password'"
                  class="form-input pr-10 w-full"
                  placeholder="••••••••"
                />
                <button type="button" class="absolute inset-y-0 right-0 flex items-center pr-3 text-white-dark hover:text-primary transition-colors" @click="mostrarNueva = !mostrarNueva">
                  <svg v-if="!mostrarNueva" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                  </svg>
                  <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                    <line x1="1" y1="1" x2="23" y2="23"/>
                  </svg>
                </button>
              </div>
              <p class="text-xs text-white-dark mt-1.5">Mínimo 6 caracteres.</p>
            </div>

            <!-- Confirmar contraseña -->
            <div>
              <label class="flex items-center gap-1.5 text-xs font-semibold uppercase mb-2"
                :class="formPassword.confirmarPassword && formPassword.confirmarPassword !== formPassword.passwordNueva ? 'text-danger' : 'text-white-dark'">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
                Confirmar contraseña
              </label>
              <div class="relative">
                <input
                  v-model="formPassword.confirmarPassword"
                  :type="mostrarConfirma ? 'text' : 'password'"
                  class="form-input pr-10 w-full"
                  :class="formPassword.confirmarPassword && formPassword.confirmarPassword !== formPassword.passwordNueva ? '!border-danger' : ''"
                  placeholder="••••••••"
                />
                <button type="button" class="absolute inset-y-0 right-0 flex items-center pr-3 text-white-dark hover:text-primary transition-colors" @click="mostrarConfirma = !mostrarConfirma">
                  <svg v-if="!mostrarConfirma" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                  </svg>
                  <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                    <line x1="1" y1="1" x2="23" y2="23"/>
                  </svg>
                </button>
              </div>
              <p v-if="formPassword.confirmarPassword && formPassword.confirmarPassword !== formPassword.passwordNueva"
                class="text-xs text-danger mt-1.5">
                Las contraseñas no coinciden
              </p>
            </div>
          </div>

          <button
            class="btn btn-danger w-full sm:w-auto"
            :disabled="guardandoPassword"
            @click="cambiarPassword"
          >
            <svg v-if="guardandoPassword" class="inline w-4 h-4 mr-2 animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
            </svg>
            {{ guardandoPassword ? 'Guardando...' : 'Cambiar Contraseña' }}
          </button>
        </div>

      </div>
    </div>
  </template>
</template>