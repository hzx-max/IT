<template>
  <div class="home-page">
    <div class="home-bg"></div>

    <!-- 右上角登录/退出 -->
    <div class="home-auth-bar">
      <template v-if="loggedIn">
        <span class="auth-user-info">
          <span class="auth-avatar">{{ username.charAt(0).toUpperCase() }}</span>
          <span class="auth-username">{{ username }}</span>
          <span class="auth-role" :class="role === 'SUPER_ADMIN' ? 'role-super' : 'role-admin'">{{ role === 'SUPER_ADMIN' ? '超级管理员' : '管理员' }}</span>
        </span>
        <router-link v-if="role === 'SUPER_ADMIN'" to="/super-admin" class="auth-btn auth-btn-admin">管理员管理</router-link>
        <button class="auth-btn auth-btn-outline" @click="onLogout">退出登录</button>
      </template>
      <template v-else>
        <router-link to="/login" class="auth-btn auth-btn-primary">管理员登录</router-link>
        <router-link to="/register" class="auth-btn auth-btn-outline">注册管理员</router-link>
      </template>
    </div>

    <div class="home-content">
      <div class="home-header">
        <h1 class="home-title">IT运维学习平台</h1>
        <p class="home-subtitle">涵盖网络命令、故障排查、桌面运维、Linux系统、Office办公、AI运维等IT运维核心知识</p>
      </div>

      <div class="home-grid">
        <router-link v-for="mod in modules" :key="mod.title" :to="mod.to" class="home-card">
          <div class="home-card-icon" :style="{ background: mod.iconBg }">
            <svg v-if="mod.icon==='cmd'" width="28" height="28" viewBox="0 0 16 16" fill="none" stroke="#fff" stroke-width="1.5"><rect x="2" y="2" width="5" height="5" rx="1"/><rect x="9" y="2" width="5" height="5" rx="1"/><rect x="2" y="9" width="5" height="5" rx="1"/><rect x="9" y="9" width="5" height="5" rx="1"/></svg>
            <svg v-else-if="mod.icon==='fault'" width="28" height="28" viewBox="0 0 16 16" fill="none" stroke="#fff" stroke-width="1.5"><path d="M8 1v14M1 8h14"/><circle cx="8" cy="8" r="2.5"/></svg>
            <svg v-else-if="mod.icon==='desktop'" width="28" height="28" viewBox="0 0 16 16" fill="none" stroke="#fff" stroke-width="1.5"><rect x="2" y="2" width="12" height="12" rx="2"/><path d="M5 6h6M5 8h4M5 10h5"/></svg>
            <svg v-else-if="mod.icon==='linux'" width="28" height="28" viewBox="0 0 16 16" fill="none" stroke="#fff" stroke-width="1.5"><path d="M4 3h8l1 3-3 4 3 4-1 3H4l-1-3 3-4-3-4Z"/></svg>
            <svg v-else-if="mod.icon==='ai'" width="28" height="28" viewBox="0 0 16 16" fill="none" stroke="#fff" stroke-width="1.5"><path d="M8 1l2 5h5l-4 3 1.5 5L8 11l-4.5 3L5 9l-4-3h5l2-5z"/><circle cx="8" cy="4" r="1.2" fill="#fff" stroke="none"/></svg>
            <svg v-else width="28" height="28" viewBox="0 0 16 16" fill="none" stroke="#fff" stroke-width="1.5"><path d="M4 2h8l2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Z"/><path d="M8 2v5l2-1 2 1V2"/></svg>
          </div>
          <h3 class="home-card-title">{{ mod.title }}</h3>
          <p class="home-card-desc">{{ mod.desc }}</p>
          <span class="home-card-tag" :class="mod.tagClass">{{ mod.tag }}</span>
        </router-link>
      </div>

      <div v-if="!loading" class="home-stats">
        <div class="stats-header">
          <h2 class="stats-title">数据概览</h2>
          <span class="stats-total">知识库总量：<strong>{{ totalCount }}</strong> 条</span>
        </div>

        <div class="stats-cards">
          <router-link v-for="s in statCards" :key="s.label" :to="s.to" class="stat-card">
            <div class="stat-icon" :style="{ background: s.iconBg }"></div>
            <div class="stat-info">
              <span class="stat-label">{{ s.label }}</span>
              <span class="stat-count">{{ s.count }}</span>
            </div>
          </router-link>
        </div>

        <div class="stats-row">
          <div class="stats-chart full-width">
            <h3 class="chart-title">各模块点击量分布</h3>
            <div class="vertical-chart-container">
              <div v-for="s in clickStats" :key="s.label" class="vertical-chart-bar">
                <div class="vertical-chart-bar-wrap">
                  <div class="vertical-chart-fill" :style="{ height: (s.count / maxClickCount * 100) + '%', background: s.color }">
                    <span class="vertical-chart-val">{{ s.count }}</span>
                  </div>
                </div>
                <span class="vertical-chart-label">{{ s.label }}</span>
              </div>
            </div>
            <div v-if="clickStats.every(s => s.count === 0)" class="chart-empty">暂无点击数据，浏览各模块内容后将自动统计</div>
          </div>
        </div>

        <div class="module-top10-row">
          <div v-for="mod in clickStats" :key="mod.label" class="stats-chart module-top10-chart">
            <h3 class="chart-title">{{ mod.label }} 点击量排行 TOP10</h3>
            <div v-if="moduleTop10[mod.moduleKey] && moduleTop10[mod.moduleKey].length > 0" class="top10-list">
              <router-link v-for="(item, idx) in moduleTop10[mod.moduleKey]" :key="item.id"
                :to="getItemRoute(item.module, item.itemId)" class="top10-row">
                <span class="top10-rank" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
                <span class="top10-title">{{ item.itemTitle }}</span>
                <span class="top10-count">{{ item.count }}次</span>
              </router-link>
            </div>
            <div v-else class="chart-empty">暂无该模块点击数据</div>
          </div>
        </div>
      </div>

      <div v-else-if="loading" class="home-stats">
        <div class="stats-skeleton">
          <div class="skeleton" style="height:28px;width:160px;margin-bottom:20px"></div>
          <div class="stats-cards">
            <div v-for="i in 6" :key="i" class="skeleton" style="height:72px;border-radius:12px"></div>
          </div>
        </div>
      </div>

      <div class="home-footer">
        <p>IT运维学习平台 &copy; 2026</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { apiTopics, apiFaults, apiDesktop, apiLinux, apiOffice, apiAi, apiClicks, apiAuth } from '../api/index.js'

