import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginRequest, RegisterRequest, VerifyOtpRequest } from '@/types'
import { authService } from '@/services/auth.service'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const isAuthenticated = computed(() => !!accessToken.value)

  // Initialize from localStorage
  const init = () => {
    const storedToken = localStorage.getItem('accessToken')
    const storedRefreshToken = localStorage.getItem('refreshToken')
    const storedUser = localStorage.getItem('user')

    if (storedToken && storedUser) {
      accessToken.value = storedToken
      refreshToken.value = storedRefreshToken
      user.value = JSON.parse(storedUser)
    }
  }

  const setAuth = (tokens: { accessToken: string; refreshToken: string }, userData: User) => {
    accessToken.value = tokens.accessToken
    refreshToken.value = tokens.refreshToken
    user.value = userData

    localStorage.setItem('accessToken', tokens.accessToken)
    localStorage.setItem('refreshToken', tokens.refreshToken)
    localStorage.setItem('user', JSON.stringify(userData))
  }

  const register = async (data: RegisterRequest) => {
    return await authService.register(data)
  }

  const login = async (data: LoginRequest) => {
    return await authService.login(data)
  }

  const verifyOtp = async (data: VerifyOtpRequest) => {
    const response = await authService.verifyOtp(data)
    setAuth(
      { accessToken: response.accessToken, refreshToken: response.refreshToken },
      response.user
    )
    return response
  }

  const logout = () => {
    authService.logout()
    accessToken.value = null
    refreshToken.value = null
    user.value = null
  }

  return {
    user,
    accessToken,
    isAuthenticated,
    init,
    register,
    login,
    verifyOtp,
    logout
  }
})
