<template>
  <div class="page-crud">
    <div class="page-card">
      <div class="page-toolbar">
        <div class="page-toolbar__search">
          <el-input v-model="search.realName" placeholder="搜索姓名" clearable style="width: 160px; margin-right: 10px" />
          <el-select v-model="search.role" placeholder="角色" clearable style="width: 140px; margin-right: 10px">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="保洁人员" value="CLEANER" />
            <el-option label="维修人员" value="REPAIR" />
          </el-select>
          <el-button type="primary" @click="fetchData">
            <el-icon><Search /></el-icon> 查询
          </el-button>
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增用户
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" empty-text="暂无用户数据">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="110" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column label="角色" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'CLEANER' ? 'success' : 'warning'"
              effect="plain" size="small">
              {{ row.role === 'ADMIN' ? '管理员' : row.role === 'CLEANER' ? '保洁人员' : '维修人员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button text type="warning" size="small" @click="handleResetPwd(row)">重置密码</el-button>
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" @close="resetForm" destroy-on-close>
      <el-form :model="form" ref="formRef" :rules="formRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="登录密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="留空则默认 123456" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="保洁人员" value="CLEANER" />
            <el-option label="维修人员" value="REPAIR" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
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
import { getUserPage, addUser, updateUser, deleteUser, resetPassword } from '../../api'

const loading = ref(false)
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const search = reactive({ realName: '', role: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const form = reactive({ id: null, username: '', password: '', realName: '', phone: '', role: 'CLEANER', status: 1 })
const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserPage({ current: page.current, size: page.size, ...search })
    tableData.value = res.data.records
    page.total = res.data.total
  } finally { loading.value = false }
}

function handleAdd() {
  dialogTitle.value = '新增用户'
  resetForm()
  dialogVisible.value = true
}

function resetForm() {
  Object.assign(form, { id: null, username: '', password: '', realName: '', phone: '', role: 'CLEANER', status: 1 })
  isEdit.value = false
}
function handleEdit(row) {
  dialogTitle.value = '编辑用户'
  Object.assign(form, {
    id: row.id, username: row.username, realName: row.realName,
    phone: row.phone, role: row.role, status: row.status
  })
  isEdit.value = true
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) { await updateUser(form) } else { await addUser(form) }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败，请重试')
  } finally { submitting.value = false }
}

async function handleResetPwd(row) {
  try {
    await ElMessageBox.confirm(`确定要重置 ${row.realName} 的密码为 123456 吗？`, '提示', { type: 'warning' })
    await resetPassword(row.id)
    ElMessage.success('密码已重置为 123456')
  } catch { /* 用户取消或操作失败 */ }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* 用户取消或操作失败 */ }
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
</style>
