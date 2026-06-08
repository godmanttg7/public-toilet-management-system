<template>
  <div class="page-crud">
    <div class="page-card">
      <div class="page-card__header">
        <h2 class="page-card__title">情况汇报</h2>
        <p class="page-card__desc">遇到设备异常、安全隐患或其他特殊情况时，可在此处汇报</p>
      </div>

      <el-form :model="form" ref="formRef" :rules="formRules" label-width="90px">
        <el-form-item label="所属公厕" prop="toiletId">
          <el-select v-model="form.toiletId" placeholder="请选择公厕" filterable style="width: 100%">
            <el-option v-for="t in toiletList" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="汇报类型" prop="reportType">
          <el-select v-model="form.reportType" placeholder="请选择类型" style="width: 100%">
            <el-option label="设备异常" value="设备异常" />
            <el-option label="安全隐患" value="安全隐患" />
            <el-option label="物资短缺" value="物资短缺" />
            <el-option label="卫生问题" value="卫生问题" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="情况描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="5"
            placeholder="请详细描述您遇到的情况，包括时间、地点、具体情况等" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="现场照片">
          <div class="upload-area">
            <div class="upload-images" v-if="form.images.length > 0">
              <div class="upload-image-item" v-for="(img, idx) in form.images" :key="idx">
                <el-image :src="img" fit="cover" style="width: 80px; height: 80px; border-radius: 4px;" />
                <el-button size="small" type="danger" circle @click="removeImage(idx)"
                  style="position: absolute; top: -8px; right: -8px; width: 20px; height: 20px;">
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
            </div>
            <div class="upload-input">
              <el-upload
                :show-file-list="false"
                :before-upload="handleFileUpload"
                accept="image/*"
                style="margin-right: 8px"
              >
                <el-button type="primary" size="small" :loading="uploading">
                  <el-icon><Upload /></el-icon> 选择图片
                </el-button>
              </el-upload>
              <el-input v-model="imageUrlInput" placeholder="或粘贴图片URL，回车添加" size="small"
                @keyup.enter="addImage" style="width: 300px; margin-right: 8px" />
              <el-button size="small" @click="addImage">添加</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
            提交汇报
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 我的汇报记录 -->
    <div class="page-card" style="margin-top: 20px;">
      <h3 class="page-card__title" style="margin-bottom: 16px;">我的汇报记录</h3>
      <el-table :data="myReports" border stripe v-loading="reportLoading" empty-text="暂无汇报记录">
        <el-table-column label="所属公厕" min-width="120">
          <template #default="{ row }">{{ row.toiletName || '公厕#' + row.toiletId }}</template>
        </el-table-column>
        <el-table-column prop="reportType" label="类型" width="100" align="center" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'RESOLVED' ? 'success' : 'warning'" effect="plain" size="small">
              {{ row.status === 'RESOLVED' ? '已处理' : '已上报' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="汇报时间" width="170" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getToiletList, reportIncident, getMyIncidents, uploadImage } from '../../api'

const formRef = ref(null)
const submitting = ref(false)
const toiletList = ref([])
const imageUrlInput = ref('')
const uploading = ref(false)

const form = reactive({
  toiletId: null,
  reportType: '',
  description: '',
  images: []
})

const formRules = {
  toiletId: [{ required: true, message: '请选择公厕', trigger: 'change' }],
  reportType: [{ required: true, message: '请选择汇报类型', trigger: 'change' }],
  description: [{ required: true, message: '请描述具体情况', trigger: 'blur' }]
}

const reportLoading = ref(false)
const myReports = ref([])

function addImage() {
  const url = imageUrlInput.value?.trim()
  if (!url) return
  if (!form.images.includes(url)) {
    form.images.push(url)
  }
  imageUrlInput.value = ''
}

function removeImage(idx) {
  form.images.splice(idx, 1)
}

async function handleFileUpload(file) {
  uploading.value = true
  try {
    const res = await uploadImage(file)
    if (res.data) {
      form.images.push(res.data)
    }
  } catch (e) {
    ElMessage.error('上传失败：' + (e.message || '未知错误'))
  } finally {
    uploading.value = false
  }
  return false
}

async function fetchMyReports() {
  reportLoading.value = true
  try {
    const res = await getMyIncidents({ current: 1, size: 50 })
    myReports.value = res.data?.records || []
  } catch { myReports.value = [] }
  finally { reportLoading.value = false }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await reportIncident({
      toiletId: form.toiletId,
      reportType: form.reportType,
      description: form.description,
      images: form.images.join(',')
    })
    ElMessage.success('汇报已提交，管理员将尽快处理')
    Object.assign(form, { toiletId: null, reportType: '', description: '', images: [] })
    fetchMyReports()
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  } finally { submitting.value = false }
}

onMounted(async () => {
  fetchMyReports()
  try { const res = await getToiletList(); toiletList.value = res.data || [] } catch { toiletList.value = [] }
})
</script>

<style scoped>
.page-crud { max-width: 800px; }
.page-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
.page-card__header { margin-bottom: 20px; }
.page-card__title {
  font-size: 18px;
  font-weight: 600;
  color: #1F2937;
  margin: 0;
}
.page-card__desc {
  font-size: 13px;
  color: #9CA3AF;
  margin: 6px 0 0 0;
}
.upload-area { width: 100%; }
.upload-images { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 10px; }
.upload-image-item { position: relative; }
.upload-input { display: flex; align-items: center; }
</style>
