<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authService } from '../services/authService'
import logo from '@/assets/logo.png'

const router = useRouter()
const route = useRoute()
const token = ref('')
const password = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const error = ref('')
const success = ref('')
const loading = ref(false)

onMounted(() => {
  const tokenParam = route.query.token as string
  if (tokenParam) {
    token.value = tokenParam
  } else {
    error.value = 'Invalid reset token. Please request a new password reset.'
  }
})

function validateForm(): boolean {
  error.value = ''

  if (!password.value) {
    error.value = 'Password is required'
    return false
  }

  if (password.value.length < 8) {
    error.value = 'Password must be at least 8 characters long'
    return false
  }

  if (password.value !== confirmPassword.value) {
    error.value = 'Passwords do not match'
    return false
  }

  return true
}

async function handleSubmit() {
  if (!validateForm()) {
    return
  }

  if (!token.value) {
    error.value = 'Invalid reset token'
    return
  }

  loading.value = true
  error.value = ''
  success.value = ''

  try {
    await authService.resetPassword({
      token: token.value,
      password: password.value,
      confirmPassword: confirmPassword.value
    })
    success.value = 'Password has been reset successfully! Redirecting to login...'
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to reset password. Please try again.'
  } finally {
    loading.value = false
  }
}

function togglePasswordVisibility() {
  showPassword.value = !showPassword.value
}

function toggleConfirmPasswordVisibility() {
  showConfirmPassword.value = !showConfirmPassword.value
}
</script>

<template>
  <div class="reset-password-page">
    <div class="reset-password-container">
      <div class="form-section">
        <div class="form-container">
          <!-- Logo -->
          <div class="logo-container">
            <img :src="logo" alt="Socratica logo" class="logo" />
            <span class="brand-name">Socratica</span>
          </div>

          <h2 class="form-title">Reset Your Password</h2>
          <p class="form-subtitle">Enter your new password below.</p>

          <!-- Error Message -->
          <div v-if="error" class="error-message">
            {{ error }}
          </div>

          <!-- Success Message -->
          <div v-if="success" class="success-message">
            {{ success }}
          </div>

          <form @submit.prevent="handleSubmit" class="reset-password-form">
            <!-- Password -->
            <div class="form-field">
              <label for="password" class="form-label">New Password *</label>
              <div class="password-input-wrapper">
                <input
                  id="password"
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="Enter your new password"
                  class="form-input password-input"
                  :class="{ 'input-error': error }"
                  :disabled="loading"
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
            </div>

            <!-- Confirm Password -->
            <div class="form-field">
              <label for="confirmPassword" class="form-label">Confirm New Password *</label>
              <div class="password-input-wrapper">
                <input
                  id="confirmPassword"
                  v-model="confirmPassword"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  placeholder="Confirm your new password"
                  class="form-input password-input"
                  :class="{ 'input-error': error }"
                  :disabled="loading"
                />
                <button
                  type="button"
                  @click="toggleConfirmPasswordVisibility"
                  class="password-toggle"
                  tabindex="-1"
                >
                  <svg v-if="showConfirmPassword" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                  </svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                </button>
              </div>
            </div>

            <!-- Submit Button -->
            <button
              type="submit"
              :disabled="loading || !token"
              class="submit-btn"
            >
              {{ loading ? 'Resetting...' : 'Reset Password' }}
            </button>

            <!-- Back to Login Link -->
            <p class="back-link">
              <router-link to="/login" class="back-link-text">Back to login</router-link>
            </p>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.reset-password-page {
  min-height: 100vh;
  width: 100vw;
  background: #08090a;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
}

.reset-password-container {
  width: 100%;
  max-width: 480px;
}

.form-section {
  background: #08090a;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 2rem;
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

.form-title {
  font-family: "Times New Roman", "Times", serif;
  font-size: 1.375rem;
  font-weight: 500;
  color: #f9f9fb;
  margin-bottom: 0.5rem;
  line-height: 1.2;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.form-subtitle {
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.875rem;
  color: rgba(249, 249, 251, 0.7);
  margin-bottom: 1.5rem;
  line-height: 1.5;
}

.error-message {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid #ef4444;
  color: #ef4444;
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.success-message {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: rgba(34, 197, 94, 0.1);
  border: 1px solid #22c55e;
  color: #22c55e;
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.reset-password-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.form-label {
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.8rem;
  font-weight: 500;
  color: rgba(249, 249, 251, 0.82);
  margin-bottom: 0.125rem;
  letter-spacing: 0.08em;
}

.password-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
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

.password-input {
  padding-right: 2.25rem;
}

.form-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.form-input:focus {
  outline: none;
  border-color: rgba(212, 175, 55, 0.65);
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 0 0 2px rgba(212, 175, 55, 0.15);
}

.form-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-input.input-error {
  border-color: #ef4444;
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
  color: #D4AF37;
}

.submit-btn {
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
  margin-top: 0.5rem;
  position: relative;
  overflow: hidden;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

.submit-btn:hover:not(:disabled)::before {
  left: 100%;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.back-link {
  font-family: "Times New Roman", "Times", serif;
  text-align: center;
  color: rgba(249, 249, 251, 0.75);
  font-size: 0.75rem;
  font-weight: 400;
  margin-top: 1rem;
}

.back-link-text {
  color: rgba(212, 175, 55, 0.9);
  text-decoration: underline;
  font-weight: 500;
  transition: all 0.22s ease;
}

.back-link-text:hover {
  color: rgba(212, 175, 55, 1);
}
</style>
