import { apiFavorites } from '../api/modules.js'

const HISTORY_LIMIT = 80

export const MODULE_LABELS = {
  cmd: '网络命令',
  fault: '网络故障',
  desktop: '桌面运维',
  linux: 'Linux',
  office: 'Office',
  ai: 'AI运维'
}

function isLoggedIn() {
  return !!localStorage.getItem('token')
}

function currentUserKey() {
  return localStorage.getItem('userId') || localStorage.getItem('username') || 'guest'
}

function storageKey(type) {
  return `itops:${type}:${currentUserKey()}`
}

function readList(type) {
  try {
    const raw = localStorage.getItem(storageKey(type))
    const list = raw ? JSON.parse(raw) : []
    return Array.isArray(list) ? list : []
  } catch {
    return []
  }
}

function writeList(type, list) {
  localStorage.setItem(storageKey(type), JSON.stringify(list))
  window.dispatchEvent(new CustomEvent('user-library-change', { detail: { type } }))
}

export function getDetailPath(module, id) {
  return `/${module}/detail/${id}`
}

export function makeLibraryItem(module, item) {
  if (!item?.id) return null
  const desc = item.desc || item.detail || item.symptom || item.scenario || item.solution || item.content || ''
  const category = item.cat || item.category || ''
  return {
    id: String(item.id),
    module,
    moduleLabel: MODULE_LABELS[module] || module,
    title: item.title || item.name || String(item.id),
    desc,
    category,
    createdAt: item.createdAt || item.createTime || item.time || '',
    path: getDetailPath(module, item.id),
    savedAt: new Date().toISOString()
  }
}

export function recordView(module, item) {
  const entry = makeLibraryItem(module, item)
  if (!entry) return
  const list = readList('history').filter(i => !(i.module === entry.module && String(i.id) === entry.id))
  writeList('history', [entry, ...list].slice(0, HISTORY_LIMIT))
}

export function getHistory() {
  return readList('history')
}

export async function getFavorites() {
  if (isLoggedIn()) {
    try {
      const res = await apiFavorites.list()
      if (res.data?.success) return res.data.data || []
    } catch {}
  }
  return readList('favorites')
}

export async function isFavorite(module, id) {
  if (isLoggedIn()) {
    try {
      const res = await apiFavorites.check(module, String(id))
      if (res.data?.success) return !!res.data.data
    } catch {}
  }
  return readList('favorites').some(i => i.module === module && String(i.id) === String(id))
}

export async function toggleFavorite(module, item) {
  const entry = makeLibraryItem(module, item)
  if (!entry) return false

  if (isLoggedIn()) {
    try {
      const res = await apiFavorites.toggle({
        module, itemId: String(item.id),
        itemTitle: entry.title, moduleLabel: entry.moduleLabel,
        description: entry.desc, category: entry.category,
        itemPath: entry.path
      })
      if (res.data?.success) {
        const added = res.data.data?.added
        window.dispatchEvent(new CustomEvent('user-library-change', { detail: { type: 'favorites' } }))
        return added
      }
    } catch {}
  }

  const exists = readList('favorites').some(i => i.module === module && String(i.id) === String(item.id))
  const list = exists
    ? readList('favorites').filter(i => !(i.module === module && String(i.id) === String(item.id)))
    : [entry, ...readList('favorites')]
  writeList('favorites', list)
  return !exists
}
