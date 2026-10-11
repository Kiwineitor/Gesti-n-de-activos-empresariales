<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  modelValue: Boolean,
  item: Object,
})
const emit = defineEmits(['update:modelValue', 'guardado'])

const { get, post, upload, del } = useApi()
const { toast } = useToast()
const auth = useAuthStore()

// ── Catálogos ────────────────────────────────────────────────────
const prioridades    = ref([])
const servicios      = ref([])
const tiposServicio  = ref([])
const tiposActivo    = ref([])
const fallasDisponibles = ref([])

// ── Estado del form ──────────────────────────────────────────────
const formAtender = ref({ idsFallas: [], idPrioridad: '', idsServicios: [] })
const tipoFallaSeleccionado = ref('')
const cargandoFallas = ref(false)
const guardando      = ref(false)

// ── Crear falla inline ────────────────────────────────────────────
const crearFallaAbierto   = ref(false)
const nuevaFalla          = ref({ nombreFall: '', idTipoActivo: '' })
const guardandoNuevaFalla = ref(false)

// ── Crear tipo de recurso inline ───────────────────────────────────
const crearTipoActAbierto   = ref(false)
const nuevoTipoActNombre    = ref('')
const guardandoNuevoTipoAct = ref(false)

// ── Crear servicio inline ─────────────────────────────────────────
const crearServicioAbierto   = ref(false)
const nuevoServicioNombre    = ref('')
const nuevoServicioTipoId    = ref(null)
const guardandoNuevoServicio = ref(false)

// ── Estado acordeones del form ────────────────────────────────────
const seccionFallasAbierta     = ref(true)
const seccionPrioridadAbierta  = ref(true)
const seccionServiciosAbierta  = ref(true)

// ── Filtros de búsqueda ───────────────────────────────────────────
const buscarFalla    = ref('')
const buscarServicio = ref('')

// ── Fallas filtradas ──────────────────────────────────────────────
const fallasFiltradas = computed(() => {
  const q = buscarFalla.value.toLowerCase().trim()
  if (!q) return fallasDisponibles.value
  return fallasDisponibles.value.filter(f => f.nombreFall.toLowerCase().includes(q))
})

// ── Servicios agrupados por tipo (con filtro) ─────────────────────
const serviciosPorTipo = computed(() => {
  const q = buscarServicio.value.toLowerCase().trim()
  const grupos = {}
  for (const s of servicios.value) {
    if (q && !s.nombreServ.toLowerCase().includes(q)) continue
    const k = s.idTipoServicio ?? 0
    if (!grupos[k]) grupos[k] = { label: s.nombreTipoServicio ?? 'Sin categoría', items: [] }
    grupos[k].items.push(s)
  }
  return Object.values(grupos)
})

onMounted(() => {
  cargarPrioridades()
  cargarServicios()
  cargarTiposActivo()
  cargarTiposServicio()
})

watch(() => props.modelValue, (open) => {
  if (open && props.item) {
    init()
    evidenciasAbierto.value = false
    evidencias.value = []
    blobFotos.value = {}
    limpiarSeleccion()
  }
})

function cerrar() {
  emit('update:modelValue', false)
}

function init() {
  const item = props.item
  formAtender.value = {
    idsFallas:   [],
    idPrioridad: item.idPrioridad ? String(item.idPrioridad) : '',
    idsServicios: item.idsServicios ? item.idsServicios.map(Number)
      : item.idServicio ? [Number(item.idServicio)] : [],
  }
  fallasDisponibles.value      = []
  buscarFalla.value            = ''
  buscarServicio.value         = ''
  seccionFallasAbierta.value   = true
  seccionPrioridadAbierta.value = true
  seccionServiciosAbierta.value = true
  nuevaFalla.value        = { nombreFall: '', idTipoActivo: '' }
  nuevoServicioNombre.value = ''
  nuevoServicioTipoId.value = null
  nuevoTipoActNombre.value  = ''
  tipoFallaSeleccionado.value = item.idTipoActivo ? String(item.idTipoActivo) : ''
  crearFallaAbierto.value   = false
  crearTipoActAbierto.value = false
  crearServicioAbierto.value = false

  if (!item.idAtencion && tipoFallaSeleccionado.value) {
    cargarFallasPorTipo(tipoFallaSeleccionado.value)
  }
}

// ── Carga de catálogos ────────────────────────────────────────────
async function cargarPrioridades() {
  try {
    const res = await get('/api/prioridad/listar')
    if (!res?.ok) return
    const data = await res.json()
    prioridades.value = data.sort((a, b) => (a.ordenPri ?? 99) - (b.ordenPri ?? 99))
  } catch {}
}

async function cargarServicios() {
  try {
    const res = await get('/api/servicio/lista')
    if (!res?.ok) return
    servicios.value = await res.json()
  } catch {}
}

async function cargarTiposServicio() {
  try {
    const res = await get('/api/tipo-servicio/lista')
    if (!res?.ok) return
    tiposServicio.value = await res.json()
  } catch {}
}

async function cargarTiposActivo() {
  try {
    const res = await get('/api/tipo-activo/listar')
    if (!res?.ok) return
    const data = await res.json()
    tiposActivo.value = Array.isArray(data) ? data : []
  } catch {}
}

async function cargarFallasPorTipo(idTipoActivo) {
  fallasDisponibles.value = []
  formAtender.value.idsFallas = []
  buscarFalla.value = ''
  if (!idTipoActivo) return
  cargandoFallas.value = true
  try {
    const res = await get(`/api/falla/por-tipo/${idTipoActivo}`)
    if (res?.ok) fallasDisponibles.value = await res.json()
  } catch {} finally { cargandoFallas.value = false }
}

