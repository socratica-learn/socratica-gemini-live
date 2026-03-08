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

        <div class="status-row">
          <span class="status-chip" :class="statusClass">{{ connectionLabel }}</span>
          <span class="meta-text">{{ statusMessage }}</span>
        </div>

        <div class="status-list">
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

        <div class="transcript-stream">
          <p
            v-if="!transcriptEntries.length && !currentUserTranscript && !currentModelTranscript"
            class="empty-state"
          >
            The transcript will appear here once the session starts.
          </p>

          <div
            v-for="(entry, index) in transcriptEntries"
            :key="`${entry.speaker}-${index}`"
            class="transcript-entry"
            :class="entry.speaker === 'You' ? 'user-entry' : 'model-entry'"
          >
            <span class="speaker">{{ entry.speaker }}</span>
            <p>{{ entry.text }}</p>
          </div>

          <div v-if="currentUserTranscript" class="transcript-entry user-entry pending-entry">
            <span class="speaker">You</span>
            <p>{{ currentUserTranscript }}</p>
          </div>

          <div v-if="currentModelTranscript" class="transcript-entry model-entry pending-entry">
            <span class="speaker">Socratica</span>
            <p>{{ currentModelTranscript }}</p>
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
import {
  ActivityHandling,
  GoogleGenAI,
  Modality,
  type LiveServerMessage,
  type Session,
} from '@google/genai'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import {
  liveVoiceService,
  type SavedTutorSession,
  type TutorSessionPayload,
  type TutorTranscriptEntry,
} from '@/services/liveVoiceService'

type ConnectionState = 'idle' | 'connecting' | 'connected' | 'error'
type TranscriptSpeaker = 'You' | 'Socratica'