const router = useRouter()
const loggedIn = ref(false)
const username = ref('')
const role = ref('')

function updateAuthState() {
  const token = localStorage.getItem('token')
  loggedIn.value = !!token
  username.value = localStorage.getItem('username') || ''
  role.value = localStorage.getItem('role') || ''
}

async function onLogout() {
  try { await apiAuth.logout() } catch (e) { /* ignore */ }
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  updateAuthState()
  router.push('/')
}

function onStorageChange(e) {
  if (e.key === 'token' || e.key === null) {
    updateAuthState()
  }
}

const moduleLabels = { cmd: '网络命令', fault: '网络故障', desktop: '桌面运维', linux: 'Linux系统', office: 'Office办公', ai: 'AI运维' }
const moduleColors = { cmd: '#2563eb', fault: '#ea580c', desktop: '#059669', linux: '#7c3aed', office: '#dc2626', ai: '#a855f7' }
const moduleIconBgs = {
  cmd: 'linear-gradient(135deg,#2563eb,#1d4ed8)', fault: 'linear-gradient(135deg,#ea580c,#c2410c)',
  desktop: 'linear-gradient(135deg,#059669,#047857)', linux: 'linear-gradient(135deg,#7c3aed,#6d28d9)',
  office: 'linear-gradient(135deg,#dc2626,#b91c1c)', ai: 'linear-gradient(135deg,#7c3aed,#a855f7)'
}
const moduleRoutes = { cmd: '/cmd', fault: '/fault', desktop: '/desktop', linux: '/linux', office: '/office', ai: '/ai' }

const detailRouteMap = {
  cmd: (id) => '/cmd/detail/' + id,
  fault: (id) => '/fault/detail/' + id,
  desktop: (id) => '/desktop/detail/' + id,
  linux: (id) => '/linux/detail/' + id,
  office: (id) => '/office/detail/' + id,
  ai: (id) => '/ai/detail/' + id
}

function getItemRoute(module, itemId) {
  const fn = detailRouteMap[module]
  return fn ? fn(itemId) : '/'
}

function getModuleLabel(m) { return moduleLabels[m] || m }

