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
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Facture Freelance
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          Connectez-vous à votre compte
        </p>
      </div>
      <form class="mt-8 space-y-6" @submit.prevent="handleLogin">
        <div v-if="error" class="rounded-md bg-red-50 p-4 border border-red-200">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <p class="text-sm font-medium text-red-800">{{ error }}</p>
            </div>
          </div>
        </div>

        <div class="rounded-md shadow-sm space-y-4">
          <div>
            <label for="email" class="label">Email</label>
            <input
              id="email"
              v-model="email"
              type="email"
              required
              class="input"
              placeholder="votre@email.com"
            />
          </div>
          <div>
            <label for="password" class="label">Mot de passe</label>
            <input
              id="password"
              v-model="password"
              type="password"
              required
              class="input"
              placeholder="••••••••"
            />
          </div>
        </div>

        <div>
          <button
            type="submit"
            :disabled="loading"
            class="w-full btn-primary"
          >
            {{ loading ? 'Connexion...' : 'Se connecter' }}
          </button>
        </div>

        <div class="text-center">
          <RouterLink to="/register" class="text-sm text-primary-600 hover:text-primary-500">
            Pas encore de compte ? S'inscrire
          </RouterLink>
        </div>
      </form>
    </div>
  </div>
</template>
