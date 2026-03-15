<template>
  <section class="live-page">
    <header class="hero-card">
      <div>
        <p class="eyebrow">Gemini Live Tutor</p>
        <h1>Socratic Voice Tutor</h1>
        <p class="hero-copy">
          Pick a tutoring mode, speak naturally, and get real-time Socratic feedback. Your full
          conversation is transcribed live so you can review everything that was said.
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

    <!-- Live conversation state bar — visible when connected -->
    <div v-if="isConnected" class="conversation-bar">
      <div class="conversation-state" :class="conversationPhase">
        <div class="state-icon">
          <span v-if="conversationPhase === 'speaking'" class="wave-icon" aria-hidden="true">
            <span class="wave-bar"></span>
            <span class="wave-bar"></span>
            <span class="wave-bar"></span>
            <span class="wave-bar"></span>
            <span class="wave-bar"></span>
          </span>
          <span v-else-if="conversationPhase === 'listening'" class="pulse-icon" aria-hidden="true">
            <span class="pulse-ring"></span>
            <span class="mic-dot"></span>
          </span>
          <span v-else-if="conversationPhase === 'processing'" class="dots-icon" aria-hidden="true">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </span>
          <span v-else class="idle-dot" aria-hidden="true"></span>
        </div>
        <div class="state-text">
          <strong>{{ conversationPhaseLabel }}</strong>
          <span class="state-detail">{{ statusMessage }}</span>
        </div>
      </div>

      <button
        class="interrupt-toggle-button"
        :class="{ 'toggle-on': interruptingModeEnabled }"
        type="button"
        :title="interruptingModeEnabled ? 'Socratica may interrupt you. Click to disable.' : 'Socratica waits for you to finish. Click to enable interruptions.'"
        @click="toggleInterruptingMode"
      >
        <span class="toggle-track">
          <span class="toggle-thumb"></span>
        </span>
        <span class="toggle-label">Interrupting: <strong>{{ interruptingModeEnabled ? 'ON' : 'OFF' }}</strong></span>
      </button>
    </div>

    <section class="panel-grid">
      <!-- Left: Session setup -->
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
          placeholder="Example: Explain photosynthesis for an oral exam and challenge me whenever I skip a causal step."
        />

        <div class="status-row">
          <span class="status-chip" :class="statusClass">{{ connectionLabel }}</span>
          <span class="meta-text">{{ !isConnected ? statusMessage : '' }}</span>
        </div>

        <div class="status-list">
          <div class="status-item">
            <span class="status-label">Listening</span>
            <strong :class="{ 'active-indicator': isListening }">{{ isListening ? 'Yes' : 'No' }}</strong>
          </div>
          <div class="status-item">
            <span class="status-label">Socratica speaking</span>
            <strong :class="{ 'active-indicator': isModelSpeaking }">{{ isModelSpeaking ? 'Yes' : 'No' }}</strong>
          </div>
          <div class="status-item">
            <span class="status-label">Live model</span>
            <strong>{{ liveModel }}</strong>
          </div>
          <div class="status-item">
            <span class="status-label">Tutor mode</span>
            <strong>{{ tutorMode }}</strong>
          </div>
          <div class="status-item">
            <span class="status-label">Interrupting mode</span>
            <button
              class="inline-toggle"
              :class="{ 'toggle-on': interruptingModeEnabled }"
              type="button"
              @click="toggleInterruptingMode"
            >
              {{ interruptingModeEnabled ? 'ON' : 'OFF' }}
            </button>
          </div>
        </div>

        <div class="interrupt-info script-note">
          <span class="status-label">About interrupting mode</span>
          <p v-if="interruptingModeEnabled" class="helper-text">
            <strong>ON:</strong> Socratica may gently interrupt you with follow-up questions or
            corrections. You can always interrupt Socratica at any time.
          </p>
          <p v-else class="helper-text">
            <strong>OFF:</strong> Socratica will wait for you to finish speaking before responding.
            Fully user-led. You can still always interrupt Socratica.
          </p>
        </div>

        <div class="script-note fallback-panel">
          <span class="status-label">Text input fallback</span>
          <p v-if="voiceInputBlocked" class="helper-text">
            Voice input is blocked by this browser. Type below and Socratica will still answer out loud.
          </p>
          <p v-else class="helper-text">
            If speech recognition does not work, type here to keep the session going.
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

      <!-- Center: Live conversation transcript -->
      <article class="panel transcript-panel">
        <div class="panel-header">
          <h2>Conversation</h2>
          <div class="transcript-actions">
            <span v-if="transcriptEntries.length" class="message-count">
              {{ transcriptEntries.length }} {{ transcriptEntries.length === 1 ? 'message' : 'messages' }}
            </span>
            <button class="ghost-button" type="button" @click="clearTranscript">Clear</button>
          </div>
        </div>

        <div ref="transcriptStreamRef" class="transcript-stream">
          <p
            v-if="!transcriptEntries.length"
            class="empty-state"
          >
            The conversation will appear here once the session starts.
          </p>

          <div
            v-for="entry in transcriptEntries"
            :key="entry.id"
            class="transcript-entry"
            :class="[
              entry.speaker === 'You' ? 'user-entry' : 'model-entry',
              { 'pending-entry': entry.pending },
            ]"
          >
            <div class="entry-header">
              <span class="speaker">{{ entry.speaker }}</span>
              <span v-if="!entry.pending && entry.time" class="entry-time">{{ entry.time }}</span>
              <span v-else-if="entry.pending" class="entry-time pending-label">
                {{ entry.speaker === 'You' && isTranscribing ? 'transcribing…' : 'speaking…' }}
              </span>
            </div>
            <p>{{ entry.text }}</p>
          </div>
        </div>
      </article>

      <!-- Right: Demo script + saved sessions -->
      <div class="side-panels">
        <article class="panel">
          <h2>Demo Script</h2>
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
      </div>
    </section>

    <!-- Collapsible debug events -->
    <details class="panel events-details">
      <summary class="events-summary">Live Events <span class="event-count">({{ eventLog.length }})</span></summary>
      <ul class="event-list">
        <li v-for="(entry, index) in eventLog" :key="`${entry}-${index}`">
          {{ entry }}
        </li>
      </ul>
    </details>
  </section>
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

