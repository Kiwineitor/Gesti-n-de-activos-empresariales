import { useAuthStore } from '@/stores/auth'

const BASE = import.meta.env.VITE_API_BASE || ''

export function useApi() {
  const auth = useAuthStore()

  async function api(url, options = {}) {
    const res = await fetch(`${BASE}${url}`, {
      ...options,
      headers: {
        ...auth.authHeaders(),
        ...(options.headers || {}),
      },
    })
    if (await auth.handle401(res)) return null
    return res
  }

  async function get(url) {
    return api(url)
  }

  async function post(url, body) {
    return api(url, { method: 'POST', body: JSON.stringify(body) })
  }

  async function put(url, body) {
    return api(url, { method: 'PUT', body: JSON.stringify(body) })
  }

  async function patch(url, body) {
    return api(url, { method: 'PATCH', body: JSON.stringify(body) })
  }

  async function del(url) {
    return api(url, { method: 'DELETE' })
  }

  async function upload(url, formData) {
    const { 'Content-Type': _, ...headers } = auth.authHeaders()
    const res = await fetch(`${BASE}${url}`, { method: 'POST', body: formData, headers })
    if (await auth.handle401(res)) return null
    return res
  }

  return { api, get, post, put, patch, del, upload }
}
