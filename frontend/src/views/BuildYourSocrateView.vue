<template>
  <div class="build-page">
    <a class="back-btn" @click.prevent="personalizing ? (personalizing = false) : router.push('/')">
      <svg class="back-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M19 12H5M5 12l7-7M5 12l7 7"/>
      </svg>
      <span class="back-label">{{ personalizing ? 'Back to session' : 'Back to homepage' }}</span>
    </a>

    <canvas ref="splineCanvas" class="spline-bg" :class="{ 'spline-zoom': personalizing }"></canvas>

    <!-- Personalize button over avatar (voice sessions only) -->
    <div class="personalize-btn-wrap" :class="{ 'hidden-ui': personalizing || !isVoiceSession }">
      <button class="personalize-btn" @click="personalizing = true">
        Personalize Me
      </button>
    </div>

    <!-- Personalize overlay: 4 cards around the avatar -->
    <transition name="personalize">
      <div v-if="personalizing" class="personalize-overlay">



        <!-- Top-left: AI Voice -->
        <div class="p-card top-left">
          <h3 class="p-card-title">AI Voice</h3>
          <p class="p-card-desc">Select the vocal tone and accent for your Socratic guide.</p>
          <div class="p-dropdown-wrapper">
            <button class="p-dropdown-btn" @click="voiceOpen = !voiceOpen">
              <span>{{ selectedVoice }}</span>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M6 9l6 6 6-6"/></svg>
            </button>
            <div v-if="voiceOpen" class="p-dropdown-menu">
              <button v-for="v in voices" :key="v" class="p-dropdown-item" :class="{ active: selectedVoice === v }" @click="selectedVoice = v; voiceOpen = false">{{ v }}</button>
            </div>
          </div>
        </div>

        <!-- Top-right: Teaching Style -->
        <div class="p-card top-right">
          <h3 class="p-card-title">Teaching Style</h3>
          <p class="p-card-desc">How the AI guides your learning journey.</p>
          <div class="p-dropdown-wrapper">
            <button class="p-dropdown-btn" @click="styleOpen = !styleOpen">
              <span>{{ selectedStyle }}</span>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M6 9l6 6 6-6"/></svg>
            </button>
            <div v-if="styleOpen" class="p-dropdown-menu">
              <button v-for="s in teachingStyles" :key="s" class="p-dropdown-item" :class="{ active: selectedStyle === s }" @click="selectedStyle = s; styleOpen = false">{{ s }}</button>
            </div>
          </div>
        </div>

        <!-- Bottom-left: Personality -->
        <div class="p-card bottom-left">
          <h3 class="p-card-title">Personality</h3>
          <p class="p-card-desc">Set the temperament of your AI companion.</p>
          <div class="p-dropdown-wrapper">
            <button class="p-dropdown-btn" @click="personalityOpen = !personalityOpen">
              <span>{{ selectedPersonality }}</span>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M6 9l6 6 6-6"/></svg>
            </button>
            <div v-if="personalityOpen" class="p-dropdown-menu p-dropdown-menu--up">
              <button v-for="p in personalities" :key="p" class="p-dropdown-item" :class="{ active: selectedPersonality === p }" @click="selectedPersonality = p; personalityOpen = false">{{ p }}</button>
            </div>
          </div>
        </div>

        <!-- Save button -->
        <div class="p-save-wrap">
          <button class="p-save-btn" @click="personalizing = false">
            Save Configuration
          </button>
        </div>

        <!-- Bottom-right: Dynamic Interruptions -->
        <div class="p-card bottom-right">
          <div class="p-card-header-row">
            <h3 class="p-card-title">Dynamic Interruptions</h3>
            <button class="p-toggle" :class="{ on: dynamicInterruptions }" @click="dynamicInterruptions = !dynamicInterruptions">
              <span class="p-toggle-thumb"></span>
            </button>
          </div>
          <p class="p-card-desc">Allow the AI to politely interrupt when you veer off-topic or make a logical fallacy during live voice sessions.</p>
        </div>

      </div>
    </transition>

    <!-- Left: title + controls -->
    <div class="build-hero" :class="{ 'hidden-ui': personalizing }">
      <div class="build-hero-text">
        <h1 class="build-title">Build your own <span class="title-golden">Socratic</span> Tutor.</h1>
        <p class="build-subtitle">And start improving your desired skills.</p>

        <div class="build-actions">
          <!-- Dropdown button -->
          <div class="dropdown-wrapper">
            <button class="btn-dropdown" @click="toggleDropdown">
              <span :class="{ placeholder: !selectedMode }">{{ selectedMode || 'Choose Type of Session' }}</span>
              <svg class="chevron" :class="{ open: dropdownOpen }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M6 9l6 6 6-6"/>
              </svg>
            </button>
            <transition name="dropdown">
              <div v-if="dropdownOpen" class="dropdown-menu">
                <button
                  v-for="option in modeOptions"
                  :key="option"
                  class="dropdown-item"
                  :class="{ active: selectedMode === option }"
                  @click="selectMode(option)"
                >{{ option }}</button>
              </div>
            </transition>
          </div>

          <!-- Start / Stop Live Voice button -->
          <button
            class="btn-live"
            :class="{ 'btn-live-active': isConnected }"
            :disabled="isBusy"
            @click="isConnected ? stopLiveSession() : startLiveSession()"
          >
            <svg class="mic-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="9" y="2" width="6" height="11" rx="3"/>
              <path d="M5 10a7 7 0 0014 0M12 19v3M9 22h6"/>
            </svg>
            {{ isBusy ? 'Connecting…' : isConnected ? 'Stop Session' : 'Start Live Voice' }}
          </button>
        </div>

        <!-- Live status indicator (below buttons) -->
        <div v-if="isConnected" class="live-status">
          <span class="live-dot" :class="{ listening: isListening, speaking: isModelSpeaking }"></span>
          <span class="live-label">{{ isModelSpeaking ? 'Socratica is speaking' : isListening ? 'Listening…' : 'Ready' }}</span>
        </div>
      </div>
    </div>

    <!-- Left: session wrapper (panel + tab slide together) -->
    <div class="session-wrapper" :class="{ hidden: !sessionVisible || !selectedMode, 'hidden-ui': personalizing }">
      <div class="session-panel">
        <div class="session-header">
          <span class="session-label">Session Details</span>
        </div>
        <div class="session-body">
          <div class="session-field">
            <label class="field-label">Title</label>
            <input v-model="sessionTitle" class="field-input" type="text" placeholder="e.g. Biology Exam Prep" />
          </div>
          <div class="session-field">
            <label class="field-label">What do you want to study?</label>
            <textarea
              v-model="sessionTopic"
              class="field-input field-textarea"
              placeholder="e.g. Chapter 4 of my biology textbook, focusing on cell division..."
              rows="3"
            ></textarea>
          </div>

          <div class="session-field">
            <label class="field-label">Files</label>
            <div class="file-drop" :class="{ dragging: isDragging }"
              @dragover.prevent="isDragging = true"
              @dragleave.prevent="isDragging = false"
              @drop.prevent="onDrop"
              @click="fileInput?.click()">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 16V4M12 4l-3 3M12 4l3 3"/>
                <path d="M20 16v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2"/>
              </svg>
              <span>Drop files or <u>browse</u></span>
            </div>
            <input ref="fileInput" type="file" multiple class="hidden-file-input" @change="onFileChange" />
            <ul v-if="uploadedFiles.length > 0" class="file-list">
              <li v-for="(file, i) in uploadedFiles" :key="i" class="file-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6z"/>
                  <path d="M14 2v6h6"/>
                </svg>
                <span class="file-name">{{ file.name }}</span>
                <button class="file-remove" @click.stop="removeFile(i)">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M18 6L6 18M6 6l12 12"/>
                  </svg>
                </button>
              </li>
            </ul>
          </div>
          <template v-if="isVoiceSession">
            <div class="session-field">
              <div class="toggle-row">
                <div>
                  <span class="field-label">Interruption Mode</span>
                  <p class="field-hint">Allow the AI to interrupt you mid-sentence.</p>
                </div>
                <button class="toggle" :class="{ on: interruptionMode }" @click="interruptionMode = !interruptionMode">
                  <span class="toggle-thumb"></span>
                </button>
              </div>
              <button class="toggle" :class="{ on: interruptingModeEnabled }" @click="toggleInterruptingMode">
                <span class="toggle-thumb"></span>
              </button>
            </div>
          </template>
        </div>
      </div>
      <!-- Tab attached to right edge of wrapper -->
      <button class="panel-tab" @click="sessionVisible = !sessionVisible">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path :d="sessionVisible ? 'M15 18l-6-6 6-6' : 'M9 18l6-6-6-6'"/>
        </svg>
      </button>
    </div>

    <!-- Right: transcript + fallback wrapper (both slide together) -->
    <div class="transcript-wrapper" :class="{ hidden: !transcriptVisible || !selectedMode, 'hidden-ui': personalizing }">
      <!-- Tab attached to left edge of wrapper -->
      <button class="panel-tab" @click="transcriptVisible = !transcriptVisible">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path :d="transcriptVisible ? 'M9 18l6-6-6-6' : 'M15 18l-6-6 6-6'"/>
        </svg>
      </button>
      <div class="right-panels">
        <div class="transcript-panel">
          <div class="transcript-header">
            <span class="transcript-label">Transcript</span>
            <span class="transcript-status" :class="{ active: isConnected }">
              <span class="status-dot"></span>
              {{ isConnected ? (isModelSpeaking ? 'Speaking' : isListening ? 'Listening' : 'Live') : 'Waiting…' }}
            </span>
          </div>
          <div class="transcript-body" ref="transcriptBody">
            <div v-if="transcriptEntries.length === 0" class="transcript-empty">
              Your conversation will appear here once the session starts.
            </div>
            <div
              v-for="entry in transcriptEntries"
              :key="entry.id"
              class="transcript-msg"
              :class="entry.speaker === 'You' ? 'user' : 'ai'"
            >
              <span class="msg-role">{{ entry.speaker }}</span>
              <p class="msg-text" :class="{ pending: entry.pending }">{{ entry.text }}</p>
            </div>
          </div>
        </div>
        <div class="fallback-panel">
          <form class="fallback-form" @submit.prevent="submitFallback">
            <input
              v-model="fallbackInput"
              class="fallback-input"
              type="text"
              placeholder="Type a message…"
              autocomplete="off"
              :disabled="!isConnected"
            />
            <button class="fallback-send" type="submit" :disabled="!fallbackInput.trim() || !isConnected">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 2L11 13M22 2L15 22l-4-9-9-4 20-7z"/>
              </svg>
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  ActivityHandling,
  GoogleGenAI,
  Modality,
  type LiveServerMessage,
  type Session,
} from '@google/genai'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Application } from '@splinetool/runtime'
import { liveVoiceService } from '@/services/liveVoiceService'

