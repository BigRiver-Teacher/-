<template>
  <div class="preference-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Setting /></el-icon>
        通知偏好设置
      </h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleTestPush">
          <el-icon><Message /></el-icon>
          测试推送
        </el-button>
        <el-button type="danger" plain @click="handleResetDefault">
          <el-icon><Refresh /></el-icon>
          恢复默认
        </el-button>
        <el-button type="primary" plain @click="saveAllSettings">
          <el-icon><Check /></el-icon>
          保存设置
        </el-button>
      </div>
    </div>

    <!-- ===== 1. 推送渠道设置 ===== -->
    <div class="setting-section">
      <div class="section-header">
        <span class="section-title"
          ><el-icon><Iphone /></el-icon>推送渠道</span
        >
        <span class="section-desc">配置各推送渠道的开启状态</span>
      </div>
      <div class="channel-grid">
        <div
          v-for="channel in channelSettings"
          :key="channel.key"
          class="channel-item"
          :class="{ active: channel.enabled }"
        >
          <div class="channel-icon" :style="{ background: channel.color }">
            <el-icon><component :is="channel.icon" /></el-icon>
          </div>
          <div class="channel-info">
            <div class="channel-name">{{ channel.name }}</div>
            <div class="channel-desc">{{ channel.desc }}</div>
          </div>
          <el-switch v-model="channel.enabled" size="large" />
        </div>
      </div>
    </div>

    <!-- ===== 2. 告警级别过滤 ===== -->
    <div class="setting-section">
      <div class="section-header">
        <span class="section-title"
          ><el-icon><Filter /></el-icon>告警级别过滤</span
        >
        <span class="section-desc">按告警级别配置推送渠道</span>
      </div>
      <div class="level-grid">
        <div v-for="level in levelSettings" :key="level.key" class="level-item">
          <div class="level-header">
            <el-tag :type="level.tagType" size="large" effect="dark">
              {{ level.label }}
            </el-tag>
            <span class="level-desc">{{ level.desc }}</span>
          </div>
          <div class="level-channels">
            <el-checkbox-group v-model="level.channels">
              <el-checkbox
                v-for="ch in availableChannels"
                :key="ch.key"
                :label="ch.key"
                :disabled="level.key === 'normal' && ch.key !== 'app'"
              >
                <el-icon><component :is="ch.icon" /></el-icon>
                {{ ch.name }}
              </el-checkbox>
            </el-checkbox-group>
            <span v-if="level.key === 'normal'" class="level-hint"> ⚠️ 普通告警仅支持APP推送 </span>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 3. 推送场景配置 ===== -->
    <div class="setting-section">
      <div class="section-header">
        <span class="section-title"
          ><el-icon><List /></el-icon>推送场景配置</span
        >
        <span class="section-desc">按场景单独配置推送开关、渠道和延迟时间</span>
      </div>
      <div class="scene-table-wrapper">
        <el-table :data="sceneSettings" border style="width: 100%">
          <el-table-column prop="scene" label="推送场景" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getSceneTagType(row.scene)" size="small">
                {{ row.scene }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="推送开关" width="90" align="center">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="推送渠道" min-width="280">
            <template #default="{ row }">
              <el-checkbox-group v-model="row.channels" :disabled="!row.enabled">
                <el-checkbox v-for="ch in availableChannels" :key="ch.key" :label="ch.key">
                  <el-icon><component :is="ch.icon" /></el-icon>
                  {{ ch.name }}
                </el-checkbox>
              </el-checkbox-group>
              <span v-if="!row.enabled" style="color: #ccc; font-size: 13px"> 已关闭推送 </span>
            </template>
          </el-table-column>
          <el-table-column label="延迟时间" width="140" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.delayMinutes"
                :min="0"
                :max="120"
                :disabled="!row.enabled"
                size="small"
                controls-position="right"
                style="width: 100px"
              />
              <span style="color: #999; font-size: 13px; margin-left: 4px">分钟</span>
            </template>
          </el-table-column>
          <el-table-column label="静音时段" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.silentMode ? 'warning' : 'success'" size="small">
                {{ row.silentMode ? '延迟推送' : '即时推送' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- ===== 4. 静音时段 ===== -->
    <div class="setting-section">
      <div class="section-header">
        <span class="section-title"
          ><el-icon><Timer /></el-icon>静音时段</span
        >
        <span class="section-desc">设置静音时段，仅静音声音但保留通知横幅</span>
      </div>
      <div class="silent-section">
        <div v-for="(item, index) in silentPeriods" :key="index" class="silent-item">
          <div class="silent-time">
            <el-time-picker
              v-model="item.start"
              format="HH:mm"
              value-format="HH:mm"
              placeholder="开始"
              size="small"
              style="width: 110px"
            />
            <span style="color: #999">至</span>
            <el-time-picker
              v-model="item.end"
              format="HH:mm"
              value-format="HH:mm"
              placeholder="结束"
              size="small"
              style="width: 110px"
            />
          </div>
          <div class="silent-label">
            <el-input
              v-model="item.label"
              placeholder="时段名称"
              size="small"
              style="width: 140px"
            />
          </div>
          <div class="silent-desc">
            <el-tag size="small" type="info"> {{ item.start }} - {{ item.end }} 仅静音声音 </el-tag>
          </div>
          <el-button type="danger" size="small" link @click="removeSilentPeriod(index)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
        <el-button type="primary" plain size="small" @click="addSilentPeriod">
          <el-icon><Plus /></el-icon>
          添加时段
        </el-button>
      </div>
    </div>

    <!-- ===== 5. 推送模板管理 ===== -->
    <div class="setting-section">
      <div class="section-header">
        <span class="section-title"
          ><el-icon><Document /></el-icon>推送模板管理</span
        >
        <span class="section-desc">编辑各场景的推送模板内容，支持变量替换</span>
      </div>
      <div class="template-table-wrapper">
        <el-table :data="templateSettings" border style="width: 100%">
          <el-table-column prop="scene" label="场景" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getSceneTagType(row.scene)" size="small">
                {{ row.scene }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="template" label="模板内容" min-width="300">
            <template #default="{ row }">
              <div class="template-preview">{{ row.template }}</div>
            </template>
          </el-table-column>
          <el-table-column label="变量" width="160" align="center">
            <template #default="{ row }">
              <el-popover placement="top" title="支持的变量" :width="280" trigger="hover">
                <div class="variable-list">
                  <div v-for="v in variables" :key="v.key" class="variable-item">
                    <code class="var-code">{{ v.key }}</code>
                    <span class="var-desc">{{ v.desc }}</span>
                  </div>
                </div>
                <template #reference>
                  <el-button size="small" type="info" plain>
                    <el-icon><InfoFilled /></el-icon>
                    查看变量
                  </el-button>
                </template>
              </el-popover>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center">
            <template #default="{ row }">
              <el-button size="small" type="warning" link @click="editTemplate(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button size="small" type="info" link @click="resetTemplate(row)">
                <el-icon><Refresh /></el-icon>
                恢复默认
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- ===== 6. 接收人管理 ===== -->
    <div class="setting-section">
      <div class="section-header">
        <span class="section-title"
          ><el-icon><User /></el-icon>接收人管理</span
        >
        <span class="section-desc">每个老人可设置社区主管联系人和家属联系人</span>
      </div>
      <div class="receiver-section">
        <div class="receiver-actions">
          <el-button type="primary" plain size="small" @click="handleBatchImport">
            <el-icon><Upload /></el-icon>
            批量导入
          </el-button>
          <el-button type="success" plain size="small" @click="handleExportReceivers">
            <el-icon><Download /></el-icon>
            导出联系人
          </el-button>
        </div>
        <el-table :data="receiverList" border style="width: 100%">
          <el-table-column prop="elderName" label="老人姓名" width="120" align="center" />
          <el-table-column label="社区主管" width="200" align="center">
            <template #default="{ row }">
              <div class="contact-cell">
                <span class="contact-name">{{ row.supervisorName || '未设置' }}</span>
                <span v-if="row.supervisorPhone" class="contact-phone">
                  {{ row.supervisorPhone }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="家属联系人" width="200" align="center">
            <template #default="{ row }">
              <div class="contact-cell">
                <span class="contact-name">{{ row.familyName || '未设置' }}</span>
                <span v-if="row.familyPhone" class="contact-phone">
                  {{ row.familyPhone }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="备用联系人" width="200" align="center">
            <template #default="{ row }">
              <div class="contact-cell">
                <span class="contact-name">{{ row.backupName || '未设置' }}</span>
                <span v-if="row.backupPhone" class="contact-phone">
                  {{ row.backupPhone }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center">
            <template #default="{ row }">
              <el-button size="small" type="warning" link @click="editReceiver(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button size="small" type="primary" link @click="testReceiver(row)">
                <el-icon><Message /></el-icon>
                测试
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- ===== 测试推送弹框 ===== -->
    <el-dialog
      v-model="testDialogVisible"
      title="测试推送"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px">
        <el-form-item label="选择老人">
          <el-select v-model="testForm.elder" placeholder="请选择老人" style="width: 100%">
            <el-option
              v-for="item in elderOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="推送渠道">
          <el-select v-model="testForm.channel" placeholder="请选择渠道" style="width: 100%">
            <el-option
              v-for="item in availableChannels"
              :key="item.key"
              :label="item.name"
              :value="item.key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="测试内容">
          <el-input
            v-model="testForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入测试内容..."
          />
        </el-form-item>
      </el-form>
      <div v-if="testResult" class="test-result" :class="testResult.success ? 'success' : 'failed'">
        <el-icon v-if="testResult.success"><CircleCheck /></el-icon>
        <el-icon v-else><CircleClose /></el-icon>
        <span>{{ testResult.message }}</span>
        <span v-if="testResult.duration" class="test-duration"
          >耗时：{{ testResult.duration }}</span
        >
      </div>
      <template #footer>
        <el-button @click="testDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="sendTestPush">发送测试</el-button>
      </template>
    </el-dialog>

    <!-- ===== 编辑模板弹框 ===== -->
    <el-dialog
      v-model="templateDialogVisible"
      title="编辑推送模板"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px">
        <el-form-item label="场景">
          <el-tag :type="getSceneTagType(templateForm.scene)" size="small">
            {{ templateForm.scene }}
          </el-tag>
        </el-form-item>
        <el-form-item label="模板内容" required>
          <el-input
            v-model="templateForm.template"
            type="textarea"
            :rows="5"
            placeholder="请输入模板内容，支持变量替换..."
          />
        </el-form-item>
        <el-form-item label="变量说明">
          <div class="variable-hint">
            <span v-for="v in variables" :key="v.key" class="hint-tag">
              <code>{{ v.key }}</code> - {{ v.desc }}
            </span>
          </div>
        </el-form-item>
        <el-form-item label="预览效果">
          <div class="preview-box">
            {{ previewTemplate }}
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存模板</el-button>
      </template>
    </el-dialog>

    <!-- ===== 编辑接收人弹框 ===== -->
    <el-dialog
      v-model="receiverDialogVisible"
      title="编辑联系人"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form label-width="110px">
        <el-form-item label="老人姓名">
          <span style="font-weight: 600">{{ receiverForm.elderName }}</span>
        </el-form-item>
        <el-divider />
        <el-form-item label="社区主管姓名">
          <el-input v-model="receiverForm.supervisorName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="社区主管电话">
          <el-input v-model="receiverForm.supervisorPhone" placeholder="请输入电话" />
        </el-form-item>
        <el-divider />
        <el-form-item label="家属姓名">
          <el-input v-model="receiverForm.familyName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="家属电话">
          <el-input v-model="receiverForm.familyPhone" placeholder="请输入电话" />
        </el-form-item>
        <el-divider />
        <el-form-item label="备用联系人">
          <el-input v-model="receiverForm.backupName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="备用电话">
          <el-input v-model="receiverForm.backupPhone" placeholder="请输入电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="receiverDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveReceiver">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting,
  Iphone,
  ChatDotSquare,
  Message,
  Phone,
  Monitor,
  Filter,
  List,
  Timer,
  Document,
  User,
  Refresh,
  Check,
  Delete,
  Plus,
  Edit,
  Upload,
  Download,
  InfoFilled,
  CircleCheck,
  CircleClose,
  Warning as WarningIcon,
} from '@element-plus/icons-vue'
import { getPreferenceConfig, savePreferenceConfig, testPush } from '@/api/preference'

// ===== 渠道配置 =====
const availableChannels = [
  { key: 'app', name: 'APP推送', icon: 'Iphone' },
  { key: 'wechat', name: '微信推送', icon: 'ChatDotSquare' },
  { key: 'sms', name: '短信推送', icon: 'Message' },
  { key: 'phone', name: '电话语音', icon: 'Phone' },
  { key: 'email', name: '邮件推送', icon: 'Monitor' },
]

const channelSettings = ref([
  {
    key: 'app',
    name: 'APP推送',
    desc: '通过手机APP推送通知',
    icon: 'Iphone',
    color: '#409eff',
    enabled: true,
  },
  {
    key: 'wechat',
    name: '微信推送',
    desc: '通过微信公众号/小程序推送',
    icon: 'ChatDotSquare',
    color: '#67c23a',
    enabled: true,
  },
  {
    key: 'sms',
    name: '短信推送',
    desc: '通过短信发送通知',
    icon: 'Message',
    color: '#e6a23c',
    enabled: false,
  },
  {
    key: 'phone',
    name: '电话语音',
    desc: '通过电话语音播报通知',
    icon: 'Phone',
    color: '#f56c6c',
    enabled: false,
  },
  {
    key: 'email',
    name: '邮件推送',
    desc: '通过邮件发送通知',
    icon: 'Monitor',
    color: '#909399',
    enabled: false,
  },
])

// ===== 告警级别过滤 =====
const levelSettings = ref([
  {
    key: 'urgent',
    label: '紧急告警',
    desc: '全部渠道推送',
    tagType: 'danger',
    channels: ['app', 'wechat', 'sms', 'phone', 'email'],
  },
  {
    key: 'important',
    label: '重要告警',
    desc: '指定渠道推送',
    tagType: 'warning',
    channels: ['app', 'wechat', 'sms'],
  },
  { key: 'normal', label: '普通告警', desc: '仅APP推送', tagType: 'info', channels: ['app'] },
])

// ===== 推送场景配置 =====
const sceneSettings = ref([
  {
    scene: '跌倒检测',
    enabled: true,
    channels: ['app', 'wechat', 'sms'],
    delayMinutes: 0,
    silentMode: false,
    key: 'fall',
  },
  {
    scene: 'SOS紧急呼叫',
    enabled: true,
    channels: ['app', 'wechat', 'sms', 'phone'],
    delayMinutes: 0,
    silentMode: false,
    key: 'sos',
  },
  {
    scene: '心率异常',
    enabled: true,
    channels: ['app', 'wechat'],
    delayMinutes: 3,
    silentMode: false,
    key: 'heart',
  },
  {
    scene: '血压异常',
    enabled: true,
    channels: ['app', 'wechat'],
    delayMinutes: 5,
    silentMode: false,
    key: 'blood',
  },
  {
    scene: '设备离线',
    enabled: true,
    channels: ['app'],
    delayMinutes: 10,
    silentMode: true,
    key: 'offline',
  },
  {
    scene: '活动量异常',
    enabled: true,
    channels: ['app'],
    delayMinutes: 15,
    silentMode: true,
    key: 'activity',
  },
  {
    scene: '睡眠质量差',
    enabled: true,
    channels: ['app'],
    delayMinutes: 30,
    silentMode: true,
    key: 'sleep',
  },
  {
    scene: '定期健康报告',
    enabled: true,
    channels: ['email'],
    delayMinutes: 60,
    silentMode: true,
    key: 'report',
  },
])

// ===== 静音时段 =====
const silentPeriods = ref([
  { start: '12:00', end: '14:00', label: '午休时段' },
  { start: '22:00', end: '06:00', label: '夜间时段' },
])

// ===== 推送模板 =====
const defaultTemplates = {
  跌倒检测: '【朝夕护行】{老人姓名}于{时间}在{位置}发生跌倒，请立即确认。建议措施：{建议措施}',
  SOS紧急呼叫: '【朝夕护行】紧急！{老人姓名}于{时间}在{位置}按下SOS紧急按钮，请立即响应！',
  心率异常:
    '【朝夕护行】{老人姓名}心率持续偏高，当前值{心率值}，已超出正常范围。建议措施：{建议措施}',
  血压异常: '【朝夕护行】{老人姓名}血压偏高，收缩压{收缩压}，舒张压{舒张压}。建议措施：{建议措施}',
  设备离线: '【朝夕护行】{老人姓名}的智能设备已离线超过{离线时长}，请检查设备状态。',
  活动量异常: '【朝夕护行】{老人姓名}过去{时间范围}活动量极低，建议措施：{建议措施}',
  睡眠质量差:
    '【朝夕护行】{老人姓名}夜间睡眠质量较差，深睡时长不足{深睡时长}。建议措施：{建议措施}',
  定期健康报告: '【朝夕护行】{老人姓名}的{报告周期}健康报告已生成，请登录系统查看完整数据。',
}

const templateSettings = ref([
  { scene: '跌倒检测', template: defaultTemplates['跌倒检测'] },
  { scene: 'SOS紧急呼叫', template: defaultTemplates['SOS紧急呼叫'] },
  { scene: '心率异常', template: defaultTemplates['心率异常'] },
  { scene: '血压异常', template: defaultTemplates['血压异常'] },
  { scene: '设备离线', template: defaultTemplates['设备离线'] },
  { scene: '活动量异常', template: defaultTemplates['活动量异常'] },
  { scene: '睡眠质量差', template: defaultTemplates['睡眠质量差'] },
  { scene: '定期健康报告', template: defaultTemplates['定期健康报告'] },
])

// ===== 变量列表 =====
const variables = [
  { key: '{老人姓名}', desc: '老人姓名' },
  { key: '{时间}', desc: '告警发生时间' },
  { key: '{位置}', desc: '告警发生位置' },
  { key: '{告警类型}', desc: '告警类型名称' },
  { key: '{告警描述}', desc: '告警详细描述' },
  { key: '{建议措施}', desc: '系统推荐处理措施' },
  { key: '{家属电话}', desc: '家属联系电话' },
  { key: '{心率值}', desc: '心率数值（心率异常场景）' },
  { key: '{收缩压}', desc: '收缩压数值（血压异常场景）' },
  { key: '{舒张压}', desc: '舒张压数值（血压异常场景）' },
  { key: '{离线时长}', desc: '设备离线时长（设备离线场景）' },
  { key: '{时间范围}', desc: '活动量统计时间范围' },
  { key: '{深睡时长}', desc: '深睡时长（睡眠质量差场景）' },
  { key: '{报告周期}', desc: '健康报告周期（定期报告场景）' },
]

// ===== 接收人管理 =====
const receiverList = ref([
  {
    elderName: '张桂花',
    supervisorName: '王主管',
    supervisorPhone: '138****0001',
    familyName: '张先生',
    familyPhone: '138****1234',
    backupName: '张女士',
    backupPhone: '139****5678',
  },
  {
    elderName: '李建国',
    supervisorName: '王主管',
    supervisorPhone: '138****0001',
    familyName: '李女士',
    familyPhone: '139****5678',
    backupName: '',
    backupPhone: '',
  },
  {
    elderName: '王秀英',
    supervisorName: '王主管',
    supervisorPhone: '138****0001',
    familyName: '王先生',
    familyPhone: '137****9012',
    backupName: '王女士',
    backupPhone: '136****3456',
  },
  {
    elderName: '刘德明',
    supervisorName: '王主管',
    supervisorPhone: '138****0001',
    familyName: '刘女士',
    familyPhone: '136****3456',
    backupName: '',
    backupPhone: '',
  },
  {
    elderName: '陈素芬',
    supervisorName: '王主管',
    supervisorPhone: '138****0001',
    familyName: '陈先生',
    familyPhone: '135****7890',
    backupName: '陈女士',
    backupPhone: '134****5678',
  },
])

const elderOptions = computed(() => {
  return receiverList.value.map((item) => ({
    label: item.elderName,
    value: item.elderName,
  }))
})

// ===== 测试推送 =====
const testDialogVisible = ref(false)
const testForm = reactive({
  elder: '',
  channel: 'app',
  content: '【朝夕护行】这是一条测试推送通知，用于验证通知配置是否正常。',
})
const testResult = ref(null)

// ===== 模板编辑 =====
const templateDialogVisible = ref(false)
const templateForm = reactive({
  scene: '',
  template: '',
  index: -1,
})

// ===== 接收人编辑 =====
const receiverDialogVisible = ref(false)
const receiverForm = reactive({
  elderName: '',
  supervisorName: '',
  supervisorPhone: '',
  familyName: '',
  familyPhone: '',
  backupName: '',
  backupPhone: '',
  index: -1,
})

// ===== 工具函数 =====
function getSceneTagType(scene) {
  const map = {
    跌倒检测: 'danger',
    SOS紧急呼叫: 'danger',
    心率异常: 'warning',
    血压异常: 'warning',
    设备离线: 'info',
    活动量异常: 'info',
    睡眠质量差: 'info',
    定期健康报告: 'success',
  }
  return map[scene] || 'info'
}

// ===== 静音时段操作 =====
function addSilentPeriod() {
  silentPeriods.value.push({ start: '08:00', end: '18:00', label: '新时段' })
}

function removeSilentPeriod(index) {
  if (silentPeriods.value.length <= 1) {
    ElMessage.warning('至少保留一个静音时段')
    return
  }
  silentPeriods.value.splice(index, 1)
}

// ===== 模板操作 =====
function editTemplate(row) {
  const index = templateSettings.value.findIndex((item) => item.scene === row.scene)
  templateForm.scene = row.scene
  templateForm.template = row.template
  templateForm.index = index
  templateDialogVisible.value = true
}

function resetTemplate(row) {
  ElMessageBox.confirm(`确定要恢复 "${row.scene}" 的默认模板吗？`, '恢复默认确认', {
    confirmButtonText: '确定恢复',
    cancelButtonText: '取消',
    type: 'info',
  })
    .then(() => {
      const index = templateSettings.value.findIndex((item) => item.scene === row.scene)
      if (index > -1) {
        templateSettings.value[index].template = defaultTemplates[row.scene] || ''
        ElMessage.success('已恢复默认模板')
      }
    })
    .catch(() => {})
}

function saveTemplate() {
  if (!templateForm.template) {
    ElMessage.warning('请输入模板内容')
    return
  }
  if (templateForm.index > -1) {
    templateSettings.value[templateForm.index].template = templateForm.template
    ElMessage.success('模板保存成功')
    templateDialogVisible.value = false
  }
}

// ===== 预览模板 =====
const previewTemplate = computed(() => {
  if (!templateForm.template) return '请输入模板内容以预览'
  let preview = templateForm.template
  const demoData = {
    '{老人姓名}': '张桂花',
    '{时间}': '2024-07-15 14:30:25',
    '{位置}': '3栋502室-卫生间',
    '{告警类型}': '跌倒检测',
    '{告警描述}': '检测到老人在卫生间发生跌倒',
    '{建议措施}': '立即联系家属并安排人员上门查看',
    '{家属电话}': '138****1234',
    '{心率值}': '125',
    '{收缩压}': '165',
    '{舒张压}': '95',
    '{离线时长}': '2小时',
    '{时间范围}': '过去6小时',
    '{深睡时长}': '1.5小时',
    '{报告周期}': '7月第3周',
  }
  Object.entries(demoData).forEach(([key, value]) => {
    preview = preview.replaceAll(key, value)
  })
  return preview
})

// ===== 接收人操作 =====
function editReceiver(row) {
  const index = receiverList.value.findIndex((item) => item.elderName === row.elderName)
  Object.assign(receiverForm, row, { index })
  receiverDialogVisible.value = true
}

function saveReceiver() {
  if (!receiverForm.supervisorName) {
    ElMessage.warning('请输入社区主管姓名')
    return
  }
  if (!receiverForm.supervisorPhone) {
    ElMessage.warning('请输入社区主管电话')
    return
  }
  if (!receiverForm.familyName) {
    ElMessage.warning('请输入家属姓名')
    return
  }
  if (!receiverForm.familyPhone) {
    ElMessage.warning('请输入家属电话')
    return
  }

  if (receiverForm.index > -1) {
    receiverList.value[receiverForm.index] = {
      elderName: receiverForm.elderName,
      supervisorName: receiverForm.supervisorName,
      supervisorPhone: receiverForm.supervisorPhone,
      familyName: receiverForm.familyName,
      familyPhone: receiverForm.familyPhone,
      backupName: receiverForm.backupName || '',
      backupPhone: receiverForm.backupPhone || '',
    }
    ElMessage.success('联系人更新成功')
    receiverDialogVisible.value = false
  }
}

function testReceiver(row) {
  testForm.elder = row.elderName
  testForm.channel = 'app'
  testResult.value = null
  testDialogVisible.value = true
}

// ===== 测试推送 =====
async function sendTestPush() {
  if (!testForm.elder) {
    ElMessage.warning('请选择老人')
    return
  }
  if (!testForm.channel) {
    ElMessage.warning('请选择推送渠道')
    return
  }
  if (!testForm.content) {
    ElMessage.warning('请输入测试内容')
    return
  }

  testResult.value = null

  try {
    await testPush({
      elderName: testForm.elder,
      channel: testForm.channel,
      content: testForm.content,
    })
    testResult.value = {
      success: true,
      message: '测试推送发送成功',
      duration: '',
    }
    ElMessage.success('测试推送发送成功')
  } catch {
    testResult.value = {
      success: false,
      message: '测试推送发送失败',
      duration: '',
    }
    ElMessage.error('测试推送发送失败')
  }
}

// ===== 批量导入 =====
function handleBatchImport() {
  ElMessage.info('请选择要导入的联系人文件（Excel/CSV格式）')
}

function handleExportReceivers() {
  ElMessage.success('联系人导出成功')
}

// ===== 恢复默认 =====
function handleResetDefault() {
  ElMessageBox.confirm(
    '确定要恢复所有设置为默认值吗？此操作将覆盖所有自定义配置！',
    '恢复默认确认',
    {
      confirmButtonText: '确定恢复',
      cancelButtonText: '取消',
      type: 'warning',
    },
  )
    .then(() => {
      // 恢复渠道设置
      channelSettings.value.forEach((item) => {
        const defaults = {
          app: true,
          wechat: true,
          sms: false,
          phone: false,
          email: false,
        }
        item.enabled = defaults[item.key] || false
      })

      // 恢复级别设置
      levelSettings.value.forEach((item) => {
        const defaults = {
          urgent: ['app', 'wechat', 'sms', 'phone', 'email'],
          important: ['app', 'wechat', 'sms'],
          normal: ['app'],
        }
        item.channels = defaults[item.key] || ['app']
      })

      // 恢复场景设置
      sceneSettings.value.forEach((item) => {
        const defaults = {
          fall: { channels: ['app', 'wechat', 'sms'], delay: 0, silent: false },
          sos: { channels: ['app', 'wechat', 'sms', 'phone'], delay: 0, silent: false },
          heart: { channels: ['app', 'wechat'], delay: 3, silent: false },
          blood: { channels: ['app', 'wechat'], delay: 5, silent: false },
          offline: { channels: ['app'], delay: 10, silent: true },
          activity: { channels: ['app'], delay: 15, silent: true },
          sleep: { channels: ['app'], delay: 30, silent: true },
          report: { channels: ['email'], delay: 60, silent: true },
        }
        const def = defaults[item.key]
        if (def) {
          item.channels = def.channels
          item.delayMinutes = def.delay
          item.silentMode = def.silent
          item.enabled = true
        }
      })

      // 恢复模板
      templateSettings.value.forEach((item) => {
        item.template = defaultTemplates[item.scene] || ''
      })

      // 恢复静音时段
      silentPeriods.value = [
        { start: '12:00', end: '14:00', label: '午休时段' },
        { start: '22:00', end: '06:00', label: '夜间时段' },
      ]

      ElMessage.success('已恢复所有默认设置')
    })
    .catch(() => {})
}

// ===== 渠道/级别展示元数据（用于补齐图标、颜色、描述等展示字段） =====
const channelMeta: Record<string, any> = {
  app: { name: 'APP推送', desc: '通过手机APP推送通知', icon: 'Iphone', color: '#409eff' },
  wechat: { name: '微信推送', desc: '通过微信公众号/小程序推送', icon: 'ChatDotSquare', color: '#67c23a' },
  sms: { name: '短信推送', desc: '通过短信发送通知', icon: 'Message', color: '#e6a23c' },
  phone: { name: '电话语音', desc: '通过电话语音播报通知', icon: 'Phone', color: '#f56c6c' },
  email: { name: '邮件推送', desc: '通过邮件发送通知', icon: 'Monitor', color: '#909399' },
}
const levelMeta: Record<string, any> = {
  urgent: { label: '紧急告警', desc: '全部渠道推送', tagType: 'danger' },
  important: { label: '重要告警', desc: '指定渠道推送', tagType: 'warning' },
  normal: { label: '普通告警', desc: '仅APP推送', tagType: 'info' },
}
const sceneKeyMap: Record<string, string> = {
  跌倒检测: 'fall',
  SOS紧急呼叫: 'sos',
  心率异常: 'heart',
  血压异常: 'blood',
  设备离线: 'offline',
  活动量异常: 'activity',
  睡眠质量差: 'sleep',
  定期健康报告: 'report',
}

// ===== 加载偏好配置（后端 DTO 与页面结构不同，加载时做映射） =====
async function loadConfig() {
  try {
    const res = await getPreferenceConfig()
    const data = res.data.data
    if (!data) return

    // 渠道：后端 {code,name,enable} → 页面 {key,name,desc,icon,color,enabled}
    if (Array.isArray(data.channels) && data.channels.length > 0) {
      channelSettings.value = data.channels.map((c) => ({
        key: c.code,
        name: c.name || channelMeta[c.code]?.name || c.code,
        desc: channelMeta[c.code]?.desc || '',
        icon: channelMeta[c.code]?.icon || 'Iphone',
        color: channelMeta[c.code]?.color || '#409eff',
        enabled: !!c.enable,
      }))
    }

    // 级别过滤：后端 {code,name,channels} → 页面 {key,label,desc,tagType,channels}
    if (Array.isArray(data.levelFilters) && data.levelFilters.length > 0) {
      levelSettings.value = data.levelFilters.map((f) => ({
        key: f.code,
        label: f.name || levelMeta[f.code]?.label || f.code,
        desc: levelMeta[f.code]?.desc || '',
        tagType: levelMeta[f.code]?.tagType || 'info',
        channels: f.channels || [],
      }))
    }

    // 场景配置：后端 {name,enable,channels,delayMin,silentMode} → 页面 {scene,enabled,channels,delayMinutes,silentMode,key}
    if (Array.isArray(data.scenes) && data.scenes.length > 0) {
      sceneSettings.value = data.scenes.map((s) => ({
        scene: s.name,
        enabled: !!s.enable,
        channels: s.channels || [],
        delayMinutes: s.delayMin ?? 0,
        silentMode: !!s.silentMode,
        key: sceneKeyMap[s.name] || s.name,
      }))
    }

    // 静音时段：后端 {start,end,name} → 页面 {start,end,label}
    if (Array.isArray(data.silentPeriods) && data.silentPeriods.length > 0) {
      silentPeriods.value = data.silentPeriods.map((p) => ({
        start: p.start,
        end: p.end,
        label: p.name || '',
      }))
    }

    // 推送模板：后端 {scene,content} → 页面 {scene,template}
    if (Array.isArray(data.templates) && data.templates.length > 0) {
      templateSettings.value = data.templates.map((t) => ({
        scene: t.scene,
        template: t.content || defaultTemplates[t.scene] || '',
      }))
    }

    // 接收人：后端 {enderName,managerName,managerPhone,...} → 页面 {elderName,supervisorName,supervisorPhone,...}
    if (Array.isArray(data.receivers) && data.receivers.length > 0) {
      receiverList.value = data.receivers.map((r) => ({
        elderName: r.elderName ?? r.enderName ?? '',
        supervisorName: r.managerName || '',
        supervisorPhone: r.managerPhone || '',
        familyName: r.familyName || '',
        familyPhone: r.familyPhone || '',
        backupName: r.backupName || '',
        backupPhone: r.backupPhone || '',
      }))
    }
  } catch {
    // 请求失败时由响应拦截器统一提示，页面保留默认值
  }
}

// ===== 保存所有设置（按后端 DTO 结构提交） =====
async function saveAllSettings() {
  try {
    const payload = {
      channels: channelSettings.value.map((c) => ({ code: c.key, name: c.name, enable: c.enabled })),
      levelFilters: levelSettings.value.map((l) => ({ code: l.key, name: l.label, channels: l.channels })),
      receivers: receiverList.value.map((r) => ({
        enderName: r.elderName,
        managerName: r.supervisorName,
        managerPhone: r.supervisorPhone,
        familyName: r.familyName,
        familyPhone: r.familyPhone,
        backupName: r.backupName || '',
        backupPhone: r.backupPhone || '',
      })),
      scenes: sceneSettings.value.map((s) => ({
        name: s.scene,
        enable: s.enabled,
        channels: s.channels,
        delayMin: s.delayMinutes,
        silentMode: s.silentMode,
      })),
      templates: templateSettings.value.map((t) => ({ scene: t.scene, content: t.template })),
      silentPeriods: silentPeriods.value.map((p) => ({ start: p.start, end: p.end, name: p.label })),
    }
    await savePreferenceConfig(payload)
    ElMessage.success('所有设置已保存')
  } catch {
    // 失败提示由拦截器统一处理
  }
}

// ===== 生命周期 =====
onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.preference-page {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100%;
}

/* ===== 页面标题 ===== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.page-title .el-icon {
  font-size: 26px;
  color: #409eff;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* ===== 设置区块 ===== */
.setting-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.section-title .el-icon {
  font-size: 18px;
  color: #409eff;
}

.section-desc {
  font-size: 13px;
  color: #999;
}

/* ===== 推送渠道 ===== */
.channel-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.channel-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #f8f9fa;
  border-radius: 10px;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.channel-item.active {
  border-color: #409eff;
  background: #e6f7ff;
}

.channel-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  flex-shrink: 0;
}

.channel-info {
  flex: 1;
}

.channel-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.channel-desc {
  font-size: 12px;
  color: #999;
}

/* ===== 告警级别过滤 ===== */
.level-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
}

.level-item {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 10px;
}

.level-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.level-desc {
  font-size: 13px;
  color: #999;
}

.level-channels {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.level-hint {
  font-size: 12px;
  color: #e6a23c;
}

/* ===== 推送场景配置 ===== */
.scene-table-wrapper {
  overflow-x: auto;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f5f7fa;
  color: #333;
  font-weight: 600;
}

:deep(.el-table .el-table__cell) {
  padding: 10px 0;
}

/* ===== 静音时段 ===== */
.silent-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.silent-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  flex-wrap: wrap;
}

.silent-time {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ===== 模板管理 ===== */
.template-table-wrapper {
  overflow-x: auto;
}

.template-preview {
  font-size: 13px;
  color: #555;
  line-height: 1.6;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 4px;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.variable-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.variable-item {
  display: flex;
  gap: 12px;
  font-size: 13px;
}

.var-code {
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 4px;
  font-family: monospace;
  color: #409eff;
  font-size: 12px;
}

.var-desc {
  color: #666;
}

.variable-hint {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.hint-tag {
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  padding: 2px 10px;
  border-radius: 4px;
}

.preview-box {
  padding: 12px 16px;
  background: #f0f9eb;
  border-radius: 8px;
  border-left: 3px solid #67c23a;
  font-size: 14px;
  color: #333;
  line-height: 1.8;
}

/* ===== 接收人管理 ===== */
.receiver-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.contact-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.5;
}

.contact-name {
  font-size: 14px;
  color: #333;
}

.contact-phone {
  font-size: 12px;
  color: #999;
}

/* ===== 测试推送 ===== */
.test-result {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 8px;
  margin-top: 12px;
}

.test-result.success {
  background: #f0f9eb;
  color: #67c23a;
}

.test-result.failed {
  background: #fef0f0;
  color: #f56c6c;
}

.test-result .el-icon {
  font-size: 20px;
}

.test-duration {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
  .channel-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .level-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .channel-grid {
    grid-template-columns: 1fr 1fr;
  }

  .level-grid {
    grid-template-columns: 1fr;
  }

  .silent-item {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .preference-page {
    padding: 12px;
  }

  .header-actions {
    flex-wrap: wrap;
  }

  .setting-section {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 18px;
  }

  .channel-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>