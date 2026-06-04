<template>
  <MainLayout>
    <div class="top-bar">
      <h2>管理 Linux 内容</h2>
      <span class="text-sm text-slate-500 font-medium">共{{ filtered.length }}条</span>
    </div>

    <SearchBar v-model="search" placeholder="搜索 Linux 内容..." :showVendor="true" :showCat="true"
      module="linux-admin"
      v-model:vendorValue="vendorFilter" v-model:catValue="catFilter"
      :vendorMap="LINUX_VENDOR_MAP" :catMap="dynCatMap" />

    <div class="table-wrap mt-[18px]" style="padding-bottom:120px">
      <table class="w-full border-collapse text-sm table-fixed">
        <colgroup>
          <col style="width:3%"><col style="width:5%"><col style="width:25%"><col style="width:22%"><col style="width:12%"><col style="width:15%"><col style="width:18%">
        </colgroup>
        <thead>
          <tr class="bg-slate-50">
            <th class="sticky-th px-2 py-3.5 text-center"><input type="checkbox" :checked="isAllSelected" @change="toggleAll" class="w-[18px] h-[18px] accent-blue-600 cursor-pointer"></th>
            <th class="sticky-th px-2 py-3.5 text-center text-slate-500 font-semibold text-xs uppercase tracking-wider">序号</th>
            <th class="sticky-th px-4 py-3.5 text-center text-slate-500 font-semibold text-xs uppercase tracking-wider">标题</th>
            <th class="sticky-th px-2 py-3.5 text-center text-slate-500 font-semibold text-xs uppercase tracking-wider">发行版</th>
            <th class="sticky-th px-2 py-3.5 text-center text-slate-500 font-semibold text-xs uppercase tracking-wider">分类</th>
            <th class="sticky-th px-4 py-3.5 text-center text-slate-500 font-semibold text-xs uppercase tracking-wider">创建时间</th>
            <th class="sticky-th px-4 py-3.5 text-center text-slate-500 font-semibold text-xs uppercase tracking-wider">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filtered.length === 0"><td colspan="7" class="text-center py-[60px] text-slate-400 text-base">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="mx-auto mb-4 opacity-40"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg><br>没有匹配的结果
          </td></tr>
          <tr v-for="(item, idx) in filtered" :key="item.id" class="border-t border-slate-100 hover:bg-blue-50 transition-all duration-150" :class="{'bg-blue-50': selected.includes(item.id)}">
            <td class="px-2 py-3 text-center"><input type="checkbox" :checked="selected.includes(item.id)" @change="toggleItem(item.id)" class="w-[18px] h-[18px] accent-blue-600 cursor-pointer"></td>
            <td class="px-2 py-3 text-center text-slate-600">{{ idx + 1 }}</td>
            <td class="px-4 py-3 text-center font-medium">{{ item.title }}</td>
            <td class="px-2 py-3 text-center whitespace-nowrap">
              <span v-if="item.vendor"
                class="inline-block px-1.5 py-0.5 m-[0_2px] rounded-full text-xs border"
                :style="{ background: getVendorColor(item.vendor, LINUX_VENDOR_MAP)+'15', color: getVendorColor(item.vendor, LINUX_VENDOR_MAP), borderColor: getVendorColor(item.vendor, LINUX_VENDOR_MAP)+'40' }">
                {{ getVendorName(item.vendor, LINUX_VENDOR_MAP) }}
              </span>
            </td>
            <td class="px-2 py-3 text-center text-slate-600">{{ item.cat === getCatLabel(item.cat, LINUX_CAT_MAP) ? item.cat : item.cat + ' - ' + getCatLabel(item.cat, LINUX_CAT_MAP) }}</td>
            <td class="px-4 py-3 text-center text-slate-500 text-xs">{{ formatTime(item.createdAt) }}</td>
            <td class="px-4 py-3 text-center">
              <select @change="handleAction($event, item.id)" class="action-select">
                <option value="">操作</option>
                <option value="view">查看</option>
                <option value="edit">编辑</option>
                <option value="delete">删除</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 批量操作栏 -->
    <div v-if="selected.length > 0" class="fixed bottom-0 left-[240px] right-0 z-[1000] flex items-center gap-3.5 px-5 py-3.5 bg-red-50 border-t border-red-200 text-sm font-medium text-red-600 animate-fadeUp">
      <span class="flex-1">已选择 {{ selected.length }} 项</span>
      <button class="btn btn-ghost-sm" @click="clearSelection">取消</button>
      <button class="btn btn-danger btn-sm" @click="batchDelete">删除</button>
    </div>

    <!-- 删除确认弹窗 -->
    <ModalDialog :visible="modal.visible" :message="modal.message" :type="modal.type"
      @confirm="onModalConfirm" @cancel="onModalCancel" />
  </MainLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MainLayout from '../../layouts/MainLayout.vue'
import SearchBar from '../../components/SearchBar.vue'
import ModalDialog from '../../components/ModalDialog.vue'
import { apiLinux, LINUX_VENDOR_MAP, LINUX_CAT_MAP, getVendorName, getVendorColor, getCatLabel, formatTime } from '../../api/index.js'
import { submitWithApproval } from '../../api/approval.js'

const router = useRouter()
const search = ref('')
const vendorFilter = ref('')
const catFilter = ref('')
const items = ref([])
const selected = ref([])

const dynCatMap = computed(() => {
  const map = {}
  items.value.forEach(i => { if (i.cat && !map[i.cat]) map[i.cat] = getCatLabel(i.cat, LINUX_CAT_MAP) })
  return map
})

