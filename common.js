// ===== 常量 =====
const VENDOR_MAP = { huawei:{n:'华为',c:'#cf1322'}, h3c:{n:'华三',c:'#08979c'}, cisco:{n:'Cisco',c:'#1a73e8'}, ruijie:{n:'锐捷',c:'#d4380d'}, fiberhome:{n:'烽火',c:'#722ed1'}, maipu:{n:'迈普',c:'#13c2c2'}, zte:{n:'中兴',c:'#1677ff'} };
const CAT_MAP = { basic:'基础配置', vlan:'VLAN', routing:'路由', ospf:'OSPF', bgp:'BGP', acl:'ACL/NAT', security:'安全', stp:'STP/冗余', wlan:'WLAN/无线', manage:'管理维护', agg:'链路聚合', mpls:'MPLS/VPN', dhcp:'DHCP', qos:'QoS' };
const LINUX_VENDOR_MAP = { centos:{n:'CentOS',c:'#932279'}, ubuntu:{n:'Ubuntu',c:'#E95420'}, debian:{n:'Debian',c:'#A80030'}, redhat:{n:'RedHat',c:'#EE0000'}, suse:{n:'SUSE',c:'#73BA25'}, rocky:{n:'Rocky',c:'#10B981'}, alpine:{n:'Alpine',c:'#0D597F'}, arch:{n:'Arch',c:'#1793D1'} };
const LINUX_CAT_MAP = { basic:'基础操作', file:'文件管理', user:'用户权限', network:'网络配置', service:'服务管理', disk:'磁盘管理', package:'软件包', process:'进程管理', firewall:'防火墙', shell:'Shell脚本', cron:'定时任务', backup:'备份恢复', monitor:'监控日志', security:'安全加固' };
const OFFICE_VENDOR_MAP = { word:{n:'Word',c:'#2B579A'}, excel:{n:'Excel',c:'#217346'}, ppt:{n:'PowerPoint',c:'#B7472A'}, outlook:{n:'Outlook',c:'#0078D4'}, wps_word:{n:'WPS文字',c:'#D4380D'}, wps_excel:{n:'WPS表格',c:'#08979C'}, wps_ppt:{n:'WPS演示',c:'#722ED1'}, libre:{n:'LibreOffice',c:'#18A303'} };
const OFFICE_CAT_MAP = { basic:'基础操作', format:'格式排版', formula:'公式函数', chart:'图表', data:'数据处理', mail:'邮件管理', macro:'宏/VBA', template:'模板', print:'打印', share:'协作共享', security:'安全', shortcut:'快捷键', style:'样式主题', insert:'插入对象' };

// ===== 时间格式化 =====
function formatTime(ts){
 if(!ts)return '';
 if(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/.test(ts))return ts;
 try{
  const d=new Date(ts);
  if(isNaN(d.getTime()))return ts;
  const pad=n=>String(n).padStart(2,'0');
  return d.getFullYear()+'-'+pad(d.getMonth()+1)+'-'+pad(d.getDate())+' '+pad(d.getHours())+':'+pad(d.getMinutes());
 }catch(e){return ts;}
}

// ===== PDF导出 =====
function loadHtml2Pdf(){
 return new Promise((resolve,reject)=>{
  if(window.html2pdf){resolve();return;}
  const s=document.createElement('script');
  s.src='https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js';
  s.onload=resolve;s.onerror=reject;document.head.appendChild(s);
 });
}
async function exportPDF(title){
 const btn=document.querySelector('.btn-pdf');if(btn){btn.textContent='生成中...';btn.disabled=true;}
 try{
  await loadHtml2Pdf();
  const el=document.querySelector('.detail-page');
  const opt={margin:[10,10,10,10],filename:(title||'导出')+'.pdf',image:{type:'jpeg',quality:0.95},html2canvas:{scale:2,useCORS:true,logging:false},jsPDF:{unit:'mm',format:'a4',orientation:'portrait'}};
  await html2pdf().set(opt).from(el).save();
 }catch(e){alert('PDF生成失败，请检查网络连接后重试');console.error(e);}
 finally{if(btn){btn.textContent='导出PDF';btn.disabled=false;}}
}

