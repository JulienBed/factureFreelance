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
    await authStore.login({ email: value.value, password: password.value })
    router.push({ name: 'verify-otp', query: { email: email.value } })
  } catch (e: any) {
    error.value = e.response?.data?.message || 'Erreur lors de la connexion'
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
        <div v-if="error" class="rounded-md bg-red-50 p-4">
          <p class="text-sm text-red-800">{{ error }}</p>
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
