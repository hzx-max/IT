<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏（PC=固定，移动端=抽屉）详情页不显示 -->
    <Sidebar v-if="!isDetailPage" ref="sidebarRef" :mobile-open="sidebarOpen" @close-mobile="closeSidebar" @toggle-collapse="onCollapseChange" @position-change="onPositionChange" />

    <!-- 移动端汉堡按钮（≤768px 显示） -->
    <button v-if="sidebarOpen" class="sidebar-overlay" @click="closeSidebar" aria-label="关闭菜单"></button>

    <!-- 主内容 -->
    <div class="flex-1 min-h-screen main-content"
         :style="{ marginLeft: isDetailPage ? '0' : (sidebarCollapsed ? '0' : (sidebarPos === 'left' ? '290px' : '0')), marginRight: isDetailPage ? '0' : (sidebarCollapsed ? '0' : (sidebarPos === 'right' ? '290px' : '0')), transition: 'margin .35s cubic-bezier(.4,0,.2,1)' }">
      <!-- 移动端顶部条 -->
      <div class="mobile-topbar">
        <button class="hamburger" :class="{ open: sidebarOpen }" @click="toggleSidebar" aria-label="菜单">
          <span></span><span></span><span></span>
        </button>
        <span class="mobile-topbar-title">IT运维学习平台</span>
        <div class="mobile-topbar-spacer"></div>
      </div>
      <!-- 页面内容 -->
      <div class="page-enter-active" :class="pageLayoutClass" :style="sidebarOpen ? { overflow: 'hidden' } : {}">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'
import { useSidebarCollapse, useSidebarPosition } from '../composables/useSidebarCollapse.js'

const route = useRoute()
const sidebarOpen = ref(false)
const sidebarCollapsed = useSidebarCollapse()
const sidebarPos = useSidebarPosition()
const pageLayoutClass = computed(() => {
  const name = String(route.name || '')
  if (name.endsWith('-add')) return 'page-layout-add'
  if (name.endsWith('-list') || name.endsWith('-admin')) return 'page-layout-wide'
  if (name.endsWith('-detail')) return 'page-layout-detail'
  return ''
})
const isDetailPage = computed(() => String(route.name || '').endsWith('-detail'))
function toggleSidebar() { sidebarOpen.value = !sidebarOpen.value }
function closeSidebar() { sidebarOpen.value = false }
function onCollapseChange(val) { sidebarCollapsed.value = val }
function onPositionChange(pos) { sidebarPos.value = pos }

// 路由切换自动关闭移动端菜单
watch(() => route.path, () => { sidebarOpen.value = false })

// 进入详情页自动收起侧边栏
watch(() => route.name, (name) => {
  if (String(name || '').endsWith('-detail')) {
    sidebarCollapsed.value = true
  }
})

// ESC 关闭
function onKeydown(e) {
  if (e.key === 'Escape' && sidebarOpen.value) sidebarOpen.value = false
}
onMounted(() => {
  document.addEventListener('keydown', onKeydown)
})
onBeforeUnmount(() => document.removeEventListener('keydown', onKeydown))
</script>

<style scoped>
.mobile-topbar {
  display: none;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 40;
  padding-top: env(safe-area-inset-top, 0px);
  height: calc(56px + env(safe-area-inset-top, 0px));
}
.mobile-topbar-title {
  font-size: 1rem;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.3px;
  background: #2563eb;
  padding: 4px 12px;
  border-radius: 6px;
}
.mobile-topbar-spacer { width: 40px; }

.hamburger {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  width: 40px; height: 40px;
  background: none; border: none;
  cursor: pointer; padding: 0;
}
.hamburger span {
  display: block;
  height: 2.5px;
  background: #000;
  border-radius: 2px;
  transition: transform .25s ease, opacity .15s ease;
}
.hamburger.open span:nth-child(1) { transform: translateY(7.5px) rotate(45deg); }
.hamburger.open span:nth-child(2) { opacity: 0; }
.hamburger.open span:nth-child(3) { transform: translateY(-7.5px) rotate(-45deg); }

.sidebar-overlay {
  display: none;
  position: fixed; inset: 0;
  background: rgba(0,0,0,.4);
  z-index: 45;
  border: none; cursor: pointer;
}

.page-enter-active {
  width: 70%;
  margin-left: 15%;
  margin-right: 15%;
  padding: 28px 0;
}

.page-enter-active.page-layout-add {
  width: 60%;
  margin-left: 20%;
  margin-right: 20%;
}

.page-enter-active.page-layout-wide {
  width: 80%;
  margin-left: 10%;
  margin-right: 10%;
}

.page-enter-active.page-layout-detail {
  width: 90%;
  margin-left: 5%;
  margin-right: 5%;
  padding: 28px 0;
}

@media (max-width: 768px) {
  .page-enter-active {
    width: auto;
    margin-left: 0;
    margin-right: 0;
    padding: 16px;
  }
  .ml-\[240px\] { margin-left: 0; }
  .p-7 { padding: 16px; }
  .mobile-topbar { display: flex; }
  .sidebar-overlay { display: block; }
}
</style>
