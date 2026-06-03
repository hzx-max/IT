<template>
  <div class="super-page">
    <div class="super-bg"></div>
    <div class="super-content">
      <!-- 顶部导航 -->
      <div class="super-topbar">
        <router-link to="/" class="super-brand">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
            <line x1="8" y1="21" x2="16" y2="21"/>
            <line x1="12" y1="17" x2="12" y2="21"/>
          </svg>
          <span>IT运维学习平台</span>
        </router-link>
        <div class="super-topbar-right">
          <span class="super-user-tag">{{ username }}</span>
          <button class="super-btn-ghost" @click="onLogout">退出</button>
          <router-link to="/" class="super-btn-ghost">返回首页</router-link>
        </div>
      </div>

      <div class="super-main">
        <h2 class="super-title">管理员账号管理</h2>
        <p class="super-desc">审核和管理所有管理员账号</p>

        <div v-if="loading" class="super-loading">
          <div class="super-spinner"></div>
          <span>加载中...</span>
        </div>

        <div v-else>
          <!-- 待审核 -->
          <div v-if="pendingUsers.length > 0" class="super-card">
            <h3 class="super-card-title">
              待审核申请
              <span class="super-badge super-badge-orange">{{ pendingUsers.length }}</span>
            </h3>
            <div class="super-list">
              <div v-for="u in pendingUsers" :key="u.id" class="super-list-item">
                <div>
                  <div class="super-list-name">{{ u.username }}</div>
                  <div class="super-list-time">申请时间: {{ u.createdAt }}</div>
                </div>
                <div class="super-list-actions">
                  <button class="super-btn super-btn-primary" @click="approve(u.id, true)">通过</button>
                  <button class="super-btn super-btn-danger" @click="approve(u.id, false)">拒绝</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 待审核数据变更 -->
          <div v-if="pendingChanges.length > 0" class="super-card">
            <h3 class="super-card-title">
              待审核数据变更
              <span class="super-badge super-badge-orange">{{ pendingChanges.length }}</span>
            </h3>
            <div class="super-list">
              <div v-for="c in pendingChanges" :key="c.id" class="super-list-item">
                <div>
                  <div class="super-list-name">
                    <span class="super-tag" :class="opClass(c.operation)">{{ opLabel(c.operation) }}</span>
                    <span class="super-tag super-tag-gray">{{ moduleLabel(c.module) }}</span>
                    <span class="super-list-name-text">{{ c.submitterName }}</span>
                  </div>
                  <div class="super-list-time">{{ formatTime(c.createdAt) }}</div>
                </div>
                <div class="super-list-actions">
                  <button class="super-btn super-btn-primary" @click="approveChange(c.id)">通过</button>
                  <button class="super-btn super-btn-danger" @click="rejectChange(c.id)">拒绝</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 全部管理员 -->
          <div class="super-card">
            <h3 class="super-card-title">全部管理员</h3>
            <div class="super-table-wrap">
              <table class="super-table">
                <thead>
                  <tr>
                    <th>用户名</th>
                    <th>角色</th>
                    <th>状态</th>
                    <th>注册时间</th>
                    <th class="text-right">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="u in allUsers" :key="u.id">
                    <td>
                      <span class="super-table-name">{{ u.username }}</span>
                      <span v-if="currentUser === u.username" class="super-tag-current">当前</span>
                    </td>
                    <td>
                      <span class="super-tag" :class="u.role === 'SUPER_ADMIN' ? 'super-tag-purple' : 'super-tag-blue'">
                        {{ u.role === 'SUPER_ADMIN' ? '超级管理员' : '管理员' }}
                      </span>
                    </td>
                    <td>
                      <span class="super-tag" :class="statusClass(u.status)">{{ statusLabel(u.status) }}</span>
                    </td>
                    <td class="super-table-time">{{ u.createdAt }}</td>
                    <td class="text-right">
                      <button v-if="u.role !== 'SUPER_ADMIN'" class="super-btn-text-danger" @click="removeUser(u)">删除</button>
                      <span v-else class="super-table-na">-</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-if="allUsers.length === 0" class="super-empty">暂无管理员</div>
          </div>
        </div>

        <div v-if="msg" class="super-toast" :class="msgType === 'success' ? 'super-toast-success' : 'super-toast-error'">{{ msg }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { apiAuth, apiAdmin } from '../../api/index.js'

const router = useRouter()
const loading = ref(true)
const allUsers = ref([])
const pendingChanges = ref([])
const msg = ref('')
const msgType = ref('success')
const currentUser = ref(localStorage.getItem('username') || '')
const username = ref(localStorage.getItem('username') || '')

const pendingUsers = computed(() => allUsers.value.filter(u => u.status === 'PENDING'))

const moduleLabels = { cmd: '网络命令', fault: '网络故障', desktop: '桌面运维', linux: 'Linux', office: 'Office', ai: 'AI运维' }

function moduleLabel(m) { return moduleLabels[m] || m }
function opLabel(op) { return { CREATE: '新增', UPDATE: '修改', DELETE: '删除' }[op] || op }
function opClass(op) { return { CREATE: 'super-tag-green', UPDATE: 'super-tag-blue', DELETE: 'super-tag-red' }[op] || 'super-tag-gray' }
function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function statusClass(status) {
  return {
    'APPROVED': 'super-tag-green',
    'PENDING': 'super-tag-orange',
    'REJECTED': 'super-tag-red'
  }[status] || 'super-tag-gray'
}

function statusLabel(status) {
  return {
    'APPROVED': '已通过',
    'PENDING': '待审核',
    'REJECTED': '已拒绝'
  }[status] || status
}

async function loadUsers() {
  try {
    const res = await apiAuth.getUsers()
    allUsers.value = res.data.data || []
  } catch (e) {
    showMsg('加载失败: ' + (e.response?.data?.msg || e.message), 'error')
  } finally {
    loading.value = false
  }
}

async function loadPendingChanges() {
  try {
    const res = await apiAdmin.getPendingChanges()
    pendingChanges.value = res.data.data || []
  } catch (e) {
    console.error('加载待审核变更失败:', e)
  }
}

async function approveChange(id) {
  try {
    await apiAdmin.approveChange(id)
    showMsg('已批准变更', 'success')
    await loadPendingChanges()
  } catch (e) {
    showMsg('操作失败: ' + (e.response?.data?.msg || e.message), 'error')
  }
}

async function rejectChange(id) {
  try {
    await apiAdmin.rejectChange(id)
    showMsg('已拒绝变更', 'success')
    await loadPendingChanges()
  } catch (e) {
    showMsg('操作失败: ' + (e.response?.data?.msg || e.message), 'error')
  }
}

async function approve(userId, approved) {
  try {
    await apiAuth.approveUser(userId, approved)
    showMsg(approved ? '已通过审核' : '已拒绝申请', 'success')
    await loadUsers()
  } catch (e) {
    showMsg('操作失败: ' + (e.response?.data?.msg || e.message), 'error')
  }
}

async function removeUser(user) {
  if (!confirm(`确认删除管理员"${user.username}"吗？`)) return
  try {
    await apiAuth.deleteUser(user.id)
    showMsg('已删除', 'success')
    await loadUsers()
  } catch (e) {
    showMsg('删除失败: ' + (e.response?.data?.msg || e.message), 'error')
  }
}

function onLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
  router.push('/')
}

