<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authService } from '../services/authService'
import logo from '@/assets/logo.png'

const router = useRouter()
const email = ref('')
const error = ref('')
const success = ref('')
const loading = ref(false)

async function handleSubmit() {
  if (!email.value.trim()) {
    error.value = 'Email is required'
    return
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
    error.value = 'Please enter a valid email'
    return
  }

  error.value = ''
  success.value = ''
  loading.value = true

  try {
    await authService.forgotPassword({ email: email.value })
    success.value = 'If an account with that email exists, a password reset link has been sent.'
  } catch (err: any) {
    error.value = err.response?.data?.message || 'An error occurred. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="forgot-password-page">
    <div class="forgot-password-container">
      <div class="form-section">
        <div class="form-container">
          <!-- Logo -->
          <div class="logo-container">
            <img :src="logo" alt="Socratica logo" class="logo" />
            <span class="brand-name">Socratica</span>
          </div>

          <h2 class="form-title">Reset Your Password</h2>
          <p class="form-subtitle">Enter your email address and we'll send you a link to reset your password.</p>

          <!-- Error Message -->
          <div v-if="error" class="error-message">
            {{ error }}
          </div>

          <!-- Success Message -->
          <div v-if="success" class="success-message">
            {{ success }}
          </div>

          <form @submit.prevent="handleSubmit" class="forgot-password-form">
            <!-- Email -->
            <div class="form-field">
              <label for="email" class="form-label">Email address *</label>
              <input
                id="email"
                v-model="email"
                type="email"
                placeholder="eg. andrew@example.com"
                class="form-input"
                :class="{ 'input-error': error }"
                :disabled="loading"
              />
            </div>

            <!-- Submit Button -->
            <button
              type="submit"
              :disabled="loading"
              class="submit-btn"
            >
              {{ loading ? 'Sending...' : 'Send Reset Link' }}
            </button>

            <!-- Back to Login Link -->
            <p class="back-link">
              Remember your password? <router-link to="/login" class="back-link-text">Back to login</router-link>
            </p>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.forgot-password-page {
  min-height: 100vh;
  width: 100vw;
  background: #08090a;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
}

.forgot-password-container {
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

.forgot-password-form {
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
