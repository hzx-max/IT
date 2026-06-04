<template>
  <div v-if="mediaList.length > 0" class="card-carousel" @mouseenter="pause" @mouseleave="resume">
    <div class="carousel-viewport">
      <div class="carousel-track" :style="{ transform: `translateX(-${current * 100}%)` }">
        <div v-for="(m, i) in mediaList" :key="i" class="carousel-slide">
          <img v-if="m.type === 'image'" :src="m.url" class="carousel-media" />
          <video v-else :src="m.url" class="carousel-media" muted loop playsinline></video>
        </div>
      </div>
    </div>
    <button v-if="mediaList.length > 1" class="carousel-btn carousel-prev" @click.stop="prev">&#8249;</button>
    <button v-if="mediaList.length > 1" class="carousel-btn carousel-next" @click.stop="next">&#8250;</button>
    <div v-if="mediaList.length > 1" class="carousel-dots">
      <span v-for="(_, i) in mediaList" :key="i" :class="['carousel-dot', { active: i === current }]" @click.stop="goTo(i)"></span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  images: { type: Array, default: () => [] },
  videos: { type: Array, default: () => [] },
  topo: { type: Array, default: () => [] }
})

const mediaList = computed(() => {
  const list = []
  if (Array.isArray(props.images)) {
    props.images.forEach(url => { if (url) list.push({ type: 'image', url }) })
  }
  if (Array.isArray(props.videos)) {
    props.videos.forEach(url => { if (url) list.push({ type: 'video', url }) })
  }
  if (Array.isArray(props.topo)) {
    props.topo.forEach(t => {
      const url = t?.url
      if (url) list.push({ type: 'image', url })
    })
  }
  return list
})

const current = ref(0)
let timer = null

function next() { current.value = (current.value + 1) % mediaList.value.length }
function prev() { current.value = (current.value - 1 + mediaList.value.length) % mediaList.value.length }
function goTo(i) { current.value = i }
function pause() { clearInterval(timer) }
function resume() { startAuto() }
function startAuto() {
  clearInterval(timer)
  if (mediaList.value.length > 1) timer = setInterval(next, 3000)
}

onMounted(() => startAuto())
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.card-carousel {
  position: relative;
  width: 100%;
  margin-bottom: 10px;
  border-radius: 8px;
  cursor: pointer;
  z-index: 1;
}
.card-carousel:hover { z-index: 30; }

.carousel-viewport {
  width: 100%;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  border-radius: 8px;
  background: #f1f5f9;
}

.carousel-track {
  display: flex;
  height: 100%;
  transition: transform 0.4s ease;
}

.carousel-slide {
  min-width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.carousel-media {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
  transform-origin: center center;
}

.card-carousel:hover .carousel-media {
  transform: scale(1.5);
}

.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0,0,0,.4);
  color: #fff;
  border: none;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity .2s;
  z-index: 5;
}
.card-carousel:hover .carousel-btn { opacity: 1; }
.carousel-prev { left: 8px; }
.carousel-next { right: 8px; }

.carousel-dots {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
  z-index: 5;
}
.carousel-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: rgba(255,255,255,.5);
  cursor: pointer;
  transition: all .2s;
}
.carousel-dot.active {
  background: #fff;
  transform: scale(1.3);
}
</style>