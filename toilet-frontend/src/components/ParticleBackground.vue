<template>
  <canvas ref="canvasRef" class="particle-bg" />
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const canvasRef = ref(null)
let ctx, w, h, particles = []
let animationId

class Particle {
  constructor() {
    this.reset()
    this.y = Math.random() * h
  }
  reset() {
    this.x = Math.random() * w
    this.y = -10
    this.size = Math.random() * 2.5 + 0.5
    this.speed = Math.random() * 0.6 + 0.2
    this.opacity = Math.random() * 0.5 + 0.15
    this.wind = (Math.random() - 0.5) * 0.3
  }
  update() {
    this.y += this.speed
    this.x += this.wind
    if (this.y > h + 10) this.reset()
    if (this.x < -10) this.x = w + 10
    if (this.x > w + 10) this.x = -10
  }
  draw() {
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(24, 144, 255, ${this.opacity})`
    ctx.fill()
  }
}

function init() {
  const canvas = canvasRef.value
  if (!canvas) return
  ctx = canvas.getContext('2d')
  resize()
  particles = Array.from({ length: 80 }, () => new Particle())
  animate()
}

function resize() {
  const canvas = canvasRef.value
  if (!canvas) return
  w = canvas.width = window.innerWidth
  h = canvas.height = window.innerHeight
}

function animate() {
  ctx.clearRect(0, 0, w, h)
  particles.forEach(p => { p.update(); p.draw() })

  // 连线效果
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x
      const dy = particles[i].y - particles[j].y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < 120) {
        ctx.beginPath()
        ctx.moveTo(particles[i].x, particles[i].y)
        ctx.lineTo(particles[j].x, particles[j].y)
        ctx.strokeStyle = `rgba(24, 144, 255, ${0.04 * (1 - dist / 120)})`
        ctx.lineWidth = 0.5
        ctx.stroke()
      }
    }
  }

  animationId = requestAnimationFrame(animate)
}

onMounted(() => init())
onUnmounted(() => {
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize', resize)
})

window.addEventListener('resize', resize)
</script>

<style scoped>
.particle-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}
</style>
