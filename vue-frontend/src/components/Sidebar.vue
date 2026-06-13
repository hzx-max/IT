<template>
  <nav class="sidebar" ref="sidebarEl"
       :class="{ 'sidebar-drawer': mobileOpen, 'sidebar-collapsed': collapsed, 'sidebar-right': position === 'right', 'sidebar-dragging': isDragging }"
       :style="dragStyle">
    <div class="sidebar-drag-handle" @mousedown.prevent="onDragStart">
      <div class="drag-indicator"></div>
    </div>
    <div class="sidebar-brand-wrap">
      <router-link to="/" class="sidebar-brand">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="mr-2.5 shrink-0">
          <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
          <line x1="8" y1="21" x2="16" y2="21"/>
          <line x1="12" y1="17" x2="12" y2="21"/>
        </svg>
        <span>IT运维学习平台</span>
      </router-link>
      <button class="sidebar-collapse-btn" @click.stop="toggleCollapse" title="收起侧边栏">
        <svg v-if="position==='right'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18l6-6-6-6"/></svg>
        <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 18l-6-6 6-6"/></svg>
      </button>
    </div>

    <div class="flex-1 overflow-y-auto overflow-x-hidden py-2">
      <NavDropdown v-for="m in filteredMenus" :key="m.name" :open="openDropdown===m.name" :icon="m.icon" :label="m.label" :items="m.items" @toggle="onToggle(m.name)" @navigate="onNavigate" />
    </div>

    <div class="sidebar-profile">
      <router-link to="/profile" class="nav-profile-link" @click="emit('closeMobile')">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
          <circle cx="12" cy="7" r="4"/>
        </svg>
        <span>个人中心</span>
      </router-link>
    </div>
    <div class="sidebar-shortcuts">
      <kbd>ESC</kbd> 返回
      <kbd>Ctrl+K</kbd> 搜索
    </div>
  </nav>
  <button v-show="collapsed" class="sidebar-expand-btn" :class="{ 'sidebar-expand-btn-right': position === 'right' }" @click="toggleCollapse" title="展开侧边栏">
    <svg v-if="position==='right'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 18l-6-6 6-6"/></svg>
    <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18l6-6-6-6"/></svg>
  </button>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import NavDropdown from './NavDropdown.vue'
import { useSidebarCollapse, useSidebarPosition, setSidebarPosition } from '../composables/useSidebarCollapse.js'

const props = defineProps({
  mobileOpen: { type: Boolean, default: false }
})
const emit = defineEmits(['closeMobile', 'toggleCollapse', 'positionChange'])

const route = useRoute()
const openDropdown = ref('')
const loggedIn = ref('')
const role = ref('')
const collapsed = ref(false)
const position = useSidebarPosition()
const sidebarEl = ref(null)

const isAdmin = computed(() => loggedIn.value && (role.value === 'SUPER_ADMIN' || role.value === 'ADMIN'))

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

// ─── 拖拽状态 ───
const isDragging = ref(false)
const dragOffsetX = ref(0)
const dragStartX = ref(0)
const dragThreshold = 120

const dragStyle = computed(() => {
  if (!isDragging.value) return {}
  return { transform: `translateX(${dragOffsetX.value}px)`, transition: 'none' }
})

function onDragStart(e) {
  if (collapsed.value) return
  isDragging.value = true
  dragStartX.value = e.clientX
  dragOffsetX.value = 0
  document.addEventListener('mousemove', onDragMove)
  document.addEventListener('mouseup', onDragEnd)
  document.body.style.userSelect = 'none'
  document.body.style.cursor = 'grabbing'
}

function onDragMove(e) {
  if (!isDragging.value) return
  const dx = e.clientX - dragStartX.value
  dragOffsetX.value = dx
}

function onDragEnd(e) {
  if (!isDragging.value) return
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
  document.body.style.userSelect = ''
  document.body.style.cursor = ''

  const dx = e.clientX - dragStartX.value

  if (position.value === 'left') {
    if (dx < -dragThreshold) {
      collapsed.value = true
      localStorage.setItem('sidebarCollapsed', '1')
      emit('toggleCollapse', true)
    } else if (dx > dragThreshold) {
      setSidebarPosition('right')
      emit('positionChange', 'right')
    }
  } else {
    if (dx > dragThreshold) {
      collapsed.value = true
      localStorage.setItem('sidebarCollapsed', '1')
      emit('toggleCollapse', true)
    } else if (dx < -dragThreshold) {
      setSidebarPosition('left')
      emit('positionChange', 'left')
    }
  }

  isDragging.value = false
  dragOffsetX.value = 0
}

function toggleCollapse() {
  collapsed.value = !collapsed.value
  localStorage.setItem('sidebarCollapsed', collapsed.value ? '1' : '')
  emit('toggleCollapse', collapsed.value)
}

function updateAuthState() {
  const token = localStorage.getItem('token')
  loggedIn.value = !!token
  role.value = localStorage.getItem('role') || ''
}

