<template>
  <section class="live-page">
    <header class="hero-card">
      <div>
        <p class="eyebrow">Gemini Live Tutor</p>
        <h1>Build a judge-ready Socratic voice tutor.</h1>
        <p class="hero-copy">
          Pick a tutoring mode, follow a demo script, speak naturally, and save each session so
          your hackathon demo shows both live interruption and persistent study history.
        </p>
      </div>

      <div class="hero-actions">
        <button class="secondary-button" type="button" @click="goHome">Back Home</button>
        <button class="secondary-button" type="button" :disabled="isSaving" @click="saveCurrentSession">
          {{ isSaving ? 'Saving...' : 'Save Session' }}
        </button>
        <button
          v-if="!isConnected"
          class="primary-button"
          type="button"
          :disabled="isBusy"
          @click="startLiveSession"
        >
          {{ isBusy ? 'Connecting...' : 'Start Live Voice' }}
        </button>
        <button
          v-else
          class="danger-button"
          type="button"
          @click="stopLiveSession"
        >
          Stop Session
        </button>
      </div>
    </header>

    <section class="panel-grid">
      <article class="panel setup-panel">
        <h2>Session Setup</h2>

        <label class="field-label" for="preset">Tutor preset</label>
        <select id="preset" v-model="selectedPresetId" class="field-input" @change="applyPreset">
          <option v-for="preset in tutorPresets" :key="preset.id" :value="preset.id">
            {{ preset.label }}
          </option>
        </select>

        <label class="field-label" for="title">Session title</label>
        <input id="title" v-model="sessionTitle" class="field-input" type="text" />

        <label class="field-label" for="goal">Learning goal</label>
        <input id="goal" v-model="learningGoal" class="field-input" type="text" />

        <label class="field-label" for="topic">What do you want to explain?</label>
        <textarea
          id="topic"
          v-model="studyTopic"
          class="topic-input"
          rows="5"
          placeholder="Example: Explain photosynthesis for an oral exam and challenge me whenever I sound vague."
        />

        <div class="interrupt-toggle-row">
          <label class="field-label">Socratica may interrupt</label>
          <button
            type="button"
            class="toggle-button"
            :class="{ active: allowInterruption }"
            @click="allowInterruption = !allowInterruption"
          >
            {{ allowInterruption ? 'On' : 'Off' }}
          </button>
          <span class="helper-text interrupt-helper">
            {{ allowInterruption ? 'Socratica can jump in when unclear or to probe deeper.' : 'Socratica waits for you to finish before responding.' }}
          </span>
        </div>

        <div class="status-row">
          <span class="status-chip" :class="statusClass">{{ connectionLabel }}</span>
          <span class="meta-text">{{ statusMessage }}</span>
        </div>

        <div class="status-list">
          <div class="status-item">
            <span class="status-label">Conversation state</span>
            <strong>{{ conversationPhaseLabel }}</strong>
          </div>
          <div class="status-item">
            <span class="status-label">Whose turn</span>
            <strong>{{ conversationTurnOwner }}</strong>
          </div>
          <div class="status-item">
            <span class="status-label">Listening</span>
            <strong>{{ isListening ? 'Yes' : 'No' }}</strong>
          </div>
          <div class="status-item">
            <span class="status-label">Model speaking</span>
            <strong>{{ isModelSpeaking ? 'Yes' : 'No' }}</strong>
          </div>
          <div class="status-item">
            <span class="status-label">Live model</span>
            <strong>{{ liveModel }}</strong>
          </div>
          <div class="status-item">
            <span class="status-label">Tutor mode</span>
            <strong>{{ tutorMode }}</strong>
          </div>
        </div>

        <div class="script-note fallback-panel">
          <span class="status-label">Input fallback</span>
          <p v-if="voiceInputBlocked" class="helper-text">
            Voice transcript input is blocked by this browser. Type one sentence below and Gemini
            will still answer out loud.
          </p>
          <p v-else class="helper-text">
            If browser speech recognition does not work, you can type here and keep the live audio
            demo moving.
          </p>

          <textarea
            v-model="manualUserInput"
            class="topic-input fallback-input"
            rows="3"
            :disabled="!isConnected"
            placeholder="Type a question or answer for the tutor..."
          />

          <button
            class="secondary-button fallback-send"
            type="button"
            :disabled="!isConnected || !manualUserInput.trim()"
            @click="submitManualUserInput"
          >
            Send To Tutor
          </button>
        </div>
      </article>

      <article class="panel">
        <h2>Judge Demo Script</h2>
        <ol class="demo-steps">
          <li v-for="(step, index) in activeDemoSteps" :key="`${step}-${index}`">
            {{ step }}
          </li>
        </ol>

        <div class="script-note">
          <span class="status-label">Narration hook</span>
          <p>{{ activePreset.demoNarration }}</p>
        </div>
      </article>

      <article class="panel transcript-panel">
        <div class="panel-header">
          <h2>Transcript</h2>
          <button class="ghost-button" type="button" @click="clearTranscript">Clear</button>
        </div>

        <div class="transcript-live-status">
          <div class="turn-indicator" :class="phaseToneClass">
            <span class="turn-pulse" />
            <div>
              <span class="status-label">Live turn state</span>
              <strong>{{ conversationPhaseLabel }}</strong>
            </div>
          </div>
          <p class="helper-text">{{ conversationGuidance }}</p>
        </div>

        <div ref="transcriptStreamRef" class="transcript-stream">
          <p v-if="!visibleTranscriptEntries.length" class="empty-state">
            The transcript will appear here once the session starts.
          </p>

          <div
            v-for="entry in visibleTranscriptEntries"
            :key="entry.id"
            class="transcript-entry"
            :class="[
              entry.speaker === 'You' ? 'user-entry' : 'model-entry',
              entry.status !== 'final' ? 'pending-entry' : '',
              entry.status === 'interrupted' ? 'interrupted-entry' : '',
            ]"
          >
            <div class="transcript-entry-header">
              <span class="speaker">{{ entry.speaker }}</span>
              <span v-if="entry.status === 'streaming'" class="entry-chip live-chip">Live</span>
              <span v-else-if="entry.status === 'interrupted'" class="entry-chip interrupted-chip">
                Interrupted
              </span>
            </div>
            <p>{{ entry.text }}</p>
          </div>
        </div>
      </article>

      <article class="panel">
        <h2>Saved Sessions</h2>
        <div class="saved-session-list">
          <p v-if="!savedSessions.length" class="empty-state">
            Save a session to build a reusable demo history.
          </p>

          <button
            v-for="savedSession in savedSessions"
            :key="savedSession.id"
            type="button"
            class="saved-session-card"
            @click="loadSavedSession(savedSession.id)"
          >
            <strong>{{ savedSession.title }}</strong>
            <span>{{ savedSession.tutorMode || 'General tutor' }}</span>
            <span>{{ formatDate(savedSession.updatedAt) }}</span>
          </button>
        </div>
      </article>
    </section>

    <section class="panel">
      <h2>Live Events</h2>
      <ul class="event-list">
        <li v-for="(entry, index) in eventLog" :key="`${entry}-${index}`">
          {{ entry }}
        </li>
      </ul>
    </section>
  </section>
</template>

<script setup lang="ts">
import { ActivityHandling, GoogleGenAI, Modality } from '@google/genai'
import type { LiveServerMessage, Session } from '@google/genai'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import {
  liveVoiceService,
  type SavedTutorSession,
  type TutorSessionPayload,
  type TutorTranscriptEntry,
} from '@/services/liveVoiceService'

type ConnectionState = 'idle' | 'connecting' | 'connected' | 'error'
type TranscriptSpeaker = 'You' | 'Socratica'
type TranscriptStatus = 'streaming' | 'final' | 'interrupted'
type ConversationPhase =
  | 'idle'
  | 'connecting'
  | 'ready'
  | 'assistant-speaking'
  | 'waiting-for-user'
  | 'user-speaking'
  | 'processing-user'
  | 'error'

interface TranscriptEntry {
  id: string
  speaker: TranscriptSpeaker
  text: string
  status: TranscriptStatus
}

interface TutorPreset {
  id: string
  label: string
  title: string
  topic: string
  learningGoal: string
  tutorMode: string
  demoNarration: string
  demoSteps: string[]
}

