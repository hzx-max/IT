<template>
  <MainLayout>
    <div class="max-w-[720px] mx-auto">
      <div class="top-bar">
        <h2>编辑网络命令</h2>
      </div>

      <div v-if="loading" class="text-center py-20 text-slate-400"><div class="w-9 h-9 mx-auto mb-4 border-3 border-slate-200 border-t-blue-600 rounded-full animate-spin"></div>加载中...</div>

      <div v-else>
        <div class="form-card">
          <div class="form-group">
            <label>标题 <span class="text-red-600">*</span></label>
            <input v-model="form.title" class="form-input">
          </div>

          <div class="form-group">
            <label>分类 <span class="text-red-600">*</span></label>
            <ComboBox v-model="form.cat" :options="catOptions" placeholder="选择或输入分类" />
          </div>

          <div class="form-group">
            <label>描述</label>
            <textarea v-model="form.desc" rows="3" class="form-input"></textarea>
          </div>

          <div class="form-group">
            <label>详细内容</label>
            <textarea v-model="form.detail" rows="6" class="form-input"></textarea>
          </div>

          <div class="form-group">
            <label>拓扑图</label>
            <div class="upload-area" @click="$refs.topoInput.click()" @dragover.prevent @drop.prevent="onTopoDrop">
              <input type="file" multiple accept="image/*,video/*" class="hidden" ref="topoInput" @change="onTopoChange">
              <div v-if="topoFiles.length === 0" class="upload-placeholder">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.5"><path d="M12 5v14M5 12h14"/></svg>
                <p>点击上传或拖拽图片/视频</p>
              </div>
              <div v-else class="upload-preview-list">
                <div v-for="(f, i) in topoFiles" :key="i" class="upload-preview-item">
                  <img v-if="f.type.startsWith('image/')" :src="f.url" class="upload-thumb" />
                  <video v-else :src="f.url" class="upload-thumb"></video>
                  <button class="upload-remove" @click.stop="removeTopo(i)">×</button>
                </div>
                <div class="upload-add-more" @click.stop="$refs.topoInput.click()">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="form-card mt-4">
          <h3 class="text-base font-semibold mb-4" style="color:var(--text)">厂商配置</h3>

          <div class="form-group">
            <label>厂商 <span class="text-red-600">*</span></label>
            <div class="vendor-row">
              <DropdownSelect v-model="vendorForm.vendor" :options="vendorOptions" placeholder="选择厂商" @change="onVendorChange" />
              <button class="btn btn-primary btn-add-vendor" @click="addVendorConfig" :disabled="!vendorForm.vendor || !vendorForm.config">
                {{ vendorExists ? '更新' : '添加' }}
              </button>
            </div>
          </div>

          <div class="form-group">
            <label>配置命令 <span class="text-red-600">*</span></label>
            <textarea v-model="vendorForm.config" rows="5" placeholder="输入配置命令" class="form-input font-mono"></textarea>
          </div>

          <div class="form-group">
            <label>配置说明</label>
            <textarea v-model="vendorForm.comment" rows="2" placeholder="配置说明或注意事项" class="form-input"></textarea>
          </div>

          <div class="form-group">
            <label>验证命令</label>
            <textarea v-model="vendorForm.verificationCmd" rows="3" placeholder="输入验证配置是否生效的命令" class="form-input font-mono"></textarea>
          </div>

          <div class="form-group">
            <label>验证命令图片</label>
            <div class="upload-area" @click="$refs.verifyInput.click()" @dragover.prevent @drop.prevent="onVerifyDrop">
              <input type="file" multiple accept="image/*" class="hidden" ref="verifyInput" @change="onVerifyChange">
              <div v-if="verifyFiles.length === 0" class="upload-placeholder">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.5"><path d="M12 5v14M5 12h14"/></svg>
                <p>点击上传或拖拽图片</p>
              </div>
              <div v-else class="upload-preview-list">
                <div v-for="(f, i) in verifyFiles" :key="i" class="upload-preview-item">
                  <img :src="f.url" class="upload-thumb" />
                  <button class="upload-remove" @click.stop="removeVerify(i)">×</button>
                </div>
                <div class="upload-add-more" @click.stop="$refs.verifyInput.click()">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
                </div>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>参考文档URL</label>
            <input v-model="vendorForm.doc" placeholder="https://..." class="form-input">
          </div>
        </div>

        <div v-if="vendorConfigs.length > 0" class="max-w-[720px] mx-auto mt-4">
          <div class="form-card">
            <h3 class="text-base font-semibold mb-3" style="color:var(--text)">已添加的厂商配置 ({{ vendorConfigs.length }})</h3>
            <div v-for="(cfg, i) in vendorConfigs" :key="i" class="vendor-config-item">
              <div class="flex items-center justify-between mb-2">
                <span class="vendor-tag" :style="{ background: getVendorColor(cfg.vendor, VENDOR_MAP)+'15', color: getVendorColor(cfg.vendor, VENDOR_MAP), borderColor: getVendorColor(cfg.vendor, VENDOR_MAP)+'40' }">
                  {{ getVendorName(cfg.vendor, VENDOR_MAP) }}
                </span>
                <button class="text-red-500 text-sm hover:text-red-700 font-medium" @click="removeVendorConfig(i)">删除</button>
              </div>
              <pre class="config-pre">{{ cfg.config }}</pre>
              <div v-if="cfg.comment" class="text-sm text-slate-500 mt-1">{{ cfg.comment }}</div>
              <div v-if="cfg.verificationCmd" class="text-sm text-slate-500 mt-1">验证: {{ cfg.verificationCmd }}</div>
              <div v-if="cfg.doc" class="text-sm text-blue-500 mt-1"><a :href="cfg.doc" target="_blank" class="hover:underline">参考文档</a></div>
            </div>
          </div>
        </div>

        <div class="max-w-[720px] mx-auto mt-4">
          <div class="form-card">
            <div class="form-group">
              <label>附件</label>
              <FileUploader v-model="files" />
            </div>
          </div>
        </div>

        <div class="max-w-[720px] mx-auto mt-4">
          <div class="form-card">
            <div class="form-actions" style="border:none;padding-top:0;margin-top:0">
              <button class="btn btn-primary" @click="onSubmit" :disabled="submitting">
                {{ submitting ? '提交中...' : '更新' }}
              </button>
              <button class="btn btn-primary" @click="$router.back()">取消</button>
            </div>
            <div v-if="error" class="mt-4 p-3 bg-red-50 border border-red-200 rounded-md text-red-600 text-sm">{{ error }}</div>
            <div v-if="successMsg" class="mt-4 p-3 bg-emerald-50 border border-emerald-200 rounded-md text-emerald-600 text-sm">{{ successMsg }}</div>
          </div>
        </div>
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
import FileUploader from '../../components/FileUploader.vue'
import DropdownSelect from '../../components/DropdownSelect.vue'
import { VENDOR_MAP, CAT_MAP, getVendorName, getVendorColor, apiTopics } from '../../api/index.js'
import { submitWithApproval } from '../../api/approval.js'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const submitting = ref(false)
const error = ref('')
const successMsg = ref('')

