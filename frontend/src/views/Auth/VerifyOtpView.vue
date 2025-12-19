<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Vérification du code OTP
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          Un code de vérification a été envoyé à votre adresse email
        </p>
      </div>
      <form class="mt-8 space-y-6" @submit.prevent="handleVerifyOtp">
        <div>
          <label for="email" class="sr-only">Email</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            required
            class="appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
            placeholder="Adresse email"
          />
        </div>
        <div>
          <label for="otp" class="sr-only">Code OTP</label>
          <input
            id="otp"
            v-model="form.otp"
            type="text"
            required
            maxlength="6"
            class="appearance-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm text-center text-2xl tracking-widest"
            placeholder="000000"
          />
        </div>

        <div v-if="error" class="text-red-600 text-sm text-center">
          {{ error }}
        </div>

        <div>
          <button
            type="submit"
            :disabled="loading"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
          >
            {{ loading ? 'Vérification...' : 'Vérifier' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  email: '',
  otp: ''
})

const loading = ref(false)
const error = ref('')

const handleVerifyOtp = async () => {
  loading.value = true
  error.value = ''

  try {
    await authStore.verifyOtp(form.value)
    router.push({ name: 'dashboard' })
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Code OTP invalide ou expiré'
  } finally {
    loading.value = false
  }
}
</script>
