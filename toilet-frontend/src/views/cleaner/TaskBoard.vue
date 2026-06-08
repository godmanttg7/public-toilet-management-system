<template>
  <div class="task-board">
    <!-- 日期和统计 -->
    <div class="board-header">
      <div class="board-header__info">
        <h2 class="board-header__title">今日任务</h2>
        <span class="board-header__date">{{ today }}</span>
      </div>
      <div class="board-header__stats">
        <div class="mini-stat">
          <span class="mini-stat__value">{{ tasks.length }}</span>
          <span class="mini-stat__label">总任务</span>
        </div>
        <div class="mini-stat mini-stat--pending">
          <span class="mini-stat__value">{{ tasks.filter(t => t.status === 'PENDING').length }}</span>
          <span class="mini-stat__label">待打卡</span>
        </div>
        <div class="mini-stat mini-stat--done">
          <span class="mini-stat__value">{{ tasks.filter(t => t.status === 'FINISHED').length }}</span>
          <span class="mini-stat__label">已完成</span>
        </div>
      </div>
    </div>

    <!-- 任务卡片 -->
    <el-empty v-if="tasks.length === 0" description="今日暂无排班任务" :image-size="120" />
    <div class="task-cards" v-else>
      <div
        v-for="task in tasks"
        :key="task.id"
        class="task-card"
        :class="`task-card--${task.status.toLowerCase()}`"
      >
        <div class="task-card__header">
          <div class="task-card__toilet">
            <el-icon><Location /></el-icon>
            <span>{{ task.toiletName || task.toiletId + '号公厕' }}</span>
          </div>
          <el-tag :type="task.status === 'FINISHED' ? 'success' : task.status === 'CHECKED_IN' ? 'primary' : task.status === 'REJECTED' ? 'danger' : 'warning'"
            effect="plain" size="small">
            {{ statusText(task.status) }}
          </el-tag>
        </div>
        <div class="task-card__tags">
          <el-tag size="small" effect="plain">
            {{ task.shiftType === 'MORNING' ? '早班' : task.shiftType === 'AFTERNOON' ? '中班' : '晚班' }}
          </el-tag>
          <el-tag size="small" :type="task.cleanType === 'DAILY' ? 'success' : 'warning'" effect="plain">
            {{ task.cleanType === 'DAILY' ? '日常保洁' : '深度保洁' }}
          </el-tag>
        </div>
        <div class="task-card__actions">
          <el-button
            v-if="task.status === 'PENDING'"
            type="primary" @click="handleCheckin(task)"
            :loading="checkingIn === task.id"
          >
            打卡上岗
          </el-button>
          <div v-else-if="task.status === 'CHECKED_IN'" class="task-card__inline-actions">
            <el-tag type="success" size="small">已打卡</el-tag>
            <el-button type="primary" size="small" @click="handleSignoff(task)"
              :loading="signingOff === task.id">
              签退
            </el-button>
          </div>
          <div v-else-if="task.status === 'SIGNED_OFF'">
            <el-tag type="primary" size="small">等待评分</el-tag>
          </div>
          <div v-else-if="task.status === 'REJECTED'">
            <el-tag type="danger" size="small">不合格需整改</el-tag>
          </div>
          <div v-else-if="task.status === 'FINISHED'" class="task-card__score">
            <el-tag type="success" size="small">已完成</el-tag>
            <span v-if="task.score != null" class="task-card__score-value">{{ task.score }}分</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 故障上报 -->
    <div class="report-section">
      <div class="report-section__header">
        <h3>故障上报</h3>
        <span class="report-section__desc">发现设施故障时，请及时上报维修</span>
      </div>
      <el-form :model="reportForm" inline class="report-form">
        <el-form-item label="所属公厕">
          <el-select v-model="reportForm.toiletId" placeholder="选择公厕" filterable style="width: 160px">
            <el-option v-for="t in toiletList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障设施">
          <el-select v-model="reportForm.facilityId" placeholder="选择设施" filterable style="width: 160px"
            :disabled="!reportForm.toiletId" :loading="facilityLoading">
            <el-option v-for="f in facilityList" :key="f.id"
              :label="f.facilityType + (f.brand ? ' - ' + f.brand : '')" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述">
          <el-input v-model="reportForm.faultDesc" placeholder="请描述故障情况" style="width: 300px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleReport" :loading="reporting">
            <el-icon><Warning /></el-icon> 提交报修
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getMySchedules, checkin, signoff, reportRepair, getToiletList, getFacilityList } from '../../api'

