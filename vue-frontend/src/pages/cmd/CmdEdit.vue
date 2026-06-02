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
        </div>

        <div class="form-card mt-4">
          <h3 class="text-base font-semibold mb-4" style="color:var(--text)">厂商配置</h3>

          <div class="form-group">
            <label>厂商 <span class="text-red-600">*</span></label>
            <div class="vendor-row">
              <select v-model="vendorForm.vendor" class="form-input vendor-select">
                <option v-for="(v,k) in VENDOR_MAP" :key="k" :value="k">{{ v.n }}</option>
              </select>
              <button class="btn btn-primary btn-add-vendor" @click="addVendorConfig" :disabled="!vendorForm.vendor || !vendorForm.config">
                添加
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
            </div>
          </div>
        </div>

        <div class="max-w-[720px] mx-auto mt-4">
          <div class="form-card">
            <div class="form-actions" style="border:none;padding-top:0;margin-top:0">
              <button class="btn btn-primary" @click="onSubmit" :disabled="submitting">
                {{ submitting ? '提交中...' : '更新' }}
              </button>
              <button class="btn btn-ghost" @click="$router.back()">取消</button>
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
import { VENDOR_MAP, CAT_MAP, getVendorName, getVendorColor, apiTopics } from '../../api/index.js'

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

onMounted(async () => {
  try {
    const res = await apiTopics.get(route.params.id)
    const d = res.data
    form.value.id = d.id
    form.value.title = d.title
    form.value.cat = d.cat
    form.value.desc = d.desc || ''
    form.value.detail = d.detail || ''
    if (Array.isArray(d.configs)) {
      vendorConfigs.value = d.configs.map(cfg => ({
        vendor: cfg.vendor || '',
        config: cfg.config || '',
        comment: cfg.comment || '',
        verificationCmd: cfg.verificationCmd || '',
        doc: cfg.doc || ''
      }))
    }
  } catch (e) { error.value = '加载失败: ' + e.message }
  finally { loading.value = false }
})

function addVendorConfig() {
  if (!vendorForm.value.vendor || !vendorForm.value.config) return
  vendorConfigs.value.push({
    vendor: vendorForm.value.vendor,
    config: vendorForm.value.config,
    comment: vendorForm.value.comment || '',
    verificationCmd: vendorForm.value.verificationCmd || '',
    doc: vendorForm.value.doc || ''
  })
  vendorForm.value = { vendor: 'huawei', config: '', comment: '', verificationCmd: '', doc: '' }
}

function removeVendorConfig(i) {
  vendorConfigs.value.splice(i, 1)
}

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
    await apiTopics.update(route.params.id, {
      id: form.value.id,
      title: form.value.title,
      vendor: vendorConfigs.value[0]?.vendor || '',
      cat: form.value.cat,
      desc: form.value.desc || '',
      detail: form.value.detail || '',
      configs: vendorConfigs.value,
      comments: {}, docs: {}, topo: [], verification: {}
    })
    successMsg.value = '更新成功！'
    setTimeout(() => router.push('/cmd'), 1000)
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
.form-input:focus{border-color:var(--primary);box-shadow:0 0 0 3px rgba(37,99,235,.12)}
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
.vendor-select{flex:1}
.btn-add-vendor{padding:10px 20px;font-size:14px;white-space:nowrap;flex-shrink:0}
.vendor-config-item{padding:14px;border:1.5px solid #e2e8f0;border-radius:8px;margin-bottom:10px;transition:border-color .2s}
.vendor-config-item:hover{border-color:#cbd5e1}
.config-pre{font-family:'Cascadia Code','Fira Code',Consolas,monospace;font-size:13px;background:#f8fafc;padding:10px;border-radius:4px;white-space:pre-wrap;margin:0;max-height:120px;overflow-y:auto}
.vendor-tag{font-size:13px;padding:4px 12px;border-radius:12px;font-weight:500;display:inline-block;border:1.5px solid}
</style>