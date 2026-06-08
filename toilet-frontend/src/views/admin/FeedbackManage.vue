<template>
  <div class="page-crud">
    <div class="page-card">
      <div class="page-toolbar">
        <div class="page-toolbar__search">
          <el-select v-model="search.toiletId" placeholder="选择公厕" clearable style="width: 180px; margin-right: 10px">
            <el-option v-for="t in toiletList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
          <el-button type="primary" @click="fetchData">
            <el-icon><Search /></el-icon> 查询
          </el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" empty-text="暂无反馈数据">
        <el-table-column label="反馈人" width="100">
          <template #default="{ row }">{{ row.userName || (row.userId ? '用户#' + row.userId : '未知') }}</template>
        </el-table-column>
        <el-table-column label="所属公厕" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.toiletName || '公厕#' + row.toiletId }}</template>
        </el-table-column>
        <el-table-column label="评分" width="220">
          <template #default="{ row }">
            <el-rate v-model="row.score" disabled show-score text-color="#FAAD14" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="反馈内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" width="170" />
      </el-table>

      <div class="page-pagination">
        <el-pagination
          v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total"
          layout="total, prev, pager, next, sizes" :page-sizes="[10, 20, 50]"
          @change="fetchData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getFeedbackPage, getToiletList } from '../../api'

const loading = ref(false)
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const search = reactive({ toiletId: null })
const toiletList = ref([])

async function fetchData() {
  loading.value = true
  try {
    const params = { current: page.current, size: page.size }
    if (search.toiletId) params.toiletId = search.toiletId
    const res = await getFeedbackPage(params)
    tableData.value = res.data.records
    page.total = res.data.total
  } finally { loading.value = false }
}

onMounted(async () => {
  fetchData()
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
