<template>
  <div class="profile-page">
    <div class="profile-content">
      <template v-if="!libraryMode">
        <div class="profile-card profile-main-card">
          <div class="profile-header">
            <div class="profile-avatar-wrap">
              <div class="profile-avatar" :style="avatarStyle">
                <img v-if="profile.avatar" :src="profile.avatar" class="profile-avatar-img" @error="profile.avatar=''" />
                <span v-else class="profile-avatar-letter">{{ displayName.charAt(0).toUpperCase() }}</span>
                <div class="profile-avatar-overlay" @click="triggerUpload">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                    <circle cx="12" cy="13" r="4"/>
                  </svg>
                  <span>更换头像</span>
                </div>
                <input ref="fileInput" type="file" accept="image/*" @change="onAvatarChange" style="display:none" />
              </div>
              <div class="profile-role-badge" :class="role === 'SUPER_ADMIN' ? 'badge-purple' : 'badge-blue'">
                {{ role === 'SUPER_ADMIN' ? '超级管理员' : role === 'ADMIN' ? '管理员' : '用户' }}
              </div>
            </div>
            <div class="profile-header-info">
              <h2>{{ profile.realName || displayName }}</h2>
              <p class="profile-username">@{{ displayName }}</p>
              <p class="profile-meta">注册时间: {{ accountCreatedAt }}</p>
            </div>
            <button class="profile-back-primary" @click="returnToList">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M19 12H5"/>
                <path d="M12 19l-7-7 7-7"/>
              </svg>
              返回
            </button>
          </div>

          <div class="profile-info-panel">
            <h3>个人信息</h3>
            <div class="profile-form">
              <div class="form-row">
                <div class="form-group">
                  <label>昵称</label>
                  <input v-model="form.realName" placeholder="请输入昵称" maxlength="50" />
                </div>
                <div class="form-group">
                  <label>邮箱</label>
                  <input v-model="form.email" type="email" placeholder="请输入邮箱" maxlength="100" />
                </div>
              </div>
              <div class="form-group form-group-full">
                <label>个人简介</label>
                <textarea v-model="form.bio" placeholder="简单介绍一下自己" rows="3" maxlength="500"></textarea>
              </div>
              <div class="form-actions">
                <button class="btn-save" :disabled="saving" @click="saveProfile">
                  {{ saving ? '保存中...' : '保存修改' }}
                </button>
                <button class="profile-reset-danger" @click="resetForm">重置</button>
              </div>
            </div>
          </div>
        </div>

        <div class="profile-card library-section">
          <div class="library-header">
            <h3>历史查看记录</h3>
            <div class="library-header-actions">
              <span>{{ historyItems.length }} 条</span>
              <button v-if="historyItems.length > 5" class="library-more" type="button" @click="openLibrary('history')">更多</button>
            </div>
          </div>
          <div v-if="historyItems.length" class="library-grid">
            <button
              v-for="entry in previewHistoryItems"
              :key="'history-' + entry.module + '-' + entry.id"
              class="library-card"
              type="button"
              @click="goLibraryItem(entry)"
            >
              <div class="library-card-top">
                <span class="library-module">{{ entry.moduleLabel }}</span>
                <span v-if="entry.category" class="library-category">{{ entry.category }}</span>
              </div>
              <h4>{{ entry.title }}</h4>
              <p>{{ truncateText(entry.desc) || '暂无描述' }}</p>
              <div class="library-meta">{{ formatLibraryTime(entry.savedAt) }}</div>
            </button>
          </div>
          <div v-else class="library-empty">暂无历史记录</div>
        </div>

        <div class="profile-card library-section">
          <div class="library-header">
            <h3>收藏内容</h3>
            <div class="library-header-actions">
              <span>{{ favoriteItems.length }} 条</span>
              <button class="library-more" type="button" @click="toggleSelectAllFavorites" v-if="favoriteItems.length">{{ allFavoritesSelected ? '取消全选' : '全选' }}</button>
              <button v-if="favoriteItems.length > 5 && !selectedFavoriteIds.size" class="library-more" type="button" @click="openLibrary('favorites')">更多</button>
            </div>
          </div>
          <div v-if="favoriteItems.length" class="library-grid">
            <div
              v-for="entry in previewFavoriteItems"
              :key="'favorite-' + entry.module + '-' + entry.id"
              class="library-card-wrap"
            >
              <label class="favorite-checkbox" @click.stop :title="selectedFavoriteIds.has(entry.id) ? '取消选择' : '选择'">
                <input type="checkbox" :checked="selectedFavoriteIds.has(entry.id)" @change="toggleFavoriteSelection(entry.id)" />
                <span class="checkmark"></span>
              </label>
              <button class="library-card" type="button" @click="goLibraryItem(entry)">
                <div class="library-card-top">
                  <span class="library-module">{{ entry.moduleLabel }}</span>
                  <span v-if="entry.category" class="library-category">{{ entry.category }}</span>
                </div>
                <h4>{{ entry.title }}</h4>
                <p>{{ truncateText(entry.desc) || '暂无描述' }}</p>
                <div class="library-meta">{{ formatLibraryTime(entry.savedAt) }}</div>
              </button>
            </div>
          </div>
          <div v-else class="library-empty">暂无收藏内容</div>
          <div v-if="selectedFavoriteIds.size" class="favorites-batch-bar">
            <span>已选择 {{ selectedFavoriteIds.size }} 项</span>
            <button class="btn-batch-delete" :disabled="batchDeleting" @click="deleteSelectedFavorites">
              {{ batchDeleting ? '删除中...' : '批量删除' }}
            </button>
          </div>
        </div>
      </template>

      <div v-else class="profile-card library-section library-full-section">
        <div class="library-header">
          <h3>{{ libraryMode === 'history' ? '历史查看记录' : '收藏内容' }}</h3>
          <div class="library-header-actions">
            <span>{{ currentLibraryItems.length }} 条</span>
            <button v-if="libraryMode === 'favorites' && currentLibraryItems.length" class="library-more" type="button" @click="toggleSelectAllFavorites">{{ allFavoritesSelected ? '取消全选' : '全选' }}</button>
            <button class="library-more" type="button" @click="openLibrary(null)">返回个人中心</button>
          </div>
        </div>
        <div v-if="currentLibraryItems.length" class="library-grid library-grid-full">
          <div
            v-for="entry in currentLibraryItems"
            :key="libraryMode + '-' + entry.module + '-' + entry.id"
            class="library-card-wrap"
          >
            <label v-if="libraryMode === 'favorites'" class="favorite-checkbox" @click.stop :title="selectedFavoriteIds.has(entry.id) ? '取消选择' : '选择'">
              <input type="checkbox" :checked="selectedFavoriteIds.has(entry.id)" @change="toggleFavoriteSelection(entry.id)" />
              <span class="checkmark"></span>
            </label>
            <button class="library-card" type="button" @click="goLibraryItem(entry)">
              <div class="library-card-top">
                <span class="library-module">{{ entry.moduleLabel }}</span>
                <span v-if="entry.category" class="library-category">{{ entry.category }}</span>
              </div>
              <h4>{{ entry.title }}</h4>
              <p>{{ truncateText(entry.desc) || '暂无描述' }}</p>
              <div class="library-meta">{{ formatLibraryTime(entry.savedAt) }}</div>
            </button>
          </div>
        </div>
        <div v-else class="library-empty">{{ libraryMode === 'history' ? '暂无历史记录' : '暂无收藏内容' }}</div>
        <div v-if="libraryMode === 'favorites' && selectedFavoriteIds.size" class="favorites-batch-bar">
          <span>已选择 {{ selectedFavoriteIds.size }} 项</span>
          <button class="btn-batch-delete" :disabled="batchDeleting" @click="deleteSelectedFavorites">
            {{ batchDeleting ? '删除中...' : '批量删除' }}
          </button>
        </div>
      </div>

      <div v-if="msg" class="profile-toast" :class="msgType">{{ msg }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { http } from '../../api/index.js'
import { apiFavorites } from '../../api/modules.js'
import { getFavorites, getHistory } from '../../utils/userLibrary.js'

const router = useRouter()
const route = useRoute()
const displayName = ref(localStorage.getItem('username') || '')
const role = ref(localStorage.getItem('role') || '')
const saving = ref(false)
const msg = ref('')
const msgType = ref('success')
const fileInput = ref(null)
const historyItems = ref([])
const favoriteItems = ref([])
const selectedFavoriteIds = ref(new Set())
const batchDeleting = ref(false)

const profile = reactive({
  realName: '', email: '', avatar: '', bio: ''
})
const accountCreatedAt = ref('')

const form = reactive({
  realName: '', email: '', bio: ''
})

const libraryMode = computed(() => {
  const mode = route.query.library
  return mode === 'history' || mode === 'favorites' ? mode : ''
})
const previewHistoryItems = computed(() => historyItems.value.slice(0, 5))
const previewFavoriteItems = computed(() => favoriteItems.value.slice(0, 5))
const currentLibraryItems = computed(() => libraryMode.value === 'history' ? historyItems.value : favoriteItems.value)

const avatarColor = computed(() => {
  const colors = ['#2563eb', '#7c3aed', '#059669', '#dc2626', '#ea580c', '#0891b2']
  let hash = 0
  for (let i = 0; i < displayName.value.length; i++) {
    hash = displayName.value.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
})

const avatarStyle = computed(() => {
  if (profile.avatar) {
    return { backgroundImage: `url(${profile.avatar})`, backgroundSize: 'cover', backgroundPosition: 'center', background: avatarColor.value }
  }
  return { background: avatarColor.value }
})

function resetForm() {
  form.realName = profile.realName || ''
  form.email = profile.email || ''
  form.bio = profile.bio || ''
}

function triggerUpload() {
  fileInput.value?.click()
}

async function onAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 2 * 1024 * 1024) {
    showMsg('头像图片不能超过2MB', 'error')
    return
  }
  try {
    const fd = new FormData()
    fd.append('files', file)
    const uploadRes = await http.post('/upload', fd, { headers: { 'Content-Type': undefined } })
    const results = uploadRes.data?.data
    if (results && results.length > 0) {
      const avatarUrl = results[0].url
      profile.avatar = avatarUrl
      const saveRes = await http.post('/profile/update', {
        realName: form.realName || '',
        email: form.email || '',
        bio: form.bio || '',
        avatar: avatarUrl
      })
      if (saveRes.data?.ok) {
        showMsg('头像已更新', 'success')
      } else {
        showMsg(saveRes.data?.error || '保存头像失败', 'error')
      }
    }
  } catch (err) {
    const errMsg = err.response?.data?.error || err.response?.data?.msg || err.message
    showMsg('上传失败: ' + errMsg, 'error')
  }
  e.target.value = ''
}