import {
  liveVoiceService,
  type SavedTutorSession,
  type TutorSessionPayload,
  type TutorTranscriptEntry,
} from '@/services/liveVoiceService'

type ConnectionState = 'idle' | 'connecting' | 'connected' | 'error'
type TranscriptSpeaker = 'You' | 'Socratica'
type ConversationPhase = 'idle' | 'speaking' | 'listening' | 'processing' | 'waiting'

interface TranscriptEntry {
  id: string
  speaker: TranscriptSpeaker
  text: string
  time: string
  pending: boolean
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
      'This mode turns the tutor into an interviewer who actively tests the student\'s evidence, clarity, and structure.',
    demoSteps: [
      'Introduce the interview scenario and begin answering.',
      'Let the AI interrupt when your answer lacks specifics.',
      'Refine the answer in real time with more concrete detail.',
      'Save the session to show how the app supports repeated interview practice.',
    ],
  },
]

const router = useRouter()

// Connection & session state
const connectionState = ref<ConnectionState>('idle')
const statusMessage = ref('Ready to start a live voice tutoring session.')
const liveModel = ref('Waiting for backend token...')

// Voice activity state
const isListening = ref(false)
const isModelSpeaking = ref(false)
const isTranscribing = ref(false)

// Session management state
const isSaving = ref(false)
const transcriptEntries = ref<TranscriptEntry[]>([])
const eventLog = ref<string[]>([
  'Pick a tutor preset, click "Start Live Voice", allow microphone access, and begin speaking.',
])
const savedSessions = ref<SavedTutorSession[]>([])
const manualUserInput = ref('')
const voiceInputBlocked = ref(false)

// Interrupting mode: controls whether Socratica may interrupt the user.
// The user may always interrupt Socratica regardless of this setting.
const interruptingModeEnabled = ref(true)

// Preset / session config
const selectedPresetId = ref(tutorPresets[0].id)
const sessionTitle = ref(tutorPresets[0].title)
const studyTopic = ref(tutorPresets[0].topic)
const learningGoal = ref(tutorPresets[0].learningGoal)
const tutorMode = ref(tutorPresets[0].tutorMode)
const activeSessionId = ref<string | undefined>(undefined)

// Transcript auto-scroll
const transcriptStreamRef = ref<HTMLElement | null>(null)

// Non-reactive audio/session handles
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
let consecutiveSpeechChunks = 0  // how many consecutive above-threshold chunks we've seen
// OFF mode only: accumulates isFinal speech recognition segments until the
// ScriptProcessor detects a long silence, then sends the full accumulated text.
let accumulatedRecognitionText = ''
// ON mode only: briefly buffers isFinal segments before flushing to Gemini so
// she receives a meaningful clause (not a single word) for semantic evaluation.
let onModeBuffer = ''
let onModeFlushTimer: ReturnType<typeof setTimeout> | null = null
// Set to true when the user's mic audio interrupts Socratica mid-speech.
// Used to skip the 600 ms echo-decay delay and restart recognition immediately
// so the user's interruption appears in the live transcript right away.
let userInterruptedSocratica = false

// ─── Audio constants ──────────────────────────────────────────────────────────
// Each ScriptProcessor chunk is 4096 samples.
// At 48 kHz ≈ 85 ms/chunk; at 44.1 kHz ≈ 93 ms/chunk.

// A single chunk above threshold could be noise (click, breath, AC hum).
// Require this many CONSECUTIVE above-threshold chunks before treating it as
// real speech. This prevents noise spikes from killing Socratica's audio.
const SPEECH_LEVEL_THRESHOLD = 0.02
const SPEECH_CONFIRM_CHUNKS = 3    // ≈ 255 ms of sustained sound → real speech

// Silence needed after speech ends before we transcribe.
// Interrupting ON  → snappy ~750 ms hand-off.
// Interrupting OFF → patient ~2 s wait so mid-sentence pauses don't fire early.
const SILENCE_CHUNKS_INTERRUPTING_ON = 9   // ≈ 750 ms
const SILENCE_CHUNKS_INTERRUPTING_OFF = 24  // ≈ 2 s

// ON mode: how long (ms) to wait after the last isFinal segment before flushing
// the buffer to Gemini. Brief enough to feel reactive; long enough to group
// related clauses so Gemini evaluates meaning, not isolated words.
const ON_MODE_FLUSH_DELAY_MS = 450

// ─── Computed ────────────────────────────────────────────────────────────────

const activePreset = computed(
  () => tutorPresets.find((preset) => preset.id === selectedPresetId.value) ?? tutorPresets[0]
)
const activeDemoSteps = computed(() => activePreset.value.demoSteps)
const isBusy = computed(() => connectionState.value === 'connecting')
const isConnected = computed(() => connectionState.value === 'connected')

const connectionLabel = computed(() => {
  switch (connectionState.value) {
    case 'connecting': return 'Connecting'
    case 'connected': return 'Live'
    case 'error': return 'Error'
    default: return 'Idle'
  }
})

const statusClass = computed(() => ({
  idle: connectionState.value === 'idle',
  connecting: connectionState.value === 'connecting',
  connected: connectionState.value === 'connected',
  error: connectionState.value === 'error',
}))

const conversationPhase = computed<ConversationPhase>(() => {
  if (!isConnected.value) return 'idle'
  if (isModelSpeaking.value) return 'speaking'
  if (isListening.value) return 'listening'
  if (isTranscribing.value) return 'processing'
  return 'waiting'
})

const conversationPhaseLabel = computed(() => {
  switch (conversationPhase.value) {
    case 'speaking': return 'Socratica is speaking'
    case 'listening': return 'Listening to you'
    case 'processing': return 'Processing your answer'
    case 'waiting': return 'Waiting for you'
    default: return 'Session ready'
  }
})

// ─── Helpers ─────────────────────────────────────────────────────────────────

const addEvent = (message: string) => {
  const timestamp = new Date().toLocaleTimeString()
  eventLog.value = [`${timestamp} — ${message}`, ...eventLog.value].slice(0, 20)
}

const scrollTranscriptToBottom = async () => {
  await nextTick()
  if (transcriptStreamRef.value) {
    transcriptStreamRef.value.scrollTop = transcriptStreamRef.value.scrollHeight
  }
}

