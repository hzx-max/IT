<template>
  <MainLayout>
    <div class="max-w-[960px] mx-auto export-pdf-area">
      <div class="flex justify-between items-center mb-6 flex-wrap gap-3">
        <h2 class="text-[26px] font-bold">{{ item?.title || '加载中...' }}</h2>
        <div class="flex gap-2.5 flex-wrap items-center">
          <button class="btn btn-primary text-sm" @click="$router.push('/desktop/edit/'+$route.params.id)" v-if="item?.id">编辑</button>
          <button class="btn btn-pdf text-sm" @click="exportPDF">导出PDF</button>
          <button class="btn btn-ghost text-sm" @click="$router.back()">&larr; 返回</button>
        </div>
      </div>

      <div v-if="loading" class="text-center py-20 text-slate-400"><div class="w-9 h-9 mx-auto mb-4 border-3 border-slate-200 border-t-blue-600 rounded-full animate-spin"></div>加载中...</div>
      <div v-else-if="error" class="text-center py-20 text-red-500">{{ error }}</div>

      <template v-else-if="item">
        <div class="detail-section">
          <div class="detail-label">图片/视频</div>
          <div class="media-grid">
            <img v-for="(url, i) in (item.images || [])" :key="'img-'+i" :src="url" class="media-img" @click="openViewer(url,'image')">
            <video v-for="(url, i) in (item.videos || [])" :key="'vid-'+i" :src="url" class="media-video" controls></video>
          </div>
          <div v-if="(!item.images || item.images.length === 0) && (!item.videos || item.videos.length === 0)" class="detail-value text-slate-400">暂无图片/视频</div>
        </div>

        <div class="detail-section"><div class="detail-label">故障现象</div><div class="detail-value">{{ item.symptom || '暂无' }}</div></div>
        <div class="detail-section"><div class="detail-label">解决方案</div><div class="detail-value">{{ item.solution || '暂无' }}</div></div>

        <div class="detail-section">
          <div class="detail-label">附件</div>
          <div class="file-list" v-if="item.files && item.files.length > 0">
            <div v-for="(f, i) in item.files" :key="'file-'+i" class="file-row">
              <div class="file-icon" :style="{ background: getFileIconBg(f.name), color: getFileIconColor(f.name) }">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
                </svg>
              </div>
              <div class="file-info">
                <div class="file-name" :title="f.name">{{ f.name }}</div>
                <div class="file-meta">{{ formatFileSize(f.size) }} · {{ getFileExtLabel(f.name) }}</div>
              </div>
              <a :href="f.url" :download="f.name" class="file-download-btn" @click.stop>下载</a>
            </div>
          </div>
          <div v-else class="detail-value text-slate-400">暂无附件</div>
        </div>

        <div class="detail-footer">
          <span class="tag-time">{{ formatTime(item.createdAt) }}</span>
          <span class="tag-cat">{{ item.category }}</span>
        </div>
      </template>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '../../layouts/MainLayout.vue'
import { apiDesktop, formatTime } from '../../api/index.js'
import { exportPdf } from '../../utils/pdfExport.js'

const route = useRoute()
const item = ref(null)
const loading = ref(true)
const error = ref('')

function openViewer(url, type) {
  window.open(url, '_blank')
}

const extColors = {
  pdf: { bg: '#fef2f2', color: '#ef4444' },
  doc: { bg: '#eff6ff', color: '#3b82f6' },
  docx: { bg: '#eff6ff', color: '#3b82f6' },
  xls: { bg: '#ecfdf5', color: '#10b981' },
  xlsx: { bg: '#ecfdf5', color: '#10b981' },
  csv: { bg: '#ecfdf5', color: '#10b981' },
  ppt: { bg: '#fff7ed', color: '#f97316' },
  pptx: { bg: '#fff7ed', color: '#f97316' },
  zip: { bg: '#f8fafc', color: '#64748b' },
  rar: { bg: '#f8fafc', color: '#64748b' },
  '7z': { bg: '#f8fafc', color: '#64748b' },
  tar: { bg: '#f8fafc', color: '#64748b' },
  gz: { bg: '#f8fafc', color: '#64748b' },
  txt: { bg: '#f8fafc', color: '#64748b' },
  md: { bg: '#f8fafc', color: '#64748b' },
}

function getFileExt(name) {
  const idx = (name || '').lastIndexOf('.')
  return idx >= 0 ? name.slice(idx + 1).toLowerCase() : ''
}

function getFileIconColor(name) {
  const ext = getFileExt(name)
  return extColors[ext]?.color || '#64748b'
}

function getFileIconBg(name) {
  const ext = getFileExt(name)
  return extColors[ext]?.bg || '#f8fafc'
}

