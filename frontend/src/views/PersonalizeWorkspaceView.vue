<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import { personalizeService } from '../services/personalizeService'
import logo from '@/assets/logo.png'
import personalizeImage from '@/assets/PersonalizeYourWorkspace.png'

const route = useRoute()
const authStore = useAuthStore()

// Custom dropdown state (native select dropdown can't be styled – blue hover, sharp corners)
const openDropdown = ref<'learningPace' | 'focusSession' | 'experience' | null>(null)

const learningPaceOptions = [
  { value: 'slow-deep', label: 'Slow & deep (detailed explanations, concepts first)' },
  { value: 'balanced', label: 'Balanced (theory + practice)' },
  { value: 'fast-efficient', label: 'Fast & efficient (straight to key points)' },
  { value: 'adaptive', label: 'Adaptive (adjust automatically)' }
]

const focusSessionOptions = [
  { value: '15-30', label: '15–30 minutes' },
  { value: '30-60', label: '30–60 minutes' },
  { value: '1-2-hours', label: '1–2 hours' },
  { value: 'flexible', label: 'Flexible' }
]

const experienceLevelOptions = [
  { value: 'beginner', label: 'Beginner' },
  { value: 'intermediate', label: 'Intermediate' },
  { value: 'advanced', label: 'Advanced' },
  { value: 'expert', label: 'Expert' }
]

function toggleDropdown(id: 'learningPace' | 'focusSession' | 'experience') {
  openDropdown.value = openDropdown.value === id ? null : id
}

function closeDropdown() {
  openDropdown.value = null
}

// Allow step to be set via query parameter for testing (e.g., ?step=2)
const currentStep = ref(route.query.step ? parseInt(route.query.step as string) : 1)

// Step 1: Personal Details
const personalDetailsData = ref({
  fullName: '',
  preferredName: '',
  countryTimeZone: '',
  currentRole: '',
})

// Step 2: Academic Background
const academicBackgroundData = ref({
  fieldOfStudy: '',
  currentRole: '',
  experienceLevel: '',
  backgroundDescription: '',
})

const totalSteps = 2
const progressStep = ref(2)

