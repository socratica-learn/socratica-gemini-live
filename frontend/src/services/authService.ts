import axios from 'axios'

// Use proxy in development (Vite proxy), or full URL in production
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || (import.meta.env.DEV ? '/api' : 'http://localhost:8080/api')

export interface SignUpRequest {
  name: string
  surname: string
  email: string
  password: string
  confirmPassword: string
  aboutYourself?: string
}

export interface SignUpResponse {
  token: string
  user: {
    id: string
    email: string
    name: string
    surname: string
  }
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  user: {
    id: string
    email: string
    name: string
    surname: string
  }
}

export interface ForgotPasswordRequest {
  email: string
}

export interface ResetPasswordRequest {
  token: string
  password: string
  confirmPassword: string
}

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Add token to requests if available
api.interceptors.request.use((config) => {
  try {
    const token = typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
  } catch (e) {
    console.error('Failed to get token from localStorage:', e)
  }
  return config
})

export const authService = {
  async signUp(data: SignUpRequest): Promise<SignUpResponse> {
    const requestBody: any = {
      name: data.name,
      surname: data.surname,
      email: data.email,
      password: data.password,
    }
    
    // Only include aboutYourself if it's provided
    if (data.aboutYourself) {
      requestBody.aboutYourself = data.aboutYourself
    }
    
    const response = await api.post<SignUpResponse>('/auth/signup', requestBody)
    return response.data
  },

  async login(data: LoginRequest): Promise<LoginResponse> {
    const response = await api.post<LoginResponse>('/auth/login', data)
    return response.data
  },

  logout() {
    try {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('auth_token')
        localStorage.removeItem('user')
      }
    } catch (e) {
      console.error('Failed to clear localStorage:', e)
    }
  },

  getToken(): string | null {
    try {
      return typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null
    } catch {
      return null
    }
  },

  isAuthenticated(): boolean {
    return !!this.getToken()
  },

  async getGoogleAuthUrl(): Promise<string> {
    const response = await api.get<{ url: string }>('/auth/oauth/google/url')
    return response.data.url
  },

  async getMicrosoftAuthUrl(): Promise<string> {
    const response = await api.get<{ url: string }>('/auth/oauth/microsoft/url')
    return response.data.url
  },

  async forgotPassword(data: ForgotPasswordRequest): Promise<{ message: string }> {
    const response = await api.post<{ message: string }>('/auth/forgot-password', data)
    return response.data
  },

  async resetPassword(data: ResetPasswordRequest): Promise<{ message: string }> {
    const response = await api.post<{ message: string }>('/auth/reset-password', data)
    return response.data
  },
}

