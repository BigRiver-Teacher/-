<template>
  <div class="space-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><House /></el-icon>
        居家空间档案
      </h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加空间
        </el-button>
        <el-button type="success" plain @click="handleExport">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
        <el-button type="primary" plain @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- ===== 统计卡片 ===== -->
    <div class="stats-grid">
      <div class="stat-card total">
        <div class="stat-icon">
          <el-icon><House /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.total }}</div>
          <div class="stat-label">总空间数</div>
        </div>
      </div>
      <div class="stat-card occupied">
        <div class="stat-icon">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.occupied }}</div>
          <div class="stat-label">已入住</div>
        </div>
      </div>
      <div class="stat-card vacant">
        <div class="stat-icon">
          <el-icon><HomeFilled /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.vacant }}</div>
          <div class="stat-label">空置房间</div>
        </div>
      </div>
      <div class="stat-card safety">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.safeRooms }}</div>
          <div class="stat-label">安全评级 A/B</div>
        </div>
      </div>
    </div>

    <!-- ===== 筛选区域 ===== -->
    <div class="filter-section">
      <div class="filter-header">
        <div class="filter-title">
          <el-icon><Filter /></el-icon>
          <span>筛选条件</span>
        </div>
        <div class="filter-actions">
          <el-button type="primary" plain @click="resetFilter">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </div>
      <div class="filter-body">
        <el-form :model="filterForm" label-width="80px" inline>
          <el-form-item label="楼栋">
            <el-select
              v-model="filterForm.building"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in buildingOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="楼层">
            <el-select v-model="filterForm.floor" placeholder="全部" clearable style="width: 100px">
              <el-option
                v-for="item in floorOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="入住状态">
            <el-select
              v-model="filterForm.occupancyStatus"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in occupancyOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="安全评级">
            <el-select
              v-model="filterForm.safetyRating"
              placeholder="全部"
              clearable
              style="width: 110px"
            >
              <el-option
                v-for="item in ratingOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="老人姓名">
            <el-input
              v-model="filterForm.elderName"
              placeholder="请输入姓名"
              clearable
              style="width: 150px"
              prefix-icon="Search"
            />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- ===== 卡片列表区域 ===== -->
    <div class="card-section">
      <div class="card-header">
        <div class="card-title">
          <el-icon><List /></el-icon>
          <span>空间列表</span>
          <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredList.length === 0" class="empty-state">
        <el-empty description="暂无空间记录" />
      </div>

      <!-- 卡片列表 -->
      <div v-else class="card-list">
        <div v-for="item in paginatedList" :key="item.code" class="space-card" :class="item.status">
          <div class="card-left">
            <div class="card-icon" :class="getSafetyRatingClass(item.safetyRating)">
              {{ item.safetyRating }}
            </div>
          </div>

          <div class="card-center">
            <div class="card-row">
              <span class="card-code">{{ item.code }}</span>
              <span class="card-name">{{ item.name }}</span>
              <el-tag :type="item.occupancyStatus === 'occupied' ? 'success' : 'info'" size="small">
                {{ item.occupancyStatus === 'occupied' ? '✅ 已入住' : '📭 空置' }}
              </el-tag>
              <el-tag :type="getStatusType(item.status)" size="small">
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>

            <div class="card-row">
              <span class="card-detail">
                <el-icon><OfficeBuilding /></el-icon>
                {{ item.building }} {{ item.floor }}层
              </span>
              <span class="card-detail">
                <el-icon><HomeFilled /></el-icon>
                {{ item.layout }} {{ item.area }}㎡
              </span>
              <span v-if="item.occupancyStatus === 'occupied'" class="card-detail">
                <el-icon><User /></el-icon>
                {{ item.elderName }}
                <span class="card-sub">入住：{{ item.moveInDate }}</span>
              </span>
              <span v-else class="card-detail">
                <el-icon><Clock /></el-icon>
                上次入住：{{ item.lastOccupied || '无记录' }}
              </span>
            </div>

            <div class="card-row">
              <span class="card-label">适老化配置：</span>
              <span class="card-tags">
                <el-tag
                  v-for="facility in item.facilities"
                  :key="facility"
                  size="small"
                  type="success"
                  style="margin-right: 4px"
                >
                  ✅ {{ facility }}
                </el-tag>
                <span v-if="item.facilities.length === 0" style="color: #ccc"> 暂无配置 </span>
              </span>
            </div>

            <div class="card-row">
              <span class="card-label">风险因素：</span>
              <span class="card-tags">
                <el-tag
                  v-for="risk in item.risks"
                  :key="risk"
                  size="small"
                  type="danger"
                  style="margin-right: 4px"
                >
                  ⚠️ {{ risk }}
                </el-tag>
                <span v-if="item.risks.length === 0" style="color: #67c23a"> ✅ 无风险因素 </span>
              </span>
            </div>

            <div class="card-row">
              <span class="card-label">最近检查：</span>
              <span class="card-detail">
                {{ item.lastInspection || '暂无' }}
                <span v-if="item.lastInspectionResult" class="card-sub">
                  结果：{{ item.lastInspectionResult }}
                </span>
              </span>
              <span class="card-label" style="margin-left: 20px">下次检查：</span>
              <span class="card-detail">
                {{ item.nextInspection || '未安排' }}
              </span>
            </div>
          </div>

          <div class="card-right">
            <el-button size="small" type="primary" link @click="handleView(item)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button size="small" type="warning" link @click="handleEdit(item)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="info" link @click="handleInspection(item)">
              <el-icon><Document /></el-icon>
              检查记录
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(item)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          background
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[6, 12, 18, 24]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="currentPage = 1"
        >
          <template #total="{ total }">
            <span style="font-weight: 500">共 {{ total }} 条</span>
          </template>
          <template #jumper="{ go }">
            <span style="margin-left: 8px; font-size: 14px; color: #666">前往</span>
            <el-input-number
              :min="1"
              :max="Math.ceil(total / pageSize) || 1"
              :model-value="currentPage"
              @update:model-value="
                (val) => {
                  if (val >= 1 && val <= Math.ceil(total / pageSize)) go(val)
                }
              "
              size="small"
              controls-position="right"
              style="width: 60px; margin: 0 4px"
            />
            <span style="font-size: 14px; color: #666">页</span>
          </template>
        </el-pagination>
      </div>
    </div>

    <!-- ===== 查看详情弹框 ===== -->
    <el-dialog v-model="detailVisible" title="空间详情" width="620px">
      <div v-if="detailData" class="detail-content">
        <div class="detail-header">
          <div class="detail-title">
            <span class="detail-code">{{ detailData.code }}</span>
            <span class="detail-name">{{ detailData.name }}</span>
            <el-tag
              :type="detailData.occupancyStatus === 'occupied' ? 'success' : 'info'"
              size="small"
            >
              {{ detailData.occupancyStatus === 'occupied' ? '✅ 已入住' : '📭 空置' }}
            </el-tag>
            <el-tag :type="getStatusType(detailData.status)" size="small">
              {{ getStatusText(detailData.status) }}
            </el-tag>
          </div>
          <div class="detail-rating" :class="getSafetyRatingClass(detailData.safetyRating)">
            安全评级：{{ detailData.safetyRating }}
          </div>
        </div>

        <el-divider />

        <div class="detail-grid">
          <div class="detail-section">
            <div class="section-title">基本信息</div>
            <div class="detail-item">
              <span class="detail-label">楼栋</span>
              <span class="detail-value">{{ detailData.building }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">楼层</span>
              <span class="detail-value">{{ detailData.floor }} 层</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">户型</span>
              <span class="detail-value">{{ detailData.layout }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">面积</span>
              <span class="detail-value">{{ detailData.area }} ㎡</span>
            </div>
            <div v-if="detailData.occupancyStatus === 'occupied'" class="detail-item">
              <span class="detail-label">入住老人</span>
              <span class="detail-value">{{ detailData.elderName }}</span>
            </div>
            <div v-if="detailData.occupancyStatus === 'occupied'" class="detail-item">
              <span class="detail-label">入住时间</span>
              <span class="detail-value">{{ detailData.moveInDate }}</span>
            </div>
            <div v-else class="detail-item">
              <span class="detail-label">上次入住</span>
              <span class="detail-value">{{ detailData.lastOccupied || '无记录' }}</span>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">适老化配置</div>
            <div class="facility-grid">
              <span
                v-for="facility in allFacilities"
                :key="facility.key"
                class="facility-item"
                :class="{ active: detailData.facilities.includes(facility.label) }"
              >
                {{ detailData.facilities.includes(facility.label) ? '✅' : '❌' }}
                {{ facility.label }}
              </span>
            </div>
          </div>
        </div>

        <el-divider />

        <div class="detail-grid">
          <div class="detail-section">
            <div class="section-title">风险因素</div>
            <div class="risk-list">
              <span v-for="risk in detailData.risks" :key="risk" class="risk-item">
                ⚠️ {{ risk }}
              </span>
              <span v-if="detailData.risks.length === 0" style="color: #67c23a">
                ✅ 无风险因素
              </span>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">检查记录</div>
            <div v-if="detailData.inspectionHistory && detailData.inspectionHistory.length > 0">
              <div
                v-for="(record, idx) in detailData.inspectionHistory"
                :key="idx"
                class="inspection-record"
              >
                <span class="inspection-date">{{ record.date }}</span>
                <el-tag :type="record.result === '合格' ? 'success' : 'danger'" size="small">
                  {{ record.result }}
                </el-tag>
                <span class="inspection-person">{{ record.inspector }}</span>
              </div>
            </div>
            <span v-else style="color: #ccc">暂无检查记录</span>
          </div>
        </div>

        <div class="detail-actions">
          <el-button type="primary" size="small" @click="handleAddInspectionFromDetail">
            <el-icon><Plus /></el-icon>
            添加检查
          </el-button>
          <el-button type="warning" size="small" @click="handleEditFromDetail">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDeleteFromDetail">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 添加/编辑弹框 ===== -->
    <el-dialog
      v-model="formDialogVisible"
      :title="formTitle"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="空间编号" prop="code">
              <el-input v-model="formData.code" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="空间名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入空间名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="楼栋" prop="building">
              <el-select v-model="formData.building" placeholder="请选择楼栋" style="width: 100%">
                <el-option
                  v-for="item in buildingOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="楼层" prop="floor">
              <el-input-number
                v-model="formData.floor"
                :min="1"
                :max="30"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="户型" prop="layout">
              <el-select v-model="formData.layout" placeholder="请选择户型" style="width: 100%">
                <el-option label="一室一厅" value="一室一厅" />
                <el-option label="两室一厅" value="两室一厅" />
                <el-option label="三室一厅" value="三室一厅" />
                <el-option label="独居公寓" value="独居公寓" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面积(㎡)" prop="area">
              <el-input-number
                v-model="formData.area"
                :min="10"
                :max="200"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="入住状态" prop="occupancyStatus">
          <el-radio-group v-model="formData.occupancyStatus">
            <el-radio label="occupied">已入住</el-radio>
            <el-radio label="vacant">空置</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="formData.occupancyStatus === 'occupied'" label="入住老人">
          <el-select v-model="formData.elderName" placeholder="请选择入住老人" style="width: 100%">
            <el-option
              v-for="item in elderOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="适老化配置">
          <el-checkbox-group v-model="formData.facilities">
            <el-checkbox
              v-for="facility in allFacilities"
              :key="facility.key"
              :label="facility.label"
            />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="风险因素">
          <el-checkbox-group v-model="formData.risks">
            <el-checkbox label="地面湿滑" />
            <el-checkbox label="光线不足" />
            <el-checkbox label="家具摆放不当" />
            <el-checkbox label="用电隐患" />
            <el-checkbox label="消防隐患" />
            <el-checkbox label="地面高差" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="空间状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="正常" value="normal" />
            <el-option label="改造中" value="renovating" />
            <el-option label="待维修" value="repair" />
            <el-option label="停用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- ===== 检查记录弹框 ===== -->
    <el-dialog
      v-model="inspectionDialogVisible"
      title="添加检查记录"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="inspectionFormRef"
        :model="inspectionForm"
        :rules="inspectionRules"
        label-width="100px"
      >
        <el-form-item label="检查类型" prop="type">
          <el-select v-model="inspectionForm.type" placeholder="请选择检查类型" style="width: 100%">
            <el-option label="月度检查" value="月度检查" />
            <el-option label="季度检查" value="季度检查" />
            <el-option label="专项检查" value="专项检查" />
            <el-option label="应急检查" value="应急检查" />
          </el-select>
        </el-form-item>
        <el-form-item label="检查人" prop="inspector">
          <el-input v-model="inspectionForm.inspector" placeholder="请输入检查人姓名" />
        </el-form-item>
        <el-form-item label="检查结果" prop="result">
          <el-select
            v-model="inspectionForm.result"
            placeholder="请选择检查结果"
            style="width: 100%"
          >
            <el-option label="合格" value="合格" />
            <el-option label="不合格" value="不合格" />
            <el-option label="需整改" value="需整改" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input
            v-model="inspectionForm.issue"
            type="textarea"
            :rows="2"
            placeholder="请输入问题描述"
          />
        </el-form-item>
        <el-form-item label="整改状态">
          <el-select
            v-model="inspectionForm.repairStatus"
            placeholder="请选择整改状态"
            style="width: 100%"
          >
            <el-option label="待整改" value="待整改" />
            <el-option label="整改中" value="整改中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item label="整改期限">
          <el-date-picker
            v-model="inspectionForm.deadline"
            type="date"
            placeholder="选择整改期限"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            action="#"
            :auto-upload="false"
            :limit="3"
            multiple
            accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
          >
            <el-button size="small">
              <el-icon><Upload /></el-icon>
              上传附件
            </el-button>
            <template #tip>
              <div style="font-size: 12px; color: #999">支持 PDF、图片格式，单个文件不超过10MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inspectionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitInspection">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  House,
  Plus,
  Download,
  Refresh,
  Filter,
  Search,
  User,
  HomeFilled,
  CircleCheck,
  View,
  Edit,
  Delete,
  Document,
  OfficeBuilding,
  Clock,
  List,
  Upload,
} from '@element-plus/icons-vue'
import { getSpaceList, getSpaceById, updateSpace, deleteSpace, addInspection } from '@/api/space'

// ===== 统计数据 =====
const stats = reactive({
  total: 0,
  occupied: 0,
  vacant: 0,
  safeRooms: 0,
})

// ===== 所有适老化配置 =====
const allFacilities = [
  { key: '扶手', label: '卫生间扶手' },
  { key: '防滑', label: '防滑地面' },
  { key: '呼叫按钮', label: '紧急呼叫按钮' },
  { key: '智能传感器', label: '智能传感器' },
  { key: '摄像头', label: '监控摄像头' },
  { key: '可调节床', label: '可调节床' },
  { key: '轮椅通道', label: '轮椅通道' },
  { key: '无障碍设施', label: '无障碍设施' },
]

// ===== 数据来源：后端 API（GET /space/list） =====

// ===== 数据列表 =====
const spaceList = ref([])

// ===== 筛选条件 =====
const filterForm = reactive({
  building: '',
  floor: '',
  occupancyStatus: '',
  safetyRating: '',
  elderName: '',
})

const buildingOptions = [
  { label: '全部', value: '' },
  { label: '3栋', value: '3栋' },
  { label: '4栋', value: '4栋' },
  { label: '5栋', value: '5栋' },
]

const floorOptions = [
  { label: '全部', value: '' },
  { label: '1层', value: 1 },
  { label: '2层', value: 2 },
  { label: '3层', value: 3 },
  { label: '4层', value: 4 },
  { label: '5层', value: 5 },
  { label: '6层', value: 6 },
  { label: '7层', value: 7 },
]

const occupancyOptions = [
  { label: '全部', value: '' },
  { label: '已入住', value: 'occupied' },
  { label: '空置', value: 'vacant' },
]

const ratingOptions = [
  { label: '全部', value: '' },
  { label: 'A级', value: 'A' },
  { label: 'B级', value: 'B' },
  { label: 'C级', value: 'C' },
  { label: 'D级', value: 'D' },
]

// ===== 老人选项 =====
const elderOptions = ref([
  { label: '张桂花', value: '张桂花' },
  { label: '李建国', value: '李建国' },
  { label: '王秀英', value: '王秀英' },
  { label: '刘德明', value: '刘德明' },
  { label: '陈素芬', value: '陈素芬' },
  { label: '赵志强', value: '赵志强' },
  { label: '孙美兰', value: '孙美兰' },
  { label: '周永康', value: '周永康' },
  { label: '吴玉珍', value: '吴玉珍' },
  { label: '郑国平', value: '郑国平' },
  { label: '钱素琴', value: '钱素琴' },
  { label: '冯建华', value: '冯建华' },
])

// ===== 分页 =====
const currentPage = ref(1)
const pageSize = ref(6)

// ===== 详情弹框 =====
const detailVisible = ref(false)
const detailData = ref(null)

// ===== 表单弹框 =====
const formDialogVisible = ref(false)
const formTitle = ref('添加空间')
const isEdit = ref(false)
const editItem = ref(null)
const formRef = ref(null)

const formData = reactive({
  code: '',
  name: '',
  building: '3栋',
  floor: 1,
  layout: '一室一厅',
  area: 45,
  occupancyStatus: 'vacant',
  elderName: '',
  facilities: [],
  risks: [],
  status: 'normal',
  remark: '',
})

const formRules = {
  name: [{ required: true, message: '请输入空间名称', trigger: 'blur' }],
  building: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  floor: [{ required: true, message: '请输入楼层', trigger: 'blur' }],
  layout: [{ required: true, message: '请选择户型', trigger: 'change' }],
  area: [{ required: true, message: '请输入面积', trigger: 'blur' }],
}

// ===== 检查记录弹框 =====
const inspectionDialogVisible = ref(false)
const inspectionTarget = ref(null)
const inspectionFormRef = ref(null)

const inspectionForm = reactive({
  type: '',
  inspector: '',
  result: '',
  issue: '',
  repairStatus: '',
  deadline: '',
})

const inspectionRules = {
  type: [{ required: true, message: '请选择检查类型', trigger: 'change' }],
  inspector: [{ required: true, message: '请输入检查人', trigger: 'blur' }],
  result: [{ required: true, message: '请选择检查结果', trigger: 'change' }],
}

// ===== 自动生成编号 =====
function generateCode(building, floor) {
  const floorStr = String(floor).padStart(2, '0')
  const existingCodes = spaceList.value.map((item) => item.code)
  let maxNum = 0
  const prefix = `${building.replace('栋', '')}-${floorStr}`
  existingCodes.forEach((code) => {
    if (code.startsWith(prefix)) {
      const num = parseInt(code.split('-')[1])
      if (num > maxNum) maxNum = num
    }
  })
  const nextNum = String(maxNum + 1).padStart(2, '0')
  return `${building.replace('栋', '')}-${floorStr}${nextNum}`
}

// ===== 计算安全评级 =====
function calculateSafetyRating(item) {
  let score = 100

  // 适老化配置加分
  const facilityScore = item.facilities.length * 5
  score += Math.min(facilityScore, 30)

  // 风险因素扣分
  const riskScore = item.risks.length * 10
  score -= riskScore

  // 状态扣分
  if (item.status === 'renovating') score -= 10
  if (item.status === 'repair') score -= 15
  if (item.status === 'inactive') score -= 20

  // 检查结果影响
  if (item.lastInspectionResult === '不合格') score -= 15
  if (item.lastInspectionResult === '需整改') score -= 5

  // 入住状态
  if (item.occupancyStatus === 'vacant') score += 5

  score = Math.max(0, Math.min(100, score))

  if (score >= 85) return 'A'
  if (score >= 70) return 'B'
  if (score >= 55) return 'C'
  return 'D'
}

// ===== 字段解析 / 格式化 =====
function parseJsonArray(value) {
  if (Array.isArray(value)) return value
  if (typeof value === 'string' && value) {
    try {
      const parsed = JSON.parse(value)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return []
    }
  }
  return []
}

function formatDateTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ')
}

