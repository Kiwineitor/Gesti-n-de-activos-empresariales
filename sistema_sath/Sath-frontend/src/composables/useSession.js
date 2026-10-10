import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const WARN_BEFORE_MS = 10 * 60 * 1000 // avisar 10 min antes

const showModal = ref(false)
const secondsLeft = ref(0)

let warnTimer = null
let logoutTimer = null
let tickInterval = null

export function useSession() {
  const auth = useAuthStore()

  const minutesLeft = computed(() => {
    const m = Math.floor(secondsLeft.value / 60)
    const s = secondsLeft.value % 60
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  })

  const isWarning = computed(() => secondsLeft.value > 0 && secondsLeft.value <= WARN_BEFORE_MS / 1000)

  function _clearTimers() {
    clearTimeout(warnTimer)
    clearTimeout(logoutTimer)
    clearInterval(tickInterval)
    warnTimer = null
    logoutTimer = null
    tickInterval = null
  }

  function _startTick() {
    clearInterval(tickInterval)
    tickInterval = setInterval(() => {
      const remaining = Math.max(0, Math.floor((auth.getTokenExpiry() - Date.now()) / 1000))
      secondsLeft.value = remaining
      if (remaining <= 0) {
        clearInterval(tickInterval)
        showModal.value = false
        auth.logout()
      }
    }, 1000)
  }

  function startSession() {
    _clearTimers()
    showModal.value = false

    const exp = auth.getTokenExpiry()
    if (!exp) return

    const msUntilWarn = exp - Date.now() - WARN_BEFORE_MS
    const msUntilLogout = exp - Date.now()

    if (msUntilLogout <= 0) {
      auth.logout()
      return
    }

    secondsLeft.value = Math.floor(msUntilLogout / 1000)

    if (msUntilWarn > 0) {
      warnTimer = setTimeout(() => {
        showModal.value = true
        _startTick()
      }, msUntilWarn)
    } else {
      // ya estamos en zona de aviso
      showModal.value = true
      _startTick()
    }

    logoutTimer = setTimeout(() => {
      showModal.value = false
      auth.logout()
    }, msUntilLogout)
  }

  async function renewSession() {
    const ok = await auth.refreshToken()
    if (ok) {
      showModal.value = false
      secondsLeft.value = 0
      startSession()
    } else {
      auth.logout()
    }
  }

  function destroySession() {
    _clearTimers()
    showModal.value = false
    auth.logout()
  }

  function destroy() {
    _clearTimers()
  }

  return {
    showModal,
    secondsLeft,
    minutesLeft,
    isWarning,
    startSession,
    renewSession,
    destroySession,
    destroy,
  }
}