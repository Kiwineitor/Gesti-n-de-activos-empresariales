<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { useDownload } from '@/composables/useDownload'
import DrawerAtenderEquipo from '@/components/DrawerAtenderEquipo.vue'

const route = useRoute()
const auth = useAuthStore()
const { get, post, del, upload } = useApi()
const { toast } = useToast()
const { descargarArchivo } = useDownload()

const id = route.params.id
const esAdmin = auth.isAdmin
const esTecnico = auth.isTecnico

// ── Estado ────────────────────────────────────────────────────────
const solicitud = ref(null)
const cargando = ref(false)

// ── Drawer: registrar atención ───────────────────────────────────
const drawerAtender = ref(false)
const activoSeleccionado = ref(null)

// ── Modal: Ficha Técnica (observación + descarga PDF) ─────────────
const modalObservacion = ref(false)
const solicitudParaReporte = ref(null)
const observacionTexto = ref('')
const descargandoFicha = ref(null)

// ── Modal: Informe General ───────────────────────────────────────
const modalInformeGen = ref(false)
const formInformeGen = ref({ observaciones: '', resumenTecnico: '' })
const guardandoInformeGen = ref(false)

// ── Modal: Evidencias / Archivos ─────────────────────────────────
const modalEvidencias = ref(false)
const evidencias = ref([])
const cargandoEvidencias = ref(false)
const archivoEvidencia = ref(null)
const descripcionEvidencia = ref('')
const subiendoArchivo = ref(false)
const eliminandoId = ref(null)

// ── Init ─────────────────────────────────────────────────────────
onMounted(cargar)

async function cargar() {
  cargando.value = true
  try {
    const res = await get(`/api/solicitud/detalle/${id}`)
    if (!res) return
    if (!res.ok) throw new Error('No se pudo cargar el detalle')
    solicitud.value = await res.json()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    cargando.value = false
  }
}

function abrirAtender(activo) {
  activoSeleccionado.value = {
    ...activo,
    problemaReportado: activo.problemaEspecifico ?? null,
    idSolicitud: solicitud.value.idSolicitud,
  }
  drawerAtender.value = true
}

async function onGuardado() {
  await cargar()
}

// ── Ficha Técnica: modal de observación + descarga PDF ────────────
function abrirModalFicha(s) {
  solicitudParaReporte.value = s
  observacionTexto.value = s.observacionFicha || ''
  modalObservacion.value = true
}

async function confirmarDescargaFicha() {
  const s = solicitudParaReporte.value
  if (!s?.nroSol) return
  modalObservacion.value = false
  await descargarFicha(s, observacionTexto.value.trim())
  observacionTexto.value = ''
  solicitudParaReporte.value = null
  await cargar()
}

async function descargarFicha(s, observacion = '') {
  const key = 'ficha-' + s.nroSol
  descargandoFicha.value = key
  try {
    let url = `/api/reportes/ficha-tecnica/${s.nroSol}`
    if (observacion) url += `?observacion=${encodeURIComponent(observacion)}`
    const res = await get(url)
    if (!res || !res.ok) {
      const err = await res?.json().catch(() => ({}))
      throw new Error(err?.message || 'No se pudo generar el PDF')
    }
    const blob = await res.blob()
    const resultado = await descargarArchivo(blob, `Ficha_Tecnica_${s.nroSol}.pdf`)
    if (resultado?.mensaje) toast(resultado.mensaje, 'success')
    else toast('Ficha técnica descargada', 'success')
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    descargandoFicha.value = null
  }
}

async function guardarInformeGen() {
  guardandoInformeGen.value = true
  try {
    const body = { ...formInformeGen.value, idSolicitud: id }
    // @PostMapping en /api/informes-equipo-general (sin /guardar)
    const res = await post('/api/informes-equipo-general', body)
    if (!res) return
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      throw new Error(err.message || `Error ${res.status}`)
    }
    toast('Informe general guardado', 'success')
    modalInformeGen.value = false
    await cargar()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    guardandoInformeGen.value = false
  }
}

async function abrirEvidencias() {
  modalEvidencias.value = true
  await cargarEvidencias()
}

