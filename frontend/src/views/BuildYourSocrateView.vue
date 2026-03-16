<template>
  <div class="build-page">
    <a class="back-btn" @click.prevent="personalizing ? (personalizing = false) : historyOpen ? (historyOpen = false) : router.push('/')">
      <svg class="back-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M19 12H5M5 12l7-7M5 12l7 7"/>
      </svg>
      <span class="back-label">{{ personalizing ? 'Back to session' : historyOpen ? 'Back to session' : 'Back to homepage' }}</span>
    </a>

    <!-- History button -->
    <button class="history-btn" :class="{ 'hidden-ui': personalizing }" @click="historyOpen = true">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 8v4l3 3M3.05 11a9 9 0 1 0 .5-3"/>
        <path d="M3 4v4h4"/>
      </svg>
      <span>History</span>
    </button>

    <!-- History overlay -->
    <transition name="history">
      <div v-if="historyOpen" class="history-overlay" :class="{ 'hidden-ui': personalizing }">
        <div class="history-panel">
          <div class="history-header">
            <h2 class="history-title">Session History</h2>
            <button class="history-close" @click="historyOpen = false">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12"/>
              </svg>
            </button>
          </div>
          <div class="history-body">
            <div v-if="sessionHistory.length === 0" class="history-empty">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 8v4l3 3M3.05 11a9 9 0 1 0 .5-3"/><path d="M3 4v4h4"/>
              </svg>
              <p>No sessions yet. Start a session to see your history here.</p>
            </div>
            <div v-for="session in sessionHistory" :key="session.id" class="history-item">
              <div class="history-item-type">{{ session.type }}</div>
              <div class="history-item-main">
                <span class="history-item-title">{{ session.title || 'Untitled session' }}</span>
                <span v-if="session.topic" class="history-item-topic">{{ session.topic }}</span>
              </div>
              <div class="history-item-meta">
                <span class="history-item-date">{{ session.date }}</span>
                <span class="history-item-count">{{ session.messageCount }} {{ session.messageCount === 1 ? 'message' : 'messages' }}</span>
              </div>
              <button class="history-item-delete" @click.stop="deleteSession(session.id)" title="Delete">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>
            </div>
          </div>
          <div v-if="sessionHistory.length > 0" class="history-footer">
            <button class="history-clear" @click="clearHistory">Clear all history</button>
          </div>
        </div>
      </div>
    </transition>

    <canvas ref="splineCanvas" class="spline-bg" :class="{ 'spline-zoom': personalizing, 'spline-speaking': isModelSpeaking }"></canvas>

    <!-- Speaking pulse rings — appear behind the avatar when AI is talking -->
    <transition name="speaking-rings">
      <div v-if="isModelSpeaking" class="avatar-speaking-overlay" aria-hidden="true">
        <div class="avatar-ring ring-1"></div>
        <div class="avatar-ring ring-2"></div>
        <div class="avatar-ring ring-3"></div>
      </div>
    </transition>

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
          <p class="p-card-desc">{{ voices.find(v => v.name === selectedVoice)?.description || 'Choose your Socratic guide.' }}</p>
          <div class="p-dropdown-wrapper">
            <button class="p-dropdown-btn" @click="voiceOpen = !voiceOpen">
              <span class="voice-btn-label">
                <span class="voice-name">{{ selectedVoice }}</span>
                <span class="voice-style">{{ voices.find(v => v.name === selectedVoice)?.style }}</span>
              </span>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M6 9l6 6 6-6"/></svg>
            </button>
            <div v-if="voiceOpen" class="p-dropdown-menu voice-dropdown-menu">
              <button
                v-for="v in voices"
                :key="v.name"
                class="p-dropdown-item voice-dropdown-item"
                :class="{ active: selectedVoice === v.name }"
                @click="selectedVoice = v.name; voiceOpen = false"
              >
                <span class="voice-item-header">
                  <span class="voice-item-name">{{ v.name }}</span>
                  <span class="voice-item-style">{{ v.style }}</span>
                </span>
                <span class="voice-item-desc">{{ v.description }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Top-right: Teaching Style -->
        <div class="p-card top-right">
          <h3 class="p-card-title">Teaching Style</h3>
          <p class="p-card-desc">
            <span v-for="(trait, i) in (teachingStyles.find(s => s.name === selectedStyle)?.traits ?? [])" :key="i">
              {{ trait }}<span v-if="i < (teachingStyles.find(s => s.name === selectedStyle)?.traits.length ?? 1) - 1"> · </span>
            </span>
          </p>
          <div class="p-dropdown-wrapper">
            <button class="p-dropdown-btn" @click="styleOpen = !styleOpen">
              <span>{{ selectedStyle }}</span>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M6 9l6 6 6-6"/></svg>
            </button>
            <div v-if="styleOpen" class="p-dropdown-menu style-dropdown-menu">
              <button
                v-for="s in teachingStyles"
                :key="s.name"
                class="p-dropdown-item style-dropdown-item"
                :class="{ active: selectedStyle === s.name }"
                @click="selectedStyle = s.name; styleOpen = false"
              >
                <span class="style-item-name">{{ s.name }}</span>
                <span class="style-item-traits">{{ s.traits.join(' · ') }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Bottom-center: Learning Mode -->
        <div class="p-card bottom-center">
          <h3 class="p-card-title">Learning Mode</h3>
          <p class="p-card-desc">{{ learningModes.find(m => m.id === selectedLearningMode)?.desc || 'Choose your learning approach.' }}</p>
          <div class="p-dropdown-wrapper">
            <button class="p-dropdown-btn" @click="learningOpen = !learningOpen">
              <span class="voice-btn-label">
                <span class="voice-name">{{ learningModes.find(m => m.id === selectedLearningMode)?.label }}</span>
                <span class="voice-style">{{ learningModes.find(m => m.id === selectedLearningMode)?.tag }}</span>
              </span>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M6 9l6 6 6-6"/></svg>
            </button>
            <div v-if="learningOpen" class="p-dropdown-menu p-dropdown-menu--up style-dropdown-menu">
              <button
                v-for="mode in learningModes"
                :key="mode.id"
                class="p-dropdown-item style-dropdown-item"
                :class="{ active: selectedLearningMode === mode.id }"
                @click="selectedLearningMode = mode.id; learningOpen = false"
              >
                <span class="style-item-name">{{ mode.label }}</span>
                <span class="style-item-traits">{{ mode.tag }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Bottom-left: Personality -->
        <div class="p-card bottom-left">
          <h3 class="p-card-title">Personality</h3>
          <p class="p-card-desc">{{ personalities.find(p => p.name === selectedPersonality)?.traits[0] || 'Set the temperament of your AI companion.' }}</p>
          <div class="p-dropdown-wrapper">
            <button class="p-dropdown-btn" @click="personalityOpen = !personalityOpen">
              <span>{{ selectedPersonality }}</span>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M6 9l6 6 6-6"/></svg>
            </button>
            <div v-if="personalityOpen" class="p-dropdown-menu p-dropdown-menu--up style-dropdown-menu">
              <button
                v-for="p in personalities"
                :key="p.name"
                class="p-dropdown-item style-dropdown-item"
                :class="{ active: selectedPersonality === p.name }"
                @click="selectedPersonality = p.name; personalityOpen = false"
              >
                <span class="style-item-name">{{ p.name }}</span>
                <span class="style-item-traits">{{ p.traits[0] }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Bottom-right: Dynamic Interruptions -->
        <div class="p-card bottom-right">
          <div class="p-card-header-row">
            <h3 class="p-card-title">Dynamic Interruptions</h3>
            <button class="p-toggle" :class="{ on: dynamicInterruptions }" @click="dynamicInterruptions = !dynamicInterruptions">
              <span class="p-toggle-thumb"></span>
            </button>
          </div>
          <p class="p-card-desc">Choose which triggers allow the AI to interrupt during live voice sessions.</p>
          <transition name="dropdown">
            <div v-if="dynamicInterruptions" class="interruption-modes interruption-modes--overlay">
              <button
                v-for="opt in interruptionModeOptions"
                :key="opt.id"
                class="interruption-chip"
                :class="{ active: activeInterruptionModes.includes(opt.id) }"
                @click="activeInterruptionModes.includes(opt.id)
                  ? activeInterruptionModes.splice(activeInterruptionModes.indexOf(opt.id), 1)
                  : activeInterruptionModes.push(opt.id)"
                :title="opt.desc"
              >
                <svg v-if="activeInterruptionModes.includes(opt.id)" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M20 6L9 17l-5-5"/></svg>
                {{ opt.label }}
              </button>
            </div>
          </transition>
        </div>

        <!-- Save button -->
        <div class="p-save-wrap">
          <button class="p-save-btn" @click="personalizing = false">
            Save Configuration
          </button>
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
            v-if="isVoiceSession"
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
      <div class="session-panel" :class="{ 'has-more': sessionBodyHasMore }">
        <div class="session-header">
          <span class="session-label">Session Details</span>
        </div>
        <div class="session-body" ref="sessionBodyEl" @scroll="onSessionBodyScroll">
          <div class="session-field">
            <label class="field-label">Session Title</label>
            <input v-model="sessionTitle" class="field-input" type="text" :placeholder="selectedMode === 'Interview Prep' ? 'e.g. Google SWE Interview' : selectedMode === 'Cover Letter Analysis' ? 'e.g. Product Manager at Stripe' : 'e.g. Biology Exam Prep'" />
          </div>

          <!-- Interview Prep & Cover Letter Analysis specific fields -->
          <template v-if="selectedMode === 'Interview Prep' || selectedMode === 'Cover Letter Analysis'">
            <div class="session-field">
              <label class="field-label">Company Description</label>
              <textarea
                v-model="companyDescription"
                class="field-input field-textarea"
                placeholder="e.g. Google is a multinational technology company focused on search, cloud computing, and AI..."
                rows="3"
              ></textarea>
            </div>
            <div class="session-field">
              <label class="field-label">Job Description</label>
              <textarea
                v-model="jobDescription"
                class="field-input field-textarea"
                placeholder="e.g. We are looking for a Software Engineer with experience in distributed systems..."
                rows="3"
              ></textarea>
            </div>
          </template>

          <!-- Default fields for other modes -->
          <template v-else>
            <div class="session-field">
              <label class="field-label">What do you want to study?</label>
              <textarea
                v-model="sessionTopic"
                class="field-input field-textarea"
                placeholder="e.g. Chapter 4 of my biology textbook, focusing on cell division..."
                rows="3"
              ></textarea>
            </div>
          </template>

          <div class="session-field">
            <label class="field-label">{{
              selectedMode === 'Interview Prep' ? 'Upload your CV' :
              selectedMode === 'Cover Letter Analysis' ? 'Upload your Cover Letter, Motivational Letter, Letter of Intent, etc.' :
              'Files'
            }}</label>
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
      <!-- Voice session: transcript + fallback -->
      <div v-if="isVoiceSession" class="right-panels">

        <!-- Camera panel (Presentation Prep only) -->
        <div v-if="isPresentationPrep" class="camera-panel">
          <div v-if="!isConnected && !cameraGranted" class="camera-prompt">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M15 10l4.553-2.069A1 1 0 0121 8.87v6.26a1 1 0 01-1.447.894L15 14M3 8a2 2 0 012-2h10a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2V8z"/>
            </svg>
            <p>Your camera will activate when you start the session</p>
          </div>
          <div v-else-if="cameraError" class="camera-error">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
            </svg>
            <p>{{ cameraError }}</p>
          </div>
          <video v-show="cameraGranted" ref="cameraVideoEl" class="camera-feed" autoplay playsinline muted></video>
          <!-- Countdown overlay -->
          <transition name="countdown-fade">
            <div v-if="countdownValue !== null" class="countdown-overlay">
              <transition name="countdown-pop" mode="out-in">
                <span :key="countdownValue" class="countdown-number">{{ countdownValue }}</span>
              </transition>
            </div>
          </transition>
        </div>

        <!-- Start Presentation button (shown during intro phase) -->
        <div v-if="isPresentationPrep && isConnected && presentationPhase === 'intro'" class="start-presentation-wrap">
          <button class="start-presentation-btn" @click="beginCountdown">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="5 3 19 12 5 21 5 3"/>
            </svg>
            Say "Start" or click here to begin
          </button>
        </div>

        <div class="transcript-panel" :class="{ 'transcript-panel--compact': isPresentationPrep }">
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

      <!-- Non-voice session: chat -->
      <div v-else class="right-panels">
        <div class="transcript-panel chat-panel">
          <div class="transcript-header">
            <span class="transcript-label">Chat</span>
          </div>
          <div class="transcript-body" ref="transcriptBody">
            <div v-if="chatMessages.length === 0" class="transcript-empty">
              Ask your AI tutor anything about your session topic.
            </div>
            <div v-for="(msg, i) in chatMessages" :key="i" class="transcript-msg" :class="msg.role">
              <span class="msg-role">{{ msg.role === 'user' ? 'You' : 'Socratica' }}</span>
              <p class="msg-text">{{ msg.text }}</p>
            </div>
          </div>
        </div>
        <div class="fallback-panel">
          <form class="fallback-form" @submit.prevent="sendChat">
            <input v-model="chatInput" class="fallback-input" type="text" placeholder="Message Socratica..." autocomplete="off" />
            <button class="fallback-send" type="submit" :disabled="!chatInput.trim()">
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

const voiceSessions = ['Presentation Prep', 'Socratic Evaluation', 'Interview Prep']
const isVoiceSession = computed(() => voiceSessions.includes(selectedMode.value))

// Chat (non-voice sessions)
const chatMessages = ref<{ role: 'user' | 'ai'; text: string }[]>([])
const chatInput = ref('')


const sessionVisible = ref(true)
const transcriptVisible = ref(true)
const personalizing = ref(false)

// ─── Personalization refs ─────────────────────────────────────────────────────

const voiceOpen = ref(false)
const styleOpen = ref(false)
const personalityOpen = ref(false)
const learningOpen = ref(false)
const selectedVoice = ref('Socrates')
const selectedStyle = ref('Socratic Challenger')
const selectedPersonality = ref('Analytical')
const dynamicInterruptions = ref(true)
const learningModes = [
  { id: 'explorer',   label: 'Explorer',   tag: 'Curiosity driven',              desc: 'Follow your curiosity — the AI adapts to where your questions lead.' },
  { id: 'apprentice', label: 'Apprentice', tag: 'Guided learning',               desc: 'Step-by-step guidance with scaffolded support throughout the session.' },
  { id: 'scholar',    label: 'Scholar',    tag: 'Conceptual depth',              desc: 'Deep conceptual questioning that builds rigorous understanding.' },
  { id: 'mastery',    label: 'Mastery',    tag: 'Debate & interruptions',        desc: 'Hard questions, active debate, and real-time interruptions for peak challenge.' },
]
const selectedLearningMode = ref('explorer')
const interruptionModeOptions = [
  { id: 'logical-fallacy',  label: 'Logical Fallacy Detector',              desc: 'Catches flawed reasoning in real time' },
  { id: 'off-topic',        label: 'Off-Topic Redirector',                  desc: 'Brings you back when you stray' },
  { id: 'confidence',       label: 'Confidence Challenge',                  desc: 'Pushes back when you sound uncertain' },
  { id: 'clarification',    label: 'Clarification Trigger',                 desc: 'Asks you to elaborate on vague points' },
  { id: 'silence-breaker',  label: 'Silence Breaker',                       desc: 'Prompts you if you pause too long' },
]
const activeInterruptionModes = ref<string[]>(['logical-fallacy', 'off-topic'])
const voices: { name: string; style: string; description: string }[] = [
  { name: 'Socrates',  style: 'Philosophical', description: 'Constantly asks probing questions and challenges assumptions' },
  { name: 'Athena',   style: 'Strategic',      description: 'Structured, analytical, and focused on clarity' },
  { name: 'Leonardo', style: 'Creative',       description: 'Encourages exploration and unconventional thinking' },
  { name: 'Curie',    style: 'Scientific',     description: 'Precise, logical, evidence-driven' },
  { name: 'Seneca',   style: 'Reflective',     description: 'Calm, thoughtful, emphasizes reasoning and clarity' },
  { name: 'Tesla',    style: 'Visionary',      description: 'Connects concepts and encourages deep insight' },
  { name: 'Aristotle',style: 'Systematic',     description: 'Focuses on definitions, categories, and structured knowledge' },
  { name: 'Maya',     style: 'Empathetic',     description: 'Supportive, patient, encouraging confidence' },
  { name: 'Darwin',   style: 'Curious',        description: 'Always asking "why" and pushing deeper inquiry' },
  { name: 'Hypatia',  style: 'Mathematical',   description: 'Analytical and precise reasoning' },
]
const teachingStyles: { name: string; traits: string[] }[] = [
  { name: 'Socratic Challenger', traits: ['Deep questioning', 'Interrupts weak reasoning', 'Forces clarification'] },
  { name: 'Debate Partner',      traits: ['Challenges ideas', 'Argues counterpoints', 'Tests logical consistency'] },
  { name: 'Concept Builder',     traits: ['Builds understanding step by step', 'Helps construct mental models'] },
  { name: 'Exam Coach',          traits: ['Rapid-fire questions', 'Simulates exam pressure'] },
  { name: 'Gentle Guide',        traits: ['Encouraging', 'Slow paced', 'Ideal for beginners'] },
  { name: 'Critical Reviewer',   traits: ['Focuses on weaknesses', 'Identifies gaps in reasoning'] },
  { name: 'Story Teacher',       traits: ['Explains through metaphors and examples'] },
]
const personalities: { name: string; traits: string[] }[] = [
  { name: 'Analytical',    traits: ['Focuses on logic and precision'] },
  { name: 'Provocative',   traits: ['Challenges assumptions aggressively'] },
  { name: 'Patient',       traits: ['Gives time to think'] },
  { name: 'Playful',       traits: ['Uses humor and metaphors'] },
  { name: 'Minimalist',    traits: ['Short, sharp prompts'] },
  { name: 'Motivational',  traits: ['Encourages persistence'] },
  { name: 'Philosophical', traits: ['Connects ideas to broader thinking'] },
  { name: 'Skeptical',     traits: ['Always questions certainty'] },
]

const sessionTitle = ref('')
const sessionTopic = ref('')
const companyDescription = ref('')
const jobDescription = ref('')
const uploadedFiles = ref<File[]>([])
const isDragging = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const fallbackInput = ref('')
let voiceSessionSaved = false

// ─── Live voice reactive state ────────────────────────────────────────────────

const connectionState = ref<ConnectionState>('idle')
const isListening = ref(false)
const isModelSpeaking = ref(false)
const isTranscribing = ref(false)
const transcriptEntries = ref<TranscriptEntry[]>([])
const voiceInputBlocked = ref(false)
const interruptingModeEnabled = ref(false)

// Chat send (non-voice sessions)
function sendChat() {
  const text = chatInput.value.trim()
  if (!text) return
  if (chatMessages.value.length === 0) saveSessionToHistory()
  chatMessages.value.push({ role: 'user', text })
  chatInput.value = ''
  void scrollTranscriptToBottom()
}

// Session history (localStorage)
const HISTORY_KEY = 'socratica_session_history'

interface SessionRecord {
  id: string
  date: string
  type: string
  title: string
  topic: string
  messageCount: number
}

const sessionHistory = ref<SessionRecord[]>(
  JSON.parse(localStorage.getItem(HISTORY_KEY) ?? '[]')
)
const historyOpen = ref(false)

function saveSessionToHistory() {
  const record: SessionRecord = {
    id: Date.now().toString(),
    date: new Date().toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }),
    type: selectedMode.value,
    title: sessionTitle.value.trim(),
    topic: sessionTopic.value.trim().slice(0, 80),
    messageCount: isVoiceSession.value ? transcriptEntries.value.length : chatMessages.value.length,
  }
  sessionHistory.value.unshift(record)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(sessionHistory.value))
}

function deleteSession(id: string) {
  sessionHistory.value = sessionHistory.value.filter(s => s.id !== id)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(sessionHistory.value))
}

