<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 -->
    <Sidebar />
    <!-- 主内容 -->
    <div class="flex-1 ml-[240px] min-h-screen flex flex-col">
      <!-- 顶部栏 -->
      <header class="h-14 border-b border-slate-200 bg-white/80 backdrop-blur-md flex items-center justify-end px-6 gap-3 shrink-0 sticky top-0 z-40">
        <template v-if="loggedIn">
          <span class="text-sm text-slate-500">
            <span class="font-medium text-slate-700">{{ username }}</span>
            <span v-if="role" class="ml-2 inline-block px-2 py-0.5 rounded-full text-xs font-medium"
              :class="role === 'SUPER_ADMIN' ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'">
              {{ role === 'SUPER_ADMIN' ? '超级管理员' : role === 'ADMIN' ? '管理员' : '用户' }}
            </span>
          </span>
          <button @click="handleLogout" class="px-3 py-1.5 text-sm rounded-lg text-slate-500 hover:text-red-600 hover:bg-red-50 transition-colors">
            退出登录
          </button>
        </template>
      </header>
      <div class="flex-1 p-7 page-enter-active">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'

const router = useRouter()
const loggedIn = ref(false)
const username = ref('')
const role = ref('')

function updateAuth() {
  loggedIn.value = !!localStorage.getItem('token')
  username.value = localStorage.getItem('username') || ''
  role.value = localStorage.getItem('role') || ''
}

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  window.dispatchEvent(new Event('auth-change'))
  router.push('/')
}

onMounted(() => {
  updateAuth()
  window.addEventListener('auth-change', updateAuth)
})
</script>

<style scoped>
@media (max-width: 768px) {
  .ml-\[240px\] { margin-left: 0; }
  .p-7 { padding: 16px; }
}
</style>
