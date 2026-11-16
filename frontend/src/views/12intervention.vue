<template>
  <div class="intervention-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Tools /></el-icon>
        干预方案管理
      </h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增方案
        </el-button>
        <el-button type="success" plain @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
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
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.total }}</div>
          <div class="stat-label">总方案数</div>
        </div>
      </div>
      <div class="stat-card ongoing">
        <div class="stat-icon">
          <el-icon><Timer /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.ongoing }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </div>
      <div class="stat-card completed">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      <div class="stat-card pending">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.pendingReview }}</div>
          <div class="stat-label">待审核</div>
        </div>
      </div>
      <div class="stat-card rating">
        <div class="stat-icon">
          <el-icon><StarFilled /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.avgRating }}</div>
          <div class="stat-label">平均评分</div>
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
          <el-form-item label="方案名称">
            <el-input
              v-model="filterForm.name"
              placeholder="请输入名称"
              clearable
              style="width: 160px"
              prefix-icon="Search"
            />
          </el-form-item>
          <el-form-item label="干预类型">
            <el-select v-model="filterForm.type" placeholder="全部" clearable style="width: 130px">
              <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="filterForm.status"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="优先级">
            <el-select
              v-model="filterForm.priority"
              placeholder="全部"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="item in priorityOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关联老人">
            <el-input
              v-model="filterForm.elderName"
              placeholder="请输入老人姓名"
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
          <span>方案列表</span>
          <el-tag size="small" type="info">共 {{ total }} 条</el-tag>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredList.length === 0" class="empty-state">
        <el-empty description="暂无干预方案" />
      </div>

      <!-- 卡片列表 -->
      <div v-else class="card-list">
        <div
          v-for="item in paginatedList"
          :key="item.code"
          class="intervention-card"
          :class="item.status"
        >
          <!-- 卡片头部 -->
          <div class="card-top">
            <div class="card-left">
              <div class="card-rating">
                <el-rate
                  v-model="item.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  score-template="{value}"
                  style="display: inline-flex; align-items: center"
                />
              </div>
              <span class="card-code">{{ item.code }}</span>
              <span class="card-name">{{ item.name }}</span>
              <el-tag :type="getPriorityType(item.priority)" size="small">
                {{ getPriorityText(item.priority) }}
              </el-tag>
            </div>
            <div class="card-right">
              <el-tag :type="getStatusType(item.status)" size="small">
                {{ getStatusText(item.status) }}
              </el-tag>
              <span v-if="item.status === 'ongoing'" class="card-week">
                （第{{ item.currentWeek }}/{{ item.totalWeeks }}周）
              </span>
            </div>
          </div>

          <!-- 卡片中部 -->
          <div class="card-middle">
            <div class="card-row">
              <span class="card-label">干预类型：</span>
              <el-tag :type="getTypeColor(item.type)" size="small">
                {{ item.type }}
              </el-tag>
              <span class="card-label" style="margin-left: 20px">负责人：</span>
              <span class="card-value">{{ item.leader }}</span>
            </div>
            <div class="card-row">
              <span class="card-label">适用对象：</span>
              <span class="card-value">{{ item.targetGroup }}</span>
              <span class="card-label" style="margin-left: 20px">关联老人：</span>
              <span class="card-value">
                <el-tag
                  v-for="elder in item.associatedElders"
                  :key="elder"
                  size="small"
                  type="info"
                  style="margin-right: 4px"
                >
                  {{ elder }}
                </el-tag>
                <span
                  v-if="!item.associatedElders || item.associatedElders.length === 0"
                  style="color: #ccc"
                >
                  暂无
                </span>
              </span>
            </div>
            <div class="card-row">
              <span class="card-label">执行进度：</span>
              <el-progress
                :percentage="item.progress"
                :color="getProgressColor(item.progress)"
                :stroke-width="8"
                style="width: 200px"
              />
              <span class="card-label" style="margin-left: 20px">开始：</span>
              <span class="card-value">{{ item.startDate }}</span>
              <span class="card-label" style="margin-left: 12px">预计完成：</span>
              <span class="card-value">{{ item.endDate }}</span>
            </div>
          </div>

          <!-- 卡片底部 -->
          <div class="card-bottom">
            <el-button size="small" type="primary" link @click="handleView(item)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button size="small" type="info" link @click="handleRecords(item)">
              <el-icon><Document /></el-icon>
              执行记录
            </el-button>
            <el-button
              v-if="item.status === 'pending'"
              size="small"
              type="success"
              link
              @click="handleReview(item)"
            >
              <el-icon><Select /></el-icon>
              审核
            </el-button>
            <el-button
              v-if="item.status === 'pending'"
              size="small"
              type="warning"
              link
              @click="handleEdit(item)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              v-if="item.status === 'ongoing'"
              size="small"
              type="warning"
              link
              @click="handlePause(item)"
            >
              <el-icon><VideoPause /></el-icon>
              暂停
            </el-button>
            <el-button
              v-if="item.status === 'ongoing'"
              size="small"
              type="danger"
              link
              @click="handleTerminate(item)"
            >
              <el-icon><CircleClose /></el-icon>
              终止
            </el-button>
            <el-button
              v-if="item.status === 'ongoing'"
              size="small"
              type="success"
              link
              @click="handleComplete(item)"
            >
              <el-icon><Check /></el-icon>
              完成
            </el-button>
            <el-button
              v-if="item.status === 'draft'"
              size="small"
              type="danger"
              link
              @click="handleDelete(item)"
            >
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
          :page-sizes="[5, 10, 15, 20]"
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
    <el-dialog v-model="detailVisible" title="方案详情" width="780px" :close-on-click-modal="false">
      <div v-if="detailData" class="detail-content">
        <!-- 头部 -->
        <div class="detail-header">
          <div class="detail-title">
            <span class="detail-code">{{ detailData.code }}</span>
            <span class="detail-name">{{ detailData.name }}</span>
            <el-tag :type="getPriorityType(detailData.priority)" size="small">
              {{ getPriorityText(detailData.priority) }}
            </el-tag>
            <el-tag :type="getStatusType(detailData.status)" size="small">
              {{ getStatusText(detailData.status) }}
            </el-tag>
          </div>
          <div class="detail-rating">
            <span class="rating-label">方案评分：</span>
            <el-rate
              v-model="detailData.rating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
            />
          </div>
        </div>

        <!-- 流程步骤 -->
        <div class="detail-steps">
          <div class="steps-title">流程进度</div>
          <el-steps :active="detailData.stepIndex" finish-status="success" align-center>
            <el-step
              v-for="step in detailData.steps"
              :key="step.key"
              :title="step.title"
              :description="step.time || ''"
              :status="step.status"
            />
          </el-steps>
        </div>

        <el-divider />

        <!-- 基本信息 -->
        <div class="detail-grid">
          <div class="detail-section">
            <div class="section-title">基本信息</div>
            <div class="detail-item">
              <span class="detail-label">干预类型</span>
              <span class="detail-value">{{ detailData.type }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">负责人</span>
              <span class="detail-value">{{ detailData.leader }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">适用对象</span>
              <span class="detail-value">{{ detailData.targetGroup }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">执行周期</span>
              <span class="detail-value">{{ detailData.totalWeeks }} 周</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">执行频率</span>
              <span class="detail-value">{{ detailData.frequency }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">创建时间</span>
              <span class="detail-value">{{ detailData.createTime }}</span>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">执行统计</div>
            <div class="detail-item">
              <span class="detail-label">总执行次数</span>
              <span class="detail-value"
                >{{ detailData.executedCount }}/{{ detailData.totalCount }}</span
              >
            </div>
            <div class="detail-item">
              <span class="detail-label">执行完成率</span>
              <span class="detail-value">{{ detailData.progress }}%</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">平均评分</span>
              <span class="detail-value">{{ detailData.avgExecRating }} 星</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">异常次数</span>
              <span class="detail-value">{{ detailData.abnormalCount || 0 }} 次</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">开始日期</span>
              <span class="detail-value">{{ detailData.startDate }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">预计完成</span>
              <span class="detail-value">{{ detailData.endDate }}</span>
            </div>
          </div>
        </div>

        <el-divider />

        <!-- 关联老人 -->
        <div class="detail-elders">
          <div class="section-title">关联老人</div>
          <div class="elder-tags">
            <el-tag
              v-for="elder in detailData.associatedElders"
              :key="elder"
              size="large"
              type="info"
              style="margin-right: 8px"
            >
              {{ elder }}
            </el-tag>
            <span
              v-if="!detailData.associatedElders || detailData.associatedElders.length === 0"
              style="color: #ccc"
            >
              暂无关联老人
            </span>
          </div>
        </div>

        <el-divider />

        <!-- 干预目标 -->
        <div class="detail-goals">
          <div class="section-title">干预目标</div>
          <ul class="goal-list">
            <li v-for="(goal, idx) in detailData.goals" :key="idx">{{ goal }}</li>
          </ul>
        </div>

        <el-divider />

        <!-- 具体措施 -->
        <div class="detail-measures">
          <div class="section-title">具体措施</div>
          <ul class="measure-list">
            <li v-for="(measure, idx) in detailData.measures" :key="idx">{{ measure }}</li>
          </ul>
        </div>

        <el-divider />

        <!-- 注意事项 -->
        <div class="detail-notes">
          <div class="section-title">注意事项</div>
          <div class="notes-content">{{ detailData.notes || '无' }}</div>
        </div>

        <el-divider />

        <!-- 底部操作 -->
        <div class="detail-actions">
          <el-button
            v-if="detailData.status === 'pending'"
            type="success"
            size="small"
            @click="
              () => {
                detailVisible = false
                handleReview(detailData)
              }
            "
          >
            <el-icon><Select /></el-icon>
            审核
          </el-button>
          <el-button
            v-if="detailData.status === 'pending' || detailData.status === 'draft'"
            type="warning"
            size="small"
            @click="
              () => {
                detailVisible = false
                handleEdit(detailData)
              }
            "
          >
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button
            v-if="detailData.status === 'ongoing'"
            type="warning"
            size="small"
            @click="
              () => {
                detailVisible = false
                handlePause(detailData)
              }
            "
          >
            <el-icon><VideoPause /></el-icon>
            暂停
          </el-button>
          <el-button
            v-if="detailData.status === 'ongoing'"
            type="danger"
            size="small"
            @click="
              () => {
                detailVisible = false
                handleTerminate(detailData)
              }
            "
          >
            <el-icon><CircleClose /></el-icon>
            终止
          </el-button>
          <el-button
            v-if="detailData.status === 'ongoing'"
            type="success"
            size="small"
            @click="
              () => {
                detailVisible = false
                handleComplete(detailData)
              }
            "
          >
            <el-icon><Check /></el-icon>
            完成
          </el-button>
          <el-button type="primary" size="small" @click="handleExportReport(detailData)">
            <el-icon><Download /></el-icon>
            导出报告
          </el-button>
          <el-button @click="detailVisible = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- ===== 执行记录弹框 ===== -->
    <el-dialog v-model="recordsVisible" title="执行记录" width="720px">
      <div v-if="recordsData" class="records-content">
        <div class="records-header">
          <span class="records-title">{{ recordsData.name }}</span>
          <el-tag :type="getStatusType(recordsData.status)" size="small">
            {{ getStatusText(recordsData.status) }}
          </el-tag>
        </div>
        <el-divider />
        <div class="records-summary">
          <span>总执行：{{ recordsData.totalCount }} 次</span>
          <span>已完成：{{ recordsData.executedCount }} 次</span>
          <span>完成率：{{ recordsData.progress }}%</span>
          <span>平均评分：{{ recordsData.avgExecRating }} 星</span>
        </div>
        <el-table :data="recordsData.records" border style="width: 100%; margin-top: 12px">
          <el-table-column prop="date" label="执行日期" width="120" align="center" />
          <el-table-column prop="elderName" label="老人姓名" width="100" align="center" />
          <el-table-column prop="executor" label="执行人" width="100" align="center" />
          <el-table-column prop="status" label="执行状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="getExecStatusType(row.status)" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="rating" label="评分" width="100" align="center">
            <template #default="{ row }">
              <el-rate
                v-model="row.rating"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}"
                style="display: inline-flex"
              />
            </template>
          </el-table-column>
          <el-table-column prop="note" label="备注" min-width="120">
            <template #default="{ row }">
              <span class="remark-text">{{ row.note || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="附件" width="80" align="center">
            <template #default="{ row }">
              <el-button
                v-if="row.attachments && row.attachments.length > 0"
                size="small"
                type="primary"
                link
              >
                {{ row.attachments.length }}个
              </el-button>
              <span v-else style="color: #ccc">无</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="recordsVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 新增/编辑弹框 ===== -->
    <el-dialog
      v-model="formDialogVisible"
      :title="formTitle"
      width="620px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-form-item label="方案名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入方案名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="干预类型" prop="type">
              <el-select v-model="formData.type" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="item in typeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="formData.priority" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="item in priorityOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="leader">
              <el-input v-model="formData.leader" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用对象" prop="targetGroup">
              <el-input v-model="formData.targetGroup" placeholder="如：高跌倒风险老人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="执行周期(周)" prop="totalWeeks">
              <el-input-number
                v-model="formData.totalWeeks"
                :min="1"
                :max="52"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="执行频率" prop="frequency">
              <el-input v-model="formData.frequency" placeholder="如：每周4次" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="关联老人">
          <el-select
            v-model="formData.associatedElders"
            multiple
            filterable
            placeholder="请选择关联老人"
            style="width: 100%"
          >
            <el-option
              v-for="item in elderOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="干预目标" prop="goals">
          <el-input
            v-model="formData.goalsText"
            type="textarea"
            :rows="2"
            placeholder="每行一个目标，如：&#10;1. 3个月内降低跌倒风险40%&#10;2. 改善步态稳定性"
          />
        </el-form-item>
        <el-form-item label="具体措施" prop="measures">
          <el-input
            v-model="formData.measuresText"
            type="textarea"
            :rows="2"
            placeholder="每行一个措施，如：&#10;1. 每日平衡训练15分钟&#10;2. 每周3次力量训练"
          />
        </el-form-item>
        <el-form-item label="注意事项">
          <el-input
            v-model="formData.notes"
            type="textarea"
            :rows="2"
            placeholder="请输入注意事项"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button v-if="isEdit" type="warning" plain @click="submitForm('draft')"
          >保存草稿</el-button
        >
        <el-button type="primary" @click="submitForm('pending')">
          {{ isEdit ? '提交审核' : '创建并提交审核' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ===== 审核弹框 ===== -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="审核方案"
      width="480px"
      :close-on-click-modal="false"
    >
      <div v-if="reviewData" class="review-content">
        <div class="review-info">
          <div class="review-row">
            <span class="review-label">方案编号</span>
            <span class="review-value" style="font-family: monospace">{{ reviewData.code }}</span>
          </div>
          <div class="review-row">
            <span class="review-label">方案名称</span>
            <span class="review-value">{{ reviewData.name }}</span>
          </div>
          <div class="review-row">
            <span class="review-label">负责人</span>
            <span class="review-value">{{ reviewData.leader }}</span>
          </div>
        </div>
        <el-divider />
        <el-form label-width="80px">
          <el-form-item label="审核意见" required>
            <el-input
              v-model="reviewForm.comment"
              type="textarea"
              :rows="3"
              placeholder="请输入审核意见..."
            />
          </el-form-item>
          <el-form-item label="审核结果" required>
            <el-radio-group v-model="reviewForm.result">
              <el-radio label="approved">通过</el-radio>
              <el-radio label="rejected">驳回</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">确认审核</el-button>
      </template>
    </el-dialog>

    <!-- ===== 添加执行记录弹框 ===== -->
    <el-dialog
      v-model="execDialogVisible"
      title="添加执行记录"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="execFormRef" :model="execForm" :rules="execRules" label-width="100px">
        <el-form-item label="方案名称">
          <span>{{ execTarget?.name }}</span>
        </el-form-item>
        <el-form-item label="老人姓名" prop="elderName">
          <el-select v-model="execForm.elderName" placeholder="请选择老人" style="width: 100%">
            <el-option
              v-for="item in elderOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="执行人" prop="executor">
          <el-input v-model="execForm.executor" placeholder="请输入执行人" />
        </el-form-item>
        <el-form-item label="执行状态" prop="status">
          <el-select v-model="execForm.status" placeholder="请选择" style="width: 100%">
            <el-option label="正常" value="正常" />
            <el-option label="部分执行" value="部分执行" />
            <el-option label="未执行" value="未执行" />
            <el-option label="异常" value="异常" />
          </el-select>
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <el-rate
            v-model="execForm.rating"
            show-text
            :texts="['1星', '2星', '3星', '4星', '5星']"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="execForm.note" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            action="#"
            :auto-upload="false"
            :limit="5"
            multiple
            accept=".pdf,.jpg,.jpeg,.png,.mp4,.doc,.docx"
          >
            <el-button size="small">
              <el-icon><Upload /></el-icon>
              上传附件
            </el-button>
            <template #tip>
              <div style="font-size: 12px; color: #999; margin-top: 4px">
                支持 PDF、图片、视频格式，单个不超过10MB，最多5个
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="execDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitExec">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Tools,
  Plus,
  Download,
  Refresh,
  Filter,
  Search,
  Document,
  Timer,
  CircleCheck,
  Clock,
  StarFilled,
  List,
  View,
  Edit,
  Delete,
  Select,
  VideoPause,
  CircleClose,
  Check,
  Upload,
  Warning as WarningIcon,
} from '@element-plus/icons-vue'
import {
  getInterventionList,
  getInterventionById,
  getInterventionRecords,
  updateIntervention,
  deleteIntervention,
  reviewIntervention,
  pauseIntervention,
  terminateIntervention,
  completeIntervention,
  addInterventionRecord,
} from '@/api/intervention'

// ===== 统计数据 =====
const stats = reactive({
  total: 0,
  ongoing: 0,
  completed: 0,
  pendingReview: 0,
  avgRating: 0,
})

// ===== 数据来源：后端 API（GET /intervention/list） =====

// ===== 数据列表 =====
const interventionList = ref([])

// ===== 筛选条件 =====
const filterForm = reactive({
  name: '',
  type: '',
  status: '',
  priority: '',
  elderName: '',
})

const typeOptions = [
  { label: '全部', value: '' },
  { label: '运动康复', value: '运动康复' },
  { label: '营养干预', value: '营养干预' },
  { label: '心理疏导', value: '心理疏导' },
  { label: '用药管理', value: '用药管理' },
  { label: '日常护理', value: '日常护理' },
  { label: '综合干预', value: '综合干预' },
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '草稿', value: 'draft' },
  { label: '待审核', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '执行中', value: 'ongoing' },
  { label: '已完成', value: 'completed' },
  { label: '已终止', value: 'terminated' },
]

const priorityOptions = [
  { label: '全部', value: '' },
  { label: '紧急', value: 'urgent' },
  { label: '重要', value: 'important' },
  { label: '普通', value: 'normal' },
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
const pageSize = ref(5)

// ===== 详情弹框 =====
const detailVisible = ref(false)
const detailData = ref(null)

// ===== 执行记录弹框 =====
const recordsVisible = ref(false)
const recordsData = ref(null)

// ===== 表单弹框 =====
const formDialogVisible = ref(false)
const formTitle = ref('新增方案')
const isEdit = ref(false)
const editItem = ref(null)
const formRef = ref(null)

const formData = reactive({
  name: '',
  type: '',
  priority: 'normal',
  leader: '',
  targetGroup: '',
  totalWeeks: 4,
  frequency: '',
  associatedElders: [],
  goalsText: '',
  measuresText: '',
  notes: '',
})

const formRules = {
  name: [{ required: true, message: '请输入方案名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择干预类型', trigger: 'change' }],
  leader: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
  targetGroup: [{ required: true, message: '请输入适用对象', trigger: 'blur' }],
  totalWeeks: [{ required: true, message: '请输入执行周期', trigger: 'blur' }],
  frequency: [{ required: true, message: '请输入执行频率', trigger: 'blur' }],
  goalsText: [{ required: true, message: '请输入干预目标', trigger: 'blur' }],
  measuresText: [{ required: true, message: '请输入具体措施', trigger: 'blur' }],
}

// ===== 审核弹框 =====
const reviewDialogVisible = ref(false)
const reviewData = ref(null)
const reviewForm = reactive({
  comment: '',
  result: 'approved',
})

// ===== 执行记录弹框 =====
const execDialogVisible = ref(false)
const execTarget = ref(null)
const execFormRef = ref(null)

const execForm = reactive({
  elderName: '',
  executor: '',
  status: '正常',
  rating: 4,
  note: '',
})

const execRules = {
  elderName: [{ required: true, message: '请选择老人', trigger: 'change' }],
  executor: [{ required: true, message: '请输入执行人', trigger: 'blur' }],
  status: [{ required: true, message: '请选择执行状态', trigger: 'change' }],
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
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

function mapIntervention(item) {
  return {
    ...item,
    associatedElders: parseJsonArray(item.associatedElders),
    goals: parseJsonArray(item.goals),
    measures: parseJsonArray(item.measures),
    records: Array.isArray(item.records) ? item.records : [],
    startDate: formatDateTime(item.startDate),
    endDate: formatDateTime(item.endDate),
    createTime: formatDateTime(item.createTime),
  }
}

// ===== 加载数据 =====
async function loadData() {
  try {
    const res = await getInterventionList({ page: 1, size: 1000 })
    const data = res.data.data
    interventionList.value = (data.list || []).map(mapIntervention)
    updateStats()
  } catch {
    // 错误已由响应拦截器统一提示
  }
}

// ===== 更新统计数据 =====
function updateStats() {
  const list = interventionList.value
  stats.total = list.length
  stats.ongoing = list.filter((item) => item.status === 'ongoing').length
  stats.completed = list.filter((item) => item.status === 'completed').length
  stats.pendingReview = list.filter((item) => item.status === 'pending').length
  const rated = list.filter((item) => item.rating > 0)
  stats.avgRating =
    rated.length > 0
      ? parseFloat((rated.reduce((sum, item) => sum + item.rating, 0) / rated.length).toFixed(1))
      : 0
}

// ===== 筛选数据 =====
const filteredList = computed(() => {
  let list = [...interventionList.value]

  if (filterForm.name) {
    list = list.filter((item) => item.name.includes(filterForm.name))
  }
  if (filterForm.type) {
    list = list.filter((item) => item.type === filterForm.type)
  }
  if (filterForm.status) {
    list = list.filter((item) => item.status === filterForm.status)
  }
  if (filterForm.priority) {
    list = list.filter((item) => item.priority === filterForm.priority)
  }
  if (filterForm.elderName) {
    list = list.filter(
      (item) =>
        item.associatedElders &&
        item.associatedElders.some((elder) => elder.includes(filterForm.elderName)),
    )
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
function getStatusType(status) {
  const map = {
    draft: 'info',
    pending: 'warning',
    approved: 'primary',
    ongoing: 'success',
    completed: 'success',
    terminated: 'danger',
  }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = {
    draft: '草稿',
    pending: '待审核',
    approved: '已通过',
    ongoing: '执行中',
    completed: '已完成',
    terminated: '已终止',
  }
  return map[status] || status
}

function getPriorityType(priority) {
  const map = {
    urgent: 'danger',
    important: 'warning',
    normal: 'info',
  }
  return map[priority] || 'info'
}

function getPriorityText(priority) {
  const map = {
    urgent: '紧急',
    important: '重要',
    normal: '普通',
  }
  return map[priority] || priority
}

function getTypeColor(type) {
  const map = {
    运动康复: 'primary',
    营养干预: 'success',
    心理疏导: 'info',
    用药管理: 'warning',
    日常护理: 'info',
    综合干预: 'danger',
  }
  return map[type] || 'info'
}

function getProgressColor(progress) {
  if (progress >= 80) return '#67c23a'
  if (progress >= 50) return '#e6a23c'
  return '#409eff'
}

function getExecStatusType(status) {
  const map = {
    正常: 'success',
    部分执行: 'warning',
    未执行: 'info',
    异常: 'danger',
  }
  return map[status] || 'info'
}

// ===== 自动生成编号 =====
function generateCode() {
  const date = new Date()
  const dateStr =
    date.getFullYear() +
    String(date.getMonth() + 1).padStart(2, '0') +
    String(date.getDate()).padStart(2, '0')
  const existing = interventionList.value.filter((item) => item.code.startsWith(`IP${dateStr}`))
  const seq = String(existing.length + 1).padStart(3, '0')
  return `IP${dateStr}${seq}`
}

// ===== 操作函数 =====
async function handleView(item) {
  // 优先从详情接口获取最新数据，失败时兜底使用列表数据
  let base = item
  try {
    const res = await getInterventionById(item.id)
    if (res.data.data) {
      base = mapIntervention(res.data.data)
    }
  } catch {
    // 使用列表数据兜底
  }

  // 更新步骤状态
  const statusMap = {
    draft: 0,
    pending: 1,
    approved: 2,
    ongoing: 3,
    completed: 4,
    terminated: 5,
  }
  const stepIndex = statusMap[base.status] || 0

  // 更新步骤状态
  const steps = [
    { key: '1', title: '草稿', time: base.createTime, status: 'wait' },
    { key: '2', title: '待审核', time: '', status: 'wait' },
    { key: '3', title: '已通过', time: '', status: 'wait' },
    { key: '4', title: '执行中', time: '', status: 'wait' },
    { key: '5', title: '已完成', time: '', status: 'wait' },
    { key: '6', title: '已终止', time: '', status: 'wait' },
  ]

  // 设置已完成步骤
  for (let i = 0; i <= stepIndex && i < steps.length; i++) {
    steps[i].status = 'finish'
    if (i === stepIndex && base.status === 'ongoing') {
      steps[i].status = 'process'
    }
  }

  // 为步骤设置时间
  if (base.status === 'draft') {
    steps[0].time = base.createTime
  } else if (base.status === 'pending') {
    steps[0].time = base.createTime
    // 模拟审核提交时间
    steps[1].time = new Date(new Date(base.createTime).getTime() + 5 * 24 * 60 * 60 * 1000)
      .toISOString()
      .split('T')[0]
  } else if (base.status === 'approved') {
    steps[0].time = base.createTime
    steps[1].time = new Date(new Date(base.createTime).getTime() + 2 * 24 * 60 * 60 * 1000)
      .toISOString()
      .split('T')[0]
    steps[2].time = new Date(new Date(base.createTime).getTime() + 4 * 24 * 60 * 60 * 1000)
      .toISOString()
      .split('T')[0]
  } else if (base.status === 'ongoing' || base.status === 'completed') {
    steps[0].time = base.createTime
    steps[1].time = new Date(new Date(base.createTime).getTime() + 2 * 24 * 60 * 60 * 1000)
      .toISOString()
      .split('T')[0]
    steps[2].time = new Date(new Date(base.createTime).getTime() + 4 * 24 * 60 * 60 * 1000)
      .toISOString()
      .split('T')[0]
    steps[3].time = base.startDate
    if (base.status === 'completed') {
      steps[4].time = base.endDate
    }
  }

  const displayItem = {
    ...base,
    stepIndex: stepIndex,
    steps: steps,
  }

  detailData.value = displayItem
  detailVisible.value = true
}

async function handleRecords(item) {
  let records = Array.isArray(item.records) ? item.records : []
  try {
    const res = await getInterventionRecords(item.id)
    const data = res.data.data
    if (data && Array.isArray(data.list)) {
      records = data.list
    }
  } catch {
    // 使用列表携带的记录兜底
  }
  recordsData.value = {
    ...item,
    records: records.map((record) => ({
      ...record,
      date: formatDateTime(record.executeDate || record.date || record.createTime),
      note: record.note || record.remark || '',
    })),
  }
  recordsVisible.value = true
}

function handleAdd() {
  isEdit.value = false
  editItem.value = null
  formTitle.value = '新增方案'
  Object.assign(formData, {
    name: '',
    type: '',
    priority: 'normal',
    leader: '',
    targetGroup: '',
    totalWeeks: 4,
    frequency: '',
    associatedElders: [],
    goalsText: '',
    measuresText: '',
    notes: '',
  })
  formRef.value?.resetFields()
  formDialogVisible.value = true
}

function handleEdit(item) {
  isEdit.value = true
  editItem.value = item
  formTitle.value = '编辑方案'
  Object.assign(formData, {
    name: item.name,
    type: item.type,
    priority: item.priority,
    leader: item.leader,
    targetGroup: item.targetGroup,
    totalWeeks: item.totalWeeks,
    frequency: item.frequency,
    associatedElders: [...(item.associatedElders || [])],
    goalsText: item.goals ? item.goals.join('\n') : '',
    measuresText: item.measures ? item.measures.join('\n') : '',
    notes: item.notes || '',
  })
  formDialogVisible.value = true
}

function handleDelete(item) {
  ElMessageBox.confirm(`确定要删除方案 "${item.name}" 吗？此操作不可恢复！`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      if (item.id) {
        await deleteIntervention(item.id)
      }
      const index = interventionList.value.findIndex((i) => i.code === item.code)
      if (index > -1) {
        interventionList.value.splice(index, 1)
        ElMessage.success('删除成功')
        updateStats()
        if (currentPage.value > Math.ceil(total.value / pageSize.value)) {
          currentPage.value = 1
        }
      }
    })
    .catch(() => {})
}

function handleReview(item) {
  reviewData.value = item
  reviewForm.comment = ''
  reviewForm.result = 'approved'
  reviewDialogVisible.value = true
}

function handlePause(item) {
  ElMessageBox.confirm(`确定要暂停方案 "${item.name}" 的执行吗？`, '暂停确认', {
    confirmButtonText: '确定暂停',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      if (item.id) {
        await pauseIntervention(item.id)
      }
      const index = interventionList.value.findIndex((i) => i.code === item.code)
      if (index > -1) {
        interventionList.value[index].status = 'pending'
        ElMessage.success('方案已暂停')
        updateStats()
      }
    })
    .catch(() => {})
}

function handleTerminate(item) {
  ElMessageBox.confirm(`确定要终止方案 "${item.name}" 吗？此操作不可恢复！`, '终止确认', {
    confirmButtonText: '确定终止',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      if (item.id) {
        await terminateIntervention(item.id)
      }
      const index = interventionList.value.findIndex((i) => i.code === item.code)
      if (index > -1) {
        interventionList.value[index].status = 'terminated'
        ElMessage.success('方案已终止')
        updateStats()
      }
    })
    .catch(() => {})
}

function handleComplete(item) {
  ElMessageBox.confirm(`确定要将方案 "${item.name}" 标记为已完成吗？`, '完成确认', {
    confirmButtonText: '确定完成',
    cancelButtonText: '取消',
    type: 'success',
  })
    .then(async () => {
      if (item.id) {
        await completeIntervention(item.id)
      }
      const index = interventionList.value.findIndex((i) => i.code === item.code)
      if (index > -1) {
        interventionList.value[index].status = 'completed'
        interventionList.value[index].progress = 100
        ElMessage.success('方案已完成')
        updateStats()
      }
    })
    .catch(() => {})
}

function handleAddExecRecord(item) {
  execTarget.value = item
  Object.assign(execForm, {
    elderName:
      item.associatedElders && item.associatedElders.length > 0 ? item.associatedElders[0] : '',
    executor: '',
    status: '正常',
    rating: 4,
    note: '',
  })
  execFormRef.value?.resetFields()
  execDialogVisible.value = true
}

function handleExportReport(item) {
  ElMessage.success(`正在导出 "${item.name}" 报告...`)
}

function handleExport() {
  ElMessage.success('导出成功')
}

function resetFilter() {
  filterForm.name = ''
  filterForm.type = ''
  filterForm.status = ''
  filterForm.priority = ''
  filterForm.elderName = ''
  currentPage.value = 1
}

async function refreshData() {
  await loadData()
  ElMessage.success('数据已刷新')
}

// ===== 提交表单 =====
async function submitForm(targetStatus) {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  // 处理目标列表
  const goals = formData.goalsText.split('\n').filter((line) => line.trim())
  const measures = formData.measuresText.split('\n').filter((line) => line.trim())

  if (goals.length === 0) {
    ElMessage.warning('请至少输入一个干预目标')
    return
  }
  if (measures.length === 0) {
    ElMessage.warning('请至少输入一个具体措施')
    return
  }

  const baseData = {
    name: formData.name,
    type: formData.type,
    priority: formData.priority,
    leader: formData.leader,
    targetGroup: formData.targetGroup,
    totalWeeks: formData.totalWeeks,
    currentWeek: 0,
    frequency: formData.frequency,
    associatedElders: [...formData.associatedElders],
    goals: goals,
    measures: measures,
    notes: formData.notes,
    rating: 0,
    avgExecRating: 0,
    progress: 0,
    executedCount: 0,
    totalCount: 0,
    abnormalCount: 0,
    startDate: '',
    endDate: '',
    records: [],
  }

  try {
    if (isEdit.value && editItem.value) {
      if (editItem.value.id) {
        await updateIntervention(editItem.value.id, {
          ...baseData,
          status: targetStatus === 'pending' ? 'pending' : 'draft',
        })
      }
      const index = interventionList.value.findIndex((i) => i.code === editItem.value.code)
      if (index > -1) {
        const updated = {
          ...interventionList.value[index],
          ...baseData,
          code: editItem.value.code,
          createTime: interventionList.value[index].createTime,
        }
        if (targetStatus === 'pending') {
          updated.status = 'pending'
        } else {
          updated.status = 'draft'
        }
        interventionList.value[index] = updated
        ElMessage.success('更新成功')
      }
    } else {
      // 后端未提供新增接口，维持本地创建
      const code = generateCode()
      const newItem = {
        ...baseData,
        code: code,
        status: targetStatus === 'pending' ? 'pending' : 'draft',
        createTime: new Date().toLocaleString('zh-CN'),
        startDate: '',
        endDate: '',
        totalCount: 0,
      }
      interventionList.value.unshift(newItem)
      ElMessage.success('方案创建成功')
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

// ===== 提交审核 =====
async function submitReview() {
  if (!reviewForm.comment) {
    ElMessage.warning('请输入审核意见')
    return
  }

  try {
    const target = reviewData.value
    if (target && target.id) {
      await reviewIntervention(target.id, {
        result: reviewForm.result,
        comment: reviewForm.comment,
      })
    }

    const index = interventionList.value.findIndex((i) => i.code === target.code)
    if (index > -1) {
      if (reviewForm.result === 'approved') {
        const today = new Date().toISOString().split('T')[0]
        const endDate = new Date()
        endDate.setDate(endDate.getDate() + interventionList.value[index].totalWeeks * 7)
        interventionList.value[index].status = 'approved'
        interventionList.value[index].startDate = today
        interventionList.value[index].endDate = endDate.toISOString().split('T')[0]
        ElMessage.success('审核通过，方案已批准执行')
      } else {
        interventionList.value[index].status = 'draft'
        ElMessage.warning('审核驳回，方案已退回草稿状态')
      }
    }
    reviewDialogVisible.value = false
    updateStats()
  } catch {
    // 错误已由响应拦截器统一提示
  }
}

// ===== 提交执行记录 =====
async function submitExec() {
  const valid = await execFormRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    const target = execTarget.value
    if (target && target.id) {
      await addInterventionRecord({
        interventionId: target.id,
        elderName: execForm.elderName,
        executor: execForm.executor,
        status: execForm.status,
        rating: execForm.rating,
        note: execForm.note,
      })
    }

    const index = interventionList.value.findIndex((i) => i.code === target.code)
    if (index > -1) {
      const record = {
        date: new Date().toISOString().split('T')[0],
        elderName: execForm.elderName,
        executor: execForm.executor,
        status: execForm.status,
        rating: execForm.rating,
        note: execForm.note,
        attachments: [],
      }

      if (!interventionList.value[index].records) {
        interventionList.value[index].records = []
      }
      interventionList.value[index].records.unshift(record)

      // 更新统计数据
      const records = interventionList.value[index].records
      const total = records.length
      const completed = records.filter((r) => r.status === '正常').length
      const progress = Math.min(
        Math.round((completed / (interventionList.value[index].totalWeeks * 2)) * 100),
        100,
      )
      const avgRating = records.reduce((sum, r) => sum + r.rating, 0) / total

      interventionList.value[index].executedCount = total
      interventionList.value[index].totalCount = interventionList.value[index].totalWeeks * 2
      interventionList.value[index].progress = Math.min(progress, 100)
      interventionList.value[index].avgExecRating = parseFloat(avgRating.toFixed(1))

      // 如果进度达到100%，自动标记为已完成
      if (
        interventionList.value[index].progress >= 100 &&
        interventionList.value[index].status === 'ongoing'
      ) {
        interventionList.value[index].status = 'completed'
      }

      ElMessage.success('执行记录添加成功')
      execDialogVisible.value = false
      updateStats()
    }
  } catch {
    // 错误已由响应拦截器统一提示
  }
}

// ===== 生命周期 =====
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.intervention-page {
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
  grid-template-columns: repeat(5, 1fr);
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
.stat-card.ongoing .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
}
.stat-card.completed .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
}
.stat-card.pending .stat-icon {
  background: #fdf6ec;
  color: #e6a23c;
}
.stat-card.rating .stat-icon {
  background: #fdf6ec;
  color: #e6a23c;
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

/* ===== 干预卡片 ===== */
.card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.intervention-card {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 14px 18px;
  border-left: 4px solid #999;
  transition: all 0.3s;
}

.intervention-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transform: translateX(4px);
}

.intervention-card.draft {
  border-left-color: #909399;
}
.intervention-card.pending {
  border-left-color: #e6a23c;
}
.intervention-card.approved {
  border-left-color: #409eff;
}
.intervention-card.ongoing {
  border-left-color: #67c23a;
}
.intervention-card.completed {
  border-left-color: #67c23a;
}
.intervention-card.terminated {
  border-left-color: #f56c6c;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.card-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.card-rating {
  display: inline-flex;
  align-items: center;
}

.card-code {
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
  font-family: monospace;
}

.card-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.card-right {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.card-week {
  font-size: 13px;
  color: #999;
}

.card-middle {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px 0;
}

.card-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.card-label {
  font-size: 13px;
  color: #999;
}

.card-value {
  font-size: 13px;
  color: #333;
}

.card-bottom {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.card-bottom .el-button {
  font-size: 12px;
  padding: 4px 8px;
}

.card-bottom .el-button .el-icon {
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
  margin-bottom: 12px;
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-code {
  font-size: 16px;
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
  display: flex;
  align-items: center;
  gap: 4px;
}

.rating-label {
  font-size: 14px;
  color: #666;
}

.detail-steps {
  margin: 8px 0 4px 0;
}

.steps-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 24px;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.section-title {
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

.detail-elders {
  margin-top: 4px;
}

.elder-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.detail-goals,
.detail-measures,
.detail-notes {
  margin-top: 4px;
}

.goal-list,
.measure-list {
  margin: 4px 0 0 20px;
  padding: 0;
  font-size: 14px;
  color: #333;
  line-height: 1.8;
}

.goal-list li,
.measure-list li {
  list-style-type: decimal;
}

.notes-content {
  font-size: 14px;
  color: #666;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.detail-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

/* ===== 执行记录弹框 ===== */
.records-content {
  padding: 4px 0;
}

.records-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.records-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.records-summary {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.remark-text {
  display: block;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #666;
  font-size: 13px;
}

/* ===== 审核弹框 ===== */
.review-content {
  padding: 4px 0;
}

.review-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px 16px;
}

.review-row {
  display: flex;
  flex-direction: column;
  padding: 4px 0;
}

.review-label {
  font-size: 12px;
  color: #999;
}

.review-value {
  font-size: 14px;
  color: #333;
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
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

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .card-top {
    flex-direction: column;
    align-items: flex-start;
  }

  .intervention-page {
    padding: 12px;
  }

  .header-actions {
    flex-wrap: wrap;
  }

  .records-summary {
    flex-wrap: wrap;
    gap: 8px;
  }

  .review-info {
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

  .card-left {
    flex-wrap: wrap;
  }
  .card-right {
    flex-wrap: wrap;
  }
  .card-bottom {
    flex-wrap: wrap;
  }
}
</style>