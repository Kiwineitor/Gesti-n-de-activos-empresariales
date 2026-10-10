<script setup>
import { ref, onMounted, computed } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const { get, post } = useApi()
const { toast } = useToast()

// Listas para selects (cargados desde endpoints públicos)
const unidades = ref([])
const cargos = ref([])
const tiposActivo = ref([])

onMounted(async () => {
  const [ru, rc, rt] = await Promise.all([
    get('/api/public/unidades'),
    get('/api/public/cargos'),
    get('/api/public/tipos-activo'),
  ])
  if (ru && ru.ok) unidades.value = await ru.json()
  if (rc && rc.ok) cargos.value = await rc.json()
  if (rt && rt.ok) tiposActivo.value = await rt.json()
})

// Datos del solicitante (siempre manual, sin búsqueda)
const funcionarioManual = ref({
  ci: '',
  nombre: '', paterno: '', materno: '',
  celular: '', correo: '',
  codigoAdministrativo: null
})

// Unidad — searchable select con creación inline
const unidadSeleccion = ref({ sel: '', busqueda: '', open: false, creando: false })
const nombreUnidadFinal = computed(() => unidadSeleccion.value.sel)
function unidadesFiltradas() {
  const q = (unidadSeleccion.value.busqueda || '').toLowerCase().trim()
  if (!q) return unidades.value
  return unidades.value.filter(u => u.nombreUni.toLowerCase().includes(q))
}
function seleccionarUnidad(u) {
  unidadSeleccion.value.sel = u.nombreUni
  unidadSeleccion.value.busqueda = u.nombreUni
  unidadSeleccion.value.open = false
}
function abrirUnidad() { unidadSeleccion.value.open = true }
function cerrarUnidad() { setTimeout(() => { unidadSeleccion.value.open = false }, 160) }
async function crearUnidad() {
  const nombre = unidadSeleccion.value.busqueda.trim().toUpperCase()
  if (!nombre) return
  unidadSeleccion.value.creando = true
  const res = await post('/api/public/unidades', { nombre })
  unidadSeleccion.value.creando = false
  if (!res) return
  if (res.ok) {
    const data = await res.json()
    unidades.value.push(data)
    unidadSeleccion.value.sel = data.nombreUni
    unidadSeleccion.value.open = false
    toast('Unidad creada', 'success')
  } else {
    const err = await res.json()
    toast(err.mensaje || 'Error al crear unidad', 'error')
  }
}

// Cargo — searchable select con creación inline
const cargoSeleccion = ref({ sel: '', busqueda: '', open: false, creando: false })
const cargoFinal = computed(() => cargoSeleccion.value.sel)
function cargosFiltrados() {
  const q = (cargoSeleccion.value.busqueda || '').toLowerCase().trim()
  return !q ? cargos.value : cargos.value.filter(c => (c.nombrecar || '').toLowerCase().includes(q))
}
function seleccionarCargo(c) {
  cargoSeleccion.value.sel = c.nombrecar
  cargoSeleccion.value.busqueda = c.nombrecar
  cargoSeleccion.value.open = false
}
function abrirCargo() { cargoSeleccion.value.open = true }
function cerrarCargo() { setTimeout(() => { cargoSeleccion.value.open = false }, 160) }
async function crearCargo() {
  const nombre = cargoSeleccion.value.busqueda.trim().toUpperCase()
  if (!nombre) return
  cargoSeleccion.value.creando = true
  const res = await post('/api/public/cargos', { nombre })
  cargoSeleccion.value.creando = false
  if (!res) return
  if (res.ok) {
    const data = await res.json()
    cargos.value.push(data)
    cargoSeleccion.value.sel = data.nombrecar
    cargoSeleccion.value.open = false
    toast('Cargo creado', 'success')
  } else {
    const err = await res.json()
    toast(err.mensaje || 'Error al crear cargo', 'error')
  }
}

// Descripción
const descripcion = ref('')

// Fecha de recepción (opcional, default hoy)
const fechaSol = ref(new Date().toISOString().slice(0, 10))

// Helper: convertir input a mayúsculas en tiempo real
function aMayusculas(e) {
  e.target.value = e.target.value.toUpperCase()
}