interface SpeechRecognitionAlternativeLike {
  transcript: string
}

interface SpeechRecognitionResultLike {
  isFinal: boolean
  length: number
  [index: number]: SpeechRecognitionAlternativeLike
}

interface SpeechRecognitionEventLike extends Event {
  resultIndex: number
  results: {
    length: number
    [index: number]: SpeechRecognitionResultLike
  }
}

interface SpeechRecognitionLike extends EventTarget {
  continuous: boolean
  interimResults: boolean
  lang: string
  onresult: ((event: SpeechRecognitionEventLike) => void) | null
  onerror: ((event: Event & { error?: string }) => void) | null
  onend: (() => void) | null
  start: () => void
  stop: () => void
}

declare global {
  interface Window {
    SpeechRecognition?: new () => SpeechRecognitionLike
    webkitSpeechRecognition?: new () => SpeechRecognitionLike
  }
}

const tutorPresets: TutorPreset[] = [
  {
    id: 'exam-oral',
    label: 'Oral exam coach',
    title: 'Biology oral exam rehearsal',
    topic: 'Explain photosynthesis clearly for an oral biology exam and challenge me whenever I skip a causal step.',
    learningGoal: 'Practice concise, structured explanations under interruption.',
    tutorMode: 'Oral exam coach',
    demoNarration:
      'This mode simulates an examiner who interrupts vague reasoning and pushes the student toward more rigorous explanations.',
    demoSteps: [
      'Open the live tutor and choose the oral exam preset.',
      'Say one intentionally vague explanation so the AI interrupts you.',
      'Answer the follow-up question with a clearer explanation.',
      'Show that the transcript and session can be saved for later review.',
    ],
  },
  {
    id: 'presentation',
    label: 'Presentation rehearsal',
    title: 'History presentation rehearsal',
    topic: 'Present the causes of the French Revolution as if you are in front of a class, and interrupt me when the structure becomes unclear.',
    learningGoal: 'Improve flow, confidence, and response under pressure.',
    tutorMode: 'Presentation rehearsal',
    demoNarration:
      'This mode acts like an engaged audience member who asks for clarification mid-presentation.',
    demoSteps: [
      'Start with a smooth opening and show natural speech input.',
      'Trigger an interruption by becoming too broad or skipping evidence.',
      'Recover by giving a sharper, more structured answer.',
      'Explain that saved transcripts help students review weak spots later.',
    ],
  },
  {
    id: 'interview',
    label: 'Behavioral interview',
    title: 'Behavioral interview practice',
    topic: 'Answer behavioral interview questions using the STAR method and challenge me when my examples are weak or incomplete.',
    learningGoal: 'Practice high-stakes spoken answers with live pushback.',
    tutorMode: 'Behavioral interview',
    demoNarration:
      'This mode turns the tutor into an interviewer who actively tests the student’s evidence, clarity, and structure.',
    demoSteps: [
      'Introduce the interview scenario and begin answering.',
      'Let the AI interrupt when your answer lacks specifics.',
      'Refine the answer in real time with more concrete detail.',
      'Save the session to show how the app supports repeated interview practice.',
    ],
  },
]

const router = useRouter()

const connectionState = ref<ConnectionState>('idle')
const conversationPhase = ref<ConversationPhase>('idle')
const statusMessage = ref('Ready to start a live voice tutoring session.')
const liveModel = ref('Waiting for backend token...')
const isListening = ref(false)
const isModelSpeaking = ref(false)
const isSaving = ref(false)
const transcriptEntries = ref<TranscriptEntry[]>([])
const transcriptStreamRef = ref<HTMLElement | null>(null)
const eventLog = ref<string[]>([
  'Pick a tutor preset, click "Start Live Voice", allow microphone access, and begin speaking.',
])
const savedSessions = ref<SavedTutorSession[]>([])
const manualUserInput = ref('')
const voiceInputBlocked = ref(false)

const selectedPresetId = ref(tutorPresets[0].id)
const sessionTitle = ref(tutorPresets[0].title)
const studyTopic = ref(tutorPresets[0].topic)
const learningGoal = ref(tutorPresets[0].learningGoal)
const tutorMode = ref(tutorPresets[0].tutorMode)
const allowInterruption = ref(false)
const activeSessionId = ref<string | undefined>(undefined)

let session: Session | null = null
let mediaStream: MediaStream | null = null
let audioContext: AudioContext | null = null
let sourceNode: MediaStreamAudioSourceNode | null = null
let processorNode: ScriptProcessorNode | null = null
let sinkNode: GainNode | null = null
let playbackCursor = 0
let activePlaybackNodes: AudioBufferSourceNode[] = []
let hasSeenServerMessage = false
let serverMessageCount = 0
let rawSocketMessageCount = 0
let rawSocketListener: ((event: MessageEvent) => void) | null = null
let speechActivityStarted = false
let silenceChunkCount = 0
let interruptionSpeechChunkCount = 0
let capturedSpeechChunks: ArrayBuffer[] = []
let speechRecognition: SpeechRecognitionLike | null = null
let speechRecognitionActive = false
let speechRecognitionEnabled = false
let currentUserInterimText = ''
let speechRecognitionFinalBuffer = ''
let speechRecognitionFinalizeTimer: ReturnType<typeof window.setTimeout> | null = null
let liveUserPreviewText = ''
let liveUserPreviewTimer: ReturnType<typeof window.setTimeout> | null = null
let liveUserPreviewInFlight = false
let liveUserPreviewSessionId = 0
let suppressSpeechRecognitionOnEnd = false
let serverWaitingForUserInput = false
let lastAssistantTurnWasQuestion = false
let userInterruptedAssistant = false
const isTranscribing = ref(false)

const SPEECH_LEVEL_THRESHOLD = 0.02
const INTERRUPTION_LEVEL_THRESHOLD = 0.03
const INTERRUPTION_CHUNKS_BEFORE_HANDOFF = 2
const SILENCE_CHUNKS_BEFORE_END_OFF = 10
const SILENCE_CHUNKS_BEFORE_END_ON = 5
const SPEECH_RECOGNITION_FINALIZE_DELAY_MS_OFF = 900
const SPEECH_RECOGNITION_FINALIZE_DELAY_MS_ON = 500

const getSilenceChunksBeforeEnd = () => (allowInterruption.value ? SILENCE_CHUNKS_BEFORE_END_ON : SILENCE_CHUNKS_BEFORE_END_OFF)
const getSpeechRecognitionFinalizeDelayMs = () =>
  allowInterruption.value ? SPEECH_RECOGNITION_FINALIZE_DELAY_MS_ON : SPEECH_RECOGNITION_FINALIZE_DELAY_MS_OFF
const LIVE_USER_PREVIEW_DELAY_MS = 1200
const LIVE_USER_PREVIEW_MIN_CHUNKS = 3

const activePreset = computed(
  () => tutorPresets.find((preset) => preset.id === selectedPresetId.value) ?? tutorPresets[0]
)
const activeDemoSteps = computed(() => activePreset.value.demoSteps)
const visibleTranscriptEntries = computed(() =>
  transcriptEntries.value.filter((entry) => entry.text.trim().length > 0)
)
const isBusy = computed(() => connectionState.value === 'connecting')
const isConnected = computed(() => connectionState.value === 'connected')
const connectionLabel = computed(() => {
  switch (connectionState.value) {
    case 'connecting':
      return 'Connecting'
    case 'connected':
      return 'Live'
    case 'error':
      return 'Error'
    default:
      return 'Idle'
  }
})

const statusClass = computed(() => ({
  idle: connectionState.value === 'idle',
  connecting: connectionState.value === 'connecting',
  connected: connectionState.value === 'connected',
  error: connectionState.value === 'error',
}))

const conversationPhaseLabel = computed(() => {
  switch (conversationPhase.value) {
    case 'connecting':
      return 'Connecting'
    case 'assistant-speaking':
      return 'Socratica speaking'
    case 'waiting-for-user':
      return 'Waiting for you'
    case 'user-speaking':
      return 'Listening'
    case 'processing-user':
      return 'Processing'
    case 'error':
      return 'Error'
    case 'ready':
      return 'Live and ready'
    default:
      return 'Idle'
  }
})