// ─── Types ────────────────────────────────────────────────────────────────────

type ConnectionState = 'idle' | 'connecting' | 'connected' | 'error'
type TranscriptSpeaker = 'You' | 'Socratica'

interface TranscriptEntry {
  id: string
  speaker: TranscriptSpeaker
  text: string
  time: string
  pending: boolean
}

interface SpeechRecognitionAlternativeLike { transcript: string }
interface SpeechRecognitionResultLike {
  isFinal: boolean
  length: number
  [index: number]: SpeechRecognitionAlternativeLike
}
interface SpeechRecognitionEventLike extends Event {
  resultIndex: number
  results: { length: number; [index: number]: SpeechRecognitionResultLike }
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

// ─── Router ───────────────────────────────────────────────────────────────────

const router = useRouter()

// ─── Spline 3D background ─────────────────────────────────────────────────────

const splineCanvas = ref<HTMLCanvasElement | null>(null)
let splineApp: Application | null = null

// ─── UI state ─────────────────────────────────────────────────────────────────

const dropdownOpen = ref(false)
const selectedMode = ref('')
const modeOptions = [
  'Written Evaluation',
  'Presentation Prep',
  'Socratic Evaluation',
  'Interview Prep',
  'Cover Letter Analysis',
  'Notes & Summaries',
]

const sessionVisible = ref(true)
const transcriptVisible = ref(true)
const personalizing = ref(false)

const sessionTitle = ref('')
const sessionTopic = ref('')
const uploadedFiles = ref<File[]>([])
const isDragging = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const fallbackInput = ref('')

// ─── Live voice reactive state ────────────────────────────────────────────────

const connectionState = ref<ConnectionState>('idle')
const isListening = ref(false)
const isModelSpeaking = ref(false)
const isTranscribing = ref(false)
const transcriptEntries = ref<TranscriptEntry[]>([])
const voiceInputBlocked = ref(false)
const interruptingModeEnabled = ref(false)

// ─── Computed ─────────────────────────────────────────────────────────────────

const isBusy = computed(() => connectionState.value === 'connecting')
const isConnected = computed(() => connectionState.value === 'connected')

// ─── Transcript scroll ref ────────────────────────────────────────────────────

const transcriptBody = ref<HTMLElement | null>(null)

// ─── Non-reactive audio / session handles ────────────────────────────────────

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
let capturedSpeechChunks: ArrayBuffer[] = []
let speechRecognition: SpeechRecognitionLike | null = null
let speechRecognitionActive = false
let speechRecognitionEnabled = false
let nextEntryId = 0
let consecutiveSpeechChunks = 0
let accumulatedRecognitionText = ''
let onModeBuffer = ''
let onModeFlushTimer: ReturnType<typeof setTimeout> | null = null
let userInterruptedSocratica = false

// ─── Assistant-initiated interruption state ───────────────────────────────────

let assistantIsInterruptingUser = false
let assistantInterruptSuppressUntil = 0
let lastAssistantInterruptTime = 0
let inputTranscriptionAccumulator = ''

// ─── Audio constants ──────────────────────────────────────────────────────────

const SPEECH_LEVEL_THRESHOLD = 0.02
const SPEECH_CONFIRM_CHUNKS = 3
const SILENCE_CHUNKS_INTERRUPTING_ON = 9    // ≈ 750 ms
const SILENCE_CHUNKS_INTERRUPTING_OFF = 24  // ≈ 2 s
const ON_MODE_FLUSH_DELAY_MS = 450
const ON_MODE_MIN_WORDS = 5
const RECOGNITION_RESTART_DELAY_MS = 600

const INTERRUPT_AUDIO_SUPPRESS_MS = 3000
const INTERRUPT_EVAL_MIN_WORDS = 15
const INTERRUPT_COOLDOWN_MS = 8000

// ─── Helpers ──────────────────────────────────────────────────────────────────

const scrollTranscriptToBottom = async () => {
  await nextTick()
  if (transcriptBody.value) {
    transcriptBody.value.scrollTop = transcriptBody.value.scrollHeight
  }
}

const replaceTranscriptText = (speaker: TranscriptSpeaker, text: string) => {
  if (!text) return
  const entries = transcriptEntries.value
  const last = entries[entries.length - 1]
  if (last && last.speaker === speaker && last.pending) {
    last.text = text
  } else {
    entries.push({
      id: String(++nextEntryId),
      speaker,
      text,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      pending: true,
    })
  }
  void scrollTranscriptToBottom()
}

const streamTranscript = (speaker: TranscriptSpeaker, text: string) => {
  if (!text) return
  const entries = transcriptEntries.value
  const last = entries[entries.length - 1]
  if (last && last.speaker === speaker && last.pending) {
    last.text += text
  } else {
    entries.push({
      id: String(++nextEntryId),
      speaker,
      text,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      pending: true,
    })
  }
  void scrollTranscriptToBottom()
}

const finalizeTranscript = (speaker: TranscriptSpeaker, _finalText?: string) => {
  const entries = transcriptEntries.value
  for (let i = entries.length - 1; i >= 0; i--) {
    if (entries[i].speaker === speaker && entries[i].pending) {
      const resolved = entries[i].text.trim()
      if (!resolved) {
        entries.splice(i, 1)
      } else {
        entries[i].pending = false
      }
      void scrollTranscriptToBottom()
      return
    }
  }
}

const addUserEntry = (text: string) => {
  const trimmed = text.trim()
  if (!trimmed) return
  const entries = transcriptEntries.value
  const last = entries[entries.length - 1]
  if (last && last.speaker === 'You' && last.pending) {
    last.text = trimmed
    last.pending = false
    void scrollTranscriptToBottom()
    return
  }
  if (last && last.speaker === 'You' && last.text === trimmed) return
  entries.push({
    id: String(++nextEntryId),
    speaker: 'You',
    text: trimmed,
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    pending: false,
  })
  void scrollTranscriptToBottom()
}

// ─── System prompt ────────────────────────────────────────────────────────────

const buildTutorPrompt = () => {
  const mode = selectedMode.value || 'Socratic Evaluation'
  const topic = sessionTitle.value.trim() || 'the chosen subject'

  const interruptBehavior = interruptingModeEnabled.value
    ? [
        'You are in ACTIVE INTERRUPTION MODE.',
        'Listen carefully to the meaning of what the student says, not just whether they have paused.',
        'Interrupt the student — even mid-explanation — whenever you detect any of the following:',
        '(1) A vague or undefined term that needs clarification.',
        '(2) An incorrect fact, flawed logic, or inconsistent claim.',
        '(3) A skipped causal step — the student jumped to a conclusion without explaining why.',
        '(4) The student sounds uncertain, confused, or is visibly guessing.',
        '(5) The answer is too shallow — important details are being glossed over.',
        '(6) A natural Socratic moment: a question that would deepen understanding right now.',
        'When any of these occur, interrupt with ONE short, focused question or gentle correction.',
        'Do not wait for the student to finish their full thought — jump in naturally, like an engaged tutor leaning in.',
        'Only stay silent when the student is clearly on the right track and building a solid explanation.',
      ].join(' ')
    : 'Always wait for the student to fully finish their explanation before you speak. Do not interrupt, even if you notice an issue. Be patient and fully user-led. Only respond after the student has clearly finished their thought.'

  return [
    'You are Socratica, a spoken Socratic tutor in a real-time voice conversation.',
    `Tutor mode: ${mode}.`,
    `Student topic: ${topic}.`,
    'Greet the student briefly, invite them to begin in their own words.',
    interruptBehavior,
    'Keep spoken responses concise and natural — one to three sentences at most.',
    'Ask one question at a time. After the student answers, briefly acknowledge their point before building on it or challenging it.',
    'This is a back-and-forth spoken conversation, not a lecture. Avoid long monologues.',
  ].join(' ')
}

// ─── Interrupting mode toggle ─────────────────────────────────────────────────

const toggleInterruptingMode = () => {
  interruptingModeEnabled.value = !interruptingModeEnabled.value
}

watch(interruptingModeEnabled, (newValue) => {
  if (!session || connectionState.value !== 'connected') return
  accumulatedRecognitionText = ''
  finalizeTranscript('You')
  if (onModeFlushTimer !== null) { clearTimeout(onModeFlushTimer); onModeFlushTimer = null }
  onModeBuffer = ''
  assistantIsInterruptingUser = false
  inputTranscriptionAccumulator = ''

  const modeMessage = newValue
    ? [
        '[Behaviour update: ACTIVE INTERRUPTION MODE is now ON.',
        'From this point forward, listen carefully to the meaning of what the student says.',
        'Interrupt promptly — even mid-sentence — when the student is vague, incorrect, skipping reasoning steps, confused, or needs to elaborate.',
        'Do not rely on silence alone to decide when to respond.',
        'React to the content: jump in with one focused question or gentle correction whenever it would help.',
        'Only stay silent when the student is clearly on the right track.]',
      ].join(' ')
    : '[Behaviour update: From this point on, always wait for the student to fully finish speaking before responding. Do not interrupt under any circumstance, even if you notice an issue. Be patient and fully user-led. Only respond after the student has clearly finished their thought.]'

  try {
    session.sendClientContent({
      turns: [{ role: 'user', parts: [{ text: modeMessage }] }],
      turnComplete: true,
    })
  } catch { /* ignore if session not ready */ }
})

// ─── isModelSpeaking watcher ──────────────────────────────────────────────────

watch(isModelSpeaking, (speaking) => {
  if (!speechRecognitionEnabled || !speechRecognition) return

  if (speaking) {
    if (speechRecognitionActive) {
      speechRecognition.stop()
    }
    if (onModeFlushTimer !== null) { clearTimeout(onModeFlushTimer); onModeFlushTimer = null }
    onModeBuffer = ''
    inputTranscriptionAccumulator = ''
    finalizeTranscript('You')
  } else {
    const delay = userInterruptedSocratica ? 50 : RECOGNITION_RESTART_DELAY_MS
    userInterruptedSocratica = false
    setTimeout(() => {
      if (!speechRecognitionEnabled || !speechRecognition || isModelSpeaking.value) return
      if (!speechRecognitionActive) {
        try { speechRecognition!.start(); speechRecognitionActive = true } catch { /* ignore */ }
      }
    }, delay)
  }
})

// ─── Assistant interruption logic ────────────────────────────────────────────

const triggerAssistantInterruption = () => {
  if (!interruptingModeEnabled.value) return
  if (assistantIsInterruptingUser) return
  if (isModelSpeaking.value) return
  const now = Date.now()
  if (now - lastAssistantInterruptTime < INTERRUPT_COOLDOWN_MS) return

  assistantIsInterruptingUser = true
  assistantInterruptSuppressUntil = now + INTERRUPT_AUDIO_SUPPRESS_MS
  lastAssistantInterruptTime = now
  speechActivityStarted = false
  silenceChunkCount = 0
  if (onModeFlushTimer !== null) { clearTimeout(onModeFlushTimer); onModeFlushTimer = null }
  onModeBuffer = ''
  inputTranscriptionAccumulator = ''
}

const evaluateInputTranscriptionForInterrupt = (text: string) => {
  if (!interruptingModeEnabled.value) return
  if (assistantIsInterruptingUser || isModelSpeaking.value) return
  const words = text.trim().split(/\s+/).filter(Boolean)
  if (words.length < INTERRUPT_EVAL_MIN_WORDS) return
  const now = Date.now()
  if (now - lastAssistantInterruptTime < INTERRUPT_COOLDOWN_MS) return
  triggerAssistantInterruption()
}

// ─── Transcript helpers ───────────────────────────────────────────────────────

const sendRecognizedUserText = (text: string) => {
  const trimmed = text.trim()
  if (!trimmed || !session || connectionState.value !== 'connected') return
  addUserEntry(trimmed)
  clearPlaybackQueue()
  session.sendClientContent({
    turns: [{ role: 'user', parts: [{ text: trimmed }] }],
    turnComplete: true,
  })
}

const transcribeCapturedSpeech = async () => {
  if (!capturedSpeechChunks.length || isTranscribing.value) {
    capturedSpeechChunks = []
    return
  }
  const audioBlob = createWavBlobFromPcmChunks(capturedSpeechChunks, 16000)
  capturedSpeechChunks = []
  isTranscribing.value = true
  try {
    const transcript = await liveVoiceService.transcribeAudio(audioBlob)
    if (!transcript.trim()) { finalizeTranscript('You'); return }
    sendRecognizedUserText(transcript)
  } catch {
    finalizeTranscript('You')
  } finally {
    isTranscribing.value = false
  }
}

const submitFallback = () => {
  const text = fallbackInput.value.trim()
  if (!text) return
  sendRecognizedUserText(text)
  fallbackInput.value = ''
}

// ─── Audio playback ───────────────────────────────────────────────────────────

const clearPlaybackQueue = () => {
  activePlaybackNodes.forEach((node) => { try { node.stop() } catch { /* ignore */ } })
  activePlaybackNodes = []
  playbackCursor = audioContext ? audioContext.currentTime : 0
  isModelSpeaking.value = false
}

const ensureAudioContext = async () => {
  if (!audioContext) audioContext = new AudioContext()
  if (audioContext.state === 'suspended') await audioContext.resume()
  return audioContext
}

const decodeBase64ToArrayBuffer = (base64: string): ArrayBuffer => {
  const binary = window.atob(base64)
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i)
  return bytes.buffer
}

