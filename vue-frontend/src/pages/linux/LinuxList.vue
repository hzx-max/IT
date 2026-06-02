<template>
  <MainLayout>
    <div class="top-bar">
      <h2>Linux 学习列表</h2>
      <span class="text-sm text-slate-500 font-medium">共{{ filtered.length }}条</span>
    </div>

    <SearchBar v-model="search" placeholder="搜索 Linux 内容..." :showVendor="true" :showCat="true"
      module="linux"
      v-model:vendorValue="vendorFilter" v-model:catValue="catFilter"
      :vendorMap="LINUX_VENDOR_MAP" :catMap="dynCatMap" />

    <CategoryStrip :cats="allCats" :active="activeCat" @select="onCatSelect" />

    <div v-if="filtered.length === 0" class="text-center py-[60px] text-slate-400 text-base">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="mx-auto mb-4 opacity-40"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg><br>没有匹配的结果
    </div>

    <div class="cmd-grid">
      <div v-for="item in filtered" :key="item.id" class="cmd-card" @click="handleClick(item)">
        <div class="cmd-card-body">
          <h3>{{ item.title }}</h3>
          <div class="mt-1.5 flex gap-1.5 flex-wrap">
            <span v-if="item.vendor" class="tag-vendor"
              :style="{ background: getVendorColor(item.vendor, LINUX_VENDOR_MAP)+'15', color: getVendorColor(item.vendor, LINUX_VENDOR_MAP) }">
              {{ getVendorName(item.vendor, LINUX_VENDOR_MAP) }}
            </span>
            <span v-if="item.cat" class="tag-cat">{{ getCatLabel(item.cat, LINUX_CAT_MAP) }}</span>
          </div>
          <div class="mt-auto pt-2 flex justify-start">
            <span v-if="item.createdAt" class="tag-time">{{ formatTime(item.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MainLayout from '../../layouts/MainLayout.vue'
import SearchBar from '../../components/SearchBar.vue'
import CategoryStrip from '../../components/CategoryStrip.vue'
import { apiLinux, LINUX_VENDOR_MAP, LINUX_CAT_MAP, getVendorName, getVendorColor, getCatLabel, formatTime, apiClicks } from '../../api/index.js'

const router = useRouter()

const search = ref('')
const vendorFilter = ref('')
const catFilter = ref('')
const activeCat = ref('')
const items = ref([])

const allCats = computed(() => {
  const keys = new Set()
  items.value.forEach(i => { if (i.cat) keys.add(i.cat) })
  return [...keys].sort().map(k => ({ key: k, label: getCatLabel(k, LINUX_CAT_MAP) }))
})

const dynCatMap = computed(() => {
  const map = {}
  items.value.forEach(i => { if (i.cat && !map[i.cat]) map[i.cat] = getCatLabel(i.cat, LINUX_CAT_MAP) })
  return map
})

const filtered = computed(() => {
  let list = items.value
  const s = search.value.trim().toLowerCase()
  if (activeCat.value) list = list.filter(i => i.cat === activeCat.value)
  if (catFilter.value) list = list.filter(i => i.cat === catFilter.value)
  if (vendorFilter.value) list = list.filter(i => i.vendor === vendorFilter.value)
  if (s) list = list.filter(i => (i.title||'').toLowerCase().includes(s) || (i.desc||'').toLowerCase().includes(s))
  return list
})

function onCatSelect(key) {
  activeCat.value = activeCat.value === key ? '' : key
}

function handleClick(item) {
  apiClicks.record('linux', item.id, item.title).catch(() => {})
  router.push('/linux/detail/' + item.id)
}

onMounted(async () => {
  try {
    const res = await apiLinux.list()
    items.value = res.data
  } catch (e) { console.error('加载失败:', e) }
})
</script>

<style scoped>
.top-bar{display:flex;justify-content:space-between;align-items:center;margin-bottom:24px;flex-wrap:wrap;gap:12px}
.top-bar h2{font-size:26px;font-weight:700;color:var(--text);letter-spacing:-.3px;position:relative;padding-bottom:4px}
.top-bar h2::after{content:'';position:absolute;bottom:0;left:0;width:40px;height:3px;background:linear-gradient(90deg,var(--primary),var(--orange));border-radius:2px}
.cmd-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:16px}
.cmd-card{background:var(--bg-white);border-radius:var(--radius);border:1.5px solid var(--border);cursor:pointer;transition:var(--transition-slow);position:relative;overflow:hidden;display:flex;flex-direction:column}
.cmd-card::before{content:'';position:absolute;top:0;left:0;right:0;height:3px;background:linear-gradient(90deg,var(--primary),var(--orange));opacity:0;transition:opacity .3s ease}
.cmd-card:hover{border-color:var(--primary);box-shadow:0 8px 25px rgba(37,99,235,.12);transform:translateY(-3px)}
.cmd-card:hover::before{opacity:1}
.cmd-card:active{transform:translateY(-1px);box-shadow:0 4px 12px rgba(37,99,235,.08)}
.cmd-card-body{padding:16px 20px 20px;display:flex;flex-direction:column;height:100%}
.cmd-card h3{font-size:17px;font-weight:600;margin-bottom:6px;color:var(--text);transition:color .2s}
.cmd-card:hover h3{color:var(--primary)}
.tag-vendor{font-size:13px;padding:3px 10px;border-radius:12px;font-weight:500;transition:var(--transition-fast);display:inline-block}
.tag-cat{font-size:13px;padding:3px 10px;border-radius:12px;font-weight:500;background:#fff7ed;color:#ea580c;display:inline-block}
.tag-time{font-size:12px;padding:2px 8px;border-radius:12px;background:#f1f5f9;color:#64748b;display:inline-block}
</style>
