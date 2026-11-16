<template>
  <div class="expert-page">
    <!-- ===== 页面标题 ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Headset /></el-icon>
        专家康养咨询管理
      </h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddExpert">
          <el-icon><Plus /></el-icon>
          添加专家
        </el-button>
        <el-button type="primary" plain @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="success" plain>
          <el-icon><Download /></el-icon>
          导出报告
        </el-button>
      </div>
    </div>

    <!-- ===== 统计卡片 ===== -->
    <div class="stats-grid">
      <div class="stat-card total">
        <div class="stat-icon">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.totalExperts }}</div>
          <div class="stat-label">专家总数</div>
        </div>
      </div>
      <div class="stat-card today">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.todayConsultations }}</div>
          <div class="stat-label">今日咨询</div>
        </div>
      </div>
      <div class="stat-card pending">
        <div class="stat-icon">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.pendingAssign }}</div>
          <div class="stat-label">待分配咨询</div>
        </div>
      </div>
      <div class="stat-card ongoing">
        <div class="stat-icon">
          <el-icon><Timer /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.ongoingConsultations }}</div>
          <div class="stat-label">进行中咨询</div>
        </div>
      </div>
      <div class="stat-card satisfaction">
        <div class="stat-icon">
          <el-icon><StarFilled /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.avgSatisfaction }}</div>
          <div class="stat-label">平均满意度</div>
        </div>
      </div>
    </div>

    <!-- ===== 图表区域 ===== -->
    <div class="chart-section">
      <div class="chart-grid">
        <div class="chart-box">
          <div class="chart-header">
            <span class="chart-title">咨询趋势</span>
          </div>
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
        <div class="chart-box">
          <div class="chart-header">
            <span class="chart-title">咨询类型分布</span>
          </div>
          <div ref="pieChartRef" class="chart-container"></div>
        </div>
      </div>
    </div>

    <!-- ===== Tab切换 ===== -->
    <div class="tab-section">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="专家管理" name="experts" />
        <el-tab-pane label="咨询记录" name="consultations" />
        <el-tab-pane label="统计分析" name="statistics" />
      </el-tabs>

      <!-- ===== Tab1: 专家管理 ===== -->
      <div v-show="activeTab === 'experts'">
        <div class="filter-section">
          <div class="filter-header">
            <div class="filter-title">
              <el-icon><Filter /></el-icon>
              <span>筛选条件</span>
            </div>
            <div class="filter-actions">
              <el-button type="primary" plain @click="resetExpertFilter">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </div>
          </div>
          <div class="filter-body">
            <el-form :model="expertFilter" label-width="80px" inline>
              <el-form-item label="专家姓名">
                <el-input
                  v-model="expertFilter.name"
                  placeholder="请输入姓名"
                  clearable
                  style="width: 150px"
                  prefix-icon="Search"
                />
              </el-form-item>
              <el-form-item label="职称">
                <el-select
                  v-model="expertFilter.title"
                  placeholder="全部"
                  clearable
                  style="width: 130px"
                >
                  <el-option
                    v-for="item in titleOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select
                  v-model="expertFilter.status"
                  placeholder="全部"
                  clearable
                  style="width: 120px"
                >
                  <el-option
                    v-for="item in expertStatusOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <div class="table-section">
          <div class="table-header">
            <div class="table-title">
              <el-icon><List /></el-icon>
              <span>专家列表</span>
              <el-tag size="small" type="info">共 {{ expertTotal }} 条</el-tag>
            </div>
          </div>

          <el-table :data="paginatedExperts" stripe style="width: 100%" row-key="id" border>
            <el-table-column prop="name" label="专家姓名" width="90" align="center" />
            <el-table-column prop="title" label="职称" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="getTitleType(row.title)" size="small">
                  {{ row.title }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="specialty" label="专业领域" width="140" align="center" />
            <el-table-column prop="institution" label="所属机构" width="150" align="center" />
            <el-table-column prop="phone" label="联系电话" width="120" align="center" />
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="getExpertStatusType(row.status)" size="small">
                  {{ getExpertStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="todayCount" label="今日咨询" width="90" align="center" />
            <el-table-column prop="totalCount" label="累计咨询" width="90" align="center" />
            <el-table-column prop="rating" label="满意度" width="90" align="center">
              <template #default="{ row }">
                <span style="color: #e6a23c">★ {{ row.rating }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right" align="center">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button size="small" type="primary" link @click="handleViewExpert(row)">
                    <el-icon><View /></el-icon>
                    详情
                  </el-button>
                  <el-button size="small" type="warning" link @click="handleEditExpert(row)">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button size="small" type="info" link @click="handleSchedule(row)">
                    <el-icon><Calendar /></el-icon>
                    排班
                  </el-button>
                  <el-button size="small" type="danger" link @click="handleDeleteExpert(row)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              background
              v-model:current-page="expertPage"
              v-model:page-size="expertPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="expertTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="expertPage = 1"
            />
          </div>
        </div>
      </div>

      <!-- ===== Tab2: 咨询记录 ===== -->
      <div v-show="activeTab === 'consultations'">
        <div class="filter-section">
          <div class="filter-header">
            <div class="filter-title">
              <el-icon><Filter /></el-icon>
              <span>筛选条件</span>
            </div>
            <div class="filter-actions">
              <el-button type="primary" plain @click="resetConsultFilter">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </div>
          </div>
          <div class="filter-body">
            <el-form :model="consultFilter" label-width="80px" inline>
              <el-form-item label="老人姓名">
                <el-input
                  v-model="consultFilter.elderName"
                  placeholder="请输入姓名"
                  clearable
                  style="width: 150px"
                  prefix-icon="Search"
                />
              </el-form-item>
              <el-form-item label="咨询类型">
                <el-select
                  v-model="consultFilter.type"
                  placeholder="全部"
                  clearable
                  style="width: 130px"
                >
                  <el-option
                    v-for="item in consultTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select
                  v-model="consultFilter.status"
                  placeholder="全部"
                  clearable
                  style="width: 120px"
                >
                  <el-option
                    v-for="item in consultStatusOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="紧急程度">
                <el-select
                  v-model="consultFilter.urgency"
                  placeholder="全部"
                  clearable
                  style="width: 120px"
                >
                  <el-option
                    v-for="item in urgencyOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="日期范围">
                <el-date-picker
                  v-model="consultFilter.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始"
                  end-placeholder="结束"
                  value-format="YYYY-MM-DD"
                  style="width: 260px"
                />
              </el-form-item>
            </el-form>
          </div>
        </div>

        <div class="table-section">
          <div class="table-header">
            <div class="table-title">
              <el-icon><List /></el-icon>
              <span>咨询记录</span>
              <el-tag size="small" type="info">共 {{ consultTotal }} 条</el-tag>
            </div>
            <div class="table-actions">
              <el-button type="primary" plain size="small" @click="handleAddConsult">
                <el-icon><Plus /></el-icon>
                新建咨询
              </el-button>
            </div>
          </div>

          <el-table :data="paginatedConsults" stripe style="width: 100%" row-key="id" border>
            <el-table-column prop="elderName" label="老人姓名" width="90" align="center" />
            <el-table-column prop="type" label="咨询类型" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="getConsultTypeColor(row.type)" size="small">
                  {{ row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="expertName" label="专家" width="90" align="center">
              <template #default="{ row }">
                {{ row.expertName || '未分配' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getConsultStatusType(row.status)" size="small">
                  {{ getConsultStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="urgency" label="紧急程度" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getUrgencyType(row.urgency)" size="small">
                  {{ getUrgencyText(row.urgency) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="咨询时间" width="160" align="center" />
            <el-table-column prop="rating" label="满意度" width="90" align="center">
              <template #default="{ row }">
                {{ row.rating ? '★ ' + row.rating : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="300" fixed="right" align="center">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button size="small" type="primary" link @click="handleViewConsult(row)">
                    <el-icon><View /></el-icon>
                    详情
                  </el-button>
                  <el-button
                    v-if="row.status === 'pending' || row.status === 'ongoing'"
                    size="small"
                    type="success"
                    link
                    @click="handleAssignExpert(row)"
                  >
                    <el-icon><User /></el-icon>
                    分配专家
                  </el-button>
                  <el-button
                    v-if="row.status === 'ongoing'"
                    size="small"
                    type="warning"
                    link
                    @click="handleCompleteConsult(row)"
                  >
                    <el-icon><Check /></el-icon>
                    完成
                  </el-button>
                  <el-button size="small" type="danger" link @click="handleDeleteConsult(row)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="consultPage"
              v-model:page-size="consultPageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="consultTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="consultPage = 1"
            />
          </div>
        </div>
      </div>

      <!-- ===== Tab3: 统计分析 ===== -->
      <div v-show="activeTab === 'statistics'">
        <div class="stats-analysis">
          <div class="analysis-grid">
            <div class="analysis-card">
              <div class="analysis-title">📊 专家工作量排行</div>
              <div class="analysis-list">
                <div v-for="(item, index) in expertWorkload" :key="index" class="analysis-item">
                  <span class="rank" :class="{ top: index < 3 }">
                    {{ index + 1 }}
                  </span>
                  <span class="name">{{ item.name }}</span>
                  <span class="count">{{ item.count }} 次</span>
                  <el-progress
                    :percentage="item.percentage"
                    :color="index < 3 ? '#67c23a' : '#409eff'"
                    :stroke-width="8"
                  />
                </div>
              </div>
            </div>
            <div class="analysis-card">
              <div class="analysis-title">📈 咨询类型分布</div>
              <div ref="statPieChartRef" class="stat-chart"></div>
            </div>
            <div class="analysis-card">
              <div class="analysis-title">⭐ 满意度分布</div>
              <div class="satisfaction-stats">
                <div v-for="item in satisfactionData" :key="item.rating" class="satisfaction-item">
                  <span class="label">{{ item.rating }} 星</span>
                  <el-progress
                    :percentage="item.percentage"
                    :color="getSatisfactionColor(item.rating)"
                    :stroke-width="10"
                  />
                  <span class="count">{{ item.count }} 人</span>
                </div>
              </div>
            </div>
            <div class="analysis-card">
              <div class="analysis-title">📋 月度统计</div>
              <div class="monthly-stats">
                <div class="monthly-item">
                  <span>本月总咨询</span>
                  <span class="value">{{ monthlyStats.total }}</span>
                </div>
                <div class="monthly-item">
                  <span>已完成</span>
                  <span class="value success">{{ monthlyStats.completed }}</span>
                </div>
                <div class="monthly-item">
                  <span>进行中</span>
                  <span class="value warning">{{ monthlyStats.ongoing }}</span>
                </div>
                <div class="monthly-item">
                  <span>待分配</span>
                  <span class="value danger">{{ monthlyStats.pending }}</span>
                </div>
                <div class="monthly-item">
                  <span>平均满意度</span>
                  <span class="value primary">{{ monthlyStats.avgRating }} ★</span>
                </div>
                <div class="monthly-item">
                  <span>专家活跃数</span>
                  <span class="value success">{{ monthlyStats.activeExperts }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 添加/编辑专家弹框 ===== -->
    <el-dialog
      v-model="expertDialogVisible"
      :title="expertDialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="expertFormRef"
        :model="expertFormData"
        :rules="expertFormRules"
        label-width="100px"
      >
        <el-form-item label="专家姓名" prop="name">
          <el-input v-model="expertFormData.name" placeholder="请输入专家姓名" />
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-select v-model="expertFormData.title" placeholder="请选择职称" style="width: 100%">
            <el-option
              v-for="item in titleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="专业领域" prop="specialty">
          <el-input v-model="expertFormData.specialty" placeholder="请输入专业领域" />
        </el-form-item>
        <el-form-item label="所属机构">
          <el-input v-model="expertFormData.institution" placeholder="请输入所属机构" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="expertFormData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="expertFormData.status" placeholder="请选择状态" style="width: 100%">
            <el-option
              v-for="item in expertStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="expertDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitExpert">确认</el-button>
      </template>
    </el-dialog>

    <!-- ===== 专家排班弹框 ===== -->
    <el-dialog
      v-model="scheduleDialogVisible"
      title="专家排班"
      width="520px"
      :close-on-click-modal="false"
    >
      <div v-if="scheduleExpert" class="schedule-content">
        <div class="schedule-expert">
          <strong>{{ scheduleExpert.name }}</strong>
          <span style="color: #999">{{ scheduleExpert.title }}</span>
        </div>
        <el-divider />
        <el-form label-width="80px">
          <el-form-item label="排班日期">
            <el-date-picker
              v-model="scheduleData.dates"
              type="dates"
              placeholder="选择排班日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="工作时间">
            <div style="display: flex; gap: 10px; align-items: center">
              <el-time-select
                v-model="scheduleData.startTime"
                :start="'08:00'"
                :step="'00:30'"
                :end="'18:00'"
                placeholder="开始"
                style="width: 120px"
              />
              <span>至</span>
              <el-time-select
                v-model="scheduleData.endTime"
                :start="'08:00'"
                :step="'00:30'"
                :end="'18:00'"
                placeholder="结束"
                style="width: 120px"
              />
            </div>
          </el-form-item>
          <el-form-item label="时段类型">
            <el-select v-model="scheduleData.shift" placeholder="选择时段" style="width: 100%">
              <el-option label="上午班 (08:00-12:00)" value="morning" />
              <el-option label="下午班 (13:00-17:00)" value="afternoon" />
              <el-option label="全天班 (08:00-17:00)" value="full" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="scheduleData.remark" placeholder="请输入备注" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSchedule">保存排班</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看专家详情弹框 ===== -->
    <el-dialog v-model="expertDetailVisible" title="专家详情" width="560px">
      <div v-if="expertDetailData" class="detail-content">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">姓名</span>
            <span class="detail-value">{{ expertDetailData.name }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">职称</span>
            <el-tag :type="getTitleType(expertDetailData.title)" size="small">
              {{ expertDetailData.title }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">专业领域</span>
            <span class="detail-value">{{ expertDetailData.specialty }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">所属机构</span>
            <span class="detail-value">{{ expertDetailData.institution }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">联系电话</span>
            <span class="detail-value">{{ expertDetailData.phone }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态</span>
            <el-tag :type="getExpertStatusType(expertDetailData.status)" size="small">
              {{ getExpertStatusText(expertDetailData.status) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">今日咨询</span>
            <span class="detail-value">{{ expertDetailData.todayCount }} 次</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">累计咨询</span>
            <span class="detail-value">{{ expertDetailData.totalCount }} 次</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">满意度</span>
            <span class="detail-value" style="color: #e6a23c">★ {{ expertDetailData.rating }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">排班信息</span>
            <span class="detail-value">{{ expertDetailData.schedule || '未设置' }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="expertDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看咨询详情弹框 ===== -->
    <el-dialog v-model="consultDetailVisible" title="咨询详情" width="580px">
      <div v-if="consultDetailData" class="detail-content">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">老人姓名</span>
            <span class="detail-value">{{ consultDetailData.elderName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">咨询类型</span>
            <el-tag :type="getConsultTypeColor(consultDetailData.type)" size="small">
              {{ consultDetailData.type }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">专家</span>
            <span class="detail-value">{{ consultDetailData.expertName || '未分配' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态</span>
            <el-tag :type="getConsultStatusType(consultDetailData.status)" size="small">
              {{ getConsultStatusText(consultDetailData.status) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">紧急程度</span>
            <el-tag :type="getUrgencyType(consultDetailData.urgency)" size="small">
              {{ getUrgencyText(consultDetailData.urgency) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">咨询时间</span>
            <span class="detail-value">{{ consultDetailData.time }}</span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">咨询内容</span>
            <span class="detail-value">{{ consultDetailData.content }}</span>
          </div>
          <div v-if="consultDetailData.reply" class="detail-item full-width">
            <span class="detail-label">专家回复</span>
            <span class="detail-value" style="color: #409eff">{{ consultDetailData.reply }}</span>
          </div>
          <div v-if="consultDetailData.rating" class="detail-item">
            <span class="detail-label">满意度</span>
            <span class="detail-value" style="color: #e6a23c"
              >★ {{ consultDetailData.rating }}</span
            >
          </div>
          <div v-if="consultDetailData.feedback" class="detail-item full-width">
            <span class="detail-label">反馈意见</span>
            <span class="detail-value">{{ consultDetailData.feedback }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="consultDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 分配专家弹框 ===== -->
    <el-dialog
      v-model="assignDialogVisible"
      title="分配专家"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item label="咨询内容">
          <span style="font-size: 14px; color: #666">{{ assignConsult?.content }}</span>
        </el-form-item>
        <el-form-item label="选择专家" required>
          <el-select v-model="assignExpertId" placeholder="请选择专家" style="width: 100%">
            <el-option
              v-for="item in availableExperts"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">确认分配</el-button>
      </template>
    </el-dialog>

    <!-- ===== 完成咨询弹框 ===== -->
    <el-dialog
      v-model="completeDialogVisible"
      title="完成咨询"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px">
        <el-form-item label="专家回复" required>
          <el-input
            v-model="completeForm.reply"
            type="textarea"
            :rows="3"
            placeholder="请输入专家回复内容..."
          />
        </el-form-item>
        <el-form-item label="满意度评价">
          <el-rate
            v-model="completeForm.rating"
            :texts="['很差', '较差', '一般', '满意', '非常满意']"
            show-text
            style="display: flex; align-items: center"
          />
        </el-form-item>
        <el-form-item label="反馈意见">
          <el-input
            v-model="completeForm.feedback"
            type="textarea"
            :rows="2"
            placeholder="请输入反馈意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete">确认完成</el-button>
      </template>
    </el-dialog>

    <!-- ===== 新建咨询弹框 ===== -->
    <el-dialog
      v-model="addConsultDialogVisible"
      title="新建咨询"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="consultFormRef"
        :model="consultFormData"
        :rules="consultFormRules"
        label-width="100px"
      >
        <el-form-item label="老人姓名" prop="elderName">
          <el-input v-model="consultFormData.elderName" placeholder="请输入老人姓名" />
        </el-form-item>
        <el-form-item label="咨询类型" prop="type">
          <el-select
            v-model="consultFormData.type"
            placeholder="请选择咨询类型"
            style="width: 100%"
          >
            <el-option
              v-for="item in consultTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度" prop="urgency">
          <el-select
            v-model="consultFormData.urgency"
            placeholder="请选择紧急程度"
            style="width: 100%"
          >
            <el-option
              v-for="item in urgencyOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="咨询内容" prop="content">
          <el-input
            v-model="consultFormData.content"
            type="textarea"
            :rows="3"
            placeholder="请输入咨询内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addConsultDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConsult">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, markRaw, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  Headset,
  Refresh,
  Plus,
  View,
  Edit,
  Delete,
  Filter,
  List,
  Download,
  User,
  Clock,
  Bell,
  Timer,
  StarFilled,
  Calendar,
  Check,
} from '@element-plus/icons-vue'
import {
  getExpertList,
  getExpertById,
  addExpert,
  updateExpert,
  deleteExpert,
  updateExpertSchedule,
} from '@/api/expert'
import {
  getConsultList,
  addConsult as addConsultApi,
  assignConsult as assignConsultApi,
  completeConsult as completeConsultApi,
  deleteConsult as deleteConsultApi,
} from '@/api/consult'

// ===== 统计数据 =====
const stats = reactive({
  totalExperts: 0,
  todayConsultations: 0,
  pendingAssign: 0,
  ongoingConsultations: 0,
  avgSatisfaction: 0,
})

// ===== 数据列表 =====
const expertList = ref([])
const consultList = ref([])

// ===== 图表引用 =====
const trendChartRef = ref(null)
const pieChartRef = ref(null)
const statPieChartRef = ref(null)
let trendChartInstance = null
let pieChartInstance = null
let statPieChartInstance = null

// ===== 职称枚举映射（后端 code/文本 → 页面中文） =====
const TITLE_TEXT_MAP = {
  chief_physician: '主任医师',
  deputy_chief: '副主任医师',
  attendee: '主治医师',
  rehab_therapist: '康复师',
}

// ===== 专家状态映射（后端 code → 页面内部 code） =====
const STATUS_TEXT_MAP = {
  ACTIVE: 'online',
  BUSY: 'busy',
  OFFLINE: 'offline',
  LEAVE: 'leave',
}

// ===== 专家状态映射（页面内部 code → 后端 code） =====
const STATUS_CODE_MAP = {
  online: 'ACTIVE',
  busy: 'BUSY',
  offline: 'OFFLINE',
  leave: 'LEAVE',
}

// ===== 排班 JSON（如 {"mon":"AM"}）格式化为可读文本 =====
function formatSchedule(s) {
  if (!s) return s
  if (typeof s === 'string' && s.trim().startsWith('{')) {
    try {
      const obj = JSON.parse(s)
      const dayMap = {
        mon: '周一',
        tue: '周二',
        wed: '周三',
        thu: '周四',
        fri: '周五',
        sat: '周六',
        sun: '周日',
      }
      const slotMap = { AM: '上午', PM: '下午', FULL: '全天' }
      const parts = Object.keys(obj).map((k) => `${dayMap[k] || k}(${slotMap[obj[k]] || obj[k]})`)
      if (parts.length > 0) return parts.join(' ')
    } catch (e) {
      // 非 JSON 排班直接原样展示
    }
  }
  return s
}

// ===== 加载咨询记录（从后端 /consult 接口） =====
async function loadConsultList() {
  try {
    const consultRes = await getConsultList({ page: 1, size: 10000 })
    consultList.value = (consultRes.data.data?.list || []).map((item: any) => ({
      ...item,
      time: (item.consultTime || '').replace('T', ' '),
    }))
  } catch (e) {
    consultList.value = []
  }
}

// ===== 初始化数据（专家列表 + 咨询记录从后端加载） =====
async function initData() {
  try {
    const res = await getExpertList({ page: 1, size: 10000 })
    const data = res.data.data
    expertList.value = (data?.list || []).map((item) => ({
      ...item,
      title: TITLE_TEXT_MAP[item.titleText || item.title] || item.titleText || item.title,
      status: STATUS_TEXT_MAP[item.status] || item.status,
      schedule: formatSchedule(item.schedule),
    }))
    await loadConsultList()
    updateStats()
    nextTick(() => {
      setTimeout(initCharts, 400)
    })
  } catch (e) {
    // 错误提示已由响应拦截器统一处理
  }
}

// ===== 更新统计数据 =====
function updateStats() {
  const experts = expertList.value
  const consults = consultList.value

  stats.totalExperts = experts.length

  const today = new Date().toISOString().split('T')[0]
  stats.todayConsultations = consults.filter((c) => c.time.startsWith(today)).length

  stats.pendingAssign = consults.filter((c) => c.status === 'pending').length
  stats.ongoingConsultations = consults.filter((c) => c.status === 'ongoing').length

  const completed = consults.filter((c) => c.status === 'completed' && c.rating)
  if (completed.length > 0) {
    const totalRating = completed.reduce((sum, c) => sum + c.rating, 0)
    stats.avgSatisfaction = parseFloat((totalRating / completed.length).toFixed(1))
  } else {
    stats.avgSatisfaction = 0
  }
}

// ===== 图表初始化 =====
function initCharts() {
  initTrendChart()
  initPieChart()
  initStatPieChart()
}

function initTrendChart() {
  if (!trendChartRef.value) return
  if (trendChartInstance) {
    trendChartInstance.dispose()
    trendChartInstance = null
  }

  // 近7天咨询量（根据咨询记录统计）
  const days: string[] = []
  const data: number[] = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    days.push(`${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`)
    const dayStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    data.push(consultList.value.filter((item: any) => (item.time || '').startsWith(dayStr)).length)
  }

  trendChartInstance = markRaw(echarts.init(trendChartRef.value))
  trendChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: days,
      axisLabel: { fontSize: 12 },
    },
    yAxis: {
      type: 'value',
      name: '咨询量',
      min: 0,
      max: 12,
    },
    series: [
      {
        type: 'line',
        data: data,
        smooth: true,
        lineStyle: { color: '#409eff', width: 3 },
        itemStyle: { color: '#409eff' },
        areaStyle: { color: 'rgba(64,158,255,0.15)' },
      },
    ],
  })

  const resizeHandler = () => trendChartInstance?.resize()
  window.removeEventListener('resize', resizeHandler)
  window.addEventListener('resize', resizeHandler)
}

function initPieChart() {
  if (!pieChartRef.value) return
  if (pieChartInstance) {
    pieChartInstance.dispose()
    pieChartInstance = null
  }

  const typeCount = {}
  consultList.value.forEach((item) => {
    typeCount[item.type] = (typeCount[item.type] || 0) + 1
  })

  const colors = ['#409eff', '#67c23a', '#e6a23c']
  const data = Object.keys(typeCount).map((key, index) => ({
    name: key,
    value: typeCount[key],
    itemStyle: { color: colors[index % colors.length] },
  }))

  pieChartInstance = markRaw(echarts.init(pieChartRef.value))
  pieChartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}次 ({d}%)' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        label: { show: true, formatter: '{b}\n{d}%', fontSize: 12 },
        data: data,
      },
    ],
  })

  const resizeHandler = () => pieChartInstance?.resize()
  window.removeEventListener('resize', resizeHandler)
  window.addEventListener('resize', resizeHandler)
}

function initStatPieChart() {
  if (!statPieChartRef.value) return
  if (statPieChartInstance) {
    statPieChartInstance.dispose()
    statPieChartInstance = null
  }

  const typeCount = {}
  consultList.value.forEach((item) => {
    typeCount[item.type] = (typeCount[item.type] || 0) + 1
  })

  const colors = ['#409eff', '#67c23a', '#e6a23c']
  const data = Object.keys(typeCount).map((key, index) => ({
    name: key,
    value: typeCount[key],
    itemStyle: { color: colors[index % colors.length] },
  }))

  statPieChartInstance = markRaw(echarts.init(statPieChartRef.value))
  statPieChartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}次 ({d}%)' },
    series: [
      {
        type: 'pie',
        radius: ['35%', '65%'],
        label: { show: true, formatter: '{b}\n{d}%', fontSize: 11 },
        data: data,
      },
    ],
  })

  const resizeHandler = () => statPieChartInstance?.resize()
  window.removeEventListener('resize', resizeHandler)
  window.addEventListener('resize', resizeHandler)
}

// ===== Tab切换 =====
const activeTab = ref('experts')

function handleTabChange(tab) {
  setTimeout(() => {
    if (tab === 'statistics') {
      initStatPieChart()
    }
  }, 300)
}

// ===== 选项数据 =====
const titleOptions = [
  { label: '全部', value: '' },
  { label: '主任医师', value: '主任医师' },
  { label: '副主任医师', value: '副主任医师' },
  { label: '主治医师', value: '主治医师' },
  { label: '康复师', value: '康复师' },
]

const expertStatusOptions = [
  { label: '全部', value: '' },
  { label: '在线', value: 'online' },
  { label: '忙碌', value: 'busy' },
  { label: '离线', value: 'offline' },
  { label: '休假', value: 'leave' },
]

const consultTypeOptions = [
  { label: '全部', value: '' },
  { label: '健康咨询', value: '健康咨询' },
  { label: '用药指导', value: '用药指导' },
  { label: '康复建议', value: '康复建议' },
]

const consultStatusOptions = [
  { label: '全部', value: '' },
  { label: '待分配', value: 'pending' },
  { label: '进行中', value: 'ongoing' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
]

const urgencyOptions = [
  { label: '全部', value: '' },
  { label: '普通', value: 'normal' },
  { label: '重要', value: 'important' },
  { label: '紧急', value: 'urgent' },
]

// ===== 专家管理筛选 =====
const expertFilter = reactive({
  name: '',
  title: '',
  status: '',
})

const expertPage = ref(1)
const expertPageSize = ref(5)

const filteredExperts = computed(() => {
  let list = [...expertList.value]
  if (expertFilter.name) {
    list = list.filter((item) => item.name.includes(expertFilter.name))
  }
  if (expertFilter.title) {
    list = list.filter((item) => item.title === expertFilter.title)
  }
  if (expertFilter.status) {
    list = list.filter((item) => item.status === expertFilter.status)
  }
  return list
})

const expertTotal = computed(() => filteredExperts.value.length)

const paginatedExperts = computed(() => {
  const start = (expertPage.value - 1) * expertPageSize.value
  const end = start + expertPageSize.value
  return filteredExperts.value.slice(start, end)
})

function resetExpertFilter() {
  expertFilter.name = ''
  expertFilter.title = ''
  expertFilter.status = ''
  expertPage.value = 1
}

// ===== 咨询记录筛选 =====
const consultFilter = reactive({
  elderName: '',
  type: '',
  status: '',
  urgency: '',
  dateRange: [],
})

const consultPage = ref(1)
const consultPageSize = ref(5)

const filteredConsults = computed(() => {
  let list = [...consultList.value]
  if (consultFilter.elderName) {
    list = list.filter((item) => item.elderName.includes(consultFilter.elderName))
  }
  if (consultFilter.type) {
    list = list.filter((item) => item.type === consultFilter.type)
  }
  if (consultFilter.status) {
    list = list.filter((item) => item.status === consultFilter.status)
  }
  if (consultFilter.urgency) {
    list = list.filter((item) => item.urgency === consultFilter.urgency)
  }
  if (consultFilter.dateRange && consultFilter.dateRange.length === 2) {
    list = list.filter((item) => {
      const date = item.time.split(' ')[0]
      return date >= consultFilter.dateRange[0] && date <= consultFilter.dateRange[1]
    })
  }
  return list
})

const consultTotal = computed(() => filteredConsults.value.length)

const paginatedConsults = computed(() => {
  const start = (consultPage.value - 1) * consultPageSize.value
  const end = start + consultPageSize.value
  return filteredConsults.value.slice(start, end)
})

function resetConsultFilter() {
  consultFilter.elderName = ''
  consultFilter.type = ''
  consultFilter.status = ''
  consultFilter.urgency = ''
  consultFilter.dateRange = []
  consultPage.value = 1
}

// ===== 工具函数 =====
function getTitleType(title) {
  const map = {
    主任医师: 'danger',
    副主任医师: 'warning',
    主治医师: 'primary',
    康复师: 'success',
  }
  return map[title] || 'info'
}

function getExpertStatusType(status) {
  const map = {
    online: 'success',
    busy: 'warning',
    offline: 'info',
    leave: 'danger',
  }
  return map[status] || 'info'
}

function getExpertStatusText(status) {
  const map = {
    online: '在线',
    busy: '忙碌',
    offline: '离线',
    leave: '休假',
  }
  return map[status] || status
}

function getConsultTypeColor(type) {
  const map = {
    健康咨询: 'primary',
    用药指导: 'success',
    康复建议: 'warning',
  }
  return map[type] || 'info'
}

function getConsultStatusType(status) {
  const map = {
    pending: 'warning',
    ongoing: 'primary',
    completed: 'success',
    cancelled: 'info',
  }
  return map[status] || 'info'
}

function getConsultStatusText(status) {
  const map = {
    pending: '待分配',
    ongoing: '进行中',
    completed: '已完成',
    cancelled: '已取消',
  }
  return map[status] || status
}

function getUrgencyType(urgency) {
  const map = {
    normal: 'info',
    important: 'warning',
    urgent: 'danger',
  }
  return map[urgency] || 'info'
}

function getUrgencyText(urgency) {
  const map = {
    normal: '普通',
    important: '重要',
    urgent: '紧急',
  }
  return map[urgency] || urgency
}

function getSatisfactionColor(rating) {
  if (rating >= 4.5) return '#67c23a'
  if (rating >= 3.5) return '#e6a23c'
  return '#f56c6c'
}

// ===== 专家管理操作 =====
const expertDialogVisible = ref(false)
const expertDialogTitle = ref('添加专家')
const isEditExpert = ref(false)
const editExpertId = ref(null)

const expertFormData = reactive({
  name: '',
  title: '',
  specialty: '',
  institution: '',
  phone: '',
  status: 'online',
})

const expertFormRules = {
  name: [{ required: true, message: '请输入专家姓名', trigger: 'blur' }],
  title: [{ required: true, message: '请选择职称', trigger: 'change' }],
  specialty: [{ required: true, message: '请输入专业领域', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
}

const expertFormRef = ref(null)

function handleAddExpert() {
  isEditExpert.value = false
  editExpertId.value = null
  expertDialogTitle.value = '添加专家'
  Object.assign(expertFormData, {
    name: '',
    title: '',
    specialty: '',
    institution: '',
    phone: '',
    status: 'online',
  })
  expertFormRef.value?.resetFields()
  expertDialogVisible.value = true
}

function handleEditExpert(row) {
  isEditExpert.value = true
  editExpertId.value = row.id
  expertDialogTitle.value = '编辑专家'
  Object.assign(expertFormData, row)
  expertDialogVisible.value = true
}

function handleDeleteExpert(row) {
  ElMessageBox.confirm(`确定要删除专家 "${row.name}" 吗？`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      await deleteExpert(row.id)
      ElMessage.success('删除成功')
      await initData()
    })
    .catch(() => {})
}

async function submitExpert() {
  expertFormRef.value?.validate(async (valid) => {
    if (!valid) return

    const payload = {
      name: expertFormData.name,
      title: expertFormData.title,
      specialty: expertFormData.specialty,
      institution: expertFormData.institution,
      phone: expertFormData.phone,
      status: STATUS_CODE_MAP[expertFormData.status] || expertFormData.status,
    }

    try {
      if (isEditExpert.value) {
        await updateExpert(editExpertId.value, {
          ...payload,
          rating: expertFormData.rating ?? 0,
        })
        ElMessage.success('更新成功')
      } else {
        await addExpert(payload)
        ElMessage.success('添加成功')
      }
      expertDialogVisible.value = false
      await initData()
    } catch (e) {
      // 错误提示已由响应拦截器统一处理
    }
  })
}

// ===== 排班操作 =====
const scheduleDialogVisible = ref(false)
const scheduleExpert = ref(null)
const scheduleData = reactive({
  dates: [],
  startTime: '08:00',
  endTime: '17:00',
  shift: 'full',
  remark: '',
})

function handleSchedule(row) {
  scheduleExpert.value = row
  scheduleData.dates = []
  scheduleData.startTime = '08:00'
  scheduleData.endTime = '17:00'
  scheduleData.shift = 'full'
  scheduleData.remark = ''
  scheduleDialogVisible.value = true
}

async function submitSchedule() {
  if (!scheduleData.dates || scheduleData.dates.length === 0) {
    ElMessage.warning('请选择排班日期')
    return
  }

  const expert = expertList.value.find((item) => item.id === scheduleExpert.value.id)
  if (expert) {
    const dateStr = scheduleData.dates
      .map((d) => {
        const date = new Date(d)
        return `${date.getMonth() + 1}月${date.getDate()}日`
      })
      .join('、')
    const shiftMap = {
      morning: '上午班',
      afternoon: '下午班',
      full: '全天班',
    }
    const schedule = `${dateStr} ${shiftMap[scheduleData.shift]} (${scheduleData.startTime}-${scheduleData.endTime})`

    try {
      await updateExpertSchedule(expert.id, { schedule })
      expert.schedule = schedule
      ElMessage.success('排班保存成功')
      scheduleDialogVisible.value = false
    } catch (e) {
      // 错误提示已由响应拦截器统一处理
    }
  }
}

// ===== 专家详情 =====
const expertDetailVisible = ref(false)
const expertDetailData = ref(null)

async function handleViewExpert(row) {
  try {
    const res = await getExpertById(row.id)
    const detail = res.data.data
    expertDetailData.value = detail
      ? {
          ...detail,
          title:
            TITLE_TEXT_MAP[detail.titleText || detail.title] || detail.titleText || detail.title,
          status: STATUS_TEXT_MAP[detail.status] || detail.status,
          schedule: formatSchedule(detail.schedule),
        }
      : row
  } catch (e) {
    expertDetailData.value = row
  }
  expertDetailVisible.value = true
}

// ===== 咨询记录操作 =====
const consultDetailVisible = ref(false)
const consultDetailData = ref(null)

const assignDialogVisible = ref(false)
const assignConsult = ref(null)
const assignExpertId = ref(null)

const completeDialogVisible = ref(false)
const completeForm = reactive({
  reply: '',
  rating: 0,
  feedback: '',
})

const addConsultDialogVisible = ref(false)
const consultFormData = reactive({
  elderName: '',
  type: '',
  urgency: 'normal',
  content: '',
})

const consultFormRules = {
  elderName: [{ required: true, message: '请输入老人姓名', trigger: 'blur' }],
  type: [{ required: true, message: '请选择咨询类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入咨询内容', trigger: 'blur' }],
}

const consultFormRef = ref(null)

function handleViewConsult(row) {
  consultDetailData.value = row
  consultDetailVisible.value = true
}

function handleAddConsult() {
  Object.assign(consultFormData, {
    elderName: '',
    type: '',
    urgency: 'normal',
    content: '',
  })
  consultFormRef.value?.resetFields()
  addConsultDialogVisible.value = true
}

function handleAssignExpert(row) {
  assignConsult.value = row
  assignExpertId.value = null
  assignDialogVisible.value = true
}

function handleCompleteConsult(row) {
  assignConsult.value = row
  completeForm.reply = ''
  completeForm.rating = 0
  completeForm.feedback = ''
  completeDialogVisible.value = true
}

function handleDeleteConsult(row) {
  ElMessageBox.confirm(`确定要删除 "${row.elderName}" 的咨询记录吗？`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await deleteConsultApi(row.id)
        ElMessage.success('删除成功')
        await loadConsultList()
        updateStats()
      } catch (e) {
        /* 拦截器已提示 */
      }
    })
    .catch(() => {})
}

// ===== 可用专家列表（用于分配） =====
const availableExperts = computed(() => {
  return expertList.value
    .filter((item) => item.status === 'online' || item.status === 'busy')
    .map((item) => ({
      label: `${item.name} (${item.title})`,
      value: item.id,
    }))
})

// ===== 提交分配 =====
async function submitAssign() {
  if (!assignExpertId.value) {
    ElMessage.warning('请选择专家')
    return
  }

  const expert = expertList.value.find((item) => item.id === assignExpertId.value)
  const consult = consultList.value.find((item) => item.id === assignConsult.value.id)

  if (consult && expert) {
    try {
      await assignConsultApi(consult.id, expert.id)
      ElMessage.success(`已分配给专家 ${expert.name}`)
      assignDialogVisible.value = false
      await loadConsultList()
      updateStats()
    } catch (e) {
      /* 拦截器已提示 */
    }
  }
}

// ===== 提交完成 =====
async function submitComplete() {
  if (!completeForm.reply) {
    ElMessage.warning('请输入专家回复')
    return
  }

  const consult = consultList.value.find((item) => item.id === assignConsult.value.id)
  if (consult) {
    try {
      await completeConsultApi(consult.id, {
        reply: completeForm.reply,
        rating: Math.round(completeForm.rating) || 5,
        feedback: completeForm.feedback || '',
      })
      ElMessage.success('咨询已完成')
      completeDialogVisible.value = false
      await loadConsultList()
      updateStats()
    } catch (e) {
      /* 拦截器已提示 */
    }
  }
}

// ===== 提交新建咨询 =====
function submitConsult() {
  consultFormRef.value?.validate(async (valid) => {
    if (!valid) return

    try {
      await addConsultApi({
        elderName: consultFormData.elderName,
        type: consultFormData.type,
        urgency: consultFormData.urgency,
        content: consultFormData.content,
      })
      ElMessage.success('咨询创建成功')
      addConsultDialogVisible.value = false
      await loadConsultList()
      updateStats()
    } catch (e) {
      /* 拦截器已提示 */
    }
  })
}

// ===== 统计分析数据 =====
const expertWorkload = computed(() => {
  const counts = {}
  consultList.value.forEach((item) => {
    if (item.expertName) {
      counts[item.expertName] = (counts[item.expertName] || 0) + 1
    }
  })

  const sorted = Object.keys(counts)
    .map((name) => ({
      name: name,
      count: counts[name],
    }))
    .sort((a, b) => b.count - a.count)

  const maxCount = sorted.length > 0 ? sorted[0].count : 1

  return sorted.map((item) => ({
    ...item,
    percentage: Math.round((item.count / maxCount) * 100),
  }))
})

const satisfactionData = computed(() => {
  const completed = consultList.value.filter((c) => c.status === 'completed' && c.rating)
  const ratings = {}
  completed.forEach((item) => {
    const key = item.rating.toFixed(1)
    ratings[key] = (ratings[key] || 0) + 1
  })

  const total = completed.length || 1
  return Object.keys(ratings)
    .map((key) => ({
      rating: parseFloat(key),
      count: ratings[key],
      percentage: Math.round((ratings[key] / total) * 100),
    }))
    .sort((a, b) => b.rating - a.rating)
})

const monthlyStats = computed(() => {
  const monthAgo = new Date()
  monthAgo.setMonth(monthAgo.getMonth() - 1)

  const monthConsults = consultList.value.filter((item) => {
    const date = new Date(item.time)
    return date >= monthAgo
  })

  const completed = monthConsults.filter((c) => c.status === 'completed')
  const avgRating =
    completed.length > 0
      ? (completed.reduce((sum, c) => sum + (c.rating || 0), 0) / completed.length).toFixed(1)
      : 0

  return {
    total: monthConsults.length,
    completed: completed.length,
    ongoing: monthConsults.filter((c) => c.status === 'ongoing').length,
    pending: monthConsults.filter((c) => c.status === 'pending').length,
    avgRating: avgRating,
    activeExperts: expertList.value.filter((e) => e.todayCount > 0).length,
  }
})

// ===== 刷新数据 =====
function refreshData() {
  initData()
  ElMessage.success('数据已刷新')
}

// ===== 生命周期 =====
onMounted(() => {
  initData()
})
</script>

<style scoped>
.expert-page {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100%;
}

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
  gap: 10px;
}

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
.stat-card.today .stat-icon {
  background: #f0f9eb;
  color: #67c23a;
}
.stat-card.pending .stat-icon {
  background: #fdf6ec;
  color: #e6a23c;
}
.stat-card.ongoing .stat-icon {
  background: #fef0f0;
  color: #f56c6c;
}
.stat-card.satisfaction .stat-icon {
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

.chart-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-box {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px 16px;
}

.chart-header {
  padding-bottom: 8px;
  margin-bottom: 8px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.chart-container {
  width: 100%;
  height: 220px;
}

.tab-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

:deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.filter-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
  margin-bottom: 8px;
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.filter-title .el-icon {
  font-size: 16px;
  color: #409eff;
}

.filter-body :deep(.el-form-item) {
  margin-bottom: 4px;
}

.table-section {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.table-title .el-icon {
  font-size: 16px;
  color: #409eff;
}

.table-actions {
  display: flex;
  gap: 8px;
}

:deep(.el-table) {
  border-radius: 0;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f5f7fa !important;
  color: #333;
  font-weight: 600;
  padding: 10px 0;
}

:deep(.el-table .el-table__cell) {
  padding: 6px 0;
}

.action-buttons {
  display: flex;
  gap: 2px;
  flex-wrap: nowrap;
  justify-content: center;
  align-items: center;
}

.action-buttons .el-button {
  padding: 2px 6px;
  font-size: 12px;
}

.action-buttons .el-button .el-icon {
  font-size: 13px;
  margin-right: 2px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

.detail-content {
  padding: 4px 0;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  padding: 6px 0;
  border-bottom: 1px solid #f5f5f5;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}

.detail-value {
  font-size: 14px;
  color: #333;
  word-break: break-all;
}

.schedule-content {
  padding: 8px 0;
}

.schedule-expert {
  font-size: 16px;
}
</style>