async function loadProfile() {
  try {
    const res = await http.get('/profile')
    if (res.data?.ok && res.data?.data) {
      const d = res.data.data
      profile.realName = d.realName || ''
      profile.email = d.email || ''
      profile.avatar = d.avatar || ''
      profile.bio = d.bio || ''
      accountCreatedAt.value = d.createdAt || ''
      role.value = d.role || role.value
      resetForm()
    }
  } catch (e) {
    showMsg('加载个人信息失败', 'error')
  }
}

async function saveProfile() {
  saving.value = true
  try {
    const res = await http.post('/profile/update', {
      realName: form.realName,
      email: form.email,
      bio: form.bio,
      avatar: profile.avatar
    })
    if (res.data?.ok) {
      profile.realName = form.realName
      profile.email = form.email
      profile.bio = form.bio
      showMsg('保存成功', 'success')
    } else {
      showMsg(res.data?.error || '保存失败', 'error')
    }
  } catch (e) {
    showMsg('保存失败: ' + (e.response?.data?.error || e.message), 'error')
  } finally {
    saving.value = false
  }
}

function showMsg(text, type) {
  msg.value = text
  msgType.value = type
  setTimeout(() => { msg.value = '' }, 3000)
}

const allFavoritesSelected = computed(() => {
  return favoriteItems.value.length > 0 && selectedFavoriteIds.value.size === favoriteItems.value.length
})

