<template>
  <button
    class="favorite-btn"
    :class="{ active: active }"
    type="button"
    :title="active ? '取消收藏' : '收藏'"
    @click.stop="toggle"
  >
    <svg width="18" height="18" viewBox="0 0 24 24" :fill="active ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
    </svg>
    <span v-if="showText">{{ active ? '已收藏' : '收藏' }}</span>
  </button>
</template>

<script setup>
import { ref, watch } from 'vue'
import { isFavorite, toggleFavorite } from '../utils/userLibrary.js'

const props = defineProps({
  module: { type: String, required: true },
  item: { type: Object, default: null },
  showText: { type: Boolean, default: false }
})

const active = ref(false)

async function sync() {
  active.value = !!props.item?.id && await isFavorite(props.module, props.item.id)
}

async function toggle() {
  if (!props.item?.id) return
  active.value = await toggleFavorite(props.module, props.item)
}

watch(() => [props.module, props.item?.id], sync, { immediate: true })
</script>

<style scoped>
.favorite-btn{
  display:inline-flex;align-items:center;justify-content:center;gap:6px;
  min-width:36px;padding:9px 22px;border:1.5px solid #dbe3ef;border-radius:8px;
  background:#fff;color:#64748b;cursor:pointer;transition:all .2s ease;font-size:14px;font-weight:500;
  line-height:1.2;box-sizing:border-box
}
.favorite-btn:hover{border-color:#2563eb;color:#2563eb;box-shadow:0 4px 12px rgba(37,99,235,.12)}
.favorite-btn.active{background:#2563eb;border-color:#2563eb;color:#fff}
.favorite-btn.active:hover{background:#1d4ed8;border-color:#1d4ed8;color:#fff}
.favorite-btn svg{flex-shrink:0}
</style>
