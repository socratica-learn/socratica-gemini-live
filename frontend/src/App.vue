<script setup lang="ts">
import { RouterView, useRoute } from 'vue-router'
import { computed, ref, watch } from 'vue'

const route = useRoute()
const transitionDirection = ref<'left' | 'right'>('right')
const previousRoute = ref<string | null>(null)

watch(() => route.name, (to, from) => {
  previousRoute.value = from as string
  
  // Determine direction based on route order
  if (from === 'signup' && to === 'login') {
    transitionDirection.value = 'right' // Sign Up → Login: slide right
  } else if (from === 'login' && to === 'signup') {
    transitionDirection.value = 'left' // Login → Sign Up: slide left
  } else if (from === 'personalize' && to === 'build-your-socrate') {
    transitionDirection.value = 'right' // Personalize → Post-auth: new page from right, old leaves left
  }
})

const transitionName = computed(() => {
  const name = route.name as string
  if (name === 'signup' || name === 'login') {
    return transitionDirection.value === 'right' ? 'slide-right' : 'slide-left'
  }
  if (name === 'build-your-socrate' && previousRoute.value === 'personalize') {
    return 'slide-right' // Cool right-to-left: build-your-socrate enters from right, personalize leaves to left
  }
  return 'fade'
})
</script>

<template>
  <div id="app" class="app-container">
    <RouterView v-slot="{ Component }">
      <transition :name="transitionName">
        <component :is="Component" :key="route.path" />
      </transition>
    </RouterView>
  </div>
</template>

<style>
#app {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu,
    Cantarell, 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100vh;
  position: relative;
}

.app-container {
  width: 100%;
  min-height: 100vh;
  position: relative;
  margin: 0;
  padding: 0;
  background: transparent;
  overflow: hidden;
}

/* Cool slide transitions with visual effects */
.slide-right-enter-active,
.slide-right-leave-active,
.slide-left-enter-active,
.slide-left-leave-active {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  transition: transform 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94), 
              opacity 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94),
              filter 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.slide-right-enter-active,
.slide-left-enter-active {
  z-index: 2;
}

.slide-right-leave-active,
.slide-left-leave-active {
  z-index: 1;
}

/* Slide Right (Sign Up → Login): New page enters from right, old exits to left */
.slide-right-enter-from {
  transform: translateX(100%) scale(0.95);
  opacity: 0;
  filter: blur(10px);
}

.slide-right-leave-to {
  transform: translateX(-100%) scale(0.95);
  opacity: 0.3;
  filter: blur(8px);
}

.slide-right-enter-to {
  transform: translateX(0) scale(1);
  opacity: 1;
  filter: blur(0);
}

.slide-right-leave-from {
  transform: translateX(0) scale(1);
  opacity: 1;
  filter: blur(0);
}

/* Slide Left (Login → Sign Up): New page enters from left, old exits to right */
.slide-left-enter-from {
  transform: translateX(-100%) scale(0.95);
  opacity: 0;
  filter: blur(10px);
}

.slide-left-leave-to {
  transform: translateX(100%) scale(0.95);
  opacity: 0.3;
  filter: blur(8px);
}

.slide-left-enter-to {
  transform: translateX(0) scale(1);
  opacity: 1;
  filter: blur(0);
}

.slide-left-leave-from {
  transform: translateX(0) scale(1);
  opacity: 1;
  filter: blur(0);
}

/* Section swap animation within the page */
.slide-right-enter-active .signup-page .left-section,
.slide-right-enter-active .signup-page .right-section,
.slide-right-enter-active .login-page .left-section,
.slide-right-enter-active .login-page .right-section,
.slide-left-enter-active .signup-page .left-section,
.slide-left-enter-active .signup-page .right-section,
.slide-left-enter-active .login-page .left-section,
.slide-left-enter-active .login-page .right-section {
  transition: transform 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

/* Sign Up → Login: Sections swap */
.slide-right-leave-active .signup-page .left-section {
  transform: translateX(100%);
}

.slide-right-leave-active .signup-page .right-section {
  transform: translateX(-100%);
}

.slide-right-enter-from .login-page .left-section.form-section {
  transform: translateX(-100%);
}

.slide-right-enter-from .login-page .right-section.welcome-section {
  transform: translateX(100%);
}

/* Login → Sign Up: Sections swap */
.slide-left-leave-active .login-page .left-section.form-section {
  transform: translateX(100%);
}

.slide-left-leave-active .login-page .right-section.welcome-section {
  transform: translateX(-100%);
}

.slide-left-enter-from .signup-page .left-section {
  transform: translateX(-100%);
}

.slide-left-enter-from .signup-page .right-section {
  transform: translateX(100%);
}

/* Final state */
.slide-right-enter-to .left-section,
.slide-right-enter-to .right-section,
.slide-left-enter-to .left-section,
.slide-left-enter-to .right-section {
  transform: translateX(0);
}

/* Fade transition for other pages */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

</style>