// ── Fallas ────────────────────────────────────────────────────────
function fallaSeleccionada(idFalla) {
  return formAtender.value.idsFallas.includes(Number(idFalla))
}
function toggleFalla(idFalla) {
  const id = Number(idFalla)
  const idx = formAtender.value.idsFallas.indexOf(id)
  if (idx === -1) formAtender.value.idsFallas.push(id)
  else formAtender.value.idsFallas.splice(idx, 1)
}
function toggleCrearFalla() {
  crearFallaAbierto.value = !crearFallaAbierto.value
  if (crearFallaAbierto.value) {
    nuevaFalla.value = { nombreFall: '', idTipoActivo: tipoFallaSeleccionado.value || '' }
    crearTipoActAbierto.value = false
  }
}
async function guardarNuevaFalla() {
  if (!nuevaFalla.value.nombreFall.trim()) {
    toast('El nombre de la falla es requerido', 'error'); return
  }
  guardandoNuevaFalla.value = true
  try {
    const res = await post('/api/falla/crear', {
      nombreFall:   nuevaFalla.value.nombreFall.trim(),
      idTipoActivo: nuevaFalla.value.idTipoActivo ? Number(nuevaFalla.value.idTipoActivo) : null,
    })
    if (!res?.ok) throw new Error('Error al crear la falla')
    const nueva = await res.json()
    fallasDisponibles.value.push(nueva)
    formAtender.value.idsFallas.push(Number(nueva.idFalla))
    crearFallaAbierto.value = false
    toast(`Falla "${nueva.nombreFall}" creada y seleccionada`, 'success')
  } catch (e) { toast(e.message, 'error') } finally { guardandoNuevaFalla.value = false }
}

// ── Tipo de recurso inline ─────────────────────────────────────────
function toggleCrearTipoAct() {
  crearTipoActAbierto.value = !crearTipoActAbierto.value
  if (crearTipoActAbierto.value) nuevoTipoActNombre.value = ''
}
async function guardarNuevoTipoAct() {
  if (!nuevoTipoActNombre.value.trim()) {
    toast('El nombre del tipo es requerido', 'error'); return
  }
  guardandoNuevoTipoAct.value = true
  try {
    const res = await post('/api/tipo-activo/crear', { nombreTipoAct: nuevoTipoActNombre.value.trim() })
    if (!res?.ok) throw new Error('Error al crear el tipo')
    const nuevo = await res.json()
    tiposActivo.value.push(nuevo)
    tipoFallaSeleccionado.value = String(nuevo.idTipoActivo)
    nuevaFalla.value.idTipoActivo = String(nuevo.idTipoActivo)
    crearTipoActAbierto.value = false
    nuevoTipoActNombre.value = ''
    toast(`Tipo "${nuevo.nombreTipoAct}" creado y seleccionado`, 'success')
  } catch (e) { toast(e.message, 'error') } finally { guardandoNuevoTipoAct.value = false }
}

// ── Servicios ─────────────────────────────────────────────────────
function servicioSeleccionado(idServicio) {
  return formAtender.value.idsServicios.includes(Number(idServicio))
}
function toggleServicio(idServicio) {
  const id = Number(idServicio)
  const idx = formAtender.value.idsServicios.indexOf(id)
  if (idx === -1) formAtender.value.idsServicios.push(id)
  else formAtender.value.idsServicios.splice(idx, 1)
}
function toggleCrearServicio() {
  crearServicioAbierto.value = !crearServicioAbierto.value
  if (crearServicioAbierto.value) {
    nuevoServicioNombre.value = ''
    nuevoServicioTipoId.value = null
  }
}
async function guardarNuevoServicio() {
  if (!nuevoServicioNombre.value.trim()) {
    toast('El nombre del servicio es requerido', 'error'); return
  }
  guardandoNuevoServicio.value = true
  try {
    const res = await post('/api/servicio/crear', {
      nombreServ:     nuevoServicioNombre.value.trim(),
      idTipoServicio: nuevoServicioTipoId.value || null,
    })
    if (!res?.ok) throw new Error('Error al crear el servicio (requiere permisos de administrador)')
    const nuevo = await res.json()
    servicios.value.push({
      idServicio:         nuevo.idServicio,
      nombreServ:         nuevo.nombreServ,
      idTipoServicio:     nuevo.idTipoServicio ?? null,
      nombreTipoServicio: nuevo.nombreTipoServicio ?? null,
    })
    formAtender.value.idsServicios.push(Number(nuevo.idServicio))
    crearServicioAbierto.value = false
    toast(`Servicio "${nuevo.nombreServ}" creado y seleccionado`, 'success')
  } catch (e) { toast(e.message, 'error') } finally { guardandoNuevoServicio.value = false }
}

// ── Evidencias fotográficas ───────────────────────────────────────
const evidenciasAbierto  = ref(false)
const evidencias         = ref([])
const cargandoEvidencias = ref(false)
const archivoFoto        = ref(null)
const descripcionFoto    = ref('')
const previewLocal       = ref(null)   // blob URL del archivo seleccionado
const blobFotos          = ref({})     // blob URLs de fotos ya subidas
const subiendoFoto       = ref(false)
const eliminandoFotoId   = ref(null)
const archivoInputRef    = ref(null)

// ── Cámara getUserMedia ───────────────────────────────────────────
const camaraModalAbierto = ref(false)
const videoRef           = ref(null)
const canvasRef          = ref(null)
let mediaStream          = null

function seleccionarArchivo() { archivoInputRef.value?.click() }

