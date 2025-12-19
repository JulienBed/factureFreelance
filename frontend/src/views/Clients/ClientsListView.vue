<template>
  <div class="space-y-6">
    <div class="sm:flex sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Clients</h1>
        <p class="mt-1 text-sm text-gray-500">
          Gérez vos clients
        </p>
      </div>
      <div class="mt-4 sm:mt-0">
        <router-link
          to="/clients/new"
          class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
        >
          Nouveau client
        </router-link>
      </div>
    </div>

    <div class="bg-white shadow overflow-hidden sm:rounded-md">
      <ul v-if="clients.length > 0" class="divide-y divide-gray-200">
        <li v-for="client in clients" :key="client.id">
          <router-link
            :to="`/clients/${client.id}`"
            class="block hover:bg-gray-50 transition"
          >
            <div class="px-4 py-4 sm:px-6">
              <div class="flex items-center justify-between">
                <div class="flex-1">
                  <p class="text-sm font-medium text-indigo-600 truncate">
                    {{ client.companyName }}
                  </p>
                  <p class="mt-1 text-sm text-gray-500">
                    {{ client.email }}
                  </p>
                </div>
                <div class="ml-2 flex-shrink-0">
                  <svg class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                  </svg>
                </div>
              </div>
            </div>
          </router-link>
        </li>
      </ul>
      <div v-else class="px-4 py-12 text-center">
        <p class="text-sm text-gray-500">Aucun client trouvé</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useClientsStore } from '@/stores/clients'

const clientsStore = useClientsStore()
const clients = ref<any[]>([])

onMounted(async () => {
  await clientsStore.fetchClients()
  clients.value = clientsStore.clients
})
</script>
