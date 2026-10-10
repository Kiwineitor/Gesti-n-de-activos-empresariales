import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      component: () => import('@/components/layout/AppLayout.vue'),
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
        },
        {
          path: 'solicitud',
          name: 'solicitud',
          component: () => import('@/views/SolicitudView.vue'),
        },
        {
          path: 'solicitud/manual',
          name: 'solicitud-manual',
          component: () => import('@/views/SolicitudManualView.vue'),
          meta: { public: true },
        },
        {
          path: 'solicitud/:id',
          name: 'solicitud-detalle',
          component: () => import('@/views/SolicitudDetalleView.vue'),
          props: true,
        },
        {
          path: 'atenciones',
          name: 'atenciones',
          component: () => import('@/views/AtencionesView.vue'),
        },
        {
          path: 'tecnico-asignado',
          name: 'tecnico-asignado',
          component: () => import('@/views/TecnicoAsignadoView.vue'),
        },
        {
          path: 'administrativo',
          name: 'administrativo',
          component: () => import('@/views/AdministrativoView.vue'),
        },
        {
          path: 'configuracion',
          name: 'configuracion',
          component: () => import('@/views/ConfiguracionView.vue'),
        },
        {
          path: 'usuario',
          name: 'usuario',
          component: () => import('@/views/UsuarioView.vue'),
        },
        {
          path: 'servicio',
          name: 'servicio',
          component: () => import('@/views/ServicioView.vue'),
        },
        {
          path: 'activo',
          name: 'activo',
          component: () => import('@/views/ActivoView.vue'),
        },
        {
          path: 'historial-activo',
          name: 'historial-activo',
          component: () => import('@/views/HistorialActivoView.vue'),
        },
        {
          path: 'reportes',
          name: 'reportes',
          component: () => import('@/views/ReportesView.vue'),
        },
        {
          path: 'reportes-legacy',
          name: 'reportes-legacy',
          component: () => import('@/views/ReportesLegacyView.vue'),
        },
        {
          path: 'perfil',
          name: 'perfil',
          component: () => import('@/views/PerfilView.vue'),
        },
        {
          path: 'notificaciones',
          name: 'notificaciones',
          component: () => import('@/views/NotificacionesView.vue'),
        },
        {
          path: 'logs',
          name: 'logs',
          component: () => import('@/views/LogsView.vue'),
        },
        {
          path: 'logs-vue',
          name: 'logs-vue',
          component: () => import('@/views/LogsVueView.vue'),
        },
        {
          path: 'tipo-servicio',
          name: 'tipo-servicio',
          component: () => import('@/views/TipoServicioView.vue'),
        },
        {
          path: 'tipo-activo',
          name: 'tipo-activo',
          component: () => import('@/views/TipoActivoView.vue'),
        },
        {
          path: 'falla',
          name: 'falla',
          component: () => import('@/views/FallaView.vue'),
        },
        {
          path: 'dashboard-tecnico',
          name: 'dashboard-tecnico',
          component: () => import('@/views/DashboardTecnicoView.vue'),
        },
      ],
    },
    {
      path: '/error/500',
      name: 'error-500',
      component: () => import('@/views/Error500View.vue'),
      meta: { public: true },
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && auth.isTokenExpired()) {
    auth.clear()
    return { name: 'login' }
  }
  if (!to.meta.public && !auth.isAuthenticated) {
    return { name: 'login' }
  }
  if (to.name === 'login' && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }
})

export default router
