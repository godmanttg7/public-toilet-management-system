<template>
  <div class="page-crud">
    <div class="page-card">
      <div class="page-toolbar">
        <div class="page-toolbar__search">
          <el-select v-model="search.status" placeholder="状态筛选" clearable style="width: 130px; margin-right: 10px">
            <el-option label="全部" value="" />
            <el-option label="已上报" value="PENDING" />
            <el-option label="已处理" value="RESOLVED" />
          </el-select>
          <el-button type="primary" @click="fetchData">
            <el-icon><Search /></el-icon> 查询
          </el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" empty-text="暂无情况汇报">
        <el-table-column label="所属公厕" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.toiletName || '公厕#' + row.toiletId }}</template>
        </el-table-column>
        <el-table-column prop="reportType" label="汇报类型" width="100" align="center" />
        <el-table-column prop="description" label="情况描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="汇报人" width="100">
          <template #default="{ row }">{{ row.reporterName || (row.reporterId ? '用户#' + row.reporterId : '未知') }}</template>
        </el-table-column>
        <el-table-column label="现场照片" width="100" align="center">
          <template #default="{ row }">
            <el-button v-if="row.images" text type="primary" size="small" @click="showImages(row)">查看照片</el-button>
            <span v-else style="color: #999; font-size: 12px;">无</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'RESOLVED' ? 'success' : 'warning'" effect="plain" size="small">
              {{ row.status === 'RESOLVED' ? '已处理' : '已上报' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="汇报时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="handleResolve(row)">处理完成</el-button>
            <span v-else style="color: #999; font-size: 12px;">-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 照片查看 -->
    <el-dialog title="现场照片" v-model="imageDialogVisible" width="600px" destroy-on-close>
      <div style="display: flex; flex-wrap: wrap; gap: 12px; justify-content: center;">
        <el-image v-for="(img, idx) in currentImages" :key="idx" :src="img" fit="contain"
          style="width: 200px; height: 200px; border: 1px solid #eee; border-radius: 8px;"
          :preview-src-list="currentImages" :initial-index="idx" preview-teleported />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getIncidentPage, resolveIncident } from '../../api'

const loading = ref(false)
const tableData = ref([])
const search = reactive({ status: '' })
const imageDialogVisible = ref(false)
const currentImages = ref([])

function showImages(row) {
  if (row.images) {
    currentImages.value = row.images.split(',').filter(Boolean)
    imageDialogVisible.value = true
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = { current: 1, size: 100 }
    if (search.status) params.status = search.status
    const res = await getIncidentPage(params)
    tableData.value = res.data?.records || []
  } finally { loading.value = false }
}

async function handleResolve(row) {
  try {
    await ElMessageBox.confirm('确认已处理该情况？', '提示', { type: 'success', confirmButtonText: '确认处理完成' })
    await resolveIncident(row.id)
    ElMessage.success('已标记为已处理')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e?.toString() !== 'Cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

onMounted(() => fetchData())
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
</style>
