/**
 * PDF导出工具 - 构建排版HTML并导出为PDF
 * 特点：单列布局，图片一行一张，内容格式化输出
 */

function loadHtml2Pdf() {
  return new Promise((resolve, reject) => {
    if (window.html2pdf) { resolve(); return }
    const s = document.createElement('script')
    s.src = 'https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js'
    s.onload = resolve; s.onerror = reject; document.head.appendChild(s)
  })
}

function isImageUrl(url) {
  return /\.(png|jpg|jpeg|gif|webp|svg|bmp)(\?|$)/i.test(url)
}

function isVideoUrl(url) {
  return /\.(mp4|webm|ogg|mov)(\?|$)/i.test(url)
}

function escapeHtml(str) {
  if (!str) return ''
  return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
}

/**
 * 构建单个媒体项的HTML（图片/视频）
 */
function buildMediaHtml(mediaUrl) {
  if (!mediaUrl) return ''
  if (isImageUrl(mediaUrl)) {
    return `<div style="margin-bottom:12px;text-align:center;"><img src="${mediaUrl}" style="max-width:100%;height:auto;display:block;margin:0 auto;border-radius:4px;border:1px solid #e2e8f0;" /></div>`
  }
  if (isVideoUrl(mediaUrl)) {
    return `<div style="margin-bottom:12px;text-align:center;color:#64748b;font-size:12px;padding:8px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:4px;">[视频: ${mediaUrl.split('/').pop()}]</div>`
  }
  return `<div style="margin-bottom:12px;"><a href="${mediaUrl}" style="color:#2563eb;font-size:13px;">${mediaUrl.split('/').pop()}</a></div>`
}

const STYLE = `
<style>
  * { margin:0; padding:0; box-sizing:border-box; }
  body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "PingFang SC", "Microsoft YaHei", sans-serif; font-size:14px; color:#1e293b; line-height:1.7; padding:20px; background:#fff; }
  .pdf-title { font-size:22px; font-weight:700; color:#0f172a; margin-bottom:20px; padding-bottom:12px; border-bottom:2px solid #2563eb; }
  .pdf-section { margin-bottom:18px; }
  .pdf-label { font-size:13px; font-weight:600; color:#2563eb; margin-bottom:6px; }
  .pdf-value { font-size:14px; color:#334155; white-space:pre-wrap; word-break:break-word; }
  .pdf-media { margin-bottom:4px; }
  .pdf-empty { color:#94a3b8; font-size:13px; }
  .pdf-footer { margin-top:24px; padding-top:12px; border-top:1px solid #e2e8f0; font-size:12px; color:#94a3b8; }
  .pdf-code { background:#f1f5f9; padding:10px 14px; border-radius:4px; font-family:"Consolas","Monaco","Courier New",monospace; font-size:12px; white-space:pre-wrap; word-break:break-all; color:#1e293b; }
  .pdf-file { font-size:13px; padding:4px 0; }
  .pdf-file a { color:#2563eb; text-decoration:none; }
</style>
`

/**
 * 构建PDF用的HTML内容
 * @param {Object} options
 * @param {string} options.title - 标题
 * @param {Array} options.sections - 内容区块
 * @param {string} options.sections[].label - 区块标签
 * @param {string} options.sections[].type - 'text' | 'media' | 'code' | 'files' | 'html'
 * @param {*} options.sections[].value - 内容值
 * @param {string} options.footer - 页脚文字
 */
export function buildPdfHtml({ title, sections = [], footer = '' }) {
  let sectionsHtml = ''

  for (const sec of sections) {
    if (!sec || sec.value === undefined || sec.value === null || sec.value === '') continue

    sectionsHtml += `<div class="pdf-section"><div class="pdf-label">${escapeHtml(sec.label)}</div>`

    if (sec.type === 'media') {
      // 媒体文件：图片一行一张
      const mediaList = Array.isArray(sec.value) ? sec.value : [sec.value]
      const hasContent = mediaList.some(m => m)
      if (!hasContent) {
        sectionsHtml += `<div class="pdf-empty">暂无</div>`
      } else {
        sectionsHtml += `<div class="pdf-media">`
        for (const m of mediaList) {
          sectionsHtml += buildMediaHtml(m)
        }
        sectionsHtml += `</div>`
      }
    } else if (sec.type === 'files') {
      const fileList = Array.isArray(sec.value) ? sec.value : [sec.value]
      const hasContent = fileList.some(f => f)
      if (!hasContent) {
        sectionsHtml += `<div class="pdf-empty">暂无</div>`
      } else {
        for (const f of fileList) {
          const name = typeof f === 'string' ? f.split('/').pop() : (f.name || '未知文件')
          const url = typeof f === 'string' ? f : (f.url || '')
          sectionsHtml += `<div class="pdf-file"><a href="${url}">${escapeHtml(name)}</a></div>`
        }
      }
    } else if (sec.type === 'code') {
      sectionsHtml += `<pre class="pdf-code">${escapeHtml(String(sec.value))}</pre>`
    } else if (sec.type === 'html') {
      sectionsHtml += sec.value
    } else {
      // 默认文本类型
      sectionsHtml += `<div class="pdf-value">${escapeHtml(String(sec.value))}</div>`
    }

    sectionsHtml += `</div>`
  }

  if (footer) {
    sectionsHtml += `<div class="pdf-footer">${escapeHtml(footer)}</div>`
  }

  return `<!DOCTYPE html><html><head><meta charset="utf-8">${STYLE}</head><body><div class="pdf-title">${escapeHtml(title)}</div>${sectionsHtml}</body></html>`
}

/**
 * 导出PDF
 * @param {Object} options - 同buildPdfHtml参数
 * @param {string} filename - 文件名（不含扩展名）
 */
export async function exportPdf(options, filename) {
  const btn = document.querySelector('.btn-pdf')
  if (btn) { btn.textContent = '生成中...'; btn.disabled = true }

  try {
    await loadHtml2Pdf()

    const html = buildPdfHtml(options)

    // 创建临时容器
    const container = document.createElement('div')
    container.innerHTML = html
    container.style.position = 'fixed'
    container.style.left = '-9999px'
    container.style.top = '0'
    container.style.width = '210mm'
    container.style.background = '#fff'
    container.style.zIndex = '-1'
    document.body.appendChild(container)

    const opt = {
      margin: [10, 12, 10, 12],
      filename: (filename || '导出') + '.pdf',
      image: { type: 'jpeg', quality: 0.95 },
      html2canvas: { scale: 2, useCORS: true, logging: false },
      jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
    }

    await window.html2pdf().set(opt).from(container).save()

    // 清理临时容器
    document.body.removeChild(container)
  } catch (e) {
    alert('PDF生成失败，请检查网络连接后重试')
  } finally {
    if (btn) { btn.textContent = '导出PDF'; btn.disabled = false }
  }
}