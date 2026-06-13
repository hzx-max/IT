<template>
  <MainLayout>
    <div class="max-w-[720px] mx-auto">
      <div class="top-bar">
        <h2>编辑办公项目</h2>
      </div>

      <div v-if="loading" class="text-center py-20 text-slate-400"><div class="w-9 h-9 mx-auto mb-4 border-3 border-slate-200 border-t-blue-600 rounded-full animate-spin"></div>加载中...</div>

      <div v-else class="form-card">
        <div class="form-group">
          <label>标题 <span class="text-red-600">*</span></label>
          <input v-model="form.title" class="form-input">
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="form-group">
            <label>厂商 <span class="text-red-600">*</span></label>
            <DropdownSelect v-model="form.vendor" :options="vendorOptions" placeholder="选择厂商" />
          </div>
          <div class="form-group">
            <label>分类 <span class="text-red-600">*</span></label>
            <ComboBox v-model="form.cat" :options="catOptions" placeholder="选择或输入分类" />
          </div>
        </div>

        <div class="form-group">
          <label>描述</label>
          <textarea v-model="form.desc" rows="3" class="form-input"></textarea>
        </div>

        <div class="form-group">
          <label>图片/视频</label>
          <div class="upload-area" @click="triggerMediaInput" @dragover.prevent @drop.prevent="onMediaDrop">
            <input type="file" ref="mediaInput" accept="image/*,video/*" multiple @change="onMediaSelected" class="hidden-input">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
            <span class="upload-text">点击或拖拽上传图片/视频</span>
          </div>
          <div v-if="mediaItems.length > 0" class="preview-grid">
            <div v-for="(item, idx) in mediaItems" :key="idx" class="preview-item">
              <img v-if="item.type === 'image'" :src="item.url" class="preview-img">
              <video v-else :src="item.url" class="preview-video" controls></video>
              <button class="preview-remove" @click="removeMedia(idx)">&times;</button>
            </div>
          </div>
          <div v-if="uploading" class="mt-2 text-sm text-blue-600">上传中...</div>
        </div>

        <div class="form-group">
          <label>详细内容</label>
          <textarea v-model="form.detail" rows="6" class="form-input"></textarea>
        </div>

        <div class="form-group">
          <label>配置内容 <span class="text-red-600">*</span></label>
          <textarea v-model="form.config" rows="5" class="form-input font-mono"></textarea>
        </div>

        <div class="form-actions">
          <button class="btn btn-primary" @click="onSubmit" :disabled="submitting">
            {{ submitting ? '提交中...' : '更新' }}
          </button>
          <button class="btn btn-primary" @click="$router.back()">取消</button>
        </div>

        <div v-if="error" class="mt-4 p-3 bg-red-50 border border-red-200 rounded-md text-red-600 text-sm">{{ error }}</div>
        <div v-if="successMsg" class="mt-4 p-3 bg-emerald-50 border border-emerald-200 rounded-md text-emerald-600 text-sm">{{ successMsg }}</div>
      </div>
    </div>
    <ModalDialog :visible="modal.visible" :message="modal.message" :type="modal.type" @confirm="modal.visible=false" @cancel="modal.visible=false" />
  </MainLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MainLayout from '../../layouts/MainLayout.vue'
import ComboBox from '../../components/ComboBox.vue'
import ModalDialog from '../../components/ModalDialog.vue'
import DropdownSelect from '../../components/DropdownSelect.vue'
import { OFFICE_VENDOR_MAP, OFFICE_CAT_MAP, apiOffice, apiUpload } from '../../api/index.js'
import { submitWithApproval } from '../../api/approval.js'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const submitting = ref(false)
const error = ref('')
const successMsg = ref('')

const form = ref({ id: '', title: '', vendor: '', cat: '', desc: '', detail: '', config: '' })
const vendorOptions = computed(() => Object.entries(OFFICE_VENDOR_MAP).map(([k, v]) => ({ value: k, label: v.n })))
const catOptions = computed(() => Object.entries(OFFICE_CAT_MAP).map(([k, v]) => ({ value: k, label: v })))
const modal = ref({ visible: false, message: '', type: 'confirm' })

const mediaInput = ref(null)
const mediaItems = ref([])
const uploading = ref(false)

function triggerMediaInput() { mediaInput.value?.click() }

async function onMediaSelected(e) {
  const files = Array.from(e.target.files || [])
  if (files.length === 0) return
  await uploadFiles(files)
  e.target.value = ''
}

function onMediaDrop(e) {
  const files = Array.from(e.dataTransfer.files).filter(f => f.type.startsWith('image/') || f.type.startsWith('video/'))
  if (files.length > 0) uploadFiles(files)
}

async function uploadFiles(files) {
  uploading.value = true
  try {
    const res = await apiUpload.upload(files)
    const uploadedItems = res.data.data.map(f => ({
      url: f.url,
      type: f.type && f.type.startsWith('image/') ? 'image' : 'video'
    }))
    mediaItems.value.push(...uploadedItems)
  } catch (e) {
    error.value = '上传失败: ' + (e.response?.data?.msg || e.message)
  } finally {
    uploading.value = false
  }
}