const form = ref({ id: '', title: '', cat: '', desc: '', detail: '' })
const catOptions = computed(() => Object.entries(CAT_MAP).map(([k, v]) => ({ value: k, label: v })))
const modal = ref({ visible: false, message: '', type: 'confirm' })

const vendorConfigs = ref([])
const vendorForm = ref({ vendor: 'huawei', config: '', comment: '', verificationCmd: '', doc: '' })
const vendorOptions = computed(() => Object.entries(VENDOR_MAP).map(([k, v]) => ({ value: k, label: v.n })))
const topoFiles = ref([])
const verifyFiles = ref([])
const files = ref([])

const vendorExists = computed(() => {
  return vendorConfigs.value.some(c => c.vendor === vendorForm.value.vendor)
})

function onVendorChange() {
  const existing = vendorConfigs.value.find(c => c.vendor === vendorForm.value.vendor)
  if (existing) {
    vendorForm.value.config = existing.config
    vendorForm.value.comment = existing.comment || ''
    vendorForm.value.verificationCmd = existing.verificationCmd || ''
    vendorForm.value.doc = existing.doc || ''
    verifyFiles.value = (existing.verificationImages || []).map(f => ({ url: f.url, type: f.type || 'image', name: f.name || '' }))
  } else {
    vendorForm.value.config = ''
    vendorForm.value.comment = ''
    vendorForm.value.verificationCmd = ''
    vendorForm.value.doc = ''
    verifyFiles.value = []
  }
}

