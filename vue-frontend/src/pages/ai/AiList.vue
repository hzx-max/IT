<template>
  <MainLayout>
    <div class="top-bar">
      <h2>AI运维</h2>
      <span class="text-sm text-slate-500 font-medium">共{{ filtered.length }}条</span>
    </div>

    <SearchBar v-model="search" placeholder="搜索AI运维..." :showVendor="false" :showCat="true"
      module="ai-list"
      v-model:catValue="catFilter" :catMap="aiCatMap" />

    <div v-if="filtered.length === 0" class="text-center py-[60px] text-slate-400 text-base">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="mx-auto mb-4 opacity-40"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg><br>没有匹配的结果
    </div>

    <div class="cmd-grid">
      <div v-for="item in filtered" :key="item.id" class="cmd-card" @click="handleClick(item)">
        <div class="cmd-card-body">
          <h3>{{ item.title }}</h3>
          <p class="scenario-text">{{ truncate(item.scenario, 80) }}</p>
          <div class="tag-row mt-auto pt-2">
            <span class="tag-cat">{{ item.category }}</span>
            <span v-if="item.createdAt" class="tag-time">{{ formatDate(item.createdAt) }}</span>
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
import { apiAi, formatDate, apiClicks } from '../../api/index.js'

const router = useRouter()

function truncate(text, len) {
  if (!text) return ''
  return text.length > len ? text.slice(0, len) + '...' : text
}

const search = ref('')
const catFilter = ref('')
const items = ref([])

const aiCatMap = computed(() => {
  const map = {}
  items.value.forEach(i => { if (i.category && !map[i.category]) map[i.category] = i.category })
  return map
})

const filtered = computed(() => {
  let list = items.value
  const s = search.value.trim().toLowerCase()
  if (catFilter.value) list = list.filter(i => i.category === catFilter.value)
  if (s) list = list.filter(i => (i.title || '').toLowerCase().includes(s) || (i.id || '').toLowerCase().includes(s) || (i.scenario || '').toLowerCase().includes(s))
  return list
})

function handleClick(item) {
  apiClicks.record('ai', item.id, item.title).catch(() => {})
  router.push('/ai/detail/' + item.id)
}

onMounted(async () => {
  try {
    const res = await apiAi.list()
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
.cmd-card::before{content:'';position:absolute;top:0;left:0;right:0;height:3px;background:linear-gradient(90deg,#7c3aed,#a855f7);opacity:0;transition:opacity .3s ease}
.cmd-card:hover{border-color:#7c3aed;box-shadow:0 8px 25px rgba(124,58,237,.12);transform:translateY(-3px)}
.cmd-card:hover::before{opacity:1}
.cmd-card:active{transform:translateY(-1px);box-shadow:0 4px 12px rgba(124,58,237,.08)}
.cmd-card-body{padding:16px 20px 20px;display:flex;flex-direction:column;height:100%}
.cmd-card h3{font-size:17px;font-weight:600;margin-bottom:6px;color:var(--text);transition:color .2s}
.cmd-card:hover h3{color:#7c3aed}
.scenario-text{font-size:14px;color:#64748b;line-height:1.5;margin:0}
.tag-row{display:flex;align-items:center;gap:6px;flex-wrap:nowrap;overflow:hidden;min-width:0;width:100%;max-width:100%}
.tag-row .tag-cat{font-size:13px;padding:3px 10px;border-radius:12px;font-weight:500;background:#f5f3ff;color:#7c3aed;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;flex-shrink:1;min-width:0}
.tag-row .tag-time{font-size:12px;padding:2px 8px;border-radius:12px;background:#f1f5f9;color:#64748b;white-space:nowrap;flex-shrink:0}
</style>