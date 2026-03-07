<template>
  <section class="landing-page">
    <nav class="navbar">
      <div class="navbar-inner">
        <div class="brand">
          <img class="nav-logo" :src="logo" alt="Socratica wordmark" />
          <span class="brand-name">Socratica</span>
        </div>
        <ul class="nav-links">
          <li><a href="#about">About Us</a></li>
          <li><a href="#discover">Discover More</a></li>
          <li><a href="#build">Build your own Socrate</a></li>
          <li><a href="#contact">Contact</a></li>
        </ul>
        <div class="nav-actions">
          <button
            class="profile-button"
            type="button"
            aria-label="Go to login"
            @click="goToLogin"
          >
            <svg
              class="profile-icon"
              viewBox="0 0 24 24"
              role="img"
              aria-hidden="true"
            >
              <circle cx="12" cy="8.5" r="3.5" />
              <path
                d="M5.5 18.5c0-3.05 2.46-5.5 5.5-5.5h2c3.04 0 5.5 2.45 5.5 5.5"
              />
            </svg>
          </button>
        </div>
      </div>
    </nav>
    <div class="hero">
      <video class="hero-video" autoplay muted loop playsinline>
        <source :src="videoSrc" type="video/mp4" />
      </video>
      <div class="video-overlay"></div>
      <div class="hero-content">
          <div class="hero-copy">
          <p class="pre-title">In search of elevating your way of learning?</p>
            <h1 class="title">Socratica</h1>
          <div class="title-divider"></div>
            <div class="cta-stack">
            <p class="subtitle">Evaluate your <span class="highlight-neon">understanding</span>, prepare for <span class="highlight-neon">presentations</span>, ace&nbsp;<span class="highlight-neon">interviews</span>,<br>and transform how you learn&nbsp;- all through intelligent conversation.</p>
              <div class="cta-group">
                <a href="#get-started" class="cta primary">Get Started</a>
              <a ref="secondaryButton" href="#discover" class="cta secondary" @mouseenter="handleButtonEnter" @mousemove="handleButtonMove" @mouseleave="handleButtonLeave">See How It Works <span class="arrow">→</span></a>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- About Us Section -->
    <section id="about" class="content-section">
      <div class="about-section-wrapper">
        <div class="about-image-container">
          <img :src="goldenSocrate" alt="Golden Socrates bust" class="about-image" />
        </div>
        <div class="section-content">
          <h2 class="section-title">About Us</h2>
          <div class="title-divider"></div>
          <p class="section-subheading">"I cannot teach anybody anything. I can only make them think" ― Socrates</p>
          
          <div class="about-content">
            <div class="about-paragraph">
              <p class="about-text">
                Socratica was born from a simple yet powerful realization: true learning happens not through passive absorption, 
                but through active dialogue and questioning. In an age where information is abundant but understanding is scarce, 
                we sought to create a platform that transforms how people engage with knowledge.
              </p>
              <p class="about-text">
                Our mission is to revive the ancient art of Socratic inquiry in a modern, accessible format that empowers learners to think critically, ask better questions, 
                and discover insights through conversation.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Discover More Section -->
    <section id="discover" ref="discoverSection" class="content-section discover-section">
      <div class="carousel-container">
        <div 
          class="carousel-wrapper"
          @touchstart="handleTouchStart"
          @touchmove="handleTouchMove"
          @touchend="handleTouchEnd"
          @mousedown="handleMouseDown"
          @mousemove="handleMouseMove"
          @mouseup="handleMouseUp"
          @mouseleave="handleMouseUp"
        >
          <div 
            class="carousel-track" 
            :style="{ transform: `translateX(-${currentSlide * 100}%)` }"
          >
            <div 
              v-for="(feature, index) in features" 
              :key="index"
              class="carousel-slide"
            >
              <div 
                :ref="el => { if (el) cardRefs[index] = el as HTMLElement }"
                class="feature-card-large scroll-animated-card"
                :class="{ 'has-background': index === 0 || index === 1 || index === 2 || index === 3 || index === 4, 'text-top-right': index === 0 || index === 3 || index === 4, 'text-bottom-left': index === 1, 'has-video-background': index === 1, 'card-job-prep': index === 3, 'card-written-eval': index === 0, 'card-notes-ipad': index === 4 }"
                :style="index === 0 ? { backgroundImage: `url(${writtenEvalImage})`, backgroundSize: 'cover', backgroundPosition: 'center' } : index === 2 ? { backgroundImage: `url(${brainGold})` } : index === 1 ? {} : index === 3 ? { backgroundImage: `url(${jobImage})`, backgroundSize: 'cover', backgroundPosition: 'bottom center' } : index === 4 ? { backgroundImage: `url(${ipadImage})`, backgroundSize: 'cover', backgroundPosition: 'center' } : {}"
              >
                <video 
                  v-if="index === 1"
                  class="card-video-background"
                  autoplay
                  muted
                  loop
                  playsinline
                >
                  <source :src="presentationVideo" type="video/mp4" />
                </video>
                <h3 
                  class="feature-title title-section-font gliding-text"
                  :class="{ 
                    'glide-in': cardScrollProgress[index] !== undefined && cardScrollProgress[index] > 0.2,
                    'glide-from-right': index % 2 === 0 || index === 3,
                    'glide-from-left': index % 2 === 1 && index !== 3,
                    'title-golden': index === 0
                  }"
                  :style="{ 
                    '--progress': cardScrollProgress[index] || 0
                  }"
                >{{ feature.title }}</h3>
                <p 
                  class="feature-description gliding-text"
                  :class="{ 
                    'glide-in': cardScrollProgress[index] !== undefined && cardScrollProgress[index] > 0.2,
                    'glide-from-right': index % 2 === 0 && index !== 3,
                    'glide-from-left': index % 2 === 1 || index === 3
                  }"
                  :style="{ 
                    '--progress': cardScrollProgress[index] || 0
                  }"
                >{{ feature.description }}</p>
              </div>
            </div>
          </div>
        </div>
        <div class="carousel-arrows">
          <button
            type="button"
            class="carousel-arrow left"
            @click="prevSlide"
            aria-label="Previous slide"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 6l-6 6 6 6"/>
            </svg>
          </button>
          <button
            type="button"
            class="carousel-arrow right"
            @click="nextSlide"
            aria-label="Next slide"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 6l6 6-6 6"/>
            </svg>
          </button>
        </div>
      </div>
    </section>

    <!-- Build your own Socrate Section -->
    <section id="build" class="content-section">
      <div class="section-content">
        <h2 class="section-title">Build your own Socrate</h2>
        <p class="section-text">
          Customize your learning experience by creating a personalized AI avatar that matches your learning style, 
          preferences, and goals. Your Socrate adapts to you, making every conversation more effective and engaging.
        </p>
      </div>
    </section>

    <!-- Contact Section -->
    <section id="contact" class="content-section">
      <div class="section-content">
        <h2 class="section-title">Contact</h2>
        <p class="section-text">
          Have questions or want to learn more? We'd love to hear from you.
        </p>
        <div class="contact-info">
          <p class="contact-text">Get in touch with our team to discover how Socratica can transform your learning journey.</p>
        </div>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import logo from '@/assets/logo.png'
