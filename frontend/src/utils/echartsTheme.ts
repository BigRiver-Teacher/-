/**
 * 数据大屏图表统一视觉规范
 * 深色底 + 青蓝主色，统一坐标轴/提示框/图例/渐变，避免四个大屏各写一套配色
 */
import * as echarts from 'echarts'

/** 主色序列 */
export const BS_COLORS = [
  '#38e1ff',
  '#4f8cff',
  '#8b5cf6',
  '#22d3a7',
  '#f5a524',
  '#ff5c7c',
  '#7dd3fc',
]

/** 语义色 */
export const BS_SEMANTIC = {
  online: '#22d3a7',
  offline: '#f5a524',
  fault: '#ff5c7c',
  maintain: '#8b5cf6',
  cyan: '#38e1ff',
  blue: '#4f8cff',
  violet: '#8b5cf6',
  mint: '#22d3a7',
  amber: '#f5a524',
  rose: '#ff5c7c',
}

/** #rrggbb → rgba() */
export function hexA(hex: string, alpha: number): string {
  const h = hex.replace('#', '')
  const full = h.length === 3 ? h.split('').map((c) => c + c).join('') : h
  const r = parseInt(full.slice(0, 2), 16)
  const g = parseInt(full.slice(2, 4), 16)
  const b = parseInt(full.slice(4, 6), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

/** 纵向渐变（用于柱状图柱体） */
export function vGradient(from: string, to: string) {
  return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    { offset: 0, color: from },
    { offset: 1, color: to },
  ])
}

/** 横向渐变（用于条形图） */
export function hGradient(from: string, to: string) {
  return new echarts.graphic.LinearGradient(0, 0, 1, 0, [
    { offset: 0, color: from },
    { offset: 1, color: to },
  ])
}

/** 面积渐变（用于折线图 areaStyle） */
export function areaGradient(color: string, topAlpha = 0.4, bottomAlpha = 0.01) {
  return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    { offset: 0, color: hexA(color, topAlpha) },
    { offset: 1, color: hexA(color, bottomAlpha) },
  ])
}

/** 统一提示框 */
export function bsTooltip(extra: Record<string, unknown> = {}) {
  return {
    backgroundColor: 'rgba(8, 18, 34, 0.94)',
    borderColor: 'rgba(56, 225, 255, 0.35)',
    borderWidth: 1,
    padding: [9, 12],
    textStyle: { color: '#dcecff', fontSize: 12 },
    extraCssText:
      'border-radius:10px;box-shadow:0 10px 30px rgba(0,0,0,0.5);backdrop-filter:blur(6px);',
    ...extra,
  }
}

/** 统一坐标轴样式（axisLabel / line / splitLine） */
export const bsAxis = {
  axisLine: { lineStyle: { color: 'rgba(120, 170, 230, 0.22)' } },
  axisTick: { show: false },
  axisLabel: { color: '#8ba8ca', fontSize: 11, margin: 10 },
  splitLine: { lineStyle: { color: 'rgba(120, 170, 230, 0.1)', type: 'dashed' as const } },
}

/** 统一网格边距 */
export const bsGrid = { top: 26, left: 6, right: 14, bottom: 4, containLabel: true }

/** 统一图例 */
export function bsLegend(extra: Record<string, unknown> = {}) {
  return {
    top: 0,
    right: 0,
    icon: 'roundRect',
    itemWidth: 8,
    itemHeight: 8,
    itemGap: 12,
    textStyle: { color: '#9db8d6', fontSize: 11 },
    ...extra,
  }
}

/** 图表通用动画配置 */
export const bsAnimation = {
  animationDuration: 900,
  animationEasing: 'cubicOut' as const,
  animationDurationUpdate: 600,
}

/** 饼图/环图通用配置 */
export function bsPieStyle(color: string) {
  return {
    itemStyle: {
      color,
      borderColor: 'rgba(6, 14, 28, 0.9)',
      borderWidth: 2,
      borderRadius: 4,
    },
  }
}

/** 数据为空时在图表中间显示占位文案 */
export function bsEmpty(dataLength: number, text = '暂无数据') {
  if (dataLength > 0) return {}
  return {
    graphic: {
      type: 'text',
      left: 'center',
      top: 'middle',
      silent: true,
      style: {
        text,
        fill: 'rgba(140, 170, 200, 0.45)',
        fontSize: 12,
        letterSpacing: 2,
      },
    },
  }
}