const modules = [
  { title: '网络命令', desc: '华为、H3C、Cisco等网络设备配置命令与管理', tag: '命令查询', tagClass: 'tag-blue', icon: 'cmd', iconBg: 'linear-gradient(135deg,#2563eb,#1d4ed8)', to: '/cmd', color: '#2563eb' },
  { title: '网络故障', desc: '常见网络故障排查与解决方案', tag: '故障排查', tagClass: 'tag-orange', icon: 'fault', iconBg: 'linear-gradient(135deg,#ea580c,#c2410c)', to: '/fault', color: '#ea580c' },
  { title: '桌面运维', desc: '桌面系统常见问题处理与维护', tag: '运维支持', tagClass: 'tag-emerald', icon: 'desktop', iconBg: 'linear-gradient(135deg,#059669,#047857)', to: '/desktop', color: '#059669' },
  { title: 'Linux系统', desc: 'Linux系统管理与运维命令', tag: '系统管理', tagClass: 'tag-purple', icon: 'linux', iconBg: 'linear-gradient(135deg,#7c3aed,#6d28d9)', to: '/linux', color: '#7c3aed' },
  { title: 'Office办公', desc: 'Word、Excel、PowerPoint等办公软件操作', tag: '办公技能', tagClass: 'tag-red', icon: 'office', iconBg: 'linear-gradient(135deg,#dc2626,#b91c1c)', to: '/office', color: '#dc2626' },
  { title: 'AI运维', desc: 'ChatGPT、Copilot、Claude等AI工具运维应用', tag: 'AI工具', tagClass: 'tag-purple', icon: 'ai', iconBg: 'linear-gradient(135deg,#7c3aed,#a855f7)', to: '/ai', color: '#a855f7' }
]

const clickStats = ref([])
const statCards = ref([])
const top10 = ref([])
const moduleTop10 = ref({})
const totalCount = ref(0)
const maxClickCount = ref(1)
const loading = ref(true)
const order = ['cmd', 'fault', 'desktop', 'linux', 'office', 'ai']

onMounted(async () => {
  updateAuthState()
  window.addEventListener('storage', onStorageChange)
  try {
    const listResults = await Promise.allSettled([
      apiTopics.list(), apiFaults.list(), apiDesktop.list(),
      apiLinux.list(), apiOffice.list(), apiAi.list()
    ])

    const totalCounts = listResults.map((r) => {
      if (r.status === 'fulfilled' && r.value) {
        const data = r.value.data
        return Array.isArray(data) ? data.length : 0
      }
      return 0
    })
    totalCount.value = totalCounts.reduce((s, c) => s + c, 0)

    statCards.value = order.map((m, i) => ({
      moduleKey: m,
      label: moduleLabels[m],
      count: totalCounts[i] || 0,
      color: moduleColors[m],
      iconBg: moduleIconBgs[m],
      to: moduleRoutes[m]
    }))

    try {
      const clickRes = await apiClicks.stats()
      const clickMap = {}
      if (clickRes.data && clickRes.data.ok) {
        (clickRes.data.data || []).forEach(e => { clickMap[e.module] = e.count })
      }
      clickStats.value = order.map(m => ({
        moduleKey: m,
        label: moduleLabels[m],
        count: clickMap[m] || 0,
        color: moduleColors[m],
        iconBg: moduleIconBgs[m],
        to: moduleRoutes[m]
      }))
      maxClickCount.value = Math.max(...clickStats.value.map(s => s.count), 1)
    } catch (e) {
      console.error('加载点击统计失败:', e)
      clickStats.value = order.map(m => ({
        moduleKey: m, label: moduleLabels[m],
        count: 0, color: moduleColors[m],
        iconBg: moduleIconBgs[m], to: moduleRoutes[m]
      }))
      maxClickCount.value = 1
    }

    const moduleTop10Map = {}
    const top10Promises = order.map(async (m) => {
      try {
        const res = await apiClicks.top10ByModule(m)
        if (res.data && res.data.ok) {
          moduleTop10Map[m] = res.data.data || []
        }
      } catch { moduleTop10Map[m] = [] }
    })
    await Promise.all(top10Promises)
    moduleTop10.value = moduleTop10Map
  } catch (e) { console.error('加载统计失败:', e) }
  finally { loading.value = false }
})

onUnmounted(() => {
  window.removeEventListener('storage', onStorageChange)
})
</script>