import videoSrc from '@/assets/Socio-technical-3.mp4'
import goldenBack from '@/assets/golden-back.png'
import golden2 from '@/assets/golden2.png'
import goldenSocrate from '@/assets/socrate-golden-back.png'
import thinkingSocrate from '@/assets/thinking socrate.png'
import socrateCombined from '@/assets/socrate combined.png'
import socrateCombined2 from '@/assets/socrate combined 2.png'
import mixture from '@/assets/mixture.png'
import semiTranspBack from '@/assets/semi-transp-back.png'
import brainGold from '@/assets/brain-gold.png'
import presentationImage from '@/assets/image copy.png'
import micImage from '@/assets/mic.png'
import writtenEvalImage from '@/assets/David_-_The_Death_of_Socrates 2.jpg'
import presentationVideo from '@/assets/women presenting.mp4'
import jobImage from '@/assets/job2-img.png'
import ipadImage from '@/assets/ipad-img.png'

const secondaryButton = ref<HTMLElement | null>(null)
const discoverSection = ref<HTMLElement | null>(null)
let handleScroll: (() => void) | null = null
let handleWheel: ((event: WheelEvent) => void) | null = null
let lastWheelTime = 0
const isCarouselLocked = ref(false)
let carouselObserver: IntersectionObserver | null = null
let lockedScrollY = 0
let isRepositioningScroll = false
const activationTolerance = 16
let lastScrollY = 0
let activationScrollY: number | null = null
let lastViewportHeight = window.innerHeight
const router = useRouter()

// Carousel state
const currentSlide = ref(0)
const isAutoPlaying = ref(false)
let autoPlayInterval: number | null = null
let touchStartX = 0
let touchEndX = 0
let isDragging = false
let dragStartX = 0

// Scroll-based animations
const cardRefs = ref<(HTMLElement | null)[]>([])
const cardScrollProgress = ref<number[]>([])
let scrollAnimationFrame: number | null = null

const features = [
  {
    title: 'Written Evaluation',
    description: 'Upload your files and let AI create customized quizzes, exams, or tests. Adjust questions through conversation or add your own. Get instant, detailed feedback.'
  },
  {
    title: 'Presentation Prep',
    description: 'Analyze tone, intonation, slide quality, tempo, engagement, word choice, and body language. Receive accessibility-focused recommendations tailored to your audience.'
  },
  {
    title: 'Socratic Evaluation',
    description: 'Engage in one-on-one conversations with a tailored avatar. The AI can interrupt to ask for clarifications or dive deeper into concepts. Review passed questions to identify areas for improvement.'
  },
  {
    title: 'Job Preparation',
    description: 'Practice behavioral and job interviews with your avatar. Cover letter analysis with AI-powered suggestions.'
  },
  {
    title: 'Notes & Summaries',
    description: 'Upload files and let AI generate structured notes and summaries. Transform unstructured information into organized, digestible content.'
  },
  {
    title: 'Teaching Advice',
    description: 'Receive content and explanation advice based on your specific audience. Optimize your teaching approach for maximum engagement and comprehension.'
  }
]

const goToSlide = (index: number) => {
  currentSlide.value = index
}

const nextSlide = () => {
  currentSlide.value = (currentSlide.value + 1) % features.length
}

const prevSlide = () => {
  currentSlide.value = (currentSlide.value - 1 + features.length) % features.length
}

const toggleAutoPlay = () => {
  isAutoPlaying.value = !isAutoPlaying.value
  if (isAutoPlaying.value) {
    autoPlayInterval = window.setInterval(() => {
      nextSlide()
    }, 5000)
  } else {
    if (autoPlayInterval) {
      clearInterval(autoPlayInterval)
      autoPlayInterval = null
    }
  }
}

const goToLogin = () => {
  router.push('/login')
}

// Touch handlers
const handleTouchStart = (e: TouchEvent) => {
  touchStartX = e.touches[0].clientX
}

const handleTouchMove = (e: TouchEvent) => {
  touchEndX = e.touches[0].clientX
}

const handleTouchEnd = () => {
  if (!touchStartX || !touchEndX) return
  const distance = touchStartX - touchEndX
  const minSwipeDistance = 50
  
  if (Math.abs(distance) > minSwipeDistance) {
    if (distance > 0) {
      nextSlide()
    } else {
      prevSlide()
    }
  }
  
  touchStartX = 0
  touchEndX = 0
}

// Mouse handlers for desktop drag
const handleMouseDown = (e: MouseEvent) => {
  isDragging = true
  dragStartX = e.clientX
}

const handleMouseMove = (e: MouseEvent) => {
  if (!isDragging) return
  e.preventDefault()
}

const handleMouseUp = (e: MouseEvent) => {
  if (!isDragging) return
  isDragging = false
  
  const distance = dragStartX - e.clientX
  const minSwipeDistance = 50
  
  if (Math.abs(distance) > minSwipeDistance) {
    if (distance > 0) {
      nextSlide()
    } else {
      prevSlide()
    }
  }
  
  dragStartX = 0
}