const conversationTurnOwner = computed(() => {
  switch (conversationPhase.value) {
    case 'assistant-speaking':
      return 'Socratica'
    case 'waiting-for-user':
    case 'user-speaking':
      return 'You'
    case 'processing-user':
      return 'Gemini'
    default:
      return 'Shared'
  }
})

const conversationGuidance = computed(() => {
  switch (conversationPhase.value) {
    case 'connecting':
      return 'Setting up the live socket, microphone, and tutor instructions.'
    case 'assistant-speaking':
      return 'Socratica has the floor. Start speaking to interrupt cleanly if you want to jump in.'
    case 'waiting-for-user':
      return lastAssistantTurnWasQuestion
        ? 'Socratica asked a question and is waiting for your answer.'
        : 'It is your turn. Speak naturally or type a reply below.'
    case 'user-speaking':
      return 'Your response is being captured live and added to the transcript.'
    case 'processing-user':
      return 'Your last turn was captured. Socratica is preparing the next response.'
    case 'error':
      return 'The session hit an error. Stop and restart the live voice session.'
    case 'ready':
      return 'The session is live and ready for a natural back-and-forth conversation.'
    default:
      return 'Start a live session to see the full conversation here.'
  }
})

const phaseToneClass = computed(() => ({
  idle: conversationPhase.value === 'idle',
  connecting: conversationPhase.value === 'connecting',
  ready: conversationPhase.value === 'ready',
  'assistant-speaking': conversationPhase.value === 'assistant-speaking',
  'waiting-for-user': conversationPhase.value === 'waiting-for-user',
  'user-speaking': conversationPhase.value === 'user-speaking',
  'processing-user': conversationPhase.value === 'processing-user',
  error: conversationPhase.value === 'error',
}))

const applyPreset = () => {
  const preset = activePreset.value
  sessionTitle.value = preset.title
  studyTopic.value = preset.topic
  learningGoal.value = preset.learningGoal
  tutorMode.value = preset.tutorMode
  statusMessage.value = `Preset loaded: ${preset.label}.`
}

const addEvent = (message: string) => {
  const timestamp = new Date().toLocaleTimeString()
  eventLog.value = [`${timestamp} - ${message}`, ...eventLog.value].slice(0, 20)
}

const buildTranscriptId = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }

  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

const normalizeTranscriptText = (text: string) => text.replace(/\s+/g, ' ').trim()

const mergeTranscriptText = (existingText: string, incomingText: string) => {
  const existing = normalizeTranscriptText(existingText)
  const incoming = normalizeTranscriptText(incomingText)

  if (!existing) {
    return incoming
  }

  if (!incoming) {
    return existing
  }

  if (existing === incoming) {
    return existing
  }

  if (incoming.startsWith(existing)) {
    return incoming
  }

  if (existing.startsWith(incoming)) {
    return existing
  }

  let overlapLength = Math.min(existing.length, incoming.length)
  while (overlapLength > 0) {
    if (existing.slice(-overlapLength) === incoming.slice(0, overlapLength)) {
      return normalizeTranscriptText(`${existing}${incoming.slice(overlapLength)}`)
    }
    overlapLength -= 1
  }

  return normalizeTranscriptText(`${existing} ${incoming}`)
}

const findLatestTranscriptIndex = (speaker: TranscriptSpeaker, status?: TranscriptStatus) => {
  for (let index = transcriptEntries.value.length - 1; index >= 0; index -= 1) {
    const entry = transcriptEntries.value[index]
    if (entry.speaker === speaker && (!status || entry.status === status)) {
      return index
    }
  }

  return -1
}

const getStreamingTranscriptEntry = (speaker: TranscriptSpeaker) => {
  const index = findLatestTranscriptIndex(speaker, 'streaming')
  return index >= 0 ? transcriptEntries.value[index] : null
}

const clearStreamingTranscriptEntry = (speaker: TranscriptSpeaker) => {
  const index = findLatestTranscriptIndex(speaker, 'streaming')
  if (index >= 0) {
    transcriptEntries.value.splice(index, 1)
  }
}

const closeStreamingEntriesForSpeakerChange = (nextSpeaker: TranscriptSpeaker) => {
  transcriptEntries.value = transcriptEntries.value.flatMap((entry) => {
    if (entry.status !== 'streaming' || entry.speaker === nextSpeaker) {
      return [entry]
    }

    const normalized = normalizeTranscriptText(entry.text)
    if (!normalized) {
      return []
    }

    return [
      {
        ...entry,
        text: normalized,
        status:
          entry.speaker === 'Socratica' && nextSpeaker === 'You' && userInterruptedAssistant
            ? 'interrupted'
            : 'final',
      },
    ]
  })
}

const upsertStreamingTranscriptEntry = (speaker: TranscriptSpeaker, text: string) => {
  const normalized = normalizeTranscriptText(text)
  if (!normalized) {
    clearStreamingTranscriptEntry(speaker)
    return
  }

  closeStreamingEntriesForSpeakerChange(speaker)

  const entry = getStreamingTranscriptEntry(speaker)
  if (entry) {
    entry.text = mergeTranscriptText(entry.text, normalized)
    return
  }

  transcriptEntries.value.push({
    id: buildTranscriptId(),
    speaker,
    text: normalized,
    status: 'streaming',
  })
}

const finalizeTranscriptEntry = (
  speaker: TranscriptSpeaker,
  text: string,
  status: Exclude<TranscriptStatus, 'streaming'> = 'final'
) => {
  const normalized = normalizeTranscriptText(text)
  if (!normalized) {
    clearStreamingTranscriptEntry(speaker)
    return
  }

  const streamingEntry = getStreamingTranscriptEntry(speaker)
  if (streamingEntry) {
    streamingEntry.text = mergeTranscriptText(streamingEntry.text, normalized)
    streamingEntry.status = status
    return
  }

  const previous = transcriptEntries.value[transcriptEntries.value.length - 1]
  if (
    previous &&
    previous.speaker === speaker &&
    previous.text === normalized &&
    previous.status === status
  ) {
    return
  }

  transcriptEntries.value.push({
    id: buildTranscriptId(),
    speaker,
    text: normalized,
    status,
  })
}

const looksLikeQuestion = (text: string) => /\?\s*$/.test(text.trim())

const syncConversationPhase = (preferredMessage?: string) => {
  if (connectionState.value === 'error') {
    conversationPhase.value = 'error'
    if (preferredMessage) {
      statusMessage.value = preferredMessage
    }
    return
  }

  if (connectionState.value === 'connecting') {
    conversationPhase.value = 'connecting'
    statusMessage.value = preferredMessage ?? 'Connecting to Gemini Live...'
    return
  }

  if (connectionState.value !== 'connected') {
    conversationPhase.value = 'idle'
    statusMessage.value = preferredMessage ?? 'Ready to start a live voice tutoring session.'
    return
  }

  if (isTranscribing.value) {
    conversationPhase.value = 'processing-user'
    statusMessage.value = preferredMessage ?? 'Processing what you said...'
    return
  }

  if (speechActivityStarted || isListening.value) {
    conversationPhase.value = 'user-speaking'
    statusMessage.value = preferredMessage ?? 'Listening to your response...'
    return
  }

  if (isModelSpeaking.value) {
    conversationPhase.value = 'assistant-speaking'
    statusMessage.value = preferredMessage ?? 'Socratica is speaking...'
    return
  }

  if (serverWaitingForUserInput) {
    conversationPhase.value = 'waiting-for-user'
    statusMessage.value =
      preferredMessage ??
      (lastAssistantTurnWasQuestion
        ? 'Socratica asked a question and is waiting for your answer.'
        : 'Socratica is waiting for your next response.')
    return
  }

  conversationPhase.value = 'ready'
  statusMessage.value = preferredMessage ?? 'Live session ready for a natural back-and-forth.'
}

const finalizeOpenTranscriptEntries = () => {
  transcriptEntries.value = transcriptEntries.value.flatMap((entry) => {
    const normalized = normalizeTranscriptText(entry.text)
    if (!normalized) {
      return []
    }

    if (entry.status === 'streaming') {
      return [
        {
          ...entry,
          text: normalized,
          status: entry.speaker === 'Socratica' ? 'interrupted' : 'final',
        },
      ]
    }

    return [{ ...entry, text: normalized }]
  })
}

