<template>
  <Teleport to="body">
    <div v-if="visible" class="fixed bottom-7 right-7 z-[999] px-6 py-3.5 rounded-lg shadow-lg text-sm font-medium border-l-4 border-orange-500 animate-toast"
         style="background:linear-gradient(135deg,#0f172a,#1e293b);color:#fff">
      {{ message }}
      <button class="toast-close" @click="$emit('close')">&times;</button>
    </div>
  </Teleport>
</template>

<script setup>
import { watch } from 'vue'

const props = defineProps({ visible: Boolean, message: String })
const emit = defineEmits(['close'])

watch(() => props.visible, (v) => {
  if (v) setTimeout(() => emit('close'), 2000)
})
</script>

<style scoped>
.animate-toast { animation: toastIn .35s cubic-bezier(.4,0,.2,1); }
@keyframes toastIn { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }
/* 移动端安全区 */
@media (max-width: 768px) {
  .fixed.bottom-7 { bottom: calc(20px + env(safe-area-inset-bottom, 0px)); right: 16px; left: 16px; }
}
</style>
