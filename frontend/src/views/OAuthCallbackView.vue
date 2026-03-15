<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

onMounted(() => {
  const token = route.query.token as string
  const provider = route.query.provider as string
  const userId = route.query.userId as string
  const email = route.query.email as string
  const name = route.query.name as string
  const surname = route.query.surname as string

  if (token) {
    // Store the token and user info
    authStore.setAuth(token, {
      id: userId || '',
      email: email || '',
      name: name || '',
      surname: surname || ''
    })
    
    // Check if this was from signup or login
    const oauthSource = sessionStorage.getItem('oauth_source')
    sessionStorage.removeItem('oauth_source') // Clean up
    
    // Redirect based on source: signup goes to personalize, login goes to build-your-socrate
    if (oauthSource === 'signup') {
      router.push('/personalize')
    } else {
      router.push('/build-your-socrate')
    }
  } else {
    // No token received, redirect to signup
    router.push('/signup')
  }
})
</script>

<template>
  <div class="oauth-callback">
    <div class="loading-container">
      <div class="spinner"></div>
      <p>Completing authentication...</p>
    </div>
  </div>
</template>

<style scoped>
.oauth-callback {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #08090a;
}

.loading-container {
  text-align: center;
  color: #f9f9fb;
}

.spinner {
  border: 4px solid rgba(201, 169, 97, 0.1);
  border-top: 4px solid #C9A961;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

p {
  font-family: "Times New Roman", "Times", serif;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
}
</style>