const markModelTranscriptInterrupted = () => {
  const entry = getStreamingTranscriptEntry('Socratica')
  if (!entry || !normalizeTranscriptText(entry.text)) {
    return
  }

  entry.text = normalizeTranscriptText(entry.text)
  entry.status = 'interrupted'
  lastAssistantTurnWasQuestion = false
}

const interruptModelForUserSpeech = (message: string) => {
  if (!isModelSpeaking.value && !getStreamingTranscriptEntry('Socratica')) {
    return
  }

  userInterruptedAssistant = true
  clearPlaybackQueue()
  markModelTranscriptInterrupted()
  syncConversationPhase(message)
}

const getCurrentUserStreamingText = () => {
  let combinedText = ''
  combinedText = mergeTranscriptText(combinedText, liveUserPreviewText)
  combinedText = mergeTranscriptText(combinedText, speechRecognitionFinalBuffer)
  combinedText = mergeTranscriptText(combinedText, currentUserInterimText)
  return combinedText
}

const updateUserStreamingTranscript = (interimText: string) => {
  currentUserInterimText = normalizeTranscriptText(interimText)
  const combinedText = getCurrentUserStreamingText()
  if (combinedText) {
    upsertStreamingTranscriptEntry('You', combinedText)
  } else {
    clearStreamingTranscriptEntry('You')
  }
}

const clearSpeechRecognitionFinalizeTimer = () => {
  if (speechRecognitionFinalizeTimer) {
    window.clearTimeout(speechRecognitionFinalizeTimer)
    speechRecognitionFinalizeTimer = null
  }
}

const clearLiveUserPreviewTimer = () => {
  if (liveUserPreviewTimer) {
    window.clearTimeout(liveUserPreviewTimer)
    liveUserPreviewTimer = null
  }
}

const flushSpeechRecognitionUtterance = () => {
  clearSpeechRecognitionFinalizeTimer()
  clearLiveUserPreviewTimer()
  const finalText = normalizeTranscriptText(getCurrentUserStreamingText())
  currentUserInterimText = ''
  speechRecognitionFinalBuffer = ''
  liveUserPreviewText = ''

  if (!finalText) {
    clearStreamingTranscriptEntry('You')
    syncConversationPhase()
    return
  }

  clearStreamingTranscriptEntry('You')
  sendRecognizedUserText(finalText)
}

const scheduleSpeechRecognitionFinalize = () => {
  clearSpeechRecognitionFinalizeTimer()
  speechRecognitionFinalizeTimer = window.setTimeout(() => {
    flushSpeechRecognitionUtterance()
  }, getSpeechRecognitionFinalizeDelayMs())
}

const transcribeLiveUserPreview = async (sessionId: number) => {
  if (
    liveUserPreviewInFlight ||
    sessionId !== liveUserPreviewSessionId ||
    !speechActivityStarted ||
    isModelSpeaking.value ||
    capturedSpeechChunks.length < LIVE_USER_PREVIEW_MIN_CHUNKS
  ) {
    return
  }

  liveUserPreviewInFlight = true
  const previewBlob = createWavBlobFromPcmChunks([...capturedSpeechChunks], 16000)

  try {
    const transcript = normalizeTranscriptText(await liveVoiceService.transcribeAudio(previewBlob))
    if (
      sessionId !== liveUserPreviewSessionId ||
      !speechActivityStarted ||
      isModelSpeaking.value ||
      !transcript
    ) {
      return
    }

    liveUserPreviewText = mergeTranscriptText(liveUserPreviewText, transcript)
    updateUserStreamingTranscript(currentUserInterimText)
  } catch {
    // Ignore preview transcription failures and rely on final transcription instead.
  } finally {
    liveUserPreviewInFlight = false
    if (speechActivityStarted && sessionId === liveUserPreviewSessionId && !isModelSpeaking.value) {
      scheduleLiveUserPreview()
    }
  }
}

const scheduleLiveUserPreview = () => {
  if (liveUserPreviewTimer || liveUserPreviewInFlight || isModelSpeaking.value || !speechActivityStarted) {
    return
  }

  const sessionId = liveUserPreviewSessionId
  liveUserPreviewTimer = window.setTimeout(() => {
    liveUserPreviewTimer = null
    void transcribeLiveUserPreview(sessionId)
  }, LIVE_USER_PREVIEW_DELAY_MS)
}

const pauseSpeechRecognitionCapture = () => {
  if (!speechRecognition) {
    return
  }

  speechRecognitionEnabled = false
  isListening.value = false
  clearSpeechRecognitionFinalizeTimer()
  clearLiveUserPreviewTimer()
  currentUserInterimText = ''
  speechRecognitionFinalBuffer = ''
  liveUserPreviewText = ''
  clearStreamingTranscriptEntry('You')

  if (speechRecognitionActive) {
    suppressSpeechRecognitionOnEnd = true
    speechRecognition.stop()
    speechRecognitionActive = false
  }
}

const resumeSpeechRecognitionCapture = () => {
  if (connectionState.value !== 'connected' || isModelSpeaking.value || voiceInputBlocked.value) {
    return false
  }

  return startSpeechRecognition()
}

const scrollTranscriptToBottom = async () => {
  await nextTick()
  if (!transcriptStreamRef.value) {
    return
  }

  transcriptStreamRef.value.scrollTop = transcriptStreamRef.value.scrollHeight
}

watch(
  () => visibleTranscriptEntries.value.map((entry) => `${entry.id}:${entry.status}:${entry.text}`).join('\n'),
  () => {
    void scrollTranscriptToBottom()
  }
)

const sendRecognizedUserText = (text: string) => {
  const trimmed = normalizeTranscriptText(text)
  if (!trimmed || !session || connectionState.value !== 'connected') {
    return
  }

  if (isModelSpeaking.value) {
    interruptModelForUserSpeech('You interrupted Socratica. Keep going.')
  }

  closeStreamingEntriesForSpeakerChange('You')
  clearSpeechRecognitionFinalizeTimer()
  clearLiveUserPreviewTimer()
  currentUserInterimText = ''
  speechRecognitionFinalBuffer = ''
  liveUserPreviewText = ''
  finalizeTranscriptEntry('You', trimmed, 'final')
  clearStreamingTranscriptEntry('You')
  serverWaitingForUserInput = false
  userInterruptedAssistant = false
  syncConversationPhase('Socratica is thinking about your response...')
  addEvent(`Captured user turn: "${trimmed}"`)

  session.sendClientContent({
    turns: [
      {
        role: 'user',
        parts: [{ text: trimmed }],
      },
    ],
    turnComplete: true,
  })
}

const transcribeCapturedSpeech = async () => {
  if (speechRecognitionEnabled) {
    capturedSpeechChunks = []
    return
  }

  if (!capturedSpeechChunks.length || isTranscribing.value) {
    capturedSpeechChunks = []
    return
  }

  const audioBlob = createWavBlobFromPcmChunks(capturedSpeechChunks, 16000)
  capturedSpeechChunks = []
  isTranscribing.value = true
  syncConversationPhase('Transcribing your microphone input...')
  addEvent('Uploading captured speech for backend transcription.')

  try {
    const transcript = await liveVoiceService.transcribeAudio(audioBlob)

    if (!normalizeTranscriptText(transcript)) {
      syncConversationPhase('I did not catch that. Please try again.')
      addEvent('No speech recognized from the captured audio.')
      return
    }

    addEvent(`Backend transcription: "${normalizeTranscriptText(transcript)}"`)
    sendRecognizedUserText(transcript)
  } catch (error) {
    console.error('Failed to transcribe captured speech:', error)
    syncConversationPhase('Could not transcribe your speech.')
    addEvent('Backend transcription failed.')
  } finally {
    isTranscribing.value = false
    syncConversationPhase()
  }
}

const submitManualUserInput = () => {
  if (!manualUserInput.value.trim()) {
    return
  }

  sendRecognizedUserText(manualUserInput.value)
  manualUserInput.value = ''
}

const clearTranscript = () => {
  transcriptEntries.value = []
  manualUserInput.value = ''
  activeSessionId.value = undefined
}

const clearPlaybackQueue = () => {
  activePlaybackNodes.forEach((node) => {
    try {
      node.stop()
    } catch {
      // Ignore nodes that already ended.
    }
  })

  activePlaybackNodes = []
  playbackCursor = audioContext ? audioContext.currentTime : 0
  isModelSpeaking.value = false
}

