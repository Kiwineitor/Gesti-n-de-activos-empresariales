<script setup>
import { ref, computed, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import { useToast } from '@/composables/useToast'
import { useDownload } from '@/composables/useDownload'
import SearchSelect from '@/components/ui/SearchSelect.vue'
import SathLoader from '@/components/ui/SathLoader.vue'

const { get, post } = useApi()
const { toast } = useToast()
const { descargarArchivo } = useDownload()

// ── Estado global ─────────────────────────────────────────────────
const tabActivo = ref('fichas')
const descargando = ref(null)
const miAdministrativoId = ref(null)

// ── Fichas Técnicas ───────────────────────────────────────────────
const solicitudList = ref([])
const textoBusqueda = ref('')
const filtroEstado = ref('')
const cargandoFichas = ref(false)

// Modal observación para ficha técnica
const modalObservacion = ref(false)
const solicitudParaReporte = ref(null)
const observacionTexto = ref('')

const solicitudFiltrada = computed(() => {
  const q = textoBusqueda.value.toLowerCase().trim()
  return solicitudList.value.filter((s) => {
    const coincide =
      !q ||
      String(s.nroSol ?? '').includes(q) ||
      (s.descripcionSol ?? '').toLowerCase().includes(q) ||
      (s.nroSolExterna ?? '').toLowerCase().includes(q)
    const estado = !filtroEstado.value || s.estadoSol === filtroEstado.value
    return coincide && estado
  })
})

// ── Informes Específicos ──────────────────────────────────────────
const informesEspecificos = ref([])
const busquedaEspecifico = ref('')
const cargandoEspecificos = ref(false)

// Modal crear informe específico
const modalNuevoEsp = ref(false)
const guardandoEsp = ref(false)
const detallesParaSelect = ref([])
const cargandoDetalles = ref(false)
const formEsp = ref({ idDetalleSolicitud: '', observaciones: '', trabajoRealizado: '', detalle: '' })

const detallesConLabel = computed(() =>
  detallesParaSelect.value.map(d => ({
    ...d,
    labelDisplay: [
      d.codigoActivo      || null,
      d.descripcionActivo || null,
      d.nroSol ? `Sol. ${d.nroSol}` : null,
    ].filter(Boolean).join(' · ') || `Detalle #${d.idDetalleSolicitud}`,
  }))
)

const solicitudesConLabel = computed(() =>
  solicitudesParaGeneral.value.map(s => ({
    ...s,
    labelDisplay: s.nroSol
      ? `Nro. ${s.nroSol}${s.descripcionSol ? ' — ' + s.descripcionSol.substring(0, 60) : ''}`
      : `Solicitud #${s.idSolicitud}`,
  }))
)

const informesEspFiltrados = computed(() => {
  const q = busquedaEspecifico.value.toLowerCase().trim()
  if (!q) return informesEspecificos.value
  return informesEspecificos.value.filter(
    (i) =>
      String(i.nroInforme ?? '').includes(q) ||
      (i.observaciones ?? '').toLowerCase().includes(q) ||
      (i.trabajoRealizado ?? '').toLowerCase().includes(q),
  )
})

// ── Informes Generales ────────────────────────────────────────────
const informesGenerales = ref([])
const busquedaGeneral = ref('')
const cargandoGenerales = ref(false)

// Modal crear informe general
const modalNuevoGen = ref(false)
const guardandoGen = ref(false)
const solicitudesParaGeneral = ref([])
const cargandoSolicitudesGen = ref(false)
const formGen = ref({ idSolicitud: '', observaciones: '', resumenTecnico: '' })

const informesGenFiltrados = computed(() => {
  const q = busquedaGeneral.value.toLowerCase().trim()
  if (!q) return informesGenerales.value
  return informesGenerales.value.filter(
    (i) =>
      String(i.nroInforme ?? '').includes(q) ||
      String(i.nroSolicitud ?? '').includes(q) ||
      (i.observaciones ?? '').toLowerCase().includes(q),
  )
})

// ── Reporte General ServiceDesk ──────────────────────────────────────────
const rgFechaInicial = ref('')
const rgFechaFinal = ref('')
const descargandoRG = ref(false)
const descargandoExcel = ref(false)

// ── Fichas Servicio Técnico ───────────────────────────────────────
const solicitudServicioList = ref([])
const textoServicio = ref('')
const filtroEstadoServicio = ref('')
const cargandoServicio = ref(false)
const modalFichaServicio = ref(false)
const solicitudParaFichaServicio = ref(null)
const observacionFichaServicio = ref('')

const solicitudServicioFiltrada = computed(() => {
  const q = textoServicio.value.toLowerCase().trim()
  return solicitudServicioList.value.filter((s) => {
    const coincide = !q || String(s.nroSol ?? '').includes(q) || (s.descripcionSol ?? '').toLowerCase().includes(q)
    const estado = !filtroEstadoServicio.value || s.estadoSol === filtroEstadoServicio.value
    return coincide && estado
  })
})

// ── Reporte Estadístico de Servicios ─────────────────────────────
const reFechaInicial = ref('')
const reFechaFinal = ref('')
const descargandoRE = ref(false)
const descargandoREExcel = ref(false)

// ── Reporte Personalizado ─────────────────────────────────────────
const rpFechaInicial = ref('')
const rpFechaFinal = ref('')
const rpIdUnidad = ref('')
const rpIdTipoServicio = ref('')
const rpIdTipoActivo = ref('')
const descargandoRP = ref(false)
const descargandoRPExcel = ref(false)
const rpUnidades = ref([])
const rpTiposServicio = ref([])
const rpTiposActivo = ref([])

// ── Init ──────────────────────────────────────────────────────────
onMounted(async () => {
  await cargarPerfil()
  await cargarFichas()
})

async function cargarPerfil() {
  try {
    const res = await get('/api/perfil/me')
    if (!res || !res.ok) return
    const p = await res.json()
    miAdministrativoId.value = p.idAdministrativo ?? null
  } catch {}
}

async function cambiarTab(tab) {
  tabActivo.value = tab
  if (tab === 'fichas' && solicitudList.value.length === 0) await cargarFichas()
  if (tab === 'servicio' && solicitudServicioList.value.length === 0) await cargarFichasServicio()
  if (tab === 'especificos' && informesEspecificos.value.length === 0) await cargarInformesEspecificos()
  if (tab === 'generales' && informesGenerales.value.length === 0) await cargarInformesGenerales()
  if (tab === 'personalizado' && rpUnidades.value.length === 0) await cargarFiltrosPersonalizado()
}

// ═══════════════════════════════════════════════════════════════════
// FICHAS TÉCNICAS
// ═══════════════════════════════════════════════════════════════════

async function cargarFichas() {
  cargandoFichas.value = true
  try {
    const url = filtroEstado.value
      ? `/api/solicitud/listar?estado=${filtroEstado.value}`
      : '/api/solicitud/listar'
    const res = await get(url)
    if (!res || !res.ok) return
    solicitudList.value = await res.json()
  } catch {
  } finally {
    cargandoFichas.value = false
  }
}

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
  await cargarFichas()
}

