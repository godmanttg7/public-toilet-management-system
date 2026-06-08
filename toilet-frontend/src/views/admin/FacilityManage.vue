<template>
  <div class="page-crud">
    <div class="page-card">
      <div class="page-toolbar">
        <div class="page-toolbar__search">
          <el-select v-model="search.toiletId" placeholder="选择公厕" clearable style="width: 180px; margin-right: 10px">
            <el-option v-for="t in toiletList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
          <el-input v-model="search.facilityType" placeholder="设施类型" clearable style="width: 150px; margin-right: 10px" />
          <el-select v-model="search.status" placeholder="状态" clearable style="width: 130px; margin-right: 10px">
            <el-option label="正常" value="NORMAL" />
            <el-option label="故障" value="FAULT" />
            <el-option label="维修中" value="REPAIR" />
          </el-select>
          <el-button type="primary" @click="fetchData">
            <el-icon><Search /></el-icon> 查询
          </el-button>
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增设施
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" empty-text="暂无设施记录">
        <el-table-column label="所属公厕" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.toiletName || '公厕#' + row.toiletId }}</template>
        </el-table-column>
        <el-table-column prop="facilityType" label="设施类型" width="120" show-overflow-tooltip />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="model" label="型号" width="130" show-overflow-tooltip />
        <el-table-column prop="installDate" label="安装日期" width="120" />
        <el-table-column prop="warrantyDate" label="保修截止" width="120" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="plain" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="560px" @close="resetForm" destroy-on-close>
      <el-form :model="form" ref="formRef" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属公厕" prop="toiletId">
              <el-select v-model="form.toiletId" placeholder="选择公厕" filterable style="width: 100%">
                <el-option v-for="t in toiletList" :key="t.id" :label="t.name" :value="t.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设施类型" prop="facilityType">
              <el-input v-model="form.facilityType" placeholder="如 洗手盆、小便器" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="form.brand" placeholder="请输入品牌" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号" prop="model">
              <el-input v-model="form.model" placeholder="请输入型号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安装日期" prop="installDate">
              <el-date-picker v-model="form.installDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保修截止" prop="warrantyDate">
              <el-date-picker v-model="form.warrantyDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="正常" value="NORMAL" />
                <el-option label="故障" value="FAULT" />
                <el-option label="维修中" value="REPAIR" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFacilityPage, addFacility, updateFacility, deleteFacility, getToiletList } from '../../api'

const loading = ref(false)
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const search = reactive({ toiletId: null, facilityType: '', status: '' })
const toiletList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增设施')
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const form = reactive({ id: null, toiletId: null, facilityType: '', brand: '', model: '', installDate: '', warrantyDate: '', status: 'NORMAL' })
const formRules = {
  toiletId: [{ required: true, message: '请选择公厕', trigger: 'change' }],
  facilityType: [{ required: true, message: '请输入设施类型', trigger: 'blur' }],
  brand: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
  model: [{ required: true, message: '请输入型号', trigger: 'blur' }]
}

function statusLabel(s) { return { NORMAL: '正常', FAULT: '故障', REPAIR: '维修中' }[s] || s }
function statusTagType(s) { return { NORMAL: 'success', FAULT: 'danger', REPAIR: 'warning' }[s] || 'info' }

async function fetchData() {
  loading.value = true
  try {
    const params = { current: page.current, size: page.size }
    if (search.toiletId) params.toiletId = search.toiletId
    if (search.facilityType) params.facilityType = search.facilityType
    if (search.status) params.status = search.status
    const res = await getFacilityPage(params)
    tableData.value = res.data.records
    page.total = res.data.total
  } finally { loading.value = false }
}

function resetForm() {
  Object.assign(form, { id: null, toiletId: null, facilityType: '', brand: '', model: '', installDate: '', warrantyDate: '', status: 'NORMAL' })
  isEdit.value = false
}
function handleAdd() { dialogTitle.value = '新增设施'; dialogVisible.value = true }
function handleEdit(row) {
  dialogTitle.value = '编辑设施'
  Object.assign(form, {
    id: row.id, toiletId: row.toiletId, facilityType: row.facilityType,
    brand: row.brand, model: row.model, installDate: row.installDate,
    warrantyDate: row.warrantyDate, status: row.status
  })
  isEdit.value = true
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) { await updateFacility({ ...form }) } else { await addFacility({ ...form }) }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败，请重试')
  } finally { submitting.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该设施吗？', '提示', { type: 'warning' })
    await deleteFacility(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e?.toString() !== 'Cancel' && !(e instanceof Error && e.message?.includes('cancel'))) {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

onMounted(async () => {
  fetchData()
  try { const res = await getToiletList(); toiletList.value = res.data || [] } catch { toiletList.value = [] }
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
