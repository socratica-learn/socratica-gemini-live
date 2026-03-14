<template>
  <section class="review-page">
    <!-- Header -->
    <header class="hero-card">
      <div>
        <p class="eyebrow">Gemini Document Review</p>
        <h1>AI Document Reviewer</h1>
        <p class="hero-copy">
          Upload a PDF, Word, or PowerPoint file and let Google Gemini detect the document type,
          evaluate content quality, and give you context-aware feedback on structure, clarity, and style.
        </p>
      </div>
      <div class="hero-actions">
        <button class="secondary-button" type="button" @click="goHome">Back Home</button>
      </div>
    </header>

    <!-- Upload zone + results -->
    <div class="content-grid">
      <!-- Left: upload -->
      <article class="panel upload-panel">
        <h2>Upload Document</h2>
        <p class="panel-sub">Supported: PDF, DOCX, PPTX, PPT, TXT, MD</p>

        <!-- Drop zone -->
        <div
          class="drop-zone"
          :class="{ 'drop-zone--active': isDragging, 'drop-zone--filled': selectedFile }"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="onDrop"
          @click="triggerFileInput"
        >
          <input
            ref="fileInputRef"
            type="file"
            class="hidden-input"
            accept=".pdf,.docx,.pptx,.ppt,.txt,.md"
            @change="onFileChange"
          />

          <div v-if="!selectedFile" class="drop-zone-content">
            <span class="upload-icon" aria-hidden="true">📄</span>
            <p class="drop-label">Drag &amp; drop your document here</p>
            <p class="drop-sub">or click to browse files</p>
          </div>

          <div v-else class="drop-zone-content file-selected">
            <span class="upload-icon" aria-hidden="true">✅</span>
            <p class="drop-label">{{ selectedFile.name }}</p>
            <p class="drop-sub">{{ formatBytes(selectedFile.size) }}</p>
          </div>
        </div>

        <div v-if="selectedFile" class="upload-actions">
          <button
            class="primary-button"
            type="button"
            :disabled="isAnalyzing"
            @click="analyzeDocument"
          >
            {{ isAnalyzing ? 'Analyzing...' : 'Analyze with Gemini' }}
          </button>
          <button class="secondary-button" type="button" :disabled="isAnalyzing" @click="reset">
            Clear
          </button>
        </div>

        <!-- Error -->
        <div v-if="errorMessage" class="error-box">
          <strong>Error:</strong> {{ errorMessage }}
        </div>

        <!-- Google stack badge -->
        <div class="google-badge">
          <span class="badge-label">Powered by</span>
          <span class="badge-item">Google Gemini 2.5 Flash</span>
          <span class="badge-sep">·</span>
          <span class="badge-item">Google GenAI SDK</span>
        </div>
      </article>

      <!-- Right: results -->
      <article class="panel results-panel">
        <!-- Loading state -->
        <div v-if="isAnalyzing" class="loading-state">
          <div class="spinner" aria-hidden="true"></div>
          <p>Gemini is reading your document…</p>
        </div>

        <!-- Empty state -->
        <div v-else-if="!review" class="empty-state-panel">
          <span class="empty-icon" aria-hidden="true">🔍</span>
          <p>Upload a document and click <strong>Analyze with Gemini</strong> to see your review here.</p>
        </div>

        <!-- Review results -->
        <template v-else>
          <!-- Doc type + summary -->
          <div class="result-header">
            <div class="doc-type-badge" :class="`type--${review.documentType}`">
              {{ review.documentTypeLabel }}
            </div>
            <p class="result-summary">{{ review.summary }}</p>
          </div>

          <!-- Quality scores -->
          <section class="scores-section">
            <h3>Quality Scores</h3>
            <div class="scores-grid">
              <div
                v-for="(score, dim) in review.qualityScores"
                :key="dim"
                class="score-card"
              >
                <span class="score-dim">{{ formatDimension(dim) }}</span>
                <div class="score-bar-wrap">
                  <div
                    class="score-bar"
                    :style="{ width: (score * 10) + '%' }"
                    :class="scoreClass(score)"
                  ></div>
                </div>
                <span class="score-value">{{ score }}/10</span>
              </div>
            </div>
          </section>

          <!-- Strengths -->
          <section class="feedback-section">
            <h3 class="section-heading strengths-heading">
              <span class="section-icon" aria-hidden="true">✅</span> Strengths
            </h3>
            <ul class="feedback-list">
              <li v-for="(item, i) in review.strengths" :key="i" class="feedback-item strength-item">
                {{ item }}
              </li>
            </ul>
          </section>

          <!-- Weaknesses -->
          <section class="feedback-section">
            <h3 class="section-heading weaknesses-heading">
              <span class="section-icon" aria-hidden="true">⚠️</span> Weaknesses
            </h3>
            <ul class="feedback-list">
              <li v-for="(item, i) in review.weaknesses" :key="i" class="feedback-item weakness-item">
                {{ item }}
              </li>
            </ul>
          </section>

          <!-- Suggestions -->
          <section class="feedback-section">
            <h3 class="section-heading suggestions-heading">
              <span class="section-icon" aria-hidden="true">💡</span> Actionable Suggestions
            </h3>
            <ol class="feedback-list suggestions-list">
              <li v-for="(item, i) in review.suggestions" :key="i" class="feedback-item suggestion-item">
                {{ item }}
              </li>
            </ol>
          </section>
        </template>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