function clearHistory() {
  sessionHistory.value = []
  localStorage.removeItem(HISTORY_KEY)
}

// ─── Computed ─────────────────────────────────────────────────────────────────

const isBusy = computed(() => connectionState.value === 'connecting')
const isConnected = computed(() => connectionState.value === 'connected')
const isPresentationPrep = computed(() => selectedMode.value === 'Presentation Prep')

// Presentation Prep always has interruption mode off
watch(isPresentationPrep, (val) => {
  if (val) interruptingModeEnabled.value = false
}, { immediate: true })

// ─── Transcript scroll ref ────────────────────────────────────────────────────

const transcriptBody = ref<HTMLElement | null>(null)
const sessionBodyEl = ref<HTMLElement | null>(null)
const sessionBodyHasMore = ref(false)

function onSessionBodyScroll() {
  const el = sessionBodyEl.value
  if (!el) return
  sessionBodyHasMore.value = el.scrollTop + el.clientHeight < el.scrollHeight - 4
}

watch(selectedMode, async () => {
  await nextTick()
  onSessionBodyScroll()
})

// ─── Camera state (Presentation Prep) ────────────────────────────────────────

const cameraVideoEl = ref<HTMLVideoElement | null>(null)
const cameraGranted = ref(false)
const cameraError = ref('')
let cameraStream: MediaStream | null = null
let frameCaptureInterval: ReturnType<typeof setInterval> | null = null

