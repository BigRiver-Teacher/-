// 菜单路由映射
export const menuRouteMap = {
  1: '/home/management', //管理主页
  2: '/home/warning', //跌倒预警
  '3-1': '/home/elder/base', //老人详情-基础档案
  '3-2': '/home/elder/gait', //老人详情-步态历史
  '3-3': '/home/elder/fall-record', //老人详情-既往跌倒记录
  '3-4': '/home/elder/health-note', //老人详情-健康备注
  '4-1': '/home/visualization/pose', //数据可视化-姿态时序曲线
  '4-2': '/home/visualization/risk', //数据可视化-月度风险统计
  '4-3': '/home/visualization/heatmap', //数据可视化-活动轨迹热力图
  '5-1': '/home/monitor/sensor', //监视数据可视化-多传感器对照
  '5-2': '/home/monitor/report', //监视数据可视化-周期导出报表
  '5-3': '/home/monitor/environment', //监视数据可视化-环境风险评估
  6: '/home/device', //传感设备管理
  7: '/home/expert', //专家康养咨询
  8: '/home/caregiver', //陪护人员管理
  9: '/home/profile', //个人主页
  '10-1': '/home/alert/list', //告警消息中心-实时告警列表
  '10-2': '/home/alert/push-log', //告警消息中心-家属推送日志
  '10-3': '/home/alert/community', //告警消息中心-社区处理旧账
  '10-4': '/home/alert/preference', //告警消息中心-通知偏好设置
  11: '/home/space', //居家空间档案
  12: '/home/intervention', //干预方案管理
  13: '/home/schedule', //日常康养日程
  '14-1': '/home/system/permission', //系统管理-账号权限
  '14-2': '/home/system/log', //系统管理-操作日志
  '14-3': '/home/system/backup', //系统管理-数据备份
  '14-4': '/home/system/model', //系统管理-模型参数配置
  '15-1': '/home/custom/sidebar', //组件&功能-侧边栏自定义
  '15-2': '/home/custom/dashboard', //组件&功能-首页看板布局
  '15-3': '/home/custom/mobile', //移动端导航设置
}
