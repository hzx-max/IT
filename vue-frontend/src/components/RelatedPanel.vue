<template>
  <div class="related-panel-wrapper" ref="wrapperRef" :class="wrapperClass" :style="wrapperStyle">
    <!-- 折叠态 -->
    <div v-if="collapsed" class="related-collapsed" @click="expanded = true" title="展开同类别内容">
      <span class="related-expand-icon">◀</span>
    </div>

    <!-- 展开态 -->
    <aside v-else class="related-panel" :class="{ 'related-dragging': isDragging }" :style="dragStyle">
      <div class="related-drag-handle" @mousedown.prevent="onDragStart">
        <div class="drag-indicator"></div>
      </div>
      <div class="related-header">
        <span class="related-header-text">同类别内容</span>
        <button class="related-collapse-btn" @click="expanded = false" title="收起">▶</button>
      </div>
      <div class="related-body" v-if="related.length > 0">
        <div
          v-for="r in related"
          :key="r.id"
          class="related-item"
          :class="{ 'is-current': isCurrent(r) }"
          @click="goToDetail(r.id)"
        >
          <div class="related-title">{{ r.title }}</div>
          <div v-if="r.desc" class="related-desc clamped">{{ r.desc }}</div>
        </div>
      </div>
      <div class="related-body related-empty" v-else>
        <div class="text-slate-400 text-center py-8 text-sm">暂无相关内容</div>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps({
  apiList: { type: Function, required: true },
  currentId: { type: [String, Number], required: true },
  currentCat: { type: String, default: '' },
  basePath: { type: String, required: true }
})

const LS_KEY_COLLAPSED = 'relatedPanelCollapsed'
const LS_KEY_POSITION = 'relatedPanelPosition'

const wrapperRef = ref(null)
const expanded = ref(!localStorage.getItem(LS_KEY_COLLAPSED))
const collapsed = computed(() => !expanded.value)
const position = ref(localStorage.getItem(LS_KEY_POSITION) || 'right')

function updateRootVar(val) {
  document.documentElement.dataset.relatedPanelSide = val
}
updateRootVar(position.value)

function updateExpandedVar(val) {
  document.documentElement.dataset.relatedPanelExpanded = val ? '1' : '0'
}
updateExpandedVar(expanded.value)

const wrapperStyle = computed(() => {
  if (!expanded.value) {
    return position.value === 'left'
      ? { width: '32px', flex: 'none', left: '8px' }
      : { width: '32px', flex: 'none', right: '8px' }
  }
  return {}
})

const wrapperClass = computed(() => ({
  'related-panel-fixed': true,
  'related-panel-left': position.value === 'left',
  'related-panel-collapsed': !expanded.value
}))

const related = ref([])

function goToDetail(id) {
  router.push(props.basePath + '/detail/' + id)
}

function isCurrent(item) {
  return String(item.id) === String(props.currentId)
}

function getCreatedTime(item) {
  const value = item?.createdAt || item?.createTime || item?.created_time || item?.time || ''
  const timestamp = new Date(value).getTime()
  return Number.isFinite(timestamp) ? timestamp : 0
}

const isDragging = ref(false)
const dragOffsetX = ref(0)
const dragStartX = ref(0)
const dragThreshold = 120

const dragStyle = computed(() => {
  if (!isDragging.value) return {}
  return { transform: `translateX(${dragOffsetX.value}px)`, transition: 'none' }
})

function onDragStart(e) {
  if (collapsed.value) return
  isDragging.value = true
  dragStartX.value = e.clientX
  dragOffsetX.value = 0
  document.addEventListener('mousemove', onDragMove)
  document.addEventListener('mouseup', onDragEnd)
  document.body.style.userSelect = 'none'
  document.body.style.cursor = 'grabbing'
}

function onDragMove(e) {
  if (!isDragging.value) return
  const dx = e.clientX - dragStartX.value
  dragOffsetX.value = dx
}

function onDragEnd(e) {
  if (!isDragging.value) return
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
  document.body.style.userSelect = ''
  document.body.style.cursor = ''

  const dx = e.clientX - dragStartX.value

  if (position.value === 'right') {
    if (dx < -dragThreshold) {
      position.value = 'left'
      localStorage.setItem(LS_KEY_POSITION, 'left')
    } else if (dx > dragThreshold) {
      expanded.value = false
      localStorage.setItem(LS_KEY_COLLAPSED, '1')
    }
  } else {
    if (dx > dragThreshold) {
      position.value = 'right'
      localStorage.setItem(LS_KEY_POSITION, 'right')
    } else if (dx < -dragThreshold) {
      expanded.value = false
      localStorage.setItem(LS_KEY_COLLAPSED, '1')
    }
  }

  isDragging.value = false
  dragOffsetX.value = 0
}

onUnmounted(() => {
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
  delete document.documentElement.dataset.relatedPanelSide
  delete document.documentElement.dataset.relatedPanelExpanded
})

watch(expanded, (val) => {
  localStorage.setItem(LS_KEY_COLLAPSED, val ? '' : '1')
  updateExpandedVar(val)
})

watch(position, (val) => {
  updateRootVar(val)
})

