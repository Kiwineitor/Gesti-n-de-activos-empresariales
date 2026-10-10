<script setup>
defineProps({
  width:  { type: String, default: '100%' },
  height: { type: String, default: '1rem' },
  circle: { type: Boolean, default: false },
  class:  { type: String, default: '' },
})
</script>

<template>
  <div
    class="skeleton-loader"
    :class="[circle ? 'rounded-full' : 'rounded', $props.class]"
    :style="{ width, height }"
  ></div>
</template>

<style scoped>
.skeleton-loader {
  background: linear-gradient(
    90deg,
    rgba(109, 90, 240, 0.05) 25%,
    rgba(109, 90, 240, 0.15) 50%,
    rgba(109, 90, 240, 0.05) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite linear;
}

/* Ajuste para modo claro */
:global(html.light) .skeleton-loader {
  background: linear-gradient(
    90deg,
    rgba(109, 90, 240, 0.08) 25%,
    rgba(109, 90, 240, 0.18) 50%,
    rgba(109, 90, 240, 0.08) 75%
  );
  background-size: 200% 100%;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
