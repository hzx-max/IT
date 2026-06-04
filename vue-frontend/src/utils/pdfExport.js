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

function buildMediaHtml(mediaUrl) {
  if (!mediaUrl) return ''
  if (isImageUrl(mediaUrl)) {
    return `<div style="margin-bottom:14px;text-align:center;"><img src="${mediaUrl}" style="width:100%;height:auto;max-height:200mm;display:block;margin:0 auto;border-radius:4px;border:1px solid #e2e8f0;" /></div>`
  }
  if (isVideoUrl(mediaUrl)) {
    return `<div style="margin-bottom:14px;color:#64748b;font-size:14pt;font-family:'Times New Roman','宋体',SimSun,serif;padding:8px;background:#f8fafc;border:1px solid #e2e8f0;border-radius:4px;">[视频: ${mediaUrl.split('/').pop()}]</div>`
  }
  return `<div style="margin-bottom:14px;"><a href="${mediaUrl}" style="color:#2563eb;font-size:14pt;font-family:'Times New Roman','宋体',SimSun,serif;">${mediaUrl.split('/').pop()}</a></div>`
}

/**
 * 构建PDF用的HTML内容块（纯内容，不含html/head/body标签）
 */
export function buildPdfHtml({ title, sections = [], footer = '' }) {
  let html = `<style>
  * { margin:0; padding:0; box-sizing:border-box; }
  .pdf-title { font-size:16pt; font-weight:bold; font-family:"Times New Roman","宋体",SimSun,serif; color:#1e3a5f; margin-bottom:20px; padding:14px 16px; background:#dbeafe; border-left:4px solid #2563eb; border-bottom:2px solid #93c5fd; border-radius:0 4px 4px 0; }
  .pdf-section { margin-bottom:18px; break-inside:avoid-page; }
  .pdf-label { font-size:15pt; font-weight:bold; font-family:"Times New Roman","宋体",SimSun,serif; color:#1e40af; margin-bottom:6px; padding:6px 10px; background:#dbeafe; border-radius:4px; display:inline-block; }
  .pdf-value { font-size:14pt; font-family:"Times New Roman","宋体",SimSun,serif; color:#334155; white-space:pre-wrap; word-break:break-word; text-indent:2em; line-height:1.8; break-inside:avoid-page; }
  .pdf-empty { color:#94a3b8; font-size:14pt; font-family:"Times New Roman","宋体",SimSun,serif; }
  .pdf-footer { margin-top:24px; padding-top:12px; border-top:1px solid #e2e8f0; font-size:14pt; font-family:"Times New Roman","宋体",SimSun,serif; color:#94a3b8; }
  .pdf-code { background:#f1f5f9; padding:10px 14px; border-radius:4px; font-family:"Consolas","Monaco","Courier New",monospace; font-size:14pt; white-space:pre-wrap; word-break:break-all; color:#1e293b; line-height:1.7; }
  .pdf-file { font-size:14pt; font-family:"Times New Roman","宋体",SimSun,serif; padding:4px 0; }
  .pdf-file a { color:#2563eb; text-decoration:none; }
</style><h1 class="pdf-title">${escapeHtml(title)}</h1>`

  for (const sec of sections) {
    if (!sec || sec.value === undefined || sec.value === null || sec.value === '') continue
    if (Array.isArray(sec.value) && sec.value.length === 0) continue

    html += `<div class="pdf-section"><div class="pdf-label">${escapeHtml(sec.label)}</div>`

    if (sec.type === 'media') {
      const mediaList = Array.isArray(sec.value) ? sec.value : [sec.value]
      const valid = mediaList.filter(m => m)
      if (valid.length === 0) {
        html += `<div class="pdf-empty">暂无</div>`
      } else {
        for (const m of valid) {
          html += buildMediaHtml(m)
        }
      }
    } else if (sec.type === 'files') {
      const fileList = Array.isArray(sec.value) ? sec.value : [sec.value]
      const valid = fileList.filter(f => f)
      if (valid.length === 0) {
        html += `<div class="pdf-empty">暂无</div>`
      } else {
        for (const f of valid) {
          const name = typeof f === 'string' ? f.split('/').pop() : (f.name || '未知文件')
          const url = typeof f === 'string' ? f : (f.url || '')
          html += `<div class="pdf-file"><a href="${url}">${escapeHtml(name)}</a></div>`
        }
      }
    } else if (sec.type === 'code') {
      html += `<pre class="pdf-code">${escapeHtml(String(sec.value))}</pre>`
    } else {
      html += `<div class="pdf-value">${escapeHtml(String(sec.value))}</div>`
    }

    html += `</div>`
  }

  if (footer) {
    html += `<div class="pdf-footer">${escapeHtml(footer)}</div>`
  }

  return html
}

/**
 * 等待容器内所有图片加载完成（超时10秒）
 */
function waitForImages(container, timeout = 10000) {
  const imgs = container.querySelectorAll('img')
  if (imgs.length === 0) return Promise.resolve()
  const promises = []
  imgs.forEach(img => {
    if (img.complete) return
    promises.push(new Promise((resolve) => {
      const timer = setTimeout(resolve, timeout)
      img.onload = () => { clearTimeout(timer); resolve() }
      img.onerror = () => { clearTimeout(timer); resolve() }
    }))
  })
  return Promise.all(promises)
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

    const contentHtml = buildPdfHtml(options)

    // 创建全屏遮罩容器，确保html2canvas能正确渲染
    const overlay = document.createElement('div')
    overlay.id = '__pdf_export_overlay__'
    overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;z-index:999999;background:#fff;overflow-y:auto;'

    const container = document.createElement('div')
    container.id = '__pdf_export_content__'
    container.style.cssText = `
      width: 210mm;
      min-height: 100%;
      margin: 0 auto;
      padding: 20px 24px;
      background: #fff;
      font-family: "Times New Roman", "宋体", SimSun, serif;
      font-size: 14pt;
      color: #1e293b;
      line-height: 1.7;
    `
    container.innerHTML = contentHtml
    overlay.appendChild(container)
    document.body.appendChild(overlay)

    // 等待图片加载
    await waitForImages(container)
    // 再等一帧确保布局稳定
    await new Promise(r => requestAnimationFrame(r))

    const opt = {
      margin: [2, 2, 2, 2],
      filename: (filename || '导出') + '.pdf',
      image: { type: 'jpeg', quality: 0.95 },
      html2canvas: { scale: 2, useCORS: true, logging: false },
      jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' },
      pagebreak: { mode: ['avoid-all', 'css', 'legacy'] }
    }

    await window.html2pdf().set(opt).from(container).save()

    // 清理
    document.body.removeChild(overlay)
  } catch (e) {
    // 清理可能残留的overlay
    const old = document.getElementById('__pdf_export_overlay__')
    if (old) document.body.removeChild(old)
    alert('PDF生成失败，请检查网络连接后重试')
    console.error('PDF导出错误:', e)
  } finally {
    if (btn) { btn.textContent = '导出PDF'; btn.disabled = false }
  }
}