const ensureAudioContext = async () => {
  if (!audioContext) {
    audioContext = new AudioContext()
  }

  if (audioContext.state === 'suspended') {
    await audioContext.resume()
  }

  return audioContext
}

const createWavBlobFromPcmChunks = (chunks: ArrayBuffer[], sampleRate: number): Blob => {
  const totalPcmBytes = chunks.reduce((sum, chunk) => sum + chunk.byteLength, 0)
  const wavBuffer = new ArrayBuffer(44 + totalPcmBytes)
  const view = new DataView(wavBuffer)

  const writeAscii = (offset: number, value: string) => {
    for (let index = 0; index < value.length; index += 1) {
      view.setUint8(offset + index, value.charCodeAt(index))
    }
  }

  writeAscii(0, 'RIFF')
  view.setUint32(4, 36 + totalPcmBytes, true)
  writeAscii(8, 'WAVE')
  writeAscii(12, 'fmt ')
  view.setUint32(16, 16, true)
  view.setUint16(20, 1, true)
  view.setUint16(22, 1, true)
  view.setUint32(24, sampleRate, true)
  view.setUint32(28, sampleRate * 2, true)
  view.setUint16(32, 2, true)
  view.setUint16(34, 16, true)
  writeAscii(36, 'data')
  view.setUint32(40, totalPcmBytes, true)

  let offset = 44
  for (const chunk of chunks) {
    new Uint8Array(wavBuffer, offset, chunk.byteLength).set(new Uint8Array(chunk))
    offset += chunk.byteLength
  }

  return new Blob([wavBuffer], { type: 'audio/wav' })
}

const decodeBase64ToArrayBuffer = (base64: string): ArrayBuffer => {
  const binary = window.atob(base64)
  const bytes = new Uint8Array(binary.length)

  for (let index = 0; index < binary.length; index += 1) {
    bytes[index] = binary.charCodeAt(index)
  }

  return bytes.buffer
}

const pcm16ToFloat32 = (buffer: ArrayBuffer): Float32Array => {
  const pcmView = new DataView(buffer)
  const samples = new Float32Array(buffer.byteLength / 2)

  for (let index = 0; index < samples.length; index += 1) {
    const value = pcmView.getInt16(index * 2, true)
    samples[index] = value / 32768
  }

  return samples
}

const enqueueAudioChunk = async (base64Audio: string) => {
  const playbackContext = await ensureAudioContext()

  const floatSamples = pcm16ToFloat32(decodeBase64ToArrayBuffer(base64Audio))
  if (!floatSamples.length) {
    return
  }

  const buffer = playbackContext.createBuffer(1, floatSamples.length, 24000)
  buffer.copyToChannel(floatSamples, 0)

  const source = playbackContext.createBufferSource()
  source.buffer = buffer
  source.connect(playbackContext.destination)

  const now = playbackContext.currentTime
  userInterruptedAssistant = false
  serverWaitingForUserInput = false
  pauseSpeechRecognitionCapture()
  playbackCursor = Math.max(playbackCursor, now + 0.02)
  source.start(playbackCursor)
  playbackCursor += buffer.duration
  isModelSpeaking.value = true
  syncConversationPhase('Socratica is speaking...')
  activePlaybackNodes.push(source)

  source.onended = () => {
    activePlaybackNodes = activePlaybackNodes.filter((node) => node !== source)
    if (!activePlaybackNodes.length && audioContext) {
      playbackCursor = audioContext.currentTime
      isModelSpeaking.value = false
      resumeSpeechRecognitionCapture()
      syncConversationPhase()
    }
  }
}

const downsampleTo16k = (input: Float32Array, inputSampleRate: number): Float32Array => {
  if (inputSampleRate === 16000) {
    return input
  }

  const ratio = inputSampleRate / 16000
  const outputLength = Math.max(1, Math.round(input.length / ratio))
  const output = new Float32Array(outputLength)
  let outputIndex = 0
  let inputIndex = 0

  while (outputIndex < outputLength) {
    const nextInputIndex = Math.round((outputIndex + 1) * ratio)
    let sample = 0
    let count = 0

    for (let index = inputIndex; index < nextInputIndex && index < input.length; index += 1) {
      sample += input[index]
      count += 1
    }

    output[outputIndex] = count > 0 ? sample / count : 0
    outputIndex += 1
    inputIndex = nextInputIndex
  }

  return output
}

const float32ToPcm16 = (input: Float32Array): ArrayBuffer => {
  const buffer = new ArrayBuffer(input.length * 2)
  const view = new DataView(buffer)

  for (let index = 0; index < input.length; index += 1) {
    const clamped = Math.max(-1, Math.min(1, input[index]))
    view.setInt16(index * 2, clamped < 0 ? clamped * 32768 : clamped * 32767, true)
  }

  return buffer
}

const calculateLevel = (input: Float32Array): number => {
  let sum = 0
  for (let index = 0; index < input.length; index += 1) {
    sum += input[index] * input[index]
  }

  return Math.sqrt(sum / input.length)
}

const normalizeServerMessage = (payload: unknown): LiveServerMessage | null => {
  if (!payload) {
    return null
  }

  if (typeof payload === 'string') {
    try {
      return JSON.parse(payload) as LiveServerMessage
    } catch {
      return null
    }
  }

  if (payload instanceof MessageEvent) {
    return normalizeServerMessage(payload.data)
  }

  if (
    typeof payload === 'object' &&
    payload !== null &&
    'data' in payload &&
    !('serverContent' in payload) &&
    !('setupComplete' in payload)
  ) {
    return normalizeServerMessage((payload as { data: unknown }).data)
  }

  return payload as LiveServerMessage
}

const handleServerMessage = async (payload: unknown) => {
  const message = normalizeServerMessage(payload)
  if (!message) {
    addEvent('Received an unreadable Gemini server payload.')
    return
  }

  serverMessageCount += 1
  if (serverMessageCount <= 5) {
    addEvent(`Gemini message #${serverMessageCount}: ${Object.keys(message).join(', ') || 'none'}`)
  }

  if (!hasSeenServerMessage) {
    hasSeenServerMessage = true
    addEvent('Received first Gemini server message.')
    addEvent(`First Gemini message keys: ${Object.keys(message).join(', ') || 'none'}`)
  }

  if (message.setupComplete) {
    addEvent('Gemini Live socket is ready.')
  }

  const outputTranscription = message.serverContent?.outputTranscription ?? message.outputTranscription
  const serverContent = message.serverContent

  if (serverContent?.interrupted) {
    clearPlaybackQueue()
    markModelTranscriptInterrupted()
    addEvent('Model response interrupted by new user activity.')
    syncConversationPhase('The turn shifted back to you.')
  }

  const outputText = normalizeTranscriptText(outputTranscription?.text ?? '')

  if (outputText) {
    upsertStreamingTranscriptEntry('Socratica', outputText)
    syncConversationPhase('Socratica is speaking...')
  }

  if (outputTranscription?.finished) {
    const finalizedOutputText = outputText || getStreamingTranscriptEntry('Socratica')?.text || ''
    finalizeTranscriptEntry('Socratica', finalizedOutputText, 'final')
    lastAssistantTurnWasQuestion = looksLikeQuestion(finalizedOutputText)
  }

  const parts = serverContent?.modelTurn?.parts ?? []
  for (const part of parts) {
    if (part.inlineData?.data) {
      await enqueueAudioChunk(part.inlineData.data)
    }
  }

  if (!serverContent) {
    return
  }

  if (serverContent.waitingForInput) {
    serverWaitingForUserInput = true
    resumeSpeechRecognitionCapture()
    syncConversationPhase()
  } else if (parts.length || outputText) {
    serverWaitingForUserInput = false
  } else if (serverContent.generationComplete) {
    serverWaitingForUserInput = lastAssistantTurnWasQuestion
    if (serverWaitingForUserInput || !isModelSpeaking.value) {
      resumeSpeechRecognitionCapture()
    }
    syncConversationPhase()
  }
}