// ─── Presentation phase state ─────────────────────────────────────────────────
const presentationPhase = ref<'intro' | 'countdown' | 'presenting'>('intro')
const countdownValue = ref<number | null>(null)

// ─── Non-reactive audio / session handles ────────────────────────────────────

let session: Session | null = null
let mediaStream: MediaStream | null = null
let audioContext: AudioContext | null = null       // mic pipeline only
let playbackContext: AudioContext | null = null    // AI audio output — separate so macOS never routes it through the communications device
let sourceNode: MediaStreamAudioSourceNode | null = null
let processorNode: ScriptProcessorNode | null = null
let sinkNode: GainNode | null = null
let playbackCursor = 0
let activePlaybackNodes: AudioBufferSourceNode[] = []
let hasSeenServerMessage = false
let serverMessageCount = 0
let rawSocketMessageCount = 0
let rawSocketListener: ((event: MessageEvent) => void) | null = null
let pendingSessionKickoff: (() => void) | null = null
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
  const isInterviewPrep = mode === 'Interview Prep' || mode === 'Cover Letter Analysis'
  const topic = isInterviewPrep
    ? [
        companyDescription.value.trim() ? `Company: ${companyDescription.value.trim()}.` : '',
        jobDescription.value.trim() ? `Job description: ${jobDescription.value.trim()}.` : '',
        sessionTitle.value.trim() ? `Role: ${sessionTitle.value.trim()}.` : '',
      ].filter(Boolean).join(' ') || 'an unspecified role'
    : sessionTopic.value.trim() || sessionTitle.value.trim() || 'the chosen subject'

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

  if (mode === 'Presentation Prep') {
    return [
      'You are Socratica, a warm and encouraging real-time presentation coach in a live video and voice session.',
      'You can see the student through their camera.',
      topic ? `The student will be presenting on: ${topic}.` : '',
      'The session has two phases:',
      'PHASE 1 — INTRO: Greet the student warmly and ask them to briefly tell you about their presentation: the topic, the intended audience, and what they most want to improve (eye contact, body language, voice, content, confidence, etc.). Ask one natural follow-up question if it helps clarify their goal. Keep this intro conversational and brief.',
      'When they are done explaining and ready to start, tell them: "Whenever you are ready, just say Start or click the Start button."',
      'PHASE 2 — PRESENTATION (triggered when you receive [PRESENTATION STARTED]): Enter silent observation mode immediately. Do NOT speak or interrupt while the student is actively presenting. Only after they clearly stop speaking (several seconds of silence) or explicitly ask for feedback (e.g. "what do you think?" or "pause"), give concise spoken feedback — two to four sentences maximum.',
      'Feedback should cover: (1) Eye contact — were they looking at the camera? (2) Body language — posture, gestures, fidgeting. (3) Voice — pace, tone, clarity, filler words. (4) Confidence and energy. (5) Content delivery — clarity and structure.',
      'Be warm, specific, and encouraging. Always highlight one thing done well before suggesting one concrete improvement.',
    ].filter(Boolean).join(' ')
  }

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