function prevStep() {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

function goToStep(step: number) {
  currentStep.value = step
}

const saving = ref(false)
const saveError = ref<string | null>(null)

async function handleSaveAndContinue() {
  // Check if user is authenticated
  if (!authStore.user?.id) {
    saveError.value = 'Please sign in to save your preferences'
    return
  }

  saving.value = true
  saveError.value = null

  try {
    if (currentStep.value === 1) {
      await personalizeService.savePersonalDetails(authStore.user.id, {
        fullName: personalDetailsData.value.fullName,
        preferredName: personalDetailsData.value.preferredName,
        countryTimeZone: personalDetailsData.value.countryTimeZone,
        currentRole: personalDetailsData.value.currentRole,
      })
      console.log('Personal Details saved successfully')
      currentStep.value = 2
      progressStep.value = 2
      return
    } else if (currentStep.value === 2) {
      await personalizeService.saveAcademicBackground(authStore.user.id, {
        fieldOfStudy: academicBackgroundData.value.fieldOfStudy,
        currentRole: academicBackgroundData.value.currentRole,
        experienceLevel: academicBackgroundData.value.experienceLevel,
        backgroundDescription: academicBackgroundData.value.backgroundDescription,
      })
      console.log('Academic Background saved successfully')
      progressStep.value = 3
      return
    }

    // Move to next step
    if (currentStep.value < totalSteps) {
      currentStep.value++
    }
  } catch (error: any) {
    console.error('Error saving data:', error)
    saveError.value = error.response?.data?.message || 'Failed to save. Please try again.'
  } finally {
    saving.value = false
  }
}


const progressPercentage = computed(() => {
  return (currentStep.value / totalSteps) * 100
})

// Dynamic typing tagline
const taglineMessages = [
  'Your own Socratic mentor. Built to elevate your mind.',
  'Upload anything. Get quizzes, summaries, and clarity in seconds.',
  'Learn deeper and faster through intelligent, targeted questioning.',
  'Master presentations, interviews, and exams with real-time AI coaching.'
]

const displayedText = ref('')
const currentMessageIndex = ref(0)
const isDeleting = ref(false)
let typingTimeout: number | null = null

function typeText() {
  const currentMessage = taglineMessages[currentMessageIndex.value]
  
  if (!isDeleting.value) {
    // Typing forward
    if (displayedText.value.length < currentMessage.length) {
      displayedText.value = currentMessage.substring(0, displayedText.value.length + 1)
      typingTimeout = window.setTimeout(typeText, 50) // Typing speed
    } else {
      // Finished typing, wait a bit then start deleting
      typingTimeout = window.setTimeout(() => {
        isDeleting.value = true
        typeText()
      }, 800) // Brief pause to read the message before deleting
    }
  } else {
    // Deleting
    if (displayedText.value.length > 0) {
      displayedText.value = displayedText.value.substring(0, displayedText.value.length - 1)
      typingTimeout = window.setTimeout(typeText, 15) // Deleting speed (faster)
    } else {
      // Finished deleting, move to next message
      isDeleting.value = false
      currentMessageIndex.value = (currentMessageIndex.value + 1) % taglineMessages.length
      typingTimeout = window.setTimeout(typeText, 500) // Brief pause before next message
    }
  }
}

function onDocumentClick() {
  closeDropdown()
}

onMounted(() => {
  typeText()
  document.addEventListener('click', onDocumentClick, true)
})

onBeforeUnmount(() => {
  if (typingTimeout) {
    clearTimeout(typingTimeout)
  }
  document.removeEventListener('click', onDocumentClick, true)
})
</script>

<template>
  <div class="signup-page">
    <div class="signup-container">
      <!-- Left Section: Image with Personalize Your Workspace -->
      <div class="left-section">
        <!-- Personalize Your Workspace Image -->
        <img
          :src="personalizeImage"
          alt="Personalize Your Workspace"
          class="socrates-background-image"
        />
        <div class="left-content">
          <!-- Dynamic Typing Tagline -->
          <div class="dynamic-tagline-container">
            <p class="dynamic-tagline">
              <span class="tagline-text">{{ displayedText }}</span>
              <span class="typing-cursor">|</span>
            </p>
          </div>
          
          <!-- Steps Cards -->
          <div class="steps-section">
            <div class="step-card" :class="{ active: progressStep === 1 }">
              <div class="step-number">1</div>
              <div class="step-text">
                <span>Sign up</span>
              </div>
            </div>
            <div class="step-card" :class="{ active: progressStep === 2 }">
              <div class="step-number">2</div>
              <div class="step-text">
                <span>Tell us more</span>
                <span>about yourself</span>
              </div>
            </div>
            <div class="step-card" :class="{ active: progressStep === 3 }">
              <div class="step-number">3</div>
              <div class="step-text">
                <span>Personalise your</span>
                <span>workspace</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Section: Form or Get started -->
      <div class="right-section">
        <div class="form-container">
          <!-- Step 3: All done – same left image, right shows Get started CTA -->
          <template v-if="progressStep === 3">
            <div class="get-started-screen">
              <div class="form-header form-header-center">
                <div class="logo-container">
                  <img :src="logo" alt="Socratica logo" class="logo" />
                  <span class="brand-name">Socratica</span>
                </div>
              </div>
              <h2 class="form-title get-started-title">Let Socratica adapt to the way you think and learn.</h2>
              <div class="get-started-wrap">
                <router-link to="/post-auth" class="btn-get-started">
                  <span class="btn-get-started-text">Get started with Socratica</span>
                  <span class="btn-get-started-arrow" aria-hidden="true">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </span>
                </router-link>
              </div>
            </div>
          </template>

          <!-- Steps 1–2: Form -->
          <template v-else>
          <!-- Header -->
          <div class="form-header">
          <div class="logo-container">
            <img :src="logo" alt="Socratica logo" class="logo" />
            <span class="brand-name">Socratica</span>
          </div>
            <div class="step-indicator">
              <span class="step-text">Step {{ currentStep }} of {{ totalSteps }}</span>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
              </div>
            </div>
          </div>

          <h2 class="form-title">Tell us more about yourself</h2>
          <p class="form-subtitle">Personalize your details for a more tailored experience.</p>

          <!-- Navigation Tabs -->
          <div class="tabs-container">
            <button
              type="button"
              @click="goToStep(1)"
              class="tab"
              :class="{ active: currentStep === 1 }"
            >
              Personal Details
            </button>
            <button
              type="button"
              @click="goToStep(2)"
              class="tab"
              :class="{ active: currentStep === 2 }"
            >
              Academic Background
            </button>
            </div>

          <form @submit.prevent="handleSaveAndContinue" class="personalize-form">
            <!-- Step 1: Personal Details -->
            <div v-if="currentStep === 1" class="step-content">
              <div class="form-field">
                <label for="preferredName" class="form-label">Preferred Name</label>
                <div class="input-shine">
                  <input
                    id="preferredName"
                    v-model="personalDetailsData.preferredName"
                    type="text"
                    placeholder="e.g. Plato"
                    class="form-input"
                  />
                </div>
              </div>

              <div class="form-field">
                <label for="learningPace" class="form-label">How fast do you prefer to learn?</label>
                <div class="custom-dropdown" @click.stop>
                  <button
                    id="learningPace"
                    type="button"
                    class="custom-dropdown-trigger"
                    :class="{ open: openDropdown === 'learningPace' }"
                    @click="toggleDropdown('learningPace')"
                  >
                    <span class="custom-dropdown-label" :class="{ placeholder: !personalDetailsData.currentRole }">{{ learningPaceOptions.find(o => o.value === personalDetailsData.currentRole)?.label || 'Select pace' }}</span>
                    <span class="custom-dropdown-chevron"></span>
                  </button>
                  <div v-if="openDropdown === 'learningPace'" class="custom-dropdown-list">
                    <button
                      v-for="opt in learningPaceOptions"
                      :key="opt.value"
                      type="button"
                      class="custom-dropdown-option"
                      :class="{ selected: personalDetailsData.currentRole === opt.value }"
                      @click="personalDetailsData.currentRole = opt.value; closeDropdown()"
                    >{{ opt.label }}</button>
                  </div>
                </div>
              </div>

              <div class="form-field">
                <label for="focusSessionLength" class="form-label">How long are your usual focus sessions?</label>
                <div class="custom-dropdown" @click.stop>
                  <button
                    id="focusSessionLength"
                    type="button"
                    class="custom-dropdown-trigger"
                    :class="{ open: openDropdown === 'focusSession' }"
                    @click="toggleDropdown('focusSession')"
                  >
                    <span class="custom-dropdown-label" :class="{ placeholder: !personalDetailsData.countryTimeZone }">{{ focusSessionOptions.find(o => o.value === personalDetailsData.countryTimeZone)?.label || 'Select duration' }}</span>
                    <span class="custom-dropdown-chevron"></span>
                  </button>
                  <div v-if="openDropdown === 'focusSession'" class="custom-dropdown-list">
                    <button
                      v-for="opt in focusSessionOptions"
                      :key="opt.value"
                      type="button"
                      class="custom-dropdown-option"
                      :class="{ selected: personalDetailsData.countryTimeZone === opt.value }"
                      @click="personalDetailsData.countryTimeZone = opt.value; closeDropdown()"
                    >{{ opt.label }}</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Step 2: Academic Background -->
            <div v-if="currentStep === 2" class="step-content">
              <div class="form-field">
                <label for="fieldOfStudy" class="form-label">Field of Study / Industry</label>
                <div class="input-shine">
                  <input
                    id="fieldOfStudy"
                    v-model="academicBackgroundData.fieldOfStudy"
                    type="text"
                    placeholder="e.g. Computer Science"
                    class="form-input"
                  />
                </div>
              </div>

              <div class="form-field">
                <label for="academicCurrentRole" class="form-label">Current Role</label>
                <div class="input-shine">
                  <input
                    id="academicCurrentRole"
                    v-model="academicBackgroundData.currentRole"
                    type="text"
                    placeholder="e.g. Student, Software Engineer"
                    class="form-input"
                  />
                </div>
              </div>

              <div class="form-field">
                <label for="experienceLevel" class="form-label">Experience Level</label>
                <div class="custom-dropdown" @click.stop>
                  <button
                    id="experienceLevel"
                    type="button"
                    class="custom-dropdown-trigger"
                    :class="{ open: openDropdown === 'experience' }"
                    @click="toggleDropdown('experience')"
                  >
                    <span class="custom-dropdown-label" :class="{ placeholder: !academicBackgroundData.experienceLevel }">{{ experienceLevelOptions.find(o => o.value === academicBackgroundData.experienceLevel)?.label || 'Select level' }}</span>
                    <span class="custom-dropdown-chevron"></span>
                  </button>
                  <div v-if="openDropdown === 'experience'" class="custom-dropdown-list">
                    <button
                      v-for="opt in experienceLevelOptions"
                      :key="opt.value"
                      type="button"
                      class="custom-dropdown-option"
                      :class="{ selected: academicBackgroundData.experienceLevel === opt.value }"
                      @click="academicBackgroundData.experienceLevel = opt.value; closeDropdown()"
                    >{{ opt.label }}</button>
                  </div>
                </div>
              </div>

              <div class="form-field">
                <label for="backgroundDescription" class="form-label">Short Background Description</label>
                <div class="input-shine">
                  <textarea
                    id="backgroundDescription"
                    v-model="academicBackgroundData.backgroundDescription"
                    placeholder="Tell us about yourself..."
                    class="form-input"
                    rows="4"
                  ></textarea>
                </div>
              </div>
              </div>


            <!-- Error Message -->
            <div v-if="saveError" class="error-message">
              {{ saveError }}
            </div>

            <!-- Navigation Buttons -->
            <div class="form-actions">
              <button
                v-if="currentStep > 1"
                type="button"
                @click="prevStep"
                class="btn-secondary"
                :disabled="saving"
              >
                Previous
              </button>
              <button
                type="submit"
                class="btn-primary"
                :disabled="saving"
              >
                {{ saving ? 'Saving...' : 'Save & Continue' }}
              </button>
            </div>
          </form>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.signup-page {
  height: 100vh;
  width: 100vw;
  background: #08090a;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  overflow: hidden;
  box-sizing: border-box;
  position: absolute;
  top: 0;
  left: 0;
}

.signup-container {
  display: flex;
  width: 100%;
  max-width: 95%;
  height: calc(100vh - 4rem);
  min-height: calc(100vh - 4rem);
  max-height: calc(100vh - 4rem);
  background: #08090a;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.06);
  margin: 0;
  position: relative;
}

/* Logo Container */
.logo-container {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 2rem;
  margin-top: 1rem;
}

.logo {
  width: 48px;
  height: 48px;
  object-fit: contain;
  opacity: 0.85;
}

.brand-name {
  font-family: "Times New Roman", "Times", serif;
  font-size: 1rem;
  color: #f9f9fb;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  opacity: 0.85;
}

/* Left Section */
.left-section {
  flex: 0 0 50%;
  width: 50%;
  height: 100%;
  background: #000000;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.75rem 1.5rem;
  overflow: hidden;
  border-radius: 12px 0 0 12px;
  z-index: 2;
  will-change: transform;
  box-sizing: border-box;
}

.left-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: transparent;
  z-index: 0;
}