interface DocumentReviewResponse {
  documentType: string
  documentTypeLabel: string
  summary: string
  strengths: string[]
  weaknesses: string[]
  suggestions: string[]
  qualityScores: Record<string, number>
}

const router = useRouter()

const selectedFile = ref<File | null>(null)
const isDragging = ref(false)
const isAnalyzing = ref(false)
const review = ref<DocumentReviewResponse | null>(null)
const errorMessage = ref('')
const fileInputRef = ref<HTMLInputElement | null>(null)

function goHome() {
  router.push('/')
}

function triggerFileInput() {
  fileInputRef.value?.click()
}

function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.files?.[0]) {
    setFile(target.files[0])
  }
}

function onDrop(e: DragEvent) {
  isDragging.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file) setFile(file)
}

function setFile(file: File) {
  selectedFile.value = file
  review.value = null
  errorMessage.value = ''
}

function reset() {
  selectedFile.value = null
  review.value = null
  errorMessage.value = ''
  if (fileInputRef.value) fileInputRef.value.value = ''
}

async function analyzeDocument() {
  if (!selectedFile.value) return

  isAnalyzing.value = true
  errorMessage.value = ''
  review.value = null

  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)

    const backendUrl = import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8080'
    const response = await fetch(`${backendUrl}/api/ai/document-review`, {
      method: 'POST',
      body: formData,
    })

    if (!response.ok) {
      const text = await response.text()
      throw new Error(text || `Server error: ${response.status}`)
    }

    review.value = (await response.json()) as DocumentReviewResponse
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : 'Unknown error occurred.'
  } finally {
    isAnalyzing.value = false
  }
}

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function formatDimension(dim: string): string {
  return dim.charAt(0).toUpperCase() + dim.slice(1)
}

function scoreClass(score: number): string {
  if (score >= 8) return 'bar--high'
  if (score >= 5) return 'bar--mid'
  return 'bar--low'
}
</script>

<style scoped>
/* ── Page shell ─────────────────────────────────────────── */
.review-page {
  min-height: 100vh;
  background: #000;
  color: #e8e0d0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  padding: 0 0 4rem;
}

/* ── Hero card ──────────────────────────────────────────── */
.hero-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 2rem;
  padding: 2.5rem 3rem;
  background: linear-gradient(135deg, #0a0a0a 0%, #111 60%, #1a1200 100%);
  border-bottom: 1px solid #2a2200;
}

.eyebrow {
  font-size: 0.75rem;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: #c9a84c;
  margin: 0 0 0.4rem;
}

.hero-card h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #fff;
  margin: 0 0 0.6rem;
  letter-spacing: -0.02em;
}

.hero-copy {
  font-size: 0.95rem;
  color: #9a9080;
  max-width: 540px;
  line-height: 1.6;
  margin: 0;
}

.hero-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  flex-shrink: 0;
}

/* ── Grid layout ────────────────────────────────────────── */
.content-grid {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 1.5rem;
  padding: 2rem 3rem;
  align-items: start;
}

@media (max-width: 900px) {
  .content-grid {
    grid-template-columns: 1fr;
    padding: 1.5rem;
  }
}

/* ── Panels ─────────────────────────────────────────────── */
.panel {
  background: #0e0e0e;
  border: 1px solid #222;
  border-radius: 12px;
  padding: 1.75rem;
}

.panel h2 {
  font-size: 1rem;
  font-weight: 600;
  color: #fff;
  margin: 0 0 0.25rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.panel-sub {
  font-size: 0.8rem;
  color: #6a6050;
  margin: 0 0 1.25rem;
}

/* ── Drop zone ──────────────────────────────────────────── */
.drop-zone {
  border: 2px dashed #2a2200;
  border-radius: 10px;
  padding: 2.5rem 1.5rem;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
  background: #0a0a0a;
}

.drop-zone:hover,
.drop-zone--active {
  border-color: #c9a84c;
  background: #120f00;
}

.drop-zone--filled {
  border-color: #3a8c5a;
  border-style: solid;
  background: #04120a;
}

.hidden-input {
  display: none;
}

.drop-zone-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
}