function toggleSelectAllFavorites() {
  if (allFavoritesSelected.value) {
    selectedFavoriteIds.value = new Set()
  } else {
    selectedFavoriteIds.value = new Set(favoriteItems.value.map(i => i.id))
  }
}

function toggleFavoriteSelection(id) {
  const next = new Set(selectedFavoriteIds.value)
  if (next.has(id)) {
    next.delete(id)
  } else {
    next.add(id)
  }
  selectedFavoriteIds.value = next
}

async function deleteSelectedFavorites() {
  const ids = [...selectedFavoriteIds.value]
  if (!ids.length) return
  batchDeleting.value = true
  try {
    await apiFavorites.batchDelete(ids)
    selectedFavoriteIds.value = new Set()
    await refreshLibrary()
    showMsg(`已删除 ${ids.length} 条收藏`, 'success')
  } catch (e) {
    showMsg('批量删除失败', 'error')
  } finally {
    batchDeleting.value = false
  }
}

async function refreshLibrary() {
  historyItems.value = getHistory()
  favoriteItems.value = await getFavorites()
  selectedFavoriteIds.value = new Set()
}

function goLibraryItem(entry) {
  if (entry?.path) router.push(entry.path)
}

function openLibrary(mode) {
  router.push(mode ? { path: '/profile', query: { library: mode } } : '/profile')
}

