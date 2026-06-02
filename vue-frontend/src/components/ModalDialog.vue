<template>
  <Teleport to="body">
    <div v-if="visible" class="fixed inset-0 z-[1000] flex items-center justify-center bg-black/35 backdrop-blur-sm"
         @click.self="onCancel">
      <div class="bg-white rounded-xl shadow-2xl p-7 min-w-[340px] max-w-[460px] relative animate-modal-slide">
        <button class="absolute top-2 right-2.5 bg-none border-none text-slate-400 cursor-pointer text-lg leading-none p-0.5 rounded hover:text-slate-700 hover:bg-black/10" @click="onCancel">&times;</button>
        <div class="text-base font-semibold text-red-600 mb-5 leading-relaxed break-words text-center">{{ message }}</div>
        <input v-if="type==='prompt'" v-model="inputVal" ref="inputRef"
               class="w-full px-3.5 py-2.5 border border-slate-200 rounded-md text-sm outline-none mb-4 focus:border-blue-500 focus:shadow-[0_0_0_3px_rgba(37,99,235,.12)]"
               @keydown.enter="onConfirm" @keydown.esc="onCancel">
        <div class="flex justify-end gap-2.5">
          <button class="px-5 py-2 rounded-md text-sm font-medium border border-slate-200 bg-white text-slate-500 hover:bg-slate-50" @click="onCancel">{{ cancelText }}</button>
          <button class="px-5 py-2 rounded-md text-sm font-medium border-0 bg-blue-600 text-white hover:bg-blue-700" @click="onConfirm">{{ okText }}</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  visible: Boolean, type: { type: String, default: 'confirm' },
  message: { type: String, default: '' },
  defaultValue: { type: String, default: '' },
  okText: { type: String, default: '确定' },
  cancelText: { type: String, default: '取消' }
})
const emit = defineEmits(['confirm', 'cancel'])
const inputVal = ref('')
const inputRef = ref(null)

watch(() => props.visible, async (v) => {
  if (v) {
    inputVal.value = props.defaultValue
    await nextTick()
    if (props.type === 'prompt' && inputRef.value) inputRef.value.focus()
  }
})

function onConfirm() {
  if (props.type === 'prompt') emit('confirm', inputVal.value)
  else emit('confirm', true)
}
function onCancel() {
  emit('cancel')
}
</script>

<style scoped>
.animate-modal-slide { animation: modalSlide .25s ease; }
@keyframes modalSlide { from { transform: translateY(-20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
</style>
