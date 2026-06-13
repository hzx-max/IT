<template>
  <div class="comment-section">
    <div class="comment-header">
      <div class="comment-title">学习笔记 <span class="comment-count">{{ notes.length }}</span></div>
      <div class="comment-sort">
        <span :class="{ active: sortBy === 'hot' }" @click="sortBy = 'hot'">最热</span>
        <span class="sort-divider">|</span>
        <span :class="{ active: sortBy === 'new' }" @click="sortBy = 'new'">最新</span>
      </div>
    </div>

    <div class="comment-input-row">
      <div class="avatar">
        <svg viewBox="0 0 40 40" width="40" height="40"><circle cx="20" cy="20" r="20" fill="#e2e8f0"/><circle cx="20" cy="15" r="7" fill="#94a3b8"/><ellipse cx="20" cy="33" rx="12" ry="9" fill="#94a3b8"/></svg>
      </div>
      <div class="comment-input-box">
        <textarea v-model="newContent" class="comment-textarea" rows="1" placeholder="记录你的学习笔记..." @input="autoGrow"></textarea>
        <div class="comment-input-footer">
          <span class="comment-status">{{ status }}</span>
          <button class="comment-submit-btn" :disabled="!newContent.trim() || submitting" @click="submit">发布</button>
        </div>
      </div>
    </div>

    <div v-if="sortedNotes.length === 0" class="comment-empty">暂无笔记，来发表第一条吧</div>

    <div v-else class="comment-list">
      <div v-for="note in sortedNotes" :key="note.id" class="comment-item">
        <div class="avatar" :style="{ background: getAvatarColor(note.username) }">
          <span class="avatar-text">{{ getAvatarText(note.username) }}</span>
        </div>
        <div class="comment-body">
          <div class="comment-meta">
            <span class="comment-author">{{ note.username }}</span>
          </div>
          <div v-if="editingNoteId === note.id" class="edit-box">
            <textarea v-model="editContent" class="edit-textarea" rows="2" @input="autoGrow"></textarea>
            <div class="edit-actions">
              <button class="reply-cancel-btn" @click="cancelEdit">取消</button>
              <button class="reply-submit-btn" :disabled="!editContent.trim() || submitting" @click="saveEdit(note)">保存</button>
            </div>
          </div>
          <div v-else class="comment-text">{{ note.content }}</div>
          <div class="comment-actions">
            <span class="comment-time">{{ formatTime(note.createdAt) }}</span>
            <button class="action-btn" @click="likeNote(note)" :class="{ liked: note.userReaction === 'like' }">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
              <span v-if="note.likeCount">{{ note.likeCount }}</span>
            </button>
            <button class="action-btn" @click="dislikeNote(note)" :class="{ disliked: note.userReaction === 'dislike' }">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10 15v4a3 3 0 0 0 3 3l4-9V2H5.72a2 2 0 0 0-2 1.7l-1.38 9a2 2 0 0 0 2 2.3zm7-13h2.67A2.31 2.31 0 0 1 22 4v7a2.31 2.31 0 0 1-2.33 2H17"/></svg>
              <span v-if="note.dislikeCount">{{ note.dislikeCount }}</span>
            </button>
            <button class="action-btn reply-btn" @click="toggleReply(note.id)">回复</button>
            <button v-if="canEditNote(note)" class="action-btn" @click="startEdit(note)">编辑</button>
            <button v-if="canDeleteNote(note)" class="action-btn danger" @click="removeNote(note)">删除</button>
          </div>

          <div v-if="replyingTo === note.id" class="reply-input-row">
            <textarea v-model="replyContent" class="reply-textarea" rows="1" placeholder="写回复..." @input="autoGrow"></textarea>
            <div class="reply-input-footer">
              <button class="reply-cancel-btn" @click="cancelReply">取消</button>
              <button class="reply-submit-btn" :disabled="!replyContent.trim() || submitting" @click="submitReply(note)">回复</button>
            </div>
          </div>

          <button
            v-if="note.replies && note.replies.length > 0"
            class="reply-toggle-btn"
            @click="toggleReplies(note.id)"
          >
            {{ isRepliesExpanded(note.id) ? '收起回复' : `展开 ${note.replies.length} 条回复` }}
          </button>

          <div v-if="note.replies && note.replies.length > 0 && isRepliesExpanded(note.id)" class="reply-list">
            <div v-for="reply in note.replies" :key="reply.id" class="reply-item">
              <div class="avatar avatar-sm" :style="{ background: getAvatarColor(reply.username) }">
                <span class="avatar-text avatar-text-sm">{{ getAvatarText(reply.username) }}</span>
              </div>
              <div class="reply-body">
                <div class="comment-meta">
                  <span class="comment-author">{{ reply.username }}</span>
                </div>
                <div v-if="editingNoteId === reply.id" class="edit-box compact">
                  <textarea v-model="editContent" class="edit-textarea" rows="2" @input="autoGrow"></textarea>
                  <div class="edit-actions">
                    <button class="reply-cancel-btn" @click="cancelEdit">取消</button>
                    <button class="reply-submit-btn" :disabled="!editContent.trim() || submitting" @click="saveEdit(reply)">保存</button>
                  </div>
                </div>
                <div v-else class="comment-text">{{ reply.content }}</div>
                <div class="comment-actions">
                  <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
                  <button class="action-btn" @click="likeNote(reply)" :class="{ liked: reply.userReaction === 'like' }">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
                    <span v-if="reply.likeCount">{{ reply.likeCount }}</span>
                  </button>
                  <button class="action-btn" @click="dislikeNote(reply)" :class="{ disliked: reply.userReaction === 'dislike' }">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10 15v4a3 3 0 0 0 3 3l4-9V2H5.72a2 2 0 0 0-2 1.7l-1.38 9a2 2 0 0 0 2 2.3zm7-13h2.67A2.31 2.31 0 0 1 22 4v7a2.31 2.31 0 0 1-2.33 2H17"/></svg>
                    <span v-if="reply.dislikeCount">{{ reply.dislikeCount }}</span>
                  </button>
                  <button v-if="canEditNote(reply)" class="action-btn" @click="startEdit(reply)">编辑</button>
                  <button v-if="canDeleteNote(reply)" class="action-btn danger" @click="removeNote(reply)">删除</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { apiNotes } from '../api/index.js'

