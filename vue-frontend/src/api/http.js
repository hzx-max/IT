import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：自动添加 token 和 CSRF 保护头
http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  config.headers['X-Requested-With'] = 'XMLHttpRequest'
  return config
})

http.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      const token = localStorage.getItem('token')
      if (token) {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('role')
        localStorage.removeItem('userId')
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      }
    }
    console.error('API Error:', err.response?.data || err.message)
    return Promise.reject(err)
  }
)

export default http