function onToggle(name) {
  openDropdown.value = openDropdown.value === name ? '' : name
}

function onNavigate() {
  emit('closeMobile')
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
  collapsed.value = localStorage.getItem('sidebarCollapsed') === '1'
  emit('toggleCollapse', collapsed.value)
})

onUnmounted(() => {
  window.removeEventListener('storage', onStorageChange)
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
})
</script>

<style scoped>
.sidebar {
  --sidebar-deep-blue:#2563eb;
  position: fixed;
  top: 12px;
  left: 12px;
  width: 270px;
  height: calc((100vh - 24px) * .7);
  z-index: 50;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 0.5px solid #e5e7eb;
  box-shadow: none;
  border-radius: 16px;
  transition: transform .35s cubic-bezier(.4,0,.2,1);
  overflow: hidden;
  font-family: 'Microsoft YaHei', sans-serif;
}
.sidebar-right {
  left: auto;
  right: 12px;
}
.sidebar-collapsed {
  transform: translateX(calc(-100% - 24px));
}
.sidebar-right.sidebar-collapsed {
  transform: translateX(calc(100% + 24px));
}
.sidebar-dragging {
  transition: none !important;
  box-shadow: 0 20px 60px rgba(0,0,0,.15);
  z-index: 999;
}

.sidebar-brand-wrap{
  display:flex;align-items:center;padding:16px 16px;
  background:var(--sidebar-deep-blue);border-bottom:none
}
.sidebar-brand{
  display:flex;align-items:center;color:#fff;text-decoration:none;flex:1;min-width:0
}
.sidebar-brand span{font-size:16px;font-weight:500;letter-spacing:0;white-space:nowrap;color:#fff}
.sidebar-collapse-btn{
  margin-left:auto;display:flex;align-items:center;justify-content:center;width:26px;height:26px;
  border:0.5px solid rgba(255,255,255,.22);background:var(--sidebar-deep-blue);border-radius:6px;color:#fff;cursor:pointer;
  transition:all .2s;flex-shrink:0
}
.sidebar-collapse-btn:hover{background:#1d4ed8;color:#fff}

.sidebar-drag-handle{
  display:flex;align-items:center;justify-content:center;
  height:14px;margin-top:0;cursor:grab;user-select:none;flex-shrink:0;
  background:var(--sidebar-deep-blue);
}
.sidebar-drag-handle:hover{background:#1d4ed8}
.sidebar-drag-handle:active{cursor:grabbing}
.drag-indicator{
  width:24px;height:3px;border-radius:2px;background:rgba(255,255,255,.5);
}
.sidebar-drag-handle:hover .drag-indicator{background:rgba(255,255,255,.75)}

.sidebar-profile{padding:0 8px;text-align:left;background:var(--sidebar-deep-blue)}
.sidebar-shortcuts{
  margin-top:auto;padding:8px 14px;color:#fff;text-align:left;
  font-size:11px;font-family:'Microsoft YaHei',sans-serif;font-weight:400;line-height:1.6;flex-shrink:0;
  background:var(--sidebar-deep-blue);border-top:0.5px solid rgba(255,255,255,.16)
}
.sidebar-shortcuts kbd{
  display:inline-block;padding:1px 6px;margin:0 3px;border-radius:3px;border:0.5px solid rgba(255,255,255,.32);
  background:rgba(255,255,255,.12);color:#fff;font-size:10px;font-weight:600;line-height:1.2
}

.sidebar-expand-btn{
  position:fixed;top:calc(12px + ((100vh - 24px) * .35));left:12px;z-index:49;transform:translateY(-50%);
  width:28px;height:44px;display:flex;align-items:center;justify-content:center;
  background:#2563eb;border:0.5px solid #2563eb;
  border-radius:0 8px 8px 0;color:#fff;cursor:pointer;
  box-shadow:none;transition:all .2s
}
.sidebar-expand-btn-right{
  left:auto;right:12px;
  border-radius:8px 0 0 8px;
}
.sidebar-expand-btn:hover{background:#1d4ed8;color:#fff}

.nav-profile-link {
  display:flex;align-items:center;gap:8px;padding:10px 14px;border-radius:0;
  color:#fff;font-size:13px;font-family:'Microsoft YaHei',sans-serif;font-weight:500;text-decoration:none;transition:all .2s ease;
  border-bottom:0.5px solid rgba(255,255,255,.16)
}
.nav-profile-link:hover{color:#fff;background:#1d4ed8}
.nav-profile-link.router-link-active{color:#fff;background:#1d4ed8}

@media (max-width: 768px) {
  .sidebar { transform: translateX(-100%); top: 0; left: 0; border-radius: 0; height: 100vh; }
  .sidebar-right { left: 0; right: auto; }
  .sidebar.sidebar-drawer { transform: translateX(0); }
  .sidebar-collapsed { transform: translateX(-100%); }
  .sidebar-expand-btn { display: none; }
  .sidebar-drag-handle { display: none; }
}
</style>