const filtered = computed(() => {
  let list = items.value
  const s = search.value.trim().toLowerCase()
  if (vendorFilter.value) list = list.filter(i => i.vendor === vendorFilter.value)
  if (catFilter.value) list = list.filter(i => i.cat === catFilter.value)
  if (s) list = list.filter(i => (i.title||'').toLowerCase().includes(s) || (i.desc||'').toLowerCase().includes(s))
  return list
})

const modal = ref({ visible: false, message: '', type: 'confirm', action: null, payload: null })

function onModalConfirm() {
  if (modal.value.action === 'delete-single') { doDelete(modal.value.payload) }
  else if (modal.value.action === 'delete-batch') { doBatchDelete() }
  modal.value.visible = false
}
function onModalCancel() { modal.value.visible = false }

function handleAction(e, id) {
  const action = e.target.value
  e.target.value = ''
  if (action === 'view') router.push('/linux/detail/' + id)
  else if (action === 'edit') router.push('/linux/edit/' + id)
  else if (action === 'delete') {
    modal.value = { visible: true, message: '确定要删除这条 Linux 记录吗？', type: 'confirm', action: 'delete-single', payload: id }
  }
}

async function doDelete(id) {
  const result = await submitWithApproval('linux', 'DELETE', null, id, () => apiLinux.delete(id))
  if (result.ok) { selected.value = selected.value.filter(i => i !== id); await loadData() }
  else { alert(result.message) }
}

function toggleItem(id) {
  const idx = selected.value.indexOf(id)
  if (idx >= 0) selected.value.splice(idx, 1)
  else selected.value.push(id)
}

const isAllSelected = computed(() => filtered.value.length > 0 && filtered.value.every(i => selected.value.includes(i.id)))

function toggleAll() {
  const allSelected = filtered.value.length > 0 && filtered.value.every(i => selected.value.includes(i.id))
  if (allSelected) {
    const ids = new Set(filtered.value.map(i => i.id))
    selected.value = selected.value.filter(id => !ids.has(id))
  } else {
    const existing = new Set(selected.value)
    filtered.value.forEach(i => { if (!existing.has(i.id)) selected.value.push(i.id) })
  }
}

function clearSelection() {
  selected.value = []
}

async function batchDelete() {
  modal.value = { visible: true, message: `确定要删除已选择的 ${selected.value.length} 项吗？此操作不可撤销！`, type: 'confirm', action: 'delete-batch', payload: null }
}

async function doBatchDelete() {
  try {
    await apiLinux.batchDelete([...selected.value])
    selected.value = []
    await loadData()
  } catch { alert('批量删除失败，请重试') }
}

async function loadData() {
  try { const res = await apiLinux.list(); items.value = res.data }
  catch (e) { console.error('加载失败:', e) }
}

onMounted(loadData)
</script>

<style scoped>
.top-bar{display:flex;justify-content:space-between;align-items:center;margin-bottom:24px;flex-wrap:wrap;gap:12px}
.top-bar h2{font-size:26px;font-weight:700;color:var(--text);letter-spacing:-.3px;position:relative;padding-bottom:4px}
.top-bar h2::after{content:'';position:absolute;bottom:0;left:0;width:40px;height:3px;background:linear-gradient(90deg,var(--primary),var(--orange));border-radius:2px}
.table-wrap{background:#fff;border:1.5px solid #e2e8f0;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,.04);overflow:visible}
.sticky-th{position:sticky;top:0;z-index:10;background:#f8fafc;box-shadow:0 1px 0 #e2e8f0}
.action-select{padding:6px 30px 6px 12px;border:1.5px solid var(--border);border-radius:5px;font-size:13px;background:var(--bg-white);color:var(--text);cursor:pointer;outline:none;appearance:none;background-image:url("data:image/svg+xml,%3Csvg width='10' height='6' viewBox='0 0 10 6' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1l4 4 4-4' stroke='%2364748b' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");background-repeat:no-repeat;background-position:right 10px center;transition:var(--transition-fast)}
.action-select:hover{border-color:var(--primary)}
.action-select:focus{border-color:var(--primary);box-shadow:0 0 0 3px rgba(37,99,235,.12)}
.btn{display:inline-flex;align-items:center;gap:6px;border-radius:6px;cursor:pointer;font-weight:500;transition:all .25s ease}
.btn-danger{background:#dc2626;color:#fff;border:1.5px solid #dc2626;box-shadow:0 1px 3px rgba(220,38,38,.3)}
.btn-danger:hover{background:#b91c1c;border-color:#b91c1c;box-shadow:0 4px 14px rgba(220,38,38,.35);transform:translateY(-1px)}
.btn-sm{padding:6px 14px!important;font-size:13px!important;border-radius:5px!important}
.btn-ghost-sm{padding:6px 14px!important;font-size:13px!important;border-radius:5px!important;background:#fff;color:#64748b;border:1.5px solid #cbd5e1;cursor:pointer;font-weight:500;transition:all .25s ease}
.btn-ghost-sm:hover{border-color:#2563eb;color:#2563eb;background:#eff6ff}
.animate-fadeUp{animation:fadeInUp .25s ease}
@keyframes fadeInUp{from{opacity:0;transform:translateY(12px)}to{opacity:1;transform:translateY(0)}}
@media(max-width:768px){.fixed.bottom-0{left:0!important}}
</style>