// Activos
function nuevoEquipo() {
  return {
    codigoActivo: '',
    descripcionActivo: '',
    tipoActivoSel: '',
    tipoActivoNuevo: '',
    problemaEspecifico: '',
    buscando: false,
    responsableInfo: null,
    tipoActivoOpen: false,
    tipoActivoBusqueda: '',
  }
}

function tipoActivoFinal(eq) {
  return eq.tipoActivoSel === '__nuevo__' ? eq.tipoActivoNuevo : eq.tipoActivoSel
}
const equipos = ref([nuevoEquipo()])

// Post-submit
const solicitudCreada = ref(null)
const enviando = ref(false)

async function buscarActivo(eq) {
  if (!eq.codigoActivo.trim()) return
  eq.buscando = true
  eq.responsableInfo = null

  // 1. Buscar en SCIAF para obtener responsable y descripción
  const sciafRes = await get(`/api/public/sciaf/ficha/${encodeURIComponent(eq.codigoActivo.trim())}`)
  if (sciafRes && sciafRes.ok) {
    const sciaf = await sciafRes.json()
    if (!eq.descripcionActivo) eq.descripcionActivo = sciaf.descripcion || ''
    eq.responsableInfo = {
      responsable: sciaf.responsable || '—',
      cargo:       sciaf.oficina   || '—',
      unidad:      sciaf.unidad    || '—',
      fuente: 'sciaf'
    }
    // 2. Intentar pre-llenar tipoActivo desde BD local
    const localRes = await get(`/api/public/activos/buscar?codigo=${eq.codigoActivo.trim()}`)
    if (localRes && localRes.ok) {
      const local = await localRes.json()
      const match = tiposActivo.value.find(t => t.nombreTipoAct === local.tipoActivo)
      eq.tipoActivoSel = match ? local.tipoActivo : (local.tipoActivo ? '__nuevo__' : '')
      eq.tipoActivoBusqueda = match ? local.tipoActivo : ''
      if (!match && local.tipoActivo) eq.tipoActivoNuevo = local.tipoActivo
    }
    eq.buscando = false
    toast('Activo encontrado', 'success')
    return
  }

  // No encontrado — sin datos del solicitante (el usuario ingresa manualmente)
  eq.responsableInfo = null
  eq.buscando = false
  toast('Activo no encontrado — ingresa los datos manualmente', 'warning')
}

function agregarEquipo() { equipos.value.push(nuevoEquipo()) }
function quitarEquipo(i) { if (equipos.value.length > 1) equipos.value.splice(i, 1) }
async function descargarPdf(id) {
  const res = await get(`/api/solicitud/${id}/pdf`)
  if (!res || !res.ok) { toast('Error al generar el PDF', 'error'); return }
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  window.open(url, '_blank')
  setTimeout(() => URL.revokeObjectURL(url), 15000)
}
async function imprimirPdf(id) {
  const res = await get(`/api/solicitud/${id}/pdf`)
  if (!res || !res.ok) { toast('Error al generar el PDF', 'error'); return }
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const iframe = document.createElement('iframe')
  iframe.style.cssText = 'position:fixed;width:0;height:0;border:0;opacity:0;'
  iframe.src = url
  document.body.appendChild(iframe)
  iframe.addEventListener('load', () => {
    iframe.contentWindow.focus()
    iframe.contentWindow.print()
    setTimeout(() => { document.body.removeChild(iframe); URL.revokeObjectURL(url) }, 60000)
  })
}

function tipoActivosFiltrados(eq) {
  const q = (eq.tipoActivoBusqueda || '').toLowerCase().trim()
  if (!q) return tiposActivo.value
  return tiposActivo.value.filter(t => t.nombreTipoAct.toLowerCase().includes(q))
}
function seleccionarTipoActivo(eq, tipo) {
  eq.tipoActivoSel = tipo.nombreTipoAct
  eq.tipoActivoBusqueda = tipo.nombreTipoAct
  eq.tipoActivoNuevo = ''
  eq.tipoActivoOpen = false
}
function seleccionarTipoActivoNuevo(eq) {
  eq.tipoActivoSel = '__nuevo__'
  eq.tipoActivoBusqueda = ''
  eq.tipoActivoNuevo = ''
  eq.tipoActivoOpen = false
}
function abrirTipoActivo(eq) {
  eq.tipoActivoOpen = true
  if (eq.tipoActivoSel && eq.tipoActivoSel !== '__nuevo__') eq.tipoActivoBusqueda = eq.tipoActivoSel
}
function cerrarTipoActivo(eq) {
  setTimeout(() => { eq.tipoActivoOpen = false }, 160)
}

