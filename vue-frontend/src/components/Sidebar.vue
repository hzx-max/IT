<template>
  <nav class="sidebar fixed top-0 left-0 w-[240px] h-screen z-50 flex flex-col"
       style="background:linear-gradient(180deg,#e0f0ff 0%,#d0e4f8 50%,#c5ddf5 100%)">
    <router-link to="/" class="px-[22px] py-6 border-b border-slate-300/40 flex items-center no-underline">
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="mr-2.5 text-blue-600 shrink-0">
        <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
        <line x1="8" y1="21" x2="16" y2="21"/>
        <line x1="12" y1="17" x2="12" y2="21"/>
      </svg>
      <span class="text-lg text-slate-800 whitespace-nowrap font-bold tracking-tight">IT运维学习平台</span>
    </router-link>

    <div class="flex-1 overflow-y-auto overflow-x-hidden py-2">
      <NavDropdown v-for="m in filteredMenus" :key="m.name" :open="openDropdown===m.name" :icon="m.icon" :label="m.label" :items="m.items" @toggle="onToggle(m.name)" />
    </div>

    <!-- 超级管理员入口 -->
    <div v-if="isSuperAdmin" class="px-[22px] py-2 border-t border-slate-300/40 bg-slate-200/30">
      <router-link to="/super-admin" class="flex items-center gap-2 px-3 py-2 rounded-lg text-sm text-purple-700 hover:bg-purple-100 transition no-underline">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1Z"/></svg>
        <span>管理员管理</span>
      </router-link>
    </div>

    <!-- 快捷键提示 -->
    <div class="mt-auto px-[22px] py-4 border-t border-slate-300/40 text-xs text-slate-600 shrink-0 bg-slate-200/40">
      <kbd class="inline-block px-2 py-0.5 bg-blue-200 border border-blue-300 rounded text-xs text-blue-700 mx-0.5 font-semibold">ESC</kbd> 返回
      <kbd class="inline-block px-2 py-0.5 bg-blue-200 border border-blue-300 rounded text-xs text-blue-700 mx-0.5 font-semibold">Ctrl+K</kbd> 搜索
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import NavDropdown from './NavDropdown.vue'

const route = useRoute()
const openDropdown = ref('')
const loggedIn = ref(false)
const role = ref('')

const isAdmin = computed(() => loggedIn.value && (role.value === 'SUPER_ADMIN' || role.value === 'ADMIN'))
const isSuperAdmin = computed(() => loggedIn.value && role.value === 'SUPER_ADMIN')

const allMenus = [
  { name: 'cmd', icon: 'cmd', label: '网络命令', items: [
    { label: '命令列表', path: '/cmd' },
    { label: '管理命令', path: '/cmd/admin', admin: true },
    { label: '添加命令', path: '/cmd/add', admin: true }
  ]},
  { name: 'fault', icon: 'fault', label: '网络故障', items: [
    { label: '故障列表', path: '/fault' },
    { label: '管理故障', path: '/fault/admin', admin: true },
    { label: '添加故障', path: '/fault/add', admin: true }
  ]},
  { name: 'desktop', icon: 'desktop', label: '桌面运维', items: [
    { label: '桌面列表', path: '/desktop' },
    { label: '管理桌面', path: '/desktop/admin', admin: true },
    { label: '添加桌面', path: '/desktop/add', admin: true }
  ]},
  { name: 'linux', icon: 'linux', label: 'Linux', items: [
    { label: 'Linux列表', path: '/linux' },
    { label: '管理Linux', path: '/linux/admin', admin: true },
    { label: '添加Linux', path: '/linux/add', admin: true }
  ]},
  { name: 'office', icon: 'office', label: 'Office', items: [
    { label: 'Office列表', path: '/office' },
    { label: '管理Office', path: '/office/admin', admin: true },
    { label: '添加Office', path: '/office/add', admin: true }
  ]},
  { name: 'ai', icon: 'ai', label: 'AI运维', items: [
    { label: 'AI列表', path: '/ai' },
    { label: '管理AI', path: '/ai/admin', admin: true },
    { label: '添加AI', path: '/ai/add', admin: true }
  ]}
]

const filteredMenus = computed(() => {
  return allMenus.map(m => ({
    ...m,
    items: m.items.filter(item => !item.admin || isAdmin.value)
  }))
})

function updateAuthState() {
  const token = localStorage.getItem('token')
  loggedIn.value = !!token
  role.value = localStorage.getItem('role') || ''
}

function onToggle(name) {
  openDropdown.value = openDropdown.value === name ? '' : name
}

function onStorageChange(e) {
  if (e.key === 'token' || e.key === null) {
    updateAuthState()
  }
}

watch(() => route.path, (p) => {
  for (const m of allMenus) {
    if (p.startsWith('/' + m.name)) {
      if (openDropdown.value !== m.name) openDropdown.value = m.name
      return
    }
  }
  openDropdown.value = ''
}, { immediate: true })

onMounted(() => {
  updateAuthState()
  window.addEventListener('storage', onStorageChange)
})

onUnmounted(() => {
  window.removeEventListener('storage', onStorageChange)
})
</script>

<style scoped>
.sidebar { border-right: 1px solid rgba(0,0,0,.06); box-shadow: 3px 0 16px rgba(0,0,0,.06); }
@media (max-width: 768px) { .sidebar { display: none; } }
</style>