.socrates-background-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  z-index: 1;
  opacity: 1;
  pointer-events: none;
}


.left-content {
  position: relative;
  z-index: 2;
  width: 100%;
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: space-between;
}

.socrates-image-container {
  position: relative;
  margin-bottom: 1rem;
  display: flex;
  justify-content: center;
  align-items: center;
}

.socrates-image {
  width: 90px;
  height: 90px;
  object-fit: contain;
  filter: drop-shadow(0 10px 30px rgba(201, 169, 97, 0.3));
  position: relative;
  z-index: 2;
}

.futuristic-background {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 140px;
  height: 140px;
  background: radial-gradient(circle, rgba(201, 169, 97, 0.2) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse 3s ease-in-out infinite;
  z-index: 1;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.5;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 0.8;
    transform: translate(-50%, -50%) scale(1.1);
  }
}

.welcome-text {
  color: #ffffff;
}

.welcome-title {
  font-family: "Times New Roman", "Times", serif;
  font-size: 1.5rem;
  font-weight: 500;
  color: #f9f9fb;
  margin-bottom: 0.5rem;
  line-height: 1.2;
  letter-spacing: 0.28em;
  text-transform: uppercase;
}

.welcome-subtitle {
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.85rem;
  font-weight: 400;
  color: rgba(249, 249, 251, 0.82);
  margin-bottom: 0.375rem;
  line-height: 1.4;
  letter-spacing: 0.08em;
}