const pcm16ToFloat32 = (buffer: ArrayBuffer): Float32Array => {
  const view = new DataView(buffer)
  const samples = new Float32Array(buffer.byteLength / 2)
  for (let i = 0; i < samples.length; i++) samples[i] = view.getInt16(i * 2, true) / 32768
  return samples
}

const enqueueAudioChunk = async (base64Audio: string) => {
  const ctx = await ensureAudioContext()
  const floatSamples = pcm16ToFloat32(decodeBase64ToArrayBuffer(base64Audio))
  if (!floatSamples.length) return

  const buffer = ctx.createBuffer(1, floatSamples.length, 24000)
  buffer.copyToChannel(floatSamples, 0)

  const source = ctx.createBufferSource()
  source.buffer = buffer
  source.connect(ctx.destination)

  const now = ctx.currentTime
  playbackCursor = Math.max(playbackCursor, now + 0.02)
  source.start(playbackCursor)
  playbackCursor += buffer.duration
  isModelSpeaking.value = true
  activePlaybackNodes.push(source)

  // Gemini is now speaking — restore user audio so barge-in always works.
  assistantIsInterruptingUser = false
  inputTranscriptionAccumulator = ''

  source.onended = () => {
    activePlaybackNodes = activePlaybackNodes.filter((n) => n !== source)
    if (!activePlaybackNodes.length && audioContext) {
      playbackCursor = audioContext.currentTime
      isModelSpeaking.value = false
    }
  }
}

