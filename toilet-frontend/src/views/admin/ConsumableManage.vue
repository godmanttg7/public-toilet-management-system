<template>
  <div class="page-crud">
    <div class="page-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- ==================== 库存管理 ==================== -->
        <el-tab-pane label="库存管理" name="stock">
          <div class="page-toolbar">
            <div class="page-toolbar__search">
              <el-select v-model="stockSearch.toiletId" placeholder="选择公厕" clearable style="width: 180px; margin-right: 10px">
                <el-option v-for="t in toiletList" :key="t.id" :label="t.name" :value="t.id" />
              </el-select>
              <el-button type="primary" @click="fetchStock">
                <el-icon><Search /></el-icon> 查询
              </el-button>
            </div>
            <div class="page-toolbar__actions">
              <el-badge :value="alertCount" :hidden="alertCount === 0" style="margin-right: 10px">
                <el-button type="warning" plain @click="showAlertList">
                  <el-icon><Bell /></el-icon> 库存预警
                </el-button>
              </el-badge>
              <el-button type="primary" @click="handleStockIn">
                <el-icon><Plus /></el-icon> 入库
              </el-button>
              <el-button type="danger" plain style="margin-left: 8px" @click="handleStockOut">
                <el-icon><Minus /></el-icon> 出库
              </el-button>
            </div>
          </div>

          <el-table :data="stockData" border stripe v-loading="stockLoading" empty-text="暂无库存数据"
            :row-class-name="stockRowClass">
            <el-table-column label="所属公厕" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">{{ row.toiletName || '公厕#' + row.toiletId }}</template>
            </el-table-column>
            <el-table-column prop="consumableName" label="耗材名称" width="140" show-overflow-tooltip />
            <el-table-column prop="unit" label="单位" width="60" align="center" />
            <el-table-column label="当前库存" width="100" align="center">
              <template #default="{ row }">
                <span :class="{ 'text-danger': row.currentStock < row.minStock, 'text-bold': row.currentStock < row.minStock }">
                  {{ row.currentStock }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="minStock" label="最低库存" width="90" align="center" />
            <el-table-column label="库存状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.currentStock === 0" type="danger" effect="plain" size="small">已用完</el-tag>
                <el-tag v-else-if="row.currentStock < row.minStock" type="warning" effect="plain" size="small">库存不足</el-tag>
                <el-tag v-else type="success" effect="plain" size="small">正常</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="170" />
          </el-table>

          <div class="page-pagination">
            <el-pagination
              v-model:current-page="stockPage.current" v-model:page-size="stockPage.size" :total="stockPage.total"
              layout="total, prev, pager, next, sizes" :page-sizes="[10, 20, 50]" @change="fetchStock"
            />
          </div>
        </el-tab-pane>

        <!-- ==================== 出入库记录 ==================== -->
        <el-tab-pane label="出入库记录" name="records">
          <div class="page-toolbar">
            <div class="page-toolbar__search">
              <el-select v-model="recordSearch.toiletId" placeholder="选择公厕" clearable style="width: 180px; margin-right: 10px">
                <el-option v-for="t in toiletList" :key="t.id" :label="t.name" :value="t.id" />
              </el-select>
              <el-button type="primary" @click="fetchRecords">
                <el-icon><Search /></el-icon> 查询
              </el-button>
            </div>
          </div>

          <el-table :data="recordData" border stripe v-loading="recordLoading" empty-text="暂无出入库记录">
            <el-table-column label="所属公厕" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">{{ row.toiletName || '公厕#' + row.toiletId }}</template>
            </el-table-column>
            <el-table-column prop="consumableName" label="耗材名称" width="140" show-overflow-tooltip />
            <el-table-column prop="quantity" label="数量" width="80" align="center" />
            <el-table-column label="类型" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.type === 'IN' ? 'success' : 'danger'" effect="plain" size="small">
                  {{ row.type === 'IN' ? '入库' : '出库' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作人" width="100">
              <template #default="{ row }">{{ row.operatorName || (row.operatorId ? '用户#' + row.operatorId : '未知') }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="操作时间" width="170" />
          </el-table>

          <div class="page-pagination">
            <el-pagination
              v-model:current-page="recordPage.current" v-model:page-size="recordPage.size" :total="recordPage.total"
              layout="total, prev, pager, next, sizes" :page-sizes="[10, 20, 50]" @change="fetchRecords"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 入库弹窗 -->
    <el-dialog title="入库" v-model="stockInDialogVisible" width="420px" @close="resetStockInForm" destroy-on-close>
      <el-form :model="stockInForm" ref="stockInFormRef" :rules="moveFormRules" label-width="90px">
        <el-form-item label="所属公厕" prop="toiletId">
          <el-select v-model="stockInForm.toiletId" placeholder="选择公厕" filterable style="width: 100%">
            <el-option v-for="t in toiletList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="耗材名称" prop="consumableName">
          <el-select v-model="stockInForm.consumableName" placeholder="选择或输入耗材名称" filterable allow-create clearable style="width: 100%">
            <el-option v-for="name in consumableNameList" :key="name" :label="name" :value="name" />
          </el-select>
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="stockInForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockInDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStockInSubmit" :loading="stockInSubmitting">确认入库</el-button>
      </template>
    </el-dialog>

    <!-- 出库弹窗 -->
    <el-dialog title="出库" v-model="stockOutDialogVisible" width="420px" @close="resetStockOutForm" destroy-on-close>
      <el-form :model="stockOutForm" ref="stockOutFormRef" :rules="moveFormRules" label-width="90px">
        <el-form-item label="所属公厕" prop="toiletId">
          <el-select v-model="stockOutForm.toiletId" placeholder="选择公厕" filterable style="width: 100%">
            <el-option v-for="t in toiletList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="耗材名称" prop="consumableName">
          <el-select v-model="stockOutForm.consumableName" placeholder="选择耗材名称" filterable clearable style="width: 100%">
            <el-option v-for="name in consumableNameList" :key="name" :label="name" :value="name" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number v-model="stockOutForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockOutDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStockOutSubmit" :loading="stockOutSubmitting">确认出库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConsumablePage, getConsumableAlerts, stockIn, stockOut, getConsumableRecordPage, getToiletList, getConsumableNames } from '../../api'

const activeTab = ref('stock')

const stockLoading = ref(false)
const stockData = ref([])
const stockPage = reactive({ current: 1, size: 10, total: 0 })
const stockSearch = reactive({ toiletId: null })
const toiletList = ref([])
const alertCount = ref(0)

const stockInDialogVisible = ref(false)
const stockInSubmitting = ref(false)
const stockInFormRef = ref(null)
const stockInForm = reactive({ toiletId: null, consumableName: '', quantity: null })

const stockOutDialogVisible = ref(false)
const stockOutSubmitting = ref(false)
const stockOutFormRef = ref(null)
const stockOutForm = reactive({ toiletId: null, consumableName: '', quantity: null })

const consumableNameList = ref([])

const moveFormRules = {
  toiletId: [{ required: true, message: '请选择公厕', trigger: 'change' }],
  consumableName: [{ required: true, message: '请选择或输入耗材名称', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

const recordLoading = ref(false)
const recordData = ref([])
const recordPage = reactive({ current: 1, size: 10, total: 0 })
const recordSearch = reactive({ toiletId: null })

function stockRowClass({ row }) { return row.currentStock < row.minStock ? 'stock-warning-row' : '' }

async function fetchAlerts() {
  try { const res = await getConsumableAlerts(); alertCount.value = Array.isArray(res.data) ? res.data.length : (res.data || 0) }
  catch { alertCount.value = 0 }
}

async function showAlertList() {
  stockLoading.value = true
  try {
    const res = await getConsumableAlerts()
    const alerts = Array.isArray(res.data) ? res.data : []
    if (alerts.length === 0) {
      ElMessage.info('当前没有库存不足的耗材')
      fetchStock()
      return
    }
    stockData.value = alerts
    stockPage.total = alerts.length
  } catch {
    ElMessage.error('获取预警数据失败')
    fetchStock()
  } finally { stockLoading.value = false }
}

async function fetchStock() {
  stockLoading.value = true
  try {
    const params = { current: stockPage.current, size: stockPage.size }
    if (stockSearch.toiletId) params.toiletId = stockSearch.toiletId
    const res = await getConsumablePage(params)
    stockData.value = res.data.records
    stockPage.total = res.data.total
  } finally { stockLoading.value = false }
}

async function fetchConsumableNames() {
  try {
    const res = await getConsumableNames()
    consumableNameList.value = res.data || []
  } catch { consumableNameList.value = [] }
}

async function fetchRecords() {
  recordLoading.value = true
  try {
    const params = { current: recordPage.current, size: recordPage.size }
    if (recordSearch.toiletId) params.toiletId = recordSearch.toiletId
    const res = await getConsumableRecordPage(params)
    recordData.value = res.data.records
    recordPage.total = res.data.total
  } finally { recordLoading.value = false }
}

function handleTabChange(tab) {
  if (tab === 'stock') { fetchStock(); fetchAlerts(); fetchConsumableNames() }
  else if (tab === 'records') { fetchRecords() }
}

function resetStockInForm() { Object.assign(stockInForm, { toiletId: null, consumableName: '', quantity: null }) }
function handleStockIn() { stockInDialogVisible.value = true }

async function handleStockInSubmit() {
  const valid = await stockInFormRef.value.validate().catch(() => false)
  if (!valid) return
  stockInSubmitting.value = true
  try {
    await stockIn({ ...stockInForm })
    ElMessage.success('入库成功')
    stockInDialogVisible.value = false
    fetchStock()
    fetchAlerts()
    fetchConsumableNames()
  } catch (e) {
    ElMessage.error(e.message || '入库失败')
  } finally { stockInSubmitting.value = false }
}

function resetStockOutForm() { Object.assign(stockOutForm, { toiletId: null, consumableName: '', quantity: null }) }
function handleStockOut() { stockOutDialogVisible.value = true }

async function handleStockOutSubmit() {
  const valid = await stockOutFormRef.value.validate().catch(() => false)
  if (!valid) return
  stockOutSubmitting.value = true
  try {
    await stockOut({ ...stockOutForm })
    ElMessage.success('出库成功')
    stockOutDialogVisible.value = false
    fetchStock()
    fetchAlerts()
    fetchConsumableNames()
  } catch (e) {
    ElMessage.error(e.message || '出库失败')
  } finally { stockOutSubmitting.value = false }
}

onMounted(async () => {
  fetchStock(); fetchAlerts(); fetchConsumableNames()
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
.page-toolbar__actions { display: flex; align-items: center; }
.page-pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.text-danger { color: #FF4D4F; }
.text-bold { font-weight: 600; }
:deep(.stock-warning-row) { background-color: #FFF2F0 !important; }
</style>