function removeMedia(idx) { mediaItems.value.splice(idx, 1) }

onMounted(async () => {
  try {
    const res = await apiOffice.get(route.params.id)
    const d = res.data
    form.value.id = d.id
    form.value.title = d.title
    form.value.vendor = d.vendor
    form.value.cat = d.cat
    form.value.desc = d.desc || ''
    form.value.detail = d.detail || ''
    form.value.config = d.config || ''
    if (d.images && d.images.length > 0) {
      mediaItems.value.push(...d.images.map(url => ({ url, type: 'image' })))
    }
    if (d.videos && d.videos.length > 0) {
      mediaItems.value.push(...d.videos.map(url => ({ url, type: 'video' })))
    }
  } catch (e) { error.value = '加载失败: ' + e.message }
  finally { loading.value = false }
})

async function onSubmit() {
  if (!form.value.title || !form.value.vendor || !form.value.cat || !form.value.config) {
    modal.value = { visible: true, message: '请填写标题、厂商、分类和配置内容', type: 'confirm' }
    return
  }
  submitting.value = true
  error.value = ''
  successMsg.value = ''
  try {
    const dto = {
      id: form.value.id, title: form.value.title, vendor: form.value.vendor, cat: form.value.cat,
      desc: form.value.desc || '', detail: form.value.detail || '', config: form.value.config || '',
      images: mediaItems.value.filter(m => m.type === 'image').map(m => m.url),
      videos: mediaItems.value.filter(m => m.type === 'video').map(m => m.url)
    }
    const result = await submitWithApproval('office', 'UPDATE', dto, route.params.id, () => apiOffice.update(route.params.id, dto))
    if (result.ok) {
      successMsg.value = result.message
      setTimeout(() => router.push('/office'), 1000)
    } else {
      error.value = result.message
    }
  } catch (e) { error.value = '更新失败: ' + (e.response?.data?.msg || e.message) }
  finally { submitting.value = false }
}
</script>

<style scoped>
.top-bar h2{font-size:26px;font-weight:700;color:var(--text);letter-spacing:-.3px;position:relative;padding-bottom:4px;margin-bottom:24px}
.top-bar h2::after{content:'';position:absolute;bottom:0;left:0;width:40px;height:3px;background:linear-gradient(90deg,var(--primary),var(--orange));border-radius:2px}
.form-card{background:var(--bg-white);border:1.5px solid var(--border);border-radius:var(--radius);padding:28px;box-shadow:var(--shadow-sm)}
.form-group{margin-bottom:18px}
.form-group label{display:block;font-size:15px;font-weight:600;color:var(--text);margin-bottom:6px}
.form-input{width:100%;padding:11px 14px;border:1.5px solid var(--border);border-radius:var(--radius-xs);font-size:15px;outline:none;font-family:inherit;background:var(--bg-white);transition:var(--transition-normal)}
.form-input:hover{border-color:#cbd5e1}
.form-input:focus{border-color:var(--primary);box-shadow:0 0 0 3px rgba(30,64,175,.12)}
.form-input.font-mono{font-family:'Cascadia Code','Fira Code',Consolas,monospace;line-height:1.6}
.upload-area{border:2px dashed #cbd5e1;border-radius:8px;padding:24px;text-align:center;cursor:pointer;transition:all .2s ease;display:flex;flex-direction:column;align-items:center;gap:8px;color:#94a3b8}
.upload-area:hover{border-color:#2563eb;color:#2563eb;background:#f8fafc}
.upload-text{font-size:14px}
.hidden-input{display:none}
.preview-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(120px,1fr));gap:8px;margin-top:10px}
.preview-item{position:relative;border-radius:6px;overflow:hidden;border:1.5px solid #e2e8f0;aspect-ratio:1}
.preview-img{width:100%;height:100%;object-fit:cover}
.preview-video{width:100%;height:100%;object-fit:cover}
.preview-remove{position:absolute;top:4px;right:4px;width:22px;height:22px;border-radius:50%;background:rgba(0,0,0,.6);color:#fff;border:none;cursor:pointer;font-size:14px;line-height:1;display:flex;align-items:center;justify-content:center;transition:background .2s}
.preview-remove:hover{background:rgba(220,38,38,.8)}
.form-actions{display:flex;gap:10px;flex-wrap:wrap;justify-content:flex-end;margin-top:20px;padding-top:20px;border-top:1.5px solid var(--border)}
.btn{display:inline-flex;align-items:center;gap:6px;padding:10px 24px;border-radius:6px;font-size:15px;cursor:pointer;font-weight:600;transition:all .25s ease;border:none}
.btn-primary{background:#2563eb;color:#fff;box-shadow:0 1px 3px rgba(37,99,235,.3)}
.btn-primary:hover{background:#1d4ed8;box-shadow:0 4px 14px rgba(37,99,235,.35);transform:translateY(-1px)}
.btn-primary:disabled{opacity:.6;cursor:not-allowed;transform:none!important}
.btn-ghost{background:var(--bg-white);color:var(--text);border:1.5px solid var(--border)}
.btn-ghost:hover{border-color:var(--primary);color:var(--primary);background:var(--primary-light)}
</style>