<template>
  <router-view />
</template>

<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

function handleKeydown(e) {
  // ESC 返回
  if (e.key === 'Escape' && !['INPUT', 'TEXTAREA'].includes(e.target.tagName) && !e.ctrlKey && !e.metaKey && !e.altKey) {
    e.preventDefault()
    if (document.activeElement) document.activeElement.blur()
    router.back()
  }
  // Ctrl+K 搜索
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    const el = document.querySelector('[data-search-focus]')
    if (el) { el.focus(); el.select() }
  }
}

onMounted(() => document.addEventListener('keydown', handleKeydown))
onBeforeUnmount(() => document.removeEventListener('keydown', handleKeydown))
</script>