// Replace the pending bubble's text for a speaker.
// Used for CUMULATIVE sources (browser speech recognition interim results) where
// each event provides the full current interim text, not just a new chunk.
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

// Stream live text into the current speaker's pending bubble.
// Gemini sends incremental text chunks, so we APPEND each chunk to build up the full response.
// If the last entry is already a pending bubble for this speaker, append to it.
// Otherwise create a new pending bubble — this is what starts a new chat message.
const streamTranscript = (speaker: TranscriptSpeaker, text: string) => {
  if (!text) return

  const entries = transcriptEntries.value
  const last = entries[entries.length - 1]

  if (last && last.speaker === speaker && last.pending) {
    // Append the new chunk directly (the API includes its own spacing/punctuation).
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

// Finalize the most recent pending bubble for a speaker.
// We ALWAYS prefer the text we accumulated via streamTranscript because Gemini
// sends multiple finished events — one per sentence — and each carries only that
// sentence as finalText. Using finalText would silently truncate the bubble to
// just the last sentence. The accumulated text is the complete response.
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

// Add a finalized entry for the user directly (used when we transcribe locally
// and send text to Gemini ourselves — no inputTranscription event follows).
// Merges into an existing pending You bubble if one is already open.
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

  // Avoid exact duplicate consecutive entries.
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

const buildTutorPrompt = () => {
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
    `Tutor mode: ${tutorMode.value}.`,
    `Student topic: ${studyTopic.value.trim()}.`,
    `Learning goal: ${learningGoal.value.trim()}.`,
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

// Notify Socratica of a mid-session mode change so it adjusts behaviour immediately.
watch(interruptingModeEnabled, (newValue) => {
  if (!session || connectionState.value !== 'connected') return

  // Clear any accumulated text from the previous mode so it isn't sent in the
  // wrong context after the switch.
  accumulatedRecognitionText = ''
  finalizeTranscript('You')

  // Clear any ON-mode buffer from the previous mode.
  if (onModeFlushTimer !== null) {
    clearTimeout(onModeFlushTimer)
    onModeFlushTimer = null
  }
  onModeBuffer = ''

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
    addEvent(`Interrupting mode → ${newValue ? 'ON' : 'OFF'}`)
  } catch {
    // Ignore if session is not ready.
  }
})

// How long (ms) to wait after Socratica's last audio sample before restarting
// speech recognition. Her voice lingers in the room/speakers for a moment after
// the buffer ends — starting recognition immediately picks up that residue and
// attributes it to "You".
const RECOGNITION_RESTART_DELAY_MS = 600

// Stop speech recognition while Socratica speaks so the engine cannot buffer her
// audio. When she finishes, wait for the room echo to decay before restarting.
watch(isModelSpeaking, (speaking) => {
  if (!speechRecognitionEnabled || !speechRecognition) return

  if (speaking) {
    // Socratica started — stop recognition immediately.
    if (speechRecognitionActive) {
      speechRecognition.stop()
      // speechRecognitionActive is set false by the onend handler.
    }
    // Cancel any pending ON-mode flush — Socratica is already responding.
    if (onModeFlushTimer !== null) {
      clearTimeout(onModeFlushTimer)
      onModeFlushTimer = null
    }
    onModeBuffer = ''
    // Drop any stale pending "You" bubble (interim echo from a previous turn).
    finalizeTranscript('You')
  } else {
    // Socratica finished speaking (either naturally or because the user interrupted).
    // When the user interrupted, playback was killed immediately so there is no
    // residual echo — restart recognition quickly so the live transcript shows up.
    // When Socratica finished naturally, wait for the full echo-decay delay.
    const delay = userInterruptedSocratica ? 50 : RECOGNITION_RESTART_DELAY_MS
    userInterruptedSocratica = false
    setTimeout(() => {
      if (!speechRecognitionEnabled || !speechRecognition || isModelSpeaking.value) return
      if (!speechRecognitionActive) {
        try {
          speechRecognition.start()
          speechRecognitionActive = true
        } catch { /* ignore timing issues */ }
      }
    }, delay)
  }
})

// ─── Preset ───────────────────────────────────────────────────────────────────

const applyPreset = () => {
  const preset = activePreset.value
  sessionTitle.value = preset.title
  studyTopic.value = preset.topic
  learningGoal.value = preset.learningGoal
  tutorMode.value = preset.tutorMode
  statusMessage.value = `Preset loaded: ${preset.label}.`
}

// ─── Transcript helpers ───────────────────────────────────────────────────────

const sendRecognizedUserText = (text: string) => {
  const trimmed = text.trim()
  if (!trimmed || !session || connectionState.value !== 'connected') return

  // Record the user's turn as a finalized chat bubble.
  addUserEntry(trimmed)
  statusMessage.value = 'Sending your answer to Socratica…'
  addEvent(`Recognised speech: "${trimmed}"`)

  // User is speaking — always stop Socratica's audio immediately (user can always interrupt).
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
  statusMessage.value = 'Transcribing microphone input…'
  addEvent('Uploading captured speech for transcription.')

  try {
    const transcript = await liveVoiceService.transcribeAudio(audioBlob)

    if (!transcript.trim()) {
      // Drop any open pending user bubble — nothing was said.
      finalizeTranscript('You')
      addEvent('No speech detected in the captured audio.')
      return
    }

    addEvent(`Transcription: "${transcript.trim()}"`)
    sendRecognizedUserText(transcript)
  } catch (error) {
    const msg = error instanceof Error ? error.message : String(error)
    console.error('Failed to transcribe captured speech:', error)
    finalizeTranscript('You')
    statusMessage.value = 'Could not transcribe your speech.'
    addEvent(`Transcription failed: ${msg}`)
  } finally {
    isTranscribing.value = false
  }
}

const submitManualUserInput = () => {
  if (!manualUserInput.value.trim()) return
  sendRecognizedUserText(manualUserInput.value)
  manualUserInput.value = ''
}

const clearTranscript = () => {
  transcriptEntries.value = []
  manualUserInput.value = ''
  activeSessionId.value = undefined
}

// ─── Audio playback ───────────────────────────────────────────────────────────

const clearPlaybackQueue = () => {
  activePlaybackNodes.forEach((node) => {
    try { node.stop() } catch { /* ignore */ }
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

const decodeBase64ToArrayBuffer = (base64: string): ArrayBuffer => {
  const binary = window.atob(base64)
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i += 1) bytes[i] = binary.charCodeAt(i)
  return bytes.buffer
}

const pcm16ToFloat32 = (buffer: ArrayBuffer): Float32Array => {
  const view = new DataView(buffer)
  const samples = new Float32Array(buffer.byteLength / 2)
  for (let i = 0; i < samples.length; i += 1) {
    samples[i] = view.getInt16(i * 2, true) / 32768
  }
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
    for (let i = 0; i < value.length; i += 1) view.setUint8(offset + i, value.charCodeAt(i))
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

const downsampleTo16k = (input: Float32Array, inputSampleRate: number): Float32Array => {
  if (inputSampleRate === 16000) return input

  const ratio = inputSampleRate / 16000
  const outputLength = Math.max(1, Math.round(input.length / ratio))
  const output = new Float32Array(outputLength)
  let outputIndex = 0
  let inputIndex = 0

  while (outputIndex < outputLength) {
    const nextInputIndex = Math.round((outputIndex + 1) * ratio)
    let sample = 0
    let count = 0
    for (let i = inputIndex; i < nextInputIndex && i < input.length; i += 1) {
      sample += input[i]
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
  for (let i = 0; i < input.length; i += 1) {
    const clamped = Math.max(-1, Math.min(1, input[i]))
    view.setInt16(i * 2, clamped < 0 ? clamped * 32768 : clamped * 32767, true)
  }
  return buffer
}

const calculateLevel = (input: Float32Array): number => {
  let sum = 0
  for (let i = 0; i < input.length; i += 1) sum += input[i] * input[i]
  return Math.sqrt(sum / input.length)
}

const arrayBufferToBase64 = (buffer: ArrayBuffer): string => {
  const bytes = new Uint8Array(buffer)
  let binary = ''
  for (let i = 0; i < bytes.byteLength; i += 1) binary += String.fromCharCode(bytes[i])
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
    addEvent('Received an unreadable server payload.')
    return
  }

  serverMessageCount += 1
  if (serverMessageCount <= 5) {
    addEvent(`Gemini message #${serverMessageCount}: ${Object.keys(message).join(', ') || 'none'}`)
  }

  if (!hasSeenServerMessage) {
    hasSeenServerMessage = true
    addEvent('First Gemini server message received.')
  }

  if (message.setupComplete) {
    addEvent('Gemini Live socket is ready.')
  }

  const inputTranscription = message.serverContent?.inputTranscription ?? message.inputTranscription
  const outputTranscription = message.serverContent?.outputTranscription ?? message.outputTranscription
  const serverContent = message.serverContent

  // User interrupted the model — stop playback immediately.
  if (serverContent?.interrupted) {
    clearPlaybackQueue()
    // Finalize whatever Socratica had streamed so far so the bubble doesn't stay pending.
    finalizeTranscript('Socratica')
    addEvent('Model response interrupted by user.')
  }

  // Live user transcription from Gemini's input transcription service.
  // (Only fires when real audio is streamed to Gemini via sendRealtimeInput.)
  if (inputTranscription?.text) {
    streamTranscript('You', inputTranscription.text)
  }
  if (inputTranscription?.finished) {
    finalizeTranscript('You', inputTranscription.text ?? undefined)
  }

  // Live Socratica transcript — streams the model's spoken response as it plays.
  if (outputTranscription?.text) {
    streamTranscript('Socratica', outputTranscription.text)
  }
  if (outputTranscription?.finished) {
    finalizeTranscript('Socratica', outputTranscription.text ?? undefined)
  }

  // Enqueue model audio chunks for playback.
  const parts = serverContent?.modelTurn?.parts ?? []
  for (const part of parts) {
    if (part.inlineData?.data) {
      await enqueueAudioChunk(part.inlineData.data)
    }
  }

  if (!serverContent) return

  if (serverContent.waitingForInput) {
    statusMessage.value = 'Socratica is listening — your turn.'
  } else if (serverContent.generationComplete) {
    statusMessage.value = 'Socratica finished speaking — your turn.'
  }
}

// ─── Microphone pipeline ──────────────────────────────────────────────────────

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
    if (!session || connectionState.value !== 'connected' || !audioContext) return

    const inputSamples = event.inputBuffer.getChannelData(0)
    const level = calculateLevel(inputSamples)
    const downsampled = downsampleTo16k(inputSamples, audioContext.sampleRate)
    const pcm16Buffer = float32ToPcm16(downsampled)

    // Stream audio to Gemini Live in real-time.
    // This enables Gemini's built-in VAD, inputAudioTranscription, and
    // START_OF_ACTIVITY_INTERRUPTS to work correctly.
    // Skip while Socratica is speaking to prevent mic echo from being sent.
    if (!isModelSpeaking.value) {
      try {
        session.sendRealtimeInput({ audio: { data: arrayBufferToBase64(pcm16Buffer), mimeType: 'audio/pcm;rate=16000' } })
      } catch { /* session may be closing */ }
    }

    // Speech detection — same for both modes.
    // Used to: (a) update isListening UI state, (b) clear Socratica's audio the
    // moment the user starts speaking, (c) trigger the end-of-turn flush in OFF mode.
    if (level > SPEECH_LEVEL_THRESHOLD) {
      consecutiveSpeechChunks += 1

      if (speechActivityStarted) {
        silenceChunkCount = 0
        isListening.value = true
        capturedSpeechChunks.push(pcm16Buffer)
      } else if (consecutiveSpeechChunks >= SPEECH_CONFIRM_CHUNKS) {
        // Do NOT clear capturedSpeechChunks here — the chunks collected during
        // the confirmation phase (the else branch below) ARE the first ~180 ms
        // of the user's speech. Discarding them loses the first word.
        addEvent('User speech confirmed — clearing model audio.')
        const wasModelSpeaking = isModelSpeaking.value
        clearPlaybackQueue()
        if (wasModelSpeaking) {
          // User interrupted Socratica. Flag this so the isModelSpeaking watcher
          // uses a near-zero restart delay instead of the normal 600 ms echo-decay
          // delay, letting the live interruption transcript appear immediately.
          userInterruptedSocratica = true
        }
        // Restart recognition immediately whenever speech is confirmed, not only
        // on interruption. Recognition may still be in the echo-decay restart
        // window after Socratica finished naturally — starting it now prevents
        // the first word from being missed while the timer is still counting down.
        if (speechRecognitionEnabled && speechRecognition && !speechRecognitionActive) {
          try {
            speechRecognition.start()
            speechRecognitionActive = true
            addEvent('Recognition restarted on speech confirmation.')
          } catch { /* recognition may still be stopping; watcher will handle it */ }
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
      silenceChunkCount += 1
      capturedSpeechChunks.push(pcm16Buffer)

      if (silenceChunkCount >= SILENCE_CHUNKS_INTERRUPTING_OFF) {
        speechActivityStarted = false
        silenceChunkCount = 0
        isListening.value = false
        // Gemini receives the audio via sendRealtimeInput and handles the response
        // itself. We only need to finalize the local transcript display here.
        if (accumulatedRecognitionText) {
          addUserEntry(accumulatedRecognitionText)
          accumulatedRecognitionText = ''
        }
        capturedSpeechChunks = []
      }
      return
    }

    isListening.value = false
  }
}

const stopAudioPipeline = async () => {
  clearPlaybackQueue()

  if (processorNode) {
    processorNode.disconnect()
    processorNode.onaudioprocess = null
    processorNode = null
  }
  if (sourceNode) { sourceNode.disconnect(); sourceNode = null }
  if (sinkNode) { sinkNode.disconnect(); sinkNode = null }

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
  consecutiveSpeechChunks = 0
  isListening.value = false
  isModelSpeaking.value = false
}

// ─── Speech recognition (browser fallback) ───────────────────────────────────

const detachRawSocketListener = () => {
  const rawSocket = (session as { conn?: { ws?: WebSocket } } | null)?.conn?.ws
  if (rawSocket && rawSocketListener) {
    rawSocket.removeEventListener('message', rawSocketListener)
  }
  rawSocketListener = null
}

const stopSpeechRecognition = () => {
  speechRecognitionEnabled = false
  speechRecognitionActive = false
  voiceInputBlocked.value = false
  isListening.value = false
  accumulatedRecognitionText = ''
  // Finalize any open user bubble left from speech recognition.
  finalizeTranscript('You')
  if (speechRecognition) speechRecognition.stop()
}

const startSpeechRecognition = (): boolean => {
  const RecognitionCtor = window.SpeechRecognition ?? window.webkitSpeechRecognition
  if (!RecognitionCtor) {
    voiceInputBlocked.value = true
    addEvent('Browser speech recognition is not supported.')
    return false
  }

  if (!speechRecognition) {
    speechRecognition = new RecognitionCtor()
    speechRecognition.continuous = true
    speechRecognition.interimResults = true
    speechRecognition.lang = 'en-US'

    speechRecognition.onresult = (event) => {
      // Ignore results while Socratica is speaking — the microphone picks up her
      // audio through the speakers and speech recognition would attribute it to "You".
      if (isModelSpeaking.value) return

      let interimTranscript = ''
      const finalSegments: string[] = []

      for (let i = event.resultIndex; i < event.results.length; i += 1) {
        const result = event.results[i]
        const transcript = result[0]?.transcript?.trim()
        if (!transcript) continue

        if (result.isFinal) {
          finalSegments.push(transcript)
        } else {
          interimTranscript += `${transcript} `
        }
      }

      const interim = interimTranscript.trim()
      isListening.value = interim.length > 0 || finalSegments.length > 0

      // Show live interim text in the pending "You" bubble (cumulative → replace).
      if (interim) {
        // In OFF mode, prefix with anything already accumulated so the bubble
        // shows the full in-progress turn text.
        const display = accumulatedRecognitionText
          ? `${accumulatedRecognitionText} ${interim}`
          : interim
        replaceTranscriptText('You', display)
      }

      if (finalSegments.length) {
        const finalText = finalSegments.join(' ')

        if (interruptingModeEnabled.value) {
          // ── Interrupting ON ──────────────────────────────────────────────────
          // Buffer final segments briefly before flushing to Gemini. This groups
          // related clauses together so Gemini can evaluate meaning across a full
          // thought, not just react to a single isolated word or phrase.
          // The flush fires ON_MODE_FLUSH_DELAY_MS after the last isFinal segment,
          // giving Gemini richer semantic context to decide whether to interrupt.
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
            // Gemini receives audio via sendRealtimeInput and handles the response.
            // Just finalize the local transcript display.
            addUserEntry(text)
          }, ON_MODE_FLUSH_DELAY_MS)
        } else {
          // ── Interrupting OFF ─────────────────────────────────────────────────
          // Accumulate clauses. The ScriptProcessor silence detection (2 s) will
          // flush `accumulatedRecognitionText` once the user truly stops talking,
          // so Gemini gets the full answer before Socratica responds.
          accumulatedRecognitionText = accumulatedRecognitionText
            ? `${accumulatedRecognitionText} ${finalText}`
            : finalText
          // Update the bubble with the fully accumulated text so far.
          replaceTranscriptText('You', accumulatedRecognitionText)
        }
      }
    }

    speechRecognition.onerror = (event) => {
      addEvent(`Speech recognition error${event.error ? `: ${event.error}` : '.'}`)
      if (event.error === 'service-not-allowed' || event.error === 'not-allowed') {
        speechRecognitionEnabled = false
        voiceInputBlocked.value = true
        statusMessage.value = 'This browser blocked speech recognition. Try Chrome or Edge for voice input.'
      }
    }

    speechRecognition.onend = () => {
      speechRecognitionActive = false
      isListening.value = false
      // Do not auto-restart while Socratica is speaking — we intentionally stopped
      // recognition to prevent her audio from being buffered and later attributed to "You".
      if (speechRecognitionEnabled && connectionState.value === 'connected' && !isModelSpeaking.value) {
        try {
          speechRecognition?.start()
          speechRecognitionActive = true
        } catch { /* ignore restart timing issues */ }
      }
    }
  }

  speechRecognitionEnabled = true
  voiceInputBlocked.value = false
  if (!speechRecognitionActive) {
    try {
      speechRecognition.start()
      speechRecognitionActive = true
      addEvent('Browser speech recognition started.')
    } catch (error) {
      speechRecognitionEnabled = false
      addEvent(`Speech recognition could not start${error instanceof Error ? `: ${error.message}` : '.'}`)
      return false
    }
  }

  return true
}

// ─── Session persistence ──────────────────────────────────────────────────────

const loadSavedSessions = async () => {
  try {
    savedSessions.value = await liveVoiceService.listTutorSessions()
  } catch (error) {
    console.error('Failed to load saved sessions:', error)
    addEvent('Could not load saved sessions.')
  }
}

const saveCurrentSession = async () => {
  if (!transcriptEntries.value.length) {
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
      transcriptEntries: transcriptEntries.value
        .filter((e) => !e.pending && e.text.trim())
        .map<TutorTranscriptEntry>((entry) => ({
          speaker: entry.speaker,
          text: entry.text,
        })),
    }

    const savedSession = await liveVoiceService.saveTutorSession(payload)
    activeSessionId.value = savedSession.id
    statusMessage.value = 'Session saved.'
    addEvent(`Saved session "${savedSession.title}".`)
    await loadSavedSessions()
  } catch (error) {
    console.error('Failed to save session:', error)
    statusMessage.value = 'Failed to save session.'
    addEvent('Could not save session.')
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
      id: String(++nextEntryId),
      speaker: (entry.speaker === 'Socratica' ? 'Socratica' : 'You') as TranscriptSpeaker,
      text: entry.text,
      time: '',
      pending: false,
    }))
    statusMessage.value = `Loaded session "${savedSession.title}".`
    addEvent(`Loaded saved session "${savedSession.title}".`)
    void scrollTranscriptToBottom()
  } catch (error) {
    console.error('Failed to load saved session:', error)
    addEvent('Could not load the selected saved session.')
  }
}

