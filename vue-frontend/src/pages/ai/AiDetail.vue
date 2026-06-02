<template>
  <MainLayout>
    <div class="max-w-[960px] mx-auto export-pdf-area">
      <div class="flex justify-between items-center mb-6 flex-wrap gap-3">
        <h2 class="text-[26px] font-bold">{{ item?.title || '加载中...' }}</h2>
        <div class="flex gap-2.5 flex-wrap items-center">
          <button class="btn btn-primary text-sm" @click="$router.push('/ai/edit/'+$route.params.id)" v-if="item?.id">编辑</button>
          <button class="btn btn-pdf text-sm" @click="exportPDF">导出PDF</button>
          <button class="btn btn-ghost text-sm" @click="$router.back()">&larr; 返回</button>
        </div>
      </div>

      <div v-if="loading" class="text-center py-20 text-slate-400"><div class="w-9 h-9 mx-auto mb-4 border-3 border-slate-200 border-t-purple-600 rounded-full animate-spin"></div>加载中...</div>
      <div v-else-if="error" class="text-center py-20 text-red-500">{{ error }}</div>

      <template v-else-if="item">
        <div v-if="item?.scenario" class="detail-section"><div class="detail-label">AI场景</div><div class="detail-value">{{ item.scenario }}</div></div>

        <div v-if="(item?.images && item.images.length > 0) || (item?.videos && item.videos.length > 0)" class="detail-section">
          <div class="detail-label">图片/视频</div>
          <div class="media-grid">
            <img v-for="(url, i) in item.images" :key="'img-'+i" :src="url" class="media-img" @click="openViewer(url)">
            <video v-for="(url, i) in item.videos" :key="'vid-'+i" :src="url" class="media-video" controls></video>
          </div>
        </div>

        <div v-if="item?.prompt" class="detail-section"><div class="detail-label">提示词</div><pre class="code-block">{{ item.prompt }}</pre></div>
        <div v-if="item?.config" class="detail-section"><div class="detail-label">配置</div><pre class="code-block">{{ item.config }}</pre></div>
        <div v-if="item?.desc" class="detail-section"><div class="detail-label">描述</div><div class="detail-value">{{ item.desc }}</div></div>
        <div v-if="item?.detail" class="detail-section"><div class="detail-label">详细内容</div><div class="detail-value">{{ item.detail }}</div></div>
        <div class="detail-footer">
          <span v-if="item?.createdAt" class="tag-time">{{ formatTime(item.createdAt) }}</span>
          <span v-if="item?.category" class="tag-cat">{{ item.category }}</span>
        </div>
      </template>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '../../layouts/MainLayout.vue'
import { apiAi, formatTime } from '../../api/index.js'

const route = useRoute()
const item = ref(null)
const loading = ref(true)
const error = ref('')

function openViewer(url) {
  window.open(url, '_blank')
}

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

onMounted(async () => {
  try {
    const res = await apiAi.get(route.params.id)
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
.code-block{font-family:'Cascadia Code','Fira Code',Consolas,monospace;font-size:14px;background:#fff;color:#000;padding:16px;border-radius:6px;border:1.5px solid #e2e8f0;white-space:pre-wrap;line-height:1.7;margin:0}
.detail-footer{display:flex;align-items:center;gap:10px;flex-wrap:wrap;padding:16px 24px;background:var(--bg-white);border:1.5px solid var(--border);border-radius:var(--radius);box-shadow:var(--shadow-sm)}
.tag-time{font-size:13px;padding:4px 12px;border-radius:12px;font-weight:500;background:#f5f3ff;color:#7c3aed;border:1.5px solid #ddd6fe;display:inline-block}
.tag-cat{font-size:13px;padding:4px 12px;border-radius:12px;font-weight:500;background:#f5f3ff;color:#7c3aed;border:1.5px solid #ddd6fe;display:inline-block}
.media-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:10px}
.media-img{width:100%;border-radius:6px;cursor:pointer;border:1.5px solid #e2e8f0;transition:transform .2s}
.media-img:hover{transform:scale(1.02);box-shadow:0 4px 12px rgba(0,0,0,.1)}
.media-video{width:100%;border-radius:6px;border:1.5px solid #e2e8f0}
.btn{display:inline-flex;align-items:center;gap:6px;padding:9px 18px;border-radius:8px;font-size:15px;cursor:pointer;font-weight:500;transition:all .25s ease}
.btn-primary{background:#7c3aed;color:#fff;border:1.5px solid #7c3aed}
.btn-primary:hover{background:#6d28d9;border-color:#6d28d9;box-shadow:0 4px 12px rgba(124,58,237,.3)}
.btn-pdf{background:#7c3aed;color:#fff;border:1.5px solid #7c3aed}
.btn-pdf:hover{background:#6d28d9;border-color:#6d28d9;box-shadow:0 4px 12px rgba(124,58,237,.3)}
.btn-pdf:disabled{opacity:.6;cursor:not-allowed}
.btn-ghost{background:var(--bg-white);border:1.5px solid var(--border);color:var(--text-muted);text-decoration:none}
.btn-ghost:hover{border-color:#7c3aed;color:#7c3aed;background:#f5f3ff;transform:translateX(-2px)}
</style>