.welcome-tagline {
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.8rem;
  font-weight: 500;
  color: rgba(201, 169, 97, 0.9);
  margin-top: 0.5rem;
  letter-spacing: 0.08em;
}

.dynamic-tagline-container {
  width: 100%;
  margin: 2rem 0 1.5rem 0;
  padding: 0;
  background: transparent;
  border: none;
  border-radius: 0;
  backdrop-filter: none;
  box-shadow: none;
}

.dynamic-tagline {
  min-height: 2em;
  display: block;
  text-align: center;
  max-width: 100%;
  margin: 0;
  font-family: "Times New Roman", "Times", serif;
  font-size: 1.25rem;
  font-weight: 500;
  letter-spacing: 0.05em;
  line-height: 1.6;
}

.tagline-text {
  display: inline;
  color: #ffffff;
  font-family: "Times New Roman", "Times", serif;
  font-size: 1.25rem;
  font-weight: 500;
  letter-spacing: 0.05em;
  animation: fade-in 0.5s ease-in;
}

.typing-cursor {
  display: inline-block;
  margin-left: 3px;
  color: #ffffff;
  animation: blink 1s infinite;
  font-weight: 300;
  font-size: 1.25rem;
  vertical-align: middle;
  line-height: 1.6;
  transform: translateY(-3px);
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

@keyframes gradient-shift {
  0% {
    background-position: 0% center;
  }
  50% {
    background-position: 100% center;
  }
  100% {
    background-position: 0% center;
  }
}

@keyframes fade-in {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

/* Steps Section – extra top margin so "Sign up" (step 1) aligns with right-side Step 1 content */
.steps-section {
  display: flex;
  gap: 0.5rem;
  margin-top: 8.5rem;
  width: 100%;
}

.step-card {
  flex: 1 1 0;
  background: linear-gradient(135deg, rgba(201, 169, 97, 0.08) 0%, rgba(201, 169, 97, 0.15) 100%);
  border: 1px solid rgba(201, 169, 97, 0.3);
  border-radius: 12px;
  padding: 1rem 0.75rem;
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 0;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.step-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(201, 169, 97, 0.2), transparent);
  transition: left 0.6s ease;
}

.step-card:hover::before {
  left: 100%;
}

.step-card:hover {
  border-color: rgba(201, 169, 97, 0.5);
  box-shadow: 0 6px 20px rgba(201, 169, 97, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.step-card.active {
  background: #ffffff;
  border: 1px solid rgba(201, 169, 97, 0.4);
  box-shadow: none;
  transform: translateY(-1px);
}

.step-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.75rem;
  font-weight: 700;
  flex-shrink: 0;
  transition: all 0.3s ease;
  position: relative;
}

.step-number::after {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 50%;
  padding: 2px;
  background: linear-gradient(45deg, rgba(201, 169, 97, 0.5), rgba(201, 169, 97, 0.2));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.step-card:hover .step-number::after {
  opacity: 1;
}

.step-card.active .step-number {
  background: linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%);
  color: #C9A961;
  box-shadow: 0 0 12px rgba(201, 169, 97, 0.5), inset 0 1px 2px rgba(255, 255, 255, 0.2);
  font-weight: 600;
  text-shadow: 0 0 8px rgba(201, 169, 97, 0.4);
}

.step-card:not(.active) .step-number {
  background: linear-gradient(135deg, rgba(201, 169, 97, 0.3) 0%, rgba(201, 169, 97, 0.2) 100%);
  color: rgba(201, 169, 97, 1);
  box-shadow: 0 2px 8px rgba(201, 169, 97, 0.2);
}

/* Step card labels (left side) – keep same size as signup form fields */
.step-card .step-text {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.875rem;
  font-weight: 500;
  line-height: 1.3;
}

.step-card.active .step-text {
  color: #1a1a1a;
}

.step-card:not(.active) .step-text {
  color: rgba(255, 255, 255, 0.9);
}

/* Right Section */
.right-section {
  flex: 0 0 50%;
  width: 50%;
  height: 100%;
  background: #08090a;
  border-left: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 1.75rem 1.5rem;
  overflow-y: auto;
  overflow-x: hidden;
  border-radius: 0 12px 12px 0;
  z-index: 2;
  will-change: transform;
  box-sizing: border-box;
}

.right-section::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.right-section {
  scrollbar-width: none;
}


.form-container {
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.form-title {
  font-family: "Times New Roman", "Times", serif;
  font-size: 2rem;
  font-weight: 600;
  background: linear-gradient(135deg, #D4AF37 0%, #C9A961 50%, #B8860B 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 0.5rem;
  filter: drop-shadow(0 1px 1px rgba(0, 0, 0, 0.2));
  line-height: 1.2;
  letter-spacing: 0.05em;
  filter: drop-shadow(0 1px 1px rgba(0, 0, 0, 0.2));
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.form-header .logo-container {
  margin-bottom: 0;
  margin-top: 0;
}

.form-header-center {
  justify-content: center;
}

.get-started-screen {
  animation: get-started-fade-in 0.5s ease-out;
}

@keyframes get-started-fade-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.get-started-title {
  margin-top: 0.5rem;
}

.get-started-subtitle {
  margin-bottom: 0.5rem;
}

.get-started-wrap {
  margin-top: 2.5rem;
  display: flex;
  justify-content: center;
}

.btn-get-started {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 0.9rem 2rem;
  text-decoration: none;
  min-width: 240px;
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.95rem;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #C9A961;
  background: linear-gradient(135deg, rgba(201, 169, 97, 0.1) 0%, rgba(201, 169, 97, 0.18) 100%);
  border: 1px solid rgba(201, 169, 97, 0.4);
  border-radius: 14px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25),
              inset 0 1px 0 rgba(255, 255, 255, 0.1),
              0 0 24px rgba(201, 169, 97, 0.15);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-get-started::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(201, 169, 97, 0.25), transparent);
  transition: left 0.6s ease;
}

.btn-get-started:hover {
  border-color: rgba(201, 169, 97, 0.65);
  color: #f0e6d0;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.35),
              inset 0 1px 0 rgba(255, 255, 255, 0.15),
              0 0 32px rgba(201, 169, 97, 0.35);
  transform: translateY(-3px);
}

.btn-get-started:hover::before {
  left: 100%;
}

.btn-get-started:hover .btn-get-started-arrow {
  transform: translateX(4px);
}

.btn-get-started-text {
  position: relative;
  z-index: 1;
}

.btn-get-started-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-get-started-arrow svg {
  flex-shrink: 0;
}

.step-indicator {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.5rem;
}

/* "Step 1 of 2" label in form header only – do not affect left-side step cards */
.step-indicator .step-text {
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.75rem;
  background: linear-gradient(135deg, #D4AF37 0%, #C9A961 50%, #B8860B 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  opacity: 0.9;
  letter-spacing: 0.08em;
  filter: drop-shadow(0 1px 1px rgba(0, 0, 0, 0.2));
}

.progress-bar {
  width: 120px;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #C9A961;
  transition: width 0.3s ease;
}

.form-subtitle {
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.875rem;
  color: #f9f9fb;
  margin-bottom: 1.5rem;
  line-height: 1.5;
}

.select-input:invalid {
  color: rgba(255, 255, 255, 0.7);
}

.tabs-container {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  justify-content: center;
}

.tab {
  font-family: "Times New Roman", "Times", serif;
  background: none;
  border: none;
  padding: 0.75rem 1rem;
  color: #f9f9fb;
  font-size: 0.875rem;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
}

.tab:hover {
  color: rgba(255, 255, 255, 0.9);
}

.tab.active {
  background: linear-gradient(135deg, #D4AF37 0%, #C9A961 50%, #B8860B 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  border-bottom-color: #C9A961;
  filter: drop-shadow(0 1px 1px rgba(0, 0, 0, 0.2));
}

.tab-icon {
  font-size: 1rem;
}

.personalize-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.step-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.section-header {
  margin-top: 1rem;
  margin-bottom: 0.5rem;
}

.section-title {
  font-family: "Times New Roman", "Times", serif;
  font-size: 1rem;
  font-weight: 500;
  background: linear-gradient(135deg, #D4AF37 0%, #C9A961 50%, #B8860B 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 1px 1px rgba(0, 0, 0, 0.2));
  letter-spacing: 0.08em;
  margin: 0;
}

.radio-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.9);
  cursor: pointer;
}

.radio-input {
  width: 18px;
  height: 18px;
  accent-color: #C9A961;
  cursor: pointer;
}

.form-input select,
.form-input textarea {
  font-family: "Times New Roman", "Times", serif;
}

.form-input textarea {
  resize: vertical;
  min-height: 100px;
}

.form-input textarea::-webkit-resizer,
.form-input::-webkit-resizer,
textarea::-webkit-resizer {
  display: none;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 0.75rem;
  margin-bottom: 0;
  justify-content: flex-end;
  width: 100%;
  flex-wrap: wrap;
}

.btn-primary,
.btn-secondary,
.btn-skip {
  font-family: "Times New Roman", "Times", serif;
  padding: 0.75rem 2rem;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.22s ease;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, rgba(201, 169, 97, 0.08) 0%, rgba(201, 169, 97, 0.15) 100%);
  border: 1px solid rgba(201, 169, 97, 0.3);
  color: #C9A961;
  min-width: 160px;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(201, 169, 97, 0.2), transparent);
  transition: left 0.6s ease;
}

.btn-primary:hover::before {
  left: 100%;
}

.btn-primary:hover {
  border-color: rgba(201, 169, 97, 0.5);
  box-shadow: none;
  transform: translateY(0);
}

.btn-secondary {
  background: linear-gradient(135deg, rgba(201, 169, 97, 0.08) 0%, rgba(201, 169, 97, 0.15) 100%);
  border: 1px solid rgba(201, 169, 97, 0.3);
  color: #C9A961;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-secondary::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(201, 169, 97, 0.2), transparent);
  transition: left 0.6s ease;
}

.btn-secondary:hover::before {
  left: 100%;
}

.btn-secondary:hover {
  border-color: rgba(201, 169, 97, 0.5);
  box-shadow: none;
  transform: translateY(0);
}

.btn-skip {
  background: transparent;
  color: rgba(255, 255, 255, 0.6);
  text-decoration: underline;
  text-transform: none;
  padding: 0.75rem 1rem;
}

.btn-skip:hover {
  color: rgba(255, 255, 255, 0.9);
}

.signup-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  position: relative;
}
.input-shine {
  position: relative;
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.08);
  background: linear-gradient(135deg, rgba(201, 169, 97, 0.06) 0%, rgba(201, 169, 97, 0.1) 100%);
  background-color: rgba(12, 12, 12, 0.85);
}

