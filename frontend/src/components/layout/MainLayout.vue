<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  HomeIcon,
  UsersIcon,
  DocumentTextIcon,
  Cog6ToothIcon,
  ArrowLeftOnRectangleIcon,
  Bars3Icon,
  XMarkIcon
} from '@heroicons/vue/24/outline'

const router = useRouter()
const authStore = useAuthStore()
const sidebarOpen = ref(false)

const navigation = [
  { name: 'Dashboard', href: '/', icon: HomeIcon },
  { name: 'Clients', href: '/clients', icon: UsersIcon },
  { name: 'Factures', href: '/invoices', icon: DocumentTextIcon },
  { name: 'Paramètres', href: '/settings', icon: Cog6ToothIcon }
]

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Mobile sidebar -->
    <div v-if="sidebarOpen" class="fixed inset-0 z-40 lg:hidden">
      <div class="fixed inset-0 bg-gray-600 bg-opacity-75" @click="sidebarOpen = false"></div>
      <div class="fixed inset-y-0 left-0 flex w-64 flex-col bg-white">
        <div class="flex items-center justify-between h-16 px-6 border-b">
          <h1 class="text-xl font-bold text-primary-600">Facture Freelance</h1>
          <button @click="sidebarOpen = false">
            <XMarkIcon class="h-6 w-6" />
          </button>
        </div>
        <nav class="flex-1 px-4 py-6 space-y-1">
          <RouterLink
            v-for="item in navigation"
            :key="item.name"
            :to="item.href"
            class="flex items-center px-4 py-3 text-sm font-medium rounded-md hover:bg-gray-100"
            active-class="bg-primary-50 text-primary-600"
            @click="sidebarOpen = false"
          >
            <component :is="item.icon" class="mr-3 h-5 w-5" />
            {{ item.name }}
          </RouterLink>
        </nav>
      </div>
    </div>

    <!-- Desktop sidebar -->
    <div class="hidden lg:fixed lg:inset-y-0 lg:flex lg:w-64 lg:flex-col">
      <div class="flex min-h-0 flex-1 flex-col bg-white border-r">
        <div class="flex items-center h-16 px-6 border-b">
          <h1 class="text-xl font-bold text-primary-600">Facture Freelance</h1>
        </div>
        <nav class="flex-1 px-4 py-6 space-y-1">
          <RouterLink
            v-for="item in navigation"
            :key="item.name"
            :to="item.href"
            class="flex items-center px-4 py-3 text-sm font-medium rounded-md hover:bg-gray-100"
            active-class="bg-primary-50 text-primary-600"
          >
            <component :is="item.icon" class="mr-3 h-5 w-5" />
            {{ item.name }}
          </RouterLink>
        </nav>
        <div class="p-4 border-t">
          <div class="flex items-center mb-3">
            <div class="flex-1">
              <p class="text-sm font-medium text-gray-900">{{ authStore.user?.firstName }} {{ authStore.user?.lastName }}</p>
              <p class="text-xs text-gray-500">{{ authStore.user?.email }}</p>
            </div>
          </div>
          <button
            @click="handleLogout"
            class="flex w-full items-center px-4 py-2 text-sm font-medium text-red-600 rounded-md hover:bg-red-50"
          >
            <ArrowLeftOnRectangleIcon class="mr-3 h-5 w-5" />
            Déconnexion
          </button>
        </div>
      </div>
    </div>

    <!-- Main content -->
    <div class="lg:pl-64">
      <!-- Mobile header -->
      <div class="sticky top-0 z-10 flex h-16 bg-white border-b lg:hidden">
        <button @click="sidebarOpen = true" class="px-4 text-gray-500">
          <Bars3Icon class="h-6 w-6" />
        </button>
        <div class="flex flex-1 items-center justify-between px-4">
          <h1 class="text-lg font-semibold">Facture Freelance</h1>
        </div>
      </div>

      <!-- Page content -->
      <main class="py-6 px-4 sm:px-6 lg:px-8">
        <RouterView />
      </main>
    </div>
  </div>
</template>
