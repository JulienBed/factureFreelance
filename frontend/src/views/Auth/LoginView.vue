<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const handleLogin = async () => {
  if (!email.value || !password.value) {
    error.value = 'Veuillez remplir tous les champs'
    return
  }

  loading.value = true
  error.value = ''

  try {
    await authStore.login({ email: email.value, password: password.value })
    router.push({ name: 'verify-otp', query: { email: email.value } })
  } catch (e: any) {
    // Gestion spécifique des erreurs 401 (identifiants incorrects)
    if (e.response?.status === 401) {
      error.value = 'Email ou mot de passe incorrect'
    } else if (e.response?.status === 404) {
      error.value = 'Utilisateur non trouvé. Veuillez créer un compte.'
    } else {
      error.value = e.response?.data?.message || 'Erreur lors de la connexion. Veuillez réessayer.'
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 via-white to-primary-100 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full">
      <!-- Card avec glassmorphism -->
      <div class="bg-white/80 backdrop-blur-xl shadow-2xl rounded-3xl p-8 border border-primary-100/50">
        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-primary-600 to-primary-700 rounded-2xl mb-4 shadow-lg">
            <svg class="w-8 h-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <h2 class="text-3xl font-bold text-gray-900 tracking-tight">
            Facture Freelance
          </h2>
          <p class="mt-2 text-sm text-gray-600 font-medium">
            Connectez-vous à votre compte
          </p>
        </div>

        <form class="space-y-5" @submit.prevent="handleLogin">
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
                v-model="email"
                type="email"
                required
                class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400"
                placeholder="votre@email.com"
              />
            </div>
            <div>
              <label for="password" class="block text-sm font-semibold text-gray-700 mb-2">Mot de passe</label>
              <input
                id="password"
                v-model="password"
                type="password"
                required
                class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200 text-gray-900 placeholder-gray-400"
                placeholder="••••••••"
              />
            </div>
          </div>

          <div class="pt-2">
            <button
              type="submit"
              :disabled="loading"
              class="w-full bg-gradient-to-r from-primary-600 to-primary-700 hover:from-primary-700 hover:to-primary-800 text-white font-semibold py-3.5 px-6 rounded-xl shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
            >
              {{ loading ? 'Connexion...' : 'Se connecter' }}
            </button>
          </div>

          <div class="text-center pt-2">
            <RouterLink to="/register" class="text-sm font-semibold text-primary-600 hover:text-primary-700 transition-colors duration-200">
              Pas encore de compte ? <span class="underline">S'inscrire</span>
            </RouterLink>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
