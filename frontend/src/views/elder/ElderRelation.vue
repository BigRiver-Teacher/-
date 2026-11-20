<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getElderRelation } from '@/api/elderPortal'
import type { ElderRelation } from '@/api/elderPortal'
import ListManagePage from '@/components/ListManagePage.vue'

const relation = ref<ElderRelation>({ caregivers: [], families: [] })
const loading = ref(false)
const keyword = ref('')

const serviceTypeText = (t?: string) =>
  ({ DAILY_CARE: '日常照护', MEDICAL: '医疗护理', REHAB: '康复护理', FULL: '全托照护' }[t as string] || t || '照护服务')

const relationText = (r?: string) =>
  ({ SON: '儿子', DAUGHTER: '女儿', SPOUSE: '配偶', GRANDCHILD: '孙辈', SIBLING: '兄弟姐妹', OTHER: '其他' }[r as string] || r || '-')

const filteredCaregivers = computed(() =>
  relation.value.caregivers.filter((c) => !keyword.value || c.name.includes(keyword.value)),
)
const filteredFamilies = computed(() =>
  relation.value.families.filter((f) => !keyword.value || f.name.includes(keyword.value)),
)

const stats = computed(() => {
  const activeCg = relation.value.caregivers.filter((c) => c.status === 'ACTIVE').length
  const emergency = relation.value.families.filter((f) => f.isEmergency).length
  return [
    { iconComponent: 'User', value: relation.value.caregivers.length, label: '我的护工', iconBgColor: '#e6f7ff', iconColor: '#409eff' },
    { iconComponent: 'UserFilled', value: relation.value.families.length, label: '我的家人', iconBgColor: '#f0f9eb', iconColor: '#67c23a' },
    { iconComponent: 'Bell', value: emergency, label: '紧急联系人', iconBgColor: '#fef0f0', iconColor: '#f56c6c' },
    { iconComponent: 'CircleCheck', value: activeCg, label: '服务中护工', iconBgColor: '#fdf6ec', iconColor: '#e6a23c' },
  ]
})

// 详情弹窗
const detailVisible = ref(false)
const detailType = ref<'caregiver' | 'family'>('caregiver')
const detail = ref<any>({})

function showCaregiver(row: any) {
  detailType.value = 'caregiver'
  detail.value = row
  detailVisible.value = true
}
function showFamily(row: any) {
  detailType.value = 'family'
  detail.value = row
  detailVisible.value = true
}

async function load() {
  loading.value = true
  try {
    const res = await getElderRelation()
    if (res.data.code === 200) {
      relation.value = res.data.data || { caregivers: [], families: [] }
    }
  } catch (e) {
    ElMessage.error('加载关系信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <ListManagePage
    title="我的护工与家人"
    icon="UserFilled"
    :stats="stats"
    :total="filteredCaregivers.length + filteredFamilies.length"
    :show-pagination="false"
    table-title="关系列表"
  >
    <template #header-actions>
      <el-button :loading="loading" @click="load">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </template>

    <template #filters>
      <el-form inline>
        <el-form-item label="姓名搜索">
          <el-input v-model="keyword" placeholder="输入姓名" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="keyword = ''">重置</el-button>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <!-- 护工表格 -->
      <div class="sub-title"><el-icon><User /></el-icon>我的护工</div>
      <el-table :data="filteredCaregivers" border v-loading="loading" style="margin-bottom: 20px">
        <el-table-column prop="name" label="护工姓名" min-width="140" />
        <el-table-column label="服务类型" min-width="140">
          <template #default="{ row }">{{ serviceTypeText(row.serviceType) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">{{ row.status === 'ACTIVE' ? '服务中' : row.status || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showCaregiver(row)">查看</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无护工" :image-size="80" />
        </template>
      </el-table>

      <!-- 家人表格 -->
      <div class="sub-title"><el-icon><UserFilled /></el-icon>我的家人</div>
      <el-table :data="filteredFamilies" border v-loading="loading">
        <el-table-column prop="name" label="姓名" min-width="140" />
        <el-table-column label="关系" min-width="120">
          <template #default="{ row }">{{ relationText(row.relationship) }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" min-width="140" />
        <el-table-column label="紧急联系人" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.isEmergency" type="danger" size="small">紧急</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showFamily(row)">查看</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无家人" :image-size="80" />
        </template>
      </el-table>
    </template>

    <template #dialogs>
      <el-dialog v-model="detailVisible" :title="detailType === 'caregiver' ? '护工详情' : '家人详情'" width="420px">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="姓名">{{ detail.name || '-' }}</el-descriptions-item>
          <template v-if="detailType === 'caregiver'">
            <el-descriptions-item label="服务类型">{{ serviceTypeText(detail.serviceType) }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ detail.status === 'ACTIVE' ? '服务中' : detail.status || '-' }}</el-descriptions-item>
          </template>
          <template v-else>
            <el-descriptions-item label="关系">{{ relationText(detail.relationship) }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ detail.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="紧急联系人">{{ detail.isEmergency ? '是' : '否' }}</el-descriptions-item>
          </template>
        </el-descriptions>
      </el-dialog>
    </template>
  </ListManagePage>
</template>

<style scoped>
.sub-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 4px 0 12px;
}
.sub-title .el-icon {
  font-size: 18px;
  color: #409eff;
}
</style>
