<template>
  <div class="file-uploader">
    <div class="upload-area" @click="triggerInput" @dragover.prevent @drop.prevent="onDrop">
      <input type="file" ref="fileInput" multiple @change="onChange" class="hidden-input">
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"/>
      </svg>
      <span class="upload-text">点击或拖拽上传附件</span>
      <span class="upload-hint">支持 PDF、Word、Excel、PPT、TXT、ZIP、图片、视频、代码等主流格式</span>
    </div>
    <div v-if="uploading" class="upload-status">
      <span class="upload-spinner"></span>
      上传中...
    </div>
    <div v-if="files.length > 0" class="file-list">
      <div v-for="(f, i) in files" :key="i" class="file-item" :class="{ 'file-item-new': f._justUploaded }">
        <div class="file-icon" :style="{ background: getFileTypeInfo(f.name).bg + '18', color: getFileTypeInfo(f.name).bg }">
          <!-- Image -->
          <svg v-if="getFileTypeInfo(f.name).type === 'image'" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/>
          </svg>
          <!-- Video -->
          <svg v-else-if="getFileTypeInfo(f.name).type === 'video'" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <polygon points="5 3 19 12 5 21 5 3" fill="currentColor" opacity="0.3"/><polygon points="5 3 19 12 5 21 5 3" fill="none"/>
          </svg>
          <!-- PDF -->
          <svg v-else-if="getFileTypeInfo(f.name).type === 'pdf'" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="13" x2="15" y2="13"/><line x1="9" y1="17" x2="13" y2="17"/>
          </svg>
          <!-- Word -->
          <svg v-else-if="getFileTypeInfo(f.name).type === 'word'" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="8" y1="13" x2="16" y2="13"/><line x1="8" y1="17" x2="16" y2="17"/>
          </svg>
          <!-- Excel -->
          <svg v-else-if="getFileTypeInfo(f.name).type === 'excel'" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="8" y1="13" x2="16" y2="13"/><line x1="8" y1="17" x2="16" y2="17"/><line x1="8" y1="9" x2="16" y2="9"/>
          </svg>
          <!-- PPT -->
          <svg v-else-if="getFileTypeInfo(f.name).type === 'ppt'" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><rect x="8" y="10" width="8" height="8" rx="1"/>
          </svg>
          <!-- Code -->
          <svg v-else-if="getFileTypeInfo(f.name).type === 'code'" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/>
          </svg>
          <!-- Archive -->
          <svg v-else-if="getFileTypeInfo(f.name).type === 'archive'" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M21 8v13H3V8"/><path d="M1 3h22v5H1z"/><line x1="10" y1="12" x2="14" y2="12"/>
          </svg>
          <!-- Audio -->
          <svg v-else-if="getFileTypeInfo(f.name).type === 'audio'" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/>
          </svg>
          <!-- Default file -->
          <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
          </svg>
        </div>
        <div class="file-info">
          <div class="file-name" :title="f.name">{{ f.name }}</div>
          <div class="file-meta">
            <span class="file-type-badge">{{ getFileTypeInfo(f.name).label }}</span>
            <span v-if="f.size" class="file-size">{{ formatSize(f.size) }}</span>
            <span v-if="f._justUploaded" class="file-done">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              已上传
            </span>
          </div>
        </div>
        <button class="file-remove" @click.stop="removeFile(i)" title="删除">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { apiUpload } from '../api/index.js'