const props = defineProps({ targetId: { type: String, required: true } })

const notes = ref([])
const newContent = ref('')
const status = ref('')
const submitting = ref(false)
const sortBy = ref('new')
const replyingTo = ref(null)
const replyContent = ref('')
const expandedReplies = ref(new Set())
const editingNoteId = ref(null)
const editContent = ref('')

function currentUsername() {
  return localStorage.getItem('username') || ''
}

function currentRole() {
  return localStorage.getItem('role') || ''
}

function canEditNote(note) {
  return note.canEdit === true || (!!currentUsername() && note.username === currentUsername())
}

function canDeleteNote(note) {
  return note.canDelete === true || currentRole() === 'SUPER_ADMIN' || (!!currentUsername() && note.username === currentUsername())
}

const sortedNotes = computed(() => {
  const list = [...notes.value]
  if (sortBy.value === 'hot') {
    list.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
  } else {
    list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  }
  return list
})

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.toLocaleDateString('zh-CN') + ' ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const avatarColors = ['#3b82f6', '#8b5cf6', '#ec4899', '#f97316', '#10b981', '#06b6d4', '#6366f1', '#f59e0b']
function getAvatarColor(name) {
  let hash = 0
  for (let i = 0; i < (name || '').length; i++) hash = ((hash << 5) - hash + name.charCodeAt(i)) | 0
  return avatarColors[Math.abs(hash) % avatarColors.length]
}
function getAvatarText(name) { return (name || '?').charAt(0).toUpperCase() }

