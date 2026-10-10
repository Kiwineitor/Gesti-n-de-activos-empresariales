<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: { default: null },
  options:    { type: Array,   default: () => [] },
  labelKey:   { type: String,  default: 'label' },
  valueKey:   { type: String,  default: 'value' },
  placeholder:{ type: String,  default: 'Seleccionar...' },
  emptyLabel: { type: String,  default: null },
  emptyValue: { default: '' },
  disabled:   { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'change'])

const open        = ref(false)
const search      = ref('')
const rootRef     = ref(null)
const triggerRef  = ref(null)
const dropdownRef = ref(null)
const searchRef   = ref(null)
const dropStyle   = ref({})

const selectedLabel = computed(() => {
  const v = props.modelValue
  if (v === null || v === undefined || v === props.emptyValue) return null
  const opt = props.options.find(o => String(o[props.valueKey]) === String(v))
  return opt ? opt[props.labelKey] : null
})

const filtered = computed(() => {
  const q = search.value.toLowerCase().trim()
  if (!q) return props.options
  return props.options.filter(o =>
    String(o[props.labelKey] ?? '').toLowerCase().includes(q)
  )
})

function calcPosition() {
  const rect = triggerRef.value?.getBoundingClientRect()
  if (!rect) return
  const spaceBelow = window.innerHeight - rect.bottom - 8
  const spaceAbove = rect.top - 8
  const goUp = spaceBelow < 200 && spaceAbove > spaceBelow
  const availableHeight = Math.min(goUp ? spaceAbove : spaceBelow, 360)
  dropStyle.value = {
    position: 'fixed',
    width: rect.width + 'px',
    left: rect.left + 'px',
    zIndex: 9999,
    maxHeight: availableHeight + 'px',
    ...(goUp
      ? { bottom: window.innerHeight - rect.top + 4 + 'px', top: 'auto' }
      : { top: rect.bottom + 4 + 'px',                      bottom: 'auto' }),
  }
}

function toggle() {
  if (props.disabled) return
  if (!open.value) {
    search.value = ''
    calcPosition()
    open.value = true
    nextTick(() => searchRef.value?.focus())
  } else {
    open.value = false
  }
}

function pick(value) {
  emit('update:modelValue', value)
  emit('change', value)
  open.value = false
  search.value = ''
}

function isSelected(opt) {
  return String(opt[props.valueKey]) === String(props.modelValue)
}

function onOutside(e) {
  const inRoot     = rootRef.value?.contains(e.target)
  const inDropdown = dropdownRef.value?.contains(e.target)
  if (!inRoot && !inDropdown) open.value = false
}

function onScrollOrResize(e) {
  if (!open.value) return
  if (dropdownRef.value?.contains(e.target)) return
  calcPosition()
}

onMounted(() => {
  document.addEventListener('mousedown', onOutside)
  window.addEventListener('scroll', onScrollOrResize, true)
  window.addEventListener('resize', onScrollOrResize)
})
onUnmounted(() => {
  document.removeEventListener('mousedown', onOutside)
  window.removeEventListener('scroll', onScrollOrResize, true)
  window.removeEventListener('resize', onScrollOrResize)
})
</script>

<template>
  <div ref="rootRef" class="relative">

    <!-- Trigger -->
    <button
      ref="triggerRef"
      type="button"
      class="form-input flex w-full items-center justify-between gap-2 text-left cursor-pointer select-none"
      :class="disabled ? 'opacity-60 cursor-not-allowed' : ''"
      @click="toggle"
    >
      <span class="truncate" :class="selectedLabel ? '' : 'text-white-dark'">
        {{ selectedLabel ?? placeholder }}
      </span>
      <svg
        class="h-4 w-4 shrink-0 text-white-dark transition-transform duration-200"
        :class="{ 'rotate-180': open }"
        viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
      >
        <path d="M6 9l6 6 6-6" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </button>

    <!-- Dropdown — teleportado al body para evitar clipping en modales/drawers -->
    <Teleport to="body">
      <Transition name="ss-drop">
        <div
          v-if="open"
          ref="dropdownRef"
          :style="dropStyle"
          class="min-w-[180px] flex flex-col rounded-xl border border-white-light bg-white shadow-[0_8px_30px_rgba(0,0,0,0.18)] dark:border-[#253b5e] dark:bg-[#1b2e4b] overflow-hidden"
        >
          <!-- Search input -->
          <div class="p-2 border-b border-white-light/60 dark:border-[#253b5e] flex-shrink-0">
            <div class="relative">
              <input
                ref="searchRef"
                v-model="search"
                type="text"
                class="w-full rounded-lg bg-white-light/60 dark:bg-[#0e1726] py-1.5 pl-8 pr-3 text-sm outline-none border border-transparent focus:border-primary/50 placeholder:text-white-dark transition-colors"
                placeholder="Buscar..."
                @keydown.esc="open = false"
              />
              <svg class="absolute left-2.5 top-1/2 -translate-y-1/2 text-white-dark pointer-events-none"
                width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35" stroke-linecap="round"/>
              </svg>
            </div>
          </div>

          <!-- Options -->
          <ul class="flex-1 min-h-0 overflow-y-auto py-1">

            <!-- Opción vacía -->
            <li v-if="emptyLabel !== null">
              <button
                type="button"
                class="flex w-full items-center gap-2 px-3 py-2.5 text-sm transition-colors hover:bg-primary/5 dark:hover:bg-primary/10"
                :class="(modelValue === emptyValue || modelValue === null)
                  ? 'font-semibold text-primary bg-primary/5 dark:bg-primary/10'
                  : 'text-white-dark'"
                @click="pick(emptyValue)"
              >
                <svg v-if="modelValue === emptyValue || modelValue === null"
                  class="shrink-0 text-primary" width="13" height="13" viewBox="0 0 24 24"
                  fill="none" stroke="currentColor" stroke-width="3">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                <span :class="{ 'pl-[19px]': modelValue !== emptyValue && modelValue !== null }">
                  {{ emptyLabel }}
                </span>
              </button>
            </li>

            <!-- Sin resultados -->
            <li v-if="filtered.length === 0 && options.length > 0"
              class="px-3 py-5 text-center text-sm text-white-dark">
              Sin resultados para "{{ search }}"
            </li>
            <li v-if="options.length === 0"
              class="px-3 py-5 text-center text-sm text-white-dark">
              Sin opciones disponibles
            </li>

            <!-- Opciones -->
            <li v-for="opt in filtered" :key="opt[valueKey]">
              <button
                type="button"
                class="flex w-full items-center gap-2 px-3 py-2.5 text-sm transition-colors hover:bg-primary/5 dark:hover:bg-primary/10"
                :class="isSelected(opt)
                  ? 'font-semibold text-primary bg-primary/5 dark:bg-primary/10'
                  : 'text-dark dark:text-white-dark'"
                @click="pick(opt[valueKey])"
              >
                <svg v-if="isSelected(opt)"
                  class="shrink-0 text-primary" width="13" height="13" viewBox="0 0 24 24"
                  fill="none" stroke="currentColor" stroke-width="3">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                <span class="truncate" :class="{ 'pl-[19px]': !isSelected(opt) }">
                  {{ opt[labelKey] }}
                </span>
              </button>
            </li>
          </ul>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.ss-drop-enter-active,
.ss-drop-leave-active {
  transition: opacity 0.12s ease, transform 0.12s ease;
}
.ss-drop-enter-from,
.ss-drop-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.98);
}
</style>