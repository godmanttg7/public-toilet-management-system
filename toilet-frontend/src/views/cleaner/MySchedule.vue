<template>
  <div class="page-crud">
    <div class="page-card">
      <div class="page-card__header">
        <h2 class="page-card__title">我的排班记录</h2>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" empty-text="暂无排班记录">
        <el-table-column prop="scheduleDate" label="日期" width="130" />
        <el-table-column label="公厕" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.toiletName || '公厕#' + row.toiletId }}</template>
        </el-table-column>
        <el-table-column label="班次" width="90">
          <template #default="{ row }">
            {{ row.shiftType === 'MORNING' ? '早班' : row.shiftType === 'AFTERNOON' ? '中班' : '晚班' }}
          </template>
        </el-table-column>
        <el-table-column label="保洁类型" width="110">
          <template #default="{ row }">
            <el-tag :type="row.cleanType === 'DAILY' ? 'success' : 'warning'" effect="plain" size="small">
              {{ row.cleanType === 'DAILY' ? '日常保洁' : '深度保洁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="plain" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkinTime" label="打卡时间" width="170" />
        <el-table-column label="评分" width="70" align="center">
          <template #default="{ row }">
            <span v-if="row.score != null" :class="{ 'score-pass': row.score >= 60, 'score-fail': row.score < 60 }">
              {{ row.score }}分
            </span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="primary" size="small"
              @click="handleCheckin(row)" :loading="checkingIn === row.id">
              打卡
            </el-button>
            <el-button v-else-if="row.status === 'CHECKED_IN'" type="success" size="small"
              @click="handleSignoff(row)" :loading="signingOff === row.id">
              签退
            </el-button>
            <span v-else-if="row.status === 'SIGNED_OFF'" style="color: #722ED1; font-size: 12px">待评分</span>
            <el-tag v-else-if="row.status === 'REJECTED'" type="danger" size="small">需整改</el-tag>
            <span v-else style="color: #9CA3AF">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination
          v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total"
          layout="total, prev, pager, next" @change="fetchData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMySchedules, checkin, signoff } from '../../api'

const loading = ref(false)
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const checkingIn = ref(null)
const signingOff = ref(null)

function statusText(s) {
  return { PENDING: '待打卡', CHECKED_IN: '已打卡', SIGNED_OFF: '待评分', REJECTED: '整改中', FINISHED: '已完成' }[s] || s
}
function statusTagType(s) {
  return { PENDING: 'warning', CHECKED_IN: 'primary', SIGNED_OFF: 'info', REJECTED: 'danger', FINISHED: 'success' }[s] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getMySchedules({ current: page.current, size: page.size })
    tableData.value = res.data.records
    page.total = res.data.total
  } finally { loading.value = false }
}

async function handleCheckin(row) {
  checkingIn.value = row.id
  try {
    await checkin(row.id)
    ElMessage.success('打卡成功')
    fetchData()
  } finally { checkingIn.value = null }
}

async function handleSignoff(row) {
  signingOff.value = row.id
  try {
    await signoff(row.id)
    ElMessage.success('签退成功')
    fetchData()
  } finally { signingOff.value = null }
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-crud { max-width: 1200px; }
.page-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.page-card__header { margin-bottom: 16px; }
.page-card__title {
  font-size: 18px;
  font-weight: 600;
  color: #1F2937;
  margin: 0;
}
.page-pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.score-pass { color: #52C41A; font-weight: 600; }
.score-fail { color: #FF4D4F; font-weight: 600; }
</style>