onMounted(async () => {
  try {
    const res = await apiTopics.get(route.params.id)
    const d = res.data
    form.value.id = d.id
    form.value.title = d.title
    form.value.cat = d.cat
    form.value.desc = d.desc || ''
    form.value.detail = d.detail || ''
    if (Array.isArray(d.topo)) {
      topoFiles.value = d.topo.map(f => ({ url: f.url, type: f.type || 'image', name: f.name || '' }))
    }
    if (Array.isArray(d.files)) {
      files.value = d.files.map(f => ({ name: f.name || f.originalName || '', url: f.url, size: f.size || 0, type: f.type || '' }))
    }
    if (Array.isArray(d.configs)) {
      vendorConfigs.value = d.configs.map(cfg => ({
        vendor: cfg.vendor || '',
        config: cfg.config || '',
        comment: cfg.comment || '',
        verificationCmd: cfg.verificationCmd || '',
        doc: cfg.doc || '',
        verificationImages: Array.isArray(cfg.verificationImages) ? cfg.verificationImages : []
      }))
    }
  } catch (e) { error.value = '加载失败: ' + e.message }
  finally { loading.value = false }
})

function addVendorConfig() {
  if (!vendorForm.value.vendor || !vendorForm.value.config) return
  const idx = vendorConfigs.value.findIndex(c => c.vendor === vendorForm.value.vendor)
  const verifyData = verifyFiles.value.map(f => ({ type: 'image', name: f.name, url: f.url }))
  const entry = {
    vendor: vendorForm.value.vendor,
    config: vendorForm.value.config,
    comment: vendorForm.value.comment || '',
    verificationCmd: vendorForm.value.verificationCmd || '',
    doc: vendorForm.value.doc || '',
    verificationImages: verifyData
  }
  if (idx >= 0) {
    vendorConfigs.value[idx] = entry
  } else {
    vendorConfigs.value.push(entry)
  }
  vendorForm.value = { vendor: 'huawei', config: '', comment: '', verificationCmd: '', doc: '' }
  verifyFiles.value = []
}

function removeVendorConfig(i) {
  const removedVendor = vendorConfigs.value[i]?.vendor
  vendorConfigs.value.splice(i, 1)
  if (removedVendor === vendorForm.value.vendor) {
    vendorForm.value.config = ''
    vendorForm.value.comment = ''
    vendorForm.value.verificationCmd = ''
    vendorForm.value.doc = ''
    verifyFiles.value = []
  }
}

function onTopoChange(e) { addTopoFiles([...e.target.files]) }
function onTopoDrop(e) { addTopoFiles([...e.dataTransfer.files]) }
function addTopoFiles(files) {
  files.forEach(f => {
    if (f.type.startsWith('image/') || f.type.startsWith('video/')) {
      topoFiles.value.push({ file: f, url: URL.createObjectURL(f), type: f.type, name: f.name })
    }
  })
}
function removeTopo(i) { URL.revokeObjectURL(topoFiles.value[i].url); topoFiles.value.splice(i, 1) }

function onVerifyChange(e) { addVerifyFiles([...e.target.files]) }
function onVerifyDrop(e) { addVerifyFiles([...e.dataTransfer.files]) }
function addVerifyFiles(files) {
  files.forEach(f => {
    if (f.type.startsWith('image/')) {
      verifyFiles.value.push({ file: f, url: URL.createObjectURL(f), type: f.type, name: f.name })
    }
  })
}
function removeVerify(i) { URL.revokeObjectURL(verifyFiles.value[i].url); verifyFiles.value.splice(i, 1) }

