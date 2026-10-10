<script setup>
import { computed, ref } from 'vue'
import { useSession } from '@/composables/useSession'

const session = useSession()
const renewing = ref(false)

const urgente = computed(() => session.secondsLeft.value <= 120)

async function renovar() {
  renewing.value = true
  await session.renewSession()
  renewing.value = false
}
</script>

<template>
  <Teleport to="body">
    <Transition name="session-fade">
      <div
        v-if="session.showModal.value"
        class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/60 backdrop-blur-sm"
      >
        <div class="w-full max-w-sm mx-4 rounded-2xl bg-white dark:bg-[#1b2e4b] shadow-2xl overflow-hidden">
          <!-- Franja superior -->
          <div
            class="h-1.5 w-full transition-colors duration-500"
            :class="urgente ? 'bg-danger' : 'bg-warning'"
          />

          <div class="px-6 py-8 text-center">
            <!-- Ícono -->
            <div
              class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full transition-colors duration-500"
              :class="urgente ? 'bg-danger/10 text-danger' : 'bg-warning/10 text-warning'"
            >
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="12" cy="12" r="10" />
                <path d="M12 6v6l4 2" stroke-linecap="round" />
              </svg>
            </div>

            <h2 class="text-lg font-bold dark:text-white mb-1">Tu sesión está por vencer</h2>
            <p class="text-sm text-gray-500 dark:text-gray-400 mb-6">
              Renueva para continuar trabajando sin perder tu progreso.
            </p>

            <!-- Countdown -->
            <div
              class="mb-6 inline-flex items-center gap-2 rounded-xl px-6 py-3 font-mono text-3xl font-bold transition-colors duration-500"
              :class="urgente ? 'bg-danger/10 text-danger' : 'bg-warning/10 text-warning'"
            >
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10" />
                <path d="M12 6v6l4 2" stroke-linecap="round" />
              </svg>
              {{ session.minutesLeft.value }}
            </div>

            <!-- Botones -->
            <div class="flex flex-col gap-2.5">
              <button
                class="btn btn-warning w-full"
                :disabled="renewing"
                @click="renovar"
              >
                <svg v-if="!renewing" class="mr-2 inline" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                  <path d="M4 4v5h5M20 20v-5h-5" />
                  <path d="M20.49 9A9 9 0 0 0 5.64 5.64M3.51 15a9 9 0 0 0 14.85 3.36" />
                </svg>
                <span v-if="renewing" class="inline-block h-4 w-4 mr-2 animate-spin rounded-full border-2 border-white border-t-transparent" />
                {{ renewing ? 'Renovando...' : 'Renovar sesión' }}
              </button>
              <button
                class="btn btn-outline-danger w-full"
                :disabled="renewing"
                @click="session.destroySession()"
              >
                Cerrar sesión
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.session-fade-enter-active,
.session-fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}
.session-fade-enter-from,
.session-fade-leave-to {
  opacity: 0;
  transform: scale(0.96);
}
</style>