import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

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

export interface PersonalDetailsRequest {
  fullName: string
  preferredName: string
  countryTimeZone: string
  currentRole: string
}

export interface AcademicBackgroundRequest {
  fieldOfStudy: string
  currentRole: string
  experienceLevel: string
  backgroundDescription: string
}

export interface WorkspaceRequest {
  theme: string
  layoutPreference: string
  topicsStudying: string
  upcomingDeadlines: string
}

export const personalizeService = {
  async savePersonalDetails(userId: string, data: PersonalDetailsRequest) {
    const response = await api.post('/personalize/personal-details', data, {
      headers: {
        'X-User-Id': userId,
      },
    })
    return response.data
  },

  async saveAcademicBackground(userId: string, data: AcademicBackgroundRequest) {
    const response = await api.post('/personalize/academic-background', data, {
      headers: {
        'X-User-Id': userId,
      },
    })
    return response.data
  },

  async saveWorkspace(userId: string, data: WorkspaceRequest) {
    const response = await api.post('/personalize/workspace', data, {
      headers: {
        'X-User-Id': userId,
      },
    })
    return response.data
  },
}