function returnToList() {
  router.push('/cmd')
}

function truncateText(text, limit = 84) {
  const value = String(text || '').trim()
  return value.length > limit ? value.slice(0, limit) + '...' : value
}

function formatLibraryTime(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = n => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

onMounted(() => {
  if (!localStorage.getItem('token')) {
    router.push('/login')
    return
  }
  loadProfile()
  refreshLibrary()
  window.addEventListener('user-library-change', refreshLibrary)
})

onBeforeUnmount(() => {
  window.removeEventListener('user-library-change', refreshLibrary)
})
</script>

<style scoped>
.profile-page{min-height:100vh;position:relative}
.profile-content{position:relative;z-index:1;width:80vw;max-width:none;margin:0 auto;padding:40px 0}

.profile-card{background:#fff;border:1.5px solid #e2e8f0;border-radius:12px;padding:28px;box-shadow:0 1px 3px rgba(0,0,0,.04)}
.profile-main-card{padding:28px 28px 32px}
.profile-header{display:flex;gap:24px;align-items:flex-start;position:relative;padding:0 0 28px;margin-bottom:28px;border-bottom:1.5px solid #f1f5f9}
.profile-back-primary{position:absolute;top:0;right:0;display:inline-flex;align-items:center;gap:5px;padding:10px 28px;border:1.5px solid #2563eb!important;border-radius:8px;background:#2563eb!important;color:#fff!important;font-size:14px;font-weight:600;cursor:pointer;transition:all .2s;box-shadow:0 4px 12px rgba(37,99,235,.18)}
.profile-back-primary:hover{background:#1d4ed8!important;border-color:#1d4ed8!important;color:#fff!important}
.profile-back-primary svg{flex-shrink:0}
.profile-avatar-wrap{display:flex;flex-direction:column;align-items:center;gap:10px;flex-shrink:0}
.profile-avatar{width:80px;height:80px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:32px;font-weight:700;color:#fff;box-shadow:0 4px 12px rgba(0,0,0,.1);position:relative;overflow:hidden;cursor:pointer;transition:all .2s}
.profile-avatar:hover{box-shadow:0 4px 20px rgba(0,0,0,.2)}
.profile-avatar:hover .profile-avatar-overlay{opacity:1}
.profile-avatar-img{width:100%;height:100%;object-fit:cover;border-radius:50%;display:block}
.profile-avatar-letter{display:flex;align-items:center;justify-content:center;width:100%;height:100%;pointer-events:none;user-select:none}
.profile-avatar-overlay{position:absolute;inset:0;border-radius:50%;background:rgba(0,0,0,.55);display:flex;flex-direction:column;align-items:center;justify-content:center;color:#fff;font-size:12px;font-weight:500;gap:4px;cursor:pointer;opacity:0;transition:opacity .2s;pointer-events:none}
.profile-avatar:hover .profile-avatar-overlay{pointer-events:auto}
.profile-avatar-overlay svg{width:18px;height:18px}
.profile-role-badge{padding:3px 10px;border-radius:6px;font-size:12px;font-weight:600}
.badge-purple{background:#f3e8ff;color:#7c3aed}
.badge-blue{background:#e0f2fe;color:#0284c7}
.profile-header-info{flex:1;min-width:0;padding-right:150px}
.profile-header-info h2{font-size:24px;font-weight:700;color:#0f172a;margin:0 0 4px}
.profile-username{font-size:14px;color:#64748b;margin:0 0 6px}
.profile-meta{font-size:13px;color:#94a3b8;margin:0}

.profile-info-panel h3,.library-header h3{font-size:18px;font-weight:600;color:#0f172a;margin:0}
.profile-info-panel h3{margin-bottom:20px}
.profile-form{display:flex;flex-direction:column;gap:16px}
.form-row{display:flex;gap:16px}
.form-row .form-group{flex:1;min-width:0}
.form-group{display:flex;flex-direction:column;gap:6px}
.form-group-full{width:100%}
.form-group label{font-size:13px;font-weight:600;color:#475569}
.form-group input,.form-group textarea{padding:10px 14px;border:1.5px solid #e2e8f0;border-radius:8px;font-size:14px;color:#1e293b;background:#f8fafc;transition:all .2s;outline:none;font-family:inherit}
.form-group input:focus,.form-group textarea:focus{border-color:#1e40af;background:#fff;box-shadow:0 0 0 3px rgba(30,64,175,.1)}
.form-group textarea{resize:vertical;min-height:80px}
.form-group input::placeholder,.form-group textarea::placeholder{color:#94a3b8}
.form-actions{display:flex;gap:12px;padding-top:8px;justify-content:flex-end}
.btn-save{padding:10px 28px;background:#2563eb;color:#fff;border:none;border-radius:8px;font-size:14px;font-weight:600;cursor:pointer;transition:all .2s}
.btn-save:hover{background:#1d4ed8}
.btn-save:disabled{opacity:.5;cursor:not-allowed}
.profile-reset-danger{padding:10px 28px;background:#dc2626!important;color:#fff!important;border:1.5px solid #dc2626!important;border-radius:8px;font-size:14px;font-weight:600;cursor:pointer;transition:all .2s;box-shadow:0 4px 12px rgba(220,38,38,.16)}
.profile-reset-danger:hover{background:#b91c1c!important;border-color:#b91c1c!important;color:#fff!important}

.library-section{margin-top:16px}
.library-full-section{margin-top:0}
.library-header{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:18px;border-bottom:1.5px solid #f1f5f9;padding-bottom:12px}
.library-header-actions{display:flex;align-items:center;gap:12px}
.library-header span{font-size:13px;color:#64748b;font-weight:600}
.library-more{height:32px;padding:0 14px;border:1.5px solid #dbe3ef;border-radius:8px;background:#fff;color:#2563eb;font-size:13px;font-weight:700;cursor:pointer;transition:all .2s}
.library-more:hover{border-color:#2563eb;background:#eff6ff}
.library-grid{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:14px}
.library-grid-full{grid-template-columns:repeat(5,minmax(0,1fr))}
.library-card{display:flex;flex-direction:column;align-items:stretch;text-align:left;aspect-ratio:1.414 / 1;min-height:0;padding:18px;border:1.5px solid #e2e8f0;border-radius:10px;background:#fff;cursor:pointer;transition:all .2s ease;font-family:inherit;overflow:hidden}
.library-card:hover{border-color:#2563eb;box-shadow:0 10px 24px rgba(37,99,235,.12);transform:translateY(-1px)}
.library-card-top{display:flex;align-items:center;gap:8px;flex-wrap:wrap;margin-bottom:12px}
.library-module,.library-category{display:inline-flex;align-items:center;max-width:100%;height:24px;padding:0 10px;border-radius:999px;font-size:12px;font-weight:700;white-space:nowrap}
.library-module{background:#eff6ff;color:#2563eb}
.library-category{background:#f8fafc;color:#64748b;border:1px solid #e2e8f0}
.library-card h4{font-size:16px;line-height:1.4;color:#0f172a;margin:0 0 8px;font-weight:700}
.library-card p{font-size:14px;line-height:1.6;color:#64748b;margin:0;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}
.library-meta{margin-top:auto;padding-top:14px;font-size:12px;color:#94a3b8}
.library-empty{height:96px;border:1.5px dashed #dbe3ef;border-radius:10px;display:flex;align-items:center;justify-content:center;color:#94a3b8;font-size:14px;background:#f8fafc}

.library-card-wrap{position:relative}
.favorite-checkbox{position:absolute;top:8px;right:8px;z-index:2;width:22px;height:22px;cursor:pointer;display:flex;align-items:center;justify-content:center;border-radius:4px;background:#fff;border:1.5px solid #cbd5e1;transition:all .15s}
.favorite-checkbox:hover{border-color:#2563eb;background:#eff6ff}
.favorite-checkbox input{position:absolute;opacity:0;width:0;height:0}
.favorite-checkbox .checkmark{width:12px;height:12px;border-radius:2px;border:2px solid #94a3b8;transition:all .15s;display:block;box-sizing:border-box}
.favorite-checkbox input:checked+.checkmark{background:#2563eb;border-color:#2563eb;background-image:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16' fill='%23fff'%3E%3Cpath d='M13.78 4.22a.75.75 0 0 1 0 1.06l-7.25 7.25a.75.75 0 0 1-1.06 0L2.22 9.28a.75.75 0 0 1 1.06-1.06L6 10.94l6.72-6.72a.75.75 0 0 1 1.06 0z'/%3E%3C/svg%3E");background-size:10px;background-position:center;background-repeat:no-repeat}

.favorites-batch-bar{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-top:16px;padding:12px 18px;background:#f1f5f9;border:1.5px solid #e2e8f0;border-radius:10px;font-size:14px;font-weight:600;color:#0f172a}
.btn-batch-delete{padding:8px 20px;background:#dc2626;color:#fff;border:none;border-radius:8px;font-size:13px;font-weight:600;cursor:pointer;transition:all .2s}
.btn-batch-delete:hover{background:#b91c1c}
.btn-batch-delete:disabled{opacity:.5;cursor:not-allowed}

.profile-toast{position:fixed;bottom:24px;left:50%;transform:translateX(-50%);padding:10px 24px;border-radius:8px;font-size:14px;font-weight:500;z-index:100;white-space:nowrap}
.profile-toast.success{background:#ecfdf5;color:#059669;border:1px solid #a7f3d0}
.profile-toast.error{background:#fef2f2;color:#dc2626;border:1px solid #fecaca}

@media (max-width:1100px){
  .profile-content{width:calc(100vw - 32px)}
  .library-grid,.library-grid-full{grid-template-columns:repeat(3,minmax(0,1fr))}
}
@media (max-width:820px){
  .library-grid,.library-grid-full{grid-template-columns:repeat(2,minmax(0,1fr))}
}
@media (max-width:640px){
  .profile-content{width:calc(100vw - 32px);padding:20px 0}
  .profile-card{padding:18px}
  .profile-header{flex-direction:column;align-items:center;text-align:center;padding-bottom:20px;margin-bottom:20px;gap:16px}
  .profile-header-info{text-align:center;padding-right:0;width:100%}
  .profile-header-info h2{font-size:22px}
  .profile-back-primary{position:static;align-self:stretch;justify-content:center;min-height:44px}
  .form-row{flex-direction:column;gap:12px}
  .library-grid,.library-grid-full{grid-template-columns:1fr}
  .library-header{align-items:flex-start;flex-direction:column}
  .library-header-actions{width:100%;flex-wrap:wrap}
  .library-card{aspect-ratio:auto;min-height:190px}
  .favorites-batch-bar{flex-direction:column;align-items:stretch}
  .btn-batch-delete{min-height:42px}
  .profile-toast{width:calc(100vw - 32px);text-align:center;white-space:normal}
}
</style>
