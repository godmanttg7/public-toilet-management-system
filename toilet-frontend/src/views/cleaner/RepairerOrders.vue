<template>
  <div class="page-crud">
    <div class="page-card">
      <div class="page-card__header">
        <h2 class="page-card__title">我的维修工单</h2>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" empty-text="暂无维修工单">
        <el-table-column prop="orderNo" label="工单编号" width="180" show-overflow-tooltip />
        <el-table-column label="所属公厕" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.toiletName || '公厕#' + row.toiletId }}</template>
        </el-table-column>
        <el-table-column label="故障设施" min-width="120">
          <template #default="{ row }">{{ row.facilityType || '设施#' + row.facilityId }}</template>
        </el-table-column>
        <el-table-column prop="faultDesc" label="故障描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="plain" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上报时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'REPAIRING'" type="primary" size="small"
              @click="handleFinish(row)">
              完成维修
            </el-button>
            <el-tag v-else-if="row.status === 'CHECKING'" type="warning" effect="plain" size="small">等待验收</el-tag>
            <el-tag v-else-if="row.status === 'FINISHED'" type="success" effect="plain" size="small">已完成</el-tag>
            <el-tag v-else type="info" effect="plain" size="small">待处理</el-tag>
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

    <!-- 完成维修弹窗 -->
    <el-dialog title="填写维修记录" v-model="dialogVisible" width="560px" destroy-on-close>
      <el-form :model="repairForm" label-width="80px">
        <el-form-item label="工单编号" v-if="currentOrder">
          <span style="color: #6B7280; font-size: 13px">{{ currentOrder.orderNo }}</span>
        </el-form-item>
        <el-form-item label="维修描述">
          <el-input v-model="repairForm.repairDesc" type="textarea" :rows="4"
            placeholder="请描述维修内容、更换的配件等信息" />
        </el-form-item>
        <el-form-item label="维修照片">
          <el-upload :auto-upload="false" list-type="picture-card" :on-change="handleImageAdd"
            :file-list="repairForm.images" accept="image/jpeg,image/png,image/jpg" multiple>
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFinish" :loading="submitting">提交验收</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyRepairOrders, updateRepairStatus, uploadImage } from '../../api'

const loading = ref(false)
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const dialogVisible = ref(false)
const submitting = ref(false)
const currentOrder = ref(null)
const repairForm = reactive({ repairDesc: '', images: [] })
const uploading = ref(false)

function handleImageAdd(file) {
  repairForm.images.push(file.raw)
  return false
}

function statusText(s) { return { PENDING: '待处理', REPAIRING: '维修中', CHECKING: '待验收', FINISHED: '已完成' }[s] || s }
function statusType(s) { return { PENDING: 'warning', REPAIRING: 'primary', CHECKING: 'info', FINISHED: 'success' }[s] || 'info' }

async function fetchData() {
  loading.value = true
  try {
    const res = await getMyRepairOrders({ current: page.current, size: page.size })
    tableData.value = res.data.records
    page.total = res.data.total
  } finally { loading.value = false }
}

function handleFinish(row) {
  currentOrder.value = row
  repairForm.repairDesc = ''
  repairForm.images = []
  repairForm.imageUrlInput = ''
  dialogVisible.value = true
}

async function submitFinish() {
  if (!repairForm.repairDesc) {
    ElMessage.warning('请填写维修描述')
    return
  }
  submitting.value = true
  try {
    let imageUrls = []
    for (const file of repairForm.images) {
      const res = await uploadImage(file)
      if (res.data) imageUrls.push(res.data)
    }
    await updateRepairStatus({
      orderId: currentOrder.value.id,
      newStatus: 'CHECKING',
      repairDesc: repairForm.repairDesc,
      images: imageUrls.join(',')
    })
    ElMessage.success('已提交验收申请')
    dialogVisible.value = false
    fetchData()
  } finally { submitting.value = false }
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
.upload-area { width: 100%; }
.upload-images { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 10px; }
.upload-image-item { position: relative; }
.upload-input { display: flex; align-items: center; }
.page-card__header { margin-bottom: 16px; }
.page-card__title {
  font-size: 18px;
  font-weight: 600;
  color: #1F2937;
  margin: 0;
}
.page-pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