const extLabels = { pdf: 'PDF', doc: 'Word', docx: 'Word', xls: 'Excel', xlsx: 'Excel', csv: 'CSV', ppt: 'PPT', pptx: 'PPT', zip: '压缩包', rar: '压缩包', '7z': '压缩包', tar: '压缩包', gz: '压缩包', txt: '文本', md: 'Markdown', jpg: '图片', jpeg: '图片', png: '图片', gif: '图片', webp: '图片', mp4: '视频', avi: '视频', mov: '视频', mp3: '音频', wav: '音频' }

function getFileExtLabel(name) {
  const ext = getFileExt(name)
  return extLabels[ext] || ext.toUpperCase() || '文件'
}

function formatFileSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

async function exportPDF() {
  await exportPdf({
    title: item.value?.title || '桌面运维',
    sections: [
      { label: '图片/视频', type: 'media', value: [...(item.value?.images || []), ...(item.value?.videos || [])] },
      { label: '故障现象', type: 'text', value: item.value?.symptom },
      { label: '解决方案', type: 'text', value: item.value?.solution },
      { label: '附件', type: 'files', value: item.value?.files }
    ],
    footer: `创建时间: ${formatTime(item.value?.createdAt)}`
  }, item.value?.title || '桌面运维')
}

onMounted(async () => {
  try {
    const res = await apiDesktop.get(route.params.id)
    item.value = res.data
    if (item.value) {
      document.title = item.value.title + ' - IT运维学习平台'
    }
  } catch (e) { error.value = '加载失败: ' + e.message }
  finally { loading.value = false }
})
</script>

<style scoped>
.detail-section{background:var(--bg-white);border:1.5px solid var(--border);border-radius:var(--radius);padding:24px;margin-bottom:16px;box-shadow:var(--shadow-sm);transition:var(--transition-normal)}
.detail-section:hover{box-shadow:var(--shadow-md)}
.detail-label{font-size:13px;font-weight:600;color:var(--text-muted);text-transform:uppercase;letter-spacing:.5px;margin-bottom:8px}
.detail-value{font-size:16px;color:var(--text);line-height:1.8;white-space:pre-wrap}
.detail-footer{display:flex;align-items:center;gap:10px;flex-wrap:wrap;padding:16px 24px;background:var(--bg-white);border:1.5px solid var(--border);border-radius:var(--radius);box-shadow:var(--shadow-sm)}
.tag-time{font-size:13px;padding:4px 12px;border-radius:12px;font-weight:500;background:#f0f9ff;color:#2563eb;border:1.5px solid #bfdbfe;display:inline-block}
.tag-cat{font-size:13px;padding:4px 12px;border-radius:12px;font-weight:500;background:#fff7ed;color:#ea580c;border:1.5px solid #fed7aa;display:inline-block}
.media-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:10px}
.media-img{width:100%;border-radius:6px;cursor:pointer;border:1.5px solid #e2e8f0;transition:transform .2s}
.media-img:hover{transform:scale(1.02);box-shadow:0 4px 12px rgba(0,0,0,.1)}
.media-video{width:100%;border-radius:6px;border:1.5px solid #e2e8f0}
.btn{display:inline-flex;align-items:center;gap:6px;padding:9px 18px;border-radius:8px;font-size:15px;cursor:pointer;font-weight:500;transition:all .25s ease}
.btn-primary{background:#2563eb;color:#fff;border:1.5px solid #2563eb}
.btn-primary:hover{background:#1d4ed8;border-color:#1d4ed8;box-shadow:0 4px 12px rgba(37,99,235,.3)}
.btn-pdf{background:#2563eb;color:#fff;border:1.5px solid #2563eb}
.btn-pdf:hover{background:#1d4ed8;border-color:#1d4ed8;box-shadow:0 4px 12px rgba(37,99,235,.3)}
.btn-pdf:disabled{opacity:.6;cursor:not-allowed}
.btn-ghost{background:var(--bg-white);border:1.5px solid var(--border);color:var(--text-muted);text-decoration:none}
.btn-ghost:hover{border-color:var(--primary);color:var(--primary);background:var(--primary-light);transform:translateX(-2px)}
/* 附件列表 */
.file-list{display:flex;flex-direction:column;gap:8px}
.file-row{display:flex;align-items:center;gap:12px;padding:10px 14px;border:1.5px solid #e2e8f0;border-radius:8px;transition:all .2s}
.file-row:hover{border-color:#93c5fd;background:#f8fafc}
.file-icon{width:38px;height:38px;border-radius:8px;display:flex;align-items:center;justify-content:center;flex-shrink:0}
.file-info{flex:1;min-width:0}
.file-name{font-size:14px;font-weight:500;color:#1e293b;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.file-meta{font-size:12px;color:#94a3b8;margin-top:2px}
.file-download-btn{display:inline-flex;align-items:center;gap:4px;padding:6px 14px;background:#2563eb;color:#fff;border:none;border-radius:6px;font-size:13px;font-weight:500;cursor:pointer;text-decoration:none;transition:all .2s;flex-shrink:0}
.file-download-btn:hover{background:#1d4ed8}
</style>
