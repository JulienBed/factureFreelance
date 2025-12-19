import api from './api'
import type { LoginRequest, RegisterRequest, VerifyOtpRequest, AuthResponse } from '@/types'

export const authService = {
  async register(data: RegisterRequest): Promise<{ message: string }> {
    const response = await api.post('/auth/register', data)
    return response.data
  },

  async login(data: LoginRequest): Promise<{ message: string }> {
    const response = await api.post('/auth/login', data)
    return response.data
  },

  async verifyOtp(data: VerifyOtpRequest): Promise<AuthResponse> {
    const response = await api.post('/auth/verify-otp', data)
    return response.data
  },

  logout() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }
}