// ─── WAV / PCM utilities ──────────────────────────────────────────────────────

const createWavBlobFromPcmChunks = (chunks: ArrayBuffer[], sampleRate: number): Blob => {
  const totalPcmBytes = chunks.reduce((sum, chunk) => sum + chunk.byteLength, 0)
  const wavBuffer = new ArrayBuffer(44 + totalPcmBytes)
  const view = new DataView(wavBuffer)
  const writeAscii = (offset: number, value: string) => {
    for (let i = 0; i < value.length; i++) view.setUint8(offset + i, value.charCodeAt(i))
  }
  writeAscii(0, 'RIFF'); view.setUint32(4, 36 + totalPcmBytes, true)
  writeAscii(8, 'WAVE'); writeAscii(12, 'fmt ')
  view.setUint32(16, 16, true); view.setUint16(20, 1, true); view.setUint16(22, 1, true)
  view.setUint32(24, sampleRate, true); view.setUint32(28, sampleRate * 2, true)
  view.setUint16(32, 2, true); view.setUint16(34, 16, true)
  writeAscii(36, 'data'); view.setUint32(40, totalPcmBytes, true)
  let offset = 44
  for (const chunk of chunks) {
    new Uint8Array(wavBuffer, offset, chunk.byteLength).set(new Uint8Array(chunk))
    offset += chunk.byteLength
  }
  return new Blob([wavBuffer], { type: 'audio/wav' })
}

const downsampleTo16k = (input: Float32Array, inputSampleRate: number): Float32Array => {
  if (inputSampleRate === 16000) return input
  const ratio = inputSampleRate / 16000
  const outputLength = Math.max(1, Math.round(input.length / ratio))
  const output = new Float32Array(outputLength)
  let outputIndex = 0, inputIndex = 0
  while (outputIndex < outputLength) {
    const nextInputIndex = Math.round((outputIndex + 1) * ratio)
    let sample = 0, count = 0
    for (let i = inputIndex; i < nextInputIndex && i < input.length; i++) { sample += input[i]; count++ }
    output[outputIndex] = count > 0 ? sample / count : 0
    outputIndex++; inputIndex = nextInputIndex
  }
  return output
}

const float32ToPcm16 = (input: Float32Array): ArrayBuffer => {
  const buffer = new ArrayBuffer(input.length * 2)
  const view = new DataView(buffer)
  for (let i = 0; i < input.length; i++) {
    const clamped = Math.max(-1, Math.min(1, input[i]))
    view.setInt16(i * 2, clamped < 0 ? clamped * 32768 : clamped * 32767, true)
  }
  return buffer
}

const calculateLevel = (input: Float32Array): number => {
  let sum = 0
  for (let i = 0; i < input.length; i++) sum += input[i] * input[i]
  return Math.sqrt(sum / input.length)
}

const arrayBufferToBase64 = (buffer: ArrayBuffer): string => {
  const bytes = new Uint8Array(buffer)
  let binary = ''
  for (let i = 0; i < bytes.byteLength; i++) binary += String.fromCharCode(bytes[i])
  return window.btoa(binary)
}

// ─── Server message handler ───────────────────────────────────────────────────

const normalizeServerMessage = (payload: unknown): LiveServerMessage | null => {
  if (!payload) return null
  if (typeof payload === 'string') {
    try { return JSON.parse(payload) as LiveServerMessage } catch { return null }
  }
  if (payload instanceof MessageEvent) return normalizeServerMessage(payload.data)
  if (
    typeof payload === 'object' && payload !== null &&
    'data' in payload && !('serverContent' in payload) && !('setupComplete' in payload)
  ) return normalizeServerMessage((payload as { data: unknown }).data)
  return payload as LiveServerMessage
}

const handleServerMessage = async (payload: unknown) => {
  const message = normalizeServerMessage(payload)
  if (!message) return

  serverMessageCount++
  if (!hasSeenServerMessage) hasSeenServerMessage = true

  const inputTranscription = message.serverContent?.inputTranscription ?? message.inputTranscription
  const outputTranscription = message.serverContent?.outputTranscription ?? message.outputTranscription
  const serverContent = message.serverContent

  if (serverContent?.interrupted) {
    clearPlaybackQueue()
    finalizeTranscript('Socratica')
  }

  if (inputTranscription?.text) {
    streamTranscript('You', inputTranscription.text)
    if (interruptingModeEnabled.value) {
      inputTranscriptionAccumulator += inputTranscription.text
      evaluateInputTranscriptionForInterrupt(inputTranscriptionAccumulator)
    }
  }
  if (inputTranscription?.finished) {
    finalizeTranscript('You', inputTranscription.text ?? undefined)
    inputTranscriptionAccumulator = ''
  }

  if (outputTranscription?.text) streamTranscript('Socratica', outputTranscription.text)
  if (outputTranscription?.finished) finalizeTranscript('Socratica', outputTranscription.text ?? undefined)

  const parts = serverContent?.modelTurn?.parts ?? []
  for (const part of parts) {
    if (part.inlineData?.data) await enqueueAudioChunk(part.inlineData.data)
  }
}

