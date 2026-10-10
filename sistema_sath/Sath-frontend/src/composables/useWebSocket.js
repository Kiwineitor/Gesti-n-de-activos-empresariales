import { onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'

let _client = null
let _connected = false
const _subscribers = new Map()

function getClient() {
  if (_client) return _client

  _client = new Client({
    webSocketFactory: () => new SockJS(`${import.meta.env.VITE_API_BASE || ''}/ws`),
    reconnectDelay: 5000,
    debug: () => {},
    onConnect: () => {
      _connected = true
      _client.subscribe('/topic/solicitudes/nueva', (msg) => {
        try {
          const data = JSON.parse(msg.body)
          window.dispatchEvent(new CustomEvent('sath:nueva-solicitud', { detail: data }))
        } catch {
          window.dispatchEvent(new CustomEvent('sath:nueva-solicitud', { detail: {} }))
        }
      })
      _client.subscribe('/topic/solicitudes/estado', (msg) => {
        try {
          const data = JSON.parse(msg.body)
          window.dispatchEvent(new CustomEvent('sath:estado-solicitud', { detail: data }))
        } catch {
          window.dispatchEvent(new CustomEvent('sath:estado-solicitud', { detail: {} }))
        }
      })
      _client.subscribe('/topic/solicitudes/modo-auto-asignar', (msg) => {
        try {
          const data = JSON.parse(msg.body)
          window.dispatchEvent(new CustomEvent('sath:modo-auto-asignar', { detail: data }))
        } catch {
          window.dispatchEvent(new CustomEvent('sath:modo-auto-asignar', { detail: { activo: false } }))
        }
      })
    },
    onDisconnect: () => {
      _connected = false
    },
  })
  _client.activate()
  return _client
}

export function useWebSocket() {
  const client = getClient()

  function on(event, handler) {
    window.addEventListener(event, handler)
    onUnmounted(() => window.removeEventListener(event, handler))
  }

  return { client, on, isConnected: () => _connected }
}