.input-shine::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  pointer-events: none;
  transition: border-color 0.2s ease;
  z-index: 3;
}
.input-shine .form-input {
  border-color: transparent;
  box-shadow: none;
  background: transparent;
}


.input-shine::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 12px;
  background: linear-gradient(90deg, transparent, rgba(201, 169, 97, 0.2), transparent);
  transform: translateX(-120%);
  transition: transform 0.6s ease;
  pointer-events: none;
  z-index: 2;
}
.input-shine:hover::after,
.input-shine:focus-within::after {
  border-color: rgba(201, 169, 97, 0.65);
}


.password-fields-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.form-label {
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.8rem;
  font-weight: 500;
  color: #ffffff;
  margin-bottom: 0.125rem;
  letter-spacing: 0.08em;
}

.form-input {
  font-family: "Times New Roman", "Times", serif;
  width: 100%;
  padding: 0.5625rem 0.875rem;
  background: linear-gradient(135deg, rgba(201, 169, 97, 0.06) 0%, rgba(201, 169, 97, 0.1) 100%);
  background-color: rgba(12, 12, 12, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #f9f9fb;
  font-size: 0.875rem;
  font-weight: 400;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-sizing: border-box;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.08);
  position: relative;
  z-index: 1;
}

.form-input.select-input {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  padding-right: 2.5rem;
  border-radius: 12px;
  accent-color: #404040;
  background-image:
    linear-gradient(45deg, transparent 50%, rgba(201, 169, 97, 0.9) 50%),
    linear-gradient(135deg, rgba(201, 169, 97, 0.9) 50%, transparent 50%),
    linear-gradient(to right, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.08));
  background-position:
    calc(100% - 18px) 50%,
    calc(100% - 12px) 50%,
    calc(100% - 36px) 50%;
  background-size: 6px 6px, 6px 6px, 1px 20px;
  background-repeat: no-repeat;
}