// Apple-style scroll-based animations
const setupScrollAnimations = () => {
  const updateScrollProgress = () => {
    if (scrollAnimationFrame) {
      cancelAnimationFrame(scrollAnimationFrame)
    }

    scrollAnimationFrame = requestAnimationFrame(() => {
      const viewportHeight = window.innerHeight
      const viewportCenter = viewportHeight / 2

      cardRefs.value.forEach((card, index) => {
        if (!card) return

        // Only animate the currently visible slide
        if (index !== currentSlide.value) {
          cardScrollProgress.value[index] = 0
          return
        }

        const cardRect = card.getBoundingClientRect()
        const cardTop = cardRect.top
        const cardHeight = cardRect.height
        const cardCenter = cardTop + cardHeight / 2

        // Calculate distance from viewport center
        const distanceFromCenter = Math.abs(cardCenter - viewportCenter)
        const maxDistance = viewportHeight / 2 + cardHeight / 2

        // Calculate progress (0 to 1) - more progress as card center approaches viewport center
        let progress = 1 - (distanceFromCenter / maxDistance)
        progress = Math.max(0, Math.min(1, progress))

        // Apply easing for smoother animation
        progress = easeInOutCubic(progress)

        cardScrollProgress.value[index] = progress
      })

      scrollAnimationFrame = null
    })
  }

  // Easing function for smooth animation
  const easeInOutCubic = (t: number): number => {
    return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2
  }

  // Watch for carousel slide changes
  watch(currentSlide, () => {
    // Reset progress for all cards
    cardScrollProgress.value = new Array(features.length).fill(0)
    // Update progress for new slide
    setTimeout(updateScrollProgress, 100)
  })

  window.addEventListener('scroll', updateScrollProgress, { passive: true })
  window.addEventListener('resize', updateScrollProgress, { passive: true })
  
  // Initial call
  updateScrollProgress()
}

const handleButtonEnter = () => {
  // Button is being hovered, the CSS will handle the animation
}

const handleButtonMove = (event: MouseEvent) => {
  if (!secondaryButton.value) return
  const rect = secondaryButton.value.getBoundingClientRect()
  const x = ((event.clientX - rect.left) / rect.width) * 100
  const y = ((event.clientY - rect.top) / rect.height) * 100
  secondaryButton.value.style.setProperty('--mouse-x', `${x}%`)
  secondaryButton.value.style.setProperty('--mouse-y', `${y}%`)
}

const handleButtonLeave = () => {
  // Reset or maintain state when leaving
}

const updateActivationScrollY = () => {
  const firstCard = cardRefs.value[0]
  if (!firstCard) {
    activationScrollY = null
    return
  }

  const rect = firstCard.getBoundingClientRect()
  const fitsViewport =
    rect.width <= window.innerWidth &&
    rect.height <= window.innerHeight

  if (!fitsViewport) {
    activationScrollY = null
    return
  }

  const cardCenterOffset = rect.top + rect.height / 2
  const targetScrollY = window.scrollY + cardCenterOffset - window.innerHeight / 2
  activationScrollY = Math.max(0, Math.round(targetScrollY))
}

onMounted(() => {
  // Initialize scroll progress array
  cardScrollProgress.value = new Array(features.length).fill(0)
  
  // Setup scroll-based animations
  setTimeout(() => {
    setupScrollAnimations()
  }, 200)

  // Small delay to ensure DOM is ready
  setTimeout(() => {
    updateActivationScrollY()
    const cards = document.querySelectorAll('.about-card')
    let lastScrollTop = window.pageYOffset || document.documentElement.scrollTop
    let isScrollingDown = true
    let animationTimeout: number | null = null
    
    // Function to reset cards
    const resetCards = () => {
      cards.forEach((card) => {
        card.classList.remove('visible')
      })
    }
    
    // Function to animate cards when scrolling down
    const animateCards = () => {
      if (animationTimeout) {
        clearTimeout(animationTimeout)
      }
      resetCards()
      animationTimeout = window.setTimeout(() => {
        cards.forEach((card, index) => {
          const rect = card.getBoundingClientRect()
          if (rect.top < window.innerHeight && rect.bottom > 0) {
            setTimeout(() => {
              card.classList.add('visible')
            }, index * 150)
          }
        })
      }, 50)
    }
    
    // Scroll direction detection
    handleScroll = () => {
      if (window.innerHeight !== lastViewportHeight) {
        lastViewportHeight = window.innerHeight
        updateActivationScrollY()
      }

      if (isCarouselLocked.value && !isRepositioningScroll) {
        isRepositioningScroll = true
        requestAnimationFrame(() => {
          window.scrollTo(0, lockedScrollY)
          isRepositioningScroll = false
        })
        return
      }
      const currentScrollTop = window.pageYOffset || document.documentElement.scrollTop
      const scrollingDownThisFrame = currentScrollTop > lastScrollY
      const hasActivationPoint = activationScrollY !== null
      const crossedActivation =
        hasActivationPoint &&
        scrollingDownThisFrame &&
        lastScrollY < (activationScrollY as number) &&
        currentScrollTop >= (activationScrollY as number)
      lastScrollY = currentScrollTop

      if (!isCarouselLocked.value && currentSlide.value === 0 && crossedActivation) {
        isCarouselLocked.value = true
        lockedScrollY = activationScrollY as number
        isRepositioningScroll = true
        requestAnimationFrame(() => {
          window.scrollTo({ top: lockedScrollY, behavior: 'auto' })
          isRepositioningScroll = false
        })
        return
      }

      const aboutSection = document.getElementById('about')
      
      if (!aboutSection) return
      
      const sectionTop = aboutSection.offsetTop
      const sectionBottom = sectionTop + aboutSection.offsetHeight
      const isInSection = currentScrollTop >= sectionTop - window.innerHeight && 
                          currentScrollTop <= sectionBottom
      
      if (!isInSection) return
      
      // Detect scroll direction
      if (currentScrollTop > lastScrollTop) {
        // Scrolling down
        if (!isScrollingDown) {
          animateCards()
        }
        isScrollingDown = true
      } else if (currentScrollTop < lastScrollTop) {
        // Scrolling up
        if (isScrollingDown) {
          resetCards()
        }
        isScrollingDown = false
      }
      
      lastScrollTop = currentScrollTop <= 0 ? 0 : currentScrollTop
    }
    
    // Intersection Observer for initial animation
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting && isScrollingDown) {
            const card = entry.target as HTMLElement
            const index = Array.from(cards).indexOf(card)
            setTimeout(() => {
              card.classList.add('visible')
            }, index * 150)
          }
        })
      },
      {
        threshold: 0.1,
        rootMargin: '0px 0px -100px 0px'
      }
    )
    
    cards.forEach((card) => {
      observer.observe(card)
    })
    
    // Add scroll listener
    window.addEventListener('scroll', handleScroll, { passive: true })
  }, 100)

  handleWheel = (event: WheelEvent) => {
    const now = Date.now()
    const isThrottled = now - lastWheelTime < 450
    const canGoNext = currentSlide.value < features.length - 1
    const canGoPrev = currentSlide.value > 0

    if (!isCarouselLocked.value) {
      if (currentSlide.value !== 0 || event.deltaY <= 0 || activationScrollY === null) {
        return
      }

      const firstCard = cardRefs.value[0]
      if (!firstCard) return

      const rect = firstCard.getBoundingClientRect()
      const cardFullyVisible =
        rect.top >= 0 &&
        rect.left >= 0 &&
        rect.bottom <= window.innerHeight &&
        rect.right <= window.innerWidth
      const cardCenter = (rect.top + rect.bottom) / 2
      const viewportCenter = window.innerHeight / 2
      const centerDelta = cardCenter - viewportCenter
      const isCentered = Math.abs(centerDelta) <= activationTolerance

      if (!cardFullyVisible || !isCentered) return

      isCarouselLocked.value = true
      lockedScrollY = activationScrollY ?? Math.max(0, Math.round(window.scrollY + centerDelta))
      event.preventDefault()

      if (!isThrottled && canGoNext) {
        lastWheelTime = now
        nextSlide()
      }
      return
    }

    // Locked state: only move cards or unlock at bounds
    if (event.deltaY > 0) {
      if (canGoNext) {
        event.preventDefault()
        if (!isThrottled) {
          lastWheelTime = now
          nextSlide()
        }
      } else {
        isCarouselLocked.value = false
      }
      return
    }

    if (event.deltaY < 0) {
      if (canGoPrev) {
        event.preventDefault()
        if (!isThrottled) {
          lastWheelTime = now
          prevSlide()
        }
      } else {
        isCarouselLocked.value = false
      }
    }
  }

  window.addEventListener('wheel', handleWheel, { passive: false })
})

