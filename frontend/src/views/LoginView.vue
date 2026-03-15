<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import { authService } from '../services/authService'
import logo from '@/assets/logo.png'
import socrateAndPeople from '@/assets/socrateandpeople.png'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const showPassword = ref(true)

const formData = ref({
  email: '',
  password: '',
})

const errors = ref({
  email: '',
  password: '',
})

function validateForm(): boolean {
  let isValid = true
  errors.value = { email: '', password: '' }

  if (!formData.value.email.trim()) {
    errors.value.email = 'Email is required'
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.value.email)) {
    errors.value.email = 'Please enter a valid email'
    isValid = false
  }

  if (!formData.value.password) {
    errors.value.password = 'Password is required'
    isValid = false
  }

  return isValid
}

async function handleSubmit() {
  if (!validateForm()) {
    return
  }

  try {
    await authStore.login({
      email: formData.value.email,
      password: formData.value.password,
    })
    const redirectPath = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    if (redirectPath) {
      router.push(redirectPath)
    } else {
      router.push('/build-your-socrate')
    }
  } catch (error) {
    // Error is handled by the store
  }
}

async function handleSocialLogin(provider: string) {
  try {
    let authUrl: string
    if (provider === 'google') {
      authUrl = await authService.getGoogleAuthUrl()
    } else if (provider === 'microsoft') {
      authUrl = await authService.getMicrosoftAuthUrl()
    } else {
      console.error('Unknown provider:', provider)
      return
    }
    
    // Store that this is a login action
    sessionStorage.setItem('oauth_source', 'login')
    
    // Redirect to OAuth provider
    window.location.href = authUrl
  } catch (error: any) {
    console.error('Error initiating OAuth:', error)
    authStore.error = error.response?.data?.message || 'Failed to initiate social login. Please try again.'
  }
}

function togglePasswordVisibility() {
  showPassword.value = !showPassword.value
}
</script>

<template>
  <div class="login-page">
    <div class="login-container">
      <!-- Left Section: Form -->
      <div class="left-section form-section">
        <div class="form-container">
          <!-- Logo -->
          <div class="logo-container">
            <img :src="logo" alt="Socratica logo" class="logo" />
            <span class="brand-name">Socratica</span>
          </div>
          <h2 class="form-title">Log in to Socratica</h2>

          <!-- Error Message -->
          <div v-if="authStore?.error" class="error-message">
            {{ authStore.error }}
          </div>

          <form @submit.prevent="handleSubmit" class="login-form">
            <!-- Social Login Buttons -->
            <div class="social-section">
              <button
                type="button"
                @click="handleSocialLogin('google')"
                class="social-btn"
              >
                <svg class="social-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                  <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                  <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                  <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                </svg>
                Google
              </button>
              <button
                type="button"
                @click="handleSocialLogin('microsoft')"
                class="social-btn"
              >
                <svg class="social-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 23 23" fill="currentColor">
                  <path fill="#f25022" d="M0 0h11v11H0z"/>
                  <path fill="#00a4ef" d="M12 0h11v11H12z"/>
                  <path fill="#7fba00" d="M0 12h11v11H0z"/>
                  <path fill="#ffb900" d="M12 12h11v11H12z"/>
                </svg>
                Microsoft
              </button>
              <button
                type="button"
                @click="handleSocialLogin('university')"
                class="social-btn"
              >
                University Email
              </button>
            </div>

            <!-- Separator -->
            <div class="separator">
              <span class="separator-text">Or</span>
            </div>

            <!-- Email -->
            <div class="form-field">
              <label for="email" class="form-label">Email address *</label>
              <div class="input-shine">
                <input
                  id="email"
                  v-model="formData.email"
                  type="email"
                  placeholder="eg. andrew@example.com"
                  class="form-input"
                  :class="{ 'input-error': errors.email }"
                />
              </div>
              <p v-if="errors.email" class="error-text">{{ errors.email }}</p>
            </div>

            <!-- Password -->
            <div class="form-field">
              <label for="password" class="form-label">Password</label>
              <div class="password-input-wrapper input-shine">
                <input
                  id="password"
                  v-model="formData.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="Enter your password"
                  class="form-input password-input"
                  :class="{ 'input-error': errors.password }"
                />
                <button
                  type="button"
                  @click="togglePasswordVisibility"
                  class="password-toggle"
                  tabindex="-1"
                >
                  <svg v-if="showPassword" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                  </svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                </button>
              </div>
              <p v-if="errors.password" class="error-text">{{ errors.password }}</p>
            </div>

            <!-- Login Button -->
            <button
              type="submit"
              :disabled="authStore?.loading"
              class="login-btn"
            >
              {{ authStore?.loading ? 'Logging in...' : 'Log in' }}
            </button>

            <!-- Forgot Password Link -->
            <p class="forgot-password-link">
              <router-link to="/forgot-password" class="forgot-password-link-text">Forgot your password?</router-link>
            </p>

            <!-- Sign Up Link -->
            <p class="signup-link">
              Don't have an account? <router-link to="/signup" class="signup-link-text">Sign up</router-link>
            </p>
          </form>
        </div>
      </div>

      <!-- Right Section: Welcome -->
      <div class="right-section welcome-section">
        <!-- Socrates and People Image Background -->
        <img :src="socrateAndPeople" alt="Socrates and People" class="socrates-background-image" />
        <div class="left-content">
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
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

