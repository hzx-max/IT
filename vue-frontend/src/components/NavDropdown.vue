<template>
  <div class="nav-dropdown">
    <button class="nav-dd-toggle" :class="{ active: open && !hasActiveChild }" @click="$emit('toggle')">
      <span class="nav-icon-abs">
        <svg v-if="icon==='cmd'" width="20" height="20" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="2" width="5" height="5" rx="1"/><rect x="9" y="2" width="5" height="5" rx="1"/><rect x="2" y="9" width="5" height="5" rx="1"/><rect x="9" y="9" width="5" height="5" rx="1"/></svg>
        <svg v-else-if="icon==='fault'" width="20" height="20" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M8 1v14M1 8h14"/><circle cx="8" cy="8" r="2.5"/></svg>
        <svg v-else-if="icon==='desktop'" width="20" height="20" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="2" width="12" height="12" rx="2"/><path d="M5 6h6M5 8h4M5 10h5"/></svg>
        <svg v-else-if="icon==='linux'" width="20" height="20" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 3h8l1 3-3 4 3 4-1 3H4l-1-3 3-4-3-4Z"/></svg>
        <svg v-else-if="icon==='ai'" width="20" height="20" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M8 1l2 5h5l-4 3 1.5 5L8 11l-4.5 3L5 9l-4-3h5l2-5z"/><circle cx="8" cy="4" r="1.2" fill="currentColor" stroke="none"/></svg>
        <svg v-else width="20" height="20" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 2h8l2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Z"/><path d="M8 2v5l2-1 2 1V2"/></svg>
      </span>
      <span class="nav-text">{{ label }}</span>
      <svg class="nav-dd-arrow" width="14" height="14" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 6l4 4 4-4"/></svg>
    </button>
    <div class="nav-dd-menu" :class="{ open: open }">
      <router-link v-for="item in items" :key="item.path" :to="item.path"
        class="nav-btn" :class="{ active: isActive(item.path) }" @click="$emit('navigate')">
        <span class="nav-text">{{ item.label }}</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
const props = defineProps({ icon: String, label: String, items: Array, open: Boolean })
const emit = defineEmits(['toggle', 'navigate'])
const route = useRoute()
const hasActiveChild = computed(() => props.items.some(item => {
  if (item.path === '/cmd' && route.path.startsWith('/cmd')) return route.path === '/cmd'
  return route.path === item.path
}))
const isActive = (path) => {
  if (path === '/cmd' && route.path.startsWith('/cmd')) return route.path === '/cmd'
  return route.path === path
}
</script>

<style scoped>
.nav-dropdown{overflow:hidden}
.nav-text{padding-left:34px;display:block}

.nav-dd-toggle{
  display:flex;align-items:center;width:calc(100% - 16px);margin:0 8px;padding:10px 14px;border:none;background:transparent;
  color:#000;font-size:16px;font-family:'Microsoft YaHei',sans-serif;font-weight:500;cursor:pointer;text-align:left;transition:all .2s ease;
  border-radius:6px;box-sizing:border-box;position:relative
}
.nav-icon-abs{position:absolute;left:14px;top:50%;transform:translateY(-50%);display:flex;align-items:center;pointer-events:none}
.nav-dd-toggle:hover{color:#000;background:#f5f5f5}
.nav-dd-toggle.active{color:#000;font-weight:500;background:transparent}
.nav-dd-arrow{position:absolute;right:14px;top:50%;transform:translateY(-50%);transition:transform .25s ease;color:#000;pointer-events:none}
.nav-dd-toggle.active .nav-dd-arrow{transform:translateY(-50%) rotate(180deg)}

.nav-dd-menu{
  max-height:0;overflow:hidden;transition:max-height .3s ease;
  padding:0 8px
}
.nav-dd-menu.open{max-height:300px}

.nav-btn{
  display:flex;align-items:center;width:calc(100% - 16px);margin:0 8px;padding:9px 14px;border:none;background:transparent;
  color:#000;font-size:16px;font-family:'Microsoft YaHei',sans-serif;font-weight:500;cursor:pointer;text-align:left;text-decoration:none;transition:all .15s ease;
  border-radius:6px;position:relative;box-sizing:border-box
}
.nav-btn:hover{background:#2563eb;color:#fff;border-radius:6px}
.nav-btn.active{background:#2563eb;color:#fff;font-weight:500;border-radius:6px}
</style>
