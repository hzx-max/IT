<template>
  <div class="flex min-h-screen">
    <!-- 侧边栏 -->
    <Sidebar />
    <!-- 主内容 -->
    <div class="flex-1 ml-[240px] min-h-screen">
      <div class="p-7 page-enter-active">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
import Sidebar from '../components/Sidebar.vue'
</script>

<style scoped>
@media (max-width: 768px) {
  .ml-\[240px\] { margin-left: 0; }
  .p-7 { padding: 16px; }
}
</style>