function mapSpace(item) {
  return {
    ...item,
    facilities: parseJsonArray(item.facilities),
    risks: parseJsonArray(item.risks),
    moveInDate: formatDateTime(item.moveInDate),
    lastOccupied: formatDateTime(item.lastOccupied),
    lastInspection: formatDateTime(item.lastInspection),
    nextInspection: formatDateTime(item.nextInspection),
  }
}

// ===== 加载数据 =====
async function loadData() {
  try {
    const res = await getSpaceList({ page: 1, size: 1000 })
    const data = res.data.data
    spaceList.value = (data.list || []).map(mapSpace)
    updateStats()
  } catch {
    // 错误已由响应拦截器统一提示
  }
}

// ===== 更新统计数据 =====
function updateStats() {
  const list = spaceList.value
  stats.total = list.length
  stats.occupied = list.filter((item) => item.occupancyStatus === 'occupied').length
  stats.vacant = list.filter((item) => item.occupancyStatus === 'vacant').length
  stats.safeRooms = list.filter(
    (item) => item.safetyRating === 'A' || item.safetyRating === 'B',
  ).length
}

// ===== 筛选数据 =====
const filteredList = computed(() => {
  let list = [...spaceList.value]

  if (filterForm.building) {
    list = list.filter((item) => item.building === filterForm.building)
  }
  if (filterForm.floor) {
    list = list.filter((item) => item.floor === filterForm.floor)
  }
  if (filterForm.occupancyStatus) {
    list = list.filter((item) => item.occupancyStatus === filterForm.occupancyStatus)
  }
  if (filterForm.safetyRating) {
    list = list.filter((item) => item.safetyRating === filterForm.safetyRating)
  }
  if (filterForm.elderName) {
    list = list.filter((item) => item.elderName && item.elderName.includes(filterForm.elderName))
  }

  return list
})

