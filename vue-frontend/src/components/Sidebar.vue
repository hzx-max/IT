<template>
  <nav class="sidebar fixed top-0 left-0 w-[240px] h-screen z-50 flex flex-col"
       style="background:linear-gradient(180deg,#0f172a 0%,#1a2332 100%)">
    <router-link to="/" class="px-[22px] py-6 border-b border-white/[.06] flex items-center no-underline">
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="mr-2.5 text-blue-400 shrink-0">
        <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
        <line x1="8" y1="21" x2="16" y2="21"/>
        <line x1="12" y1="17" x2="12" y2="21"/>
      </svg>
      <span class="text-lg text-slate-200 whitespace-nowrap font-bold tracking-tight">IT运维学习平台</span>
    </router-link>

    <div class="flex-1 overflow-y-auto overflow-x-hidden py-2">
      <NavDropdown v-for="m in menus" :key="m.name" :open="openDropdown===m.name" :icon="m.icon" :label="m.label" :items="m.items" @toggle="onToggle(m.name)" />
    </div>

    <div class="mt-auto px-[22px] py-4 border-t border-white/[.08] text-xs text-slate-500 shrink-0 bg-black/[.15]">
      <kbd class="inline-block px-2 py-0.5 bg-blue-900/40 border border-blue-500/40 rounded text-xs text-blue-300 mx-0.5 font-semibold">ESC</kbd> 返回
      <kbd class="inline-block px-2 py-0.5 bg-blue-900/40 border border-blue-500/40 rounded text-xs text-blue-300 mx-0.5 font-semibold">Ctrl+K</kbd> 搜索
    </div>
  </nav>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import NavDropdown from './NavDropdown.vue'

const route = useRoute()
const openDropdown = ref('')

const menus = [
  { name: 'cmd', icon: 'cmd', label: '网络命令', items: [
    { label: '命令列表', path: '/cmd' },
    { label: '管理命令', path: '/cmd/admin' },
    { label: '添加命令', path: '/cmd/add' }
  ]},
  { name: 'fault', icon: 'fault', label: '网络故障', items: [
    { label: '故障列表', path: '/fault' },
    { label: '管理故障', path: '/fault/admin' },
    { label: '添加故障', path: '/fault/add' }
  ]},
  { name: 'desktop', icon: 'desktop', label: '桌面运维', items: [
    { label: '桌面列表', path: '/desktop' },
    { label: '管理桌面', path: '/desktop/admin' },
    { label: '添加桌面', path: '/desktop/add' }
  ]},
  { name: 'linux', icon: 'linux', label: 'Linux', items: [
    { label: 'Linux列表', path: '/linux' },
    { label: '管理Linux', path: '/linux/admin' },
    { label: '添加Linux', path: '/linux/add' }
  ]},
  { name: 'office', icon: 'office', label: 'Office', items: [
    { label: 'Office列表', path: '/office' },
    { label: '管理Office', path: '/office/admin' },
    { label: '添加Office', path: '/office/add' }
  ]},
  { name: 'ai', icon: 'ai', label: 'AI运维', items: [
    { label: 'AI列表', path: '/ai' },
    { label: '管理AI', path: '/ai/admin' },
    { label: '添加AI', path: '/ai/add' }
  ]}
]

function onToggle(name) {
  if (openDropdown.value === name) {
    openDropdown.value = ''
  } else {
    openDropdown.value = name
  }
}

watch(() => route.path, (p) => {
  for (const m of menus) {
    if (p.startsWith('/' + m.name)) {
      if (openDropdown.value !== m.name) {
        openDropdown.value = m.name
      }
      return
    }
  }
  openDropdown.value = ''
}, { immediate: true })
</script>

<style scoped>
.sidebar { border-right: 1px solid rgba(255,255,255,.04); box-shadow: 3px 0 16px rgba(0,0,0,.2); }
@media (max-width: 768px) { .sidebar { display: none; } }
</style>