watch(isVoiceSession, (isVoice) => {
  if (!isVoice) personalizing.value = false
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
  playbackCursor = playbackContext ? playbackContext.currentTime : 0
  isModelSpeaking.value = false
}

const ensureAudioContext = async () => {
  // Mic pipeline context — may be routed by the OS to the comms device; keep separate.
  if (!audioContext) audioContext = new AudioContext()
  if (audioContext.state === 'suspended') await audioContext.resume()
  return audioContext
}

const ensurePlaybackContext = async () => {
  // Dedicated playback context, created independently of the mic pipeline so that
  // macOS / Chrome never routes it through the echo-cancellation / comms device.
  if (!playbackContext) {
    playbackContext = new AudioContext()
    playbackCursor = 0
  }
  if (playbackContext.state === 'suspended') await playbackContext.resume()
  // Warm up: play a silent 1-sample buffer to guarantee Chrome unlocks it.
  if (playbackContext.state === 'running') {
    const silence = playbackContext.createBuffer(1, 1, playbackContext.sampleRate)
    const src = playbackContext.createBufferSource()
    src.buffer = silence
    src.connect(playbackContext.destination)
    src.start()
  }
  return playbackContext
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
  const ctx = await ensurePlaybackContext()
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
    if (!activePlaybackNodes.length && playbackContext) {
      playbackCursor = playbackContext.currentTime
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

  // Gemini sends setupComplete once it's ready to receive clientContent.
  // Fire the kickoff message only at this point so it isn't ignored.
  if (message.setupComplete !== undefined) {
    if (pendingSessionKickoff) {
      const kickoff = pendingSessionKickoff
      pendingSessionKickoff = null
      kickoff()
    }
    return
  }

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

  // Reuse the AudioContext already created during user gesture; only create a new
  // one if none exists (handles edge cases where the pipeline was fully torn down).
  if (!audioContext) audioContext = new AudioContext()
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
      const suppressing = (assistantIsInterruptingUser && nowMs < assistantInterruptSuppressUntil)
        || presentationPhase.value === 'countdown'
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
  if (playbackContext) { await playbackContext.close(); playbackContext = null }
  capturedSpeechChunks = []
  speechActivityStarted = false
  silenceChunkCount = 0
  consecutiveSpeechChunks = 0
  isListening.value = false
  isModelSpeaking.value = false
}

// ─── Camera pipeline (Presentation Prep) ─────────────────────────────────────

const startCamera = async () => {
  cameraError.value = ''
  try {
    cameraStream = await navigator.mediaDevices.getUserMedia({ video: { width: 640, height: 480, facingMode: 'user' } })
    cameraGranted.value = true
    await nextTick()
    if (cameraVideoEl.value) {
      cameraVideoEl.value.srcObject = cameraStream
      await cameraVideoEl.value.play()
    }
  } catch {
    cameraGranted.value = false
    cameraError.value = 'Camera access denied. Please allow camera access and try again.'
  }
}

const stopCamera = () => {
  if (frameCaptureInterval !== null) { clearInterval(frameCaptureInterval); frameCaptureInterval = null }
  if (cameraStream) { cameraStream.getTracks().forEach(t => t.stop()); cameraStream = null }
  if (cameraVideoEl.value) cameraVideoEl.value.srcObject = null
  cameraGranted.value = false
  cameraError.value = ''
}

const startFrameCapture = () => {
  if (!cameraVideoEl.value || !session) return
  const canvas = document.createElement('canvas')
  canvas.width = 640
  canvas.height = 480
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  frameCaptureInterval = setInterval(() => {
    if (!session || !cameraVideoEl.value || !cameraGranted.value) return
    ctx.drawImage(cameraVideoEl.value, 0, 0, 640, 480)
    const base64 = canvas.toDataURL('image/jpeg', 0.7).split(',')[1]
    try {
      session.sendRealtimeInput({ video: { data: base64, mimeType: 'image/jpeg' } } as never)
    } catch { /* ignore if session not ready */ }
  }, 1000)
}

// ─── Presentation countdown ───────────────────────────────────────────────────

const beginCountdown = async () => {
  if (presentationPhase.value !== 'intro') return
  clearPlaybackQueue()
  presentationPhase.value = 'countdown'

  for (let i = 3; i >= 1; i--) {
    countdownValue.value = i
    await new Promise<void>(r => setTimeout(r, 1000))
  }

  countdownValue.value = null
  presentationPhase.value = 'presenting'

  if (session && connectionState.value === 'connected') {
    session.sendClientContent({
      turns: [{ role: 'user', parts: [{ text: '[PRESENTATION STARTED] The user is now presenting. Enter silent coach mode: do not speak while they are talking. Only respond after they clearly finish or explicitly ask for feedback.' }] }],
      turnComplete: true,
    })
  }
}

// ─── Speech recognition ───────────────────────────────────────────────────────

const detachRawSocketListener = () => {
  const rawSocket = session?.socket ?? null
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

        // Detect "start" keyword to begin the presentation countdown
        if (isPresentationPrep.value && presentationPhase.value === 'intro') {
          const hasStartKeyword = finalSegments.some(s => /\bstart\b/i.test(s))
          if (hasStartKeyword) {
            accumulatedRecognitionText = ''
            finalizeTranscript('You')
            beginCountdown()
            return
          }
        }

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

  // Create both AudioContexts during the user gesture (before any awaits) so Chrome
  // grants audio permission.  The playback context is kept separate from the mic
  // pipeline context to prevent macOS routing AI audio through the comms device.
  await ensureAudioContext()
  await ensurePlaybackContext()

  connectionState.value = 'connecting'
  transcriptEntries.value = []
  hasSeenServerMessage = false
  serverMessageCount = 0
  rawSocketMessageCount = 0
  presentationPhase.value = 'intro'
  countdownValue.value = null

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
        onclose: () => {
          if (connectionState.value === 'connected') {
            connectionState.value = 'idle'
          }
        },
      },
    })

    const rawSocket = (session as { conn?: { ws?: WebSocket } } | null)?.conn?.ws
    if (rawSocket?.addEventListener) {
      rawSocketListener = (event: MessageEvent) => {
        rawSocketMessageCount++
      }
      rawSocket.addEventListener('message', rawSocketListener)
    }

    voiceInputBlocked.value = false
    await startMicrophone()
    if (isPresentationPrep.value) {
      await startCamera()
    }
    startSpeechRecognition()

    connectionState.value = 'connected'

    if (isPresentationPrep.value) startFrameCapture()

    // Kick off the conversation immediately — the SDK handles setup internally.
    const mode = selectedMode.value || 'Socratic Evaluation'
    const isInterviewPrep = mode === 'Interview Prep' || mode === 'Cover Letter Analysis'
    const topic = isInterviewPrep
      ? [
          companyDescription.value.trim() ? `Company: ${companyDescription.value.trim()}.` : '',
          jobDescription.value.trim() ? `Job description: ${jobDescription.value.trim()}.` : '',
          sessionTitle.value.trim() ? `Role: ${sessionTitle.value.trim()}.` : '',
        ].filter(Boolean).join(' ') || 'an unspecified role'
      : sessionTopic.value.trim() || sessionTitle.value.trim() || 'the chosen subject'

    if (isPresentationPrep.value) {
      session.sendClientContent({
        turns: [{ role: 'user', parts: [{ text: 'Session starting. Please begin the intro phase.' }] }],
        turnComplete: true,
      })
    } else {
      session.sendClientContent({
        turns: [{
          role: 'user',
          parts: [{
            text: `The student wants to practice: ${topic}. Guide them using the ${mode} style. Greet them and invite them to begin in their own words.`,
          }],
        }],
        turnComplete: true,
      })
    }
  } catch (error) {
    console.error('Failed to start live session:', error)
    connectionState.value = 'error'
    pendingSessionKickoff = null
    stopSpeechRecognition()
    await stopAudioPipeline()
    detachRawSocketListener()
    if (session) { session.close(); session = null }
  }
}

