<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useWebSocket } from '@/composables/useWebSocket'

const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const showPass = ref(false)
const loading = ref(false)
const errorMsg = ref('')

function togglePass() {
  showPass.value = !showPass.value
}

async function handleLogin() {
  errorMsg.value = ''
  loading.value = true
  try {
    const res = await fetch(`${import.meta.env.VITE_API_BASE || ''}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value.trim(), password: password.value }),
    })
    if (res.ok) {
      const data = await res.json()
      auth.setSession(data.token, {
        username: data.username,
        nombreCompleto: data.nombreCompleto,
        roles: data.roles,
      })
      useWebSocket()
      router.push('/')
    } else {
      const err = await res.json().catch(() => ({}))
      errorMsg.value = err.message || err.error || 'Usuario o contraseña incorrectos'
    }
  } catch {
    errorMsg.value = 'Error de conexión con el servidor'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-root">
    <!-- Panel izquierdo: imagen + logo flotante -->
    <div class="panel-imagen">
      <img class="fondo" src="/images/fondo.png" alt="SATH background" />
      <div class="logo-sobre-imagen">
        <div class="logo-wrap-central">
          <img src="/images/Logo_Sath_Bn.png" alt="SATH Logo" />
        </div>
      </div>
    </div>

    <!-- Panel derecho: formulario -->
    <div class="panel-form" :class="{ 'loading-state': loading }">
      <div class="welcome-icon">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        </svg>
      </div>

      <h1>Bienvenido</h1>
      <p class="sub-text">Ingresa tus credenciales para continuar</p>

      <!-- Error -->
      <Transition name="error-fade">
        <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
      </Transition>

      <form @submit.prevent="handleLogin">
        <!-- Usuario -->
        <div class="input-wrap">
          <label for="username">Usuario</label>
          <div class="input-field">
            <span class="input-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
            </span>
            <input
              id="username"
              v-model="username"
              type="text"
              required
              placeholder="Ingresa tu usuario"
              autocomplete="username"
            />
          </div>
        </div>

        <!-- Contraseña -->
        <div class="input-wrap">
          <label for="password">Contraseña</label>
          <div class="input-field">
            <span class="input-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
            </span>
            <input
              id="password"
              v-model="password"
              :type="showPass ? 'text' : 'password'"
              required
              placeholder="••••••••"
              autocomplete="current-password"
            />
            <button type="button" class="toggle-pass" @click="togglePass">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
            </button>
          </div>
        </div>

        <button type="submit" class="btn-login" :class="{ loading }" :disabled="loading">
          <template v-if="loading">
            <div class="loading-dots">
              <div class="dot"></div>
              <div class="dot"></div>
              <div class="dot"></div>
            </div>
            <span class="loading-text">Verificando credenciales...</span>
          </template>
          <template v-else>Ingresar al sistema</template>
        </button>
      </form>

      <div class="login-divider">
        <span>¿No tienes acceso al sistema?</span>
      </div>

      <router-link to="/solicitud/manual" class="btn-solicitud-publica">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
        </svg>
        Registrar Solicitud Manual
      </router-link>

      <p class="form-footer">SATH © 2026 · Universidad Amazónica de Pando</p>
    </div>
  </div>
</template>

<style scoped>
/* ── RESET LOCAL ── */
*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.login-root {
  font-family: 'Nunito', sans-serif;
  min-height: 100vh;
  display: flex;
  background: #0d0d1a;
  overflow: hidden;
  color: #fff;
}

/* ── PANEL IZQUIERDO ── */
.panel-imagen {
  flex: 1;
  position: relative;
  display: none;
  overflow: hidden;
  background: #000;
}

@media (min-width: 768px) {
  .panel-imagen {
    display: block;
  }
}

.panel-imagen img.fondo {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  opacity: 0.4;
}

.logo-sobre-imagen {
  position: absolute;
  inset: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-wrap-central {
  position: relative;
  width: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  animation: float 4s ease-in-out infinite;
}

.logo-wrap-central::before {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(124, 106, 247, 0.4) 0%, transparent 70%);
  z-index: -1;
}

.logo-wrap-central img {
  width: 100%;
  height: auto;
  object-fit: contain;
  filter: drop-shadow(0 0 20px rgba(124, 106, 247, 0.6))
          drop-shadow(0 0 5px rgba(255, 255, 255, 0.2));
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-15px); }
}

/* ── PANEL DERECHO ── */
.panel-form {
  width: 100%;
  max-width: 420px;
  flex-shrink: 0;
  background: rgba(19, 19, 43, 0.95);
  backdrop-filter: blur(20px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 40px;
  z-index: 10;
  border-left: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.4s ease;
}

.panel-form.loading-state {
  pointer-events: none;
}
.panel-form.loading-state > *:not(form):not(.welcome-icon) {
  opacity: 0.5;
  filter: blur(1px);
}
.panel-form.loading-state .welcome-icon {
  animation: pulse-welcome 1.5s infinite;
}
@keyframes pulse-welcome {
  0%, 100% { transform: scale(1); box-shadow: 0 10px 25px rgba(109, 90, 240, 0.4); }
  50% { transform: scale(1.05); box-shadow: 0 15px 35px rgba(109, 90, 240, 0.6); }
}

@media (max-width: 767px) { .panel-form { max-width: 100%; } }
@media (max-width: 400px)  { .panel-form { padding: 32px 20px; } }

/* ── ICONO BIENVENIDA ── */
.welcome-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #6d5af0 0%, #a855f7 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 25px rgba(109, 90, 240, 0.4);
}

h1 {
  font-size: 1.8rem;
  text-align: center;
  margin-bottom: 10px;
  font-weight: 800;
}

.sub-text {
  text-align: center;
  color: #94a3b8;
  font-size: 0.9rem;
  margin-bottom: 40px;
}

/* ── ERROR ── */
.error-msg {
  background: rgba(239, 68, 68, 0.12);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 10px;
  padding: 12px;
  font-size: 0.85rem;
  color: #f87171;
  margin-bottom: 20px;
  text-align: center;
}

.error-fade-enter-active, .error-fade-leave-active { transition: opacity 0.25s; }
.error-fade-enter-from,  .error-fade-leave-to      { opacity: 0; }

/* ── INPUTS ── */
.input-wrap {
  position: relative;
  margin-bottom: 25px;
}

.input-wrap label {
  display: block;
  font-size: 0.75rem;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 8px;
  font-weight: 700;
}

.input-field {
  position: relative;
}

.input-field input {
  width: 100%;
  padding: 14px 15px 14px 45px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #fff;
  font-family: inherit;
  font-size: 0.95rem;
  transition: all 0.3s;
}

.input-field input::placeholder {
  color: #475569;
}

.input-field input:focus {
  background: rgba(255, 255, 255, 0.07);
  border-color: #6d5af0;
  box-shadow: 0 0 15px rgba(109, 90, 240, 0.2);
  outline: none;
}

.input-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #475569;
  display: flex;
}

.toggle-pass {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: #475569;
  display: flex;
  padding: 0;
}

/* ── BOTÓN ── */
.btn-login {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #6d5af0 0%, #8b5cf6 100%);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-weight: 700;
  font-size: 1rem;
  cursor: pointer;
  box-shadow: 0 10px 20px rgba(109, 90, 240, 0.3);
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-family: inherit;
}

.btn-login:hover:not(:disabled) {
  transform: translateY(-2px);
  filter: brightness(1.1);
}

.btn-login:disabled {
  opacity: 0.8;
  cursor: not-allowed;
}

.btn-login.loading {
  position: relative;
  overflow: hidden;
}

.btn-login.loading::after {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  animation: shimmer-btn 1.5s infinite;
}

@keyframes shimmer-btn {
  100% { left: 100%; }
}

@keyframes spin { to { transform: rotate(360deg); } }
.spinner { animation: spin 0.8s linear infinite; }

/* ── LOADING DOTS ── */
.loading-dots {
  display: flex;
  gap: 4px;
  align-items: center;
}
.loading-dots .dot {
  width: 6px;
  height: 6px;
  background: white;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}
.loading-dots .dot:nth-child(1) { animation-delay: -0.32s; }
.loading-dots .dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1.0); }
}

.loading-text {
  font-size: 0.9rem;
  font-weight: 600;
  letter-spacing: 0.5px;
}

/* ── DIVIDER ── */
.login-divider {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 24px 0 20px;
}
.login-divider::before,
.login-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.08);
}
.login-divider span {
  font-size: 0.75rem;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 600;
  white-space: nowrap;
}

/* ── BOTÓN SOLICITUD PÚBLICA ── */
.btn-solicitud-publica {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  padding: 14px;
  background: transparent;
  border: 2px solid rgba(16, 185, 129, 0.4);
  border-radius: 12px;
  color: #10b981;
  font-weight: 700;
  font-size: 0.95rem;
  text-decoration: none;
  font-family: inherit;
  transition: all 0.3s;
}
.btn-solicitud-publica:hover {
  background: rgba(16, 185, 129, 0.1);
  border-color: #10b981;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(16, 185, 129, 0.2);
  color: #10b981;
}

/* ── PIE ── */
.form-footer {
  margin-top: auto;
  padding-top: 30px;
  text-align: center;
  font-size: 0.75rem;
  color: #475569;
}
</style>