const startMicrophone = async () => {
  if (!navigator.mediaDevices?.getUserMedia) {
    throw new Error('This browser does not support microphone streaming.')
  }

  mediaStream = await navigator.mediaDevices.getUserMedia({
    audio: {
      channelCount: 1,
      echoCancellation: true,
      noiseSuppression: true,
      autoGainControl: true,
    },
  })

  audioContext = new AudioContext()
  await audioContext.resume()

  sourceNode = audioContext.createMediaStreamSource(mediaStream)
  processorNode = audioContext.createScriptProcessor(4096, 1, 1)
  sinkNode = audioContext.createGain()
  sinkNode.gain.value = 0

  sourceNode.connect(processorNode)
  processorNode.connect(sinkNode)
  sinkNode.connect(audioContext.destination)

  processorNode.onaudioprocess = (event) => {
    if (!session || connectionState.value !== 'connected' || !audioContext) {
      return
    }

    const inputSamples = event.inputBuffer.getChannelData(0)
    const level = calculateLevel(inputSamples)
    const downsampled = downsampleTo16k(inputSamples, audioContext.sampleRate)
    const pcm16Buffer = float32ToPcm16(downsampled)

    if (isModelSpeaking.value && !userInterruptedAssistant) {
      if (level > INTERRUPTION_LEVEL_THRESHOLD) {
        interruptionSpeechChunkCount += 1
        if (interruptionSpeechChunkCount >= INTERRUPTION_CHUNKS_BEFORE_HANDOFF) {
          addEvent('User speech detected while Socratica was speaking. Handing over the turn.')
          liveUserPreviewSessionId += 1
          clearLiveUserPreviewTimer()
          liveUserPreviewText = ''
          currentUserInterimText = ''
          speechRecognitionFinalBuffer = ''
          capturedSpeechChunks = [pcm16Buffer]
          speechActivityStarted = true
          silenceChunkCount = 0
          isListening.value = true
          interruptModelForUserSpeech('You interrupted Socratica. Finish your thought.')
          interruptionSpeechChunkCount = 0
        }
      } else {
        interruptionSpeechChunkCount = 0
      }

      return
    }

    if (level > SPEECH_LEVEL_THRESHOLD) {
      interruptionSpeechChunkCount = 0

      if (!speechActivityStarted) {
        liveUserPreviewSessionId += 1
        clearLiveUserPreviewTimer()
        liveUserPreviewText = ''
        currentUserInterimText = ''
        if (!speechRecognitionEnabled) {
          speechRecognitionFinalBuffer = ''
        }
        capturedSpeechChunks = []
      }
      speechActivityStarted = true
      silenceChunkCount = 0
      isListening.value = true
      syncConversationPhase('Listening to your response...')
      capturedSpeechChunks.push(pcm16Buffer)
      scheduleLiveUserPreview()
      return
    }

    interruptionSpeechChunkCount = 0

    if (speechActivityStarted) {
      silenceChunkCount += 1
      capturedSpeechChunks.push(pcm16Buffer)

      if (silenceChunkCount >= getSilenceChunksBeforeEnd()) {
        speechActivityStarted = false
        silenceChunkCount = 0
        isListening.value = false
        if (speechRecognitionEnabled) {
          if (getCurrentUserStreamingText()) {
            scheduleSpeechRecognitionFinalize()
          } else {
            capturedSpeechChunks = []
            syncConversationPhase()
          }
        } else {
          void transcribeCapturedSpeech()
        }
      }
      return
    }

    isListening.value = false
    syncConversationPhase()
  }
}

const stopAudioPipeline = async () => {
  clearPlaybackQueue()

  if (processorNode) {
    processorNode.disconnect()
    processorNode.onaudioprocess = null
    processorNode = null
  }

  if (sourceNode) {
    sourceNode.disconnect()
    sourceNode = null
  }

  if (sinkNode) {
    sinkNode.disconnect()
    sinkNode = null
  }

  if (mediaStream) {
    mediaStream.getTracks().forEach((track) => track.stop())
    mediaStream = null
  }

  if (audioContext) {
    await audioContext.close()
    audioContext = null
  }

  capturedSpeechChunks = []
  speechActivityStarted = false
  silenceChunkCount = 0
  interruptionSpeechChunkCount = 0
  clearSpeechRecognitionFinalizeTimer()
  speechRecognitionFinalBuffer = ''
  serverWaitingForUserInput = false
  userInterruptedAssistant = false
  isListening.value = false
  isModelSpeaking.value = false
}

const detachRawSocketListener = () => {
  const rawSocket = (session as { conn?: { ws?: WebSocket } } | null)?.conn?.ws
  if (rawSocket && rawSocketListener) {
    rawSocket.removeEventListener('message', rawSocketListener)
  }

  rawSocketListener = null
}

const stopSpeechRecognition = () => {
  suppressSpeechRecognitionOnEnd = false
  speechRecognitionEnabled = false
  speechRecognitionActive = false
  voiceInputBlocked.value = false
  isListening.value = false
  clearSpeechRecognitionFinalizeTimer()
  clearLiveUserPreviewTimer()
  currentUserInterimText = ''
  speechRecognitionFinalBuffer = ''
  liveUserPreviewText = ''
  clearStreamingTranscriptEntry('You')
  if (speechRecognition) {
    speechRecognition.stop()
  }
}

const startSpeechRecognition = (): boolean => {
  const RecognitionCtor = window.SpeechRecognition ?? window.webkitSpeechRecognition
  if (!RecognitionCtor) {
    voiceInputBlocked.value = true
    addEvent('Browser speech recognition is not supported here.')
    return false
  }

  if (!speechRecognition) {
    speechRecognition = new RecognitionCtor()
    speechRecognition.continuous = true
    speechRecognition.interimResults = true
    speechRecognition.lang = 'en-US'

    speechRecognition.onresult = (event) => {
      if (isModelSpeaking.value && !userInterruptedAssistant) {
        return
      }

      let interimTranscript = ''
      let finalTranscriptDelta = ''

      for (let index = event.resultIndex; index < event.results.length; index += 1) {
        const result = event.results[index]
        const transcript = result[0]?.transcript?.trim()
        if (!transcript) {
          continue
        }

        if (result.isFinal) {
          finalTranscriptDelta += `${transcript} `
        } else {
          interimTranscript += `${transcript} `
        }
      }

      const normalizedFinalDelta = normalizeTranscriptText(finalTranscriptDelta)
      const normalizedInterim = normalizeTranscriptText(interimTranscript)

      if (normalizedFinalDelta) {
        speechRecognitionFinalBuffer = mergeTranscriptText(
          speechRecognitionFinalBuffer,
          normalizedFinalDelta
        )
        scheduleSpeechRecognitionFinalize()
      }

      updateUserStreamingTranscript(normalizedInterim)
      isListening.value = normalizedInterim.length > 0 || Boolean(speechRecognitionFinalBuffer)
      syncConversationPhase()
    }

    speechRecognition.onerror = (event) => {
      addEvent(`Browser speech recognition error${event.error ? `: ${event.error}` : '.'}`)
      if (event.error === 'service-not-allowed' || event.error === 'not-allowed') {
        speechRecognitionEnabled = false
        voiceInputBlocked.value = true
        statusMessage.value = 'This browser blocked local speech recognition. Try Chrome or Edge for voice transcript input.'
      }
      syncConversationPhase()
    }

    speechRecognition.onend = () => {
      const shouldSuppress = suppressSpeechRecognitionOnEnd
      suppressSpeechRecognitionOnEnd = false
      speechRecognitionActive = false
      isListening.value = false
      if (shouldSuppress) {
        clearSpeechRecognitionFinalizeTimer()
        speechRecognitionFinalBuffer = ''
        clearStreamingTranscriptEntry('You')
      } else {
        flushSpeechRecognitionUtterance()
      }
      if (speechRecognitionEnabled && connectionState.value === 'connected' && !isModelSpeaking.value) {
        try {
          speechRecognition?.start()
          speechRecognitionActive = true
        } catch {
          // Ignore browser restart timing issues.
        }
      }
      syncConversationPhase()
    }
  }

  speechRecognitionEnabled = true
  voiceInputBlocked.value = false
  suppressSpeechRecognitionOnEnd = false
  if (!speechRecognitionActive) {
    try {
      speechRecognition.start()
      speechRecognitionActive = true
      addEvent('Browser speech recognition started.')
    } catch (error) {
      speechRecognitionEnabled = false
      addEvent(
        `Browser speech recognition could not start${error instanceof Error ? `: ${error.message}` : '.'}`
      )
      return false
    }
  }

  return true
}

