<template>
  <div class="space-y-8">
    <div class="sm:flex sm:items-center sm:justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900 tracking-tight">Clients</h1>
        <p class="mt-2 text-sm text-gray-600 font-medium">
          Gérez vos clients
        </p>
      </div>
      <div class="mt-4 sm:mt-0">
        <router-link
          to="/clients/new"
          class="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-primary-600 to-primary-700 hover:from-primary-700 hover:to-primary-800 text-white font-semibold rounded-xl shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 transition-all duration-200"
        >
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Nouveau client
        </router-link>
      </div>
    </div>

    <div class="bg-white/80 backdrop-blur-sm shadow-lg overflow-hidden rounded-2xl border border-gray-100">
      <ul v-if="clients.length > 0" class="divide-y divide-gray-100">
        <li v-for="client in clients" :key="client.id">
          <router-link
            :to="`/clients/${client.id}`"
            class="block hover:bg-primary-50/50 transition-all duration-200"
          >
            <div class="px-6 py-5">
              <div class="flex items-center justify-between">
                <div class="flex-1 min-w-0">
                  <p class="text-base font-semibold text-gray-900 truncate">
                    {{ client.companyName }}
                  </p>
                  <p class="mt-1.5 text-sm text-gray-600 font-medium">
                    {{ client.email }}
                  </p>
                </div>
                <div class="ml-4 flex-shrink-0">
                  <div class="p-2 rounded-lg bg-primary-50 group-hover:bg-primary-100 transition-colors">
                    <svg class="h-5 w-5 text-primary-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                  </div>
                </div>
              </div>
            </div>
          </router-link>
        </li>
      </ul>
      <div v-else class="px-6 py-16 text-center">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-2xl mb-4">
          <svg class="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
        </div>
        <p class="text-sm font-medium text-gray-600">Aucun client trouvé</p>
        <p class="mt-1 text-xs text-gray-500">Commencez par créer votre premier client</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useClientStore } from '@/stores/clients'

const clientsStore = useClientStore()
const clients = ref<any[]>([])

onMounted(async () => {
  await clientsStore.fetchClients()
  clients.value = clientsStore.clients
})
</script>
