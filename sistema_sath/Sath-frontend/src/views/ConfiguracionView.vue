<script setup>
import { ref, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import SathLoader from '@/components/ui/SathLoader.vue'

const { get, put } = useApi()
const { toast } = useToast()

const cargando  = ref(false)
const guardando = ref(false)
const formulario = ref({ nombreAutorizado: '', cargoAutorizado: '' })

onMounted(cargar)

async function cargar() {
  cargando.value = true
  try {
    const res = await get('/api/configuracion/dtic')
    if (!res?.ok) throw new Error('Error al cargar la configuración')
    const datos = await res.json()
    formulario.value.nombreAutorizado = datos.nombreAutorizado ?? ''
    formulario.value.cargoAutorizado  = datos.cargoAutorizado ?? ''
  } catch (e) { toast(e.message, 'error') } finally { cargando.value = false }
}

async function guardar() {
  if (!formulario.value.nombreAutorizado.trim() || !formulario.value.cargoAutorizado.trim()) {
    toast('El nombre y el cargo son obligatorios', 'error')
    return
  }
  guardando.value = true
  try {
    const res = await put('/api/configuracion/dtic', {
      nombreAutorizado: formulario.value.nombreAutorizado.trim(),
      cargoAutorizado:  formulario.value.cargoAutorizado.trim(),
    })
    if (!res?.ok) throw new Error('Error al guardar la configuración')
    toast('Configuración guardada correctamente')
  } catch (e) { toast(e.message, 'error') } finally { guardando.value = false }
}
</script>

<template>
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Configuración de Reportes</li>
  </ul>

  <div class="panel max-w-2xl">
    <div class="flex items-center gap-3 mb-6">
      <div class="grid h-10 w-10 shrink-0 place-content-center rounded-lg bg-primary/10">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="text-primary">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <path d="M12 11v4"/><path d="M12 18h.01"/>
        </svg>
      </div>
      <div>
        <h5 class="text-xl font-bold dark:text-white-light/90">Firma de reportes</h5>
        <p class="text-sm text-white-dark">
          Nombre y cargo que aparecen en la ficha técnica (PDF y QR de autorización).
        </p>
      </div>
    </div>

    <SathLoader v-if="cargando" size="md" />

    <div v-else class="space-y-4">
      <div>
        <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">
          Nombre del autorizado <span class="text-danger">*</span>
        </label>
        <input v-model="formulario.nombreAutorizado" type="text" class="form-input"
          placeholder="Ej: Lic. Eduardo Zubieta Copeticon" autocomplete="off" @keydown.enter.prevent="guardar" />
      </div>
      <div>
        <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">
          Cargo <span class="text-danger">*</span>
        </label>
        <input v-model="formulario.cargoAutorizado" type="text" class="form-input"
          placeholder="Ej: JEFE a.i DIRECCION DE TECNOLOGIA, INFORMACION Y COMUNICACION" autocomplete="off" @keydown.enter.prevent="guardar" />
      </div>
      <div class="flex justify-end gap-2">
        <button class="btn btn-outline-secondary" :disabled="guardando" @click="cargar">Restablecer</button>
        <button class="btn btn-primary gap-2" :disabled="guardando" @click="guardar">
          <svg v-if="!guardando" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
            <polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/>
          </svg>
          {{ guardando ? 'Guardando...' : 'Guardar' }}
        </button>
      </div>
    </div>
  </div>
</template>