async function abrirCamara() {
  if (!window.isSecureContext) {
    toast('Se requiere HTTPS para usar la cámara. Accede por localhost o habilita SSL en el servidor.', 'error'); return
  }
  if (!navigator.mediaDevices?.getUserMedia) {
    toast('Tu navegador no soporta acceso a cámara.', 'error'); return
  }
  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } },
      audio: false,
    })
    camaraModalAbierto.value = true
    await nextTick()
    if (videoRef.value) videoRef.value.srcObject = mediaStream
  } catch (e) {
    const msg = {
      NotAllowedError:   'Permiso de cámara denegado. Habilítalo en la configuración del navegador.',
      NotFoundError:     'No se encontró ninguna cámara en este dispositivo.',
      NotReadableError:  'La cámara está en uso por otra aplicación.',
      OverconstrainedError: 'No se pudo iniciar la cámara con la configuración solicitada.',
    }[e.name] ?? `Error de cámara: ${e.message}`
    toast(msg, 'error')
  }
}

function capturarFoto() {
  const video  = videoRef.value
  const canvas = canvasRef.value
  if (!video || !canvas) return
  canvas.width  = video.videoWidth
  canvas.height = video.videoHeight
  canvas.getContext('2d').drawImage(video, 0, 0)
  canvas.toBlob((blob) => {
    const file = new File([blob], `foto_${Date.now()}.jpg`, { type: 'image/jpeg' })
    if (previewLocal.value) URL.revokeObjectURL(previewLocal.value)
    archivoFoto.value  = file
    previewLocal.value = URL.createObjectURL(blob)
    cerrarCamara()
  }, 'image/jpeg', 0.88)
}

function cerrarCamara() {
  camaraModalAbierto.value = false
  if (mediaStream) { mediaStream.getTracks().forEach(t => t.stop()); mediaStream = null }
}

function handleFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (previewLocal.value) URL.revokeObjectURL(previewLocal.value)
  archivoFoto.value   = file
  previewLocal.value  = URL.createObjectURL(file)
}

function limpiarSeleccion() {
  archivoFoto.value  = null
  descripcionFoto.value = ''
  if (previewLocal.value) { URL.revokeObjectURL(previewLocal.value); previewLocal.value = null }
}

async function cargarEvidencias() {
  if (!props.item?.idSolicitud) return
  cargandoEvidencias.value = true
  try {
    const res = await get(`/api/evidencia/${props.item.idSolicitud}`)
    if (!res?.ok) return
    const lista = await res.json()
    evidencias.value = lista
    // Carga los blobs con auth para poder mostrarlos como <img>
    for (const foto of lista) {
      if (!blobFotos.value[foto.idEvidenciaFoto]) {
        cargarBlobFoto(foto.idEvidenciaFoto)
      }
    }
  } catch {} finally { cargandoEvidencias.value = false }
}

async function cargarBlobFoto(idEvidenciaFoto) {
  try {
    const res = await get(`/api/evidencia/foto/${idEvidenciaFoto}`)
    if (!res?.ok) return
    const blob = await res.blob()
    if (blobFotos.value[idEvidenciaFoto]) URL.revokeObjectURL(blobFotos.value[idEvidenciaFoto])
    blobFotos.value = { ...blobFotos.value, [idEvidenciaFoto]: URL.createObjectURL(blob) }
  } catch {}
}

async function subirFoto() {
  if (!archivoFoto.value || !props.item?.idSolicitud) return
  subiendoFoto.value = true
  try {
    const form = new FormData()
    form.append('archivo', archivoFoto.value)
    form.append('descripcion', descripcionFoto.value)
    const res = await upload(`/api/evidencia/${props.item.idSolicitud}`, form)
    if (!res) return
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      throw new Error(err.error || `Error ${res.status}`)
    }
    limpiarSeleccion()
    await cargarEvidencias()
    toast('Foto subida correctamente', 'success')
  } catch (e) { toast(e.message, 'error') } finally { subiendoFoto.value = false }
}

async function eliminarFoto(idEvidenciaFoto) {
  eliminandoFotoId.value = idEvidenciaFoto
  try {
    const res = await del(`/api/evidencia/${idEvidenciaFoto}`)
    if (!res?.ok) throw new Error('No se pudo eliminar')
    if (blobFotos.value[idEvidenciaFoto]) {
      URL.revokeObjectURL(blobFotos.value[idEvidenciaFoto])
      const copia = { ...blobFotos.value }
      delete copia[idEvidenciaFoto]
      blobFotos.value = copia
    }
    evidencias.value = evidencias.value.filter(f => f.idEvidenciaFoto !== idEvidenciaFoto)
    toast('Foto eliminada', 'success')
  } catch (e) { toast(e.message, 'error') } finally { eliminandoFotoId.value = null }
}

onUnmounted(() => {
  if (previewLocal.value) URL.revokeObjectURL(previewLocal.value)
  Object.values(blobFotos.value).forEach(u => URL.revokeObjectURL(u))
  cerrarCamara()
})

