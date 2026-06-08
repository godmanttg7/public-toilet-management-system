<template>
  <div class="page-crud">
    <div class="page-card">
      <div class="page-toolbar">
        <div class="page-toolbar__search">
          <el-input v-model="search.name" placeholder="搜索公厕名称" clearable style="width: 200px; margin-right: 10px" />
          <el-input v-model="search.district" placeholder="搜索区域" clearable style="width: 150px; margin-right: 10px" />
          <el-button type="primary" @click="fetchData">
            <el-icon><Search /></el-icon> 查询
          </el-button>
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增公厕
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" empty-text="暂无公厕数据">
        <el-table-column prop="name" label="公厕名称" width="180" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="district" label="区域" width="100" />
        <el-table-column prop="openTime" label="开放时间" width="120" />
        <el-table-column label="厕位" width="100">
          <template #default="{ row }">
            <span class="toilet-count">
              <span class="toilet-count__male">{{ row.maleCount }}</span>/
              <span class="toilet-count__female">{{ row.femaleCount }}</span>/
              <span class="toilet-count__accessible">{{ row.accessibleCount }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="manageUnit" label="管理单位" width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain" size="small">
              {{ row.status === 1 ? '运营中' : '已停用' }}
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
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :total="page.total"
          layout="total, prev, pager, next, sizes"
          :page-sizes="[10, 20, 50]"
          @change="fetchData"
        />
      </div>
    </div>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="650px" @close="resetForm" destroy-on-close>
      <el-form :model="form" ref="formRef" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="公厕名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详细地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入详细地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input v-model="form.longitude" placeholder="如 120.123456" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input v-model="form.latitude" placeholder="如 30.123456" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属区域">
              <el-input v-model="form.district" placeholder="如 滨江区" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开放时间">
              <el-input v-model="form.openTime" placeholder="如 06:00-22:00" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="男厕位数">
              <el-input-number v-model="form.maleCount" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="女厕位数">
              <el-input-number v-model="form.femaleCount" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="无障碍位数">
              <el-input-number v-model="form.accessibleCount" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="管理单位">
              <el-input v-model="form.manageUnit" placeholder="请输入管理单位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">正常</el-radio>
                <el-radio :label="0">停用</el-radio>
              </el-radio-group>
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
import { getToiletPage, addToilet, updateToilet, deleteToilet } from '../../api'

const loading = ref(false)
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const search = reactive({ name: '', district: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增公厕')
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

const form = reactive({
  id: null, name: '', address: '', longitude: null, latitude: null,
  district: '', openTime: '', maleCount: 0, femaleCount: 0, accessibleCount: 0,
  manageUnit: '', phone: '', status: 1
})

const formRules = {
  name: [{ required: true, message: '请输入公厕名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  longitude: [{ required: true, message: '请输入经度', trigger: 'blur' }],
  latitude: [{ required: true, message: '请输入纬度', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getToiletPage({ current: page.current, size: page.size, ...search })
    tableData.value = res.data.records
    page.total = res.data.total
  } finally { loading.value = false }
}

function resetForm() {
  Object.assign(form, {
    id: null, name: '', address: '', longitude: '', latitude: '',
    district: '', openTime: '', maleCount: 0, femaleCount: 0, accessibleCount: 0,
    manageUnit: '', phone: '', status: 1
  })
  isEdit.value = false
}

function handleAdd() { dialogTitle.value = '新增公厕'; dialogVisible.value = true }
function handleEdit(row) {
  dialogTitle.value = '编辑公厕'
  Object.assign(form, {
    id: row.id, name: row.name, address: row.address,
    longitude: row.longitude, latitude: row.latitude, district: row.district,
    openTime: row.openTime, maleCount: row.maleCount, femaleCount: row.femaleCount,
    accessibleCount: row.accessibleCount, manageUnit: row.manageUnit, phone: row.phone, status: row.status
  })
  isEdit.value = true
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) { await updateToilet(form) } else { await addToilet(form) }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败，请重试')
  } finally { submitting.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该公厕吗？', '提示', { type: 'warning' })
    await deleteToilet(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel' && e?.toString() !== 'Cancel') {
      ElMessage.error(e.message || '删除失败')
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
.page-pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.toilet-count { font-family: 'SF Mono', 'Menlo', monospace; font-size: 13px; }
.toilet-count__male { color: #1890FF; }
.toilet-count__female { color: #EB2F96; }
.toilet-count__accessible { color: #52C41A; }
</style>
