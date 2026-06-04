<template>
  <MainLayout>
    <div class="max-w-[960px] mx-auto export-pdf-area">
      <div class="flex justify-between items-center mb-6 flex-wrap gap-3">
        <h2 class="text-[28px] font-bold">{{ item?.title || '加载中...' }}</h2>
        <div class="flex gap-2.5 flex-wrap items-center">
          <button class="btn btn-primary text-sm" @click="$router.push('/cmd/edit/'+$route.params.id)" v-if="item?.id">编辑</button>
          <button class="btn btn-pdf text-sm" @click="exportPDF">导出PDF</button>
          <button class="btn btn-ghost text-sm" @click="$router.back()">← 返回</button>
        </div>
      </div>

      <template v-if="loading">
        <div class="text-center py-20 text-slate-400"><div class="w-9 h-9 mx-auto mb-4 border-3 border-slate-200 border-t-blue-600 rounded-full animate-spin"></div>加载中...</div>
      </template>
      <template v-else-if="error">
        <div class="text-center py-20 text-red-500">{{ error }}</div>
      </template>
      <template v-else-if="item">
        <div v-if="item?.desc" class="detail-section"><div class="detail-label">描述</div><div class="detail-value">{{ item.desc }}</div></div>

        <div v-if="item?.detail" class="detail-section"><div class="detail-label">详细内容</div><div class="detail-value whitespace-pre-wrap">{{ item.detail }}</div></div>

        <!-- 拓扑图 -->
        <div v-if="topoImages.length > 0" class="detail-section">
          <div class="detail-label">拓扑图</div>
          <div class="image-grid">
            <div v-for="(img, idx) in topoImages" :key="'topo-'+idx" class="image-item">
              <img :src="img.url" :alt="img.name || '拓扑图'" class="image-thumb" @click="previewImage(img.url)" @error="onImageError" loading="lazy" />
              <span class="image-name">{{ img.name || '拓扑图' + (idx+1) }}</span>
            </div>
          </div>
        </div>

        <div v-if="configs.length > 0" class="detail-section">
          <div class="detail-label mb-3">厂商配置</div>
          <div class="flex gap-1.5 flex-wrap mb-[18px]">
            <button v-for="(cfg,i) in configs" :key="cfg.vendor"
              class="vendor-tab" :class="{ active: activeConfig === i }"
              :style="activeConfig === i ? { background: getVendorColor(cfg.vendor, VENDOR_MAP), borderColor: getVendorColor(cfg.vendor, VENDOR_MAP), color: '#fff' } : {}"
              @click="activeConfig = i">
              <span class="inline-block w-[10px] h-[10px] rounded-full mr-1.5 align-middle" :style="{ background: getVendorColor(cfg.vendor, VENDOR_MAP) }"></span>
              {{ getVendorName(cfg.vendor, VENDOR_MAP) }}
            </button>
          </div>
          <div v-for="(cfg,i) in configs" :key="cfg.vendor" v-show="activeConfig === i">
            <div v-if="cfg.config" class="mb-4">
              <div class="detail-sublabel">配置命令</div>
              <pre class="code-block">{{ cfg.config }}</pre>
            </div>
            <div v-if="cfg.comment" class="mb-3"><div class="detail-sublabel">配置说明</div><div class="detail-value">{{ cfg.comment }}</div></div>
            <div v-if="cfg.doc" class="mb-3"><div class="detail-sublabel">参考文档</div><div class="detail-value"><a :href="cfg.doc" target="_blank" class="text-blue-600 no-underline hover:underline">{{ cfg.doc }}</a></div></div>
            <div v-if="cfg.verificationCmd">
              <div class="detail-sublabel">验证命令</div>
              <pre class="code-block">{{ cfg.verificationCmd }}</pre>
            </div>
            <!-- 验证命令图片 -->
            <div v-if="getVerificationImages(cfg).length > 0" class="mt-4">
              <div class="detail-sublabel">验证截图</div>
              <div class="image-grid">
                <div v-for="(img, idx) in getVerificationImages(cfg)" :key="'verify-'+idx" class="image-item">
                  <img :src="img.url" :alt="img.name || '验证截图'" class="image-thumb" @click="previewImage(img.url)" @error="onImageError" loading="lazy" />
                  <span class="image-name">{{ img.name || '截图' + (idx+1) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="item?.files && item.files.length > 0" class="detail-section">
          <div class="detail-label">附件</div>
          <div class="file-list">
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
        </div>

        <div class="detail-section">
          <div class="detail-label mb-2">学习笔记</div>
          <textarea v-model="noteContent" class="w-full px-3.5 py-[11px] border border-slate-200 rounded-md text-sm outline-none font-inherit resize-y transition-all duration-200 focus:border-blue-500 focus:shadow-[0_0_0_3px_rgba(37,99,235,.12)]" rows="4" placeholder="在此记录你的学习笔记..."></textarea>
          <div class="mt-2 flex gap-2 items-center justify-end">
            <span class="text-sm text-slate-500">{{ noteStatus }}</span>
            <button class="btn btn-primary text-xs !py-1.5 !px-4" @click="saveNote">保存笔记</button>
          </div>
        </div>

        <div class="detail-footer">
          <span v-if="item?.createdAt" class="tag-time">{{ formatTime(item.createdAt) }}</span>
          <span v-for="cfg in configs" :key="cfg.vendor" class="tag-vendor" :style="{ background: getVendorColor(cfg.vendor, VENDOR_MAP)+'15', color: getVendorColor(cfg.vendor, VENDOR_MAP), borderColor: getVendorColor(cfg.vendor, VENDOR_MAP)+'40' }">{{ getVendorName(cfg.vendor, VENDOR_MAP) }}</span>
          <span v-if="item?.cat" class="tag-cat">{{ getCatLabel(item.cat, CAT_MAP) }}</span>
        </div>
      </template>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '../../layouts/MainLayout.vue'
import { VENDOR_MAP, CAT_MAP, getVendorName, getVendorColor, getCatLabel, formatTime, apiTopics, apiNotes } from '../../api/index.js'
import { exportPdf } from '../../utils/pdfExport.js'

const route = useRoute()
const item = ref(null)
const loading = ref(true)
const error = ref('')
const activeConfig = ref(0)
const noteContent = ref('')
const noteStatus = ref('')

const extColors = {
  pdf: { bg: '#fef2f2', color: '#ef4444' },
  doc: { bg: '#eff6ff', color: '#3b82f6' }, docx: { bg: '#eff6ff', color: '#3b82f6' },
  xls: { bg: '#ecfdf5', color: '#10b981' }, xlsx: { bg: '#ecfdf5', color: '#10b981' }, csv: { bg: '#ecfdf5', color: '#10b981' },
  ppt: { bg: '#fff7ed', color: '#f97316' }, pptx: { bg: '#fff7ed', color: '#f97316' },
  zip: { bg: '#f8fafc', color: '#64748b' }, rar: { bg: '#f8fafc', color: '#64748b' },
  '7z': { bg: '#f8fafc', color: '#64748b' }, tar: { bg: '#f8fafc', color: '#64748b' }, gz: { bg: '#f8fafc', color: '#64748b' },
  txt: { bg: '#f8fafc', color: '#64748b' }, md: { bg: '#f8fafc', color: '#64748b' },
}
function getFileExt(name) { const idx = (name || '').lastIndexOf('.'); return idx >= 0 ? name.slice(idx + 1).toLowerCase() : '' }
function getFileIconColor(name) { return extColors[getFileExt(name)]?.color || '#64748b' }
function getFileIconBg(name) { return extColors[getFileExt(name)]?.bg || '#f8fafc' }
const extLabels = { pdf:'PDF',doc:'Word',docx:'Word',xls:'Excel',xlsx:'Excel',csv:'CSV',ppt:'PPT',pptx:'PPT',zip:'压缩包',rar:'压缩包','7z':'压缩包',tar:'压缩包',gz:'压缩包',txt:'文本',md:'Markdown',jpg:'图片',jpeg:'图片',png:'图片',gif:'图片',webp:'图片',mp4:'视频',avi:'视频',mov:'视频',mp3:'音频',wav:'音频' }
function getFileExtLabel(name) { return extLabels[getFileExt(name)] || getFileExt(name).toUpperCase() || '文件' }
function formatFileSize(bytes) { if(!bytes) return ''; if(bytes<1024) return bytes+' B'; if(bytes<1048576) return (bytes/1024).toFixed(1)+' KB'; return (bytes/1048576).toFixed(1)+' MB' }

const configs = computed(() => {
  if (!item.value?.configs) return []
  const arr = typeof item.value.configs === 'string' ? JSON.parse(item.value.configs) : item.value.configs
  return (Array.isArray(arr) ? arr : []).sort((a, b) => {
    const keys = Object.keys(VENDOR_MAP)
    return keys.indexOf(a.vendor) - keys.indexOf(b.vendor)
  })
})

const topoImages = computed(() => {
  if (!item.value?.topo) return []
  const arr = typeof item.value.topo === 'string' ? JSON.parse(item.value.topo) : item.value.topo
  return Array.isArray(arr) ? arr.filter(f => f.type === 'image' || f.type?.startsWith('image/') || (!f.type && f.url)) : []
})

function getVerificationImages(cfg) {
  if (!cfg) return []
  const key = cfg.verificationImages !== undefined ? cfg.verificationImages : cfg.verification_images
  if (!key) return []
  const arr = typeof key === 'string' ? JSON.parse(key) : key
  return Array.isArray(arr) ? arr.filter(f => f.type === 'image' || f.type?.startsWith('image/') || (!f.type && f.url)) : []
}

function previewImage(url) {
  window.open(url, '_blank')
}

function onImageError(e) {
  if (e.target) {
    e.target.style.display = 'none'
  }
}

async function exportPDF() {
  // 构建网络命令的 PDF 数据
  const sections = []

  if (item.value?.desc) {
    sections.push({ label: '描述', type: 'text', value: item.value.desc })
  }
  if (item.value?.detail) {
    sections.push({ label: '详细内容', type: 'text', value: item.value.detail })
  }

  // 拓扑图
  const topoImgs = topoImages.value
  if (topoImgs.length > 0) {
    sections.push({ label: '拓扑图', type: 'media', value: topoImgs.map(img => img.url) })
  }

  // 厂商配置
  for (const cfg of configs.value) {
    const vendorName = getVendorName(cfg.vendor, VENDOR_MAP)
    if (cfg.config) {
      sections.push({ label: `${vendorName} - 配置命令`, type: 'code', value: cfg.config })
    }
    if (cfg.comment) {
      sections.push({ label: `${vendorName} - 配置说明`, type: 'text', value: cfg.comment })
    }
    const verifyImgs = getVerificationImages(cfg)
    if (verifyImgs.length > 0) {
      sections.push({ label: `${vendorName} - 验证截图`, type: 'media', value: verifyImgs.map(img => img.url) })
    }
  }

  if (item.value?.files?.length > 0) {
    sections.push({ label: '附件', type: 'files', value: item.value.files })
  }

  await exportPdf({
    title: item.value?.title || '网络命令',
    sections,
    footer: `创建时间: ${formatTime(item.value?.createdAt)}`
  }, item.value?.title || '网络命令')
}

async function loadNote() {
  try {
    const res = await apiNotes.get(route.params.id)
    if (res.data?.content) { noteContent.value = res.data.content; noteStatus.value = '上次保存: ' + new Date().toLocaleString() }
  } catch {}
}

async function saveNote() {
  try {
    await apiNotes.save(route.params.id, noteContent.value)
    noteStatus.value = '已保存 ' + new Date().toLocaleString()
  } catch { alert('保存失败') }
}

onMounted(async () => {
  try {
    const res = await apiTopics.get(route.params.id)
    item.value = res.data
    if (item.value) {
      document.title = item.value.title + ' - IT运维学习平台'
      loadNote()
    }
  } catch (e) { error.value = '加载失败: ' + e.message }
  finally { loading.value = false }
})
</script>

<style scoped>
.detail-section{background:var(--bg-white);border:1.5px solid var(--border);border-radius:var(--radius);padding:24px;margin-bottom:16px;box-shadow:var(--shadow-sm);transition:var(--transition-normal)}
.detail-section:hover{box-shadow:var(--shadow-md)}
.detail-label{font-size:22px;font-weight:600;color:var(--text-muted);text-transform:uppercase;letter-spacing:.5px;margin-bottom:8px}
.detail-sublabel{font-size:18px;font-weight:600;color:#475569;margin-bottom:6px;font-family:"Times New Roman","宋体",SimSun,serif}
.detail-value{font-size:18px;font-family:"Times New Roman","宋体",SimSun,serif;color:var(--text);line-height:1.8;white-space:pre-wrap;text-indent:2em}
.detail-footer{display:flex;align-items:center;gap:10px;flex-wrap:wrap;padding:16px 24px;background:var(--bg-white);border:1.5px solid var(--border);border-radius:var(--radius);box-shadow:var(--shadow-sm)}
.tag-time{font-size:14px;padding:4px 12px;border-radius:12px;font-weight:500;background:#f0f9ff;color:#2563eb;border:1.5px solid #bfdbfe;display:inline-block}
.tag-vendor{display:inline-block;padding:4px 12px;border-radius:12px;font-size:14px;font-weight:500;border:1.5px solid}
.tag-cat{font-size:14px;padding:4px 12px;border-radius:12px;font-weight:500;background:#fff7ed;color:#ea580c;border:1.5px solid #fed7aa;display:inline-block}
.vendor-tab{padding:8px 18px;border:1.5px solid var(--border);border-radius:var(--radius-sm);background:var(--bg-white);color:var(--text-muted);font-size:15px;cursor:pointer;font-weight:500;transition:var(--transition-normal)}
.vendor-tab:hover{border-color:var(--primary);color:var(--primary);background:var(--primary-light)}
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
/* 图片网格 */
.image-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:12px}
.image-item{display:flex;flex-direction:column;align-items:center;border:1.5px solid #e2e8f0;border-radius:8px;overflow:hidden;transition:all .2s;cursor:pointer}
.image-item:hover{border-color:#93c5fd;box-shadow:0 4px 12px rgba(37,99,235,.12)}
.image-thumb{width:100%;height:140px;object-fit:cover;display:block}
.image-name{font-size:12px;color:#64748b;padding:6px 8px;text-align:center;width:100%;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
</style>
