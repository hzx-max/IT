<template>
  <div class="relative" ref="wrapper">
    <div class="flex">
      <input :value="modelValue" @input="onInput" @focus="onFocus" @keydown="onKeydown"
        :placeholder="placeholder"
        class="combo-input" :class="{ 'combo-input-open': show }">
      <button class="combo-btn" :class="{ 'combo-btn-open': show }" @click="show = !show" tabindex="-1">
        <svg width="12" height="8" viewBox="0 0 12 8" fill="none"><path d="M2 2l4 4 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
    </div>
    <transition name="combo-drop">
      <div v-if="show" class="combo-dropdown">
        <div v-if="filteredOptions.length > 0" class="combo-options-list">
          <div v-for="opt in filteredOptions" :key="opt.value"
            class="combo-option" :class="{ 'combo-option-active': opt.value === modelValue }"
            @mousedown.prevent="select(opt.value)">
            <span class="flex-1">{{ opt.value === opt.label ? opt.label : opt.value + ' - ' + opt.label }}</span>
            <template v-if="manageable">
              <button class="combo-opt-btn combo-opt-edit" @mousedown.stop @click.stop="startEdit(opt)" title="编辑">
                <svg width="14" height="14" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M11.5 1.5l3 3L5 14H2v-3z"/></svg>
              </button>
              <button class="combo-opt-btn combo-opt-del" @mousedown.stop @click.stop="deleteCat(opt)" title="删除">
                <svg width="14" height="14" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 4l8 8M12 4l-8 8"/></svg>
              </button>
            </template>
          </div>
        </div>
        <div v-else class="combo-empty">无匹配选项</div>
        <div v-if="manageable" class="combo-manage-bar">
          <template v-if="!editing">
            <button class="combo-add-btn" @mousedown.prevent @click.stop="startAdd">
              <svg width="14" height="14" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 3v10M3 8h10"/></svg>
              添加分类
            </button>
          </template>
          <template v-else>
            <input v-model="editKey" class="combo-edit-input" placeholder="分类" @mousedown.stop>
            <button class="combo-edit-save" @mousedown.prevent @click.stop="saveEdit">保存</button>
            <button class="combo-edit-cancel" @mousedown.prevent @click.stop="cancelEdit">取消</button>
          </template>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  modelValue: String,
  options: { type: Array, default: () => [] },
  placeholder: { type: String, default: '' },
  manageable: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'add-cat', 'edit-cat', 'delete-cat'])
const show = ref(false)
const wrapper = ref(null)
const editing = ref(false)
const editKey = ref('')
const editLabel = ref('')
const editOriginalKey = ref('')

const filteredOptions = computed(() => {
  const q = (props.modelValue || '').toLowerCase()
  if (!q) return props.options
  return props.options.filter(o => o.value.toLowerCase().includes(q) || o.label.toLowerCase().includes(q))
})

function onInput(e) {
  emit('update:modelValue', e.target.value)
  show.value = true
}

function onFocus() {
  show.value = true
}

function select(val) {
  emit('update:modelValue', val)
  show.value = false
  editing.value = false
}

function onKeydown(e) {
  if (e.key === 'Escape') { show.value = false; editing.value = false }
  if (e.key === 'Enter' && !editing.value && filteredOptions.value.length > 0) {
    select(filteredOptions.value[0].value)
    e.preventDefault()
  }
}

function startAdd() {
  editing.value = true
  editKey.value = ''
  editLabel.value = ''
  editOriginalKey.value = ''
}

function startEdit(opt) {
  editing.value = true
  editKey.value = opt.value
  editLabel.value = opt.label
  editOriginalKey.value = opt.value
}

function saveEdit() {
  const name = editKey.value.trim()
  if (!name) return
  if (editOriginalKey.value) {
    emit('edit-cat', { oldKey: editOriginalKey.value, key: name, label: name })
  } else {
    emit('add-cat', { key: name, label: name })
  }
  editing.value = false
  editKey.value = ''
  editLabel.value = ''
  editOriginalKey.value = ''
}

function cancelEdit() {
  editing.value = false
  editKey.value = ''
  editLabel.value = ''
  editOriginalKey.value = ''
}

function deleteCat(opt) {
  emit('delete-cat', { key: opt.value, label: opt.label })
}