// ── Guardar atención ──────────────────────────────────────────────
async function guardarAtencion() {
  if (!formAtender.value.idPrioridad) {
    toast('Selecciona una prioridad', 'error'); return
  }
  if (!formAtender.value.idsServicios.length) {
    toast('Selecciona al menos un servicio', 'error'); return
  }
  guardando.value = true
  try {
    const body = {
      idDetalleSolicitud: props.item.idDetalleSolicitud,
      idFallaReportada:   props.item.idFallaReportada ?? null,
      idsFallas:          formAtender.value.idsFallas,
      idPrioridad:        Number(formAtender.value.idPrioridad),
      idsServicios:       formAtender.value.idsServicios,
    }
    const res = await post('/api/atencion-solicitud/atender', body)
    if (!res) return
    if (!res.ok) throw new Error('Error al guardar la atención')

    const pri = prioridades.value.find(p => p.idPrioridad === Number(formAtender.value.idPrioridad))
    const srvNombres = formAtender.value.idsServicios
      .map(id => servicios.value.find(s => s.idServicio === id)?.nombreServ)
      .filter(Boolean).join(', ')

    toast('Atención registrada correctamente', 'success')
    cerrar()
    emit('guardado', {
      idDetalleSolicitud: props.item.idDetalleSolicitud,
      prioridad:          pri?.nombrePri ?? null,
      ordenPrioridad:     pri?.ordenPri ?? 999,
      servicio:           srvNombres || null,
      idPrioridad:        Number(formAtender.value.idPrioridad),
      idsServicios:       formAtender.value.idsServicios,
    })
  } catch (e) { toast(e.message, 'error') } finally { guardando.value = false }
}

function etiquetaPrioridad(valor) {
  return { ALTO: 'Alta', MEDIO: 'Media', BAJO: 'Baja' }[valor] ?? (valor || 'Sin prioridad')
}
</script>

