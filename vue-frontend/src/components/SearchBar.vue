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
    <select v-if="showVendor" :value="vendorValue" @change="$emit('update:vendorValue', $event.target.value)"
      class="search-select">
      <option value="">全部厂商</option>
      <option v-for="(v,k) in vendorMap" :key="k" :value="k">{{ v.n }}</option>
    </select>
    <select v-if="showCat" :value="catValue" @change="$emit('update:catValue', $event.target.value)"
      class="search-select">
      <option value="">全部分类</option>
      <option v-for="(l,k) in catMap" :key="k" :value="k">{{ k === l ? l : k + ' - ' + l }}</option>
    </select>
    <button class="search-btn" @click="onSearchClick">搜索</button>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
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

async function loadHistory() {
  if (!props.module) return
  try {
    const res = await apiSearchHistory.list(props.module)
    historyList.value = res.data.data || []
  } catch { historyList.value = [] }
}

async function onEnter(e) {
  const val = e.target.value.trim()
  emit('enter', val)
  showHistory.value = false
}

async function onSearchClick() {
  const val = (props.modelValue || '').trim()
  emit('enter', val)
  if (val && props.module) {
    try { await apiSearchHistory.save(props.module, val) } catch {}
    await loadHistory()
  }
  showHistory.value = false
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
  width:100%;padding:11px 14px;border:1.5px solid #e2e8f0;border-radius:8px;
  font-size:14px;outline:none;background:#fff;transition:all .2s ease
}
.search-input:hover{border-color:#cbd5e1}
.search-input:focus{border-color:#2563eb;box-shadow:0 0 0 3px rgba(37,99,235,.12)}
.history-dropdown{
  position:absolute;top:100%;left:0;right:0;z-index:50;
  background:#fff;border:1.5px solid #e2e8f0;border-radius:8px;
  box-shadow:0 8px 24px rgba(0,0,0,.12);margin-top:4px;overflow:hidden
}
.history-header{display:flex;justify-content:space-between;align-items:center;padding:8px 14px;border-bottom:1px solid #f1f5f9;font-size:13px;color:#64748b;font-weight:600}
.history-clear{border:none;background:none;color:#2563eb;font-size:12px;cursor:pointer;font-weight:500}
.history-clear:hover{color:#1d4ed8;text-decoration:underline}
.history-item{display:flex;align-items:center;gap:8px;padding:8px 14px;cursor:pointer;transition:background .15s ease;font-size:13px}
.history-item:hover{background:#f8fafc}
.history-keyword{flex:1;color:#334155;font-weight:500;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.history-time{color:#94a3b8;font-size:11px;white-space:nowrap}
.history-del{border:none;background:none;color:#cbd5e1;font-size:16px;cursor:pointer;padding:0 2px;line-height:1;transition:color .15s ease}
.history-del:hover{color:#ef4444}
.search-select{
  padding:11px 32px 11px 14px;border:1.5px solid #e2e8f0;border-radius:8px;
  font-size:14px;background:#fff;outline:none;cursor:pointer;transition:all .2s ease;
  appearance:none;min-width:150px;
  background-image:url("data:image/svg+xml,%3Csvg width='12' height='8' viewBox='0 0 12 8' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M2 2l4 4 4-4' stroke='%2364748b' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat:no-repeat;background-position:right 12px center
}
.search-select:hover{border-color:#cbd5e1}
.search-select:focus{border-color:#2563eb;box-shadow:0 0 0 3px rgba(37,99,235,.12)}
.search-btn{
  padding:11px 20px;border:1.5px solid #2563eb;border-radius:8px;
  font-size:14px;font-weight:500;background:#2563eb;color:#fff;
  outline:none;cursor:pointer;transition:all .2s ease;white-space:nowrap
}
.search-btn:hover{background:#1d4ed8;border-color:#1d4ed8}
.search-btn:active{background:#1e40af;border-color:#1e40af}
</style>
