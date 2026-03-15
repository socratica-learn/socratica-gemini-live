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

          <!-- Start Live Voice button (voice sessions only) -->
          <button v-if="isVoiceSession" class="btn-live">
            <svg class="mic-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="9" y="2" width="6" height="11" rx="3"/>
              <path d="M5 10a7 7 0 0014 0M12 19v3M9 22h6"/>
            </svg>
            Start Live Voice
          </button>
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
              <transition name="dropdown">
                <div v-if="interruptionMode" class="interruption-modes">
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
      <!-- Voice session: transcript + fallback -->
      <div v-if="isVoiceSession" class="right-panels">
        <div class="transcript-panel">
          <div class="transcript-header">
            <span class="transcript-label">Transcript</span>
            <span class="transcript-status" :class="{ active: messages.length > 0 }">
              <span class="status-dot"></span>
              {{ messages.length > 0 ? 'Live' : 'Waiting...' }}
            </span>
          </div>
          <div class="transcript-body" ref="transcriptBody">
            <div v-if="messages.length === 0" class="transcript-empty">
              Your conversation will appear here once the session starts.
            </div>
            <div v-for="(msg, i) in messages" :key="i" class="transcript-msg" :class="msg.role">
              <span class="msg-role">{{ msg.role === 'user' ? 'You' : 'Socratica' }}</span>
              <p class="msg-text">{{ msg.text }}</p>
            </div>
          </div>
        </div>
        <div class="fallback-panel">
          <form class="fallback-form" @submit.prevent="sendFallback">
            <input v-model="fallbackInput" class="fallback-input" type="text" placeholder="Type if voice fails..." autocomplete="off" />
            <button class="fallback-send" type="submit" :disabled="!fallbackInput.trim()">
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
import { onMounted, onUnmounted, ref, nextTick, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Application } from '@splinetool/runtime'

const router = useRouter()

const splineCanvas = ref<HTMLCanvasElement | null>(null)
let splineApp: Application | null = null

const voiceSessions = ['Presentation Prep', 'Socratic Evaluation', 'Interview Prep']
const isVoiceSession = computed(() => voiceSessions.includes(selectedMode.value))

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

// Transcript (voice sessions)
const transcriptBody = ref<HTMLElement | null>(null)
const messages = ref<{ role: 'user' | 'ai'; text: string }[]>([])

// Chat (non-voice sessions)
const chatMessages = ref<{ role: 'user' | 'ai'; text: string }[]>([])
const chatInput = ref('')

// Panel visibility
const sessionVisible = ref(true)
const transcriptVisible = ref(true)
const personalizing = ref(false)

// Personalize options
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

function handleEscape(e: KeyboardEvent) {
  if (e.key === 'Escape') personalizing.value = false
}

watch(isVoiceSession, (isVoice) => {
  if (!isVoice) {
    personalizing.value = false
    messages.value = []
  } else {
    chatMessages.value = []
  }
  voiceSessionSaved = false
})

// Session details
const sessionTitle = ref('')
const sessionTopic = ref('')
const uploadedFiles = ref<File[]>([])
const isDragging = ref(false)
const interruptionMode = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files) uploadedFiles.value.push(...Array.from(input.files))
}

function onDrop(e: DragEvent) {
  isDragging.value = false
  if (e.dataTransfer?.files) uploadedFiles.value.push(...Array.from(e.dataTransfer.files))
}

function removeFile(index: number) {
  uploadedFiles.value.splice(index, 1)
}

// Fallback input (voice sessions)
const fallbackInput = ref('')
let voiceSessionSaved = false

function sendFallback() {
  const text = fallbackInput.value.trim()
  if (!text) return
  if (!voiceSessionSaved) { saveSessionToHistory(); voiceSessionSaved = true }
  addMessage('user', text)
  fallbackInput.value = ''
}

// Chat send (non-voice sessions)
function sendChat() {
  const text = chatInput.value.trim()
  if (!text) return
  if (chatMessages.value.length === 0) saveSessionToHistory()
  chatMessages.value.push({ role: 'user', text })
  chatInput.value = ''
  scrollToBottom()
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
    messageCount: isVoiceSession.value ? messages.value.length : chatMessages.value.length,
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

function toggleDropdown() {
  dropdownOpen.value = !dropdownOpen.value
}

function selectMode(option: string) {
  selectedMode.value = option
  dropdownOpen.value = false
}

function handleOutsideClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (!target.closest('.dropdown-wrapper')) {
    dropdownOpen.value = false
  }
}

async function scrollToBottom() {
  await nextTick()
  if (transcriptBody.value) {
    transcriptBody.value.scrollTop = transcriptBody.value.scrollHeight
  }
}

// Call this from parent/service to push messages into the transcript
function addMessage(role: 'user' | 'ai', text: string) {
  messages.value.push({ role, text })
  scrollToBottom()
}

defineExpose({ addMessage })

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
          bubbles: true,
          cancelable: true,
          clientX,
          clientY: centerY,
          screenX: clientX,
          screenY: centerY,
          movementX: 0,
          movementY: 0,
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

onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick)
  document.removeEventListener('keydown', handleEscape)
  if (splineApp) {
    splineApp.dispose()
    splineApp = null
  }
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

.back-btn:hover {
  color: #F7F7F2;
}

.back-arrow {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

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

.back-btn:hover .back-label {
  opacity: 1;
  transform: translateX(0);
}

.spline-bg {
  position: fixed;
  top: 15vh;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 85vh;
  z-index: 0;
}

/* Fade out UI elements */
.hidden-ui {
  opacity: 0 !important;
  pointer-events: none !important;
  transition: opacity 0.7s ease !important;
}

/* Zoom in the Spline canvas */
.spline-bg {
  transition: transform 1.2s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  transform-origin: center center;
}

.spline-zoom {
  transform: scale(1.18);
}

/* Personalize button */
.personalize-btn-wrap {
  position: fixed;
  top: 52%;
  left: 50%;
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

/* Hero (left-center) */
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
    #8B6914 0%,
    #cb9b51 22%,
    #f6e27a 45%,
    #f6f2c0 50%,
    #f6e27a 55%,
    #cb9b51 78%,
    #8B6914 100%
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

/* Actions row */
.build-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 1rem;
  pointer-events: all;
}

/* Shared button base */
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
  transition:
    transform 180ms ease,
    box-shadow 220ms ease,
    border-color 220ms ease;
}

/* Dropdown */
.dropdown-wrapper {
  position: relative;
}

.btn-dropdown {
  gap: 0.75rem;
  background: transparent;
  color: #F7F7F2;
  border: 1px solid rgba(247, 247, 242, 0.3);
  min-width: 220px;
  justify-content: space-between;
}

.btn-dropdown:hover {
  transform: translateY(-1px);
  border-color: rgba(247, 247, 242, 0.6);
}

.placeholder {
  color: rgba(247, 247, 242, 0.4);
}

.chevron {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  transition: transform 0.25s ease;
}

.chevron.open {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: rgba(15, 15, 15, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  overflow: hidden;
  z-index: 200;
  min-width: 220px;
  backdrop-filter: blur(12px);
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 0.9rem 1.4rem;
  background: transparent;
  border: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  color: rgba(247, 247, 242, 0.75);
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.95rem;
  font-weight: 400;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}

.dropdown-item:last-child {
  border-bottom: none;
}

.dropdown-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #F7F7F2;
}

.dropdown-item.active {
  background: transparent;
  color: #F7F7F2;
}

/* Dropdown transition */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* Live voice button */
.btn-live {
  gap: 0.6rem;
  background: #F7F7F2;
  color: #000000;
  border: 1px solid #F7F7F2;
}

.btn-live:hover {
  transform: translateY(-1px);
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.4);
}

.mic-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

/* Right wrapper */
.transcript-wrapper {
  position: fixed;
  top: 13rem;
  bottom: 4rem;
  right: 2.5rem;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 0;
  z-index: 5;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
}

.transcript-wrapper.hidden {
  transform: translateX(calc(360px + 2.5rem));
}

.right-panels {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 1rem;
  pointer-events: all;
  width: 360px;
}

/* Transcript panel (inside wrapper, not fixed anymore) */
.transcript-panel {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  overflow: hidden;
}

.transcript-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem 0.75rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  flex-shrink: 0;
}

.transcript-label {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: rgba(247, 247, 242, 0.5);
}

.transcript-status {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.75rem;
  color: rgba(247, 247, 242, 0.35);
}

.transcript-status.active {
  color: rgba(247, 247, 242, 0.6);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(247, 247, 242, 0.25);
}

.transcript-status.active .status-dot {
  background: #6ee7b7;
  box-shadow: 0 0 6px #6ee7b7;
  animation: pulse 2s ease-in-out infinite;
}

.transcript-body {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.1) transparent;
}

.transcript-body::-webkit-scrollbar {
  width: 4px;
}
.transcript-body::-webkit-scrollbar-track {
  background: transparent;
}
.transcript-body::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.1);
  border-radius: 2px;
}

.transcript-empty {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.82rem;
  color: rgba(247, 247, 242, 0.25);
  text-align: center;
  line-height: 1.6;
  padding: 1rem 0;
}

.transcript-msg {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.msg-role {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: rgba(247, 247, 242, 0.35);
}

.transcript-msg.ai .msg-role {
  color: rgba(203, 155, 81, 0.7);
}

.msg-text {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.875rem;
  line-height: 1.6;
  color: rgba(247, 247, 242, 0.8);
  margin: 0;
}

/* Panel tab (shared) */
.panel-tab {
  flex-shrink: 0;
  width: 22px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.2);
  cursor: pointer;
  color: rgba(247, 247, 242, 0.5);
  transition: color 0.2s ease, background 0.2s ease;
  pointer-events: all;
}

.panel-tab svg {
  width: 12px;
  height: 12px;
  flex-shrink: 0;
}

