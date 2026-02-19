<template>
  <div class="pixelated-canvas-container" :class="containerClass">
    <canvas
      ref="canvasRef"
      :width="width"
      :height="height"
      class="pixelated-canvas"
      :class="className"
    ></canvas>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'

interface PixelatedCanvasProps {
  src: string
  width?: number
  height?: number
  cellSize?: number
  dotScale?: number
  shape?: 'square' | 'circle'
  backgroundColor?: string
  dropoutStrength?: number
  interactive?: boolean
  distortionStrength?: number
  distortionRadius?: number
  distortionMode?: 'swirl' | 'repel' | 'attract'
  followSpeed?: number
  jitterStrength?: number
  jitterSpeed?: number
  sampleAverage?: boolean
  tintColor?: string
  tintStrength?: number
  className?: string
  containerClass?: string
}

const props = withDefaults(defineProps<PixelatedCanvasProps>(), {
  width: 400,
  height: 500,
  cellSize: 3,
  dotScale: 0.9,
  shape: 'square',
  backgroundColor: '#000000',
  dropoutStrength: 0.4,
  interactive: true,
  distortionStrength: 3,
  distortionRadius: 80,
  distortionMode: 'swirl',
  followSpeed: 0.2,
  jitterStrength: 4,
  jitterSpeed: 4,
  sampleAverage: true,
  tintColor: '#FFFFFF',
  tintStrength: 0.2,
})

const canvasRef = ref<HTMLCanvasElement | null>(null)
let ctx: CanvasRenderingContext2D | null = null
let image: HTMLImageElement | null = null
let sourceImageData: ImageData | null = null
let animationId = 0
let targetMouseX = 0
let targetMouseY = 0
let mouseX = 0
let mouseY = 0
let time = 0
let isMouseOver = false

const loadImage = (): Promise<HTMLImageElement> => {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => resolve(img)
    img.onerror = reject
    img.src = props.src
  })
}

const getPixelColor = (x: number, y: number, width: number, height: number): [number, number, number, number] => {
  if (!sourceImageData) return [0, 0, 0, 0]
  
  const px = Math.floor(Math.max(0, Math.min(width - 1, x)))
  const py = Math.floor(Math.max(0, Math.min(height - 1, y)))
  const idx = (py * width + px) * 4
  
  if (idx < 0 || idx >= sourceImageData.data.length) return [0, 0, 0, 0]
  
  return [
    sourceImageData.data[idx],
    sourceImageData.data[idx + 1],
    sourceImageData.data[idx + 2],
    sourceImageData.data[idx + 3]
  ]
}

const getAverageColor = (cx: number, cy: number, cellSize: number, canvasWidth: number, canvasHeight: number): [number, number, number, number] => {
  if (!sourceImageData) return [0, 0, 0, 0]
  
  let r = 0, g = 0, b = 0, a = 0, count = 0
  const half = Math.floor(cellSize / 2)
  
  for (let dy = -half; dy <= half; dy++) {
    for (let dx = -half; dx <= half; dx++) {
      const [pr, pg, pb, pa] = getPixelColor(cx + dx, cy + dy, canvasWidth, canvasHeight)
      r += pr
      g += pg
      b += pb
      a += pa
      count++
    }
  }
  
  return count > 0 
    ? [Math.floor(r / count), Math.floor(g / count), Math.floor(b / count), Math.floor(a / count)]
    : [0, 0, 0, 0]
}

