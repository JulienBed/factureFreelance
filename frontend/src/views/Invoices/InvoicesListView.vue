<template>
  <div class="space-y-6">
    <div class="sm:flex sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Factures</h1>
        <p class="mt-1 text-sm text-gray-500">
          Gérez vos factures
        </p>
      </div>
      <div class="mt-4 sm:mt-0">
        <router-link
          to="/invoices/new"
          class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
        >
          Nouvelle facture
        </router-link>
      </div>
    </div>

    <div class="bg-white shadow overflow-hidden sm:rounded-md">
      <ul v-if="invoices.length > 0" class="divide-y divide-gray-200">
        <li v-for="invoice in invoices" :key="invoice.id">
          <router-link
            :to="`/invoices/${invoice.id}`"
            class="block hover:bg-gray-50 transition"
          >
            <div class="px-4 py-4 sm:px-6">
              <div class="flex items-center justify-between">
                <div class="flex-1">
                  <div class="flex items-center justify-between">
                    <p class="text-sm font-medium text-indigo-600 truncate">
                      {{ invoice.invoiceNumber }}
                    </p>
                    <div class="ml-2 flex-shrink-0 flex">
                      <span
                        :class="getStatusColor(invoice.status)"
                        class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                      >
                        {{ getStatusLabel(invoice.status) }}
                      </span>
                    </div>
                  </div>
                  <div class="mt-2 sm:flex sm:justify-between">
                    <div class="sm:flex">
                      <p class="flex items-center text-sm text-gray-500">
                        Client: {{ invoice.client?.companyName }}
                      </p>
                    </div>
                    <div class="mt-2 flex items-center text-sm text-gray-500 sm:mt-0">
                      <p>
                        {{ formatCurrency(invoice.total) }}
                      </p>
                    </div>
                  </div>
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
        <p class="text-sm text-gray-500">Aucune facture trouvée</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useInvoicesStore } from '@/stores/invoices'

const invoicesStore = useInvoicesStore()
const invoices = ref<any[]>([])

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR'
  }).format(amount)
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    DRAFT: 'Brouillon',
    SENT: 'Envoyée',
    PAID: 'Payée',
    OVERDUE: 'En retard',
    CANCELLED: 'Annulée'
  }
  return labels[status] || status
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    DRAFT: 'bg-gray-100 text-gray-800',
    SENT: 'bg-blue-100 text-blue-800',
    PAID: 'bg-green-100 text-green-800',
    OVERDUE: 'bg-red-100 text-red-800',
    CANCELLED: 'bg-gray-100 text-gray-800'
  }
  return colors[status] || 'bg-gray-100 text-gray-800'
}

onMounted(async () => {
  await invoicesStore.fetchInvoices()
  invoices.value = invoicesStore.invoices
})
</script>