<style scoped>
.home-page{min-height:100vh;position:relative;overflow:hidden}
.home-bg{position:fixed;inset:0;background:linear-gradient(135deg,#f0f4ff 0%,#e8f0fe 30%,#f5f3ff 60%,#fef3c7 100%);z-index:0}
.home-bg::before{content:'';position:absolute;top:-50%;right:-30%;width:80%;height:80%;background:radial-gradient(circle,rgba(37,99,235,.06) 0%,transparent 70%);border-radius:50%}
.home-bg::after{content:'';position:absolute;bottom:-30%;left:-20%;width:60%;height:60%;background:radial-gradient(circle,rgba(124,58,237,.05) 0%,transparent 70%);border-radius:50%}
.home-content{position:relative;z-index:1;max-width:1100px;margin:0 auto;padding:60px 24px 40px}
.home-header{text-align:center;margin-bottom:56px}
.home-title{font-size:40px;font-weight:800;color:#0f172a;margin:20px 0 12px;letter-spacing:-.5px}
.home-subtitle{font-size:18px;color:#64748b;max-width:560px;margin:0 auto;line-height:1.7}
.home-auth-bar{position:fixed;top:16px;right:24px;z-index:100;display:flex;align-items:center;gap:12px}
.home-auth-links{display:flex;align-items:center;justify-content:center;gap:12px;margin-top:20px}
.auth-user-info{display:flex;align-items:center;gap:8px;padding:6px 14px;background:#fff;border:1.5px solid #e2e8f0;border-radius:8px;box-shadow:0 1px 3px rgba(0,0,0,.04)}
.auth-avatar{width:28px;height:28px;border-radius:50%;background:#2563eb;color:#fff;display:flex;align-items:center;justify-content:center;font-size:13px;font-weight:700}
.auth-username{font-size:14px;color:#1e293b;font-weight:500}
.auth-role{font-size:11px;font-weight:600;padding:2px 6px;border-radius:4px}
.role-super{color:#7c3aed;background:#f5f3ff}
.role-admin{color:#2563eb;background:#eff6ff}
.auth-btn{display:inline-flex;align-items:center;padding:8px 20px;border-radius:8px;font-size:14px;font-weight:500;cursor:pointer;text-decoration:none;transition:all .2s;border:1.5px solid transparent;box-shadow:0 1px 3px rgba(0,0,0,.04)}
.auth-btn-primary{background:#2563eb;color:#fff;border-color:#2563eb}
.auth-btn-primary:hover{background:#1d4ed8;box-shadow:0 4px 12px rgba(37,99,235,.3)}
.auth-btn-outline{background:#fff;color:#2563eb;border-color:#2563eb}
.auth-btn-outline:hover{background:#2563eb;color:#fff;box-shadow:0 4px 12px rgba(37,99,235,.2)}
.auth-btn-admin{background:linear-gradient(135deg,#7c3aed,#6d28d9);color:#fff;border-color:#7c3aed}
.auth-btn-admin:hover{background:linear-gradient(135deg,#6d28d9,#5b21b6);box-shadow:0 4px 12px rgba(124,58,237,.35)}
.home-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:24px;margin-bottom:60px}
.home-card{background:#fff;border-radius:16px;padding:28px 24px;border:1.5px solid #e2e8f0;cursor:pointer;transition:all .35s ease;text-decoration:none;position:relative;overflow:hidden}
.home-card::before{content:'';position:absolute;top:0;left:0;right:0;height:3px;background:linear-gradient(90deg,var(--primary),var(--orange));opacity:0;transition:opacity .3s ease}
.home-card:hover{border-color:#2563eb;box-shadow:0 12px 32px rgba(37,99,235,.12);transform:translateY(-4px)}
.home-card:hover::before{opacity:1}
.home-card-icon{width:52px;height:52px;border-radius:14px;display:flex;align-items:center;justify-content:center;margin-bottom:16px}
.home-card-title{font-size:20px;font-weight:700;color:#0f172a;margin-bottom:8px}
.home-card-desc{font-size:14px;color:#64748b;line-height:1.6;margin-bottom:16px}
.home-card-tag{display:inline-block;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:600}
.tag-blue{background:#eff6ff;color:#2563eb}
.tag-orange{background:#fff7ed;color:#ea580c}
.tag-emerald{background:#ecfdf5;color:#059669}
.tag-purple{background:#f5f3ff;color:#7c3aed}
.tag-red{background:#fef2f2;color:#dc2626}
.home-footer{text-align:center;color:#94a3b8;font-size:13px;padding-top:24px;border-top:1px solid #e2e8f0}
.home-stats{margin-bottom:60px}
.stats-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:24px;flex-wrap:wrap;gap:12px}
.stats-title{font-size:24px;font-weight:700;color:#0f172a;position:relative;padding-bottom:4px}
.stats-title::after{content:'';position:absolute;bottom:0;left:0;width:36px;height:3px;background:linear-gradient(90deg,var(--primary),var(--orange));border-radius:2px}
.stats-total{font-size:15px;color:#64748b}
.stats-total strong{color:#0f172a;font-size:22px;font-weight:800}
.stats-cards{display:grid;grid-template-columns:repeat(3,1fr);gap:14px;margin-bottom:32px}
.stat-card{background:#fff;border-radius:12px;padding:16px 18px;border:1.5px solid #e2e8f0;display:flex;align-items:center;gap:14px;text-decoration:none;transition:all .25s ease}
.stat-card:hover{border-color:var(--primary);box-shadow:0 4px 16px rgba(37,99,235,.1);transform:translateY(-2px)}
.stat-icon{width:36px;height:36px;border-radius:8px;flex-shrink:0}
.stat-info{display:flex;flex-direction:column;gap:2px;min-width:0}
.stat-label{font-size:13px;color:#64748b;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.stat-count{font-size:24px;font-weight:700;color:#0f172a;line-height:1}
.stats-chart{background:#fff;border-radius:14px;padding:24px 28px;border:1.5px solid #e2e8f0}
.stats-chart.full-width{grid-column:1/-1}
.chart-title{font-size:16px;font-weight:600;color:#0f172a;margin-bottom:20px}
.vertical-chart-container{display:flex;align-items:stretch;justify-content:space-around;gap:16px;height:200px;padding-bottom:8px}
.vertical-chart-bar{display:flex;flex-direction:column;align-items:center;gap:8px;flex:1;max-width:100px}
.vertical-chart-bar-wrap{flex:1;width:100%;display:flex;align-items:flex-end;justify-content:center;position:relative}
.vertical-chart-fill{width:48px;min-height:24px;border-radius:8px 8px 0 0;display:flex;align-items:center;justify-content:center;transition:height .8s ease;position:absolute;bottom:0}
.vertical-chart-val{font-size:12px;font-weight:700;color:#fff;text-shadow:0 1px 2px rgba(0,0,0,.2)}
.vertical-chart-label{font-size:12px;color:#64748b;font-weight:500;text-align:center;white-space:nowrap}
.stats-row{display:grid;grid-template-columns:1fr;gap:20px;margin-bottom:32px}
.module-top10-row{display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:32px}
.module-top10-chart{min-width:0}
.top10-list{display:flex;flex-direction:column;gap:6px}
.top10-row{display:flex;align-items:center;gap:8px;padding:6px 10px;border-radius:8px;background:#f8fafc;transition:background .2s;text-decoration:none}
.top10-row:hover{background:#eff6ff}
.top10-rank{width:22px;height:22px;border-radius:6px;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:700;color:#fff;background:#94a3b8;flex-shrink:0}
.top10-rank.rank-1{background:linear-gradient(135deg,#f59e0b,#d97706)}
.top10-rank.rank-2{background:linear-gradient(135deg,#94a3b8,#64748b)}
.top10-rank.rank-3{background:linear-gradient(135deg,#cd7836,#a0522d)}
.top10-title{flex:1;font-size:13px;color:#0f172a;font-weight:500;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.top10-count{font-size:12px;color:#64748b;font-weight:600;flex-shrink:0}
.chart-empty{text-align:center;padding:20px 0;color:#94a3b8;font-size:13px}
.stats-skeleton{padding:20px 0}
@media(max-width:1024px){.module-top10-row{grid-template-columns:repeat(2,1fr)}.stats-cards{grid-template-columns:repeat(2,1fr)}}
@media(max-width:768px){.home-auth-bar{top:12px;right:12px;gap:6px}.auth-btn{padding:6px 14px;font-size:13px}.auth-user-info{padding:4px 10px}}
@media(max-width:640px){.home-title{font-size:28px}.home-subtitle{font-size:15px}.home-content{padding:48px 16px 32px}.home-header{margin-bottom:40px}.home-grid{grid-template-columns:1fr;gap:16px;margin-bottom:40px}.home-card{padding:22px 18px}.home-card-icon{width:44px;height:44px;border-radius:12px;margin-bottom:12px}.home-card-title{font-size:18px}.home-card-desc{font-size:13px;margin-bottom:12px}.stats-cards{grid-template-columns:repeat(2,1fr);gap:10px}.stat-card{padding:12px 14px;gap:10px}.stat-icon{width:30px;height:30px;border-radius:6px}.stat-count{font-size:20px}.stats-title{font-size:20px}.stats-total{font-size:13px}.stats-total strong{font-size:18px}.stats-row{grid-template-columns:1fr}.module-top10-row{grid-template-columns:1fr}.vertical-chart-container{height:160px;gap:8px}.vertical-chart-fill{width:32px}.vertical-chart-label{font-size:10px}.stats-chart{padding:16px}.home-auth-bar{top:8px;right:8px;gap:4px;flex-wrap:wrap;justify-content:flex-end}.auth-btn{padding:5px 12px;font-size:12px;border-radius:6px}.auth-user-info{padding:3px 8px;gap:4px}.auth-username{font-size:12px}.auth-avatar{width:24px;height:24px;font-size:11px}}
</style>
