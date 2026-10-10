import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export const useAppStore = defineStore('app', () => {
  const theme = ref(JSON.parse(localStorage.getItem('_x_theme') || '"light"'))
  const isDarkMode = ref(JSON.parse(localStorage.getItem('_x_isDarkMode') || 'false'))
  const sidebar = ref(false)
  const menu = ref(localStorage.getItem('_x_menu') || 'vertical')
  const layout = ref(localStorage.getItem('_x_layout') || 'full')
  const rtlClass = ref(localStorage.getItem('_x_rtlClass') || 'ltr')
  const animation = ref(localStorage.getItem('_x_animation') || 'animate__fadeIn')
  const navbar = ref(localStorage.getItem('_x_navbar') || 'navbar-sticky')
  const semidark = ref(JSON.parse(localStorage.getItem('_x_semidark') || 'false'))

  const isDark = computed(() => {
    if (theme.value === 'dark') return true
    if (theme.value === 'system') return window.matchMedia('(prefers-color-scheme: dark)').matches
    return isDarkMode.value
  })

  // Aplica las clases de layout al <body> — igual que el Alpine original
  // El CSS fue compilado esperando estas clases en el body
  function applyBodyClasses() {
    const b = document.body
    b.classList.toggle('dark', isDark.value)
    b.classList.toggle('toggle-sidebar', sidebar.value)

    // menu (vertical / collapsible-vertical / horizontal)
    b.classList.remove('vertical', 'collapsible-vertical', 'horizontal')
    b.classList.add(menu.value || 'vertical')

    // layout (full / boxed-layout)
    b.classList.remove('full', 'boxed-layout')
    b.classList.add(layout.value || 'full')

    // rtl class
    b.classList.remove('ltr', 'rtl')
    b.classList.add(rtlClass.value || 'ltr')
  }

  // También en <html>: dir attribute + dark class
  function applyHtmlAttrs() {
    document.documentElement.setAttribute('dir', rtlClass.value || 'ltr')
    document.documentElement.classList.toggle('dark', isDark.value)
  }

  // Ejecutar inmediatamente al crear el store
  applyBodyClasses()
  applyHtmlAttrs()

  // Reaccionar a cambios
  watch([isDark, sidebar, menu, layout, rtlClass], () => {
    applyBodyClasses()
    applyHtmlAttrs()
  })

  function toggleSidebar() {
    sidebar.value = !sidebar.value
  }

  function toggleTheme(val) {
    theme.value = val
    localStorage.setItem('_x_theme', JSON.stringify(val))
  }

  return {
    theme,
    isDarkMode,
    sidebar,
    menu,
    layout,
    rtlClass,
    animation,
    navbar,
    semidark,
    isDark,
    toggleSidebar,
    toggleTheme,
  }
})
