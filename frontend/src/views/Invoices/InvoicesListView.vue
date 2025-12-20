<template>
  <div class="space-y-8">
    <div class="sm:flex sm:items-center sm:justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900 tracking-tight">{{ t('invoices.list.title') }}</h1>
        <p class="mt-2 text-sm text-gray-600 font-medium">
          {{ t('invoices.list.subtitle') }}
        </p>
      </div>
      <div class="mt-4 sm:mt-0">
        <router-link
          to="/invoices/new"
          class="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-primary-600 to-primary-700 hover:from-primary-700 hover:to-primary-800 text-white font-semibold rounded-xl shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 transition-all duration-200"
        >
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          {{ t('invoices.list.newInvoice') }}
        </router-link>
      </div>
    </div>

    <div class="bg-white/80 backdrop-blur-sm shadow-lg overflow-hidden rounded-2xl border border-gray-100">
      <ul v-if="invoices.length > 0" class="divide-y divide-gray-100">
        <li v-for="invoice in invoices" :key="invoice.id" class="hover:bg-primary-50/50 transition-all duration-200">
          <div class="px-6 py-5 flex items-start justify-between gap-4">
            <router-link
              :to="`/invoices/${invoice.id}`"
              class="flex-1 min-w-0"
            >
              <div class="flex items-center gap-3 mb-2">
                <p class="text-base font-semibold text-gray-900">
                  {{ invoice.invoiceNumber }}
                </p>
                <span
                  :class="getStatusColor(invoice.status)"
                  class="px-3 py-1 inline-flex text-xs font-semibold rounded-lg"
                >
                  {{ getStatusLabel(invoice.status) }}
                </span>
              </div>
              <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                <p class="text-sm text-gray-600 font-medium">
                  {{ invoice.client?.companyName }}
                </p>
                <p class="text-base font-bold text-gray-900">
                  {{ formatCurrency(invoice.total) }}
                </p>
              </div>
            </router-link>
            <div class="flex-shrink-0 flex items-center gap-2">
              <button
                @click="handleDownloadPdf(invoice.id, invoice.invoiceNumber)"
                :disabled="downloadingId === invoice.id"
                class="p-2 rounded-lg bg-green-50 hover:bg-green-100 text-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                :title="t('invoices.actions.downloadPdf')"
              >
                <svg v-if="downloadingId === invoice.id" class="h-5 w-5 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <svg v-else class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </button>
              <router-link :to="`/invoices/${invoice.id}`">
                <div class="p-2 rounded-lg bg-primary-50 hover:bg-primary-100 transition-colors">
                  <svg class="h-5 w-5 text-primary-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                  </svg>
                </div>
              </router-link>
            </div>
          </div>
        </li>
      </ul>
      <div v-else class="px-6 py-16 text-center">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-gray-100 rounded-2xl mb-4">
          <svg class="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        </div>
        <p class="text-sm font-medium text-gray-600">{{ t('invoices.list.empty.title') }}</p>
        <p class="mt-1 text-xs text-gray-500">{{ t('invoices.list.empty.subtitle') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useInvoiceStore } from '@/stores/invoices'
import { useI18n } from 'vue-i18n'

const invoicesStore = useInvoiceStore()
const invoices = ref<any[]>([])
const downloadingId = ref<number | null>(null)
const { t } = useI18n()

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR'
  }).format(amount)
}

const getStatusLabel = (status: string) => {
  return t(`invoices.status.${status}`, status)
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    DRAFT: 'bg-gray-100 text-gray-700 border border-gray-200',
    SENT: 'bg-blue-50 text-blue-700 border border-blue-200',
    PAID: 'bg-green-50 text-green-700 border border-green-200',
    OVERDUE: 'bg-red-50 text-red-700 border border-red-200',
    CANCELLED: 'bg-gray-100 text-gray-600 border border-gray-200'
  }
  return colors[status] || 'bg-gray-100 text-gray-700 border border-gray-200'
}

const handleDownloadPdf = async (id: number, invoiceNumber: string) => {
  downloadingId.value = id
  try {
    await invoicesStore.downloadPdf(id, invoiceNumber)
  } catch (error) {
    console.error('Error downloading PDF:', error)
  } finally {
    downloadingId.value = null
  }
}

onMounted(async () => {
  await invoicesStore.fetchInvoices()
  invoices.value = invoicesStore.invoices
})
</script>