const buildTutorPrompt = () => {
  const base = [
    'You are Socratica, a warm spoken Socratic tutor for a live hackathon demo.',
    `Tutor mode: ${tutorMode.value}.`,
    `Student topic: ${studyTopic.value.trim()}.`,
    `Learning goal: ${learningGoal.value.trim()}.`,
    'Speak naturally, use short turns, and avoid sounding robotic or scripted.',
    'Ask only one question at a time. When you ask a direct question, stop and wait for the student to answer before continuing.',
    'Do not fill silence by answering your own question or moving on too early.',
    'If the student interrupts you, stop gracefully, acknowledge the interruption naturally, and continue from what they said next.',
    'Challenge vague reasoning politely whenever they skip a causal link or make an unsupported claim.',
    'Keep most spoken responses to one to three concise sentences unless the student explicitly asks for more detail.',
  ]
  if (allowInterruption.value) {
    base.push(
      'You may interrupt the student after brief pauses when something is unclear or when probing deeper would help—do not wait for long silence before responding.'
    )
  } else {
    base.push(
      'Wait for the student to finish their full thought before responding. Do not interrupt or jump in during brief pauses.'
    )
  }
  return base.join(' ')
}

const loadSavedSessions = async () => {
  try {
    savedSessions.value = await liveVoiceService.listTutorSessions()
  } catch (error) {
    console.error('Failed to load saved sessions:', error)
    addEvent('Could not load saved tutor sessions.')
  }
}

const saveCurrentSession = async () => {
  finalizeOpenTranscriptEntries()
  const savableEntries = transcriptEntries.value.filter((entry) => normalizeTranscriptText(entry.text))

  if (!savableEntries.length) {
    statusMessage.value = 'Say a few things first so there is something to save.'
    return
  }

  isSaving.value = true
  try {
    const payload: TutorSessionPayload = {
      sessionId: activeSessionId.value,
      title: sessionTitle.value.trim() || activePreset.value.title,
      topic: studyTopic.value.trim(),
      learningGoal: learningGoal.value.trim(),
      tutorMode: tutorMode.value.trim(),
      demoScript: activePreset.value.demoNarration,
      transcriptEntries: savableEntries.map<TutorTranscriptEntry>((entry) => ({
        speaker: entry.speaker,
        text: entry.text,
      })),
    }

    const savedSession = await liveVoiceService.saveTutorSession(payload)
    activeSessionId.value = savedSession.id
    statusMessage.value = 'Session saved to Mongo history.'
    addEvent(`Saved session "${savedSession.title}".`)
    await loadSavedSessions()
  } catch (error) {
    console.error('Failed to save session:', error)
    statusMessage.value = 'Failed to save session history.'
    addEvent('Could not save session history.')
  } finally {
    isSaving.value = false
  }
}

const loadSavedSession = async (sessionId: string) => {
  try {
    const savedSession = await liveVoiceService.getTutorSession(sessionId)
    activeSessionId.value = savedSession.id
    sessionTitle.value = savedSession.title
    studyTopic.value = savedSession.topic
    learningGoal.value = savedSession.learningGoal ?? ''
    tutorMode.value = savedSession.tutorMode ?? activePreset.value.tutorMode
    transcriptEntries.value = savedSession.transcriptEntries.map((entry) => ({
      id: buildTranscriptId(),
      speaker: entry.speaker === 'Socratica' ? 'Socratica' : 'You',
      text: entry.text,
      status: 'final',
    }))
    serverWaitingForUserInput = false
    statusMessage.value = `Loaded saved session "${savedSession.title}".`
    addEvent(`Loaded saved session "${savedSession.title}".`)
  } catch (error) {
    console.error('Failed to load saved session:', error)
    addEvent('Could not load the selected saved session.')
  }
}

const startLiveSession = async () => {
  if (isBusy.value || isConnected.value) {
    return
  }

  connectionState.value = 'connecting'
  conversationPhase.value = 'connecting'
  statusMessage.value = 'Requesting a short-lived Gemini Live token...'
  transcriptEntries.value = []
  liveModel.value = 'Resolving model...'
  serverWaitingForUserInput = false
  userInterruptedAssistant = false
  lastAssistantTurnWasQuestion = false

  try {
    hasSeenServerMessage = false
    serverMessageCount = 0
    rawSocketMessageCount = 0
    const tokenResponse = await liveVoiceService.createSessionToken()
    liveModel.value = tokenResponse.model
    statusMessage.value = 'Opening Live API socket...'

    const ai = new GoogleGenAI({
      apiKey: tokenResponse.token,
      apiVersion: 'v1alpha',
      httpOptions: {
        apiVersion: 'v1alpha',
      },
    })

    session = await ai.live.connect({
      model: tokenResponse.model,
      config: {
        responseModalities: [Modality.AUDIO],
        systemInstruction: buildTutorPrompt(),
        realtimeInputConfig: {
          activityHandling: ActivityHandling.START_OF_ACTIVITY_INTERRUPTS,
        },
        inputAudioTranscription: {},
        outputAudioTranscription: {},
      },
      callbacks: {
        onopen: () => {
          addEvent('WebSocket connected.')
        },
        onmessage: (message) => {
          void handleServerMessage(message)
        },
        onerror: (event) => {
          console.error('Gemini Live error:', event)
          connectionState.value = 'error'
          conversationPhase.value = 'error'
          statusMessage.value = 'Gemini Live reported an error.'
          addEvent(
            `Gemini Live error received from the browser client${'message' in event && typeof event.message === 'string' && event.message ? `: ${event.message}` : '.'}`
          )
        },
        onclose: (event) => {
          addEvent(`Gemini Live session closed (code ${event.code}${event.reason ? `: ${event.reason}` : ''}).`)
          if (connectionState.value === 'connected') {
            connectionState.value = 'idle'
            conversationPhase.value = 'idle'
            statusMessage.value = 'Session closed.'
          }
        },
      },
    })

    const rawSocket = (session as { conn?: { ws?: WebSocket } } | null)?.conn?.ws
    if (rawSocket?.addEventListener) {
      rawSocketListener = (event: MessageEvent) => {
        rawSocketMessageCount += 1
        if (rawSocketMessageCount <= 5) {
          if (typeof event.data === 'string') {
            addEvent(`Raw socket message #${rawSocketMessageCount}: ${event.data.slice(0, 120)}`)
          } else {
            addEvent(`Raw socket message #${rawSocketMessageCount}: ${Object.prototype.toString.call(event.data)}`)
          }
        }
      }
      rawSocket.addEventListener('message', rawSocketListener)
      rawSocket.addEventListener('close', (event) => {
        addEvent(`Raw socket closed (code ${event.code}${event.reason ? `: ${event.reason}` : ''}).`)
      })
      rawSocket.addEventListener('error', () => {
        addEvent('Raw socket reported an error.')
      })
    }

    voiceInputBlocked.value = false
    await startMicrophone()
    addEvent('Microphone streaming started.')
    connectionState.value = 'connected'
    if (startSpeechRecognition()) {
      syncConversationPhase('Live session ready. Socratica will greet you first.')
    } else {
      addEvent('Falling back to backend transcription for microphone input.')
      syncConversationPhase('Live session ready. Socratica will greet you first.')
    }

    addEvent('Sending initial tutor prompt to Gemini.')
    session.sendClientContent({
      turns: [
        {
          role: 'user',
          parts: [
            {
              text: `The student wants to practice this topic: ${studyTopic.value.trim()}. Start with a short greeting, invite them to begin in their own words, and keep the exchange conversational. Challenge them using the ${tutorMode.value} style, but always wait after asking a question. Their goal is: ${learningGoal.value.trim()}.`,
            },
          ],
        },
      ],
      turnComplete: true,
    })
  } catch (error) {
    console.error('Failed to start live session:', error)
    connectionState.value = 'error'
    conversationPhase.value = 'error'
    statusMessage.value =
      error instanceof Error ? error.message : 'Failed to start the live voice session.'
    addEvent(
      `Failed to start the live voice session${error instanceof Error ? `: ${error.message}` : '.'}`
    )
    stopSpeechRecognition()
    await stopAudioPipeline()
    detachRawSocketListener()
    if (session) {
      session.close()
      session = null
    }
  }
}

