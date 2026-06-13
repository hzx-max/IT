<template>
  <div class="flex gap-2.5 mb-[18px] flex-wrap">
    <div class="search-wrap" ref="wrapRef">
      <input type="text" :placeholder="placeholder" :value="modelValue"
             class="search-input"
             @input="$emit('update:modelValue', $event.target.value)"
             @focus="onFocus"
             @click="onFocus"
             @keydown.enter="onEnter"
             data-search-focus>
      <button class="search-icon-btn" type="button" @click="onSearchClick" aria-label="搜索">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="7"></circle>
          <path d="m20 20-3.5-3.5"></path>
        </svg>
      </button>
      <div v-if="showHistory && historyList.length > 0" class="history-dropdown">
        <div class="history-header">
          <span>搜索历史</span>
          <button class="history-clear" @click="onClear">清空</button>
        </div>
        <div v-for="h in historyList" :key="h.id" class="history-item" @mousedown.prevent="onSelect(h.keyword)">
          <span class="history-keyword">{{ h.keyword }}</span>
          <span class="history-time">{{ h.searchedAt }}</span>
          <button class="history-del" @mousedown.prevent.stop="onDelete(h.id)">×</button>
        </div>
      </div>
    </div>
    <DropdownSelect
      v-if="showVendor"
      class="search-select"
      :model-value="vendorValue"
      :options="vendorOptions"
      placeholder="全部厂商"
      @update:model-value="$emit('update:vendorValue', $event)"
    />
    <DropdownSelect
      v-if="showCat"
      class="search-select"
      :model-value="catValue"
      :options="catOptions"
      placeholder="全部分类"
      @update:model-value="$emit('update:catValue', $event)"
    />
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import DropdownSelect from './DropdownSelect.vue'
import { apiSearchHistory } from '../api/index.js'

const props = defineProps({
  modelValue: String, placeholder: { type: String, default: '搜索...' },
  showVendor: Boolean, showCat: Boolean,
  vendorValue: String, catValue: String,
  vendorMap: { type: Object, default: () => ({}) },
  catMap: { type: Object, default: () => ({}) },
  module: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue', 'update:vendorValue', 'update:catValue', 'enter'])

const showHistory = ref(false)
const historyList = ref([])
const wrapRef = ref(null)

const vendorOptions = computed(() => [
  { value: '', label: '全部厂商' },
  ...Object.entries(props.vendorMap || {}).map(([k, v]) => ({ value: k, label: v.n || k }))
])

const catOptions = computed(() => [
  { value: '', label: '全部分类' },
  ...Object.entries(props.catMap || {}).map(([k, l]) => ({ value: k, label: k === l ? l : k + ' - ' + l }))
])

async function loadHistory() {
  if (!props.module) return
  try {
    const res = await apiSearchHistory.list(props.module)
    historyList.value = res.data.data || []
  } catch { historyList.value = [] }
}

async function performSearch(val) {
  const keyword = (val || '').trim()
  emit('enter', keyword)
  if (keyword && props.module) {
    try { await apiSearchHistory.save(props.module, keyword) } catch {}
    await loadHistory()
  }
  showHistory.value = false
}

async function onEnter(e) {
  await performSearch(e.target.value)
}

async function onSearchClick() {
  await performSearch(props.modelValue)
}

function onSelect(keyword) {
  emit('update:modelValue', keyword)
  showHistory.value = false
}

async function onDelete(id) {
  try { await apiSearchHistory.delete(props.module, id) } catch {}
  await loadHistory()
}

async function onClear() {
  try { await apiSearchHistory.clear(props.module) } catch {}
  historyList.value = []
}

function onClickOutside(e) {
  if (wrapRef.value && !wrapRef.value.contains(e.target)) {
    showHistory.value = false
  }
}

function onFocus() {
  showHistory.value = true
  loadHistory()
}

onMounted(() => {
  loadHistory()
  document.addEventListener('click', onClickOutside)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', onClickOutside)
})
</script>

<style scoped>
.search-wrap{position:relative;flex:1;min-width:200px}
.search-input{
  width:100%;padding:10px 48px 10px 14px;border:1.5px solid #d1d5db;border-radius:8px;
  font-size:16px;outline:none;background:#fff;transition:all .2s ease;height:55px;box-sizing:border-box
}
.search-input:hover{border-color:#999}
.search-input:focus{border-color:#000;box-shadow:0 0 0 3px rgba(0,0,0,.08)}
.search-icon-btn{
  position:absolute;right:6px;top:50%;transform:translateY(-50%);
  width:45px;height:45px;border:none;border-radius:7px;
  display:inline-flex;align-items:center;justify-content:center;
  background:#2563eb;color:#fff;cursor:pointer;transition:all .18s ease
}
.search-icon-btn:hover{background:#1d4ed8;box-shadow:0 4px 12px rgba(37,99,235,.18)}
.search-icon-btn:active{background:#1e40af;transform:translateY(-50%) scale(.98)}
.history-dropdown{
  position:absolute;top:100%;left:0;right:0;z-index:50;
  background:#fff;border:1.5px solid #d1d5db;border-radius:8px;
  box-shadow:0 8px 24px rgba(0,0,0,.12);margin-top:4px;overflow:hidden
}
.history-header{display:flex;justify-content:space-between;align-items:center;padding:8px 14px;border-bottom:1px solid #f5f5f5;font-size:13px;color:#999;font-weight:600}
.history-clear{border:none;background:none;color:#000;font-size:12px;cursor:pointer;font-weight:500}
.history-clear:hover{text-decoration:underline}
.history-item{display:flex;align-items:center;gap:8px;padding:8px 14px;cursor:pointer;transition:background .15s ease;font-size:13px}
.history-item:hover{background:#eff6ff}
.history-keyword{flex:1;color:#000;font-weight:500;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.history-time{color:#999;font-size:11px;white-space:nowrap}
.history-del{border:none;background:none;color:#d1d5db;font-size:16px;cursor:pointer;padding:0 2px;line-height:1;transition:color .15s ease}
.history-del:hover{color:#1e40af}
.search-select{width:170px;min-width:170px;flex:0 0 170px}
.search-select:hover{outline:1.5px solid #1e40af}
.search-select:focus{outline:1.5px solid #1e40af}
/* 移动端搜索栏 */
@media (max-width: 640px) {
  .search-wrap { min-width: 100%; }
  .search-select { min-width: auto; flex: 1; }
}
</style>
