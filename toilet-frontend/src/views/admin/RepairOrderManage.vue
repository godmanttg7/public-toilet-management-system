<template>
  <div class="page-crud">
    <div class="page-card">
      <div class="page-toolbar">
        <div class="page-toolbar__search">
          <el-select v-model="search.status" placeholder="工单状态" clearable style="width: 130px; margin-right: 10px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="维修中" value="REPAIRING" />
            <el-option label="待验收" value="CHECKING" />
            <el-option label="已完成" value="FINISHED" />
          </el-select>
          <el-select v-model="search.toiletId" placeholder="选择公厕" clearable style="width: 180px; margin-right: 10px">
            <el-option v-for="t in toiletList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
          <el-button type="primary" @click="fetchData">
            <el-icon><Search /></el-icon> 查询
          </el-button>
        </div>
        <el-button type="primary" @click="handleReport">
          <el-icon><Warning /></el-icon> 上报故障
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" empty-text="暂无维修工单">
        <el-table-column prop="orderNo" label="工单编号" width="150" show-overflow-tooltip />
        <el-table-column label="所属公厕" min-width="100" show-overflow-tooltip>
          <template #default="{ row }">{{ row.toiletName || '公厕#' + row.toiletId }}</template>
        </el-table-column>
        <el-table-column label="故障设施" min-width="100">
          <template #default="{ row }">{{ row.facilityType || '设施#' + row.facilityId }}</template>
        </el-table-column>
        <el-table-column label="故障描述" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ truncate(row.faultDesc, 35) }}</template>
        </el-table-column>
        <el-table-column label="上报人" width="90">
          <template #default="{ row }">{{ row.reporterName || '用户#' + row.reporterId }}</template>
        </el-table-column>
        <el-table-column label="维修人" width="90">
          <template #default="{ row }">{{ row.assigneeName || (row.assigneeId ? '用户#' + row.assigneeId : '-') }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="plain" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="repairDesc" label="维修描述" min-width="120" show-overflow-tooltip />
        <el-table-column label="维修照片" width="110" align="center">
          <template #default="{ row }">
            <el-button v-if="row.images" text type="primary" size="small" @click="showImages(row)">查看照片</el-button>
            <span v-else style="color: #999; font-size: 12px;">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button text type="primary" size="small" @click="handleAssign(row)">指派</el-button>
              <el-button text type="danger" size="small" @click="handleCancel(row)">取消</el-button>
            </template>
            <template v-if="row.status === 'CHECKING'">
              <el-button text type="success" size="small" @click="handleAccept(row)">验收</el-button>
              <el-button text type="danger" size="small" @click="handleReject(row)">退回</el-button>
            </template>
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

    <!-- 上报故障弹窗 -->
    <el-dialog title="上报故障" v-model="reportDialogVisible" width="500px" @close="resetReportForm" destroy-on-close>
      <el-form :model="reportForm" ref="reportFormRef" :rules="reportFormRules" label-width="90px">
        <el-form-item label="所属公厕" prop="toiletId">
          <el-select v-model="reportForm.toiletId" placeholder="请选择公厕" filterable style="width: 100%">
            <el-option v-for="t in toiletList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障设施" prop="facilityId">
          <el-select v-model="reportForm.facilityId" placeholder="请选择设施" filterable style="width: 100%"
            :loading="facilityLoading" :disabled="!reportForm.toiletId">
            <el-option v-for="f in facilityList" :key="f.id"
              :label="f.facilityType + (f.brand ? ' - ' + f.brand : '') + (f.model ? ' (' + f.model + ')' : '')" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上报人" prop="reporterName">
          <el-input v-model="reportForm.reporterName" placeholder="填写上报人姓名，留空默认为当前用户" />
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDesc">
          <el-input v-model="reportForm.faultDesc" type="textarea" :rows="3" placeholder="请描述故障情况" />
        </el-form-item>
        <el-form-item label="现场照片">
          <el-upload :auto-upload="false" list-type="picture-card" :on-change="handleReportImageAdd"
            :file-list="reportForm.images" accept="image/jpeg,image/png,image/jpg">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReportSubmit" :loading="reportSubmitting">提交工单</el-button>
      </template>
    </el-dialog>

    <!-- 指派维修人员弹窗 -->
    <el-dialog title="指派维修人员" v-model="assignDialogVisible" width="440px" destroy-on-close>
      <el-form :model="assignForm" ref="assignFormRef" :rules="assignFormRules" label-width="90px">
        <el-form-item label="工单编号" v-if="currentRepairRow">
          <span style="color: #6B7280; font-size: 13px">{{ currentRepairRow.orderNo }}</span>
        </el-form-item>
        <el-form-item label="维修人员" prop="assigneeId">
          <el-select v-model="assignForm.assigneeId" placeholder="请选择维修人员" style="width: 100%">
            <el-option v-for="u in repairUsers" :key="u.id" :label="u.realName || u.username" :value="u.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSubmit" :loading="assignSubmitting">确认指派</el-button>
      </template>
    </el-dialog>
    <!-- 维修照片查看 -->
    <el-dialog title="维修照片" v-model="imageDialogVisible" width="600px" destroy-on-close>
      <div style="display: flex; flex-wrap: wrap; gap: 12px; justify-content: center;">
        <el-image v-for="(img, idx) in currentImages" :key="idx" :src="img" fit="contain"
          style="width: 200px; height: 200px; border: 1px solid #eee; border-radius: 8px;"
          :preview-src-list="currentImages" :initial-index="idx" preview-teleported />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRepairOrderPage, reportRepair, assignRepair, updateRepairStatus, cancelRepair, getUserList, getToiletList, getFacilityList, uploadImage } from '../../api'