onUnmounted(() => {
  if (handleScroll) {
    window.removeEventListener('scroll', handleScroll)
  }
  if (handleWheel) {
    window.removeEventListener('wheel', handleWheel)
  }
  if (carouselObserver) {
    carouselObserver.disconnect()
  }
  if (autoPlayInterval) {
    clearInterval(autoPlayInterval)
  }
  if (scrollAnimationFrame) {
    cancelAnimationFrame(scrollAnimationFrame)
  }
})
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap");
@import url("https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;500;600;700&display=swap");
@import url("https://fonts.googleapis.com/css2?family=Red+Hat+Display:wght@400;500;600;700&display=swap");

.landing-page {
  min-height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  background: #000000;
  font-family: "Times New Roman", "Times", serif;
}

.navbar {
  background: #000000;
  border-bottom: 1px solid rgba(247, 247, 242, 0.1);
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  backdrop-filter: blur(12px);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  width: 100%;
  z-index: 10;
}

.navbar-inner {
  width: 100%;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: clamp(1.5rem, 4vw, 2.75rem);
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 220px;
  padding-left: clamp(2.5rem, 6vw, 5rem);
}

.nav-logo {
  width: 42px;
  height: 42px;
  object-fit: contain;
  opacity: 0.9;
}

.brand-name {
  font-family: "Times New Roman", "Times", serif;
  font-size: 1rem;
  color: #f9f9fb;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  opacity: 0.85;
}

.nav-links {
  list-style: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: clamp(1.5rem, 5vw, 3rem);
  padding: 0;
  margin: 0;
}

.nav-actions {
  min-width: 220px;
  justify-self: center;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-right: clamp(2.5rem, 6vw, 5rem);
}

.profile-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 0;
  transition: transform 180ms ease;
}

.profile-button:hover {
  transform: translateY(-1px);
}

.profile-button:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgba(90, 112, 255, 0.35);
  border-radius: 6px;
}

.profile-icon {
  width: 22px;
  height: 22px;
  stroke: rgba(249, 249, 251, 0.82);
  stroke-width: 1.5;
  fill: none;
}

.nav-links a {
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.85rem;
  letter-spacing: 0.08em;
  text-transform: none;
  color: rgba(249, 249, 251, 0.82);
  text-decoration: none;
  transition: color 200ms ease, background 200ms ease, -webkit-background-clip 200ms ease, background-clip 200ms ease;
  background-image: none;
  -webkit-background-clip: unset;
  background-clip: unset;
}

.nav-links a:hover {
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

.hero {
  flex: 1;
  display: flex;
  align-items: stretch;
  padding: 0;
  position: relative;
  z-index: 1;
  overflow: hidden;
  min-height: calc(100vh - 54px);
  height: calc(100vh - 54px);
  width: 100%;
  background: #000000;
  margin-top: 54px;
}

.neon-glow {
  display: none;
}

@keyframes pulse-glow {
  0%, 100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.1);
  }
}

.interactive-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.bg-particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(247, 247, 242, 0.3);
  border-radius: 50%;
  animation: float-particle 20s infinite ease-in-out;
}

.bg-particle:nth-child(1) {
  left: 10%;
  top: 20%;
  animation-delay: 0s;
  animation-duration: 25s;
}

.bg-particle:nth-child(2) {
  left: 80%;
  top: 60%;
  animation-delay: -5s;
  animation-duration: 30s;
  background: rgba(247, 247, 242, 0.3);
}

.bg-particle:nth-child(3) {
  left: 50%;
  top: 80%;
  animation-delay: -10s;
  animation-duration: 22s;
  background: rgba(247, 247, 242, 0.2);
}