<template>
  <Teleport to="body">
    <!-- Overlay -->
    <Transition name="overlay">
      <div v-if="modelValue" class="fixed inset-0 z-[998] bg-black/60" @click="cerrar" />
    </Transition>

    <!-- Drawer -->
    <Transition name="drawer">
      <div
        v-if="modelValue"
        class="fixed inset-y-0 ltr:right-0 rtl:left-0 z-[999] flex flex-col bg-white dark:bg-[#0e1726] shadow-2xl"
        style="width: 100%; max-width: 460px"
      >
        <!-- Franja superior de color -->
        <div class="h-1 w-full flex-shrink-0" :class="item?.idAtencion ? 'bg-info' : 'bg-success'" />

        <!-- Header -->
        <div class="flex items-center justify-between px-6 py-5 border-b border-[#e0e6ed] dark:border-[#1b2e4b] flex-shrink-0 bg-[#fbfbfb] dark:bg-[#121c2c]">
          <div class="flex items-center gap-3">
            <div class="grid h-11 w-11 place-content-center rounded-xl"
              :class="item?.idAtencion ? 'bg-info/10 text-info' : 'bg-success/10 text-success'">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" /><polyline points="22 4 12 14.01 9 11.01" />
              </svg>
            </div>
            <div>
              <h5 class="font-bold text-base dark:text-white-light">
                {{ item?.idAtencion ? 'Ver atención' : 'Atender recurso' }}
              </h5>
              <p class="text-xs text-white-dark mt-0.5">{{ item?.nroSolicitud || '' }}</p>
            </div>
          </div>
          <button type="button"
            class="grid h-8 w-8 place-content-center rounded-lg text-white-dark hover:bg-[#f1f5ff] dark:hover:bg-[#1b2e4b] transition-colors"
            @click="cerrar">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <!-- Body scrollable -->
        <div class="flex-1 overflow-y-auto px-6 py-6 space-y-5">

          <!-- Info del recurso -->
          <div class="rounded-xl bg-white-light/50 dark:bg-[#1b2e4b] p-4 space-y-3">
            <div class="flex items-start justify-between gap-2">
              <div>
                <p class="font-bold leading-tight">{{ item?.descripcionActivo }}</p>
                <p class="text-xs font-mono text-white-dark mt-0.5">{{ item?.codigoActivo }}</p>
              </div>
              <span class="badge bg-secondary/10 text-secondary text-xs shrink-0">{{ item?.tipoActivo }}</span>
            </div>
            <div v-if="item?.problemaReportado">
              <p class="text-xs uppercase font-semibold text-white-dark mb-1">Problema reportado</p>
              <p class="text-sm bg-warning/5 border border-warning/20 rounded-lg px-3 py-2 leading-relaxed">
                {{ item.problemaReportado }}
              </p>
            </div>
            <div v-else>
              <p class="text-xs uppercase font-semibold text-white-dark mb-1">Problema reportado</p>
              <p class="text-sm text-white-dark italic">Sin descripción</p>
            </div>
          </div>

          <!-- ── FALLAS (acordeón) ── -->
          <div v-if="!item?.idAtencion" class="border border-[#e0e6ed] dark:border-[#1b2e4b] rounded-xl overflow-hidden">
            <!-- Header fallas -->
            <button
              type="button"
              class="w-full flex items-center justify-between px-4 py-3 bg-[#f8f9fa] dark:bg-[#121c2c] hover:bg-[#f1f5ff] dark:hover:bg-[#1b2e4b] transition-colors"
              @click="seccionFallasAbierta = !seccionFallasAbierta"
            >
              <span class="flex items-center gap-2 font-bold text-sm dark:text-white">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="text-primary">
                  <path opacity="0.5" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z" fill="currentColor" stroke="none"/>
                  <path d="M12 7v6M12 17h.01" stroke-linecap="round"/>
                </svg>
                Fallas detectadas
                <span class="text-xs font-normal text-white-dark">(opcional)</span>
              </span>
              <span class="flex items-center gap-2">
                <span v-if="formAtender.idsFallas.length > 0"
                  class="badge bg-primary/10 text-primary text-xs py-0.5 px-2 rounded-full">
                  {{ formAtender.idsFallas.length }} selec.
                </span>
                <svg class="shrink-0 transition-transform duration-200 text-white-dark" :class="seccionFallasAbierta ? 'rotate-180' : ''" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M6 9l6 6 6-6"/>
                </svg>
              </span>
            </button>

            <!-- Cuerpo fallas -->
            <Transition name="accordion">
              <div v-show="seccionFallasAbierta" class="px-4 pb-4 pt-3 space-y-3">
                <!-- Selector tipo + crear tipo -->
                <div class="flex gap-2">
                  <select
                    v-model="tipoFallaSeleccionado"
                    class="form-select text-sm flex-1"
                    @change="cargarFallasPorTipo(tipoFallaSeleccionado)"
                  >
                    <option value="">— Tipo de recurso —</option>
                    <option v-for="t in tiposActivo" :key="t.idTipoActivo" :value="String(t.idTipoActivo)">
                      {{ t.nombreTipoAct }}
                    </option>
                  </select>
                  <button type="button" class="btn btn-sm shrink-0"
                    :class="crearTipoActAbierto ? 'btn-outline-danger' : 'btn-outline-secondary'"
                    @click="toggleCrearTipoAct">{{ crearTipoActAbierto ? '✕' : '+' }}</button>
                </div>

                <Transition name="fade">
                  <div v-if="crearTipoActAbierto" class="flex gap-2">
                    <input v-model="nuevoTipoActNombre" type="text" class="form-input text-sm flex-1"
                      placeholder="Nombre del tipo (ej: Laptop)" autocomplete="off"
                      @keydown.enter.prevent="guardarNuevoTipoAct" />
                    <button type="button" class="btn btn-secondary btn-sm shrink-0"
                      :disabled="guardandoNuevoTipoAct" @click="guardarNuevoTipoAct">
                      {{ guardandoNuevoTipoAct ? '...' : 'Crear' }}
                    </button>
                  </div>
                </Transition>

                <!-- Buscador fallas -->
                <div v-if="!cargandoFallas && fallasDisponibles.length > 4" class="relative">
                  <input v-model="buscarFalla" type="text" placeholder="Buscar falla..."
                    class="form-input text-sm py-1.5 ltr:pr-9 rtl:pl-9 w-full" />
                  <span class="absolute top-1/2 -translate-y-1/2 ltr:right-3 rtl:left-3 text-white-dark">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5"/><path d="M18.5 18.5L22 22" stroke-linecap="round"/>
                    </svg>
                  </span>
                </div>

                <div v-if="cargandoFallas" class="text-sm text-white-dark animate-pulse">Cargando fallas...</div>
                <div v-else-if="!tipoFallaSeleccionado && !crearFallaAbierto" class="text-sm text-white-dark italic">
                  Selecciona un tipo de recurso para ver sus fallas.
                </div>
                <div v-else-if="tipoFallaSeleccionado && fallasDisponibles.length === 0 && !crearFallaAbierto" class="text-sm text-white-dark italic">
                  No hay fallas catalogadas para este tipo de recurso.
                </div>
                <div v-else-if="fallasDisponibles.length > 0 && fallasFiltradas.length === 0" class="text-sm text-white-dark italic">
                  Sin resultados para "{{ buscarFalla }}".
                </div>

                <div v-if="!cargandoFallas && fallasFiltradas.length > 0" class="flex flex-wrap gap-2">
                  <button v-for="f in fallasFiltradas" :key="f.idFalla" type="button"
                    class="px-3 py-1.5 rounded-full text-sm font-medium border transition-all duration-150"
                    :class="fallaSeleccionada(f.idFalla)
                      ? 'bg-primary text-white border-primary shadow-md scale-105'
                      : 'bg-white dark:bg-[#1b2e4b] text-gray-600 dark:text-white-dark border-white-light dark:border-[#253b5e] hover:border-primary hover:text-primary'"
                    @click="toggleFalla(f.idFalla)">{{ f.nombreFall }}</button>
                </div>

                <!-- Botón nueva falla + panel inline -->
                <div class="flex justify-between items-center pt-1">
                  <button type="button"
                    :class="crearFallaAbierto ? 'btn btn-xs btn-outline-danger' : 'btn btn-xs btn-outline-primary'"
                    @click="toggleCrearFalla">
                    {{ crearFallaAbierto ? '✕ Cancelar' : '+ Nueva falla' }}
                  </button>
                </div>
                <Transition name="fade">
                  <div v-if="crearFallaAbierto" class="p-3 rounded-xl border border-primary/30 bg-primary/5 dark:bg-[#1b2e4b] space-y-3">
                    <input v-model="nuevaFalla.nombreFall" type="text" class="form-input text-sm"
                      placeholder="Nombre (ej: Pantalla dañada)" autocomplete="off"
                      @keydown.enter.prevent="guardarNuevaFalla" />
                    <select v-model="nuevaFalla.idTipoActivo" class="form-select text-sm">
                      <option value="">— Tipo de recurso (opcional) —</option>
                      <option v-for="t in tiposActivo" :key="t.idTipoActivo" :value="String(t.idTipoActivo)">{{ t.nombreTipoAct }}</option>
                    </select>
                    <div class="flex justify-end gap-2">
                      <button type="button" class="btn btn-sm btn-outline-danger" @click="toggleCrearFalla">Cancelar</button>
                      <button type="button" class="btn btn-sm btn-primary"
                        :disabled="!nuevaFalla.nombreFall.trim() || guardandoNuevaFalla"
                        @click="guardarNuevaFalla">
                        {{ guardandoNuevaFalla ? 'Creando...' : 'Crear y Seleccionar' }}
                      </button>
                    </div>
                  </div>
                </Transition>
              </div>
            </Transition>
          </div>

          <!-- ── PRIORIDAD (acordeón) ── -->
          <div v-if="!item?.idAtencion" class="border border-[#e0e6ed] dark:border-[#1b2e4b] rounded-xl overflow-hidden">
            <button
              type="button"
              class="w-full flex items-center justify-between px-4 py-3 bg-[#f8f9fa] dark:bg-[#121c2c] hover:bg-[#f1f5ff] dark:hover:bg-[#1b2e4b] transition-colors"
              @click="seccionPrioridadAbierta = !seccionPrioridadAbierta"
            >
              <span class="flex items-center gap-2 font-bold text-sm dark:text-white">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="text-warning">
                  <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                </svg>
                Prioridad
                <span class="text-danger text-xs">*</span>
              </span>
              <span class="flex items-center gap-2">
                <span v-if="formAtender.idPrioridad" class="text-xs font-semibold"
                  :class="prioridades.find(p=>String(p.idPrioridad)===formAtender.idPrioridad)?.nombrePri === 'ALTO' ? 'text-danger'
                        : prioridades.find(p=>String(p.idPrioridad)===formAtender.idPrioridad)?.nombrePri === 'MEDIO' ? 'text-warning'
                        : 'text-success'">
                  {{ etiquetaPrioridad(prioridades.find(p=>String(p.idPrioridad)===formAtender.idPrioridad)?.nombrePri) }}
                </span>
                <span v-else class="text-xs text-white-dark">Sin selección</span>
                <svg class="shrink-0 transition-transform duration-200 text-white-dark" :class="seccionPrioridadAbierta ? 'rotate-180' : ''" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M6 9l6 6 6-6"/>
                </svg>
              </span>
            </button>
            <Transition name="accordion">
              <div v-show="seccionPrioridadAbierta" class="px-4 pb-4 pt-3">
                <div class="flex flex-wrap gap-3">
                  <button v-for="p in prioridades" :key="p.idPrioridad" type="button"
                    class="px-5 py-2 rounded-full text-sm font-semibold border-2 transition-all duration-150"
                    :class="formAtender.idPrioridad === String(p.idPrioridad)
                      ? (p.nombrePri === 'ALTO'  ? 'bg-danger  text-white border-danger  shadow-md scale-105'
                       : p.nombrePri === 'MEDIO' ? 'bg-warning text-white border-warning shadow-md scale-105'
                       :                           'bg-success text-white border-success shadow-md scale-105')
                      : 'bg-white dark:bg-[#1b2e4b] text-gray-600 dark:text-white-dark border-white-light dark:border-[#253b5e] hover:border-primary'"
                    @click="formAtender.idPrioridad = String(p.idPrioridad); seccionPrioridadAbierta = false">
                    {{ etiquetaPrioridad(p.nombrePri) }}
                  </button>
                </div>
              </div>
            </Transition>
          </div>

          <!-- ── SERVICIOS (acordeón) ── -->
          <div v-if="!item?.idAtencion" class="border border-[#e0e6ed] dark:border-[#1b2e4b] rounded-xl overflow-hidden">
            <button
              type="button"
              class="w-full flex items-center justify-between px-4 py-3 bg-[#f8f9fa] dark:bg-[#121c2c] hover:bg-[#f1f5ff] dark:hover:bg-[#1b2e4b] transition-colors"
              @click="seccionServiciosAbierta = !seccionServiciosAbierta"
            >
              <span class="flex items-center gap-2 font-bold text-sm dark:text-white">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="text-success">
                  <circle cx="12" cy="12" r="3"/><path opacity="0.5" d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/>
                </svg>
                Servicios a realizar
                <span class="text-danger text-xs">*</span>
              </span>
              <span class="flex items-center gap-2">
                <span v-if="formAtender.idsServicios.length > 0"
                  class="badge bg-success/10 text-success text-xs py-0.5 px-2 rounded-full">
                  {{ formAtender.idsServicios.length }} selec.
                </span>
                <span v-else class="text-xs text-white-dark">Sin selección</span>
                <svg class="shrink-0 transition-transform duration-200 text-white-dark" :class="seccionServiciosAbierta ? 'rotate-180' : ''" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M6 9l6 6 6-6"/>
                </svg>
              </span>
            </button>
            <Transition name="accordion">
              <div v-show="seccionServiciosAbierta" class="px-4 pb-4 pt-3 space-y-3">
                <!-- Buscador -->
                <div class="relative">
                  <input v-model="buscarServicio" type="text" placeholder="Buscar servicio..."
                    class="form-input text-sm py-1.5 ltr:pr-9 rtl:pl-9 w-full" />
                  <span class="absolute top-1/2 -translate-y-1/2 ltr:right-3 rtl:left-3 text-white-dark">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5"/><path d="M18.5 18.5L22 22" stroke-linecap="round"/>
                    </svg>
                  </span>
                </div>

                <div v-if="serviciosPorTipo.length === 0" class="text-sm text-white-dark italic">
                  Sin resultados para "{{ buscarServicio }}".
                </div>

                <!-- Chips agrupados -->
                <div class="space-y-3">
                  <div v-for="grupo in serviciosPorTipo" :key="grupo.label">
                    <p class="text-xs font-bold uppercase tracking-widest text-white-dark border-b border-white-light dark:border-[#253b5e] pb-1 mb-2">
                      {{ grupo.label }}
                    </p>
                    <div class="flex flex-wrap gap-2">
                      <button v-for="s in grupo.items" :key="s.idServicio" type="button"
                        class="px-3 py-1.5 rounded-full text-sm font-medium border transition-all duration-150"
                        :class="servicioSeleccionado(s.idServicio)
                          ? 'bg-success text-white border-success shadow-md scale-105'
                          : 'bg-white dark:bg-[#1b2e4b] text-gray-600 dark:text-white-dark border-white-light dark:border-[#253b5e] hover:border-success hover:text-success'"
                        @click="toggleServicio(s.idServicio)">{{ s.nombreServ }}</button>
                    </div>
                  </div>
                </div>

                <!-- Botón nuevo servicio + panel inline -->
                <div v-if="auth.isAdmin" class="pt-1">
                  <button type="button"
                    :class="crearServicioAbierto ? 'btn btn-xs btn-outline-danger' : 'btn btn-xs btn-outline-primary'"
                    @click="toggleCrearServicio">
                    {{ crearServicioAbierto ? '✕ Cancelar' : '+ Nuevo servicio' }}
                  </button>
                </div>
                <Transition name="fade">
                  <div v-if="crearServicioAbierto" class="p-3 rounded-xl border border-success/30 bg-success/5 dark:bg-[#1b2e4b] space-y-3">
                    <select v-model="nuevoServicioTipoId" class="form-select text-sm">
                      <option :value="null">— Tipo de servicio (opcional) —</option>
                      <option v-for="t in tiposServicio" :key="t.idTipoServicio" :value="t.idTipoServicio">{{ t.nombreTipo }}</option>
                    </select>
                    <input v-model="nuevoServicioNombre" type="text" class="form-input text-sm"
                      placeholder="Nombre del servicio" autocomplete="off"
                      @keydown.enter.prevent="guardarNuevoServicio" />
                    <div class="flex justify-end gap-2">
                      <button type="button" class="btn btn-sm btn-outline-danger" @click="toggleCrearServicio">Cancelar</button>
                      <button type="button" class="btn btn-sm btn-success"
                        :disabled="!nuevoServicioNombre.trim() || guardandoNuevoServicio"
                        @click="guardarNuevoServicio">
                        {{ guardandoNuevoServicio ? 'Creando...' : 'Crear y Seleccionar' }}
                      </button>
                    </div>
                  </div>
                </Transition>
              </div>
            </Transition>
          </div>

          <!-- ── EVIDENCIAS FOTOGRÁFICAS ── -->
          <div v-if="item?.idSolicitud" class="border-t border-white-light dark:border-[#253b5e] pt-4">
            <button
              type="button"
              class="flex items-center justify-between w-full text-left"
              @click="evidenciasAbierto = !evidenciasAbierto; if (evidenciasAbierto && !evidencias.length) cargarEvidencias()"
            >
              <span class="text-base font-bold dark:text-white flex items-center gap-2">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                  <circle cx="12" cy="13" r="4"/>
                </svg>
                Evidencias fotográficas
                <span class="text-xs font-normal text-white-dark">({{ evidencias.length }}/5)</span>
              </span>
              <span class="text-white-dark text-lg leading-none">{{ evidenciasAbierto ? '▲' : '▼' }}</span>
            </button>

            <Transition name="fade">
              <div v-if="evidenciasAbierto" class="mt-4 space-y-4">

                <!-- Preview local del archivo elegido -->
                <div v-if="previewLocal" class="relative w-full rounded-xl overflow-hidden border border-primary/30">
                  <img :src="previewLocal" alt="Preview" class="w-full max-h-48 object-contain bg-black/5" />
                  <button
                    type="button"
                    class="absolute top-2 right-2 bg-black/50 hover:bg-black/70 text-white rounded-full w-7 h-7 flex items-center justify-center text-sm leading-none transition-colors"
                    @click="limpiarSeleccion"
                  >✕</button>
                </div>

                <!-- Upload -->
                <div v-if="evidencias.length < 5" class="space-y-2">
                  <!-- Input galería -->
                  <input
                    ref="archivoInputRef"
                    type="file"
                    accept=".jpg,.jpeg,.png"
                    class="hidden"
                    @change="handleFileChange"
                  />

                  <!-- Nombre del archivo seleccionado -->
                  <div v-if="archivoFoto" class="flex items-center gap-2 px-3 py-2 rounded-lg bg-primary/5 border border-primary/20 text-sm text-primary">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                      <polyline points="14 2 14 8 20 8"/>
                    </svg>
                    <span class="truncate flex-1">{{ archivoFoto.name }}</span>
                    <button type="button" class="shrink-0 text-danger/70 hover:text-danger" @click="limpiarSeleccion">✕</button>
                  </div>

                  <!-- Botones de selección -->
                  <div v-else class="grid grid-cols-2 gap-2">