function autoGrow(e) {
  const el = e.target
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

function toggleReply(noteId) {
  replyingTo.value = replyingTo.value === noteId ? null : noteId
  replyContent.value = ''
}

function cancelReply() {
  replyingTo.value = null
  replyContent.value = ''
}

function isRepliesExpanded(noteId) {
  return expandedReplies.value.has(noteId)
}

function toggleReplies(noteId) {
  const next = new Set(expandedReplies.value)
  if (next.has(noteId)) {
    next.delete(noteId)
  } else {
    next.add(noteId)
  }
  expandedReplies.value = next
}

function expandReplies(noteId) {
  const next = new Set(expandedReplies.value)
  next.add(noteId)
  expandedReplies.value = next
}

function startEdit(note) {
  editingNoteId.value = note.id
  editContent.value = note.content || ''
  replyingTo.value = null
  replyContent.value = ''
}

function cancelEdit() {
  editingNoteId.value = null
  editContent.value = ''
}

function updateNoteInList(id, data) {
  const idx = notes.value.findIndex(n => n.id === id)
  if (idx !== -1) {
    notes.value[idx] = { ...notes.value[idx], ...data }
  } else {
    for (const n of notes.value) {
      if (n.replies) {
        const rIdx = n.replies.findIndex(r => r.id === id)
        if (rIdx !== -1) {
          n.replies[rIdx] = { ...n.replies[rIdx], ...data }
          break
        }
      }
    }
  }
}

function removeNoteFromList(id) {
  const idx = notes.value.findIndex(n => n.id === id)
  if (idx !== -1) {
    notes.value.splice(idx, 1)
    return
  }
  for (const n of notes.value) {
    if (n.replies) {
      const rIdx = n.replies.findIndex(r => r.id === id)
      if (rIdx !== -1) {
        n.replies.splice(rIdx, 1)
        break
      }
    }
  }
}

async function likeNote(note) {
  try {
    const res = await apiNotes.like(note.id)
    const d = res.data?.data
    if (d && d.ok !== false) {
      updateNoteInList(note.id, { userReaction: 'like', likeCount: d.likeCount, dislikeCount: d.dislikeCount })
    }
  } catch {}
}

async function dislikeNote(note) {
  try {
    const res = await apiNotes.dislike(note.id)
    const d = res.data?.data
    if (d && d.ok !== false) {
      updateNoteInList(note.id, { userReaction: 'dislike', likeCount: d.likeCount, dislikeCount: d.dislikeCount })
    }
  } catch {}
}

async function loadNotes() {
  try {
    const res = await apiNotes.list(props.targetId)
    notes.value = Array.isArray(res.data?.data) ? res.data.data : []
    expandedReplies.value = new Set()
  } catch {}
}

async function saveEdit(note) {
  const content = editContent.value.trim()
  if (!content || submitting.value) return
  try {
    submitting.value = true
    const res = await apiNotes.update(note.id, content)
    const updated = res.data?.data
    updateNoteInList(note.id, { content: updated?.content || content })
    cancelEdit()
  } catch (e) {
    const msg = e.response?.data?.error || e.response?.data?.msg || '编辑失败'
    status.value = msg
    setTimeout(() => status.value = '', 3000)
  } finally {
    submitting.value = false
  }
}

async function removeNote(note) {
  if (submitting.value) return
  if (!window.confirm('确定删除这条评论吗？')) return
  try {
    submitting.value = true
    await apiNotes.remove(note.id)
    removeNoteFromList(note.id)
    if (editingNoteId.value === note.id) cancelEdit()
  } catch (e) {
    const msg = e.response?.data?.error || e.response?.data?.msg || '删除失败'
    status.value = msg
    setTimeout(() => status.value = '', 3000)
  } finally {
    submitting.value = false
  }
}

async function submit() {
  if (!newContent.value.trim() || submitting.value) return
  try {
    submitting.value = true
    status.value = '发布中...'
    await apiNotes.create(props.targetId, newContent.value.trim())
    newContent.value = ''
    status.value = ''
    await loadNotes()
  } catch (e) {
    const msg = e.response?.data?.error || '发布失败'
    status.value = msg
    setTimeout(() => status.value = '', 3000)
  } finally {
    submitting.value = false
  }
}

async function submitReply(note) {
  if (!replyContent.value.trim() || submitting.value) return
  try {
    submitting.value = true
    await apiNotes.reply(note.id, props.targetId, replyContent.value.trim())
    replyContent.value = ''
    replyingTo.value = null
    await loadNotes()
  } catch (e) {
    const msg = e.response?.data?.error || '回复失败'
    status.value = msg
    setTimeout(() => status.value = '', 3000)
  } finally {
    submitting.value = false
  }
}

onMounted(loadNotes)
</script>

<style scoped>
.comment-section{background:#fff;border:1.5px solid #e2e8f0;border-radius:12px;padding:24px}
.comment-header{display:flex;align-items:center;gap:12px;margin-bottom:20px;padding-bottom:16px;border-bottom:1px solid #f1f5f9}
.comment-title{font-size:18px;font-weight:700;color:#1e293b}
.comment-count{font-size:14px;font-weight:400;color:#94a3b8;margin-left:4px}
.comment-sort{display:flex;align-items:center;gap:8px;font-size:14px;color:#94a3b8;margin-left:auto;cursor:pointer}
.comment-sort span{transition:all .2s;padding:4px 8px;border-radius:4px;cursor:pointer}
.comment-sort span:hover{color:#64748b;background:#f8fafc}
.comment-sort span.active{color:#1e293b;font-weight:600;background:#f1f5f9}
.sort-divider{color:#e2e8f0}

.comment-input-row{display:flex;gap:12px;align-items:flex-start;margin-bottom:24px}
.avatar{width:40px;height:40px;border-radius:50%;flex-shrink:0;display:flex;align-items:center;justify-content:center;overflow:hidden}
.avatar-sm{width:32px;height:32px}
.avatar-text{color:#fff;font-size:16px;font-weight:600}
.avatar-text-sm{font-size:13px}
.comment-input-box{flex:1;display:grid;grid-template-columns:1fr auto;align-items:center;gap:10px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:10px 12px;transition:border-color .2s}
.comment-input-box:focus-within{border-color:#3b82f6}
.comment-textarea{width:100%;border:none;background:transparent;outline:none;font-size:14px;line-height:1.6;resize:none;color:#1e293b;font-family:inherit;min-height:40px;max-height:80px;padding:9px 4px}
.comment-textarea::placeholder{color:#94a3b8}
.comment-input-footer{display:flex;align-items:center;justify-content:flex-end;margin-top:0}
.comment-status{display:none;font-size:12px;color:#94a3b8}
.comment-submit-btn{min-height:40px;padding:8px 20px;background:#3b82f6;color:#fff;border:none;border-radius:6px;font-size:13px;font-weight:500;cursor:pointer;transition:all .2s}
.comment-submit-btn:hover{background:#2563eb}
.comment-submit-btn:disabled{opacity:.5;cursor:not-allowed}

.comment-empty{text-align:center;padding:32px 0;color:#94a3b8;font-size:14px}

.comment-list{display:flex;flex-direction:column}
.comment-item{display:flex;gap:12px;padding:16px 0;border-bottom:1px solid #f1f5f9}
.comment-item:last-child{border-bottom:none}
.comment-body{flex:1;min-width:0}
.comment-meta{display:flex;align-items:center;gap:8px;margin-bottom:6px}
.comment-author{font-size:14px;font-weight:600;color:#1e293b}
.comment-text{font-size:14px;color:#334155;line-height:1.7;margin-bottom:8px;white-space:pre-wrap;word-break:break-word}
.comment-actions{display:flex;align-items:center;gap:16px}
.comment-time{font-size:12px;color:#94a3b8}
.action-btn{display:inline-flex;align-items:center;gap:4px;background:none;border:none;cursor:pointer;font-size:12px;color:#94a3b8;padding:4px 6px;border-radius:4px;transition:all .2s}
.action-btn:hover{color:#64748b;background:#f1f5f9}
.action-btn.liked{color:#3b82f6}
.action-btn.disliked{color:#ef4444}
.action-btn.danger{color:#ef4444}
.action-btn.danger:hover{color:#dc2626;background:#fef2f2}
.reply-btn{font-size:12px}

.edit-box{margin-bottom:8px;padding:10px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px}
.edit-box.compact{padding:8px}
.edit-textarea{width:100%;min-height:48px;border:none;background:transparent;outline:none;resize:none;color:#1e293b;font-size:14px;line-height:1.6;font-family:inherit}
.edit-actions{display:flex;justify-content:flex-end;gap:8px;margin-top:8px}

.reply-input-row{margin-top:12px;padding:12px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px}
.reply-textarea{width:100%;border:none;background:transparent;outline:none;font-size:13px;line-height:1.5;resize:none;color:#1e293b;font-family:inherit;min-height:20px}
.reply-textarea::placeholder{color:#94a3b8}
.reply-input-footer{display:flex;align-items:center;justify-content:flex-end;gap:8px;margin-top:8px}
.reply-cancel-btn{padding:4px 12px;background:#2563eb;color:#fff;border:1px solid #2563eb;border-radius:4px;font-size:12px;cursor:pointer;transition:all .2s}
.reply-cancel-btn:hover{background:#1d4ed8}
.reply-submit-btn{padding:4px 12px;background:#3b82f6;color:#fff;border:none;border-radius:4px;font-size:12px;font-weight:500;cursor:pointer;transition:all .2s}
.reply-submit-btn:hover{background:#2563eb}
.reply-submit-btn:disabled{opacity:.5;cursor:not-allowed}

.reply-toggle-btn{margin-top:10px;background:#f8fafc;border:1px solid #e2e8f0;color:#64748b;border-radius:6px;padding:5px 10px;font-size:12px;cursor:pointer;transition:all .2s}
.reply-toggle-btn:hover{background:#f1f5f9;color:#1e293b}
.reply-list{margin-top:12px;padding-left:12px;border-left:2px solid #e2e8f0}
.reply-item{display:flex;gap:10px;padding:10px 0;border-bottom:1px solid #f1f5f9}
.reply-item:last-child{border-bottom:none}
.reply-body{flex:1;min-width:0}
</style>