// ===== 自定义分类标签（从API加载） =====
let catLabels = {};
async function loadCatLabels(){
 try{ const r=await fetch('/api/categories'); catLabels=await r.json(); }catch(e){ catLabels={}; }
}
function getCatLabel(key){ return catLabels[key]||CAT_MAP[key]||key; }
async function saveCatLabel(key,label){
 try{ await apiPost('/api/categories',{cat_key:key,cat_label:label}); catLabels[key]=label; }catch(e){ console.error(e); }
}
async function deleteCatLabel(key){
 try{ await apiDelete('/api/categories/'+key); delete catLabels[key]; }catch(e){ console.error(e); }
}

// ===== 分类排除（持久化删除/隐藏分类） =====
let catExclusions = new Set();
async function loadCatExclusions(){
 try{ const r=await fetch('/api/categories/exclusions'); const arr=await r.json(); catExclusions=new Set(arr); }catch(e){ catExclusions=new Set(); }
}
async function addCatExclusion(key){
 try{ await apiPost('/api/categories/exclusions',{cat_key:key}); catExclusions.add(key); }catch(e){ console.error(e); }
}
async function removeCatExclusion(key){
 try{ await apiDelete('/api/categories/exclusions/'+key); catExclusions.delete(key); }catch(e){ console.error(e); }
}
// 获取所有分类（过滤被排除的分类和自定义标签的显示名）
function getAllCategories(){
 const cats=new Set();
 topics.forEach(c=>{if(c.cat)cats.add(c.cat);});
 Object.keys(CAT_MAP).forEach(k=>cats.add(k));
 return [...cats].filter(c=>!catExclusions.has(c)).sort();
}

function getAllVendorsFromTopics(){
 const vendors=new Set();
 topics.forEach(t=>{
  if(t.configs) t.configs.forEach(cfg=>{if(cfg.vendor) vendors.add(cfg.vendor);});
 });
 return [...vendors];
}

// ===== 数据层 (SQLite API) =====
let topics = [];

async function loadData() {
  try {
    const res = await fetch('/api/topics');
    if (!res.ok) throw new Error('API error');
    topics = await res.json();
  } catch (e) {
    console.error('加载数据失败:', e);
  }
  await loadCatLabels();
  await loadCatExclusions();
}

async function loadCommandDetail(id) {
  try {
    const res = await fetch('/api/topics/' + encodeURIComponent(id));
    if (!res.ok) throw new Error('API error');
    return await res.json();
  } catch (e) {
    console.error('加载详情失败:', e);
    return null;
  }
}

