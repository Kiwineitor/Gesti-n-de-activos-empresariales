<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-[#0d0d1a] text-white font-nunito">
    <div class="text-center px-6">
      <div class="mb-8">
        <svg
          class="mx-auto text-danger"
          width="80"
          height="80"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.5"
        >
          <circle cx="12" cy="12" r="10" />
          <line x1="15" y1="9" x2="9" y2="15" />
          <line x1="9" y1="9" x2="15" y2="15" />
        </svg>
      </div>
      <h1 class="text-6xl font-extrabold mb-4 text-danger">500</h1>
      <h2 class="text-2xl font-bold mb-3">Error interno del servidor</h2>
      <p class="text-white/60 mb-8 max-w-md mx-auto">
        Ocurrió un error inesperado en el servidor. Por favor intenta nuevamente o contacta al
        administrador.
      </p>
      <div class="flex gap-4 justify-center">
        <button class="btn btn-outline-danger" @click="router.back()">Volver</button>
        <RouterLink to="/" class="btn btn-primary">Ir al Inicio</RouterLink>
      </div>
    </div>
  </div>
</template>
