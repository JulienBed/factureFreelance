import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Auth/LoginView.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Auth/RegisterView.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/verify-otp',
      name: 'verify-otp',
      component: () => import('@/views/Auth/VerifyOtpView.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/',
      component: () => import('@/components/layout/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('@/views/Dashboard/DashboardView.vue')
        },
        {
          path: '/clients',
          name: 'clients',
          component: () => import('@/views/Clients/ClientsListView.vue')
        },
        {
          path: '/clients/new',
          name: 'client-create',
          component: () => import('@/views/Clients/ClientFormView.vue')
        },
        {
          path: '/clients/:id',
          name: 'client-edit',
          component: () => import('@/views/Clients/ClientFormView.vue')
        },
        {
          path: '/invoices',
          name: 'invoices',
          component: () => import('@/views/Invoices/InvoicesListView.vue')
        },
        {
          path: '/invoices/new',
          name: 'invoice-create',
          component: () => import('@/views/Invoices/InvoiceFormView.vue')
        },
        {
          path: '/invoices/:id',
          name: 'invoice-edit',
          component: () => import('@/views/Invoices/InvoiceFormView.vue')
        },
        {
          path: '/settings',
          name: 'settings',
          component: () => import('@/views/Settings/SettingsView.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'login' })
  } else if (to.meta.requiresGuest && authStore.isAuthenticated) {
    next({ name: 'dashboard' })
  } else {
    next()
  }
})

export default router