async function cargarEvidencias() {
  cargandoEvidencias.value = true
  try {
    const res = await get(`/api/evidencia/${id}`)
    if (!res) return
    if (!res.ok) throw new Error('No se pudieron cargar las evidencias')
    evidencias.value = await res.json()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    cargandoEvidencias.value = false
  }
}

async function subirArchivo() {
  if (!archivoEvidencia.value) return
  subiendoArchivo.value = true
  try {
    const form = new FormData()
    form.append('archivo', archivoEvidencia.value)
    form.append('descripcion', descripcionEvidencia.value)
    const res = await upload(`/api/evidencia/${id}`, form)
    if (!res) return
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      throw new Error(err.error || `Error ${res.status}`)
    }
    archivoEvidencia.value = null
    descripcionEvidencia.value = ''
    await cargarEvidencias()
    toast('Archivo subido correctamente', 'success')
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    subiendoArchivo.value = false
  }
}

async function eliminarArchivo(idEvidencia) {
  eliminandoId.value = idEvidencia
  try {
    const res = await del(`/api/evidencia/${idEvidencia}`)
    if (!res) return
    if (!res.ok) throw new Error('No se pudo eliminar')
    evidencias.value = evidencias.value.filter(e => e.idEvidenciaFoto !== idEvidencia)
    toast('Archivo eliminado', 'success')
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    eliminandoId.value = null
  }
}

function esPdf(nombreOriginal) {
  return nombreOriginal?.toLowerCase().endsWith('.pdf')
}

function urlArchivo(idEvidenciaFoto) {
  return `${import.meta.env.VITE_API_BASE || ''}/api/evidencia/foto/${idEvidenciaFoto}`
}

function badgeClase(estado) {
  return (
    {
      PENDIENTE: 'sath-badge sath-badge-warning',
      EN_MANTENIMIENTO: 'sath-badge sath-badge-info',
      FINALIZADO: 'sath-badge sath-badge-success',
    }[estado] ?? 'sath-badge sath-badge-secondary'
  )
}
</script>