const drawPixelated = () => {
  if (!ctx || !canvasRef.value || !image || !sourceImageData) return

  const canvas = canvasRef.value
  const cellSize = props.cellSize
  
  // Clear or fill background
  if (props.backgroundColor === 'transparent') {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
  } else {
    ctx.fillStyle = props.backgroundColor
    ctx.fillRect(0, 0, canvas.width, canvas.height)
  }

  // Smooth mouse following
  if (props.interactive) {
    mouseX += (targetMouseX - mouseX) * props.followSpeed
    mouseY += (targetMouseY - mouseY) * props.followSpeed
    
    if (!isMouseOver) {
      mouseX *= 0.9
      mouseY *= 0.9
    }
  }

  const cols = Math.ceil(canvas.width / cellSize)
  const rows = Math.ceil(canvas.height / cellSize)

  for (let row = 0; row < rows; row++) {
    for (let col = 0; col < cols; col++) {
      const x = col * cellSize + cellSize / 2
      const y = row * cellSize + cellSize / 2

      // Calculate distortion
      let offsetX = 0
      let offsetY = 0

      if (props.interactive) {
        const dx = mouseX - x
        const dy = mouseY - y
        const distance = Math.sqrt(dx * dx + dy * dy)

        if (distance < props.distortionRadius) {
          const influence = 1 - (distance / props.distortionRadius)
          const strength = influence * props.distortionStrength

          if (props.distortionMode === 'swirl') {
            const angle = Math.atan2(dy, dx) + strength * 2
            offsetX = Math.cos(angle) * strength
            offsetY = Math.sin(angle) * strength
          } else if (props.distortionMode === 'repel') {
            const angle = Math.atan2(dy, dx)
            offsetX = Math.cos(angle) * strength
            offsetY = Math.sin(angle) * strength
          } else if (props.distortionMode === 'attract') {
            const angle = Math.atan2(-dy, -dx)
            offsetX = Math.cos(angle) * strength
            offsetY = Math.sin(angle) * strength
          }
        }
      }

      // Jitter effect
      const jitterX = Math.sin(time * props.jitterSpeed + x * 0.01) * props.jitterStrength * 0.5
      const jitterY = Math.cos(time * props.jitterSpeed + y * 0.01) * props.jitterStrength * 0.5

      const sampleX = x + offsetX + jitterX
      const sampleY = y + offsetY + jitterY

      // Get color
      let r: number, g: number, b: number, a: number
      
      if (props.sampleAverage) {
        [r, g, b, a] = getAverageColor(sampleX, sampleY, cellSize, canvas.width, canvas.height)
      } else {
        [r, g, b, a] = getPixelColor(sampleX, sampleY, canvas.width, canvas.height)
      }

      // Apply dropout based on brightness
      if (props.dropoutStrength > 0 && a > 0) {
        const brightness = (r + g + b) / 3
        const threshold = (brightness / 255) * props.dropoutStrength
        if (Math.random() < threshold) {
          continue
        }
      }

      // Skip transparent pixels
      if (a < 10) continue

      // Apply tint
      const tintR = parseInt(props.tintColor.slice(1, 3), 16)
      const tintG = parseInt(props.tintColor.slice(3, 5), 16)
      const tintB = parseInt(props.tintColor.slice(5, 7), 16)
      
      const finalR = Math.min(255, Math.max(0, r + (tintR - r) * props.tintStrength))
      const finalG = Math.min(255, Math.max(0, g + (tintG - g) * props.tintStrength))
      const finalB = Math.min(255, Math.max(0, b + (tintB - b) * props.tintStrength))

      ctx.fillStyle = `rgb(${finalR}, ${finalG}, ${finalB})`

      const size = cellSize * props.dotScale
      const drawX = col * cellSize + (cellSize - size) / 2
      const drawY = row * cellSize + (cellSize - size) / 2

      if (props.shape === 'circle') {
        ctx.beginPath()
        ctx.arc(col * cellSize + cellSize / 2, row * cellSize + cellSize / 2, size / 2, 0, Math.PI * 2)
        ctx.fill()
      } else {
        ctx.fillRect(drawX, drawY, size, size)
      }
    }
  }
}

const animate = () => {
  if (!image) return
  time += 0.016 // ~60fps
  drawPixelated()
  animationId = requestAnimationFrame(animate)
}

const handleMouseMove = (e: MouseEvent) => {
  if (!canvasRef.value) return
  const rect = canvasRef.value.getBoundingClientRect()
  const scaleX = canvasRef.value.width / rect.width
  const scaleY = canvasRef.value.height / rect.height
  targetMouseX = (e.clientX - rect.left) * scaleX
  targetMouseY = (e.clientY - rect.top) * scaleY
  isMouseOver = true
}

const handleMouseLeave = () => {
  isMouseOver = false
}

const init = async () => {
  if (!canvasRef.value) return
  
  ctx = canvasRef.value.getContext('2d', { alpha: props.backgroundColor === 'transparent' })
  if (!ctx) return

  try {
    image = await loadImage()
    if (!image) return

    // Create source image data
    const tempCanvas = document.createElement('canvas')
    tempCanvas.width = canvasRef.value.width
    tempCanvas.height = canvasRef.value.height
    const tempCtx = tempCanvas.getContext('2d')
    if (!tempCtx) return

    // Draw image to fit canvas
    tempCtx.drawImage(image, 0, 0, canvasRef.value.width, canvasRef.value.height)
    sourceImageData = tempCtx.getImageData(0, 0, canvasRef.value.width, canvasRef.value.height)

    drawPixelated()
    
    if (props.interactive) {
      animationId = requestAnimationFrame(animate)
    }
  } catch (error) {
    console.error('Failed to initialize pixelated canvas:', error)
  }
}

onMounted(() => {
  init()
  if (props.interactive && canvasRef.value) {
    canvasRef.value.addEventListener('mousemove', handleMouseMove)
    canvasRef.value.addEventListener('mouseleave', handleMouseLeave)
  }
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animationId)
  if (canvasRef.value) {
    canvasRef.value.removeEventListener('mousemove', handleMouseMove)
    canvasRef.value.removeEventListener('mouseleave', handleMouseLeave)
  }
})
</script>

<style scoped>
.pixelated-canvas-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.pixelated-canvas {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
}
</style>
