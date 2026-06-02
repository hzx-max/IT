<template>
  <div class="flex gap-1.5 flex-wrap mb-8">
    <button v-for="c in cats" :key="c.key"
      class="px-4 py-1.5 rounded-full text-sm font-medium cursor-pointer transition-all duration-200"
      :class="c.key === active ? 'bg-blue-600 text-white shadow-[0_2px_8px_rgba(37,99,235,.3)]' : 'bg-white text-slate-500 border border-slate-200 hover:border-blue-500 hover:text-blue-600 hover:-translate-y-px hover:shadow-sm'"
      @click="$emit('select', c.key)">
      {{ c.label }}
    </button>
  </div>
</template>

<script setup>
defineProps({ cats: Array, active: { type: String, default: '' } })
defineEmits(['select'])
</script>
