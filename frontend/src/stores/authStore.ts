import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService, type SignUpRequest, type LoginRequest } from '../services/authService'

interface User {
  id: string
  email: string
  name: string
  surname: string
}

export const useAuthStore = defineStore('auth', () => {
  // Safe localStorage access
  const getStoredToken = () => {
    try {
      return typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null
    } catch {
      return null
    }
  }
  
  const getStoredUser = () => {
    try {
      const stored = typeof window !== 'undefined' ? localStorage.getItem('user') : null
      return stored ? JSON.parse(stored) : null
    } catch {
      return null
    }
  }

  const token = ref<string | null>(getStoredToken())
  const user = ref<User | null>(getStoredUser())
  const loading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  function setAuth(tokenValue: string, userValue: User) {
    token.value = tokenValue
    user.value = userValue
    try {
      if (typeof window !== 'undefined') {
        localStorage.setItem('auth_token', tokenValue)
        localStorage.setItem('user', JSON.stringify(userValue))
      }
    } catch (e) {
      console.error('Failed to save to localStorage:', e)
    }
    error.value = null
  }

  function clearAuth() {
    token.value = null
    user.value = null
    authService.logout()
    error.value = null
  }

  async function signUp(data: SignUpRequest) {
    loading.value = true
    error.value = null
    try {
      const response = await authService.signUp(data)
      setAuth(response.token, response.user)
      return response
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Sign up failed. Please try again.'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function login(data: LoginRequest) {
    loading.value = true
    error.value = null
    try {
      const response = await authService.login(data)
      setAuth(response.token, response.user)
      return response
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Login failed. Please check your credentials.'
      throw err
    } finally {
      loading.value = false
    }
  }

  function logout() {
    clearAuth()
  }

  return {
    token,
    user,
    loading,
    error,
    isAuthenticated,
    setAuth,
    signUp,
    login,
    logout,
  }
})

