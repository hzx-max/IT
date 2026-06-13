<template>
  <router-view />
</template>

<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { apiAuth } from './api/index.js'

const router = useRouter()

// 页面加载时校验 token 有效性，过期则清除登录状态
async function checkToken() {
  const token = localStorage.getItem('token')
  if (!token) return
  try {
    await apiAuth.me()
  } catch {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    localStorage.removeItem('userId')
  }
}

function handleKeydown(e) {
  if (e.key === 'Escape' && !['INPUT', 'TEXTAREA'].includes(e.target.tagName) && !e.ctrlKey && !e.metaKey && !e.altKey) {
    e.preventDefault()
    if (document.activeElement) document.activeElement.blur()
    router.back()
  }
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    const el = document.querySelector('[data-search-focus]')
    if (el) { el.focus(); el.select() }
  }
}

onMounted(() => {
  checkToken()
  document.addEventListener('keydown', handleKeydown)
})
onBeforeUnmount(() => document.removeEventListener('keydown', handleKeydown))
</script>
