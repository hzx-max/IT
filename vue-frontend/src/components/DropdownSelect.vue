<template>
  <div ref="root" class="dropdown-select" :class="{ open, disabled, compact }">
    <button
      type="button"
      class="dropdown-trigger"
      :disabled="disabled"
      @click="toggle"
      @keydown.down.prevent="openMenu"
      @keydown.enter.prevent="toggle"
      @keydown.esc.prevent="close"
    >
      <span class="dropdown-label" :class="{ placeholder: !selectedLabel }">{{ selectedLabel || placeholder }}</span>
      <svg class="dropdown-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
        <path d="M4 6l4 4 4-4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </button>

    <transition name="dropdown-pop">
      <div v-if="open" class="dropdown-menu">
        <button
          v-for="option in options"
          :key="option.value"
          type="button"
          class="dropdown-option"
          :class="{ active: option.value === modelValue, danger: option.danger }"
          @click="select(option.value)"
        >
          <span>{{ option.label }}</span>
        </button>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  modelValue: { type: [String, Number], default: '' },
  options: { type: Array, default: () => [] },
  placeholder: { type: String, default: '请选择' },
  disabled: { type: Boolean, default: false },
  compact: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'change', 'select'])
const open = ref(false)
const root = ref(null)

const selectedLabel = computed(() => {
  return props.options.find(o => o.value === props.modelValue)?.label || ''
})

function toggle() {
  if (props.disabled) return
  open.value = !open.value
}

function openMenu() {
  if (!props.disabled) open.value = true
}

function close() {
  open.value = false
}

function select(value) {
  emit('update:modelValue', value)
  emit('change', value)
  emit('select', value)
  close()
}

function onClickOutside(e) {
  if (root.value && !root.value.contains(e.target)) close()
}

onMounted(() => document.addEventListener('mousedown', onClickOutside))
onBeforeUnmount(() => document.removeEventListener('mousedown', onClickOutside))
</script>

<style scoped>
.dropdown-select{position:relative;display:inline-block;width:100%;min-width:150px}
.dropdown-select.compact{width:auto;min-width:0}
.dropdown-trigger{
  width:100%;height:55px;display:flex;align-items:center;justify-content:space-between;gap:12px;
  padding:10px 14px;border:1.5px solid #d1d5db;border-radius:8px;background:#fff;color:#000;
  font-size:16px;font-weight:500;line-height:1.4;cursor:pointer;outline:none;transition:all .18s ease;
  box-shadow:0 1px 2px rgba(0,0,0,.03);box-sizing:border-box
}
.compact .dropdown-trigger{width:auto;height:32px;padding:6px 14px;font-size:13px;gap:8px;background:#1d4ed8;color:#fff!important;border:1.5px solid #1d4ed8!important;border-radius:6px;font-weight:500;box-shadow:none!important;position:relative;z-index:1;opacity:1!important}
.compact .dropdown-trigger:hover{background:#1e40af;border-color:#1e40af!important}
.compact .dropdown-icon{color:#fff!important;position:relative;z-index:1;opacity:1!important}
.compact .dropdown-label{color:#fff!important;position:relative;z-index:1;opacity:1!important}
.dropdown-trigger:hover{border-color:#1e40af}
.dropdown-trigger:focus,.open .dropdown-trigger{
  border-color:#1e40af;box-shadow:0 0 0 3px rgba(30,64,175,.12)
}
.dropdown-label{min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;text-align:left}
.dropdown-label.placeholder{color:#999}
.dropdown-icon{flex:0 0 auto;color:#999;transition:transform .18s ease,color .18s ease}
.open .dropdown-icon{transform:rotate(180deg);color:#1e40af}
.dropdown-menu{
  position:absolute;top:calc(100% + 6px);left:0;right:0;z-index:2000;max-height:280px;overflow:auto;
  padding:6px;background:#fff;border:1.5px solid #d1d5db;border-radius:10px;
  box-shadow:0 16px 36px rgba(0,0,0,.12)
}
.dropdown-option{
  width:100%;display:flex;align-items:center;gap:8px;padding:9px 10px;border:none;border-radius:8px;
  background:transparent;color:#000;font-size:14px;line-height:1.35;text-align:left;cursor:pointer;
  transition:background .14s ease,color .14s ease
}
.dropdown-option:hover{background:#eff6ff;color:#1e40af}
.dropdown-option.active{background:#1e40af;color:#fff;font-weight:600}
.dropdown-option.danger{color:#000}
.dropdown-option.danger:hover{background:#fee2e2;color:#dc2626}
.dropdown-option.danger.active{background:#1e40af;color:#fff}
.disabled{opacity:.6;pointer-events:none}
.dropdown-pop-enter-active,.dropdown-pop-leave-active{transition:opacity .14s ease,transform .14s ease}
.dropdown-pop-enter-from,.dropdown-pop-leave-to{opacity:0;transform:translateY(-4px)}
</style>
