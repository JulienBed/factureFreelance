<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Paramètres</h1>
      <p class="mt-1 text-sm text-gray-500">
        Gérez vos informations personnelles et de facturation
      </p>
    </div>

    <form @submit.prevent="handleSubmit" class="space-y-6 bg-white shadow sm:rounded-lg p-6">
      <div>
        <h3 class="text-lg font-medium leading-6 text-gray-900">
          Informations personnelles
        </h3>
        <div class="mt-6 grid grid-cols-1 gap-6 sm:grid-cols-2">
          <div>
            <label for="firstName" class="block text-sm font-medium text-gray-700">
              Prénom
            </label>
            <input
              id="firstName"
              v-model="form.firstName"
              type="text"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            />
          </div>

          <div>
            <label for="lastName" class="block text-sm font-medium text-gray-700">
              Nom
            </label>
            <input
              id="lastName"
              v-model="form.lastName"
              type="text"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            />
          </div>

          <div class="sm:col-span-2">
            <label for="email" class="block text-sm font-medium text-gray-700">
              Email
            </label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              disabled
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 bg-gray-50 text-gray-500 sm:text-sm"
            />
          </div>
        </div>
      </div>

      <div class="border-t border-gray-200 pt-6">
        <h3 class="text-lg font-medium leading-6 text-gray-900">
          Informations de facturation
        </h3>
        <div class="mt-6 grid grid-cols-1 gap-6 sm:grid-cols-2">
          <div class="sm:col-span-2">
            <label for="companyName" class="block text-sm font-medium text-gray-700">
              Nom de l'entreprise
            </label>
            <input
              id="companyName"
              v-model="form.companyName"
              type="text"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            />
          </div>

          <div>
            <label for="siret" class="block text-sm font-medium text-gray-700">
              SIRET
            </label>
            <input
              id="siret"
              v-model="form.siret"
              type="text"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            />
          </div>

          <div>
            <label for="phone" class="block text-sm font-medium text-gray-700">
              Téléphone
            </label>
            <input
              id="phone"
              v-model="form.phone"
              type="tel"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            />
          </div>

          <div class="sm:col-span-2">
            <label for="addressStreet" class="block text-sm font-medium text-gray-700">
              Adresse
            </label>
            <input
              id="addressStreet"
              v-model="form.addressStreet"
              type="text"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            />
          </div>

          <div>
            <label for="addressPostalCode" class="block text-sm font-medium text-gray-700">
              Code postal
            </label>
            <input
              id="addressPostalCode"
              v-model="form.addressPostalCode"
              type="text"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            />
          </div>

          <div>
            <label for="addressCity" class="block text-sm font-medium text-gray-700">
              Ville
            </label>
            <input
              id="addressCity"
              v-model="form.addressCity"
              type="text"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            />
          </div>

          <div>
            <label for="iban" class="block text-sm font-medium text-gray-700">
              IBAN
            </label>
            <input
              id="iban"
              v-model="form.iban"
              type="text"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            />
          </div>

          <div>
            <label for="bic" class="block text-sm font-medium text-gray-700">
              BIC
            </label>
            <input
              id="bic"
              v-model="form.bic"
              type="text"
              class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            />
          </div>
        </div>
      </div>

      <div v-if="error" class="text-red-600 text-sm">
        {{ error }}
      </div>

      <div v-if="success" class="text-green-600 text-sm">
        {{ success }}
      </div>

      <div class="flex justify-end">
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
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const form = ref({
  firstName: '',
  lastName: '',
  email: '',
  companyName: '',
  siret: '',
  phone: '',
  addressStreet: '',
  addressPostalCode: '',
  addressCity: '',
  iban: '',
  bic: ''
})

const loading = ref(false)
const error = ref('')
const success = ref('')

onMounted(() => {
  if (authStore.user) {
    form.value = { ...authStore.user }
  }
})

const handleSubmit = async () => {
  loading.value = true
  error.value = ''
  success.value = ''

  try {
    // TODO: Call API to update user profile
    success.value = 'Paramètres enregistrés avec succès'
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erreur lors de l\'enregistrement'
  } finally {
    loading.value = false
  }
}
</script>