const stopLiveSession = async () => {
  pendingSessionKickoff = null
  if (session) { detachRawSocketListener(); session.close(); session = null }
  stopSpeechRecognition()
  await stopAudioPipeline()
  if (isPresentationPrep.value) stopCamera()
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
  nextTick(onSessionBodyScroll)

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

/* Avatar speaking animation */
@keyframes avatar-breathe {
  0%, 100% { transform: scale(1);      filter: brightness(1); }
  50%       { transform: scale(1.016); filter: brightness(1.1); }
}

.spline-speaking {
  animation: avatar-breathe 1.6s ease-in-out infinite;
  /* keep zoom-in transform composable */
  transform-origin: center center;
}

.spline-speaking.spline-zoom {
  animation: avatar-breathe-zoomed 1.6s ease-in-out infinite;
}

@keyframes avatar-breathe-zoomed {
  0%, 100% { transform: scale(1.18);      filter: brightness(1); }
  50%       { transform: scale(1.197);    filter: brightness(1.1); }
}

/* Pulsing ring overlay */
.avatar-speaking-overlay {
  position: fixed;
  /* Avatar sits at roughly the top third of the viewport inside the Spline canvas */
  top: 33%;
  left: 50%;
  /* Centre on the point (overlay itself has no intrinsic size) */
  width: 0;
  height: 0;
  pointer-events: none;
  z-index: 1;
}

.avatar-ring {
  position: absolute;
  border-radius: 50%;
  border: 1.5px solid rgba(139, 92, 246, 0.45);
  /* Centre each ring on the overlay's anchor point */
  top: 0;
  left: 0;
  transform: translate(-50%, -50%);
  animation: ring-expand 2.4s ease-out infinite;
}

.ring-1 { width: 90px;  height: 90px;  animation-delay: 0s; }
.ring-2 { width: 90px;  height: 90px;  animation-delay: 0.8s; }
.ring-3 { width: 90px;  height: 90px;  animation-delay: 1.6s; }

@keyframes ring-expand {
  0%   { width: 90px;  height: 90px;  opacity: 0.6; }
  100% { width: 220px; height: 220px; opacity: 0; }
}

.speaking-rings-enter-active { transition: opacity 0.4s ease; }
.speaking-rings-leave-active  { transition: opacity 0.6s ease; }
.speaking-rings-enter-from,
.speaking-rings-leave-to      { opacity: 0; }

.personalize-btn-wrap {
  position: fixed;
  top: 78%; left: 50%;
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
  position: relative;
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

.session-panel::after {
  content: '';
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 80px;
  background: linear-gradient(to bottom, transparent, rgba(0, 0, 0, 0.7));
  border-radius: 0 0 20px 20px;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.session-panel.has-more::after {
  opacity: 1;
}

.session-panel.has-more::before {
  content: '';
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px; height: 20px;
  border-right: 2px solid rgba(255,255,255,0.5);
  border-bottom: 2px solid rgba(255,255,255,0.5);
  transform: translateX(-50%) rotate(45deg);
  pointer-events: none;
  z-index: 1;
  animation: scroll-hint-bounce 1.2s ease-in-out infinite;
}

@keyframes scroll-hint-bounce {
  0%, 100% { opacity: 0.3; bottom: 12px; }
  50% { opacity: 0.9; bottom: 8px; }
}

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

.field-textarea {
  resize: none;
  min-height: 80px;
  line-height: 1.5;
}

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
  bottom: 11%;
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
  z-index: 1;
  gap: 0.4rem;
}

.top-left     { top: 24%;    left: 34%; }
.top-right    { top: 32%;    right: 20%; }
.bottom-left  { bottom: 36%; left: 14%; }
.bottom-right { top: 58%; right: 12%; }
.bottom-center{ bottom: 12%; left: 18%; }

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

.p-card:has(.p-dropdown-menu) {
  z-index: 100;
}

.p-dropdown-menu {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  background: rgba(5, 5, 5, 0.92);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 14px;
  overflow: hidden;
  z-index: 300;
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
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
  color: rgba(247, 247, 242, 0.85);
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.85rem;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}

.p-dropdown-item:last-child { border-bottom: none; }
.p-dropdown-item:hover { background: rgba(255, 255, 255, 0.08); color: #F7F7F2; }
.p-dropdown-item.active { color: #F7F7F2; }

/* Voice button selected display */
.voice-btn-label {
  display: flex;
  align-items: baseline;
  gap: 0.45rem;
}

.voice-name {
  font-weight: 600;
  color: rgba(247, 247, 242, 0.9);
}

.voice-style {
  font-size: 0.73rem;
  color: rgba(203, 155, 81, 0.75);
  font-weight: 400;
}

/* Voice dropdown items */
.voice-dropdown-menu {
  max-height: 380px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.1) transparent;
}

.voice-dropdown-item {
  display: flex !important;
  flex-direction: column !important;
  align-items: flex-start !important;
  gap: 0.2rem;
  padding: 0.7rem 0.85rem !important;
}

.voice-item-header {
  display: flex;
  align-items: baseline;
  gap: 0.45rem;
}

.voice-item-name {
  font-weight: 600;
  font-size: 0.85rem;
  color: rgba(247, 247, 242, 0.95);
}

.voice-item-style {
  font-size: 0.72rem;
  color: rgba(203, 155, 81, 0.85);
  font-weight: 400;
}

.voice-item-desc {
  font-size: 0.71rem;
  color: rgba(247, 247, 242, 0.55);
  line-height: 1.4;
  font-weight: 400;
}

/* Style dropdown */
.style-dropdown-menu {
  max-height: 260px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.1) transparent;
  z-index: 200 !important;
  position: relative;
}

.style-dropdown-item {
  display: flex !important;
  flex-direction: column !important;
  align-items: flex-start !important;
  gap: 0.15rem;
  padding: 0.65rem 0.85rem !important;
}

.style-item-name {
  font-weight: 600;
  color: rgba(247, 247, 242, 0.95);
  font-size: 0.85rem;
}

.style-item-traits {
  font-size: 0.72rem;
  color: rgba(247, 247, 242, 0.55);
  font-weight: 400;
  line-height: 1.3;
}

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

/* ─── Camera panel ─────────────────────────────────────────────────────────── */

.camera-panel {
  flex-shrink: 0;
  width: 100%;
  height: 180px;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.camera-feed {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scaleX(-1);
  border-radius: 12px;
}

.camera-prompt,
.camera-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.6rem;
  padding: 1rem;
  text-align: center;
}

.camera-prompt svg,
.camera-error svg {
  width: 2rem;
  height: 2rem;
  opacity: 0.4;
}

.camera-prompt p,
.camera-error p {
  font-size: 0.78rem;
  color: rgba(247, 247, 242, 0.4);
  line-height: 1.4;
}

.camera-error svg { stroke: #f87171; opacity: 0.8; }
.camera-error p { color: #f87171; }

.transcript-panel--compact {
  flex: 1;
  min-height: 0;
}

/* ─── Countdown overlay ─────────────────────────────────────────────────────── */

.countdown-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.72);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  z-index: 10;
}

.countdown-number {
  font-size: 5rem;
  font-weight: 700;
  color: #fff;
  line-height: 1;
  text-shadow: 0 0 30px rgba(139, 92, 246, 0.8);
}

.countdown-fade-enter-active,
.countdown-fade-leave-active { transition: opacity 0.2s ease; }
.countdown-fade-enter-from,
.countdown-fade-leave-to { opacity: 0; }

.countdown-pop-enter-active { animation: countdown-pop-in 0.35s cubic-bezier(0.34, 1.56, 0.64, 1); }
.countdown-pop-leave-active { animation: countdown-pop-out 0.25s ease-in forwards; }

@keyframes countdown-pop-in {
  from { transform: scale(2.2); opacity: 0; }
  to   { transform: scale(1);   opacity: 1; }
}

@keyframes countdown-pop-out {
  from { transform: scale(1);   opacity: 1; }
  to   { transform: scale(0.5); opacity: 0; }
}

/* ─── Start Presentation button ──────────────────────────────────────────────── */

.start-presentation-wrap {
  display: flex;
  justify-content: center;
  padding: 0.4rem 0;
}

.start-presentation-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.45rem 1.1rem;
  border-radius: 20px;
  border: 1px solid rgba(139, 92, 246, 0.45);
  background: rgba(139, 92, 246, 0.12);
  color: rgba(247, 247, 242, 0.85);
  font-size: 0.8rem;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.start-presentation-btn:hover {
  background: rgba(139, 92, 246, 0.28);
  border-color: rgba(139, 92, 246, 0.7);
}

.start-presentation-btn svg {
  width: 13px;
  height: 13px;
  stroke: rgba(139, 92, 246, 0.9);
  fill: rgba(139, 92, 246, 0.15);
}

/* ─── Camera panel ─────────────────────────────────────────────────────────── */

.camera-panel {
  flex-shrink: 0;
  width: 100%;
  height: 180px;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.camera-feed {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scaleX(-1);
  border-radius: 12px;
}

.camera-prompt,
.camera-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.6rem;
  padding: 1rem;
  text-align: center;
}

.camera-prompt svg,
.camera-error svg {
  width: 2rem;
  height: 2rem;
  opacity: 0.4;
}

.camera-prompt p,
.camera-error p {
  font-size: 0.78rem;
  color: rgba(247, 247, 242, 0.4);
  line-height: 1.4;
}

.camera-error svg { stroke: #f87171; opacity: 0.8; }
.camera-error p { color: #f87171; }

.transcript-panel--compact {
  flex: 1;
  min-height: 0;
}

/* Interruption mode chips */
.interruption-modes {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  margin-top: 0.6rem;
}

.interruption-modes--overlay {
  margin-top: 0.75rem;
}

.interruption-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.3rem 0.7rem;
  border-radius: 9999px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.04);
  color: rgba(247, 247, 242, 0.45);
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.72rem;
  font-weight: 500;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease, color 0.2s ease;
  white-space: nowrap;
}

.interruption-chip svg {
  width: 11px;
  height: 11px;
  flex-shrink: 0;
  color: rgba(247, 247, 242, 0.85);
}

.interruption-chip:hover {
  border-color: rgba(255, 255, 255, 0.3);
  color: rgba(247, 247, 242, 0.75);
  background: rgba(255, 255, 255, 0.07);
}

.interruption-chip.active {
  border-color: rgba(247, 247, 242, 0.4);
  background: rgba(247, 247, 242, 0.1);
  color: rgba(247, 247, 242, 0.9);
}

/* History button */
.history-btn {
  position: fixed;
  top: 1.9rem;
  right: 2rem;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 9999px;
  padding: 0.5rem 1rem;
  color: rgba(247, 247, 242, 0.6);
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.8rem;
  font-weight: 500;
  letter-spacing: 0.06em;
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.history-btn svg {
  width: 15px;
  height: 15px;
  flex-shrink: 0;
}

.history-btn:hover {
  color: #F7F7F2;
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(0, 0, 0, 0.4);
}

/* History overlay */
.history-overlay {
  position: fixed;
  inset: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  padding: 2rem;
}

.history-panel {
  width: 100%;
  max-width: 640px;
  max-height: 80vh;
  background: rgba(10, 10, 10, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 1.75rem 1.25rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
  flex-shrink: 0;
}

.history-title {
  font-family: "Times New Roman", "Times", serif;
  font-size: 1.4rem;
  font-weight: 400;
  color: #F7F7F2;
  margin: 0;
  letter-spacing: 0.04em;
}

.history-close {
  background: transparent;
  border: none;
  color: rgba(247, 247, 242, 0.4);
  cursor: pointer;
  padding: 0.25rem;
  display: flex;
  transition: color 0.2s ease;
}

.history-close svg { width: 18px; height: 18px; }
.history-close:hover { color: #F7F7F2; }

.history-body {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 1.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.1) transparent;
}

.history-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 3rem 1rem;
  color: rgba(247, 247, 242, 0.25);
  text-align: center;
}

.history-empty svg {
  width: 36px;
  height: 36px;
  opacity: 0.4;
}

.history-empty p {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.9rem;
  line-height: 1.6;
  margin: 0;
}

.history-item {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  align-items: center;
  gap: 0.75rem;
  padding: 0.9rem 1rem;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.07);
  border-radius: 14px;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.history-item:hover {
  background: rgba(255, 255, 255, 0.07);
  border-color: rgba(255, 255, 255, 0.12);
}

.history-item-type {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.68rem;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(203, 155, 81, 0.8);
  background: rgba(203, 155, 81, 0.1);
  border: 1px solid rgba(203, 155, 81, 0.2);
  border-radius: 9999px;
  padding: 0.2rem 0.65rem;
  white-space: nowrap;
}

.history-item-main {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  min-width: 0;
}

.history-item-title {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.88rem;
  font-weight: 600;
  color: rgba(247, 247, 242, 0.85);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-item-topic {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.76rem;
  color: rgba(247, 247, 242, 0.35);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-item-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.15rem;
  flex-shrink: 0;
}

.history-item-date {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.72rem;
  color: rgba(247, 247, 242, 0.3);
}

.history-item-count {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.7rem;
  color: rgba(247, 247, 242, 0.2);
}

.history-item-delete {
  background: transparent;
  border: none;
  color: rgba(247, 247, 242, 0.2);
  cursor: pointer;
  padding: 0.2rem;
  display: flex;
  transition: color 0.15s ease;
  flex-shrink: 0;
}

.history-item-delete svg { width: 13px; height: 13px; }
.history-item-delete:hover { color: rgba(247, 247, 242, 0.7); }

.history-footer {
  padding: 1rem 1.75rem 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.07);
  flex-shrink: 0;
  display: flex;
  justify-content: center;
}

.history-clear {
  background: transparent;
  border: none;
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.78rem;
  color: rgba(247, 247, 242, 0.25);
  cursor: pointer;
  letter-spacing: 0.04em;
  transition: color 0.2s ease;
}

.history-clear:hover { color: rgba(247, 247, 242, 0.6); }

/* History transition */
.history-enter-active,
.history-leave-active {
  transition: opacity 0.25s ease;
}
.history-enter-from,
.history-leave-to {
  opacity: 0;
}
</style>
