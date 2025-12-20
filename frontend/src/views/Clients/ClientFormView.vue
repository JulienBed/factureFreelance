<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">
        {{ isEdit ? 'Modifier le client' : 'Nouveau client' }}
      </h1>
    </div>

    <form @submit.prevent="handleSubmit" class="space-y-6 bg-white shadow sm:rounded-lg p-6">
      <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
        <div class="col-span-2">
          <label for="companyName" class="block text-sm font-medium text-gray-700">
            Nom de l'entreprise *
          </label>
          <input
            id="companyName"
            v-model="form.companyName"
            type="text"
            required
            class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
          />
        </div>

        <div>
          <label for="email" class="block text-sm font-medium text-gray-700">
            Email *
          </label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            required
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

        <div class="col-span-2">
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
      </div>

      <div v-if="error" class="text-red-600 text-sm">
        {{ error }}
      </div>

      <div class="flex justify-end space-x-3">
        <router-link
          to="/clients"
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
import { useClientStore } from '@/stores/clients'

const route = useRoute()
const router = useRouter()
const clientsStore = useClientStore()

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
    error.value = err.response?.data?.message || 'Erreur lors de l\'enregistrement'
  } finally {
    loading.value = false
  }
}
</script>
