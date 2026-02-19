<template>
  <div class="wavy-container" :class="containerClass">
    <canvas
      id="canvas"
      ref="canvasRef"
      class="wavy-canvas"
      :style="{ filter: isSafari ? `blur(${blur}px)` : undefined }"
    ></canvas>
    <div class="wavy-content" :class="contentClass">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { createNoise3D } from 'simplex-noise'
import { onBeforeUnmount, onMounted, ref, watchEffect } from 'vue'

interface WavyBackgroundProps {
  contentClass?: string
  containerClass?: string
  colors?: string[]
  waveWidth?: number
  backgroundFill?: string
  blur?: number
  speed?: 'slow' | 'fast'
  waveOpacity?: number
}

const props = withDefaults(defineProps<WavyBackgroundProps>(), {
  colors: () => ['#d0dad8', '#739995', '#0f191b', '#2d4f57', '#9ebbb6'],
  waveWidth: 100,
  backgroundFill: '#011C27',
  blur: 20,
  speed: 'fast',
  waveOpacity: 0.5,
})

const noise = createNoise3D()

let width = 0
let height = 0
let nTime = 0
let ctx: CanvasRenderingContext2D | null = null
let animationId = 0

const canvasRef = ref<HTMLCanvasElement | null>(null)
const isSafari = ref(false)

const getSpeed = () => (props.speed === 'slow' ? 0.001 : 0.002)

const drawWave = (count: number) => {
  if (!ctx) return
  nTime += getSpeed()
  for (let i = 0; i < count; i++) {
    ctx.beginPath()
    ctx.lineWidth = props.waveWidth
    ctx.strokeStyle = props.colors[i % props.colors.length]
    for (let x = 0; x < width; x += 5) {
      const y = noise(x / 800, 0.3 * i, nTime) * 120
      ctx.lineTo(x, y + height * 0.52)
    }
    ctx.stroke()
    ctx.closePath()
  }
}

const render = () => {
  if (!ctx) return
  ctx.globalAlpha = 1
  ctx.save()
  ctx.filter = 'none'
  ctx.fillStyle = props.backgroundFill
  ctx.fillRect(0, 0, width, height)
  ctx.restore()
  ctx.globalAlpha = props.waveOpacity
  drawWave(5)
  animationId = requestAnimationFrame(render)
}

const resizeCanvas = () => {
  const canvas = canvasRef.value
  if (!canvas || !ctx) return
  const parent = canvas.parentElement
  if (!parent) return
  width = ctx.canvas.width = parent.clientWidth
  height = ctx.canvas.height = parent.clientHeight
  ctx.filter = `blur(${props.blur}px)`
}

const init = () => {
  const canvas = canvasRef.value
  if (!canvas) return
  ctx = canvas.getContext('2d')
  if (!ctx) return
  resizeCanvas()
  render()
}

onMounted(() => {
  isSafari.value =
    typeof window !== 'undefined' &&
    navigator.userAgent.includes('Safari') &&
    !navigator.userAgent.includes('Chrome')

  init()
  window.addEventListener('resize', resizeCanvas)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize', resizeCanvas)
})

watchEffect(() => {
  if (ctx) {
    ctx.filter = `blur(${props.blur}px)`
  }
})
</script>

<style scoped>
.wavy-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.wavy-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.wavy-content {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
</style>

