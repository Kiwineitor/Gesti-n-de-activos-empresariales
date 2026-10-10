import { createApp } from 'vue'
import { createPinia, setActivePinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useVueLogsStore } from '@/stores/vueLogs'

const app   = createApp(App)
const pinia = createPinia()

setActivePinia(pinia)
app.use(pinia)
app.use(router)

// ── Captura de errores de Vue ──────────────────────────────────────
const vueLogs = useVueLogsStore()

app.config.errorHandler = (err, instance, info) => {
  vueLogs.registrar(
    'ERROR',
    err?.message || String(err),
    instance?.$options?.name || instance?.$.type?.name || 'Desconocido',
    info,
    err?.stack,
  )
  console.error('[Vue error]', err)
}

app.config.warnHandler = (msg, instance, trace) => {
  vueLogs.registrar(
    'WARN',
    msg,
    instance?.$options?.name || instance?.$.type?.name || 'Desconocido',
    trace,
  )
}

// ── Errores globales de JS (fuera de Vue) ─────────────────────────
window.addEventListener('error', (e) => {
  vueLogs.registrar('ERROR', e.message, 'window', `${e.filename}:${e.lineno}`, e.error?.stack)
})

window.addEventListener('unhandledrejection', (e) => {
  const msg = e.reason?.message || String(e.reason || 'Promise rechazada')
  vueLogs.registrar('ERROR', msg, 'Promise', 'unhandledrejection', e.reason?.stack)
})

app.directive('click-outside', {
  mounted(el, binding) {
    el._clickOutsideHandler = (e) => {
      if (!el.contains(e.target)) binding.value(e)
    }
    document.addEventListener('mousedown', el._clickOutsideHandler)
  },
  unmounted(el) {
    document.removeEventListener('mousedown', el._clickOutsideHandler)
  },
})

app.mount('#app')
