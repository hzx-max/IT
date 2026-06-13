<template>
  <div class="auth-page">
    <div class="auth-bg"></div>

    <div class="auth-card" :class="mode === 'login' ? 'panel-right' : 'panel-left'">
      <!-- 注册表单（在左半边） -->
      <div class="form-container sign-up-container">
        <div class="form-inner" :class="{ 'form-visible': mode === 'register', 'form-hidden': mode !== 'register' }">
          <h1 class="form-title">创建账号</h1>

          <div class="form-field" :class="{ 'field-taken': regUsernameStatus === 'taken', 'field-available': regUsernameStatus === 'available' }">
            <input v-model="regAccount" type="text" placeholder="账号名 (3-20位)" maxlength="20" />
            <span v-if="regUsernameStatus === 'checking'" class="field-status status-checking" aria-hidden="true">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="spin"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
            </span>
            <span v-else-if="regUsernameStatus === 'available'" class="field-status status-ok" aria-hidden="true">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
            </span>
            <span v-else-if="regUsernameStatus === 'taken'" class="field-status status-bad" aria-hidden="true">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </span>
          </div>
          <div v-if="regUsernameHint" class="hint" :class="'hint-' + regUsernameStatus">{{ regUsernameHint }}</div>
          <div class="form-field">
            <input v-model="regPassword" :type="showRegPwd ? 'text' : 'password'" placeholder="密码 (至少6位)" @keyup.enter="onRegister" />
            <button type="button" class="eye-btn" @click="showRegPwd = !showRegPwd" :aria-label="showRegPwd ? '隐藏密码' : '显示密码'">
              <svg v-if="showRegPwd" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
            </button>
          </div>
          <div class="form-field">
            <input v-model="regConfirmPassword" :type="showRegConfirmPwd ? 'text' : 'password'" placeholder="确认密码" @keyup.enter="onRegister" />
            <button type="button" class="eye-btn" @click="showRegConfirmPwd = !showRegConfirmPwd" :aria-label="showRegConfirmPwd ? '隐藏密码' : '显示密码'">
              <svg v-if="showRegConfirmPwd" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
            </button>
          </div>

          <div v-if="regError" class="msg msg-error">{{ regError }}</div>
          <div v-if="regSuccess" class="msg msg-success">{{ regSuccess }}</div>

          <button class="submit-btn" :disabled="regLoading || regUsernameStatus === 'taken' || regUsernameStatus === 'checking'" @click="onRegister">
            {{ regLoading ? '注册中...' : '注 册' }}
          </button>

          <p class="mobile-switch">已有账号？<a @click="switchTo('login')">去登录</a></p>
        </div>
      </div>

      <!-- 登录表单（在右半边） -->
      <div class="form-container sign-in-container">
        <div class="form-inner" :class="{ 'form-visible': mode === 'login', 'form-hidden': mode !== 'login' }">
          <h1 class="form-title">登录</h1>

          <div class="form-field">
            <input v-model="loginUsername" type="text" placeholder="用户名" @keyup.enter="onLogin" />
          </div>
          <div class="form-field">
            <input v-model="loginPassword" :type="showLoginPwd ? 'text' : 'password'" placeholder="密码" @keyup.enter="onLogin" />
            <button type="button" class="eye-btn" @click="showLoginPwd = !showLoginPwd" :aria-label="showLoginPwd ? '隐藏密码' : '显示密码'">
              <svg v-if="showLoginPwd" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
            </button>
          </div>

          <a class="forgot-link" @click.prevent="onForgot">忘记密码？</a>

          <div v-if="loginError" class="msg msg-error">{{ loginError }}</div>

          <button class="submit-btn" :disabled="loginLoading" @click="onLogin">
            {{ loginLoading ? '登录中...' : '登 录' }}
          </button>

          <p class="mobile-switch">还没有账号？<a @click="switchTo('register')">立即注册</a></p>
        </div>
      </div>

      <!-- 蓝色覆盖层（在两半之间滑动） -->
      <div class="overlay-container">
        <div class="overlay" :class="mode === 'login' ? 'overlay-right' : 'overlay-left'">
          <div class="overlay-panel overlay-panel-left">
            <h1>欢迎回来！</h1>
            <p>已有账号？立即登录继续学习，畅享海量IT运维知识与实战文档</p>
            <button class="ghost-btn" @click="switchTo('register')">注 册</button>
          </div>
          <div class="overlay-panel overlay-panel-right">
            <h1>你好，朋友！</h1>
            <p>注册账号开启您的IT运维学习之旅，掌握网络命令、Linux、故障排查、Office等全方位实战技能</p>
            <button class="ghost-btn" @click="switchTo('login')">登 录</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 顶部品牌 + 返回 -->
    <div class="top-bar">
      <router-link to="/" class="brand">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
          <line x1="8" y1="21" x2="16" y2="21"/>
          <line x1="12" y1="17" x2="12" y2="21"/>
        </svg>
        <span>IT运维学习平台</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiAuth } from '../../api/index.js'