.form-input.select-input option {
  background: #1a1a1a;
  color: #f9f9fb;
  padding: 0.5rem 0.75rem;
}

.form-input.select-input option:hover {
  background: #404040;
  color: #ffffff;
}

.form-input.select-input option:checked,
.form-input.select-input option:focus {
  background: #333333;
  color: #ffffff;
}

/* Custom dropdown – gray hover, rounded list corners (replaces native select) */
.custom-dropdown {
  position: relative;
  width: 100%;
}

.custom-dropdown-trigger {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
  padding: 0.5625rem 0.875rem;
  padding-right: 2.5rem;
  background: linear-gradient(135deg, rgba(201, 169, 97, 0.06) 0%, rgba(201, 169, 97, 0.1) 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #f9f9fb;
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.875rem;
  font-weight: 400;
  text-align: left;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.custom-dropdown-trigger::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(201, 169, 97, 0.2), transparent);
  transition: left 0.6s ease;
}

.custom-dropdown-trigger:hover::before,
.custom-dropdown-trigger.open::before {
  left: 100%;
}

.custom-dropdown-trigger:hover {
  border-color: rgba(201, 169, 97, 0.5);
  background: rgba(255, 255, 255, 0.08);
  box-shadow: none;
  transform: translateY(0);
}

.custom-dropdown-trigger.open {
  border-color: rgba(201, 169, 97, 0.65);
  background: linear-gradient(135deg, rgba(201, 169, 97, 0.06) 0%, rgba(201, 169, 97, 0.1) 100%);
  box-shadow: none;
  transform: translateY(0);
}

