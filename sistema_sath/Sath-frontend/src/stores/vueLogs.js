import {defineStore} from 'pinia'
import {ref, computed} from 'vue'

const STORAGE_KEY = 'sath_vue_logs'
const MAX_LOGS = 300

function ahora() {
    const d = new Date()
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function esHoy(fechaStr) {
    const hoy = ahora().slice(0, 10)
    return fechaStr?.startsWith(hoy)
}

export const useVueLogsStore = defineStore('vueLogs', () => {
    const logs = ref(_cargar())

    function _cargar() {
        try {
            return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
        } catch {
            return []
        }
    }

    function _guardar() {
        try {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(logs.value.slice(0, MAX_LOGS)))
        } catch {
        }
    }

    function registrar(nivel, mensaje, componente = '', info = '', stackTrace = '') {
        logs.value.unshift({
            id: Date.now() + Math.random(),
            fecha: ahora(),
            nivel,
            mensaje: String(mensaje ?? '').slice(0, 2000),
            componente: String(componente ?? ''),
            info: String(info ?? ''),
            stackTrace: String(stackTrace ?? '').slice(0, 5000),
            url: window.location.pathname,
        })
        if (logs.value.length > MAX_LOGS) logs.value.length = MAX_LOGS
        _guardar()
    }

    function limpiar() {
        logs.value = []
        localStorage.removeItem(STORAGE_KEY)
    }

    const stats = computed(() => ({
        total: logs.value.length,
        errores: logs.value.filter(l => l.nivel === 'ERROR').length,
        warnings: logs.value.filter(l => l.nivel === 'WARN').length,
        hoy: logs.value.filter(l => esHoy(l.fecha)).length,
    }))

    return {logs, stats, registrar, limpiar}
})
