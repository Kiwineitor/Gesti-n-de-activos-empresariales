<script setup>
defineProps({
  size: {type: String, default: 'md'},  // sm | md | lg
  label: {type: String, default: ''},
})
</script>

<template>
  <div class="sath-loader-wrap">
    <div class="sath-pre-icon-wrap" :class="`sath-loader-${size}`">
      <div class="sath-pre-ring"></div>
      <div class="sath-pre-ring-2"></div>
      <div class="sath-pre-icon-inner">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
          <rect x="4" y="4" width="16" height="16" rx="2"/>
          <rect x="8" y="8" width="8" height="8" rx="1" fill="currentColor" stroke="none"/>
          <line x1="9" y1="1" x2="9" y2="4"/>
          <line x1="12" y1="1" x2="12" y2="4"/>
          <line x1="15" y1="1" x2="15" y2="4"/>
          <line x1="9" y1="20" x2="9" y2="23"/>
          <line x1="12" y1="20" x2="12" y2="23"/>
          <line x1="15" y1="20" x2="15" y2="23"/>
          <line x1="1" y1="9" x2="4" y2="9"/>
          <line x1="1" y1="12" x2="4" y2="12"/>
          <line x1="1" y1="15" x2="4" y2="15"/>
          <line x1="20" y1="9" x2="23" y2="9"/>
          <line x1="20" y1="12" x2="23" y2="12"/>
          <line x1="20" y1="15" x2="23" y2="15"/>
        </svg>
      </div>
    </div>
    <p v-if="label" class="sath-loader-label">{{ label }}</p>
  </div>
</template>

<style scoped>
.sath-loader-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
}

/* Tamaños del contenedor de anillos */
.sath-loader-sm {
  width: 44px;
  height: 44px;
}

.sath-loader-md {
  width: 72px;
  height: 72px;
}

.sath-loader-lg {
  width: 96px;
  height: 96px;
}

/* El SVG dentro de sath-pre-icon-inner hereda el tamaño del contenedor */
.sath-pre-icon-inner svg {
  width: 45%;
  height: 45%;
}

.sath-loader-label {
  font-size: 12px;
  letter-spacing: 0.12em;
  font-family: 'Courier New', monospace;
  color: #4361ee;
  animation: sath-loader-blink 1.2s step-end infinite;
  margin: 0;
}

.dark .sath-loader-label {
  color: rgba(255, 255, 255, 0.4);
}

@keyframes sath-loader-blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.35;
  }
}
</style>