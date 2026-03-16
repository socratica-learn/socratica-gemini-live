import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export interface LiveSessionTokenResponse {
  token: string
  model: string
  expiresAt: string
  newSessionExpiresAt: string
}

export interface AudioTranscriptionResponse {
  transcript: string
}

export interface TutorTranscriptEntry {
  speaker: string
  text: string
}

export interface TutorSessionPayload {
  sessionId?: string
  title: string
  topic: string
  learningGoal?: string
  tutorMode?: string
  demoScript?: string
  transcriptEntries: TutorTranscriptEntry[]
}

export interface SavedTutorSession {
  id: string
  userId: string
  title: string
  topic: string
  learningGoal?: string
  tutorMode?: string
  demoScript?: string
  transcriptEntries: TutorTranscriptEntry[]
  createdAt: string
  updatedAt: string
}

const getLiveUserId = (): string => {
  if (typeof window === 'undefined') {
    return 'anonymous-live-user'
  }

  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    try {
      const parsedUser = JSON.parse(storedUser)
      if (parsedUser?.id) {
        return parsedUser.id
      }
    } catch {
      // Fall back to a guest identifier.
    }
  }

  const existingGuestId = localStorage.getItem('live_guest_user_id')
  if (existingGuestId) {
    return existingGuestId
  }

  const guestId = `guest-${crypto.randomUUID()}`
  localStorage.setItem('live_guest_user_id', guestId)
  return guestId
}

export const liveVoiceService = {
  async createSessionToken(): Promise<LiveSessionTokenResponse> {
    const response = await api.post<LiveSessionTokenResponse>('/ai/live/session-token')
    return response.data
  },

  async transcribeAudio(audio: Blob): Promise<string> {
    const formData = new FormData()
    formData.append('audio', audio, 'speech.wav')

    const response = await api.post<AudioTranscriptionResponse>('/ai/live/transcribe', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    return response.data.transcript ?? ''
  },

  async listTutorSessions(): Promise<SavedTutorSession[]> {
    const response = await api.get<SavedTutorSession[]>('/ai/live-tutor/sessions', {
      headers: {
        'X-User-Id': getLiveUserId(),
      },
    })
    return response.data
  },

  async getTutorSession(sessionId: string): Promise<SavedTutorSession> {
    const response = await api.get<SavedTutorSession>(`/ai/live-tutor/sessions/${sessionId}`, {
      headers: {
        'X-User-Id': getLiveUserId(),
      },
    })
    return response.data
  },

  async saveTutorSession(payload: TutorSessionPayload): Promise<SavedTutorSession> {
    const response = await api.post<SavedTutorSession>('/ai/live-tutor/sessions', payload, {
      headers: {
        'X-User-Id': getLiveUserId(),
      },
    })
    return response.data
  },
}
