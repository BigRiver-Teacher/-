<template>
  <div class="list-manage-page">
    <!-- ===== 页面标题（左上角标明当前模块） ===== -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><component :is="icon" /></el-icon>
        {{ title }}
      </h2>
      <div class="header-actions">
        <slot name="header-actions" />
      </div>
    </div>

    <!-- ===== 统计卡片（基础卡片预览） ===== -->
    <div v-if="showStats && stats.length" class="stats-grid">
      <StatsCard v-for="(s, i) in stats" :key="i" v-bind="s" />
    </div>

    <!-- ===== 筛选区域 ===== -->
    <div v-if="showFilter" class="filter-section">
      <div class="filter-header">
        <div class="filter-title">
          <el-icon><Search /></el-icon>
          <span>搜索</span>
        </div>
        <div class="filter-actions">
          <slot name="filter-actions" />
        </div>
      </div>
      <div class="filter-body">
        <slot name="filters" />
      </div>
    </div>

    <!-- ===== 表格区域 ===== -->
    <div class="table-section">
      <div class="table-header">
        <div class="table-title">
          <el-icon><List /></el-icon>
          <span>{{ tableTitle }}</span>
          <el-tag v-if="showTotal" size="small" type="info">共 {{ total }} 条</el-tag>
        </div>
        <div class="table-header-actions">
          <slot name="table-header-actions" />
        </div>
      </div>

      <slot name="table" />

      <!-- 分页 -->
      <div v-if="showPagination" class="pagination-wrapper">
        <el-pagination
          background
          v-model:current-page="currentPage"
          v-model:page-size="currentPageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @update:current-page="(v) => emit('page-change', v)"
          @update:page-size="(v) => emit('size-change', v)"
        />
      </div>
    </div>

    <!-- ===== 详情 / 编辑弹窗 ===== -->
    <slot name="dialogs" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Search, List } from '@element-plus/icons-vue'
import StatsCard from './StatsCard.vue'

interface StatItem {
  iconComponent: string
  value: string | number
  label: string
  iconBgColor: string
  iconColor: string
}

withDefaults(
  defineProps<{
    title: string
    icon?: string
    stats?: StatItem[]
    total?: number
    showStats?: boolean
    showFilter?: boolean
    showPagination?: boolean
    showTotal?: boolean
    tableTitle?: string
  }>(),
  {
    icon: 'List',
    stats: () => [],
    total: 0,
    showStats: true,
    showFilter: true,
    showPagination: true,
    showTotal: true,
    tableTitle: '列表',
  },
)

const emit = defineEmits<{
  (e: 'page-change', page: number): void
  (e: 'size-change', size: number): void
}>()

const currentPage = ref(1)
const currentPageSize = ref(10)
</script>

<style scoped>
.list-manage-page {
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
  gap: 10px;
}

/* ===== 统计卡片 ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
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
.filter-actions {
  display: flex;
  gap: 8px;
}
.filter-body :deep(.el-form-item) {
  margin-bottom: 8px;
}

/* ===== 表格区域 ===== */
.table-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow-x: auto;
}
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}
.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.table-title .el-icon {
  font-size: 18px;
  color: #409eff;
}
.table-header-actions {
  display: flex;
  gap: 8px;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}
:deep(.el-table th.el-table__cell) {
  background-color: #f5f7fa !important;
  color: #333;
  font-weight: 600;
  padding: 12px 0;
}
:deep(.el-table .el-table__cell) {
  padding: 8px 0;
}

/* ===== 分页 ===== */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .list-manage-page {
    padding: 12px;
  }
}
</style>