const total = computed(() => filteredList.value.length)

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

// ===== 工具函数 =====
function getSafetyRatingClass(rating) {
  const map = {
    A: 'rating-a',
    B: 'rating-b',
    C: 'rating-c',
    D: 'rating-d',
  }
  return map[rating] || ''
}

function getStatusType(status) {
  const map = {
    normal: 'success',
    renovating: 'warning',
    repair: 'danger',
    inactive: 'info',
  }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = {
    normal: '正常',
    renovating: '改造中',
    repair: '待维修',
    inactive: '停用',
  }
  return map[status] || status
}

// ===== 操作函数 =====
async function handleView(item) {
  try {
    const res = await getSpaceById(item.id)
    const data = res.data.data
    const space = mapSpace(data.space || {})
    const history = Array.isArray(data.inspectionHistory) ? data.inspectionHistory : []
    detailData.value = {
      ...space,
      inspectionHistory: history.map((record) => ({
        ...record,
        date: formatDateTime(record.date || record.createTime),
      })),
    }
  } catch {
    // 详情接口异常时兜底使用列表数据
    detailData.value = item
  }
  detailVisible.value = true
}

function handleAdd() {
  isEdit.value = false
  editItem.value = null
  formTitle.value = '添加空间'
  const nextCode = generateCode('3栋', 1)
  Object.assign(formData, {
    code: nextCode,
    name: '',
    building: '3栋',
    floor: 1,
    layout: '一室一厅',
    area: 45,
    occupancyStatus: 'vacant',
    elderName: '',
    facilities: [],
    risks: [],
    status: 'normal',
    remark: '',
  })
  formRef.value?.resetFields()
  formDialogVisible.value = true
}