.bg-particle:nth-child(4) {
  left: 20%;
  top: 40%;
  animation-delay: -15s;
  animation-duration: 28s;
  background: rgba(247, 247, 242, 0.3);
}

.bg-particle:nth-child(5) {
  left: 70%;
  top: 10%;
  animation-delay: -8s;
  animation-duration: 26s;
}

.bg-accent {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.15;
  animation: pulse-accent 8s infinite ease-in-out;
}

.bg-accent-1 {
  width: 300px;
  height: 300px;
  background: rgba(247, 247, 242, 0.2);
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.bg-accent-2 {
  width: 400px;
  height: 400px;
  background: rgba(247, 247, 242, 0.3);
  bottom: -150px;
  right: -150px;
  animation-delay: -2s;
}

.bg-accent-3 {
  width: 250px;
  height: 250px;
  background: rgba(247, 247, 242, 0.2);
  top: 50%;
  right: 10%;
  animation-delay: -4s;
}

@keyframes float-particle {
  0%, 100% {
    transform: translate(0, 0) scale(1);
    opacity: 0.3;
  }
  25% {
    transform: translate(30px, -50px) scale(1.2);
    opacity: 0.6;
  }
  50% {
    transform: translate(-20px, -80px) scale(0.8);
    opacity: 0.4;
  }
  75% {
    transform: translate(40px, -30px) scale(1.1);
    opacity: 0.5;
  }
}

@keyframes pulse-accent {
  0%, 100% {
    transform: scale(1);
    opacity: 0.15;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.25;
  }
}

@keyframes aurora {
  from {
    background-position:
      50% 50%,
      50% 50%;
  }
  to {
    background-position:
      350% 50%,
      350% 50%;
  }
}

.hero-content {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding-left: clamp(2.5rem, 6vw, 5rem);
  min-height: 100vh;
  position: relative;
  z-index: 2;
  pointer-events: none;
}

.hero-content * {
  pointer-events: auto;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  gap: clamp(1.5rem, 3vh, 2.5rem);
  padding: clamp(3rem, 8vw, 6rem) clamp(2rem, 6vw, 5rem);
  z-index: 2;
  max-width: 100%;
  text-align: left;
  position: relative;
}

.hero-video {
  position: fixed;
  top: -200px;
  right: 0;
  width: 60%;
  height: 140vh;
  object-fit: cover;
  z-index: 0;
  pointer-events: none;
  mask-image: linear-gradient(to left, black 0%, black 50%, rgba(0, 0, 0, 0.8) 65%, rgba(0, 0, 0, 0.4) 80%, transparent 100%);
  -webkit-mask-image: linear-gradient(to left, black 0%, black 50%, rgba(0, 0, 0, 0.8) 65%, rgba(0, 0, 0, 0.4) 80%, transparent 100%);
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to right, 
    #000000 0%, 
    #000000 30%, 
    rgba(0, 0, 0, 0.8) 40%, 
    rgba(0, 0, 0, 0.4) 50%, 
    transparent 60%
  );
  z-index: 1;
  pointer-events: none;
}


.cta-stack {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: clamp(1.5rem, 3vh, 2.5rem);
  width: 100%;
}

.pre-title {
  margin: 0 0 0.75rem 0;
  font-family: 'Red Hat Display', sans-serif;
  font-size: clamp(1rem, 1.8vw, 1.25rem);
  font-weight: 400;
  font-style: italic;
  letter-spacing: 0.02em;
  color: rgba(247, 247, 242, 0.85);
  line-height: 1.4;
}

.title {
  margin: -1.5rem 0 0 0;
  font-family: "Times New Roman", "Times", serif;
  font-size: clamp(2.5rem, 6vw, 5.5rem);
  font-weight: 400;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: #F7F7F2;
  line-height: 1.1;
}

.title-divider {
  width: 60px;
  height: 1px;
  background: #F7F7F2;
  margin: 1rem 0;
}

.subtitle {
  margin: 0;
  font-family: 'Red Hat Display', sans-serif;
  font-size: clamp(1rem, 1.8vw, 1.25rem);
  font-weight: 400;
  letter-spacing: -0.01em;
  color: rgba(247, 247, 242, 0.9);
  text-transform: none;
  line-height: 1.1;
  white-space: pre-line;
}

.subtitle .highlight-neon {
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

.cta-group {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 1.5rem;
  margin-top: 0.5rem;
}

.cta {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.85rem 2rem;
  border-radius: 9999px;
  font-family: 'Red Hat Display', sans-serif;
  font-size: 0.95rem;
  letter-spacing: 0.05em;
  text-transform: none;
  text-decoration: none;
  transition:
    transform 180ms ease,
    box-shadow 220ms ease,
    background 220ms ease,
    color 220ms ease;
}

.cta.primary {
  background: #F7F7F2;
  color: #000000;
  box-shadow: none;
  border: 1px solid #F7F7F2;
}

.cta.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.4);
}

.cta.secondary {
  --mouse-x: 50%;
  --mouse-y: 50%;
  background: transparent;
  color: #F7F7F2;
  border: 1px solid rgba(247, 247, 242, 0.3);
  box-shadow: none;
  position: relative;
  overflow: hidden;
}

.cta.secondary::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 0;
  height: 100%;
  background: linear-gradient(
    to right,
    #8B6914 0%,
    #cb9b51 22%,
    #f6e27a 45%,
    #f6f2c0 50%,
    #f6e27a 55%,
    #cb9b51 78%,
    #8B6914 100%
  );
  transition: width 400ms ease;
  z-index: -1;
}

.cta.secondary::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 0;
  height: 100%;
  background: radial-gradient(
    circle 150px at var(--mouse-x) var(--mouse-y),
    #f6f2c0 0%,
    #f6e27a 30%,
    #cb9b51 60%,
    transparent 100%
  );
  transition: width 400ms ease;
  z-index: -1;
  opacity: 0;
}

.cta.secondary:hover {
  transform: translateY(-1px);
  border-color: transparent;
  color: #000000;
}

.cta.secondary:hover::before {
  width: 100%;
}

