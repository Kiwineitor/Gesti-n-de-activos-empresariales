import { Capacitor } from '@capacitor/core'

function blobToBase64(blob) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload  = () => resolve(reader.result.split(',')[1])
    reader.onerror = reject
    reader.readAsDataURL(blob)
  })
}

export function useDownload() {
  async function descargarArchivo(blob, filename) {
    if (Capacitor.isNativePlatform()) {
      const base64 = await blobToBase64(blob)
      const { Filesystem, Directory } = await import('@capacitor/filesystem')

      // Pedir permisos de almacenamiento (necesario en Android < 10)
      try {
        const perm = await Filesystem.checkPermissions()
        if (perm.publicStorage !== 'granted') {
          await Filesystem.requestPermissions()
        }
      } catch (_) { /* no aplica en todos los dispositivos */ }

      // Guardar directo en la carpeta Descargas del dispositivo
      try {
        await Filesystem.writeFile({
          path:      `Download/${filename}`,
          data:      base64,
          directory: Directory.ExternalStorage,
          recursive: true,
        })
        return { ok: true, mensaje: `"${filename}" guardado en Descargas` }
      } catch (e) {
        console.warn('[useDownload] ExternalStorage falló, usando Share:', e)
      }

      // Fallback: guardar en directorio externo de la app y compartir
      try {
        const { Share } = await import('@capacitor/share')
        const result = await Filesystem.writeFile({
          path:      filename,
          data:      base64,
          directory: Directory.External,
          recursive: true,
        })
        await Share.share({ title: filename, url: result.uri, dialogTitle: 'Guardar archivo' })
        return { ok: true, mensaje: null }
      } catch (e) {
        console.warn('[useDownload] Share falló:', e)
        return { ok: false, mensaje: 'No se pudo guardar el archivo' }
      }
    }

    // Navegador web — descarga estándar
    const url = URL.createObjectURL(blob)
    const a   = document.createElement('a')
    a.href     = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
    return { ok: true, mensaje: null }
  }

  return { descargarArchivo }
}