.custom-dropdown-label {
  flex: 1;
  min-width: 0;
  color: inherit;
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.875rem;
  font-weight: 400;
}

.custom-dropdown-trigger:not(.open) .custom-dropdown-label:empty::before,
.custom-dropdown-trigger .custom-dropdown-label {
  color: #f9f9fb;
}

.custom-dropdown-label.placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.custom-dropdown-chevron {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  width: 0;
  height: 0;
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-top: 6px solid rgba(201, 169, 97, 0.9);
  flex-shrink: 0;
}

.custom-dropdown-trigger.open .custom-dropdown-chevron {
  transform: translateY(-50%) rotate(180deg);
}

.custom-dropdown-list {
  position: absolute;
  left: 0;
  right: 0;
  top: calc(100% + 4px);
  z-index: 50;
  background: #1a1a1a;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  overflow: hidden;
  max-height: 220px;
  overflow-y: auto;
}

.custom-dropdown-option {
  display: block;
  width: 100%;
  padding: 0.625rem 0.875rem;
  border: none;
  background: transparent;
  color: #f9f9fb;
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.875rem;
  font-weight: 400;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s ease;
}

.custom-dropdown-option:hover {
  background: #404040;
  color: #ffffff;
}

.custom-dropdown-option.selected {
  background: #333333;
  color: #ffffff;
}

.select-wrapper {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
}

.select-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(201, 169, 97, 0.2), transparent);
  transition: left 0.6s ease;
  pointer-events: none;
}

.select-wrapper::after {
  content: '';
  position: absolute;
  inset: 0;
  border: 1px solid rgba(201, 169, 97, 0.3);
  border-radius: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.select-wrapper:hover::before {
  left: 100%;
}

.select-wrapper:hover::after {
  opacity: 1;
}

.select-wrapper:focus-within::after {
  opacity: 1;
}

.select-wrapper .select-input {
  background-color: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
}

.form-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

::selection {
  background: rgba(201, 169, 97, 0.35);
  color: #f9f9fb;
}

.form-input::selection,
textarea::selection {
  background: rgba(201, 169, 97, 0.35);
  color: #f9f9fb;
}

.form-field:hover .input-shine::before {
  transform: translateX(120%);
}

.form-field:hover .form-input {
  border-color: transparent;
  box-shadow: none;
  transform: translateY(0);
}

.form-input:focus {
  outline: none;
  border-color: transparent;
  box-shadow: none;
}

.form-input.input-error {
  border-color: #ef4444;
}

.form-input:-webkit-autofill,
.form-input:-webkit-autofill:hover,
.form-input:-webkit-autofill:focus {
  -webkit-text-fill-color: #f9f9fb;
  transition: background-color 5000s ease-in-out 0s;
  box-shadow: 0 0 0 1000px rgba(15, 15, 15, 0.9) inset, 0 4px 12px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.1);
}

