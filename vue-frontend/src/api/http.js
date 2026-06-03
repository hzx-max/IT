import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：自动添加 token
http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      // 只在用户已登录时跳转登录页（防止未登录用户在公开页面被误重定向）
      const token = localStorage.getItem('token')
      if (token) {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('role')
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