// ─── Session lifecycle ────────────────────────────────────────────────────────

const startLiveSession = async () => {
  if (isBusy.value || isConnected.value) return

  connectionState.value = 'connecting'
  statusMessage.value = 'Requesting a Gemini Live token…'
  transcriptEntries.value = []
  liveModel.value = 'Resolving model…'

  try {
    hasSeenServerMessage = false
    serverMessageCount = 0
    rawSocketMessageCount = 0

    const tokenResponse = await liveVoiceService.createSessionToken()
    liveModel.value = tokenResponse.model
    statusMessage.value = 'Opening Live API socket…'

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
          // START_OF_ACTIVITY_INTERRUPTS means the user's speech always interrupts
          // Socratica's audio. This is always enabled regardless of the interrupting mode toggle.
          activityHandling: ActivityHandling.START_OF_ACTIVITY_INTERRUPTS,
        },
        inputAudioTranscription: {},
        outputAudioTranscription: {},
      },
      callbacks: {
        onopen: () => { addEvent('WebSocket connected.') },
        onmessage: (message) => { void handleServerMessage(message) },
        onerror: (event) => {
          console.error('Gemini Live error:', event)
          connectionState.value = 'error'
          statusMessage.value = 'Gemini Live reported an error.'
          addEvent(`Gemini Live error${'message' in event && typeof event.message === 'string' && event.message ? `: ${event.message}` : '.'}`)
        },
        onclose: (event) => {
          addEvent(`Session closed (code ${event.code}${event.reason ? `: ${event.reason}` : ''}).`)
          if (connectionState.value === 'connected') {
            connectionState.value = 'idle'
            statusMessage.value = 'Session closed.'
          }
        },
      },
    })

    // Attach raw socket debug listener for the first few messages.
    const rawSocket = (session as { conn?: { ws?: WebSocket } } | null)?.conn?.ws
    if (rawSocket?.addEventListener) {
      rawSocketListener = (event: MessageEvent) => {
        rawSocketMessageCount += 1
        if (rawSocketMessageCount <= 5) {
          addEvent(
            typeof event.data === 'string'
              ? `Raw socket #${rawSocketMessageCount}: ${event.data.slice(0, 120)}`
              : `Raw socket #${rawSocketMessageCount}: ${Object.prototype.toString.call(event.data)}`
          )
        }
      }
      rawSocket.addEventListener('message', rawSocketListener)
      rawSocket.addEventListener('close', (event) => {
        addEvent(`Raw socket closed (code ${event.code}${event.reason ? `: ${event.reason}` : ''}).`)
      })
      rawSocket.addEventListener('error', () => { addEvent('Raw socket error.') })
    }

    voiceInputBlocked.value = false
    await startMicrophone()
    addEvent('Microphone streaming started.')

    // Start speech recognition for both modes. In OFF mode it accumulates clauses
    // until the ScriptProcessor's 2 s silence fires. In ON mode it sends each
    // clause immediately so Socratica can respond before the user finishes.
    const recognitionStarted = startSpeechRecognition()
    if (!recognitionStarted) {
      addEvent('Browser speech recognition unavailable — will use backend transcription.')
    }

    connectionState.value = 'connected'
    statusMessage.value = 'Live session ready — start talking.'

    // Kick off the conversation with context.
    addEvent('Sending initial prompt to Socratica.')
    session.sendClientContent({
      turns: [
        {
          role: 'user',
          parts: [
            {
              text: `The student wants to practice: ${studyTopic.value.trim()}. Ask them to begin in their own words, then guide them using the ${tutorMode.value} style. Their goal: ${learningGoal.value.trim()}.`,
            },
          ],
        },
      ],
      turnComplete: true,
    })
  } catch (error) {
    console.error('Failed to start live session:', error)
    connectionState.value = 'error'
    statusMessage.value = error instanceof Error ? error.message : 'Failed to start the live voice session.'
    addEvent(`Failed to start session${error instanceof Error ? `: ${error.message}` : '.'}`)
    stopSpeechRecognition()
    await stopAudioPipeline()
    detachRawSocketListener()
    if (session) { session.close(); session = null }
  }
}