.password-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-input {
  padding-right: 2.25rem;
}

.password-toggle {
  position: absolute;
  right: 0.625rem;
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.25rem;
  transition: color 0.3s ease;
}

.password-toggle svg {
  width: 16px;
  height: 16px;
}

.password-toggle:hover {
  color: #C9A961;
}

.password-hint {
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.625rem;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 0.125rem;
  line-height: 1.3;
}

.error-text {
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.7rem;
  font-weight: 400;
  color: #ef4444;
  margin-top: 0.125rem;
}

.error-message {
  font-family: "Times New Roman", "Times", serif;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid #ef4444;
  color: #ef4444;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  font-size: 0.75rem;
  margin-bottom: 0.75rem;
}

.social-section {
  display: flex;
  gap: 0.5rem;
  margin: 0 0 0.75rem 0;
}

.separator {
  position: relative;
  text-align: center;
  margin: 0.5rem 0;
}

.separator::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: rgba(255, 255, 255, 0.2);
}

.separator-text {
  font-family: "Times New Roman", "Times", serif;
  position: relative;
  background: #08090a;
  padding: 0 1rem;
  color: rgba(249, 249, 251, 0.6);
  font-size: 0.8125rem;
  font-weight: 500;
}

.social-btn {
  font-family: "Times New Roman", "Times", serif;
  flex: 1;
  padding: 0.5625rem 0.75rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: rgba(249, 249, 251, 0.82);
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  letter-spacing: 0.08em;
}

.social-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.social-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(201, 169, 97, 0.65);
  color: #ffffff;
}

.terms-section {
  margin: 0.125rem 0;
}

.checkbox-label {
  font-family: "Times New Roman", "Times", serif;
  display: flex;
  align-items: flex-start;
  gap: 0.625rem;
  color: rgba(255, 255, 255, 0.85);
  font-size: 0.75rem;
  font-weight: 400;
  cursor: pointer;
  line-height: 1.4;
}

.checkbox-input {
  width: 0.9375rem;
  height: 0.9375rem;
  margin-top: 0.125rem;
  cursor: pointer;
  accent-color: rgba(201, 169, 97, 0.9);
  flex-shrink: 0;
}

.terms-link {
  color: rgba(201, 169, 97, 0.9);
  text-decoration: underline;
  transition: color 0.3s ease;
}

.terms-link:hover {
  color: rgba(201, 169, 97, 1);
}

.signup-btn {
  font-family: "Times New Roman", "Times", serif;
  width: 100%;
  padding: 0.72rem 1.6rem;
  background: #f9f9fb;
  color: #08090a;
  border: none;
  border-radius: 999px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.22s ease;
  margin-top: 0;
  margin-bottom: 0;
  position: relative;
  overflow: hidden;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.signup-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

.signup-btn:hover:not(:disabled)::before {
  left: 100%;
}

.signup-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.4);
}

.signup-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
}

.signup-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.login-link {
  font-family: "Times New Roman", "Times", serif;
  text-align: center;
  color: rgba(249, 249, 251, 0.75);
  font-size: 0.75rem;
  font-weight: 400;
  margin-top: -0.25rem;
}

.login-link-text {
  color: rgba(201, 169, 97, 0.9);
  text-decoration: underline;
  font-weight: 500;
  transition: all 0.22s ease;
  position: relative;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  display: inline-block;
}

.login-link-text::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 4px;
  background: linear-gradient(135deg, rgba(201, 169, 97, 0.2) 0%, rgba(201, 169, 97, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.login-link-text::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(201, 169, 97, 0.9), transparent);
  transform: translateX(-50%);
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 8px rgba(201, 169, 97, 0.6);
}

.login-link-text:hover {
  color: rgba(201, 169, 97, 1);
}

.login-link-text:hover::before {
  opacity: 1;
}

.login-link-text:hover::after {
  width: 100%;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .signup-page {
    padding: 1.5rem;
  }

  .signup-container {
    flex-direction: column;
    border-radius: 12px;
  }

  .left-section {
    flex: 0 0 auto;
    min-height: 40vh;
    padding: 2.5rem 2rem;
  }

  .right-section {
    flex: 0 0 auto;
    padding: 2.5rem 2rem;
  }

  .welcome-title {
    font-size: 2rem;
  }

  .socrates-image {
    width: 150px;
    height: 150px;
  }
}

@media (max-width: 768px) {
  .left-section {
    min-height: 30vh;
    padding: 1.5rem;
  }

  .right-section {
    padding: 1.5rem;
  }

  .form-title {
    font-size: 1.5rem;
  }

  .welcome-title {
    font-size: 1.75rem;
  }

  .welcome-subtitle {
    font-size: 1rem;
  }

  .social-section {
    flex-direction: column;
  }
}
</style>