const route = useRoute()
const router = useRouter()

const mode = ref(route.path === '/register' ? 'register' : 'login')

function routeMode(path) {
  return path === '/register' ? 'register' : 'login'
}

function syncMode() {
  mode.value = routeMode(route.path)
}

function switchTo(target) {
  if (target === mode.value) return
  const targetPath = target === 'login' ? '/login' : '/register'
  mode.value = target
  if (route.path !== targetPath) router.push(targetPath)
}

onMounted(syncMode)
watch(() => route.path, (path) => { mode.value = routeMode(path) })

// 登录
const loginUsername = ref('')
const loginPassword = ref('')
const showLoginPwd = ref(false)
const loginLoading = ref(false)
const loginError = ref('')

async function onLogin() {
  if (!loginUsername.value || !loginPassword.value) {
    loginError.value = '请输入用户名和密码'
    return
  }
  loginLoading.value = true
  loginError.value = ''
  try {
    const res = await apiAuth.login({ username: loginUsername.value, password: loginPassword.value })
    const data = res.data.data
    localStorage.setItem('token', data.token)
    localStorage.setItem('username', data.username)
    localStorage.setItem('role', data.role)
    localStorage.setItem('userId', data.id || data.userId || '')
    window.dispatchEvent(new StorageEvent('storage', { key: 'token', newValue: data.token }))
    router.push('/')
  } catch (e) {
    loginError.value = e.response?.data?.msg || '登录失败，请重试'
  } finally {
    loginLoading.value = false
  }
}

function onForgot() {
  loginError.value = '请联系超级管理员重置密码'
}

// 注册
const regAccount = ref('')
const regPassword = ref('')
const regConfirmPassword = ref('')
const showRegPwd = ref(false)
const showRegConfirmPwd = ref(false)
const regLoading = ref(false)
const regError = ref('')
const regSuccess = ref('')

// 用户名实时校验：'idle' | 'checking' | 'available' | 'taken' | 'invalid'
const regUsernameStatus = ref('idle')
const regUsernameHint = ref('')
let regCheckTimer = null
let regCheckSeq = 0

watch(regAccount, (val) => {
  regUsernameStatus.value = 'idle'
  regUsernameHint.value = ''
  if (regCheckTimer) {
    clearTimeout(regCheckTimer)
    regCheckTimer = null
  }
  const name = (val || '').trim()
  if (!name) return
  if (name.length < 3 || name.length > 20) {
    regUsernameStatus.value = 'invalid'
    regUsernameHint.value = '账号名长度需3-20位'
    return
  }
  regUsernameStatus.value = 'checking'
  regUsernameHint.value = '正在检查账号名是否可用…'
  const seq = ++regCheckSeq
  regCheckTimer = setTimeout(async () => {
    try {
      const res = await apiAuth.checkUsername(name)
      if (seq !== regCheckSeq) return
      const data = res.data?.data
      if (data?.available) {
        regUsernameStatus.value = 'available'
        regUsernameHint.value = '账号名可用 ✓'
      } else {
        regUsernameStatus.value = 'taken'
        regUsernameHint.value = '该账号名已被占用，请更换一个'
      }
    } catch (e) {
      if (seq !== regCheckSeq) return
      regUsernameStatus.value = 'idle'
      regUsernameHint.value = ''
    }
  }, 400)
})