function handleEdit(item) {
  isEdit.value = true
  editItem.value = item
  formTitle.value = '编辑空间'
  Object.assign(formData, {
    code: item.code,
    name: item.name,
    building: item.building,
    floor: item.floor,
    layout: item.layout,
    area: item.area,
    occupancyStatus: item.occupancyStatus,
    elderName: item.elderName || '',
    facilities: [...item.facilities],
    risks: [...item.risks],
    status: item.status,
    remark: item.remark || '',
  })
  formDialogVisible.value = true
}

function handleDelete(item) {
  ElMessageBox.confirm(`确定要删除空间 "${item.name}" 吗？此操作不可恢复！`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      if (item.id) {
        await deleteSpace(item.id)
      }
      const index = spaceList.value.findIndex((i) => i.code === item.code)
      if (index > -1) {
        spaceList.value.splice(index, 1)
        ElMessage.success('删除成功')
        updateStats()
        if (currentPage.value > Math.ceil(total.value / pageSize.value)) {
          currentPage.value = 1
        }
      }
    })
    .catch(() => {})
}

function handleInspection(item) {
  inspectionTarget.value = item
  Object.assign(inspectionForm, {
    type: '月度检查',
    inspector: '',
    result: '',
    issue: '',
    repairStatus: '',
    deadline: '',
  })
  inspectionFormRef.value?.resetFields()
  inspectionDialogVisible.value = true
}

