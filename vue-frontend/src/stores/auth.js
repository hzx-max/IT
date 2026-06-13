import { ref, reactive } from 'vue'

const token = ref(localStorage.getItem('token') || '')
const user = reactive({
  username: localStorage.getItem('username') || '',
  role: localStorage.getItem('role') || '',
  loggedIn: !!localStorage.getItem('token')
})

export function useAuth() {
  function setAuth(data) {
    token.value = data.token
    user.username = data.username
    user.role = data.role
    user.loggedIn = true
    localStorage.setItem('token', data.token)
    localStorage.setItem('username', data.username)
    localStorage.setItem('role', data.role)
  }

  function clearAuth() {
    token.value = ''
    user.username = ''
    user.role = ''
    user.loggedIn = false
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
  }

  function isAdmin() {
    return user.loggedIn && (user.role === 'SUPER_ADMIN' || user.role === 'ADMIN')
  }

  function isSuperAdmin() {
    return user.loggedIn && user.role === 'SUPER_ADMIN'
  }

  return { token, user, setAuth, clearAuth, isAdmin, isSuperAdmin }
}