const stopLiveSession = async () => {
  if (session) {
    detachRawSocketListener()
    session.close()
    session = null
  }

  stopSpeechRecognition()
  await stopAudioPipeline()
  connectionState.value = 'idle'
  statusMessage.value = 'Session stopped.'
  addEvent('Live session stopped.')

  if (transcriptEntries.value.length) {
    await saveCurrentSession()
  }
}

// ─── Misc ─────────────────────────────────────────────────────────────────────

const formatDate = (value: string) => new Date(value).toLocaleString()

const goHome = () => { router.push('/') }

onMounted(() => {
  applyPreset()
  void loadSavedSessions()
})

onBeforeUnmount(() => {
  void stopLiveSession()
})
</script>

<style scoped>
/* ── Page shell ─────────────────────────────────────────────────────────────── */
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

/* ── Hero card ──────────────────────────────────────────────────────────────── */
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

h1, h2 { margin: 0; }

h1 {
  font-size: clamp(1.75rem, 3.5vw, 2.75rem);
  line-height: 1.08;
}

h2 {
  font-size: 1.1rem;
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
  margin: 0.75rem 0 0;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  justify-content: flex-end;
  flex-shrink: 0;
}

/* ── Buttons ────────────────────────────────────────────────────────────────── */
.primary-button,
.secondary-button,
.danger-button,
.ghost-button,
.saved-session-card {
  border: none;
  border-radius: 999px;
  padding: 0.85rem 1.2rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 160ms ease, opacity 160ms ease;
  font-size: 0.9rem;
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
  opacity: 0.6;
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
  border: 1px solid rgba(255, 95, 95, 0.25);
}