const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
const tasks = ref([])
const checkingIn = ref(null)
const signingOff = ref(null)
const reporting = ref(false)
const reportForm = ref({ toiletId: null, facilityId: null, faultDesc: '' })
const toiletList = ref([])
const facilityList = ref([])
const facilityLoading = ref(false)

function statusText(s) {
  return { PENDING: '待打卡', CHECKED_IN: '已打卡', SIGNED_OFF: '待评分', REJECTED: '整改中', FINISHED: '已完成' }[s] || s
}

async function loadTasks() {
  try {
    const res = await getMySchedules({ current: 1, size: 50 })
    if (res.data?.records) {
      const todayStr = new Date().toISOString().split('T')[0]
      tasks.value = res.data.records.filter(t => t.scheduleDate === todayStr)
    }
  } catch (e) { console.error('加载任务失败', e) }
}

async function handleCheckin(task) {
  checkingIn.value = task.id
  try {
    await checkin(task.id)
    ElMessage.success('打卡成功')
    loadTasks()
  } catch { /* handled by interceptor */ }
  finally { checkingIn.value = null }
}

async function handleSignoff(task) {
  signingOff.value = task.id
  try {
    await signoff(task.id)
    ElMessage.success('签退成功')
    loadTasks()
  } catch { /* handled */ }
  finally { signingOff.value = null }
}

async function handleReport() {
  if (!reportForm.value.toiletId || !reportForm.value.faultDesc) {
    ElMessage.warning('请选择公厕并填写故障描述')
    return
  }
  reporting.value = true
  try {
    await reportRepair(reportForm.value)
    ElMessage.success('故障上报成功')
    reportForm.value = { toiletId: null, facilityId: null, faultDesc: '' }
    facilityList.value = []
  } finally { reporting.value = false }
}

// 选择公厕后加载设施
watch(() => reportForm.value.toiletId, async (newVal) => {
  reportForm.value.facilityId = null
  if (!newVal) { facilityList.value = []; return }
  facilityLoading.value = true
  try {
    const res = await getFacilityList({ toiletId: newVal })
    facilityList.value = res.data || []
  } catch { facilityList.value = [] }
  finally { facilityLoading.value = false }
})

onMounted(async () => {
  loadTasks()
  try {
    const res = await getToiletList()
    toiletList.value = res.data || []
  } catch { toiletList.value = [] }
})
</script>

<style scoped>
.task-board { max-width: 1200px; }

.board-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.board-header__title {
  font-size: 22px;
  font-weight: 700;
  color: #1F2937;
  margin: 0;
}

.board-header__date {
  font-size: 14px;
  color: #6B7280;
  margin-left: 12px;
}

.board-header__stats {
  display: flex;
  gap: 16px;
}

.mini-stat {
  background: #fff;
  border-radius: 10px;
  padding: 12px 20px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  min-width: 80px;
}

.mini-stat__value {
  font-size: 24px;
  font-weight: 700;
  color: #1E3A5F;
  display: block;
  line-height: 1.2;
}

.mini-stat__label {
  font-size: 12px;
  color: #9CA3AF;
}

.mini-stat--pending .mini-stat__value { color: #FAAD14; }
.mini-stat--done .mini-stat__value { color: #52C41A; }

.task-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.task-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  border-left: 4px solid #E5E7EB;
  transition: box-shadow 0.25s;
}
.task-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.task-card--pending { border-left-color: #FAAD14; }
.task-card--checked_in { border-left-color: #1890FF; }
.task-card--signed_off { border-left-color: #722ED1; }
.task-card--rejected { border-left-color: #FF4D4F; }
.task-card--finished { border-left-color: #52C41A; }

.task-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.task-card__toilet {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #1F2937;
  font-size: 15px;
}

.task-card__tags {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}

.task-card__actions { min-height: 32px; }
.task-card__inline-actions { display: flex; align-items: center; gap: 8px; }
.task-card__score { display: flex; align-items: center; gap: 8px; }
.task-card__score-value { font-size: 13px; color: #1890FF; font-weight: 600; }

.report-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.report-section__header {
  margin-bottom: 16px;
}

.report-section__header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1F2937;
  margin: 0 0 4px;
}

.report-section__desc {
  font-size: 13px;
  color: #9CA3AF;
}

.report-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
}

@media (max-width: 1024px) {
  .task-cards { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 640px) {
  .task-cards { grid-template-columns: 1fr; }
}
</style>
