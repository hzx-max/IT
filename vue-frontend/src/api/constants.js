export const VENDOR_MAP = {
  huawei: { n: '华为', c: '#cf1322' }, h3c: { n: '华三', c: '#08979c' },
  cisco: { n: 'Cisco', c: '#1a73e8' }, ruijie: { n: '锐捷', c: '#d4380d' },
  fiberhome: { n: '烽火', c: '#722ed1' }, maipu: { n: '迈普', c: '#13c2c2' },
  zte: { n: '中兴', c: '#1677ff' }
}
export const CAT_MAP = {
  basic: '基础配置', vlan: 'VLAN', routing: '路由', ospf: 'OSPF', bgp: 'BGP',
  acl: 'ACL/NAT', security: '安全', stp: 'STP/冗余', wlan: 'WLAN/无线',
  manage: '管理维护', agg: '链路聚合', mpls: 'MPLS/VPN', dhcp: 'DHCP', qos: 'QoS'
}
export const LINUX_VENDOR_MAP = {
  centos: { n: 'CentOS', c: '#932279' }, ubuntu: { n: 'Ubuntu', c: '#E95420' },
  debian: { n: 'Debian', c: '#A80030' }, redhat: { n: 'RedHat', c: '#EE0000' },
  suse: { n: 'SUSE', c: '#73BA25' }, rocky: { n: 'Rocky', c: '#10B981' },
  alpine: { n: 'Alpine', c: '#0D597F' }, arch: { n: 'Arch', c: '#1793D1' }
}
export const LINUX_CAT_MAP = {
  basic: '基础操作', file: '文件管理', user: '用户权限', network: '网络配置',
  service: '服务管理', disk: '磁盘管理', package: '软件包', process: '进程管理',
  firewall: '防火墙', shell: 'Shell脚本', cron: '定时任务', backup: '备份恢复',
  monitor: '监控日志', security: '安全加固'
}
export const OFFICE_VENDOR_MAP = {
  word: { n: 'Word', c: '#2B579A' }, excel: { n: 'Excel', c: '#217346' },
  ppt: { n: 'PowerPoint', c: '#B7472A' }, outlook: { n: 'Outlook', c: '#0078D4' },
  wps_word: { n: 'WPS文字', c: '#D4380D' }, wps_excel: { n: 'WPS表格', c: '#08979C' },
  wps_ppt: { n: 'WPS演示', c: '#722ED1' }, libre: { n: 'LibreOffice', c: '#18A303' }
}
export const OFFICE_CAT_MAP = {
  basic: '基础操作', format: '格式排版', formula: '公式函数', chart: '图表',
  data: '数据处理', mail: '邮件管理', macro: '宏/VBA', template: '模板',
  print: '打印', share: '协作共享', security: '安全', shortcut: '快捷键',
  style: '样式主题', insert: '插入对象'
}

export function getVendorName(k, map) { return (map || VENDOR_MAP)[k]?.n || k }
export function getVendorColor(k, map) { return (map || VENDOR_MAP)[k]?.c || '#666' }
export function getCatLabel(k, map) { return (map || CAT_MAP)[k] || k }

export function formatTime(ts) {
  if (!ts) return ''
  if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/.test(ts)) return ts
  try {
    const d = new Date(ts)
    if (isNaN(d.getTime())) return ts
    const pad = n => String(n).padStart(2, '0')
    return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
  } catch { return ts }
}
