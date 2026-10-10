<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useAppStore } from '@/stores/app'
import { useSession } from '@/composables/useSession'
import TheSidebar from './TheSidebar.vue'
import TheNavbar from './TheNavbar.vue'
import SessionModal from '@/components/SessionModal.vue'

const appStore = useAppStore()
const session = useSession()

const showTopButton = ref(false)

function onScroll() {
  showTopButton.value = window.scrollY > 50
}
function goToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  window.addEventListener('scroll', onScroll)
  session.startSession()
})
onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  session.destroy()
})
</script>

<template>
  <!--
    Las clases de layout (vertical, full, ltr, dark, toggle-sidebar)
    las aplica directamente el store al <body>, igual que en Alpine.js original.
    El CSS fue compilado esperando esas clases en body.
  -->

  <!-- Overlay móvil para cerrar sidebar -->
  <div
    v-if="appStore.sidebar"
    class="fixed inset-0 z-50 bg-[black]/60 lg:hidden"
    @click="appStore.toggleSidebar()"
  />

  <!-- Scroll-to-top -->
  <div class="fixed bottom-6 z-50 ltr:right-6 rtl:left-6">
    <Transition name="fade">
      <button
        v-if="showTopButton"
        type="button"
        class="btn btn-outline-primary animate-pulse rounded-full bg-[#fafafa] p-2 dark:bg-[#060818] dark:hover:bg-primary"
        @click="goToTop"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path
            opacity="0.5"
            fill-rule="evenodd"
            clip-rule="evenodd"
            d="M12 20.75C12.4142 20.75 12.75 20.4142 12.75 20L12.75 10.75L11.25 10.75L11.25 20C11.25 20.4142 11.5858 20.75 12 20.75Z"
            fill="currentColor"
          />
          <path
            d="M6.00002 10.75C5.69667 10.75 5.4232 10.5673 5.30711 10.287C5.19103 10.0068 5.25519 9.68417 5.46969 9.46967L11.4697 3.46967C11.6103 3.32902 11.8011 3.25 12 3.25C12.1989 3.25 12.3897 3.32902 12.5304 3.46967L18.5304 9.46967C18.7449 9.68417 18.809 10.0068 18.6929 10.287C18.5768 10.5673 18.3034 10.75 18 10.75L6.00002 10.75Z"
            fill="currentColor"
          />
        </svg>
      </button>
    </Transition>
  </div>

  <SessionModal />

  <div class="main-container min-h-screen text-black dark:text-white-dark" :class="[appStore.navbar]">
    <TheSidebar />

    <div class="main-content flex flex-col min-h-screen">
      <TheNavbar />

      <div class="animate__animated p-4 sm:p-6" :class="[appStore.animation]">
        <RouterView />
      </div>

      <footer class="p-6 pt-0 mt-auto text-center dark:text-white-dark/60 text-xs">
        © {{ new Date().getFullYear() }} SATH · Universidad Amazónica de Pando
      </footer>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
