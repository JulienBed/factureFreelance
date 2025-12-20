import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'

const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor to add JWT token
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor to handle token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Ne pas rediriger automatiquement pour les endpoints d'authentification
    // Les erreurs doivent être gérées par les composants Vue correspondants
    const isAuthEndpoint = originalRequest?.url?.includes('/auth/login') || 
                          originalRequest?.url?.includes('/auth/register') ||
                          originalRequest?.url?.includes('/auth/verify-otp')

    if (error.response?.status === 401 && !originalRequest._retry && !isAuthEndpoint) {
      originalRequest._retry = true

      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          // TODO: Implement token refresh endpoint
          // For now, just redirect to login
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          window.location.href = '/login'
        } catch (refreshError) {
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          window.location.href = '/login'
        }
      } else {
        window.location.href = '/login'
      }
    }

    return Promise.reject(error)
  }
)

export default api
