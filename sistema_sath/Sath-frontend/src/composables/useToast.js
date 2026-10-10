import Swal from 'sweetalert2'

const ICONS = {
  success: `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>`,
  error:   `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>`,
  warning: `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`,
  info:    `<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>`,
}

const COLORS = {
  success: '#00ab55',
  error:   '#e7515a',
  warning: '#e2a03f',
  info:    '#2196f3',
}

let _timer = null

export function useToast() {
  function toast(msg, icon = 'success') {
    if (_timer) { clearTimeout(_timer); _timer = null }
    document.querySelector('.sath-notif-bar')?.remove()

    const color = COLORS[icon] ?? COLORS.info

    const bar = document.createElement('div')
    bar.className = 'sath-notif-bar'
    bar.innerHTML = `<span class="sath-notif-bar__icon">${ICONS[icon] ?? ICONS.info}</span><span>${msg}</span>`
    bar.style.setProperty('--sath-notif-color', color)
    document.body.appendChild(bar)

    _timer = setTimeout(() => {
      bar.classList.add('sath-notif-bar--out')
      setTimeout(() => bar.remove(), 280)
      _timer = null
    }, 3000)
  }

  function confirm(title, text = '') {
    return Swal.fire({
      title,
      text,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#e7515a',
    })
  }

  return { toast, confirm }
}
