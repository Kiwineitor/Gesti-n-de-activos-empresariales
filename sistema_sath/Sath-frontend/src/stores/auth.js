import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const TOKEN_KEY = 'sath_token'
const USER_KEY = 'sath_user'

// Migración sessionStorage → localStorage si existe dato antiguo
;(function migrate() {
  const t = sessionStorage.getItem(TOKEN_KEY)
  const u = sessionStorage.getItem(USER_KEY)
  if (t && !localStorage.getItem(TOKEN_KEY)) {
    localStorage.setItem(TOKEN_KEY, t)
    if (u) localStorage.setItem(USER_KEY, u)
  }
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(USER_KEY)
})()

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(JSON.parse(localStorage.getItem(USER_KEY) || '{}'))

  function getTokenExpiry() {
    if (!token.value) return 0
    try {
      const payload = JSON.parse(atob(token.value.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
      return (payload.exp || 0) * 1000
    } catch {
      return 0
    }
  }

  function isTokenExpired() {
    const exp = getTokenExpiry()
    return exp === 0 || Date.now() >= exp
  }

  const isAuthenticated = computed(() => !!token.value && !isTokenExpired())
  const roles = computed(() => user.value.roles || [])
  const isAdmin = computed(() => roles.value.includes('ADMINISTRADOR'))
  const isTecnico = computed(() => roles.value.includes('TECNICO'))
  const isDeveloper = computed(() => roles.value.includes('DEVELOPER'))
  const username = computed(() => user.value.username || '')
  const nombre = computed(() => user.value.nombreCompleto || user.value.username || 'Usuario')
  const cargo = computed(() => user.value.cargo || '')
  const inicial = computed(() => (nombre.value || 'U').charAt(0).toUpperCase())
  const permisos = computed(() => user.value.permisos || [])

  function hasModuloAccess(nombre) {
    return isAdmin.value || permisos.value.includes(nombre)
  }

  function setSession(newToken, newUser) {
    token.value = newToken
    user.value = newUser
    localStorage.setItem(TOKEN_KEY, newToken)
    localStorage.setItem(USER_KEY, JSON.stringify(newUser))
  }

  function clear() {
    token.value = ''
    user.value = {}
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  function logout() {
    clear()
    window.location.href = '/login'
  }

  function authHeaders() {
    return {
      Authorization: `Bearer ${token.value || ''}`,
      'Content-Type': 'application/json',
    }
  }

  async function handle401(res) {
    if (res.status === 401) {
      logout()
      return true
    }
    return false
  }

  async function refreshToken() {
    const res = await fetch('/api/auth/refresh', {
      method: 'POST',
      headers: authHeaders(),
    })
    if (!res.ok) return false
    const data = await res.json()
    setSession(data.token, {
      username: data.username,
      nombreCompleto: data.nombreCompleto,
      roles: data.roles,
      cargo: data.cargo,
      permisos: data.permisos,
    })
    return true
  }

  return {
    token,
    user,
    isAuthenticated,
    isAdmin,
    isTecnico,
    isDeveloper,
    username,
    nombre,
    cargo,
    inicial,
    permisos,
    hasModuloAccess,
    setSession,
    clear,
    logout,
    authHeaders,
    handle401,
    getTokenExpiry,
    isTokenExpired,
    refreshToken,
  }
})