async function descargarFicha(s, observacion = '') {
  const key = 'ficha-' + s.nroSol
  descargando.value = key
  try {
    let url = `/api/reportes/ficha-tecnica/${s.nroSol}`
    if (observacion) url += `?observacion=${encodeURIComponent(observacion)}`
    const res = await get(url)
    if (!res || !res.ok) {
      const err = await res?.json().catch(() => ({}))
      throw new Error(err?.message || 'No se pudo generar el PDF')
    }
    await _descargarBlob(res, `Ficha_Tecnica_${s.nroSol}.pdf`)
    toast('Ficha técnica descargada', 'success')
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    descargando.value = null
  }
}

async function descargarFichaWord(s) {
  const key = 'word-' + s.nroSol
  descargando.value = key
  try {
    const res = await get(`/api/reportes/ficha-tecnica-word/${s.nroSol}`)
    if (!res || !res.ok) throw new Error('No se pudo generar el Word')
    await _descargarBlob(res, `Ficha_Tecnica_${s.nroSol}.docx`)
    toast('Ficha Word descargada', 'success')
  } catch (e) { toast(e.message, 'error') } finally { descargando.value = null }
}

// ═══════════════════════════════════════════════════════════════════
// FICHAS SERVICIO TÉCNICO
// ═══════════════════════════════════════════════════════════════════

async function cargarFichasServicio() {
  cargandoServicio.value = true
  try {
    const res = await get('/api/solicitud/lista-servicio-tecnico')
    if (!res || !res.ok) return
    solicitudServicioList.value = await res.json()
  } catch {} finally { cargandoServicio.value = false }
}

function abrirModalFichaServicio(s) {
  solicitudParaFichaServicio.value = s
  observacionFichaServicio.value = ''
  modalFichaServicio.value = true
}

async function confirmarDescargaFichaServicio() {
  const s = solicitudParaFichaServicio.value
  if (!s?.nroSol) return
  modalFichaServicio.value = false
  await descargarFichaServicio(s, observacionFichaServicio.value.trim())
  solicitudParaFichaServicio.value = null
  await cargarFichas()
}

async function descargarFichaServicio(s, observacion = '') {
  const key = 'svc-' + s.nroSol
  descargando.value = key
  try {
    let url = `/api/reportes/ficha-tecnica-servicio/${s.nroSol}`
    if (observacion) url += `?observacion=${encodeURIComponent(observacion)}`
    const res = await get(url)
    if (!res || !res.ok) throw new Error('No se pudo generar el PDF de servicio')
    await _descargarBlob(res, `Ficha_Servicio_${s.nroSol}.pdf`)
    toast('Ficha de servicio técnico descargada', 'success')
  } catch (e) { toast(e.message, 'error') } finally { descargando.value = null }
}

async function descargarFichaServicioWord(s) {
  const key = 'svcw-' + s.nroSol
  descargando.value = key
  try {
    const res = await get(`/api/reportes/ficha-tecnica-servicio-word/${s.nroSol}`)
    if (!res || !res.ok) throw new Error('No se pudo generar el Word de servicio')
    await _descargarBlob(res, `Ficha_Servicio_${s.nroSol}.docx`)
    toast('Ficha Word descargada', 'success')
  } catch (e) { toast(e.message, 'error') } finally { descargando.value = null }
}

// ═══════════════════════════════════════════════════════════════════
// REPORTE PERSONALIZADO
// ═══════════════════════════════════════════════════════════════════

async function cargarFiltrosPersonalizado() {
  try {
    const [resU, resTS, resTA] = await Promise.all([
      get('/api/unidad/lista'),
      get('/api/tipo-servicio/lista'),
      get('/api/tipo-activo/lista'),
    ])
    if (resU?.ok)  rpUnidades.value      = await resU.json()
    if (resTS?.ok) rpTiposServicio.value = await resTS.json()
    if (resTA?.ok) rpTiposActivo.value   = await resTA.json()
  } catch (e) { toast('Error cargando filtros', 'error') }
}

async function descargarReportePersonalizado() {
  if (!rpFechaInicial.value || !rpFechaFinal.value) {
    toast('Selecciona el rango de fechas', 'warning'); return
  }
  descargandoRP.value = true
  try {
    const params = new URLSearchParams({ fechaInicial: rpFechaInicial.value, fechaFinal: rpFechaFinal.value })
    if (rpIdUnidad.value)       params.append('idUnidad', rpIdUnidad.value)
    if (rpIdTipoServicio.value) params.append('idTipoServicio', rpIdTipoServicio.value)
    if (rpIdTipoActivo.value)   params.append('idTipoActivo', rpIdTipoActivo.value)
    if (miAdministrativoId.value) params.append('idAdministrativo', miAdministrativoId.value)
    const res = await get(`/api/reportes/reporte-personalizado?${params}`)
    if (!res || !res.ok) throw new Error('No se pudo generar el reporte')
    await _descargarBlob(res, `Reporte_Personalizado_${rpFechaInicial.value}_${rpFechaFinal.value}.pdf`)
    toast('Reporte personalizado descargado', 'success')
  } catch (e) { toast(e.message, 'error') } finally { descargandoRP.value = false }
}

async function descargarReportePersonalizadoExcel() {
  if (!rpFechaInicial.value || !rpFechaFinal.value) {
    toast('Selecciona el rango de fechas', 'warning'); return
  }
  descargandoRPExcel.value = true
  try {
    const params = new URLSearchParams({ fechaInicial: rpFechaInicial.value, fechaFinal: rpFechaFinal.value })
    if (rpIdUnidad.value)       params.append('idUnidad', rpIdUnidad.value)
    if (rpIdTipoServicio.value) params.append('idTipoServicio', rpIdTipoServicio.value)
    if (rpIdTipoActivo.value)   params.append('idTipoActivo', rpIdTipoActivo.value)
    if (miAdministrativoId.value) params.append('idAdministrativo', miAdministrativoId.value)
    const res = await get(`/api/reportes/reporte-personalizado-excel?${params}`)
    if (!res || !res.ok) throw new Error('No se pudo generar el Excel')
    await _descargarBlob(res, `Reporte_Personalizado_${rpFechaInicial.value}_${rpFechaFinal.value}.xlsx`)
    toast('Excel personalizado descargado', 'success')
  } catch (e) { toast(e.message, 'error') } finally { descargandoRPExcel.value = false }
}

// ═══════════════════════════════════════════════════════════════════
// INFORMES ESPECÍFICOS
// ═══════════════════════════════════════════════════════════════════

async function cargarInformesEspecificos() {
  cargandoEspecificos.value = true
  try {
    const res = await get('/api/informes-equipo-especifico')
    if (!res || !res.ok) throw new Error('Error al cargar informes específicos')
    informesEspecificos.value = await res.json()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    cargandoEspecificos.value = false
  }
}

async function abrirModalNuevoEsp() {
  formEsp.value = { idDetalleSolicitud: '', observaciones: '', trabajoRealizado: '', detalle: '' }
  modalNuevoEsp.value = true
  cargandoDetalles.value = true
  try {
    const res = await get('/api/detalleSolicitud/listaParaElFrontend')
    if (!res || !res.ok) return
    const todos = await res.json()
    const conInforme = new Set(informesEspecificos.value.map((i) => i.idDetalleSolicitud))
    detallesParaSelect.value = todos.filter((d) => !conInforme.has(d.idDetalleSolicitud))
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    cargandoDetalles.value = false
  }
}