const stopLiveSession = async () => {
  finalizeOpenTranscriptEntries()

  if (session) {
    detachRawSocketListener()
    session.close()
    session = null
  }

  stopSpeechRecognition()
  await stopAudioPipeline()
  connectionState.value = 'idle'
  conversationPhase.value = 'idle'
  statusMessage.value = 'Session stopped.'
  addEvent('Live session stopped.')

  if (transcriptEntries.value.length) {
    await saveCurrentSession()
  }
}

const formatDate = (value: string) => {
  return new Date(value).toLocaleString()
}

const goHome = () => {
  router.push('/')
}

onMounted(() => {
  applyPreset()
  void loadSavedSessions()
})

onBeforeUnmount(() => {
  void stopLiveSession()
})
</script>

<style scoped>
.live-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(200, 155, 81, 0.18), transparent 30%),
    linear-gradient(180deg, #050505 0%, #111111 100%);
  color: #f5f1e8;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.hero-card,
.panel {
  background: rgba(17, 17, 17, 0.88);
  border: 1px solid rgba(246, 226, 122, 0.16);
  border-radius: 24px;
  padding: 1.5rem;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.28);
}

.hero-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1.5rem;
}

.eyebrow {
  margin: 0 0 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  color: #cb9b51;
  font-size: 0.78rem;
}

h1,
h2 {
  margin: 0;
}

h1 {
  font-size: clamp(2rem, 4vw, 3.25rem);
  line-height: 1.05;
}

h2 {
  font-size: 1.15rem;
  margin-bottom: 1rem;
}

.hero-copy,
.helper-text,
.meta-text,
.script-note p {
  color: rgba(245, 241, 232, 0.76);
}

.hero-copy {
  max-width: 56rem;
  margin: 1rem 0 0;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.primary-button,
.secondary-button,
.danger-button,
.ghost-button,
.saved-session-card {
  border: none;
  border-radius: 999px;
  padding: 0.9rem 1.25rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 180ms ease, opacity 180ms ease;
}

.primary-button:hover,
.secondary-button:hover,
.danger-button:hover,
.ghost-button:hover,
.saved-session-card:hover {
  transform: translateY(-1px);
}

.primary-button:disabled,
.secondary-button:disabled {
  cursor: wait;
  opacity: 0.7;
}

.primary-button {
  background: linear-gradient(135deg, #f6e27a, #cb9b51);
  color: #101010;
}

.secondary-button,
.ghost-button {
  background: transparent;
  color: #f5f1e8;
  border: 1px solid rgba(245, 241, 232, 0.2);
}

.danger-button {
  background: rgba(255, 95, 95, 0.16);
  color: #ffd3d3;
  border: 1px solid rgba(255, 95, 95, 0.2);
}

.panel-grid {
  display: grid;
  grid-template-columns: minmax(320px, 420px) minmax(320px, 420px) minmax(0, 1fr);
  gap: 1.5rem;
  align-items: start;
}

.field-label,
.status-label,
.speaker {
  display: block;
  font-size: 0.84rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(245, 241, 232, 0.66);
}

.field-input,
.topic-input {
  width: 100%;
  margin-top: 0.65rem;
  border-radius: 16px;
  border: 1px solid rgba(245, 241, 232, 0.16);
  background: rgba(255, 255, 255, 0.03);
  color: #f5f1e8;
  padding: 1rem;
}

.field-input {
  margin-bottom: 1rem;
}

.topic-input {
  resize: vertical;
  min-height: 8rem;
}

.interrupt-toggle-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.75rem;
  margin-top: 1rem;
}

.interrupt-toggle-row .field-label {
  margin: 0;
}

.toggle-button {
  padding: 0.4rem 1rem;
  border-radius: 999px;
  font-weight: 600;
  font-size: 0.85rem;
  border: 1px solid rgba(245, 241, 232, 0.2);
  background: rgba(255, 255, 255, 0.05);
  color: #f5f1e8;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.toggle-button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.toggle-button.active {
  background: rgba(105, 219, 124, 0.2);
  border-color: rgba(105, 219, 124, 0.4);
  color: #8ce99a;
}

.interrupt-helper {
  margin: 0;
  font-size: 0.85rem;
  color: rgba(245, 241, 232, 0.65);
}

.status-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-top: 1rem;
}

.status-chip {
  border-radius: 999px;
  padding: 0.4rem 0.8rem;
  font-weight: 700;
  font-size: 0.85rem;
}

.status-chip.idle {
  background: rgba(245, 241, 232, 0.1);
}

.status-chip.connecting {
  background: rgba(246, 226, 122, 0.16);
  color: #f6e27a;
}

.status-chip.connected {
  background: rgba(105, 219, 124, 0.16);
  color: #8ce99a;
}

.status-chip.error {
  background: rgba(255, 95, 95, 0.16);
  color: #ffb3b3;
}

.status-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.85rem;
  margin-top: 1rem;
}

.status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.85rem 1rem;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.03);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.transcript-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-height: 24rem;
}

.transcript-live-status {
  padding: 1rem;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(245, 241, 232, 0.08);
}

.transcript-live-status .helper-text {
  margin: 0.75rem 0 0;
  line-height: 1.6;
}

.turn-indicator {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  color: #f5f1e8;
}

.turn-indicator.assistant-speaking {
  color: #8ce99a;
}

.turn-indicator.waiting-for-user,
.turn-indicator.user-speaking {
  color: #f6e27a;
}

.turn-indicator.processing-user {
  color: #9ec5fe;
}

.turn-indicator.connecting {
  color: #f8d66d;
}

.turn-indicator.error {
  color: #ffb3b3;
}

.turn-pulse {
  width: 0.85rem;
  height: 0.85rem;
  border-radius: 999px;
  background: currentColor;
  flex-shrink: 0;
  box-shadow: 0 0 0 0 currentColor;
  animation: live-pulse 1.9s infinite;
}

.transcript-stream {
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
  flex: 1;
  min-height: 0;
  max-height: 38rem;
  overflow-y: auto;
}

.transcript-entry {
  border-radius: 18px;
  padding: 1rem;
}

.transcript-entry-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.transcript-entry p {
  margin: 0.45rem 0 0;
  line-height: 1.65;
}

.user-entry {
  background: rgba(246, 226, 122, 0.08);
  border: 1px solid rgba(246, 226, 122, 0.16);
}

.model-entry {
  background: rgba(105, 219, 124, 0.07);
  border: 1px solid rgba(105, 219, 124, 0.14);
}

.pending-entry {
  opacity: 0.88;
}

.interrupted-entry {
  border-style: dashed;
}

.entry-chip {
  border-radius: 999px;
  padding: 0.2rem 0.55rem;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.live-chip {
  background: rgba(245, 241, 232, 0.08);
  color: rgba(245, 241, 232, 0.82);
}

.interrupted-chip {
  background: rgba(255, 95, 95, 0.16);
  color: #ffd3d3;
}

.empty-state {
  margin: 0;
  color: rgba(245, 241, 232, 0.55);
}

.demo-steps {
  margin: 0;
  padding-left: 1.2rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  line-height: 1.6;
}

.script-note {
  margin-top: 1.25rem;
  padding: 1rem;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
}

.saved-session-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.saved-session-card {
  border-radius: 18px;
  text-align: left;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(245, 241, 232, 0.08);
  color: #f5f1e8;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.saved-session-card span {
  font-size: 0.88rem;
  color: rgba(245, 241, 232, 0.68);
}

.event-list {
  margin: 0;
  padding-left: 1.1rem;
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  color: rgba(245, 241, 232, 0.82);
}

@keyframes live-pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.3);
  }

  70% {
    box-shadow: 0 0 0 0.55rem rgba(255, 255, 255, 0);
  }

  100% {
    box-shadow: 0 0 0 0 rgba(255, 255, 255, 0);
  }
}

@media (max-width: 1200px) {
  .panel-grid {
    grid-template-columns: 1fr 1fr;
  }

  .transcript-panel {
    grid-column: 1 / -1;
  }
}

@media (max-width: 900px) {
  .live-page {
    padding: 1rem;
  }

  .hero-card,
  .panel-grid {
    grid-template-columns: 1fr;
  }

  .hero-card {
    flex-direction: column;
  }

  .hero-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .status-list {
    grid-template-columns: 1fr;
  }
}
</style>