/* ── Conversation state bar ─────────────────────────────────────────────────── */
.conversation-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.5rem;
  border-radius: 20px;
  border: 1px solid rgba(246, 226, 122, 0.2);
  background: rgba(17, 17, 17, 0.92);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.24);
  flex-wrap: wrap;
}

.conversation-state {
  display: flex;
  align-items: center;
  gap: 0.9rem;
}

.state-icon {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.05);
}

.state-text {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.state-text strong {
  font-size: 0.95rem;
}

.state-detail {
  font-size: 0.82rem;
  color: rgba(245, 241, 232, 0.62);
}

/* Speaking state — wave bars */
.conversation-state.speaking .state-icon {
  background: rgba(105, 219, 124, 0.12);
}

.wave-icon {
  display: flex;
  align-items: center;
  gap: 3px;
}

.wave-bar {
  display: block;
  width: 3px;
  border-radius: 2px;
  background: #8ce99a;
  animation: wave 0.8s ease-in-out infinite;
}

.wave-bar:nth-child(1) { height: 8px;  animation-delay: 0s; }
.wave-bar:nth-child(2) { height: 14px; animation-delay: 0.1s; }
.wave-bar:nth-child(3) { height: 18px; animation-delay: 0.2s; }
.wave-bar:nth-child(4) { height: 14px; animation-delay: 0.3s; }
.wave-bar:nth-child(5) { height: 8px;  animation-delay: 0.4s; }

@keyframes wave {
  0%, 100% { transform: scaleY(0.5); opacity: 0.7; }
  50%       { transform: scaleY(1.3); opacity: 1; }
}

/* Listening state — pulse ring */
.conversation-state.listening .state-icon {
  background: rgba(246, 226, 122, 0.12);
}

.pulse-icon {
  position: relative;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pulse-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 2px solid #f6e27a;
  animation: pulse-ring 1.2s ease-out infinite;
}

.mic-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f6e27a;
}