async function enviar() {
  const f = funcionarioManual.value
  if (!f.nombre.trim()) { toast('El nombre es obligatorio', 'error'); return }
  if (!f.paterno.trim()) { toast('El apellido paterno es obligatorio', 'error'); return }
  if (!f.materno.trim()) { toast('El apellido materno es obligatorio', 'error'); return }
  
  if (!f.codigoAdministrativo) { toast('El código administrativo es obligatorio', 'error'); return }
  if (!nombreUnidadFinal.value) { toast('La unidad es obligatoria', 'error'); return }
  if (!cargoFinal.value) { toast('El cargo es obligatorio', 'error'); return }
  if (!descripcion.value.trim() || descripcion.value.trim().length < 5) {
    toast('Descripción mínimo 5 caracteres', 'error'); return
  }
  if (equipos.value.some(e => !e.codigoActivo.trim() || !e.problemaEspecifico.trim())) {
    toast('Cada activo requiere código y problema específico', 'error'); return
  }

  const body = {
    funcionario: {
      ci: f.ci ? f.ci.trim() : '',
      nombre: f.nombre,
      paterno: f.paterno,
      materno: f.materno,
      celular: f.celular.trim() || null,
      correo: f.correo ? f.correo.trim() : null,
      codigoAdministrativo: f.codigoAdministrativo,
      nombreUnidad: nombreUnidadFinal.value,
      cargoSolicitante: cargoFinal.value
    },
    descripcion: descripcion.value.trim(),
    fechaSol: fechaSol.value || null,
    equipos: equipos.value.map(e => ({
      codigoActivo: e.codigoActivo.trim(),
      descripcionActivo: e.descripcionActivo.trim(),
      tipoActivo: tipoActivoFinal(e).trim(),
      problemaEspecifico: e.problemaEspecifico.trim()
    }))
  }

  enviando.value = true
  const res = await post('/api/solicitud/manual', body)
  enviando.value = false
  if (!res) return
  if (res.ok || res.status === 201) {
    solicitudCreada.value = await res.json()
    toast(`Solicitud #${solicitudCreada.value.nroSol} registrada`, 'success')
  } else {
    toast('Error al registrar la solicitud', 'error')
  }
}
</script>