<!--                    <button-->
<!--                      type="button"-->
<!--                      class="flex items-center justify-center gap-2 px-3 py-3 rounded-xl border-2 border-dashed border-info/40 hover:border-info bg-info/5 hover:bg-info/10 text-info transition-all text-sm font-medium"-->
<!--                      @click="abrirCamara"-->
<!--                    >-->
<!--                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">-->
<!--                        <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>-->
<!--                        <circle cx="12" cy="13" r="4"/>-->
<!--                      </svg>-->
<!--                      Tomar foto-->
<!--                    </button>-->
                    <button
                      type="button"
                      class="flex items-center justify-center gap-2 px-3 py-3 rounded-xl border-2 border-dashed border-primary/40 hover:border-primary bg-primary/5 hover:bg-primary/10 text-primary transition-all text-sm font-medium"
                      @click="seleccionarArchivo"
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                        <circle cx="8.5" cy="8.5" r="1.5"/>
                        <polyline points="21 15 16 10 5 21"/>
                      </svg>
                      Galería
                    </button>
                  </div>
                  <input
                    v-model="descripcionFoto"
                    type="text"
                    class="form-input text-sm"
                    placeholder="Descripción (opcional)"
                  />
                  <button
                    type="button"
                    class="btn btn-sm btn-primary w-full"
                    :disabled="!archivoFoto || subiendoFoto"
                    @click="subirFoto"
                  >
                    <span v-if="!subiendoFoto">Subir foto</span>
                    <span v-else class="flex items-center justify-center gap-2">
                      <span class="animate-spin border-2 border-white border-l-transparent rounded-full w-4 h-4" />
                      Subiendo...
                    </span>
                  </button>
                </div>
                <p v-else class="text-xs text-warning">Límite de 5 fotos alcanzado.</p>

                <!-- Fotos ya subidas -->
                <div v-if="cargandoEvidencias" class="flex justify-center py-4">
                  <span class="animate-spin border-4 border-primary border-l-transparent rounded-full w-7 h-7" />
                </div>
                <div v-else-if="evidencias.length === 0" class="text-sm text-white-dark italic text-center py-2">
                  Sin fotos aún.
                </div>
                <div v-else class="grid grid-cols-2 gap-2">
                  <div
                    v-for="foto in evidencias"
                    :key="foto.idEvidenciaFoto"
                    class="relative rounded-xl overflow-hidden border border-white-light dark:border-[#253b5e] aspect-square"
                    :class="eliminandoFotoId === foto.idEvidenciaFoto ? 'opacity-40' : ''"
                  >
                    <img
                      v-if="blobFotos[foto.idEvidenciaFoto]"
                      :src="blobFotos[foto.idEvidenciaFoto]"
                      :alt="foto.descripcion || foto.nombreOriginal"
                      class="w-full h-full object-cover"
                    />
                    <div v-else class="w-full h-full bg-gray-100 dark:bg-[#1b2e4b] flex items-center justify-center">
                      <span class="animate-spin border-2 border-primary border-l-transparent rounded-full w-5 h-5" />
                    </div>
                    <div class="absolute inset-x-0 bottom-0 bg-black/50 px-2 py-1">
                      <p class="text-white text-[10px] truncate">{{ foto.descripcion || foto.nombreOriginal }}</p>
                    </div>
                    <button
                      type="button"
                      class="absolute top-1 right-1 bg-danger/80 hover:bg-danger text-white rounded-full w-6 h-6 flex items-center justify-center text-xs leading-none transition-colors"
                      :disabled="eliminandoFotoId === foto.idEvidenciaFoto"
                      @click="eliminarFoto(foto.idEvidenciaFoto)"
                    >✕</button>
                  </div>
                </div>

              </div>
            </Transition>
          </div>

        </div>

        <!-- Footer fijo -->
        <div class="flex-shrink-0 border-t border-[#e0e6ed] dark:border-[#1b2e4b] px-6 py-4 bg-[#fbfbfb] dark:bg-[#121c2c]">
          <div class="flex items-center justify-between">
            <div class="text-sm text-white-dark">
              <span v-if="formAtender.idsFallas.length > 0">{{ formAtender.idsFallas.length }} falla(s) · </span>
              <span v-if="formAtender.idsServicios.length > 0" class="text-success font-semibold">
                {{ formAtender.idsServicios.length }} servicio(s)
              </span>
            </div>
            <div class="flex gap-3">
              <button type="button" class="btn btn-outline-danger" @click="cerrar">Cancelar</button>
              <button
                v-if="!item?.idAtencion"
                type="button"
                class="btn btn-success px-6"
                :disabled="guardando"
                @click="guardarAtencion"
              >
                <span v-if="guardando" class="flex items-center gap-2">
                  <span class="animate-spin border-2 border-white border-l-transparent rounded-full w-4 h-4 inline-block" />
                  Guardando...
                </span>
                <span v-else>Guardar Atención</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
    <!-- Modal cámara -->
    <Transition name="fade">
      <div v-if="camaraModalAbierto" class="fixed inset-0 z-[1100] bg-black flex flex-col">
        <!-- Header -->
        <div class="flex items-center justify-between px-5 py-4 bg-black/70 shrink-0">
          <span class="text-white font-bold text-base flex items-center gap-2">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
              <circle cx="12" cy="13" r="4"/>
            </svg>
            Tomar foto
          </span>
          <button type="button" class="text-white/70 hover:text-white transition-colors" @click="cerrarCamara">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>

        <!-- Video stream -->
        <video ref="videoRef" autoplay playsinline muted class="flex-1 w-full object-contain bg-black" />
        <canvas ref="canvasRef" class="hidden" />

        <!-- Botón capturar (pendiente de implementar) -->
        <!-- <div class="flex justify-center items-center py-8 bg-black/70 shrink-0">
          <button
            type="button"
            class="w-18 h-18 rounded-full bg-white border-[5px] border-white/40 hover:scale-95 active:scale-90 transition-transform shadow-2xl"
            style="width:72px;height:72px"
            @click="capturarFoto"
          /> -->
        <div class="flex justify-center items-center py-8 bg-black/70 shrink-0">
          <p class="text-white/50 text-sm">Próximamente...</p>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.accordion-enter-active, .accordion-leave-active {
  transition: max-height 0.25s ease, opacity 0.2s ease;
  max-height: 600px; overflow: hidden;
}
.accordion-enter-from, .accordion-leave-to { max-height: 0; opacity: 0; }
.overlay-enter-active { transition: opacity 0.3s ease-out; }
.overlay-leave-active { transition: opacity 0.2s ease-in; }
.overlay-enter-from, .overlay-leave-to { opacity: 0; }
.drawer-enter-active { transition: transform 0.3s ease-out; }
.drawer-leave-active { transition: transform 0.2s ease-in; }
.drawer-enter-from, .drawer-leave-to { transform: translateX(100%); }
</style>