// 通过API保存的辅助函数
async function apiPost(url, data) {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

async function apiPut(url, data) {
  const res = await fetch(url, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

async function apiDelete(url) {
  const res = await fetch(url, { method: 'DELETE' });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

async function apiBatchDelete(ids) {
  const res = await fetch('/api/topics/batch-delete', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ids })
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

function saveData() {
}

// ===== 工具函数 =====
function esc(s){if(!s)return'';const d=document.createElement('div');d.textContent=s;return d.innerHTML;}
function getVendorName(k){return VENDOR_MAP[k]?.n||k;}
function getVendorColor(k){return VENDOR_MAP[k]?.c||'#666';}
function showToast(m){
 const overlay=document.createElement('div');
 overlay.className='modal-overlay';
 const box=document.createElement('div');
 box.className='modal-box modal-box-toast';
 const closeBtn=document.createElement('button');
 closeBtn.className='toast-close';
 closeBtn.innerHTML='&times;';
 closeBtn.onclick=()=>{overlay.style.opacity='0';overlay.style.transition='opacity .3s';setTimeout(()=>overlay.remove(),300);};
 box.appendChild(closeBtn);
 const msg=document.createElement('div');
 msg.className='modal-msg';
 msg.textContent=m;
 box.appendChild(msg);
 overlay.appendChild(box);
 document.body.appendChild(overlay);
 setTimeout(()=>{overlay.style.opacity='0';overlay.style.transition='opacity .3s';setTimeout(()=>overlay.remove(),300);},1800);
 overlay.addEventListener('click',()=>overlay.remove());
}

// ===== 自定义弹窗（替代 confirm/prompt） =====
function showModal(opts){
 return new Promise(resolve=>{
  const overlay=document.createElement('div');
  overlay.className='modal-overlay';
  const box=document.createElement('div');
  box.className='modal-box';
  const closeBtn=document.createElement('button');
  closeBtn.className='modal-close';
  closeBtn.innerHTML='&times;';
  closeBtn.onclick=()=>{overlay.remove();resolve(opts.type==='prompt'?null:false);};
  box.appendChild(closeBtn);
  const msg=document.createElement('div');
  msg.className='modal-msg';
  msg.textContent=opts.message||'';
  box.appendChild(msg);
  let promptInput=null;
  if(opts.type==='prompt'){
   promptInput=document.createElement('input');
   promptInput.className='modal-input';
   promptInput.value=opts.defaultValue||'';
   box.appendChild(promptInput);
   setTimeout(()=>promptInput.focus(),50);
   promptInput.addEventListener('keydown',e=>{if(e.key==='Enter'){overlay.remove();resolve(promptInput.value);}if(e.key==='Escape'){overlay.remove();resolve(null);}});
  }
  const btns=document.createElement('div');
  btns.className='modal-btns';
  const cancelBtn=document.createElement('button');
  cancelBtn.className='modal-btn modal-btn-cancel';
  cancelBtn.textContent=opts.cancelText||'取消';
  cancelBtn.onclick=()=>{overlay.remove();resolve(opts.type==='prompt'?null:false);};
  const okBtn=document.createElement('button');
  okBtn.className='modal-btn modal-btn-ok';
  okBtn.textContent=opts.okText||'确定';
  okBtn.onclick=()=>{overlay.remove();resolve(opts.type==='prompt'?promptInput.value:true);};
  btns.appendChild(cancelBtn);
  btns.appendChild(okBtn);
  box.appendChild(btns);
  overlay.appendChild(box);
  document.body.appendChild(overlay);
  overlay.addEventListener('click',e=>{if(e.target===overlay){overlay.remove();resolve(opts.type==='prompt'?null:false);}});
 });
}

async function confirmDialog(msg){
 return await showModal({type:'confirm',message:msg,okText:'确定',cancelText:'取消'});
}

async function promptDialog(msg,def){
 return await showModal({type:'prompt',message:msg,defaultValue:def||'',okText:'确定',cancelText:'取消'});
}

// ===== 媒体文件处理（图片+视频） =====
function readTopoFiles(files,callback){
 const results=[];
 let pending=files.length;
 if(pending===0){callback(results);return;}
 for(let i=0;i<files.length;i++){
  const f=files[i];
  if(f.type.startsWith('video/')){
   // 视频文件：读取为dataURL
   const reader=new FileReader();
   reader.onload=function(e){
    results.push({src:e.target.result,comment:'',type:'video'});
    if(results.length===pending)callback(results);
   };
   reader.readAsDataURL(f);
  }else if(f.type.startsWith('image/')){
   const reader=new FileReader();
   reader.onload=function(e){
    const img=new Image();
    img.onload=function(){
     const canvas=document.createElement('canvas');
     const maxW=800;
     let w=img.width,h=img.height;
     if(w>maxW){h=h*maxW/w;w=maxW;}
     canvas.width=w;canvas.height=h;
     const ctx=canvas.getContext('2d');
     ctx.drawImage(img,0,0,w,h);
     results.push({src:canvas.toDataURL('image/jpeg',0.8),comment:'',type:'image'});
     if(results.length===pending)callback(results);
    };
    img.onerror=function(){pending--;if(results.length===pending)callback(results);};
    img.src=e.target.result;
   };
   reader.readAsDataURL(f);
  }else{
   pending--;if(pending===0)callback(results);
  }
 }
}

// ===== 媒体渲染网格 =====
function renderTopoGrid(containerId,images,prefix){
 const el=document.getElementById(containerId);
 if(!images||images.length===0){el.innerHTML='<span style="font-size:16px;color:#94a3b8">尚未添加媒体文件</span>';return;}
 el.innerHTML=images.map((img,i)=>{
  const isVideo=img.type==='video'||(!img.type&&img.src&&img.src.match(/^data:video\//));
  const mediaHtml=isVideo
   ?`<video src="${esc(img.src)}" controls style="width:100%;border-radius:var(--radius-xs);display:block;max-height:400px;background:#000" preload="metadata"></video>`
   :`<img src="${esc(img.src)}" alt="媒体${i+1}">`;
  return`
  <div class="topo-image-item">
   <div class="img-wrap">
    <span class="img-index">#${i+1}</span>
    <button class="img-del" onclick="removeTopoImage('${prefix}',${i})" title="删除">&times;</button>
    ${mediaHtml}
   </div>
   <textarea class="img-cmt-input" placeholder="为此媒体添加注释说明..." oninput="updateTopoComment('${prefix}',${i},this.value)">${esc(img.comment||'')}</textarea>
  </div>`;
 }).join('');
}

function removeTopoImage(prefix,i){
 if(typeof window.removeTopoImageByPrefix==='function'){window.removeTopoImageByPrefix(prefix,i);}
}
function updateTopoComment(prefix,i,val){
 if(typeof window.updateTopoCommentByPrefix==='function'){window.updateTopoCommentByPrefix(prefix,i,val);}
}

// ===== 粘贴支持 =====
document.addEventListener('paste',function(e){
 const items=e.clipboardData&&e.clipboardData.items;
 if(!items)return;
 const imageFiles=[];
 for(let i=0;i<items.length;i++){
  if(items[i].type.startsWith('image/')){
   const blob=items[i].getAsFile();
   if(blob)imageFiles.push(blob);
  }
 }
 if(imageFiles.length>0&&typeof window.handlePasteTopo==='function'){
  e.preventDefault();
  window.handlePasteTopo(imageFiles);
 }
});

// ===== 拖入支持 =====
document.addEventListener('dragover',function(e){
 const target=e.target.closest('.topo-dropzone');
 if(!target)return;
 e.preventDefault();
 target.classList.add('dragover');
});
document.addEventListener('dragleave',function(e){
 const target=e.target.closest('.topo-dropzone');
 if(!target)return;
 target.classList.remove('dragover');
});
document.addEventListener('drop',function(e){
 const target=e.target.closest('.topo-dropzone');
 if(!target)return;
 e.preventDefault();
 target.classList.remove('dragover');
 const files=e.dataTransfer.files;
 if(!files||files.length===0)return;
 if(typeof window.handleDropTopo==='function'){
  window.handleDropTopo(files);
 }
});

// ===== 获取URL参数 =====
function getUrlParam(name){
 const url=new URL(window.location.href);
 return url.searchParams.get(name);
}

// ===== 搜索历史 =====
function getSearchHistory(key){
 try{return JSON.parse(localStorage.getItem('sh_'+key))||[];}catch(e){return[];}
}
function addSearchHistory(key,text){
 if(!text.trim())return;
 let h=getSearchHistory(key);
 h=h.filter(x=>x!==text);
 h.unshift(text);
 if(h.length>5)h=h.slice(0,5);
 localStorage.setItem('sh_'+key,JSON.stringify(h));
}
function clearSearchHistory(key){
 localStorage.removeItem('sh_'+key);
}
function initSearchHistory(inputId,historyKey,onSelect){
 const input=document.getElementById(inputId);
 if(!input)return;
 const wrapper=input.parentElement;
 if(wrapper.style.position===''||wrapper.style.position==='static')wrapper.style.position='relative';
 const dropdown=document.createElement('div');
 dropdown.className='search-history-dropdown';
 dropdown.style.display='none';
 wrapper.appendChild(dropdown);
 function renderHistory(){
  const h=getSearchHistory(historyKey);
  if(h.length===0){dropdown.style.display='none';return;}
  dropdown.innerHTML='<div class="search-history-header"><span>最近搜索</span><button class="search-history-clear" onclick="clearSearchHistory(\''+historyKey+'\');this.closest(\'.search-history-dropdown\').style.display=\'none\';">清除</button></div>'+h.map(t=>'<div class="search-history-item" data-text="'+esc(t)+'">'+esc(t)+'</div>').join('');
  dropdown.style.display='block';
 }
 input.addEventListener('focus',function(){
  const h=getSearchHistory(historyKey);
  if(h.length>0&&!input.value.trim())renderHistory();
 });
 input.addEventListener('input',function(){
  if(!input.value.trim())renderHistory();
  else dropdown.style.display='none';
 });
 dropdown.addEventListener('click',function(e){
  const item=e.target.closest('.search-history-item');
  if(item){
   input.value=item.dataset.text;
   dropdown.style.display='none';
   if(onSelect)onSelect(item.dataset.text);
   else input.dispatchEvent(new Event('input',{bubbles:true}));
  }
 });
 document.addEventListener('click',function(e){
  if(!wrapper.contains(e.target))dropdown.style.display='none';
 });
 input.addEventListener('keydown',function(e){
  if(e.key==='Enter'&&input.value.trim()){
   addSearchHistory(historyKey,input.value.trim());
   dropdown.style.display='none';
  }
 });
}

// ===== 侧边栏下拉菜单 =====
function initSidebarDropdowns(){
 document.querySelectorAll('.nav-dropdown').forEach(dd=>{
  const toggle=dd.querySelector('.nav-dropdown-toggle');
  const menu=dd.querySelector('.nav-dropdown-menu');
  if(!toggle||!menu)return;
  if(menu.querySelector('.nav-btn.active')){
   dd.classList.add('open');
   toggle.classList.add('active');
  }
  toggle.addEventListener('click',()=>{
   const isOpen=dd.classList.contains('open');
   document.querySelectorAll('.nav-dropdown.open').forEach(other=>{
    if(other!==dd){other.classList.remove('open');other.querySelector('.nav-dropdown-toggle')?.classList.remove('active');}
   });
   dd.classList.toggle('open',!isOpen);
   toggle.classList.toggle('active',!isOpen);
  });
 });
}

// ===== 快捷键 =====
document.addEventListener('keydown',function(e){
 // ESC 返回（仅在非输入状态下）
 if(e.key==='Escape'&&e.target.tagName!=='INPUT'&&e.target.tagName!=='TEXTAREA'&&!e.ctrlKey&&!e.metaKey&&!e.altKey){
  e.preventDefault();
  e.stopPropagation();
  if(document.activeElement) document.activeElement.blur();
  history.back();
 }
 // Ctrl+K 聚焦搜索框
 if((e.ctrlKey||e.metaKey)&&e.key==='k'){
  e.preventDefault();
  const searchInput=document.querySelector('#searchInput');
  if(searchInput){
   searchInput.focus();
   searchInput.select();
   searchInput.style.transition='box-shadow .15s ease';
   searchInput.style.boxShadow='0 0 0 4px rgba(37,99,235,.35)';
   setTimeout(function(){searchInput.style.boxShadow='';},600);
  }
 }
});
