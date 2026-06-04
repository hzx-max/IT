<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏（PC=固定，移动端=抽屉） -->
    <Sidebar ref="sidebarRef" :mobile-open="sidebarOpen" @close-mobile="closeSidebar" />

    <!-- 移动端汉堡按钮（≤768px 显示） -->
    <button v-if="sidebarOpen" class="sidebar-overlay" @click="closeSidebar" aria-label="关闭菜单"></button>

    <!-- 主内容 -->
    <div class="flex-1 ml-[240px] min-h-screen">
      <!-- 移动端顶部条 -->
      <div class="mobile-topbar">
        <button class="hamburger" :class="{ open: sidebarOpen }" @click="toggleSidebar" aria-label="菜单">
          <span></span><span></span><span></span>
        </button>
        <span class="mobile-topbar-title">IT运维学习平台</span>
        <div class="mobile-topbar-spacer"></div>
      </div>
      <!-- 页面内容 -->
      <div class="p-7 page-enter-active" :style="sidebarOpen ? { overflow: 'hidden' } : {}">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'

const route = useRoute()
const sidebarOpen = ref(false)

function toggleSidebar() { sidebarOpen.value = !sidebarOpen.value }
function closeSidebar() { sidebarOpen.value = false }

// 路由切换自动关闭移动端菜单
watch(() => route.path, () => { sidebarOpen.value = false })

// ESC 关闭
function onKeydown(e) {
  if (e.key === 'Escape' && sidebarOpen.value) sidebarOpen.value = false
}
onMounted(() => document.addEventListener('keydown', onKeydown))
onBeforeUnmount(() => document.removeEventListener('keydown', onKeydown))
</script>

<style scoped>
/* ── 移动端顶部条（默认隐藏，≤768px 显示）── */
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
  color: var(--text);
  letter-spacing: -0.3px;
}
.mobile-topbar-spacer { width: 40px; }

/* ── 汉堡按钮 ── */
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
  background: #334155;
  border-radius: 2px;
  transition: transform .25s ease, opacity .15s ease;
}
.hamburger.open span:nth-child(1) { transform: translateY(7.5px) rotate(45deg); }
.hamburger.open span:nth-child(2) { opacity: 0; }
.hamburger.open span:nth-child(3) { transform: translateY(-7.5px) rotate(-45deg); }

/* ── 覆盖层 ── */
.sidebar-overlay {
  display: none;
  position: fixed; inset: 0;
  background: rgba(15,23,42,.4);
  z-index: 45;
  border: none; cursor: pointer;
}

/* ── 移动端适配（≤768px）── */
@media (max-width: 768px) {
  .ml-\[240px\] { margin-left: 0; }
  .p-7 { padding: 16px; }
  .mobile-topbar { display: flex; }
  .sidebar-overlay { display: block; }
}
</style>