function onClickOutside(e) {
  if (wrapper.value && !wrapper.value.contains(e.target)) {
    show.value = false
    editing.value = false
  }
}

onMounted(() => document.addEventListener('mousedown', onClickOutside))
onBeforeUnmount(() => document.removeEventListener('mousedown', onClickOutside))
</script>

<style scoped>
.combo-input{
  flex:1;padding:11px 14px;border:1.5px solid #e2e8f0;border-radius:8px 0 0 8px;font-size:15px;outline:none;
  background:#fff;transition:all .2s ease;font-family:inherit
}
.combo-input:hover{border-color:#cbd5e1}
.combo-input:focus,.combo-input-open{border-color:#2563eb;box-shadow:0 0 0 3px rgba(37,99,235,.12);z-index:1;position:relative}
.combo-input-open{border-radius:8px 0 0 0;border-bottom-color:transparent}
.combo-btn{
  width:38px;border:1.5px solid #e2e8f0;border-left:none;border-radius:0 8px 8px 0;
  background:#f8fafc;cursor:pointer;display:flex;align-items:center;justify-content:center;
  color:#64748b;transition:all .15s ease;flex-shrink:0
}
.combo-btn:hover{background:#eff6ff;color:#2563eb}
.combo-btn-open{border-color:#2563eb;background:#eff6ff;color:#2563eb;border-radius:0 8px 0 0;border-bottom-color:transparent}
.combo-dropdown{
  position:absolute;top:100%;left:0;right:0;z-index:100;
  background:#fff;border:1.5px solid #2563eb;border-top:none;border-radius:0 0 8px 8px;
  box-shadow:0 8px 24px rgba(37,99,235,.12);overflow:hidden
}
.combo-options-list{max-height:200px;overflow-y:auto}
.combo-empty{padding:12px 16px;font-size:14px;color:#94a3b8;text-align:center}
.combo-option{
  padding:10px 12px;font-size:14px;cursor:pointer;color:#334155;
  transition:all .15s ease;display:flex;align-items:center;gap:4px
}
.combo-option:hover{background:#eff6ff;color:#2563eb}
.combo-option-active{background:#eff6ff;color:#2563eb;font-weight:600}
.combo-opt-btn{
  width:24px;height:24px;border:none;border-radius:4px;cursor:pointer;
  display:flex;align-items:center;justify-content:center;flex-shrink:0;
  transition:all .15s ease;background:transparent;opacity:0
}
.combo-option:hover .combo-opt-btn{opacity:1}
.combo-opt-edit{color:#64748b}
.combo-opt-edit:hover{background:#dbeafe;color:#2563eb}
.combo-opt-del{color:#64748b}
.combo-opt-del:hover{background:#fee2e2;color:#dc2626}
.combo-manage-bar{
  display:flex;align-items:center;gap:6px;padding:8px 12px;
  border-top:1px solid #e2e8f0;background:#f8fafc
}
.combo-add-btn{
  display:flex;align-items:center;gap:4px;padding:5px 12px;border:1px dashed #94a3b8;
  border-radius:6px;background:transparent;color:#64748b;font-size:13px;cursor:pointer;
  transition:all .15s ease;width:100%;justify-content:center
}
.combo-add-btn:hover{border-color:#2563eb;color:#2563eb;background:#eff6ff}
.combo-edit-input{
  flex:1;padding:5px 8px;border:1.5px solid #e2e8f0;border-radius:4px;font-size:13px;
  outline:none;min-width:0
}
.combo-edit-input:focus{border-color:#2563eb}
.combo-edit-save{
  padding:5px 10px;border:none;border-radius:4px;background:#2563eb;color:#fff;
  font-size:13px;cursor:pointer;white-space:nowrap
}
.combo-edit-save:hover{background:#1d4ed8}
.combo-edit-cancel{
  padding:5px 10px;border:1px solid #e2e8f0;border-radius:4px;background:#fff;
  color:#64748b;font-size:13px;cursor:pointer;white-space:nowrap
}
.combo-edit-cancel:hover{border-color:#94a3b8;color:#334155}
.combo-drop-enter-active{transition:all .15s ease}
.combo-drop-leave-active{transition:all .1s ease}
.combo-drop-enter-from{opacity:0;transform:translateY(-4px)}
.combo-drop-leave-to{opacity:0;transform:translateY(-4px)}
</style>