.upload-icon {
  font-size: 2.5rem;
}

.drop-label {
  font-size: 0.95rem;
  color: #e8e0d0;
  margin: 0;
}

.drop-sub {
  font-size: 0.78rem;
  color: #6a6050;
  margin: 0;
}

/* ── Upload actions ─────────────────────────────────────── */
.upload-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
}

/* ── Error box ──────────────────────────────────────────── */
.error-box {
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  background: #1a0505;
  border: 1px solid #6b1515;
  border-radius: 8px;
  font-size: 0.85rem;
  color: #e08080;
}

/* ── Google stack badge ─────────────────────────────────── */
.google-badge {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.4rem;
  margin-top: 1.5rem;
  padding: 0.6rem 0.9rem;
  background: #0a0a00;
  border: 1px solid #2a2200;
  border-radius: 8px;
  font-size: 0.72rem;
}

.badge-label {
  color: #6a6050;
}

.badge-item {
  color: #c9a84c;
  font-weight: 500;
}

.badge-sep {
  color: #3a3020;
}

/* ── Loading / empty ────────────────────────────────────── */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.25rem;
  padding: 3rem 0;
  color: #9a9080;
  font-size: 0.9rem;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #2a2200;
  border-top-color: #c9a84c;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 3rem 2rem;
  text-align: center;
  color: #6a6050;
  font-size: 0.9rem;
}

.empty-icon {
  font-size: 2.5rem;
}

/* ── Result header ──────────────────────────────────────── */
.result-header {
  margin-bottom: 1.75rem;
}

.doc-type-badge {
  display: inline-block;
  padding: 0.3rem 0.9rem;
  border-radius: 20px;
  font-size: 0.78rem;
  font-weight: 600;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  margin-bottom: 0.75rem;
  background: #1a1200;
  border: 1px solid #c9a84c;
  color: #c9a84c;
}

.type--presentation { border-color: #7c6fd4; color: #7c6fd4; background: #0f0d1a; }
.type--research_paper { border-color: #4a9eff; color: #4a9eff; background: #050d1a; }
.type--report { border-color: #4acf8f; color: #4acf8f; background: #040f09; }
.type--essay { border-color: #e9a84c; color: #e9a84c; background: #130d00; }
.type--notes { border-color: #9a9080; color: #9a9080; background: #0a0a0a; }

.result-summary {
  font-size: 0.9rem;
  color: #b0a898;
  line-height: 1.65;
  margin: 0;
}

/* ── Scores ─────────────────────────────────────────────── */
.scores-section {
  margin-bottom: 1.75rem;
}

.scores-section h3,
.feedback-section h3 {
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin: 0 0 0.9rem;
}

.scores-section h3 { color: #c9a84c; }

.scores-grid {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.score-card {
  display: grid;
  grid-template-columns: 130px 1fr 44px;
  align-items: center;
  gap: 0.6rem;
}

.score-dim {
  font-size: 0.8rem;
  color: #9a9080;
}

.score-bar-wrap {
  height: 6px;
  background: #1a1a1a;
  border-radius: 3px;
  overflow: hidden;
}

.score-bar {
  height: 100%;
  border-radius: 3px;
  transition: width 0.6s ease;
}

.bar--high { background: #4acf8f; }
.bar--mid  { background: #c9a84c; }
.bar--low  { background: #cf4a4a; }

.score-value {
  font-size: 0.78rem;
  color: #9a9080;
  text-align: right;
}

/* ── Feedback sections ──────────────────────────────────── */
.feedback-section {
  margin-bottom: 1.5rem;
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.strengths-heading  { color: #4acf8f; }
.weaknesses-heading { color: #e07050; }
.suggestions-heading { color: #c9a84c; }

.section-icon { font-size: 1rem; }

.feedback-list {
  margin: 0;
  padding-left: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
}

.suggestions-list {
  padding-left: 1.5rem;
}

.feedback-item {
  font-size: 0.875rem;
  line-height: 1.55;
  color: #c8c0b0;
}

.strength-item   { color: #a8d8b8; }
.weakness-item   { color: #d8a898; }
.suggestion-item { color: #d8c898; }

/* ── Buttons ─────────────────────────────────────────────── */
.primary-button {
  padding: 0.55rem 1.4rem;
  background: #c9a84c;
  color: #000;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, opacity 0.2s;
}

.primary-button:hover:not(:disabled) { background: #e0bf66; }
.primary-button:disabled { opacity: 0.45; cursor: not-allowed; }

.secondary-button {
  padding: 0.55rem 1.2rem;
  background: transparent;
  color: #9a9080;
  border: 1px solid #3a3020;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
}

.secondary-button:hover:not(:disabled) { border-color: #c9a84c; color: #c9a84c; }
.secondary-button:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