function handleExport() {
  ElMessage.success('导出报表成功')
}

function resetFilter() {
  filterForm.building = ''
  filterForm.floor = ''
  filterForm.occupancyStatus = ''
  filterForm.safetyRating = ''
  filterForm.elderName = ''
  currentPage.value = 1
}

async function refreshData() {
  await loadData()
  ElMessage.success('数据已刷新')
}

// ===== 提交表单 =====
async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    if (isEdit.value && editItem.value) {
      if (editItem.value.id) {
        await updateSpace(editItem.value.id, {
          name: formData.name,
          building: formData.building,
          floor: formData.floor,
          layout: formData.layout,
          area: formData.area,
          occupancyStatus: formData.occupancyStatus,
          elderName: formData.occupancyStatus === 'occupied' ? formData.elderName : '',
          facilities: [...formData.facilities],
          risks: [...formData.risks],
          status: formData.status,
          remark: formData.remark,
        })
      }
      const index = spaceList.value.findIndex((i) => i.code === editItem.value.code)
      if (index > -1) {
        const updatedItem = {
          ...spaceList.value[index],
          name: formData.name,
          building: formData.building,
          floor: formData.floor,
          layout: formData.layout,
          area: formData.area,
          occupancyStatus: formData.occupancyStatus,
          elderName: formData.occupancyStatus === 'occupied' ? formData.elderName : '',
          facilities: [...formData.facilities],
          risks: [...formData.risks],
          status: formData.status,
          remark: formData.remark,
          moveInDate:
            formData.occupancyStatus === 'occupied'
              ? spaceList.value[index].moveInDate || new Date().toISOString().split('T')[0]
              : '',
          lastOccupied:
            formData.occupancyStatus === 'vacant'
              ? spaceList.value[index].lastOccupied || new Date().toISOString().split('T')[0]
              : '',
        }
        updatedItem.safetyRating = calculateSafetyRating(updatedItem)
        spaceList.value[index] = updatedItem
        ElMessage.success('更新成功')
      }
    } else {
      // 后端未提供新增空间接口，维持本地创建
      const newItem = {
        code: formData.code,
        name: formData.name,
        building: formData.building,
        floor: formData.floor,
        layout: formData.layout,
        area: formData.area,
        occupancyStatus: formData.occupancyStatus,
        elderName: formData.occupancyStatus === 'occupied' ? formData.elderName : '',
        moveInDate:
          formData.occupancyStatus === 'occupied' ? new Date().toISOString().split('T')[0] : '',
        lastOccupied: formData.occupancyStatus === 'vacant' ? '无历史记录' : '',
        facilities: [...formData.facilities],
        risks: [...formData.risks],
        status: formData.status,
        remark: formData.remark,
        lastInspection: '暂无',
        lastInspectionResult: '',
        nextInspection: '未安排',
        inspectionHistory: [],
      }
      newItem.safetyRating = calculateSafetyRating(newItem)
      spaceList.value.unshift(newItem)
      ElMessage.success('添加成功')
    }

    formDialogVisible.value = false
    updateStats()
    if (currentPage.value > Math.ceil(total.value / pageSize.value)) {
      currentPage.value = 1
    }
  } catch {
    // 错误已由响应拦截器统一提示
  }
}