async function onRegister() {
  regError.value = ''
  regSuccess.value = ''
  if (!regAccount.value || !regPassword.value || !regConfirmPassword.value) {
    regError.value = '请填写所有字段'
    return
  }
  if (regAccount.value.length < 3 || regAccount.value.length > 20) {
    regError.value = '账号名长度3-20位'
    return
  }
  if (regPassword.value.length < 6) {
    regError.value = '密码至少6位'
    return
  }
  if (regPassword.value !== regConfirmPassword.value) {
    regError.value = '两次输入的密码不一致'
    return
  }
  if (regUsernameStatus.value === 'taken') {
    regError.value = '该账号名已被占用，请更换一个'
    return
  }
  if (regUsernameStatus.value === 'checking') {
    regError.value = '正在校验账号名，请稍候…'
    return
  }
  // 提交前再实时校验一次，防止用户输入完成后才被占用
  try {
    const res = await apiAuth.checkUsername(regAccount.value.trim())
    const data = res.data?.data
    if (data && data.available === false) {
      regUsernameStatus.value = 'taken'
      regUsernameHint.value = '该账号名已被占用，请更换一个'
      regError.value = '该账号名已被占用，请更换一个'
      return
    }
  } catch (_) {}
  regLoading.value = true
  try {
    const res = await apiAuth.register({ username: regAccount.value, password: regPassword.value })
    regSuccess.value = res.data.data.message || '注册成功，等待超级管理员审核'
    regAccount.value = ''
    regPassword.value = ''
    regConfirmPassword.value = ''
    regUsernameStatus.value = 'idle'
    regUsernameHint.value = ''
  } catch (e) {
    if (e.response?.status === 409) {
      regUsernameStatus.value = 'taken'
      regUsernameHint.value = '该账号名已被占用，请更换一个'
    }
    regError.value = e.response?.data?.msg || '注册失败'
  } finally {
    regLoading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
}

.auth-bg {
  position: fixed;
  inset: 0;
  background: #fff;
  z-index: 0;
}
.auth-bg::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -30%;
  width: 80%;
  height: 80%;
  background: transparent;
  border-radius: 50%;
}
.auth-bg::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -20%;
  width: 60%;
  height: 60%;
  background: transparent;
  border-radius: 50%;
}

.top-bar {
  position: absolute;
  top: 24px;
  left: 24px;
  z-index: 10;
}
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  letter-spacing: -.3px;
  padding: 8px 14px;
  background: #2563eb;
  backdrop-filter: blur(8px);
  border-radius: 10px;
  border: 1px solid #2563eb;
  transition: all .2s;
}
.brand:hover {
  background: #1d4ed8;
  color: #2563eb;
  transform: translateY(-1px);
}

/* ───── 主卡片 ───── */
.auth-card {
  position: relative;
  z-index: 1;
  width: 850px;
  max-width: 100%;
  min-height: 540px;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, .12), 0 2px 6px rgba(15, 23, 42, .04);
  overflow: hidden;
}
.auth-card::after {
  display: none;
}

/* 两个表单容器（左右两半，叠加在一起） */
.form-container {
  position: absolute;
  top: 0;
  height: 100%;
  width: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 40px;
  transition: transform .7s cubic-bezier(.65, 0, .35, 1);
  will-change: transform;
}
.sign-up-container { left: 0; }
.sign-in-container { right: 0; }

.form-inner {
  width: 100%;
  max-width: 320px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  transition: opacity .5s cubic-bezier(.65, 0, .35, 1), transform .5s cubic-bezier(.65, 0, .35, 1);
}
.form-hidden { opacity: 0; transform: translateY(12px); pointer-events: none; }
.form-visible { opacity: 1; transform: translateY(0); transition-delay: 0.5s; }

.form-title {
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 18px;
  letter-spacing: -.5px;
}

