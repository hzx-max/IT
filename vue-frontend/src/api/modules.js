import http from './http.js'

export const apiAuth = {
  login: (data) => http.post('/auth/login', data),
  register: (data) => http.post('/auth/register', data),
  logout: () => http.post('/auth/logout'),
  me: () => http.get('/auth/me'),
  getUsers: () => http.get('/auth/users'),
  approveUser: (userId, approved) => http.post(`/auth/approve/${userId}`, { approved }),
  deleteUser: (userId) => http.delete(`/auth/users/${userId}`)
}

export const apiAdmin = {
  submitPendingChange: (data) => http.post('/admin/pending-change', data),
  getPendingChanges: () => http.get('/admin/pending-changes'),
  approveChange: (id) => http.post(`/admin/pending-change/${id}/approve`),
  rejectChange: (id) => http.post(`/admin/pending-change/${id}/reject`)
}

export const apiTopics = {
  list: () => http.get('/topics'),
  get: (id) => http.get(`/topics/${id}`),
  create: (data) => http.post('/topics', data),
  update: (id, data) => http.put(`/topics/${id}`, data),
  delete: (id) => http.delete(`/topics/${id}`),
  batchDelete: (ids) => http.post('/topics/batch-delete', { ids })
}

export const apiFaults = {
  list: () => http.get('/faults'),
  get: (id) => http.get(`/faults/${id}`),
  create: (data) => http.post('/faults', data),
  update: (id, data) => http.put(`/faults/${id}`, data),
  delete: (id) => http.delete(`/faults/${id}`),
  batchDelete: (ids) => http.post('/faults/batch-delete', { ids })
}

export const apiDesktop = {
  list: () => http.get('/desktop'),
  get: (id) => http.get(`/desktop/${id}`),
  create: (data) => http.post('/desktop', data),
  update: (id, data) => http.put(`/desktop/${id}`, data),
  delete: (id) => http.delete(`/desktop/${id}`),
  batchDelete: (ids) => http.post('/desktop/batch-delete', { ids })
}

export const apiLinux = {
  list: () => http.get('/linux'),
  get: (id) => http.get(`/linux/${id}`),
  create: (data) => http.post('/linux', data),
  update: (id, data) => http.put(`/linux/${id}`, data),
  delete: (id) => http.delete(`/linux/${id}`),
  batchDelete: (ids) => http.post('/linux/batch-delete', { ids })
}

export const apiOffice = {
  list: () => http.get('/office'),
  get: (id) => http.get(`/office/${id}`),
  create: (data) => http.post('/office', data),
  update: (id, data) => http.put(`/office/${id}`, data),
  delete: (id) => http.delete(`/office/${id}`),
  batchDelete: (ids) => http.post('/office/batch-delete', { ids })
}

export const apiAi = {
  list: () => http.get('/ai'),
  get: (id) => http.get(`/ai/${id}`),
  create: (data) => http.post('/ai', data),
  update: (id, data) => http.put(`/ai/${id}`, data),
  delete: (id) => http.delete(`/ai/${id}`),
  batchDelete: (ids) => http.post('/ai/batch-delete', { ids })
}

export const apiNotes = {
  get: (id) => http.get(`/notes/${id}`),
  save: (id, content) => http.put(`/notes/${id}`, { content })
}

export const apiCategories = {
  list: () => http.get('/categories'),
  save: (data) => http.post('/categories', data),
  delete: (key) => http.delete(`/categories/${key}`),
  listExclusions: () => http.get('/categories/exclusions'),
  addExclusion: (data) => http.post('/categories/exclusions', data),
  removeExclusion: (key) => http.delete(`/categories/exclusions/${key}`)
}

export const apiSearchHistory = {
  list: (module) => http.get(`/search-history/${module}`),
  save: (module, keyword) => http.post('/search-history', { module, keyword }),
  delete: (module, id) => http.delete(`/search-history/${module}/${id}`),
  clear: (module) => http.delete(`/search-history/${module}`)
}

export const apiUpload = {
  upload: (files) => {
    const formData = new FormData()
    files.forEach(f => formData.append('files', f))
    return http.post('/upload', formData, {
      headers: { 'Content-Type': undefined }
    })
  }
}

export const apiClicks = {
  record: (module, itemId, itemTitle) => http.post('/clicks/record', { module, itemId, itemTitle }),
  stats: () => http.get('/clicks/stats'),
  top10: () => http.get('/clicks/top10'),
  top10ByModule: (module) => http.get(`/clicks/top10/${module}`)
}