// ===== 提交检查记录 =====
async function submitInspection() {
  const valid = await inspectionFormRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    const target = inspectionTarget.value
    if (target && target.id) {
      await addInspection({
        spaceId: target.id,
        type: inspectionForm.type,
        inspector: inspectionForm.inspector,
        result: inspectionForm.result,
        issue: inspectionForm.issue,
        repairStatus: inspectionForm.repairStatus,
        deadline: inspectionForm.deadline,
      })
    }

    const index = spaceList.value.findIndex((i) => i.code === target.code)
    if (index > -1) {
      const newRecord = {
        date: new Date().toISOString().split('T')[0],
        result: inspectionForm.result,
        inspector: inspectionForm.inspector,
        type: inspectionForm.type,
        issue: inspectionForm.issue,
        repairStatus: inspectionForm.repairStatus,
        deadline: inspectionForm.deadline,
      }

      if (!spaceList.value[index].inspectionHistory) {
        spaceList.value[index].inspectionHistory = []
      }
      spaceList.value[index].inspectionHistory.unshift(newRecord)

      spaceList.value[index].lastInspection = newRecord.date
      spaceList.value[index].lastInspectionResult = newRecord.result
      spaceList.value[index].safetyRating = calculateSafetyRating(spaceList.value[index])

      ElMessage.success('检查记录添加成功')
      inspectionDialogVisible.value = false
      updateStats()
    }
  } catch {
    // 错误已由响应拦截器统一提示
  }
}