function showMsg(text, type) {
  msg.value = text
  msgType.value = type
  setTimeout(() => { msg.value = '' }, 3000)
}

onMounted(() => {
  loadUsers()
  loadPendingChanges()
})
</script>

<style scoped>
.super-page{min-height:100vh;position:relative;overflow:hidden}
.super-bg{position:fixed;inset:0;background:linear-gradient(135deg,#f0f4ff 0%,#e8f0fe 30%,#f5f3ff 60%,#fef3c7 100%);z-index:0}
.super-bg::before{content:'';position:absolute;top:-50%;right:-30%;width:80%;height:80%;background:radial-gradient(circle,rgba(37,99,235,.06) 0%,transparent 70%);border-radius:50%}
.super-bg::after{content:'';position:absolute;bottom:-30%;left:-20%;width:60%;height:60%;background:radial-gradient(circle,rgba(124,58,237,.05) 0%,transparent 70%);border-radius:50%}
.super-content{position:relative;z-index:1}

/* 顶栏 */
.super-topbar{display:flex;align-items:center;justify-content:space-between;padding:0 32px;height:60px;background:rgba(255,255,255,.85);backdrop-filter:blur(12px);border-bottom:1.5px solid #e2e8f0}
.super-brand{display:flex;align-items:center;gap:10px;text-decoration:none;color:#2563eb}
.super-brand span{font-size:17px;font-weight:700;color:#0f172a}
.super-topbar-right{display:flex;align-items:center;gap:12px}
.super-user-tag{font-size:13px;color:#64748b;padding:4px 10px;background:#f1f5f9;border-radius:6px}
.super-btn-ghost{padding:6px 14px;border:1.5px solid #e2e8f0;border-radius:6px;background:transparent;color:#475569;font-size:13px;cursor:pointer;text-decoration:none;transition:all .2s}
.super-btn-ghost:hover{border-color:#2563eb;color:#2563eb}

/* 主体 */
.super-main{max-width:900px;margin:0 auto;padding:40px 24px}
.super-title{font-size:28px;font-weight:700;color:#0f172a;margin:0 0 6px}
.super-desc{font-size:14px;color:#64748b;margin:0 0 32px}

/* 加载 */
.super-loading{display:flex;align-items:center;justify-content:center;gap:10px;padding:60px 0;color:#94a3b8}
.super-spinner{width:24px;height:24px;border:3px solid #e2e8f0;border-top-color:#2563eb;border-radius:50%;animation:spin .7s linear infinite}
@keyframes spin{to{transform:rotate(360deg)}}

/* 卡片 */
.super-card{background:#fff;border:1.5px solid #e2e8f0;border-radius:12px;padding:28px;box-shadow:0 1px 3px rgba(0,0,0,.04);margin-bottom:20px}
.super-card-title{font-size:16px;font-weight:600;color:#0f172a;margin:0 0 18px;display:flex;align-items:center;gap:8px}

/* 标签 */
.super-badge{display:inline-flex;align-items:center;justify-content:center;min-width:20px;height:20px;padding:0 6px;border-radius:10px;font-size:11px;font-weight:600}
.super-badge-orange{background:#fff7ed;color:#ea580c}
.super-tag{display:inline-block;padding:3px 10px;border-radius:6px;font-size:12px;font-weight:500}
.super-tag-purple{background:#f3e8ff;color:#7c3aed}
.super-tag-blue{background:#e0f2fe;color:#0284c7}
.super-tag-green{background:#ecfdf5;color:#059669}
.super-tag-orange{background:#fff7ed;color:#ea580c}
.super-tag-red{background:#fef2f2;color:#dc2626}
.super-tag-gray{background:#f1f5f9;color:#64748b}
.super-tag-current{display:inline-block;margin-left:6px;padding:1px 6px;background:#e0f2fe;color:#0284c7;border-radius:4px;font-size:11px;font-weight:500}

/* 列表 */
.super-list{display:flex;flex-direction:column;gap:10px}
.super-list-item{display:flex;align-items:center;justify-content:space-between;padding:14px 16px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px}
.super-list-name{font-size:14px;font-weight:600;color:#1e293b;display:flex;align-items:center;gap:6px}
.super-list-name-text{font-size:14px;font-weight:600;color:#1e293b}
.super-list-time{font-size:12px;color:#94a3b8;margin-top:3px}
.super-list-actions{display:flex;gap:8px}

/* 按钮 */
.super-btn{padding:7px 16px;border-radius:6px;font-size:13px;font-weight:500;cursor:pointer;border:none;transition:all .2s}
.super-btn-primary{background:#2563eb;color:#fff}
.super-btn-primary:hover{background:#1d4ed8}
.super-btn-danger{background:#fef2f2;color:#dc2626;border:1px solid #fecaca}
.super-btn-danger:hover{background:#fecaca}
.super-btn-text-danger{background:transparent;border:none;color:#ef4444;font-size:13px;cursor:pointer;padding:4px 8px;border-radius:4px;transition:all .2s}
.super-btn-text-danger:hover{background:#fef2f2}

/* 表格 */
.super-table-wrap{overflow-x:auto}
.super-table{width:100%;border-collapse:collapse;font-size:13px}
.super-table th{text-align:left;padding:12px 14px;font-weight:600;color:#475569;border-bottom:2px solid #e2e8f0;white-space:nowrap}
.super-table td{padding:14px 14px;border-bottom:1px solid #f1f5f9;color:#334155}
.super-table tr:hover td{background:#f8fafc}
.super-table-name{font-weight:500;color:#1e293b}
.super-table-time{color:#94a3b8}
.super-table-na{color:#cbd5e1}
.text-right{text-align:right}

/* 空状态 */
.super-empty{text-align:center;padding:40px 0;color:#94a3b8;font-size:14px}

/* 提示 */
.super-toast{position:fixed;bottom:24px;left:50%;transform:translateX(-50%);padding:10px 24px;border-radius:8px;font-size:14px;font-weight:500;z-index:100;white-space:nowrap}
.super-toast-success{background:#ecfdf5;color:#059669;border:1px solid #a7f3d0}
.super-toast-error{background:#fef2f2;color:#dc2626;border:1px solid #fecaca}
</style>