.cta.secondary:hover::after {
  width: 100%;
  opacity: 1;
}

.cta.secondary:hover .arrow {
  color: #000000;
}

.cta .arrow {
  display: inline-block;
  margin-left: 0.5rem;
  transition: transform 0.2s ease;
}

.cta:hover .arrow {
  transform: translateX(4px);
}

@media (max-width: 960px) {
  .hero-content {
    grid-template-columns: 1fr;
  }

  .hero-image {
    display: none;
  }

  .hero-copy {
    align-items: center;
    text-align: center;
  }

  .cta-stack {
    align-items: center;
  }

  .cta-group {
    justify-content: center;
  }

  .subtitle {
    max-width: 100%;
  }
}

@media (max-width: 600px) {
  .navbar {
    height: 50px;
  }

  .navbar-inner {
    gap: 1.4rem;
  }

  .brand-name {
    font-size: 1rem;
    letter-spacing: 0.12em;
  }

  .nav-links {
    gap: 1.2rem;
  }

  .title {
    letter-spacing: 0.18em;
  }
}

/* Content Sections */
.content-section {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(4rem, 8vw, 8rem) clamp(2.5rem, 6vw, 5rem);
  background: #000000;
  position: relative;
  z-index: 2;
}

#about.content-section {
  background: #F7F7F2;
  position: relative;
  overflow: visible;
  min-height: auto;
  padding: clamp(1rem, 2vw, 2rem) clamp(2rem, 4vw, 3rem);
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

#about.content-section::before {
  display: none;
}

.about-section-wrapper {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: clamp(3rem, 6vw, 5rem);
  align-items: flex-start;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.about-image-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-left: clamp(2rem, 4vw, 3rem);
  padding-top: clamp(2rem, 4vw, 4rem);
}

.about-image {
  width: clamp(300px, 35vw, 450px);
  height: auto;
  object-fit: contain;
  animation: levitate 3s ease-in-out infinite;
  filter: drop-shadow(0 20px 40px rgba(0, 0, 0, 0.2));
}

@keyframes levitate {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-15px);
  }
}

.side-text {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 0;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  max-width: 280px;
}

.side-text-left {
  left: clamp(1rem, 3vw, 2rem);
  text-align: left;
}

.side-text-right {
  right: clamp(1rem, 3vw, 2rem);
  text-align: right;
}

.side-text-item {
  font-family: 'Red Hat Display', sans-serif;
  font-size: clamp(0.9rem, 1.5vw, 1.2rem);
  color: #000000;
  margin: 0;
  line-height: 1.4;
  font-weight: 400;
  letter-spacing: 0.02em;
  position: relative;
}

/* Varying positions to follow Socrates' body shape */
.side-text-left .text-very-close {
  margin-left: clamp(8vw, 12vw, 15vw);
}

.side-text-left .text-close {
  margin-left: clamp(5vw, 8vw, 10vw);
}

.side-text-left .text-medium {
  margin-left: clamp(2vw, 4vw, 6vw);
}

.side-text-left .text-far {
  margin-left: 0;
}

.side-text-right .text-very-close {
  margin-right: clamp(8vw, 12vw, 15vw);
}

.side-text-right .text-close {
  margin-right: clamp(5vw, 8vw, 10vw);
}

.side-text-right .text-medium {
  margin-right: clamp(2vw, 4vw, 6vw);
}

.side-text-right .text-far {
  margin-right: 0;
}

.top-right-text {
  position: absolute;
  top: clamp(3rem, 8vh, 6rem);
  right: clamp(4rem, 8vw, 6rem);
  max-width: clamp(380px, 40vw, 530px);
  z-index: 1;
}

.top-left-text {
  position: absolute;
  top: clamp(8rem, 18vh, 14rem);
  left: clamp(2rem, 5vw, 4rem);
  max-width: clamp(280px, 30vw, 400px);
  z-index: 1;
}

.compact-paragraph {
  font-family: 'Red Hat Display', sans-serif;
  font-size: clamp(0.85rem, 1.3vw, 1.1rem);
  color: #000000;
  line-height: 1.6;
  font-weight: 400;
  letter-spacing: 0.01em;
  margin: 0;
  text-align: left;
}

.top-right-text .compact-paragraph {
  text-align: right;
}

#about .section-content {
  position: relative;
  z-index: 2;
  flex: 1;
  padding-top: clamp(4rem, 8vw, 8rem);
  padding-bottom: 0;
  padding-left: 0;
  width: 100%;
  max-width: 1200px;
  margin: 0;
  margin-left: auto;
  padding-right: clamp(2rem, 6vw, 5rem);
  text-align: left;
}

#about .section-title {
  color: #000000;
  margin-bottom: 1rem;
  margin-top: 0;
  text-align: left;
  font-size: clamp(2rem, 4vw, 3.5rem);
}

#about .title-divider {
  width: 60px;
  height: 1px;
  background: #000000;
  margin: 0.5rem 0;
}

#about .section-text {
  color: rgba(0, 0, 0, 0.8);
}

.section-quote {
  font-family: 'Red Hat Display', sans-serif;
  font-size: clamp(1.1rem, 2vw, 1.4rem);
  font-style: italic;
  color: rgba(0, 0, 0, 0.7);
  text-align: center;
  margin: -1rem 0 3rem 0;
  line-height: 1.6;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
  white-space: nowrap;
}

#about .section-subheading {
  font-family: 'Red Hat Display', sans-serif;
  font-size: clamp(1rem, 1.8vw, 1.3rem);
  font-style: italic;
  text-align: left;
  margin-top: 1rem;
  margin-bottom: clamp(1rem, 2vw, 1.5rem);
  line-height: 1.6;
  max-width: 800px;
  margin-left: 0;
  margin-right: 0;
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

.about-content {
  max-width: 900px;
  margin: 0;
  margin-left: 0;
  display: flex;
  flex-direction: column;
  gap: clamp(2rem, 4vw, 2.5rem);
  text-align: left;
}

.about-paragraph {
  text-align: left;
}

.about-heading {
  font-family: "Times New Roman", "Times", serif;
  font-size: clamp(1.4rem, 2.2vw, 1.8rem);
  font-weight: 400;
  color: #000000;
  margin-bottom: 0.75rem;
  letter-spacing: 0.05em;
}

