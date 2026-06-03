<template>
  <div class="auth-page">
    <div class="auth-bg"></div>
    <div class="auth-content">
      <div class="auth-card">
        <router-link to="/" class="auth-logo">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
            <line x1="8" y1="21" x2="16" y2="21"/>
            <line x1="12" y1="17" x2="12" y2="21"/>
          </svg>
          <span>IT运维学习平台</span>
        </router-link>

        <h2 class="auth-title">注册管理员账号</h2>
        <p class="auth-desc">注册后需等待超级管理员审核通过</p>

        <div class="auth-field">
          <label>用户名</label>
          <input v-model="username" type="text" placeholder="3-20位字符">
        </div>
        <div class="auth-field">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="至少6位密码">
        </div>
        <div class="auth-field">
          <label>确认密码</label>
          <input v-model="confirmPassword" type="password" placeholder="再次输入密码">
        </div>

        <div v-if="error" class="auth-error">{{ error }}</div>
        <div v-if="success" class="auth-success">{{ success }}</div>

        <button class="auth-btn-submit" :disabled="loading" @click="onRegister">
          {{ loading ? '注册中...' : '注 册' }}
        </button>

        <div class="auth-footer">
          <router-link to="/login">已有账号？去登录</router-link>
          <router-link to="/">返回首页</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { apiAuth } from '../../api/index.js'

const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')

async function onRegister() {
  if (!username.value || !password.value) {
    error.value = '请填写用户名和密码'
    return
  }
  if (username.value.length < 3 || username.value.length > 20) {
    error.value = '用户名长度3-20位'
    return
  }
  if (password.value.length < 6) {
    error.value = '密码至少6位'
    return
  }
  if (password.value !== confirmPassword.value) {
    error.value = '两次密码不一致'
    return
  }
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    const res = await apiAuth.register({ username: username.value, password: password.value })
    success.value = res.data.data.message || '注册成功，等待超级管理员审核'
    username.value = ''
    password.value = ''
    confirmPassword.value = ''
  } catch (e) {
    error.value = e.response?.data?.msg || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page{min-height:100vh;position:relative;overflow:hidden}
.auth-bg{position:fixed;inset:0;background:linear-gradient(135deg,#f0f4ff 0%,#e8f0fe 30%,#f5f3ff 60%,#fef3c7 100%);z-index:0}
.auth-bg::before{content:'';position:absolute;top:-50%;right:-30%;width:80%;height:80%;background:radial-gradient(circle,rgba(37,99,235,.06) 0%,transparent 70%);border-radius:50%}
.auth-bg::after{content:'';position:absolute;bottom:-30%;left:-20%;width:60%;height:60%;background:radial-gradient(circle,rgba(124,58,237,.05) 0%,transparent 70%);border-radius:50%}
.auth-content{position:relative;z-index:1;display:flex;align-items:center;justify-content:center;min-height:100vh;padding:24px}
.auth-card{width:100%;max-width:400px;background:#fff;border-radius:16px;padding:36px 32px;border:1.5px solid #e2e8f0;box-shadow:0 4px 24px rgba(0,0,0,.06)}
.auth-logo{display:flex;align-items:center;gap:10px;text-decoration:none;margin-bottom:28px}
.auth-logo span{font-size:18px;font-weight:700;color:#0f172a}
.auth-title{font-size:24px;font-weight:700;color:#0f172a;margin:0 0 6px}
.auth-desc{font-size:14px;color:#64748b;margin:0 0 24px}
.auth-field{margin-bottom:18px}
.auth-field label{display:block;font-size:13px;font-weight:600;color:#334155;margin-bottom:6px}
.auth-field input{width:100%;padding:11px 14px;border:1.5px solid #e2e8f0;border-radius:8px;font-size:14px;color:#1e293b;outline:none;transition:all .2s;background:#f8fafc;box-sizing:border-box}
.auth-field input:focus{border-color:#2563eb;box-shadow:0 0 0 3px rgba(37,99,235,.1);background:#fff}
.auth-error{padding:10px 14px;background:#fef2f2;border:1px solid #fecaca;border-radius:8px;color:#dc2626;font-size:13px;margin-bottom:16px}
.auth-success{padding:10px 14px;background:#ecfdf5;border:1px solid #a7f3d0;border-radius:8px;color:#059669;font-size:13px;margin-bottom:16px}
.auth-btn-submit{width:100%;padding:12px;background:#2563eb;color:#fff;border:none;border-radius:8px;font-size:16px;font-weight:600;cursor:pointer;transition:all .2s}
.auth-btn-submit:hover{background:#1d4ed8;box-shadow:0 4px 12px rgba(37,99,235,.3)}
.auth-btn-submit:disabled{opacity:.5;cursor:not-allowed}
.auth-footer{display:flex;justify-content:space-between;margin-top:16px}
.auth-footer a{font-size:13px;color:#64748b;text-decoration:none;transition:color .2s}
.auth-footer a:hover{color:#2563eb}
</style>