const loading = ref(false)
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const search = reactive({ status: '', toiletId: null })

const reportDialogVisible = ref(false)
const reportSubmitting = ref(false)
const reportFormRef = ref(null)
const reportForm = reactive({ toiletId: null, facilityId: null, faultDesc: '', reporterName: '', images: [] })
const reportFormRules = {
  toiletId: [{ required: true, message: '请选择公厕', trigger: 'change' }],
  facilityId: [{ required: true, message: '请选择故障设施', trigger: 'change' }],
  faultDesc: [{ required: true, message: '请输入故障描述', trigger: 'blur' }]
}

const assignDialogVisible = ref(false)
const assignSubmitting = ref(false)
const assignFormRef = ref(null)
const currentRepairRow = ref(null)
const repairUsers = ref([])
const assignForm = reactive({ assigneeId: null })
const assignFormRules = {
  assigneeId: [{ required: true, message: '请选择维修人员', trigger: 'change' }]
}

// 维修照片查看
const imageDialogVisible = ref(false)
const currentImages = ref([])

function showImages(row) {
  if (row.images) {
    currentImages.value = row.images.split(',').filter(Boolean)
    imageDialogVisible.value = true
  }
}

// 公厕与设施下拉数据
const toiletList = ref([])
const facilityList = ref([])
const facilityLoading = ref(false)

// 选择公厕后动态加载设施
watch(() => reportForm.toiletId, async (newVal) => {
  reportForm.facilityId = null
  if (!newVal) { facilityList.value = []; return }
  facilityLoading.value = true
  try {
    const res = await getFacilityList({ toiletId: newVal })
    facilityList.value = res.data || []
  } catch { facilityList.value = [] }
  finally { facilityLoading.value = false }
})

function truncate(str, len) { return str && str.length > len ? str.slice(0, len) + '...' : (str || '') }
function statusLabel(s) { return { PENDING: '待处理', REPAIRING: '维修中', CHECKING: '待验收', FINISHED: '已完成', CANCELLED: '已取消' }[s] || s }
function statusTagType(s) { return { PENDING: 'warning', REPAIRING: 'primary', CHECKING: 'info', FINISHED: 'success', CANCELLED: 'danger' }[s] || 'info' }

async function fetchData() {
  loading.value = true
  try {
    const params = { current: page.current, size: page.size }
    if (search.status) params.status = search.status
    if (search.toiletId) params.toiletId = search.toiletId
    const res = await getRepairOrderPage(params)
    tableData.value = res.data.records
    page.total = res.data.total
  } finally { loading.value = false }
}

function resetReportForm() {
  Object.assign(reportForm, { toiletId: null, facilityId: null, faultDesc: '', reporterName: '', images: [] })
}
function handleReport() { reportDialogVisible.value = true }

function handleReportImageAdd(file) {
  reportForm.images.push(file.raw)
  return false
}

async function handleReportSubmit() {
  const valid = await reportFormRef.value.validate().catch(() => false)
  if (!valid) return
  reportSubmitting.value = true
  try {
    // 先上传图片
    let imageUrls = []
    for (const file of reportForm.images) {
      const res = await uploadImage(file)
      if (res.data) imageUrls.push(res.data)
    }
    await reportRepair({
      toiletId: reportForm.toiletId,
      facilityId: reportForm.facilityId,
      faultDesc: reportForm.faultDesc,
      reporterName: reportForm.reporterName,
      images: imageUrls.join(',')
    })
    ElMessage.success('故障上报成功')
    reportDialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '提交工单失败')
  } finally { reportSubmitting.value = false }
}

async function handleAssign(row) {
  currentRepairRow.value = row
  assignForm.assigneeId = null
  try {
    const res = await getUserList('REPAIR')
    repairUsers.value = res.data || []
  } catch { repairUsers.value = [] }
  assignDialogVisible.value = true
}

async function handleAssignSubmit() {
  const valid = await assignFormRef.value.validate().catch(() => false)
  if (!valid) return
  assignSubmitting.value = true
  try {
    await assignRepair({ orderId: currentRepairRow.value.id, assigneeId: assignForm.assigneeId })
    ElMessage.success('指派成功')
    assignDialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '指派失败')
  } finally { assignSubmitting.value = false }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确定取消该工单吗？', '提示', { type: 'warning' })
    await cancelRepair(row.id)
    ElMessage.success('工单已取消')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e?.toString() !== 'Cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

async function handleAccept(row) {
  try {
    await ElMessageBox.confirm('确定验收通过该维修工单吗？', '验收确认', { type: 'success', confirmButtonText: '验收通过', cancelButtonText: '取消' })
    await updateRepairStatus({ orderId: row.id, newStatus: 'FINISHED' })
    ElMessage.success('验收通过')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e?.toString() !== 'Cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

async function handleReject(row) {
  try {
    await ElMessageBox.confirm('确定退回该工单吗？退回后将重新进入维修中状态。', '退回确认', { type: 'warning', confirmButtonText: '确定退回', cancelButtonText: '取消' })
    await updateRepairStatus({ orderId: row.id, newStatus: 'REPAIRING' })
    ElMessage.success('已退回')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e?.toString() !== 'Cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

onMounted(async () => {
  fetchData()
  // 加载公厕列表供下拉选择
  try {
    const res = await getToiletList()
    toiletList.value = res.data || []
  } catch { toiletList.value = [] }
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
