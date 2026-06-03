import { apiAdmin } from './modules.js'

/**
 * 如果用户是普通管理员，通过审核流程提交变更；超级管理员直接提交。
 * @param {string} module - 模块名 (cmd, fault, desktop, linux, office, ai)
 * @param {string} operation - 操作 (CREATE, UPDATE, DELETE)
 * @param {Object|null} payload - 请求数据
 * @param {number|string|null} entityId - 实体ID
 * @param {Function} directApiCall - 超级管理员直接调用的API函数
 * @returns {Promise<{ok: boolean, message: string}>}
 */
export async function submitWithApproval(module, operation, payload, entityId, directApiCall) {
  const role = localStorage.getItem('role')
  const userId = localStorage.getItem('userId')
  const username = localStorage.getItem('username')

  if (role === 'ADMIN') {
    // 普通管理员走审核流程
    try {
      await apiAdmin.submitPendingChange({
        module,
        operation,
        entityId: entityId || null,
        payload,
        submitterId: parseInt(userId) || 0,
        submitterName: username || 'unknown'
      })
      return { ok: true, message: '已提交审核，等待超级管理员确认' }
    } catch (e) {
      return { ok: false, message: e.response?.data?.msg || '提交审核失败' }
    }
  }

  // 超级管理员直接执行
  try {
    await directApiCall()
    return { ok: true, message: '操作成功' }
  } catch (e) {
    return { ok: false, message: e.response?.data?.msg || '操作失败' }
  }
}