// ===== 详情弹框中的操作 =====
function handleAddInspectionFromDetail() {
  const item = detailData.value
  detailVisible.value = false
  setTimeout(() => {
    handleInspection(item)
  }, 300)
}

function handleEditFromDetail() {
  const item = detailData.value
  detailVisible.value = false
  setTimeout(() => {
    handleEdit(item)
  }, 300)
}

function handleDeleteFromDetail() {
  const item = detailData.value
  detailVisible.value = false
  setTimeout(() => {
    handleDelete(item)
  }, 300)
}

// ===== 生命周期 =====
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.space-page {
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

/* ===== 统计卡片 ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-card.total .stat-icon {
  background: #e6f7ff;
  color: #409eff;
}
.stat-card.occupied .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
}
.stat-card.vacant .stat-icon {
  background: #fdf6ec;
  color: #e6a23c;
}
.stat-card.safety .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 2px;
}

/* ===== 筛选区域 ===== */
.filter-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.filter-title .el-icon {
  font-size: 18px;
  color: #409eff;
}

.filter-body :deep(.el-form-item) {
  margin-bottom: 8px;
}

/* ===== 卡片区域 ===== */
.card-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.card-title .el-icon {
  font-size: 18px;
  color: #409eff;
}

.empty-state {
  padding: 40px 0;
}

/* ===== 空间卡片 ===== */
.card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.space-card {
  display: flex;
  align-items: stretch;
  background: #f8f9fa;
  border-radius: 10px;
  padding: 14px 18px;
  border-left: 4px solid #999;
  transition: all 0.3s;
}

.space-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transform: translateX(4px);
}