.login-container {
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
  border: 1px solid rgba(255, 255, 255, 0.2);
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

/* Left Section (Form on Login page) */
.left-section.form-section {
  flex: 0 0 50%;
  width: 50%;
  height: 100%;
  background: #08090a;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 1.75rem 1.5rem;
  overflow: hidden;
  border-radius: 12px 0 0 12px;
  z-index: 2;
  will-change: transform;
  box-sizing: border-box;
}


.left-content {
  position: relative;
  z-index: 2;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
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
  filter: drop-shadow(0 10px 30px rgba(212, 175, 55, 0.3));
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
  background: radial-gradient(circle, rgba(212, 175, 55, 0.2) 0%, transparent 70%);
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
  color: rgba(212, 175, 55, 0.9);
  margin-top: 0.5rem;
  letter-spacing: 0.08em;
}

/* Steps Section */
.steps-section {
  display: flex;
  gap: 0.5rem;
  margin-top: 2rem;
  width: 100%;
}

.step-card {
  flex: 1 1 0;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.08) 0%, rgba(212, 175, 55, 0.15) 100%);
  border: 1px solid rgba(212, 175, 55, 0.3);
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
  background: linear-gradient(90deg, transparent, rgba(212, 175, 55, 0.2), transparent);
  transition: left 0.6s ease;
}

.step-card:hover::before {
  left: 100%;
}

.step-card:hover {
  border-color: rgba(212, 175, 55, 0.5);
  box-shadow: 0 6px 20px rgba(212, 175, 55, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.step-card.active {
  background: linear-gradient(135deg, #ffffff 0%, #f8f8f8 100%);
  border: 1px solid rgba(212, 175, 55, 0.4);
  box-shadow: 0 8px 24px rgba(212, 175, 55, 0.4), 0 0 20px rgba(212, 175, 55, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transform: translateY(-1px);
}

.step-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
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
  background: linear-gradient(45deg, rgba(212, 175, 55, 0.5), rgba(212, 175, 55, 0.2));
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
  color: #D4AF37;
  box-shadow: 0 0 12px rgba(212, 175, 55, 0.5), inset 0 1px 2px rgba(255, 255, 255, 0.2);
}

.step-card:not(.active) .step-number {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.3) 0%, rgba(212, 175, 55, 0.2) 100%);
  color: #D4AF37;
  box-shadow: 0 2px 8px rgba(212, 175, 55, 0.2);
}

.step-text {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 0.8125rem;
  font-weight: 500;
  line-height: 1.3;
}

.step-card.active .step-text {
  color: #1a1a1a;
}

.step-card:not(.active) .step-text {
  color: rgba(255, 255, 255, 0.9);
}

/* Right Section (Welcome on Login page) */
.right-section.welcome-section {
  flex: 0 0 50%;
  width: 50%;
  height: 100%;
  background: #000000;
  border-left: 1px solid rgba(255, 255, 255, 0.06);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.75rem 1.5rem;
  overflow: hidden;
  border-radius: 0 12px 12px 0;
  z-index: 2;
  will-change: transform;
  box-sizing: border-box;
}

.right-section.welcome-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: transparent;
  z-index: 1;
}