<template>
  <!-- Breadcrumb -->
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2">
      <RouterLink to="/solicitud" class="text-primary hover:underline">Solicitudes</RouterLink>
    </li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Detalle</li>
  </ul>

  <div v-if="cargando" class="flex justify-center py-16">
    <span class="animate-spin border-4 border-primary border-l-transparent rounded-full w-10 h-10" />
  </div>

  <div v-else-if="!solicitud" class="panel text-center text-white-dark py-10">
    No se encontró la solicitud.
  </div>

  <div v-else>
    <!-- Cabecera de la solicitud -->
    <div class="panel mb-6">
      <div class="flex flex-wrap items-start justify-between gap-4 mb-5">
        <div>
          <h5 class="text-xl font-bold dark:text-white-light/90">
            Solicitud {{ solicitud.nroSol != null ? solicitud.nroSol : '#' + solicitud.idSolicitud }}
          </h5>
          <p class="text-white-dark text-sm mt-1">{{ solicitud.descripcionSol }}</p>
        </div>
        <div class="flex gap-2 items-center flex-wrap">
          <span :class="badgeClase(solicitud.estadoSol)">{{ solicitud.estadoSol }}</span>
          <button
            v-if="(esAdmin || esTecnico) && (solicitud.estadoSol === 'EN_MANTENIMIENTO' || solicitud.estadoSol === 'FINALIZADO')"
            class="btn btn-sm btn-outline-info"
            :disabled="descargandoFicha === 'ficha-' + solicitud.nroSol"
            @click="abrirModalFicha(solicitud)"
          >
            <span v-if="descargandoFicha === 'ficha-' + solicitud.nroSol"
              class="animate-spin border-2 border-info border-l-transparent rounded-full w-3 h-3 inline-block mr-1" />
            Ficha Técnica
          </button>
          <button
            v-if="(esAdmin || esTecnico) && solicitud.estadoSol === 'EN_MANTENIMIENTO' && !solicitud.tieneInformeGen"
            class="btn btn-sm btn-outline-success"
            @click="modalInformeGen = true"
          >
            Informe General
          </button>
        </div>
      </div>

      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
        <div>
          <span class="text-white-dark">Fecha:</span>
          <p class="font-semibold dark:text-white">{{ solicitud.fechaSol || '—' }}</p>
        </div>
        <div>
          <span class="text-white-dark">Solicitante:</span>
          <!-- Campo real: solicitante -->
          <p class="font-semibold dark:text-white">{{ solicitud.solicitante || '—' }}</p>
        </div>
        <div>
          <span class="text-white-dark">Unidad:</span>
          <!-- Campo real: unidad -->
          <p class="font-semibold dark:text-white">{{ solicitud.unidad || '—' }}</p>
        </div>
        <div>
          <span class="text-white-dark">Nro. Externo:</span>
          <p class="font-semibold dark:text-white">{{ solicitud.nroSolExterna || '—' }}</p>
        </div>
        <div v-if="solicitud.cargo">
          <span class="text-white-dark">Cargo:</span>
          <p class="font-semibold dark:text-white">{{ solicitud.cargo }}</p>
        </div>
        <div>
          <span class="text-white-dark">Teléfono:</span>
          <p class="font-semibold dark:text-white">{{ solicitud.celular || '—' }}</p>
        </div>
        <div>
          <span class="text-white-dark">Correo:</span>
          <p class="font-semibold dark:text-white break-all">{{ solicitud.correo || '—' }}</p>
        </div>
      </div>
    </div>

    <!-- Activos / detalles -->
    <div class="panel">
      <h5 class="text-lg font-semibold dark:text-white-light/90 mb-4">
        Activos en esta solicitud
        <span class="ml-2 text-sm font-normal text-white-dark">
          ({{ (solicitud.activos || []).length }} registro(s))
        </span>
      </h5>
      <div class="table-responsive">
        <table class="table-striped table-hover">
          <thead>
            <tr>
              <th>Activo</th>
              <th class="hidden sm:table-cell">Tipo</th>
              <th class="hidden md:table-cell">Código</th>
              <th class="hidden lg:table-cell">Problema</th>
              <th class="text-center">Estado</th>
              <th class="text-center hidden sm:table-cell">Prioridad</th>
              <th v-if="esAdmin || esTecnico" class="text-center">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!solicitud.activos || solicitud.activos.length === 0">
              <td colspan="7" class="text-center text-white-dark py-6">Sin activos registrados</td>
            </tr>
            <tr v-for="activo in solicitud.activos" :key="activo.idDetalleSolicitud">
              <td>
                <div class="font-semibold dark:text-white">{{ activo.descripcionActivo || '—' }}</div>
                <div v-if="activo.codigoActivo" class="text-xs font-mono text-primary mt-0.5">{{ activo.codigoActivo }}</div>
                <div class="sm:hidden flex flex-wrap gap-x-3 gap-y-0.5 mt-0.5 text-[10px] text-white-dark">
                  <span v-if="activo.tipoActivo">{{ activo.tipoActivo }}</span>
                  <span v-if="activo.prioridad" class="text-white-dark/70">{{ activo.prioridad }}</span>
                </div>
                <div v-if="activo.problemaEspecifico" class="lg:hidden mt-0.5 text-[10px] text-white-dark line-clamp-1" :title="activo.problemaEspecifico">
                  {{ activo.problemaEspecifico }}
                </div>
              </td>
              <td class="text-white-dark hidden sm:table-cell">{{ activo.tipoActivo || '—' }}</td>
              <td class="text-white-dark font-mono text-xs hidden md:table-cell">{{ activo.codigoActivo || '—' }}</td>
              <td class="text-white-dark text-xs hidden lg:table-cell max-w-[180px]">
                <span class="block truncate" :title="activo.problemaEspecifico">{{ activo.problemaEspecifico || '—' }}</span>
              </td>
              <td class="text-center">
                <span :class="badgeClase(activo.estadoDetalle)">{{ activo.estadoDetalle || '—' }}</span>
              </td>
              <td class="text-center text-white-dark text-xs hidden sm:table-cell">
                {{ activo.prioridad || '—' }}
              </td>
              <td v-if="esAdmin || esTecnico" class="text-center">
                <div class="flex flex-col sm:flex-row items-center justify-center gap-1">
                  <button
                    v-if="!activo.tieneAtencion"
                    class="btn btn-xs btn-outline-primary w-full sm:w-auto"
                    @click="abrirAtender(activo)"
                  >
                    Atender
                  </button>
                  <span v-else class="text-success text-xs font-semibold whitespace-nowrap">✓ Atendido</span>

                  <button
                    class="btn btn-xs btn-outline-info w-full sm:w-auto"
                    :disabled="descargandoFicha === 'ficha-' + solicitud.nroSol"
                    @click="abrirModalFicha(solicitud)"
                  >
                    <span v-if="descargandoFicha === 'ficha-' + solicitud.nroSol"
                      class="animate-spin border-2 border-info border-l-transparent rounded-full w-3 h-3 inline-block" />
                    <span v-else>Ficha Técnica</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <!-- ── Drawer: registrar atención ── -->
  <DrawerAtenderEquipo
    v-model="drawerAtender"
    :item="activoSeleccionado"
    @guardado="onGuardado"
  />

  <!-- ── Modal: Observación para Ficha Técnica ── -->
  <Teleport to="body">
  <Transition name="fade">
    <div
      v-if="modalObservacion"
      class="fixed inset-0 z-[999] overflow-y-auto bg-black/60"
      @click.self="modalObservacion = false"
    >
      <div class="flex min-h-screen items-center justify-center px-4">
        <div class="panel w-full max-w-sm rounded-lg border-0 p-0 overflow-hidden">
          <!-- Header -->
          <div class="flex items-center justify-between bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4">
            <div class="flex items-center gap-3">
              <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary/10">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                </svg>
              </div>
              <div>
                <h3 class="text-base font-semibold">Ficha Técnica</h3>
                <p class="text-xs text-white-dark">{{ solicitudParaReporte?.nroSol || '' }}</p>
              </div>
            </div>
            <button type="button" class="text-white-dark hover:text-dark" @click="modalObservacion = false">✕</button>
          </div>

          <!-- Body -->
          <div class="p-5 space-y-4">
            <p v-if="solicitudParaReporte?.descripcionSol" class="text-sm text-white-dark">
              {{ solicitudParaReporte.descripcionSol.substring(0, 80) }}
            </p>
            <div>
              <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">
                Observación <span class="normal-case font-normal">(opcional)</span>
              </label>
              <textarea v-model="observacionTexto" class="form-textarea w-full" rows="3"
                placeholder="Observaciones del técnico..." />
            </div>
            <div class="flex justify-end gap-2">
              <button class="btn btn-outline-danger" @click="modalObservacion = false">Cancelar</button>
              <button class="btn btn-primary" @click="confirmarDescargaFicha">Descargar PDF</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>
  </Teleport>

  <!-- ── Modal: Informe General ── -->
  <Transition name="fade">
    <div
      v-if="modalInformeGen"
      class="fixed inset-0 z-[999] flex items-center justify-center bg-black/60"
      @click.self="modalInformeGen = false"
    >
      <div class="bg-white dark:bg-[#1b2e4b] rounded-xl p-6 shadow-xl" style="width: 100%; max-width: 512px">
        <h5 class="text-lg font-semibold mb-1 dark:text-white">Informe General</h5>
        <p class="text-white-dark text-sm mb-4">
          Solicitud {{ solicitud?.nroSol }}
        </p>

        <div class="mb-4">
          <label class="block text-sm font-semibold mb-1 dark:text-white-light">Observaciones</label>
          <textarea v-model="formInformeGen.observaciones" class="form-textarea w-full" rows="3" />
        </div>
        <div class="mb-5">
          <label class="block text-sm font-semibold mb-1 dark:text-white-light">Resumen Técnico</label>
          <textarea v-model="formInformeGen.resumenTecnico" class="form-textarea w-full" rows="3" />
        </div>
        <div class="flex gap-3 justify-end">
          <button class="btn btn-outline-danger" @click="modalInformeGen = false">Cancelar</button>
          <button class="btn btn-primary" :disabled="guardandoInformeGen" @click="guardarInformeGen">
            {{ guardandoInformeGen ? 'Guardando...' : 'Guardar' }}
          </button>
        </div>
      </div>
    </div>
  </Transition>

  <!-- ── Modal: Evidencias / Archivos ── -->
  <Transition name="fade">
    <div
      v-if="modalEvidencias"
      class="fixed inset-0 z-[999] flex items-center justify-center bg-black/60"
      @click.self="modalEvidencias = false"
    >
      <div class="bg-white dark:bg-[#1b2e4b] rounded-xl p-6 shadow-xl w-full max-w-lg max-h-[90vh] flex flex-col">
        <!-- Cabecera -->
        <div class="flex items-start justify-between mb-1">
          <div>
            <h5 class="text-lg font-semibold dark:text-white">Evidencias y Archivos</h5>
            <p class="text-xs text-white-dark mt-0.5">Máximo 5 archivos · JPG, PNG, PDF</p>
          </div>
          <button class="text-white-dark hover:text-dark text-xl leading-none" @click="modalEvidencias = false">✕</button>
        </div>

        <!-- Upload -->
        <div v-if="evidencias.length < 5" class="border border-dashed border-white-light dark:border-[#515365] rounded-lg p-4 mb-4 mt-3">
          <p class="text-sm font-semibold dark:text-white-light mb-2">Subir archivo</p>
          <input
            type="file"
            accept=".jpg,.jpeg,.png,.pdf"
            class="form-input mb-2 text-sm"
            @change="archivoEvidencia = $event.target.files[0]"
          />
          <input
            v-model="descripcionEvidencia"
            type="text"
            class="form-input mb-3 text-sm"
            placeholder="Descripción (opcional)"
          />
          <button
            class="btn btn-sm btn-primary w-full"
            :disabled="!archivoEvidencia || subiendoArchivo"
            @click="subirArchivo"
          >
            <span v-if="!subiendoArchivo">Subir</span>
            <span v-else class="flex items-center justify-center gap-2">
              <span class="animate-spin border-2 border-white border-l-transparent rounded-full w-4 h-4" />
              Subiendo...
            </span>
          </button>
        </div>
        <p v-else class="text-warning text-xs mb-4">Límite de 5 archivos alcanzado.</p>

        <!-- Lista -->
        <div class="overflow-y-auto flex-1">
          <div v-if="cargandoEvidencias" class="flex justify-center py-8">
            <span class="animate-spin border-4 border-primary border-l-transparent rounded-full w-8 h-8" />
          </div>

          <div v-else-if="evidencias.length === 0" class="text-center text-white-dark py-8 text-sm">
            No hay archivos adjuntos aún.
          </div>

          <div v-else class="space-y-2">
            <div
              v-for="ev in evidencias"
              :key="ev.idEvidenciaFoto"
              class="flex items-center gap-3 p-3 rounded-lg border border-white-light dark:border-[#515365]"
              :class="eliminandoId === ev.idEvidenciaFoto ? 'opacity-40' : ''"
            >
              <!-- Ícono / miniatura -->
              <div class="shrink-0 w-12 h-12 rounded overflow-hidden bg-gray-100 dark:bg-[#253550] flex items-center justify-center">
                <span v-if="esPdf(ev.nombreOriginal)" class="text-2xl">📄</span>
                <img
                  v-else
                  :src="urlArchivo(ev.idEvidenciaFoto)"
                  :alt="ev.descripcion || ev.nombreOriginal"
                  class="w-full h-full object-cover"
                />
              </div>

              <!-- Info -->
              <div class="flex-1 min-w-0">
                <p class="text-sm font-semibold dark:text-white truncate">{{ ev.nombreOriginal }}</p>
                <p v-if="ev.descripcion" class="text-xs text-white-dark truncate">{{ ev.descripcion }}</p>
              </div>

              <!-- Acciones -->
              <div class="flex items-center gap-2 shrink-0">
                <a
                  :href="urlArchivo(ev.idEvidenciaFoto)"
                  target="_blank"
                  class="btn btn-xs btn-outline-primary"
                >
                  {{ esPdf(ev.nombreOriginal) ? 'Ver' : 'Ver' }}
                </a>
                <button
                  class="btn btn-xs btn-outline-danger"
                  :disabled="eliminandoId === ev.idEvidenciaFoto"
                  @click="eliminarArchivo(ev.idEvidenciaFoto)"
                >
                  ✕
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
