<template>
  <div class="nav-dropdown">
    <button class="nav-dd-toggle" :class="{ active: open }" @click="$emit('toggle')">
      <!-- SVG icons per module -->
      <svg v-if="icon==='cmd'" width="24" height="24" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="2" width="5" height="5" rx="1"/><rect x="9" y="2" width="5" height="5" rx="1"/><rect x="2" y="9" width="5" height="5" rx="1"/><rect x="9" y="9" width="5" height="5" rx="1"/></svg>
      <svg v-else-if="icon==='fault'" width="24" height="24" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M8 1v14M1 8h14"/><circle cx="8" cy="8" r="2.5"/><path d="M3 3l2 2M11 3l2 2M3 13l2-2M11 13l2-2" opacity="0.5"/></svg>
      <svg v-else-if="icon==='desktop'" width="24" height="24" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="2" width="12" height="12" rx="2"/><path d="M5 6h6M5 8h4M5 10h5"/></svg>
      <svg v-else-if="icon==='linux'" width="24" height="24" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 3h8l1 3-3 4 3 4-1 3H4l-1-3 3-4-3-4Z"/></svg>
      <svg v-else-if="icon==='ai'" width="24" height="24" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M8 1l2 5h5l-4 3 1.5 5L8 11l-4.5 3L5 9l-4-3h5l2-5z"/><circle cx="8" cy="4" r="1.2" fill="currentColor" stroke="none"/></svg>
      <svg v-else width="24" height="24" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 2h8l2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Z"/><path d="M8 2v5l2-1 2 1V2"/></svg>
      <span>{{ label }}</span>
      <svg class="nav-dd-arrow ml-auto" width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 6l4 4 4-4"/></svg>
    </button>
    <div class="nav-dd-menu" :class="{ open: open }">
      <router-link v-for="item in items" :key="item.path" :to="item.path"
        class="nav-btn" :class="{ active: isActive(item.path) }">
        <span>{{ item.label }}</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
defineProps({ icon: String, label: String, items: Array, open: Boolean })
const emit = defineEmits(['toggle'])
const route = useRoute()
const isActive = (path) => {
  if (path === '/cmd' && route.path.startsWith('/cmd')) return route.path === '/cmd'
  return route.path === path
}
</script>

<style scoped>
.nav-dd-toggle{
  display:flex;align-items:center;gap:10px;width:100%;padding:10px 22px;border:none;background:none;
  color:#475569;font-size:15px;cursor:pointer;text-align:left;transition:all .25s ease;font-weight:600;letter-spacing:.3px
}
.nav-dd-toggle:hover{color:#1e293b;background:rgba(37,99,235,.06)}
.nav-dd-toggle.active{color:#f97316}
.nav-dd-toggle svg{flex-shrink:0;opacity:.7;transition:all .15s ease}
.nav-dd-toggle:hover svg,.nav-dd-toggle.active svg{opacity:1}
.nav-dd-arrow{transition:transform .25s ease}
.nav-dd-toggle.active .nav-dd-arrow{transform:rotate(180deg)}
.nav-dd-menu{max-height:0;overflow:hidden;transition:max-height .3s ease}
.nav-dd-menu.open{max-height:300px}
.nav-btn{
  display:flex;align-items:center;gap:12px;width:100%;padding:10px 22px;border:none;background:none;
  color:#64748b;font-size:15px;cursor:pointer;text-align:left;text-decoration:none;transition:all .25s ease;
  border-left:3px solid transparent;position:relative;font-weight:500
}
.nav-btn:hover{background:rgba(37,99,235,.08);color:#334155;border-left-color:rgba(37,99,235,.5)}
.nav-btn.active{background:rgba(37,99,235,.12);color:#1e293b;border-left-color:#2563eb;font-weight:600}
.nav-btn.active::after{content:'';position:absolute;right:0;top:50%;transform:translateY(-50%);width:3px;height:20px;background:#2563eb;border-radius:3px 0 0 3px;box-shadow:0 0 8px rgba(37,99,235,.5)}
</style>