.socrates-background-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  z-index: 0;
  opacity: 1;
  pointer-events: none;
}


.form-container {
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
  padding-top: 0.75rem;
}

.form-title {
  font-family: "Times New Roman", "Times", serif;
  font-size: 1.375rem;
  font-weight: 500;
  color: #f9f9fb;
  margin-bottom: 1.25rem;
  line-height: 1.2;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.login-form {
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

.form-label {
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.8rem;
  font-weight: 500;
  color: rgba(249, 249, 251, 0.82);
  margin-bottom: 0.125rem;
  letter-spacing: 0.08em;
}

.form-input {
  font-family: "Times New Roman", "Times", serif;
  width: 100%;
  padding: 0.5625rem 0.875rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: #f9f9fb;
  font-size: 0.875rem;
  font-weight: 400;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.form-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

::selection {
  background: rgba(201, 169, 97, 0.35);
  color: #f9f9fb;
}

.form-input::selection,
.form-input::placeholder::selection,
.password-input::selection,
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
  z-index: 3;
}

.password-toggle svg {
  width: 16px;
  height: 16px;
}

.password-toggle:hover {
  color: #D4AF37;
}

.error-text {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  font-size: 0.7rem;
  font-weight: 400;
  color: #ef4444;
  margin-top: 0.125rem;
}

.error-message {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
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
  border-color: rgba(212, 175, 55, 0.65);
  color: #ffffff;
}

.login-btn {
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
  margin-top: 0.125rem;
  position: relative;
  overflow: hidden;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

.login-btn:hover:not(:disabled)::before {
  left: 100%;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.4);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.signup-link {
  font-family: "Times New Roman", "Times", serif;
  text-align: center;
  color: rgba(249, 249, 251, 0.75);
  font-size: 0.75rem;
  font-weight: 400;
  margin-top: 0.5rem;
}

.signup-link-text {
  color: rgba(212, 175, 55, 0.9);
  text-decoration: underline;
  font-weight: 500;
  transition: all 0.22s ease;
  position: relative;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  display: inline-block;
}

.signup-link-text::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 4px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.2) 0%, rgba(212, 175, 55, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.signup-link-text::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(212, 175, 55, 0.8), transparent);
  transform: translateX(-50%);
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 8px rgba(212, 175, 55, 0.6);
}

.signup-link-text:hover {
  color: rgba(212, 175, 55, 1);
}

.signup-link-text:hover::before {
  opacity: 1;
}

.signup-link-text:hover::after {
  width: 100%;
}

.forgot-password-link {
  font-family: "Times New Roman", "Times", serif;
  text-align: center;
  color: rgba(249, 249, 251, 0.75);
  font-size: 0.75rem;
  font-weight: 400;
  margin-top: 0.5rem;
}

.forgot-password-link-text {
  color: rgba(212, 175, 55, 0.9);
  text-decoration: underline;
  font-weight: 500;
  transition: all 0.22s ease;
  position: relative;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  display: inline-block;
}

.forgot-password-link-text::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 4px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.2) 0%, rgba(212, 175, 55, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.forgot-password-link-text::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(212, 175, 55, 0.8), transparent);
  transform: translateX(-50%);
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 8px rgba(212, 175, 55, 0.6);
}

.forgot-password-link-text:hover {
  color: rgba(212, 175, 55, 1);
}

.forgot-password-link-text:hover::before {
  opacity: 1;
}

.forgot-password-link-text:hover::after {
  width: 100%;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .login-page {
    padding: 1.5rem;
  }

  .login-container {
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
    border-radius: 0 0 12px 12px;
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
