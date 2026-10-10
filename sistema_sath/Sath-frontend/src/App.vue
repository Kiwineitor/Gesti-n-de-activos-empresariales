<script setup>
import {useWebSocket} from '@/composables/useWebSocket'
import {useAuthStore} from '@/stores/auth'

const auth = useAuthStore()
if (auth.isAuthenticated) {
  useWebSocket()
}
</script>

<template>
  <RouterView/>
</template>

<!-- Estilos globales que garantizan fondos correctos en toda la app -->
<style>
/* Fondo del área de contenido — light y dark mode */
.main-content {
  background-color: #fafafa;
}

body.dark .main-content {
  background-color: #060818;
}

/* Asegura que el panel tenga fondo sólido visible */
.panel {
  background-color: #ffffff !important;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.12), 0 1px 2px -1px rgba(0, 0, 0, 0.08) !important;
}

body.dark .panel {
  background-color: #0e1726 !important;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.4) !important;
}

/* Color de texto base — cubre también elementos teleportados fuera del main-container */
body {
  color: #0e1726;
}

body.dark {
  color: #e0e6ed;
}
</style>
