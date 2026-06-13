import { ref } from 'vue'

const sidebarCollapsed = ref(localStorage.getItem('sidebarCollapsed') === '1')
const sidebarPosition = ref(localStorage.getItem('sidebarPosition') || 'left')

export function useSidebarCollapse() {
  return sidebarCollapsed
}

export function useSidebarPosition() {
  return sidebarPosition
}

export function setSidebarPosition(pos) {
  sidebarPosition.value = pos
  localStorage.setItem('sidebarPosition', pos)
}
