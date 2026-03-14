import { createRouter, createWebHistory } from 'vue-router'
import LandingPageView from '../views/LandingPageView.vue'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'landing',
      component: LandingPageView
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView
    },
    {
      path: '/signup',
      name: 'signup',
      component: () => import('../views/SignUpView.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue')
    },
    {
      path: '/personalize',
      name: 'personalize',
      component: () => import('../views/PersonalizeWorkspaceView.vue')
    },
    {
      path: '/auth/callback',
      name: 'oauth-callback',
      component: () => import('../views/OAuthCallbackView.vue')
    },
    {
      path: '/post-auth',
      name: 'post-auth',
      component: () => import('../views/PostAuthView.vue')
    },
    {
      path: '/forgot-password',
      name: 'forgot-password',
      component: () => import('../views/ForgotPasswordView.vue')
    },
    {
      path: '/reset-password',
      name: 'reset-password',
      component: () => import('../views/ResetPasswordView.vue')
    },
    {
      path: '/live-voice',
      name: 'live-voice',
      component: () => import('../views/GeminiLiveVoiceView.vue')
    },
    {
      path: '/document-review',
      name: 'document-review',
      component: () => import('../views/DocumentReviewView.vue')
    },
    {
      path: '/privacy-policy',
      name: 'privacy-policy',
      component: () => import('../views/PrivacyPolicyView.vue')
    },
    {
      path: '/cookie-policy',
      name: 'cookie-policy',
      component: () => import('../views/CookiePolicyView.vue')
    },
    {
      path: '/terms-of-service',
      name: 'terms-of-service',
      component: () => import('../views/TermsOfServiceView.vue')
    }
  ]
})

export default router

