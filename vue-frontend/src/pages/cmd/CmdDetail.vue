<template>
  <MainLayout>
    <div class="max-w-[960px] mx-auto export-pdf-area">
      <div class="flex justify-between items-center mb-6 flex-wrap gap-3">
        <h2 class="text-[26px] font-bold">{{ item?.title || '加载中...' }}</h2>
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
              <div class="font-semibold text-sm text-slate-700 mb-2">配置命令</div>
              <pre class="code-block">{{ cfg.config }}</pre>
            </div>
            <div v-if="cfg.comment" class="mb-3 px-3.5 py-2.5 rounded-md bg-blue-50 text-blue-600 text-sm"><strong>配置说明：</strong>{{ cfg.comment }}</div>
            <div v-if="cfg.doc" class="mb-3 text-sm"><strong>参考文档：</strong><a :href="cfg.doc" target="_blank" class="text-blue-600 no-underline hover:underline">{{ cfg.doc }}</a></div>
            <div v-if="cfg.verificationCmd">
              <div class="font-semibold text-sm text-slate-700 mb-2">验证命令</div>
              <pre class="code-block">{{ cfg.verificationCmd }}</pre>
            </div>
          </div>
        </div>

        <div v-if="item?.detail" class="detail-section"><div class="detail-label">详细内容</div><div class="detail-value whitespace-pre-wrap">{{ item.detail }}</div></div>

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

function loadHtml2Pdf() {
  return new Promise((resolve, reject) => {
    if (window.html2pdf) { resolve(); return }
    const s = document.createElement('script')
    s.src = 'https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js'
    s.onload = resolve; s.onerror = reject; document.head.appendChild(s)
  })
}

async function exportPDF() {
  const btn = document.querySelector('.btn-pdf')
  if (btn) { btn.textContent = '生成中...'; btn.disabled = true }
  try {
    await loadHtml2Pdf()
    const el = document.querySelector('.export-pdf-area')
    const opt = { margin: [10, 10, 10, 10], filename: (item.value?.title || '导出') + '.pdf', image: { type: 'jpeg', quality: 0.95 }, html2canvas: { scale: 2, useCORS: true, logging: false }, jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' } }
    await window.html2pdf().set(opt).from(el).save()
  } catch (e) { alert('PDF生成失败，请检查网络连接后重试') }
  finally { if (btn) { btn.textContent = '导出PDF'; btn.disabled = false } }
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
.detail-label{font-size:13px;font-weight:600;color:var(--text-muted);text-transform:uppercase;letter-spacing:.5px;margin-bottom:8px}
.detail-value{font-size:16px;color:var(--text);line-height:1.8;white-space:pre-wrap}
.detail-footer{display:flex;align-items:center;gap:10px;flex-wrap:wrap;padding:16px 24px;background:var(--bg-white);border:1.5px solid var(--border);border-radius:var(--radius);box-shadow:var(--shadow-sm)}
.tag-time{font-size:13px;padding:4px 12px;border-radius:12px;font-weight:500;background:#f0f9ff;color:#2563eb;border:1.5px solid #bfdbfe;display:inline-block}
.tag-vendor{display:inline-block;padding:4px 12px;border-radius:12px;font-size:13px;font-weight:500;border:1.5px solid}
.tag-cat{font-size:13px;padding:4px 12px;border-radius:12px;font-weight:500;background:#fff7ed;color:#ea580c;border:1.5px solid #fed7aa;display:inline-block}
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
</style>