.space-card.normal {
  border-left-color: #67c23a;
}
.space-card.renovating {
  border-left-color: #e6a23c;
}
.space-card.repair {
  border-left-color: #f56c6c;
}
.space-card.inactive {
  border-left-color: #909399;
}

.card-left {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-right: 16px;
  flex-shrink: 0;
}

.card-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.rating-a {
  background: #67c23a;
}
.rating-b {
  background: #409eff;
}
.rating-c {
  background: #e6a23c;
}
.rating-d {
  background: #f56c6c;
}

.card-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.card-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.card-code {
  font-size: 14px;
  font-weight: 700;
  color: #409eff;
  font-family: monospace;
}

.card-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.card-detail {
  font-size: 13px;
  color: #666;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.card-sub {
  font-size: 12px;
  color: #999;
  margin-left: 4px;
}

.card-label {
  font-size: 13px;
  color: #999;
}

.card-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 2px;
}

.card-right {
  display: flex;
  flex-direction: column;
  gap: 2px;
  justify-content: center;
  padding-left: 16px;
  flex-shrink: 0;
  border-left: 1px solid #e8e8e8;
}

.card-right .el-button {
  font-size: 12px;
  padding: 2px 8px;
  justify-content: flex-start;
}

.card-right .el-button .el-icon {
  font-size: 14px;
  margin-right: 2px;
}

/* ===== 分页 ===== */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
}

/* ===== 详情弹框 ===== */
.detail-content {
  padding: 4px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-code {
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
  font-family: monospace;
}

.detail-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.detail-rating {
  font-size: 16px;
  font-weight: 600;
  padding: 4px 16px;
  border-radius: 20px;
  color: #fff;
}

.detail-rating.rating-a {
  background: #67c23a;
}
.detail-rating.rating-b {
  background: #409eff;
}
.detail-rating.rating-c {
  background: #e6a23c;
}
.detail-rating.rating-d {
  background: #f56c6c;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 24px;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-section .section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  border-bottom: 1px solid #f5f5f5;
}

.detail-label {
  font-size: 13px;
  color: #999;
}

.detail-value {
  font-size: 13px;
  color: #333;
}

.facility-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px 12px;
}

.facility-item {
  font-size: 13px;
  padding: 2px 0;
  color: #ccc;
}

.facility-item.active {
  color: #333;
}

.risk-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.risk-item {
  font-size: 13px;
  color: #f56c6c;
  padding: 2px 10px;
  background: #fef0f0;
  border-radius: 4px;
}

.inspection-record {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 13px;
}

.inspection-date {
  color: #999;
  min-width: 80px;
}

.inspection-person {
  color: #666;
}

.detail-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }

  .filter-body :deep(.el-form-item) {
    display: block;
    width: 100%;
  }

  .filter-body :deep(.el-select) {
    width: 100% !important;
  }
  .filter-body :deep(.el-input) {
    width: 100% !important;
  }

  .space-card {
    flex-direction: column;
    gap: 10px;
  }

  .card-left {
    padding-right: 0;
    justify-content: flex-start;
  }

  .card-right {
    flex-direction: row;
    padding-left: 0;
    padding-top: 10px;
    border-left: none;
    border-top: 1px solid #e8e8e8;
    flex-wrap: wrap;
  }

  .card-right .el-button {
    padding: 2px 6px;
    font-size: 12px;
  }

  .card-row {
    gap: 6px;
  }

  .space-page {
    padding: 12px;
  }

  .header-actions {
    flex-wrap: wrap;
  }

  .detail-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .facility-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .page-title {
    font-size: 18px;
  }

  .stat-number {
    font-size: 22px;
  }

  .stat-card {
    padding: 16px;
  }

  .card-code {
    font-size: 12px;
  }
  .card-name {
    font-size: 14px;
  }
  .card-detail {
    font-size: 12px;
  }
}
</style>