async function load() {
  if (!props.currentCat) { related.value = []; return }
  try {
    const res = await props.apiList()
    const list = Array.isArray(res.data) ? res.data : (Array.isArray(res) ? res : [])
    related.value = list
      .filter(item => String(item.cat) === String(props.currentCat))
      .sort((a, b) => getCreatedTime(b) - getCreatedTime(a))
  } catch (e) { related.value = [] }
}
watch(() => props.currentCat, (val) => { if (val) load() })
watch(() => props.currentId, () => { if (props.currentCat) load() })
onMounted(() => { if (props.currentCat) load() })
</script>

<style scoped>
.related-panel-wrapper.related-panel-fixed{
  position:fixed;top:28px;z-index:50;
  width:400px;height:calc(100vh - 56px);flex:none;align-self:auto
}
.related-panel-fixed.related-panel-left{
  left:10vw
}
.related-panel-fixed:not(.related-panel-left){
  right:10vw
}
.related-panel-collapsed{
  width:32px;height:0
}
.related-collapsed{
  position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);
  width:32px;height:32px;
  display:flex;align-items:center;justify-content:center;
  cursor:pointer;z-index:5;border:none;background:transparent;padding:0;
  transition:all .2s;border-radius:50%
}
.related-collapsed:hover{background:rgba(37,99,235,.08)}
.related-expand-icon{font-size:14px;color:#2563eb}
.related-expand-icon{
  font-size:0
}
.related-expand-icon::before{
  content:'◀';
  font-size:14px;
  color:#2563eb
}
.related-panel-left .related-expand-icon::before{
  content:'▶'
}
.related-panel{
  --related-panel-blue-height:67px;
  background:#fff;border:1px solid #e2e8f0;border-radius:12px;
  display:flex;flex-direction:column;overflow:hidden;
  width:100%;height:100%;position:relative
}
.related-panel::after{
  content:'';
  display:block;
  height:var(--related-panel-blue-height);
  flex:0 0 var(--related-panel-blue-height);
  background:#2563eb
}
.related-panel-left .related-panel{
  border-radius:12px
}
.related-panel-left .related-drag-handle{
  border-radius:12px 12px 0 0
}
.related-panel-fixed:not(.related-panel-left) .related-panel{
  border-radius:12px
}
.related-panel-fixed:not(.related-panel-left) .related-drag-handle{
  border-radius:12px 12px 0 0
}
.related-drag-handle{
  display:flex;align-items:center;justify-content:center;
  height:14px;cursor:grab;user-select:none;flex-shrink:0;
  background:#2563eb;border-radius:12px 12px 0 0;
  transition:background .2s
}
.related-drag-handle:hover{background:#1d4ed8}
.related-drag-handle:active{cursor:grabbing}
.drag-indicator{
  width:24px;height:3px;border-radius:2px;background:rgba(255,255,255,.5);
}
.related-drag-handle:hover .drag-indicator{background:rgba(255,255,255,.75)}
.related-header{
  display:flex;align-items:center;justify-content:space-between;
  position:relative;
  min-height:53px;
  padding:0 16px;border-bottom:1px solid #2563eb;flex-shrink:0;
  background:#2563eb
}
.related-header-text{
  position:absolute;left:50%;top:50%;transform:translate(-50%,-50%);
  display:flex;align-items:center;justify-content:center;
  min-height:53px;
  font-size:21px;font-weight:600;color:#fff;white-space:nowrap;pointer-events:none
}
.related-collapse-btn{
  background:none;border:none;cursor:pointer;font-size:14px;color:rgba(255,255,255,.82);
  padding:4px 8px;border-radius:6px;transition:all .2s;line-height:1;margin-left:auto
}
.related-collapse-btn{
  font-size:0
}
.related-collapse-btn::before{
  content:'▶';
  font-size:14px;
  color:inherit
}
.related-panel-left .related-collapse-btn::before{
  content:'◀'
}
.related-collapse-btn:hover{background:rgba(255,255,255,.14);color:#fff}
.related-body{flex:1;overflow-y:auto;padding:8px 0}
.related-empty{display:flex;align-items:center;justify-content:center}
.related-item{
  margin:6px 16px;
  padding:13px 16px;
  cursor:pointer;
  transition:all .15s;
  border-bottom:none;
  border-radius:7px;
  background:#fff
}
.related-item:hover{background:#f8fafc}
.related-item.is-current{
  margin:6px 16px;
  padding:13px 16px;
  border-bottom:none;
  border-radius:7px;
  background:#2563eb
}
.related-item.is-current:hover{background:#1d4ed8}
.related-item.is-current .related-title{
  color:#fff;
  text-align:left;
  font-size:18px;
  font-weight:600
}
.related-item.is-current .related-desc{
  color:rgba(255,255,255,.82);
  text-align:left;
  margin-top:6px
}
.related-item:last-child{border-bottom:none}
.related-title{font-size:18px;font-weight:500;color:#1e293b;line-height:1.4}
.related-desc{font-size:16px;color:#94a3b8;margin-top:4px;line-height:1.4}
.related-desc.clamped{
  display:-webkit-box;-webkit-line-clamp:1;-webkit-box-orient:vertical;overflow:hidden
}
@media (max-width:1200px){
  .related-panel-wrapper{display:none}
}
</style>