async function crearInformeEspecifico() {
  if (!formEsp.value.idDetalleSolicitud) {
    toast('Selecciona un recurso', 'error')
    return
  }
  guardandoEsp.value = true
  try {
    const body = {
      idDetalleSolicitud: Number(formEsp.value.idDetalleSolicitud),
      observaciones: formEsp.value.observaciones.trim() || null,
      trabajoRealizado: formEsp.value.trabajoRealizado.trim() || null,
      detalle: formEsp.value.detalle.trim() || null,
      nroInforme: 0,
    }
    const res = await post('/api/informes-equipo-especifico', body)
    if (!res || !res.ok) {
      const err = await res?.json().catch(() => ({}))
      throw new Error(err?.message || `Error ${res?.status}`)
    }
    toast('Informe específico creado', 'success')
    modalNuevoEsp.value = false
    await cargarInformesEspecificos()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    guardandoEsp.value = false
  }
}

async function descargarInformeEsp(id, nro) {
  const key = 'esp-' + id
  descargando.value = key
  try {
    const res = await get(`/api/reportes/informe-especifico/${id}`)
    if (!res || !res.ok) {
      const err = await res?.json().catch(() => ({}))
      throw new Error(err?.message || 'No se pudo generar el PDF')
    }
    await _descargarBlob(res, `Informe_Especifico_${nro}.pdf`)
    toast('Informe específico descargado', 'success')
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    descargando.value = null
  }
}

// ═══════════════════════════════════════════════════════════════════
// INFORMES GENERALES
// ═══════════════════════════════════════════════════════════════════

async function cargarInformesGenerales() {
  cargandoGenerales.value = true
  try {
    const res = await get('/api/informes-equipo-general')
    if (!res || !res.ok) throw new Error('Error al cargar informes generales')
    informesGenerales.value = await res.json()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    cargandoGenerales.value = false
  }
}

async function abrirModalNuevoGen() {
  formGen.value = { idSolicitud: '', observaciones: '', resumenTecnico: '' }
  modalNuevoGen.value = true
  cargandoSolicitudesGen.value = true
  try {
    const res = await get('/api/solicitud/listar')
    if (!res || !res.ok) return
    const todas = await res.json()
    const conInforme = new Set(informesGenerales.value.map((i) => i.idSolicitud))
    solicitudesParaGeneral.value = todas.filter((s) => !conInforme.has(s.idSolicitud))
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    cargandoSolicitudesGen.value = false
  }
}

async function crearInformeGeneral() {
  if (!formGen.value.idSolicitud) {
    toast('Selecciona una solicitud', 'error')
    return
  }
  guardandoGen.value = true
  try {
    const body = {
      idSolicitud: Number(formGen.value.idSolicitud),
      observaciones: formGen.value.observaciones.trim() || null,
      ResumenTecnico: formGen.value.resumenTecnico.trim() || null,
      nroInforme: 0,
      fechaCierreInforme: new Date().toISOString().split('T')[0],
    }
    const res = await post('/api/informes-equipo-general', body)
    if (!res || !res.ok) {
      const err = await res?.json().catch(() => ({}))
      throw new Error(err?.message || `Error ${res?.status}`)
    }
    toast('Informe general creado', 'success')
    modalNuevoGen.value = false
    await cargarInformesGenerales()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    guardandoGen.value = false
  }
}

async function descargarInformeGen(id, nro) {
  const key = 'gen-' + id
  descargando.value = key
  try {
    const res = await get(`/api/reportes/informe-general/${id}`)
    if (!res || !res.ok) {
      const err = await res?.json().catch(() => ({}))
      throw new Error(err?.message || 'No se pudo generar el PDF')
    }
    await _descargarBlob(res, `Informe_General_${nro}.pdf`)
    toast('Informe general descargado', 'success')
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    descargando.value = null
  }
}

// ═══════════════════════════════════════════════════════════════════
// REPORTE GENERAL
// ═══════════════════════════════════════════════════════════════════

async function descargarReporteGeneral() {
  if (!rgFechaInicial.value || !rgFechaFinal.value) {
    toast('Selecciona el rango de fechas', 'warning')
    return
  }
  if (rgFechaInicial.value > rgFechaFinal.value) {
    toast('La fecha inicial no puede ser posterior a la final', 'warning')
    return
  }
  const idAdm = miAdministrativoId.value ?? 0
  descargandoRG.value = true
  try {
    const url = `/api/reportes/reporte-general?fechaInicial=${rgFechaInicial.value}&fechaFinal=${rgFechaFinal.value}&idAdministrativo=${idAdm}`
    const res = await get(url)
    if (!res || !res.ok) {
      const err = await res?.json().catch(() => ({}))
      throw new Error(err?.message || 'No se pudo generar el reporte')
    }
    await _descargarBlob(res, `Reporte_ServiceDesk_${rgFechaInicial.value}_${rgFechaFinal.value}.pdf`)
    toast('Reporte general descargado', 'success')
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    descargandoRG.value = false
  }
}

async function descargarReporteEstadistico() {
  if (!reFechaInicial.value || !reFechaFinal.value) {
    toast('Selecciona el rango de fechas', 'warning')
    return
  }
  if (reFechaInicial.value > reFechaFinal.value) {
    toast('La fecha inicial no puede ser posterior a la final', 'warning')
    return
  }
  const idAdm = miAdministrativoId.value ?? 0
  descargandoRE.value = true
  try {
    const url = `/api/reportes/reporte-estadistico-servicios?fechaInicial=${reFechaInicial.value}&fechaFinal=${reFechaFinal.value}&idAdministrativo=${idAdm}`
    const res = await get(url)
    if (!res || !res.ok) {
      const err = await res?.json().catch(() => ({}))
      throw new Error(err?.message || 'No se pudo generar el reporte')
    }
    await _descargarBlob(res, `Reporte_Estadistico_${reFechaInicial.value}_${reFechaFinal.value}.pdf`)
    toast('Reporte estadístico descargado', 'success')
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    descargandoRE.value = false
  }
}

async function descargarReporteEstadisticoExcel() {
  if (!reFechaInicial.value || !reFechaFinal.value) {
    toast('Selecciona el rango de fechas', 'warning')
    return
  }
  if (reFechaInicial.value > reFechaFinal.value) {
    toast('La fecha inicial no puede ser posterior a la final', 'warning')
    return
  }
  const idAdm = miAdministrativoId.value ?? 0
  descargandoREExcel.value = true
  try {
    const url = `/api/reportes/reporte-estadistico-servicios-excel?fechaInicial=${reFechaInicial.value}&fechaFinal=${reFechaFinal.value}&idAdministrativo=${idAdm}`
    const res = await get(url)
    if (!res || !res.ok) {
      const err = await res?.json().catch(() => ({}))
      throw new Error(err?.message || 'No se pudo generar el Excel')
    }
    await _descargarBlob(res, `Reporte_Estadistico_${reFechaInicial.value}_${reFechaFinal.value}.xlsx`)
    toast('Reporte estadístico Excel descargado', 'success')
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    descargandoREExcel.value = false
  }
}

