<template>
  <div class="space-y-8">
    <div>
      <h1 class="text-3xl font-bold text-gray-900 tracking-tight">
        {{ isEdit ? t('clients.form.titleEdit') : t('clients.form.titleNew') }}
      </h1>
      <p class="mt-2 text-sm text-gray-600 font-medium">
        {{ isEdit ? t('clients.form.subtitleEdit') : t('clients.form.subtitleNew') }}
      </p>
    </div>

    <form @submit.prevent="handleSubmit" class="space-y-8 bg-white/80 backdrop-blur-sm shadow-lg rounded-2xl p-8 border border-gray-100">
      <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
        <div class="col-span-2">
          <label for="companyName" class="block text-sm font-semibold text-gray-700 mb-2">
            {{ t('clients.form.companyName') }} *
          </label>
          <input
            id="companyName"
            v-model="form.companyName"
            type="text"
            required
            class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400"
            :placeholder="t('clients.form.companyNamePlaceholder')"
          />
        </div>

        <div>
          <label for="email" class="block text-sm font-semibold text-gray-700 mb-2">
            {{ t('common.email') }} *
          </label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            required
            class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400"
            :placeholder="t('clients.form.emailPlaceholder')"
          />
        </div>

        <div>
          <label for="phone" class="block text-sm font-semibold text-gray-700 mb-2">
            {{ t('common.phone') }}
          </label>
          <input
            id="phone"
            v-model="form.phone"
            type="tel"
            class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400"
            :placeholder="t('clients.form.phonePlaceholder')"
          />
        </div>

        <div class="col-span-2">
          <label for="addressStreet" class="block text-sm font-semibold text-gray-700 mb-2">
            {{ t('common.address') }}
          </label>
          <input
            id="addressStreet"
            v-model="form.addressStreet"
            type="text"
            class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400"
            :placeholder="t('clients.form.addressPlaceholder')"
          />
        </div>

        <div>
          <label for="addressPostalCode" class="block text-sm font-semibold text-gray-700 mb-2">
            {{ t('common.postalCode') }}
          </label>
          <input
            id="addressPostalCode"
            v-model="form.addressPostalCode"
            type="text"
            class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400"
            :placeholder="t('clients.form.postalCodePlaceholder')"
          />
        </div>

        <div>
          <label for="addressCity" class="block text-sm font-semibold text-gray-700 mb-2">
            {{ t('common.city') }}
          </label>
          <input
            id="addressCity"
            v-model="form.addressCity"
            type="text"
            class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400"
            :placeholder="t('clients.form.cityPlaceholder')"
          />
        </div>

        <div>
          <label for="siret" class="block text-sm font-semibold text-gray-700 mb-2">
            {{ t('clients.form.siret') }}
          </label>
          <input
            id="siret"
            v-model="form.siret"
            type="text"
            class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400"
            :placeholder="t('clients.form.siretPlaceholder')"
          />
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

      <div class="flex justify-end gap-3 pt-4">
        <router-link
          to="/clients"
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
import { useClientStore } from '@/stores/clients'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const router = useRouter()
const clientsStore = useClientStore()
const { t } = useI18n()

const isEdit = computed(() => !!route.params.id)

const form = ref({
  companyName: '',
  email: '',
  phone: '',
  addressStreet: '',
  addressPostalCode: '',
  addressCity: '',
  siret: ''
})

const loading = ref(false)
const error = ref('')

onMounted(async () => {
  if (isEdit.value) {
    const client = await clientsStore.fetchClient(Number(route.params.id))
    if (client) {
      form.value = { ...client }
    }
  }
})

const handleSubmit = async () => {
  loading.value = true
  error.value = ''

  try {
    if (isEdit.value) {
      await clientsStore.updateClient(Number(route.params.id), form.value)
    } else {
      await clientsStore.createClient(form.value)
    }
    router.push({ name: 'clients' })
  } catch (err: any) {
    error.value = err.response?.data?.message || t('errors.saveFailed')
  } finally {
    loading.value = false
  }
}
</script>
