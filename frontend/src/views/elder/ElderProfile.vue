<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getElderProfile } from '@/api/elderPortal'
import type { ElderProfile } from '@/api/elderPortal'

const profile = ref<ElderProfile | null>(null)
const loading = ref(false)

const genderText = (g?: string) => ({ MALE: '男', FEMALE: '女' }[g as string] || g || '-')

const residenceText = (r?: string) =>
  ({ PERMANENT: '常居', TEMPORARY: '暂居', DAY_CARE: '日托', SHORT_STAY: '短住' }[r as string] || r || '-')

const relationText = (r?: string) =>
  ({ SON: '儿子', DAUGHTER: '女儿', SPOUSE: '配偶', GRANDCHILD: '孙辈', SIBLING: '兄弟姐妹', OTHER: '其他' }[r as string] || r || '-')

async function load() {
  loading.value = true
  try {
    const res = await getElderProfile()
    if (res.data.code === 200) {
      profile.value = res.data.data || null
    }
  } catch (e) {
    ElMessage.error('加载档案失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="elder-profile" v-loading="loading">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Avatar /></el-icon>
        个人档案
      </h2>
      <el-button :loading="loading" @click="load">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <div class="profile-card">
      <div class="head">
        <el-avatar :size="56">{{ profile?.name?.charAt(0) || '老' }}</el-avatar>
        <div class="head-info">
          <div class="name">{{ profile?.name || '-' }}</div>
          <div class="sub">{{ genderText(profile?.gender) }} · {{ profile?.age }} 岁 · {{ profile?.ethnicity || '-' }}</div>
        </div>
      </div>

      <el-descriptions :column="2" border class="desc">
        <el-descriptions-item label="身份证号">{{ profile?.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ profile?.birthDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="民族">{{ profile?.ethnicity || '-' }}</el-descriptions-item>
        <el-descriptions-item label="居住类型">{{ residenceText(profile?.residenceType) }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ profile?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="家庭地址">{{ profile?.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="家属人数">{{ profile?.familyCount ?? 0 }} 人</el-descriptions-item>
        <el-descriptions-item label="备注">{{ profile?.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="family-card">
      <div class="family-title">
        <el-icon><UserFilled /></el-icon>
        我的家属
      </div>
      <el-table :data="profile?.families || []" border>
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column label="关系" min-width="120">
          <template #default="{ row }">{{ relationText(row.relationship) }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" min-width="140" />
        <el-table-column label="紧急联系人" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.isEmergency" type="danger" size="small">紧急</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无家属" :image-size="80" />
        </template>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.elder-profile {
  padding: 20px;
  background: #f0f2f5;
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
.profile-card,
.family-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}
.name {
  font-size: 20px;
  font-weight: 700;
}
.sub {
  margin-top: 4px;
  color: #909399;
  font-size: 13px;
}
.desc {
  margin-bottom: 0;
}
.family-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
}
.family-title .el-icon {
  font-size: 18px;
  color: #409eff;
}
</style>