// ─── Microphone pipeline ──────────────────────────────────────────────────────

const startMicrophone = async () => {
  if (!navigator.mediaDevices?.getUserMedia) throw new Error('This browser does not support microphone streaming.')

  mediaStream = await navigator.mediaDevices.getUserMedia({
    audio: { channelCount: 1, echoCancellation: true, noiseSuppression: true, autoGainControl: true },
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
    if (!session || connectionState.value !== 'connected' || !audioContext) return

    const inputSamples = event.inputBuffer.getChannelData(0)
    const level = calculateLevel(inputSamples)
    const downsampled = downsampleTo16k(inputSamples, audioContext.sampleRate)
    const pcm16Buffer = float32ToPcm16(downsampled)

    // Stream to Gemini — send silence during assistant-initiated interruption so
    // Gemini's VAD detects end-of-activity and can respond without being blocked.
    try {
      const nowMs = Date.now()
      const suppressing = assistantIsInterruptingUser && nowMs < assistantInterruptSuppressUntil
      if (!suppressing && assistantIsInterruptingUser) assistantIsInterruptingUser = false
      const audioPayload = suppressing ? new ArrayBuffer(pcm16Buffer.byteLength) : pcm16Buffer
      session.sendRealtimeInput({ audio: { data: arrayBufferToBase64(audioPayload), mimeType: 'audio/pcm;rate=16000' } })
    } catch { /* session may be closing */ }

    if (level > SPEECH_LEVEL_THRESHOLD) {
      consecutiveSpeechChunks++
      if (speechActivityStarted) {
        silenceChunkCount = 0
        isListening.value = true
        capturedSpeechChunks.push(pcm16Buffer)
      } else if (consecutiveSpeechChunks >= SPEECH_CONFIRM_CHUNKS) {
        const wasModelSpeaking = isModelSpeaking.value
        clearPlaybackQueue()
        if (wasModelSpeaking) userInterruptedSocratica = true
        if (speechRecognitionEnabled && speechRecognition && !speechRecognitionActive) {
          try { speechRecognition.start(); speechRecognitionActive = true } catch { /* ignore */ }
        }
        speechActivityStarted = true
        silenceChunkCount = 0
        isListening.value = true
        capturedSpeechChunks.push(pcm16Buffer)
      } else {
        capturedSpeechChunks.push(pcm16Buffer)
      }
      return
    }

    consecutiveSpeechChunks = 0

    if (speechActivityStarted) {
      silenceChunkCount++
      capturedSpeechChunks.push(pcm16Buffer)

      const silenceThreshold = interruptingModeEnabled.value
        ? SILENCE_CHUNKS_INTERRUPTING_ON
        : SILENCE_CHUNKS_INTERRUPTING_OFF

      if (silenceChunkCount >= silenceThreshold) {
        speechActivityStarted = false
        silenceChunkCount = 0
        isListening.value = false
        if (speechRecognitionEnabled && accumulatedRecognitionText) {
          capturedSpeechChunks = []
          const text = accumulatedRecognitionText
          accumulatedRecognitionText = ''
          sendRecognizedUserText(text)
        } else {
          accumulatedRecognitionText = ''
          void transcribeCapturedSpeech()
        }
      }
      return
    }

    isListening.value = false
  }
}

const stopAudioPipeline = async () => {
  clearPlaybackQueue()
  if (processorNode) { processorNode.disconnect(); processorNode.onaudioprocess = null; processorNode = null }
  if (sourceNode) { sourceNode.disconnect(); sourceNode = null }
  if (sinkNode) { sinkNode.disconnect(); sinkNode = null }
  if (mediaStream) { mediaStream.getTracks().forEach((t) => t.stop()); mediaStream = null }
  if (audioContext) { await audioContext.close(); audioContext = null }
  capturedSpeechChunks = []
  speechActivityStarted = false
  silenceChunkCount = 0
  consecutiveSpeechChunks = 0
  isListening.value = false
  isModelSpeaking.value = false
}

// ─── Speech recognition ───────────────────────────────────────────────────────

const detachRawSocketListener = () => {
  const rawSocket = (session as { conn?: { ws?: WebSocket } } | null)?.conn?.ws
  if (rawSocket && rawSocketListener) rawSocket.removeEventListener('message', rawSocketListener)
  rawSocketListener = null
}

const stopSpeechRecognition = () => {
  speechRecognitionEnabled = false
  speechRecognitionActive = false
  voiceInputBlocked.value = false
  isListening.value = false
  accumulatedRecognitionText = ''
  finalizeTranscript('You')
  if (speechRecognition) speechRecognition.stop()
}

const startSpeechRecognition = (): boolean => {
  const RecognitionCtor = window.SpeechRecognition ?? window.webkitSpeechRecognition
  if (!RecognitionCtor) { voiceInputBlocked.value = true; return false }

  if (!speechRecognition) {
    speechRecognition = new RecognitionCtor()
    speechRecognition.continuous = true
    speechRecognition.interimResults = true
    speechRecognition.lang = 'en-US'

    speechRecognition.onresult = (event) => {
      if (isModelSpeaking.value) return

      let interimTranscript = ''
      const finalSegments: string[] = []

      for (let i = event.resultIndex; i < event.results.length; i++) {
        const result = event.results[i]
        const transcript = result[0]?.transcript?.trim()
        if (!transcript) continue
        if (result.isFinal) finalSegments.push(transcript)
        else interimTranscript += `${transcript} `
      }

      const interim = interimTranscript.trim()
      isListening.value = interim.length > 0 || finalSegments.length > 0

      if (interim) {
        const display = accumulatedRecognitionText ? `${accumulatedRecognitionText} ${interim}` : interim
        replaceTranscriptText('You', display)
      }

      if (finalSegments.length) {
        const finalText = finalSegments.join(' ')

        if (interruptingModeEnabled.value) {
          onModeBuffer = onModeBuffer ? `${onModeBuffer} ${finalText}` : finalText
          replaceTranscriptText('You', onModeBuffer)

          if (onModeFlushTimer !== null) clearTimeout(onModeFlushTimer)
          onModeFlushTimer = setTimeout(() => {
            onModeFlushTimer = null
            if (!onModeBuffer) return
            const text = onModeBuffer
            onModeBuffer = ''
            capturedSpeechChunks = []
            isListening.value = false
            const wordCount = text.trim().split(/\s+/).filter(Boolean).length
            if (wordCount < ON_MODE_MIN_WORDS) { addUserEntry(text); return }
            sendRecognizedUserText(text)
          }, ON_MODE_FLUSH_DELAY_MS)
        } else {
          accumulatedRecognitionText = accumulatedRecognitionText
            ? `${accumulatedRecognitionText} ${finalText}`
            : finalText
          replaceTranscriptText('You', accumulatedRecognitionText)
        }
      }
    }

    speechRecognition.onerror = (event) => {
      if (event.error === 'service-not-allowed' || event.error === 'not-allowed') {
        speechRecognitionEnabled = false
        voiceInputBlocked.value = true
      }
    }

    speechRecognition.onend = () => {
      speechRecognitionActive = false
      isListening.value = false
      if (speechRecognitionEnabled && connectionState.value === 'connected' && !isModelSpeaking.value) {
        try { speechRecognition?.start(); speechRecognitionActive = true } catch { /* ignore */ }
      }
    }
  }

  speechRecognitionEnabled = true
  voiceInputBlocked.value = false
  if (!speechRecognitionActive) {
    try { speechRecognition.start(); speechRecognitionActive = true; return true }
    catch { speechRecognitionEnabled = false; return false }
  }
  return true
}

// ─── Session lifecycle ────────────────────────────────────────────────────────

const startLiveSession = async () => {
  if (isBusy.value || isConnected.value) return

  connectionState.value = 'connecting'
  transcriptEntries.value = []
  hasSeenServerMessage = false
  serverMessageCount = 0
  rawSocketMessageCount = 0

  try {
    const tokenResponse = await liveVoiceService.createSessionToken()

    const ai = new GoogleGenAI({
      apiKey: tokenResponse.token,
      apiVersion: 'v1alpha',
      httpOptions: { apiVersion: 'v1alpha' },
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
        onopen: () => {},
        onmessage: (message) => { void handleServerMessage(message) },
        onerror: (event) => {
          connectionState.value = 'error'
          console.error('Gemini Live error:', event)
        },
        onclose: (event) => {
          if (connectionState.value === 'connected') {
            connectionState.value = 'idle'
          }
        },
      },
    })

    // Raw socket debug listener (first few messages).
    const rawSocket = (session as { conn?: { ws?: WebSocket } } | null)?.conn?.ws
    if (rawSocket?.addEventListener) {
      rawSocketListener = (event: MessageEvent) => {
        rawSocketMessageCount++
        if (rawSocketMessageCount <= 5) {
          // silent debug only
        }
      }
      rawSocket.addEventListener('message', rawSocketListener)
    }

    voiceInputBlocked.value = false
    await startMicrophone()
    startSpeechRecognition()

    connectionState.value = 'connected'

    // Kick off the conversation.
    const mode = selectedMode.value || 'Socratic Evaluation'
    const topic = sessionTitle.value.trim() || 'the chosen subject'
    session.sendClientContent({
      turns: [{
        role: 'user',
        parts: [{
          text: `The student wants to practice: ${topic}. Guide them using the ${mode} style. Greet them and invite them to begin in their own words.`,
        }],
      }],
      turnComplete: true,
    })
  } catch (error) {
    console.error('Failed to start live session:', error)
    connectionState.value = 'error'
    stopSpeechRecognition()
    await stopAudioPipeline()
    detachRawSocketListener()
    if (session) { session.close(); session = null }
  }
}

