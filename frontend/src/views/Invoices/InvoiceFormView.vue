<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">
        {{ isEdit ? 'Modifier la facture' : 'Nouvelle facture' }}
      </h1>
    </div>

    <form @submit.prevent="handleSubmit" class="space-y-6 bg-white shadow sm:rounded-lg p-6">
      <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
        <div>
          <label for="clientId" class="block text-sm font-medium text-gray-700">
            Client *
          </label>
          <select
            id="clientId"
            v-model="form.clientId"
            required
            class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
          >
            <option value="">Sélectionnez un client</option>
            <option v-for="client in clients" :key="client.id" :value="client.id">
              {{ client.companyName }}
            </option>
          </select>
        </div>

        <div>
          <label for="status" class="block text-sm font-medium text-gray-700">
            Statut *
          </label>
          <select
            id="status"
            v-model="form.status"
            required
            class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
          >
            <option value="DRAFT">Brouillon</option>
            <option value="SENT">Envoyée</option>
            <option value="PAID">Payée</option>
            <option value="OVERDUE">En retard</option>
            <option value="CANCELLED">Annulée</option>
          </select>
        </div>

        <div>
          <label for="issueDate" class="block text-sm font-medium text-gray-700">
            Date d'émission *
          </label>
          <input
            id="issueDate"
            v-model="form.issueDate"
            type="date"
            required
            class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
          />
        </div>

        <div>
          <label for="dueDate" class="block text-sm font-medium text-gray-700">
            Date d'échéance *
          </label>
          <input
            id="dueDate"
            v-model="form.dueDate"
            type="date"
            required
            class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
          />
        </div>

        <div class="col-span-2">
          <label class="block text-sm font-medium text-gray-700 mb-2">
            Lignes de facture
          </label>
          <div class="space-y-2">
            <div
              v-for="(item, index) in form.items"
              :key="index"
              class="flex gap-2 items-start"
            >
              <input
                v-model="item.description"
                type="text"
                placeholder="Description"
                required
                class="flex-1 border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              />
              <input
                v-model.number="item.quantity"
                type="number"
                step="0.01"
                placeholder="Qté"
                required
                class="w-20 border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              />
              <input
                v-model.number="item.unitPrice"
                type="number"
                step="0.01"
                placeholder="Prix unitaire"
                required
                class="w-32 border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              />
              <input
                v-model.number="item.taxRate"
                type="number"
                step="0.01"
                placeholder="TVA %"
                required
                class="w-24 border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              />
              <button
                type="button"
                @click="removeItem(index)"
                class="px-3 py-2 text-red-600 hover:text-red-800"
              >
                ✕
              </button>
            </div>
          </div>
          <button
            type="button"
            @click="addItem"
            class="mt-2 text-sm text-indigo-600 hover:text-indigo-800"
          >
            + Ajouter une ligne
          </button>
        </div>

        <div class="col-span-2">
          <label for="notes" class="block text-sm font-medium text-gray-700">
            Notes
          </label>
          <textarea
            id="notes"
            v-model="form.notes"
            rows="3"
            class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
          ></textarea>
        </div>
      </div>

      <div v-if="error" class="text-red-600 text-sm">
        {{ error }}
      </div>

      <div class="flex justify-end space-x-3">
        <router-link
          to="/invoices"
          class="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
        >
          Annuler
        </router-link>
        <button
          type="submit"
          :disabled="loading"
          class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
        >
          {{ loading ? 'Enregistrement...' : 'Enregistrer' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useInvoiceStore } from '@/stores/invoices'
import { useClientStore } from '@/stores/clients'

const route = useRoute()
const router = useRouter()
const invoicesStore = useInvoiceStore()
const clientsStore = useClientStore()

const isEdit = computed(() => !!route.params.id)
const clients = ref<any[]>([])

const form = ref({
  clientId: '',
  status: 'DRAFT',
  issueDate: new Date().toISOString().split('T')[0],
  dueDate: '',
  items: [
    { description: '', quantity: 1, unitPrice: 0, taxRate: 20 }
  ],
  notes: ''
})

const loading = ref(false)
const error = ref('')

const addItem = () => {
  form.value.items.push({ description: '', quantity: 1, unitPrice: 0, taxRate: 20 })
}

const removeItem = (index: number) => {
  if (form.value.items.length > 1) {
    form.value.items.splice(index, 1)
  }
}

onMounted(async () => {
  await clientsStore.fetchClients()
  clients.value = clientsStore.clients

  if (isEdit.value) {
    const invoice = await invoicesStore.fetchInvoice(Number(route.params.id))
    if (invoice) {
      form.value = {
        clientId: invoice.client.id,
        status: invoice.status,
        issueDate: invoice.issueDate,
        dueDate: invoice.dueDate,
        items: invoice.items || [{ description: '', quantity: 1, unitPrice: 0, taxRate: 20 }],
        notes: invoice.notes || ''
      }
    }
  }
})

const handleSubmit = async () => {
  loading.value = true
  error.value = ''

  try {
    if (isEdit.value) {
      await invoicesStore.updateInvoice(Number(route.params.id), form.value)
    } else {
      await invoicesStore.createInvoice(form.value)
    }
    router.push({ name: 'invoices' })
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erreur lors de l\'enregistrement'
  } finally {
    loading.value = false
  }
}
</script>
