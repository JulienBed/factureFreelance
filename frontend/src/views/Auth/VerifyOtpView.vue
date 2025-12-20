<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 via-white to-primary-100 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full">
      <!-- Card avec glassmorphism -->
      <div class="bg-white/80 backdrop-blur-xl shadow-2xl rounded-3xl p-8 border border-primary-100/50">
        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-primary-600 to-primary-700 rounded-2xl mb-4 shadow-lg">
            <svg class="w-8 h-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
          </div>
          <h2 class="text-3xl font-bold text-gray-900 tracking-tight">
            Vérification du code OTP
          </h2>
          <p class="mt-2 text-sm text-gray-600 font-medium">
            Un code de vérification a été envoyé à votre adresse email
          </p>
        </div>

        <form class="space-y-5" @submit.prevent="handleVerifyOtp">
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

          <div class="space-y-4">
            <div>
              <label for="email" class="block text-sm font-semibold text-gray-700 mb-2">Email</label>
              <input
                id="email"
                v-model="form.email"
                type="email"
                required
                readonly
                class="w-full px-4 py-3 bg-gray-100 border border-gray-200 rounded-xl text-gray-600 cursor-not-allowed"
                placeholder="Adresse email"
              />
            </div>
            <div>
              <label for="otp" class="block text-sm font-semibold text-gray-700 mb-2">Code OTP</label>
              <input
                id="otp"
                v-model="form.otp"
                type="text"
                required
                maxlength="6"
                class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400 text-center text-3xl tracking-[0.5em] font-semibold"
                placeholder="000000"
                autofocus
              />
            </div>
          </div>

          <div class="pt-2">
            <button
              type="submit"
              :disabled="loading"
              class="w-full bg-gradient-to-r from-primary-600 to-primary-700 hover:from-primary-700 hover:to-primary-800 text-white font-semibold py-3.5 px-6 rounded-xl shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
            >
              {{ loading ? 'Vérification...' : 'Vérifier' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = ref({
  email: '',
  otp: ''
})

const loading = ref(false)
const error = ref('')

onMounted(() => {
  // Pré-remplir l'email depuis les paramètres de la route
  if (route.query.email) {
    form.value.email = route.query.email as string
  }
})

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