.form-field {
  margin-bottom: 12px;
  background: #f1f5f9;
  border-radius: 8px;
  transition: all .2s;
  position: relative;
}
.form-field:focus-within {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(13, 148, 136, .15);
}
.form-field.field-taken:focus-within { box-shadow: 0 0 0 2px rgba(220, 38, 38, .18); }
.form-field.field-available:focus-within { box-shadow: 0 0 0 2px rgba(16, 185, 129, .22); }
.form-field.field-taken { background: #fef2f2; }
.form-field.field-available { background: #ecfdf5; }
.form-field input {
  width: 100%;
  padding: 11px 40px 11px 14px;
  border: none;
  background: transparent;
  font-size: 13px;
  color: #1e293b;
  outline: none;
}

.field-status {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  pointer-events: none;
}
.status-checking { color: #64748b; }
.status-ok       { color: #059669; }
.status-bad      { color: #dc2626; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.hint {
  font-size: 11.5px;
  line-height: 1.4;
  margin: -6px 4px 10px;
  display: flex;
  align-items: center;
  gap: 4px;
  min-height: 14px;
  transition: color .2s;
}
.hint-checking  { color: #64748b; }
.hint-available { color: #059669; }
.hint-taken     { color: #dc2626; font-weight: 600; }
.hint-invalid   { color: #d97706; }
.eye-btn {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  width: 30px;
  height: 30px;
  border: none;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all .15s;
}
.eye-btn:hover {
  color: #0d9488;
  background: rgba(13, 148, 136, .08);
}
.eye-btn:active { transform: translateY(-50%) scale(.92); }

.forgot-link {
  display: block;
  text-align: center;
  font-size: 12px;
  color: #64748b;
  text-decoration: none;
  margin: 4px 0 14px;
  cursor: pointer;
  transition: color .2s;
}
.forgot-link:hover { color: #0d9488; }

.msg {
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  margin-bottom: 10px;
  text-align: center;
}
.msg-error { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }
.msg-success { background: #ecfdf5; color: #059669; border: 1px solid #a7f3d0; }

.submit-btn {
  width: 100%;
  padding: 11px;
  background: #0d9488;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 4px;
  cursor: pointer;
  transition: all .2s;
  box-shadow: 0 4px 12px rgba(13, 148, 136, .25);
  margin-top: 4px;
}
.submit-btn:hover:not(:disabled) {
  background: #0f766e;
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(13, 148, 136, .35);
}
.submit-btn:active:not(:disabled) { transform: translateY(0); }
.submit-btn:disabled { opacity: .55; cursor: not-allowed; }

.mobile-switch {
  text-align: center;
  font-size: 12px;
  color: #64748b;
  margin: 12px 0 0;
  display: none;
}
.mobile-switch a {
  color: #0d9488;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
}

/* ───── 蓝色覆盖层（滑动） ───── */
.overlay-container {
  position: absolute;
  top: 0;
  left: 50%;
  width: 50%;
  height: 100%;
  overflow: hidden;
  transition: transform .7s cubic-bezier(.65, 0, .35, 1);
  z-index: 10;
  will-change: transform;
}
.auth-card.panel-right .overlay-container { transform: translateX(-100%); }

.overlay {
  position: relative;
  left: -100%;
  width: 200%;
  height: 100%;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 50%, #1d4ed8 100%);
  color: #fff;
  display: flex;
}
.overlay::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, .18) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(255, 255, 255, .12) 0%, transparent 50%);
  pointer-events: none;
}
.overlay-left { transform: translateX(0); }
.overlay-right { transform: translateX(50%); }

.overlay-panel {
  position: relative;
  width: 50%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 50px;
  text-align: center;
  z-index: 1;
}
.overlay-panel h1 {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 14px;
  letter-spacing: -.5px;
}
.overlay-panel p {
  font-size: 13px;
  line-height: 1.7;
  margin: 0 0 24px;
  color: rgba(255, 255, 255, .9);
  max-width: 280px;
}
.ghost-btn {
  padding: 10px 38px;
  background: transparent;
  color: #fff;
  border: 1.5px solid rgba(255, 255, 255, .85);
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 4px;
  cursor: pointer;
  transition: all .2s;
}
.ghost-btn:hover {
  background: rgba(255, 255, 255, .15);
  border-color: #fff;
  transform: translateY(-1px);
}

/* ───── 切换动画：登录状态 ───── */
.auth-card.panel-right .sign-in-container { z-index: 5; }
.auth-card.panel-right .sign-up-container { z-index: 1; }

/* ───── 切换动画：注册状态（默认） ───── */
.auth-card.panel-left .sign-up-container { z-index: 5; }
.auth-card.panel-left .sign-in-container { z-index: 1; }

/* ───── 移动端 ───── */
@media (max-width: 768px) {
  .auth-page { padding: 16px; }
  .auth-card {
    min-height: 620px;
    width: 100%;
    border-radius: 18px;
  }
  .overlay-container { display: none; }
  .form-container {
    width: 100%;
    left: 0;
    padding: 0 28px;
  }
  .auth-card.panel-right .sign-in-container { transform: none; }
  .auth-card.panel-left .sign-up-container { transform: none; }
  .auth-card.panel-right .sign-up-container,
  .auth-card.panel-left .sign-in-container { display: none; }
  .form-inner { max-width: 100%; }
  .form-title { font-size: 22px; }
  .mobile-switch { display: block; }
  .top-bar { top: 16px; left: 16px; }
  .brand { font-size: 14px; padding: 6px 12px; }
}
</style>