.about-text {
  font-family: 'Red Hat Display', sans-serif;
  font-size: clamp(0.95rem, 1.5vw, 1.1rem);
  color: rgba(0, 0, 0, 0.75);
  line-height: 1.75;
  margin: 0;
  margin-bottom: clamp(1rem, 2vw, 1.5rem);
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.about-text:last-child {
  margin-bottom: 0;
}

.quote-golden {
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

/* About Grid */
.about-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: auto auto;
  gap: 2.5rem;
  margin-top: 4rem;
  max-width: 1400px;
  margin-left: auto;
  margin-right: auto;
}

.about-card {
  background: #F7F7F2;
  border: 3px solid #000000;
  border-radius: 20px;
  padding: 3.5rem;
  opacity: 0;
  transition: opacity 1s ease, transform 1s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  will-change: transform, opacity;
  transform: translateX(0);
}

.about-card.card-extra-large {
  grid-column: span 2;
  min-height: 450px;
  padding: 4.5rem;
}

.about-card.card-golden {
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-color: transparent;
  border: none;
}

.about-card.card-golden .card-title,
.about-card.card-golden .card-text {
  color: #F7F7F2;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

.about-card.card-golden2 {
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-color: transparent;
  border: none;
}

.about-card.card-golden2 .card-title,
.about-card.card-golden2 .card-text {
  color: #F7F7F2;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

.about-card.card-socrate {
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-color: transparent;
  border: none;
}

.about-card.card-medium {
  min-height: 350px;
  padding: 3.5rem;
}

.about-card.card-small {
  grid-column: span 1;
  min-height: 250px;
  padding: 2.5rem;
}

.about-card:nth-child(4) {
  grid-column: span 2;
}

.about-card.slide-in-left:not(.visible) {
  transform: translateX(-300px) !important;
}

.about-card.slide-in-right:not(.visible) {
  transform: translateX(300px) !important;
}

.about-card.visible {
  opacity: 1;
  transform: translateX(0) !important;
}

.card-title {
  font-family: "Times New Roman", "Times", serif;
  font-size: clamp(2rem, 3vw, 2.5rem);
  font-weight: 400;
  color: #000000;
  margin-bottom: 1.5rem;
  letter-spacing: 0.05em;
}

.card-text {
  font-family: 'Red Hat Display', sans-serif;
  font-size: clamp(1.1rem, 1.8vw, 1.3rem);
  color: rgba(0, 0, 0, 0.8);
  line-height: 1.8;
  margin: 0;
}

.section-content {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}

.section-title {
  font-family: "Times New Roman", "Times", serif;
  font-size: clamp(2.5rem, 5vw, 4rem);
  font-weight: 400;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: #F7F7F2;
  margin-bottom: 2rem;
  text-align: center;
}

.section-text {
  font-family: 'Red Hat Display', sans-serif;
  font-size: clamp(1rem, 1.8vw, 1.25rem);
  color: rgba(247, 247, 242, 0.9);
  line-height: 1.8;
  text-align: center;
  max-width: 800px;
  margin: 0 auto;
}

/* Features Grid */
/* Discover More Carousel */
.discover-section {
  background: #000000;
  min-height: 100vh;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-container {
  width: 100%;
  max-width: 100%;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

.carousel-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
  cursor: grab;
  user-select: none;
  touch-action: pan-x;
  -webkit-touch-callout: none;
}

.carousel-wrapper:active {
  cursor: grabbing;
}

.carousel-track {
  display: flex;
  width: 100%;
  height: 100%;
  transition: transform 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  will-change: transform;
}

.carousel-slide {
  min-width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(3rem, 10vw, 10rem) clamp(4rem, 8vw, 8rem);
  box-sizing: border-box;
}

.feature-card-large {
  width: 100%;
  max-width: min(90vw, 1600px);
  min-height: 80vh;
  padding: clamp(4rem, 8vw, 8rem) clamp(5rem, 10vw, 10rem);
  background: #0f0f0f;
  border: none;
  border-radius: 20px;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(10px);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.feature-card-large.has-background {
  background-size: contain;
  background-position: bottom center;
  background-repeat: no-repeat;
}

.feature-card-large.has-video-background {
  position: relative;
  overflow: hidden;
}

.card-video-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
  pointer-events: none;
}

/* Apple-style scroll animations */
.scroll-animated-card {
  position: relative;
}

/* Gliding text animations - Apple style */
.gliding-text {
  opacity: 0;
  transition: transform 1s cubic-bezier(0.25, 0.46, 0.45, 0.94),
              opacity 1s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  will-change: transform, opacity;
}

.gliding-text.glide-from-right {
  transform: translateX(80px);
}

.gliding-text.glide-from-left {
  transform: translateX(-80px);
}

.gliding-text.glide-in {
  opacity: 1;
  transform: translateX(0);
}

/* Description text appears 1s after the title on all cards */
.feature-description.gliding-text {
  transition-delay: 1s;
}

/* Ensure text is above video */
.feature-card-large.has-video-background .feature-title,
.feature-card-large.has-video-background .feature-description {
  position: relative;
  z-index: 2;
}


.feature-card-large.text-top-right {
  justify-content: flex-start;
  align-items: flex-end;
  text-align: right;
  padding-top: clamp(3rem, 6vw, 5rem);
  padding-right: clamp(3rem, 6vw, 5rem);
}

.feature-card-large.text-bottom-left {
  justify-content: flex-end;
  align-items: flex-start;
  text-align: left;
  padding-bottom: clamp(3rem, 6vw, 5rem);
  padding-left: clamp(3rem, 6vw, 5rem);
}

.feature-title {
  font-family: "Times New Roman", "Times", serif;
  font-size: clamp(2.5rem, 5vw, 4.5rem);
  font-weight: 400;
  color: #F7F7F2;
  margin-bottom: clamp(1.5rem, 3vw, 2.5rem);
  letter-spacing: 0.02em;
  line-height: 1.2;
  position: relative;
  z-index: 1;
}

.feature-title.title-section-font {
  letter-spacing: 0.05em;
  text-transform: uppercase;
  font-size: clamp(1.4rem, 3vw, 3rem);
  max-width: none;
  white-space: nowrap;
}

.feature-card-large.text-top-right .feature-title.title-section-font,
.feature-card-large.text-bottom-left .feature-title.title-section-font {
  max-width: 600px;
}

.feature-card-large.text-top-right .feature-title.title-section-font {
  margin-left: auto;
}

.feature-card-large.text-bottom-left .feature-title.title-section-font {
  margin-right: auto;
}

.feature-description {
  font-family: 'Red Hat Display', sans-serif;
  font-size: clamp(1.1rem, 2vw, 1.5rem);
  color: rgba(247, 247, 242, 0.9);
  line-height: 1.7;
  max-width: 900px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.feature-card-large.text-top-right .feature-description {
  max-width: 600px;
  margin-left: auto;
  margin-right: 0;
}

.feature-card-large.text-bottom-left .feature-description {
  max-width: 700px;
  margin-right: auto;
  margin-left: 0;
}

/* Job Preparation card: title upper-right, description lower-left; same styles as other cards, no boxes; text directly on image */
.feature-card-large.card-job-prep.text-top-right {
  justify-content: flex-start;
  align-items: stretch;
  text-align: left;
  padding-top: 0;
  padding-right: 0;
  padding-bottom: 0;
  position: relative;
}

.feature-card-large.card-job-prep .feature-title {
  position: absolute;
  right: clamp(4rem, 9vw, 7rem);
  top: clamp(2.5rem, 5vw, 4rem);
  color: #F7F7F2;
  background: none;
  margin: 0;
  text-align: right;
  white-space: nowrap;
}

.feature-card-large.card-job-prep .feature-title.title-section-font {
  font-family: "Times New Roman", "Times", serif;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  font-size: clamp(1.75rem, 3.8vw, 3.6rem);
  margin: 0;
  white-space: nowrap;
  line-height: 1.2;
}

.feature-card-large.card-job-prep .feature-description {
  position: absolute;
  left: clamp(5rem, 10vw, 8rem);
  top: auto;
  bottom: clamp(16.25rem, 34.5vw, 25rem);
  color: #000;
  background: none;
  font-family: 'Red Hat Display', sans-serif;
  font-size: clamp(0.72rem, 1.15vw, 0.92rem);
  font-weight: 700;
  line-height: 1.4;
  max-width: min(265px, 29vw);
  margin: 0;
  padding: 0;
  text-align: left;
}

/* Written Evaluation card: golden title */
.feature-title.title-golden {
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

/* Notes & Summaries card: text in top-right of iPad screen */
.feature-card-large.card-notes-ipad.text-top-right {
  padding-top: clamp(4rem, 12vw, 14rem);
  padding-right: clamp(3rem, 28vw, 32rem);
}

.feature-card-large.card-notes-ipad .feature-title {
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Display", "SF Pro Text", "Helvetica Neue", Arial, sans-serif;
  font-size: clamp(1.35rem, 2.5vw, 1.9rem);
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.02em;
  line-height: 1.3;
  margin-bottom: 0.5rem;
  text-align: right;
  white-space: normal;
  max-width: min(50%, 420px);
  margin-left: auto;
  text-transform: none;
}

.feature-card-large.card-notes-ipad .feature-description {
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", Arial, sans-serif;
  font-size: clamp(0.8rem, 1.4vw, 1rem);
  font-weight: 400;
  color: #1d1d1f;
  line-height: 1.5;
  margin: 0;
  text-align: right;
  max-width: min(50%, 420px);
  margin-left: auto;
}

/* Hidden until after 1s delay, then visible at 0.9 like other cards */
.feature-card-large.card-notes-ipad .feature-description.gliding-text {
  opacity: 0;
}
.feature-card-large.card-notes-ipad .feature-description.gliding-text.glide-in {
  opacity: 0.9;
}


.carousel-arrows {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 clamp(1.5rem, 4vw, 4rem);
  z-index: 10;
  pointer-events: none;
}

.carousel-arrow {
  pointer-events: auto;
  width: 3rem;
  height: 3rem;
  border-radius: 0;
  border: none;
  background: transparent;
  color: rgba(247, 247, 242, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.carousel-arrow:hover {
  color: #cb9b51;
  transform: scale(1.08);
}

.carousel-arrow svg {
  display: block;
  width: 1.2rem;
  height: 1.2rem;
}


/* Contact Section */
.contact-info {
  margin-top: 2rem;
}

.contact-text {
  font-family: 'Red Hat Display', sans-serif;
  font-size: clamp(1rem, 1.8vw, 1.25rem);
  color: rgba(247, 247, 242, 0.9);
  line-height: 1.8;
  text-align: center;
}

@media (max-width: 1024px) {
  .about-grid {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .about-card.card-extra-large,
  .about-card:nth-child(4) {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .about-section-wrapper {
    grid-template-columns: 1fr;
    gap: clamp(2rem, 5vw, 3rem);
  }

  .about-image-container {
    padding-left: 0;
    justify-content: center;
  }

  .about-image {
    width: clamp(250px, 55vw, 380px);
  }

  #about .section-content {
    padding-right: clamp(2rem, 4vw, 3rem);
  }

  .about-grid {
    grid-template-columns: 1fr;
    gap: 2rem;
  }

  .about-card {
    padding: 2.5rem;
    min-height: auto;
  }

  .about-card.card-extra-large,
  .about-card.card-medium,
  .about-card.card-small,
  .about-card:nth-child(4) {
    grid-column: span 1;
    min-height: auto;
    padding: 2.5rem;
  }

  .about-card.slide-in-left:not(.visible) {
    transform: translateX(-200px) !important;
  }

  .about-card.slide-in-right:not(.visible) {
    transform: translateX(200px) !important;
  }

  .about-card.visible {
    transform: translateX(0) !important;
  }

  .carousel-slide {
    padding: clamp(2rem, 6vw, 4rem) clamp(1.5rem, 4vw, 3rem);
  }

  .feature-card-large {
    padding: clamp(2rem, 4vw, 3rem);
  }

}
</style>