const stopLiveSession = async () => {
  if (session) { detachRawSocketListener(); session.close(); session = null }
  stopSpeechRecognition()
  await stopAudioPipeline()
  connectionState.value = 'idle'
}

// ─── UI helpers ───────────────────────────────────────────────────────────────

function toggleDropdown() { dropdownOpen.value = !dropdownOpen.value }
function selectMode(option: string) { selectedMode.value = option; dropdownOpen.value = false }

function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files) uploadedFiles.value.push(...Array.from(input.files))
}
function onDrop(e: DragEvent) {
  isDragging.value = false
  if (e.dataTransfer?.files) uploadedFiles.value.push(...Array.from(e.dataTransfer.files))
}
function removeFile(index: number) { uploadedFiles.value.splice(index, 1) }

function handleEscape(e: KeyboardEvent) { if (e.key === 'Escape') personalizing.value = false }
function handleOutsideClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (!target.closest('.dropdown-wrapper')) dropdownOpen.value = false
}

// ─── Lifecycle ────────────────────────────────────────────────────────────────

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
  document.addEventListener('keydown', handleEscape)

  if (splineCanvas.value) {
    splineApp = new Application(splineCanvas.value)
    splineApp.load('https://prod.spline.design/VZSS2xX-gze-TSeK/scene.splinecode').then(() => {
      const canvas = splineCanvas.value
      if (!canvas) return
      let isSynthetic = false
      const dispatchCentered = (clientX: number) => {
        const rect = canvas.getBoundingClientRect()
        const centerY = rect.top + rect.height / 2
        isSynthetic = true
        canvas.dispatchEvent(new MouseEvent('mousemove', {
          bubbles: true, cancelable: true, clientX, clientY: centerY,
          screenX: clientX, screenY: centerY, movementX: 0, movementY: 0,
        }))
        isSynthetic = false
      }
      canvas.addEventListener('mousemove', (e: MouseEvent) => {
        if (isSynthetic) return
        e.stopImmediatePropagation()
        dispatchCentered(e.clientX)
      }, { capture: true })
      const rect = canvas.getBoundingClientRect()
      dispatchCentered(rect.left + rect.width / 2)
    })
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
  document.removeEventListener('keydown', handleEscape)
  if (splineApp) { splineApp.dispose(); splineApp = null }
  void stopLiveSession()
})
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Red+Hat+Display:wght@400;500;600;700&display=swap");

.build-page {
  min-height: 100vh;
  width: 100vw;
  background: #000000;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.back-btn {
  position: fixed;
  top: 2rem;
  left: 2rem;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  color: rgba(247, 247, 242, 0.7);
  text-decoration: none;
  transition: color 0.2s ease;
}

.back-btn:hover { color: #F7F7F2; }

.back-arrow { width: 20px; height: 20px; flex-shrink: 0; }

.back-label {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.85rem;
  letter-spacing: 0.04em;
  white-space: nowrap;
  opacity: 0;
  transform: translateX(-6px);
  transition: opacity 0.25s ease, transform 0.25s ease;
  pointer-events: none;
}

.back-btn:hover .back-label { opacity: 1; transform: translateX(0); }

.spline-bg {
  position: fixed;
  top: 15vh; left: 0; right: 0; bottom: 0;
  width: 100%; height: 85vh;
  z-index: 0;
  transition: transform 1.2s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  transform-origin: center center;
}

.hidden-ui {
  opacity: 0 !important;
  pointer-events: none !important;
  transition: opacity 0.7s ease !important;
}

.spline-zoom { transform: scale(1.18); }

.personalize-btn-wrap {
  position: fixed;
  top: 52%; left: 50%;
  transform: translate(-50%, -50%);
  z-index: 4;
  pointer-events: all;
}

.personalize-btn {
  display: inline-flex;
  align-items: center;
  padding: 0.65rem 1.5rem;
  border-radius: 9999px;
  background: rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  color: rgba(247, 247, 242, 0.8);
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.82rem;
  font-weight: 500;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  cursor: pointer;
  transition: border-color 0.2s ease, color 0.2s ease, background 0.2s ease;
  white-space: nowrap;
}

.personalize-btn:hover {
  border-color: rgba(255, 255, 255, 0.5);
  color: #F7F7F2;
  background: rgba(0, 0, 0, 0.4);
}

.build-hero {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  min-height: 100vh;
  padding: clamp(3rem, 7vw, 5rem) clamp(3rem, 7vw, 6rem) clamp(4rem, 8vw, 6rem);
  pointer-events: none;
}

.build-hero-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  animation: fadeSlideIn 0.9s cubic-bezier(0.25, 0.46, 0.45, 0.94) 0.3s both;
}

.build-title {
  font-family: "Times New Roman", "Times", serif;
  font-size: clamp(1.6rem, 3.5vw, 3rem);
  font-weight: 400;
  line-height: 1.2;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #F7F7F2;
  text-align: center;
  white-space: nowrap;
  margin: 0;
}

.title-golden {
  color: transparent;
  background-image: linear-gradient(
    to right,
    #8B6914 0%, #cb9b51 22%, #f6e27a 45%, #f6f2c0 50%, #f6e27a 55%, #cb9b51 78%, #8B6914 100%
  );
  -webkit-background-clip: text;
  background-clip: text;
}

