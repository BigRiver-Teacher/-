// 大屏图表枚举值 → 中文标签映射
// 后端图表接口（/api/echart/**）直接返回数据库里的枚举 code（如 FALL、ONLINE），
// 图表展示前统一在这里转成中文，避免坐标轴/图例出现英文原文。

const DEVICE_STATUS_LABELS: Record<string, string> = {
  ONLINE: '在线',
  OFFLINE: '离线',
  FAULT: '故障',
  MAINTENANCE: '维护',
  LOW_BATTERY: '低电量',
  PENDING: '待激活',
}

const ALERT_TYPE_LABELS: Record<string, string> = {
  FALL: '跌倒',
  SIG: '跌倒',
  DEVICE_OFFLINE: '设备离线',
  EMERGENCY: '紧急呼叫',
  ABNORMAL_GAIT: '步态异常',
  FALL_DETECT: '跌倒检测',
  HEART_RATE_ABNORMAL: '心率异常',
  BP_ABNORMAL: '血压异常',
  ACTIVITY_ANORMAL: '活动量异常',
  SOS_CALL: 'SOS紧急呼叫',
  POOR_SLEEP: '睡眠质量差',
}

function lookup(dict: Record<string, string>, code: string): string {
  if (code === null || code === undefined) return ''
  const key = String(code).trim()
  return dict[key] || dict[key.toUpperCase()] || key
}

/** 设备状态 code → 中文（ONLINE → 在线） */
export function deviceStatusLabel(code: string): string {
  return lookup(DEVICE_STATUS_LABELS, code)
}

/** 告警/故障类型 code → 中文（DEVICE_OFFLINE → 设备离线） */
export function alertTypeLabel(code: string): string {
  return lookup(ALERT_TYPE_LABELS, code)
}