@keyframes pulse-ring {
  0%   { transform: scale(0.7); opacity: 0.9; }
  100% { transform: scale(1.6); opacity: 0; }
}

/* Processing state — bouncing dots */
.conversation-state.processing .state-icon {
  background: rgba(130, 180, 255, 0.12);
}

.dots-icon {
  display: flex;
  align-items: center;
  gap: 4px;
}

.dot {
  display: block;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #82b4ff;
  animation: dot-bounce 0.9s ease-in-out infinite;
}

.dot:nth-child(1) { animation-delay: 0s; }
.dot:nth-child(2) { animation-delay: 0.18s; }
.dot:nth-child(3) { animation-delay: 0.36s; }

@keyframes dot-bounce {
  0%, 80%, 100% { transform: translateY(0);    opacity: 0.6; }
  40%           { transform: translateY(-5px); opacity: 1; }
}

/* Waiting state */
.conversation-state.waiting .state-icon {
  background: rgba(245, 241, 232, 0.06);
}

.idle-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(245, 241, 232, 0.4);
  animation: idle-pulse 2s ease-in-out infinite;
}

@keyframes idle-pulse {
  0%, 100% { opacity: 0.4; }
  50%      { opacity: 0.9; }
}

/* ── Interrupting mode toggle (conversation bar) ────────────────────────────── */
.interrupt-toggle-button {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.55rem 1rem 0.55rem 0.65rem;
  border-radius: 999px;
  border: 1px solid rgba(245, 241, 232, 0.18);
  background: rgba(255, 255, 255, 0.04);
  color: #f5f1e8;
  cursor: pointer;
  font-size: 0.88rem;
  transition: border-color 200ms, background 200ms;
  white-space: nowrap;
}

.interrupt-toggle-button:hover {
  background: rgba(255, 255, 255, 0.08);
}

.interrupt-toggle-button.toggle-on {
  border-color: rgba(105, 219, 124, 0.4);
  background: rgba(105, 219, 124, 0.06);
}

.toggle-track {
  width: 32px;
  height: 18px;
  border-radius: 9px;
  background: rgba(245, 241, 232, 0.15);
  position: relative;
  transition: background 200ms;
  flex-shrink: 0;
}

.toggle-on .toggle-track {
  background: rgba(105, 219, 124, 0.45);
}

.toggle-thumb {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(245, 241, 232, 0.7);
  transition: transform 200ms, background 200ms;
}

.toggle-on .toggle-thumb {
  transform: translateX(14px);
  background: #8ce99a;
}

.toggle-label strong {
  font-weight: 700;
}