.build-subtitle {
  font-family: 'Red Hat Display', sans-serif;
  font-size: clamp(1rem, 1.8vw, 1.3rem);
  color: rgba(247, 247, 242, 0.6);
  text-align: center;
  margin: 0;
  letter-spacing: 0.02em;
}

.build-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 1rem;
  pointer-events: all;
}

.btn-dropdown,
.btn-live {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.85rem 2rem;
  border-radius: 9999px;
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.95rem;
  letter-spacing: 0.05em;
  cursor: pointer;
  white-space: nowrap;
  transition: transform 180ms ease, box-shadow 220ms ease, border-color 220ms ease;
}

.dropdown-wrapper { position: relative; }

.btn-dropdown {
  gap: 0.75rem;
  background: transparent;
  color: #F7F7F2;
  border: 1px solid rgba(247, 247, 242, 0.3);
  min-width: 220px;
  justify-content: space-between;
}

.btn-dropdown:hover { transform: translateY(-1px); border-color: rgba(247, 247, 242, 0.6); }

.placeholder { color: rgba(247, 247, 242, 0.4); }

.chevron { width: 18px; height: 18px; flex-shrink: 0; transition: transform 0.25s ease; }
.chevron.open { transform: rotate(180deg); }

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  left: 0; right: 0;
  background: rgba(15, 15, 15, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  overflow: hidden;
  z-index: 200;
  min-width: 220px;
  backdrop-filter: blur(12px);
}

.dropdown-item {
  display: block; width: 100%;
  padding: 0.9rem 1.4rem;
  background: transparent; border: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  color: rgba(247, 247, 242, 0.75);
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.95rem; font-weight: 400;
  text-align: left; cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}

.dropdown-item:last-child { border-bottom: none; }
.dropdown-item:hover { background: rgba(255, 255, 255, 0.05); color: #F7F7F2; }
.dropdown-item.active { background: transparent; color: #F7F7F2; }

.dropdown-enter-active, .dropdown-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.dropdown-enter-from, .dropdown-leave-to { opacity: 0; transform: translateY(-6px); }

.btn-live {
  gap: 0.6rem;
  background: #F7F7F2;
  color: #000000;
  border: 1px solid #F7F7F2;
}

.btn-live:hover { transform: translateY(-1px); box-shadow: 0 18px 44px rgba(0, 0, 0, 0.4); }
.btn-live:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }

.btn-live-active {
  background: rgba(255, 80, 80, 0.15);
  color: #ff6b6b;
  border-color: rgba(255, 80, 80, 0.4);
}

.btn-live-active:hover { box-shadow: 0 18px 44px rgba(255, 80, 80, 0.2); }

.mic-icon { width: 18px; height: 18px; flex-shrink: 0; }

/* Live status indicator */
.live-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  pointer-events: none;
}

.live-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: rgba(247, 247, 242, 0.3);
  transition: background 0.3s ease, box-shadow 0.3s ease;
}

.live-dot.listening {
  background: #6ee7b7;
  box-shadow: 0 0 8px #6ee7b7;
  animation: pulse 1.2s ease-in-out infinite;
}

.live-dot.speaking {
  background: #cb9b51;
  box-shadow: 0 0 8px #cb9b51;
  animation: pulse 0.8s ease-in-out infinite;
}

.live-label {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.78rem;
  color: rgba(247, 247, 242, 0.5);
  letter-spacing: 0.04em;
}

/* Right wrapper */
.transcript-wrapper {
  position: fixed;
  top: 13rem; bottom: 4rem; right: 2.5rem;
  display: flex; flex-direction: row; align-items: center;
  gap: 0; z-index: 5;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
}

.transcript-wrapper.hidden { transform: translateX(calc(360px + 2.5rem)); }

.right-panels {
  display: flex; flex-direction: column;
  height: 100%; gap: 1rem;
  pointer-events: all; width: 360px;
}

.transcript-panel {
  flex: 1; min-height: 0;
  display: flex; flex-direction: column;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  overflow: hidden;
}

.transcript-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 1rem 1.25rem 0.75rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  flex-shrink: 0;
}

.transcript-label {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.8rem; font-weight: 600;
  letter-spacing: 0.1em; text-transform: uppercase;
  color: rgba(247, 247, 242, 0.5);
}

.transcript-status {
  display: flex; align-items: center; gap: 0.4rem;
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.75rem;
  color: rgba(247, 247, 242, 0.35);
}

.transcript-status.active { color: rgba(247, 247, 242, 0.6); }

.status-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: rgba(247, 247, 242, 0.25);
}

.transcript-status.active .status-dot {
  background: #6ee7b7;
  box-shadow: 0 0 6px #6ee7b7;
  animation: pulse 2s ease-in-out infinite;
}

.transcript-body {
  flex: 1; overflow-y: auto;
  padding: 1rem 1.25rem;
  display: flex; flex-direction: column; gap: 1rem;
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.1) transparent;
}

.transcript-body::-webkit-scrollbar { width: 4px; }
.transcript-body::-webkit-scrollbar-track { background: transparent; }
.transcript-body::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 2px; }

.transcript-empty {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.82rem;
  color: rgba(247, 247, 242, 0.25);
  text-align: center; line-height: 1.6; padding: 1rem 0;
}

.transcript-msg { display: flex; flex-direction: column; gap: 0.25rem; }

.msg-role {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.7rem; font-weight: 600;
  letter-spacing: 0.06em; text-transform: uppercase;
  color: rgba(247, 247, 242, 0.35);
}

.transcript-msg.ai .msg-role { color: rgba(203, 155, 81, 0.7); }

.msg-text {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.875rem; line-height: 1.6;
  color: rgba(247, 247, 242, 0.8); margin: 0;
  transition: opacity 0.2s ease;
}

.msg-text.pending { opacity: 0.6; }

.panel-tab {
  flex-shrink: 0; width: 22px; height: 52px;
  display: flex; align-items: center; justify-content: center;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.2);
  cursor: pointer; color: rgba(247, 247, 242, 0.5);
  transition: color 0.2s ease, background 0.2s ease;
  pointer-events: all;
}

.panel-tab svg { width: 12px; height: 12px; flex-shrink: 0; }
.panel-tab:hover { color: #F7F7F2; background: rgba(255, 255, 255, 0.12); }

.session-wrapper {
  position: fixed;
  top: 23rem; bottom: 4rem; left: 2.5rem;
  display: flex; flex-direction: row; align-items: center;
  z-index: 5;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
}

.session-wrapper.hidden .panel-tab,
.transcript-wrapper.hidden .panel-tab { pointer-events: all; }

.session-wrapper.hidden { transform: translateX(calc(-300px - 2.5rem)); }

.session-wrapper .panel-tab { border-left: none; border-radius: 0 10px 10px 0; }

.session-panel {
  width: 300px; height: 100%;
  display: flex; flex-direction: column;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  overflow: hidden;
  pointer-events: all;
}

.session-header {
  display: flex; align-items: center;
  padding: 1rem 1.25rem 0.75rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  flex-shrink: 0;
}

.session-label {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.8rem; font-weight: 600;
  letter-spacing: 0.1em; text-transform: uppercase;
  color: rgba(247, 247, 242, 0.5);
}

.session-body {
  flex: 1; overflow-y: auto;
  padding: 1.25rem;
  display: flex; flex-direction: column; gap: 1.5rem;
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.1) transparent;
}

.session-body::-webkit-scrollbar { width: 4px; }
.session-body::-webkit-scrollbar-track { background: transparent; }
.session-body::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 2px; }