interface TranscriptEntry {
  speaker: TranscriptSpeaker
  text: string
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
const statusMessage = ref('Ready to start a live voice tutoring session.')
const liveModel = ref('Waiting for backend token...')
const isListening = ref(false)
const isModelSpeaking = ref(false)
const isSaving = ref(false)
const transcriptEntries = ref<TranscriptEntry[]>([])
const currentUserTranscript = ref('')
const currentModelTranscript = ref('')
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
let capturedSpeechChunks: ArrayBuffer[] = []
let speechRecognition: SpeechRecognitionLike | null = null
let speechRecognitionActive = false
let speechRecognitionEnabled = false
const isTranscribing = ref(false)

const SPEECH_LEVEL_THRESHOLD = 0.02
const SILENCE_CHUNKS_BEFORE_END = 4

const activePreset = computed(
  () => tutorPresets.find((preset) => preset.id === selectedPresetId.value) ?? tutorPresets[0]
)
const activeDemoSteps = computed(() => activePreset.value.demoSteps)
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

const addTranscriptEntry = (speaker: TranscriptSpeaker, text: string) => {
  const trimmed = text.trim()
  if (!trimmed) {
    return
  }

  const previous = transcriptEntries.value[transcriptEntries.value.length - 1]
  if (previous && previous.speaker === speaker && previous.text === trimmed) {
    return
  }

  transcriptEntries.value.push({ speaker, text: trimmed })
}

const sendRecognizedUserText = (text: string) => {
  const trimmed = text.trim()
  if (!trimmed || !session || connectionState.value !== 'connected') {
    return
  }

  addTranscriptEntry('You', trimmed)
  currentUserTranscript.value = ''
  statusMessage.value = 'Sending your answer to Gemini...'
  addEvent(`Recognized speech locally: "${trimmed}"`)
  clearPlaybackQueue()

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
  if (!capturedSpeechChunks.length || isTranscribing.value) {
    capturedSpeechChunks = []
    return
  }

  const audioBlob = createWavBlobFromPcmChunks(capturedSpeechChunks, 16000)
  capturedSpeechChunks = []
  isTranscribing.value = true
  currentUserTranscript.value = 'Transcribing your speech...'
  statusMessage.value = 'Transcribing your microphone input...'
  addEvent('Uploading captured speech for backend transcription.')

  try {
    const transcript = await liveVoiceService.transcribeAudio(audioBlob)
    currentUserTranscript.value = ''

    if (!transcript.trim()) {
      addEvent('No speech recognized from the captured audio.')
      return
    }

    addEvent(`Backend transcription: "${transcript.trim()}"`)
    sendRecognizedUserText(transcript)
  } catch (error) {
    console.error('Failed to transcribe captured speech:', error)
    currentUserTranscript.value = ''
    statusMessage.value = 'Could not transcribe your speech.'
    addEvent('Backend transcription failed.')
  } finally {
    isTranscribing.value = false
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
  currentUserTranscript.value = ''
  currentModelTranscript.value = ''
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
  playbackCursor = Math.max(playbackCursor, now + 0.02)
  source.start(playbackCursor)
  playbackCursor += buffer.duration
  isModelSpeaking.value = true
  activePlaybackNodes.push(source)

  source.onended = () => {
    activePlaybackNodes = activePlaybackNodes.filter((node) => node !== source)
    if (!activePlaybackNodes.length && audioContext) {
      playbackCursor = audioContext.currentTime
      isModelSpeaking.value = false
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

  const inputTranscription = message.serverContent?.inputTranscription ?? message.inputTranscription
  const outputTranscription = message.serverContent?.outputTranscription ?? message.outputTranscription
  const serverContent = message.serverContent

  if (serverContent?.interrupted) {
    clearPlaybackQueue()
    addEvent('Model response interrupted by new user activity.')
  }

  if (inputTranscription?.text) {
    currentUserTranscript.value = inputTranscription.text
  }

  if (inputTranscription?.finished && currentUserTranscript.value.trim()) {
    addTranscriptEntry('You', currentUserTranscript.value)
    currentUserTranscript.value = ''
  }

  if (outputTranscription?.text) {
    currentModelTranscript.value = outputTranscription.text
  }

  if (outputTranscription?.finished && currentModelTranscript.value.trim()) {
    addTranscriptEntry('Socratica', currentModelTranscript.value)
    currentModelTranscript.value = ''
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
    statusMessage.value = 'Listening for your explanation...'
  } else if (serverContent.generationComplete) {
    statusMessage.value = 'Gemini finished speaking.'
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

    if (level > SPEECH_LEVEL_THRESHOLD) {
      if (!isListening.value) {
        addEvent('User speech detected. Clearing pending model audio.')
      }

      if (!speechActivityStarted) {
        capturedSpeechChunks = []
      }
      speechActivityStarted = true
      silenceChunkCount = 0
      isListening.value = true
      clearPlaybackQueue()
      capturedSpeechChunks.push(pcm16Buffer)
      return
    }

    if (speechActivityStarted) {
      silenceChunkCount += 1
      capturedSpeechChunks.push(pcm16Buffer)

      if (silenceChunkCount >= SILENCE_CHUNKS_BEFORE_END) {
        speechActivityStarted = false
        silenceChunkCount = 0
        isListening.value = false
        void transcribeCapturedSpeech()
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
  speechRecognitionEnabled = false
  speechRecognitionActive = false
  voiceInputBlocked.value = false
  isListening.value = false
  currentUserTranscript.value = ''
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
      let interimTranscript = ''
      const finalSegments: string[] = []

      for (let index = event.resultIndex; index < event.results.length; index += 1) {
        const result = event.results[index]
        const transcript = result[0]?.transcript?.trim()
        if (!transcript) {
          continue
        }

        if (result.isFinal) {
          finalSegments.push(transcript)
        } else {
          interimTranscript += `${transcript} `
        }
      }

      currentUserTranscript.value = interimTranscript.trim()
      isListening.value = interimTranscript.trim().length > 0

      if (finalSegments.length) {
        isListening.value = false
        sendRecognizedUserText(finalSegments.join(' '))
      }
    }

    speechRecognition.onerror = (event) => {
      addEvent(`Browser speech recognition error${event.error ? `: ${event.error}` : '.'}`)
      if (event.error === 'service-not-allowed' || event.error === 'not-allowed') {
        speechRecognitionEnabled = false
        voiceInputBlocked.value = true
        statusMessage.value = 'This browser blocked local speech recognition. Try Chrome or Edge for voice transcript input.'
      }
    }

    speechRecognition.onend = () => {
      speechRecognitionActive = false
      isListening.value = false
      if (speechRecognitionEnabled && connectionState.value === 'connected') {
        try {
          speechRecognition?.start()
          speechRecognitionActive = true
        } catch {
          // Ignore browser restart timing issues.
        }
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
      addEvent(
        `Browser speech recognition could not start${error instanceof Error ? `: ${error.message}` : '.'}`
      )
      return false
    }
  }

  return true
}

const buildTutorPrompt = () => {
  return [
    'You are Socratica, a spoken Socratic tutor for a hackathon demo.',
    `Tutor mode: ${tutorMode.value}.`,
    `Student topic: ${studyTopic.value.trim()}.`,
    `Learning goal: ${learningGoal.value.trim()}.`,
    'Greet the student briefly, invite them to begin, then interrupt politely whenever they are vague, skip a causal link, or make an unsupported claim.',
    'Keep spoken responses concise, natural, and judge-friendly.',
  ].join(' ')
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
      transcriptEntries: transcriptEntries.value.map<TutorTranscriptEntry>((entry) => ({
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
      speaker: entry.speaker === 'Socratica' ? 'Socratica' : 'You',
      text: entry.text,
    }))
    currentUserTranscript.value = ''
    currentModelTranscript.value = ''
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
  statusMessage.value = 'Requesting a short-lived Gemini Live token...'
  transcriptEntries.value = []
  currentUserTranscript.value = ''
  currentModelTranscript.value = ''
  liveModel.value = 'Resolving model...'

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
          statusMessage.value = 'Gemini Live reported an error.'
          addEvent(
            `Gemini Live error received from the browser client${'message' in event && typeof event.message === 'string' && event.message ? `: ${event.message}` : '.'}`
          )
        },
        onclose: (event) => {
          addEvent(`Gemini Live session closed (code ${event.code}${event.reason ? `: ${event.reason}` : ''}).`)
          if (connectionState.value === 'connected') {
            connectionState.value = 'idle'
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
    statusMessage.value = 'Live session ready. Start talking.'

    addEvent('Sending initial tutor prompt to Gemini.')
    session.sendClientContent({
      turns: [
        {
          role: 'user',
          parts: [
            {
              text: `The student wants to practice this topic: ${studyTopic.value.trim()}. Ask them to begin in their own words, then challenge them using the ${tutorMode.value} style. Their goal is: ${learningGoal.value.trim()}.`,
            },
          ],
        },
      ],
      turnComplete: true,
    })
  } catch (error) {
    console.error('Failed to start live session:', error)
    connectionState.value = 'error'
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
  min-height: 24rem;
}

.transcript-stream {
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
  max-height: 38rem;
  overflow-y: auto;
}

.transcript-entry {
  border-radius: 18px;
  padding: 1rem;
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
  opacity: 0.74;
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
}
</style>