/* ── Inline toggle (setup panel) ────────────────────────────────────────────── */
.inline-toggle {
  padding: 0.3rem 0.75rem;
  border-radius: 999px;
  border: 1px solid rgba(245, 241, 232, 0.2);
  background: rgba(255, 255, 255, 0.04);
  color: rgba(245, 241, 232, 0.7);
  cursor: pointer;
  font-size: 0.82rem;
  font-weight: 700;
  transition: border-color 160ms, background 160ms, color 160ms;
}

.inline-toggle.toggle-on {
  border-color: rgba(105, 219, 124, 0.5);
  background: rgba(105, 219, 124, 0.1);
  color: #8ce99a;
}

/* ── Panel grid ─────────────────────────────────────────────────────────────── */
.panel-grid {
  display: grid;
  grid-template-columns: minmax(300px, 400px) minmax(0, 1fr) minmax(280px, 360px);
  gap: 1.5rem;
  align-items: start;
}

.side-panels {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* ── Setup panel ────────────────────────────────────────────────────────────── */
.field-label,
.status-label,
.speaker {
  display: block;
  font-size: 0.82rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(245, 241, 232, 0.6);
}

.field-input,
.topic-input {
  width: 100%;
  margin-top: 0.6rem;
  border-radius: 14px;
  border: 1px solid rgba(245, 241, 232, 0.14);
  background: rgba(255, 255, 255, 0.03);
  color: #f5f1e8;
  padding: 0.85rem;
  box-sizing: border-box;
}

.field-input { margin-bottom: 1rem; }

.topic-input {
  resize: vertical;
  min-height: 7rem;
  margin-bottom: 1rem;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-top: 1rem;
}

.status-chip {
  border-radius: 999px;
  padding: 0.35rem 0.75rem;
  font-weight: 700;
  font-size: 0.82rem;
  flex-shrink: 0;
}

.status-chip.idle    { background: rgba(245, 241, 232, 0.1); }
.status-chip.connecting { background: rgba(246, 226, 122, 0.16); color: #f6e27a; }
.status-chip.connected  { background: rgba(105, 219, 124, 0.16); color: #8ce99a; }
.status-chip.error      { background: rgba(255, 95, 95, 0.16); color: #ffb3b3; }

.status-list {
  display: grid;
  gap: 0.6rem;
  margin-top: 1rem;
}

.status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.03);
}

.active-indicator { color: #8ce99a; }

.interrupt-info,
.script-note {
  margin-top: 1rem;
  padding: 1rem;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.03);
}

.fallback-panel { margin-top: 1rem; }

.fallback-input { margin-top: 0.65rem; margin-bottom: 0; }

.fallback-send {
  margin-top: 0.75rem;
  width: 100%;
}

/* ── Transcript panel ───────────────────────────────────────────────────────── */
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.transcript-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.message-count {
  font-size: 0.82rem;
  color: rgba(245, 241, 232, 0.5);
}

.transcript-panel {
  display: flex;
  flex-direction: column;
  min-height: 28rem;
}

.transcript-stream {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 0.25rem;
  scroll-behavior: smooth;
}

.transcript-stream::-webkit-scrollbar { width: 4px; }
.transcript-stream::-webkit-scrollbar-track { background: transparent; }
.transcript-stream::-webkit-scrollbar-thumb {
  background: rgba(245, 241, 232, 0.15);
  border-radius: 2px;
}

.transcript-entry {
  border-radius: 16px;
  padding: 0.9rem 1rem;
}

.entry-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 0.5rem;
  margin-bottom: 0.4rem;
}

.entry-time {
  font-size: 0.75rem;
  color: rgba(245, 241, 232, 0.38);
  flex-shrink: 0;
}

.pending-label {
  color: rgba(245, 241, 232, 0.5);
  font-style: italic;
}

.transcript-entry p {
  margin: 0;
  line-height: 1.65;
}

.user-entry {
  background: rgba(246, 226, 122, 0.07);
  border: 1px solid rgba(246, 226, 122, 0.15);
}

.model-entry {
  background: rgba(105, 219, 124, 0.07);
  border: 1px solid rgba(105, 219, 124, 0.13);
}

.pending-entry { opacity: 0.72; }

.empty-state {
  margin: 0;
  color: rgba(245, 241, 232, 0.5);
}

/* ── Demo script / side panels ──────────────────────────────────────────────── */
.demo-steps {
  margin: 0;
  padding-left: 1.2rem;
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
  line-height: 1.6;
}

.script-note {
  margin-top: 1rem;
  padding: 1rem;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.03);
}

/* ── Saved sessions ─────────────────────────────────────────────────────────── */
.saved-session-list {
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
}

.saved-session-card {
  border-radius: 16px;
  text-align: left;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(245, 241, 232, 0.08);
  color: #f5f1e8;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  padding: 0.9rem 1rem;
}

.saved-session-card span {
  font-size: 0.85rem;
  color: rgba(245, 241, 232, 0.6);
}

/* ── Debug events (collapsible) ─────────────────────────────────────────────── */
.events-details {
  border-radius: 16px;
  overflow: hidden;
}

.events-summary {
  cursor: pointer;
  font-size: 0.88rem;
  color: rgba(245, 241, 232, 0.6);
  user-select: none;
  padding: 0;
  list-style: none;
}

.events-summary::-webkit-details-marker { display: none; }
.events-summary::before { content: '▶  '; font-size: 0.7rem; }
details[open] .events-summary::before { content: '▼  '; }

.event-count {
  color: rgba(245, 241, 232, 0.4);
}

.event-list {
  margin: 1rem 0 0;
  padding-left: 1.1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  color: rgba(245, 241, 232, 0.75);
  font-size: 0.84rem;
}

/* ── Responsive ─────────────────────────────────────────────────────────────── */
@media (max-width: 1280px) {
  .panel-grid {
    grid-template-columns: minmax(280px, 380px) minmax(0, 1fr);
    grid-template-rows: auto auto;
  }

  .side-panels {
    grid-column: 1 / -1;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
  }
}

@media (max-width: 900px) {
  .live-page { padding: 1rem; }

  .hero-card {
    flex-direction: column;
  }

  .hero-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .panel-grid {
    grid-template-columns: 1fr;
  }

  .side-panels {
    grid-column: 1;
    grid-template-columns: 1fr;
  }

  .transcript-stream {
    max-height: 50vh;
  }

  .conversation-bar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