.panel-tab:hover {
  color: #F7F7F2;
  background: rgba(255, 255, 255, 0.12);
}

/* Left wrapper: panel then tab on its right */
.session-wrapper {
  position: fixed;
  top: 23rem;
  bottom: 4rem;
  left: 2.5rem;
  display: flex;
  flex-direction: row;
  align-items: center;
  z-index: 5;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none; /* panels override this individually */
}

.session-wrapper.hidden .panel-tab,
.transcript-wrapper.hidden .panel-tab {
  pointer-events: all;
}

.session-wrapper.hidden {
  transform: translateX(calc(-300px - 2.5rem));
}

/* Tab on the right edge of session wrapper */
.session-wrapper .panel-tab {
  border-left: none;
  border-radius: 0 10px 10px 0;
}

/* Session panel (inside wrapper) */
.session-panel {
  width: 300px;
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  overflow: hidden;
  pointer-events: all;
}

.session-header {
  display: flex;
  align-items: center;
  padding: 1rem 1.25rem 0.75rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  flex-shrink: 0;
}

.session-label {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: rgba(247, 247, 242, 0.5);
}

.session-body {
  flex: 1;
  overflow-y: auto;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.1) transparent;
}

.session-body::-webkit-scrollbar { width: 4px; }
.session-body::-webkit-scrollbar-track { background: transparent; }
.session-body::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 2px; }

.session-field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.field-label {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: rgba(247, 247, 242, 0.4);
}

.field-hint {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.78rem;
  color: rgba(247, 247, 242, 0.3);
  margin: 0.2rem 0 0;
  line-height: 1.4;
}

.field-input {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  padding: 0.65rem 0.9rem;
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.875rem;
  color: #F7F7F2;
  outline: none;
  transition: border-color 0.2s ease;
}

.field-textarea {
  resize: none;
  line-height: 1.6;
  min-height: 80px;
}

.field-input::placeholder {
  color: rgba(247, 247, 242, 0.25);
}

.field-input:focus {
  border-color: rgba(255, 255, 255, 0.25);
}

/* File drop zone */
.file-drop {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
  padding: 1rem;
  border: 1px dashed rgba(255, 255, 255, 0.18);
  border-radius: 10px;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.file-drop svg {
  width: 22px;
  height: 22px;
  color: rgba(247, 247, 242, 0.3);
}

.file-drop span {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.8rem;
  color: rgba(247, 247, 242, 0.35);
}

.file-drop u {
  color: rgba(247, 247, 242, 0.6);
  text-decoration-color: rgba(247, 247, 242, 0.3);
}

.file-drop:hover,
.file-drop.dragging {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.04);
}

.hidden-file-input {
  display: none;
}

.file-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.45rem 0.6rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
}

.file-item svg {
  width: 14px;
  height: 14px;
  color: rgba(247, 247, 242, 0.4);
  flex-shrink: 0;
}

.file-name {
  flex: 1;
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.78rem;
  color: rgba(247, 247, 242, 0.7);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-remove {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
  display: flex;
  color: rgba(247, 247, 242, 0.25);
  transition: color 0.15s ease;
}

.file-remove:hover {
  color: rgba(247, 247, 242, 0.7);
}

.file-remove svg {
  width: 12px;
  height: 12px;
}

/* Interruption toggle */
.toggle-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
}

.toggle {
  flex-shrink: 0;
  width: 42px;
  height: 24px;
  border-radius: 9999px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.08);
  cursor: pointer;
  position: relative;
  transition: background 0.25s ease, border-color 0.25s ease;
  margin-top: 2px;
}

.toggle.on {
  background: rgba(247, 247, 242, 0.85);
  border-color: transparent;
}

.toggle-thumb {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: rgba(247, 247, 242, 0.5);
  transition: transform 0.25s ease, background 0.25s ease;
}

.toggle.on .toggle-thumb {
  transform: translateX(18px);
  background: #000000;
}

/* Tab on the left edge of transcript wrapper */
.transcript-wrapper .panel-tab {
  border-right: none;
  border-radius: 10px 0 0 10px;
}

/* Fallback panel (inside right wrapper, not fixed) */
.fallback-panel {
  flex-shrink: 0;
  pointer-events: all;
}

.fallback-form {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 9999px;
  padding: 0.5rem 0.5rem 0.5rem 1.25rem;
}

.fallback-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.9rem;
  color: #F7F7F2;
  letter-spacing: 0.02em;
}

.fallback-input::placeholder {
  color: rgba(247, 247, 242, 0.35);
}

.fallback-send {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.12);
  color: rgba(247, 247, 242, 0.6);
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.2s ease, color 0.2s ease;
}

.fallback-send:not(:disabled):hover {
  background: #F7F7F2;
  color: #000000;
}

.fallback-send:disabled {
  opacity: 0.3;
  cursor: default;
}

.fallback-send svg {
  width: 15px;
  height: 15px;
}

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
  max-height: 240px;
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
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
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