async function onSubmit() {
  if (!form.value.title || !form.value.cat) {
    modal.value = { visible: true, message: '请填写标题和分类', type: 'confirm' }
    return
  }
  if (vendorConfigs.value.length === 0) {
    modal.value = { visible: true, message: '请至少添加一个厂商配置', type: 'confirm' }
    return
  }
  submitting.value = true
  error.value = ''
  successMsg.value = ''
  try {
    const topoData = topoFiles.value.map(f => ({ type: f.type.startsWith('image/') ? 'image' : 'video', name: f.name, url: f.url }))
    const dto = {
      id: form.value.id,
      title: form.value.title,
      vendor: vendorConfigs.value[0]?.vendor || '',
      cat: form.value.cat,
      desc: form.value.desc || '',
      detail: form.value.detail || '',
      topo: topoData,
      configs: vendorConfigs.value,
      comments: {}, docs: {}, verification: {},
      files: files.value
    }
    const result = await submitWithApproval('cmd', 'UPDATE', dto, route.params.id, () => apiTopics.update(route.params.id, dto))
    if (result.ok) {
      successMsg.value = result.message
      setTimeout(() => router.push('/cmd'), 1000)
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
.form-actions{display:flex;gap:10px;flex-wrap:wrap;justify-content:flex-end;margin-top:20px;padding-top:20px;border-top:1.5px solid var(--border)}
.btn{display:inline-flex;align-items:center;gap:6px;padding:10px 24px;border-radius:6px;font-size:15px;cursor:pointer;font-weight:600;transition:all .25s ease;border:none}
.btn-sm{padding:7px 14px;font-size:13px}
.btn-primary{background:#2563eb;color:#fff;box-shadow:0 1px 3px rgba(37,99,235,.3)}
.btn-primary:hover{background:#1d4ed8;box-shadow:0 4px 14px rgba(37,99,235,.35);transform:translateY(-1px)}
.btn-primary:disabled{opacity:.6;cursor:not-allowed;transform:none!important}
.btn-ghost{background:var(--bg-white);color:var(--text);border:1.5px solid var(--border)}
.btn-ghost:hover{border-color:var(--primary);color:var(--primary);background:var(--primary-light)}
.vendor-row{display:flex;gap:10px;align-items:stretch}

.btn-add-vendor{padding:10px 20px;font-size:14px;white-space:nowrap;flex-shrink:0}
.vendor-config-item{padding:14px;border:1.5px solid #e2e8f0;border-radius:8px;margin-bottom:10px;transition:border-color .2s}
.vendor-config-item:hover{border-color:#cbd5e1}
.config-pre{font-family:'Cascadia Code','Fira Code',Consolas,monospace;font-size:13px;background:#f8fafc;padding:10px;border-radius:4px;white-space:pre-wrap;margin:0;max-height:120px;overflow-y:auto}
.vendor-tag{font-size:13px;padding:4px 12px;border-radius:12px;font-weight:500;display:inline-block;border:1.5px solid}
.upload-area{border:2px dashed #e2e8f0;border-radius:8px;padding:16px;cursor:pointer;transition:all .2s ease;min-height:80px}
.upload-area:hover{border-color:#2563eb;background:#f8faff}
.upload-placeholder{display:flex;flex-direction:column;align-items:center;gap:8px;color:#94a3b8;font-size:14px;padding:12px 0}
.upload-preview-list{display:flex;flex-wrap:wrap;gap:8px}
.upload-preview-item{position:relative;width:80px;height:80px;border-radius:6px;overflow:hidden;border:1px solid #e2e8f0}
.upload-thumb{width:100%;height:100%;object-fit:cover}
.upload-remove{position:absolute;top:2px;right:2px;width:20px;height:20px;border-radius:50%;background:rgba(0,0,0,.6);color:#fff;border:none;cursor:pointer;font-size:14px;display:flex;align-items:center;justify-content:center;line-height:1}
.upload-add-more{width:80px;height:80px;border:2px dashed #e2e8f0;border-radius:6px;display:flex;align-items:center;justify-content:center;cursor:pointer;transition:all .2s ease}
.upload-add-more:hover{border-color:#2563eb;background:#f8faff}
.hidden{display:none}
</style>