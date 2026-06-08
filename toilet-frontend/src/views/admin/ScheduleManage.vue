<template>
  <div class="page-crud">
    <div class="page-card">
      <div class="page-toolbar">
        <div class="page-toolbar__search">
          <el-select v-model="search.toiletId" placeholder="选择公厕" clearable style="width: 180px; margin-right: 10px">
            <el-option v-for="t in toiletList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
          <el-select v-model="search.cleanerId" placeholder="选择保洁员" clearable style="width: 180px; margin-right: 10px">
            <el-option v-for="u in cleanerList" :key="u.id" :label="u.realName || u.username" :value="u.id" />
          </el-select>
          <el-button type="primary" @click="fetchData">
            <el-icon><Search /></el-icon> 查询
          </el-button>
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增排班
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" empty-text="暂无排班记录">
        <el-table-column label="公厕" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.toiletName || '公厕#' + row.toiletId }}</template>
        </el-table-column>
        <el-table-column label="保洁员" min-width="100">
          <template #default="{ row }">{{ row.cleanerName || (row.cleanerId ? '用户#' + row.cleanerId : '未知') }}</template>
        </el-table-column>
        <el-table-column prop="scheduleDate" label="排班日期" width="120" />
        <el-table-column label="班次" width="90">
          <template #default="{ row }">{{ shiftLabel(row.shiftType) }}</template>
        </el-table-column>
        <el-table-column label="保洁类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.cleanType === 'DAILY' ? 'success' : 'warning'" effect="plain" size="small">
              {{ cleanLabel(row.cleanType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="scheduleStatusTagType(row.status)" effect="plain" size="small">
              {{ scheduleStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="70" align="center" />
        <el-table-column prop="checkinTime" label="打卡时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'SIGNED_OFF' || row.status === 'REJECTED'"
              text type="primary" size="small" @click="handleScore(row)">评分</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination
          v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total"
          layout="total, prev, pager, next, sizes" :page-sizes="[10, 20, 50]"
          @change="fetchData"
        />
      </div>
    </div>

    <!-- 新增排班弹窗 -->
    <el-dialog title="新增排班" v-model="dialogVisible" width="500px" @close="resetForm" destroy-on-close>
      <el-form :model="form" ref="formRef" :rules="formRules" label-width="100px">
        <el-form-item label="所属公厕" prop="toiletId">
          <el-select v-model="form.toiletId" placeholder="请选择公厕" filterable style="width: 100%">
            <el-option v-for="t in toiletList" :key="t.id" :label="t.name + ' - ' + t.address" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="保洁人员" prop="cleanerId">
          <el-select v-model="form.cleanerId" placeholder="请选择保洁员" filterable style="width: 100%">
            <el-option v-for="u in cleanerList" :key="u.id"
              :label="u.realName + ' (' + u.username + ')'" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排班日期" prop="scheduleDate">
          <el-date-picker v-model="form.scheduleDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="班次" prop="shiftType">
          <el-select v-model="form.shiftType" style="width: 100%">
            <el-option label="早班" value="MORNING" />
            <el-option label="中班" value="AFTERNOON" />
            <el-option label="晚班" value="EVENING" />
          </el-select>
        </el-form-item>
        <el-form-item label="保洁类型" prop="cleanType">
          <el-select v-model="form.cleanType" style="width: 100%">
            <el-option label="日常保洁" value="DAILY" />
            <el-option label="深度保洁" value="DEEP" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 评分弹窗 -->
    <el-dialog title="保洁评分" v-model="scoreDialogVisible" width="460px" destroy-on-close>
      <el-form :model="scoreForm" ref="scoreFormRef" :rules="scoreFormRules" label-width="80px">
        <el-form-item label="评分" prop="score">
          <el-slider v-model="scoreForm.score" :min="0" :max="100" :marks="scoreMarks" show-input style="width: 100%" />
        </el-form-item>
        <div style="font-size: 12px; color: #999; margin: -10px 0 10px 80px">
          60分以上为合格，低于60分将退回整改
        </div>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="scoreForm.remark" type="textarea" :rows="3" placeholder="请输入评分备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleScoreSubmit" :loading="scoreSubmitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSchedulePage, addSchedule, deleteSchedule, scoreSchedule, getToiletList, getUserList } from '../../api'

const loading = ref(false)
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const search = reactive({ toiletId: null, cleanerId: null })
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ toiletId: null, cleanerId: null, scheduleDate: '', shiftType: 'MORNING', cleanType: 'DAILY' })
const formRules = {
  toiletId: [{ required: true, message: '请选择公厕', trigger: 'change' }],
  cleanerId: [{ required: true, message: '请选择保洁员', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择排班日期', trigger: 'change' }],
  shiftType: [{ required: true, message: '请选择班次', trigger: 'change' }],
  cleanType: [{ required: true, message: '请选择保洁类型', trigger: 'change' }]
}

const scoreDialogVisible = ref(false)
const scoreSubmitting = ref(false)
const scoreFormRef = ref(null)
const currentScoreRow = ref(null)
const scoreForm = reactive({ score: 60, remark: '' })
const scoreFormRules = { score: [{ required: true, message: '请评分', trigger: 'change' }] }
const scoreMarks = { 0: '0', 60: '及格', 100: '100' }

const toiletList = ref([])
const cleanerList = ref([])

function shiftLabel(t) { return { MORNING: '早班', AFTERNOON: '中班', EVENING: '晚班' }[t] || t }
function cleanLabel(t) { return { DAILY: '日常保洁', DEEP: '深度保洁' }[t] || t }
function scheduleStatusLabel(s) {
  return { PENDING: '待打卡', CHECKED_IN: '已打卡', SIGNED_OFF: '已签退', REJECTED: '整改中', FINISHED: '已完成' }[s] || s
}
function scheduleStatusTagType(s) {
  return { PENDING: 'info', CHECKED_IN: 'warning', SIGNED_OFF: 'primary', REJECTED: 'danger', FINISHED: 'success' }[s] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const params = { current: page.current, size: page.size }
    if (search.toiletId) params.toiletId = search.toiletId
    if (search.cleanerId) params.cleanerId = search.cleanerId
    const res = await getSchedulePage(params)
    tableData.value = res.data.records
    page.total = res.data.total
  } finally { loading.value = false }
}

function resetForm() { Object.assign(form, { toiletId: null, cleanerId: null, scheduleDate: '', shiftType: 'MORNING', cleanType: 'DAILY' }) }
function handleAdd() {
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await addSchedule({ ...form })
    ElMessage.success('新增排班成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败，请重试')
  } finally { submitting.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该排班记录吗？', '提示', { type: 'warning' })
    await deleteSchedule(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e?.toString() !== 'Cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

function handleScore(row) {
  currentScoreRow.value = row
  scoreForm.score = row.score || 60
  scoreForm.remark = ''
  scoreDialogVisible.value = true
}

async function handleScoreSubmit() {
  const valid = await scoreFormRef.value.validate().catch(() => false)
  if (!valid) return
  scoreSubmitting.value = true
  try {
    await scoreSchedule({ scheduleId: currentScoreRow.value.id, score: scoreForm.score, remark: scoreForm.remark })
    ElMessage.success('评分提交成功')
    scoreDialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '评分提交失败')
  } finally { scoreSubmitting.value = false }
}

onMounted(async () => {
  fetchData()
  try {
    const [toiletRes, cleanerRes] = await Promise.all([
      getToiletList(),
      getUserList('CLEANER')
    ])
    toiletList.value = toiletRes.data || []
    cleanerList.value = cleanerRes.data || []
  } catch { /* ignore */ }
})
</script>

<style scoped>
.page-crud { max-width: 1400px; }
.page-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}
.page-toolbar__search { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.page-pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