async function descargarReporteExcel() {
  if (!rgFechaInicial.value || !rgFechaFinal.value) {
    toast('Selecciona el rango de fechas', 'warning')
    return
  }
  if (rgFechaInicial.value > rgFechaFinal.value) {
    toast('La fecha inicial no puede ser posterior a la final', 'warning')
    return
  }
  const idAdm = miAdministrativoId.value ?? 0
  descargandoExcel.value = true
  try {
    const url = `/api/reportes/reporte-general-excel?fechaInicial=${rgFechaInicial.value}&fechaFinal=${rgFechaFinal.value}&idAdministrativo=${idAdm}`
    const res = await get(url)
    if (!res || !res.ok) {
      const err = await res?.json().catch(() => ({}))
      throw new Error(err?.message || 'No se pudo generar el Excel')
    }
    await _descargarBlob(res, `Reporte_ServiceDesk_${rgFechaInicial.value}_${rgFechaFinal.value}.xlsx`)
    toast('Reporte Excel descargado', 'success')
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    descargandoExcel.value = false
  }
}

// ═══════════════════════════════════════════════════════════════════
// HELPERS
// ═══════════════════════════════════════════════════════════════════

async function _descargarBlob(res, filename) {
  const blob = await res.blob()
  const resultado = await descargarArchivo(blob, filename)
  if (resultado?.mensaje) toast(resultado.mensaje, 'success')
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
  <ul class="flex space-x-2 rtl:space-x-reverse mb-6">
    <li><RouterLink to="/" class="text-primary hover:underline">Inicio</RouterLink></li>
    <li class="before:content-['/'] ltr:before:mr-2 rtl:before:ml-2 text-gray-400">Centro de Reportes</li>
  </ul>

  <!-- ── Tabs ── -->
  <div class="mb-5 overflow-x-auto border-b border-[#e0e6ed] dark:border-[#253b5e]">
    <ul class="flex -mb-px min-w-max gap-0.5">
      <li v-for="tab in [
        { id: 'fichas',        label: 'Fichas Técnicas' },
        { id: 'servicio',      label: 'Fichas Servicio' },
        { id: 'especificos',   label: 'Inf. Específicos' },
        { id: 'generales',     label: 'Inf. Generales' },
        { id: 'general-sath',  label: 'Reporte General' },
        { id: 'estadistico',   label: 'Estadístico' },
        { id: 'personalizado', label: 'Personalizado' },
      ]" :key="tab.id">
        <button
          class="whitespace-nowrap px-4 py-2.5 border-b-2 rounded-t-lg font-semibold text-sm transition-colors"
          :class="tabActivo === tab.id
            ? 'border-primary text-primary bg-primary/5'
            : 'border-transparent text-white-dark hover:text-primary hover:border-primary/30'"
          @click="cambiarTab(tab.id)"
        >
          {{ tab.label }}
        </button>
      </li>
    </ul>
  </div>

  <!-- ══════════════════════════════════════════════════════════════ -->
  <!-- Tab: Fichas Técnicas                                          -->
  <!-- ══════════════════════════════════════════════════════════════ -->
  <div v-if="tabActivo === 'fichas'" class="panel">
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
      <h5 class="text-xl font-bold dark:text-white-light/90">
        Fichas Técnicas
        <span class="ml-2 badge bg-primary/10 text-primary py-1 px-3 rounded-full text-sm">
          {{ solicitudFiltrada.length }} registros
        </span>
      </h5>
      <div class="flex w-full flex-col gap-3 sm:w-auto sm:flex-row sm:items-center">
        <select v-model="filtroEstado" class="form-select py-2 w-full sm:w-44" @change="cargarFichas">
          <option value="">Todos los estados</option>
          <option value="PENDIENTE">Pendiente</option>
          <option value="EN_MANTENIMIENTO">En Mantenimiento</option>
          <option value="FINALIZADO">Finalizado</option>
        </select>
        <div class="relative">
          <input v-model="textoBusqueda" type="text" placeholder="Buscar..."
            class="peer form-input py-2 ltr:pr-11 rtl:pl-11 w-full sm:w-52" />
          <div class="absolute top-1/2 -translate-y-1/2 peer-focus:text-primary ltr:right-[11px] rtl:left-[11px]">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5" /><path d="M18.5 18.5L22 22" stroke-linecap="round" />
            </svg>
          </div>
        </div>
      </div>
    </div>

    <SathLoader v-if="cargandoFichas" size="md" label="Cargando fichas..." class="py-10" />
    <template v-else>
      <!-- Cards mobile -->
      <div class="sm:hidden space-y-3">
        <p v-if="solicitudFiltrada.length === 0" class="text-center text-white-dark py-6">No hay solicitudes que coincidan</p>
        <div v-for="s in solicitudFiltrada" :key="s.idSolicitud"
          class="rounded-xl border border-white-light dark:border-[#1b2e4b] bg-white dark:bg-[#0e1726] p-4">
          <div class="flex items-start justify-between mb-1.5">
            <span class="font-bold text-primary text-sm">{{ s.nroSol ?? ('SOL-' + s.idSolicitud) }}</span>
            <span :class="badgeClase(s.estadoSol)" class="shrink-0 ml-2">{{ s.estadoSol }}</span>
          </div>
          <p class="text-sm text-white-dark line-clamp-2 mb-1">{{ s.descripcionSol || '—' }}</p>
          <p class="text-xs text-white-dark/60 mb-3">{{ s.fechaSol || '—' }}</p>
          <div class="flex gap-2">
            <button type="button" class="btn btn-sm btn-outline-primary flex-1" :disabled="descargando === 'ficha-' + s.nroSol" @click="abrirModalFicha(s)">
              <span v-if="descargando === 'ficha-' + s.nroSol" class="animate-spin border-2 border-primary border-l-transparent rounded-full w-3 h-3 inline-block mr-1" />
              <span v-else>PDF</span>
            </button>
            <button type="button" class="btn btn-sm btn-outline-secondary flex-1" :disabled="descargando === 'word-' + s.nroSol" @click="descargarFichaWord(s)">
              <span v-if="descargando === 'word-' + s.nroSol" class="animate-spin border-2 border-secondary border-l-transparent rounded-full w-3 h-3 inline-block mr-1" />
              <span v-else>Word</span>
            </button>
          </div>
        </div>
      </div>
      <!-- Tabla desktop -->
      <div class="hidden sm:block table-responsive">
        <table class="table-striped table-hover">
          <thead>
            <tr>
              <th>Nro. Solicitud</th>
              <th>Descripción</th>
              <th>Fecha</th>
              <th class="text-center">Estado</th>
              <th class="text-center">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="solicitudFiltrada.length === 0">
              <td colspan="5" class="text-center text-white-dark py-6">No hay solicitudes que coincidan</td>
            </tr>
            <tr v-for="s in solicitudFiltrada" :key="s.idSolicitud">
              <td class="font-semibold whitespace-nowrap">{{ s.nroSol ?? ('SOL-' + s.idSolicitud) }}</td>
              <td class="max-w-xs"><span class="block truncate text-white-dark" :title="s.descripcionSol">{{ s.descripcionSol || '—' }}</span></td>
              <td class="text-white-dark whitespace-nowrap">{{ s.fechaSol || '—' }}</td>
              <td class="text-center"><span :class="badgeClase(s.estadoSol)">{{ s.estadoSol }}</span></td>
              <td class="text-center">
                <div class="flex items-center justify-center gap-1">
                  <button type="button" class="btn btn-xs btn-outline-primary px-2" :disabled="descargando === 'ficha-' + s.nroSol" @click="abrirModalFicha(s)">
                    <span v-if="descargando === 'ficha-' + s.nroSol" class="animate-spin border-2 border-primary border-l-transparent rounded-full w-3 h-3 inline-block" />
                    <span v-else>PDF</span>
                  </button>
                  <button type="button" class="btn btn-xs btn-outline-secondary px-2" :disabled="descargando === 'word-' + s.nroSol" @click="descargarFichaWord(s)">
                    <span v-if="descargando === 'word-' + s.nroSol" class="animate-spin border-2 border-secondary border-l-transparent rounded-full w-3 h-3 inline-block" />
                    <span v-else>Word</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>

  <!-- ══════════════════════════════════════════════════════════════ -->
  <!-- Tab: Fichas Servicio Técnico                                  -->
  <!-- ══════════════════════════════════════════════════════════════ -->
  <div v-if="tabActivo === 'servicio'" class="panel">
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
      <h5 class="text-xl font-bold dark:text-white-light/90">
        Fichas Servicio Técnico
        <span class="ml-2 badge bg-primary/10 text-primary py-1 px-3 rounded-full text-sm">
          {{ solicitudServicioFiltrada.length }} registros
        </span>
      </h5>
      <div class="flex w-full flex-col gap-3 sm:w-auto sm:flex-row sm:items-center">
        <select v-model="filtroEstadoServicio" class="form-select py-2 w-full sm:w-44">
          <option value="">Todos los estados</option>
          <option value="PENDIENTE">Pendiente</option>
          <option value="EN_MANTENIMIENTO">En Mantenimiento</option>
          <option value="FINALIZADO">Finalizado</option>
        </select>
        <div class="relative">
          <input v-model="textoServicio" type="text" placeholder="Buscar..."
            class="peer form-input py-2 ltr:pr-11 rtl:pl-11 w-full sm:w-52" />
          <div class="absolute top-1/2 -translate-y-1/2 peer-focus:text-primary ltr:right-[11px] rtl:left-[11px]">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5" /><path d="M18.5 18.5L22 22" stroke-linecap="round" />
            </svg>
          </div>
        </div>
      </div>
    </div>
    <SathLoader v-if="cargandoServicio" size="md" label="Cargando servicios..." class="py-10" />
    <template v-else>
      <!-- Cards mobile -->
      <div class="sm:hidden space-y-3">
        <p v-if="solicitudServicioFiltrada.length === 0" class="text-center text-white-dark py-6">Sin solicitudes de servicio técnico</p>
        <div v-for="s in solicitudServicioFiltrada" :key="s.idSolicitud"
          class="rounded-xl border border-white-light dark:border-[#1b2e4b] bg-white dark:bg-[#0e1726] p-4">
          <div class="flex items-start justify-between mb-1.5">
            <span class="font-bold text-primary text-sm">{{ s.nroSol ?? ('SOL-' + s.idSolicitud) }}</span>
            <span :class="badgeClase(s.estadoSol)" class="shrink-0 ml-2">{{ s.estadoSol }}</span>
          </div>
          <p class="text-sm text-white-dark line-clamp-2 mb-1">{{ s.descripcionSol || '—' }}</p>
          <p class="text-xs text-white-dark/60 mb-3">{{ s.fechaSol || '—' }}</p>
          <div class="flex gap-2">
            <button type="button" class="btn btn-sm btn-outline-primary flex-1" :disabled="descargando === 'svc-' + s.nroSol" @click="abrirModalFichaServicio(s)">
              <span v-if="descargando === 'svc-' + s.nroSol" class="animate-spin border-2 border-primary border-l-transparent rounded-full w-3 h-3 inline-block mr-1" />
              <span v-else>PDF</span>
            </button>
            <button type="button" class="btn btn-sm btn-outline-secondary flex-1" :disabled="descargando === 'svcw-' + s.nroSol" @click="descargarFichaServicioWord(s)">
              <span v-if="descargando === 'svcw-' + s.nroSol" class="animate-spin border-2 border-secondary border-l-transparent rounded-full w-3 h-3 inline-block mr-1" />
              <span v-else>Word</span>
            </button>
          </div>
        </div>
      </div>
      <!-- Tabla desktop -->
      <div class="hidden sm:block table-responsive">
        <table class="table-striped table-hover">
          <thead>
            <tr>
              <th>Nro. Solicitud</th><th>Descripción</th><th>Fecha</th>
              <th class="text-center">Estado</th><th class="text-center">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="solicitudServicioFiltrada.length === 0">
              <td colspan="5" class="text-center text-white-dark py-6">Sin solicitudes de servicio técnico</td>
            </tr>
            <tr v-for="s in solicitudServicioFiltrada" :key="s.idSolicitud">
              <td class="font-semibold whitespace-nowrap">{{ s.nroSol ?? ('SOL-' + s.idSolicitud) }}</td>
              <td class="max-w-xs"><span class="block truncate text-white-dark" :title="s.descripcionSol">{{ s.descripcionSol || '—' }}</span></td>
              <td class="text-white-dark whitespace-nowrap">{{ s.fechaSol || '—' }}</td>
              <td class="text-center"><span :class="badgeClase(s.estadoSol)">{{ s.estadoSol }}</span></td>
              <td class="text-center">
                <div class="flex items-center justify-center gap-1">
                  <button type="button" class="btn btn-xs btn-outline-primary px-2" :disabled="descargando === 'svc-' + s.nroSol" @click="abrirModalFichaServicio(s)">
                    <span v-if="descargando === 'svc-' + s.nroSol" class="animate-spin border-2 border-primary border-l-transparent rounded-full w-3 h-3 inline-block" />
                    <span v-else>PDF</span>
                  </button>
                  <button type="button" class="btn btn-xs btn-outline-secondary px-2" :disabled="descargando === 'svcw-' + s.nroSol" @click="descargarFichaServicioWord(s)">
                    <span v-if="descargando === 'svcw-' + s.nroSol" class="animate-spin border-2 border-secondary border-l-transparent rounded-full w-3 h-3 inline-block" />
                    <span v-else>Word</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>

  <!-- ══════════════════════════════════════════════════════════════ -->
  <!-- Tab: Informes Específicos                                     -->
  <!-- ══════════════════════════════════════════════════════════════ -->
  <div v-if="tabActivo === 'especificos'" class="panel">
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
      <h5 class="text-xl font-bold dark:text-white-light/90">
        Informes Específicos
        <span class="ml-2 badge bg-primary/10 text-primary py-1 px-3 rounded-full text-sm">
          {{ informesEspFiltrados.length }} registros
        </span>
      </h5>
      <div class="flex w-full flex-col gap-3 sm:w-auto sm:flex-row sm:items-center">
        <button type="button" class="btn btn-primary" @click="abrirModalNuevoEsp">
          <svg class="inline w-4 h-4 mr-1.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Nuevo Informe
        </button>
        <div class="relative">
          <input v-model="busquedaEspecifico" type="text" placeholder="Buscar..."
            class="peer form-input py-2 ltr:pr-11 rtl:pl-11 w-full sm:w-52" />
          <div class="absolute top-1/2 -translate-y-1/2 peer-focus:text-primary ltr:right-[11px] rtl:left-[11px]">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5" /><path d="M18.5 18.5L22 22" stroke-linecap="round" />
            </svg>
          </div>
        </div>
      </div>
    </div>

    <SathLoader v-if="cargandoEspecificos" size="md" label="Cargando informes..." class="py-10" />
    <template v-else>
      <!-- Cards mobile -->
      <div class="sm:hidden space-y-3">
        <p v-if="informesEspFiltrados.length === 0" class="text-center text-white-dark py-6">Sin informes específicos</p>
        <div v-for="inf in informesEspFiltrados" :key="inf.idInformeEquipoEspecifico"
          class="rounded-xl border border-white-light dark:border-[#1b2e4b] bg-white dark:bg-[#0e1726] p-4">
          <span class="font-bold text-primary text-sm block mb-2">#{{ inf.nroInforme }}</span>
          <div v-if="inf.observaciones" class="mb-1.5">
            <p class="text-xs font-semibold uppercase text-white-dark/60 mb-0.5">Observaciones</p>
            <p class="text-sm text-white-dark line-clamp-2">{{ inf.observaciones }}</p>
          </div>
          <div v-if="inf.trabajoRealizado" class="mb-3">
            <p class="text-xs font-semibold uppercase text-white-dark/60 mb-0.5">Trabajo Realizado</p>
            <p class="text-sm text-white-dark line-clamp-2">{{ inf.trabajoRealizado }}</p>
          </div>
          <button type="button" class="btn btn-sm btn-outline-primary w-full"
            :disabled="descargando === 'esp-' + inf.idInformeEquipoEspecifico"
            @click="descargarInformeEsp(inf.idInformeEquipoEspecifico, inf.nroInforme)">
            <span v-if="descargando === 'esp-' + inf.idInformeEquipoEspecifico"
              class="animate-spin border-2 border-primary border-l-transparent rounded-full w-3 h-3 inline-block mr-1" />
            <span v-else>Descargar PDF</span>
          </button>
        </div>
      </div>
      <!-- Tabla desktop -->
      <div class="hidden sm:block table-responsive">
        <table class="table-striped table-hover">
          <thead>
            <tr>
              <th>Nro. Informe</th>
              <th>Observaciones</th>
              <th>Trabajo Realizado</th>
              <th class="text-center">PDF</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="informesEspFiltrados.length === 0">
              <td colspan="4" class="text-center text-white-dark py-6">Sin informes específicos</td>
            </tr>
            <tr v-for="inf in informesEspFiltrados" :key="inf.idInformeEquipoEspecifico">
              <td class="font-semibold whitespace-nowrap">#{{ inf.nroInforme }}</td>
              <td class="max-w-xs"><span class="block truncate text-white-dark" :title="inf.observaciones">{{ inf.observaciones || '—' }}</span></td>
              <td class="max-w-xs"><span class="block truncate text-white-dark" :title="inf.trabajoRealizado">{{ inf.trabajoRealizado || '—' }}</span></td>
              <td class="text-center">
                <button type="button" class="btn btn-xs btn-outline-primary px-2"
                  :disabled="descargando === 'esp-' + inf.idInformeEquipoEspecifico"
                  @click="descargarInformeEsp(inf.idInformeEquipoEspecifico, inf.nroInforme)">
                  <span v-if="descargando === 'esp-' + inf.idInformeEquipoEspecifico"
                    class="animate-spin border-2 border-primary border-l-transparent rounded-full w-3 h-3 inline-block" />
                  <span v-else>PDF</span>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>

  <!-- ══════════════════════════════════════════════════════════════ -->
  <!-- Tab: Informes Generales                                       -->
  <!-- ══════════════════════════════════════════════════════════════ -->
  <div v-if="tabActivo === 'generales'" class="panel">
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
      <h5 class="text-xl font-bold dark:text-white-light/90">
        Informes Generales
        <span class="ml-2 badge bg-primary/10 text-primary py-1 px-3 rounded-full text-sm">
          {{ informesGenFiltrados.length }} registros
        </span>
      </h5>
      <div class="flex w-full flex-col gap-3 sm:w-auto sm:flex-row sm:items-center">
        <button type="button" class="btn btn-primary" @click="abrirModalNuevoGen">
          <svg class="inline w-4 h-4 mr-1.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Nuevo Informe
        </button>
        <div class="relative">
          <input v-model="busquedaGeneral" type="text" placeholder="Buscar..."
            class="peer form-input py-2 ltr:pr-11 rtl:pl-11 w-full sm:w-52" />
          <div class="absolute top-1/2 -translate-y-1/2 peer-focus:text-primary ltr:right-[11px] rtl:left-[11px]">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="11.5" cy="11.5" r="9.5" opacity="0.5" /><path d="M18.5 18.5L22 22" stroke-linecap="round" />
            </svg>
          </div>
        </div>
      </div>
    </div>

    <SathLoader v-if="cargandoGenerales" size="md" label="Cargando informes..." class="py-10" />
    <template v-else>
      <!-- Cards mobile -->
      <div class="sm:hidden space-y-3">
        <p v-if="informesGenFiltrados.length === 0" class="text-center text-white-dark py-6">Sin informes generales</p>
        <div v-for="inf in informesGenFiltrados" :key="inf.idInformeEquipoGeneral"
          class="rounded-xl border border-white-light dark:border-[#1b2e4b] bg-white dark:bg-[#0e1726] p-4">
          <div class="flex items-start justify-between mb-2">
            <span class="font-bold text-primary text-sm">#{{ inf.nroInforme }}</span>
            <span class="text-xs text-white-dark">{{ inf.fechaCierreInforme || '—' }}</span>
          </div>
          <p class="text-xs text-white-dark/60 mb-0.5">Solicitud: <span class="font-semibold text-white-dark">{{ inf.nroSolicitud ?? '—' }}</span></p>
          <div v-if="inf.observaciones" class="mb-3">
            <p class="text-xs font-semibold uppercase text-white-dark/60 mb-0.5 mt-1.5">Observaciones</p>
            <p class="text-sm text-white-dark line-clamp-2">{{ inf.observaciones }}</p>
          </div>
          <button type="button" class="btn btn-sm btn-outline-primary w-full mt-2"
            :disabled="descargando === 'gen-' + inf.idInformeEquipoGeneral"
            @click="descargarInformeGen(inf.idInformeEquipoGeneral, inf.nroInforme)">
            <span v-if="descargando === 'gen-' + inf.idInformeEquipoGeneral"
              class="animate-spin border-2 border-primary border-l-transparent rounded-full w-3 h-3 inline-block mr-1" />
            <span v-else>Descargar PDF</span>
          </button>
        </div>
      </div>
      <!-- Tabla desktop -->
      <div class="hidden sm:block table-responsive">
        <table class="table-striped table-hover">
          <thead>
            <tr>
              <th>Nro. Informe</th>
              <th>Nro. Solicitud</th>
              <th>Observaciones</th>
              <th>Fecha Cierre</th>
              <th class="text-center">PDF</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="informesGenFiltrados.length === 0">
              <td colspan="5" class="text-center text-white-dark py-6">Sin informes generales</td>
            </tr>
            <tr v-for="inf in informesGenFiltrados" :key="inf.idInformeEquipoGeneral">
              <td class="font-semibold whitespace-nowrap">#{{ inf.nroInforme }}</td>
              <td class="text-white-dark whitespace-nowrap">{{ inf.nroSolicitud ?? '—' }}</td>
              <td class="max-w-xs"><span class="block truncate text-white-dark" :title="inf.observaciones">{{ inf.observaciones || '—' }}</span></td>
              <td class="text-white-dark whitespace-nowrap">{{ inf.fechaCierreInforme || '—' }}</td>
              <td class="text-center">
                <button type="button" class="btn btn-xs btn-outline-primary px-2"
                  :disabled="descargando === 'gen-' + inf.idInformeEquipoGeneral"
                  @click="descargarInformeGen(inf.idInformeEquipoGeneral, inf.nroInforme)">
                  <span v-if="descargando === 'gen-' + inf.idInformeEquipoGeneral"
                    class="animate-spin border-2 border-primary border-l-transparent rounded-full w-3 h-3 inline-block" />
                  <span v-else>PDF</span>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>

  <!-- ══════════════════════════════════════════════════════════════ -->
  <!-- Tab: Reporte General ServiceDesk                                     -->
  <!-- ══════════════════════════════════════════════════════════════ -->
  <div v-if="tabActivo === 'general-sath'" class="panel">
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
      <h5 class="text-xl font-bold dark:text-white-light/90">Reporte General ServiceDesk</h5>
    </div>

    <p class="text-white-dark text-sm mb-5">
      Genera el reporte consolidado de solicitudes en el rango de fechas indicado.
    </p>

    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6 max-w-lg">
      <div>
        <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Fecha Inicial</label>
        <input v-model="rgFechaInicial" type="date" class="form-input w-full" />
      </div>
      <div>
        <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Fecha Final</label>
        <input v-model="rgFechaFinal" type="date" class="form-input w-full" />
      </div>
    </div>

    <div class="flex flex-wrap gap-3">
      <button type="button" class="btn btn-primary" :disabled="descargandoRG" @click="descargarReporteGeneral">
        <span v-if="descargandoRG"
          class="animate-spin border-2 border-white border-l-transparent rounded-full w-4 h-4 inline-block mr-2 align-middle" />
        <svg v-else class="inline w-4 h-4 mr-1.5 align-middle" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/>
        </svg>
        Descargar PDF
      </button>
      <button type="button" class="btn btn-outline-success" :disabled="descargandoExcel" @click="descargarReporteExcel">
        <span v-if="descargandoExcel"
          class="animate-spin border-2 border-success border-l-transparent rounded-full w-4 h-4 inline-block mr-2 align-middle" />
        <svg v-else class="inline w-4 h-4 mr-1.5 align-middle" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
          <polyline points="10 9 9 9 8 9"/>
        </svg>
        Descargar Excel
      </button>
    </div>
  </div>

  <!-- ══════════════════════════════════════════════════════════════ -->
  <!-- Tab: Reporte Estadístico de Servicios                         -->
  <!-- ══════════════════════════════════════════════════════════════ -->
  <div v-if="tabActivo === 'estadistico'" class="panel">
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
      <h5 class="text-xl font-bold dark:text-white-light/90">Reporte Estadístico de Servicios</h5>
    </div>

    <p class="text-white-dark text-sm mb-5">
      Genera un reporte con tabla de servicios agrupados por tipo, más gráficos de torta y barras mostrando la distribución del periodo seleccionado.
    </p>

    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6 max-w-lg">
      <div>
        <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Fecha Inicial</label>
        <input v-model="reFechaInicial" type="date" class="form-input w-full" />
      </div>
      <div>
        <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Fecha Final</label>
        <input v-model="reFechaFinal" type="date" class="form-input w-full" />
      </div>
    </div>

    <div class="flex flex-wrap gap-3">
      <button type="button" class="btn btn-primary" :disabled="descargandoRE" @click="descargarReporteEstadistico">
        <span v-if="descargandoRE"
          class="animate-spin border-2 border-white border-l-transparent rounded-full w-4 h-4 inline-block mr-2 align-middle" />
        <svg v-else class="inline w-4 h-4 mr-1.5 align-middle" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/>
        </svg>
        Descargar PDF
      </button>
      <button type="button" class="btn btn-outline-success" :disabled="descargandoREExcel" @click="descargarReporteEstadisticoExcel">
        <span v-if="descargandoREExcel"
          class="animate-spin border-2 border-success border-l-transparent rounded-full w-4 h-4 inline-block mr-2 align-middle" />
        <svg v-else class="inline w-4 h-4 mr-1.5 align-middle" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
          <polyline points="10 9 9 9 8 9"/>
        </svg>
        Descargar Excel
      </button>
    </div>
  </div>

  <!-- ══════════════════════════════════════════════════════════════ -->
  <!-- Tab: Reporte Personalizado ServiceDesk                               -->
  <!-- ══════════════════════════════════════════════════════════════ -->
  <div v-if="tabActivo === 'personalizado'" class="panel">
    <div class="flex flex-wrap items-center justify-between gap-4 mb-5">
      <h5 class="text-xl font-bold dark:text-white-light/90">Reporte Personalizado ServiceDesk</h5>
    </div>
    <p class="text-white-dark text-sm mb-5">
      Genera un reporte filtrado por rango de fechas y filtros opcionales.
    </p>
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-5 max-w-2xl">
      <div>
        <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Fecha Inicial</label>
        <input v-model="rpFechaInicial" type="date" class="form-input w-full" />
      </div>
      <div>
        <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Fecha Final</label>
        <input v-model="rpFechaFinal" type="date" class="form-input w-full" />
      </div>
      <div>
        <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">
          Unidad <span class="normal-case font-normal">(opcional)</span>
        </label>
        <SearchSelect v-model="rpIdUnidad" :options="rpUnidades" label-key="nombreUni" value-key="idUnidad"
          placeholder="Todas las unidades" empty-label="Todas las unidades" empty-value="" />
      </div>
      <div>
        <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">
          Tipo Servicio <span class="normal-case font-normal">(opcional)</span>
        </label>
        <SearchSelect v-model="rpIdTipoServicio" :options="rpTiposServicio" label-key="nombreTipo" value-key="idTipoServicio"
          placeholder="Todos los tipos" empty-label="Todos los tipos" empty-value="" />
      </div>
      <div>
        <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">
          Tipo de Recurso <span class="normal-case font-normal">(opcional)</span>
        </label>
        <SearchSelect v-model="rpIdTipoActivo" :options="rpTiposActivo" label-key="nombreTipoAct" value-key="idTipoActivo"
          placeholder="Todos los tipos" empty-label="Todos los tipos" empty-value="" />
      </div>
    </div>
    <div class="flex flex-wrap gap-3">
      <button type="button" class="btn btn-primary" :disabled="descargandoRP" @click="descargarReportePersonalizado">
        <span v-if="descargandoRP" class="animate-spin border-2 border-white border-l-transparent rounded-full w-4 h-4 inline-block mr-2 align-middle" />
        <svg v-else class="inline w-4 h-4 mr-1.5 align-middle" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/>
        </svg>
        Descargar PDF
      </button>
      <button type="button" class="btn btn-outline-success" :disabled="descargandoRPExcel" @click="descargarReportePersonalizadoExcel">
        <span v-if="descargandoRPExcel" class="animate-spin border-2 border-success border-l-transparent rounded-full w-4 h-4 inline-block mr-2 align-middle" />
        <svg v-else class="inline w-4 h-4 mr-1.5 align-middle" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
        </svg>
        Descargar Excel
      </button>
    </div>
  </div>

  <!-- ══════════════════════════════════════════════════════════════ -->
  <!-- Modal: Observación para Ficha Técnica                        -->
  <!-- ══════════════════════════════════════════════════════════════ -->
  <Teleport to="body">
  <Transition name="fade">
    <div v-if="modalObservacion" class="fixed inset-0 z-[999] overflow-y-auto bg-black/60" @click.self="modalObservacion = false">
      <div class="flex min-h-screen items-center justify-center px-4">
        <div class="panel w-full max-w-sm rounded-lg border-0 p-0 overflow-hidden">
          <div class="flex items-center justify-between bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4">
            <div class="flex items-center gap-3">
              <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary/10">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-primary">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/>
                </svg>
              </div>
              <div>
                <h3 class="text-base font-semibold">Ficha Técnica</h3>
                <p class="text-xs text-white-dark">{{ solicitudParaReporte?.nroSol || '' }}</p>
              </div>
            </div>
            <button type="button" class="text-white-dark hover:text-dark" @click="modalObservacion = false">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
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

  <!-- ══════════════════════════════════════════════════════════════ -->
  <!-- Modal: Nuevo Informe Específico                               -->
  <!-- ══════════════════════════════════════════════════════════════ -->
  <Teleport to="body">
  <Transition name="fade">
    <div v-if="modalNuevoEsp" class="fixed inset-0 z-[999] overflow-y-auto bg-black/60" @click.self="modalNuevoEsp = false">
      <div class="flex min-h-screen items-center justify-center px-4">
        <div class="panel w-full max-w-lg rounded-lg border-0 p-0 overflow-hidden">
          <div class="flex items-center justify-between bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4">
            <div class="flex items-center gap-3">
              <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary/10">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-primary">
                  <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
                </svg>
              </div>
              <div>
                <h3 class="text-base font-semibold">Nuevo Informe Específico</h3>
                <p class="text-xs text-white-dark">Complete los campos del formulario</p>
              </div>
            </div>
            <button type="button" class="text-white-dark hover:text-dark" @click="modalNuevoEsp = false">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
          <SathLoader v-if="cargandoDetalles" size="sm" />
          <div v-else class="p-5 space-y-4">
            <div>
              <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">
                Recurso <span class="text-danger">*</span>
              </label>
              <SearchSelect
                v-model="formEsp.idDetalleSolicitud"
                :options="detallesConLabel"
                label-key="labelDisplay"
                value-key="idDetalleSolicitud"
                placeholder="Buscar recurso..."
                empty-label="Seleccionar recurso..."
                empty-value=""
              />
            </div>
            <div>
              <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Observaciones</label>
              <textarea v-model="formEsp.observaciones" class="form-textarea w-full" rows="3" />
            </div>
            <div>
              <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Trabajo Realizado</label>
              <textarea v-model="formEsp.trabajoRealizado" class="form-textarea w-full" rows="3" />
            </div>
            <div>
              <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Detalle</label>
              <textarea v-model="formEsp.detalle" class="form-textarea w-full" rows="2" />
            </div>
            <div class="flex justify-end gap-2">
              <button class="btn btn-outline-danger" @click="modalNuevoEsp = false">Cancelar</button>
              <button class="btn btn-primary" :disabled="guardandoEsp" @click="crearInformeEspecifico">
                {{ guardandoEsp ? 'Guardando...' : 'Crear Informe' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>
  </Teleport>

  <!-- ══════════════════════════════════════════════════════════════ -->
  <!-- Modal: Nuevo Informe General                                  -->
  <!-- ══════════════════════════════════════════════════════════════ -->
  <Teleport to="body">
  <Transition name="fade">
    <div v-if="modalNuevoGen" class="fixed inset-0 z-[999] overflow-y-auto bg-black/60" @click.self="modalNuevoGen = false">
      <div class="flex min-h-screen items-center justify-center px-4">
        <div class="panel w-full max-w-lg rounded-lg border-0 p-0 overflow-hidden">
          <div class="flex items-center justify-between bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4">
            <div class="flex items-center gap-3">
              <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary/10">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-primary">
                  <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
                </svg>
              </div>
              <div>
                <h3 class="text-base font-semibold">Nuevo Informe General</h3>
                <p class="text-xs text-white-dark">Complete los campos del formulario</p>
              </div>
            </div>
            <button type="button" class="text-white-dark hover:text-dark" @click="modalNuevoGen = false">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
          <SathLoader v-if="cargandoSolicitudesGen" size="sm" />
          <div v-else class="p-5 space-y-4">
            <div>
              <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">
                Solicitud <span class="text-danger">*</span>
              </label>
              <SearchSelect
                v-model="formGen.idSolicitud"
                :options="solicitudesConLabel"
                label-key="labelDisplay"
                value-key="idSolicitud"
                placeholder="Buscar solicitud..."
                empty-label="Seleccionar solicitud..."
                empty-value=""
              />
            </div>
            <div>
              <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Observaciones</label>
              <textarea v-model="formGen.observaciones" class="form-textarea w-full" rows="3" />
            </div>
            <div>
              <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">Resumen Técnico</label>
              <textarea v-model="formGen.resumenTecnico" class="form-textarea w-full" rows="3" />
            </div>
            <div class="flex justify-end gap-2">
              <button class="btn btn-outline-danger" @click="modalNuevoGen = false">Cancelar</button>
              <button class="btn btn-primary" :disabled="guardandoGen" @click="crearInformeGeneral">
                {{ guardandoGen ? 'Guardando...' : 'Crear Informe' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>
  </Teleport>

  <!-- ══════════════════════════════════════════════════════════════ -->
  <!-- Modal: Observación para Ficha Servicio Técnico               -->
  <!-- ══════════════════════════════════════════════════════════════ -->
  <Teleport to="body">
  <Transition name="fade">
    <div v-if="modalFichaServicio" class="fixed inset-0 z-[999] overflow-y-auto bg-black/60" @click.self="modalFichaServicio = false">
      <div class="flex min-h-screen items-center justify-center px-4">
        <div class="panel w-full max-w-sm rounded-lg border-0 p-0 overflow-hidden">
          <div class="flex items-center justify-between bg-[#fbfbfb] dark:bg-[#121c2c] px-5 py-4">
            <div class="flex items-center gap-3">
              <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary/10">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-primary">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/>
                </svg>
              </div>
              <div>
                <h3 class="text-base font-semibold">Ficha Servicio Técnico</h3>
                <p class="text-xs text-white-dark">{{ solicitudParaFichaServicio?.nroSol || '' }}</p>
              </div>
            </div>
            <button type="button" class="text-white-dark hover:text-dark" @click="modalFichaServicio = false">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
          <div class="p-5 space-y-4">
            <p v-if="solicitudParaFichaServicio?.descripcionSol" class="text-sm text-white-dark">
              {{ solicitudParaFichaServicio.descripcionSol.substring(0, 80) }}
            </p>
            <div>
              <label class="mb-1 block text-xs font-semibold uppercase text-white-dark">
                Observación <span class="normal-case font-normal">(opcional)</span>
              </label>
              <textarea v-model="observacionFichaServicio" class="form-textarea w-full" rows="3"
                placeholder="Observaciones del técnico..." />
            </div>
            <div class="flex justify-end gap-2">
              <button class="btn btn-outline-danger" @click="modalFichaServicio = false">Cancelar</button>
              <button class="btn btn-primary" @click="confirmarDescargaFichaServicio">Descargar PDF</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