<template>
  <!-- Pantalla de éxito -->
  <div v-if="solicitudCreada" class="panel">
    <div class="flex flex-col items-center justify-center py-12 text-center gap-6">
      <div class="flex h-16 w-16 items-center justify-center rounded-full bg-success/10">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-success">
          <path d="M20 6L9 17l-5-5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <div>
        <h2 class="text-2xl font-bold dark:text-white mb-1">Solicitud registrada</h2>
        <p class="text-white-dark text-lg">
          Número: <span class="font-bold text-primary">{{ solicitudCreada.nroSol }}</span>
        </p>
        <p v-if="solicitudCreada.posicionCola > 0" class="text-white-dark mt-1">
          Posición en cola: <span class="font-semibold text-black dark:text-white">#{{ solicitudCreada.posicionCola }}</span>
        </p>
      </div>
      <div class="flex flex-wrap gap-3 justify-center">
        <button class="btn btn-primary flex items-center gap-2" @click="descargarPdf(solicitudCreada.idSolicitud)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
          Descargar PDF
        </button>
        <button class="btn btn-outline-primary flex items-center gap-2" @click="imprimirPdf(solicitudCreada.idSolicitud)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6 9 6 2 18 2 18 9"/><path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/><rect x="6" y="14" width="12" height="8"/>
          </svg>
          Imprimir
        </button>
        <button v-if="auth.isAuthenticated" class="btn btn-outline-primary flex items-center gap-2" @click="router.push('/solicitud')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
          </svg>
          Ver solicitudes
        </button>
        <button v-else class="btn btn-outline-primary flex items-center gap-2" @click="router.push('/login')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/>
          </svg>
          Ir al Login
        </button>
      </div>
    </div>
  </div>

  <!-- Formulario -->
  <div v-else class="space-y-5">

    <!-- Título -->
    <div class="panel">
      <div class="flex items-center gap-3 mb-1">
        <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary/10">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-primary">
            <path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
          </svg>
        </div>
        <h5 class="text-xl font-bold dark:text-white-light/90">Registrar Solicitud de Mantenimiento</h5>
      </div>
      <p class="text-sm text-white-dark ltr:pl-13 rtl:pr-13">
        Completa los datos del solicitante, describe el problema y registra los activos afectados.
      </p>
    </div>

    <!-- ═══════════════════════════════════════════════ -->
    <!-- PASO 1: IDENTIFICACIÓN DEL SOLICITANTE          -->
    <!-- ═══════════════════════════════════════════════ -->
    <div class="panel">
      <div class="flex items-center gap-2 mb-1">
        <span class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-primary text-white text-xs font-bold">1</span>
        <h6 class="text-base font-semibold dark:text-white">Identificación del solicitante</h6>
      </div>
      <p class="text-xs text-white-dark mb-4 ltr:pl-9 rtl:pr-9">
        Ingresa los datos de la persona que reporta el problema. Los campos con <span class="text-danger">*</span> son obligatorios.
      </p>

      <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <div>
          <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">C.I. (Carnet de Identidad)</label>
          <input v-model="funcionarioManual.ci" type="text" class="form-input" placeholder="Ej. 1234567" @input="aMayusculas" />
        </div>
        <div>
          <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Nombre <span class="text-danger">*</span></label>
          <input v-model="funcionarioManual.nombre" type="text" class="form-input" placeholder="Nombre" @input="aMayusculas" />
        </div>
        <div>
          <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Apellido paterno <span class="text-danger">*</span></label>
          <input v-model="funcionarioManual.paterno" type="text" class="form-input" placeholder="Apellido paterno" @input="aMayusculas" />
        </div>
        <div>
          <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Apellido materno <span class="text-danger">*</span></label>
          <input v-model="funcionarioManual.materno" type="text" class="form-input" placeholder="Apellido materno" @input="aMayusculas" />
        </div>
        <div>
          <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Celular</label>
          <input v-model="funcionarioManual.celular" type="text" class="form-input" placeholder="Celular" @input="aMayusculas" />
        </div>
        <div>
          <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Correo electrónico</label>
          <input v-model="funcionarioManual.correo" type="email" class="form-input" placeholder="correo@ejemplo.com" />
        </div>
        <div>
          <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Código administrativo <span class="text-danger">*</span></label>
          <input v-model="funcionarioManual.codigoAdministrativo" type="number" class="form-input" placeholder="Código" />
        </div>
        <!-- Unidad -->
        <div class="sm:col-span-2 relative">
          <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Unidad <span class="text-danger">*</span></label>
          <input
            v-model="unidadSeleccion.busqueda"
            type="text"
            class="form-input"
            placeholder="Buscar o escribir unidad..."
            autocomplete="off"
            @focus="abrirUnidad"
            @input="unidadSeleccion.open = true, unidadSeleccion.sel = ''"
            @blur="cerrarUnidad"
          />
          <div
            v-if="unidadSeleccion.open"
            class="absolute z-50 w-full mt-1 bg-white dark:bg-[#1b2e4b] border border-[#e0e6ed] dark:border-[#1b2e4b] rounded-lg shadow-lg max-h-48 overflow-y-auto"
          >
            <div
              v-if="unidadesFiltradas().length === 0 && unidadSeleccion.busqueda"
              class="px-3 py-2 text-sm text-white-dark italic"
            >Sin coincidencias</div>
            <div
              v-for="u in unidadesFiltradas()"
              :key="u.idUnidad"
              class="px-3 py-2 text-sm cursor-pointer hover:bg-primary/10 dark:hover:bg-primary/20"
              :class="unidadSeleccion.sel === u.nombreUni ? 'bg-primary/10 font-semibold text-primary' : 'text-black dark:text-white'"
              @mousedown.prevent="seleccionarUnidad(u)"
            >
              {{ u.nombreUni }}
            </div>
            <div
              v-if="unidadSeleccion.busqueda && !unidadesFiltradas().some(u => u.nombreUni.toLowerCase() === unidadSeleccion.busqueda.toLowerCase())"
              class="px-3 py-2 text-sm cursor-pointer hover:bg-success/10 text-success border-t border-[#e0e6ed] dark:border-[#1b2e4b] flex items-center gap-2"
              @mousedown.prevent="crearUnidad"
            >
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              {{ unidadSeleccion.creando ? 'Creando...' : `Crear "${unidadSeleccion.busqueda.trim().toUpperCase()}"` }}
            </div>
          </div>
        </div>
        <!-- Cargo -->
        <div class="relative">
          <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Cargo <span class="text-danger">*</span></label>
          <input
            v-model="cargoSeleccion.busqueda"
            type="text"
            class="form-input"
            placeholder="Buscar o escribir cargo..."
            autocomplete="off"
            @focus="abrirCargo"
            @input="cargoSeleccion.open = true, cargoSeleccion.sel = ''"
            @blur="cerrarCargo"
          />
          <div
            v-if="cargoSeleccion.open"
            class="absolute z-50 w-full mt-1 bg-white dark:bg-[#1b2e4b] border border-[#e0e6ed] dark:border-[#1b2e4b] rounded-lg shadow-lg max-h-48 overflow-y-auto"
          >
            <div
              v-if="cargosFiltrados().length === 0 && cargoSeleccion.busqueda"
              class="px-3 py-2 text-sm text-white-dark italic"
            >Sin coincidencias</div>
            <div
              v-for="c in cargosFiltrados()"
              :key="c.idcar"
              class="px-3 py-2 text-sm cursor-pointer hover:bg-primary/10 dark:hover:bg-primary/20"
              :class="cargoSeleccion.sel === c.nombrecar ? 'bg-primary/10 font-semibold text-primary' : 'text-black dark:text-white'"
              @mousedown.prevent="seleccionarCargo(c)"
            >
              {{ c.nombrecar }}
            </div>
            <div
              v-if="cargoSeleccion.busqueda && !cargosFiltrados().some(c => (c.nombrecar || '').toLowerCase() === cargoSeleccion.busqueda.toLowerCase())"
              class="px-3 py-2 text-sm cursor-pointer hover:bg-success/10 text-success border-t border-[#e0e6ed] dark:border-[#1b2e4b] flex items-center gap-2"
              @mousedown.prevent="crearCargo"
            >
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              {{ cargoSeleccion.creando ? 'Creando...' : `Crear "${cargoSeleccion.busqueda.trim().toUpperCase()}"` }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ═══════════════════════════════════════════════ -->
    <!-- PASO 2: DESCRIPCIÓN DEL PROBLEMA               -->
    <!-- ═══════════════════════════════════════════════ -->
    <div class="panel">
      <div class="flex items-center gap-2 mb-1">
        <span class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-primary text-white text-xs font-bold">2</span>
        <h6 class="text-base font-semibold dark:text-white">Descripción del problema</h6>
      </div>
      <p class="text-xs text-white-dark mb-4 ltr:pl-9 rtl:pr-9">
        Describe de forma general el motivo de la solicitud y la fecha de hoy.
      </p>

      <div class="grid grid-cols-1 sm:grid-cols-4 gap-4">
        <div class="sm:col-span-3">
          <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Descripción general <span class="text-danger">*</span></label>
          <textarea
            v-model="descripcion"
            class="form-textarea w-full"
            rows="4"
            placeholder="Describe el motivo general de la solicitud (mínimo 5 caracteres)..."
          />
        </div>
        <div>
          <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Fecha </label>
          <input v-model="fechaSol" type="date" class="form-input" />
          <p class="text-[10px] text-white-dark mt-1">Si se deja vacía se usa la fecha de hoy</p>
        </div>
      </div>
    </div>

    <!-- ═══════════════════════════════════════════════ -->
    <!-- PASO 3: EQUIPOS / ACTIVOS                       -->
    <!-- ═══════════════════════════════════════════════ -->
    <div class="panel">
      <div class="flex items-center justify-between mb-1">
        <div class="flex items-center gap-2">
          <span class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-primary text-white text-xs font-bold">3</span>
          <h6 class="text-base font-semibold dark:text-white">Equipos afectados</h6>
          <span class="badge bg-primary/10 text-primary py-1 px-2 rounded-full text-xs">{{ equipos.length }}</span>
        </div>
        <button class="btn btn-sm btn-outline-primary flex items-center gap-1" @click="agregarEquipo">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Agregar activo
        </button>
      </div>
      <p class="text-xs text-white-dark mb-4 ltr:pl-9 rtl:pr-9">
        Registra cada activo con su código. Al buscarlo, el sistema intentará obtener la información del responsable automáticamente.
      </p>

      <!-- Ayuda -->
      <details class="mb-4 rounded-lg border border-info/30 bg-info/5">
        <summary class="flex items-center gap-2 px-4 py-3 cursor-pointer text-sm font-semibold text-info select-none">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
          ¿Cómo buscar mis activos?
        </summary>
        <div class="px-4 pb-4 text-xs text-white-dark space-y-2 leading-relaxed">
          <p><strong>1.</strong> Ingresa el código del activo en el campo <em>"Código del activo"</em> y presiona <strong>Buscar</strong> o la tecla <strong>Enter</strong>.</p>
          <div class="rounded-md border border-warning/30 bg-warning/5 p-3">
            <p class="font-semibold text-warning text-xs mb-1">⚠ Importante: no incluyas el prefijo "148-"</p>
            <p>El código del activo generalmente aparece como <strong>148-XX-XX-XX-XXXX</strong> en los registros de SCIAF. Para la búsqueda, ingresa <strong>solo el código sin el prefijo 148-</strong>. Ejemplo: si tu activo es <strong>148-01-12-03-0005</strong>, escribe únicamente <strong>01-12-03-0005</strong>.</p>
          </div>
          <p><strong>2.</strong> Si el activo se encuentra en el sistema SCIAF, se mostrará automáticamente el responsable, la unidad y la descripción del equipo.</p>
          <p><strong>3.</strong> Si el activo no se encuentra, podrás completar los datos manualmente.</p>
          <p><strong>4.</strong> En el campo <em>"Tipo de activo"</em> puedes escribir para filtrar la lista o seleccionar directamente.</p>
          <p><strong>5.</strong> Describe brevemente el problema que presenta cada equipo en el campo <em>"Describe el problema"</em>.</p>
          <p><strong>6.</strong> Si necesitas reportar más de un equipo, haz clic en <em>"Agregar activo"</em> para añadir otro.</p>
        </div>
      </details>

      <div class="space-y-4">
        <div
          v-for="(eq, i) in equipos"
          :key="i"
          class="rounded-lg border border-[#e0e6ed] dark:border-[#1b2e4b] p-4 space-y-3"
        >
          <!-- Header del activo -->
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold uppercase text-white-dark">Activo #{{ i + 1 }}</span>
            <button
              v-if="equipos.length > 1"
              class="btn btn-xs btn-outline-danger flex items-center gap-1"
              @click="quitarEquipo(i)"
            >
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
              </svg>
              Eliminar
            </button>
          </div>

          <!-- Código + Buscar -->
          <div class="flex gap-2">
            <div class="flex-1">
              <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Código del activo <span class="text-danger">*</span></label>
              <input
                v-model="eq.codigoActivo"
                type="text"
                class="form-input"
                placeholder="Solo el código, sin prefijo 148. Ej: 00123"
                @keyup.enter="buscarActivo(eq)"
              />
            </div>
            <div class="flex items-end">
              <button
                class="btn btn-outline-primary flex items-center gap-2"
                :disabled="eq.buscando"
                @click="buscarActivo(eq)"
              >
                <svg v-if="eq.buscando" class="animate-spin" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
                </svg>
                <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
                </svg>
                {{ eq.buscando ? 'Buscando...' : 'Buscar' }}
              </button>
            </div>
          </div>

          <!-- Panel responsable del activo -->
          <div
            v-if="eq.responsableInfo"
            :class="eq.responsableInfo.fuente === 'sciaf'
              ? 'rounded-lg border border-success/30 bg-success/5 p-3 text-sm space-y-1'
              : 'rounded-lg border border-warning/30 bg-warning/5 p-3 text-sm space-y-1'"
          >
            <p :class="eq.responsableInfo.fuente === 'sciaf' ? 'text-xs font-semibold text-success uppercase mb-1' : 'text-xs font-semibold text-warning uppercase mb-1'">
              {{ eq.responsableInfo.fuente === 'sciaf' ? 'Responsable del activo (SCIAF)' : 'Datos del solicitante (referencia)' }}
            </p>
            <p class="text-white-dark">
              <span class="font-semibold text-black dark:text-white">Responsable:</span> {{ eq.responsableInfo.responsable }}
            </p>
            <p class="text-white-dark">
              <span class="font-semibold text-black dark:text-white">Cargo / Oficina:</span> {{ eq.responsableInfo.cargo }}
            </p>
            <p class="text-white-dark">
              <span class="font-semibold text-black dark:text-white">Unidad:</span> {{ eq.responsableInfo.unidad }}
            </p>
          </div>

          <!-- Descripción y Tipo -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Descripción del activo</label>
              <input v-model="eq.descripcionActivo" type="text" class="form-input" placeholder="Descripción" />
            </div>
            <div class="relative">
              <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Tipo de activo <span class="text-[10px] normal-case font-normal text-white-dark">(opcional — lo asigna el técnico)</span></label>
              <input
                v-model="eq.tipoActivoBusqueda"
                type="text"
                class="form-input"
                :placeholder="eq.tipoActivoSel && eq.tipoActivoSel !== '__nuevo__' ? eq.tipoActivoSel : 'Buscar tipo de activo...'"
                autocomplete="off"
                @focus="abrirTipoActivo(eq)"
                @input="eq.tipoActivoOpen = true"
                @blur="cerrarTipoActivo(eq)"
              />
              <div
                v-if="eq.tipoActivoOpen"
                class="absolute z-50 w-full mt-1 bg-white dark:bg-[#1b2e4b] border border-[#e0e6ed] dark:border-[#1b2e4b] rounded-lg shadow-lg max-h-48 overflow-y-auto"
              >
                <div
                  v-if="tipoActivosFiltrados(eq).length === 0 && eq.tipoActivoBusqueda"
                  class="px-3 py-2 text-sm text-white-dark italic"
                >Sin coincidencias</div>
                <div
                  v-for="t in tipoActivosFiltrados(eq)"
                  :key="t.idTipoActivo"
                  class="px-3 py-2 text-sm cursor-pointer hover:bg-primary/10 dark:hover:bg-primary/20"
                  :class="eq.tipoActivoSel === t.nombreTipoAct ? 'bg-primary/10 font-semibold text-primary' : 'text-black dark:text-white'"
                  @mousedown.prevent="seleccionarTipoActivo(eq, t)"
                >
                  {{ t.nombreTipoAct }}
                </div>
                <div
                  class="px-3 py-2 text-sm cursor-pointer hover:bg-warning/10 text-warning border-t border-[#e0e6ed] dark:border-[#1b2e4b]"
                  @mousedown.prevent="seleccionarTipoActivoNuevo(eq)"
                >
                  ➕ Nuevo tipo...
                </div>
              </div>
              <input
                v-if="eq.tipoActivoSel === '__nuevo__'"
                v-model="eq.tipoActivoNuevo"
                type="text"
                class="form-input mt-2"
                placeholder="Escribe el nuevo tipo de activo"
              />
            </div>
          </div>

          <!-- Problema -->
          <div>
            <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Describe el problema que presenta este activo <span class="text-danger">*</span></label>
            <textarea
              v-model="eq.problemaEspecifico"
              class="form-textarea w-full"
              rows="2"
              placeholder="Describe el problema específico de este activo..."
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Acciones -->
    <div class="panel">
      <div class="flex flex-wrap items-center justify-end gap-3">
        <button class="btn btn-outline-danger" @click="auth.isAuthenticated ? router.push('/solicitud') : router.push('/')">
          Cancelar
        </button>
        <button
          class="btn btn-primary flex items-center gap-2"
          :disabled="enviando"
          @click="enviar"
        >
          <svg v-if="enviando" class="animate-spin" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
          </svg>
          {{ enviando ? 'Registrando...' : 'Registrar Solicitud' }}
        </button>
      </div>
    </div>

  </div>
</template>

<style scoped>
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
input[type="number"] {
  -moz-appearance: textfield;
}
</style>
