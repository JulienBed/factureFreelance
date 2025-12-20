<template>
  <div class="space-y-8">
    <div>
      <h1 class="text-3xl font-bold text-gray-900 tracking-tight">Tableau de bord</h1>
      <p class="mt-2 text-sm text-gray-600 font-medium">
        Vue d'ensemble de votre activité
      </p>
    </div>

    <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
      <!-- Card Total Clients -->
      <div class="bg-white/80 backdrop-blur-sm overflow-hidden shadow-lg hover:shadow-xl transition-all duration-200 rounded-2xl border border-gray-100">
        <div class="p-6">
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <p class="text-sm font-semibold text-gray-600 uppercase tracking-wide">
                Total Clients
              </p>
              <p class="mt-2 text-3xl font-bold text-gray-900">
                {{ stats.totalClients }}
              </p>
            </div>
            <div class="flex-shrink-0">
              <div class="p-3 bg-gradient-to-br from-primary-500 to-primary-600 rounded-xl shadow-lg">
                <svg class="h-7 w-7 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Card Factures du mois -->
      <div class="bg-white/80 backdrop-blur-sm overflow-hidden shadow-lg hover:shadow-xl transition-all duration-200 rounded-2xl border border-gray-100">
        <div class="p-6">
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <p class="text-sm font-semibold text-gray-600 uppercase tracking-wide">
                Factures du mois
              </p>
              <p class="mt-2 text-3xl font-bold text-gray-900">
                {{ stats.monthlyInvoices }}
              </p>
            </div>
            <div class="flex-shrink-0">
              <div class="p-3 bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl shadow-lg">
                <svg class="h-7 w-7 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Card Factures payées -->
      <div class="bg-white/80 backdrop-blur-sm overflow-hidden shadow-lg hover:shadow-xl transition-all duration-200 rounded-2xl border border-gray-100">
        <div class="p-6">
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <p class="text-sm font-semibold text-gray-600 uppercase tracking-wide">
                Factures payées
              </p>
              <p class="mt-2 text-3xl font-bold text-green-600">
                {{ formatCurrency(stats.paidAmount) }}
              </p>
            </div>
            <div class="flex-shrink-0">
              <div class="p-3 bg-gradient-to-br from-green-500 to-green-600 rounded-xl shadow-lg">
                <svg class="h-7 w-7 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Card En attente -->
      <div class="bg-white/80 backdrop-blur-sm overflow-hidden shadow-lg hover:shadow-xl transition-all duration-200 rounded-2xl border border-gray-100">
        <div class="p-6">
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <p class="text-sm font-semibold text-gray-600 uppercase tracking-wide">
                En attente
              </p>
              <p class="mt-2 text-3xl font-bold text-orange-600">
                {{ formatCurrency(stats.pendingAmount) }}
              </p>
            </div>
            <div class="flex-shrink-0">
              <div class="p-3 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl shadow-lg">
                <svg class="h-7 w-7 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Factures récentes -->
    <div class="bg-white/80 backdrop-blur-sm shadow-lg rounded-2xl border border-gray-100">
      <div class="px-6 py-6">
        <h3 class="text-lg font-bold text-gray-900">
          Factures récentes
        </h3>
        <div class="mt-6">
          <p class="text-sm text-gray-500">
            Les factures récentes s'afficheront ici
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const stats = ref({
  totalClients: 0,
  monthlyInvoices: 0,
  paidAmount: 0,
  pendingAmount: 0
})

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR'
  }).format(amount)
}

onMounted(async () => {
  // TODO: Load real statistics from API
})
</script>
