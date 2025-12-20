<template>
  <div class="space-y-8">
    <div>
      <h1 class="text-3xl font-bold text-gray-900 tracking-tight">
        {{ isEdit ? t('invoices.form.titleEdit') : t('invoices.form.titleNew') }}
      </h1>
      <p class="mt-2 text-sm text-gray-600 font-medium">
        {{ isEdit ? t('invoices.form.subtitleEdit') : t('invoices.form.subtitleNew') }}
      </p>
    </div>

    <form @submit.prevent="handleSubmit" class="space-y-8 bg-white/80 backdrop-blur-sm shadow-lg rounded-2xl p-8 border border-gray-100">
      <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
        <div>
          <label for="clientId" class="block text-sm font-semibold text-gray-700 mb-2">
            {{ t('invoices.form.client') }} *
          </label>
          <select
            id="clientId"
            v-model="form.clientId"
            required
            class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900"
          >
            <option value="">{{ t('invoices.form.selectClient') }}</option>
            <option v-for="client in clients" :key="client.id" :value="client.id">
              {{ client.companyName }}
            </option>
          </select>
        </div>

        <div>
          <label for="status" class="block text-sm font-semibold text-gray-700 mb-2">
            {{ t('invoices.form.status') }} *
          </label>
          <select
            id="status"
            v-model="form.status"
            required
            class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900"
          >
            <option value="DRAFT">{{ t('invoices.status.DRAFT') }}</option>
            <option value="SENT">{{ t('invoices.status.SENT') }}</option>
            <option value="PAID">{{ t('invoices.status.PAID') }}</option>
            <option value="OVERDUE">{{ t('invoices.status.OVERDUE') }}</option>
            <option value="CANCELLED">{{ t('invoices.status.CANCELLED') }}</option>
          </select>
        </div>

        <div>
          <label for="issueDate" class="block text-sm font-semibold text-gray-700 mb-2">
            {{ t('invoices.form.issueDate') }} *
          </label>
          <input
            id="issueDate"
            v-model="form.issueDate"
            type="date"
            required
            class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900"
          />
        </div>

        <div>
          <label for="dueDate" class="block text-sm font-semibold text-gray-700 mb-2">
            {{ t('invoices.form.dueDate') }} *
          </label>
          <input
            id="dueDate"
            v-model="form.dueDate"
            type="date"
            required
            class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900"
          />
        </div>

        <div class="col-span-2">
          <label class="block text-sm font-semibold text-gray-700 mb-3">
            {{ t('invoices.form.items') }}
          </label>
          <div class="space-y-3">
            <div
              v-for="(item, index) in form.items"
              :key="index"
              class="flex gap-3 items-start p-4 bg-gray-50 rounded-xl border border-gray-200"
            >
              <input
                v-model="item.description"
                type="text"
                :placeholder="t('invoices.form.itemDescription')"
                required
                class="flex-1 px-4 py-2.5 bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400"
              />
              <input
                v-model.number="item.quantity"
                type="number"
                step="0.01"
                :placeholder="t('invoices.form.quantity')"
                required
                class="w-20 px-3 py-2.5 bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400"
              />
              <input
                v-model.number="item.unitPrice"
                type="number"
                step="0.01"
                :placeholder="t('invoices.form.unitPrice')"
                required
                class="w-28 px-3 py-2.5 bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400"
              />
              <input
                v-model.number="item.taxRate"
                type="number"
                step="0.01"
                :placeholder="t('invoices.form.taxRate')"
                required
                class="w-24 px-3 py-2.5 bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400"
              />
              <button
                type="button"
                @click="removeItem(index)"
                class="p-2.5 text-red-600 hover:text-white hover:bg-red-600 rounded-lg transition-all duration-200"
                :disabled="form.items.length === 1"
                :class="{ 'opacity-50 cursor-not-allowed': form.items.length === 1 }"
              >
                <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>
          <button
            type="button"
            @click="addItem"
            class="mt-4 inline-flex items-center gap-2 px-4 py-2.5 text-sm font-semibold text-primary-700 bg-primary-50 hover:bg-primary-100 rounded-lg transition-all duration-200"
          >
            <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            {{ t('invoices.form.addItem') }}
          </button>
        </div>

        <div class="col-span-2">
          <label for="notes" class="block text-sm font-semibold text-gray-700 mb-2">
            {{ t('common.notes') }}
          </label>
          <textarea
            id="notes"
            v-model="form.notes"
            rows="4"
            class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400 resize-none"
            :placeholder="t('invoices.form.notesPlaceholder')"
          ></textarea>
        </div>
      </div>

      <div v-if="error" class="rounded-2xl bg-red-50 p-4 border border-red-100">
        <div class="flex">
          <div class="flex-shrink-0">
            <svg class="h-5 w-5 text-red-500" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
          </div>
          <div class="ml-3">
            <p class="text-sm font-semibold text-red-800">{{ error }}</p>
          </div>
        </div>
      </div>

      <div class="flex justify-end gap-3 pt-4 border-t border-gray-100">
        <router-link
          to="/invoices"
          class="inline-flex items-center px-6 py-3 border border-gray-200 shadow-sm text-sm font-semibold rounded-xl text-gray-700 bg-white hover:bg-gray-50 transition-all duration-200"
        >
          {{ t('common.cancel') }}
        </router-link>
        <button
          type="submit"
          :disabled="loading"
          class="inline-flex items-center px-6 py-3 bg-gradient-to-r from-primary-600 to-primary-700 hover:from-primary-700 hover:to-primary-800 text-white font-semibold rounded-xl shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
        >
          {{ loading ? t('common.saving') : t('common.save') }}
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
import { useI18n } from 'vue-i18n'

const route = useRoute()
const router = useRouter()
const invoicesStore = useInvoiceStore()
const clientsStore = useClientStore()
const { t } = useI18n()

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
    await invoicesStore.fetchInvoice(Number(route.params.id))
    const invoice = invoicesStore.currentInvoice
    if (invoice) {
      form.value = {
        clientId: String(invoice.client?.id || ''),
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
    error.value = err.response?.data?.message || t('errors.saveFailed')
  } finally {
    loading.value = false
  }
}
</script>