const props = defineProps({
  modelValue: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue'])

const fileInput = ref(null)
const files = ref([...props.modelValue])
const uploading = ref(false)

function getFileExt(name) {
  const idx = name.lastIndexOf('.')
  return idx >= 0 ? name.slice(idx + 1).toLowerCase() : ''
}

const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico', 'tiff', 'tif', 'heic', 'heif']
const videoExts = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'webm', 'm4v', '3gp', 'mpeg', 'mpg']
const audioExts = ['mp3', 'wav', 'flac', 'aac', 'ogg', 'wma', 'm4a', 'opus']
const pdfExts = ['pdf']
const wordExts = ['doc', 'docx', 'docm', 'odt', 'rtf', 'wps']
const excelExts = ['xls', 'xlsx', 'xlsm', 'csv', 'ods', 'tsv', 'et']
const pptExts = ['ppt', 'pptx', 'pptm', 'odp', 'dps']
const codeExts = ['js', 'jsx', 'ts', 'tsx', 'py', 'java', 'c', 'cpp', 'h', 'hpp', 'cs', 'go', 'rs', 'rb', 'php', 'swift', 'kt', 'scala', 'lua', 'r', 'sql', 'sh', 'bash', 'bat', 'ps1', 'cmd', 'yaml', 'yml', 'json', 'xml', 'html', 'htm', 'css', 'scss', 'less', 'vue', 'svelte', 'toml', 'ini', 'cfg', 'conf', 'log', 'md', 'dockerfile', 'makefile']
const archiveExts = ['zip', 'rar', '7z', 'tar', 'gz', 'bz2', 'xz', 'tgz', 'iso', 'dmg']

function getFileTypeInfo(name) {
  const ext = getFileExt(name)
  const map = [
    { type: 'image',   label: '图片',  bg: '#f59e0b', exts: imageExts },
    { type: 'video',   label: '视频',  bg: '#8b5cf6', exts: videoExts },
    { type: 'audio',   label: '音频',  bg: '#ec4899', exts: audioExts },
    { type: 'pdf',     label: 'PDF',   bg: '#ef4444', exts: pdfExts },
    { type: 'word',    label: 'Word',  bg: '#3b82f6', exts: wordExts },
    { type: 'excel',   label: 'Excel', bg: '#10b981', exts: excelExts },
    { type: 'ppt',     label: 'PPT',   bg: '#f97316', exts: pptExts },
    { type: 'code',    label: '代码',  bg: '#6366f1', exts: codeExts },
    { type: 'archive', label: '压缩包',bg: '#64748b', exts: archiveExts },
  ]
  for (const m of map) {
    if (m.exts.includes(ext)) return m
  }
  return { type: 'file', label: ext.toUpperCase() || '文件', bg: '#94a3b8', exts: [] }
}

function formatSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function triggerInput() { fileInput.value?.click() }

function onChange(e) {
  const selected = Array.from(e.target.files || [])
  if (selected.length) uploadFiles(selected)
  e.target.value = ''
}

function onDrop(e) {
  const selected = Array.from(e.dataTransfer.files)
  if (selected.length) uploadFiles(selected)
}

async function uploadFiles(selected) {
  uploading.value = true
  try {
    const res = await apiUpload.upload(selected)
    const uploaded = res.data.data.map(f => ({
      name: f.originalName,
      url: f.url,
      size: parseInt(f.size) || 0,
      type: f.type,
      _justUploaded: true
    }))
    files.value.push(...uploaded)
    emit('update:modelValue', [...files.value])
    // 3秒后移除"已上传"标记
    uploaded.forEach((f, idx) => {
      setTimeout(() => {
        const fi = files.value.find(x => x.name === f.name && x.url === f.url)
        if (fi) fi._justUploaded = false
      }, 3000 + idx * 300)
    })
  } catch (e) {
    console.error('文件上传失败:', e)
  } finally {
    uploading.value = false
  }
}

function removeFile(i) {
  files.value.splice(i, 1)
  emit('update:modelValue', [...files.value])
}
</script>

<style scoped>
.file-uploader { margin-bottom: 0 }
.upload-area {
  border: 2px dashed #cbd5e1;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all .2s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #94a3b8;
}
.upload-area:hover {
  border-color: #2563eb;
  color: #2563eb;
  background: #f8fafc;
}
.upload-text { font-size: 14px; font-weight: 500 }
.upload-hint { font-size: 12px; color: #94a3b8 }
.hidden-input { display: none }
.upload-status {
  margin-top: 8px;
  font-size: 13px;
  color: #2563eb;
  display: flex;
  align-items: center;
  gap: 6px;
}
.upload-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid #e2e8f0;
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin .6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg) } }
.file-list { margin-top: 10px; display: flex; flex-direction: column; gap: 6px }
.file-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  transition: all .25s ease;
}
.file-item:hover { border-color: #cbd5e1; box-shadow: 0 1px 3px rgba(0,0,0,.04) }
.file-item-new {
  border-color: #86efac;
  background: #f0fdf4;
  animation: filePulse .6s ease;
}
@keyframes filePulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.02); background: #dcfce7; }
  100% { transform: scale(1); background: #f0fdf4; }
}
.file-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform .2s;
}
.file-item:hover .file-icon { transform: scale(1.05) }
.file-info { flex: 1; min-width: 0 }
.file-name {
  font-size: 13px;
  font-weight: 500;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.file-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
  font-size: 11px;
  color: #94a3b8;
}
.file-type-badge {
  padding: 1px 6px;
  border-radius: 3px;
  background: #f1f5f9;
  color: #64748b;
  font-weight: 500;
  font-size: 10px;
}
.file-size { color: #94a3b8 }
.file-done {
  display: flex;
  align-items: center;
  gap: 3px;
  color: #16a34a;
  font-weight: 500;
  animation: fadeIn .3s ease;
}
@keyframes fadeIn { from { opacity: 0 } to { opacity: 1 } }
.file-remove {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: transparent;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  transition: all .2s;
  flex-shrink: 0;
}
.file-remove:hover { background: #fee2e2; color: #dc2626 }
</style>