.session-field { display: flex; flex-direction: column; gap: 0.5rem; }

.field-label {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.75rem; font-weight: 600;
  letter-spacing: 0.06em; text-transform: uppercase;
  color: rgba(247, 247, 242, 0.4);
}

.field-hint {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.78rem;
  color: rgba(247, 247, 242, 0.3);
  margin: 0.2rem 0 0; line-height: 1.4;
}

.field-input {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  padding: 0.65rem 0.9rem;
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.875rem;
  color: #F7F7F2; outline: none;
  transition: border-color 0.2s ease;
}

.field-input::placeholder { color: rgba(247, 247, 242, 0.25); }
.field-input:focus { border-color: rgba(255, 255, 255, 0.25); }

.file-drop {
  display: flex; flex-direction: column; align-items: center; gap: 0.4rem;
  padding: 1rem;
  border: 1px dashed rgba(255, 255, 255, 0.18);
  border-radius: 10px; cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.file-drop svg { width: 22px; height: 22px; color: rgba(247, 247, 242, 0.3); }
.file-drop span { font-family: 'Red Hat Display', sans-serif; font-size: 0.8rem; color: rgba(247, 247, 242, 0.35); }
.file-drop u { color: rgba(247, 247, 242, 0.6); text-decoration-color: rgba(247, 247, 242, 0.3); }
.file-drop:hover, .file-drop.dragging { border-color: rgba(255, 255, 255, 0.4); background: rgba(255, 255, 255, 0.04); }

.hidden-file-input { display: none; }

.file-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 0.4rem; }

.file-item {
  display: flex; align-items: center; gap: 0.5rem;
  padding: 0.45rem 0.6rem;
  background: rgba(255, 255, 255, 0.05); border-radius: 8px;
}

.file-item svg { width: 14px; height: 14px; color: rgba(247, 247, 242, 0.4); flex-shrink: 0; }

.file-name {
  flex: 1;
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.78rem; color: rgba(247, 247, 242, 0.7);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

.file-remove {
  background: transparent; border: none; cursor: pointer; padding: 0;
  display: flex; color: rgba(247, 247, 242, 0.25); transition: color 0.15s ease;
}

.file-remove:hover { color: rgba(247, 247, 242, 0.7); }
.file-remove svg { width: 12px; height: 12px; }

.toggle-row { display: flex; align-items: flex-start; justify-content: space-between; gap: 1rem; }

.toggle {
  flex-shrink: 0; width: 42px; height: 24px;
  border-radius: 9999px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.08);
  cursor: pointer; position: relative;
  transition: background 0.25s ease, border-color 0.25s ease;
  margin-top: 2px;
}

.toggle.on { background: rgba(247, 247, 242, 0.85); border-color: transparent; }

.toggle-thumb {
  position: absolute; top: 3px; left: 3px;
  width: 16px; height: 16px; border-radius: 50%;
  background: rgba(247, 247, 242, 0.5);
  transition: transform 0.25s ease, background 0.25s ease;
}

.toggle.on .toggle-thumb { transform: translateX(18px); background: #000000; }

.transcript-wrapper .panel-tab { border-right: none; border-radius: 10px 0 0 10px; }

.fallback-panel { flex-shrink: 0; pointer-events: all; }

.fallback-form {
  display: flex; align-items: center; gap: 0.5rem;
  background: rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(6px); -webkit-backdrop-filter: blur(6px);
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 9999px;
  padding: 0.5rem 0.5rem 0.5rem 1.25rem;
}

.fallback-input {
  flex: 1; background: transparent; border: none; outline: none;
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.9rem; color: #F7F7F2; letter-spacing: 0.02em;
}

.fallback-input::placeholder { color: rgba(247, 247, 242, 0.35); }
.fallback-input:disabled { opacity: 0.4; }

.fallback-send {
  display: flex; align-items: center; justify-content: center;
  width: 34px; height: 34px; border-radius: 50%; border: none;
  background: rgba(255, 255, 255, 0.12); color: rgba(247, 247, 242, 0.6);
  cursor: pointer; flex-shrink: 0;
  transition: background 0.2s ease, color 0.2s ease;
}

.fallback-send:not(:disabled):hover { background: #F7F7F2; color: #000000; }
.fallback-send:disabled { opacity: 0.3; cursor: default; }
.fallback-send svg { width: 15px; height: 15px; }

/* Personalize overlay */
.personalize-overlay {
  position: fixed;
  inset: 0;
  z-index: 15;
  pointer-events: none;
}

.personalize-enter-active,
.personalize-leave-active {
  transition: opacity 0.5s ease;
}
.personalize-enter-from,
.personalize-leave-to {
  opacity: 0;
}

/* Save button */
.p-save-wrap {
  position: absolute;
  bottom: 6%;
  left: 50%;
  transform: translateX(-50%);
  pointer-events: all;
}

.p-save-btn {
  display: inline-flex;
  align-items: center;
  padding: 0.85rem 2.5rem;
  border-radius: 9999px;
  background: #F7F7F2;
  color: #000000;
  border: none;
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.95rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 220ms ease;
}

.p-save-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.4);
}

/* Cards */
.p-card {
  position: absolute;
  width: 300px;
  background: rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 20px;
  padding: 1rem 1.2rem;
  pointer-events: all;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.top-left    { top: 24%;    left: 24%; }
.top-right   { top: 24%;    right: 24%; }
.bottom-left { bottom: 18%; left: 18%; }
.bottom-right{ bottom: 18%; right: 18%; }

.p-card-title {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.85rem;
  font-weight: 700;
  color: rgba(247, 247, 242, 0.9);
  margin: 0;
  letter-spacing: 0.03em;
}

.p-card-desc {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.75rem;
  color: rgba(247, 247, 242, 0.4);
  margin: 0 0 0.4rem;
  line-height: 1.5;
}

.p-card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* Personalize dropdowns */
.p-dropdown-wrapper {
  position: relative;
}

.p-dropdown-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.55rem 0.85rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 9999px;
  color: rgba(247, 247, 242, 0.85);
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.82rem;
  font-weight: 500;
  cursor: pointer;
  transition: border-color 0.2s ease;
}

.p-dropdown-btn:hover {
  border-color: rgba(255, 255, 255, 0.4);
}

.p-dropdown-btn svg {
  width: 14px;
  height: 14px;
  color: rgba(247, 247, 242, 0.4);
  flex-shrink: 0;
}

.p-dropdown-menu {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  overflow: hidden;
  z-index: 50;
}

.p-dropdown-menu--up {
  top: auto;
  bottom: calc(100% + 6px);
}

.p-dropdown-item {
  display: block;
  width: 100%;
  padding: 0.65rem 0.85rem;
  background: transparent;
  border: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  color: rgba(247, 247, 242, 0.7);
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.82rem;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}

.p-dropdown-item:last-child { border-bottom: none; }
.p-dropdown-item:hover { background: rgba(255, 255, 255, 0.06); color: #F7F7F2; }
.p-dropdown-item.active { color: rgba(247, 247, 242, 0.95); }

/* Personalize toggle */
.p-toggle {
  flex-shrink: 0;
  width: 42px;
  height: 24px;
  border-radius: 9999px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.08);
  cursor: pointer;
  position: relative;
  transition: background 0.25s ease, border-color 0.25s ease;
}

.p-toggle.on {
  background: rgba(247, 247, 242, 0.85);
  border-color: transparent;
}

.p-toggle-thumb {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: rgba(247, 247, 242, 0.45);
  transition: transform 0.25s ease, background 0.25s ease;
}

.p-toggle.on .p-toggle-thumb {
  transform: translateX(18px);
  background: #000;
}

@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
